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
    public static final String[] ARP_OCTAVES = { "1 Oct", "2 Oct", "3 Oct" };
    public static final String[] ARP_BEATS = { "1/32", "1/16 T", "1/16", "1/8 T", "1/16 D", "1/8", "1/4 T", "1/8 D", "1/4", };
    public static final String[] SUBOSCILLATOR_SHAPES = { "Saw", "Square", "Pulse", "Triangle", "Saw / Root", "Square / Root", "Pulse / Root", "Triangle / Root" };
    public static final String[] AUX_OSCILLATOR_MODES = { "Noise", "Ring Mod" };
    // FIXME: There's also a "bypass bit" 0x08. There's also an "enable bit" 0x10. They should always be 1.
    public static final String[] FILTER_MODES = { "12dB Low Pass", "24dB Low Pass", "12dB Band Pass", "24db Band Pass", "12dB High Pass", "24dB High Pass" };         
    public static final String[] LFO_MODES = { "Monophonic", "Polyphonic", "Random Phase", "Dual Antiphase", "Dual Quadrature" }; 
    public static final String[] LFO3_MODES = { "Monophonic", "Polyphonic", "Random Phase" };
    public static final String[] LFO_TIME_SOURCES = { "Internal", "MIDI 1/16", "MIDI 1/8 T", "MIDI 3/32", "MIDI 1/8", "MIDI 1/4 T", "MIDI 3/16", "MIDI 1/4", "MIDI 1/2 T", "MIDI 3/8", "MIDI 1/1", "MIDI 2/1", "MIDI 2/1" };
    public static final String[] MOD_SOURCES = { 
        "Pitch Bend", "Channel Pressure", "Mod Wheel (CC 1)", "Breadth (CC 2)", 
        "MIDI CC 3", "Foot (CC 4)", "Data Entry (CC 6)", "Balance (CC 8)", 
        "CC 9", "Pan (CC 10)", "Expression (CC 11)", "CC 12", "CC 13", "CC 14", 
        "CC 15", "CC 16", "CC 17", "CC 18", "CC 19", "Sustain (CC 64)", 
        "Envelope 1 [Poly]", "Envelope 2 [Poly]", "Envelope 3 [Poly]", "LFO 1 Unipolar [Poly]", "LFO 1 Bipolar [Poly]", 
        "LFO 2 Unipolar [Poly]", "LFO 2 Bipolar [Poly]", "LFO 3 Unipolar [Poly]", "LFO 3 Bipolar [Poly]", 
        "Note On Velocity [Poly]", "Random per Note [Poly]", "MIDI Note [Poly]", "Polyphonic Pressure [Poly]", "Note Off Velocity [Poly]" };
    public static final String[] MOD_DESTINATIONS = {
        "Osc 1 Tune", "Osc 1 Detune", "Osc 1 Wave Osc 2 FM", "Osc 1 LFO 1 Pitch", 
        "Osc 1 LFO 2 Pulsewidth", "Osc 1 Sawtooth Level", "Osc 1 Wave Level", 
        "Osc 1 Pulse Level", "Osc 1 Aux Osc Level", "Osc 1 Sub Level", "Osc 1 Sub Detune", 
        "Osc 1 Pulsewidth", "Osc 2 Tune", "Osc 2 Detune", "Osc 2 LFO 1 Pitch", 
        "Osc 2 LFO 2 Pulsewidth", "Osc 2 Sawtooth Level", "Osc 2 Wave Level", 
        "Osc 2 Pulse Level", "Osc 2 Sub Level", "Osc 2 Sub Detune", "Osc 2 Pulsewidth", 
        "Hypersaw Intensity", "Hypersaw Spread", "Filter 1/2 Balance", "Filter 1 Cutoff", 
        "Filter 1 Resonance", "Filter 1 Env 1 Cutoff", "Filter 1 Env 2 Cutoff", 
        "Filter 1 LFO 2 Cutoff", "Filter 2 Resonance", "Filter 2 Env 1 Cutoff", 
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
        vbox.add(addLFO(1, Style.COLOR_B()));
        
        hbox = new HBox();
        hbox.add(addLFO(2, Style.COLOR_B()));
        hbox.addLast(addLFO(3, Style.COLOR_B()));
        vbox.add(hbox);
                
        vbox.add(addArpeggiator(Style.COLOR_C()));

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
                
        JTextField number = new JTextField("" + model.get("number"), 3);
                
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
        globalCategory.makeUnresettable();
                
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

        comp = new LabelledDial("Volume", this, "patchvolume", color, 0, 127);
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

// FIXME: On the Kyra, gate length is 1% ... 100%.  Don't know how to convert this
        comp = new LabelledDial("Gate Length", this, "arpgatelength", color, 0, 127);
        hbox.add(comp);

// FIXME: the tempo goes 58...185 but this makes no sense 
        comp = new LabelledDial("Tempo", this, "arptempo", color, 0, 17);
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
		category.makePasteable("osc" + osc);

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
		category.makePasteable("filter" + filter);

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

        
    CheckBox bd;
    public JComponent addEnvelope(int env, Color color)
        {
        Category category = new Category(this, "Envelope " + env + (env == 1 ? " (Amplifier)" : (env == 2 ? " (Filter)" : " (Aux)")), color);
		category.makePasteable("eg" + env);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
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
		category.makePasteable("lfo" + lfo);

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
        hbox.add(vbox);

        vbox = new VBox();
        params = LFO_TIME_SOURCES;
        comp = new Chooser("Time Source", this, "lfo" + lfo + "timesource", params);            // FIXME: Is this lfosync?
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Rate", this, "lfo" + lfo + "rate", color, 0, 127)
            {
            public String map(int value)
                {
                value = value + 1;
                int pre = (value / 10);
                int post = (value % 10);
                return "" + pre + "." + post + " Hz";
                }
            };
        hbox.add(comp);

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
            }

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


/// Slots start at 0 internally
    public JComponent addModMatrix(int mod, Color color)
        {
        Category category = new Category(this, "Slot " + (mod + 1), color);
		category.makePasteable("modmat" + mod);

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

        vbox = new VBox();
        params = DELAY_CLOCK_SOURCES;
        comp = new Chooser("Clock Source", this, "fxddlclocksource", params);
        vbox.add(comp);

        vbox = new VBox();
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

        comp = new LabelledDial("Color", this, "fxddlcolor", color, 0, 127);
        hbox.add(comp);

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

        comp = new LabelledDial("Time", this, "fxreverbrt60", color, 0, 127);		// reverb time
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

        comp = new LabelledDial("FX Dynamics", this, "fxdynamicsfinallevel", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Final Level");
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
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
        data[7] = (byte)0x7f;           // Current Part -- or should we force it t0 part 0, or maybe we should have a menu option to request different parts?
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
        int part = 0;           // Maybe customize for different parts?
		if (key.equals("-") || key.equals("bank") || key.equals("number"))
			{
			return new Object[0];		// do nothing
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
                data[6] = (byte)(((param >>> 7) & 127) & (part << 4));
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
            data[6] = (byte)(((param >>> 7) & 127) & (part << 4));
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
            data2[6] = (byte)(((param >>> 7) & 127) & (part << 4));
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
            data[6] = (byte)(((param >>> 7) & 127) & (part << 4));
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
            data2[6] = (byte)(((param >>> 7) & 127) & (part << 4));
            data2[7] = (byte)(param & 127);
            data2[8] = (byte)val;
            data2[9] = (byte)0xF7;
            
            return new Object[] { data, data2 };
        	}
        else if (key.equals("filter1mode") ||
            key.equals("filter2mode"))
            {
            // FIXME: There are one or two magic bits:
            // FIXME: There's also a "bypass bit" 0x08. There's also an "enable bit" 0x10. They should always be 1.
            int param = ((Integer)parametersToIndex.get(key)).intValue();
            int val = model.get(key, 0);
        
            byte[] data = new byte[10];
            data[0] = (byte)0xF0;
            data[1] = (byte)0x3e;
            data[2] = (byte)0x22;
            data[3] = (byte)getID();
            data[4] = (byte)0x10;                   // Send Parameter to patch Edit Buffer
            data[5] = (byte)0x01;                   // Current Version
            data[6] = (byte)(((param >>> 7) & 127) & (part << 4));
            data[7] = (byte)(param & 127);
            data[8] = (byte)(val | 0x08 | 0x10);
            data[9] = (byte)0xF7;
            return new Object[] { data };
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
            data[6] = (byte)(((param >>> 7) & 127) & (part << 4));
            data[7] = (byte)(param & 127);
            data[8] = (byte)val;
            data[9] = (byte)0xF7;
            return new Object[] { data };
            }
        }

    public byte[] emit(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        if (tempModel == null)
            tempModel = getModel();
                
        byte BB = (byte) tempModel.get("bank");
        byte NN = (byte) tempModel.get("number");
        if (toWorkingMemory) { BB = 0x7F; NN = 0x7F; }  // current bank, current part?

        byte[] data = new byte[224 + 10];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x3e;
        data[2] = (byte)0x22;
        data[3] = (byte)getID();
        data[4] = (byte)0x00;                   // Send Patch
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
            else if (parameters[i].equals("filter1mode") ||
                parameters[i].equals("filter2mode"))
                {
                // FIXME: There are one or two magic bits:
                // FIXME: There's also a "bypass bit" 0x08. There's also an "enable bit" 0x10. They should always be 1.
                data[i + 8] = (byte)((model.get(parameters[i], 0)) | 0x08 | 0x10);
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
        return data;
        }


    public int parse(byte[] data, boolean fromFile)
        {
        if (data[6] != 0x7F)
            {
            model.set("bank", data[6]);
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
			else if (parameters[i].equals("osc1wavetablegroup") || parameters[i].equals("osc2wavetablegroup"))		// group is before number
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
            else if (parameters[i].equals("filter1mode") ||
                parameters[i].equals("filter2mode"))
                {
                // FIXME: There are one or two magic bits:
                // FIXME: There's also a "bypass bit" 0x08. There's also an "enable bit" 0x10. They should always be 1.
                // Strip out here with an 0x07?
                model.set(parameters[i], data[i+8] & 0x07);
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
    "auxoscillatorlevel",                                       // this is noise level or ringmod level
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
    "-",									// spare01
    "auxoscillatormode",
    "osc2coarsetune",
    "osc2detune",
    "osc2sawtoothlevel",
    "osc2pulselevel",
    "osc2wavetablelevel",
    "osc2pulsewidth",
    "osc2subosclevel",
    "osc2suboscpulsewidth",                                     // This appears nowhere in the Kyra manual.  It's actually suboscillator shape and octave
    "osc2suboscdetune",
    "osc2wavetablegroup",
    "osc2wavetablenumber",
    "osc2lfo1topitch",
    "osc2lfo2topulsewidth",
    "osc2hardsync",
    "hypersawsuboscillator",
    "filter1mode",
    "filterconfiguration",
    "filter1frequency",
    "filter1resonance",
    "filter1eg1tofreq",
    "filter1eg2tofreq",
    "filter1lfo2tofreq",
    "filter1lfo3tofreq",
    "filter1keyfollowkey",
    "filter1keyfollowamt",
    "-",									// VCA Drive: this was never implemented			
    "fxlimitermode",                                            // this is called PATCH_PARAM_VCA_SATURATOR_MODE 
    "vcapan",
    "vcalfo1toamp",
    "vcalfo2topan",
    "filter2mode",
    "filter2frequency",
    "filter2resonance",
    "filter2eg1tofreq",
    "filter2eg2tofreq",
    "eg1attack",
    "eg1decay",
    "eg1sustain",
    "eg1release",
    "-",							//"spare02",
    "eg2attack",
    "eg2decay",
    "eg2sustain",
    "eg2release",
    "eg2bassdelay",
    "eg3attack",
    "eg3decay",
    "eg3sustain",
    "eg3release",
    "eg3bassdelay",
    "filter2lfo2tofreq",
    "filter2lfo3tofreq",
    "filter2keyfollowkey",
    "filter2keyfollowamt",
    "filterbalance",                                            // this is normally called FILTER_1_2_BALANCE but the '1' will cause problems with Edisyn's cut/paste 
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
    "-",												// spare07
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
    "-",					// "spare12",
    "-", 					// "spare13",
    "-", 					// "spare14",
    "-", 					// "spare15",
    "fxddltype",
    "fxddldelaytime",
    "fxddlfeedback",
    "fxddlmix",
    "fxddlcolor",
    "fxddlclocksource",
    "fxddlclockbeat",
    "-", 					// "spare16",
    "-", 					// "spare17",
    "-", 					// "spare18",
    "fxmdfxdelaytime",
    "fxmdfxdelayscale",
    "fxmdfxfeedback",
    "fxmdfxmodspeed",
    "fxmdfxmoddepth",
    "fxmdfxmix",
    "-", 					// "spare19",
    "-", 					// "spare20",
    "-", 					// "spare21",
    "dualmodedetune",
    "fxreverbpredelay",
    "fxreverbrt60",                                                 // This is actually reverb time
    "fxreverbdamping",
    "fxreverbdarkness",
    "fxreverbmix",
    "fxdistortionmix",
    "fxdistortiondrive",
    "fxdistortiontype",
    "fxdynamicsfinallevel",
    "fxdistortionlpf",                                              // "rolloff corner"
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

    
    }
    
