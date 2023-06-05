/***
    Copyright 2022 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.waldorfm;

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
   A patch editor for the Waldorf M.
        
   @author Sean Luke
*/

public class WaldorfM extends Synth
    {
    public static final String[] NOTES = { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
    public static final String[] BANKS = { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15" };
    public static final String[] ALLOCATOR_STEAL_TYPES = { "Mono Latest", "Mono Earliest", "Lowest", "Highest" };
    public static final String[] ARP_CLOCKS = { "1/1", "1/2", "1/4", "1/8 Dot", "1/4 Triplet", "1/8", "1/8 Triplet", "1/16", "1/16 Triplet", "1/32", "Fast", "Max" };
    public static final String[] ARP_DIRECTIONS = { "Up", "Down", "AltUp", "AltDown" };
    public static final String[] ARP_MODES = { "Off", "Normal", "One Shot", "Hold" };
    public static final String[] DIGIVCF_FILTER_TYPES = { "LP 12", "BP 12", "HP 12", "[M] Notch 12", "[M] LP 24", "[M] BP 24", "[M] HP 24", "[M] Notch 24", "[M] Sin + LP", "[M] Dual LP/BP", "[M] Band Stop 12", "[M] S&H + LP 12", "[M] Waveshaper", "[M] FM + LP 12", "[M] S&H + HP 12", "[M] FM + HP 12", "Frequency Boost", "[M] M Waveshaper" };
    // public static final String[] DIGIVCF_FILTER_TYPES = { "LP 12", "BP 12", "HP 12", "Notch 12 [M]", "LP 24 [M]", "BP 24 [M]", "HP 24 [M]", "Notch 24 [M]", "Sin + LP [M]", "Dual LP/BP [M]", "Band Stop 12", "S&H + LP 12 [M]", "Waveshaper [M]", "FM + LP 12 [M]", "S&H + HP 12 [M]", "FM + HP 12 [M]", "Frequency Boost [M]", "M Waveshaper [M]" };
    public static final String[] ENGINE_PLAY_MODES = { "Mono Retrig", "Mono Legato", "Polyphonic" };
    public static final String[] ENV_RATES = { "Slow", "Fast", "Normal" };              // Note M differs from docs, which say Fast, Normal, Slow
    public static final String[] LFO_RANGE_TYPES = { "Normal", "Slow", "Fast" };                // Note M differs from docs, which say Fast, Normal, Slow
    public static final String[] LFO_SHAPES = { "Sine", "Triangle", "Pulse", "Random", "S&H" };
    public static final String[] LFO_MIDI_SYNC_VALUES = { "1024 Bars", "512 Bars", "256 Bars", "192 Bars", "128 Bars", "96 Bars", "64 Bars", "48 Bars", "32 Bars", "24 Bars", "16 Bars", "12 Bars", "8 Bars", "6 Bars", "4 Bars", "3 Bars", "2 Bars", "6/4", "4/4", "3/4", "1/2", "3/8", "1/4", "3/16", "1/8", "3/32", "1/16", "3/64", "1/32" };
    public static final String[] MOD_SOURCES = { "Off", "Mod Wheel", "Pitch Bend", "LFO 1", "LFO 2", "Amp Env", "Filter Env", "Wave Env", "Free Env", "LFO1 AD Env", "Sustain Pedal", "Volume Ctrl", "Pan Ctrl", "Breath Ctrl", "Foot Ctrl", "Expression Ctrl", "Ctrl A", "Ctrl B", "Ctrl C", "Ctrl D", "Ctrl W", "Ctrl X", "Ctrl Y", "Ctrl Z", "Key Tracking", "Velocity", "Release Vel", "Aftertouch", "Poly Pressure", "Global LFO", "min", "MAX", "Inverse", "Coin Flip", "Random", "Gate", "Noise" };
    public static final String[] OSC_MODES = { "Microwave 1 (Classic)", "Microwave 2 (Modern)" };
    public static final String[] TRANSITION_LOOP_TYPES = { "Off", "Forward", "Backward", "Start+Fwd", "Start+Bkwd" };
    public static final String[] TIME_LEVEL_MOD_MODES = { "M", "Microwave" };
    public static final String[] GLIDE_MODE_TYPES = { "Linear", "Exponential" };
    public static final String[] WAVE_TRAVEL_MODES = { "Analog On", "Analog Off", "Analog Only", "Circular" };
    public static final String[] WAVETABLES = {  "Resonant", "Resonant2 LP", "Mallet Synth", "Square Sweep", "Bell", "Pulse Sweep", "Saw Sweep", "Mellow Saw",
        "Feedback", "Add Harm", "Resonant3 HP", "Wind Synth", "High Harm", "Clipper", "Organ Synth", "Square Saw",
        "Formant1", "Polated", "Transient", "Elec Piano", "Robotic", "Strong Harm", "Perc Organ", "Clip'n'Sweep",
        "Reso Harm", "2 Echoes", "Formant2", "Formant Voc", "Micro Sync", "Micro PWM", "Glassy", "Square HP",
        "Saw Sync1", "Saw Sync2", "Saw Sync3", "Pulse Sync1", "Pulse Sync2", "Pulse Sync3", "Sine Sync1", "Sine Sync2",
        "Sine Sync3", "PWM Pulse", "PWM Saw", "Fuzz Wave", "Distorted", "Heavy Fuzz", "Fuzz Sync", "KarpStrong1",
        "KarpStrong2", "KarpStrong3", "1-2-3-4-5", "19/20", "Wavetrip1", "Wavetrip2", "Wavetrip3", "Wavetrip4",
        "Male Voice", "Low Piano", "Resonant Sweep", "Xmas Bell", "FM Piano", "FAT Organ", "Vibes", "Chorus2",
        "True PWM", "Wave23 SawSync", "Wave23 UpWaves", "Q Alt1", "Q Alt2", "Wave22 Clipper", "Wave22 PSax", "Wave23 PSax",
        "Mayschoss Wine", "Parks of Bonn", "Niederzissen", "Kripper Ferry", "Sinziger Organ", "La Palma's Sun", "Remagen Works", "Ahrtal Walks",
        "3V Multi Aah", "3V Clavinet", "3V Cncert Gtar", "3V Celeste", "3V Marimba", "3V Bell", "3V Grand Piano", "3V ElPiano",
        "3V Trombone", "3V Pluck Bass", "3V GL Bass", "3V Harp", "3V Sitar", "3V ElGitar TTR", "3V Y-Synth", "3V HausOrgan",
        "User WT 00", "User WT 01", "User WT 02", "User WT 03", "User WT 04", "User WT 05", "User WT 06", "User WT 07", 
        "User WT 08", "User WT 09", "User WT 10", "User WT 11", "User WT 12", "User WT 13", "User WT 14", "User WT 15", 
        "User WT 16", "User WT 17", "User WT 18", "User WT 19", "User WT 20", "User WT 21", "User WT 22", "User WT 23", 
        "User WT 24", "User WT 25", "User WT 26", "User WT 27", "User WT 28", "User WT 29", "User WT 30", "User WT 31" };
    public static final String[] LFO_RATES = { 
        "0.121", "0.242", "0.363", "0.485", "0.606", "0.727", "0.848", "0.970", 
        "1.091", "1.212", "1.333", "1.455", "1.576", "1.697", "1.818", "1.940", 
        "2.061", "2.182", "2.303", "2.425", "2.546", "2.667", "2.788", "2.910", 
        "3.031", "3.152", "3.274", "3.395", "3.516", "3.637", "3.759", "3.880", 
        "4.001", "4.112", "4.244", "4.365", "4.486", "4.607", "4.729", "4.850", 
        "4.971", "5.092", "5.214", "5.335", "5.456", "5.577", "5.699", "5.820", 
        "5.941", "6.062", "6.814", "6.305", "6.426", "6.548", "6.669", "6.790", 
        "6.911", "7.033", "7.154", "7.275", "7.396", "7.518", "7.639", "7.760", 
        "7.881", "8.003", "8.124", "8.245", "8.366", "8.488", "8.609", "8.730", 
        "8.851", "8.973", "9.094", "9.215", "9.337", "9.458", "9.579", "9.700", 
        "9.822", "9.943", "10.064", "10.185", "10.307", "10.428", "10.549", "10.670", 
        "10.792", "10.913", "11.034", "11.155", "11.277", "11.398", "11.519", "11.640", 
        "11.762", "11.883", "12.004", "12.125", "12.247", "12.368", "12.489", "12.611", 
        "12.732", "12.853", "12.974", "13.096", "13.217", "13.338", "13.459", "13.581", 
        "13.702", "13.823", "13.944", "14.066", "14.187", "14.308", "14.429", "14.551", 
        "14.672", "14.793", "14.914", "15.036", "15.157", "15.278", "15.400" };
    public static final String[] LFO_RATES_SLOW = {
/*
  "1.21", "2.42", "3.63", "4.85", "6.06", "7.27", "8.48", "9.70", 
  "10.91", "12.12", "13.33", "14.55", "15.76", "16.97", "18.18", "19.40", 
  "20.61", "21.82", "23.03", "24.25", "25.46", "26.67", "27.88", "29.10", 
  "30.31", "31.52", "32.74", "33.95", "35.16", "36.37", "37.59", "38.80", 
  "40.01", "41.12", "42.44", "43.65", "44.86", "46.07", "47.29", "48.50",
  "49.71", "50.92", "52.14", "53.35", "54.56", "55.77", "56.99", "58.20", 
  "59.41", "60.62", "68.14", "63.05", "64.26", "65.48", "66.69", "67.90", 
  "69.11", "70.33", "71.54", "72.75", "73.96", "75.18", "76.39", "77.60", 
  "78.81", "80.03", "81.24", "82.45", "83.66", "84.88", "86.09", "87.30", 
  "88.51", "89.73", "90.94", "92.15", "93.37", "94.58", "95.79", "97.00", 
  "98.22", "99.43", "100.64", "101.85", "103.07", "104.28", "105.49", "106.70", 
  "107.92", "109.13", "110.34", "111.55", "112.77", "113.98", "115.19", "116.40", 
  "117.62", "118.83", "120.04", "121.25", "122.47", "123.68", "124.89", "126.11", 
  "127.32", "128.53", "129.74", "130.96", "132.17", "133.38", "134.59", "135.81", 
  "137.02", "138.23", "139.44", "140.66", "141.87", "143.08", "144.29", "145.51", 
  "146.72", "147.93", "149.14", "150.36", "151.57", "152.78", "15.40" 
*/
        "0.484", "0.968", "1.452", "1.940", "2.424", "2.908", "3.392", "3.880", 
        "4.364", "4.848", "5.332", "5.820", "6.304", "6.788", "7.272", "7.760", 
        "8.244", "8.728", "9.212", "9.700", "10.184", "10.668", "11.152", "11.640", 
        "12.124", "12.608", "13.096", "13.580", "14.064", "14.548", "15.036", "15.520", 
        "16.004", "16.448", "16.976", "17.460", "17.944", "18.428", "18.916", "19.400", 
        "19.884", "20.368", "20.856", "21.340", "21.824", "22.308", "22.796", "23.280", 
        "23.764", "24.248", "27.256", "25.220", "25.704", "26.192", "26.676", "27.160", 
        "27.644", "28.132", "28.616", "29.100", "29.584", "30.072", "30.556", "31.040", 
        "31.524", "32.012", "32.496", "32.980", "33.464", "33.952", "34.436", "34.920", 
        "35.404", "35.892", "36.376", "36.860", "37.348", "37.832", "38.316", "38.800", 
        "39.288", "39.772", "40.256", "40.740", "41.228", "41.712", "42.196", "42.680", 
        "43.168", "43.652", "44.136", "44.620", "45.108", "45.592", "46.076", "46.560", 
        "47.048", "47.532", "48.016", "48.500", "48.988", "49.472", "49.956", "50.444", 
        "50.928", "51.412", "51.896", "52.384", "52.868", "53.352", "53.836", "54.324", 
        "54.808", "55.292", "55.776", "56.264", "56.748", "57.232", "57.716", "58.204", 
        "58.688", "59.172", "59.656", "60.144", "60.628", "61.112", "61.600" };
    public static final String[] LFO_RATES_FAST = {
/*
  "0.030", "0.060", "0.091", "0.121", "0.152", "0.182", "0.212", "0.243",
  "0.273", "0.303", "0.333", "0.364", "0.394", "0.424", "0.454", "0.485", 
  "0.515", "0.545", "0.576", "0.606", "0.637", "0.667", "0.697", "0.728", 
  "0.758", "0.788", "0.818", "0.849", "0.879", "0.909", "0.940", "0.970", 
  "1.000", "1.028", "1.061", "1.091", "1.122", "1.152", "1.182", "1.212", 
  "1.243", "1.273", "1.304", "1.334", "1.364", "1.394", "1.425", "1.455", 
  "1.485", "1.515", "1.704", "1.576", "1.607", "1.637", "1.667", "1.697", 
  "1.728", "1.758", "1.788", "1.819", "1.849", "1.880", "1.910", "1.940", 
  "1.970", "2.001", "2.031", "2.061", "2.092", "2.122", "2.152", "2.182", 
  "2.213", "2.243", "2.273", "2.304", "2.334", "2.365", "2.395", "2.425", 
  "2.455", "2.486", "2.516", "2.546", "2.577", "2.607", "2.637", "2.668", 
  "2.698", "2.728", "2.759", "2.789", "2.819", "2.849", "2.880", "2.910", 
  "2.941", "2.971", "3.001", "3.031", "3.062", "3.092", "3.122", "3.153", 
  "3.183", "3.213", "3.243", "3.274", "3.304", "3.335", "3.365", "3.395", 
  "3.425", "3.456", "3.486", "3.516", "3.547", "3.577", "3.607", "3.638", 
  "3.668", "3.698", "3.728", "3.759", "3.789", "3.819", "3.850" 
*/
        "0.012", "0.024", "0.036", "0.048", "0.060", "0.072", "0.084", "0.097", 
        "0.109", "0.121", "0.133", "0.145", "0.157", "0.169", "0.181", "0.194", 
        "0.206", "0.218", "0.230", "0.242", "0.254", "0.266", "0.278", "0.291", 
        "0.303", "0.315", "0.327", "0.339", "0.351", "0.363", "0.375", "0.388", 
        "0.400", "0.411", "0.424", "0.436", "0.448", "0.460", "0.472", "0.485", 
        "0.497", "0.509", "0.521", "0.533", "0.545", "0.557", "0.569", "0.582", 
        "0.594", "0.606", "0.681", "0.630", "0.642", "0.654", "0.666", "0.679", 
        "0.691", "0.703", "0.715", "0.727", "0.739", "0.751", "0.763", "0.776", 
        "0.788", "0.800", "0.812", "0.824", "0.836", "0.848", "0.860", "0.873", 
        "0.885", "0.897", "0.909", "0.921", "0.933", "0.945", "0.957", "0.970", 
        "0.982", "0.994", "1.006", "1.018", "1.030", "1.042", "1.054", "1.067", 
        "1.079", "1.091", "1.103", "1.115", "1.127", "1.139", "1.151", "1.164", 
        "1.176", "1.188", "1.200", "1.212", "1.224", "1.236", "1.248", "1.261", 
        "1.273", "1.285", "1.297", "1.309", "1.321", "1.333", "1.345", "1.358", 
        "1.370", "1.382", "1.394", "1.406", "1.418", "1.430", "1.442", "1.455", 
        "1.467", "1.479", "1.491", "1.503", "1.515", "1.527", "1.540" };


    public WaldorfM()
        {
        if (parametersToIndex == null)
            {
            parametersToIndex = new HashMap();
            for(int i = 0; i < parameters.length; i++)
                {
                parametersToIndex.put(parameters[i], Integer.valueOf(i));
                }
            }

        if (ccToIndex == null)
            {
            ccToIndex = new HashMap();
            for(int i = 0; i < cc.length; i++)
                {
                ccToIndex.put(Integer.valueOf(cc[i]), Integer.valueOf(i));
                }
            }

        /// SOUND PANEL
                
        JComponent soundPanel = new SynthPanel(this);
        VBox vbox = new VBox();
        
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        hbox.add(addGeneral(Style.COLOR_B()));
        hbox.addLast(addGlide(Style.COLOR_C()));
        vbox.add(hbox);

        JComponent comp = addOscillator(1, Style.COLOR_A());
        JComponent oscillatorStrut1 = Strut.makeStrut(comp, false);
        JComponent oscillatorStrut2 = Strut.makeStrut(comp, false);

        hbox = new HBox();
        hbox.add(comp);
        hbox.addLast(addOscillatorMod(1, Style.COLOR_B()));
        vbox.add(hbox);
        
        hbox = new HBox();
        hbox.add(addOscillator(2, Style.COLOR_A()));
        hbox.addLast(addOscillatorMod(2, Style.COLOR_B()));
        vbox.add(hbox);
        
        hbox = new HBox();
        VBox strutBox = new VBox();
        strutBox.add(oscillatorStrut1);
        strutBox.add(addWavetable(1, Style.COLOR_A()));
        hbox.add(strutBox);
        hbox.addLast(addWavetableMod(1, Style.COLOR_B()));
        vbox.add(hbox);

        wavetable2Box = new HBox();
        hbox = new HBox();
        wavetable2 = hbox;
        strutBox = new VBox();
        strutBox.add(oscillatorStrut2);
        strutBox.add(addWavetable(2, Style.COLOR_A()));
        hbox.add(strutBox);
        hbox.addLast(addWavetableMod(2, Style.COLOR_B()));
        wavetable2Box.addLast(hbox);
        vbox.add(wavetable2Box);

        transitionBox = new HBox();
        transitionBox.addLast(transition = addTransition(Style.COLOR_C()));
        vbox.add(transitionBox);

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Oscillators", soundPanel);
                
                
        soundPanel = new SynthPanel(this);
                
        // First build the modulations
        VBox right = new VBox();
        HBox box = new HBox();
        Category cat = (Category)(addMixerMod(Style.COLOR_B()));
        right.add(cat);
        right.add(addAmplifierMod(Style.COLOR_B()));
        right.add(addAnalogFilterMod(Style.COLOR_B()));
        right.add(addDigitalFilterMod(Style.COLOR_B()));

        // Next build the main column, with struts
        VBox left = new VBox();
        HBox row = new HBox();
        row.add(Strut.makeStrut(cat, true, false));
        row.addLast(addMixer(Style.COLOR_A()));
        left.add(row);
        row = new HBox();
        row.add(Strut.makeStrut(cat, true, false));
        row.addLast(addAmplifier(Style.COLOR_A()));
        left.add(row);
        row = new HBox();
        row.add(Strut.makeStrut(cat, true, false));
        row.addLast(addAnalogFilter(Style.COLOR_A()));
        left.add(row);
        row = new HBox();
        row.add(Strut.makeStrut(cat, true, false));
        row.addLast(addDigitalFilter(Style.COLOR_A()));
        left.add(row);

        // Now put them together
        hbox = new HBox();
        hbox.add(left);
        hbox.addLast(right);
        soundPanel.add(hbox, BorderLayout.CENTER);
        addTab("Mixer VCF VCA", soundPanel);
        
        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        hbox = new HBox(HBox.LEFT_CONSUMES);
        hbox.addLast(addEnvelope(1, Style.COLOR_A()));
        hbox.add(addEnvelopeMod(1, Style.COLOR_B()));
        vbox.add(hbox);

        hbox = new HBox(HBox.LEFT_CONSUMES);
        hbox.addLast(addEnvelope(2, Style.COLOR_A()));
        hbox.add(addEnvelopeMod(2, Style.COLOR_B()));
        vbox.add(hbox);

        hbox = new HBox(HBox.LEFT_CONSUMES);
        hbox.addLast(addFreeEnvelope(Style.COLOR_A()));
        hbox.add(addFreeEnvelopeMod(Style.COLOR_B()));
        vbox.add(hbox);

        hbox = new HBox(HBox.LEFT_CONSUMES);
        hbox.addLast(addWaveEnvelope(Style.COLOR_A()));
        hbox.add(addWaveEnvelopeMod(Style.COLOR_B()));
        vbox.add(hbox);

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Envelopes", soundPanel);

        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        hbox = new HBox(HBox.LEFT_CONSUMES);
        hbox.addLast(addLFO(1, Style.COLOR_A()));
        hbox.add(addLFO1Mod(Style.COLOR_B()));
        vbox.add(hbox);
        vbox.add(addLFO(2, Style.COLOR_A()));
        vbox.add(addGlobalLFO(Style.COLOR_A()));
        vbox.add(addArpeggiator(Style.COLOR_C()));
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("LFOs Arp", soundPanel);

        model.set("bank", 0);
        model.set("number", 0);
        model.set("name", "Untitled");

        loadDefaults();
        readyForRevision = true;
        reviseUI();
        }
                
                
                
    public String getDefaultResourceFileName() { return "WaldorfM.init"; }
    public String getHTMLResourceFileName() { return "WaldorfM.html"; }



    // HIDING AND SHOWING STUFF
    //
    // The M is unusual in that the stuff you hide and show in response
    // to certain parameters turning stuff on and off is strewn throught
    // the synthesizer, not in one place (so we can't hide a single
    // category or a single tab in response, say). Also there are two
    // parameters which combined change different things together.  
    // So I have a general function called reviseUI which is called
    // when either of these parameters is updated to hide and show
    // the appropriate stuff. 
        
    // ALSO NOTE that while Edisyn is hiding stuff (in response to
    // user requests btw) the M itself does *not*.  That's a bit
    // confusing.
                
    HBox wavetable2Box = new HBox();
    JComponent wavetable2;
    HBox transitionBox = new HBox();
    JComponent transition;
    HBox asicBugBox = new HBox();
    JComponent asicBug;
    HBox enableTransitionBox = new HBox();
    JComponent enableTransition;
    HBox ringModBox = new HBox();
    JComponent ringMod;
    HBox ringModSourceBox = new HBox();
    JComponent ringModSource;
    HBox ringModAmountBox = new HBox();
    JComponent ringModAmount;
    HBox digitalFilterModBox1 = new HBox();
    JComponent digitalFilterMod1;
    HBox digitalFilterModBox2 = new HBox();
    JComponent digitalFilterMod2;
    HBox digitalFilterModBox3 = new HBox();
    JComponent digitalFilterMod3;
    HBox osc1SyncBox = new HBox();
    JComponent osc1Sync;
    HBox smoothScanBox = new HBox();
    JComponent smoothScan;
    boolean readyForRevision = false;
        
    public void reviseUI()
        {
        if (!readyForRevision) return;
                
        // remove everything
        wavetable2Box.removeAll();
        transitionBox.removeAll();
        asicBugBox.removeAll();
        enableTransitionBox.removeAll();
        ringModBox.removeAll();
        ringModSourceBox.removeAll();
        ringModAmountBox.removeAll();
        digitalFilterModBox1.removeAll();
        digitalFilterModBox2.removeAll();
        digitalFilterModBox3.removeAll();
        osc1SyncBox.removeAll();
        smoothScanBox.removeAll();

        // selectively put back
        if (model.get("mmwmode") == 1)          // Modern = 1
            {
            ringModBox.addLast(ringMod);
            ringModSourceBox.addLast(ringModSource);
            ringModAmountBox.addLast(ringModAmount);
            digitalFilterModBox1.addLast(digitalFilterMod1);
            digitalFilterModBox2.addLast(digitalFilterMod2);
            digitalFilterModBox3.addLast(digitalFilterMod3);
            osc1SyncBox.addLast(osc1Sync);
            smoothScanBox.addLast(smoothScan);
            wavetable2Box.addLast(wavetable2);
            }
        else                    // Classic = 0
            {
            enableTransitionBox.addLast(enableTransition);
            asicBugBox.addLast(asicBug);                    

            if (model.get("transitionenable") == 1)
                {
                transitionBox.addLast(transition);
                }
            else
                {
                wavetable2Box.addLast(wavetable2);
                }
            }
                        
        // revalidate
        wavetable2Box.revalidate();
        transitionBox.revalidate();
        asicBugBox.revalidate();
        enableTransitionBox.revalidate();
        ringModBox.revalidate();
        ringModSourceBox.revalidate();
        ringModAmountBox.revalidate();
        digitalFilterModBox1.revalidate();
        digitalFilterModBox2.revalidate();
        digitalFilterModBox2.revalidate();
        osc1SyncBox.revalidate();
        smoothScanBox.revalidate();
                
        // repaint
        wavetable2Box.repaint();
        transitionBox.repaint();
        asicBugBox.repaint();
        enableTransitionBox.repaint();
        ringModBox.repaint();
        ringModSourceBox.repaint();
        ringModAmountBox.repaint();
        digitalFilterModBox1.repaint();
        digitalFilterModBox2.repaint();
        digitalFilterModBox3.repaint();
        osc1SyncBox.repaint();
        smoothScanBox.repaint();
        }



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
        HBox hbox = new HBox();
        VBox vbox = new VBox();

        VBox inner = new VBox();
        comp = new PatchDisplay(this, 6);
        inner.add(comp);
        hbox.add(inner);
                
        inner = new VBox();        
        /*params = OSC_MODES;
          comp = new Chooser("Synth Mode", this, "mmwmode", params)
          {
          public void update(String key, Model model)
          {
          super.update(key, model);
          reviseUI();
          }
          };
        */
        comp = new CheckBox("Modern (MWII/XT) Mode", this, "mmwmode")
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                reviseUI();
                }
            };
        inner.add(comp);
        
        HBox strutBox = new HBox();
        comp = new CheckBox("Enable Transition", this, "transitionenable")
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                reviseUI();
                }
            };
        enableTransition = comp;
        enableTransitionBox.addLast(comp);
        strutBox.addLast(enableTransitionBox);
        strutBox.add(Strut.makeStrut(comp, true)); // so it doesn't collapse
        
        inner.add(strutBox);
        hbox.add(inner);
        vbox.add(hbox);
        
        comp = new StringComponent("Patch Name", this, "name", 23, "Name must be up to 23 ASCII characters.")
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
        
        globalCategory.add(vbox, BorderLayout.WEST);
        return globalCategory;
        }

    public static final int MAXIMUM_NAME_LENGTH = 23;
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
        // Handle Global LFO Rate and ENV Timer Resolution, which can be out of whack
        if (model.get("globallforate") < 1 || model.get("globallforate") > 127)
            model.set("globallforate", 1);              // default
                
        if (model.get("envtimeresolution") < 0 || model.get("envtimeresolution") >= ENV_RATES.length)
            model.set("envtimeresolution", 1);              // "Normal"
        
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
        params = ENGINE_PLAY_MODES;
        comp = new Chooser("Play Mode", this, "playmode", params);
        vbox.add(comp);
 
        params = ALLOCATOR_STEAL_TYPES;
        comp = new Chooser("Voice Steal Mode", this, "voicestealmode", params);
        vbox.add(comp);
        hbox.add(vbox);

        vbox = new VBox();

        params = TIME_LEVEL_MOD_MODES;
        comp = new Chooser("Time/Level Mod Mode", this, "timelevelmodmode", params);
        vbox.add(comp);

        comp = new CheckBox("ASIC Bug", this, "oscasicbug");
        asicBug = comp;
        asicBugBox.addLast(asicBug);
        vbox.add(asicBugBox);

        hbox.add(vbox);


        vbox = new VBox();
        params = ENV_RATES;
        // we add some spaces at the end so Smooth Wavetable Scan doesn't change the width
        comp = new Chooser("Envelope Timer Resolution      ", this, "envtimeresolution", params);
        vbox.add(comp);

        comp = new CheckBox("Smooth Wavetable Scan", this, "smoothscanwt");
        smoothScan = comp;
        smoothScanBox.addLast(smoothScan);
        vbox.add(smoothScanBox);
 
        hbox.add(vbox);
        
        comp = new LabelledDial("Oscillator", this, "oscsderez", color, 0, 14);
        ((LabelledDial)comp).addAdditionalLabel("De-Rez");
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addGlide(Color color)
        {
        Category category = new Category(this, "Glide", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();

        VBox vbox = new VBox();
        params = GLIDE_MODE_TYPES;
        comp = new Chooser("Mode", this, "glidemode", params);
        vbox.add(comp);
 
        comp = new CheckBox("Enable", this, "glide");
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Rate", this, "gliderate", color, 0, 127);
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

 
    public JComponent addWavetable(int osc, Color color)
        {
        Category category = new Category(this, "Wavetable " + osc, color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = new String[WAVETABLES.length];
        for(int i = 0; i < params.length; i++)
            {
            params[i] = (i < 10 ? "0" : "") + i + " " + WAVETABLES[i];
            }
        comp = new Chooser("Wavetable", this, "wave" + osc + "wavetable", params);
        vbox.add(comp);
 
        params = WAVE_TRAVEL_MODES;
        comp = new Chooser("Wave Travel Mode", this, "wave" + osc + "wavetravelmode", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Start Wave", this, "wave" + osc + "startwave", color, 0, 63);
        hbox.add(comp);

        // WAVE_PHASE missing in docs
        comp = new LabelledDial("Start Phase", this, "wave" + osc + "startphase", color, 0, 127);
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addOscillator(int osc, Color color)
        {
        Category category = new Category(this, "Oscillator " + osc, color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        comp = new CheckBox("Pitch Mode", this, "osc" + osc + "pitchmode");
        vbox.add(comp);

        if (osc == 2)
            {
            comp = new CheckBox("Osc 1 Sync", this, "osc" + osc + "synctoosc1");
            ((CheckBox)comp).addToWidth(2);
            osc1Sync = comp;
            osc1SyncBox.addLast(osc1Sync);
            vbox.add(osc1SyncBox);
            }
 
        hbox.add(vbox);

        comp = new LabelledDial("Octave", this, "osc" + osc + "octave", color, -2, 2);
        hbox.add(comp);

        comp = new LabelledDial("Semitone", this, "osc" + osc + "semitone", color, -12, 12);
        hbox.add(comp);

        comp = new LabelledDial("Detune", this, "osc" + osc + "detune", color, -64, 63);
        hbox.add(comp);

        comp = new LabelledDial("Bend Range", this, "osc" + osc + "bendrange", color, 0, 48);
        hbox.add(comp);

        // default is 24, representing Middle C
        comp = new LabelledDial("Fixed Pitch", this, "osc" + osc + "fixedpitch", color, 0, 60)
            {
            public String map(int val)
                {
                return NOTES[val % 12] + ((val / 12) - 2);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Base");
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addOscillatorMod(int osc, Color color)
        {
        Category category = new Category(this, "Oscillator " + osc + " Modulation", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();

        params = MOD_SOURCES;
        comp = new Chooser("Mod 1 Source", this, "osc" + osc + "mod1source", params);
        vbox.add(comp);

        params = MOD_SOURCES;
        comp = new Chooser("Mod 1 Control", this, "osc" + osc + "mod1control", params);
        vbox.add(comp);
        
        hbox.add(vbox);

        comp = new LabelledDial("Mod 1", this, "osc" + osc + "mod1amount", color, -64, 63);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        vbox = new VBox();
        params = MOD_SOURCES;
        comp = new Chooser("Mod 2 Source", this, "osc" + osc + "mod2source", params);
        vbox.add(comp);

        if (osc == 2)
            {
            comp = new CheckBox("Link to Osc 1", this, "osc" + osc + "linkosc1mods");
            ((CheckBox)comp).addToWidth(1);
            vbox.add(comp);
            }
        hbox.add(vbox);

        comp = new LabelledDial("Mod 2", this, "osc" + osc + "mod2amount", color, -64, 63);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        comp = new LabelledDial("Mod 2", this, "osc" + osc + "mod2quantize", color, 0, 7);
        ((LabelledDial)comp).addAdditionalLabel("Quantize");
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addWavetableMod(int osc, Color color)
        {
        Category category = new Category(this, "Wavetable " + osc + " Modulation", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = MOD_SOURCES;
        comp = new Chooser("Mod 1 Source", this, "wave" + osc + "mod1source", params);
        vbox.add(comp);

        params = MOD_SOURCES;
        comp = new Chooser("Mod 1 Control", this, "wave" + osc + "mod1control", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Mod 1", this, "wave" + osc + "mod1amount", color, -64, 63);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        vbox = new VBox();
        params = MOD_SOURCES;
        comp = new Chooser("Mod 2 Source", this, "wave" + osc + "mod2source", params);
        vbox.add(comp);
        if (osc == 2)
            {
            comp = new CheckBox("Link to Wave 1", this, "wave" + osc + "linkwave1mods");
            vbox.add(comp);
            }
        hbox.add(vbox);

        comp = new LabelledDial("Mod 2", this, "wave" + osc + "mod2amount", color, -64, 63);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        comp = new LabelledDial("Wave Env", this, "wave" + osc + "envamount", color, -64, 63);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        comp = new LabelledDial("Wave Env", this, "wave" + osc + "envvelocity", color, -64, 63);
        ((LabelledDial)comp).addAdditionalLabel("Velocity");
        hbox.add(comp);

        comp = new LabelledDial("Keytrack", this, "wave" + osc + "keytrack", color, -64, 63);
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addMixer(Color color)
        {
        Category category = new Category(this, "Mixer", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        comp = new LabelledDial("Osc 1", this, "mixosc1level", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Osc 2", this, "mixosc2level", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Noise", this, "mixnoiselevel", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Ring Mod", this, "mixringmodlevel", color, 0, 127);
        ringMod = comp;
        ringModBox.addLast(ringMod);
        hbox.add(ringModBox);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    // This is used for sizing
    public JComponent addMixerMod(Color color)
        {
        Category category = new Category(this, "Mixer Modulation", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = MOD_SOURCES;
        comp = new Chooser("Osc 1 Mod Source", this, "mixosc1modsource", params);
        vbox.add(comp);

        params = MOD_SOURCES;
        comp = new Chooser("Osc 2 Mod Source", this, "mixosc2modsource", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Osc 1 Mod", this, "mixosc1modamount", color, -64, 63);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        comp = new LabelledDial("Osc 2 Mod", this, "mixosc2modamount", color, -64, 63);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        vbox = new VBox();
        params = MOD_SOURCES;
        comp = new Chooser("Noise Mod Source", this, "mixnoisemodsource", params);
        vbox.add(comp);
        hbox.add(vbox);

        params = MOD_SOURCES;
        comp = new Chooser("Ring Mod Source", this, "mixringmodmodsource", params);
        ringModSource = comp;
        ringModSourceBox.add(ringModSource);
        vbox.add(ringModSourceBox);

        comp = new LabelledDial("Noise Mod", this, "mixnoisemodamount", color, -64, 63);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        comp = new LabelledDial("Ring Mod", this, "mixringmodmodamount", color, -64, 63);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        ringModAmount = comp;
        ringModAmountBox.add(ringModAmount);
        hbox.add(ringModAmountBox);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addDigitalFilter(Color color)
        {
        Category category = new Category(this, "Digital Filter", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = DIGIVCF_FILTER_TYPES;
        comp = new Chooser("Type", this, "digivcftype", params);
        vbox.add(comp);
        comp = new CheckBox("Enable", this, "digivcfenabled");
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Cutoff", this, "digivcfcutoff", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Resonance", this, "digivcfresonance", color, 0, 127);
        hbox.add(comp);

        // FIXME: This probably changes name depending on digital filter type
        comp = new LabelledDial("Extra", this, "digivcfextraparam", color, 0, 127);
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
        
    public JComponent addAnalogFilter(Color color)
        {
        Category category = new Category(this, "Analog Filter", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        comp = new LabelledDial("Cutoff", this, "vcfcutoff", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Resonance", this, "vcfresonance", color, 0, 127);
        hbox.add(comp);
        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addDigitalFilterMod(Color color)
        {
        Category category = new Category(this, "Digital Filter Modulation", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        

        digitalFilterMod1 = new HBox();
        VBox vbox = new VBox();
        params = MOD_SOURCES;
        comp = new Chooser("Mod 1 Source", this, "digivcfmod1source", params);
        vbox.add(comp);

        params = MOD_SOURCES;
        comp = new Chooser("Mod 1 Control", this, "digivcfmod1control", params);
        vbox.add(comp);
        digitalFilterMod1.add(vbox);

        comp = new LabelledDial("Mod 1", this, "digivcfmod1amount", color, -64, 63);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        digitalFilterMod1.add(comp);
        digitalFilterModBox1.add(digitalFilterMod1);
        hbox.add(digitalFilterModBox1);
                
        vbox = new VBox();
        params = MOD_SOURCES;
        comp = new Chooser("Mod 2 Source", this, "digivcfmod2source", params);
        vbox.add(comp);

        params = MOD_SOURCES;
        comp = new Chooser("Resonance Mod Source", this, "digivcfresmodsource", params);
        digitalFilterMod2 = comp;
        digitalFilterModBox2.add(digitalFilterMod2);
        vbox.add(digitalFilterModBox2);
        hbox.add(vbox);

        comp = new LabelledDial("Mod 2", this, "digivcfmod2amount", color, -64, 63);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        digitalFilterMod3 = new HBox();
        comp = new LabelledDial("Resonance Mod", this, "digivcfresmodamount", color, -64, 63);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        digitalFilterMod3.add(comp);

        comp = new LabelledDial("VCF Env", this, "digivcfenvamount", color, -64, 63);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        digitalFilterMod3.add(comp);

        comp = new LabelledDial("VCF Env", this, "digivcfenvvelocity", color, -64, 63);
        ((LabelledDial)comp).addAdditionalLabel("Velocity");
        digitalFilterMod3.add(comp);

        comp = new LabelledDial("Keytrack", this, "digivcfkeytrack", color, -64, 63);
        digitalFilterMod3.add(comp);
        digitalFilterModBox2.add(digitalFilterMod3);
        hbox.add(digitalFilterModBox3);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addAnalogFilterMod(Color color)
        {
        Category category = new Category(this, "Analog Filter Modulation", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = MOD_SOURCES;
        comp = new Chooser("Mod 1 Source", this, "vcfmod1source", params);
        vbox.add(comp);

        params = MOD_SOURCES;
        comp = new Chooser("Mod 1 Control", this, "vcfmod1control", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Mod 1", this, "vcfmod1amount", color, -64, 63);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        vbox = new VBox();
        params = MOD_SOURCES;
        comp = new Chooser("Mod 2 Source", this, "vcfmod2source", params);
        vbox.add(comp);
        params = MOD_SOURCES;
        comp = new Chooser("Resonance Mod Source", this, "vcfresmodsource", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Mod 2", this, "vcfmod2amount", color, -64, 63);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        comp = new LabelledDial("Resonance Mod", this, "vcfresmodamount", color, -64, 63);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        comp = new LabelledDial("VCF Env", this, "vcfenvamount", color, -64, 63);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        comp = new LabelledDial("VCF Env", this, "vcfenvvelocity", color, -64, 63);
        ((LabelledDial)comp).addAdditionalLabel("Velocity");
        hbox.add(comp);

        comp = new LabelledDial("Keytrack", this, "vcfkeytrack", color, -64, 63);
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addAmplifier(Color color)
        {
        Category category = new Category(this, "Amplifier", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        comp = new LabelledDial("Volume", this, "instrumentvolume", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Pan", this, "vcapan", color, -64, 63)
            {
            public String map(int val)
                {
                if (val == 0) return "--";
                else if (val < 0) return "< " + (0 - val);
                else return "" + val + " >";
                }
            };
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addAmplifierMod(Color color)
        {
        Category category = new Category(this, "Amplifier Modulation", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = MOD_SOURCES;
        comp = new Chooser("Mod 1 Source", this, "vcamod1source", params);
        vbox.add(comp);

        params = MOD_SOURCES;
        comp = new Chooser("Mod 1 Control", this, "vcamod1control", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Mod 1", this, "vcamod1amount", color, -64, 63);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        vbox = new VBox();
        params = MOD_SOURCES;
        comp = new Chooser("Mod 2 Source", this, "vcamod2source", params);
        vbox.add(comp);

        params = MOD_SOURCES;
        comp = new Chooser("Pan Mod Source", this, "vcapanmodsource", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Mod 2", this, "vcamod2amount", color, -64, 63);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        comp = new LabelledDial("Pan Mod", this, "vcapanmodamount", color, -64, 63);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        comp = new LabelledDial("VCA Env", this, "vcaenvamount", color, -64, 63);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        comp = new LabelledDial("VCA Env", this, "vcaenvvelocity", color, -64, 63);
        ((LabelledDial)comp).addAdditionalLabel("Velocity");
        hbox.add(comp);

        comp = new LabelledDial("Keytrack", this, "vcakeytrack", color, -64, 63);
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addEnvelope(int env, Color color)
        {
        Category category = new Category(this, (env == 1 ? "Amplifier Envelope" : "Filter Envelope") , color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        LabelledDial attack = new LabelledDial("Attack", this, "env" + env + "attack", color, 0, 127);

        if (env == 2)
            {
            comp = new LabelledDial("Delay", this, "env" + env + "delay", color, 0, 127);
            hbox.add(comp);
            }
        else
            {
            comp = Strut.makeStrut(attack);
            hbox.add(comp);
            }
        
        comp = attack;
        hbox.add(comp);

        comp = new LabelledDial("Decay", this, "env" + env + "decay", color, 0, 127);
        hbox.add(comp);
        
        comp = new LabelledDial("Sustain", this, "env" + env + "sustain", color, 0, 127);
        hbox.add(comp);
        
        comp = new LabelledDial("Release", this, "env" + env + "release", color, 0, 127);
        hbox.add(comp);

        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "env" + env +  "attack", "env" + env +  "decay", null, "env" + env +  "release" },
            new String[] { null, null, "env" + env +  "sustain", "env" + env +  "sustain", null },
            new double[] { 0, 0.25/127.0, 0.25 / 127.0,  0.25, 0.25/127.0},
            new double[] { 0, 1.0, 1.0 / 127.0, 1.0/127.0, 0 });
        hbox.addLast(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addEnvelopeMod(int env, Color color)
        {
        Category category = new Category(this, (env == 1 ? "Amplifier Envelope Modulation" : "Filter Envelope Modulation"), color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        
        if (env == 2)
            {
            params = MOD_SOURCES;
            comp = new Chooser("Delay Mod Source", this, "env" + env + "delaymodsource", params);
            vbox.add(comp);
            }

        params = MOD_SOURCES;
        comp = new Chooser("Attack Mod Source", this, "env" + env + "attackmodsource", params);
        vbox.add(comp);

        params = MOD_SOURCES;
        comp = new Chooser("Decay Mod Source", this, "env" + env + "decaymodsource", params);
        vbox.add(comp);
        hbox.add(vbox);

        vbox = new VBox();
        params = MOD_SOURCES;
        comp = new Chooser("Sustain Mod Source", this, "env" + env + "sustainmodsource", params);
        vbox.add(comp);

        params = MOD_SOURCES;
        comp = new Chooser("Release Mod Source", this, "env" + env + "releasemodsource", params);
        vbox.add(comp);
        hbox.add(vbox);


        LabelledDial decaymodamount = new LabelledDial("Decay Mod", this, "env" + env + "decaymodamount", color, -64, 63);
        ((LabelledDial)decaymodamount).addAdditionalLabel("Amount");


        if (env == 1)
            {
            hbox.add(Strut.makeStrut(decaymodamount));
            }
        else
            {
            comp = new LabelledDial("Delay Mod", this, "env" + env + "delaymodamount", color, -64, 63);
            ((LabelledDial)comp).addAdditionalLabel("Amount");
            hbox.add(comp);
            }

        comp = new LabelledDial("Attack Mod", this, "env" + env + "attackmodamount", color, -64, 63);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        hbox.add(decaymodamount);

        comp = new LabelledDial("Sustain Mod", this, "env" + env + "sustainmodamount", color, -64, 63);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        comp = new LabelledDial("Release Mod", this, "env" + env + "releasemodamount", color, -64, 63);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addFreeEnvelope(Color color)
        {
        Category category = new Category(this, "Free Envelope" , color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        comp = new CheckBox("Loop", this, "freeenvloop");
        vbox.add(comp);
        comp = new CheckBox("Release Loop", this, "freeenvloopatrelease");
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Time 1", this, "freeenvp1time", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Level 1", this, "freeenvp1level", color, 0, 127);
        hbox.add(comp);
        
        comp = new LabelledDial("Time 2", this, "freeenvp2time", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Level 2", this, "freeenvp2level", color, 0, 127);
        hbox.add(comp);
        
        comp = new LabelledDial("Time 3", this, "freeenvp3time", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Sustain Level", this, "freeenvsustainlevel", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Release Time", this, "freeenvreleasetime", color, 0, 127);
        hbox.add(comp);
        
        comp = new LabelledDial("Release Level", this, "freeenvreleaselevel", color, 0, 127);
        hbox.add(comp);

        // FIXME: Should the release level also be the start level?
        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "freeenvp1time", "freeenvp2time", "freeenvp3time", null, "freeenvreleasetime" },
            new String[] { null, "freeenvp1level", "freeenvp2level", "freeenvsustainlevel", "freeenvsustainlevel", "freeenvreleaselevel" },
            new double[] { 0, 0.2/127.0, 0.2/127.0, 0.2/127.0,  0.2, 0.2/127.0},
            new double[] { 0, 1.0 / 127.0, 1.0 / 127.0, 1.0 / 127.0, 1.0/127.0, 1.0 / 127.0 });
        hbox.addLast(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addFreeEnvelopeMod(Color color)
        {
        Category category = new Category(this, "Free Envelope Modulation", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = MOD_SOURCES;
        comp = new Chooser("Time Mod Source", this, "freeenvtimemodsource", params);
        vbox.add(comp);
        
        params = MOD_SOURCES;
        comp = new Chooser("Level Mod Source", this, "freeenvlevelmodsource", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Time Mod", this, "freeenvtimemodamount", color, -64, 63);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        comp = new LabelledDial("Level Mod", this, "freeenvlevelmodamount", color, -64, 63);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addWaveEnvelope(Color color)
        {
        Category category = new Category(this, "Wave Envelope" , color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        HBox outer = new HBox();
        VBox inner = new VBox();
        
        VBox vbox = new VBox();
        comp = new CheckBox("Loop", this, "wenvloopmode");
        ((CheckBox)comp).addToWidth(1);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Loop Begin", this, "wenvloopbegin", color, 0, 7);
        hbox.add(comp);
        
        comp = new LabelledDial(" Loop End ", this, "wenvloopend", color, 0, 7);
        hbox.add(comp);

        comp = new LabelledDial("Key Off Point", this, "wenvkeyoffpoint", color, 0, 7);
        hbox.add(comp);
        inner.add(hbox);
        
        EnvelopeDisplay disp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "wenvp1time", "wenvp2time", "wenvp3time", "wenvp4time", "wenvp5time", "wenvp6time", "wenvp7time", "wenvp8time" },
            new String[] { null, "wenvp1level", "wenvp2level", "wenvp3level", "wenvp4level", "wenvp5level", "wenvp6level", "wenvp7level", "wenvp8level" },
            new double[] { 0, 0.125/127.0, 0.125/127.0, 0.125/127.0, 0.125/127.0, 0.125/127.0, 0.125/127.0, 0.125/127.0, 0.125/127.0 },
            new double[] { 0, 1.0 / 127.0, 1.0 / 127.0, 1.0 / 127.0, 1.0 / 127.0, 1.0 / 127.0, 1.0 / 127.0, 1.0 / 127.0, 1.0 / 127.0,})
            {
            public int postProcessLoopOrStageKey(String key, int val)
                {
                // they'll all be off by 1
                return val + 1;
                }
            };
        disp.setPreferredWidth(disp.getPreferredWidth() * 2);
        disp.setLoopKeys(0, "wenvloopbegin", "wenvloopend");
        disp.setFinalStageKey("wenvkeyoffpoint");
        comp = disp;
        inner.add(comp);
        outer.add(inner);

        inner = new VBox();
        hbox = new HBox();
                
        comp = new LabelledDial("Time 1", this, "wenvp1time", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Time 2", this, "wenvp2time", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Time 3", this, "wenvp3time", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Time 4", this, "wenvp4time", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Time 5", this, "wenvp5time", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Time 6", this, "wenvp6time", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Time 7", this, "wenvp7time", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Time 8", this, "wenvp8time", color, 0, 127);
        hbox.add(comp);
                
        inner.add(hbox);
        hbox = new HBox();

        comp = new LabelledDial("Level 1", this, "wenvp1level", color, 0, 127);
        hbox.add(comp);
        
        comp = new LabelledDial("Level 2", this, "wenvp2level", color, 0, 127);
        hbox.add(comp);
        
        comp = new LabelledDial("Level 3", this, "wenvp3level", color, 0, 127);
        hbox.add(comp);
        
        comp = new LabelledDial("Level 4", this, "wenvp4level", color, 0, 127);
        hbox.add(comp);
        
        comp = new LabelledDial("Level 5", this, "wenvp5level", color, 0, 127);
        hbox.add(comp);
        
        comp = new LabelledDial("Level 6", this, "wenvp6level", color, 0, 127);
        hbox.add(comp);
        
        comp = new LabelledDial("Level 7", this, "wenvp7level", color, 0, 127);
        hbox.add(comp);
        
        comp = new LabelledDial("Level 8", this, "wenvp8level", color, 0, 127);
        hbox.add(comp);

        inner.add(hbox);
        outer.add(inner);

        category.add(outer, BorderLayout.CENTER);
        return category;
        }

    public JComponent addWaveEnvelopeMod(Color color)
        {
        Category category = new Category(this, "Wave Envelope Modulation", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = MOD_SOURCES;
        comp = new Chooser("Time Mod Source", this, "wenvtimemodsource", params);
        vbox.add(comp);

        params = MOD_SOURCES;
        comp = new Chooser("Level Mod Source", this, "wenvlevelmodsource", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Time Mod", this, "wenvtimemodamount", color, -64, 63);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        comp = new LabelledDial("Level Mod", this, "wenvlevelmodamount", color, -64, 63);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addTransition(Color color)
        {
        Category category = new Category(this, "Transition", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = TRANSITION_LOOP_TYPES;
        comp = new Chooser("Loop Type", this, "transitionlooptype", params);
        vbox.add(comp);
        
        hbox.add(vbox);

        comp = new LabelledDial("Number", this, "transitionnumber", color, 0, 63);
        hbox.add(comp);

        // default is 24, representing Middle C
        comp = new LabelledDial("Pitch Base", this, "transitionbase", color, 0, 60)
            {
            public String map(int val)
                {
                return NOTES[val % 12] + ((val / 12) - 2);
                }
            };
        hbox.add(comp);
        
        comp = new LabelledDial("Start Sample", this, "transitionstartsample", color, 0, 32767);
        hbox.add(comp);

        comp = new LabelledDial("End Sample", this, "transitionendsample", color, 0, 32767);
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }



    public JComponent addLFO(final int lfo, Color color)
        {
        Category category = new Category(this, "LFO " + lfo, color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = LFO_SHAPES;
        comp = new Chooser("Shape", this, "lfo" + lfo + "shape", params);
        vbox.add(comp);

        final LabelledDial rate = new LabelledDial("Rate", this, "lfo" + lfo + "rate", color, 1, 127)
            {
            public String map(int val)
                {
                int range = model.get("lfo" + lfo + "range");
                return "<html><center>" + val + "<br><font size=-2>" + 
                    (range == 0 ? LFO_RATES_FAST[val - 1] :
                    (range == 1 ? LFO_RATES[val - 1] : LFO_RATES_SLOW[val - 1] )) +  
                    "</font></center></html>";
                }
            };

        params = LFO_RANGE_TYPES;
        comp = new Chooser("Range", this, "lfo" + lfo + "range", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                rate.repaint();
                }
            };
        vbox.add(comp);
        hbox.add(vbox);

        hbox.add(rate);

        comp = new LabelledDial("Symmetry", this, "lfo" + lfo + "symmetry", color, -64, 63);
        hbox.add(comp);

        comp = new LabelledDial("Humanize", this, "lfo" + lfo + "humanize", color, 0, 7);
        hbox.add(comp);

        if (lfo == 1)
            {
            vbox = new VBox();
            comp = new CheckBox("Sync", this, "lfo" + lfo + "sync");
            ((CheckBox)comp).addToWidth(1);
            vbox.add(comp);
            hbox.add(vbox);

            comp = new LabelledDial("EG Delay", this, "lfo" + lfo + "egdelay", color, 0, 127);
            hbox.add(comp);

            comp = new LabelledDial("EG Attack", this, "lfo" + lfo + "egattack", color, 0, 127);
            hbox.add(comp);

            comp = new LabelledDial("EG Decay", this, "lfo" + lfo + "egdecay", color, 0, 127)
                {
                public String map(int val)
                    {
                    if (val == 0) return "Off";
                    return "" + val;
                    }
                };
            hbox.add(comp);
                
            comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
                new String[] { null, "lfo" + lfo +  "egdelay", "lfo" + lfo +  "egattack", "lfo" + lfo +  "egdecay"},
                new String[] { null, null, null, null },
                new double[] { 0, (1.0 / 3.0)/127.0, (1.0 / 3.0) / 127.0,  (1.0 / 3.0)/127.0},
                new double[] { 0, 0.0, 1.0, 0.0 })
                {
                public void postProcess(double[] xVals, double[] yVals)
                    {
                    if (xVals[3] == 0) // no decay
                        {
                        xVals[3] = 1 - (xVals[2] + xVals[1]);
                        yVals[3] = 1;
                        }
                    }            
                };
            hbox.addLast(comp);
            }
        else            // LFO 2
            {
            comp = new LabelledDial("Phase Shift", this, "lfo" + lfo + "phaseshift", color, 0, 90)
                {
                public String map(int val)
                    {
                    return "" + (val * 2);
                    }
                };
            hbox.add(comp);
            }

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addLFO1Mod(Color color)
        {
        Category category = new Category(this, "LFO 1 Modulation", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = MOD_SOURCES;
        comp = new Chooser("Rate Mod Source", this, "lfo1ratemodsource", params);
        vbox.add(comp);

        params = MOD_SOURCES;
        comp = new Chooser("Level Mod Source", this, "lfo1levelmodsource", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Rate Mod", this, "lfo1ratemodamount", color, -64, 63);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addGlobalLFO(Color color)
        {
        Category category = new Category(this, "Global LFO", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = LFO_SHAPES;
        comp = new Chooser("Shape", this, "globallfoshape", params);
        vbox.add(comp);

        comp = new CheckBox("Enable", this, "globallfoenable");
        vbox.add(comp);
        hbox.add(vbox);
        
        vbox = new VBox();
        params = LFO_MIDI_SYNC_VALUES;
        comp = new Chooser("MIDI Sync Rate", this, "globallfomidisyncbarvalue", params);
        vbox.add(comp);

        comp = new CheckBox("MIDI Sync On", this, "globallfomidisynced");
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Rate", this, "globallforate", color, 1, 127)
            {
            public String map(int val)
                {
                return "<html><center>" + val + "<br><font size=-2>" + LFO_RATES[val - 1] + "</font></center></html>";
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Symmetry", this, "globallfosymmetry", color, -64, 63);
        hbox.add(comp);

        comp = new LabelledDial("Humanize", this, "globallfohumanize", color, 0, 7);
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

        params = ARP_CLOCKS;
        comp = new Chooser("Clock", this, "arpclock", params);
        vbox.add(comp);
        hbox.add(vbox);
        
        vbox = new VBox();
        params = ARP_DIRECTIONS;
        comp = new Chooser("Direction", this, "arpplaydir", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Tempo", this, "arptempo", color, 30, 300);
        hbox.add(comp);

        comp = new LabelledDial("Pattern", this, "arppatternidx", color, 0, 15);
        ((LabelledDial)comp).addAdditionalLabel("Number");
        hbox.add(comp);

        comp = new LabelledDial("Octaves", this, "arpoctaverange", color, 0, 9);
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
            
    public JFrame sprout()     
        {
        JFrame frame = super.sprout();
        addMMenu();
        return frame;
        }

    int part = 0;
    boolean updateScreen = true;                // Always true, not available
    JCheckBoxMenuItem[] partMenu = new JCheckBoxMenuItem[4];

    public void setPart(int part)
        {
        this.part = part;
        partMenu[part].setSelected(true);
        }

    public void addMMenu()
        {
        JMenu menu = new JMenu("M");
        menubar.add(menu);
        
        // Not Available
        /*
          final JCheckBoxMenuItem updateScreenMenu = new JCheckBoxMenuItem("Update Screen When Changing Parameters");
          updateScreenMenu.addActionListener(new ActionListener()
          {
          public void actionPerformed(ActionEvent e)
          {
          updateScreen = updateScreenMenu.isSelected();
          }
          });
          menu.add(updateScreenMenu);                
          menu.addSeparator();
        */
        
        ButtonGroup buttonGroup = new ButtonGroup();
        for(int i = 0; i < 4; i++)
            {
            final int _i = i;
            partMenu[i] = new JCheckBoxMenuItem("Changing Parameters Updates Part " + (i + 1));
            if (i == 0) partMenu[i].setSelected(true);
            partMenu[i].addActionListener(new ActionListener()
                {
                public void actionPerformed(ActionEvent e)
                    {
                    part = _i;
                    }
                });
            buttonGroup.add(partMenu[i]);
            menu.add(partMenu[i]);
            }
        partMenu[part].setSelected(true);
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
            if (bank >= 16)
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
        
        int number = (model.get("number") + 1);
        int bank = ((model.get("bank")));
        return BANKS[bank] + "/" + ((number > 99 ? "" : (number > 9 ? "0" : "00")) + number);
        }
        


    public Object[] emitAll(String key)
        {        
        simplePause(10);
        if (key.equals("--") || key.equals("bank") || key.equals("number") || key.equals("name"))
            {
            return new Object[0];           // do nothing
            }
        else
            {
            int _part = part;
            int param = ((Integer)parametersToIndex.get(key)).intValue() + 8192;
            int val = model.get(key, 0) + 8192;

            if (key.equals("transitionstartsample") ||
                key.equals("transitionendsample"))
                {
                param = ((Integer)parametersToIndex.get(key)).intValue();
                int topBit = (param >>> 14) & 0x01;     // extract top bit
                param = param & 16383;  // remove top bit
                _part = _part | (topBit << 6);
                }
        
            byte[] data = new byte[12];
            data[0] = (byte)0xF0;
            data[1] = (byte)0x3e;                                       // Waldorf
            data[2] = (byte)0x30;                                       // M
            data[3] = (byte)0x00;
            data[4] = (byte)0x71;                   // Send Parameter to patch Edit Buffer
            data[5] = (byte)_part;            
            data[6] = (byte)(param & 127);
            data[7] = (byte)((param >>> 7) & 127);
            data[8] = (byte)(val & 127);
            data[9] = (byte)((val >>> 7) & 127);
            data[10] = (byte)(updateScreen ? 1 : 0);
            data[11] = (byte)0xF7;
            return new Object[] { data };
            }
        }
    
    public void handleSynthCCOrNRPN(Midi.CCData data)
        {
        if (false)
            //if (data.type == Midi.CCDATA_TYPE_RAW_CC)
            {
            int cc = data.number;
            Integer i = (Integer)(ccToIndex.get(cc));
            if (i != null && i.intValue() >= 0)
                {
                String key = parameters[i];
                if (key.equals("--")) return;
                int val = data.value;

                // Docs are wrong about these, they go 0...4
                if (cc == 33 ||         // Osc 1 Octave
                    cc == 38)       // Osc 2 Octave
                    {
                    val -= 2;               // center on 0
                    }
                // Docs are wrong about these, they go 0...12
                else if (cc == 34 ||    // Osc 1 Semitone
                    cc == 39)       // Osc 2 Semitone
                    {
                    val -= 12;              // center on 0
                    }
                // All centered on 64 according to p. 84 of manual, need to re-center on 0
                else if (cc == 35 ||                                                    // Detune
                    cc == 40 ||                                                         // Detune
                    cc == 51 || cc == 52 || cc == 53 ||             // VCF Keytrack, VCF EG Amount, VCF EG Velocity
                    cc == 58 || cc == 59 ||                         // VCA EG Amount, VCA EG Velocity
                    cc == 73 || cc == 79)                           // Wave EG to Wave Osc 1, Wave EG to Wave Osc 2
                    {
                    val -= 64;
                    }
                model.set(key, val);
                }
            }
        }
        
    public static final int PAUSE_AFTER_CHANGE_MODE = 25;

    public void changePatch(Model tempModel)
        {
        int bank = tempModel.get("bank", 0);
        int number = tempModel.get("number", 0);
        
        // We have to set the patch ourselves
        model.set("bank", bank);
        model.set("number", number);
        
    	updateMode(); 

        // It's not clear if this will work
        tryToSendMIDI(buildCC(getChannelOut(), 32, bank));
        tryToSendMIDI(buildPC(getChannelOut(), number));
        }

    public String getPatchName(Model model) { return model.get("name", "Untitled"); }

    public byte[] requestCurrentDump()
        {
        byte[] data = new byte[9];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x3e;                   // Waldorf
        data[2] = (byte)0x30;                   // M
        data[3] = (byte)0x00;
        data[4] = (byte)0x74;           // Request Patch
        data[5] = (byte)0x00;
        data[6] = (byte)0x00;                   // Request Current Patch 
        data[7] = (byte)0x00;                   // Request Current Patch
        data[8] = (byte)0xF7;
        return data;
        }

    public byte[] requestDump(Model tempModel)
        {
        if (tempModel == null)
            tempModel = getModel();

        int NN = tempModel.get("number", 0);
        int BB = tempModel.get("bank", 0) + 1;          // banks go 1...16, not 0..15.  0 is current patch
        
        // The M's patch dumps have slots to indicate the patch and number but unfortunately they
        // do not populate these slots.  Maybe we can get that to change.  In the meantime, we
        // set the bank and number here.
        
        model.set("bank", tempModel.get("bank"));
        model.set("number", tempModel.get("number"));
        
        byte[] data = new byte[9];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x3e;                   // Waldorf
        data[2] = (byte)0x30;                   // M
        data[3] = (byte)0x00;
        data[4] = (byte)0x74;           // Request Patch
        data[5] = (byte)0x00;
        data[6] = (byte)BB;
        data[7] = (byte)NN;
        data[8] = (byte)0xF7;
        return data;
        }

	public void updateMode()
		{
        // set mode to SINGLE MODE
        tryToSendSysex(new byte[] { (byte)0xF0, 0x3E, 0x30, 0x00, 0x64, 0x00, 0x00, 0x00, (byte)0xF7 });
        simplePause(PAUSE_AFTER_CHANGE_MODE);
		}
		
    public void windowBecameFront() 
    	{ 
    	updateMode(); 
    	}

    public void startingBatchDownload(Model firstPatch, Model finalPatch) 
        { 
    	updateMode(); 
        }

    public int getPauseAfterWritePatch() { return 2600; }

    // Change Patch can get stomped if we do a request immediately afterwards
    // public int getPauseAfterChangePatch() { return 200; }
    
    // public int getPauseAfterSendAllParameters() { return 1000; }
 
    public Object[] emitAll(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        if (tempModel == null)
            tempModel = getModel();
                
        byte BB = (byte) (tempModel.get("bank") + 1);           // yep, banks start at 1
        byte NN = (byte) tempModel.get("number");
        byte DO_NOT_SAVE = 0;
        
        if (toWorkingMemory) { BB = 0; NN = 0; DO_NOT_SAVE = 1;}
                
        // Other options include:
        // BB = 0 NN = 0 DO_NOT_SAVE = 1   *WRITE* to current patch, then reload from flash

        byte[] data = new byte[512];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x3e;
        data[2] = (byte)0x30;
        data[3] = (byte)0x00;                                   // maybe later getID();
        data[4] = (byte)0x72;                   // Dump Single Sound
        data[5] = (byte)0x01;                                   // This appears to be a version marker, and should be 0x01 at present
        
        // Handle Name
        String str = model.get("name", "") + "                       ";
        for(int i = 0; i < 23; i++)
            {
            data[i + 6] = (byte)((str.charAt(i)) & 127);
            }
        data[29] = 0x00;        // Extra padded Name byte
        data[30] = 0x00;        // Extra padded Name byte
        data[31] = 0x00;        // Extra padded Name byte
        
        // Handle Bank and Number, which are after the name oddly
        data[32] = (byte)BB;
        data[33] = (byte)NN;
        data[34] = (byte)DO_NOT_SAVE;
        data[35] = 0x00;

        int pos = 36;
        for(int i = 0; i < parameters.length; i++)
            {
            if (!parameters[i].equals("--"))
                {
                int val = 0;
                if (i == 233 || i == 234)
                    {
                    val = model.get(parameters[i]) & 16383;
                    }
                else
                    {
                    val = model.get(parameters[i]) + 8192;
                    }
                int lsb = (byte)(val & 0x7F);
                int msb = (byte)((val >>> 7) & 0x7F);
                data[pos] = (byte)lsb;
                data[pos + 1] = (byte)msb;
                }
            pos += 2;
            }

        // Handle special parameters
        int smoothscanwt = (model.get("smoothscanwt") & 0x01);
        int transitionstartsample = ((model.get("transitionstartsample") >>> 14) & 0x01);
        int transitionendsample = ((model.get("transitionendsample") >>> 14) & 0x01);
        data[35] = (byte)(smoothscanwt | (transitionstartsample << 1) | (transitionendsample << 2));
                
        data[data.length - 2] = 0x7F;
        data[data.length - 1] = (byte)0xF7;
        return new Object[] { data };
        }
        

    public int parse(byte[] data, boolean fromFile)
        {
        // Handle Name
        char[] name = new char[23];
        for(int i = 0; i < 23; i++)
            {
            name[i] = (char)(data[i + 6] & 127);
            }
        model.set("name", new String(StringUtility.rightTrim(new String(name))));

        /*
        // Handle Bank and Number, which are after the name oddly
        if (data[32] == 0)               // Edit Buffer
        {
        // Might as well set the patch to 1/00
        model.set("bank", 0);
        model.set("number", 0);
        }
        else
        {
        model.set("bank", data[32] - 1);
        model.set("number", data[33]);
        }
        */

        // Remaining parameters
        int pos = 36;
        for(int i = 0; i < parameters.length; i++)
            {
            if (!parameters[i].equals("--"))
                {
                int lsb = data[pos];
                int msb = data[pos + 1];
                if (i == 233 || i == 234)
                    {
                    model.set(parameters[i], (msb << 7) | lsb);
                    }
                else
                    {
                    model.set(parameters[i], ((msb << 7) | lsb) - 8192);
                    }
                }
            pos += 2;
            }
            
        // Handle special parameters
        model.set("smoothscanwt", data[35] & 0x01);
        model.set("transitionstartsample", 
            model.get("transitionstartsample") | (((data[35] >>> 1) & 0x01) << 14));
        model.set("transitionendsample", 
            model.get("transitionendsample") | (((data[35] >>> 2) & 0x01) << 14));
        
        revise();
        return PARSE_SUCCEEDED;     
        }


    public static String getSynthName() { return "Waldorf M"; }
    

    public static HashMap parametersToIndex = null;
    public static final String[] parameters = new String[] 
    {
    "osc1octave",
    "osc1semitone",
    "osc1detune",
    "osc1bendrange",
    "osc1pitchmode",
    "osc1fixedpitch",
    "osc1mod1source",
    "osc1mod1control",
    "osc1mod1amount",
    "osc1mod2source",
    "osc1mod2amount",
    "osc1mod2quantize",
    "digivcfcutoff",
    "digivcfresonance",
    "osc2octave",
    "osc2semitone",
    "osc2detune",
    "osc2bendrange",
    "osc2pitchmode",
    "osc2fixedpitch",
    "osc2mod1source",
    "osc2mod1control",
    "osc2mod1amount",
    "osc2mod2source",
    "osc2mod2amount",
    "osc2mod2quantize",
    "oscasicbug",
    "digivcftype",
    "osc2synctoosc1",
    "osc2linkosc1mods",
    "wave1wavetable",
    "wave1startwave",
    "wave1startphase",
    "wave1envamount",
    "wave1envvelocity",
    "wave1keytrack",
    "wave1mod1source",
    "wave1mod1control",
    "wave1mod1amount",
    "wave1mod2source",
    "wave1mod2amount",
    "wave1wavetravelmode",
    "digivcfmod2source",
    "wave2wavetable",
    "wave2startwave",
    "wave2startphase",
    "wave2envamount",
    "wave2envvelocity",
    "wave2keytrack",
    "wave2mod1source",
    "wave2mod1control",
    "wave2mod1amount",
    "wave2mod2source",
    "wave2mod2amount",
    "wave2wavetravelmode",
    "digivcfmod2amount",
    "wave2linkwave1mods",
    "mixosc1level",
    "mixosc2level",
    "mixringmodlevel",
    "mixnoiselevel",
    "mixosc1modsource",
    "mixosc1modamount",
    "mixosc2modsource",
    "mixosc2modamount",
    "mixringmodmodsource",
    "mixringmodmodamount",
    "mixnoisemodsource",
    "mixnoisemodamount",
    "vcfcutoff",
    "digivcfenabled",
    "vcfresonance",
    "vcfenvamount",
    "vcfenvvelocity",
    "vcfkeytrack",
    "vcfmod1source",
    "vcfmod1control",
    "vcfmod1amount",
    "vcfmod2source",
    "vcfmod2amount",
    "vcfresmodsource",
    "vcfresmodamount",
    "instrumentvolume",
    "vcaenvamount",
    "vcaenvvelocity",
    "vcakeytrack",
    "vcamod1source",
    "vcamod1control",
    "vcamod1amount",
    "vcamod2source",
    "vcamod2amount",
    "vcapan",
    "vcapanmodsource",
    "vcapanmodamount",
    "env1attack",                                   // vca env
    "env1decay",
    "env1sustain",
    "env1release",
    "env1attackmodsource",
    "env1attackmodamount",
    "env1decaymodsource",
    "env1decaymodamount",
    "env1sustainmodsource",
    "env1sustainmodamount",
    "env1releasemodsource",
    "env1releasemodamount",
    "timelevelmodmode",
    "envtimeresolution",
    "env2delay",                                    // filter env
    "env2attack",
    "env2decay",
    "env2sustain",
    "env2release",
    "env2delaymodsource",
    "env2delaymodamount",
    "env2attackmodsource",
    "env2attackmodamount",
    "env2decaymodsource",
    "env2decaymodamount",
    "env2sustainmodsource",
    "env2sustainmodamount",
    "env2releasemodsource",
    "env2releasemodamount",
    "freeenvloop",
    "freeenvloopatrelease",
    "wenvp1time",
    "wenvp1level",
    "wenvp2time",
    "wenvp2level",
    "wenvp3time",
    "wenvp3level",
    "wenvp4time",
    "wenvp4level",
    "wenvp5time",
    "wenvp5level",
    "wenvp6time",
    "wenvp6level",
    "wenvp7time",
    "wenvp7level",
    "wenvp8time",
    "wenvp8level",
    "wenvtimemodsource",
    "wenvtimemodamount",
    "wenvlevelmodsource",
    "wenvlevelmodamount",
    "wenvkeyoffpoint",
    "wenvloopmode",
    "wenvloopbegin",
    "wenvloopend",
    "freeenvp1time",
    "freeenvp1level",
    "freeenvp2time",
    "freeenvp2level",
    "freeenvp3time",
    "freeenvsustainlevel",
    "freeenvreleasetime",
    "freeenvreleaselevel",
    "freeenvtimemodsource",
    "freeenvtimemodamount",
    "freeenvlevelmodsource",
    "freeenvlevelmodamount",
    "lfo1rate",
    "lfo1shape",
    "lfo1symmetry",
    "lfo1humanize",
    "lfo1ratemodsource",
    "lfo1ratemodamount",
    "lfo1levelmodsource",
    "lfo1sync",
    "lfo1egdelay",
    "lfo1egattack",
    "lfo1egdecay",
    "lfo1range",
    "lfo2rate",
    "lfo2shape",
    "lfo2symmetry",
    "lfo2humanize",
    "lfo2phaseshift",
    "lfo2range",
    "glide",
    "gliderate",
    "glidemode",
    "globallfohumanize",
    "globallforate",
    "arpmode",
    "arpclock",
    "arptempo",
    "arppatternidx",
    "arpplaydir",
    "arpoctaverange",
    "--",           // unused arplength
    "--",           // unused arpsortorder
    "--",           // unused arptimingfactor
    "--",           // unused arpvelomode
    "--",           // unused arppatternlength
    "--",           // unused arppatternreset
    "--",           // unused arpmaxnotes
    "globallfoenable",
    "globallfomidisynced",
    "globallfomidisyncbarvalue",
    "mmwmode",
    "playmode",
    "voicestealmode",
    "globallfoshape",
    "--",           // reserved 0
    "--",           // reserved 1
    "--",           // reserved 2
    "--",           // reserved 3
    "--",           // reserved 4
    "--",           // reserved 5
    "--",           // reserved 6
    "--",           // reserved 7
    "--",           // reserved 8
    "--",           // reserved 9
    "--",           // reserved 10
    "--",           // reserved 11
    "--",           // reserved 12
    "--",           // reserved 13
    "--",           // reserved 14
    "--",           // reserved 15
    "digivcfenvamount",
    "digivcfenvvelocity",
    "digivcfkeytrack",
    "digivcfmod1source",
    "digivcfmod1control",
    "digivcfmod1amount",
    "digivcfresmodsource",
    "digivcfresmodamount",
    "digivcfextraparam",
    "transitionenable",
    "transitionnumber",
    "transitionbase",
    "transitionlooptype",
    "transitionstartsample",
    "transitionendsample",
    "globallfosymmetry",
    "oscsderez",
    "smoothscanwt",
    };


    // See p. 84 of manual for this
    public static HashMap ccToIndex = null;
    public static final int[] cc = new int[] 
    {
    33,     // osc1octave
    34,     // osc1semitone
    35,     // osc1detune
    -1,     // osc1bendrange
    -1,     // osc1pitchmode
    -1,     // osc1fixedpitch
    -1,     // osc1mod1source
    -1,     // osc1mod1control
    -1,     // osc1mod1amount
    -1,     // osc1mod2source
    -1,     // osc1mod2amount
    -1,     // osc1mod2quantize
    -1,     // digivcfcutoff
    -1,     // digivcfresonance
    38,     // osc2octave
    39,     // osc2semitone
    40,     // osc2detune
    -1,     // osc2bendrange
    -1,     // osc2pitchmode
    -1,     // osc2fixedpitch
    -1,     // osc2mod1source
    -1,     // osc2mod1control
    -1,     // osc2mod1amount
    -1,     // osc2mod2source
    -1,     // osc2mod2amount
    -1,     // osc2mod2quantize
    -1,     // oscasicbug
    -1,     // digivcftype
    41,     // osc2synctoosc1
    -1,     // osc2linkosc1mods
    70,     // wave1wavetable
    71,     // wave1startwave
    -1,     // wave1startphase
    73,     // wave1envamount
    -1,     // wave1envvelocity
    -1,     // wave1keytrack
    -1,     // wave1mod1source
    -1,     // wave1mod1control
    -1,     // wave1mod1amount
    -1,     // wave1mod2source
    -1,     // wave1mod2amount
    -1,     // wave1wavetravelmode
    -1,     // digivcfmod2source
    63,     // wave2wavetable
    78,     // wave2startwave
    -1,     // wave2startphase
    79,     // wave2envamount
    -1,     // wave2envvelocity
    -1,     // wave2keytrack
    -1,     // wave2mod1source
    -1,     // wave2mod1control
    -1,     // wave2mod1amount
    -1,     // wave2mod2source
    -1,     // wave2mod2amount
    -1,     // wave2wavetravelmode
    -1,     // digivcfmod2amount
    -1,     // wave2linkwave1mods
    45,     // mixosc1level
    46,     // mixosc2level
    47,     // mixringmodlevel
    48,     // mixnoiselevel
    -1,     // mixosc1modsource
    -1,     // mixosc1modamount
    -1,     // mixosc2modsource
    -1,     // mixosc2modamount
    -1,     // mixringmodmodsource
    -1,     // mixringmodmodamount
    -1,     // mixnoisemodsource
    -1,     // mixnoisemodamount
    50,     // vcfcutoff
    -1,     // digivcfenabled
    56,     // vcfresonance
    52,     // vcfenvamount
    53,     // vcfenvvelocity
    51,     // vcfkeytrack
    -1,     // vcfmod1source
    -1,     // vcfmod1control
    -1,     // vcfmod1amount
    -1,     // vcfmod2source
    -1,     // vcfmod2amount
    -1,     // vcfresmodsource
    -1,     // vcfresmodamount
    57,     // instrumentvolume
    59,     // vcaenvamount                             // Manual says 58 but it is wrong
    58,     // vcaenvvelocity                   // Manual says 59 but it is wrong
    -1,     // vcakeytrack
    -1,     // vcamod1source
    -1,     // vcamod1control
    -1,     // vcamod1amount
    -1,     // vcamod2source
    -1,     // vcamod2amount
    -1,     // vcapan
    -1,     // vcapanmodsource
    -1,     // vcapanmodamount
    14,     // env1attack                                   
    15,     // env1decay
    16,     // env1sustain
    17,     // env1release
    -1,     // env1attackmodsource
    -1,     // env1attackmodamount
    -1,     // env1decaymodsource
    -1,     // env1decaymodamount
    -1,     // env1sustainmodsource
    -1,     // env1sustainmodamount
    -1,     // env1releasemodsource
    -1,     // env1releasemodamount
    -1,     // timelevelmodmode
    -1,     // envtimeresolution
    -1,     // env2delay                                    
    18,     // env2attack
    19,     // env2decay
    20,     // env2sustain
    21,     // env2release
    -1,     // env2delaymodsource
    -1,     // env2delaymodamount
    -1,     // env2attackmodsource
    -1,     // env2attackmodamount
    -1,     // env2decaymodsource
    -1,     // env2decaymodamount
    -1,     // env2sustainmodsource
    -1,     // env2sustainmodamount
    -1,     // env2releasemodsource
    -1,     // env2releasemodamount
    -1,     // freeenvloop
    -1,     // freeenvloopatrelease
    -1,     // wenvp1time
    -1,     // wenvp1level
    -1,     // wenvp2time
    -1,     // wenvp2level
    -1,     // wenvp3time
    -1,     // wenvp3level
    -1,     // wenvp4time
    -1,     // wenvp4level
    -1,     // wenvp5time
    -1,     // wenvp5level
    -1,     // wenvp6time
    -1,     // wenvp6level
    -1,     // wenvp7time
    -1,     // wenvp7level
    -1,     // wenvp8time
    -1,     // wenvp8level
    -1,     // wenvtimemodsource
    -1,     // wenvtimemodamount
    -1,     // wenvlevelmodsource
    -1,     // wenvlevelmodamount
    -1,     // wenvkeyoffpoint
    -1,     // wenvloopmode
    -1,     // wenvloopbegin
    -1,     // wenvloopend
    85,     // freeenvp1time
    86,     // freeenvp1level
    87,     // freeenvp2time
    88,     // freeenvp2level
    89,     // freeenvp3time
    90,     // freeenvsustainlevel
    91,     // freeenvreleasetime
    92,     // freeenvreleaselevel
    -1,     // freeenvtimemodsource
    -1,     // freeenvtimemodamount
    -1,     // freeenvlevelmodsource
    -1,     // freeenvlevelmodamount
    24,     // lfo1rate
    25,     // lfo1shape
    -1,     // lfo1symmetry
    -1,     // lfo1humanize
    -1,     // lfo1ratemodsource
    -1,     // lfo1ratemodamount
    -1,     // lfo1levelmodsource
    -1,     // lfo1sync
    -1,     // lfo1egdelay
    -1,     // lfo1egattack
    -1,     // lfo1egdecay
    -1,     // lfo1range
    26,     // lfo2rate
    28,     // lfo2shape
    -1,     // lfo2symmetry
    -1,     // lfo2humanize
    -1,     // lfo2phaseshift
    -1,     // lfo2range
    65,     // glide
    5,      // gliderate
    -1,     // glidemode
    -1,     // globallfohumanize
    -1,     // globallforate
    -1,     // arpmode
    -1,     // arpclock
    -1,     // arptempo
    -1,     // arppatternidx
    -1,     // arpplaydir
    -1,     // arpoctaverange
    -1,     // --           
    -1,     // --           
    -1,     // --           
    -1,     // --           
    -1,     // --           
    -1,     // --           
    -1,     // --           
    -1,     // globallfoenable
    -1,     // globallfomidisynced
    -1,     // globallfomidisyncbarvalue
    -1,     // mmwmode
    -1,     // playmode
    -1,     // voicestealmode
    -1,     // globallfoshape
    -1,     // --           
    -1,     // --           
    -1,     // --           
    -1,     // --           
    -1,     // --           
    -1,     // --           
    -1,     // --           
    -1,     // --           
    -1,     // --           
    -1,     // --           
    -1,     // --           
    -1,     // --           
    -1,     // --           
    -1,     // --           
    -1,     // --           
    -1,     // --           
    -1,     // digivcfenvamount
    -1,     // digivcfenvvelocity
    -1,     // digivcfkeytrack
    -1,     // digivcfmod1source
    -1,     // digivcfmod1control
    -1,     // digivcfmod1amount
    -1,     // digivcfresmodsource
    -1,     // digivcfresmodamount
    -1,     // digivcfextraparam
    -1,     // transitionenable
    -1,     // transitionnumber
    -1,     // transitionbase
    -1,     // transitionlooptype
    -1,     // transitionstartsample
    -1,     // transitionendsample
    -1,     // globallfosymmetry
    -1,     // oscsderez
    -1,     // smoothscanwt
    };

    
    
    public String[] getPatchNumberNames()  
        { 
        return buildIntegerNames(128, 0);
        }

    public boolean[] getWriteableBanks() 
        { 
        return buildBankBooleans(16, 0, 0);
        }

    public String[] getBankNames() { return BANKS; }

    public boolean getSupportsPatchWrites() { return true; }

    public int getPatchNameLength() { return MAXIMUM_NAME_LENGTH; }

    public int getBatchDownloadWaitTime()
        {
        return 650;
        }

    public boolean librarianTested() { return true; }
    }
