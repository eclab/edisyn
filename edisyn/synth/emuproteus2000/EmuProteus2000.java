/***
    Copyright 2022 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.emuproteus2000;

import edisyn.*;
import edisyn.gui.*;
import edisyn.util.*;
import edisyn.test.*;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.border.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javax.sound.midi.*;
import javax.swing.table.*;

/**
   A patch editor for the E-Mu Proteus 2000 Family.  This family includes the Proteus 2000 and 1000, Audity 2000 (firmware 2.0 or 
   higher), Virtuoso 200, Xtreme Lead-1, Mo'Phatt, B-3, Planet Earth, Orbit-3, Custom, XL-1 Turbo, Turbo Phatt, and Vintage Pro modules.  
   It also includes the Proteus 2500, XL-7, MP-7, and PX-7 "Command Stations", as well as the PK-6, XK-6, MK-6, and Halo keyboards.  
   These machines all are essentially compatible with one another, and vary in the ROMs installed in their respective SIMM slots.  
   Edisyn knows about many of these ROMS: the Audity, Composer, Protozoa, Definitive B-3, X-Lead Vols. 1 and 2, Sounds of the ZR,
   World Expedition, Orchestral Sessions Vols. 1 and 2, Pure Phatt 1 and 2, Ensoniq Project, Proteus Pop Collection, Vintage Collection,
   Protean Drums, Holy Grail Piano, Techno Synth Construction Yard, Peter Siedlaczek Advanced Orchetra, and Beat Garden.  There are four
   ROMS that Edisyn does not have information on: Audity Extreme, QROM (for the Halo keyboard), MROM1 (for the MK-6 Keyboard), and XROM1
   (for the XK-6 Keyboard).  Edisyn also cannot handle custom Flash RAM SIMMs.
   
   <p>The problem with the Proteus 2000 family, which makes it so complex, is that each of these machines can hold up to four ROMs in
   SIMM slots, and that these ROMs store a highly variable number of patch presets, arpeggios, "riffs", and instruments (samples).
   This makes for a nontrivial structuring of ROM / Bank / PatchNumber: and of course Edisyn doesn't know beforehand which ROMs are
   stored in a given machine.  Further complicating matters: E-Mu's fancy-pants sysex format is complex to the point of being convoluted,
   with many sysex messages necessary to establish a single patch dump, plus lots of undocumented but critical features, plus a very 
   large dose of bugs.  Finally many parameters (ROM IDs, modulation matrix sources and destinations, filter types, lfo types) are
   laid out in a non-linear mapping with holes. There are four different categories of filters each with its own range of cutoff
   frequencies.  Did I mention that there are almost 800 parameters?  All this makes for a pretty complex patch editor.  See
   the "rant" at bottom of this file.
        
   @author Sean Luke
*/

