/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.korgwavestation;

import edisyn.*;
import edisyn.gui.*;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.border.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javax.sound.midi.*;

/**
   A patch editor for the Korg SG RACK [Multi].  Note NOT for the SG Pro X.
        
   @author Sean Luke
*/

public class KorgWavestationPerformance extends KorgWavestationAbstract
    {
    /// Various collections of parameter names for pop-up menus
    
    public static final String[] SCALES = new String[] { "Equal Temperment 1", "Equal Temperment 2", "Pure Major", "Pure Minor", "User 1", "User 2", "User 3", "User 4", "User 5", "User 6", "User 7", "User 8", "User 9", "User 10", "User 11", "User 12" };
    public static final String[] PLAY_MODES = new String[] { "Local", "MIDI (Off)", "Local + MIDI" };
    public static final String[] POLYPHONY = new String[] { "Polyphonic", "Unison Retrigger", "Unison Legato" };
    public static final String[] KEY_PRIORITY = new String[] { "Low Note", "High Note", "Last Note" };
    public static final String[] HIGH_BUSSES = new String[] { "B", "C", "C + D", "D", "All", "Patch" };
    public static final String[] VOCODER_BUSSES = new String[] { "A", "B", "C", "D" };
    public static final String[] CONTROL_SOURCES = new String[] { "Normal", "Bus A+B", "Bus C+D", "Bus A/B/C/D" };
    public static final String[] SERIES_MIX = new String[] { "Off", "Dry", "9/1", "8/2", "7/3", "6/4", "5/5", "4/6", "3/7", "2/8", "1/9", "Wet" };
    public static final String[] PARALLEL_MIX = new String[] { "Off", "Left", "9/1", "8/2", "7/3", "6/4", "5/5", "4/6", "3/7", "2/8", "1/9", "Right" };

    public static final String[] FACTORS = new String[] { "1/1", "1/2", "1/3", "1/4", "1/5", "1/6", "1/7", "1/8", "2/3", "2/5", "2/7", "3/4", "3/5", "3/7", "3/8", "4/5", "4/7", "5/6", "5/7", "5/8", "6/7", "7/8", "2/1", "3/1", "4/1", "5/1", "6/1", "7/1", "8/1", "3/2", "5/2", "7/2", "4/3", "5/3", "7/3", "8/3", "5/4", "7/4", "6/5", "7/5", "8/5", "7/6", "8/7"  };

    public static final String[] CHORUS_LFO_RATE = new String[] 
    {
    "0.03", "0.06", "0.09", "0.12", "0.15", "0.18", "0.21", "0.24", "0.27", "0.30", "0.33", "0.36", "0.39", "0.42", "0.45", "0.48", "0.51", "0.54", "0.57", "0.60", "0.63", "0.66", "0.69", "0.73", "0.76", "0.80", "0.84", "0.88", "0.93", "0.97", "1.00",
    "1.07", "1.13", "1.18", "1.24", "1.31", "1.37", "1.44", "1.51", "1.59", "1.67", "1.75", "1.84", "1.93", "2.00",
    "2.13", "2.24", "2.35", "2.47", "2.59", "2.72", "2.86", "3.00",
    "3.15", "3.31", "3.48", "3.65", "3.83", "4.00",
    "4.23", "4.44", "4.66", "4.90", "5.14", "5.40", "5.67", "6.00",
    "6.26", "6.57", "6.90", "7.25", "7.61", "8.00",
    "8.39", "8.82", "9.26", "9.72", "10.21", "10.72", "11.26", "11.83", "12.42", "13.00",
    "13.70", "14.39", "15.11", "15.87", "16.67", "17.00",
    "18.38", "19.30", "20.27", "21.29", "22.36", "23.48", "24.66", "26.00",
    "27.00",
    "28.57", "30.00"
    };
                
    public static final String[] SPLIT_POINTS = new String[] { "160Hz", "200Hz", "250Hz", "320Hz", "400Hz", "500Hz", "640Hz", "800Hz", "1KHz", "1.25KHz", "1.6KHz", "2KHz", "2.5KHz", "3.2KHz", "4KHz", "5KHz", "6.4KHz", "8KHz", "10KHz" };

    public static final String[] FREQUENCIES = new String[] { "20Hz", "25Hz", "32Hz", "40Hz", "50Hz", "64Hz", "80Hz", "100Hz", "125Hz", "160Hz", "200Hz", "250Hz", "320Hz", "400Hz", "500Hz", "640Hz", "800Hz", "1KHz", "1.25KHz", "1.6KHz", "2KHz", "2.5KHz", "3.2KHz", "4KHz", "5KHz", "6.4Khz", "8KHz", "10KHz", "12.5KHz", "15KHz" };

    public static final String[] LFO_SHAPES = new String[] { "Tri 10", "Tri 9", "Tri 8", "Tri 7", "Tri 6", "Tri 5", "Tri 4", "Tri 3", "Tri 2", "Tri 1", "Tri 0", "Tri -1", "Tri -2", "Tri -3", "Tri -4", "Tri -5", "Tri -6", "Tri -7", "Tri -8", "Tri -9", "Tri -10",
                                                             "Sin -10", "Sin -9", "Sin -8", "Sin -7", "Sin -6", "Sin -5", "Sin -4", "Sin -3", "Sin -2", "Sin -1", "Sin 0", "Sin 1", "Sin 2", "Sin 3", "Sin 4", "Sin 5", "Sin 6", "Sin 7", "Sin 8", "Sin 9", "Sin 10" };


    //// EXPLANATION OF FX PARAMETER ARRAYS
    ////
    ////
    //// Including "No Effects", there are 56 effects in the SR.  8 of these effects are "extended effects"
    //// not found in the original Wavestation.  These effects are organized into certain groups where
    //// they have parameters in common.   Edisyn breaks them into 31 groups.  The FX_MAP array below 
    //// indicates, for each effect, the group it is in.
    ////
    //// Next comes FX_PARAMETERS.  This is an array of arrays of parameters, ONE ARRAY PER GROUP.  The parameters
    //// are Korg Wavestation parameters (see the sysex document) for emitting individual parameter values.
    ////
    //// Next comes FX_INDICES.  This is again an array of arrays of integers, one array per group.  The integers indicate
    //// which parameter number (of the form "fx" + fx-num [1 or 2] + "class" + fx-group + "param" + index) is associated with
    //// that FX_PARAMETERS entry.
    ////
    //// Next comes FX_PCL_LIST.  This is a copy of the pcl list in Korg's Effects.txt document, slightly tweaked to
    //// match Edisyn's parameter group choices.  For each FX group,
    //// it contains an array "PCR" arrays, each of which explains how to map a piece of the effects block into a given
    //// parameter.  PCR array entry 3 (the fourth one) is the FX INDEX to be modified by that piece of data.
    ////
    //// Next comes FX_PRESETS.  This is another copy from the presets list in Korg's Effects.txt document, slightly tweaked.
    //// For each FX, it contains an array of preset values, corresponding to elements in that FX type's PCL List.

    static final int FX_MAP[] = new int[]
    {
    0,              // Off
    1,              // Small Hall
    1,              // Medium Hall
    2,              // Large Hall
    1,              // Small Room
    1,              // Large Room
    1,              // Live Stage
    1,              // Wet Plate
    1,              // Dry Plate
    1,              // Spring Reverb
    3,              // Early Reflections 1
    3,              // Early Reflections 2
    3,              // Early Reflections 3
    4,              // Gated Reverb
    4,              // Reverse Gate
    5,              // Stereo Delay
    5,              // Ping-Pong Delay
    6,              // Dual Mono Delay
    7,              // Multi-Tap Delay 1
    7,              // Multi-Tap Delay 2
    7,              // Multi-Tap Delay 3
    8,              // Stereo Chorus
    8,              // Quadrature Chorus
    8,              // Crossover Chorus
    9,              // Harmonic Chorus
    10,             // Stereo Flanger
    10,             // Stereo Flanger
    10,             // Crossover Flanger
    11,             // Enhancer/Exciter
    12,             // Distortion-Filter
    12,             // Overdrive-Filter
    13,             // Stereo Phaser 1
    13,             // Stereo Phaser 2
    14,             // Rotary Speaker
    15,             // Stereo Mod-Pan
    15,             // Quadrature Mod-Pan
    16,             // Stereo Parametric EQ
    17,             // Chorus-Stereo Delay
    18,             // Flanger-Stereo Delay
    19,             // Dual Mono Delay/Hall Reverb
    20,             // Dual Mono Delay/Room Reverb
    21,             // Dual Mono Delay/Chorus
    22,             // Dual Mono Delay/Flanger
    23,             // Dual Mono Delay/Distortion
    23,             // Dual Mono Delay/Overdrive
    24,             // Dual Mono Delay/Phaser
    25,             // Dual Mono Delay/Rotary Speaker
    26,             // Stereo Pitch Shiter
    27,             // Modulatable Pitch Shifter-Delay
    28,             // Stereo Compressor-Limiter/Gate
    29,             // Small Vocoder 1
    29,             // Small Vocoder 2
    29,             // Small Vocoder 3
    29,             // Small Vocoder 4
    30,             // Stereo Vocoder-Delay 1
    30,             // Stereo Vocoder-Delay 2
    };


    public static final int[][] FX_PARAMETERS = new int[][] {
// NO EFFECT
            {
            },

// REVERB EQ
            {
            FX_100_WET_DRY0,                // Dry Wet
            FX_MOD1,                                // Mix Mod Source
            FX_PARAM2,                      // Mix Mod Amount
            FX_UPARAM5,                     // Decay time?              // 6?
            FX_UPARAM3,                     // Pre Delay
            FX_UPARAM4,                     // Early Ref
            FX_UPARAM6,                     // HiFreq                   // 5?
            FX_PARAM11,             // EQ Low
            FX_PARAM12,             // EQ Hi
            },

// REVERB EQ (LARGE HALL ONLY)
            {
            FX_100_WET_DRY0,                // Dry Wet
            FX_MOD1,                                // Mix Mod Source
            FX_PARAM2,                      // Mix Mod Amount
            FX_UPARAM5,                     // Decay time?              // 6?
            FX_UPARAM3,                     // Pre Delay
            FX_UPARAM4,                     // Early Ref
            FX_UPARAM6,                     // HiFreq                   // 5?
            FX_PARAM11,             // EQ Low
            FX_PARAM12,             // EQ Hi
            },

// EARLY REFLECTIONS
            {
            FX_100_WET_DRY0,                // Dry Wet
            FX_MOD1,                                // Mix Mod Source
            FX_PARAM2,                      // Mix Mod Amount
            FX_UPARAM4,                     // Decay Time
            FX_UPARAM3,                     // Pre Delay
            FX_PARAM5,                      // EQ Low
            FX_PARAM6,                      // EQ High
            },

// GATED REVERB
            {
            FX_100_WET_DRY0,                // Dry Wet
            FX_UPARAM5,                     // Decay Time
            FX_UPARAM4,                     // Pre Delay
            FX_UPARAM1,                     // Gate Hold
            FX_MOD2,                                // Gate Key Mod
            FX_UPARAM3,                     // Gate Thresh
            },

// STEREO DELAY AND PING PONG DELAY
            {
            FX_100_WET_DRY0,                // Dry Wet      
            FX_MOD1,                                // Mix Mod Source
            FX_PARAM2,                      // Mix Mod Amount
            FX_UPARAM6,                     // Delay Time
            FX_MOD4,                                // Delay Mod Source
            FX_PARAM5,                      // Delay Mod Amount
            FX_DELAY_FACTOR7,       // Delay Mod Factor
            FX_PARAM13,                     // Feedback
            FX_MOD10,                       // Level Mod Source
            FX_PARAM11,                     // Level Mod AMount
            },

// DUAL MONO DELAY
            {
            FX_11_WET_DRY0,         // Dry/Wet Mix A
            FX_UPARAM1,                     // Delay A
            FX_PARAM2,                      // Feedback A
            FX_11_WET_DRY3,         // Dry/Wet Mix B
            FX_UPARAM4,                     // Delay B
            FX_PARAM5,                      // Feedback B
            },

// MULTI-TAP DELAY
            {
            FX_100_WET_DRY0,                // DRY/WET
            FX_MOD1,                                // MIX MOD Source
            FX_PARAM2,                      // MIX MOD AMOUNT
            FX_UPARAM6,                     // Delay time 1
            FX_UPARAM7,                     // Delay time 2
            FX_PARAM8,                      // Feedback
            FX_MOD4,                                // Level Mod Source
            FX_PARAM5,                      // Level Mod Amount
            FX_PARAM9,                      // EQ Low
            FX_PARAM10,                     // EQ Hi
            },

// STEREO CHORUS AND CROSSOVER CHORUS
            {
            FX_UPARAM8,                     // LFO Depth
            FX_LFO_RATE5,           // LFO Rate
            FX_MOD6,                                // LFO Rate Mod Source
            FX_PARAM7,                      // LFO Rate Mod Amount
            FX_LFO_SHAPE,           // LFO Shape         -- this is 9
            FX_UPARAM3,                     // Delay Left
            FX_UPARAM4,                     // Delay Right
            FX_FOOTSWITCH_ENABLE1,  // Footswitch
            FX_PARAM10,                     // EQ Low
            FX_PARAM11,                     // EQ HI
            },

// HARMONIC CHORUS
            {
            FX_UPARAM7,                     // LFO Depth
            FX_MOD8,                                // Depth Mod Source
            FX_PARAM9,                      // Depth Mod Amount
            FX_LFO_RATE4,           // LFO Rate
            FX_MOD5,                                // LFO Rate Mod Source
            FX_PARAM6,                      // LFO Rate Mod Amount
            FX_SPLIT_POINT10,       // Split Point
            FX_UPARAM2,                     // Delay Left
            FX_UPARAM3,                     // Delay Right
            FX_FOOTSWITCH_ENABLE1,  // Footswitch
            },

// FLANGER
            {
            FX_UPARAM4,                     // LFO Range
            FX_RAMP5,                       // Ramp Speed
            FX_MOD6,                                // Ramp Speed Mod Source
            FX_PARAM7,                      // Ramp Speed Mod Amount
            FX_PARAM8,                      // Resonance
            FX_UPARAM3,                     // Top Delay
            FX_PARAM0,                      // Output Mix
            FX_FOOTSWITCH_ENABLE1,  // Footswitch
            FX_PARAM9,                      // EQ Low
            FX_PARAM10,                     // EQ Hi
            },

// ENHANCER / EXCITER
            {
            FX_11_WET_DRY0,         // Dry/Wet
            FX_UPARAM1,                     // Excitation
            FX_UPARAM2,                     // Hot Spot
            FX_UPARAM3,                     // Stereo Width
            FX_UPARAM4,                     // Crossover Delay
            FX_PARAM5,                      // EQ Low
            FX_PARAM6,                      // EQ Hi
            },

// DISTORTION / FILTER
            {
            FX_10_WET_DRY0,         // Wet/Dry
            FX_UPARAM2,                     // Edge
            FX_UPARAM3,                     // Hot Spot
            FX_MOD4,                                // Hot Spot Mod Source
            FX_PARAM5,                      // Hot Spot Mod Amount
            FX_UPARAM6,                     // Resonance
            FX_UPARAM9,                     // Output Level
            FX_MOD10,                       // Output Level Mod Source
            FX_PARAM11,                     // Output Level Mod Amount
            FX_FOOTSWITCH_ENABLE1,          // Footswitch
            FX_PARAM7,                      // EQ Low
            FX_PARAM8,                      // EQ High
            },

// STEREO PHASER
            {
            FX_10_WET_DRY0,         // WET/DRY
            FX_UPARAM6,                     // LFO Depth
            FX_MOD7,                                // LFO Depth Mod Source
            FX_PARAM8,                      // LFO Depth Mod Amount
            FX_LFO_RATE3,           // LFO Rate
            FX_MOD4,                                // LFO Rate Mod Source
            FX_PARAM5,                      // LFO Rate Mod Amount
            FX_UPARAM2,                     // Center Frequency
            FX_PARAM9,                      // Feedback
            FX_FOOTSWITCH_ENABLE1,  // Footswitch
            },

// ROTARY SPEAKER
            {
            FX_10_WET_DRY0,         // Wet/Dry
            FX_UPARAM2,                     // Depth
            FX_LFO_RATE6,           // Slow Speed
            FX_LFO_RATE7,           // Fast Speed
            FX_MOD5,                                // Speed Mod Source
            FX_UPARAM4,                     // Acceleration
            FX_FOOTSWITCH_ENABLE1,  // Footswitch
            },

// STEREO MOD PAN
            {
            FX_11_WET_DRY0,         // Wet/Dry
            FX_MOD1,                                // Dry/Wet Mix Mod Source
            FX_PARAM2,                      // Dry/Wet Mix Mod Amount
            FX_UPARAM4,                     // LFO Depth
            FX_MOD5,                                // LFO Depth Mod Source
            FX_PARAM6,                      // LFO Depth Mod Amount
            FX_LFO_RATE3,           // LFO Rate
            FX_PARAM7,                      // EQ Low
            FX_PARAM8,                      // EQ High
            },

// STEREO PARAMETRIC EQ
            {
            FX_EQ_FREQ_HIGH7,       // Hi Frequency
            FX_PARAM8,                      // High Level
            FX_UPARAM2,                     // Mid Frequency
            FX_PARAM5,                      // Mid Level
            FX_UPARAM6,                     // Mid Width
            FX_MOD3,                                // Mid Frequency Mod Source
            FX_PARAM4,                      // Mid Frequency Mod Amount
            FX_EQ_FREQ_LOW0,                // Low Frequency
            FX_PARAM1,                      // Low Level
            },

// CHORUS / DELAY
            {
            FX_100_WET_DRY4,                // Wet/Dry
            FX_UPARAM0,                     // Chorus Delay
            FX_UPARAM2,                     // LFO Depth
            FX_LFO_RATE1,           // LFO Rate
            FX_PARAM3,                      // Chorus Feedback
            FX_UPARAM5,                     // Delay Time
            FX_PARAM8,                      // Delay Feedback
            FX_FOOTSWITCH_ENABLE6,  // Footswitch
            FX_PARAM9,                      // EQ Low
            FX_PARAM10,                     // EQ High
            },

// FLANGER / DELAY
            {
            FX_100_WET_DRY4,                // Wet/Dry
            FX_UPARAM0,                     // Flanger Delay
            FX_UPARAM2,                     // LFO Depth
            FX_LFO_RATE1,           // LFO Rate
            FX_PARAM3,                      // Flanger Feedback
            FX_UPARAM5,                     // Delay Time
            FX_PARAM8,                      // Delay Feedback
            FX_FOOTSWITCH_ENABLE6,  // Footswitch
            FX_PARAM9,                      // EQ Low
            FX_PARAM10,                     // EQ High
            },

// DELAY / REVERB (HALL)
            {
            FX_11_WET_DRY0,         // Delay Wet/Dry
            FX_UPARAM1,                     // Delay Time
            FX_PARAM2,                      // Delay Feedback
            FX_100_WET_DRY3,                // Reverb Wet/Dry
            FX_UPARAM5,                     // Reverb Decay Time
            FX_UPARAM4,                     // Reverb Predelay
            FX_UPARAM6,                     // High Frequency Damping
            },

// DELAY / REVERB (ROOM)
            {
            FX_11_WET_DRY0,         // Delay Wet/Dry
            FX_UPARAM1,                     // Delay Time
            FX_PARAM2,                      // Delay Feedback
            FX_100_WET_DRY3,                // Reverb Wet/Dry
            FX_UPARAM5,                     // Reverb Decay Time
            FX_UPARAM4,                     // Reverb Predelay
            FX_UPARAM6,                     // High Frequency Damping
            },

// DELAY/CHORUS
            {
            FX_11_WET_DRY0,         // Delay Wet/Dry
            FX_UPARAM1,                     // Delay Time
            FX_PARAM2,                      // Delay Feedback
            FX_UPARAM3,                     // Chorus Delay
            FX_UPARAM5,                     // LFO Depth
            FX_LFO_RATE4,           // LFO Rate
            FX_PARAM6,                      // Chorus Feedback
            },

// DELAY/FLANGER
            {
            FX_11_WET_DRY0,         // Delay Wet/Dry
            FX_UPARAM1,                     // Delay Time
            FX_PARAM2,                      // Delay Feedback
            FX_UPARAM3,                     // Chorus Delay
            FX_UPARAM5,                     // LFO Depth
            FX_LFO_RATE4,           // LFO Rate
            FX_PARAM6,                      // Chorus Feedback
            },

// DELAY/DISTORTION AND DELAY/OVERDRIVE
            {
            FX_11_WET_DRY0,         // Delay Dry/Wet
            FX_UPARAM1,                     // Delay Time
            FX_PARAM2,                      // Delay Feedback
            FX_UPARAM3,                     // Edge
            FX_UPARAM4,                     // Hot Spot
            FX_UPARAM5,                     // Resonance
            FX_UPARAM6,                     // Distortion Output Level
            },

// DELAY/PHASER
            {
            FX_11_WET_DRY0,         // Wet/Dry
            FX_UPARAM1,                     // Delay Time
            FX_PARAM2,                      // Delay Feedback
            FX_UPARAM3,                     // Phaser Center
            FX_UPARAM5,                     // LFO Depth
            FX_LFO_RATE4,           // LFO Rate
            FX_PARAM6,                      // Phaser Feedback
            },

// DELAY/ROTARY
            {
            FX_11_WET_DRY0,         // Delay Wet/Dry
            FX_UPARAM1,                     // Delay Time
            FX_PARAM2,                      // Delay Feedback
            FX_LFO_RATE6,           // Slow Speed
            FX_LFO_RATE7,           // Fast Speed
            FX_MOD5,                                // Speed Mod Source
            FX_UPARAM4,                     // Acceleration
            },

// PITCH SHIFT
            {
            FX_11_WET_DRY0,         // Wet/Dry (Delay)
            FX_MOD1,                                // Mix Mod Source
            FX_PARAM2,                      // Mix Mod Amount
            FX_UPARAM3,                     // Pitch Shift
            FX_UPARAM4,                     // Delay Left
            FX_UPARAM5,                     // Delay Right
            },

// MODULATABLE PITCH-SHIFTER DELAY
            {
            FX_11_WET_DRY0,         // Wet/Dry (Delay)
            FX_MOD1,                                // Mix Modulation Source
            FX_PARAM2,                      // Mix Modulation Amount
            FX_PARAM3,                      // Max Shift
            FX_UPARAM4,                     // Shift Scaler
            FX_MOD5,                                // Shift Scaler Modulation Source
            FX_PARAM6,                      // Shift Scaler Modulation Amount
            FX_UPARAM8,                     // Delay Left
            FX_UPARAM7,                     // Delay Right          (interesting, backwards)
            FX_UPARAM9,                     // Feedback
            },

// STEREO COMPRESSOR-LIMITER/GATE
            {
            COMP_CONTROL0,          // Control Source
            FX_UPARAM1,                     // Sensitivity
            FX_UPARAM2,                     // Compression Ratio
            FX_UPARAM3,                     // Compression Threshold
            FX_UPARAM4,                     // Gate Threshold
            FX_UPARAM5,                     // Output Level
            },

// SMALL VOCODER
            {
            FX_BUS0,                                // Modulator Bus
            FX_UPARAM1,                     // Modulator Sensitivity
            FX_BUS2,                                // Carrier Bus
            FX_UPARAM3,                     // Carrier Sensitivity
            FX_UPARAM4,                     // Sibilance
            FX_MOD5,                                // Sibiliance Mod Source
            FX_PARAM6,                      // Sibilance Mod Amount
            },

// STEREO VOCODER
            {
            FX_BUS0,                                // Modulator Bus
            FX_UPARAM1,                     // Modulator Sensitivity
            FX_BUS2,                                // Carrier Bus
            FX_UPARAM3,                     // Carrier Sensitivity
            FX_UPARAM4,                     // Sibilance
            FX_MOD5,                                // Sibilance Mod Source
            FX_PARAM6,                      // Sibilance Mod Amount
            FX_UPARAM7,                     // Stereo Width
            FX_UPARAM8,                     // Delay Time
            FX_UPARAM9,                     // Delay Feedback
            FX_UPARAM10,                    // Delay Level
            },
        };



    public static final int[][] FX_INDICES = new int[][] {

// NO EFFECT
            {
            },

// REVERB EQ
            {
            0,                // Dry Wet
            1,                                // Mix Mod Source
            2,                      // Mix Mod Amount
            5,                     // Decay time?               // 6?
            3,                     // Pre Delay
            4,                     // Early Ref
            6,                     // HiFreq                    // 5?
            11,             // EQ Low
            12,             // EQ Hi
            },

// REVERB EQ (HALL ONLY)
            {
            0,                // Dry Wet
            1,                                // Mix Mod Source
            2,                      // Mix Mod Amount
            5,                     // Decay time?               // 6?
            3,                     // Pre Delay
            4,                     // Early Ref
            6,                     // HiFreq                    // 5?
            11,             // EQ Low
            12,             // EQ Hi
            },

// EARLY REFLECTIONS
            {
            0,                // Dry Wet
            1,                                // Mix Mod Source
            2,                      // Mix Mod Amount
            4,                     // Decay Time
            3,                     // Pre Delay
            5,                      // EQ Low
            6,                      // EQ High
            },

// GATED REVERB
            {
            0,                // Dry Wet
            5,                     // Decay Time
            4,                     // Pre Delay
            1,                     // Gate Hold
            2,                                // Gate Key Mod
            3,                     // Gate Thresh
            },

// STEREO DELAY AND PING PONG DELAY
            {
            0,                // Dry Wet      
            1,                                // Mix Mod Source
            2,                      // Mix Mod Amount
            6,                     // Delay Time
            4,                                // Delay Mod Source
            5,                      // Delay Mod Amount
            7,       // Delay Mod Factor
            3,                     // Feedback
            10,                       // Level Mod Source
            11,                     // Level Mod AMount
            },

// DUAL MONO DELAY
            {
            0,         // Dry/Wet Mix A
            1,                     // Delay A
            2,                      // Feedback A
            3,         // Dry/Wet Mix B
            4,                     // Delay B
            5,                      // Feedback B
            },

// MULTI-TAP DELAY
            {
            0,                // DRY/WET
            1,                                // MIX MOD Source
            2,                      // MIX MOD AMOUNT
            6,                     // Delay time 1
            7,                     // Delay time 2
            8,                      // Feedback
            4,                                // Level Mod Source
            5,                      // Level Mod Amount
            9,                      // EQ Low
            10,                     // EQ Hi
            },

// STEREO CHORUS AND CROSSOVER CHORUS
            {
            8,                     // LFO Depth
            5,           // LFO Rate
            6,                                // LFO Rate Mod Source
            7,                      // LFO Rate Mod Amount
            9,           // LFO Shape    -- this is 9
            3,                     // Delay Left
            4,                     // Delay Right
            1,  // Footswitch
            10,                     // EQ Low
            11,                     // EQ HI
            },

// HARMONIC CHORUS
            {
            7,                     // LFO Depth
            8,                                // Depth Mod Source
            9,                      // Depth Mod Amount
            4,           // LFO Rate
            5,                                // LFO Rate Mod Source
            6,                      // LFO Rate Mod Amount
            10,       // Split Point
            2,                     // Delay Left
            3,                     // Delay Right
            1,  // Footswitch
            },

// FLANGER
            {
            4,                     // LFO Range
            5,                       // Ramp Speed
            6,                                // Ramp Speed Mod Source
            7,                      // Ramp Speed Mod Amount
            8,                      // Resonance
            3,                     // Top Delay
            0,                      // Output Mix
            1,  // Footswitch
            9,                      // EQ Low
            10,                     // EQ Hi
            },

// ENHANCER / EXCITER
            {
            0,         // Dry/Wet
            1,                     // Excitation
            2,                     // Hot Spot
            3,                     // Stereo Width
            4,                     // Crossover Delay
            5,                      // EQ Low
            6,                      // EQ Hi
            },

// DISTORTION / FILTER
            {
            0,         // Wet/Dry
            2,                     // Edge
            3,                     // Hot Spot
            4,                                // Hot Spot Mod Source
            5,                      // Hot Spot Mod Amount
            6,                     // Resonance
            9,                     // Output Level
            10,                       // Output Level Mod Source
            11,                     // Output Level Mod Amount
            1,          // Footswitch
            7,                      // EQ Low
            8,                      // EQ High
            },

// STEREO PHASER
            {
            0,         // WET/DRY
            6,                     // LFO Depth
            7,                                // LFO Depth Mod Source
            8,                      // LFO Depth Mod Amount
            3,           // LFO Rate
            4,                                // LFO Rate Mod Source
            5,                      // LFO Rate Mod Amount
            2,                     // Center Frequency
            9,                      // Feedback
            1,  // Footswitch
            },

// ROTARY SPEAKER
            {
            0,         // Wet/Dry
            2,                     // Depth
            6,           // Slow Speed
            7,           // Fast Speed
            5,                                // Speed Mod Source
            4,                     // Acceleration
            1,  // Footswitch
            },

// STEREO MOD PAN
            {
            0,         // Wet/Dry
            1,                                // Dry/Wet Mix Mod Source
            2,                      // Dry/Wet Mix Mod Amount
            4,                     // LFO Depth
            5,                                // LFO Depth Mod Source
            6,                      // LFO Depth Mod Amount
            3,           // LFO Rate
            7,                      // EQ Low
            8,                      // EQ High
            },

// STEREO PARAMETRIC EQ
            {
            7,       // Hi Frequency
            8,                      // High Level
            2,                     // Mid Frequency
            5,                      // Mid Level
            6,                     // Mid Width
            3,                                // Mid Frequency Mod Source
            4,                      // Mid Frequency Mod Amount
            0,                // Low Frequency
            1,                      // Low Level
            },

// CHORUS / DELAY
            {
            4,                // Wet/Dry
            0,                     // Chorus Delay
            2,                     // LFO Depth
            1,           // LFO Rate
            3,                      // Chorus Feedback
            5,                     // Delay Time
            8,                      // Delay Feedback
            6,  // Footswitch
            9,                      // EQ Low
            10,                     // EQ High
            },

// FLANGER / DELAY
            {
            4,                // Wet/Dry
            0,                     // Flanger Delay
            2,                     // LFO Depth
            1,           // LFO Rate
            3,                      // Flanger Feedback
            5,                     // Delay Time
            8,                      // Delay Feedback
            6,  // Footswitch
            9,                      // EQ Low
            10,                     // EQ High
            },

// DELAY / REVERB (HALL)
            {
            0,         // Delay Wet/Dry
            1,                     // Delay Time
            2,                      // Delay Feedback
            3,                // Reverb Wet/Dry
            5,                     // Reverb Decay Time
            4,                     // Reverb Predelay
            6,                     // High Frequency Damping
            },

// DELAY / REVERB (ROOM)
            {
            0,         // Delay Wet/Dry
            1,                     // Delay Time
            2,                      // Delay Feedback
            3,                // Reverb Wet/Dry
            5,                     // Reverb Decay Time
            4,                     // Reverb Predelay
            6,                     // High Frequency Damping
            },

// DELAY/CHORUS
            {
            0,         // Delay Wet/Dry
            1,                     // Delay Time
            2,                      // Delay Feedback
            3,                     // Chorus Delay
            5,                     // LFO Depth
            4,           // LFO Rate
            6,                      // Chorus Feedback
            },

// DELAY/FLANGER
            {
            0,         // Delay Wet/Dry
            1,                     // Delay Time
            2,                      // Delay Feedback
            3,                     // Chorus Delay
            5,                     // LFO Depth
            4,           // LFO Rate
            6,                      // Chorus Feedback
            },

// DELAY/DISTORTION
            {
            0,         // Delay Dry/Wet
            1,                     // Delay Time
            2,                      // Delay Feedback
            3,                     // Edge
            4,                     // Hot Spot
            5,                     // Resonance
            6,                     // Distortion Output Level
            },

// DELAY/PHASER
            {
            0,         // Wet/Dry
            1,                     // Delay Time
            2,                      // Delay Feedback
            3,                     // Phaser Center
            5,                     // LFO Depth
            4,           // LFO Rate
            6,                      // Phaser Feedback
            },

// DELAY/ROTARY
            {
            0,         // Delay Wet/Dry
            1,                     // Delay Time
            2,                      // Delay Feedback
            6,           // Slow Speed
            7,           // Fast Speed
            5,                                // Speed Mod Source
            4,                     // Acceleration
            },

// PITCH SHIFT
            {
            0,         // Wet/Dry (Delay)
            1,                                // Mix Mod Source
            2,                      // Mix Mod Amount
            3,                     // Pitch Shift
            4,                     // Delay Left
            5,                     // Delay Right
            },

// MODULATABLE PITCH-SHIFTER DELAY
            {
            0,         // Wet/Dry (Delay)
            1,                                // Mix Modulation Source
            2,                      // Mix Modulation Amount
            3,                      // Max Shift
            4,                     // Shift Scaler
            5,                                // Shift Scaler Modulation Source
            6,                      // Shift Scaler Modulation Amount
            8,                     // Delay Left
            7,                     // Delay Right          (interesting, backwards)
            9,                     // Feedback
            },

// STEREO COMPRESSOR-LIMITER/GATE
            {
            0,          // Control Source
            1,                     // Sensitivity
            2,                     // Compression Ratio
            3,                     // Compression Threshold
            4,                     // Gate Threshold
            5,                     // Output Level
            },

// SMALL VOCODER
            {
            0,                                // Modulator Bus
            1,                     // Modulator Sensitivity
            2,                                // Carrier Bus
            3,                     // Carrier Sensitivity
            4,                     // Sibilance
            5,                                // Sibiliance Mod Source
            6,                      // Sibilance Mod Amount
            },

// STEREO VOCODER,
            {
            0,                                // Modulator Bus
            1,                     // Modulator Sensitivity
            2,                                // Carrier Bus
            3,                     // Carrier Sensitivity
            4,                     // Sibilance
            5,                                // Sibilance Mod Source
            6,                      // Sibilance Mod Amount
            7,                     // Stereo Width
            8,                     // Delay Time
            9,                     // Delay Feedback
            10,                    // Delay Level
            },

        };




    public static final int[][][] FX_PCL_LIST = new int[][][] {
            {
//// HEADER INFORMATION [STORED HERE AS PCL 0]
            { 2, 0, 4, 0, 0 }, // p0 = mix 3 level
            { 3, 0, 4, 1, 0 }, // p1 = mix 3 mod src
            { 4, 0, 4, 2, 0 }, // p2 = mix 3 mod amt
            { 0, 6, -1, 2, 4 }, // p2 = mix 3 mod amt (sign)
            { 2, 4, 4, 3, 0 }, // p3 = mix 4 level
            { 3, 4, 4, 4, 0 }, // p4 = mix 4 mod src
            { 4, 4, 4, 5, 0 }, // p5 = mix 4 mod amt
            { 1, 6, -1, 5, 4 }, // p2 = mix 4 mod amt (sign)
            },
            
// REVERB EQ
            {
            { 0, 0, 4, 0, 0 }, // p0 = dry/wet mix level
            { 6, 5, 3, 0, 4 }, // p0 = dry/wet mix level (high)
            { 0, 4, 4, 1, 0 }, // p1 = dry/wet mix mod src
            { 1, 0, 4, 2, 0 }, // p2 = dry/wet mix mod amt
            { 3, 7, -1, 2, 4 }, // p2 = dry/wet mix mod amt (sign)
            { 2, 0, 8, 3, 0 }, // p3 = pre delay
            { 7, 5, 1, 3, 8 }, // p3 = pre delay (high bit)
            { 5, 4, 4, 4, 0 }, // p4 = ER level
            { 4, 0, 7, 5, 0 }, // p5 = HF damping factor
            { 3, 0, 7, 6, 0 }, // p6 = decay time
            { 5, 0, 4, 7, 0 }, // p7 = reserved for future use
            { 1, 4, 4, 8, 0 }, // p8 = reserved for future use
            { 4, 7, -1, 8, 4 }, // p8 = reserved for future use
            { 6, 0, -5, 11, 0 }, // p11 = low EQ
            { 7, 0, -5, 12, 0 }, // p12 = high EQ
            },

// REVERB EQ (LARGE HALL ONLY)
            {
            { 0, 0, 4, 0, 0 }, // p0 = dry/wet mix level
            { 6, 5, 3, 0, 4 }, // p0 = dry/wet mix level (high)
            { 0, 4, 4, 1, 0 }, // p1 = dry/wet mix mod src
            { 1, 0, 4, 2, 0 }, // p2 = dry/wet mix mod amt
            { 3, 7, -1, 2, 4 }, // p2 = dry/wet mix mod amt (sign)
            { 2, 0, 8, 3, 0 }, // p3 = pre delay
            { 7, 5, 1, 3, 8 }, // p3 = pre delay (high bit)
            { 5, 4, 4, 4, 0 }, // p4 = ER level
            { 4, 0, 7, 5, 0 }, // p5 = HF damping factor
            { 3, 0, 7, 6, 0 }, // p6 = decay time
            { 5, 0, 4, 7, 0 }, // p7 = reserved for future use
            { 1, 4, 4, 8, 0 }, // p8 = reserved for future use
            { 4, 7, -1, 8, 4 }, // p8 = reserved for future use
            { 6, 0, -5, 11, 0 }, // p11 = low EQ
            { 7, 0, -5, 12, 0 }, // p12 = high EQ
            },

// EARLY REFLECTIONS
            {
            { 0, 0, 7, 0, 0 }, // p0 = dry/wet mix level
            { 1, 0, 4, 1, 0 }, // p1 = dry/wet mix mod src
            { 2, 0, -5, 2, 0 }, // p2 = dry/wet mix mod amt
            { 3, 0, 8, 3, 0 }, // p3 = pre delay
            { 4, 0, 8, 4, 0 }, // p4 = decay time
            { 5, 0, -5, 5, 0 }, // p5 = low EQ
            { 6, 0, -5, 6, 0 }, // p6 = high EQ
            },
            {

// GATED REVERB
            { 0, 0, 7, 0, 0 }, // p0 = dry/wet mix level
            { 1, 0, 7, 1, 0 }, // p1 = gate hold time
            { 2, 0, 4, 2, 0 }, // p2 = key source
            { 3, 0, 8, 3, 0 }, // p3 = threshold
            { 4, 0, 8, 4, 0 }, // p4 = pre delay
            { 5, 0, 8, 5, 0 }, // p5 = decay time
            },

// STEREO DELAY AND PING PONG DELAY
            {
            { 0, 0, 4, 0, 0 }, // p0 = dry/wet mix level (low)
            { 7, 2, 3, 0, 4 }, // p0 = dry/wet mix level (high)
            { 0, 4, 4, 1, 0 }, // p1 = dry/wet mix mod src
            { 3, 6, 2, 2, 0 }, // p2 = dry/wet mix mod amt (low)
            { 5, 0, -3, 2, 2 }, // p2 = dry/wet mix mod amt (high)
            { 1, 0, 4, 10, 0 }, // p10 = input level mod src
            { 5, 3, -5, 11, 0 }, // p11 = input level mod amt
            { 6, 0, 8, 6, 0 }, // p6 = delay time (low)
            { 7, 0, 2, 6, 8 }, // p6 = delay time (high)
            { 1, 4, 4, 4, 0 }, // p4 = delay mod src
            { 2, 0, -8, 5, 0 }, // p5 = delay mod amt
            { 3, 0, 6, 7, 0 }, // p7 = delay factor
            { 4, 0, -8, 13, 0 }, // p13 = feedback
            },

// DUAL MONO DELAY
            {
            { 0, 0, 4, 0, 0 }, // p0 = dry/wet mix left
            { 1, 0, 8, 1, 0 }, // p1 = delay time left (low)
            { 2, 0, 2, 1, 8 }, // p1 = delay time left (high)
            { 3, 0, -8, 2, 0 }, // p2 = feedback left
            { 4, 0, 4, 3, 0 }, // p3 = dry/wet mix left
            { 5, 0, 8, 4, 0 }, // p4 = delay time left (low)
            { 6, 0, 2, 4, 8 }, // p4 = delay time left (high)
            { 7, 0, -8, 5, 0 }, // p5 = feedback left
            },

// MULTI-TAP DELAY
            {
            { 0, 0, 4, 0, 0 }, // p0 = dry/wet mix (low)
            { 7, 5, 3, 0, 4 }, // p0 = dry/wet mix (high)
            { 0, 4, 4, 1, 0 }, // p1 = dry/wet mix mod source
            { 1, 0, 4, 2, 0 }, // p2 = dry/wet mix mod amount (low)
            { 2, 5, -1, 2, 4 }, // p2 = dry/wet mix mod amount (high)
            { 1, 4, 4, 4, 0 }, // p4 = input level mod source
            { 2, 0, -5, 5, 0 }, // p5 = input level mod amount
            { 3, 0, 8, 6, 0 }, // p6 = delay time 1 (low)
            { 6, 5, 1, 6, 8 }, // p6 = delay time 1 (high)
            { 4, 0, 8, 7, 0 }, // p7 = delay time 2 (low)
            { 6, 6, 1, 7, 8 }, // p7 = delay time 2 (high)
            { 5, 0, -8, 8, 0 }, // p8 = feedback
            { 6, 0, -5, 9, 0 }, // p9 = low EQ
            { 7, 0, -5, 10, 0 }, // p10 = high EQ
            },

// STEREO CHORUS AND CROSSOVER CHORUS
            {
            { 6, 7, 1, 1, 0 }, // p1 = footswitch enable
            { 0, 0, 8, 3, 0 }, // p3 = delay time left
            { 4, 7, 1, 3, 8 }, // p3 = delay time left (high bit)
            { 1, 0, 8, 4, 0 }, // p4 = delay time right
            { 5, 7, 1, 4, 8 }, // p4 = delay time right (high bit)
            { 2, 0, 8, 5, 0 }, // p5 = LFO rate
            { 3, 0, 4, 6, 0 }, // p6 = LFO rate mod src
            { 3, 4, 4, 7, 0 }, // p7 = LFO rate mod amt
            { 7, 7, -1, 7, 4 }, // p7 = LFO rate mod amt (sign)
            { 4, 0, 7, 8, 0 }, // p8 = LFO depth
            { 5, 0, -7, 9, 0 }, // p9 = LFO shape
            { 6, 0, -5, 10, 0 }, // p10 = low EQ
            { 7, 0, -5, 11, 0 }, // p11 = high EQ
            },

// HARMONIC CHORUS
            {
            { 6, 7, 1, 1, 0 }, // p1 = footswitch enable
            { 0, 0, 8, 2, 0 }, // p2 = delay left
            { 5, 7, 1, 2, 8 }, // p2 = delay left (high bit)
            { 1, 0, 8, 3, 0 }, // p3 = delay right
            { 7, 7, 1, 3, 8 }, // p3 = delay right (high bit)
            { 2, 0, 8, 4, 0 }, // p4 = LFO rate
            { 4, 4, 4, 5, 0 }, // p5 = LFO rate mod src
            { 7, 0, -5, 6, 0 }, // p6 = LFO rate mod amt
            { 3, 0, 7, 7, 0 }, // p7 = LFO depth
            { 4, 0, 4, 8, 0 }, // p8 = LFO depth mod src
            { 5, 0, -5, 9, 0 }, // p9 = LFO depth mod amt
            { 6, 0, 7, 10, 0 }, // p10 = split point
            },

// FLANGER
            {
            { 7, 0, -5, 0, 0 }, // p0 = output mix
            { 1, 7, 1, 1, 0 }, // p1 = footswitch enable
            { 0, 0, 8, 3, 0 }, // p3 = top delay
            { 1, 0, 7, 4, 0 }, // p4 = range
            { 2, 0, 8, 5, 0 }, // p5 = ramp speed
            { 3, 0, 4, 6, 0 }, // p6 = ramp speed mod src
            { 4, 0, -8, 7, 0 }, // p7 = ramp speed mod amt
            { 5, 0, -8, 8, 0 }, // p8 = resonance
            { 6, 0, 4, 9, 0 }, // p9 = low EQ
            { 6, 4, 4, 10, 0 }, // p10 = high EQ
            { 3, 4, -1, 9, 5 }, // p9 = low EQ (sign)
            { 3, 5, -1, 10, 5 }, // p10 = high EQ (sign)
            },

// ENHANCER / EXCITER
            {
            { 0, 0, 4, 0, 0 }, // p0 = dry/wet mix
            { 1, 0, 7, 1, 0 }, // p1 = harmonic density
            { 2, 0, 4, 2, 0 }, // p2 = hot spot
            { 3, 0, 7, 3, 0 }, // p3 = stereo width
            { 4, 0, 7, 4, 0 }, // p4 = delay
            { 5, 0, -5, 5, 0 }, // p5 = low EQ
            { 6, 0, -5, 6, 0 }, // p6 = high EQ
            },

// DISTORTION / FILTER
            {
            { 0, 0, 4, 0, 0 }, // p0 = dry/wet mix
            { 2, 7, 1, 1, 0 }, // p1 = footswitch enable
            { 1, 0, 8, 2, 0 }, // p2 = edge (drive level)
            { 2, 0, 7, 3, 0 }, // p3 = hot spot (filter frequency)
            { 0, 4, 4, 4, 0 }, // p4 = hot spot mod source
            { 3, 0, -5, 5, 0 }, // p5 = hot spot mod amount
            { 4, 0, 7, 6, 0 }, // p6 = resonance
            { 5, 0, -5, 7, 0 }, // p7 = low EQ
            { 6, 0, -5, 8, 0 }, // p8 = high EQ
            { 7, 0, 7, 9, 0 }, // p9 = level (output level)
            { 3, 5, 3, 10, 0 }, // p9 = level mod source
            { 4, 7, 1, 10, 3 }, // p9 = level mod source (high bit)
            { 5, 5, 3, 11, 0 }, // p9 = level mod amount
            { 6, 5, -2, 11, 3 }, // p9 = level mod amount (high bits)
            },

// STEREO PHASER
            {
            { 0, 0, -5, 0, 0 }, // p0 = dry/wet mix
            { 1, 7, 1, 1, 0 }, // p1 = footswitch
            { 1, 0, 7, 2, 0 }, // p2 = center
            { 2, 0, 8, 3, 0 }, // p3 = LFO rate
            { 5, 4, 4, 4, 0 }, // p4 = LFO rate mod source
            { 3, 0, -5, 5, 0 }, // p5 = LFO rate mod amount
            { 4, 0, 7, 6, 0 }, // p6 = LFO depth 
            { 5, 0, 4, 7, 0 }, // p7 = LFO depth mod source
            { 6, 0, -5, 8, 0 }, // p8 = LFO depth mod amount
            { 7, 0, -8, 9, 0 }, // p9 = feedback
            },

// ROTARY SPEAKER
            {
            { 6, 0, 4, 0, 0 }, // p0 = dry/wet mix
            { 6, 4, 1, 1, 0 }, // p1 = footswitch enable
            { 5, 4, 4, 2, 0 }, // p2 = vibrato depth
            { 4, 4, 4, 4, 0 }, // p4 = rotor acceleration
            { 4, 0, 4, 5, 0 }, // p5 = rotor speed mod source
            { 3, 0, 8, 6, 0 }, // p6 = rotor slow speed
            { 2, 0, 8, 7, 0 }, // p7 = rotor fast speed
            { 1, 0, 8, 8, 0 }, // p8 = reserved for future use
            { 0, 0, 8, 9, 0 }, // p9 = reserved for future use
            },

// STEREO MOD PAN
            {
            { 0, 0, 4, 0, 0 }, // p0 = dry/wet mix
            { 0, 4, 4, 1, 0 }, // p1 = dry/wet mix mod source
            { 1, 0, -5, 2, 0 }, // p2 = dry/wet mix mod amount
            { 2, 0, 8, 3, 0 }, // p3 = LFO rate
            { 3, 0, 4, 4, 0 }, // p4 = LFO depth 
            { 3, 4, 4, 5, 0 }, // p5 = LFO depth mod source
            { 4, 0, -5, 6, 0 }, // p6 = LFO depth mod amount
            { 5, 0, -5, 7, 0 }, // p7 = low EQ
            { 6, 0, -5, 8, 0 }, // p8 = high EQ
            },

// STEREO PARAMETRIC EQ
            {
            { 6, 0, 6, 0, 0 }, // p0 = low freq
            { 0, 0, 4, 1, 0 }, // p1 = low level (low)
            { 1, 7, -1, 1, 4 }, // p1 = low level (high)
            { 1, 0, 7, 2, 0 }, // p2 = mid freq
            { 4, 0, 4, 3, 0 }, // p3 = mid freq mod source
            { 2, 0, 4, 4, 0 }, // p4 = mid freq mod amount (low)
            { 3, 7, -1, 4, 4 }, // p4 = mid freq mod amount (high)
            { 5, 0, -5, 5, 0 }, // p5 = mid level
            { 3, 0, 7, 6, 0 }, // p6 = mid width
            { 7, 0, 6, 7, 0 }, // p7 = high freq
            { 4, 4, 4, 8, 0 }, // p8 = high level (low)
            { 5, 5, -1, 8, 4 }, // p8 = high level (high)
            },

// CHORUS / DELAY
            {
            { 5, 4, 4, 0, 0 }, // p0 = chorus/flanger delay time (low)
            { 6, 5, 2, 0, 4 }, // p0 = chorus/flanger delay time (high)
            { 0, 0, 8, 1, 0 }, // p1 = LFO rate
            { 1, 0, 7, 2, 0 }, // p2 = LFO depth
            { 2, 0, -8, 3, 0 }, // p3 = chorus/flanger feedback
            { 5, 0, 4, 4, 0 }, // p4 = echo dry/wet mix
            { 7, 5, 3, 4, 4 }, // p4 = echo dry/wet mix (high nibble)
            { 3, 0, 8, 5, 0 }, // p5 = echo delay time (low)
            { 1, 7, 1, 5, 8 }, // p5 = echo delay time (high)
            { 6, 7, 1, 7, 0 }, // p7 = footswitch sample enable
            { 4, 0, -8, 8, 0 }, // p8 = echo feedback
            { 6, 0, -5, 9, 0 }, // p9 = low EQ
            { 7, 0, -5, 10, 0 }, // p10 = high EQ
            },

// FLANGER / DELAY
            {
            { 5, 4, 4, 0, 0 }, // p0 = chorus/flanger delay time (low)
            { 6, 5, 2, 0, 4 }, // p0 = chorus/flanger delay time (high)
            { 0, 0, 8, 1, 0 }, // p1 = LFO rate
            { 1, 0, 7, 2, 0 }, // p2 = LFO depth
            { 2, 0, -8, 3, 0 }, // p3 = chorus/flanger feedback
            { 5, 0, 4, 4, 0 }, // p4 = echo dry/wet mix
            { 7, 5, 3, 4, 4 }, // p4 = echo dry/wet mix (high nibble)
            { 3, 0, 8, 5, 0 }, // p5 = echo delay time (low)
            { 1, 7, 1, 5, 8 }, // p5 = echo delay time (high)
            { 6, 7, 1, 7, 0 }, // p7 = footswitch sample enable
            { 4, 0, -8, 8, 0 }, // p8 = echo feedback
            { 6, 0, -5, 9, 0 }, // p9 = low EQ
            { 7, 0, -5, 10, 0 }, // p10 = high EQ
            },

// DELAY / REVERB (HALL)
            {
            { 0, 0, 4, 0, 0 }, // p0 = dly dry/wet mix
            { 1, 0, 8, 1, 0 }, // p1 = dly delay time (low)
            { 2, 0, 2, 1, 8 }, // p1 = dly delay time (high)
            { 3, 0, -8, 2, 0 }, // p2 = dly feedback
            { 4, 0, 7, 3, 0 }, // p3 = reverb dry/wet mix
            { 5, 0, 8, 4, 0 }, // p4 = reverb pre-delay (low)
            { 2, 2, 2, 4, 0 }, // p4 = reverb pre-delay (high)
            { 6, 0, 7, 5, 0 }, // p5 = reverb decay time
            { 7, 0, 7, 6, 0 }, // p6 = reverb HF damping
            },

// DELAY / REVERB (ROOM)
            {
            { 0, 0, 4, 0, 0 }, // p0 = dly dry/wet mix
            { 1, 0, 8, 1, 0 }, // p1 = dly delay time (low)
            { 2, 0, 2, 1, 8 }, // p1 = dly delay time (high)
            { 3, 0, -8, 2, 0 }, // p2 = dly feedback
            { 4, 0, 7, 3, 0 }, // p3 = reverb dry/wet mix
            { 5, 0, 8, 4, 0 }, // p4 = reverb pre-delay (low)
            { 2, 2, 2, 4, 0 }, // p4 = reverb pre-delay (high)
            { 6, 0, 7, 5, 0 }, // p5 = reverb decay time
            { 7, 0, 7, 6, 0 }, // p6 = reverb HF damping
            },

// DELAY/CHORUS
            {
            { 0, 0, 4, 0, 0 }, // p0 = dly dry/wet mix
            { 1, 0, 8, 1, 0 }, // p1 = dly delay time (low)
            { 2, 0, 2, 1, 8 }, // p1 = dly delay time (high)
            { 3, 0, -8, 2, 0 }, // p2 = dly feedback
            { 4, 0, 8, 3, 0 }, // p3 = flgr/chr delay time
            { 5, 0, 8, 4, 0 }, // p4 = flgr/chr LFO rate
            { 6, 0, 7, 5, 0 }, // p5 = flgr/chr LFO depth
            { 7, 0, -8, 6, 0 }, // p6 = flgr/chr feedback
            },

// DELAY/FLANGER
            {
            { 0, 0, 4, 0, 0 }, // p0 = dly dry/wet mix
            { 1, 0, 8, 1, 0 }, // p1 = dly delay time (low)
            { 2, 0, 2, 1, 8 }, // p1 = dly delay time (high)
            { 3, 0, -8, 2, 0 }, // p2 = dly feedback
            { 4, 0, 8, 3, 0 }, // p3 = flgr/chr delay time
            { 5, 0, 8, 4, 0 }, // p4 = flgr/chr LFO rate
            { 6, 0, 7, 5, 0 }, // p5 = flgr/chr LFO depth
            { 7, 0, -8, 6, 0 }, // p6 = flgr/chr feedback
            },

// DELAY/DISTORTION AND DELAY/OVERDRIVE
            {
            { 0, 0, 4, 0, 0 }, // p0 = dly dry/wet mix
            { 1, 0, 8, 1, 0 }, // p1 = dly delay time (low)
            { 2, 0, 2, 1, 8 }, // p1 = dly delay time (high)
            { 3, 0, -8, 2, 0 }, // p2 = dly feedback
            { 4, 0, 8, 3, 0 }, // p3 = edge (distortion drive) 
            { 5, 0, 8, 4, 0 }, // p4 = hot spot (filter frequency)
            { 6, 0, 8, 5, 0 }, // p5 = filter resonance
            { 7, 0, 8, 6, 0 }, // p6 = distortion level
            },

// DELAY/PHASER
            {
            { 0, 0, 4, 0, 0 }, // p0 = dly dry/wet mix
            { 1, 0, 8, 1, 0 }, // p1 = dly delay time (low)
            { 2, 0, 2, 1, 8 }, // p1 = dly delay time (high)
            { 3, 0, -8, 2, 0 }, // p2 = dly feedback
            { 4, 0, 7, 3, 0 }, // p3 = phaser center
            { 5, 0, 8, 4, 0 }, // p4 = phaser LFO rate
            { 6, 0, 7, 5, 0 }, // p5 = phaser LFO depth
            { 7, 0, -8, 6, 0 }, // p6 = phaser feedback
            },

// DELAY/ROTARY
            {
            { 0, 0, 4, 0, 0 }, // p0 = dly dry/wet mix
            { 1, 0, 8, 1, 0 }, // p1 = dly delay time (low)
            { 2, 0, 2, 1, 8 }, // p1 = dly delay time (high)
            { 3, 0, -8, 2, 0 }, // p2 = dly feedback
            { 6, 0, 4, 4, 0 }, // p4 = rotor acceleration
            { 6, 4, 4, 5, 0 }, // p5 = rotor speed mod source
            { 4, 0, 8, 6, 0 }, // p6 = slow rotor speed
            { 5, 0, 8, 7, 0 }, // p7 = fast rotor speed
            },

// PITCH SHIFT
            {
            { 0, 0, 8, 0, 0 }, // p0 = dry/wet mix
            { 1, 0, 8, 1, 0 }, // p1 = dry/wet mix mod source
            { 2, 0, 8, 2, 0 }, // p2 = dry/wet mix mod amount
            { 3, 0, 8, 3, 0 }, // p3 = pitch shift
            { 4, 0, 8, 4, 0 }, // p4 = delay left
            { 6, 0, 1, 4, 8 }, // p4 = delay left (high bit)
            { 5, 0, 8, 5, 0 }, // p5 = delay right
            { 7, 0, 1, 5, 8 }, // p5 = delay right (high bit)
            },

//*********************************************************************
//*                                                                                                                                                     *
//*             NOTE:   All effects after this point are expanded mode effects.         *
//*                             The first four bits of p0 must be left blank to allow           *
//*                             room for the macro number.                                                                      *
//*                                                                                                                                                     *
//*{ 0, 0, 4, x, x }, // p0 = macro number      (reserved)                      *
//*                                                                                                                                                     *
//*********************************************************************


// MODULATABLE PITCH-SHIFTER DELAY
            {
            { 0, 4, 4, 0, 0 }, // p0 = dry/wet mix
            { 1, 0, 4, 1, 0 }, // p1 = dry/wet mix mod source
            { 1, 4, 4, 2, 0 }, // p2 = dry/wet mix mod amount
            { 4, 5, -1, 2, 4 }, // p2 = dry/wet mix mod amount (sign bit)
            { 2, 0, 4, 3, 0 }, // p3 = max pitch shift
            { 4, 6, -1, 3, 4 }, // p3 = max pitch shift (sign bit)
            { 3, 0, 7, 4, 0 }, // p4 = shift scaler range
            { 2, 4, 4, 5, 0 }, // p5 = shift scaler mod source
            { 4, 0, -5, 6, 0 }, // p6 = shift scaler mod amount
            { 5, 0, 8, 7, 0 }, // p7 = delay right
            { 3, 7, 1, 7, 8 }, // p7 = delay right (high bit)
            { 6, 0, 8, 8, 0 }, // p8 = delay left
            { 4, 7, 1, 8, 8 }, // p8 = delay left (high bit)
            { 7, 0, 8, 9, 0 }, // p9 = feedback
            },

// STEREO COMPRESSOR-LIMITER/GATE
            {
            { 1, 0, 5, 0, 0 }, // p0 = control source
            { 2, 0, 7, 1, 0 }, // p1 = control sensitivity
            { 3, 0, 7, 2, 0 }, // p2 = compression ratio
            { 4, 0, 7, 3, 0 }, // p3 = compression threshold
            { 5, 0, 7, 4, 0 }, // p4 = gate threshold
            { 6, 0, 7, 5, 0 }, // p5 = output level
            },

// SMALL VOCODER
            {
            { 0, 4, 2, 0, 0 }, // p0 = modulator bus
            { 1, 0, 7, 1, 0 }, // p1 = modulator sensitivity
            { 0, 6, 2, 2, 0 }, // p2 = carrier bus
            { 2, 0, 7, 3, 0 }, // p3 = carrier sensitivity
            { 3, 0, 4, 4, 0 }, // p4 = sibilance
            { 3, 4, 4, 5, 0 }, // p5 = sibilance mod src
            { 4, 0, -5, 6, 0 }, // p6 = sibilance mod amt
            },

// STEREO VOCODER
            {
            { 3, 4, 2, 0, 0 }, // p0 = modulator bus
            { 2, 0, 7, 1, 0 }, // p1 = modulator sensitivity
            { 3, 6, 2, 2, 0 }, // p2 = carrier bus
            { 1, 0, 7, 3, 0 }, // p3 = carrier sensitivity
            { 0, 4, 4, 4, 0 }, // p4 = sibilance
            { 3, 0, 4, 5, 0 }, // p5 = sibilance mod src
            { 4, 0, 4, 6, 0 }, // p6 = sibilance mod amt
            { 2, 7, -1, 6, 4 }, // p6 = sibilance mod amt (sign)
            { 4, 4, 4, 7, 0 }, // p7 = stereo width
            { 5, 0, 8, 8, 0 }, // p8 = delay time
            { 6, 7, 1, 8, 8 }, // p8 = delay time (bit8)
            { 7, 7, 1, 8, 9 }, // p8 = delay time (bit9)
            { 6, 0, 7, 9, 0 }, // p9 = delay feedback
            { 7, 0, 7, 10, 0 }, // p10 = delay level
            }
        };



    public static final int[][] FX_PRESETS = new int[][] {
/////           **** Off ****
        { }, 
/////           **** Small Hall Reverb ****
            {
            14      ,       //p0 = dry/wet mix level
            1       ,       //p0 = dry/wet mix level (high)
            0       ,       //p1 = dry/wet mix mod src
            0       ,       //p2 = dry/wet mix mod amt
            0       ,       //p2 = dry/wet mix mod amt
            55      ,       //p3 = pre delay
            0       ,       //p3 = pre delay (high bit)
            5       ,       //p4 = ER level
            55      ,       //p5 = decay time
            40      ,       //p6 = HF damping factor
            0       ,       //p7 = reserved for future use
            0       ,       //p8 = reserved for future use
            0       ,       //p8 = reserved for future use
            -2      ,       //p11 = low EQ
            0       ,       //p12 = high EQ

            },
/////           **** Medium Hall Reverb ****
            {
            14      ,       //p0 = dry/wet mix level
            1       ,       //p0 = dry/wet mix level (high)
            0       ,       //p1 = dry/wet mix mod src
            0       ,       //p2 = dry/wet mix mod amt
            0       ,       //p2 = dry/wet mix mod amt
            65      ,       //p3 = pre delay
            0       ,       //p3 = pre delay (high bit)
            5       ,       //p4 = ER level
            65      ,       //p5 = decay time
            45      ,       //p6 = HF damping factor
            0       ,       //p7 = decay time mod src
            0       ,       //p8 = decay time mod amt
            0       ,       //p8 = decay time mod amt
            -2      ,       //p11 = low EQ
            -2      ,       //p12 = high EQ

            },
/////           **** Large Hall Reverb ****
            {
            14      ,       //p0 = dry/wet mix level
            1       ,       //p0 = dry/wet mix level (high)
            0       ,       //p1 = dry/wet mix mod src
            0       ,       //p2 = dry/wet mix mod amt
            0       ,       //p2 = dry/wet mix mod amt
            170     ,       //p3 = pre delay
            0       ,       //p3 = pre delay (high bit)
            5       ,       //p4 = ER level
            70      ,       //p5 = decay time
            40      ,       //p6 = HF damping factor
            0       ,       //p7 = decay time mod src
            0       ,       //p8 = decay time mod amt
            0       ,       //p8 = decay time mod amt
            0       ,       //p11 = low EQ
            -2      ,       //p12 = high EQ

            },
/////           **** Small Room Reverb ****
            {
            13      ,       //p0 = dry/wet mix level
            2       ,       //p0 = dry/wet mix level (high)
            0       ,       //p1 = dry/wet mix mod src
            0       ,       //p2 = dry/wet mix mod amt
            0       ,       //p2 = dry/wet mix mod amt
            22      ,       //p3 = pre delay
            0       ,       //p3 = pre delay (high bit)
            7       ,       //p4 = ER level
            12      ,       //p5 = decay time
            20      ,       //p6 = HF damping factor
            0       ,       //p7 = decay time mod src
            0       ,       //p8 = decay time mod amt
            0       ,       //p8 = decay time mod amt
            1       ,       //p11 = low EQ
            -1      ,       //p12 = high EQ

            },
/////           **** Large Room Reverb ****
            {
            3       ,       //p0 = dry/wet mix level
            2       ,       //p0 = dry/wet mix level (high)
            0       ,       //p1 = dry/wet mix mod src
            0       ,       //p2 = dry/wet mix mod amt
            0       ,       //p2 = dry/wet mix mod amt
            55      ,       //p3 = pre delay
            0       ,       //p3 = pre delay (high bit)
            8       ,       //p4 = ER level
            25      ,       //p5 = decay time
            20      ,       //p6 = HF damping factor
            0       ,       //p7 = decay time mod src
            0       ,       //p8 = decay time mod amt
            0       ,       //p8 = decay time mod amt
            1       ,       //p11 = low EQ
            -4      ,       //p12 = high EQ

            },
/////           **** Live Stage Reverb ****
            {
            3       ,       //p0 = dry/wet mix level
            2       ,       //p0 = dry/wet mix level (high)
            0       ,       //p1 = dry/wet mix mod src
            0       ,       //p2 = dry/wet mix mod amt
            0       ,       //p2 = dry/wet mix mod amt
            25      ,       //p3 = pre delay
            0       ,       //p3 = pre delay (high bit)
            8       ,       //p4 = ER level
            40      ,       //p5 = decay time
            30      ,       //p6 = HF damping factor
            0       ,       //p7 = decay time mod src
            0       ,       //p8 = decay time mod amt
            0       ,       //p8 = decay time mod amt
            -1      ,       //p11 = low EQ
            -1      ,       //p12 = high EQ

            },
/////           **** Wet Plate Reverb ****
            {
            14      ,       //p0 = dry/wet mix level
            1       ,       //p0 = dry/wet mix level (high)
            0       ,       //p1 = dry/wet mix mod src
            0       ,       //p2 = dry/wet mix mod amt
            0       ,       //p2 = dry/wet mix mod amt
            50      ,       //p3 = pre delay
            0       ,       //p3 = pre delay (high bit)
            1       ,       //p4 = ER level
            60      ,       //p5 = decay time
            30      ,       //p6 = HF damping factor
            0       ,       //p7 = decay time mod src
            0       ,       //p8 = decay time mod amt
            0       ,       //p8 = decay time mod amt
            -1      ,       //p11 = low EQ
            -1      ,       //p12 = high EQ

            },
/////           **** Dry Plate Reverb ****
            {
            4       ,       //p0 = dry/wet mix level
            1       ,       //p0 = dry/wet mix level (high)
            0       ,       //p1 = dry/wet mix mod src
            0       ,       //p2 = dry/wet mix mod amt
            0       ,       //p2 = dry/wet mix mod amt
            60      ,       //p3 = pre delay
            0       ,       //p3 = pre delay (high bit)
            10      ,       //p4 = ER level
            40      ,       //p5 = decay time
            20      ,       //p6 = HF damping factor
            0       ,       //p7 = decay time mod src
            0       ,       //p8 = decay time mod amt
            0       ,       //p8 = decay time mod amt
            2       ,       //p11 = low EQ
            6       ,       //p12 = high EQ

            },
/////           **** Spring Reverb ****
            {
            4       ,       //p0 = dry/wet mix level
            1       ,       //p0 = dry/wet mix level (high)
            0       ,       //p1 = dry/wet mix mod src
            0       ,       //p2 = dry/wet mix mod amt
            0       ,       //p2 = dry/wet mix mod amt
            0       ,       //p3 = pre delay
            0       ,       //p3 = pre delay (high bit)
            5       ,       //p4 = ER level
            50      ,       //p5 = decay time
            30      ,       //p6 = HF damping factor
            0       ,       //p7 = decay time mod src
            0       ,       //p8 = decay time mod amt
            0       ,       //p8 = decay time mod amt
            3       ,       //p11 = low EQ
            4       ,       //p12 = high EQ

            },
/////           **** Early Reflections - EQ 1 ****
            {
            50      ,       //p0 = dry/wet mix level
            0       ,       //p1 = dry/wet mix mod src
            0       ,       //p2 = dry/wet mix mod amt
            20      ,       //p3 = pre delay
            40      ,       //p4 = decay time
            0       ,       //p5 = low EQ
            0       ,       //p6 = high EQ

            },
/////           **** Early Reflections - EQ 2 ****
            {
            50      ,       //p0 = dry/wet mix level
            0       ,       //p1 = dry/wet mix mod src
            0       ,       //p2 = dry/wet mix mod amt
            20      ,       //p3 = pre delay
            40      ,       //p4 = decay time
            0       ,       //p5 = low EQ
            0       ,       //p6 = high EQ

            },
/////           **** Early Reflections - EQ 3 ****
            {
            50      ,       //p0 = dry/wet mix level
            0       ,       //p1 = dry/wet mix mod src
            0       ,       //p2 = dry/wet mix mod amt
            20      ,       //p3 = pre delay
            40      ,       //p4 = decay time
            0       ,       //p5 = low EQ
            0       ,       //p6 = high EQ

            },
/////           **** Forward Gated Reverb ****
            {
            50      ,       //p0 = dry/wet mix level
            10      ,       //p1 = gate hold time
            6       ,       //p2 = key source
            50      ,       //p3 = threshold
            0       ,       //p4 = pre delay
            30      ,       //p5 = decay time

            },
/////           **** Reverse Gated Reverb ****
            {
            50      ,       //p0 = dry/wet mix level
            30      ,       //p1 = gate hold time
            6       ,       //p2 = key source
            50      ,       //p3 = trigger level
            20      ,       //p4 = pre delay
            35      ,       //p5 = decay time

            },
/////           **** Stereo Delay ****
            {
            8       ,       //p0 = dry/wet mix level (low)
            2       ,       //p0 = dry/wet mix level (high)
            0       ,       //p1 = dry/wet mix mod src
            0       ,       //p2 = dry/wet mix mod amt
            0       ,       //p2 = dry/wet mix mod amt
            0       ,       //p10 = input level mod src
            0       ,       //p11 = input level mod amt
            0       ,       //p6 = delay time (low)
            1       ,       //p6 = delay time (high)
            0       ,       //p4 = delay mod src
            0       ,       //p5 = delay mod amt
            1       ,       //p7 = delay factor
            40      ,       //p13 = feedback

            },
/////           **** Ping-Pong Delay ****
            {
            8       ,       //p0 = dry/wet mix level (low)
            2       ,       //p0 = dry/wet mix level (high)
            0       ,       //p1 = dry/wet mix mod src
            0       ,       //p2 = dry/wet mix mod amt
            0       ,       //p2 = dry/wet mix mod amt
            0       ,       //p10 = input level mod src
            0       ,       //p11 = input level mod amt
            0       ,       //p6 = delay time (low)
            1       ,       //p6 = delay time (high)
            0       ,       //p4 = delay mod src
            0       ,       //p5 = delay mod amt
            0       ,       //p7 = delay factor
            40      ,       //p13 = feedback

            },
/////           **** Dual Mono Delay ****
            {
            5       ,       //p0 = dry/wet mix left
            200     ,       //p1 = delay left (low)
            0       ,       //p1 = delay left (high)
            55      ,       //p2 = feedback left
            5       ,       //p3 = dry/wet mix right
            144     ,       //p4 = delay right (low)
            1       ,       //p4 = delay right (high)
            45      ,       //p5 = feedback right

            },
/////           **** Multi-tap Delay - EQ 1 ****
            {
            2       ,       //p0 = dry/wet mix (low)
            3       ,       //p0 = dry/wet mix (high)
            0       ,       //p1 = dry/wet mix mod source
            0       ,       //p2 = dry/wet mix mod amount (low)
            0       ,       //p2 = dry/wet mix mod amount (high)
            0       ,       //p4 = input level mod source
            0       ,       //p5 = input level mod amount
            44      ,       //p6 = delay time 1 (low)
            1       ,       //p6 = delay time 1 (high)
            144     ,       //p7 = delay time 2 (low)
            1       ,       //p7 = delay time 2 (high)
            50      ,       //p8 = feedback
            0       ,       //p9 = low EQ
            0       ,       //p10 = high EQ

            },
/////           **** Multi-tap Delay - EQ 2 ****
            {
            2       ,       //p0 = dry/wet mix (low)
            3       ,       //p0 = dry/wet mix (high)
            0       ,       //p1 = dry/wet mix mod source
            0       ,       //p2 = dry/wet mix mod amount (low)
            0       ,       //p2 = dry/wet mix mod amount (high)
            0       ,       //p4 = input level mod source
            0       ,       //p5 = input level mod amount
            11      ,       //p6 = delay time 1 (low)
            1       ,       //p6 = delay time 1 (high)
            144     ,       //p7 = delay time 2 (low)
            1       ,       //p7 = delay time 2 (high)
            50      ,       //p8 = feedback
            0       ,       //p9 = low EQ
            0       ,       //p10 = high EQ

            },
/////           **** Multi-tap Delay - EQ 3 ****
            {
            2       ,       //p0 = dry/wet mix (low)
            3       ,       //p0 = dry/wet mix (high)
            0       ,       //p1 = dry/wet mix mod source
            0       ,       //p2 = dry/wet mix mod amount (low)
            0       ,       //p2 = dry/wet mix mod amount (high)
            0       ,       //p4 = input level mod source
            0       ,       //p5 = input level mod amount
            44      ,       //p6 = delay time 1 (low)
            1       ,       //p6 = delay time 1 (high)
            144     ,       //p7 = delay time 2 (low)
            1       ,       //p7 = delay time 2 (high)
            50      ,       //p8 = feedback
            0       ,       //p9 = low EQ
            0       ,       //p10 = high EQ

            },
/////           **** Stereo Chorus - EQ ****
            {
            0       ,       //p1 = footswitch enable
            11      ,       //p3 = delay time left
            0       ,       //p3 = delay time left (high bit)
            23      ,       //p4 = delay time right
            0       ,       //p4 = delay time right (high bit)
            15      ,       //p5 = LFO rate
            0       ,       //p6 = LFO rate mod src
            0       ,       //p7 = LFO rate mod amt
            0       ,       //p7 = LFO rate mod amt (sign)
            60      ,       //p8 = LFO depth
            10      ,       //p9 = LFO shape
            0       ,       //p10 = low EQ
            0       ,       //p11 = high EQ

            },
/////           **** Quadrature Chorus - EQ ****
            {
            0       ,       //p1 = footswitch enable
            11      ,       //p3 = delay time left
            0       ,       //p3 = delay time left (high bit)
            23      ,       //p4 = delay time right
            0       ,       //p4 = delay time right (high bit)
            33      ,       //p5 = LFO rate
            0       ,       //p6 = LFO rate mod src
            0       ,       //p7 = LFO rate mod amt
            0       ,       //p7 = LFO rate mod amt (sign)
            50      ,       //p8 = LFO depth
            -11     ,       //p9 = LFO shape
            0       ,       //p10 = low EQ
            0       ,       //p11 = high EQ

            },
/////           **** Crossover Chorus - EQ ****
            {
            0       ,       //p1 = footswitch enable
            11      ,       //p3 = delay time left
            0       ,       //p3 = delay time left (high bit)
            23      ,       //p4 = delay time right
            0       ,       //p4 = delay time right (high bit)
            33      ,       //p5 = LFO rate
            0       ,       //p6 = LFO rate mod src
            0       ,       //p7 = LFO rate mod amt
            0       ,       //p7 = LFO rate mod amt (sign)
            50      ,       //p8 = LFO depth
            -11     ,       //p9 = LFO shape
            0       ,       //p10 = low EQ
            0       ,       //p11 = high EQ

            },
/////           **** Harmonic Chorus ****
            {
            0       ,       //p1 = footswitch enable
            22      ,       //p2 = delay left
            0       ,       //p2 = delay left (high bit)
            46      ,       //p3 = delay right
            0       ,       //p3 = delay right (high bit)
            35      ,       //p4 = LFO rate
            2       ,       //p5 = LFO rate mod src
            7       ,       //p6 = LFO rate mod amt
            100     ,       //p7 = LFO depth
            2       ,       //p8 = LFO depth mod src
            -7      ,       //p9 = LFO depth mod amt
            1       ,       //p10 = split point

            },
/////           **** Stereo Flanger - EQ 1 ****
            {
            -8      ,       //p0 = output mix
            0       ,       //p1 = footswitch enable
            5       ,       //p3 = top delay
            100     ,       //p4 = range
            20      ,       //p5 = ramp speed
            12      ,       //p6 = ramp speed mod src
            2       ,       //p7 = ramp speed mod amt
            -85     ,       //p8 = resonance
            0       ,       //p9 = low EQ
            0       ,       //p10 = high EQ
            0       ,       //p9 = low EQ (sign)
            0       ,       //p10 = high EQ (sign)

            },
/////           **** Stereo Flanger - EQ 2 ****
            {
            5       ,       //p0 = output mix
            0       ,       //p1 = footswitch enable
            10      ,       //p3 = top delay
            100     ,       //p4 = range
            20      ,       //p5 = ramp speed
            0       ,       //p6 = ramp speed mod src
            0       ,       //p7 = ramp speed mod amt
            -85     ,       //p8 = resonance                                                /// NOTE: this is an error in the documentation
            0       ,       //p9 = low EQ
            0       ,       //p10 = high EQ
            0       ,       //p9 = low EQ (sign)
            0       ,       //p10 = high EQ (sign)

            },
/////           **** Crossover Flanger - EQ ****
            {
            -5      ,       //p0 = output mix
            0       ,       //p1 = footswitch enable
            50      ,       //p3 = top delay
            100     ,       //p4 = range
            0       ,       //p5 = ramp speed
            5       ,       //p6 = ramp speed mod src
            -15     ,       //p7 = ramp speed mod amt
            -85     ,       //p8 = resonance                                                /// NOTE: this is an error in the documentation
            3       ,       //p9 = low EQ
            3       ,       //p10 = high EQ
            0       ,       //p9 = low EQ (sign)
            0       ,       //p10 = high EQ (sign)

            },
/////           **** Enhancer - Exciter - EQ ****
            {
            11      ,       //p0 = dry/wet mix
            80      ,       //p1 = harmonic density
            1       ,       //p2 = hot spot
            50      ,       //p3 = stereo width
            25      ,       //p4 = delay
            1       ,       //p5 = low EQ
            1       ,       //p6 = high EQ
            },
/////           **** Distortion-Filter-EQ ****
            {
            10      ,       //p0 = dry/wet mix
            0       ,       //p1 = footswitch enable
            111     ,       //p2 = edge (drive level)
            5       ,       //p3 = hot spot (filter frequency)
            5       ,       //p4 = hot spot mod source
            5       ,       //p5 = hot spot mod amount
            80      ,       //p6 = resonance
            2       ,       //p7 = low EQ
            -12     ,       //p8 = high EQ
            10      ,       //p9 = level (output level)
            0       ,       //p10 = level mod source
            0       ,       //p10 = level mod source (high bit)
            0       ,       //p11 = level mod amount
            0       ,       //p11 = level mod amount (high bits)

            },
/////           **** Overdrive-Filter-EQ ****
            {
            10      ,       //p0 = dry/wet mix
            0       ,       //p1 = footswitch enable
            50      ,       //p2 = edge (drive level)
            45      ,       //p3 = hot spot (filter frequency)
            0       ,       //p4 = hot spot mod source
            0       ,       //p5 = hot spot mod amount
            0       ,       //p6 = resonance
            3       ,       //p7 = low EQ
            -3      ,       //p8 = high EQ
            20      ,       //p9 = level (output level)
            0       ,       //p10 = level mod source
            0       ,       //p10 = level mod source (high bit)
            0       ,       //p11 = level mod amount
            0       ,       //p11 = level mod amount (high bits)

            },
/////           **** Stereo Phaser 1 ****
            {
            -5      ,       //p0 = dry/wet mix
            0       ,       //p1 = footswitch
            40      ,       //p2 = center
            6       ,       //p3 = LFO rate
            12      ,       //p4 = LFO rate mod source
            5       ,       //p5 = LFO rate mod amount
            30      ,       //p6 = LFO depth
            0       ,       //p7 = LFO depth mod source
            0       ,       //p8 = LFO depth mod amount
            70      ,       //p9 = feedback

            },
/////           **** Stereo Phaser 2 ****
            {
            5       ,       //p0 = dry/wet mix
            0       ,       //p1 = footswitch
            27      ,       //p2 = center
            20      ,       //p3 = LFO rate
            12      ,       //p4 = LFO rate mod source
            2       ,       //p5 = LFO rate mod amount
            97      ,       //p6 = LFO depth
            0       ,       //p7 = LFO depth mod source
            0       ,       //p8 = LFO depth mod amount
            -30     ,       //p9 = feedback

            },
/////           **** Rotary Speaker ****
            {
            10      ,       //p0 = dry/wet mix
            0       ,       //p1 = footswitch enable
            9       ,       //p2 = vibrato depth
            4       ,       //p4 = rotor acceleration
            12      ,       //p5 = rotor speed mod source
            25      ,       //p6 = rotor slow speed
            70      ,       //p7 = rotor fast speed
            20      ,       //p8 = reserved for future use
            60      ,       //p9 = reserved for future use

            },
/////           **** Stereo mod-pan - EQ ****
            {
            11      ,       //p0 = dry/wet mix
            0       ,       //p1 = dry/wet mix mod source
            0       ,       //p2 = dry/wet mix mod amount
            20      ,       //p3 = LFO rate
            10      ,       //p4 = LFO depth 
            0       ,       //p5 = LFO depth mod source
            0       ,       //p6 = LFO depth mod amount
            0       ,       //p7 = low EQ
            0       ,       //p8 = high EQ

            },
/////           **** Quadrature mod-pan - EQ ****
            {
            11      ,       //p0 = dry/wet mix
            0       ,       //p1 = dry/wet mix mod source
            0       ,       //p2 = dry/wet mix mod amount
            20      ,       //p3 = LFO rate
            10      ,       //p4 = LFO depth 
            0       ,       //p5 = LFO depth mod source
            0       ,       //p6 = LFO depth mod amount
            0       ,       //p7 = low EQ
            0       ,       //p8 = high EQ

            },
/////           **** Stereo Parametric EQ ****
            {
            12      ,       //p0 = low freq
            12      ,       //p1 = low gain (low)
            0       ,       //p1 = low gain (high)
            8       ,       //p2 = mid freq
            12      ,       //p3 = mid freq mod source
            6       ,       //p4 = mid freq mod amount (low)
            0       ,       //p4 = mid freq mod amount (high)
            12      ,       //p5 = mid gain
            50      ,       //p6 = mid width
            20      ,       //p7 = high freq
            12      ,       //p8 = high gain (low)
            0       ,       //p8 = high gain (high)

            },
/////           **** Chorus-Stereo Delay ****
            {
            27      ,       //p0 = chorus/flanger delay time (low)
            0       ,       //p0 = chorus/flanger delay time (high)
            30      ,       //p1 = LFO rate
            50      ,       //p2 = LFO depth
            10      ,       //p3 = chorus/flanger feedback
            14      ,       //p4 = echo dry/wet mix
            1       ,       //p4 = echo dry/wet mix (high nibble)
            110     ,       //p5 = echo delay time (low)
            0       ,       //p5 = echo delay time (high)
            0       ,       //p7 = footswitch sample enable
            -10     ,       //p8 = echo feedback
            2       ,       //p9 = low EQ
            2       ,       //p10 = high EQ

            },
/////           **** Flanger-Stereo Delay ****
            {
            0       ,       //p0 = chorus/flanger delay time (low)
            0       ,       //p0 = chorus/flanger delay time (high)
            10      ,       //p1 = LFO rate
            50      ,       //p2 = LFO depth
            -90     ,       //p3 = chorus/flanger feedback
            2       ,       //p4 = echo dry/wet mix
            3       ,       //p4 = echo dry/wet mix (high nibble)
            144     ,       //p5 = echo delay time (low)
            1       ,       //p5 = echo delay time (high)
            0       ,       //p7 = footswitch sample enable
            60      ,       //p8 = echo feedback
            0       ,       //p9 = low EQ
            0       ,       //p10 = high EQ

            },
/////           **** Delay/Hall ****
            {
            5       ,       //p0 = dly dry/wet mix
            250     ,       //p1 = dly delay time (low)
            0       ,       //p1 = dly delay time (high)
            40      ,       //p2 = dly feedback
            20      ,       //p3 = reverb dry/wet mix
            150     ,       //p4 = reverb pre-delay (low)
            0       ,       //p4 = reverb pre-deley (high)
            99      ,       //p5 = reverb decay time
            30      ,       //p6 = reverb HF damping

            },
/////           **** Delay/Room ****
            {
            5       ,       //p0 = dly dry/wet mix
            250     ,       //p1 = dly delay time (low)
            0       ,       //p1 = dly delay time (high)
            40      ,       //p2 = dly feedback
            40      ,       //p3 = reverb dry/wet mix
            0       ,       //p4 = reverb pre-delay (low)
            0       ,       //p4 = reverb pre-deley (high)
            30      ,       //p5 = reverb decay time
            30      ,       //p6 = reverb HF damping

            },
/////           **** Delay/Chorus ****
            {
            5       ,       //p0 = dly dry/wet mix
            250     ,       //p1 = dly delay time (low)
            0       ,       //p1 = dly delay time (high)
            40      ,       //p2 = dly feedback
            50      ,       //p3 = chr delay time
            30      ,       //p4 = chr LFO rate
            50      ,       //p5 = chr LFO depth
            0       ,       //p6 = chr feedback

            },
/////           **** Delay/Flanger ****
            {
            5       ,       //p0 = dly dry/wet mix
            250     ,       //p1 = dly delay time (low)
            0       ,       //p1 = dly delay time (high)
            40      ,       //p2 = dly feedback
            0       ,       //p3 = flgr delay time
            9       ,       //p4 = flgr LFO rate
            50      ,       //p5 = flgr LFO depth
            85      ,       //p6 = flgr feedback

            },
/////           **** Delay/Distortion-filter ****
            {
            5       ,       //p0 = dly dry/wet mix
            250     ,       //p1 = dly delay time (low)
            0       ,       //p1 = dly delay time (high)
            40      ,       //p2 = dly feedback
            111     ,       //p3 = edge (distortion drive)
            50      ,       //p4 = hot spot (filter frequency)
            75      ,       //p5 = filter resonance
            5       ,       //p6 = distortion level

            },
/////           **** Delay/Overdrive-filter ****
            {
            5       ,       //p0 = dly dry/wet mix
            250     ,       //p1 = dly delay time (low)
            0       ,       //p1 = dly delay time (high)
            40      ,       //p2 = dly feedback
            50      ,       //p3 = distortion drive (edge)
            90      ,       //p4 = filter frequency (hot spot)
            0       ,       //p5 = filter resonance
            15      ,       //p6 = distortion level

            },
/////           **** Delay/Phaser ****
            {
            5       ,       //p0 = dly dry/wet mix
            250     ,       //p1 = dly delay time (low)
            0       ,       //p1 = dly delay time (high)
            40      ,       //p2 = dly feedback
            35      ,       //p3 = phaser center
            5       ,       //p4 = phaser LFO rate
            30      ,       //p5 = phaser LFO depth
            70      ,       //p6 = phaser feedback

            },
/////           **** Delay/Rotary Speaker ****
            {
            5       ,       //p0 = dly dry/wet mix
            250     ,       //p1 = dly delay time (low)
            0       ,       //p1 = dly delay time (high)
            40      ,       //p2 = dly feedback
            4       ,       //p4 = rotor acceleration
            12      ,       //p5 = rotor speed mod source
            25      ,       //p6 = slow rotor speed
            70      ,       //p7 = fast rotor speed

            },
/////           **** Stereo Pitch Shifter ****
            {
            6       ,       //p0 = dry/wet mix
            0       ,       //p1 = dry/wet mix mod source
            0       ,       //p2 = dry/wet mix mod amount
            12      ,       //p3 = pitch shift
            30      ,       //p4 = delay left
            0       ,       //p4 = delay left (high bit)
            50      ,       //p5 = delay right
            0       ,       //p5 = delay right (high bit)
            },
/////           **** Mod Pitch Shift - Delay ****
            {
            6       ,       //p0 = dry/wet mix
            0       ,       //p1 = dry/wet mix mod source
            0       ,       //p2 = dry/wet mix mod amount
            0       ,       //p2 = dry/wet mix mod amount (sign bit)
            7       ,       //p3 = max pitch shift
            0       ,       //p3 = max pitch shift (sign bit)
            100     ,       //p4 = shift scaler range
            0       ,       //p5 = shift scaler mod source
            0       ,       //p6 = shift scaler mod amount
            30      ,       //p7 = delay right
            0       ,       //p7 = delay right (high bit)
            50      ,       //p8 = delay left
            0       ,       //p8 = delay left (high bit)
            20      ,       //p9 = feedback

            },
/////           **** Stereo Comp-Lim/Gate ****
            {
            0       ,       //p0 = control source
            100     ,       //p1 = control sensitivity
            95      ,       //p2 = compression ratio
            1       ,       //p3 = compression threshold
            1       ,       //p4 = gate threshold
            20      ,       //p5 = output level

            },
/////           **** Small Vocoder 1 ****
            {
            0       ,       //p0 = modulator bus
            50      ,       //p1 = modulator sensitivity
            1       ,       //p2 = carrier bus
            50      ,       //p3 = carrier sensitivity
            0       ,       //p4 = sibilance
            6       ,       //p5 = sibilance mod src
            8       ,       //p6 = sibilance mod amt

            },
/////           **** Small Vocoder 2 ****
            {
            0       ,       //p0 = modulator bus
            50      ,       //p1 = modulator sensitivity
            1       ,       //p2 = carrier bus
            50      ,       //p3 = carrier sensitivity
            0       ,       //p4 = sibilance
            6       ,       //p5 = sibilance mod src
            8       ,       //p6 = sibilance mod amt

            },
/////           **** Small Vocoder 3 ****
            {
            0       ,       //p0 = modulator bus
            50      ,       //p1 = modulator sensitivity
            1       ,       //p2 = carrier bus
            50      ,       //p3 = carrier sensitivity
            0       ,       //p4 = sibilance
            6       ,       //p5 = sibilance mod src
            8       ,       //p6 = sibilance mod amt

            },
/////           **** Small Vocoder 4 ****
            {
            0       ,       //p0 = modulator bus
            50      ,       //p1 = modulator sensitivity
            1       ,       //p2 = carrier bus
            50      ,       //p3 = carrier sensitivity
            0       ,       //p4 = sibilance
            6       ,       //p5 = sibilance mod src
            8       ,       //p6 = sibilance mod amt

            },
/////           **** Stereo Vocoder-Dly 1 ****
            {
            0       ,       //p0 = modulator bus
            50      ,       //p1 = modulator sensitivity
            1       ,       //p2 = carrier bus
            50      ,       //p3 = carrier sensitivity
            0       ,       //p4 = sibilance
            6       ,       //p5 = sibilance mod src
            8       ,       //p6 = sibilance mod amt
            0       ,       //p6 = sibilance mod amt (sign)
            10      ,       //p7 = stereo width
            50      ,       //p8 = delay time
            0       ,       //p8 = delay time (bit8)
            0       ,       //p8 = delay time (bit9)
            30      ,       //p9 = delay feedback
            70      ,       //p10 = delay level
            },
/////           **** Stereo Vocoder-Dly 2 ****
            {
            0       ,       //p0 = modulator bus
            50      ,       //p1 = modulator sensitivity
            1       ,       //p2 = carrier bus
            50      ,       //p3 = carrier sensitivity
            0       ,       //p4 = sibilance
            6       ,       //p5 = sibilance mod src
            8       ,       //p6 = sibilance mod amt
            0       ,       //p6 = sibilance mod amt (sign)
            10      ,       //p7 = stereo width
            50      ,       //p8 = delay time
            0       ,       //p8 = delay time (bit8)
            0       ,       //p8 = delay time (bit9)
            30      ,       //p9 = delay feedback
            70      ,       //p10 = delay level
            },
        };



    public KorgWavestationPerformance()
        {
        JComponent soundPanel = new SynthPanel(this);
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        hbox.addLast(addFXGlobal(Style.COLOR_A()));
        vbox.add(hbox);
        vbox.add(addFX(1, Style.COLOR_B()));
        vbox.add(addFX(2, Style.COLOR_B()));
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("FX", soundPanel);
        
        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        vbox.add(addPart(1, Style.COLOR_A()));
        vbox.add(addPart(2, Style.COLOR_B()));
        vbox.add(addPart(3, Style.COLOR_A()));
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Parts 1-3", soundPanel);

        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        vbox.add(addPart(4, Style.COLOR_B()));
        vbox.add(addPart(5, Style.COLOR_A()));
        vbox.add(addPart(6, Style.COLOR_B()));
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Parts 4-6", soundPanel);

        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        vbox.add(addPart(7, Style.COLOR_A()));
        vbox.add(addPart(8, Style.COLOR_B()));
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Parts 7-8", soundPanel);

        model.set("name", "Init");
        
        model.set("number", 0);
        model.set("bank", 0);

        loadDefaults();
        }
                
    
    public String getDefaultResourceFileName() { return "KorgWavestationPerformance.init"; }
    public String getHTMLResourceFileName() { return "KorgWavestationPerformance.html"; }
                
                
    public JFrame sprout()     
        {
        JFrame frame = super.sprout();
        receiveCurrent.setEnabled(false);  // we can't request the "current" performance
        return frame;
        }

              
    /** Add the global patch category (name, id, number, etc.) */
    public JComponent addNameGlobal(Color color)
        {
        Category globalCategory = new Category(this, getSynthName(), color);
        globalCategory.makeUnresettable();
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
                
        VBox vbox = new VBox();
        HBox hbox2 = new HBox();
        comp = new PatchDisplay(this, 9);
        hbox2.add(comp);
        vbox.add(hbox2);
        
        comp = new StringComponent("Performance Name", this, "name", 15, "Name must be up to 15 ASCII characters.")
            {
            public String replace(String val)
                {
                return revisePatchName(val);
                }
                                
            public void update(String key, Model model)
                {
                super.update(key, model);
                updateTitle();
                }
            };
        vbox.addBottom(comp);  // doesn't work right :-(
        hbox.add(vbox);
        
        /*
          LabelledDial delay = new LabelledDial("Delay", this, "hillclimbdelay", Style.COLOR_A(), 0, 5000);                
          hbox.add(delay);
        */
                
        hbox.add(Strut.makeHorizontalStrut(130));

        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }
        
    public String revisePatchName(String name)
        {
        name = super.revisePatchName(name);
        if (name == null) name = "";
        char[] chars = name.toCharArray();
        for(int i = 0; i < chars.length; i++)
            {
            if (chars[i] < 32 || chars[i] > 127)
                chars[i] = ' ';
            }
        return new String(chars);
        }


    VBox[][] fx = new VBox[2][31];
        
    public JComponent buildFXDryWetMix(String param, Color color) { return buildFXDryWetMix(param, "", color); }

    public JComponent buildFXDryWetMix(String param, String label, Color color)
        {
        JComponent comp = new LabelledDial("Dry/Wet", this, param, color, 0, 100)
            {
            public String map(int val)
                {
                if (val == 0)
                    return "Dry";
                else if (val == 100)
                    return "Wet";
                else return "" + (100 - val) + "/" + val;
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Mix" + label);
        return comp;
        }

    public JComponent buildFXDryWetMixSmall(String param, Color color) { return buildFXDryWetMixSmall(param, "", color); }

    public JComponent buildFXDryWetMixSmall(String param, String label, Color color)
        {
        JComponent comp = new LabelledDial("Dry/Wet", this, param, color, 1, 11)
            {
            public String map(int val)
                {
                if (val == 1)
                    return "Dry";
                else if (val == 11)
                    return "Wet";
                else return "" + (11 - val) + "/" + (val - 1);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Mix" + label);
        return comp;
        }



    // ugh
    public VBox[] buildFX(int val, Color color)
        {
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        VBox main = new VBox();

        // build
        VBox[] fx = new VBox[31];
                        
        // 0 NO EFFECT
        hbox = new HBox();

        fx[0] = main;
                        
        // 1 REVERB-EQ
        hbox = new HBox();
        main = new VBox();

        fx[1] = main;
                        
        JComponent comp = buildFXDryWetMix("fx" + val + "class1param0", color);
        hbox.add(comp);
                        
        String[] params = FX_SOURCES;
        comp = new Chooser("Mix Mod Source", this, "fx" + val + "class1param1", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Mix Mod Amount", this, "fx" + val + "class1param2", color, -15, 15);
        hbox.add(comp);
        main.add(hbox);
        hbox = new HBox();
                        
        comp = new LabelledDial("Decay Time", this, "fx" + val + "class1param3", color, 0, 99);
        hbox.add(comp);

        comp = new LabelledDial("Pre Delay", this, "fx" + val + "class1param4", color, 0, 400);
        hbox.add(comp);

        comp = new LabelledDial("Early Reflect", this, "fx" + val + "class1param5", color, 0, 10);
        hbox.add(comp);

        comp = new LabelledDial("HiFreq Damping", this, "fx" + val + "class1param6", color, 0, 99);
        hbox.add(comp);

        comp = new LabelledDial("EQ Low", this, "fx" + val + "class1param7", color, -12, 12);
        hbox.add(comp);

        comp = new LabelledDial("EQ High", this, "fx" + val + "class1param8", color, -12, 12);
        hbox.add(comp);
        main.add(hbox);
        hbox = new HBox();


                        
        // 2 REVERB-EQ (LARGE HALL ONLY)
        hbox = new HBox();
        main = new VBox();

        fx[2] = main;
                        
        comp = buildFXDryWetMix("fx" + val + "class2param0", color);
        hbox.add(comp);
                        
        params = FX_SOURCES;
        comp = new Chooser("Mix Mod Source", this, "fx" + val + "class2param1", params);
        vbox = new VBox();
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Mix Mod Amount", this, "fx" + val + "class2param2", color, -15, 15);
        hbox.add(comp);
                        
        main.add(hbox);
        hbox = new HBox();
        comp = new LabelledDial("Decay Time", this, "fx" + val + "class2param3", color, 0, 99);
        hbox.add(comp);

        comp = new LabelledDial("Pre Delay", this, "fx" + val + "class2param4", color, 0, 190);
        hbox.add(comp);
                        
        comp = new LabelledDial("Early Reflect", this, "fx" + val + "class2param5", color, 0, 10);
        hbox.add(comp);

        comp = new LabelledDial("HiFreq Damping", this, "fx" + val + "class2param6", color, 0, 99);
        hbox.add(comp);

        comp = new LabelledDial("EQ Low", this, "fx" + val + "class2param7", color, -12, 12);
        hbox.add(comp);

        comp = new LabelledDial("EQ High", this, "fx" + val + "class2param8", color, -12, 12);
        hbox.add(comp);
        main.add(hbox);
        hbox = new HBox();



        // 3 EARLY REFLECTIONS
        hbox = new HBox();
        main = new VBox();

        fx[3] = main;
                        
        comp = buildFXDryWetMix("fx" + val + "class3param0", color);
        hbox.add(comp);
                        
        params = FX_SOURCES;
        comp = new Chooser("Mix Mod Source", this, "fx" + val + "class3param1", params);
        vbox = new VBox();
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Mix Mod Amount", this, "fx" + val + "class3param2", color, -15, 15);
        hbox.add(comp);
                        
        comp = new LabelledDial("Decay Time", this, "fx" + val + "class3param3", color, 1, 80);
        hbox.add(comp);

        comp = new LabelledDial("Pre Delay", this, "fx" + val + "class3param4", color, 0, 200);
        hbox.add(comp);

        comp = new LabelledDial("EQ Low", this, "fx" + val + "class3param5", color, -12, 12);
        hbox.add(comp);

        comp = new LabelledDial("EQ High", this, "fx" + val + "class3param6", color, -12, 12);
        hbox.add(comp);
        main.add(hbox);
        hbox = new HBox();


        // 4 GATED REVERB
        hbox = new HBox();
        main = new VBox();

        fx[4] = main;
                        
        comp = buildFXDryWetMix("fx" + val + "class4param0", color);
        hbox.add(comp);
                        
        comp = new LabelledDial("Decay Time", this, "fx" + val + "class4param1", color, 1, 80);
        hbox.add(comp);

        comp = new LabelledDial("Pre Delay", this, "fx" + val + "class4param2", color, 0, 200);
        hbox.add(comp);
                        
        comp = new LabelledDial("Gate Hold Time", this, "fx" + val + "class4param3", color, 1, 80);
        hbox.add(comp);

        params = FX_SOURCES;
        comp = new Chooser("Gate Key Mod Source", this, "fx" + val + "class4param4", params);
        vbox = new VBox();
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Gate Threshold", this, "fx" + val + "class4param5", color, 0, 100);
        hbox.add(comp);
        main.add(hbox);
        hbox = new HBox();




        // 5 STEREO DELAY AND PING PONG DELAY
        hbox = new HBox();
        main = new VBox();

        fx[5] = main;
                        
        comp = buildFXDryWetMix("fx" + val + "class5param0", color);
        hbox.add(comp);

        params = FX_SOURCES;
        comp = new Chooser("Mix Mod Source", this, "fx" + val + "class5param1", params);
        vbox = new VBox();
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Mix Mod", this, "fx" + val + "class5param2", color, -15, 15);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);
                        
        comp = new LabelledDial("Delay Time", this, "fx" + val + "class5param3", color, 0, 500);
        hbox.add(comp);
                        
        params = FX_SOURCES;
        comp = new Chooser("Delay Mod Source", this, "fx" + val + "class5param4", params);
        vbox = new VBox();
        vbox.add(comp);
        hbox.add(vbox);

        main.add(hbox);
        hbox = new HBox();

        comp = new LabelledDial("Delay Mod Amount", this, "fx" + val + "class5param5", color, -15, 15);
        hbox.add(comp);
                        
        params = FACTORS;
        comp = new Chooser("Delay Factor", this, "fx" + val + "class5param6", params);
        vbox = new VBox();
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Feedback", this, "fx" + val + "class5param7", color, -100, 100);
        hbox.add(comp);
        main.add(hbox);
                        
        params = FX_SOURCES;
        comp = new Chooser("Input Level Mod Source", this, "fx" + val + "class5param8", params);
        vbox = new VBox();
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Input Level", this, "fx" + val + "class5param9", color, -15, 15);
        ((LabelledDial)comp).addAdditionalLabel("Mod Amount");
        hbox.add(comp);
                                                

        hbox = new HBox();




        // 6 DUAL MONO DELAY
        hbox = new HBox();
        main = new VBox();

        fx[6] = main;
                        
        comp = buildFXDryWetMix("fx" + val + "class6param0", " A", color);
        hbox.add(comp);
        comp = new LabelledDial("Delay A", this, "fx" + val + "class6param1", color, 0, 500);
        hbox.add(comp);
                        
        comp = new LabelledDial("Feedback A", this, "fx" + val + "class6param2", color, -100, 100);
        hbox.add(comp);

        comp = buildFXDryWetMix("fx" + val + "class6param3", " B", color);
        hbox.add(comp);
        comp = new LabelledDial("Delay B", this, "fx" + val + "class6param4", color, 0, 500);
        hbox.add(comp);
                        
        comp = new LabelledDial("Feedback B", this, "fx" + val + "class6param5", color, -100, 100);
        hbox.add(comp);
        main.add(hbox);
        hbox = new HBox();




        // 7 MULTI-TAP DELAY (EQ1, EQ2, EQ3)
        hbox = new HBox();
        main = new VBox();

        fx[7] = main;
                        
        comp = buildFXDryWetMix("fx" + val + "class7param0", color);
        hbox.add(comp);

        params = FX_SOURCES;
        comp = new Chooser("Mix Mod Source", this, "fx" + val + "class7param1", params);
        vbox = new VBox();
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Mix Mod", this, "fx" + val + "class7param2", color, -15, 15);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);
                
        comp = new LabelledDial("Delay 1", this, "fx" + val + "class7param3", color, 0, 500);
        hbox.add(comp);
                        
        comp = new LabelledDial("Delay 2", this, "fx" + val + "class7param4", color, 0, 500);
        hbox.add(comp);
                        
        comp = new LabelledDial("Feedback", this, "fx" + val + "class7param5", color, -100, 100);
        hbox.add(comp);

        main.add(hbox);
        hbox = new HBox();
        params = FX_SOURCES;

        comp = new Chooser("Input Level Mod Source", this, "fx" + val + "class7param6", params);
        vbox = new VBox();
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Input Level", this, "fx" + val + "class7param7", color, -15, 15);
        ((LabelledDial)comp).addAdditionalLabel("Mod Amount");
        hbox.add(comp);
                        
        comp = new LabelledDial("EQ Low", this, "fx" + val + "class7param8", color, -12, 12);
        hbox.add(comp);

        comp = new LabelledDial("EQ High", this, "fx" + val + "class7param9", color, -12, 12);
        hbox.add(comp);
        main.add(hbox);
        hbox = new HBox();




        // 8 STEREO CHORUS AND CROSSOVER CHORUS
        hbox = new HBox();
        main = new VBox();

        fx[8] = main;
                        
        comp = new LabelledDial("LFO Depth", this, "fx" + val + "class8param0", color, 0, 100);
        hbox.add(comp);
                        
        comp = new LabelledDial("LFO Rate", this, "fx" + val + "class8param1", color, 1, 100)   
            {
            public String map(int val)
                {
                return CHORUS_LFO_RATE[val - 1];
                }
            };
        hbox.add(comp);

        params = FX_SOURCES;
        comp = new Chooser("LFO Rate Mod Source", this, "fx" + val + "class8param2", params);
        vbox = new VBox();
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("LFO Rate Mod Amount", this, "fx" + val + "class8param3", color, -15, 15);
        hbox.add(comp);
                
        comp = new LabelledDial("LFO Shape", this, "fx" + val + "class8param4", color, -21, 20)
            {
            public String map(int val)
                {
                return LFO_SHAPES[val + 21];
                }
            };
        hbox.add(comp);
                        
        main.add(hbox);
        hbox = new HBox();

        comp = new LabelledDial("Delay Left", this, "fx" + val + "class8param5", color, 0, 500);
        hbox.add(comp);
                        
        comp = new LabelledDial("Delay Right", this, "fx" + val + "class8param6", color, 0, 500);
        hbox.add(comp);
                        
        comp = new CheckBox("Footswitch", this, "fx" + val + "class8param7");
        vbox = new VBox();
        vbox.add(comp);    
        hbox.add(vbox);
                        
        comp = new LabelledDial("EQ Low", this, "fx" + val + "class8param8", color, -12, 12);
        hbox.add(comp);

        comp = new LabelledDial("EQ High", this, "fx" + val + "class8param9", color, -12, 12);
        hbox.add(comp);
        main.add(hbox);
        hbox = new HBox();




        // 9 HARMONIC CHORUS
        hbox = new HBox();
        main = new VBox();

        fx[9] = main;
                        
        comp = new LabelledDial("LFO Depth", this, "fx" + val + "class9param0", color, 0, 100);
        hbox.add(comp);
                        
        params = FX_SOURCES;
        comp = new Chooser("LFO Depth Mod Source", this, "fx" + val + "class9param1", params);
        vbox = new VBox();
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("LFO Depth Mod Amount", this, "fx" + val + "class9param2", color, -15, 15);
        hbox.add(comp);
                
        comp = new LabelledDial("LFO Rate", this, "fx" + val + "class9param3", color, 1, 100)   
            {
            public String map(int val)
                {
                return CHORUS_LFO_RATE[val - 1];
                }
            };
        hbox.add(comp);

        params = FX_SOURCES;
        comp = new Chooser("LFO Rate Mod Source", this, "fx" + val + "class9param4", params);
        vbox = new VBox();
        vbox.add(comp);
        hbox.add(vbox);

        main.add(hbox);
        hbox = new HBox();

        comp = new LabelledDial("LFO Rate Mod Amount", this, "fx" + val + "class9param5", color, -15, 15);
        hbox.add(comp);
                
        comp = new LabelledDial("Split Point", this, "fx" + val + "class9param6", color, 0, 18)
            {
            public String map(int val)
                {
                return SPLIT_POINTS[val];
                }
            };
        hbox.add(comp);
        main.add(hbox);

        comp = new LabelledDial("Delay Left", this, "fx" + val + "class9param7", color, 0, 500);
        hbox.add(comp);
                        
        comp = new LabelledDial("Delay Right", this, "fx" + val + "class9param8", color, 0, 500);
        hbox.add(comp);
                        
        comp = new CheckBox("Footswitch", this, "fx" + val + "class9param9");
        vbox = new VBox();
        vbox.add(comp);    
        hbox.add(vbox);
                        
        hbox = new HBox();




        // 10 FLANGER
        hbox = new HBox();
        main = new VBox();

        fx[10] = main;
                        
        comp = new LabelledDial("LFO Sweep Range", this, "fx" + val + "class10param0", color, 0, 100);
        hbox.add(comp);
                        
        comp = new LabelledDial("Ramp Speed", this, "fx" + val + "class10param1", color, 0, 100)
            {
            public String map(int val)
                {
                if (val == 0) return "Manual";
                else return "" + val;
                }
            };
        hbox.add(comp);
        model.setMetricMin("fx" + val + "class10param1", 1);
                
        params = FX_SOURCES;
        comp = new Chooser("Ramp Speed Mod Source", this, "fx" + val + "class10param2", params);
        vbox = new VBox();
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Ramp Speed", this, "fx" + val + "class10param3", color, -15, 15);
        ((LabelledDial)comp).addAdditionalLabel("Mod Amount");
        hbox.add(comp);
                
        main.add(hbox);
        hbox = new HBox();
                        
        comp = new LabelledDial("Resonance", this, "fx" + val + "class10param4", color, -100, 100);
        hbox.add(comp);
                        
        comp = new LabelledDial("Top Delay", this, "fx" + val + "class10param5", color, 0, 200);
        hbox.add(comp);
                        
        comp = new LabelledDial("Output Mix", this, "fx" + val + "class10param6", color, -10, 10);
        hbox.add(comp);
                        
        comp = new CheckBox("Footswitch", this, "fx" + val + "class10param7");
        vbox = new VBox();
        vbox.add(comp);    
        hbox.add(vbox);
                        
        comp = new LabelledDial("EQ Low", this, "fx" + val + "class10param8", color, -12, 12);
        hbox.add(comp);

        comp = new LabelledDial("EQ High", this, "fx" + val + "class10param9", color, -12, 12);
        hbox.add(comp);
        main.add(hbox);
        hbox = new HBox();
                


        // 11 ENHANCER / EXCITER
        hbox = new HBox();
        main = new VBox();

        fx[11] = main;
                                                
        comp = buildFXDryWetMixSmall("fx" + val + "class11param0", color);
        hbox.add(comp);
                        
        comp = new LabelledDial("Excitation", this, "fx" + val + "class11param1", color, 0, 100);
        hbox.add(comp);
                        
        comp = new LabelledDial("Hot Spot", this, "fx" + val + "class11param2", color, 1, 20);
        hbox.add(comp);
                        
        comp = new LabelledDial("Stereo Width", this, "fx" + val + "class11param3", color, 0, 100);
        hbox.add(comp);
                
        comp = new LabelledDial("Crossover Delay", this, "fx" + val + "class11param4", color, 0, 100);
        // Though this is in fact the correct delay in ms, the Wavestation only displays 0...100
//                              {
//                              public String map(int val)
//                                      {
//                                      return "" + (val * 32);
//                                      }
//                              };
        hbox.add(comp);
                        
        comp = new LabelledDial("EQ Low", this, "fx" + val + "class11param5", color, -12, 12);
        hbox.add(comp);

        comp = new LabelledDial("EQ High", this, "fx" + val + "class11param6", color, -12, 12);
        hbox.add(comp);
        main.add(hbox);
        hbox = new HBox();
                



        // 12 DISTORTION / FILTER
        hbox = new HBox();
        main = new VBox();

        fx[12] = main;
                                                
        comp = buildFXDryWetMixSmall("fx" + val + "class12param0", color);
        hbox.add(comp);
                        
        comp = new LabelledDial("Edge", this, "fx" + val + "class12param1", color, 1, 111);
        hbox.add(comp);
                        
        comp = new LabelledDial("Hot Spot", this, "fx" + val + "class12param2", color, 0, 100);
        hbox.add(comp);
                        
        params = FX_SOURCES;
        comp = new Chooser("Hot Spot Mod Source", this, "fx" + val + "class12param3", params);
        vbox = new VBox();
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Hot Spot Mod Amount", this, "fx" + val + "class12param4", color, -15, 15);
        hbox.add(comp);

        comp = new LabelledDial("Resonance", this, "fx" + val + "class12param5", color, 0, 100);
        hbox.add(comp);
                
        main.add(hbox);
        hbox = new HBox();

        comp = new LabelledDial("Output Level", this, "fx" + val + "class12param6", color, 0, 100);
        hbox.add(comp);
                
        params = FX_SOURCES;
        comp = new Chooser("Output Level Mod Source", this, "fx" + val + "class12param7", params);
        vbox = new VBox();
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Output Level", this, "fx" + val + "class12param8", color, -15, 15);
        ((LabelledDial)comp).addAdditionalLabel("Mod Amount");
        hbox.add(comp);

        comp = new CheckBox("Footswitch", this, "fx" + val + "class12param9");
        vbox = new VBox();
        vbox.add(comp);    
        hbox.add(vbox);
                        
        comp = new LabelledDial("EQ Low", this, "fx" + val + "class12param10", color, -12, 12);
        hbox.add(comp);

        comp = new LabelledDial("EQ High", this, "fx" + val + "class12param11", color, -12, 12);
        hbox.add(comp);
        main.add(hbox);
        hbox = new HBox();



        // 13 STEREO PHASER
        hbox = new HBox();
        main = new VBox();

        fx[13] = main;
                                                
        comp = new LabelledDial("Dry/Wet", this, "fx" + val + "class13param0", color, -10, 10)
            {
            public String map(int val)
                {
                if (val == 0)
                    return "Dry";
                else if (val == 10)
                    return "Wet";
                else if (val == -10)
                    return "-Wet";
                else if (val > 0)
                    return "" + (10 - val) + "/" + val;
                else
                    return "-" + (10 - (-val)) + "/" + (-val);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Mix");
        hbox.add(comp);

        comp = new LabelledDial("LFO Depth", this, "fx" + val + "class13param1", color, 0, 100);
        hbox.add(comp);
                
        params = FX_SOURCES;
        comp = new Chooser("LFO Depth Mod Source", this, "fx" + val + "class13param2", params);
        vbox = new VBox();
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("LFO Depth Mod Amount", this, "fx" + val + "class13param3", color, -15, 15);
        hbox.add(comp);

        comp = new LabelledDial("LFO Rate", this, "fx" + val + "class13param4", color, 0, 100)  
            {
            public String map(int val)
                {
                if (val == 0) return "Fixed";
                else return CHORUS_LFO_RATE[val - 1];
                }
            };
        hbox.add(comp);
        model.setMetricMin("fx" + val + "class13param4", 1);
                        
        main.add(hbox);
        hbox = new HBox();

        params = FX_SOURCES;
        comp = new Chooser("LFO Rate Mod Source", this, "fx" + val + "class13param5", params);
        vbox = new VBox();
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("LFO Rate Mod Amount", this, "fx" + val + "class13param6", color, -15, 15);
        hbox.add(comp);

        comp = new LabelledDial("Center Frequency", this, "fx" + val + "class13param7", color, 0, 100);
        hbox.add(comp);

        comp = new LabelledDial("Feedback", this, "fx" + val + "class13param8", color, -100, 100);
        hbox.add(comp);

        comp = new CheckBox("Footswitch", this, "fx" + val + "class13param9");
        vbox = new VBox();
        vbox.add(comp);    
        hbox.add(vbox);
                        
        main.add(hbox);


        hbox = new HBox();




        // 14 ROTARY SPEAKER
        hbox = new HBox();
        main = new VBox();

        fx[14] = main;
                                                
        comp = buildFXDryWetMixSmall("fx" + val + "class14param0", color);
        hbox.add(comp);
                        
        comp = new LabelledDial("Depth", this, "fx" + val + "class14param1", color, 0, 15);
        hbox.add(comp);
                        
        comp = new LabelledDial("Slow Speed", this, "fx" + val + "class14param2", color, 1, 100)        
            {
            public String map(int val)
                {
                return CHORUS_LFO_RATE[val - 1];
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Fast Speed", this, "fx" + val + "class14param3", color, 1, 100)        
            {
            public String map(int val)
                {
                return CHORUS_LFO_RATE[val - 1];
                }
            };
        hbox.add(comp);

        params = FX_SOURCES;
        comp = new Chooser("Speed Mod Source", this, "fx" + val + "class14param4", params);
        vbox = new VBox();
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Acceleration", this, "fx" + val + "class14param5", color, 1, 15);
        hbox.add(comp);
                        
        comp = new CheckBox("Footswitch", this, "fx" + val + "class14param6");
        vbox = new VBox();
        vbox.add(comp);    
        hbox.add(vbox);
                        
        main.add(hbox);
        hbox = new HBox();



        // 15 STEREO MOD PAN
        hbox = new HBox();
        main = new VBox();

        fx[15] = main;
                                                
        comp = buildFXDryWetMixSmall("fx" + val + "class15param0", color);
        hbox.add(comp);
                        
        params = FX_SOURCES;
        comp = new Chooser("Dry/Wet Mod Source", this, "fx" + val + "class15param1", params);
        vbox = new VBox();
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Dry/Wet Mod Amount", this, "fx" + val + "class15param2", color, -15, 15);
        hbox.add(comp);
                        
        comp = new LabelledDial("LFO Depth", this, "fx" + val + "class15param3", color, 0, 10);
        hbox.add(comp);
                        
        main.add(hbox);
        hbox = new HBox();

        params = FX_SOURCES;
        comp = new Chooser("LFO Depth Mod Source", this, "fx" + val + "class15param4", params);
        vbox = new VBox();
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("LFO Depth Mod Amount", this, "fx" + val + "class15param5", color, -15, 15);
        hbox.add(comp);

        comp = new LabelledDial("LFO Rate", this, "fx" + val + "class15param6", color, 1, 100)  
            {
            public String map(int val)
                {
                return CHORUS_LFO_RATE[val - 1];
                }
            };
        hbox.add(comp);
                
        comp = new LabelledDial("EQ Low", this, "fx" + val + "class15param7", color, -12, 12);
        hbox.add(comp);

        comp = new LabelledDial("EQ High", this, "fx" + val + "class15param8", color, -12, 12);
        hbox.add(comp);
        main.add(hbox);
        hbox = new HBox();



        // 16. STEREO PARAMETRIC EQ
        hbox = new HBox();
        main = new VBox();

        fx[16] = main;
                                                
        comp = new LabelledDial("High Frequency", this, "fx" + val + "class16param0", color, 0, 29)
            {
            public String map(int val)      
                {
                return FREQUENCIES[val];
                }
            };
        ((LabelledDial)comp).addAdditionalLabel(""); // spacer
        hbox.add(comp);
                        
        comp = new LabelledDial("High Level", this, "fx" + val + "class16param1", color, -12, 12);
        hbox.add(comp);
                        
        comp = new LabelledDial("Mid Frequency", this, "fx" + val + "class16param2", color, 1, 100);
        hbox.add(comp);
                        
        comp = new LabelledDial("Mid Level", this, "fx" + val + "class16param3", color, -12, 12);
        hbox.add(comp);
                        
        comp = new LabelledDial("Mid Width", this, "fx" + val + "class16param4", color, 1, 100);
        hbox.add(comp);
                        
        main.add(hbox);
        hbox = new HBox();

        params = FX_SOURCES;
        comp = new Chooser("Mid Frequency Mod Source", this, "fx" + val + "class16param5", params);
        vbox = new VBox();
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Mid Frequency Mod Amount", this, "fx" + val + "class16param6", color, -15, 15);
        hbox.add(comp);

        comp = new LabelledDial("Low Frequency", this, "fx" + val + "class16param7", color, 0, 29)
            {
            public String map(int val)      
                {
                return FREQUENCIES[val];
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Low Level", this, "fx" + val + "class16param8", color, -12, 12);
        hbox.add(comp);
                
        main.add(hbox);
        hbox = new HBox();



        // 17. CHORUS/STEREO DELAY
                        
        hbox = new HBox();
        main = new VBox();

        fx[17] = main;
                                                
        comp = buildFXDryWetMix("fx" + val + "class17param0", color);
        hbox.add(comp);
                        
        comp = new LabelledDial("Chorus Delay", this, "fx" + val + "class17param1", color, 0, 50);
        hbox.add(comp);
                                                
        comp = new LabelledDial("Chorus LFO Depth", this, "fx" + val + "class17param2", color, 0, 100);
        hbox.add(comp);

        comp = new LabelledDial("Chorus LFO Rate", this, "fx" + val + "class17param3", color, 1, 100)
            {
            public String map(int val)
                {
                return CHORUS_LFO_RATE[val - 1];
                }
            };
                                
        hbox.add(comp);
                        
        comp = new LabelledDial("Chorus Feedback", this, "fx" + val + "class17param4", color, -100, 100);
        hbox.add(comp);
                        
        comp = new LabelledDial("Delay Time", this, "fx" + val + "class17param5", color, 0, 450);
        hbox.add(comp);

        main.add(hbox);
        hbox = new HBox();

        comp = new LabelledDial("Delay Feedback", this, "fx" + val + "class17param6", color, -100, 100);
        hbox.add(comp);

        comp = new CheckBox("Footswitch Sample", this, "fx" + val + "class17param7");
        vbox = new VBox();
        vbox.add(comp);    
        hbox.add(vbox);

        comp = new LabelledDial("EQ Low", this, "fx" + val + "class17param8", color, -12, 12);
        hbox.add(comp);

        comp = new LabelledDial("EQ High", this, "fx" + val + "class17param9", color, -12, 12);
        hbox.add(comp);
        main.add(hbox);
        hbox = new HBox();




        // 18. FLANGER/STEREO DELAY -- basically identical to chorus/delay
        hbox = new HBox();
        main = new VBox();

        fx[18] = main;
                                                
        comp = buildFXDryWetMix("fx" + val + "class18param0", color);
        hbox.add(comp);
                        
        comp = new LabelledDial("Flanger Delay", this, "fx" + val + "class18param1", color, 0, 50);
        hbox.add(comp);
                        
        comp = new LabelledDial("Flanger LFO", this, "fx" + val + "class18param2", color, 0, 100);
        ((LabelledDial)comp).addAdditionalLabel("Depth");
        hbox.add(comp);
                        
        comp = new LabelledDial("Flanger LFO", this, "fx" + val + "class18param3", color, 1, 100)
            {
            public String map(int val)
                {
                return CHORUS_LFO_RATE[val - 1];
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Rate");
        hbox.add(comp);
        comp = new LabelledDial("Flanger", this, "fx" + val + "class18param4", color, -100, 100);
        ((LabelledDial)comp).addAdditionalLabel("Feedback");
        hbox.add(comp);
                        
        comp = new LabelledDial("Delay Time", this, "fx" + val + "class18param5", color, 0, 450);
        hbox.add(comp);

        comp = new LabelledDial("Delay Feedback", this, "fx" + val + "class18param6", color, -100, 100);
        hbox.add(comp);

        main.add(hbox);
        hbox = new HBox();

        comp = new CheckBox("Footswitch Sample", this, "fx" + val + "class18param7");
        vbox = new VBox();
        vbox.add(comp);    
        hbox.add(vbox);

        comp = new LabelledDial("EQ Low", this, "fx" + val + "class18param8", color, -12, 12);
        hbox.add(comp);

        comp = new LabelledDial("EQ High", this, "fx" + val + "class18param9", color, -12, 12);
        hbox.add(comp);
        main.add(hbox);
        hbox = new HBox();



        // 19. DUAL MONO DELAY/REVERB (HALL)
        hbox = new HBox();
        main = new VBox();

        fx[19] = main;                  

        comp = buildFXDryWetMixSmall("fx" + val + "class19param0", " (Delay)", color);
        hbox.add(comp);
                        
        comp = new LabelledDial("Delay Time", this, "fx" + val + "class19param1", color, 0, 500);
        hbox.add(comp);

        comp = new LabelledDial("Delay Feedback", this, "fx" + val + "class19param2", color, -100, 100);
        hbox.add(comp);

        main.add(hbox);
        hbox = new HBox();

        comp = buildFXDryWetMix("fx" + val + "class19param3", " (Reverb)", color);
        hbox.add(comp);
                        
        comp = new LabelledDial("Reverb Decay Time", this, "fx" + val + "class19param4", color, 0, 99);
        hbox.add(comp);

        comp = new LabelledDial("Reverb Pre-Delay", this, "fx" + val + "class19param5", color, 0, 150);
        hbox.add(comp);

        comp = new LabelledDial("Reverb High Frequency Damping", this, "fx" + val + "class19param6", color, 0, 99);
        hbox.add(comp);
        main.add(hbox);
        hbox = new HBox();




        // 20. DUAL MONO DELAY/REVERB (ROOM)
        hbox = new HBox();
        main = new VBox();

        fx[20] = main;                  

        comp = buildFXDryWetMixSmall("fx" + val + "class20param0", " (Delay)", color);
        hbox.add(comp);
                        
        comp = new LabelledDial("Delay Time", this, "fx" + val + "class20param1", color, 0, 500);
        hbox.add(comp);

        comp = new LabelledDial("Delay Feedback", this, "fx" + val + "class20param2", color, -100, 100);
        hbox.add(comp);

        main.add(hbox);
        hbox = new HBox();

        comp = buildFXDryWetMix("fx" + val + "class20param3", " (Reverb)", color);
        hbox.add(comp);
                        
        comp = new LabelledDial("Reverb Decay Time", this, "fx" + val + "class20param4", color, 0, 99);
        hbox.add(comp);

        comp = new LabelledDial("Reverb Pre-Delay", this, "fx" + val + "class20param5", color, 0, 250);
        hbox.add(comp);

        comp = new LabelledDial("Reverb High Frequency Damping", this, "fx" + val + "class20param6", color, 0, 99);
        hbox.add(comp);
        main.add(hbox);
        hbox = new HBox();



        // 21. DUAL MONO DELAY/CHORUS
        hbox = new HBox();
        main = new VBox();

        fx[21] = main;                  

        comp = buildFXDryWetMixSmall("fx" + val + "class21param0", " (Delay)", color);
        hbox.add(comp);
                        
        comp = new LabelledDial("Delay Time", this, "fx" + val + "class21param1", color, 0, 500);
        hbox.add(comp);

        comp = new LabelledDial("Delay Feedback", this, "fx" + val + "class21param2", color, -100, 100);
        hbox.add(comp);

        main.add(hbox);
        hbox = new HBox();

        comp = new LabelledDial("Chorus Delay Time", this, "fx" + val + "class21param3", color, 0, 500);
        hbox.add(comp);

        comp = new LabelledDial("Chorus LFO Depth", this, "fx" + val + "class21param4", color, 0, 100);
        hbox.add(comp);
                        
        comp = new LabelledDial("Chorus LFO Rate", this, "fx" + val + "class21param5", color, 1, 100)
            {
            public String map(int val)
                {
                return CHORUS_LFO_RATE[val - 1];
                }
            };
                                
        hbox.add(comp);
                        
        comp = new LabelledDial("Chorus Feedback", this, "fx" + val + "class21param6", color, -100, 100);
        hbox.add(comp);
        main.add(hbox);
        hbox = new HBox();
                        



        // 22. DUAL MONO DELAY/FLANGER
        hbox = new HBox();
        main = new VBox();

        fx[22] = main;                  

        comp = buildFXDryWetMixSmall("fx" + val + "class22param0", " (Delay)", color);
        hbox.add(comp);
                        
        comp = new LabelledDial("Delay Time", this, "fx" + val + "class22param1", color, 0, 500);
        hbox.add(comp);

        comp = new LabelledDial("Delay Feedback", this, "fx" + val + "class22param2", color, -100, 100);
        hbox.add(comp);

        main.add(hbox);
        hbox = new HBox();

        comp = new LabelledDial("Flanger Delay Time", this, "fx" + val + "class22param3", color, 0, 500);
        hbox.add(comp);

        comp = new LabelledDial("Flanger LFO Depth", this, "fx" + val + "class22param4", color, 0, 100);
        hbox.add(comp);
                        
        comp = new LabelledDial("Flanger LFO Rate", this, "fx" + val + "class22param5", color, 1, 100)
            {
            public String map(int val)
                {
                return CHORUS_LFO_RATE[val - 1];
                }
            };
                                
        hbox.add(comp);
                        
        comp = new LabelledDial("Flanger Feedback", this, "fx" + val + "class22param6", color, -100, 100);
        hbox.add(comp);
        main.add(hbox);
        hbox = new HBox();






        // 23. DUAL MONO DELAY/OVERDRIVE-DISTORTION
        hbox = new HBox();
        main = new VBox();

        fx[23] = main;                  

        comp = buildFXDryWetMixSmall("fx" + val + "class23param0", " (Delay)", color);
        hbox.add(comp);
                        
        comp = new LabelledDial("Delay Time", this, "fx" + val + "class23param1", color, 0, 500);
        hbox.add(comp);

        comp = new LabelledDial("Delay Feedback", this, "fx" + val + "class23param2", color, -100, 100);
        hbox.add(comp);

        main.add(hbox);
        hbox = new HBox();

        comp = new LabelledDial("Edge", this, "fx" + val + "class23param3", color, 1, 111);
        hbox.add(comp);

        comp = new LabelledDial("Hot Spot", this, "fx" + val + "class23param4", color, 1, 100);
        hbox.add(comp);
                        
        comp = new LabelledDial("Resonance", this, "fx" + val + "class23param5", color, 0, 100);
        hbox.add(comp);
                        
        comp = new LabelledDial("Distortion Output Level", this, "fx" + val + "class23param6", color, 0, 100);
        hbox.add(comp);
        main.add(hbox);
        hbox = new HBox();




        // 24. DUAL MONO DELAY/PHASER
        hbox = new HBox();
        main = new VBox();

        fx[24] = main;                  

        comp = buildFXDryWetMixSmall("fx" + val + "class24param0", " (Delay)", color);
        hbox.add(comp);
                        
        comp = new LabelledDial("Delay Time", this, "fx" + val + "class24param1", color, 0, 500);
        hbox.add(comp);

        comp = new LabelledDial("Delay Feedback", this, "fx" + val + "class24param2", color, -100, 100);
        hbox.add(comp);

        main.add(hbox);
        hbox = new HBox();

        comp = new LabelledDial("Phaser Center", this, "fx" + val + "class24param3", color, 0, 100);
        hbox.add(comp);

        comp = new LabelledDial("Phaser LFO Depth", this, "fx" + val + "class24param4", color, 0, 100);
        hbox.add(comp);
                        
        comp = new LabelledDial("Phaser LFO Rate", this, "fx" + val + "class24param5", color, 1, 100)
            {
            public String map(int val)
                {
                return CHORUS_LFO_RATE[val - 1];
                }
            };
                                
        hbox.add(comp);
                        
        comp = new LabelledDial("Phaser Feedback", this, "fx" + val + "class24param6", color, -100, 100);
        hbox.add(comp);
        main.add(hbox);
        hbox = new HBox();



        // 25. DUAL MONO DELAY/ROTARY SPEAKER
        hbox = new HBox();
        main = new VBox();

        fx[25] = main;                  

        comp = buildFXDryWetMixSmall("fx" + val + "class25param0", " (Delay)", color);
        hbox.add(comp);
                                        
        comp = new LabelledDial("Delay Time", this, "fx" + val + "class25param1", color, 0, 500);
        hbox.add(comp);

        comp = new LabelledDial("Delay Feedback", this, "fx" + val + "class25param2", color, -100, 100);
        hbox.add(comp);

        main.add(hbox);
        hbox = new HBox();
                
        comp = new LabelledDial("Slow Speed", this, "fx" + val + "class25param3", color, 1, 100)        
            {
            public String map(int val)
                {
                return CHORUS_LFO_RATE[val - 1];
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Fast Speed", this, "fx" + val + "class25param4", color, 1, 100)        
            {
            public String map(int val)
                {
                return CHORUS_LFO_RATE[val - 1];
                }
            };
        hbox.add(comp);

        params = FX_SOURCES;
        comp = new Chooser("Speed Mod Source", this, "fx" + val + "class25param5", params);
        vbox = new VBox();
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Acceleration", this, "fx" + val + "class25param6", color, 1, 15);
        hbox.add(comp);

        main.add(hbox);
        hbox = new HBox();





        // 26. STEREO PITCH SHIFTER
        hbox = new HBox();
        main = new VBox();

        fx[26] = main;                  

        comp = buildFXDryWetMixSmall("fx" + val + "class26param0", " (Delay)", color);
        hbox.add(comp);
                        
        params = FX_SOURCES;
        comp = new Chooser("Mix Modulation Source", this, "fx" + val + "class26param1", params);
        vbox = new VBox();
        vbox.add(comp);
        hbox.add(vbox);
                        
        comp = new LabelledDial("Mix Modulation", this, "fx" + val + "class26param2", color, -15, 15);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        comp = new LabelledDial("Pitch Shift", this, "fx" + val + "class26param3", color, 1, 100);      
        hbox.add(comp);

        comp = new LabelledDial("Delay Left", this, "fx" + val + "class26param4", color, 0, 500);
        hbox.add(comp);

        comp = new LabelledDial("Delay Right", this, "fx" + val + "class26param5", color, 0, 500);
        hbox.add(comp);

        main.add(hbox);
        hbox = new HBox();




        // 27. MODULATABLE PITCH SHIFTER-DELAY
        hbox = new HBox();
        main = new VBox();

        fx[27] = main;                  

        comp = buildFXDryWetMixSmall("fx" + val + "class27param0", " (Delay)", color);
        hbox.add(comp);
                        
        params = FX_SOURCES;
        comp = new Chooser("Mix Modulation Source", this, "fx" + val + "class27param1", params);
        vbox = new VBox();
        vbox.add(comp);
        hbox.add(vbox);
                        
        comp = new LabelledDial("Mix Modulation", this, "fx" + val + "class27param2", color, -15, 15);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        comp = new LabelledDial("Max Shift", this, "fx" + val + "class27param3", color, -12, 12);       
        hbox.add(comp);

        main.add(hbox);
        hbox = new HBox();

        comp = new LabelledDial("Shift Scaler", this, "fx" + val + "class27param4", color, 1, 100)
            {
            public String map(int val)
                {
                return "" + val + "%";
                }
            };
        hbox.add(comp);

        params = FX_SOURCES;
        comp = new Chooser("Shift Scaler Modulation Source", this, "fx" + val + "class27param5", params);
        vbox = new VBox();
        vbox.add(comp);
        hbox.add(vbox);
                        
        comp = new LabelledDial("Shift Scaler", this, "fx" + val + "class27param6", color, -15, 15);    
        ((LabelledDial)comp).addAdditionalLabel("Modulation Amount");
        hbox.add(comp);

        comp = new LabelledDial("Delay Left", this, "fx" + val + "class27param7", color, 0, 490);
        hbox.add(comp);

        comp = new LabelledDial("Delay Right", this, "fx" + val + "class27param8", color, 0, 490);
        hbox.add(comp);

        comp = new LabelledDial("Feedback", this, "fx" + val + "class27param9", color, 1, 100); 
        hbox.add(comp);

        main.add(hbox);
        hbox = new HBox();





        // 28. STEREO COMPRESSOR-LIMITER/GATE
        hbox = new HBox();
        main = new VBox();

        fx[28] = main;                  

        params = CONTROL_SOURCES;
        comp = new Chooser("Control Source", this, "fx" + val + "class28param0", params);
        vbox = new VBox();
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Sensitivity", this, "fx" + val + "class28param1", color, 0, 100);
        hbox.add(comp);

        comp = new LabelledDial("Compression Ratio", this, "fx" + val + "class28param2", color, 0, 100);
        hbox.add(comp);

        comp = new LabelledDial("Compression", this, "fx" + val + "class28param3", color, 0, 100);
        ((LabelledDial)comp).addAdditionalLabel("Threshold");
        hbox.add(comp);

        comp = new LabelledDial("Gate Threshold", this, "fx" + val + "class28param4", color, 0, 100);   
        hbox.add(comp);

        comp = new LabelledDial("Output Level", this, "fx" + val + "class28param5", color, 0, 100);     
        hbox.add(comp);
        main.add(hbox);
        hbox = new HBox();





        // 29. SMALL VOCODER
        hbox = new HBox();
        main = new VBox();

        fx[29] = main;                  

        params = VOCODER_BUSSES;
        comp = new Chooser("Modulator Bus", this, "fx" + val + "class29param0", params);
        vbox = new VBox();
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Modulator Bus", this, "fx" + val + "class29param1", color, 0, 100);
        ((LabelledDial)comp).addAdditionalLabel("Sensitivity");
        hbox.add(comp);

        params = VOCODER_BUSSES;
        comp = new Chooser("Carrier Bus", this, "fx" + val + "class29param2", params);
        vbox = new VBox();
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Carrier Bus", this, "fx" + val + "class29param3", color, 0, 100);
        ((LabelledDial)comp).addAdditionalLabel("Sensitivity");
        hbox.add(comp);


        comp = new LabelledDial("Sibilance", this, "fx" + val + "class29param4", color, 0, 10); 
        hbox.add(comp);

        main.add(hbox);
        hbox = new HBox();

        params = FX_SOURCES;
        comp = new Chooser("Sibilance Mod Source", this, "fx" + val + "class29param5", params);
        vbox = new VBox();
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Sibilance Mod Amount", this, "fx" + val + "class29param6", color, -15, 15);    
        hbox.add(comp);



        main.add(hbox);
        hbox = new HBox();






        // 30. STEREO VOCODER-DELAY
        hbox = new HBox();
        main = new VBox();

        fx[30] = main;                  

        params = VOCODER_BUSSES;
        comp = new Chooser("Modulator Bus", this, "fx" + val + "class30param0", params);
        vbox = new VBox();
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Modulator Bus", this, "fx" + val + "class30param1", color, 0, 100);
        ((LabelledDial)comp).addAdditionalLabel("Sensitivity");
        hbox.add(comp);

        params = VOCODER_BUSSES;
        comp = new Chooser("Carrier Bus", this, "fx" + val + "class30param2", params);
        vbox = new VBox();
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Carrier Bus", this, "fx" + val + "class30param3", color, 0, 100);
        ((LabelledDial)comp).addAdditionalLabel("Sensitivity");
        hbox.add(comp);

        comp = new LabelledDial("Sibilance", this, "fx" + val + "class30param4", color, 0, 10); 
        hbox.add(comp);

        params = FX_SOURCES;
        comp = new Chooser("Sibilance Mod Source", this, "fx" + val + "class30param5", params);
        vbox = new VBox();
        vbox.add(comp);
        hbox.add(vbox);

        main.add(hbox);
        hbox = new HBox();

        comp = new LabelledDial("Sibilance Mod Amount", this, "fx" + val + "class30param6", color, -15, 15);    
        hbox.add(comp);

        comp = new LabelledDial("Stereo Width", this, "fx" + val + "class30param7", color, 0, 10);      
        hbox.add(comp);

        comp = new LabelledDial("Delay Time", this, "fx" + val + "class30param8", color, 0, 1000);      
        hbox.add(comp);

        vbox = new VBox();      
        comp = new PushButton("Up")
            {
            public void perform()
                {
                int delay = model.get("fx" + val + "class30param8", 1);
                int max = model.getMax("fx" + val + "class30param8");
                if (delay < max)
                    model.set("fx" + val + "class30param8", delay + 1);
                }
            };
        vbox.add(comp);
        comp = new PushButton("Down")
            {
            public void perform()
                {
                int delay = model.get("fx" + val + "class30param8", 1);
                int min = model.getMin("fx" + val + "class30param8");
                if (delay > min)
                    model.set("fx" + val + "class30param8", delay - 1);
                }
            };
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Delay Feedback", this, "fx" + val + "class30param9", color, 0, 100);   
        hbox.add(comp);

        comp = new LabelledDial("Delay Level", this, "fx" + val + "class30param10", color, 0, 100);     
        hbox.add(comp);
        main.add(hbox);
        hbox = new HBox();

                        
        return fx;
        }
        

    public JComponent addFX(final int fxnum, Color color)
        {
        Category category  = new Category(this, "FX " + fxnum, color);
        category.makePasteable("fx" + fxnum);
                        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        fx[fxnum - 1] = buildFX(fxnum, color);
                
        HBox main = new HBox();
                
        params = FX;
        comp = new Chooser("Type", this, "fx" + fxnum + "type", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                int type = model.get(key, 0);
                main.removeLast();
                main.addLast(fx[fxnum - 1][FX_MAP[type]]);
                main.revalidate();
                main.repaint();
                setFXPreset(fxnum);
                }
            };
        vbox = new VBox();
        vbox.add(comp);
        main.add(vbox);

        main.addLast(fx[fxnum - 1][FX_MAP[0]]);
                        
        category.add(main, BorderLayout.CENTER);
        return category;
        }

    public JComponent addFXGlobal(Color color)
        {
        Category category  = new Category(this, "FX", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        comp = new CheckBox("Series", this, "fxseries", false);
        vbox.add(comp);    
        hbox.add(vbox);
        
        comp = new LabelledDial("Mix 3", this, "fxmix3amount", color, 0, 11)
            {
            public double getStartAngle()
                {
                return 238;
                }
            
            public int getDefaultValue()
                {
                return 6;
                }
                
            public String map(int val)
                {
                return (model.get("fxseries", 0) == 0 ? PARALLEL_MIX[val] : SERIES_MIX[val]);
                }
            };
        model.register("fxseries", ((LabelledDial)comp));  // so it changes from parallel to series
        hbox.add(comp);
        model.setMetricMin("fxmix3amount", 1);

        comp = new LabelledDial("Mix 4", this, "fxmix4amount", color, 0, 11)
            {
            public double getStartAngle()
                {
                return 238;
                }
            
            public int getDefaultValue()
                {
                return 6;
                }
                
            public String map(int val)
                {
                return (model.get("fxseries", 0)  == 0 ? PARALLEL_MIX[val] : SERIES_MIX[val]);
                }
            };
        model.register("fxseries", ((LabelledDial)comp));  // so it changes from parallel to series
        hbox.add(comp);
        model.setMetricMin("fxmix4amount", 1);
        
        vbox = new VBox();
        params = FX_SOURCES;
        comp = new Chooser("Mod 3 Source", this, "fxmod3source", params);
        vbox.add(comp);

        params = FX_SOURCES;
        comp = new Chooser("Mod 4 Source", this, "fxmod4source", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Mod 3", this, "fxmod3amount", color, -15, 15);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        comp = new LabelledDial("Mod 4", this, "fxmod4amount", color, -15, 15);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        category.add(hbox, BorderLayout.WEST);
        return category;
        }
        
    public JComponent addPart(int part, Color color)
        {
        final Category category  = new Category(this, "Part " + part, color);
        category.makePasteable("part" + part);
        
        Updatable updatable = new Updatable()
            {
            public void update(String key, Model model)
                {
                if (model.get("part" + part + "bank", 0) < 0 || 
                    model.get("part" + part + "number", 0) < 0)
                    category.setName("Part " + part + " [Off] ");
                else
                    category.setName("Part " + part + ": " + PATCHES[model.get("part" + part + "bank", 0)][model.get("part" + part + "number", 0)] + " ");
                }
            };
        
        model.register("part" + part + "bank", updatable);
        model.register("part" + part + "number", updatable);
                        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        VBox main = new VBox();
        

        comp = new LabelledDial("Patch Bank", this, "part" + part + "bank", color, 0, 11)
            {
            public String map(int val)
                {
                String[] vals = BANKS;
                return vals[val];
                }
            };
        ((LabelledDial)comp).addAdditionalLabel(" ");
        model.removeMetricMinMax("part" + part + "bank");
        hbox.add(comp);

        final PushButton button = new PushButton("Show")
            {
            public void perform()
                {
                final KorgWavestationPatch synth = new KorgWavestationPatch();
                if (tuple != null)
                    synth.tuple = tuple.copy(synth.buildInReceiver(), synth.buildKeyReceiver());
                if (synth.tuple != null)
                    {       
                    // This is a little tricky.  When the dump comes in from the synth,
                    // Edisyn will only send it to the topmost panel.  So we first sprout
                    // the panel and show it, and THEN send the dump request.  But this isn't
                    // enough, because what setVisible(...) does is post an event on the
                    // Swing Event Queue to build the window at a later time.  This later time
                    // happens to be after the dump comes in, so it's ignored.  So what we
                    // ALSO do is post the dump request to occur at the end of the Event Queue,
                    // so by the time the dump request has been made, the window is shown and
                    // frontmost.
                                                
                    synth.sprout();
                    JFrame frame = ((JFrame)(SwingUtilities.getRoot(synth)));
                    frame.setVisible(true);

                    SwingUtilities.invokeLater(
                        new Runnable()
                            {
                            public void run() 
                                { 
                                Model tempModel = new Model();
                                tempModel.set("bank", KorgWavestationPerformance.this.model.get("part" + part + "bank"));
                                tempModel.set("number", KorgWavestationPerformance.this.model.get("part" + part + "number"));
                                synth.performRequestDump(tempModel, false);
                                }
                            });
                    }
                else
                    {
                    showSimpleError("Disconnected", "You can't show a patch when disconnected.");
                    }
                }
            };

        comp = new LabelledDial("Patch Number", this, "part" + part + "number", color, -1, 34)
            {
            public String map(int val)
                {
                if (val == -1)
                    return "Off";
                else return "" + val;
                }
                
            public void update(String key, Model model)
                {
                super.update(key, model);
                button.getButton().setEnabled(model.get(key) >= 0);
                }
                
            };
        model.removeMetricMinMax("part" + part + "number");
        hbox.add(comp);
                
        vbox.add(button);

        comp = new CheckBox("Sustain", this, "part" + part + "sustain", true);
        vbox.add(comp);    
        hbox.add(vbox);
        
        vbox = new VBox();
        params = POLYPHONY;
        comp = new Chooser("Voice Mode", this, "part" + part + "voicemode", params);
        vbox.add(comp);
                
        params = KEY_PRIORITY;
        comp = new Chooser("Key Priority", this, "part" + part + "keypriority", params);
        vbox.add(comp);
        hbox.add(vbox);
        
        vbox = new VBox();
        params = SCALES;
        comp = new Chooser("Scale", this, "part" + part + "scale", params);
        vbox.add(comp);

        params = PLAY_MODES;
        comp = new Chooser("Play Mode", this, "part" + part + "playmode", params);
        vbox.add(comp);

        hbox.add(vbox);
        comp = new LabelledDial("Scale", this, "part" + part + "scalekey", color, 1, 12) 
            {
            public String map(int val)
                {
                return KEYS[(val - 1) % 12];  // note integer division
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Tonic Key");
        hbox.add(comp);

        comp = new LabelledDial("Volume", this, "part" + part + "volume", color, 0, 99);
        hbox.add(comp);

        main.add(hbox);
        hbox = new HBox();

        comp = new LabelledDial("FX Bus", this, "part" + part + "fxbus", color, 0, 105)
            {
            public double getStartAngle()
                {
                return 218;
                }
            
            public int getDefaultValue()
                {
                return 50;
                }
                
            public String map(int val)
                {
                if (val >= 1 && val <= 99)
                    return "" + (100 - val) + "/" + val;
                else if (val == 0)
                    return "A";
                else
                    return HIGH_BUSSES[val - 100];
                }
            };
        hbox.add(comp);
        
        
        comp = new LabelledDial("Delay", this, "part" + part + "delay", color, 0, 9999);
        ((LabelledDial)comp).addAdditionalLabel("(in ms)");
        hbox.add(comp);

        vbox = new VBox();      
        comp = new PushButton("Up")
            {
            public void perform()
                {
                int delay = model.get("part" + part + "delay", 1);
                int max = model.getMax("part" + part + "delay");
                if (delay < max)
                    model.set("part" + part + "delay", delay + 1);
                }
            };
        vbox.add(comp);
        comp = new PushButton("Down")
            {
            public void perform()
                {
                int delay = model.get("part" + part + "delay", 1);
                if (delay > 0)
                    model.set("part" + part + "delay", delay - 1);
                }
            };
        vbox.add(comp);
        hbox.add(vbox);
    



        comp = new LabelledDial("Transpose", this, "part" + part + "transpose", color, -24, 24);
        hbox.add(comp);

        comp = new LabelledDial("Detune", this, "part" + part + "detune", color, -49, 49);
        hbox.add(comp);

        comp = new LabelledDial("Lowest", this, "part" + part + "lowkey", color, 0, 127) 
            {
            public String map(int val)
                {
                return KEYS[val % 12] + (val / 12 - 1);  // note integer division
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Key");
        hbox.add(comp);

        comp = new LabelledDial("Highest", this, "part" + part + "hikey", color, 0, 127)
            {
            public String map(int val)
                {
                return KEYS[val % 12] + (val / 12 - 1);  // note integer division
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Key");
        hbox.add(comp);

        comp = new LabelledDial("Lowest", this, "part" + part + "lowvel", color, 1, 127);
        ((LabelledDial)comp).addAdditionalLabel("Velocity");
        hbox.add(comp);

        comp = new LabelledDial("Highest", this, "part" + part + "hivel", color, 1, 127);
        ((LabelledDial)comp).addAdditionalLabel("Velocity");
        hbox.add(comp);

        // These are for the A/D only.
/*
  comp = new LabelledDial("Output", this, "part" + part + "outputchannel", color, 1, 16);
  ((LabelledDial)comp).addAdditionalLabel("Channel [WS, EX]");
  hbox.add(comp);
    
  comp = new LabelledDial("Program", this, "part" + part + "programnumber", color, 0, 127);
  ((LabelledDial)comp).addAdditionalLabel(" Number [WS, EX] ");
  hbox.add(comp);
*/
        main.add(hbox);
    
        category.add(main, BorderLayout.WEST);
        return category;
        }    


    public void setFXPreset(int fx)
        {
        int fxtype = model.get("fx" + fx + "type");
        if (fxtype == 0) return;
        
        model.setUpdateListeners(false);
        // clear
        for(int pcl = 0; pcl < FX_PCL_LIST[FX_MAP[fxtype]].length; pcl++)
            {
            int paramnum = FX_PCL_LIST[FX_MAP[fxtype]][pcl][3];

            int index = FX_INDICES[FX_MAP[fxtype]][0];  // a default in case we can't find it
            for(int i = 1; i < FX_INDICES[FX_MAP[fxtype]].length; i++)
                {
                if (FX_INDICES[FX_MAP[fxtype]][i] == paramnum)
                    {
                    index = i;
                    break;
                    }
                }

            String key = "fx" + fx + "class" + FX_MAP[fxtype] + "param" + index;
            model.set(key, 0);
            }
        
        // set
        for(int pcl = 0; pcl < FX_PCL_LIST[FX_MAP[fxtype]].length; pcl++)
            {
            int value = FX_PRESETS[fxtype][pcl];  // note NOT FX_MAP
//              int bitlen = FX_PCL_LIST[FX_MAP[fxtype]][pcl][2];
            int paramnum = FX_PCL_LIST[FX_MAP[fxtype]][pcl][3];
            int parambit = FX_PCL_LIST[FX_MAP[fxtype]][pcl][4];
                
            int index = -1;  // a default in case we can't find it
            for(int i = 0; i < FX_INDICES[FX_MAP[fxtype]].length; i++)
                {
                if (FX_INDICES[FX_MAP[fxtype]][i] == paramnum)
                    {
                    index = i;
                    break;
                    }
                }
                
            if (index == -1)  // unused (typically says "reserved for future use")
                {
                System.err.println("Warning (KorgWavestationPerformance): PCL List entry not found: " + fxtype + ":" + paramnum + " (probably 'reserved').");
                }
            else
                {
                String key = "fx" + fx + "class" + FX_MAP[fxtype] + "param" + index;
                model.set(key, model.get(key) | (value << parambit));
                }
            }
        revise();
        model.setUpdateListeners(true);

        // update
        int[] indices = FX_INDICES[FX_MAP[fxtype]];
        for(int i = 0; i < indices.length; i++)
            {
            String key = "fx" + fx + "class" + FX_MAP[fxtype] + "param" + i;
            model.set(key, (model.get(key, 0)));
            }
        
        }
        
        
    public Object[] emitAll(String key)
        {
        simplePause(40);  // without this, we seem to drop some packets as we overfill the buffer
        
        if (key.equals("bank")) return new Object[0];  // this is not emittable
        if (key.equals("number")) return new Object[0];  // this is not emittable
//        if (key.equals("hillclimbdelay")) return new Object[0];  // this is not emittable
                
        /*
          byte[] bank_mesg = paramBytes(CURRENT_BANK, edisynToWSBank[model.get("bank", 0)]);
          byte[] num_mesg = paramBytes(CURRENT_PROG, model.get("number", 0));
        */
              
        if (key.equals("name"))
            {
            byte[] mesg = paramBytes(PROG_NAME, model.get(key, ""));
            return new byte[][] { /*bank_mesg, num_mesg,*/ mesg };
            }
        else if (key.startsWith("fx"))
            {
            int index;
            int val = model.get(key, 0);
                
            if (key.equals("fxseries"))
                index = FX_PLACEMENT;
            else if (key.equals("fxmix3amount"))
                index = FX_MIX_3;
            else if (key.equals("fxmix4amount"))
                index = FX_MIX_4;
            else if (key.equals("fxmod3source"))
                index = FX_MOD_3;
            else if (key.equals("fxmod4source"))
                index = FX_MOD_4;
            else if (key.equals("fxmod3amount"))
                index = FX_MOD_AMT_3;
            else if (key.equals("fxmod4amount"))
                index = FX_MOD_AMT_4;
            else if (key.endsWith("type"))
                {
                int part = extractNumbers(key)[0];
                byte[] mesg = paramBytes((part == 1 ? FX1_PROG : FX2_PROG), val);
                return new byte[][] { /*bank_mesg, num_mesg,*/ mesg };
                }
            else
                {
                int[] vals = extractNumbers(key);
                int part = vals[0];
                int mappedNum = vals[1];
                int param = vals[2];

                byte[] prog_mesg = paramBytes((part == 1 ? FX1_PROG : FX2_PROG), model.get("fx" + part + "type", 0));
                byte[] part_mesg = paramBytes(CURRENT_FX, part);
                byte[] mesg = paramBytes(FX_PARAMETERS[mappedNum][param], val);
                return new byte[][] { /*bank_mesg, num_mesg,*/ prog_mesg, part_mesg, mesg };
                }
            byte[] mesg = paramBytes(index, val);
            return new byte[][] { /* bank_mesg, num_mesg,*/ mesg};
            }
        else 
            {
            int part = extractNumbers(key)[0];
            byte[] part_mesg = paramBytes(CURRENT_PART, part);
                
            int index = 0;
            int val = model.get(key, 0);
                        
            if (key.endsWith("bank"))
                { 
                index = PART_PATCH_BANK;
                val = edisynToWSBank[model.get(key, 0)];        // ugh, banks are mixed up
                }
            else if (key.endsWith("number"))
                index = PART_PATCH_NUM;
            else if (key.endsWith("sustain"))
                index = PART_SUS_ENABLE;
            else if (key.endsWith("voicemode"))
                {
                index = PART_MODE;
                val = val + 1;  // modes are 1..3, not 0..2
                }
            else if (key.endsWith("keypriority"))
                index = PART_UNI_NOTE_PRIORITY;
            else if (key.endsWith("scale"))
                index = PART_MTUNE_TAB ;
            else if (key.endsWith("playmode"))
                {
                index = PART_PLAY_MODE;
                val = val + 1;  // modes are 1..3, not 0..2
                }
            else if (key.endsWith("scalekey"))
                index = PART_MTUNE_KEY ;
            else if (key.endsWith("volume"))
                index = PART_VOLUME;
            else if (key.endsWith("fxbus"))
                index = PART_OUTPUT ;
            else if (key.endsWith("delay"))
                index = PART_DELAY;
            else if (key.endsWith("transpose"))
                index = PART_TRANSPOSE;
            else if (key.endsWith("detune"))
                index = PART_DETUNE;
            else if (key.endsWith("lowkey"))
                index = PART_KEY_LIMIT_LOW;
            else if (key.endsWith("hikey"))
                index = PART_KEY_LIMIT_HIGH;
            else if (key.endsWith("lowvel"))
                index = PART_VEL_LIMIT_LOW;
            else if (key.endsWith("hivel"))
                index = PART_VEL_LIMIT_HIGH;
            else if (key.endsWith("outputchannel"))
                index = PART_MIDI_XMIT_CHAN;
            else if (key.endsWith("programnumber"))
                index = PART_PROG_CHANGE_XMIT;
            else if (key.equals("fx1"))
                index = FX1_PROG;
            else if (key.equals("fx2"))
                index = FX2_PROG;
            else if (key.equals("series"))
                index = FX_PLACEMENT;
            else
                System.err.println("Warning (KorgWavestationPerformance): Unknown Key " + key);
                        
            byte[] mesg = paramBytes(index, val);
            return new byte[][] { /*bank_mesg, num_mesg,*/ part_mesg, mesg };
            }
        }



    class Part
        {
        int bankNum;
        int patchNum;
        int level;
        int output;
        int partMode;
        int loKey;
        int hiKey;
        int loVel;
        int hiVel;
        int trans;
        int detune;
        int tuneTab;
        int microTuneKey;
        int midiOutChan;
        int midiProgNum;
        int susEnable;
        int delay;
        
        int read(byte[] data, int pos)
            {
            bankNum = readByte(data, pos);
            pos += 1;
            patchNum = readByte(data, pos);
            pos += 1;
            level = readUByte(data, pos);
            pos += 1;
            output = readByte(data, pos);
            pos += 1;
            partMode = readUByte(data, pos);
            pos += 1;
            loKey = readUByte(data, pos);
            pos += 1;
            hiKey = readUByte(data, pos);
            pos += 1;
            loVel = readUByte(data, pos);
            pos += 1;
            hiVel = readUByte(data, pos);
            pos += 1;
            trans = readByte(data, pos);
            pos += 1;
            detune = readByte(data, pos);
            pos += 1;
            tuneTab = readUByte(data, pos);
            pos += 1;
            microTuneKey = readUByte(data, pos);
            pos += 1;
            midiOutChan = readUByte(data, pos);
            pos += 1;
            midiProgNum = readByte(data, pos);
            pos += 1;
            susEnable = readByte(data, pos);
            pos += 1;
            delay = readUWord(data, pos);
            pos += 2;

            return pos;
            }
                
        int write(byte[] data, int pos)
            {
            writeByte(bankNum, data, pos);
            pos += 1;
            writeByte(patchNum, data, pos);
            pos += 1;
            writeUByte(level, data, pos);
            pos += 1;
            writeByte(output, data, pos);
            pos += 1;
            writeUByte(partMode, data, pos);
            pos += 1;
            writeUByte(loKey, data, pos);
            pos += 1;
            writeUByte(hiKey, data, pos);
            pos += 1;
            writeUByte(loVel, data, pos);
            pos += 1;
            writeUByte(hiVel, data, pos);
            pos += 1;
            writeByte(trans, data, pos);
            pos += 1;
            writeByte(detune, data, pos);
            pos += 1;
            writeUByte(tuneTab, data, pos);
            pos += 1;
            writeUByte(microTuneKey, data, pos);
            pos += 1;
            writeUByte(midiOutChan, data, pos);
            pos += 1;
            writeByte(midiProgNum, data, pos);
            pos += 1;
            writeByte(susEnable, data, pos);
            pos += 1;
            writeUWord(delay, data, pos);
            pos += 2;

            return pos;
            }
        }
    
    class Performance
        {
        char[] name = new char[16];
        byte[] fxPerfBlock = new byte[21];
        Part[] parts = new Part[8];
        
        public void setName(String val)
            {
            val = val + "                ";
            System.arraycopy(val.toCharArray(), 0, name, 0, name.length);
            }
        
        public Performance()
            {
            for(int i = 0; i < parts.length; i++)
                parts[i] = new Part();
            }

        int read(byte[] data, int pos)
            {
            for(int i = 0; i < 16; i++)
                name[i] = (char)data[i + pos];
            pos += 16;
            for(int i = 0; i < 21; i++)
                fxPerfBlock[i] = data[i + pos];
            pos += 21;
            for(int i = 0; i < 8; i++)
                pos = parts[i].read(data, pos);
            return pos;
            }

        int write(byte[] data, int pos)
            {
            for(int i = 0; i < 16; i++)
                data[i + pos] = (byte)name[i];
            pos += 16;
            for(int i = 0; i < 21; i++)
                data[i + pos] = fxPerfBlock[i];
            pos += 21;
            for(int i = 0; i < 8; i++)
                pos = parts[i].write(data, pos);
            return pos;
            }


        }

    
    public static final int NYBBLIZED_LENGTH = 181;

    public int parse(byte[] data, boolean fromFile)
        {
        if (data[4] == (byte)0x49)
            {
            model.set("bank", wsToEdisynBank[data[5]]);
            model.set("number", data[6]);
            return subparse(data, 7);
            }
        else
            {
            // extract names
            String[] n = new String[50];
            for(int i = 0; i < 50; i++)
                {
                // yuck, denybblize and extract the patch just to get the name...
                byte[] d = denybblize(data, i * NYBBLIZED_LENGTH * 2 + 6, NYBBLIZED_LENGTH * 2);
        
                Performance performance = new Performance();
                performance.read(d, 0);
        
                n[i] = new String(performance.name);
                } 
                
            // Now that we have an array of names, one per patch, we present the user with options;
            // 0. Cancel [handled automatically]
            // 1. Save the bank data [handled automatically]
            // 2. Upload the bank data [handled automatically] 
            // 3. Load and edit a certain patch number
            int patchNum = showBankSysexOptions(data, n);
            if (patchNum < 0) return PARSE_CANCELLED;
                
            model.set("bank", wsToEdisynBank[data[5]]);
            model.set("number", patchNum);

            // okay, we're loading and editing patch number patchNum.  Here we go.
            return subparse(data, patchNum * NYBBLIZED_LENGTH * 2 + 6);      
            }
        }
                

    public int subparse(byte[] data, int pos)
        {
        data = denybblize(data, pos);

        // The Wavestation effects documentation is woefully incomplete.  It is missing
        // critical information how the serial/parallel routing is embedded, and also
        // how the FX type for each FX slot is embedded.  Below is my best guess.  The
        // 21 bytes of the effects block are divided into a chunk of 5 bytes, then two
        // chunks of 8 bytes.  The first chunk defines global information, and the
        // remaining chunks define information for each effect.
        //
        // +-BYTE 4-+-BYTE 3-+-BYTE 2-+-BYTE 1-+-BYTE 0-+
        // |........|........|....?.?.|Z.YYYYYY|S.XXXXXX|
        // +76543210+76543210+76543210+76543210+76543210+
        // 
        // +-BYTE 12+-BYTE 11+-BYTE 10+-BYTE 9-+-BYTE 8-+-BYTE 7-+-BYTE 6-+-BYTE 5-+
        // |........|........|........|........|........|........|........|....AAAA|
        // +76543210+76543210+76543210+76543210+76543210+76543210+76543210+76543210+
        // 
        // +-BYTE 20+-BYTE 19+-BYTE 18+-BYTE 17+-BYTE 16+-BYTE 15+-BYTE 14+-BYTE 13+
        // |........|........|........|........|........|........|........|....BBBB|
        // +76543210+76543210+76543210+76543210+76543210+76543210+76543210+76543210+            
        //
        // DEFINING EFFECTS
        // The Wavestation can either be in STANDARD EFFECT MODE, or in EXTENDED EFFECT
        // MODE.  This global mode affects BOTH FX slots.  If both FX slots are "standard"
        // effects, then we can use standard effect mode.  If either FX slot is an "extended"
        // effect, then we must use the extended effect slot.  The proper strategy is to
        // be able to read either standard or extended effect mode, but only write in
        // extended effect mode.
        //
        // STANDARD EFFECT MODE
        // In standard effect mode, the bytes marked XXXXXX in the diagram below define
        // the type of FX slot 1, and the bytes marked YYYYYY define the type of FX slot 2.
        // The slot marked Z is set to 0.  Effect values start with 2 ("No effect").
        //
        // EXTENDED EFFECT MODE
        // In extended effect mode, the slot marked Z is set to 1.
        // If you have a standard effect in FX slot 1, it is
        // specified in XXXXX and AAAA ought to be set to 0 [though it's only important, I
        // believe, to set AAAA to 0 to if your effect is No Effect].   Similarly if you
        // have a standard effect in FX slot 2, then it's found in YYYYYY, and BBBB is 0.
        // Effect values start with 2 ("No effect").
        //
        // If you have an EXTENDED effect in FX slot 1, then XXXXXX is set to 2 (same as
        // "No Effect", and AAAA defines the effect number (0 = No Effect, 1 = Stereo Mod
        // Pitch Shifter, ..., 8 = Stereo Vocoder).  Similarly, if you have an extended
        // effect in FX slot 2, then YYYYYY is set to 2, and BBBB defines the effect number.
        //
        // Note that the Wavestation docs give a dire warning that if you set the effect number
        // to something out of bounds, the Wavestation will crash.
        //
        // SERIES AND PARALLEL
        //
        // I think that Series is specified by setting bit S to 1, and Parallel sets it to 0.
        //
        // However when in Series mode, I note that the bits marked ? are *also* set to 1,
        // even though the documentation say that these two bits are reserved for the mix 3
        // level.  I believe that is because in Series, Mix 3's levels are 0=OFF, 1=Dry, 
        // ... 9/1 ... 1/9, WET=11 (default).  But in Parallel, Mix 3's levels are 0=OFF,
        // 1 = LEFT (default), 9/1...1/9, 11=RIGHT.  So when changing from parallel to Series,
        // the Wavestation is resetting the default (from 1 to 11 -- a change of 10, hence
        // bits 1 and 3 being set).  This doesn't happen in Mix 4 because in Parallel, 
        // Mix 4's default is RIGHT, which is the same value (11) as WET.
                   
        Performance performance = new Performance();
        performance.read(data, 0);        

        model.set("name", new String(performance.name));
                
        // parse fx
                
        model.set("fxmix3amount", performance.fxPerfBlock[2] & 15);
        model.set("fxmod3source", performance.fxPerfBlock[3] & 15);
        // the sign is at bit 6.  We push to bit 7, then rotate (>>, not >>>) to slot 4 to do a signed extension.
        model.set("fxmod3amount", (performance.fxPerfBlock[4] & 15) | (((performance.fxPerfBlock[0] << 1) & 128) >> 3));
        model.set("fxmix4amount", ((performance.fxPerfBlock[2] & 0xFF) >>> 4) & 15);
        model.set("fxmod4source", ((performance.fxPerfBlock[3] & 0xFF) >>> 4) & 15);
        // the sign is at bit 6.  We push to bit 7, then rotate (>>, not >>>) to slot 4 to do a signed extension.
        model.set("fxmod4amount", (((performance.fxPerfBlock[4] & 0xFF) >>> 4) & 15) | ((((performance.fxPerfBlock[1] & 0xFF) << 1) & 128) >> 3));
                                
        model.set("fxseries", (performance.fxPerfBlock[0] & 0xFF) >>> 7);

                
        boolean extendedEffects = (((performance.fxPerfBlock[1] & 0xFF) >>> 7) == 1);                       
                                
        for(int fx = 1; fx <= 2; fx++)
            {
            int fxtype = (performance.fxPerfBlock[fx - 1] & 63) - 2;
                                                
            // if we're in extended mode and we have a NO EFFECT and the extended list is > 0
            if (extendedEffects && fxtype == 0 && ((performance.fxPerfBlock[(fx == 1 ? 5 : 13)] & 15) > 0))
                fxtype = 47 + (performance.fxPerfBlock[(fx == 1 ? 5 : 13)] & 15);  // mod pitch shift (1) is effect 48

            model.set("fx" + fx + "type", fxtype);
                        
            if (fxtype == 0) // NO_EFFECT
                continue;
                
            model.setUpdateListeners(false);
                        
            // First we zero out
            for(int pcl = 0; pcl < FX_PCL_LIST[FX_MAP[fxtype]].length; pcl++)
                {
                int paramnum = FX_PCL_LIST[FX_MAP[fxtype]][pcl][3];
                int index = FX_INDICES[FX_MAP[fxtype]][0];  // a default in case we can't find it
                for(int i = 1; i < FX_INDICES[FX_MAP[fxtype]].length; i++)
                    {
                    if (FX_INDICES[FX_MAP[fxtype]][i] == paramnum)
                        {
                        index = i;
                        break;
                        }
                    }
                String key = "fx" + fx + "class" + FX_MAP[fxtype] + "param" + index;
                model.set(key, 0);
                }

            // Now we load
            for(int pcl = 0; pcl < FX_PCL_LIST[FX_MAP[fxtype]].length; pcl++)
                {
                int bytenum = FX_PCL_LIST[FX_MAP[fxtype]][pcl][0];
                int bit = FX_PCL_LIST[FX_MAP[fxtype]][pcl][1];
                int bitlen = FX_PCL_LIST[FX_MAP[fxtype]][pcl][2];
                int paramnum = FX_PCL_LIST[FX_MAP[fxtype]][pcl][3];
                int parambit = FX_PCL_LIST[FX_MAP[fxtype]][pcl][4];

                int index = -1;
                for(int i = 0; i < FX_INDICES[FX_MAP[fxtype]].length; i++)
                    {
                    if (FX_INDICES[FX_MAP[fxtype]][i] == paramnum)
                        {
                        index = i;
                        break;
                        }
                    }
                                
                if (index == -1)  // unused (typically says "reserved for future use")
                    {
                    System.err.println("Warning (KorgWavestationPerformance) 2: PCL List entry not found: " + fxtype + ":" + paramnum + " (probably reserved').");
                    }
                else
                    {
                    // push to the bottom of the byte
                    int top = ((performance.fxPerfBlock[bytenum + 8 * (fx - 1) + 5] & 0xFF) >>> bit);
                    // now push to the top of the byte
                    top = ((top << (8 - Math.abs(bitlen))) & 0xFF);

                    // now push back to the bottom of the byte.  This dance fills everything above with zeros or sign extends
                    if (bitlen < 0)
                        top = (((byte)top) >> (8 - Math.abs(bitlen)));          // notice the >> and +.  The (byte) forces sign extension of the byte.
                    else
                        top = (top >>> (8 - Math.abs(bitlen)));         // here we don't WANT a (byte)
                                
                    // Now we're ready to position the data
                    String key = "fx" + fx + "class" + FX_MAP[fxtype] + "param" + index;

                    int fxVal = model.get(key);
                    fxVal = fxVal | (top << parambit);
                                
                    // Now set the data again
                    model.set(key, fxVal);
                    }
                }

            model.setUpdateListeners(true);
                        
            // finally update all the listeners

            int[] indices = FX_INDICES[FX_MAP[fxtype]];
            for(int i = 0; i < indices.length; i++)
                {
                String key = "fx" + fx + "class" + FX_MAP[fxtype] + "param" + i;
                model.set(key, (model.get(key, 0)));
                }

            }
                
                
        // parse parts
                
        for(int i = 0; i < 8; i++)
            {
            String part = "part" + (i + 1);
                        
            // If the part is unused, the bank number is negative.  We set it to a simple value here.
                                                
            if (performance.parts[i].bankNum < 0 || performance.parts[i].bankNum > 4)
                model.set(part + "bank", 0);
            else
                model.set(part + "bank", wsBankExpToEdisynBank[((performance.parts[i].partMode >>> 6) & 3) * 4 + performance.parts[i].bankNum]);
                                
            model.set(part + "playmode", ((performance.parts[i].partMode >>> 4) & 3) - 1);
            model.set(part + "voicemode", ((performance.parts[i].partMode >>> 2) & 3) - 1);
            model.set(part + "keypriority", (performance.parts[i].partMode) & 3);  // note NOT - 1
                        
            // patch number is -1 when unset
            model.set(part + "number", performance.parts[i].patchNum);
            model.set(part + "volume", performance.parts[i].level);
                        
            // This is wrong in the doumentation, it's actually the FX Bus
            model.set(part + "fxbus", performance.parts[i].output);
            model.set(part + "lowkey", performance.parts[i].loKey);
            model.set(part + "hikey", performance.parts[i].hiKey);
            model.set(part + "lowvel", performance.parts[i].loVel);
            model.set(part + "hivel", performance.parts[i].hiVel);
            model.set(part + "transpose", performance.parts[i].trans);
            model.set(part + "detune", performance.parts[i].detune);
            model.set(part + "scale", performance.parts[i].tuneTab);
            model.set(part + "scalekey", performance.parts[i].microTuneKey + 1);  // FIXME -- is this right?
//                      model.set(part + "??????", performance.parts[i].midiOutChan);
//                      model.set(part + "??????", performance.parts[i].midiProgNum);
            model.set(part + "sustain", performance.parts[i].susEnable);
            model.set(part + "delay", performance.parts[i].delay);
            }

        return PARSE_SUCCEEDED;     
        }
    
/*
  public boolean getSendsAllParametersInBulk()
  {
  return sendWavestationParametersInBulk;
  }
*/
    public Object[] emitAll(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        byte[] d = new byte[EXPECTED_SYSEX_LENGTH];
        d[0] = (byte)0xF0;
        d[1] = (byte)0x42;
        d[2] = (byte)(48 + getChannelOut());
        d[3] = (byte)0x28;
        d[4] = (byte)0x49;
        d[5] = (byte)edisynToWSBank[model.get("bank")];
        d[6] = (byte)model.get("number");
       
        Performance performance = new Performance();

        performance.setName(model.get("name", "Untitled"));
                
        /// parse fx
        ///
        ///
        /// BE SURE TO READ THE COMMENTS IN parse(...)
        ///
        ///

        performance.fxPerfBlock[2] = (byte)(performance.fxPerfBlock[2] | model.get("fxmix3amount"));
        performance.fxPerfBlock[3] = (byte)(performance.fxPerfBlock[3] | model.get("fxmod3source"));
        performance.fxPerfBlock[4] = (byte)(performance.fxPerfBlock[4] | (model.get("fxmod3amount") & 15));
        performance.fxPerfBlock[0] = (byte)(performance.fxPerfBlock[0] | ((model.get("fxmod3amount") & 128) >> 1));  // this should be >>, not >>>: sign goes in slot 6

        performance.fxPerfBlock[2] = (byte)(performance.fxPerfBlock[2] | (model.get("fxmix4amount") << 4));
        performance.fxPerfBlock[3] = (byte)(performance.fxPerfBlock[3] | (model.get("fxmod4source") << 4));
        performance.fxPerfBlock[4] = (byte)(performance.fxPerfBlock[4] | ((model.get("fxmod4amount") & 15) << 4));
        performance.fxPerfBlock[1] = (byte)(performance.fxPerfBlock[1] | ((model.get("fxmod4amount") & 128) >> 1));  // this should be >>, not >>>: sign goes in slot 6
                
        performance.fxPerfBlock[0] = (byte)(performance.fxPerfBlock[0] | (model.get("fxseries") << 7));         
                
        // always set extended effects
        performance.fxPerfBlock[1] = (byte)(performance.fxPerfBlock[1] | 128);          
                                
        for(int fx = 1; fx <= 2; fx++)
            {
            int fxtype = model.get("fx" + fx + "type");
                        
            if (fxtype >= 48)  // extended effect
                {
                performance.fxPerfBlock[(fx == 1 ? 5 : 13)] = (byte)(performance.fxPerfBlock[(fx == 1 ? 5 : 13)] | (fxtype + 2 - 47));
                }
            else
                {
                // NO_EFFECT will get 0 in its extended area anyway
                performance.fxPerfBlock[fx - 1] = (byte)(performance.fxPerfBlock[fx - 1] | (fxtype + 2));
                }

            if (fxtype != 0)
                {
                // Now we load
                for(int pcl = 0; pcl < FX_PCL_LIST[FX_MAP[fxtype]].length; pcl++)
                    {
                    int bytenum = FX_PCL_LIST[FX_MAP[fxtype]][pcl][0];
                    int bit = FX_PCL_LIST[FX_MAP[fxtype]][pcl][1];
                    int bitlen = FX_PCL_LIST[FX_MAP[fxtype]][pcl][2];
                    int paramnum = FX_PCL_LIST[FX_MAP[fxtype]][pcl][3];
                    int parambit = FX_PCL_LIST[FX_MAP[fxtype]][pcl][4];

                    int index = FX_INDICES[FX_MAP[fxtype]][0];  // a default in case we can't find it
                    for(int i = 1; i < FX_INDICES[FX_MAP[fxtype]].length; i++)
                        {
                        if (FX_INDICES[FX_MAP[fxtype]][i] == paramnum)
                            {
                            index = i;
                            break;
                            }
                        }
                                                
                    if (index == -1)  // unused (typically says "reserved for future use")
                        {
                        System.err.println("Warning (KorgWavestationPerformance): PCL List entry not found: " + fxtype + ":" + paramnum + " (probably 'reserved').");
                        continue;
                        }
                                                
                    String key = "fx" + fx + "class" + FX_MAP[fxtype] + "param" + index;

                    int top = model.get(key);
                                                                
                    // push to the bottom of the byte
                    top  = ((top & 0xFF) >>> parambit);
                    // now push to the top of the byte
                    top = ((top << (8 - Math.abs(bitlen))) & 0xFF);
                    // Now push back to the bottom of the byte
                    top = (top >>> (8 - Math.abs(bitlen)));
                                                
                    // Now we're ready to position the data

                    performance.fxPerfBlock[bytenum + 8 * (fx - 1) + 5] = 
                        (byte)(performance.fxPerfBlock[bytenum + 8 * (fx - 1) + 5] | ((top << bit) & 0xFF));
                    }
                }
            }
                
        // emit parts
                
        for(int i = 0; i < 8; i++)
            {
            String part = "part" + (i + 1);
                        
            int bank = edisynToWSBankExpBank[model.get(part + "bank")];
            performance.parts[i].bankNum = bank % 4;
            performance.parts[i].partMode = (((bank / 4) & 3) << 6) | 
                ((model.get(part + "playmode") + 1) << 4) |
                ((model.get(part + "voicemode") + 1) << 2) |
                (model.get(part + "keypriority"));
                                                                                        
            performance.parts[i].patchNum = model.get(part + "number");
            performance.parts[i].level = model.get(part + "volume");

            // This is wrong in the doumentation, it's actually the FX Bus
            performance.parts[i].output = model.get(part + "fxbus");
            performance.parts[i].loKey = model.get(part + "lowkey");
            performance.parts[i].hiKey = model.get(part + "hikey");
            performance.parts[i].loVel = model.get(part + "lowvel");
            performance.parts[i].hiVel = model.get(part + "hivel");
            performance.parts[i].trans = model.get(part + "transpose");
            performance.parts[i].detune = model.get(part + "detune");
            performance.parts[i].tuneTab = model.get(part + "scale");
            performance.parts[i].microTuneKey = model.get(part + "scalekey") - 1;
//                      performance.parts[i].midiOutChan = model.get(part + "??????");
//                      performance.parts[i].midiProgNum = model.get(part + "??????");
            performance.parts[i].susEnable = model.get(part + "sustain");
            performance.parts[i].delay = model.get(part + "delay");
            }

        byte[] data = new byte[NYBBLIZED_LENGTH];
        performance.write(data, 0);
        data = nybblize(data);
        System.arraycopy(data, 0, d, 7, data.length);
        int checksum = 0;
        for(int i = 0; i < data.length; i++)
            checksum += data[i];
        checksum = (checksum & 127);
        d[d.length - 2] = (byte)checksum;
        d[d.length - 1] = (byte)0xF7;
           
        if (toFile || toWorkingMemory)
            return new Object[] { d };
        else
            {
            // we'll attempt a write.  Assuming we did a PC first, then we might be okay because it does a switch to mulltiset and back to performance, see the developer FAQ about writing performances
            return new Object[] { d, paramBytes(EXECUTE_WRITE, 1) };
            }
        }

        
    public int getPauseAfterChangePatch() { return 300; }  // looks like 300 is about the minimum, else we get a sysex checksum error on the next sysex dump, probably because bytes were dropped

    public void changePatch(Model tempModel)
        {
        // we need to do this in order to be able to write.  See Developer FAQ
        byte[] midi_mesg = paramBytes(MIDI_MODE, MULTISET_MIDI_MODE);
        tryToSendSysex(midi_mesg);
        
        // this too.
        byte[] midi_mesg_2 = paramBytes(MIDI_MODE, PERFORMANCE_MIDI_MODE);
        tryToSendSysex(midi_mesg_2);

        // change the bank
        try {
            tryToSendMIDI(new ShortMessage(ShortMessage.CONTROL_CHANGE, getChannelOut(), 0,  MIDI_BANKS[tempModel.get("bank", 0)]));
            }
        catch (Exception e) { e.printStackTrace(); }

        // change the number
        try {
            tryToSendMIDI(new ShortMessage(ShortMessage.PROGRAM_CHANGE, getChannelOut(), tempModel.get("number", 0) + MIDI_PROG_CHANGE_OFFSETS[tempModel.get("bank", 0)], 0));
            }
        catch (Exception e) { e.printStackTrace(); }

        // specify that we're editing said bank
        byte[] bank_mesg = paramBytes(CURRENT_BANK, edisynToWSBank[tempModel.get("bank", 0)]);
        tryToSendSysex(bank_mesg);

        // specify that we're editing said number
        byte[] prog_mesg = paramBytes(CURRENT_PROG, tempModel.get("number", 0));
        tryToSendSysex(prog_mesg);
        }

    public byte[] requestDump(Model tempModel)
        {
        byte BB = (byte)edisynToWSBank[tempModel.get("bank")];
        byte NN = (byte)tempModel.get("number");
        return new byte[] { (byte)0xF0, (byte)0x42, (byte)(48 + getChannelOut()), (byte)0x28, (byte)0x19, BB, NN, (byte)0xF7 };
        }
    
    
    public static final int EXPECTED_SYSEX_LENGTH = 371;
    public static boolean recognize(byte[] data)
        {
        return ((data.length == EXPECTED_SYSEX_LENGTH &&
                data[0] == (byte)0xF0 &&
                data[1] == (byte)0x42 &&
                data[3] == (byte)0x28 &&
                data[4] == (byte)0x49)
            
            || recognizeBulk(data));              
        }
    
    public static boolean recognizeBulk(byte[] data)
        {
        return ((data.length == 18108 &&
                data[0] == (byte)0xF0 &&
                data[1] == (byte)0x42 &&
                data[3] == (byte)0x28 &&
                data[4] == (byte)0x4D));
            
        }    
    
    /////// OTHER ABSTRACT METHODS
    
    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        JComboBox bank = new JComboBox(BANKS);
        bank.setEditable(false);
        bank.setMaximumRowCount(32);
        bank.setSelectedIndex(model.get("bank"));
                
        JTextField number = new JTextField("" + (model.get("number")), 3);
                
        while(true)
            {
            boolean result = showMultiOption(this, new String[] { "Bank", "Patch Number"}, 
                new JComponent[] { bank, number }, title, "Enter the Bank and Patch number.");
                
            if (result == false) 
                return false;
                                
            int n;
            try { n = Integer.parseInt(number.getText()); }
            catch (NumberFormatException e)
                {
                showSimpleError(title, "The Performance Number must be an integer 0 ... 49");
                continue;
                }
            if (n < 0 || n > 49)
                {
                showSimpleError(title, "The Performance Number must be an integer 0 ... 49");
                continue;
                }
                                
            change.set("bank", bank.getSelectedIndex());
            change.set("number", n);
                        
            return true;
            }
        }
        

    public void revise()
        {
        // check the easy stuff -- out of range parameters
        super.revise();

        String nm = model.get("name", "Init");
        String newnm = revisePatchName(nm);
        if (!nm.equals(newnm))
            model.set("name", newnm);
        }
        
    public static String getSynthName() { return "Korg Wavestation SR [Performance]"; }
    
    public String getPatchName(Model model) { return model.get("name", "Init"); }
    

    public Model getNextPatchLocation(Model model)
        {
        int bank = model.get("bank");
        int number = model.get("number");
        
        number++;
        if (number >= 49)
            {
            bank++;
            number = 0;
            if (bank >= 12)
                bank = 0;
            }
                
        Model newModel = buildModel();
        newModel.set("bank", bank);
        newModel.set("number", number);
        return newModel;
        }

    public String getPatchLocationName(Model model)
        {
        // getPatchLocationName() is called from sprout() as a test to see if we should enable
        // batch downloading.  If we haven't yet created an .init file, then parameters won't exist
        // yet and this method will bomb badly.  So we return null in this case.
        if (!model.exists("number")) return null;
        if (!model.exists("bank")) return null;
        
        int number = model.get("number");
        return BANKS[model.get("bank")] + " " + (number > 9 ? "" : "0") + number;
        }

    public int getPauseBetweenHillClimbPlays()
        {
//      return model.get("hillclimbdelay");
        return 1100;
        }
    }
    
