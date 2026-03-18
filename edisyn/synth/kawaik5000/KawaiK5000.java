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
   
   The K5000, like the K5, does not support Send-To-Current-Patch.  Additionally it has an unusual
   memory structure. See the end of this file for details about how we handle this.
   
   The K5000 has bank sysex messages as well (why?!? given their massive size).  We do not support  them
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
    // PCM Waves for Banks A, D, E, and F start at 341.
    // PCM Waves for Bank B run 0-340
    public static final int PCM_START = 341;
        
    // Basic parameter strings
    public static final String EMPTY_PATCH_NAME = "--------";
    public static final String[] KEYS = { "C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B" };
    public static final String[] CENTS = { "0 cent", "25 cent", "33 cent", "50 cent" };
    public static final String[] BANKS = { "A", "D", "E", "F" };
    public static final String[] POLY_MODES = { "Polyphonic", "Solo 1", "Solo 2" };
    public static final String[] HARMONIC_GROUPS = { "Low", "High" };
    public static final String[] FORMANT_MODULATIONS = { "Env", "LFO" };
    public static final String[] HARMONIC_LFO_WAVES = { "Triangle", "Sawtooth", "Random" };
    public static final String[] ENV_LOOPS = { "Off", "LP1", "LP2" };
    public static final String[] VELO_SW = { "Off", "Loud", "Soft" };
    public static final String[] PAN_TYPES = { "Normal", "+KS", "-KS", "Random" };
    public static final String[] DCF_MODES = { "Low Pass", "High Pass" }; 
    public static final String[] SOURCE_LFO_WAVES = { "Triangle", "Square", "Sawtooth", "Sine", "Random" };
    public static final String[] SOURCE_TYPES = { "PCM", "Additive                                   " };                   // these extra spaces prevent movement when changing from PCM to Additive
    public static final String[] SOURCE_NAMES = { "Source 1", "Source 2", "Source 3", "Source 4", "Source 5", "Source 6" };
    // Names of the 16 dials on the K5000s
    public static final String[] DIALS = { 
        "Hrm Lo", "FF Bias", "Cutoff", "Attack", "Hrm Hi", "FF Speed", "Reso", "Decay", "Even/Odd", "FF Depth", "Velocity", "Release", "User 1", "User 2", "User 3", "User 4" };
    // Numbers corresponding to banks A, D, E, and F
    public static final int[] BANK_VALS = { 0, 2, 3, 4 };
        
    // CCs that the 16 dials on the K5000s
    public static final int[] DIAL_CCS = { 0x10, 0x12, 0x4A, 0x49, 0x11, 0x13, 0x4D, 0x4E, 0x47, 0x4B, 0x4C, 0x48, 0x50, 0x51, 0x52, 0x53 };
        
    public static final int ACTION_UP = 0;
    public static final int ACTION_UP_HIGH = 1;
    public static final int ACTION_DOWN = 2;
    public static final int ACTION_DOWN_HIGH = 3;
    public static final int ACTION_DOUBLE = 4;
    public static final int ACTION_HALVE = 5;
    public static final int ACTION_RANDOM = 6;
    public static final int ACTION_JITTER = 7;
    public static final int ACTION_BOOST_BASS = 8;
    public static final int ACTION_DAMPEN_BASS = 9;
    public static final int ACTION_BOOST_TREBLE = 10;
    public static final int ACTION_DAMPEN_TREBLE = 11;
    public static final double BOOST = 0.4;  // 1.0000620039; /// 1.0002480158;             // 1 + 1.0 / 63 / 64.0
    public static final String[] ACTIONS = 
        { "Up\u2191", "Up\u2191\u2191\u2191", "Down\u2193", "Down\u2193\u2193\u2193", "Double", "Halve", "Random", "Jitter", "Bass\u2191", "Bass\u2193", "Treble\u2191", "Treble\u2193" };

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
        // The manual says that TAP DELAY 1 ... CROSS DELAY have min values of 0 for Delay Level (dry/wet), but they do not.  They are 0.
        // Though we go -12db .. +12db, the value are actually just 0...25
        { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,  },
        { 0, 0, 0, 0, NONE, 0, NONE, NONE, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, NONE, NONE, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0,  },
        { NONE, NONE, 0, 0, 0, 0, NONE, NONE, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 0, 0, 0, 0, 0, 0, 0, 0,  },
        { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NONE, NONE, NONE, 0, 0, 0, 0,  },
        { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NONE, NONE, NONE, NONE, 0, 0, 0, 0, 0, 0, 0, 0, NONE, 0, 0, 0, 0, 0, 0,  },
        };

    // Max values for the effect parameters
    public static final int[][] EFFECT_PARAMETER_MAXES = 
        {
        // 0...99 is actually displayed as 1..100
        // 25 is actually +12db
        { 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100,  },
        { 100, 100, 720, 720, NONE, 720, NONE, NONE, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, NONE, NONE, 100, 100, 100, 100, 100, 100, 100, 100, 20, 99, 99, 24, 24, 24, 24, 24, 24,  },
        { NONE, NONE, 99, 99, 9, 99, NONE, NONE, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 100, 99, 99, 24, 24, 24, 24, 24, 24,  },
        // The 9 below is actually displayed as (1...10).  it is Rotary Acceleration
        { 100, 100, 720, 720, 1270, 720, 720, 720, 100, 200, 100, 100, 200, 200, 100, 100, 200, 200, 100, 200, 100, 200, 100, 200, 100, 100, 200, 200, 9, 99, NONE, NONE, NONE, 99, 99, 200, 200,  },
        { 99, 99, 99, 99, 99, 99, 99, 99, 1, 1, 1, 1, 1, 100, 99, 99, 99, 99, NONE, NONE, NONE, NONE, 1, 1, 99, 99, 99, 99, 1, 99, NONE, 99, 99, 99, 99, 99, 99,  },
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
    "342 Piano Noise Attack",
    "343 EP Noise Attack",
    "344 Percus Noise Attack",
    "345 Dist Gtr Noise Attack",
    "346 Orch Noise Attack",
    "347 Flanged Noise Attack",
    "348 Saw Noise Attack",
    "349 Zipper Noise Attack",
    "350 Organ Noise Loop",
    "351 Violin Noise Loop",
    "352 Crystal Noise Loop",
    "353 Sax Breath Loop",
    "354 Panflute Noise Loop",
    "355 Pipe Noise Loop",
    "356 Saw Noise Loop",
    "357 Gorgo Noise Loop",
    "358 Enhancer Noise Loop",
    "359 Tabla Spect. Noise Loop",
    "360 Cave Spect. Noise Loop",
    "361 White Noise Loop",
    "362 Clavi Attack",
    "363 Digi EP Attack",
    "364 Glocken Attack",
    "365 Vibe Attack",
    "366 Marimba Attack",
    "367 Org Key Click",
    "368 Slap Bass Attack",
    "369 Folk Gtr Attack",
    "370 Gut Gtr Attack",
    "371 Dist Gtr Attack",
    "372 Clean Gtr Attack",
    "373 Muted Gtr Attack",
    "374 Cello & Violin Attack",
    "375 Pizz Violin Attack",
    "376 Pizz Dbl. Bass Attack",
    "377 Doo Attack",
    "378 Trombone Attack",
    "379 Brass Attack",
    "380 F.Horn1 Attack",
    "381 F.Horn2 Attack",
    "382 Flute Attack",
    "383 T.Sax Attack",
    "384 Shamisen Attack",
    "385 Voltage Attack",
    "386 BBDigi Attack",
    "387 BBDX Attack",
    "388 BBBlip Attack",
    "389 Techno Hit Attack",
    "390 Techno Attack",
    "391 X-Piano Attack",
    "392 Noisy Voise Loop",
    "393 Noisy Human Loop",
    "394 Ravoid Loop",
    "395 Hyper Loop",
    "396 Beef Loop",
    "397 Texture Loop",
    "398 MMBass Loop",
    "399 Syn PWM Cyc",
    "400 Harpshichord Cyc",
    "401 Digi EP Cyc",
    "402 Soft EP Cyc",
    "403 Ep Bell Cyc",
    "404 Bandneon Cyc",
    "405 Chees Org Cyc",
    "406 Organ Cyc",
    "407 Oboe Cyc",
    "408 Crystal Cyc",
    "409 Syn Bass1 Cyc",
    "410 Syn Bass2 Cyc",
    "411 Syn Saw1 Cyc",
    "412 Svn Saw2 Cyc",
    "413 Syn Saw3 Cyc",
    "414 Syn Square1 Cyc",
    "415 Syn Square2 Cyc",
    "416 Syn Pulse1 Cyc",
    "417 Syn Pulse2 Cyc",
    "418 Pulse20 Cyc",
    "419 Pulse40 Cyc",
    "420 Nasty Cyc",
    "421 Mini Max Cyc",
    "422 Bottom Cyc",
    "423 > 64th Harmonics Cyc",
    "424 > 64th Harmonics Cyc",                     // Is this a duplicate?
    "425 BD Attack",
    "426 Ana Kick",
    "427 SD Attack",
    "428 Tiny SD Attack",
    "429 Ana SD Attack",
    "430 Ana HHO Attack",
    "431 Simonzu Tom Attack",
    "432 Ride Cup Attack",
    "433 Cowbell Attack",
    "434 Conga Attack",
    "435 Conga Muted Attack",
    "436 Agogo Attack",
    "437 Castanet Attack",
    "438 Claves Attack",
    "439 Tambourine Attack",
    "440 JingleBell Attack",
    "441 BellTree Attack",
    "442 WindowChime Attack",
    "443 AtariGame Attack",
    "444 Rama Attack",
    "445 Udo Attack",
    "446 TablaNa Attack",
    "447 Voice Ou Attack",
    "448 HighQ Attack",
    "449 Super Q Attack",
    "450 Glass Attack",
    "451 Metal Attack",
    "452 Noise Attack",
    "453 Pop Attack",
    "454 Crash Loop",
    "455 Burner Loop",
    "456 Jet Engine Loop",
    "457 Omnibus Loop 1",
    "458 Omnibus Loop 2",
    "459 Omnibus Loop 3",
    "460 Omnibus Loop 4",
    "461 Omnibus Loop 5",
    "462 Omnibus Loop 6",
    "463 Omnibus Loop 7",
    "464 Omnibus Loop 8",
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
// Harmonic Mod Constraints  -- maybe later
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
    static HashMap addWaveSoftHCParamsToIndex = null;
    static HashMap addWaveLoudHCParamsToIndex = null;
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

        if (addWaveSoftHCParamsToIndex == null)
            {
            addWaveSoftHCParamsToIndex = new HashMap();
            for(int i = 0; i < addWaveSoftHCParams.length; i++)
                {
                addWaveSoftHCParamsToIndex.put(addWaveSoftHCParams[i], i);
                }
            }

        if (addWaveLoudHCParamsToIndex == null)
            {
            addWaveLoudHCParamsToIndex = new HashMap();
            for(int i = 0; i < addWaveLoudHCParams.length; i++)
                {
                addWaveLoudHCParamsToIndex.put(addWaveLoudHCParams[i], i);
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
        makeAllEffectParameters();
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
            ((SynthPanel)soundPanel).makePasteable("source");
            addTab("S " + source, sourceTabs[source - 1] = (SynthPanel)soundPanel);
                                
                                
            soundPanel = new SynthPanel(this);
            vbox = new VBox();

            hbox = new HBox();
            hbox.add(addHarmonics(source, Style.COLOR_A()));
            hbox.addLast(addK5Harmonics(source, Style.COLOR_C()));
            vbox.add(hbox);
                
            JComponent c1 = (JComponent)add(addHarmonicDisplay(source, true, Style.COLOR_A()));
            JComponent c2 = (JComponent)add(addHarmonicDisplay(source, false, Style.COLOR_A()));
            HSplitBox split = new HSplitBox(c1, c2);
            hbox = new HBox();
            hbox.addLast(split);
            vbox.add(split);

            vbox.add(addFormant(source, Style.COLOR_B()));
            vbox.add(addFormantDisplay(source, Style.COLOR_B()));
            vbox.add(addMorf(source, Style.COLOR_C()));
                

            soundPanel.add(vbox, BorderLayout.CENTER);
            ((SynthPanel)soundPanel).makePasteable("source");
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
            c1 = (JComponent)addHarmonicEnvelopeDisplay(source, 8, "loop", "Loop", Style.COLOR_B());
            //c2 = (JComponent)addHarmonicEnvelopeDisplay(source, 8, "loop", "Loop", Style.COLOR_B());
            c2 = (JComponent)addK5LevelHarmonics(source, Style.COLOR_C());
            split = new HSplitBox(c1, c2);
            hbox.addLast(split);
            vbox.add(hbox);

            soundPanel.add(vbox, BorderLayout.CENTER);
            ((SynthPanel)soundPanel).makePasteable("source");
            addTab("E " + source, envelopeTabs[source - 1] = (SynthPanel)soundPanel);
            }
            
        model.set("name", EMPTY_PATCH_NAME);
        model.set("bank", 0);
        model.set("number", 0);

        loadDefaults();     
        
        // reset all the Dials
        for(int j = 1; j <= 16; j++)            // note <=
            {
            model.set("dial" + j, 64);
            }
        }
                        
    
    
    /// Associated files
    public String getDefaultResourceFileName() { return "KawaiK5000.init"; }
    public String getHTMLResourceFileName() { return "KawaiK5000.html"; }


    // Asks the user for the bank and patch number 
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
            if (n < 1 || n > 128)
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
        

    // Makes sure the patch name is valid 
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
        
        
    /** Adds the Dials category */
    public JComponent addDials(Color color)
        {
        Category category = new Category(this, "Dials", color);
        category.makeDistributable("dial");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        for(int i = 0; i < 16; i+=4)
            {
            for(int j = i; j < i + 4; j++)
                {
                comp = new LabelledDial(DIALS[j], this, "dial" + (j + 1), color, 0, 127)
                    {
                    public String map(int val) 
                        { 
                        if (val == 0) return "-63";
                        else if (val == 64) return "0";
                        else if (val > 64) return "+" + (val - 64);
                        else return "" + (val - 64);
                        }

                    public boolean isSymmetric() { return true; }
                    };
                model.setStatus("dial" + (j + 1), Model.STATUS_RESTRICTED);
                hbox.add(comp);
                }
            vbox.add(hbox);
            hbox = new HBox();
            }
                
        category.add(vbox, BorderLayout.CENTER);
        return category;
        }

    /** Adds the sources category */
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
            public String map(int val) { if (val == 0) return "Off"; else return "" + val + "->" + (val + 1); }
            };
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
    
    
    /** Revises the tabs to reflect the current source types and number */
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
                else break;
                }
                    
            // Add in selected tabs
            for(int i = model.get("srctype") - 1; i >= 0; i--)
                {
                if (model.get("source" + (i + 1) + "dcoadditive") == 1 && envelopeTabs[i] != null)
                    {
                    //tabs.add(envelopeTabs[i], 1);
                    //tabs.setTitleAt(1, "E " + (i + 1));
                    insertTab("E " + (i + 1), envelopeTabs[i], 1);
                    //tabs.add(harmonicsTabs[i], 1);
                    //tabs.setTitleAt(1, "H " + (i + 1));
                    insertTab("H " + (i + 1), harmonicsTabs[i], 1);
                    }
                if (sourceTabs[i] != null)
                    {
                    //tabs.add(sourceTabs[i], 1);
                    //tabs.setTitleAt(1, "S " + (i + 1));
                    insertTab("S " + (i + 1), sourceTabs[i], 1);
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

    /** Adds the controllers category */
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
        comp = new Chooser("Foot Switch 1", this, "fsw1parameter", params);
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
        
        for(int i = 1; i <= 4; i++)
            {
            vbox = new VBox();
            comp = new LabelledDial("Macro " + i, this, "macrocontroller" + i + "depth1", color, 33, 95, 64);
            ((LabelledDial)comp).addAdditionalLabel("Depth 1");
            vbox.add(comp);
            comp = new LabelledDial("Macro " + i, this, "macrocontroller" + i + "depth2", color, 33, 95, 64);
            ((LabelledDial)comp).addAdditionalLabel("Depth 2");
            vbox.add(comp);
            hbox.add(vbox);
            }

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
        
    /** Makes the Effects category */
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
        
    // Box which holds the reverb dials, so we can rebuild them quickly
    HBox reverbBox = new HBox();

    /** Builds the current reverb dials and puts them into reverbBox */
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
            comp = new LabelledDial("Feedback", this, "reverb" + (type + 1) + "para2", color, 0, 99, -1);
            }
        else
            {
            comp = new LabelledDial("Reverb", this, "reverb" + (type + 1) + "para2", color, 3, 50)
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
            comp = new LabelledDial("Predelay", this, "reverb" + (type + 1) + "para3", color, 0, 100)
                {
                public String map(int val) { return "<html><font size=-1>" + (val * 10 + 200) + " ms</font></html>"; }
                };
            ((LabelledDial)comp).addAdditionalLabel("Time");
            }
        reverbBox.add(comp);

        comp = new LabelledDial("High Freq", this, "reverb" + (type + 1) + "para4", color, 0, 99, -1);
        ((LabelledDial)comp).addAdditionalLabel("Damping");
        reverbBox.add(comp);

        reverbBox.revalidate(); 
        reverbBox.repaint(); 
        
        setSendMIDI(midi);
        }
 
    /** Adds the Reverb category */
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

    /** Adds the EQ category */
    public JComponent addEQ(Color color)
        {
        Category category = new Category(this, "EQ", color);
        category.makeDistributable("geqfreq");

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
       
        
    
    // All four effect boxes, so we can swap in new effects rapidly
    HBox effectBox[] = { new HBox(), new HBox(), new HBox(), new HBox() };

    // The last dial created for effects -- we use this to compute the size of a
    // horizontal strut or two if there aren't enough dials to fill out the effects category
    LabelledDial lastDial = null;           // will always be filled in by the time we need it
    
    /** Prebuilds all the effect parameters */
    public void makeAllEffectParameters()
        {
        int numEffectTypes = EFFECT_PARAMETER_MINS[0].length;
        for(int i = 0; i < NUM_EFFECTS; i++)
            {
            for(int j = 0; j < numEffectTypes; j++)
                {
                makeEffectParameters(i, j);
                }
            }
        }
        
    /** Prebuilds the effect parameter for the given effect number (1-4) and type.
        This does not create any widgets, so it's pretty fast.  */
    public void makeEffectParameters(int effect, int type)
        {
        for(int i = 0; i < 5; i++)
            {                        
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
            
            int max = EFFECT_PARAMETER_MAXES[i][type];
            if (max == -1)  // Not a parameter
                {
                // make some dummies
                model.set("effect" + (effect + 1) + "type" + (type + 1) + (i == 0 ? "depth" : "para" + i), 0);
                model.setMinMax("effect" + (effect + 1) + "type" + (type + 1) + (i == 0 ? "depth" : "para" + i), 0, 0);
                model.setMetricMinMax("effect" + (effect + 1) + "type" + (type + 1) + (i == 0 ? "depth" : "para" + i), 0, 0);
                }
            else 
                {
                int _min = EFFECT_PARAMETER_MINS[i][type];
                int _max = trueMax;
                model.set("effect" + (effect + 1) + "type" + (type + 1) + (i == 0 ? "depth" : "para" + i), _min);
                model.setMinMax("effect" + (effect + 1) + "type" + (type + 1) + (i == 0 ? "depth" : "para" + i), _min, _max);
                model.setMetricMinMax("effect" + (effect + 1) + "type" + (type + 1) + (i == 0 ? "depth" : "para" + i), _min, _max);
                }
            }
        }
 
 
    // EFFECT COMPACTING.
    // My code follows the effects table on Page 123 of the K5000 manual.  Certain effects in this table
    // have holes in their parameters.  My code also has holes.  However it turns out that in both the
    // patch format and in the individual parameter sysex, these holes are compacted; thus if there is a
    // missing parameter 3, then parameter 4 takes its place, and parameter 5 takes 4's place; and there
    // is nothing for parameter 5.  Had I known these holes were compacted I would have shifted everything,
    // but it's too late for that.  So I have to compensate for it in three places:  (1) in the display in
    // the GUI (I compact the parameters to look better) (2) in the parameter sysex, and (3) in emitting
    // and parsing patches.
 
    /** Builds effect #effect for the given type */
    public HBox buildEffect(int effect, int type, Color color)
        {
        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();

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
                String key = "effect" + (effect + 1) + "type" + (type + 1) + (i == 0 ? "depth" : "para" + i);
                boolean addOne = (max == 99 || // all the 0...99 are displayed as 1-100
                    EFFECT_PARAMETER_NAMES[i][type].equals("Accel"));                // Rotary Acceleration is 0...9 but displayed 1...10  
                comp = lastDial = new LabelledDial(s[0], this, key, color, EFFECT_PARAMETER_MINS[i][type], trueMax)
                    {
                    public boolean isSymmetric()
                        {
                        return (max == 76 || max == 24);             // -12dB ... +12dB
                        }
                                        
                    public String map(int val)
                        {
                        // Based on the placeholder "max value", we need to determine the actual value to display
                        // on the LabelledDial.  The complicated one is 720, which means 0-100 by 2, 100-250 by 5, 250-720 by 10.
                        // The others are fairly easy.
                        if (max == 1)
                            {
                            if (type == 28)     // rotary
                                {
                                return (val == 0 ? "Slow" : "Fast");
                                }
                            else                                // all else
                                {
                                return (val == 0 ? "Sin" : "Tri");
                                }
                            }
                        else if (max == 24)
                            {
                            return "" + (max - 12) + "dB";
                            }
                        else if (max == 76)
                            {
                            return "" + (val - 64) + EFFECT_PARAMETER_UNITS[_i][type];
                            }
                        else if (max <= 100)
                            {
                            return String.valueOf(val + (addOne ? 1 : 0) + EFFECT_PARAMETER_UNITS[_i][type]);
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
 
    /** Adds an effect category.  */
    public JComponent addEffect(final int effect, Color color)
        {
        Category category = new Category(this, "Effect " + (effect + 1), color);
        category.makePasteable("effect");

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
                hbox.addLast(buildEffect(effect, model.get("effect" + (effect + 1) + "type"), color));
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
        
    // Triggers the listeners on the given key without changing it.  We need this in the Execute button in the Morf category
    void touch(String key)
        {
        getModel().set(key, getModel().get(key));
        }
        
    /** Adds the Morf category */
    public JComponent addMorf(int source, Color color)
        {
        Category category = new Category(this, "Morf", color);
        category.makePasteable("source");
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        /// MORF
        
        // We no longer have a checkbox for this, but it's still a sysex parameter for some
        // reason.  So we have to include it as a parameter here so it can be randomized
        // arbitrarily. 
        // model.set("source" + source + "morfflag", 0);
        //model.setMinMax("source" + source + "morfflag", 0, 1);

        PushButton doMorph = new PushButton("Execute")
            {
            public void perform()
                {
                byte[] exec = new byte[] { (byte)0xF0, 0x40, (byte)getChannelOut(), 0x10, 0x00, 0x0a, 0x02, 0x46, (byte)(source - 1), 0x00, 0x00, 0x00, 0x01, (byte)0xF7 };

                // Send all the parameters for Morf to make sure the synth has the latest
                touch("source" + source + "morfloop");
                touch("source" + source + "morfhc1patch");
                touch("source" + source + "morfhc2patch");
                touch("source" + source + "morfhc3patch");
                touch("source" + source + "morfhc4patch");
                touch("source" + source + "morfhc1source");
                touch("source" + source + "morfhc2source");
                touch("source" + source + "morfhc3source");
                touch("source" + source + "morfhc4source");
                touch("source" + source + "morfhetime1");
                touch("source" + source + "morfhetime2");
                touch("source" + source + "morfhetime3");
                touch("source" + source + "morfhetime4");
                touch("source" + source + "morfflag");          // just in case
                                
                // The K5000 has some non-morf harmonics parameters on its Morf screen for some reason.
                // So just in case I am also sending all the non-morf harmonics parameters as well in case
                // they are important to properly performing a morf
                touch("source" + source + "harmgroup");
                touch("source" + source + "harmtotalgain");
                touch("source" + source + "harmkstogain");
                touch("source" + source + "harmbalancevelocurve");
                touch("source" + source + "harmbalancevelodepth");

                // execute
                try 
                    {
                    tryToSendSysex(exec);
                    }
                catch (Exception e) { Synth.handleException(e); }
                }
            };
        vbox.add(doMorph);

        /// FIXME: The Docs and the machine disagree on this....                
        params = ENV_LOOPS;
        comp = new Chooser("P3->P2 Loop", this, "source" + source + "morfloop", params);

//        comp = new CheckBox("P3->P2 Loop", this, "source" + source + "morfloop");
 
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("HC1 Patch", this, "source" + source + "morfhc1patch", color, 0, 127, -1);
        hbox.add(comp);
                
        comp = new LabelledDial("HC2 Patch", this, "source" + source + "morfhc2patch", color, 0, 127, -1);
        hbox.add(comp);
                
        comp = new LabelledDial("HC3 Patch", this, "source" + source + "morfhc3patch", color, 0, 127, -1);
        hbox.add(comp);
                
        comp = new LabelledDial("HC4 Patch", this, "source" + source + "morfhc4patch", color, 0, 127, -1);
        hbox.add(comp);
                
        comp = new LabelledDial("HC1 Source", this, "source" + source + "morfhc1source", color, 0, 11)
            {
            public String map(int val)
                {
                if (val < 6) return "<html><font size=\"-1\">Soft&nbsp;" + (val + 1);
                else return "<html><font size=\"-1\">Loud&nbsp;" + (val - 5) + "</font></html>";
                }
            };
        hbox.add(comp);
                
        comp = new LabelledDial("HC2 Source", this, "source" + source + "morfhc2source", color, 0, 11)
            {
            public String map(int val)
                {
                if (val < 6) return "<html><font size=\"-1\">Soft&nbsp;" + (val + 1) + "</font></html>";
                else return "<html><font size=\"-1\">Loud&nbsp;" + (val - 5) + "</font></html>";
                }
            };
        hbox.add(comp);
                
        comp = new LabelledDial("HC3 Source", this, "source" + source + "morfhc3source", color, 0, 11)
            {
            public String map(int val)
                {
                if (val < 6) return "<html><font size=\"-1\">Soft&nbsp;" + (val + 1) + "</font></html>";
                else return "<html><font size=\"-1\">Loud&nbsp;" + (val - 5) + "</font></html>";
                }
            };
        hbox.add(comp);
                
        comp = new LabelledDial("HC4 Source", this, "source" + source + "morfhc4source", color, 0, 11)
            {
            public String map(int val)
                {
                if (val < 6) return "<html><font size=\"-1\">Soft&nbsp;" + (val + 1) + "</font></html>";
                else return "<html><font size=\"-1\">Loud&nbsp;" + (val - 5) + "</font></html>";
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Time 1", this, "source" + source + "morfhetime1", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Time 2", this, "source" + source + "morfhetime2", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Time 3", this, "source" + source + "morfhetime3", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Time 4", this, "source" + source + "morfhetime4", color, 0, 127);
        hbox.add(comp);
        
        vbox = new VBox();
        comp = new CheckBox("Show Morf", this, "source" + source + "morfflag");
        vbox.add(comp);
        hbox.add(vbox);
                
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
 
 
    /** Adds the Harmonics category */
    public JComponent addHarmonics(int source, Color color)
        {
        Category category = new Category(this, "Harmonics", color);
        category.makePasteable("source" + source);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        /// NON-MORF

        params = HARMONIC_GROUPS;
        comp = new Chooser("Harmonic Group", this, "source" + source + "harmgroup", params);
        vbox.add(comp);
        hbox.add(vbox);

        hbox.add(Strut.makeHorizontalStrut(8));

        comp = new LabelledDial("Gain", this, "source" + source + "harmtotalgain", color, 1, 63);
        hbox.add(comp);

        comp = new LabelledDial("KS to Gain", this, "source" + source + "harmkstogain", color, 1, 127, 64);
        hbox.add(comp);

        comp = new LabelledDial("Balance", this, "source" + source + "harmbalancevelocurve", color, 0, 11);
        ((LabelledDial)comp).addAdditionalLabel("Vel. Curve");               // this makes the height the same between LFO and ENV
        hbox.add(comp);

        comp = new LabelledDial("Balance", this, "source" + source + "harmbalancevelodepth", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Vel. Depth");               // this makes the height the same between LFO and ENV
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }       
  
    
    /** Adds the Formant category -- not the formants themselves. */    
    public JComponent addFormant(int source, Color color)
        {
        Category category = new Category(this, "Formant", color);
        category.makePasteable("source");

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
            new double[] { 1.0 / 126, 1.0 / 126, 1.0 / 126, 1.0 / 126, 1.0 / 126, 1.0 / 126 })
            {
            public double preprocessXKey(int index, String key, double value)
                {
                if (index == 6) return value;           // it's the sustain
                else return 127.0 - value;              // rates are flipped from other envelopes, grrr
                }
            };
        disp.setAxis(63 / 127.0);               // dunno if this will work, I may be off by a pixel
        envh.addLast(disp);

        hbox.revalidate();
        hbox.repaint(); 

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
                
                        
    /** Adds the per-source "General" category. */    
    public JComponent addGeneral(int source, Color color)
        {
        Category category = new Category(this, "General", color);
        category.makePasteable("source");
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();

                
        params = VELO_SW;
        comp = new Chooser("Velo Switch", this, "source" + source + "generalvelosw", params);
        hbox.add(comp);

        params = PAN_TYPES;
        comp = new Chooser("Pan Type", this, "source" + source + "generalpantype", params);
        hbox.add(comp);
        
        vbox.add(hbox);
        
        hbox = new HBox();
            
        comp = new LabelledDial("Velo Value", this, "source" + source + "generalvelo", color, 0, 31)
            {
            /// FIXME: These values need to be tested
            public String map(int val) { if (val == 0) return "4"; else return "" + (val * 4 + 3); }
            };
        hbox.add(comp);

        comp = new LabelledDial("Pan", this, "source" + source + "generalpannormalvalue", color, 1, 127, 64)
            {
            public String map(int val) 
                { 
                if (val == 64) return "--"; 
                else if (val < 64) return "<" + (64 - val);
                else return "" + (val - 64) + ">";
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Effect Path", this, "source" + source + "generalzonepath", color, 0, 3, -1);
        hbox.add(comp);

        comp = new LabelledDial("Pitch Bend", this, "source" + source + "generalbenderpitch", color, 0, 24);
        hbox.add(comp);

        comp = new LabelledDial("Key On Delay", this, "source" + source + "generalkeyondelay", color, 0, 127);
        //((LabelledDial)comp).addAdditionalLabel("Delay");
        hbox.add(comp);
        vbox.add(hbox);
        hbox = new HBox();
                
        comp = new LabelledDial("Volume", this, "source" + source + "generalvolume", color, 0, 127);
        hbox.add(comp);
                
        comp = new LabelledDial("Zone Low", this, "source" + source + "generalzonelo", color, 0, 127)
            {
// Zone Low/Hi starts at 0 = C -2
            public String map(int val) { return KEYS[val % 12] + ((val / 12) - 2); }
            };
        hbox.add(comp);

        comp = new LabelledDial("Zone Hi", this, "source" + source + "generalzonehi", color, 0, 127)
            {
// Zone Low/Hi starts at 0 = C -2
            public String map(int val) { return KEYS[val % 12] + ((val / 12) - 2); }
            };
        hbox.add(comp);

        comp = new LabelledDial("PB Cutoff", this, "source" + source + "generalbendercutoff", color, 0, 31);
        hbox.add(comp);

        vbox.add(hbox);
                
        category.add(vbox, BorderLayout.CENTER);
        return category;
        }
    
        
    /** Adds the per-source "Control" category. */    
    public JComponent addControl(int source, Color color)
        {
        Category category = new Category(this, "Control", color);
        category.makePasteable("source");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();

        //hbox.add(vbox);
        vbox = new VBox();

        params = DESTINATIONS;
        comp = new Chooser("Pressure Destination 1", this, "source" + source + "controlpressdestination1", params);
        vbox.add(comp);

        params = DESTINATIONS;
        comp = new Chooser("Mod Wheel Destination 1", this, "source" + source + "controlwheeldestination1", params);
        vbox.add(comp);

        params = DESTINATIONS;
        comp = new Chooser("Expression Destination 1", this, "source" + source + "controlexpressdestination1", params);
        vbox.add(comp);

        params = SOURCES;
        comp = new Chooser("Assignable Source 1", this, "source" + source + "controlassignable1source", params);
        vbox.add(comp);

        params = DESTINATIONS;
        comp = new Chooser("Assignable Destination 1", this, "source" + source + "controlassignable1destination", params);
        vbox.add(comp);

        hbox.add(vbox);
        vbox = new VBox();

        params = DESTINATIONS;
        comp = new Chooser("Pressure Destination 2", this, "source" + source + "controlpressdestination2", params);
        vbox.add(comp);

        params = DESTINATIONS;
        comp = new Chooser("Mod Wheel Destination 2", this, "source" + source + "controlwheeldestination2", params);
        vbox.add(comp);

        params = DESTINATIONS;
        comp = new Chooser("Expression Destination 2", this, "source" + source + "controlexpressdestination2", params);
        vbox.add(comp);

        params = SOURCES;
        comp = new Chooser("Assignable Source 2", this, "source" + source + "controlassignable2source", params);
        vbox.add(comp);

        params = DESTINATIONS;
        comp = new Chooser("Assignable Destination 2", this, "source" + source + "controlassignable2destination", params);
        vbox.add(comp);
                
        hbox.add(vbox);

        VBox vbox1 = new VBox();
        HBox hbox1 = new HBox();
                
        comp = new LabelledDial("Pressure", this, "source" + source + "controlpressdepth1", color, 33, 95, 64);
        ((LabelledDial)comp).addAdditionalLabel("Depth 1");
        hbox1.add(comp);
                
        comp = new LabelledDial("Mod Wheel", this, "source" + source + "controlwheeldepth1", color, 33, 95, 64);
        ((LabelledDial)comp).addAdditionalLabel("Depth 1");
        hbox1.add(comp);
                
        comp = new LabelledDial("Expression", this, "source" + source + "controlexpressdepth1", color, 33, 95, 64);
        ((LabelledDial)comp).addAdditionalLabel("Depth 1");
        hbox1.add(comp);
                
        comp = new LabelledDial("Assignable", this, "source" + source + "controlassignable1depth", color, 33, 95, 64);
        ((LabelledDial)comp).addAdditionalLabel("Depth 1");
        hbox1.add(comp);

        vbox1.add(hbox1);
        hbox1 = new HBox();

        comp = new LabelledDial("Pressure", this, "source" + source + "controlpressdepth2", color, 33, 95, 64);
        ((LabelledDial)comp).addAdditionalLabel("Depth 2");
        hbox1.add(comp);

        comp = new LabelledDial("Mod Wheel", this, "source" + source + "controlwheeldepth2", color, 33, 95, 64);
        ((LabelledDial)comp).addAdditionalLabel("Depth 2");
        hbox1.add(comp);
                
        comp = new LabelledDial("Expression", this, "source" + source + "controlexpressdepth2", color, 33, 95, 64);
        ((LabelledDial)comp).addAdditionalLabel("Depth 2");
        hbox1.add(comp);
                
        comp = new LabelledDial("Assignable", this, "source" + source + "controlassignable2depth", color, 33, 95, 64);
        ((LabelledDial)comp).addAdditionalLabel("Depth 2");
        hbox1.add(comp);

        vbox1.add(hbox1);
        hbox.add(vbox1);
                                
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    /** Adds the per-source "DCO" category. */    
    public JComponent addDCO(int source, Color color)
        {
        Category category = new Category(this, "DCO", color);
        category.makePasteable("source");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
                
        params = SR_WAVES;
        JComponent pcmwave = new Chooser("PCM Wave", this, "source" + source + "dcowavekit", params);

        VBox wavebox = vbox;
                
        params = SOURCE_TYPES;
        comp = new Chooser("Source Type", this, "source" + source + "dcoadditive", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                wavebox.remove(pcmwave);
                if (model.get("source" + source + "dcoadditive") == 0)
                    {
                    wavebox.add(pcmwave);
                    }
                wavebox.revalidate();
                wavebox.repaint();
                updateTabs();
                }
            };
        model.set("source" + source + "dcoadditive", 1);
        vbox.add(comp);
        //vbox.add(pcmwave);
        hbox.add(vbox);

        comp = new LabelledDial("Coarse", this, "source" + source + "dcocoarse", color, 40, 88, 64);
        hbox.add(comp);

        comp = new LabelledDial("Fine", this, "source" + source + "dcofine", color, 1, 127, 64);
        hbox.add(comp);

        comp = new LabelledDial("Fixed", this, "source" + source + "dcofixedkey", color, 0, 108 - 20)
            {
            public String map(int val) 
                { 
                // A very curious keymap  -- the range actually goes 0 = off, 21 = A-1, ..., 108 = C7
                // I am condensing it here to a consistent range of 0 = off, 1 = A-1, 108 - 20 = C7
                // But we have to deal with it in emitting and parsing
                if (val == 0) return "Off";
                if (val == 1) return "A-1";
                if (val == 2) return "Bb-1";
                if (val == 3) return "B-1";
                return KEYS[(val - 4) % 12] + (val - 4) / 12;
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Key");
        hbox.add(comp);

        comp = new LabelledDial("KS Pitch", this, "source" + source + "dcokspitch", color, 0, 3)
            {
            public String map(int val) { return CENTS[val]; }
            };
        hbox.add(comp);
                
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    /** Adds the per-source "PitchEnv" category. */    
    public JComponent addPitchEnv(int source, Color color)
        {
        Category category = new Category(this, "Pitch Envelope", color);
        category.makePasteable("source");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
                
        comp = new LabelledDial("Start", this, "source" + source + "envdcostartlevel", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);

        comp = new LabelledDial("Attack", this, "source" + source + "envdcoattacktime", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Time");
        hbox.add(comp);

        comp = new LabelledDial("Attack", this, "source" + source + "envdcoattacklevel", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);

        comp = new LabelledDial("Decay", this, "source" + source + "envdcodecaytime", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Time");
        hbox.add(comp);

        comp = new LabelledDial("Level Velo", this, "source" + source + "envdcolevelvelosense", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Sense");
        hbox.add(comp);

        comp = new LabelledDial("Time Velo", this, "source" + source + "envdcotimevelosense", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Sense");
        hbox.add(comp);

        EnvelopeDisplay disp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "source" + source + "envdcoattacktime", "source" + source + "envdcodecaytime"  },
            new String[] { "source" + source + "envdcostartlevel", "source" + source + "envdcoattacklevel", null },
            new double[] { 0, 1.0 / 2 / 127.0, 1.0 / 2 / 127.0 },
            new double[] { 1.0 / 126, 1.0 / 126, 1.0 / 126 });
        disp.setAxis(63 / 127.0);               // dunno if this will work, I may be off by a pixel
        hbox.addLast(disp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    /** Adds the per-source "DCF" category. */    
    public JComponent addDCF(int source, Color color)
        {
        Category category = new Category(this, "DCF", color);
        category.makePasteable("source");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox(); 
                
        comp = new CheckBox("Active", this, "source" + source + "dcfenable", true);
        model.set("source" + source + "dcfenable", 1);
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

        comp = new LabelledDial("Velo->Env Level", this, "source" + source + "envdcfvelolevel", color, 1, 127, 64);
        //((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    /** Adds the per-source "DCF Env" category. */    
    public JComponent addDCFEnv(int source, Color color)
        {
        Category category = new Category(this, "DCF Envelope", color);
        category.makePasteable("source");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
                
        comp = new LabelledDial("Attack", this, "source" + source + "envdcfattacktime", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Time");
        hbox.add(comp);

        comp = new LabelledDial("Decay 1", this, "source" + source + "envdcfdecay1time", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Time");
        hbox.add(comp);

        comp = new LabelledDial("Decay 1", this, "source" + source + "envdcfdecay1level", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);

        comp = new LabelledDial("Decay 2", this, "source" + source + "envdcfdecay2time", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Time");
        hbox.add(comp);

        comp = new LabelledDial("Decay 2", this, "source" + source + "envdcfdecay2level", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);

        comp = new LabelledDial("Release", this, "source" + source + "envdcfreleasetime", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Time");
        hbox.add(comp);

        comp = new LabelledDial("KS->Env", this, "source" + source + "envdcfksattacktime", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Attack Time");
        hbox.add(comp);

        comp = new LabelledDial("KS->Env", this, "source" + source + "envdcfksdecay1time", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Decay 1 Time");
        hbox.add(comp);

        comp = new LabelledDial("Velo->Env", this, "source" + source + "envdcfveloattacktime", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Attack Time");
        hbox.add(comp);

        comp = new LabelledDial("Velo->Env", this, "source" + source + "envdcfvelodecay1time", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Decay1 Time");
        hbox.add(comp);

        comp = new LabelledDial("Env Depth", this, "source" + source + "envdcfdepth", color, 1, 127, 64);
        hbox.add(comp);


        EnvelopeDisplay disp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "source" + source + "envdcfattacktime", "source" + source + "envdcfdecay1time",  "source" + source + "envdcfdecay2time",  null,                                                                        "source" + source + "envdcfreleasetime" },
            new String[] { null, null,                                                                   "source" + source + "envdcfdecay1level", "source" + source + "envdcfdecay2level", "source" + source + "envdcfdecay2level", null },
            new double[] { 0, 1.0 / 5 / 127.0, 1.0 / 5 / 127.0, 1.0 / 5 / 127.0, 1.0 / 5, 1.0 / 5 / 127.0 },
            new double[] { 0.5, 1.0, 1.0 / 126, 1.0 / 126, 1.0 / 126, 1.0 / 126 });
        disp.setAxis(63 / 127.0);               // dunno if this will work, I may be off by a pixel
        hbox.addLast(disp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }



    /** Adds the per-source "DCA" category. */    
    public JComponent addDCA(int source, Color color)
        {
        Category category = new Category(this, "DCA", color);
        category.makePasteable("source");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
                
        /// FIXME: I have made this consistent with DCF Velo curve, the documentation might be wrong
        comp = new LabelledDial("Velo Curve", this, "source" + source + "dcavelocurve", color, 0, 11, -1);
        hbox.add(comp);

        EnvelopeDisplay disp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "source" + source + "envdcaattacktime", "source" + source + "envdcadecay1time",  "source" + source + "envdcadecay2time",  null,                                                                        "source" + source + "envdcareleasetime" },
            new String[] { null, null,                                                                   "source" + source + "envdcadecay1level", "source" + source + "envdcadecay2level", "source" + source + "envdcadecay2level", null },
            new double[] { 0, 1.0 / 5 / 127.0, 1.0 / 5 / 127.0, 1.0 / 5 / 127.0, 1.0 / 5, 1.0 / 5 / 127.0 },
            new double[] { 0.0, 1.0, 1.0 / 127, 1.0 / 127, 1.0 / 127, 1.0 / 127 });
        // new double[] { 0.5, 1.0, 1.0 / 126, 1.0 / 126, 1.0 / 126, 1.0 / 126 });
//disp.setAxis(63 / 127.0);               // dunno if this will work, I may be off by a pixel
        hbox.addLast(disp);


        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    /** Adds the per-source "DCA Envelope" category. */    
    public JComponent addDCAEnv(int source, Color color)
        {
        Category category = new Category(this, "DCA Envelope", color);
        category.makePasteable("source");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
                
        comp = new LabelledDial("Attack", this, "source" + source + "envdcaattacktime", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Time");
        hbox.add(comp);

        comp = new LabelledDial("Decay 1", this, "source" + source + "envdcadecay1time", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Time");
        hbox.add(comp);

        comp = new LabelledDial("Decay 1", this, "source" + source + "envdcadecay1level", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);

        comp = new LabelledDial("Decay 2", this, "source" + source + "envdcadecay2time", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Time");
        hbox.add(comp);

        comp = new LabelledDial("Decay 2", this, "source" + source + "envdcadecay2level", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);

        comp = new LabelledDial("Release", this, "source" + source + "envdcareleasetime", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Time");
        hbox.add(comp);

        comp = new LabelledDial("KS->Env", this, "source" + source + "envdcaksattacktime", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Attack Time");
        hbox.add(comp);

        comp = new LabelledDial("KS->Env", this, "source" + source + "envdcaksdecay1time", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Decay 1 Time");
        hbox.add(comp);

        comp = new LabelledDial("KS->Env", this, "source" + source + "envdcaksreleasetime", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Release Time");
        hbox.add(comp);

        comp = new LabelledDial("Velo->Env", this, "source" + source + "envdcaveloattacktime", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Attack Time");
        hbox.add(comp);

        comp = new LabelledDial("Velo->Env", this, "source" + source + "envdcavelodecay1time", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Decay1 Time");
        hbox.add(comp);

        comp = new LabelledDial("Velo->Env", this, "source" + source + "envdcaveloreleasetime", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Release Time");
        hbox.add(comp);

        comp = new LabelledDial("KS->Env", this, "source" + source + "envdcakslevel", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);

        comp = new LabelledDial("Velo->Env", this, "source" + source + "envdcavelolevel", color, 0, 63);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    /** Adds the per-source "LFO" category. */    
    public JComponent addLFO(int source, Color color)
        {
        Category category = new Category(this, "LFO", color);
        category.makePasteable("source");

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
        hbox.add(comp);

        comp = new LabelledDial("Fade In to Speed", this, "source" + source + "lfofadeintospeed", color, 0, 63);
        hbox.add(comp);

        comp = new LabelledDial("Pitch Vibrato", this, "source" + source + "lfopitchdepth", color, 0, 63);
        hbox.add(comp);

        comp = new LabelledDial("KS->Vibrato", this, "source" + source + "lfopitchks", color, 1, 127, 64);
        hbox.add(comp);

        comp = new LabelledDial("DCF Growl", this, "source" + source + "lfodcfdepth", color, 0, 63);
        hbox.add(comp);

        comp = new LabelledDial("KS->Growl", this, "source" + source + "lfodcfks", color, 1, 127, 64);
        hbox.add(comp);

        comp = new LabelledDial("DCA Tremelo", this, "source" + source + "lfodcadepth", color, 0, 63);
        hbox.add(comp);

        comp = new LabelledDial("KS->Tremelo", this, "source" + source + "lfodcaks", color, 1, 127, 64);
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }



    // Formant widths, heights, and associated parameters
    static final double[] FORMANT_WIDTHS = new double[128];
    static final double[] FORMANT_HEIGHTS = new double[128];
    static final String[][] FORMANT_STRINGS = new String[6][128];

    // Harmonic and harmonic envelopes widths, heights, envelope level heights and loop heights
    static final double[] HC_WIDTHS = new double[64];
    static final double[] HC_HEIGHTS = new double[64];
    static final double[] LEVEL_HEIGHTS = new double[64];
    static final double[] LOOP_HEIGHTS = new double[64];

    // Associated harmonic parameters
    static final String[][] HC_SOFT_STRINGS = new String[6][64];
    static final String[][] HC_LOUD_STRINGS = new String[6][64];
          
    // Prebuild all the heights, widths, and parameters for various envelopes so they can 
    // be reused many times.     
    static
        {
        for(int i = 0; i < FORMANT_HEIGHTS.length; i++) FORMANT_HEIGHTS[i] = 1.0 / 127.0;
        FORMANT_WIDTHS[0] = 0;  // (1.0 / 128.0) / 2;
        for(int i = 1; i < FORMANT_WIDTHS.length; i++) FORMANT_WIDTHS[i] = 1.0 / 128.0;
        for(int i = 0; i < FORMANT_STRINGS.length; i++)
            {
            for(int j = 0; j < FORMANT_STRINGS[i].length; j++)
                {
                FORMANT_STRINGS[i][j] = "source" + (i + 1) + "formantband" + (j + 1);
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
                HC_SOFT_STRINGS[i][j] = "source" + (i + 1) + "hc0s" + (j + 1);
                }
            }
        for(int i = 0; i < HC_LOUD_STRINGS.length; i++)
            {
            for(int j = 0; j < HC_LOUD_STRINGS[i].length; j++)
                {
                HC_LOUD_STRINGS[i][j] = "source" + (i + 1) + "hc1s" + (j + 1);
                }
            }
        }
                        
    /** Add the per-source Formant Bands category */
    public JComponent addFormantDisplay(int source, Color color)
        {
        Category category = new Category(this, "Formant Bands", color);
        category.makeDistributable("source");
        category.makePasteable("source");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        final boolean[] setting = { false };
        
        for(int i = 0; i < 128; i++)
            {
            String key = "source" + source + "formantband" + (i + 1);
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
                boolean push = undo.getWillPush();
                undo.setWillPush(false);
                model.set("source" + source + "formantbandnumber", formant);
                model.set("source" + source + "formantbandvalue", (int) y * 127);
                undo.setWillPush(push);
                // We set this AFTER setting the number and value so it can be the last set parameter, which allows us to do distribution
                model.set("source" + source + "formantband" + (formant + 1), (int)(y * 127));
                setting[0] = false;
                }

            public void updateHighlightIndex(int index)
                {
                setting[0] = true;
                boolean push = undo.getWillPush();
                undo.setWillPush(false);
                // We grab, then restore the last key so we can retain the last set parameter, which allows us to do distribution
                String last = model.getLastKey();
                model.set("source" + source + "formantbandnumber", index);
                model.set("source" + source + "formantbandvalue", model.get("source" + source + "formantband" + (index + 1)));
                model.setLastKey(last);
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
                        

        comp = new LabelledDial("Number", this, "source" + source + "formantbandnumber", color, 0, 127, -1) 
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
        model.setStatus("source" + source + "formantbandnumber", Model.STATUS_RESTRICTED);
        vbox.add(comp);

        comp = new LabelledDial("Value", this, "source" + source + "formantbandvalue", color, 0, 127)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                if (!setting[0])
                    {
                    int val = display.getHighlightIndex();
                    if (val != EnvelopeDisplay.NO_HIGHLIGHT)
                        {
                        model.set("source" + source + "formantband" + (val + 1), model.get(key));
                        display.repaint();
                        }
                    }
                }
            };
        model.setStatus("source" + source + "formantbandvalue", Model.STATUS_RESTRICTED);
        vbox.add(comp);
                
        hbox.add(vbox);
        
        hbox.addLast(display);
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    /** Add the per-source Harmonics category, both Soft and Loud */
    public JComponent addHarmonicDisplay(int source, boolean soft, Color color)
        {
        Category category = new Category(this, (soft ? "Soft " : "Loud ") + "Harmonics", color); // , buildHarmonicMenu(source, soft));
        category.makeDistributable("source" + source);
        category.makePasteable("source" + source);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        final boolean[] setting = { false };
        
        for(int i = 0; i < 64; i++)
            {
            String key = "source" + source + "hc" + (soft ? "0" : "1") + "s" + (i + 1);
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
                boolean push = undo.getWillPush();
                undo.setWillPush(false);
                model.set("source" + source + "hc" + (soft ? "0" : "1") + "number", hc);
                model.set("source" + source + "hc" + (soft ? "0" : "1") + "value", (int) y * 127);
                undo.setWillPush(push);
                // We set this AFTER setting the number and value so it can be the last set parameter, which allows us to do distribution
                model.set("source" + source + "hc" + (soft ? "0" : "1") + "s" + (hc + 1), (int)(y * 127));
                setting[0] = false;
                }

            public void updateHighlightIndex(int index)
                {
                setting[0] = true;
                boolean push = undo.getWillPush();
                undo.setWillPush(false);
                // We grab, then restore the last key so we can retain the last set parameter, which allows us to do distribution
                String last = model.getLastKey();
                model.set("source" + source + "hc" + (soft ? "0" : "1") + "number", index);
                model.set("source" + source + "hc" + (soft ? "0" : "1") + "value", model.get("source" + source + "hc" + (soft ? "0" : "1") + "s" + (index + 1)));
                model.setLastKey(last);
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
                        

        comp = new LabelledDial("Number", this, "source" + source + "hc" + (soft ? "0" : "1") + "number", color, 0, 63, -1)       
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
        model.setStatus("source" + source + "hc" + (soft ? "0" : "1") + "number", Model.STATUS_RESTRICTED);
        vbox.add(comp);

        comp = new LabelledDial("Value", this, "source" + source + "hc" + (soft ? "0" : "1") + "value", color, 0, 127)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                if (!setting[0])
                    {
                    int val = display.getHighlightIndex();
                    if (val != EnvelopeDisplay.NO_HIGHLIGHT)
                        {
                        model.set("source" + source +  "hc" + (soft ? "0" : "1") + "s"  + (val + 1), model.get(key));
                        display.repaint();
                        }
                    }
                }
            };
        model.setStatus("source" + source + "hc" + (soft ? "0" : "1") + "value", Model.STATUS_RESTRICTED);
        vbox.add(comp);
                
        hbox.add(vbox);
        
        hbox.addLast(display);
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }



    // Are we currently changing the parameters from within an envelope display?
    boolean envelopeSetting;
    // Are we currently changing the highlight number parameter?
    boolean highlightSetting;
    // Do we permit the user to change parameters from within an envelope display?  This is set using a menu option.
    boolean allowUpdateFromMouse = true;
    // Do we change patch after doing a send?  This could get annoying if you're trying to edit the patch
    // on the synthesizer directly while also using Edisyn.
    //boolean changePatchAfterSend = true;
    // Do we always do a backup after a Librarian Write Patch operation of some sort?
//      boolean backupAfterLibrarianWrite = false;
    // Do we always do a backup after a Write to Patch operation?
    boolean backupAfterWriteToPatch = false;
    // Do we always send parameters, or just on send current patch?
    // boolean sendsParametersOnlyOnSendCurrentPatch = false;
        
    // The 9 parts to a harmonics envelope        
    static final String[] PARTS = { "rate0", "level0", "rate1", "level1", "rate2", "level2", "rate3", "level3", "loop" };
    // All envelope displays by SOURCE, then PART 
    EnvelopeDisplay envelopeDisplays[][] = new EnvelopeDisplay[6][9];
    // The reduced border around a harmonics envelope, so we can cram 'em on the screen    
    public static final int BORDER = 8;
    
    /** Builds a harmonic envelope display, given a source and part, plus a parameter for the part */ 
    public JComponent addHarmonicEnvelopeDisplay(int source, int partVal, String part, String title, Color color)
        {
        Category category = null;
        /*
          if (partVal % 2 == 1)  // it's a level
          category = new Category(this, title, color, buildLevelMenu(source, partVal));
          else
        */
        category = new Category(this, title, color);
        category.makeDistributable("source" + source);
        category.makePasteable("source" + source);

        final String[] keys = new String[64];
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        int max = (part.startsWith("rate") ? 127 : 63);
        
        for(int i = 0; i < 64; i++)
            {
            keys[i] = "source" + source + "hcenv" + part + "s" + (i + 1);
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
                if (!mouseDown && !allowUpdateFromMouse) return;

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
                boolean push = undo.getWillPush();
                undo.setWillPush(false);
                model.set("source" + source + "hcenv" + "number", hc);
                if (partVal == DISPLAY_LOOP) model.set("source" + source + "hcenv" + part + "value", (int) y * 2);
                else model.set("source" + source + "hcenv" + part + "value", (int) y * max);
                undo.setWillPush(push);
                // We set this AFTER setting the number and value so it can be the last set parameter, which allows us to do distribution
                if (partVal == DISPLAY_LOOP) model.set("source" + source + "hcenv" + part + "s" + (hc + 1), (int)(y * 2));
                else model.set("source" + source + "hcenv" + part + "s" + (hc + 1), (int)(y * max));
                envelopeSetting = false;
                }

            public void updateHighlightIndex(int index)
                {
                if (!mouseDown && !allowUpdateFromMouse) return;
               
                envelopeSetting = true;
                boolean push = undo.getWillPush();
                undo.setWillPush(false);
                // We grab, then restore the last key so we can retain the last set parameter, which allows us to do distribution
                String last = model.getLastKey();
                model.set("source" + source + "hcenv" + "number", index);
                model.set("source" + source + "hcenv" + part + "value", model.get("source" + source + "hcenv" + part + "s" + (index + 1)));
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
                model.setLastKey(last);
                undo.setWillPush(push);
                envelopeSetting = false;
                }

             
            public int highlightIndex(double x, double y, boolean continuation, MouseEvent evt)
                {
                if (!mouseDown && !allowUpdateFromMouse) return getHighlightIndex();
                
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
    
    // Unique constants for each harmonic display 
    public static final int DISPLAY_RATE_0 = 0;
    public static final int DISPLAY_LEVEL_0 = 1;
    public static final int DISPLAY_RATE_1 = 2;
    public static final int DISPLAY_LEVEL_1 = 3;
    public static final int DISPLAY_RATE_2 = 4;
    public static final int DISPLAY_LEVEL_2 = 5;
    public static final int DISPLAY_RATE_3 = 6;
    public static final int DISPLAY_LEVEL_3 = 7;
    public static final int DISPLAY_LOOP = 8;
    
    // Update the harmonic displays, except possibly one of them, in response to changing a parameter
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
        
    // Return the current highlight index for a given envelope display for a given source and part
    int getHighlightIndex(int source, int part)
        {
        source--;
        
        if (envelopeDisplays[source][part] != null)
            {
            return envelopeDisplays[source][part].getHighlightIndex();
            }
        else return EnvelopeDisplay.NO_HIGHLIGHT;
        }
    
    /** Adds the per-source harmonics envelope header */
    public JComponent addHarmonicsEnvelopeHeader(int source, Color color)
        {
        Category category = new Category(this, "Harmonics Envelope", color);
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();


        comp = new LabelledDial("Number", this, "source" + source + "hcenv" + "number", color, 0, 63)       
            {
            public String map(int val)
                {
                if (model.get("source" + source + "harmgroup") == 0) return "" + (val + 1);
                else return "" + (64 + val + 1);
                }
                
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
                    model.set("source" + source + "hcenv" + "rate0value", model.get("source" + source + "hcenv" + "rate0"  + "s" + (model.get(key) + 1)));
                    model.set("source" + source + "hcenv" + "level0value", model.get("source" + source + "hcenv" + "level0" + "s" + (model.get(key) + 1)));
                    model.set("source" + source + "hcenv" + "rate1value", model.get("source" + source + "hcenv" + "rate1" + "s" + (model.get(key) + 1)));
                    model.set("source" + source + "hcenv" + "level1value", model.get("source" + source + "hcenv" + "level1" + "s" + (model.get(key) + 1)));
                    model.set("source" + source + "hcenv" + "rate2value", model.get("source" + source + "hcenv" + "rate2" + "s" + (model.get(key) + 1)));
                    model.set("source" + source + "hcenv" + "level2value", model.get("source" + source + "hcenv" + "level2" + "s" + (model.get(key) + 1)));
                    model.set("source" + source + "hcenv" + "rate3value", model.get("source" + source + "hcenv" + "rate3" + "s" + (model.get(key) + 1)));
                    model.set("source" + source + "hcenv" + "level3value", model.get("source" + source + "hcenv" + "level3" + "s" + (model.get(key) + 1)));
                    model.set("source" + source + "hcenv" + "loopvalue", model.get("source" + source + "hcenv" + "loop" + "s" + (model.get(key) + 1)));
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
                        model.set("source" + source +  "hcenv" + "loop" + "s" + (val + 1), model.get(key));
                        envelopeDisplays[source-1][DISPLAY_RATE_0].repaint();
                        }
                    }
                }
            };
        model.setStatus("source" + source + "hcenv" + "loopvalue", Model.STATUS_RESTRICTED);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Rate 0", this, "source" + source + "hcenvrate0value", color, 0, 127)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                if (!envelopeSetting)
                    {
                    int val = getHighlightIndex(source, DISPLAY_RATE_0);
                    if (val != EnvelopeDisplay.NO_HIGHLIGHT)
                        {
                        model.set("source" + source +  "hcenvrate0s" + (val + 1), model.get(key));
                        envelopeDisplays[source-1][DISPLAY_RATE_0].repaint();
                        }
                    }
                }
            };
        model.setStatus("source" + source + "hcenvrate0value", Model.STATUS_RESTRICTED);
        hbox.add(comp);
                
        comp = new LabelledDial("Level 0", this, "source" + source + "hcenvlevel0value", color, 0, 63)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                if (!envelopeSetting)
                    {
                    int val = getHighlightIndex(source, DISPLAY_LEVEL_0);
                    if (val != EnvelopeDisplay.NO_HIGHLIGHT)
                        {
                        model.set("source" + source +  "hcenvlevel0s" + (val + 1), model.get(key));
                        envelopeDisplays[source-1][DISPLAY_LEVEL_0].repaint();
                        }
                    }
                }
            };
        model.setStatus("source" + source + "hcenvlevel0value", Model.STATUS_RESTRICTED);
        hbox.add(comp);


        comp = new LabelledDial("Rate 1", this, "source" + source + "hcenvrate1value", color, 0, 127)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                if (!envelopeSetting)
                    {
                    int val = getHighlightIndex(source, DISPLAY_RATE_1);
                    if (val != EnvelopeDisplay.NO_HIGHLIGHT)
                        {
                        model.set("source" + source +  "hcenvrate1s" + (val + 1), model.get(key));
                        envelopeDisplays[source-1][DISPLAY_RATE_1].repaint();
                        }
                    }
                }
            };
        model.setStatus("source" + source + "hcenvrate1value", Model.STATUS_RESTRICTED);
        hbox.add(comp);
                
        comp = new LabelledDial("Level 1", this, "source" + source + "hcenvlevel1value", color, 0, 63)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                if (!envelopeSetting)
                    {
                    int val = getHighlightIndex(source, DISPLAY_LEVEL_1);
                    if (val != EnvelopeDisplay.NO_HIGHLIGHT)
                        {
                        model.set("source" + source +  "hcenvlevel1s" + (val + 1), model.get(key));
                        envelopeDisplays[source-1][DISPLAY_LEVEL_1].repaint();
                        }
                    }
                }
            };
        model.setStatus("source" + source + "hcenvlevel1value", Model.STATUS_RESTRICTED);
        hbox.add(comp);
                

        comp = new LabelledDial("Rate 2", this, "source" + source + "hcenvrate2value", color, 0, 127)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                if (!envelopeSetting)
                    {
                    int val = getHighlightIndex(source, DISPLAY_RATE_2);
                    if (val != EnvelopeDisplay.NO_HIGHLIGHT)
                        {
                        model.set("source" + source +  "hcenvrate2s" + (val + 1) + "", model.get(key));
                        envelopeDisplays[source-1][DISPLAY_RATE_2].repaint();
                        }
                    }
                }
            };
        model.setStatus("source" + source + "hcenvrate2value", Model.STATUS_RESTRICTED);
        hbox.add(comp);
                
        comp = new LabelledDial("Level 2", this, "source" + source + "hcenvlevel2value", color, 0, 63)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                if (!envelopeSetting)
                    {
                    int val = getHighlightIndex(source, DISPLAY_LEVEL_2);
                    if (val != EnvelopeDisplay.NO_HIGHLIGHT)
                        {
                        model.set("source" + source +  "hcenvlevel2s" + (val + 1) , model.get(key));
                        envelopeDisplays[source-1][DISPLAY_LEVEL_2].repaint();
                        }
                    }
                }
            };
        model.setStatus("source" + source + "hcenvlevel2value", Model.STATUS_RESTRICTED);
        hbox.add(comp);
                

        comp = new LabelledDial("Rate 3", this, "source" + source + "hcenvrate3value", color, 0, 127)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                if (!envelopeSetting)
                    {
                    int val = getHighlightIndex(source, DISPLAY_RATE_3);
                    if (val != EnvelopeDisplay.NO_HIGHLIGHT)
                        {
                        model.set("source" + source +  "hcenvrate3s" + (val + 1) + "", model.get(key));
                        envelopeDisplays[source-1][DISPLAY_RATE_3].repaint();
                        }
                    }
                }
            };
        model.setStatus("source" + source + "hcenvrate3value", Model.STATUS_RESTRICTED);
        hbox.add(comp);
                
        comp = new LabelledDial("Level 3", this, "source" + source + "hcenvlevel3value", color, 0, 63)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                if (!envelopeSetting)
                    {
                    int val = getHighlightIndex(source, DISPLAY_LEVEL_3);
                    if (val != EnvelopeDisplay.NO_HIGHLIGHT)
                        {
                        model.set("source" + source +  "hcenvlevel3s" + (val + 1) + "", model.get(key));
                        envelopeDisplays[source-1][DISPLAY_LEVEL_3].repaint();
                        }
                    }
                }
            };
        model.setStatus("source" + source + "hcenvlevel3value", Model.STATUS_RESTRICTED);
        hbox.add(comp);
           
        vbox = new VBox();     
        EnvelopeDisplay disp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "source" + source + "hcenv" + "rate0value", "source" + source + "hcenv" + "rate1value",  "source" + source + "hcenv" + "rate2value",  "source" + source + "hcenv" + "rate3value" },
            new String[] { null, "source" + source + "hcenv" + "level0value", "source" + source + "hcenv" + "level1value",  "source" + source + "hcenv" + "level2value",  "source" + source + "hcenv" + "level3value" },
            new double[] { 0, 1.0 / 4 / 127.0, 1.0 / 4 / 127.0, 1.0 / 4 / 127.0, 1.0 / 4 / 127.0 },
            new double[] { 0, 1.0 / 63, 1.0 / 63, 1.0 / 63, 1.0 / 63 })
            {
            public int verticalBorderThickness() { return 4; }

            public double preprocessXKey(int index, String key, double value)
                {
                return 127.0 - value;           // rates are flipped from other envelopes, grrr
                }
            };
        disp.setPreferredHeight(70);
        vbox.add(disp);
        hbox.addLast(vbox);
                
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    /** Build the window.  This is our opportunity to turn off menu options and add new menus */    
    public JFrame sprout()     
        {
        JFrame frame = super.sprout();
        //transmitCurrent.setEnabled(true);
        receiveCurrent.setEnabled(false);                       // The K5000 can't do this
        //morphMenu.setEnabled(false);                                    // The K5000 can't do this
        //hillClimbMenu.setEnabled(false);                       // The K5000 can't do this
        addKawaiK5000Menu();
        return frame;
        }
    
    // Constraint strings for the Constrain Harmonics menu
    public static final String[] MENU_CONSTRAINTS = { "All", "Odd", "Even", "First Third", "Second Third", "Third Third", 
        "Octaves", "Fifths", "Major Thirds", "Minor Sevenths", "Major Seconds", "Major Sevenths" };
    
    // The constraints chosen by the Constrain Harmonics menu.  Used by _constrainTo(...)
    int constraints;

    /** Add the Kawai K5000 Menu and submenus */
    public void addKawaiK5000Menu()
        {
        JMenu menu = new JMenu("K5000");
        menubar.add(menu);

        JMenu loadWaveMenu = new JMenu("Load WAV file into...");
        menu.add(loadWaveMenu);
        
        for(int i = 1; i <= 6; i++)                     // note <=
            {
            final int _i = i;
            JMenuItem harmonicsMenu = new JMenuItem("Source " + _i + " Soft Harmonics");
            harmonicsMenu.addActionListener(new ActionListener()
                {
                public void actionPerformed(ActionEvent e)
                    {
                    loadWaveAsHarmonics(_i, true);
                    }
                });
            loadWaveMenu.add(harmonicsMenu);
            harmonicsMenu = new JMenuItem("Source " + _i + " Loud Harmonics");
            harmonicsMenu.addActionListener(new ActionListener()
                {
                public void actionPerformed(ActionEvent e)
                    {
                    loadWaveAsHarmonics(_i, false);
                    }
                });
            loadWaveMenu.add(harmonicsMenu);
            if (i != 6) loadWaveMenu.addSeparator();
            }
    

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
        
        JCheckBoxMenuItem unlock = new JCheckBoxMenuItem("Allow Harmonics Envelope Display Mouse-Over");
        allowUpdateFromMouse = getLastXAsBoolean("Mouseover", getSynthName(), true, true);
        unlock.setSelected(allowUpdateFromMouse);
        unlock.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                allowUpdateFromMouse = unlock.isSelected();
                setLastX("" + allowUpdateFromMouse, "Mouseover", getSynthName(), true);
                }
            });
        menu.add(unlock);

        JMenu clearBankMenu = new JMenu("Clear Bank on Synthesizer...");
        menu.add(clearBankMenu);
        for(int i = 0; i < 4; i++)
            {
            final int _i = i;
            JMenuItem bankMenu = new JMenuItem("Bank " + BANKS[i]);
            bankMenu.addActionListener(new ActionListener()
                {
                public void actionPerformed(ActionEvent e)
                    {
                    writeEmptyK5000Bank(_i);
                    }
                });
            clearBankMenu.add(bankMenu);
            }
    
        menu.addSeparator();
    
        JMenuItem resetMenuItem = new JMenuItem("Reset (Reload from Flash)");
        resetMenuItem.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                sendK5000Reset();
                }
            });
        menu.add(resetMenuItem);

        JMenuItem backupMenuItem = new JMenuItem("Backup (Write to Flash)");
        backupMenuItem.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                sendK5000Backup();
                }
            });
        menu.add(backupMenuItem);
        menu.addSeparator();

        JCheckBoxMenuItem backupAfterWriteToPatchItem = new JCheckBoxMenuItem("Perform Backup and/or Reset after Writes and Requests");
        backupAfterWriteToPatch = getLastXAsBoolean("BackupAfterWriteToPatch", getSynthName(), true, true);
        backupAfterWriteToPatchItem.setSelected(backupAfterWriteToPatch);
        transmitCurrent.setEnabled(backupAfterWriteToPatch);
        backupAfterWriteToPatchItem.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                backupAfterWriteToPatch = backupAfterWriteToPatchItem.isSelected();
                transmitCurrent.setEnabled(backupAfterWriteToPatch);
                if (backupAfterWriteToPatch)
                    {
                    showSimpleMessage("Perform Backup and/or Reset after Writes and Requests", "Be sure you have read the ABOUT panel to learn about how\nthe Kawai K5000 memory works." );
                    }
                setLastX("" + backupAfterWriteToPatch, "BackupAfterWriteToPatch", getSynthName(), true);
                }
            });
        menu.add(backupAfterWriteToPatchItem);

/*
  JCheckBoxMenuItem sendParametersItem = new JCheckBoxMenuItem("Only Update Synth on Send to Current Patch");
  sendsParametersOnlyOnSendCurrentPatch = getLastXAsBoolean("SendParametersOnlyOnSendCurrentPatch", getSynthName(), false, true);
  sendParametersItem.setSelected(sendsParametersOnlyOnSendCurrentPatch);
  sendParametersItem.addActionListener(new ActionListener()
  {
  public void actionPerformed(ActionEvent e)
  {
  sendsParametersOnlyOnSendCurrentPatch = sendParametersItem.isSelected();
  setLastX("" + sendsParametersOnlyOnSendCurrentPatch, "SendParametersOnlyOnSendCurrentPatch", getSynthName(), true);
  }
  });
  menu.add(sendParametersItem);

  JCheckBoxMenuItem backupAfterLibrarianWriteItem = new JCheckBoxMenuItem("Always Backup After Librarian Write");
  backupAfterLibrarianWrite = getLastXAsBoolean("BackupAfterLibrarianWrite", getSynthName(), false, true);
  backupAfterLibrarianWriteItem.setSelected(backupAfterLibrarianWrite);
  backupAfterLibrarianWriteItem.addActionListener(new ActionListener()
  {
  public void actionPerformed(ActionEvent e)
  {
  backupAfterLibrarianWrite = backupAfterLibrarianWriteItem.isSelected();
  if (backupAfterLibrarianWrite)
  {
  showSimpleMessage("Backup After Write to Patch", "Be sure you have read the ABOUT panel to learn about how\nthe Kawai K5000 memory works." );
  }
  setLastX("" + backupAfterLibrarianWrite, "BackupAfterLibrarianWrite", getSynthName(), true);
  }
  });
  menu.add(backupAfterLibrarianWriteItem);
*/

        /*
          JCheckBoxMenuItem changePatchAfterSendItem = new JCheckBoxMenuItem("Change Patch after Send to Current Patch");
          changePatchAfterSend = getLastXAsBoolean("ChangePatchAfterSend", getSynthName(), true, true);
          changePatchAfterSendItem.setSelected(changePatchAfterSend);
          changePatchAfterSendItem.addActionListener(new ActionListener()
          {
          public void actionPerformed(ActionEvent e)
          {
          changePatchAfterSend = changePatchAfterSendItem.isSelected();
          setLastX("" + changePatchAfterSend, "changePatchAfterSend", getSynthName(), true);
          }
          });
          menu.add(changePatchAfterSendItem);
        */
        }
        
    public void beforeWriteAllParametersHook() 
        { 
        if (backupAfterWriteToPatch)
            {
            performK5000Reset();
            }
        }

    public void afterWriteAllParametersHook()
        {
        if (backupAfterWriteToPatch)
            {
            performK5000Backup();
            }
        }
        
    public void afterLibrarianWriteHook()
        {
        if (backupAfterWriteToPatch)
//      if (backupAfterLibrarianWrite)
            {
            performK5000Backup();
            }
        }

    public void beforeLibrarianWriteHook()
        {
        if (backupAfterWriteToPatch)
//      if (backupAfterLibrarianWrite)
            {
            performK5000Reset();
            }
        }

    // Given the current constraints, returns whether the given index should be constrained 
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

    /** Returns the patch name in the model */
    public String getPatchName(Model model) { return model.get("name", EMPTY_PATCH_NAME); }

    // Sysex constant for switching to single mode
    public static final int SINGLE_MODE = 0x01;
    // Sysex constant for switching to various banks
    public static final int BANK_A_MSB = 0x64;
    public static final int BANK_D_MSB = 0x66;
    public static final int BANK_E_MSB = 0x67;
    public static final int BANK_F_MSB = 0x68;
    public static final int BANK_M_MSB = 0x65;                // only has 64 PC values

    public void sendK5000Reset()
        {
        if (showSimpleConfirm("Reset the K5000?", "Reload all current working memory from the stored Flash RAM?\nThis includes all Multi and Single patches.\nThis operation cannot be undone."))
            {
            performK5000Reset();
            }
        }
        
    void performK5000Reset()
        {
        try 
            {
            tryToSendSysex(new byte[] { (byte)0xF0, 0x40, (byte)getChannelOut(), 0x32, 0x00, 0x0a, 0x02, (byte)0xF7 });
            }
        catch (Exception e) { Synth.handleException(e); }
        }
        
    void performK5000Backup()
        {
        try 
            {
            tryToSendSysex(new byte[] { (byte)0xF0, 0x40, (byte)getChannelOut(), 0x32, 0x00, 0x0a, 0x01, (byte)0xF7 });
            }
        catch (Exception e) { Synth.handleException(e); }
        }
        
    public void sendK5000Backup()
        {
        if (showSimpleConfirm("Backup the K5000?", "Save all current working memory to the stored Flash RAM?\nThis includes all Multi and Single patches.\nThis operation cannot be undone."))
            {
            performK5000Backup();
            }
        }

    // We override this method to deselect the write all patches and save all patches menus.
    public void librarianCreated(Librarian librarian) 
        { 
        writeAllPatchesMenu.setSelected(false); 
        saveAllPatchesMenu.setSelected(false); 
        }


    /** Changes the patch to the bank and number provided */
    public void changePatch(Model tempModel)
        {
        byte BB = (byte)tempModel.get("bank");
        byte NN = (byte)tempModel.get("number");
        int away = NN + 1;
        if (away > 127) away = 0;
                        
        int bankMSB = (BB == 0 ? BANK_A_MSB : (BB == 1 ? BANK_D_MSB : (BB == 2 ? BANK_E_MSB : BANK_F_MSB)));
        int bankLSB = 0;
        
        try 
            {
            // Change to Single Mode.  See p. 35  NOTE there is no "Change to Multi / Combo Mode" I think, ugh.
            // FIXME: I don't know if this is necessary or even if it works
            //tryToSendSysex(new byte[] { (byte)(0xF0), 0x40, (byte)getChannelOut(), 0x31, 0x00, 0x0a, SINGLE_MODE, (byte)0xF7 });
                
            // Bank Change
            tryToSendMIDI(new ShortMessage(ShortMessage.CONTROL_CHANGE, getChannelOut(), 0, bankMSB));
            tryToSendMIDI(new ShortMessage(ShortMessage.CONTROL_CHANGE, getChannelOut(), 32, bankLSB));
                
            // PC Away, so we can always see the name change
            tryToSendMIDI(new ShortMessage(ShortMessage.PROGRAM_CHANGE, getChannelOut(), away, 0));
            // PC
            tryToSendMIDI(new ShortMessage(ShortMessage.PROGRAM_CHANGE, getChannelOut(), NN, 0));
            }
        catch (Exception e) { Synth.handleException(e); }
        }

    
    /** Provides the bank and number of the NEXT patch, given the current one provided. */
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

    /** Provides a String version of the patch location provided. */
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

        String nm = model.get("name", EMPTY_PATCH_NAME);
        String newnm = revisePatchName(nm);
        if (!nm.equals(newnm))
            model.set("name", newnm);
        }
    
    // Custom-repair out-of-range parameter values
    public void fix(Model model, String key, int val, int min, int max)
        {
        // Bugs in Kawai's patches result in 0 provided when it should be 33 for signed parameters (like -31 ... +31,
        // where 0 == 33).  For now we will assume that it meant 33
        if ((key.startsWith("effectcontrol") || key.startsWith("macrocontroller")) && 
            (key.contains("depth")) && val == 0)            // effectcontrol1depth, macrocontroller4depth2, etc.
            {
            int newVal = 64;
            model.set(key, newVal);
            if (getPrintRevised()) System.out.println("Warning (Synth): Revised " + key + " from " + val + " to " + newVal + " (range " + min + " ... " + max + ")");             
            }
        /*
          else if ((key.startsWith("effect1") || key.startsWith("effect2") || key.startsWith("effect3") || key.startsWith("effect4")) &&
          !(key.endsWith("depth")))
          {
          // It's effectXtypeYparaZ
          //int effect = StringUtility.getFirstInt(key);
          key = StringUtility.removePreambleAndFirstDigits(key, "effect");
          int type = StringUtility.getFirstInt(key);
          key = StringUtility.removePreambleAndFirstDigits(key, "type");
          int param = StringUtility.getFirstInt(key);
          if (EFFECT_PARAMETER_NAMES[param][type - 1].equals("Depth"))
          {
          if (val < 0) val = 1;
          if (val > 100) val = 100;
          }
          }
        */
        else            // we also have one patch with source1formantlfodepth = 85, ugh
            {
            // bound
            int newVal = val;
            if (newVal < min) newVal = min;
            if (newVal > max) newVal = max;
            model.set(key, newVal);
            if (getPrintRevised()) System.out.println("Warning (Synth): Revised " + key + " from " + val + " to " + newVal + " (range " + min + " ... " + max + ")");             
            }
        // else super.fix(model, key, val, min, max);
        }

    /** Returns the synthesizer name */
    public static String getSynthName() { return "Kawai K5000S/K5000R"; }
    
    // The K5000 can't send to temporary memory.  Presently we are only sending as individual parameters.
    //public boolean getSendsAllParametersAsDump() { return false; }

    // The only way to send all parameters is via Send To Current Patch -- all others will fail
    public boolean getSendsParametersOnlyOnSendCurrentPatch() { return !backupAfterWriteToPatch; }

    /** Returns the patch name as a byte array, but returned as an int[] */
    public int[] getNameAsBytes(Model model)
        {
        String name = model.get("name", "        ") + "        ";
        int[] bytes = new int[8];
        for(int i = 0; i < 8; i ++)
            {
            bytes[i] = name.charAt(i);
            }
        return bytes;
        }

    /** Encodes the given key at the given position in the data */
    int p(byte[] data, int pos, String key, Model model)
        {
        data[pos] = (byte)model.get(key);
        return pos + 1;
        }
        
    // Given a model (which will never be null), writes out the full Single Tone of a patch, including all sources and wavekits, and all checksums,
    // starting at the given position in data
    public int emitTone(Model model, byte[] data, int pos, int sources )
        {
        pos++;                      // skip checksum space
        int start = pos;

        // LOAD EFFECTS
    
        pos = p(data, pos, "algorithm", model);
        int reverbtype = model.get("reverbtype");
        data[pos++] = (byte)reverbtype;
        pos = p(data, pos, "reverb" + (reverbtype + 1) + "drywet1", model);
        pos = p(data, pos, "reverb" + (reverbtype + 1) + "para1", model);
        pos = p(data, pos, "reverb" + (reverbtype + 1) + "para2", model);
        pos = p(data, pos, "reverb" + (reverbtype + 1) + "para3", model);
        pos = p(data, pos, "reverb" + (reverbtype + 1) + "para4", model);


        for(int effect = 1; effect <= 4; effect++)                                  // NOTE <=
            {
            int missingEffectParams = 0;
            int effectType = model.get("effect" + effect + "type");
            data[pos++] = (byte)(effectType + 11);                                                      // effects start at 11
            if (EFFECT_PARAMETER_MINS[0][effectType] == NONE)
                { missingEffectParams++; }
            else { pos = p(data, pos, "effect" + effect + "type" + (effectType + 1) + "depth", model); }
            if (EFFECT_PARAMETER_MINS[1][effectType] == NONE)
                {  missingEffectParams++; }
            else { pos = p(data, pos, "effect" + effect + "type" + (effectType + 1) + "para1", model); }
            if (EFFECT_PARAMETER_MINS[2][effectType] == NONE)
                { missingEffectParams++; }
            else { pos = p(data, pos, "effect" + effect + "type" + (effectType + 1) + "para2", model); }
            if (EFFECT_PARAMETER_MINS[3][effectType] == NONE)
                { missingEffectParams++; }
            else { pos = p(data, pos, "effect" + effect + "type" + (effectType + 1) + "para3", model); }
            if (EFFECT_PARAMETER_MINS[4][effectType] == NONE)
                { missingEffectParams++; }
            else { pos = p(data, pos, "effect" + effect + "type" + (effectType + 1) + "para4", model); }
            pos += missingEffectParams;
            }
                
        for(int freq = 1; freq <= 7; freq++)                                            // NOTE <=
            {
            pos = p(data, pos, "geqfreq" + freq, model);
            }
                
        // LOAD COMMON

        data[pos++] = 0;                                                        // the "drum mark", see page 13 of sysex spec, line 39
        
        int[] name = getNameAsBytes(model);
        for(int i = 0; i < 8; i++)
            {
            data[pos++] = (byte)name[i];
            }
        pos = p(data, pos, "volume", model);
        pos = p(data, pos, "poly", model);
        data[pos++] = 0;                                                                // "no use", see page 14 of sysex spec, line 50
        
        data[pos++] = (byte)sources;

        int srcmute = (byte)((1 - (model.get("srcmute1")) << 0) |
            (1 - (model.get("srcmute2")) << 1) |
            (1 - (model.get("srcmute3")) << 2) |
            (1 - (model.get("srcmute4")) << 3) |
            (1 - (model.get("srcmute5")) << 4) |
            (1 - (model.get("srcmute6")) << 5));
        data[pos++] = (byte)srcmute;                                                    // bitpacking

        pos = p(data, pos, "am", model);
        pos = p(data, pos, "effectcontrol1source", model);
        pos = p(data, pos, "effectcontrol1destination", model);
        pos = p(data, pos, "effectcontrol1depth", model);
        pos = p(data, pos, "effectcontrol2source", model);
        pos = p(data, pos, "effectcontrol2destination", model);
        pos = p(data, pos, "effectcontrol2depth", model);
        pos = p(data, pos, "portamento", model);
        pos = p(data, pos, "portamentospeed", model);
        for(int c = 1; c <= 4; c++)
            {
            pos = p(data, pos, "macrocontroller" + c + "parameter1", model);
            pos = p(data, pos, "macrocontroller" + c + "parameter2", model);
            }
        for(int c = 1; c <= 4; c++)
            {
            pos = p(data, pos, "macrocontroller" + c + "depth1", model);
            pos = p(data, pos, "macrocontroller" + c + "depth2", model);
            }
                
        pos = p(data, pos, "sw1parameter", model);
        pos = p(data, pos, "sw2parameter", model);
        pos = p(data, pos, "fsw1parameter", model);
        pos = p(data, pos, "fsw2parameter", model);

        // LOAD SOURCE DATA
    
        for(int i = 1; i <= sources; i++)                                           // note <=
            {
            for(int j = 0; j < sourceParams.length; j++)
                {
                String key = sourceParams[j];
                                
                if (j == 2)             // velo sw and velo
                    {
                    data[pos++] = (byte)((model.get("source" + i + "generalvelosw") << 5) | 
                        (model.get("source" + i + "generalvelo")));
                    }
                else if (j == 28) // wavekitmsb
                    {
                    int wavekit = model.get("source" + i + "dcowavekit") + PCM_START;
                    boolean additive = (model.get("source" + i + "dcoadditive") == 1);
                    data[pos++] = (byte)(additive ? 4 : (wavekit >>> 7));                           // additive is 512
                    }
                else if (j == 29) // wavekitlsb
                    {
                    int wavekit = model.get("source" + i + "dcowavekit") + PCM_START;
                    boolean additive = (model.get("source" + i + "dcoadditive") == 1);
                    data[pos++] = (byte)(additive ? 0 : (wavekit & 127));                           // additive is 512
                    }
                else
                    {
                    pos = p(data, pos, "source" + i + key, model);
                    }
                }
            }

        // COMMON AND SOURCE DATA CHECKSUM
    
        data[start - 1] = checksum(data, start, pos);
    

        // LOAD WAVE KIT DATA
            
        for(int i = 1; i <= sources; i++)                                           // note <=
            {
            if (model.get("source" + i + "dcoadditive") == 0) continue;
            
            int checksumpos = pos;
            pos++;
            
            start = pos;
        
            for(int j = 1; j < addWaveKitParams.length; j++)                // skip "--", checksum
                {
                String key = addWaveKitParams[j];
                pos = p(data, pos, "source" + i + key, model);
                }

            for(int j = 0; j < addWaveSoftHCParams.length; j++)
                {
                String key = addWaveSoftHCParams[j];
                pos = p(data, pos, "source" + i + key, model);
                }

            for(int j = 0; j < addWaveLoudHCParams.length; j++)
                {
                String key = addWaveLoudHCParams[j];
                pos = p(data, pos, "source" + i + key, model);
                }

            for(int j = 0; j < addWaveFormantFilterParams.length; j++)
                {
                String key = addWaveFormantFilterParams[j];
                pos = p(data, pos, "source" + i + key, model);
                }

            for(int j = 0; j < addWaveHarmonicEnvelopeParams.length; j++)
                {
                String key = addWaveHarmonicEnvelopeParams[j];
                if (key.contains("level1"))
                    {
                    ///// NOTE: documentation is wrong. It says that val1 & 64 = 0 is "LP1" and val1 & 64 = 64 is "Loop/LP2".
                    ///// It's actually the other way around.
                    
                    ////                                        CORRECT                                 DOCUMENTATION
                    ////        TYPE            LEVEL 1         LEVEL 2         LEVEL 1         LEVEL 2
                    ////        (0) Off         0                       0                       64                      0       
                    ////        (1) LP1         64                      64                      0                       64
                    ////        (2) LP2         0                       64                      64                      64
                    ////        (3) ---?        64                      0                       0                       0                       We assume this is "Off"
                    
                    int num = StringUtility.getSecondInt(key);
                    data[pos++] = (byte)(model.get("source" + i + key) |
                        (model.get("source" + i + "hcenv" + "loop" + "s" + num) == 1 ? 64 : 0)); // lp1
                    }
                else if (key.contains("level2"))
                    {
                    int num = StringUtility.getSecondInt(key);
                    data[pos++] = (byte)(model.get("source" + i + key) |
                        (model.get("source" + i + "hcenv" + "loop" + "s" + num) == 0 ? 0 : 64)); // off
                    }
                else if (key.equals("--"))
                    {
                    pos++;
                    }
                else
                    {
                    pos = p(data, pos, "source" + i + key, model);
                    }
                }

            data[checksumpos] = checksum(data, start, pos);
            }
        return pos;
        }

    /** Emit a patch */
    public Object[] emitAll(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
//        if (toWorkingMemory) return new Object[0];                      // For the time being
        
        if (tempModel == null)
            tempModel = getModel();
    
        int bank = tempModel.get("bank");
        // bank     Name    data[8]
        // 0        A               0
        // 1        D               2
        // 2        E               3
        // 3        F               4
    
        int sources = model.get("srctype");

        // compute data length
        int datalen = 10 + 81 + 1;
        for(int i = 0; i < sources; i++)
            {
            datalen += 86;
            if (model.get("source" + (i + 1) + "dcoadditive") == 1) 
                {
                datalen += 806;
                }
            }
    
        byte[] data = new byte[datalen];
        // Format is
        // F0 40 CHANNEL 20 00 0a 00 BANK PATCHNUM SINGLE_CHECKSUM EFFECT/COMMON SOURCE* WAVEKIT* F7
        // The only wavekits provided are those for which the source is NOT additive.
    
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

        pos = emitTone(model, data, pos, sources);

        data[data.length - 1] = (byte)0xF7;
        if (toWorkingMemory)    // && changePatchAfterSend)
            {
            // On the K5000, Send to Current Patch is identical to Write Current Patch, but we
            // have to do a write to the patch in question because ALL patches are in temporary memory,
            // and there's no way to write to temporary memory, grrr.
            //
            // So we write to bank/number, or to A001 if there is none.
            // Then we have to do a change patch to bank/number to display the result
            byte BB = (byte)tempModel.get("bank");
            byte NN = (byte)tempModel.get("number");
            if (BB < 0 || BB > 3) BB = 0;
            if (NN < 0 || NN > 127) NN = 0;
            int away = NN + 1;
            if (away > 127) away = 0;
                                                
            int bankMSB = (BB == 0 ? BANK_A_MSB : (BB == 1 ? BANK_D_MSB : (BB == 2 ? BANK_E_MSB : BANK_F_MSB)));
            int bankLSB = 0;
                
            try
                {
                return new Object[]
                    {
                    // PC away, so we can see the name change
                    new ShortMessage(ShortMessage.PROGRAM_CHANGE, getChannelOut(), away, 0),
                    data,
                    // PC Back to our patch
                    new ShortMessage(ShortMessage.CONTROL_CHANGE, getChannelOut(), 0, bankMSB),
                    new ShortMessage(ShortMessage.CONTROL_CHANGE, getChannelOut(), 32, bankLSB),
                    new ShortMessage(ShortMessage.PROGRAM_CHANGE, getChannelOut(), NN, 0),
                    };
                }
            catch (InvalidMidiDataException ex)
                {
                return new Object[] { data };                   // I guess...
                }
            }
        else                    // writing
            {
            return new Object[] { data };
            }
        }

    // We want empty patches to be marked as null, not as INIT banks, so we can save them out properly
    // in emitBank
    public boolean markEmptyBankPatchModelsAsNull() { return true; }

    public boolean getSupportsBankSaves()
        {
        return true;            // we can save a bank but not write it
        }

    public void writeEmptyK5000Bank(int bank)
        {
        if (showSimpleConfirm("Clear Bank?", "Erase bank " + BANKS[bank] + " on the synthesizer?\nThis operation cannot be undone."))
            {
            writeBankAsRangePreamble(bank);
            }
        }

    public void writeBankAsRangePreamble(int bank)
        {
        if (bank > 0) bank++;               // A = 0, D = 2, E = 3, F = 4

        try 
            {
            byte[] preamble = new byte[]
                {
                (byte)0xF0,
                0x40,
                (byte)getChannelOut(),
                0x21,
                0x00,
                0x0A,
                0x00,
                (byte)bank,
                // Empty tone except first patch.  Will this work?
                0x01, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00,

                // First patch -- this is a small one.  We can't have ZERO patches apparently...
                0x06, 0x01, 0x0A, 0x3C, 0x3C,
                0x63, 0x1E, 0x63, 0x27, 0x55, 0x14, 0x64, 0x04, 0x01, 0x12, 0x64, 0x46, 0x3B, 0x00,
                0x00, 0x0F, 0x64, 0x00, 0x32, 0x31, 0x00, 0x0B, 0x46, 0x63, 0x05, 0x09, 0x00, 0x40,
                0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x00, 0x2D, 0x2D, 0x2D, 0x2D, 0x2D, 0x2D, 0x2D,
                0x2D, 0x7F, 0x00, 0x00, 0x02, 0x00, 0x00, 0x02, 0x00, 0x40, 0x02, 0x00, 0x40, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x40, 0x40, 0x40, 0x40, 0x40,
                0x40, 0x40, 0x40, 0x00, 0x00, 0x00, 0x00, 0x00, 0x7F, 0x10, 0x00, 0x7E, 0x02, 0x00,
                0x00, 0x40, 0x00, 0x40, 0x03, 0x59, 0x00, 0x40, 0x02, 0x5F, 0x00, 0x40, 0x00, 0x00,
                0x40, 0x00, 0x00, 0x40, 0x00, 0x01, 0x40, 0x03, 0x0F, 0x40, 0x40, 0x00, 0x00, 0x40,
                0x04, 0x40, 0x40, 0x40, 0x40, 0x00, 0x00, 0x05, 0x00, 0x00, 0x2F, 0x40, 0x40, 0x71,
                0x00, 0x3E, 0x60, 0x65, 0x40, 0x65, 0x40, 0x40, 0x78, 0x40, 0x40, 0x01, 0x00, 0x5F,
                0x72, 0x6E, 0x00, 0x2C, 0x40, 0x40, 0x40, 0x40, 0x37, 0x40, 0x40, 0x40, 0x00, 0x5B,
                0x01, 0x0E, 0x0A, 0x00, 0x40, 0x00, 0x40, 0x00, 0x40, 0x00, 0x7F, 0x10, 0x00, 0x79,
                0x02, 0x00, 0x00, 0x40, 0x00, 0x40, 0x03, 0x59, 0x00, 0x40, 0x02, 0x5F, 0x00, 0x40,
                0x00, 0x00, 0x40, 0x00, 0x00, 0x40, 0x00, 0x01, 0x40, 0x03, 0x0F, 0x40, 0x40, 0x00,
                0x00, 0x40, 0x04, 0x40, 0x40, 0x40, 0x40, 0x00, 0x00, 0x05, 0x00, 0x00, 0x31, 0x40,
                0x40, 0x71, 0x00, 0x3E, 0x60, 0x65, 0x40, 0x65, 0x40, 0x40, 0x78, 0x40, 0x40, 0x01,
                0x00, 0x5B, 0x72, 0x73, 0x41, 0x34, 0x40, 0x40, 0x40, 0x40, 0x37, 0x40, 0x40, 0x40,
                0x00, 0x5B, 0x01, 0x0E, 0x0A, 0x00, 0x40, 0x00, 0x40, 0x00, 0x40,
                (byte)0xF7
                };
            tryToSendSysex(preamble);
            }
        catch (Exception e) { Synth.handleException(e); }
        }
                

    public Object[] emitBank(Model[] models, int bank, boolean toFile) 
        {
        boolean[] tonemap = new boolean[128];
        
        int datalen = 8 + 19 + 1;
        for(int i = 0; i < 128; i++)
            {
            // build tone map
            Model m = models[i];
                
            tonemap[i] = (m != null);
            if (!tonemap[i]) continue;              // we don't have this data, it's a null patch

            // compute data length
            datalen += 81 + 1;              // add in the checksum
            int sources = m.get("srctype");
            for(int s = 0; s < sources; s++)
                {
                datalen += 86;
                if (m.get("source" + (s + 1) + "dcoadditive") == 1) 
                    {
                    datalen += 806;
                    }
                }
            }

        // bank     Name    data[8]
        // 0        A               0
        // 1        D               2
        // 2        E               3
        // 3        F               4
        
        byte[] data = new byte[datalen];
        
        data[0] = (byte)0xF0;
        data[1] = (byte)0x40;                                               // Kawai
        data[2] = (byte)getChannelOut();                    // Channel
        data[3] = (byte)0x21;
        data[4] = (byte)0x00;
        data[5] = (byte)0x0A;
        data[6] = (byte)0x00;
        data[7] = (byte)BANK_VALS[bank];                    // bank
        int pos = 8;
                        
        // Load the tone map
        int patch = 0;
        for(int j = 0; j < 18; j++)
            {
            data[pos++] = (byte)
                ((tonemap[patch + 0] ? 1 : 0) +
                (tonemap[patch + 1] ? 2 : 0) +
                (tonemap[patch + 2] ? 4 : 0) +
                (tonemap[patch + 3] ? 8 : 0) +
                (tonemap[patch + 4] ? 16 : 0) +
                (tonemap[patch + 5] ? 32 : 0) +
                (tonemap[patch + 6] ? 64 : 0));
            patch += 7;
            }
        data[pos++] = (byte)
            ((tonemap[126] ? 1 : 0) +
            (tonemap[127] ? 2 : 0));

            
                                        
        // Load the patches

        for(Model m : models)
            {
            if (m != null)
                {
                pos = emitTone(m, data, pos, m.get("srctype"));
                }
            }
        data[data.length - 1] = (byte)0xF7;
        return new Object[] { data };
        }


    /** Parse a patch from data */
    public int parse(byte[] result, boolean fromFile)
        {
        if (result[3] == 0x20)  // single
            {
            int bank = result[7];
            if (bank == 1)  // Bank B, we don't do that.  
                return PARSE_FAILED;
            if (bank > 0) bank--;                        // bank = 1 is invalid.  Shift all others down one.
            // bank     Name    result[8]
            // 0        A               0
            // 1        D               2
            // 2        E               3
            // 3        F               4

            model.set("bank", bank);
            model.set("number", result[8]);
                                
            int pos = 10;
                                                
            // okay, here we go
            pos = parseSingle(result, pos);
            revise();
            if (pos < 0) return PARSE_FAILED;
            else return PARSE_SUCCEEDED;
            }
        else    //      bank sysex
            {
            preprocessParseFromBank(result);

            // Now that we have an array of names, one per patch, we present the user with options;
            // 0. Cancel [handled automatically]
            // 1. Save the bank data [handled automatically]
            // 2. Upload the bank data [handled automatically] 
            // 3. Load and edit a certain patch number
            int patchNum = showBankSysexOptions(result, patchBankNames);
            if (patchNum < 0) return PARSE_CANCELLED;
            
            int bank = result[7];
            if (bank > 0) bank--;               // A = 0, D = 1, E = 2, F = 3
            model.set("bank", bank);
            model.set("number", patchNum);

            return parseFromBank(result, patchNum);
            }

        
        }
    
    /** Parse a Single Tone section */
    // return -1 if FAILED, else return resulting pos
    // This starts BEYOND the checksum
    public int parseSingle(byte[] result, int pos)
        {             
        int numSources = result[pos + 50];
        if (numSources < 2 || numSources > 6) return -1;              

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
            int missingEffectParams = 0;

            // EFFECTS are PACKED.  So if an effect doesn't have a particular parameter, the NEXT PARAMETER fits in its slot.  
            // This is of course NOT documented

            int effectType = result[pos++] - 11;                                        // effects go 11...47
            if (effectType < 0 || effectType >= EFFECT_PARAMETER_MINS[0].length) effectType = 0;                        // this happens for Patch 71, which has a value of 0 for one of its effect types 
            
            model.set("effect" + effect + "type", effectType);
            if (EFFECT_PARAMETER_MINS[0][effectType] == NONE)
                { missingEffectParams++; }
            else { model.set("effect" + effect + "type" + (effectType + 1) + "depth", result[pos++]); }
            if (EFFECT_PARAMETER_MINS[1][effectType] == NONE)
                {  missingEffectParams++; }
            else { model.set("effect" + effect + "type" + (effectType + 1) + "para1", result[pos++]); }
            if (EFFECT_PARAMETER_MINS[2][effectType] == NONE)
                { missingEffectParams++; }
            else { model.set("effect" + effect + "type" + (effectType + 1) + "para2", result[pos++]); }
            if (EFFECT_PARAMETER_MINS[3][effectType] == NONE)
                { missingEffectParams++; }
            else { model.set("effect" + effect + "type" + (effectType + 1) + "para3", result[pos++]); }
            if (EFFECT_PARAMETER_MINS[4][effectType] == NONE)
                { missingEffectParams++; }
            else { model.set("effect" + effect + "type" + (effectType + 1) + "para4", result[pos++]); }
            pos += missingEffectParams;                                 // fill in the missing spaces
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
                    model.set("source" + i + "generalvelosw", (val >>> 5) & 3);
                    model.set("source" + i + "generalvelo", val & 31);
                    }
                else if (j == 28) // wavekitmsb
                    {
                    int msb = result[pos++];
                    int lsb = result[pos++];
                    int val = (msb << 7) | lsb;
                    if (val == 512)
                        {
                        model.set("source" + i + "dcoadditive", 1);
                        model.set("source" + i + "dcowavekit", 0);
                        additiveSource[i - 1] = true;
                        }
                    else
                        {
                        model.set("source" + i + "dcoadditive", 0);
                        model.set("source" + i + "dcowavekit", val - PCM_START);
                        }
                    }
                else if (j == 29) // wavekitlsb
                    {
                    // do nothing
                    }
                else if (!key.equals("--"))
                    {
                    model.set("source" + i + key, result[pos++]);
                    }
                }
            }
    
        // COMMON AND SOURCE DATA CHECKSUM
        
        // skip

        // LOAD WAVE KIT DATA
            
        for(int i = 1; i <= srctype; i++)                                           // note <=
            {
            if (!additiveSource[i - 1]) continue;
            
            if ((result[pos] & 0xFF) == 0xF7)   // we ran out!!!!
                {
                System.err.println("ERROR (KawaiK5000.parse()): early truncation of wavekits!");
                return PARSE_FAILED;            // I think this should be a failure
                }
            
            pos++;     
            int start = pos;                                                                                         // checksum
        
            for(int j = 1; j < addWaveKitParams.length; j++)                // skip "--", checksum
                {
                String key = addWaveKitParams[j];
                if (!key.equals("--"))
                    model.set("source" + i + key, result[pos++]);
                }

            for(int j = 0; j < addWaveSoftHCParams.length; j++)
                {
                String key = addWaveSoftHCParams[j];
                if (!key.equals("--"))
                    model.set("source" + i + key, result[pos++]);
                }

            for(int j = 0; j < addWaveLoudHCParams.length; j++)
                {
                String key = addWaveLoudHCParams[j];
                if (!key.equals("--"))
                    model.set("source" + i + key, result[pos++]);
                }

            for(int j = 0; j < addWaveFormantFilterParams.length; j++)
                {
                String key = addWaveFormantFilterParams[j];
                if (!key.equals("--"))
                    model.set("source" + i + key, result[pos++]);
                }

            for(int j = 0; j < addWaveHarmonicEnvelopeParams.length; j++)
                {
                String key = addWaveHarmonicEnvelopeParams[j];
                if (key.contains("level1"))
                    {
                    int num = StringUtility.getSecondInt(key);
                    int val1 = result[pos];                     // level 1
                    int val2 = result[pos + 2];                         // level 2 (skip rate1)
                    
                    ///// NOTE: documentation is wrong. It says that val1 & 64 = 0 is "LP1" and val1 & 64 = 64 is "Loop/LP2".
                    ///// It's actually the other way around.
                    
                    ////                                        CORRECT                                 DOCUMENTATION
                    ////        TYPE            LEVEL 1         LEVEL 2         LEVEL 1         LEVEL 2
                    ////        (0) Off         0                       0                       64                      0       
                    ////        (1) LP1         64                      64                      0                       64
                    ////        (2) LP2         0                       64                      64                      64
                    ////        (3) ---?        64                      0                       0                       0                       We assume this is "Off"
                    
                    pos++;
                    model.set("source" + i + key, val1 & 63);
                    model.set("source" + i + "hcenv" + "loop" + "s" + num, 
                            ((val1 & 64) == 0 ? 
                            ((val2 & 64) == 0 ? 0 : 2) :            // Level 1 = 0, Level 2 = 0 -> OFF,   Level 1 = 0, Level 2 = 64 -> LP2
                            ((val2 & 64) == 0 ? 0 : 1)));           // Level 1 = 64, Level 2 = 0 -> OFF DEFAULT,   Level 1 = 64, Level 2 = 64 -> LP1
                    }
                else if (key.contains("level2"))
                    {
                    int val1 = result[pos++];           // level 2
                    model.set("source" + i + key, val1 & 63);
                    }
                else if (key.equals("--"))
                    {
                    pos++;
                    }
                else
                    {
                    model.set("source" + i + key, result[pos++]);
                    }
                }
            }
        return pos;
        }
        
    // Computes the checksum on data[start] ... data[end - 1]
    public byte checksum(byte[] data, int start, int end)
        {
        // FIXME: the text appears to suggest that it is 0xa5 + sum mod 127, but it is unclear
        int sum = 0xa5;
        for(int i = start; i < end; i++)
            {
            sum += (data[i] & 0xFF);
            }
        return (byte)(sum & 127);
        }


    // Offset for the common (non-effects) parameter data 
    public static final int COMMON_START = 39;

    /** Emit a parameter as MIDI */
    public Object[] emitAll(String key)
        {
        // There's a lot of custom stuff here.  :-(
        
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
            // This is for velo and generalvelosw, which must be bitpacked together
            else if (key.startsWith("source1generalvelo") || key.startsWith("source2generalvelo") || key.startsWith("source3generalvelo") || key.startsWith("source4generalvelo") || key.startsWith("source5generalvelo") || key.startsWith("source6generalvelo"))
                {
                int source = StringUtility.getFirstInt(key);
                data = new int[] { 0x01, 0x01, (source - 1), 0x00, 0x02, 0x00, 
                    (model.get("source" + source + "generalvelosw") << 5) | 
                    model.get("source" + source + "generalvelo") };
                }
            // This must be sent 14-bit
            else if (key.startsWith("source1wavekit") || key.startsWith("source2wavekit") || key.startsWith("source3wavekit") || key.startsWith("source4wavekit") || key.startsWith("source5wavekit") || key.startsWith("source6wavekit") ||
                key.startsWith("source1additive") || key.startsWith("source2additive") || key.startsWith("source3additive") || key.startsWith("source4additive") || key.startsWith("source5additive") || key.startsWith("source6additive"))
                {
                int source = StringUtility.getFirstInt(key);
                boolean additive = (model.get("source" + source + "dcoadditive") == 1);
                int val = model.get("source" + source + "dcowavekit") + PCM_START;
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
                    remainder.equals("hcenvvalue") ||
                    remainder.equals("hcenvlevel0value") || 
                    remainder.equals("hcenvlevel1value") || 
                    remainder.equals("hcenvlevel2value") || 
                    remainder.equals("hcenvlevel3value") || 
                    remainder.equals("hcenvrate0value") || 
                    remainder.equals("hcenvrate1value") || 
                    remainder.equals("hcenvrate2value") || 
                    remainder.equals("hcenvrate3value") || 
                    remainder.equals("hcenvloopvalue") ||
                    remainder.equals("formantbandnumber") ||
                    remainder.equals("formantbandvalue") ||
                    remainder.equals("hc0number") ||
                    remainder.equals("hc0value") ||
                    remainder.equals("hc1number") ||
                    remainder.equals("hc1value"))
                    return new Object[0];
                
                int val = model.get(key);
                if ((index = (Integer)(sourceParamsToIndex.get(remainder))) != null)
                    {
                    data = new int[] { 0x01, 0x01, (source - 1), 0x00, index.intValue(), 0x00, val };
                    }
                else if ((index = (Integer)(addWaveKitParamsToIndex.get(remainder))) != null)
                    {
                    data = new int[] { 0x02, 0x40, (source - 1), 0x00, index.intValue() - 1, 0x00, val };               // we subtract 1 to make up for checksum marked as "--"
                    }
                else if ((index = (Integer)(addWaveSoftHCParamsToIndex.get(remainder))) != null)
                    {
                    int harmonic = StringUtility.getSecondInt(remainder);       // after the "0"
                    data = new int[] { 0x02, 0x41, (source - 1), (harmonic - 1), 0x00, 0x00, val };
                    }
                else if ((index = (Integer)(addWaveLoudHCParamsToIndex.get(remainder))) != null)
                    {
                    int harmonic = StringUtility.getSecondInt(remainder);       // after the "1"
                    data = new int[] { 0x02, 0x42, (source - 1), (harmonic - 1), 0x00, 0x00, val };
                    }
                else if ((index = (Integer)(addWaveFormantFilterParamsToIndex.get(remainder))) != null)
                    {
                    int formant = StringUtility.getFirstInt(remainder);
                    data = new int[] { 0x02, 0x43, (source - 1), (formant - 1), 0x00, 0x00, val };
                    }
                else if ((index = (Integer)(addWaveHarmonicEnvelopeParamsToIndex.get(remainder))) != null)
                    {
                    int stage = StringUtility.getFirstInt(remainder);
                    int harmonic = StringUtility.getSecondInt(remainder);
                    boolean rate = remainder.contains("rate");
                    // each stage is two elements, first the rate and then the level
                    data = new int[] { 0x02, 0x44, (source - 1), (harmonic - 1), stage * 2 + (rate ? 0 : 1), 0x00, val };
                    }
                else if ((index = (Integer)(addWaveHarmonicLoopParamsToIndex.get(remainder))) != null)
                    {
                    int harmonic = StringUtility.getFirstInt(remainder);
                    data = new int[] { 0x02, 0x44, (source - 1), (harmonic - 1), 0x08, 0x00, val };
                    }
                else if (key.endsWith("ksoftharmonics") || key.endsWith("kloudharmonics") ||
                    key.contains("klevelharmonics"))
                    {
                    return new Object[0];   // harmonics dials
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
            int[] name = getNameAsBytes(model);
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
                data = new int[] { 0x03, 0x00, 0x01, 0x00, 0x00, 0x00, model.get(key) };
                }
            else 
                {
                if (key.endsWith("drywet1"))
                    {
                    if (type + 1 != StringUtility.getFirstInt(key))  return new Object[0]; // we're the wrong type
                    sub5 = 1;
                    }
                else if (key.endsWith("para1"))
                    {
                    if (type + 1 != StringUtility.getFirstInt(key))  return new Object[0]; // we're the wrong type
                    sub5 = 2;
                    }
                else if (key.endsWith("para2"))
                    {
                    if (type + 1 != StringUtility.getFirstInt(key))  return new Object[0]; // we're the wrong type
                    sub5 = 3;
                    }
                else if (key.endsWith("para3"))
                    {
                    if (type + 1 != StringUtility.getFirstInt(key))  return new Object[0]; // we're the wrong type
                    sub5 = 4;
                    }
                else if (key.endsWith("para4"))
                    {
                    if (type + 1 != StringUtility.getFirstInt(key))  return new Object[0]; // we're the wrong type
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
            int extra = 0;
            if (key.endsWith("type"))
                {
                sub5 = 0;
                extra = 11;                                                             // effects start with 11
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
            
            sub5 = effectShift(type, sub5 + 1) - 1;  // compress to fill in gaps
            data = new int[] { 0x03, 0x00, effect + 1, 0x00, sub5, 0x00, model.get(key) + extra };
            }
        else if (key.startsWith("geq"))
            {
            int eq = StringUtility.getFirstInt(key) - 1;
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
        /*
          else if (key.startsWith("macrocontroller"))
          {
          int pos = ((Integer)singleToneDataParamsToIndex.get(key)).intValue() - 
          ((Integer)singleToneDataParamsToIndex.get("macrocontroller1parameter1")).intValue();
          data = new int[] { 0x01, 0x00, 0x00, 0x00, pos, 0x00, model.get(key) };
          }
        */
        else if ((index = (Integer)(singleToneDataParamsToIndex.get(key))) != null)
            {
            data = new int[] { 0x01, 0x00, 0x00, 0x00, index.intValue() - COMMON_START, 0x00, model.get(key) };
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

    // We override this to guarantee that a reset is done before any change patch and also before the request is issued
    public void performRequestDump(Model tempModel, boolean changePatch)
        {
        // First, reload from Flash             -- we don't do this in ChangePatch because ChangePatch is also used when we write a patch, and it would undo everything
        if (backupAfterWriteToPatch)
            {
            performK5000Reset();  
            }  
                
        super.performRequestDump(tempModel, changePatch);
        }
    


    /** Request that the K5000 give us a patch */
    public byte[] requestDump(Model tempModel)
        {
        if (tempModel == null)
            tempModel = getModel();
        
        // Now return the sysex to send to request
        return new byte[] 
            { 
            (byte)0xF0, 
            (byte)0x40, 
            (byte)getChannelOut(), 
            (byte)0x00, 
            (byte)0x00, 
            (byte)0x0A,
            (byte)0x00,
            (byte)BANK_VALS[tempModel.get("bank")],
            (byte)(tempModel.get("number")),
            (byte)0xF7
            };
        }
        
    // This function is used to compact the effects that have holes in their parameters.
    // See the discussion above, labelled EFFECT COMPACTING.
    // type starts at 0
    // para starts at 1
    public int effectShift(int type, int para)
        {
        if (type + 12 == 12)    // Early Reflections 1
            {
            if (para >= 4) return para - 1;
            }
        if (type + 12 == 13)    // Early Reflections 2
            {
            if (para >= 4) return para - 1;
            }
        if (type + 12 == 16)    // Single Delay
            {
            if (para >= 3) return para - 1;
            }
        if (type + 12 == 18)    // Stereo Delay
            {
            if (para >= 4) return para - 2;
            }
        if (type + 12 == 19)    // Cross Delay
            {
            if (para >= 4) return para - 2;
            }
        if (type + 12 == 30)    // Ensemble
            {
            if (para >= 3) return para - 1;
            }
        if (type + 12 == 31)    // Ensemble and Delay
            {
            if (para >= 3) return para - 1;
            }
        if (type + 12 == 43)    // Exciter
            {
            if (para >= 5) return para - 1;
            }
        if (type + 12 == 44)    // Enhancer
            {
            if (para >= 5) return para - 1;
            }
        return para;
        }
        
    /** Return all bank names */
    public String[] getBankNames() { return BANKS; }

// Return a list of all patch number names.  
    public String[] getPatchNumberNames() { return buildIntegerNames(128, 1); }

// Return a list whether patches in banks are writeable.
    public boolean[] getWriteableBanks() { return new boolean[] { true, true, true, true, }; }

// Return a list whether individual patches can be written.
    public boolean getSupportsPatchWrites() { return true; }

// Return whether we allow downloads
    public boolean getSupportsDownloads() { return true; }

// Return the maximum length of a patch name
    public int getPatchNameLength() { return 8; }


    /** Request that the K5000 give us a bank */
    public byte[] requestBankDump(int bank) 
        {
        bank = BANK_VALS[bank];         // A = 0, D = 1, E = 2
        
        return new byte[] 
            { 
            (byte)0xF0, 
            (byte)0x40, 
            (byte)getChannelOut(), 
            (byte)0x01, 
            (byte)0x00, 
            (byte)0x0A,
            (byte)0x00,
            (byte)bank,
            (byte)0x00,
            (byte)0xF7
            };
        }


    // Patches are big.  We need a bigger countdown to wait for them to arrive during bulk downloading
    public int getBatchDownloadFailureCountdown() { return 50; }
        
    // When we don't get a response while doing a batch download, it is likely not because
    // the synth was slow to respond, but rather because the patch is a NULL patch, and the
    // K5000 doesn't respond at all for NULL patches -- in this case we must skip to the next patch.
    public boolean skipBatchPatchDownload() { return true; }
    
    // A list of positions in the sysex for real patches in the bank, as opposed to stubs (-1)
    int[] patchBankPositions = null;
    // A list of names of valid patches in the bank
    String[] patchBankNames = null;

    // Returns the position in bank sysex for a given patch number
    public int getPatchPosition(byte[] bank, int num)
        {
        if (patchBankPositions == null) // uh oh, should not happen
            {
            System.err.println("KawaiK5000.getPatchPosition() ERROR: bankPatchPositions was null");
            preprocessParseFromBank(bank);
            }

        return patchBankPositions[num];
        }


    // A name for stub patches, though we only use this in popup menus, not in bank lists
    public static final String EMPTY_PATCH = "[EMPTY PATCH]";
    
    /** Computer the locations of patches in the bank sysex, as well as patch names for each of them */
    // set to -1 if there is no such position -- the patch is null
    public void preprocessParseFromBank(byte[] bank)
        {
        // A Block Single Dump consists of:
        // A HEADER     F0 40 CHANNEL 21 00 0A 00 00            [8 Bytes]
        // A TONE MAP                                                                   [19 Bytes]
        // Some number (1-128) of TONE DATA:
        //              A CHECKSUM                                                              [1 Byte]
        //              EFFECT DATA                                                     [38 Bytes]
        //              COMMON DATA                                                             [43 Bytes]
        //              Some number (2-6) of SOURCE DATA:               [86 Bytes]
        //                      SOURCE DATA                                                     
        //              Some number (2-6) of ADD WAVE KIT DATA: [806 Bytes]
        //                      CHECKSUM
        //                      HC KIT DATA
        //                      HC CODE 1 DATA
        //                      HC CODE 2 DATA
        //                      FORMANT FILTER DATA
        //                      HARMONIC ENVELOPE DATA
        //                      LOUDNESS SENSE SELECT [DUMMY]           
        // A FOOTER F7                                                                  [1 Byte]
        
        patchBankNames = new String[128];
        patchBankPositions = new int[128];
        boolean[] tonemap = new boolean[128];
        int datalen = 8 + 19 + 1;
        int pos = 8;                                                    // start of tone map
        int count = 0;
        for (int i = 0; i < 126; i++)
            {
            // parse tone map
            tonemap[i] = (((bank[pos] >>> count++) & 0x1) == 0x1);
            if (count >= 7) { count = 0; pos++; }
            }
        tonemap[126] = (((bank[pos] >>> 0) & 0x1) == 0x1);
        tonemap[127] = (((bank[pos] >>> 1) & 0x1) == 0x1);
            
        // Now we have to go searching for the position :=(
                
        // reset pos
        pos = 8 + 19;                                                                   // start of first tone data
        for(int i = 0; i < 128; i++)
            {
            if (!tonemap[i]) 
                {
                patchBankPositions[i] = -1;
                patchBankNames[i] = EMPTY_PATCH;
                }
            else
                {
                patchBankPositions[i] = pos;

                pos++;                                                                              // skip checksum
                // extract name
                byte[] name = new byte[8];
                for(int j = 0; j < 8; j++)
                    {
                    name[j] = bank[pos + 39 + j];
                    }
                patchBankNames[i] = new String(name);
                
                // find the number of sources
                int sources = bank[pos + 50];

                pos += 81;                                              // start of source data
                boolean additive[] = new boolean[sources];
                for(int s = 0; s < sources; s++)
                    {
                    int msb = bank[pos + 28];
                    int lsb = bank[pos + 29];
                    additive[s] = (msb * 128 + lsb == 512);
                    pos += 86;                                                  // include source data
                    }
                
                for(int s = 0; s < sources; s++)
                    {
                    if (additive[s]) 
                        {
                        pos += 806;                                             // include checksum and optional wave kit data, though it's out of place here
                        }
                    }
                }
            }
        }



    /** Extracts and parses a patch from bank sysex */
    public int parseFromBank(byte[] bankSysex, int number) 
        {
        // Because this calls getPatchPosition each time, we're looking at an O(size(banksysex)^2)
        // total operation.  It's costly.  We need to preprocess all the bank positions once
        // and then access them from successive parseFromBank calls.  But first we'll see if
        // this is expensive enough that we need to consider doing it. 
        
        int pos = getPatchPosition(bankSysex, number);
        if (pos == -1)
            {
            return PARSE_IGNORE;                    // should produce an init patch (blank)
            }
        else
            {
            pos++;              // skip checksum, parseSingle() starts beyond it
            int retval = parseSingle(bankSysex, pos);
            revise();
            return retval;
            }
        }


    /** Returns which bank the bank sysex refers to */
    public int getBank(byte[] bankSysex) 
        { 
        int bank = bankSysex[7];
        if (bank > 0) bank--;   // A (0) == 0, D (2) == 1, E (3) == 2, F (4) == 3
        return bank; 
        }

    /** Returns the standard pause after writing a bank -- this is set to the pause after writing
        a patch (or pause after sending all parameters), but it may be moot if we can't write a whole bank anyway.  */
    public int getPauseAfterWriteBank() 
        {
        // Returns the pause, in milliseconds, after writing a bank sysex message
        // to the synthesizer.  By default this returns the value of 
        // getPauseAfterWritePatch();   This method only needs to be implemented 
        // if your patch editor supports bank reads (see documentation for 
        // getSupportsBankReads() and getSupportsBankWrites()).
        return getPauseAfterWritePatch(); 
        }  
        
    // We do NOT write bank patches on the Mac, because Java, or CoreMidi4Java, at present cannot write sysex messages larger than 39844 on the Mac
    public boolean getSupportsBankWrites() 
        {
        return false;
        //if (Style.isMac()) return false;
        //else return true;
        }
        
    // We read bank patches
    public boolean getSupportsBankReads()
        {
        return true;
        }


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
    "generalzonelo",
    "generalzonehi",
    "--",                                               // "velo sw" and "generalvelo"
    "generalzonepath",
    "generalvolume",
    "generalbenderpitch",
    "generalbendercutoff",
    "controlpressdestination1",
    "controlpressdepth1",
    "controlpressdestination2",
    "controlpressdepth2",
    "controlwheeldestination1",
    "controlwheeldepth1",
    "controlwheeldestination2",
    "controlwheeldepth2",
    "controlexpressdestination1",
    "controlexpressdepth1",
    "controlexpressdestination2",
    "controlexpressdepth2",
    "controlassignable1source",
    "controlassignable1destination",
    "controlassignable1depth",
    "controlassignable2source",
    "controlassignable2destination",
    "controlassignable2depth",
    "generalkeyondelay",
    "generalpantype",
    "generalpannormalvalue",
    "--",                       // wavekitmsb                           // we make:  wavekit  and       additive
    "--",                       // wavekitlsb
    "dcocoarse",
    "dcofine",
    "dcofixedkey",                                                                 // This is stored in Edisyn as 0 ... 108-21, but must be emitted as 0 vs 21...108
    "dcokspitch",
    "envdcostartlevel",
    "envdcoattacktime",
    "envdcoattacklevel",
    "envdcodecaytime",
    "envdcotimevelosense",
    "envdcolevelvelosense",
    "dcfenable",
    "dcfmode",
    "dcfvelocurve",
    "dcfresonance",
    "dcflevel",
    "dcfcutoff",
    "dcfcutoffksdepth",
    "dcfcutoffvelodepth",
    "envdcfdepth",
    "envdcfattacktime",
    "envdcfdecay1time",
    "envdcfdecay1level",
    "envdcfdecay2time",
    "envdcfdecay2level",
    "envdcfreleasetime",
    "envdcfksattacktime",
    "envdcfksdecay1time",
    "envdcfvelolevel",
    "envdcfveloattacktime",
    "envdcfvelodecay1time",
    "dcavelocurve",
    "envdcaattacktime",
    "envdcadecay1time",
    "envdcadecay1level",
    "envdcadecay2time",
    "envdcadecay2level",
    "envdcareleasetime",
    "envdcakslevel",
    "envdcaksattacktime",
    "envdcaksdecay1time",
    "envdcaksreleasetime",
    "envdcavelolevel",
    "envdcaveloattacktime",
    "envdcavelodecay1time",
    "envdcaveloreleasetime",
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
    "morfflag",                                 // this has no real function I believe
    "harmtotalgain",
    "harmgroup",
    "harmkstogain",
    "harmbalancevelocurve",
    "harmbalancevelodepth",
    "morfhc1patch",
    "morfhc1source",
    "morfhc2patch",
    "morfhc2source",
    "morfhc3patch",
    "morfhc3source",
    "morfhc4patch",
    "morfhc4source",
    "morfhetime1",
    "morfhetime2",
    "morfhetime3",
    "morfhetime4",
    "morfloop",
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
    // These are structured as soft/loud (soft=0) FIRST and harmonic ("s") SECOND.
    // This makes it possible to copy the envelope displays to one another.
    public static final String[] addWaveSoftHCParams = new String[]
    {
    "hc0s1",
    "hc0s2",
    "hc0s3",
    "hc0s4",
    "hc0s5",
    "hc0s6",
    "hc0s7",
    "hc0s8",
    "hc0s9",
    "hc0s10",
    "hc0s11",
    "hc0s12",
    "hc0s13",
    "hc0s14",
    "hc0s15",
    "hc0s16",
    "hc0s17",
    "hc0s18",
    "hc0s19",
    "hc0s20",
    "hc0s21",
    "hc0s22",
    "hc0s23",
    "hc0s24",
    "hc0s25",
    "hc0s26",
    "hc0s27",
    "hc0s28",
    "hc0s29",
    "hc0s30",
    "hc0s31",
    "hc0s32",
    "hc0s33",
    "hc0s34",
    "hc0s35",
    "hc0s36",
    "hc0s37",
    "hc0s38",
    "hc0s39",
    "hc0s40",
    "hc0s41",
    "hc0s42",
    "hc0s43",
    "hc0s44",
    "hc0s45",
    "hc0s46",
    "hc0s47",
    "hc0s48",
    "hc0s49",
    "hc0s50",
    "hc0s51",
    "hc0s52",
    "hc0s53",
    "hc0s54",
    "hc0s55",
    "hc0s56",
    "hc0s57",
    "hc0s58",
    "hc0s59",
    "hc0s60",
    "hc0s61",
    "hc0s62",
    "hc0s63",
    "hc0s64"
    };
    
    // Names of the loud harmonics parameters
    // The full name is "sourceNPARAM" where N is a number 1...6, and PARAM a string below.
    // These are structured as soft/loud (loud=1) FIRST and harmonic ("s") SECOND.
    // This makes it possible to copy the envelope displays to one another.
    public static final String[] addWaveLoudHCParams = new String[]
    {
    "hc1s1",
    "hc1s2",
    "hc1s3",
    "hc1s4",
    "hc1s5",
    "hc1s6",
    "hc1s7",
    "hc1s8",
    "hc1s9",
    "hc1s10",
    "hc1s11",
    "hc1s12",
    "hc1s13",
    "hc1s14",
    "hc1s15",
    "hc1s16",
    "hc1s17",
    "hc1s18",
    "hc1s19",
    "hc1s20",
    "hc1s21",
    "hc1s22",
    "hc1s23",
    "hc1s24",
    "hc1s25",
    "hc1s26",
    "hc1s27",
    "hc1s28",
    "hc1s29",
    "hc1s30",
    "hc1s31",
    "hc1s32",
    "hc1s33",
    "hc1s34",
    "hc1s35",
    "hc1s36",
    "hc1s37",
    "hc1s38",
    "hc1s39",
    "hc1s40",
    "hc1s41",
    "hc1s42",
    "hc1s43",
    "hc1s44",
    "hc1s45",
    "hc1s46",
    "hc1s47",
    "hc1s48",
    "hc1s49",
    "hc1s50",
    "hc1s51",
    "hc1s52",
    "hc1s53",
    "hc1s54",
    "hc1s55",
    "hc1s56",
    "hc1s57",
    "hc1s58",
    "hc1s59",
    "hc1s60",
    "hc1s61",
    "hc1s62",
    "hc1s63",
    "hc1s64"
    };

    // Names of the 64 formant frequencies.
    // The full name is "sourceNPARAM" where N is a number 1...6, and PARAM a string below.
    public static final String[] addWaveFormantFilterParams = new String[]
    {
    "formantband1",
    "formantband2",
    "formantband3",
    "formantband4",
    "formantband5",
    "formantband6",
    "formantband7",
    "formantband8",
    "formantband9",
    "formantband10",
    "formantband11",
    "formantband12",
    "formantband13",
    "formantband14",
    "formantband15",
    "formantband16",
    "formantband17",
    "formantband18",
    "formantband19",
    "formantband20",
    "formantband21",
    "formantband22",
    "formantband23",
    "formantband24",
    "formantband25",
    "formantband26",
    "formantband27",
    "formantband28",
    "formantband29",
    "formantband30",
    "formantband31",
    "formantband32",
    "formantband33",
    "formantband34",
    "formantband35",
    "formantband36",
    "formantband37",
    "formantband38",
    "formantband39",
    "formantband40",
    "formantband41",
    "formantband42",
    "formantband43",
    "formantband44",
    "formantband45",
    "formantband46",
    "formantband47",
    "formantband48",
    "formantband49",
    "formantband50",
    "formantband51",
    "formantband52",
    "formantband53",
    "formantband54",
    "formantband55",
    "formantband56",
    "formantband57",
    "formantband58",
    "formantband59",
    "formantband60",
    "formantband61",
    "formantband62",
    "formantband63",
    "formantband64",
    "formantband65",
    "formantband66",
    "formantband67",
    "formantband68",
    "formantband69",
    "formantband70",
    "formantband71",
    "formantband72",
    "formantband73",
    "formantband74",
    "formantband75",
    "formantband76",
    "formantband77",
    "formantband78",
    "formantband79",
    "formantband80",
    "formantband81",
    "formantband82",
    "formantband83",
    "formantband84",
    "formantband85",
    "formantband86",
    "formantband87",
    "formantband88",
    "formantband89",
    "formantband90",
    "formantband91",
    "formantband92",
    "formantband93",
    "formantband94",
    "formantband95",
    "formantband96",
    "formantband97",
    "formantband98",
    "formantband99",
    "formantband100",
    "formantband101",
    "formantband102",
    "formantband103",
    "formantband104",
    "formantband105",
    "formantband106",
    "formantband107",
    "formantband108",
    "formantband109",
    "formantband110",
    "formantband111",
    "formantband112",
    "formantband113",
    "formantband114",
    "formantband115",
    "formantband116",
    "formantband117",
    "formantband118",
    "formantband119",
    "formantband120",
    "formantband121",
    "formantband122",
    "formantband123",
    "formantband124",
    "formantband125",
    "formantband126",
    "formantband127",
    "formantband128"
    };
    
    // Names of the parameters for additive wave harmonic envelopes. 
    // The full name is "sourceNPARAM" where N is a number 1...6, and PARAM a string below.
    // These are structured as rate/level FIRST and harmonic ("s") SECOND.
    // This makes it possible to copy the envelope displays to one another.
    public static final String[] addWaveHarmonicEnvelopeParams = new String[]
    {
    "hcenvrate0s1",
    "hcenvlevel0s1",
    "hcenvrate1s1",
    "hcenvlevel1s1",
    "hcenvrate2s1",
    "hcenvlevel2s1",
    "hcenvrate3s1",
    "hcenvlevel3s1",
    "hcenvrate0s2",
    "hcenvlevel0s2",
    "hcenvrate1s2",
    "hcenvlevel1s2",
    "hcenvrate2s2",
    "hcenvlevel2s2",
    "hcenvrate3s2",
    "hcenvlevel3s2",
    "hcenvrate0s3",
    "hcenvlevel0s3",
    "hcenvrate1s3",
    "hcenvlevel1s3",
    "hcenvrate2s3",
    "hcenvlevel2s3",
    "hcenvrate3s3",
    "hcenvlevel3s3",
    "hcenvrate0s4",
    "hcenvlevel0s4",
    "hcenvrate1s4",
    "hcenvlevel1s4",
    "hcenvrate2s4",
    "hcenvlevel2s4",
    "hcenvrate3s4",
    "hcenvlevel3s4",
    "hcenvrate0s5",
    "hcenvlevel0s5",
    "hcenvrate1s5",
    "hcenvlevel1s5",
    "hcenvrate2s5",
    "hcenvlevel2s5",
    "hcenvrate3s5",
    "hcenvlevel3s5",
    "hcenvrate0s6",
    "hcenvlevel0s6",
    "hcenvrate1s6",
    "hcenvlevel1s6",
    "hcenvrate2s6",
    "hcenvlevel2s6",
    "hcenvrate3s6",
    "hcenvlevel3s6",
    "hcenvrate0s7",
    "hcenvlevel0s7",
    "hcenvrate1s7",
    "hcenvlevel1s7",
    "hcenvrate2s7",
    "hcenvlevel2s7",
    "hcenvrate3s7",
    "hcenvlevel3s7",
    "hcenvrate0s8",
    "hcenvlevel0s8",
    "hcenvrate1s8",
    "hcenvlevel1s8",
    "hcenvrate2s8",
    "hcenvlevel2s8",
    "hcenvrate3s8",
    "hcenvlevel3s8",
    "hcenvrate0s9",
    "hcenvlevel0s9",
    "hcenvrate1s9",
    "hcenvlevel1s9",
    "hcenvrate2s9",
    "hcenvlevel2s9",
    "hcenvrate3s9",
    "hcenvlevel3s9",
    "hcenvrate0s10",
    "hcenvlevel0s10",
    "hcenvrate1s10",
    "hcenvlevel1s10",
    "hcenvrate2s10",
    "hcenvlevel2s10",
    "hcenvrate3s10",
    "hcenvlevel3s10",
    "hcenvrate0s11",
    "hcenvlevel0s11",
    "hcenvrate1s11",
    "hcenvlevel1s11",
    "hcenvrate2s11",
    "hcenvlevel2s11",
    "hcenvrate3s11",
    "hcenvlevel3s11",
    "hcenvrate0s12",
    "hcenvlevel0s12",
    "hcenvrate1s12",
    "hcenvlevel1s12",
    "hcenvrate2s12",
    "hcenvlevel2s12",
    "hcenvrate3s12",
    "hcenvlevel3s12",
    "hcenvrate0s13",
    "hcenvlevel0s13",
    "hcenvrate1s13",
    "hcenvlevel1s13",
    "hcenvrate2s13",
    "hcenvlevel2s13",
    "hcenvrate3s13",
    "hcenvlevel3s13",
    "hcenvrate0s14",
    "hcenvlevel0s14",
    "hcenvrate1s14",
    "hcenvlevel1s14",
    "hcenvrate2s14",
    "hcenvlevel2s14",
    "hcenvrate3s14",
    "hcenvlevel3s14",
    "hcenvrate0s15",
    "hcenvlevel0s15",
    "hcenvrate1s15",
    "hcenvlevel1s15",
    "hcenvrate2s15",
    "hcenvlevel2s15",
    "hcenvrate3s15",
    "hcenvlevel3s15",
    "hcenvrate0s16",
    "hcenvlevel0s16",
    "hcenvrate1s16",
    "hcenvlevel1s16",
    "hcenvrate2s16",
    "hcenvlevel2s16",
    "hcenvrate3s16",
    "hcenvlevel3s16",
    "hcenvrate0s17",
    "hcenvlevel0s17",
    "hcenvrate1s17",
    "hcenvlevel1s17",
    "hcenvrate2s17",
    "hcenvlevel2s17",
    "hcenvrate3s17",
    "hcenvlevel3s17",
    "hcenvrate0s18",
    "hcenvlevel0s18",
    "hcenvrate1s18",
    "hcenvlevel1s18",
    "hcenvrate2s18",
    "hcenvlevel2s18",
    "hcenvrate3s18",
    "hcenvlevel3s18",
    "hcenvrate0s19",
    "hcenvlevel0s19",
    "hcenvrate1s19",
    "hcenvlevel1s19",
    "hcenvrate2s19",
    "hcenvlevel2s19",
    "hcenvrate3s19",
    "hcenvlevel3s19",
    "hcenvrate0s20",
    "hcenvlevel0s20",
    "hcenvrate1s20",
    "hcenvlevel1s20",
    "hcenvrate2s20",
    "hcenvlevel2s20",
    "hcenvrate3s20",
    "hcenvlevel3s20",
    "hcenvrate0s21",
    "hcenvlevel0s21",
    "hcenvrate1s21",
    "hcenvlevel1s21",
    "hcenvrate2s21",
    "hcenvlevel2s21",
    "hcenvrate3s21",
    "hcenvlevel3s21",
    "hcenvrate0s22",
    "hcenvlevel0s22",
    "hcenvrate1s22",
    "hcenvlevel1s22",
    "hcenvrate2s22",
    "hcenvlevel2s22",
    "hcenvrate3s22",
    "hcenvlevel3s22",
    "hcenvrate0s23",
    "hcenvlevel0s23",
    "hcenvrate1s23",
    "hcenvlevel1s23",
    "hcenvrate2s23",
    "hcenvlevel2s23",
    "hcenvrate3s23",
    "hcenvlevel3s23",
    "hcenvrate0s24",
    "hcenvlevel0s24",
    "hcenvrate1s24",
    "hcenvlevel1s24",
    "hcenvrate2s24",
    "hcenvlevel2s24",
    "hcenvrate3s24",
    "hcenvlevel3s24",
    "hcenvrate0s25",
    "hcenvlevel0s25",
    "hcenvrate1s25",
    "hcenvlevel1s25",
    "hcenvrate2s25",
    "hcenvlevel2s25",
    "hcenvrate3s25",
    "hcenvlevel3s25",
    "hcenvrate0s26",
    "hcenvlevel0s26",
    "hcenvrate1s26",
    "hcenvlevel1s26",
    "hcenvrate2s26",
    "hcenvlevel2s26",
    "hcenvrate3s26",
    "hcenvlevel3s26",
    "hcenvrate0s27",
    "hcenvlevel0s27",
    "hcenvrate1s27",
    "hcenvlevel1s27",
    "hcenvrate2s27",
    "hcenvlevel2s27",
    "hcenvrate3s27",
    "hcenvlevel3s27",
    "hcenvrate0s28",
    "hcenvlevel0s28",
    "hcenvrate1s28",
    "hcenvlevel1s28",
    "hcenvrate2s28",
    "hcenvlevel2s28",
    "hcenvrate3s28",
    "hcenvlevel3s28",
    "hcenvrate0s29",
    "hcenvlevel0s29",
    "hcenvrate1s29",
    "hcenvlevel1s29",
    "hcenvrate2s29",
    "hcenvlevel2s29",
    "hcenvrate3s29",
    "hcenvlevel3s29",
    "hcenvrate0s30",
    "hcenvlevel0s30",
    "hcenvrate1s30",
    "hcenvlevel1s30",
    "hcenvrate2s30",
    "hcenvlevel2s30",
    "hcenvrate3s30",
    "hcenvlevel3s30",
    "hcenvrate0s31",
    "hcenvlevel0s31",
    "hcenvrate1s31",
    "hcenvlevel1s31",
    "hcenvrate2s31",
    "hcenvlevel2s31",
    "hcenvrate3s31",
    "hcenvlevel3s31",
    "hcenvrate0s32",
    "hcenvlevel0s32",
    "hcenvrate1s32",
    "hcenvlevel1s32",
    "hcenvrate2s32",
    "hcenvlevel2s32",
    "hcenvrate3s32",
    "hcenvlevel3s32",
    "hcenvrate0s33",
    "hcenvlevel0s33",
    "hcenvrate1s33",
    "hcenvlevel1s33",
    "hcenvrate2s33",
    "hcenvlevel2s33",
    "hcenvrate3s33",
    "hcenvlevel3s33",
    "hcenvrate0s34",
    "hcenvlevel0s34",
    "hcenvrate1s34",
    "hcenvlevel1s34",
    "hcenvrate2s34",
    "hcenvlevel2s34",
    "hcenvrate3s34",
    "hcenvlevel3s34",
    "hcenvrate0s35",
    "hcenvlevel0s35",
    "hcenvrate1s35",
    "hcenvlevel1s35",
    "hcenvrate2s35",
    "hcenvlevel2s35",
    "hcenvrate3s35",
    "hcenvlevel3s35",
    "hcenvrate0s36",
    "hcenvlevel0s36",
    "hcenvrate1s36",
    "hcenvlevel1s36",
    "hcenvrate2s36",
    "hcenvlevel2s36",
    "hcenvrate3s36",
    "hcenvlevel3s36",
    "hcenvrate0s37",
    "hcenvlevel0s37",
    "hcenvrate1s37",
    "hcenvlevel1s37",
    "hcenvrate2s37",
    "hcenvlevel2s37",
    "hcenvrate3s37",
    "hcenvlevel3s37",
    "hcenvrate0s38",
    "hcenvlevel0s38",
    "hcenvrate1s38",
    "hcenvlevel1s38",
    "hcenvrate2s38",
    "hcenvlevel2s38",
    "hcenvrate3s38",
    "hcenvlevel3s38",
    "hcenvrate0s39",
    "hcenvlevel0s39",
    "hcenvrate1s39",
    "hcenvlevel1s39",
    "hcenvrate2s39",
    "hcenvlevel2s39",
    "hcenvrate3s39",
    "hcenvlevel3s39",
    "hcenvrate0s40",
    "hcenvlevel0s40",
    "hcenvrate1s40",
    "hcenvlevel1s40",
    "hcenvrate2s40",
    "hcenvlevel2s40",
    "hcenvrate3s40",
    "hcenvlevel3s40",
    "hcenvrate0s41",
    "hcenvlevel0s41",
    "hcenvrate1s41",
    "hcenvlevel1s41",
    "hcenvrate2s41",
    "hcenvlevel2s41",
    "hcenvrate3s41",
    "hcenvlevel3s41",
    "hcenvrate0s42",
    "hcenvlevel0s42",
    "hcenvrate1s42",
    "hcenvlevel1s42",
    "hcenvrate2s42",
    "hcenvlevel2s42",
    "hcenvrate3s42",
    "hcenvlevel3s42",
    "hcenvrate0s43",
    "hcenvlevel0s43",
    "hcenvrate1s43",
    "hcenvlevel1s43",
    "hcenvrate2s43",
    "hcenvlevel2s43",
    "hcenvrate3s43",
    "hcenvlevel3s43",
    "hcenvrate0s44",
    "hcenvlevel0s44",
    "hcenvrate1s44",
    "hcenvlevel1s44",
    "hcenvrate2s44",
    "hcenvlevel2s44",
    "hcenvrate3s44",
    "hcenvlevel3s44",
    "hcenvrate0s45",
    "hcenvlevel0s45",
    "hcenvrate1s45",
    "hcenvlevel1s45",
    "hcenvrate2s45",
    "hcenvlevel2s45",
    "hcenvrate3s45",
    "hcenvlevel3s45",
    "hcenvrate0s46",
    "hcenvlevel0s46",
    "hcenvrate1s46",
    "hcenvlevel1s46",
    "hcenvrate2s46",
    "hcenvlevel2s46",
    "hcenvrate3s46",
    "hcenvlevel3s46",
    "hcenvrate0s47",
    "hcenvlevel0s47",
    "hcenvrate1s47",
    "hcenvlevel1s47",
    "hcenvrate2s47",
    "hcenvlevel2s47",
    "hcenvrate3s47",
    "hcenvlevel3s47",
    "hcenvrate0s48",
    "hcenvlevel0s48",
    "hcenvrate1s48",
    "hcenvlevel1s48",
    "hcenvrate2s48",
    "hcenvlevel2s48",
    "hcenvrate3s48",
    "hcenvlevel3s48",
    "hcenvrate0s49",
    "hcenvlevel0s49",
    "hcenvrate1s49",
    "hcenvlevel1s49",
    "hcenvrate2s49",
    "hcenvlevel2s49",
    "hcenvrate3s49",
    "hcenvlevel3s49",
    "hcenvrate0s50",
    "hcenvlevel0s50",
    "hcenvrate1s50",
    "hcenvlevel1s50",
    "hcenvrate2s50",
    "hcenvlevel2s50",
    "hcenvrate3s50",
    "hcenvlevel3s50",
    "hcenvrate0s51",
    "hcenvlevel0s51",
    "hcenvrate1s51",
    "hcenvlevel1s51",
    "hcenvrate2s51",
    "hcenvlevel2s51",
    "hcenvrate3s51",
    "hcenvlevel3s51",
    "hcenvrate0s52",
    "hcenvlevel0s52",
    "hcenvrate1s52",
    "hcenvlevel1s52",
    "hcenvrate2s52",
    "hcenvlevel2s52",
    "hcenvrate3s52",
    "hcenvlevel3s52",
    "hcenvrate0s53",
    "hcenvlevel0s53",
    "hcenvrate1s53",
    "hcenvlevel1s53",
    "hcenvrate2s53",
    "hcenvlevel2s53",
    "hcenvrate3s53",
    "hcenvlevel3s53",
    "hcenvrate0s54",
    "hcenvlevel0s54",
    "hcenvrate1s54",
    "hcenvlevel1s54",
    "hcenvrate2s54",
    "hcenvlevel2s54",
    "hcenvrate3s54",
    "hcenvlevel3s54",
    "hcenvrate0s55",
    "hcenvlevel0s55",
    "hcenvrate1s55",
    "hcenvlevel1s55",
    "hcenvrate2s55",
    "hcenvlevel2s55",
    "hcenvrate3s55",
    "hcenvlevel3s55",
    "hcenvrate0s56",
    "hcenvlevel0s56",
    "hcenvrate1s56",
    "hcenvlevel1s56",
    "hcenvrate2s56",
    "hcenvlevel2s56",
    "hcenvrate3s56",
    "hcenvlevel3s56",
    "hcenvrate0s57",
    "hcenvlevel0s57",
    "hcenvrate1s57",
    "hcenvlevel1s57",
    "hcenvrate2s57",
    "hcenvlevel2s57",
    "hcenvrate3s57",
    "hcenvlevel3s57",
    "hcenvrate0s58",
    "hcenvlevel0s58",
    "hcenvrate1s58",
    "hcenvlevel1s58",
    "hcenvrate2s58",
    "hcenvlevel2s58",
    "hcenvrate3s58",
    "hcenvlevel3s58",
    "hcenvrate0s59",
    "hcenvlevel0s59",
    "hcenvrate1s59",
    "hcenvlevel1s59",
    "hcenvrate2s59",
    "hcenvlevel2s59",
    "hcenvrate3s59",
    "hcenvlevel3s59",
    "hcenvrate0s60",
    "hcenvlevel0s60",
    "hcenvrate1s60",
    "hcenvlevel1s60",
    "hcenvrate2s60",
    "hcenvlevel2s60",
    "hcenvrate3s60",
    "hcenvlevel3s60",
    "hcenvrate0s61",
    "hcenvlevel0s61",
    "hcenvrate1s61",
    "hcenvlevel1s61",
    "hcenvrate2s61",
    "hcenvlevel2s61",
    "hcenvrate3s61",
    "hcenvlevel3s61",
    "hcenvrate0s62",
    "hcenvlevel0s62",
    "hcenvrate1s62",
    "hcenvlevel1s62",
    "hcenvrate2s62",
    "hcenvlevel2s62",
    "hcenvrate3s62",
    "hcenvlevel3s62",
    "hcenvrate0s63",
    "hcenvlevel0s63",
    "hcenvrate1s63",
    "hcenvlevel1s63",
    "hcenvrate2s63",
    "hcenvlevel2s63",
    "hcenvrate3s63",
    "hcenvlevel3s63",
    "hcenvrate0s64",
    "hcenvlevel0s64",
    "hcenvrate1s64",
    "hcenvlevel1s64",
    "hcenvrate2s64",
    "hcenvlevel2s64",
    "hcenvrate3s64",
    "hcenvlevel3s64",
    "--",                           // dummy -- is this "loudness sense select?"
    };
        
    public static final String[] addWaveHarmonicLoopParams = new String[]
    {
    "hcenvloops1",
    "hcenvloops2",
    "hcenvloops3",
    "hcenvloops4",
    "hcenvloops5",
    "hcenvloops6",
    "hcenvloops7",
    "hcenvloops8",
    "hcenvloops9",
    "hcenvloops10",
    "hcenvloops11",
    "hcenvloops12",
    "hcenvloops13",
    "hcenvloops14",
    "hcenvloops15",
    "hcenvloops16",
    "hcenvloops17",
    "hcenvloops18",
    "hcenvloops19",
    "hcenvloops20",
    "hcenvloops21",
    "hcenvloops22",
    "hcenvloops23",
    "hcenvloops24",
    "hcenvloops25",
    "hcenvloops26",
    "hcenvloops27",
    "hcenvloops28",
    "hcenvloops29",
    "hcenvloops30",
    "hcenvloops31",
    "hcenvloops32",
    "hcenvloops33",
    "hcenvloops34",
    "hcenvloops35",
    "hcenvloops36",
    "hcenvloops37",
    "hcenvloops38",
    "hcenvloops39",
    "hcenvloops40",
    "hcenvloops41",
    "hcenvloops42",
    "hcenvloops43",
    "hcenvloops44",
    "hcenvloops45",
    "hcenvloops46",
    "hcenvloops47",
    "hcenvloops48",
    "hcenvloops49",
    "hcenvloops50",
    "hcenvloops51",
    "hcenvloops52",
    "hcenvloops53",
    "hcenvloops54",
    "hcenvloops55",
    "hcenvloops56",
    "hcenvloops57",
    "hcenvloops58",
    "hcenvloops59",
    "hcenvloops60",
    "hcenvloops61",
    "hcenvloops62",
    "hcenvloops63",
    "hcenvloops64",
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
    
    
    //// UTILITIES
    
    public static String[] kHarmonicsNames = null;
    public static int[][] kHarmonics = null;
    
    void loadKHarmonics()
        {
        if (kHarmonics == null)
            {
            try
                {
                ArrayList names = new ArrayList();
                ArrayList harmonics = new ArrayList();
        
                LineNumberReader reader = new LineNumberReader(new InputStreamReader(new BufferedInputStream(getClass().getResourceAsStream("kharmonics.out"))));
                while(true)
                    {
                    String n = reader.readLine();
                    if (n == null) break;
                    names.add(n.trim());
                    String h = reader.readLine();
                    Scanner scan = new Scanner(h);
                    int[] harm = new int[128];
                    for(int i = 0; i < 128; i++)
                        {
                        harm[i] = (int)Math.round((127.0 * scan.nextInt()) / 99.0);
                        }
                    harmonics.add(harm);
                    }
                kHarmonicsNames = (String[])(names.toArray(new String[0]));
                kHarmonics = (int[][])(harmonics.toArray(new int[0][0]));
                }
            catch (IOException ex)
                {
                ex.printStackTrace();
                }
            }
        }
    
    public void setKHarmonics(int source, int harmonics, boolean soft)
        {
        boolean midi = getSendMIDI();
        setSendMIDI(false);
        for(int i = 1; i <= 128; i++)                   // note <=
            {
            if (_constrainTo(i - 1)) model.set("source" + source + "hc" + (soft ? "0" : "1") + "s" + i, kHarmonics[harmonics - 1][i - 1]);
            }
        setSendMIDI(midi);
        } 

    public void actionKHarmonics(int source, int action, boolean soft)
        {
        getUndo().push(getModel());
        getUndo().setWillPush(false);
        boolean midi = getSendMIDI();
        setSendMIDI(false);
        for(int i = 1; i <= 64; i++)                   // note <=
            {
            if (_constrainTo(i - 1)) 
                {
                int val = model.get("source" + source + "hc" + (soft ? "0" : "1") + "s" + i);
                switch (action)
                    {
                    case ACTION_UP:
                        val = val + 1;
                        if (val > 127) val = 127;
                        break;
                    case ACTION_UP_HIGH:
                        val = val + 8;
                        if (val > 127) val = 127;
                        break;
                    case ACTION_DOWN:
                        val = val - 1;
                        if (val < 0) val = 0;
                        break;
                    case ACTION_DOWN_HIGH:
                        val = val - 8;
                        if (val < 0) val = 0;
                        break;
                    case ACTION_DOUBLE:
                        if (val == 0) val = 1;
                        else val = val * 2;
                        if (val > 127) val = 127;
                        break;
                    case ACTION_HALVE:
                        val = val / 2;
                        break;
                    case ACTION_RANDOM:
                        val = random.nextInt(128);
                        break;
                    case ACTION_JITTER:
                        int noise = 0;
                        while (true)
                            {
                            noise = random.nextInt(33) - 16;
                            if (noise + val <= 127 && noise + val >= 0) break;
                            }
                        val = noise + val;
                        break;
                    case ACTION_BOOST_BASS:
                        val = (int)Math.ceil(val + BOOST * (63 - (i - 1)));
                        if (val > 127) val = 127;
                        break;
                    case ACTION_DAMPEN_BASS:
                        val = (int)Math.floor(val - BOOST * (63 - (i - 1)));
                        if (val < 0) val = 0; 
                        break;
                    case ACTION_BOOST_TREBLE:
                        val = (int)Math.ceil(val + BOOST * (i - 1) );
                        if (val > 127) val = 127;
                        break;
                    case ACTION_DAMPEN_TREBLE:
                        val = (int)Math.floor(val - BOOST * (i - 1));
                        if (val < 0) val = 0; 
                        break;
                    }
                model.set("source" + source + "hc" + (soft ? "0" : "1") + "s" + i, val);
                }
            }
        setSendMIDI(midi);
        getUndo().setWillPush(true);
        }

    public void actionKLevelHarmonics(int source, int action, int level)
        {
        getUndo().push(getModel());
        getUndo().setWillPush(false);
        boolean midi = getSendMIDI();
        setSendMIDI(false);
        for(int i = 1; i <= 64; i++)                   // note <=
            {
            if (_constrainTo(i - 1)) 
                {
                int val = model.get("source" + source + "hcenvlevel" + level + "s" + i);
                switch (action)
                    {
                    case ACTION_UP:
                        val = val + 1;
                        if (val > 63) val = 63;
                        break;
                    case ACTION_UP_HIGH:
                        val = val + 4;
                        if (val > 63) val = 63;
                        break;
                    case ACTION_DOWN:
                        val = val - 1;
                        if (val < 0) val = 0;
                        break;
                    case ACTION_DOWN_HIGH:
                        val = val - 4;
                        if (val < 0) val = 0;
                        break;
                    case ACTION_DOUBLE:
                        if (val == 0) val = 1;
                        else val = val * 2;
                        if (val > 63) val = 63;
                        break;
                    case ACTION_HALVE:
                        val = val / 2;
                        break;
                    case ACTION_RANDOM:
                        val = random.nextInt(64);
                        break;
                    case ACTION_JITTER:
                        int noise = 0;
                        while (true)
                            {
                            noise = random.nextInt(17) - 8;
                            if (noise + val <= 63 && noise + val >= 0) break;
                            }
                        val = noise + val;
                        break;
                    case ACTION_BOOST_BASS:
                        val = (int)Math.ceil(val + BOOST / 2.0 * (63 - (i - 1)));
                        if (val > 63) val = 63;
                        break;
                    case ACTION_DAMPEN_BASS:
                        val = (int)Math.floor(val - BOOST / 2.0 * (63 - (i - 1)));
                        if (val < 0) val = 0; 
                        break;
                    case ACTION_BOOST_TREBLE:
                        val = (int)Math.ceil(val + BOOST / 2.0 * (i - 1) );
                        if (val > 63) val = 63;
                        break;
                    case ACTION_DAMPEN_TREBLE:
                        val = (int)Math.floor(val - BOOST / 2.0 * (i - 1));
                        if (val < 0) val = 0; 
                        break;
                    }
                model.set("source" + source + "hcenvlevel" + level + "s" + i, val);
                }
            }
        setSendMIDI(midi);
        getUndo().setWillPush(true);
        }
        
    public void setKLevelHarmonics(int source, int harmonics, int level)
        {
        boolean midi = getSendMIDI();
        setSendMIDI(false);
        for(int i = 1; i <= 128; i++)                   // note <=
            {
            if (_constrainTo(i - 1)) model.set("source" + source + "hcenvlevel" + level + "s" + i, kHarmonics[harmonics - 1][i - 1] / 2);       // only goes to 64
            }
        setSendMIDI(midi);
        } 
        
        
    String buildKHarmonics(int val)
    	{
                loadKHarmonics();
                String str = kHarmonicsNames[val-1];
                String name = null;
                String type = null;
                if (str.startsWith("BASIC"))
                	{
                	name = str.substring(6);
                	type = "&nbsp;";
                	}
                else if (str.startsWith("K5"))
                	{
                	name = str.substring(3);
                	type = "K5";
                	}
                else if (str.startsWith("VS"))
                	{
                	name = str.substring(3);
                	type = "VS";
                	}
                else if (str.startsWith("SQ"))
                	{
                	name = str.substring(3);
                	type = "SQ-80";
                	}
                else if (str.startsWith("K3"))
                	{
                	name = str.substring(3);
                	type = "K3";
                	}
                return "<html><center>&nbsp;&nbsp;&nbsp;" + val + "&nbsp;&nbsp;&nbsp;<br><font size=-3>" + name + "<br>" + type + "</font></center></html>";
    	}
        
    /** Adds the per-source "K5Harmonics" category. */    
    public JComponent addK5Harmonics(int source, Color color)
        {
        Category category = new Category(this, "Harmonics Edit", color);
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();

        comp = new LabelledDial("Soft", this, "source" + source + "ksoftharmonics", color, 1, 657)
            {
            boolean mouseDown;
            public String map(int val) 
                { 
                return buildKHarmonics(val);
                }
            public void didMouseDown()
                {
                getUndo().push(getModel());
                getUndo().setWillPush(false);
                mouseDown = true;
                }
            public void didMouseUp()
                {
                getUndo().setWillPush(true);
                mouseDown = false;              
                }
            public void update(String key, Model model)
                {
                super.update(key, model);
                if (mouseDown)
                    {
                    loadKHarmonics();
                    setKHarmonics(source, model.get(key, 1), true);
                    }
                }
            };
        ((LabelledDial)comp).setMaxExtent(658);
        model.setStatus("source" + source + "ksoftharmonics", Model.STATUS_RESTRICTED);
                
        hbox.add(comp);
        
        comp = new LabelledDial("Loud", this, "source" + source + "kloudharmonics", color, 1, 657)
            {
            boolean mouseDown;
            public String map(int val) 
                { 
                return buildKHarmonics(val);
                }
            public void didMouseDown()
                {
                getUndo().push(getModel());
                getUndo().setWillPush(false);
                mouseDown = true;
                }
            public void didMouseUp()
                {
                getUndo().setWillPush(true);
                mouseDown = false;              
                }
            public void update(String key, Model model)
                {
                super.update(key, model);
                if (mouseDown)
                    {
                    loadKHarmonics();
                    setKHarmonics(source, model.get(key, 1), false);
                    }
                }
            };
        ((LabelledDial)comp).setMaxExtent(658);
        model.setStatus("source" + source + "kloudharmonics", Model.STATUS_RESTRICTED);

        hbox.add(comp);

        PushButton doSoft = new PushButton("Soft Action", ACTIONS)
            {
            public void perform(int action)
                {
                actionKHarmonics(source, action, true);
                }
            };

        PushButton doLoud = new PushButton("Loud Action", ACTIONS)
            {
            public void perform(int action)
                {
                actionKHarmonics(source, action, false);
                }
            };
        
        vbox.add(doSoft);
        vbox.add(doLoud);
        hbox.add(vbox);
            
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
    

    /** Adds the per-source "K5 Level Harmonics" category. */    
    public JComponent addK5LevelHarmonics(int source, Color color)
        {
        Category category = new Category(this, "Level Harmonics Edit", color);
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        for(int i = 0; i < 4; i++)
            {
            final int _i = i;
            comp = new LabelledDial("Level " + i, this, "source" + source + "klevelharmonics" + i, color, 1, 657)
                {
                boolean mouseDown;
                public String map(int val) 
                    { 
                	return buildKHarmonics(val);
                    }
                public void didMouseDown()
                    {
                    getUndo().push(getModel());
                    getUndo().setWillPush(false);
                    mouseDown = true;
                    }
                public void didMouseUp()
                    {
                    getUndo().setWillPush(true);
                    mouseDown = false;              
                    }
                public void update(String key, Model model)
                    {
                    super.update(key, model);
                    if (mouseDown)
                        {
                        loadKHarmonics();
                        setKLevelHarmonics(source, model.get(key, 1), _i);
                        }
                    }
                };
            ((LabelledDial)comp).setMaxExtent(658);
            model.setStatus("source" + source + "klevelharmonics" + i, Model.STATUS_RESTRICTED);
                                
            hbox.add(comp);
            }

        PushButton doLevel0 = new PushButton("Level 0 Action", ACTIONS)
            {
            public void perform(int action)
                {
                actionKLevelHarmonics(source, action, 0);
                }
            };

        PushButton doLevel1 = new PushButton("Level 1 Action", ACTIONS)
            {
            public void perform(int action)
                {
                actionKLevelHarmonics(source, action, 1);
                }
            };
        
        PushButton doLevel2 = new PushButton("Level 2 Action", ACTIONS)
            {
            public void perform(int action)
                {
                actionKLevelHarmonics(source, action, 2);
                }
            };
        
        PushButton doLevel3 = new PushButton("Level 3 Action", ACTIONS)
            {
            public void perform(int action)
                {
                actionKLevelHarmonics(source, action, 3);
                }
            };
        
        vbox.add(doLevel0);
        vbox.add(doLevel1);
        hbox.add(vbox);
        
        vbox = new VBox();
        vbox.add(doLevel2);
        vbox.add(doLevel3);
        hbox.add(vbox);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
 
    /** A convenience method for loading a WAV file. */
    public File doLoad(String title, final String[] filenameExtensions)
        {
        FileDialog fd = new FileDialog((JFrame)(SwingUtilities.getRoot(this)), title, FileDialog.LOAD);
        fd.setFilenameFilter(new FilenameFilter()
            {
            public boolean accept(File dir, String name)
                {
                for(int i = 0; i < filenameExtensions.length; i++)
                    if (StringUtility.ensureFileEndsWith(name, filenameExtensions[i]).equals(name))
                        return true;
                return false;
                }
            });

        fd.setDirectory(getLastX("WavDirectory", getSynthClassName(), true));

        disableMenuBar();
        fd.setVisible(true);
        enableMenuBar();
        File f = null; // make compiler happy
                
        if (fd.getFile() != null)
            {
            try
                {
                f = new File(fd.getDirectory(), fd.getFile());
                setLastX(f.getCanonicalPath(), "WavDirectory", getSynthClassName(), true);
                }                       
            catch (Exception ex)
                {
                Synth.handleException(ex); 
                }
            }
            
        return f;
        }

    public static final int MAXIMUM_SAMPLES = 2048;
    public static final int WINDOW_SIZE = 65;
    public static final double MINIMUM_AMPLITUDE = 0.001;

    public double[] doLoadWave()
        {
        File file = doLoad("Load Wave...", new String[] { "wav", "WAV" });
        if (file == null) return null;
        
        double[] waves = null;
        double[] buffer = new double[256];
        int count = 0;
        
        WavFile wavFile = null;
        try 
            {
            double[] _waves = new double[MAXIMUM_SAMPLES];
            wavFile = WavFile.openWavFile(file);
                        
            while(true)
                {
                // Read frames into buffer
                int framesRead = wavFile.readFrames(buffer, buffer.length);
                if (count + framesRead > MAXIMUM_SAMPLES)
                    {
                    showSimpleError("File Too Large", "This file may contain no more than " + MAXIMUM_SAMPLES + " samples.");
                    return null;
                    }
                System.arraycopy(buffer, 0, _waves, count, framesRead);
                count += framesRead;
                if (framesRead < buffer.length) 
                    break;
                }
            waves = new double[count];
            System.arraycopy(_waves, 0, waves, 0, count);
            }
        catch (IOException ex)
            {
            showSimpleError("File Error", "An error occurred on reading the file.");
            return null;
            }
        catch (WavFileException ex)
            {
            showSimpleError("Not a proper WAV file", "WAV files must be mono 16-bit.");
            return null;
            }

        try
            {
            wavFile.close();
            }
        catch (Exception ex) { }
        
        int desiredSampleSize = 128 * 2;                          // because we have up to 128 partials
        int currentSampleSize = waves.length;
                                                                        
        /// Resample to our sampling rate
        double[] newvals = WindowedSinc.interpolate(
            waves,
            currentSampleSize,
            desiredSampleSize,              // notice desired and current are swapped -- because these are SIZES, not RATES
            WINDOW_SIZE,
            true);           
                        
        // Note no window.  Should still be okay (I think?)
        double[] harmonics = FFT.getHarmonics(newvals);
        double[] finished = new double[harmonics.length / 2];
        for (int s=1 ; s < harmonics.length / 2; s++)                   // we skip the DC offset (0) and set the Nyquist frequency bin (harmonics.length / 2) to 0
            {
            finished[s - 1] = (harmonics[s] >= MINIMUM_AMPLITUDE ? harmonics[s]  : 0 );
            }

        double max = 0;
        for(int i = 0; i < finished.length; i++)
            {
            if (max < finished[i])
                max = finished[i];
            }
                                        
        if (max > 0)
            {
            for(int i = 0; i < finished.length; i++)
                {
                finished[i] /= max;
                }
            }
                        
        return finished;
        }

    public static final int HARMONICS_BOTH = 0;
    public static final int HARMONICS_1 = 1;
    public static final int HARMONICS_2 = 2;
    
    public void loadWaveAsHarmonics(int source, boolean soft)
        {
        boolean currentMIDI = getSendMIDI();
        setSendMIDI(false);
        double[] harm = doLoadWave();
        if (harm == null) 
            {
            setSendMIDI(currentMIDI);
            return;
            }
                
        for(int i = 1; i <= 128; i++)                   // note <=
            {
            int h = (int)(harm[i - 1] * 128);
            if (h == 128) h = 127;
            model.set("source" + source + "hc" + (soft ? "0" : "1") + "s" + i, h);
            }
        setSendMIDI(currentMIDI);
        repaint();
        }
       
    
    //// TESTING

    public boolean testVerify(Synth synth2, String key, Object obj1, Object obj2) 
        {
        // We have lots of effects that will be zero regardless
        if (key.startsWith("effect")) return true;
        if (key.startsWith("reverb")) return true;
        
        // wavekit is spit up into msb and lsb and will give incorrect values
        if (key.contains("dcowavekit")) return true;
        
        if (key.startsWith("source"))
            {
            int source = StringUtility.getFirstInt(key);
            if (source >= model.get("srctype")) return true;                // this isn't a valid source
            
            String removed = StringUtility.removePreambleAndFirstDigits(key, "source");
            if ((addWaveKitParamsToIndex.containsKey(removed) ||
                    addWaveSoftHCParamsToIndex.containsKey(removed) ||
                    addWaveLoudHCParamsToIndex.containsKey(removed) ||
                    addWaveFormantFilterParamsToIndex.containsKey(removed) ||
                    addWaveHarmonicEnvelopeParamsToIndex.containsKey(removed) ||
                    addWaveHarmonicLoopParamsToIndex.containsKey(removed)) &&               // it's a wavekit parameter
                model.get("source" + source + "dcoadditive") == 0) // it's not an additive source, so shouldn't be there any more
                {
                return true;
                }
            }
                
        return false; 
        }

    }
     
/* 
   ABOUT K5000 MEMORY

   I suppose in order to support loading from the disk drive, the K5000 has an unusual memory structure.
   It has a CURRENT WORKING MEMORY consisting of ALL BANKS OF ALL PATCHES.  It also has a PERMANENT STORE
   consisting of ALL BANKS OF ALL PATCHES.  The current working memory also has a pointer to a single patch
   which is the current patch being played.
        
   On the machine, you can:
        
   - Load a patch from disk to a slot in the current working memory
   - Load a patch from permanent store to a slot in the current working memory
   - Load a bank from the disk to the current working memory
   - Write a patch from the current working memory to the disk
   - Write a bank from the current working memory to the disk
   - Edit a the patch being played in current working memory.  However when you have finished 
   editing it, you must either (1) write the changes for that patch to the permanent store or 
   (2) undo your changes.
   - Change individual parameters in the patch being played in current working memory.
                
   You can also (but it's not useful unless you're working with an editor):
   - Write all patches in all banks from the permanent store to current working memory ("RESET")
   - Write all patches in all banks from current working memory to the permanent store ("BACKUP")

   But this isn't how it works from sysex.  In sysex you can read and modify any numbered patch in current  
   working memory independently without writing any of them to the permanent store.  However, you can't query
   what the currently-played patch is, nor request it, nor ask to modify it. You also cannot write a single 
   patch to the permanent store.  You *can* perform a reset or a backup.  You can request a whole bank
   from current working memory and update a whole bank, but you cannot clear a bank.  And you can change 
   individual parameters.  You cannot access the disk at all.
        
   Also, single-mode patches are wildly varying size, but each bank is fixed in number of patches and also
   in byte length, so you will wind up with banks that are smaller in number than the full bank length but
   have filled up its entire memory.  If you attempt to load another patch, it will fail *silently*.
   There is also no way to clear patches or clear the bank.
        
   Also a bank must have at least one patch in it.
        
   Furthermore, the permanent store is in Flash RAM.  This poses a challenge.
        
   At present Edisyn works like this:
        
   - WRITE TO PATCH                Does a RESET, then updates the patch in current working memory, then a BACKUP,
   and also does a PC away from the patch (to guarantee the name is updated on 
   display), then back to the patch 
   - LIBRARY WRITE REGION  Does a RESET, then updates the patches in current working memory, then a BACKUP
   - LIBRARY WRITE BANK    Does a RESET, then updates the patches in current working memory, then a BACKUP
   - LIBRARY WRITE ALL             IS DISABLED                     [so is Library SAVE ALL]
   - K5000 CLEAR BANK              Writes a bank message of one small blank patch, plus 126 stubs, 
   to current working memory.
   - K5000 RESET                   Does a RESET
   - K5000 BACKUP                  Does a BACKUP
   - SEND TO CURRENT PATCH Updates the patch in current working memory: if the patch number is unknown,
   then assumes it is Patch A001. also does a PC away from the patch (to guarantee 
   the name is updated on display), then back to the patch
                
   If you have unchecked the menu option "Perform Backup and/or Reset after Writes and Requests", then
   these behave differently:

   - WRITE TO PATCH                Updates the patch in current working memory, and also does a PC away from the 
   patch (to guarantee the name is updated on display), then back to the patch 
   - SEND TO CURRENT PATCH IS DISABLED     [In all cases, including Send to Current, undo/hillclimbing/etc.]
                
   The purpose of this unchecked option is primarily to allow a user to negotiate with the disk.  For example,
   he can load a bank from the disk, write some patches into it, and then save the bank to the disk (or do a
   BACKUP to save the bank to the permanent store).  It should normally be kept checked.

/*
KAWAI K5000 ECCENTRICITIES
        
The is a small documentation of some of the Kawai K5000's misfeatures and documentation errors.
        
BANKS GO A, D, E, F, M.  Bank B is a modified general MIDI bank for the Kawai K5000W and Bank C is a fixed
general MIDI bank for the K5000W.
        
MISSING SEND-TO-CURRENT-PATCH SYSEX MESSAGE.  This is the biggest one.  The Kawai K5000, like the K5 and K1,
does not have a send-to-current-patch message, which makes auditioning, syncing, resetting, randomizing, and
lots of other tasks very difficult.
        
EXCESSIVELY LONG BANK SYSEX MESSAGE.  The K5000's bank sysex dump mesages can be 100K or longer.  This is
absurd: it takes forever, provides no feedback as to its current status, and worst of all, Java can't send
messages that long.
        
UNDOCUMENTED KAA AND KA1 FILES.  People have reverse-engineered these but Edisyn presently cannot support them.
        
HARMONIC ENVELOPE RATES ARE OPPOSITE OTHER ENVELOPE RATES.  Most envelopes have "times" which go 0...127.
But harmonic envelope rates are "rates" and go 127...0.
        
SYSEX VALUES THAT SERVE NO PURPOSE.
        
- MORF FLAG only serves to determine which screen is displayed
- DRUM MARK is unknown
- NO USE is some how different from DUMMY
                
HARMONIC ENVELOPE LOOP PARAMETER DOCUMENTATION IS WRONG.   The documentation says that OFF is Level1=64, 
Level2 =0; LP1 is Level1=0, Level2=64; LP2 is Level1=64, Level2=64, UNKNOWN is Level1=0, Level2=0.  This is
WRONG.  The correct values are: OFF is Level1=0, Level2=0; LP1 is Level1=64, Level2=64, LP2 is Level1=0,
Level2=64, UNKNOWN is Level1=64, Level2=0.
        
DCA VELO CURVE is shown with just three bits.  But it has values 0...11.
        
CHANGE TO SINGLE MODE (p. 36) does not do anything.
        
EFFECTS START AT 11.  Effects are separate from Reverb methods in the documentation and in the sysex messages.
But they still start at 11 for no reason (there are 11 reverb methods, 0...10).
        
PATCHES ARE VARIABLE IN SIZE.  However banks are fixed to 128 patches and have a fixed amount of memory.  This
means that (1) you typically can't put 128 patches in a bank, so (2) you have to have dummy patches filling the
remaining slots.
        
SR WAVE 424 DUPLICATES SR WAVE 425.  At least in the documentation, where they're both called
"423 > 64th Harmonics Cyclic".
        
NUMEROUS INCONSISTENCIES IN EFFECTS PARAMETERS.   Don't get me started.
        
MORF LOOP.  The Morf Loop parameter in sysex appears to have off, lp1, and lp2 as options.  But on the unit
the only options are off and "loop".
        
MORF EXECUTION.  You can execute a Morf from sysex.  But you cannot download the resulting Morf because the
K5000 is lacking a Request Current Patch sysex message.
        
*/                                  