public class EmuProteus2000 extends Synth
    {
    public static final String[] ROMS = { "Audity (A2000)", "Composer (P2000/P1000)", "Protozoa", "Definitive B3", "Extreme Lead V1 (XL1/Turbo)",
                                          "Sounds of the ZR", "World Expedition (Earth)", "Orchestral V1 (V2000)", "Orchestral V2 (V2000)",
                                          "Pure Phatt (Mo'Phatt/Turbo)", "Extreme Lead (XL7)", "Pure Phatt 2 (MP7)", "Ensoniq Project",
                                          "Proteus Pop (P2500/PK6)", "Vintage Collection (Pro, Keys)", "Protean Drums (PX7)", "Holy Grail Piano",
                                          "Techno Synth Cons. Yard (Orbit3)", "Siedlaczek", "Beat Garden (Orbit3)" };
    public static final int[] ROM_IDS = { 3, 4, 5, 6, 7, 8, 9, 10, 11, 13, 14, 15, 16, 17, 18, 19, 64, 65, 66, 67 };
    public static final String[] ROMS_AND_USER = { "User", 
                                                   "Audity (A2000)", "Composer (P2000/P1000)", "Protozoa", "Definitive B3", "Extreme Lead V1 (XL1/Turbo)",
                                                   "Sounds of the ZR", "World Expedition (Earth)", "Orchestral V1 (V2000)", "Orchestral V2 (V2000)",
                                                   "Pure Phatt (Mo'Phatt/Turbo)", "Extreme Lead (XL7)", "Pure Phatt 2 (MP7)", "Ensoniq Project",
                                                   "Proteus Pop (P2500/PK6)", "Vintage Collection (Pro, Keys)", "Protean Drums (PX7)", "Holy Grail Piano",
                                                   "Techno Synth Cons. Yard (Orbit3)", "Siedlaczek", "Beat Garden (Orbit3)"  };  // pretty dumb that we have to do this
    public static final String[] ROMS_AND_USER_SHORT = { "User", 
                                                         "Audity", "Composer", "Protozoa", "B3", "XLead",
                                                         "ZR", "World", "Orchestral-1", "Orchestral-2",
                                                         "Phatt", "XLead-2", "Phatt-2", "Ensoniq",
                                                         "Pop", "Vintage", "Drums", "Piano",
                                                         "Techno", "Siedlaczek", "Beat"  };
    public static final int[] ROM_AND_USER_IDS = { 0, 3, 4, 5, 6, 7, 8, 9, 10, 11, 13, 14, 15, 16, 17, 18, 19, 64, 65, 66, 67 };
    public static final int[] NUM_BANKS = { 4, 5, 8, 4, 3, 4, 5, 4, 3, 2, 4, 4, 4, 4, 4, 4, 4, 1, 4, 2, 4 };
    public static final String[] ROMS_AND_EMPTY = { "Empty", 
                                                    "Audity (A2000)", "Composer (P2000/P1000)", "Protozoa", "Definitive B3", "Extreme Lead V1 (XL1/Turbo)",
                                                    "Sounds of the ZR", "World Expedition (Earth)", "Orchestral V1 (V2000)", "Orchestral V2 (V2000)",
                                                    "Pure Phatt (Mo'Phatt/Turbo)", "Extreme Lead (XL7)", "Pure Phatt 2 (MP7)", "Ensoniq Project",
                                                    "Proteus Pop (P2500/PK6)", "Vintage Collection (Pro, Keys)", "Protean Drums (PX7)", "Holy Grail Piano",
                                                    "Techno Synth Cons. Yard (Orbit3)", "Siedlaczek", "Beat Garden (Orbit3)"  };  // even dumber that we have to do this AGAIN
    // This is totally different than the documentation
    public static final String[] TEMPO_OFFSETS = { "Current / 2", "Current", "Current x 2" };   
    public static final String[] KEYBOARD_TUNINGS = { "Equal Temperament", "Just C", "Just C2", "Just C minor", "Just C 3", "Valotti", "19 Tone", "Gamelan", "Werkmeister III", "Kirnberger", "Scarlatti", "Repeating Octave",
                                                      "User 1", "User 2", "User 3", "User 4", "User 5", "User 6", "User 7", "User 8", "User 9", "User 10", "User 11", "User 12" };

    public static final String[] PATCHCORD_SOURCES = { "Off", "Pitch Wheel", "Mod Wheel", "Pressure", "Pedal", "MIDI A", "MIDI B", "Foot Switch 1", "Foot Switch 2", "Flip-Flop Foot Switch 1", "Flip-Flop Foot Switch 2", 
                                                       "Volme Controller (7)", "Pan Controller (10)", "MIDI C", "MIDI D", "MIDI E", "MIDI F", "MIDI G", "MIDI H", "MIDI I", "MIDI J", "MIDI K", "MIDI L", "DC Offset" };
    public static final int[] PATCHCORD_SOURCE_INDICES = { 0, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 32, 33, 34, 35, 36, 37, 40, 41, 42, 43, 160 };

    public static final String[] PATCHCORD_DESTINATIONS = { "Off", "FX A Send 1", "FX A Send 2", "FX A Send 3", "FX A Send 4", "FX B Send 1", "FX B Send 2", "FX B Send 3", "FX B Send 4", "Arpeggiator Rate", "Arpeggiator Extension", "Arpeggiator Velocity", "Arpeggiator Gate", "Arpeggiator Interval", 
                                                            "Beats Velocity Group 1", "Beats Velocity Group 2", "Beats Velocity Group 3", "Beats Velocity Group 4", "Beats Transpose Group 1", "Beats Transpose Group 2", "Beats Transpose Group 3", "Beats Transpose Group 4", 
                                                            "Beats Busy", "Beats Variation", "Preset Lag In", "Preset Lag Amount", "Preset Ramp Rate" };
    public static final int[] PATCHCORD_DESTINATION_INDICES = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 96, 97, 98, 99, 100, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 128, 129, 131 };

    public static final String[] ARP_MODES = { "Up", "Down", "Up/Down", "Forward Assign", "Backward Assign", "Fwd/Bkwd Assign", "Random", "Pattern" };
    public static final String[] ARP_SYNC = { "Key Sync", "Quantized" };
        
    public static final String[] ARP_NOTE_VALUES = { "1/32", "1/16T", "1/32D", "1/16", "1/8T", "1/16D", "1/8", "1/4T", "1/8D", "1/4", "1/2T", "1/4D", "1/2", "1/1T", "1/2D", "1/1", "2/1T", "1/1D", "2/1" };                                                                                                

    public static final String[] ARP_DURATIONS = { "Off", "1/32", "1/16T", "1/32D", "1/16", "1/8T", "1/16D", "1/8", "1/4T", "1/8D", "1/4", "1/2T", "1/4D", "1/2", "1/1T", "1/2D", "1/1", "2/1T", "1/1D", "2/1" };                                                                                           
    public static final String[] ARP_RECYCLES = { "Off", "On", "No Pre-delay"};                                                                                             
    public static final String[] ARP_PATTERN_SPEEDS = { "4x", "2x", "1x", "1/2x", "1/4x" };                                                                                         
    public static final String[] NOTES = new String[] { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };

    public static final String[] FX_A_ALGORITHMS = { "Use Master", "Room 1", "Room 2", "Room 3", "Hall 1", "Hall 2", "Plate", "Delay", "Panning Delay", "Multitap 1", "Multitap Pan", "3 Tap", "3 Tap Pan", "Soft Room", "Warm Room", "Perfect Room", "Tiled Room", "Hard Plate", "Warm Hall", "Spacious Hall", "Bright Hall", "Bright Hall Pan", "Bright Plate", "Basketball Court", "Gymnasium", "Cavern", "Concert 9", "Concert 10 Pan", "Reverse Gate", "Gate 2", "Gate Pan", "Concert 11", "Medium Concert", "Large Concert", "Large Concert Pan", "Canyon", "DelayVerb 1", "DelayVerb 2", "DelayVerb 3", "DelayVerb 4 Pan", "DelayVerb 5 Pan", "DelayVerb 6", "DelayVerb 7", "DelayVerb 8", "DelayVerb 9", };
    public static final String[] FX_B_ALGORITHMS = { "Use Master", "Chorus 1", "Chorus 2", "Chorus 3", "Chorus 4", "Chorus 5", "Doubling ", "Slapback", "Flange 1", "Flange 2", "Flange 3", "Flange 4", "Flange 5", "Flange 6", "Flange 7", "Big Chorus ", "Symphonic ", "Ensemble", "Delay", "Delay Stereo ", "Delay Stereo 2 ", "Panning Delay ", "Delay Chorus ", "Panning Delay Chorus 1", "Panning Delay Chorus 2 ", "Dual Tap 1/3 ", "Dual Tap 1/4 ", "Vibrato ", "Distortion 1", "Distortion 2", "Distorted Flange ", "Distorted Chorus", "Distorted Double", };

    public static final String[] SUBMIXES = { "Main", "Sub 1", "Sub 2" };
    public static final String[] GLIDE_CURVES = { "Linear", "Exp 1", "Exp 2", "Exp 3", "Exp 4", "Exp 5", "Exp 6", "Exp 7", "Exp 8" };
    
    public static final String[] SOLOS = { "Off", "Multiple Trigger", "Melody (Last)", "Melody (Low)", "Melody (High)", "Synth (Last)", "Synth (Low)", "Synth (High)", "Fingered Glide" };
    public static final String[] GROUPS = { "Poly All", "Poly 16 A", "Poly 16 B", "Poly 8 A", "Poly 8 B", "Poly 8 C", "Poly 8 D", "Poly 4 A", "Poly 4 B", "Poly 4 C", "Poly 4 D", "Poly 2 A", "Poly 2 B", "Poly 2 C", "Poly 2 D", "Mono A", "Mono B", "Mono C", "Mono D", "Mono E", "Mono F", "Mono G", "Mono H", "Mono I" };
    
    public static final String[] FILTER_TYPES = { "Off", "4 LPF Classic", "2 LPF Smooth", "6 LPF Steeper", "2 HPF Shallow", "4 HPF Deeper", "2 BPF Band-Pass 1", "4 BPF Band-Pass 2", "6 BPF ContraBand", 
                                                  "6 EQ+ Swept 1 Oct", "6 EQ+ Swept 2>1 Oct", "6 EQ+ Swept 3>1 Oct", "6 PHA PhazeShift1", "6 PHA PhaseShift2", "6 PHA BlissBatz", "6 FLG FlangerLite", "6 VOW Aah-Ay-Eeh", "6 VOW Ooh-To-Aah",
                                                  "12 EQ+ AceOfBass", "12 LPF MegaSweepz", "12 LPF EarlyRizer", "12 LPF Millennium", "12 REZ MeatyGizmo", "12 LPF KlubKassi", "12 LPF BassBox-303", "12 DST FuzziFace", 
                                                  "12 REZ DeadRinger", "12 EQ+ TB-OrNot-TB", "12 VOW Ooh-To-Eee", "12 EQ+ Bolanass", "12 VOW MultiQVox", "12 VOW TalkingHedz", "12 REZ ZoomPeaks",
                                                  "12 EQ+ DJAlkaline", "12 EQ+ BassTracer", "12 EQ+ RogueHertz", "12 EQ- RazorBlades", "12 EQ- RadioCraze", "12 VOW Eeh-To-Aah", "12 VOW UbuOrator", "12 VOW DeepBouche",
                                                  "12 PHA FreakShifta", "12 PHA CruzPusher", "12 FLG AngelHairz", "12 FLG DreamWeava", "12 REZ AcidRavage", "12 REZ BassOMatic", "12 REZ LucifersQ", "12 REZ ToothComb", "12 WAH EarBender", "12 SFX KlangKling" };
    public static final int[] FILTER_INDICES = { 0x7F, 0x00, 0x01, 0x02, 0x08, 0x09, 0x10, 0x11, 0x12, 0x20, 0x21, 0x22, 0x40, 0x41, 0x42, 0x48, 0x50, 0x51, 
                                                 0x083, 0x084, 0x085, 0x086, 0x087, 0x088, 0x089, 0x08A, 0x08B, 0x08C, 0x08D, 0x08E, 0x08F, 
                                                 0x090, 0x091, 0x092, 0x093, 0x094, 0x095, 0x096, 0x097, 0x098, 0x099, 0x09A, 0x09B, 0x09C, 0x09D, 0x09E, 0x09F, 
                                                 0x0A0, 0x0A1, 0x0A2, 0x0A3 };
    public static final int[] FILTER_CATEGORIES = { -1, 0, 0, 0, 1, 1, 2, 2, 2, 3, 3, 3, 4, 4, 4, 4, 5, 5,
                                                    6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6 };
    
    public static final String[] LFO_SYNCS = { "Key Sync", "Free Run" };

    // These go -25...-1, then are followed by numbers 0 ... 127
    public static final String[] LFO_DELAYS = { "8/1", "4/1D", "8/1T", "4/1", "2/1D", "4/1T", "2/1", "1/1D", "2/1T", "1/1", "1/2D", "1/1T", "1/2", "1/4D", "1/2T", "1/4", "1/8D", "1/4T", "1/8", "1/16D", "1/8T", "1/16", "1/32D", "1/16T", "1/32" };                                                                                           

    // Also negative rates are the same as the LFO_DELAYS
    public static final String[] LFO_RATES = { "0.08", "0.11", "0.15", "0.18", "0.21", "0.25", "0.28", "0.32", "0.35", "0.39", "0.42", "0.46", "0.50", "0.54", "0.58", "0.63", 
                                               "0.67", "0.71", "0.76", "0.80", "0.85", "0.90", "0.94", "0.99", "1.04", "1.10", "1.15", "1.20", "1.25", "1.31", "1.37", "1.42", 
                                               "1.48", "1.54", "1.60", "1.67", "1.73", "1.79", "1.86", "1.93", "2.00", "2.07", "2.14", "2.21", "2.29", "2.36", "2.44", "2.52", 
                                               "2.60", "2.68", "2.77", "2.85", "2.94", "3.03", "3.12", "3.21", "3.31", "3.40", "3.50", "3.60", "3.70", "3.81", "3.91", "4.02", 
                                               "4.13", "4.25", "4.36", "4.48", "4.60", "4.72", "4.84", "4.97", "5.10", "5.23", "5.37", "5.51", "5.65", "5.79", "5.94", "6.08", 
                                               "6.24", "6.39", "6.55", "6.71", "6.88", "7.04", "7.21", "7.39", "7.57", "7.75", "7.93", "8.12", "8.32", "8.51", "8.71", "8.92", 
                                               "9.13", "9.34", "9.56", "9.78", "10.00", "10.23", "10.47", "10.71", "10.95", "11.20", "11.46", "11.71", "11.98", "12.25", "12.52", "12.80", 
                                               "13.09", "13.38", "13.68", "13.99", "14.30", "14.61", "14.93", "15.26", "15.60", "15.94", "16.29", "16.65", "17.01", "17.38", "17.76", "18.14" };
    
    
    /// Documentation is mistaken here
    public static final String[] LFO_SHAPES = { "Random", "Triangle", "Sine", "Sawtooth", "Square", "33% Pulse", "25% Pulse", "16% Pulse", "12% Pulse", "Pat: Octaves", "Pat: 5th + Oct", "Pat: Sus4Trip", "Pat: Neener", "Sine 1, 2", "Sine 1, 3, 5", "Sine + Noise", "Hemiquaver" };
    public static final int[] LFO_INDICES = { -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 };

    public static final String[] VOL_ENV_MODES = { "Factory", "Time", "Tempo" };
    public static final String[] ENV_MODES = { "Time", "Tempo" };
    public static final int[] ENV_INDICES = { 1, 2 };           // This is only for ENV_MODES, not VOL_ENV_MODES

    public static final String[] LAYER_PATCHCORD_SOURCES = { "Off", "Crossfade Random", "Key +", "Key +/-", "Velocity +", "Velocity +/-", "Velocity <", "Release Velocity", "Gate", "Pitch Wheel", "Mod Wheel", "Pressure", "Pedal", "MIDI A", "MIDI B", "Foot Switch 1", "Foot Switch 2", "Flip-Flop Foot Switch 1", "Flip-Flop Foot Switch 2", "MIDI Volume Controller (7)", 
                                                             "MIDI Pan Controller (10)", "MIDI C", "MIDI D", "MIDI E", "MIDI F", "MIDI G", "MIDI H", "Foot Switch 3", "Flip-Flop Foot Switch 3", "MIDI I", "MIDI J", "MIDI K", "MIDI L", "Key Glide", "Volume Envelope +", "Volume Envelope +/-", "Volume Envelope <", "Filter Envelope +", "Filter Envelope +/-", "Filter Envelope <", "Auxiliary Envelope +", 
                                                             "Auxiliary Envelope +/-", "Auxiliary Envelope <", "LFO 1 +/-", "LFO 1 +", "White Noise", "Pink Noise", "Key Random 1", "Key Random 2", "LFO 2 +/-", "LFO 2 +", "Lag 0 Summing Amp", "Lag 0", "Lag 1 Summing Amp", "Lag 1", "Preset Lag Out", "Preset Ramp Out", "Clock Double Whole Note", "Clock Whole Note", "Clock Half Note", "Clock Quarter Note", 
                                                             "Clock Eighth Note", "Clock Sixteenth Note", "Clock Octal Whole Note", "Clock Quad Whole Note", "DC Offset", "Summing Amp", "Switch", "Absolute Value", "Diode", "Flip Flop", "Quantizer", "Gain 4X" };
    public static final int[] LAYER_PATCHCORD_SOURCE_INDICES = { 0, 4, 8, 9, 10, 11, 12, 13, 14, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 48, 72, 73, 74, 80, 81, 82, 88, 89, 90, 96, 97, 98, 99, 100, 101, 104, 105, 106, 107, 108, 109, 128, 129, 144, 145, 146, 147, 148, 149, 150, 151, 160, 161, 162, 163, 164, 165, 166, 167 };

        
    // The documentation is wrong in several places here: it's missing envelope sustains, has retriggers for envelopes other than volume, and has "chorus position ITD" which doesn't exist.
    // I think Amplifier Crossfade is RTXFade (for "Real Time XFade")
    public static final String[] LAYER_PATCHCORD_DESTINATIONS = { "Off", "Key Sustain", "Fine Pitch", "Pitch", "Glide", "Chorus Amount", "Sample Start", "Sample Loop", "Sample Retrigger", "Filter Frequency", "Filter Resonance", "Amplifier Volume", "Amplifier Pan", "Real-Time Crossfade", "Volume Envelope Rates", "Volume Envelope Attack", 
                                                                  "Volume Envelope Decay", "Volume Envelope Sustain", "Volume Envelope Release", "Filter Envelope Rates", "Filter Envelope Attack", "Filter Envelope Decay", "Filter Envelope Sustain", "Filter Envelope Release", "Auxiliary Envelope Rates", "Auxiliary Envelope Attack", "Auxiliary Envelope Decay", "Auxiliary Envelope Sustain", "Auxiliary Envelope Release", 
                                                                  "Auxiliary Envelope Trigger", "LFO 1 Rate", "LFO 1 Trigger", "LFO 2 Rate", "LFO 2 Trigger", "Lag 0 In", "Lag 1 In", "Summing Amp", "Switch", "Absolute Value", "Diode", "Flip Flop", "Quantize", "Gain 4X", "Cord 1 Amount", "Cord 2 Amount", "Cord 3 Amount", "Cord 4 Amount", "Cord 5 Amount", 
                                                                  "Cord 6 Amount", "Cord 7 Amount", "Cord 8 Amount", "Cord 9 Amount", "Cord 10 Amount", "Cord 11 Amount", "Cord 12 Amount", "Cord 13 Amount", "Cord 14 Amount", "Cord 15 Amount", "Cord 16 Amount", "Cord 17 Amount", "Cord 18 Amount", "Cord 19 Amount", "Cord 20 Amount", "Cord 21 Amount", "Cord 22 Amount", "Cord 23 Amount", "Cord 24 Amount",  };
    public static final int[] LAYER_PATCHCORD_DESTINATION_INDICES = { 0, 8, 47, 48, 49, 50, 52, 53, 54, 56, 57, 64, 65, 66, 72, 73, 74, 76, 75, 80, 81, 82, 84, 83, 88, 89, 90, 92, 91, 94, 96, 97, 104, 105, 106, 108, 161, 162, 163, 164, 165, 166, 167, 168, 169, 170, 171, 172, 173, 174, 175, 176, 177, 178, 179, 180, 181, 182, 183, 184, 185, 186, 187, 188, 189 , 190, 191 };

    public static final String[] TEMPO_BASED_ENV_RATES = {
        "0", "1", "2", "3", "4", "5", "6", "1/64", "8", "9", "10", "11", "1/32t", "13", "1/64d", "15", 
        "16", "17", "18", "1/32", "20", "21", "22", "23", "1/16t", "25", "1/32d", "27", "28", "29", "30", "1/16", 
        "32", "33", "34", "35", "1/8t", "37", "1/16d", "39", "40", "41", "42", "1/8", "44", "45", "46", "47", 
        "1/4t", "49", "1/8d", "51", "52", "53", "54", "1/4", "56", "57", "58", "59", "1/2t", "61", "1/4d", "63", 
        "64", "65", "66", "1/2", "68", "69", "70", "71", "1/1t", "73", "1/2d", "75", "76", "77", "78", "1/1", 
        "80", "81", "82", "83", "2/1t", "85", "1/1d", "87", "88", "89", "90", "2/1", "92", "93", "94", "95", 
        "4/1t", "97", "2/1d", "99", "100", "101", "102", "4/1", "104", "105", "106", "107", "8/1t", "109", "4/1d", "111", 
        "112", "113", "114", "8/1", "116", "117", "118", "119", "16/1t", "121", "8/1d", "123", "124", "125", "126", "16/1" };

    public static final String[] GLIDE_RATES = { 
        "0.000", "0.002", "0.004", "0.006", "0.008", "0.010", "0.012", "0.014", "0.016", "0.018", "0.020", "0.022", "0.024",
        "0.026", "0.028", "0.030", "0.032", "0.034", "0.036", "0.038", "0.040", "0.042", "0.044", "0.046", "0.050", "0.052", 
        "0.056", "0.058", "0.064", "0.068", "0.072", "0.076", "0.082", "0.086", "0.092", "0.098", "0.104", "0.110", "0.116", 
        "0.124", "0.130", "0.140", "0.148", "0.158", "0.166", "0.176", "0.186", "0.196", "0.208", "0.220", "0.234", "0.248", 
        "0.262", "0.278", "0.294", "0.312", "0.330", "0.348", "0.368", "0.390", "0.412", "0.436", "0.462", "0.488", "0.518", 
        "0.546", "0.578", "0.612", "0.646", "0.684", "0.724", "0.764", "0.808", "0.856", "0.904", "0.956", "1.010", "1.068", 
        "1.128", "1.194", "1.264", "1.334", "1.412", "1.492", "1.580", "1.670", "1.766", "1.868", "1.974", "2.090", "2.212", 
        "2.340", "2.476", "2.622", "2.776", "2.940", "3.112", "3.298", "3.496", "3.706", "3.930", "4.170", "4.426", "4.700", 
        "4.994", "5.308", "5.648", "6.012", "6.404", "6.830", "7.288", "7.786", "8.328", "8.920", "9.568", "10.282", "11.068", 
        "11.940", "12.912", "14.006", "15.244", "16.656", "18.280", "20.174", "22.148", "25.130", "28.472", "32.738" };

    public static final String[] GAINS = {          // these are in dB, but we don't have space for that
        "-24.0", "-23.7", "-23.3", "-22.9", "-22.5", "-22.2", "-21.8", "-21.4", 
        "-21.0", "-20.7", "-20.3", "-19.9", "-19.5", "-19.2", "-18.8", "-18.4", 
        "-18.0", "-17.7", "-17.3", "-16.9", "-16.5", "-16.2", "-15.8", "-15.4", 
        "-15.0", "-14.7", "-14.3", "-13.9", "-13.5", "-13.2", "-12.8", "-12.4", 
        "-12.0", "-11.7", "-11.3", "-10.9", "-10.5", "-10.2", "-9.8", "-9.4", 
        "-9.0", "-8.7", "-8.3", "-7.9", "-7.5", "-7.2", "-6.8", "-6.4", 
        "-6.0", "-5.7", "-5.3", "-4.9", "-4.5", "-4.2", "-3.8", "-3.4", 
        "-3.0", "-2.7", "-2.3", "-1.9", "-1.5", "-1.2", "-0.8", "-0.4", 
        "0.0", "0.3", "0.7", "1.1", "1.5", "1.8", "2.2", "2.6", 
        "3.0", "3.3", "3.7", "4.1", "4.5", "4.8", "5.2", "5.6", 
        "6.0", "6.3", "6.7", "7.1", "7.5", "7.8", "8.2", "8.6", 
        "9.0", "9.3", "9.7", "10.1", "10.5", "10.8", "11.2", "11.6", 
        "12.0", "12.3", "12.7", "13.1", "13.5", "13.8", "14.2", "14.6", 
        "15.0", "15.3", "15.7", "16.1", "16.5", "16.8", "17.2", "17.6", 
        "18.0", "18.3", "18.7", "19.1", "19.5", "19.8", "20.2", "20.6", 
        "21.0", "21.3", "21.7", "22.1", "22.5", "22.8", "23.2", "23.6"
        };

    public static final String[] FILTERS_TYPE_1 = {
        "57 Hz", "59 Hz", "61 Hz", "63 Hz", "65 Hz", "67 Hz", "69 Hz", "71 Hz", 
        "73 Hz", "75 Hz", "77 Hz", "79 Hz", "81 Hz", "83 Hz", "85 Hz", "87 Hz", 
        "89 Hz", "91 Hz", "93 Hz", "96 Hz", "99 Hz", "102 Hz", "105 Hz", "108 Hz", 
        "111 Hz", "114 Hz", "117 Hz", "120 Hz", "123 Hz", "126 Hz", "129 Hz", "132 Hz", 
        "135 Hz", "138 Hz", "142 Hz", "146 Hz", "150 Hz", "154 Hz", "158 Hz", "162 Hz", 
        "166 Hz", "170 Hz", "174 Hz", "178 Hz", "182 Hz", "186 Hz", "191 Hz", "196 Hz", 
        "201 Hz", "206 Hz", "211 Hz", "216 Hz", "221 Hz", "226 Hz", "231 Hz", "237 Hz", 
        "243 Hz", "249 Hz", "255 Hz", "261 Hz", "267 Hz", "273 Hz", "280 Hz", "287 Hz", 
        "294 Hz", "301 Hz", "308 Hz", "315 Hz", "322 Hz", "330 Hz", "338 Hz", "346 Hz", 
        "354 Hz", "362 Hz", "370 Hz", "379 Hz", "388 Hz", "397 Hz", "406 Hz", "415 Hz", 
        "425 Hz", "435 Hz", "445 Hz", "455 Hz", "466 Hz", "477 Hz", "488 Hz", "499 Hz", 
        "510 Hz", "522 Hz", "534 Hz", "546 Hz", "558 Hz", "571 Hz", "584 Hz", "597 Hz", 
        "611 Hz", "625 Hz", "639 Hz", "654 Hz", "669 Hz", "684 Hz", "700 Hz", "716 Hz", 
        "732 Hz", "749 Hz", "766 Hz", "783 Hz", "801 Hz", "819 Hz", "837 Hz", "856 Hz", 
        "875 Hz", "895 Hz", "915 Hz", "936 Hz", "957 Hz", "979 Hz", "1001Hz", "1023Hz", 
        "1046Hz", "1069Hz", "1093Hz", "1117Hz", "1142Hz", "1168Hz", "1194Hz", "1221Hz", 
        "1248Hz", "1276Hz", "1305Hz", "1334Hz", "1364Hz", "1394Hz", "1425Hz", "1457Hz", 
        "1489Hz", "1522Hz", "1556Hz", "1591Hz", "1626Hz", "1662Hz", "1699Hz", "1737Hz", 
        "1776Hz", "1815Hz", "1855Hz", "1896Hz", "1938Hz", "1981Hz", "2025Hz", "2070Hz", 
        "2116Hz", "2163Hz", "2211Hz", "2260Hz", "2310Hz", "2361Hz", "2413Hz", "2467Hz", 
        "2522Hz", "2578Hz", "2635Hz", "2693Hz", "2753Hz", "2814Hz", "2876Hz", "2940Hz", 
        "3005Hz", "3071Hz", "3139Hz", "3208Hz", "3279Hz", "3352Hz", "3426Hz", "3502Hz", 
        "3579Hz", "3658Hz", "3739Hz", "3822Hz", "3906Hz", "3992Hz", "4080Hz", "4170Hz", 
        "4262Hz", "4356Hz", "4452Hz", "4550Hz", "4650Hz", "4753Hz", "4858Hz", "4965Hz", 
        "5075Hz", "5187Hz", "5301Hz", "5418Hz", "5537Hz", "5659Hz", "5784Hz", "5912Hz", 
        "6042Hz", "6175Hz", "6311Hz", "6450Hz", "6592Hz", "6737Hz", "6885Hz", "7037Hz", 
        "7192Hz", "7350Hz", "7512Hz", "7677Hz", "7846Hz", "8019Hz", "8196Hz", "8376Hz", 
        "8560Hz", "8748Hz", "8941Hz", "9138Hz", "9339Hz", "9545Hz", "9755Hz", "9970Hz", 
        "10189", "10413", "10642", "10876", "11115", "11360", "11610", "11865",         // not enough space to say "Hz" for >= 10000Hz
        "12126", "12393", "12666", "12945", "13230", "13521", "13818", "14122", 
        "14433", "14750", "15074", "15405", "15744", "16090", "16444", "16806", 
        "17176", "17554", "17940", "18334", "18737", "19149", "19570", "20000"
        };
                
    public static final String[] FILTERS_TYPE_2 = {
        "69 Hz", "71 Hz", "73 Hz", "75 Hz", "77 Hz", "79 Hz", "81 Hz", "83 Hz", 
        "85 Hz", "87 Hz", "89 Hz", "91 Hz", "93 Hz", "95 Hz", "97 Hz", "100 Hz", 
        "103 Hz", "106 Hz", "109 Hz", "112 Hz", "115 Hz", "118 Hz", "121 Hz", "124 Hz", 
        "127 Hz", "130 Hz", "133 Hz", "136 Hz", "139 Hz", "142 Hz", "145 Hz", "149 Hz", 
        "153 Hz", "157 Hz", "161 Hz", "165 Hz", "169 Hz", "173 Hz", "177 Hz", "181 Hz", 
        "185 Hz", "189 Hz", "193 Hz", "198 Hz", "203 Hz", "208 Hz", "213 Hz", "218 Hz", 
        "223 Hz", "228 Hz", "233 Hz", "238 Hz", "244 Hz", "250 Hz", "256 Hz", "262 Hz", 
        "268 Hz", "274 Hz", "280 Hz", "286 Hz", "292 Hz", "299 Hz", "306 Hz", "313 Hz", 
        "320 Hz", "327 Hz", "334 Hz", "342 Hz", "350 Hz", "358 Hz", "366 Hz", "374 Hz", 
        "382 Hz", "390 Hz", "399 Hz", "408 Hz", "417 Hz", "426 Hz", "435 Hz", "445 Hz", 
        "455 Hz", "465 Hz", "475 Hz", "485 Hz", "496 Hz", "507 Hz", "518 Hz", "529 Hz", 
        "541 Hz", "553 Hz", "565 Hz", "577 Hz", "590 Hz", "603 Hz", "616 Hz", "629 Hz", 
        "643 Hz", "657 Hz", "671 Hz", "686 Hz", "701 Hz", "716 Hz", "732 Hz", "748 Hz", 
        "764 Hz", "780 Hz", "797 Hz", "814 Hz", "832 Hz", "850 Hz", "868 Hz", "887 Hz", 
        "906 Hz", "925 Hz", "945 Hz", "965 Hz", "986 Hz", "1007Hz", "1029Hz", "1051Hz", 
        "1074Hz", "1097Hz", "1120Hz", "1144Hz", "1168Hz", "1193Hz", "1218Hz", "1244Hz", 
        "1271Hz", "1298Hz", "1326Hz", "1354Hz", "1383Hz", "1412Hz", "1442Hz", "1473Hz", 
        "1504Hz", "1536Hz", "1569Hz", "1602Hz", "1636Hz", "1671Hz", "1707Hz", "1743Hz", 
        "1780Hz", "1818Hz", "1857Hz", "1896Hz", "1936Hz", "1977Hz", "2019Hz", "2062Hz", 
        "2106Hz", "2151Hz", "2197Hz", "2244Hz", "2292Hz", "2341Hz", "2391Hz", "2442Hz", 
        "2494Hz", "2547Hz", "2601Hz", "2656Hz", "2712Hz", "2769Hz", "2827Hz", "2887Hz", 
        "2948Hz", "3010Hz", "3074Hz", "3139Hz", "3205Hz", "3273Hz", "3342Hz", "3412Hz", 
        "3484Hz", "3557Hz", "3632Hz", "3709Hz", "3787Hz", "3867Hz", "3948Hz", "4031Hz", 
        "4116Hz", "4203Hz", "4292Hz", "4382Hz", "4474Hz", "4568Hz", "4664Hz", "4762Hz", 
        "4862Hz", "4964Hz", "5068Hz", "5175Hz", "5284Hz", "5395Hz", "5508Hz", "5624Hz", 
        "5742Hz", "5863Hz", "5986Hz", "6112Hz", "6240Hz", "6371Hz", "6505Hz", "6642Hz", 
        "6782Hz", "6925Hz", "7070Hz", "7219Hz", "7371Hz", "7526Hz", "7684Hz", "7845Hz", 
        "8010Hz", "8178Hz", "8350Hz", "8525Hz", "8704Hz", "8887Hz", "9074Hz", "9264Hz", 
        "9458Hz", "9657Hz", "9860Hz", "10067", "10278", "10494", "10714", "10939",      // not enough space to say "Hz" for >= 10000Hz
        "11169", "11403", "11642", "11886", "12135", "12390", "12650", "12915", 
        "13186", "13463", "13745", "14033", "14327", "14627", "14934", "15247", 
        "15567", "15893", "16226", "16566", "16913", "17268", "17630", "18000"
        };
        
    public static final String[] FILTERS_TYPE_3 = {
        "83 Hz", "85 Hz", "87 Hz", "89 Hz", "91 Hz", "93 Hz", "95 Hz", "97 Hz", 
        "99 Hz", "101 Hz", "103 Hz", "105 Hz", "107 Hz", "109 Hz", "111 Hz", "113 Hz", 
        "116 Hz", "119 Hz", "122 Hz", "125 Hz", "128 Hz", "131 Hz", "134 Hz", "137 Hz", 
        "140 Hz", "143 Hz", "146 Hz", "149 Hz", "152 Hz", "155 Hz", "158 Hz", "161 Hz", 
        "164 Hz", "167 Hz", "170 Hz", "174 Hz", "178 Hz", "182 Hz", "186 Hz", "190 Hz", 
        "194 Hz", "198 Hz", "202 Hz", "206 Hz", "210 Hz", "214 Hz", "218 Hz", "222 Hz", 
        "226 Hz", "231 Hz", "236 Hz", "241 Hz", "246 Hz", "251 Hz", "256 Hz", "261 Hz", 
        "266 Hz", "271 Hz", "276 Hz", "281 Hz", "287 Hz", "293 Hz", "299 Hz", "305 Hz", 
        "311 Hz", "317 Hz", "323 Hz", "329 Hz", "335 Hz", "342 Hz", "349 Hz", "356 Hz", 
        "363 Hz", "370 Hz", "377 Hz", "384 Hz", "391 Hz", "399 Hz", "407 Hz", "415 Hz", 
        "423 Hz", "431 Hz", "439 Hz", "447 Hz", "455 Hz", "464 Hz", "473 Hz", "482 Hz", 
        "491 Hz", "500 Hz", "509 Hz", "519 Hz", "529 Hz", "539 Hz", "549 Hz", "559 Hz", 
        "570 Hz", "581 Hz", "592 Hz", "603 Hz", "614 Hz", "625 Hz", "637 Hz", "649 Hz", 
        "661 Hz", "673 Hz", "686 Hz", "699 Hz", "712 Hz", "725 Hz", "738 Hz", "752 Hz", 
        "766 Hz", "780 Hz", "794 Hz", "809 Hz", "824 Hz", "839 Hz", "855 Hz", "871 Hz", 
        "887 Hz", "903 Hz", "920 Hz", "937 Hz", "954 Hz", "972 Hz", "990 Hz", "1008Hz", 
        "1027Hz", "1046Hz", "1065Hz", "1085Hz", "1105Hz", "1125Hz", "1146Hz", "1167Hz", 
        "1188Hz", "1210Hz", "1232Hz", "1255Hz", "1278Hz", "1301Hz", "1325Hz", "1349Hz", 
        "1374Hz", "1399Hz", "1425Hz", "1451Hz", "1477Hz", "1504Hz", "1531Hz", "1559Hz", 
        "1587Hz", "1616Hz", "1645Hz", "1675Hz", "1705Hz", "1736Hz", "1768Hz", "1800Hz", 
        "1833Hz", "1866Hz", "1900Hz", "1934Hz", "1969Hz", "2005Hz", "2041Hz", "2078Hz", 
        "2116Hz", "2154Hz", "2193Hz", "2233Hz", "2273Hz", "2314Hz", "2356Hz", "2399Hz", 
        "2442Hz", "2486Hz", "2531Hz", "2577Hz", "2624Hz", "2671Hz", "2719Hz", "2768Hz", 
        "2818Hz", "2869Hz", "2921Hz", "2974Hz", "3028Hz", "3083Hz", "3139Hz", "3196Hz", 
        "3254Hz", "3313Hz", "3373Hz", "3434Hz", "3496Hz", "3559Hz", "3623Hz", "3688Hz", 
        "3754Hz", "3822Hz", "3891Hz", "3961Hz", "4032Hz", "4105Hz", "4179Hz", "4254Hz", 
        "4331Hz", "4409Hz", "4488Hz", "4569Hz", "4651Hz", "4735Hz", "4820Hz", "4907Hz", 
        "4995Hz", "5085Hz", "5177Hz", "5270Hz", "5365Hz", "5461Hz", "5559Hz", "5659Hz", 
        "5761Hz", "5865Hz", "5970Hz", "6077Hz", "6186Hz", "6297Hz", "6410Hz", "6525Hz", 
        "6642Hz", "6761Hz", "6882Hz", "7006Hz", "7132Hz", "7260Hz", "7390Hz", "7523Hz", 
        "7658Hz", "7796Hz", "7936Hz", "8078Hz", "8223Hz", "8371Hz", "8521Hz", "8674Hz", 
        "8830Hz", "8989Hz", "9150Hz", "9314Hz", "9481Hz", "9651Hz", "9824Hz", "10000"   // not enough space to say "Hz" for >= 10000Hz
        };
    
    // There are actually 128 + 12 delays, the first 12 are negative values.  This is not documented or even mentioned in the specs.  :-(
    public static final String[] FX_DELAYS = { 
        "1/4d", "1/2t", "1/4", "1/8d", "1/4t", "1/8", "1/16d", "1/8t", "1/16", "1/32d", "1/16t", "1/32", 
        "0ms", "5ms", "10ms", "15ms", "20ms", "25ms", "30ms", "35ms", "40ms", "45ms", "50ms", "55ms", "60ms", "65ms", "70ms", "75ms", 
        "80ms", "85ms", "90ms", "95ms", "100ms", "105ms", "110ms", "115ms", "120ms", "125ms", "130ms", "135ms", "140ms", "145ms", "150ms", "155ms", 
        "160ms", "165ms", "170ms", "175ms", "180ms", "185ms", "190ms", "195ms", "200ms", "205ms", "210ms", "215ms", "220ms", "225ms", "230ms", "235ms", 
        "240ms", "245ms", "250ms", "255ms", "260ms", "265ms", "270ms", "275ms", "280ms", "285ms", "290ms", "295ms", "300ms", "305ms", "310ms", "315ms", 
        "320ms", "325ms", "330ms", "335ms", "340ms", "345ms", "350ms", "355ms", "360ms", "365ms", "370ms", "375ms", "380ms", "385ms", "390ms", "395ms", 
        "400ms", "405ms", "410ms", "415ms", "420ms", "425ms", "430ms", "435ms", "440ms", "445ms", "450ms", "455ms", "460ms", "465ms", "470ms", "475ms", 
        "480ms", "485ms", "490ms", "495ms", "500ms", "505ms", "510ms", "515ms", "520ms", "525ms", "530ms", "535ms", "540ms", "545ms", "550ms", "555ms", 
        "560ms", "565ms", "570ms", "575ms", "580ms", "585ms", "590ms", "595ms", "600ms", "605ms", "610ms", "615ms", "620ms", "625ms", "630ms", "635ms", 
        };
        
        


    //// RESOURCE LOADING
    //// The Proteus 2000 has lots of possible ROMs, and each ROM has a set of PRESETS, a set of ARPEGGIOS, a set of INSTRUMENTS, and a set of RIFFS.
    //// These sets may zero, and they can also be quite large, and they're highly variable in size from ROM to ROM.  We don't want to load all of them
    //// when we fire up the editor.  So instead we're loading them on the fly.  If the presets, arps, instruments, and riffs are loaded for a given
    //// ROM, then (for example) PRESET_NAMES[rom] will be non-null, where "rom" is the rom number, not the rom ID.  Rom 0 is USER.  However only
    //// the PRESET will load names from user, as the user memory doesn't have arps, instruments, or riffs.
    ////
    //// To do loading on the fly we call updateFromFile(...), which loads from a given resource into one of the four arrays. This
    //// is in turn called by updateArps(rom), updatePresets(rom), updateInstruments(rom), and updateRiffs(rom).  
                
    // These are loaded on request.  Note that PRESET_NAMES includes the USER preset
    public static final String[][] ARP_NAMES = new String[ROMS.length][];
    public static final String[][] INSTRUMENT_NAMES = new String[ROMS.length][];
    public static final String[][] PRESET_NAMES = new String[ROMS_AND_USER.length][];           // Notice that this has to be ROMS_AND_USER, as presets include user presets
    public static final String[][] RIFF_NAMES = new String[ROMS.length][];
    
    Chooser arpChooser;
    Chooser riffChooser;
    Chooser[] instrumentChooser = new Chooser[4];
    Chooser[] presetChooser = new Chooser[2];
    int defaultRiff = 0;
    int defaultArp = 0;
    int[] defaultInstrument = { 0, 0, 0, 0 };
    int[] defaultPreset = { 0, 0 };
    int[] simms = { 0, 0, 0, 0 };

    void updateFromFile(int rom, String[][] update, String resource)
        {
        if (update[rom] == null)
            {
            LineNumberReader reader = new LineNumberReader(new InputStreamReader(getClass().getResourceAsStream(resource)));
            ArrayList<String> txt = new ArrayList<>();
            try
                {
                int item = 0;
                while(true)
                    {
                    String str = reader.readLine();
                    if (str == null) break;
                    if (update == PRESET_NAMES)
                        {
                        int bank = item / 128;
                        int number = item % 128;
                        String pad = (number < 100 ? (number < 10 ? "00" : "0") : "") + number;
                        txt.add("" + bank + "/" + pad + " " + str);
                        }
                    else
                        {
                        txt.add("" + item + " " + str);
                        }
                    item++;
                    }
                }
            catch (IOException ex)
                {
                ex.printStackTrace();
                }
            update[rom] = txt.toArray(new String[0]);
            }
        }

    void updateArps(int rom)
        {
        updateFromFile(rom, ARP_NAMES, "roms/n_arp_" + ROM_IDS[rom] + ".txt");
        }

    void updateInstruments(int rom)
        {
        updateFromFile(rom, INSTRUMENT_NAMES, "roms/n_ins_" + ROM_IDS[rom] + ".txt");
        }

    void updatePresets(int romOrUser)
        {
        updateFromFile(romOrUser, PRESET_NAMES, "roms/n_prs_" + ROM_AND_USER_IDS[romOrUser] + ".txt");
        }

    void updateRiffs(int rom)
        {
        updateFromFile(rom, RIFF_NAMES, "roms/n_rff_" + ROM_IDS[rom] + ".txt");
        }










    public EmuProteus2000()
        {
        buildParameters();
        
        // Fix a few min/max warnings by entering some values here:
        model.set("riff", 0);
        model.set("link1preset", 0);
        model.set("link2preset", 0);
        model.set("arppattern", 0);
        model.set("layer1instrument", 0);
        model.set("layer2instrument", 0);
        model.set("layer3instrument", 0);
        model.set("layer4instrument", 0);
        
        /// SOUND PANEL
                
        SynthPanel soundPanel = new SynthPanel(this);
        
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        hbox.addLast(addCommonGeneral(Style.COLOR_B()));
        vbox.add(hbox);
  
        vbox.add(addCommonLink(1, Style.COLOR_A()));
        vbox.add(addCommonLink(2, Style.COLOR_A()));
        
        vbox.add(addCommonEffects(1, Style.COLOR_C()));
        vbox.add(addCommonEffects(2, Style.COLOR_C()));

        vbox.add(addCommonArpeggiator(Style.COLOR_A())); 

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Common", soundPanel);
                

        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        vbox.add(addCommonPatchCords(Style.COLOR_B())); 
        vbox.add(addCommonControl(Style.COLOR_A())); 
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Common Cords", soundPanel);

        for(int i = 1; i <= 4; i++)
            {
            soundPanel = new SynthPanel(this);
            vbox = new VBox();
            vbox.add(addLayerGeneral(i, Style.COLOR_A()));
            hbox = new HBox();
            hbox.add(addLayerLFO(i, 1, Style.COLOR_C()));
            hbox.add(addLayerLFO(i, 2, Style.COLOR_C()));
            hbox.addLast(addLayerFilter(i, Style.COLOR_B()));
            vbox.add(hbox);
            vbox.add(addLayerEnvelope(i, 1, Style.COLOR_A()));
            vbox.add(addLayerEnvelope(i, 2, Style.COLOR_B()));
            vbox.add(addLayerEnvelope(i, 3, Style.COLOR_C()));
            soundPanel.add(vbox, BorderLayout.CENTER);
            addTab("Layer " + i, soundPanel);
            soundPanel.makePasteable("layer");
                                                
            soundPanel = new SynthPanel(this);
            vbox = new VBox();
            vbox.add(addLayerPatchCords(i, Style.COLOR_A()));
            soundPanel.add(vbox, BorderLayout.CENTER);
            addTab("Cords " + i, soundPanel);
            }

        model.set("name", "Untitled");
        model.set("number", 0);
        
        loadDefaults();
        }
                
    public String getDefaultResourceFileName() { return "EmuProteus2000.init"; }
    public String getHTMLResourceFileName() { return "EmuProteus2000.html"; }


    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        // Because there are so many ROMs, we'll store the last one the user selected and use that
        int lastBank = getLastXAsInt("LastROM", getSynthClassName(), 0, false);
        boolean showingAll = (getLastXAsInt("ShowAllROMs", getSynthClassName(), 1, false) != 0);

        // The "bank" is going to be a combination of ROM/User and Bank.  Specifically it will
        // be the ROM/User index into ROMS_AND_USER (0...21) times eight, plus the bank.

        final JComboBox rom = new JComboBox(ROMS_AND_USER);
        rom.setEditable(false);
        rom.setMaximumRowCount(32);
        rom.setSelectedIndex(writing ? 0 : lastBank);   // model.get("bank"));          // let's retain the ROM if we possibly can

        final JCheckBox showAll = new JCheckBox("");
        showAll.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                if (!writing)
                    {
                    rom.removeAllItems();
                    if (showAll.isSelected()) 
                        {
                        for(int i = 0; i < ROMS_AND_USER.length; i++)
                            {
                            rom.addItem(ROMS_AND_USER[i]);
                            }
                        rom.setSelectedIndex(lastBank);   
                        }
                    else
                        {
                        rom.addItem(ROMS_AND_USER[0]);
                        if (lastBank == 0)
                            rom.setSelectedIndex(0);   
                        for(int i = 0; i < simms.length; i++)
                            {
                            if (simms[i] != 0) 
                                {
                                rom.addItem(ROMS_AND_USER[simms[i]]);
                                if (lastBank == simms[i])
                                    rom.setSelectedIndex(i + 1);   
                                }
                            }
                        }
                    }
                }
            });
        // because setSelected doesn't trigger the action listener, and we want to do that to set up the ROM list,
        // we set to the wrong value and click once.
        showAll.setSelected(!showingAll);
        showAll.doClick();

        int num = model.get("number");
        int bnk = num / 128;
        num = num % 128;
        
        JTextField bank = new SelectedTextField("" + bnk, 1);
        JTextField number = new SelectedTextField("" + (num < 10 ? "00" : (num < 100 ? "0" : "")) + num, 3);

        while(true)
            {
            boolean result = (writing ?
                showMultiOption(this, new String[] { "Bank", "Number" }, new JComponent[] { bank, number }, title, "Enter the User Bank and Patch Number") :
                showMultiOption(this, new String[] { "Show All ROMs", "ROM", "Bank", "Number" }, new JComponent[] { showAll, rom, bank, number }, title, "Enter the User/ROM, Bank, and Patch Number"));
                                        
            if (result == false) 
                return false;
                                
            int b;
            int maxBank = (NUM_BANKS[writing ? 0 : rom.getSelectedIndex()] - 1);
            try {b = Integer.parseInt(bank.getText()); }
            catch (NumberFormatException e)
                {
                showSimpleError(title, "The Bank must be an integer 0..." + maxBank);
                continue;
                }
            if (b < 0 || b > maxBank)
                {
                showSimpleError(title, "The Bank must be an integer 0..." + maxBank);
                continue;
                }
                                                                        
            int n;
            try { n = Integer.parseInt(number.getText()); }
            catch (NumberFormatException e)
                {
                showSimpleError(title, "The Patch Number must be an integer 0...127");
                continue;
                }
            if (n < 0 || n > 127)
                {
                showSimpleError(title, "The Patch Number must be an integer 0...127");
                continue;
                }
            
            // Now we have to figure out what ROM was selected
            if (writing)
                {
                change.set("bank", 0);
                }
            else if (showAll.isSelected() || rom.getSelectedIndex() == 0)       // show all ROMS, or user
                {
                change.set("bank", rom.getSelectedIndex());
                }
            else
                {
                // Find the sim
                for(int i = 0; i < simms.length; i++)
                    {
                    if (ROMS_AND_USER[simms[i]].equals(rom.getSelectedItem()))  // got it 
                        {
                        change.set("bank", simms[i]);
                        break;
                        }
                    }
                }
                
            change.set("number", b * 128 + n);
            if (!writing) setLastX("" + rom.getSelectedIndex(), "LastROM", getSynthClassName(), true);                          // only set in synth
            if (!writing) setLastX(showAll.isSelected() ? "1" : "0", "ShowAllROMs", getSynthClassName(), true);        // only set in synth
                        
            return true;
            }
        }

 
    /** Add the global patch category (name, id, number, etc.) */
    public JComponent addNameGlobal(Color color)
        {
        Category globalCategory = new Category(this, getSynthName(), color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();

        VBox vbox = new VBox();
        comp = new PatchDisplay(this, 20);
        hbox.add(comp);
 
        vbox.add(hbox);
        
        comp = new StringComponent("Patch Name", this, "name", 16, "Name must be up to 16 ASCII characters\nand the fourth character MUST be a colon.")
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


    public JComponent addCommonControl(Color color)
        {
        Category category = new Category(this, "Initial Controller Amounts", color);
        category.makeDistributable("ctrl");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        for(int i = 1; i <= 12; i++)    // yes, <=      There are actually 16 CTRL parameters (A...P) but only A...L (12) are exposed
            {
            comp = new LabelledDial("" + (char)('A' + i - 1), this, "ctrl" + i , color, -1, 127)  
                {
                public String map(int val)
                    {
                    if (val == -1) return "Off";                // The sysex docs say "Current", but the machine says "off"
                    else return "" + val;
                    }
                };
            hbox.add(comp);
            }

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    void updateRiffChooser(String key, Model model)
        {
        if (riffChooser != null)
            {
            int val = model.get(key);
            int cur = model.get("riff");
            updateRiffs(val);
            riffChooser.setElements(RIFF_NAMES[val]);
            if (cur >= RIFF_NAMES[val].length)
                model.set("riff", 0); 
            }
        }
    public JComponent addCommonGeneral(Color color)
        {
        Category category = new Category(this, "General", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        params = KEYBOARD_TUNINGS;
        comp = new Chooser("Keyboard Tuning", this, "kbdtune", params);
        vbox.add(comp);

        params = TEMPO_OFFSETS;
        comp = new Chooser("Tempo Offset", this, "tempooffset", params);
        vbox.add(comp);
        
        hbox.add(vbox);
        vbox = new VBox();
        
        params = ROMS;
        comp = new Chooser("Riff ROM ID", this, "riffromid", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                updateRiffChooser(key, model);
                }
            };
        vbox.add(comp);


        /// The sysex docs say that riff's range is the following, whatever this means:
        /// (-127 - MIDI Note; 1-TBD Riff number)
        /// I have no idea what that is.  But I can say that the riff values appear to be
        /// just just -1...maxriff-1, corresponding to the values in the text files.  
        //  -1 means "off" it appears.
        
        //// NOTE: It appears that RIFFs cannot be turned on or off on the unit.  So I'm
        //// disabling "On" -- note I did this in parse(), emitAll(String), and emitAll(...) as well.
                
        params = new String[] { "Nada" };       // for the moment, this will be changed soon
        comp = new Chooser("Riff", this, "riff", params);
        riffChooser = (Chooser)comp;

        HBox riffbox = new HBox();
        //comp = new CheckBox("On", this, "riffon");
        riffbox.add(riffChooser);
        //riffbox.add(comp);
        vbox.add(riffbox);
        
        updateRiffs(defaultRiff);
        riffChooser.setElements(RIFF_NAMES[defaultRiff]);
        // don't need to update model, as we're presently at 0

        hbox.add(vbox);
        vbox = new VBox();
        
        params = TEMPO_OFFSETS;
        comp = new Chooser("Tempo Offset", this, "tempooffset", params);
        vbox.add(comp);


        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addCommonPatchCords(Color color)
        {
        Category category = new Category(this, "Common Patch Cords", color);
        category.makeDistributable("cord");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox outer = new VBox();
        VBox vbox = new VBox();
        
        for(int i = 0; i < 12; i += 3)
            {
            for(int j = i; j < i + 3; j++)
                {
                vbox = new VBox();
                                
                params = PATCHCORD_SOURCES;
                comp = new Chooser("Source " + (j + 1), this, "cord" + j + "source", params);
                vbox.add(comp);

                params = PATCHCORD_DESTINATIONS;
                comp = new Chooser("Tempo Offset " + (j + 1), this, "cord" + j + "dest", params);
                vbox.add(comp);
                hbox.add(vbox);
                        
                comp = new LabelledDial("Amount " + (j + 1), this, "cord" + j + "amount", color, -100, 255)
                    {
                    public double getStartAngle() { return 170; }
                    public int getDefaultValue() { return 0; }
                    };
                hbox.add(comp);

                if (j < i + 2)
                    {
                    hbox.add(Strut.makeHorizontalStrut(16));
                    }
                }
            outer.add(hbox);
            hbox = new HBox();
            }

        category.add(outer, BorderLayout.CENTER);
        return category;
        }

    void updatePresetChooser(int link, String key, Model model)
        {
        if (presetChooser[link - 1] != null)
            {
            int val = model.get(key);
            int cur = model.get("link" + link + "preset");
            updatePresets(val);
            presetChooser[link - 1].setElements(PRESET_NAMES[val]);
            if (cur >= PRESET_NAMES[val].length)
                model.set("link" + link + "preset", 0); 
            }
        }

    public JComponent addCommonLink(final int link, Color color)
        {
        Category category = new Category(this, "Link " + link, color);
        category.makePasteable("link");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox outer = new VBox();
        VBox vbox = new VBox();

        params = ROMS_AND_USER;
        comp = new Chooser("Preset ROM ID", this, "link" + link + "presetromid", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                updatePresetChooser(link, key, model);
                }
            };
        vbox.add(comp);

        params = new String[] { "Nada" };       // for the moment, this will be changed soon
        comp = new Chooser("Preset", this, "link" + link + "preset", params);
        presetChooser[link - 1] = (Chooser)comp;

        HBox presetBox = new HBox();
        comp = new CheckBox("On", this, "link" + link + "preseton");
        presetBox.add(presetChooser[link - 1]);
        presetBox.add(comp);
        vbox.add(presetBox);

        updatePresets(defaultPreset[link - 1]);
        presetChooser[link - 1].setElements(PRESET_NAMES[defaultPreset[link - 1]]);
        // don't need to update model, as we're presently at 0
        
        hbox.add(vbox);
        vbox = new VBox();

        comp = new LabelledDial("Volume", this, "link" + link + "volume", color, -96, 10)
            {
            public double getStartAngle() { return 335.0; }
            public int getDefaultValue() { return 0; }
            };
        hbox.add(comp);

        comp = new LabelledDial("Pan", this, "link" + link + "pan", color, -64, +63)            // the docs say -64 ... +64 but nope.  Weirdly this is a different range from layer pan (-63 ... +63)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val)
                {
                if (val == 0) return "--";
                else if (val > 0) return "" + val + " >";
                else return "< " + (-val);
                }
            };
        hbox.add(comp);
                        
        comp = new LabelledDial("Transpose", this, "link" + link + "transpose", color, -24, 24);
        hbox.add(comp);

        comp = new LabelledDial("Delay", this, "link" + link + "delay", color, -25, 127)
            {
            public String map(int val)
                {
                if (val < 0) return LFO_DELAYS[val + 25];
                else return "" + val;
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Key Low", this, "link" + link + "keylow", color, 0, 127)
            {
            public String map(int value)
                {
                return NOTES[value % 12] + (value / 12 - 2);
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Key High", this, "link" + link + "keyhigh", color, 0, 127)
            {
            public String map(int value)
                {
                return NOTES[value % 12] + (value / 12 - 2);
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Velocity Low", this, "link" + link + "vellow", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Velocity High", this, "link" + link + "velhigh", color, 0, 127);
        hbox.add(comp);
                        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addCommonEffects(int fx, Color color)
        {
        Category category = new Category(this, (fx == 1 ? "Effects A" : "Effects B"), color);
        category.makePasteable("fx");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox outer = new VBox();
        VBox vbox = new VBox();

        params = (fx == 1 ? FX_A_ALGORITHMS : FX_B_ALGORITHMS);
        comp = new Chooser("Algorithm", this, "fx" + fx + "algorithm", params);
        vbox.add(comp);
        hbox.add(vbox);

        if (fx == 1)
            {
            comp = new LabelledDial("Decay", this, "fx" + fx + "decay", color, 0, 90);
            hbox.add(comp);
                        
            comp = new LabelledDial("High Freq", this, "fx" + fx + "hfdamp", color, 0, 127);
            ((LabelledDial)comp).addAdditionalLabel("Dampening");
            hbox.add(comp);

            comp = new LabelledDial("FX B -> A", this, "fx" + fx + "ab", color, 0, 127);
            hbox.add(comp);
            }
        else
            {
            comp = new LabelledDial("Feedback", this, "fx" + fx + "feedback", color, 0, 127);
            hbox.add(comp);
                        
            comp = new LabelledDial("LFO Rate", this, "fx" + fx + "lforate", color, 0, 127);
            hbox.add(comp);

            // This is COMPLETELY undocumented.  :-(  The range is not 0...127 as indicated in the spec,
            // but is -12 ... 127 to include the synced ranges.
            comp = new LabelledDial("Delay", this, "fx" + fx + "delay", color, -12, 127)
                {
                public String map(int val)
                    {
                    return FX_DELAYS[val + 12];
                    }
                };
            hbox.add(comp);
            }

        comp = new LabelledDial("Send 1", this, "fx" + fx + "mixmain", color, 0, 100);
        hbox.add(comp);

        comp = new LabelledDial("Send 2", this, "fx" + fx + "mixsub1", color, 0, 100);
        hbox.add(comp);

        comp = new LabelledDial("Send 3", this, "fx" + fx + "mixsub2", color, 0, 100);
        hbox.add(comp);

        if (fx == 2)
            {
            comp = new LabelledDial("Send 4", this, "fx" + fx + "mixsub4", color, 0, 100);
            hbox.add(comp);
            }
        else            // this is literally insane.  mixsub*3*?
            {
            comp = new LabelledDial("Send 4", this, "fx" + fx + "mixsub3", color, 0, 100);
            hbox.add(comp);
                
            }

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }



    void updateArpChooser(String key, Model model)
        {
        if (arpChooser != null)
            {
            int val = model.get(key);
            int cur = model.get("arppattern");
            updateArps(val);
            arpChooser.setElements(ARP_NAMES[val]);
            if (cur >= ARP_NAMES[val].length)
                model.set("arppattern", 0); 
            }
        }

    public JComponent addCommonArpeggiator(Color color)
        {
        Category category = new Category(this, "Arpeggiator", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox outer = new VBox();
        VBox vbox = new VBox();
        
        comp = new CheckBox("Status", this, "arpstatus");
        vbox.add(comp);

        comp = new CheckBox("Latch", this, "arplatch");
        vbox.add(comp);

        comp = new CheckBox("Thru", this, "arpkbdthru");
        vbox.add(comp);

        hbox.add(vbox);
        vbox = new VBox();

        params = ARP_MODES;
        comp = new Chooser("Mode", this, "arpmode", params);
        vbox.add(comp);

        params = ARP_SYNC;
        comp = new Chooser("Sync", this, "arpsync", params);
        vbox.add(comp);

        params = ARP_RECYCLES;
        comp = new Chooser("Recycle", this, "arprecycle", params);
        vbox.add(comp);
        
        hbox.add(vbox);
        vbox = new VBox();

        params = ARP_DURATIONS;
        comp = new Chooser("Pre-Delay", this, "arppredelay", params);
        vbox.add(comp);

        params = ARP_DURATIONS;
        comp = new Chooser("Duration", this, "arpduration", params);
        vbox.add(comp);

        params = ARP_DURATIONS;
        comp = new Chooser("Post-Delay", this, "arppostdelay", params);
        vbox.add(comp);

        hbox.add(vbox);
        vbox = new VBox();

        params = ROMS;
        comp = new Chooser("Pattern ROM ID", this, "arppatternromid", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                updateArpChooser(key, model);
                }
            };
        vbox.add(comp);

        params = new String[] { "Nada" };       // for the moment, this will be changed soon
        comp = new Chooser("Pattern", this, "arppattern", params);
        arpChooser = (Chooser)comp;
        vbox.add(comp);

        updateArps(defaultArp);
        arpChooser.setElements(ARP_NAMES[defaultArp]);
        // don't need to update model, as we're presently at 0

        params = ARP_NOTE_VALUES;
        comp = new Chooser("Note Value", this, "arpnote", params);
        vbox.add(comp);

        hbox.add(vbox);

        comp = new LabelledDial("Velocity", this, "arpvel", color, 0, 127)
            {
            public String map(int val)
                {
                if (val == 0) return "Played";
                else return "" + val;
                }
            };
                
        hbox.add(comp);
                        
        comp = new LabelledDial("Gate Time", this, "arpgatetime", color, 1, 100)
            {
            public String map(int val)
                {
                return "" + val + "%";
                }
            };
        hbox.add(comp);
                        
        comp = new LabelledDial("Extension", this, "arpextcount", color, 0, 15);
        ((LabelledDial)comp).addAdditionalLabel("Count");
        hbox.add(comp);

        comp = new LabelledDial("Extension", this, "arpextint", color, 1, 16);
        ((LabelledDial)comp).addAdditionalLabel("Interval");
        hbox.add(comp);
                        
        comp = new LabelledDial("Key Range", this, "arpkrlow", color, 0, 127)
            {
            public String map(int value)
                {
                return NOTES[value % 12] + (value / 12 - 2);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Low");
        hbox.add(comp);

        comp = new LabelledDial("Key Range", this, "arpkrhigh", color, 0, 127)
            {
            public String map(int value)
                {
                return NOTES[value % 12] + (value / 12 - 2);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("High");
        hbox.add(comp);
                        
        comp = new LabelledDial("Pattern", this, "arppatternspeed", color, -2, 2)
            {
            public String map(int val)
                {
                return ARP_PATTERN_SPEEDS[val + 2];
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Speed");
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }




    void updateInstrumentChooser(int layer, String key, Model model)
        {
        if (instrumentChooser[layer - 1] != null)
            {
            int val = model.get(key);
            int cur = model.get("layer" + layer + "instrument");
            updateInstruments(val);
            instrumentChooser[layer - 1].setElements(INSTRUMENT_NAMES[val]);
            if (cur >= INSTRUMENT_NAMES[val].length)
                model.set("layer" + layer + "instrument", 0); 
            }
        }

    public JComponent addLayerGeneral(final int layer, Color color)
        {
        Category category = new Category(this, "General", color);
        category.makePasteable("layer");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox outer = new VBox();
        VBox vbox = new VBox();
        
        params = ROMS;
        comp = new Chooser("Instrument ROM ID", this, "layer" + layer + "instrumentromid", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                updateInstrumentChooser(layer, key, model);
                }
            };
        vbox.add(comp);

        params = new String[] { "Nada" };       // for the moment, this will be changed soon
        comp = new Chooser("Instrument", this, "layer" + layer + "instrument", params);
        instrumentChooser[layer - 1] = (Chooser)comp;
        vbox.add(comp);

        updateInstruments(defaultInstrument[layer - 1]);
        instrumentChooser[layer - 1].setElements(INSTRUMENT_NAMES[defaultInstrument[layer - 1]]);
        
        hbox.add(vbox);
        vbox = new VBox();

        comp = new LabelledDial("Key", this, "layer" + layer + "keylow", color, 0, 127)
            {
            public String map(int value)
                {
                return NOTES[value % 12] + (value / 12 - 2);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Low");
        hbox.add(comp);

        comp = new LabelledDial("Key", this, "layer" + layer + "keylowfade", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Low Fade");
        hbox.add(comp);

        comp = new LabelledDial("Key", this, "layer" + layer + "keyhigh", color, 0, 127)
            {
            public String map(int value)
                {
                return NOTES[value % 12] + (value / 12 - 2);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("High");
        hbox.add(comp);

        comp = new LabelledDial("Key", this, "layer" + layer + "keyhighfade", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("High Fade");
        hbox.add(comp);

        comp = new LabelledDial("Velocity", this, "layer" + layer + "vellow", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Low");
        hbox.add(comp);

        comp = new LabelledDial("Velocity", this, "layer" + layer + "vellowfade", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Low Fade");
        hbox.add(comp);

        comp = new LabelledDial("Velocity", this, "layer" + layer + "velhigh", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("High");
        hbox.add(comp);

        comp = new LabelledDial("Velocity", this, "layer" + layer + "velhighfade", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("High Fade");
        hbox.add(comp);

        comp = new LabelledDial("Real-Time", this, "layer" + layer + "rtlow", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Low");
        hbox.add(comp);

        comp = new LabelledDial("Real-Time", this, "layer" + layer + "rtlowfade", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Low Fade");
        hbox.add(comp);

        comp = new LabelledDial("Real-Time", this, "layer" + layer + "rthigh", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("High");
        hbox.add(comp);

        comp = new LabelledDial("Real-Time", this, "layer" + layer + "rthighfade", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("High Fade");
        hbox.add(comp);
                
        outer.add(hbox);
        hbox = new HBox();



        vbox = new VBox();
        params = SOLOS;
        comp = new Chooser("Solo", this, "layer" + layer + "solo", params);
        vbox.add(comp);

        params = SUBMIXES;
        comp = new Chooser("Submix", this, "layer" + layer + "submix", params);
        vbox.add(comp);

        comp = new CheckBox("Non-Transpose", this, "layer" + layer + "nontranspose");
        vbox.add(comp);
        
        hbox.add(vbox);
        vbox = new VBox();

        params = GROUPS;
        comp = new Chooser("Assign Group", this, "layer" + layer + "group", params);
        vbox.add(comp);

        params = GLIDE_CURVES;
        comp = new Chooser("Glide Curve", this, "layer" + layer + "glidecurve", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new CheckBox("Loop", this, "layer" + layer + "loop");
        vbox.add(comp);

        hbox.add(vbox);

        comp = new LabelledDial("Volume", this, "layer" + layer + "volume", color, -96, 10)
            {
            public double getStartAngle() { return 335.0; }
            public int getDefaultValue() { return 0; }
            };
        hbox.add(comp);
        

        comp = new LabelledDial("Pan", this, "layer" + layer + "pan", color, -64, 63)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val)
                {
                if (val == 0) return "--";
                else if (val > 0) return "" + val + " >";
                else return "< " + (-val);
                }
            };
        hbox.add(comp);
                

        comp = new LabelledDial("Coarse Tune", this, "layer" + layer + "ctune", color, -36, 36);
        hbox.add(comp);

        comp = new LabelledDial("Fine Tune", this, "layer" + layer + "ftune", color, -63, 63);          // docs say -64 to +64 but that's wrong
        hbox.add(comp);

        comp = new LabelledDial("Chorus", this, "layer" + layer + "ldetune", color, 0, 100)
            {
            public String map(int val)
                {
                if (val == 0) return "Off";
                else return "" + val;
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Chorus Width", this, "layer" + layer + "ldetunewidth", color, 0, 100);
        hbox.add(comp);

        comp = new LabelledDial("Transpose", this, "layer" + layer + "transpose", color, -36, 36);
        hbox.add(comp);

        comp = new LabelledDial("Bend", this, "layer" + layer + "bend", color, -1, 12);
        hbox.add(comp);

        comp = new LabelledDial("Glide Rate", this, "layer" + layer + "gliderate", color, 0, 127)
            {
            public String map(int val)
                {
                return GLIDE_RATES[val];
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Sound Start", this, "layer" + layer + "startoffset", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Sound Delay ", this, "layer" + layer + "startdelay", color, -25, 127)
            {
            public String map(int val)
                {
                if (val < 0) return LFO_DELAYS[val + 25];
                else return "" + val;
                }
            };
        hbox.add(comp);

                
        outer.add(hbox);

        category.add(outer, BorderLayout.CENTER);
        return category;
        }








    public JComponent addLayerFilter(final int layer, Color color)
        {
        Category category = new Category(this, "Filter", color);
        category.makePasteable("layer");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox outer = new VBox();
        VBox vbox = new VBox();
        JLabel frequencyLabel;
        JLabel resonanceLabel;
        
        final LabelledDial frequency = new LabelledDial("Frequency", this, "layer" + layer + "filtfreq", color, 0, 255)
            {
            public String map(int val)
                {
                int category = FILTER_CATEGORIES[model.get("layer" + layer + "filttype")];
                if (category == 0)
                    {
                    return FILTERS_TYPE_1[val];
                    }
                else if (category == 1)
                    {
                    return FILTERS_TYPE_2[val];
                    }
                else if (category == 3)
                    {
                    return FILTERS_TYPE_3[val];
                    }
                else 
                    {
                    return "" + val;
                    }
                }
            };
        frequencyLabel = frequency.addAdditionalLabel("[Cutoff]");
                
        final LabelledDial resonance = new LabelledDial("Q", this, "layer" + layer + "filtq", color, 0, 127)
            {
            public String map(int val)
                {
                int category = FILTER_CATEGORIES[model.get("layer" + layer + "filttype")];
                if (category == 3)
                    {
                    return GAINS[val];
                    }
                else 
                    {
                    return "" + val;
                    }
                }
            };
        resonanceLabel = resonance.addAdditionalLabel("[Resonance]");
        
        final HBox inner = new HBox();
        inner.add(frequency);
        inner.add(resonance);
        
        params = FILTER_TYPES;
        comp = new Chooser("Type", this, "layer" + layer + "filttype", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                switch(FILTER_CATEGORIES[model.get(key)])
                    {
                    case -1:        // Off
                        inner.removeAll();
                        break;
                    case 0:         // Classic, Smooth, Steeper
                        inner.removeAll();
                        inner.add(frequency);
                        inner.add(resonance);
                        frequencyLabel.setText("[Cutoff]");
                        resonanceLabel.setText("[Resonance]");
                        break;
                    case 1:         // Shallow, Deeper
                        inner.removeAll();
                        inner.add(frequency);
                        inner.add(resonance);
                        frequencyLabel.setText("[Cutoff]");
                        resonanceLabel.setText("[Resonance]");
                        break;
                    case 2:         // Band-pass1, Band-pass2, Contraband
                        inner.removeAll();
                        inner.add(frequency);
                        inner.add(resonance);
                        frequencyLabel.setText("[Cutoff]");
                        resonanceLabel.setText("[Resonance]");
                        break;
                    case 3:         // Swept1oct, Swept2>1oct, Swept3>1oct
                        inner.removeAll();
                        inner.add(frequency);
                        inner.add(resonance);
                        frequencyLabel.setText("[Cutoff]");
                        resonanceLabel.setText("[Gain]");
                        break;
                    case 4:         // PhazeShift1, PhazeShift2, BlissBatz, FlangerLite
                        inner.removeAll();
                        inner.add(frequency);
                        inner.add(resonance);
                        frequencyLabel.setText(" ");
                        resonanceLabel.setText(" ");
                        break;
                    case 5:         // Aah-Ay-Eeh, Ooh-To-Aah
                        inner.removeAll();
                        inner.add(frequency);
                        inner.add(resonance);
                        frequencyLabel.setText("[Morph]");
                        resonanceLabel.setText("[Body Size]");
                        break;
                    case 6:         // All other 12th order
                        inner.removeAll();
                        inner.add(frequency);
                        inner.add(resonance);
                        frequencyLabel.setText(" ");
                        resonanceLabel.setText("");
                        break;
                    }
                inner.revalidate();
                inner.repaint();
                }
            };
        vbox.add(comp);
        hbox.add(vbox);
        hbox.add(inner);
                
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }




    public JComponent addLayerLFO(int layer, int lfo, Color color)
        {
        Category category = new Category(this, "LFO " + lfo, color);
        category.makePasteable("layer" + layer + "lfo");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox outer = new VBox();
        VBox vbox = new VBox();
        
        params = LFO_SHAPES;
        comp = new Chooser("Shape", this, "layer" + layer + "lfo" + lfo + "shape", params);
        vbox.add(comp);

        params = LFO_SYNCS;
        comp = new Chooser("Sync", this, "layer" + layer + "lfo" + lfo + "sync", params);
        vbox.add(comp);

        hbox.add(vbox);
                
        comp = new LabelledDial("Rate", this, "layer" + layer + "lfo" + lfo + "rate", color, -25, 127)
            {
            public String map(int val)
                {
                if (val < 0) return LFO_DELAYS[val + 25];
                else return LFO_RATES[val];
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Delay", this, "layer" + layer + "lfo" + lfo + "delay", color, -25, 127)
            {
            public String map(int val)
                {
                if (val < 0) return LFO_DELAYS[val + 25];
                else return "" + val;
                }
            };
        hbox.add(comp);
                
        comp = new LabelledDial("Variance", this, "layer" + layer + "lfo" + lfo + "var", color, 0, 100)
            {
            public String map(int val)
                {
                return "" + val + "%";
                }
            };
        hbox.add(comp);
        ((LabelledDial)comp).addAdditionalLabel(" ");                   // this prevents the whole row from increasing in height when the user sets a filter other than "Off"

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }





    public JComponent addLayerEnvelope(final int layer, final int env, Color color)
        {
        final Category category = new Category(this, (env == 1 ? "Volume Envelope" : (env == 2 ? "Filter Envelope" : "Auxiliary Envelope")), color);
        category.makePasteable("layer" + layer + "env");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox outer = new VBox();
        VBox vbox = new VBox();
        
        EnvelopeDisplay _disp = null;
        
        if (env == 1)   // volume envelope has only positive levels
            {
            _disp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
                new String[] { null, "layer" + layer + "env" + env + "atk1rate", "layer" + layer + "env" + env + "atk2rate", "layer" + layer + "env" + env + "dcy1rate", "layer" + layer + "env" + env + "dcy2rate", "layer" + layer + "env" + env + "rls1rate", "layer" + layer + "env" + env + "rls2rate" },
                new String[] { null, "layer" + layer + "env" + env + "atk1lvl", "layer" + layer + "env" + env + "atk2lvl", "layer" + layer + "env" + env + "dcy1lvl", "layer" + layer + "env" + env + "dcy2lvl", "layer" + layer + "env" + env + "rls1lvl", "layer" + layer + "env" + env + "rls2lvl" },
                new double[] { 0, 1/6.0/127, 1/6.0/127, 1/6.0/127, 1/6.0/127, 1/6.0/127, 1/6.0/127 },
                new double[] { 0, 1.0/100, 1.0/100, 1.0/100, 1.0/100, 1.0/100, 1.0/100 } 
                );
            }
        else            // filter and auxiliary envelopes have positive and negative levels
            {
            _disp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
                new String[] { null, "layer" + layer + "env" + env + "atk1rate", "layer" + layer + "env" + env + "atk2rate", "layer" + layer + "env" + env + "dcy1rate", "layer" + layer + "env" + env + "dcy2rate", "layer" + layer + "env" + env + "rls1rate", "layer" + layer + "env" + env + "rls2rate" },
                new String[] { null, "layer" + layer + "env" + env + "atk1lvl", "layer" + layer + "env" + env + "atk2lvl", "layer" + layer + "env" + env + "dcy1lvl", "layer" + layer + "env" + env + "dcy2lvl", "layer" + layer + "env" + env + "rls1lvl", "layer" + layer + "env" + env + "rls2lvl" },
                new double[] { 0, 1/6.0/127, 1/6.0/127, 1/6.0/127, 1/6.0/127, 1/6.0/127, 1/6.0/127 },
                new double[] { 0, 0.5/100, 0.5/100, 0.5/100, 0.5/100, 0.5/100, 0.5/100 } 
                )
                {
                public void postProcess(double[] xVals, double[] yVals)
                    {
                    for(int i = 1; i < yVals.length; i++)
                        {
                        yVals[i] += 0.5;
                        }
                    }
                };
            _disp.setAxis(0.5);
            }
        
        final EnvelopeDisplay disp = _disp;

        params = (env == 1 ? VOL_ENV_MODES : ENV_MODES);                        // volume envelope has three modes 0 1 2, including "factory", others just have two modes but irritatingly they're 1, 2
        comp = new Chooser("Mode", this, "layer" + layer + "env" + env + "mode", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                category.repaint();
                }
            };
        vbox.add(comp);
        
        // It appears that volume envelope does not have a repeat
        
        if (env == 2 || env == 3)
            {
            comp = new CheckBox("Repeat", this, "layer" + layer + "env" + env + "repeat");
            disp.repaint();
            vbox.add(comp);
            }
                
        hbox.add(vbox);

        comp = new LabelledDial("Attack 1", this, "layer" + layer + "env" + env + "atk1rate", color, 0, 127)
            {
            public String map(int val)
                {
                if ((env == 1 && model.get("layer" + layer + "env" + env + "mode") == 2) ||             // tempo for volume env
                    (env != 1 && model.get("layer" + layer + "env" + env + "mode") == 1))           // tempo for non-volume env
                    return TEMPO_BASED_ENV_RATES[val];
                else return "" + val;
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Rate");
        hbox.add(comp);

        comp = new LabelledDial("Attack 1", this, "layer" + layer + "env" + env + "atk1lvl", color, (env == 1 ? 0 : -100), 100)
            {
            public String map(int val)
                {
                return "" + val + "%";
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);

        comp = new LabelledDial("Attack 2", this, "layer" + layer + "env" + env + "atk2rate", color, 0, 127)
            {
            public String map(int val)
                {
                if ((env == 1 && model.get("layer" + layer + "env" + env + "mode") == 2) ||             // tempo for volume env
                    (env != 1 && model.get("layer" + layer + "env" + env + "mode") == 1))           // tempo for non-volume env
                    return TEMPO_BASED_ENV_RATES[val];
                else return "" + val;
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Rate");
        hbox.add(comp);

        comp = new LabelledDial("Attack 2", this, "layer" + layer + "env" + env + "atk2lvl", color, (env == 1 ? 0 : -100), 100)
            {
            public String map(int val)
                {
                return "" + val + "%";
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);

        comp = new LabelledDial("Decay 1", this, "layer" + layer + "env" + env + "dcy1rate", color, 0, 127)
            {
            public String map(int val)
                {
                if ((env == 1 && model.get("layer" + layer + "env" + env + "mode") == 2) ||             // tempo for volume env
                    (env != 1 && model.get("layer" + layer + "env" + env + "mode") == 1))           // tempo for non-volume env
                    return TEMPO_BASED_ENV_RATES[val];
                else return "" + val;
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Rate");
        hbox.add(comp);

        comp = new LabelledDial("Decay 1", this, "layer" + layer + "env" + env + "dcy1lvl", color, (env == 1 ? 0 : -100), 100)
            {
            public String map(int val)
                {
                return "" + val + "%";
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);

        comp = new LabelledDial("Decay 2", this, "layer" + layer + "env" + env + "dcy2rate", color, 0, 127)
            {
            public String map(int val)
                {
                if ((env == 1 && model.get("layer" + layer + "env" + env + "mode") == 2) ||             // tempo for volume env
                    (env != 1 && model.get("layer" + layer + "env" + env + "mode") == 1))           // tempo for non-volume env
                    return TEMPO_BASED_ENV_RATES[val];
                else return "" + val;
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Rate");
        hbox.add(comp);

        comp = new LabelledDial("Decay 2", this, "layer" + layer + "env" + env + "dcy2lvl", color, (env == 1 ? 0 : -100), 100)
            {
            public String map(int val)
                {
                return "" + val + "%";
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);


        comp = new LabelledDial("Release 1", this, "layer" + layer + "env" + env + "rls1rate", color, 0, 127)
            {
            public String map(int val)
                {
                if ((env == 1 && model.get("layer" + layer + "env" + env + "mode") == 2) ||             // tempo for volume env
                    (env != 1 && model.get("layer" + layer + "env" + env + "mode") == 1))           // tempo for non-volume env
                    return TEMPO_BASED_ENV_RATES[val];
                else return "" + val;
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Rate");
        hbox.add(comp);

        comp = new LabelledDial("Release 1", this, "layer" + layer + "env" + env + "rls1lvl", color, (env == 1 ? 0 : -100), 100)
            {
            public String map(int val)
                {
                return "" + val + "%";
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);

        comp = new LabelledDial("Release 2", this, "layer" + layer + "env" + env + "rls2rate", color, 0, 127)
            {
            public String map(int val)
                {
                if ((env == 1 && model.get("layer" + layer + "env" + env + "mode") == 2) ||             // tempo for volume env
                    (env != 1 && model.get("layer" + layer + "env" + env + "mode") == 1))           // tempo for non-volume env
                    return TEMPO_BASED_ENV_RATES[val];
                else return "" + val;
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Rate");
        hbox.add(comp);

        //// NOTE: The docs say that if this is non-zero, release will never drop to zero and we'll drone
        
        comp = new LabelledDial("Release 2", this, "layer" + layer + "env" + env + "rls2lvl", color, (env == 1 ? 0 : -100), 100)
            {
            public String map(int val)
                {
                return "" + val + "%";
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);


        hbox.addLast(disp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }






    public JComponent addLayerPatchCords(int layer, Color color)
        {
        Category category = new Category(this, "Patch Cords", color);
        category.makePasteable("layer");
        category.makeDistributable("layer");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox outer = new VBox();
        VBox vbox = new VBox();
        
        for(int i = 0; i < 24; i+= 3)
            {
            for(int j = i; j < i + 3; j++)
                {
                vbox = new VBox();
                params = LAYER_PATCHCORD_SOURCES;
                comp = new Chooser("Source " + (j + 1), this, "layer" + layer + "cord" + j + "src", params);
                vbox.add(comp);
                        
                params = LAYER_PATCHCORD_DESTINATIONS;
                comp = new Chooser("Destination " + (j + 1), this, "layer" + layer + "cord" + j + "dst", params);
                vbox.add(comp);
                hbox.add(vbox);
                        
                comp = new LabelledDial("Amount " + (j + 1) + " ", this, "layer" + layer + "cord" + j + "amt", color, -100, 100);
                hbox.add(comp);
                                
                if (j < i + 2)
                    {
                    hbox.add(Strut.makeHorizontalStrut(16));
                    }
                }
            outer.add(hbox);

            /*
              if (i < 21)
              {
              outer.add(Strut.makeVerticalStrut(7));
              }
            */

            hbox = new HBox();
            }

        category.add(outer, BorderLayout.CENTER);
        return category;
        }


        
    public byte getID() 
        { 
        try 
            { 
            byte b = (byte)(Byte.parseByte(tuple.id));
            if (b >= 0 && b < 16) return b;
            }
        catch (NullPointerException e) { } // expected.  Happens when tuple's not built yet
        catch (NumberFormatException e) { Synth.handleException(e); }
        return 0;
        }
        
    public String reviseID(String id)
        {
        try 
            { 
            byte b =(byte)(Byte.parseByte(id)); 
            if (b >= 0 && b < 16) return "" + b;
            } 
        catch (NumberFormatException e) { }             // expected
        return "" + getID();
        }






    ////// MACHINE ROM CONFIGURATION

    /// The Proteus 2000 machines have custom configurations of ROMs depending on the machine type and user's whim.
    /// Edisyn doesn't *really* need to know the configuration of the machine, since these ROMs are all read-only
    /// and the user can just ask for patches from whatever ROM he wants whether or not it exists in the machine.
    /// But knowing the configuration is useful because it makes it convenient to present the user with the ROMS
    /// on his machine so he doesn't have to deal with the others; and it also helps Edisyn iterate through all
    /// the patches for things like batch downloads.


    /** Requests the configuration from the synthesizer. */
    public void requestConfiguration()
        {
        tryToSendSysex(new byte[] { (byte)0xF0, 0x18, 0x0F, getID(), 0x55, 0x0A, (byte)0xF7 });
        }
        
    /** Processes the configuration returned by the synthesizer. The ROMs returned are stored
        in the global array "simms", and are also stored in preferences.  These are then
        reloaded from preferences during initialization.
    */
    public int processConfiguration(byte[] data)
        {
        try
            {
            int numGeneralInformationBytes = data[6];   // 2
            int numSimms = data[7 + numGeneralInformationBytes];
            int numInfoBytesPerSimm = data[8 + numGeneralInformationBytes];
            int startOfPerSimmInformationBytes = 9 + numGeneralInformationBytes;
                        
            if (numSimms > 4) // uh oh
                numSimms = 4;
            int[] simmID = { 0, 0, 0, 0 };
            String[] simmName = { "Empty", "Empty", "Empty", "Empty" };

            for(int i = 0; i < numSimms; i++)
                {
                simmID[i] = data[startOfPerSimmInformationBytes + numInfoBytesPerSimm * i] + 
                    (data[startOfPerSimmInformationBytes + numInfoBytesPerSimm * i + 1] << 7);
                }
                                
            // verify SIMMs
            for(int i = 0; i < numSimms; i++)
                {
                boolean found = false;
                for(int j = 0; j < ROM_AND_USER_IDS.length; j++)
                    {
                    if (ROM_AND_USER_IDS[j] == simmID[i])
                        {
                        simmName[i] = ROMS_AND_EMPTY[j];
                        simms[i] = j;
                        setLastX("" + simmID[i], "SIMM" + i, getSynthClassName(), true);        // only set in synth
                        simmMenu[i].setText("SIMM " + i + ": " + simmName[i]);
                        // Will this call setLastX again?
                        ((JRadioButtonMenuItem)(simmMenu[i].getItem(j))).setSelected(true);
                        found = true;
                        break;
                        }
                    }
                if (!found)
                    {
                    showSimpleMessage("Unknown SIMM", "Unknown SIMM ID " + simmID[i] + " in SIMM Socket " + i);
                    simmID[i] = 0;
                    }
                }
                                
            // Indicate to the user the SIMMs we have (or failed to have) here maybe?
            if (numSimms == 0)
                {
                showSimpleMessage("No SIMMs Found", "The synth did not indicate that it had any SIMMs (or the message was garbled).");
                return PARSE_FAILED;
                }
            else
                showSimpleMessage("SIMMs Found",
                    "The Synth has stated that it has the following SIMMs:\n\n" +
                    ("SIMM 1: " + (simmName[0] == null ? "--" :  simmName[0])) + "\n" + 
                    ("SIMM 2: " + (simmName[1] == null ? "--" :  simmName[1])) + "\n" + 
                    ("SIMM 3: " + (simmName[2] == null ? "--" :  simmName[2])) + "\n" + 
                    ("SIMM 4: " + (simmName[3] == null ? "--" :  simmName[3])));
            }
        catch (Exception ex)
            {
            ex.printStackTrace();
            }
        return PARSE_SUCCEEDED;
        }









    ////// PARSING

    /// Parsing is a two-step process.  First, Edisyn reads in a HEADER sysex message, then some N DATA sysex
    /// messages.  When it has received what it believes to be the final data sysex message, it calls processParse()
    /// on the header and all the data to actually parse the sysex dump.  The Proteus 2000 can send these messages
    /// in either open loop (asynchronously) or closed loop (synchronously): Edisyn naturally requests them open loop.
    /// The parse message also recognizes ROM configuration messages (see section above) and calls processConfiguration(...)
    /// as appropriate.
    ///
    /// By the time parse has called processParse() it has created and loaded several variables.  First, header is the
    /// header message, which processParse will parse to extract the number of data bytes in each Proteus 2000 section.
    /// Second, loadedData is essentially an ArrayList of bytes.  It contains all the bytes of the actual data.  Also
    /// the bytes variable contains the number of bytes: this is really kinda useless since loadedData can be queried for
    /// this too.  Finally, expectBytes contains the number of bytes expected to parse from the various data sysex messages
    /// before Edisyn decides it's got the entire patch and calls processParse().  This number is extracted from the header.


    byte[] header = null;
    ByteBag loadedData = null;
    int bytes = 0;
    int expectBytes = 0;
    public int parse(byte[] data, boolean fromFile)
        {
        if (data[5] == 0x09)    // Configuration response
            {
            return processConfiguration(data);
            }
        else if (numSysexMessages(data) > 1)            // break it up, though this is not going to happen
            {
            int result = PARSE_FAILED;
            byte[][] d = cutUpSysex(data);
            for(int i = 0; i < d.length; i++)
                {
                result = parse(d[i], fromFile);
                }
            return result;
            }
        /// We only accept OPEN LOOP preset dumps
        else if (data[6] == 0x03)       // Is this the header?
            {
            header = data;
            bytes = 0;
            expectBytes = data[9] + (data[10] << 7) + (data[11] << 14) + (data[12] << 21);
            loadedData = new ByteBag();
            return PARSE_INCOMPLETE;
            }
        else if (data[6] == 0x04)       // is this a data message
            {
            if (loadedData == null) return PARSE_FAILED;                // no header
            byte[] d = new byte[data.length - 11];                              // our data bytes
            System.arraycopy(data, 9, d, 0, d.length);
            loadedData.addAll(d);
            bytes += d.length;            // the data bytes proper
            if (bytes >= expectBytes)
                {
                int retval = processParse(header, loadedData.toArray()); 
                header = null;
                loadedData = null;
                bytes = 0;
                expectBytes = 0;
                return retval;
                }               
            else return PARSE_INCOMPLETE;
            }
        else
            {
            // uh, that shouldn't have happened
            header = null;
            loadedData = null;
            bytes = 0;
            expectBytes = 0;
            System.err.println("EmuProteus2000.parse(): this shouldn't be reachable");
            return PARSE_FAILED;
            }
        }
    
    
    
    /// PROCESS PARSE
    
    /// processParse() is nontrivial.  E-mu's data bytes are partitioned into little categories, like effects, links, layer 1 filter, layer 2 lfo,
    /// and so on.  The size of these partitions can technically vary so you have to predetermine their sizes from the header. [Also note that these
    /// partitions are entirely independent of the respective sizes of the individual data packets]. After that you
    /// iterate your way through the data as a state machine (the state is the partition you're presently in), and based on the state and the
    /// current byte number after the last state changed, you determine which parameter you're loading.  Most parameters are two-byte two's complement,
    /// but the patch name is a string of single bytes (with no explanation or warning in the docs).  There appears to be an extra superfluous
    /// pair of junk bytes after each ENVELOPE state [a bug].  Also many parameters have to be run through a nonlinear mapping table for no
    /// good reason, ugh.  

    public static final int STATE_COMMON_GENERAL = 0;
    public static final int STATE_RESERVED = 1;
    public static final int STATE_COMMON_EFFECTS = 2;
    public static final int STATE_COMMON_LINK = 3;
    public static final int STATE_LAYER_1_GENERAL = 4;
    public static final int STATE_LAYER_1_FILTER = 5;
    public static final int STATE_LAYER_1_LFO = 6;
    public static final int STATE_LAYER_1_ENVELOPE = 7;
    public static final int STATE_LAYER_1_PATCH_CORDS = 8;
    public static final int STATE_LAYER_2_GENERAL = 9;
    public static final int STATE_LAYER_2_FILTER = 10;
    public static final int STATE_LAYER_2_LFO = 11;
    public static final int STATE_LAYER_2_ENVELOPE = 12;
    public static final int STATE_LAYER_2_PATCH_CORDS = 13;
    public static final int STATE_LAYER_3_GENERAL = 14;
    public static final int STATE_LAYER_3_FILTER = 15;
    public static final int STATE_LAYER_3_LFO = 16;
    public static final int STATE_LAYER_3_ENVELOPE = 17;
    public static final int STATE_LAYER_3_PATCH_CORDS = 18;
    public static final int STATE_LAYER_4_GENERAL = 19;
    public static final int STATE_LAYER_4_FILTER = 20;
    public static final int STATE_LAYER_4_LFO = 21;
    public static final int STATE_LAYER_4_ENVELOPE = 22;
    public static final int STATE_LAYER_4_PATCH_CORDS = 23;

    // These are the starts of the parameters for all the states above in the parameters[] array.
    // For example, the parameters in the STATE_LAYER_1_GENERAL category start at parameters[131].
    public static final int[] STATE_PARAM_STARTS = new int[] 
    { 0, 73, 93, 110, 
      131, 163, 167, 178, 221, 
      294, 326, 330, 341, 384,
      457, 489, 493, 504, 547,
      620, 652, 656, 667, 710 };
    
    // hunt through a list of indices for one matching a given value, then return the index or issue an error
    int findValue(int value, int[] indices, String tag)
        {
        for(int i = 0; i < indices.length; i++)
            {
            if (indices[i] == value)
                return i;
            }
        // didn't find it...
        System.err.println("Error in findValue: couldn't find value " + value + " in " + tag);
        return 0;
        }

    /// MAN, the Proteus2K sysex parsing scheme is hideously complex
    public int processParse(byte[] header, byte[] data)
        {
        try
            {
            // Let's extract the relevant information from the header
            // I don't think we have to convert any of these to negative...
            int presetNumber = header[7] + (header[8] << 7);        // maybe this should be MSB first?
            int numPresetCommonGeneral = header[13] + (header[14] << 7);            // I think this does NOT include the name
            int numReserved = header[15] + (header[16] << 7);
            int numPresetCommonEffects = header[17] + (header[18] << 7);
            int numPresetCommonLink = header[19] + (header[20] << 7);
            int numLayers = header[21] + (header[22] << 7);
            int numPresetLayerGeneral = header[23] + (header[24] << 7);
            int numPresetLayerFilter = header[25] + (header[26] << 7);
            int numPresetLayerLFO = header[27] + (header[28] << 7);
            int numPresetLayerEnvelope = header[29] + (header[30] << 7);
            int numPresetLayerPatchCords = header[31] + (header[32] << 7);
            int presetROMID = header[33] + (header[34] << 7);       // maybe this should be MSB first?


            // I have found that numPresetLayerEnvelope is reported to be one more than it actually is
            // -- but there's no mystery parameter.  It looks to be an error.  So we need to double-check
            // and bound these to be certain
            int _numPresetCommonGeneral = 52;               // This does not include the name chars, so we subtract 16.  We'll leave 4 for CTRL13...16 just in case
            int _numReserved = 19;                      // I don't know if these are arpeggio or not: there are exactly 20 arp params...
            int _numPresetCommonEffects = 16;
            int _numPresetCommonLink = 20;
            int _numLayers = 4;
            int _numPresetLayerGeneral = 31;
            int _numPresetLayerFilter = 3;
            int _numPresetLayerLFO = 10;
            int _numPresetLayerEnvelope = 42;               // one more parameter than actually exists      -- this is a BUG in the Proteus 2000 I believe
            int _numPresetLayerPatchCords = 72;

            // Let's check:
            if (numPresetCommonGeneral != _numPresetCommonGeneral) 
                { System.err.println("processParse(): numPresetCommonGeneral params reported by synth is not standard: " + numPresetCommonGeneral + " vs " + _numPresetCommonGeneral); }
            if (numReserved != _numReserved) 
                { System.err.println("processParse(): numReserved params reported by synth is not standard: " + numReserved + " vs " + _numReserved); }
            if (numPresetCommonEffects != _numPresetCommonEffects) 
                { System.err.println("processParse(): numPresetCommonEffects params reported by synth is not standard: " + numPresetCommonEffects + " vs " + _numPresetCommonEffects); }
            if (numPresetCommonLink != _numPresetCommonLink) 
                { System.err.println("processParse(): numPresetCommonLink params reported by synth is not standard: " + numPresetCommonLink + " vs " + _numPresetCommonLink); }
            if (numLayers != numLayers) 
                { System.err.println("processParse(): numLayers params reported by synth is not standard: " + numLayers + " vs " + _numLayers); }
            if (numPresetLayerGeneral != _numPresetLayerGeneral) 
                { System.err.println("processParse(): numPresetLayerGeneral params reported by synth is not standard: " + numPresetLayerGeneral + " vs " + _numPresetLayerGeneral); }
            if (numPresetLayerFilter != _numPresetLayerFilter) 
                { System.err.println("processParse(): numPresetLayerFilter params reported by synth is not standard: " + numPresetLayerFilter + " vs " + _numPresetLayerFilter); }
            if (numPresetLayerLFO != _numPresetLayerLFO) 
                { System.err.println("processParse(): numPresetLayerLFO params reported by synth is not standard: " + numPresetLayerLFO + " vs " + _numPresetLayerLFO); }
            // We KNOW this one is wrong but we already adjusted for it above
            if (numPresetLayerEnvelope != _numPresetLayerEnvelope) 
                { System.err.println("processParse(): numPresetLayerEnvelope params reported by synth is not standard: " + numPresetLayerEnvelope + " vs " + _numPresetLayerEnvelope); }
            if (numPresetLayerPatchCords != _numPresetLayerPatchCords) 
                { System.err.println("processParse(): numPresetLayerPatchCords params reported by synth is not standard: " + numPresetLayerPatchCords + " vs " + _numPresetLayerPatchCords); }
                
            if (numLayers > 4)
                {
                numLayers = 4;
                System.err.println("EmuProteus2000.processParse WARNING: incoming sysex with more than 4 layers. Will be restricted to 4.");
                }
            else if (numLayers < 4)
                {
                System.err.println("EmuProteus2000.processParse WARNING: incoming sysex with fewer than 4 layers.");
                }
                
            // If we're loading from current memory, the preset number and rom will be UNINFORMATIVE.
            // The preset will be 16383 ("Current Memory" 7F 7F) and the rom will be 0, unfortunately.
            // The best we can do is reset the number
            if (presetNumber == 16383)      // current memory
                presetNumber = 0;
        
            // Update bank
            int bank = -1;          // something....
            for(int i = 0; i < ROM_AND_USER_IDS.length; i++)
                {
                if (ROM_AND_USER_IDS[i] == presetROMID)
                    {
                    bank = i;
                    break;
                    }
                }
            if (bank == -1)
                {
                System.err.println("EmuProteus2000.processParse WARNING: can't determine incoming ROM ID " + presetROMID);
                }
            else
                {
                model.set("bank", bank);
                }
            // Update patch number
            model.set("number", presetNumber);
        
            // Load our state array
            int[] numParameters = new int[4 + 5 * numLayers];
            numParameters[STATE_COMMON_GENERAL] = numPresetCommonGeneral + 16;      // give slop for name
            numParameters[STATE_RESERVED] = numReserved;
            numParameters[STATE_COMMON_EFFECTS] = numPresetCommonEffects;
            numParameters[STATE_COMMON_LINK] = numPresetCommonLink;
            for(int i = 0; i < numLayers; i++)
                {
                numParameters[i * 5 + STATE_LAYER_1_GENERAL] = numPresetLayerGeneral;
                numParameters[i * 5 + STATE_LAYER_1_FILTER] = numPresetLayerFilter;
                numParameters[i * 5 + STATE_LAYER_1_LFO] = numPresetLayerLFO;
                numParameters[i * 5 + STATE_LAYER_1_ENVELOPE] = numPresetLayerEnvelope;
                numParameters[i * 5 + STATE_LAYER_1_PATCH_CORDS] = numPresetLayerPatchCords;
                } 

            // Load our maximum values just to be certain
            int[] maxParameters = new int[4 + 5 * _numLayers];
            maxParameters[STATE_COMMON_GENERAL] = _numPresetCommonGeneral + 16;      // give slop for name
            maxParameters[STATE_RESERVED] = _numReserved;
            maxParameters[STATE_COMMON_EFFECTS] = _numPresetCommonEffects;
            maxParameters[STATE_COMMON_LINK] = _numPresetCommonLink;
            for(int i = 0; i < numLayers; i++)
                {
                maxParameters[i * 5 + STATE_LAYER_1_GENERAL] = _numPresetLayerGeneral;
                maxParameters[i * 5 + STATE_LAYER_1_FILTER] = _numPresetLayerFilter;
                maxParameters[i * 5 + STATE_LAYER_1_LFO] = _numPresetLayerLFO;
                maxParameters[i * 5 + STATE_LAYER_1_ENVELOPE] = _numPresetLayerEnvelope;
                maxParameters[i * 5 + STATE_LAYER_1_PATCH_CORDS] = _numPresetLayerPatchCords;
                } 
                      
            // Load name
            char[] nm = new char[16];
            for(int i = 0; i < 16; i++)
                {
                nm[i] = (char)(data[i] & 255);
                }
            model.set("name", String.valueOf(nm));
                        
            // Load parameters
            int state = STATE_COMMON_GENERAL;
            int param = 16;                                                         // Start right after Name
            for(int i = 16; i < data.length; i+=2)          // Start right after Name
                {
                // update state
                if (param == numParameters[state])
                    {
                    param = 0;
                    state++;
                    if (state >= numParameters.length)
                        break;  // all done
                    }
                else if (param >= maxParameters[state])
                    {
                    /// uhh.....
                    System.err.println("EmuProteus2000.processParse WARNING: Extra parameter");
                    param++;
                    continue;
                    }
                
                // Grab next parameter
                String p = parameters[param + STATE_PARAM_STARTS[state]];
                                
                if (!(p.equals("---")))
                    {
                    int v = (data[i] | (data[i+1] << 7));
                    // we have to guarantee that negative numbers stay negative
                    if (v >= 8192) v = v - 16384;
                
                    // Unfortunately we have a LOT of custom parameters strewn throughout, and we have to do
                    // endswith rather than equals or startsWith.  :-(  So this will be costly.
                
                    if (p.equals("arpnote"))
                        {
                        // range is wrong in documentation, it actually goes 1...19, go figure
                        v -= 1;
                        }
//                    else if (p.equals("riff") || p.equals("link1preset") || p.equals("link2preset"))
                    else if (p.equals("link1preset") || p.equals("link2preset"))
                        {
                        // have to handle "on"
                        if (v < 0)
                            {
                            model.set(p + "on", 0);
                            v = 0;  // set the riff/preset to 0
                            }
                        else
                            {
                            model.set(p + "on", 1);
                            }
                        }
                    else if (p.endsWith("romid"))        
                        {
                        if (p.equals("link1presetromid") || p.equals("link2presetromid"))
                            v = findValue(v, ROM_AND_USER_IDS, p);
                        else
                            v = findValue(v, ROM_IDS, p);
                        }
                    else if (p.endsWith("source"))
                        {
                        v = findValue(v, PATCHCORD_SOURCE_INDICES, p);
                        }
                    else if (p.endsWith("dest"))
                        {
                        v = findValue(v, PATCHCORD_DESTINATION_INDICES, p);
                        }
                    else if (p.endsWith("filttype"))
                        {
                        v = findValue(v, FILTER_INDICES, p);
                        }
                    else if (p.endsWith("shape"))
                        {
                        v = findValue(v, LFO_INDICES, p);
                        }
                    else if (p.endsWith("mode") && !p.equals("arpmode") && !p.endsWith("env1mode")) // not arp mode or volume envelope mode
                        {
                        v = findValue(v, ENV_INDICES, p);
                        }
                    else if (p.endsWith("src"))     // thankfully, layer patchcords have src versus source
                        {
                        v = findValue(v, LAYER_PATCHCORD_SOURCE_INDICES, p);
                        }
                    else if (p.endsWith("dst")) // thankfully, layer patchcords have dst vs dest
                        {
                        v = findValue(v, LAYER_PATCHCORD_DESTINATION_INDICES, p);
                        }
                    else if (p.equals("tempooffset"))
                        {
                        // The range -1...+1 needs to be converted to 0 ... 2
                        v = v + 1;
                        }
                    else
                        {
                        // nothing
                        }
                        
                    if (!p.equals("layer1fakeparam") && !p.equals("layer2fakeparam") && !p.equals("layer3fakeparam") && !p.equals("layer4fakeparam"))
                        {
                        model.set(p, v);
                        }
                    }
                param++;
                }
            return PARSE_SUCCEEDED;
            }
        catch (Exception ex)
            {
            ex.printStackTrace();
            return PARSE_FAILED;
            }
        finally
            {
            revise();
            }
        }







    /// REVISION
    
    /// Revision is normally simple in edisyn, but not here.  Thie is because the length of the number
    /// of presets, arps, riffs, and instruments depends on the particular ROM chosen for them.  What
    /// happens is that the ROM gets changed (perhaps during mutation), and so does the preset say, 
    /// but the min/max values for the preset list are based on the *previous* ROM and so get bounded
    /// to that range rather than to the new range.  We have to fix that.  Also note that this
    /// issue resulted in me modifying how revision bounded things in the first place in Synth.java.
    /// Previously things were bounded to min or max; now they are bounded with mod so as to provide
    /// some more interesting randomness.  


    public void revise(Model model)
        {
        // The problem here is that when the model is revised, at present the
        // min/max values for the following items are based on the *previous* ROM.
        // not any *new* ROM; but the new ROM will have an entirely different list,
        // and so these values will get bounded to the length of the old list and not
        // the new one.  Our hack to get around this is to first backup the correct 
        // values here...
                  
        int riff = model.get("riff");
        int arppattern = model.get("arppattern");
        int link1preset = model.get("link1preset");
        int link2preset = model.get("link2preset");
        int layer1instrument = model.get("layer1instrument");
        int layer2instrument = model.get("layer2instrument");
        int layer3instrument = model.get("layer3instrument");
        int layer4instrument = model.get("layer4instrument");
        
        // Next revise the model.  This will set up the ROMs but will not update the
        // min-max values because the choosers aren't getting updated...
        super.revise(model);
        
        // Next manually do what the ROM choosers do when they are updated, which is
        // to load the proper lists into the parameter choosers and thus revise the
        // min/max values.  We only do this if the model is the current synth model,
        // otherwise crazy things could happen I imagine...
        if (model == getModel())
            {
            // Updating the choosers is very slow.  So we only update them
            // if we're likely to actually use the choosers at all
            if (!getAvoidUpdating())
                {
                updateRiffChooser("riffromid", model);
                updateArpChooser("arppatternromid", model);
                updatePresetChooser(1, "link1presetromid", model);
                updatePresetChooser(2, "link2presetromid", model);
                updateInstrumentChooser(1, "layer1instrumentromid", model);
                updateInstrumentChooser(2, "layer2instrumentromid", model);
                updateInstrumentChooser(3, "layer3instrumentromid", model);
                updateInstrumentChooser(4, "layer4instrumentromid", model);
                }

            // Now we set the values, and these should stick because they're within
            // the proper min/max ranges
        
            model.set("riff", riff);
            model.set("arppattern", arppattern);
            model.set("link1preset", link1preset);
            model.set("link2preset", link2preset);
            model.set("layer1instrument", layer1instrument);
            model.set("layer2instrument", layer2instrument);
            model.set("layer3instrument", layer3instrument);
            model.set("layer4instrument", layer4instrument);
                
            // Finally revise again to deal with any truly bad settings!
            super.revise(model);
            }
        // Maybe this will work...
        }


    public int getPauseAfterWritePatch() { return 0; }

    //public int getPauseAfterReceivePatch() { return 1000; }         // 150 appears to be too fast
                
    // public int getPauseAfterSendOneParameter() { return 2; }

    public boolean getSendsParametersAfterNonMergeParse() { return true; }









    /// EMITTING A SINGLE PARAMETER
    
    /// Emitting one parameter isn't that interesting except that we have to tell the Proteus 2000 that
    /// we are modifying current working memory by emitting a special PRESET = -1 sysex command.
    /// THEN we may have to instruct the Proteus 2000 which layer we're changing by emitting
    /// another special LAYER = ... command.  Finally, we emit the command to change the parameter.
        

    byte[] buildParameterBytes(int param, int val)
        {
        if (val < 0) val = val + 16384;
                
        return new byte[] { (byte)0xF0, 0x18, 0x0F, getID(), 0x55, 0x01, 0x02, 
            (byte)(param & 127),
            (byte)((param >>> 7) & 127),
            (byte)(val & 127),
            (byte)((val >>> 7) & 127),
            (byte)0xF7 };
        }
                
    static final int PRESET_SELECT = 897;
    static final int LAYER_SELECT = 898;
    static final int PRESET_NAME_START = 899;
    public Object[] emitAll(String key)
        {
        if (key.equals("number")) return new Object[0];  // this is not emittable
        if (key.equals("bank")) return new Object[0];  // this is not emittable
        if (key.equals("---")) return new Object[0];            // should never happen
        
        // We will be using Parameter Value Edit (page 6 of sysex docs)

        int layer = parameterToLayer(key);
        if (key.equals("name"))
            {
            String name = model.get(key, "") + "                ";
            char[] n = name.toCharArray();
            Object[] ret = new Object[17];
            ret[0] = buildParameterBytes(PRESET_SELECT, -1);              // edit buffer
            for(int i = 0; i < 16; i++)
                {
                ret[i + 1] = buildParameterBytes(PRESET_NAME_START + i, (int)n[i]);
                }
            return ret;
            }
        else 
            {
            int id = 0;
            try { id = ((Integer)(parametersToID.get(key))).intValue(); }
            catch (NullPointerException ex) { } // riffon, link1preseton, link2preseton are not there
            
            Object[] ret = new Object[3];
            ret[0] = buildParameterBytes(PRESET_SELECT, -1);              // edit buffer
            ret[1] = (layer >= 0 ? buildParameterBytes(LAYER_SELECT, layer) : new Object[0]);
            
            // Unfortunately we have a LOT of custom parameters strewn throughout, and we have to do
            // endswith rather than equals or startsWith.  :-(  So this will be costly.
                        
            int v = model.get(key, 0);
            String p = key;
            if (p.equals("arpnote"))
                {
                // range is wrong in documentation, it actually goes 1...19, go figure
                v += 1;
                }
//            else if (p.equals("riff") || p.equals("riffon"))
//                {
//                id = ((Integer)(parametersToID.get("riff"))).intValue();
//                v = (model.get("riffon") == 0 ? -1 : model.get("riff"));
//                }
            else if (p.equals("link1preset") || p.equals("link1preseton"))
                {
                id = ((Integer)(parametersToID.get("link1preset"))).intValue();
                v = (model.get("link1preseton") == 0 ? -1 : model.get("link1preset"));
                }
            else if (p.equals("link2preset") || p.equals("link2preseton"))
                {
                id = ((Integer)(parametersToID.get("link2preset"))).intValue();
                v = (model.get("link2preseton") == 0 ? -1 : model.get("link2preset"));
                }
            else if (p.endsWith("romid"))        
                {
                if (p.equals("link1presetromid") || p.equals("link2presetromid"))
                    v = ROM_AND_USER_IDS[v];
                else
                    v = ROM_IDS[v];
                }
            else if (p.endsWith("source"))
                {
                v = PATCHCORD_SOURCE_INDICES[v];
                }
            else if (p.endsWith("dest"))
                {
                v = PATCHCORD_DESTINATION_INDICES[v];
                }
            else if (p.endsWith("filttype"))
                {
                v = FILTER_INDICES[v];
                }
            else if (p.endsWith("shape"))
                {
                v = LFO_INDICES[v];
                }
            else if (p.endsWith("mode") && !p.equals("arpmode") && !p.endsWith("env1mode"))     // not arp mode or volume envelope mode
                {
                v = ENV_INDICES[v];
                }
            else if (p.endsWith("src"))     // thankfully, layer patchcords have src versus source
                {
                v = LAYER_PATCHCORD_SOURCE_INDICES[v];
                }
            else if (p.endsWith("dst")) // thankfully, layer patchcords have dst vs dest
                {
                v = LAYER_PATCHCORD_DESTINATION_INDICES[v];
                }
            else if (p.equals("tempooffset"))
                {
                // The range 0...2 needs to be converted to -1 ... +1
                v = v - 1;
                }
            else
                {
                // nothing
                }
            
            ret[2] = buildParameterBytes(id, v);
            return ret;
            }
        }













    /// EMITTING A PATCH
    
    /// To emit a patch we have to first emit a header packet, then collect all our parameters into a data array,
    /// then break the data array into 244-byte chunks, the insert each chunk into its own data packet.  What a mess!
    /// Note that to emit to current working memory, our "Patch Number" is 16383 (7F 7F).  It'd sure be nice if earlier
    /// Proteus models had this feature, but sadly they do not.
    ///
    /// An apparent bug in the Proteus 2000: after you upload a patch, you can no longer make real-time parameter
    /// changes.  Thankfully Prodatum found a workaround for this: emitting a special sysex message; see very the end of
    /// emitAll(...).  This fixes things.
        

    public Object[] emitAll(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        if (tempModel == null)
            tempModel = getModel();

        int NN = tempModel.get("number", 0);
        int BB = ROM_AND_USER_IDS[tempModel.get("bank", 0)];
        /*
          if (!toFile)
          BB = 0;                     // has to be it's going to RAM
        */
                
        if (toWorkingMemory)
            {
            BB = 0;                                                     // has to be if it's going to RAM
            NN = 16383;                     // 7F 7F
            }

        // Let's define some parameters
        int numNameBytes = 16;
        int numPresetCommonGeneral = 52;                // This does not include the name chars, so we subtract 16.  We ALSO remove 4 params for CTRL13...16, which are not included in the dump
        int numReserved = 19;                   // I don't know if these are arpeggio or not: there are exactly 20 arp params...
        int numPresetCommonEffects = 16;
        int numPresetCommonLink = 20;
        int numPresetLayers = 4;
        int numPresetLayerGeneral = 31;
        int numPresetLayerFilter = 3;
        int numPresetLayerLFO = 10;
        int numPresetLayerEnvelope = 42;
        int numPresetLayerPatchCord = 72;
        int dataBytes = numNameBytes + 2 * (
            numPresetCommonGeneral + 
            numReserved + 
            numPresetCommonEffects + 
            numPresetCommonLink + 
            numPresetLayers * (
                numPresetLayerGeneral +
                numPresetLayerFilter +
                numPresetLayerLFO + 
                numPresetLayerEnvelope + 
                numPresetLayerPatchCord));
                
        // First we create the header
        byte[] header = new byte[36];
        header[0] = (byte)0xF0;
        header[1] = (byte)0x18;
        header[2] = (byte)0x0F;
        header[3] = (byte)getID();
        header[4] = (byte)0x55;
        header[5] = (byte)0x10;                       // Preset Dump
        header[6] = (byte)0x03;                       // Open Loop Header
        header[7] = (byte)(NN & 127);
        header[8] = (byte)((NN >>> 7) & 127);
        header[9] = (byte)(dataBytes & 127);
        header[10] = (byte)((dataBytes >>> 7) & 127);
        header[11] = (byte)((dataBytes >>> 14) & 127);
        header[12] = (byte)((dataBytes >>> 21) & 127);
        header[13] = (byte)((numPresetCommonGeneral) & 127);                    // don't include name
        header[14] = (byte)(((numPresetCommonGeneral) >>> 7) & 127);
        header[15] = (byte)(numReserved & 127);
        header[16] = (byte)((numReserved >>> 7) & 127);
        header[17] = (byte)(numPresetCommonEffects & 127);
        header[18] = (byte)((numPresetCommonEffects >>> 7) & 127);
        header[19] = (byte)(numPresetCommonLink & 127);
        header[20] = (byte)((numPresetCommonLink >>> 7) & 127);
        header[21] = (byte)(numPresetLayers & 127);
        header[22] = (byte)((numPresetLayers >>> 7) & 127);
        header[23] = (byte)(numPresetLayerGeneral & 127);
        header[24] = (byte)((numPresetLayerGeneral >>> 7) & 127);
        header[25] = (byte)(numPresetLayerFilter & 127);
        header[26] = (byte)((numPresetLayerFilter >>> 7) & 127);
        header[27] = (byte)(numPresetLayerLFO & 127);
        header[28] = (byte)((numPresetLayerLFO >>> 7) & 127);
        header[29] = (byte)(numPresetLayerEnvelope & 127);
        header[30] = (byte)((numPresetLayerEnvelope >>> 7) & 127);
        header[31] = (byte)(numPresetLayerPatchCord & 127);
        header[32] = (byte)((numPresetLayerPatchCord >>> 7) & 127);
        header[33] = (byte)(BB & 127);                  // always gonna be 0
        header[34] = (byte)((BB >>> 7) & 127);  // always gonna be 0
        header[35] = (byte)0xF7;                        // there is no checksum
  
        byte[] data = new byte[dataBytes];              // the 16 name chars are only 1 byte each, so remove half
        int offset = 0;
  
        // First the name
        String name = model.get("name", "") + "            ";
        for(int i = 0; i < 16; i++)
            {
            byte val = (byte)(name.charAt(i));
            data[offset++] = (byte)(val & 127);
            }
  
        // Now all the main parameters
        for(int i = 16; i < parameters.length; i++)                     // Start after name
            {
            String p = parameters[i];
            if (p.equals("---"))  // skip
                continue;
                
            if (p.equals("ctrl13") || p.equals("ctrl14") || p.equals("ctrl15") || p.equals("ctrl16"))
                continue;       // these are not included in the final dump
                                
            // we're going to just upload ctrl12 ... ctrl15 as they are, since they're not used
            int v = model.get(p, 0);                            // "---" will be 0        -- this includes layer1fakeparam, layer2fakeparam, layer3fakeparam, layer4fakeparam

            
            // Unfortunately we have a LOT of custom parameters strewn throughout, and we have to do
            // endswith rather than equals or startsWith.  :-(  So this will be costly.
                        
            if (p.equals("arpnote"))
                {
                // range is wrong in documentation, it actually goes 1...19, go figure
                v += 1;
                }
//            else if (p.equals("riff") || p.equals("riffon"))
//                {
//                v = (model.get("riffon") == 0 ? -1 : model.get("riff"));
//                }
            else if (p.equals("link1preset") || p.equals("link1preseton"))
                {
                v = (model.get("link1preseton") == 0 ? -1 : model.get("link1preset"));
                }
            else if (p.equals("link2preset") || p.equals("link2preseton"))
                {
                v = (model.get("link2preseton") == 0 ? -1 : model.get("link2preset"));
                }
            else if (p.endsWith("romid"))        
                {
                if (p.equals("link1presetromid") || p.equals("link2presetromid"))
                    v = ROM_AND_USER_IDS[v];
                else
                    v = ROM_IDS[v];
                }
            else if (p.endsWith("source"))
                {
                v = PATCHCORD_SOURCE_INDICES[v];
                }
            else if (p.endsWith("dest"))
                {
                v = PATCHCORD_DESTINATION_INDICES[v];
                }
            else if (p.endsWith("filttype"))
                {
                v = FILTER_INDICES[v];
                }
            else if (p.endsWith("shape"))
                {
                v = LFO_INDICES[v];
                }
            else if (p.endsWith("mode") && !p.equals("arpmode") && !p.endsWith("env1mode"))     // not arp mode or volume envelope mode
                {
                v = ENV_INDICES[v];
                }
            else if (p.endsWith("src"))     // thankfully, layer patchcords have src versus source
                {
                v = LAYER_PATCHCORD_SOURCE_INDICES[v];
                }
            else if (p.endsWith("dst")) // thankfully, layer patchcords have dst vs dest
                {
                v = LAYER_PATCHCORD_DESTINATION_INDICES[v];
                }
            else if (p.equals("tempooffset"))
                {
                // The range 0...2 needs to be converted to -1 ... +1
                v = v - 1;
                }
            else
                {
                // nothing
                }
                
            if (v < 0) v = v + 16384;
            data[offset++] = (byte)(v & 127);
            data[offset++] = (byte)((v >>> 7) & 127);
            }
            
        // Now that we have our data, we have to break it into packets
        // Each data packet can hold at most 244 bytes.
        // We have 1494 bytes.  Thus we need 7 data packets, plus the header and the footer
        Object[] result = new Object[9];
        result[0] = header;

        offset = 0;             // in bytes
        for(int i = 1; i < result.length - 1; i++)              // retain space for footer
            {
            int numBytes = Math.min(244, data.length - offset);
            byte[] packet = new byte[11 + numBytes];
            packet[0] = (byte)0xF0;
            packet[1] = (byte)0x18;
            packet[2] = (byte)0x0F;
            packet[3] = (byte)getID();
            packet[4] = (byte)0x55;
            packet[5] = (byte)0x10;                 // Preset Dump
            packet[6] = (byte)0x04;                 // Open Loop Data Packet
            packet[7] = (byte)(i & 127);                // packet count.  note i starts at 1
            packet[8] = (byte)((i >>> 7) & 127);        // packet count.   note i starts at 1
                
            System.arraycopy(data, offset, packet, 9, numBytes);
            offset += numBytes;

            // Compute the checksum
            int checksum = 0;
            for(int j = 9; j < packet.length - 2; j++)
                {
                checksum += packet[j];
                }

            packet[packet.length - 2] = (byte)((~(checksum & 127)) & 127);
            packet[packet.length - 1] = (byte)0xF7;
            result[i] = packet;
            }

        // Without this magical message, once we have modified the current working memory, the
        // synthesizer will refuse to respond to real-time changes, or at least won't update its
        // screen.  This message is output by Prodatum and is changing a specific parameter: the multimode "ROM ID"
        // (see page 41 of specification).  I seriously have no idea why this is necessary.
        byte[] footer = 
            (toFile ? new byte[0] :
            new byte[] { (byte)0xF0, 0x18, 0x0F, 0x00, 0x55, 0x01, 0x02, 0x0A, 0x01, 0x00, 0x00, (byte)0xF7 });
        result[8] = footer;
        
        // Intersperse pauses
        Object[] result2 = new Object[17];
        for(int i = 0; i < 9; i++)
            {
            result2[i * 2] = result[i];
            if (i != 8) result2[i * 2 + 1] = Integer.valueOf(150);          // 150 seems to be okay but 100 is too fast
            }
                        
        return result2;
        }
        



    public void changePatch(Model tempModel)
        {
        // We will presume that the "bank" is actually the preset ROM ID
        int number = tempModel.get("number", 0);
        int bank = tempModel.get("bank", 0);
        
        // cc 0 is the ROM or User
        tryToSendMIDI(buildCC(getChannelOut(), 0, ROM_AND_USER_IDS[bank]));
        
        // cc 32 is the "Actual" bank
        tryToSendMIDI(buildCC(getChannelOut(), 32, number / 128));

        // PC is the "Number
        tryToSendMIDI(buildPC(getChannelOut(), number % 128));
        }




    public byte[] requestDump(Model tempModel)
        {
        if (tempModel == null)
            tempModel = getModel();

        // We will presume that the "bank" is actually the preset ROM ID
        int number = tempModel.get("number", 0);
        int bank = tempModel.get("bank", 0);

        // We're going to use "Preset Dump Request (Closed Loop)", page 17 of the sysex spec
        
        int rom = ROM_AND_USER_IDS[bank];

        byte[] data = new byte[12];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x18;
        data[2] = (byte)0x0F;
        data[3] = (byte)getID();
        data[4] = (byte)0x55;
        data[5] = (byte)0x11;
        data[6] = (byte)0x04;
        data[7] = (byte)(number & 127);
        data[8] = (byte)((number >>> 7) & 127);
        data[9] = (byte)(rom & 127);
        data[10] = (byte)((rom >>> 7) & 127);
        data[11] = (byte)0xF7;

        return data;
        }



    // To request the current patch, we request patch number 7F 7F (16383).
    public byte[] requestCurrentDump()
        {
        // We're going to use "Preset Dump Request (Closed Loop)", page 17 of the sysex spec
        
        byte[] data = new byte[12];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x18;
        data[2] = (byte)0x0F;
        data[3] = (byte)getID();
        data[4] = (byte)0x55;
        data[5] = (byte)0x11;
        data[6] = (byte)0x04;
        data[7] = (byte)0x7F;
        data[8] = (byte)0x7F;
        data[9] = (byte)0x00;
        data[10] = (byte)0x00;
        data[11] = (byte)0xF7;

        return data;
        }
        
        
        
        
        
    JMenu[] simmMenu = new JMenu[4];
        
    public void addProteusMenu()
        {
        JMenu menu = new JMenu("Proteus 2000");
        menubar.add(menu);

        JMenuItem requestMenu = new JMenuItem("Request SIMM Configuration");
        requestMenu.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                requestConfiguration();
                }
            });
        menu.add(requestMenu);

        for(int i = 0; i < 4; i++)
            {
            final int _i = i;
            simmMenu[i] = new JMenu("SIMM " + i);
            ButtonGroup group = new ButtonGroup();
            for(int j = 0; j < ROMS_AND_EMPTY.length; j++)
                {
                final int _j = j;
                JRadioButtonMenuItem item = new JRadioButtonMenuItem(ROMS_AND_EMPTY[j]);
                group.add(item);
                simmMenu[i].add(item);
                if (j == simms[i]) 
                    {
                    item.setSelected(true);
                    simmMenu[i].setText("SIMM " + _i + ": " + ROMS_AND_EMPTY[_j]);
                    }
                item.addActionListener(new ActionListener()
                    {
                    public void actionPerformed(ActionEvent e)
                        {
                        setLastX("" + ROM_AND_USER_IDS[_j], "SIMM" + _i, getSynthClassName(), true);        // only set in synth
                        simms[_i] = _j;
                        simmMenu[_i].setText("SIMM " + _i + ": " + ROMS_AND_EMPTY[_j]);
                        }
                    });
                }
            menu.add(simmMenu[i]);
            }
        }



    public JFrame sprout()
        {
        JFrame frame = super.sprout();
        //receiveCurrent.setEnabled(false);               // can't request current in Emu synths

        // Load the SIMMs.  Each SIMM is stored as a 0 ("No SIMM") or as the ROM ID.
        // We store them into simms[i] as the index into the ROMS_AND_EMPTY array, with 0 being EMPTY
        // I'm doing it this way because I figure we might add future SIMMs without breaking users'
        // existing preferences, but maybe that's stupid.
        for(int i = 0; i < 4; i++)
            {
            simms[i] = 0;           // empty by default
            int v = getLastXAsInt("SIMM" + i, getSynthClassName(), 0, false);
            // Find the ID
            for(int a = 0; a < ROM_AND_USER_IDS.length; a++)
                {
                if (ROM_AND_USER_IDS[a] == v) 
                    {
                    simms[i] = a;           // because 0 is empty
                    break;
                    }
                }
            }
                
        addProteusMenu();
        
        return frame;
        }
    
                
    public static final int MAXIMUM_NAME_LENGTH = 16;
    public String revisePatchName(String name)
        {
        //// Proteus 2000 names are weird: the fourth character MUST be a colon :
        
        name = super.revisePatchName(name) + "                ";  // buffer it to max so we for sure have space for the colon
        name.substring(0, MAXIMUM_NAME_LENGTH);
        
        StringBuffer nameb = new StringBuffer(name);                            
        for(int i = 0 ; i < nameb.length(); i++)
            {
            if (i == 3) 
                nameb.setCharAt(i, ':');
            else
                {
                char c = nameb.charAt(i);
                if (c < 32 || c > 127)
                    nameb.setCharAt(i, ' ');
                }
            }
        name = nameb.toString();
        return super.revisePatchName(name);  // trim again I guess
        }

    /** Verify that all the parameters are within valid values, and tweak them if not. */
    public void revise()
        {
        // Next check the easy stuff -- out of range parameters
        super.revise();
        
        String nm = model.get("name", "Untitled");
        String newnm = revisePatchName(nm);
        if (!nm.equals(newnm))
            model.set("name", newnm);
        }

    ///  MODEL FIXING to handle the different length ROMs.  
    public Model buildModel() 
        { 
        Model model = super.buildModel(); 
        model.setFixer(this);
        return model;
        }
        
    public void fix(String key, Model model)
        {
        if (key.endsWith("romid"))
            {
            String[][] names = null;
            int val = model.get(key);
            String name = key.substring(0, key.length() - 5);               // remove "romid"
            if (key.equals("riffromid")) { updateRiffs(val); names = RIFF_NAMES; }
            else if (key.equals("arppatternromid")) { updateArps(val); names = ARP_NAMES; }
            else if (key.equals("link1presetromid") || key.equals("link2presetromid")) { updatePresets(val); names = PRESET_NAMES; }
            else { updateInstruments(val); names = INSTRUMENT_NAMES; }      // instruments
            model.setMinMax(name, 0, names[val].length);
            if (model.get(name) >= names[val].length)
                model.set(name, 0); 
            }
        }
    
        
    public static String getSynthName() { return "E-Mu Proteus 2000"; }
    public String getPatchName(Model model) { return model.get("name", "Untitled"); }
        
        
        
        
        
    //// ITERATING THROUGH PATCHES        
    
    //// We iterate by skipping ROMs that we believe the user does not have installed, and sticking only
    //// with the ones he indicated in the Proteus 2000 menu.
        
    public Model getNextPatchLocation(Model model)
        {
        int number = model.get("number");
        int bank = number / 128;
        number = number % 128;
        int rom = model.get("bank");

        number++;
        if (number >= 128)
            {
            number = 0;
            bank++;
            if (bank >= NUM_BANKS[rom])
                {
                if (rom == 0)       // user
                    {
                    boolean found = false;
                    // Hunt for next valid ROM
                    for(int i = 0; i < simms.length; i++)
                        {
                        if (simms[i] > 0)       // Something other than "No ROM"
                            {
                            rom = simms[i];
                            bank = 0;
                            number = 0;
                            found = true;
                            break;
                            }
                        }
                    if (!found)     // No SIMMs at all!
                        {
                        System.err.println("getNextPatchLocation ERROR: No SIMMs found at all");
                        rom = 0;        // user
                        bank = 0;
                        number = 0;
                        }
                    }
                else            // we're at a current ROM
                    {
                    // Find the ROM
                    int romID = ROM_AND_USER_IDS[rom];
                    nonuser: for(int i = 0; i < simms.length; i++)
                        {
                        if (simms[i] == rom)    // got it
                            {
                            // find the next rom
                            for(int k = i + 1; k < simms.length; k++)
                                {
                                if (simms[k] > 0)       // Something other than "No ROM"
                                    {
                                    rom = simms[k];
                                    bank = 0;
                                    number = 0;
                                    break nonuser;
                                    }
                                }
                            // at this point we don't have any more ROM SIMMS, so we're gonna do USER
                            rom = 0;
                            bank = 0;
                            number = 0;
                            break nonuser;
                            }
                        }
                    }
                }
            }
                
        Model newModel = buildModel();
        newModel.set("number", bank * 128 + number);
        newModel.set("bank", rom);
        return newModel;
        }

    public String getPatchLocationName(Model model)
        {
        // getPatchLocationName() is called from sprout() as a test to see if we should enable
        // batch downloading.  If we haven't yet created an .init file, then parameters won't exist
        // yet and this method will bomb badly.  So we return null in this case.
        if (!model.exists("number")) return null;
        
        String rom = ROMS_AND_USER_SHORT[model.get("bank", 0)];
        int number = (model.get("number"));
        int bank = number / 128;
        number = number % 128;
        return rom + " " + bank + "/" + (number > 99 ? "" : (number > 9 ? "0" : "00")) + number;
        }
    


    /** List of all Emu Proteus 2000 parameters in order. */
                
    final static String[] parameters = new String[]
    {
//// Note that these parameters are in order by ID, which is rather different from
//// the order in which they are presented in the documentation.  I turns out that
//// the ID order is the order in which they appear in the sysex dump messages: why
//// E-Mu is showing them in the other order is unknown to me.  So frustrating.
////
//// Also note that the first two parameters (PRESET and LAYER) are not here at all;
//// they do not appear in sysex patch dump messages as it turns out.

///// BEGIN COMMON GENERAL
///// [BEGIN NAME]
    "---",          // 899
    "---",          // 900
    "---",          // 901
    "---",          // 902
    "---",          // 903
    "---",          // 904
    "---",          // 905
    "---",          // 906
    "---",          // 907
    "---",          // 908
    "---",          // 909
    "---",          // 910
    "---",          // 911
    "---",          // 912
    "---",          // 913
    "---",          // 914
    "ctrl1",                // 915
    "ctrl2",                // 916
    "ctrl3",                // 917
    "ctrl4",                // 918
    "ctrl5",                // 919
    "ctrl6",                // 920
    "ctrl7",                // 921
    "ctrl8",                // 922
    "kbdtune",              // 923
    "ctrl9",                // 924
    "ctrl10",               // 925
    "ctrl11",               // 926
    "ctrl12",               // 927
    "riff",         // 928
    "riffromid",            // 929
    "tempooffset",          // 930
    "cord0source",          // 931
    "cord0dest",            // 932
    "cord0amount",          // 933
    "cord1source",          // 934
    "cord1dest",            // 935
    "cord1amount",          // 936
    "cord2source",          // 937
    "cord2dest",            // 938
    "cord2amount",          // 939
    "cord3source",          // 940
    "cord3dest",            // 941
    "cord3amount",          // 942
    "cord4source",          // 943
    "cord4dest",            // 944
    "cord4amount",          // 945
    "cord5source",          // 946
    "cord5dest",            // 947
    "cord5amount",          // 948
    "cord6source",          // 949
    "cord6dest",            // 950
    "cord6amount",          // 951
    "cord7source",          // 952
    "cord7dest",            // 953
    "cord7amount",          // 954
    "cord8source",          // 955
    "cord8dest",            // 956
    "cord8amount",          // 957
    "cord9source",          // 958
    "cord9dest",            // 959
    "cord9amount",          // 960
    "cord10source",         // 961
    "cord10dest",           // 962
    "cord10amount",         // 963
    "cord11source",         // 964
    "cord11dest",           // 965
    "cord11amount",         // 966
    "ctrl13",               // 967
    "ctrl14",               // 968
    "ctrl15",               // 969
    "ctrl16",               // 970
//// BEGIN (I THINK) RESERVED
    "---",          // 1024
    "arpstatus",            // 1025
    "arpmode",              // 1026
    "arppattern",           // 1027
    "arpnote",              // 1028
    "arpvel",               // 1029
    "arpgatetime",          // 1030
    "arpextcount",          // 1031
    "arpextint",            // 1032
    "arpsync",              // 1033
    "arppredelay",          // 1034
    "arpduration",          // 1035
    "arprecycle",           // 1036
    "arpkbdthru",           // 1037
    "arplatch",             // 1038
    "arpkrlow",             // 1039
    "arpkrhigh",            // 1040
    "arppatternspeed",              // 1041
    "arppatternromid",              // 1042
    "arppostdelay",         // 1043
//// BEGIN COMMON EFFECTS
    "---",          // 1152
    "fx1algorithm",         // 1153
    "fx1decay",             // 1154
    "fx1hfdamp",            // 1155
    "fx1ab",                // 1156
    "fx1mixmain",           // 1157
    "fx1mixsub1",           // 1158
    "fx1mixsub2",           // 1159
    "fx2algorithm",         // 1160
    "fx2feedback",          // 1161
    "fx2lforate",           // 1162
    "fx2delay",             // 1163
    "fx2mixmain",           // 1164
    "fx2mixsub1",           // 1165
    "fx2mixsub2",           // 1166
    "fx1mixsub3",           // 1167                 // yes, fx*1*
    "fx2mixsub4",           // 1168
//// BEGIN COMMON LINK
    "---",          // 1280
    "link1preset",          // 1281
    "link1volume",          // 1282
    "link1pan",             // 1283
    "link1transpose",               // 1284
    "link1delay",           // 1285
    "link1keylow",          // 1286
    "link1keyhigh",         // 1287
    "link1vellow",          // 1288
    "link1velhigh",         // 1289
    "link2preset",          // 1290
    "link2volume",          // 1291
    "link2pan",             // 1292
    "link2transpose",               // 1293
    "link2delay",           // 1294
    "link2keylow",          // 1295
    "link2keyhigh",         // 1296
    "link2vellow",          // 1297
    "link2velhigh",         // 1298
    "link1presetromid",             // 1299
    "link2presetromid",             // 1300
//// BEGIN COMMON LAYER 1 GENERAL
    "---",          // 1408
    "layer1instrument",             // 1409
    "layer1volume",         // 1410
    "layer1pan",            // 1411
    "layer1submix",         // 1412
    "layer1keylow",         // 1413
    "layer1keylowfade",             // 1414
    "layer1keyhigh",                // 1415
    "layer1keyhighfade",            // 1416
    "layer1vellow",         // 1417
    "layer1vellowfade",             // 1418
    "layer1velhigh",                // 1419
    "layer1velhighfade",            // 1420
    "layer1rtlow",          // 1421
    "layer1rtlowfade",              // 1422
    "layer1rthigh",         // 1423
    "layer1rthighfade",             // 1424
    "layer1ctune",          // 1425
    "layer1ftune",          // 1426
    "layer1ldetune",                // 1427
    "layer1ldetunewidth",           // 1428
    "layer1transpose",              // 1429
    "layer1nontranspose",           // 1430
    "layer1bend",           // 1431
    "layer1gliderate",              // 1432
    "layer1glidecurve",             // 1433
    "layer1loop",           // 1434
    "layer1startdelay",             // 1435
    "layer1startoffset",            // 1436
    "layer1solo",           // 1437
    "layer1group",          // 1438
    "layer1instrumentromid",              // 1439
//// BEGIN COMMON LAYER 1 FILTER
    "---",          // 1536
    "layer1filttype",               // 1537
    "layer1filtfreq",               // 1538
    "layer1filtq",          // 1539
//// BEGIN COMMON LAYER 1 LFO
    "---",          // 1664
    "layer1lfo1rate",               // 1665
    "layer1lfo1shape",              // 1666
    "layer1lfo1delay",              // 1667
    "layer1lfo1var",                // 1668
    "layer1lfo1sync",               // 1669
    "layer1lfo2rate",               // 1670
    "layer1lfo2shape",              // 1671
    "layer1lfo2delay",              // 1672
    "layer1lfo2var",                // 1673
    "layer1lfo2sync",               // 1674
//// BEGIN COMMON LAYER 1 ENVELOPE
    "---",          // 1792
    "layer1env1mode",               // 1793
    "layer1env1atk1rate",           // 1794
    "layer1env1atk1lvl",            // 1795
    "layer1env1dcy1rate",           // 1796
    "layer1env1dcy1lvl",            // 1797
    "layer1env1rls1rate",           // 1798
    "layer1env1rls1lvl",            // 1799
    "layer1env1atk2rate",           // 1800
    "layer1env1atk2lvl",            // 1801
    "layer1env1dcy2rate",           // 1802
    "layer1env1dcy2lvl",            // 1803
    "layer1env1rls2rate",           // 1804
    "layer1env1rls2lvl",            // 1805
    "layer1env2mode",               // 1806
    "layer1env2atk1rate",           // 1807
    "layer1env2atk1lvl",            // 1808
    "layer1env2dcy1rate",           // 1809
    "layer1env2dcy1lvl",            // 1810
    "layer1env2rls1rate",           // 1811
    "layer1env2rls1lvl",            // 1812
    "layer1env2atk2rate",           // 1813
    "layer1env2atk2lvl",            // 1814
    "layer1env2dcy2rate",           // 1815
    "layer1env2dcy2lvl",            // 1816
    "layer1env2rls2rate",           // 1817
    "layer1env2rls2lvl",            // 1818
    "layer1env3mode",               // 1819
    "layer1env3atk1rate",           // 1820
    "layer1env3atk1lvl",            // 1821
    "layer1env3dcy1rate",           // 1822
    "layer1env3dcy1lvl",            // 1823
    "layer1env3rls1rate",           // 1824
    "layer1env3rls1lvl",            // 1825
    "layer1env3atk2rate",           // 1826
    "layer1env3atk2lvl",            // 1827
    "layer1env3dcy2rate",           // 1828
    "layer1env3dcy2lvl",            // 1829
    "layer1env3rls2rate",           // 1830
    "layer1env3rls2lvl",            // 1831
    "layer1env2repeat",             // 1833
    "layer1env3repeat",             // 1834
    "layer1fakeparam",                                                  // we'll call it 1835, it's a bug
//// BEGIN COMMON LAYER 1 PATCH CORDS
    "---",          // 1920
    "layer1cord0src",               // 1921
    "layer1cord0dst",               // 1922
    "layer1cord0amt",               // 1923
    "layer1cord1src",               // 1924
    "layer1cord1dst",               // 1925
    "layer1cord1amt",               // 1926
    "layer1cord2src",               // 1927
    "layer1cord2dst",               // 1928
    "layer1cord2amt",               // 1929
    "layer1cord3src",               // 1930
    "layer1cord3dst",               // 1931
    "layer1cord3amt",               // 1932
    "layer1cord4src",               // 1933
    "layer1cord4dst",               // 1934
    "layer1cord4amt",               // 1935
    "layer1cord5src",               // 1936
    "layer1cord5dst",               // 1937
    "layer1cord5amt",               // 1938
    "layer1cord6src",               // 1939
    "layer1cord6dst",               // 1940
    "layer1cord6amt",               // 1941
    "layer1cord7src",               // 1942
    "layer1cord7dst",               // 1943
    "layer1cord7amt",               // 1944
    "layer1cord8src",               // 1945
    "layer1cord8dst",               // 1946
    "layer1cord8amt",               // 1947
    "layer1cord9src",               // 1948
    "layer1cord9dst",               // 1949
    "layer1cord9amt",               // 1950
    "layer1cord10src",              // 1951
    "layer1cord10dst",              // 1952
    "layer1cord10amt",              // 1953
    "layer1cord11src",              // 1954
    "layer1cord11dst",              // 1955
    "layer1cord11amt",              // 1956
    "layer1cord12src",              // 1957
    "layer1cord12dst",              // 1958
    "layer1cord12amt",              // 1959
    "layer1cord13src",              // 1960
    "layer1cord13dst",              // 1961
    "layer1cord13amt",              // 1962
    "layer1cord14src",              // 1963
    "layer1cord14dst",              // 1964
    "layer1cord14amt",              // 1965
    "layer1cord15src",              // 1966
    "layer1cord15dst",              // 1967
    "layer1cord15amt",              // 1968
    "layer1cord16src",              // 1969
    "layer1cord16dst",              // 1970
    "layer1cord16amt",              // 1971
    "layer1cord17src",              // 1972
    "layer1cord17dst",              // 1973
    "layer1cord17amt",              // 1974
    "layer1cord18src",              // 1975
    "layer1cord18dst",              // 1976
    "layer1cord18amt",              // 1977
    "layer1cord19src",              // 1978
    "layer1cord19dst",              // 1979
    "layer1cord19amt",              // 1980
    "layer1cord20src",              // 1981
    "layer1cord20dst",              // 1982
    "layer1cord20amt",              // 1983
    "layer1cord21src",              // 1984
    "layer1cord21dst",              // 1985
    "layer1cord21amt",              // 1986
    "layer1cord22src",              // 1987
    "layer1cord22dst",              // 1988
    "layer1cord22amt",              // 1989
    "layer1cord23src",              // 1990
    "layer1cord23dst",              // 1991
    "layer1cord23amt",              // 1992
//// BEGIN COMMON LAYER 2 GENERAL
    "---",          // 1408
    "layer2instrument",             // 1409
    "layer2volume",         // 1410
    "layer2pan",            // 1411
    "layer2submix",         // 1412
    "layer2keylow",         // 1413
    "layer2keylowfade",             // 1414
    "layer2keyhigh",                // 1415
    "layer2keyhighfade",            // 1416
    "layer2vellow",         // 1417
    "layer2vellowfade",             // 1418
    "layer2velhigh",                // 1419
    "layer2velhighfade",            // 1420
    "layer2rtlow",          // 1421
    "layer2rtlowfade",              // 1422
    "layer2rthigh",         // 1423
    "layer2rthighfade",             // 1424
    "layer2ctune",          // 1425
    "layer2ftune",          // 1426
    "layer2ldetune",                // 1427
    "layer2ldetunewidth",           // 1428
    "layer2transpose",              // 1429
    "layer2nontranspose",           // 1430
    "layer2bend",           // 1431
    "layer2gliderate",              // 1432
    "layer2glidecurve",             // 1433
    "layer2loop",           // 1434
    "layer2startdelay",             // 1435
    "layer2startoffset",            // 1436
    "layer2solo",           // 1437
    "layer2group",          // 1438
    "layer2instrumentromid",              // 1439
//// BEGIN COMMON LAYER 2 FILTER
    "---",          // 1536
    "layer2filttype",               // 1537
    "layer2filtfreq",               // 1538
    "layer2filtq",          // 1539
//// BEGIN COMMON LAYER 2 LFO
    "---",          // 1664
    "layer2lfo1rate",               // 1665
    "layer2lfo1shape",              // 1666
    "layer2lfo1delay",              // 1667
    "layer2lfo1var",                // 1668
    "layer2lfo1sync",               // 1669
    "layer2lfo2rate",               // 1670
    "layer2lfo2shape",              // 1671
    "layer2lfo2delay",              // 1672
    "layer2lfo2var",                // 1673
    "layer2lfo2sync",               // 1674
//// BEGIN COMMON LAYER 2 ENVELOPE
    "---",          // 1792
    "layer2env1mode",               // 1793
    "layer2env1atk1rate",           // 1794
    "layer2env1atk1lvl",            // 1795
    "layer2env1dcy1rate",           // 1796
    "layer2env1dcy1lvl",            // 1797
    "layer2env1rls1rate",           // 1798
    "layer2env1rls1lvl",            // 1799
    "layer2env1atk2rate",           // 1800
    "layer2env1atk2lvl",            // 1801
    "layer2env1dcy2rate",           // 1802
    "layer2env1dcy2lvl",            // 1803
    "layer2env1rls2rate",           // 1804
    "layer2env1rls2lvl",            // 1805
    "layer2env2mode",               // 1806
    "layer2env2atk1rate",           // 1807
    "layer2env2atk1lvl",            // 1808
    "layer2env2dcy1rate",           // 1809
    "layer2env2dcy1lvl",            // 1810
    "layer2env2rls1rate",           // 1811
    "layer2env2rls1lvl",            // 1812
    "layer2env2atk2rate",           // 1813
    "layer2env2atk2lvl",            // 1814
    "layer2env2dcy2rate",           // 1815
    "layer2env2dcy2lvl",            // 1816
    "layer2env2rls2rate",           // 1817
    "layer2env2rls2lvl",            // 1818
    "layer2env3mode",               // 1819
    "layer2env3atk1rate",           // 1820
    "layer2env3atk1lvl",            // 1821
    "layer2env3dcy1rate",           // 1822
    "layer2env3dcy1lvl",            // 1823
    "layer2env3rls1rate",           // 1824
    "layer2env3rls1lvl",            // 1825
    "layer2env3atk2rate",           // 1826
    "layer2env3atk2lvl",            // 1827
    "layer2env3dcy2rate",           // 1828
    "layer2env3dcy2lvl",            // 1829
    "layer2env3rls2rate",           // 1830
    "layer2env3rls2lvl",            // 1831
    "layer2env2repeat",             // 1833
    "layer2env3repeat",             // 1834
    "layer2fakeparam",                                                  // we'll call it 1835, it's a bug
//// BEGIN COMMON LAYER 2 PATCH CORDS
    "---",          // 1920
    "layer2cord0src",               // 1921
    "layer2cord0dst",               // 1922
    "layer2cord0amt",               // 1923
    "layer2cord1src",               // 1924
    "layer2cord1dst",               // 1925
    "layer2cord1amt",               // 1926
    "layer2cord2src",               // 1927
    "layer2cord2dst",               // 1928
    "layer2cord2amt",               // 1929
    "layer2cord3src",               // 1930
    "layer2cord3dst",               // 1931
    "layer2cord3amt",               // 1932
    "layer2cord4src",               // 1933
    "layer2cord4dst",               // 1934
    "layer2cord4amt",               // 1935
    "layer2cord5src",               // 1936
    "layer2cord5dst",               // 1937
    "layer2cord5amt",               // 1938
    "layer2cord6src",               // 1939
    "layer2cord6dst",               // 1940
    "layer2cord6amt",               // 1941
    "layer2cord7src",               // 1942
    "layer2cord7dst",               // 1943
    "layer2cord7amt",               // 1944
    "layer2cord8src",               // 1945
    "layer2cord8dst",               // 1946
    "layer2cord8amt",               // 1947
    "layer2cord9src",               // 1948
    "layer2cord9dst",               // 1949
    "layer2cord9amt",               // 1950
    "layer2cord10src",              // 1951
    "layer2cord10dst",              // 1952
    "layer2cord10amt",              // 1953
    "layer2cord11src",              // 1954
    "layer2cord11dst",              // 1955
    "layer2cord11amt",              // 1956
    "layer2cord12src",              // 1957
    "layer2cord12dst",              // 1958
    "layer2cord12amt",              // 1959
    "layer2cord13src",              // 1960
    "layer2cord13dst",              // 1961
    "layer2cord13amt",              // 1962
    "layer2cord14src",              // 1963
    "layer2cord14dst",              // 1964
    "layer2cord14amt",              // 1965
    "layer2cord15src",              // 1966
    "layer2cord15dst",              // 1967
    "layer2cord15amt",              // 1968
    "layer2cord16src",              // 1969
    "layer2cord16dst",              // 1970
    "layer2cord16amt",              // 1971
    "layer2cord17src",              // 1972
    "layer2cord17dst",              // 1973
    "layer2cord17amt",              // 1974
    "layer2cord18src",              // 1975
    "layer2cord18dst",              // 1976
    "layer2cord18amt",              // 1977
    "layer2cord19src",              // 1978
    "layer2cord19dst",              // 1979
    "layer2cord19amt",              // 1980
    "layer2cord20src",              // 1981
    "layer2cord20dst",              // 1982
    "layer2cord20amt",              // 1983
    "layer2cord21src",              // 1984
    "layer2cord21dst",              // 1985
    "layer2cord21amt",              // 1986
    "layer2cord22src",              // 1987
    "layer2cord22dst",              // 1988
    "layer2cord22amt",              // 1989
    "layer2cord23src",              // 1990
    "layer2cord23dst",              // 1991
    "layer2cord23amt",              // 1992
//// BEGIN COMMON LAYER 3 GENERAL
    "---",          // 1408
    "layer3instrument",             // 1409
    "layer3volume",         // 1410
    "layer3pan",            // 1411
    "layer3submix",         // 1412
    "layer3keylow",         // 1413
    "layer3keylowfade",             // 1414
    "layer3keyhigh",                // 1415
    "layer3keyhighfade",            // 1416
    "layer3vellow",         // 1417
    "layer3vellowfade",             // 1418
    "layer3velhigh",                // 1419
    "layer3velhighfade",            // 1420
    "layer3rtlow",          // 1421
    "layer3rtlowfade",              // 1422
    "layer3rthigh",         // 1423
    "layer3rthighfade",             // 1424
    "layer3ctune",          // 1425
    "layer3ftune",          // 1426
    "layer3ldetune",                // 1427
    "layer3ldetunewidth",           // 1428
    "layer3transpose",              // 1429
    "layer3nontranspose",           // 1430
    "layer3bend",           // 1431
    "layer3gliderate",              // 1432
    "layer3glidecurve",             // 1433
    "layer3loop",           // 1434
    "layer3startdelay",             // 1435
    "layer3startoffset",            // 1436
    "layer3solo",           // 1437
    "layer3group",          // 1438
    "layer3instrumentromid",              // 1439
//// BEGIN COMMON LAYER 3 FILTER
    "---",          // 1536
    "layer3filttype",               // 1537
    "layer3filtfreq",               // 1538
    "layer3filtq",          // 1539
//// BEGIN COMMON LAYER 3 LFO
    "---",          // 1664
    "layer3lfo1rate",               // 1665
    "layer3lfo1shape",              // 1666
    "layer3lfo1delay",              // 1667
    "layer3lfo1var",                // 1668
    "layer3lfo1sync",               // 1669
    "layer3lfo2rate",               // 1670
    "layer3lfo2shape",              // 1671
    "layer3lfo2delay",              // 1672
    "layer3lfo2var",                // 1673
    "layer3lfo2sync",               // 1674
//// BEGIN COMMON LAYER 3 ENVELOPE
    "---",          // 1792
    "layer3env1mode",               // 1793
    "layer3env1atk1rate",           // 1794
    "layer3env1atk1lvl",            // 1795
    "layer3env1dcy1rate",           // 1796
    "layer3env1dcy1lvl",            // 1797
    "layer3env1rls1rate",           // 1798
    "layer3env1rls1lvl",            // 1799
    "layer3env1atk2rate",           // 1800
    "layer3env1atk2lvl",            // 1801
    "layer3env1dcy2rate",           // 1802
    "layer3env1dcy2lvl",            // 1803
    "layer3env1rls2rate",           // 1804
    "layer3env1rls2lvl",            // 1805
    "layer3env2mode",               // 1806
    "layer3env2atk1rate",           // 1807
    "layer3env2atk1lvl",            // 1808
    "layer3env2dcy1rate",           // 1809
    "layer3env2dcy1lvl",            // 1810
    "layer3env2rls1rate",           // 1811
    "layer3env2rls1lvl",            // 1812
    "layer3env2atk2rate",           // 1813
    "layer3env2atk2lvl",            // 1814
    "layer3env2dcy2rate",           // 1815
    "layer3env2dcy2lvl",            // 1816
    "layer3env2rls2rate",           // 1817
    "layer3env2rls2lvl",            // 1818
    "layer3env3mode",               // 1819
    "layer3env3atk1rate",           // 1820
    "layer3env3atk1lvl",            // 1821
    "layer3env3dcy1rate",           // 1822
    "layer3env3dcy1lvl",            // 1823
    "layer3env3rls1rate",           // 1824
    "layer3env3rls1lvl",            // 1825
    "layer3env3atk2rate",           // 1826
    "layer3env3atk2lvl",            // 1827
    "layer3env3dcy2rate",           // 1828
    "layer3env3dcy2lvl",            // 1829
    "layer3env3rls2rate",           // 1830
    "layer3env3rls2lvl",            // 1831
    "layer3env2repeat",             // 1833
    "layer3env3repeat",             // 1834
    "layer3fakeparam",                                                  // we'll call it 1835, it's a bug
//// BEGIN COMMON LAYER 3 PATCH CORDS
    "---",          // 1920
    "layer3cord0src",               // 1921
    "layer3cord0dst",               // 1922
    "layer3cord0amt",               // 1923
    "layer3cord1src",               // 1924
    "layer3cord1dst",               // 1925
    "layer3cord1amt",               // 1926
    "layer3cord2src",               // 1927
    "layer3cord2dst",               // 1928
    "layer3cord2amt",               // 1929
    "layer3cord3src",               // 1930
    "layer3cord3dst",               // 1931
    "layer3cord3amt",               // 1932
    "layer3cord4src",               // 1933
    "layer3cord4dst",               // 1934
    "layer3cord4amt",               // 1935
    "layer3cord5src",               // 1936
    "layer3cord5dst",               // 1937
    "layer3cord5amt",               // 1938
    "layer3cord6src",               // 1939
    "layer3cord6dst",               // 1940
    "layer3cord6amt",               // 1941
    "layer3cord7src",               // 1942
    "layer3cord7dst",               // 1943
    "layer3cord7amt",               // 1944
    "layer3cord8src",               // 1945
    "layer3cord8dst",               // 1946
    "layer3cord8amt",               // 1947
    "layer3cord9src",               // 1948
    "layer3cord9dst",               // 1949
    "layer3cord9amt",               // 1950
    "layer3cord10src",              // 1951
    "layer3cord10dst",              // 1952
    "layer3cord10amt",              // 1953
    "layer3cord11src",              // 1954
    "layer3cord11dst",              // 1955
    "layer3cord11amt",              // 1956
    "layer3cord12src",              // 1957
    "layer3cord12dst",              // 1958
    "layer3cord12amt",              // 1959
    "layer3cord13src",              // 1960
    "layer3cord13dst",              // 1961
    "layer3cord13amt",              // 1962
    "layer3cord14src",              // 1963
    "layer3cord14dst",              // 1964
    "layer3cord14amt",              // 1965
    "layer3cord15src",              // 1966
    "layer3cord15dst",              // 1967
    "layer3cord15amt",              // 1968
    "layer3cord16src",              // 1969
    "layer3cord16dst",              // 1970
    "layer3cord16amt",              // 1971
    "layer3cord17src",              // 1972
    "layer3cord17dst",              // 1973
    "layer3cord17amt",              // 1974
    "layer3cord18src",              // 1975
    "layer3cord18dst",              // 1976
    "layer3cord18amt",              // 1977
    "layer3cord19src",              // 1978
    "layer3cord19dst",              // 1979
    "layer3cord19amt",              // 1980
    "layer3cord20src",              // 1981
    "layer3cord20dst",              // 1982
    "layer3cord20amt",              // 1983
    "layer3cord21src",              // 1984
    "layer3cord21dst",              // 1985
    "layer3cord21amt",              // 1986
    "layer3cord22src",              // 1987
    "layer3cord22dst",              // 1988
    "layer3cord22amt",              // 1989
    "layer3cord23src",              // 1990
    "layer3cord23dst",              // 1991
    "layer3cord23amt",              // 1992
//// BEGIN COMMON LAYER 4 GENERAL
    "---",          // 1408
    "layer4instrument",             // 1409
    "layer4volume",         // 1410
    "layer4pan",            // 1411
    "layer4submix",         // 1412
    "layer4keylow",         // 1413
    "layer4keylowfade",             // 1414
    "layer4keyhigh",                // 1415
    "layer4keyhighfade",            // 1416
    "layer4vellow",         // 1417
    "layer4vellowfade",             // 1418
    "layer4velhigh",                // 1419
    "layer4velhighfade",            // 1420
    "layer4rtlow",          // 1421
    "layer4rtlowfade",              // 1422
    "layer4rthigh",         // 1423
    "layer4rthighfade",             // 1424
    "layer4ctune",          // 1425
    "layer4ftune",          // 1426
    "layer4ldetune",                // 1427
    "layer4ldetunewidth",           // 1428
    "layer4transpose",              // 1429
    "layer4nontranspose",           // 1430
    "layer4bend",           // 1431
    "layer4gliderate",              // 1432
    "layer4glidecurve",             // 1433
    "layer4loop",           // 1434
    "layer4startdelay",             // 1435
    "layer4startoffset",            // 1436
    "layer4solo",           // 1437
    "layer4group",          // 1438
    "layer4instrumentromid",              // 1439
//// BEGIN COMMON LAYER 4 FILTER
    "---",          // 1536
    "layer4filttype",               // 1537
    "layer4filtfreq",               // 1538
    "layer4filtq",          // 1539
//// BEGIN COMMON LAYER 4 LFO
    "---",          // 1664
    "layer4lfo1rate",               // 1665
    "layer4lfo1shape",              // 1666
    "layer4lfo1delay",              // 1667
    "layer4lfo1var",                // 1668
    "layer4lfo1sync",               // 1669
    "layer4lfo2rate",               // 1670
    "layer4lfo2shape",              // 1671
    "layer4lfo2delay",              // 1672
    "layer4lfo2var",                // 1673
    "layer4lfo2sync",               // 1674
//// BEGIN COMMON LAYER 4 ENVELOPE
    "---",          // 1792
    "layer4env1mode",               // 1793
    "layer4env1atk1rate",           // 1794
    "layer4env1atk1lvl",            // 1795
    "layer4env1dcy1rate",           // 1796
    "layer4env1dcy1lvl",            // 1797
    "layer4env1rls1rate",           // 1798
    "layer4env1rls1lvl",            // 1799
    "layer4env1atk2rate",           // 1800
    "layer4env1atk2lvl",            // 1801
    "layer4env1dcy2rate",           // 1802
    "layer4env1dcy2lvl",            // 1803
    "layer4env1rls2rate",           // 1804
    "layer4env1rls2lvl",            // 1805
    "layer4env2mode",               // 1806
    "layer4env2atk1rate",           // 1807
    "layer4env2atk1lvl",            // 1808
    "layer4env2dcy1rate",           // 1809
    "layer4env2dcy1lvl",            // 1810
    "layer4env2rls1rate",           // 1811
    "layer4env2rls1lvl",            // 1812
    "layer4env2atk2rate",           // 1813
    "layer4env2atk2lvl",            // 1814
    "layer4env2dcy2rate",           // 1815
    "layer4env2dcy2lvl",            // 1816
    "layer4env2rls2rate",           // 1817
    "layer4env2rls2lvl",            // 1818
    "layer4env3mode",               // 1819
    "layer4env3atk1rate",           // 1820
    "layer4env3atk1lvl",            // 1821
    "layer4env3dcy1rate",           // 1822
    "layer4env3dcy1lvl",            // 1823
    "layer4env3rls1rate",           // 1824
    "layer4env3rls1lvl",            // 1825
    "layer4env3atk2rate",           // 1826
    "layer4env3atk2lvl",            // 1827
    "layer4env3dcy2rate",           // 1828
    "layer4env3dcy2lvl",            // 1829
    "layer4env3rls2rate",           // 1830
    "layer4env3rls2lvl",            // 1831
    "layer4env2repeat",             // 1833
    "layer4env3repeat",             // 1834
    "layer4fakeparam",                                                  // we'll call it 1835, it's a bug
//// BEGIN COMMON LAYER 4 PATCH CORDS
    "---",          // 1920
    "layer4cord0src",               // 1921
    "layer4cord0dst",               // 1922
    "layer4cord0amt",               // 1923
    "layer4cord1src",               // 1924
    "layer4cord1dst",               // 1925
    "layer4cord1amt",               // 1926
    "layer4cord2src",               // 1927
    "layer4cord2dst",               // 1928
    "layer4cord2amt",               // 1929
    "layer4cord3src",               // 1930
    "layer4cord3dst",               // 1931
    "layer4cord3amt",               // 1932
    "layer4cord4src",               // 1933
    "layer4cord4dst",               // 1934
    "layer4cord4amt",               // 1935
    "layer4cord5src",               // 1936
    "layer4cord5dst",               // 1937
    "layer4cord5amt",               // 1938
    "layer4cord6src",               // 1939
    "layer4cord6dst",               // 1940
    "layer4cord6amt",               // 1941
    "layer4cord7src",               // 1942
    "layer4cord7dst",               // 1943
    "layer4cord7amt",               // 1944
    "layer4cord8src",               // 1945
    "layer4cord8dst",               // 1946
    "layer4cord8amt",               // 1947
    "layer4cord9src",               // 1948
    "layer4cord9dst",               // 1949
    "layer4cord9amt",               // 1950
    "layer4cord10src",              // 1951
    "layer4cord10dst",              // 1952
    "layer4cord10amt",              // 1953
    "layer4cord11src",              // 1954
    "layer4cord11dst",              // 1955
    "layer4cord11amt",              // 1956
    "layer4cord12src",              // 1957
    "layer4cord12dst",              // 1958
    "layer4cord12amt",              // 1959
    "layer4cord13src",              // 1960
    "layer4cord13dst",              // 1961
    "layer4cord13amt",              // 1962
    "layer4cord14src",              // 1963
    "layer4cord14dst",              // 1964
    "layer4cord14amt",              // 1965
    "layer4cord15src",              // 1966
    "layer4cord15dst",              // 1967
    "layer4cord15amt",              // 1968
    "layer4cord16src",              // 1969
    "layer4cord16dst",              // 1970
    "layer4cord16amt",              // 1971
    "layer4cord17src",              // 1972
    "layer4cord17dst",              // 1973
    "layer4cord17amt",              // 1974
    "layer4cord18src",              // 1975
    "layer4cord18dst",              // 1976
    "layer4cord18amt",              // 1977
    "layer4cord19src",              // 1978
    "layer4cord19dst",              // 1979
    "layer4cord19amt",              // 1980
    "layer4cord20src",              // 1981
    "layer4cord20dst",              // 1982
    "layer4cord20amt",              // 1983
    "layer4cord21src",              // 1984
    "layer4cord21dst",              // 1985
    "layer4cord21amt",              // 1986
    "layer4cord22src",              // 1987
    "layer4cord22dst",              // 1988
    "layer4cord22amt",              // 1989
    "layer4cord23src",              // 1990
    "layer4cord23dst",              // 1991
    "layer4cord23amt",              // 1992
    };


// This maps ALL parameters, for all four layers, to the indexes in the parameters array
    static HashMap<String, Integer> parametersToIndex = new HashMap<>();
// This maps ALL parameters, for all four layers, to the ID associated with them.  You can
// use the parameterToLayer(...) function to get the layer for the parameter, if they are layer params
    static HashMap<String, Integer> parametersToID = new HashMap<>();
// This maps the parameterIDs to the indexes in the parameterIDs array
    static HashMap<Integer, Integer> idToIndex = new HashMap<>();

    public int parameterToLayer(String param)
        {
        if (param.startsWith("layer"))
            {
            return (int)(((param.charAt(5)) - '1'));
            }
        else
            {
            return -1;
            }
        }

    static void buildParameters()
        {
        if (!parametersToIndex.isEmpty()) return;               // already built
        
        // Handle global parameters first
        for(int i = 0; i < 130; i++)
            {
            parametersToIndex.put(parameters[i], i);
            idToIndex.put(parameterIDs[i], i);
            parametersToID.put(parameters[i], parameterIDs[i]);
            }
            

        // Handle layer parameters
        for(int i = 130; i < 293; i++)
            {
            parametersToIndex.put(parameters[i], i);
            parametersToIndex.put(parameters[i + 163], i + 163);
            parametersToIndex.put(parameters[i + 163 * 2], i + 163 * 2);
            parametersToIndex.put(parameters[i + 163 * 3], i + 163 * 3);
            parametersToID.put(parameters[i], parameterIDs[i]);
            parametersToID.put(parameters[i + 163], parameterIDs[i]);
            parametersToID.put(parameters[i + 163 * 2], parameterIDs[i]);
            parametersToID.put(parameters[i + 163 * 3], parameterIDs[i]);
            idToIndex.put(parameterIDs[i], i);
            }
        }

    final static int[] parameterIDs = new int[]
    {
    // PRESET AND LAYER
    // 897,            // Preset
    // 898,            // Layer
    // NAME 
    899,            // ---                          
    900,            // ---
    901,            // ---
    902,            // ---
    903,            // ---
    904,            // ---
    905,            // ---
    906,            // ---
    907,            // ---
    908,            // ---
    909,            // ---
    910,            // ---
    911,            // ---
    912,            // ---
    913,            // ---
    914,            // ---
    // GENERAL
    915,            // ctrl1
    916,            // ctrl2
    917,            // ctrl3
    918,            // ctrl4
    919,            // ctrl5
    920,            // ctrl6
    921,            // ctrl7
    922,            // ctrl8
    923,            // kbdtune
    924,            // ctrl9
    925,            // ctrl10
    926,            // ctrl11
    927,            // ctrl12
    928,            // riff
    929,            // riffromid
    930,            // tempooffset
    931,            // cord0source
    932,            // cord0dest
    933,            // cord0amount
    934,            // cord1source
    935,            // cord1dest
    936,            // cord1amount
    937,            // cord2source
    938,            // cord2dest
    939,            // cord2amount
    940,            // cord3source
    941,            // cord3dest
    942,            // cord3amount
    943,            // cord4source
    944,            // cord4dest
    945,            // cord4amount
    946,            // cord5source
    947,            // cord5dest
    948,            // cord5amount
    949,            // cord6source
    950,            // cord6dest
    951,            // cord6amount
    952,            // cord7source
    953,            // cord7dest
    954,            // cord7amount
    955,            // cord8source
    956,            // cord8dest
    957,            // cord8amount
    958,            // cord9source
    959,            // cord9dest
    960,            // cord9amount
    961,            // cord10source
    962,            // cord10dest
    963,            // cord10amount
    964,            // cord11source
    965,            // cord11dest
    966,            // cord11amount
    967,            // ctrl13
    968,            // ctrl14
    969,            // ctrl15
    970,            // ctrl16
    /// ARPEGGIATOR
    1024,           // ---
    1025,           // arpstatus
    1026,           // arpmode
    1027,           // arppattern
    1028,           // arpnode
    1029,           // arpvel
    1030,           // arpgatetime
    1031,           // arpextcount
    1032,           // arpextint
    1033,           // arpsync
    1034,           // arppredelay
    1035,           // arpduration
    1036,           // arprecycle
    1037,           // arpkbdthru
    1038,           // arplatch
    1039,           // arpkrlow
    1040,           // arpkrhigh
    1041,           // arppatternspeed
    1042,           // arppatternromid
    1043,           // arppostdelay
    /// EFFECTS
    1152,           // ---
    1153,           // fx1algorithm
    1154,           // fx1decay
    1155,           // fx1hfdamp
    1156,           // fx1ab
    1157,           // fx1mixmain
    1158,           // fx1mixsub1
    1159,           // fx1mixsub2
    1160,           // fx2algorithm
    1161,           // fx2feeback
    1162,           // fx2lforate
    1163,           // fx2delay
    1164,           // fx2mixmain
    1165,           // fx2mixsub1
    1166,           // fx2mixsub2
    1167,           // fx1mixsub3, yes fx*1*
    1168,           // fx2mixsub4
    /// LINKS
    1280,           // ---
    1281,           // link1preset
    1282,           // link1volume
    1283,           // link1pan
    1284,           // link1transpose
    1285,           // link1delay
    1286,           // link1keylow
    1287,           // link1keyhigh
    1288,           // link1vellow
    1289,           // link1velhigh
    1290,           // link2preset
    1291,           // link2volume
    1292,           // link2pan
    1293,           // link2transpose
    1294,           // link1delay
    1295,           // link2keylow
    1296,           // link2keyhigh
    1297,           // link2vellow
    1298,           // link2velhigh
    1299,           // link1presetromid
    1300,           // link2presetromid
    /// LAYER GENERAL
    1408,           // ---
    1409,           // layer1instrument
    1410,           // layer1volume
    1411,           // layer1pan
    1412,           // layer1submix
    1413,           // layer1keylow
    1414,           // layer1keylowfade
    1415,           // layer1keyhigh
    1416,           // layer1keyhighfade
    1417,           // layer1vellow
    1418,           // layer1vellowfade
    1419,           // layer1velhigh
    1420,           // layer1velhighfade
    1421,           // layer1rtlow
    1422,           // layer1rtlowfade
    1423,           // layer1rthigh
    1424,           // layer1rthighfade
    1425,           // layer1ctune
    1426,           // layer1ftune
    1427,           // layer1ldetune
    1428,           // layer1ldetunewidth
    1429,           // layer1transpose
    1430,           // layer1nontranspose
    1431,           // layer1bend
    1432,           // layer1gliderate
    1433,           // layer1glidecurve
    1434,           // layer1loop
    1435,           // layer1startdelay
    1436,           // layer1startoffset
    1437,           // layer1solo
    1438,           // layer1group
    1439,           // layer1instrumentromid
    /// LAYER FILTER
    1536,           // ---
    1537,           // layer1filttype
    1538,           // layer1filtfreq
    1539,           // layer1filtq
    /// LAYER LFOS
    1664,           // ---
    1665,           // layer1lfo1rate
    1666,           // layer1lfo1shape
    1667,           // layer1lfo1delay
    1668,           // layer1lfo1var
    1669,           // layer1lfo1sync
    1670,           // layer1lfo2rate
    1671,           // layer1lfo2shape
    1672,           // layer1lfo2delay
    1673,           // layer1lfo2var
    1674,           // layer1lfo2sync
    /// LAYER ENVELOPES
    1792,           // ---
    1793,           // layer1env1mode
    1794,           // layer1env1atk1rate
    1795,           // layer1env1atk1lvl
    1796,           // layer1env1dcy1rate
    1797,           // layer1env1dcy1lvl
    1798,           // layer1env1rls1rate
    1799,           // layer1env1rls1lvl
    1800,           // layer1env1atk2rate
    1801,           // layer1env1atk2lvl
    1802,           // layer1env1dcy2rate
    1803,           // layer1env1dcy2lvl
    1804,           // layer1env1rls2rate
    1805,           // layer1env1rls2lvl
    1806,           // layer1env2mode
    1807,           // layer1env2atk1rate
    1808,           // layer1env2atk1lvl
    1809,           // layer1env2dcy1rate
    1810,           // layer1env2dcy1lvl
    1811,           // layer1env2rls1rate
    1812,           // layer1env2rls1lvl
    1813,           // layer1env2atk2rate
    1814,           // layer1env2atk2lvl
    1815,           // layer1env2dcy2rate
    1816,           // layer1env2dcy2lvl
    1817,           // layer1env2rls2rate
    1818,           // layer1env2rls2lvl
    1819,           // layer1env3mode
    1820,           // layer1env3atk1rate
    1821,           // layer1env3atk1lvl
    1822,           // layer1env3dcy1rate
    1823,           // layer1env3dcy1lvl
    1824,           // layer1env3rls1rate
    1825,           // layer1env3rls1lvl
    1826,           // layer1env3atk2rate
    1827,           // layer1env3atk2lvl
    1828,           // layer1env3dcy2rate
    1829,           // layer1env3dcy2lvl
    1830,           // layer1env3rls2rate
    1831,           // layer1env3rls2lvl
    1833,           // layer1env2repeat
    1834,           // layer1env3repeat
    1835,                       // THIS DOES NOT EXIST.  It's an E-Mu bug but we need a stand-in here
    /// LAYER PATCH CORDS
    1920,           // ---
    1921,           // layer1cord0src
    1922,           // layer1cord0dst
    1923,           // layer1cord0amt
    1924,           // layer1cord1src
    1925,           // layer1cord1dst
    1926,           // layer1cord1amt
    1927,           // layer1cord2src
    1928,           // layer1cord2dst
    1929,           // layer1cord2amt
    1930,           // layer1cord3src
    1931,           // layer1cord3dst
    1932,           // layer1cord3amt
    1933,           // layer1cord4src
    1934,           // layer1cord4dst
    1935,           // layer1cord4amt
    1936,           // layer1cord5src
    1937,           // layer1cord5dst
    1938,           // layer1cord5amt
    1939,           // layer1cord6src
    1940,           // layer1cord6dst
    1941,           // layer1cord6amt
    1942,           // layer1cord7src
    1943,           // layer1cord7dst
    1944,           // layer1cord7amt
    1945,           // layer1cord8src
    1946,           // layer1cord8dst
    1947,           // layer1cord8amt
    1948,           // layer1cord9src
    1949,           // layer1cord9dst
    1950,           // layer1cord9amt
    1951,           // layer1cord10src
    1952,           // layer1cord10dst
    1953,           // layer1cord10amt
    1954,           // layer1cord11src
    1955,           // layer1cord11dst
    1956,           // layer1cord11amt
    1957,           // layer1cord12src
    1958,           // layer1cord12dst
    1959,           // layer1cord12amt
    1960,           // layer1cord13src
    1961,           // layer1cord13dst
    1962,           // layer1cord13amt
    1963,           // layer1cord14src
    1964,           // layer1cord14dst
    1965,           // layer1cord14amt
    1966,           // layer1cord15src
    1967,           // layer1cord15dst
    1968,           // layer1cord15amt
    1969,           // layer1cord16src
    1970,           // layer1cord16dst
    1971,           // layer1cord16amt
    1972,           // layer1cord17src
    1973,           // layer1cord17dst
    1974,           // layer1cord17amt
    1975,           // layer1cord18src
    1976,           // layer1cord18dst
    1977,           // layer1cord18amt
    1978,           // layer1cord19src
    1979,           // layer1cord19dst
    1980,           // layer1cord19amt
    1981,           // layer1cord20src
    1982,           // layer1cord20dst
    1983,           // layer1cord20amt
    1984,           // layer1cord21src
    1985,           // layer1cord21dst
    1986,           // layer1cord21amt
    1987,           // layer1cord22src
    1988,           // layer1cord22dst
    1989,           // layer1cord22amt
    1990,           // layer1cord23src
    1991,           // layer1cord23dst
    1992,           // layer1cord23amt
    };
        
    public String[] getPatchNumberNames() 
        {
        String[] str = new String[1024];
        for(int i = 0; i < 1024; i++)
            {
            str[i] = "" + (i / 128) + " / " + (i % 128);
            }
        return str;
        }

    public String[] getBankNames() { return ROMS_AND_USER_SHORT; }

    public boolean[] getWriteableBanks() { return new boolean[] { true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false }; }

    public boolean getSupportsPatchWrites() { return true; }

    public int getPatchNameLength() { return 12; }

    public boolean isValidPatchLocation(int bank, int num) 
        { 
        return (num < getValidBankSize(bank));
        }
        
    public int getValidBankSize(int bank) { return NUM_BANKS[bank] * 128; }

    public int getBatchDownloadWaitTime() { return 100; }

    // we'll fail about 5 times or so, it's okay
    public int getBatchDownloadFailureCountdown() { return 50; }

    public boolean testVerify(Synth synth2, String key, Object obj1, Object obj2)
        {
        // The following keys are permitted to deviate:
        return (key.equals("riff") ||
            key.equals("arppattern") ||
            key.equals("link1preset") ||
            key.equals("link2preset") ||
            key.equals("layer1instrument") ||
            key.equals("layer2instrument") ||
            key.equals("layer3instrument") ||
            key.equals("layer4instrument"));
        }
                
    // Here we're coloring the non-installed ROMs gray and the rest magenta
    public boolean isAppropriatePatchLocation(int bank, int num) 
        {
        if (bank == 0) return true;
        
        boolean installedSimms = false;
        for(int i = 0; i < simms.length; i++)
            {
            if (simms[i] == bank) return true;
            if (simms[i] != 0) installedSimms = true;
            }
        return !installedSimms;
        }

    public void librarianCreated(Librarian librarian)
        {
        // We need to reorder the columns 

        JTable table = librarian.getTable();
        ArrayList<TableColumn> columns = new ArrayList<>();
        
        // The only way to reorder columns in a JTable without modifying their underlying indices
        // is (as far as I know) to physically remove the TableColumns and then reinsert them in
        // the desired order.  So that's what we're gonna do.
        
        // remove all columns
        int len = table.getColumnModel().getColumnCount();
        
        for(int i = 0; i < len; i++)
            {
            TableColumn c = table.getColumnModel().getColumn(i);
            }
                
        for(int i = 2; i < len; i++)
            {
            TableColumn column = table.getColumn(i);
            table.removeColumn(column);
            columns.add(column);            // this removes the column identified/named as "i", since we named the columns the same as their ordering
            }

        // insert special columns
        for(int i = 0; i < simms.length; i++)
            {
            if (simms[i] != 0)
                {
                for(int j = 0; j < columns.size(); j++)
                    {
                    if (((Integer)(columns.get(j).getIdentifier())).intValue() == simms[i] + 1)     // got it ( + 1 compensates for User in the columns)
                        {
                        TableColumn column = columns.remove(j);
                        table.addColumn(column);
                        break;
                        }
                    }
                }
            }

        // reinsert remainder
        int size = columns.size();
        for(int j = 0; j < size; j++)
            {
            table.addColumn(columns.remove(0));
            }
        }
                
    public boolean librarianTested() { return true; }
    }






/* 
   THE EMU PROTEUS 2000 RANT

   SYSEX.
   The Proteus 2000 has awful, just awful sysex.  Compared to its predecessors, it has an ENORMOUS
   sysex specification, and yet this specification is missing an extraordinary amount of critical
   information.  For example, the Proteus 2000 has an arpeggiator, and it has a bunch of sysex
   commands for updating the arpeggiator by itself.  But the spec writers kinda forgot to indicate
   where the arpeggiator parameters appear in the standard patch format.  In reality, it secretly 
   shows up in a shadowy and undocumented area called "Reserved", with no hints to that effect.  
   Also, the specification contains a million parameters, and provides them in a nicely convenient 
   list, but doesn't mention that the parameters appear in the patch format in a **different order** 
   than in the list, nor does it explain what that order is [the order is in fact the order of each 
   of the IDs of the parameters]. It also doesn't explain that the first two parameters do not appear 
   in the patch format all, and that the name parameters are one byte each while all other parameters 
   are two bytes.  Also the name parameters aren't included in the parameter tally for the so-called
   "Common General" parameters, even though they're listed as Common General parameters.  The only 
   way to discover these unfortunate misfeatures is to reverse engineer the spec by hand.  Oh yeah,
   and there are myriad range errors in the documentation and user manual: effects delays go -12...127,
   not 0...127; arp notes are offset by 1; several patchcord destinations are missing; others are
   listed as there, but they actually aren't.  Did I mention that the number of parameters reported
   in dumps is wrong?  There's one fewer envelope parameter than the machine indicates.  Also wrong:
   LFO shapes, tempo offsets, ...
     

   On top of it, the Proteus 2000's sysex is needlessly broken into many 255-byte chunks: it cannot 
   receive its entire patch format at one time.  Thankfully unlike all other E-Mu machines, it's now
   possible to request the current working patch in memory, but of course E-Mu forgot to document
   just how that's done: Prodatum figured out that you request patch 16383.  They also forgot to
   indicate how to send to the current working patch: this was almost a disaster for me, since sending
   is really critical, and there would be only two possible workarounds: either send each parameter
   independently (very slow) or write to a "scratch" location in patch RAM, which is not advisable
   on the Proteus 2000 because its scratch RAM is in Flash, rather than battery-backed, and so this
   would burn out the machine in short order  
   

   SIMMS AND "BANKS".
   E-Mu must have thought they were really smart by allowing each of these machines to have up to four
   SIMMs.  But it's just a nightmare for a patch editor because nowhere did they document the presets,
   instruments, arpeggios, and riffs on these SIMMs, nor did they even keep the same number of each
   on each SIMM.  Instead they assumed you'd query a machine's SIMMs for information.  Now Edisyn has
   patch editors for many synthesizers and other stuff and they can't all query your current machine; 
   thus Edisyn doesn't engage in synchronous back-and-forth with synthesizers.  But even if it **did**, 
   this still wouldn't work because patches are permitted to link to, refer to riffs and arps of, and 
   even use instruments from SIMMs that aren't present on the machine, under the asumption that later 
   they might be loaded onto a machine which does have those SIMMs.  This makes querying completely 
   useless.  The only alternative is to compile a big list of all this information.  Fortunately 
   someone has largely done this for me already (thanks Jan).  Unfortunately this list is incomplete: 
   there are at least four ROMs out there with no known information.  And on top of it you can create 
   your own "ROM" SIMMs out of Flash.

   And then there's the problem of banks.  From the perspective of a sysex writer, a SIMM is a bank.
   Everywhere in Proteus 2000 sysex, addresses are referred to as tuples, <SIMM or User Memory, number>.  
   This corresponds to the <bank, number> approach on other synths.  EXCEPT that the user is presented 
   with a lie on-screen: he is given a TRIPLE of the form <SIMM or User Memory, number / 128, number % 128>.
   This is supposed to make the user think that patches are organized as <SIMM/User Memory, bank, number>.
   The intent is that from the user's perspective, each SIMM has some N "banks", each of which has 128
   patches, arps, instruments, or riffs.  The number of "banks" varies wildly from SIMM to SIMM.  Even
   the user RAM memory is divided up into "banks".  Normally this falsehood wouldn't be an issue at all,
   after how hard is it to do div and mod?   But remember that E-Mu SIMMs vary in the number of patches:
   and thus they would vary in the number of "banks".  This turns out to cause problems in many places, 
   but none moreso than when developing a librarian.  

   Edisyn's librarians are two-dimensional tables with banks along the horizontal and patch numbers 
   along the vertical.  These tables can accommodate banks with different numbers of patches per bank.  
   But how would we organize these banks?  We can't just display the "banks" of the SIMMs that the user 
   has on his machine, because libraries are loaded from sysex files, and these files pay no heed to a 
   user's current configuration.  Any preset from any SIMM might be in that library.  Okay, so how about
   just listing every bank from every SIMM?  This would result in some 70-odd columns, nearly all of 
   which would be BLANK.  Instead the only reasonable approach would be to once again treat SIMMs as
   banks: thus we'd have one column for User RAM, and about 15 columns for known ROMs.  The problem here
   is that some ROMs have many patches: indeed one has over a thousand of them.  But other ROMs have just
   a hundred or so.  So we would have not only ragged columns but ones which are highly variable.  And
   it would be confusing to the user, who has been lied to about the actual organization of his patches,
   thinking that 128-patch "banks" are real.  C'est la vie.

*/





