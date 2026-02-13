/***
    Copyright 2026 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.kawaik5000;

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
import edisyn.util.*;

/**
   A patch editor for the Kawai K5000S and K5000R, compatible with the K5000W (Bank B not supported).
   
   The K5000, like the K5, does not support Send-To-Current-Patch.  Ordinarily that would not be a 
   problem, but it is for the K5000.  A huge problem.  We could use a scratch patch like we do in the K5, 
   except that the K5000 has Flash RAM rather than battery-backed RAM, so if we did that we'd burn out
   the Flash.  Alternatively we could send each parameter separately in its own send-parameter sysex
   message (which the K5000 does support).  But there are upwards of 6000 parameters!  That's about 25
   seconds worth of sysex messages.  I have no solution to this: we have to turn off many Edisyn features
   and the user has to decide whether or not to send a patch manually via a scratch patch or individual
   parameters.
   
   The K5000 has bank sysex messages as well (why?!? given their massive size).  We do not suppor them
   yet but they should be easy to set up.
   
   The K5000 has special files called FOO.KA1 (for individual patches) and FOO.KAA (for bank patches).
   We do not support them at this time but may later if we can figure out how they work.
        
   @author Sean Luke
*/

public class KawaiK5000 extends Synth
    {
    
    // Pictures of the effect algorithms
    public static final ImageIcon[] EFFECT_ALGORITHM_ICONS = 
        {
        new ImageIcon(KawaiK5000.class.getResource("Algorithm1.png")),
        new ImageIcon(KawaiK5000.class.getResource("Algorithm2.png")),
        new ImageIcon(KawaiK5000.class.getResource("Algorithm3.png")),
        new ImageIcon(KawaiK5000.class.getResource("Algorithm4.png")),
        };

    // The Patch Name can be no longer than 8
    public static final int MAXIMUM_NAME_LENGTH = 8;
    // Various tables below have a "none" slot
    public static final int NONE = -1;                                      // indicates "nothing" in effects lists
        
    // Basic parameter struings
    public static final String[] KEYS = { "A", "Bb", "B", "C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab" };
    public static final String[] CENTS = { "0 cent", "25 cent", "33 cent", "50 cent" };
    public static final String[] BANKS = { "A", "D", "E", "F" };
    public static final String[] POLY_MODES = { "Polyphonic", "Solo 1", "Solo 2" };
    public static final String[] HARMONIC_GROUPS = { "Low", "High" };
    public static final String[] FORMANT_MODULATIONS = { "Env", "LFO" };
    public static final String[] HARMONIC_LFO_WAVES = { "Triangle", "Sawtooth", "Random" };
    public static final String[] ENV_LOOPS = { "Off", "LP1", "LP2" };
    public static final String[] VELO_SW = { "Off", "Low", "High" };
    public static final String[] PAN_TYPES = { "Normal", "KS", "-KS", "Random" };
    public static final String[] DCF_MODES = { "Low Pass", "High Pass" }; 
    public static final String[] SOURCE_LFO_WAVES = { "Triangle", "Square", "Sawtooth", "Sine", "Random" };
    public static final String[] SOURCE_TYPES = { "PCM", "Additive                                   " };                   // these extra spaces prevent movement when changing from PCM to Additive

    // Names of the 16 dials on the K5000s
    public static final String[] DIALS = { 
        "Hrm Lo", "FF Bias", "Cutoff", "Attack", "Hrm Hi", "FF Speed", "Reso", "Decay", "Even/Odd", "FF Depth", "Velocity", "Release", "User 1", "User 2", "User 3", "User 4" };
        
    // CCs that the 16 dials on the K5000s
    public static final int[] DIAL_CCS = { 0x10, 0x12, 0x4A, 0x49, 0x11, 0x13, 0x4D, 0x4E, 0x47, 0x4B, 0x4C, 0x48, 0x50, 0x51, 0x52, 0x53 };
        
    // Number of effects
    public static final int NUM_EFFECTS = 4;
    // Number of parameters per effect
    public static final int NUM_EFFECT_PARAMETERS = 5;

    // Names of effect parameters.  An underscore _ indicates a suggested non-breaking space
    public static final String[][] EFFECT_PARAMETER_NAMES = 
        {
        { "Dry/Wet", "Dry/Wet", "Delay Level", "Delay Level", "Delay Level", "Delay Level", " Delay Level", "Delay Level", "Dry/Wet", "Dry/Wet", "Dry/Wet", "Dry/Wet", "Dry/Wet", "Dry/Wet", "Dry/Wet", "Dry/Wet", "Dry/Wet", "Dry/Wet", "Dry/Wet", "Dry/Wet", "Dry/Wet", "Dry/Wet", "Dry/Wet", "Dry/Wet", "Dry/Wet", "Dry/Wet", "Dry/Wet", "Dry/Wet", "Dry/Wet", "Dry/Wet", "Dry/Wet", "Dry/Wet", "Dry/Wet", "Dry/Wet", "Dry/Wet", "Dry/Wet", "Dry/Wet" },
        { "Slope", "Slope", "Delay Time_1", "Delay Time_1", "", "Delay Time_L", "", "", "Speed", "Speed", "Speed", "Speed", "Speed", "Speed", "Speed", "Speed", "Speed", "Speed", "", "", "Speed", "Speed", "Speed", "Speed", "Speed", "Speed", "Speed", "Speed", "Slow Speed", "Sense", "Center Frequency", "EQ_Low", "EQ_Low", "EQ_Low", "EQ_Low", "EQ_Low", "EQ_Low" },
        { "", "", "Tap Level", "Tap Level", "Delay Fine", "Feedback_L", "", "", "Depth", "Depth", "Depth", "Depth", "Depth", "Depth", "Depth", "Depth", "Depth", "Depth", "Depth", "Depth", "Depth", "Depth", "Depth", "Depth", "Depth", "Depth", "Depth", "Depth", "Fast Speed", "Frequency Bottom", "Bandwidth", "EQ_High", "EQ_High", "EQ_High", "EQ_High", "EQ_High", "EQ_High" },
        { "Predelay Time", "Predelay Time", "Delay Time_2", "Delay Time_2", "Delay Coarse", "Delay Time_R", "Delay Time", "Delay Time", "Predelay Time", "Delay Time", "Predelay Time", "Predelay Time", "Delay Time", "Delay Time", "Predelay Time", "Predelay Time", "Delay Time", "Delay Time", "Predelay Time", "Delay Time", "Predelay Time", "Delay Time", "Predelay Time", "Delay Time", "Predelay Time", "Predelay Time", "Delay Time", "Delay Time", "Accel", "Frequency Top", "", "", "", "Output Level", "Output Level", "Delay Time", "Delay Time" },
        { "Feedback", "Feedback", "Feedback", "Feedback", "Feedback", "Feedback_R", " Feedback", "Feedback", "Wave", "Wave", "Wave", "Wave", "Wave", "Wave", "Feedback", "Feedback", "Feedback", "Feedback", "", "", "", "", "Wave", "Wave", "Feedback", "Feedback", "Feedback", "Feedback", "Slow/Fast", "Resonance", "", "Intensity", "Intensity", "Drive", "Drive", "Drive", "Drive" },
        };

    // Units used in the effect paramters
    public static final String[][] EFFECT_PARAMETER_UNITS = 
        {
        { "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "" },
        { "", "", "ms", "ms", "", "ms", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "dB", "dB", "dB", "dB", "dB", "dB" },
        { "", "", "", "", "ms", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "dB", "dB", "dB", "dB", "dB", "dB" },
        { "sec", "sec", "ms", "ms", "ms", "ms", "ms", "ms", "ms", "ms", "ms", "ms", "ms", "ms", "ms", "ms", "ms", "ms", "ms", "ms", "ms", "ms", "ms", "ms", "ms", "ms", "ms", "ms", "", "", "", "", "", "", "", "ms", "ms" },
        { "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "" },
        };

    // Default values for the effect parameters.  Presently not using this.
    public static final int[][] EFFECT_PARAMETER_DEFAULTS = 
        {
        { 30, 60, 100, 100, 25, 35, 40, 50, 24, 24, 50, 60, 0, 0, 52, 50, 52, 50, 35, 35, 50, 50, 30, 30, 10, 60, 30, 80, 15, 0, 0, 0, 0, 0, 0, 0, 0 },
        { 1, 19, 56, 56, NONE, 56, NONE, NONE, 14, 14, 6, 6, 6, 6, 9, 6, 9, 6, NONE, NONE, 6, 6, 53, 53, 5, 4, 6, 7, 8, 60, 50, 0, 0, 0, 0, 0, 0 },
        { NONE, NONE, 90, 90, 0, 60, NONE, NONE, 91, 91, 76, 76, 75, 75, 54, 75, 54, 75, 75, 75, 75, 75, 49, 48, 93, 75, 75, 100, 71, 30, 50, 0, 0, 0, 0, 0, 0 },
        { 5, 36, 66, 81, 51, 77, 81, 71, 32, 150, 28, 28, 150, 150, 0, 0, 100, 60, 40, 200, 80, 200, 0, 170, 0, 80, 200, 200, 5, 80, NONE, NONE, NONE, 50, 50, 150, 150 },
        { 10, 50, 50, 50, 50, 33, 55, 60, 0, 0, 0, 0, 0, 0, 46, 50, 46, 50, NONE, NONE, NONE, NONE, 0, 0, 80, 80, 80, 50, 1, 50, NONE, 50, 50, 50, 50, 50, 50 }
        };

    // Min values for the effect parameters
    public static final int[][] EFFECT_PARAMETER_MINS = 
        {
        { 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
        { 0, 0, 0, 0, NONE, 0, NONE, NONE, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, NONE, NONE, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 52, 52, 52, 52, 52, 52 },
        { NONE, NONE, 1, 1, 0, 1, NONE, NONE, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 20, 1, 1, 52, 52, 52, 52, 52, 52 },
        { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, NONE, NONE, NONE, 1, 1, 0, 0 },
        { 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, NONE, NONE, NONE, NONE, 0, 0, 1, 1, 1, 1, 0, 1, NONE, 1, 1, 1, 1, 1, 1 }
        };

    // Max values for the effect parameters
    public static final int[][] EFFECT_PARAMETER_MAXES = 
        {
        { 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100 },
        { 100, 100, 720, 720, NONE, 720, NONE, NONE, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, NONE, NONE, 100, 100, 100, 100, 100, 100, 100, 100, 20, 100, 100, 76, 76, 76, 76, 76, 76 },
        { NONE, NONE, 100, 100, 9, 100, NONE, NONE, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 76, 76, 76, 76, 76, 76 },
        { 100, 100, 720, 720, 1270, 720, 720, 720, 100, 200, 100, 200, 200, 200, 100, 100, 200, 200, 100, 200, 100, 200, 100, 200, 100, 100, 200, 200, 10, 100, NONE, NONE, NONE, 100, 100, 200, 200 },
        { 100, 100, 100, 100, 100, 100, 100, 100, 1, 1, 1, 1, 1, 100, 100, 100, 100, 100, NONE, NONE, NONE, NONE, 1, 1, 100, 100, 100, 100, 1, 100, NONE, 100, 100, 100, 100, 100, 100 }
        };

    // Types of reverbs
    public static final String[] REVERB_TYPES = new String[]
    {
    "Hall 1 (Standard)",
    "Hall 2 (Small)",
    "Hall 3 (Bright)",
    "Room 1 (Standard)",
    "Room 2 (Large)",
    "Room 3 (Bright)",
    "Plate 1 (Large)",
    "Plate 2 (Small)",
    "Plate 3 (Mellow)",
    "Reverse",              // "No Reverb" in the sysex docs, dunno what that means
    "Long Delay",               // "No Reverb" in the sysex docs, dunno what that means
    };
                
    // Types of Effects
    public static final String[] EFFECT_TYPES = new String[]
    {
    "Early Reflections 1",
    "Early Reflections 2",
    "Tap Delay 1",
    "Tap Delay 2",
    "Single Delay",
    "Dual Delay",
    "Stereo Delay",
    "Cross Delay",
    "Auto Pan",
    "Auto Pan and Delay",
    "Chorus 1",
    "Chorus 2",
    "Chorus 1 and Delay",
    "Chorus 2 and Delay",
    "Flanger 1",
    "Flanger 2",
    "Flanger 1 and Delay",
    "Flanger 2 and Delay",
    "Ensemble",
    "Ensemble and Delay",
    "Celeste",
    "Celeste and Delay",
    "Tremelo",
    "Tremelo and Delay",
    "Phaser 1",
    "Phaser 2",
    "Phaser 1 and Delay",
    "Phaser 2 and Delay",
    "Rotary",
    "Auto Wah",
    "Bandpass Filter",
    "Exciter",
    "Enhancer",
    "Overdrive",
    "Distortion",
    "Overdrive and Delay",
    "Distortion and Delay"
    };
        
    // Things the panel and foot switches can affect
    public static final String[] SWITCHES = new String[]
    {
    "Off",
    "Harm Max",
    "Harm Bright",
    "Harm Dark",
    "Harm Saw",
    "Select Loud",
    "Add Loud",
    "Add 5th",
    "Add Odd",
    "Add Even",
    "HE #1",
    "HE #2",
    "HE Loop",
    "FF Max",
    "FF Comb",
    "FF Hi Cut",
    "FF Comb 2"
    };

    // Macro Destinations
    public static final String[] MACRO_DESTINATIONS = new String[]
    {
    "Pitch Offset",
    "Cutoff Offset",
    "Level",
    "Vibrato Depth Offset",
    "Growl Depth Offset",
    "Tremelo Depth Offset",
    "LFO Speed Offset",
    "Attack Time Offset", 
    "Decay 1 Time Offset",
    "Release Time Offset",
    "Velocity Offset", 
    "Resonance Offset",
    "Panpot Offset",
    "FF Bias Offset",
    "FF Env/LFO Depth Off.", // set",
    "FF Env/LFO Speed Off.", // set",
    "Harmonic Lo Offset",
    "Harmonic Hi Offset",
    "Harmonic Even Offset",
    "Harmonic Odd Offset"
    };
        
    // Effect Controller and Assignable Controller Sources
    public static final String[] SOURCES = new String[]     
    {
    "Bender",
    "Channel Pressure",
    "Modulation Wheel",
    "Expression Pedal",
    "MIDI Volume",
    "Panpot",
    "Controller 1",
    "Controller 2",
    "Controller 3",
    "Controller 4",
    "Controller 5",
    "Controller 6",
    "Controller 7",
    "Controller 8"
    };      
        

    // Effect Controller Destinations
    public static final String[] EFFECT_DESTINATIONS = new String[]
    {
    "Effect 1 Dry/Wet",
    "Effect 1 Para",
    "Effect 2 Dry/Wet",
    "Effect 2 Para",
    "Effect 3 Dry/Wet",
    "Effect 3 Para",
    "Effect 4 Dry/Wet",
    "Effect 4 Para",
    "Reverb Dry/Wet 1",
    "Reverb Dry/Wet 2",
    };


    // Controller Destinations (other than Macro, Effect controllers)
    public static final String[] DESTINATIONS = new String[]
    {
    "Pitch Offset",
    "Cutoff Offset",
    "Level",
    "Vibrato Depth Offset",
    "Growl Depth Offset",
    "Tremelo Depth Offset",
    "LFO Speed Offset",
    "Attack Time Offset", 
    "Decay 1 Time Offset",
    "Release Time Offset",
    "Velocity Offset", 
    "Resonance Offset",
    "Panpot Offset",
    "FF Bias Offset",
    "FF Env/LFO Depth Offset",
    "FF Env/LFO Speed Offset",
    "Harmonic Lo Offset",
    "Harmonic Hi Offset",
    "Harmonic Even Offset",
    "Harmonic Odd Offset"
    };
        
    // Names of PCM waves to include in additive patches
    public static final String[] SR_WAVES = new String[]
    {
    "Piano Noise Attack",
    "EP Noise Attack",
    "Percus Noise Attack",
    "Dist Gtr Noise Attack",
    "Orch Noise Attack",
    "Flanged Noise Attack",
    "Saw Noise Attack",
    "Zipper Noise Attack",
    "Organ Noise Looped",
    "Violin Noise Looped",
    "Crystal Noise Looped",
    "Sax Breath Looped",
    "Panflute Noise Looped",
    "Pipe Noise Looped",
    "Saw Noise Looped",
    "Gorgo Noise Looped",
    "Enhancer Noise Looped",
    "Tabla Spectrum Noise Looped",
    "Cave Spectrum Noise Looped",
    "White Noise Looped",
    "Clavi Attack",
    "Digi EP Attack",
    "Glocken Attack",
    "Vibe Attack",
    "Marimba Attack",
    "Org Key Click",
    "Slap Bass Attack",
    "Folk Gtr Attack",
    "Gut Gtr Attack",
    "Dist Gtr Attack",
    "Clean Gtr Attack",
    "Muted Gtr Attack",
    "Cello & Violin Attack",
    "Pizz Violin Attack",
    "Pizz Double Bass Attack",
    "Doo Attack",
    "Trombone Attack",
    "Brass Attack",
    "F.Horn1 Attack",
    "F.Horn2 Attack",
    "Flute Attack",
    "T.Sax Attack",
    "Shamisen Attack",
    "Voltage Attack",
    "BBDigi Attack",
    "BBDX Attack",
    "BBBlip Attack",
    "Techno Hit Attack",
    "Techno Attack",
    "X-Piano Attack",
    "Noisy Voise Looped",
    "Noisy Human Looped",
    "Ravoid Looped",
    "Hyper Looped",
    "Beef Looped",
    "Texture Looped",
    "MMBass Looped",
    "Syn PWM Cya",
    "Harpshichord Cyc",
    "Digi EP Cyc",
    "Soft EP Cyc",
    "Ep Bell Cyc",
    "Bandneon Cyc",
    "Chees Org Cyc",
    "Organ Cyc",
    "Oboe Cyc",
    "Crystal Cyc",
    "Syn Bass1 Cyc",
    "Syn Bass2 Cyc",
    "Syn Saw1 Cyc",
    "Svn Saw2 Cvq",
    "Syn Saw3 Cyc",
    "Syn Square1 Cyc",
    "Syn Square2 Cyc",
    "Syn Pulse1 Cyc",
    "Syn Pulse2 Cyc",
    "Pulse20 Cyc",
    "Pulse40 Cyc",
    "Nasty Cyc",
    "Mini Max Cyc",
    "Bottom Cyc",
    "> 64th Harmonics Cyc", //"Over 64th Harmonics Only Cyc",
    "> 64th Harmonics Cyc", //"Over 64th Harmonics Only Cyc",                           // FIXME Is this right?
    "BD Attack",
    "Ana Kick",
    "SD Attack",
    "Tiny SD Attack",
    "Ana SD Attack",
    "Ana HHO Attack",
    "Simonzu Tom Attack",
    "Ride Cup Attack",
    "Cowbell Attack",
    "Conga Attack",
    "Conga Muted Attack",
    "Agogo Attack",
    "Castanet Attack",
    "Claves Attack",
    "Tambourine Attack",
    "JingleBell Attack",
    "BellTree Attack",
    "WindowChime Attack",
    "AtariGame Attack",
    "Rama Attack",
    "Udo Attack",
    "TablaNa Attack",
    "Voice Ou Attack",
    "HighQ Attack",
    "Super Q Attack",
    "Glass Attack",
    "Metal Attack",
    "Noise Attack",
    "Pop Attack",
    "Crash Looped",
    "Burner Looped",
    "Jet Engine Looped",
    "Omnibus Loop 1",
    "Omnibus Loop 2",
    "Omnibus Loop 3",
    "Omnibus Loop 4",
    "Omnibus Loop 5",
    "Omnibus Loop 6",
    "Omnibus Loop 7",
    "Omnibus Loop 8"
    };

// Harmonic Constraints Edisyn provides
    public static final int ALL = 0;
    public static final int ODD = 1;
    public static final int EVEN = 2;
    public static final int FIRST_THIRD = 3;
    public static final int SECOND_THIRD = 4;
    public static final int THIRD_THIRD = 5;
    public static final int OCTAVE = 6;
    public static final int FIFTH = 7;
    public static final int MAJOR_THIRD = 8;
    public static final int MINOR_SEVENTH = 9;
    public static final int MAJOR_SECOND = 10; 
    public static final int MAJOR_SEVENTH = 11; 
    
/*
// Harmonic Mod Constraints
public static final int SAWTOOTH = 0;
public static final int SQUARE = 1;
public static final int TRIANGLE = 2;
public static final int ALL_OFF = 3;
public static final int ALL_ON = 4;
*/


    // Various harmonics Edisyn provides
    public static final int[] OCTAVE_HARMONICS = { 1, 2, 4, 8, 16, 32, 64 };
    public static final int[] FIFTH_HARMONICS = { 3, 6, 12, 24, 48 };               // -1 because we're in the 64..127 
    public static final int[] MAJOR_THIRD_HARMONICS = { 5, 10, 20, 40 };
    public static final int[] MINOR_SEVENTH_HARMONICS = { 7, 14, 28, 56 };
    public static final int[] MAJOR_SECOND_HARMONICS = { 9, 18, 36 };
    public static final int[] MAJOR_SEVENTH_HARMONICS = { 15, 30, 60 };
    
    
    // Inverted indices for lookup for emitting CCs
    static HashMap singleToneDataParamsToIndex = null;
    static HashMap sourceParamsToIndex = null;
    static HashMap addWaveKitParamsToIndex = null;
    static HashMap addWaveHCSoftParamsToIndex = null;
    static HashMap addWaveHCLoudParamsToIndex = null;
    static HashMap addWaveFormantFilterParamsToIndex = null;
    static HashMap addWaveHarmonicEnvelopeParamsToIndex = null;
    static HashMap addWaveHarmonicLoopParamsToIndex = null;
    static HashMap dialsToIndex = null;

    // All source, harmonics, and envelope tabs, so we can show and hide them when the user changes the number of sources
    SynthPanel[] sourceTabs = new SynthPanel[6];
    SynthPanel[] harmonicsTabs = new SynthPanel[6];
    SynthPanel[] envelopeTabs = new SynthPanel[6];



    public KawaiK5000()
        {
        if (singleToneDataParamsToIndex == null)
            {
            singleToneDataParamsToIndex = new HashMap();
            for(int i = 0; i < singleToneDataParams.length; i++)
                {
                singleToneDataParamsToIndex.put(singleToneDataParams[i], i);
                }
            }

        if (sourceParamsToIndex == null)
            {
            sourceParamsToIndex = new HashMap();
            for(int i = 0; i < sourceParams.length; i++)
                {
                sourceParamsToIndex.put(sourceParams[i], i);
                }
            }

        if (addWaveKitParamsToIndex == null)
            {
            addWaveKitParamsToIndex = new HashMap();
            for(int i = 0; i < addWaveKitParams.length; i++)
                {
                addWaveKitParamsToIndex.put(addWaveKitParams[i], i);
                }
            }

        if (addWaveHCSoftParamsToIndex == null)
            {
            addWaveHCSoftParamsToIndex = new HashMap();
            for(int i = 0; i < addWaveHCSoftParams.length; i++)
                {
                addWaveHCSoftParamsToIndex.put(addWaveHCSoftParams[i], i);
                }
            }

        if (addWaveHCLoudParamsToIndex == null)
            {
            addWaveHCLoudParamsToIndex = new HashMap();
            for(int i = 0; i < addWaveHCLoudParams.length; i++)
                {
                addWaveHCLoudParamsToIndex.put(addWaveHCLoudParams[i], i);
                }
            }

        if (addWaveFormantFilterParamsToIndex == null)
            {
            addWaveFormantFilterParamsToIndex = new HashMap();
            for(int i = 0; i < addWaveFormantFilterParams.length; i++)
                {
                addWaveFormantFilterParamsToIndex.put(addWaveFormantFilterParams[i], i);
                }
            }

        if (addWaveHarmonicEnvelopeParamsToIndex == null)
            {
            addWaveHarmonicEnvelopeParamsToIndex = new HashMap();
            for(int i = 0; i < addWaveHarmonicEnvelopeParams.length; i++)
                {
                addWaveHarmonicEnvelopeParamsToIndex.put(addWaveHarmonicEnvelopeParams[i], i);
                }
            }

        if (addWaveHarmonicLoopParamsToIndex == null)
            {
            addWaveHarmonicLoopParamsToIndex = new HashMap();
            for(int i = 0; i < addWaveHarmonicLoopParams.length; i++)
                {
                addWaveHarmonicLoopParamsToIndex.put(addWaveHarmonicLoopParams[i], i);
                }
            }

        if (dialsToIndex == null)
            {
            dialsToIndex = new HashMap();
            for(int i = 0; i < DIALS.length; i++)
                {
                dialsToIndex.put(DIALS[i], i);
                }
            }


        /// SOUND PANEL
                
        JComponent soundPanel = new SynthPanel(this);
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        hbox.addLast(addSources(Style.COLOR_C()));
        vbox.add(hbox);
        hbox = new HBox();
        buildAllEffects(new Color[] { Style.COLOR_B(), Style.COLOR_A(), Style.COLOR_B(), Style.COLOR_A(),  });
        hbox.add(addEffect(0, Style.COLOR_B()));
        hbox.addLast(addEffect(1, Style.COLOR_A()));
        vbox.add(hbox);
        hbox = new HBox();
        
        VBox a = new VBox();
        a.add(addEffect(2, Style.COLOR_B()));
        a.add(addReverb(Style.COLOR_B()));
        hbox.add(a);
                
        a = new VBox();
        a.add(addEffect(3, Style.COLOR_A()));
        a.add(addEQ(Style.COLOR_A()));
        hbox.addLast(a);

        vbox.add(hbox);

        HBox hbox2 = new HBox();
        hbox2.add(addDials(Style.COLOR_C()));
        
        VBox vbox2 = new VBox();
        vbox2.add(addEffectsGeneral(Style.COLOR_A()));
        vbox2.addLast(addControllers(Style.COLOR_B()));
        hbox2.addLast(vbox2);
        vbox.add(hbox2);

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("General", soundPanel);
        
        for(int source = 1; source <= 6; source++)
            {
            soundPanel = new SynthPanel(this);
            vbox = new VBox();
                
            hbox = new HBox();
            hbox.add(addDCO(source, Style.COLOR_C()));
            hbox.addLast(addPitchEnv(source, Style.COLOR_C()));
            vbox.add(hbox);
                
            hbox = new HBox();

            vbox.add(addDCFEnv(source, Style.COLOR_B()));
            hbox.add(addDCF(source, Style.COLOR_B()));
            hbox.addLast(addDCA(source, Style.COLOR_A()));
            vbox.add(hbox);
            vbox.add(addDCAEnv(source, Style.COLOR_A()));

            vbox.add(addLFO(source, Style.COLOR_B()));
            
            hbox = new HBox();
            hbox.add(addGeneral(source, Style.COLOR_A()));
			hbox.addLast(addControl(source, Style.COLOR_C()));
			vbox.add(hbox);

            soundPanel.add(vbox, BorderLayout.CENTER);
            addTab("S " + source, sourceTabs[source - 1] = (SynthPanel)soundPanel);
                                
                                
            soundPanel = new SynthPanel(this);
            vbox = new VBox();

            vbox.add(addHarmonics(source, Style.COLOR_A()));
                
            JComponent c1 = (JComponent)add(addHarmonicDisplay(source, true, Style.COLOR_A()));
            JComponent c2 = (JComponent)add(addHarmonicDisplay(source, false, Style.COLOR_A()));
            HSplitBox split = new HSplitBox(c1, c2);
            hbox = new HBox();
            hbox.addLast(split);
            vbox.add(split);

            vbox.add(addFormant(source, Style.COLOR_B()));
            vbox.add(addFormantDisplay(source, Style.COLOR_B()));
                

            soundPanel.add(vbox, BorderLayout.CENTER);
            addTab("H " + source, harmonicsTabs[source - 1] = (SynthPanel)soundPanel);
                

            soundPanel = new SynthPanel(this);
            vbox = new VBox();

            vbox.add(addHarmonicsEnvelopeHeader(source, Style.COLOR_C()));
                        
            hbox = new HBox();
            c1 = (JComponent)addHarmonicEnvelopeDisplay(source, 0, "rate0", "Rate 0", Style.COLOR_B());
            c2 = (JComponent)addHarmonicEnvelopeDisplay(source, 2, "rate1", "Rate 1", Style.COLOR_B());
            split = new HSplitBox(c1, c2);
            hbox.addLast(split);
            vbox.add(hbox);

            hbox = new HBox();
            c1 = (JComponent)addHarmonicEnvelopeDisplay(source, 1, "level0", "Level 0", Style.COLOR_A());
            c2 = (JComponent)addHarmonicEnvelopeDisplay(source, 3, "level1", "Level 1", Style.COLOR_A());
            split = new HSplitBox(c1, c2);
            hbox.addLast(split);
            vbox.add(hbox);

            hbox = new HBox();
            c1 = (JComponent)addHarmonicEnvelopeDisplay(source, 4, "rate2", "Rate 2", Style.COLOR_B());
            c2 = (JComponent)addHarmonicEnvelopeDisplay(source, 6, "rate3", "Rate 3", Style.COLOR_B());
            split = new HSplitBox(c1, c2);
            hbox.addLast(split);
            vbox.add(hbox);

            hbox = new HBox();
            c1 = (JComponent)addHarmonicEnvelopeDisplay(source, 5, "level2", "Level 2", Style.COLOR_A());
            c2 = (JComponent)addHarmonicEnvelopeDisplay(source, 7, "level3", "Level 3", Style.COLOR_A());
            split = new HSplitBox(c1, c2);
            hbox.addLast(split);
            vbox.add(hbox);
            
            hbox = new HBox();
            hbox.addLast(addHarmonicEnvelopeDisplay(source, 8, "loop", "Loop", Style.COLOR_B()));
            vbox.add(hbox);

            soundPanel.add(vbox, BorderLayout.CENTER);
            addTab("E " + source, envelopeTabs[source - 1] = (SynthPanel)soundPanel);
            }
                
        model.set("name", "INIT");  // has to be 10 long

        model.set("bank", 0);
        model.set("number", 0);

        loadDefaults();        
        }
                
                
    public String getDefaultResourceFileName() { return "KawaiK5000.init"; }
    public String getHTMLResourceFileName() { return "KawaiK5000.html"; }

    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        JComboBox bank = new JComboBox(BANKS);
        bank.setSelectedIndex(model.get("bank"));
        
        JTextField number = new SelectedTextField("" + (model.get("number") + 1), 3);

        while(true)
            {
            boolean result = showMultiOption(this, new String[] { "Bank", "Patch Number"}, 
                new JComponent[] { bank, number }, title, "Enter the Bank and Patch number");
                
            if (result == false) 
                return false;
                                
            int n;
            try { n = Integer.parseInt(number.getText()); }
            catch (NumberFormatException e)
                {
                showSimpleError(title, "The Patch Number must be an integer 1...128");
                continue;
                }
            if (n < 1 || n > 12)
                {
                showSimpleError(title, "The Patch Number must be an integer 1...128");
                continue;
                }
                
            n--;
                                
            int i = bank.getSelectedIndex();
                        
            change.set("bank", i);
            change.set("number", n);
                        
            return true;
            }
        }
        
    public String revisePatchName(String name)
        {
        name = super.revisePatchName(name);  // trim first time
        if (name.length() > MAXIMUM_NAME_LENGTH)
            name = name.substring(0, MAXIMUM_NAME_LENGTH);
        
        StringBuffer nameb = new StringBuffer(name);                            
        for(int i = 0 ; i < nameb.length(); i++)
            {
            char c = nameb.charAt(i);
            if (c < 32 || c > 127)
                nameb.setCharAt(i, ' ');
            }
        name = nameb.toString();
        return super.revisePatchName(name);  // trim again
        }


    /** Add the global patch category (name, id, number, etc.) */
    public JComponent addNameGlobal(Color color)
        {
        Category globalCategory = new Category(this, getSynthName(), color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
                
        VBox vbox = new VBox();
        comp = new PatchDisplay(this, 8);
        vbox.add(comp);
        hbox.add(vbox);
        
        vbox = new VBox();
        
        comp = new StringComponent("Patch Name", this, "name", MAXIMUM_NAME_LENGTH, "Name must be up to " + MAXIMUM_NAME_LENGTH + " ASCII characters.")
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
        vbox.add(comp);  // doesn't work right :-(
        hbox.add(vbox);

        vbox = new VBox();
        params = POLY_MODES;
        comp = new Chooser("Mode", this, "poly", params);
        vbox.add(comp);
        comp = new CheckBox("Portamento", this, "portamento", false);
        vbox.add(comp);
        hbox.add(vbox);
        comp = new LabelledDial("Portamento Speed", this, "portamentospeed", color, 0, 127);
        hbox.add(comp);
        comp = new LabelledDial("Volume", this, "volume", color, 0, 127);
        hbox.add(comp);
        
        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }
        
    public JComponent addDials(Color color)
        {
        Category category = new Category(this, "Dials", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        for(int i = 0; i < 16; i+=4)
            {
            for(int j = i; j < i + 4; j++)
                {
                comp = new LabelledDial(DIALS[j], this, "dial" + (j + 1), color, 0, 127);
                model.setStatus("dial" + (j + 1), Model.STATUS_RESTRICTED);
                hbox.add(comp);
                }
            vbox.add(hbox);
            hbox = new HBox();
            }
                
        category.add(vbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addSources(Color color)
        {
        Category category = new Category(this, "Sources", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        comp = new CheckBox("Source 1", this, "srcmute1", true);
        vbox.add(comp); 
        comp = new CheckBox("Source 2", this, "srcmute2", true);
        vbox.add(comp); 
        hbox.add(vbox);   
        vbox = new VBox();
        comp = new CheckBox("Source 3", this, "srcmute3", true);
        vbox.add(comp); 
        comp = new CheckBox("Source 4", this, "srcmute4", true);
        vbox.add(comp); 
        hbox.add(vbox);   
        vbox = new VBox();
        comp = new CheckBox("Source 5", this, "srcmute5", true);
        vbox.add(comp); 
        comp = new CheckBox("Source 6", this, "srcmute6", true);
        vbox.add(comp); 
        hbox.add(vbox);   

        comp = new LabelledDial("Sources", this, "srctype", color, 2, 6)
            {
            public void update(String key, Model model) 
                { 
                super.update(key, model);
                updateTabs();
                }
            };
                
        hbox.add(comp);

        comp = new LabelledDial("AM Source", this, "am", color, 0, 5)
            {
            public String map(int val) { if (val == 0) return "Off"; else return "" + (val + 1); }
            };
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
        
    public void updateTabs()
    	{
    	// Get the title of the current tab
    	int index = getSelectedTabIndex();
    	String tabTitle = (index >= 0 ? tabs.getTitleAt(index) : null);
    	
    	// Remove tabs and add in proper ones
                if (sourceTabs[0] != null)
                    {
                    // remove all the tabs
                    while(getNumTabs() > 1)
                        {
                        String s = tabs.getTitleAt(1);
                        if (s.startsWith("S ") || s.startsWith("H ") || s.startsWith("E "))
                            {
                            tabs.remove(1);
                            }
                        }
                    // Add in selected tabs
                    for(int i = model.get("srctype") - 1; i >= 0; i--)
                        {
                        if (model.get("source" + (i + 1) + "additive") == 1 && envelopeTabs[i] != null)
                        	{
                        	tabs.add(envelopeTabs[i], 1);
                        	tabs.setTitleAt(1, "E " + (i + 1));
	                        tabs.add(harmonicsTabs[i], 1);
                        	tabs.setTitleAt(1, "H " + (i + 1));
                        	}
                        if (sourceTabs[i] != null)
                        	{
                        	tabs.add(sourceTabs[i], 1);
                        	tabs.setTitleAt(1, "S " + (i + 1));
                        	}
                        }
                    }
        // re-select the tab we used to be in
        if (tabTitle != null)
        	{
        	index = getIndexOfTabTitle(tabTitle);
        	if (index >= 0)
        		{
        		setSelectedTabIndex(index);
        		}
        	}
    	}

    public JComponent addControllers(Color color)
        {
        Category category = new Category(this, "Controllers", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = SWITCHES;
        comp = new Chooser("Switch 1", this, "sw1parameter", params);
        vbox.add(comp);
        comp = new Chooser("Switch 2", this, "sw2parameter", params);
        vbox.add(comp);
        comp = new Chooser("Foot Switch 1", this, "fsw2parameter", params);
        vbox.add(comp);
        comp = new Chooser("Foot Switch 2", this, "fsw2parameter", params);
        vbox.add(comp);
        hbox.add(vbox);

        vbox = new VBox();
        for(int i = 1; i <= 4; i++)
            {
            params = MACRO_DESTINATIONS;
            comp = new Chooser("Macro " + i + " Destination 1", this, "macrocontroller" + i + "parameter1", params);
            vbox.add(comp);
            }
        hbox.add(vbox);

        vbox = new VBox();
        for(int i = 1; i <= 4; i++)
            {
            params = MACRO_DESTINATIONS;
            comp = new Chooser("Macro " + i + " Destination 2", this, "macrocontroller" + i + "parameter2", params);
            vbox.add(comp);
            }
        hbox.add(vbox);        
        
        vbox = new VBox();
        HBox hbox2 = new HBox();
        for(int i = 1; i <= 4; i++)
            {
            comp = new LabelledDial("Macro " + i, this, "macrocontroller" + i + "depth1", color, 33, 95, 64);
            ((LabelledDial)comp).addAdditionalLabel("Depth 1");
            hbox2.add(comp);
            comp = new LabelledDial("Macro " + i, this, "macrocontroller" + i + "depth2", color, 33, 95, 64);
            ((LabelledDial)comp).addAdditionalLabel("Depth 2");
            hbox2.add(comp);
            if (i == 2 || i == 4) 
                {
                vbox.add(hbox2);
                hbox2 = new HBox();
                }
            }
        hbox.add(vbox);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
        
    public JComponent addEffectsGeneral(Color color)
        {
        Category category = new Category(this, "Effects", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        comp = new LabelledDial("Algorithm", this, "algorithm", color, 0, 3, -1);
        hbox.add(comp);

        hbox.add(Strut.makeHorizontalStrut(16));
        comp = new IconDisplay(null, EFFECT_ALGORITHM_ICONS, this, "algorithm", 192, 84);
        hbox.add(comp);
        hbox.add(Strut.makeHorizontalStrut(16));

        VBox vbox = new VBox();
        params = SOURCES;
        comp = new Chooser("Control Source 1", this, "effectcontrol1source", params);
        vbox.add(comp);
        params = EFFECT_DESTINATIONS;
        comp = new Chooser("Control Destination 1", this, "effectcontrol1destination", params);
        vbox.add(comp);
        hbox.add(vbox);

        vbox = new VBox();
        params = SOURCES;
        comp = new Chooser("Control Source 2", this, "effectcontrol2source", params);
        vbox.add(comp);
        params = EFFECT_DESTINATIONS;
        comp = new Chooser("Control Destination 2", this, "effectcontrol2destination", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Control 1", this, "effectcontrol1depth", color, 33, 95, 64);
        ((LabelledDial)comp).addAdditionalLabel("Depth");
        hbox.add(comp);
        comp = new LabelledDial("Control 2", this, "effectcontrol2depth", color, 33, 95, 64);
        ((LabelledDial)comp).addAdditionalLabel("Depth");
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
 
        
    /*
      public static final int[] REVERB_DEFAULT_1 = { 85, 40, 70, 85, 80, 65, 70, 80, 70, 0, 40 };
      public static final int[] REVERB_DEFAULT_2 = { 85, 40, 70, 85, 80, 65, 70, 80, 70, 0, 40 };
      public static final int[] REVERB_DEFAULT_3 = { 13, 13, 28, 4, 8, 13, 8, 18, 28, 10, 100 };
      public static final int[] REVERB_DEFAULT_4 = { 30, 50, 60, 5, 30, 40, 5, 30, 40, 0, 500 };
      public static final int[] REVERB_DEFAULT_5 = { 25, 5, 25, 15, 5, 40, 5, 20, 40, 20, 20 };
    */
        
    HBox reverbBox = new HBox();

    public void rebuildReverb(Color color)
        {
        // This updates a lot of stuff, which sends out parameters that we don't want.
        // So we're blocking midi here
        boolean midi = getSendMIDI();
        setSendMIDI(false);
        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        reverbBox.removeAll();
        int type = this.getModel().get("reverbtype");
        
        comp = new LabelledDial("Dry/Wet 1", this, "reverb" + (type + 1) + "drywet1", color, 0, 100);
        reverbBox.add(comp);

        comp = new LabelledDial("Dry/Wet 2", this, "reverb" + (type + 1) + "para1", color, 0, 100);
        reverbBox.add(comp);

        if (type == 9 || type == 10)  // reverse, long delay
            {
            comp = new LabelledDial("Feedback", this, "reverb" + (type + 1) + "para2", color, 0, 100);
            }
        else
            {
            comp = new LabelledDial("Reverb", this, "reverb" + (type + 1) + "para2", color, 0, 47)
                {
                public String map(int val) { int sec = val * 10 + 3; return "" + (sec / 10) + "." + (sec % 10) + " s"; }
                };
            ((LabelledDial)comp).addAdditionalLabel("Time");
            }
        reverbBox.add(comp);

        if (type == 10)  // long delay
            {
            comp = new LabelledDial("Delay", this, "reverb" + (type + 1) + "para3", color, 0, 100)
                {
                public String map(int val) { return "" + val + " ms"; }
                };
            ((LabelledDial)comp).addAdditionalLabel("Time");
            }
        else
            {
            comp = new LabelledDial("Predelay", this, "reverb" + (type + 1) + "para3", color, 0, 127)
                {
                public String map(int val) { return "<html><font size=-1>" + (val * 10 + 200) + " ms</font></html>"; }
                };
            ((LabelledDial)comp).addAdditionalLabel("Time");
            }
        reverbBox.add(comp);

        comp = new LabelledDial("High Freq", this, "reverb" + (type + 1) + "para4", color, 0, 100);
        ((LabelledDial)comp).addAdditionalLabel("Damping");
        reverbBox.add(comp);

        reverbBox.revalidate(); 
        reverbBox.repaint(); 
        
        setSendMIDI(midi);
        }
 
    public JComponent addReverb(Color color)
        {
        Category category = new Category(this, "Reverb", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        // Make some dummies
        for(int type = 0; type < REVERB_TYPES.length; type++)
            {
            String key = "reverb" + (type + 1) + "drywet1";
            model.set(key, 0);
            int max = 100;
            model.setMinMax(key, 0, max);
            model.setMetricMinMax(key, 0, max);
                                
            key = "reverb" + (type + 1) + "para1";
            model.set(key, 0);
            max = 100;
            model.setMinMax(key, 0, max);
            model.setMetricMinMax(key, 0, max);

            key = "reverb" + (type + 1) + "para2";
            model.set(key, 0);
            max = (type == 9 || type == 10 ? 100 : 47);
            model.setMinMax(key, 0, max);
            model.setMetricMinMax(key, 0, max);

            key = "reverb" + (type + 1) + "para3";
            model.set(key, 0);
            max = (type == 10 ? 100 : 127);
            model.setMinMax(key, 0, max);
            model.setMetricMinMax(key, 0, max);

            key = "reverb" + (type + 1) + "para4";
            model.set(key, 0);
            max = 100;
            model.setMinMax(key, 0, max);
            model.setMetricMinMax(key, 0, max);
            }
        
        params = REVERB_TYPES;
        comp = new Chooser("Type", this, "reverbtype", params)
            {
            public void update(String key, Model model) 
                { 
                super.update(key, model); 
                rebuildReverb(color); 
                int type = model.get("reverbtype");
                // We also want to send the reverb params
                for(int i = 0; i < 5; i++)
                    {
                    String paramkey = "reverb" + (type + 1) + (i == 0 ? "drywet1" : "para" + i);
                    model.set(paramkey, model.get(paramkey));
                    }
                }
            };
        vbox.add(comp);
        hbox.add(vbox);
                
        hbox.addLast(reverbBox);
                        
        rebuildReverb(color);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addEQ(Color color)
        {
        Category category = new Category(this, "EQ", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        comp = new LabelledDial("100 Hz", this, "geqfreq1", color, 58, 70, 64);
        hbox.add(comp);
        comp = new LabelledDial("200 Hz", this, "geqfreq2", color, 58, 70, 64);
        hbox.add(comp);
        comp = new LabelledDial("450 Hz", this, "geqfreq3", color, 58, 70, 64);
        hbox.add(comp);
        comp = new LabelledDial("1 KHz", this, "geqfreq4", color, 58, 70, 64);
        hbox.add(comp);
        comp = new LabelledDial("2 KHz", this, "geqfreq5", color, 58, 70, 64);
        hbox.add(comp);
        comp = new LabelledDial("4 KHz", this, "geqfreq6", color, 58, 70, 64);
        hbox.add(comp);
        comp = new LabelledDial("8 KHz", this, "geqfreq7", color, 58, 70, 64);
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
       
        
        
    HBox effectBox[] = { new HBox(), new HBox(), new HBox(), new HBox() };

    LabelledDial lastDial = null;           // will always be filled in by the time we need it
        
    HBox allEffects[][];
        
    public void buildAllEffects(Color[] color)              // ugh this is expensive
        {
        int numEffectTypes = EFFECT_PARAMETER_MINS[0].length;
        allEffects = new HBox[NUM_EFFECTS][numEffectTypes];
        for(int i = 0; i < NUM_EFFECTS; i++)
            {
            for(int j = 0; j < numEffectTypes; j++)
                {
                allEffects[i][j] = buildEffect(i, j, color[i]);
                }
            }
        }
        
    public HBox buildEffect(int effect, int type, Color color)
        {
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();

        //int type = this.getModel().get("effect" + (effect + 1) + "type");
                
        int count = 0;
        for(int i = 0; i < 5; i++)
            {
            final int _i = i;
            // First we need to determine how to split up the labelled dial name into multiple lines
            String s[] = EFFECT_PARAMETER_NAMES[i][type].split(" ");
            for(int j = 0; j < s.length; j++) s[j] = s[j].replace("_", " ");
                        
            // We make sure the first dial name has two lines, so the effects categories sync up vertically
            if (s.length == 1)
                {
                String[] oldS = s;
                s = new String[2];
                s[0] = oldS[0];
                s[1] = "<html>&nbsp;</html>";  // this seems to work
                }
                        
            // Next, 1270, 720, and 200 are NOT the real "max value" -- they are placeholders for three
            // display functions.  The real max values are 127, 127, and 100 respectively.
            int trueMax = EFFECT_PARAMETER_MAXES[i][type];
            if (EFFECT_PARAMETER_MAXES[i][type] == 1270)
                {
                trueMax = 127;
                }
            else if (EFFECT_PARAMETER_MAXES[i][type] == 720)
                {
                trueMax = 127;
                }
            else if (EFFECT_PARAMETER_MAXES[i][type] == 200)
                {
                trueMax = 100;
                }
                        
            int max = EFFECT_PARAMETER_MAXES[_i][type];
            if (max == -1)  // Not a parameter
                {
                // make some dummies
                model.set("effect" + (effect + 1) + "type" + (type + 1) + (i == 0 ? "depth" : "para" + i), 0);
                model.setMinMax("effect" + (effect + 1) + "type" + (type + 1) + (i == 0 ? "depth" : "para" + i), 0, 0);
                model.setMetricMinMax("effect" + (effect + 1) + "type" + (type + 1) + (i == 0 ? "depth" : "para" + i), 0, 0);
                }
            else 
                {
                count++;
                comp = lastDial = new LabelledDial(s[0], this, "effect" + (effect + 1) + "type" + (type + 1) + (i == 0 ? "depth" : "para" + i), color, EFFECT_PARAMETER_MINS[i][type], trueMax)
                    {
                    public boolean isSymmetric()
                        {
                        return (max == 76);             // -12dB ... +12dB
                        }
                                        
                    public String map(int val)
                        {
                        // Based on the placeholder "max value", we need to determine the actual value to display
                        // on the LabelledDial.  The complicated one is 720, which means 0-100 by 2, 100-250 by 5, 250-720 by 10.
                        // The others are fairly easy.
                        if (max == 1)
                            {
                            return (val == 0 ? "Sin" : "Tri");
                            }
                        else if (max == 76)
                            {
                            return "" + (val - 64) + EFFECT_PARAMETER_UNITS[_i][type];
                            }
                        else if (max <= 100)
                            {
                            return String.valueOf(val) + EFFECT_PARAMETER_UNITS[_i][type];
                            }
                        else if (max == 1270)
                            {
                            return "<html><center>" + (val * 10) + "<br>" + EFFECT_PARAMETER_UNITS[_i][type] + "</center></html>";
                            }
                        else if (max == 720)
                            {
                            return "" + 
                                (val < 50 ? val * 2 :
                                    (val < 50 + 30 ? (val - 50) * 5 + 100 :
                                    (val - 50 - 30) * 10 + 250)) + EFFECT_PARAMETER_UNITS[_i][type];
                            }
                        else if (max == 200)
                            {
                            return "" + (val * 2) + EFFECT_PARAMETER_UNITS[_i][type];
                            }
                        else
                            {
                            return "What?";
                            }
                        }
                    };
                                
                        
                // Add the additional labels and put in the box
                for(int j = 1; j < s.length; j++) ((LabelledDial)comp).addAdditionalLabel(s[j]);
                hbox.add(comp);
                }
            }
        // Add filler
        for(int i = count; i < 5; i++)
            {
            hbox.add(Strut.makeStrut(lastDial));
            }
        return hbox;
        }
 
    public JComponent addEffect(final int effect, Color color)
        {
        Category category = new Category(this, "Effect " + (effect + 1), color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        params = EFFECT_TYPES;
        comp = new Chooser("Type", this, "effect" + (effect + 1) + "type", params)
            {
            public void update(String key, Model model) 
                { 
                super.update(key, model); 
                hbox.removeLast();
                hbox.addLast(allEffects[effect][model.get("effect" + (effect + 1) + "type")]);
                int type = model.get("effect" + (effect + 1) + "type");
                // We also want to send the effects
                for(int i = 0; i < 5; i++)
                    {
                    String paramkey = "effect" + (effect + 1) + "type" + (type + 1) + (i == 0 ? "depth" : "para" + i);
                    model.set(paramkey, model.get(paramkey));
                    }
                hbox.revalidate();
                hbox.repaint();         
                }
            };
        vbox.add(comp);
        hbox.add(vbox);

        hbox.revalidate();
        hbox.repaint(); 

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
        
    public JComponent addHarmonics(int source, Color color)
        {
        Category category = new Category(this, "Harmonics", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        VBox morfv = new VBox();
        VBox normv = new VBox();
        HBox morfh = new HBox();
        HBox normh = new HBox();

        comp = new CheckBox("MORF", this, "source" + source + "morfflag")
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                vbox.remove(normv);
                vbox.remove(morfv);
                hbox.remove(normh);
                hbox.remove(morfh);
                if (model.get(key) == 0)
                    {
                    vbox.add(normv);
                    hbox.add(normh);
                    }
                else
                    {
                    vbox.add(morfv);
                    hbox.add(morfh);
                    }
                vbox.revalidate();
                hbox.revalidate();
                hbox.repaint();
                }
            };
        vbox.add(comp);
        hbox.add(vbox);
            
            
        /// NON-MORF

        params = HARMONIC_GROUPS;
        comp = new Chooser("Harmonic Group", this, "source" + source + "harmgroup", params);
        normv.add(comp);

        normh.add(Strut.makeHorizontalStrut(8));

        comp = new LabelledDial("Gain", this, "source" + source + "totalgain", color, 1, 63);
        normh.add(comp);

        comp = new LabelledDial("KS to Gain", this, "source" + source + "kstogain", color, 1, 127, 64);
        normh.add(comp);

        comp = new LabelledDial("Balance Vel. Curve", this, "source" + source + "balancevelocurve", color, 0, 11);
        normh.add(comp);

        comp = new LabelledDial("Balance Vel. Depth", this, "source" + source + "balancevelodepth", color, 0, 127);
        normh.add(comp);


        /// MORF
                
        comp = new CheckBox("Loop", this, "source" + source + "loop");
        morfv.add(comp);

        comp = new LabelledDial("HC1 Patch", this, "source" + source + "hc1patch", color, 0, 127);
        morfh.add(comp);
                
        comp = new LabelledDial("HC2 Patch", this, "source" + source + "hc2patch", color, 0, 127);
        morfh.add(comp);
                
        comp = new LabelledDial("HC3 Patch", this, "source" + source + "hc3patch", color, 0, 127);
        morfh.add(comp);
                
        comp = new LabelledDial("HC4 Patch", this, "source" + source + "hc4patch", color, 0, 127);
        morfh.add(comp);
                
        comp = new LabelledDial("HC1 Source", this, "source" + source + "hc1source", color, 0, 11)
            {
            public String map(int val)
                {
                if (val < 6) return "<html><font size=\"-1\">Soft&nbsp;" + (val + 1);
                else return "<html><font size=\"-1\">Loud&nbsp;" + (val - 5) + "</font></html>";
                }
            };
        morfh.add(comp);
                
        comp = new LabelledDial("HC2 Source", this, "source" + source + "hc2source", color, 0, 127)
            {
            public String map(int val)
                {
                if (val < 6) return "<html><font size=\"-1\">Soft&nbsp;" + (val + 1) + "</font></html>";
                else return "<html><font size=\"-1\">Loud&nbsp;" + (val - 5) + "</font></html>";
                }
            };
        morfh.add(comp);
                
        comp = new LabelledDial("HC3 Source", this, "source" + source + "hc3source", color, 0, 127)
            {
            public String map(int val)
                {
                if (val < 6) return "<html><font size=\"-1\">Soft&nbsp;" + (val + 1) + "</font></html>";
                else return "<html><font size=\"-1\">Loud&nbsp;" + (val - 5) + "</font></html>";
                }
            };
        morfh.add(comp);
                
        comp = new LabelledDial("HC4 Source", this, "source" + source + "hc4source", color, 0, 127)
            {
            public String map(int val)
                {
                if (val < 6) return "<html><font size=\"-1\">Soft&nbsp;" + (val + 1) + "</font></html>";
                else return "<html><font size=\"-1\">Loud&nbsp;" + (val - 5) + "</font></html>";
                }
            };
        morfh.add(comp);

        comp = new LabelledDial("Time 1", this, "source" + source + "hetime1", color, 0, 127);
        morfh.add(comp);

        comp = new LabelledDial("Time 2", this, "source" + source + "hetime2", color, 0, 127);
        morfh.add(comp);

        comp = new LabelledDial("Time 3", this, "source" + source + "hetime3", color, 0, 127);
        morfh.add(comp);

        comp = new LabelledDial("Time 4", this, "source" + source + "hetime4", color, 0, 127);
        morfh.add(comp);
                
        vbox.add(normv);
        hbox.add(normh);
                
        hbox.revalidate();
        hbox.repaint(); 

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
        
        
    public JComponent addFormant(int source, Color color)
        {
        Category category = new Category(this, "Formant", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();

        VBox envv = new VBox();
        VBox lfov = new VBox();
        HBox envh = new HBox();
        HBox lfoh = new HBox();

        params = FORMANT_MODULATIONS;
        comp = new Chooser("Modulation", this, "source" + source + "formantenvlfosel", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                
                vbox.remove(lfov);
                vbox.remove(envv);
                hbox.remove(lfoh);
                hbox.remove(envh);
                
                if (model.get(key) == 0)
                    {
                    vbox.add(envv);
                    hbox.addLast(envh);
                    }
                else
                    {
                    vbox.add(lfov);
                    hbox.addLast(lfoh);
                    }
                vbox.revalidate();
                hbox.revalidate();
                hbox.repaint();
                }
            };
        vbox.remove(lfov);
        vbox.remove(envv);
        hbox.remove(lfoh);
        hbox.remove(envh);
                
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Bias", this, "source" + source + "formantbias", color, 1, 127, 64);
        hbox.add(comp);

            
        vbox.add(envv);
        hbox.addLast(envh);
            

        /// LFO

        params = HARMONIC_LFO_WAVES;
        comp = new Chooser("LFO Shape", this, "source" + source + "formantlfoshape", params);
        lfov.add(comp);

        comp = new LabelledDial("Speed", this, "source" + source + "formantlfospeed", color, 0, 127);
        lfoh.add(comp);

        comp = new LabelledDial("LFO", this, "source" + source + "formantlfodepth", color, 0, 63);
        ((LabelledDial)comp).addAdditionalLabel("Depth");               // this makes the height the same between LFO and ENV
        lfoh.add(comp);
                
                
        //// ENV

        params = ENV_LOOPS;
        comp = new Chooser("Loop", this, "source" + source + "formantloop", params);
        envv.add(comp);

        comp = new LabelledDial("Env Depth", this, "source" + source + "formantenvdepth", color, 1, 127, 64);
        envh.add(comp);

        comp = new LabelledDial("Attack", this, "source" + source + "formantattackrate", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Rate");
        envh.add(comp);

        comp = new LabelledDial("Attack", this, "source" + source + "formantattacklevel", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        envh.add(comp);

        comp = new LabelledDial("Decay 1", this, "source" + source + "formantdecay1rate", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Rate");
        envh.add(comp);

        comp = new LabelledDial("Decay 1", this, "source" + source + "formantdecay1level", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        envh.add(comp);

        comp = new LabelledDial("Decay 2", this, "source" + source + "formantdecay2rate", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Rate");
        envh.add(comp);

        comp = new LabelledDial("Decay 2", this, "source" + source + "formantdecay2level", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        envh.add(comp);

        comp = new LabelledDial("Release", this, "source" + source + "formantreleaserate", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Rate");
        envh.add(comp);

        comp = new LabelledDial("Release", this, "source" + source + "formantreleaselevel", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        envh.add(comp);

        comp = new LabelledDial("Velo Sense", this, "source" + source + "formantvelosenseenvdepth", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Env Depth");
        envh.add(comp);

        comp = new LabelledDial("KS", this, "source" + source + "formantksenvdepth", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Env Depth");
        envh.add(comp);
                
        EnvelopeDisplay disp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "source" + source + "formantattackrate",  "source" + source + "formantdecay1rate",  "source" + source + "formantdecay2rate",  null,                                                                     "source" + source + "formantreleaserate" },
            new String[] { null, "source" + source + "formantattacklevel", "source" + source + "formantdecay1level", "source" + source + "formantdecay2level", "source" + source + "formantdecay2level", "source" + source + "formantreleaselevel"},
            new double[] { 0, 1.0 / 5 / 127.0, 1.0 / 5 / 127.0, 1.0 / 5 / 127.0, 1.0 / 5, 1.0 / 5 / 127.0 },
            new double[] { 1.0 / 126, 1.0 / 126, 1.0 / 126, 1.0 / 126, 1.0 / 126, 1.0 / 126 });
        disp.setAxis(63 / 127.0);               // dunno if this will work, I may be off by a pixel
        envh.addLast(disp);

        hbox.revalidate();
        hbox.repaint(); 

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
                
                        
    public JComponent addGeneral(int source, Color color)
        {
        Category category = new Category(this, "General", color);
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();

                
        params = VELO_SW;
        comp = new Chooser("Velo Switch", this, "source" + source + "velosw", params);
        hbox.add(comp);

        params = PAN_TYPES;
        comp = new Chooser("Pan Type", this, "source" + source + "pantype", params);
        hbox.add(comp);
        
        vbox.add(hbox);
        
        hbox = new HBox();
            
        comp = new LabelledDial("Velocity", this, "source" + source + "velo", color, 0, 31)
            {
            /// FIXME: These values need to be tested
            public String map(int val) { if (val == 0) return "4"; else return "" + (val * 4 + 3); }
            };
        hbox.add(comp);

        comp = new LabelledDial("Pan", this, "source" + source + "pannormalvalue", color, 1, 127, 64)
            {
            public String map(int val) 
                { 
                if (val == 64) return "--"; 
                else if (val < 64) return "<" + (64 - val);
                else return "" + (val - 64) + ">";
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Volume", this, "source" + source + "volume", color, 0, 127);
        hbox.add(comp);
                
        comp = new LabelledDial("Effect", this, "source" + source + "effectpath", color, 0, 3, -1);
        hbox.add(comp);

		vbox.add(hbox);
		hbox = new HBox();
		
        comp = new LabelledDial("Zone Low", this, "source" + source + "zonelo", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Zone Hi", this, "source" + source + "zonehi", color, 0, 127);
        hbox.add(comp);


        comp = new LabelledDial("Key On Delay", this, "source" + source + "keyondelay", color, 0, 127);
        //((LabelledDial)comp).addAdditionalLabel("Delay");
        hbox.add(comp);
        vbox.add(hbox);
                
        category.add(vbox, BorderLayout.CENTER);
        return category;
        }
                
    public JComponent addControl(int source, Color color)
        {
        Category category = new Category(this, "Control", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();

        //hbox.add(vbox);
        vbox = new VBox();

        params = DESTINATIONS;
        comp = new Chooser("Mod Wheel Destination 1", this, "source" + source + "wheeldestination1", params);
        vbox.add(comp);

        params = DESTINATIONS;
        comp = new Chooser("Expression Destination 1", this, "source" + source + "wheeldestination1", params);
        vbox.add(comp);

        params = DESTINATIONS;
        comp = new Chooser("Pressure Destination 1", this, "source" + source + "pressdestination1", params);
        vbox.add(comp);

        params = SOURCES;
        comp = new Chooser("Assignable Source 1", this, "source" + source + "assignablecontrol1source", params);
        vbox.add(comp);

        params = DESTINATIONS;
        comp = new Chooser("Assignable Destination 1", this, "source" + source + "assignablecontrol1destination", params);
        vbox.add(comp);

        hbox.add(vbox);
        vbox = new VBox();

        params = DESTINATIONS;
        comp = new Chooser("Mod Wheel Destination 2", this, "source" + source + "wheeldestination2", params);
        vbox.add(comp);

        params = DESTINATIONS;
        comp = new Chooser("Expression Destination 2", this, "source" + source + "wheeldestination2", params);
        vbox.add(comp);

        params = SOURCES;
        comp = new Chooser("Assignable Source 2", this, "source" + source + "assignablecontrol2source", params);
        vbox.add(comp);

        params = DESTINATIONS;
        comp = new Chooser("Pressure Destination 2", this, "source" + source + "pressdestination2", params);
        vbox.add(comp);

        params = DESTINATIONS;
        comp = new Chooser("Assignable Destination 2", this, "source" + source + "assignablecontrol2destination", params);
        vbox.add(comp);
                
        hbox.add(vbox);

        VBox vbox1 = new VBox();
        HBox hbox1 = new HBox();
                
        comp = new LabelledDial("Pitch Bend", this, "source" + source + "benderpitch", color, 0, 24);
//        ((LabelledDial)comp).addAdditionalLabel("Bend");
        hbox1.add(comp);

        comp = new LabelledDial("Pressure", this, "source" + source + "pressdepth1", color, 33, 95, 64);
        ((LabelledDial)comp).addAdditionalLabel("Depth 1");
        hbox1.add(comp);
                
        comp = new LabelledDial("Mod Wheel", this, "source" + source + "wheeldepth1", color, 33, 95, 64);
             ((LabelledDial)comp).addAdditionalLabel("Depth 1");
        hbox1.add(comp);
                
        comp = new LabelledDial("Expression", this, "source" + source + "expressdepth1", color, 33, 95, 64);
               ((LabelledDial)comp).addAdditionalLabel("Depth 1");
        hbox1.add(comp);
                
        comp = new LabelledDial("Assignable", this, "source" + source + "assignablecontrol1depth", color, 33, 95, 64);
              ((LabelledDial)comp).addAdditionalLabel("Depth 1");
        hbox1.add(comp);

        vbox1.add(hbox1);
        hbox1 = new HBox();

        comp = new LabelledDial("PB Cutoff", this, "source" + source + "bendercutoff", color, 0, 31);
        // ((LabelledDial)comp).addAdditionalLabel("Cutoff");
        hbox1.add(comp);

        comp = new LabelledDial("Pressure", this, "source" + source + "pressdepth2", color, 33, 95, 64);
           ((LabelledDial)comp).addAdditionalLabel("Depth 2");
        hbox1.add(comp);

        comp = new LabelledDial("Mod Wheel", this, "source" + source + "wheeldepth2", color, 33, 95, 64);
             ((LabelledDial)comp).addAdditionalLabel("Depth 2");
        hbox1.add(comp);
                
        comp = new LabelledDial("Expression", this, "source" + source + "expressdepth2", color, 33, 95, 64);
               ((LabelledDial)comp).addAdditionalLabel("Depth 2");
        hbox1.add(comp);
                
        comp = new LabelledDial("Assignable", this, "source" + source + "assignablecontrol2depth", color, 33, 95, 64);
        ((LabelledDial)comp).addAdditionalLabel("Depth 2");
        hbox1.add(comp);

        vbox1.add(hbox1);
        hbox.add(vbox1);
                                
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addDCO(int source, Color color)
        {
        Category category = new Category(this, "DCO", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
                
        params = SR_WAVES;
        JComponent pcmwave = new Chooser("PCM Wave", this, "source" + source + "wavekit", params);

        VBox wavebox = vbox;
                
        params = SOURCE_TYPES;
        comp = new Chooser("Source Type", this, "source" + source + "additive", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                wavebox.remove(pcmwave);
                if (model.get("source" + source + "additive") == 0)
                    {
                    wavebox.add(pcmwave);
                    }
                wavebox.revalidate();
                wavebox.repaint();
                updateTabs();
                }
            };
        model.set("source" + source + "additive", 1);
        vbox.add(comp);
        //vbox.add(pcmwave);
        hbox.add(vbox);

        comp = new LabelledDial("Coarse", this, "source" + source + "coarse", color, 40, 88, 64);
        hbox.add(comp);

        comp = new LabelledDial("Fine", this, "source" + source + "fine", color, 1, 127, 64);
        hbox.add(comp);

        comp = new LabelledDial("Fixed", this, "source" + source + "fixedkey", color, 0, 108 - 20)
            {
            public String map(int val) { return (val == 0 ? "Off" : "" + KEYS[(val - 1) % 12] + ((val - 1) / 12 - 1)); }
            };
        ((LabelledDial)comp).addAdditionalLabel("Key");
        hbox.add(comp);

        comp = new LabelledDial("KS Pitch", this, "source" + source + "kspitch", color, 0, 3)
            {
            public String map(int val) { return CENTS[val]; }
            };
        hbox.add(comp);
                
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addPitchEnv(int source, Color color)
        {
        Category category = new Category(this, "Pitch Env", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
                
        comp = new LabelledDial("Start", this, "source" + source + "pitchenvstartlevel", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);

        comp = new LabelledDial("Attack", this, "source" + source + "pitchenvattacktime", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Time");
        hbox.add(comp);

        comp = new LabelledDial("Attack", this, "source" + source + "pitchenvattacklevel", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);

        comp = new LabelledDial("Decay", this, "source" + source + "pitchenvdecaytime", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Time");
        hbox.add(comp);

        comp = new LabelledDial("Time Velo", this, "source" + source + "pitchenvtimevelosense", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Sense");
        hbox.add(comp);

        comp = new LabelledDial("Level Velo", this, "source" + source + "pitchenvlevelvelosense", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Sense");
        hbox.add(comp);

        EnvelopeDisplay disp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "source" + source + "pitchenvattacktime", "source" + source + "pitchenvdecaytime"  },
            new String[] { "source" + source + "pitchenvstartlevel", "source" + source + "pitchenvattacklevel", null },
            new double[] { 0, 1.0 / 2 / 127.0, 1.0 / 2 / 127.0 },
            new double[] { 1.0 / 126, 1.0 / 126, 1.0 / 126 });
        disp.setAxis(63 / 127.0);               // dunno if this will work, I may be off by a pixel
        hbox.addLast(disp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addDCF(int source, Color color)
        {
        Category category = new Category(this, "DCF", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
                
        comp = new CheckBox("Enable", this, "source" + source + "dcf");
        model.set("source" + source + "dcf", 1);
        vbox.add(comp);

        params = DCF_MODES;
        comp = new Chooser("Mode", this, "source" + source + "dcfmode", params);
        vbox.add(comp);
        hbox.add(vbox);
                
        comp = new LabelledDial("Velo Curve", this, "source" + source + "dcfvelocurve", color, 0, 11, -1);
        hbox.add(comp);

        comp = new LabelledDial("Resonance", this, "source" + source + "dcfresonance", color, 0, 7);
        hbox.add(comp);

        comp = new LabelledDial("Level", this, "source" + source + "dcflevel", color, 0, 7)
            {
            /// FIXME: is this right?  The docs are crazytown
            public String map(int val) { return "" + (7 - val); }
            };
        hbox.add(comp);

        comp = new LabelledDial("Cutoff", this, "source" + source + "dcfcutoff", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Cutoff KS Depth", this, "source" + source + "dcfcutoffksdepth", color, 1, 127, 64);
        //((LabelledDial)comp).addAdditionalLabel("KS Depth");
        hbox.add(comp);

        comp = new LabelledDial("Cutoff Velo Depth", this, "source" + source + "dcfcutoffvelodepth", color, 1, 127, 64);
        //((LabelledDial)comp).addAdditionalLabel("Velo Depth");
        hbox.add(comp);

        comp = new LabelledDial("Velo->Env Level", this, "source" + source + "dcfvelotoenvlevel", color, 1, 127, 64);
        //((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addDCFEnv(int source, Color color)
        {
        Category category = new Category(this, "DCF Env", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
                
        comp = new LabelledDial("Attack", this, "source" + source + "dcfenvattacktime", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Time");
        hbox.add(comp);

        comp = new LabelledDial("Decay 1", this, "source" + source + "dcfenvdecay1time", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Time");
        hbox.add(comp);

        comp = new LabelledDial("Decay 1", this, "source" + source + "dcfenvdecay1level", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);

        comp = new LabelledDial("Decay 2", this, "source" + source + "dcfenvdecay2time", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Time");
        hbox.add(comp);

        comp = new LabelledDial("Decay 2", this, "source" + source + "dcfenvdecay2level", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);

        comp = new LabelledDial("Release", this, "source" + source + "dcfenvreleasetime", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Time");
        hbox.add(comp);

        comp = new LabelledDial("KS->Env", this, "source" + source + "dcfkstoenvattacktime", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Attack Time");
        hbox.add(comp);

        comp = new LabelledDial("KS->Env", this, "source" + source + "dcfkstoenvdecay1time", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Decay 1 Time");
        hbox.add(comp);

        comp = new LabelledDial("Velo->Env", this, "source" + source + "dcfvelotoenvattacktime", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Attack Time");
        hbox.add(comp);

        comp = new LabelledDial("Velo->Env", this, "source" + source + "dcfvelotoenvdecay1time", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Decay1 Time");
        hbox.add(comp);

        comp = new LabelledDial("Env Depth", this, "source" + source + "dcfenvdepth", color, 1, 127, 64);
        hbox.add(comp);


        EnvelopeDisplay disp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "source" + source + "dcfenvattacktime", "source" + source + "dcfenvdecay1time",  "source" + source + "dcfenvdecay2time",  null,                                                                        "source" + source + "dcfenvreleasetime" },
            new String[] { null, null,                                                                   "source" + source + "dcfenvdecay1level", "source" + source + "dcfenvdecay2level", "source" + source + "dcfenvdecay2level", null },
            new double[] { 0, 1.0 / 5 / 127.0, 1.0 / 5 / 127.0, 1.0 / 5 / 127.0, 1.0 / 5, 1.0 / 5 / 127.0 },
            new double[] { 0.5, 1.0, 1.0 / 126, 1.0 / 126, 1.0 / 126, 1.0 / 126 });
        disp.setAxis(63 / 127.0);               // dunno if this will work, I may be off by a pixel
        hbox.addLast(disp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addDCA(int source, Color color)
        {
        Category category = new Category(this, "DCA", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
                
        /// FIXME: I have made this consistent with DCF Velo curve, the documentation might be wrong
        comp = new LabelledDial("Velo Curve", this, "source" + source + "dcavelocurve", color, 0, 11, -1);
        hbox.add(comp);

// I need space
        comp = new LabelledDial("KS->Env Level", this, "source" + source + "dcakstoenvlevel", color, 1, 127, 64);
 //       ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);

        comp = new LabelledDial("Velo->Env Level", this, "source" + source + "dcavelotoenvlevel", color, 1, 127, 64);
//        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addDCAEnv(int source, Color color)
        {
        Category category = new Category(this, "DCA Env", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
                
        comp = new LabelledDial("Attack", this, "source" + source + "dcaenvattacktime", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Time");
        hbox.add(comp);

        comp = new LabelledDial("Decay 1", this, "source" + source + "dcaenvdecay1time", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Time");
        hbox.add(comp);

        comp = new LabelledDial("Decay 1", this, "source" + source + "dcaenvdecay1level", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);

        comp = new LabelledDial("Decay 2", this, "source" + source + "dcaenvdecay2time", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Time");
        hbox.add(comp);

        comp = new LabelledDial("Decay 2", this, "source" + source + "dcaenvdecay2level", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);

        comp = new LabelledDial("Release", this, "source" + source + "dcaenvreleasetime", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Time");
        hbox.add(comp);

        comp = new LabelledDial("KS->Env", this, "source" + source + "dcakstoenvattacktime", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Attack Time");
        hbox.add(comp);

        comp = new LabelledDial("KS->Env", this, "source" + source + "dcakstoenvdecay1time", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Decay 1 Time");
        hbox.add(comp);

        comp = new LabelledDial("KS->Env", this, "source" + source + "dcakstoenvreleasetime", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Release Time");
        hbox.add(comp);

        comp = new LabelledDial("Velo->Env", this, "source" + source + "dcavelotoenvattacktime", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Attack Time");
        hbox.add(comp);

        comp = new LabelledDial("Velo->Env", this, "source" + source + "dcavelotoenvdecay1time", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Decay1 Time");
        hbox.add(comp);

        comp = new LabelledDial("Velo->Env", this, "source" + source + "dcavelotoenvreleasetime", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Release Time");
        hbox.add(comp);

        EnvelopeDisplay disp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "source" + source + "dcaenvattacktime", "source" + source + "dcaenvdecay1time",  "source" + source + "dcaenvdecay2time",  null,                                                                        "source" + source + "dcaenvreleasetime" },
            new String[] { null, null,                                                                   "source" + source + "dcaenvdecay1level", "source" + source + "dcaenvdecay2level", "source" + source + "dcaenvdecay2level", null },
            new double[] { 0, 1.0 / 5 / 127.0, 1.0 / 5 / 127.0, 1.0 / 5 / 127.0, 1.0 / 5, 1.0 / 5 / 127.0 },
            new double[] { 0.5, 1.0, 1.0 / 126, 1.0 / 126, 1.0 / 126, 1.0 / 126 });
        disp.setAxis(63 / 127.0);               // dunno if this will work, I may be off by a pixel
        hbox.addLast(disp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addLFO(int source, Color color)
        {
        Category category = new Category(this, "LFO", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
                
        params = SOURCE_LFO_WAVES;
        comp = new Chooser("Wave", this, "source" + source + "lfowaveform", params);
        vbox.add(comp);
        hbox.add(vbox);
                
        comp = new LabelledDial("Speed", this, "source" + source + "lfospeed", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Delay", this, "source" + source + "lfodelayonset", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Fade In Time", this, "source" + source + "lfofadeintime", color, 0, 127);
//        ((LabelledDial)comp).addAdditionalLabel("Time");
        hbox.add(comp);

        comp = new LabelledDial("Fade In to Speed", this, "source" + source + "lfofadeintospeed", color, 0, 63);
//        ((LabelledDial)comp).addAdditionalLabel("To Speed");
        hbox.add(comp);

        comp = new LabelledDial("Pitch Depth", this, "source" + source + "lfopitchdepth", color, 0, 63);
//        ((LabelledDial)comp).addAdditionalLabel("Depth");
        hbox.add(comp);

        comp = new LabelledDial("Pitch KS", this, "source" + source + "lfopitchks", color, 1, 127, 64);
//        ((LabelledDial)comp).addAdditionalLabel("KS");
        hbox.add(comp);

        comp = new LabelledDial("DCF Depth", this, "source" + source + "lfodcfdepth", color, 0, 63);
//        ((LabelledDial)comp).addAdditionalLabel("Depth");
        hbox.add(comp);

        comp = new LabelledDial("DCF KS", this, "source" + source + "lfodcfks", color, 1, 127, 64);
//        ((LabelledDial)comp).addAdditionalLabel("KS");
        hbox.add(comp);

        comp = new LabelledDial("DCA Depth", this, "source" + source + "lfodcadepth", color, 0, 63);
//        ((LabelledDial)comp).addAdditionalLabel("Depth");
        hbox.add(comp);

        comp = new LabelledDial("DCA KS", this, "source" + source + "lfodcaks", color, 1, 127, 64);
//        ((LabelledDial)comp).addAdditionalLabel("KS");
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    static final double[] FORMANT_WIDTHS = new double[128];
    static final double[] FORMANT_HEIGHTS = new double[128];
    static final String[][] FORMANT_STRINGS = new String[6][128];

    static final double[] HC_WIDTHS = new double[64];
    static final double[] HC_HEIGHTS = new double[64];
    static final double[] LEVEL_HEIGHTS = new double[64];
    static final double[] LOOP_HEIGHTS = new double[64];

    static final String[][] HC_SOFT_STRINGS = new String[6][64];
    static final String[][] HC_LOUD_STRINGS = new String[6][64];
                
    static
        {
        for(int i = 0; i < FORMANT_HEIGHTS.length; i++) FORMANT_HEIGHTS[i] = 1.0 / 127.0;
        FORMANT_WIDTHS[0] = 0;  // (1.0 / 128.0) / 2;
        for(int i = 1; i < FORMANT_WIDTHS.length; i++) FORMANT_WIDTHS[i] = 1.0 / 128.0;
        for(int i = 0; i < FORMANT_STRINGS.length; i++)
            {
            for(int j = 0; j < FORMANT_STRINGS[i].length; j++)
                {
                FORMANT_STRINGS[i][j] = "source" + (i + 1) + "formant" + (j + 1);
                }
            }

        for(int i = 0; i < LOOP_HEIGHTS.length; i++) LOOP_HEIGHTS[i] = 1.0 / 2.0;

        for(int i = 0; i < HC_HEIGHTS.length; i++) HC_HEIGHTS[i] = 1.0 / 127.0;
        HC_WIDTHS[0] = 0; // 1.0 / 64 / 2;
        for(int i = 0; i < LEVEL_HEIGHTS.length; i++) LEVEL_HEIGHTS[i] = 1.0 / 63.0;
        for(int i = 1; i < HC_WIDTHS.length; i++) HC_WIDTHS[i] = 1.0 / 64;
        for(int i = 0; i < HC_SOFT_STRINGS.length; i++)
            {
            for(int j = 0; j < HC_SOFT_STRINGS[i].length; j++)
                {
                HC_SOFT_STRINGS[i][j] = "source" + (i + 1) + "hcsoft" + (j + 1);
                }
            }
        for(int i = 0; i < HC_LOUD_STRINGS.length; i++)
            {
            for(int j = 0; j < HC_LOUD_STRINGS[i].length; j++)
                {
                HC_LOUD_STRINGS[i][j] = "source" + (i + 1) + "hcloud" + (j + 1);
                }
            }
        }
                        
                
    public JComponent addFormantDisplay(int source, Color color)
        {
        Category category = new Category(this, "Formant Bands", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        final boolean[] setting = { false };
        
        for(int i = 0; i < 128; i++)
            {
            String key = "source" + source + "formant" + (i + 1);
            model.set(key, 0);
            model.setMinMaxMetricMinMax(key, 0, 127, 0, 127);
            }
                
        EnvelopeDisplay display = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), new String[128], FORMANT_STRINGS[source - 1], FORMANT_WIDTHS, FORMANT_HEIGHTS)
            {
            int lastFormant = -1;
            boolean mouseDown = false;
                
            // The mouseDown and mouseUp code here enables us to only do undo()
            // ONCE.
            public void mouseDown()
                {
                getUndo().push(getModel());
                getUndo().setWillPush(false);
                mouseDown = true;
                }
                
            public void mouseUp()
                {
                getUndo().setWillPush(true);
                mouseDown = false;
                }
                
            public void updateFromMouse(double x, double y, boolean continuation, MouseEvent evt)
                {
                if (x < 0) x = 0;
                else if (x > 1) x = 1.0;

                if (y <= 0.0) y = 0.0;
                if (y >= 1.0) y = 1.0;
                int formant = (int)(x * 128);
                if (formant == 128) formant = 127;

                // If I am pressing BUTTON3 or SHIFT,
                // and we're continuing
                // and the last formant is not the current formant
                // Then we don't want to change anything
                if (((evt.getModifiersEx() & evt.BUTTON3_DOWN_MASK) != evt.BUTTON3_DOWN_MASK &&
                        (evt.getModifiersEx() & evt.BUTTON2_DOWN_MASK) != evt.BUTTON2_DOWN_MASK &&
                        (evt.getModifiersEx() & evt.SHIFT_DOWN_MASK) != evt.SHIFT_DOWN_MASK)
                    && continuation && lastFormant != formant)
                    {
                    formant = lastFormant;
                    }
                                        
                lastFormant = formant;
                                                        
                setting[0] = true;
                model.set("source" + source + "formant" + (formant + 1), (int)(y * 127));
                boolean push = undo.getWillPush();
                undo.setWillPush(false);
                model.set("source" + source + "formantnumber", formant);
                model.set("source" + source + "formantvalue", (int) y * 127);
                undo.setWillPush(push);
                setting[0] = false;
                }

            public void updateHighlightIndex(int index)
                {
                setting[0] = true;
                boolean push = undo.getWillPush();
                undo.setWillPush(false);
                model.set("source" + source + "formantnumber", index);
                model.set("source" + source + "formantvalue", model.get("source" + source + "formant" + (index + 1)));
                undo.setWillPush(push);
                setting[0] = false;
                }
             
             
            public int highlightIndex(double x, double y, boolean continuation, MouseEvent evt)
                {
                if (x < 0) x = 0;
                if (x > 1.0) x = 1.0;
                int returnval = (int)(x * 128);
                if (returnval == 128) returnval = 127;

                if (mouseDown && continuation && evt != null && returnval != lastFormant &&
                        ((evt.getModifiersEx() & evt.BUTTON3_DOWN_MASK) != evt.BUTTON3_DOWN_MASK &&
                        (evt.getModifiersEx() & evt.BUTTON2_DOWN_MASK) != evt.BUTTON2_DOWN_MASK &&
                        (evt.getModifiersEx() & evt.SHIFT_DOWN_MASK) != evt.SHIFT_DOWN_MASK))
                    {
                    return lastFormant;
                    }
                                                        
                lastFormant = returnval;
                return returnval;
                }

            public int highlightIndex(double x, double y, boolean continuation)
                {
                if (x < 0) x = 0;
                if (x > 1.0) x = 1.0;
                int returnval = (int)(x * 128);
                if (returnval == 128) returnval = 127;
                return returnval;
                }

            public int verticalBorderThickness() { return 4; }
            };
        ((EnvelopeDisplay)display).addVerticalDivider(0.5);
        ((EnvelopeDisplay)display).setPreferredHeight(130);
        ((EnvelopeDisplay)display).setStyle(EnvelopeDisplay.STYLE_LINES);
                        

        comp = new LabelledDial("Number", this, "source" + source + "formantnumber", color, 0, 127, -1) 
            {
            public void setState(int val)
                {
                boolean push = undo.getWillPush();
                undo.setWillPush(false);
                super.setState(val);
                undo.setWillPush(push);
                }
                                
            public void update(String key, Model model)
                {
                super.update(key, model);
                if (!setting[0])
                    {
                    display.setHighlightIndex(model.get(key));
                    display.repaint();
                    }
                }
            };
        model.setStatus("source" + source + "formantnumber", Model.STATUS_RESTRICTED);
        vbox.add(comp);

        comp = new LabelledDial("Value", this, "source" + source + "formantvalue", color, 0, 127)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                if (!setting[0])
                    {
                    int val = display.getHighlightIndex();
                    if (val != EnvelopeDisplay.NO_HIGHLIGHT)
                        {
                        model.set("source" + source + "formant" + (val + 1), model.get(key));
                        display.repaint();
                        }
                    }
                }
            };
        model.setStatus("source" + source + "formantvalue", Model.STATUS_RESTRICTED);
        vbox.add(comp);
                
        hbox.add(vbox);
        
        hbox.addLast(display);
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addHarmonicDisplay(int source, boolean soft, Color color)
        {
        Category category = new Category(this, (soft ? "Soft " : "Loud ") + "Harmonics", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        final boolean[] setting = { false };
        
        for(int i = 0; i < 64; i++)
            {
            String key = "source" + source + "hc" + (soft ? "soft" : "loud") + (i + 1);
            model.set(key, 0);
            model.setMinMaxMetricMinMax(key, 0, 127, 0, 127);
            }
                
        EnvelopeDisplay display = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), new String[64], soft ? HC_SOFT_STRINGS[source - 1] : HC_LOUD_STRINGS[source - 1], HC_WIDTHS, HC_HEIGHTS)
            {
            int lastHc = -1;
            boolean mouseDown = false;
                
            // The mouseDown and mouseUp code here enables us to only do undo()
            // ONCE.
            public void mouseDown()
                {
                getUndo().push(getModel());
                getUndo().setWillPush(false);
                mouseDown = true;
                }
                
            public void mouseUp()
                {
                getUndo().setWillPush(true);
                mouseDown = false;
                }
                        
            public void updateFromMouse(double x, double y, boolean continuation, MouseEvent evt)
                {
                if (x < 0) x = 0;
                else if (x > 1) x = 1.0;

                if (y <= 0.0) y = 0.0;
                if (y >= 1.0) y = 1.0;
                int hc = (int)(x * 64);
                if (hc == 64) hc = 63;

                // If I am pressing BUTTON3 or SHIFT,
                // and we're continuing
                // and the last formant is not the current formant
                // Then we don't want to change anything
                if (((evt.getModifiersEx() & evt.BUTTON3_DOWN_MASK) != evt.BUTTON3_DOWN_MASK &&
                        (evt.getModifiersEx() & evt.BUTTON2_DOWN_MASK) != evt.BUTTON2_DOWN_MASK &&
                        (evt.getModifiersEx() & evt.SHIFT_DOWN_MASK) != evt.SHIFT_DOWN_MASK)
                    && continuation && lastHc != hc)
                    {
                    hc = lastHc;
                    }
                                        
                lastHc = hc;
                                        
                if (!constrainTo(hc))
                    return;

                setting[0] = true;
                model.set("source" + source + "hc" + (soft ? "soft" : "loud") + (hc + 1), (int)(y * 127));
                boolean push = undo.getWillPush();
                undo.setWillPush(false);
                model.set("source" + source + "hc" + (soft ? "soft" : "loud") + "number", hc);
                model.set("source" + source + "hc" + (soft ? "soft" : "loud") + "value", (int) y * 127);
                undo.setWillPush(push);
                setting[0] = false;
                }

            public void updateHighlightIndex(int index)
                {
                setting[0] = true;
                boolean push = undo.getWillPush();
                undo.setWillPush(false);
                model.set("source" + source + "hc" + (soft ? "soft" : "loud") + "number", index);
                model.set("source" + source + "hc" + (soft ? "soft" : "loud") + "value", model.get("source" + source + "hc" + (soft ? "soft" : "loud") + (index + 1)));
                undo.setWillPush(push);
                setting[0] = false;
                }

             
            public int highlightIndex(double x, double y, boolean continuation, MouseEvent evt)
                {
                if (x < 0) x = 0;
                if (x > 1.0) x = 1.0;
                int returnval = (int)(x * 64);
                if (returnval == 64) returnval = 63;

                if (mouseDown && continuation && evt != null && returnval != lastHc &&
                        ((evt.getModifiersEx() & evt.BUTTON3_DOWN_MASK) != evt.BUTTON3_DOWN_MASK &&
                        (evt.getModifiersEx() & evt.BUTTON2_DOWN_MASK) != evt.BUTTON2_DOWN_MASK &&
                        (evt.getModifiersEx() & evt.SHIFT_DOWN_MASK) != evt.SHIFT_DOWN_MASK))
                    {
                    return lastHc;
                    }
                                        
                lastHc = returnval;
                return returnval;
                }

            public int highlightIndex(double x, double y, boolean continuation)
                {
                if (x < 0) x = 0;
                if (x > 1.0) x = 1.0;
                int returnval = (int)(x * 64);
                if (returnval == 64) returnval = 63;
                return returnval;
                }

            public boolean constrainTo(int index) { return _constrainTo(index); }
            public int verticalBorderThickness() { return 4; }
            };
        ((EnvelopeDisplay)display).setPreferredHeight(130);
        ((EnvelopeDisplay)display).setStyle(EnvelopeDisplay.STYLE_LINES);
                        

        comp = new LabelledDial("Number", this, "source" + source + "hc" + (soft ? "soft" : "loud") + "number", color, 0, 63, -1)       
            {
            public void setState(int val)
                {
                boolean push = undo.getWillPush();
                undo.setWillPush(false);
                super.setState(val);
                undo.setWillPush(push);
                }
                                
            public void update(String key, Model model)
                {
                super.update(key, model);
                if (!setting[0])
                    {
                    display.setHighlightIndex(model.get(key));
                    display.repaint();
                    }
                }
            };
        model.setStatus("source" + source + "hc" + (soft ? "soft" : "loud") + "number", Model.STATUS_RESTRICTED);
        vbox.add(comp);

        comp = new LabelledDial("Value", this, "source" + source + "hc" + (soft ? "soft" : "loud") + "value", color, 0, 127)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                if (!setting[0])
                    {
                    int val = display.getHighlightIndex();
                    if (val != EnvelopeDisplay.NO_HIGHLIGHT)
                        {
                        model.set("source" + source +  "hc" + (soft ? "soft" : "loud")  + (val + 1), model.get(key));
                        display.repaint();
                        }
                    }
                }
            };
        model.setStatus("source" + source + "hc" + (soft ? "soft" : "loud") + "value", Model.STATUS_RESTRICTED);
        vbox.add(comp);
                
        hbox.add(vbox);
        
        hbox.addLast(display);
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }



    boolean envelopeSetting;
    boolean highlightSetting;
        
    EnvelopeDisplay envelopeDisplays[][] = new EnvelopeDisplay[6][9];
    static final String[] PARTS = { "rate0", "level0", "rate1", "level1", "rate2", "level2", "rate3", "level3", "loop" };
        
    public static final int BORDER = 8;
    public JComponent addHarmonicEnvelopeDisplay(int source, int partVal, String part, String title, Color color)
        {
        Category category = new Category(this, title, color);

        final String[] keys = new String[64];
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        int max = (part.startsWith("rate") ? 127 : 63);
        
        for(int i = 0; i < 64; i++)
            {
            keys[i] = "source" + source + "hcenv" + (i + 1) + part;
            model.set(keys[i], 0);
            if (partVal == DISPLAY_LOOP) model.setMinMax(keys[i], 0, 2);
            else model.setMinMaxMetricMinMax(keys[i], 0, max, 0, max);
            }
                
        EnvelopeDisplay display = envelopeDisplays[source-1][partVal] = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), new String[64], keys, 
            HC_WIDTHS, (partVal == DISPLAY_LOOP ? LOOP_HEIGHTS : (max == 63 ? LEVEL_HEIGHTS : HC_HEIGHTS)))
            {
            int lastHc = -1;
            boolean mouseDown = false;
                
            // The mouseDown and mouseUp code here enables us to only do undo()
            // ONCE.
            public void mouseDown()
                {
                getUndo().push(getModel());
                getUndo().setWillPush(false);
                mouseDown = true;
                }
                
            public void mouseUp()
                {
                getUndo().setWillPush(true);
                mouseDown = false;
                }
                        
            public void updateFromMouse(double x, double y, boolean continuation, MouseEvent evt)
                {
                if (x < 0) x = 0;
                else if (x > 1) x = 1.0;

                if (y <= 0.0) y = 0.0;
                if (y >= 1.0) y = 1.0;
                int hc = (int)(x * 64);
                if (hc == 64) hc = 63;

                // If I am pressing BUTTON3 or SHIFT,
                // and we're continuing
                // and the last formant is not the current formant
                // Then we don't want to change anything
                if (((evt.getModifiersEx() & evt.BUTTON3_DOWN_MASK) != evt.BUTTON3_DOWN_MASK &&
                        (evt.getModifiersEx() & evt.BUTTON2_DOWN_MASK) != evt.BUTTON2_DOWN_MASK &&
                        (evt.getModifiersEx() & evt.SHIFT_DOWN_MASK) != evt.SHIFT_DOWN_MASK)
                    && continuation && lastHc != hc)
                    {
                    hc = lastHc;
                    }
                                        
                lastHc = hc;
                                        
                if (!constrainTo(hc))
                    return;

                envelopeSetting = true;
                if (partVal == DISPLAY_LOOP) model.set("source" + source + "hcenv" + (hc + 1) + part, (int)(y * 2));
                else model.set("source" + source + "hcenv" + (hc + 1) + part, (int)(y * max));
                boolean push = undo.getWillPush();
                undo.setWillPush(false);
                model.set("source" + source + "hcenv" + "number", hc);
                if (partVal == DISPLAY_LOOP) model.set("source" + source + "hcenv" + part + "value", (int) y * 2);
                else model.set("source" + source + "hcenv" + part + "value", (int) y * max);
                undo.setWillPush(push);
                envelopeSetting = false;
                }

            public void updateHighlightIndex(int index)
                {
                envelopeSetting = true;
                boolean push = undo.getWillPush();
                undo.setWillPush(false);
                model.set("source" + source + "hcenv" + "number", index);
                model.set("source" + source + "hcenv" + part + "value", model.get("source" + source + "hcenv" + (index + 1) + part));
                if (!highlightSetting)
                    {
                    for(int i = 0; i < PARTS.length; i++)
                        {
                        if (i != partVal)       // no recursion
                            {
                            highlightSetting = true;
                            updateEnvelopeDisplays(source, index, partVal);
                            highlightSetting = false;
                            }
                        }
                    }
                undo.setWillPush(push);
                envelopeSetting = false;
                }

             
            public int highlightIndex(double x, double y, boolean continuation, MouseEvent evt)
                {
                if (x < 0) x = 0;
                if (x > 1.0) x = 1.0;
                int returnval = (int)(x * 64);
                if (returnval == 64) returnval = 63;

                if (mouseDown && continuation && evt != null && returnval != lastHc &&
                        ((evt.getModifiersEx() & evt.BUTTON3_DOWN_MASK) != evt.BUTTON3_DOWN_MASK &&
                        (evt.getModifiersEx() & evt.BUTTON2_DOWN_MASK) != evt.BUTTON2_DOWN_MASK &&
                        (evt.getModifiersEx() & evt.SHIFT_DOWN_MASK) != evt.SHIFT_DOWN_MASK))
                    {
                    return lastHc;
                    }
                                        
                lastHc = returnval;
                return returnval;
                }

            public int highlightIndex(double x, double y, boolean continuation)
                {
                if (x < 0) x = 0;
                if (x > 1.0) x = 1.0;
                int returnval = (int)(x * 64);
                if (returnval == 64) returnval = 63;
                return returnval;
                }

            public boolean constrainTo(int index) { return _constrainTo(index); }
            public int verticalBorderThickness() { return 4; } // if (partVal == DISPLAY_LOOP) return super.verticalBorderThickness(); else return 4; }
            };
        if (partVal == DISPLAY_LOOP) ((EnvelopeDisplay)display).setPreferredHeight(70);
        else ((EnvelopeDisplay)display).setPreferredHeight(max + BORDER);
        ((EnvelopeDisplay)display).setStyle(EnvelopeDisplay.STYLE_LINES);
        hbox.addLast(display);
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
    
    public static final int DISPLAY_RATE_0 = 0;
    public static final int DISPLAY_LEVEL_0 = 1;
    public static final int DISPLAY_RATE_1 = 2;
    public static final int DISPLAY_LEVEL_1 = 3;
    public static final int DISPLAY_RATE_2 = 4;
    public static final int DISPLAY_LEVEL_2 = 5;
    public static final int DISPLAY_RATE_3 = 6;
    public static final int DISPLAY_LEVEL_3 = 7;
    public static final int DISPLAY_LOOP = 8;
    
    void updateEnvelopeDisplays(int source, int index, int except)
        {
        source--;
        
        for(int i = 0; i < 9; i++)
            {
            if (i == except) continue;
            if (envelopeDisplays[source][i] != null)
                {
                envelopeDisplays[source][i].setHighlightIndex(index);
                envelopeDisplays[source][i].repaint();
                }
            }
        }
        
    int getHighlightIndex(int source, int part)
        {
        source--;
        
        if (envelopeDisplays[source][part] != null)
            {
            return envelopeDisplays[source][part].getHighlightIndex();
            }
        else return EnvelopeDisplay.NO_HIGHLIGHT;
        }
    
    public JComponent addHarmonicsEnvelopeHeader(int source, Color color)
        {
        Category category = new Category(this, "Harmonics Envelope", color);
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();


        comp = new LabelledDial("Number", this, "source" + source + "hcenv" + "number", color, 0, 63, -1)       
            {
            public void setState(int val)
                {
                boolean push = undo.getWillPush();
                undo.setWillPush(false);
                super.setState(val);
                undo.setWillPush(push);
                }
                                
            public void update(String key, Model model)
                {
                super.update(key, model);
                if (!envelopeSetting)
                    {
                    updateEnvelopeDisplays(source, model.get(key), -1);
                    envelopeSetting = true;
                    model.set("source" + source + "hcenv" + "rate0value", model.get("source" + source + "hcenv" + (model.get(key) + 1) + "rate0"));
                    model.set("source" + source + "hcenv" + "level0value", model.get("source" + source + "hcenv" + (model.get(key) + 1) + "level0"));
                    model.set("source" + source + "hcenv" + "rate1value", model.get("source" + source + "hcenv" + (model.get(key) + 1) + "rate1"));
                    model.set("source" + source + "hcenv" + "level1value", model.get("source" + source + "hcenv" + (model.get(key) + 1) + "level1"));
                    model.set("source" + source + "hcenv" + "rate2value", model.get("source" + source + "hcenv" + (model.get(key) + 1) + "rate2"));
                    model.set("source" + source + "hcenv" + "level2value", model.get("source" + source + "hcenv" + (model.get(key) + 1) + "level2"));
                    model.set("source" + source + "hcenv" + "rate3value", model.get("source" + source + "hcenv" + (model.get(key) + 1) + "rate3"));
                    model.set("source" + source + "hcenv" + "level3value", model.get("source" + source + "hcenv" + (model.get(key) + 1) + "level3"));
                    model.set("source" + source + "hcenv" + "loopvalue", model.get("source" + source + "hcenv" + (model.get(key) + 1) + "loop"));
                    envelopeSetting = false;
                    }
                }
            };
        model.setStatus("source" + source + "hcenv" + "number", Model.STATUS_RESTRICTED);
        hbox.add(comp);
                
        params = ENV_LOOPS;
        comp = new Chooser("Loop", this, "source" + source + "hcenv" + "loopvalue", params)     
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                if (!envelopeSetting)
                    {
                    int val = getHighlightIndex(source, DISPLAY_RATE_0);
                    if (val != EnvelopeDisplay.NO_HIGHLIGHT)
                        {
                        model.set("source" + source +  "hcenv" + (val + 1) + "loop", model.get(key));
                        envelopeDisplays[source-1][DISPLAY_RATE_0].repaint();
                        }
                    }
                }
            };
        model.setStatus("source" + source + "hcenv" + "loopvalue", Model.STATUS_RESTRICTED);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Rate 0", this, "source" + source + "hcenv" + "rate0value", color, 0, 127)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                if (!envelopeSetting)
                    {
                    int val = getHighlightIndex(source, DISPLAY_RATE_0);
                    if (val != EnvelopeDisplay.NO_HIGHLIGHT)
                        {
                        model.set("source" + source +  "hcenv" + (val + 1) + "rate0", model.get(key));
                        envelopeDisplays[source-1][DISPLAY_RATE_0].repaint();
                        }
                    }
                }
            };
        model.setStatus("source" + source + "hcenv" + "rate0value", Model.STATUS_RESTRICTED);
        hbox.add(comp);
                
        comp = new LabelledDial("Level 0", this, "source" + source + "hcenv" + "level0value", color, 0, 63)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                if (!envelopeSetting)
                    {
                    int val = getHighlightIndex(source, DISPLAY_LEVEL_0);
                    if (val != EnvelopeDisplay.NO_HIGHLIGHT)
                        {
                        model.set("source" + source +  "hcenv" + (val + 1) + "level0", model.get(key));
                        envelopeDisplays[source-1][DISPLAY_LEVEL_0].repaint();
                        }
                    }
                }
            };
        model.setStatus("source" + source + "hcenv" + "level0value", Model.STATUS_RESTRICTED);
        hbox.add(comp);


        comp = new LabelledDial("Rate 1", this, "source" + source + "hcenv" + "rate1value", color, 0, 127)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                if (!envelopeSetting)
                    {
                    int val = getHighlightIndex(source, DISPLAY_RATE_1);
                    if (val != EnvelopeDisplay.NO_HIGHLIGHT)
                        {
                        model.set("source" + source +  "hcenv" + (val + 1) + "rate1", model.get(key));
                        envelopeDisplays[source-1][DISPLAY_RATE_1].repaint();
                        }
                    }
                }
            };
        model.setStatus("source" + source + "hcenv" + "rate1value", Model.STATUS_RESTRICTED);
        hbox.add(comp);
                
        comp = new LabelledDial("Level 1", this, "source" + source + "hcenv" + "level1value", color, 0, 63)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                if (!envelopeSetting)
                    {
                    int val = getHighlightIndex(source, DISPLAY_LEVEL_1);
                    if (val != EnvelopeDisplay.NO_HIGHLIGHT)
                        {
                        model.set("source" + source +  "hcenv" + (val + 1) + "level1", model.get(key));
                        envelopeDisplays[source-1][DISPLAY_LEVEL_1].repaint();
                        }
                    }
                }
            };
        model.setStatus("source" + source + "hcenv" + "level1value", Model.STATUS_RESTRICTED);
        hbox.add(comp);
                

        comp = new LabelledDial("Rate 2", this, "source" + source + "hcenv" + "rate2value", color, 0, 127)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                if (!envelopeSetting)
                    {
                    int val = getHighlightIndex(source, DISPLAY_RATE_2);
                    if (val != EnvelopeDisplay.NO_HIGHLIGHT)
                        {
                        model.set("source" + source +  "hcenv" + (val + 1) + "rate2", model.get(key));
                        envelopeDisplays[source-1][DISPLAY_RATE_2].repaint();
                        }
                    }
                }
            };
        model.setStatus("source" + source + "hcenv" + "rate2value", Model.STATUS_RESTRICTED);
        hbox.add(comp);
                
        comp = new LabelledDial("Level 2", this, "source" + source + "hcenv" + "level2value", color, 0, 63)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                if (!envelopeSetting)
                    {
                    int val = getHighlightIndex(source, DISPLAY_LEVEL_2);
                    if (val != EnvelopeDisplay.NO_HIGHLIGHT)
                        {
                        model.set("source" + source +  "hcenv" + (val + 1) + "level2", model.get(key));
                        envelopeDisplays[source-1][DISPLAY_LEVEL_2].repaint();
                        }
                    }
                }
            };
        model.setStatus("source" + source + "hcenv" + "level2value", Model.STATUS_RESTRICTED);
        hbox.add(comp);
                

        comp = new LabelledDial("Rate 3", this, "source" + source + "hcenv" + "rate3value", color, 0, 127)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                if (!envelopeSetting)
                    {
                    int val = getHighlightIndex(source, DISPLAY_RATE_3);
                    if (val != EnvelopeDisplay.NO_HIGHLIGHT)
                        {
                        model.set("source" + source +  "hcenv" + (val + 1) + "rate3", model.get(key));
                        envelopeDisplays[source-1][DISPLAY_RATE_3].repaint();
                        }
                    }
                }
            };
        model.setStatus("source" + source + "hcenv" + "rate3value", Model.STATUS_RESTRICTED);
        hbox.add(comp);
                
        comp = new LabelledDial("Level 3", this, "source" + source + "hcenv" + "level3value", color, 0, 63)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                if (!envelopeSetting)
                    {
                    int val = getHighlightIndex(source, DISPLAY_LEVEL_3);
                    if (val != EnvelopeDisplay.NO_HIGHLIGHT)
                        {
                        model.set("source" + source +  "hcenv" + (val + 1) + "level3", model.get(key));
                        envelopeDisplays[source-1][DISPLAY_LEVEL_3].repaint();
                        }
                    }
                }
            };
        model.setStatus("source" + source + "hcenv" + "level3value", Model.STATUS_RESTRICTED);
        hbox.add(comp);
           
        vbox = new VBox();     
        EnvelopeDisplay disp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "source" + source + "hcenv" + "rate0value", "source" + source + "hcenv" + "rate1value",  "source" + source + "hcenv" + "rate2value",  "source" + source + "hcenv" + "rate3value" },
            new String[] { null, "source" + source + "hcenv" + "level0value", "source" + source + "hcenv" + "level1value",  "source" + source + "hcenv" + "level2value",  "source" + source + "hcenv" + "level3value" },
            new double[] { 0, 1.0 / 4 / 127.0, 1.0 / 4 / 127.0, 1.0 / 4 / 127.0, 1.0 / 4 / 127.0 },
            new double[] { 0, 1.0 / 63, 1.0 / 63, 1.0 / 63, 1.0 / 63 })
            {
            public int verticalBorderThickness() { return 4; }
            };
        disp.setPreferredHeight(70);
        vbox.add(disp);
        hbox.addLast(vbox);
                
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
                    
    public JFrame sprout()     
        {
        JFrame frame = super.sprout();
        addKawaiK5000Menu();
        return frame;
        }
        
    public static final String[] MENU_CONSTRAINTS = { "All", "Odd", "Even", "First Third", "Second Third", "Third Third", 
        "Octaves", "Fifths", "Major Thirds", "Minor Sevenths", "Major Seconds", "Major Sevenths" };
        
    public void addKawaiK5000Menu()
        {
        JMenu menu = new JMenu("K5000");
        menubar.add(menu);
        JMenu constrainMenu = new JMenu("Constrain Harmonics...");
                
        ButtonGroup group = new ButtonGroup();
        for(int i = 0; i <= MAJOR_SEVENTH; i++)
            {
            final int _i = i;
            JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(MENU_CONSTRAINTS[i]);
            if (i == 0) menuItem.setSelected(true);
            group.add(menuItem);
            menuItem.addActionListener(new ActionListener()
                {
                public void actionPerformed(ActionEvent e)
                    {
                    constraints = _i;
                    repaint();
                    }
                });
            constrainMenu.add(menuItem);
            }
        menu.add(constrainMenu);
        }



    int constraints;
        
    boolean _constrainTo(int index)
        {
        switch(constraints)
            {
            case ALL:
            {
            return true;
            }
            case ODD:
            {
            return ((index & 0x1) == 0x0);  // yes, it looks backwards but it isn't
            }
            case EVEN:
            {
            return ((index & 0x1) == 0x1);  // yes, it looks backwards but it isn't
            }
            case FIRST_THIRD:
            {
            return (index % 3) == 0;
            }
            case SECOND_THIRD:
            {
            return (index % 3) == 1;
            }
            case THIRD_THIRD:
            {
            return (index % 3) == 2;
            }
            case OCTAVE:
            {
            for(int i = 0; i < OCTAVE_HARMONICS.length; i++)
                if (OCTAVE_HARMONICS[i] == (index + 1)) return true;
            return false; 
            }
            case FIFTH:
            {
            for(int i = 0; i < FIFTH_HARMONICS.length; i++)
                if (FIFTH_HARMONICS[i] == (index + 1)) return true;
            return false; 
            }
            case MAJOR_THIRD:
            {
            for(int i = 0; i < MAJOR_THIRD_HARMONICS.length; i++)
                if (MAJOR_THIRD_HARMONICS[i] == (index + 1)) return true;
            return false;                                           
            }
            case MINOR_SEVENTH:
            {
            for(int i = 0; i < MINOR_SEVENTH_HARMONICS.length; i++)
                if (MINOR_SEVENTH_HARMONICS[i] == (index + 1)) return true;
            return false;                                           
            }
            case MAJOR_SECOND:
            {
            for(int i = 0; i < MAJOR_SECOND_HARMONICS.length; i++)
                if (MAJOR_SECOND_HARMONICS[i] == (index + 1)) return true;
            return false;                                           
            }
            case MAJOR_SEVENTH:
            {
            for(int i = 0; i < MAJOR_SEVENTH_HARMONICS.length; i++)
                if (MAJOR_SEVENTH_HARMONICS[i] == (index + 1)) return true;
            return false;                                           
            }
            }
        return false;
        }


    public String getPatchName(Model model) { return model.get("name", "INIT    "); }

    public static final int SINGLE_MODE = 0x01;
    public static final int BANK_A_MSB = 64;
    public static final int BANK_D_MSB = 66;
    public static final int BANK_E_MSB = 67;
    public static final int BANK_F_MSB = 68;
    public static final int BANK_M_MSB = 65;                // only has 64 PC values

    public void changePatch(Model tempModel)
        {
        byte BB = (byte)tempModel.get("bank");
        byte NN = (byte)tempModel.get("number");
                
        int bankMSB = (BB == 0 ? BANK_A_MSB : (BB == 1 ? BANK_D_MSB : (BB == 2 ? BANK_E_MSB : BANK_F_MSB)));
        int bankLSB = 0;
        
        try 
            {
            // Change to Single Mode.  See p. 36  NOTE there is no "Change to Multi / Combo Mode" I think, ugh.
            // FIXME: I don't know if this is necessary
            tryToSendSysex(new byte[] { (byte)(0xF0), 0x40, (byte)getChannelOut(), 0x31, 0x00, 0x0a, SINGLE_MODE });
                
            // Bank Change
            tryToSendMIDI(new ShortMessage(ShortMessage.CONTROL_CHANGE, getChannelOut(), 32, bankMSB));
            tryToSendMIDI(new ShortMessage(ShortMessage.CONTROL_CHANGE, getChannelOut(), 0, bankLSB));
                
            // PC
            tryToSendMIDI(new ShortMessage(ShortMessage.PROGRAM_CHANGE, getChannelOut(), NN, 0));
            }
        catch (Exception e) { Synth.handleException(e); }
        }

    
    public Model getNextPatchLocation(Model model)
        {
        int bank = model.get("bank");
        int number = model.get("number");
                
        number++;
        if (number >= 128)
            {
            bank++;
            number = 0;
            if (bank >= 4)                  // FIXME: This is going to go up through banks E and F, which we may not have....
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
        
        String n = null;
        int number = model.get("number") + 1;
        if (number < 10) n = "00" + number;
        else if (number < 100) n = "0" + number;
        else n = "" + number; 
        return BANKS[model.get("bank")] + n;
        }

    // Verify that all the parameters are within valid values, and tweak them if not.
    public void revise()
        {
        // check the easy stuff -- out of range parameters
        super.revise();

        String nm = model.get("name", "INIT    ");
        String newnm = revisePatchName(nm);
        if (!nm.equals(newnm))
            model.set("name", newnm);
        }
        

    public static String getSynthName() { return "Kawai K5000S/K5000R"; }
    
    // The K5000 can't send to temporary memory.  Presently we are only sending as individual parameters.
    public boolean getSendsAllParametersAsDump() { return false; }


    public int[] getNameAsBytes()
        {
        String name = model.get("name", "        ") + "        ";
        int[] bytes = new int[8];
        for(int i = 0; i < 8; i ++)
            {
            bytes[i] = name.charAt(i);
            }
        return bytes;
        }


    final int[] BANK_VALS = { 0, 2, 3, 4 };
    public Object[] emitAll(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        if (toWorkingMemory) return new Object[0];                      // For the time being
        
        if (tempModel == null)
            tempModel = getModel();
    
        int sources = model.get("srctype");

        int bank = model.get("bank");
        // bank     Name    data[8]
        // 0        A               0
        // 1        D               2
        // 2        E               3
        // 3        F               4
    
    
        byte[] data = new byte[10 + 81 + sources * 86 + sources * 806 + 1];
        // Format is
        // F0 40 CHANNEL 20 00 0a 00 BANK PATCHNUM SINGLE_CHECKSUM EFFECT/COMMON SOURCE* WAVEKIT* F7
    
        data[0] = (byte)0xF0;
        data[1] = (byte)0x40;                                               // Kawai
        data[2] = (byte)getChannelOut();                    // Channel
        data[3] = (byte)0x20;
        data[4] = (byte)0x00;
        data[5] = (byte)0x0A;
        data[6] = (byte)0x00;
        data[7] = (byte)BANK_VALS[bank];                    // bank
        data[8] = (byte)tempModel.get("number");            // patch
    
        int pos = 9;
        pos++;                      // skip checksum space
    
        int start = pos;

        // LOAD EFFECTS
    
        data[pos++] = (byte)tempModel.get("algorithm");
        int reverbtype = tempModel.get("reverbtype");
        data[pos++] = (byte)reverbtype;
        data[pos++] = (byte)tempModel.get("reverb" + (reverbtype + 1) + "drywet1");
        data[pos++] = (byte)tempModel.get("reverb" + (reverbtype + 1) + "para1");
        data[pos++] = (byte)tempModel.get("reverb" + (reverbtype + 1) + "para2");
        data[pos++] = (byte)tempModel.get("reverb" + (reverbtype + 1) + "para3");
        data[pos++] = (byte)tempModel.get("reverb" + (reverbtype + 1) + "para4");
    
        for(int effect = 1; effect <= 4; effect++)                                  // NOTE <=
            {
            int effecttype = tempModel.get("effect" + effect + "type");
            data[pos++] = (byte)(effecttype + 11);							// effects start at 11
            data[pos++] = (byte)tempModel.get("effect" + effect + "type" + (effecttype + 1) + "depth");
            data[pos++] = (byte)tempModel.get("effect" + effect + "type" + (effecttype + 1) + "para1");
            data[pos++] = (byte)tempModel.get("effect" + effect + "type" + (effecttype + 1) + "para2");
            data[pos++] = (byte)tempModel.get("effect" + effect + "type" + (effecttype + 1) + "para3");
            data[pos++] = (byte)tempModel.get("effect" + effect + "type" + (effecttype + 1) + "para4");
            }
                
        for(int freq = 1; freq <= 7; freq++)                                            // NOTE <=
            {
            data[pos++] = (byte)tempModel.get("geqfreq" + freq);
            }
                
        // LOAD COMMON

        data[pos++] = 0;                                                        // the "drum mark", see page 13 of sysex spec, line 39
        
        int[] name = getNameAsBytes();
        for(int i = 0; i < 8; i++)
            {
            data[pos++] = (byte)name[i];
            }
        data[pos++] = (byte)tempModel.get("volume");
        data[pos++] = (byte)tempModel.get("poly");
        data[pos++] = 0;                                                                // "no use", see page 14 of sysex spec, line 50
        
        data[pos++] = (byte)sources;

        int srcmute = (byte)(tempModel.get("srcmute1") << 0) |
            (tempModel.get("srcmute2") << 1) |
            (tempModel.get("srcmute3") << 2) |
            (tempModel.get("srcmute4") << 3) |
            (tempModel.get("srcmute5") << 4) |
            (tempModel.get("srcmute6") << 5);
        data[pos++] = (byte)srcmute;                                                    // bitpacking

        data[pos++] = (byte)tempModel.get("am");
        data[pos++] = (byte)tempModel.get("effectcontrol1source");
        data[pos++] = (byte)tempModel.get("effectcontrol1destination");
        data[pos++] = (byte)tempModel.get("effectcontrol1depth");
        data[pos++] = (byte)tempModel.get("effectcontrol2source");
        data[pos++] = (byte)tempModel.get("effectcontrol2destination");
        data[pos++] = (byte)tempModel.get("effectcontrol2depth");
        data[pos++] = (byte)tempModel.get("portamento");
        data[pos++] = (byte)tempModel.get("portamentospeed");
        for(int c = 1; c <= 4; c++)
            {
            data[pos++] = (byte)tempModel.get("macrocontroller" + c + "parameter1");
            data[pos++] = (byte)tempModel.get("macrocontroller" + c + "parameter2");
            }
        for(int c = 1; c <= 4; c++)
            {
            data[pos++] = (byte)tempModel.get("macrocontroller" + c + "depth1");
            data[pos++] = (byte)tempModel.get("macrocontroller" + c + "depth2");
            }
                
        data[pos++] = (byte)tempModel.get("sw1parameter");
        data[pos++] = (byte)tempModel.get("sw2parameter");
        data[pos++] = (byte)tempModel.get("fsw1parameter");
        data[pos++] = (byte)tempModel.get("fsw2parameter");

        start = pos;

        // LOAD SOURCE DATA
    
        for(int i = 1; i <= sources; i++)                                           // note <=
            {
            for(int j = 0; j < sourceParams.length; j++)
                {
                String key = sourceParams[j];

                if (j == 2)             // velo sw and velo
                    {
                    data[pos++] = (byte)((tempModel.get("source" + i + "velosw") << 5) | 
                        (tempModel.get("source" + i + "velo")));
                    }
                else if (j == 28) // wavekitmsb
                    {
                    int wavekit = tempModel.get("source" + i + "wavekit");
                    boolean additive = (tempModel.get("source" + i + "additive") == 1);
                    data[pos++] = (byte)(additive ? 4 : (wavekit >>> 7));                           // additive is 512
                    }
                else if (j == 29) // wavekitlsb
                    {
                    int wavekit = tempModel.get("source" + i + "wavekit");
                    boolean additive = (tempModel.get("source" + i + "additive") == 1);
                    data[pos++] = (byte)(additive ? 0 : (wavekit & 127));                           // additive is 512
                    }
                else
                    {
                    data[pos++] = (byte)(tempModel.get("source" + i + key));
                    }
                }
            }

        // COMMON AND SOURCE DATA CHECKSUM
    
        data[9] = checksum(data, 10, pos);

        start = pos;

    

        // LOAD WAVE KIT DATA
            
        for(int i = 1; i <= sources; i++)                                           // note <=
            {
            int checksumpos = pos;
            pos++;
        
            for(int j = 1; j < addWaveKitParams.length; j++)                // skip "--", checksum
                {
                String key = addWaveKitParams[j];
                data[pos++] = (byte)(tempModel.get("source" + i + key));
                }

            start = pos;

            for(int j = 0; j < addWaveHCSoftParams.length; j++)
                {
                String key = addWaveHCSoftParams[j];
                data[pos++] = (byte)(tempModel.get("source" + i + key));
                }

            start = pos;

            for(int j = 0; j < addWaveHCLoudParams.length; j++)
                {
                String key = addWaveHCLoudParams[j];
                data[pos++] = (byte)(tempModel.get("source" + i + key));
                }

            start = pos;

            for(int j = 0; j < addWaveFormantFilterParams.length; j++)
                {
                String key = addWaveFormantFilterParams[j];
                data[pos++] = (byte)(tempModel.get("source" + i + key));
                }

            start = pos;

            for(int j = 0; j < addWaveHarmonicEnvelopeParams.length; j++)
                {
                String key = addWaveHarmonicEnvelopeParams[j];
                if (key.endsWith("level1"))
                    {
                    int num = StringUtility.getFirstInt(key);
                    data[pos++] = (byte)(tempModel.get("source" + i + key) |
                        (tempModel.get("source" + i + "hcenv" + num + "loop") == 1 ? 0 : 64)); // lp1
                    }
                else if (key.endsWith("level2"))
                    {
                    int num = StringUtility.getFirstInt(key);
                    data[pos++] = (byte)(tempModel.get("source" + i + key) |
                        (tempModel.get("source" + i + "hcenv" + num + "loop") == 0 ? 0 : 64)); // off
                    }
                else
                    {
                    data[pos++] = (byte)(tempModel.get("source" + i + key));
                    }
                }
            start = pos;
                        
            data[checksumpos] = checksum(data, checksumpos + 1, pos);

            start = pos;
            }


        data[data.length - 1] = (byte)0xF7;
        return new Object[] { data };
        }



    public int parse(byte[] result, boolean fromFile)
        {
        int bank = result[7];
        if (bank == 1)  // Bank B, we don't do that.  
            return PARSE_FAILED;
        if (bank > 0) bank -= 1;                        // bank = 1 is invalid.  Shift all others down one.
        // bank     Name    result[8]
        // 0        A               0
        // 1        D               2
        // 2        E               3
        // 3        F               4

        model.set("bank", bank);
        model.set("number", result[8]);
                
        int pos = 10;

        int numSources = result[pos + 50];
        if (numSources < 2 || numSources > 6) return PARSE_FAILED;              
                        
        // okay, here we go
                        
        model.set("algorithm", result[pos++]);
        int reverbtype = result[pos++];
        model.set("reverbtype", reverbtype);
        model.set("reverb" + (reverbtype + 1) + "drywet1", result[pos++]);
        model.set("reverb" + (reverbtype + 1) + "para1", result[pos++]);
        model.set("reverb" + (reverbtype + 1) + "para2", result[pos++]);
        model.set("reverb" + (reverbtype + 1) + "para3", result[pos++]);
        model.set("reverb" + (reverbtype + 1) + "para4", result[pos++]);

        for(int effect = 1; effect <= 4; effect++)                                  // NOTE <=
            {
            int effecttype = result[pos++] - 11;					// effects go 11...47
            model.set("effect" + effect + "type", effecttype);
            model.set("effect" + effect + "type" + (effecttype + 1) + "depth", result[pos++]);
            model.set("effect" + effect + "type" + (effecttype + 1) + "para1", result[pos++]);
            model.set("effect" + effect + "type" + (effecttype + 1) + "para2", result[pos++]);
            model.set("effect" + effect + "type" + (effecttype + 1) + "para3", result[pos++]);
            model.set("effect" + effect + "type" + (effecttype + 1) + "para4", result[pos++]);
            }
                
        for(int freq = 1; freq <= 7; freq++)                                            // NOTE <=
            {
            model.set("geqfreq" + freq, result[pos++]);
            }
                
        // LOAD COMMON

        pos++;                                                        // the "drum mark", see page 13 of sysex spec, line 39
        
        byte[] name = new byte[8];
        for(int i = 0; i < 8; i++)
            {
            name[i] = result[pos++];
            }
        model.set("name", new String(name));
        model.set("volume", result[pos++]);
        model.set("poly", result[pos++]);
        pos++;                                                                 // "no use", see page 14 of sysex spec, line 50
        int srctype = result[pos++];
        //System.err.println("num sources " + srctype);
        model.set("srctype", srctype);

        int srcmute = result[pos++];                                            // ugh, bitpacking
        model.set("srcmute1", 1 - ((srcmute >>> 0) & 1));
        model.set("srcmute2", 1 - ((srcmute >>> 1) & 1));
        model.set("srcmute3", 1 - ((srcmute >>> 2) & 1));
        model.set("srcmute4", 1 - ((srcmute >>> 3) & 1));
        model.set("srcmute5", 1 - ((srcmute >>> 4) & 1));
        model.set("srcmute6", 1 - ((srcmute >>> 5) & 1));

        model.set("am", result[pos++]);
        model.set("effectcontrol1source", result[pos++]);
        model.set("effectcontrol1destination", result[pos++]);
        model.set("effectcontrol1depth", result[pos++]);
        model.set("effectcontrol2source", result[pos++]);
        model.set("effectcontrol2destination", result[pos++]);
        model.set("effectcontrol2depth", result[pos++]);
        model.set("portamento", result[pos++]);
        model.set("portamentospeed", result[pos++]);
        for(int c = 1; c <= 4; c++)
            {
            model.set("macrocontroller" + c + "parameter1", result[pos++]);
            model.set("macrocontroller" + c + "parameter2", result[pos++]);
            }
        for(int c = 1; c <= 4; c++)
            {
            model.set("macrocontroller" + c + "depth1", result[pos++]);
            model.set("macrocontroller" + c + "depth2", result[pos++]);
            }
                
        model.set("sw1parameter", result[pos++]);
        model.set("sw2parameter", result[pos++]);
        model.set("fsw1parameter", result[pos++]);
        model.set("fsw2parameter", result[pos++]);


        // LOAD SOURCE result
        
        boolean[] additiveSource = new boolean[6];
    
        for(int i = 1; i <= srctype; i++)                                           // note <=
            {
            for(int j = 0; j < sourceParams.length; j++)
                {
                String key = sourceParams[j];
                if (j == 2)             // velo sw and velo
                    {
                    int val = result[pos++];
                    model.set("source" + i + "velosw", (val >>> 5) & 3);
                    model.set("source" + i + "velo", val & 31);
                    }
                else if (j == 28) // wavekitmsb
                    {
                    int msb = result[pos++];
                    int lsb = result[pos++];
                    int val = (msb << 7) | lsb;
                    if (val == 512)
                        {
                        model.set("source" + i + "additive", 1);
                        model.set("source" + i + "wavekit", 0);
                        additiveSource[i - 1] = true;
                        }
                    else
                        {
                        model.set("source" + i + "additive", 0);
                        model.set("source" + i + "wavekit", val);
                        }
                    }
                else if (j == 29) // wavekitlsb
                    {
                    // do nothing
                    }
                else
                    {
                    model.set("source" + i + key, result[pos++]);
                    }
                }
            }
    
        // COMMON AND SOURCE DATA CHECKSUM
        
		//System.err.println("Checksum should be " + checksum(result, 10, pos));
        // skip

        // LOAD WAVE KIT DATA
            
        for(int i = 1; i <= srctype; i++)                                           // note <=
            {
            if (!additiveSource[i - 1]) continue;
            
            if ((result[pos] & 0xFF) == 0xF7)	// we ran out!!!!
            	{
            	System.err.println("ERROR (KawaiK5000.parse()): early truncation of wavekits!");
            	return PARSE_FAILED;		// I think this should be a failure
            	}
            
    		//System.err.println("Wave Kit " + i + " Checksum " + result[pos]);
            pos++;     
            int start = pos;                                                                                         // checksum
        
            for(int j = 1; j < addWaveKitParams.length; j++)                // skip "--", checksum
                {
                String key = addWaveKitParams[j];
                model.set("source" + i + key, result[pos++]);
                }

            for(int j = 0; j < addWaveHCSoftParams.length; j++)
                {
                String key = addWaveHCSoftParams[j];
                model.set("source" + i + key, result[pos++]);
                }

            for(int j = 0; j < addWaveHCLoudParams.length; j++)
                {
                String key = addWaveHCLoudParams[j];
                model.set("source" + i + key, result[pos++]);
                }

            for(int j = 0; j < addWaveFormantFilterParams.length; j++)
                {
                String key = addWaveFormantFilterParams[j];
                model.set("source" + i + key, result[pos++]);
                }

            for(int j = 0; j < addWaveHarmonicEnvelopeParams.length; j++)
                {
                String key = addWaveHarmonicEnvelopeParams[j];
                if (key.endsWith("level1"))
                    {
                    int num = StringUtility.getFirstInt(key);
                    int val1 = result[pos];                     // level 1
                    int val2 = result[pos + 2];         // level 2 (skip rate1)
                    pos++;
                    model.set("source" + i + key, val1 & 63);
                    model.set("source" + i + "hcenv" + num + "loop", 
                        (val2 & 64) == 0 ? 0 : ((val1 & 64) == 0 ? 1 : 2));
                    }
                else if (key.endsWith("level2"))
                    {
                    int val1 = result[pos++];           // level 2
                    model.set("source" + i + key, val1 & 63);
                    }
                else
                    {
                    model.set("source" + i + key, result[pos++]);
                    }
                }
			//System.err.println("Wavekit checksum should be " + checksum(result, start, pos));
            }
        
            
        revise();
        return PARSE_SUCCEEDED;
        }
        
    // Computes the checksum on data[start] ... data[end - 1]
    public byte checksum(byte[] data, int start, int end)
        {
        // FIXME: the text appears to suggest that it is 0xa5 + sum mod 127
        int sum = 0xa5;
        for(int i = start; i < end; i++)
            {
            sum += (data[i] & 0xFF);
            }
        return (byte)(sum & 127);
        }

    public Object[] emitAll(String key)
        {
        int[] data = null;              // { sub1, sub2, sub3, sub4, sub5, datahi, datalo };
        int[][] data8 = null;
        int sub5 = 0;
        Integer index = null;
        
        if (key.equals("bank")) return new Object[0];
        else if (key.equals("number")) return new Object[0];
        else if (key.startsWith("dial"))
            {
            int dial = StringUtility.getFirstInt(key) - 1;
            return buildCC(getChannelOut(), DIAL_CCS[dial], model.get(key));       
            }
        else if (key.startsWith("source"))
            {
            // Is this source being used?
            int i = StringUtility.getFirstInt(key);
            if (i > model.get("srctype"))           // srctype (num sources) goes 2...6
                {
                return new Object[0];                   // not gonna do it
                }
            // This is for velo and velosw, which must be bitpacked together
            else if (key.startsWith("source1velo") || key.startsWith("source2velo") || key.startsWith("source3velo") || key.startsWith("source4velo") || key.startsWith("source5velo") || key.startsWith("source6velo"))
                {
                int source = StringUtility.getFirstInt(key);
                data = new int[] { 0x01, 0x01, (source - 1), 0x00, 0x02, 0x00, 
                    (model.get("source" + source + "velosw") << 5) | 
                    model.get("source" + source + "velo") };
                }
            // This must be sent 14-bit
            else if (key.startsWith("source1wavekit") || key.startsWith("source2wavekit") || key.startsWith("source3wavekit") || key.startsWith("source4wavekit") || key.startsWith("source5wavekit") || key.startsWith("source6wavekit") ||
                key.startsWith("source1additive") || key.startsWith("source2additive") || key.startsWith("source3additive") || key.startsWith("source4additive") || key.startsWith("source5additive") || key.startsWith("source6additive"))
                {
                int source = StringUtility.getFirstInt(key);
                boolean additive = (model.get("source" + source + "additive") == 1);
                int val = model.get(key);
                data = new int[] { 0x01, 0x01, (source - 1), 0x00, 0x1c, additive ? 4 : (val >>> 7), additive ? 0 : (val & 127)};               // additive is 512
                }
            // This is for fixedkey, which has a hole its range that we must deal wtih
            else if (key.startsWith("source1fixedkey") || key.startsWith("source2fixedkey") || key.startsWith("source3fixedkey") || key.startsWith("source4fixedkey") || key.startsWith("source5fixedkey") || key.startsWith("source6fixedkey"))
                {
                // This is stored in Edisyn as 0 ... 108-21, but must be emitted as 0 vs 21...108
                int source = StringUtility.getFirstInt(key);
                int val = model.get(key);
                data = new int[] { 0x01, 0x01, (source - 1), 0x00, 0x20, 0x00, val == 0 ? 0 : val + 20 };
                }
            else
                {
                int source = StringUtility.getFirstInt(key);
                String remainder = StringUtility.removePreambleAndFirstDigits(key, "source");
                
                if (remainder.equals("hcenvnumber") ||
                    remainder.equals("hcenvlevel0value") || 
                    remainder.equals("hcenvlevel1value") || 
                    remainder.equals("hcenvlevel2value") || 
                    remainder.equals("hcenvlevel3value") || 
                    remainder.equals("hcenvrate0value") || 
                    remainder.equals("hcenvrate1value") || 
                    remainder.equals("hcenvrate2value") || 
                    remainder.equals("hcenvrate3value") || 
                    remainder.equals("hcenvloopvalue") ||
                    remainder.equals("formantnumber") ||
                    remainder.equals("formantvalue") ||
                    remainder.equals("hcsoftnumber") ||
                    remainder.equals("hcsoftvalue") ||
                    remainder.equals("hcloudnumber") ||
                    remainder.equals("hcloudvalue"))
                    return new Object[0];
                
                int val = model.get(key);
                if ((index = (Integer)(sourceParamsToIndex.get(remainder))) != null)
                    {
                    data = new int[] { 0x01, 0x01, (source - 1), 0x00, index.intValue(), 0x00, val };
                    }
                else if ((index = (Integer)(addWaveKitParamsToIndex.get(remainder))) != null)
                    {
                    data = new int[] { 0x02, 0x40, (source - 1), 0x00, index.intValue(), 0x00, val };
                    }
                else if ((index = (Integer)(addWaveHCSoftParamsToIndex.get(remainder))) != null)
                    {
                    int harmonic = StringUtility.getFirstInt(remainder);
                    data = new int[] { 0x02, 0x41, (source - 1), (harmonic - 1), 0x00, 0x00, val };
                    }
                else if ((index = (Integer)(addWaveHCLoudParamsToIndex.get(remainder))) != null)
                    {
                    int harmonic = StringUtility.getFirstInt(remainder);
                    data = new int[] { 0x02, 0x42, (source - 1), (harmonic - 1), 0x00, 0x00, val };
                    }
                else if ((index = (Integer)(addWaveFormantFilterParamsToIndex.get(remainder))) != null)
                    {
                    int formant = StringUtility.getFirstInt(remainder);
                    data = new int[] { 0x02, 0x43, (source - 1), (formant - 1), 0x00, 0x00, val };
                    }
                else if ((index = (Integer)(addWaveHarmonicEnvelopeParamsToIndex.get(remainder))) != null)
                    {
                    int harmonic = StringUtility.getFirstInt(remainder);
                    int stage = StringUtility.getSecondInt(remainder);
                    boolean rate = remainder.contains("rate");
                    // each stage is two elements, first the rate and then the level
                    data = new int[] { 0x02, 0x44, (source - 1), (harmonic - 1), stage * 2 + (rate ? 0 : 1), 0x00, val };
                    }
                else if ((index = (Integer)(addWaveHarmonicLoopParamsToIndex.get(remainder))) != null)
                    {
                    int harmonic = StringUtility.getFirstInt(remainder);
                    data = new int[] { 0x02, 0x44, (source - 1), (harmonic - 1), 0x08, 0x00, val };
                    }
                else
                    {
                    System.err.println("Unknown Parameter " + key);
                    return new Object[0];
                    }
                }
            }
        else if (key.equals("name"))
            {
            data8 = new int[8][];
            int[] name = getNameAsBytes();
            for(int i = 0; i < data8.length; i++)
                {
                data8[i] = new int[] { 0x01, 0x00, 0x00, 0x00, i, 0x00, name[i] };
                }
            }
        else if (key.equals("algorithm"))
            {
            data = new int[] { 0x03, 0x00, 0x00, 0x00, 0x00, 0x00, model.get(key) };
            }
        else if (key.startsWith("reverb"))
            {
            int type = model.get("reverbtype");
            if (key.endsWith("type"))
                {
            	data = new int[] { 0x03, 0x00, 0x01, 0x00, 0x00, 0x00, model.get(key) + 11 };		// effects start at 11
                }
            else 
            	{
            	if (key.endsWith("drywet1"))
                {
                if (type + 1 != StringUtility.getSecondInt(key))  return new Object[0]; // we're the wrong type
                sub5 = 1;
                }
            else if (key.endsWith("para1"))
                {
                if (type + 1 != StringUtility.getSecondInt(key))  return new Object[0]; // we're the wrong type
                sub5 = 2;
                }
            else if (key.endsWith("para2"))
                {
                if (type + 1 != StringUtility.getSecondInt(key))  return new Object[0]; // we're the wrong type
                sub5 = 3;
                }
            else if (key.endsWith("para3"))
                {
                if (type + 1 != StringUtility.getSecondInt(key))  return new Object[0]; // we're the wrong type
                sub5 = 4;
                }
            else if (key.endsWith("para4"))
                {
                if (type + 1 != StringUtility.getSecondInt(key))  return new Object[0]; // we're the wrong type
                sub5 = 5;
                }
            data = new int[] { 0x03, 0x00, 0x01, 0x00, sub5, 0x00, model.get(key) };
            }
            }
        else if (key.startsWith("effectcontrol"))
            {
            int effect = StringUtility.getFirstInt(key);
            boolean source = key.endsWith("source");
            boolean depth = key.endsWith("depth");
            if (source)
                {
                data = new int[] { 0x01, 0x00, 0x00, 0x00, effect == 1 ? 0x0E : 0x11, 0x00, model.get(key) };
                }
            else if (depth)
                {
                data = new int[] { 0x01, 0x00, 0x00, 0x00, effect == 1 ? 0x10 : 0x13, 0x00, model.get(key) };
                }
            else
                {
                data = new int[] { 0x01, 0x00, 0x00, 0x00, effect == 1 ? 0x0F : 0x12, 0x00, model.get(key) };
                }
            }
        else if (key.startsWith("effect"))
            {
            int effect = StringUtility.getFirstInt(key);
            int type = model.get("effect" + effect + "type");
            String reduced = StringUtility.removePreambleAndFirstDigits(key, "effect");
            if (key.endsWith("type"))
                {
                sub5 = 0;
                }
            else if (key.endsWith("depth"))
                {
                if (type + 1 != StringUtility.getSecondInt(key))  return new Object[0]; // we're the wrong type
                sub5 = 1;
                }
            else if (key.endsWith("para1"))
                {
                if (type + 1 != StringUtility.getSecondInt(key))  return new Object[0]; // we're the wrong type
                sub5 = 2;
                }
            else if (key.endsWith("para2"))
                {
                if (type + 1 != StringUtility.getSecondInt(key))  return new Object[0]; // we're the wrong type
                sub5 = 3;
                }
            else if (key.endsWith("para3"))
                {
                if (type + 1 != StringUtility.getSecondInt(key))  return new Object[0]; // we're the wrong type
                sub5 = 4;
                }
            else if (key.endsWith("para4"))
                {
                if (type + 1 != StringUtility.getSecondInt(key))  return new Object[0]; // we're the wrong type
                sub5 = 5;
                }
            data = new int[] { 0x03, 0x00, effect + 1, 0x00, sub5, 0x00, model.get(key) };
            }
        else if (key.startsWith("geq"))
            {
            int eq = StringUtility.getFirstInt(key);
            data = new int[] { 0x03, 0x01, 0x00, 0x00, eq, 0x00, model.get(key) };
            }
        else if (key.startsWith("srcmute"))
            {
            // send all the srcmutes as one blob
            data = new int[] { 0x01, 0x00, 0x00, 0x00, 0x0C, 0x00, 
                ((1 - model.get("srcmute1")) << 0) |
                ((1 - model.get("srcmute2")) << 1) |
                ((1 - model.get("srcmute3")) << 2) |
                ((1 - model.get("srcmute4")) << 3) |
                ((1 - model.get("srcmute5")) << 4) |
                ((1 - model.get("srcmute6")) << 5) };
            }
        else if (key.startsWith("macrocontroller"))
            {
            int pos = ((Integer)singleToneDataParamsToIndex.get(key)).intValue() - 
                ((Integer)singleToneDataParamsToIndex.get("macrocontroller1parameter1")).intValue();
            data = new int[] { 0x01, 0x00, 0x00, 0x00, pos, 0x00, model.get(key) };
            }
        else if ((index = (Integer)(singleToneDataParamsToIndex.get(key))) != null)
            {
            data = new int[] { 0x01, 0x00, 0x00, 0x00, index.intValue(), 0x00, model.get(key) };
            }
        else
            {
            System.err.println("ERROR: unknown parameter " + key);
            return new Object[0];
            }
                
        
        /// At this point we either have data or we have data8
        
        if (data8 != null)
            {
            Object[] retval = new Object[data8.length];
            for(int i = 0; i < data8.length; i++)
                {
                retval[i] = new byte[] 
                    { 
                    (byte)0xF0, 0x40, 0x00, 0x10, 0x00, 0x0A, 
                    (byte)data8[i][0], (byte)data8[i][1], (byte)data8[i][2], (byte)data8[i][3], (byte)data8[i][4], (byte)data8[i][5], (byte)data8[i][6],
                    (byte)0xF7 
                    };
                }
            return retval;
            }
        else
            {
            byte[] payload = new byte[] 
                { 
                (byte)0xF0, 0x40, 0x00, 0x10, 0x00, 0x0A, 
                (byte)data[0], (byte)data[1], (byte)data[2], (byte)data[3], (byte)data[4], (byte)data[5], (byte)data[6],
                (byte)0xF7 
                };
            return new Object[] { payload };
            }
        }
        


        
    public void addK5000Menu()
        {
        /*
          JMenu menu = new JMenu("Kawai K5000");
          menubar.add(menu);
          JMenuItem harmonicsMenu = new JMenuItem("Load Wave into Harmonics 1");
          harmonicsMenu.addActionListener(new ActionListener()
          {
          public void actionPerformed(ActionEvent e)
          {
          loadWaveAsHarmonics(HARMONICS_1);
          }
          });
          menu.add(harmonicsMenu);
          harmonicsMenu = new JMenuItem("Load Wave into Harmonics 2");
          harmonicsMenu.addActionListener(new ActionListener()
          {
          public void actionPerformed(ActionEvent e)
          {
          loadWaveAsHarmonics(HARMONICS_2);
          }
          });
          menu.add(harmonicsMenu);
          harmonicsMenu = new JMenuItem("Load Wave into Harmonics 1 + 2");
          harmonicsMenu.addActionListener(new ActionListener()
          {
          public void actionPerformed(ActionEvent e)
          {
          loadWaveAsHarmonics(HARMONICS_BOTH);
          }
          });
          menu.add(harmonicsMenu);
        */
        }

    public String[] getBankNames() { return BANKS; }

/*
// Return a list of all patch number names.  Default is { "Main" }
public String[] getPatchNumberNames() { return buildIntegerNames(12, 0); }

// Return a list whether patches in banks are writeable.  Default is { false }
public boolean[] getWriteableBanks() { return new boolean[] { true, true, true, true, true, true, true, true }; }

// Return a list whether individual patches can be written.  Default is FALSE.
public boolean getSupportsPatchWrites() { return true; }

public int getPatchNameLength() { return 8; }
*/


    // General (non-source) parameter names
    public static final String[] singleToneDataParams = new String[] 
    {
    "algorithm",
    "reverbtype",
    "--",   // "reverbdrywet1",         // the actual parameter stored is "reverbNdrywet1" where N is the reverb type
    "--",   // "reverbpara1",           // ... and so on ...
    "--",   // "reverbpara2",
    "--",   // "reverbpara3",
    "--",   // "reverbpara4",
    "effect1type",
    "--",   // "effect1depth",          // the actual parameter stored is "effect1typeNdepth" where N is the effect type
    "--",   // "effect1para1",          // ... and so on ...
    "--",   // "effect1para2",
    "--",   // "effect1para3",
    "--",   // "effect1para4",
    "effect2type",
    "--",   // "effect2depth",
    "--",   // "effect2para1",
    "--",   // "effect2para2",
    "--",   // "effect2para3",
    "--",   // "effect2para4",
    "effect3type",
    "--",   // "effect3depth",
    "--",   // "effect3para1",
    "--",   // "effect3para2",
    "--",   // "effect3para3",
    "--",   // "effect3para4",
    "effect4type",
    "--",   // "effect4depth",
    "--",   // "effect4para1",
    "--",   // "effect4para2",
    "--",   // "effect4para3",
    "--",   // "effect4para4",
    "geqfreq1",
    "geqfreq2",
    "geqfreq3",
    "geqfreq4",
    "geqfreq5",
    "geqfreq6",
    "geqfreq7",
    "--",           // drum_mark        -- no idea what this is
    "name",         // name1
    "--",           // name2
    "--",           // name3
    "--",           // name4
    "--",           // name5
    "--",           // name6
    "--",           // name7
    "--",           // name8
    "volume",
    "poly",
    "--",                                // "no use"
    "srctype",              // strange name for "number of sources"
    "srcmute1",             // we break this into "srcmute1" ... "srcmute6"
    "am",
    "effectcontrol1source",
    "effectcontrol1destination",
    "effectcontrol1depth",
    "effectcontrol2source",
    "effectcontrol2destination",
    "effectcontrol2depth",
    "portamento",
    "portamentospeed",
    "macrocontroller1parameter1",                   // k5k S/R only
    "macrocontroller1parameter2",                   // k5k S/R only
    "macrocontroller2parameter1",                   // k5k S/R only
    "macrocontroller2parameter2",                   // k5k S/R only
    "macrocontroller3parameter1",                   // k5k S/R only
    "macrocontroller3parameter2",                   // k5k S/R only
    "macrocontroller4parameter1",                   // k5k S/R only
    "macrocontroller4parameter2",                   // k5k S/R only
    "macrocontroller1depth1",                               // k5k S/R only
    "macrocontroller1depth2",                               // k5k S/R only
    "macrocontroller2depth1",                               // k5k S/R only
    "macrocontroller2depth2",                               // k5k S/R only
    "macrocontroller3depth1",                               // k5k S/R only
    "macrocontroller3depth2",                               // k5k S/R only
    "macrocontroller4depth1",                               // k5k S/R only
    "macrocontroller4depth2",                               // k5k S/R only
    "sw1parameter",                 // k5k S/R only
    "sw2parameter",                 // k5k S/R only
    "fsw1parameter",                        // k5k S/R only
    "fsw2parameter"                 // k5k S/R only
    };
    
    // Names of various parameters (other than formants, harmonics, and harmonics envelopes, and the "wave kit") for a given source
    // The full name is "sourceNPARAM" where N is a number 1...6, and PARAM a string below.
    public static final String[] sourceParams = new String[] 
    {
    "zonelo",
    "zonehi",
    "--",                                               // "velo sw" and "velo"
    "effectpath",
    "volume",
    "benderpitch",
    "bendercutoff",
    "pressdestination1",
    "pressdepth1",
    "pressdestination2",
    "pressdepth2",
    "wheeldestination1",
    "wheeldepth1",
    "wheeldestination2",
    "wheeldepth2",
    "expressdestination1",
    "expressdepth1",
    "expressdestination2",
    "expressdepth2",
    "assignablecontrol1source",
    "assignablecontrol1destination",
    "assignablecontrol1depth",
    "assignablecontrol2source",
    "assignablecontrol2destination",
    "assignablecontrol2depth",
    "keyondelay",
    "pantype",
    "pannormalvalue",
    "--",                       // wavekitmsb                           // we make:  wavekit  and       additive
    "--",                       // wavekitlsb
    "coarse",
    "fine",
    "fixedkey",                                                                 // This is stored in Edisyn as 0 ... 108-21, but must be emitted as 0 vs 21...108
    "kspitch",
    "pitchenvstartlevel",
    "pitchenvattacktime",
    "pitchenvattacklevel",
    "pitchenvdecaytime",
    "pitchenvtimevelosense",
    "pitchenvlevelvelosense",
    "dcf",
    "dcfmode",
    "dcfvelocurve",
    "dcfresonance",
    "dcflevel",
    "dcfcutoff",
    "dcfcutoffksdepth",
    "dcfcutoffvelodepth",
    "dcfenvdepth",
    "dcfenvattacktime",
    "dcfenvdecay1time",
    "dcfenvdecay1level",
    "dcfenvdecay2time",
    "dcfenvdecay2level",
    "dcfenvreleasetime",
    "dcfkstoenvattacktime",
    "dcfkstoenvdecay1time",
    "dcfvelotoenvlevel",
    "dcfvelotoenvattacktime",
    "dcfvelotoenvdecay1time",
    "dcavelocurve",
    "dcaenvattacktime",
    "dcaenvdecay1time",
    "dcaenvdecay1level",
    "dcaenvdecay2time",
    "dcaenvdecay2level",
    "dcaenvreleasetime",
    "dcakstoenvlevel",
    "dcakstoenvattacktime",
    "dcakstoenvdecay1time",
    "dcakstoenvreleasetime",
    "dcavelotoenvlevel",
    "dcavelotoenvattacktime",
    "dcavelotoenvdecay1time",
    "dcavelotoenvreleasetime",
    "lfowaveform",
    "lfospeed",
    "lfodelayonset",
    "lfofadeintime",
    "lfofadeintospeed",
    "lfopitchdepth",
    "lfopitchks",
    "lfodcfdepth",
    "lfodcfks",
    "lfodcadepth",
    "lfodcaks",
    };
        
    // Names of various parameters (other than formants, harmonics, and harmonics envelopes) of the so-called "wave kit" for a given source
    // The full name is "sourceNPARAM" where N is a number 1...6, and PARAM a string below.
    public static final String[] addWaveKitParams = new String[] 
    {
    "--",                       // checksum
    "morfflag",
    "totalgain",
    "harmgroup",
    "kstogain",
    "balancevelocurve",
    "balancevelodepth",
    "hc1patch",
    "hc1source",
    "hc2patch",
    "hc2source",
    "hc3patch",
    "hc3source",
    "hc4patch",
    "hc4source",
    "hetime1",
    "hetime2",
    "hetime3",
    "hetime4",
    "loop",
    "formantbias",
    "formantenvlfosel",
    "formantenvdepth",
    "formantattackrate",
    "formantattacklevel",
    "formantdecay1rate",
    "formantdecay1level",
    "formantdecay2rate",
    "formantdecay2level",
    "formantreleaserate",
    "formantreleaselevel",
    "formantloop",
    "formantvelosenseenvdepth",
    "formantksenvdepth",
    "formantlfospeed",
    "formantlfoshape",
    "formantlfodepth",
    };
    
    // Names of the soft harmonics parameters
    // The full name is "sourceNPARAM" where N is a number 1...6, and PARAM a string below.
    public static final String[] addWaveHCSoftParams = new String[]
    {
    "hcsoft1",
    "hcsoft2",
    "hcsoft3",
    "hcsoft4",
    "hcsoft5",
    "hcsoft6",
    "hcsoft7",
    "hcsoft8",
    "hcsoft9",
    "hcsoft10",
    "hcsoft11",
    "hcsoft12",
    "hcsoft13",
    "hcsoft14",
    "hcsoft15",
    "hcsoft16",
    "hcsoft17",
    "hcsoft18",
    "hcsoft19",
    "hcsoft20",
    "hcsoft21",
    "hcsoft22",
    "hcsoft23",
    "hcsoft24",
    "hcsoft25",
    "hcsoft26",
    "hcsoft27",
    "hcsoft28",
    "hcsoft29",
    "hcsoft30",
    "hcsoft31",
    "hcsoft32",
    "hcsoft33",
    "hcsoft34",
    "hcsoft35",
    "hcsoft36",
    "hcsoft37",
    "hcsoft38",
    "hcsoft39",
    "hcsoft40",
    "hcsoft41",
    "hcsoft42",
    "hcsoft43",
    "hcsoft44",
    "hcsoft45",
    "hcsoft46",
    "hcsoft47",
    "hcsoft48",
    "hcsoft49",
    "hcsoft50",
    "hcsoft51",
    "hcsoft52",
    "hcsoft53",
    "hcsoft54",
    "hcsoft55",
    "hcsoft56",
    "hcsoft57",
    "hcsoft58",
    "hcsoft59",
    "hcsoft60",
    "hcsoft61",
    "hcsoft62",
    "hcsoft63",
    "hcsoft64"
    };
    
    // Names of the loud harmonics parameters
    // The full name is "sourceNPARAM" where N is a number 1...6, and PARAM a string below.
    public static final String[] addWaveHCLoudParams = new String[]
    {
    "hcloud1",
    "hcloud2",
    "hcloud3",
    "hcloud4",
    "hcloud5",
    "hcloud6",
    "hcloud7",
    "hcloud8",
    "hcloud9",
    "hcloud10",
    "hcloud11",
    "hcloud12",
    "hcloud13",
    "hcloud14",
    "hcloud15",
    "hcloud16",
    "hcloud17",
    "hcloud18",
    "hcloud19",
    "hcloud20",
    "hcloud21",
    "hcloud22",
    "hcloud23",
    "hcloud24",
    "hcloud25",
    "hcloud26",
    "hcloud27",
    "hcloud28",
    "hcloud29",
    "hcloud30",
    "hcloud31",
    "hcloud32",
    "hcloud33",
    "hcloud34",
    "hcloud35",
    "hcloud36",
    "hcloud37",
    "hcloud38",
    "hcloud39",
    "hcloud40",
    "hcloud41",
    "hcloud42",
    "hcloud43",
    "hcloud44",
    "hcloud45",
    "hcloud46",
    "hcloud47",
    "hcloud48",
    "hcloud49",
    "hcloud50",
    "hcloud51",
    "hcloud52",
    "hcloud53",
    "hcloud54",
    "hcloud55",
    "hcloud56",
    "hcloud57",
    "hcloud58",
    "hcloud59",
    "hcloud60",
    "hcloud61",
    "hcloud62",
    "hcloud63",
    "hcloud64"
    };

    // Names of the 64 formant frequencies.
    // The full name is "sourceNPARAM" where N is a number 1...6, and PARAM a string below.
    public static final String[] addWaveFormantFilterParams = new String[]
    {
    "formant1",
    "formant2",
    "formant3",
    "formant4",
    "formant5",
    "formant6",
    "formant7",
    "formant8",
    "formant9",
    "formant10",
    "formant11",
    "formant12",
    "formant13",
    "formant14",
    "formant15",
    "formant16",
    "formant17",
    "formant18",
    "formant19",
    "formant20",
    "formant21",
    "formant22",
    "formant23",
    "formant24",
    "formant25",
    "formant26",
    "formant27",
    "formant28",
    "formant29",
    "formant30",
    "formant31",
    "formant32",
    "formant33",
    "formant34",
    "formant35",
    "formant36",
    "formant37",
    "formant38",
    "formant39",
    "formant40",
    "formant41",
    "formant42",
    "formant43",
    "formant44",
    "formant45",
    "formant46",
    "formant47",
    "formant48",
    "formant49",
    "formant50",
    "formant51",
    "formant52",
    "formant53",
    "formant54",
    "formant55",
    "formant56",
    "formant57",
    "formant58",
    "formant59",
    "formant60",
    "formant61",
    "formant62",
    "formant63",
    "formant64",
    "formant65",
    "formant66",
    "formant67",
    "formant68",
    "formant69",
    "formant70",
    "formant71",
    "formant72",
    "formant73",
    "formant74",
    "formant75",
    "formant76",
    "formant77",
    "formant78",
    "formant79",
    "formant80",
    "formant81",
    "formant82",
    "formant83",
    "formant84",
    "formant85",
    "formant86",
    "formant87",
    "formant88",
    "formant89",
    "formant90",
    "formant91",
    "formant92",
    "formant93",
    "formant94",
    "formant95",
    "formant96",
    "formant97",
    "formant98",
    "formant99",
    "formant100",
    "formant101",
    "formant102",
    "formant103",
    "formant104",
    "formant105",
    "formant106",
    "formant107",
    "formant108",
    "formant109",
    "formant110",
    "formant111",
    "formant112",
    "formant113",
    "formant114",
    "formant115",
    "formant116",
    "formant117",
    "formant118",
    "formant119",
    "formant120",
    "formant121",
    "formant122",
    "formant123",
    "formant124",
    "formant125",
    "formant126",
    "formant127",
    "formant128"
    };
    
    // Names of the parameters for additive wave harmonic envelopes. 
    // The full name is "sourceNPARAM" where N is a number 1...6, and PARAM a string below.
    public static final String[] addWaveHarmonicEnvelopeParams = new String[]
    {
    "hcenv1rate0",
    "hcenv1level0",
    "hcenv1rate1",
    "hcenv1level1",
    "hcenv1rate2",
    "hcenv1level2",
    "hcenv1rate3",
    "hcenv1level3",
    "hcenv2rate0",
    "hcenv2level0",
    "hcenv2rate1",
    "hcenv2level1",
    "hcenv2rate2",
    "hcenv2level2",
    "hcenv2rate3",
    "hcenv2level3",
    "hcenv3rate0",
    "hcenv3level0",
    "hcenv3rate1",
    "hcenv3level1",
    "hcenv3rate2",
    "hcenv3level2",
    "hcenv3rate3",
    "hcenv3level3",
    "hcenv4rate0",
    "hcenv4level0",
    "hcenv4rate1",
    "hcenv4level1",
    "hcenv4rate2",
    "hcenv4level2",
    "hcenv4rate3",
    "hcenv4level3",
    "hcenv5rate0",
    "hcenv5level0",
    "hcenv5rate1",
    "hcenv5level1",
    "hcenv5rate2",
    "hcenv5level2",
    "hcenv5rate3",
    "hcenv5level3",
    "hcenv6rate0",
    "hcenv6level0",
    "hcenv6rate1",
    "hcenv6level1",
    "hcenv6rate2",
    "hcenv6level2",
    "hcenv6rate3",
    "hcenv6level3",
    "hcenv7rate0",
    "hcenv7level0",
    "hcenv7rate1",
    "hcenv7level1",
    "hcenv7rate2",
    "hcenv7level2",
    "hcenv7rate3",
    "hcenv7level3",
    "hcenv8rate0",
    "hcenv8level0",
    "hcenv8rate1",
    "hcenv8level1",
    "hcenv8rate2",
    "hcenv8level2",
    "hcenv8rate3",
    "hcenv8level3",
    "hcenv9rate0",
    "hcenv9level0",
    "hcenv9rate1",
    "hcenv9level1",
    "hcenv9rate2",
    "hcenv9level2",
    "hcenv9rate3",
    "hcenv9level3",
    "hcenv10rate0",
    "hcenv10level0",
    "hcenv10rate1",
    "hcenv10level1",
    "hcenv10rate2",
    "hcenv10level2",
    "hcenv10rate3",
    "hcenv10level3",
    "hcenv11rate0",
    "hcenv11level0",
    "hcenv11rate1",
    "hcenv11level1",
    "hcenv11rate2",
    "hcenv11level2",
    "hcenv11rate3",
    "hcenv11level3",
    "hcenv12rate0",
    "hcenv12level0",
    "hcenv12rate1",
    "hcenv12level1",
    "hcenv12rate2",
    "hcenv12level2",
    "hcenv12rate3",
    "hcenv12level3",
    "hcenv13rate0",
    "hcenv13level0",
    "hcenv13rate1",
    "hcenv13level1",
    "hcenv13rate2",
    "hcenv13level2",
    "hcenv13rate3",
    "hcenv13level3",
    "hcenv14rate0",
    "hcenv14level0",
    "hcenv14rate1",
    "hcenv14level1",
    "hcenv14rate2",
    "hcenv14level2",
    "hcenv14rate3",
    "hcenv14level3",
    "hcenv15rate0",
    "hcenv15level0",
    "hcenv15rate1",
    "hcenv15level1",
    "hcenv15rate2",
    "hcenv15level2",
    "hcenv15rate3",
    "hcenv15level3",
    "hcenv16rate0",
    "hcenv16level0",
    "hcenv16rate1",
    "hcenv16level1",
    "hcenv16rate2",
    "hcenv16level2",
    "hcenv16rate3",
    "hcenv16level3",
    "hcenv17rate0",
    "hcenv17level0",
    "hcenv17rate1",
    "hcenv17level1",
    "hcenv17rate2",
    "hcenv17level2",
    "hcenv17rate3",
    "hcenv17level3",
    "hcenv18rate0",
    "hcenv18level0",
    "hcenv18rate1",
    "hcenv18level1",
    "hcenv18rate2",
    "hcenv18level2",
    "hcenv18rate3",
    "hcenv18level3",
    "hcenv19rate0",
    "hcenv19level0",
    "hcenv19rate1",
    "hcenv19level1",
    "hcenv19rate2",
    "hcenv19level2",
    "hcenv19rate3",
    "hcenv19level3",
    "hcenv20rate0",
    "hcenv20level0",
    "hcenv20rate1",
    "hcenv20level1",
    "hcenv20rate2",
    "hcenv20level2",
    "hcenv20rate3",
    "hcenv20level3",
    "hcenv21rate0",
    "hcenv21level0",
    "hcenv21rate1",
    "hcenv21level1",
    "hcenv21rate2",
    "hcenv21level2",
    "hcenv21rate3",
    "hcenv21level3",
    "hcenv22rate0",
    "hcenv22level0",
    "hcenv22rate1",
    "hcenv22level1",
    "hcenv22rate2",
    "hcenv22level2",
    "hcenv22rate3",
    "hcenv22level3",
    "hcenv23rate0",
    "hcenv23level0",
    "hcenv23rate1",
    "hcenv23level1",
    "hcenv23rate2",
    "hcenv23level2",
    "hcenv23rate3",
    "hcenv23level3",
    "hcenv24rate0",
    "hcenv24level0",
    "hcenv24rate1",
    "hcenv24level1",
    "hcenv24rate2",
    "hcenv24level2",
    "hcenv24rate3",
    "hcenv24level3",
    "hcenv25rate0",
    "hcenv25level0",
    "hcenv25rate1",
    "hcenv25level1",
    "hcenv25rate2",
    "hcenv25level2",
    "hcenv25rate3",
    "hcenv25level3",
    "hcenv26rate0",
    "hcenv26level0",
    "hcenv26rate1",
    "hcenv26level1",
    "hcenv26rate2",
    "hcenv26level2",
    "hcenv26rate3",
    "hcenv26level3",
    "hcenv27rate0",
    "hcenv27level0",
    "hcenv27rate1",
    "hcenv27level1",
    "hcenv27rate2",
    "hcenv27level2",
    "hcenv27rate3",
    "hcenv27level3",
    "hcenv28rate0",
    "hcenv28level0",
    "hcenv28rate1",
    "hcenv28level1",
    "hcenv28rate2",
    "hcenv28level2",
    "hcenv28rate3",
    "hcenv28level3",
    "hcenv29rate0",
    "hcenv29level0",
    "hcenv29rate1",
    "hcenv29level1",
    "hcenv29rate2",
    "hcenv29level2",
    "hcenv29rate3",
    "hcenv29level3",
    "hcenv30rate0",
    "hcenv30level0",
    "hcenv30rate1",
    "hcenv30level1",
    "hcenv30rate2",
    "hcenv30level2",
    "hcenv30rate3",
    "hcenv30level3",
    "hcenv31rate0",
    "hcenv31level0",
    "hcenv31rate1",
    "hcenv31level1",
    "hcenv31rate2",
    "hcenv31level2",
    "hcenv31rate3",
    "hcenv31level3",
    "hcenv32rate0",
    "hcenv32level0",
    "hcenv32rate1",
    "hcenv32level1",
    "hcenv32rate2",
    "hcenv32level2",
    "hcenv32rate3",
    "hcenv32level3",
    "hcenv33rate0",
    "hcenv33level0",
    "hcenv33rate1",
    "hcenv33level1",
    "hcenv33rate2",
    "hcenv33level2",
    "hcenv33rate3",
    "hcenv33level3",
    "hcenv34rate0",
    "hcenv34level0",
    "hcenv34rate1",
    "hcenv34level1",
    "hcenv34rate2",
    "hcenv34level2",
    "hcenv34rate3",
    "hcenv34level3",
    "hcenv35rate0",
    "hcenv35level0",
    "hcenv35rate1",
    "hcenv35level1",
    "hcenv35rate2",
    "hcenv35level2",
    "hcenv35rate3",
    "hcenv35level3",
    "hcenv36rate0",
    "hcenv36level0",
    "hcenv36rate1",
    "hcenv36level1",
    "hcenv36rate2",
    "hcenv36level2",
    "hcenv36rate3",
    "hcenv36level3",
    "hcenv37rate0",
    "hcenv37level0",
    "hcenv37rate1",
    "hcenv37level1",
    "hcenv37rate2",
    "hcenv37level2",
    "hcenv37rate3",
    "hcenv37level3",
    "hcenv38rate0",
    "hcenv38level0",
    "hcenv38rate1",
    "hcenv38level1",
    "hcenv38rate2",
    "hcenv38level2",
    "hcenv38rate3",
    "hcenv38level3",
    "hcenv39rate0",
    "hcenv39level0",
    "hcenv39rate1",
    "hcenv39level1",
    "hcenv39rate2",
    "hcenv39level2",
    "hcenv39rate3",
    "hcenv39level3",
    "hcenv40rate0",
    "hcenv40level0",
    "hcenv40rate1",
    "hcenv40level1",
    "hcenv40rate2",
    "hcenv40level2",
    "hcenv40rate3",
    "hcenv40level3",
    "hcenv41rate0",
    "hcenv41level0",
    "hcenv41rate1",
    "hcenv41level1",
    "hcenv41rate2",
    "hcenv41level2",
    "hcenv41rate3",
    "hcenv41level3",
    "hcenv42rate0",
    "hcenv42level0",
    "hcenv42rate1",
    "hcenv42level1",
    "hcenv42rate2",
    "hcenv42level2",
    "hcenv42rate3",
    "hcenv42level3",
    "hcenv43rate0",
    "hcenv43level0",
    "hcenv43rate1",
    "hcenv43level1",
    "hcenv43rate2",
    "hcenv43level2",
    "hcenv43rate3",
    "hcenv43level3",
    "hcenv44rate0",
    "hcenv44level0",
    "hcenv44rate1",
    "hcenv44level1",
    "hcenv44rate2",
    "hcenv44level2",
    "hcenv44rate3",
    "hcenv44level3",
    "hcenv45rate0",
    "hcenv45level0",
    "hcenv45rate1",
    "hcenv45level1",
    "hcenv45rate2",
    "hcenv45level2",
    "hcenv45rate3",
    "hcenv45level3",
    "hcenv46rate0",
    "hcenv46level0",
    "hcenv46rate1",
    "hcenv46level1",
    "hcenv46rate2",
    "hcenv46level2",
    "hcenv46rate3",
    "hcenv46level3",
    "hcenv47rate0",
    "hcenv47level0",
    "hcenv47rate1",
    "hcenv47level1",
    "hcenv47rate2",
    "hcenv47level2",
    "hcenv47rate3",
    "hcenv47level3",
    "hcenv48rate0",
    "hcenv48level0",
    "hcenv48rate1",
    "hcenv48level1",
    "hcenv48rate2",
    "hcenv48level2",
    "hcenv48rate3",
    "hcenv48level3",
    "hcenv49rate0",
    "hcenv49level0",
    "hcenv49rate1",
    "hcenv49level1",
    "hcenv49rate2",
    "hcenv49level2",
    "hcenv49rate3",
    "hcenv49level3",
    "hcenv50rate0",
    "hcenv50level0",
    "hcenv50rate1",
    "hcenv50level1",
    "hcenv50rate2",
    "hcenv50level2",
    "hcenv50rate3",
    "hcenv50level3",
    "hcenv51rate0",
    "hcenv51level0",
    "hcenv51rate1",
    "hcenv51level1",
    "hcenv51rate2",
    "hcenv51level2",
    "hcenv51rate3",
    "hcenv51level3",
    "hcenv52rate0",
    "hcenv52level0",
    "hcenv52rate1",
    "hcenv52level1",
    "hcenv52rate2",
    "hcenv52level2",
    "hcenv52rate3",
    "hcenv52level3",
    "hcenv53rate0",
    "hcenv53level0",
    "hcenv53rate1",
    "hcenv53level1",
    "hcenv53rate2",
    "hcenv53level2",
    "hcenv53rate3",
    "hcenv53level3",
    "hcenv54rate0",
    "hcenv54level0",
    "hcenv54rate1",
    "hcenv54level1",
    "hcenv54rate2",
    "hcenv54level2",
    "hcenv54rate3",
    "hcenv54level3",
    "hcenv55rate0",
    "hcenv55level0",
    "hcenv55rate1",
    "hcenv55level1",
    "hcenv55rate2",
    "hcenv55level2",
    "hcenv55rate3",
    "hcenv55level3",
    "hcenv56rate0",
    "hcenv56level0",
    "hcenv56rate1",
    "hcenv56level1",
    "hcenv56rate2",
    "hcenv56level2",
    "hcenv56rate3",
    "hcenv56level3",
    "hcenv57rate0",
    "hcenv57level0",
    "hcenv57rate1",
    "hcenv57level1",
    "hcenv57rate2",
    "hcenv57level2",
    "hcenv57rate3",
    "hcenv57level3",
    "hcenv58rate0",
    "hcenv58level0",
    "hcenv58rate1",
    "hcenv58level1",
    "hcenv58rate2",
    "hcenv58level2",
    "hcenv58rate3",
    "hcenv58level3",
    "hcenv59rate0",
    "hcenv59level0",
    "hcenv59rate1",
    "hcenv59level1",
    "hcenv59rate2",
    "hcenv59level2",
    "hcenv59rate3",
    "hcenv59level3",
    "hcenv60rate0",
    "hcenv60level0",
    "hcenv60rate1",
    "hcenv60level1",
    "hcenv60rate2",
    "hcenv60level2",
    "hcenv60rate3",
    "hcenv60level3",
    "hcenv61rate0",
    "hcenv61level0",
    "hcenv61rate1",
    "hcenv61level1",
    "hcenv61rate2",
    "hcenv61level2",
    "hcenv61rate3",
    "hcenv61level3",
    "hcenv62rate0",
    "hcenv62level0",
    "hcenv62rate1",
    "hcenv62level1",
    "hcenv62rate2",
    "hcenv62level2",
    "hcenv62rate3",
    "hcenv62level3",
    "hcenv63rate0",
    "hcenv63level0",
    "hcenv63rate1",
    "hcenv63level1",
    "hcenv63rate2",
    "hcenv63level2",
    "hcenv63rate3",
    "hcenv63level3",
    "hcenv64rate0",
    "hcenv64level0",
    "hcenv64rate1",
    "hcenv64level1",
    "hcenv64rate2",
    "hcenv64level2",
    "hcenv64rate3",
    "hcenv64level3",
    "--",                           // dummy -- is this "loudness sense select?"
    };
        
    public static final String[] addWaveHarmonicLoopParams = new String[]
    {
    "hcenv1loop",
    "hcenv2loop",
    "hcenv3loop",
    "hcenv4loop",
    "hcenv5loop",
    "hcenv6loop",
    "hcenv7loop",
    "hcenv8loop",
    "hcenv9loop",
    "hcenv10loop",
    "hcenv11loop",
    "hcenv12loop",
    "hcenv13loop",
    "hcenv14loop",
    "hcenv15loop",
    "hcenv16loop",
    "hcenv17loop",
    "hcenv18loop",
    "hcenv19loop",
    "hcenv20loop",
    "hcenv21loop",
    "hcenv22loop",
    "hcenv23loop",
    "hcenv24loop",
    "hcenv25loop",
    "hcenv26loop",
    "hcenv27loop",
    "hcenv28loop",
    "hcenv29loop",
    "hcenv30loop",
    "hcenv31loop",
    "hcenv32loop",
    "hcenv33loop",
    "hcenv34loop",
    "hcenv35loop",
    "hcenv36loop",
    "hcenv37loop",
    "hcenv38loop",
    "hcenv39loop",
    "hcenv40loop",
    "hcenv41loop",
    "hcenv42loop",
    "hcenv43loop",
    "hcenv44loop",
    "hcenv45loop",
    "hcenv46loop",
    "hcenv47loop",
    "hcenv48loop",
    "hcenv49loop",
    "hcenv50loop",
    "hcenv51loop",
    "hcenv52loop",
    "hcenv53loop",
    "hcenv54loop",
    "hcenv55loop",
    "hcenv56loop",
    "hcenv57loop",
    "hcenv58loop",
    "hcenv59loop",
    "hcenv60loop",
    "hcenv61loop",
    "hcenv62loop",
    "hcenv63loop",
    "hcenv64loop",
    };
        
        
    /// K5000w PCM wave names for Bank B.  These appear to be more or less the same as the standard General MIDI list (see https://en.wikipedia.org/wiki/General_MIDI)
    /// Not sure if we'll eventually use this but we'll see
    public static final String[] W_WAVES = new String[]
    {
    "OldUprit1",
    "OldUprit2",
    " Gr.Piano",
    " WidPiano",
    " Br.Piano",
    "Hnkytonk1",
    "E.Grand1",
    "Hnkytonk2",
    "E.Grand2",
    "E.Grand3",
    "Metallic1",
    "E.Piano1",
    "60's EP",
    "E.Piano2",
    "E.Piano3",
    "E.Piano4",
    "Clavi1",
    "Drawbar1",
    "Drawbar2",
    "DetunOr1",
    "Drawbar3",
    "PercOrg1",
    "PercOrg2",
    "ChrcOrg1",
    "ChrcOrg2",
    "Celesta1",
    " Vibe",
    "Glocken1",
    " Marimba",
    "Glocken2",
    "NewAge1",
    " Xylophon",
    " TubulBel",
    " Stl Drum",
    "Timpani1",
    "CncertBD1",
    "NylonGt1",
    " Ukulele",
    "NylonGt2",
    " Nyln+Stl",
    "Atmosphr1",
    "SteelGt1",
    "Sci-Fi1",
    "Mandolin1",
    "Mandolin2",
    "SteelGt2",
    "12strGtr1",
    "12strGtr2",
    "Dulcimer1",
    "JazzGtr1",
    "CleanGtr1",
    "Hi.E.Gtr1",
    " ChorusGt",
    "TubeBass1",
    "CleanGtr2",
    "Hi.E.Gtr2",
    "MuteGtr1",
    "OvrDrive1",
    "ResO.D.1",
    "OvrDrive2",
    "ResO.D.2",
    " Distortd",
    "Charang1",
    "Charang2",
    "FeedbkGt1",
    "PowerGtr1",
    " Res.Dist",
    "RockOrgn1",
    "PowerGtr2",
    " Harmnics",
    "Dulcimer2",
    " Banjo",
    " Sitar",
    " Shamisen",
    " Koto",
    "TaishoKt1",
    "TaishoKt2",
    "Harp1",
    "Harp2",
    "Ac.Bass1",
    "Ac.Bass2",
    "Ac.Bass3",
    "FngBass1",
    "Ac.Bass4",
    "FngBass2",
    "TubeBass2",
    "PickBass1",
    "MutePlck1",
    "PickBass2",
    "MutePlck2",
    " Fretless",
    "SlapBas1",
    "FunkGtr1",
    "FunkGtr2",
    "SlapBas2",
    "SlapBas3",
    "SlapBas4",
    "SynBass1",
    "SynBass2",
    "SynBass3",
    "SynBass4",
    "HouseBass1",
    "HouseBass2",
    "SynBass5",
    " Violin",
    " Fiddle",
    " SlwViolin",
    " Viola",
    " Cello",
    "Contra1",
    "Contra2",
    "Strings1",
    "Strings2",
    "Orchstra1",
    "Strings3",
    "Strings4",
    "Bright1",
    "Atmosphr2",
    "Sweep1",
    "Pizzicto1",
    "Pizzicto2",
    "SynStrg1",
    "SynBras1",
    "SynStrg2",
    "Poly Syn1",
    "Rain1",
    "Soundtrk1",
    "Soundtrk2",
    "SynBass5",
    "SynStrg3",
    "SynStrg4",
    "SynBras2",
    "SynBras3",
    "Chiff1",
    "Fifth1",
    "Fifth2",
    "Metallic2",
    "Sci-Fi2",
    "ChorAah1",
    "Voice1",
    "Halo Pad1",
    " Echoes",
    "ChorAah2",
    "ChorAah3",
    "Sweep2",
    "RockOrgn2",
    "Choir1",
    "Halo Pad2",
    "Chiff2",
    "Bright2",
    "Voi Ooh1",
    " SynVoice",
    "NewAge2",
    "Choir2",
    "Goblns1",
    "Voi Ooh2",
    "Orchstra2",
    "Oct.Bras1",
    "BrasSect1",
    "Brass1",
    "Oct.Bras2",
    "Orch hit1",
    "Orch hit2",
    " WarmTrpt",
    " Trumpet",
    "Tuba1",
    " DublBone",
    "Tuba2",
    " TromBone",
    "BrasSect2",
    " Mute Tp",
    "FrenchHr1",
    "FrenchHr2",
    " SprnoSax",
    "Bassoon1",
    "AltoSax1",
    "AltoSax2",
    "TenorSax1",
    "BrthTenr1",
    "Brass2",
    " Bari Sax",
    " EnglHorn",
    "Bassoon2",
    " Oboe",
    "Winds1",
    "Winds2",
    "Shanai1",
    "Bag Pipe1",
    " Clarinet",
    "Winds3",
    "Flute1",
    "Winds4",
    "Calliope1",
    "Flute2",
    "Piccolo1",
    "PanFlute1",
    " Bottle",
    "Calliope2",
    "Voice2",
    " Shakhach",
    "Kalimba1",
    " Agogo",
    " WoodBlok",
    " Melo.Tom",
    " Syn.Drum",
    " E.Percus",
    " Scratch",
    "E.Tom1",
    "E.Tom2",
    " Castanet",
    " TaikoDrm",
    "RevCymb1",
    " WndChime",
    " BrthNoiz",
    "Flute3",
    "Recorder1",
    "PanFlute2",
    "Ocarina1",
    "Flute4",
    "DrawBar4",
    "Piccolo2",
    "TenorSax2",
    "BrthTenr2",
    " SeaShore",
    " Wind",
    " FretNoiz",
    "GtCtNiz1",
    "GtCtNiz2",
    " StrgSlap",
    "Rain2",
    " Thunder",
    "Stream1",
    "Stream2",
    " Bubble",
    "Bird1",
    "Bird2",
    " Dog",
    " HorseGalp",
    "Tel1",
    " DoorCreak",
    " Door",
    " Helicopter",
    " CarEngine",
    " CarStop",
    " CarPass",
    " CarCrash",
    " Siren",
    " Train",
    " JetPlan",
    " StarShip",
    "Applause1",
    "Applause2",
    " Laughing",
    " Screaming",
    " Punch",
    " HeartBeat",
    " FootStep",
    " Gun",
    " MachineGun",
    " LaserGun",
    " Explosion",
    "Omni1",
    "Omni2",
    "Rain3",
    "MuteGtr2",
    "MusicBox1",
    " Sine",
    "Bowed1",
    "ConcrtBD2",
    "FngBass3",
    "FeedbkGt2",
    "Timpani2",
    "SawLead1",
    "Dr.Solo1",
    "Dr.Solo2",
    "SawLead2",
    "DistClav1",
    "DistClav2",
    "DstSawLd1",
    "DstSawLd2",
    "Bass&Ld1",
    "Bass&Ld2",
    "Poly Syn2",
    "SawLead3",
    "SquarLd1",
    "SquarLd2",
    "SquarLd3",
    "SquarLd4",
    "Dist.Sqr1",
    "Dist.Sqr2",
    "E.Piano5",
    "E.Piano6",
    "E.Piano7",
    "Clavi2",
    "Hrpschrd1",
    "Hrpschrd2",
    "PercOrg3",
    "DrawBar5",
    "DetunOr2",
    "DetunOr3",
    "60's Org",
    " CheseOrg",
    "PercOrg4",
    "ChrcOrg3",
    "ReedOrgn1",
    "ReedOrgn2",
    "Accord.1",
    "Accord.2",
    "Accord.3",
    "Accord.4",
    "TangoAcd1",
    "TangoAcd2",
    " Harmnica",
    "Celesta2",
    "MusicBox2",
    "Crystal1",
    "Crystal2",
    "Kalimba2",
    "TnklBell1",
    "TnklBell2",
    "JazzGtr2",
    "MelowGt1",
    " Hawaiian",
    "MelowGt2",
    "SynBass6",
    "SynBass7",
    "SynBass8",
    "SynBras4",
    "SynBras5",
    "Warm1",
    "Warm2",
    "Bowed2",
    "Sweep3",
    "Sweep4",
    "Goblns2",
    "Whistle1",
    "Whistle2",
    "Ocarina2",
    "Recorder2",
    "Bag Pipe2",
    "Shanai2"
    };

    public boolean testVerify(Synth synth2, String key, Object obj1, Object obj2) 
        {
        // We have lots of effects that will be zero regardless
        if (key.startsWith("effect")) return true;
        if (key.startsWith("reverb")) return true;
        
        // wavekit is spit up into msb and lsb and will give incorrect values
        if (key.contains("wavekit")) return true;
        
        if (key.startsWith("source"))
            {
            int source = StringUtility.getFirstInt(key);
            if (source >= model.get("srctype")) return true;                // this isn't a valid source
            }
                
        return false; 
        }

    }
                                        
