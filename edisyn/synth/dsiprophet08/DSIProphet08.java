/***
    Copyright 2018 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.dsiprophet08;

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
   A patch editor for the Dave Smith Instruments Prophet '08.
        
   @author Sean Luke
*/

public class DSIProphet08 extends Synth
    {
    /// Various collections of parameter names for pop-up menus
        
    public static final String[] MODULATION_SOURCES = new String[] { "Off", "Track 1", "Track 2", "Track 3", "Track 4", "LFO 1", "LFO 2", "LFO 3", "LFO 4", "Filter Env", "Amp Env", "Env 3", "Pitch Bend", "Mod Wheel", "Pressure", "MIDI Breath", "MIDI Foot", "MIDI Expression", "Velocity", "Note Number", "Noise" };
    public static final String[] MODULATION_DESTINATIONS = new String[] { "Off", "Osc 1 Freq", "Osc 2 Freq", "Osc 1&2 Freq", "Osc Mix", "Noise Level", "Osc 1 PW", "Osc 2 PW", "Osc 1&2 PW", "Filter Freq", "Filter Resonance", "Filter Audio Mod", "VCA Level", "Pan Spread", "LFO 1 Freq", "LFO 2 Freq", "LFO 3 Freq", "LFO 4 Freq", "All LFO Freqs", "LFO 1 Amt", "LFO 2 Amt", "LFO 3 Amt", "LFO 4 Amt", "All LFO Amts", "Filter Env Amt", "Amp Env Amt", "Env 3 Amt", "All Env Amts", "Env 1 Attack", "Env 2 Attack", "Env 3 Attack", "All Env Attacks", "Env 1 Decay", "Env 2 Decay", "Env 3 Decay", "All Env Decays", "Env 1 Release", "Env 2 Release", "Env 3 Release", "All Env Releases", "Mod 1 Amt", "Mod 2 Amt", "Mod 3 Amt", "Mod 4 Amt" };
    public static final String[] OSC_SHAPES = new String[] { "Off", "Saw", "Tri", "Saw/Tri" };
    public static final String[] GLIDE_MODES = new String[] { "Fixed Rate", "Fixed Rate Auto", "Fixed Time", "Fixed Time Auto" };
    public static final String[] FILTER_POLES = new String[] { "2-Pole", "4-Pole" };
    public static final String[] BANKS = new String[] { "1", "2" };
    public static final String[] NOTES = new String[] { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
    
    // this one is obviously wrong, the documentation seems strange
    public static final String[] LFO_FREQUENCIES = new String[] { "32 S", "16 S", "8 S", "6 S", "4 S", "3 S", "2 S", "3 S/2", "1 S", "2 S/3", "S/2", "3 S", "S/4", "S/6", "S/8", "S/16" };
    public static final String[] LFO_SHAPES = new String[] { "Triangle", "Reverse Saw", "Sawtooth", "Square", "Random" };
    public static final String[] CLOCK_DIVIDES = new String[] { "Half Note", "Quarter Note", "8th Note", "8th Note Half Swing", "8th Note Full Swing", "8th Note Triplets", "16th Note", "16th Note Half Swing", "16th Note Full Swing", "16th Note Triplets", "32nd Note", "32nd Note Triplets", "64th Note Triplets" };
    public static final String[] SEQUENCER_TRIGGERS = new String[] { "Normal", "Normal No Reset", "No Gate", "No Gate, No Reset", "Key Step" };
    public static final String[] KEY_MODES = new String[] { "Low Note", "Low Note Retrigger", "High Note", "High Note Retrigger", "Last Note", "Last Note Retrigger" };
    public static final String[] UNISON_MODES = new String[] { "1 Voice", "All Voices", "All Voices Detune 1",  "All Voices Detune 2",  "All Voices Detune 3" };
    public static final String[] ARPEGGIATOR_MODES = new String[] { "Up", "Down", "Up/Down", "Assign" };
    public static final String[] KEYBOARD_MODES = new String[] { "Normal", "Stack", "Split" };
     
    public static final int FILTER_ENVELOPE = 1;
    public static final int AMPLIFIER_ENVELOPE = 2;
    public static final int THIRD_ENVELOPE = 3;
    
    SynthPanel[] synthPanels = new SynthPanel[6];
    
    public DSIProphet08()
        {
        int panel = 0;
        
        for(int i = 0; i < parameters.length; i++)
            {
            parametersToIndex.put(parameters[i], Integer.valueOf(i));
            }
                        
        /// SOUND PANEL
                
        SynthPanel soundPanel = new SynthPanel(this);
        synthPanels[panel++] = soundPanel;
        
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        JComponent nameGlobal = addNameGlobal(Style.COLOR_GLOBAL());
        hbox.add(nameGlobal);
        hbox.addLast(addGlobal(1, Style.COLOR_B()));
        vbox.add(hbox);
        
        hbox = new HBox();
        hbox.add(addOscillatorGlobal(1, Style.COLOR_A()));
        hbox.add(addOscillator(1, 1, Style.COLOR_A()));
        hbox.addLast(addOscillator(1, 2, Style.COLOR_A()));
        vbox.add(hbox);
        
        hbox = new HBox();
        VBox vbox2 = new VBox(VBox.TOP_CONSUMES);
        vbox2.addLast(addFilter(1, Style.COLOR_B()));
        vbox2.add(addAmplifier(1, Style.COLOR_C()));
        hbox.add(vbox2);
                
        vbox2 = new VBox(VBox.TOP_CONSUMES);
        vbox2.addLast(addEnvelope(1, FILTER_ENVELOPE, Style.COLOR_B()));
        vbox2.add(addEnvelope(1, AMPLIFIER_ENVELOPE, Style.COLOR_C()));
        hbox.addLast(vbox2);
        vbox.add(hbox);
        
        soundPanel.add(vbox, BorderLayout.CENTER);
        soundPanel.makePasteable("layer1");
        soundPanel.setSendsAllParameters(false);
        addTab("Oscillators and Filters A", soundPanel);
                

                
        soundPanel = new SynthPanel(this);
        synthPanels[panel++] = soundPanel;
        vbox = new VBox();
        hbox = new HBox();
        hbox.add(Strut.makeHorizontalStrut(nameGlobal.getPreferredSize().width));
        hbox.addLast(addGlobal(2, Style.COLOR_B()));
        vbox.add(hbox);
        
        hbox = new HBox();
        hbox.add(addOscillatorGlobal(2, Style.COLOR_A()));
        hbox.add(addOscillator(2, 1, Style.COLOR_A()));
        hbox.addLast(addOscillator(2, 2, Style.COLOR_A()));
        vbox.add(hbox);
        
        hbox = new HBox();
        vbox2 = new VBox(VBox.TOP_CONSUMES);
        vbox2.addLast(addFilter(2, Style.COLOR_B()));
        vbox2.add(addAmplifier(2, Style.COLOR_C()));
        hbox.add(vbox2);
                
        vbox2 = new VBox(VBox.TOP_CONSUMES);
        vbox2.addLast(addEnvelope(2, FILTER_ENVELOPE, Style.COLOR_B()));
        vbox2.add(addEnvelope(2, AMPLIFIER_ENVELOPE, Style.COLOR_C()));
        hbox.addLast(vbox2);
        vbox.add(hbox);
        
        soundPanel.add(vbox, BorderLayout.CENTER);
        soundPanel.makePasteable("layer2");
        soundPanel.setSendsAllParameters(false);
        addTab("Oscillators and Filters B", soundPanel);
                
                        
        
        // MODULATION PANEL
                
        SynthPanel modulationPanel = new SynthPanel(this);
        synthPanels[panel++] = modulationPanel;
        
        vbox = new VBox();
        
        hbox = new HBox();
        hbox.add(addExternalModulation(1, "Velocity", "velocity", Style.COLOR_C()));
        hbox.addLast(addEnvelope(1, THIRD_ENVELOPE, Style.COLOR_C()));
        vbox.add(hbox);
        
        hbox = new HBox();
        hbox.add(addExternalModulation(1, "Aftertouch", "pressure", Style.COLOR_C()));
        hbox.add(addExternalModulation(1, "Mod Wheel", "wheel", Style.COLOR_C()));
        hbox.add(addExternalModulation(1, "Breath", "breath", Style.COLOR_C()));
        hbox.addLast(addExternalModulation(1, "Foot Control", "foot", Style.COLOR_C()));
        vbox.add(hbox);
                
        for(int i = 1; i <= 4; i+=2)
            {
            hbox = new HBox();
            hbox.add(addModulation(1, i, Style.COLOR_A()));
            hbox.add(addModulation(1, i+1, Style.COLOR_A()));
            hbox.add(addLFO(1, i, Style.COLOR_B()));
            hbox.addLast(addLFO(1, i+1, Style.COLOR_B()));
            vbox.add(hbox);
            }

        modulationPanel.add(vbox, BorderLayout.CENTER);
        modulationPanel.makePasteable("layer1");
        addTab("Modulation A", modulationPanel);



        modulationPanel = new SynthPanel(this);
        synthPanels[panel++] = modulationPanel;
        
        vbox = new VBox();
        
        hbox = new HBox();
        hbox.add(addExternalModulation(2, "Velocity", "velocity", Style.COLOR_C()));
        hbox.addLast(addEnvelope(2, THIRD_ENVELOPE, Style.COLOR_C()));
        vbox.add(hbox);
        
        hbox = new HBox();
        hbox.add(addExternalModulation(2, "Aftertouch", "pressure", Style.COLOR_C()));
        hbox.add(addExternalModulation(2, "Mod Wheel", "wheel", Style.COLOR_C()));
        hbox.add(addExternalModulation(2, "Breath", "breath", Style.COLOR_C()));
        hbox.addLast(addExternalModulation(2, "Foot Control", "foot", Style.COLOR_C()));
        vbox.add(hbox);
                
        for(int i = 1; i <= 4; i+=2)
            {
            hbox = new HBox();
            hbox.add(addModulation(2, i, Style.COLOR_A()));
            hbox.add(addModulation(2, i+1, Style.COLOR_A()));
            hbox.add(addLFO(2, i, Style.COLOR_B()));
            hbox.addLast(addLFO(2, i+1, Style.COLOR_B()));
            vbox.add(hbox);
            }

        modulationPanel.add(vbox, BorderLayout.CENTER);
        modulationPanel.makePasteable("layer2");
        addTab("Modulation B", modulationPanel);

        // SEQUENCE PANEL
                
        SynthPanel sequence = new SynthPanel(this);
        synthPanels[panel++] = sequence;

        vbox = new VBox();
        vbox.add(addSequencer(1, Style.COLOR_C()));
        vbox.add(addSequencerTrack(1, 1, Style.COLOR_A()));
        vbox.add(addSequencerTrack(1, 2, Style.COLOR_B()));
        vbox.add(addSequencerTrack(1, 3, Style.COLOR_A()));
        vbox.add(addSequencerTrack(1, 4, Style.COLOR_B()));
        
        sequence.add(vbox, BorderLayout.CENTER);
        sequence.makePasteable("layer1");
        addTab("Sequencer A", sequence);
        
        
        sequence = new SynthPanel(this);
        synthPanels[panel++] = sequence;

        vbox = new VBox();
        vbox.add(addSequencer(2, Style.COLOR_C()));
        vbox.add(addSequencerTrack(2, 1, Style.COLOR_A()));
        vbox.add(addSequencerTrack(2, 2, Style.COLOR_B()));
        vbox.add(addSequencerTrack(2, 3, Style.COLOR_A()));
        vbox.add(addSequencerTrack(2, 4, Style.COLOR_B()));
        
        sequence.add(vbox, BorderLayout.CENTER);
        sequence.makePasteable("layer2");
        addTab("Sequencer B", sequence);
        
        
        model.set("name", "Untitled");
        model.set("bank", 0);
        model.set("number", 0);
                
        loadDefaults();        
        }
                
    public String getDefaultResourceFileName() { return "DSIProphet08.init"; }
    public String getHTMLResourceFileName() { return "DSIProphet08.html"; }

    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        JComboBox bank = new JComboBox(BANKS);
        int num = model.get("number") + 1;
        JTextField number = new JTextField("" + (num < 10 ? "00" : (num < 100 ? "0" : "")) + num, 3);
        
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
        
    /** Add the global patch category (name, id, number, etc.) */
    public JComponent addNameGlobal(Color color)
        {
        Category globalCategory = new Category(this, getSynthName(), color);
        globalCategory.makeUnresettable();
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
                                
        VBox vbox = new VBox();
        comp = new PatchDisplay(this, 4);
        vbox.add(comp);
        
        comp = new StringComponent("Patch Name", this, "name", 16, "Name must be up to 16 characters.")
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

        vbox = new VBox();
        params = KEYBOARD_MODES;
        comp = new Chooser("Keyboard Mode", this, "keyboardmode", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Split Point", this, "splitpoint", color, 0, 127)
            {
            public String map(int val)
                {
                return (NOTES[val % 12] + " " + (val / 12));
                }
            };
        hbox.add(comp);


//        hbox.addLast(Strut.makeHorizontalStrut(100));

        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }


    public JComponent addGlobal(int layer, Color color)
        {
        Category category = new Category(this, "Performance", color);
        category.makePasteable("layer" + layer);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();

        HBox hbox2 = new HBox();

        JComponent c1 = comp = new CheckBox("Arpeggiator", this, "layer" + layer + "arpeggiator");
        hbox2.add(comp);
        
        params = ARPEGGIATOR_MODES;
        JComponent c2 = comp = new Chooser("Arpeggiator Mode", this, "layer" + layer + "arpeggiatormode", params);
        hbox2.add(comp);
        
        params = CLOCK_DIVIDES;
        JComponent c3 = comp = new Chooser("Clock Divide", this, "layer" + layer + "clockdivide", params);
        hbox2.add(comp);

        vbox.add(hbox2);

        hbox2 = new HBox();
                
        comp = new CheckBox("Unison", this, "layer" + layer + "unison");
        comp.setPreferredSize(c1.getPreferredSize());
        hbox2.add(comp);
        
        params = KEY_MODES;
        comp = new Chooser("Unison Assign", this, "layer" + layer + "unisonkeymode", params);
        comp.setPreferredSize(c2.getPreferredSize());
        hbox2.add(comp);

        params = UNISON_MODES;
        comp = new Chooser("Unison Mode", this, "layer" + layer + "unisonmode", params);
        comp.setPreferredSize(c3.getPreferredSize());
        hbox2.add(comp);

        vbox.add(hbox2);

        hbox.add(vbox);

        comp = new LabelledDial("BPM", this, "layer" + layer + "tempo", color, 30, 250);
        ((LabelledDial)comp).addAdditionalLabel("Tempo");
        hbox.add(comp);

        comp = new LabelledDial("Pitch Bend", this, "layer" + layer + "pitchbendrange", color, 0, 12);
        ((LabelledDial)comp).addAdditionalLabel("Range");
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
                


    public JComponent addOscillatorGlobal(int layer, Color color)
        {
        Category category = new Category(this, "Oscillators", color);
        category.makePasteable("layer" + layer);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = GLIDE_MODES;
        comp = new Chooser("Glide Mode", this, "layer" + layer + "glidemode", params);
        vbox.add(comp);

        comp = new CheckBox("Sync", this, "layer" + layer + "sync");
        vbox.add(comp);
        
        hbox.add(vbox);

        comp = new LabelledDial("Slop", this, "layer" + layer + "slop", color, 0, 5);
        hbox.add(comp);

        comp = new LabelledDial("Mix", this, "layer" + layer + "mix", color, 0, 127)
            {
            public String map(int val)
                {
                // is this right?
                if (val == 64) return "--";
                else if (val < 64) return "< " + (64 - val);
                else return "" + (val - 64) + " >";
                }
            public boolean isSymmetric() { return true; }
            };
        ((LabelledDial)comp).addAdditionalLabel("Osc 1 <> 2");
        hbox.add(comp);

        comp = new LabelledDial("Noise", this, "layer" + layer + "noise", color, 0, 127);
        hbox.add(comp);
       
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
                
    /** Add an Oscillator category */
    public JComponent addOscillator(int layer, int osc, Color color)
        {
        Category category = new Category(this, "Oscillator " + osc, color);
        category.makePasteable("layer" + layer + "dco" + osc);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
               
        VBox vbox = new VBox();
        comp = new CheckBox("Key", this, "layer" + layer + "dco" + osc + "key");
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Frequency", this, "layer" + layer + "dco" + osc + "frequency", color, 0, 120)
            {
            public String map(int val)
                {
                return (NOTES[val % 12] + " " + (val / 12 ));
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Fine Tune", this, "layer" + layer + "dco" + osc + "finetune", color, 0, 100, 50)
            {
            public boolean isSymmetric() { return true; }
            };
        hbox.add(comp);
                
        comp = new LabelledDial("Shape", this, "layer" + layer + "dco" + osc + "shape", color, 0, 103)
            {
            public String map(int val)
                {
                if (val == 0) return "Off";
                else if (val == 1) return "Saw";
                else if (val == 2) return "Tri";
                else if (val == 3) return "Saw/Tri";
                else return "PW " + (val - 4);
                }
            };
        getModel().setMetricMin("layer" + layer + "dco" + osc + "shape", 4);
        hbox.add(comp);

        comp = new LabelledDial("Glide", this, "layer" + layer + "dco" + osc + "glide", color, 0, 127);
        hbox.add(comp);
                
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
        

    public JComponent addFilter(int layer, Color color)
        {
        Category category = new Category(this, "Filter", color);
        category.makePasteable("layer" + layer + "vcf");

        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        
        comp = new CheckBox("4-Pole", this, "layer" + layer + "vcfpoles");
        vbox.add(comp);
        hbox.add(vbox);
       
        comp = new LabelledDial("Frequency", this, "layer" + layer + "vcffrequency", color, 0, 164);
        hbox.add(comp);
                
        comp = new LabelledDial("Resonance", this, "layer" + layer + "vcfresonance", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Keyboard", this, "layer" + layer + "vcfkeyboardamount", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);
                
        comp = new LabelledDial("Audio", this, "layer" + layer + "vcfaudiomodulation", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Modulation");
        hbox.add(comp);
                                
        category.add(hbox, BorderLayout.WEST);
        return category;
        }

    public JComponent addEnvelope(int layer, int env, Color color)
        {
        Category category = new Category(this, 
                (env == FILTER_ENVELOPE ? "Filter Envelope" : 
                (env == AMPLIFIER_ENVELOPE ? "Amplifier Envelope" : "Envelope 3")), color);
        category.makePasteable("layer" + layer + "env" + env);
                
        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        if (env == THIRD_ENVELOPE)
            {
            params = MODULATION_DESTINATIONS;
            comp = new Chooser("Mod Destination", this, "layer" + layer + "env" + env + "moddestination", params);
            vbox.add(comp);

            comp = new CheckBox("Repeat", this, "layer" + layer + "env" + env + "repeat");
            vbox.add(comp);
            hbox.add(vbox);
            }
        
        comp = new LabelledDial("Delay", this, "layer" + layer + "env" + env + "delay", color, 0, 127);
        hbox.add(comp);
                
        comp = new LabelledDial("Attack", this, "layer" + layer + "env" + env + "attack", color, 0, 127);
        hbox.add(comp);
                
        comp = new LabelledDial("Decay", this, "layer" + layer + "env" + env + "decay", color, 0, 127);
        hbox.add(comp);
                
        comp = new LabelledDial("Sustain", this, "layer" + layer + "env" + env + "sustain", color, 0, 127);
        hbox.add(comp);
                
        comp = new LabelledDial("Release", this, "layer" + layer + "env" + env + "release", color, 0, 127);
        hbox.add(comp);
 
        comp = new LabelledDial("Envelope", this, "layer" + layer + "env" + env + "amount", color, 
            0, (env == AMPLIFIER_ENVELOPE ? 127 : 254), (env == AMPLIFIER_ENVELOPE ? 0 : 127))
            {
            public boolean isSymmetric() { return env != AMPLIFIER_ENVELOPE; }
            };        
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);
                                
        comp = new LabelledDial("Envelope", this, "layer" + layer + "env" + env + "velocityamount", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Velocity");
        hbox.add(comp);
                                
                
        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "layer" + layer + "env" + env + "delay", "layer" + layer + "env" + env + "attack", "layer" + layer + "env" + env + "decay", null,"layer" + layer +  "env" + env + "release" },
            new String[] { null, null, null, "layer" + layer + "env" + env + "sustain", "layer" + layer + "env" + env + "sustain", null },
            new double[] { 0, 0.2/127.0, 0.2/127.0, 0.2 / 127.0,  0.2, 0.2/127.0},
            new double[] { 0, 0, 1.0, 1.0 / 127.0, 1.0/127.0, 0 });
        hbox.addLast(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addAmplifier(int layer, Color color)
        {
        Category category = new Category(this, "Amplifier", color);
        category.makePasteable("layer" + layer + "vca");
                
        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();
        VBox vbox = new VBox();
        
               
        comp = new LabelledDial("Initial Level", this, "layer" + layer + "vcainitiallevel", color, 0, 127);
        hbox.add(comp);
                
        comp = new LabelledDial("Spread", this, "layer" + layer + "vcaoutputspread", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Voice", this, "layer" + layer + "vcavoicevolume", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Volume");
        hbox.add(comp);

        category.add(hbox, BorderLayout.WEST);
        return category;
        }


    public JComponent addExternalModulation(int layer, String title, String key, Color color)
        {
        Category category = new Category(this, title, color);
        category.makePasteable("layer" + layer + "key");
                
        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        params = MODULATION_DESTINATIONS;
        comp = new Chooser("Destination", this, "layer" + layer + key + "destination", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Amount", this, "layer" + layer + key + "amount", color, 0, 254, 127)
            {
            public boolean isSymmetric() { return true; }
            };
        hbox.add(comp);

        category.add(hbox, BorderLayout.WEST);
        return category;
        }


    public JComponent addSequencerTrack(int layer, final int track, Color color)
        {
        Category category = new Category(this, "Track " + track, color);
        category.makeDistributable("layer" + layer + "track" + track);
        category.makePasteable("layer" + layer + "track" + track);
                
        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        for(int i = 1; i <= 16; i++)
            {
            comp = new LabelledDial("" + i, this, "layer" + layer + "track" + track + "note" + i, color, 0, (track == 1 ? 127 : 126))
                {
                public String map(int val)
                    {
                    if (val <= 125)
                        {
                        String key = "layer" + layer + "track" + track + "destination";
                        int type = model.get(key, 0);
                        if (type == 1 || type == 2 || type == 3)  // oscillator frequencies
                            {
                            return (NOTES[val % 12] + " " + (val / 12 ));
                            }
                        else                                                                      // other stuff
                            {
                            return "" + val;
                            }
                        }
                    else if (val == 126) return "Reset";
                    else return "Rest";
                    }
                };
        	getModel().setMetricMax("layer" + layer + "track" + track + "note" + i, 125);  // Reset and Rest are non-metric
            hbox.add(comp);
            }
        
        category.add(hbox, BorderLayout.WEST);
        return category;
        }




    /** Add an LFO category */
    public JComponent addLFO(int layer, final int lfo, Color color)
        {
        Category category = new Category(this, "LFO " + lfo, color);
        category.makePasteable("layer" + layer + "lfo" + lfo);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();

        params = LFO_SHAPES;
        comp = new Chooser("Shape", this, "layer" + layer + "lfo" + lfo + "shape", params);
        vbox.add(comp);

        params = MODULATION_DESTINATIONS;
        comp = new Chooser("Mod Destination", this, "layer" + layer + "lfo" + lfo + "moddestination", params);
        vbox.add(comp);

        comp = new CheckBox("Key Sync", this, "layer" + layer + "lfo" + lfo + "keysync");
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Frequency", this, "layer" + layer + "lfo" + lfo + "frequency", color, 0, 166)
            {
            public String map(int val)
                {
                if (val <= 150) return "" + val;
                else return LFO_FREQUENCIES[val - 151];
                }
            };
        // this is a tough call, as the stuff above 150 is metric, just in a different way
        getModel().setMetricMax("layer" + layer + "lfo" + lfo + "frequency", 150);
        hbox.add(comp);
                
        comp = new LabelledDial("Amount", this, "layer" + layer + "lfo" + lfo + "amount", color, 0, 127);
        hbox.add(comp);
                                
        category.add(hbox, BorderLayout.WEST);
        return category;
        }


    /** Add the Modulation category */
    public JComponent addModulation(int layer, int mod, Color color)
        {
        Category category  = new Category(this, "Modulation " + mod, color);
        category.makePasteable("layer" + layer + "mod" + mod);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();

        params = MODULATION_SOURCES;
        comp = new Chooser("Source", this, "layer" + layer + "mod" + mod + "source", params);
        vbox.add(comp);

        params = MODULATION_DESTINATIONS;
        comp = new Chooser("Destination", this, "layer" + layer + "mod" + mod + "destination", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Amount", this, "layer" + layer + "mod" + mod + "amount", color, 0, 254, 127)
            {
            public boolean isSymmetric() { return true; }
            };              
        hbox.add(comp);
                                    
        category.add(hbox, BorderLayout.WEST);
        return category;
        }

    /** Add the Modulation category */
    public JComponent addSequencer(int layer, Color color)
        {
        final Category category  = new Category(this, "Sequencer", color);
        category.makePasteable("layer" + layer);
        category.makeDistributable("layer" + layer);
                       
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();        
        VBox vbox = new VBox();

        comp = new CheckBox("On", this, "layer" + layer + "sequencer");
        hbox.add(comp);

        params = SEQUENCER_TRIGGERS;
        comp = new Chooser("Trigger", this, "layer" + layer + "sequencertrigger", params);
        hbox.add(comp);

        for(int i = 1; i <= 4; i++)
            {
            vbox = new VBox();
            params = MODULATION_DESTINATIONS;
            comp = new Chooser("Track " + i + " Destination", this, "layer" + layer + "track" + i + "destination", params)
                {
                public void update(String key, Model model)
                    {
                    super.update(key, model);
                    Component component = SwingUtilities.getRoot(category);
                    if (component != null) 
                        component.repaint();
                    }
                };
            vbox.add(comp);
            hbox.add(vbox);
            }

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }





    /** Map of parameter -> index in the allParameters array. */
    HashMap parametersToIndex = new HashMap();


    /** List of all DSI Prophet 08 parameters in order. */
                
    final static String[] parameters = new String[]
    {
    "layer1dco1frequency",    
    "layer1dco1finetune",    
    "layer1dco1shape",    
    "layer1dco1glide",    
    "layer1dco1key",    
    "layer1dco2frequency",    
    "layer1dco2finetune",    
    "layer1dco2shape",    
    "layer1dco2glide",    
    "layer1dco2key",    

    "layer1sync",    
    "layer1glidemode",    
    "layer1slop",    
    "layer1mix",    
    "layer1noise",    

    "layer1vcffrequency",    
    "layer1vcfresonance",    
    "layer1vcfkeyboardamount",    
    "layer1vcfaudiomodulation",    
    "layer1vcfpoles",    

    "layer1env1amount",    
    "layer1env1velocityamount",    
    "layer1env1delay",    
    "layer1env1attack",    
    "layer1env1decay",    
    "layer1env1sustain",    
    "layer1env1release",    

    "layer1vcainitiallevel",    
    "layer1vcaoutputspread",    
    "layer1vcavoicevolume",    

    "layer1env2amount",    
    "layer1env2velocityamount",    
    "layer1env2delay",    
    "layer1env2attack",    
    "layer1env2decay",    
    "layer1env2sustain",    
    "layer1env2release",    


    "layer1lfo1frequency",    
    "layer1lfo1shape",    
    "layer1lfo1amount",    
    "layer1lfo1moddestination",    
    "layer1lfo1keysync",    

    "layer1lfo2frequency",    
    "layer1lfo2shape",    
    "layer1lfo2amount",    
    "layer1lfo2moddestination",    
    "layer1lfo2keysync",    

    "layer1lfo3frequency",    
    "layer1lfo3shape",    
    "layer1lfo3amount",    
    "layer1lfo3moddestination",    
    "layer1lfo3keysync",    

    "layer1lfo4frequency",    
    "layer1lfo4shape",    
    "layer1lfo4amount",    
    "layer1lfo4moddestination",    
    "layer1lfo4keysync",

    "layer1env3moddestination",    
    "layer1env3amount",    
    "layer1env3velocityamount",    
    "layer1env3delay",    
    "layer1env3attack",    
    "layer1env3decay",    
    "layer1env3sustain",    
    "layer1env3release",  

    "layer1mod1source",    
    "layer1mod1amount",    
    "layer1mod1destination",    

    "layer1mod2source",    
    "layer1mod2amount",    
    "layer1mod2destination",    

    "layer1mod3source",    
    "layer1mod3amount",    
    "layer1mod3destination",    

    "layer1mod4source",    
    "layer1mod4amount",    
    "layer1mod4destination",    

    "layer1track1destination",    
    "layer1track2destination",    
    "layer1track3destination",    
    "layer1track4destination",    

    "layer1wheelamount",    
    "layer1wheeldestination",    
    "layer1pressureamount",    
    "layer1pressuredestination",    
    "layer1breathamount",    
    "layer1breathdestination",    
    "layer1velocityamount",    
    "layer1velocitydestination",    
    "layer1footamount",    
    "layer1footdestination",    

    "layer1tempo",    
    "layer1clockdivide",    
    "layer1pitchbendrange",    
    "layer1sequencertrigger",    
    "layer1unisonmode",    
    "layer1unisonkeymode",    
    "layer1arpeggiatormode",    

    "layer1env3repeat",    
    "layer1unison",    
    "layer1arpeggiator",    
    "layer1sequencer",    

    "---",
    "---",
    "---",
    "---",
    "---",
    "---",
    "---",
    "---",
    "---",
    "---",
    "---",
    "---",
    "---",
    "---",
    "---",
    "---",

    "splitpoint",
    "keyboardmode",

    "layer1track1note1",    
    "layer1track1note2",    
    "layer1track1note3",    
    "layer1track1note4",    
    "layer1track1note5",    
    "layer1track1note6",    
    "layer1track1note7",    
    "layer1track1note8",    
    "layer1track1note9",    
    "layer1track1note10",    
    "layer1track1note11",    
    "layer1track1note12",    
    "layer1track1note13",    
    "layer1track1note14",    
    "layer1track1note15",    
    "layer1track1note16",    
    "layer1track2note1",    
    "layer1track2note2",    
    "layer1track2note3",    
    "layer1track2note4",    
    "layer1track2note5",    
    "layer1track2note6",    
    "layer1track2note7",    
    "layer1track2note8",    
    "layer1track2note9",    
    "layer1track2note10",    
    "layer1track2note11",    
    "layer1track2note12",    
    "layer1track2note13",    
    "layer1track2note14",    
    "layer1track2note15",    
    "layer1track2note16",    
    "layer1track3note1",    
    "layer1track3note2",    
    "layer1track3note3",    
    "layer1track3note4",    
    "layer1track3note5",    
    "layer1track3note6",    
    "layer1track3note7",    
    "layer1track3note8",    
    "layer1track3note9",    
    "layer1track3note10",    
    "layer1track3note11",    
    "layer1track3note12",    
    "layer1track3note13",    
    "layer1track3note14",    
    "layer1track3note15",    
    "layer1track3note16",    
    "layer1track4note1",    
    "layer1track4note2",    
    "layer1track4note3",    
    "layer1track4note4",    
    "layer1track4note5",    
    "layer1track4note6",    
    "layer1track4note7",    
    "layer1track4note8",    
    "layer1track4note9",    
    "layer1track4note10",    
    "layer1track4note11",    
    "layer1track4note12",    
    "layer1track4note13",    
    "layer1track4note14",    
    "layer1track4note15",    
    "layer1track4note16",    

    "---",
    "---",
    "---",
    "---",
    "---",
    "---",
    "---",
    "---",
    "---",
    "---",
    "---",
    "---",
    "---",
    "---",
    "---",
    "---",

    "layer2dco1frequency",    
    "layer2dco1finetune",    
    "layer2dco1shape",    
    "layer2dco1glide",    
    "layer2dco1key",    
    "layer2dco2frequency",    
    "layer2dco2finetune",    
    "layer2dco2shape",    
    "layer2dco2glide",    
    "layer2dco2key",    

    "layer2sync",    
    "layer2glidemode",    
    "layer2slop",    
    "layer2mix",    
    "layer2noise",    

    "layer2vcffrequency",    
    "layer2vcfresonance",    
    "layer2vcfkeyboardamount",    
    "layer2vcfaudiomodulation",    
    "layer2vcfpoles",    

    "layer2env1amount",    
    "layer2env1velocityamount",    
    "layer2env1delay",    
    "layer2env1attack",    
    "layer2env1decay",    
    "layer2env1sustain",    
    "layer2env1release",    

    "layer2vcainitiallevel",    
    "layer2vcaoutputspread",    
    "layer2vcavoicevolume",    

    "layer2env2amount",    
    "layer2env2velocityamount",    
    "layer2env2delay",    
    "layer2env2attack",    
    "layer2env2decay",    
    "layer2env2sustain",    
    "layer2env2release",    


    "layer2lfo1frequency",    
    "layer2lfo1shape",    
    "layer2lfo1amount",    
    "layer2lfo1moddestination",    
    "layer2lfo1keysync",    

    "layer2lfo2frequency",    
    "layer2lfo2shape",    
    "layer2lfo2amount",    
    "layer2lfo2moddestination",    
    "layer2lfo2keysync",    

    "layer2lfo3frequency",    
    "layer2lfo3shape",    
    "layer2lfo3amount",    
    "layer2lfo3moddestination",    
    "layer2lfo3keysync",    

    "layer2lfo4frequency",    
    "layer2lfo4shape",    
    "layer2lfo4amount",    
    "layer2lfo4moddestination",    
    "layer2lfo4keysync",

    "layer2env3moddestination",    
    "layer2env3amount",    
    "layer2env3velocityamount",    
    "layer2env3delay",    
    "layer2env3attack",    
    "layer2env3decay",    
    "layer2env3sustain",    
    "layer2env3release",  


    "layer2mod1source",    
    "layer2mod1amount",    
    "layer2mod1destination",    

    "layer2mod2source",    
    "layer2mod2amount",    
    "layer2mod2destination",    

    "layer2mod3source",    
    "layer2mod3amount",    
    "layer2mod3destination",    

    "layer2mod4source",    
    "layer2mod4amount",    
    "layer2mod4destination",    

    "layer2track1destination",    
    "layer2track2destination",    
    "layer2track3destination",    
    "layer2track4destination",    


    "layer2wheelamount",    
    "layer2wheeldestination",    
    "layer2pressureamount",    
    "layer2pressuredestination",    
    "layer2breathamount",    
    "layer2breathdestination",    
    "layer2velocityamount",    
    "layer2velocitydestination",    
    "layer2footamount",    
    "layer2footdestination",    

    "layer2tempo",    
    "layer2clockdivide",    
    "layer2pitchbendrange",    
    "layer2sequencertrigger",    
    "layer2unisonmode",    
    "layer2unisonkeymode",    
    "layer2arpeggiatormode",    

    "layer2env3repeat",    
    "layer2unison",    
    "layer2arpeggiator",    
    "layer2sequencer",    

    "---",
    "---",
    "---",
    "---",
    "---",
    "---",
    "---",
    "---",
    "---",
    "---",
    "---",
    "---",
    "---",
    "---",
    "---",
    "---",

    "---",
    "---",

    "layer2track1note1",    
    "layer2track1note2",    
    "layer2track1note3",    
    "layer2track1note4",    
    "layer2track1note5",    
    "layer2track1note6",    
    "layer2track1note7",    
    "layer2track1note8",    
    "layer2track1note9",    
    "layer2track1note10",    
    "layer2track1note11",    
    "layer2track1note12",    
    "layer2track1note13",    
    "layer2track1note14",    
    "layer2track1note15",    
    "layer2track1note16",    
    "layer2track2note1",    
    "layer2track2note2",    
    "layer2track2note3",    
    "layer2track2note4",    
    "layer2track2note5",    
    "layer2track2note6",    
    "layer2track2note7",    
    "layer2track2note8",    
    "layer2track2note9",    
    "layer2track2note10",    
    "layer2track2note11",    
    "layer2track2note12",    
    "layer2track2note13",    
    "layer2track2note14",    
    "layer2track2note15",    
    "layer2track2note16",    
    "layer2track3note1",    
    "layer2track3note2",    
    "layer2track3note3",    
    "layer2track3note4",    
    "layer2track3note5",    
    "layer2track3note6",    
    "layer2track3note7",    
    "layer2track3note8",    
    "layer2track3note9",    
    "layer2track3note10",    
    "layer2track3note11",    
    "layer2track3note12",    
    "layer2track3note13",    
    "layer2track3note14",    
    "layer2track3note15",    
    "layer2track3note16",    
    "layer2track4note1",    
    "layer2track4note2",    
    "layer2track4note3",    
    "layer2track4note4",    
    "layer2track4note5",    
    "layer2track4note6",    
    "layer2track4note7",    
    "layer2track4note8",    
    "layer2track4note9",    
    "layer2track4note10",    
    "layer2track4note11",    
    "layer2track4note12",    
    "layer2track4note13",    
    "layer2track4note14",    
    "layer2track4note15",    
    "layer2track4note16",    

    "---",
    "---",
    "---",
    "---",
    "---",
    "---",
    "---",
    "---",
    "---",
    "---",
    "---",
    "---",
    "---",
    "---",
    "---",
    "---"
    };
    
    

    public Object[] emitAll(String key)
        {
        if (key.equals("bank")) return new Object[0];  // this is not emittable
        if (key.equals("number")) return new Object[0];  // this is not emittable

        int index;
        int value;
        
        if (key.equals("name"))
            {
            Object[] ret = new Object[4 * 16];
            char[] name = (model.get("name", "Untitled") + "                ").toCharArray();
            for(int i = 0; i < 16; i++)
                {
                Object[] nrpn = buildNRPN(getChannelOut(), i + 184, name[i]);
                System.arraycopy(nrpn, 0, ret, i * 4, 4);
                }
            return ret;
            }
        else 
            {
            return buildNRPN(
                getChannelOut(),
                ((Integer)(parametersToIndex.get(key))).intValue(),
                model.get(key, 0));
            }
        }

/*
  public static int[] CC_PARAM_STARTS = new int[] { 14, 20, 75, 85, 102 };
  public static String[][] CC_PARAMS = 
  new String[][]
  {
  "layer1tempo",    
  "layer1clockdivide",    
  },
  {
  "layer1dco1frequency", 
  "layer1dco1finetune",    
  "layer1dco1shape",    
  "layer1dco1glide",    
  "layer1dco2frequency",    
  "layer1dco2finetune",    
  "layer1dco2shape",    
  "layer1dco2glide",    
  "layer1mix",    
  "layer1noise",    
  },
  {
  "layer1env2sustain",    
  "layer1env2release",    
  "layer1env3sustain",    
  "layer1env3release",  
  },
  {
  "layer1env3moddestination",    
  "layer1env3amount",    
  "layer1env3velocityamount",    
  "layer1env3delay",    
  "layer1env3attack",    
  "layer1env3decay",    
  },
  {
  "layer1vcffrequency",    
  "layer1vcfresonance",    
  "layer1vcfkeyboardamount",    
  "layer1vcfaudiomodulation",    
  "layer1env1amount",    
  "layer1env1velocityamount",    
  "layer1env1delay",    
  "layer1env1attack",    
  "layer1env1decay",    
  "layer1env1sustain",    
  "layer1env1release",    
  "layer1vcainitiallevel",    
  "layer1vcaoutputspread",    
  "layer1env2amount",    
  "layer1env2velocityamount",    
  "layer1env2delay",    
  "layer1env2attack",    
  "layer1env2decay",   
  }
  };
*/      

    public void handleSynthCCOrNRPN(Midi.CCData data)
        {
        /*
        // Handle incoming CC
        if (data.type == CCDATA_TYPE_RAW_CC)
        {
        for(int i = 0; i < CC_PARAM_STARTS.length; i++)
        {
        if (data.number >= CC_PARAM_STARTS[i] &&
        data.number < CC_PARAM_STARTS[i] + CC_PARAM_STARTS.length)
        {
        model.set(CC_PARAMS[i][data.number - CC_PARAM_STARTS[i]], data.value);
        break;
        }
        }
        }  
        else*/
        if (data.type == Midi.CCDATA_TYPE_NRPN && data.number <= 383)
            {
            if (data.number >= 184 && data.number <= 199)  // Name
                {
                char[] name = (model.get("name", "Untitled") + "                ").toCharArray();
                name[data.number - 184] = (char)(data.value);
                model.set("name", new String(name));
                }
            else
                {
                String key = parameters[data.number];
                if (key == "---")
                    return;
                else
                    model.set(key, data.value);
                } 
            }               
        }
    
    // converts all but last byte (F7)
    byte[] convertTo8Bit(byte[] data, int offset)
        {
        // How big?
        int size = (data.length - offset - 1) / 8 * 7;
        if ((data.length - offset - 1) % 8 > 0)
            size += ((data.length - offset - 1) % 8 - 1);           
        byte[] newd = new byte[size];
        
        int j = 0;
        for(int i = offset; i < data.length; i += 8)
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
        int size = (data.length) / 7 * 8;
        if (data.length % 7 > 0)
            size += (1 + data.length % 7);
        byte[] newd = new byte[size];
        
        int j = 0;
        for(int i = 0; i < data.length; i+=7)
            {
            for(int x = 0; x < 7; x++)
                {
                if (j + x + 1 < newd.length)
                    {
                    newd[j + x + 1] = (byte)(data[i + x] & 127);
                    // Note that I have do to & 1 because data[i + x] is promoted to an int
                    // first, and then shifted, and that makes a BIG NUMBER which requires
                    // me to mask out the 1.  I hope this isn't the case for other stuff (which
                    // is typically 7-bit).
                    newd[j] = (byte)(newd[j] | (((data[i + x] >>> 7) & 1) << x));
                    }
                }
            j += 8;
            }
        return newd;
        }

/*
  int[] highBit = new int[]
  {
  15, 215,                // Filter Frequency
  20, 220,                // Filter Envelope Amount
  37, 237,                // LFO1 Frequency
  42, 242,                // LFO2 Frequency
  47, 247,                // LFO3 Frequency
  52, 252,                // LFO4 Frequency
  58, 258,                // ENV3 Amount
  66, 266,                // Mod1 Amount
  69, 269,                // Mod2 Amount
  72, 272,                // Mod3 Amount
  75, 275,                // Mod4 Amount
  81, 281,                // Mod Wheel Amount
  83, 283,                // Pressure Amount
  85, 285,                // Breath Amount
  87, 287,                // Velocity Amount
  89, 289,                // Foot Control Amount
  91, 291,                // BPM Tempo
  };

  // yeah, yeah, O(n)
  boolean isHighBit(int number)
  {
  for(int i = 0; i < highBit.length; i++)
  if (highBit[i] == number) 
  return true;
  return false;
  }               
*/      
    public int parse(byte[] data, boolean fromFile)
        {
        // unfortunately, the Prophet '08 doesn't provide number/bank info
        // with its edit buffer data dump.  :-(
        
        if (data[3] == 0x02)  // program data only, not (0x03) edit buffer
            {
            model.set("bank", data[4]);
            model.set("number", data[5]);
            }

        byte[] d = null;
        if (data[3] == 0x02)
            d = convertTo8Bit(data, 6);
        else if (data[3] == 0x03)
            d = convertTo8Bit(data, 4);
        else
            System.err.println("Warning (DSIProphet08): Unknown program data format " + data[3]);
        
        for(int i = 0; i < 384; i++)
            {
            if (!parameters[i].equals("---"))
                {
                // Note: DSI isn't 2's complement.  So everything in the model is being stored starting at 0
                int q = d[i];
                if (q < 0) q += 256;  // push to unsigned (not 2's complement)
                model.set(parameters[i], q);
                }
            }
        
        // handle name specially
        byte[] name = new byte[16];
        System.arraycopy(d, 184, name, 0, 16);
        try
            {
            model.set("name", new String(name, "US-ASCII"));
            }
        catch (UnsupportedEncodingException e)
            {
            e.printStackTrace();
            }
                
        revise();
        return PARSE_SUCCEEDED;
        }
    

    public byte[] emit(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        if (tempModel == null)
            tempModel = getModel();

        byte[] d = new byte[384];
        for(int i = 0; i < 384; i++)
            {
            if (!parameters[i].equals("---"))
                {
                // Note: DSI isn't 2's complement.  So everything in the model is being stored starting at 0
                int q = model.get(parameters[i], 0);
                if (q > 127) q -= 256;  // push to signed (not 2's complement)
                d[i] = (byte)q;
                }
            }
                        
        // handle name specially
        char[] name = (model.get("name", "Untitled") + "                " ).toCharArray();
        for(int i = 0; i < 16; i++)
            d[184 + i] = (byte)(name[i] & 127);
        
        byte[] data = convertTo7Bit(d);        
        
        if (toWorkingMemory)
            {
            byte[] emit = new byte[444];
            emit[0] = (byte)0xF0;
            emit[1] = (byte)0x01;  // DSI
            emit[2] = (byte)0x23;  // Prophet 08
            emit[3] = (byte)0x03;  // Edit Buffer Data Dump
            System.arraycopy(data, 0, emit, 4, data.length);
            emit[emit.length - 1] = (byte)0xF7;
            return emit;
            }
        else
            {
            byte[] emit = new byte[446];
            emit[0] = (byte)0xF0;
            emit[1] = (byte)0x01;  // DSI
            emit[2] = (byte)0x23;  // Prophet 08
            emit[3] = (byte)0x02;  // Program Data Dump
            emit[4] = (byte)tempModel.get("bank", 0);
            emit[5] = (byte)tempModel.get("number", 0);
            System.arraycopy(data, 0, emit, 6, data.length);
            emit[emit.length - 1] = (byte)0xF7;
            return emit;
            }
        }
        
        
    public void changePatch(Model tempModel)
        {
        changePatch(tempModel.get("bank"), tempModel.get("number"));
        }

    public void writeAllParameters(Model model)
        {
        performChangePatch(model);     // we need to be at the start for the Oberheim Matrix 1000
        tryToSendMIDI(emitAll(model, false, false));
        // we CANNOT do change patch after writing data -- it ruins the sysex write.  DSI Bug it would seem. 
        }

    public void changePatch(int bank, int number)
        {
        Object[] message = new Object[2];
        
        // bank select
        message[0] = buildCC(getChannelOut(), 32, bank)[0];
        // PC
        message[1] = buildPC(getChannelOut(), number)[0];
        
        tryToSendMIDI(message);
        }

    public byte[] requestCurrentDump()
        {
        byte[] data = new byte[5];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x01;   // DSI
        data[2] = (byte)0x23;   // Prophet '08
        data[3] = (byte)0x06;
        data[4] = (byte)0xF7;
        return data;
        }

    public byte[] requestDump(Model tempModel)
        {
        byte[] data = new byte[7];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x01;   // DSI
        data[2] = (byte)0x23;   // Prophet '08
        data[3] = (byte)0x05;
        data[4] = (byte)(tempModel.get("bank", 0));
        data[5] = (byte)(tempModel.get("number", 0));
        data[6] = (byte)0xF7;
        return data;
        }
                
    public static boolean recognize(byte[] data)
        {
        return ((data.length == 446 &&
                data[0] == (byte)0xF0 &&
                data[1] == (byte) 0x01 &&       // DSI
                data[2] == (byte) 0x23 &&       // Prophet 08
                data[3] == (byte) 0x02) ||      // Program Data Dump
                (data.length == 444 &&
                data[0] == (byte)0xF0 &&
                data[1] == (byte) 0x01 &&       // DSI
                data[2] == (byte) 0x23 &&       // Prophet 08
                data[3] == (byte) 0x03));       // Edit Buffer Data Dump
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
        }
        
    public static String getSynthName() { return "DSI Prophet '08"; }
    public String getPatchName(Model model) { return model.get("name", "Untitled"); }
    
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
        
        int number = (model.get("number") + 1);
        return ("" + (model.get("bank") + 1) + "-" + (number > 99 ? "" : (number > 9 ? "0" : "00")) + number);
        }



    final static String[] tetraParameters = new String[]
    {
    "layer1dco1frequency",    
    "layer1dco1finetune",    
    "layer1dco1shape",    
    "layer1dco1glide",    
    "layer1dco1key",   
     
    "layer1suboscillator1level",
    
    "layer1dco2frequency",    
    "layer1dco2finetune",    
    "layer1dco2shape",    
    "layer1dco2glide",    
    "layer1dco2key",
    
    "layer1suboscillator2level",

    "layer1sync",    
    "layer1glidemode",    
    "layer1slop",    
    
    "layer1pitchbendrange",    
    
    "layer1mix",    
    "layer1noise",    

	"layer1feedbackvolume",

	"layer1feedback gain",

    "layer1vcffrequency",    
    "layer1vcfresonance",    
    "layer1vcfkeyboardamount",    
    "layer1vcfaudiomodulation",    
    "layer1vcfpoles",    

    "layer1env1amount",    
    "layer1env1velocityamount",    
    "layer1env1delay",    
    "layer1env1attack",    
    "layer1env1decay",    
    "layer1env1sustain",    
    "layer1env1release",    

    "layer1vcainitiallevel",    
    
    "layer1env2amount",    
    "layer1env2velocityamount",    
    "layer1env2delay",    
    "layer1env2attack",    
    "layer1env2decay",    
    "layer1env2sustain",    
    "layer1env2release",    

    
    "layer1vcaoutputspread",    
    "layer1vcavoicevolume",    



    "layer1lfo1frequency",    
    "layer1lfo1shape",    
    "layer1lfo1amount",    
    "layer1lfo1moddestination",    
    "layer1lfo1keysync",    

    "layer1lfo2frequency",    
    "layer1lfo2shape",    
    "layer1lfo2amount",    
    "layer1lfo2moddestination",    
    "layer1lfo2keysync",    

    "layer1lfo3frequency",    
    "layer1lfo3shape",    
    "layer1lfo3amount",    
    "layer1lfo3moddestination",    
    "layer1lfo3keysync",    

    "layer1lfo4frequency",    
    "layer1lfo4shape",    
    "layer1lfo4amount",    
    "layer1lfo4moddestination",    
    "layer1lfo4keysync",

    "layer1env3moddestination",    
    "layer1env3amount",    
    "layer1env3velocityamount",    
    "layer1env3delay",    
    "layer1env3attack",    
    "layer1env3decay",    
    "layer1env3sustain",    
    "layer1env3release",  

    "layer1env3repeat",    

    "layer1mod1source",    
    "layer1mod1amount",    
    "layer1mod1destination",    

    "layer1mod2source",    
    "layer1mod2amount",    
    "layer1mod2destination",    

    "layer1mod3source",    
    "layer1mod3amount",    
    "layer1mod3destination",    

    "layer1mod4source",    
    "layer1mod4amount",    
    "layer1mod4destination",    

    "layer1wheelamount",    
    "layer1wheeldestination",    
    "layer1pressureamount",    
    "layer1pressuredestination",    
    "layer1breathamount",    
    "layer1breathdestination",    
    "layer1velocityamount",    
    "layer1velocitydestination",    
    "layer1footamount",    
    "layer1footdestination",  
    
    "layer1unisonmode",    
    "layer1unisonkeymode",    
    "layer1unison",    

	    
	"layer1pushitnote",
	"layer1pushitvelocity",
	"layer1pushitmode",

    "splitpoint",
    "keyboardmode",
	
    "layer1tempo",    
    "layer1clockdivide",    

    "layer1arpeggiatormode",    

    "layer1arpeggiator",    

    "layer1sequencertrigger",    
   
    "layer1sequencer",    
    
    // 107 ... 110
    
    "assignableparameter1",    
    "assignableparameter2",    
    "assignableparameter3",    
    "assignableparameter4",    

	// 115 ... 116

	"editorbyte",
	
	// 118 ... 119
    
    "layer1track1destination",
    
    // 121 ... 135
    
    "layer1track2destination",    
    
    // 137 ... 151
    
    "layer1track3destination",   
    
    // 153 ... 167
     
    "layer1track4destination",    

  	// 168 ... 183
  	
  	// name...
    };

    }
