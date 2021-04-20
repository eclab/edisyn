/***
    Copyright 2020 by Sean Luke
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
   A combo patch editor for the Dave Smith Instruments Tetra.
        
   @author Sean Luke
*/

public class DSITetraCombo extends Synth
    {
    /// Various collections of parameter names for pop-up menus
        
    public static final String[] MODULATION_SOURCES = new String[] { "Off", "Track 1", "Track 2", "Track 3", "Track 4", "LFO 1", "LFO 2", "LFO 3", "LFO 4", "Filter Env", "Amp Env", "Env 3", "Pitch Bend", "Mod Wheel", "Pressure", "MIDI Breath", "MIDI Foot", "MIDI Expression", "Velocity", "Note Number", "Noise", "AudioIn EnvFollow", "AudioIn PkHold" };
    public static final String[] MODULATION_DESTINATIONS = new String[] { "Off", "Osc 1 Freq", "Osc 2 Freq", "Osc 1&2 Freq", "Osc Mix", "Noise Level", "Osc 1 PW", "Osc 2 PW", "Osc 1&2 PW", "Filter Freq", "Filter Resonance", "Filter Audio Mod", "VCA Level", "Pan Spread", "LFO 1 Freq", "LFO 2 Freq", "LFO 3 Freq", "LFO 4 Freq", "All LFO Freqs", "LFO 1 Amt", "LFO 2 Amt", "LFO 3 Amt", "LFO 4 Amt", "All LFO Amts", "Filter Env Amt", "Amp Env Amt", "Env 3 Amt", "All Env Amts", "Env 1 Attack", "Env 2 Attack", "Env 3 Attack", "All Env Attacks", "Env 1 Decay", "Env 2 Decay", "Env 3 Decay", "All Env Decays", "Env 1 Release", "Env 2 Release", "Env 3 Release", "All Env Releases", "Mod 1 Amt", "Mod 2 Amt", "Mod 3 Amt", "Mod 4 Amt", "Feedback", "Sub Osc 1", "Sub Osc 2", "Feedback Gain" };
    public static final String[] MODULATION_DESTINATIONS_SEQ24 = new String[] { "Off", "Osc 1 Freq", "Osc 2 Freq", "Osc 1&2 Freq", "Osc Mix", "Noise Level", "Osc 1 PW", "Osc 2 PW", "Osc 1&2 PW", "Filter Freq", "Filter Resonance", "Filter Audio Mod", "VCA Level", "Pan Spread", "LFO 1 Freq", "LFO 2 Freq", "LFO 3 Freq", "LFO 4 Freq", "All LFO Freqs", "LFO 1 Amt", "LFO 2 Amt", "LFO 3 Amt", "LFO 4 Amt", "All LFO Amts", "Filter Env Amt", "Amp Env Amt", "Env 3 Amt", "All Env Amts", "Env 1 Attack", "Env 2 Attack", "Env 3 Attack", "All Env Attacks", "Env 1 Decay", "Env 2 Decay", "Env 3 Decay", "All Env Decays", "Env 1 Release", "Env 2 Release", "Env 3 Release", "All Env Releases", "Mod 1 Amt", "Mod 2 Amt", "Mod 3 Amt", "Mod 4 Amt", "Feedback", "Sub Osc 1", "Sub Osc 2", "Feedback Gain", "Slew" };
    public static final String[] OSC_SHAPES = new String[] { "Off", "Saw", "Tri", "Saw/Tri" };
    public static final String[] GLIDE_MODES = new String[] { "Fixed Rate", "Fixed Rate Auto", "Fixed Time", "Fixed Time Auto" };
    public static final String[] FILTER_POLES = new String[] { "2-Pole", "4-Pole" };
    public static final String[] NOTES = new String[] { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
    public static final String[] HALF_NOTES = new String[] { "C", "C+", "C#", "C#+", "D", "D+", "D#", "D#+", "E", "E+", "F", "F+", "F#", "F#+", "G", "G+", "G#", "G#+", "A", "A+", "A#", "A#+", "B", "B+" };

    // this one is obviously wrong, the documentation seems strange
    public static final String[] LFO_FREQUENCIES = new String[] { "32 S", "16 S", "8 S", "6 S", "4 S", "3 S", "2 S", "3 S/2", "1 S", "2 S/3", "S/2", "3 S", "S/4", "S/6", "S/8", "S/16" };
    public static final String[] LFO_SHAPES = new String[] { "Triangle", "Reverse Saw", "Sawtooth", "Square", "Random" };
    public static final String[] CLOCK_DIVIDES = new String[] { "Half Note", "Quarter Note", "8th Note", "8th Note Half Swing", "8th Note Full Swing", "8th Note Triplets", "16th Note", "16th Note Half Swing", "16th Note Full Swing", "16th Note Triplets", "32nd Note", "32nd Note Triplets", "64th Note Triplets" };
    public static final String[] SEQUENCER_TRIGGERS = new String[] { "Normal", "Normal No Reset", "No Gate", "No Gate, No Reset", "Key Step", "Audio In" };
    public static final String[] KEY_MODES = new String[] { "Low Note", "Low Note Retrigger", "High Note", "High Note Retrigger", "Last Note", "Last Note Retrigger" };
    public static final String[] UNISON_MODES = new String[] { "1 Voice", "All Voices", "All Voices Detune 1",  "All Voices Detune 2",  "All Voices Detune 3" };
    public static final String[] ARPEGGIATOR_MODES = new String[] { "Up", "Down", "Up/Down", "Assign", "Random", "2 Octaves Up", "2 Octaves Down", "2 Octaves UpDown", "2 Octaves Assign", "2 Octaves Random", "3 Octaves Up", "3 Octaves Down", "3 Octaves UpDown", "3 Octaves Assign", "3 Octaves Random" };
    public static final String[] KEYBOARD_MODES = new String[] { "Normal", "Stack", "Split" };
    public static final String[] PUSH_IT_MODES = new String[] { "Normal", "Toggle", "Audio In" };
    public static final String[] PRESETS = new String[] { "Saw", "Tri", "Saw/Tri", "Square", "Off" };
    public static final int[] PRESET_VALS = new int[] { 1, 2, 3, 54, 0 };
    public static final String[] VOICES = new String[] { "Voice 1", "Voice 2", "Voice 3", "Voice 4" };
    
    // Sysex machine IDs
    public static final byte TETRA_ID = 0x26;

    public static final int FILTER_ENVELOPE = 1;
    public static final int AMPLIFIER_ENVELOPE = 2;
    public static final int THIRD_ENVELOPE = 3;
    
    public static final int LOAD_ALL = 0;
    public static final int LOAD_A = 1;
    public static final int LOAD_B = 2;
    public static final int LOAD_C = 3;
    public static final int LOAD_D = 4;
    int load = LOAD_ALL;
    
    boolean sendAssignableParams;
    
    public DSITetraCombo()
        {
        int panel = 0;
        
        for(int i = 0; i < parameters.length; i++)
            {
            parametersToIndex.put(parameters[i], Integer.valueOf(i));
            }
                        
        /// SOUND PANEL
                
        SynthPanel soundPanel = new SynthPanel(this);
        
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        JComponent nameGlobal = addNameGlobal(1, Style.COLOR_GLOBAL());
        hbox.add(nameGlobal);
        hbox.add(addGlobal(1, Style.COLOR_B()));
        hbox.addLast(addTetra(1, Style.COLOR_B()));
        vbox.add(hbox);
        
        hbox = new HBox();
        hbox.add(addOscillatorGlobal(1, Style.COLOR_A()));
        hbox.add(addOscillator(1, 1, Style.COLOR_A()));
        hbox.addLast(addOscillator(1, 2, Style.COLOR_A()));
        vbox.add(hbox);
        
        hbox = new HBox();
        VBox vbox2 = new VBox(VBox.TOP_CONSUMES);
        HBox hbox2 = new HBox();
        hbox2.add(addSuboscillator(1, Style.COLOR_A()));
        hbox2.addLast(addFilter(1, Style.COLOR_B()));
        vbox2.addLast(hbox2);
        
        hbox2 = new HBox();
        hbox2.add(addFeedback(1, Style.COLOR_A()));
        hbox2.addLast(addAmplifier(1, Style.COLOR_C()));
        vbox2.add(hbox2);
        hbox.add(vbox2);

        vbox2 = new VBox(VBox.TOP_CONSUMES);
        vbox2.addLast(addEnvelope(1, FILTER_ENVELOPE, Style.COLOR_B()));
        vbox2.add(addEnvelope(1, AMPLIFIER_ENVELOPE, Style.COLOR_C()));
        hbox.addLast(vbox2);
        vbox.add(hbox);
        
        soundPanel.add(vbox, BorderLayout.CENTER);
        //        soundPanel.makePasteable("layer1");
        soundPanel.makePasteable("layer");
        soundPanel.setSendsAllParameters(false);
        addTab("Osc Filt 1", soundPanel);
                

        // MODULATION PANEL
                
        SynthPanel modulationPanel = new SynthPanel(this);
        
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
        //        modulationPanel.makePasteable("layer1");
        modulationPanel.makePasteable("layer");
        addTab("Mod 1", modulationPanel);


        // SEQUENCE PANEL
                
        SynthPanel sequence = new SynthPanel(this);

        vbox = new VBox();
        vbox.add(addSequencer(1, Style.COLOR_C()));
        vbox.add(addSequencerTrack(1, 1, Style.COLOR_A()));
        vbox.add(addSequencerTrack(1, 2, Style.COLOR_B()));
        vbox.add(addSequencerTrack(1, 3, Style.COLOR_A()));
        vbox.add(addSequencerTrack(1, 4, Style.COLOR_B()));
        
        sequence.add(vbox, BorderLayout.CENTER);
        //        sequence.makePasteable("layer1");
        sequence.makePasteable("layer");
        addTab("Seq 1", sequence);
        
        




               
        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        hbox = new HBox();
        nameGlobal = addNameGlobal(2, Style.COLOR_C());
        hbox.add(nameGlobal);
        hbox.add(addGlobal(2, Style.COLOR_B()));
        hbox.addLast(addTetra(2, Style.COLOR_B()));
        vbox.add(hbox);
        
        hbox = new HBox();
        hbox.add(addOscillatorGlobal(2, Style.COLOR_A()));
        hbox.add(addOscillator(2, 1, Style.COLOR_A()));
        hbox.addLast(addOscillator(2, 2, Style.COLOR_A()));
        vbox.add(hbox);
        
        hbox = new HBox();
        vbox2 = new VBox(VBox.TOP_CONSUMES);
        hbox2 = new HBox();
        hbox2.add(addSuboscillator(2, Style.COLOR_A()));
        hbox2.addLast(addFilter(2, Style.COLOR_B()));
        vbox2.addLast(hbox2);
        
        hbox2 = new HBox();
        hbox2.add(addFeedback(2, Style.COLOR_A()));
        hbox2.addLast(addAmplifier(2, Style.COLOR_C()));
        vbox2.add(hbox2);
        hbox.add(vbox2);

        vbox2 = new VBox(VBox.TOP_CONSUMES);
        vbox2.addLast(addEnvelope(2, FILTER_ENVELOPE, Style.COLOR_B()));
        vbox2.add(addEnvelope(2, AMPLIFIER_ENVELOPE, Style.COLOR_C()));
        hbox.addLast(vbox2);
        vbox.add(hbox);
        
        soundPanel.add(vbox, BorderLayout.CENTER);
        //        soundPanel.makePasteable("layer2");
        soundPanel.makePasteable("layer");
        soundPanel.setSendsAllParameters(false);
        addTab("Osc Filt 2", soundPanel);
                
                        

        modulationPanel = new SynthPanel(this);
        
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
        //        modulationPanel.makePasteable("layer2");
        modulationPanel.makePasteable("layer");
        addTab("Mod 2", modulationPanel);



        sequence = new SynthPanel(this);
        vbox = new VBox();
        vbox.add(addSequencer(2, Style.COLOR_C()));
        vbox.add(addSequencerTrack(2, 1, Style.COLOR_A()));
        vbox.add(addSequencerTrack(2, 2, Style.COLOR_B()));
        vbox.add(addSequencerTrack(2, 3, Style.COLOR_A()));
        vbox.add(addSequencerTrack(2, 4, Style.COLOR_B()));
        
        sequence.add(vbox, BorderLayout.CENTER);
        //        sequence.makePasteable("layer2");
        sequence.makePasteable("layer");
        addTab("Seq 2", sequence);
        





        
        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        hbox = new HBox();
        nameGlobal = addNameGlobal(3, Style.COLOR_C());
        hbox.add(nameGlobal);
        hbox.add(addGlobal(3, Style.COLOR_B()));
        hbox.addLast(addTetra(3, Style.COLOR_B()));
        vbox.add(hbox);
        
        hbox = new HBox();
        hbox.add(addOscillatorGlobal(3, Style.COLOR_A()));
        hbox.add(addOscillator(3, 1, Style.COLOR_A()));
        hbox.addLast(addOscillator(3, 2, Style.COLOR_A()));
        vbox.add(hbox);
        
        hbox = new HBox();
        vbox2 = new VBox(VBox.TOP_CONSUMES);
        hbox2 = new HBox();
        hbox2.add(addSuboscillator(3, Style.COLOR_A()));
        hbox2.addLast(addFilter(3, Style.COLOR_B()));
        vbox2.addLast(hbox2);
        
        hbox2 = new HBox();
        hbox2.add(addFeedback(3, Style.COLOR_A()));
        hbox2.addLast(addAmplifier(3, Style.COLOR_C()));
        vbox2.add(hbox2);
        hbox.add(vbox2);

        vbox2 = new VBox(VBox.TOP_CONSUMES);
        vbox2.addLast(addEnvelope(3, FILTER_ENVELOPE, Style.COLOR_B()));
        vbox2.add(addEnvelope(3, AMPLIFIER_ENVELOPE, Style.COLOR_C()));
        hbox.addLast(vbox2);
        vbox.add(hbox);
        
        soundPanel.add(vbox, BorderLayout.CENTER);
        //        soundPanel.makePasteable("layer3");
        soundPanel.makePasteable("layer");
        soundPanel.setSendsAllParameters(false);
        addTab("Osc Filt 3", soundPanel);
                

        modulationPanel = new SynthPanel(this);
        
        vbox = new VBox();
        
        hbox = new HBox();
        hbox.add(addExternalModulation(3, "Velocity", "velocity", Style.COLOR_C()));
        hbox.addLast(addEnvelope(3, THIRD_ENVELOPE, Style.COLOR_C()));
        vbox.add(hbox);
        
        hbox = new HBox();
        hbox.add(addExternalModulation(3, "Aftertouch", "pressure", Style.COLOR_C()));
        hbox.add(addExternalModulation(3, "Mod Wheel", "wheel", Style.COLOR_C()));
        hbox.add(addExternalModulation(3, "Breath", "breath", Style.COLOR_C()));
        hbox.addLast(addExternalModulation(3, "Foot Control", "foot", Style.COLOR_C()));
        vbox.add(hbox);
                
        for(int i = 1; i <= 4; i+=2)
            {
            hbox = new HBox();
            hbox.add(addModulation(3, i, Style.COLOR_A()));
            hbox.add(addModulation(3, i+1, Style.COLOR_A()));
            hbox.add(addLFO(3, i, Style.COLOR_B()));
            hbox.addLast(addLFO(3, i+1, Style.COLOR_B()));
            vbox.add(hbox);
            }

        modulationPanel.add(vbox, BorderLayout.CENTER);
        //        modulationPanel.makePasteable("layer3");
        modulationPanel.makePasteable("layer");
        addTab("Mod 3", modulationPanel);




        sequence = new SynthPanel(this);

        vbox = new VBox();
        vbox.add(addSequencer(3, Style.COLOR_C()));
        vbox.add(addSequencerTrack(3, 1, Style.COLOR_A()));
        vbox.add(addSequencerTrack(3, 2, Style.COLOR_B()));
        vbox.add(addSequencerTrack(3, 3, Style.COLOR_A()));
        vbox.add(addSequencerTrack(3, 4, Style.COLOR_B()));
        
        sequence.add(vbox, BorderLayout.CENTER);
        //        sequence.makePasteable("layer3");
        sequence.makePasteable("layer");
        addTab("Seq 3", sequence);
        
        



        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        hbox = new HBox();
        nameGlobal = addNameGlobal(4, Style.COLOR_C());
        hbox.add(nameGlobal);
        hbox.add(addGlobal(4, Style.COLOR_B()));
        hbox.addLast(addTetra(4, Style.COLOR_B()));
        vbox.add(hbox);
        
        hbox = new HBox();
        hbox.add(addOscillatorGlobal(4, Style.COLOR_A()));
        hbox.add(addOscillator(4, 1, Style.COLOR_A()));
        hbox.addLast(addOscillator(4, 2, Style.COLOR_A()));
        vbox.add(hbox);
        
        hbox = new HBox();
        vbox2 = new VBox(VBox.TOP_CONSUMES);
        hbox2 = new HBox();
        hbox2.add(addSuboscillator(4, Style.COLOR_A()));
        hbox2.addLast(addFilter(4, Style.COLOR_B()));
        vbox2.addLast(hbox2);
        
        hbox2 = new HBox();
        hbox2.add(addFeedback(4, Style.COLOR_A()));
        hbox2.addLast(addAmplifier(4, Style.COLOR_C()));
        vbox2.add(hbox2);
        hbox.add(vbox2);

        vbox2 = new VBox(VBox.TOP_CONSUMES);
        vbox2.addLast(addEnvelope(4, FILTER_ENVELOPE, Style.COLOR_B()));
        vbox2.add(addEnvelope(4, AMPLIFIER_ENVELOPE, Style.COLOR_C()));
        hbox.addLast(vbox2);
        vbox.add(hbox);
        
        soundPanel.add(vbox, BorderLayout.CENTER);
        //        soundPanel.makePasteable("layer4");
        soundPanel.makePasteable("layer");
        soundPanel.setSendsAllParameters(false);
        addTab("Osc Filt 4", soundPanel);
                


        modulationPanel = new SynthPanel(this);
        
        vbox = new VBox();
        
        hbox = new HBox();
        hbox.add(addExternalModulation(4, "Velocity", "velocity", Style.COLOR_C()));
        hbox.addLast(addEnvelope(4, THIRD_ENVELOPE, Style.COLOR_C()));
        vbox.add(hbox);
        
        hbox = new HBox();
        hbox.add(addExternalModulation(4, "Aftertouch", "pressure", Style.COLOR_C()));
        hbox.add(addExternalModulation(4, "Mod Wheel", "wheel", Style.COLOR_C()));
        hbox.add(addExternalModulation(4, "Breath", "breath", Style.COLOR_C()));
        hbox.addLast(addExternalModulation(4, "Foot Control", "foot", Style.COLOR_C()));
        vbox.add(hbox);
                
        for(int i = 1; i <= 4; i+=2)
            {
            hbox = new HBox();
            hbox.add(addModulation(4, i, Style.COLOR_A()));
            hbox.add(addModulation(4, i+1, Style.COLOR_A()));
            hbox.add(addLFO(4, i, Style.COLOR_B()));
            hbox.addLast(addLFO(4, i+1, Style.COLOR_B()));
            vbox.add(hbox);
            }

        modulationPanel.add(vbox, BorderLayout.CENTER);
        //        modulationPanel.makePasteable("layer4");
        modulationPanel.makePasteable("layer");
        addTab("Mod 4", modulationPanel);
        


        sequence = new SynthPanel(this);
        vbox = new VBox();
        vbox.add(addSequencer(4, Style.COLOR_C()));
        vbox.add(addSequencerTrack(4, 1, Style.COLOR_A()));
        vbox.add(addSequencerTrack(4, 2, Style.COLOR_B()));
        vbox.add(addSequencerTrack(4, 3, Style.COLOR_A()));
        vbox.add(addSequencerTrack(4, 4, Style.COLOR_B()));
        
        sequence.add(vbox, BorderLayout.CENTER);
        //        sequence.makePasteable("layer4");
        sequence.makePasteable("layer");
        addTab("Seq 4", sequence);
        
        
        model.set("name", "Untitled");
        model.set("number", 0);
                
        loadDefaults();        
        }
                
    public String getDefaultResourceFileName() { return "DSITetraCombo.init"; }
    public String getHTMLResourceFileName() { return "DSITetraCombo.html"; }

    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        int num = model.get("number") + 1;
        JTextField number = new JTextField("" + (num < 10 ? "00" : (num < 100 ? "0" : "")) + num, 3);
        
        while(true)
            {
            boolean result = showMultiOption(this, new String[] { "Patch Number"}, 
                new JComponent[] { number }, title, "Enter Patch number");
                
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
                                
            change.set("number", n);
                        
            return true;
            }
        }
        
    /** Add the global patch category (name, id, number, etc.) */
    public JComponent addNameGlobal(int layer, Color color)
        {
        Category globalCategory = new Category(this, (layer == 1 ? getSynthName() : "Tetra Voice " + layer), color);
        //if (layer == 1) globalCategory.makeUnresettable();
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
                                
        VBox vbox = new VBox();
        comp = new PatchDisplay(this, 4);
        vbox.add(comp);
        
        comp = new StringComponent(
            (layer == 1 ? "Patch (and Voice 1) Name" : "Voice " + layer + " Name"), 
            this, 
            (layer == 1 ? "name" : "name" + layer), 16, "Name must be up to 16 characters.")
            {
            public String replace(String val)
                {
                return revisePatchName(val);
                }
                                
            public void update(String key, Model model)
                {
                super.update(key, model);
                if (layer == 1)
                    updateTitle();
                }
            };
        vbox.add(comp);
        hbox.add(vbox);

        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }


    public JComponent addGlobal(int layer, Color color)
        {
        Category category = new Category(this, "Performance", color);
        //        category.makePasteable("layer" + layer);
        category.makePasteable("layer");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();

        HBox hbox2 = new HBox();

        CheckBox unison = null;
        CheckBox arpeggiator = null;
        JComponent c2 = null;
        arpeggiator = new CheckBox((layer == 1 ? "Arpeggiator" : "Arpeggiator [-]"), this, "layer" + layer + "arpeggiator");
                 
        if (layer == 1)
            {
            CheckBox temp = new CheckBox("Arpeggiator [-]", this, "throwaway");
            arpeggiator.setPreferredSize(temp.getPreferredSize());
            }
                 
        hbox2.add(arpeggiator);
        
        params = ARPEGGIATOR_MODES;
        c2 = comp = new Chooser((layer == 1 ? "Arpeggiator Mode" : "Arpeggiator Mode [-]"), this, "layer" + layer + "arpeggiatormode", params);
        hbox2.add(comp);
 
        unison = new CheckBox((layer == 1 ? "Unison" : "Unison [-]"), this, "layer" + layer + "unison");
        unison.setPreferredSize(arpeggiator.getPreferredSize());

       
        params = CLOCK_DIVIDES;
        JComponent c3 = comp = new Chooser("Clock Divide", this, "layer" + layer + "clockdivide", params);
        hbox2.add(comp);

        vbox.add(hbox2);

        hbox2 = new HBox();
             
        hbox2.add(unison);
        
        params = KEY_MODES;
        comp = new Chooser((layer == 1 ? "Unison/Key Assign" : "Unison/Key Assign [-]"), this, "layer" + layer + "unisonkeymode", params);
        comp.setPreferredSize(c2.getPreferredSize());
        hbox2.add(comp);

                
        params = UNISON_MODES;
        comp = new Chooser((layer == 1 ? "Unison Mode" : "Unison Mode [-]"), this, "layer" + layer + "unisonmode", params);
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
        //        category.makePasteable("layer" + layer);
        category.makePasteable("layer");

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
        //        category.makePasteable("layer" + layer + "dco" + osc);
        category.makePasteable("layer" + layer + "dco");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
               
        VBox vbox = new VBox();
        PushButton button = new PushButton("Preset", PRESETS)
            {
            public void perform(int i)
                {
                getModel().set("layer" + layer + "dco" + osc + "shape", PRESET_VALS[i]);
                }
            };
        vbox.add(button);
        
        comp = new CheckBox("Key", this, "layer" + layer + "dco" + osc + "key");
        vbox.add(comp);
        
        hbox.add(vbox);

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
                
        comp = new LabelledDial("Glide", this, "layer" + layer + "dco" + osc + "glide", color, 0, 127);
        hbox.add(comp);
                
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
        

    public JComponent addSuboscillator(int layer, Color color)
        {
        Category category = new Category(this, "Sub Osc", color);
        //        category.makePasteable("layer" + layer + "tetrasuboscillator");
        category.makePasteable("layer");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
               
        comp = new LabelledDial("Level 1", this, "layer" + layer + "tetrasuboscillator1level", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Level 2", this, "layer" + layer + "tetrasuboscillator2level", color, 0, 127);
        hbox.add(comp);
                
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addFeedback(int layer, Color color)
        {
        Category category = new Category(this, "Feedback", color);
        //        category.makePasteable("layer" + layer + "tetrafeedback");
        category.makePasteable("layer");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
               
        comp = new LabelledDial("Feedback", this, "layer" + layer + "tetrafeedbackvolume", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Volume");
        hbox.add(comp);

        comp = new LabelledDial("Feedback", this, "layer" + layer + "tetrafeedbackgain", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Gain");
        hbox.add(comp);
                        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addFilter(int layer, Color color)
        {
        Category category = new Category(this, "Filter", color);
        //        category.makePasteable("layer" + layer + "vcf");
        category.makePasteable("layer");

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

    public JComponent addEnvelope(int layer, final int env, Color color)
        {
        Category category = new Category(this, 
                (env == FILTER_ENVELOPE ? "Filter Envelope" : 
                (env == AMPLIFIER_ENVELOPE ? "Amplifier Envelope" : "Envelope 3")), color);
        //        category.makePasteable("layer" + layer + "env" + env);
        category.makePasteable("layer" + layer + "env");
                
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
        //        category.makePasteable("layer" + layer + "vca");
        category.makePasteable("layer");
                
        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();
        VBox vbox = new VBox();
        
               
        comp = new LabelledDial("Initial Level", this, "layer" + layer + "vcainitiallevel", color, 0, 127);
        hbox.add(comp);
                
        comp = new LabelledDial("Pan", this, "layer" + layer + "vcaoutputspread", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Voice", this, "layer" + layer + "vcavoicevolume", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Volume");
        //        ((LabelledDial)comp).addAdditionalLabel(layer == 1 ? "Volume (A)" : (layer == 3 ? "Volume (B)" : "Volume"));
        hbox.add(comp);

        category.add(hbox, BorderLayout.WEST);
        return category;
        }


    public JComponent addExternalModulation(int layer, String title, String key, Color color)
        {
        Category category = new Category(this, title, color);
        //        category.makePasteable("layer" + layer + "key");
        category.makePasteable("layer");
              
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


    public JComponent addSequencerTrack(final int layer, final int track, Color color)
        {
        Category category = new Category(this, "Track " + track, color);
        //        category.makeDistributable("layer" + layer + "track" + track);
        //        category.makePasteable("layer" + layer + "track" + track);
        category.makeDistributable("layer" + layer + "track");
        category.makePasteable("layer" + layer + "track");
                
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
                            return (HALF_NOTES[val % 24] + " " + (val / 24));
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
        //        category.makePasteable("layer" + layer + "lfo" + lfo);
        category.makePasteable("layer" + layer + "lfo");
                
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
        //        category.makePasteable("layer" + layer + "mod" + mod);
        category.makePasteable("layer" + layer + "mod");

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
        //        category.makePasteable("layer" + layer);
        //        category.makeDistributable("layer" + layer);
        category.makePasteable("layer");
        category.makeDistributable("layer");
                       
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
            params = (i == 2 || i == 4 ? MODULATION_DESTINATIONS_SEQ24 : MODULATION_DESTINATIONS);
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


    /** Add a Tetra category */
    public JComponent addTetra(int layer, Color color)
        {
        Category category = new Category(this, "Extra", color);
        //        category.makePasteable("layer" + layer + "tetra");
        category.makePasteable("layer");
              
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();

        params = PUSH_IT_MODES;
        comp = new Chooser("Push-It Mode", this, "layer" + layer + "tetrapushitmode", params);
        vbox.add(comp);
                                
                        
        if (layer == 1)
            {
            params = tetraAssignableParameters;
            comp = new Chooser("Assignable Parameter 1", this, "layer1tetraassignableparameter1", params);
            vbox.add(comp);
            }
        else if (layer == 2)
            {
            params = tetraAssignableParameters;
            comp = new Chooser("Assignable Parameter 2", this, "layer2tetraassignableparameter2", params);
            vbox.add(comp);
            }
        else if (layer == 3)
            {
            params = tetraAssignableParameters;
            comp = new Chooser("Assignable Parameter 3", this, "layer3tetraassignableparameter3", params);
            vbox.add(comp);
            }
        else
            {
            params = tetraAssignableParameters;
            comp = new Chooser("Assignable Parameter 4", this, "layer4tetraassignableparameter4", params);
            vbox.add(comp);
            }
                        
        hbox.add(vbox);

        if (layer == 1)
            {
            // DSI tells me this was basically used by the SoundTower editor and is meaningless.  So we build it to create the parameter but don't show it
            comp = new LabelledDial("Editor", this, "layer" + layer + "tetraeditorbyte", color, 0, 127);
            ((LabelledDial)comp).addAdditionalLabel("Byte");
            // hbox.add(comp);
            }


        comp = new LabelledDial("Push-It", this, "layer" + layer + "tetrapushitnote", color, 0, 127)
            {
            public String map(int val)
                {
                return (NOTES[val % 12] + " " + (val / 12));
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Note");
        hbox.add(comp);

        comp = new LabelledDial("Push-It", this, "layer" + layer + "tetrapushitvelocity", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Velocity");
        hbox.add(comp);

                                
        category.add(hbox, BorderLayout.WEST);
        return category;
        }





    /** Map of parameter -> index in the allParameters array. */
    HashMap parametersToIndex = new HashMap();


    /** List of all Tetra- parameters mapped in the the appropriate location given their NRPN values. */
                
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
    "layer1tetraassignableparameter1",                  // Notice layer1...layer4
    "layer2tetraassignableparameter2",    
    "layer3tetraassignableparameter3",    
    "layer4tetraassignableparameter4",    
    "---",
    "layer1tetrafeedbackgain",    
    "layer1tetrapushitnote",    
    "layer1tetrapushitvelocity",    
    "layer1tetrapushitmode",    
    "layer1tetrasuboscillator1level",    
    "layer1tetrasuboscillator2level",    
    "layer1tetrafeedbackvolume",    
    "layer1tetraeditorbyte",

    "---",                      // "splitpoint",
    "---",                      // "keyboardmode",

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
    "layer2tetrafeedbackgain",    
    "layer2tetrapushitnote",    
    "layer2tetrapushitvelocity",    
    "layer2tetrapushitmode",    
    "layer2tetrasuboscillator1level",    
    "layer2tetrasuboscillator2level",    
    "layer2tetrafeedbackvolume",    
    "---",

    "---",                      // "splitpoint",
    "---",                      // "keyboardmode",

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
    "---",

    "layer3dco1frequency",    
    "layer3dco1finetune",    
    "layer3dco1shape",    
    "layer3dco1glide",    
    "layer3dco1key",    
    "layer3dco2frequency",    
    "layer3dco2finetune",    
    "layer3dco2shape",    
    "layer3dco2glide",    
    "layer3dco2key",    

    "layer3sync",    
    "layer3glidemode",    
    "layer3slop",    
    "layer3mix",    
    "layer3noise",    

    "layer3vcffrequency",    
    "layer3vcfresonance",    
    "layer3vcfkeyboardamount",    
    "layer3vcfaudiomodulation",    
    "layer3vcfpoles",    

    "layer3env1amount",    
    "layer3env1velocityamount",    
    "layer3env1delay",    
    "layer3env1attack",    
    "layer3env1decay",    
    "layer3env1sustain",    
    "layer3env1release",    

    "layer3vcainitiallevel",    
    "layer3vcaoutputspread",    
    "layer3vcavoicevolume",    

    "layer3env2amount",    
    "layer3env2velocityamount",    
    "layer3env2delay",    
    "layer3env2attack",    
    "layer3env2decay",    
    "layer3env2sustain",    
    "layer3env2release",    


    "layer3lfo1frequency",    
    "layer3lfo1shape",    
    "layer3lfo1amount",    
    "layer3lfo1moddestination",    
    "layer3lfo1keysync",    

    "layer3lfo2frequency",    
    "layer3lfo2shape",    
    "layer3lfo2amount",    
    "layer3lfo2moddestination",    
    "layer3lfo2keysync",    

    "layer3lfo3frequency",    
    "layer3lfo3shape",    
    "layer3lfo3amount",    
    "layer3lfo3moddestination",    
    "layer3lfo3keysync",    

    "layer3lfo4frequency",    
    "layer3lfo4shape",    
    "layer3lfo4amount",    
    "layer3lfo4moddestination",    
    "layer3lfo4keysync",

    "layer3env3moddestination",    
    "layer3env3amount",    
    "layer3env3velocityamount",    
    "layer3env3delay",    
    "layer3env3attack",    
    "layer3env3decay",    
    "layer3env3sustain",    
    "layer3env3release",  


    "layer3mod1source",    
    "layer3mod1amount",    
    "layer3mod1destination",    

    "layer3mod2source",    
    "layer3mod2amount",    
    "layer3mod2destination",    

    "layer3mod3source",    
    "layer3mod3amount",    
    "layer3mod3destination",    

    "layer3mod4source",    
    "layer3mod4amount",    
    "layer3mod4destination",    

    "layer3track1destination",    
    "layer3track2destination",    
    "layer3track3destination",    
    "layer3track4destination",    


    "layer3wheelamount",    
    "layer3wheeldestination",    
    "layer3pressureamount",    
    "layer3pressuredestination",    
    "layer3breathamount",    
    "layer3breathdestination",    
    "layer3velocityamount",    
    "layer3velocitydestination",    
    "layer3footamount",    
    "layer3footdestination",    

    "layer3tempo",    
    "layer3clockdivide",    
    "layer3pitchbendrange",    
    "layer3sequencertrigger",    
    "layer3unisonmode",    
    "layer3unisonkeymode",    
    "layer3arpeggiatormode",    

    "layer3env3repeat",    
    "layer3unison",    
    "layer3arpeggiator",    
    "layer3sequencer",    

    "---",
    "---",
    "---",
    "---",    
    "---",    
    "---",    
    "---",    
    "---",
    "layer3tetrafeedbackgain",    
    "layer3tetrapushitnote",    
    "layer3tetrapushitvelocity",    
    "layer3tetrapushitmode",    
    "layer3tetrasuboscillator1level",    
    "layer3tetrasuboscillator2level",    
    "layer3tetrafeedbackvolume",    
    "---",

    "---",                      // "splitpoint",
    "---",                      // "keyboardmode",

    "layer3track1note1",    
    "layer3track1note2",    
    "layer3track1note3",    
    "layer3track1note4",    
    "layer3track1note5",    
    "layer3track1note6",    
    "layer3track1note7",    
    "layer3track1note8",    
    "layer3track1note9",    
    "layer3track1note10",    
    "layer3track1note11",    
    "layer3track1note12",    
    "layer3track1note13",    
    "layer3track1note14",    
    "layer3track1note15",    
    "layer3track1note16",    
    "layer3track2note1",    
    "layer3track2note2",    
    "layer3track2note3",    
    "layer3track2note4",    
    "layer3track2note5",    
    "layer3track2note6",    
    "layer3track2note7",    
    "layer3track2note8",    
    "layer3track2note9",    
    "layer3track2note10",    
    "layer3track2note11",    
    "layer3track2note12",    
    "layer3track2note13",    
    "layer3track2note14",    
    "layer3track2note15",    
    "layer3track2note16",    
    "layer3track3note1",    
    "layer3track3note2",    
    "layer3track3note3",    
    "layer3track3note4",    
    "layer3track3note5",    
    "layer3track3note6",    
    "layer3track3note7",    
    "layer3track3note8",    
    "layer3track3note9",    
    "layer3track3note10",    
    "layer3track3note11",    
    "layer3track3note12",    
    "layer3track3note13",    
    "layer3track3note14",    
    "layer3track3note15",    
    "layer3track3note16",    
    "layer3track4note1",    
    "layer3track4note2",    
    "layer3track4note3",    
    "layer3track4note4",    
    "layer3track4note5",    
    "layer3track4note6",    
    "layer3track4note7",    
    "layer3track4note8",    
    "layer3track4note9",    
    "layer3track4note10",    
    "layer3track4note11",    
    "layer3track4note12",    
    "layer3track4note13",    
    "layer3track4note14",    
    "layer3track4note15",    
    "layer3track4note16",    

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


    "layer4dco1frequency",    
    "layer4dco1finetune",    
    "layer4dco1shape",    
    "layer4dco1glide",    
    "layer4dco1key",    
    "layer4dco2frequency",    
    "layer4dco2finetune",    
    "layer4dco2shape",    
    "layer4dco2glide",    
    "layer4dco2key",    

    "layer4sync",    
    "layer4glidemode",    
    "layer4slop",    
    "layer4mix",    
    "layer4noise",    

    "layer4vcffrequency",    
    "layer4vcfresonance",    
    "layer4vcfkeyboardamount",    
    "layer4vcfaudiomodulation",    
    "layer4vcfpoles",    

    "layer4env1amount",    
    "layer4env1velocityamount",    
    "layer4env1delay",    
    "layer4env1attack",    
    "layer4env1decay",    
    "layer4env1sustain",    
    "layer4env1release",    

    "layer4vcainitiallevel",    
    "layer4vcaoutputspread",    
    "layer4vcavoicevolume",    

    "layer4env2amount",    
    "layer4env2velocityamount",    
    "layer4env2delay",    
    "layer4env2attack",    
    "layer4env2decay",    
    "layer4env2sustain",    
    "layer4env2release",    


    "layer4lfo1frequency",    
    "layer4lfo1shape",    
    "layer4lfo1amount",    
    "layer4lfo1moddestination",    
    "layer4lfo1keysync",    

    "layer4lfo2frequency",    
    "layer4lfo2shape",    
    "layer4lfo2amount",    
    "layer4lfo2moddestination",    
    "layer4lfo2keysync",    

    "layer4lfo3frequency",    
    "layer4lfo3shape",    
    "layer4lfo3amount",    
    "layer4lfo3moddestination",    
    "layer4lfo3keysync",    

    "layer4lfo4frequency",    
    "layer4lfo4shape",    
    "layer4lfo4amount",    
    "layer4lfo4moddestination",    
    "layer4lfo4keysync",

    "layer4env3moddestination",    
    "layer4env3amount",    
    "layer4env3velocityamount",    
    "layer4env3delay",    
    "layer4env3attack",    
    "layer4env3decay",    
    "layer4env3sustain",    
    "layer4env3release",  


    "layer4mod1source",    
    "layer4mod1amount",    
    "layer4mod1destination",    

    "layer4mod2source",    
    "layer4mod2amount",    
    "layer4mod2destination",    

    "layer4mod3source",    
    "layer4mod3amount",    
    "layer4mod3destination",    

    "layer4mod4source",    
    "layer4mod4amount",    
    "layer4mod4destination",    

    "layer4track1destination",    
    "layer4track2destination",    
    "layer4track3destination",    
    "layer4track4destination",    


    "layer4wheelamount",    
    "layer4wheeldestination",    
    "layer4pressureamount",    
    "layer4pressuredestination",    
    "layer4breathamount",    
    "layer4breathdestination",    
    "layer4velocityamount",    
    "layer4velocitydestination",    
    "layer4footamount",    
    "layer4footdestination",    

    "layer4tempo",    
    "layer4clockdivide",    
    "layer4pitchbendrange",    
    "layer4sequencertrigger",    
    "layer4unisonmode",    
    "layer4unisonkeymode",    
    "layer4arpeggiatormode",    

    "layer4env3repeat",    
    "layer4unison",    
    "layer4arpeggiator",    
    "layer4sequencer",    

    "---",
    "---",
    "---",
    "---",    
    "---",    
    "---",    
    "---",    
    "---",
    "layer4tetrafeedbackgain",    
    "layer4tetrapushitnote",    
    "layer4tetrapushitvelocity",    
    "layer4tetrapushitmode",    
    "layer4tetrasuboscillator1level",    
    "layer4tetrasuboscillator2level",    
    "layer4tetrafeedbackvolume",    
    "---",

    "---",                      // "splitpoint",
    "---",                      // "keyboardmode",

    "layer4track1note1",    
    "layer4track1note2",    
    "layer4track1note3",    
    "layer4track1note4",    
    "layer4track1note5",    
    "layer4track1note6",    
    "layer4track1note7",    
    "layer4track1note8",    
    "layer4track1note9",    
    "layer4track1note10",    
    "layer4track1note11",    
    "layer4track1note12",    
    "layer4track1note13",    
    "layer4track1note14",    
    "layer4track1note15",    
    "layer4track1note16",    
    "layer4track2note1",    
    "layer4track2note2",    
    "layer4track2note3",    
    "layer4track2note4",    
    "layer4track2note5",    
    "layer4track2note6",    
    "layer4track2note7",    
    "layer4track2note8",    
    "layer4track2note9",    
    "layer4track2note10",    
    "layer4track2note11",    
    "layer4track2note12",    
    "layer4track2note13",    
    "layer4track2note14",    
    "layer4track2note15",    
    "layer4track2note16",    
    "layer4track3note1",    
    "layer4track3note2",    
    "layer4track3note3",    
    "layer4track3note4",    
    "layer4track3note5",    
    "layer4track3note6",    
    "layer4track3note7",    
    "layer4track3note8",    
    "layer4track3note9",    
    "layer4track3note10",    
    "layer4track3note11",    
    "layer4track3note12",    
    "layer4track3note13",    
    "layer4track3note14",    
    "layer4track3note15",    
    "layer4track3note16",    
    "layer4track4note1",    
    "layer4track4note2",    
    "layer4track4note3",    
    "layer4track4note4",    
    "layer4track4note5",    
    "layer4track4note6",    
    "layer4track4note7",    
    "layer4track4note8",    
    "layer4track4note9",    
    "layer4track4note10",    
    "layer4track4note11",    
    "layer4track4note12",    
    "layer4track4note13",    
    "layer4track4note14",    
    "layer4track4note15",    
    "layer4track4note16",    

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
    
    
    public final int LAYER_OFFSET = 256;
    public final int BASE_OFFSET = 512;
    public final int LAYER_SIZE = 200;

    public Object[] emitAll(String key)
        {
        if (key.equals("bank")) return new Object[0];  // this is not emittable
        if (key.equals("number")) return new Object[0];  // this is not emittable

        int index;
        int value;
        
        if (key.startsWith("name"))
            {
            int pos = (key.equals("name") ? 0 : (key.equals("name2") ? 1 : (key.equals("name3") ? 2 : 3))) * LAYER_OFFSET + BASE_OFFSET + 184;
            Object[] ret = new Object[4 * 16];
            char[] name = (model.get(key, "Untitled") + "                ").toCharArray();
            for(int i = 0; i < 16; i++)
                {
                Object[] nrpn = buildNRPN(getChannelOut(), i + pos, name[i]);
                System.arraycopy(nrpn, 0, ret, i * 4, 4);
                }
            return ret;
            }
        else 
            {
            int val = model.get(key, 0);
            
            if (key.equals("layer1tetraassignableparameter1") || 
                key.equals("layer2tetraassignableparameter2") || 
                key.equals("layer3tetraassignableparameter3") || 
                key.equals("layer4tetraassignableparameter4"))
                {
                if (!sendAssignableParams) return new Object[0];  // we don't send these
                                
                // there is a hole in these values which we have to ignore
                if (val >= 111 && val <= 119)
                    {
                    // reset to 0
                    val = 0;
                    }
                }               

            int pos = (((Integer)(parametersToIndex.get(key))).intValue());
            int revisedpos = (pos / LAYER_SIZE) * LAYER_OFFSET + (pos % LAYER_SIZE) + BASE_OFFSET;
            return buildNRPN(getChannelOut(), revisedpos, val);
            }
        }


    public void setParamByNRPN(int layer, int num, int val)
        {
        String key = parameters[layer * LAYER_SIZE + num];
        if (key == "---")
            return;
        else
            model.set(key, val);
        }

    public void handleSynthCCOrNRPN(Midi.CCData data)
        {
        if (data.type == Midi.CCDATA_TYPE_NRPN && data.number <= 1719)
            {
            if (data.number >= 696 && data.number <= 712)  // Name
                {
                char[] name = (model.get("name", "Untitled") + "                ").toCharArray();
                name[data.number - 696] = (char)(data.value);
                model.set("name", new String(name));
                }
            else
                {
                int layer = (data.number - BASE_OFFSET) / LAYER_OFFSET;
                int num = (data.number - BASE_OFFSET) % LAYER_OFFSET;
                
                if (layer == 4) // Quad
                    {
                    setParamByNRPN(0, num, data.value);
                    setParamByNRPN(1, num, data.value);
                    setParamByNRPN(2, num, data.value);
                    setParamByNRPN(3, num, data.value);
                    }
                else
                    {
                    setParamByNRPN(layer, num, data.value);
                    }
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


    public int parse(byte[] data, boolean fromFile)
        {
        // unfortunately, the Prophet '08 doesn't provide number/bank info
        // with its edit buffer data dump.  :-(
        
        if (data[3] == 0x22)  // program data only, not (0x03) edit buffer
            {
            model.set("number", data[4]);
            }

        byte[] d = null;
        if (data[3] == 0x22)
            d = convertTo8Bit(data, 5);
        else if (data[3] == 0x37)
            d = convertTo8Bit(data, 4);
        else
            System.err.println("Warning (DSITetraCombo): Unknown program data format " + data[3]);
        
        for(int i = 0; i < 800; i++)
            {
            int j = i;

            if (j >= 600)
                {
                j = tetraComboParams[j - 600] + 768;
                }
            else if (j >= 400)
                {
                j = tetraComboParams[j - 400] + 512;
                }
            else if (j >= 200) 
                {
                j = tetraComboParams[j - 200] + 256;
                }
            else 
                {
                j = tetraComboParams[j];
                }
                                                                
            if (load == LOAD_A && !parameters[i].startsWith("layer1"))
                {
                // do nothing -- they're not layer 1 (A) parameters
                }
            else if (load == LOAD_B && !parameters[i].startsWith("layer2"))
                {
                // do nothing -- they're not layer 2 (B) parameters
                }
            else if (load == LOAD_C && !parameters[i].startsWith("layer3"))
                {
                // do nothing -- they're not layer 2 (B) parameters
                }
            else if (load == LOAD_D && !parameters[i].startsWith("layer4"))
                {
                // do nothing -- they're not layer 2 (B) parameters
                }
            else if (!parameters[i].equals("---"))
                {
                // Note: DSI isn't 2's complement.  So everything in the model is being stored starting at 0
                int q = d[j];
                if (q < 0) q += 256;  // push to unsigned (not 2's complement)
                model.set(parameters[i], q);
                }
            }
                
        for(int n = 1; n <= 4; n++)
            {
            if (load == LOAD_A && n != 1) continue;
            if (load == LOAD_B && n != 2) continue;
            if (load == LOAD_C && n != 3) continue;
            if (load == LOAD_D && n != 4) continue;

            // handle name specially
            byte[] name = new byte[16];
            int pos = ((n-1) * 256) + 184;          // 1: 184, 2: 256+184, 3:512+184, 4:768+184
            System.arraycopy(d, pos, name, 0, 16);
            try
                {
                String key = (n == 1 ? "name" : "name" + n);
                model.set(key, new String(name, "US-ASCII"));
                }
            catch (UnsupportedEncodingException e)
                {
                Synth.handleException(e); 
                }
            }
                
        revise();
        return PARSE_SUCCEEDED;
        }
    

    public byte[] emit(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        if (tempModel == null)
            tempModel = getModel();

        byte[] data = null;

        byte[] d = new byte[1024];
        for(int i = 0; i < 800; i++)
            {
            int j = i;

            if (j >= 600)
                {
                j = tetraComboParams[j - 600] + 768;
                }
            else if (j >= 400)
                {
                j = tetraComboParams[j - 400] + 512;
                }
            else if (j >= 200) 
                {
                j = tetraComboParams[j - 200] + 256;
                }
            else 
                {
                j = tetraComboParams[j];
                }
                                                                
            if (!parameters[i].equals("---"))
                {
                // Note: DSI isn't 2's complement.  So everything in the model is being stored starting at 0
                int q = model.get(parameters[i], 0);

                if (parameters[i].equals("layer1tetraassignableparameter1") || 
                    parameters[i].equals("layer2tetraassignableparameter2") || 
                    parameters[i].equals("layer3tetraassignableparameter3") || 
                    parameters[i].equals("layer4tetraassignableparameter4"))
                    {
                    // For the Tetra, there is a hole in these values which we have to ignore
                    if (q >= 111 && q <= 119)
                        {
                        // reset to 0
                        q = 0;
                        }
                    }               
                                                                                
                if (q > 127) q -= 256;  // push to signed (not 2's complement)
                d[j] = (byte)q;
                }
            }
                                                
        for(int n = 1; n <= 4; n++)
            {
            if (load == LOAD_A && n != 1) continue;
            if (load == LOAD_B && n != 2) continue;
            if (load == LOAD_C && n != 3) continue;
            if (load == LOAD_D && n != 4) continue;

            // handle name specially
            String key = (n == 1 ? "name" : "name" + n);
            char[] name = (model.get(key, "Untitled") + "                " ).toCharArray();
            int pos = ((n-1) * 256) + 184;          // 1: 184, 2: 256+184, 3:512+184, 4:768+184
            for(int i = 0; i < 16; i++)
                d[pos + i] = (byte)(name[i] & 127);
            }

        data = convertTo7Bit(d);  
        
        if (toWorkingMemory)
            {
            byte[] emit = new byte[1176];
            emit[0] = (byte)0xF0;
            emit[1] = (byte)0x01;  // DSI
            emit[2] = TETRA_ID;
            emit[3] = (byte)0x37;  // Edit Buffer Data Dump
            System.arraycopy(data, 0, emit, 4, data.length);
            emit[emit.length - 1] = (byte)0xF7;
            return emit;
            }
        else
            {
            byte[] emit = new byte[1177];
            emit[0] = (byte)0xF0;
            emit[1] = (byte)0x01;  // DSI
            emit[2] = TETRA_ID;
            emit[3] = (byte)0x22;  // Program Data Dump
            emit[4] = (byte)tempModel.get("number", 0);
            System.arraycopy(data, 0, emit, 5, data.length);
            emit[emit.length - 1] = (byte)0xF7;
            return emit;
            }
        }
        
        
    public void changePatch(Model tempModel)
        {
        int number = tempModel.get("number");
        Object[] message = new Object[1];
        
        // PC
        message[0] = buildPC(getChannelOut(), number)[0];
        
        tryToSendMIDI(message);
        }

    public void writeAllParameters(Model model)
        {
        performChangePatch(model);     // we need to be at the start for the Oberheim Matrix 1000
        tryToSendMIDI(emitAll(model, false, false));
        // we CANNOT do change patch after writing data -- it ruins the sysex write.  DSI Bug it would seem. 
        }
 
    public byte[] requestCurrentDump()
        {
        byte[] data = new byte[5];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x01;   // DSI
        data[2] = TETRA_ID;
        data[3] = (byte)0x38;
        data[4] = (byte)0xF7;                   
        return data;
        }

    public byte[] requestDump(Model tempModel)
        {
        if (tempModel == null)
            tempModel = getModel();

        byte[] data = new byte[6];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x01;   // DSI
        data[2] = TETRA_ID;
        data[3] = (byte)0x21;
        data[4] = (byte)(tempModel.get("number", 0));
        data[5] = (byte)0xF7;
        return data;
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
        
    public static String getSynthName() { return "DSI Tetra [Combo]"; }
    public String getPatchName(Model model) { return model.get("name", "Untitled"); }
    
    public Model getNextPatchLocation(Model model)
        {
        int number = model.get("number");
        
        number++;
        if (number >= 128)
            {
            number = 0;
            }
                
        Model newModel = buildModel();
        newModel.set("number", number);
        return newModel;
        }

    public String getPatchLocationName(Model model)
        {
        // getPatchLocationName() is called from sprout() as a test to see if we should enable
        // batch downloading.  If we haven't yet created an .init file, then parameters won't exist
        // yet and this method will bomb badly.  So we return null in this case.
        if (!model.exists("number")) return null;
        
        int number = (model.get("number") + 1);
        return ("" + (number > 99 ? "" : (number > 9 ? "0" : "00")) + number);
        }


    public JFrame sprout()
        {
        JFrame frame = super.sprout();
        addProphetMenu();
        return frame;
        }         

    public void addProphetMenu()
        {
        JMenu menu = new JMenu("DSI");
        menubar.add(menu);

        JMenuItem a2b = new JMenuItem("Copy Voice To...");
        menu.add(a2b);
        a2b.addActionListener(new ActionListener()
            {
            JComboBox part1 = new JComboBox(VOICES);
            JComboBox part2 = new JComboBox(VOICES);
                
            public void actionPerformed(ActionEvent evt)
                {
                boolean result = showMultiOption(DSITetraCombo.this, new String[] { "Copy", "To" }, 
                    new JComponent[] { part1, part2 }, "Copy Voice To...", "Enter the voice to copy, and where to copy it.");

                if (result)
                    {
                    int p1 = part1.getSelectedIndex() + 1;
                    int p2 = part2.getSelectedIndex() + 1;

                    undo.push(getModel());
                    setSendMIDI(false);
                    boolean currentPush = undo.getWillPush();
                    undo.setWillPush(false);

                    for(int i = 0; i < parameters.length; i++)
                        {
                        if (parameters[i].startsWith("layer" + p1))
                            {
                            model.set(("layer" + p2) + parameters[i].substring(6), model.get(parameters[i]));
                            }
                        }
                
                    // copy name
                    String key1 = (p1 == 1 ? "name" : "name" + p1);
                    String key2 = (p2 == 1 ? "name" : "name" + p2);
                    String name1 = model.get(key1, "Untitled");
                    model.set(key2, name1);

                    undo.setWillPush(currentPush);
                    setSendMIDI(true);
                    sendAllParameters();
                    }
                }
            });

        JMenuItem swap = new JMenuItem("Swap Voices...");
        menu.add(swap);
        swap.addActionListener(new ActionListener()
            {
            JComboBox part1 = new JComboBox(VOICES);
            JComboBox part2 = new JComboBox(VOICES);
                
            public void actionPerformed(ActionEvent evt)
                {
                boolean result = showMultiOption(DSITetraCombo.this, new String[] { "Swap", "With" }, 
                    new JComponent[] { part1, part2 }, "Swap Voices...", "Enter the voices to swap with one another.");

                if (result)
                    {
                    int p1 = part1.getSelectedIndex() + 1;
                    int p2 = part2.getSelectedIndex() + 1;

                    undo.push(getModel());
                    setSendMIDI(false);
                    boolean currentPush = undo.getWillPush();
                    undo.setWillPush(false);

                    for(int i = 0; i < parameters.length; i++)
                        {
                        if (parameters[i].startsWith("layer" + p2))
                            {
                            int val2 = model.get(parameters[i]);
                            int val1 = model.get(("layer" + p1) + parameters[i].substring(6));
                            model.set(("layer" + p1) + parameters[i].substring(6), val2);
                            model.set(parameters[i], val1);
                            }
                        }
                                                
                    // swap names
                    String key1 = (p1 == 1 ? "name" : "name" + p1);
                    String key2 = (p2 == 1 ? "name" : "name" + p2);
                    String name1 = model.get(key1, "Untitled");
                    String name2 = model.get(key2, "Untitled");
                    model.set(key1, name2);
                    model.set(key2, name1);
                                
                    undo.setWillPush(currentPush);
                    setSendMIDI(true);
                    sendAllParameters();
                    }
                }
            });
            
        menu.addSeparator();

        JRadioButtonMenuItem loadBoth = new JRadioButtonMenuItem("Load All Voices");
        loadBoth.setSelected(true);
        menu.add(loadBoth);
        loadBoth.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt)
                {
                load = LOAD_ALL;
                }
            });
            
        JRadioButtonMenuItem restrictA = new JRadioButtonMenuItem("Load Only Voice 1");
        menu.add(restrictA);
        restrictA.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt)
                {
                load = LOAD_A;
                }
            });

        JRadioButtonMenuItem restrictB = new JRadioButtonMenuItem("Load Only Voice 2");
        menu.add(restrictB);
        restrictB.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt)
                {
                load = LOAD_B;
                }
            });

        JRadioButtonMenuItem restrictC = new JRadioButtonMenuItem("Load Only Voice 3");
        menu.add(restrictC);
        restrictC.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt)
                {
                load = LOAD_C;
                }
            });

        JRadioButtonMenuItem restrictD = new JRadioButtonMenuItem("Load Only Voice 4");
        menu.add(restrictD);
        restrictD.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt)
                {
                load = LOAD_D;
                }
            });
        
        ButtonGroup group = new ButtonGroup();
        group.add(loadBoth);
        group.add(restrictA);
        group.add(restrictB);
        group.add(restrictC);
        group.add(restrictD);
        
        menu.addSeparator();

        JMenuItem build = new JMenuItem("Build Single Patch...");
        menu.add(build);
        build.addActionListener(new ActionListener()
            {
            JComboBox part1 = new JComboBox(VOICES);
            JComboBox part2 = new JComboBox(VOICES);
                
            public void actionPerformed(ActionEvent evt)
                {
                boolean result = showMultiOption(DSITetraCombo.this, new String[] { "Layer A", "Layer B" }, 
                    new JComponent[] { part1, part2 }, "Build Single Patch...", "Enter the voices to put in layers.");

                if (result)
                    {
                    int p1 = part1.getSelectedIndex() + 1;
                    int p2 = part2.getSelectedIndex() + 1;

                    Synth newSynth = instantiate(DSIProphet08.class, false, true, tuple);
                    newSynth.setSendMIDI(false);
                    boolean currentPush = newSynth.getUndo().getWillPush();
                    newSynth.getUndo().setWillPush(false);
                    Model newModel = newSynth.getModel();
           
                    for(int i = 0; i < parameters.length; i++)
                        {
                        if (parameters[i].equals("layer1editorbyte"))
                            {
                            int val2 = model.get(parameters[i]);
                            newModel.set("layer1" + parameters[i].substring(6), val2);
                            }
                        else if (
                            parameters[i].equals("layer1tetraassignableparameter1") ||
                            parameters[i].equals("layer2tetraassignableparameter2") ||
                            parameters[i].equals("layer3tetraassignableparameter3") ||
                            parameters[i].equals("layer4tetraassignableparameter4"))        // Map 1 -> A
                            {
                            int val2 = model.get(parameters[i]);
                            newModel.set("layer1" + parameters[i].substring(6), val2);
                            }
                        else if (parameters[i].startsWith("layer" + p1))
                            {
                            int val2 = model.get(parameters[i]);
                            newModel.set("layer1" + parameters[i].substring(6), val2);
                            }
                        else if (parameters[i].startsWith("layer" + p2))
                            {
                            int val2 = model.get(parameters[i]);
                            newModel.set("layer2" + parameters[i].substring(6), val2);
                            }
                        }
                    String key = (p1 == 1 ? "name" : "name" + p1);
                    newModel.set("name", model.get(key, "Untitled"));
                                
                    newSynth.getUndo().setWillPush(currentPush);
                    newSynth.setSendMIDI(true);
                    }
                }
            });

        String str = getLastX("SendAssignableParams", getSynthName(), true);
        if (str == null)
            sendAssignableParams = true;            // default is true
        else if (str.equalsIgnoreCase("true"))
            sendAssignableParams = true;
        else
            sendAssignableParams = false;
                
        final JCheckBoxMenuItem beta = new JCheckBoxMenuItem("Send Assignable Params");
        beta.setSelected(sendAssignableParams);
        menu.add(beta);
        
        beta.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt)
                {
                sendAssignableParams = beta.isSelected();
                setLastX("" + sendAssignableParams, "SendAssignableParams", getSynthName(), true);
                }
            });
        }


    // These are maps from the first-column tetra combo NRPN parameters
    // to the data byte positions in the sysex dump.
    // A "-1" means that that parameter is not supported by the tetra and does not appear
    // in its dump.
        
    // TETRA
    public static final int[] tetraComboParams = new int[]
    {
    0, 1, 2, 3, 4, 6, 7, 8, 9, 10, 12, 13, 14, 16, 17, 20, 
    21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 40, 41, 
    33, 34, 35, 36, 37, 38, 39, 42, 43, 44, 45, 46, 47, 48, 
    49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 
    63, 64, 65, 66, 67, 68, 69, 71, 72, 73, 74, 75, 76, 77, 
    78, 79, 80, 81, 82, 107, 108, 109, 110, 83, 84, 85, 86, 
    87, 88, 89, 90, 91, 92, 101, 102, 15, 105, 93, 94, 103, 
    70, 95, 104, 106, 
    -1, -1, -1, 111, 112, 113, 114, 
    -1, 19, 96, 97, 98, 5, 11, 18, 117, 
    -1, -1, 120, 121, 122, 123, 124, 125, 126, 127, 128, 
    129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 139, 
    140, 141, 142, 143, 144, 145, 146, 147, 148, 149, 150, 
    151, 152, 153, 154, 155, 156, 157, 158, 159, 160, 161, 
    162, 163, 164, 165, 166, 167, 168, 169, 170, 171, 172, 
    173, 174, 175, 176, 177, 178, 179, 180, 181, 182, 183, 
    184, 185, 186, 187, 188, 189, 190, 191, 192, 193, 194, 
    195, 196, 197, 198, 199         
    };

    public static final String[] tetraAssignableParameters = new String[]
    {
    "Osc 1 Frequency",
    "Osc 1 Fine Freq",
    "Osc 1 Shape",
    "Osc 1 Glide",
    "Osc 1 Key Track",
    "Sub Osc 1 Level",
    "Osc 2 Frequency",
    "Osc 2 Fine Freq",
    "Osc 2 Shape",
    "Osc 2 Glide",
    "Osc 2 Key Track",
    "Sub Osc 2 Level",
    "Osc Hard Sync",
    "Glide Mode",
    "Oscillator Slop",
    "Pitch Wheel Range",
    "Oscillator Mix",
    "Noise Level",
    "Feedback Volume",
    "Feedback Gain",
    "Filter Cutoff Freq",
    "Filter Resonance",
    "Filter Keyboard Amt",
    "Filter Audio Mod",
    "Filter Config/Mode",
    "Filter Env Amount",
    "Filter Env Velocity",
    "Filter Env Delay",
    "Filter Env Attack",
    "Filter Env Decay",
    "Filter Env Sustain",
    "Filter Env Release",
    "VCA Level",
    "VCA Env Amount",
    "VCA Env Velocity",
    "VCA Env Delay",
    "VCA Env Attack",
    "VCA Env Decay",
    "VCA Env Sustain",
    "VCA Env Release",
    "Pan Spread",
    "Program Volume",
    "LFO 1 Frequency",
    "LFO 1 Shape",
    "LFO 1 Amount",
    "LFO 1 Destination",
    "LFO 1 Key Sync",
    "LFO 2 Frequency",
    "LFO 2 Shape",
    "LFO 2 Amount",
    "LFO 2 Destination",
    "LFO 2 Key Sync",
    "LFO 3 Frequency",
    "LFO 3 Shape",
    "LFO 3 Amount",
    "LFO 3 Destination",
    "LFO 3 Key Sync",
    "LFO 4 Frequency",
    "LFO 4 Shape",
    "LFO 4 Amount",
    "LFO 4 Destination",
    "LFO 4 Key Sync",
    "Env 3 Desination",
    "Env 3 Amount",
    "Env 3 Velocity",
    "Env 3 Delay",
    "Env 3 Attack",
    "Env 3 Decay",
    "Env 3 Sustain",
    "Env 3 Release",
    "Env 3 Repeat",
    "Mod 1 Source",
    "Mod 1 Amount",
    "Mod 1 Destination",
    "Mod 2 Source",
    "Mod 2 Amount",
    "Mod 2 Destination",
    "Mod 3 Source",
    "Mod 3 Amount",
    "Mod 3 Destination",
    "Mod 4 Source",
    "Mod 4 Amount",
    "Mod 4 Destination",
    "Mod Wheel Amount",
    "Mod Wheel Dest",
    "Pressure Amount",
    "Pressure Destination",
    "Breath Amount",
    "Breath Destination",
    "Velocity Amount",
    "Velocity Destination",
    "Foot Control Amt",
    "Foot Control Dest",
    "Unison Mode",
    "Unison Assign",
    "Unison On/off",
    "Push It Note",
    "Push It Velocity",
    "Push It Mode",
    "Split Point",
    "Key Mode",
    "Clock BPM",
    "Clock Divide",
    "Arpeggiator Mode",
    "Arpeggiator On/Off",
    "Sequence Trigger",
    "Sequencer On/Off",
    "Seq 1 Destination",
    "Seq 2 Destination",
    "Seq 3 Destination",
    "Seq 4 Destination",
    "[Invalid 1]",
    "[Invalid 2]",
    "[Invalid 3]",
    "[Invalid 4]",
    "[Invalid 5]",    // "Osc 1 Wave Reset",
    "[Invalid 6]",    // "Osc 2 Wave Reset",
    "[Invalid 7]",
    "[Invalid 8]",
    "[Invalid 9]",
    "Seq 1 Step 1",
    "Seq 1 Step 2",
    "Seq 1 Step 3",
    "Seq 1 Step 4",
    "Seq 1 Step 5",
    "Seq 1 Step 6",
    "Seq 1 Step 7",
    "Seq 1 Step 8",
    "Seq 1 Step 9",
    "Seq 1 Step 10",
    "Seq 1 Step 11",
    "Seq 1 Step 12",
    "Seq 1 Step 13",
    "Seq 1 Step 14",
    "Seq 1 Step 15",
    "Seq 1 Step 16",
    "Seq 2 Step 1",
    "Seq 2 Step 2",
    "Seq 2 Step 3",
    "Seq 2 Step 4",
    "Seq 2 Step 5",
    "Seq 2 Step 6",
    "Seq 2 Step 7",
    "Seq 2 Step 8",
    "Seq 2 Step 9",
    "Seq 2 Step 10",
    "Seq 2 Step 11",
    "Seq 2 Step 12",
    "Seq 2 Step 13",
    "Seq 2 Step 14",
    "Seq 2 Step 15",
    "Seq 2 Step 16",
    "Seq 3 Step 1",
    "Seq 3 Step 2",
    "Seq 3 Step 3",
    "Seq 3 Step 4",
    "Seq 3 Step 5",
    "Seq 3 Step 6",
    "Seq 3 Step 7",
    "Seq 3 Step 8",
    "Seq 3 Step 9",
    "Seq 3 Step 10",
    "Seq 3 Step 11",
    "Seq 3 Step 12",
    "Seq 3 Step 13",
    "Seq 3 Step 14",
    "Seq 3 Step 15",
    "Seq 3 Step 16",
    "Seq 4 Step 1",
    "Seq 4 Step 2",
    "Seq 4 Step 3",
    "Seq 4 Step 4",
    "Seq 4 Step 5",
    "Seq 4 Step 6",
    "Seq 4 Step 7",
    "Seq 4 Step 8",
    "Seq 4 Step 9",
    "Seq 4 Step 10",
    "Seq 4 Step 11",
    "Seq 4 Step 12",
    "Seq 4 Step 13",
    "Seq 4 Step 14",
    "Seq 4 Step 15",
    "Seq 4 Step 16",
    "Edit Name 1",
    "Edit Name 2",
    "Edit Name 3",
    "Edit Name 4",
    "Edit Name 5",
    "Edit Name 6",
    "Edit Name 7",
    "Edit Name 8",
    "Edit Name 9",
    "Edit Name 10",
    "Edit Name 11",
    "Edit Name 12",
    "Edit Name 13",
    "Edit Name 14",
    "Edit Name 15",
    "Edit Name 16",
    };


    public boolean testVerify(Synth synth2, String key, Object val1, Object val2)
        {
        // These can be invalid regardless due to the hole in the middle                                
        if (key.equals("layer1tetraassignableparameter1") || 
            key.equals("layer2tetraassignableparameter2") || 
            key.equals("layer3tetraassignableparameter3") || 
            key.equals("layer4tetraassignableparameter4") )
            return true;

        // This is just a junk widget                                   
        if (key.equals("throwaway"))
            return true;
        
        else return false;
        }
    }

