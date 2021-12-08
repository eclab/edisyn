/***
    Copyright 2018 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.maudiovenom;

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
   A patch editor for the M-Audio Venom
        
   @author Sean Luke
*/


//// Some open issues:

// Arp latch and octave parameters don't seem to be responded to in real time unless you've sent ONE patch upload first

// Send to Current patch always resets the patch bank / number to A 000

// Osc flags bit 6 is set by Vyzex but is unknown to me



public class MAudioVenom extends Synth
    {
    public static final byte DEFAULT_ID = (byte)0x7F;           // this seems to be what the venom returns
    
    public static final String[] BANKS = new String[] { "A", "B", "C", "D" };
    public static final String[] WRITEABLE_BANKS = new String[] { "C", "D" };
    
    public static final String[] WAVEFORMS = new String[] 
    { 
    "HP Sine", "PB Sine", "RP Sine", "SH Triangle", "MG Triangle", "RP Triangle", "RP Sawtooth", 
    "SH Sawtooth", "MG Sawtooth", "OB Sawtooth", "JX Sawtooth", "RP Sawtooth", "MS Sawtooth", "PB Square", 
    "SH Square", "MG Square", "OB Square", "JX Square", "RP Square", "MS Square", 
    "AL Pulse", "MG Pulse", "MG Sync", "SH Sync", "JX Sync", "Bit Wave 1", 
    "Bit Wave 2", "Bit Wave 3", "AL FM Wave", "DP X Wave", "RP FM Wave", 
    "AL FM Bass", "AL FM Quack", "AL FM Woody", "AL FM Science", "AL FM Organ 1", 
    "AL FM Organ 2", "AL FM Inharmonic", "MG White Noise", "08 Kit", "09 Kit", 
    "DR Kit", "FM Kit", "08 Kick 1", "08 Kick 2", "09 Kick 1", "09 Kick 2", 
    "09 Kick 3", "DR Kick", "FM Kick 1", "FM Kick 2", "08 Tom High", "08 Tom Mid", 
    "08 Tom Low", "09 Tom High", "09 Tom Mid", "09 Tom Low", "FM Tom", "08 Snare 1", 
    "08 Snare 2", "09 Snare 1", "09 Snare 2", "DR Snare", "FM Snare 1", "FM Snare 2", 
    "FM Snare 3", "08 Hat Closed", "08 Hat Open", "09 Hat Closed", "09 Hat Open", 
    "DR Hat", "FM Hat Closed", "FM Hat Open ", "08 Crash", "08 Ride", "09 Crash", 
    "09 Ride", "08 Rim Shot", "09 Rim Shot", "DR Rim Shot", "08 Hand Clap", "09 Hand Clap", 
    "08 Cowbell", "FM Cowbell", "RP Guiro", "08 Conga", "08 Clave", 
    "08 Maracas", "RP Zap 1", "RP Zap 1", "RP Zap 3", "RP Zap 4", "TB Saw", "TB Square" 
    };
    public static final String[] FILTER_TYPES = new String[] { "Off", "Lowpass 12", "Bandpass 12", "Highpass 12", "Lowpass 24", "Bandpass 24", "Highpass 24" };
    public static final String[] LFO_WAVES = new String[] { "Sine", "Sine+", "Triangle", "Sawtooth", "Square", "Sample and Hold", "Linear Sample and Hold", "Log Sample and Hold", "Exp Square", "Log Square", "Log Up Sawtooth", "Exp Up Sawtooth" };    
    public static final String[] A_MOD_WAVES = new String[] { "Sine", "Triangle", "Saw Up", "Saw Down", "Square" };    
    public static final String[] MOD_SOURCES = new String[] 
    { 
    "Off", "Envelope 1", "Envelope 2", "Envelope 3", "Envelope 1 Bipolar", "Envelope 2 Bipolar", "Envelope 3 Bipolar", 
    "LFO 1 Wide Bipolar", "LFO 2 Wide Bipolar", "LFO 3 Wide Bipolar", "LFO 1 Wide Unipolar", "LFO 2 Wide Unipolar", 
    "LFO 3 Wide Unipolar", "LFO 1 Fine Bipolar", "LFO 2 Fine Bipolar", "LFO 3 Fine Bipolar", "LFO 1 Fine Unipolar", 
    "LFO 2 Fine Unipolar", "LFO 3 Fine Unipolar", "Velocity", "- Velocity", "Keytrack", "Mod Wheel", "Pitch Bend", 
    "Aftertouch", "Expression", "- Expression", "Sustain", "- Aftertouch", "- Keytrack", "- Mod Wheel", "- Sustain"
    };    
    public static final String[] MOD_DESTINATIONS = new String[] 
    {
    "Off", "Filter Cutoff", "Pitch", "Osc 1 Pitch", "Osc 2 Pitch", "Osc 3 Pitch", "Amplitude", 
    "Filter Resonance", "Ring Mod", "External Input Level", "FM Amount", "Osc 1 Waveshaper", 
    "LFO 1 Rate", "LFO 2 Rate", "Osc Detune", "Osc 1 Level", "Osc 2 Level", "Osc 3 Level", 
    }; 
    public static final String[] EXTENDED_MOD_DESTINATIONS = new String[] 
    {
    "Off", "Filter Cutoff", "Pitch", "Osc 1 Pitch", "Osc 2 Pitch", "Osc 3 Pitch", "Amplitude", 
    "Filter Resonance", "Ring Mod", "External Input Level", "FM Amount", "Osc 1 Waveshaper", 
    "LFO 1 Rate", "LFO 2 Rate", "Osc Detune", "Osc 1 Level", "Osc 2 Level", "Osc 3 Level",
    "Mod 1 Amount", "Mod 2 Amount", "Mod 3 Amount", "Mod 4 Amount",
    "Mod 5 Amount", "Mod 6 Amount", "Mod 7 Amount", "Mod 8 Amount", 
    "Mod 9 Amount", "Mod 10 Amount", "Mod 11 Amount", "Mod 12 Amount", 
    "Mod 13 Amount", "Mod 14 Amount", "Mod 15 Amount", "Mod 16 Amount", 
    };     
    public static final String[] ARP_MODES = new String[] { "Standard", "Phrase", "Drum" };
    public static final String[] ARP_SOURCES = new String[] { "Pattern", "Single" };
    public static final String[] ARP_BANKS = new String[] { "A", "B" };
    public static final String[] AUX_FX_1_TYPES = new String[] 
    { 
    "Plate Reverb", "Room Reverb", "Hall Reverb", "Mono Echo", "Stereo Echo",
    "Mono 3/4 Echo", "Stereo 3/4 Echo", "Mono 4/4 Echo", "Stereo 4/4 Echo",
    "Mono Triplet", "Stereo Triplet", "Long Mono Delay", "Long Ping Pong"
    };
    public static final String[] AUX_FX_2_TYPES = new String[] {  "Chorus", "Flanger", "Phaser", "Delay" };
    public static final String[] TIMES = new String[] 
    {
    "1/32 N", "1/16 T", "1/16 N", "1/8 T", "1/8 N", "1/4 T", "1/8 .", "1/4 N", "1/2 T", "1/4 .", "1/2 N"
    /*
      Not enough space:
      "1/32 Note", "1/16 Note Triplet", "1/16 Note", "1/8 Note Triplet", "1/8 Note", 
      "1/4 Note Triplet", "Dotted 1/8 Note", "1/4 Note", "1/2 Note Triplet",
      "Dotted 1/4 Note", "1/2 Note"
    */
    };
    public static final String[] INSERT_FX_TYPES = new String[]  { "Off", "EQ Bandpass", "Compressor", "Auto Wah", "Distortion", "Reducer" };
    public static final String[] AUTO_WAH_TYPES = new String[] {  "Low Pass", "High Pass" };        // in that order?
    public static final String[] DISTORTION_TYPES = new String[]  { "Overdrive", "Distortion", "Fuzz" };
    public static final String[] MIX_EXTERNAL_SOURCES = new String[] { "Off", "Input Left", "Input Right", "Input Left/Right", "USB Left", "USB Right", "USB Left/Right" };
    public static final String[] GLIDE_TYPES = new String[] { "Rate", "Time" };
    public static final String[] TEMPO_SYNCS = new String[]         // for LFOs, A-Mod
    {
    "1/32 N", "1/16 T", "1/16 N", "1/8 T", "1/8 N", "1/4 T", "1/8 .", "1/4 N", "1/2 T", "1/4 .", "1/2 N", "1 N", "1 .", "2 B", "3 B", "4 B"
    /*
      Not enough space:
      "1/32 Note", "1/16 Note Triplet", "1/16 Note", "1/8 Note Triplet", "1/8 Note", 
      "1/4 Note Triplet", "Dotted 1/8 Note", "1/4 Note", "1/2 Note Triplet",
      "Dotted 1/4 Note", "1/2 Note", "Whole Note", "Dotted Whole Note", 
      "2 Bars", "3 Bars", "4 Bars"
    */
    };
    public static final String[] ARP_NOTE_ORDERS = new String[]  { "Up", "Down", "Up/Down Excl.",  "Up/Down Incl.", "Down/Up Excl.",  "Down/Up Incl.", "Chord" };
    public static final String[] NOTES = new String[] { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };

        
    public MAudioVenom()
        {
        if (parametersToIndex == null)
            parametersToIndex = new HashMap();
        for(int i = 0; i < parameters.length; i++)
            {
            parametersToIndex.put(parameters[i], Integer.valueOf(i));
            }

        /// SOUND PANEL
                
        SynthPanel soundPanel = new SynthPanel(this);
        
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        hbox.add(addGlobal(Style.COLOR_A()));
        hbox.addLast(addVoice(Style.COLOR_B()));
        vbox.add(hbox);

        hbox = new HBox();
        hbox.add(addOscillator(1, Style.COLOR_A()));
        hbox.addLast(addMixer(Style.COLOR_B()));
        vbox.add(hbox);
        hbox = new HBox();
        hbox.add(addOscillator(2, Style.COLOR_A()));
        hbox.add(addOscillator(3, Style.COLOR_A()));
        hbox.addLast(addFilter(Style.COLOR_C()));
        vbox.add(hbox);
        vbox.add(addChannel(Style.COLOR_A()));

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Sound", soundPanel);


        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        hbox = new HBox();

        hbox.add(addLFO(1, Style.COLOR_B()));
        hbox.addLast(addEnvelope(1, Style.COLOR_A()));
        vbox.add(hbox);
        hbox = new HBox();
        hbox.add(addLFO(2, Style.COLOR_B()));
        hbox.addLast(addEnvelope(2, Style.COLOR_A()));
        vbox.add(hbox);
        hbox = new HBox();
        hbox.add(addLFO(3, Style.COLOR_B()));
        hbox.addLast(addEnvelope(3, Style.COLOR_A()));
        vbox.add(hbox);
        hbox = new HBox();
        hbox.add(addAMod(Style.COLOR_C()));
        hbox.addLast(addArpeggiator(Style.COLOR_A()));
        vbox.add(hbox);

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Modulation", soundPanel);


        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        hbox = new HBox();

        vbox.add(addModulation(Style.COLOR_A()));

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Matrix", soundPanel);



        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        hbox = new HBox();

        hbox.add(addHiLoEQ(Style.COLOR_A()));
        hbox.addLast(addMasterEQ(Style.COLOR_C()));
        vbox.add(hbox);
        vbox.add(addAux1(Style.COLOR_B()));
        vbox.add(addAux2(Style.COLOR_B()));
        vbox.add(addInsert(Style.COLOR_B()));

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Effects", soundPanel);
                        
        model.set("name", "Untitled");
        model.set("number", 0);
        model.set("bank", 0);
        
        loadDefaults();
        }
                
    public String getDefaultResourceFileName() { return "MAudioVenom.init"; }
    public String getHTMLResourceFileName() { return "MAudioVenom.html"; }

    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        JComboBox bank = new JComboBox(writing? WRITEABLE_BANKS : BANKS);
        bank.setEditable(false);
        bank.setMaximumRowCount(32);
        int b = model.get("bank");
        if (writing)
            {
            b -= 2;
            if (b < 0) b = 0;
            }
        bank.setSelectedIndex(b);
                
        JTextField number = new SelectedTextField("" + (model.get("number")), 3);
                
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
                                
            change.set("bank", bank.getSelectedIndex() + (writing ? 2 : 0));
            change.set("number", n);
                        
            return true;
            }
        }
                                                                      
        
    /** Add the global patch category (name, id, number, etc.) */
    public JComponent addNameGlobal(Color color)
        {
        Category globalCategory = new Category(this, "M-Audio Venom", color);
        //globalCategory.makeUnresettable();
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        final PatchDisplay pd = new PatchDisplay(this, 3);
        comp = pd;
        vbox.add(comp);
                
        comp = new StringComponent("Patch Name", this, "name", 10, "Name must be up to 10 characters.")
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
        hbox.add(vbox);
        hbox.add(Strut.makeHorizontalStrut(30));
                
        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }


    // GLIDE MODE is either 0 or it is 127
    // we skip SaveBank and SavePatch

    public JComponent addGlobal(Color color)
        {
        Category category = new Category(this, "General", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        vbox = new VBox();
        comp = new CheckBox("Glide", this, "glidemode");
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Glide", this, "glidetime", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Time/Rate");
        hbox.add(comp);
        
        comp = new LabelledDial("Bend", this, "bendrange", color, 0, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Range");
        hbox.add(comp);
        
        comp = new LabelledDial("Oscillator", this, "startmod", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Start Mod");
        hbox.add(comp);
        
        comp = new LabelledDial("Oscillator", this, "oscdrift", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Drift");
        hbox.add(comp);

        comp = new LabelledDial("Volume", this, "progvolume", color, 0, 127);
        hbox.add(comp);
        

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
                
                
    public JComponent addEnvelope(int env, Color color)
        {
        Category category = new Category(this, "Envelope " + env + " (" + (env == 1 ? "Amplifier)" : (env == 2 ? "Filter Cutoff)" : "Pitch)")), color);
        //        category.makePasteable("env" + env);
        category.makePasteable("env");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        vbox = new VBox();

        comp = new LabelledDial("Attack", this, "env" + env + "attack", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Hold", this, "env" + env + "hold", color, 0, 127);
        hbox.add(comp);
        
        comp = new LabelledDial("Decay", this, "env" + env + "decay", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Sustain", this, "env" + env + "sustain", color, 0, 127);
        hbox.add(comp);

        if (env == 1)
            {
            comp = new LabelledDial("Release", this, "env" + env + "release", color, 0, 126);
            }
        else
            {
            comp = new LabelledDial("Release", this, "env" + env + "release", color, 0, 127)
                {
                public String map(int value)
                    {
                    if (value == 127)
                        return "Hold";
                    else
                        return "" + value;
                    }
                };
            }
        hbox.add(comp);

        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "env" + env + "attack", "env" + env + "hold", "env" + env + "decay", null, "env" + env + "release" },
            new String[] { null, null, null, "env" + env + "sustain", "env" + env + "sustain", null },
            new double[] { 0, 0.2/127.0, 0.2/127.0, 0.2/127.0, 0.2, 0.2/127.0 },
            new double[] { 0, 1.0, 1.0, 1.0/127.0, 1.0/127.0, 0 } 
            );
        hbox.addLast(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
                

    // Flags appear to be:
    // 0    ??
    // 1    OSC 2 Sync
    // 2    OSC 3 Sync
    // 3    OSC 1 Keytrack OFF
    // 4    OSC 2 Keytrack OFF
    // 5    OSC 3 Keytrack Off
    // 6    ??

    public JComponent addOscillator(int osc, Color color)
        {
        Category category = new Category(this, "Oscillator " + osc, color);
        //        category.makePasteable("osc" + osc);
        category.makePasteable("osc");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();

        VBox vbox = new VBox();
        params = WAVEFORMS;
        comp = new Chooser("Waveform", this, "osc" + osc + "waveform", params);
        vbox.add(comp);

        comp = new CheckBox("Keytrack", this, "osc" + osc + "keytrack", true);  // flipped
        vbox.add(comp);
                
        if (osc == 1)
            {
            comp = new CheckBox("Waveshape", this, "waveshape");
            vbox.add(comp);
            }
                                
        if (osc == 2 || osc == 3)
            {
            comp = new CheckBox("Osc 1 Sync", this, "osc" + osc + "sync");
            vbox.add(comp);
            }
        
        hbox.add(vbox);
                
        comp = new LabelledDial("Coarse", this, "osc" + osc + "coarsetune", color, 0, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Tune");
        hbox.add(comp);

        comp = new LabelledDial("Fine", this, "osc" + osc + "finetune", color, 0, 127)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                if (value <= 64) return String.format("%2.1f", ((value - 64) / 64.0 * 50.0));
                else return String.format("%2.1f", ((value - 64) / 63.0) * 50.0);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Tune");
        hbox.add(comp);
        
        if (osc == 1)
            {
            comp = new LabelledDial("Osc 3 FM", this, "fmlevel", color, 0, 127);
            hbox.add(comp);

            comp = new LabelledDial("Waveshape", this, "waveshapewidth", color, 0, 127);
            ((LabelledDial)comp).addAdditionalLabel("Width");
            hbox.add(comp);
            }

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
                

    public JComponent addMixer(Color color)
        {
        Category category = new Category(this, "Mixer", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();

        VBox vbox = new VBox();
        params = MIX_EXTERNAL_SOURCES;
        comp = new Chooser("External Source", this, "extinsource", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Osc 1", this, "osc1volume", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);

        comp = new LabelledDial("Osc 2", this, "osc2volume", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);

        comp = new LabelledDial("Osc 3", this, "osc3volume", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);

        comp = new LabelledDial("Ring Mod", this, "ringmod", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);

        comp = new LabelledDial("Ext. In", this, "extinvolume", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);
        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }



    /// NOTE THAT CUTOFF IS 16-BIT          "cutoff high" "cutoff low"

    public JComponent addFilter(Color color)
        {
        Category category = new Category(this, "Filter", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();

        VBox vbox = new VBox();
        params = FILTER_TYPES;
        comp = new Chooser("Type", this, "filtertype", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Pre-Filter", this, "boost", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Boost");
        hbox.add(comp);

        comp = new LabelledDial("Cutoff", this, "cutoff", color, 0, 16383);
        hbox.add(comp);

        comp = new LabelledDial("Resonance", this, "resonance", color, 0, 127);
        hbox.add(comp);
        
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
        final HBox hbox = new HBox();

        VBox vbox = new VBox();
        params = LFO_WAVES;
        comp = new Chooser("Waveform", this, "lfo" + lfo + "waveform", params);
        vbox.add(comp);
        hbox.add(vbox);

        /// The manual says this only goes to 123, but in fact it goes to 127
        comp = new LabelledDial("Rate", this, "lfo" + lfo + "rate", color, 0, 127)
            {
            public String map(int value)
                {
                if (value < 111) return "" + value;
                else return TEMPO_SYNCS[value - 111];
                }
            };
        hbox.add(comp);
        model.setMetricMax("lfo" + lfo + "rate", 110);
                
        comp = new LabelledDial("Delay", this, "lfo" + lfo + "delay", color, 0, 127);
        hbox.add(comp);
                
        comp = new LabelledDial("Attack", this, "lfo" + lfo + "attack", color, 0, 127);
        hbox.add(comp);
                
        comp = new LabelledDial("Phase", this, "lfo" + lfo + "startphase", color, 0, 127);
        hbox.add(comp);
                
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addAMod(Color color)
        {
        Category category = new Category(this, "A-Mod (Tremolo)", color);
              
        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();

        VBox vbox = new VBox();
        params = A_MOD_WAVES;
        comp = new Chooser("Temolo Waveform", this, "tremolowaveform", params);
        vbox.add(comp);
        hbox.add(vbox);

        /// The manual says this only goes to 123, but in fact it goes to 127
        comp = new LabelledDial("Tremolo", this, "tremolorate", color, 0, 127)
            {
            public String map(int value)
                {
                if (value < 111) return "" + value;
                else return TEMPO_SYNCS[value - 111];
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Rate");
        hbox.add(comp);
        model.setMetricMax("tremolorate", 110);
                
        comp = new LabelledDial("Tremolo", this, "tremolovoldepth", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);
                
        comp = new LabelledDial("Tremolo", this, "tremolopandepth", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Auto Pan");
        hbox.add(comp);
                
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    // Docs say sources go 0...30, but actually they go 0...32
    // Docs say destinations go 0...79, but actually they go 1...17.  I believe "0" is probably "off", but Vyzex doesn't allow that.  I'm gonna try it.

    public JComponent addModulation(Color color)
        {
        Category category = new Category(this, "Modulation Matrix", color);
              
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox outer = new VBox();
        
        for(int mod = 1; mod <= 16; mod++)              // note <=
            {
            VBox vbox = new VBox();
            params = MOD_SOURCES;
            comp = new Chooser("Source " + mod, this, "mod" + mod + "source", params);
            vbox.add(comp);

            params = (mod > 2 ? EXTENDED_MOD_DESTINATIONS : MOD_DESTINATIONS);
            comp = new Chooser("Destination " + mod, this, "mod" + mod + "destination", params);
            vbox.add(comp);

            hbox.add(vbox);

            comp = new LabelledDial("Amount " + mod, this, "mod" + mod + "scaling", color, 0, 127)
                {
                public boolean isSymmetric() { return true; }
                public String map(int value)
                    {
                    if (value <= 64) return "" + (((value - 64) * 100) / 64) + "%";
                    else return "" + (((value - 64) * 100) / 63) + "%";
                    }
                };
            hbox.add(comp);
                        
            if (mod == 4 || mod == 8 || mod == 12 || mod == 16)
                {
                outer.add(hbox);
                if (mod == 4 || mod == 8 || mod == 12)
                    outer.add(Strut.makeVerticalStrut(20));
                hbox = new HBox();
                }
            }
                
        category.add(outer, BorderLayout.CENTER);
        return category;
        }


    /// voicemode is 0 vs 1
    /// unisonmode is 0 vs 7F

    public JComponent addVoice(Color color)
        {
        Category category = new Category(this, "Voice", color);
              
        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();

        VBox vbox = new VBox();
        comp = new CheckBox("Polyphonic", this, "voicemode");
        ((CheckBox)comp).addToWidth(1);
        vbox.add(comp);
        comp = new CheckBox("Unison", this, "unisonmode");
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Unison", this, "unisoncount", color, 2, 12);
        ((LabelledDial)comp).addAdditionalLabel("Voices");
        hbox.add(comp);
                
        comp = new LabelledDial("Unison", this, "unisondetune", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Detune");
        hbox.add(comp);
                
        comp = new LabelledDial("Coarse", this, "coarsetune", color, 0, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Tune");
        hbox.add(comp);

        comp = new LabelledDial("Fine", this, "finetune", color, 0, 127)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                if (value <= 64) return String.format("%2.1f", ((value - 64) / 64.0 * 50.0));
                else return String.format("%2.1f", ((value - 64) / 63.0) * 50.0);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Tune");
        hbox.add(comp);
                
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addChannel(Color color)
        {
        Category category = new Category(this, "Channel", color);
              
        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();

        VBox vbox = new VBox();
                
        comp = new CheckBox("Enable FX1", this, "aux1mode");
        ((CheckBox)comp).addToWidth(1);
        vbox.add(comp);

        comp = new CheckBox("Enable FX2", this, "aux2mode");
        ((CheckBox)comp).addToWidth(1);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Volume", this, "channelvolume", color, 0, 127);
        hbox.add(comp);
                
        comp = new LabelledDial("Pan", this, "channelpan", color, 0, 127)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                if (value < 64) return "< " + (0 - (((value - 64) * 100) / 64));
                else if (value > 64) return "" + (((value - 64) * 100) / 63) + " >";
                else return "--";
                }
            };
        hbox.add(comp);
                
        comp = new LabelledDial("Direct", this, "channeldirect", color, 0, 127);
        hbox.add(comp);
                
        comp = new LabelledDial("Aux 1", this, "channelaux1send", color, 0, 127);
        hbox.add(comp);
                
        comp = new LabelledDial("Aux 2", this, "channelaux2send", color, 0, 127);
        hbox.add(comp);
                
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addHiLoEQ(Color color)
        {
        Category category = new Category(this, "Equalization", color);
              
        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();

        comp = new LabelledDial("Low", this, "hiloeqlowfreq", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Freq");
        hbox.add(comp);
                
        comp = new LabelledDial("Low", this, "hiloeqlowgain", color, 0, 127)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                if (value <= 64) return "" + (((value - 64) * 100) / 64) + "%";
                else return "" + (((value - 64) * 100) / 63) + "%";
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Gain");
        hbox.add(comp);

        comp = new LabelledDial("High", this, "hiloeqhighfreq", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Freq");
        hbox.add(comp);

        comp = new LabelledDial("High", this, "hiloeqhighgain", color, 0, 127)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                if (value <= 64) return "" + (((value - 64) * 100) / 64) + "%";
                else return "" + (((value - 64) * 100) / 63) + "%";
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Gain");
        hbox.add(comp);
                
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    /// Docs say Reducer Bit Depth goes 0...12 but it actually goes 0...127

    public JComponent addInsert(Color color)
        {
        Category category = new Category(this, "Insert FX", color);
              
        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();

        final HBox[] insert = new HBox[6];

        
        // OFF
        insert[0] = new HBox();


        // EQ Bandpass
        insert[1] = new HBox();         

        comp = new LabelledDial("Mid", this, "bandpassmidfreq", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Freq");
        insert[1].add(comp);
                
        comp = new LabelledDial("Mid", this, "bandpassmidgain", color, 0, 127)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                if (value <= 64) return "" + (((value - 64) * 100) / 64) + "%";
                else return "" + (((value - 64) * 100) / 63) + "%";
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Gain");
        insert[1].add(comp);

        comp = new LabelledDial("Mid", this, "bandpassmidq", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Q");
        insert[1].add(comp);


        // Compressor
        insert[2] = new HBox();         

        comp = new LabelledDial("Attack", this, "compressorattack", color, 0, 127);
        insert[2].add(comp);
                
        comp = new LabelledDial("Release", this, "compressorrelease", color, 0, 127);
        insert[2].add(comp);
                
        comp = new LabelledDial("Threshold", this, "compressorthreshold", color, 0, 127);
        insert[2].add(comp);
                
        comp = new LabelledDial("Ratio", this, "compressorratio", color, 0, 127);
        insert[2].add(comp);
                
        comp = new LabelledDial("Makeup", this, "compressorgain", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Gain");
        insert[2].add(comp);


        // Auto Wah
        insert[3] = new HBox();         

        VBox vbox = new VBox();
        params = AUTO_WAH_TYPES;
        comp = new Chooser("Wah Wah Type", this, "autowahtype", params);
        vbox.add(comp);
        insert[3] .add(vbox);

        comp = new LabelledDial("Cutoff", this, "autowahcutoff", color, 0, 127);
        insert[3].add(comp);
                
        comp = new LabelledDial("Resonance", this, "autowahresonance", color, 0, 127);
        insert[3].add(comp);
                
        comp = new LabelledDial("Sensitivity", this, "autowahsensitivity", color, 0, 127);
        insert[3].add(comp);
        
        // shared with Compressor
        comp = new LabelledDial("Attack", this, "compressorattack", color, 0, 127);
        insert[3].add(comp);
                
        // shared with Compressor
        comp = new LabelledDial("Release", this, "compressorrelease", color, 0, 127);
        insert[3].add(comp);
                
                

        // Distortion
        insert[4] = new HBox();         

        vbox = new VBox();
        params = DISTORTION_TYPES;
        comp = new Chooser("Distortion Type", this, "distortiontype", params);
        vbox.add(comp);
        insert[4].add(vbox);

        comp = new LabelledDial("Depth", this, "distortiondepth", color, 0, 127);
        insert[4].add(comp);
                
        comp = new LabelledDial("Pre", this, "distortionpregain", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Gain");
        insert[4].add(comp);
                
        comp = new LabelledDial("Post", this, "distortionpostgain", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Gain");
        insert[4].add(comp);
                
        comp = new LabelledDial("High", this, "distortionhighcutoff", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Cutoff");
        insert[4].add(comp);
                

        // Reducer
        insert[5] = new HBox();         

        comp = new LabelledDial("Bit", this, "reducerbitdepth", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Depth");
        insert[5].add(comp);
                
        comp = new LabelledDial("Sample", this, "reducersamplerate", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Rate");
        insert[5].add(comp);
                
                
        /// CHOOSER
        
        final HBox fx = new HBox();

        vbox = new VBox();
        params = INSERT_FX_TYPES;
        comp = new Chooser("Type", this, "channelfxtype", params)       // I *think* this is insert
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                fx.removeLast();
                int channelfxtype = model.get("channelfxtype", 0);
                if (channelfxtype >= 0 && channelfxtype < insert.length)
                    fx.addLast(insert[channelfxtype]);
                else Synth.handleException(new Throwable("Invalid channel fx type: " + channelfxtype));
                fx.revalidate();
                fx.repaint();
                }
            };              
        vbox.add(comp);
        hbox.add(vbox);
        fx.add(insert[0]);              // empty
        hbox.add(fx);
                
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }





    // aux1mode (enable) is either 0 or 127
    // Docs say aux1type is 0 or 1, but it's actually 0...12 (0 is NOT off)

    public JComponent addAux1(Color color)
        {
        Category category = new Category(this, "Aux 1 FX", color);
              
        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();

        final HBox reverb = new HBox();
                
        comp = new LabelledDial("Time", this, "aux1time", color, 0, 127);
        reverb.add(comp);
                
        comp = new LabelledDial("Tone", this, "aux1tonegain", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Gain");
        reverb.add(comp);
                
        comp = new LabelledDial("Tone", this, "aux1tonefreq", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Frequency");
        reverb.add(comp);

        comp = new LabelledDial("Gate", this, "aux1gatethresh", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Threshold");
        reverb.add(comp);
        
        final HBox echo = new HBox();
                
        comp = new LabelledDial("Feedback", this, "aux1feedback", color, 0, 127);
        echo.add(comp);
                
        VBox vbox = new VBox();
        params = AUX_FX_1_TYPES;
        comp = new Chooser("Type", this, "aux1type", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                hbox.removeLast();
                if (model.get("aux1type", 0) <= 2)      // reverb
                    {
                    hbox.addLast(reverb);
                    }
                else    // echo
                    {
                    hbox.addLast(echo);
                    }
                hbox.revalidate();
                hbox.repaint();
                }
            };              
        vbox.add(comp);

        comp = new CheckBox("Enable", this, "aux1mode");
        vbox.add(comp);

        hbox.add(vbox);
        hbox.addLast(reverb);
        
        /// The manual says this only goes to 123, but in fact it goes to 127
        comp = new LabelledDial("Gate /", this, "aux1gatedelaytime", color, 0, 127)
            {
            public String map(int value)
                {
                if (value < 111) return "" + value;
                else return TIMES[value - 111];
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Delay Time");
        hbox.add(comp);
        model.setMetricMax("aux1gatedelaytime", 110);
                
        comp = new LabelledDial("Depth", this, "aux1depth", color, 0, 127);
        hbox.add(comp);
                
        comp = new LabelledDial("Pre HP", this, "aux1prehp", color, 0, 127);
        hbox.add(comp);
                
        comp = new LabelledDial("Pre Delay", this, "aux1predelay", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("High", this, "aux1highdamp", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Damp");
        hbox.add(comp);
                
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }



    // Docs say aux1type is 0...4, but it's actually 0...3 (0 is NOT off)

    public JComponent addAux2(Color color)
        {
        Category category = new Category(this, "Aux 2 FX", color);
              
        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();

        final HBox chorus = new HBox();
                
        comp = new LabelledDial("High", this, "aux2highdamp", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Damp");
        chorus.add(comp);
                
        comp = new LabelledDial("Time", this, "aux2time", color, 0, 127);
        chorus.add(comp);
                
        VBox vbox = new VBox();
        params = AUX_FX_2_TYPES;
        comp = new Chooser("Type", this, "aux2type", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                hbox.removeLast();
                if (model.get("aux2type", 0) != 2)      // anything but phaser
                    {
                    hbox.addLast(chorus);
                    }
                hbox.revalidate();
                hbox.repaint();
                }
            };              
        vbox.add(comp);

        comp = new CheckBox("Enable", this, "aux2mode");
        vbox.add(comp);

        hbox.add(vbox);
        hbox.addLast(chorus);
        
        comp = new LabelledDial("Send", this, "aux2toaux1", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("To Aux 1");
        hbox.add(comp);
                
        comp = new LabelledDial("Depth", this, "aux2depth", color, 0, 127);
        hbox.add(comp);
                
        comp = new LabelledDial("Pre LP", this, "aux2prelp", color, 0, 127);
        hbox.add(comp);
                
        comp = new LabelledDial("Pre HP", this, "aux2prehp", color, 0, 127);
        hbox.add(comp);
                
        comp = new LabelledDial("Feedback", this, "aux2feedback", color, 0, 127);
        hbox.add(comp);

        // The manual says this goes to 123.  An Vyzex has no MIDI clock sync for this.
        // But quite a number of preset patches go clear to 127.  Discussion with Jan Bote
        // makes it more likely that this LFO is just like the others -- it goes 0-110
        // for basic rates, then 110-127 for MIDI synced rates.  So that's what I'm doing.
        comp = new LabelledDial("LFO", this, "aux2lforate", color, 0, 127)
            {
            public String map(int value)
                {
                if (value < 111) return "" + value;
                else return TEMPO_SYNCS[value - 111];
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Rate");
        hbox.add(comp);
        model.setMetricMax("aux2lforate", 110);

        comp = new LabelledDial("LFO", this, "aux2lfodepth", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Depth");
        hbox.add(comp);
                
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addMasterEQ(Color color)
        {
        Category category = new Category(this, "Master EQ", color);
              
        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();

        comp = new LabelledDial("Low", this, "mastereqlowfreq", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Freq");
        hbox.add(comp);

        comp = new LabelledDial("Low", this, "mastereqlowgain", color, 0, 127)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                if (value <= 64) return "" + (((value - 64) * 100) / 64) + "%";
                else return "" + (((value - 64) * 100) / 63) + "%";
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Gain");
        hbox.add(comp);

        comp = new LabelledDial("Mid", this, "mastereqmidfreq", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Freq");
        hbox.add(comp);

        comp = new LabelledDial("Mid", this, "mastereqmidgain", color, 0, 127)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                if (value <= 64) return "" + (((value - 64) * 100) / 64) + "%";
                else return "" + (((value - 64) * 100) / 63) + "%";
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Gain");
        hbox.add(comp);

        comp = new LabelledDial("High", this, "mastereqhighfreq", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Freq");
        hbox.add(comp);

        comp = new LabelledDial("High", this, "mastereqhighgain", color, 0, 127)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                if (value <= 64) return "" + (((value - 64) * 100) / 64) + "%";
                else return "" + (((value - 64) * 100) / 63) + "%";
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Gain");
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    // arpsrc is 0=pattern, *127*=single

    // arpbipolar is 0 or 127

    // arplatchkeys is 0 or 127

    // docs say octave is -4...4, but it is 60...68

    public JComponent addArpeggiator(Color color)
        {
        final Category category = new Category(this, "Arpeggiator", color);
              
        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();
        
        final HBox out = new HBox();
        final HBox in = new HBox();
        final VBox outv = new VBox();
        final VBox inv = new VBox();

        final LabelledDial octaverange = new LabelledDial("Octave", this, "arpoctaverange", color, 60, 68, 64);
        ((LabelledDial)octaverange).addAdditionalLabel("Range");

        final LabelledDial rootnote = new LabelledDial("Root", this, "arprootnote", color, 0, 127)
            {
            public String map(int value)
                {
                return NOTES[value % 12] + (value / 12 - 2);                    
                }
            };
        ((LabelledDial)rootnote).addAdditionalLabel("Note");

        final VBox noteorder_bipolar = new VBox();
        params = ARP_NOTE_ORDERS;
        comp = new Chooser("Note Order", this, "arpnoteorder", params);
        noteorder_bipolar.add(comp);

        comp = new CheckBox("Bipolar", this, "arpbipolar");
        noteorder_bipolar.add(comp);

        VBox vbox = new VBox();
        params = ARP_SOURCES;
        comp = new Chooser("Source", this, "arpsrc", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);

                //if (model.get(key) == 1)                // SINGLE
                    {
                    int bank = model.get("arpbank");
                    int pattern = model.get("arppattern");
                    if (bank >= 0 && pattern >= 0)
                        {
                        category.setName("Arpeggiator (" + DEFAULT_ARP_PATTERN_NAMES[bank][pattern] + ")");
                        }
                    }
                /*
                  else
                  {
                  category.setName("Arpeggiator");
                  }
                */

                out.removeLast();
                outv.removeLast();
                if (model.get(key) == 1)        // SINGLE
                    {
                    out.addLast(in);
                    outv.addLast(inv);
                    }
                out.revalidate();
                out.repaint();
                outv.revalidate();
                outv.repaint();
                }
            };
        vbox.add(comp);

        comp = new CheckBox("Enable", this, "arpenable");
        vbox.add(comp);
        hbox.add(vbox);

        vbox = new VBox();

        HBox inout = new HBox();

        params = ARP_BANKS;
        comp = new Chooser("Bank", this, "arpbank", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                int bank = model.get("arpbank");
                int pattern = model.get("arppattern");
                if (bank >= 0  && bank < DEFAULT_ARP_PATTERN_NAMES.length && pattern >= 0 && pattern < DEFAULT_ARP_PATTERN_NAMES[0].length)
                    {
                    category.setName("Arpeggiator (" + DEFAULT_ARP_PATTERN_NAMES[bank][pattern] + ")");
                    }
                // else Synth.handleException(new Throwable("Invalid bank or pattern.  Bank: " + bank + " Pattern: " + pattern));
                }
            };
        vbox.add(comp);
        //inv.add(comp);
        inout.add(Strut.makeStrut(inv, true, false));
        inout.addLast(outv);
        //vbox.add(inout);
       
        comp = new PushButton("Show Arp")
            {
            public void perform()
                {
                final MAudioVenomArp synth = new MAudioVenomArp();
                if (tuple != null)
                    synth.tuple = new Midi.Tuple(tuple, synth.buildInReceiver(), synth.buildKeyReceiver(), synth.buildKey2Receiver());
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
                                Model tempModel = buildModel();
                                tempModel.set("bank", MAudioVenom.this.model.get("arpbank"));
                                tempModel.set("number", MAudioVenom.this.model.get("arppattern"));
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
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Pattern", this, "arppattern", color, 0, 127)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                //if (model.get("arpsrc") == 1)           // SINGLE
                    {
                    int bank = model.get("arpbank");
                    int pattern = model.get("arppattern");
                    if (bank >= 0  && bank < DEFAULT_ARP_PATTERN_NAMES.length && pattern >= 0 && pattern < DEFAULT_ARP_PATTERN_NAMES[0].length)
                        category.setName("Arpeggiator (" + DEFAULT_ARP_PATTERN_NAMES[bank][pattern] + ")");
                    else Synth.handleException(new Throwable("Invalid bank or pattern.  Bank: " + bank + " Pattern: " + pattern));
                    }
                /*
                  else
                  {
                  category.setName("Arpeggiator");
                  }
                */
                }
            };
        hbox.add(comp);

        hbox.addLast(out);
        //out.addLast(in);

        vbox = new VBox();        
        params = ARP_MODES;
        comp = new Chooser("Mode", this, "arpmode", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);

                in.remove(rootnote);
                in.remove(octaverange);
                in.remove(noteorder_bipolar);

                int val = model.get("arpmode", 0);
                if (val == 0)           // standard
                    {
                    in.add(octaverange);
                    in.add(noteorder_bipolar);
                    }
                else if (val == 1)      // phrase
                    {
                    in.add(octaverange);
                    in.add(rootnote);
                    }
                else                            // drum 
                    {
                    // addnothing
                    }
                        
                in.revalidate();
                in.repaint();
                }
            };
        vbox.add(comp);

        comp = new CheckBox("Latch", this, "arplatchkeys");
        vbox.add(comp);
        in.add(vbox);

        in.add(octaverange);
        in.add(noteorder_bipolar);
        hbox.add(Strut.makeStrut(octaverange, true, false));            //so we keep the same height

        //category.setName("Arpeggiator");
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    /*
      public byte getID() 
      { 
      try 
      { 
      byte b = (byte)(Byte.parseByte(tuple.id));
      if (b >= 0 && b < 128) return b;
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
      if (b >= 0 && b < 128) return "" + b;
      } 
      catch (NumberFormatException e) { }             // expected
      return "" + getID();
      }
    */

    public Object[] emitAll(String key)
        {
        if (key.equals("number")) return new Object[0];  // this is not emittable
        if (key.equals("bank")) return new Object[0];
        
        if (key.equals("name"))
            {
            String name = model.get(key, "") + "            ";
            Object[] ret = new Object[10];
            for(int i = 0; i < 10; i++)
                {
                int val = (int)(name.charAt(i));
                byte valMSB = (byte)(val >>> 7);
                byte valLSB = (byte)(val & 127);
                byte paramMSB = (byte)0x01;
                byte paramLSB = (byte)(0x3C + i);               // PatchName[0...9], p. 100-101

                byte[] data = new byte[] 
                    { 
                    (byte)0xF0, 
                    (byte)0x00,             // M-Audio
                    (byte)0x01, 
                    (byte)0x05, 
                    (byte)0x21,                     // Venom 
                    (byte)DEFAULT_ID, //(byte)getID(), 
                    (byte)0x02,                     // Write Data Dump 
                    (byte)0x09,             // Edit Single Param
                    paramMSB,
                    paramLSB,
                    valMSB,                         // This is useless because the data is always 7-bit
                    valLSB,
                    (byte)0xF7
                    };

                ret[i] = data;
                }
            return ret;
            }
        else if (key.equals("cutoff"))
            {
            // Map to cutoffhigh and cutofflow
            int val = (model.get("cutoff"));
            byte valMSB = (byte)(val >>> 7);
            byte valLSB = (byte)(val & 127);

            byte paramMSB = (byte)0x00;
            byte paramLSB = (byte)0x6A;         // Filter.Cutoff High (p. 99)
            
            byte[] data1 = new byte[] 
                { 
                (byte)0xF0, 
                (byte)0x00,             // M-Audio
                (byte)0x01, 
                (byte)0x05, 
                (byte)0x21,                     // Venom 
                (byte)DEFAULT_ID,               // (byte)getID(), 
                (byte)0x02,                     // Write Data Dump 
                (byte)0x09,             // Edit Single Param
                paramMSB,
                paramLSB,
                0,                              // This is useless because the data is always 7-bit
                valMSB,
                (byte)0xF7
                };

            paramMSB = (byte)0x00;
            paramLSB = (byte)0x6B;             // Filter.Cutoff Low (p. 99)
            
            byte[] data2 = new byte[] 
                { 
                (byte)0xF0, 
                (byte)0x00,             // M-Audio
                (byte)0x01, 
                (byte)0x05, 
                (byte)0x21,                     // Venom 
                (byte)DEFAULT_ID, //(byte)getID(), 
                (byte)0x02,                     // Write Data Dump 
                (byte)0x09,             // Edit Single Param
                paramMSB,
                paramLSB,
                0,                              // This is useless because the data is always 7-bit
                valLSB,
                (byte)0xF7
                };
            return new Object[] { data1, data2 };
            }
        else if (key.equals("osc2sync") || key.equals("osc3sync") || key.equals("osc1keytrack") || key.equals("osc2keytrack") || key.equals("osc3keytrack") || key.equals("waveshape"))
            {
            // The documentation has a "**" next to oscflags but mistakenly doesn't indicate what they are.  They are:
            //
            // 0    Waveshape ON
            // 1    OSC 2 Sync ON
            // 2    OSC 3 Sync ON
            // 3    OSC 1 Keytrack OFF  [inverted]
            // 4    OSC 2 Keytrack OFF  [inverted]
            // 5    OSC 3 Keytrack OFF  [inverted]
            // 6    UNKNOWN but always set ON               

            byte paramMSB = (byte)0x00;
            byte paramLSB = (byte)0x18;         // OscMisc.OscFlags (p. 97)
            int val = 
                (model.get("waveshape") << 0) |
                (model.get("osc2sync") << 1) |
                (model.get("osc3sync") << 2) |
                (model.get("osc1keytrack") << 3) |              // Already flipped...
                (model.get("osc2keytrack") << 4) |
                (model.get("osc3keytrack") << 5) |
                0x40;                                                           // set bit 6 to ON
                            
            byte valMSB = (byte)(val >>> 7);
            byte valLSB = (byte)(val & 127);
            
            byte[] data = new byte[] 
                { 
                (byte)0xF0, 
                (byte)0x00,             // M-Audio
                (byte)0x01, 
                (byte)0x05, 
                (byte)0x21,                     // Venom 
                (byte)DEFAULT_ID, //(byte)getID(), 
                (byte)0x02,                     // Write Data Dump 
                (byte)0x09,             // Edit Single Param
                paramMSB,
                paramLSB,
                valMSB,                         // This is useless because the data is always 7-bit
                valLSB,
                (byte)0xF7
                };
            return new Object[] { data };
            }
        else
            {
            int val = model.get(key);
                
            if (key.equals("glidemode") || key.equals("unisonmode") || key.equals("aux1mode") || 
                key.equals("aux2mode") || key.equals("apsrc") || key.equals("arpbipolar") || 
                key.equals("arplatchkeys") ||key.equals("arpenable"))
                {
                if (val == 1) 
                    val = 127;
                }
            else if (key.endsWith("destination"))  // mod destination
                {
                if (val >= 18)
                    {
                    val = (val - 18 + 64);
                    }
                }

            int param = ((Integer)(parametersToIndex.get(key))).intValue();
            byte paramMSB = (byte)(param >>> 7);
            byte paramLSB = (byte)(param & 127);
            byte valMSB = (byte)(val >>> 7);
            byte valLSB = (byte)(val & 127);
            
            byte[] data = new byte[] 
                { 
                (byte)0xF0, 
                (byte)0x00,             // M-Audio
                (byte)0x01, 
                (byte)0x05, 
                (byte)0x21,                     // Venom 
                (byte)DEFAULT_ID, //(byte)getID(), 
                (byte)0x02,                     // Write Data Dump 
                (byte)0x09,             // Edit Single Param
                paramMSB,
                paramLSB,
                valMSB,                         // This is useless because the data is always 7-bit
                valLSB,
                (byte)0xF7
                };
            return new Object[] { data };
            }
                
        }
    
    
    //// These are a lot like the Prophet '08 7<-->8 bit conversions.
    ////
    //// The Venom manual describes conversion from 8 bit to 7 bit as
    //// taking 7 bytes at a time, removing the top bits and putting them
    //// in the first byte, producing 8 7-bit bytes as a result.  That's all
    //// fine and good, except that the data doesn't come in 7 byte chunks.
    //// It's 198 bytes long, which isn't a multiple of 7.  It appears that
    //// the answer is to take the remainder (2 bytes), strip off the high bits
    //// and make 3 more 7-bit bytes.


    // converts up to but not including 'to'
    byte[] convertTo8Bit(byte[] data, int from, int to)
        {
        // How big?
        int main = ((to - from) / 8) * 8;
        int remainder = (to - from) - main;
        int size = ((to - from) / 8) * 7 + (remainder - 1);
        
        byte[] newd = new byte[size];
        
        int j = 0;
        for(int i = from; i < to; i += 8)
            {            
            for(int x = 0; x < 7; x++)
                {
                if (j + x < newd.length)
                    newd[j + x] = (byte)(data[i + x + 1] | (byte)(((data[i] >>> x) & 0x1) << 7));
                }
            j += 7;
            }
        return newd;
        }
        
        
    // converts all bytes
    byte[] convertTo7Bit(byte[] data)
        {
        // How big?
        int main = ((data.length) / 7) * 7;
        int remainder = data.length - main;
        int size = (data.length) / 7 * 8 + (remainder + 1);
        byte[] newd = new byte[size];   
             
        int j = 0;
        for(int i = 0; i < data.length; i+=7)
            {
            // First load the top bits
            for(int x = 0; x < 7; x++)
                {
                if (i+x < data.length)
                    newd[j] = (byte)(newd[j] | (((data[i + x] >>> 7) & 1) << x));
                }
            j++;
            // Next load the data
            for(int x = 0; x < 7; x++)
                {
                if (i+x < data.length)
                    newd[j+x] = (byte)(data[i+x] & 127);
                }
            j+=7;
            }
        return newd;
        }



    public Object[] emitAll(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        if (tempModel == null)
            tempModel = getModel();

        int BB = model.get("bank", 0);
        int NN = model.get("number", 0);

        // there is a bug in the venom's patch format.  Working memory is correct,
        // but submission to patch slots directly incorrectly inserts an extra repeated byte
        // after osc2waveform.  I have to deal with this here, since it's 240 instead of 239
        byte[] data = new byte[toWorkingMemory ? 239 : 240];
        
        data[0] = (byte)0xF0;
        data[1] = (byte)0x00;           // M-audio
        data[2] = (byte)0x01;
        data[3] = (byte)0x05;
        data[4] = (byte)0x21;           // Venom
        data[5] = (byte)DEFAULT_ID; //(byte)getID();
        data[6] = (byte)0x02;           // Write Data Dump
        data[7] = (byte)(toWorkingMemory ? 0x00 : 0x01);
        data[8] = (byte)(toWorkingMemory ? 0x01 : BB + 1);              // it's literally "1..4"
        data[9] = (byte)NN;             // ignored when toWorkingMemory
        data[data.length - 1] = (byte)0xF7;     
        
        int offset = 10;
        
        String[] params = (toWorkingMemory ? parameters : parameters240);
        byte[] d = new byte[params.length];
        int j = 0;
        for(int i = 0; i < params.length; i++)
            {
            String key = params[i];
            
            if (key.equals("osc2waveform"))
                {
                // this is doubled
                if (data.length == 240) // it should be 239
                    {
                    d[j] = (byte)(model.get(key));
                    d[j + 1] = d[j];
                    }
                }   
                
            if (key.equals("-"))
                {
                // nothing
                }
            else if (key.equals("arpoctaverange"))
                {
                // a bug in the venom returns 0 rather than 64 when data.length == 240.
                // For the time being, I'm replicating this, though I don't know if the
                // venom can handle a proper upload if it needs this broken upload
                if (data.length == 240)
                    {
                    d[j] = (byte)(model.get(key) - 64);         // shift to -4 ... +4
                    }
                else
                    {
                    d[j] = (byte)model.get(key);
                    }
                }
            else if (key.equals("cutoffhigh"))
                {
                d[j] = (byte)(model.get("cutoff") >>> 7);
                }
            else if (key.equals("cutofflow"))
                {
                d[j] = (byte)(model.get("cutoff") & 127);
                }
            else if (key.equals("oscflags"))
                {
                // The documentation has a "**" next to oscflags but mistakenly doesn't indicate what they are.  They are:
                //
                // 0    Waveshape ON
                // 1    OSC 2 Sync ON
                // 2    OSC 3 Sync ON
                // 3    OSC 1 Keytrack OFF  [inverted]
                // 4    OSC 2 Keytrack OFF  [inverted]
                // 5    OSC 3 Keytrack OFF  [inverted]
                // 6    UNKNOWN but always set ON               

                d[j] = (byte)(
                    (model.get("waveshape") << 0) | 
                    (model.get("osc2sync") << 1) |
                    (model.get("osc3sync") << 2) |
                    (model.get("osc1keytrack") << 3) |              // Already flipped...
                    (model.get("osc2keytrack") << 4) |
                    (model.get("osc3keytrack") << 5) |
                    0x40);                                                                      // set bit 6 to ON
                }
            else if (key.equals("glidemode") || key.equals("unisonmode") || key.equals("aux1mode") || 
                key.equals("aux2mode") || key.equals("arpsrc") || key.equals("arpbipolar") || key.equals("arplatchkeys") || key.equals("arpenable"))
                {
                d[j] = (byte)(model.get(key) == 0 ? 0 : 127);
                }
            else if (key.endsWith("destination"))  // mod destination
                {
                int val = model.get(key);
                if (val >= 18)
                    {
                    d[j] = (byte)(val - 18 + 64);
                    }
                else
                    d[j] = (byte)val;
                }
            else
                {
                d[j] = (byte)(model.get(key));
                }
            j++;
            }
                                
        // Load name
        String name = model.get("name", "") + "          ";
        for(int i = 0; i < 10; i++)
            {
            d[(data.length == 240 ? 189 : 188) + i] = (byte)(name.charAt(i));
            }

        // Nybblize, or whatever you'd call it, into data
        d = convertTo7Bit(d);
        System.arraycopy(d, 0, data, 10, d.length);
                
        // Compute checksum
        data[data.length - 2] = checksum(data, 6, data.length - 2);             // starting at command

        Object[] result = new Object[] { data };
        return result;
        }

    public int processArpOctave(byte val)
        {
        int o = val;
        if (o >= 60 && o <= 68) // we're good
            return o;
        if (o >= -4 && o <= 4)
            return 64 + o;
        else if (o >= 124 && o <= 127)          // (-4 ... -1)
            return (o - 128 + 64);
        else if (o >= -128 && o <= -124)        // (0 ... 
            return (o + 128 + 64);
        else 
            {
            System.err.println("BAD unconverted arp octave value " + o);
            return 0;
            }
        }



    // The Venom spits out various CC and NRPN when you turn its knobs, corresponding to certain
    // parameters.  We handle them here.
    //
    // The table below indicates the CC values, or in some cases NRPN values when indicated, emitted for
    // each of the knobs and buttons on the Venom.   It's a weird assortment.  The Modulation wheel also
    // emits but we're ignoring that of course.  Ranges are always 0-127 for CC, or 0-16383 for 14-bit CC,
    // unless indicated otherwise.  NRPN ranges are as indicated and always MSB only.
    //
    //                        COLUMNS 
    //          1       2        3        4      5
    //      +----------------------------------------------------------------------------------------------------
    //    1 |   3/35*  71      103      104      70 (range 0-6)
    // R  2 |  50      51       30/62*   31/63*  NRPN 17D, NRPN 17E, or BOTH (MSB ONLY OFF:<=63 ON:>64) ***
    // O  3 |  20      22       23       24      NRPN 16A (MSB ONLY OFF:<=63 ON:>64)
    // W  4 |  73      75       79       72      126 ON vs. 127 ON **
    // S  5 |  86      14       15        5      65
    //    6 |   7      10       91       93      NRPN 120 (range 0-5 MSB ONLY)
    //
    //      * 14-bit CC, sent as LSB and MSB, perhaps separately
    //     ** Either 126 is sent OR 127 is sent to counter each other
    //    *** NRPN 17D is turned ON and 17E is turned OFF, or the opposite, or both on, or both off (4 possibilities)
    //

    public void handleSynthCCOrNRPN(Midi.CCData data)
        {
        boolean sendMIDI = getSendMIDI();
        setSendMIDI(true);

        if (data.type == Midi.CCDATA_TYPE_RAW_CC)
            {
            switch (data.number)
                {
                // TOP ROW
                case 3:         // Filter cutoff Coarse
                    {
                    model.set("cutoff", (model.get("cutoff") % 128) + (data.value * 128));
                    break;
                    }
                case 35:        // Filter cutoff fine
                    {
                    model.set("cutoff", ((model.get("cutoff") / 128) * 128) + data.value);
                    break;
                    }
                case 71:        // Filter Resonance
                    {
                    model.set("resonance", data.value);
                    break;
                    }
                case 103:       // Modulation Depth Node 1      [by default, this is Env2 -> Filter Cutoff]
                    {
                    model.set("mod1scaling", data.value);
                    break;
                    }
                case 104:       // Modulation Depth Node 2      [by default, this is Keytrack -> Filter Cutoff]
                    {
                    model.set("mod2scaling", data.value);
                    break;
                    }
                case 70:        // VCF Type
                    {
                    // 0 = Off
                    // 1 = 12db LP
                    // 2 = 12db BP
                    // 3 = 12db HP
                    // 4 = 24db LP
                    // 5 = 24db BP
                    // 6 = 24db HP
                    if (data.value <= 6)
                        model.set("filtertype", data.value);
                    break;
                    }
                        
                // SECOND ROW
                case 50:                // OSC3 -> OSC 1 FM
                    {
                    model.set("fmlevel", data.value);
                    break;
                    }
                case 51:                // OSC1 * OSC2 Ring Mod Mix Level
                    {
                    model.set("ringmod", data.value);
                    break;
                    }
                case 30:                // OSC2 Coarse Tune?    52 = 16' 64 = 8' 72 = 4' 84 = 2'
                    {
                    model.set("osc2coarsetune", data.value);
                    break;
                    }
                case 62:                // OSC2 Fine Tune?    0 = -0.5 semitones ... 64 = none  127 = +0.5 semitones
                    {
                    model.set("osc2finetune", data.value);
                    break;
                    }
                case 31:                // OSC3 Coarse Tune?    52 = 16' 64 = 8' 72 = 4' 84 = 2'
                    {
                    model.set("osc3coarsetune", data.value);
                    break;
                    }
                case 63:                // OSC3 Fine Tune?    0 = -0.5 semitones ... 64 = none  127 = +0.5 semitones
                    {
                    model.set("osc3finetune", data.value);
                    break;
                    }

                // THIRD ROW
                case 20:                // EG2 Attack?
                    {
                    model.set("env2attack", data.value);
                    break;
                    }
                case 22:                // EG2 Decay?
                    {
                    model.set("env2decay", data.value);
                    break;
                    }
                case 23:                // EG2 Sustain?
                    {
                    model.set("env2sustain", data.value);
                    break;
                    }
                case 24:                // EG2 Release?  127 = release hold
                    {
                    model.set("env2release", data.value);
                    break;
                    }

                // FOURTH ROW
                case 73:                // EG1 Attack
                    {
                    model.set("env1attack", data.value);
                    break;
                    }
                case 75:                // EG1 Decay
                    {
                    model.set("env1decay", data.value);
                    break;
                    }
                case 79:                // EG1 Sustain
                    {
                    model.set("env1sustain", data.value);
                    break;
                    }
                case 72:                // EG1 Release? 127 = release hold
                    {
                    model.set("env1release", data.value);
                    break;
                    }
                case 126:               // Mono ON (poly off)
                    {
                    model.set("voicemode", 0);
                    break;
                    }
                case 127:               // Poly ON (mono off)
                    {
                    model.set("voicemode", 1);
                    break;
                    }


                // FIFTH ROW
                case 86:                // LFO1 Rate 0..110, plus others
                    {
                    model.set("lfo1rate", data.value);
                    break;
                    }
                case 14:                // LFO2 Rate
                    {
                    model.set("lfo2rate", data.value);
                    break;
                    }
                case 15:                // LFO2 Wave            FIXME: (May have "Exponential Square", "Logarithmic Square" backwards?)
                    {
                    model.set("lfo2waveform", data.value);
                    break;
                    }
                case 5:                 // Portamento (Glide) time
                    {
                    model.set("glidetime", data.value);
                    break;
                    }
                case 65:                // Portamento (Glide) <= 63 off >= 64 on
                    {
                    model.set("glidemode", (data.value < 64 ? 0 : 1));
                    break;
                    }


                // SIXTH ROW
                case 7:                 // "Synth Track Volume" (Channel volume)
                    {
                    model.set("channelvolume", data.value);
                    break;
                    }
                case 10:                // Pan
                    {
                    model.set("channelpan", data.value);
                    break;
                    }
                case 91:                // "Reverb Send Level"  (AuxFX1 Send)
                    {
                    model.set("channelaux1send", data.value);
                    break;
                    }
                case 93:                // "Delay Send Level"   (AuxFX2 Send)
                    {
                    model.set("channelaux2send", data.value);
                    break;
                    }
                }
            }
        else if (data.type == Midi.CCDATA_TYPE_NRPN)
            {
            switch (data.number)            // COARSE ONLY
                {
                // SECOND ROW
                /// FIXME TEST THIS
                case (2 * 128 + 125):   // 17D  OSC2 Sync <=63 OFF >= 64 ON             COARSE ONLY
                    {
                    model.set("osc2sync", (data.value / 128 < 64 ? 0 : 1));
                    break;
                    }
                /// FIXME TEST THIS
                case (2 * 128 + 126):   // 17E  OSC3 Sync <=63 OFF >= 63 ON             COARSE ONLY
                    {
                    model.set("osc3sync", (data.value / 128 < 64 ? 0 : 1));
                    break;
                    }
                                
                // THIRD ROW
                /// FIXME TEST THIS
                case (2 * 128 + 106):   // Unison Switch <=63 OFF >= 63 ON              COARSE ONLY
                    {
                    model.set("unisonmode", (data.value / 128 < 64 ? 0 : 1));
                    break;
                    }
                        
                // SIXTH ROW
                /// FIXME TEST THIS
                case (2 * 128 + 32):    // Insert Select  0 = OFF 1 = EQ  2 = compressor 3 = wah 4 = distortion 5 = "destructive" (reducer)    COARSE ONLY
                    {
                    int val = data.value / 128;
                    if (val <= 5)
                        model.set("channelfxtype", val);
                    break;
                    }
                }
            }

        setSendMIDI(sendMIDI);
        }

    public int parse(byte[] data, boolean fromFile)
        {
        if (data[7] == 0x01)     // it's going to a specific patch (0x01 == Single Patch Dump as opposed to 0x00 == Edit Buffer Dump)
            {
            int bank = data[8] - 1;
            if (bank < 0 || bank > 3) bank = 0;
            model.set("bank", bank);
            int number = data[9];
            model.set("number", number);
            }

        // First denybblize
        byte[] d = convertTo8Bit(data, 10, data.length - 2);
        
        //        model.debug = true;
        int offset = 10;
        
        // Load name
        char[] name = new char[10];
        for(int i = 0; i < 10; i++)
            {
            name[i] = (char)(d[(data.length == 240 ? 189 : 188) + i] & 127);
            }
        model.set("name", new String(name));
                                                 
        // Load remaining parameters
        int j = 0;
        String[] params = (data.length == 240 ? parameters240 : parameters);
        for(int i = 0; i < params.length; i++)
            {
            String key = params[i];
            
            // we're now handling this with a different set of parameters
            /*
              if (key.equals("osc2coarsetune"))
              {
              // this is right after osc2waveform, which has an incorrectly repeated byte
              // in the sysex IF the data arriving was from a specific patch rather than
              // from the edit buffer.  So I need to skip this extra byte before continuing
              // here.
              if (data.length == 240) // it should be 239
              {
              j++;
              }
              }
            */
            
            // no else here
            if (key.equals("-"))
                {
                // nothing
                }
            else if (key.equals("arpoctaverange"))
                {
                model.set("arpoctaverange", processArpOctave(d[i]));
                }
            else if (key.equals("cutoffhigh"))
                {
                model.set("cutoff", (d[j] << 7) | d[j+1] );
                }
            else if (key.equals("cutofflow"))
                {
                // nothing
                }
            else if (key.equals("oscflags"))
                {
                // The documentation has a "**" next to oscflags but mistakenly doesn't indicate what they are.  They are:
                //
                // 0    Waveshape ON
                // 1    OSC 2 Sync ON
                // 2    OSC 3 Sync ON
                // 3    OSC 1 Keytrack OFF  [inverted]
                // 4    OSC 2 Keytrack OFF  [inverted]
                // 5    OSC 3 Keytrack OFF  [inverted]
                // 6    UNKNOWN but always set ON               

                model.set("waveshape", (d[j] >>> 0) & 0x01);
                model.set("osc2sync", (d[j] >>> 1) & 0x1);
                model.set("osc3sync", (d[j] >>> 2) & 0x1);
                model.set("osc1keytrack", (d[j] >>> 3) & 0x1);
                model.set("osc2keytrack", (d[j] >>> 4) & 0x1);
                model.set("osc3keytrack", (d[j] >>> 5) & 0x1);
                // ignore bit 6, we don't know what it is
                }
            else if (key.equals("glidemode") || key.equals("unisonmode") || key.equals("aux1mode") || 
                key.equals("aux2mode") || key.equals("arpsrc") || key.equals("arpbipolar") || key.equals("arplatchkeys") || key.equals("arpenable"))
                {
                model.set(key, d[j] < 64 ? 0 : 1);              // according to footnote *** on p. 97
                }
            else if (key.endsWith("destination"))  // mod destination
                {
                if (d[j] >= 64) // it's a mod destination, need to shift
                    {
                    model.set(key, (d[j] - 64) + 18);
                    }
                else
                    {
                    model.set(key, d[j]);
                    }
                }
            else
                {
                model.set(key, d[j]);
                }
            j++;
            }
                        
        // we must now let the venom know to shut up or else it'll keep sending us junk

        //        model.debug = false;
                
        if (!fromFile)
            {
            boolean sendMIDI = getSendMIDI();
            setSendMIDI(true);
                                
            // CANCEL
            // We send a CANCEL instead of an ACK because the Venom seems to respond to ACKs by often resending
            // the data.
            tryToSendSysex(new byte[] { (byte)0xF0, 0x00, 0x01, 0x05, 0x21, 
                (byte)DEFAULT_ID, //getID(), 
                0x7D,                       // ACK is 0x7F
                (byte)0xF7 });
                                
            
            setSendMIDI(sendMIDI);
            }

        revise();
        return PARSE_SUCCEEDED;
        }
        
        
    // manual says: 
    // The checksum is calculated as the sum of all bytes taken from the <cmd> byte 
    // and stores 0-Total with the top bit set to 0. When a SysEx is received, it 
    // totals up all values from the <cmd> byte including the checksum and the 
    // result in the bot- tom 7 bits should be 0.

    // FIXME: is this right?
    public byte checksum(byte[] data, int start, int end)
        {
        int sum = 0;
        for(int i = start; i < end; i++)
            {
            sum += data[i];
            }
        return (byte)((0-sum) & 0x7F);
        }
        

    void switchScreen()
        {
        // Then Vyzex seems to send the venom the following undocumented command to switch to "Single"

        byte[] data = new byte[13];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x00;
        data[2] = (byte)0x01;
        data[3] = (byte)0x05;
        data[4] = (byte)0x21;
        data[5] = (byte)DEFAULT_ID; //(byte)getID();
        data[6] = (byte)0x02;           // "Write Dump" What???  Really???
        data[7] = (byte)0x15;           // UNDOCUMENTED LOCATION
        data[8] = (byte)0x00;           // UNDOCUMENTED LOCATION
        data[9] = (byte)0x01;           // UNDOCUMENTED LOCATION
        data[10] = (byte)0x00;  
        data[11] = (byte)0x00;
        data[12] = (byte)0xF7;
        tryToSendSysex(data);
        }
        
    /// This switches the screen when we do a curent dump but don't change the patch
    public void performRequestCurrentDump()
        {
        super.performRequestCurrentDump();
        switchScreen();
        }


        
    // The Venom doesn't say exactly how to change patches for multi vs single.  However in the documentation
    // for the "MIDI Single Select" and "MIDI Multi Select" global parameter options, it says this:
        
    // "MIDI Single Select Determines whether or not Single Programs respond to MIDI Program Change messages.
    //
    // MIDI Multi Select Determines whether or not Multi Programs respond to MIDI Program Change messages. 
    // Note that since Multi Parts can be set to any MIDI channel, Multi Program changes are handled with 
    // the bank change number assignments above the Single Program banks."
        
    // From this I am *guessing* that it means that bank select works like this?
    //
    // BANK SELECT VAL      BANK
    // 0                            Single A
    // 1                            Single B
    // 2                            Single C
    // 3                            Single D
    // 4                            Multi A
    // 5                            Multi B
        
    // FIXME: I do not know if this is correct.  Nor do I know if you have to have the two MIDI Select parameters
    // turned On for it to work, nor what happens when they're not turned on.  But that's what I'm going with
    // for the moment.
        
    public void changePatch(Model tempModel)
        {
        int bank = tempModel.get("bank", 0);
        int number = tempModel.get("number", 0);
        
        /*
        // Can't get the venom to respond to PC or BC
        tryToSendMIDI(buildCC(getChannelOut(), 0, bank));
        tryToSendMIDI(buildPC(getChannelOut(), number));
        */

        // Vyzex seems to send to the venom the following undocumented command to change the patch:

        //F0 00 01 05 21 F7 02 15 00 0D 00 100 F7

        byte[] data = new byte[13];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x00;
        data[2] = (byte)0x01;
        data[3] = (byte)0x05;
        data[4] = (byte)0x21;
        data[5] = (byte)DEFAULT_ID; //(byte)getID();
        data[6] = (byte)0x02;           // "Write Dump" What???  Really???
        data[7] = (byte)0x15;           // UNDOCUMENTED LOCATION
        data[8] = (byte)0x00;           // UNDOCUMENTED LOCATION
        data[9] = (byte)0x0D;           // UNDOCUMENTED LOCATION
        data[10] = (byte)bank;
        data[11] = (byte)number;
        data[12] = (byte)0xF7;
        tryToSendSysex(data);

        switchScreen();
        }


    public byte[] requestCurrentDump()
        {
        byte[] data = new byte[11];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x00;
        data[2] = (byte)0x01;
        data[3] = (byte)0x05;
        data[4] = (byte)0x21;
        data[5] = (byte)DEFAULT_ID; //(byte)getID();
        data[6] = (byte)0x01;           // data dump request
        data[7] = (byte)0x00;           // current buffer
        data[8] = (byte)0x01;           // single patch
        data[9] = (byte)0x00;           // doesn't matter
        data[10] = (byte)0xF7;
        return data;
        }

    public void performRequestDump(Model tempModel, boolean changePatch)
        {
        performChangePatch(tempModel);
        tryToSendSysex(requestDump(tempModel));
        model.set("number", tempModel.get("number"));
        model.set("bank", tempModel.get("bank"));
        }

    public int getPauseAfterChangePatch() { return 500; }                               // quite a long time
    public int getPauseAfterSendAllParameters() { return 750; }

    // This is how you'd request a patch, but we're not using it because we have
    // overridden performRequestDump above.
    public byte[] requestDump(Model tempModel)
        {
        if (tempModel == null)
            tempModel = getModel();

        int NN = tempModel.get("number", 0);                    // The numbers are 0...127 but...
        int BB = tempModel.get("bank", 0) + 1;                  // Believe it or not, the banks are literally 1...4
        
        byte[] data = new byte[11];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x00;
        data[2] = (byte)0x01;
        data[3] = (byte)0x05;
        data[4] = (byte)0x21;
        data[5] = (byte)DEFAULT_ID; //(byte)getID();
        data[6] = (byte)0x01;           // data dump request
        data[7] = (byte)0x01;           // single patch
        data[8] = (byte)BB;     
        data[9] = (byte)NN;
        data[10] = (byte)0xF7;
        return data;
        }


    public static final int MAXIMUM_NAME_LENGTH = 10;
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
        
        String nm = model.get("name", "Untitled");
        String newnm = revisePatchName(nm);
        if (!nm.equals(newnm))
            model.set("name", newnm);
            
        // deal with modulation amount destinations
        for(int i = 3; i < 16; i++)
            {
            if (model.get("mod" + i + "destination") == i + 17)     // can't refer to yourself
                model.set("mod" + i + "destination", 0);                        // off
            for(int j = 3; j < 16; j++)
                {
                if (model.get("mod" + j + "destination") == i + 17 &&   // someone is referring to you
                    model.get("mod" + i + "destination") >= 18)                     // you're using a modulation destination, not legal 
                    model.set("mod" + i + "destination", 0);                                // off
                }
            }
        }
    
    public static String getSynthName() { return "M-Audio Venom"; }
    public String getPatchName(Model model) { return model.get("name", "Untitled"); }
    
    public Model getNextPatchLocation(Model model)
        {
        int number = model.get("number");
        int bank = model.get("bank");
        
        number++;
        if (number >= 128)
            {
            number = 0;
            bank++;
            if (bank >= 4)
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
        
    public boolean adjustBulkSysexForWrite(Synth window, byte[][][] data)
        {
        int res = showMultiOption(window, new String[0], new JComponent[0], 
            new String[] { "Bank C", "Bank D", "Cancel" },
            0, getSynthName() + " Bulk Write", 
            new JLabel("Write " + getSynthName() + " patches to which bank?"));
        if (res < 0 || res == 2)
            return false;
        else for(int i = 0; i < data.length; i++)
                 {
                 for(int j = 0; j < data[i].length; j++)
                     {
                     data[i][j][5] = DEFAULT_ID;             // 0x7F just in case
                     if (data[i][j][7] == 0x01)      // Single Patch Dump
                         {
                         data[i][j][8] = (byte)(res == 0 ? 3 : 4);                       // banks are a=1, b=2, c=3, d=4
                                        
                         // recompute checksum
                         int len = data[i][j].length - 2;
                         data[i][j][len] = checksum(data[i][j], 6, len);
                         }
                     else System.err.println("Unknown sysex");
                     }
                 }
        return true;
        }
                 

    /** Map of parameter -> index in the allParameters array. */
    static HashMap parametersToIndex = null;


    /**  List of all M-audio Venom parameters in order. 
    
         Special situations: 
         // glidemode is either 0 or 127
         //      Edisyn cutoff is 16-bit -> venom cutoffhigh and cutofflow
         //      venom oscflags:
         // Flags appear to be:
         // 0    ??
         // 1    OSC 2 Sync
         // 2    OSC 3 Sync
         // 3    OSC 1 Keytrack OFF
         // 4    OSC 2 Keytrack OFF
         // 5    OSC 3 Keytrack Off
         // 6    ??
         // voicemode is 0 vs 1
         // unisonmode is 0 vs 127
         // aux1mode (enable) is either 0 or 127
         // probably same for aux2mode?
         // Docs say aux1type is 0...4, but it's actually 0...3 (0 is NOT off)
         // arpsrc is 0=pattern, *127*=single
         // arpbipolar is 0 or 127
         // arplatchkeys is 0 or 127
        
         */
                
    //should be 198 long
    final static String[] parameters = new String[]
    {
    "glidemode",
    "glidetime",
    "-",                                    // bank                 2
    "-",                                    // number               3
    "env1attack",
    "env1hold",
    "env1decay",
    "env1sustain",
    "env1release",
    "env2attack",
    "env2hold",
    "env2decay",
    "env2sustain",
    "env2release",
    "env3attack",
    "env3hold",
    "env3decay",
    "env3sustain",
    "env3release",
    "startmod",
    "oscdrift",
    "bendrange",
    "ringmod",
    "fmlevel",
    "oscflags",                                     // broken out           24
    "waveshapewidth",
    "osc1waveform",
    "osc1coarsetune",
    "osc1finetune",
    "osc2waveform",
    "osc2coarsetune",
    "osc2finetune",
    "boost",
    "osc3waveform",
    "osc3coarsetune",
    "osc3finetune",
    "-",                                            // reserved             36
    "lfo1waveform",
    "lfo1rate",
    "lfo1delay",
    "lfo1attack",
    "lfo1startphase",
    "lfo2waveform",
    "lfo2rate",
    "lfo2delay",
    "lfo2attack",
    "lfo2startphase",
    "lfo3waveform",
    "lfo3rate",
    "lfo3delay",
    "lfo3attack",
    "lfo3startphase",
    "mod1source",
    "mod2source",
    "mod3source",
    "mod4source",
    "mod5source",
    "mod6source",
    "mod7source",
    "mod8source",
    "mod9source",
    "mod10source",
    "mod11source",
    "mod12source",
    "mod13source",
    "mod14source",
    "mod15source",
    "mod16source",
    "mod1destination",
    "mod2destination",
    "mod3destination",
    "mod4destination",
    "mod5destination",
    "mod6destination",
    "mod7destination",
    "mod8destination",
    "mod9destination",
    "mod10destination",
    "mod11destination",
    "mod12destination",
    "mod13destination",
    "mod14destination",
    "mod15destination",
    "mod16destination",
    "mod1scaling",
    "mod2scaling",
    "mod3scaling",
    "mod4scaling",
    "mod5scaling",
    "mod6scaling",
    "mod7scaling",
    "mod8scaling",
    "mod9scaling",
    "mod10scaling",
    "mod11scaling",
    "mod12scaling",
    "mod13scaling",
    "mod14scaling",
    "mod15scaling",
    "mod16scaling",
    "osc1volume",
    "osc2volume",
    "osc3volume",
    "extinvolume",
    "extinsource",
    "filtertype",
    "cutoffhigh",                                   // combined to cutoff           106
    "cutofflow",                                    // combined to cutoff           107
    "resonance",
    "coarsetune",
    "finetune",
    "voicemode",
    "unisonmode",
    "unisoncount",
    "unisondetune",
    "channelvolume",
    "channelpan",
    "channeldirect",
    "channelaux1send",
    "channelaux2send",
    "channelfxtype",
    "hiloeqlowfreq",
    "hiloeqlowgain",
    "hiloeqhighfreq",
    "hiloeqhighgain",
    "tremolowaveform",
    "tremolorate",
    "tremolovoldepth",
    "tremolopandepth",
    "autowahtype",
    "autowahcutoff",
    "autowahresonance",
    "autowahsensitivity",
    "compressorattack",
    "compressorrelease",
    "compressorthreshold",
    "compressorratio",
    "compressorgain",
    "distortiontype",
    "distortiondepth",
    "distortionpregain",
    "distortionpostgain",
    "distortionhighcutoff",
    "bandpassmidfreq",
    "bandpassmidgain",
    "bandpassmidq",
    "reducerbitdepth",
    "reducersamplerate",
    "aux1mode",
    "aux1type",
    "aux1depth",
    "aux1prehp",
    "aux1predelay",
    "aux1highdamp",
    "aux1time",
    "aux1feedback",
    "aux1gatedelaytime",
    "aux1gatethresh",
    "aux1tonegain",
    "aux1tonefreq",
    "aux2mode",
    "aux2type",
    "aux2depth",
    "aux2toaux1",
    "aux2prehp",
    "aux2prelp",
    "aux2time",
    "aux2feedback",
    "aux2highdamp",
    "aux2lforate",
    "aux2lfodepth",
    "progvolume",
    "mastereqlowfreq",
    "mastereqlowgain",
    "mastereqmidfreq",
    "mastereqmidgain",
    "mastereqhighfreq",
    "mastereqhighgain",
    "arpenable",
    "arpsrc",
    "arpbank",
    "arppattern",
    "arpmode",
    "arpnoteorder",
    "arpoctaverange",
    "arpbipolar",
    "arplatchkeys",
    "arprootnote",
    "-",                                                    // patch names                          188
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    };    



    //// When the data is 240 in size, there are two major differences.
    //// 1. There's an extra osc2coarsetune for unknown reasons, likely a bug
    //// 2. The mod matrix is rearranged to source/destination/scaling
    ////    rather than all the sources, then all the destinations, then all the scalings!!!

    final static String[] parameters240 = new String[]
    {
    "glidemode",
    "glidetime",
    "-",                                    // bank                 2
    "-",                                    // number               3
    "env1attack",
    "env1hold",
    "env1decay",
    "env1sustain",
    "env1release",
    "env2attack",
    "env2hold",
    "env2decay",
    "env2sustain",
    "env2release",
    "env3attack",
    "env3hold",
    "env3decay",
    "env3sustain",
    "env3release",
    "startmod",
    "oscdrift",
    "bendrange",
    "ringmod",
    "fmlevel",
    "oscflags",                                     // broken out           24
    "waveshapewidth",
    "osc1waveform",
    "osc1coarsetune",
    "osc1finetune",
    "osc2waveform",
    "-",                                                                                        // ERROR, osc2waveform is repeated
    "osc2coarsetune",
    "osc2finetune",
    "boost",
    "osc3waveform",
    "osc3coarsetune",
    "osc3finetune",
    "-",                                            // reserved             36
    "lfo1waveform",
    "lfo1rate",
    "lfo1delay",
    "lfo1attack",
    "lfo1startphase",
    "lfo2waveform",
    "lfo2rate",
    "lfo2delay",
    "lfo2attack",
    "lfo2startphase",
    "lfo3waveform",
    "lfo3rate",
    "lfo3delay",
    "lfo3attack",
    "lfo3startphase",
    "mod1source",
    "mod1destination",
    "mod1scaling",
    "mod2source",
    "mod2destination",
    "mod2scaling",
    "mod3source",
    "mod3destination",
    "mod3scaling",
    "mod4source",
    "mod4destination",
    "mod4scaling",
    "mod5source",
    "mod5destination",
    "mod5scaling",
    "mod6source",
    "mod6destination",
    "mod6scaling",
    "mod7source",
    "mod7destination",
    "mod7scaling",
    "mod8source",
    "mod8destination",
    "mod8scaling",
    "mod9source",
    "mod9destination",
    "mod9scaling",
    "mod10source",
    "mod10destination",
    "mod10scaling",
    "mod11source",
    "mod11destination",
    "mod11scaling",
    "mod12source",
    "mod12destination",
    "mod12scaling",
    "mod13source",
    "mod13destination",
    "mod13scaling",
    "mod14source",
    "mod14destination",
    "mod14scaling",
    "mod15source",
    "mod15destination",
    "mod15scaling",
    "mod16source",
    "mod16destination",
    "mod16scaling",
    "osc1volume",
    "osc2volume",
    "osc3volume",
    "extinvolume",
    "extinsource",
    "filtertype",
    "cutoffhigh",                                   // combined to cutoff           106
    "cutofflow",                                    // combined to cutoff           107
    "resonance",
    "coarsetune",
    "finetune",
    "voicemode",
    "unisonmode",
    "unisoncount",
    "unisondetune",
    "channelvolume",
    "channelpan",
    "channeldirect",
    "channelaux1send",
    "channelaux2send",
    "channelfxtype",
    "hiloeqlowfreq",
    "hiloeqlowgain",
    "hiloeqhighfreq",
    "hiloeqhighgain",
    "tremolowaveform",
    "tremolorate",
    "tremolovoldepth",
    "tremolopandepth",
    "autowahtype",
    "autowahcutoff",
    "autowahresonance",
    "autowahsensitivity",
    "compressorattack",
    "compressorrelease",
    "compressorthreshold",
    "compressorratio",
    "compressorgain",
    "distortiontype",
    "distortiondepth",
    "distortionpregain",
    "distortionpostgain",
    "distortionhighcutoff",
    "bandpassmidfreq",
    "bandpassmidgain",
    "bandpassmidq",
    "reducerbitdepth",
    "reducersamplerate",
    "aux1mode",
    "aux1type",
    "aux1depth",
    "aux1prehp",
    "aux1predelay",
    "aux1highdamp",
    "aux1time",
    "aux1feedback",
    "aux1gatedelaytime",
    "aux1gatethresh",
    "aux1tonegain",
    "aux1tonefreq",
    "aux2mode",
    "aux2type",
    "aux2depth",
    "aux2toaux1",
    "aux2prehp",
    "aux2prelp",
    "aux2time",
    "aux2feedback",
    "aux2highdamp",
    "aux2lforate",
    "aux2lfodepth",
    "progvolume",
    "mastereqlowfreq",
    "mastereqlowgain",
    "mastereqmidfreq",
    "mastereqmidgain",
    "mastereqhighfreq",
    "mastereqhighgain",
    "arpenable",
    "arpsrc",
    "arpbank",
    "arppattern",
    "arpmode",
    "arpnoteorder",
    "arpoctaverange",
    "arpbipolar",
    "arplatchkeys",
    "arprootnote",
    "-",                                                    // patch names                          188
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    };    




    /// arp pattern names

    public static final String[][] DEFAULT_ARP_PATTERN_NAMES = new String[][]
    {
    { "Dance Club", "Warming Up", "Solid Gruv", "LaynItDown", "BreakItUp", "Fun-kay", "GruvHound", "StreetWise", "Vampin’", "New Hats", "GetInGear", "Many Hands", "Clave City", "Hip Hip", "Everything", "Rollin’", "Classic", "Tight Enuf", "TwoFisted", "U know it", "SleuthClik", "Reminds Me", "MorCowbell", "On The Rim", "Low-n-slow", "RnB ballad", "WaitingFor", "TalkItOver", "Callback", "TurnAround", "UR Famous", "1stRespons", "Hip Click", "YerBasicBt", "Bommm", "Move Along", "Three Seas", "RollWithIt", "InTheAlley", "Skip It", "Baby Me", "And Now---", "3za Crowd", "3sqrChurch", "Traces Of-", "RelyOnMe", "EbbNFlow", "MeltingPot", "QuickClub", "TriplePark", "Octavator", "Chance It", "FrmTheEast", "Lazy Bass", "Offbeat", "RightWithU", "Lockstep", "Dovetail", "Skipper", "Psycho", "Ponger", "DownDirty", "Thruster", "Homeboy", "Nite Fever", "SwinginHrd", "OneOfAKind", "WaitForIt", "Roll a Six", "Invader", "Driver", "Poppy", "Sneaky", "Pensive", "HammerHnd", "Careful", "Ubiquitous", "ThinkAbout", "Bizness", "Interim", "Attention", "Strummer", "Mowdown", "TyrinNotTo", "BackMeUp", "Latismo", "Relentless", "FastTalker", "DrillNfill", "Blistering", "Slow Jump", "Fast Jump", "Beeper", "Tension", "OneFourAll", "Minor Hit", "Jump Over", "Downward", "OverTheTop", "Angelic", "Two bar L", "One bar L", "Half bar L", "Quarter L", "Q-trip L", "8th L", "8-trip L", "16th L", "16-trip L", "32nd L", "Offbeat Q", "Q-dot 1", "Q-dot 2", "Q-dot 3", "8-dot 1", "8-dot 2", "8-dot 3", "8-dot 4", "16 swing 1", "16 swing 2", "16-dot 1", "16-dot 2", "16-dot 3", "16-dot 4", "Offbeat 16", "Session", "Stepper", "GotDaBenz", }, 
    { "Bounce", "Metropolis", "Percolate", "Robotheque", "GottaDance", "HipCricket", "Tradeoff", "MadeTheCut", "Drop Out", "InThPocket", "HoldSteady", "oh8-n-see", "Restart", "Coastin’", "PlastiPerc", "oh8daGreat", "Shufflin’", "Sambalicis", "ClappTrapp", "Happnin", "WooferTest", "Congregate", "Take Over", "Refocus", "RockinRide", "Thermometr", "HotPotato", "Contrasts", "Undertow", "StandClear", "Mid Scheme", "Back Hat", "Determin8n", "Hipalong", "KickTheCow", "Misfire", "Drop Kick", "Gypsy Clap", "BackNForth", "ClaveMastr", "Boot Scoot", "Power Of 3", "Nativitan", "AllYouNeed", "TriplThret", "Euro Club", "ThePunches", "PrettyCool", "MixinMatch", "MissingMan", "OneNFive", "OneNSeven", "PrettyHary", "Dozen Off", "BustedMain", "Assertive", "MattrOFact", "Omination", "BlackCoptr", "TooMuch4U", "DigginHard", "Radical", "16 Stage", "YouTakeIt", "JmaicaThis", "WhichCntry", "HalfWhole", "ZunkItUp", "TurtleSix", "SixShooter", "OctoFunk", "Malevolent", "Poly Fill", "Royalty", "MellowOut", "Contempl8", "3rdWorldly", "Backlash", "Nine Drop", "Climbin 9", "Ripper", "Funkworthy", "Tremulous", "ShufflWalk", "FunkyBend", "Finish ‘em", "Flame-n-Co", "Stutterfly", "Overwhelm", "Outpost", "ShuffleOff", "Paralyzer", "Provocativ", "Funky fill", "Dangerous", "Pressurize", "Recursive", "Off Kilter", "Plink", "Retrospect", "Two bar S", "One bar S", "Half bar S", "Quarter S", "Q-trip S", "8th S", "8-trip S", "16th S", "16-trip S", "32nd S", "Offbeat H", "Syncop8ion", "Q-LaidBack", "Q-FulSwing", "8-LaidBack", "8 swing 1", "8 swing 2", "8 swing 3", "16 swing 3", "HausToHaus", "Pluck 1", "Pluck 2", "Pluck 3", "Diggin In", "Climber", "Toads", "AspirinPlz", "BendUrWill", }, 
    };

    }
