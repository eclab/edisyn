/***
    Copyright 2023 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.asmhydrasynth;

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
   A patch editor for the ASM Hydrasynth.  It can read 1.5.5 and 2.0.0 and can write 2.0.0.
   
   <p>The Hydrasynth is a big and complex machine with over 1000 patch parameters and very little guidance as to
   how they are implemented.  As such it has four major problems which are expressed in this editor.
   
   <ul>
   <li>
   ASM will not release its sysex spec except sometimes under NDA (which I cannot agree to as I'm doing open source).  
   Thus in order to do this editor I had to reverse engineer the entire spec.  It was not fun.  I'm sure there are errors 
   or missing features.  Furthermore, the NRPN spec is incomplete.
   
   <p><li>
   To the best of my knowledge, the Hydrasynth incredibly does not have a sysex command to request the current patch nor
   to send to the current patch.  Furthermore, while most parameters can be sent to the Hydrasynth individually via NRPN,
   a few (notably Macro parameters) cannot; and the Hydrasynth is buggy in accepting them.  The Hydrasynth weirdly doesn't
   have memory for a *current patch* so much as a *current bank*, so you *can*, with some care (it's buggy), upload
   to a *current named patch*.  You'd think this means you could just send to the patch number you want, but it's not
   so easy.  If you send to patch X, then later abandon it and write patch Y, the Hydrasynth writes entire banks at a time, 
   so it will unhelpfully write patch X as well.  That will cause a lot of heartache. As a result Edisyn is forced to treat 
   H 128 as a scratch patch, and send to it instead of the desired patch.
   
   <p><li>
   The Hydrasynth has a LOT of bugs in its NRPN, sysex, and areas.  Working around these bugs is a large task.
   </ul>

   <p><li>
   Windows+MIDI+USB+Java has a host of bugs and the Hydrasynth is right in the thick of it.  In Windows, we must send
   specific sysex messages twice to the Hydrasynth, and the Hydrasynth unfortunately sends us multiple copies of certain
   patch chunk dumps which we have to watch for.  And often when launching Edisyn it won't connect to the Hydrasynth
   unless you quit Edisyn, disconnect USB, reconnect, and relaunch Edisyn. The Mac and Linux don't have these problems.
   </ul>
   
   <p>
   In the edisyn/synth/asmhydrasynth/info directory is my best shot at a reverse engineered sysex and patch encoding spec,
   along with an extensive log documenting the reverse engineering process.  Additionally I have a list of known bugs, a 
   heavily revised NRPN spec with proper display information, and reverse-engineered FX Presets information.
        
   @author Sean Luke
*/

public class ASMHydrasynth extends Synth
    {
    public static final int VERSION_1_5_5 = 0x9B;
    public static final int VERSION_2_0_0 = 0xC8;
    
    public static final String[] BANKS = { "A", "B", "C", "D", "E", "F", "G", "H" };
    public static final String[] OSC_MODES = { "Single", "WaveScan" };
    public static final String[] SUSTAIN_PEDAL_MODES = { "Sustain", "Sostenuto", "Mod Only" };
    public static final String[] LFO_ONE_SHOT_OPTIONS = { "Off", "On", "Step" };
    public static final String[] GLIDE_MODES = { "Off", "Glide", "Glissando" };
    public static final String[] MUTANT_MODES = { "FM-Linear", "WavStack", "Osc Sync", "PW-Orig", "PW-Sqeez", "PW-ASM", "Harmonic", "PhazDiff" };
    public static final String[] MUTANT_SOURCES_FM_LIN = { "Sine", "Triangle", "Osc 1", "Osc 2", "Osc 3", "Ring Mod", "Noise", "Mutant 1", "Mutant 2", "Mutant 3", "Mutant 4", "Mod in 1", "Mod in 2" };
    public static final String[] MUTANT_SOURCES_OSC_SYNC = { "Osc 1", "Osc 2", "Osc 3" };
    public static final String[] RING_MOD_SOURCES = { "Osc 1", "Osc 2", "Osc 3", "Noise", "Mutant 1", "Mutant 2", "Mutant 3", "Mutant 4", "Mod In 1", "Mod In 2" };
    public static final String[] NOISE_TYPES = { "White", "Pink", "Brown", "Red", "Blue", "Violet", "Grey" };
    public static final String[] FILTER_ROUTES = { "Series", "Parallel" };
    public static final String[] FILTER_POSITIONS = { "Pre", "Post" };
    public static final String[] VOWEL_ORDERS = { "AEIOU", "AIUEO", "AUIOE", "AOUIE", "IOUAE", "UEAOI", "IOEAU", "UIEAO" };
    public static final String[] FILTER_1_TYPES = { "LP Ladder 12", "LP Ladder 24", "LP Fat 12", "LP Fat 24", "LP Gate", "LP MS20", "HP MS20", "LP 3-Ler", "BP 3-Ler", "HP 3-Ler",  "Vowel", "LP Stn 12", "BP Stn 12", "HP Stn 12", "LP 1 Pole", "LP 8 Pole" };
    public static final String[] FILTER_2_TYPES = { "LP-BP-HP", "LP-Notch-HP" };
    public static final String[] DELAY_TYPES = { "Basic Mono", "Basic Stereo", "Pan Delay", "LRC Delay", "Reverse" };
    public static final String[] REVERB_TYPES = { "Hall", "Room", "Plate", "Cloud" };
    public static final String[] ENV_TRIG_SOURCES = { "Off", "Note On", "LFO1", "LFO2", "LFO3", "LFO4", "LFO5", "Ribbon On", "Ribbon Rel", "Sustain", "Mod In 1", "Mod In 2" };
    public static final String[] ARP_DIVISIONS = { "1/1", "1/2", "1/4", "1/8", "1/16", "1/32", "1/1 T", "1/2 T", "1/4 T", "1/8 T", "1/16 T", "1/32 T" };
    public static final String[] ARP_MODES = { "Up", "Down", "Up/Down", "Up & Down", "Order", "Random", "Chord", "Phrase" };
    public static final String[] ARP_OCTAVE_MODES = { "Up", "Down", "Up/Down", "Alt", "Alt 2" };
    public static final String[] VIBRATO_RATES_SYNC_ON = { "1/4", "1/8", "1/16", "1/32", "1/1 T", "1/2 T", "1/4 T", "1/8 T", "1/16 T", "1/32 T", "1/1 D", "1/2 D", "1/4 D", "1/8 D", "1/16 D", "1/32 D" };
    public static final String[] ENV_LFO_RATES_SYNC_ON = { "0", "1/64 T", "1/64", "1/32 T", "1/64 D", "1/32", "1/16 T", "1/32 D", "1/16", "1/8 T", "1/16 D", "1/8", "1/4 T", "1/8 D", "1/4", "1/2 T", "1/4 D", "1/2", "1/1 T", "1/2 D", "1/1", "1/1 D", "8", "12", "16", "24", "32", "64" };
    public static final String[] LFO_RATES_SYNC_ON = { "64", "32", "24", "16", "12", "8", "1/1 D", "1/1", "1/2 D", "1/1 T", "1/2", "1/4 D", "1/2 T", "1/4", "1/8 D", "1/4 T", "1/8", "1/16 D", "1/8 T", "1/16", "1/32 D", "1/16 T", "1/32", "1/64 D", "1/32 T", "1/64", "1/64 T" };
    public static final String[] FX_DELAYS_SYNC_ON = { "1/64 T", "1/64", "1/32 T", "1/64 D", "1/32", "1/16 T", "1/32 D", "1/16", "1/8 T", "1/16 D", "1/8", "1/4 T", "1/8 D", "1/4", "1/2 T", "1/4 D", "1/2", "1/1 T", "1/2 D", "1/1", "1/1 D" };
    public static final String[] LFO_WAVES = { "Sine", "Triangle", "Saw Up", "Saw Down", "Square", "Pulse 27%", "Pulse 13%", "S & H", "Noise", "Random", "Step" };
    public static final String[] LFO_TRIG_SYNCS = { "Poly", "Single", "Off" };
    public static final String[] POLYPHONY_TYPES = { "Polyphonic", "Monophonic", "Mono Lo", "Mono Hi", "Unison", "Unison Lo", "Unison Hi" };
    public static final String[] STEREO_MODES = { "Rotate", "Alter", "Random" };
    public static final String[] CATEGORIES = { "Ambient", "Arp", "Bass", "BassLead", "Brass", "Chord", "Drum", "E-piano", "FX", "FxMusic", "Keys", "Lead", "Organ", "Pad", "Perc", "Rhythmic", "Sequence", "Strings", "Vocal" };
    public static final String[] RIBBON_MODES = { "Pitch Bend", "Theremin", "Mod Only" };
    // FIXME: Are these in the right order?
    public static final String[] RIBBON_KEYSPANS = { "2 Oct", "4 Oct", "6 Oct" };
    public static final String[] LO_FI_FILTER_TYPES = { "Thru", "PWBass", "Radio", "Tele", "Clean", "Low" };  
    public static final String[] TREMOLO_LFO_SHAPES = { "Sine", "Square" };
    public static final String[] FX_TYPES = { "Bypass", "Chorus", "Flanger", "Rotary", "Phaser", "Lo-Fi", "Tremolo", "EQ", "Compressor", "Distortion" };
    public static final String[][] FX_PRESETS = { { }, { "Chorus 1", "Chorus 2", "Chorus 3" }, { "Flanger 1", "Flanger 2", "Flanger 3" }, { "Rotary 1", "Rotary 2", "Rotary 3" }, { "Phaser 1", "Phaser 2", "Phaser 3" }, { "Lo-Fi 1", "Lo-Fi 2" }, { "Tremolo 1", "Tremolo 2", "Tremolo 3" },  { "Flat", "Low Boost", "High Boost", "Bass Cut", "Smile", "Lo-Fi", "Warm" }, { }, { "Drive 1", "Drive 2", "Drive 3" } }; 
    public static final String[] LO_FI_SAMPLES = { "44100", "22050", "14700", "11025", "8820", "7350", "6300", "5513", "4900", "4410", "4009", "3675", "3392", "3150", "2940", "2756" }; 
    public static final String[] SIDECHAINS = { "Off", "BPM Duck", "Tap", "Mod In 1", "Mod In 2" };
    public static final char[] INVALID_PATCH_NAME_CHARS = { '"', '*', '\\', '|', '/', '<', '>', '?',  ';', '~' };
    public static final String[] NOTES = { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
    public static final String[] OSC_BIT_REDUCTIONS = { "Off", "16", "12", "10", "9", "8", "7", "6", "5", "4", "3", "2" };
    public static final String[] QUANTIZATIONS = { "Off", "257", "129", "65", "33", "17", "9", "5", "3" };
    public static final String[] SCALES = 
        {       
        "Custom Edit",
        "Chromatic", 
        //// STANDARD SCALES START HERE
        "S Major", "S BeBop Major", "S BeBop", "S Mixolydian", "S Harmonic Major", "S Lydian", "S Lydian Augmented", "S Acoustic", "S Pentatonic Major", "S Locrian Major", "S Prometheus", "S Whole Tone", "S Melodic Minor", "S Half Diminished", "S Aeolian", "S Dorian", "S Harmonic Minor",
        "S Algerian", "S Gypsy", "S Hungarian", "S Ukranian", "S Diminished Whole Tone", "S Locrian", "S Neapolitan Major", "S Neapolitan Minor", "S Phrygian", "S Flamenco", "S Persian", "S Phrygian Dominant", "S Enigmatic", "S Tritone", "S In", "S Insen", "S Augmented", "S Blues", "S Pentatonic Minor", "S Hirajoshi",
        //// MICROTUNE SCALES START HERE
        "M 1/4 Tone", "M 19 Tone", "M 31 Tone", "M Al-Farabi SynChrom", "M Arabic 12-Tone", "M Archytas Chromatic", "M Archytas Enharmonic", "M Belafon Singapore", "M Belafon West Africa", "M Bohlen 11-Tone", "M Chinese 300 B.C.", "M Chinese DiziFlute", "M Crysanthos Byzantine", "M Dekany 1 3 5 11-3", "M Dekany 1 3 5 7 11",
        "M Diaphonic 12-Tone", "M Eikosany 1 3-11", "M Greeg Aeolic", "M H. Partch 43-Note", "M Harmonic A 1-60", "M Hexany 1 3 5 9", "M Hexany 1 3 7 11", "M Hexany 13 11 13", "M Indian Raga", "M Japanese Koto", "M Just Major C", "M Just Minor C", "M Mean Tone C", "M Pelog / Slendro", "M Sk8board 17-65 Tun", "M W. Carlos Harmonic" 
        };

    public static final int[] LFO_FADE_INS_SYNC_ON =            // all the repetition here is just nuts
        {
        0, 22, 45, 68, 91, 115, 138, 161, 184, 207, 231, 254, 277, 300, 324, 346, 
        370, 395, 416, 439, 465, 484, 510, 533, 558, 578, 601, 624, 650, 668, 698, 719, 
        742, 766, 791, 805, 833, 863, 879, 896, 931, 950, 969, 989, 1010, 1032, 1055, 1079, 
        1104, 1131, 1158, 1187, 1218, 1218, 1250, 1284, 1284, 1320, 1357, 1357, 1397, 1397, 1440, 1440, 
        1485, 1485, 1533, 1533, 1533, 1584, 1584, 1638, 1638, 1638, 1697, 1697, 1697, 1760, 1760, 1828, 
        1828, 1828, 1901, 1901, 1901, 1980, 1980, 2066, 2066, 2066, 2160, 2160, 2160, 2263, 2263, 2376, 
        2376, 2376, 2501, 2501, 2501, 2640, 2640, 2796, 2796, 2796, 2971, 2971, 2971, 3169, 3169, 3395, 
        3395, 3395, 3657, 3657, 3657, 3691, 3691, 4322, 4322, 4322, 4754, 4754, 4754, 5282, 5282, 5943,
        };

    public static final String[] MOD_SOURCES = 
        {
        "Off", "Env 1", "Env 2", "Env 3", "Env 4", "Env 5",
        "LFO 1", "LFO 2", "LFO 3", "LFO 4", "LFO 5", "LFO 1 Unipolar", "LFO 2 Unipolar", "LFO 3 Unipolar", "LFO 4 Unipolar", "LFO 5 Unipolar",
        "Chan Aftertouch", "Poly Aftertouch",
        "Keytrack",
        "Note-On Vel", "Note-Off Vel",
        "Pitch Wheel", "Mod Wheel",
        "Ribbon Absolute", "Ribbon Unipolar", "Ribbon Relative",
        "Expression Ped", "CC 64 / Sustain",
        "Mod In 1", "Mod In 2",
        "MPE-X", "MPE-Y Absolute", "MPE-Y Relative",
        "Voice Mod", "Voice Mod+",
        "CC 0", "CC 1", "CC 2", "CC 3", "CC 4", "CC 5", "CC 6", "CC 7", 
        "CC 8", "CC 9", "CC 10", "CC 11", "CC 12", "CC 13", "CC 14", "CC 15", 
        "CC 16", "CC 17", "CC 18", "CC 19", "CC 20", "CC 21", "CC 22", "CC 23", 
        "CC 24", "CC 25", "CC 26", "CC 27", "CC 28", "CC 29", "CC 30", "CC 31", 
        "CC 32", "CC 33", "CC 34", "CC 35", "CC 36", "CC 37", "CC 38", "CC 39", 
        "CC 40", "CC 41", "CC 42", "CC 43", "CC 44", "CC 45", "CC 46", "CC 47", 
        "CC 48", "CC 49", "CC 50", "CC 51", "CC 52", "CC 53", "CC 54", "CC 55", 
        "CC 56", "CC 57", "CC 58", "CC 59", "CC 60", "CC 61", "CC 62", "CC 63", 
        "CC 64 / Sustain", "CC 65", "CC 66", "CC 67", "CC 68", "CC 69", "CC 70", "CC 71", 
        "CC 72", "CC 73", "CC 74", "CC 75", "CC 76", "CC 77", "CC 78", "CC 79", 
        "CC 80", "CC 81", "CC 82", "CC 83", "CC 84", "CC 85", "CC 86", "CC 87", 
        "CC 88", "CC 89", "CC 90", "CC 91", "CC 92", "CC 93", "CC 94", "CC 95", 
        "CC 96", "CC 97", "CC 98", "CC 99", "CC 100", "CC 101", "CC 102", "CC 103", 
        "CC 104", "CC 105", "CC 106", "CC 107", "CC 108", "CC 109", "CC 110", "CC 111", 
        "CC 112", "CC 113", "CC 114", "CC 115", "CC 116", "CC 117", "CC 118", "CC 119", 
        "CC 120", "CC 121", "CC 122", "CC 123", "CC 124", "CC 125", "CC 126", "CC 127" 
        };

    public static final int[] MOD_DESTINATION_CATEGORIES = 
        {
        0,                                              // OFF
        1,1,1,1,1,1,1,1,1,1,                            // Arp
        2,2,2,                                          // OSC 1
        3,3,3,                                          // OSC 2
        4,4,                                            // OSC 3
        5,                                              // ALL OSC
        6,6,6,6,6,6,6,6,6,6,6,6,6,                      // Mutant 1
        7,7,7,7,7,7,7,7,7,7,7,7,7,                                              // Mutant 2
        8,8,8,8,8,8,8,8,8,8,8,8,8,                                              // Mutant 3
        9,9,9,9,9,9,9,9,9,9,9,9,9,                                              // Mutant 4
        10,                                                                                             // Ring Mod
        11,11,11,11,11,11,11,11,11,11,11,11,11,11,11,   // Mixer
        12,12,12,12,12,12,12,                                                   // Filter 1
        13,13,13,13,13,13,                                                              // Filter 2
        14,14,                                                                                  // Amp
        15,15,15,                                                                               // Pre-FX
        16,16,16,16,16,                                                                 // Delay
        17,17,17,17,17,                                                                 // Reverb
        18,18,18,                                                                               // Post-FX
        19,19,19,19,19,                                                                 // ENV 1
        20,20,20,20,20,                                                                 // ENV 2
        21,21,21,21,21,                                                                 // ENV 3
        22,22,22,22,22,                                                                 // ENV 4
        23,23,23,23,23,                                                                 // ENV 5
        24,24,                                                                                  // LFO 1
        25,25,                                                                                  // LFO 2
        26,26,                                                                                  // LFO 3
        27,27,                                                                                  // LFO 4
        28,28,                                                                                  // LFO 5
        29,29,29,29,29,29,29,29,29,29,29,29,29,29,29,29,// Mod Matrix
        29,29,29,29,29,29,29,29,29,29,29,29,29,29,29,29,// Mod Matrix
        30,30,30,30,30,30,30,30,                                                // Macro
        31,31,31,31,31,31,                                                              // Voice
        32,32,                                                                                  // CV
        33,33,33,33,33,33,33,33,33,33,33,33,33,33,33,33,// MIDI
        33,33,33,33,33,33,33,33,33,33,33,33,33,33,33,33,// MIDI
        33,33,33,33,33,33,33,33,33,33,33,33,33,33,33,33,// MIDI
        33,33,33,33,33,33,33,33,33,33,33,33,33,33,33,33,// MIDI
        33,33,33,33,33,33,33,33,33,33,33,33,33,33,33,33,// MIDI
        33,33,33,33,33,33,33,33,33,33,33,33,33,33,33,33,// MIDI
        33,33,33,33,33,33,33,33,33,33,33,33,33,33,33,33,// MIDI
        33,33,33,33,33,33,33,33,33,33,33,33,33,33,33,33,// MIDI
        };
                
    public static final String[] MOD_DESTINATIONS = 
        {
        "Off", 
        "Arp Mode", "Arp Division", "Arp Swing", "Arp Gate", "Arp Octave", "Arp Oct Mode", "Arp Length", "Arp Phrase", "Arp Ratchet", "Arp Chance",
        "Osc 1 Pitch", "Osc 1 Wave", "Osc 1 WaveScan",
        "Osc 2 Pitch", "Osc 2 Wave", "Osc 2 WaveScan",
        "Osc 3 Pitch", "Osc 3 Wave",
        "All Osc Pitch",
        "Mut 1 Ratio", "Mut 1 Depth", "Mut 1 Window", "Mut 1 Feedback", "Mut 1 Dry/Wet", "Mut 1 Warp 1", "Mut 1 Warp 2", "Mut 1 Warp 3", "Mut 1 Warp 4", "Mut 1 Warp 5", "Mut 1 Warp 6", "Mut 1 Warp 7", "Mut 1 Warp 8",
        "Mut 2 Ratio", "Mut 2 Depth", "Mut 2 Window", "Mut 2 Feedback", "Mut 2 Dry/Wet", "Mut 2 Warp 1", "Mut 2 Warp 2", "Mut 2 Warp 3", "Mut 2 Warp 4", "Mut 2 Warp 5", "Mut 2 Warp 6", "Mut 2 Warp 7", "Mut 2 Warp 8",
        "Mut 3 Ratio", "Mut 3 Depth", "Mut 3 Window", "Mut 3 Feedback", "Mut 3 Dry/Wet", "Mut 3 Warp 1", "Mut 3 Warp 2", "Mut 3 Warp 3", "Mut 3 Warp 4", "Mut 3 Warp 5", "Mut 3 Warp 6", "Mut 3 Warp 7", "Mut 3 Warp 8",
        "Mut 4 Ratio", "Mut 4 Depth", "Mut 4 Window", "Mut 4 Feedback", "Mut 4 Dry/Wet", "Mut 4 Warp 1", "Mut 4 Warp 2", "Mut 4 Warp 3", "Mut 4 Warp 4", "Mut 4 Warp 5", "Mut 4 Warp 6", "Mut 4 Warp 7", "Mut 4 Warp 8",
        "Ring Mod Depth",
        "Osc 1 Vol", "Osc 2 Vol", "Osc 3 Vol", "Ring Mod Vol", "Noise Vol", "Osc 1 Pan", "Osc 2 Pan", "Osc 3 Pan", "Ring Mod Pan", "Noise Pan", "Osc 1 F1/2", "Osc 2 F1/2", "Osc 3 F1/2", "Ring Mod F1/2", "Noise F1/2",
        "Filt 1 Cutoff", "Filt 1 Resonance", "Filt 1 Drive", "Filt 1 Control", "Filt 1 Env 1", "Filt 1 LFO 1", "Filt 1 Keytrack",
        "Filt 2 Morph", "Filt 2 Cutoff", "Filt 2 Resonance", "Filt 2 Env 1", "Filt 2 LFO 1", "Filt 2 Keytrack",
        "Amp LFO 2", "Amp Level",
        "Pre-FX Param1", "Pre-FX Param2", "Pre-FX Dry/Wet",
        "Delay Time", "Delay Feedback", "Delay Wet Tone", "Delay Feed Tone", "Delay Dry/Wet",
        "Reverb Time", "Reverb Tone", "Reverb Hi Damp", "Reverb Lo Damp", "Reverb Dry/Wet",
        "Post-FX Param1", "Post-FX Param2", "Post-FX Dry/Wet",
        "Env 1 Attack", "Env 1 Hold", "Env 1 Decay", "Env 1 Sustain", "Env 1 Release",
        "Env 2 Attack", "Env 2 Hold", "Env 2 Decay", "Env 2 Sustain", "Env 2 Release",
        "Env 3 Attack", "Env 3 Hold", "Env 3 Decay", "Env 3 Sustain", "Env 3 Release",
        "Env 4 Attack", "Env 4 Hold", "Env 4 Decay", "Env 4 Sustain", "Env 4 Release",
        "Env 5 Attack", "Env 5 Hold", "Env 5 Decay", "Env 5 Sustain", "Env 5 Release",
        "LFO 1 Rate", "LFO 1 Level",
        "LFO 2 Rate", "LFO 2 Level",
        "LFO 3 Rate", "LFO 3 Level",
        "LFO 4 Rate", "LFO 4 Level",
        "LFO 5 Rate", "LFO 5 Level",
        "Matrix 1 Depth", "Matrix 2 Depth", "Matrix 3 Depth", "Matrix 4 Depth", "Matrix 5 Depth", "Matrix 6 Depth", "Matrix 7 Depth", "Matrix 8 Depth",
        "Matrix 9 Depth", "Matrix 10 Depth", "Matrix 11 Depth", "Matrix 12 Depth", "Matrix 13 Depth", "Matrix 14 Depth", "Matrix 15 Depth", "Matrix 16 Depth", 
        "Matrix 17 Depth", "Matrix 18 Depth", "Matrix 19 Depth", "Matrix 20 Depth", "Matrix 21 Depth", "Matrix 22 Depth", "Matrix 23 Depth", "Matrix 24 Depth", 
        "Matrix 25 Depth", "Matrix 26 Depth", "Matrix 27 Depth", "Matrix 28 Depth", "Matrix 29 Depth", "Matrix 30 Depth", "Matrix 31 Depth", "Matrix 32 Depth",
        "Macro 1", "Macro 2", "Macro 3", "Macro 4", "Macro 5", "Macro 6", "Macro 7", "Macro 8",
        "Detune", "Analog Feel", "Pitch Bend", "Vibrato Amount", "Vibrato Rate", "Glide Time",
        "CV Mod Out 1", "CV Mod Out 2",
        "CC 0", "CC 1", "CC 2", "CC 3", "CC 4", "CC 5", "CC 6", "CC 7", 
        "CC 8", "CC 9", "CC 10", "CC 11", "CC 12", "CC 13", "CC 14", "CC 15", 
        "CC 16", "CC 17", "CC 18", "CC 19", "CC 20", "CC 21", "CC 22", "CC 23", 
        "CC 24", "CC 25", "CC 26", "CC 27", "CC 28", "CC 29", "CC 30", "CC 31", 
        "CC 32", "CC 33", "CC 34", "CC 35", "CC 36", "CC 37", "CC 38", "CC 39", 
        "CC 40", "CC 41", "CC 42", "CC 43", "CC 44", "CC 45", "CC 46", "CC 47", 
        "CC 48", "CC 49", "CC 50", "CC 51", "CC 52", "CC 53", "CC 54", "CC 55", 
        "CC 56", "CC 57", "CC 58", "CC 59", "CC 60", "CC 61", "CC 62", "CC 63", 
        "CC 64", "CC 65", "CC 66", "CC 67", "CC 68", "CC 69", "CC 70", "CC 71", 
        "CC 72", "CC 73", "CC 74", "CC 75", "CC 76", "CC 77", "CC 78", "CC 79", 
        "CC 80", "CC 81", "CC 82", "CC 83", "CC 84", "CC 85", "CC 86", "CC 87", 
        "CC 88", "CC 89", "CC 90", "CC 91", "CC 92", "CC 93", "CC 94", "CC 95", 
        "CC 96", "CC 97", "CC 98", "CC 99", "CC 100", "CC 101", "CC 102", "CC 103", 
        "CC 104", "CC 105", "CC 106", "CC 107", "CC 108", "CC 109", "CC 110", "CC 111", 
        "CC 112", "CC 113", "CC 114", "CC 115", "CC 116", "CC 117", "CC 118", "CC 119", 
        "CC 120", "CC 121", "CC 122", "CC 123", "CC 124", "CC 125", "CC 126", "CC 127" 
        };

    public static final String[] OSC_WAVES = 
        {
        "Sine", "Triangle", "TriSaw", "Saw", "Square", 
        "Pulse 1", "Pulse 2", "Pulse 3", "Pulse 4", "Pulse 5", "Pulse 6", 
        "Horizon 1", "Horizon 2", "Horizon 3", "Horizon 4", "Horizon 5", "Horizon 6", "Horizon 7", "Horizon 8", 
        "SyncLav 1", "SyncLav 2", "SyncLav 3", "SyncLav 4", "SyncLav 5", 
        "Esquire 1", "Esquire 2", "Esquire 3", "Esquire 4", 
        "ChriMey 1", "ChriMey 2", "ChriMey 3", "ChriMey 4", "ChriMey 5", "ChriMey 6", 
        "Spect A 1", "Spect A 2", "Spect A 3", "Spect A 4", "Spect A 5", "Spect A 6", "Spect A 7", 
        "Spect X 1", "Spect X 2", "Spect X 3", "Spect X 4", "Spect X 5", "Spect X 6", "Spect X 7", 
        "Klangor 1", "Klangor 2", "Klangor 3", "Klangor 4", "Klangor 5", 
        "Induct 1", "Induct 2", "Induct 3", 
        "Scorpio 1", "Scorpio 2", "Scorpio 3", "Scorpio 4", "Scorpio 5", "Scorpio 6", "Scorpio 7", "Scorpio 8", "Scorpio 9", 
        "Belview 1", "Belview 2", "Belview 3", "Belview 4", "Belview 5", 
        "Chendom 1", "Chendom 2", "Chendom 3", "Chendom 4", "Chendom 5", "Chendom 6", "Chendom 7", "Chendom 8", 
        "Glefan 1", "Glefan 2", "Glefan 3", "Glefan 4", "Glefan 5", "Glefan 6", "Glefan 7", 
        "Sqarbel 1", "Sqarbel 2", 
        "Obob 1", "Obob 2", "Obob 3", 
        "Ingvay 1", "Ingvay 2", "Ingvay 3", 
        "Particl 1", "Particl 2", "Particl 3", 
        "Vokz 1", "Vokz 2", "Vokz 3", "Vokz 4", "Vokz 5", "Vokz 6", 
        "Flux 1", "Flux 2", "Flux 3", "Flux 4", "Flux 5", 
        "Alweg 1", "Alweg 2", "Alweg 3", "Alweg 4", "Alweg 5", "Alweg 6", "Alweg 7", "Alweg 8", 
        "Tronic 1", "Tronic 2", "Tronic 3", "Tronic 4", "Tronic 5", "Tronic 6", 
        "Duotone 1", "Duotone 2", "Duotone 3", "Duotone 4", "Duotone 5", "Duotone 6", 
        "Bobanab 1", "Bobanab 2", "Bobanab 3", "Bobanab 4", 
        "Melotic 1", "Melotic 2", "Melotic 3", "Melotic 4", "Melotic 5", "Melotic 6", "Melotic 7", 
        "Cluster 1", "Cluster 2", "Cluster 3", "Cluster 4", "Cluster 5", "Cluster 6", "Cluster 7", "Cluster 8", 
        "Micoten 1", "Micoten 2", "Micoten 3", "Micoten 4", "Micoten 5", 
        "Orland 1", "Orland 2", "Orland 3", "Orland 4", "Orland 5", "Orland 6", "Orland 7", "Orland 8", 
        "Neuton 1", "Neuton 2", "Neuton 3", "Neuton 4", "Neuton 5", "Neuton 6", "Neuton 7", 
        "Xfer 1", "Xfer 2", "Xfer 3", "Xfer 4", "Xfer 5", "Xfer 6", "Xfer 7", 
        "Resyn 1", "Resyn 2", "Resyn 3", "Resyn 4", 
        "Sano 1", "Sano 2", "Sano 3", "Sano 4", 
        "SquRoo 1", "SquRoo 2", "SquRoo 3", "SquRoo 4", "SquRoo 5", "SquRoo 6", "SquRoo 7", "SquRoo 8", "SquRoo 9", "SquRoo 10", "SquRoo 11", "SquRoo 12", "SquRoo 13", "SquRoo 14", "SquRoo 15", 
        "Harmon 1", "Harmon 2", "Harmon 3", "Harmon 4", "Harmon 5", "Harmon 6", "Harmon 7", "Harmon 8", "Harmon 9", "Harmon 10", "Harmon 11", "Harmon 12", 
        "Harmon 13", "Harmon 14", "Harmon 15", "Harmon 16", "Harmon 17", "Harmon 18", "Harmon 19", "Harmon 20", "Harmon 21", "Harmon 22", "Harmon 23" 
        };

    public static final String[] OSC_WAVES_OFF_SILENCE =                    // ugh, repetition just because OSC_WAVES is missing off and silence
        {
        "Off", "Silence", "Sine", "Triangle", "TriSaw", "Saw", "Square", 
        "Pulse 1", "Pulse 2", "Pulse 3", "Pulse 4", "Pulse 5", "Pulse 6", 
        "Horizon 1", "Horizon 2", "Horizon 3", "Horizon 4", "Horizon 5", "Horizon 6", "Horizon 7", "Horizon 8", 
        "SyncLav 1", "SyncLav 2", "SyncLav 3", "SyncLav 4", "SyncLav 5", 
        "Esquire 1", "Esquire 2", "Esquire 3", "Esquire 4", 
        "ChriMey 1", "ChriMey 2", "ChriMey 3", "ChriMey 4", "ChriMey 5", "ChriMey 6", 
        "Spect A 1", "Spect A 2", "Spect A 3", "Spect A 4", "Spect A 5", "Spect A 6", "Spect A 7", 
        "Spect X 1", "Spect X 2", "Spect X 3", "Spect X 4", "Spect X 5", "Spect X 6", "Spect X 7", 
        "Klangor 1", "Klangor 2", "Klangor 3", "Klangor 4", "Klangor 5", 
        "Induct 1", "Induct 2", "Induct 3", 
        "Scorpio 1", "Scorpio 2", "Scorpio 3", "Scorpio 4", "Scorpio 5", "Scorpio 6", "Scorpio 7", "Scorpio 8", "Scorpio 9", 
        "Belview 1", "Belview 2", "Belview 3", "Belview 4", "Belview 5", 
        "Chendom 1", "Chendom 2", "Chendom 3", "Chendom 4", "Chendom 5", "Chendom 6", "Chendom 7", "Chendom 8", 
        "Glefan 1", "Glefan 2", "Glefan 3", "Glefan 4", "Glefan 5", "Glefan 6", "Glefan 7", 
        "Sqarbel 1", "Sqarbel 2", 
        "Obob 1", "Obob 2", "Obob 3", 
        "Ingvay 1", "Ingvay 2", "Ingvay 3", 
        "Particl 1", "Particl 2", "Particl 3", 
        "Vokz 1", "Vokz 2", "Vokz 3", "Vokz 4", "Vokz 5", "Vokz 6", 
        "Flux 1", "Flux 2", "Flux 3", "Flux 4", "Flux 5", 
        "Alweg 1", "Alweg 2", "Alweg 3", "Alweg 4", "Alweg 5", "Alweg 6", "Alweg 7", "Alweg 8", 
        "Tronic 1", "Tronic 2", "Tronic 3", "Tronic 4", "Tronic 5", "Tronic 6", 
        "Duotone 1", "Duotone 2", "Duotone 3", "Duotone 4", "Duotone 5", "Duotone 6", 
        "Bobanab 1", "Bobanab 2", "Bobanab 3", "Bobanab 4", 
        "Melotic 1", "Melotic 2", "Melotic 3", "Melotic 4", "Melotic 5", "Melotic 6", "Melotic 7", 
        "Cluster 1", "Cluster 2", "Cluster 3", "Cluster 4", "Cluster 5", "Cluster 6", "Cluster 7", "Cluster 8", 
        "Micoten 1", "Micoten 2", "Micoten 3", "Micoten 4", "Micoten 5", 
        "Orland 1", "Orland 2", "Orland 3", "Orland 4", "Orland 5", "Orland 6", "Orland 7", "Orland 8", 
        "Neuton 1", "Neuton 2", "Neuton 3", "Neuton 4", "Neuton 5", "Neuton 6", "Neuton 7", 
        "Xfer 1", "Xfer 2", "Xfer 3", "Xfer 4", "Xfer 5", "Xfer 6", "Xfer 7", 
        "Resyn 1", "Resyn 2", "Resyn 3", "Resyn 4", 
        "Sano 1", "Sano 2", "Sano 3", "Sano 4", 
        "SquRoo 1", "SquRoo 2", "SquRoo 3", "SquRoo 4", "SquRoo 5", "SquRoo 6", "SquRoo 7", "SquRoo 8", "SquRoo 9", "SquRoo 10", "SquRoo 11", "SquRoo 12", "SquRoo 13", "SquRoo 14", "SquRoo 15", 
        "Harmon 1", "Harmon 2", "Harmon 3", "Harmon 4", "Harmon 5", "Harmon 6", "Harmon 7", "Harmon 8", "Harmon 9", "Harmon 10", "Harmon 11", "Harmon 12", 
        "Harmon 13", "Harmon 14", "Harmon 15", "Harmon 16", "Harmon 17", "Harmon 18", "Harmon 19", "Harmon 20", "Harmon 21", "Harmon 22", "Harmon 23" 
        };


    public static final String[] MACRO_NAME_PRESETS = 
        {
        "2nd", "3rd", "4th", "5th", "6th", "7th", 
        "Air", "Amp", "ArpMode", "Attack", 
        "Bacon", "Bark", "BassDrop", "Beef", "Bend", "Bite", "Bleed", "Breath", "Brighten", "Buildup", 
        "CV 1", "CV 2", "Chance", "Chord", "Chorus", "Compres", "Crunch", "Crystal", "Cutoff", 
        "Darken", "Decay", "Delay", "Depth", "Distort", "Dry/Wet", 
        "EQ - Hi", "EQ - Low", "EQ - Mid", "Env Amt", 
        "FM", "Fast", "Feedback", "Filter", "Flanger", "Force", "Funk", 
        "GateTime", "Glide", "Go",
        "Harmonic", "Harmony", "Hurt", 
        "Itch", 
        "Jianbing", 
        "LFO Amt", "Level", 
        "MIDI CC", "MaiTai", "Major", "MakeHuge", "Mangle", "Massage", "Minor", "Mod 1", "Mod 2", "Mod Amt", "Morph", 
        "Noise", 
        "Oct +", "Oct -", "Oh", "Ouch", "Overdriv", 
        "PWM", "Pan", "Phase", "Phrase", "Pitch", "Pressure", "PulsWdth", "Purr", 
        "Range", "Ratchet", "Rate", "Ratio", "Release", "Reso", "Reverb", "RingMod", "Rotary", "Rumble", 
        "Scratch", "Slow", "Snarl", "Space", "Speed", "Spin", "Spread", "Stank", "Stop", "Stretch", "Sub", "Swing", 
        "Teardown", "Thicken", "Thin", "Time", "TimeDiv", "Twist", 
        "Velocity", "Vowel", 
        "Warp", "WavStack", "Wavescan", "Width", "Wobble", "Woof", "Wow"
        };

    public static final Color[] COLORS =
        {
        // The first 30 colors appear to move around HSB starting at 0 degrees (full Red) in increases of about 11,
        // This could be tweaked to move up to 11.5, but 12 seems too much.
        // Each even color is "light" (I have it at 50% saturation 100% brightness)
        // Each odd color is "dark" (I have it at 100% saturation 100% brightness)
        // But note that the light/dark colors aren't really *paired* -- they're still offset in hue
        // You could of course make the dark colors lower brightness, but it seems that they
        // really largely differ in terms of saturation.
        // The last two colors are gray and white.  White is pure white.  Gray is white at 75% brightness
        // Try Google's Color Picker for HSB   https://g.co/kgs/jmM2Wo
 
        // Color                                                // Hue                  Description                             
        new Color(0x00ff0000),          // 0            Red
        new Color(0x00ff9780),          // 11           Light Pink Coral
        new Color(0x00ff5e00),          // 22           Orange Red
        new Color(0x00ffc680),          // 33           Light Oranger Coral
        new Color(0x00ffbb00),          // 44           Orange
        new Color(0x00fff480),          // 55           Light Orange
        new Color(0x00e5ff00),          // 66           Orangish Yellow
        new Color(0x00dbff80),          // 77           Light Orangish Yellow
        new Color(0x0088ff00),          // 88           Yellow
        new Color(0x00acff80),          // 99           Light Yellow
        new Color(0x002bff00),          // 110          Greenish Yellow
        new Color(0x0080ff82),          // 121          Light Greenish Yellow
        new Color(0x0000ff33),          // 132          Green
        new Color(0x0080ffb1),          // 143          Light Green Moving towards Blue
        new Color(0x0000ff91),          // 154          Bluish Green
        new Color(0x0080ffdd),          // 165          Light Bluish Green
        new Color(0x0000ffee),          // 176          Turquoise
        new Color(0x0080f0ff),          // 187          Light Turquoise moving towards Azure
        new Color(0x0000b3ff),          // 198          Azure
        new Color(0x00802cff),          // 209          Light Azure
        new Color(0x000055ff),          // 220          Sky Blue
        new Color(0x008093ff),          // 231          Light Sky Blue
        new Color(0x000800ff),          // 242          Blue
        new Color(0x009b80ff),          // 253          Light Blue
        new Color(0x006600ff),          // 264          Purple
        new Color(0x00ca80ff),          // 275          Light Purple
        new Color(0x00c300ff),          // 286          Violet
        new Color(0x00f980ff),          // 297          Light Violet
        new Color(0x00ff00dd),          // 308          Magenta
        new Color(0x00ff80d7),          // 319          Light Magenta
        new Color(0x00bfbfbf),          //              Gray
        new Color(0x00ffffff),          //              White
        };

    public static final String[] COLORS_HTML = 
        {
        // These ae the same as the COLORS above, and in fact I use them but not COLORS in the code.
        "#ff0000",
        "#ff9780",
        "#ff5e00",
        "#ffc680",
        "#ffbb00",
        "#fff480",
        "#e5ff00",
        "#dbff80",
        "#88ff00",
        "#acff80",
        "#2bff00",
        "#80ff82",
        "#00ff33",
        "#80ffb1",
        "#00ff91",
        "#80ffdd",
        "#00ffee",
        "#80f0ff",
        "#00b3ff",
        "#802cff",
        "#0055ff",
        "#8093ff",
        "#0800ff",
        "#9b80ff",
        "#6600ff",
        "#ca80ff",
        "#c300ff",
        "#f980ff",
        "#ff00dd",
        "#ff80d7",
        "#bfbfbf",
        "#ffffff",
        };


    public static HashMap MOD_DESTINATION_NRPN_VALUES_TO_INDEX = null;
    public static final int[] MOD_DESTINATION_NRPN_VALUES = 
        {
        0x02 * 128 + 0x00,              //Off
        0x04 * 128 + 0x30,              //Arp Mode
        0x04 * 128 + 0x36,              //Arp Division
        0x04 * 128 + 0x37,              //Arp Swing
        0x04 * 128 + 0x35,              //Arp Gate
        0x04 * 128 + 0x32,              //Arp Octave
        0x04 * 128 + 0x33,              //Arp Oct Mode
        0x04 * 128 + 0x34,              //Arp Length
        0x04 * 128 + 0x31,              //Arp Phrase
        0x04 * 128 + 0x38,              //Arp Ratchet
        0x04 * 128 + 0x39,              //Arp Chance
        0x04 * 128 + 0x01,              //Osc 1 Pitch
        0x04 * 128 + 0x3A,              //Osc 1 Wave
        0x04 * 128 + 0x2A,              //Osc 1 WaveScan
        0x04 * 128 + 0x02,              //Osc 2 Pitch
        0x04 * 128 + 0x3B,              //Osc 2 Wave
        0x04 * 128 + 0x2B,              //Osc 2 WaveScan
        0x04 * 128 + 0x03,              //Osc 3 Pitch
        0x04 * 128 + 0x3C,              //Osc 3 Wave
        0x04 * 128 + 0x04,              //All Osc Pitch
        0x04 * 128 + 0x2C,              //Mut 1 Ratio
        0x02 * 128 + 0x1F,              //Mut 1 Depth
        0x02 * 128 + 0x1C,              //Mut 1 Window
        0x02 * 128 + 0x25,              //Mut 1 Feedback
        0x02 * 128 + 0x22,              //Mut 1 Dry/Wet
        0x02 * 128 + 0x60,              //Mut 1 Warp 1
        0x02 * 128 + 0x61,              //Mut 1 Warp 2
        0x02 * 128 + 0x62,              //Mut 1 Warp 3
        0x02 * 128 + 0x63,              //Mut 1 Warp 4
        0x02 * 128 + 0x64,              //Mut 1 Warp 5
        0x02 * 128 + 0x65,              //Mut 1 Warp 6
        0x02 * 128 + 0x66,              //Mut 1 Warp 7
        0x02 * 128 + 0x67,              //Mut 1 Warp 8
        0x04 * 128 + 0x2D,              //Mut 2 Ratio
        0x02 * 128 + 0x20,              //Mut 2 Depth
        0x02 * 128 + 0x1D,              //Mut 2 Window
        0x02 * 128 + 0x26,              //Mut 2 Feedback
        0x02 * 128 + 0x23,              //Mut 2 Dry/Wet
        0x02 * 128 + 0x68,              //Mut 2 Warp 1
        0x02 * 128 + 0x69,              //Mut 2 Warp 2
        0x02 * 128 + 0x6A,              //Mut 2 Warp 3
        0x02 * 128 + 0x6B,              //Mut 2 Warp 4
        0x02 * 128 + 0x6C,              //Mut 2 Warp 5
        0x02 * 128 + 0x6D,              //Mut 2 Warp 6
        0x02 * 128 + 0x6E,              //Mut 2 Warp 7
        0x02 * 128 + 0x6F,              //Mut 2 Warp 8
        0x04 * 128 + 0x2E,              //Mut 3 Ratio
        0x02 * 128 + 0x21,              //Mut 3 Depth
        0x02 * 128 + 0x1E,              //Mut 3 Window
        0x02 * 128 + 0x27,              //Mut 3 Feedback
        0x02 * 128 + 0x24,              //Mut 3 Dry/Wet
        0x02 * 128 + 0x70,              //Mut 3 Warp 1
        0x02 * 128 + 0x71,              //Mut 3 Warp 2
        0x02 * 128 + 0x72,              //Mut 3 Warp 3
        0x02 * 128 + 0x73,              //Mut 3 Warp 4
        0x02 * 128 + 0x74,              //Mut 3 Warp 5
        0x02 * 128 + 0x75,              //Mut 3 Warp 6
        0x02 * 128 + 0x76,              //Mut 3 Warp 7
        0x02 * 128 + 0x77,              //Mut 3 Warp 8
        0x04 * 128 + 0x2F,              //Mut 4 Ratio
        0x02 * 128 + 0x16,              //Mut 4 Depth 
        0x02 * 128 + 0x1A,              //Mut 4 Window 
        0x02 * 128 + 0x1B,              //Mut 4 Feedback 
        0x02 * 128 + 0x17,              //Mut 4 Dry/Wet 
        0x02 * 128 + 0x78,              //Mut 4 Warp 1 
        0x02 * 128 + 0x79,              //Mut 4 Warp 2 
        0x02 * 128 + 0x7A,              //Mut 4 Warp 3 
        0x02 * 128 + 0x7B,              //Mut 4 Warp 4 
        0x02 * 128 + 0x7C,              //Mut 4 Warp 5 
        0x02 * 128 + 0x7D,              //Mut 4 Warp 6 
        0x02 * 128 + 0x7E,              //Mut 4 Warp 7 
        0x02 * 128 + 0x7F,              //Mut 4 Warp 8 
        0x02 * 128 + 0x03,              //Ring Mod Depth
        0x02 * 128 + 0x07,              //Osc 1 Vol
        0x02 * 128 + 0x09,              //Osc 2 Vol 
        0x02 * 128 + 0x0B,              //Osc 3 Vol 
        0x02 * 128 + 0x01,              //Ring Mod Vol
        0x02 * 128 + 0x0D,              //Noise Vol 
        0x02 * 128 + 0x08,              //Osc 1 Pan 
        0x02 * 128 + 0x0A,              //Osc 2 Pan 
        0x02 * 128 + 0x0C,              //Osc 3 Pan 
        0x02 * 128 + 0x04,              //Ring Mod Pan  
        0x02 * 128 + 0x0E,              //Noise Pan 
        0x02 * 128 + 0x31,              //Osc 1 F1/2
        0x02 * 128 + 0x32,              //Osc 2 F1/2
        0x02 * 128 + 0x33,              //Osc 3 F1/2
        0x02 * 128 + 0x35,              //Ring Mod F1/2
        0x02 * 128 + 0x34,              //Noise F1/2
        0x02 * 128 + 0x28,              //Filt 1 Cutoff
        0x02 * 128 + 0x29,              //Filt 1 Resonance
        0x02 * 128 + 0x2B,              //Filt 1 Drive
        0x02 * 128 + 0x2A,              //Filt 1 Control
        0x04 * 128 + 0x61,              //Filt 1 Env 1
        0x04 * 128 + 0x60,              //Filt 1 LFO 1
        0x04 * 128 + 0x66,              //Filt 1 Keytrack
        0x02 * 128 + 0x2E,              //Filt 2 Morph
        0x02 * 128 + 0x2C,              //Filt 2 Cutoff
        0x02 * 128 + 0x2D,              //Filt 2 Resonance
        0x04 * 128 + 0x63,              //Filt 2 Env 1
        0x04 * 128 + 0x62,              //Filt 2 LFO 1
        0x04 * 128 + 0x67,              //Filt 2 Keytrack
        0x04 * 128 + 0x64,              //Amp LFO 2
        0x02 * 128 + 0x02,              //Amp Level
        0x04 * 128 + 0x6F,              //Pre-FX Param1
        0x04 * 128 + 0x70,              //Pre-FX Param2
        0x04 * 128 + 0x6E,              //Pre-FX Dry/Wet
        0x04 * 128 + 0x74,              //Delay Time
        0x04 * 128 + 0x75,              //Delay Feedback
        0x04 * 128 + 0x77,              //Delay Wet Tone
        0x04 * 128 + 0x76,              //Delay Feed Tone
        0x04 * 128 + 0x78,              //Delay Dry/Wet
        0x04 * 128 + 0x79,              //Reverb Time
        0x04 * 128 + 0x7A,              //Reverb Tone
        0x04 * 128 + 0x7B,              //Reverb Hi Damp
        0x04 * 128 + 0x7C,              //Reverb Lo Damp
        0x04 * 128 + 0x7E,              //Reverb Dry/Wet
        0x04 * 128 + 0x72,              //Post-FX Param1
        0x04 * 128 + 0x73,              //Post-FX Param2
        0x04 * 128 + 0x71,              //Post-FX Dry/Wet
        0x04 * 128 + 0x11,              //Env 1 Attack
        0x04 * 128 + 0x16,              //Env 1 Hold
        0x04 * 128 + 0x1B,              //Env 1 Decay
        0x04 * 128 + 0x20,              //Env 1 Sustain
        0x04 * 128 + 0x25,              //Env 1 Release
        0x04 * 128 + 0x12,              //Env 2 Attack
        0x04 * 128 + 0x17,              //Env 2 Hold
        0x04 * 128 + 0x1C,              //Env 2 Decay
        0x04 * 128 + 0x21,              //Env 2 Sustain
        0x04 * 128 + 0x26,              //Env 2 Release
        0x04 * 128 + 0x13,              //Env 3 Attack
        0x04 * 128 + 0x18,              //Env 3 Hold
        0x04 * 128 + 0x1D,              //Env 3 Decay
        0x04 * 128 + 0x22,              //Env 3 Sustain
        0x04 * 128 + 0x27,              //Env 3 Release
        0x04 * 128 + 0x14,              //Env 4 Attack
        0x04 * 128 + 0x19,              //Env 4 Hold
        0x04 * 128 + 0x1E,              //Env 4 Decay
        0x04 * 128 + 0x23,              //Env 4 Sustain
        0x04 * 128 + 0x28,              //Env 4 Release
        0x04 * 128 + 0x15,              //Env 5 Attack
        0x04 * 128 + 0x1A,              //Env 5 Hold
        0x04 * 128 + 0x1F,              //Env 5 Decay
        0x04 * 128 + 0x24,              //Env 5 Sustain
        0x04 * 128 + 0x29,              //Env 5 Release
        0x04 * 128 + 0x05,              //LFO 1 Rate
        0x04 * 128 + 0x0B,              //LFO 1 Level
        0x04 * 128 + 0x06,              //LFO 2 Rate
        0x04 * 128 + 0x0C,              //LFO 2 Level
        0x04 * 128 + 0x07,              //LFO 3 Rate
        0x04 * 128 + 0x0D,              //LFO 3 Level
        0x04 * 128 + 0x08,              //LFO 4 Rate
        0x04 * 128 + 0x0E,              //LFO 4 Level
        0x04 * 128 + 0x09,              //LFO 5 Rate
        0x04 * 128 + 0x0F,              //LFO 5 Level
        0x04 * 128 + 0x40,              //Matrix 1 Depth
        0x04 * 128 + 0x41,              //Matrix 2 Depth
        0x04 * 128 + 0x42,              //Matrix 3 Depth
        0x04 * 128 + 0x43,              //Matrix 4 Depth
        0x04 * 128 + 0x44,              //Matrix 5 Depth
        0x04 * 128 + 0x45,              //Matrix 6 Depth
        0x04 * 128 + 0x46,              //Matrix 7 Depth
        0x04 * 128 + 0x47,              //Matrix 8 Depth
        0x04 * 128 + 0x48,              //Matrix 9 Depth
        0x04 * 128 + 0x49,              //Matrix 10 Depth
        0x04 * 128 + 0x4A,              //Matrix 11 Depth
        0x04 * 128 + 0x4B,              //Matrix 12 Depth
        0x04 * 128 + 0x4C,              //Matrix 13 Depth
        0x04 * 128 + 0x4D,              //Matrix 14 Depth
        0x04 * 128 + 0x4E,              //Matrix 15 Depth
        0x04 * 128 + 0x4F,              //Matrix 16 Depth
        0x04 * 128 + 0x50,              //Matrix 17 Depth
        0x04 * 128 + 0x51,              //Matrix 18 Depth
        0x04 * 128 + 0x52,              //Matrix 19 Depth
        0x04 * 128 + 0x53,              //Matrix 20 Depth
        0x04 * 128 + 0x54,              //Matrix 21 Depth
        0x04 * 128 + 0x55,              //Matrix 22 Depth
        0x04 * 128 + 0x56,              //Matrix 23 Depth
        0x04 * 128 + 0x57,              //Matrix 24 Depth 
        0x04 * 128 + 0x58,              //Matrix 25 Depth
        0x04 * 128 + 0x59,              //Matrix 26 Depth
        0x04 * 128 + 0x5A,              //Matrix 27 Depth
        0x04 * 128 + 0x5B,              //Matrix 28 Depth
        0x04 * 128 + 0x5C,              //Matrix 29 Depth
        0x04 * 128 + 0x5D,              //Matrix 30 Depth
        0x04 * 128 + 0x5E,              //Matrix 31 Depth
        0x04 * 128 + 0x5F,              //Matrix 32 Depth
        0x02 * 128 + 0x50,              //Macro 1
        0x02 * 128 + 0x51,              //Macro 2
        0x02 * 128 + 0x52,              //Macro 3
        0x02 * 128 + 0x53,              //Macro 4
        0x02 * 128 + 0x54,              //Macro 5
        0x02 * 128 + 0x55,              //Macro 6
        0x02 * 128 + 0x56,              //Macro 7
        0x02 * 128 + 0x57,              //Macro 8
        0x02 * 128 + 0x5A,              //Detune
        0x02 * 128 + 0x5B,              //Analog Feel
        0x02 * 128 + 0x5D,              //Pitch Bend
        0x02 * 128 + 0x5E,              //Vibrato Amount
        0x02 * 128 + 0x5F,              //Vibrato Rate
        0x02 * 128 + 0x58,              //Glide Time
        0x02 * 128 + 0x05,              //CV Mod Out 1
        0x02 * 128 + 0x06,              //CV Mod Out 2
        0x05 * 128 + 0x00,              //CC 0
        0x05 * 128 + 0x01,              //CC 1
        0x05 * 128 + 0x02,              //CC 2
        0x05 * 128 + 0x03,              //CC 3
        0x05 * 128 + 0x04,              //CC 4
        0x05 * 128 + 0x05,              //CC 5
        0x05 * 128 + 0x06,              //CC 6
        0x05 * 128 + 0x07,              //CC 7
        0x05 * 128 + 0x08,              //CC 8
        0x05 * 128 + 0x09,              //CC 9
        0x05 * 128 + 0x0A,              //CC 10
        0x05 * 128 + 0x0B,              //CC 11
        0x05 * 128 + 0x0C,              //CC 12
        0x05 * 128 + 0x0D,              //CC 13
        0x05 * 128 + 0x0E,              //CC 14
        0x05 * 128 + 0x0F,              //CC 15
        0x05 * 128 + 0x10,              //CC 16
        0x05 * 128 + 0x11,              //CC 17
        0x05 * 128 + 0x12,              //CC 18
        0x05 * 128 + 0x13,              //CC 19
        0x05 * 128 + 0x14,              //CC 20
        0x05 * 128 + 0x15,              //CC 21
        0x05 * 128 + 0x16,              //CC 22
        0x05 * 128 + 0x17,              //CC 23
        0x05 * 128 + 0x18,              //CC 24
        0x05 * 128 + 0x19,              //CC 25
        0x05 * 128 + 0x1A,              //CC 26
        0x05 * 128 + 0x1B,              //CC 27
        0x05 * 128 + 0x1C,              //CC 28
        0x05 * 128 + 0x1D,              //CC 29
        0x05 * 128 + 0x1E,              //CC 30
        0x05 * 128 + 0x1F,              //CC 31
        0x05 * 128 + 0x20,              //CC 32
        0x05 * 128 + 0x21,              //CC 33
        0x05 * 128 + 0x22,              //CC 34
        0x05 * 128 + 0x23,              //CC 35
        0x05 * 128 + 0x24,              //CC 36
        0x05 * 128 + 0x25,              //CC 37
        0x05 * 128 + 0x26,              //CC 38
        0x05 * 128 + 0x27,              //CC 39
        0x05 * 128 + 0x28,              //CC 40
        0x05 * 128 + 0x29,              //CC 41
        0x05 * 128 + 0x2A,              //CC 42
        0x05 * 128 + 0x2B,              //CC 43
        0x05 * 128 + 0x2C,              //CC 44
        0x05 * 128 + 0x2D,              //CC 45
        0x05 * 128 + 0x2E,              //CC 46
        0x05 * 128 + 0x2F,              //CC 47
        0x05 * 128 + 0x30,              //CC 48
        0x05 * 128 + 0x31,              //CC 49
        0x05 * 128 + 0x32,              //CC 50
        0x05 * 128 + 0x33,              //CC 51
        0x05 * 128 + 0x34,              //CC 52
        0x05 * 128 + 0x35,              //CC 53
        0x05 * 128 + 0x36,              //CC 54
        0x05 * 128 + 0x37,              //CC 55
        0x05 * 128 + 0x38,              //CC 56
        0x05 * 128 + 0x39,              //CC 57
        0x05 * 128 + 0x3A,              //CC 58
        0x05 * 128 + 0x3B,              //CC 59
        0x05 * 128 + 0x3C,              //CC 60
        0x05 * 128 + 0x3D,              //CC 61
        0x05 * 128 + 0x3E,              //CC 62
        0x05 * 128 + 0x3F,              //CC 63
        0x05 * 128 + 0x40,              //CC 64
        0x05 * 128 + 0x41,              //CC 65
        0x05 * 128 + 0x42,              //CC 66
        0x05 * 128 + 0x43,              //CC 67
        0x05 * 128 + 0x44,              //CC 68
        0x05 * 128 + 0x45,              //CC 69
        0x05 * 128 + 0x46,              //CC 70
        0x05 * 128 + 0x47,              //CC 71
        0x05 * 128 + 0x48,              //CC 72
        0x05 * 128 + 0x49,              //CC 73
        0x05 * 128 + 0x4A,              //CC 74
        0x05 * 128 + 0x4B,              //CC 75
        0x05 * 128 + 0x4C,              //CC 76
        0x05 * 128 + 0x4D,              //CC 77
        0x05 * 128 + 0x4E,              //CC 78
        0x05 * 128 + 0x4F,              //CC 79
        0x05 * 128 + 0x50,              //CC 80
        0x05 * 128 + 0x51,              //CC 81
        0x05 * 128 + 0x52,              //CC 82
        0x05 * 128 + 0x53,              //CC 83
        0x05 * 128 + 0x54,              //CC 84
        0x05 * 128 + 0x55,              //CC 85
        0x05 * 128 + 0x56,              //CC 86
        0x05 * 128 + 0x57,              //CC 87
        0x05 * 128 + 0x58,              //CC 88
        0x05 * 128 + 0x59,              //CC 89
        0x05 * 128 + 0x5A,              //CC 90
        0x05 * 128 + 0x5B,              //CC 91
        0x05 * 128 + 0x5C,              //CC 92
        0x05 * 128 + 0x5D,              //CC 93
        0x05 * 128 + 0x5E,              //CC 94
        0x05 * 128 + 0x5F,              //CC 95
        0x05 * 128 + 0x60,              //CC 96
        0x05 * 128 + 0x61,              //CC 97
        0x05 * 128 + 0x62,              //CC 98
        0x05 * 128 + 0x63,              //CC 99
        0x05 * 128 + 0x64,              //CC 100
        0x05 * 128 + 0x65,              //CC 101
        0x05 * 128 + 0x66,              //CC 102
        0x05 * 128 + 0x67,              //CC 103
        0x05 * 128 + 0x68,              //CC 104
        0x05 * 128 + 0x69,              //CC 105
        0x05 * 128 + 0x6A,              //CC 106
        0x05 * 128 + 0x6B,              //CC 107
        0x05 * 128 + 0x6C,              //CC 108
        0x05 * 128 + 0x6D,              //CC 109
        0x05 * 128 + 0x6E,              //CC 110
        0x05 * 128 + 0x6F,              //CC 111
        0x05 * 128 + 0x70,              //CC 112
        0x05 * 128 + 0x71,              //CC 113
        0x05 * 128 + 0x72,              //CC 114
        0x05 * 128 + 0x73,              //CC 115
        0x05 * 128 + 0x74,              //CC 116
        0x05 * 128 + 0x75,              //CC 117
        0x05 * 128 + 0x76,              //CC 118
        0x05 * 128 + 0x77,              //CC 119
        0x05 * 128 + 0x78,              //CC 120
        0x05 * 128 + 0x79,              //CC 121
        0x05 * 128 + 0x7A,              //CC 122
        0x05 * 128 + 0x7B,              //CC 123
        0x05 * 128 + 0x7C,              //CC 124
        0x05 * 128 + 0x7D,              //CC 125
        0x05 * 128 + 0x7E,              //CC 126
        0x05 * 128 + 0x7F,              //CC 127 
        };


/*
  WARNING: For some reason, there is a Sustain Ped source which is the same as CC 64 (also sustain).
  Note that there is no CC source equivalent to the Expression Ped source.
*/

    public static HashMap MOD_SOURCE_NRPN_VALUES_TO_INDEX = null;
    public static final int[] MOD_SOURCE_NRPN_VALUES = 
        {
        0x01 * 128 + 0x00,              //Off
        0x01 * 128 + 0x01,              //Env 1
        0x01 * 128 + 0x02,              //Env 2
        0x01 * 128 + 0x03,              //Env 3
        0x01 * 128 + 0x04,              //Env 4
        0x01 * 128 + 0x05,              //Env 5
        0x01 * 128 + 0x06,              //LFO 1
        0x01 * 128 + 0x07,              //LFO 2
        0x01 * 128 + 0x08,              //LFO 3
        0x01 * 128 + 0x09,              //LFO 4
        0x01 * 128 + 0x0A,              //LFO 5
        0x01 * 128 + 0x0B,              //LFO 1 Unipolar
        0x01 * 128 + 0x0C,              //LFO 2 Unipolar
        0x01 * 128 + 0x0D,              //LFO 3 Unipolar
        0x01 * 128 + 0x0E,              //LFO 4 Unipolar
        0x01 * 128 + 0x0F,              //LFO 5 Unipolar
        0x01 * 128 + 0x12,              //Chan Aftertouch
        0x01 * 128 + 0x13,              //Poly Aftertouch
        0x01 * 128 + 0x16,              //Keytrack
        0x01 * 128 + 0x14,              //Note-On Vel
        0x03 * 128 + 0x2B,              //Note-Off Vel
        0x01 * 128 + 0x17,              //Pitch Wheel
        0x01 * 128 + 0x18,              //Mod Wheel
        0x01 * 128 + 0x1B,              //Ribbon Absolute
        0x01 * 128 + 0x19,              //Ribbon Unipolar
        0x01 * 128 + 0x1C,              //Ribbon Relative
        0x01 * 128 + 0x1F,              //Expression Ped
        0x01 * 128 + 0x68,              //Sustain Ped                   Same source as CC 64 !!!
        0x01 * 128 + 0x1D,              //Mod In 1
        0x01 * 128 + 0x1E,              //Mod In 2
        0x03 * 128 + 0x28,              //MPE-X
        0x03 * 128 + 0x2A,              //MPE-Y Absolute
        0x03 * 128 + 0x2E,              //MPE-Y Relative
        0x03 * 128 + 0x2F,              //Voice Mod
        0x03 * 128 + 0x30,              //Voice Mod +
        0x01 * 128 + 0x28,              //CC 0
        0x01 * 128 + 0x29,              //CC 1
        0x01 * 128 + 0x2A,              //CC 2
        0x01 * 128 + 0x2B,              //CC 3
        0x01 * 128 + 0x2C,              //CC 4
        0x01 * 128 + 0x2D,              //CC 5
        0x01 * 128 + 0x2E,              //CC 6
        0x01 * 128 + 0x2F,              //CC 7
        0x01 * 128 + 0x30,              //CC 8
        0x01 * 128 + 0x31,              //CC 9
        0x01 * 128 + 0x32,              //CC 10
        0x01 * 128 + 0x33,              //CC 11
        0x01 * 128 + 0x34,              //CC 12
        0x01 * 128 + 0x35,              //CC 13
        0x01 * 128 + 0x36,              //CC 14
        0x01 * 128 + 0x37,              //CC 15
        0x01 * 128 + 0x38,              //CC 16
        0x01 * 128 + 0x39,              //CC 17
        0x01 * 128 + 0x3A,              //CC 18
        0x01 * 128 + 0x3B,              //CC 19
        0x01 * 128 + 0x3C,              //CC 20
        0x01 * 128 + 0x3D,              //CC 21
        0x01 * 128 + 0x3E,              //CC 22
        0x01 * 128 + 0x3F,              //CC 23
        0x01 * 128 + 0x40,              //CC 24
        0x01 * 128 + 0x41,              //CC 25
        0x01 * 128 + 0x42,              //CC 26
        0x01 * 128 + 0x43,              //CC 27
        0x01 * 128 + 0x44,              //CC 28
        0x01 * 128 + 0x45,              //CC 29
        0x01 * 128 + 0x46,              //CC 30
        0x01 * 128 + 0x47,              //CC 31
        0x01 * 128 + 0x48,              //CC 32
        0x01 * 128 + 0x49,              //CC 33
        0x01 * 128 + 0x4A,              //CC 34
        0x01 * 128 + 0x4B,              //CC 35
        0x01 * 128 + 0x4C,              //CC 36
        0x01 * 128 + 0x4D,              //CC 37
        0x01 * 128 + 0x4E,              //CC 38
        0x01 * 128 + 0x4F,              //CC 39
        0x01 * 128 + 0x50,              //CC 40
        0x01 * 128 + 0x51,              //CC 41
        0x01 * 128 + 0x52,              //CC 42
        0x01 * 128 + 0x53,              //CC 43
        0x01 * 128 + 0x54,              //CC 44
        0x01 * 128 + 0x55,              //CC 45
        0x01 * 128 + 0x56,              //CC 46
        0x01 * 128 + 0x57,              //CC 47
        0x01 * 128 + 0x58,              //CC 48
        0x01 * 128 + 0x59,              //CC 49
        0x01 * 128 + 0x5A,              //CC 50
        0x01 * 128 + 0x5B,              //CC 51
        0x01 * 128 + 0x5C,              //CC 52
        0x01 * 128 + 0x5D,              //CC 53
        0x01 * 128 + 0x5E,              //CC 54
        0x01 * 128 + 0x5F,              //CC 55
        0x01 * 128 + 0x60,              //CC 56
        0x01 * 128 + 0x61,              //CC 57
        0x01 * 128 + 0x62,              //CC 58
        0x01 * 128 + 0x63,              //CC 59
        0x01 * 128 + 0x64,              //CC 60
        0x01 * 128 + 0x65,              //CC 61
        0x01 * 128 + 0x66,              //CC 62
        0x01 * 128 + 0x67,              //CC 63
        0x01 * 128 + 0x68,              //CC 64                 Same source as Sustain Ped  !!!
        0x01 * 128 + 0x69,              //CC 65
        0x01 * 128 + 0x6A,              //CC 66
        0x01 * 128 + 0x6B,              //CC 67
        0x01 * 128 + 0x6C,              //CC 68
        0x01 * 128 + 0x6D,              //CC 69
        0x01 * 128 + 0x6E,              //CC 70
        0x01 * 128 + 0x6F,              //CC 71
        0x01 * 128 + 0x70,              //CC 72
        0x01 * 128 + 0x71,              //CC 73
        0x01 * 128 + 0x72,              //CC 74
        0x01 * 128 + 0x73,              //CC 75
        0x01 * 128 + 0x74,              //CC 76
        0x01 * 128 + 0x75,              //CC 77
        0x01 * 128 + 0x76,              //CC 78
        0x01 * 128 + 0x77,              //CC 79
        0x01 * 128 + 0x78,              //CC 80
        0x01 * 128 + 0x79,              //CC 81
        0x01 * 128 + 0x7A,              //CC 82
        0x01 * 128 + 0x7B,              //CC 83
        0x01 * 128 + 0x7C,              //CC 84
        0x01 * 128 + 0x7D,              //CC 85
        0x01 * 128 + 0x7E,              //CC 86
        0x01 * 128 + 0x7F,              //CC 87
        0x03 * 128 + 0x00,              //CC 88
        0x03 * 128 + 0x01,              //CC 89
        0x03 * 128 + 0x02,              //CC 90
        0x03 * 128 + 0x03,              //CC 91
        0x03 * 128 + 0x04,              //CC 92
        0x03 * 128 + 0x05,              //CC 93
        0x03 * 128 + 0x06,              //CC 94
        0x03 * 128 + 0x07,              //CC 95
        0x03 * 128 + 0x08,              //CC 96
        0x03 * 128 + 0x09,              //CC 97
        0x03 * 128 + 0x0A,              //CC 98
        0x03 * 128 + 0x0B,              //CC 99
        0x03 * 128 + 0x0C,              //CC 100
        0x03 * 128 + 0x0D,              //CC 101
        0x03 * 128 + 0x0E,              //CC 102
        0x03 * 128 + 0x0F,              //CC 103
        0x03 * 128 + 0x10,              //CC 104
        0x03 * 128 + 0x11,              //CC 105
        0x03 * 128 + 0x12,              //CC 106
        0x03 * 128 + 0x13,              //CC 107
        0x03 * 128 + 0x14,              //CC 108
        0x03 * 128 + 0x15,              //CC 109
        0x03 * 128 + 0x16,              //CC 110
        0x03 * 128 + 0x17,              //CC 111
        0x03 * 128 + 0x18,              //CC 112
        0x03 * 128 + 0x19,              //CC 113
        0x03 * 128 + 0x1A,              //CC 114
        0x03 * 128 + 0x1B,              //CC 115
        0x03 * 128 + 0x1C,              //CC 116
        0x03 * 128 + 0x1D,              //CC 117
        0x03 * 128 + 0x1E,              //CC 118
        0x03 * 128 + 0x1F,              //CC 119
        0x03 * 128 + 0x20,              //CC 120
        0x03 * 128 + 0x21,              //CC 121
        0x03 * 128 + 0x22,              //CC 122
        0x03 * 128 + 0x23,              //CC 123
        0x03 * 128 + 0x24,              //CC 124
        0x03 * 128 + 0x25,              //CC 125
        0x03 * 128 + 0x26,              //CC 126
        0x03 * 128 + 0x27,              //CC 127 
        };


// This is an array of FX preset values in the order   [type][preset group][value in the format MSB * 128 + LSB]
// The first values are FX TYPE and FX PRESET, the remainder are the five FX PRESET VALUES
    public int[][][] FX_PRESET_VALUES =   
        {
            { 
            },
            {
            {0x00 * 128 + 0x08, 0x00 * 128 + 0x00, 0x10 * 128 + 0x18, 0x0E * 128 + 0x00, 0x0B * 128 + 0x20, 0x04 * 128 + 0x00, 0x00 * 128 + 0x08, 0x20 * 128 + 0x00},
            {0x00 * 128 + 0x08, 0x00 * 128 + 0x08, 0x14 * 128 + 0x10, 0x11 * 128 + 0x40, 0x0B * 128 + 0x20, 0x04 * 128 + 0x00, 0x00 * 128 + 0x08, 0x20 * 128 + 0x00},
            {0x00 * 128 + 0x08, 0x00 * 128 + 0x10, 0x21 * 128 + 0x40, 0x09 * 128 + 0x00, 0x0B * 128 + 0x20, 0x04 * 128 + 0x00, 0x00 * 128 + 0x08, 0x20 * 128 + 0x00},
            },
            {
            {0x00 * 128 + 0x10, 0x00 * 128 + 0x00, 0x07 * 128 + 0x58, 0x36 * 128 + 0x40, 0x00 * 128 + 0x00, 0x06 * 128 + 0x68, 0x00 * 128 + 0x08, 0x20 * 128 + 0x00},
            {0x00 * 128 + 0x10, 0x00 * 128 + 0x08, 0x10 * 128 + 0x18, 0x06 * 128 + 0x40, 0x00 * 128 + 0x00, 0x07 * 128 + 0x30, 0x00 * 128 + 0x08, 0x20 * 128 + 0x00},
            {0x00 * 128 + 0x10, 0x00 * 128 + 0x10, 0x07 * 128 + 0x58, 0x1E * 128 + 0x00, 0x00 * 128 + 0x00, 0x00 * 128 + 0x48, 0x00 * 128 + 0x08, 0x20 * 128 + 0x00},
            },
            {
            {0x00 * 128 + 0x18, 0x00 * 128 + 0x00, 0x1A * 128 + 0x18, 0x23 * 128 + 0x20, 0x01 * 128 + 0x50, 0x02 * 128 + 0x18, 0x04 * 128 + 0x30, 0x20 * 128 + 0x00},
            {0x00 * 128 + 0x18, 0x00 * 128 + 0x08, 0x0C * 128 + 0x18, 0x1E * 128 + 0x60, 0x01 * 128 + 0x58, 0x01 * 128 + 0x69, 0x04 * 128 + 0x00, 0x20 * 128 + 0x00},
            {0x00 * 128 + 0x18, 0x00 * 128 + 0x10, 0x1A * 128 + 0x10, 0x1C * 128 + 0x60, 0x04 * 128 + 0x30, 0x04 * 128 + 0x30, 0x04 * 128 + 0x20, 0x20 * 128 + 0x00},
            },
            {
            {0x00 * 128 + 0x20, 0x00 * 128 + 0x00, 0x10 * 128 + 0x18, 0x25 * 128 + 0x00, 0x06 * 128 + 0x78, 0x04 * 128 + 0x50, 0x0B * 128 + 0x20, 0x20 * 128 + 0x00},
            {0x00 * 128 + 0x20, 0x00 * 128 + 0x08, 0x10 * 128 + 0x18, 0x36 * 128 + 0x00, 0x06 * 128 + 0x78, 0x04 * 128 + 0x50, 0x00 * 128 + 0x00, 0x20 * 128 + 0x00},
            {0x00 * 128 + 0x20, 0x00 * 128 + 0x10, 0x05 * 128 + 0x58, 0x30 * 128 + 0x00, 0x06 * 128 + 0x00, 0x04 * 128 + 0x00, 0x00 * 128 + 0x00, 0x20 * 128 + 0x00},
            },
            {
            {0x00 * 128 + 0x28, 0x00 * 128 + 0x00, 0x13 * 128 + 0x58, 0x11 * 128 + 0x28, 0x00 * 128 + 0x18, 0x04 * 128 + 0x18, 0x00 * 128 + 0x40, 0x20 * 128 + 0x00},
            {0x00 * 128 + 0x28, 0x00 * 128 + 0x08, 0x13 * 128 + 0x58, 0x0A * 128 + 0x40, 0x00 * 128 + 0x20, 0x04 * 128 + 0x18, 0x00 * 128 + 0x28, 0x20 * 128 + 0x00},
            },
            {
            {0x00 * 128 + 0x30, 0x00 * 128 + 0x00, 0x39 * 128 + 0x18, 0x18 * 128 + 0x40, 0x00 * 128 + 0x00, 0x0D * 128 + 0x58, 0x00 * 128 + 0x00, 0x20 * 128 + 0x00},
            {0x00 * 128 + 0x30, 0x00 * 128 + 0x08, 0x39 * 128 + 0x18, 0x1A * 128 + 0x00, 0x00 * 128 + 0x00, 0x0D * 128 + 0x58, 0x00 * 128 + 0x18, 0x20 * 128 + 0x00},
            {0x00 * 128 + 0x30, 0x00 * 128 + 0x10, 0x30 * 128 + 0x58, 0x32 * 128 + 0x00, 0x00 * 128 + 0x00, 0x05 * 128 + 0x50, 0x01 * 128 + 0x40, 0x20 * 128 + 0x00},
            },
            {
            {0x00 * 128 + 0x38, 0x00 * 128 + 0x00, 0x26 * 128 + 0x00, 0x26 * 128 + 0x00, 0x16 * 128 + 0x40, 0x0F * 128 + 0x50, 0x0F * 128 + 0x50, 0x20 * 128 + 0x00},
            {0x00 * 128 + 0x38, 0x00 * 128 + 0x08, 0x29 * 128 + 0x20, 0x23 * 128 + 0x50, 0x15 * 128 + 0x20, 0x0B * 128 + 0x70, 0x0F * 128 + 0x50, 0x20 * 128 + 0x00},
            {0x00 * 128 + 0x38, 0x00 * 128 + 0x10, 0x1E * 128 + 0x20, 0x26 * 128 + 0x38, 0x17 * 128 + 0x10, 0x1C * 128 + 0x10, 0x0F * 128 + 0x50, 0x20 * 128 + 0x00},
            {0x00 * 128 + 0x38, 0x00 * 128 + 0x18, 0x26 * 128 + 0x00, 0x1B * 128 + 0x50, 0x17 * 128 + 0x10, 0x0F * 128 + 0x50, 0x13 * 128 + 0x48, 0x20 * 128 + 0x00},
            {0x00 * 128 + 0x38, 0x00 * 128 + 0x20, 0x29 * 128 + 0x38, 0x29 * 128 + 0x38, 0x16 * 128 + 0x40, 0x0F * 128 + 0x50, 0x1E * 128 + 0x28, 0x20 * 128 + 0x00},
            {0x00 * 128 + 0x38, 0x00 * 128 + 0x28, 0x0A * 128 + 0x10, 0x00 * 128 + 0x00, 0x19 * 128 + 0x50, 0x1C * 128 + 0x60, 0x0F * 128 + 0x50, 0x20 * 128 + 0x00},
            {0x00 * 128 + 0x38, 0x00 * 128 + 0x30, 0x27 * 128 + 0x70, 0x1F * 128 + 0x28, 0x17 * 128 + 0x10, 0x18 * 128 + 0x00, 0x1D * 128 + 0x58, 0x20 * 128 + 0x00},
            },
            {
            },
            {
            {0x00 * 128 + 0x48, 0x00 * 128 + 0x00, 0x1D * 128 + 0x00, 0x12 * 128 + 0x60, 0x00 * 128 + 0x00, 0x08 * 128 + 0x00, 0x11 * 128 + 0x58, 0x20 * 128 + 0x00},
            {0x00 * 128 + 0x48, 0x00 * 128 + 0x08, 0x1F * 128 + 0x40, 0x33 * 128 + 0x30, 0x01 * 128 + 0x40, 0x00 * 128 + 0x68, 0x13 * 128 + 0x50, 0x20 * 128 + 0x00},
            {0x00 * 128 + 0x48, 0x00 * 128 + 0x10, 0x18 * 128 + 0x58, 0x28 * 128 + 0x50, 0x00 * 128 + 0x00, 0x00 * 128 + 0x00, 0x0F * 128 + 0x70, 0x20 * 128 + 0x00}                
            },
        };


    /// WAVE IMAGES

    public static ImageIcon[] waves = new ImageIcon[219];
    static
        {
        for(int i = 0; i < waves.length; i++)
            {
            waves[i] = new ImageIcon(ASMHydrasynth.class.getResource("waves/" + i + ".png"));
            }
        }



    /// DELAY TIME DISPLAY COMPUTATION  (what a mess)
        
    // These are the delay times (in ms) corresonding to delaytime values 72 ... 183.
    // See getDelayTimeSyncOff(...) to get all delay times
    public static final int[] SOME_DELAY_TIMES = 
        {
        10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10,
        11, 11, 11, 11, 11, 11, 11, 11, 
        12, 12, 12, 12, 12, 12, 
        13, 13, 
        15, 15, 15, 
        16, 16, 16, 
        17, 17, 
        18, 18, 18, 
        19, 19, 19, 
        20, 20, 20, 20, 20, 
        21, 21, 21, 
        22, 22, 
        23, 23, 23, 
        25, 25, 25, 
        26, 26, 
        27, 27, 27, 
        28, 28, 28, 
        29, 
        29, 30, 30, 30, 30, 30, 30, 
        31, 31, 
        32, 32, 32, 
        33, 33, 33, 
        35, 35, 
        36, 36, 36, 
        37, 37, 37, 
        38, 38, 
        39, 39, 39, 
        40, 40, 40, 40, 
        41, 
        42, 42, 
        43, 43, 
        45, 
        46, 46, 
        47, 
        48, 48, 
        49, 49
        };
        
    // These patterns are used to compute delay times corresponding to values from 184...743.
    // See getDelayTimeSyncOff(...) to get all delay times
    public static final int[] DELAY_TIME_PATTERN_1 = { 0, 0, 0, 1, 2, 2, 3, 3, 5, 6, 6, 7, 8, 8, 9, 9 };
    public static final int[] DELAY_TIME_PATTERN_2 = { 0, 0, 2, 3, 5, 6, 8, 9 };
    public static final int[] DELAY_TIME_PATTERN_3 = { 0, 2, 5, 8 };
    public static final int[] DELAY_TIME_PATTERN_4 = { 0, 3, 8, 10, 15, 19, 22, 26 };

    // These patterns are used to compute delay times corresponding to values from 744...1024.
    // See getDelayTimeSyncOff(...) to get all delay times
    public static final String[] SOME_MORE_DELAY_TIMES =
        {
        "1.00s", "1.00s", "1.01s", "1.01s", "1.02s", "1.02s", "1.02s", "1.03s", "1.03s", "1.03s", 
        "1.04s", "1.04s", "1.05s", "1.05s", "1.06s", "1.06s", "1.06s", "1.07s", "1.07s", "1.07s", 
        "1.08s", "1.08s", "1.08s", "1.09s", "1.09s", "1.10s", "1.11s", "1.11s", "1.12s", "1.13s", 
        "1.14s", "1.14s", "1.15s", "1.16s", "1.16s", "1.17s", "1.18s", "1.18s", "1.19s", "1.19s", 
        "1.20s", "1.21s", "1.21s", "1.22s", "1.23s", "1.23s", "1.24s", "1.24s", "1.25s", "1.26s", 
        "1.26s", "1.27s", "1.28s", "1.28s", "1.29s", "1.29s", "1.30s", "1.31s", "1.31s", "1.32s", 
        "1.33s", "1.33s", "1.34s", "1.34s", "1.35s", "1.36s", "1.36s", "1.37s", "1.38s", "1.38s", 
        "1.39s", "1.39s", "1.40s", "1.41s", "1.41s", "1.42s", "1.43s", "1.43s", "1.44s", "1.44s", 
        "1.45s", "1.46s", "1.46s", "1.47s", "1.48s", "1.48s", "1.49s", "1.49s", "1.50s", "1.51s", 
        "1.51s", "1.52s", "1.53s", "1.53s", "1.54s", "1.54s", "1.55s", "1.56s", "1.56s", "1.57s", 
        "1.58s", "1.58s", "1.59s", "1.59s", "1.60s", "1.61s", "1.61s", "1.62s", "1.63s", "1.63s", 
        "1.64s", "1.64s", "1.65s", "1.66s", "1.66s", "1.67s", "1.68s", "1.68s", "1.69s", "1.69s", 
        "1.70s", "1.71s", "1.71s", "1.72s", "1.73s", "1.73s", "1.74s", "1.74s", "1.75s", "1.76s", 
        "1.76s", "1.77s", "1.78s", "1.78s", "1.79s", "1.79s", "1.80s", "1.81s", "1.81s", "1.82s", 
        "1.83s", "1.83s", "1.84s", "1.84s", "1.85s", "1.86s", "1.86s", "1.87s", "1.88s", "1.88s", 
        "1.89s", "1.89s", "1.90s", "1.91s", "1.91s", "1.92s", "1.93s", "1.93s", "1.94s", "1.94s", 
        "1.95s", "1.96s", "1.96s", "1.97s", "1.98s", "1.98s", "1.99s", "1.99s", "2.00s", "2.01s", 
        "2.01s", "2.02s", "2.03s", "2.03s", "2.04s", "2.04s", "2.05s", "2.06s", "2.06s", "2.07s", 
        "2.08s", "2.08s", "2.09s", "2.09s", "2.10s", "2.11s", "2.11s", "2.12s", "2.13s", "2.13s", 
        "2.14s", "2.14s", "2.15s", "2.16s", "2.16s", "2.17s", "2.18s", "2.18s", "2.19s", "2.19s", 
        "2.20s", "2.21s", "2.21s", "2.22s", "2.23s", "2.23s", "2.24s", "2.24s", "2.25s", "2.26s", 
        "2.26s", "2.27s", "2.28s", "2.28s", "2.29s", "2.29s", "2.30s", "2.31s", "2.31s", "2.32s", 
        "2.33s", "2.33s", "2.34s", "2.34s", "2.35s", "2.36s", "2.36s", "2.37s", "2.38s", "2.38s", 
        "2.39s", "2.39s", "2.40s", "2.41s", "2.43s", "2.44s", "2.45s", "2.46s", "2.48s", "2.49s", 
        "2.50s", "2.51s", "2.53s", "2.54s", "2.55s", "2.56s", "2.58s", "2.59s", "2.60s", "2.61s", 
        "2.63s", "2.64s", "2.65s", "2.66s", "2.68s", "2.69s", "2.70s", "2.71s", "2.73s", "2.74s", 
        "2.75s", "2.76s", "2.78s", "2.79s", "2.80s", "2.81s", "2.83s", "2.84s", "2.85s", "2.86s", 
        "2.88s", "2.89s", "2.90s", "2.91s", "2.93s", "2.94s", "2.95s", "2.96s", "2.98s", "2.99s", 
        "3.00s"
        };
                
    // The delay time pattern is very convoluted.  It's 0...1024, with:
    // 0-72 being 1.0...10ms in 0.125 increments, displayed as "x.xms" rounded 0.5 towards even
    // 72-183 being drawn from SOME_DELAY_TIMES in ms
    // 184-344 being 50-150ms in the following pattern every multiple of 10: 
    //              x0 x0 x0 x1 x1 x2 x2 x3 x3 x5 x6 x6 x7 x7 x8 x8 x9 x9 
    // 344-544 being 150-400ms in the following pattern every multiple of 10: 
    //              x0 x0 x2 x3 x5 x6 x8 x9
    // 544-664 being 400-700ms in the following pattern every multiple of 10:
    //              x0 x2 x5 x8
    // 664-744 being 700-1000ms (1.00 sec) in the following pattern every  multiple of 30:
    //              x0 x3 x8 (x+1)0 (x+1)5 (x+1)9 (x+2)2 (x+2)6
    // 744-1024     being drawn from SOME_MORE_DELAY_TIMES 
    //
    // The following function computes it based on the five tables above.
        
    public static String getDelayTimeSyncOff(int val)
        {
        if (val < 72)
            {
            int v = (int)Math.round((val * 0.125 + 1.0) * 10);
            return "" + (v / 10) + "." + (v % 10) + "ms";
            }
        else if (val < 184)
            {
            return "" + SOME_DELAY_TIMES[val-72] + "ms";
            }
        else if (val < 344)
            {
            int tens = (val - 184) / DELAY_TIME_PATTERN_1.length;
            int ones = (val - 184) % DELAY_TIME_PATTERN_1.length;
            return "" + ((tens * 10 + 50) + DELAY_TIME_PATTERN_1[ones]) + "ms";
            }
        else if (val < 544)
            {
            int tens = (val - 344) / DELAY_TIME_PATTERN_2.length;
            int ones = (val - 344) % DELAY_TIME_PATTERN_2.length;
            return "" + ((tens * 10 + 150) + DELAY_TIME_PATTERN_2[ones]) + "ms";
            }
        else if (val < 664)
            {
            int tens = (val - 544) / DELAY_TIME_PATTERN_3.length;
            int ones = (val - 544) % DELAY_TIME_PATTERN_3.length;
            return "" + ((tens * 10 + 400) + DELAY_TIME_PATTERN_3[ones]) + "ms";
            }
        else if (val < 744)
            {
            int tens = (val - 664) / DELAY_TIME_PATTERN_4.length;
            int ones = (val - 664) % DELAY_TIME_PATTERN_4.length;
            return "" + ((tens * 3 * 10 + 700) + DELAY_TIME_PATTERN_4[ones]) + "ms";
            }
        else
            {
            return SOME_MORE_DELAY_TIMES[val - 744];
            }
        }



                
    /// REVERB TIME DISPLAY COMPUTATION

    // These are the reverb times corresonding to reverbtime values 0...1024 in intervals of 8.
    public static final String[] REVERB_TIMES = 
        {
        "120ms", "130ms", "140ms", "155ms", "170ms", "185ms", "200ms", "215ms", "230ms", "245ms", 
        "260ms", "280ms", "300ms", "320ms", "345ms", "370ms", "400ms", "420ms", "440ms", "460ms", 
        "480ms", "500ms", "520ms", "540ms", "560ms", "570ms", "600ms", "630ms", "660ms", "700ms", 
        "730ms", "765ms", "800ms", "860ms", "930ms", "1.00s", "1.02s", "1.04s", "1.06s", "1.08s", 
        "1.10s", "1.15s", "1.20s", "1.25s", "1.30s", "1.35s", "1.40s", "1.45s", "1.50s", "1.55s", 
        "1.60s", "1.67s", "1.75s", "1.80s", "1.90s", "2.00s", "2.05s", "2.10s", "2.15s", "2.20s", 
        "2.25s", "2.30s", "2.36s", "2.43s", "2.50s", "2.60s", "2.70s", "2.80s", "2.90s", "3.00s", 
        "3.10s", "3.20s", "3.30s", "3.40s", "3.50s", "3.60s", "3.70s", "3.80s", "3.90s", "3.95s", 
        "4.00s", "4.10s", "4.20s", "4.40s", "4.60s", "4.80s", "5.00s", "5.20s", "5.40s", "5.60s", 
        "5.90s", "6.20s", "6.50s", "6.80s", "7.20s", "7.60s", "8.00s", "8.80s", "9.60s", "10.4s", 
        "11.0s", "12.0s", "13.0s", "14.0s", "15.0s", "16.0s", "17.0s", "18.0s", "19.0s", "20.0s", 
        "21.0s", "22.0s", "23.0s", "24.0s", "25.0s", "27.0s", "29.0s", "31.0s", "33.0s", "35.0s", 
        "38.0s", "42.0s", "45.0s", "51.0s", "58.0s", "65.0s", "75.0s", "90.0s", "Freeze"
        };

    public static String getReverbTime(int val)
        {
        return REVERB_TIMES[val / 8];
        }

    // Courtesy of Benny Ronnhager.  I have not verified these are right yet, so we're still using the estimating function.
    public static final String[] LFO_RATES_SYNC_OFF = 
        {
        "0.02 Hz", "0.02 Hz", "0.02 Hz", "0.02 Hz", "0.02 Hz", "0.02 Hz", "0.02 Hz", "0.02 Hz", "0.02 Hz", "0.02 Hz", "0.02 Hz", "0.02 Hz", "0.02 Hz", "0.02 Hz", "0.02 Hz", "0.02 Hz", "0.02 Hz", "0.02 Hz", "0.02 Hz", 
        "0.02 Hz", "0.02 Hz", "0.02 Hz", "0.02 Hz", "0.02 Hz", "0.02 Hz", "0.02 Hz", "0.02 Hz", "0.02 Hz", "0.03 Hz", "0.03 Hz", "0.03 Hz", "0.03 Hz", "0.03 Hz", "0.03 Hz", "0.03 Hz", "0.03 Hz", "0.03 Hz", "0.03 Hz", 
        "0.03 Hz", "0.03 Hz", "0.03 Hz", "0.03 Hz", "0.03 Hz", "0.03 Hz", "0.03 Hz", "0.03 Hz", "0.03 Hz", "0.03 Hz", "0.03 Hz", "0.03 Hz", "0.03 Hz", "0.03 Hz", "0.03 Hz", "0.03 Hz", "0.03 Hz", "0.03 Hz", "0.03 Hz", 
        "0.03 Hz", "0.03 Hz", "0.03 Hz", "0.03 Hz", "0.03 Hz", "0.03 Hz", "0.03 Hz", "0.03 Hz", "0.03 Hz", "0.03 Hz", "0.03 Hz", "0.04 Hz", "0.04 Hz", "0.04 Hz", "0.04 Hz", "0.04 Hz", "0.04 Hz", "0.04 Hz", "0.04 Hz", 
        "0.04 Hz", "0.04 Hz", "0.04 Hz", "0.04 Hz", "0.04 Hz", "0.04 Hz", "0.04 Hz", "0.04 Hz", "0.04 Hz", "0.04 Hz", "0.04 Hz", "0.04 Hz", "0.04 Hz", "0.04 Hz", "0.04 Hz", "0.04 Hz", "0.04 Hz", "0.04 Hz", "0.04 Hz", 
        "0.04 Hz", "0.05 Hz", "0.05 Hz", "0.05 Hz", "0.05 Hz", "0.05 Hz", "0.05 Hz", "0.05 Hz", "0.05 Hz", "0.05 Hz", "0.05 Hz", "0.05 Hz", "0.05 Hz", "0.05 Hz", "0.05 Hz", "0.05 Hz", "0.05 Hz", "0.05 Hz", "0.05 Hz", 
        "0.05 Hz", "0.05 Hz", "0.05 Hz", "0.05 Hz", "0.05 Hz", "0.05 Hz", "0.06 Hz", "0.06 Hz", "0.06 Hz", "0.06 Hz", "0.06 Hz", "0.06 Hz", "0.06 Hz", "0.06 Hz", "0.06 Hz", "0.06 Hz", "0.06 Hz", "0.06 Hz", "0.06 Hz", 
        "0.06 Hz", "0.06 Hz", "0.06 Hz", "0.07 Hz", "0.07 Hz", "0.07 Hz", "0.07 Hz", "0.07 Hz", "0.07 Hz", "0.07 Hz", "0.07 Hz", "0.07 Hz", "0.07 Hz", "0.07 Hz", "0.07 Hz", "0.07 Hz", "0.07 Hz", "0.07 Hz", "0.07 Hz", 
        "0.08 Hz", "0.08 Hz", "0.08 Hz", "0.08 Hz", "0.08 Hz", "0.08 Hz", "0.08 Hz", "0.08 Hz", "0.08 Hz", "0.08 Hz", "0.08 Hz", "0.08 Hz", "0.08 Hz", "0.08 Hz", "0.08 Hz", "0.08 Hz", "0.09 Hz", "0.09 Hz", "0.09 Hz", 
        "0.09 Hz", "0.09 Hz", "0.09 Hz", "0.09 Hz", "0.09 Hz", "0.09 Hz", "0.09 Hz", "0.09 Hz", "0.09 Hz", "0.10 Hz", "0.10 Hz", "0.10 Hz", "0.10 Hz", "0.10 Hz", "0.10 Hz", "0.10 Hz", "0.10 Hz", "0.10 Hz", "0.10 Hz", 
        "0.10 Hz", "0.10 Hz", "0.11 Hz", "0.11 Hz", "0.11 Hz", "0.11 Hz", "0.11 Hz", "0.11 Hz", "0.11 Hz", "0.11 Hz", "0.11 Hz", "0.11 Hz", "0.11 Hz", "0.11 Hz", "0.12 Hz", "0.12 Hz", "0.12 Hz", "0.12 Hz", "0.12 Hz", 
        "0.12 Hz", "0.12 Hz", "0.12 Hz", "0.13 Hz", "0.13 Hz", "0.13 Hz", "0.13 Hz", "0.13 Hz", "0.13 Hz", "0.13 Hz", "0.13 Hz", "0.14 Hz", "0.14 Hz", "0.14 Hz", "0.14 Hz", "0.14 Hz", "0.14 Hz", "0.14 Hz", "0.14 Hz", 
        "0.15 Hz", "0.15 Hz", "0.15 Hz", "0.15 Hz", "0.15 Hz", "0.15 Hz", "0.15 Hz", "0.15 Hz", "0.16 Hz", "0.16 Hz", "0.16 Hz", "0.16 Hz", "0.16 Hz", "0.16 Hz", "0.16 Hz", "0.16 Hz", "0.17 Hz", "0.17 Hz", "0.17 Hz", 
        "0.17 Hz", "0.17 Hz", "0.17 Hz", "0.17 Hz", "0.17 Hz", "0.18 Hz", "0.18 Hz", "0.18 Hz", "0.18 Hz", "0.19 Hz", "0.19 Hz", "0.19 Hz", "0.19 Hz", "0.19 Hz", "0.19 Hz", "0.19 Hz", "0.19 Hz", "0.20 Hz", "0.20 Hz", 
        "0.20 Hz", "0.20 Hz", "0.21 Hz", "0.21 Hz", "0.21 Hz", "0.21 Hz", "0.21 Hz", "0.21 Hz", "0.21 Hz", "0.21 Hz", "0.22 Hz", "0.22 Hz", "0.22 Hz", "0.22 Hz", "0.23 Hz", "0.23 Hz", "0.23 Hz", "0.23 Hz", "0.24 Hz", 
        "0.24 Hz", "0.24 Hz", "0.24 Hz", "0.25 Hz", "0.25 Hz", "0.25 Hz", "0.25 Hz", "0.25 Hz", "0.25 Hz", "0.25 Hz", "0.25 Hz", "0.26 Hz", "0.26 Hz", "0.26 Hz", "0.26 Hz", "0.27 Hz", "0.30 Hz", "0.30 Hz", "0.30 Hz", 
        "0.30 Hz", "0.30 Hz", "0.30 Hz", "0.30 Hz", "0.30 Hz", "0.30 Hz", "0.30 Hz", "0.30 Hz", "0.30 Hz", "0.30 Hz", "0.30 Hz", "0.30 Hz", "0.31 Hz", "0.31 Hz", "0.32 Hz", "0.32 Hz", "0.33 Hz", "0.33 Hz", "0.33 Hz", 
        "0.33 Hz", "0.34 Hz", "0.34 Hz", "0.34 Hz", "0.34 Hz", "0.35 Hz", "0.35 Hz", "0.35 Hz", "0.35 Hz", "0.36 Hz", "0.36 Hz", "0.36 Hz", "0.36 Hz", "0.37 Hz", "0.37 Hz", "0.38 Hz", "0.38 Hz", "0.39 Hz", "0.39 Hz", 
        "0.39 Hz", "0.39 Hz", "0.40 Hz", "0.40 Hz", "0.40 Hz", "0.40 Hz", "0.41 Hz", "0.41 Hz", "0.42 Hz", "0.42 Hz", "0.43 Hz", "0.43 Hz", "0.43 Hz", "0.43 Hz", "0.44 Hz", "0.44 Hz", "0.45 Hz", "0.45 Hz", "0.46 Hz", 
        "0.46 Hz", "0.47 Hz", "0.47 Hz", "0.48 Hz", "0.48 Hz", "0.48 Hz", "0.48 Hz", "0.49 Hz", "0.49 Hz", "0.50 Hz", "0.50 Hz", "0.51 Hz", "0.51 Hz", "0.52 Hz", "0.52 Hz", "0.53 Hz", "0.53 Hz", "0.54 Hz", "0.54 Hz", 
        "0.55 Hz", "0.55 Hz", "0.56 Hz", "0.56 Hz", "0.57 Hz", "0.57 Hz", "0.58 Hz", "0.58 Hz", "0.59 Hz", "0.59 Hz", "0.60 Hz", "0.60 Hz", "0.61 Hz", "0.61 Hz", "0.62 Hz", "0.62 Hz", "0.63 Hz", "0.63 Hz", "0.64 Hz", 
        "0.64 Hz", "0.65 Hz", "0.65 Hz", "0.66 Hz", "0.67 Hz", "0.68 Hz", "0.68 Hz", "0.69 Hz", "0.69 Hz", "0.70 Hz", "0.70 Hz", "0.71 Hz", "0.71 Hz", "0.72 Hz", "0.72 Hz", "0.73 Hz", "0.74 Hz", "0.75 Hz", "0.75 Hz", 
        "0.76 Hz", "0.77 Hz", "0.78 Hz", "0.78 Hz", "0.79 Hz", "0.79 Hz", "0.80 Hz", "0.80 Hz", "0.81 Hz", "0.82 Hz", "0.83 Hz", "0.83 Hz", "0.84 Hz", "0.85 Hz", "0.86 Hz", "0.86 Hz", "0.87 Hz", "0.88 Hz", "0.89 Hz", 
        "0.89 Hz", "0.90 Hz", "0.91 Hz", "0.92 Hz", "0.93 Hz", "0.94 Hz", "0.95 Hz", "0.96 Hz", "0.96 Hz", "0.97 Hz", "0.98 Hz", "0.99 Hz", "1.00 Hz", "1.01 Hz", "1.02 Hz", "1.03 Hz", "1.03 Hz", "1.04 Hz", "1.05 Hz", 
        "1.06 Hz", "1.07 Hz", "1.08 Hz", "1.09 Hz", "1.10 Hz", "1.11 Hz", "1.12 Hz", "1.13 Hz", "1.14 Hz", "1.15 Hz", "1.16 Hz", "1.17 Hz", "1.18 Hz", "1.19 Hz", "1.20 Hz", "1.21 Hz", "1.22 Hz", "1.23 Hz", "1.24 Hz", 
        "1.25 Hz", "1.27 Hz", "1.28 Hz", "1.29 Hz", "1.30 Hz", "1.31 Hz", "1.32 Hz", "1.33 Hz", "1.34 Hz", "1.36 Hz", "1.37 Hz", "1.38 Hz", "1.39 Hz", "1.41 Hz", "1.42 Hz", "1.43 Hz", "1.44 Hz", "1.46 Hz", "1.47 Hz", 
        "1.48 Hz", "1.49 Hz", "1.51 Hz", "1.52 Hz", "1.53 Hz", "1.54 Hz", "1.56 Hz", "1.57 Hz", "1.59 Hz", "1.60 Hz", "1.62 Hz", "1.63 Hz", "1.64 Hz", "1.65 Hz", "1.67 Hz", "1.68 Hz", "1.70 Hz", "1.71 Hz", "1.73 Hz", 
        "1.74 Hz", "1.76 Hz", "1.77 Hz", "1.79 Hz", "1.80 Hz", "1.82 Hz", "1.84 Hz", "1.86 Hz", "1.87 Hz", "1.89 Hz", "1.90 Hz", "1.92 Hz", "1.93 Hz", "1.95 Hz", "1.97 Hz", "1.99 Hz", "2.00 Hz", "2.02 Hz", "2.04 Hz", 
        "2.06 Hz", "2.07 Hz", "2.09 Hz", "2.11 Hz", "2.13 Hz", "2.15 Hz", "2.17 Hz", "2.19 Hz", "2.21 Hz", "2.23 Hz", "2.25 Hz", "2.27 Hz", "2.29 Hz", "3.31 Hz", "2.33 Hz", "2.35 Hz", "2.37 Hz", "2.39 Hz", "2.41 Hz", 
        "2.43 Hz", "2.45 Hz", "2.47 Hz", "2.49 Hz", "2.51 Hz", "2.54 Hz", "2.56 Hz", "2.58 Hz", "2.60 Hz", "2.63 Hz", "2.65 Hz", "2.67 Hz", "2.69 Hz", "2.72 Hz", "2.74 Hz", "2.77 Hz", "2.79 Hz", "2.82 Hz", "2.84 Hz", 
        "2.87 Hz", "2.89 Hz", "2.92 Hz", "2.94 Hz", "2.97 Hz", "3.00 Hz", "3.03 Hz", "3.05 Hz", "3.08 Hz", "3.10 Hz", "3.13 Hz", "3.15 Hz", "3.18 Hz", "3.21 Hz", "3.24 Hz", "3.27 Hz", "3.30 Hz", "3.33 Hz", "3.36 Hz", 
        "3.39 Hz", "3.42 Hz", "3.45 Hz", "3.48 Hz", "3.51 Hz", "3.54 Hz", "3.57 Hz", "3.60 Hz", "3.63 Hz", "3.66 Hz", "3.69 Hz", "3.73 Hz", "3.76 Hz", "3.79 Hz", "3.82 Hz", "3.86 Hz", "3.89 Hz", "3.93 Hz", "3.96 Hz", 
        "4.00 Hz", "4.03 Hz", "4.07 Hz", "4.10 Hz", "4.14 Hz", "4.17 Hz", "4.21 Hz", "4.25 Hz", "4.29 Hz", "4.32 Hz", "4.36 Hz", "4.40 Hz", "4.44 Hz", "4.48 Hz", "4.52 Hz", "4.56 Hz", "4.60 Hz", "4.64 Hz", "4.68 Hz", 
        "4.72 Hz", "4.76 Hz", "4.80 Hz", "4.84 Hz", "4.88 Hz", "4.93 Hz", "4.97 Hz", "5.01 Hz", "5.05 Hz", "5.10 Hz", "5.14 Hz", "5.19 Hz", "5.23 Hz", "5.28 Hz", "5.32 Hz", "5.37 Hz", "5.42 Hz", "5.47 Hz", "5.52 Hz", 
        "5.57 Hz", "5.62 Hz", "5.67 Hz", "5.72 Hz", "5.77 Hz", "5.82 Hz", "5.87 Hz", "5.92 Hz", "5.97 Hz", "6.02 Hz", "6.07 Hz", "6.12 Hz", "6.18 Hz", "6.23 Hz", "6.29 Hz", "6.34 Hz", "6.40 Hz", "6.45 Hz", "6.51 Hz", 
        "6.56 Hz", "6.62 Hz", "6.68 Hz", "6.74 Hz", "6.80 Hz", "6.86 Hz", "6.92 Hz", "6.98 Hz", "7.04 Hz", "7.10 Hz", "7.16 Hz", "7.23 Hz", "7.29 Hz", "7.36 Hz", "7.42 Hz", "7.49 Hz", "7.55 Hz", "7.62 Hz", "7.68 Hz", 
        "7.75 Hz", "7.82 Hz", "7.89 Hz", "7.96 Hz", "8.03 Hz", "8.10 Hz", "8.17 Hz", "8.24 Hz", "8.31 Hz", "8.38 Hz", "8.46 Hz", "8.53 Hz", "8.61 Hz", "8.68 Hz", "8.76 Hz", "8.83 Hz", "8.91 Hz", "8.99 Hz", "9.07 Hz", 
        "9.15 Hz", "9.23 Hz", "9.31 Hz", "9.39 Hz", "9.47 Hz", "9.56 Hz", "9.64 Hz", "9.72 Hz", "9.80 Hz", "9.89 Hz", "9.98 Hz", "10.07 Hz", "10.16 Hz", "10.25 Hz", "10.34 Hz", "10.43 Hz", "10.52 Hz", "10.61 Hz", "10.70 Hz", "10.80 Hz", 
        "10.89 Hz", "10.99 Hz", "11.08 Hz", "11.18 Hz", "11.28 Hz", "11.38 Hz", "11.48 Hz", "11.58 Hz", "11.68 Hz", "11.78 Hz", "11.88 Hz", "11.99 Hz", "12.09 Hz", "12.20 Hz", "12.30 Hz", "12.41 Hz", "12.52 Hz", "12.63 Hz", "12.74 Hz", "12.85 Hz", 
        "12.96 Hz", "13.08 Hz", "13.19 Hz", "13.31 Hz", "13.42 Hz", "13.54 Hz", "13.66 Hz", "13.78 Hz", "13.90 Hz", "14.02 Hz", "14.14 Hz", "14.27 Hz", "14.39 Hz", "14.52 Hz", "14.64 Hz", "14.77 Hz", "14.90 Hz", "15.03 Hz", "15.16 Hz", "15.30 Hz", 
        "15.43 Hz", "15.57 Hz", "15.70 Hz", "15.84 Hz", "15.98 Hz", "16.12 Hz", "16.26 Hz", "16.40 Hz", "16.54 Hz", "16.69 Hz", "16.83 Hz", "16.98 Hz", "17.13 Hz", "17.28 Hz", "17.43 Hz", "17.59 Hz", "17.74 Hz", "17.90 Hz", "18.05 Hz", "18.21 Hz", 
        "18.37 Hz", "18.53 Hz", "18.69 Hz", "18.86 Hz", "19.02 Hz", "19.19 Hz", "19.36 Hz", "19.53 Hz", "19.70 Hz", "19.87 Hz", "20.04 Hz", "20.22 Hz", "20.39 Hz", "20.57 Hz", "20.75 Hz", "20.93 Hz", "21.11 Hz", "21.30 Hz", "21.49 Hz", "21.68 Hz", 
        "21.87 Hz", "22.06 Hz", "22.25 Hz", "22.45 Hz", "22.64 Hz", "22.84 Hz", "23.04 Hz", "23.24 Hz", "23.44 Hz", "23.65 Hz", "23.86 Hz", "24.07 Hz", "24.28 Hz", "24.49 Hz", "24.70 Hz", "24.92 Hz", "25.14 Hz", "25.36 Hz", "25.58 Hz", "25.81 Hz", 
        "26.03 Hz", "26.26 Hz", "26.49 Hz", "26.72 Hz", "26.95 Hz", "27.19 Hz", "27.43 Hz", "27.67 Hz", "27.91 Hz", "28.15 Hz", "28.40 Hz", "28.65 Hz", "28.90 Hz", "29.15 Hz", "29.41 Hz", "29.67 Hz", "29.93 Hz", "30.19 Hz", "30.45 Hz", "30.72 Hz", 
        "30.99 Hz", "31.26 Hz", "31.53 Hz", "31.84 Hz", "32.08 Hz", "32.36 Hz", "32.64 Hz", "32.93 Hz", "33.22 Hz", "33.51 Hz", "33.80 Hz", "34.10 Hz", "34.40 Hz", "34.70 Hz", "35.00 Hz", "35.31 Hz", "35.62 Hz", "35.93 Hz", "36.25 Hz", "36.57 Hz", 
        "36.89 Hz", "37.21 Hz", "37.54 Hz", "37.87 Hz", "38.20 Hz", "38.53 Hz", "38.87 Hz", "39.21 Hz", "39.55 Hz", "39.89 Hz", "40.24 Hz", "40.60 Hz", "40.95 Hz", "41.31 Hz", "41.67 Hz", "42.04 Hz", "42.40 Hz", "42.77 Hz", "43.15 Hz", "43.53 Hz", 
        "43.91 Hz", "44.29 Hz", "44.68 Hz", "45.07 Hz", "45.46 Hz", "45.86 Hz", "46.26 Hz", "46.67 Hz", "47.08 Hz", "47.49 Hz", "47.91 Hz", "48.33 Hz", "48.75 Hz", "49.17 Hz", "49.60 Hz", "50.04 Hz", "50.47 Hz", "50.91 Hz", "51.36 Hz", "51.81 Hz", 
        "52.26 Hz", "52.72 Hz", "53.18 Hz", "53.65 Hz", "54.12 Hz", "54.59 Hz", "55.07 Hz", "55.56 Hz", "56.04 Hz", "56.53 Hz", "57.03 Hz", "57.53 Hz", "58.03 Hz", "58.53 Hz", "59.05 Hz", "59.57 Hz", "60.09 Hz", "60.61 Hz", "61.14 Hz", "61.68 Hz", 
        "62.22 Hz", "62.76 Hz", "63.31 Hz", "63.87 Hz", "64.42 Hz", "64.98 Hz", "65.55 Hz", "66.13 Hz", "66.71 Hz", "67.29 Hz", "67.88 Hz", "68.48 Hz", "69.08 Hz", "69.68 Hz", "70.29 Hz", "70.91 Hz", "71.53 Hz", "72.15 Hz", "72.79 Hz", "73.43 Hz", 
        "74.07 Hz", "74.71 Hz", "75.37 Hz", "76.03 Hz", "76.69 Hz", "77.36 Hz", "78.04 Hz", "78.73 Hz", "79.41 Hz", "80.10 Hz", "80.81 Hz", "81.52 Hz", "82.23 Hz", "82.94 Hz", "83.67 Hz", "84.41 Hz", "85.14 Hz", "85.88 Hz", "86.64 Hz", "87.40 Hz", 
        "88.16 Hz", "88.93 Hz", "89.71 Hz", "90.50 Hz", "91.29 Hz", "92.08 Hz", "92.89 Hz", "93.71 Hz", "94.53 Hz", "95.35 Hz", "96.19 Hz", "97.04 Hz", "97.88 Hz", "98.73 Hz", "99.60 Hz", "100.5 Hz", "101.3 Hz", "102.2 Hz", "103.1 Hz", "104.0 Hz", 
        "104.9 Hz", "105.9 Hz", "106.8 Hz", "107.7 Hz", "108.7 Hz", "109.6 Hz", "110.6 Hz", "111.6 Hz", "112.5 Hz", "113.5 Hz", "114.5 Hz", "115.5 Hz", "116.5 Hz", "117.5 Hz", "118.6 Hz", "119.6 Hz", "120.7 Hz", "121.7 Hz", "122.8 Hz", "123.8 Hz", 
        "124.9 Hz", "126.0 Hz", "127.1 Hz", "128.2 Hz", "129.4 Hz", "130.5 Hz", "131.6 Hz", "132.8 Hz", "133.9 Hz", "135.1 Hz", "136.3 Hz", "137.5 Hz", "138.7 Hz", "139.9 Hz", "141.1 Hz", "142.4 Hz", "143.6 Hz", "144.9 Hz", "146.1 Hz", "147.4 Hz", 
        "148.7 Hz", "150.0 Hz"
        };


    //// LFO RATE COMPUTATION

    // A close mapping of val (0...1024) to LFO rate values (0.02 ... 150.00 Hz).
    // The LFO rate appears to be nearly a perfect exponential increase, except
    // at the very low end where it's nonmonotonically off in the 0.03 Hz area.  
    // Would be nice to know what the real mapping is...
    public double lfoRate(int x)
        {
        return Math.pow(2.0, (1 + 0.012571 * x)) / 100.0;
        }




    /** Rounds d as usual to the nearest int, but with 0.5 rounded 
        towards the nearest even number rather than towards zero.  */
    // Large numbers of displayed values require rounding towards 0.5 on the Hydrasynth
    public static int roundEven(double d) 
        {
        // https://stackoverflow.com/questions/32971262/how-to-round-a-double-to-closest-even-number
        // is wrong.  I think below works for both positive and negative values
                
        int i = (int) d;
        double rem = (d - i);
        if (rem == 0.5 || rem == -0.5)
            {
            if ((i & 1) == 0)       // even
                return i;
            else if (i < 0)
                return i - 1;
            else
                return i + 1;
            }
        else return (int)Math.round(d);
        }
        
    public TextLabel sendLabel = new TextLabel("  ");

    public ASMHydrasynth()
        {
        model.setFixer(this);
        
        if (parametersToIndex == null)
            {
            parametersToIndex = new HashMap();
            for(int i = 0; i < parameters.length; i++)
                {
                parametersToIndex.put(parameters[i], Integer.valueOf(i));
                }

            nrpnToIndex = new HashMap();
            for(int i = 0; i < nrpn.length; i++)
                {
                if (nrpnToIndex.get(nrpn[i]) == null)   // it's not already there; we want the first one
                    {
                    nrpnToIndex.put(nrpn[i], Integer.valueOf(i));
                    }
                }

            MOD_DESTINATION_NRPN_VALUES_TO_INDEX = new HashMap();
            for(int i = 0; i < MOD_DESTINATION_NRPN_VALUES.length; i++)
                {
                MOD_DESTINATION_NRPN_VALUES_TO_INDEX.put(MOD_DESTINATION_NRPN_VALUES[i], Integer.valueOf(i));
                }
                                        
            MOD_SOURCE_NRPN_VALUES_TO_INDEX = new HashMap();
            for(int i = 0; i < MOD_SOURCE_NRPN_VALUES.length; i++)
                {
                MOD_SOURCE_NRPN_VALUES_TO_INDEX.put(MOD_SOURCE_NRPN_VALUES[i], Integer.valueOf(i));
                }
            }

/*
  String str = getLastX("Deluxe", getSynthClassName(), true);
  if (str == null)
  deluxe = false;            // default is false
  else if (str.equalsIgnoreCase("true"))
  deluxe = true;
  else
  deluxe = false;
*/

        String str = getLastX("IgnoreParametersFromSynth", getSynthClassName(), true);
        if (str == null)
            ignoreParametersFromSynth = true;            // default is true
        else if (str.equalsIgnoreCase("true"))
            ignoreParametersFromSynth = true;
        else
            ignoreParametersFromSynth = false;

        str = getLastX("DisallowCCMutation", getSynthClassName(), true);
        if (str == null)
            disallowCCMutation = true;            // default is true
        else if (str.equalsIgnoreCase("true"))
            disallowCCMutation = true;
        else
            disallowCCMutation = false;

// Looks like this was fixed in 2.0.0
/*
  str = getLastX("SendArpTapTrig", getSynthClassName(), true);
  if (str == null)
  sendArpTapTrig = false;            // default is false
  else if (str.equalsIgnoreCase("true"))
  sendArpTapTrig = true;
  else
  sendArpTapTrig = false;
*/

        str = getLastX("LockUserLFOSteps", getSynthClassName(), true);
        if (str == null)
            lockUserLFOSteps = false;            // default is false
        else if (str.equalsIgnoreCase("true"))
            lockUserLFOSteps = true;
        else
            lockUserLFOSteps = false;


        /// SOUND PANEL
                
        JComponent soundPanel = new SynthPanel(this);
        VBox vbox = new VBox();
        
        // This little dance allows the Glide category
        // to line up with the categories below it, at least on the Mac.  
        // It was driving Andrew Macaulay nuts.
        
        HBox hbox = new HBox();
        HBox inner = new HBox();
        inner.add(addNameGlobal(Style.COLOR_GLOBAL()));
        inner.addLast(addVoice(Style.COLOR_B()));
        VBox innerV = new VBox();
        innerV.add(inner);
        hbox.add(innerV);
        hbox.addLast(addGlide(Style.COLOR_C()));
        vbox.add(hbox);

        JComponent comp = null;
        hbox = new HBox();
        hbox.add(comp = addOscillator(1, Style.COLOR_A()));
        hbox.addLast(addRingMod(Style.COLOR_B()));
        vbox.add(hbox);
        
        innerV.add(Strut.makeHorizontalStrut(comp));
        
        hbox = new HBox();
        hbox.add(addOscillator(2, Style.COLOR_A()));
        hbox.addLast(addNoise(Style.COLOR_B()));
        vbox.add(hbox);
                
        hbox = new HBox();
        hbox.add(addOscillator(3, Style.COLOR_A()));
        hbox.addLast(addVibrato(Style.COLOR_B()));
        vbox.add(hbox);
                
        vbox.add(addMixer(Style.COLOR_B()));
        
        hbox = new HBox();
        hbox.addLast(addArp(Style.COLOR_C()));
        vbox.add(hbox);
                
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Osc", soundPanel);
                
        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        vbox.add(addMutant(1, Style.COLOR_A()));
        vbox.add(addMutant(2, Style.COLOR_A()));
        vbox.add(addMutant(3, Style.COLOR_B()));
        vbox.add(addMutant(4, Style.COLOR_B()));
        vbox.add(addScale(Style.COLOR_C()));
        vbox.add(addRibbon(Style.COLOR_C()));
                
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Mutant Ribbon Scale", soundPanel);
        
        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        vbox.add(addFilter(1, Style.COLOR_B()));
        hbox = new HBox();
        hbox.add(addFilter(2, Style.COLOR_B()));
        hbox.addLast(addFilters(Style.COLOR_B()));
        vbox.add(hbox);
        hbox = new HBox();
        hbox.add(addAmp(Style.COLOR_C()));
        hbox.addLast(addVoiceModulation(Style.COLOR_C()));
        vbox.add(hbox);
        vbox.add(addFX(true, Style.COLOR_A()));
        hbox = new HBox();
        hbox.add(addDelay(Style.COLOR_A()));
        hbox.addLast(addReverb(Style.COLOR_A()));
        vbox.add(hbox);
        vbox.add(addFX(false, Style.COLOR_A()));

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Filter Amp FX", soundPanel);
        
        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        vbox.add(addEnvelope(1, Style.COLOR_A()));
        vbox.add(addEnvelope(2, Style.COLOR_B()));
        vbox.add(addEnvelope(3, Style.COLOR_A()));
        vbox.add(addEnvelope(4, Style.COLOR_B()));
        vbox.add(addEnvelope(5, Style.COLOR_A()));

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Env", soundPanel);

        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        vbox.add(addLFO(1, Style.COLOR_B()));
        vbox.add(addLFO(2, Style.COLOR_A()));
        vbox.add(addLFO(3, Style.COLOR_B()));
        vbox.add(addLFO(4, Style.COLOR_A()));
        vbox.add(addLFO(5, Style.COLOR_B()));

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("LFO", soundPanel);

        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        //vbox.add(addLFO(1, Style.COLOR_A()));
        vbox.add(addLFOSteps(1, Style.COLOR_B()));

        soundPanel.add(vbox, BorderLayout.CENTER);
        ((SynthPanel)soundPanel).makePasteable("lfo");
        addTab("Steps 1", soundPanel);

        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        //vbox.add(addLFO(2, Style.COLOR_B()));
        vbox.add(addLFOSteps(2, Style.COLOR_A()));

        soundPanel.add(vbox, BorderLayout.CENTER);
        ((SynthPanel)soundPanel).makePasteable("lfo");
        addTab("2", soundPanel);

        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        //vbox.add(addLFO(3, Style.COLOR_A()));
        vbox.add(addLFOSteps(3, Style.COLOR_B()));

        soundPanel.add(vbox, BorderLayout.CENTER);
        ((SynthPanel)soundPanel).makePasteable("lfo");
        addTab("3", soundPanel);

        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        //vbox.add(addLFO(4, Style.COLOR_B()));
        vbox.add(addLFOSteps(4, Style.COLOR_A()));

        soundPanel.add(vbox, BorderLayout.CENTER);
        ((SynthPanel)soundPanel).makePasteable("lfo");
        addTab("4", soundPanel);

        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        //vbox.add(addLFO(5, Style.COLOR_A()));
        vbox.add(addLFOSteps(5, Style.COLOR_B()));

        soundPanel.add(vbox, BorderLayout.CENTER);
        ((SynthPanel)soundPanel).makePasteable("lfo");
        addTab("5", soundPanel);

        soundPanel = new SynthPanel(this);
        hbox = new HBox();
        vbox = new VBox();
        vbox.add(addMacro(1, Style.COLOR_A()));
        vbox.add(addMacro(3, Style.COLOR_B()));
        hbox.add(vbox);
        vbox = new VBox();
        
        vbox.add(addMacro(2, Style.COLOR_B()));
        vbox.add(addMacro(4, Style.COLOR_A()));
        hbox.addLast(vbox);

        soundPanel.add(hbox, BorderLayout.CENTER);
        
        addTab("Macro 1-4", soundPanel);
        
        soundPanel = new SynthPanel(this);
        hbox = new HBox();
        vbox = new VBox();
        vbox.add(addMacro(5, Style.COLOR_B()));
        vbox.add(addMacro(7, Style.COLOR_A()));
        hbox.add(vbox);
        vbox = new VBox();
        
        vbox.add(addMacro(6, Style.COLOR_A()));
        vbox.add(addMacro(8, Style.COLOR_B()));
        hbox.addLast(vbox);

        soundPanel.add(hbox, BorderLayout.CENTER);
        
        addTab("5-8", soundPanel);
        
        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        vbox.add(addMatrix(Style.COLOR_A()));
        soundPanel.add(vbox, BorderLayout.CENTER);
        
        addTab("Matrix", soundPanel);
        soundPanel = new SynthPanel(this);

        loadDefaults();        
        }
                
    public String getDefaultResourceFileName() { return "ASMHydrasynth.init"; }
    public String getHTMLResourceFileName() { return "ASMHydrasynth.html"; }

    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        JComboBox bank = new JComboBox(BANKS);
        bank.setEditable(false);
        bank.setMaximumRowCount(32);
        bank.setSelectedIndex(model.get("bank"));
                
        JTextField number = new SelectedTextField("" + (model.get("number") + 1), 3);
                
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
                showSimpleError(title, "The Patch Number must be an integer 1 ... 128");
                continue;
                }
            if (n < 1 || n > 128)
                {
                showSimpleError(title, "The Patch Number must be an integer 1 ... 128");
                continue;
                }
                                
            change.set("bank", bank.getSelectedIndex());
            change.set("number", n - 1);
                        
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
        HBox outer = new HBox();
        HBox hbox = new HBox();

        VBox vbox = new VBox();
        
        VBox inner = new VBox();
        comp = new PatchDisplay(this, 6);
        inner.add(comp);
        HBox inner2 = new HBox();
        sendLabel.setBorder(Style.SEND_BORDER());
        inner2.add(sendLabel);
        inner2.addLast(Stretch.makeHorizontalStretch());
        inner.add(inner2);
        hbox.add(inner);
 
        params = CATEGORIES;
        comp = new Chooser("Category", this, "category", params);
        VBox top = new VBox();
        top.add(comp);
        hbox.add(top);

        vbox.add(hbox);

        comp = new StringComponent("Patch Name", this, "name", 16, "Patch name must be up to 16 ASCII characters,\nnot including control characters, DELETE,\nor any of the following:  \" * \\ | / < > ? ; | ~")
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
        vbox.add(comp);
        
        outer.add(vbox);
        
        // we use a special dial color here so as not to overwhelm the colored text
        comp = new LabelledDial("Color", this, "color", Style.DIAL_UNSET_COLOR(), 1, 32)
            {
            public String map(int value)
                {
                return "<html><font color=" + COLORS_HTML[value - 1] + ">" + value + "</font></html>";
                }
            };
        outer.add(comp);
        

        globalCategory.add(outer, BorderLayout.WEST);
        return globalCategory;
        }

    boolean invalidChar(char c)
        {
        for(int i = 0; i < INVALID_PATCH_NAME_CHARS.length; i++)
            {
            if (c == INVALID_PATCH_NAME_CHARS[i]) return true;
            }
        return false;
        }
                
    public static final int MAXIMUM_NAME_LENGTH = 16;
    public String revisePatchName(String name)
        {
        name = super.revisePatchName(name);  // trim first time
        if (name.length() > MAXIMUM_NAME_LENGTH)
            name = name.substring(0, MAXIMUM_NAME_LENGTH);
        
        StringBuffer nameb = new StringBuffer(name);                            
        for(int i = 0 ; i < nameb.length(); i++)
            {
            char c = nameb.charAt(i);
            if (c < ' ' || c > 126 || invalidChar(c))
                nameb.setCharAt(i, ' ');
            }
        name = nameb.toString();
        return super.revisePatchName(name);  // trim again
        }        

    /** Verify that all the parameters are within valid values, and tweak them if not. */
    public void revise()
        {
        if (model.get("arptempo") < 300)        // minimum value is 30.0, but it's often set to 0
            model.set("arptempo", 1200);    // set to 120, why not.

        if (model.get("ribbonscalekeylock") == 0)
            model.set("ribbonscalekeylock", 1);     // seems reasonable
        
        if (model.get("scalekeylock") == 0)
            model.set("scalekeylock", 1);   // seems reasonable

        if (model.get("lfo1steps") < 2) // set by the Hydrasynth when disabled
            model.set("lfo1steps", 2);
        if (model.get("lfo2steps") < 2) // set by the Hydrasynth when disabled
            model.set("lfo2steps", 2);
        if (model.get("lfo3steps") < 2) // set by the Hydrasynth when disabled
            model.set("lfo3steps", 2);
        if (model.get("lfo4steps") < 2) // set by the Hydrasynth when disabled
            model.set("lfo4steps", 2);
        if (model.get("lfo5steps") < 2) // set by the Hydrasynth when disabled
            model.set("lfo5steps", 2);
        
        if (model.get("filter1type") == 16) // vowel is sometimes returned as 16 incorrectly
            model.set("filter1type", 10);
        
        // check the easy stuff -- out of range parameters
        super.revise();
        
        String nm = model.get("name", "Untitled");
        String newnm = revisePatchName(nm);
        if (!nm.equals(newnm))
            model.set("name", newnm);
            
        if (lockAllLFOSteps)
            {
            for(int lfo = 1; lfo <= 5; lfo++)
                {
                for(int step = 1; step <= 64; step++)
                    {
                    String key = "lfo" + lfo + "step" + step;
                    model.set(key, lockLFOStep(model.get(key)));
                    }
                }
            }        
        }

    public JComponent addVoiceModulation(Color color)
        {
        Category category = new Category(this, "Voice Modulation", color);
        category.makeDistributable("voice");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
 
        for(int i = 1; i <= 8; i++)
            {
            comp = new LabelledDial("Voice " + i, this, "voicemodulation" + i, color, 0, 256)
                {
                public boolean isSymmetric() { return true; }
                public String map(int value)
                    {
                    return "" + (value - 128);
                    }
                };
            hbox.add(comp);
            }
                        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    JComponent pastMode = null;
    HBox _waveScanBox = null;
    public JComponent addOscillator(final int osc, Color color)
        {
        Category category = new Category(this, "Oscillator " + osc, color);
        category.makePasteable("osc");
        category.makeDistributable("osc");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        
        final HBox waveScanBox = new HBox();
        if (osc < 3)
            {
            _waveScanBox = waveScanBox;
            comp = new LabelledDial("WaveScan", this, "osc" + osc + "wavscan", color, 0, 1024)
                {
                public String map(int value)
                    {
                    int v = value * 8;
                    // dividing 8192 by 117.03 roughly cuts into 70 pieces
                    return String.format("%1.1f", ((roundEven(v / 117.03) + 10) / 10.0));
                    }
                };
            waveScanBox.add(comp);
            for(int i = 1; i <= 4; i++)
                {
                vbox = new VBox();
                params = (i == 1 ? OSC_WAVES : OSC_WAVES_OFF_SILENCE);
                comp = new Chooser("WaveScan Wave " + i, this, "osc" + osc + "wavscanwave" + i, params);
                vbox.add(comp);

                params = OSC_WAVES_OFF_SILENCE;
                comp = new Chooser("WaveScan Wave " + (i + 4), this, "osc" + osc + "wavscanwave" + (i + 4), params);
                vbox.add(comp);
                waveScanBox.add(vbox);
                }
            }
                

        final HBox waveBox = new HBox();
        comp = new LabelledDial("Wave", this, "osc" + osc + "type", color, 0, OSC_WAVES.length - 1, -1);
        waveBox.add(comp);
        vbox = new VBox();
        params = OSC_WAVES;
        comp = new Chooser("Wave", this, "osc" + osc + "type", params);
        vbox.add(comp);
        waveBox.add(vbox);
        waveBox.add(Strut.makeHorizontalStrut(8));
        vbox = new VBox();
        IconDisplay icons = new IconDisplay(null, waves, this, "osc" + osc + "type", 110, 52)
            {
            public Dimension getPreferredSize()
                {
                Dimension d = super.getPreferredSize();
                return new Dimension(d.width, d.height+2);              // compensate for border
                }
            };
        icons.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        vbox.add(icons);
        waveBox.add(vbox);
        waveBox.add(Strut.makeHorizontalStrut(8));
        final VBox outerWaveBox = new VBox();
        outerWaveBox.add(waveBox);
        outerWaveBox.add(Strut.makeStrut(_waveScanBox, false, true));
        
        vbox = new VBox();
        if (osc == 3)
            {
            vbox.add(Strut.makeStrut(pastMode, false, true));
            }
        else
            {
            params = OSC_MODES;
            comp = new Chooser("Mode", this, "osc" + osc + "mode", params)
                {
                public void update(String key, Model model)
                    {
                    super.update(key, model);
                    if (osc != 3)
                        {
                        hbox.remove(outerWaveBox);
                        hbox.remove(waveScanBox);
                        if (model.get(key) == 0)        // wave
                            {
                            hbox.addLast(outerWaveBox);
                            }
                        else
                            {
                            hbox.addLast(waveScanBox);
                            }
                        hbox.revalidate();
                        hbox.repaint();
                        }
                    }
                };
            pastMode = comp;
            vbox.add(comp);
            }
                        
        params = OSC_BIT_REDUCTIONS;
        comp = new Chooser("Bit Reduction", this, "osc" + osc + "bitreduction", params);
        model.setMetricMinMax("osc" + osc + "bitreduction", 0, OSC_BIT_REDUCTIONS.length - 1);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Semitones", this, "osc" + osc + "semi", color, -36, 36);
        hbox.add(comp);

        comp = new LabelledDial("Cents", this, "osc" + osc + "cent", color, -50, 50);
        hbox.add(comp);

        comp = new LabelledDial("Keytrack", this, "osc" + osc + "keytrack", color, 0, 200)      
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                return "" + value + "%";
                }
            };
        hbox.add(comp);
        
        hbox.add(outerWaveBox);
        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addRingMod(Color color)
        {
        Category category = new Category(this, "Ring Modulation", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = RING_MOD_SOURCES;
        comp = new Chooser("Source 1", this, "ringmodsource1", params);
        vbox.add(comp);

        params = RING_MOD_SOURCES;
        comp = new Chooser("Source 2", this, "ringmodsource2", params);
        vbox.add(comp);

        hbox.add(vbox);

        comp = new LabelledDial("Depth", this, "ringmoddepth", color, 0, 1024)
            {
            public String map(int value)
                {
                int v = value * 8;
                // dividing 8192 by 6.4 cuts into 128 pieces
                return String.format("%1.1f", (roundEven(v / 6.4) / 10.0));
                }
            };
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addNoise(Color color)
        {
        Category category = new Category(this, "Noise", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = NOISE_TYPES;
        comp = new Chooser("Type", this, "noisetype", params);
        vbox.add(comp);

        hbox.add(vbox);
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
                
    public JComponent addGlide(Color color)
        {
        Category category = new Category(this, "Glide", color);

        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();
        
        model.set("voiceglidecurve", 0);                // the purpose of this is to make sure that voiceglidecurve sync is FIRST in the mutation list

        final CheckBox legato = new CheckBox("Legato", this, "voiceglidelegato");

        HBox curvetime = new HBox();
        final LabelledDial curve = new LabelledDial("Curve", this, "voiceglidecurve", color, 0, 128)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val)
                {
                // The &nbsp; are because centering (on the Mac anyway) appear to be controlled by
                // the widest object, which by default is the number, and when that number goes from, 
                // say, -10 to -9, it shifts its centering, which for some buggy reason causes the
                // text above it to also shift perceptibly.  So we make the text above wider so it's
                // in the driver's seat instead. 
                if (val < 64) return "<html><center><font size=-2>&nbsp;&nbsp;Log&nbsp;&nbsp;</font><br>" + (val - 64) + "</center></html>";
                else if (val > 64) return "<html><center><font size=-2>&nbsp;&nbsp;Exp&nbsp;&nbsp;</font><br>" + (val - 64) + "</center></html>"; 
                else return "Linear";
                }
            };
        curvetime.add(curve);
        final LabelledDial time = new LabelledDial("Time", this, "voiceglidetime", color, 0, 127);
        curvetime.add(time);

        final VBox vbox = new VBox();
        params = GLIDE_MODES;
        comp = new Chooser("Mode", this, "voiceglide", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                if (model.get(key) == 0)
                    {
                    vbox.remove(legato);
                    curvetime.removeAll();
                    curvetime.add(Strut.makeStrut(curve));
                    curvetime.add(Strut.makeStrut(time));
                    }
                else
                    {
                    vbox.add(legato);
                    curvetime.removeAll();
                    curvetime.add(curve);
                    curvetime.add(time);
                    }
                vbox.revalidate();
                curvetime.revalidate();
                vbox.repaint();
                curvetime.repaint();
                }
            };
        vbox.add(comp);

        vbox.add(legato);
        hbox.add(vbox);
        hbox.add(curvetime);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addVibrato(Color color)
        {
        Category category = new Category(this, "Vibrato", color);

        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();
        
        model.set("voicevibratobpmsync", 0);            // the purpose of this is to make sure that bpm sync is FIRST in the mutation list
                
        final LabelledDial off = new LabelledDial("Rate", this, "voicevibratoratesyncoff", color, 0, 127)
            {
            public String map(int value)
                {
                if (value < 30) return String.format("%1.2f", 0.3 + value * 0.01);
                else if (value < 50) return String.format("%1.2f", 0.6 + (value - 30) * 0.02);
                else if (value < 70) return String.format("%1.2f", 1.0 + (value - 50) * 0.04);
                else if (value < 102) return String.format("%1.2f", 1.8 + (value - 70) * 0.10);
                else return String.format("%1.2f", 5.0 + (value - 102) * 0.20);
                }
            };

        final LabelledDial on = new LabelledDial("Rate", this, "voicevibratoratesyncon", color, 0, VIBRATO_RATES_SYNC_ON.length - 1)
            {
            public String map(int value)
                {
                return VIBRATO_RATES_SYNC_ON[value];
                }
            };

        VBox vbox = new VBox();
        comp = new CheckBox("BPM Sync", this, "voicevibratobpmsync")
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                hbox.remove(off);
                hbox.remove(on);
                if (model.get(key) == 0)
                    hbox.add(off);
                else
                    hbox.add(on);
                hbox.revalidate();
                hbox.repaint();
                }
            };
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Amount", this, "voicevibratoamount", color, 0, 120)
            {
            public String map(int value)
                {
                return ("" + (value / 10.0));
                }
            };
        hbox.add(comp);

        hbox.add(off);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
                

    public JComponent addVoice(Color color)
        {
        Category category = new Category(this, "Voice", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox(); 
        params = STEREO_MODES;
        comp = new Chooser("Stereo Mode", this, "voicestereomode", params);
        vbox.add(comp);

        params = POLYPHONY_TYPES;
        comp = new Chooser("Polyphony", this, "voicepolyphony", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new CheckBox("Snap", this, "voicesnap");
        vbox.add(comp);
        vbox = new VBox();
        
        params = SUSTAIN_PEDAL_MODES;
        comp = new Chooser("Sustain Pedal", this, "voicesustain", params);
        vbox.add(comp);
        
        comp = new CheckBox("Random Phase", this, "voicerandomphase");
        vbox.add(comp);

        comp = new CheckBox("Warm Mode", this, "voicewarmmode");
        vbox.add(comp);

        hbox.add(vbox);

        comp = new LabelledDial("Pitch Bend", this, "voicepitchbend", color, 0, 24);
        ((LabelledDial)comp).addAdditionalLabel("Range");
        hbox.add(comp);

        comp = new LabelledDial("Detune", this, "voicedetune", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Stereo", this, "voicestereowidth", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Width");
        hbox.add(comp);

        comp = new LabelledDial("Analog", this, "voiceanalogfeel", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Feel");
        hbox.add(comp);

        comp = new LabelledDial("Density", this, "voicedensity", color, 1, 8);
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addMixer(Color color)
        {
        Category category = new Category(this, "Mixer", color);
        category.makePasteable("mixer");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        comp = new LabelledDial("Osc 1", this, "mixerosc1vol", color, 0, 1024)
            {
            public String map(int value)
                {
                int v = value * 8;
                // dividing 8192 by 6.4 cuts into 128 pieces
                return String.format("%1.1f", (roundEven(v / 6.4) / 10.0));
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Volume");
        hbox.add(comp);

        comp = new LabelledDial("Osc 2", this, "mixerosc2vol", color, 0, 1024)
            {
            public String map(int value)
                {
                int v = value * 8;
                // dividing 8192 by 6.4 cuts into 128 pieces
                return String.format("%1.1f", (roundEven(v / 6.4) / 10.0));
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Volume");
        hbox.add(comp);

        comp = new LabelledDial("Osc 3", this, "mixerosc3vol", color, 0, 1024)
            {
            public String map(int value)
                {
                int v = value * 8;
                // dividing 8192 by 6.4 cuts into 128 pieces
                return String.format("%1.1f", (roundEven(v / 6.4) / 10.0));
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Volume");
        hbox.add(comp);

        comp = new LabelledDial("Noise", this, "mixernoisevol", color, 0, 1024)
            {
            public String map(int value)
                {
                int v = value * 8;
                // dividing 8192 by 6.4 cuts into 128 pieces
                return "" + (roundEven(v / 6.4) / 10.0);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Volume");
        hbox.add(comp);

        comp = new LabelledDial("Ring Mod", this, "mixerringmodvol", color, 0, 1024)
            {
            public String map(int value)
                {
                int v = value * 8;
                // dividing 8192 by 6.4 cuts into 128 pieces
                return String.format("%1.1f", (roundEven(v / 6.4) / 10.0));
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Volume");
        hbox.add(comp);

        hbox.add(Strut.makeHorizontalStrut(16));

        comp = new LabelledDial("Osc 1", this, "mixerosc1pan", color, 0, 1024)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                double v = value * 8;
                // dividing 8192 by 6.4 cuts into 128 pieces
                v = (roundEven(v / 6.4) / 10.0);
                if (v < 64) return "<" + String.format("%1.1f", (64 - v));
                else if (v > 64) return "" + String.format("%1.1f", (v - 64)) + ">";
                else return "--";
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Pan");
        hbox.add(comp);

        comp = new LabelledDial("Osc 2", this, "mixerosc2pan", color, 0, 1024)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                double v = value * 8;
                // dividing 8192 by 6.4 cuts into 128 pieces
                v = (roundEven(v / 6.4) / 10.0);
                if (v < 64) return "<" + String.format("%1.1f", (64 - v));
                else if (v > 64) return "" + String.format("%1.1f", (v - 64)) + ">";
                else return "--";
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Pan");
        hbox.add(comp);

        comp = new LabelledDial("Osc 3", this, "mixerosc3pan", color, 0, 1024)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                double v = value * 8;
                // dividing 8192 by 6.4 cuts into 128 pieces
                v = (roundEven(v / 6.4) / 10.0);
                if (v < 64) return "<" + String.format("%1.1f", (64 - v));
                else if (v > 64) return "" + String.format("%1.1f", (v - 64)) + ">";
                else return "--";
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Pan");
        hbox.add(comp);

        comp = new LabelledDial("Noise", this, "mixernoisepan", color, 0, 1024)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                double v = value * 8;
                // dividing 8192 by 6.4 cuts into 128 pieces
                v = (roundEven(v / 6.4) / 10.0);
                if (v < 64) return "<" + String.format("%1.1f", (64 - v));
                else if (v > 64) return "" + String.format("%1.1f", (v - 64)) + ">";
                else return "--";
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Pan");
        hbox.add(comp);

        comp = new LabelledDial("Ring Mod", this, "mixerringmodpan", color, 0, 1024)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                double v = value * 8;
                // dividing 8192 by 6.4 cuts into 128 pieces
                v = (roundEven(v / 6.4) / 10.0);
                if (v < 64) return "<" + String.format("%1.1f", (64 - v));
                else if (v > 64) return "" + String.format("%.1f", (v - 64)) + ">";
                else return "--";
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Pan");
        hbox.add(comp);

        hbox.add(Strut.makeHorizontalStrut(16));

        comp = new LabelledDial("Osc 1", this, "mixerosc1filterratio", color, 0, 1024)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                double v = value * 8;
                // dividing 8192 by 81.92 cuts into 100 pieces
                value = (int)(v / 81.92);
                return "" + (100 - value) + ":" + value;
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Filter Ratio");
        hbox.add(comp);

        comp = new LabelledDial("Osc 2", this, "mixerosc2filterratio", color, 0, 1024)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                double v = value * 8;
                // dividing 8192 by 81.92 cuts into 100 pieces
                value = (int)(v / 81.92);
                return "" + (100 - value) + ":" + value;
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Filter Ratio");
        hbox.add(comp);

        comp = new LabelledDial("Osc 3", this, "mixerosc3filterratio", color, 0, 1024)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                double v = value * 8;
                // dividing 8192 by 81.92 cuts into 100 pieces
                value = (int)(v / 81.92);
                return "" + (100 - value) + ":" + value;
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Filter Ratio");
        hbox.add(comp);

        comp = new LabelledDial("Noise", this, "mixernoisefilterratio", color, 0, 1024)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                double v = value * 8;
                // dividing 8192 by 81.92 cuts into 100 pieces
                value = (int)(v / 81.92);
                return "" + (100 - value) + ":" + value;
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Filter Ratio");
        hbox.add(comp);

        comp = new LabelledDial("Ring Mod", this, "mixerringmodfilterratio", color, 0, 1024)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                double v = value * 8;
                // dividing 8192 by 81.92 cuts into 100 pieces
                value = (int)(v / 81.92);
                return "" + (100 - value) + ":" + value;
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Filter Ratio");
        hbox.add(comp);


        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addScale(Color color)
        {
        Category category = new Category(this, "Scale", color);
        category.makeDistributable("scalenote");

        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();

        final HBox scaleEditBox = new HBox();
                
        for(int i = 2; i <= 8; i++)
            {
            comp = new LabelledDial("Note " + i, this, "scalenote" + i, color, 0, 12)
                {
                public String map(int value)
                    {
                    if (value == 0) return "Off";
                    else return NOTES[(model.get("scalekeylock") + value - 2) % 12];
                    }
                };
            scaleEditBox.add(comp);
            }
                
        final JComponent scaleEditBoxStrut = Strut.makeStrut(scaleEditBox);
                        
        LabelledDial keyLock = new LabelledDial("Key Lock", this, "scalekeylock", color, 1, 12)
            {
            public String map(int value)
                {
                return NOTES[value - 1];
                }
                                
            public void update(String key, Model model)
                {
                super.update(key, model);
                scaleEditBox.repaint();
                }
            };
                        
        final JComponent keyLockStrut = Strut.makeStrut(keyLock);

        VBox vbox = new VBox();
        params = SCALES;
        comp = new Chooser("Type", this, "scaletype", params)   
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                int val = model.get(key);
                hbox.remove(keyLock);
                hbox.remove(scaleEditBox);
                hbox.remove(keyLockStrut);
                hbox.remove(scaleEditBoxStrut);
                
                if (val != 1 && val <= 38)      // not chromatic or microtonal
                    {
                    hbox.add(keyLock);
                    if (val == 0)           // scale edit
                        {
                        hbox.addLast(scaleEditBox);
                        }
                    else
                        {
                        hbox.addLast(scaleEditBoxStrut);
                        }
                    }
                else
                    {
                    hbox.add(keyLockStrut);
                    hbox.addLast(scaleEditBoxStrut);
                    }
                hbox.revalidate();
                hbox.repaint();
                }
            };
        vbox.add(comp);
        hbox.add(vbox);
        // At this point the scaletype has added the keylock in the wrong place.
        // This is because we updated the chooser before we added the chooser.
        // We need to remove it and re-add it
        hbox.remove(keyLock);
        hbox.add(keyLock);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    String formatRatio(double val)
        {
        val = roundEven(val * 1000);
        int v = (int) val;
        int d = (v % 1000);
        String s = null;
        return "" + (v / 1000) + "." + String.format("%03d", (v % 1000));
        }
        
    public JComponent addMutant(int mut, Color color)
        {
        Category category = new Category(this, "Mutant " + mut + "   (Oscillator " + (mut < 3 ? "1)" : "2)"), color);
        category.makePasteable("mutant");
        category.makeDistributable("mutant");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        model.set("mutant" + mut + "mode", 0);          // the purpose of this is to make sure that mode is FIRST in the mutation list

        params = MUTANT_SOURCES_FM_LIN;
        final Chooser sourcesFMLin = new Chooser("Source", this, "mutant" + mut + "sourcefmlin", params);
 
        params = MUTANT_SOURCES_OSC_SYNC;
        final Chooser sourcesOscSync = new Chooser("Source", this, "mutant" + mut + "sourceoscsync", params);
 
        final JComponent sourceStrut = Strut.makeStrut(sourcesOscSync, false, false);
                
        final LabelledDial ratio = new LabelledDial("Ratio", this, "mutant" + mut + "ratio", color, 0, 1024)
            {
            public String map(int value)
                {
                int v = value;
                if (v == 1024) return "64.000";
                if (v < 64) return formatRatio((v - 0) * 0.0013015625 + 0.250);
                else if (v < 128) return formatRatio((v - 64) * 0.0010421875 + 0.33333);
                else if (v < 256) return formatRatio((v - 128) * 0.0015625000 + 0.4);
                else if (v < 320) return formatRatio((v - 256) * 0.0010416656 + 0.6);
                else if (v < 384) return formatRatio((v - 320) * 0.0013020834 + 0.66666);
                else if (v < 448) return formatRatio((v - 384) * 0.0007812500 + 0.75);
                else if (v < 512) return formatRatio((v - 448) * 0.003125 + 0.8);
                else if (v < 704) return formatRatio((v - 512) * 0.015625 + 1.0);
                else if (v < 832) return formatRatio((v - 704) * 0.03125 + 4.0);
                else if (v < 896) return formatRatio((v - 832) * 0.125 + 8.0);
                else if (v < 960) return formatRatio((v - 896) * 0.25 + 16.0);
                else // if (v < 64) 
                    return formatRatio((v - 960) * 0.5 + 32.0);
                }
            };


        final LabelledDial depth = new LabelledDial("Depth", this, "mutant" + mut + "depth", color, 0, 1024)
            {
            public String map(int value)
                {
                int v = value * 8;
                // dividing 8192 by 6.4 cuts into 128 pieces
                return String.format("%1.1f", (roundEven(v / 6.4) / 10.0));
                }
            };

        final LabelledDial wet = new LabelledDial("Dry/Wet", this, "mutant" + mut + "wet", color, 0, 1024)
            {
            public String map(int value)
                {
                int v = value * 8;
                // dividing 8192 by 81.92 cuts into 100 pieces
                return "" + (int)(v / 81.92) + "%";
                }
            };

        final LabelledDial feedback = new LabelledDial("Feedback", this, "mutant" + mut + "feedback", color, 0, 1024)
            {
            public String map(int value)
                {
                int v = value * 8;
                // dividing 8192 by 54.613333 cuts into 150 pieces
                return "" + (int)(v / 54.613333) + "%";
                }
            };

        final LabelledDial window = new LabelledDial("Window", this, "mutant" + mut + "window", color, 0, 1024)
            {
            public String map(int value)
                {
                int v = value * 8;
                // dividing 8192 by 6.4 cuts into 128 pieces
                return String.format("%1.1f", (roundEven(v / 6.4) / 10.0));
                }
            };
            
        final EnvelopeDisplay disp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, null,  null,  null,  null,  null,  null,  null },
            new String[] { "mutant" + mut + "warp1", "mutant" + mut + "warp2", "mutant" + mut + "warp3", "mutant" + mut + "warp4",
                                                             "mutant" + mut + "warp5", "mutant" + mut + "warp6", "mutant" + mut + "warp7", "mutant" + mut + "warp8" },
            new double[] { 0, 1.0 / 8, 1.0 / 8, 1.0 / 8, 1.0 / 8, 1.0 / 8, 1.0 / 8, 1.0 / 8 },
            new double[] { 1.0 / 1024.0, 1.0 / 1024.0, 1.0 / 1024.0, 1.0 / 1024.0, 1.0 / 1024.0, 1.0 / 1024.0, 1.0 / 1024.0, 1.0 / 1024.0 });
        disp.setStepping(true);


        final HBox warp = new HBox();
                
        // FIXME: Does this require an envelope display of some sort
        for(int i = 1; i < 9; i++)
            {
            comp = new LabelledDial("Warp " + i, this, "mutant" + mut + "warp" + i, color, 0, 1024)
                {
                public String map(int value)
                    {
                    int v = value * 8;
                    // dividing 8192 by 6.4 cuts into 128 pieces
                    return String.format("%1.1f", (roundEven(v / 6.4) / 10.0));
                    }
                };
            warp.add(comp);
            }
    
        final VBox sourceBox = new VBox();
        final HBox knobBox = new HBox();

        VBox vbox = new VBox();
        params = MUTANT_MODES;
        comp = new Chooser("Mode", this, "mutant" + mut + "mode", params)       
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                sourceBox.removeAll();
                knobBox.removeAll();
                switch(model.get(key))
                    {
                    case 0:         // FM-Lin
                        sourceBox.add(sourcesFMLin);
                        knobBox.add(ratio);
                        knobBox.add(depth);
                        knobBox.add(feedback);
                        knobBox.add(wet);
                        knobBox.addLast(Strut.makeVerticalStrut(disp));
                        break;
                    case 1:         // WavStack
                        sourceBox.add(sourceStrut);
                        knobBox.add(depth);
                        knobBox.add(wet);
                        knobBox.addLast(Strut.makeVerticalStrut(disp));
                        break;
                    case 2:         // OSC Sync
                        sourceBox.add(sourcesOscSync);
                        knobBox.add(ratio);
                        knobBox.add(depth);
                        knobBox.add(window);
                        knobBox.add(feedback);
                        knobBox.add(wet);
                        knobBox.addLast(Strut.makeVerticalStrut(disp));
                        break;
                    case 3:         // PW-Orig
                        sourceBox.add(sourceStrut);
                        knobBox.add(ratio);
                        knobBox.add(depth);
                        knobBox.add(feedback);
                        knobBox.add(wet);
                        knobBox.addLast(Strut.makeVerticalStrut(disp));
                        break;
                    case 4:         // PW-Squeez
                        sourceBox.add(sourceStrut);
                        knobBox.add(ratio);
                        knobBox.add(depth);
                        knobBox.add(feedback);
                        knobBox.add(wet);
                        knobBox.addLast(Strut.makeVerticalStrut(disp));
                        break;
                    case 5:         // PW-ASM
                        sourceBox.add(sourceStrut);
                        knobBox.add(ratio);
                        knobBox.add(depth);
                        knobBox.add(feedback);
                        knobBox.add(wet);
                        knobBox.add(warp);
                        knobBox.addLast(disp);
                        break;
                    case 6:         // Harmonic
                        sourceBox.add(sourceStrut);
                        knobBox.add(ratio);
                        knobBox.add(depth);
                        knobBox.add(feedback);
                        knobBox.add(wet);
                        knobBox.addLast(Strut.makeVerticalStrut(disp));
                        break;
                    case 7:         // PhazDiff
                        sourceBox.add(sourceStrut);
                        knobBox.add(depth);
                        knobBox.add(feedback);
                        knobBox.add(wet);
                        knobBox.addLast(Strut.makeVerticalStrut(disp));
                        break;
                    default:
                        System.out.println("ERROR: (Mutant Mode) bad mutant " + model.get(key));
                        break;
                    }
                sourceBox.revalidate();
                knobBox.revalidate();
                sourceBox.repaint();
                knobBox.repaint();
                }
            };
        vbox.add(comp);
        
        vbox.add(sourceBox);
        hbox.add(vbox);
        hbox.addLast(knobBox);


        // set up FM-Lin
        sourceBox.add(sourcesFMLin);
        knobBox.add(ratio);
        knobBox.add(depth);
        knobBox.add(feedback);
        knobBox.add(wet);
 
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addRibbon(Color color)
        {
        Category category = new Category(this, "Ribbon", color);
        category.makeDistributable("ribbonscalenote");

        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();
        
        model.set("ribbonmode", 0);             // the purpose of this is to make sure that mode is FIRST in the mutation list



        //// This is basically a duplicate of addScale()

        HBox _hbox = new HBox();
        final HBox scaleEditBox = new HBox();
                
        for(int i = 2; i <= 8; i++)
            {
            comp = new LabelledDial("Note " + i, this, "ribbonscalenote" + i, color, 0, 12)
                {
                public String map(int value)
                    {
                    if (value == 0) return "Off";
                    else return NOTES[(model.get("ribbonscalekeylock") + value - 2) % 12];
                    }
                };
            scaleEditBox.add(comp);
            }
                
        final JComponent scaleEditBoxStrut = Strut.makeStrut(scaleEditBox);
                        
        LabelledDial keyLock = new LabelledDial("Key Lock", this, "ribbonscalekeylock", color, 1, 12)
            {
            public String map(int value)
                {
                return NOTES[value - 1];
                }
                                
            public void update(String key, Model model)
                {
                super.update(key, model);
                scaleEditBox.repaint();
                }
            };
                        
        final JComponent keyLockStrut = Strut.makeStrut(keyLock);

        VBox _vbox = new VBox();
        params = SCALES;
        comp = new Chooser("Scale Type", this, "ribbonscaletype", params)   
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                int val = model.get(key);
                _hbox.remove(keyLock);
                _hbox.remove(scaleEditBox);
                _hbox.remove(keyLockStrut);
                _hbox.remove(scaleEditBoxStrut);
                
                if (val != 1 && val <= 38)      // not chromatic or microtonal
                    {
                    _hbox.add(keyLock);
                    if (val == 0)           // scale edit
                        {
                        _hbox.addLast(scaleEditBox);
                        }
                    else
                        {
                        _hbox.addLast(scaleEditBoxStrut);
                        }
                    }
                else
                    {
                    _hbox.add(keyLockStrut);
                    _hbox.addLast(scaleEditBoxStrut);
                    }
                _hbox.revalidate();
                _hbox.repaint();
                }
            };
        _vbox.add(comp);
        _hbox.add(_vbox);
        // At this point the scaletype has added the keylock in the wrong place.
        // This is because we updated the chooser before we added the chooser.
        // We need to remove it and re-add it
        _hbox.remove(keyLock);
        _hbox.add(keyLock);


        /// End replication of addScale()
        HBox scale = _hbox;



        params = RIBBON_KEYSPANS;
        final Chooser keyspan = new Chooser("Theremin Keyspan", this, "ribbonkeyspan", params);
 
        final HBox thereminBox = new HBox();
        VBox vbox = new VBox(); 
        comp = new CheckBox("Quantize", this, "ribbonquantize");
        vbox.add(comp);

        comp = new CheckBox("Wheel Volume", this, "ribbonmodcontrol");
        vbox.add(comp);
        thereminBox.add(vbox);

        comp = new LabelledDial("Octave Shift", this, "ribbonoctave", color, 0, 8)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                return "" + (value - 4);
                }
            };
        thereminBox.add(comp);

        comp = new LabelledDial("Glide", this, "ribbonglide", color, 0, 127);
        thereminBox.add(comp);
        
        thereminBox.addLast(scale);
        
        final CheckBox ribbonHold = new CheckBox("Hold", this, "ribbonhold");

      
        /// FIXME: Setting this to THEREMIN will cause the Hydrasynth to respond via NRPN
        /// with all the theremin values, thereby erasing your current settings.  :-(  :-(
        final VBox modeBox = new VBox(); 
        params = RIBBON_MODES;
        comp = new Chooser("Mode", this, "ribbonmode", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                if (model.get(key) == 1)                // Theremin
                    {
                    modeBox.add(keyspan);
                    modeBox.remove(ribbonHold);
                    hbox.addLast(thereminBox);
                    }
                else if (model.get(key) == 2)   // Mod
                    {
                    modeBox.remove(keyspan);
                    modeBox.add(ribbonHold);
                    hbox.remove(thereminBox);
                    }
                else            // Pitch Bend
                    {
                    modeBox.remove(keyspan);
                    modeBox.remove(ribbonHold);
                    hbox.remove(thereminBox);
                    }
                modeBox.revalidate();
                hbox.revalidate();
                modeBox.repaint();
                hbox.repaint();
                }
            };
        modeBox.add(comp);
        hbox.add(modeBox);


        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addFilter(int filter, Color color)
        {
        Category category = new Category(this, "Filter " + filter, color);
        category.makePasteable("filter");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        HBox vowels = new HBox();
        HBox vowelsouter = new HBox();
                
        VBox vbox = new VBox();
        params = (filter == 1 ? FILTER_1_TYPES : FILTER_2_TYPES);
        comp = new Chooser("Type", this, "filter" + filter + "type", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                vowelsouter.removeAll();
                if (model.get(key) == 10)               // vowel
                    {
                    vowelsouter.add(vowels);
                    }
                vowelsouter.revalidate();
                vowelsouter.repaint();
                }
            };
        vbox.add(comp);

        if (filter == 1)
            {
            params = FILTER_POSITIONS;
            comp = new Chooser("Drive Route", this, "filter" + filter + "positionofdrive", params);
            vbox.add(comp);
            }

        hbox.add(vbox);

        comp = new LabelledDial("Cutoff", this, "filter" + filter + "cutoff", color, 0, 1024)
            {
            public String map(int value)
                {
                int v = value * 8;
                // dividing 8192 by 6.4 roughly cuts into 1280 pieces
                return String.format("%1.1f", (roundEven(v / 6.4) / 10.0));
                }
            };
                
        hbox.add(comp);

        comp = new LabelledDial("Resonance", this, "filter" + filter + "resonance", color, 0, 1024)
            {
            public String map(int value)
                {
                int v = value * 8;
                // dividing 8192 by 6.4 roughly cuts into 1280 pieces
                return String.format("%1.1f", (roundEven(v / 6.4) / 10.0));
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Env 1", this, "filter" + filter + "env1amount", color, 0, 1024)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                int v = value * 8;
                // dividing 8192 by 6.4 roughly cuts into 1280 pieces
                return String.format("%1.1f", (roundEven(v / 6.4) / 10.0) - 64);
                }
            };
        hbox.add(comp);
        ((LabelledDial)comp).addAdditionalLabel("Amount");

        comp = new LabelledDial("LFO 1", this, "filter" + filter + "lfo1amount", color, 0, 1024)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                int v = value * 8;
                // dividing 8192 by 6.4 roughly cuts into 1280 pieces
                return String.format("%1.1f", (roundEven(v / 6.4) / 10.0) - 64);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        comp = new LabelledDial("Vel Env", this, "filter" + filter + "velenv", color, 0, 1024)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                int v = value * 8;
                // dividing 8192 by 6.4 roughly cuts into 1280 pieces
                return String.format("%1.1f", (roundEven(v / 6.4) / 10.0) - 64);
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Keytrack", this, "filter" + filter + "keytrack", color, 0, 1024)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                int v = value * 8;
                // dividing 8192 by 20.48 roughly cuts into 400 pieces
                return "" + (((int)(v / 20.48)) - 200) + "%";
                }
            };
        hbox.add(comp);

        if (filter == 1)
            {
            comp = new LabelledDial("Drive", this, "filter" + filter + "drive", color, 0, 1024)
                {
                public String map(int value)
                    {
                    int v = value * 8;
                    // dividing 8192 by 6.4 roughly cuts into 1280 pieces
                    return String.format("%1.1f", (roundEven(v / 6.4) / 10.0));
                    }
                };
            hbox.add(comp);
                        
            // "Special" in the NRPN docs is Formant Control
            comp = new LabelledDial("Formant", this, "filter" + filter + "special", color, 0, 1024)
                {
                public String map(int value)
                    {
                    int v = value * 8;
                    // dividing 8192 by 6.4 roughly cuts into 1280 pieces
                    return String.format("%1.1f", (roundEven(v / 6.4) / 10.0));
                    }
                };
            ((LabelledDial)comp).addAdditionalLabel("Control");
            vowels.add(comp);
            
            vbox = new VBox();
            params = VOWEL_ORDERS;
            comp = new Chooser("Vowel Order", this, "filter" + filter + "vowelorder", params);
            vbox.add(comp);
            vowels.add(vbox);
            // don't do this: vowelsouter.add(vowels);
            hbox.add(vowelsouter);
            }
        else    // if (filter == 2)
            {
            comp = new LabelledDial("Morph", this, "filter" + filter + "morph", color, 0, 1024)
                {
                public String map(int value)
                    {
                    int v = value * 8;
                    // dividing 8192 by 6.4 roughly cuts into 1280 pieces
                    return String.format("%1.1f", (roundEven(v / 6.4) / 10.0));
                    }
                };
            hbox.add(comp);
            }
 
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addAmp(Color color)
        {
        Category category = new Category(this, "Amplifier", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        comp = new LabelledDial("Level", this, "amplevel", color, 0, 1024)
            {
            public String map(int value)
                {
                int v = value * 8;
                // dividing 8192 by 6.4 roughly cuts into 1280 pieces
                return String.format("%1.1f", (roundEven(v / 6.4) / 10.0));
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Vel Env", this, "ampvelenv", color, 0, 1024)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                int v = value * 8;
                // dividing 8192 by 6.4 roughly cuts into 1280 pieces
                return String.format("%1.1f", (roundEven(v / 6.4) / 10.0) - 64);
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("LFO 2", this, "amplfo2amount", color, 0, 1024)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                int v = value * 8;
                // dividing 8192 by 6.4 roughly cuts into 1280 pieces
                return String.format("%1.1f", (roundEven(v / 6.4) / 10.0) - 64);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);
 
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
        
        
        
        
    public JComponent addFX(final boolean pre, Color color)
        {
        Category category = new Category(this, (pre ? "Pre-FX" : "Post-FX"), color);

        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();

        model.set((pre ? "pre" : "post") + "fxtype", 0);                // the purpose of this is to make sure that type is FIRST in the mutation list
        
        // make it so mutation won't try to play with these
        for(int i = 1; i <= 5; i++)
            {
            model.set((pre ? "pre" : "post") + "fxparam" + i, 0);
            model.setStatus((pre ? "pre" : "post") + "fxparam" + i, Model.STATUS_IMMUTABLE);
            }

        final HBox chorus = new HBox();
        comp = new LabelledDial("Rate", this, (pre ? "pre" : "post") + "fx1param1", color, 0, 1024)
            {
            public String map(int value)
                {
                int v = value / 8;
                if (v < 40)     // 0.02 - 0.42
                    {
                    return String.format("%4.2f", (v - 0) * 0.01 + 0.02);
                    }
                else if (v < 59)        // 0.42 - 0.80
                    {
                    return String.format("%4.2f", (v - 40) * 0.02 + 0.42);
                    }
                else if (v < 83)        // 0.80 - 2.00
                    {
                    return String.format("%4.2f", (v - 59) * 0.05 + 0.80);
                    }
                else if (v < 111)       // 2.00 - 4.80
                    {
                    return String.format("%4.2f", (v - 83) * 0.10 + 2.0);
                    }
                else if (v < 122)       // 4.80 - 7.00
                    {
                    return String.format("%4.2f", (v - 111) * 0.20 + 4.8);
                    }
                else  // v <= 128       // 7.00 - 10.00
                    {
                    return String.format("%4.2f", (v - 122) * 0.50 + 7.0);
                    }
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("(Param 1)");
        chorus.add(comp);
        comp = new LabelledDial("Depth", this, (pre ? "pre" : "post") + "fx1param2", color, 0, 1024)
            {
            public String map(int value)
                {
                int v = value * 8;
                // dividing 8192 by 6.4 roughly cuts into 1280 pieces
                return String.format("%1.1f", (roundEven(v / 6.4) / 10.0));
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("(Param 2)");
        chorus.add(comp);
        comp = new LabelledDial("Offset", this, (pre ? "pre" : "post") + "fx1param3", color, 0, 360, 180)
            {
            public boolean isSymmetric() { return true; }
            };
        // ((LabelledDial)comp).addAdditionalLabel("(Param 3)");
        chorus.add(comp);
        comp = new LabelledDial("Feedback", this, (pre ? "pre" : "post") + "fx1param4", color, 1, 127, 64)
            {
            public boolean isSymmetric() { return true; }
            };
        // ((LabelledDial)comp).addAdditionalLabel("(Param 4)");
        chorus.add(comp);
        VBox vbox = new VBox();
        comp = new CheckBox("Stereo", this, (pre ? "pre" : "post") + "fx1param5");
        vbox.add(comp);
        chorus.addLast(vbox);

        final HBox flanger = new HBox();
        comp = new LabelledDial("Rate", this, (pre ? "pre" : "post") + "fx2param1", color, 0, 1024)
            {
            public String map(int value)
                {
                int v = value / 8;
                if (v < 40)     // 0.02 - 0.42
                    {
                    return String.format("%4.2f", (v - 0) * 0.01 + 0.02);
                    }
                else if (v < 59)        // 0.42 - 0.80
                    {
                    return String.format("%4.2f", (v - 40) * 0.02 + 0.42);
                    }
                else if (v < 83)        // 0.80 - 2.00
                    {
                    return String.format("%4.2f", (v - 59) * 0.05 + 0.80);
                    }
                else if (v < 111)       // 2.00 - 4.80
                    {
                    return String.format("%4.2f", (v - 83) * 0.10 + 2.0);
                    }
                else if (v < 122)       // 4.80 - 7.00
                    {
                    return String.format("%4.2f", (v - 111) * 0.20 + 4.8);
                    }
                else  // v <= 128       // 7.00 - 10.00
                    {
                    return String.format("%4.2f", (v - 122) * 0.50 + 7.0);
                    }
                }
            };
        flanger.add(comp);
        ((LabelledDial)comp).addAdditionalLabel("(Param 1)");
        comp = new LabelledDial("Depth", this, (pre ? "pre" : "post") + "fx2param2", color, 0, 1024)
            {
            public String map(int value)
                {
                int v = value * 8;
                // dividing 8192 by 6.4 roughly cuts into 1280 pieces
                return String.format("%1.1f", (roundEven(v / 6.4) / 10.0));
                }
            };
        flanger.add(comp);
        ((LabelledDial)comp).addAdditionalLabel("(Param 2)");
        comp = new LabelledDial("Offset", this, (pre ? "pre" : "post") + "fx2param3", color, 0, 360, 180)
            {
            public boolean isSymmetric() { return true; }
            };
        flanger.add(comp);
        // ((LabelledDial)comp).addAdditionalLabel("(Param 3)");
        comp = new LabelledDial("Feedback", this, (pre ? "pre" : "post") + "fx2param4", color, 1, 127, 64)
            {
            public boolean isSymmetric() { return true; }
            };
        // ((LabelledDial)comp).addAdditionalLabel("(Param 4)");
        flanger.add(comp);
        vbox = new VBox();
        comp = new CheckBox("Stereo", this, (pre ? "pre" : "post") + "fx2param5");
        vbox.add(comp);
        flanger.addLast(vbox);

        final HBox rotary = new HBox();
        comp = new LabelledDial("Low-Speed", this, (pre ? "pre" : "post") + "fx3param1", color, 0, 1024)
            {
            public String map(int value)
                {
                int v = value / 8;
                if (v < 40)     // 0.02 - 0.42
                    {
                    return String.format("%4.2f", (v - 0) * 0.01 + 0.02);
                    }
                else if (v < 59)        // 0.42 - 0.80
                    {
                    return String.format("%4.2f", (v - 40) * 0.02 + 0.42);
                    }
                else if (v < 83)        // 0.80 - 2.00
                    {
                    return String.format("%4.2f", (v - 59) * 0.05 + 0.80);
                    }
                else if (v < 111)       // 2.00 - 4.80
                    {
                    return String.format("%4.2f", (v - 83) * 0.10 + 2.0);
                    }
                else if (v < 122)       // 4.80 - 7.00
                    {
                    return String.format("%4.2f", (v - 111) * 0.20 + 4.8);
                    }
                else  // v <= 128       // 7.00 - 10.00
                    {
                    return String.format("%4.2f", (v - 122) * 0.50 + 7.0);
                    }
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("(Param 1)");
        rotary.add(comp);
        comp = new LabelledDial("Hi-Speed", this, (pre ? "pre" : "post") + "fx3param2", color, 0, 1024)
            {
            public String map(int value)
                {
                int v = value / 8;
                if (v < 40)     // 0.02 - 0.42
                    {
                    return String.format("%4.2f", (v - 0) * 0.01 + 0.02);
                    }
                else if (v < 59)        // 0.42 - 0.80
                    {
                    return String.format("%4.2f", (v - 40) * 0.02 + 0.42);
                    }
                else if (v < 83)        // 0.80 - 2.00
                    {
                    return String.format("%4.2f", (v - 59) * 0.05 + 0.80);
                    }
                else if (v < 111)       // 2.00 - 4.80
                    {
                    return String.format("%4.2f", (v - 83) * 0.10 + 2.0);
                    }
                else if (v < 122)       // 4.80 - 7.00
                    {
                    return String.format("%4.2f", (v - 111) * 0.20 + 4.8);
                    }
                else  // v <= 128       // 7.00 - 10.00
                    {
                    return String.format("%4.2f", (v - 122) * 0.50 + 7.0);
                    }
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("(Param 2)");
        rotary.add(comp);
        comp = new LabelledDial("Low-Depth", this, (pre ? "pre" : "post") + "fx3param3", color, 0, 127);
        // ((LabelledDial)comp).addAdditionalLabel("(Param 3)");
        rotary.add(comp);
        comp = new LabelledDial("Hi-Depth", this, (pre ? "pre" : "post") + "fx3param4", color, 0, 127);
        // ((LabelledDial)comp).addAdditionalLabel("(Param 4)");
        rotary.add(comp);
        comp = new LabelledDial("Low/High", this, (pre ? "pre" : "post") + "fx3param5", color,1, 127, 64)
            {
            public boolean isSymmetric() { return true; }
            };
        // ((LabelledDial)comp).addAdditionalLabel("(Param 5)");
        rotary.addLast(comp);
        
        final HBox phaser = new HBox();
        comp = new LabelledDial("Rate", this, (pre ? "pre" : "post") + "fx4param1", color, 0, 1024)
            {
            public String map(int value)
                {
                int v = value / 8;
                if (v < 40)     // 0.02 - 0.42
                    {
                    return String.format("%4.2f", (v - 0) * 0.01 + 0.02);
                    }
                else if (v < 59)        // 0.42 - 0.80
                    {
                    return String.format("%4.2f", (v - 40) * 0.02 + 0.42);
                    }
                else if (v < 83)        // 0.80 - 2.00
                    {
                    return String.format("%4.2f", (v - 59) * 0.05 + 0.80);
                    }
                else if (v < 111)       // 2.00 - 4.80
                    {
                    return String.format("%4.2f", (v - 83) * 0.10 + 2.0);
                    }
                else if (v < 122)       // 4.80 - 7.00
                    {
                    return String.format("%4.2f", (v - 111) * 0.20 + 4.8);
                    }
                else  // v <= 128       // 7.00 - 10.00
                    {
                    return String.format("%4.2f", (v - 122) * 0.50 + 7.0);
                    }
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("(Param 1)");
        phaser.add(comp);
        comp = new LabelledDial("Feedback", this, (pre ? "pre" : "post") + "fx4param2", color, 0, 1024)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                int v = value * 8;
                // dividing 8192 by 6.4 roughly cuts into 1280 pieces
                return String.format("%1.1f", (roundEven(v / 6.4) / 10.0) - 64);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("(Param 2)");
        phaser.add(comp);
        comp = new LabelledDial("Depth", this, (pre ? "pre" : "post") + "fx4param3", color, 0, 127);
        // ((LabelledDial)comp).addAdditionalLabel("(Param 3)");
        phaser.add(comp);
        comp = new LabelledDial("Phase", this, (pre ? "pre" : "post") + "fx4param4", color, 0, 127);
        // ((LabelledDial)comp).addAdditionalLabel("(Param 4)");
        phaser.add(comp);
        comp = new LabelledDial("Offset", this, (pre ? "pre" : "post") + "fx4param5", color, 0, 360, 180)
            {
            public boolean isSymmetric() { return true; }
            };
        // ((LabelledDial)comp).addAdditionalLabel("(Param 5)");
        phaser.addLast(comp);
        
        final HBox lofi = new HBox();
        comp = new LabelledDial("Cutoff", this, (pre ? "pre" : "post") + "fx5param1", color, 0, 1024)
            {
            public String map(int value)
                {
                int v = value * 8;
                // dividing 8192 by 64 roughly cuts into 128 pieces
                v = roundEven(v / 64);
                if (v < 10)
                    {
                    return "" + (((v - 0) * 10) + 160);
                    }
                else if (v < 15)
                    {
                    return "" + (((v - 10) * 20) + 260);
                    }
                else if (v < 16)
                    {
                    return "360";
                    }
                else if (v < 39)
                    {
                    return "" + (((v - 16) * 50) + 400);
                    }
                else if (v < 93)
                    {
                    return "" + (((v - 39) * 100) + 1600);
                    }
                else if (v < 108)
                    {
                    return "" + (((v - 93) * 200) + 7000);
                    }
                else  // v < 128
                    {
                    return "" + (((v - 108) * 500) + 10000);
                    }
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("(Param 1)");
        lofi.add(comp);
        comp = new LabelledDial("Resonance", this, (pre ? "pre" : "post") + "fx5param2", color, 0, 1023)
            {
            public String map(int value)
                {
                int v = value * 8;
                // dividing 8184 by 74.4 roughly cuts into 110 pieces
                return String.format("%1.1f", (roundEven(v / 74.4) / 10.0 + 1.0));
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("(Param 2)");
        lofi.add(comp);
        vbox = new VBox(); 
        params = LO_FI_FILTER_TYPES;
        comp = new Chooser("Filter Type", this, (pre ? "pre" : "post") + "fx5param3", params);
        vbox.add(comp);
        lofi.add(vbox);
        comp = new LabelledDial("Output", this, (pre ? "pre" : "post") + "fx5param4", color, 58, 100)
            {
            public double getStartAngle() { return 270 + 22; }
            public String map(int value)
                {
                return "" + (value - 64) + "dB";
                }
            };
        // ((LabelledDial)comp).addAdditionalLabel("(Param 4)");
        lofi.add(comp);
        comp = new LabelledDial("Sampling", this, (pre ? "pre" : "post") + "fx5param5", color, 1, 16)
            {
            public String map(int value)
                {
                return LO_FI_SAMPLES[value - 1];
                }
            };
        lofi.addLast(comp);
        // ((LabelledDial)comp).addAdditionalLabel("(Param 5)");
       
        final HBox tremolo = new HBox();
        comp = new LabelledDial("Rate", this, (pre ? "pre" : "post") + "fx6param1", color, 0, 1024)
            {
            public String map(int value)
                {
                int v = value / 8;
                if (v < 40)     // 0.02 - 0.42
                    {
                    return String.format("%4.2f", (v - 0) * 0.01 + 0.02);
                    }
                else if (v < 59)        // 0.42 - 0.80
                    {
                    return String.format("%4.2f", (v - 40) * 0.02 + 0.42);
                    }
                else if (v < 83)        // 0.80 - 2.00
                    {
                    return String.format("%4.2f", (v - 59) * 0.05 + 0.80);
                    }
                else if (v < 111)       // 2.00 - 4.80
                    {
                    return String.format("%4.2f", (v - 83) * 0.10 + 2.0);
                    }
                else if (v < 122)       // 4.80 - 7.00
                    {
                    return String.format("%4.2f", (v - 111) * 0.20 + 4.8);
                    }
                else  // v <= 128       // 7.00 - 10.00
                    {
                    return String.format("%4.2f", (v - 122) * 0.50 + 7.0);
                    }
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("(Param 1)");
        tremolo.add(comp);
        comp = new LabelledDial("Depth", this, (pre ? "pre" : "post") + "fx6param2", color, 0, 1024)
            {
            public String map(int value)
                {
                int v = value * 8;
                // dividing 8192 by 6.4 roughly cuts into 1280 pieces
                return String.format("%1.1f", (roundEven(v / 6.4) / 10.0));
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("(Param 2)");
        tremolo.add(comp);
        vbox = new VBox(); 
        params = TREMOLO_LFO_SHAPES;
        comp = new Chooser("LFO Shape", this, (pre ? "pre" : "post") + "fx6param3", params);
        vbox.add(comp);
        tremolo.add(vbox);
        comp = new LabelledDial("Phase", this, (pre ? "pre" : "post") + "fx6param4", color, 0, 360, 180)
            {
            public boolean isSymmetric() { return true; }
            };
        // ((LabelledDial)comp).addAdditionalLabel("(Param 4)");
        tremolo.add(comp);
        comp = new LabelledDial("Pitch Mod", this, (pre ? "pre" : "post") + "fx6param5", color, 0, 127);
        // ((LabelledDial)comp).addAdditionalLabel("(Param 5)");
        tremolo.addLast(comp);

        final HBox eq = new HBox();
        comp = new LabelledDial("Low Gain", this, (pre ? "pre" : "post") + "fx7param1", color, 0, 1020)
            {
            public double getStartAngle() { return 252; }
            public String map(int value)
                {
                return "" + ((roundEven(value / 1.7) - 360) / 10.0);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("(Param 1)");
        eq.add(comp);
        comp = new LabelledDial("Mid Gain", this, (pre ? "pre" : "post") + "fx7param3", color, 0, 600)
            {
            public double getStartAngle() { return 252; }
            public String map(int value)
                {
                return "" + ((value - 360) / 10.0);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("(Param 2)");
        eq.add(comp);
        
        comp = new LabelledDial("High Gain", this, (pre ? "pre" : "post") + "fx7param2", color, 0, 1020)
            {
            public double getStartAngle() { return 252; }
            public String map(int value)
                {
                return "" + ((roundEven(value / 1.7) - 360) / 10.0);
                }
            };
        // ((LabelledDial)comp).addAdditionalLabel("(Param 3)");
        eq.add(comp);
        comp = new LabelledDial("Xover Lo", this, (pre ? "pre" : "post") + "fx7param4", color, 16, 1000)
            {
            public String map(int value)
                {
                return "" + (value * 2);
                }
            };
        // ((LabelledDial)comp).addAdditionalLabel("(Param 4)");
        eq.add(comp);
        comp = new LabelledDial("Xover Hi", this, (pre ? "pre" : "post") + "fx7param5", color, 32, 1000)
            {
            public String map(int value)
                {
                return "" + (value * 16);
                }
            };
        // ((LabelledDial)comp).addAdditionalLabel("(Param 5)");
        eq.addLast(comp);

        final HBox compressor = new HBox();
        comp = new LabelledDial("Ratio", this, (pre ? "pre" : "post") + "fx8param1", color, 51, 1020)
            {
            public String map(int value)
                {
                int v = value * 8;
                // dividing 7752  (8160 - 408) by 40.8 roughly cuts into 190 pieces                             
                return String.format("%1.1f", roundEven((v - 408) / 40.8) / 10.0 + 1.0) + ":1";
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("(Param 1)");
        compressor.add(comp);
        comp = new LabelledDial("Threshold", this, (pre ? "pre" : "post") + "fx8param2", color, 0, 1024)
            {
            public double getStartAngle() { return 180; }
            public String map(int value)
                {
                int v = value * 8;
                // dividing 8192 by 12.8 roughly cuts into 640 pieces
                return String.format("%1.1f", (roundEven(v / 12.8) / 10.0 - 64.0));
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("(Param 2)");
        compressor.add(comp);
        comp = new LabelledDial("Attack", this, (pre ? "pre" : "post") + "fx8param3", color, 1, 400);
        // ((LabelledDial)comp).addAdditionalLabel("(Param 3)");
        compressor.add(comp);
        comp = new LabelledDial("Release", this, (pre ? "pre" : "post") + "fx8param4", color, 5, 560);
        // ((LabelledDial)comp).addAdditionalLabel("(Param 4)");
        compressor.add(comp);
        comp = new LabelledDial("Output", this, (pre ? "pre" : "post") + "fx8param5", color, 0, 512);
        // ((LabelledDial)comp).addAdditionalLabel("(Param 5)");
        compressor.add(comp);
        
        VBox sidechainbox = new VBox();
        params = SIDECHAINS;
        comp = new Chooser("Sidechain", this, (pre ? "pre" : "post") + "fxsidechain", params);
        sidechainbox.add(comp);
        compressor.addLast(sidechainbox);
        
        final HBox distort = new HBox();
        comp = new LabelledDial("Drive", this, (pre ? "pre" : "post") + "fx9param1", color, 0, 1024)
            {
            public String map(int value)
                {
                int v = value * 8;
                // dividing 8192 by 6.4 roughly cuts into 1280 pieces
                return String.format("%1.1f", (roundEven(v / 6.4) / 10.0));
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("(Param 1)");
        distort.add(comp);
        comp = new LabelledDial("Tone", this, (pre ? "pre" : "post") + "fx9param2", color, 0, 1024)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                int v = value * 8;
                // dividing 8192 by 6.4 roughly cuts into 1280 pieces
                return String.format("%1.1f", (roundEven(v / 6.4) / 10.0) - 64);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("(Param 2)");
        distort.add(comp);
        comp = new LabelledDial("Asym", this, (pre ? "pre" : "post") + "fx9param3", color, 0, 128);
        // ((LabelledDial)comp).addAdditionalLabel("(Param 3)");
        distort.add(comp);
        comp = new LabelledDial("Curve", this, (pre ? "pre" : "post") + "fx9param4", color, 0, 128);
        // ((LabelledDial)comp).addAdditionalLabel("(Param 4)");
        distort.add(comp);
        comp = new LabelledDial("Output", this, (pre ? "pre" : "post") + "fx9param5", color, 0, 600)
            {
            public double getStartAngle() { return 255; }            
            public String map(int value)
                {
                return "" + ((value - 360) / 10.0);
                }
            };
        // ((LabelledDial)comp).addAdditionalLabel("(Param 5)");
        distort.addLast(comp);
        
        HBox bypass = new HBox();
                
        final HBox fxparams = new HBox();
        final HBox[] allParams = { bypass, chorus, flanger, rotary, phaser, lofi, tremolo, eq, compressor, distort };

        final VBox typeBox = new VBox(); 

        final PushButton presets = new PushButton("Presets", FX_PRESETS[0])
            {
            public void perform(int i)
                {
                int type = model.get((pre ? "pre" : "post") + "fxtype");
                if (type != 0 && type < 10)             // just in case
                    {
                    for(int j = 0; j < 5; j++)
                        {
                        parseNRPN((pre ? "pre" : "post") + "fxparam" + (j + 1), FX_PRESET_VALUES[type][i][j + 2]);
                        }
                    }
                }
            };

        final LabelledDial drywet = new LabelledDial("Dry/Wet", this, (pre ? "pre" : "post") + "fxwet", color, 0, 1024)
            {
            public String map(int value)
                {
                int v = value * 8;
                // dividing 8192 by 8.192 cuts into 1000 pieces
                return "" + (roundEven(v / 8.192) / 10.0) + "%";
                }
            };
        drywet.addAdditionalLabel(" ");         // so Bypass is the right height

        params = FX_TYPES;
        comp = new Chooser("Type", this, (pre ? "pre" : "post") + "fxtype", params)     
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                
                int preset = model.get(key);
                typeBox.remove(presets);
                if (preset != 0 && preset != 8)                                                         // 0 = Bypass
                    {
                    typeBox.add(presets);
                    presets.setOptions(FX_PRESETS[model.get(key)]);
                    }
                typeBox.revalidate();
                typeBox.repaint();
                
                fxparams.removeAll();
                fxparams.addLast(allParams[model.get(key)]);
                fxparams.revalidate();
                fxparams.repaint();
                }
            };
        typeBox.add(comp);
        //typeBox.add(presets);
        
        hbox.add(typeBox);
        hbox.add(Strut.makeStrut(drywet, true));
        hbox.add(drywet);
        fxparams.add(allParams[0]);
        hbox.add(fxparams);
        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
        

    public JComponent addDelay(Color color)
        {
        Category category = new Category(this, "Delay", color);

        model.set("delaybpmsync", 0);           // the purpose of this is to make sure that bpm sync is FIRST in the mutation list

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();

        final LabelledDial delaytimesyncoff = new LabelledDial("Time", this, "delaytimesyncoff", color, 0, 1024)
            {
            public String map(int value)
                {
                return getDelayTimeSyncOff(value);
                }
            };

        final LabelledDial delaytimesyncon = new LabelledDial("Time", this, "delaytimesyncon", color, 0, FX_DELAYS_SYNC_ON.length - 1)
            {
            public String map(int value)
                {
                return FX_DELAYS_SYNC_ON[value];
                }
            };
                
        final HBox delaytimebox = new HBox();

                
        VBox vbox = new VBox(); 
        params = DELAY_TYPES;
        comp = new Chooser("Type", this, "delaytype", params);
        vbox.add(comp);
        comp = new CheckBox("BPM Sync", this, "delaybpmsync")   
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                delaytimebox.removeAll();
                if (model.get(key) == 0)
                    {
                    delaytimebox.add(delaytimesyncoff);
                    }
                else
                    {
                    delaytimebox.add(delaytimesyncon);
                    }
                delaytimebox.revalidate();
                delaytimebox.repaint();
                }
            };
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Dry/Wet", this, "delaywet", color, 0, 1024)
            {
            public String map(int value)
                {
                int v = value * 8;
                // dividing 8192 by 8.192 cuts into 1000 pieces
                return "" + (roundEven(v / 8.192) / 10.0) + "%";
                }
            };
        hbox.add(comp);

        delaytimebox.add(delaytimesyncoff);
        hbox.add(delaytimebox);
        
        comp = new LabelledDial("Feedback", this, "delayfeedback", color, 0, 1024)
            {
            public String map(int value)
                {
                int v = value * 8;
                // dividing 8192 by 6.4 cuts into 128 pieces
                return String.format("%1.1f", (roundEven(v / 6.4) / 10.0));
                }
            };
        hbox.add(comp);
        comp = new LabelledDial("Wet Tone", this, "delaywettone", color, 0, 1024)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                int v = value * 8;
                // dividing 8192 by 6.4 cuts into 128 pieces
                return String.format("%1.1f", (roundEven(v / 6.4) / 10.0) - 64.0);
                }
            };
        hbox.add(comp);
        comp = new LabelledDial("Feed Tone", this, "delayfeedtone", color, 0, 1024)
            {
            public String map(int value)
                {
                int v = value * 8;
                // dividing 8192 by 6.4 cuts into 128 pieces
                return String.format("%1.1f", (roundEven(v / 6.4) / 10.0));
                }
            };
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
                
        VBox vbox = new VBox(); 
        params = REVERB_TYPES;
        comp = new Chooser("Type", this, "reverbtype", params);
        vbox.add(comp);
        hbox.add(vbox);
        
        comp = new LabelledDial("Dry/Wet", this, "reverbwet", color, 0, 1024)
            {
            public String map(int value)
                {
                int v = value * 8;
                // dividing 8192 by 8.192 cuts into 1000 pieces
                return "" + (roundEven(v / 8.192) / 10.0) + "%";
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("PreDelay", this, "reverbpredelay", color, 0, 1024)
            {
            public String map(int value)
                {
                                
                return String.format("%1.1f", roundEven(value * 10.0 / 4.1042084168) / 10.0 + 0.5);
                }
            };
        hbox.add(comp);
        
        comp = new LabelledDial("Time", this, "reverbtime", color, 0, 1024)
            {
            public String map(int value)
                {
                return getReverbTime(value);
                }
            };
        hbox.add(comp);
        comp = new LabelledDial("Tone", this, "reverbtone", color, 0, 1024)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                int v = value * 8;
                // dividing 8192 by 6.4 cuts into 128 pieces
                return String.format("%1.1f", (roundEven(v / 6.4) / 10.0) - 64.0);
                }
            };
        hbox.add(comp);
        comp = new LabelledDial(" Hi Damp ", this, "reverbhidamp", color, 0, 1024)
            {
            public String map(int value)
                {
                int v = value * 8;
                // dividing 8192 by 6.4 cuts into 128 pieces
                return String.format("%1.1f", (roundEven(v / 6.4) / 10.0));
                }
            };
        hbox.add(comp);
        comp = new LabelledDial(" Lo Damp ", this, "reverblodamp", color, 0, 1024)
            {
            public String map(int value)
                {
                int v = value * 8;
                // dividing 8192 by 6.4 cuts into 128 pieces
                return String.format("%1.1f", (roundEven(v / 6.4) / 10.0));
                }
            };
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
        params = FILTER_ROUTES;
        comp = new Chooser("Routing", this, "mixerfilterrouting", params);
        vbox.add(comp);
        hbox.add(vbox);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addLFO(int lfo, Color color)
        {
        Category category = new Category(this, "LFO " + lfo + (lfo == 1 ? " (Filter)" : (lfo == 2 ? " (Amplifier)" : "")), color);
        category.makePasteable("lfo");

        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();
        
        model.set("lfo" + lfo + "bpmsync", 0);          // the purpose of this is to make sure that bpm sync is FIRST in the mutation list

        final LabelledDial steps = new LabelledDial("Steps", this, "lfo" + lfo + "steps", color, 2, 64);

        VBox vbox = new VBox();
        params = LFO_WAVES;
        comp = new Chooser("Wave", this, "lfo" + lfo + "wave", params)  
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                if (model.get(key) < 10) // no steps
                    {
                    hbox.remove(steps);
                    }
                else
                    {
                    hbox.add(steps);
                    }
                hbox.revalidate();
                hbox.repaint();
                }
            };
        vbox.add(comp);

        params = LFO_TRIG_SYNCS;
        comp = new Chooser("Trig Sync", this, "lfo" + lfo + "trigsync", params);
        vbox.add(comp);
        hbox.add(vbox);
        
        vbox = new VBox();
        
        params = LFO_ONE_SHOT_OPTIONS;
        comp = new Chooser("One-Shot", this, "lfo" + lfo + "oneshot", params);
        vbox.add(comp);

        params = QUANTIZATIONS;
        comp = new Chooser("Quantize", this, "lfo" + lfo + "quantize", params);
        model.setMetricMinMax("lfo" + lfo + "quantize", 0, QUANTIZATIONS.length - 1);
        vbox.add(comp);

        final HBox bpmOn = new HBox();
        final HBox bpmOff = new HBox();
        final HBox bpm = new HBox();
        
        comp = new CheckBox("BPM Sync", this, "lfo" + lfo + "bpmsync")
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                bpm.removeAll();
                if (model.get(key) == 0)
                    bpm.add(bpmOff);
                else
                    bpm.add(bpmOn);
                bpm.revalidate();
                bpm.repaint();
                }
            };
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Level", this, "lfo" + lfo + "level", color, 0, 1024)
            {
            public String map(int value)
                {
                int v = value * 8;
                // dividing 8192 by 6.4 roughly cuts into 1280 pieces
                return String.format("%1.1f", (roundEven(v / 6.4) / 10.0));
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Rate", this, "lfo" + lfo + "ratesyncoff", color, 0, 1024)
            {
            public String map(int value)
                {
                return String.format("%3.2f", lfoRate(value));
                }
            };
        bpmOff.add(comp);

        comp = new LabelledDial("Delay", this, "lfo" + lfo + "delaysyncoff", color, 0, 127)
            {
            // identical to Envelope Delay
            public String map(int value)
                {
                int v = value;
                if (v < 20) return "" + v + "ms";       // 0-20ms by 1
                else if (v < 30) return "" + (((v - 20) * 2) + 20) + "ms";      // 20-40ms by 2
                else if (v < 40) return "" + (((v - 30) * 4) + 40) + "ms";      // 40-80ms by 4
                else if (v < 50) return "" + (((v - 40) * 8) + 80) + "ms";      // 80-160ms by 8
                else if (v < 60) return "" + (((v - 50) * 16) + 160) + "ms";    // 160-320ms by 16
                else if (v < 70) return "" + (((v - 60) * 32) + 320) + "ms";    // 320ms-640ms by 32
                else if (v < 80) 
                    {
                    int i = (((v - 70) * 64) + 640);
                    if (i < 1000) return "" + i + "ms";     // 640ms-9780ms by 64
                    else return String.format("%4.2f", (i/1000.0)) + "s";   // 1024ms-1280ms by 64
                    }
                else if (v < 90) return eForm(v, 80, 128, 1280) + "s";  // 1280 - 2560 by 128
                else if (v < 100) return eForm(v, 90, 256, 2560) + "s"; // 2560 - 5120 by 256
                else if (v < 110) return eForm(v, 100, 512, 5120) + "s";        // 5120 - 9728 by 512 
                else if (v < 122) return ((v - 110) + 10) + "s";        // 10 - 22 sec by 1
                else return (((v - 122) * 2) + 22) + "s";       // 20 - 32 sec by 2
                }
            };
        bpmOff.add(comp);

        comp = new LabelledDial("Fade In", this, "lfo" + lfo + "fadeinsyncoff", color, 0, 127)
            {
            public String map(int value)
                {
                return "" + LFO_FADE_INS_SYNC_ON[value];
                }
            };
        bpmOff.add(comp);

        comp = new LabelledDial("Rate", this, "lfo" + lfo + "ratesyncon", color, 0, LFO_RATES_SYNC_ON.length - 1)
            {
            public String map(int value)
                {
                return LFO_RATES_SYNC_ON[value];
                }
            };
        bpmOn.add(comp);

        comp = new LabelledDial("Delay", this, "lfo" + lfo + "delaysyncon", color, 0, ENV_LFO_RATES_SYNC_ON.length - 1)
            {
            public String map(int value)
                {
                return ENV_LFO_RATES_SYNC_ON[value];
                }
            };
        bpmOn.add(comp);
        
        comp = new LabelledDial("Fade In", this, "lfo" + lfo + "fadeinsyncon", color, 0, ENV_LFO_RATES_SYNC_ON.length - 1)
            {
            public String map(int value)
                {
                return ENV_LFO_RATES_SYNC_ON[value];
                }
            };
        bpmOn.add(comp);

        bpm.add(bpmOff);
        hbox.add(bpm);
                
        comp = new LabelledDial("Phase", this, "lfo" + lfo + "phase", color, 0, 360);
        hbox.add(comp);

        comp = new LabelledDial("Smooth", this, "lfo" + lfo + "smooth", color, 0, 127);
        hbox.add(comp);        

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
                

    int lockLFOStep(int proposedState)
        {
        double v = proposedState * 8;
        v = (int)(roundEven(v / 6.4) / 10.0) - 64.0;
                
        v = roundEven(v / 5.0) * 5;
        if (v > 60) v = 60;
        if (v < -60) v = -60;
                
        v = v + 64;
        v = v * 64;
        v = v / 8;
        return (int) v;
        }

    public JComponent addLFOSteps(int lfo, Color color)
        {
        Category category = new Category(this, "LFO " + lfo + " Steps", color);
        category.makePasteable("lfo");
        category.makeDistributable("lfo");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        
        final LabelledDial[] dials = new LabelledDial[64];
        
        for(int i = 1; i <= 64; i+= 16)
            {
            hbox = new HBox();
            for(int j = i; j < i + 16; j++)
                {
                comp = dials[j - 1] = new LabelledDial("Step " + j, this, "lfo" + lfo + "step" + j, color, 0, 1024)
                    {
                    public boolean isSymmetric() { return true; }

                    // We want to override mouse movements, not setting the state in general,
                    // so we override getProposedState(MouseEvent e) rather than setState(...)
                                        
                    public int updateProposedState(int proposedState)
                        {
                        if (lockUserLFOSteps || lockAllLFOSteps)
                            {
                            proposedState = lockLFOStep(proposedState);
                            }
                        return proposedState;
                        }

                    public String map(int value)
                        {
                        double v = value * 8;
                        // dividing 8192 by 6.4 cuts into 1280 pieces
                        v = (roundEven(v / 6.4) / 10.0);
                        String str = null;
                                                
                        if (v < 64) str= String.format("-%1.1f", (64 - v));
                        else if (v > 64) str= "" + String.format("%1.1f", (v - 64));
                        else str = "--";

                        if (v == 0)
                            {
                            return str;
                            }
                        else if (v == 128 - 4)
                            {
                            return "<html><center><font size=-3><br></font>" + str + "<br><font size=-3>+1 Oct</font></center></html>";
                            }
                        else if (v == 0 + 4)
                            {
                            return "<html><center><font size=-3><br></font>" + str + "<br><font size=-3>-1 Oct</font></center></html>";
                            }
                        else 
                            {
                            double vv = (v - 64.0);
                            if (vv != 0 && (((int)vv) / 5) * 5 == vv)
                                {
                                return "<html><center><font size=-3><br></font>" + str + "<br><font size=-3>" + 
                                    (vv < 0 ? "-" : "+") + (((int)Math.abs(vv))/5) + " Semi</font></center></html>";
                                }
                            else return str;
                            }
                        }
                    };
                hbox.add(comp);
                }
            vbox.add(hbox);
            vbox.add(Strut.makeVerticalStrut(10));
            }

        String[] xs = new String[64];
        String[] ys = new String[64];
        double[] x = new double[64];
        double[] y = new double[64];
                
        for(int i = 0; i < 64; i++)
            {
            xs[i] = null;
            ys[i] = "lfo" + lfo + "step" + (i + 1);
            x[i] = (i == 0 ? 0 : 1.0 / 64);
            y[i] = 1.0 / 1024;
            }
                        
        vbox.add(Strut.makeVerticalStrut(16));
                
        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), xs, ys, x, y)
            {
            int lastIndex = -1;
                        
            // The mouseDown and mouseUp code here enables us to only do undo()
            // ONCE.
            public void mouseDown()
                {
                getUndo().push(getModel());
                getUndo().setWillPush(false);
                }
                        
            public void mouseUp()
                {
                getUndo().setWillPush(true);
                }
                                        
            public void updateFromMouse(double x, double y, boolean continuation)
                {
                if (x < 0.0)
                    x = 0.0;
                else if (x > 1.0)
                    x = 1.0;

                if (y <= 0.0) 
                    y = 0.0;
                else if (y >= 1.0) 
                    y = 1.0;
                                        
                int step = (int)(x * 64);
                if (step >= 64) step = 63;

                double val = (((int)(y * 1024)) / 8) * 8;
                                
                int proposedState = (int) Math.round(val);
                if (lockUserLFOSteps || lockAllLFOSteps)
                    {
                    proposedState = lockLFOStep(proposedState);
                    }
                                
                model.set("lfo" + lfo + "step" + (step + 1), proposedState);
                }

            public void updateHighlightIndex(int index)
                {
                if (lastIndex >= 0)
                    {
                    dials[lastIndex].setTextColor(Style.TEXT_COLOR());
                    lastIndex = -1;
                    }
                                                                        
                if (index >= 0)
                    {
                    dials[index].setTextColor(Style.DYNAMIC_COLOR());
                    lastIndex = index;
                    }
                }
                                                                                                                        
            public int highlightIndex(double x, double y, boolean continuation)
                {
                if (x < 0) x = 0;
                if (x > 1.0) x = 1.0;
                int step = (int)(x * 64);
                if (step >= 64) step = 63;
                return step;
                }

//                      public int verticalBorderThickness() { return 4; }
            };

        EnvelopeDisplay disp = (EnvelopeDisplay)comp;
        disp.setStepping(true);
        disp.setAxis(0.5);
        disp.setFinalStageKey("lfo" + lfo + "steps");
        disp.setPreferredHeight(disp.getPreferredHeight() * 2);
        
        vbox.add(comp);
        
        category.add(vbox, BorderLayout.CENTER);
        return category;
        }


    String eForm(int v, int sub, int mul, int add)
        {
        // we're dividing by 1000 but it appears the hydrasynth floors the digits to 0, so
        // we divide by 10 first to push the first digit to zero, and then divide 100.
        double val = (((int)((((v - sub) * mul) + add) / 10.0)) / 100.0);
        return String.format("%4.2f", val);
        }               


    public JComponent addEnvelope(int env, Color color)
        {
        Category category = new Category(this, "Envelope " + env + (env == 1 ? " (Filter)" : (env == 2 ? " (Amplifier)" : "")), color);
        category.makePasteable("env");
        category.makeDistributable("env");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        
        model.set("env" + env + "bpmsync", 0);          // the purpose of this is to make sure that bpm sync is FIRST in the mutation list

        if (env == 2)  // No Env 2 Trigger Source 1, but it's still a parameter!  Hydrasynth is weird
            {
            model.set("env" + env + "trigsrc" + 1, 1);
            model.setMin("env" + env + "trigsrc" + 1, 1);  
            model.setMax("env" + env + "trigsrc" + 1, 1);  

            params = ENV_TRIG_SOURCES;
            comp = new Chooser("Trigger Source " + 2, this, "env" + env + "trigsrc" + 2, params);

            vbox.add(Strut.makeStrut(comp));                // for Trigger Source 1
            vbox.add(comp);
            }
        else
            {
            params = ENV_TRIG_SOURCES;
            comp = new Chooser("Trigger Source " + 1, this, "env" + env + "trigsrc" + 1, params);
            vbox.add(comp);

            params = ENV_TRIG_SOURCES;
            comp = new Chooser("Trigger Source " + 2, this, "env" + env + "trigsrc" + 2, params);
            vbox.add(comp);
            }
         
        final HBox bpmOn = new HBox();
        final HBox bpmOff = new HBox();
        final HBox bpm = new HBox();
        
        comp = new CheckBox("BPM Sync", this, "env" + env + "bpmsync")
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                bpm.removeAll();
                if (model.get(key) == 0)
                    bpm.addLast(bpmOff);
                else
                    bpm.addLast(bpmOn);
                bpm.revalidate();
                bpm.repaint();
                }
            };
        vbox.add(comp);

        hbox.add(vbox);
        vbox = new VBox();

        params = ENV_TRIG_SOURCES;
        comp = new Chooser("Trigger Source " + 3, this, "env" + env + "trigsrc" + 3, params);
        vbox.add(comp);
                
        params = ENV_TRIG_SOURCES;
        comp = new Chooser("Trigger Source " + 4, this, "env" + env + "trigsrc" + 4, params);
        vbox.add(comp);
                
        comp = new CheckBox("Free Run", this, "env" + env + "freerun");
        vbox.add(comp);

        hbox.add(vbox);
        vbox = new VBox();

        params = QUANTIZATIONS;
        comp = new Chooser("Quantize", this, "env" + env + "quantize", params);
        model.setMetricMinMax("env" + env + "quantize", 0, QUANTIZATIONS.length - 1);
        vbox.add(comp);
                
        final VBox resetBox = new VBox();
        final CheckBox reset = new CheckBox("Reset", this, "env" + env + "reset");

        comp = new CheckBox("Legato", this, "env" + env + "legato")
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                if (model.get(key) == 0)
                    {
                    resetBox.add(reset);
                    }
                else
                    {
                    resetBox.remove(reset);
                    }
                resetBox.revalidate();
                resetBox.repaint();
                }
            };
        vbox.add(comp);
        vbox.add(resetBox);
                
        hbox.add(vbox);
        vbox = new VBox();
        
        comp = new LabelledDial("Loop", this, "env" + env + "loop", color, 0, 50)
            {
            public String map(int val)
                {
                if (val == 0) return "Off";
                if (val == 50) return "Inf";
                return "" + (val + 1);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Curve");
        hbox.add(comp);
        
        //// BUG: An apparent Hydrasynth bug mislabels EXP values as LOG for Attack and vice versa.  
        //// I do not know if this was intentional or a misunderstanding of exponential dropoff.
        comp = new LabelledDial("Attack", this, "env" + env + "atkcurve", color, 0, 128)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val)
                {
                // The &nbsp; are because centering (on the Mac anyway) appear to be controlled by
                // the widest object, which by default is the number, and when that number goes from, 
                // say, -10 to -9, it shifts its centering, which for some buggy reason causes the
                // text above it to also shift perceptibly.  So we make the text above wider so it's
                // in the driver's seat instead. 
                if (val < 64) return "<html><center><font size=-2>&nbsp;&nbsp;Exp&nbsp;&nbsp;</font><br>" + (val - 64) + "</center></html>";
                else if (val > 64) return "<html><center><font size=-2>&nbsp;&nbsp;Log&nbsp;&nbsp;</font><br>" + (val - 64) + "</center></html>"; 
                else return "Linear";
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Curve");
        hbox.add(comp);

        comp = new LabelledDial("Decay", this, "env" + env + "deccurve", color, 0, 128)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val)
                {
                // The &nbsp; are because centering (on the Mac anyway) appear to be controlled by
                // the widest object, which by default is the number, and when that number goes from, 
                // say, -10 to -9, it shifts its centering, which for some buggy reason causes the
                // text above it to also shift perceptibly.  So we make the text above wider so it's
                // in the driver's seat instead. 
                if (val < 64) return "<html><center><font size=-2>&nbsp;&nbsp;Log&nbsp;&nbsp;</font><br>" + (val - 64) + "</center></html>";
                else if (val > 64) return "<html><center><font size=-2>&nbsp;&nbsp;Exp&nbsp;&nbsp;</font><br>" + (val - 64) + "</center></html>"; 
                else return "Linear";
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Curve");
        hbox.add(comp);

        comp = new LabelledDial("Release", this, "env" + env + "relcurve", color, 0, 128)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val)
                {
                // The &nbsp; are because centering (on the Mac anyway) appear to be controlled by
                // the widest object, which by default is the number, and when that number goes from, 
                // say, -10 to -9, it shifts its centering, which for some buggy reason causes the
                // text above it to also shift perceptibly.  So we make the text above wider so it's
                // in the driver's seat instead. 
                if (val < 64) return "<html><center><font size=-2>&nbsp;&nbsp;Log&nbsp;&nbsp;</font><br>" + (val - 64) + "</center></html>";
                else if (val > 64) return "<html><center><font size=-2>&nbsp;&nbsp;Exp&nbsp;&nbsp;</font><br>" + (val - 64) + "</center></html>"; 
                else return "Linear";
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Curve");
        hbox.add(comp);


        comp = new LabelledDial("Delay", this, "env" + env + "delaysyncoff", color, 0, 127)
            {
            public String map(int value)
                {
                int v = value;
                if (v < 20) return "" + v + "ms";       // 0-20ms by 1
                else if (v < 30) return "" + (((v - 20) * 2) + 20) + "ms";      // 20-40ms by 2
                else if (v < 40) return "" + (((v - 30) * 4) + 40) + "ms";      // 40-80ms by 4
                else if (v < 50) return "" + (((v - 40) * 8) + 80) + "ms";      // 80-160ms by 8
                else if (v < 60) return "" + (((v - 50) * 16) + 160) + "ms";    // 160-320ms by 16
                else if (v < 70) return "" + (((v - 60) * 32) + 320) + "ms";    // 320ms-640ms by 32
                else if (v < 80) 
                    {
                    int i = (((v - 70) * 64) + 640);
                    if (i < 1000) return "" + i + "ms";     // 640ms-9780ms by 64
                    else return String.format("%4.2f", (i/1000.0)) + "s";   // 1024ms-1280ms by 64
                    }
                else if (v < 90) return eForm(v, 80, 128, 1280) + "s";  // 1280 - 2560 by 128
                else if (v < 100) return eForm(v, 90, 256, 2560) + "s"; // 2560 - 5120 by 256
                else if (v < 110) return eForm(v, 100, 512, 5120) + "s";        // 5120 - 9728 by 512 
                else if (v < 122) return ((v - 110) + 10) + "s";        // 10 - 22 sec by 1
                else return (((v - 122) * 2) + 22) + "s";                       // 20 - 32 sec by 2
                }
            };
        bpmOff.add(comp);

        comp = new LabelledDial("Attack", this, "env" + env + "attacksyncoff", color, 0, 1024)
            {
            public String map(int value)
                {
                int v = value * 8;
                // dividing 8192 by 64 cuts into 128 pieces
                v = (int)(v / 64);
                if (v < 20) return "" + v + "ms";       // 0-20ms by 1
                else if (v < 30) return "" + (((v - 20) * 2) + 20) + "ms";      // 20-40ms by 2
                else if (v < 40) return "" + (((v - 30) * 4) + 40) + "ms";      // 40-80ms by 4
                else if (v < 50) return "" + (((v - 40) * 8) + 80) + "ms";      // 80-160ms by 8
                else if (v < 60) return "" + (((v - 50) * 16) + 160) + "ms";    // 160-320ms by 16
                else if (v < 70) return "" + (((v - 60) * 32) + 320) + "ms";    // 320ms-640ms by 32
                else if (v < 80) 
                    {
                    int i = (((v - 70) * 64) + 640);
                    if (i < 1000) return "" + i + "ms";     // 640ms-9780ms by 64
                    else return String.format("%4.2f", (i/1000.0)) + "s";   // 1024ms-1280ms by 64
                    }
                else if (v < 90) return eForm(v, 80, 128, 1280) + "s";  // 1280 - 2560 by 128
                else if (v < 100) return eForm(v, 90, 256, 2560) + "s"; // 2560 - 5120 by 256
                else if (v < 110) return eForm(v, 100, 512, 5120) + "s";        // 5120 - 9728 by 512 
                else if (v < 120) return ((v - 110) + 10) + "s";        // 10 - 20 sec by 1
                else return (((v - 120) * 2) + 20) + "s";       // 20 - 32 sec by 2
                }
            };
        bpmOff.add(comp);

        comp = new LabelledDial("Hold", this, "env" + env + "holdsyncoff", color, 0, 1024)
            {
            public String map(int value)
                {
                int v = value * 8;
                // dividing 8192 by 64 cuts into 128 pieces
                v = roundEven(v / 64);
                if (v < 20) return "" + v + "ms";       // 0-20ms by 1
                else if (v < 30) return "" + (((v - 20) * 2) + 20) + "ms";      // 20-40ms by 2
                else if (v < 40) return "" + (((v - 30) * 4) + 40) + "ms";      // 40-80ms by 4
                else if (v < 50) return "" + (((v - 40) * 8) + 80) + "ms";      // 80-160ms by 8
                else if (v < 60) return "" + (((v - 50) * 16) + 160) + "ms";    // 160-320ms by 16
                else if (v < 70) return "" + (((v - 60) * 32) + 320) + "ms";    // 320ms-640ms by 32
                else if (v < 80) 
                    {
                    int i = (((v - 70) * 64) + 640);
                    if (i < 1000) return "" + i + "ms";     // 640ms-9780ms by 64
                    else return String.format("%4.2f", (i/1000.0)) + "s";   // 1024ms-1280ms by 64
                    }
                else if (v < 90) return eForm(v, 80, 128, 1280) + "s";  // 1280 - 2560 by 128
                else if (v < 100) return eForm(v, 90, 256, 2560) + "s"; // 2560 - 5120 by 256
                else if (v < 110) return eForm(v, 100, 512, 5120) + "s";        // 5120 - 9728 by 512 
                else if (v < 120) return ((v - 110) + 10) + "s";        // 10 - 20 sec by 1
                else return (((v - 120) * 2) + 20) + "s";       // 20 - 32 sec by 2
                }
            };
        bpmOff.add(comp);

        comp = new LabelledDial("Decay", this, "env" + env + "decaysyncoff", color, 0, 1024)
            {
            public String map(int value)
                {
                int v = value * 8;
                // dividing 8192 by 64 roughly cuts into 128 pieces
                v = roundEven(v / 64);
                if (v < 20) return "" + (((v - 0) * 2) + 0) + "ms";       // 0-40ms by 2
                else if (v < 30) return "" + (((v - 20) * 4) + 40) + "ms";      // 40-80ms by 4
                else if (v < 40) return "" + (((v - 30) * 8) + 80) + "ms";      // 80-160ms by 8
                else if (v < 50) return "" + (((v - 40) * 16) + 160) + "ms";      // 160-320ms by 16
                else if (v < 60) return "" + (((v - 50) * 32) + 320) + "ms";    // 320-640ms by 32
                else if (v < 70) 
                    {
                    int i = (((v - 60) * 64) + 640);
                    if (i < 1000) return "" + i + "ms";     // 640ms-9780ms by 64
                    else return String.format("%4.2f", (i/1000.0)) + "s";   // 1024ms-1280ms by 64
                    }
                else if (v < 80) return eForm(v, 70, 128, 1280) + "s";  // 1280 - 2560 by 128
                else if (v < 90) return eForm(v, 80, 256, 2560) + "s"; // 2560 - 5120 by 256
                else if (v < 100) return eForm(v, 90, 512, 5120) + "s";        // 5120 - 9728 by 512 
                else if (v < 106) return ((v - 100) + 10) + "s";        // 10 - 16 sec by 1
                else return (((v - 106) * 2) + 16) + "s";       // 16 - 60 sec by 2
                }
            };
        bpmOff.add(comp);

        comp = new LabelledDial("Sustain", this, "env" + env + "sustain", color, 0, 1024)
            {
            public String map(int value)
                {
                int v = value * 8;
                // dividing 8192 by 6.4 roughly cuts into 1280 pieces
                return String.format("%1.1f", (roundEven(v / 6.4) / 10.0));
                }
            };
        bpmOff.add(comp);

        comp = new LabelledDial("Release", this, "env" + env + "releasesyncoff", color, 0, 1024)
            {
            public String map(int value)
                {
                int v = value * 8;
                // dividing 8192 by 64 roughly cuts into 128 pieces
                v = roundEven(v / 64);
                if (v < 20) return "" + (((v - 0) * 2) + 0) + "ms";       // 0-40ms by 2
                else if (v < 30) return "" + (((v - 20) * 4) + 40) + "ms";      // 40-80ms by 4
                else if (v < 40) return "" + (((v - 30) * 8) + 80) + "ms";      // 80-160ms by 8
                else if (v < 50) return "" + (((v - 40) * 16) + 160) + "ms";      // 160-320ms by 16
                else if (v < 60) return "" + (((v - 50) * 32) + 320) + "ms";    // 320-640ms by 32
                else if (v < 70) 
                    {
                    int i = (((v - 60) * 64) + 640);
                    if (i < 1000) return "" + i + "ms";     // 640ms-9780ms by 64
                    else return String.format("%4.2f", (i/1000.0)) + "s";   // 1024ms-1280ms by 64
                    }
                else if (v < 80) return eForm(v, 70, 128, 1280) + "s";  // 1280 - 2560 by 128
                else if (v < 90) return eForm(v, 80, 256, 2560) + "s"; // 2560 - 5120 by 256
                else if (v < 100) return eForm(v, 90, 512, 5120) + "s";        // 5120 - 9728 by 512 
                else if (v < 106) return ((v - 100) + 10) + "s";        // 10 - 16 sec by 1
                else return (((v - 106) * 2) + 16) + "s";       // 16 - 60 sec by 2
                }
            };
        bpmOff.add(comp);

        final EnvelopeDisplay disp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "env" + env + "delaysyncoff", "env" + env + "attacksyncoff", "env" + env + "holdsyncoff", "env" + env + "decaysyncoff", null, "env" + env + "releasesyncoff" },
            new String[] { null, null, null, null, "env" + env + "sustain", "env" + env + "sustain", null },
            new double[] { 0.0, 0.1666666 / 127, 0.1666666 / 1024, 0.1666666 / 1024, 0.1666666 / 1024, 0.1666666, 0.1666666 / 1024 },
            new double[] { 0.0, 0.0, 1.0, 1.0, 1.0 / 1024, 1.0 / 1024, 0.0 })
            {
            public double getControlValue(double x1, double x2, double y1, double y2, String key, int value)
                {
                //// NOTE: An apparent Hydrasynth bug mislabels EXP values as LOG for Attack and vice versa.  
                //// I do not know if this was intentional or a misunderstanding of exponential dropoff.
                //// However the control values should be identical for attack, decay, and release.

                return (value - 64) / 64.0;
                }
            };
        disp.setCurveKeys(new String[] { null, null, "env" + env + "atkcurve", null, "env" + env + "deccurve", null, "env" + env + "relcurve" });
        bpmOff.addLast(disp);


        comp = new LabelledDial("Delay", this, "env" + env + "delaysyncon", color, 0, ENV_LFO_RATES_SYNC_ON.length - 1)
            {
            public String map(int value)
                {
                return "" + ENV_LFO_RATES_SYNC_ON[value];
                }
            };
        bpmOn.add(comp);

        comp = new LabelledDial("Attack", this, "env" + env + "attacksyncon", color, 0, ENV_LFO_RATES_SYNC_ON.length - 1)
            {
            public String map(int value)
                {
                return "" + ENV_LFO_RATES_SYNC_ON[value];
                }
            };
        bpmOn.add(comp);

        comp = new LabelledDial("Hold", this, "env" + env + "holdsyncon", color, 0, ENV_LFO_RATES_SYNC_ON.length - 1)
            {
            public String map(int value)
                {
                return "" + ENV_LFO_RATES_SYNC_ON[value];
                }
            };
        bpmOn.add(comp);

        comp = new LabelledDial("Decay", this, "env" + env + "decaysyncon", color, 0, ENV_LFO_RATES_SYNC_ON.length - 1)
            {
            public String map(int value)
                {
                return "" + ENV_LFO_RATES_SYNC_ON[value];
                }
            };
        bpmOn.add(comp);

        comp = new LabelledDial("Sustain", this, "env" + env + "sustain", color, 0, 1024)
            {
            public String map(int value)
                {
                int v = value * 8;
                // dividing 8192 by 6.4 roughly cuts into 1280 pieces
                return String.format("%1.1f", (roundEven(v / 6.4) / 10.0));
                }
            };
        bpmOn.add(comp);

        comp = new LabelledDial("Release", this, "env" + env + "releasesyncon", color, 0, ENV_LFO_RATES_SYNC_ON.length - 1)
            {
            public String map(int value)
                {
                return "" + ENV_LFO_RATES_SYNC_ON[value];
                }
            };
        bpmOn.add(comp);

        // replacement for missing envelope display
        bpmOn.add(Strut.makeStrut(disp, true));
                
        bpm.addLast(bpmOff);
        hbox.addLast(bpm);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
                


    public static final int MAXIMUM_MACRO_NAME_LENGTH = 8;
    public String reviseMacroName(String name)
        {
        if (name.length() > MAXIMUM_MACRO_NAME_LENGTH)
            name = name.substring(0, MAXIMUM_MACRO_NAME_LENGTH);
        
        StringBuffer nameb = new StringBuffer(name);                            
        for(int i = 0 ; i < nameb.length(); i++)
            {
            char c = nameb.charAt(i);
            if (c < ' ' || c > 126 || invalidChar(c))
                nameb.setCharAt(i, ' ');
            }
        name = nameb.toString();
        return name;
        }        


    public JComponent addMacro(int macro, Color color)
        {
        Category category = new Category(this, "Macro " + macro, color);
        category.makePasteable("macro");
        category.makeDistributable("macro");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();

        VBox vbox = new VBox();
        comp = new StringComponent("Name", this, "macro" + macro + "name", 8, "Macro name must be up to 8 ASCII characters,\nnot including control characters, DELETE,\nor any of the following:  \" * \\ | / < > ? ; | ~")
            {
            public String replace(String val)
                {
                return reviseMacroName(val);
                }
            public String[] getList()
                {
                return MACRO_NAME_PRESETS;
                }
            };
        vbox.add(comp);
        
        comp = new LabelledDial("Panel Value", this, "macro" + macro + "panelvalue", color, 0, 1024)
            {
            public String map(int value)
                {
                int v = value * 8;
                // dividing 8192 by 6.4 cuts into 2560 pieces
                return String.format("%1.1f", ((roundEven(v / 6.4) / 10.0)));
                }
            };
        vbox.add(comp);

        comp = new CheckBox("Panel Button", this, "macro" + macro + "panelbuttonstate");
        vbox.add(comp);

        hbox.add(vbox);
                
        vbox = new VBox();
        for(int i = 1; i <= 8; i+= 2)
            {
            VBox inner = new VBox();
            HBox outer = new HBox();
                        
            params = MOD_DESTINATIONS;
            comp = new Chooser("Destination " + i, this, "macro" + macro + "target" + i, params);
            inner.add(comp);
                
            params = MOD_DESTINATIONS;
            comp = new Chooser("Destination " + (i + 1), this, "macro" + macro + "target" + (i + 1), params);
            inner.add(comp);
            outer.add(inner);
                        
            comp = new LabelledDial("Depth " + i, this, "macro" + macro + "depth" + i, color, 0, 1024)
                {
                public boolean isSymmetric() { return true; }
                public String map(int value)
                    {
                    int v = value * 8;
                    // dividing 8192 by 3.2 cuts into 2560 pieces
                    return String.format("%1.1f", ((roundEven(v / 3.2) / 10.0) - 128));
                    }
                };
            outer.add(comp);

            comp = new LabelledDial("Button " + i, this, "macro" + macro + "buttonvalue" + i, color, 0, 1024)
                {
                public boolean isSymmetric() { return true; }
                public String map(int value)
                    {
                    int v = value * 8;
                    // dividing 8192 by 3.2 cuts into 2560 pieces
                    return String.format("%1.1f", ((roundEven(v / 3.2) / 10.0) - 128));
                    }
                };
            outer.add(comp);

            comp = new LabelledDial("Depth " + (i+1), this, "macro" + macro + "depth" + (i+1), color, 0, 1024)
                {
                public boolean isSymmetric() { return true; }
                public String map(int value)
                    {
                    int v = value * 8;
                    // dividing 8192 by 3.2 cuts into 2560 pieces
                    return String.format("%1.1f", ((roundEven(v / 3.2) / 10.0) - 128));
                    }
                };
            outer.add(comp);

            comp = new LabelledDial("Button " + (i+1), this, "macro" + macro + "buttonvalue" + (i+1), color, 0, 1024)
                {
                public boolean isSymmetric() { return true; }
                public String map(int value)
                    {
                    int v = value * 8;
                    // dividing 8192 by 3.2 cuts into 2560 pieces
                    return String.format("%1.1f", ((roundEven(v / 3.2) / 10.0) - 128));
                    }
                };
            outer.add(comp);
            vbox.add(outer);
            }
        hbox.add(vbox);
                        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addArp(Color color)
        {
        Category category = new Category(this, "Arpeggiator", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox(); 

        comp = new CheckBox("Enable", this, "arpenable");
        vbox.add(comp);

        comp = new CheckBox("Latch", this, "arplatch");
        vbox.add(comp);

        hbox.add(vbox);
        vbox = new VBox();

        comp = new CheckBox("Clock Lock", this, "arpclklock");
        ((CheckBox)comp).addToWidth(1);
        vbox.add(comp);

        comp = new CheckBox("Tap Trig", this, "arptaptrig");
        ((CheckBox)comp).addToWidth(1);
        vbox.add(comp);

        hbox.add(vbox);
        vbox = new VBox();

        params = ARP_MODES;
        comp = new Chooser("Mode", this, "arpmode", params);
        vbox.add(comp);

        params = ARP_OCTAVE_MODES;
        comp = new Chooser("Octave Mode", this, "arpoctmode", params);
        vbox.add(comp);
                
        hbox.add(vbox);
        vbox = new VBox();

        params = ARP_DIVISIONS;
        comp = new Chooser("Division", this, "arpdivision", params);
        vbox.add(comp);

        hbox.add(vbox);

        comp = new LabelledDial("Tempo", this, "arptempo", color, 300, 2400)
            {
            public String map(int val)
                {
                return "" + (val / 10) + "." + (val % 10);
                }
            };
        hbox.add(comp);
        
        comp = new LabelledDial("Octave", this, "arpoctave", color, 1, 6);
        hbox.add(comp);

        comp = new LabelledDial("Gate", this, "arpgate", color, 5, 100);
        hbox.add(comp);

        comp = new LabelledDial("Swing", this, "arpswing", color, 50, 75);
        hbox.add(comp);

        comp = new LabelledDial("Length", this, "arplength", color, 0, 32)
            {
            public String map(int value)
                {
                if (value == 0) return "Default";
                else return "" + value;
                }
            };
                
        hbox.add(comp);

        comp = new LabelledDial("Phrase", this, "arpphrase", color, 1, 64);
        hbox.add(comp);

        comp = new LabelledDial("Ratchet", this, "arpratchet", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Chance", this, "arpchance", color, 0, 100)
            {
            public String map(int value)
                {
                return "" + value + "%";
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Step", this, "arpstepoffset", color, 0, 64)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                return "" + (value - 32);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Offset");
        hbox.add(comp);


        category.add(hbox, BorderLayout.CENTER);
        return category;
        }



    public JComponent addMatrix(Color color)
        {
        Category category = new Category(this, "Mod Matrix", color);
        category.makeDistributable("modmatrix");

        JComponent comp;
        String[] params;

        VBox outer = new VBox();
        for(int i = 1; i <= 32; i+=5)
            {
            HBox hbox = new HBox();
            for(int j = i; j < Math.min(i + 5, 33); j++)
                {
                VBox vbox = new VBox();
                params = MOD_SOURCES;
                comp = new Chooser("Source " + j + " ", this, "modmatrix" + j + "modsource", params);
                vbox.add(comp);

                params = MOD_DESTINATIONS;
                comp = new Chooser("Destination " + j + " ", this, "modmatrix" + j + "modtarget", params);
                vbox.add(comp);
                hbox.add(vbox);

                comp = new LabelledDial(" Depth " + j + " ", this, "modmatrix" + j + "depth", color, 0, 1024)
                    {
                    public boolean isSymmetric() { return true; }
                    public String map(int value)
                        {
                        int v = value * 8;
                        // dividing 8192 by 3.2 cuts into 2560 pieces
                        return String.format("%1.1f", ((roundEven(v / 3.2) / 10.0) - 128));
                        }
                    };
                hbox.add(comp);
                if (j != i + 4) hbox.add(Strut.makeHorizontalStrut(6));
                }
            outer.add(hbox);
            if (i != 31) outer.add(Strut.makeVerticalStrut(12));
            }
                                
        category.add(outer, BorderLayout.CENTER);
        return category;
        }

        

    boolean lockUserLFOSteps;
    boolean lockAllLFOSteps;
    boolean ignoreParametersFromSynth;
    boolean disallowCCMutation;
        
    public void addHydrasynthMenu()
        {
        JMenu menu = new JMenu("Hydrasynth");
        menubar.add(menu);

        JCheckBoxMenuItem ignoreParametersMenu = new JCheckBoxMenuItem("Ignore Parameters from Synth");
        ignoreParametersMenu.setSelected(ignoreParametersFromSynth);
        menu.add(ignoreParametersMenu);
        ignoreParametersMenu.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt)
                {
                ignoreParametersFromSynth = ignoreParametersMenu.isSelected();
                setLastX("" + ignoreParametersFromSynth, "IgnoreParametersFromSynth", getSynthClassName(), true);
                }
            });

        JCheckBoxMenuItem disallowCCMutationMenu = new JCheckBoxMenuItem("Disallow CC Mutation");
        disallowCCMutationMenu.setSelected(disallowCCMutation);
        menu.add(disallowCCMutationMenu);
        disallowCCMutationMenu.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt)
                {
                disallowCCMutation = disallowCCMutationMenu.isSelected();
                setLastX("" + disallowCCMutation, "DisallowCCMutation", getSynthClassName(), true);
                }
            });

        JCheckBoxMenuItem lockUserLFOStepsMenu = new JCheckBoxMenuItem("Lock User LFO Steps to Notes");
        lockUserLFOStepsMenu.setSelected(lockUserLFOSteps);
        menu.add(lockUserLFOStepsMenu);
        lockUserLFOStepsMenu.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt)
                {
                lockUserLFOSteps = lockUserLFOStepsMenu.isSelected();
                setLastX("" + lockUserLFOSteps, "LockUserLFOSteps", getSynthClassName(), true);
                }
            });

        JCheckBoxMenuItem lockAllLFOStepsMenu = new JCheckBoxMenuItem("Lock All LFO Steps to Notes");
        lockAllLFOStepsMenu.setSelected(lockAllLFOSteps);
        menu.add(lockAllLFOStepsMenu);
        lockAllLFOStepsMenu.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt)
                {
                lockAllLFOSteps = lockAllLFOStepsMenu.isSelected();
                }
            });
        }
                        
    public JFrame sprout()
        {
        JFrame frame = super.sprout();
        addHydrasynthMenu();
        receiveCurrent.setEnabled(false);
        return frame;
        }         

    public void windowCreated()
        {
        //setLastX("false", "Warned", getSynthClassName(), true);
        showOneTimeWarning("Warned", "Read the About Tab", "The Hydrasynth has many eccentricities to be aware of.\nBe certain to fully read the About Tab before use.\n\n"+
            "Particularly read the warnings about Windows and Java MIDI,\nand about Send to Current Patch.");
        }

    public void showedOneTimeWarning(String key)
        {
        setSelectedTabIndex(getIndexOfTabTitle("About"));
        }

    public String getPatchLocationName(Model model)
        {
        // getPatchLocationName() is called from sprout() as a test to see if we should enable
        // batch downloading.  If we haven't yet created an .init file, then parameters won't exist
        // yet and this method will bomb badly.  So we return null in this case.
        if (!model.exists("number")) return null;
                
        int number = (model.get("number") + 1);
        int bank = (model.get("bank"));
        return BANKS[bank] + " " + ((number > 99 ? "" : (number > 9 ? "0" : "00")) + number);
        }

    public int getBatchDownloadWaitTime() { return 50; }                                // this will make a lot of "tardy" messages but it's a tiny bit faster than 400
    public int getBatchDownloadFailureCountdown() { return 100; }

    // A small syntatic shortening function for emitAll(...) to return the parameter value.
    int p(String key)
        {
        Object obj = parametersToIndex.get(key);
        if (obj == null) return 0;
        return nrpn[((Integer)obj).intValue()];
        }
                
                
    /// The Hydrasynth NRPN is a mess, requiring a very high degree of customization
    /// per-parameter to emit NRPN values.  This is *extremely* unfortunate -- it is so highly
    /// customized and arbitrary that I can't do a table lookup of range informaton per-parameter 
    /// and have to resort to a giant if-tree, which is gonna be slow.  :-(  :-(  It's also 
    /// gonna be a mess parsing these back in from the Hydrasynth...
    public Object[] emitAll(String key)
        {
        if (key.equals("bank") || key.equals("number") || key.equals("--"))
            {
            return new Object[0];
            }
        
        // There is no NRPN for these items
        if (key.equals("color") || 
            key.equals("category") || 
            (key.startsWith("macro") && (key.endsWith("name"))) || 
            key.equals("name"))
            {
            return null;
            }
                        
        int p = p(key);
        if (p == 0) return null;
        
        int v = 0;
        int w = 0;
        int val = model.get(key, 0);
        
        if (key.startsWith("osc"))
            {
            if (key.equals("osc1mode"))
                {
                p = p("osc1mode");
                v = 0;
                w = val;
                val = v * 128 + w;
                }
            else if (key.equals("osc2mode"))
                {
                p = p("osc1mode");
                v = 1;
                w = val;
                val = v * 128 + w;
                }
            /// NOTE: This parameter doesn't exist (osc3 doesn't have a mode)
            /// But we're keeping it here for consistency with the buggy documentation
            else if (key.equals("osc3mode"))
                {
                p = p("osc1mode");
                v = 2;
                w = val;
                val = v * 128 + w;
                }
            else if (key.equals("osc1semi"))
                {
                p = p("osc1semi");
                v = 0;
                if (val < 0) val = val + 128;
                w = val;                        // two's complement :-(
                val = v * 128 + w;
                }
            else if (key.equals("osc2semi"))
                {
                p = p("osc1semi");
                v = 1;
                if (val < 0) val = val + 128;
                w = val;                        // two's complement :-(
                val = v * 128 + w;
                }
            else if (key.equals("osc3semi"))
                {
                p = p("osc1semi");
                v = 2;
                if (val < 0) val = val + 128;
                w = val;                        // two's complement :-(
                val = v * 128 + w;
                }
            else if (key.equals("osc1wavscan") || 
                key.equals("osc2wavscan"))
                {
                val = val * 8;
                }
            else if (key.equals("osc1cent") || 
                key.equals("osc2cent") || 
                key.equals("osc3cent"))
                {
                if (val < 0) val += 8192;
                }
            /// FIXME: It is likely that osc1solowavscan1 etc. don't do anything at all.
            else if (key.startsWith("osc1solowavscan"))
                {
                p = p("osc1solowavscan1");
                w = val;
                if (key.equals("osc1solowavscan1"))
                    {
                    v = 0;
                    }
                else if (key.equals("osc1solowavscan2"))
                    {
                    v = 1;
                    }
                else if (key.equals("osc1solowavscan3"))
                    {
                    v = 2;
                    }
                else if (key.equals("osc1solowavscan4"))
                    {
                    v = 3;
                    }
                else if (key.equals("osc1solowavscan5"))
                    {
                    v = 4;
                    }
                else if (key.equals("osc1solowavscan6"))
                    {
                    v = 5;
                    }
                else if (key.equals("osc1solowavscan7"))
                    {
                    v = 6;
                    }
                else // if (key.equals("osc1solowavscan8"))
                    {
                    v = 7;
                    }
                val = v * 128 + w;
                }
            else if (key.startsWith("osc2solowavscan"))
                {
                p = p("osc2solowavscan1");
                w = val;
                if (key.equals("osc2solowavscan1"))
                    {
                    v = 0;
                    }
                else if (key.equals("osc2solowavscan2"))
                    {
                    v = 1;
                    }
                else if (key.equals("osc2solowavscan3"))
                    {
                    v = 2;
                    }
                else if (key.equals("osc2solowavscan4"))
                    {
                    v = 3;
                    }
                else if (key.equals("osc2solowavscan5"))
                    {
                    v = 4;
                    }
                else if (key.equals("osc2solowavscan6"))
                    {
                    v = 5;
                    }
                else if (key.equals("osc2solowavscan7"))
                    {
                    v = 6;
                    }
                else // if (key.equals("osc2solowavscan8"))
                    {
                    v = 7;
                    }
                val = v * 128 + w;
                }
            else if (key.endsWith("bitreduction"))
                {
                if (key.equals("osc1bitreduction"))
                    {
                    p = p("osc1bitreduction");
                    v = 0;
                    w = val;
                    val = v * 128 + w;
                    }
                else if (key.equals("osc2bitreduction"))
                    {
                    p = p("osc1bitreduction");
                    v = 1;
                    w = val;
                    val = v * 128 + w;
                    }
                else    // if (key.equals("osc3bitreduction"))
                    {
                    p = p("osc1bitreduction");
                    v = 2;
                    w = val;
                    val = v * 128 + w;
                    }
                }
            }
        else if (key.startsWith("mutant"))
            {
            if (key.equals("mutant1mode"))
                {
                p = p("mutant1mode");
                v = 0;
                w = val;
                val = v * 128 + w;
                }
            else if (key.equals("mutant2mode"))
                {
                p = p("mutant1mode");
                v = 1;
                w = val;
                val = v * 128 + w;
                }
            else if (key.equals("mutant3mode"))
                {
                p = p("mutant1mode");
                v = 2;
                w = val;
                val = v * 128 + w;
                }
            else if (key.equals("mutant4mode"))
                {
                p = p("mutant1mode");
                v = 3;
                w = val;
                val = v * 128 + w;
                }
            else if (key.equals("mutant1sourcefmlin"))
                {
                p = p("mutant1sourcefmlin");
                v = 0;
                w = val;
                val = v * 128 + w;
                }
            else if (key.equals("mutant2sourcefmlin"))
                {
                p = p("mutant1sourcefmlin");
                v = 1;
                w = val;
                val = v * 128 + w;
                }
            else if (key.equals("mutant3sourcefmlin"))
                {
                p = p("mutant1sourcefmlin");
                v = 2;
                w = val;
                val = v * 128 + w;
                }
            else if (key.equals("mutant4sourcefmlin"))
                {
                p = p("mutant1sourcefmlin");
                v = 3;
                w = val;
                val = v * 128 + w;
                }
            else if (key.equals("mutant1sourceoscsync"))
                {
                p = p("mutant1sourceoscsync");
                v = 0;
                w = val;
                val = v * 128 + w;
                }
            else if (key.equals("mutant2sourceoscsync"))
                {
                p = p("mutant1sourceoscsync");
                v = 1;
                w = val;
                val = v * 128 + w;
                }
            else if (key.equals("mutant3sourceoscsync"))
                {
                p = p("mutant1sourceoscsync");
                v = 2;
                w = val;
                val = v * 128 + w;
                }
            else if (key.equals("mutant4sourceoscsync"))
                {
                p = p("mutant1sourceoscsync");
                v = 3;
                w = val;
                val = v * 128 + w;
                }
            else
                {
                String subkey = key.substring(7);
                if (subkey.equals("ratio") || 
                    subkey.equals("depth") || 
                    subkey.equals("feedback") || 
                    subkey.equals("wet") || 
                    subkey.equals("window") || 
                    subkey.startsWith("warp"))
                    {
                    val = val * 8;
                    }
                }
            }
        else if (key.startsWith("ring"))
            {
            if (key.equals("ringmoddepth"))
                {
                val = val * 8;
                }
            if (key.equals("ringmodsource1"))
                {
                p = p("ringmodsource1");
                v = 0;
                w = val;
                val = v * 128 + w;
                }
            if (key.equals("ringmodsource2"))
                {
                p = p("ringmodsource1");
                v = 1;
                w = val;
                val = v * 128 + w;
                }
            }
        else if (key.startsWith("mix"))
            {
            if (!key.equals("mixerfilterrouting"))
                {
                val = val * 8;
                }
            }
        else if (key.startsWith("filter"))
            {
            String subkey = key.substring(7);
            if (subkey.equals("cutoff") ||
                subkey.equals("drive") ||
                subkey.equals("resonance") ||
                subkey.equals("special") || 
                subkey.equals("morph") ||
                subkey.equals("lfo1amount") ||
                subkey.equals("velenv") ||
                subkey.equals("env1amount") || 
                subkey.equals("keytrack") ||
                subkey.equals("morph"))
                {
                val = val * 8;
                }
            }
        else if (key.startsWith("amp"))
            {
            val = val * 8;
            }
        else if (key.startsWith("pre"))
            {
            if (!key.equals("prefxwet") && !key.equals("prefxtype") && !key.equals("prefxpreset") && !key.equals("prefxsidechain"))
                {
                // It's one of the 5 parameters
                int type = (int)(key.charAt(5) - '0');   // as in prefxN...                     // don't need this?
                if (type != model.get("prefxtype"))     // it's not the right set
                    return null;
                int param = (int)(key.charAt(11) - '0');  // as in prefxNparamM
                p = p("prefxparam" + param);
                }
            val = val * 8;
            }
        else if (key.startsWith("delay"))
            {
            val = val * 8;
            }
        else if (key.startsWith("reverb"))
            {
            val = val * 8;
            }
        else if (key.startsWith("post"))
            {
            if (!key.equals("postfxwet") && !key.equals("postfxtype") && !key.equals("postfxpreset") && !key.equals("postfxsidechain"))
                {
                // It's one of the 5 parameters
                int type = (int)(key.charAt(6) - '0');   // as in postfxN...                    // don't need this?
                if (type != model.get("postfxtype"))    // it's not the right set
                    return null;
                int param = (int)(key.charAt(12) - '0');  // as in postfxNparamM
                p = p("postfxparam" + param);
                }
            val = val * 8;
            }
        else if (key.startsWith("lfo"))
            {
            String subkey = key.substring(4);
            if (subkey.startsWith("wave") ||
                subkey.startsWith("bpmsync") ||
                subkey.startsWith("trigsync") ||
                subkey.startsWith("smooth") ||
                subkey.startsWith("steps") ||
                subkey.startsWith("delaysyncoff") ||
                subkey.startsWith("fadeinsyncoff") ||
                subkey.startsWith("delaysyncon") ||
                subkey.startsWith("fadeinsyncon") ||
                subkey.startsWith("oneshot"))
                {
                if (subkey.startsWith("delaysyncon") || subkey.startsWith("fadeinsyncon"))
                    {
                    // This little stupidity is because a bug in the hydrasynth doesn't distinguish between
                    // delay/rate sync on and delay/rate sync off in NRPN
                    int type = (int)(key.charAt(3) - '0');   // as in envN...
                    if (model.get("lfo" + type + "bpmsync") == 0) // BPM is off, need to bail
                        return null;
                    }
                if (subkey.startsWith("delaysyncoff") || subkey.startsWith("fadeinsyncoff"))
                    {
                    // This little stupidity is because a bug in the hydrasynth doesn't distinguish between
                    // delay/rate sync on and delay/rate sync off in NRPN
                    // This one is probably not needed -- it's really syncon that's the problem,
                    // but just for good measure...
                    int type = (int)(key.charAt(3) - '0');   // as in envN...
                    if (model.get("lfo" + type + "bpmsync") == 1) // BPM is on, need to bail
                        return null;
                    }


                int lfo = StringUtility.getFirstInt(key);
                p = p("lfo1wave") + lfo - 1;
                // ugh... is this faster than a hashtable lookup?  Gross.
                if (subkey.startsWith("wave")) v = 0x00;
                else if (subkey.startsWith("bpmsync")) v = 0x01;
                else if (subkey.startsWith("trigsync")) v = 0x03;
                else if (subkey.startsWith("smooth")) v = 0x06;
                else if (subkey.startsWith("steps")) v = 0x07;
                else if (subkey.startsWith("delaysyncoff")) v = 0x11;
                else if (subkey.startsWith("fadeinsyncoff")) v = 0x12;
                else if (subkey.startsWith("delaysyncon")) v = 0x21;
                else if (subkey.startsWith("fadeinsyncon")) v = 0x13;
                else // if (subkey.startsWith("oneshot")) 
                    v = 0x14;
                w = val;
                val = v * 128 + w;
                }
            else if (subkey.startsWith("step"))
                {
                val = val * 8;
                }
            else if (subkey.startsWith("rate"))             // both ratesyncon and ratesyncoff
                {
                val = val * 8;
                }
            else if (subkey.startsWith("level"))
                {
                val = val * 8;
                }
            /// THIS IS A BUG -- THE HYDRASYNTH PROPERLY MULTPLIES BY 8
            /// ON OUTPUT BUT DOES NOT DIVIDE BY 8 ON INPUT (2.0.0)
            /*
              else if (subkey.endsWith("quantize"))
              {
              val = val * 8;
              }
            */
            }
        else if (key.startsWith("env"))
            {
            String subkey = key.substring(4);
            if (subkey.startsWith("delaysyncoff"))
                {
                // This little stupidity is because a bug in the hydrasynth doesn't distinguish between
                // delay sync on and delay sync off in NRPN
                int type = (int)(key.charAt(3) - '0');   // as in envN...
                if (model.get("env" + type + "bpmsync") == 1) // BPM is on, need to bail
                    return null;
                val = val + (0x08 * 128);                       // MSB is 0x08 for some reason
                }
            else if (subkey.startsWith("delaysyncon"))
                {
                // This little stupidity is because a bug in the hydrasynth doesn't distinguish between
                // delay sync on and delay sync off in NRPN
                int type = (int)(key.charAt(3) - '0');   // as in envN...
                if (model.get("env" + type + "bpmsync") == 0) // BPM is off, need to bail
                    return null;
                val = val + (0x18 * 128);                       // MSB is 0x18 for some reason
                }
            else if (subkey.startsWith("attacksyncoff") ||
                subkey.startsWith("holdsyncoff") ||
                subkey.startsWith("decaysyncoff") ||
                subkey.startsWith("sustain") ||
                subkey.startsWith("releasesyncoff") || 
                subkey.startsWith("attacksyncon") ||
                subkey.startsWith("holdsyncon") ||
                subkey.startsWith("decaysyncon") ||
                subkey.startsWith("releasesyncon") ||
                subkey.startsWith("trigsrc"))
                {
                val = val * 8;
                }
            else if (subkey.startsWith("loop") ||
                subkey.startsWith("legato") ||
                subkey.startsWith("bpmsync") ||
                subkey.startsWith("freerun") ||
                subkey.startsWith("reset"))
                {
                int env = StringUtility.getFirstInt(key);
                p = p("env1loop") + env - 1;    // 0x3F 0x00 ... 0x3F 0x04
                if (env == 5)  // special case this, it's a weird hydrasynth thing
                    p =  p("env1loop") + 0x0A;
                // ugh... is this faster than a hashtable lookup?  Gross.
                if (subkey.startsWith("loop")) v = 0x06;
                else if (subkey.startsWith("legato")) v = 0x07;
                else if (subkey.startsWith("bpmsync")) v = 0x0C;
                else if (subkey.startsWith("freerun")) v = 0x0D;
                else // if (subkey.startsWith("reset")) 
                    v = 0x0F;
                w = val;
                val = v * 128 + w;
                }
            /// THIS IS A BUG -- THE HYDRASYNTH PROPERLY MULTPLIES BY 8
            /// ON OUTPUT BUT DOES NOT DIVIDE BY 8 ON INPUT (2.0.0)
            /*
              else if (subkey.endsWith("quantize"))
              {
              val = val * 8;
              }
            */
            }
        else if (key.startsWith("arp"))
            {
            if (key.equals("arpenable"))
                {
                p = p("arpdivision");
                v = 0;
                w = val;
                val = v * 128 + w;
                }
            else if (key.equals("arpdivision"))
                {
                p = p("arpdivision");
                v = 1;
                w = val;
                val = v * 128 + w;
                }
            else if (key.equals("arpswing"))
                {
                p = p("arpdivision");
                v = 2;
                w = val;
                val = v * 128 + w;
                }
            else if (key.equals("arpgate"))
                {
                p = p("arpdivision");
                v = 3;
                w = val;
                val = v * 128 + w;
                }
            else if (key.equals("arpoctmode"))
                {
                p = p("arpdivision");
                v = 4;
                w = val;
                val = v * 128 + w;
                }
            else if (key.equals("arpoctave"))
                {
                p = p("arpdivision");
                v = 5;
                w = val;
                val = v * 128 + w;
                }
            else if (key.equals("arpmode"))
                {
                p = p("arpdivision");
                v = 6;
                w = val;
                val = v * 128 + w;
                }
            else if (key.equals("arplength"))
                {
                p = p("arpdivision");
                v = 7;
                w = val;
                val = v * 128 + w;
                }
            else if (key.equals("arptaptrig"))
                {
                p = p("arpdivision");
                v = 8;
                w = val;
                val = v * 128 + w;
                }
            else if (key.equals("arpphrase"))
                {
                p = p("arpdivision");
                v = 9;
                w = val;
                val = v * 128 + w;
                }
            else if (key.equals("arpratchet"))
                {
                p = p("arpdivision");
                v = 10;
                w = val;
                val = v * 128 + w;
                }
            else if (key.equals("arpchance"))
                {
                p = p("arpdivision");
                v = 11;
                w = val;
                val = v * 128 + w;
                }
            }
        else if (key.startsWith("macro"))
            {
            String subkey = key.substring(6);
            if (subkey.startsWith("target"))
                {
                // Documentation is very wrong.  Actually the values are all over the place. 
                val = MOD_DESTINATION_NRPN_VALUES[val];
                }
            else if (subkey.startsWith("buttonvalue"))
                {
                val = val * 8;
                }
            else if (subkey.startsWith("depth"))
                {
                val = val * 8;
                }
            }
        else if (key.startsWith("modmatrix"))
            {
            String subkey = StringUtility.removePreambleAndFirstDigits(key, "modmatrix");
            if (subkey.startsWith("modsource"))
                {
                // Documentation is very wrong.  Actually the values are all over the place. 
                val = MOD_SOURCE_NRPN_VALUES[val];
                }
            else if (subkey.startsWith("modtarget"))
                {
                // Documentation is very wrong.  Actually the values are all over the place. 
                val = MOD_DESTINATION_NRPN_VALUES[val];
                }
            else if (subkey.startsWith("depth"))
                {
                val = val * 8;
                }
            }
        else if (key.startsWith("ribbon"))
            {
            // interesting that v doesn't start with 0.  Not sure why
            if (key.equals("ribbonmode"))
                {
                p = p("ribbonmode");
                v = 0;
                w = val;
                val = v * 128 + w;
                }
            else if (key.equals("ribbonkeyspan"))
                {
                p = p("ribbonmode");
                v = 1;
                w = val;
                val = v * 128 + w;
                }
            else if (key.equals("ribbonoctave"))
                {
                p = p("ribbonmode");
                v = 2;
                w = val;
                val = v * 128 + w;
                }
            else if (key.equals("ribbonquantize"))
                {
                p = p("ribbonmode");
                v = 3;
                w = val;
                val = v * 128 + w;
                }
            else if (key.equals("ribbonmodcontrol"))
                {
                p = p("ribbonmode");
                v = 16;                         // no idea why the jump
                w = val;
                val = v * 128 + w;
                }
            else if (key.equals("ribbonglide"))
                {
                p = p("ribbonmode");
                v = 17;
                w = val;
                val = v * 128 + w;
                }
            }
        else if (key.startsWith("voice"))
            {
            /// THIS IS A BUG -- THE HYDRASYNTH PROPERLY MULTPLIES BY 8
            /// ON OUTPUT BUT DOES NOT DIVIDE BY 8 ON INPUT (2.0.0)
            /*
              if (key.equals("voicesustain"))
              {
              val = val * 8;
              }
            */
            /// THIS IS A BUG -- THE HYDRASYNTH PROPERLY MULTPLIES BY 8
            /// ON OUTPUT BUT DOES NOT DIVIDE BY 8 ON INPUT (2.0.0)
            /*
              else if (key.startsWith("voicemodulation"))
              {
              val = val * 8;
              }
            */
            }
        else
            {
            val = model.get(key);
            }
        return buildNRPN(getChannelOut(), p, val);
        }



    void get1(String key, byte[] data, int pos)
        {
        get2(key, data, pos);           // gotta consider negative values with extension
        }

    void get2(String key, byte[] data, int pos)
        {
        int val = model.get(key);
        if (model.getMin(key) < 0)      // signed two's complement
            {
            data[pos] = (byte)(val & 0xFF);
            data[pos+1] = (byte)((val >>> 8) & 0xFF);
            }
        else
            {
            data[pos] = (byte)(val & 0xFF);
            data[pos+1] = (byte)((val >>> 8) & 0xFF);
            }
        }


    public boolean getSendsParametersAfterLoad() { return false; }
       
       
    Integer PAUSE_AFTER_CHUNK = null;           // At present we're not doing any pauses after each chunk.  It seems to be okay
    
    int PAUSE_AFTER_WRITE_REQUEST = 3500;       // A huge number
    
    /** The Hydrasynth doesn't have a useful sysex emit mechanism, so we're inventing one here solely for
        the purposes of writing to a file. */
    public Object[] emitAll(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        if (tempModel == null)
            tempModel = getModel();
            
        byte[] data = new byte[2790];

        // Fill in header
        data[0] = (byte) 0x06;          // save to RAM
        data[1] = (byte) 0x00;
        if (toWorkingMemory)
            {
            // we update to our scratch patch.  Otherwise if the user later
            // writes a patch, he may write the changes to the updated patch
            // by accident because the Hydrasynth writes whole banks at a time
            data[2] = (byte) 7;                                                     // H
            data[3] = (byte) 127;                                           // 128
            }
        else
            {
            data[2] = (byte) tempModel.get("bank");
            data[3] = (byte) tempModel.get("number");
            }
                
        data[4] = (byte) VERSION_2_0_0;         // 1.5.5.  Change to 0xC8 for 2.0.0
        data[5] = (byte) 0x00;
        data[6] = (byte) 0x00;
        data[7] = (byte) 0x00;
        // may be only 1 byte
        data[8] = (byte) model.get("category");
                
        // BASICS
        char[] name = (model.get("name", "Untitled") + "                ").toCharArray();
        for(int i = 0; i < 16; i++)
            {
            data[9 + i] = (byte)name[i];
            }
        // may be only 1 byte
        data[26] = (byte) model.get("color");

        // VOICE
        get1("voicepolyphony", data, 30);
        get1("voicedensity", data, 32);
        get1("voicedetune", data, 34);
        get1("voiceanalogfeel", data, 36);
        get1("voicerandomphase", data, 38);
        get1("voicestereomode", data, 40);
        get1("voicestereowidth", data, 42);
        get1("voicepitchbend", data, 44);
        get1("voicevibratobpmsync", data, 50);
        if (model.get("voicevibratobpmsync") == 0)
            {
            get1("voicevibratoratesyncoff", data, 48);
            }
        else
            {
            get1("voicevibratoratesyncon", data, 48);
            }
        get1("voiceglide", data, 52);
        get1("voiceglidetime", data, 54);
        get1("voiceglidecurve", data, 56);
        get1("voiceglidelegato", data, 58);

        /// CUSTOM VIBRATO AMOUNT FOR 2.0.0
        data[46] = (byte)(model.get("voicevibratoamount") / 10);
        data[2462] = (byte)(model.get("voicevibratoamount") % 10);

        // SCALES
                
        get1("scalekeylock", data, 60);
        get1("scaletype", data, 62);
        for(int i = 2; i < 9; i++)
            {
            get1("scalenote" + i, data, 62 + i * 2);
            }               

        // OSCS
        for(int i = 1; i < 3; i++)
            {
            int p = (i == 1 ? 80 : 108);
            get1("osc" + i + "mode", data, p + 0);
            get2("osc" + i + "type", data, p + 2);  // the wave
            get1("osc" + i + "semi", data, p + 4);
            get1("osc" + i + "cent", data, p + 6);
            get1("osc" + i + "keytrack", data, p + 8);
            get2("osc" + i + "wavscan", data, p + 10);
            for(int j = 0; j < 8; j++)
                {
                get2("osc" + i + "wavscanwave" + (j + 1), data, p + 12 + j * 2);
                }
            }
        get2("osc3type", data, 136);    // the wave
        get1("osc3semi", data, 138);
        get1("osc3cent", data, 140);
        get1("osc3keytrack", data, 142);
                
        // MUTANTS
        for(int i = 1; i < 5; i++)
            {
            int p = (i == 1 ? 144 : (i == 2 ? 158 : (i == 3 ? 204 : 218)));
            get1("mutant" + i + "mode", data, p + 0);
            if (model.get("mutant" + i + "mode") == 2) // osc sync
                get1("mutant" + i + "sourceoscsync", data, p + 2);
            else    // we assume FM Linear or other
                get1("mutant" + i + "sourcefmlin", data, p + 2);
            get2("mutant" + i + "ratio", data, p + 4);
            get2("mutant" + i + "depth", data, p + 6);
            get2("mutant" + i + "window", data, p + 8);
            get2("mutant" + i + "feedback", data, p + 10);
            get2("mutant" + i + "wet", data, p + 12);
            }

        // MUTANT WARPS
        for(int i = 1; i < 5; i++)
            {
            int p = (i == 1 ? 172 : (i == 2 ? 188 : (i == 3 ? 232 : 248)));
            for(int j = 0; j < 8; j++)
                {
                get2("mutant" + i + "warp" + (j + 1), data, p + j * 2);
                }
            }
                        
        // RING MOD NOISE
        get1("ringmodsource1", data, 264);
        get1("ringmodsource2", data, 266);
        get2("ringmoddepth", data, 268);
        get1("noisetype", data, 272);
                
        // MIXER
        for(int i = 0; i < 3; i++)
            {
            get2("mixerosc" + (i + 1) + "vol", data, 274 + i * 2);
            get2("mixerosc" + (i + 1) + "pan", data, 286 + i * 2);
            get2("mixerosc" + (i + 1) + "filterratio", data, 292 + i * 2);
            }               
        get2("mixerringmodvol", data, 280);
        get2("mixernoisevol", data, 282);
        get2("mixerringmodpan", data, 298);
        get2("mixernoisepan", data, 300);
        get2("mixerringmodfilterratio", data, 304);
        get2("mixernoisefilterratio", data, 306);

        // FILTERS
        get1("mixerfilterrouting", data, 302);
        get1("filter1type", data, 308);                                         // FIXME: Values are out of order, will this affect us?
        get2("filter1cutoff", data, 310);
        get2("filter1resonance", data, 312);
        get2("filter1special", data, 314);
        get2("filter1env1amount", data, 316);
        get2("filter1lfo1amount", data, 318);
        get2("filter1velenv", data, 320);
        get2("filter1keytrack", data, 322);
        get2("filter1drive", data, 326);
        get1("filter1positionofdrive", data, 328);
        get1("filter1vowelorder", data, 330);

        get2("filter2morph", data, 332);
        get2("filter2cutoff", data, 334);
        get2("filter2resonance", data, 336);
        get2("filter2env1amount", data, 338);
        get2("filter2lfo1amount", data, 340);
        get2("filter2velenv", data, 342);
        get2("filter2keytrack", data, 344);
                
        // AMPLIFIER
        get2("amplfo2amount", data, 346);
        get2("ampvelenv", data, 348);
        get2("amplevel", data, 350);

        // PRE-FX
        get1("prefxtype", data, 352);
        get1("prefxsidechain", data, 354);
        int prefxtype = Math.max(0, Math.min(data[352], 9));    // bound to 0 ... 9
        if (prefxtype != 0) // skip bypass
            {
            for(int i = 0; i < 5; i++)
                {
                int p = 356;
                get2("prefx" + prefxtype + "param" + (i + 1), data, p + i * 2);
                }
            }
        get2("prefxwet", data, 366);

        // DELAY
        get2("delaytype", data, 368);
        get2("delaybpmsync", data, 370);
        if (model.get("delaybpmsync") == 0)
            {
            get2("delaytimesyncoff", data, 372);
            }
        else
            {
            get2("delaytimesyncon", data, 372);
            }
        get2("delayfeedback", data, 374);
        get2("delayfeedtone", data, 376);
        get2("delaywettone", data, 378);
        get2("delaywet", data, 382);
                
        // REVERB
        get2("reverbtype", data, 384);
        get2("reverbtime", data, 388);
        get2("reverbtone", data, 390);
        get2("reverbhidamp", data, 392);
        get2("reverblodamp", data, 394);
        get2("reverbpredelay", data, 396);
        get2("reverbwet", data, 398);

        // POST-FX
        get1("postfxtype", data, 400);
        get1("postfxsidechain", data, 402);
        int postfxtype = Math.max(0, Math.min(data[400], 9));   // bound to 0 ... 9
        if (postfxtype != 0) // skip bypass
            {
            for(int i = 0; i < 5; i++)
                {
                int p = 404;
                get2("postfx" + postfxtype + "param" + (i + 1), data, p + i * 2);
                }
            }
        get2("postfxwet", data, 414);

        // RIBBON
        get1("ribbonmode", data, 436);
        get1("ribbonkeyspan", data, 438);
        get1("ribbonoctave", data, 440);
        get1("ribbonhold", data, 442);                                  // Not in the standard list
        get1("ribbonquantize", data, 444);
        get1("ribbonglide", data, 446);
        get1("ribbonmodcontrol", data, 448);                            // theremin wheel volume
        get1("ribbonscalekeylock", data, 450);
        get1("ribbonscaletype", data, 452);
        for(int i = 2; i < 9; i++)
            {
            get1("ribbonscalenote" + i, data, 450 + i * 2);
            }               


        // MISC
        get1("arpclklock", data, 468);  
        data[470] = (byte)(model.get("voicewarmmode") | (model.get("voicesnap") << 1));
        // get1("voicewarmmode", data, 470, 0);                         // Note Bitpacked
        // get1("voicesnap", data, 470, 1);                                     // Note Bitpacked
        get1("filter2type", data, 472);
                
        // ENVELOPES
        for(int i = 0; i < 5; i++)
            {
            int p = (i == 0 ? 478 : (i == 1 ? 506 : (i == 2 ? 534 : (i == 3 ? 562 : 590))));
            get1("env" + (i + 1) + "bpmsync", data, p + 8);
            if (model.get("env" + (i + 1) + "bpmsync") == 0) // off
                {
                get2("env" + (i + 1) + "delaysyncoff", data, p + 10);
                get2("env" + (i + 1) + "attacksyncoff", data, p + 0);
                get2("env" + (i + 1) + "holdsyncoff", data, p + 12);
                get2("env" + (i + 1) + "decaysyncoff", data, p + 2);
                get2("env" + (i + 1) + "releasesyncoff", data, p + 6);
                }
            else
                {
                get2("env" + (i + 1) + "delaysyncon", data, p + 10);
                get2("env" + (i + 1) + "attacksyncon", data, p + 0);
                get2("env" + (i + 1) + "holdsyncon", data, p + 12);
                get2("env" + (i + 1) + "decaysyncon", data, p + 2);
                get2("env" + (i + 1) + "releasesyncon", data, p + 6);
                }
            get2("env" + (i + 1) + "sustain", data, p + 4);
            get2("env" + (i + 1) + "atkcurve", data, p + 14);
            get2("env" + (i + 1) + "deccurve", data, p + 16);
            get2("env" + (i + 1) + "relcurve", data, p + 18);
            get1("env" + (i + 1) + "legato", data, p + 20);
            get1("env" + (i + 1) + "reset", data, p + 22);          // can only be set if legato is unset, hope this is okay
            get1("env" + (i + 1) + "freerun", data, p + 24);                // can only be set if legato is unset, hope this is okay
            get1("env" + (i + 1) + "loop", data, p + 26);
            }
                        
        // LFOS
        for(int i = 0; i < 5; i++)
            {
            int p = (i == 0 ? 618 : (i == 1 ? 656 : (i == 2 ? 694 : (i == 3 ? 732 : 770))));
            get1("lfo" + (i + 1) + "wave", data, p + 0);
            get1("lfo" + (i + 1) + "bpmsync", data, p + 4);
            get1("lfo" + (i + 1) + "trigsync", data, p + 6);
            if (model.get("lfo" + (i + 1) + "bpmsync") == 0) // off
                {
                get2("lfo" + (i + 1) + "ratesyncoff", data, p + 2);
                get1("lfo" + (i + 1) + "delaysyncoff", data, p + 8);
                get1("lfo" + (i + 1) + "fadeinsyncoff", data, p + 10);
                }
            else
                {
                get2("lfo" + (i + 1) + "ratesyncon", data, p + 2);
                get1("lfo" + (i + 1) + "delaysyncon", data, p + 8);
                get1("lfo" + (i + 1) + "fadeinsyncon", data, p + 10);
                }
            get2("lfo" + (i + 1) + "phase", data, p + 12);
            get2("lfo" + (i + 1) + "level", data, p + 14);
            get1("lfo" + (i + 1) + "steps", data, p + 16);
            get1("lfo" + (i + 1) + "smooth", data, p + 18);
            get1("lfo" + (i + 1) + "oneshot", data, p + 20);
            // First 8 steps
            for(int j = 0; j < 8; j++)
                {
                get2("lfo" + (i + 1) + "step" + (j + 1), data, p + 22 + j * 2);
                }
            }
                        
        // ARPEGGIATOR
        // It appears that 28 is the one that matters, but 808 is a copy of it?
        get2("arptempo", data, 28);
        get2("arptempo", data, 808);
        get1("arpdivision", data, 810);
        get1("arpswing", data, 812);
        get1("arpgate", data, 814);
        get1("arpoctmode", data, 816);
        get1("arpoctave", data, 818);
        get1("arpmode", data, 820);
        get1("arplength", data, 822);
        get1("arptaptrig", data, 824);
        get1("arpphrase", data, 826);
        get1("arpratchet", data, 828);
        get1("arpchance", data, 830);
        get1("arpenable", data, 832);           
        get1("arplatch", data, 834);            

        // MOD MATRIX
        for(int i = 0; i < 32; i++)
            {
            get2("modmatrix" + (i + 1) + "depth", data, 838 + i * 2);
            }
        for(int i = 0; i < 32; i++)
            {
            get1("modmatrix" + (i + 1) + "modsource", data, 902 + i * 2);
            }
        for(int i = 0; i < 32; i++)
            {
            get2("modmatrix" + (i + 1) + "modtarget", data, 966 + i * 2);
            }
        // Set the target categories
        for(int i = 0; i < 32; i++)
            {
            int category = MOD_DESTINATION_CATEGORIES[model.get("modmatrix" + (i + 1) + "modtarget")];
            data[1030 + i * 2] = (byte)category;
            }
                        
        // MACRO
        for(int i = 0; i < 8; i++)
            {
            int p = 1094 + i * 16;
            for(int j = 0; j < 8; j++)
                {
                get2("macro" + (i + 1) + "depth" + (j + 1), data, p + j * 2);
                }
            }
        for(int i = 0; i < 8; i++)
            {
            int p = 1222 + i * 16;
            for(int j = 0; j < 8; j++)
                {
                get2("macro" + (i + 1) + "target" + (j + 1), data, p + j * 2);
                }
            }
        for(int i = 0; i < 8; i++)
            {
            int p = 1350 + i * 16;
            for(int j = 0; j < 8; j++)
                {
                get2("macro" + (i + 1) + "buttonvalue" + (j + 1), data, p + j * 2);
                }
            }
        for(int i = 0; i < 8; i++)
            {
            get2("macro" + (i + 1) + "panelvalue", data, 1606 + i * 2);
            }
        for(int i = 0; i < 8; i++)
            {
            get1("macro" + (i + 1) + "panelbuttonstate", data, 1622 + i);
            }
        // Set the target categories
        for(int i = 0; i < 8; i++)
            {
            int p = 1478 + i * 16;
            for(int j = 0; j < 8; j++)
                {
                int category = MOD_DESTINATION_CATEGORIES[model.get("macro" + (i + 1) + "target" + (j + 1))];
                data[p + j * 2] = (byte)category;
                }
            }
        int offset = 1630;
        for(int i = 0; i < 8; i++)
            {
            char[] macroname = (model.get("macro" + (i + 1) + "name", "Untitled") + "        ").toCharArray();
            for(int j = 0; j < 8; j++)
                {
                data[offset + j] = (byte)macroname[j];
                }
            offset += 17;                   // NO REALLY, it's *17*.  Something strange going on here.
            }
        // For the time being, macros always emit custom names
        for(int i = 0; i < 8; i++)
            {
            data[416 + i * 2] = 0;
            data[416 + i * 2 + 1] = 1;
            }


        // LFO STEPS 9 to 64
        for(int i = 0; i < 5; i++)
            {
            for(int j = 0; j < 56; j++)
                {
                get2("lfo" + (i + 1) + "step" + (j + 9), data, 1770 + i * 56 * 2 + j * 2);
                }
            }

                
        // ENV TRIG SOURCES
        for(int i = 0; i < 5; i++)
            {
            // Env 2 Trig Source 1 ought not be set
            if (i == 1)     // Env 2
                {
                data[2340 + i * 10] = 1;
                data[2340 + i * 10 + 1] = 0;
                }
            else
                {
                get2("env" + (i + 1) + "trigsrc1", data, 2340 + i * 10);
                }
            get2("env" + (i + 1) + "trigsrc2", data, 2342 + i * 10);
            get2("env" + (i + 1) + "trigsrc3", data, 2344 + i * 10);
            get2("env" + (i + 1) + "trigsrc4", data, 2346 + i * 10);
            }
                
                
        //// 2.0.0. FEATURES
        get1("voicesustain", data, 2458);
        get1("arpstepoffset", data, 2460);
        get1("osc1bitreduction", data, 2452);
        get1("osc2bitreduction", data, 2454);
        get1("osc3bitreduction", data, 2456);
        for(int i = 0; i < 5; i++)
            {
            get1("env" + (i + 1) + "quantize", data, 2432 + i * 2);
            }
        for(int i = 0; i < 5; i++)
            {
            get1("lfo" + (i + 1) + "quantize", data, 2442 + i * 2);
            }
        for(int i = 0; i < 8; i++)
            {
            get1("voicemodulation" + (i + 1), data, 2400 + i * 2);
            }
            
            
        //// VARIOUS MYSTERY BYTES
        
        // Byte 6 appears to be the patch number [again].  It's not clear why
        data[6] = data[3];
        
        // Byte 7 is unclear, sometimes 2, sometimes 3, etc.
        data[7] = 3;
        
        // This appears to always be 5.  It's in the Arp region
        data[836] = 5;
                
        // These are the (unknown) values for Sawspressive and other patches.
        // It appears that if they are not set, then later data (such as 
        // LFO steps 9-64, and arp triggers, and the Arp Step Offset) won't be
        // set in the Hydrasynth on write.  So they're pretty important.  But
        // what they do I have no idea.  Some patches have them as 88 88 88 88
        // but most have 69 84 67 68
        data[1766] = 69;
        data[1767] = 84;
        data[1768] = 67;
        data[1769] = 68;

        // These are always -100/-1
        data[2390] = (byte) -100;
        data[2391] = (byte) -1;
        data[2392] = (byte) -100;
        data[2393] = (byte) -1;
        data[2394] = (byte) -100;
        data[2395] = (byte) -1;
        data[2396] = (byte) -100;
        data[2397] = (byte) -1;
        data[2398] = (byte) -100;
        data[2399] = (byte) -1;
        
        byte[][] outgoing = Encode.encodePatch(data);
                
        Object[] sysex = new Object[outgoing.length * 2 + 2 + (toWorkingMemory ? 6 : 3)];
        sysex[0] = (isEmittingBatch() ? null : Encode.encodePayload(new byte[] { (byte)0x18, (byte)0x00 }));         // header
        sysex[1] = (isEmittingBatch() ? null : Encode.encodePayload(new byte[] { (byte)0x18, (byte)0x00 }));         // Java on Windows seems to bork unless we send the header twice.  Not Mac or Linux.
        for(int i = 0; i < outgoing.length; i++)                                                                        // patch chunks
            {
            sysex[i * 2 + 2] = outgoing[i];
            sysex[i * 2 + 3] = PAUSE_AFTER_CHUNK;
            }
            
        if (toWorkingMemory)
            {
            // Don't do save request
            sysex[sysex.length - 6] = (isEmittingBatch() ? null : Encode.encodePayload(new byte[] { (byte)0x1A, (byte)0x00 }));          // footer
            sysex[sysex.length - 5] = (isEmittingBatch() || !sendExtraHF() ? null : Encode.encodePayload(new byte[] { (byte)0x1A, (byte)0x00 }));          // Java on Windows seems to bork unless we send the footer twice.  Not Mac or Linux.
            sysex[sysex.length - 4] = buildCC(getChannelOut(), 32, 7)[0];                                                                // Bank Change
            sysex[sysex.length - 3] = buildPC(getChannelOut(), 0)[0];                                                                    // PC Away
            sysex[sysex.length - 2] = Integer.valueOf(getPauseAfterChangePatch());
            sysex[sysex.length - 1] = buildPC(getChannelOut(), 127)[0];                                                                  // PC Back
            // 200ms afterwards
            
            sendLabel.setText("Sent -> H 128");
            }
        else
            {
            sysex[sysex.length - 1] = (isEmittingBatch() ? null : Encode.encodePayload(new byte[] { (byte)0x14, (byte)0x00 }));          // save request
            sysex[sysex.length - 2] = (isEmittingBatch() ? null : Encode.encodePayload(new byte[] { (byte)0x1A, (byte)0x00 }));          // footer
            sysex[sysex.length - 3] = (isEmittingBatch() || !sendExtraHF() ? null : Encode.encodePayload(new byte[] { (byte)0x1A, (byte)0x00 }));          // Java on Windows seems to bork unless we send the footer twice.  Not Mac or Linux.
            // 3500ms afterwards
            }
                        
        if (REVERSE_ENGINEER)
            {
            System.out.println("OUTGOING DIFFERENCES");
            diff(firstPatch, data);
            }

        return sysex;
        }

    public void performRequestDump(Model tempModel, boolean changePatch)
        {
        if (changePatch)
            {
            performChangePatch(tempModel);
            }
                        
        if (tempModel == null)
            tempModel = getModel();

        incoming = new byte[22][];
        incomingPos = 0;

        int NN = tempModel.get("number", 0);
        int BB = tempModel.get("bank", 0);

        if (!isBatchDownloading())  // we already did it
            {
            sendHeader();
            }
        tryToSendSysex(Encode.encodePayload(new byte[] { 0x04, 0x00, (byte)BB, (byte)NN }));    // dump request
        /// After negotiation and downloading chunks, parse() will emit the footer
        }

    void sendHeader()
        {
        boolean sendMIDI = getSendMIDI();
        setSendMIDI(true);
        tryToSendSysex(Encode.encodePayload(new byte[] { 0x18, 000 }));
        if (sendExtraHF()) tryToSendSysex(Encode.encodePayload(new byte[] { 0x18, 000 }));              // Java on Windows seems to bork unless we send the header twice.  Not Mac or Linux.
        setSendMIDI(sendMIDI);
        }
        
    void sendFooter()
        {
        boolean sendMIDI = getSendMIDI();
        setSendMIDI(true);
        tryToSendSysex(Encode.encodePayload(new byte[] { 0x1A, 0x00 }));
        if (sendExtraHF()) tryToSendSysex(Encode.encodePayload(new byte[] { 0x1A, 0x00 }));             // Java on Windows seems to bork unless we send the footer twice.  Not Mac or Linux.
        setSendMIDI(sendMIDI);
        }
                                
    public void startingBatchDownload(Model firstPatch, Model finalPatch) 
        { 
        sendHeader();
        }

    public void stoppingBatchDownload(Model firstPatch, Model finalPatch) 
        { 
        sendFooter();
        }

    public Object[] startingBatchEmit(int bank, int start, int end, boolean toFile) 
        { 
        if (sendExtraHF())
            {
            return new Object[]
                {
                // Header 
                Encode.encodePayload(new byte[] { 0x18, 000 }),
                Encode.encodePayload(new byte[] { 0x18, 000 })                  // Java on Windows seems to bork unless we send the header twice.  Not Mac or Linux.
                };
            }
        else
            {
            return new Object[]
                {
                // Header 
                Encode.encodePayload(new byte[] { 0x18, 000 })
                };
            }
        }

    public Object[] stoppingBatchEmit(int bank, int start, int end, boolean toFile) 
        { 
        if (sendExtraHF())
            {
            return new Object[]
                {
                // Save 
                Encode.encodePayload(new byte[] { (byte)0x14, (byte)0x00 }) ,
                // Footer 
                Encode.encodePayload(new byte[] { 0x1A, 0x00 }),
                Encode.encodePayload(new byte[] { 0x1A, 0x00 })                 // Java on Windows seems to bork unless we send the footer twice.  Not Mac or Linux.
                };
            }
        else
            {
            return new Object[]
                {
                // Save 
                Encode.encodePayload(new byte[] { (byte)0x14, (byte)0x00 }) ,
                // Footer 
                Encode.encodePayload(new byte[] { 0x1A, 0x00 })
                };
            }
        }
                

    byte[] firstPatch = null;

    int incomingPos;
    byte[][] incoming = new byte[22][];
    
    void printMessage(byte[] data)
        {
        try
            {
            System.out.println("<-- " + StringUtility.toHex(Decode.decodePayload(data)));
            }
        catch (Exception ex)
            {
            System.out.println("Exception " + ex + "\nFrom " + StringUtility.toHex(data));
            }
        } 
        
    public static final boolean REVERSE_ENGINEER = false;

    public int parse(byte[] data, boolean fromFile)
        {
        // Here we're simply breaking breaking up the data into messages and parsing each one.
        // We have to be careful to send the footer at the end (the header was sent by performRequestDump)
        
        byte[][] cut = cutUpSysex(data);
        if (cut.length == 1)
            {
//              printMessage(data);
            return parseSub(data, fromFile);
            }
        else
            {
            incomingPos = 0;
            incoming = new byte[22][];
            for(int i = 0; i < cut.length; i++)
                {
                int val = parseSub(cut[i], fromFile);
                if (val == PARSE_SUCCEEDED)
                    return PARSE_SUCCEEDED;
                else if (val != PARSE_INCOMPLETE && val != PARSE_IGNORE)                // Footer already emitted
                    return PARSE_FAILED;
                }
            
            if (!fromFile && !isBatchDownloading())
                {
                sendFooter();
                }
            return PARSE_FAILED;
            }
        }
    
    boolean sendExtraHF() { return Style.isWindows(); }
    public static final int MAX_WINDOWS_TRIES = 5;
    int tryAgainCount = 0;
    
    public int parseSub(byte[] data, boolean fromFile)
        {
        // Here we parse a single message and emit the right stuff to keep 
        // additional messages coming

        // First, do we have a message we're interested in?
        if (data.length == 191 || data.length == 155 || data.length == 187)   // hopefully a patch chunk
            {
            // Send request for next chunk if appropriate
            if (!fromFile)
                {
                boolean sendMIDI = getSendMIDI();
                setSendMIDI(true);
                //  time("Sending Acknowledgment for " + incomingPos);
                tryToSendSysex(Encode.encodePayload(new byte[] { 0x17, 0x00, (byte)incomingPos, 0x16 }));
                setSendMIDI(sendMIDI);
                }

            // double check -- do we have too many chunks?
            if ((data.length == 191 || data.length == 187) && incomingPos == 21) // uh oh
                {
                if (!fromFile && !isBatchDownloading())
                    {
                    sendFooter();
                    }
                tryAgainCount = 0;
                return PARSE_FAILED;
                }
            // double check -- do we have too few chunks?                
            else if (data.length == 155 && incomingPos != 21) // uh oh
                {
                if (!fromFile && !isBatchDownloading())
                    {
                    sendFooter();
                    }
                tryAgainCount = 0;
                return PARSE_FAILED;
                }

            // Is it the right chunk?  
            byte[] decoded = Decode.decodePayload(data);
            if (decoded[0] != 0x16 || decoded[1] != 0 || decoded[3] != 0x16) // something's wrong
                {
                if (!fromFile && !isBatchDownloading())
                    {
                    sendFooter();
                    }
                tryAgainCount = 0;
                return PARSE_FAILED;
                }
                                
            // Windows often gets spurious initial chunks after a write, we need to check for them
            if (decoded[2] != incomingPos)
                {
                // System.err.println("Invalid Position " + decoded[2] + " != " + incomingPos + ", trying again " + tryAgainCount);
                if (tryAgainCount < MAX_WINDOWS_TRIES)
                    {
                    // Let's try again
                    tryAgainCount++;
                    return PARSE_INCOMPLETE;
                    }
                else
                    {
                    tryAgainCount = 0;
                    return PARSE_FAILED;
                    }
                }
            else
                {
                tryAgainCount = 0;
                }
                        
            // Load chunk
            incoming[incomingPos] = data;

            // Process if we have 21 chunks so far
            if (incomingPos == 21)
                {
                // Send footer if appropriate
                if (!fromFile && !isBatchDownloading())
                    {
                    sendFooter();
                    }
                                        
                // Decode and process!
                try
                    {
                    byte[] result = Decode.decodePatch(incoming);
                    
                    // Maybe we're also doing some reverse engineering printouts?
                    if (REVERSE_ENGINEER)
                        {
                        if (firstPatch == null)
                            {
                            System.out.println("INITIAL PATCH LOADED");
                            firstPatch = result;
                            }
                        else 
                            {
                            System.out.println("DIFFERENCES");
                            diff(firstPatch, result);
                            }
                        }

                    incoming = new byte[22][];
                    incomingPos = 0;
                    return parseReal(result, fromFile);
                    }
                catch (RuntimeException ex)
                    {
                    Synth.handleException(ex);
                    incoming = new byte[22][];
                    incomingPos = 0;

                    if (!fromFile && !isBatchDownloading())
                        {
                        // for good measure
                        sendFooter();
                        }
                    return PARSE_FAILED;
                    }
                }
            else
                {
                // Let's wait for the next chunk
                incomingPos++;
                return PARSE_INCOMPLETE;
                }
            }
        else 
            {
            return PARSE_IGNORE;           // maybe some other message got in the way
            }
        }
        
    public void diff(byte[] a, byte[] b)
        {
        for(int i = 0; i < a.length; i++)
            {
            if (a[i] != b[i])
                System.out.println("" + i + " " + StringUtility.toHex(a[i]) + " " + StringUtility.toHex(b[i]) + " (" + a[i] + " " + b[i] + ")");
            }
        }

    public void dump(byte[] a)
        {
        for(int i = 0; i < a.length; i++)
            {
            System.out.println("" + i + " " + StringUtility.toHex(a[i]) +  " (" + a[i] + ") " + (a[i] >= 32 && a[i] < 127 ? (char)a[i] : "")) ;
            }
        }

    void set1(String key, byte[] data, int pos, int bit)
        {
        if (!model.exists(key)) System.out.println("KEY NOT FOUND: " + key);
        model.set(key, (data[pos] >>> bit) & 0x01);
        }

    void set1(String key, byte[] data, int pos)
        {
        if (!model.exists(key)) System.out.println("KEY NOT FOUND: " + key);

        if (key.equals("bank") || key.equals("number"))
            {
            model.set(key, data[pos] & 0xFF);
            }
        else if (model.getMin(key) < 0)      // signed two's complement
            {
            model.set(key, data[pos]);
            }
        else
            {
            model.set(key, data[pos] & 0xFF);
            }
        }

    void set2(String key, byte[] data, int pos)
        {
        if (!model.exists(key)) System.out.println("KEY NOT FOUND: " + key);

        if (model.getMin(key) < 0)      // signed two's complement
            {
            model.set(key, (short)((data[pos] & 0xFF) | ((data[pos + 1] & 0xFF) << 8)));
            }
        else
            {
            model.set(key, ((data[pos] & 0xFF) | ((data[pos + 1] & 0xFF) << 8)));
            }
        }

    public int parseReal(byte[] data, boolean fromFile)
        {
        // VERIFY VERSION
        int version = data[4] & 0xFF;
        if (version != VERSION_1_5_5 && version != VERSION_2_0_0)
            return PARSE_FAILED;
                
        // PATCH BANK AND NAME
        set1("bank", data, 2);
        set1("number", data, 3);
        
        // BASICS
        set1("category", data, 8);
        char[] name = new char[16];
        for(int i = 0; i < 16; i++)
            {
            name[i] = (char)(data[9 + i]);
            }
        model.set("name", String.valueOf(name));
        set1("color", data, 26);
                
        // VOICE
        set1("voicepolyphony", data, 30);
        set1("voicedensity", data, 32);
        set1("voicedetune", data, 34);
        set1("voiceanalogfeel", data, 36);
        set1("voicerandomphase", data, 38);
        set1("voicestereomode", data, 40);
        set1("voicestereowidth", data, 42);
        set1("voicepitchbend", data, 44);
        set1("voicevibratobpmsync", data, 50);
        if (model.get("voicevibratobpmsync") == 0)
            {
            set1("voicevibratoratesyncoff", data, 48);
            }
        else
            {
            set1("voicevibratoratesyncon", data, 48);
            }
        set1("voiceglide", data, 52);
        set1("voiceglidetime", data, 54);
        set1("voiceglidecurve", data, 56);
        set1("voiceglidelegato", data, 58);

        /// CUSTOM VIBRATO AMOUNT
        if (version == VERSION_1_5_5)
            model.set("voicevibratoamount", data[46]);
        else if (version == VERSION_2_0_0)
            model.set("voicevibratoamount", data[46] * 10 + data[2462]);
        
        // SCALES
                
        set1("scalekeylock", data, 60);
        set1("scaletype", data, 62);
        
        /*
          for(int i = 60; i < 80; i++)
          {
          System.out.println("" + i + " " + data[i]);
          }
        */
                
        for(int i = 2; i < 9; i++)
            {
            set1("scalenote" + i, data, 62 + i * 2);
            }               

        // OSCS
        for(int i = 1; i < 3; i++)
            {
            int p = (i == 1 ? 80 : 108);
            set1("osc" + i + "mode", data, p + 0);
            set2("osc" + i + "type", data, p + 2);  // the wave
            set1("osc" + i + "semi", data, p + 4);
            set1("osc" + i + "cent", data, p + 6);
            set1("osc" + i + "keytrack", data, p + 8);
            set2("osc" + i + "wavscan", data, p + 10);
            for(int j = 0; j < 8; j++)
                {
                set2("osc" + i + "wavscanwave" + (j + 1), data, p + 12 + j * 2);
                }
            }
        set2("osc3type", data, 136);    // the wave
        set1("osc3semi", data, 138);
        set1("osc3cent", data, 140);
        set1("osc3keytrack", data, 142);
                
        // MUTANTS
        for(int i = 1; i < 5; i++)
            {
            int p = (i == 1 ? 144 : (i == 2 ? 158 : (i == 3 ? 204 : 218)));
            set1("mutant" + i + "mode", data, p + 0);
            if (model.get("mutant" + i + "mode") == 2) // osc sync
                set1("mutant" + i + "sourceoscsync", data, p + 2);
            else    // we assume FM Linear or other
                set1("mutant" + i + "sourcefmlin", data, p + 2);
            set2("mutant" + i + "ratio", data, p + 4);
            set2("mutant" + i + "depth", data, p + 6);
            set2("mutant" + i + "window", data, p + 8);
            set2("mutant" + i + "feedback", data, p + 10);
            set2("mutant" + i + "wet", data, p + 12);
            }

        // MUTANT WARPS
        for(int i = 1; i < 5; i++)
            {
            int p = (i == 1 ? 172 : (i == 2 ? 188 : (i == 3 ? 232 : 248)));
            for(int j = 0; j < 8; j++)
                {
                set2("mutant" + i + "warp" + (j + 1), data, p + j * 2);
                }
            }
                        
        // RING MOD NOISE
        set1("ringmodsource1", data, 264);
        set1("ringmodsource2", data, 266);
        set2("ringmoddepth", data, 268);
        set1("noisetype", data, 272);
                
        // MIXER
        for(int i = 0; i < 3; i++)
            {
            set2("mixerosc" + (i + 1) + "vol", data, 274 + i * 2);
            set2("mixerosc" + (i + 1) + "pan", data, 286 + i * 2);
            set2("mixerosc" + (i + 1) + "filterratio", data, 292 + i * 2);
            }               
        set2("mixerringmodvol", data, 280);
        set2("mixernoisevol", data, 282);
        set2("mixerringmodpan", data, 298);
        set2("mixernoisepan", data, 300);
        set2("mixerringmodfilterratio", data, 304);
        set2("mixernoisefilterratio", data, 306);

        // FILTERS
        set1("mixerfilterrouting", data, 302);
        set1("filter1type", data, 308);                                         // FIXME: Values are out of order, will this affect us?
        set2("filter1cutoff", data, 310);
        set2("filter1resonance", data, 312);
        set2("filter1special", data, 314);
        set2("filter1env1amount", data, 316);
        set2("filter1lfo1amount", data, 318);
        set2("filter1velenv", data, 320);
        set2("filter1keytrack", data, 322);
        set2("filter1drive", data, 326);
        set1("filter1positionofdrive", data, 328);
        set1("filter1vowelorder", data, 330);

        set2("filter2morph", data, 332);
        set2("filter2cutoff", data, 334);
        set2("filter2resonance", data, 336);
        set2("filter2env1amount", data, 338);
        set2("filter2lfo1amount", data, 340);
        set2("filter2velenv", data, 342);
        set2("filter2keytrack", data, 344);
                
        // AMPLIFIER
        set2("amplfo2amount", data, 346);
        set2("ampvelenv", data, 348);
        set2("amplevel", data, 350);

        // PRE-FX
        set1("prefxtype", data, 352);
        set1("prefxsidechain", data, 354);
        int prefxtype = Math.max(0, Math.min(data[352], 9));    // bound to 0 ... 9
        if (prefxtype != 0) // skip bypass
            {
            for(int i = 0; i < 5; i++)
                {
                int p = 356;
                set2("prefx" + prefxtype + "param" + (i + 1), data, p + i * 2);
                }
            }
        set2("prefxwet", data, 366);

        // DELAY
        set2("delaytype", data, 368);
        set2("delaybpmsync", data, 370);
        if (model.get("delaybpmsync") == 0)
            {
            set2("delaytimesyncoff", data, 372);
            }
        else
            {
            set2("delaytimesyncon", data, 372);
            }
        set2("delayfeedback", data, 374);
        set2("delayfeedtone", data, 376);
        set2("delaywettone", data, 378);
        set2("delaywet", data, 382);
                
        // REVERB
        set2("reverbtype", data, 384);
        set2("reverbtime", data, 388);
        set2("reverbtone", data, 390);
        set2("reverbhidamp", data, 392);
        set2("reverblodamp", data, 394);
        set2("reverbpredelay", data, 396);
        set2("reverbwet", data, 398);

        // POST-FX
        set1("postfxtype", data, 400);
        set1("postfxsidechain", data, 402);
        int postfxtype = Math.max(0, Math.min(data[400], 9));   // bound to 0 ... 9
        if (postfxtype != 0) // skip bypass
            {
            for(int i = 0; i < 5; i++)
                {
                int p = 404;
                set2("postfx" + postfxtype + "param" + (i + 1), data, p + i * 2);
                }
            }
        set2("postfxwet", data, 414);

        // RIBBON
        set1("ribbonmode", data, 436);
        set1("ribbonkeyspan", data, 438);
        set1("ribbonoctave", data, 440);
        set1("ribbonhold", data, 442);                                  // Not in the standard list
        set1("ribbonquantize", data, 444);
        set1("ribbonglide", data, 446);
        set1("ribbonmodcontrol", data, 448);                            // theremin wheel volume
        set1("ribbonscalekeylock", data, 450);
        set1("ribbonscaletype", data, 452);
        for(int i = 2; i < 9; i++)
            {
            set1("ribbonscalenote" + i, data, 450 + i * 2);
            }               

        // MISC
        set1("arpclklock", data, 468);          
        set1("voicewarmmode", data, 470, 0);                            // Note Bitpacked
        set1("voicesnap", data, 470, 1);                                        // Note Bitpacked
        set1("filter2type", data, 472);
                
        // ENVELOPES
        for(int i = 0; i < 5; i++)
            {
            int p = (i == 0 ? 478 : (i == 1 ? 506 : (i == 2 ? 534 : (i == 3 ? 562 : 590))));
            set1("env" + (i + 1) + "bpmsync", data, p + 8);
            if (model.get("env" + (i + 1) + "bpmsync") == 0) // off
                {
                set2("env" + (i + 1) + "delaysyncoff", data, p + 10);
                set2("env" + (i + 1) + "attacksyncoff", data, p + 0);
                set2("env" + (i + 1) + "holdsyncoff", data, p + 12);
                set2("env" + (i + 1) + "decaysyncoff", data, p + 2);
                set2("env" + (i + 1) + "releasesyncoff", data, p + 6);
                }
            else
                {
                set2("env" + (i + 1) + "delaysyncon", data, p + 10);
                set2("env" + (i + 1) + "attacksyncon", data, p + 0);
                set2("env" + (i + 1) + "holdsyncon", data, p + 12);
                set2("env" + (i + 1) + "decaysyncon", data, p + 2);
                set2("env" + (i + 1) + "releasesyncon", data, p + 6);
                }
            set2("env" + (i + 1) + "sustain", data, p + 4);
            set2("env" + (i + 1) + "atkcurve", data, p + 14);
            set2("env" + (i + 1) + "deccurve", data, p + 16);
            set2("env" + (i + 1) + "relcurve", data, p + 18);
            set1("env" + (i + 1) + "legato", data, p + 20);
            set1("env" + (i + 1) + "reset", data, p + 22);          // can only be set if legato is unset, hope this is okay
            set1("env" + (i + 1) + "freerun", data, p + 24);                // can only be set if legato is unset, hope this is okay
            set1("env" + (i + 1) + "loop", data, p + 26);
            }
                        
        // LFOS
        for(int i = 0; i < 5; i++)
            {
            int p = (i == 0 ? 618 : (i == 1 ? 656 : (i == 2 ? 694 : (i == 3 ? 732 : 770))));
            set1("lfo" + (i + 1) + "wave", data, p + 0);
            set1("lfo" + (i + 1) + "bpmsync", data, p + 4);
            set1("lfo" + (i + 1) + "trigsync", data, p + 6);
            if (model.get("lfo" + (i + 1) + "bpmsync") == 0) // off
                {
                set2("lfo" + (i + 1) + "ratesyncoff", data, p + 2);
                set1("lfo" + (i + 1) + "delaysyncoff", data, p + 8);
                set1("lfo" + (i + 1) + "fadeinsyncoff", data, p + 10);
                }
            else
                {
                set2("lfo" + (i + 1) + "ratesyncon", data, p + 2);
                set1("lfo" + (i + 1) + "delaysyncon", data, p + 8);
                set1("lfo" + (i + 1) + "fadeinsyncon", data, p + 10);
                }
            set2("lfo" + (i + 1) + "phase", data, p + 12);
            set2("lfo" + (i + 1) + "level", data, p + 14);
            set1("lfo" + (i + 1) + "steps", data, p + 16);
            set1("lfo" + (i + 1) + "smooth", data, p + 18);
            set1("lfo" + (i + 1) + "oneshot", data, p + 20);
            // First 8 steps
            for(int j = 0; j < 8; j++)
                {
                set2("lfo" + (i + 1) + "step" + (j + 1), data, p + 22 + j * 2);
                }
            }
                        
        // ARPEGGIATOR
                
        set2("arptempo", data, 28);
        // set2("arptempo", data, 808);
        set1("arpdivision", data, 810);
        set1("arpswing", data, 812);
        set1("arpgate", data, 814);
        set1("arpoctmode", data, 816);
        set1("arpoctave", data, 818);
        set1("arpmode", data, 820);
        set1("arplength", data, 822);
        set1("arptaptrig", data, 824);
        set1("arpphrase", data, 826);
        set1("arpratchet", data, 828);
        set1("arpchance", data, 830);
        set1("arpenable", data, 832);           
        set1("arplatch", data, 834);            

        // MOD MATRIX
        for(int i = 0; i < 32; i++)
            {
            set2("modmatrix" + (i + 1) + "depth", data, 838 + i * 2);
            }
        /// FIXME: VERSION 1.5.5 HAS DIFFERENT MOD SOURCES UNFORTUNATELY
        for(int i = 0; i < 32; i++)
            {
            set1("modmatrix" + (i + 1) + "modsource", data, 902 + i * 2);
            }
        for(int i = 0; i < 32; i++)
            {
            set2("modmatrix" + (i + 1) + "modtarget", data, 966 + i * 2);
            }
                        
        // MACRO
        for(int i = 0; i < 8; i++)
            {
            int p = 1094 + i * 16;
            for(int j = 0; j < 8; j++)
                {
                set2("macro" + (i + 1) + "depth" + (j + 1), data, p + j * 2);
                }
            }
        for(int i = 0; i < 8; i++)
            {
            int p = 1222 + i * 16;
            for(int j = 0; j < 8; j++)
                {
                set2("macro" + (i + 1) + "target" + (j + 1), data, p + j * 2);
                }
            }
        for(int i = 0; i < 8; i++)
            {
            int p = 1350 + i * 16;
            for(int j = 0; j < 8; j++)
                {
                set2("macro" + (i + 1) + "buttonvalue" + (j + 1), data, p + j * 2);
                }
            }
        for(int i = 0; i < 8; i++)
            {
            set2("macro" + (i + 1) + "panelvalue", data, 1606 + i * 2);
            }
        for(int i = 0; i < 8; i++)
            {
            set1("macro" + (i + 1) + "panelbuttonstate", data, 1622 + i);
            }
        int offset = 1630;
        for(int i = 0; i < 8; i++)
            {
            char[] macroname = new char[8];
            for(int j = 0; j < 8; j++)
                {
                macroname[j] = (char)(data[offset + j]);
                }
            model.set("macro" + (i + 1) + "name", String.valueOf(macroname));
            offset += 17;                   // NO REALLY, it's *17*.  Something strange going on here.
            }


        // LFO STEPS 9 to 64
        for(int i = 0; i < 5; i++)
            {
            for(int j = 0; j < 56; j++)
                {
                set2("lfo" + (i + 1) + "step" + (j + 9), data, 1770 + i * 56 * 2 + j * 2);
                }
            }

                
        // ENV TRIG SOURCES
        for(int i = 0; i < 5; i++)
            {
            // Env 2 Trig Source 1 ought not be set
            if (i == 1)     // Env 2
                {
                model.set("env" + (i + 1) + "trigsrc1", 1);
                }
            else
                {
                set2("env" + (i + 1) + "trigsrc1", data, 2340 + i * 10);
                }
            set2("env" + (i + 1) + "trigsrc2", data, 2342 + i * 10);
            set2("env" + (i + 1) + "trigsrc3", data, 2344 + i * 10);
            set2("env" + (i + 1) + "trigsrc4", data, 2346 + i * 10);
            }
                
        //// 2.0.0. FEATURES
        set1("voicesustain", data, 2458);
        set1("arpstepoffset", data, 2460);
        set1("osc1bitreduction", data, 2452);
        set1("osc2bitreduction", data, 2454);
        set1("osc3bitreduction", data, 2456);
        for(int i = 0; i < 5; i++)
            {
            set1("env" + (i + 1) + "quantize", data, 2432 + i * 2);
            }
        for(int i = 0; i < 5; i++)
            {
            set1("lfo" + (i + 1) + "quantize", data, 2442 + i * 2);
            }
        for(int i = 0; i < 8; i++)
            {
            set1("voicemodulation" + (i + 1), data, 2400 + i * 2);
            }
        
        sendLabel.setText("        ");
        
        revise();
        return PARSE_SUCCEEDED;
        }

    // The Hydrasynth always sends both the MSB and LSB
    public boolean getRequiresNRPNMSB() { return true; }
    public boolean getRequiresNRPNLSB() { return true; }

    public void handleSynthCCOrNRPN(Midi.CCData data)
        {
        // The hydrasynth unhelpfully tries to change parameters on us when we change
        // parameters in bulk.  So we have to ignore it.
        if (ignoreParametersFromSynth) return;
        
        if (data.type == Midi.CCDATA_TYPE_NRPN)
            {
            parseNRPN(data.number, data.value);
            }
        else if (data.type == Midi.CCDATA_TYPE_RAW_CC)
            {
            parseCC(data.number, data.value);
            }
        }

    void parseCC(int number, int val)
        {
        // For the time being we're not going to implement this
        }

    void parseNRPN(int number, int val)
        {
        Object obj = nrpnToIndex.get(Integer.valueOf(number));
        if (obj == null) return;
        String key = parameters[((Integer)obj).intValue()];
        if (key.equals("bank") || key.equals("number") || key.equals("name") || key.equals("--"))
            return;
        parseNRPN(key, val);        
        }
                        
    void parseNRPN(String key, int val)
        {
        int v = ((val >>> 7) & 127);
        int w = (val & 127);
        
        if (key.startsWith("osc"))
            {
            if (key.equals("osc1mode"))
                {
                if (v == 1) key = "osc2mode";
                else if (v == 2) key = "osc3mode";              // NOTE: does not exist, but we're keeping it here for consistency with the buggy documentation
                val = w;
                }
            else if (key.equals("osc1semi"))
                {
                if (v == 1) key = "osc2semi";
                else if (v == 2) key = "osc3semi";
                val = w;
                if (val >= 64) val -= 128; // two's complement :-(
                }
            else if (key.equals("osc1wavscan") || 
                key.equals("osc2wavscan"))
                {
                val = val / 8;
                }
            else if (key.equals("osc1cent") || 
                key.equals("osc2cent") || 
                key.equals("osc3cent"))
                {
                if (val >= 4096) val -= 8192;
                }
            /// FIXME: It is likely that osc1solowavscan1 etc. don't do anything at all.
            else if (key.equals("osc1solowavscan1"))
                {
                key = "osc1solowavscan" + (v + 1);
                val = w;
                }
            else if (key.equals("osc2solowavscan1"))
                {
                key = "osc2solowavscan" + (v + 1);
                val = w;
                }
            else if (key.equals("osc1bitreduction"))
                {
                if (v == 1)
                    key = "osc2bitreduction";
                else if (v == 1)
                    key = "osc3bitreduction";
                val = w;
                }
            }
        else if (key.startsWith("mutant"))
            {
            if (key.equals("mutant1mode"))
                {
                if (v == 1) key = "mutant2mode";
                else if (v == 2) key = "mutant3mode";
                else if (v == 3) key = "mutant4mode";
                val = w;
                }
            else if (key.equals("mutant1sourcefmlin"))
                {
                if (v == 1) key = "mutant2sourcefmlin";
                else if (v == 2) key = "mutant3sourcefmlin";
                else if (v == 3) key = "mutant4sourcefmlin";
                val = w;
                }
            else if (key.equals("mutant1sourceoscsync"))
                {
                if (v == 1) key = "mutant2sourceoscsync";
                else if (v == 2) key = "mutant3sourceoscsync";
                else if (v == 3) key = "mutant4sourceoscsync";
                val = w;
                }
            else
                {
                String subkey = key.substring(7);
                if (subkey.equals("ratio") || 
                    subkey.equals("depth") || 
                    subkey.equals("feedback") || 
                    subkey.equals("wet") || 
                    subkey.equals("window") || 
                    subkey.startsWith("warp"))
                    {
                    val = val / 8;
                    }
                }
            }
        else if (key.startsWith("ring"))
            {
            if (key.equals("ringmoddepth"))
                {
                val = val / 8;
                }
            if (key.equals("ringmodsource1"))
                {
                if (v == 1) key = "ringmodsource2";
                val = w;
                }
            }
        else if (key.startsWith("mix"))
            {
            if (!key.equals("mixerfilterrouting"))
                {
                val = val / 8;
                }
            }
        else if (key.startsWith("filter"))
            {
            String subkey = key.substring(7);
            if (subkey.equals("cutoff") ||
                subkey.equals("drive") ||
                subkey.equals("resonance") ||
                subkey.equals("special") || 
                subkey.equals("morph") ||
                subkey.equals("lfo1amount") ||
                subkey.equals("velenv") ||
                subkey.equals("env1amount") || 
                subkey.equals("keytrack") ||
                subkey.equals("morph"))
                {
                val = val / 8;
                }
            }
        else if (key.startsWith("amp"))
            {
            val = val / 8;
            }
        else if (key.startsWith("pre"))
            {
            int type = model.get("prefxtype");
            if (type > 0)
                { 
                if (key.equals("prefxparam1"))
                    {
                    key = "prefx" + type + "param1";
                    }
                else if (key.equals("prefxparam2"))
                    {
                    key = "prefx" + type + "param2";
                    }
                else if (key.equals("prefxparam3"))
                    {
                    key = "prefx" + type + "param3";
                    }
                else if (key.equals("prefxparam4"))
                    {
                    key = "prefx" + type + "param4";
                    }
                else if (key.equals("prefxparam5"))
                    {
                    key = "prefx" + type + "param5";
                    }
                }
            val = val / 8;
            }
        else if (key.startsWith("delay"))
            {
            val = val / 8;
            }
        else if (key.startsWith("reverb"))
            {
            val = val / 8;
            }
        else if (key.startsWith("post"))
            {
            int type = model.get("postfxtype");
            if (type > 0)
                { 
                if (key.equals("postfxparam1"))
                    {
                    key = "postfx" + type + "param1";
                    }
                else if (key.equals("postfxparam2"))
                    {
                    key = "postfx" + type + "param2";
                    }
                else if (key.equals("postfxparam3"))
                    {
                    key = "postfx" + type + "param3";
                    }
                else if (key.equals("postfxparam4"))
                    {
                    key = "postfx" + type + "param4";
                    }
                else if (key.equals("postfxparam5"))
                    {
                    key = "postfx" + type + "param5";
                    }
                }
            val = val / 8;
            }
        else if (key.startsWith("lfo"))
            {
            String subkey = key.substring(4);
            if (subkey.startsWith("wave"))
                {
                int lfo = StringUtility.getFirstInt(key);
                if (v == 0x01) key = "lfo" + lfo + "bpmsync";
                else if (v == 0x03) key = "lfo" + lfo + "trigsync";
                else if (v == 0x06) key = "lfo" + lfo + "smooth";
                else if (v == 0x07) key = "lfo" + lfo + "steps";
                else if (v == 0x11) key = "lfo" + lfo + "delaysyncoff";
                else if (v == 0x12) key = "lfo" + lfo + "fadeinsyncoff";
                else if (v == 0x21) key = "lfo" + lfo + "delaysyncon";
                else if (v == 0x13) key = "lfo" + lfo + "fadeinsyncon";
                else if (v == 0x14) key = "lfo" + lfo + "oneshot";
                val = w;
                }
            else if (subkey.startsWith("step"))
                {
                val = val / 8;
                }
            else if (subkey.startsWith("rate"))             // both ratesyncon and ratesyncoff
                {
                val = val / 8;
                }
            else if (subkey.startsWith("level"))
                {
                val = val / 8;
                }
            else if (subkey.endsWith("quantize"))
                {
                val = val / 8;
                }
            }
        else if (key.startsWith("env"))
            {
            String subkey = key.substring(4);
            if (subkey.startsWith("delaysyncoff"))
                {
                int env = StringUtility.getFirstInt(key);
                if (v == 0x06) key = "env" + env + "loop";
                else if (v == 0x07) key = "env" + env + "legato";
                else if (v == 0x0C) key = "env" + env + "bpmsync";
                else if (v == 0x0D) key = "env" + env + "freerun";
                else if (v == 0x0F) key = "env" + env + "reset";
                // else v = 0x08, key = delaysyncoff
                val = w;
                }
            else if (subkey.startsWith("delaysyncon"))
                {
                val = w;                       // MSB is 0x18 for some reason
                }
            else if (subkey.startsWith("attacksyncoff") ||
                subkey.startsWith("holdsyncoff") ||
                subkey.startsWith("decaysyncoff") ||
                subkey.startsWith("sustain") ||
                subkey.startsWith("releasesyncoff") || 
                subkey.startsWith("attacksyncon") ||
                subkey.startsWith("holdsyncon") ||
                subkey.startsWith("decaysyncon") ||
                subkey.startsWith("releasesyncon") ||
                subkey.startsWith("trigsrc"))
                {
                val = val / 8;
                }
            else if (subkey.endsWith("quantize"))
                {
                val = val / 8;
                }
            }
        else if (key.startsWith("arp"))
            {
            if (key.equals("arpenable"))
                {
                if (v == 1) key = "arpdivision";
                else if (v == 2) key = "arpswing";
                else if (v == 3) key = "arpgate";
                else if (v == 4) key = "arpoctmode";
                else if (v == 5) key = "arpoctave";
                else if (v == 6) key = "arpmode";
                else if (v == 7) key = "arplength";
                else if (v == 8) key = "arptaptrig";
                else if (v == 9) key = "arpphrase";
                else if (v == 10) key = "arpratchet";
                else if (v == 11) key = "arpchance";
                val = w;
                }
            }
        else if (key.startsWith("macro"))
            {
            String subkey = key.substring(6);
            if (subkey.startsWith("target"))
                {
                // Documentation is very wrong.  Actually the values are all over the place. 
                Object index = MOD_DESTINATION_NRPN_VALUES_TO_INDEX.get(val);
                if (index == null) val = 0;
                else val = ((Integer)index).intValue();
                }
            else if (subkey.startsWith("buttonvalue"))
                {
                val = val / 8;
                }
            else if (subkey.startsWith("depth"))
                {
                val = val / 8;
                }
            }
        else if (key.startsWith("modmatrix"))
            {
            String subkey = StringUtility.removePreambleAndFirstDigits(key, "modmatrix");
            if (subkey.startsWith("depth"))
                {
                val = val / 8;
                }
            else
                {
                int matrix = StringUtility.getFirstInt(key);
                if (v == 1 || v == 3)   // modsource
                    {
                    // Documentation is very wrong.  Actually the values are all over the place. 
                    key = "modmatrix" + matrix + "modsource";
                    Object index = MOD_SOURCE_NRPN_VALUES_TO_INDEX.get(val);
                    if (index == null) val = 0;
                    else val = ((Integer)index).intValue();
                    }
                else  // modtarget
                    {
                    // Documentation is very wrong.  Actually the values are all over the place. 
                    Object index = MOD_DESTINATION_NRPN_VALUES_TO_INDEX.get(val);
                    key = "modmatrix" + matrix + "modtarget";
                    if (index == null) val = 0;
                    else val = ((Integer)index).intValue();
                    }
                }
            }
        else if (key.startsWith("ribbon"))
            {
            // interesting that v doesn't start with 0.  Not sure why
            if (key.equals("ribbonmode"))
                {
                if (v == 1) key = "ribbonkeyspan";
                else if (v == 2) key = "ribbonoctave";
                else if (v == 3) key = "ribbonquantize";
                else if (v > 3 && v < 16) return;               // we don't handle ribbonscalekeylock or emitted *chromatic* scale notes 
                else if (v == 16) key = "ribbonmodcontrol";
                else if (v == 17) key = "ribbonglide";
                val = w;
                }
            }
        else if (key.startsWith("scale"))
            {
            return;         // we don't do any of it
            }
        else if (key.startsWith("voice"))
            {
            if (key.equals("voicesustain"))
                {
                val = val / 8;
                }
            else if (key.startsWith("voicemodulation"))
                {
                val = val / 8;
                }
            }
        else
            {
            // do nothing
            }
                                
        model.set(key, val);
        revise();
        }


    /** This is overridden because the Hydrasynth foolishly uses CC 120 as ARP OCTAVE
        rather than All Sounds Off.  So we can't send that or we'll break
        things on the Hydrasynth!  It has its own weird version of All Sounds Off sent 
        as an NRPN message. */
    public void sendAllSoundsOff()
        {
        //time("Sending All Sounds Off Hydrasynth-Style");
        try
            {
            // do an all notes off
            for(int i = 0; i < 16; i++)
                tryToSendMIDI(new ShortMessage(ShortMessage.CONTROL_CHANGE, i, 123, 0));
            // Send the Hydrasynth's special NRPN message for all sounds off *sort of*
            for(int i = 0; i < 16; i++)
                tryToSendMIDI(buildNRPN(i, 0x3F * 128 + 0x57, 0));
            }
        catch (InvalidMidiDataException ex)
            {
            Synth.handleException(ex);
            }
        }


    public static String getSynthName() { return "ASM Hydrasynth"; }
    
    public int getPauseAfterReceivePatch() { return 0; }

    // Change Patch can get stomped if we do a request immediately afterwards
    public int getPauseAfterChangePatch() { return (Style.isWindows() ? 1000 : 150); }

    public int getPauseAfterWritePatch() { return 3500; }       // an unbelievable number

    public int getPauseBetweenPatchWrites() { return 100; }

    public int getPauseAfterSendAllParameters()
        {
        return 300;
        }
    

    public void changePatch(Model tempModel)
        {
        // I believe that the Hydrasynth changes patches using Bank Select *LSB* (32), 
        // followed by Program Change.  
        
        int bank = tempModel.get("bank", 0);
        int number = tempModel.get("number", 0);

        /// IRRITATINGLY, if you do a PC to your current patch, it doesn't reload the patch.
        /// It just sits there and pretends it didn't happen (you still have your current patch
        /// changes).  So I have to PC AWAY from the patch and then PC BACK to the patch if it's
        /// different.

        // if (Style.isWindows())               sendFooter();   // not sure we need this
        tryToSendMIDI(buildCC(getChannelOut(), 32, bank));
        tryToSendMIDI(buildPC(getChannelOut(), (number == 0 ? 1 : 0)));         // PC away
        simplePause(getPauseAfterChangePatch());
        tryToSendMIDI(buildPC(getChannelOut(), number));                        // PC back
        }

    // This is 8 banks, appropriate for 2.0.0 and the Deluxe 1.5.5
    public Model getNextPatchLocation(Model model)
        {
        int number = model.get("number");
        int bank = model.get("bank");
                
        number++;
        if (number >= 128)
            {
            number = 0;
            bank++;
            if (bank >= 8)
                bank = 0;
            }
                                
        Model newModel = buildModel();
        newModel.set("number", number);
        newModel.set("bank", bank);
        return newModel;
        }
          
    public String getPatchName(Model model) { return model.get("name", "Untitled"); }


    public String[] getPatchNumberNames()  
        { 
        return buildIntegerNames(128, 1);
        }

    public boolean[] getWriteableBanks() 
        { 
        return new boolean[] { true, true, true, true, true, true, true, true };
        }
          
    public String[] getBankNames() { return BANKS; }

    public boolean getSupportsPatchWrites() { return true; }

    public int getPatchNameLength() { return 16; }

    public boolean getPatchContainsLocation() { return true; }
        
    public boolean librarianTested() { return true; }

    public int reviseMutatedValue(String key, int oldValue, int proposedValue,  Model model) 
        {
        if (!key.startsWith("mod")) 
            {
            return proposedValue;  // it's fine
            }
        else
            {
            if (!disallowCCMutation)        // everything is permitted
                {
                return proposedValue;
                }
            else if (key.endsWith("modsource"))
                {
                if (proposedValue < 35) // not mutating to a CC
                    return proposedValue;
                else 
                    {
                    return random.nextInt(35);      // mutate to something not a CC
                    }
                }
            else if (key.endsWith("modtarget"))
                {
                if (proposedValue < 200)  // not mutating to a CC or CV
                    return proposedValue;
                else 
                    {
                    return random.nextInt(200);     // mutate to something not a CC or CV
                    }
                }
            else return proposedValue;  // it's fine
            }
        }    




    /// WARNINGS ABOUT SENDING PARAMETERS VIA NRPN    
    ///
    /// 1. Changing certain top-level parameters (FX type, or ribbon mode, or osc1 mode, etc.) will change other parameters.
    /// there is a pecking order. First there are the modes (osc1..3mode, mutant1...3mode, ribbonmode, voiceglide), and then
    /// there's the types (osc1...3type, filter1..2type prefxtype, delaytype, reverbtype, postfxtype), and then there's the
    /// waves (lfo1...5wave), then the BPM syncs (delaybpmsync, lfo1...5bpmsync, env1...5bpmsync, voicevibratobpmsync), then
    /// the wavescanwaves (osc1...2wavescanwave1...8), which change each other in this order or further downstream pararameters.
    ///
    /// 2. Certain parameters simply require time to update or the Hydrasynth will just drop later NRPN commands as it's
    /// processing them (it seems the Hydrasynth has a limited buffer).  Notably, all Mod Matrix parameters require about 2ms
    /// wait time; and this may also be true for macro parameters.  I am presently electing to not wait in-between as the
    /// user can't easily change parameters at that rate manually, but it's a problem if you want to blast a bunch of NRPN
    /// at the unit.
        
                
    static HashMap parametersToIndex = null;
    public static final String[] parameters = new String[] 
    {
    // Missing parameters that can't be changed in real-time:
    // name, category, color

    "--",                                                       // "allosccent",                  /// This isn't a patch parameter
    "osc1mode",                                 
    "osc2mode",                                 
    "--",                                                       // "osc3mode",                    /// This parameter shouldn't exist (osc3 doesn't have a mode)
    "osc1semi",
    "osc2semi",
    "osc3semi",
    "osc1type",                                 
    "osc1cent",
    "osc1keytrack",
    "osc1wavscan",
    "--",                                                       // "osc1solowavscan1",            /// This parameter shouldn't exist (it appears to do nothing)
    "--",                                                       // "osc1solowavscan2",            /// This parameter shouldn't exist (it appears to do nothing)
    "--",                                                       // "osc1solowavscan3",            /// This parameter shouldn't exist (it appears to do nothing)
    "--",                                                       // "osc1solowavscan4",            /// This parameter shouldn't exist (it appears to do nothing)
    "--",                                                       // "osc1solowavscan5",            /// This parameter shouldn't exist (it appears to do nothing)
    "--",                                                       // "osc1solowavscan6",            /// This parameter shouldn't exist (it appears to do nothing)
    "--",                                                       // "osc1solowavscan7",            /// This parameter shouldn't exist (it appears to do nothing)
    "--",                                                       // "osc1solowavscan8",            /// This parameter shouldn't exist (it appears to do nothing)
    "osc1wavscanwave1",
    "osc1wavscanwave2",
    "osc1wavscanwave3",
    "osc1wavscanwave4",
    "osc1wavscanwave5",
    "osc1wavscanwave6",
    "osc1wavscanwave7",
    "osc1wavscanwave8",
    "osc2type",                                 
    "osc2cent",
    "osc2keytrack",
    "osc2wavscan",
    "--",                                                       // "osc2solowavscan1",            /// This parameter shouldn't exist (it appears to do nothing)
    "--",                                                       // "osc2solowavscan2",            /// This parameter shouldn't exist (it appears to do nothing)
    "--",                                                       // "osc2solowavscan3",            /// This parameter shouldn't exist (it appears to do nothing)
    "--",                                                       // "osc2solowavscan4",            /// This parameter shouldn't exist (it appears to do nothing)
    "--",                                                       // "osc2solowavscan5",            /// This parameter shouldn't exist (it appears to do nothing)
    "--",                                                       // "osc2solowavscan6",            /// This parameter shouldn't exist (it appears to do nothing)
    "--",                                                       // "osc2solowavscan7",            /// This parameter shouldn't exist (it appears to do nothing)
    "--",                                                       // "osc2solowavscan8",            /// This parameter shouldn't exist (it appears to do nothing)
    "osc2wavscanwave1",
    "osc2wavscanwave2",
    "osc2wavscanwave3",
    "osc2wavscanwave4",
    "osc2wavscanwave5",
    "osc2wavscanwave6",
    "osc2wavscanwave7",
    "osc2wavscanwave8",
    "osc3type",                                 
    "osc3cent",
    "osc3keytrack",
    "mutant1mode",                                          // These are all called "mutator" in the NRPN docs but "mutant" in the manual and on the machine
    "mutant2mode",                                      
    "mutant3mode",                                      
    "mutant4mode",                                      
    "mutant1sourcefmlin",
    "mutant2sourcefmlin",
    "mutant3sourcefmlin",
    "mutant4sourcefmlin",
    "mutant1sourceoscsync",
    "mutant2sourceoscsync",
    "mutant3sourceoscsync",
    "mutant4sourceoscsync",
    "mutant1ratio",
    "mutant1depth",
    "mutant1wet",
    "mutant1feedback",
    "mutant1window",
    "mutant1warp1",
    "mutant1warp2",
    "mutant1warp3",
    "mutant1warp4",
    "mutant1warp5",
    "mutant1warp6",
    "mutant1warp7",
    "mutant1warp8",
    "mutant2ratio",
    "mutant2depth",
    "mutant2wet",
    "mutant2feedback",
    "mutant2window",
    "mutant2warp1",
    "mutant2warp2",
    "mutant2warp3",
    "mutant2warp4",
    "mutant2warp5",
    "mutant2warp6",
    "mutant2warp7",
    "mutant2warp8",
    "mutant3ratio",
    "mutant3depth",
    "mutant3wet",
    "mutant3feedback",
    "mutant3window",
    "mutant3warp1",
    "mutant3warp2",
    "mutant3warp3",
    "mutant3warp4",
    "mutant3warp5",
    "mutant3warp6",
    "mutant3warp7",
    "mutant3warp8",
    "mutant4ratio",
    "mutant4depth",
    "mutant4wet",
    "mutant4feedback",
    "mutant4window",
    "mutant4warp1",
    "mutant4warp2",
    "mutant4warp3",
    "mutant4warp4",
    "mutant4warp5",
    "mutant4warp6",
    "mutant4warp7",
    "mutant4warp8",
    "noisetype",
    "ringmoddepth",
    "ringmodsource1",
    "ringmodsource2",
    "--",                                                       // "mixersolo",                    /// This isn't a patch parameter
    "mixerosc1vol",
    "mixerosc1pan",
    "mixerosc1filterratio",
    "mixerosc2vol",
    "mixerosc2pan",
    "mixerosc2filterratio",
    "mixerosc3vol",
    "mixerosc3pan",
    "mixerosc3filterratio",
    "mixernoisevol",
    "mixernoisepan",
    "mixernoisefilterratio",
    "mixerringmodvol",
    "mixerringmodpan",
    "mixerringmodfilterratio",
    "mixerfilterrouting",
    "filter1positionofdrive",
    "filter1cutoff",
    "filter1drive",
    "filter1resonance",
    "filter1special",
    "filter1keytrack",
    "filter1lfo1amount",
    "filter1vowelorder",
    "filter1type",                                      
    "filter1velenv",
    "filter1env1amount",
    "--",                                                    //"filter2positionofdrive",                 // This parameter does not exist
    "filter2cutoff",
    "filter2resonance",
    "filter2morph",
    "filter2keytrack",
    "filter2lfo1amount",
    "filter2velenv",
    "filter2env1amount",
    "filter2type",                                      
    "amplevel",
    "ampvelenv",
    "amplfo2amount",
    "prefxtype",                                        
    "--",                                                      // "prefxpreset",                    /// This isn't a patch parameter
    "prefxwet",
    "prefxparam1",
    "prefxparam2",
    "prefxparam3",
    "prefxparam4",
    "prefxparam5",
    "prefxsidechain",
    "delaybpmsync",                                     
    "delaywet",
    "delayfeedback",
    "delayfeedtone",
    "delaytimesyncoff",
    "delaytimesyncon",
    "delaytype",                                        
    "delaywettone",
    "reverbwet",
    "reverbhidamp",
    "reverblodamp",
    "reverbpredelay",
    "reverbtime",
    "reverbtone",
    "reverbtype",                                       
    "postfxtype",
    "--",                                                       // postfxpreset                    /// This isn't a patch parameter
    "postfxwet",
    "postfxparam1",
    "postfxparam2",
    "postfxparam3",
    "postfxparam4",
    "postfxparam5",
    "postfxsidechain",
    "lfo1level",
    "lfo1wave",                                 
    "lfo1bpmsync",                                      
    "lfo1trigsync",
    "lfo1smooth",
    "lfo1steps",
    "lfo1delaysyncoff",
    "lfo1fadeinsyncoff",
    "lfo1delaysyncon",
    "lfo1fadeinsyncon",
    "lfo1oneshot",
    "lfo1phase",
    "lfo1ratesyncoff",
    "lfo1ratesyncon",
    "lfo1step1",
    "lfo1step2",
    "lfo1step3",
    "lfo1step4",
    "lfo1step5",
    "lfo1step6",
    "lfo1step7",
    "lfo1step8",
    "lfo1step9",
    "lfo1step10",
    "lfo1step11",
    "lfo1step12",
    "lfo1step13",
    "lfo1step14",
    "lfo1step15",
    "lfo1step16",
    "lfo1step17",
    "lfo1step18",
    "lfo1step19",
    "lfo1step20",
    "lfo1step21",
    "lfo1step22",
    "lfo1step23",
    "lfo1step24",
    "lfo1step25",
    "lfo1step26",
    "lfo1step27",
    "lfo1step28",
    "lfo1step29",
    "lfo1step30",
    "lfo1step31",
    "lfo1step32",
    "lfo1step33",
    "lfo1step34",
    "lfo1step35",
    "lfo1step36",
    "lfo1step37",
    "lfo1step38",
    "lfo1step39",
    "lfo1step40",
    "lfo1step41",
    "lfo1step42",
    "lfo1step43",
    "lfo1step44",
    "lfo1step45",
    "lfo1step46",
    "lfo1step47",
    "lfo1step48",
    "lfo1step49",
    "lfo1step50",
    "lfo1step51",
    "lfo1step52",
    "lfo1step53",
    "lfo1step54",
    "lfo1step55",
    "lfo1step56",
    "lfo1step57",
    "lfo1step58",
    "lfo1step59",
    "lfo1step60",
    "lfo1step61",
    "lfo1step62",
    "lfo1step63",
    "lfo1step64",
    "lfo2level",
    "lfo2wave",                                 
    "lfo2bpmsync",                                      
    "lfo2trigsync",
    "lfo2smooth",
    "lfo2steps",
    "lfo2delaysyncoff",
    "lfo2fadeinsyncoff",
    "lfo2delaysyncon",
    "lfo2fadeinsyncon",
    "lfo2oneshot",
    "lfo2phase",
    "lfo2ratesyncoff",
    "lfo2ratesyncon",
    "lfo2step1",
    "lfo2step2",
    "lfo2step3",
    "lfo2step4",
    "lfo2step5",
    "lfo2step6",
    "lfo2step7",
    "lfo2step8",
    "lfo2step9",
    "lfo2step10",
    "lfo2step11",
    "lfo2step12",
    "lfo2step13",
    "lfo2step14",
    "lfo2step15",
    "lfo2step16",
    "lfo2step17",
    "lfo2step18",
    "lfo2step19",
    "lfo2step20",
    "lfo2step21",
    "lfo2step22",
    "lfo2step23",
    "lfo2step24",
    "lfo2step25",
    "lfo2step26",
    "lfo2step27",
    "lfo2step28",
    "lfo2step29",
    "lfo2step30",
    "lfo2step31",
    "lfo2step32",
    "lfo2step33",
    "lfo2step34",
    "lfo2step35",
    "lfo2step36",
    "lfo2step37",
    "lfo2step38",
    "lfo2step39",
    "lfo2step40",
    "lfo2step41",
    "lfo2step42",
    "lfo2step43",
    "lfo2step44",
    "lfo2step45",
    "lfo2step46",
    "lfo2step47",
    "lfo2step48",
    "lfo2step49",
    "lfo2step50",
    "lfo2step51",
    "lfo2step52",
    "lfo2step53",
    "lfo2step54",
    "lfo2step55",
    "lfo2step56",
    "lfo2step57",
    "lfo2step58",
    "lfo2step59",
    "lfo2step60",
    "lfo2step61",
    "lfo2step62",
    "lfo2step63",
    "lfo2step64",
    "lfo3level",
    "lfo3wave",                                 
    "lfo3bpmsync",                                      
    "lfo3trigsync",
    "lfo3smooth",
    "lfo3steps",
    "lfo3delaysyncoff",
    "lfo3fadeinsyncoff",
    "lfo3delaysyncon",
    "lfo3fadeinsyncon",
    "lfo3oneshot",
    "lfo3phase",
    "lfo3ratesyncoff",
    "lfo3ratesyncon",
    "lfo3step1",
    "lfo3step2",
    "lfo3step3",
    "lfo3step4",
    "lfo3step5",
    "lfo3step6",
    "lfo3step7",
    "lfo3step8",
    "lfo3step9",
    "lfo3step10",
    "lfo3step11",
    "lfo3step12",
    "lfo3step13",
    "lfo3step14",
    "lfo3step15",
    "lfo3step16",
    "lfo3step17",
    "lfo3step18",
    "lfo3step19",
    "lfo3step20",
    "lfo3step21",
    "lfo3step22",
    "lfo3step23",
    "lfo3step24",
    "lfo3step25",
    "lfo3step26",
    "lfo3step27",
    "lfo3step28",
    "lfo3step29",
    "lfo3step30",
    "lfo3step31",
    "lfo3step32",
    "lfo3step33",
    "lfo3step34",
    "lfo3step35",
    "lfo3step36",
    "lfo3step37",
    "lfo3step38",
    "lfo3step39",
    "lfo3step40",
    "lfo3step41",
    "lfo3step42",
    "lfo3step43",
    "lfo3step44",
    "lfo3step45",
    "lfo3step46",
    "lfo3step47",
    "lfo3step48",
    "lfo3step49",
    "lfo3step50",
    "lfo3step51",
    "lfo3step52",
    "lfo3step53",
    "lfo3step54",
    "lfo3step55",
    "lfo3step56",
    "lfo3step57",
    "lfo3step58",
    "lfo3step59",
    "lfo3step60",
    "lfo3step61",
    "lfo3step62",
    "lfo3step63",
    "lfo3step64",
    "lfo4level",
    "lfo4wave",                                 
    "lfo4bpmsync",                                      
    "lfo4trigsync",
    "lfo4smooth",
    "lfo4steps",
    "lfo4delaysyncoff",
    "lfo4fadeinsyncoff",
    "lfo4delaysyncon",
    "lfo4fadeinsyncon",
    "lfo4oneshot",
    "lfo4phase",
    "lfo4ratesyncoff",
    "lfo4ratesyncon",
    "lfo4step1",
    "lfo4step2",
    "lfo4step3",
    "lfo4step4",
    "lfo4step5",
    "lfo4step6",
    "lfo4step7",
    "lfo4step8",
    "lfo4step9",
    "lfo4step10",
    "lfo4step11",
    "lfo4step12",
    "lfo4step13",
    "lfo4step14",
    "lfo4step15",
    "lfo4step16",
    "lfo4step17",
    "lfo4step18",
    "lfo4step19",
    "lfo4step20",
    "lfo4step21",
    "lfo4step22",
    "lfo4step23",
    "lfo4step24",
    "lfo4step25",
    "lfo4step26",
    "lfo4step27",
    "lfo4step28",
    "lfo4step29",
    "lfo4step30",
    "lfo4step31",
    "lfo4step32",
    "lfo4step33",
    "lfo4step34",
    "lfo4step35",
    "lfo4step36",
    "lfo4step37",
    "lfo4step38",
    "lfo4step39",
    "lfo4step40",
    "lfo4step41",
    "lfo4step42",
    "lfo4step43",
    "lfo4step44",
    "lfo4step45",
    "lfo4step46",
    "lfo4step47",
    "lfo4step48",
    "lfo4step49",
    "lfo4step50",
    "lfo4step51",
    "lfo4step52",
    "lfo4step53",
    "lfo4step54",
    "lfo4step55",
    "lfo4step56",
    "lfo4step57",
    "lfo4step58",
    "lfo4step59",
    "lfo4step60",
    "lfo4step61",
    "lfo4step62",
    "lfo4step63",
    "lfo4step64",
    "lfo5level",
    "lfo5wave",                                 
    "lfo5bpmsync",                                      
    "lfo5trigsync",
    "lfo5smooth",
    "lfo5steps",
    "lfo5delaysyncoff",
    "lfo5fadeinsyncoff",
    "lfo5delaysyncon",
    "lfo5fadeinsyncon",
    "lfo5oneshot",
    "lfo5phase",
    "lfo5ratesyncoff",
    "lfo5ratesyncon",
    "lfo5step1",
    "lfo5step2",
    "lfo5step3",
    "lfo5step4",
    "lfo5step5",
    "lfo5step6",
    "lfo5step7",
    "lfo5step8",
    "lfo5step9",
    "lfo5step10",
    "lfo5step11",
    "lfo5step12",
    "lfo5step13",
    "lfo5step14",
    "lfo5step15",
    "lfo5step16",
    "lfo5step17",
    "lfo5step18",
    "lfo5step19",
    "lfo5step20",
    "lfo5step21",
    "lfo5step22",
    "lfo5step23",
    "lfo5step24",
    "lfo5step25",
    "lfo5step26",
    "lfo5step27",
    "lfo5step28",
    "lfo5step29",
    "lfo5step30",
    "lfo5step31",
    "lfo5step32",
    "lfo5step33",
    "lfo5step34",
    "lfo5step35",
    "lfo5step36",
    "lfo5step37",
    "lfo5step38",
    "lfo5step39",
    "lfo5step40",
    "lfo5step41",
    "lfo5step42",
    "lfo5step43",
    "lfo5step44",
    "lfo5step45",
    "lfo5step46",
    "lfo5step47",
    "lfo5step48",
    "lfo5step49",
    "lfo5step50",
    "lfo5step51",
    "lfo5step52",
    "lfo5step53",
    "lfo5step54",
    "lfo5step55",
    "lfo5step56",
    "lfo5step57",
    "lfo5step58",
    "lfo5step59",
    "lfo5step60",
    "lfo5step61",
    "lfo5step62",
    "lfo5step63",
    "lfo5step64",
    "env1delaysyncoff",
    "env1attacksyncoff",
    "env1holdsyncoff",
    "env1decaysyncoff",
    "env1sustain",
    "env1releasesyncoff",
    "env1delaysyncon",
    "env1attacksyncon",
    "env1decaysyncon",
    "env1holdsyncon",
    "env1releasesyncon",
    "env1atkcurve",
    "env1deccurve",
    "env1loop",
    "env1legato",
    "env1bpmsync",                                      
    "env1freerun",
    "env1reset",
    "env1relcurve",
    "env1trigsrc1",
    "env1trigsrc2",
    "env1trigsrc3",
    "env1trigsrc4",
    "env2delaysyncoff",
    "env2attacksyncoff",
    "env2holdsyncoff",
    "env2decaysyncoff",
    "env2sustain",
    "env2releasesyncoff",
    "env2delaysyncon",
    "env2attacksyncon",
    "env2decaysyncon",
    "env2holdsyncon",
    "env2releasesyncon",
    "env2atkcurve",
    "env2deccurve",
    "env2loop",
    "env2legato",
    "env2bpmsync",                                      
    "env2freerun",
    "env2reset",
    "env2relcurve",
    "env2trigsrc1",
    "env2trigsrc2",
    "env2trigsrc3",
    "env2trigsrc4",
    "env3delaysyncoff",
    "env3attacksyncoff",
    "env3holdsyncoff",
    "env3decaysyncoff",
    "env3sustain",
    "env3releasesyncoff",
    "env3delaysyncon",
    "env3attacksyncon",
    "env3decaysyncon",
    "env3holdsyncon",
    "env3releasesyncon",
    "env3atkcurve",
    "env3deccurve",
    "env3loop",
    "env3legato",
    "env3bpmsync",                                      
    "env3freerun",
    "env3reset",
    "env3relcurve",
    "env3trigsrc1",
    "env3trigsrc2",
    "env3trigsrc3",
    "env3trigsrc4",
    "env4delaysyncoff",
    "env4attacksyncoff",
    "env4holdsyncoff",
    "env4decaysyncoff",
    "env4sustain",
    "env4releasesyncoff",
    "env4delaysyncon",
    "env4attacksyncon",
    "env4decaysyncon",
    "env4holdsyncon",
    "env4releasesyncon",
    "env4atkcurve",
    "env4deccurve",
    "env4loop",
    "env4legato",
    "env4bpmsync",                                      
    "env4freerun",
    "env4reset",
    "env4relcurve",
    "env4trigsrc1",
    "env4trigsrc2",
    "env4trigsrc3",
    "env4trigsrc4",
    "env5delaysyncoff",
    "env5attacksyncoff",
    "env5holdsyncoff",
    "env5decaysyncoff",
    "env5sustain",
    "env5releasesyncoff",
    "env5delaysyncon",
    "env5attacksyncon",
    "env5decaysyncon",
    "env5holdsyncon",
    "env5releasesyncon",
    "env5atkcurve",
    "env5deccurve",
    "env5loop",
    "env5legato",
    "env5bpmsync",                                      
    "env5freerun",
    "env5reset",
    "env5relcurve",
    "env5trigsrc1",
    "env5trigsrc2",
    "env5trigsrc3",
    "env5trigsrc4",
    "arptempo",
    "arpenable",
    "arpdivision",
    "arpswing",
    "arpgate",
    "arpoctmode",
    "arpoctave",
    "arpmode",
    "arplength",
    "arptaptrig",
    "arpphrase",
    "arpratchet",
    "arpchance",
    "macro1target1",
    "macro1target2",
    "macro1target3",
    "macro1target4",
    "macro1target5",
    "macro1target6",
    "macro1target7",
    "macro1target8",
    "macro1buttonvalue1",
    "macro1buttonvalue2",
    "macro1buttonvalue3",
    "macro1buttonvalue4",
    "macro1buttonvalue5",
    "macro1buttonvalue6",
    "macro1buttonvalue7",
    "macro1buttonvalue8",
    "macro1depth1",
    "macro1depth2",
    "macro1depth3",
    "macro1depth4",
    "macro1depth5",
    "macro1depth6",
    "macro1depth7",
    "macro1depth8",
    "macro2target1",
    "macro2target2",
    "macro2target3",
    "macro2target4",
    "macro2target5",
    "macro2target6",
    "macro2target7",
    "macro2target8",
    "macro2buttonvalue1",
    "macro2buttonvalue2",
    "macro2buttonvalue3",
    "macro2buttonvalue4",
    "macro2buttonvalue5",
    "macro2buttonvalue6",
    "macro2buttonvalue7",
    "macro2buttonvalue8",
    "macro2depth1",
    "macro2depth2",
    "macro2depth3",
    "macro2depth4",
    "macro2depth5",
    "macro2depth6",
    "macro2depth7",
    "macro2depth8",
    "macro3target1",
    "macro3target2",
    "macro3target3",
    "macro3target4",
    "macro3target5",
    "macro3target6",
    "macro3target7",
    "macro3target8",
    "macro3buttonvalue1",
    "macro3buttonvalue2",
    "macro3buttonvalue3",
    "macro3buttonvalue4",
    "macro3buttonvalue5",
    "macro3buttonvalue6",
    "macro3buttonvalue7",
    "macro3buttonvalue8",
    "macro3depth1",
    "macro3depth2",
    "macro3depth3",
    "macro3depth4",
    "macro3depth5",
    "macro3depth6",
    "macro3depth7",
    "macro3depth8",
    "macro4target1",
    "macro4target2",
    "macro4target3",
    "macro4target4",
    "macro4target5",
    "macro4target6",
    "macro4target7",
    "macro4target8",
    "macro4buttonvalue1",
    "macro4buttonvalue2",
    "macro4buttonvalue3",
    "macro4buttonvalue4",
    "macro4buttonvalue5",
    "macro4buttonvalue6",
    "macro4buttonvalue7",
    "macro4buttonvalue8",
    "macro4depth1",
    "macro4depth2",
    "macro4depth3",
    "macro4depth4",
    "macro4depth5",
    "macro4depth6",
    "macro4depth7",
    "macro4depth8",
    "macro5target1",
    "macro5target2",
    "macro5target3",
    "macro5target4",
    "macro5target5",
    "macro5target6",
    "macro5target7",
    "macro5target8",
    "macro5buttonvalue1",
    "macro5buttonvalue2",
    "macro5buttonvalue3",
    "macro5buttonvalue4",
    "macro5buttonvalue5",
    "macro5buttonvalue6",
    "macro5buttonvalue7",
    "macro5buttonvalue8",
    "macro5depth1",
    "macro5depth2",
    "macro5depth3",
    "macro5depth4",
    "macro5depth5",
    "macro5depth6",
    "macro5depth7",
    "macro5depth8",
    "macro6target1",
    "macro6target2",
    "macro6target3",
    "macro6target4",
    "macro6target5",
    "macro6target6",
    "macro6target7",
    "macro6target8",
    "macro6buttonvalue1",
    "macro6buttonvalue2",
    "macro6buttonvalue3",
    "macro6buttonvalue4",
    "macro6buttonvalue5",
    "macro6buttonvalue6",
    "macro6buttonvalue7",
    "macro6buttonvalue8",
    "macro6depth1",
    "macro6depth2",
    "macro6depth3",
    "macro6depth4",
    "macro6depth5",
    "macro6depth6",
    "macro6depth7",
    "macro6depth8",
    "macro7target1",
    "macro7target2",
    "macro7target3",
    "macro7target4",
    "macro7target5",
    "macro7target6",
    "macro7target7",
    "macro7target8",
    "macro7buttonvalue1",
    "macro7buttonvalue2",
    "macro7buttonvalue3",
    "macro7buttonvalue4",
    "macro7buttonvalue5",
    "macro7buttonvalue6",
    "macro7buttonvalue7",
    "macro7buttonvalue8",
    "macro7depth1",
    "macro7depth2",
    "macro7depth3",
    "macro7depth4",
    "macro7depth5",
    "macro7depth6",
    "macro7depth7",
    "macro7depth8",
    "macro8target1",
    "macro8target2",
    "macro8target3",
    "macro8target4",
    "macro8target5",
    "macro8target6",
    "macro8target7",
    "macro8target8",
    "macro8buttonvalue1",
    "macro8buttonvalue2",
    "macro8buttonvalue3",
    "macro8buttonvalue4",
    "macro8buttonvalue5",
    "macro8buttonvalue6",
    "macro8buttonvalue7",
    "macro8buttonvalue8",
    "macro8depth1",
    "macro8depth2",
    "macro8depth3",
    "macro8depth4",
    "macro8depth5",
    "macro8depth6",
    "macro8depth7",
    "macro8depth8",
    "macro1panelvalue",
    "macro2panelvalue",
    "macro3panelvalue",
    "macro4panelvalue",
    "macro5panelvalue",
    "macro6panelvalue",
    "macro7panelvalue",
    "macro8panelvalue",
    "modmatrix1modsource",
    "modmatrix2modsource",
    "modmatrix3modsource",
    "modmatrix4modsource",
    "modmatrix5modsource",
    "modmatrix6modsource",
    "modmatrix7modsource",
    "modmatrix8modsource",
    "modmatrix9modsource",
    "modmatrix10modsource",
    "modmatrix11modsource",
    "modmatrix12modsource",
    "modmatrix13modsource",
    "modmatrix14modsource",
    "modmatrix15modsource",
    "modmatrix16modsource",
    "modmatrix17modsource",
    "modmatrix18modsource",
    "modmatrix19modsource",
    "modmatrix20modsource",
    "modmatrix21modsource",
    "modmatrix22modsource",
    "modmatrix23modsource",
    "modmatrix24modsource",
    "modmatrix25modsource",
    "modmatrix26modsource",
    "modmatrix27modsource",
    "modmatrix28modsource",
    "modmatrix29modsource",
    "modmatrix30modsource",
    "modmatrix31modsource",
    "modmatrix32modsource",
    "modmatrix1modtarget",
    "modmatrix2modtarget",
    "modmatrix3modtarget",
    "modmatrix4modtarget",
    "modmatrix5modtarget",
    "modmatrix6modtarget",
    "modmatrix7modtarget",
    "modmatrix8modtarget",
    "modmatrix9modtarget",
    "modmatrix10modtarget",
    "modmatrix11modtarget",
    "modmatrix12modtarget",
    "modmatrix13modtarget",
    "modmatrix14modtarget",
    "modmatrix15modtarget",
    "modmatrix16modtarget",
    "modmatrix17modtarget",
    "modmatrix18modtarget",
    "modmatrix19modtarget",
    "modmatrix20modtarget",
    "modmatrix21modtarget",
    "modmatrix22modtarget",
    "modmatrix23modtarget",
    "modmatrix24modtarget",
    "modmatrix25modtarget",
    "modmatrix26modtarget",
    "modmatrix27modtarget",
    "modmatrix28modtarget",
    "modmatrix29modtarget",
    "modmatrix30modtarget",
    "modmatrix31modtarget",
    "modmatrix32modtarget",
    "modmatrix1depth",
    "modmatrix2depth",
    "modmatrix3depth",
    "modmatrix4depth",
    "modmatrix5depth",
    "modmatrix6depth",
    "modmatrix7depth",
    "modmatrix8depth",
    "modmatrix9depth",
    "modmatrix10depth",
    "modmatrix11depth",
    "modmatrix12depth",
    "modmatrix13depth",
    "modmatrix14depth",
    "modmatrix15depth",
    "modmatrix16depth",
    "modmatrix17depth",
    "modmatrix18depth",
    "modmatrix19depth",
    "modmatrix20depth",
    "modmatrix21depth",
    "modmatrix22depth",
    "modmatrix23depth",
    "modmatrix24depth",
    "modmatrix25depth",
    "modmatrix26depth",
    "modmatrix27depth",
    "modmatrix28depth",
    "modmatrix29depth",
    "modmatrix30depth",
    "modmatrix31depth",
    "modmatrix32depth",
    "ribbonmode",                                                                      
    "ribbonkeyspan",
    "ribbonoctave",
    "ribbonquantize",
    "ribbonmodcontrol",
    "ribbonglide",
    "voicedetune",
    "voicestereowidth",
    "voicevibratoamount",
    "voiceanalogfeel",
    "voicedensity",
    "voiceglidecurve",
    "voiceglide",
    "voiceglidelegato",
    "voiceglidetime",
    "voicestereomode",
    "voicepolyphony",
    "voicepitchbend",
    "voicevibratoratesyncoff",
    "voicevibratoratesyncon",
    "voicerandomphase",
    "voicewarmmode",
    "voicevibratobpmsync",  
    "voicesnap",                                  // Not Documented   
    
    /// New 2.0.0 Parameters
    "voicesustain",
    "osc1bitreduction",                  
    "osc2bitreduction",                  
    "osc3bitreduction",                  
    "voicemodulation1",                  
    "voicemodulation2",                  
    "voicemodulation3",                  
    "voicemodulation4",                  
    "voicemodulation5",                  
    "voicemodulation6",                  
    "voicemodulation7",                  
    "voicemodulation8",                  
    "env1quantize",                  
    "env2quantize",                  
    "env3quantize",                  
    "env4quantize",                  
    "env5quantize",                  
    "lfo1quantize",                  
    "lfo2quantize",                  
    "lfo3quantize",                  
    "lfo4quantize",                  
    "lfo5quantize",  

    // Panel Buton values (have no useful NRPN)
    // "macro1panelbuttonvalue",
    // "macro2panelbuttonvalue",
    // "macro3panelbuttonvalue",
    // "macro4panelbuttonvalue",
    // "macro5panelbuttonvalue",
    // "macro6panelbuttonvalue",
    // "macro7panelbuttonvalue",
    // "macro8panelbuttonvalue",
    };
    
    
    
    
    static HashMap nrpnToIndex = null;
    public static final int[] nrpn = new int[]
    {
    8324,           // 41 4             "allosccent"            
    8088,           // 3f 18            "osc1mode",
    8088,           // 3f 18            "osc2mode",
    8088,           // 3f 18            "osc3mode",
    8081,           // 3f 11            "osc1semi",
    8081,           // 3f 11            "osc2semi",
    8081,           // 3f 11            "osc3semi",
    8089,           // 3f 19            "osc1type",
    8321,           // 41 1             "osc1cent",
    8148,           // 3f 54            "osc1keytrack",
    8362,           // 41 2a            "osc1wavscan",
    8091,           // 3f 1b            "osc1solowavscan1",
    8091,           // 3f 1b            "osc1solowavscan2",
    8091,           // 3f 1b            "osc1solowavscan3",
    8091,           // 3f 1b            "osc1solowavscan4",
    8091,           // 3f 1b            "osc1solowavscan5",
    8091,           // 3f 1b            "osc1solowavscan6",
    8091,           // 3f 1b            "osc1solowavscan7",
    8091,           // 3f 1b            "osc1solowavscan8",
    8160,           // 3f 60            "osc1wavscanwave1",
    8161,           // 3f 61            "osc1wavscanwave2",
    8162,           // 3f 62            "osc1wavscanwave3",
    8163,           // 3f 63            "osc1wavscanwave4",
    8164,           // 3f 64            "osc1wavscanwave5",
    8165,           // 3f 65            "osc1wavscanwave6",
    8166,           // 3f 66            "osc1wavscanwave7",
    8167,           // 3f 67            "osc1wavscanwave8",
    8090,           // 3f 1a            "osc2type",
    8322,           // 41 2             "osc2cent",
    8149,           // 3f 55            "osc2keytrack",
    8363,           // 41 2b            "osc2wavscan",
    8092,           // 3f 1c            "osc2solowavscan1",
    8092,           // 3f 1c            "osc2solowavscan2",
    8092,           // 3f 1c            "osc2solowavscan3",
    8092,           // 3f 1c            "osc2solowavscan4",
    8092,           // 3f 1c            "osc2solowavscan5",
    8092,           // 3f 1c            "osc2solowavscan6",
    8092,           // 3f 1c            "osc2solowavscan7",
    8092,           // 3f 1c            "osc2solowavscan8",
    8168,           // 3f 68            "osc2wavscanwave1",
    8169,           // 3f 69            "osc2wavscanwave2",
    8170,           // 3f 6a            "osc2wavscanwave3",
    8171,           // 3f 6b            "osc2wavscanwave4",
    8172,           // 3f 6c            "osc2wavscanwave5",
    8173,           // 3f 6d            "osc2wavscanwave6",
    8174,           // 3f 6e            "osc2wavscanwave7",
    8175,           // 3f 6f            "osc2wavscanwave8",
    8077,           // 3f d             "osc3type",
    8323,           // 41 3             "osc3cent",
    8150,           // 3f 56            "osc3keytrack",
    8097,           // 3f 21            "mutant1mode",
    8097,           // 3f 21            "mutant2mode",
    8097,           // 3f 21            "mutant3mode",
    8097,           // 3f 21            "mutant4mode",
    8100,           // 3f 24            "mutant1sourcefmlin",
    8100,           // 3f 24            "mutant2sourcefmlin",
    8100,           // 3f 24            "mutant3sourcefmlin",
    8100,           // 3f 24            "mutant4sourcefmlin",
    8098,           // 3f 22            "mutant1sourceoscsync",
    8098,           // 3f 22            "mutant2sourceoscsync",
    8098,           // 3f 22            "mutant3sourceoscsync",
    8098,           // 3f 22            "mutant4sourceoscsync",
    8364,           // 41 2c            "mutant1ratio",
    8223,           // 40 1f            "mutant1depth",
    8226,           // 40 22            "mutant1wet",
    8229,           // 40 25            "mutant1feedback",
    8220,           // 40 1c            "mutant1window",
    8288,           // 40 60            "mutant1warp1",
    8289,           // 40 61            "mutant1warp2",
    8290,           // 40 62            "mutant1warp3",
    8291,           // 40 63            "mutant1warp4",
    8292,           // 40 64            "mutant1warp5",
    8293,           // 40 65            "mutant1warp6",
    8294,           // 40 66            "mutant1warp7",
    8295,           // 40 67            "mutant1warp8",
    8365,           // 41 2d            "mutant2ratio",
    8224,           // 40 20            "mutant2depth",
    8227,           // 40 23            "mutant2wet",
    8230,           // 40 26            "mutant2feedback",
    8221,           // 40 1d            "mutant2window",
    8296,           // 40 68            "mutant2warp1",
    8297,           // 40 69            "mutant2warp2",
    8298,           // 40 6a            "mutant2warp3",
    8299,           // 40 6b            "mutant2warp4",
    8300,           // 40 6c            "mutant2warp5",
    8301,           // 40 6d            "mutant2warp6",
    8302,           // 40 6e            "mutant2warp7",
    8303,           // 40 6f            "mutant2warp8",
    8366,           // 41 2e            "mutant3ratio",
    8225,           // 40 21            "mutant3depth",
    8228,           // 40 24            "mutant3wet",
    8231,           // 40 27            "mutant3feedback",
    8222,           // 40 1e            "mutant3window",
    8304,           // 40 70            "mutant3warp1",
    8305,           // 40 71            "mutant3warp2",
    8306,           // 40 72            "mutant3warp3",
    8307,           // 40 73            "mutant3warp4",
    8308,           // 40 74            "mutant3warp5",
    8309,           // 40 75            "mutant3warp6",
    8310,           // 40 76            "mutant3warp7",
    8311,           // 40 77            "mutant3warp8",
    8367,           // 41 2f            "mutant4ratio",
    8214,           // 40 16            "mutant4depth",
    8215,           // 40 17            "mutant4wet",
    8219,           // 40 1b            "mutant4feedback",
    8218,           // 40 1a            "mutant4window",
    8312,           // 40 78            "mutant4warp1",
    8313,           // 40 79            "mutant4warp2",
    8314,           // 40 7a            "mutant4warp3",
    8315,           // 40 7b            "mutant4warp4",
    8316,           // 40 7c            "mutant4warp5",
    8317,           // 40 7d            "mutant4warp6",
    8318,           // 40 7e            "mutant4warp7",
    8319,           // 40 7f            "mutant4warp8",
    8103,           // 3f 27            "noisetype",
    8195,           // 40 3             "ringmoddepth",
    8102,           // 3f 26            "ringmodsource1",
    8102,           // 3f 26            "ringmodsource2",
    8101,           // 3f 25            "mixersolo",
    8199,           // 40 7             "mixerosc1vol",
    8200,           // 40 8             "mixerosc1pan",
    8241,           // 40 31            "mixerosc1filterratio",
    8201,           // 40 9             "mixerosc2vol",
    8202,           // 40 a             "mixerosc2pan",
    8242,           // 40 32            "mixerosc2filterratio",
    8203,           // 40 b             "mixerosc3vol",
    8204,           // 40 c             "mixerosc3pan",
    8243,           // 40 33            "mixerosc3filterratio",
    8205,           // 40 d             "mixernoisevol",
    8206,           // 40 e             "mixernoisepan",
    8244,           // 40 34            "mixernoisefilterratio",
    8193,           // 40 1             "mixerringmodvol",
    8196,           // 40 4             "mixerringmodpan",
    8245,           // 40 35            "mixerringmodfilterratio",
    8108,           // 3f 2c            "mixerfilterrouting",
    8105,           // 3f 29            "filter1positionofdrive",
    8232,           // 40 28            "filter1cutoff",
    8235,           // 40 2b            "filter1drive",
    8233,           // 40 29            "filter1resonance",
    8234,           // 40 2a            "filter1special",
    8422,           // 41 66            "filter1keytrack",
    8416,           // 41 60            "filter1lfo1amount",
    8110,           // 3f 2e            "filter1vowelorder",
    8104,           // 3f 28            "filter1type",
    8425,           // 41 69            "filter1velenv",
    8417,           // 41 61            "filter1env1amount",
    8107,           // 3f 2b            "filter2positionofdrive",
    8236,           // 40 2c            "filter2cutoff",
    8237,           // 40 2d            "filter2resonance",
    8238,           // 40 2e            "filter2morph",
    8423,           // 41 67            "filter2keytrack",
    8418,           // 41 62            "filter2lfo1amount",
    8426,           // 41 6a            "filter2velenv",
    8419,           // 41 63            "filter2env1amount",
    8099,           // 3f 23            "filter2type",
    8194,           // 40 2             "amplevel",
    8427,           // 41 6b            "ampvelenv",
    8420,           // 41 64            "amplfo2amount",
    7679,           // 3b 7f            "prefxtype",
    7552,           // 3b 0             "prefxpreset",
    8430,           // 41 6e            "prefxwet",
    8431,           // 41 6f            "prefxparam1",
    8432,           // 41 70            "prefxparam2",
    7600,           // 3b 30            "prefxparam3",
    7616,           // 3b 40            "prefxparam4",
    7632,           // 3b 50            "prefxparam5",
    7667,           // 3b 73            "prefxsidechain",
    7664,           // 3b 70            "delaybpmsync",
    8440,           // 41 78            "delaywet",
    8437,           // 41 75            "delayfeedback",
    8438,           // 41 76            "delayfeedtone",
    8436,           // 41 74            "delaytimesyncoff",
    8692,           // 43 74            "delaytimesyncon",
    7665,           // 3b 71            "delaytype",
    8439,           // 41 77            "delaywettone",
    8446,           // 41 7e            "reverbwet",
    8443,           // 41 7b            "reverbhidamp",
    8444,           // 41 7c            "reverblodamp",
    8445,           // 41 7d            "reverbpredelay",
    8441,           // 41 79            "reverbtime",
    8442,           // 41 7a            "reverbtone",
    7794,           // 3c 72            "reverbtype",
    7807,           // 3c 7f            "postfxtype",
    7680,           // 3c 0             "postfxpreset",
    8433,           // 41 71            "postfxwet",
    8434,           // 41 72            "postfxparam1",
    8435,           // 41 73            "postfxparam2",
    7728,           // 3c 30            "postfxparam3",
    7744,           // 3c 40            "postfxparam4",
    7760,           // 3c 50            "postfxparam5",
    7795,           // 3c 73            "postfxsidechain",
    8331,           // 41 b             "lfo1level",
    8068,           // 3f 4             "lfo1wave",
    8068,           // 3f 4             "lfo1bpmsync",
    8068,           // 3f 4             "lfo1trigsync",
    8068,           // 3f 4             "lfo1smooth",
    8068,           // 3f 4             "lfo1steps",
    8068,           // 3f 4             "lfo1delaysyncoff",
    8068,           // 3f 4             "lfo1fadeinsyncoff",
    8068,           // 3f 4             "lfo1delaysyncon",
    8068,           // 3f 4             "lfo1fadeinsyncon",
    8068,           // 3f 4             "lfo1oneshot",
    8112,           // 3f 30            "lfo1phase",
    8325,           // 41 5             "lfo1ratesyncoff",
    8581,           // 43 5             "lfo1ratesyncon",
    7440,           // 3a 10            "lfo1step1",
    7441,           // 3a 11            "lfo1step2",
    7442,           // 3a 12            "lfo1step3",
    7443,           // 3a 13            "lfo1step4",
    7444,           // 3a 14            "lfo1step5",
    7445,           // 3a 15            "lfo1step6",
    7446,           // 3a 16            "lfo1step7",
    7447,           // 3a 17            "lfo1step8",
    9472,           // 4a 0             "lfo1step9",
    9473,           // 4a 1             "lfo1step10",
    9474,           // 4a 2             "lfo1step11",
    9475,           // 4a 3             "lfo1step12",
    9476,           // 4a 4             "lfo1step13",
    9477,           // 4a 5             "lfo1step14",
    9478,           // 4a 6             "lfo1step15",
    9479,           // 4a 7             "lfo1step16",
    9480,           // 4a 8             "lfo1step17",
    9481,           // 4a 9             "lfo1step18",
    9482,           // 4a a             "lfo1step19",
    9483,           // 4a b             "lfo1step20",
    9484,           // 4a c             "lfo1step21",
    9485,           // 4a d             "lfo1step22",
    9486,           // 4a e             "lfo1step23",
    9487,           // 4a f             "lfo1step24",
    9488,           // 4a 10            "lfo1step25",
    9489,           // 4a 11            "lfo1step26",
    9490,           // 4a 12            "lfo1step27",
    9491,           // 4a 13            "lfo1step28",
    9492,           // 4a 14            "lfo1step29",
    9493,           // 4a 15            "lfo1step30",
    9494,           // 4a 16            "lfo1step31",
    9495,           // 4a 17            "lfo1step32",
    9496,           // 4a 18            "lfo1step33",
    9497,           // 4a 19            "lfo1step34",
    9498,           // 4a 1a            "lfo1step35",
    9499,           // 4a 1b            "lfo1step36",
    9500,           // 4a 1c            "lfo1step37",
    9501,           // 4a 1d            "lfo1step38",
    9502,           // 4a 1e            "lfo1step39",
    9503,           // 4a 1f            "lfo1step40",
    9504,           // 4a 20            "lfo1step41",
    9505,           // 4a 21            "lfo1step42",
    9506,           // 4a 22            "lfo1step43",
    9507,           // 4a 23            "lfo1step44",
    9508,           // 4a 24            "lfo1step45",
    9509,           // 4a 25            "lfo1step46",
    9510,           // 4a 26            "lfo1step47",
    9511,           // 4a 27            "lfo1step48",
    9512,           // 4a 28            "lfo1step49",
    9513,           // 4a 29            "lfo1step50",
    9514,           // 4a 2a            "lfo1step51",
    9515,           // 4a 2b            "lfo1step52",
    9516,           // 4a 2c            "lfo1step53",
    9517,           // 4a 2d            "lfo1step54",
    9518,           // 4a 2e            "lfo1step55",
    9519,           // 4a 2f            "lfo1step56",
    9520,           // 4a 30            "lfo1step57",
    9521,           // 4a 31            "lfo1step58",
    9522,           // 4a 32            "lfo1step59",
    9523,           // 4a 33            "lfo1step60",
    9524,           // 4a 34            "lfo1step61",
    9525,           // 4a 35            "lfo1step62",
    9526,           // 4a 36            "lfo1step63",
    9527,           // 4a 37            "lfo1step64",
    8332,           // 41 c             "lfo2level",
    8069,           // 3f 5             "lfo2wave",
    8069,           // 3f 5             "lfo2bpmsync",
    8069,           // 3f 5             "lfo2trigsync",
    8069,           // 3f 5             "lfo2smooth",
    8069,           // 3f 5             "lfo2steps",
    8069,           // 3f 5             "lfo2delaysyncoff",
    8069,           // 3f 5             "lfo2fadeinsyncoff",
    8069,           // 3f 5             "lfo2delaysyncon",
    8069,           // 3f 5             "lfo2fadeinsyncon",
    8069,           // 3f 5             "lfo2oneshot",
    8113,           // 3f 31            "lfo2phase",
    8326,           // 41 6             "lfo2ratesyncoff",
    8582,           // 43 6             "lfo2ratesyncon",
    7448,           // 3a 18            "lfo2step1",
    7449,           // 3a 19            "lfo2step2",
    7450,           // 3a 1a            "lfo2step3",
    7451,           // 3a 1b            "lfo2step4",
    7452,           // 3a 1c            "lfo2step5",
    7453,           // 3a 1d            "lfo2step6",
    7454,           // 3a 1e            "lfo2step7",
    7455,           // 3a 1f            "lfo2step8",
    9536,           // 4a 40            "lfo2step9",
    9537,           // 4a 41            "lfo2step10",
    9538,           // 4a 42            "lfo2step11",
    9539,           // 4a 43            "lfo2step12",
    9540,           // 4a 44            "lfo2step13",
    9541,           // 4a 45            "lfo2step14",
    9542,           // 4a 46            "lfo2step15",
    9543,           // 4a 47            "lfo2step16",
    9544,           // 4a 48            "lfo2step17",
    9545,           // 4a 49            "lfo2step18",
    9546,           // 4a 4a            "lfo2step19",
    9547,           // 4a 4b            "lfo2step20",
    9548,           // 4a 4c            "lfo2step21",
    9549,           // 4a 4d            "lfo2step22",
    9550,           // 4a 4e            "lfo2step23",
    9551,           // 4a 4f            "lfo2step24",
    9552,           // 4a 50            "lfo2step25",
    9553,           // 4a 51            "lfo2step26",
    9554,           // 4a 52            "lfo2step27",
    9555,           // 4a 53            "lfo2step28",
    9556,           // 4a 54            "lfo2step29",
    9557,           // 4a 55            "lfo2step30",
    9558,           // 4a 56            "lfo2step31",
    9559,           // 4a 57            "lfo2step32",
    9560,           // 4a 58            "lfo2step33",
    9561,           // 4a 59            "lfo2step34",
    9562,           // 4a 5a            "lfo2step35",
    9563,           // 4a 5b            "lfo2step36",
    9564,           // 4a 5c            "lfo2step37",
    9565,           // 4a 5d            "lfo2step38",
    9566,           // 4a 5e            "lfo2step39",
    9567,           // 4a 5f            "lfo2step40",
    9568,           // 4a 60            "lfo2step41",
    9569,           // 4a 61            "lfo2step42",
    9570,           // 4a 62            "lfo2step43",
    9571,           // 4a 63            "lfo2step44",
    9572,           // 4a 64            "lfo2step45",
    9573,           // 4a 65            "lfo2step46",
    9574,           // 4a 66            "lfo2step47",
    9575,           // 4a 67            "lfo2step48",
    9576,           // 4a 68            "lfo2step49",
    9577,           // 4a 69            "lfo2step50",
    9578,           // 4a 6a            "lfo2step51",
    9579,           // 4a 6b            "lfo2step52",
    9580,           // 4a 6c            "lfo2step53",
    9581,           // 4a 6d            "lfo2step54",
    9582,           // 4a 6e            "lfo2step55",
    9583,           // 4a 6f            "lfo2step56",
    9584,           // 4a 70            "lfo2step57",
    9585,           // 4a 71            "lfo2step58",
    9586,           // 4a 72            "lfo2step59",
    9587,           // 4a 73            "lfo2step60",
    9588,           // 4a 74            "lfo2step61",
    9589,           // 4a 75            "lfo2step62",
    9590,           // 4a 76            "lfo2step63",
    9591,           // 4a 77            "lfo2step64",
    8333,           // 41 d             "lfo3level",
    8070,           // 3f 6             "lfo3wave",
    8070,           // 3f 6             "lfo3bpmsync",
    8070,           // 3f 6             "lfo3trigsync",
    8070,           // 3f 6             "lfo3smooth",
    8070,           // 3f 6             "lfo3steps",
    8070,           // 3f 6             "lfo3delaysyncoff",
    8070,           // 3f 6             "lfo3fadeinsyncoff",
    8070,           // 3f 6             "lfo3delaysyncon",
    8070,           // 3f 6             "lfo3fadeinsyncon",
    8070,           // 3f 6             "lfo3oneshot",
    8114,           // 3f 32            "lfo3phase",
    8327,           // 41 7             "lfo3ratesyncoff",
    8583,           // 43 7             "lfo3ratesyncon",
    7456,           // 3a 20            "lfo3step1",
    7457,           // 3a 21            "lfo3step2",
    7458,           // 3a 22            "lfo3step3",
    7459,           // 3a 23            "lfo3step4",
    7460,           // 3a 24            "lfo3step5",
    7461,           // 3a 25            "lfo3step6",
    7462,           // 3a 26            "lfo3step7",
    7463,           // 3a 27            "lfo3step8",
    9600,           // 4b 0             "lfo3step9",
    9601,           // 4b 1             "lfo3step10",
    9602,           // 4b 2             "lfo3step11",
    9603,           // 4b 3             "lfo3step12",
    9604,           // 4b 4             "lfo3step13",
    9605,           // 4b 5             "lfo3step14",
    9606,           // 4b 6             "lfo3step15",
    9607,           // 4b 7             "lfo3step16",
    9608,           // 4b 8             "lfo3step17",
    9609,           // 4b 9             "lfo3step18",
    9610,           // 4b a             "lfo3step19",
    9611,           // 4b b             "lfo3step20",
    9612,           // 4b c             "lfo3step21",
    9613,           // 4b d             "lfo3step22",
    9614,           // 4b e             "lfo3step23",
    9615,           // 4b f             "lfo3step24",
    9616,           // 4b 10            "lfo3step25",
    9617,           // 4b 11            "lfo3step26",
    9618,           // 4b 12            "lfo3step27",
    9619,           // 4b 13            "lfo3step28",
    9620,           // 4b 14            "lfo3step29",
    9621,           // 4b 15            "lfo3step30",
    9622,           // 4b 16            "lfo3step31",
    9623,           // 4b 17            "lfo3step32",
    9624,           // 4b 18            "lfo3step33",
    9625,           // 4b 19            "lfo3step34",
    9626,           // 4b 1a            "lfo3step35",
    9627,           // 4b 1b            "lfo3step36",
    9628,           // 4b 1c            "lfo3step37",
    9629,           // 4b 1d            "lfo3step38",
    9630,           // 4b 1e            "lfo3step39",
    9631,           // 4b 1f            "lfo3step40",
    9632,           // 4b 20            "lfo3step41",
    9633,           // 4b 21            "lfo3step42",
    9634,           // 4b 22            "lfo3step43",
    9635,           // 4b 23            "lfo3step44",
    9636,           // 4b 24            "lfo3step45",
    9637,           // 4b 25            "lfo3step46",
    9638,           // 4b 26            "lfo3step47",
    9639,           // 4b 27            "lfo3step48",
    9640,           // 4b 28            "lfo3step49",
    9641,           // 4b 29            "lfo3step50",
    9642,           // 4b 2a            "lfo3step51",
    9643,           // 4b 2b            "lfo3step52",
    9644,           // 4b 2c            "lfo3step53",
    9645,           // 4b 2d            "lfo3step54",
    9646,           // 4b 2e            "lfo3step55",
    9647,           // 4b 2f            "lfo3step56",
    9648,           // 4b 30            "lfo3step57",
    9649,           // 4b 31            "lfo3step58",
    9650,           // 4b 32            "lfo3step59",
    9651,           // 4b 33            "lfo3step60",
    9652,           // 4b 34            "lfo3step61",
    9653,           // 4b 35            "lfo3step62",
    9654,           // 4b 36            "lfo3step63",
    9655,           // 4b 37            "lfo3step64",
    8334,           // 41 e             "lfo4level",
    8071,           // 3f 7             "lfo4wave",
    8071,           // 3f 7             "lfo4bpmsync",
    8071,           // 3f 7             "lfo4trigsync",
    8071,           // 3f 7             "lfo4smooth",
    8071,           // 3f 7             "lfo4steps",
    8071,           // 3f 7             "lfo4delaysyncoff",
    8071,           // 3f 7             "lfo4fadeinsyncoff",
    8071,           // 3f 7             "lfo4delaysyncon",
    8071,           // 3f 7             "lfo4fadeinsyncon",
    8071,           // 3f 7             "lfo4oneshot",
    8115,           // 3f 33            "lfo4phase",
    8328,           // 41 8             "lfo4ratesyncoff",
    8584,           // 43 8             "lfo4ratesyncon",
    7464,           // 3a 28            "lfo4step1",
    7465,           // 3a 29            "lfo4step2",
    7466,           // 3a 2a            "lfo4step3",
    7467,           // 3a 2b            "lfo4step4",
    7468,           // 3a 2c            "lfo4step5",
    7469,           // 3a 2d            "lfo4step6",
    7470,           // 3a 2e            "lfo4step7",
    7471,           // 3a 2f            "lfo4step8",
    9664,           // 4b 40            "lfo4step9",
    9665,           // 4b 41            "lfo4step10",
    9666,           // 4b 42            "lfo4step11",
    9667,           // 4b 43            "lfo4step12",
    9668,           // 4b 44            "lfo4step13",
    9669,           // 4b 45            "lfo4step14",
    9670,           // 4b 46            "lfo4step15",
    9671,           // 4b 47            "lfo4step16",
    9672,           // 4b 48            "lfo4step17",
    9673,           // 4b 49            "lfo4step18",
    9674,           // 4b 4a            "lfo4step19",
    9675,           // 4b 4b            "lfo4step20",
    9676,           // 4b 4c            "lfo4step21",
    9677,           // 4b 4d            "lfo4step22",
    9678,           // 4b 4e            "lfo4step23",
    9679,           // 4b 4f            "lfo4step24",
    9680,           // 4b 50            "lfo4step25",
    9681,           // 4b 51            "lfo4step26",
    9682,           // 4b 52            "lfo4step27",
    9683,           // 4b 53            "lfo4step28",
    9684,           // 4b 54            "lfo4step29",
    9685,           // 4b 55            "lfo4step30",
    9686,           // 4b 56            "lfo4step31",
    9687,           // 4b 57            "lfo4step32",
    9688,           // 4b 58            "lfo4step33",
    9689,           // 4b 59            "lfo4step34",
    9690,           // 4b 5a            "lfo4step35",
    9691,           // 4b 5b            "lfo4step36",
    9692,           // 4b 5c            "lfo4step37",
    9693,           // 4b 5d            "lfo4step38",
    9694,           // 4b 5e            "lfo4step39",
    9695,           // 4b 5f            "lfo4step40",
    9696,           // 4b 60            "lfo4step41",
    9697,           // 4b 61            "lfo4step42",
    9698,           // 4b 62            "lfo4step43",
    9699,           // 4b 63            "lfo4step44",
    9700,           // 4b 64            "lfo4step45",
    9701,           // 4b 65            "lfo4step46",
    9702,           // 4b 66            "lfo4step47",
    9703,           // 4b 67            "lfo4step48",
    9704,           // 4b 68            "lfo4step49",
    9705,           // 4b 69            "lfo4step50",
    9706,           // 4b 6a            "lfo4step51",
    9707,           // 4b 6b            "lfo4step52",
    9708,           // 4b 6c            "lfo4step53",
    9709,           // 4b 6d            "lfo4step54",
    9710,           // 4b 6e            "lfo4step55",
    9711,           // 4b 6f            "lfo4step56",
    9712,           // 4b 70            "lfo4step57",
    9713,           // 4b 71            "lfo4step58",
    9714,           // 4b 72            "lfo4step59",
    9715,           // 4b 73            "lfo4step60",
    9716,           // 4b 74            "lfo4step61",
    9717,           // 4b 75            "lfo4step62",
    9718,           // 4b 76            "lfo4step63",
    9719,           // 4b 77            "lfo4step64",
    8335,           // 41 f             "lfo5level",
    8072,           // 3f 8             "lfo5wave",
    8072,           // 3f 8             "lfo5bpmsync",
    8072,           // 3f 8             "lfo5trigsync",
    8072,           // 3f 8             "lfo5smooth",
    8072,           // 3f 8             "lfo5steps",
    8072,           // 3f 8             "lfo5delaysyncoff",
    8072,           // 3f 8             "lfo5fadeinsyncoff",
    8072,           // 3f 8             "lfo5delaysyncon",
    8072,           // 3f 8             "lfo5fadeinsyncon",
    8072,           // 3f 8             "lfo5oneshot",
    8116,           // 3f 34            "lfo5phase",
    8329,           // 41 9             "lfo5ratesyncoff",
    8585,           // 43 9             "lfo5ratesyncon",
    7472,           // 3a 30            "lfo5step1",
    7473,           // 3a 31            "lfo5step2",
    7474,           // 3a 32            "lfo5step3",
    7475,           // 3a 33            "lfo5step4",
    7476,           // 3a 34            "lfo5step5",
    7477,           // 3a 35            "lfo5step6",
    7478,           // 3a 36            "lfo5step7",
    7479,           // 3a 37            "lfo5step8",
    9728,           // 4c 0             "lfo5step9",
    9729,           // 4c 1             "lfo5step10",
    9730,           // 4c 2             "lfo5step11",
    9731,           // 4c 3             "lfo5step12",
    9732,           // 4c 4             "lfo5step13",
    9733,           // 4c 5             "lfo5step14",
    9734,           // 4c 6             "lfo5step15",
    9735,           // 4c 7             "lfo5step16",
    9736,           // 4c 8             "lfo5step17",
    9737,           // 4c 9             "lfo5step18",
    9738,           // 4c a             "lfo5step19",
    9739,           // 4c b             "lfo5step20",
    9740,           // 4c c             "lfo5step21",
    9741,           // 4c d             "lfo5step22",
    9742,           // 4c e             "lfo5step23",
    9743,           // 4c f             "lfo5step24",
    9744,           // 4c 10            "lfo5step25",
    9745,           // 4c 11            "lfo5step26",
    9746,           // 4c 12            "lfo5step27",
    9747,           // 4c 13            "lfo5step28",
    9748,           // 4c 14            "lfo5step29",
    9749,           // 4c 15            "lfo5step30",
    9750,           // 4c 16            "lfo5step31",
    9751,           // 4c 17            "lfo5step32",
    9752,           // 4c 18            "lfo5step33",
    9753,           // 4c 19            "lfo5step34",
    9754,           // 4c 1a            "lfo5step35",
    9755,           // 4c 1b            "lfo5step36",
    9756,           // 4c 1c            "lfo5step37",
    9757,           // 4c 1d            "lfo5step38",
    9758,           // 4c 1e            "lfo5step39",
    9759,           // 4c 1f            "lfo5step40",
    9760,           // 4c 20            "lfo5step41",
    9761,           // 4c 21            "lfo5step42",
    9762,           // 4c 22            "lfo5step43",
    9763,           // 4c 23            "lfo5step44",
    9764,           // 4c 24            "lfo5step45",
    9765,           // 4c 25            "lfo5step46",
    9766,           // 4c 26            "lfo5step47",
    9767,           // 4c 27            "lfo5step48",
    9768,           // 4c 28            "lfo5step49",
    9769,           // 4c 29            "lfo5step50",
    9770,           // 4c 2a            "lfo5step51",
    9771,           // 4c 2b            "lfo5step52",
    9772,           // 4c 2c            "lfo5step53",
    9773,           // 4c 2d            "lfo5step54",
    9774,           // 4c 2e            "lfo5step55",
    9775,           // 4c 2f            "lfo5step56",
    9776,           // 4c 30            "lfo5step57",
    9777,           // 4c 31            "lfo5step58",
    9778,           // 4c 32            "lfo5step59",
    9779,           // 4c 33            "lfo5step60",
    9780,           // 4c 34            "lfo5step61",
    9781,           // 4c 35            "lfo5step62",
    9782,           // 4c 36            "lfo5step63",
    9783,           // 4c 37            "lfo5step64",
    8064,           // 3f 0             "env1delaysyncoff",
    8337,           // 41 11            "env1attacksyncoff",
    8342,           // 41 16            "env1holdsyncoff",
    8347,           // 41 1b            "env1decaysyncoff",
    8352,           // 41 20            "env1sustain",
    8357,           // 41 25            "env1releasesyncoff",
    8064,           // 3f 0             "env1delaysyncon",
    8593,           // 43 11            "env1attacksyncon",
    8603,           // 43 1b            "env1decaysyncon",
    8598,           // 43 16            "env1holdsyncon",
    8613,           // 43 25            "env1releasesyncon",
    8176,           // 3f 70            "env1atkcurve",
    8181,           // 3f 75            "env1deccurve",
    8064,           // 3f 0             "env1loop",
    8064,           // 3f 0             "env1legato",
    8064,           // 3f 0             "env1bpmsync",
    8064,           // 3f 0             "env1freerun",
    8064,           // 3f 0             "env1reset",
    8186,           // 3f 7a            "env1relcurve",
    7520,           // 3a 60            "env1trigsrc1",
    7521,           // 3a 61            "env1trigsrc2",
    7522,           // 3a 62            "env1trigsrc3",
    7523,           // 3a 63            "env1trigsrc4",
    8065,           // 3f 1             "env2delaysyncoff",
    8338,           // 41 12            "env2attacksyncoff",
    8343,           // 41 17            "env2holdsyncoff",
    8348,           // 41 1c            "env2decaysyncoff",
    8353,           // 41 21            "env2sustain",
    8358,           // 41 26            "env2releasesyncoff",
    8065,           // 3f 1             "env2delaysyncon",
    8594,           // 43 12            "env2attacksyncon",
    8604,           // 43 1c            "env2decaysyncon",
    8599,           // 43 17            "env2holdsyncon",
    8614,           // 43 26            "env2releasesyncon",
    8177,           // 3f 71            "env2atkcurve",
    8182,           // 3f 76            "env2deccurve",
    8065,           // 3f 1             "env2loop",
    8065,           // 3f 1             "env2legato",
    8065,           // 3f 1             "env2bpmsync",
    8065,           // 3f 1             "env2freerun",
    8065,           // 3f 1             "env2reset",
    8187,           // 3f 7b            "env2relcurve",
    7524,           // 3a 64            "env2trigsrc1",
    7525,           // 3a 65            "env2trigsrc2",
    7526,           // 3a 66            "env2trigsrc3",
    7527,           // 3a 67            "env2trigsrc4",
    8066,           // 3f 2             "env3delaysyncoff",
    8339,           // 41 13            "env3attacksyncoff",
    8344,           // 41 18            "env3holdsyncoff",
    8349,           // 41 1d            "env3decaysyncoff",
    8354,           // 41 22            "env3sustain",
    8359,           // 41 27            "env3releasesyncoff",
    8066,           // 3f 2             "env3delaysyncon",
    8595,           // 43 13            "env3attacksyncon",
    8605,           // 43 1d            "env3decaysyncon",
    8600,           // 43 18            "env3holdsyncon",
    8615,           // 43 27            "env3releasesyncon",
    8178,           // 3f 72            "env3atkcurve",
    8183,           // 3f 77            "env3deccurve",
    8066,           // 3f 2             "env3loop",
    8066,           // 3f 2             "env3legato",
    8066,           // 3f 2             "env3bpmsync",
    8066,           // 3f 2             "env3freerun",
    8066,           // 3f 2             "env3reset",
    8188,           // 3f 7c            "env3relcurve",
    7528,           // 3a 68            "env3trigsrc1",
    7529,           // 3a 69            "env3trigsrc2",
    7530,           // 3a 6a            "env3trigsrc3",
    7531,           // 3a 6b            "env3trigsrc4",
    8067,           // 3f 3             "env4delaysyncoff",
    8340,           // 41 14            "env4attacksyncoff",
    8345,           // 41 19            "env4holdsyncoff",
    8350,           // 41 1e            "env4decaysyncoff",
    8355,           // 41 23            "env4sustain",
    8360,           // 41 28            "env4releasesyncoff",
    8067,           // 3f 3             "env4delaysyncon",
    8596,           // 43 14            "env4attacksyncon",
    8606,           // 43 1e            "env4decaysyncon",
    8601,           // 43 19            "env4holdsyncon",
    8616,           // 43 28            "env4releasesyncon",
    8179,           // 3f 73            "env4atkcurve",
    8184,           // 3f 78            "env4deccurve",
    8067,           // 3f 3             "env4loop",
    8067,           // 3f 3             "env4legato",
    8067,           // 3f 3             "env4bpmsync",
    8067,           // 3f 3             "env4freerun",
    8067,           // 3f 3             "env4reset",
    8189,           // 3f 7d            "env4relcurve",
    7532,           // 3a 6c            "env4trigsrc1",
    7533,           // 3a 6d            "env4trigsrc2",
    7534,           // 3a 6e            "env4trigsrc3",
    7535,           // 3a 6f            "env4trigsrc4",
    8074,           // 3f 4             "env5delaysyncoff",
    8341,           // 41 15            "env5attacksyncoff",
    8346,           // 41 1a            "env5holdsyncoff",
    8351,           // 41 1f            "env5decaysyncoff",
    8356,           // 41 24            "env5sustain",
    8361,           // 41 29            "env5releasesyncoff",
    8074,           // 3f a             "env5delaysyncon",
    8597,           // 43 15            "env5attacksyncon",
    8607,           // 43 1f            "env5decaysyncon",
    8602,           // 43 1a            "env5holdsyncon",
    8617,           // 43 29            "env5releasesyncon",
    8180,           // 3f 74            "env5atkcurve",
    8185,           // 3f 79            "env5deccurve",
    8074,           // 3f a             "env5loop",
    8074,           // 3f a             "env5legato",
    8074,           // 3f a             "env5bpmsync",
    8074,           // 3f a             "env5freerun",
    8074,           // 3f a             "env5reset",
    8190,           // 3f 7e            "env5relcurve",
    7536,           // 3a 70            "env5trigsrc1",
    7537,           // 3a 71            "env5trigsrc2",
    7538,           // 3a 72            "env5trigsrc3",
    7539,           // 3a 73            "env5trigsrc4",
    8120,           // 3f 38            "arptempo",
    7299,           // 39 3             "arpenable"
    7299,           // 39 3             "arpdivision",
    7299,           // 39 3             "arpswing",
    7299,           // 39 3             "arpgate",
    7299,           // 39 3             "arpoctmode",
    7299,           // 39 3             "arpoctave",
    7299,           // 39 3             "arpmode",
    7299,           // 39 3             "arplength",
    7299,           // 39 3             "arptaptrig",
    7299,           // 39 3             "arpphrase",
    7299,           // 39 3             "arpratchet",
    7299,           // 39 3             "arpchance",
    7984,           // 3e 30            "macro1target1",
    7985,           // 3e 31            "macro1target2",
    7986,           // 3e 32            "macro1target3",
    7987,           // 3e 33            "macro1target4",
    7988,           // 3e 34            "macro1target5",
    7989,           // 3e 35            "macro1target6",
    7990,           // 3e 36            "macro1target7",
    7991,           // 3e 37            "macro1target8",
    7856,           // 3d 30            "macro1buttonvalue1",
    7857,           // 3d 31            "macro1buttonvalue2",
    7858,           // 3d 32            "macro1buttonvalue3",
    7859,           // 3d 33            "macro1buttonvalue4",
    7860,           // 3d 34            "macro1buttonvalue5",
    7861,           // 3d 35            "macro1buttonvalue6",
    7862,           // 3d 36            "macro1buttonvalue7",
    7863,           // 3d 37            "macro1buttonvalue8",
    6960,           // 36 30            "macro1depth1",
    6961,           // 36 31            "macro1depth2",
    6962,           // 36 32            "macro1depth3",
    6963,           // 36 33            "macro1depth4",
    6964,           // 36 34            "macro1depth5",
    6965,           // 36 35            "macro1depth6",
    6966,           // 36 36            "macro1depth7",
    6967,           // 36 37            "macro1depth8",
    7992,           // 3e 38            "macro2target1",
    7993,           // 3e 39            "macro2target2",
    7994,           // 3e 3a            "macro2target3",
    7995,           // 3e 3b            "macro2target4",
    7996,           // 3e 3c            "macro2target5",
    7997,           // 3e 3d            "macro2target6",
    7998,           // 3e 3e            "macro2target7",
    7999,           // 3e 3f            "macro2target8",
    7864,           // 3d 38            "macro2buttonvalue1",
    7865,           // 3d 39            "macro2buttonvalue2",
    7866,           // 3d 3a            "macro2buttonvalue3",
    7867,           // 3d 3b            "macro2buttonvalue4",
    7868,           // 3d 3c            "macro2buttonvalue5",
    7869,           // 3d 3d            "macro2buttonvalue6",
    7870,           // 3d 3e            "macro2buttonvalue7",
    7871,           // 3d 3f            "macro2buttonvalue8",
    6968,           // 36 38            "macro2depth1",
    6969,           // 36 39            "macro2depth2",
    6970,           // 36 3a            "macro2depth3",
    6971,           // 36 3b            "macro2depth4",
    6972,           // 36 3c            "macro2depth5",
    6973,           // 36 3d            "macro2depth6",
    6974,           // 36 3e            "macro2depth7",
    6975,           // 36 3f            "macro2depth8",
    8000,           // 3e 40            "macro3target1",
    8001,           // 3e 41            "macro3target2",
    8002,           // 3e 42            "macro3target3",
    8003,           // 3e 43            "macro3target4",
    8004,           // 3e 44            "macro3target5",
    8005,           // 3e 45            "macro3target6",
    8006,           // 3e 46            "macro3target7",
    8007,           // 3e 47            "macro3target8",
    7872,           // 3d 40            "macro3buttonvalue1",
    7873,           // 3d 41            "macro3buttonvalue2",
    7874,           // 3d 42            "macro3buttonvalue3",
    7875,           // 3d 43            "macro3buttonvalue4",
    7876,           // 3d 44            "macro3buttonvalue5",
    7877,           // 3d 45            "macro3buttonvalue6",
    7878,           // 3d 46            "macro3buttonvalue7",
    7879,           // 3d 47            "macro3buttonvalue8",
    6976,           // 36 40            "macro3depth1",
    6977,           // 36 41            "macro3depth2",
    6978,           // 36 42            "macro3depth3",
    6979,           // 36 43            "macro3depth4",
    6980,           // 36 44            "macro3depth5",
    6981,           // 36 45            "macro3depth6",
    6982,           // 36 46            "macro3depth7",
    6983,           // 36 47            "macro3depth8",
    8008,           // 3e 48            "macro4target1",
    8009,           // 3e 49            "macro4target2",
    8010,           // 3e 4a            "macro4target3",
    8011,           // 3e 4b            "macro4target4",
    8012,           // 3e 4c            "macro4target5",
    8013,           // 3e 4d            "macro4target6",
    8014,           // 3e 4e            "macro4target7",
    8015,           // 3e 4f            "macro4target8",
    7880,           // 3d 48            "macro4buttonvalue1",
    7881,           // 3d 49            "macro4buttonvalue2",
    7882,           // 3d 4a            "macro4buttonvalue3",
    7883,           // 3d 4b            "macro4buttonvalue4",
    7884,           // 3d 4c            "macro4buttonvalue5",
    7885,           // 3d 4d            "macro4buttonvalue6",
    7886,           // 3d 4e            "macro4buttonvalue7",
    7887,           // 3d 4f            "macro4buttonvalue8",
    6984,           // 36 48            "macro4depth1",
    6985,           // 36 49            "macro4depth2",
    6986,           // 36 4a            "macro4depth3",
    6987,           // 36 4b            "macro4depth4",
    6988,           // 36 4c            "macro4depth5",
    6989,           // 36 4d            "macro4depth6",
    6990,           // 36 4e            "macro4depth7",
    6991,           // 36 4f            "macro4depth8",
    8016,           // 3e 50            "macro5target1",
    8017,           // 3e 51            "macro5target2",
    8018,           // 3e 52            "macro5target3",
    8019,           // 3e 53            "macro5target4",
    8020,           // 3e 54            "macro5target5",
    8021,           // 3e 55            "macro5target6",
    8022,           // 3e 56            "macro5target7",
    8023,           // 3e 57            "macro5target8",
    7888,           // 3d 50            "macro5buttonvalue1",
    7889,           // 3d 51            "macro5buttonvalue2",
    7890,           // 3d 52            "macro5buttonvalue3",
    7891,           // 3d 53            "macro5buttonvalue4",
    7892,           // 3d 54            "macro5buttonvalue5",
    7893,           // 3d 55            "macro5buttonvalue6",
    7894,           // 3d 56            "macro5buttonvalue7",
    7895,           // 3d 57            "macro5buttonvalue8",
    6992,           // 36 50            "macro5depth1",
    6993,           // 36 51            "macro5depth2",
    6994,           // 36 52            "macro5depth3",
    6995,           // 36 53            "macro5depth4",
    6996,           // 36 54            "macro5depth5",
    6997,           // 36 55            "macro5depth6",
    6998,           // 36 56            "macro5depth7",
    6999,           // 36 57            "macro5depth8",
    8024,           // 3e 58            "macro6target1",
    8025,           // 3e 59            "macro6target2",
    8026,           // 3e 60            "macro6target3",
    8027,           // 3e 61            "macro6target4",
    8028,           // 3e 62            "macro6target5",
    8029,           // 3e 63            "macro6target6",
    8030,           // 3e 64            "macro6target7",
    8031,           // 3e 65            "macro6target8",
    7896,           // 3d 58            "macro66buttonvalue1",
    7897,           // 3d 59            "macro6buttonvalue2",
    7898,           // 3d 5a            "macro6buttonvalue3",
    7899,           // 3d 5b            "macro6buttonvalue4",
    7900,           // 3d 5c            "macro6buttonvalue5",
    7901,           // 3d 5d            "macro6buttonvalue6",
    7902,           // 3d 5e            "macro6buttonvalue7",
    7903,           // 3d 5f            "macro6buttonvalue8",
    7000,           // 36 58            "macro6depth1",
    7001,           // 36 59            "macro6depth2",
    7002,           // 36 5a            "macro6depth3",
    7003,           // 36 5b            "macro6depth4",
    7004,           // 36 5c            "macro6depth5",
    7005,           // 36 5d            "macro6depth6",
    7006,           // 36 5e            "macro6depth7",
    7007,           // 36 5f            "macro6depth8",
    8032,           // 3e 60            "macro7target1",
    8033,           // 3e 61            "macro7target2",
    8034,           // 3e 62            "macro7target3",
    8035,           // 3e 63            "macro7target4",
    8036,           // 3e 64            "macro7target5",
    8037,           // 3e 65            "macro7target6",
    8038,           // 3e 66            "macro7target7",
    8039,           // 3e 67            "macro7target8",
    7904,           // 3d 60            "macro7buttonvalue1",
    7905,           // 3d 61            "macro7buttonvalue2",
    7906,           // 3d 62            "macro7buttonvalue3",
    7907,           // 3d 63            "macro7buttonvalue4",
    7908,           // 3d 64            "macro7buttonvalue5",
    7909,           // 3d 65            "macro7buttonvalue6",
    7910,           // 3d 66            "macro7buttonvalue7",
    7911,           // 3d 67            "macro7buttonvalue8",
    7008,           // 36 60            "macro7depth1",
    7009,           // 36 61            "macro7depth2",
    7010,           // 36 62            "macro7depth3",
    7011,           // 36 63            "macro7depth4",
    7012,           // 36 64            "macro7depth5",
    7013,           // 36 65            "macro7depth6",
    7014,           // 36 66            "macro7depth7",
    7015,           // 36 67            "macro7depth8",
    8040,           // 3e 68            "macro8target1",
    8041,           // 3e 69            "macro8target2",
    8042,           // 3e 6a            "macro8target3",
    8043,           // 3e 6b            "macro8target4",
    8044,           // 3e 6c            "macro8target5",
    8045,           // 3e 6d            "macro8target6",
    8046,           // 3e 6e            "macro8target7",
    8047,           // 3e 6f            "macro8target8",
    7912,           // 3d 68            "macro8buttonvalue1",
    7913,           // 3d 69            "macro8buttonvalue2",
    7914,           // 3d 6a            "macro8buttonvalue3",
    7915,           // 3d 6b            "macro8buttonvalue4",
    7916,           // 3d 6c            "macro8buttonvalue5",
    7917,           // 3d 6d            "macro8buttonvalue6",
    7918,           // 3d 6e            "macro8buttonvalue7",
    7919,           // 3d 6f            "macro8buttonvalue8",
    7016,           // 36 68            "macro8depth1",
    7017,           // 36 69            "macro8depth2",
    7018,           // 36 6a            "macro8depth3",
    7019,           // 36 6b            "macro8depth4",
    7020,           // 36 6c            "macro8depth5",
    7021,           // 36 6d            "macro8depth6",
    7022,           // 36 6e            "macro8depth7",
    7023,           // 36 6f            "macro8depth8",
    8152,           // 3f 58            "macro1panelvalue",
    8153,           // 3f 59            "macro2panelvalue",
    8154,           // 3f 5a            "macro3panelvalue",
    8155,           // 3f 5b            "macro4panelvalue",
    8156,           // 3f 5c            "macro5panelvalue",
    8157,           // 3f 5d            "macro6panelvalue",
    8158,           // 3f 5e            "macro7panelvalue",
    8159,           // 3f 5f            "macro8panelvalue",
    // Macro Panel Button Triggers are not included here since they are,
    // for some unknown reason, per-Macro item, per slot.  See NRPN spreadsheet.
    7936,           // 3e 0             "modmatrix1modsource",
    7937,           // 3e 1             "modmatrix2modsource",
    7938,           // 3e 2             "modmatrix3modsource",
    7939,           // 3e 3             "modmatrix4modsource",
    7940,           // 3e 4             "modmatrix5modsource",
    7941,           // 3e 5             "modmatrix6modsource",
    7942,           // 3e 6             "modmatrix7modsource",
    7943,           // 3e 7             "modmatrix8modsource",
    7944,           // 3e 8             "modmatrix9modsource",
    7945,           // 3e 9             "modmatrix10modsource",
    7946,           // 3e a             "modmatrix11modsource",
    7947,           // 3e b             "modmatrix12modsource",
    7948,           // 3e c             "modmatrix13modsource",
    7949,           // 3e d             "modmatrix14modsource",
    7950,           // 3e e             "modmatrix15modsource",
    7951,           // 3e f             "modmatrix16modsource",
    7952,           // 3e 10            "modmatrix17modsource",
    7953,           // 3e 11            "modmatrix18modsource",
    7954,           // 3e 12            "modmatrix19modsource",
    7955,           // 3e 13            "modmatrix20modsource",
    7956,           // 3e 14            "modmatrix21modsource",
    7957,           // 3e 15            "modmatrix22modsource",
    7958,           // 3e 16            "modmatrix23modsource",
    7959,           // 3e 17            "modmatrix24modsource",
    7960,           // 3e 18            "modmatrix25modsource",
    7961,           // 3e 19            "modmatrix26modsource",
    7962,           // 3e 1a            "modmatrix27modsource",
    7963,           // 3e 1b            "modmatrix28modsource",
    7964,           // 3e 1c            "modmatrix29modsource",
    7965,           // 3e 1d            "modmatrix30modsource",
    7966,           // 3e 1e            "modmatrix31modsource",
    7967,           // 3e 1f            "modmatrix32modsource",
    7936,           // 3e 0             "modmatrix1modtarget",
    7937,           // 3e 1             "modmatrix2modtarget",
    7938,           // 3e 2             "modmatrix3modtarget",
    7939,           // 3e 3             "modmatrix4modtarget",
    7940,           // 3e 4             "modmatrix5modtarget",
    7941,           // 3e 5             "modmatrix6modtarget",
    7942,           // 3e 6             "modmatrix7modtarget",
    7943,           // 3e 7             "modmatrix8modtarget",
    7944,           // 3e 8             "modmatrix9modtarget",
    7945,           // 3e 9             "modmatrix10modtarget",
    7946,           // 3e a             "modmatrix11modtarget",
    7947,           // 3e b             "modmatrix12modtarget",
    7948,           // 3e c             "modmatrix13modtarget",
    7949,           // 3e d             "modmatrix14modtarget",
    7950,           // 3e e             "modmatrix15modtarget",
    7951,           // 3e f             "modmatrix16modtarget",
    7952,           // 3e 10            "modmatrix17modtarget",
    7953,           // 3e 11            "modmatrix18modtarget",
    7954,           // 3e 12            "modmatrix19modtarget",
    7955,           // 3e 13            "modmatrix20modtarget",
    7956,           // 3e 14            "modmatrix21modtarget",
    7957,           // 3e 15            "modmatrix22modtarget",
    7958,           // 3e 16            "modmatrix23modtarget",
    7959,           // 3e 17            "modmatrix24modtarget",
    7960,           // 3e 18            "modmatrix25modtarget",
    7961,           // 3e 19            "modmatrix26modtarget",
    7962,           // 3e 1a            "modmatrix27modtarget",
    7963,           // 3e 1b            "modmatrix28modtarget",
    7964,           // 3e 1c            "modmatrix29modtarget",
    7965,           // 3e 1d            "modmatrix30modtarget",
    7966,           // 3e 1e            "modmatrix31modtarget",
    7967,           // 3e 1f            "modmatrix32modtarget",
    8384,           // 41 40            "modmatrix1depth",
    8385,           // 41 41            "modmatrix2depth",
    8386,           // 41 42            "modmatrix3depth",
    8387,           // 41 43            "modmatrix4depth",
    8388,           // 41 44            "modmatrix5depth",
    8389,           // 41 45            "modmatrix6depth",
    8390,           // 41 46            "modmatrix7depth",
    8391,           // 41 47            "modmatrix8depth",
    8392,           // 41 48            "modmatrix9depth",
    8393,           // 41 49            "modmatrix10depth",
    8394,           // 41 4a            "modmatrix11depth",
    8395,           // 41 4b            "modmatrix12depth",
    8396,           // 41 4c            "modmatrix13depth",
    8397,           // 41 4d            "modmatrix14depth",
    8398,           // 41 4e            "modmatrix15depth",
    8399,           // 41 4f            "modmatrix16depth",
    8400,           // 41 50            "modmatrix17depth",
    8401,           // 41 51            "modmatrix18depth",
    8402,           // 41 52            "modmatrix19depth",
    8403,           // 41 53            "modmatrix20depth",
    8404,           // 41 54            "modmatrix21depth",
    8405,           // 41 55            "modmatrix22depth",
    8406,           // 41 56            "modmatrix23depth",
    8407,           // 41 57            "modmatrix24depth",
    8408,           // 41 58            "modmatrix25depth",
    8409,           // 41 59            "modmatrix26depth",
    8410,           // 41 5a            "modmatrix27depth",
    8411,           // 41 5b            "modmatrix28depth",
    8412,           // 41 5c            "modmatrix29depth",
    8413,           // 41 5d            "modmatrix30depth",
    8414,           // 41 5e            "modmatrix31depth",
    8415,           // 41 5f            "modmatrix32depth",
    8123,           // 3f 3b            "ribbonmode",
    8123,           // 3f 3b            "ribbonkeyspan",
    8123,           // 3f 3b            "ribbonoctave",
    8123,           // 3f 3b            "ribbonquantize",
    8123,           // 3f 3b            "ribbonmodcontrol",
    8123,           // 3f 3b            "ribbonglide",
    8121,           // 3f 39            "voicedetune",
    8132,           // 3f 44            "voicestereowidth",
    8131,           // 3f 43            "voicevibratoamount",
    8134,           // 3f 46            "voiceanalogfeel",
    8124,           // 3f 3c            "voicedensity",
    8084,           // 3f 14            "voiceglidecurve",
    8082,           // 3f 12            "voiceglide",
    8095,           // 3f 1f            "voiceglidelegto",
    8085,           // 3f 15            "voiceglidetime",
    8136,           // 3f 48            "voicestereomode",
    8083,           // 3f 13            "voicepolyphony",
    8129,           // 3f 41            "voicepitchbend",
    8130,           // 3f 42            "voicevibratoratesyncoff",
    8127,           // 3f 3f            "voicevibratoratesyncon",
    8094,           // 3f 1e            "voicerandomphase",
    8143,           // 3f 4f            "voicewarmmode",
    8137,           // 3f 49            "voicevibratobpmsync",
    8117,           // 3f 35            "voicesnap",
    
    /// New 2.0.0 Parameters
    14464,           // 71 00                    "voicesustain",
    8128,            // 3f 40                    "osc1bitreduction",                  
    8128,            // 3f 40                    "osc2bitreduction",                  
    8128,            // 3f 40                    "osc3bitreduction",                  
    14465,           // 71 01                   "voicemodulation1",                  
    14466,           // 71 02                       "voicemodulation2",                  
    14467,           // 71 03                       "voicemodulation3",                  
    14468,           // 71 04                       "voicemodulation4",                  
    14469,           // 71 05                       "voicemodulation5",                  
    14470,           // 71 05                       "voicemodulation6",                  
    14471,           // 71 06                       "voicemodulation7",                  
    14472,           // 71 06                       "voicemodulation8",                  
    14481,           // 71 11                   "env1quantize",                  
    14482,           // 71 12                   "env2quantize",                  
    14483,           // 71 13                   "env3quantize",                  
    14484,           // 71 14                   "env4quantize",                  
    14485,           // 71 15                   "env5quantize",                  
    14486,           // 71 16                   "lfo1quantize",                  
    14487,           // 71 17                   "lfo2quantize",                  
    14488,           // 71 18                   "lfo3quantize",                  
    14489,           // 71 19                   "lfo4quantize",                  
    14490,           // 71 1a                   "lfo5quantize",                  
    };
        
        
        
        
    // This is a list of each parameter that corresponds to a CC, for all 128 CC values.
    // We'll use it to maybe parse incoming CC values but not to emit them, so there's 
    // no need for a corresponding hashmap.
    // NOTE: This is unused in the code, it's just here for posterity
    public static final String[] CC = new String[]
    {
    "--",                           // 0x00         (Bank Select MSB)
    "--",                           // 0x01         (Mod Wheel)
    "--",                           // 0x02         (Breath Controller)
    "mixernoisevol",                // 0x03
    "--",                           // 0x04         (Foot Pedal -- often Expression Pedal)
    "voiceglidetime",               // 0x05
    "--",                           // 0x06         (Data Entry MSB, for RPN/NRPN)
    "--",                           // 0x07         (Volume)
    "mixernoisepan",                // 0x08
    "mixerringmodvol",              // 0x09
    "mixerringmodpan",              // 0x0A
    "--",                           // 0x0B         (Expression -- sometimes Expression Pedal)
    "prefxparam1",                  // 0x0C
    "prefxparam2",                  // 0x0D
    "delayfeedback",                // 0x0E
    "delaytime",                    // 0x0F
    "macro1panelvalue",             // 0x10
    "macro2panelvalue",             // 0x11
    "macro3panelvalue",             // 0x12
    "macro4panelvalue",             // 0x13
    "macro5panelvalue",             // 0x14
    "macro6panelvalue",             // 0x15
    "macro7panelvalue",             // 0x16
    "macro8panelvalue",             // 0x17
    "osc1wavscan",                  // 0x18
    "env4attacksyncoff",            // 0x19
    "osc2wavscan",                  // 0x1A
    "env4decaysyncoff",             // 0x1B
    "lfo2level",                    // 0x1C
    "mutant1ratio",                 // 0x1D
    "mutant1depth",                 // 0x1E
    "mutant1wet",                   // 0x1F
    "--",                           // 0x20         (Bank Select LSB)
    "mutant2ratio",                 // 0x21
    "mutant2depth",                 // 0x22
    "mutant2wet",                   // 0x23
    "mutant3ratio",                 // 0x24
    "mutant3depth",                 // 0x25
    "--",                           // 0x26         (Data Entry LSB, for RPN/NRPN)
    "mutant3wet",                   // 0x27
    "mutant4ratio",                 // 0x28
    "mutant4depth",                 // 0x29
    "mutant4wet",                   // 0x2A
    "ringmoddepth",                 // 0x2B
    "mixerosc1vol",                 // 0x2C
    "mixerosc1pan",                 // 0x2D
    "mixerosc2vol",                 // 0x2E
    "mixerosc2pan",                 // 0x2F
    "mixerosc3vol",                 // 0x30
    "mixerosc3pan",                 // 0x31
    "filter1drive",                 // 0x32
    "filter1keytrack",              // 0x33
    "filter1lfo1amount",            // 0x34
    "filter1velenv",                // 0x35
    "filter1env1amount",            // 0x36
    "filter2cutoff",                // 0x37
    "filter2resonance",             // 0x38
    "filter2morph",                 // 0x39
    "filter2keytrack",              // 0x3A
    "filter2lfo1amount",            // 0x3B
    "filter2velenv",                // 0x3C
    "filter2env1amount",            // 0x3D
    "amplfo2amount",                // 0x3E
    "delaywettone",                 // 0x3F
    "--",                           // 0x40         (Sustain Pedal)
    "reverbtime",                   // 0x41
    "voiceglide",                   // 0x42
    "reverbtone",                   // 0x43
    "postfxparam1",                 // 0x44
    "postfxparam2",                 // 0x45
    "lfo1level",                    // 0x46
    "filter1resonance",             // 0x47
    "lfo1ratesyncoff",              // 0x48
    "lfo2ratesyncoff",              // 0x49
    "filter1cutoff",                // 0x4A
    "lfo3level",                    // 0x4B
    "lfo3ratesyncoff",              // 0x4C
    "lfo4level",                    // 0x4D
    "lfo4ratesyncoff",              // 0x4E
    "lfo5level",                    // 0x4F
    "lfo5ratesyncoff",              // 0x50
    "env1attacksyncoff",            // 0x51
    "env1decaysyncoff",             // 0x52
    "env1sustain",                  // 0x53
    "env1releasesyncoff",           // 0x54
    "env2attacksyncoff",            // 0x55
    "env2decaysyncoff",             // 0x56
    "env2sustain",                  // 0x57
    "env2releasesyncoff",           // 0x58
    "env3attacksyncoff",            // 0x59
    "env3decaysyncoff",             // 0x5A
    "reverbwet",                    // 0x5B
    "delaywet",                     // 0x5C
    "prefxwet",                     // 0x5D
    "postfxwet",                    // 0x5E
    "voicedetune",                  // 0x5F
    "env3sustain",                  // 0x60
    "env3releasesyncoff",           // 0x61
    "--",                           // 0x62         (NRPN Parameter LSB)
    "--",                           // 0x63         (NRPN Parameter MSB)
    "--",                           // 0x64         (RPN Parameter LSB)
    "--",                           // 0x65         (RPN Parameter MSB)
    "env5attacksyncoff",            // 0x66
    "env5decaysyncoff",             // 0x67
    "env5sustain",                  // 0x68
    "env5releasesyncoff",           // 0x69
    "arpdivision",                  // 0x6A
    "arpgate",                      // 0x6B
    "arpmode",                      // 0x6C
    "arpratchet",                   // 0x6D
    "arpchance",                    // 0x6E
    "osc1cent",                     // 0x6F
    "osc2cent",                     // 0x70
    "osc3cent",                     // 0x71
    "mixerosc3filterratio",         // 0x72
    "mixernoisefilterratio",        // 0x73
    "mixerringmodfilterratio",      // 0x74
    "voicestereowidth",             // 0x75
    "mixerosc1filterratio",         // 0x76
    "mixerosc2filterratio",         // 0x77
    "arpoctave",                    // 0x78         (This is normally All Sounds Off -- unfortunate that this is not implemented)
    "--",                           // 0x79         (Reset All Controllers -- kinda useless)
    "arplength",                    // 0x7A
    "--",                           // 0x7B         (All Notes Off)
    "env4releasesyncoff",           // 0x7C
    "env4sustain",                  // 0x7D
    "--",                           // 0x7E         (Mono Mode)
    "--",                           // 0x7F         (Poly Mode)
    };
    
    public boolean testVerify(Synth synth2, String key, Object obj1, Object obj2)
        {
        // Lots of parameters are stuffed into the same space and so will fail verification.
        
        return (key.endsWith("oscsync") ||      
            key.endsWith("fmlin") ||
            key.endsWith("param1") ||
            key.endsWith("param2") ||
            key.endsWith("param3") ||
            key.endsWith("param4") ||
            key.endsWith("param5") ||
            key.endsWith("loop") ||
            key.endsWith("syncon") ||
            key.endsWith("syncoff") ||
            key.endsWith("name"));
        }

    }
    
    
 
