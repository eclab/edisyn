/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.waldorfkyra;

import edisyn.*;
import edisyn.gui.*;
import edisyn.util.*;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.border.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javax.sound.midi.*;


/**
   A patch editor for the Waldorf Kyra.
        
   @author Sean Luke
*/

public class WaldorfKyra extends Synth
    {
    public static final String[] BANKS = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };
    public static final String[] WRITABLE_BANKS = { "A", "B", "C", "D", "E", "F", "G" };
    
    public static final String[] CATEGORIES = 
        {
        "Work in Progress", "Acid", "Ambient", "Arpeggio", "Bass", "EDM & Dubstep", "Effects", 
        "Electronica", "Guitars & Plucked", "Leads", "Lo-Fi & Distorted", "My Favourites",              // british "favorites" grrr
        "Orchestral", "Organs", "Pads", "Percussion", "Pianos", "Stabs & Strings", "Strings", "Trance"
        };
        
    public static final String[] WAVE_GROUPS =
        {
        "Construction Set 1", "Construction Set 2", "Construction Set 3", "Construction Set 4", 
        "Construction Set 5", "Construction Set 6", "Construction Set 7", "Construction Set 8", 
        "Construction Set 9", "Construction Set 10", "Construction Set 11", "Construction Set 12", 
        "Construction Set 13", "Construction Set 14", "Construction Set 15", "Construction Set 16", 
        "Construction Set 17", "Construction Set 18", "Construction Set 19", "Construction Set 20", 
        "Acoustic Guitar", "Alto Saxophone", "Birds", "Bit Reduced Lo-Fi", "Blended Waveforms", 
        "Bright Sawtooth", "C604", "Cello", "Cheezy Strings", "Clarinet", "Clavinet", 
        "Double Bass", "Distorted", "Electric Bass", "Electric Guitar", "Electric Organ 1", 
        "Electric Organ 2", "Electric Piano", "Flute", "FM Synth", "Gapped Sawtooth", "Granular", 
        "Hand Drawn", "Human Voice", "Linear Waveforms", "Oboe", "Osc Chip Lo-Fi 1", 
        "Osc Chip Lo-Fi 2", "Overtone", "Piano", "Pluck Algorithm", "Rounded Squares", 
        "Raw Waveforms", "Round Squares Sym", "Round Squares Asym", "Rounded Saw Sym", 
        "Sawtooth Bit Reduced", "Sawtooth", "Sine Bit Reduced", "Sine Harmonics", "Sine", 
        "Snippets", "Square Bit Reduced", "Square", "Symmetric", "Tannernin", "Theremin", 
        "Triangle Bit Reduced", "Triangle", "Video Game 1", "Video Game 2", "Violin"
        };
        
    public static final int[] WAVE_COUNTS = 
        {
        // Construction Set
        100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 
        100, 100, 100, 100, 100, 100, 100, 100, 100, 111,
        // Others
        38, 26, 14, 40, 73, 10, 32, 19, 6, 25, 33, 69, 
        45, 70, 22, 128, 27, 73, 16, 122, 42, 44, 50,
        104, 85, 13, 128, 30, 44, 30, 9, 26, 36, 26,
        26, 26, 8, 50, 8, 16, 12, 48, 7, 100, 17, 4,
        22, 8, 25, 128, 11, 14
        };

    public static final String[] CHORUS_EFFECTS = { "Comb (0-10ms)", "Flanger (0-20ms)", "Chorus (0-40ms)", "Doubler (0-160ms)" };
    public static final String[] DUAL_MODES = { "Mono", "Stereo" };
    public static final String[] VOICE_MODES = {  "Wave", "Hypersaw" };
    public static final String[] KEY_MODES = {  "Polyphonic", "Monophonic" };
    public static final String[] ARP_MODES = { "UpDown", "Up", "Down", "Chord", "Random" };
    public static final String[] LATCH_MODES = { "Sustain Pedal", "Key Latch" };
    public static final String[] ARP_OCTAVES = { "1 Oct", "2 Oct", "3 Oct" };
    public static final String[] ARP_BEATS = { "1/32", "1/16 T", "1/16", "1/8 T", "1/16 D", "1/8", "1/4 T", "1/8 D", "1/4", };
    public static final String[] SUBOSCILLATOR_SHAPES = { "Saw", "Square", "Pulse", "Triangle", "Saw / Root", "Square / Root", "Pulse / Root", "Triangle / Root" };
    public static final String[] AUX_OSCILLATOR_MODES = { "Noise", "Ring Mod" };
    public static final String[] FILTER_MODES = { "12dB Low Pass", "24dB Low Pass", "12dB Band Pass", "24db Band Pass", "12dB High Pass", "24dB High Pass" };         
    public static final String[] LFO_MODES = { "Monophonic", "Polyphonic", "Random Phase", "Dual Antiphase", "Dual Quadrature" }; 
    public static final String[] LFO3_MODES = { "Monophonic", "Polyphonic", "Random Phase" };
    public static final String[] LFO_TIME_SOURCES = { "Internal", "MIDI 1/16", "MIDI 1/8 T", "MIDI 3/32", "MIDI 1/8", "MIDI 1/4 T", "MIDI 3/16", "MIDI 1/4", "MIDI 1/2 T", "MIDI 3/8", "MIDI 1/1", "MIDI 2/1", "MIDI 2/1" };
    
    public static final String[] MOD_SOURCES = { 
        "None",
        "Pitch Bend", "Channel Pressure", "Mod Wheel (CC 1)", "Breadth (CC 2)", 
        "MIDI CC 3", "Foot (CC 4)", "Data Entry (CC 6)", "Balance (CC 8)", 
        "CC 9", "Pan (CC 10)", "Expression (CC 11)", "CC 12", "CC 13", "CC 14", 
        "CC 15", "CC 16", "CC 17", "CC 18", "CC 19", "Sustain (CC 64)", 
        "Envelope 1 [Poly]", "Envelope 2 [Poly]", "Envelope 3 [Poly]", "LFO 1 Unipolar [Poly]", "LFO 1 Bipolar [Poly]", 
        "LFO 2 Unipolar [Poly]", "LFO 2 Bipolar [Poly]", "LFO 3 Unipolar [Poly]", "LFO 3 Bipolar [Poly]", 
        "Note On Velocity [Poly]", "Random per Note [Poly]", "MIDI Note [Poly]", "Polyphonic Pressure [Poly]", "Note Off Velocity [Poly]" };
    
    public static final String[] MOD_DESTINATIONS = {
        "None",
        "Osc 1 Tune", "Osc 1 Detune", "Osc 1 Wave Osc 2 FM", "Osc 1 LFO 1 Pitch", 
        "Osc 1 LFO 2 Pulsewidth", "Osc 1 Sawtooth Level", "Osc 1 Wave Level", 
        "Osc 1 Pulse Level", "Osc 1 Aux Osc Level", "Osc 1 Sub Level", "Osc 1 Sub Detune", 
        "Osc 1 Pulsewidth", "Osc 2 Tune", "Osc 2 Detune", "Osc 2 LFO 1 Pitch", 
        "Osc 2 LFO 2 Pulsewidth", "Osc 2 Sawtooth Level", "Osc 2 Wave Level", 
        "Osc 2 Pulse Level", "Osc 2 Sub Level", "Osc 2 Sub Detune", "Osc 2 Pulsewidth", 
        "Hypersaw Intensity", "Hypersaw Spread", "Filter 1/2 Balance", "Filter 1 Cutoff", 
        "Filter 1 Resonance", "Filter 1 Env 1 Cutoff", "Filter 1 Env 2 Cutoff", 
        "Filter 1 LFO 2 Cutoff", "Filter 2 Cutoff", "Filter 2 Resonance", "Filter 2 Env 1 Cutoff", 
        "Filter 2 Env 2 Cutoff", "Filter 2 LFO 2 Cutoff", "VCA Level (Pre-Effects)", 
        "VCA Pan", "VCA Stereo Width", "VCA LFO 1 > Amp", "VCA LFO 2 > Pan", 
        "Env 1 Attack", "Env 1 Decay", "Env 1 Sustain", "Env 1 Release", "Env 2 Attack", 
        "Env 2 Decay", "Env 2 Sustain", "Env 2 Release", "Env 3 Attack", "Env 3 Decay", 
        "Env 3 Sustain", "Env 3 Release", "LFO 1 Speed", "LFO 1 Delay", "LFO 2 Speed", 
        "LFO 2 Delay", "LFO 3 Speed", "DDL Mix", "DDL Delay ", "DDL Feedback", 
        "Phaser Mix", "Phaser Feedback", "Phaser Modulation Rate", "Phaser Modulation Depth", 
        "Phaser Frequency", "Chorus Mix", "Chorus Delay", "Chorus Feedback", 
        "Chorus Modulation Rate", "Chorus Modulation Depth", "Reverb Mix", "Reverb Time", 
        "Reverb Damping", "Reverb Darkness", "EQ Mid Gain", "EQ Mid Frequency", 
        "Formant Filter Gain", "Formant Filter Tune", "Distortion Mix", "Distortion Drive", 
        "Final Level (Post-Effects)", "Dual Detune Amount"  };
        
    public static final String[] DELAY_TYPES = {  "Stereo", "Ping Pong" };                      // Are these right?
    public static final String[] DELAY_CLOCK_SOURCES = { "Internal", "MIDI", "LFO3" };
    public static final String[] DELAY_CLOCK_BEATS = { "1/64", "1/32 T", "1/32", "1/16 T", "1/16", "1/8 T", "3/32", "1/8", "1/4 T", "3/16", "1/4", "1/2 T", "3/8", "1/2" };
    public static final String[] DISTORTION_LPF = new String[] { "1 kHz", "2 kHz", "4 kHz", "8 kHz" };
    public static final String[] DISTORTION_ALGORITHMS = {  "Soft Rectifier", "Hard Rectifier", "Soft Bitcrusher", "Hard Bitcrusher", "Gnasher"  };
    public static final String[] FX_LIMITER_MODES = { "Light", "Medium", "Heavy" };                    // FIXME:  is this the vca saturator mode?  Are these names correct?
    public static final int NUM_AKWF_WAVE_GROUPS = 72;
    
    public static final int[] EQ_MID_Q = new int[]
    {
    66, 68, 69, 71, 73, 75, 77, 79, 80, 83, 85, 87, 89, 91, 93, 96, 
    98, 101, 103, 106, 108, 111, 114, 117, 120, 123, 126, 129, 132, 135, 139, 142, 
    146, 150, 153, 157, 161, 165, 169, 174, 178, 182, 187, 192, 197, 201, 207, 212, 
    217, 223, 228, 234, 240, 246, 252, 258, 265, 271, 278, 285, 292, 300, 307, 315, 
    323, 331, 339, 348, 356, 365, 375, 384, 394, 404, 414, 424, 435, 446, 457, 468, 
    480, 492, 504, 517, 530, 543, 557, 571, 585, 600, 615, 631, 646, 663, 679, 696, 
    714, 732, 750, 769, 788, 808, 828, 849, 871, 892, 915, 938, 961, 985, 1010, 1040, 
    1060, 1090, 1120, 1140, 1170, 1200, 1230, 1260, 1300, 1330, 1360, 1400, 1430, 1470, 1500, 1540
    };
    
    public static final int[] EQ_LOW_FREQUENCIES = new int[]
    {
    200, 205, 210, 215, 220, 230, 235, 240, 250, 255, 265, 270, 280, 285, 295, 
    300, 310, 320, 330, 340, 350, 360, 370, 380, 390, 400, 410, 420, 430, 440, 
    450, 460, 480, 490, 500, 520, 530, 550, 560, 580, 600, 610, 630, 650, 660, 
    680, 700, 720, 740, 760, 780, 800, 820, 850, 870, 900, 920, 950, 970, 1000, 
    1030, 1060, 1090, 1110, 1150, 1180, 1210, 1240, 1280, 1310, 1350, 1390, 
    1430, 1470, 1510, 1550, 1590, 1640, 1680, 1730, 1770, 1820, 1870, 1930, 
    1980, 2030, 2090, 2150, 2200, 2270, 2330, 2400, 2460, 2530, 2600, 2670, 
    2750, 2820, 2900, 2980, 3060, 3150, 3240, 3320, 3410, 3510, 3610, 3710, 
    3810, 3920, 4020, 4140, 4250, 4370, 4490, 4610, 4740, 4870, 5010, 5140, 
    5290, 5430, 5580, 5740, 5900, 6060, 6230, 6400
    };
 
    public static final int[] EQ_MID_FREQUENCIES = new int[]
    {
    20, 21, 22, 23, 25, 26, 28, 29, 31, 33, 35, 36, 38, 41, 43, 45, 48, 51, 
    53, 56, 60, 63, 66, 70, 74, 78, 83, 87, 92, 97, 102, 109, 115, 121, 128, 
    135, 143, 151, 159, 168, 177, 187, 198, 209, 221, 233, 246, 260, 275, 
    290, 306, 324, 342, 361, 381, 402, 425, 449, 474, 501, 529, 558, 590, 
    623, 658, 695, 734, 775, 818, 864, 913, 964, 1010, 1080, 1140, 1200, 
    1270, 1340, 1410, 1490, 1580, 1670, 1760, 1860, 1960, 2070, 2190, 2310, 
    2440, 2570, 2720, 2870, 3030, 3200, 3380, 3570, 3770, 3980, 4200, 4440, 
    4700, 7960, 5230, 5530, 5840, 6160, 6510, 6870, 7260, 7670, 8100, 8560, 
    9030, 9540, 10000, 10600, 11200, 11900, 12500, 13200, 14000, 14800, 
    15600, 16500, 17400, 18400, 19400, 20500
    };
   
    public static final int[] EQ_HIGH_FREQUENCIES = new int[]
    {
    1280, 1310, 1340, 1370, 1400, 1430, 1460, 1490, 1520, 1560, 1590, 1630, 1660, 1700, 1740, 
    1780, 1820, 1860, 1900, 1940, 1980, 2020, 2070, 2110, 2160, 2210, 2260, 2310, 2360, 2410, 
    2460, 2520, 2570, 2630, 2690, 2750, 2810, 2870, 2930, 3000, 3070, 3130, 3200, 3270, 3340, 
    3420, 3490, 3570, 3650, 3730, 3810, 3900, 3980, 4070, 4160, 4250, 4350, 4440, 4540, 4640, 
    4740, 4850, 4960, 5060, 5180, 5290, 5410, 5530, 5650, 5770, 5900, 6030, 6160, 6300, 6440, 
    6590, 6730, 6880, 7030, 7180, 7340, 7500, 7670, 7840, 8010, 8190, 8370, 8560, 8740, 8930, 
    9130, 9330, 9540, 9750, 9960, 10200, 10400, 10600, 10900, 11100, 11400, 11600, 11900, 
    12100, 12400, 12700, 12900, 13200, 13500, 13800, 14100, 14400, 14800, 15100, 15400, 
    15800, 16100, 16500, 16800, 17200, 17600, 18000, 18400, 18800, 19200, 19600, 20000, 20500
    };

    // Used for Delay (FX) time
    public static final int[] DELAY_TIMES = new int[]
    {
    21, 42, 63, 81, 105, 126, 147, 168, 189, 210, 231, 252, 273, 294, 
    315, 336, 357, 378, 399, 420, 441, 462, 483, 504, 525, 546, 567, 
    588, 609, 630, 651, 672, 693, 714, 735, 756, 777, 798, 819, 840, 
    861, 882, 903, 924, 945, 966, 987, 1010, 1030, 1040, 1070, 1090, 
    1110, 1130, 1160, 1180, 1200, 1220, 1240, 1260, 1280, 1300, 1320, 
    1340, 1370, 1390, 1410, 1430, 1450, 1470, 1490, 1510, 1530, 1550, 
    1580, 1600, 1620, 1640, 1660, 1680, 1700, 1720, 1740, 1760, 1790, 
    1810, 1830, 1850, 1870, 1890, 1910, 1930, 1950, 1970, 2000, 2020, 
    2040, 2060, 2080, 2100, 2120, 2140, 2160, 2180, 2210, 2230, 2250, 
    2270, 2290, 2310, 2330, 2350, 2370, 2390, 2420, 2440, 2460, 2480, 
    2500, 2520, 2540, 2560, 2580, 2600, 2630, 2650, 2670, 2690
    };

    public static final int[] CUTOFF_FREQUENCIES = new int[]
    {
    40, 42, 44, 46, 49, 51, 54, 56, 59, 62, 65, 69, 72, 76, 80, 84, 
    88, 92, 97, 102, 107, 112, 118, 124, 130, 137, 143, 151, 158, 
    166, 175, 183, 193, 202, 212, 223, 234, 246, 259, 272, 285, 
    300, 315, 331, 347, 365, 383, 402, 423, 444, 466, 490, 515, 
    540, 568, 596, 626, 658, 691, 726, 762, 800, 841, 883, 928, 
    974, 1020, 1080, 1130, 1190, 1250, 1310, 1370, 1440, 1520, 
    1600, 1670, 1760, 1850, 1950, 2040, 2140, 2250, 2360, 2480, 
    2600, 2730, 2870, 3020, 3170, 3330, 3490, 3670, 3850, 4050, 
    4250, 4470, 4700, 4930, 5180, 5430, 5710, 6000, 6300, 6620, 
    6950, 7300, 7670, 8060, 8460, 8890, 9330, 9800, 10300, 10800, 
    11400, 11900, 12500, 13200, 13800, 14500, 15200, 16000, 16800, 
    17700, 18600, 19500, 20500
    };
    
    public static final int[] LFO_PHASES = new int[]
    {
    0, 1, 3, 4, 6, 7, 9, 10, 11, 13, 14, 16, 17, 18, 20, 21, 23, 24, 26, 27, 28, 30, 
    31, 33, 34, 35, 37, 38, 40, 41, 43, 44, 45, 47, 48, 50, 51, 52, 54, 55, 57, 58, 
    60, 61, 62, 64, 65, 67, 68, 69, 71, 72, 74, 75, 77, 78, 79, 81, 82, 84, 85, 86, 
    88, 90, 91, 92, 94, 95, 96, 98, 99, 101, 102, 103, 105, 106, 108, 109, 111, 112, 
    113, 115, 116, 118, 119, 120, 122, 123, 125, 126, 128, 129, 130, 132, 133, 135, 
    136, 137, 139, 140, 142, 143, 145, 146, 147, 149, 150, 152, 153, 154, 156, 157, 
    159, 160, 162, 163, 164, 166, 167, 169, 170, 171, 173, 174, 176, 177, 179, 180
    };
    
    // Used for LFO Delay, Attack, Decay, and Release times
    public static final int[] TIMES = new int[] 
    {
    0, 1, 2, 5, 8, 13, 18, 25, 32, 41, 50, 61, 72, 84, 98, 112, 
    127,  114,  161,  180,  200,  219,  241,  263,  286,  311,  336,  362,  389,  418,  447,  477, 
    508,  541,  574,  608,  643,  680,  717,  755,  794,  834,  875,  918,  961, 1005, 1050, 1096, 
    1143, 1191, 1241, 1291, 1342, 1394, 1447, 1500, 1556, 1612, 1669, 1727, 1786, 1846, 1907, 1969, 
    2032, 2096, 2161, 2227, 2294, 2362, 2431, 2500, 2572, 2644, 2717, 2791, 2865, 2941, 3018, 3096, 
    3175, 3255, 3336, 3417, 3500, 3584, 3669, 3755, 3842, 3929, 4018, 4108, 4200, 4290, 4383, 4477, 
    4572, 4667, 4764, 4862, 4961, 5060, 5161, 5263, 5365, 5469, 5574, 5679, 5786, 5893, 6000, 6112, 
    6222, 6334, 6447, 6560, 6675, 6790, 6907, 7024, 7143, 7262, 7383, 7504, 7627, 7751, 7875, 8000
    };
    
    public static final String[] NOTES = new String[] { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
    
    public static final String[] LFO_SHAPE_NAMES = new String[] 
    { 
    "Sine", "Square", "Triangle", "Sample & Hold", "Sawtooth", "Ramp",
    "Sine Ramp", "Round Square", "Sine Saw", "Pulse", "Ramp Square",
    "Filtered Square", "Thin Pulse", "Triangle Square", "Wobble Down",
    "Lagged Saw", "Lagged Square", "Sine Harmonic", "Blended Wave"
    };
    
    public static final int[] LFO_SHAPE_COUNTS = new int[] { 1, 1, 1, 1, 1, 1, 6, 16, 1, 2, 1, 1, 1, 1, 1, 16, 16, 16, 44 };
    public static String[] LFO_SHAPES = null;               // built on the fly by buildLFOShapes()
        
    public static void buildLFOShapes()
        {
        if (LFO_SHAPES != null) return;
                
        LFO_SHAPES = new String[128];
        int c = 0;
        for(int i = 0; i < LFO_SHAPE_COUNTS.length; i++)
            {
            if (LFO_SHAPE_COUNTS[i] == 1)
                {
                LFO_SHAPES[c++] = LFO_SHAPE_NAMES[i];
                }
            else
                {
                for(int j = 0; j < LFO_SHAPE_COUNTS[i]; j++)
                    {
                    LFO_SHAPES[c++] = LFO_SHAPE_NAMES[i] + " " + (j+1);
                    }
                }
            }
        }
    
    int currentPart = 0;   

    public WaldorfKyra()
        {
        buildLFOShapes();        
        
        for(int i = 0; i < parameters.length; i++)
            {
            parametersToIndex.put(parameters[i], Integer.valueOf(i));
            }

        /// SOUND PANEL
                
        JComponent soundPanel = new SynthPanel(this);
        VBox vbox = new VBox();
        
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        hbox.addLast(addGeneral(Style.COLOR_B()));
        vbox.add(hbox);

        vbox.add(addOscillator(1, Style.COLOR_A()));
        vbox.add(addOscillator(2, Style.COLOR_C()));
        
        hbox = new HBox();
        hbox.add(addSuboscillator(1, Style.COLOR_A()));
        hbox.addLast(addSuboscillator(2, Style.COLOR_C()));
        vbox.add(hbox);

        hbox = new HBox();
        hbox.add(addAuxOscillator(Style.COLOR_B()));
        hbox.add(addHypersaw(Style.COLOR_B()));
        hbox.addLast(addDualMode(Style.COLOR_B()));
        vbox.add(hbox);

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("General, Osc", soundPanel);
                

        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        vbox.add(addFilters(Style.COLOR_A()));
        vbox.add(addFilter(1, Style.COLOR_A()));
        vbox.add(addFilter(2, Style.COLOR_A()));
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Filters", soundPanel);
        
        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        
        // Envelope 2 must be made FIRST (see the code for addEnvelope)
        Component env2 = addEnvelope(2, Style.COLOR_A());
        
        vbox.add(addEnvelope(1, Style.COLOR_A()));
        vbox.add(env2);
        vbox.add(addEnvelope(3, Style.COLOR_A()));

        hbox = new HBox();
        hbox.add(addLFO(1, Style.COLOR_B()));
        hbox.addLast(addLFO(2, Style.COLOR_B()));
        vbox.add(hbox);
        
        hbox = new HBox();
        hbox.add(addLFO(3, Style.COLOR_B()));
        hbox.addLast(addArpeggiator(Style.COLOR_C()));
        vbox.add(hbox);
                
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Modulation", soundPanel);

        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        vbox.add(addModMatrix(0, Style.COLOR_A()));
        vbox.add(addModMatrix(1, Style.COLOR_B()));
        vbox.add(addModMatrix(2, Style.COLOR_A()));
        vbox.add(addModMatrix(3, Style.COLOR_B()));
        vbox.add(addModMatrix(4, Style.COLOR_A()));
        vbox.add(addModMatrix(5, Style.COLOR_B()));
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Matrix", soundPanel);


        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        hbox = new HBox();
        vbox.add(addVCA(Style.COLOR_B()));
        hbox.add(addEq(Style.COLOR_A()));
        hbox.addLast(addFormant(Style.COLOR_B()));
        vbox.add(hbox);
        hbox = new HBox();
        hbox.add(addDistortion(Style.COLOR_B()));
        hbox.addLast(addDelay(Style.COLOR_A()));
        vbox.add(hbox);
        vbox.add(addPhaser(Style.COLOR_A()));
        vbox.add(addChorus(Style.COLOR_B()));
        vbox.add(addReverb(Style.COLOR_A()));
        
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("VCA, Effects", soundPanel);

        model.set("bank", 0);
        model.set("number", 0);
        model.set("name", "Untitled");

        loadDefaults();        
        }
                
    public String getDefaultResourceFileName() { return "WaldorfKyra.init"; }
    public String getHTMLResourceFileName() { return "WaldorfKyra.html"; }

    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        JComboBox bank = new JComboBox(writing ? WRITABLE_BANKS : BANKS);
        bank.setEditable(false);
        bank.setMaximumRowCount(32);
        bank.setSelectedIndex(model.get("bank"));
                
        JTextField number = new SelectedTextField("" + model.get("number"), 3);
                
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
                showSimpleError(title, "The Patch Number must be an integer 0 ... 127");
                continue;
                }
            if (n < 0 || n > 127)
                {
                showSimpleError(title, "The Patch Number must be an integer 0 ... 127");
                continue;
                }
                                
            change.set("bank", bank.getSelectedIndex());
            change.set("number", n);
                        
            return true;
            }
        }
                                    
    /** Add the global patch category (name, id, number, etc.) */
    public JComponent addNameGlobal(Color color)
        {
        Category globalCategory = new Category(this, getSynthName(), color);
        //globalCategory.makeUnresettable();
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();

        VBox vbox = new VBox();
        comp = new PatchDisplay(this, 6);
        hbox.add(comp);
 
        params = CATEGORIES;
        comp = new Chooser("Category", this, "category", params);
        hbox.add(comp);

        vbox.add(hbox);
        
        comp = new StringComponent("Patch Name", this, "name", 22, "Name must be up to 22 ASCII characters.")
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
                
        globalCategory.add(vbox, BorderLayout.WEST);
        return globalCategory;
        }

    public static final int MAXIMUM_NAME_LENGTH = 22;
    public String revisePatchName(String name)
        {
        name = super.revisePatchName(name);  // trim first time
        if (name.length() > MAXIMUM_NAME_LENGTH)
            name = name.substring(0, MAXIMUM_NAME_LENGTH);
        
        StringBuffer nameb = new StringBuffer(name);                            
        for(int i = 0 ; i < nameb.length(); i++)
            {
            char c = nameb.charAt(i);
            if (c < ' ' || c > 126)             // It appears that 127 (DEL) is not permitted
                nameb.setCharAt(i, ' ');
            }
        name = nameb.toString();
        return super.revisePatchName(name);  // trim again
        }        

    /** Verify that all the parameters are within valid values, and tweak them if not. */
    public void revise()
        {
        // check the easy stuff -- out of range parameters
        super.revise();
        
        String nm = model.get("name", "Untitled");
        String newnm = revisePatchName(nm);
        if (!nm.equals(newnm))
            model.set("name", newnm);
        }
        
                
    public JComponent addGeneral(Color color)
        {
        Category category = new Category(this, "General", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = KEY_MODES;
        comp = new Chooser("Key Mode", this, "keymode", params);
        vbox.add(comp);
 
        params = VOICE_MODES;
        comp = new Chooser("Voice Mode", this, "voicemode", params);
        vbox.add(comp);

        hbox.add(vbox);

        comp = new LabelledDial("Patch", this, "patchvolume", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Volume");
        hbox.add(comp);

        comp = new LabelledDial("Portamento", this, "portamentotime", color, 0, 127)
            {
            public String map(int value)
                {
                if (value == 0) return "Off";
                else return "" + ((value * 2) / 100) + "." + ((value * 2) % 100); // + "s";
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Time");
        hbox.add(comp);

        comp = new LabelledDial("Bend", this, "bendrange", color, 0, 12);
        ((LabelledDial)comp).addAdditionalLabel("Range");
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addDualMode(Color color)
        {
        Category category = new Category(this, "Dual Mode", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = DUAL_MODES;
        comp = new Chooser("Mode", this, "dualmode", params);
        vbox.add(comp);
 
        hbox.add(vbox);
        vbox = new VBox();

        // Actually only available when hypersaw is OFF and dual mode is STEREO
        comp = new LabelledDial("Detune", this, "dualmodedetune", color, 0, 127);
        hbox.add(comp);

        // Actually only available when dual mode is STEREO
        comp = new LabelledDial("Panorama", this, "panorama", color, 0, 127);
        hbox.add(comp);


        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addAuxOscillator(Color color)
        {
        Category category = new Category(this, "Aux Oscillator", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = AUX_OSCILLATOR_MODES;
        comp = new Chooser("Mode", this, "auxoscillatormode", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Level", this, "auxoscillatorlevel", color, 0, 127);
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addHypersaw(Color color)
        {
        Category category = new Category(this, "Hypersaw", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        comp = new CheckBox("Suboscillator", this, "hypersawsuboscillator");
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Intensity", this, "hypersawintensity", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Spread", this, "hypersawspread", color, 0, 127);
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addArpeggiator(Color color)
        {
        Category category = new Category(this, "Arpeggiator", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = ARP_MODES;
        comp = new Chooser("Mode", this, "arpmode", params);
        vbox.add(comp);

        params = LATCH_MODES;
        comp = new Chooser("Latch Mode", this, "latchmode", params);
        vbox.add(comp);

        comp = new CheckBox("Active", this, "arpenable");
        vbox.add(comp);

        hbox.add(vbox);
        vbox = new VBox();
                
        params = ARP_BEATS;
        comp = new Chooser("Beat", this, "arpbeat", params);
        vbox.add(comp);

        comp = new CheckBox("MIDI Clock", this, "arptimesource");
        vbox.add(comp);

        hbox.add(vbox);

        comp = new LabelledDial("Range", this, "arprange", color, 0, 2) 
            {
            public String map(int val)
                {
                return ARP_OCTAVES[val];
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Pattern", this, "arppattern", color, 0, 127);
        hbox.add(comp);

        // On the Kyra, gate length is 1% ... 100%, which is mapped 0...99.  Docs appear to be wrong.
        comp = new LabelledDial("Gate Length", this, "arpgatelength", color, 0, 99)
            {
            public String map(int value)
                {
                return "" + (value + 1) + "%";
                }
            };
        hbox.add(comp);

        // the tempo goes 58...185, and sysex goes 0...127 but the documentation suggests 0...117 which makes no sense
        comp = new LabelledDial("Tempo", this, "arptempo", color, 0, 127)
        	{
        	public String map(int value)
        		{
        		return "" + (value + 58);
        		}
        	};
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    // Given a wave from 0...4096, returns the wave number in its corresponding group
    int waveToNumber(int wave)
        {
        int total = 0;
        for(int i = 0; i < WAVE_COUNTS.length; i++)
            {
            if (total + WAVE_COUNTS[i] > wave)
                {
                // got it
                return wave - total;
                }
            else total += WAVE_COUNTS[i];
            }
        return -1;
        }

    // Given a wave from 0...4096, returns the corresponding group
    int waveToGroup(int wave)
        {
        int total = 0;
        for(int i = 0; i < WAVE_COUNTS.length; i++)
            {
            if (total + WAVE_COUNTS[i] > wave)
                {
                // got it
                return i;
                }
            else total += WAVE_COUNTS[i];
            }
        return -1;
        }

    // Given a wave group and number, returns the given wave 
    int getWave(int group, int number)
        {
        int total = 0;
        for(int i = 0; i < group; i++)
            {
            total += WAVE_COUNTS[i];
            }
        return total + number;
        }



    static String[] WAVE_NAMES = null;
    public JComponent addOscillator(final int osc, Color color)
        {
        Category category = new Category(this, "Oscillator " + osc, color);
        //        category.makePasteable("osc" + osc);
        category.makePasteable("osc");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
   
        // build wave names if this is the first time
        if (WAVE_NAMES == null)
            {
            WAVE_NAMES = new String[4096];
            for(int i = 0; i < WAVE_NAMES.length; i++)
                {
                WAVE_NAMES[i] = WAVE_GROUPS[waveToGroup(i)] + " (" + waveToNumber(i) + ")";
                }
            }
                        
        comp = new Chooser("Wave", this, "osc" + osc + "wave", WAVE_NAMES);
        vbox.add(comp);
                
        if (osc == 2)
            {
            comp = new CheckBox("Hard Sync", this, "osc" + osc + "hardsync");
            ((CheckBox)comp).addToWidth(2);
            vbox.add(comp);
            }
        
        hbox.add(vbox);

        comp = new LabelledDial("Wave", this, "osc" + osc + "wavetablelevel", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);

        comp = new LabelledDial("Sawtooth", this, "osc" + osc + "sawtoothlevel", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);

        comp = new LabelledDial("Pulse", this, "osc" + osc + "pulselevel", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);

        comp = new LabelledDial("Pulse Width", this, "osc" + osc + "pulsewidth", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Coarse Tune", this, "osc" + osc + "coarsetune", color, 0, 48, 24)
            {
            public boolean isSymmetric() { return true; }
            };
        hbox.add(comp);

        comp = new LabelledDial("LFO 1 to ", this, "osc" + osc + "lfo1topitch", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Pitch");
        hbox.add(comp);

        comp = new LabelledDial("LFO 2 to", this, "osc" + osc + "lfo2topulsewidth", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Pulse Width");
        hbox.add(comp);

        if (osc == 1)
            {
            comp = new LabelledDial("FM Amount", this, "osc" + osc + "fmamount", color, 0, 127);
            hbox.add(comp);
            }

        if (osc == 2)
            {
            comp = new LabelledDial("Detune", this, "osc" + osc + "detune", color, 0, 127, 64);
            hbox.add(comp);
            }

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }



    public JComponent addSuboscillator(int osc, Color color)
        {
        Category category = new Category(this, "Suboscillator " + osc, color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = SUBOSCILLATOR_SHAPES;
        comp = new Chooser("Shape/Octave", this, "osc" + osc + "suboscpulsewidth", params);     // note it says "pulsewidth"
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Level", this, "osc" + osc + "subosclevel", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Detune", this, "osc" + osc + "suboscdetune", color, 0, 127, 64);
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addFilters(Color color)
        {
        Category category = new Category(this, "Filters", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        comp = new CheckBox("Dual Filter", this, "filterconfiguration");
        vbox.add(comp);
        hbox.add(vbox);

        // Actually only available when filter configuration is single (off)
        comp = new LabelledDial("Balance", this, "filterbalance", color, 0, 127, 64);
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;        
        }


    public JComponent addFilter(int filter, Color color)
        {
        Category category = new Category(this, "Filter " + filter, color);
        //        category.makePasteable("filter" + filter);
        category.makePasteable("filter");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = FILTER_MODES;
        comp = new Chooser("Filter Mode", this, "filter" + filter + "mode", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Cutoff", this, "filter" + filter + "frequency", color, 0, 127)
            {
            public String map(int value)
                {
                return "" + CUTOFF_FREQUENCIES[value];
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Frequency");
        hbox.add(comp);

        comp = new LabelledDial("Resonance", this, "filter" + filter + "resonance", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Env 1 to", this, "filter" + filter + "eg1tofreq", color, 0, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Frequency");
        hbox.add(comp);

        comp = new LabelledDial("Env 2 to ", this, "filter" + filter + "eg2tofreq", color, 0, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Frequency");
        hbox.add(comp);

        comp = new LabelledDial("LFO 2 to", this, "filter" + filter + "lfo2tofreq", color, 0, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Frequency");
        hbox.add(comp);

        comp = new LabelledDial("LFO 3 to", this, "filter" + filter + "lfo3tofreq", color, 0, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Frequency");
        hbox.add(comp);

        comp = new LabelledDial("Key Follow", this, "filter" + filter + "keyfollowamt", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Frequency");
        hbox.add(comp);

        comp = new LabelledDial("Key Follow", this, "filter" + filter + "keyfollowkey", color, 0, 127)
            {
            public String map(int value)
                {
                return NOTES[value % 12] + (value / 12 - 1);                    
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Key");
        hbox.add(comp);


        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

        
    //CheckBox bd;
    public JComponent addEnvelope(int env, Color color)
        {
        Category category = new Category(this, "Envelope " + env + (env == 1 ? " (Amplifier)" : (env == 2 ? " (Filter)" : " (Aux)")), color);
        //        category.makePasteable("eg" + env);
        category.makePasteable("eg");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        
        // Bass delay is now deprecated
        /*
          VBox vbox = new VBox();
          if (env == 2 || env == 3)
          {
          comp = new CheckBox("Bass Delay", this, "eg" + env + "bassdelay");
          bd = (CheckBox)comp;
          vbox.add(comp);
          }
          else
          {
          // this will only work if envelope 1 is constructed AFTER envelope 2
          vbox.add(Strut.makeStrut(bd));
          }
          hbox.add(vbox);
        */

        comp = new LabelledDial("Attack", this, "eg" + env + "attack", color, 0, 127)
            {
            public String map(int value)
                {
                return (TIMES[value] / 1000) + "." + (TIMES[value] % 1000); //  + "s";
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Decay", this, "eg" + env + "decay", color, 0, 127)
            {
            public String map(int value)
                {
                return (TIMES[value] / 1000) + "." + (TIMES[value] % 1000); // + "s";
                }
            };
        hbox.add(comp);
                
        comp = new LabelledDial("Sustain", this, "eg" + env + "sustain", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Release", this, "eg" + env + "release", color, 0, 127)
            {
            public String map(int value)
                {
                return (TIMES[value] / 1000) + "." + (TIMES[value] % 1000); // + "s";
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Slope", this, "eg" + env + "slope", color, 0, 127, 64);
        hbox.add(comp);

        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "eg" + env + "attack", "eg" + env + "decay", null, "eg" + env + "release" },
            new String[] { null, null, "eg" + env + "sustain", "eg" + env + "sustain", null },
            new double[] { 0, 0.25/127.0, 0.25 / 127.0,  0.25, 0.25/127.0},
            new double[] { 0, 1.0, 1.0 / 127.0, 1.0/127.0, 0 });

        hbox.addLast(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }



    public JComponent addLFO(int lfo, Color color)
        {
        Category category = new Category(this, "LFO " + lfo, color);
        //        category.makePasteable("lfo" + lfo);
        category.makePasteable("lfo");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = LFO_SHAPES;
        comp = new Chooser("Shape", this, "lfo" + lfo + "shape", params);
        vbox.add(comp);

        params = (lfo == 3 ? LFO3_MODES : LFO_MODES);
        comp = new Chooser("Mode", this, "lfo" + lfo + "mode", params);
        vbox.add(comp);

        params = LFO_TIME_SOURCES;
        comp = new Chooser("Time Source", this, "lfo" + lfo + "timesource", params);            // FIXME: Is this lfosync?
        vbox.add(comp);
        hbox.add(vbox);

        VBox ratebox = new VBox();
        LabelledDial rate = new LabelledDial("Rate", this, "lfo" + lfo + "rate", color, 0, 127)
            {
            public String map(int value)
                {
                value = value + 1;
                if ((lfo == 1 || lfo == 2) && model.get("lfo" + lfo + "range", 0) == 1) // extended range
                    {
                    value *= 4;
                    }
                int pre = (value / 10);
                int post = (value % 10);
                return "" + pre + "." + post; // + " Hz";
                }
            };
        ratebox.add(rate);
        if (lfo == 1 || lfo == 2)
            {
            CheckBox extend = new CheckBox("Extend", this, "lfo" + lfo + "range") // define it first so it's already loaded when we make the dial
                {
                public void update(String key, Model model)
                    {
                    super.update(key, model);
                    rate.repaint();
                    }
                };
            ratebox.add(extend);
            }
        hbox.add(ratebox);


        if (lfo == 1 || lfo == 2)
            {
            comp = new LabelledDial("Delay", this, "lfo" + lfo + "delay", color, 0, 127)
                {
                public String map(int value)
                    {
                    return (TIMES[value] / 1000) + "." + (TIMES[value] % 1000); // + "s";
                    }
                };
            hbox.add(comp);

            comp = new LabelledDial("Phase", this, "lfo" + lfo + "phase", color, 0, 127)
                {
                public String map(int value)
                    {
                    return "" + LFO_PHASES[value];
                    }
                };
            hbox.add(comp);
            }

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    /// Slots start at 0 internally
    public JComponent addModMatrix(int mod, Color color)
        {
        Category category = new Category(this, "Slot " + (mod + 1), color);
        //        category.makePasteable("modmat" + mod);
        category.makePasteable("modmat");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = MOD_SOURCES;
        comp = new Chooser("Source", this, "modmat" + mod + "source", params);
        vbox.add(comp);

        params = MOD_DESTINATIONS;
        comp = new Chooser("Destination 1", this, "modmat" + mod + "destination0", params);
        vbox.add(comp);

        hbox.add(vbox);
        vbox = new VBox();

        params = MOD_DESTINATIONS;
        comp = new Chooser("Destination 2", this, "modmat" + mod + "destination1", params);
        vbox.add(comp);

        params = MOD_DESTINATIONS;
        comp = new Chooser("Destination 3", this, "modmat" + mod + "destination2", params);
        vbox.add(comp);
        hbox.add(vbox);
            
        hbox.add(Strut.makeHorizontalStrut(8));
                        
        comp = new LabelledDial("Amount 1", this, "modmat" + mod + "amount0", color, 0, 127, 64);
        hbox.add(comp);
                        
        hbox.add(Strut.makeHorizontalStrut(8));
        comp = new LabelledDial("Amount 2", this, "modmat" + mod + "amount1", color, 0, 127, 64);
        hbox.add(comp);
                        
        hbox.add(Strut.makeHorizontalStrut(8));
        comp = new LabelledDial("Amount 3", this, "modmat" + mod + "amount2", color, 0, 127, 64);
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }



    public JComponent addPhaser(Color color)
        {
        Category category = new Category(this, "Phaser", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = LFO_SHAPES;
        comp = new Chooser("LFO Shape", this, "fxphaserwaveshape", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Mix", this, "fxphasermix", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Frequency", this, "fxphaserfrequency", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Mod Speed", this, "fxphasermodspeed", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Mod Depth", this, "fxphasermoddepth", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Feedback", this, "fxphaserfeedback", color, 0, 127);
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addDelay(Color color)
        {
        Category category = new Category(this, "Delay", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = DELAY_TYPES;
        comp = new Chooser("Type", this, "fxddltype", params);
        vbox.add(comp);
        hbox.add(vbox);
                
        vbox = new VBox();
        params = DELAY_CLOCK_SOURCES;
        comp = new Chooser("Clock Source", this, "fxddlclocksource", params);
        vbox.add(comp);

        params = DELAY_CLOCK_BEATS;
        comp = new Chooser("Clock Beat", this, "fxddlclockbeat", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Mix", this, "fxddlmix", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Delay Time", this, "fxddldelaytime", color, 0, 127)
            {
            public String map(int value)
                {
                int q = DELAY_TIMES[value];
                if (q < 100)
                    return "0.0" + q;
                else
                    return "" + (q / 1000) + "." + (q % 1000);
                }
            };
        hbox.add(comp);

        //comp = new LabelledDial("Color", this, "fxddlcolor", color, 0, 127);
        //hbox.add(comp);

        comp = new LabelledDial("Feedback", this, "fxddlfeedback", color, 0, 127);
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addChorus(Color color)
        {
        Category category = new Category(this, "Chorus", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        

        VBox vbox = new VBox();
        params = CHORUS_EFFECTS;
        comp = new Chooser("Mode", this, "fxmdfxdelayscale", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Mix", this, "fxmdfxmix", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Delay Time", this, "fxmdfxdelaytime", color, 0, 127);          // "Delay"?
        hbox.add(comp);

        comp = new LabelledDial("Mod Speed", this, "fxmdfxmodspeed", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Mod Depth", this, "fxmdfxmoddepth", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Feedback", this, "fxmdfxfeedback", color, 0, 127);
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }



    public JComponent addReverb(Color color)
        {
        Category category = new Category(this, "Reverb", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        comp = new LabelledDial("Mix", this, "fxreverbmix", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Predelay", this, "fxreverbpredelay", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Time", this, "fxreverbrt60", color, 0, 127);           // reverb time
        hbox.add(comp);

        comp = new LabelledDial("Damping", this, "fxreverbdamping", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Darkness", this, "fxreverbdarkness", color, 0, 127);
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addDistortion(Color color)
        {
        Category category = new Category(this, "Distortion", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = DISTORTION_ALGORITHMS;
        comp = new Chooser("Algorithm", this, "fxdistortiontype", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Mix", this, "fxdistortionmix", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Drive", this, "fxdistortiondrive", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Rolloff", this, "fxdistortionlpf", color, 0, 3)
            {
            public String map(int value)
                {
                return DISTORTION_LPF[value];
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Corner");
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addEq(Color color)
        {
        Category category = new Category(this, "Equalization", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        //// NOTE -- doesn't appear to have mix!

        comp = new LabelledDial("Low Shelf", this, "fxeqlowshelffreq", color, 0, 127)
            {
            public String map(int value)
                {
                int q = EQ_LOW_FREQUENCIES[value];
                return "" + (q / 10) + "." + (q % 10);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Frequency");
        hbox.add(comp);

        comp = new LabelledDial("Low Shelf", this, "fxeqlowshelfgain", color, 0, 127)
            {
            public String map(int value)
                {
                return "" + (value - 64) / 4.0;
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Gain");
        hbox.add(comp);

        comp = new LabelledDial("Mid", this, "fxeqmidfreq", color, 0, 127)
            {
            public String map(int value)
                {
                int q = EQ_MID_FREQUENCIES[value];
                return "" + q;
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Frequency");
        hbox.add(comp);

        comp = new LabelledDial("Mid", this, "fxeqmidq", color, 0, 127)
            {
            public String map(int value)
                {
                int q = EQ_MID_Q[value];
                return "" + (q / 100) + "." + (q % 100);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Q Factor");
        hbox.add(comp);

        comp = new LabelledDial("Mid", this, "fxeqmidgain", color, 0, 127)
            {
            public String map(int value)
                {
                return "" + (value - 64) / 4.0;
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Gain");
        hbox.add(comp);

        comp = new LabelledDial("High Shelf", this, "fxeqhighshelffreq", color, 0, 127)
            {
            public String map(int value)
                {
                int q = EQ_HIGH_FREQUENCIES[value];
                return "" + q;
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Frequency");
        hbox.add(comp);

        comp = new LabelledDial("High Shelf", this, "fxeqhighshelfgain", color, 0, 127)
            {
            public String map(int value)
                {
                return "" + (value - 64) / 4.0;
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Gain");
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addFormant(Color color)
        {
        Category category = new Category(this, "Formant Filter", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        //// NOTE -- doesn't appear to have mix!

        comp = new LabelledDial("Gain", this, "fxformantgain", color, 0, 63)
            {
            public String map(int value)
                {
                return "" + value / 4.0;
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Tune", this, "fxformanttune", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Modulation", this, "fxformantmodspeed", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Speed");
        hbox.add(comp);

        comp = new LabelledDial("Modulation", this, "fxformantmoddepth", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Depth");
        hbox.add(comp);
                
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addVCA(Color color)
        {
        Category category = new Category(this, "Amplifier / Limiter", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = FX_LIMITER_MODES;
        comp = new Chooser("Limiter Curve", this, "fxlimitermode", params);             // this is the same as VCA Saturator Mode
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Pan", this, "vcapan", color, 0, 127)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                if (value < 64) return "< " + (64 - value);
                else if (value > 64) return "" + (value - 64) + " >";
                else return "--";
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("LFO 1 to", this, "vcalfo1toamp", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Amplifier");
        hbox.add(comp);
                
        comp = new LabelledDial("LFO 2 to", this, "vcalfo2topan", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Pan");
        hbox.add(comp);

        comp = new LabelledDial("Velocity", this, "velocityscaling", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Scaling");
        hbox.add(comp);

        comp = new LabelledDial("FX Dynamics", this, "fxdynamicsfinallevel", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Final Level");
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

            
    public JFrame sprout()     
        {
        JFrame frame = super.sprout();
        addKyraMenu();
        return frame;
        }

	JCheckBoxMenuItem[] partMenu = new JCheckBoxMenuItem[8];

    public void addKyraMenu()
        {
        JMenu menu = new JMenu("Kyra");
        menubar.add(menu);
        
        JMenuItem oneMPEMenu = new JMenuItem("Write Patch as Pseudo-MPE");
        oneMPEMenu.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                JTextField number = new SelectedTextField("" + (model.get("number") + 1), 3);
                
                int n = 0;
                String title = "Write Patch as Pseudo-MPE";
                while(true)
                    {
                    boolean result = showMultiOption(WaldorfKyra.this, new String[] { "Patch Number"}, 
                        new JComponent[] { number }, title, "Enter the Multimode patch number.");
                
                    try { n = Integer.parseInt(number.getText()); }
                    catch (NumberFormatException ex)
                        {
                        showSimpleError(title, "The Patch Number must be an integer 0 ... 127");
                        continue;
                        }
                
                    if (n < 1 || n > 128)
                        {
                        showSimpleError(title, "The Patch Number must be an integer 0 ... 127");
                        continue;
                        }

                    if (result) 
                        break;
                    if (!result)
                        return;
                    }           
                 
                boolean send = getSendMIDI();
                setSendMIDI(true);
                tryToSendMIDI(getMPEForPatch(model.get("bank", 0), model.get("number"), n, model.get("name", "")));
                setSendMIDI(send);
                }
            });

        menu.add(oneMPEMenu);
        

        menu.addSeparator();
		ButtonGroup buttonGroup = new ButtonGroup();
        for(int i = 0; i < 8; i++)
        	{
        	final int _i = i;
        	partMenu[i] = new JCheckBoxMenuItem("Send to Part " + (i + 1) + " as Current Patch");
        	if (i == 0) partMenu[i].setSelected(true);
        	partMenu[i].addActionListener(new ActionListener()
        		{
            	public void actionPerformed(ActionEvent e)
                	{
                	currentPart = _i;
                	updateCurrentPart();
                	}
        		});
        	buttonGroup.add(partMenu[i]);
        	menu.add(partMenu[i]);
        	}
        }
      
    void setPart(int part)
    	{
    	partMenu[part].setSelected(true);
		currentPart = part;
		updateCurrentPart();
    	}
    	
	void updateCurrentPart()
		{
		tryToSendSysex(new byte[]
			{
			(byte)0xF0,
			(byte)0x3E,
			(byte)0x22,
			(byte)getID(),
			(byte)0x13,
			0, 0, 0,
			(byte)currentPart,
			(byte)0xF7,
			});
		}
        
    public Object[] getMPEForPatch(int bank, int number, int multinumber, String name)
        {
        WaldorfKyraMulti multi = (WaldorfKyraMulti)
            instantiate(WaldorfKyraMulti.class, true, false, null);
        
        multi.setSendMIDI(false);
        multi.getUndo().setWillPush(false);
        multi.getModel().setUpdateListeners(false);
        multi.getModel().set("number", multinumber);
        for(int j = 1; j <= 8; j++)
            {
            multi.getModel().set("part" + j + "outputchannel", 0);
            multi.getModel().set("part" + j + "midichannel", j - 1);
            multi.getModel().set("part" + j + "volume", 127);
            multi.getModel().set("part" + j + "pan", 64);
            multi.getModel().set("part" + j + "patchbank", bank);
            multi.getModel().set("part" + j + "patchnumber", number);
            multi.getModel().set("part" + j + "transpose", 24);
            multi.getModel().set("part" + j + "detune", 64);
            multi.getModel().set("part" + j + "lowerkeyrange", 1);              // not permitted to be 0
            multi.getModel().set("part" + j + "upperkeyrange", 127);
            multi.getModel().set("part" + j + "rxvolume", 1);
            multi.getModel().set("part" + j + "rxprogram", 1);
            multi.getModel().set("part" + j + "enabledelay", 1);
            multi.getModel().set("part" + j + "enablemdfx", 1);
            multi.getModel().set("part" + j + "enableeq", 1);
            multi.getModel().set("part" + j + "enablereverb", 1);
            }
        multi.getModel().set("name", name);
        return multi.emitAll(null, false, false);
        }
        
            
    public byte getID() 
        { 
        try 
            { 
            byte b = (byte)(Byte.parseByte(tuple.id));
            if (b >= 0) return b;
            }
        catch (NullPointerException e) { } // expected.  Happens when tuple's not built yet
        catch (NumberFormatException e) { Synth.handleException(e); }
        return 17;
        }
        
    public String reviseID(String id)
        {
        try 
            { 
            byte b =(byte)(Byte.parseByte(id)); 
            if (b >= 0) return "" + b;
            } 
        catch (NumberFormatException e) { }             // expected
        return "" + getID();
        }


    public Model getNextPatchLocation(Model model)
        {
        int number = model.get("number");
        int bank = model.get("bank");
        
        number++;
        if (number >= 128)
            {
            number = 0;
            bank++;
            if (bank >= 26)
                bank = 0;
            }
                
        Model newModel = buildModel();
        newModel.set("number", number);
        newModel.set("bank", bank);
        return newModel;
        }

    public String getPatchLocationName(Model model)
        {
        // getPatchLocationName() is called from sprout() as a test to see if we should enable
        // batch downloading.  If we haven't yet created an .init file, then parameters won't exist
        // yet and this method will bomb badly.  So we return null in this case.
        if (!model.exists("number")) return null;
        
        int number = (model.get("number"));
        int bank = (model.get("bank"));
        return BANKS[bank] + " " + ((number > 99 ? "" : (number > 9 ? "0" : "00")) + number);
        }
        


    public void changePatch(Model tempModel)
        {
        // I believe that the Kyra changes *single* patches using Bank Select *LSB* (32) (?!?), 
        // followed by Program Change.  This has to be on a channel that's not the multi channel.
        
        int bank = tempModel.get("bank", 0);
        int number = tempModel.get("number", 0);
        
        tryToSendMIDI(buildCC(getChannelOut(), 32, bank));
        tryToSendMIDI(buildPC(getChannelOut(), number));
        }


    public String getPatchName(Model model) { return model.get("name", "Untitled"); }

    public byte[] requestCurrentDump()
        {
        // The documentation is not clear on this.  But I think it should be:
        
        byte[] data = new byte[9];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x3e;
        data[2] = (byte)0x22;
        data[3] = (byte)getID();
        data[4] = (byte)0x20;                   // Request Patch
        data[5] = (byte)0x01;                   // Current Version
        data[6] = (byte)0x7f;           // Current Bank -- perhaps we should do 0x7E "curent configured bank in the Config menu"?
        data[7] = (byte)0x7f;           // Current Part -- or should we force it to part 0, or maybe we should have a menu option to request different parts?
        data[8] = (byte)0xF7;
        return data;
        }

    public byte[] requestDump(Model tempModel)
        {
        if (tempModel == null)
            tempModel = getModel();

        int NN = tempModel.get("number", 0);
        int BB = tempModel.get("bank", 0);
        
        byte[] data = new byte[9];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x3e;
        data[2] = (byte)0x22;
        data[3] = (byte)getID();
        data[4] = (byte)0x20;                   // Request Patch
        data[5] = (byte)0x01;                   // Current Version
        data[6] = (byte)BB;
        data[7] = (byte)NN;
        data[8] = (byte)0xF7;
        return data;
        }


    public Object[] emitAll(String key)
        {
        int part = currentPart;           // Maybe customize for different parts?
        if (key.equals("-") || key.equals("bank") || key.equals("number"))
            {
            return new Object[0];           // do nothing
            }
        else if (key.equals("name"))
            {
            String val = model.get(key, "") + "                      ";

            // name chars are params 202 through 223
            Object[] nm = new Object[22];
            for(int i = 0; i < nm.length; i++)
                {
                int param = 202 + i;            // start of name
                        
                byte[] data = new byte[10];
                data[0] = (byte)0xF0;
                data[1] = (byte)0x3e;
                data[2] = (byte)0x22;
                data[3] = (byte)getID();
                data[4] = (byte)0x10;                   // Send Parameter to patch Edit Buffer
                data[5] = (byte)0x01;                   // Current Version
                data[6] = (byte)(((param >>> 7) & 0x01) | (part << 4));
                data[7] = (byte)(param & 127);
                data[8] = (byte)((val.charAt(i)) & 127);
                data[9] = (byte)0xF7;
                nm[i] = data;
                }
            return nm;
            }
        else if (key.equals("osc1wave"))
            {
            int param = ((Integer)parametersToIndex.get("osc1wavetablegroup")).intValue();
            int val = waveToGroup(model.get(key, 0));
        
            byte[] data = new byte[10];
            data[0] = (byte)0xF0;
            data[1] = (byte)0x3e;
            data[2] = (byte)0x22;
            data[3] = (byte)getID();
            data[4] = (byte)0x10;                   // Send Parameter to patch Edit Buffer
            data[5] = (byte)0x01;                   // Current Version
            data[6] = (byte)(((param >>> 7) & 0x01) | (part << 4));
            data[7] = (byte)(param & 127);
            data[8] = (byte)val;
            data[9] = (byte)0xF7;
            
            param = ((Integer)parametersToIndex.get("osc1wavetablenumber")).intValue();
            val = waveToNumber(model.get(key, 0));
        
            byte[] data2 = new byte[10];
            data2[0] = (byte)0xF0;
            data2[1] = (byte)0x3e;
            data2[2] = (byte)0x22;
            data2[3] = (byte)getID();
            data2[4] = (byte)0x10;                   // Send Parameter to patch Edit Buffer
            data2[5] = (byte)0x01;                   // Current Version
            data2[6] = (byte)(((param >>> 7) & 0x01) | (part << 4));
            data2[7] = (byte)(param & 127);
            data2[8] = (byte)val;
            data2[9] = (byte)0xF7;
            
            return new Object[] { data, data2 };
            }
        else if (key.equals("osc2wave"))
            {
            int param = ((Integer)parametersToIndex.get("osc2wavetablegroup")).intValue();
            int val = waveToGroup(model.get(key, 0));
        
            byte[] data = new byte[10];
            data[0] = (byte)0xF0;
            data[1] = (byte)0x3e;
            data[2] = (byte)0x22;
            data[3] = (byte)getID();
            data[4] = (byte)0x10;                   // Send Parameter to patch Edit Buffer
            data[5] = (byte)0x01;                   // Current Version
            data[6] = (byte)(((param >>> 7) & 127) | (part << 4));
            data[7] = (byte)(param & 127);
            data[8] = (byte)val;
            data[9] = (byte)0xF7;
            
            param = ((Integer)parametersToIndex.get("osc2wavetablenumber")).intValue();
            val = waveToNumber(model.get(key, 0));
        
            byte[] data2 = new byte[10];
            data2[0] = (byte)0xF0;
            data2[1] = (byte)0x3e;
            data2[2] = (byte)0x22;
            data2[3] = (byte)getID();
            data2[4] = (byte)0x10;                   // Send Parameter to patch Edit Buffer
            data2[5] = (byte)0x01;                   // Current Version
            data2[6] = (byte)(((param >>> 7) & 0x01) | (part << 4));
            data2[7] = (byte)(param & 127);
            data2[8] = (byte)val;
            data2[9] = (byte)0xF7;
            
            return new Object[] { data, data2 };
            }
        else
            {
            int param = ((Integer)parametersToIndex.get(key)).intValue();
            int val = model.get(key, 0);
        
            byte[] data = new byte[10];
            data[0] = (byte)0xF0;
            data[1] = (byte)0x3e;
            data[2] = (byte)0x22;
            data[3] = (byte)getID();
            data[4] = (byte)0x10;                   // Send Parameter to patch Edit Buffer
            data[5] = (byte)0x01;                   // Current Version
            data[6] = (byte)(((param >>> 7) & 0x01) | (part << 4));
            data[7] = (byte)(param & 127);
            data[8] = (byte)val;
            data[9] = (byte)0xF7;
            return new Object[] { data };
            }
        }

    public Object[] emitAll(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        if (tempModel == null)
            tempModel = getModel();
                
        byte BB = (byte) tempModel.get("bank");
        byte NN = (byte) tempModel.get("number");
        if (toWorkingMemory) { BB = 0x7F; NN = 0x7F;}  // current bank, current part?

        byte[] data = new byte[224 + 10];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x3e;
        data[2] = (byte)0x22;
        data[3] = (byte)getID();
        data[4] = (byte)0x00;                   // Send Patch           -- we must always do 0x00, not 0x40
        data[5] = (byte)0x01;                   // Current Version
        data[6] = (byte)BB;
        data[7] = (byte)NN;

        // magic and version
        data[8] = (byte)0x2e;                              // magic number?
        data[9] = (byte)0x00;                              // version number?
        
        // handle non-name parameters
        for(int i = 2; i < 202; i++)
            {
            if (parameters[i].equals("-"))
                {
                data[i + 8] = 0; // do nothing
                }
            else if (parameters[i].equals("osc1wavetablegroup"))
                {
                data[i + 8] = (byte)(waveToGroup(model.get("osc1wave")));
                }
            else if (parameters[i].equals("osc1wavetablenumber"))
                {
                data[i + 8] = (byte)(waveToNumber(model.get("osc1wave")));
                }
            else if (parameters[i].equals("osc2wavetablegroup"))
                {
                data[i + 8] = (byte)(waveToGroup(model.get("osc2wave")));
                }
            else if (parameters[i].equals("osc2wavetablenumber"))
                {
                data[i + 8] = (byte)(waveToNumber(model.get("osc2wave")));
                }
            else
                {
                data[i + 8] = (byte)(model.get(parameters[i], 0));
                }
            }
                
        // handle name
        String val = model.get("name", "") + "                      ";
        for(int i = 0; i < 22; i++)
            {
            data[i + 202 + 8] = (byte)((val.charAt(i)) & 127);
            }
                
        // compute checksum
        int checksum = 0;
        for(int i = 8; i < data.length - 2; i++)
            {
            checksum += data[i];
            }

        data[data.length - 2] = (byte)(checksum & 127);
        data[data.length - 1] = (byte)0xF7;
        
        if (toWorkingMemory)	// need to change current part to working memory
        	{
        	return new Object[] 
        		{
        		new byte[]
        			{
					(byte)0xF0,
					(byte)0x3E,
					(byte)0x22,
					(byte)getID(),
					(byte)0x13,
					0, 0, 0,
					(byte)currentPart,
					(byte)0xF7,
					},
				data
				};
        	}
        else
        	{
	        return new Object[] { data };
	        }
        }

    public void parseParameter(byte[] data)
        {
        if (data.length == 10 &&        // single parameter
            data[0] == (byte)0xF0 &&
            data[1] == 0x3E &&
            data[2] == 0x22 &&
            (data[4] == 0x10 || data[4] == 0x50))
            {
            // extract part
            int part = ((data[6] >>> 4) & 3);
            // extract param
            int param = ((data[6] & 1) << 7) | data[7];
            // extract value
            int val = data[8];
                
            // "name"
            // name chars are params 202 through 223
            if (param >= 202 && param <= 223)
                {
                char c = (char) val;
                int pos = param - 202;
                char[] name = (model.get("name", "") + "                      ").toCharArray();
                name[pos] = c;
                model.set("name", new String(StringUtility.rightTrim(new String(name))));
                }
                
            // "osc1wavetablegroup"
            else if (parameters[param].equals("osc1wavetablegroup"))
                {
                int group = val;
                int number = waveToNumber(model.get("osc1wave"));
                if (WAVE_COUNTS[group] <= number) // no longer legal, need to revise to default value, hopefully osc1wavetablenumber will come along soon
                    number = 0;
                model.set("osc1wave", getWave(group, number));
                }
                
            // "osc1wavetablenumber"
            else if (parameters[param].equals("osc1wavetablenumber"))
                {
                int group = waveToGroup(model.get("osc1wave"));
                int number = val;
                if (WAVE_COUNTS[group] <= number) // no longer legal, need to revise to default value, hope this doesn't happen
                    number = 0;
                model.set("osc1wave", getWave(group, number));
                }
                
            // "osc2wavetablegroup"
            else if (parameters[param].equals("osc2wavetablegroup"))
                {
                int group = val;
                int number = waveToNumber(model.get("osc2wave"));
                if (WAVE_COUNTS[group] <= number) // no longer legal, need to revise to default value, hopefully osc1wavetablenumber will come along soon
                    number = 0;
                model.set("osc2wave", getWave(group, number));
                }
                
            // "osc2wavetablenumber"
            else if (parameters[param].equals("osc2wavetablenumber"))
                {
                int group = waveToGroup(model.get("osc2wave"));
                int number = val;
                if (WAVE_COUNTS[group] <= number) // no longer legal, need to revise to default value, hope this doesn't happen
                    number = 0;
                model.set("osc2wave", getWave(group, number));
                }
                
            else
                {
                model.set(parameters[param], val);
                }
            }
        }

    public int parse(byte[] data, boolean fromFile)
        {
        if (data[6] != 0x7F &&          // Edit Buffer
            data[6] != 0x7E)            // "Current Configured Bank (in the Config Menu)"
            {
            if (data[6] < 26)
                {
                model.set("bank", data[6]);
                }
            else
                {
                System.err.println("WARNING: Invalid Bank number " + data[6] + " in Sysex, changing to 0 (Bank A)");
                model.set("bank", 0);
                }
            model.set("number", data[7]);
            }

        // handle non-name parameters
        int group = 0;
        for(int i = 2; i < 202; i++)
            {
            if (parameters[i].equals("-"))
                {
                // do nothing
                }
            else if (parameters[i].equals("osc1wavetablegroup") || parameters[i].equals("osc2wavetablegroup"))              // group is before number
                {
                group = data[i + 8];
                }
            else if (parameters[i].equals("osc1wavetablenumber"))
                {
                int number = data[i + 8];
                model.set("osc1wave", getWave(group, number));
                }
            else if (parameters[i].equals("osc2wavetablenumber"))
                {
                int number = data[i + 8];
                model.set("osc2wave", getWave(group, number));
                }
            else
                {
                model.set(parameters[i], data[i+8]);
                }
            }
                
        // handle name
        char[] name = new char[22];
        for(int i = 0; i < 22; i++)
            {
            name[i] = (char)(data[i + 202 + 8] & 127);
            }
        model.set("name", new String(StringUtility.rightTrim(new String(name))));

        return PARSE_SUCCEEDED;     
        }

    public static String getSynthName() { return "Waldorf Kyra"; }
    
    //    public boolean getSendsParametersAfterNonMergeParse() { return true; }

    // Change Patch can get stomped if we do a request immediately afterwards
    public int getPauseAfterChangePatch() { return 200; }
    
    // The Kyra doesn't load into memory when you do a write I believe
    public boolean getSendsParametersAfterWrite() { return true; }


    HashMap parametersToIndex = new HashMap();
    public static final String[] parameters = new String[] 
    {
    "magic",
    "version",
    "patchvolume",
    "portamentotime",
    "voicemode",
    "dualmode",
    "bendrange",
    "auxoscillatorlevel",                  // this is noise level and ringmod level
    "panorama",
    "hypersawintensity",
    "hypersawspread",
    "arpenable",
    "arpmode",
    "arprange",
    "arppattern",
    "arpgatelength",
    "arptempo",
    "arptimesource",
    "arpbeat",
    "keymode",
    "osc1coarsetune",
    "osc1sawtoothlevel",
    "osc1pulselevel",
    "osc1wavetablelevel",
    "osc1pulsewidth",
    "osc1subosclevel",
    "osc1suboscpulsewidth",                 // This appears nowhere in the Kyra manual.  It's actually suboscillator shape and octave
    "osc1suboscdetune",
    "osc1wavetablegroup",
    "osc1wavetablenumber",
    "osc1lfo1topitch",
    "osc1lfo2topulsewidth",
    "osc1fmamount",
    "-",                                                                        // spare01
    "auxoscillatormode",
    "osc2coarsetune",
    "osc2detune",
    "osc2sawtoothlevel",
    "osc2pulselevel",
    "osc2wavetablelevel",
    "osc2pulsewidth",
    "osc2subosclevel",
    "osc2suboscpulsewidth",               // This appears nowhere in the Kyra manual.  It's actually suboscillator shape and octave
    "osc2suboscdetune",
    "osc2wavetablegroup",
    "osc2wavetablenumber",
    "osc2lfo1topitch",
    "osc2lfo2topulsewidth",
    "osc2hardsync",
    "hypersawsuboscillator",
    "filter1mode",                       // The docs talk about two magic bits, "bypass" (0x08) and "enable" (0x10), but I am assured this is just internal and they never show up.
    "filterconfiguration",
    "filter1frequency",
    "filter1resonance",
    "filter1eg1tofreq",
    "filter1eg2tofreq",
    "filter1lfo2tofreq",
    "filter1lfo3tofreq",
    "filter1keyfollowkey",
    "filter1keyfollowamt",
    "-",                                   // VCA Drive: this was never implemented                        
    "fxlimitermode",                        // this was originally called PATCH_PARAM_VCA_SATURATOR_MODE 
    "vcapan",
    "vcalfo1toamp",
    "vcalfo2topan",
    "filter2mode",                                                      // The docs talk about two magic bits, "bypass" (0x08) and "enable" (0x10), but I am assured this is just internal and they never show up.
    "filter2frequency",
    "filter2resonance",
    "filter2eg1tofreq",
    "filter2eg2tofreq",
    "eg1attack",
    "eg1decay",
    "eg1sustain",
    "eg1release",
    "-",                                                        //"spare02",
    "eg2attack",
    "eg2decay",
    "eg2sustain",
    "eg2release",
    "-",                                                        // "eg2bassdelay", (deprecated)
    "eg3attack",
    "eg3decay",
    "eg3sustain",
    "eg3release",
    "-",                                                        // "eg3bassdelay", (deprecated)
    "filter2lfo2tofreq",
    "filter2lfo3tofreq",
    "filter2keyfollowkey",
    "filter2keyfollowamt",
    "filterbalance",                // this is normally called FILTER_1_2_BALANCE but the '1' will cause problems with Edisyn's cut/paste 
    "lfo1shape",
    "lfo2shape",
    "lfo3shape",
    "lfo1rate",
    "lfo2rate",
    "lfo3rate",
    "lfo1mode",
    "lfo2mode",
    "lfo3mode",
    "lfo1timesource",
    "lfo2timesource",
    "lfo3timesource",
    "lfo1delay",
    "lfo2delay",
    "velocityscaling",                                  // was spare07
    "modmat0source",
    "modmat0destination0",
    "modmat0amount0",
    "modmat0destination1",
    "modmat0amount1",
    "modmat0destination2",
    "modmat0amount2",
    "modmat1source",
    "modmat1destination0",
    "modmat1amount0",
    "modmat1destination1",
    "modmat1amount1",
    "modmat1destination2",
    "modmat1amount2",
    "modmat2source",
    "modmat2destination0",
    "modmat2amount0",
    "modmat2destination1",
    "modmat2amount1",
    "modmat2destination2",
    "modmat2amount2",
    "modmat3source",
    "modmat3destination0",
    "modmat3amount0",
    "modmat3destination1",
    "modmat3amount1",
    "modmat3destination2",
    "modmat3amount2",
    "modmat4source",
    "modmat4destination0",
    "modmat4amount0",
    "modmat4destination1",
    "modmat4amount1",
    "modmat4destination2",
    "modmat4amount2",
    "modmat5source",
    "modmat5destination0",
    "modmat5amount0",
    "modmat5destination1",
    "modmat5amount1",
    "modmat5destination2",
    "modmat5amount2",
    "eg1slope",
    "eg2slope",
    "eg3slope",
    "fxphaserfrequency",
    "fxphasermodspeed",
    "fxphasermoddepth",
    "fxphaserfeedback",
    "fxphasermix",
    "fxphaserwaveshape",
    "lfo1range",                                        // was "spare12",
    "lfo2range",                                        // was "spare13",
    "lfo1phase",                                        // was "spare14",
    "lfo2phase",                                        // was "spare15",
    "fxddltype",
    "fxddldelaytime",
    "fxddlfeedback",
    "fxddlmix",
    "-",                                                                        // was "fxddlcolor",
    "fxddlclocksource",
    "fxddlclockbeat",
    "-",                                        // "spare16",
    "-",                                        // "spare17",
    "latchmode",                                        // was "spare18",
    "fxmdfxdelaytime",
    "fxmdfxdelayscale",
    "fxmdfxfeedback",
    "fxmdfxmodspeed",
    "fxmdfxmoddepth",
    "fxmdfxmix",
    "-",                                        // "spare19",
    "-",                                        // "spare20",
    "-",                                        // "spare21",
    "dualmodedetune",
    "fxreverbpredelay",
    "fxreverbrt60",                   // This is reverb time
    "fxreverbdamping",
    "fxreverbdarkness",
    "fxreverbmix",
    "fxdistortionmix",
    "fxdistortiondrive",
    "fxdistortiontype",
    "fxdynamicsfinallevel",
    "fxdistortionlpf",               // "rolloff corner"
    "fxeqlowshelffreq",
    "fxeqlowshelfgain",
    "fxeqmidfreq",
    "fxeqmidq",
    "fxeqmidgain",
    "fxeqhighshelffreq",
    "fxeqhighshelfgain",
    "fxformantgain",
    "fxformanttune",
    "fxformantmodspeed",
    "category",
    "fxformantmoddepth",
    "name00",
    "name01",
    "name02",
    "name03",
    "name04",
    "name05",
    "name06",
    "name07",
    "name08",
    "name09",
    "name10",
    "name11",
    "name12",
    "name13",
    "name14",
    "name15",
    "name16",
    "name17",
    "name18",
    "name19",
    "name20",
    "name21"
    };

    
    
    public String[] getPatchNumberNames()  
        { 
        return buildIntegerNames(128, 0);
        }

    public String[] getBankNames() { return BANKS; }

    public boolean getSupportsPatchWrites() { return true; }

    public int getPatchNameLength() { return 22; }

    /** Return true if individual (non-bank) patches on the synthesizer contain location information (bank, number). 
        This will matter when reading patches from disk, rather than loading them from the synth, so as to put them
        in the right place. */
    public boolean getPatchContainsLocation() { return true; }
    }
    
    
    
    
    
    
    
/*** 
     Kyra Single Patch Sysex Format
     [As of Firmware 1.78, though it should roughly work with earlier versions]
     Sean Luke sean@cs.gmu.edu
        
     This is a rough description of the format used by the Edisyn editor.
     Note that there are more commands than this -- it's just what Edisyn uses.
     Waldorf has a somewhat fuller description [as of January 2022]
     
     This description is derived from my code and may have errors.  Let me know what errors
     you find.  My code works well, so if there is a discrepancy between this description
     and the actual code, use the code.

     Be certain to properly set "Receive MIDI Program" to "USB", "MIDI+USB", or "MIDI"
     depending on how you're communicating with the Kyra.
     
     For the Kyra Multimode patch sysex format, see the end of WaldorfKyraMulti.java
                
        
     CHANGE MULTIMODE PATCH
     Do an *LSB* Bank Change (CC = 32) with MIDI Channel = the channel for the part number.
     Do a Program Change with MIDI Channel = the channel for the part number.
        
     NOTE: This channel may not be the Kyra's "Multi Channel"
     NOTE: Banks and Patches start at 0
     NOTE: A patch change requires at least 200ms pause afterwards
        
     REQUEST PATCH DUMP 
     F0
     3E              Waldorf 
     22              Kyra
     [ID]            Synth ID, typically 17 (11 hex)
     20              Request Single patch
     01              Version 1       [current as of September 2021]
     [MSB]           Bank number as follows:
                     (Current Working Patch): MSB = 0x7E
                     (Patch in Memory): MSB = 0 ... 25		(represents banks A ... Z)           
     [LSB]           Patch number as follows:
                     (Current Working Patch): LSB = part, or 0x7F (current part)
                     (Patch in Memory): MSB = patch number          
     F7

     SEND ONE PARAMETER TO EDIT BUFFER
     F0
     3E              Waldorf 
     22              Kyra
     [ID]            Synth ID, typically 17 (11 hex)
     10              Send Single Parameter to Kyra
     01              Version 1       [current as of September 2021]
     [PARAM 1]       Bit 0 is high bit (bit 7) of parameter number, Bits 4..6 are part
     [PARAM 2]       Bits 0 ... 6 of parameter number
     [VALUE]         Parameter Value (see Table 1)
     F7
        
     NOTE: Param1 in java would be     (byte)(((param >>> 7) & 0x01) | (part << 4));
     NOTE: Param2 in java would be     (byte)(param & 127);
    
        
     SINGLE PATCH DUMP        [Received from Kyra]
     F0
     3E              Waldorf 
     22              Kyra
     [ID]            Synth ID, typically 17 (11 hex)
     40              Single Patch Dump from Kyra
     01              Version 1       [current as of September 2021]
     [BANK]          0...25    0x7F indicates current working memory
     [NUMBER]        0...127   For current working memory I set 0x7F
     [DATA]          See Table 0
     [CHECKSUM]      Checksum is sum of bytes in [DATA] mod 128, that is, & 0x7F 
     F7

     SINGLE PATCH DUMP        [Sent to Kyra]
     F0
     3E              Waldorf 
     22              Kyra
     [ID]            Synth ID, typically 17 (11 hex)
     00              Send Single Patch Dump to Kyra
     01              Version 1       [current as of September 2021]
     [BANK]          0...25    0x7F indicates current working memory
     [NUMBER]        0...127   For current working memory I set 0x7F
     [DATA]          See Table 0
     [CHECKSUM]      Checksum is sum of bytes in [DATA] mod 128, that is, & 0x7F 
     F7

     NOTE: writing a patch to storage does not update the edit buffer.
     You'll want to do that manually afterwards.

     REQUEST WHICH PART NUMBER IS CURRENT 
     F0
     3E              Waldorf 
     22              Kyra
     [ID]            Synth ID, typically 17 (11 hex)
     23              Request which Part Number is Current
     00              
     00              
     00              
     00              
     F7
                
     NOTE: you can omit one of the 00 bytes.
     
     RESPONSE AS TO WHICH PART NUMBER IS CURRENT  [Received from Kyra]
     F0
     3E              Waldorf 
     22              Kyra
     [ID]            Synth ID, typically 17 (11 hex)
     43              Request which Part Number is Current
     00              
     00              
     00
     [PART]          Current Part Number
     F7

     NOTE: this message is also automatically sent from the Kyra whenever
     the user changes the current part.

     SET WHICH PART NUMBER IS CURRENT  [Sent to Kyra]
     F0
     3E              Waldorf 
     22              Kyra
     [ID]            Synth ID, typically 17 (11 hex)
     13              Set which Part Number is Current
     00              
     00              
     00
     [PART]          Desired Current Part Number
     F7

                

     TABLE 1.  PARAMETERS AND VALUES
     Num   Name                     Values
     0     magic                    (always 0x2e)
     1     version                  (presently 0x00)
     2     patchvolume              0...127
     3     portamentotime           0...127
     4     voicemode                0 = Wave, 1 = Hypersaw
     5     dualmode                 0 = Mono, 1 = Stereo
     6     bendrange                0...12
     7     auxoscillatorlevel       0...127  (this is noise and/or ringmod level)
     8     panorama                 0...127  (only available when dual mode is stereo)
     9     hypersawintensity        0...127
     10    hypersawspread           0...127
     11    arpenable                0...1
     12    arpmode                  0 = UpDown, 1 = Up, 2 = Down, 3 = Chord, 4 = Random
     13    arprange                 0 = 1 Oct, 1 = 2 Oct, 2 = 3 Oct
     14    arppattern               0...127
     15    arpgatelength            0...99, representing 1% ... 100%  (Waldorf docs appear to be wrong)
     16    arptempo                 0...127  representing 58...185 (Waldorf docs say 0....117 but this appears to be wrong)
     17    arptimesource            0...1        (Arp MIDI clock)
     18    arpbeat                  0 = 1/32, 1 = 1/16 T, 2 = 1/16, 3 = 1/8 T, 4 = 1/16 D, 5 = 1/8, 6 = 1/4 T, 7 = 1/8 D, 8 = 1/4
     19    keymode                  0 = Polyphonic, 1 = Monophonic
     20    osc1coarsetune           0...48 representing -24 ... + 24
     21    osc1sawtoothlevel        0...127
     22    osc1pulselevel           0...127
     23    osc1wavetablelevel       0...127
     24    osc1pulsewidth           0...127
     25    osc1subosclevel          0...127
     26    osc1suboscpulsewidth     0 = Saw, 1 = Square, 2 = Pulse, 3 = Triangle, 4 = Saw / Root, 5 = Square / Root, 6 = Pulse / Root, 7 = Triangle / Root      (this is all labelled 'pulsewidth' in docs but it is not.  It's shape)
     27    osc1suboscdetune         0...127 representing -64 ... + 63
     28    osc1wavetablegroup       See Table 2   (groups start at 0)
     29    osc1wavetablenumber      See Table 2   (waves start at 0)
     30    osc1lfo1topitch          0...127
     31    osc1lfo2topulsewidth     0...127
     32    osc1fmamount             0...127
     33    [Unused]
     34    auxoscillatormode        0 = Noise, 1 = Ring Mod
     35    osc2coarsetune           0...48 representing -24 ... + 24
     36    osc2detune               0...127 representing -64 ... + 64
     37    osc2sawtoothlevel        0...127
     38    osc2pulselevel           0...127
     39    osc2wavetablelevel       0...127
     40    osc2pulsewidth           0...127
     41    osc2subosclevel          0...127
     42    osc2suboscpulsewidth     0 = Saw, 1 = Square, 2 = Pulse, 3 = Triangle, 4 = Saw / Root, 5 = Square / Root, 6 = Pulse / Root, 7 = Triangle / Root      (this is all labelled 'pulsewidth' in docs but it is not.  It's shape)
     43    osc2suboscdetune         0...127 representing -64 ... + 63
     44    osc2wavetablegroup       See Table 2   (groups start at 0)
     45    osc2wavetablenumber      See Table 2   (waves start at 0)
     46    osc2lfo1topitch          0...127
     47    osc2lfo2topulsewidth     0...127
     48    osc2hardsync             0...1
     49    hypersawsuboscillator    0...1
     50    filter1mode              0 = 12db Low Pass, 1 = 24dB Low Pass, 2 = 12dB Band Pass, 3 = 24dB Band Pass, 4 = 12dB High Pass, 5 = 24dB High Pass
     51    filter1frequency         See Table 3
     52    filter1resonance         0...127
     53    filter1eg1tofreq         0...127 representing -64 ... +63
     54    filter1eg2tofreq         0...127 representing -64 ... +63
     55    filter1lfo2tofreq        0...127 representing -64 ... +63
     56    filter1lfo3tofreq        0...127 representing -64 ... +63
     57    filter1keyfollowkey      0...127 representing all MIDI Note Values
     58    filter1keyfollowamt      0...127
     59    [Unused]
     60    fxlimitermode            0 = Light, 1 = Medium, 2 = Heavy   (this was originally called vca saturator mode)
     61    vcapan                   0...127 representing -64 (full left) ... +63 (full right)
     62    vcalfo1toamp             0...127
     63    vcalfo2topan             0...127
     64    filter2mode              0 = 12db Low Pass, 1 = 24dB Low Pass, 2 = 12dB Band Pass, 3 = 24dB Band Pass, 4 = 12dB High Pass, 5 = 24dB High Pass
     65    filter2resonance         0...127
     66    filter2eg1tofreq         0...127 representing -64 ... +63
     67    filter2eg2tofreq         0...127 representing -64 ... +63
     68    eg1attack                See Table 4
     69    eg1decay                 See Table 4
     70    eg1sustain               0...127
     71    eg1release               See Table 4
     72    [Unused]
     73    eg2attack                See Table 4
     74    eg2decay                 See Table 4
     75    eg2sustain               0...127
     76    eg2release               See Table 4
     77    [Unused]
     78    eg3attack                See Table 4
     79    eg3decay                 See Table 4
     80    eg3sustain               0...127
     81    eg3release               See Table 4
     82    [Unused]
     83    filter2lfo2tofreq        0...127 representing -64 ... +63
     84    filter2lfo3tofreq        0...127 representing -64 ... +63
     85    filter2keyfollowkey      0...127 representing all MIDI Note Values
     86    filter2keyfollowamt      0...127
     87    filterbalance            0...127 representing -64 (all filter 1) ... +63 (all filter 2).  Only has effect when filter configuration is single
     88    lfo1shape                See Table 5
     89    lfo2shape                See Table 5
     90    lfo3shape                See Table 4
     91    lfo1rate                 n = 0 ... 127 representing (n + 1)/10 Hz.  If lfo1range is 1 (extended), then represents (n + 1) * 4 /10
     92    lfo2rate                 n = 0 ... 127 representing (n + 1)/10 Hz.  If lfo2range is 1 (extended), then represents (n + 1) * 4 /10
     93    lfo3rate                 n = 0 ... 127 representing (n + 1)/10 Hz.
     94    lfo1mode                 0 = Monophonic, 2 = Polyphonic, 3 = Random Phase, 4 = Dual Antiphase, 5 = Dual Quadrature
     95    lfo2mode                 0 = Monophonic, 2 = Polyphonic, 3 = Random Phase, 4 = Dual Antiphase, 5 = Dual Quadrature
     96    lfo3mode                 0 = Monophonic, 2 = Polyphonic, 3 = Random Phase
     97    lfo1timesource           0 = Internal, 1 = MIDI 1/16, 2 = MIDI 1/8 T, 2 = MIDI 3/32, 3 = MIDI 1/8, 4 = MII 1/4 T, 5 = MIDI 3/16, 6 = MIDI 1/4, 7 = MIDI 1/2 T, 8 = MIDI 3/8, 9 = MIDI 1/1, 10 = MIDI 2/1, 11 = MIDI 2/1
     98    lfo2timesource           0 = Internal, 1 = MIDI 1/16, 2 = MIDI 1/8 T, 2 = MIDI 3/32, 3 = MIDI 1/8, 4 = MII 1/4 T, 5 = MIDI 3/16, 6 = MIDI 1/4, 7 = MIDI 1/2 T, 8 = MIDI 3/8, 9 = MIDI 1/1, 10 = MIDI 2/1, 11 = MIDI 2/1
     99    lfo3timesource           0 = Internal, 1 = MIDI 1/16, 2 = MIDI 1/8 T, 2 = MIDI 3/32, 3 = MIDI 1/8, 4 = MII 1/4 T, 5 = MIDI 3/16, 6 = MIDI 1/4, 7 = MIDI 1/2 T, 8 = MIDI 3/8, 9 = MIDI 1/1, 10 = MIDI 2/1, 11 = MIDI 2/1
     100   lfo1delay                See Table 4
     101   lfo2delay                See Table 4
     102   velocityscaling          0...127
     103   modmat0source            See Table 6
     104   modmat0destination0      See Table 7
     105   modmat0amount0           0...127 representing -64 ... +63
     106   modmat0destination1      See Table 7
     107   modmat0amount1           0...127 representing -64 ... +63
     108   modmat0destination2      See Table 7
     109   modmat0amount2           0...127 representing -64 ... +63
     110   modmat1source            See Table 6
     111   modmat1destination0      See Table 7
     112   modmat1amount0           0...127 representing -64 ... +63
     113   modmat1destination1      See Table 7
     114   modmat1amount1           0...127 representing -64 ... +63
     115   modmat1destination2      See Table 7
     116   modmat1amount2           0...127 representing -64 ... +63
     117   modmat2source            See Table 6
     118   modmat2destination0      See Table 7
     119   modmat2amount0           0...127 representing -64 ... +63
     120   modmat2destination1      See Table 7
     121   modmat2amount1           0...127 representing -64 ... +63
     122   modmat2destination2      See Table 7
     123   modmat2amount2           0...127 representing -64 ... +63
     124   modmat3source            See Table 6
     125   modmat3destination0      See Table 7
     126   modmat3amount0           0...127 representing -64 ... +63
     127   modmat3destination1      See Table 7
     128   modmat3amount1           0...127 representing -64 ... +63
     129   modmat3destination2      See Table 7
     130   modmat3amount2           0...127 representing -64 ... +63
     131   modmat4source            See Table 6
     132   modmat4destination0      See Table 7
     133   modmat4amount0           0...127 representing -64 ... +63
     134   modmat4destination1      See Table 7
     135   modmat4amount1           0...127 representing -64 ... +63
     136   modmat4destination2      See Table 7
     137   modmat4amount2           0...127 representing -64 ... +63
     138   modmat5source            See Table 6
     139   modmat5destination0      See Table 7
     140   modmat5amount0           0...127 representing -64 ... +63
     141   modmat5destination1      See Table 7
     142   modmat5amount1           0...127 representing -64 ... +63
     143   modmat5destination2      See Table 7
     144   modmat5amount2           0...127 representing -64 ... +63
     145   eg1slope                 0...127 representing -64 ... +63
     146   eg2slope                 0...127 representing -64 ... +63
     147   eg3slope                 0...127 representing -64 ... +63
     148   fxphaserfrequency        0...127
     149   fxphasermodspeed         0...127
     150   fxphasermoddepth         0...127
     151   fxphaserfeedback         0...127
     152   fxphasermix              0...127
     153   fxphaserwaveshape        See Table 5
     154   lfo1range                0...1   (LFO 1 Extended Range)
     155   lfo2range                0...1   (LFO 2 Extended Range)
     156   lfo1phase                See Table 8
     157   lfo2phase                See Table 8
     158   fxddltype                0 = Stereo, 1 = Ping Pong   (DDL is Dynamic Delay Line)
     159   fxddldelaytime           See Table 9  (DDL is Dynamic Delay Line)
     160   fxddlfeedback            0...127      (DDL is Dynamic Delay Line)
     161   fxddlmix                 0...127      (DDL is Dynamic Delay Line)
     162   [Unused]
     163   fxddlclocksource         0 = Internal, 1 = MIDI, 2 = LFO 3
     164   fxddlclockbeat           0 = 1/64, 1 = 1/32 T, 2 = 1/32, 3 = 1/16 T, 4 = 1/16, 5 = 1/8 T, 6 = 3/32, 7 = 1/8, 8 = 1/4 T, 9 = 3/16, 10 = 1/4, 11 = 1/2 T, 12 = 3/8, 13 = 1/2
     165   [Unused]
     166   [Unused]
     167   latchmode                0 = Sustain Pedal, 1 = Key Latch
     168   fxmdfxdelaytime          0...127      (mdfx is chorus effects)
     169   fxmdfxdelayscale         0 = Comb (0-10ms), 1 = Flanger (0-20ms), 2 = Chorus (0-40ms), 3 = Doubler (0-160ms)      (mdfx is chorus effects)
     170   fxmdfxfeedback           0...127      (mdfx is chorus effects)
     171   fxmdfxmodspeed           0...127      (mdfx is chorus effects)
     172   fxmdfxmoddepth           0...127      (mdfx is chorus effects)
     173   fxmdfxmix                0...127      (mdfx is chorus effects)
     174   [Unused]
     175   [Unused]
     176   [Unused]
     177   dualmodedetune           0...127      (only available when hypersaw is off and dual mode is stereo)
     178   fxreverbpredelay         0...127
     179   fxreverbrt60             0...127      (this is reverb time)
     180   fxreverbdamping          0...127
     181   fxreverbdarkness         0...127
     182   fxreverbmix              0...127
     183   fxdistortionmix          0...127
     184   fxdistortiondrive        0...127
     185   fxdistortiontype         0 = Soft Rectifier, 1 = Hard Rectifier, 2 = Soft Bitcrusher, 3 = Hard Bitcrusher, 4 = Gnasher
     186   fxdynamicsfinallevel     0...127
     187   fxdistortionlpf          0 = 1 kHz, 1 = 2 kHz, 3 = 4 kHz, 4 = kHz     (this is distortion rolloff corner)
     188   fxeqlowshelffreq         See Table 10
     189   fxeqlowshelfgain         0...127 Representing -16 ... +15.75 in 0.25 increments
     190   fxeqmidfreq              See Table 11
     191   fxeqmidq                 See Table 12
     192   fxeqmidgain              0...127 Representing -16 ... +15.75 in 0.25 increments
     193   fxeqhighshelffreq        See Table 13
     194   fxeqhighshelfgain        0...127 Representing -16 ... +15.75 in 0.25 increments
     195   fxformantgain            0...63 Representing 0 ... +15.75 in 0.25 increments
     196   fxformanttune            0...127
     197   fxformantmodspeed        0...127
     198   category                 See Table 14
     199   fxformantmoddepth        0...127
     200   name00                   ASCII, excepting < 32 and 127
     201   name01                   ASCII, excepting < 32 and 127
     202   name02                   ASCII, excepting < 32 and 127
     203   name03                   ASCII, excepting < 32 and 127
     204   name04                   ASCII, excepting < 32 and 127
     205   name05                   ASCII, excepting < 32 and 127
     206   name06                   ASCII, excepting < 32 and 127
     207   name07                   ASCII, excepting < 32 and 127
     208   name08                   ASCII, excepting < 32 and 127
     209   name09                   ASCII, excepting < 32 and 127
     210   name10                   ASCII, excepting < 32 and 127
     211   name11                   ASCII, excepting < 32 and 127
     212   name12                   ASCII, excepting < 32 and 127
     213   name13                   ASCII, excepting < 32 and 127
     214   name14                   ASCII, excepting < 32 and 127
     215   name15                   ASCII, excepting < 32 and 127
     216   name16                   ASCII, excepting < 32 and 127
     217   name17                   ASCII, excepting < 32 and 127
     218   name18                   ASCII, excepting < 32 and 127
     219   name19                   ASCII, excepting < 32 and 127
     220   name20                   ASCII, excepting < 32 and 127
     221   name21                   ASCII, excepting < 32 and 127
     
     NOTE: These names are taken from Waldorf documents, and so in some cases may not make sense anymore
     
     
     TABLE 2.  WAVE GROUPS
     Val  Num Waves Group Name
     0    100       Construction Set 1
     1    100       Construction Set 2
     2    100       Construction Set 3
     3    100       Construction Set 4
     4    100       Construction Set 5
     5    100       Construction Set 6
     6    100       Construction Set 7
     7    100       Construction Set 8
     8    100       Construction Set 9
     9    100       Construction Set 10
     10   100       Construction Set 11
     11   100       Construction Set 12
     12   100       Construction Set 13
     13   100       Construction Set 14
     14   100       Construction Set 15
     15   100       Construction Set 16
     16   100       Construction Set 17
     17   100       Construction Set 18
     18   100       Construction Set 19
     19   111       Construction Set 20
     20   38        Acoustic Guitar
     21   26        Alto Saxophone
     22   14        Birds
     23   40        Bit Reduced Lo-Fi
     24   73        Blended Waveforms
     25   10        Bright Sawtooth
     26   32        C604
     27   19        Cello
     28   6         Cheezy Strings
     29   25        Clarinet
     30   33        Clavinet
     31   69        Double Bass
     32   45        Distorted
     33   70        Electric Bass
     34   22        Electric Guitar
     35   128       Electric Organ 1
     36   27        Electric Organ 2
     37   73        Electric Piano
     38   16        Flute
     39   122       FM Synth
     40   42        Gapped Sawtooth
     41   44        Granular
     42   50        Hand Drawn
     43   104       Human Voice
     44   85        Linear Waveforms
     45   13        Oboe
     46   128       Osc Chip Lo-Fi 1
     47   30        Osc Chip Lo-Fi 2
     48   44        Overtone
     49   30        Piano
     50   9         Pluck Algorithm
     51   26        Rounded Squares
     52   36        Raw Waveforms
     53   26        Round Squares Sym
     54   26        Round Squares Asym
     55   26        Rounded Saw Sym
     56   8         Sawtooth Bit Reduced
     57   50        Sawtooth
     58   8         Sine Bit Reduced
     59   16        Sine Harmonics
     60   12        Sine
     61   48        Snippets
     62   7         Square Bit Reduced
     63   100       Square
     64   17        Symmetric
     65   4         Tannernin
     66   22        Theremin
     67   8         Triangle Bit Reduced
     68   25        Triangle
     69   128       Video Game 1
     70   11        Video Game 2
     71   14        Violin
     
     NOTE: These waves are directly from the AdventureKid waveform collection
           https://www.adventurekid.se/AKRTfiles/AKWF/view/waveforms_index.html
     
     
     
     TABLE 3.  FILTER FREQUENCIES
     Val   Frequency
     0     40
     1     42
     2     44
     3     46
     4     49
     5     51
     6     54
     7     56
     8     59
     9     62
     10    65
     11    69
     12    72
     13    76
     14    80
     15    84
     16    88
     17    92
     18    97
     19    102
     20    107
     21    112
     22    118
     23    124
     24    130
     25    137
     26    143
     27    151
     28    158
     29    166
     30    175
     31    183
     32    193
     33    202
     34    212
     35    223
     36    234
     37    246
     38    259
     39    272
     40    285
     41    300
     42    315
     43    331
     44    347
     45    365
     46    383
     47    402
     48    423
     49    444
     50    466
     51    490
     52    515
     53    540
     54    568
     55    596
     56    626
     57    658
     58    691
     59    726
     60    762
     61    800
     62    841
     63    883
     64    928
     65    974
     66    1020
     67    1080
     68    1130
     69    1190
     70    1250
     71    1310
     72    1370
     73    1440
     74    1520
     75    1600
     76    1670
     77    1760
     78    1850
     79    1950
     80    2040
     81    2140
     82    2250
     83    2360
     84    2480
     85    2600
     86    2730
     87    2870
     88    3020
     89    3170
     90    3330
     91    3490
     92    3670
     93    3850
     94    4050
     95    4250
     96    4470
     97    4700
     98    4930
     99    5180
     100   5430
     101   5710
     102   6000
     103   6300
     104   6620
     105   6950
     106   7300
     107   7670
     108   8060
     109   8460
     110   8890
     111   9330
     112   9800
     113   10300
     114   10800
     115   11400
     116   11900
     117   12500
     118   13200
     119   13800
     120   14500
     121   15200
     122   16000
     123   16800
     124   17700
     125   18600
     126   19500
     127   20500
     
     
     TABLE 4.  EG ATTACK, DECAY, RELEASE, AND LFO DELAY TIMES
     Val   Time
     0     0
     1     1
     2     2
     3     5
     4     8
     5     13
     6     18
     7     25
     8     32
     9     41
     10    50
     11    61
     12    72
     13    84
     14    98
     15    112
     16    127
     17    114
     18    161
     19    180
     20    200
     21    219
     22    241
     23    263
     24    286
     25    311
     26    336
     27    362
     28    389
     29    418
     30    447
     31    477
     32    508
     33    541
     34    574
     35    608
     36    643
     37    680
     38    717
     39    755
     40    794
     41    834
     42    875
     43    918
     44    961
     45    1005
     46    1050
     47    1096
     48    1143
     49    1191
     50    1241
     51    1291
     52    1342
     53    1394
     54    1447
     55    1500
     56    1556
     57    1612
     58    1669
     59    1727
     60    1786
     61    1846
     62    1907
     63    1969
     64    2032
     65    2096
     66    2161
     67    2227
     68    2294
     69    2362
     70    2431
     71    2500
     72    2572
     73    2644
     74    2717
     75    2791
     76    2865
     77    2941
     78    3018
     79    3096
     80    3175
     81    3255
     82    3336
     83    3417
     84    3500
     85    3584
     86    3669
     87    3755
     88    3842
     89    3929
     90    4018
     91    4108
     92    4200
     93    4290
     94    4383
     95    4477
     96    4572
     97    4667
     98    4764
     99    4862
     100   4961
     101   5060
     102   5161
     103   5263
     104   5365
     105   5469
     106   5574
     107   5679
     108   5786
     109   5893
     110   6000
     111   6112
     112   6222
     113   6334
     114   6447
     115   6560
     116   6675
     117   6790
     118   6907
     119   7024
     120   7143
     121   7262
     122   7383
     123   7504
     124   7627
     125   7751
     126   7875
     127   8000
     
     
     TABLE 5.  LFO WAVE SHAPES AND PHASER SHAPE
     Val   Shape
     0     Sine
     1     Square
     2     Triangle
     3     Sample & Hold
     4     Sawtooth
     5     Ramp
     6     Sine Ramp 1
     7     Sine Ramp 2
     8     Sine Ramp 3
     9     Sine Ramp 4
     10    Sine Ramp 5
     11    Sine Ramp 6
     12    Round Square 1
     13    Round Square 2
     14    Round Square 3
     15    Round Square 4
     16    Round Square 5
     17    Round Square 6
     18    Round Square 7
     19    Round Square 8
     20    Round Square 9
     21    Round Square 10
     22    Round Square 11
     23    Round Square 12
     24    Round Square 13
     25    Round Square 14
     26    Round Square 15
     27    Round Square 16
     28    Sine Saw
     29    Pulse 1
     30    Pulse 2
     31    Ramp Square
     32    Filtered Square
     33    Thin Pulse
     34    Triangle Square
     35    Wobble Down
     36    Lagged Saw 1
     37    Lagged Saw 2
     38    Lagged Saw 3
     39    Lagged Saw 4
     40    Lagged Saw 5
     41    Lagged Saw 6
     42    Lagged Saw 7
     43    Lagged Saw 8
     44    Lagged Saw 9
     45    Lagged Saw 10
     46    Lagged Saw 11
     47    Lagged Saw 12
     48    Lagged Saw 13
     49    Lagged Saw 14
     50    Lagged Saw 15
     51    Lagged Saw 16
     52    Lagged Square 1
     53    Lagged Square 2
     54    Lagged Square 3
     55    Lagged Square 4
     56    Lagged Square 5
     57    Lagged Square 6
     58    Lagged Square 7
     59    Lagged Square 8
     60    Lagged Square 9
     61    Lagged Square 10
     62    Lagged Square 11
     63    Lagged Square 12
     64    Lagged Square 13
     65    Lagged Square 14
     66    Lagged Square 15
     67    Lagged Square 16
     68    Sine Harmonic 1
     69    Sine Harmonic 2
     70    Sine Harmonic 3
     71    Sine Harmonic 4
     72    Sine Harmonic 5
     73    Sine Harmonic 6
     74    Sine Harmonic 7
     75    Sine Harmonic 8
     76    Sine Harmonic 9
     77    Sine Harmonic 10
     78    Sine Harmonic 11
     79    Sine Harmonic 12
     80    Sine Harmonic 13
     81    Sine Harmonic 14
     82    Sine Harmonic 15
     83    Sine Harmonic 16
     84    Blended Wave 1
     85    Blended Wave 2
     86    Blended Wave 3
     87    Blended Wave 4
     88    Blended Wave 5
     89    Blended Wave 6
     90    Blended Wave 7
     91    Blended Wave 8
     92    Blended Wave 9
     93    Blended Wave 10
     94    Blended Wave 11
     95    Blended Wave 12
     96    Blended Wave 13
     97    Blended Wave 14
     98    Blended Wave 15
     99    Blended Wave 16
     100   Blended Wave 17
     101   Blended Wave 18
     102   Blended Wave 19
     103   Blended Wave 20
     104   Blended Wave 21
     105   Blended Wave 22
     106   Blended Wave 23
     107   Blended Wave 24
     108   Blended Wave 25
     109   Blended Wave 26
     110   Blended Wave 27
     111   Blended Wave 28
     112   Blended Wave 29
     113   Blended Wave 30
     114   Blended Wave 31
     115   Blended Wave 32
     116   Blended Wave 33
     117   Blended Wave 34
     118   Blended Wave 35
     119   Blended Wave 36
     120   Blended Wave 37
     121   Blended Wave 38
     122   Blended Wave 39
     123   Blended Wave 40
     124   Blended Wave 41
     125   Blended Wave 42
     126   Blended Wave 43
     127   Blended Wave 44
     
     
     TABLE 6.  MOD SOURCES
     Val   Source
     0     None
     1     Pitch Bend
     2     Channel Pressure
     3     Mod Wheel (CC 1)
     4     Breadth (CC 2)
     5     MIDI CC 3
     6     Foot (CC 4)
     7     Data Entry (CC 6)
     8     Balance (CC 8)
     9     CC 9
     10    Pan (CC 10)
     11    Expression (CC 11)
     12    CC 12
     13    CC 13
     14    CC 14
     15    CC 15
     16    CC 16
     17    CC 17
     18    CC 18
     19    CC 19
     20    Sustain (CC 64)
     21    Envelope 1 [Poly]
     22    Envelope 2 [Poly]
     23    Envelope 3 [Poly]
     24    LFO 1 Unipolar [Poly]
     25    LFO 1 Bipolar [Poly]
     26    LFO 2 Unipolar [Poly]
     27    LFO 2 Bipolar [Poly]
     28    LFO 3 Unipolar [Poly]
     29    LFO 3 Bipolar [Poly]
     30    Note On Velocity [Poly]
     31    Random per Note [Poly]
     32    MIDI Note [Poly]
     33    Polyphonic Pressure [Poly]
     34    Note Off Velocity [Poly]
     
     
     TABLE 7.  MOD DESTINATIONS
     Val   Destination
     0     None
     1     Osc 1 Tune
     2     Osc 1 Detune             (Osc 1 Detune is not available as a direct parameter, only as a destination)
     3     Osc 1 Wave Osc 2 FM
     4     Osc 1 LFO 1 Pitch
     5     Osc 1 LFO 2 Pulsewidth
     6     Osc 1 Sawtooth Level
     7     Osc 1 Wave Level
     8     Osc 1 Pulse Level
     9     Osc 1 Aux Osc Level
     10    Osc 1 Sub Level
     11    Osc 1 Sub Detune
     12    Osc 1 Pulsewidth
     13    Osc 2 Tune
     14    Osc 2 Detune
     15    Osc 2 LFO 1 Pitch
     16    Osc 2 LFO 2 Pulsewidth
     17    Osc 2 Sawtooth Level
     18    Osc 2 Wave Level
     19    Osc 2 Pulse Level
     20    Osc 2 Sub Level
     21    Osc 2 Sub Detune
     22    Osc 2 Pulsewidth
     23    Hypersaw Intensity
     24    Hypersaw Spread
     25    Filter 1/2 Balance
     26    Filter 1 Cutoff
     27    Filter 1 Resonance
     28    Filter 1 Env 1 Cutoff
     29    Filter 1 Env 2 Cutoff
     30    Filter 1 LFO 2 Cutoff
     31    Filter 2 Cutoff
     32    Filter 2 Resonance
     33    Filter 2 Env 1 Cutoff
     34    Filter 2 Env 2 Cutoff
     35    Filter 2 LFO 2 Cutoff
     36    VCA Level (Pre-Effects)
     37    VCA Pan
     38    VCA Stereo Width
     39    VCA LFO 1 > Amp
     40    VCA LFO 2 > Pan
     41    Env 1 Attack
     42    Env 1 Decay
     43    Env 1 Sustain
     44    Env 1 Release
     45    Env 2 Attack
     46    Env 2 Decay
     47    Env 2 Sustain
     48    Env 2 Release
     49    Env 3 Attack
     50    Env 3 Decay
     51    Env 3 Sustain
     52    Env 3 Release
     53    LFO 1 Speed
     54    LFO 1 Delay
     55    LFO 2 Speed
     56    LFO 2 Delay
     57    LFO 3 Speed
     58    DDL Mix
     59    DDL Delay 
     60    DDL Feedback
     61    Phaser Mix
     62    Phaser Feedback
     63    Phaser Modulation Rate
     64    Phaser Modulation Depth
     65    Phaser Frequency
     66    Chorus Mix
     67    Chorus Delay
     68    Chorus Feedback
     69    Chorus Modulation Rate
     70    Chorus Modulation Depth
     71    Reverb Mix
     72    Reverb Time
     73    Reverb Damping
     74    Reverb Darkness
     75    EQ Mid Gain
     76    EQ Mid Frequency
     77    Formant Filter Gain
     78    Formant Filter Tune
     79    Distortion Mix
     80    Distortion Drive
     81    Final Level (Post-Effects)
     82    Dual Detune Amount
     
     
     
     TABLE 8.  LFO PHASES
     Val  Phase
     0    0
     1    1
     2    3
     3    4
     4    6
     5    7
     6    9
     7    10
     8    11
     9    13
     10    14
     11    16
     12    17
     13    18
     14    20
     15    21
     16    23
     17    24
     18    26
     19    27
     20    28
     21    30
     22    31
     23    33
     24    34
     25    35
     26    37
     27    38
     28    40
     29    41
     30    43
     31    44
     32    45
     33    47
     34    48
     35    50
     36    51
     37    52
     38    54
     39    55
     40    57
     41    58
     42    60
     43    61
     44    62
     45    64
     46    65
     47    67
     48    68
     49    69
     50    71
     51    72
     52    74
     53    75
     54    77
     55    78
     56    79
     57    81
     58    82
     59    84
     60    85
     61    86
     62    88
     63    90
     64    91
     65    92
     66    94
     67    95
     68    96
     69    98
     70    99
     71    101
     72    102
     73    103
     74    105
     75    106
     76    108
     77    109
     78    111
     79    112
     80    113
     81    115
     82    116
     83    118
     84    119
     85    120
     86    122
     87    123
     88    125
     89    126
     90    128
     91    129
     92    130
     93    132
     94    133
     95    135
     96    136
     97    137
     98    139
     99    140
     100    142
     101    143
     102    145
     103    146
     104    147
     105    149
     106    150
     107    152
     108    153
     109    154
     110    156
     111    157
     112    159
     113    160
     114    162
     115    163
     116    164
     117    166
     118    167
     119    169
     120    170
     121    171
     122    173
     123    174
     124    176
     125    177
     126    179
     127    180
     
     
     TABLE 9.  DELAY TIMES
     Val  Time
     0    21
     1    42
     2    63
     3    81
     4    105
     5    126
     6    147
     7    168
     8    189
     9    210
     10    231
     11    252
     12    273
     13    294
     14    315
     15    336
     16    357
     17    378
     18    399
     19    420
     20    441
     21    462
     22    483
     23    504
     24    525
     25    546
     26    567
     27    588
     28    609
     29    630
     30    651
     31    672
     32    693
     33    714
     34    735
     35    756
     36    777
     37    798
     38    819
     39    840
     40    861
     41    882
     42    903
     43    924
     44    945
     45    966
     46    987
     47    1010
     48    1030
     49    1040
     50    1070
     51    1090
     52    1110
     53    1130
     54    1160
     55    1180
     56    1200
     57    1220
     58    1240
     59    1260
     60    1280
     61    1300
     62    1320
     63    1340
     64    1370
     65    1390
     66    1410
     67    1430
     68    1450
     69    1470
     70    1490
     71    1510
     72    1530
     73    1550
     74    1580
     75    1600
     76    1620
     77    1640
     78    1660
     79    1680
     80    1700
     81    1720
     82    1740
     83    1760
     84    1790
     85    1810
     86    1830
     87    1850
     88    1870
     89    1890
     90    1910
     91    1930
     92    1950
     93    1970
     94    2000
     95    2020
     96    2040
     97    2060
     98    2080
     99    2100
     100    2120
     101    2140
     102    2160
     103    2180
     104    2210
     105    2230
     106    2250
     107    2270
     108    2290
     109    2310
     110    2330
     111    2350
     112    2370
     113    2390
     114    2420
     115    2440
     116    2460
     117    2480
     118    2500
     119    2520
     120    2540
     121    2560
     122    2580
     123    2600
     124    2630
     125    2650
     126    2670
     127    2690
     
     
     TABLE 10.  EQ LOW FREQUENCIES
     Val  Frequency
     0    20.0
     1    20.5
     2    21.0
     3    21.5
     4    22.0
     5    23.0
     6    23.5
     7    24.0
     8    25.0
     9    25.5
     10    26.5
     11    27.0
     12    28.0
     13    28.5
     14    29.5
     15    30.0
     16    31.0
     17    32.0
     18    33.0
     19    34.0
     20    35.0
     21    36.0
     22    37.0
     23    38.0
     24    39.0
     25    40.0
     26    41.0
     27    42.0
     28    43.0
     29    44.0
     30    45.0
     31    46.0
     32    48.0
     33    49.0
     34    50.0
     35    52.0
     36    53.0
     37    55.0
     38    56.0
     39    58.0
     40    60.0
     41    61.0
     42    63.0
     43    65.0
     44    66.0
     45    68.0
     46    70.0
     47    72.0
     48    74.0
     49    76.0
     50    78.0
     51    80.0
     52    82.0
     53    85.0
     54    87.0
     55    90.0
     56    92.0
     57    95.0
     58    97.0
     59    100.0
     60    103.0
     61    106.0
     62    109.0
     63    111.0
     64    115.0
     65    118.0
     66    121.0
     67    124.0
     68    128.0
     69    131.0
     70    135.0
     71    139.0
     72    143.0
     73    147.0
     74    151.0
     75    155.0
     76    159.0
     77    164.0
     78    168.0
     79    173.0
     80    177.0
     81    182.0
     82    187.0
     83    193.0
     84    198.0
     85    203.0
     86    209.0
     87    215.0
     88    220.0
     89    227.0
     90    233.0
     91    240.0
     92    246.0
     93    253.0
     94    260.0
     95    267.0
     96    275.0
     97    282.0
     98    290.0
     99    298.0
     100    306.0
     101    315.0
     102    324.0
     103    332.0
     104    341.0
     105    351.0
     106    361.0
     107    371.0
     108    381.0
     109    392.0
     110    402.0
     111    414.0
     112    425.0
     113    437.0
     114    449.0
     115    461.0
     116    474.0
     117    487.0
     118    501.0
     119    514.0
     120    529.0
     121    543.0
     122    558.0
     123    574.0
     124    590.0
     125    606.0
     126    623.0
     127    640.0
     
     
     
     TABLE 11.  EQ MID FREQUENCIES
     Val  Frequency
     0    20
     1    21
     2    22
     3    23
     4    25
     5    26
     6    28
     7    29
     8    31
     9    33
     10    35
     11    36
     12    38
     13    41
     14    43
     15    45
     16    48
     17    51
     18    53
     19    56
     20    60
     21    63
     22    66
     23    70
     24    74
     25    78
     26    83
     27    87
     28    92
     29    97
     30    102
     31    109
     32    115
     33    121
     34    128
     35    135
     36    143
     37    151
     38    159
     39    168
     40    177
     41    187
     42    198
     43    209
     44    221
     45    233
     46    246
     47    260
     48    275
     49    290
     50    306
     51    324
     52    342
     53    361
     54    381
     55    402
     56    425
     57    449
     58    474
     59    501
     60    529
     61    558
     62    590
     63    623
     64    658
     65    695
     66    734
     67    775
     68    818
     69    864
     70    913
     71    964
     72    1010
     73    1080
     74    1140
     75    1200
     76    1270
     77    1340
     78    1410
     79    1490
     80    1580
     81    1670
     82    1760
     83    1860
     84    1960
     85    2070
     86    2190
     87    2310
     88    2440
     89    2570
     90    2720
     91    2870
     92    3030
     93    3200
     94    3380
     95    3570
     96    3770
     97    3980
     98    4200
     99    4440
     100    4700
     101    7960
     102    5230
     103    5530
     104    5840
     105    6160
     106    6510
     107    6870
     108    7260
     109    7670
     110    8100
     111    8560
     112    9030
     113    9540
     114    10000
     115    10600
     116    11200
     117    11900
     118    12500
     119    13200
     120    14000
     121    14800
     122    15600
     123    16500
     124    17400
     125    18400
     126    19400
     127    20500
     
     
     
     TABLE 12.  EQ MID Q VALUES
     Val  Q
     0    0.66
     1    0.68
     2    0.69
     3    0.71
     4    0.73
     5    0.75
     6    0.77
     7    0.79
     8    0.80
     9    0.83
     10    0.85
     11    0.87
     12    0.89
     13    0.91
     14    0.93
     15    0.96
     16    0.98
     17    1.01
     18    1.03
     19    1.06
     20    1.08
     21    1.11
     22    1.14
     23    1.17
     24    1.20
     25    1.23
     26    1.26
     27    1.29
     28    1.32
     29    1.35
     30    1.39
     31    1.42
     32    1.46
     33    1.50
     34    1.53
     35    1.57
     36    1.61
     37    1.65
     38    1.69
     39    1.74
     40    1.78
     41    1.82
     42    1.87
     43    1.92
     44    1.97
     45    2.01
     46    2.07
     47    2.12
     48    2.17
     49    2.23
     50    2.28
     51    2.34
     52    2.40
     53    2.46
     54    2.52
     55    2.58
     56    2.65
     57    2.71
     58    2.78
     59    2.85
     60    2.92
     61    3.00
     62    3.07
     63    3.15
     64    3.23
     65    3.31
     66    3.39
     67    3.48
     68    3.56
     69    3.65
     70    3.75
     71    3.84
     72    3.94
     73    4.04
     74    4.14
     75    4.24
     76    4.35
     77    4.46
     78    4.57
     79    4.68
     80    4.80
     81    4.92
     82    5.04
     83    5.17
     84    5.30
     85    5.43
     86    5.57
     87    5.71
     88    5.85
     89    6.00
     90    6.15
     91    6.31
     92    6.46
     93    6.63
     94    6.79
     95    6.96
     96    7.14
     97    7.32
     98    7.50
     99    7.69
     100    7.88
     101    8.08
     102    8.28
     103    8.49
     104    8.71
     105    8.92
     106    9.15
     107    9.38
     108    9.61
     109    9.85
     110    10.10
     111    10.40
     112    10.60
     113    10.90
     114    11.20
     115    11.40
     116    11.70
     117    12.00
     118    12.30
     119    12.60
     120    13.00
     121    13.30
     122    13.60
     123    14.00
     124    14.30
     125    14.70
     126    15.00
     127    15.40
     
     
     
     TABLE 13.  EQ HIGH FREQUENCIES
     Val  Frequency
     0    1280
     1    1310
     2    1340
     3    1370
     4    1400
     5    1430
     6    1460
     7    1490
     8    1520
     9    1560
     10    1590
     11    1630
     12    1660
     13    1700
     14    1740
     15    1780
     16    1820
     17    1860
     18    1900
     19    1940
     20    1980
     21    2020
     22    2070
     23    2110
     24    2160
     25    2210
     26    2260
     27    2310
     28    2360
     29    2410
     30    2460
     31    2520
     32    2570
     33    2630
     34    2690
     35    2750
     36    2810
     37    2870
     38    2930
     39    3000
     40    3070
     41    3130
     42    3200
     43    3270
     44    3340
     45    3420
     46    3490
     47    3570
     48    3650
     49    3730
     50    3810
     51    3900
     52    3980
     53    4070
     54    4160
     55    4250
     56    4350
     57    4440
     58    4540
     59    4640
     60    4740
     61    4850
     62    4960
     63    5060
     64    5180
     65    5290
     66    5410
     67    5530
     68    5650
     69    5770
     70    5900
     71    6030
     72    6160
     73    6300
     74    6440
     75    6590
     76    6730
     77    6880
     78    7030
     79    7180
     80    7340
     81    7500
     82    7670
     83    7840
     84    8010
     85    8190
     86    8370
     87    8560
     88    8740
     89    8930
     90    9130
     91    9330
     92    9540
     93    9750
     94    9960
     95    10200
     96    10400
     97    10600
     98    10900
     99    11100
     100    11400
     101    11600
     102    11900
     103    12100
     104    12400
     105    12700
     106    12900
     107    13200
     108    13500
     109    13800
     110    14100
     111    14400
     112    14800
     113    15100
     114    15400
     115    15800
     116    16100
     117    16500
     118    16800
     119    17200
     120    17600
     121    18000
     122    18400
     123    18800
     124    19200
     125    19600
     126    20000
     127    20500
     
     
     TABLE 14.  CATEGORIES
     Val   Category
     0     Work in Progress
     1     Acid
     2     Ambient
     3     Arpeggio
     4     Bass
     5     EDM & Dubstep
     6     Effects
     7     Electronica
     8     Guitars & Plucked
     9     Leads
     10    Lo-Fi & Distorted
     11    My Favourites
     12    Orchestral
     13    Organs
     14    Pads
     15    Percussion
     16    Pianos
     17    Stabs & Strings
     18    Strings
     19    Trance
*/
