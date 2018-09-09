/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.waldorfmicrowavext;

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
   A patch editor for the Waldorf Microwave XT.  Does not deal with Multi mode, global parameters,
   modifying wavetables, or uploading samples.  Only Single mode patches.
        
   @author Sean Luke
*/

public class WaldorfMicrowaveXT extends Synth
    {
    /// Various collections of parameter names for pop-up menus
        
    static final String[] BANKS = new String[] { "A", "B" };
    static final String[] FM_SOURCES = new String[] { "Off", "Osc1", "Osc2", "Osc3", "Noise", "LFO 1", "LFO 2", "LFO 3", "Filter Env", "Amp Env", "Env 3", "Env 4" };
    static final String[] MOD_SOURCES = new String[/*32*/] {"Off", "LFO 1", "LFO1*MW", "LFO1*Press.", "LFO 2", "Filter Env", "Amp Env", "Wave Env", "Free Env", "Key Follow", "Keytrack", "Velocity", "Rel. Velo", "Pressure", "Poly Press", "Pitch Bend", "Mod Wheel", "Sustain", "Foot Ctrl", "Breath Ctrl", "Control W", "Control X", "Control Y", "Control Z", "Ctrl. Delay", "Modifier 1", "Modifier 2", "Modifier 3", "Modifier 4", "MIDI Clock", "Minimum", "Maximum" };
    static final String[] MOD_DESTINATIONS = new String[/*36*/] { "Pitch", "O1 Pitch", "O2 Pitch", "Wave 1 Pos", "Wave 2 Pos", "Mix Wave 1", "Mix Wave 2", "Mix Ringmod", "Mix Noise", "F1 Cutoff", "F1 Reson.", "F2 Cutoff", "Volume", "Panning", "FE Attack", "FE Decay", "FE Sustain", "FE Release", "AE Attack", "AE Decay", "AE Sustain", "AE Release", "WE Times", "WE Levels", "FE Times", "FE Levels", "LFO1 Rate", "LFO1 Level", "LFO2 Rate", "LFO2 Level", "M1 Amount", "M2 Amount", "M3 Amount", "M4 Amount", "FM Amount", "F1 Extra" };
    static final String[] MODIFIER_OPERATORS = new String[/*16*/] { "+", "-", "*", "/", "XOR", "OR", "AND", "S&H", "Ramp", "Switch", "Abs", "Min", "Max", "Lag", "Filter", "Diff"};
    static final String[] FILTER_1_TYPES = new String[/*10*/] {  "24dB LP", "12dB LP", "24dB BP", "12dB BP", "12dB HP", "Sin -> 12dB LP", "12dB LP -> Wave", "Dual 12dB LP/BP", "12dB FM LP", "S&H -> 12dB LP", "24dB Notch", "12dB Notch", "Band Stop" };   
    static final String[] PLAY_PARAMETERS = new String[/*83*/] { "Osc 1 Octave", "Osc 1 Semitone", "Osc 1 Detune", "Osc 1 Pitchbend", "Osc 1 Keytrack", "Osc 2 Octave", "Osc 2 Semitone", "Osc 2 Detune", "Osc 2 Pitchbend", "Osc 2 Keytrack", "Wavetable", "Wave 1 Startwave", "Wave 1 Phase", "Wave 1 Env Amount", "Wave 1 Velocity", "Wave 1 Keytrack", "Wave 2 Startwave", "Wave 2 Phase", "Wave 2 Env Amount", "Wave 2 Velocity", "Wave 2 Keytrack", "Mix Wave 1", "Mix Wave 2", "Mix Ringmod", "Mix Noise", "Aliasing", "Quantize", "Clipping", "Filter 1 Cutoff", "Filter 1 Resonance", "Filter 1 Type", "Filter 1 Keytrack", "Filter 1 Env Emount", "Filter 1 Velocity", "Filter 2 Cutoff", "Filter 2 Type", "Filter 2 Keytrack", "Sound Volume", "Amp Env Velocity", "Amp Keytrack", "Chorus", "Panning", "Pan Keytrack", "Glide on/off", "Glide Type", "Arp On/Off/Hold", "Arp Tempo", "Arp Clock", "Arp Range", "Arp Pattern", "Arp Direction", "Arp Note Order", "Arp Velocity", "Allocation", "Assignment", "Filter Env Attack", "Filter Env Decay", "Filter Env Sustain", "Filter Env Release", "Amp Env Attack", "Amp Env Decay", "Amp Env Sustain", "Amp Env Release", "LFO1 Rate", "LFO1 Shape", "LFO1 Delay", "LFO1 Sync", "LFO1 Symmetry", "LFO1 Humanize", "LFO2 Rate", "LFO2 Shape", "LFO2 Delay", "LFO2 Sync", "LFO2 Symmetry", "LFO2 Humanize", "LFO2 Phase", "Osc 1 FM Amount", "Filter 1 Special", "Glide Time", "Control W", "Control X", "Control Y", "Control Z" };
    static final String[] TRIGGERS = new String[] { "Normal", "Single", "Retrigger" };
    static final String[] LFO_SHAPES = new String[] { "Sin", "Tri", "Sqr", "Saw", "Rand", "S&H" };
    static final String[] LFO_SYNC = new String[] { "Off", "On", "Clock" };
    static final String[] LFO_SPEEDS = new String[] {      "128", "96", "64", "48", "36", "32", "24", "18", 
                                                           "16", "12",  "9", "8", "6", "4", "3", "2", "1", 
                                                           "1/2 .", "1/2 T", "1/2", "1/4 .", "1/4 T", "1/4", "1/8 .", "1/8 T", 
                                                           "1/8", "1/16 .", "1/16 T", "1/16", "1/32 .", "1/32", "1/64" };
    static final String[] LFO_PHASES = new String[] {"Free", "3", "6", "8", "11", "14", "17", "20", "23", "25", "28", "31", "34", "37", "39", "42", "45", "48", 
                                                     "51", "53", "56", "59", "62", "65", "68", "70", "73", "76", "79", "82", "84", "87", "90", "93", "96", "98", 
                                                     "101", "104", "107", "110", "113", "115", "118", "121", "124", "127", "129", "132", "135", "138", "141", 
                                                     "143", "146", "149", "152", "155", "158", "160", "163", "166", "169", "172", "174", "177", "180", "183", 
                                                     "186", "188", "191", "194", "197", "200", "203", "205", "208", "211", "214", "217", "219", "222", "225", 
                                                     "228", "231", "233", "236", "239", "242", "245", "248", "250", "253", "256", "259", "262", "264", "267", 
                                                     "270", "273", "276", "278", "281", "284", "287", "290", "293", "295", "298", "301", "304", "307", "309", 
                                                     "312", "315", "318", "321", "323", "326", "329", "332", "335", "338", "340", "343", "346", "349", "352", "354", "357"};
    static final String[] CLIPPING = new String[] { "Saturate", "Overflow" };
    static final String[] FILTER_2_TYPES = new String[] { "6dB LP", "6dB HP" };
    static final String[] EFFECT_TYPES = new String[] { "Off", "Chorus", "Flanger 1", "Flanger 2", "AutoWahLP", "AutoWahBP", "Overdrive", "Amp. Mod", "Delay [XT/Xtk]", "Pan Delay [XT/Xtk]", "Mod Delay [XT/Xtk]" };
    static final String[] OSCILLATOR_GLIDE_TYPES = new String[] { "Portamento", "Glissando", "Fingered P", "Fingered G" };
    static final String[] OSCILLATOR_GLIDE_MODES = new String[] { "Exponential", "Linear" };
    static final String[] ARPEGGIATOR_ACTIVE = new String[] { "Off", "On", "Hold" };
    static final String[] ARPEGGIATOR_DIRECTION = new String[] { "Up", "Down", "Alternating", "Random" };  // is it Alternating?
    static final String[] ARPEGGIATOR_ORDER = new String[] { "By Note", "By Note Reversed", "As Played", "As Played Reversed" };
    static final String[] ARPEGGIATOR_VELOCITY = new String[] { "Root Note", "Last Note" }; 
    static final String[] ASSIGNMENT = new String[] { "Normal", "Dual", "Unisono" }; 
    static final String[] OSCILLATOR_OCTAVES = new String[] { "128'", "64'", "32'", "16'", "8'", "4'", "2'", "1'", "1/2'" };
    static final String[] AMPLIFIER_TYPES = new String[] { "Direct", "Combo", "Medium", "Stack" };
    static final String[] WAVES = new String[] {           "Resonant", "Resonant2", "MalletSyn", 
                                                           "Sqr-Sweep", "Bellish", "Pul-Sweep", "Saw-Sweep", "MellowSaw", "Feedback", "Add Harm", "Reso 3 HP", 
                                                           "Wind Syn", "HighHarm", "Clipper", "OrganSyn", "SquareSaw", "Format1", "Polated", "Transient", 
                                                           "ElectricP", "Robotic", "StrongHrm", "PercOrgan", "ClipSweep", "ResoHarms", "2 Echoes", "Formant2", 
                                                           "FmntVocal", "MicroSync", "MicroPWM", "Glassy", "SquareHP", "SawSync1", "SawSync2", "SawSync3", 
                                                           "PulSync1", "PulSync2", "PulSync3", "SinSync1", "SinSync2", "SinSync3", "PWM Pulse", "PWM Saw", 
                                                           "Fuzz Wave", "Distorted", "HeavyFuzz", "Fuzz Sync", "K+Strong1", "K+Strong2", "K+Strong3", "1-2-3-4-5", 
                                                           "19/twenty", "Wavetrip1", "Wavetrip2", "Wavetrip3", "Wavetrip4", "MaleVoice", "Low Piano", "ResoSweep", 
                                                           "Xmas Bell", "FM Piano", "Fat Organ", "Vibes", "Chorus 2", "True PWM" };
    // how to modify this?  Search for "notation" in manual
    static final String[] RATE = new String[] { "1/96", "1/48", "1/32", "1/16 T", "1/32 .", "1/16", "1/8T", "1/16 .", "1/8", "1/4 T", "1/8 .", "1/4", "1/2 T", "1/4 .", "1/2", "1/1 T", "1/2 .", "1", "1.5", "2", "2.5", "3", "3.5", "4", "5", "6", "7", "8", "9", "10", "12", "14", "16", "18", "20", "24", "28", "32", "36", "40", "48", "56", "64" };
    static final String[] ARP_CLOCK = new String[] { "1/1", "1/2 .", "1/2 T", "1/2", "1/4 .", "1/4 T", "1/4", "1/8 .", "1/8 T", "1/8", "1/16 .", "1/16 T", "1/16", "1/32 .", "1/32 T", "1/32"};
    
    static final int[][] ARP_PATTERNS = new int[][] {
        { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
        { 1, 0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1 },
        { 1, 0, 1, 0, 1, 0, 0, 1, 1, 0, 1, 0, 1, 0, 0, 1 },
        { 1, 0, 1, 0, 1, 0, 1, 1, 1, 0, 1, 0, 1, 0, 1, 1 },
        { 1, 0, 1, 1, 1, 0, 1, 0, 1, 0, 1, 1, 1, 0, 1, 0 },
        { 1, 0, 1, 0, 1, 1, 0, 1, 1, 0, 1, 0, 1, 1, 0, 1 },
        { 1, 1, 0, 1, 0, 1, 1, 0, 1, 1, 0, 1, 0, 1, 1, 0 },
        { 1, 0, 1, 0, 1, 0, 1, 0, 1, 1, 0, 1, 0, 1, 0, 1 },
        { 1, 0, 1, 0, 1, 0, 1, 1, 0, 1, 0, 1, 1, 0, 1, 0 },
        { 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 0 },
        { 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 1, 0 },
        { 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 0, 1, 0 },
        { 1, 1, 0, 1, 1, 0, 1, 0, 1, 1, 0, 1, 1, 0, 1, 0 },
        { 1, 0, 1, 0, 1, 0, 1, 0, 1, 1, 0, 1, 0, 1, 1, 1 },
        { 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1 },
        { 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0, 1, 1, 0, 1, 0 } };
        
    public WaldorfMicrowaveXT()
        {
        for(int i = 0; i < allParameters.length; i++)
            {
            allParametersToIndex.put(allParameters[i], Integer.valueOf(i));
            }
        
        /// SOUND PANEL
                
        JComponent soundPanel = new SynthPanel(this);
        VBox vbox = new VBox();
        HBox hbox = new HBox(HBox.LEFT_CONSUMES);
        hbox.addLast(addNameGlobal(Style.COLOR_GLOBAL()));
        hbox.add(addWavetable(Style.COLOR_A()));
                
        vbox.add(hbox);
        
        hbox = new HBox();
        VBox vbox2 = new VBox();
        vbox2.add(addOscillator(1, Style.COLOR_A()));
        vbox2.add(addOscillator(2, Style.COLOR_A()));
        vbox2.add(addAmplifier(Style.COLOR_C()));
        hbox.add(vbox2);
        
        vbox2 = new VBox();
        vbox2.add(addWave(1, Style.COLOR_A()));
        vbox2.add(addWave(2, Style.COLOR_A()));
        vbox2.add(addMixer(Style.COLOR_C()));
        hbox.addLast(vbox2);
        
        vbox.add(hbox);
        
        hbox = new HBox();
        hbox.add(addFilter1(Style.COLOR_B()));
        hbox.addLast(addFilter2(Style.COLOR_B()));

        vbox.add(hbox);

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Oscillators and Filters", soundPanel);
                
                
        // ENVELOPE PANEL
                
        JComponent envelopePanel = new SynthPanel(this);
        vbox = new VBox();

        hbox = new HBox();
        hbox.add(addEnvelope(1, Style.COLOR_A()));
        hbox.addLast(addEnvelope(2, Style.COLOR_A()));
        vbox.add(hbox);
        
        vbox.add(addWaveEnvelope(Style.COLOR_A()));
        vbox.add(addFreeEnvelope(Style.COLOR_A()));
        
        hbox = new HBox();
        hbox.add(addEnvelopeDisplay(1,Style.COLOR_B()));
        hbox.add(addEnvelopeDisplay(2,Style.COLOR_B()));
        hbox.add(addWaveEnvelopeDisplay(Style.COLOR_B()));
        hbox.addLast(addFreeEnvelopeDisplay(Style.COLOR_B()));
        vbox.add(hbox);
        
        envelopePanel.add(vbox, BorderLayout.CENTER);
        addTab("Envelopes", envelopePanel);

        
        
        // MODULATION PANEL
                
        JComponent modulationPanel = new SynthPanel(this);
        
        vbox = new VBox();
        vbox.add(addModulation(Style.COLOR_A()));
        vbox.add(addModifiers(Style.COLOR_B()));
                                
        modulationPanel.add(vbox, BorderLayout.CENTER);
        addTab("Modulation", modulationPanel);



        // OTHER PANEL
        JComponent otherPanel = new SynthPanel(this);
        
        vbox = new VBox();

        hbox = new HBox();
        hbox.add(addLFO(1, Style.COLOR_A()));
        hbox.addLast(addLFO(2, Style.COLOR_A()));
        vbox.add(hbox);

        vbox.add(addArpeggiation(Style.COLOR_B()));

        hbox = new HBox();
        hbox.add(addAllocation(Style.COLOR_C()));
        hbox.add(addQuality(Style.COLOR_C()));
        hbox.addLast(addGlide(Style.COLOR_C()));
        vbox.add(hbox);

        vbox.add(addPlayParameters(Style.COLOR_C()));
        vbox.add(addEffect(Style.COLOR_C()));


        
        otherPanel.add(vbox, BorderLayout.CENTER);
        addTab("Other", otherPanel);

        model.set("name", "Init Sound V1.1 ");

        model.set("number", 0);
        model.set("bank", 0);

        // make sure this never can be mutated
        model.set("soundformatversion", 1);  // always
        model.setStatus("soundformatversion", Model.STATUS_IMMUTABLE);
        
        loadDefaults();        
        }
                
    public void windowBecameFront() { updateMode(); }

    public void updateMode()
        {
        boolean send = getSendMIDI();
        setSendMIDI(true);
        byte DEV = (byte)(getID());
        // we'll send a mode dump to change the mode to Single
        tryToSendSysex(new byte[] { (byte)0xF0, 0x3E, 0x0E, DEV, 0x17, 0x00, (byte)0xF7 });
        setSendMIDI(send);
        }
               
    public String getDefaultResourceFileName() { return "WaldorfMicrowaveXT.init"; }
    public String getHTMLResourceFileName() { return "WaldorfMicrowaveXT.html"; }

    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        JComboBox bank = new JComboBox(BANKS);
        bank.setEditable(false);
        bank.setMaximumRowCount(32);
        bank.setSelectedIndex(model.get("bank"));
                
        JTextField number = new JTextField("" + (model.get("number") + 1), 3);

        while(true)
            {
            boolean result = showMultiOption(this, new String[] { "Bank", "Patch Number"}, 
                new JComponent[] { bank, number }, title, "Enter the Bank and Patch Number");
                
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
        Category globalCategory = new Category(this, "Waldorf Microwave II/XT/XTk", color);
        globalCategory.makeUnresettable();

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
                
        VBox vbox = new VBox();
        HBox hbox2 = new HBox();
        comp = new PatchDisplay(this, 4);
        hbox2.add(comp);
        vbox.add(hbox2);
        hbox.add(vbox);
        
        comp = new StringComponent("Patch Name", this, "name", 16, "Name must be up to 16 ASCII characters.")
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
        hbox.add(comp);
                        
        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }

        

    /** Add the Global Oscillator category */
    public JComponent addWavetable(Color color)
        {
        Category category = new Category(this, "Waves", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        String[] params1 = WAVES;
        params = new String[128];
        System.arraycopy(params1, 0, params, 0, 65);
        for(int i = 65; i < 96; i++)
            params[i] = "Reserved " + (i - 65 + 66);
        for(int i = 96; i < 128; i++)
            params[i] = "User " + (i - 96 + 1);
        comp = new Chooser("Wavetable", this, "wavetable", params);

        // only standard wavetables are permitted to be mutated
        model.setValidMax("wavetable", 64);

        vbox.add(comp);

        hbox.add(vbox);
        category.add(hbox, BorderLayout.WEST);
        return category;
        }


    /** Add the Quality category */    
    public JComponent addQuality(Color color)
        {
        Category category = new Category(this, "Quality", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        params = CLIPPING; 
        comp = new Chooser("Clipping", this, "clipping", params);
        vbox.add(comp);

        comp = new CheckBox("Accurate", this, "accuracy");
        vbox.add(comp);

        hbox.add(vbox);
                
        comp = new LabelledDial("Aliasing", this, "aliasing", color, 0, 5)
            {
            public String map(int val)
                {
                if (val == 0) return "Off";
                else return ("" + (val));
                }
            };
        model.setMetricMin( "aliasing", 1);
        hbox.add(comp);

        comp = new LabelledDial("Time", this, "timequant", color, 0, 5)
            {
            public String map(int val)
                {
                if (val == 0) return "Off";
                else return ("" + (val));
                }
            };
        model.setMetricMin( "timequant", 1);
        ((LabelledDial)comp).addAdditionalLabel("Quantization");
        hbox.add(comp);

        category.add(hbox, BorderLayout.WEST);
        return category;
        }


    /** Add the Mixer category */    
    public JComponent addMixer(Color color)
        {
        Category category = new Category(this, "Mixer", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        //        VBox vbox = new VBox();
        
        comp = new LabelledDial("Wave 1", this, "mixwave1", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Wave 2", this, "mixwave2", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Ring Mod", this, "mixringmod", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Noise", this, "mixnoise", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("External", this, "mixexternal", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel(" [XT/XTk] ");
        hbox.add(comp);

        category.add(hbox, BorderLayout.WEST);
        return category;
        }

    public String pitchKeytrack(int val)
        {
        if (val < 38)
            {
            return "" + ((val * 5) - 100);
            }
        else if (val > 58)
            {
            return "" + ((val - 59) * 5 + 115);
            }
        else // 38 .. 58 
            {
            return "" + ((val - 48) + 100);
            }
        }

    /** Add an Oscillator category */
    public JComponent addOscillator(int osc, Color color)
        {
        Category category = new Category(this, "Oscillator " + osc, color);
        category.makePasteable("osc" + osc);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        
        comp = new LabelledDial("Octave", this, "osc" + osc + "octave", color, 0, 8, 4)
            {
            public boolean isSymmetric()
                {
                return true;
                }
            };
        hbox.add(comp);
                
        comp = new LabelledDial("Semitone", this, "osc" + osc + "semitone", color, 52, 76, 64);
        hbox.add(comp);

        comp = new LabelledDial("Detune", this, "osc" + osc + "detune", color, 0, 127, 64);
        hbox.add(comp);

        // Keytrack goes by 5 from -100 ... 90, then by 1 to 110, then by 5 to 200
        comp = new LabelledDial("Keytrack", this, "osc" + osc + "keytrack", color, 0, 76)
            {
            public String map(int val)
                {
                return pitchKeytrack(val);
                }
            public double getStartAngle()
                {
                return 260;
                }
            public int getDefaultValue() { return 48; } // +100
            };
        hbox.add(comp);
        
        comp = new LabelledDial("Pitch Bend", this, "osc" + osc + "bendrange", color, 0, 122)
            {
            public String map(int val)
                {
                if (val == 121) return "Harm";
                else if (val == 122) return "Global";
                else return ("" + val);
                }
            };
        model.setMetricMax("osc" + osc + "bendrange", 120);
        ((LabelledDial)comp).addAdditionalLabel("Range");
        hbox.add(comp);


        if (osc==1)
            {
            comp = new LabelledDial("FM", this, "osc" + osc + "fmamount", color, 0, 127);
            ((LabelledDial)comp).addAdditionalLabel("Amount");
            hbox.add(comp);
            }
                        
        if (osc==2)
            {
            VBox vbox = new VBox();
            // The Sync checkbox on OS X seems to collapse to slightly less than it's supposed to
            // even though preferred size is also minimum size.  Not sure what's causing this bug;
            // but the Link box is extended with a single space below in order to stretch things out
            // so the Sync box doesn't get packed as tight.
            comp = new CheckBox("Sync", this, "osc" + osc + "sync");
            vbox.add(comp);    
            comp = new CheckBox("Link ", this, "osc" + osc + "link");
            vbox.add(comp);    
            hbox.add(vbox);
            }


        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
        

    // Note: this keytrack works for values which keytrack -200...197,
    // but the osc keytracks are different and have to be handled on their own specially
    String keytrack(int val)
        {
        // there are 8 positive (and 8 negative) jumps of 4 rather than 3,
        // every 25.  Yuck :-(
        //9  ->13
        //34 ->38
        //59 ->63
        //84 ->88
        //109->113
        //134->138
        //159->163
        //184->188
                
        val -= 64;
        double jump = 0;
        if (val > 3)
            {
            jump = (double)((val - 3) / 8.0);
            if (jump != (int)jump)  // will fail for NaN of course
                jump = (int)jump + 1;
            }
        else if (val < -3)
            {
            jump = (double)((val + 3) / 8.0);
            if (jump != (int)jump)
                jump = (int)jump - 1;
            }
                        
        return "" + ((val * 3) + (int)jump);
        }


    /** Add an Oscillator category */
    public JComponent addWave(int wave, Color color)
        {
        Category category = new Category(this, "Wave " + wave, color);
        category.makePasteable("wave" + wave);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        comp = new LabelledDial("Start Wave", this, "wave" + wave + "startwave", color, 0, 63)
            {
            public String map(int val)
                {
                if (val < 61)
                    {
                    return "" + val;
                    }
                else if (val == 61)
                    {
                    return "Tri";
                    }
                else if (val == 62)
                    {
                    return "Square";
                    }
                else // if (val == 63)
                    {
                    return "Saw";
                    }
                }
            };
        model.setMetricMax( "wave" + wave + "startwave", 60);
        hbox.add(comp);

        comp = new LabelledDial("Start Phase", this, "wave" + wave + "startphase", color, 0, 127)
            {
            public String map(int val)
                {
                if (val == 0)
                    {
                    return "Free";
                    }
                else
                    {
                    return "" + ((val * 2) + 1);
                    }
                }
            };
        model.setMetricMin( "wave" + wave + "startphase", 1);
        hbox.add(comp);

        comp = new LabelledDial("Envelope", this, "wave" + wave + "envamount", color, 0, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        comp = new LabelledDial("Envelope", this, "wave" + wave + "envvelocity", color, 0, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Velocity");
        hbox.add(comp);

        comp = new LabelledDial("Keytrack", this, "wave" + wave + "keytrack", color, 0, 127)
            {
            public String map(int val)
                {
                return keytrack(val);
                }
            public boolean isSymmetric()
                {
                return true;
                }
            };
            
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        VBox vbox = new VBox();

        comp = new CheckBox("Limit", this, "wave" + wave + "limit");
        vbox.add(comp);
        
        if (wave==2)
            {
            comp = new CheckBox("Link", this, "wave2link");
            vbox.add(comp);    
            }

        hbox.add(vbox);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }



    /** Add a Filter1 category */
    public JComponent addFilter1(Color color)
        {
        Category category = new Category(this, "Filter 1", color);
                
        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        final JComponent[] extras = new JComponent[13];
        
        extras[6] = new LabelledDial("Wave", this, "filter1special", color, 0, 127);
        extras[7] = new LabelledDial("BP Offset", this, "filter1special", color, 0, 127, 64);
        extras[8] = new LabelledDial("Osc2 FM", this, "filter1special", color, 0, 127);
        extras[9] = new LabelledDial("S&H Rate", this, "filter1special", color, 0, 127);
        extras[12] = new LabelledDial("Bandwidth", this, "filter1special", color, 0, 127);
                
        JComponent strut = Strut.makeStrut(extras[7].getPreferredSize().width, extras[7].getPreferredSize().height);
        extras[11] = strut;
        extras[10] = strut;
        extras[5] = strut;
        extras[4] = strut;
        extras[3] = strut;
        extras[2] = strut;
        extras[1] = strut;
        extras[0] = strut;

        hbox.addLast(extras[0]);
 
        params = FILTER_1_TYPES;
        comp = new Chooser("Type", this, "filter1type", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                hbox.removeLast();
                hbox.addLast(extras[model.get(key)]);
                hbox.revalidate();
                hbox.repaint();
                }
            };
            
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Cutoff", this, "filter1cutoff", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Resonance", this, "filter1resonance", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Keytrack", this, "filter1keytrack", color, 0, 127)
            {
            public String map(int val)
                {
                return keytrack(val);
                }
            public boolean isSymmetric()
                {
                return true;
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Envelope", this, "filter1envamount", color, 0, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        comp = new LabelledDial("Envelope", this, "filter1envvelocity", color, 0, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Velocity");
        hbox.add(comp);
        
        category.add(hbox, BorderLayout.WEST);
        return category;
        }




    /** Add a Filter2 category */
    public JComponent addFilter2(Color color)
        {
        Category category = new Category(this, "Filter 2", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        params = FILTER_2_TYPES;
        comp = new Chooser("Type", this, "filter2type", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Cutoff", this, "filter2cutoff", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Keytrack", this, "filter2keytrack", color, 0, 127)
            {
            public String map(int val)
                {
                return keytrack(val);
                }
            public boolean isSymmetric()
                {
                return true;
                }
            };
        hbox.add(comp);

        category.add(hbox, BorderLayout.WEST);
        return category;
        }
        
    /** Add Play Parameter category */
    public JComponent addPlayParameters(Color color)
        {
        Category category = new Category(this, "Play Parameters", color);
        category.makeDistributable("playparam");
                
        JComponent comp;
        String[] params = PLAY_PARAMETERS;
        HBox hbox = new HBox();
        //        VBox vbox = new VBox();
        
        for(int i = 1; i < 5; i++)
            {
            comp = new Chooser("Play Parameter " + i, this, "playparam" + i, params);
            hbox.add(comp);
            }
                        
        category.add(hbox, BorderLayout.WEST);
        return category;
        }

    /** Add Amplifier and Pan category */
    public JComponent addAmplifier(Color color)
        {
        Category category = new Category(this, "Amplifier and Pan", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        comp = new LabelledDial("Volume", this, "amplifiervolume", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Envelope", this, "amplifierenvvelocity", color, 0, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Velocity");
        hbox.add(comp);

        comp = new LabelledDial("Volume", this, "amplifierkeytrack", color, 0, 127)
            {
            public String map(int val)
                {
                return keytrack(val);
                }
            public boolean isSymmetric()
                {
                return true;
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Keytrack");
        hbox.add(comp);

        comp = new LabelledDial("Panning", this, "pan", color, 0, 127, 64)
            {
            public String map(int val)
                {
                if ((val - 64) < 0) return "L " + Math.abs(val - 64);
                else if ((val - 64) > 0) return "R " + (val - 64);
                else return "--";
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Panning", this, "pankeytrack", color, 0, 127)
            {
            public String map(int val)
                {
                return keytrack(val);
                }
            public boolean isSymmetric()
                {
                return true;
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Keytrack");
        hbox.add(comp);

        comp = new CheckBox("Chorus", this, "chorus");
        // irritating font metric bugs bite again
        ((CheckBox)comp).addToWidth(1);
        vbox.add(comp);
        hbox.add(vbox);
                
        category.add(hbox, BorderLayout.WEST);
        return category;
        }

    /** Add Glid Category */
    public JComponent addGlide(Color color)
        {
        Category category = new Category(this, "Glide", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        //        VBox vbox = new VBox();
        

        VBox vbox = new VBox();
        params = OSCILLATOR_GLIDE_TYPES;
        comp = new Chooser("Type", this, "glidetype", params);
        vbox.add(comp);
                
        HBox hbox2 = new HBox();
        comp = new CheckBox("Active", this, "glide");
        hbox2.add(comp);
        comp = new CheckBox("Linear", this, "glidemode");
        hbox2.add(comp);
        vbox.add(hbox2);
        hbox.add(vbox);

        comp = new LabelledDial("Time", this, "glidetime", color, 0, 127);
        hbox.add(comp);
        
        category.add(hbox, BorderLayout.WEST);
        return category;
        }

    /** Add an LFO category */
    public JComponent addLFO(final int lfo, Color color)
        {
        Category category = new Category(this, "LFO " + lfo, color);
        category.makePasteable("lfo" + lfo);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        //        VBox vbox = new VBox();
        

        VBox vbox = new VBox();
        params = LFO_SHAPES;
        comp = new Chooser("Shape", this, "lfo" + lfo + "shape", params);
        vbox.add(comp);
        params = LFO_SYNC;
        comp = new Chooser("Sync", this, "lfo" + lfo + "sync", params);
        vbox.add(comp);
        hbox.add(vbox);
                
        comp = new LabelledDial("Speed", this, "lfo" + lfo + "rate", color, 0, 127)
            {
            public String map(int val)
                {
                // we display this in two different ways depending on whether we're clocked or not
                if (model.get("lfo" + lfo + "sync") < 2)  // 3 is "clocked"
                    return "" + val;
                else
                    {
                    val /= 4;  // we map to 0...31
                    String[] vals = LFO_SPEEDS;
                    return vals[val];
                    }
                }
            };
        model.register("lfo" + lfo + "sync", (LabelledDial)comp);  // so we get updated if clocked changes
        hbox.add(comp);
        
        comp = new LabelledDial("Delay", this, "lfo" + lfo + "delay", color, 0, 127)
            {
            public String map(int val)
                {
                if (val == 0)
                    return "Off";
                else if (val == 1)
                    return "Retrig.";
                else return "" + (val - 1);
                }
            };
        model.setMetricMin( "lfo" + lfo + "delay", 2);
        hbox.add(comp);
        
        comp = new LabelledDial("Symmetry", this, "lfo" + lfo + "symmetry", color, 0, 127, 64);
        hbox.add(comp);
        
        comp = new LabelledDial("Humanize", this, "lfo" + lfo + "humanize", color, 0, 127);
        hbox.add(comp);

        if (lfo == 2)
            {
            comp = new LabelledDial("Phase", this, "lfo" + lfo + "phase", color, 0, 127)
                {
                public String map(int val)
                    {
                    // This phase is complex.  Normally we go by 3.
                    // We start at 3, then at 6 we jump to 8.
                    // Why this is different from phase calculation elsewhere I have no idea
                    // Then at 23 we jump to 25.    // Note 23 is 17 from 6
                    // Then at 37 we jump to 39.    // Note 37 is 14 from 23
                    // Then at 51 we jump to 53             // Note 51 is 14 from 37
                    // Then at 68 we jump to 70.    // Note 68 is 17 from 51
                    // Then at 82 we jump to 84.    // Note 82 is 14 from 68
                    // For this reason I've just hard-coded the strings
                    return LFO_PHASES[val];
                    }
                };
                        
            model.setMetricMin( "lfo" + lfo + "phase", 1);
            hbox.add(comp);
            }
        
        category.add(hbox, BorderLayout.WEST);
        return category;
        }
        
    /** Add a "standard" envelope category */
    public JComponent addEnvelope(final int env, Color color)
        {
        Category category = new Category(this, env == 1 ? "Filter Envelope" : "Amplifier Envelope", color);
        category.makePasteable("envelope" + env);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        //        VBox vbox = new VBox();
        

        VBox vbox = new VBox();
        params = TRIGGERS;
        comp = new Chooser("Trigger", this, "envelope" + env + "trigger", params);
        vbox.add(comp);
        hbox.add(vbox);
                
        comp = new LabelledDial("Attack", this, "envelope" + env + "attack", color, 0, 127);
        hbox.add(comp);
        
        comp = new LabelledDial("Decay", this, "envelope" + env + "decay", color, 0, 127);
        hbox.add(comp);
        
        comp = new LabelledDial("Sustain", this, "envelope" + env + "sustain", color, 0, 127);
        hbox.add(comp);
        
        comp = new LabelledDial("Release", this, "envelope" + env + "release", color, 0, 127);
        hbox.add(comp);
        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
        

    /** Add wave envelope category */
    public JComponent addWaveEnvelope(Color color)
        {
        Category category = new Category(this, "Wave Envelope", color);
        category.makeDistributable("waveenv");
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        //        VBox vbox = new VBox();
        
        VBox vbox = new VBox();
        params = TRIGGERS;
        comp = new Chooser("Trigger", this, "waveenvtrigger", params);
        vbox.add(comp);
        comp = new CheckBox("Key On Loop", this, "waveenvkeyon");
        vbox.add(comp);
        comp = new CheckBox("Key Off Loop", this, "waveenvkeyoff");
        vbox.add(comp);
        hbox.add(vbox);
                
        vbox = new VBox();
        comp = new LabelledDial("Key On", this, "waveenvkeyonstart", color, 0, 7, -1);
        ((LabelledDial)comp).addAdditionalLabel("Loop Start");
        vbox.add(comp);

        comp = new LabelledDial("Key On", this, "waveenvkeyonend", color, 0, 7, -1);
        ((LabelledDial)comp).addAdditionalLabel(" Loop End ");  // additional space because OS X cuts off the "d"
        ((LabelledDial)comp).addAdditionalLabel("(Sustain Pos)");  // additional space because OS X cuts off the "d"
        vbox.add(comp);
        hbox.add(vbox);
                
        vbox = new VBox();
        comp = new LabelledDial("Key Off", this, "waveenvkeyoffstart", color, 0, 7, -1);
        ((LabelledDial)comp).addAdditionalLabel("Loop Start");
        vbox.add(comp);

        comp = new LabelledDial("Key Off", this, "waveenvkeyoffend", color, 0, 7, -1);
        ((LabelledDial)comp).addAdditionalLabel(" Loop End ");  // additional space because OS X cuts off the "d"
        ((LabelledDial)comp).addAdditionalLabel(" (End Pos) ");  // additional space because OS X cuts off the "d"
        vbox.add(comp);
        hbox.add(vbox);
                
        for(int i = 1; i < 9; i++)
            {
            vbox = new VBox();
            comp = new LabelledDial("Time " + i, this, "waveenvtime" + i, color, 0, 127);
            ((LabelledDial)comp).addAdditionalLabel(" ");
            vbox.add(comp);
            comp = new LabelledDial("Level " + i, this, "waveenvlevel" + i, color, 0, 127);
            vbox.add(comp);
            hbox.add(vbox);
            }

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    /** Add free envelope category */
    public JComponent addFreeEnvelope(Color color)
        {
        Category category = new Category(this, "Free Envelope", color);
        category.makeDistributable("freeenv");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        //        VBox vbox = new VBox();
        
        VBox vbox = new VBox();
        params = TRIGGERS;
        comp = new Chooser("Trigger", this, "freeenvtrigger", params);
        vbox.add(comp);
        hbox.add(vbox);
                
        for(int i = 1; i < 4; i++)
            {
            //vbox = new VBox();
            comp = new LabelledDial("Time " + i, this, "freeenvtime" + i, color, 0, 127);
            ((LabelledDial)comp).addAdditionalLabel(" ");
            hbox.add(comp);
            comp = new LabelledDial("Level " + i, this, "freeenvlevel" + i, color, 0, 127, 64);
            if (i==3)
                ((LabelledDial)comp).addAdditionalLabel("(Sustain)");
            hbox.add(comp);
            //hbox.add(vbox);
            }

        //vbox = new VBox();
        comp = new LabelledDial("Release", this, "freeenvtime4", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Time");
        hbox.add(comp);
        comp = new LabelledDial("Release", this, "freeenvlevel4", color, 0, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);
        //hbox.add(vbox);
                           
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    /** Add "standard" envelope display */
    public JComponent addEnvelopeDisplay(final int env, Color color)
        {
        Category category = new Category(this, env == 1 ? "Filter" : "Amplifier", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        //        VBox vbox = new VBox();
        
        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "envelope" + env +  "attack", "envelope" + env +  "decay", null, "envelope" + env +  "release" },
            new String[] { null, null, "envelope" + env +  "sustain", "envelope" + env +  "sustain", null },
            new double[] { 0, 0.25/127.0, 0.25 / 127.0,  0.25, 0.25/127.0},
            new double[] { 0, 1.0, 1.0 / 127.0, 1.0/127.0, 0 });
        hbox.addLast(comp);
        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    /** Add free envelope display */
    public JComponent addFreeEnvelopeDisplay(Color color)
        {
        Category category = new Category(this, "Free", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        //        VBox vbox = new VBox();
        
        EnvelopeDisplay disp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "freeenvtime1", "freeenvtime2", "freeenvtime3", null, "freeenvtime4"},   //, null },
            new String[] { null, "freeenvlevel1", "freeenvlevel2", "freeenvlevel3", "freeenvlevel3", "freeenvlevel4"},            //, null },
            new double[] { 0, 0.2/127.0, 0.2 / 127.0,  0.2/127.0, 0.2, 0.2/127.0},              //, 0},
            new double[] { 64.0/127.0, 1.0/127.0, 1.0/127.0, 1.0/127.0, 1.0/127.0, 1.0/127.0}           //, 64.0/127.0 }
            );
        disp.setAxis(64.0 / 127.0);
        comp = disp;
        hbox.addLast(comp);    
        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    /** Add wave envelope display */
    public JComponent addWaveEnvelopeDisplay(Color color)
        {
        Category category = new Category(this, "Wave", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        //        VBox vbox = new VBox();
        
        EnvelopeDisplay disp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "waveenvtime1", "waveenvtime2", "waveenvtime3", "waveenvtime4", "waveenvtime5", "waveenvtime6", "waveenvtime7", "waveenvtime8", null },
            new String[] { null, "waveenvlevel1", "waveenvlevel2", "waveenvlevel3", "waveenvlevel4", "waveenvlevel5", "waveenvlevel6", "waveenvlevel7", "waveenvlevel8", null },
            new double[] { 0, 0.25/127.0/2.0, 0.25 / 127.0/2.0,  0.25/127.0/2.0, 0.25/127.0/2.0,  0.25/127.0/2.0, 0.25 / 127.0/2.0,  0.25/127.0/2.0, 0.25/127.0/2.0, 0},
            new double[] { 0, 1.0/127.0, 1.0/127.0, 1.0/127.0, 1.0/127.0, 1.0/127.0, 1.0/127.0, 1.0/127.0, 1.0/127.0, 0 })
            {
            public int postProcessLoopOrStageKey(String key, int val)
                {
                // they'll all be off by 1
                return val + 1;
                }
            };
        disp.setPreferredWidth(disp.getPreferredWidth() * 2);
        disp.setLoopKeys(0, "waveenvkeyonstart", "waveenvkeyonend");
        disp.setLoopKeys(1, "waveenvkeyoffstart", "waveenvkeyoffend");
        disp.setSustainStageKey("waveenvkeyonend");
        disp.setFinalStageKey("waveenvkeyoffend");
        comp = disp;
        hbox.addLast(comp);
        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    /** Add allocation/assignment category */
    public JComponent addAllocation(Color color)
        {
        Category category = new Category(this, "Allocation", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        //        VBox vbox = new VBox();
        
        VBox vbox = new VBox();
        params = ASSIGNMENT;
        comp = new Chooser("Assignment", this, "assignment", params);
        vbox.add(comp);
        comp = new CheckBox("Mono", this, "allocation");
        vbox.add(comp);
        hbox.add(vbox);
                
        comp = new LabelledDial("Detune", this, "detune", color, 0, 127);
        hbox.add(comp);
        comp = new LabelledDial("De-pan", this, "de-pan", color, 0, 127);
        hbox.add(comp);

        category.add(hbox, BorderLayout.WEST);
        return category;
        }


    /** Add the Modulation category */
    public JComponent addModulation(Color color)
        {
        Category category  = new Category(this, "Modulation", color);
        category.makeDistributable("modulation");
                        
        JComponent comp;
        String[] params;
        VBox main = new VBox();
        HBox hbox;
        VBox vbox;
        
        for(int row = 1; row < 17; row+= 4)
            {
            hbox = new HBox();
            boolean first = true;
            for(int i = row; i < row + 4; i++)
                {
                vbox = new VBox();

                // add some space
                if (!first)  // not the first one
                    {
                    hbox.add(Strut.makeHorizontalStrut(10));
                    }

                params = MOD_SOURCES;
                comp = new Chooser("" + i + " Source", this, "modulation" + i + "source", params);
                vbox.add(comp);

                params = MOD_DESTINATIONS;
                comp = new Chooser("" + i + " Destination", this, "modulation" + i + "destination", params);
                vbox.add(comp);

                hbox.add(vbox);
                comp = new LabelledDial("" + i + " Level", this, "modulation" + i + "amount", color, 0, 127, 64);  // it's Level, not Amount, so we save some horizontal space
                hbox.add(comp);

                first = false;
                }
                        
            // add some space
            if (row > 1)  // not the first one
                {
                main.add(Strut.makeVerticalStrut(10));
                }

            main.add(hbox);
            }
                                
        category.add(main, BorderLayout.WEST);
        return category;
        }

    /** Add the Modifiers category */
    public JComponent addModifiers(Color color)
        {
        Category category  = new Category(this, "Modifiers", color);
        category.makeDistributable("modifier");
                        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        VBox vbox2 = new VBox();
        params = MOD_SOURCES;
        comp = new Chooser("Delay Source", this, "modifierdelaysource", params);
        vbox2.add(comp);

        comp = new LabelledDial("Delay Time", this, "modifierdelaytime", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel(" ");
        vbox2.add(comp);
        
        hbox.add(vbox2);

        for(int i = 1; i < 5; i++)
            {
            vbox2 = new VBox();
            params = MOD_SOURCES;
            comp = new Chooser("" + i + " Source #1", this, "modifier" + i + "source1", params);
            vbox2.add(comp);

            // gotta change the first one to "constant" from "off" if we're in Source B
            params = new String[MOD_SOURCES.length];
            System.arraycopy(MOD_SOURCES, 0, params, 0, MOD_SOURCES.length);
            params[0] = "Constant";

            comp = new Chooser("" + i + " Source #2", this, "modifier" + i + "source2", params);
            vbox2.add(comp);

            params = MODIFIER_OPERATORS;
            comp = new Chooser("" + i + " Type", this, "modifier" + i + "type", params);
            vbox2.add(comp);
            hbox.add(vbox2);
                
            if (i == 1 || i == 3)
                {
                vbox2 = new VBox();
                HBox hbox2 = new HBox();
                comp = new LabelledDial("" + i + " Param", this, "modifier" + i + "param", color, 0, 127);
                //((LabelledDial)comp).addAdditionalLabel(" ");
                hbox2.add(comp);
                hbox2.add(Strut.makeHorizontalStrut(20));
                vbox2.add(hbox2);
                                
                vbox2.add(Strut.makeVerticalStrut(5));
                                
                hbox2 = new HBox();
                hbox2.add(Strut.makeHorizontalStrut(20));
                comp = new LabelledDial("" + (i + 1) + " Param", this, "modifier" + (i + 1) + "param", color, 0, 127);
                hbox2.add(comp);
                vbox2.add(hbox2);                               
                hbox.add(vbox2);
                }
                                        
            if (i == 2)
                {
                hbox.add(Strut.makeHorizontalStrut(20));
                }
            }

        vbox.add(hbox);
        category.add(vbox, BorderLayout.WEST);
        return category;
        }


    /** Add the Modifiers category */
    public JComponent addArpeggiation(Color color)
        {
        Category category  = new Category(this, "Arpeggiation", color);
                        
        final CheckBox[] arpeggiation = new CheckBox[16];
        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox big = new VBox();
        VBox vbox = new VBox();
        
        params = ARPEGGIATOR_ACTIVE;
        comp = new Chooser("Active", this, "arp", params);
        vbox.add(comp);
        params = ARPEGGIATOR_DIRECTION;
        comp = new Chooser("Direction", this, "arpdirection", params);
        vbox.add(comp);
        hbox.add(vbox);
        
        vbox = new VBox();
        params = ARPEGGIATOR_ORDER;
        comp = new Chooser("Note Order", this, "arpnoteorder", params);
        vbox.add(comp);
        params = ARPEGGIATOR_VELOCITY;
        comp = new Chooser("Velocity", this, "arpvelocity", params);
        vbox.add(comp);
        hbox.add(vbox);
        
        
        comp = new LabelledDial("Tempo", this, "arptempo", color, 1, 127)
            {
            public String map(int val)
                {
                if (val == 1)
                    return "Extern";
                else
                    {
                    return "" + (50 + (val - 2) * 2);
                    }
                }
            public int getDefaultValue() { return 37; } // 120 BPM
            };
        model.setMetricMin( "arptempo", 2);
        hbox.add(comp);
        
        comp = new LabelledDial("Clock", this, "arpclock", color, 0, 15)
            {
            public String map(int val)
                {
                return ARP_CLOCK[val];
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Range", this, "arprange", color, 1, 10);
        hbox.add(comp);

        comp = new LabelledDial("Pattern", this, "arppattern", color, 0, 16)
            {
            public String map(int val)
                {
                if (val == 0)
                    return "Off";
                else if (val == 1)
                    return "User";
                else return "" + (val - 1);
                }
            };
        model.setMetricMin( "arppattern", 2);
        hbox.add(comp);

        comp = new LabelledDial("User Length", this, "arpuserlength", color, 0, 15, -1);
        hbox.add(comp);

        VBox vbox2 = new VBox();
        comp = new CheckBox("Reset on Start", this, "arpreset");
        vbox2.add(comp);
        hbox.add(vbox2);
        
        Updatable updateArp = new Updatable()
            {
            public void update(String key, Model model)
                {
                int pattern = model.get("arppattern");
                if (pattern == 1)
                    {
                    int len = model.get("arpuserlength") + 1;
                    for(int i = 0; i < len; i++)
                        {
                        arpeggiation[i].setEnabled(true);
                        }
                    for(int i = len; i < 16; i++)
                        {
                        arpeggiation[i].setEnabled(false);
                        }
                    for(int i = 0; i < 16; i++)
                        {
                        arpeggiation[i].setBorder(Style.CHECKBOX_NON_HIGHLIGHTED_BORDER());
                        }
                    }
                else
                    {
                    for(int i = 0; i < 16; i++)
                        {
                        arpeggiation[i].setEnabled(false);
                        }
                                        
                    if (pattern > 1)
                        pattern--;
                                        
                    for(int i = 0; i < 16; i++)
                        {
                        arpeggiation[i].setBorder(ARP_PATTERNS[pattern][i] == 0 ?
                            Style.CHECKBOX_NON_HIGHLIGHTED_BORDER() : Style.CHECKBOX_HIGHLIGHTED_BORDER());
                        }
                    }
                }
            };
                
        model.register("arpuserlength", updateArp);
        model.register("arppattern", updateArp);

        big.add(hbox);

        hbox = new HBox();
        hbox.add(Strut.makeHorizontalStrut(Style.CATEGORY_INSET_DISTANCE()));
        hbox.add(new TextLabel("Pattern:     "));

             
        for(int pattern = 0; pattern < 16; pattern++)
            {
            arpeggiation[pattern] = new CheckBox("", this, "arpuser" + (pattern + 1));
            arpeggiation[pattern].setBorder(Style.CHECKBOX_HIGHLIGHTED_BORDER());
            arpeggiation[pattern].setEnabled(false);  // because we start with arppattern not set to User
            hbox.add(arpeggiation[pattern]);
            if (pattern % 4 == 3)
                {
                hbox.add(Strut.makeHorizontalStrut(20));
                }
            }
        big.add(hbox);
        
        category.add(big, BorderLayout.WEST);
        return category;
        }






    // Holds the current effects parameters
    HBox parameters;
        
    // The various JComponents for different effect parameters
    JComponent[/*effect type*/][/*parameters*/] parametersByEffect = new JComponent[11][3];
        
    // Various effect types
    public static final int BYPASS = 0;
    public static final int CHORUS = 1;
    public static final int FLANGER_1 = 2;
    public static final int FLANGER_2 = 3;
    public static final int AUTO_WAH_LP = 4;
    public static final int AUTO_WAH_BP = 5;
    public static final int OVERDRIVE = 6;
    public static final int AMP_MOD = 7;
    public static final int DELAY = 8;
    public static final int PAN_DELAY = 9;
    public static final int MOD_DELAY = 10;


    /** Discards existing parameter widgets and loads new ones according to the
        effect type on the given effect number. */
    void setupEffect(int type)
        {
        if (type < 10)
            {
            if (parameters == null) return;  // not ready yet
                
            parameters.removeAll();
            for(int i = 0; i < parametersByEffect[type].length; i++)
                {
                parameters.add(parametersByEffect[type][i]);
                }
            }
        parameters.revalidate();
        repaint();
        }




    /** Adds an Effect category.  */
    public JComponent addEffect(Color color)
        {
        // The first thing we have to do is build all the effect parameters for all the effect types
        // and associate them with each effect type.  This is a lot of tedious work.
                
        JComponent comp;
        String[] params;
        
        // bypass has no parameters
        parametersByEffect[BYPASS] = new JComponent[0];
        
        // parameter 0
                
        comp = new LabelledDial("Speed", this, "effectparam1", color, 0, 127);
        parametersByEffect[CHORUS][0] = comp;
        parametersByEffect[FLANGER_1][0] = comp;
        parametersByEffect[FLANGER_2][0] = comp;
        parametersByEffect[AMP_MOD][0] = comp;

        comp = new LabelledDial("Sense", this, "effectparam1", color, 0, 127);
        parametersByEffect[AUTO_WAH_LP][0] = comp;
        parametersByEffect[AUTO_WAH_BP][0] = comp;
        
        comp = new LabelledDial("Drive", this, "effectparam1", color, 0, 127);
        parametersByEffect[OVERDRIVE][0] = comp;

        comp = new LabelledDial("Time", this, "effectparam1", color, 0, 127);
        parametersByEffect[DELAY][0] = comp;
        parametersByEffect[PAN_DELAY][0] = comp;
        parametersByEffect[MOD_DELAY][0] = comp;
        
        
        // parameter 1

        comp = new LabelledDial("Depth", this, "effectparam2", color, 0, 127);
        parametersByEffect[CHORUS][1] = comp;
        parametersByEffect[FLANGER_1][1] = comp;

        comp = new LabelledDial("Feedback", this, "effectparam2", color, 0, 127);
        parametersByEffect[FLANGER_2][1] = comp;
        parametersByEffect[DELAY][1] = comp;
        parametersByEffect[PAN_DELAY][1] = comp;

        comp = new LabelledDial("Cutoff", this, "effectparam2", color, 0, 127);
        parametersByEffect[AUTO_WAH_LP][1] = comp;
        parametersByEffect[AUTO_WAH_BP][1] = comp;
        
        comp = new LabelledDial("Gain", this, "effectparam2", color, 0, 127);
        parametersByEffect[OVERDRIVE][1] = comp;

        comp = new LabelledDial("Spread", this, "effectparam2", color, 0, 127);
        parametersByEffect[AMP_MOD][1] = comp;

        comp = new LabelledDial("Speed", this, "effectparam2", color, 0, 127);
        parametersByEffect[MOD_DELAY][1] = comp;


        // parameter 2

        // 64 so it's centered
        comp = new LabelledDial("Mix", this, "effectparam3", color, 0, 127, 64)
            {
            public String map(int val)
                {
                return "" + (127 - val) + ":" + val;
                }
            };
        parametersByEffect[CHORUS][2] = comp;
        parametersByEffect[FLANGER_1][2] = comp;
        parametersByEffect[FLANGER_2][2] = comp;
        parametersByEffect[AMP_MOD][2] = comp;
        parametersByEffect[DELAY][2] = comp;
        parametersByEffect[PAN_DELAY][2] = comp;

        comp = new LabelledDial("Resonance", this, "effectparam3", color, 0, 127);
        parametersByEffect[AUTO_WAH_LP][2] = comp;
        parametersByEffect[AUTO_WAH_BP][2] = comp;

        comp = new LabelledDial("Amp Type", this, "effectparam3", color, 0, 127)
            {
            public String map(int val)
                {
                if (val >= 24)
                    return "Stack";
                else if (val >= 16)
                    return "Medium";
                else if (val >= 8)
                    return "Combo";
                else
                    return "Direct";
                }
            };
        parametersByEffect[OVERDRIVE][2] = comp;
        
        comp = new LabelledDial("Depth", this, "effectparam3", color, 0, 127);
        parametersByEffect[MOD_DELAY][2] = comp;


        // Now we can set up the category as usual.
                
        Category category = new Category(this, "Effects", color);
                        
        HBox main = new HBox();
        VBox vbox = new VBox();
                
        params = EFFECT_TYPES;
        comp = new Chooser("Type", this, "effecttype", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                setupEffect(getState());
                }
            };
        vbox.add(comp);
        main.add(vbox);

        parameters = new HBox();
        main.add(parameters);

        category.add(main, BorderLayout.WEST);
                
        setupEffect(BYPASS);
        return category;
        }







    /** Map of parameter -> index in the allParameters array. */
    HashMap allParametersToIndex = new HashMap();


    /** List of all Waldorf parameters in order.  "-" is a reserved (unused and thus unnamed) parameter. */

    /// * indicates parameters which must be handled specially due to packing
    /// that Waldorf decided to do.  :-(

    final static String[] allParameters = new String[/*256*/] 
    {
    "soundformatversion",                               // always 1 **
    "osc1octave",                   
    "osc1semitone",
    "osc1detune",
    "-",
    "osc1bendrange",
    "osc1keytrack",
    "osc1fmamount",
    "-",
    "-",
    "-",
    "-",
    "osc2octave",                   
    "osc2semitone",
    "osc2detune",
    "-",
    "osc2sync",
    "osc2bendrange",
    "osc2keytrack",
    "osc2link",
    "-",
    "-",
    "-",
    "-",
    "-",
    "wavetable",
    "wave1startwave",
    "wave1startphase",
    "wave1envamount",
    "wave1envvelocity",
    "wave1keytrack",
    "wave1limit",
    "-",
    "-",
    "-",
    "-",
    "wave2startwave",
    "wave2startphase",
    "wave2envamount",
    "wave2envvelocity",
    "wave2keytrack",
    "wave2limit",
    "wave2link",
    "-",
    "-",
    "-",
    "-",
    "mixwave1",
    "mixwave2",
    "mixringmod",       
    "mixnoise",
    "mixexternal",                                      // xt only
    "-",
    "aliasing",
    "timequant",
    "clipping",
    "-",
    "accuracy",
    "playparam1",
    "playparam2",
    "playparam3",
    "playparam4",
    "filter1cutoff",
    "filter1resonance",
    "filter1type",
    "filter1keytrack",
    "filter1envamount",
    "filter1envvelocity",
    "-",
    "-",
    "filter1special",
    "-",
    "-",
    "filter2cutoff",
    "filter2type",
    "filter2keytrack",
    "effecttype",
    "amplifiervolume",
    "-",
    "amplifierenvvelocity",
    "amplifierkeytrack",
    "effectparam1",
    "chorus",
    "effectparam2",
    "pan",
    "pankeytrack",
    "effectparam3",
    "glide",
    "glidetype",
    "glidemode",
    "glidetime",
    "-",
    "arp",
    "arptempo",
    "arpclock",
    "arprange",
    "arppattern",
    "arpdirection",
    "arpnoteorder",
    "arpvelocity",
    "arpreset",
    "arpuserlength",
    "arpuser1", //... "arpuser4"                                        //*
    "arpuser5", //... "arpuser8"                                        //*
    "arpuser9", //... "arpuser12"                                       //*
    "arpuser13", //... "arpuser16"                                      //*
    "-",
    "-",
    "allocation",
    "assignment",
    "detune",
    "-",
    "de-pan",
    "envelope1attack",
    "envelope1decay",
    "envelope1sustain",
    "envelope1release",
    "envelope1trigger",
    "-",
    "envelope2attack",
    "envelope2decay",
    "envelope2sustain",
    "envelope2release",
    "envelope2trigger",
    "-",
    "waveenvtime1",
    "waveenvlevel1",
    "waveenvtime2",
    "waveenvlevel2",
    "waveenvtime3",
    "waveenvlevel3",
    "waveenvtime4",
    "waveenvlevel4",
    "waveenvtime5",
    "waveenvlevel5",
    "waveenvtime6",
    "waveenvlevel6",
    "waveenvtime7",
    "waveenvlevel7",
    "waveenvtime8",
    "waveenvlevel8",
    "waveenvtrigger",
    "waveenvkeyon",
    "waveenvkeyonstart",
    "waveenvkeyonend",
    "waveenvkeyoff",
    "waveenvkeyoffstart",
    "waveenvkeyoffend",
    "-",
    "freeenvtime1",
    "freeenvlevel1",
    "freeenvtime2",
    "freeenvlevel2",
    "freeenvtime3",
    "freeenvlevel3",
    "freeenvtime4",
    "freeenvlevel4",
    "freeenvtrigger",
    "-",
    "lfo1rate",
    "lfo1shape",
    "lfo1delay",
    "lfo1sync",                                                 // *
    "lfo1symmetry",
    "lfo1humanize",
    "-",
    "lfo2rate",
    "lfo2shape",
    "lfo2delay",
    "lfo2sync",                                                 // *
    "lfo2symmetry",
    "lfo2humanize",
    "lfo2phase",
    "-",
    "modifierdelaysource",
    "modifierdelaytime",
    "modifier1source1",
    "modifier1source2",
    "modifier1type",
    "modifier1param",
    "modifier2source1",
    "modifier2source2",
    "modifier2type",
    "modifier2param",
    "modifier3source1",
    "modifier3source2",
    "modifier3type",
    "modifier3param",
    "modifier4source1",
    "modifier4source2",
    "modifier4type",
    "modifier4param",
    "modulation1source",
    "modulation1amount",
    "modulation1destination",
    "modulation2source",
    "modulation2amount",
    "modulation2destination",
    "modulation3source",
    "modulation3amount",
    "modulation3destination",
    "modulation4source",
    "modulation4amount",
    "modulation4destination",
    "modulation5source",
    "modulation5amount",
    "modulation5destination",
    "modulation6source",
    "modulation6amount",
    "modulation6destination",
    "modulation7source",
    "modulation7amount",
    "modulation7destination",
    "modulation8source",
    "modulation8amount",
    "modulation8destination",
    "modulation9source",
    "modulation9amount",
    "modulation9destination",
    "modulation10source",
    "modulation10amount",
    "modulation10destination",
    "modulation11source",
    "modulation11amount",
    "modulation11destination",
    "modulation12source",
    "modulation12amount",
    "modulation12destination",
    "modulation13source",
    "modulation13amount",
    "modulation13destination",
    "modulation14source",
    "modulation14amount",
    "modulation14destination",
    "modulation15source",
    "modulation15amount",
    "modulation15destination",
    "modulation16source",
    "modulation16amount",
    "modulation16destination",
    "name",         // *
    "name",         // *
    "name",         // *
    "name",         // *
    "name",         // *
    "name",         // *
    "name",         // *
    "name",         // *
    "name",         // *
    "name",         // *
    "name",         // *
    "name",         // *
    "name",         // *
    "name",         // *
    "name",         // *
    "name",         // *
    };



    public Object[] emitAll(String key)
        {
        if (key.equals("bank")) return new Object[0];  // this is not emittable
        if (key.equals("number")) return new Object[0];  // this is not emittable
        byte DEV = (byte)(getID());
                
        if (key.equals("effecttype"))
            {
            int index = ((Integer)(allParametersToIndex.get(key))).intValue();
            byte HH = (byte)((index >>> 7) & 127);
            byte PP = (byte)(index & 127);
            byte XX = (byte)model.get(key);
            // handle XT effects specially
            if (XX == 8) // delay
                XX = 32;
            else if (XX == 9)  // pan delay
                XX = 33;
            else if (XX == 10)  // mod delay
                XX = 34;
            byte[] data = new byte[] { (byte)0xF0, 0x3E, 0x0E, DEV, 0x20, 0x00, HH, PP, XX, (byte)0xF7 };
            return new Object[] { data };
            }
        else if (key.equals("osc1octave") || key.equals("osc2octave"))
            {
            int index = ((Integer)(allParametersToIndex.get(key))).intValue();
            byte HH = (byte)((index >>> 7) & 127);
            byte PP = (byte)(index & 127);
            byte XX = (byte)(16 + model.get(key) * 12);
            byte[] data = new byte[] { (byte)0xF0, 0x3E, 0x0E, DEV, 0x20, 0x00, HH, PP, XX, (byte)0xF7 };
            return new Object[] { data };
            }
        else if (key.equals("lfo1sync") || key.equals("lfo2sync"))
            {
            int index = ((Integer)(allParametersToIndex.get(key))).intValue();
            byte HH = (byte)((index >>> 7) & 127);
            byte PP = (byte)(index & 127);
            byte XX = (byte)model.get(key);
            if (XX == 2) XX = 3;  // because it's of/on/on/Clock, I dunno why
            byte[] data = new byte[] { (byte)0xF0, 0x3E, 0x0E, DEV, 0x20, 0x00, HH, PP, XX, (byte)0xF7 };
            return new Object[] { data };
            }
        else if (key.equals("arpuser1") || key.equals("arpuser2") || key.equals("arpuser3") || key.equals("arpuser4"))
            {
            int index = 102;
            int arp1 = model.get("arpuser1");
            int arp2 = model.get("arpuser2");
            int arp3 = model.get("arpuser3");
            int arp4 = model.get("arpuser4");
            int total = (arp1 << 3) | (arp2 << 2) | (arp3 << 1) | (arp4);    /// Do I have these backwards?
            byte HH = (byte)((index >>> 7) & 127);
            byte PP = (byte)(index & 127);
            byte XX = (byte)(total);
            byte[] data = new byte[] { (byte)0xF0, 0x3E, 0x0E, DEV, 0x20, 0x00, HH, PP, XX, (byte)0xF7 };
            return new Object[] { data };
            }
        else if (key.equals("arpuser5") || key.equals("arpuser6") || key.equals("arpuser7") || key.equals("arpuser8"))
            {
            int index = 103;
            int arp1 = model.get("arpuser5");
            int arp2 = model.get("arpuser6");
            int arp3 = model.get("arpuser7");
            int arp4 = model.get("arpuser8");
            int total = (arp1 << 3) | (arp2 << 2) | (arp3 << 1) | (arp4);    /// Do I have these backwards?
            byte HH = (byte)((index >>> 7) & 127);
            byte PP = (byte)(index & 127);
            byte XX = (byte)(total);
            byte[] data = new byte[] { (byte)0xF0, 0x3E, 0x0E, DEV, 0x20, 0x00, HH, PP, XX, (byte)0xF7 };
            return new Object[] { data };
            }
        else if (key.equals("arpuser9") || key.equals("arpuser10") || key.equals("arpuser11") || key.equals("arpuser12"))
            {
            int index = 104;
            int arp1 = model.get("arpuser9");
            int arp2 = model.get("arpuser10");
            int arp3 = model.get("arpuser11");
            int arp4 = model.get("arpuser12");
            int total = (arp1 << 3) | (arp2 << 2) | (arp3 << 1) | (arp4);    /// Do I have these backwards?
            byte HH = (byte)((index >>> 7) & 127);
            byte PP = (byte)(index & 127);
            byte XX = (byte)(total);
            byte[] data = new byte[] { (byte)0xF0, 0x3E, 0x0E, DEV, 0x20, 0x00, HH, PP, XX, (byte)0xF7 };
            return new Object[] { data };
            }
        else if (key.equals("arpuser13") || key.equals("arpuser14") || key.equals("arpuser15") || key.equals("arpuser16"))
            {
            int index = 105;
            int arp1 = model.get("arpuser13");
            int arp2 = model.get("arpuser14");
            int arp3 = model.get("arpuser15");
            int arp4 = model.get("arpuser16");
            int total = (arp1 << 3) | (arp2 << 2) | (arp3 << 1) | (arp4);    /// Do I have these backwards?
            byte HH = (byte)((index >>> 7) & 127);
            byte PP = (byte)(index & 127);
            byte XX = (byte)(total);
            byte[] data = new byte[] { (byte)0xF0, 0x3E, 0x0E, DEV, 0x20, 0x00, HH, PP, XX, (byte)0xF7 };
            return new Object[] { data };
            }
        else if (key.equals("name"))
            {
            Object[] data = new Object[16];
            String name = model.get(key, "Init Sound V1.1 ") + "                "; 
            for(int i = 0; i < 16; i++)
                {
                int index = i + 240;
                byte HH = (byte)((index >>> 7) & 127);
                byte PP = (byte)(index & 127);
                byte XX = (byte)(name.charAt(i));
                byte[] b = new byte[] { (byte)0xF0, 0x3E, 0x0E, DEV, 0x20, 0x00, HH, PP, XX, (byte)0xF7 };
                data[i] = b;
                }
            return data;
            }
        else
            {
            int index = ((Integer)(allParametersToIndex.get(key))).intValue();
            byte HH = (byte)((index >>> 7) & 127);
            byte PP = (byte)(index & 127);
            byte XX = (byte)model.get(key);
            byte[] data = new byte[] { (byte)0xF0, 0x3E, 0x0E, DEV, 0x20, 0x00, HH, PP, XX, (byte)0xF7 };
            return new Object[] { data };
            }
        }
    
    
    
    
    public byte[] emit(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        if (tempModel == null)
            tempModel = getModel();
        byte DEV = (byte)(getID());
        byte BB = (byte) tempModel.get("bank");
        byte NN = (byte) tempModel.get("number");
        
        if (toWorkingMemory) { BB = 0x20; NN = 0x0; }
        
        byte[] bytes = new byte[256];
        
        for(int i = 0; i < 240; i++)
            {
            String key = allParameters[i];
            if (key.equals("soundformatversion"))
                {
                bytes[i] = 1;           // always
                }
            if (key.equals("-"))
                {
                bytes[i] = 0;
                }
            else if (key.equals("effecttype"))
                {
                bytes[i] = (byte)model.get(key);
                // handle XT effects specially
                if (bytes[i] == 8) // delay
                    bytes[i] = (byte)32;
                else if (bytes[i] == 9)  // pan delay
                    bytes[i] = (byte)33;
                else if (bytes[i] == 10)  // mod delay
                    bytes[i] = (byte)34;
                }
            else if (key.equals("osc1octave") || key.equals("osc2octave"))
                {
                bytes[i] = (byte)(16 + model.get(key) * 12);
                }
            else if (key.equals("lfo1sync") || key.equals("lfo2sync"))
                {
                bytes[i] = (byte)(model.get(key));
                if (bytes[i] == 2) bytes[i] = 3;  // because it's of/on/on/Clock, I dunno why
                }
            else if (key.equals("arpuser1"))
                {
                int index = 102;
                int arp1 = model.get("arpuser1");
                int arp2 = model.get("arpuser2");
                int arp3 = model.get("arpuser3");
                int arp4 = model.get("arpuser4");
                int total = (arp1 << 3) | (arp2 << 2) | (arp3 << 1) | (arp4);    /// Do I have these backwards?
                bytes[i] = (byte)(total);
                }
            else if (key.equals("arpuser5"))
                {
                int index = 103;
                int arp1 = model.get("arpuser5");
                int arp2 = model.get("arpuser6");
                int arp3 = model.get("arpuser7");
                int arp4 = model.get("arpuser8");
                int total = (arp1 << 3) | (arp2 << 2) | (arp3 << 1) | (arp4);    /// Do I have these backwards?
                bytes[i] = (byte)(total);
                }
            else if (key.equals("arpuser9"))
                {
                int index = 104;
                int arp1 = model.get("arpuser9");
                int arp2 = model.get("arpuser10");
                int arp3 = model.get("arpuser11");
                int arp4 = model.get("arpuser12");
                int total = (arp1 << 3) | (arp2 << 2) | (arp3 << 1) | (arp4);    /// Do I have these backwards?
                bytes[i] = (byte)(total);
                }
            else if (key.equals("arpuser13"))
                {
                int index = 105;
                int arp1 = model.get("arpuser13");
                int arp2 = model.get("arpuser14");
                int arp3 = model.get("arpuser15");
                int arp4 = model.get("arpuser16");
                int total = (arp1 << 3) | (arp2 << 2) | (arp3 << 1) | (arp4);    /// Do I have these backwards?
                bytes[i] = (byte)(total);
                }
            else
                {
                bytes[i] = (byte)(model.get(key));
                }
            }

        String name = model.get("name", "Init Sound V1.1 ") + "                ";
                                
        for(int i = 240; i < 256; i++)
            {
            bytes[i] = (byte)(name.charAt(i - 240));
            }
                
        byte[] full = new byte[EXPECTED_SYSEX_LENGTH];
        full[0] = (byte)0xF0;
        full[1] = 0x3E;
        full[2] = 0x0E;
        full[3] = DEV;
        full[4] = 0x10;
        full[5] = BB;
        full[6] = NN;
        System.arraycopy(bytes, 0, full, 7, bytes.length);
        full[263] = produceChecksum(BB, NN, bytes);
        full[264] = (byte)0xF7;

        return full;
        }


    /** Generate a Waldorf checksum of the data bytes */
    byte produceChecksum(byte bb, byte nn, byte[] bytes)
        {
        //      From the sysex document:
        //
        //      "Sum of all databytes truncated to 7 bits.
        //  The addition is done in 8 bit format, the result is    
        //  masked to 7 bits (00h to 7Fh). A checksum of 7Fh is
        //  always accepted as valid.
        //  IMPORTANT: the MIDI status-bytes as well as the 
        //  ID's are not used for computing the checksum."

        byte b = 0;
        for(int i = 0; i < bytes.length; i++)
            b += bytes[i];


        // Section 2.12 says that the checksum includes BB and NN.
        // But this produces a checksum error on the Microwave XT!
        // Additionally Section 1 says that the checksum is the
        // "sum of all databytes", and in its format it's clear that
        // BB and NN (location info) are NOT databytes.  Not including BB
        // and NN below produces valid dumps.

        //b += bb;
        //b += nn;  // I *think* signed will work
        
        
        b = (byte)(b & (byte)127);
        
        return b;
        }
        
    public int getPauseAfterChangePatch()
        {
        // perhaps just a smidgen?
        return 50;
        }

    public void changePatch(Model tempModel)
        {
        byte BB = (byte)tempModel.get("bank");
        byte NN = (byte)tempModel.get("number");
        try {
            // Bank change is CC 32
            tryToSendMIDI(new ShortMessage(ShortMessage.CONTROL_CHANGE, getChannelOut(), 32, BB));
            simplePause(getPauseAfterChangePatch());
            // Number change is PC
            tryToSendMIDI(new ShortMessage(ShortMessage.PROGRAM_CHANGE, getChannelOut(), NN, 0));
            }
        catch (Exception e) { e.printStackTrace(); }
        }

    public byte[] requestDump(Model tempModel)
        {
        if (tempModel == null)
            tempModel = getModel();
        byte DEV = (byte)(getID());
        byte BB = (byte)tempModel.get("bank");
        byte NN = (byte)tempModel.get("number");
        //(BB + NN)&127 is checksum
        return new byte[] { (byte)0xF0, 0x3E, 0x0E, DEV, 0x00, BB, NN, (byte)((BB + NN)&127), (byte)0xF7 };
        }
        
    public byte[] requestDump(int bank, int number, int id)
        {
        byte DEV = (byte)id;
        byte BB = (byte)bank;
        byte NN = (byte)number;
        //(BB + NN)&127 is checksum
        return new byte[] { (byte)0xF0, 0x3E, 0x0E, DEV, 0x00, BB, NN, (byte)((BB + NN)&127), (byte)0xF7 };
        }
        
    public byte[] requestCurrentDump()
        {
        byte DEV = (byte)(getID());
        //(0x75 + 0x00)&127 is checksum
        return new byte[] { (byte)0xF0, 0x3E, 0x0E, DEV, 0x00, 0x20, 0x00, (byte)((0x20 + 0x00)&127), (byte)0xF7 };
        }

    public static boolean recognize(byte[] data)
        {
        boolean v = (
            data.length == EXPECTED_SYSEX_LENGTH &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x3E &&
            data[2] == (byte)0x0E &&
            data[4] == (byte)0x10);
        return v;
        }
        


    public static final int EXPECTED_SYSEX_LENGTH = 265;        



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
            if (c < 32 || c > 127)
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

        String nm = model.get("name", "Init");
        String newnm = revisePatchName(nm);
        if (!nm.equals(newnm))
            model.set("name", newnm);
        }
        




    public void setParameterByIndex(int i, byte b)
        {
        String key = allParameters[i];
                
        if (key.equals("effecttype"))
            {
            // handle XT effects specially
            if (b == 32) // delay
                b = 8;
            else if (b == 33)  // pan delay
                b = 9;
            else if (b == 34)  // mod delay
                b = 10;
            }


        if (key.equals("-"))
            {
            // do nothing
            }
        else if (key.equals("osc1octave") || key.equals("osc2octave"))
            {
            model.set(key, (b - 16) / 12);
            }
        else if (key.equals("lfo1sync") || key.equals("lfo2sync"))
            {
            if (b == 3)
                b = 2;          // because it's of/on/on/Clock, I dunno why
            model.set(key, b);
            }
        else if (key.equals("arpuser1"))
            {
            model.set("arpuser1", (b >>> 3) & 1);                    /// Do I have these backwards?
            model.set("arpuser2", (b >>> 2) & 1);
            model.set("arpuser3", (b >>> 1) & 1);
            model.set("arpuser4", (b) & 1);
            }
        else if (key.equals("arpuser5"))
            {
            model.set("arpuser5", (b >>> 3) & 1);                    /// Do I have these backwards?
            model.set("arpuser6", (b >>> 2) & 1);
            model.set("arpuser7", (b >>> 1) & 1);
            model.set("arpuser8", (b) & 1);
            }
        else if (key.equals("arpuser9"))
            {
            model.set("arpuser9", (b >>> 3) & 1);                    /// Do I have these backwards?
            model.set("arpuser10", (b >>> 2) & 1);
            model.set("arpuser11", (b >>> 1) & 1);
            model.set("arpuser12", (b) & 1);
            }
        else if (key.equals("arpuser13"))
            {
            model.set("arpuser13", (b >>> 3) & 1);                   /// Do I have these backwards?
            model.set("arpuser14", (b >>> 2) & 1);
            model.set("arpuser15", (b >>> 1) & 1);
            model.set("arpuser16", (b) & 1);
            }
        else if (i >= 240 && i < 240 + 16)  // name
            {
            try 
                {
                String name = model.get("name", "Init Sound V1.1 ") + "                ";
                byte[] str = name.getBytes("US-ASCII");
                byte[] newstr = new byte[] { 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20 };
                System.arraycopy(str, 0, newstr, 0, 16);
                newstr[i - 240] = b;
                model.set("name", new String(newstr, "US-ASCII"));
                }
            catch (UnsupportedEncodingException e)
                {
                e.printStackTrace();
                }
            }
        else
            {
            model.set(key, b);
            }
        }

        
    public void parseParameter(byte[] data)
        {
        int index = -1;
        byte b = 0;
                
        // is it a sysex parameter change?
        if (data[0] == (byte)0xF0 &&
            data[1] == (byte)0x3E &&
            data[2] == (byte)0x0E &&            // Microwave
            // filter by ID?  Presently I'm not
            data[4] == (byte)0x20 &&
            data[5] == 0x00 &&  // only Sound Mode Edit Bufer
            data.length == 10)
            {
            int hi = (int)(data[6] & 127);
            int lo = (int)(data[7] & 127);
             
            index = (hi << 7) | (lo);
            b = (byte)(data[8] & 127);
            setParameterByIndex(index, b);
            }
        else
            {
            // we'll put CC here later
            }
        revise();
        }
        

    public int parse(byte[] data, boolean fromFile)
        {
        boolean retval = true;
        if (data[5] < 8)  // otherwise it's probably just local patch data.  Too bad they do this. :-(
            {
            model.set("bank", data[5]);
            model.set("number", data[6]);
            }
        else
            {
            retval = false;
            }

        for(int i = 0; i < 255; i++)
            {
            setParameterByIndex(i, data[i + 7]);
            }
        revise();  
        updateMode();
        return PARSE_SUCCEEDED;     
        }

    public static String getSynthName() { return "Waldorf Microwave II/XT/XTk"; }
    
    public String getPatchName(Model model) { return model.get("name", "Init Sound V1.1 "); }
    
    public byte getID() 
        { 
        try 
            { 
            byte b = (byte)(Byte.parseByte(tuple.id));
            if (b >= 0) return b;
            }
        catch (NullPointerException e) { } // expected.  Happens when tuple's not built yet
        catch (NumberFormatException e) { e.printStackTrace(); }
        return 0;
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
        int bank = model.get("bank");
        int number = model.get("number");
        
        number++;
        if (number >= 128)
            {
            bank++;
            number = 0;
            if (bank >= 2)
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
        
        int number = model.get("number") + 1;
        return BANKS[model.get("bank")] +  (number > 99 ? "" : (number > 9 ? "0" : "00")) + number;
        }
                
    }
