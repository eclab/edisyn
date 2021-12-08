/***
    Copyright 2021 by Wim Verheyen
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.sequentialprophetrev2;

import edisyn.*;
import edisyn.gui.*;
import java.util.regex.*;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.border.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javax.sound.midi.*;
import edisyn.synth.dsiprophet08.*;

/**
   A patch editor for the Sequential Prophet Rev2.
   Run with java -cp libraries/coremidi4j-1.6.jar:. edisyn.Edisyn
   java edisyn.test.SanityCheck
        
   @author Wim Verheyen
*/

public class SequentialProphetRev2 extends Synth
    {
    /// Various collections of parameter names for pop-up menus

    public static final String[] KEYBOARD_MODES = new String[] { "Normal", "Stack", "Split" };
    public static final String[] NOTES = new String[] { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
    public static final String[] ARPEGGIATOR_MODES = new String[] { "Up", "Down", "Up/Down", "Random", "Assign"};
    public static final String[] CLOCK_DIVIDES = new String[] { "Half Note", "Quarter Note", "8th Note", "8th Note Half Swing", "8th Note Full Swing", "8th Note Triplets", "16th Note", "16th Note Half Swing", "16th Note Full Swing", "16th Note Triplets", "32nd Note", "32nd Note Triplets", "64th Note Triplets" };
    public static final String[] KEY_MODES = new String[] { "Low Note", "High Note", "Last Note", "Low Retrigger", "High Retrigger", "Last Retrigger" };
    public static final String[] BANKS_PROPHET = new String[] { "U1", "U2", "U3", "U4", "F1", "F2", "F3", "F4" };
    public static final String[] GLIDE_MODES = new String[] { "Fixed Rate", "Fixed Rate Auto", "Fixed Time", "Fixed Time Auto" };
    public static final String[] SHAPES = new String[] { "Off", "Sawtooth", "Saw+Tri", "Triangle", "Pulse" };
    public static final String[] MODULATION_SOURCES = new String[] { "Off", "Seq 1", "Seq 2", "Seq 3", "Seq 4", "LFO 1", "LFO 2", "LFO 3", "LFO 4", "Env LPF", "Env VCA", "Env 3", "Pitch Bend", "Mod Wheel", "Pressure", "Breath", "Foot Pedal", "Expression Pedal", "Velocity", "Note Number", "Noise", "DC", "Audio Out" };
    public static final String[] MODULATION_DESTINATIONS = new String[] { "Off", "Osc 1 Freq", "Osc 2 Freq", "Osc All Freq", "Osc Mix", "Noise Level", "Sub Osc Level", "Osc 1 Shape", "Osc 2 Shape", "Osc All Shape", "Filter Cutoff", "Filter Resonance", "Filter Audio Mod", "VCA Level", "Pan Spread", "LFO 1 Freq", "LFO 2 Freq", "LFO 3 Freq", "LFO 4 Freq", "LFO All Freq", "LFO 1 Amount", "LFO 2 Amount", "LFO 3 Amount", "LFO 4 Amount", "LFO All Amount", "Filter Env Amount", "Amp Env Amount", "Env 3 Amount", "Env All Amount", "LPF Attack", "VCA Attack", "Env 3 Attack", "Env All Attack", "LPF Decay", "VCA Decay", "Env 3 Decay", "Env All Decay", "LPF Release", "VCA Release", "Env 3 Release", "Env All Release", "Mod 1 Amount", "Mod 2 Amount", "Mod 3 Amount", "Mod 4 Amount", "Mod 5 Amount", "Mod 6 Amount", "Mod 7 Amount", "Mod 8 Amount", "Osc Slop", "FX Mix", "FX Param 1", "FX Param 2" };
    public static final String[] MODULATION_DESTINATIONS_SEQ24 = new String[] { "Off", "Osc 1 Freq", "Osc 2 Freq", "Osc All Freq", "Osc Mix", "Noise Level", "Sub Osc Level", "Osc 1 Shape", "Osc 2 Shape", "Osc All Shape", "Filter Cutoff", "Filter Resonance", "Filter Audio Mod", "VCA Level", "Pan Spread", "LFO 1 Freq", "LFO 2 Freq", "LFO 3 Freq", "LFO 4 Freq", "LFO All Freq", "LFO 1 Amount", "LFO 2 Amountt", "LFO 3 Amount", "LFO 4 Amount", "LFO All Amount", "Filter Env Amount", "Amp Env Amount", "Env 3 Amount", "Env All Amount", "LPF Attack", "VCA Attack", "Env 3 Attack", "Env All Attack", "LPF Decay", "VCA Decay", "Env 3 Decay", "Env All Decay", "LPF Release", "VCA Release", "Env 3 Release", "Env All Release", "Mod 1 Amount", "Mod 2 Amount", "Mod 3 Amount", "Mod 4 Amount", "Mod 5 Amount", "Mod 6 Amount", "Mod 7 Amount", "Mod 8 Amount", "Osc Slop", "FX Mix", "FX Param 1", "FX Param 2", "Seq Slew" };
    public static final String[] LFO_FREQUENCIES = new String[] { "32 S", "16 S", "8 S", "6 S", "4 S", "3 S", "2 S", "1.5 S", "1 S", "2/3 S", "1/2 S", "1/3 S", "1/4 S", "1/6 S", "1/8 S", "1/16 S", "1/16 S" };
    public static final String[] LFO_SHAPES = new String[] { "Triangle", "Sawtooth", "Reverse Saw", "Square", "Random" };
    public static final String[] HALF_NOTES = new String[] { "C", "C+", "C#", "C#+", "D", "D+", "D#", "D#+", "E", "E+", "F", "F+", "F#", "F#+", "G", "G+", "G#", "G#+", "A", "A+", "A#", "A#+", "B", "B+" };
    public static final String[] SEQUENCER_TRIGGERS = new String[] { "Normal", "No Reset", "No Gate", "No Gate, No Reset", "Key Step" };
    public static final String[] SEQUENCER_TYPES = new String[] { "Gated", "Poly" };
    public static final String[] PAN_MODES = new String[] { "Alternate", "Fixed" };
    public static final String[] FX_TYPES = new String[] {"Off", "Delay Mono", "DDL Stereo", "BBD Delay", "Chorus", "Phaser High", "Phaser Low", "Phaser Mst", "Flanger 1", "Flanger2", "Reverb", "Ring Mod", "Distortion", "HP Filter" };
    public static final String[] PARAM1_LABELS = new String[] {"", "Time", "Time", "Time", "Rate", "Rate", "Rate", "Rate", "Rate", "Rate", "Time", "Tune", "Gain", "Cutoff" };
    public static final String[] PARAM2_LABELS = new String[] {"", "Feedback", "Feedback", "Feedback", "Depth", "Depth", "Depth", "Depth", "Depth", "Depth", "Tone", "Key Track", "Tone", "Resonance" };
    
    public static final int FILTER_ENVELOPE = 1;
    public static final int AMPLIFIER_ENVELOPE = 2;
    public static final int AUX_ENVELOPE = 3;
    
    public static final int LOAD_BOTH = 0;
    public static final int LOAD_A = 1;
    public static final int LOAD_B = 2;
    int load = LOAD_BOTH;
    
    public static final int MAXIMUM_NAME_LENGTH = 20;
    
    public static final byte PROPHET_REV2_ID = 0x2F;
    
    
    /// Mapping matrices for converting to the Prophet 08
    public static final int[] RP_ARPEGGIATOR_MODES = { 0, 1, 2, 4, 3 };
    public static final int[] RP_KEY_MODES = { 0, 3, 1, 4, 2, 5 };
    public static final int[] RP_MODULATION_DESTINATIONS = { 0, 1, 2, 3, 4, 5, 0, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 
                                                             19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 0, 0, 0, 0, 0, 0, 0, 0, 48 };               // SEQ24 slew is last
    public static final int[] RP_MODULATION_SOURCES = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 0, 0 };   // same mapping as '08, both zeroed at the end
    public static final int[] RP_LFO_SHAPES = { 0, 2, 1, 3, 4 };            // In both Rev2 and P8, 2 and 1 are flippped
    public static final int[] RP_SEQUENCER_TRIGGERS = { 0, 1, 2, 3, 4, 5 };
    public static final String[] DESTINATION_TAGS = { "env3mod", "lfo1mod", "lfo2mod", "lfo3mod", "lfo4mod", 
                                                      "mod1", "mod2", "mod3", "mod4", "mod5", "mod6", "mod7", "mod8", 
                                                      "wheel", "pressure", "breath", "velocity", "foot",
                                                      "track1", "track2", "track3", "track4" };

    
    // JComponents
    JComponent oscFiltAmpPanelB;
    JComponent modulationPanelB;
    JComponent gseqeuncerPanelB;
    JComponent polyseqPanelB;
    
    boolean sendAssignableParams;
    
    public SequentialProphetRev2()
        {
        int panel = 0;
                
        for(int i = 0; i < nrpnparameters.length; i++)
            {
            nrpnparametersToIndex.put(nrpnparameters[i], Integer.valueOf(i));
            }
        
        // Oscillators, Filter, Amp Panel A
        
        SynthPanel oscFiltAmpPanel = new SynthPanel(this);
        
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        hbox.add(addPerformance(1, Style.COLOR_B()));
        hbox.addLast(addOscillatorGlobal(1, Style.COLOR_A()));
        vbox.add(hbox);
            
        hbox = new HBox();
        hbox.add(addOscillator(1, 1, Style.COLOR_A()));
        hbox.add(addOscillator(1, 2, Style.COLOR_A()));
        hbox.addLast(addEffects(1, Style.COLOR_C()));
        vbox.add(hbox);
        
        hbox = new HBox();
        hbox.add(addFilter(1, Style.COLOR_B()));
        hbox.addLast(addEnvelope(1, FILTER_ENVELOPE, Style.COLOR_B()));
        vbox.add(hbox);
        
        hbox = new HBox();
        hbox.add(addAmplifier(1, Style.COLOR_C()));
        hbox.addLast(addEnvelope(1, AMPLIFIER_ENVELOPE, Style.COLOR_C()));
        vbox.add(hbox);
        
        hbox = new HBox();
        hbox.add(addAux(1, Style.COLOR_A()));
        hbox.addLast(addEnvelope(1, AUX_ENVELOPE, Style.COLOR_A()));
        vbox.add(hbox);        
        
        oscFiltAmpPanel.add(vbox, BorderLayout.CENTER);
        oscFiltAmpPanel.makePasteable("layer");
        oscFiltAmpPanel.setSendsAllParameters(false);
        addTab("Sound A", oscFiltAmpPanel);
                
        // Modulation Panel A
        
        SynthPanel modulationPanel = new SynthPanel(this);
        
        vbox = new VBox();
             
        hbox = new HBox();
        hbox.add(addLFO(1, 1, Style.COLOR_B()));
        hbox.add(addLFO(1, 2, Style.COLOR_B()));
        hbox.add(addLFO(1, 3, Style.COLOR_B()));
        hbox.addLast(addLFO(1, 4, Style.COLOR_B()));
        vbox.add(hbox);
        
        for(int i = 1; i <= 8; i+=4)
            {
            hbox = new HBox();
            hbox.add(addModulation(1, i, Style.COLOR_A()));
            hbox.add(addModulation(1, i+1, Style.COLOR_A()));
            hbox.add(addModulation(1, i+2, Style.COLOR_A()));
            hbox.addLast(addModulation(1, i+3, Style.COLOR_A()));
            vbox.add(hbox);
            }
        
        hbox = new HBox();
        hbox.add(addExternalModulation(1, "Velocity", "velocity", Style.COLOR_C()));
        hbox.add(addExternalModulation(1, "Aftertouch", "pressure", Style.COLOR_C()));
        hbox.addLast(addExternalModulation(1, "Mod Wheel", "wheel", Style.COLOR_C()));
        
        vbox.add(hbox);
        
        hbox = new HBox();
        hbox.add(addExternalModulation(1, "Breath", "breath", Style.COLOR_C()));
        hbox.addLast(addExternalModulation(1, "Foot Control", "foot", Style.COLOR_C()));
        vbox.add(hbox);
        
        modulationPanel.add(vbox, BorderLayout.CENTER);
        modulationPanel.makePasteable("layer");
        addTab("Modulation A", modulationPanel);
        
        // Gated Sequencer Panel A
                
        SynthPanel gseqeuncerPanel = new SynthPanel(this);
                
        vbox = new VBox();
        hbox = new HBox();
        
        hbox.add(addClock(1, Style.COLOR_A()));
        hbox.add(addArp(1, Style.COLOR_B()));
        hbox.addLast(addSequencer(1, Style.COLOR_C()));
        vbox.add(hbox);
            
        vbox.add(addSequencerTrack(1, 1, Style.COLOR_A()));
        vbox.add(addSequencerTrack(1, 2, Style.COLOR_B()));
        vbox.add(addSequencerTrack(1, 3, Style.COLOR_A()));
        vbox.add(addSequencerTrack(1, 4, Style.COLOR_B()));
        
        gseqeuncerPanel.add(vbox, BorderLayout.CENTER);
        gseqeuncerPanel.makePasteable("layer");
        addTab("Automation A", gseqeuncerPanel);
        
        // Polyphonic Sequencer Panel A
        
        SynthPanel polyseqPanel = new SynthPanel(this);
        
        vbox = new VBox();
        hbox = new HBox();
        
        vbox.add(addSeqStart(1, Style.COLOR_B()));

        vbox.add(Strut.makeVerticalStrut(10));
        vbox.add(new Category(this, "", Style.COLOR_A()));
        vbox.add(Strut.makeVerticalStrut(10));
                        
        vbox.addLast(addSteps(1, Style.COLOR_B(), Style.COLOR_A(), Style.COLOR_B()));
        
        polyseqPanel.add(vbox, BorderLayout.CENTER);
        polyseqPanel.makePasteable("layer");
        addTab("Poly Seq A", polyseqPanel);

        // Oscillators, Filter, Amp Panel B
                
        oscFiltAmpPanel = new SynthPanel(this); 
        vbox = new VBox();
        hbox = new HBox();
        hbox.add(addNameGlobal2(Style.COLOR_C()));
        hbox.add(addPerformance(2, Style.COLOR_B()));
        hbox.addLast(addOscillatorGlobal(2, Style.COLOR_A()));
        vbox.add(hbox);
            
        hbox = new HBox();
        hbox.add(addOscillator(2, 1, Style.COLOR_A()));
        hbox.add(addOscillator(2, 2, Style.COLOR_A()));
        hbox.addLast(addEffects(2, Style.COLOR_C()));
        vbox.add(hbox);
        
        hbox = new HBox();
        hbox.add(addFilter(2, Style.COLOR_B()));
        hbox.addLast(addEnvelope(2, FILTER_ENVELOPE, Style.COLOR_B()));
        vbox.add(hbox);
        
        hbox = new HBox();
        hbox.add(addAmplifier(2, Style.COLOR_C()));
        hbox.addLast(addEnvelope(2, AMPLIFIER_ENVELOPE, Style.COLOR_C()));
        vbox.add(hbox);
        
        hbox = new HBox();
        hbox.add(addAux(2, Style.COLOR_A()));
        hbox.addLast(addEnvelope(2, AUX_ENVELOPE, Style.COLOR_A()));
        vbox.add(hbox);
             
        oscFiltAmpPanel.add(vbox, BorderLayout.CENTER);
        oscFiltAmpPanel.makePasteable("layer");
        oscFiltAmpPanel.setSendsAllParameters(false);
        addTab("Sound B", oscFiltAmpPanelB = oscFiltAmpPanel);
                
        // Modulation Panel B
        
        modulationPanel = new SynthPanel(this);
                
        vbox = new VBox();
        
        hbox = new HBox();
        hbox.add(addLFO(2, 1, Style.COLOR_B()));
        hbox.add(addLFO(2, 2, Style.COLOR_B()));
        hbox.add(addLFO(2, 3, Style.COLOR_B()));
        hbox.addLast(addLFO(2, 4, Style.COLOR_B()));
        vbox.add(hbox);
        
        for(int i = 1; i <= 8; i+=4)
            {
            hbox = new HBox();
            hbox.add(addModulation(2, i, Style.COLOR_A()));
            hbox.add(addModulation(2, i+1, Style.COLOR_A()));
            hbox.add(addModulation(2, i+2, Style.COLOR_A()));
            hbox.addLast(addModulation(2, i+3, Style.COLOR_A()));
            vbox.add(hbox);
            }
        
        hbox = new HBox();
        hbox.add(addExternalModulation(2, "Velocity", "velocity", Style.COLOR_C()));
        hbox.add(addExternalModulation(2, "Aftertouch", "pressure", Style.COLOR_C()));
        hbox.addLast(addExternalModulation(2, "Mod Wheel", "wheel", Style.COLOR_C()));
        vbox.add(hbox);
        
        hbox = new HBox();
        hbox.add(addExternalModulation(2, "Breath", "breath", Style.COLOR_C()));
        hbox.addLast(addExternalModulation(2, "Foot Control", "foot", Style.COLOR_C()));
        vbox.add(hbox);
        
        modulationPanel.add(vbox, BorderLayout.CENTER);
        modulationPanel.makePasteable("layer");
        addTab("Modulation B", modulationPanelB = modulationPanel);
        
        // Gated Sequencer Panel B
                
        gseqeuncerPanel = new SynthPanel(this);
                
        vbox = new VBox();
        hbox = new HBox();
        
        hbox.add(addClock(2, Style.COLOR_A()));
        hbox.add(addArp(2, Style.COLOR_B()));
        hbox.addLast(addSequencer(2, Style.COLOR_C()));
        vbox.add(hbox);
        
        vbox.add(addSequencerTrack(2, 1, Style.COLOR_A()));
        vbox.add(addSequencerTrack(2, 2, Style.COLOR_B()));
        vbox.add(addSequencerTrack(2, 3, Style.COLOR_A()));
        vbox.add(addSequencerTrack(2, 4, Style.COLOR_B()));
        
        gseqeuncerPanel.add(vbox, BorderLayout.CENTER);
        gseqeuncerPanel.makePasteable("layer");
        addTab("Automation B", gseqeuncerPanelB = gseqeuncerPanel);
        
        // Polyphonic Sequencer Panel B
        
        polyseqPanel = new SynthPanel(this);
        
        vbox = new VBox();
        hbox = new HBox();
        
        vbox.add(addSeqStart(2, Style.COLOR_B()));
        
        vbox.add(Strut.makeVerticalStrut(10));
        vbox.add(new Category(this, "", Style.COLOR_A()));
        vbox.add(Strut.makeVerticalStrut(10));
        
        vbox.addLast(addSteps(2, Style.COLOR_B(), Style.COLOR_A(), Style.COLOR_B()));
        
        polyseqPanel.add(vbox, BorderLayout.CENTER);
        polyseqPanel.makePasteable("layer");
        addTab("Poly Seq B", polyseqPanelB = polyseqPanel);
                    
        model.set("name", "Untitled");
        model.set("bank", 0);
        model.set("number", 0);
        loadDefaults();
        }




    JComponent nameGlobal = null;
    
    /** Add the global patch category (name, id, number, etc.) */
    public JComponent addNameGlobal(Color color)
        {
        Category globalCategory = new Category(this, getSynthName(), color);
                
        JComponent comp;
        String[] params;

        HBox hbox = new HBox();                  
        VBox vbox = new VBox();
        HBox inner = new HBox();                  

        comp = new PatchDisplay(this, 5);
        inner.add(comp);

        params = KEYBOARD_MODES;
        comp = new Chooser("Keyboard Mode", this, "keyboardmode", params);
        inner.addLast(comp);
        vbox.add(inner);
 
        comp = new StringComponent("Patch Name", this, "name", MAXIMUM_NAME_LENGTH, "Name must be up to 20 characters.")
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

        comp = new LabelledDial("Split Point", this, "splitpoint", color, 0, 120)
            {
            public String map(int val)
                {
                return (NOTES[val % 12] + " " + ((val / 12) - 2));
                }
            };
        hbox.add(comp);
 
        nameGlobal = hbox;
        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }
        

    /** Add the global patch category (name, id, number, etc.) */
    public JComponent addNameGlobal2(Color color)
        {
        Category category = new Category(this, getSynthName(), color);
                
        JComponent comp;
        String[] params;

        HBox hbox = new HBox();                  
        VBox vbox = new VBox();

        comp = new StringComponent("Layer B Name", this, "layer2name", MAXIMUM_NAME_LENGTH, "Name must be up to 20 characters.")
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
        vbox.add(hbox);
        vbox.add(Strut.makeStrut(nameGlobal, false, true));

        category.add(vbox, BorderLayout.WEST);
        return category;
        }

    public JComponent addPerformance(int layer, Color color)
        {
        Category category = new Category(this, "Performance", color);
        category.makePasteable("layer");
        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        HBox hbox2 = new HBox();
            
        params = KEY_MODES;
        JComponent c2 = comp = new Chooser("Unison Key Mode", this, "layer" + layer + "unisonkeymode", params);
        vbox.add(comp);
        
        CheckBox unison = new CheckBox("Unison", this, "layer" + layer + "unison");
        unison.addToWidth(1);
        vbox.add(unison);
        
        hbox.add(vbox);
        
        comp = new LabelledDial("Unison", this, "layer" + layer + "unisonmode", color, 0, 16)
            {
            public String map(int val)
                {
                if (val <= 15) return "" + (val+1);
                else return "Chord";
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Voices");
        getModel().setMetricMax("layer" + layer + "unisonmode", 16);  // Chord is non-metric
        hbox.add(comp);
        
        comp = new LabelledDial("Unison", this, "layer" + layer + "unisondetune", color, 0, 16);
        ((LabelledDial)comp).addAdditionalLabel("Detune");
        hbox.add(comp);
        
        comp = new LabelledDial("Pitch Bend", this, "layer" + layer + "pitchbendrange", color, 0, 12);
        ((LabelledDial)comp).addAdditionalLabel("Range");
        hbox.add(comp);
        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addArp(int layer, Color color)
        {
        Category category = new Category(this, "Arpeggiator", color);
        category.makePasteable("layer");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();

        HBox hbox2 = new HBox();

        params = ARPEGGIATOR_MODES;
        JComponent c2 = comp = new Chooser("Mode", this, "layer" + layer + "arpeggiatormode", params);
        vbox.add(comp);   
        
        comp = new CheckBox("On", this, "layer" + layer + "arpeggiator");
        vbox.add(comp);
        
        comp = new CheckBox("Relatch", this, "layer" + layer + "arpeggiatorrelatch");
        vbox.add(comp);

        vbox.add(hbox2);
        
        hbox.add(vbox);

        comp = new LabelledDial("Range", this, "layer" + layer + "arpeggiatoroctaves", color, 0, 2, -1);
        hbox.add(comp);
        
        comp = new LabelledDial("Repeats", this, "layer" + layer + "arpeggiatorrepeats", color, 0, 3);
        hbox.add(comp);
        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addClock(int layer, Color color)
        {
        Category category = new Category(this, "Clock", color);
        category.makePasteable("layer");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        HBox hbox2 = new HBox();
        
        params = CLOCK_DIVIDES;
        JComponent c3 = comp = new Chooser("Clock Divide", this, "layer" + layer + "clockdivide", params);
        hbox2.add(comp);

        vbox.add(hbox2);

        hbox.add(vbox);
        
        comp = new LabelledDial("BPM", this, "layer" + layer + "tempo", color, 30, 250);
        hbox.add(comp);
        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addOscillatorGlobal(int layer, Color color)
        {
        Category category = new Category(this, "Oscillators", color);
        category.makePasteable("layer");
        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();   
        VBox vbox = new VBox();
        
        params = GLIDE_MODES;
        comp = new Chooser("Glide Mode", this, "layer" + layer + "glidemode", params);
        vbox.add(comp);
            
        comp = new CheckBox("Glide", this, "layer" + layer + "glideonoff");
        vbox.add(comp);
        
        comp = new CheckBox("Sync", this, "layer" + layer + "sync");
        vbox.add(comp);
            
        hbox.add(vbox);
            
        comp = new LabelledDial("Sub Octave", this, "layer" + layer + "subosc", color, 0, 127);
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
        ((LabelledDial)comp).addAdditionalLabel("Osc 1<>2");
        hbox.add(comp);
                
        comp = new LabelledDial("Noise", this, "layer" + layer + "noise", color, 0, 127);
        hbox.add(comp);
        
        comp = new LabelledDial("Slop", this, "layer" + layer + "slop", color, 0, 127);
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
        
            
    public JComponent addOscillator(int layer, int osc, Color color)
        {
        Category category = new Category(this, "Oscillator " + osc, color);
        category.makePasteable("layer" + layer + "dco");
        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
                   
        VBox vbox = new VBox();
        params = SHAPES;
        comp = new Chooser("Shape", this, "layer" + layer + "dco" + osc + "shape", params);
        vbox.add(comp);
            
        comp = new CheckBox("Key", this, "layer" + layer + "dco" + osc + "key");
        vbox.add(comp);
            
        comp = new CheckBox("Note Reset", this, "layer" + layer + "dco" + osc + "notereset");
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
            
        comp = new LabelledDial("Shape Mod", this, "layer" + layer + "dco" + osc + "shapemod", color, 0, 99);
        hbox.add(comp);
            
        comp = new LabelledDial("Glide", this, "layer" + layer + "dco" + osc + "glide", color, 0, 127);
        hbox.add(comp);
                    
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
    
    
    JComponent filter = null;  
    
    public JComponent addFilter(int layer, Color color)
        {
        Category category = new Category(this, "Low-Pass Filter", color);
        category.makePasteable("layer");
        
        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();
        VBox vbox = new VBox();
            
        comp = new CheckBox("4-Pole", this, "layer" + layer + "vcfpoles");
        vbox.add(comp);
        hbox.add(vbox);
           
        comp = new LabelledDial("Cutoff", this, "layer" + layer + "vcffrequency", color, 0, 164);
        hbox.add(comp);
                    
        comp = new LabelledDial("Resonance", this, "layer" + layer + "vcfresonance", color, 0, 127);
        hbox.add(comp);
        
        comp = new LabelledDial("Key Amount", this, "layer" + layer + "vcfkeyboardamount", color, 0, 127);
        hbox.add(comp);
                    
        comp = new LabelledDial("Audio Mod", this, "layer" + layer + "vcfaudiomodulation", color, 0, 127);
        hbox.add(comp);
                       
        if (layer == 1) filter = hbox;
        category.add(hbox, BorderLayout.WEST);
        return category;
        }
        
        
    public JComponent addAmplifier(int layer, Color color)
        {
        Category category = new Category(this, "Amplifier", color);
        category.makePasteable("layer");
                   
        JComponent comp;
        String[] params;
        VBox outer = new VBox();
        final HBox hbox = new HBox();
        VBox vbox = new VBox();
            
        params = PAN_MODES;
        comp = new Chooser("Pan Mode", this, "layer" + layer + "panmode", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Pan", this, "layer" + layer + "vcaoutputspread", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Spread");
        hbox.add(comp);
                   
        comp = new LabelledDial("Initial", this, "layer" + layer + "vcainitiallevel", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);
        
        comp = new LabelledDial("Program", this, "layer" + layer + "vcavoicevolume", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Volume");
        hbox.add(comp);
        
        outer.add(hbox);
        outer.add(Strut.makeStrut(filter, false, true));   
        category.add(outer, BorderLayout.WEST);
        return category;
        }
    
    
    public JComponent addAux(int layer,  Color color)
        {
        Category category = new Category(this, "Aux", color);
        category.makePasteable("layer");
                    
        JComponent comp;
        VBox outer = new VBox();
        String[] params;
        final HBox hbox = new HBox();
        VBox vbox = new VBox();

        params = MODULATION_DESTINATIONS;
        comp = new Chooser("Mod Destination", this, "layer" + layer + "env3" + "moddestination", params);
        vbox.add(comp);

        comp = new CheckBox("Repeat", this, "layer" + layer + "env3" + "repeat");
        vbox.add(comp);
        hbox.add(vbox);
            
        outer.add(hbox);
        outer.add(Strut.makeStrut(filter, false, true));        
        category.add(outer, BorderLayout.WEST);
        return category;
        }


    public JComponent addEnvelope(int layer, final int env, Color color)
        {
        Category category = new Category(this, 
                (env == FILTER_ENVELOPE ? "Filter Envelope" : 
                (env == AMPLIFIER_ENVELOPE ? "Amplifier Envelope" : "Auxiliary Envelope")), color);
        category.makePasteable("layer" + layer + "env");
                    
        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();
            
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
                    
        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "layer" + layer + "env" + env + "delay", "layer" + layer + "env" + env + "attack", "layer" + layer + "env" + env + "decay", null,"layer" + layer +  "env" + env + "release" },
            new String[] { null, null, null, "layer" + layer + "env" + env + "sustain", "layer" + layer + "env" + env + "sustain", null },
            new double[] { 0, 0.2/127.0, 0.2/127.0, 0.2 / 127.0,  0.2, 0.2/127.0},
            new double[] { 0, 0, 1.0, 1.0 / 127.0, 1.0/127.0, 0 });
        hbox.addLast(comp);
        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addExternalModulation(int layer, String title, String key, Color color)
        {
        Category category = new Category(this, title, color);
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


    public JComponent addLFO(int layer, final int lfo, Color color)
        {
        Category category = new Category(this, "LFO " + lfo, color);
        category.makePasteable("layer" + layer + "lfo");
                    
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        VBox vbox2 = new VBox();
        
        params = LFO_SHAPES;
        comp = new Chooser("Shape", this, "layer" + layer + "lfo" + lfo + "shape", params);
        vbox.add(comp);
        
        params = MODULATION_DESTINATIONS;
        comp = new Chooser("Mod Destination", this, "layer" + layer + "lfo" + lfo + "moddestination", params);
        vbox.add(comp);
        
        final JComponent freq = new LabelledDial("Frequency", this, "layer" + layer + "lfo" + lfo + "frequency", color, 0, 150)
            {
            public String map(int val)
                {
                if (model.get("layer" + layer + "lfo" + lfo + "clocksync") == 0) return "" + val;
                else return LFO_FREQUENCIES[val/9];
                }
            };
        
        comp = new CheckBox("Clock Sync", this, "layer" + layer + "lfo" + lfo + "clocksync")
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                freq.repaint();
                }
            };
        vbox.add(comp);
            
        comp = new CheckBox("Key Sync", this, "layer" + layer + "lfo" + lfo + "keysync");
        vbox.add(comp);
            
        hbox.add(vbox);
        
        vbox2.add(freq);
                    
        comp = new LabelledDial("Amount", this, "layer" + layer + "lfo" + lfo + "amount", color, 0, 127);
        vbox2.add(comp);
        
        hbox.add(vbox2);
                                    
        category.add(hbox, BorderLayout.WEST);
        return category;
        }


    public JComponent addModulation(int layer, int mod, Color color)
        {
        Category category  = new Category(this, "Modulation " + mod, color);
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


    public JComponent addSequencer(int layer, Color color)
        {
        final Category category  = new Category(this, "Sequencer", color);
        category.makePasteable("layer");
        category.makeDistributable("layer");
                           
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();        
        VBox vbox = new VBox();
            
        vbox = new VBox();
        params = SEQUENCER_TYPES;
        comp = new Chooser("Type", this, "layer" + layer + "gatedpolyseq", params);
        vbox.add(comp);
            
        params = SEQUENCER_TRIGGERS;
        comp = new Chooser("Gated Sequencer Mode", this, "layer" + layer + "sequencertrigger", params);
        vbox.add(comp);
        hbox.add(vbox);
        
        vbox = new VBox();
        for(int track = 1; track <= 4; track++)
            {
            params = (track == 2 || track == 4 ? MODULATION_DESTINATIONS_SEQ24 : MODULATION_DESTINATIONS);
            comp = new Chooser("Track " + track + " Destination", this, "layer" + layer + "track" + track + "destination", params)
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
            if (track == 2)
                {
                hbox.add(vbox);
                vbox = new VBox();
                }
            }
        hbox.add(vbox);
        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
        

    public JComponent addSequencerTrack(final int layer, final int track, Color color)
        {
        Category category = new Category(this, "Track " + track, color);
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
        
    
    public JComponent addEffects(int layer, Color color)
        {
        final Category category  = new Category(this, "Effects", color);
        category.makePasteable("layer");
            
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        final JComponent param1 = new LabelledDial("Param 1", this, "layer" + layer + "fxparam1", color, 0, 255);         
        final JComponent param2 = new LabelledDial("Param 2", this, "layer" + layer + "fxparam2", color, 0, 127)
            {
            public String map(int val)
                {
                int select = model.get("layer" + layer + "fxselect");
                if (select != 11) return "" + val;
                else if (val == 0) return "Off";
                else return "On";
                }
            };
        
        params = FX_TYPES;
        comp = new Chooser("Type", this, "layer" + layer + "fxselect", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                ((LabelledDial)param1).setLabel(PARAM1_LABELS[model.get("layer" + layer + "fxselect")]);
                ((LabelledDial)param2).setLabel(PARAM2_LABELS[model.get("layer" + layer + "fxselect")]);
                param2.repaint();
                };
            };
        vbox.add(comp);
        
        comp = new CheckBox("On", this, "layer" + layer + "fxonoff");
        vbox.add(comp);

        comp = new CheckBox("Clock Sync", this, "layer" + layer + "fxclocksync");
        vbox.add(comp);
            
        hbox.add(vbox);
                    
        comp = new LabelledDial("Mix", this, "layer" + layer + "fxmix", color, 0, 127);
        hbox.add(comp);

        hbox.add(param1);
        hbox.add(param2);
            
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addSeqStart(int layer, Color color)
        {
        final Category category  = new Category(this, "Polyphonic Sequencer", color);
        category.makePasteable("layer");
        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        PushButton button = new PushButton("Play")
            {
            public void perform()
                {
                Object[] nrpn = buildNRPN(getChannelOut(), 164 + 2048 * (layer - 1), 1);
                tryToSendMIDI(nrpn);
                }
            };
        vbox.add(button);
        hbox.add(vbox);
        
        vbox = new VBox();
        PushButton stopbutton = new PushButton("Stop")
            {
            public void perform()
                {
                Object[] nrpn = buildNRPN(getChannelOut(), 164 + 2048 * (layer - 1), 0);
                tryToSendMIDI(nrpn);
                }
            };
        vbox.add(stopbutton);
        hbox.add(vbox);
        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
        
    
    public JComponent addSteps(int layer, Color color, Color colorA, Color colorB)
        {
        JComponent comp;
        String[] params;
        
        final JComponent typical = addStep(layer, 1, color);          // throwaway
        final int h = typical.getPreferredSize().height;
        final int w = typical.getPreferredSize().width;
        
        VBox steps = new VBox()
            {
            public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
                {
                if (orientation == SwingConstants.VERTICAL)
                    return w;
                else
                    return h;
                }

            public Dimension getPreferredScrollableViewportSize()
                {
                Dimension size = getPreferredSize();
                size.height = h * 3;
                return size;
                }
            };
                
        for(int i = 1; i <= 64; i++)
            {
            JComponent step = addStep(layer, i, (i % 2 == 0 ? colorA : colorB));
            steps.add(step);
            }

        JScrollPane pane = new JScrollPane(steps, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        pane.getViewport().setBackground(Style.BACKGROUND_COLOR());
        pane.setBorder(null);
                
        return pane;
        }
        

    public JComponent addStep(int layer, int step, Color color)
        {
        Category category = new Category(this, "Step " + step, color);
        category.makePasteable("layer" + layer + "step");
        category.makeDistributable("step");
        
        JComponent comp;
        String[] params;
        VBox vbox = new VBox();
        HBox hbox = new HBox();
 
        for(int i = 1; i <= 6; i++)
            {
            comp = new LabelledDial("Note " + i, this, "layer" + layer + "step" + step + "note" + i, color, 0, 128)
                {
                public String map(int val)
                    {
                    if (val <= 127)
                        {
                        return (NOTES[val % 12] + " " + (val / 12));
                        }
                    else return "Tie";
                    }
                };
            getModel().setMetricMax("layer" + layer + "step" + step + "note" + i, 127);  // Tie is non-metric
            hbox.add(comp);

            comp = new LabelledDial("Velocity " + i, this, "layer" + layer + "step" + step + "velocity" + i, color, 0, 128)
                {
                public String map(int val)
                    {
                    if (val == 1) return "Rest";
                    else if (val == 0) return "Reset";
                    else return "" + (val - 1);
                    }
                };
            // getModel().setMetricMin("layer" + layer + "step" + step + "velocity" + i, 128);  // Rest is non-metric
            hbox.add(comp);

            hbox.add(Strut.makeHorizontalStrut(16));
            }
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
    
    
    public void changePatch(Model tempModel)
        {
        changePatch(tempModel.get("bank"), tempModel.get("number"));
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


    public boolean gatherPatchInfo(String title, Model changeThis, boolean writing)     
        {
        String[] banks = BANKS_PROPHET;
                
        JComboBox bank = new JComboBox(banks);
        int num = model.get("number") + 1;
        JTextField number = new SelectedTextField("" + (num < 10 ? "00" : (num < 100 ? "0" : "")) + num, 3);
        
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
                        
            changeThis.set("bank", i);
            changeThis.set("number", n);
                        
            return true;
            }
        }
        

    public int parse(byte[] data, boolean fromFile)
        { 
        // unfortunately, the Prophet Rev2 doesn't provide number/bank info
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
            System.err.println("Warning (Prophet Rev2): Unknown program data format " + data[3]);
        for(int i = 0; i < 2046; i++)
            {
            int j = i;
                                
            if (load == LOAD_A && !parameters[i].startsWith("layer1"))
                {
                // do nothing -- they're not layer 1 (A) parameters
                }
            else if (load == LOAD_B && !parameters[i].startsWith("layer2"))
                {
                // do nothing -- they're not layer 2 (B) parameters
                }
            else if (!parameters[i].equals("---"))
                {
                // Note: Sequential isn't 2's complement.  So everything in the model is being stored starting at 0
                int q = d[j];
                if ((Pattern.matches("layer\\dstep\\d+note\\d", parameters[i])) && ((q & 0xFF) > 128)) q = 128;
                if (Pattern.matches("layer\\dstep\\d+velocity\\d", parameters[i]))
                    {
                    q = (q & 0xFF) - 127;
                    if (q < 0) q = 0;
                    }
                if (q < 0) q += 256;  // push to unsigned (not 2's complement)
                model.set(parameters[i], q);
                }
            }
        
        if ((load == LOAD_BOTH) || (load == LOAD_A))
            {
            // handle name specially
            byte[] name = new byte[MAXIMUM_NAME_LENGTH];
            System.arraycopy(d, 235, name, 0, MAXIMUM_NAME_LENGTH);
            try
                {
                model.set("name", new String(name, "US-ASCII"));
                }
            catch (UnsupportedEncodingException e)
                {
                Synth.handleException(e); 
                }
            }
        
        if ((load == LOAD_BOTH) || (load == LOAD_B))
            {
            byte[] layer2name = new byte[MAXIMUM_NAME_LENGTH];
            System.arraycopy(d, 1259, layer2name, 0, MAXIMUM_NAME_LENGTH);
            try
                {
                model.set("layer2name", new String(layer2name, "US-ASCII"));
                }
            catch (UnsupportedEncodingException e)
                {
                Synth.handleException(e); 
                }
            }

        revise();
        return PARSE_SUCCEEDED; 
        }
        
    // converts all but last byte (F7)
    byte[] convertTo8Bit(byte[] data, int offset)
        {
        // How big?
        int size = (data.length - offset - 1) / 8 * 7;          // - 1 is for the 0xF7
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

    public static String getSynthName() 
        { 
        return "Sequential Prophet Rev2"; 
        }
    
    public String getDefaultResourceFileName() 
        {
        return "SequentialProphetRev2.init";
        }
        
    public String getHTMLResourceFileName() 
        { 
        return "SequentialProphetRev2.html";
        }

    public String getPatchLocationName(Model model)
        {
        if (!model.exists("number")) return null;
        if (!model.exists("bank")) return null;
        
        int number = (model.get("number") + 1);
        return ((BANKS_PROPHET[model.get("bank")]) + "-" + (number > 99 ? "" : (number > 9 ? "0" : "00")) + number);
        }

    public Model getNextPatchLocation(Model model)
        {
        int bank = model.get("bank");
        int number = model.get("number");
        
        int numBanks = BANKS_PROPHET.length;
        
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

    public String getPatchName(Model model) 
        {
        return model.get("name", "Untitled");
        }

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
    
    public void revise()
        {
        super.revise();
        
        String nm = model.get("name", "Untitled");
        String newnm = revisePatchName(nm);
        if (!nm.equals(newnm))
            model.set("name", newnm);
        }

    /** Map of parameter -> index in the allParameters array. */
    HashMap parametersToIndex = new HashMap();

    final static String[] parameters = new String[]
    {
    /** LAYER A: */
                
    "layer1dco1frequency",              // 0      OSC1 Freq (0-120)
    "layer1dco2frequency",              // 1      OSC1 Freq (0-120)
    "layer1dco1finetune",               // 2      OSC1 Finetune (0-100)
    "layer1dco2finetune",               // 3      OSC2 Finetune (0-100)
    "layer1dco1shape",                  // 4      OSC1 Shape (0-4, off/Saw/SawTri/Tri/Pul)
    "layer1dco2shape",                  // 5      OSC2 Shape (0-4, off/Saw/SawTri/Tri/Pul)
    "layer1dco1shapemod",               // 6      OSC1 ShapeMod (0-99)
    "layer1dco2shapemod",               // 7      OSC2 ShapeMod (0-99)
    
    "layer1dco1glide",                  // 8      OSC1 Glide (0-127)
    "layer1dco2glide",                  // 9      OSC2 Glide (0-127)
    "layer1dco1key",                    // 10      OSC1 Key on/off (0-1, off/on)
    "layer1dco2key",                    // 11      OSC2 Key on/off (0-1, off/on)
    "layer1dco1notereset",              // 12      OSC1 Note Reset (0-1, off/on)
    "layer1dco2notereset",              // 13      OSC2 Note Reset (0-1, off/on)        
    "layer1mix",                        // 14      OSC Mix (0-127)
    "layer1subosc",                     // 15      SubOct Level (0-127)
    
    "layer1noise",                      // 16      Noise Level (0-127)
    "layer1sync",                       // 17      Sync (0-1, off/on)
    "layer1glidemode",                  // 18      Glide Mode (0-3, Fixed Rate/Fixed Rate A/Fixed Time/Fixed Time A)
    "layer1glideonoff",                 // 19      Glide on/off (0-1, off/on)
    "layer1pitchbendrange",             // 20      Pitch Bend Range (0-12)
    "layer1slop",                       // 21      OSC Slop (0-127)
    "layer1vcffrequency",               // 22      Cutoff Freq (0-164)
    "layer1vcfresonance",               // 23      Resonance (0-127)
        
    "layer1vcfkeyboardamount",          // 24      Filter Key Amount (0-127)    
    "layer1vcfaudiomodulation",         // 25      Filter Audio Mod (0-127)
    "layer1vcfpoles",                   // 26      Filter Poles (0-1, 2pole/4pole)
    "layer1vcainitiallevel",            // 27      VCA Level (0-127)
    "layer1vcavoicevolume",             // 28      Program Volume (0-127)
    "layer1vcaoutputspread",            // 29      Pan Spread (0-127)
    "layer1env3moddestination",         // 30      Env3 Destination (0-52, *see table below)
    "layer1env3repeat",                 // 31      Env3 Repeat (0-1, off/on)
        
    "layer1env1amount",                 // 32      EnvF Amount (0-254)
    "layer1env2amount",                 // 33      EnvA Amount (0-127)
    "layer1env3amount",                 // 34      Env3 Amount (0-254)
    "layer1env1velocityamount",         // 35      EnvF Velocity (0-127)
    "layer1env2velocityamount",         // 36      EnvA Velocity (0-127)
    "layer1env3velocityamount",         // 37      Env3 Velocity (0-127)
    "layer1env1delay",                  // 38      EnvF Delay (0-127)
    "layer1env2delay",                  // 39      EnvA Delay (0-127)
    
    "layer1env3delay",                  // 40      Env3 Delay (0-127)
    "layer1env1attack",                 // 41      EnvF Attack (0-127)
    "layer1env2attack",                 // 42      EnvA Attack (0-127)
    "layer1env3attack",                 // 43      Env3 Attack (0-127)
    "layer1env1decay",                  // 44      EnvF Decay (0-127)
    "layer1env2decay",                  // 45      EnvA Decay (0-127)
    "layer1env3decay",                  // 46      Env3 Decay (0-127)
    "layer1env1sustain",                // 47      EnvF Sustain (0-127)
    
    "layer1env2sustain",                // 48      EnvA Sustain (0-127)
    "layer1env3sustain",                // 49      Env3 Sustain (0-127)
    "layer1env1release",                // 50      EnvF Release (0-127)
    "layer1env2release",                // 51      EnvA Release (0-127)
    "layer1env3release",                // 52      Env3 Release (0-127)
    "layer1lfo1frequency",              // 53      LFO1 Rate (0-150)
    "layer1lfo2frequency",              // 54      LFO2 Rate (0-150)
    "layer1lfo3frequency",              // 55      LFO3 Rate (0-150)
    
    "layer1lfo4frequency",              // 56      LFO4 Rate (0-150)
    "layer1lfo1shape",                  // 57      LFO1 Shape (0-5, off/Tri/Saw/RevSaw/Square/Random)
    "layer1lfo2shape",                  // 58      LFO2 Shape (0-5, off/Tri/Saw/RevSaw/Square/Random)
    "layer1lfo3shape",                  // 59      LFO3 Shape (0-5, off/Tri/Saw/RevSaw/Square/Random)
    "layer1lfo4shape",                  // 60      LFO4 Shape (0-5, off/Tri/Saw/RevSaw/Square/Random)
    "layer1lfo1amount",                 // 61      LFO1 Amount (0-127)
    "layer1lfo2amount",                 // 62      LFO2 Amount (0-127)
    "layer1lfo3amount",                 // 63      LFO3 Amount (0-127)
    
    "layer1lfo4amount",                 // 64      LFO4 Amount (0-127)
    "layer1lfo1moddestination",         // 65      LFO1 Destination (0-52, *see table below)
    "layer1lfo2moddestination",         // 66      LFO2 Destination (0-52, *see table below)
    "layer1lfo3moddestination",         // 67      LFO3 Destination (0-52, *see table below)
    "layer1lfo4moddestination",         // 68      LFO4 Destination (0-52, *see table below)
    "layer1lfo1clocksync",              // 69      LFO1 ClkSync (0-1, 0ff/on)
    "layer1lfo2clocksync",              // 70      LFO2 ClkSync (0-1, 0ff/on)
    "layer1lfo3clocksync",              // 71      LFO3 ClkSync (0-1, 0ff/on)
    
    "layer1lfo4clocksync",              // 72      LFO4 ClkSync (0-1, 0ff/on)
    "layer1lfo1keysync",                // 73      LFO1 KeySync (0-1, off/on)
    "layer1lfo2keysync",                // 74      LFO2 KeySync (0-1, off/on)
    "layer1lfo3keysync",                // 75      LFO3 KeySync (0-1, off/on)
    "layer1lfo4keysync",                // 76      LFO4 KeySync (0-1, off/on)
    "layer1mod1source",                 // 77      Mod1 Source (0-22, *see table below)
    "layer1mod2source",                 // 78      Mod2 Source (0-22, *see table below)
    "layer1mod3source",                 // 79      Mod3 Source (0-22, *see table below)
    
    "layer1mod4source",                 // 80      Mod4 Source (0-22, *see table below)
    "layer1mod5source",                 // 81      Mod5 Source (0-22, *see table below)
    "layer1mod6source",                 // 82      Mod6 Source (0-22, *see table below)
    "layer1mod7source",                 // 83      Mod7 Source (0-22, *see table below)
    "layer1mod8source",                 // 84      Mod8 Source (0-22, *see table below)
    "layer1mod1amount",                 // 85      Mod1 Amount (0-254)
    "layer1mod2amount",                 // 86      Mod2 Amount (0-254)
    "layer1mod3amount",                 // 87      Mod3 Amount (0-254)
    
    "layer1mod4amount",                 // 88      Mod4 Amount (0-254)
    "layer1mod5amount",                 // 89      Mod5 Amount (0-254)
    "layer1mod6amount",                 // 90      Mod6 Amount (0-254)
    "layer1mod7amount",                 // 91      Mod7 Amount (0-254)
    "layer1mod8amount",                 // 92      Mod8 Amount (0-254)
    "layer1mod1destination",            // 93      Mod1 Destination (0-52, *see table below)
    "layer1mod2destination",            // 94      Mod2 Destination (0-52, *see table below)    
    "layer1mod3destination",            // 95      Mod3 Destination (0-52, *see table below)
    
    "layer1mod4destination",            // 96      Mod4 Destination (0-52, *see table below)
    "layer1mod5destination",            // 97      Mod5 Destination (0-52, *see table below)
    "layer1mod6destination",            // 98      Mod6 Destination (0-52, *see table below)
    "layer1mod7destination",            // 99      Mod7 Destination (0-52, *see table below)
    "layer1mod8destination",            // 100      Mod8 Destination (0-52, *see table below)
    "layer1wheelamount",                // 101      Mod Wheel Amount (0-254)
    "layer1wheeldestination",           // 102      Mod Wheel Destination (0-52, *see table below)
    "layer1pressureamount",             // 103      Pressure Mod Amount (0-254)
    
    "layer1pressuredestination",        // 104      Pressure Mod Destination (0-52, *see table below)
    "layer1breathamount",               // 105      Breath Mod Amount (0-254)
    "layer1breathdestination",          // 106      Breath Mod Destination (0-52, *see table below) 
    "layer1velocityamount",             // 107      Velocity Mod Amount (0-254)
    "layer1velocitydestination",        // 108      Velocity Mod Destination (0-52, *see table below) 
    "layer1footamount",                 // 109      MIDI Foot Mod Amount (0-254)
    "layer1footdestination",            // 110      MIDI Foot Mod Destination (0-52, *see table below)
    "layer1track1destination",          // 111      Gated Seq1 Destination (0-52, *see table below)
    
    "layer1track2destination",          // 112      Gated Seq2 Destination (0-53, *see table below)
    "layer1track3destination",          // 113      Gated Seq3 Destination (0-52, *see table below)
    "layer1track4destination",          // 114      Gated Seq4 Destination (0-53, *see table below)
    "layer1fxselect",                   // 115      FX Select (0-13, Off/DM/DDS/BBD/Ch/PH/PL/PM/F1/F2/Rvb/RM/Dst/HPF)
    "layer1fxonoff",                    // 116      FX on/off (0-1, off/on)
    "layer1fxmix",                      // 117          FX mix
    "layer1fxparam1",                   // 118      FX Parameter 1 (0-255)
    "layer1fxparam2",                   // 119      FX Parameter 2 (0-255)
    
    "layer1fxclocksync",                // 120      FX Clock Sync on/off (0-1, off/on)
    "---",                              // 121      -
    "layer1unisonkeymode",              // 122      Key Mode (0-5, Low/Hi/Last/LowR/HiR/LastR)
    "layer1unison",                     // 123      Unison on/off (0-1, off/on)
    "layer1unisonmode",                 // 124      Unison Mode (0-15/Chord)
    "---",                              // 125      -
    "---",                              // 126      -
    "---",                              // 127      -
    
    "---",                              // 128      -
    "---",                              // 129      -
    "layer1tempo",                      // 130      BPM (30-250)
    "layer1clockdivide",                // 131      Divide (0-12, H/Q/8th/8H/8S/8T/16th/16H/16S/16T/32nd/32T/64T
    "layer1arpeggiatormode",            // 132      Arp Mode (0-4, Up/Down/Up+Down/Random/assign)
    "layer1arpeggiatoroctaves",         // 133      Arp Range (0-2, 1Oct/2Oct/3Oct)
    "layer1arpeggiatorrepeats",         // 134      Arp Repeats (0-3)
    "layer1arpeggiatorrelatch",         // 135      Arp Relatch on/off (0-1, off/on)
    
    "layer1arpeggiator",                // 136      Arp on/off (0-1, off/on)
    "---",                              // 137      -
    "layer1sequencertrigger",           // 138      Sequencer Mode (0-4, Normal/NoReset/NoGate/NoGateReset/KeyStep)
    "layer1gatedpolyseq",               // 139      Sequencer Type (0-1, Gated/Poly)
    
    "layer1track1note1",                // 140      Gated Seq1 Step 1-16 (0-125/reset/rest)
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
    "layer1track1note16",               // 155
   
    "layer1track2note1",                // 156      Gated Seq2 Step 1-16 (0-125/reset/rest) 
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
    "layer1track2note16",               // 171
    
    "layer1track3note1",                // 172      Gated Seq3 Step 1-16 (0-125/reset/rest)  
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
    "layer1track3note16",               // 187
    
    "layer1track4note1",                // 188      Gated Seq4 Step 1-16 (0-125/reset/rest)    
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
    "layer1track4note16",               // 203
    
    
    "---",                              // 204      -
    "---",                              // 205      -
    "---",                              // 206      -
    "---",                              // 207      -
    
    "layer1unisondetune",               // 208      Unison Detune (0-16)
    "layer1panmode",                    // 209      Pan Mod Mode (0-1, Alternate/Fixed)
    "---",                              // 210      -
    "---",                              // 211      -
    "---",                              // 212      -
    "---",                              // 213      -
    "---",                              // 214      -
    "---",                              // 215      -
    
    "---",                              // 216      -
    "---",                              // 217      -
    "---",                              // 218      -
    "---",                              // 219      -
    "---",                              // 220      -
    "---",                              // 221      -
    "---",                              // 222      -
    "---",                              // 223      -
    
    "---",                              // 224      -
    "---",                              // 225      -
    "---",                              // 226      -
    "---",                              // 227      -
    "---",                              // 228      -
    "---",                              // 229      -
    "---",                              // 230      -
    "keyboardmode",                     // 231      Layer Mode (0-2, LayerA/SplitAB/StackAB)
    
    "splitpoint",                       // 232      Split Point (0-120)
    "---",                              // 233      -
    "---",                              // 234      -
    "---",                              // 235-     Layer A Name
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
    "---",                              // 254
    "---",                              // 255      -
       
    "layer1step1note1",                 // 256-319  Poly Seq Track1 Notes (bit0-7;note, bit8;tie)  
    "layer1step2note1",
    "layer1step3note1",
    "layer1step4note1",
    "layer1step5note1",
    "layer1step6note1",
    "layer1step7note1",
    "layer1step8note1",
    "layer1step9note1",
    "layer1step10note1",
    "layer1step11note1",
    "layer1step12note1",
    "layer1step13note1",
    "layer1step14note1",
    "layer1step15note1",
    "layer1step16note1",
    "layer1step17note1",
    "layer1step18note1",
    "layer1step19note1",
    "layer1step20note1",
    "layer1step21note1",
    "layer1step22note1",
    "layer1step23note1",
    "layer1step24note1",
    "layer1step25note1",
    "layer1step26note1",
    "layer1step27note1",
    "layer1step28note1",
    "layer1step29note1",
    "layer1step30note1",
    "layer1step31note1",
    "layer1step32note1",
    "layer1step33note1",
    "layer1step34note1",
    "layer1step35note1",
    "layer1step36note1",
    "layer1step37note1",
    "layer1step38note1",
    "layer1step39note1",
    "layer1step40note1",
    "layer1step41note1",
    "layer1step42note1",
    "layer1step43note1",
    "layer1step44note1",
    "layer1step45note1",
    "layer1step46note1",
    "layer1step47note1",
    "layer1step48note1",
    "layer1step49note1",
    "layer1step50note1",
    "layer1step51note1",
    "layer1step52note1",
    "layer1step53note1",
    "layer1step54note1",
    "layer1step55note1",
    "layer1step56note1",
    "layer1step57note1",
    "layer1step58note1",
    "layer1step59note1",
    "layer1step60note1",
    "layer1step61note1",
    "layer1step62note1",
    "layer1step63note1",
    "layer1step64note1",
    
    "layer1step1velocity1",             // 320-383  Poly Seq Track1 Velocities (bit0-7;velocity, bit8;rest)  
    "layer1step2velocity1",
    "layer1step3velocity1",
    "layer1step4velocity1",
    "layer1step5velocity1",
    "layer1step6velocity1",
    "layer1step7velocity1",
    "layer1step8velocity1",
    "layer1step9velocity1",
    "layer1step10velocity1",
    "layer1step11velocity1",
    "layer1step12velocity1",
    "layer1step13velocity1",
    "layer1step14velocity1",
    "layer1step15velocity1",
    "layer1step16velocity1",
    "layer1step17velocity1",
    "layer1step18velocity1",
    "layer1step19velocity1",
    "layer1step20velocity1",
    "layer1step21velocity1",
    "layer1step22velocity1",
    "layer1step23velocity1",
    "layer1step24velocity1",
    "layer1step25velocity1",
    "layer1step26velocity1",
    "layer1step27velocity1",
    "layer1step28velocity1",
    "layer1step29velocity1",
    "layer1step30velocity1",
    "layer1step31velocity1",
    "layer1step32velocity1",
    "layer1step33velocity1",
    "layer1step34velocity1",
    "layer1step35velocity1",
    "layer1step36velocity1",
    "layer1step37velocity1",
    "layer1step38velocity1",
    "layer1step39velocity1",
    "layer1step40velocity1",
    "layer1step41velocity1",
    "layer1step42velocity1",
    "layer1step43velocity1",
    "layer1step44velocity1",
    "layer1step45velocity1",
    "layer1step46velocity1",
    "layer1step47velocity1",
    "layer1step48velocity1",
    "layer1step49velocity1",
    "layer1step50velocity1",
    "layer1step51velocity1",
    "layer1step52velocity1",
    "layer1step53velocity1",
    "layer1step54velocity1",
    "layer1step55velocity1",
    "layer1step56velocity1",
    "layer1step57velocity1",
    "layer1step58velocity1",
    "layer1step59velocity1",
    "layer1step60velocity1",
    "layer1step61velocity1",
    "layer1step62velocity1",
    "layer1step63velocity1",
    "layer1step64velocity1",

    "layer1step1note2",                 // 384-447  Poly Seq Track2 Notes (bit0-7;note, bit8;tie)
    "layer1step2note2",
    "layer1step3note2",
    "layer1step4note2",
    "layer1step5note2",
    "layer1step6note2",
    "layer1step7note2",
    "layer1step8note2",
    "layer1step9note2",
    "layer1step10note2",
    "layer1step11note2",
    "layer1step12note2",
    "layer1step13note2",
    "layer1step14note2",
    "layer1step15note2",
    "layer1step16note2",
    "layer1step17note2",
    "layer1step18note2",
    "layer1step19note2",
    "layer1step20note2",
    "layer1step21note2",
    "layer1step22note2",
    "layer1step23note2",
    "layer1step24note2",
    "layer1step25note2",
    "layer1step26note2",
    "layer1step27note2",
    "layer1step28note2",
    "layer1step29note2",
    "layer1step30note2",
    "layer1step31note2",
    "layer1step32note2",
    "layer1step33note2",
    "layer1step34note2",
    "layer1step35note2",
    "layer1step36note2",
    "layer1step37note2",
    "layer1step38note2",
    "layer1step39note2",
    "layer1step40note2",
    "layer1step41note2",
    "layer1step42note2",
    "layer1step43note2",
    "layer1step44note2",
    "layer1step45note2",
    "layer1step46note2",
    "layer1step47note2",
    "layer1step48note2",
    "layer1step49note2",
    "layer1step50note2",
    "layer1step51note2",
    "layer1step52note2",
    "layer1step53note2",
    "layer1step54note2",
    "layer1step55note2",
    "layer1step56note2",
    "layer1step57note2",
    "layer1step58note2",
    "layer1step59note2",
    "layer1step60note2",
    "layer1step61note2",
    "layer1step62note2",
    "layer1step63note2",
    "layer1step64note2",
    
    "layer1step1velocity2",             // 448-511  Poly Seq Track2 Velocities (bit0-7;velocity, bit8;rest)
    "layer1step2velocity2",
    "layer1step3velocity2",
    "layer1step4velocity2",
    "layer1step5velocity2",
    "layer1step6velocity2",
    "layer1step7velocity2",
    "layer1step8velocity2",
    "layer1step9velocity2",
    "layer1step10velocity2",
    "layer1step11velocity2",
    "layer1step12velocity2",
    "layer1step13velocity2",
    "layer1step14velocity2",
    "layer1step15velocity2",
    "layer1step16velocity2",
    "layer1step17velocity2",
    "layer1step18velocity2",
    "layer1step19velocity2",
    "layer1step20velocity2",
    "layer1step21velocity2",
    "layer1step22velocity2",
    "layer1step23velocity2",
    "layer1step24velocity2",
    "layer1step25velocity2",
    "layer1step26velocity2",
    "layer1step27velocity2",
    "layer1step28velocity2",
    "layer1step29velocity2",
    "layer1step30velocity2",
    "layer1step31velocity2",
    "layer1step32velocity2",
    "layer1step33velocity2",
    "layer1step34velocity2",
    "layer1step35velocity2",
    "layer1step36velocity2",
    "layer1step37velocity2",
    "layer1step38velocity2",
    "layer1step39velocity2",
    "layer1step40velocity2",
    "layer1step41velocity2",
    "layer1step42velocity2",
    "layer1step43velocity2",
    "layer1step44velocity2",
    "layer1step45velocity2",
    "layer1step46velocity2",
    "layer1step47velocity2",
    "layer1step48velocity2",
    "layer1step49velocity2",
    "layer1step50velocity2",
    "layer1step51velocity2",
    "layer1step52velocity2",
    "layer1step53velocity2",
    "layer1step54velocity2",
    "layer1step55velocity2",
    "layer1step56velocity2",
    "layer1step57velocity2",
    "layer1step58velocity2",
    "layer1step59velocity2",
    "layer1step60velocity2",
    "layer1step61velocity2",
    "layer1step62velocity2",
    "layer1step63velocity2",
    "layer1step64velocity2",
    
    "layer1step1note3",                 // 512-575  Poly Seq Track3 Notes (bit0-7;note, bit8;tie)  
    "layer1step2note3",
    "layer1step3note3",
    "layer1step4note3",
    "layer1step5note3",
    "layer1step6note3",
    "layer1step7note3",
    "layer1step8note3",
    "layer1step9note3",
    "layer1step10note3",
    "layer1step11note3",
    "layer1step12note3",
    "layer1step13note3",
    "layer1step14note3",
    "layer1step15note3",
    "layer1step16note3",
    "layer1step17note3",
    "layer1step18note3",
    "layer1step19note3",
    "layer1step20note3",
    "layer1step21note3",
    "layer1step22note3",
    "layer1step23note3",
    "layer1step24note3",
    "layer1step25note3",
    "layer1step26note3",
    "layer1step27note3",
    "layer1step28note3",
    "layer1step29note3",
    "layer1step30note3",
    "layer1step31note3",
    "layer1step32note3",
    "layer1step33note3",
    "layer1step34note3",
    "layer1step35note3",
    "layer1step36note3",
    "layer1step37note3",
    "layer1step38note3",
    "layer1step39note3",
    "layer1step40note3",
    "layer1step41note3",
    "layer1step42note3",
    "layer1step43note3",
    "layer1step44note3",
    "layer1step45note3",
    "layer1step46note3",
    "layer1step47note3",
    "layer1step48note3",
    "layer1step49note3",
    "layer1step50note3",
    "layer1step51note3",
    "layer1step52note3",
    "layer1step53note3",
    "layer1step54note3",
    "layer1step55note3",
    "layer1step56note3",
    "layer1step57note3",
    "layer1step58note3",
    "layer1step59note3",
    "layer1step60note3",
    "layer1step61note3",
    "layer1step62note3",
    "layer1step63note3",
    "layer1step64note3",
    
    "layer1step1velocity3",             // 576-639  Poly Seq Track3 Velocities (bit0-7;velocity, bit8;rest)  
    "layer1step2velocity3",
    "layer1step3velocity3",
    "layer1step4velocity3",
    "layer1step5velocity3",
    "layer1step6velocity3",
    "layer1step7velocity3",
    "layer1step8velocity3",
    "layer1step9velocity3",
    "layer1step10velocity3",
    "layer1step11velocity3",
    "layer1step12velocity3",
    "layer1step13velocity3",
    "layer1step14velocity3",
    "layer1step15velocity3",
    "layer1step16velocity3",
    "layer1step17velocity3",
    "layer1step18velocity3",
    "layer1step19velocity3",
    "layer1step20velocity3",
    "layer1step21velocity3",
    "layer1step22velocity3",
    "layer1step23velocity3",
    "layer1step24velocity3",
    "layer1step25velocity3",
    "layer1step26velocity3",
    "layer1step27velocity3",
    "layer1step28velocity3",
    "layer1step29velocity3",
    "layer1step30velocity3",
    "layer1step31velocity3",
    "layer1step32velocity3",
    "layer1step33velocity3",
    "layer1step34velocity3",
    "layer1step35velocity3",
    "layer1step36velocity3",
    "layer1step37velocity3",
    "layer1step38velocity3",
    "layer1step39velocity3",
    "layer1step40velocity3",
    "layer1step41velocity3",
    "layer1step42velocity3",
    "layer1step43velocity3",
    "layer1step44velocity3",
    "layer1step45velocity3",
    "layer1step46velocity3",
    "layer1step47velocity3",
    "layer1step48velocity3",
    "layer1step49velocity3",
    "layer1step50velocity3",
    "layer1step51velocity3",
    "layer1step52velocity3",
    "layer1step53velocity3",
    "layer1step54velocity3",
    "layer1step55velocity3",
    "layer1step56velocity3",
    "layer1step57velocity3",
    "layer1step58velocity3",
    "layer1step59velocity3",
    "layer1step60velocity3",
    "layer1step61velocity3",
    "layer1step62velocity3",
    "layer1step63velocity3",
    "layer1step64velocity3",

    "layer1step1note4",                 // 640-703  Poly Seq Track4 Notes (bit0-7;note, bit8;tie)
    "layer1step2note4",
    "layer1step3note4",
    "layer1step4note4",
    "layer1step5note4",
    "layer1step6note4",
    "layer1step7note4",
    "layer1step8note4",
    "layer1step9note4",
    "layer1step10note4",
    "layer1step11note4",
    "layer1step12note4",
    "layer1step13note4",
    "layer1step14note4",
    "layer1step15note4",
    "layer1step16note4",
    "layer1step17note4",
    "layer1step18note4",
    "layer1step19note4",
    "layer1step20note4",
    "layer1step21note4",
    "layer1step22note4",
    "layer1step23note4",
    "layer1step24note4",
    "layer1step25note4",
    "layer1step26note4",
    "layer1step27note4",
    "layer1step28note4",
    "layer1step29note4",
    "layer1step30note4",
    "layer1step31note4",
    "layer1step32note4",
    "layer1step33note4",
    "layer1step34note4",
    "layer1step35note4",
    "layer1step36note4",
    "layer1step37note4",
    "layer1step38note4",
    "layer1step39note4",
    "layer1step40note4",
    "layer1step41note4",
    "layer1step42note4",
    "layer1step43note4",
    "layer1step44note4",
    "layer1step45note4",
    "layer1step46note4",
    "layer1step47note4",
    "layer1step48note4",
    "layer1step49note4",
    "layer1step50note4",
    "layer1step51note4",
    "layer1step52note4",
    "layer1step53note4",
    "layer1step54note4",
    "layer1step55note4",
    "layer1step56note4",
    "layer1step57note4",
    "layer1step58note4",
    "layer1step59note4",
    "layer1step60note4",
    "layer1step61note4",
    "layer1step62note4",
    "layer1step63note4",
    "layer1step64note4",
    
    "layer1step1velocity4",             // 704-767  Poly Seq Track4 Velocities (bit0-7;velocity, bit8;rest)  
    "layer1step2velocity4",
    "layer1step3velocity4",
    "layer1step4velocity4",
    "layer1step5velocity4",
    "layer1step6velocity4",
    "layer1step7velocity4",
    "layer1step8velocity4",
    "layer1step9velocity4",
    "layer1step10velocity4",
    "layer1step11velocity4",
    "layer1step12velocity4",
    "layer1step13velocity4",
    "layer1step14velocity4",
    "layer1step15velocity4",
    "layer1step16velocity4",
    "layer1step17velocity4",
    "layer1step18velocity4",
    "layer1step19velocity4",
    "layer1step20velocity4",
    "layer1step21velocity4",
    "layer1step22velocity4",
    "layer1step23velocity4",
    "layer1step24velocity4",
    "layer1step25velocity4",
    "layer1step26velocity4",
    "layer1step27velocity4",
    "layer1step28velocity4",
    "layer1step29velocity4",
    "layer1step30velocity4",
    "layer1step31velocity4",
    "layer1step32velocity4",
    "layer1step33velocity4",
    "layer1step34velocity4",
    "layer1step35velocity4",
    "layer1step36velocity4",
    "layer1step37velocity4",
    "layer1step38velocity4",
    "layer1step39velocity4",
    "layer1step40velocity4",
    "layer1step41velocity4",
    "layer1step42velocity4",
    "layer1step43velocity4",
    "layer1step44velocity4",
    "layer1step45velocity4",
    "layer1step46velocity4",
    "layer1step47velocity4",
    "layer1step48velocity4",
    "layer1step49velocity4",
    "layer1step50velocity4",
    "layer1step51velocity4",
    "layer1step52velocity4",
    "layer1step53velocity4",
    "layer1step54velocity4",
    "layer1step55velocity4",
    "layer1step56velocity4",
    "layer1step57velocity4",
    "layer1step58velocity4",
    "layer1step59velocity4",
    "layer1step60velocity4",
    "layer1step61velocity4",
    "layer1step62velocity4",
    "layer1step63velocity4",
    "layer1step64velocity4",
    
    "layer1step1note5",                 // 768-831  Poly Seq Track5 Notes (bit0-7;note, bit8;tie)
    "layer1step2note5",
    "layer1step3note5",
    "layer1step4note5",
    "layer1step5note5",
    "layer1step6note5",
    "layer1step7note5",
    "layer1step8note5",
    "layer1step9note5",
    "layer1step10note5",
    "layer1step11note5",
    "layer1step12note5",
    "layer1step13note5",
    "layer1step14note5",
    "layer1step15note5",
    "layer1step16note5",
    "layer1step17note5",
    "layer1step18note5",
    "layer1step19note5",
    "layer1step20note5",
    "layer1step21note5",
    "layer1step22note5",
    "layer1step23note5",
    "layer1step24note5",
    "layer1step25note5",
    "layer1step26note5",
    "layer1step27note5",
    "layer1step28note5",
    "layer1step29note5",
    "layer1step30note5",
    "layer1step31note5",
    "layer1step32note5",
    "layer1step33note5",
    "layer1step34note5",
    "layer1step35note5",
    "layer1step36note5",
    "layer1step37note5",
    "layer1step38note5",
    "layer1step39note5",
    "layer1step40note5",
    "layer1step41note5",
    "layer1step42note5",
    "layer1step43note5",
    "layer1step44note5",
    "layer1step45note5",
    "layer1step46note5",
    "layer1step47note5",
    "layer1step48note5",
    "layer1step49note5",
    "layer1step50note5",
    "layer1step51note5",
    "layer1step52note5",
    "layer1step53note5",
    "layer1step54note5",
    "layer1step55note5",
    "layer1step56note5",
    "layer1step57note5",
    "layer1step58note5",
    "layer1step59note5",
    "layer1step60note5",
    "layer1step61note5",
    "layer1step62note5",
    "layer1step63note5",
    "layer1step64note5",
    
    "layer1step1velocity5",             // 832-895  Poly Seq Track5 Velocities (bit0-7;velocity, bit8;rest)
    "layer1step2velocity5",
    "layer1step3velocity5",
    "layer1step4velocity5",
    "layer1step5velocity5",
    "layer1step6velocity5",
    "layer1step7velocity5",
    "layer1step8velocity5",
    "layer1step9velocity5",
    "layer1step10velocity5",
    "layer1step11velocity5",
    "layer1step12velocity5",
    "layer1step13velocity5",
    "layer1step14velocity5",
    "layer1step15velocity5",
    "layer1step16velocity5",
    "layer1step17velocity5",
    "layer1step18velocity5",
    "layer1step19velocity5",
    "layer1step20velocity5",
    "layer1step21velocity5",
    "layer1step22velocity5",
    "layer1step23velocity5",
    "layer1step24velocity5",
    "layer1step25velocity5",
    "layer1step26velocity5",
    "layer1step27velocity5",
    "layer1step28velocity5",
    "layer1step29velocity5",
    "layer1step30velocity5",
    "layer1step31velocity5",
    "layer1step32velocity5",
    "layer1step33velocity5",
    "layer1step34velocity5",
    "layer1step35velocity5",
    "layer1step36velocity5",
    "layer1step37velocity5",
    "layer1step38velocity5",
    "layer1step39velocity5",
    "layer1step40velocity5",
    "layer1step41velocity5",
    "layer1step42velocity5",
    "layer1step43velocity5",
    "layer1step44velocity5",
    "layer1step45velocity5",
    "layer1step46velocity5",
    "layer1step47velocity5",
    "layer1step48velocity5",
    "layer1step49velocity5",
    "layer1step50velocity5",
    "layer1step51velocity5",
    "layer1step52velocity5",
    "layer1step53velocity5",
    "layer1step54velocity5",
    "layer1step55velocity5",
    "layer1step56velocity5",
    "layer1step57velocity5",
    "layer1step58velocity5",
    "layer1step59velocity5",
    "layer1step60velocity5",
    "layer1step61velocity5",
    "layer1step62velocity5",
    "layer1step63velocity5",
    "layer1step64velocity5",

    "layer1step1note6",                 // 896-959  Poly Seq Track6 Notes (bit0-7;note, bit8;tie)  
    "layer1step2note6",
    "layer1step3note6",
    "layer1step4note6",
    "layer1step5note6",
    "layer1step6note6",
    "layer1step7note6",
    "layer1step8note6",
    "layer1step9note6",
    "layer1step10note6",
    "layer1step11note6",
    "layer1step12note6",
    "layer1step13note6",
    "layer1step14note6",
    "layer1step15note6",
    "layer1step16note6",
    "layer1step17note6",
    "layer1step18note6",
    "layer1step19note6",
    "layer1step20note6",
    "layer1step21note6",
    "layer1step22note6",
    "layer1step23note6",
    "layer1step24note6",
    "layer1step25note6",
    "layer1step26note6",
    "layer1step27note6",
    "layer1step28note6",
    "layer1step29note6",
    "layer1step30note6",
    "layer1step31note6",
    "layer1step32note6",
    "layer1step33note6",
    "layer1step34note6",
    "layer1step35note6",
    "layer1step36note6",
    "layer1step37note6",
    "layer1step38note6",
    "layer1step39note6",
    "layer1step40note6",
    "layer1step41note6",
    "layer1step42note6",
    "layer1step43note6",
    "layer1step44note6",
    "layer1step45note6",
    "layer1step46note6",
    "layer1step47note6",
    "layer1step48note6",
    "layer1step49note6",
    "layer1step50note6",
    "layer1step51note6",
    "layer1step52note6",
    "layer1step53note6",
    "layer1step54note6",
    "layer1step55note6",
    "layer1step56note6",
    "layer1step57note6",
    "layer1step58note6",
    "layer1step59note6",
    "layer1step60note6",
    "layer1step61note6",
    "layer1step62note6",
    "layer1step63note6",
    "layer1step64note6",
    
    "layer1step1velocity6",             // 960-1023 Poly Seq Track6 Velocities (bit0-7;velocity, bit8;rest)  
    "layer1step2velocity6",
    "layer1step3velocity6",
    "layer1step4velocity6",
    "layer1step5velocity6",
    "layer1step6velocity6",
    "layer1step7velocity6",
    "layer1step8velocity6",
    "layer1step9velocity6",
    "layer1step10velocity6",
    "layer1step11velocity6",
    "layer1step12velocity6",
    "layer1step13velocity6",
    "layer1step14velocity6",
    "layer1step15velocity6",
    "layer1step16velocity6",
    "layer1step17velocity6",
    "layer1step18velocity6",
    "layer1step19velocity6",
    "layer1step20velocity6",
    "layer1step21velocity6",
    "layer1step22velocity6",
    "layer1step23velocity6",
    "layer1step24velocity6",
    "layer1step25velocity6",
    "layer1step26velocity6",
    "layer1step27velocity6",
    "layer1step28velocity6",
    "layer1step29velocity6",
    "layer1step30velocity6",
    "layer1step31velocity6",
    "layer1step32velocity6",
    "layer1step33velocity6",
    "layer1step34velocity6",
    "layer1step35velocity6",
    "layer1step36velocity6",
    "layer1step37velocity6",
    "layer1step38velocity6",
    "layer1step39velocity6",
    "layer1step40velocity6",
    "layer1step41velocity6",
    "layer1step42velocity6",
    "layer1step43velocity6",
    "layer1step44velocity6",
    "layer1step45velocity6",
    "layer1step46velocity6",
    "layer1step47velocity6",
    "layer1step48velocity6",
    "layer1step49velocity6",
    "layer1step50velocity6",
    "layer1step51velocity6",
    "layer1step52velocity6",
    "layer1step53velocity6",
    "layer1step54velocity6",
    "layer1step55velocity6",
    "layer1step56velocity6",
    "layer1step57velocity6",
    "layer1step58velocity6",
    "layer1step59velocity6",
    "layer1step60velocity6",
    "layer1step61velocity6",
    "layer1step62velocity6",
    "layer1step63velocity6",
    "layer1step64velocity6",
  
    /**
       LAYER B:
       Same as above, just add 1024 to the index value.
       Last two bytes (2046-2047) do not exist in SysEx dump.
    */
    
    "layer2dco1frequency",              // 0      OSC1 Freq (0-120)
    "layer2dco2frequency",              // 1      OSC1 Freq (0-120)
    "layer2dco1finetune",               // 2      OSC1 Finetune (0-100)
    "layer2dco2finetune",               // 3      OSC2 Finetune (0-100)
    "layer2dco1shape",                  // 4      OSC1 Shape (0-4, off/Saw/SawTri/Tri/Pul)
    "layer2dco2shape",                  // 5      OSC2 Shape (0-4, off/Saw/SawTri/Tri/Pul)
    "layer2dco1shapemod",               // 6      OSC1 ShapeMod (0-99)
    "layer2dco2shapemod",               // 7      OSC2 ShapeMod (0-99)
    
    "layer2dco1glide",                  // 8      OSC1 Glide (0-127)
    "layer2dco2glide",                  // 9      OSC2 Glide (0-127)
    "layer2dco1key",                    // 10      OSC1 Key on/off (0-1, off/on)
    "layer2dco2key",                    // 11      OSC2 Key on/off (0-1, off/on)
    "layer2dco1notereset",              // 12      OSC1 Note Reset (0-1, off/on)
    "layer2dco2notereset",              // 13      OSC2 Note Reset (0-1, off/on)
    "layer2mix",                        // 14      OSC Mix (0-127)
    "layer2subosc",                     // 15      SubOct Level (0-127)
    
    "layer2noise",                      // 16      Noise Level (0-127)
    "layer2sync",                       // 17      Sync (0-1, off/on)
    "layer2glidemode",                  // 18      Glide Mode (0-3, Fixed Rate/Fixed Rate A/Fixed Time/Fixed Time A)
    "layer2glideonoff",                 // 19      Glide on/off (0-1, off/on)
    "layer2pitchbendrange",             // 20      Pitch Bend Range (0-12)
    "layer2slop",                       // 21      OSC Slop (0-127)
    "layer2vcffrequency",               // 22      Cutoff Freq (0-164)
    "layer2vcfresonance",               // 23      Resonance (0-127)
        
    "layer2vcfkeyboardamount",          // 24      Filter Key Amount (0-127)
    "layer2vcfaudiomodulation",         // 25      Filter Audio Mod (0-127)
    "layer2vcfpoles",                   // 26      Filter Poles (0-1, 2pole/4pole)
    "layer2vcainitiallevel",            // 27      VCA Level (0-127)
    "layer2vcavoicevolume",             // 28      Program Volume (0-127)
    "layer2vcaoutputspread",            // 29      Pan Spread (0-127)
    "layer2env3moddestination",         // 30      Env3 Destination (0-52, *see table below)
    "layer2env3repeat",                 // 31      Env3 Repeat (0-1, off/on)
        
    "layer2env1amount",                 // 32      EnvF Amount (0-254)
    "layer2env2amount",                 // 33      EnvA Amount (0-127)
    "layer2env3amount",                 // 34      Env3 Amount (0-254)
    "layer2env1velocityamount",         // 35      EnvF Velocity (0-127)
    "layer2env2velocityamount",         // 36      EnvA Velocity (0-127)
    "layer2env3velocityamount",         // 37      Env3 Velocity (0-127)
    "layer2env1delay",                  // 38      EnvF Delay (0-127)
    "layer2env2delay",                  // 39      EnvA Delay (0-127)
    
    "layer2env3delay",                  // 40      Env3 Delay (0-127)
    "layer2env1attack",                 // 41      EnvF Attack (0-127)
    "layer2env2attack",                 // 42      EnvA Attack (0-127)
    "layer2env3attack",                 // 43      Env3 Attack (0-127)
    "layer2env1decay",                  // 44      EnvF Decay (0-127)
    "layer2env2decay",                  // 45      EnvA Decay (0-127)
    "layer2env3decay",                  // 46      Env3 Decay (0-127)
    "layer2env1sustain",                // 47      EnvF Sustain (0-127)
    
    "layer2env2sustain",                // 48      EnvA Sustain (0-127)
    "layer2env3sustain",                // 49      Env3 Sustain (0-127)
    "layer2env1release",                // 50      EnvF Release (0-127)
    "layer2env2release",                // 51      EnvA Release (0-127)
    "layer2env3release",                // 52      Env3 Release (0-127)
    "layer2lfo1frequency",              // 53      LFO1 Rate (0-150)
    "layer2lfo2frequency",              // 54      LFO2 Rate (0-150)
    "layer2lfo3frequency",              // 55      LFO3 Rate (0-150)
   
    "layer2lfo4frequency",              // 56      LFO4 Rate (0-150)
    "layer2lfo1shape",                  // 57      LFO1 Shape (0-5, off/Tri/Saw/RevSaw/Square/Random)
    "layer2lfo2shape",                  // 58      LFO2 Shape (0-5, off/Tri/Saw/RevSaw/Square/Random)
    "layer2lfo3shape",                  // 59      LFO3 Shape (0-5, off/Tri/Saw/RevSaw/Square/Random)
    "layer2lfo4shape",                  // 60      LFO4 Shape (0-5, off/Tri/Saw/RevSaw/Square/Random)
    "layer2lfo1amount",                 // 61      LFO1 Amount (0-127)
    "layer2lfo2amount",                 // 62      LFO2 Amount (0-127)
    "layer2lfo3amount",                 // 63      LFO3 Amount (0-127)
    
    "layer2lfo4amount",                 // 64      LFO4 Amount (0-127)
    "layer2lfo1moddestination",         // 65      LFO1 Destination (0-52, *see table below)
    "layer2lfo2moddestination",         // 66      LFO2 Destination (0-52, *see table below)
    "layer2lfo3moddestination",         // 67      LFO3 Destination (0-52, *see table below)
    "layer2lfo4moddestination",         // 68      LFO4 Destination (0-52, *see table below)
    "layer2lfo1clocksync",              // 69      LFO1 ClkSync (0-1, 0ff/on)
    "layer2lfo2clocksync",              // 70      LFO2 ClkSync (0-1, 0ff/on)
    "layer2lfo3clocksync",              // 71      LFO3 ClkSync (0-1, 0ff/on)
    
    "layer2lfo4clocksync",              // 72      LFO4 ClkSync (0-1, 0ff/on)
    "layer2lfo1keysync",                // 73      LFO1 KeySync (0-1, off/on)
    "layer2lfo2keysync",                // 74      LFO2 KeySync (0-1, off/on)
    "layer2lfo3keysync",                // 75      LFO3 KeySync (0-1, off/on)
    "layer2lfo4keysync",                // 76      LFO4 KeySync (0-1, off/on)
    "layer2mod1source",                 // 77      Mod1 Source (0-22, *see table below)
    "layer2mod2source",                 // 78      Mod2 Source (0-22, *see table below)
    "layer2mod3source",                 // 79      Mod3 Source (0-22, *see table below)
    
    "layer2mod4source",                 // 80      Mod4 Source (0-22, *see table below)
    "layer2mod5source",                 // 81      Mod5 Source (0-22, *see table below)
    "layer2mod6source",                 // 82      Mod6 Source (0-22, *see table below)
    "layer2mod7source",                 // 83      Mod7 Source (0-22, *see table below)
    "layer2mod8source",                 // 84      Mod8 Source (0-22, *see table below)
    "layer2mod1amount",                 // 85      Mod1 Amount (0-254)
    "layer2mod2amount",                 // 86      Mod2 Amount (0-254)
    "layer2mod3amount",                 // 87      Mod3 Amount (0-254)
    
    "layer2mod4amount",                 // 88      Mod4 Amount (0-254)
    "layer2mod5amount",                 // 89      Mod5 Amount (0-254)
    "layer2mod6amount",                 // 90      Mod6 Amount (0-254)
    "layer2mod7amount",                 // 91      Mod7 Amount (0-254)
    "layer2mod8amount",                 // 92      Mod8 Amount (0-254)
    "layer2mod1destination",            // 93      Mod1 Destination (0-52, *see table below)
    "layer2mod2destination",            // 94      Mod2 Destination (0-52, *see table below)    
    "layer2mod3destination",            // 95      Mod3 Destination (0-52, *see table below)
    
    "layer2mod4destination",            // 96      Mod4 Destination (0-52, *see table below)
    "layer2mod5destination",            // 97      Mod5 Destination (0-52, *see table below)
    "layer2mod6destination",            // 98      Mod6 Destination (0-52, *see table below)
    "layer2mod7destination",            // 99      Mod7 Destination (0-52, *see table below)
    "layer2mod8destination",            // 100      Mod8 Destination (0-52, *see table below)
    "layer2wheelamount",                // 101      Mod Wheel Amount (0-254)
    "layer2wheeldestination",           // 102      Mod Wheel Destination (0-52, *see table below)
    "layer2pressureamount",             // 103      Pressure Mod Amount (0-254)
    
    "layer2pressuredestination",        // 104      Pressure Mod Destination (0-52, *see table below)
    "layer2breathamount",               // 105      Breath Mod Amount (0-254)
    "layer2breathdestination",          // 106      Breath Mod Destination (0-52, *see table below) 
    "layer2velocityamount",             // 107      Velocity Mod Amount (0-254)
    "layer2velocitydestination",        // 108      Velocity Mod Destination (0-52, *see table below) 
    "layer2footamount",                 // 109      MIDI Foot Mod Amount (0-254)
    "layer2footdestination",            // 110      MIDI Foot Mod Destination (0-52, *see table below)
    "layer2track1destination",          // 111      Gated Seq1 Destination (0-52, *see table below)
    
    "layer2track2destination",          // 112      Gated Seq2 Destination (0-53, *see table below)
    "layer2track3destination",          // 113      Gated Seq3 Destination (0-52, *see table below)
    "layer2track4destination",          // 114      Gated Seq4 Destination (0-53, *see table below)
    "layer2fxselect",                   // 115      FX Select (0-13, Off/DM/DDS/BBD/Ch/PH/PL/PM/F1/F2/Rvb/RM/Dst/HPF)
    "layer2fxonoff",                    // 116      FX on/off (0-1, off/on)
    "layer2fxmix",                      // 117          FX mix
    "layer2fxparam1",                   // 118      FX Parameter 1 (0-255)
    "layer2fxparam2",                   // 119      FX Parameter 2 (0-255)
    
    "layer2fxclocksync",                // 120      FX Clock Sync on/off (0-1, off/on)
    "---",                              // 121      -
    "layer2unisonkeymode",              // 122      Key Mode (0-5, Low/Hi/Last/LowR/HiR/LastR)
    "layer2unison",                     // 123      Unison on/off (0-1, off/on)
    "layer2unisonmode",                 // 124      Unison Mode (0-15/Chord)
    "---",                              // 125      -
    "---",                              // 126      -
    "---",                              // 127      -
    
    "---",                              // 128      -
    "---",                              // 129      -
    "layer2tempo",                      // 130      BPM (30-250)
    "layer2clockdivide",                // 131      Divide (0-12, H/Q/8th/8H/8S/8T/16th/16H/16S/16T/32nd/32T/64T
    "layer2arpeggiatormode",            // 132      Arp Mode (0-4, Up/Down/Up+Down/Random/assign)
    "layer2arpeggiatoroctaves",         // 133      Arp Range (0-2, 1Oct/2Oct/3Oct)
    "layer2arpeggiatorrepeats",         // 134      Arp Repeats (0-3)
    "layer2arpeggiatorrelatch",         // 135      Arp Relatch on/off (0-1, off/on)
    
    "layer2arpeggiator",                // 136      Arp on/off (0-1, off/on)
    "---",                                              // 137      -
    "layer2sequencertrigger",           // 138      Sequencer Mode (0-4, Normal/NoReset/NoGate/NoGateReset/KeyStep)
    "layer2gatedpolyseq",               // 139      Sequencer Type (0-1, Gated/Poly)
    
    "layer2track1note1",                // 140      Gated Seq1 Step 1-16 (0-125/reset/rest)
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
    "layer2track1note16",               // 155
   
    "layer2track2note1",                // 156      Gated Seq2 Step 1-16 (0-125/reset/rest) 
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
    "layer2track2note16",               // 171
    
    "layer2track3note1",                // 172      Gated Seq3 Step 1-16 (0-125/reset/rest)  
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
    "layer2track3note16",               // 187
    
    "layer2track4note1",                // 188      Gated Seq4 Step 1-16 (0-125/reset/rest)    
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
    "layer2track4note16",               // 203
    
    
    "---",                              // 204      -
    "---",                              // 205      -
    "---",                              // 206      -
    "---",                              // 207      -
    
    "layer2unisondetune",               // 208      Unison Detune (0-16)
    "layer2panmode",                    // 209      Pan Mod Mode (0-1, Alternate/Fixed)
    "---",                              // 210      -
    "---",                              // 211      -
    "---",                              // 212      -
    "---",                              // 213      -
    "---",                              // 214      -
    "---",                              // 215      -
    
    "---",                              // 216      -
    "---",                              // 217      -
    "---",                              // 218      -
    "---",                              // 219      -
    "---",                              // 220      -
    "---",                              // 221      -
    "---",                              // 222      -
    "---",                              // 223      -
    
    "---",                              // 224      -
    "---",                              // 225      -
    "---",                              // 226      -
    "---",                              // 227      -
    "---",                              // 228      -
    "---",                              // 229      -
    "---",                              // 230      -
    "---",                              // 231      Layer Mode (0-2, LayerA/SplitAB/StackAB)
    
    "---",                              // 232      Split Point (0-120)
    "---",                              // 233      -
    "---",                              // 234      -
    "---",                              // 235-     Layer B Name
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
    "---",                              // 254
    "---",                              // 255      -
       
    "layer2step1note1",                 // 256-319  Poly Seq Track1 Notes (bit0-7;note, bit8;tie)
    "layer2step2note1",
    "layer2step3note1",
    "layer2step4note1",
    "layer2step5note1",
    "layer2step6note1",
    "layer2step7note1",
    "layer2step8note1",
    "layer2step9note1",
    "layer2step10note1",
    "layer2step11note1",
    "layer2step12note1",
    "layer2step13note1",
    "layer2step14note1",
    "layer2step15note1",
    "layer2step16note1",
    "layer2step17note1",
    "layer2step18note1",
    "layer2step19note1",
    "layer2step20note1",
    "layer2step21note1",
    "layer2step22note1",
    "layer2step23note1",
    "layer2step24note1",
    "layer2step25note1",
    "layer2step26note1",
    "layer2step27note1",
    "layer2step28note1",
    "layer2step29note1",
    "layer2step30note1",
    "layer2step31note1",
    "layer2step32note1",
    "layer2step33note1",
    "layer2step34note1",
    "layer2step35note1",
    "layer2step36note1",
    "layer2step37note1",
    "layer2step38note1",
    "layer2step39note1",
    "layer2step40note1",
    "layer2step41note1",
    "layer2step42note1",
    "layer2step43note1",
    "layer2step44note1",
    "layer2step45note1",
    "layer2step46note1",
    "layer2step47note1",
    "layer2step48note1",
    "layer2step49note1",
    "layer2step50note1",
    "layer2step51note1",
    "layer2step52note1",
    "layer2step53note1",
    "layer2step54note1",
    "layer2step55note1",
    "layer2step56note1",
    "layer2step57note1",
    "layer2step58note1",
    "layer2step59note1",
    "layer2step60note1",
    "layer2step61note1",
    "layer2step62note1",
    "layer2step63note1",
    "layer2step64note1",
    
    "layer2step1velocity1",             // 320-383  Poly Seq Track1 Velocities (bit0-7;velocity, bit8;rest)  
    "layer2step2velocity1",
    "layer2step3velocity1",
    "layer2step4velocity1",
    "layer2step5velocity1",
    "layer2step6velocity1",
    "layer2step7velocity1",
    "layer2step8velocity1",
    "layer2step9velocity1",
    "layer2step10velocity1",
    "layer2step11velocity1",
    "layer2step12velocity1",
    "layer2step13velocity1",
    "layer2step14velocity1",
    "layer2step15velocity1",
    "layer2step16velocity1",
    "layer2step17velocity1",
    "layer2step18velocity1",
    "layer2step19velocity1",
    "layer2step20velocity1",
    "layer2step21velocity1",
    "layer2step22velocity1",
    "layer2step23velocity1",
    "layer2step24velocity1",
    "layer2step25velocity1",
    "layer2step26velocity1",
    "layer2step27velocity1",
    "layer2step28velocity1",
    "layer2step29velocity1",
    "layer2step30velocity1",
    "layer2step31velocity1",
    "layer2step32velocity1",
    "layer2step33velocity1",
    "layer2step34velocity1",
    "layer2step35velocity1",
    "layer2step36velocity1",
    "layer2step37velocity1",
    "layer2step38velocity1",
    "layer2step39velocity1",
    "layer2step40velocity1",
    "layer2step41velocity1",
    "layer2step42velocity1",
    "layer2step43velocity1",
    "layer2step44velocity1",
    "layer2step45velocity1",
    "layer2step46velocity1",
    "layer2step47velocity1",
    "layer2step48velocity1",
    "layer2step49velocity1",
    "layer2step50velocity1",
    "layer2step51velocity1",
    "layer2step52velocity1",
    "layer2step53velocity1",
    "layer2step54velocity1",
    "layer2step55velocity1",
    "layer2step56velocity1",
    "layer2step57velocity1",
    "layer2step58velocity1",
    "layer2step59velocity1",
    "layer2step60velocity1",
    "layer2step61velocity1",
    "layer2step62velocity1",
    "layer2step63velocity1",
    "layer2step64velocity1",

    "layer2step1note2",                 // 384-447  Poly Seq Track2 Notes (bit0-7;note, bit8;tie)
    "layer2step2note2",
    "layer2step3note2",
    "layer2step4note2",
    "layer2step5note2",
    "layer2step6note2",
    "layer2step7note2",
    "layer2step8note2",
    "layer2step9note2",
    "layer2step10note2",
    "layer2step11note2",
    "layer2step12note2",
    "layer2step13note2",
    "layer2step14note2",
    "layer2step15note2",
    "layer2step16note2",
    "layer2step17note2",
    "layer2step18note2",
    "layer2step19note2",
    "layer2step20note2",
    "layer2step21note2",
    "layer2step22note2",
    "layer2step23note2",
    "layer2step24note2",
    "layer2step25note2",
    "layer2step26note2",
    "layer2step27note2",
    "layer2step28note2",
    "layer2step29note2",
    "layer2step30note2",
    "layer2step31note2",
    "layer2step32note2",
    "layer2step33note2",
    "layer2step34note2",
    "layer2step35note2",
    "layer2step36note2",
    "layer2step37note2",
    "layer2step38note2",
    "layer2step39note2",
    "layer2step40note2",
    "layer2step41note2",
    "layer2step42note2",
    "layer2step43note2",
    "layer2step44note2",
    "layer2step45note2",
    "layer2step46note2",
    "layer2step47note2",
    "layer2step48note2",
    "layer2step49note2",
    "layer2step50note2",
    "layer2step51note2",
    "layer2step52note2",
    "layer2step53note2",
    "layer2step54note2",
    "layer2step55note2",
    "layer2step56note2",
    "layer2step57note2",
    "layer2step58note2",
    "layer2step59note2",
    "layer2step60note2",
    "layer2step61note2",
    "layer2step62note2",
    "layer2step63note2",
    "layer2step64note2",
    
    "layer2step1velocity2",             // 448-511  Poly Seq Track2 Velocities (bit0-7;velocity, bit8;rest)
    "layer2step2velocity2",
    "layer2step3velocity2",
    "layer2step4velocity2",
    "layer2step5velocity2",
    "layer2step6velocity2",
    "layer2step7velocity2",
    "layer2step8velocity2",
    "layer2step9velocity2",
    "layer2step10velocity2",
    "layer2step11velocity2",
    "layer2step12velocity2",
    "layer2step13velocity2",
    "layer2step14velocity2",
    "layer2step15velocity2",
    "layer2step16velocity2",
    "layer2step17velocity2",
    "layer2step18velocity2",
    "layer2step19velocity2",
    "layer2step20velocity2",
    "layer2step21velocity2",
    "layer2step22velocity2",
    "layer2step23velocity2",
    "layer2step24velocity2",
    "layer2step25velocity2",
    "layer2step26velocity2",
    "layer2step27velocity2",
    "layer2step28velocity2",
    "layer2step29velocity2",
    "layer2step30velocity2",
    "layer2step31velocity2",
    "layer2step32velocity2",
    "layer2step33velocity2",
    "layer2step34velocity2",
    "layer2step35velocity2",
    "layer2step36velocity2",
    "layer2step37velocity2",
    "layer2step38velocity2",
    "layer2step39velocity2",
    "layer2step40velocity2",
    "layer2step41velocity2",
    "layer2step42velocity2",
    "layer2step43velocity2",
    "layer2step44velocity2",
    "layer2step45velocity2",
    "layer2step46velocity2",
    "layer2step47velocity2",
    "layer2step48velocity2",
    "layer2step49velocity2",
    "layer2step50velocity2",
    "layer2step51velocity2",
    "layer2step52velocity2",
    "layer2step53velocity2",
    "layer2step54velocity2",
    "layer2step55velocity2",
    "layer2step56velocity2",
    "layer2step57velocity2",
    "layer2step58velocity2",
    "layer2step59velocity2",
    "layer2step60velocity2",
    "layer2step61velocity2",
    "layer2step62velocity2",
    "layer2step63velocity2",
    "layer2step64velocity2",
    
    "layer2step1note3",                 // 512-575  Poly Seq Track3 Notes (bit0-7;note, bit8;tie)  
    "layer2step2note3",
    "layer2step3note3",
    "layer2step4note3",
    "layer2step5note3",
    "layer2step6note3",
    "layer2step7note3",
    "layer2step8note3",
    "layer2step9note3",
    "layer2step10note3",
    "layer2step11note3",
    "layer2step12note3",
    "layer2step13note3",
    "layer2step14note3",
    "layer2step15note3",
    "layer2step16note3",
    "layer2step17note3",
    "layer2step18note3",
    "layer2step19note3",
    "layer2step20note3",
    "layer2step21note3",
    "layer2step22note3",
    "layer2step23note3",
    "layer2step24note3",
    "layer2step25note3",
    "layer2step26note3",
    "layer2step27note3",
    "layer2step28note3",
    "layer2step29note3",
    "layer2step30note3",
    "layer2step31note3",
    "layer2step32note3",
    "layer2step33note3",
    "layer2step34note3",
    "layer2step35note3",
    "layer2step36note3",
    "layer2step37note3",
    "layer2step38note3",
    "layer2step39note3",
    "layer2step40note3",
    "layer2step41note3",
    "layer2step42note3",
    "layer2step43note3",
    "layer2step44note3",
    "layer2step45note3",
    "layer2step46note3",
    "layer2step47note3",
    "layer2step48note3",
    "layer2step49note3",
    "layer2step50note3",
    "layer2step51note3",
    "layer2step52note3",
    "layer2step53note3",
    "layer2step54note3",
    "layer2step55note3",
    "layer2step56note3",
    "layer2step57note3",
    "layer2step58note3",
    "layer2step59note3",
    "layer2step60note3",
    "layer2step61note3",
    "layer2step62note3",
    "layer2step63note3",
    "layer2step64note3",
    
    "layer2step1velocity3",             // 576-639  Poly Seq Track3 Velocities (bit0-7;velocity, bit8;rest)  
    "layer2step2velocity3",
    "layer2step3velocity3",
    "layer2step4velocity3",
    "layer2step5velocity3",
    "layer2step6velocity3",
    "layer2step7velocity3",
    "layer2step8velocity3",
    "layer2step9velocity3",
    "layer2step10velocity3",
    "layer2step11velocity3",
    "layer2step12velocity3",
    "layer2step13velocity3",
    "layer2step14velocity3",
    "layer2step15velocity3",
    "layer2step16velocity3",
    "layer2step17velocity3",
    "layer2step18velocity3",
    "layer2step19velocity3",
    "layer2step20velocity3",
    "layer2step21velocity3",
    "layer2step22velocity3",
    "layer2step23velocity3",
    "layer2step24velocity3",
    "layer2step25velocity3",
    "layer2step26velocity3",
    "layer2step27velocity3",
    "layer2step28velocity3",
    "layer2step29velocity3",
    "layer2step30velocity3",
    "layer2step31velocity3",
    "layer2step32velocity3",
    "layer2step33velocity3",
    "layer2step34velocity3",
    "layer2step35velocity3",
    "layer2step36velocity3",
    "layer2step37velocity3",
    "layer2step38velocity3",
    "layer2step39velocity3",
    "layer2step40velocity3",
    "layer2step41velocity3",
    "layer2step42velocity3",
    "layer2step43velocity3",
    "layer2step44velocity3",
    "layer2step45velocity3",
    "layer2step46velocity3",
    "layer2step47velocity3",
    "layer2step48velocity3",
    "layer2step49velocity3",
    "layer2step50velocity3",
    "layer2step51velocity3",
    "layer2step52velocity3",
    "layer2step53velocity3",
    "layer2step54velocity3",
    "layer2step55velocity3",
    "layer2step56velocity3",
    "layer2step57velocity3",
    "layer2step58velocity3",
    "layer2step59velocity3",
    "layer2step60velocity3",
    "layer2step61velocity3",
    "layer2step62velocity3",
    "layer2step63velocity3",
    "layer2step64velocity3",

    "layer2step1note4",                 // 640-703  Poly Seq Track4 Notes (bit0-7;note, bit8;tie)
    "layer2step2note4",
    "layer2step3note4",
    "layer2step4note4",
    "layer2step5note4",
    "layer2step6note4",
    "layer2step7note4",
    "layer2step8note4",
    "layer2step9note4",
    "layer2step10note4",
    "layer2step11note4",
    "layer2step12note4",
    "layer2step13note4",
    "layer2step14note4",
    "layer2step15note4",
    "layer2step16note4",
    "layer2step17note4",
    "layer2step18note4",
    "layer2step19note4",
    "layer2step20note4",
    "layer2step21note4",
    "layer2step22note4",
    "layer2step23note4",
    "layer2step24note4",
    "layer2step25note4",
    "layer2step26note4",
    "layer2step27note4",
    "layer2step28note4",
    "layer2step29note4",
    "layer2step30note4",
    "layer2step31note4",
    "layer2step32note4",
    "layer2step33note4",
    "layer2step34note4",
    "layer2step35note4",
    "layer2step36note4",
    "layer2step37note4",
    "layer2step38note4",
    "layer2step39note4",
    "layer2step40note4",
    "layer2step41note4",
    "layer2step42note4",
    "layer2step43note4",
    "layer2step44note4",
    "layer2step45note4",
    "layer2step46note4",
    "layer2step47note4",
    "layer2step48note4",
    "layer2step49note4",
    "layer2step50note4",
    "layer2step51note4",
    "layer2step52note4",
    "layer2step53note4",
    "layer2step54note4",
    "layer2step55note4",
    "layer2step56note4",
    "layer2step57note4",
    "layer2step58note4",
    "layer2step59note4",
    "layer2step60note4",
    "layer2step61note4",
    "layer2step62note4",
    "layer2step63note4",
    "layer2step64note4",
    
    "layer2step1velocity4",             // 704-767  Poly Seq Track4 Velocities (bit0-7;velocity, bit8;rest)  
    "layer2step2velocity4",
    "layer2step3velocity4",
    "layer2step4velocity4",
    "layer2step5velocity4",
    "layer2step6velocity4",
    "layer2step7velocity4",
    "layer2step8velocity4",
    "layer2step9velocity4",
    "layer2step10velocity4",
    "layer2step11velocity4",
    "layer2step12velocity4",
    "layer2step13velocity4",
    "layer2step14velocity4",
    "layer2step15velocity4",
    "layer2step16velocity4",
    "layer2step17velocity4",
    "layer2step18velocity4",
    "layer2step19velocity4",
    "layer2step20velocity4",
    "layer2step21velocity4",
    "layer2step22velocity4",
    "layer2step23velocity4",
    "layer2step24velocity4",
    "layer2step25velocity4",
    "layer2step26velocity4",
    "layer2step27velocity4",
    "layer2step28velocity4",
    "layer2step29velocity4",
    "layer2step30velocity4",
    "layer2step31velocity4",
    "layer2step32velocity4",
    "layer2step33velocity4",
    "layer2step34velocity4",
    "layer2step35velocity4",
    "layer2step36velocity4",
    "layer2step37velocity4",
    "layer2step38velocity4",
    "layer2step39velocity4",
    "layer2step40velocity4",
    "layer2step41velocity4",
    "layer2step42velocity4",
    "layer2step43velocity4",
    "layer2step44velocity4",
    "layer2step45velocity4",
    "layer2step46velocity4",
    "layer2step47velocity4",
    "layer2step48velocity4",
    "layer2step49velocity4",
    "layer2step50velocity4",
    "layer2step51velocity4",
    "layer2step52velocity4",
    "layer2step53velocity4",
    "layer2step54velocity4",
    "layer2step55velocity4",
    "layer2step56velocity4",
    "layer2step57velocity4",
    "layer2step58velocity4",
    "layer2step59velocity4",
    "layer2step60velocity4",
    "layer2step61velocity4",
    "layer2step62velocity4",
    "layer2step63velocity4",
    "layer2step64velocity4",
    
    "layer2step1note5",                 // 768-831  Poly Seq Track5 Notes (bit0-7;note, bit8;tie)
    "layer2step2note5",
    "layer2step3note5",
    "layer2step4note5",
    "layer2step5note5",
    "layer2step6note5",
    "layer2step7note5",
    "layer2step8note5",
    "layer2step9note5",
    "layer2step10note5",
    "layer2step11note5",
    "layer2step12note5",
    "layer2step13note5",
    "layer2step14note5",
    "layer2step15note5",
    "layer2step16note5",
    "layer2step17note5",
    "layer2step18note5",
    "layer2step19note5",
    "layer2step20note5",
    "layer2step21note5",
    "layer2step22note5",
    "layer2step23note5",
    "layer2step24note5",
    "layer2step25note5",
    "layer2step26note5",
    "layer2step27note5",
    "layer2step28note5",
    "layer2step29note5",
    "layer2step30note5",
    "layer2step31note5",
    "layer2step32note5",
    "layer2step33note5",
    "layer2step34note5",
    "layer2step35note5",
    "layer2step36note5",
    "layer2step37note5",
    "layer2step38note5",
    "layer2step39note5",
    "layer2step40note5",
    "layer2step41note5",
    "layer2step42note5",
    "layer2step43note5",
    "layer2step44note5",
    "layer2step45note5",
    "layer2step46note5",
    "layer2step47note5",
    "layer2step48note5",
    "layer2step49note5",
    "layer2step50note5",
    "layer2step51note5",
    "layer2step52note5",
    "layer2step53note5",
    "layer2step54note5",
    "layer2step55note5",
    "layer2step56note5",
    "layer2step57note5",
    "layer2step58note5",
    "layer2step59note5",
    "layer2step60note5",
    "layer2step61note5",
    "layer2step62note5",
    "layer2step63note5",
    "layer2step64note5",
    
    "layer2step1velocity5",             // 832-895  Poly Seq Track5 Velocities (bit0-7;velocity, bit8;rest)
    "layer2step2velocity5",
    "layer2step3velocity5",
    "layer2step4velocity5",
    "layer2step5velocity5",
    "layer2step6velocity5",
    "layer2step7velocity5",
    "layer2step8velocity5",
    "layer2step9velocity5",
    "layer2step10velocity5",
    "layer2step11velocity5",
    "layer2step12velocity5",
    "layer2step13velocity5",
    "layer2step14velocity5",
    "layer2step15velocity5",
    "layer2step16velocity5",
    "layer2step17velocity5",
    "layer2step18velocity5",
    "layer2step19velocity5",
    "layer2step20velocity5",
    "layer2step21velocity5",
    "layer2step22velocity5",
    "layer2step23velocity5",
    "layer2step24velocity5",
    "layer2step25velocity5",
    "layer2step26velocity5",
    "layer2step27velocity5",
    "layer2step28velocity5",
    "layer2step29velocity5",
    "layer2step30velocity5",
    "layer2step31velocity5",
    "layer2step32velocity5",
    "layer2step33velocity5",
    "layer2step34velocity5",
    "layer2step35velocity5",
    "layer2step36velocity5",
    "layer2step37velocity5",
    "layer2step38velocity5",
    "layer2step39velocity5",
    "layer2step40velocity5",
    "layer2step41velocity5",
    "layer2step42velocity5",
    "layer2step43velocity5",
    "layer2step44velocity5",
    "layer2step45velocity5",
    "layer2step46velocity5",
    "layer2step47velocity5",
    "layer2step48velocity5",
    "layer2step49velocity5",
    "layer2step50velocity5",
    "layer2step51velocity5",
    "layer2step52velocity5",
    "layer2step53velocity5",
    "layer2step54velocity5",
    "layer2step55velocity5",
    "layer2step56velocity5",
    "layer2step57velocity5",
    "layer2step58velocity5",
    "layer2step59velocity5",
    "layer2step60velocity5",
    "layer2step61velocity5",
    "layer2step62velocity5",
    "layer2step63velocity5",
    "layer2step64velocity5",

    "layer2step1note6",                 // 896-959  Poly Seq Track6 Notes (bit0-7;note, bit8;tie)  
    "layer2step2note6",
    "layer2step3note6",
    "layer2step4note6",
    "layer2step5note6",
    "layer2step6note6",
    "layer2step7note6",
    "layer2step8note6",
    "layer2step9note6",
    "layer2step10note6",
    "layer2step11note6",
    "layer2step12note6",
    "layer2step13note6",
    "layer2step14note6",
    "layer2step15note6",
    "layer2step16note6",
    "layer2step17note6",
    "layer2step18note6",
    "layer2step19note6",
    "layer2step20note6",
    "layer2step21note6",
    "layer2step22note6",
    "layer2step23note6",
    "layer2step24note6",
    "layer2step25note6",
    "layer2step26note6",
    "layer2step27note6",
    "layer2step28note6",
    "layer2step29note6",
    "layer2step30note6",
    "layer2step31note6",
    "layer2step32note6",
    "layer2step33note6",
    "layer2step34note6",
    "layer2step35note6",
    "layer2step36note6",
    "layer2step37note6",
    "layer2step38note6",
    "layer2step39note6",
    "layer2step40note6",
    "layer2step41note6",
    "layer2step42note6",
    "layer2step43note6",
    "layer2step44note6",
    "layer2step45note6",
    "layer2step46note6",
    "layer2step47note6",
    "layer2step48note6",
    "layer2step49note6",
    "layer2step50note6",
    "layer2step51note6",
    "layer2step52note6",
    "layer2step53note6",
    "layer2step54note6",
    "layer2step55note6",
    "layer2step56note6",
    "layer2step57note6",
    "layer2step58note6",
    "layer2step59note6",
    "layer2step60note6",
    "layer2step61note6",
    "layer2step62note6",
    "layer2step63note6",
    "layer2step64note6",
    
    "layer2step1velocity6",             // 960-1023 Poly Seq Track6 Velocities (bit0-7;velocity, bit8;rest)  
    "layer2step2velocity6",
    "layer2step3velocity6",
    "layer2step4velocity6",
    "layer2step5velocity6",
    "layer2step6velocity6",
    "layer2step7velocity6",
    "layer2step8velocity6",
    "layer2step9velocity6",
    "layer2step10velocity6",
    "layer2step11velocity6",
    "layer2step12velocity6",
    "layer2step13velocity6",
    "layer2step14velocity6",
    "layer2step15velocity6",
    "layer2step16velocity6",
    "layer2step17velocity6",
    "layer2step18velocity6",
    "layer2step19velocity6",
    "layer2step20velocity6",
    "layer2step21velocity6",
    "layer2step22velocity6",
    "layer2step23velocity6",
    "layer2step24velocity6",
    "layer2step25velocity6",
    "layer2step26velocity6",
    "layer2step27velocity6",
    "layer2step28velocity6",
    "layer2step29velocity6",
    "layer2step30velocity6",
    "layer2step31velocity6",
    "layer2step32velocity6",
    "layer2step33velocity6",
    "layer2step34velocity6",
    "layer2step35velocity6",
    "layer2step36velocity6",
    "layer2step37velocity6",
    "layer2step38velocity6",
    "layer2step39velocity6",
    "layer2step40velocity6",
    "layer2step41velocity6",
    "layer2step42velocity6",
    "layer2step43velocity6",
    "layer2step44velocity6",
    "layer2step45velocity6",
    "layer2step46velocity6",
    "layer2step47velocity6",
    "layer2step48velocity6",
    "layer2step49velocity6",
    "layer2step50velocity6",
    "layer2step51velocity6",
    "layer2step52velocity6",
    "layer2step53velocity6",
    "layer2step54velocity6",
    "layer2step55velocity6",
    "layer2step56velocity6",
    "layer2step57velocity6",
    "layer2step58velocity6",
    "layer2step59velocity6",
    "layer2step60velocity6",
    "layer2step61velocity6",
    "layer2step62velocity6",
    "layer2step63velocity6",
    "layer2step64velocity6",
    };
 
    /** Map of parameter -> index in the allParameters array. */
    HashMap nrpnparametersToIndex = new HashMap();
    
    final static String[] nrpnparameters = new String[]
    {
    /** LAYER A: */
    "layer1dco1frequency",              // 0      OSC1 Freq (0-120)
    "layer1dco1finetune",               // 1      OSC1 Finetune (0-100)
    "layer1dco1shape",                  // 2      OSC1 Shape (0-4, off/Saw/SawTri/Tri/Pul)
    "layer1dco1glide",                  // 3      OSC1 Glide (0-127)
    "layer1dco1key",                    // 4      OSC1 Key on/off (0-1, off/on)
    
    "layer1dco2frequency",              // 5      OSC1 Freq (0-120)
    "layer1dco2finetune",               // 6      OSC2 Finetune (0-100)
    "layer1dco2shape",                  // 7      OSC2 Shape (0-4, off/Saw/SawTri/Tri/Pul)
    "layer1dco2glide",                  // 8      OSC2 Glide (0-127)
    "layer1dco2key",                    // 9      OSC2 Key on/off (0-1, off/on)
    
    "layer1sync",                       // 10      Sync (0-1, off/on)
    "layer1glidemode",                  // 11      Glide Mode (0-3, Fixed Rate/Fixed Rate A/Fixed Time/Fixed Time A)
    "layer1slop",                       // 12      OSC Slop (0-127)
    "layer1mix",                        // 13      OSC Mix (0-127)
    "layer1noise",                      // 14      Noise Level (0-127)
    
    "layer1vcffrequency",               // 15      Cutoff Freq (0-164)
    "layer1vcfresonance",               // 16      Resonance (0-127)
    "layer1vcfkeyboardamount",          // 17      Filter Key Amount (0-127)    
    "layer1vcfaudiomodulation",         // 18      Filter Audio Mod (0-127)
    "layer1vcfpoles",                   // 19      Filter Poles (0-1, 2pole/4pole)
    "layer1env1amount",                 // 20      EnvF Amount (0-254)
    "layer1env1velocityamount",         // 21      EnvF Velocity (0-127)
    "layer1env1delay",                  // 22      EnvF Delay (0-127)
    "layer1env1attack",                 // 23      EnvF Attack (0-127)
    "layer1env1decay",                  // 24      EnvF Decay (0-127)
    "layer1env1sustain",                // 25      EnvF Sustain (0-127)
    "layer1env1release",                // 26      EnvF Release (0-127)
    
    "---",                              // 27
    
    "layer1vcaoutputspread",            // 28      Pan Spread (0-127)
    "layer1vcavoicevolume",             // 29      Program Volume (0-127)
    "layer1env2amount",                 // 30      EnvA Amount (0-127)
    "layer1env2velocityamount",         // 31      EnvA Velocity (0-127)
    "layer1env2delay",                  // 32      EnvA Delay (0-127)
    "layer1env2attack",                 // 33      EnvA Attack (0-127)
    "layer1env2decay",                  // 34      EnvA Decay (0-127)
    "layer1env2sustain",                // 35      EnvA Sustain (0-127)
    "layer1env2release",                // 36      EnvA Release (0-127)
    
    "layer1lfo1frequency",              // 37      LFO1 Rate (0-150)
    "layer1lfo1shape",                  // 38      LFO1 Shape (0-5, off/Tri/Saw/RevSaw/Square/Random)
    "layer1lfo1amount",                 // 39      LFO1 Amount (0-127)
    "layer1lfo1moddestination",         // 40      LFO1 Destination (0-52, *see table below)
    "layer1lfo1clocksync",              // 41      LFO1 ClkSync (0-1, 0ff/on)
    
    "layer1lfo2frequency",              // 42     LFO2 Rate (0-150)
    "layer1lfo2shape",                  // 43      LFO2 Shape (0-5, off/Tri/Saw/RevSaw/Square/Random)
    "layer1lfo2amount",                 // 44      LFO2 Amount (0-127)
    "layer1lfo2moddestination",         // 45      LFO2 Destination (0-52, *see table below)
    "layer1lfo2clocksync",              // 46      LFO2 ClkSync (0-1, 0ff/on)
    
    "layer1lfo3frequency",              // 47      LFO3 Rate (0-150)
    "layer1lfo3shape",                  // 48      LFO3 Shape (0-5, off/Tri/Saw/RevSaw/Square/Random)
    "layer1lfo3amount",                 // 49      LFO3 Amount (0-127)
    "layer1lfo3moddestination",         // 50      LFO3 Destination (0-52, *see table below)
    "layer1lfo3clocksync",              // 51      LFO3 ClkSync (0-1, 0ff/on)
    
    "layer1lfo4frequency",              // 52      LFO4 Rate (0-150)
    "layer1lfo4shape",                  // 53      LFO4 Shape (0-5, off/Tri/Saw/RevSaw/Square/Random)    
    "layer1lfo4amount",                 // 54      LFO4 Amount (0-127)
    "layer1lfo4moddestination",         // 55      LFO4 Destination (0-52, *see table below)
    "layer1lfo4clocksync",              // 56      LFO4 ClkSync (0-1, 0ff/on)
    
    "layer1env3moddestination",         // 57      Env3 Destination (0-52, *see table below)
    "layer1env3amount",                 // 58      Env3 Amount (0-254)
    "layer1env3velocityamount",         // 59      Env3 Velocity (0-127)
    "layer1env3delay",                  // 60      Env3 Delay (0-127)
    "layer1env3attack",                 // 61      Env3 Attack (0-127)
    "layer1env3decay",                  // 62      Env3 Decay (0-127)
    "layer1env3sustain",                // 63      Env3 Sustain (0-127)  
    "layer1env3release",                // 64      Env3 Release (0-127)
    
    "layer1mod1source",                 // 65     Mod1 Source (0-22, *see table below)
    "layer1mod1amount",                 // 66      Mod1 Amount (0-254)
    "layer1mod1destination",            // 67      Mod1 Destination (0-52, *see table below)
    "layer1mod2source",                 // 68      Mod2 Source (0-22, *see table below)
    "layer1mod2amount",                 // 69      Mod2 Amount (0-254)
    "layer1mod2destination",            // 70      Mod2 Destination (0-52, *see table below)
    "layer1mod3source",                 // 71      Mod3 Source (0-22, *see table below)
    "layer1mod3amount",                 // 72      Mod3 Amount (0-254)
    "layer1mod3destination",            // 73      Mod3 Destination (0-52, *see table below)
    "layer1mod4source",                 // 74      Mod4 Source (0-22, *see table below)
    "layer1mod4amount",                 // 75      Mod4 Amount (0-254)
    "layer1mod4destination",            // 76      Mod4 Destination (0-52, *see table below)
    "layer1mod5source",                 // 77      Mod5 Source (0-22, *see table below)
    "layer1mod5amount",                 // 78      Mod5 Amount (0-254)
    "layer1mod5destination",            // 79      Mod5 Destination (0-52, *see table below)
    "layer1mod6source",                 // 80      Mod6 Source (0-22, *see table below)
    "layer1mod6amount",                 // 81      Mod6 Amount (0-254)
    "layer1mod6destination",            // 82      Mod6 Destination (0-52, *see table below)
    "layer1mod7source",                 // 83      Mod7 Source (0-22, *see table below)
    "layer1mod7amount",                 // 84      Mod7 Amount (0-254)
    "layer1mod7destination",            // 84      Mod7 Destination (0-52, *see table below)
    "layer1mod8source",                 // 86      Mod8 Source (0-22, *see table below)  
    "layer1mod8amount",                 // 87      Mod8 Amount (0-254)    
    "layer1mod8destination",            // 88      Mod8 Destination (0-52, *see table below)
    
    "---",                              // 89
    "---",                              // 90
    "---",                              // 91
    "---",                              // 92
    "---",                              // 93
    "---",                              // 94
    "---",                              // 95
    "---",                              // 96
    
    "layer1env3repeat",                 // 97      Env3 Repeat (0-1, off/on)
    
    "layer1vcainitiallevel",            // 98      VCA Level (0-127)
    
    "layer1dco1notereset",              // 99      OSC1 Note Reset (0-1, off/on)
    
    "---",                              // 100
    "---",                              // 101
    
    "layer1dco1shapemod",               // 102      OSC1 ShapeMod (0-99)
    "layer1dco2shapemod",               // 103      OSC2 ShapeMod (0-99)
    "layer1dco2notereset",              // 104      OSC2 Note Reset (0-1, off/on)
    
    "layer1lfo1keysync",                // 105      LFO1 KeySync (0-1, off/on)
    "layer1lfo2keysync",                // 106     LFO2 KeySync (0-1, off/on)
    "layer1lfo3keysync",                // 107     LFO3 KeySync (0-1, off/on)
    "layer1lfo4keysync",                // 108      LFO4 KeySync (0-1, off/on)
            
    "---",                              // 109
    
    "layer1subosc",                     // 110      SubOct Level (0-127)
            
    "layer1glideonoff",                 // 111      Glide on/off (0-1, off/on)
    
    "---",                              // 112
    
    "layer1pitchbendrange",             // 113      Pitch Bend Range (0-12)
    
    "layer1panmode",                    // 114      Pan Mod Mode (0-1, Alternate/Fixed)
    
    "---",                              // 115
            
    "layer1wheelamount",                // 116      Mod Wheel Amount (0-254)
    "layer1wheeldestination",           // 117      Mod Wheel Destination (0-52, *see table below)
    "layer1pressureamount",             // 118      Pressure Mod Amount (0-254)     
    "layer1pressuredestination",        // 119      Pressure Mod Destination (0-52, *see table below)
    "layer1breathamount",               // 120      Breath Mod Amount (0-254)
    "layer1breathdestination",          // 121      Breath Mod Destination (0-52, *see table below) 
    "layer1velocityamount",             // 122      Velocity Mod Amount (0-254)
    "layer1velocitydestination",        // 123      Velocity Mod Destination (0-52, *see table below) 
    "layer1footamount",                 // 124      MIDI Foot Mod Amount (0-254)
    "layer1footdestination",            // 125      MIDI Foot Mod Destination (0-52, *see table below)
    
    "---",                              // 126
    "---",                              // 127
    "---",                              // 128
    "---",                              // 129
    "---",                              // 130
    "---",                              // 131
    "---",                              // 132
    "---",                              // 133
    "---",                              // 134
    "---",                              // 135
    "---",                              // 136
    "---",                              // 137
    "---",                              // 138
    "---",                              // 139
    "---",                              // 140
    "---",                              // 141
    "---",                              // 142
    "---",                              // 143
    "---",                              // 144
    "---",                              // 145
    "---",                              // 146
    "---",                              // 147
    "---",                              // 148
    "---",                              // 149
    "---",                              // 150
    "---",                              // 151
    "---",                              // 152
    
    "layer1fxonoff",                    // 153      FX on/off (0-1, off/on)
    "layer1fxselect",                   // 154      FX Select (0-13, Off/DM/DDS/BBD/Ch/PH/PL/PM/F1/F2/Rvb/RM/Dst/HPF)
    "layer1fxmix",                      // 155          FX mix
    "layer1fxparam1",                   // 156      FX Parameter 1 (0-255)
    "layer1fxparam2",                   // 157      FX Parameter 2 (0-255)
    "layer1fxclocksync",                // 158      FX Clock Sync on/off (0-1, off/on)
    
    "---",                              // 159
    "---",                              // 160
    "---",                              // 161
    "---",                              // 162
    
    "keyboardmode",                     // 163      Layer Mode (0-2, LayerA/SplitAB/StackAB)
    
    "---",                              // 164      Sequencer start/stop
    
    "---",                              // 165
    "---",                              // 166
    
    "layer1unisondetune",               // 167      Unison Detune (0-16)
    "layer1unison",                     // 168      Unison on/off (0-1, off/on)
    "layer1unisonmode",                 // 169      Unison Mode (0-15/Chord)
    "layer1unisonkeymode",              // 170      Key Mode (0-5, Low/Hi/Last/LowR/HiR/LastR)
    
    "splitpoint",                       // 171      Split Point (0-120)
    
    "layer1arpeggiator",                // 172      Arp on/off (0-1, off/on)
    "layer1arpeggiatormode",            // 173      Arp Mode (0-4, Up/Down/Up+Down/Random/assign)
    "layer1arpeggiatoroctaves",         // 174      Arp Range (0-2, 1Oct/2Oct/3Oct)
    
    "layer1clockdivide",                // 175      Divide (0-12, H/Q/8th/8H/8S/8T/16th/16H/16S/16T/32nd/32T/64T
    
    "---",                              // 176
    
    "layer1arpeggiatorrepeats",         // 177      Arp Repeats (0-3)
    "layer1arpeggiatorrelatch",         // 178      Arp Relatch on/off (0-1, off/on)
    
    "layer1tempo",                      // 179      BPM (30-250)
    
    "---",                              // 180
    "---",                              // 181
    
    "layer1sequencertrigger",           // 182      Sequencer Mode (0-4, Normal/NoReset/NoGate/NoGateReset/KeyStep)
    "layer1gatedpolyseq",               // 183      Sequencer Type (0-1, Gated/Poly)
    "layer1track1destination",          // 184      Gated Seq1 Destination (0-52, *see table below)
    "layer1track2destination",          // 185      Gated Seq2 Destination (0-53, *see table below)
    "layer1track3destination",          // 186      Gated Seq3 Destination (0-52, *see table below)
    "layer1track4destination",          // 187      Gated Seq4 Destination (0-53, *see table below)
            
    "---",                              // 188
    "---",                              // 189
    "---",                              // 190
    "---",                              // 191
                    
    "layer1track1note1",                // 192      Gated Seq1 Step 1-16 (0-125/reset/rest)
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
    "layer1track1note16",               // 207
           
    "layer1track2note1",                // 208      Gated Seq2 Step 1-16 (0-125/reset/rest) 
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
    "layer1track2note16",               // 223
            
    "layer1track3note1",                // 224      Gated Seq3 Step 1-16 (0-125/reset/rest)  
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
    "layer1track3note16",               // 239
            
    "layer1track4note1",                // 240      Gated Seq4 Step 1-16 (0-125/reset/rest)    
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
    "layer1track4note16",               // 255
    
    "---",                              // 256      Name
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
    "---",                              // 275
            
               
    "layer1step1note1",                 // 276-339  Poly Seq Track1 Notes (bit0-7;note, bit8;tie)  
    "layer1step2note1",
    "layer1step3note1",
    "layer1step4note1",
    "layer1step5note1",
    "layer1step6note1",
    "layer1step7note1",
    "layer1step8note1",
    "layer1step9note1",
    "layer1step10note1",
    "layer1step11note1",
    "layer1step12note1",
    "layer1step13note1",
    "layer1step14note1",
    "layer1step15note1",
    "layer1step16note1",
    "layer1step17note1",
    "layer1step18note1",
    "layer1step19note1",
    "layer1step20note1",
    "layer1step21note1",
    "layer1step22note1",
    "layer1step23note1",
    "layer1step24note1",
    "layer1step25note1",
    "layer1step26note1",
    "layer1step27note1",
    "layer1step28note1",
    "layer1step29note1",
    "layer1step30note1",
    "layer1step31note1",
    "layer1step32note1",
    "layer1step33note1",
    "layer1step34note1",
    "layer1step35note1",
    "layer1step36note1",
    "layer1step37note1",
    "layer1step38note1",
    "layer1step39note1",
    "layer1step40note1",
    "layer1step41note1",
    "layer1step42note1",
    "layer1step43note1",
    "layer1step44note1",
    "layer1step45note1",
    "layer1step46note1",
    "layer1step47note1",
    "layer1step48note1",
    "layer1step49note1",
    "layer1step50note1",
    "layer1step51note1",
    "layer1step52note1",
    "layer1step53note1",
    "layer1step54note1",
    "layer1step55note1",
    "layer1step56note1",
    "layer1step57note1",
    "layer1step58note1",
    "layer1step59note1",
    "layer1step60note1",
    "layer1step61note1",
    "layer1step62note1",
    "layer1step63note1",
    "layer1step64note1",
            
    "layer1step1velocity1",             // 340-403  Poly Seq Track1 Velocities (bit0-7;velocity, bit8;rest)  
    "layer1step2velocity1",
    "layer1step3velocity1",
    "layer1step4velocity1",
    "layer1step5velocity1",
    "layer1step6velocity1",
    "layer1step7velocity1",
    "layer1step8velocity1",
    "layer1step9velocity1",
    "layer1step10velocity1",
    "layer1step11velocity1",
    "layer1step12velocity1",
    "layer1step13velocity1",
    "layer1step14velocity1",
    "layer1step15velocity1",
    "layer1step16velocity1",
    "layer1step17velocity1",
    "layer1step18velocity1",
    "layer1step19velocity1",
    "layer1step20velocity1",
    "layer1step21velocity1",
    "layer1step22velocity1",
    "layer1step23velocity1",
    "layer1step24velocity1",
    "layer1step25velocity1",
    "layer1step26velocity1",
    "layer1step27velocity1",
    "layer1step28velocity1",
    "layer1step29velocity1",
    "layer1step30velocity1",
    "layer1step31velocity1",
    "layer1step32velocity1",
    "layer1step33velocity1",
    "layer1step34velocity1",
    "layer1step35velocity1",
    "layer1step36velocity1",
    "layer1step37velocity1",
    "layer1step38velocity1",
    "layer1step39velocity1",
    "layer1step40velocity1",
    "layer1step41velocity1",
    "layer1step42velocity1",
    "layer1step43velocity1",
    "layer1step44velocity1",
    "layer1step45velocity1",
    "layer1step46velocity1",
    "layer1step47velocity1",
    "layer1step48velocity1",
    "layer1step49velocity1",
    "layer1step50velocity1",
    "layer1step51velocity1",
    "layer1step52velocity1",
    "layer1step53velocity1",
    "layer1step54velocity1",
    "layer1step55velocity1",
    "layer1step56velocity1",
    "layer1step57velocity1",
    "layer1step58velocity1",
    "layer1step59velocity1",
    "layer1step60velocity1",
    "layer1step61velocity1",
    "layer1step62velocity1",
    "layer1step63velocity1",
    "layer1step64velocity1",

    "layer1step1note2",                 // 404-467  Poly Seq Track2 Notes (bit0-7;note, bit8;tie)
    "layer1step2note2",
    "layer1step3note2",
    "layer1step4note2",
    "layer1step5note2",
    "layer1step6note2",
    "layer1step7note2",
    "layer1step8note2",
    "layer1step9note2",
    "layer1step10note2",
    "layer1step11note2",
    "layer1step12note2",
    "layer1step13note2",
    "layer1step14note2",
    "layer1step15note2",
    "layer1step16note2",
    "layer1step17note2",
    "layer1step18note2",
    "layer1step19note2",
    "layer1step20note2",
    "layer1step21note2",
    "layer1step22note2",
    "layer1step23note2",
    "layer1step24note2",
    "layer1step25note2",
    "layer1step26note2",
    "layer1step27note2",
    "layer1step28note2",
    "layer1step29note2",
    "layer1step30note2",
    "layer1step31note2",
    "layer1step32note2",
    "layer1step33note2",
    "layer1step34note2",
    "layer1step35note2",
    "layer1step36note2",
    "layer1step37note2",
    "layer1step38note2",
    "layer1step39note2",
    "layer1step40note2",
    "layer1step41note2",
    "layer1step42note2",
    "layer1step43note2",
    "layer1step44note2",
    "layer1step45note2",
    "layer1step46note2",
    "layer1step47note2",
    "layer1step48note2",
    "layer1step49note2",
    "layer1step50note2",
    "layer1step51note2",
    "layer1step52note2",
    "layer1step53note2",
    "layer1step54note2",
    "layer1step55note2",
    "layer1step56note2",
    "layer1step57note2",
    "layer1step58note2",
    "layer1step59note2",
    "layer1step60note2",
    "layer1step61note2",
    "layer1step62note2",
    "layer1step63note2",
    "layer1step64note2",
    
    "layer1step1velocity2",             // 468-531  Poly Seq Track2 Velocities (bit0-7;velocity, bit8;rest)
    "layer1step2velocity2",
    "layer1step3velocity2",
    "layer1step4velocity2",
    "layer1step5velocity2",
    "layer1step6velocity2",
    "layer1step7velocity2",
    "layer1step8velocity2",
    "layer1step9velocity2",
    "layer1step10velocity2",
    "layer1step11velocity2",
    "layer1step12velocity2",
    "layer1step13velocity2",
    "layer1step14velocity2",
    "layer1step15velocity2",
    "layer1step16velocity2",
    "layer1step17velocity2",
    "layer1step18velocity2",
    "layer1step19velocity2",
    "layer1step20velocity2",
    "layer1step21velocity2",
    "layer1step22velocity2",
    "layer1step23velocity2",
    "layer1step24velocity2",
    "layer1step25velocity2",
    "layer1step26velocity2",
    "layer1step27velocity2",
    "layer1step28velocity2",
    "layer1step29velocity2",
    "layer1step30velocity2",
    "layer1step31velocity2",
    "layer1step32velocity2",
    "layer1step33velocity2",
    "layer1step34velocity2",
    "layer1step35velocity2",
    "layer1step36velocity2",
    "layer1step37velocity2",
    "layer1step38velocity2",
    "layer1step39velocity2",
    "layer1step40velocity2",
    "layer1step41velocity2",
    "layer1step42velocity2",
    "layer1step43velocity2",
    "layer1step44velocity2",
    "layer1step45velocity2",
    "layer1step46velocity2",
    "layer1step47velocity2",
    "layer1step48velocity2",
    "layer1step49velocity2",
    "layer1step50velocity2",
    "layer1step51velocity2",
    "layer1step52velocity2",
    "layer1step53velocity2",
    "layer1step54velocity2",
    "layer1step55velocity2",
    "layer1step56velocity2",
    "layer1step57velocity2",
    "layer1step58velocity2",
    "layer1step59velocity2",
    "layer1step60velocity2",
    "layer1step61velocity2",
    "layer1step62velocity2",
    "layer1step63velocity2",
    "layer1step64velocity2",
    
    "layer1step1note3",                 // 532-595  Poly Seq Track3 Notes (bit0-7;note, bit8;tie)  
    "layer1step2note3",
    "layer1step3note3",
    "layer1step4note3",
    "layer1step5note3",
    "layer1step6note3",
    "layer1step7note3",
    "layer1step8note3",
    "layer1step9note3",
    "layer1step10note3",
    "layer1step11note3",
    "layer1step12note3",
    "layer1step13note3",
    "layer1step14note3",
    "layer1step15note3",
    "layer1step16note3",
    "layer1step17note3",
    "layer1step18note3",
    "layer1step19note3",
    "layer1step20note3",
    "layer1step21note3",
    "layer1step22note3",
    "layer1step23note3",
    "layer1step24note3",
    "layer1step25note3",
    "layer1step26note3",
    "layer1step27note3",
    "layer1step28note3",
    "layer1step29note3",
    "layer1step30note3",
    "layer1step31note3",
    "layer1step32note3",
    "layer1step33note3",
    "layer1step34note3",
    "layer1step35note3",
    "layer1step36note3",
    "layer1step37note3",
    "layer1step38note3",
    "layer1step39note3",
    "layer1step40note3",
    "layer1step41note3",
    "layer1step42note3",
    "layer1step43note3",
    "layer1step44note3",
    "layer1step45note3",
    "layer1step46note3",
    "layer1step47note3",
    "layer1step48note3",
    "layer1step49note3",
    "layer1step50note3",
    "layer1step51note3",
    "layer1step52note3",
    "layer1step53note3",
    "layer1step54note3",
    "layer1step55note3",
    "layer1step56note3",
    "layer1step57note3",
    "layer1step58note3",
    "layer1step59note3",
    "layer1step60note3",
    "layer1step61note3",
    "layer1step62note3",
    "layer1step63note3",
    "layer1step64note3",
    
    "layer1step1velocity3",             // 596-659  Poly Seq Track3 Velocities (bit0-7;velocity, bit8;rest)  
    "layer1step2velocity3",
    "layer1step3velocity3",
    "layer1step4velocity3",
    "layer1step5velocity3",
    "layer1step6velocity3",
    "layer1step7velocity3",
    "layer1step8velocity3",
    "layer1step9velocity3",
    "layer1step10velocity3",
    "layer1step11velocity3",
    "layer1step12velocity3",
    "layer1step13velocity3",
    "layer1step14velocity3",
    "layer1step15velocity3",
    "layer1step16velocity3",
    "layer1step17velocity3",
    "layer1step18velocity3",
    "layer1step19velocity3",
    "layer1step20velocity3",
    "layer1step21velocity3",
    "layer1step22velocity3",
    "layer1step23velocity3",
    "layer1step24velocity3",
    "layer1step25velocity3",
    "layer1step26velocity3",
    "layer1step27velocity3",
    "layer1step28velocity3",
    "layer1step29velocity3",
    "layer1step30velocity3",
    "layer1step31velocity3",
    "layer1step32velocity3",
    "layer1step33velocity3",
    "layer1step34velocity3",
    "layer1step35velocity3",
    "layer1step36velocity3",
    "layer1step37velocity3",
    "layer1step38velocity3",
    "layer1step39velocity3",
    "layer1step40velocity3",
    "layer1step41velocity3",
    "layer1step42velocity3",
    "layer1step43velocity3",
    "layer1step44velocity3",
    "layer1step45velocity3",
    "layer1step46velocity3",
    "layer1step47velocity3",
    "layer1step48velocity3",
    "layer1step49velocity3",
    "layer1step50velocity3",
    "layer1step51velocity3",
    "layer1step52velocity3",
    "layer1step53velocity3",
    "layer1step54velocity3",
    "layer1step55velocity3",
    "layer1step56velocity3",
    "layer1step57velocity3",
    "layer1step58velocity3",
    "layer1step59velocity3",
    "layer1step60velocity3",
    "layer1step61velocity3",
    "layer1step62velocity3",
    "layer1step63velocity3",
    "layer1step64velocity3",

    "layer1step1note4",                 // 660-723  Poly Seq Track4 Notes (bit0-7;note, bit8;tie)
    "layer1step2note4",
    "layer1step3note4",
    "layer1step4note4",
    "layer1step5note4",
    "layer1step6note4",
    "layer1step7note4",
    "layer1step8note4",
    "layer1step9note4",
    "layer1step10note4",
    "layer1step11note4",
    "layer1step12note4",
    "layer1step13note4",
    "layer1step14note4",
    "layer1step15note4",
    "layer1step16note4",
    "layer1step17note4",
    "layer1step18note4",
    "layer1step19note4",
    "layer1step20note4",
    "layer1step21note4",
    "layer1step22note4",
    "layer1step23note4",
    "layer1step24note4",
    "layer1step25note4",
    "layer1step26note4",
    "layer1step27note4",
    "layer1step28note4",
    "layer1step29note4",
    "layer1step30note4",
    "layer1step31note4",
    "layer1step32note4",
    "layer1step33note4",
    "layer1step34note4",
    "layer1step35note4",
    "layer1step36note4",
    "layer1step37note4",
    "layer1step38note4",
    "layer1step39note4",
    "layer1step40note4",
    "layer1step41note4",
    "layer1step42note4",
    "layer1step43note4",
    "layer1step44note4",
    "layer1step45note4",
    "layer1step46note4",
    "layer1step47note4",
    "layer1step48note4",
    "layer1step49note4",
    "layer1step50note4",
    "layer1step51note4",
    "layer1step52note4",
    "layer1step53note4",
    "layer1step54note4",
    "layer1step55note4",
    "layer1step56note4",
    "layer1step57note4",
    "layer1step58note4",
    "layer1step59note4",
    "layer1step60note4",
    "layer1step61note4",
    "layer1step62note4",
    "layer1step63note4",
    "layer1step64note4",
    
    "layer1step1velocity4",             // 724-787  Poly Seq Track4 Velocities (bit0-7;velocity, bit8;rest)  
    "layer1step2velocity4",
    "layer1step3velocity4",
    "layer1step4velocity4",
    "layer1step5velocity4",
    "layer1step6velocity4",
    "layer1step7velocity4",
    "layer1step8velocity4",
    "layer1step9velocity4",
    "layer1step10velocity4",
    "layer1step11velocity4",
    "layer1step12velocity4",
    "layer1step13velocity4",
    "layer1step14velocity4",
    "layer1step15velocity4",
    "layer1step16velocity4",
    "layer1step17velocity4",
    "layer1step18velocity4",
    "layer1step19velocity4",
    "layer1step20velocity4",
    "layer1step21velocity4",
    "layer1step22velocity4",
    "layer1step23velocity4",
    "layer1step24velocity4",
    "layer1step25velocity4",
    "layer1step26velocity4",
    "layer1step27velocity4",
    "layer1step28velocity4",
    "layer1step29velocity4",
    "layer1step30velocity4",
    "layer1step31velocity4",
    "layer1step32velocity4",
    "layer1step33velocity4",
    "layer1step34velocity4",
    "layer1step35velocity4",
    "layer1step36velocity4",
    "layer1step37velocity4",
    "layer1step38velocity4",
    "layer1step39velocity4",
    "layer1step40velocity4",
    "layer1step41velocity4",
    "layer1step42velocity4",
    "layer1step43velocity4",
    "layer1step44velocity4",
    "layer1step45velocity4",
    "layer1step46velocity4",
    "layer1step47velocity4",
    "layer1step48velocity4",
    "layer1step49velocity4",
    "layer1step50velocity4",
    "layer1step51velocity4",
    "layer1step52velocity4",
    "layer1step53velocity4",
    "layer1step54velocity4",
    "layer1step55velocity4",
    "layer1step56velocity4",
    "layer1step57velocity4",
    "layer1step58velocity4",
    "layer1step59velocity4",
    "layer1step60velocity4",
    "layer1step61velocity4",
    "layer1step62velocity4",
    "layer1step63velocity4",
    "layer1step64velocity4",
    
    "layer1step1note5",                 // 788-851  Poly Seq Track5 Notes (bit0-7;note, bit8;tie)
    "layer1step2note5",
    "layer1step3note5",
    "layer1step4note5",
    "layer1step5note5",
    "layer1step6note5",
    "layer1step7note5",
    "layer1step8note5",
    "layer1step9note5",
    "layer1step10note5",
    "layer1step11note5",
    "layer1step12note5",
    "layer1step13note5",
    "layer1step14note5",
    "layer1step15note5",
    "layer1step16note5",
    "layer1step17note5",
    "layer1step18note5",
    "layer1step19note5",
    "layer1step20note5",
    "layer1step21note5",
    "layer1step22note5",
    "layer1step23note5",
    "layer1step24note5",
    "layer1step25note5",
    "layer1step26note5",
    "layer1step27note5",
    "layer1step28note5",
    "layer1step29note5",
    "layer1step30note5",
    "layer1step31note5",
    "layer1step32note5",
    "layer1step33note5",
    "layer1step34note5",
    "layer1step35note5",
    "layer1step36note5",
    "layer1step37note5",
    "layer1step38note5",
    "layer1step39note5",
    "layer1step40note5",
    "layer1step41note5",
    "layer1step42note5",
    "layer1step43note5",
    "layer1step44note5",
    "layer1step45note5",
    "layer1step46note5",
    "layer1step47note5",
    "layer1step48note5",
    "layer1step49note5",
    "layer1step50note5",
    "layer1step51note5",
    "layer1step52note5",
    "layer1step53note5",
    "layer1step54note5",
    "layer1step55note5",
    "layer1step56note5",
    "layer1step57note5",
    "layer1step58note5",
    "layer1step59note5",
    "layer1step60note5",
    "layer1step61note5",
    "layer1step62note5",
    "layer1step63note5",
    "layer1step64note5",
    
    "layer1step1velocity5",             // 852-915  Poly Seq Track5 Velocities (bit0-7;velocity, bit8;rest)
    "layer1step2velocity5",
    "layer1step3velocity5",
    "layer1step4velocity5",
    "layer1step5velocity5",
    "layer1step6velocity5",
    "layer1step7velocity5",
    "layer1step8velocity5",
    "layer1step9velocity5",
    "layer1step10velocity5",
    "layer1step11velocity5",
    "layer1step12velocity5",
    "layer1step13velocity5",
    "layer1step14velocity5",
    "layer1step15velocity5",
    "layer1step16velocity5",
    "layer1step17velocity5",
    "layer1step18velocity5",
    "layer1step19velocity5",
    "layer1step20velocity5",
    "layer1step21velocity5",
    "layer1step22velocity5",
    "layer1step23velocity5",
    "layer1step24velocity5",
    "layer1step25velocity5",
    "layer1step26velocity5",
    "layer1step27velocity5",
    "layer1step28velocity5",
    "layer1step29velocity5",
    "layer1step30velocity5",
    "layer1step31velocity5",
    "layer1step32velocity5",
    "layer1step33velocity5",
    "layer1step34velocity5",
    "layer1step35velocity5",
    "layer1step36velocity5",
    "layer1step37velocity5",
    "layer1step38velocity5",
    "layer1step39velocity5",
    "layer1step40velocity5",
    "layer1step41velocity5",
    "layer1step42velocity5",
    "layer1step43velocity5",
    "layer1step44velocity5",
    "layer1step45velocity5",
    "layer1step46velocity5",
    "layer1step47velocity5",
    "layer1step48velocity5",
    "layer1step49velocity5",
    "layer1step50velocity5",
    "layer1step51velocity5",
    "layer1step52velocity5",
    "layer1step53velocity5",
    "layer1step54velocity5",
    "layer1step55velocity5",
    "layer1step56velocity5",
    "layer1step57velocity5",
    "layer1step58velocity5",
    "layer1step59velocity5",
    "layer1step60velocity5",
    "layer1step61velocity5",
    "layer1step62velocity5",
    "layer1step63velocity5",
    "layer1step64velocity5",

    "layer1step1note6",                 // 916-979  Poly Seq Track6 Notes (bit0-7;note, bit8;tie)  
    "layer1step2note6",
    "layer1step3note6",
    "layer1step4note6",
    "layer1step5note6",
    "layer1step6note6",
    "layer1step7note6",
    "layer1step8note6",
    "layer1step9note6",
    "layer1step10note6",
    "layer1step11note6",
    "layer1step12note6",
    "layer1step13note6",
    "layer1step14note6",
    "layer1step15note6",
    "layer1step16note6",
    "layer1step17note6",
    "layer1step18note6",
    "layer1step19note6",
    "layer1step20note6",
    "layer1step21note6",
    "layer1step22note6",
    "layer1step23note6",
    "layer1step24note6",
    "layer1step25note6",
    "layer1step26note6",
    "layer1step27note6",
    "layer1step28note6",
    "layer1step29note6",
    "layer1step30note6",
    "layer1step31note6",
    "layer1step32note6",
    "layer1step33note6",
    "layer1step34note6",
    "layer1step35note6",
    "layer1step36note6",
    "layer1step37note6",
    "layer1step38note6",
    "layer1step39note6",
    "layer1step40note6",
    "layer1step41note6",
    "layer1step42note6",
    "layer1step43note6",
    "layer1step44note6",
    "layer1step45note6",
    "layer1step46note6",
    "layer1step47note6",
    "layer1step48note6",
    "layer1step49note6",
    "layer1step50note6",
    "layer1step51note6",
    "layer1step52note6",
    "layer1step53note6",
    "layer1step54note6",
    "layer1step55note6",
    "layer1step56note6",
    "layer1step57note6",
    "layer1step58note6",
    "layer1step59note6",
    "layer1step60note6",
    "layer1step61note6",
    "layer1step62note6",
    "layer1step63note6",
    "layer1step64note6",
    
    "layer1step1velocity6",             // 980-1043 Poly Seq Track6 Velocities (bit0-7;velocity, bit8;rest)  
    "layer1step2velocity6",
    "layer1step3velocity6",
    "layer1step4velocity6",
    "layer1step5velocity6",
    "layer1step6velocity6",
    "layer1step7velocity6",
    "layer1step8velocity6",
    "layer1step9velocity6",
    "layer1step10velocity6",
    "layer1step11velocity6",
    "layer1step12velocity6",
    "layer1step13velocity6",
    "layer1step14velocity6",
    "layer1step15velocity6",
    "layer1step16velocity6",
    "layer1step17velocity6",
    "layer1step18velocity6",
    "layer1step19velocity6",
    "layer1step20velocity6",
    "layer1step21velocity6",
    "layer1step22velocity6",
    "layer1step23velocity6",
    "layer1step24velocity6",
    "layer1step25velocity6",
    "layer1step26velocity6",
    "layer1step27velocity6",
    "layer1step28velocity6",
    "layer1step29velocity6",
    "layer1step30velocity6",
    "layer1step31velocity6",
    "layer1step32velocity6",
    "layer1step33velocity6",
    "layer1step34velocity6",
    "layer1step35velocity6",
    "layer1step36velocity6",
    "layer1step37velocity6",
    "layer1step38velocity6",
    "layer1step39velocity6",
    "layer1step40velocity6",
    "layer1step41velocity6",
    "layer1step42velocity6",
    "layer1step43velocity6",
    "layer1step44velocity6",
    "layer1step45velocity6",
    "layer1step46velocity6",
    "layer1step47velocity6",
    "layer1step48velocity6",
    "layer1step49velocity6",
    "layer1step50velocity6",
    "layer1step51velocity6",
    "layer1step52velocity6",
    "layer1step53velocity6",
    "layer1step54velocity6",
    "layer1step55velocity6",
    "layer1step56velocity6",
    "layer1step57velocity6",
    "layer1step58velocity6",
    "layer1step59velocity6",
    "layer1step60velocity6",
    "layer1step61velocity6",
    "layer1step62velocity6",
    "layer1step63velocity6",
    "layer1step64velocity6",
  
    /**
       LAYER B:
       Same as above, just add 2048 to the index value.
    */
    "layer2dco1frequency",              // 0      OSC1 Freq (0-120)
    "layer2dco1finetune",               // 1      OSC1 Finetune (0-100)
    "layer2dco1shape",                  // 2      OSC1 Shape (0-4, off/Saw/SawTri/Tri/Pul)
    "layer2dco1glide",                  // 3     OSC1 Glide (0-127)
    "layer2dco1key",                    // 4      OSC1 Key on/off (0-1, off/on)
    
    "layer2dco2frequency",              // 5      OSC1 Freq (0-120)
    "layer2dco2finetune",               // 6      OSC2 Finetune (0-100)
    "layer2dco2shape",                  // 7      OSC2 Shape (0-4, off/Saw/SawTri/Tri/Pul)
    "layer2dco2glide",                  // 8      OSC2 Glide (0-127)
    "layer2dco2key",                    // 9      OSC2 Key on/off (0-1, off/on)
    
    "layer2sync",                       // 10      Sync (0-1, off/on)
    "layer2glidemode",                  // 11      Glide Mode (0-3, Fixed Rate/Fixed Rate A/Fixed Time/Fixed Time A)
    "layer2slop",                       // 12      OSC Slop (0-127)
    "layer2mix",                        // 13      OSC Mix (0-127)
    "layer2noise",                      // 14      Noise Level (0-127)
    
    "layer2vcffrequency",               // 15      Cutoff Freq (0-164)
    "layer2vcfresonance",               // 16      Resonance (0-127)
    "layer2vcfkeyboardamount",          // 17      Filter Key Amount (0-127)    
    "layer2vcfaudiomodulation",         // 18      Filter Audio Mod (0-127)
    "layer2vcfpoles",                   // 19      Filter Poles (0-1, 2pole/4pole)
    "layer2env1amount",                 // 20      EnvF Amount (0-254)
    "layer2env1velocityamount",         // 21      EnvF Velocity (0-127)
    "layer2env1delay",                  // 22      EnvF Delay (0-127)
    "layer2env1attack",                 // 23      EnvF Attack (0-127)
    "layer2env1decay",                  // 24      EnvF Decay (0-127)
    "layer2env1sustain",                // 25      EnvF Sustain (0-127)
    "layer2env1release",                // 26      EnvF Release (0-127)
    
    "---",                              // 27
    
    "layer2vcaoutputspread",            // 28      Pan Spread (0-127)
    "layer2vcavoicevolume",             // 29      Program Volume (0-127)
    "layer2env2amount",                 // 30      EnvA Amount (0-127)
    "layer2env2velocityamount",         // 31      EnvA Velocity (0-127)
    "layer2env2delay",                  // 32      EnvA Delay (0-127)
    "layer2env2attack",                 // 33      EnvA Attack (0-127)
    "layer2env2decay",                  // 34      EnvA Decay (0-127)
    "layer2env2sustain",                // 35      EnvA Sustain (0-127)
    "layer2env2release",                // 36      EnvA Release (0-127)
    
    "layer2lfo1frequency",              // 37      LFO1 Rate (0-150)
    "layer2lfo1shape",                  // 38      LFO1 Shape (0-5, off/Tri/Saw/RevSaw/Square/Random)
    "layer2lfo1amount",                 // 39      LFO1 Amount (0-127)
    "layer2lfo1moddestination",         // 40      LFO1 Destination (0-52, *see table below)
    "layer2lfo1clocksync",              // 41      LFO1 ClkSync (0-1, 0ff/on)
    
    "layer2lfo2frequency",              // 42     LFO2 Rate (0-150)
    "layer2lfo2shape",                  // 43      LFO2 Shape (0-5, off/Tri/Saw/RevSaw/Square/Random)
    "layer2lfo2amount",                 // 44      LFO2 Amount (0-127)
    "layer2lfo2moddestination",         // 45      LFO2 Destination (0-52, *see table below)
    "layer2lfo2clocksync",              // 46      LFO2 ClkSync (0-1, 0ff/on)
    
    "layer2lfo3frequency",              // 47      LFO3 Rate (0-150)
    "layer2lfo3shape",                  // 48      LFO3 Shape (0-5, off/Tri/Saw/RevSaw/Square/Random)
    "layer2lfo3amount",                 // 49      LFO3 Amount (0-127)
    "layer2lfo3moddestination",         // 50      LFO3 Destination (0-52, *see table below)
    "layer2lfo3clocksync",              // 51      LFO3 ClkSync (0-1, 0ff/on)
    
    "layer2lfo4frequency",              // 52      LFO4 Rate (0-150)
    "layer2lfo4shape",                  // 53      LFO4 Shape (0-5, off/Tri/Saw/RevSaw/Square/Random)    
    "layer2lfo4amount",                 // 54      LFO4 Amount (0-127)
    "layer2lfo4moddestination",         // 55      LFO4 Destination (0-52, *see table below)
    "layer2lfo4clocksync",              // 56      LFO4 ClkSync (0-1, 0ff/on)
    
    "layer2env3moddestination",         // 57      Env3 Destination (0-52, *see table below)
    "layer2env3amount",                 // 58      Env3 Amount (0-254)
    "layer2env3velocityamount",         // 59      Env3 Velocity (0-127)
    "layer2env3delay",                  // 60      Env3 Delay (0-127)
    "layer2env3attack",                 // 61      Env3 Attack (0-127)
    "layer2env3decay",                  // 62      Env3 Decay (0-127)
    "layer2env3sustain",                // 63      Env3 Sustain (0-127)  
    "layer2env3release",                // 64      Env3 Release (0-127)
    
    "layer2mod1source",                 // 65     Mod1 Source (0-22, *see table below)
    "layer2mod1amount",                 // 66      Mod1 Amount (0-254)
    "layer2mod1destination",            // 67      Mod1 Destination (0-52, *see table below)
    "layer2mod2source",                 // 68      Mod2 Source (0-22, *see table below)
    "layer2mod2amount",                 // 69      Mod2 Amount (0-254)
    "layer2mod2destination",            // 70      Mod2 Destination (0-52, *see table below)
    "layer2mod3source",                 // 71      Mod3 Source (0-22, *see table below)
    "layer2mod3amount",                 // 72      Mod3 Amount (0-254)
    "layer2mod3destination",            // 73      Mod3 Destination (0-52, *see table below)
    "layer2mod4source",                 // 74      Mod4 Source (0-22, *see table below)
    "layer2mod4amount",                 // 75      Mod4 Amount (0-254)
    "layer2mod4destination",            // 76      Mod4 Destination (0-52, *see table below)
    "layer2mod5source",                 // 77      Mod5 Source (0-22, *see table below)
    "layer2mod5amount",                 // 78      Mod5 Amount (0-254)
    "layer2mod5destination",            // 79      Mod5 Destination (0-52, *see table below)
    "layer2mod6source",                 // 80      Mod6 Source (0-22, *see table below)
    "layer2mod6amount",                 // 81      Mod6 Amount (0-254)
    "layer2mod6destination",            // 82      Mod6 Destination (0-52, *see table below)
    "layer2mod7source",                 // 83      Mod7 Source (0-22, *see table below)
    "layer2mod7amount",                 // 84      Mod7 Amount (0-254)
    "layer2mod7destination",            // 84      Mod7 Destination (0-52, *see table below)
    "layer2mod8source",                 // 86      Mod8 Source (0-22, *see table below)  
    "layer2mod8amount",                 // 87      Mod8 Amount (0-254)    
    "layer2mod8destination",            // 88      Mod8 Destination (0-52, *see table below)
    
    "---",                              // 89
    "---",                              // 90
    "---",                              // 91
    "---",                              // 92
    "---",                              // 93
    "---",                              // 94
    "---",                              // 95
    "---",                              // 96
    
    "layer2env3repeat",                 // 97      Env3 Repeat (0-1, off/on)
    
    "layer2vcainitiallevel",            // 98      VCA Level (0-127)
    
    "layer2dco1notereset",              // 99      OSC1 Note Reset (0-1, off/on)
    
    "---",                              // 100
    "---",                              // 101
    
    "layer2dco1shapemod",               // 102      OSC1 ShapeMod (0-99)
    "layer2dco2shapemod",               // 103      OSC2 ShapeMod (0-99)
    "layer2dco2notereset",              // 104      OSC2 Note Reset (0-1, off/on)
    
    "layer2lfo1keysync",                // 105      LFO1 KeySync (0-1, off/on)
    "layer2lfo2keysync",                // 106     LFO2 KeySync (0-1, off/on)
    "layer2lfo3keysync",                // 107     LFO3 KeySync (0-1, off/on)
    "layer2lfo4keysync",                // 108      LFO4 KeySync (0-1, off/on)
            
    "---",                              // 109
    
    "layer2subosc",                     // 110      SubOct Level (0-127)
            
    "layer2glideonoff",                 // 111      Glide on/off (0-1, off/on)
    
    "---",                              // 112
    
    "layer2pitchbendrange",             // 113      Pitch Bend Range (0-12)
    
    "layer2panmode",                    // 114      Pan Mod Mode (0-1, Alternate/Fixed)
    
    "---",                              // 115
            
    "layer2wheelamount",                // 116      Mod Wheel Amount (0-254)
    "layer2wheeldestination",           // 117      Mod Wheel Destination (0-52, *see table below)
    "layer2pressureamount",             // 118      Pressure Mod Amount (0-254)     
    "layer2pressuredestination",        // 119      Pressure Mod Destination (0-52, *see table below)
    "layer2breathamount",               // 120      Breath Mod Amount (0-254)
    "layer2breathdestination",          // 121      Breath Mod Destination (0-52, *see table below) 
    "layer2velocityamount",             // 122      Velocity Mod Amount (0-254)
    "layer2velocitydestination",        // 123      Velocity Mod Destination (0-52, *see table below) 
    "layer2footamount",                 // 124      MIDI Foot Mod Amount (0-254)
    "layer2footdestination",            // 125      MIDI Foot Mod Destination (0-52, *see table below)
    
    "---",                              // 126
    "---",                              // 127
    "---",                              // 128
    "---",                              // 129
    "---",                              // 130
    "---",                              // 131
    "---",                              // 132
    "---",                              // 133
    "---",                              // 134
    "---",                              // 135
    "---",                              // 136
    "---",                              // 137
    "---",                              // 138
    "---",                              // 139
    "---",                              // 140
    "---",                              // 141
    "---",                              // 142
    "---",                              // 143
    "---",                              // 144
    "---",                              // 145
    "---",                              // 146
    "---",                              // 147
    "---",                              // 148
    "---",                              // 149
    "---",                              // 150
    "---",                              // 151
    "---",                              // 152
    
    "layer2fxonoff",                    // 153      FX on/off (0-1, off/on)
    "layer2fxselect",                   // 154      FX Select (0-13, Off/DM/DDS/BBD/Ch/PH/PL/PM/F1/F2/Rvb/RM/Dst/HPF)
    "layer2fxmix",                      // 155          FX mix
    "layer2fxparam1",                   // 156      FX Parameter 1 (0-255)
    "layer2fxparam2",                   // 157      FX Parameter 2 (0-255)
    "layer2fxclocksync",                // 158      FX Clock Sync on/off (0-1, off/on)
    
    "---",                              // 159
    "---",                              // 160
    "---",                              // 161
    "---",                              // 162
    
    "---",                              // 163      Layer Mode (0-2, LayerA/SplitAB/StackAB)
    
    "---",                              // 164      Sequencer start/stop
    
    "---",                              // 165
    "---",                              // 166
    
    "layer2unisondetune",               // 167      Unison Detune (0-16)
    "layer2unison",                     // 168      Unison on/off (0-1, off/on)
    "layer2unisonmode",                 // 169      Unison Mode (0-15/Chord)
    "layer2unisonkeymode",              // 170      Key Mode (0-5, Low/Hi/Last/LowR/HiR/LastR)
    
    "---",                              // 171      Split Point (0-120)
    
    "layer2arpeggiator",                // 172      Arp on/off (0-1, off/on)
    "layer2arpeggiatormode",            // 173      Arp Mode (0-4, Up/Down/Up+Down/Random/assign)
    "layer2arpeggiatoroctaves",         // 174      Arp Range (0-2, 1Oct/2Oct/3Oct)
    
    "layer2clockdivide",                // 175      Divide (0-12, H/Q/8th/8H/8S/8T/16th/16H/16S/16T/32nd/32T/64T
    
    "---",                              // 176
    
    "layer2arpeggiatorrepeats",         // 177      Arp Repeats (0-3)
    "layer2arpeggiatorrelatch",         // 178      Arp Relatch on/off (0-1, off/on)
    
    "layer2tempo",                      // 179      BPM (30-250)
    
    "---",                              // 180
    "---",                              // 181
    
    "layer2sequencertrigger",           // 182      Sequencer Mode (0-4, Normal/NoReset/NoGate/NoGateReset/KeyStep)
    "layer2gatedpolyseq",               // 183      Sequencer Type (0-1, Gated/Poly)
    "layer2track1destination",          // 184      Gated Seq1 Destination (0-52, *see table below)
    "layer2track2destination",          // 185      Gated Seq2 Destination (0-53, *see table below)
    "layer2track3destination",          // 186      Gated Seq3 Destination (0-52, *see table below)
    "layer2track4destination",          // 187      Gated Seq4 Destination (0-53, *see table below)
            
    "---",                              // 188
    "---",                              // 189
    "---",                              // 190
    "---",                              // 191
                    
    "layer2track1note1",                // 192      Gated Seq1 Step 1-16 (0-125/reset/rest)
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
    "layer2track1note16",               // 207
           
    "layer2track2note1",                // 208   Gated Seq2 Step 1-16 (0-125/reset/rest) 
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
    "layer2track2note16",               // 223
            
    "layer2track3note1",                // 224      Gated Seq3 Step 1-16 (0-125/reset/rest)  
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
    "layer2track3note16",               // 239
            
    "layer2track4note1",                // 240      Gated Seq4 Step 1-16 (0-125/reset/rest)    
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
    "layer2track4note16",               // 255
    
    "---",                              // 256      Name
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
    "---",                              // 275
            
               
    "layer2step1note1",                 // 276-339  Poly Seq Track1 Notes (bit0-7;note, bit8;tie)  
    "layer2step2note1",
    "layer2step3note1",
    "layer2step4note1",
    "layer2step5note1",
    "layer2step6note1",
    "layer2step7note1",
    "layer2step8note1",
    "layer2step9note1",
    "layer2step10note1",
    "layer2step11note1",
    "layer2step12note1",
    "layer2step13note1",
    "layer2step14note1",
    "layer2step15note1",
    "layer2step16note1",
    "layer2step17note1",
    "layer2step18note1",
    "layer2step19note1",
    "layer2step20note1",
    "layer2step21note1",
    "layer2step22note1",
    "layer2step23note1",
    "layer2step24note1",
    "layer2step25note1",
    "layer2step26note1",
    "layer2step27note1",
    "layer2step28note1",
    "layer2step29note1",
    "layer2step30note1",
    "layer2step31note1",
    "layer2step32note1",
    "layer2step33note1",
    "layer2step34note1",
    "layer2step35note1",
    "layer2step36note1",
    "layer2step37note1",
    "layer2step38note1",
    "layer2step39note1",
    "layer2step40note1",
    "layer2step41note1",
    "layer2step42note1",
    "layer2step43note1",
    "layer2step44note1",
    "layer2step45note1",
    "layer2step46note1",
    "layer2step47note1",
    "layer2step48note1",
    "layer2step49note1",
    "layer2step50note1",
    "layer2step51note1",
    "layer2step52note1",
    "layer2step53note1",
    "layer2step54note1",
    "layer2step55note1",
    "layer2step56note1",
    "layer2step57note1",
    "layer2step58note1",
    "layer2step59note1",
    "layer2step60note1",
    "layer2step61note1",
    "layer2step62note1",
    "layer2step63note1",
    "layer2step64note1",
            
    "layer2step1velocity1",             // 340-403  Poly Seq Track1 Velocities (bit0-7;velocity, bit8;rest)  
    "layer2step2velocity1",
    "layer2step3velocity1",
    "layer2step4velocity1",
    "layer2step5velocity1",
    "layer2step6velocity1",
    "layer2step7velocity1",
    "layer2step8velocity1",
    "layer2step9velocity1",
    "layer2step10velocity1",
    "layer2step11velocity1",
    "layer2step12velocity1",
    "layer2step13velocity1",
    "layer2step14velocity1",
    "layer2step15velocity1",
    "layer2step16velocity1",
    "layer2step17velocity1",
    "layer2step18velocity1",
    "layer2step19velocity1",
    "layer2step20velocity1",
    "layer2step21velocity1",
    "layer2step22velocity1",
    "layer2step23velocity1",
    "layer2step24velocity1",
    "layer2step25velocity1",
    "layer2step26velocity1",
    "layer2step27velocity1",
    "layer2step28velocity1",
    "layer2step29velocity1",
    "layer2step30velocity1",
    "layer2step31velocity1",
    "layer2step32velocity1",
    "layer2step33velocity1",
    "layer2step34velocity1",
    "layer2step35velocity1",
    "layer2step36velocity1",
    "layer2step37velocity1",
    "layer2step38velocity1",
    "layer2step39velocity1",
    "layer2step40velocity1",
    "layer2step41velocity1",
    "layer2step42velocity1",
    "layer2step43velocity1",
    "layer2step44velocity1",
    "layer2step45velocity1",
    "layer2step46velocity1",
    "layer2step47velocity1",
    "layer2step48velocity1",
    "layer2step49velocity1",
    "layer2step50velocity1",
    "layer2step51velocity1",
    "layer2step52velocity1",
    "layer2step53velocity1",
    "layer2step54velocity1",
    "layer2step55velocity1",
    "layer2step56velocity1",
    "layer2step57velocity1",
    "layer2step58velocity1",
    "layer2step59velocity1",
    "layer2step60velocity1",
    "layer2step61velocity1",
    "layer2step62velocity1",
    "layer2step63velocity1",
    "layer2step64velocity1",

    "layer2step1note2",                 // 404-467  Poly Seq Track2 Notes (bit0-7;note, bit8;tie)
    "layer2step2note2",
    "layer2step3note2",
    "layer2step4note2",
    "layer2step5note2",
    "layer2step6note2",
    "layer2step7note2",
    "layer2step8note2",
    "layer2step9note2",
    "layer2step10note2",
    "layer2step11note2",
    "layer2step12note2",
    "layer2step13note2",
    "layer2step14note2",
    "layer2step15note2",
    "layer2step16note2",
    "layer2step17note2",
    "layer2step18note2",
    "layer2step19note2",
    "layer2step20note2",
    "layer2step21note2",
    "layer2step22note2",
    "layer2step23note2",
    "layer2step24note2",
    "layer2step25note2",
    "layer2step26note2",
    "layer2step27note2",
    "layer2step28note2",
    "layer2step29note2",
    "layer2step30note2",
    "layer2step31note2",
    "layer2step32note2",
    "layer2step33note2",
    "layer2step34note2",
    "layer2step35note2",
    "layer2step36note2",
    "layer2step37note2",
    "layer2step38note2",
    "layer2step39note2",
    "layer2step40note2",
    "layer2step41note2",
    "layer2step42note2",
    "layer2step43note2",
    "layer2step44note2",
    "layer2step45note2",
    "layer2step46note2",
    "layer2step47note2",
    "layer2step48note2",
    "layer2step49note2",
    "layer2step50note2",
    "layer2step51note2",
    "layer2step52note2",
    "layer2step53note2",
    "layer2step54note2",
    "layer2step55note2",
    "layer2step56note2",
    "layer2step57note2",
    "layer2step58note2",
    "layer2step59note2",
    "layer2step60note2",
    "layer2step61note2",
    "layer2step62note2",
    "layer2step63note2",
    "layer2step64note2",
    
    "layer2step1velocity2",             // 468-531  Poly Seq Track2 Velocities (bit0-7;velocity, bit8;rest)
    "layer2step2velocity2",
    "layer2step3velocity2",
    "layer2step4velocity2",
    "layer2step5velocity2",
    "layer2step6velocity2",
    "layer2step7velocity2",
    "layer2step8velocity2",
    "layer2step9velocity2",
    "layer2step10velocity2",
    "layer2step11velocity2",
    "layer2step12velocity2",
    "layer2step13velocity2",
    "layer2step14velocity2",
    "layer2step15velocity2",
    "layer2step16velocity2",
    "layer2step17velocity2",
    "layer2step18velocity2",
    "layer2step19velocity2",
    "layer2step20velocity2",
    "layer2step21velocity2",
    "layer2step22velocity2",
    "layer2step23velocity2",
    "layer2step24velocity2",
    "layer2step25velocity2",
    "layer2step26velocity2",
    "layer2step27velocity2",
    "layer2step28velocity2",
    "layer2step29velocity2",
    "layer2step30velocity2",
    "layer2step31velocity2",
    "layer2step32velocity2",
    "layer2step33velocity2",
    "layer2step34velocity2",
    "layer2step35velocity2",
    "layer2step36velocity2",
    "layer2step37velocity2",
    "layer2step38velocity2",
    "layer2step39velocity2",
    "layer2step40velocity2",
    "layer2step41velocity2",
    "layer2step42velocity2",
    "layer2step43velocity2",
    "layer2step44velocity2",
    "layer2step45velocity2",
    "layer2step46velocity2",
    "layer2step47velocity2",
    "layer2step48velocity2",
    "layer2step49velocity2",
    "layer2step50velocity2",
    "layer2step51velocity2",
    "layer2step52velocity2",
    "layer2step53velocity2",
    "layer2step54velocity2",
    "layer2step55velocity2",
    "layer2step56velocity2",
    "layer2step57velocity2",
    "layer2step58velocity2",
    "layer2step59velocity2",
    "layer2step60velocity2",
    "layer2step61velocity2",
    "layer2step62velocity2",
    "layer2step63velocity2",
    "layer2step64velocity2",
    
    "layer2step1note3",                 // 532-595  Poly Seq Track3 Notes (bit0-7;note, bit8;tie)  
    "layer2step2note3",
    "layer2step3note3",
    "layer2step4note3",
    "layer2step5note3",
    "layer2step6note3",
    "layer2step7note3",
    "layer2step8note3",
    "layer2step9note3",
    "layer2step10note3",
    "layer2step11note3",
    "layer2step12note3",
    "layer2step13note3",
    "layer2step14note3",
    "layer2step15note3",
    "layer2step16note3",
    "layer2step17note3",
    "layer2step18note3",
    "layer2step19note3",
    "layer2step20note3",
    "layer2step21note3",
    "layer2step22note3",
    "layer2step23note3",
    "layer2step24note3",
    "layer2step25note3",
    "layer2step26note3",
    "layer2step27note3",
    "layer2step28note3",
    "layer2step29note3",
    "layer2step30note3",
    "layer2step31note3",
    "layer2step32note3",
    "layer2step33note3",
    "layer2step34note3",
    "layer2step35note3",
    "layer2step36note3",
    "layer2step37note3",
    "layer2step38note3",
    "layer2step39note3",
    "layer2step40note3",
    "layer2step41note3",
    "layer2step42note3",
    "layer2step43note3",
    "layer2step44note3",
    "layer2step45note3",
    "layer2step46note3",
    "layer2step47note3",
    "layer2step48note3",
    "layer2step49note3",
    "layer2step50note3",
    "layer2step51note3",
    "layer2step52note3",
    "layer2step53note3",
    "layer2step54note3",
    "layer2step55note3",
    "layer2step56note3",
    "layer2step57note3",
    "layer2step58note3",
    "layer2step59note3",
    "layer2step60note3",
    "layer2step61note3",
    "layer2step62note3",
    "layer2step63note3",
    "layer2step64note3",
    
    "layer2step1velocity3",             // 596-659  Poly Seq Track3 Velocities (bit0-7;velocity, bit8;rest)  
    "layer2step2velocity3",
    "layer2step3velocity3",
    "layer2step4velocity3",
    "layer2step5velocity3",
    "layer2step6velocity3",
    "layer2step7velocity3",
    "layer2step8velocity3",
    "layer2step9velocity3",
    "layer2step10velocity3",
    "layer2step11velocity3",
    "layer2step12velocity3",
    "layer2step13velocity3",
    "layer2step14velocity3",
    "layer2step15velocity3",
    "layer2step16velocity3",
    "layer2step17velocity3",
    "layer2step18velocity3",
    "layer2step19velocity3",
    "layer2step20velocity3",
    "layer2step21velocity3",
    "layer2step22velocity3",
    "layer2step23velocity3",
    "layer2step24velocity3",
    "layer2step25velocity3",
    "layer2step26velocity3",
    "layer2step27velocity3",
    "layer2step28velocity3",
    "layer2step29velocity3",
    "layer2step30velocity3",
    "layer2step31velocity3",
    "layer2step32velocity3",
    "layer2step33velocity3",
    "layer2step34velocity3",
    "layer2step35velocity3",
    "layer2step36velocity3",
    "layer2step37velocity3",
    "layer2step38velocity3",
    "layer2step39velocity3",
    "layer2step40velocity3",
    "layer2step41velocity3",
    "layer2step42velocity3",
    "layer2step43velocity3",
    "layer2step44velocity3",
    "layer2step45velocity3",
    "layer2step46velocity3",
    "layer2step47velocity3",
    "layer2step48velocity3",
    "layer2step49velocity3",
    "layer2step50velocity3",
    "layer2step51velocity3",
    "layer2step52velocity3",
    "layer2step53velocity3",
    "layer2step54velocity3",
    "layer2step55velocity3",
    "layer2step56velocity3",
    "layer2step57velocity3",
    "layer2step58velocity3",
    "layer2step59velocity3",
    "layer2step60velocity3",
    "layer2step61velocity3",
    "layer2step62velocity3",
    "layer2step63velocity3",
    "layer2step64velocity3",

    "layer2step1note4",                 // 660-723  Poly Seq Track4 Notes (bit0-7;note, bit8;tie)
    "layer2step2note4",
    "layer2step3note4",
    "layer2step4note4",
    "layer2step5note4",
    "layer2step6note4",
    "layer2step7note4",
    "layer2step8note4",
    "layer2step9note4",
    "layer2step10note4",
    "layer2step11note4",
    "layer2step12note4",
    "layer2step13note4",
    "layer2step14note4",
    "layer2step15note4",
    "layer2step16note4",
    "layer2step17note4",
    "layer2step18note4",
    "layer2step19note4",
    "layer2step20note4",
    "layer2step21note4",
    "layer2step22note4",
    "layer2step23note4",
    "layer2step24note4",
    "layer2step25note4",
    "layer2step26note4",
    "layer2step27note4",
    "layer2step28note4",
    "layer2step29note4",
    "layer2step30note4",
    "layer2step31note4",
    "layer2step32note4",
    "layer2step33note4",
    "layer2step34note4",
    "layer2step35note4",
    "layer2step36note4",
    "layer2step37note4",
    "layer2step38note4",
    "layer2step39note4",
    "layer2step40note4",
    "layer2step41note4",
    "layer2step42note4",
    "layer2step43note4",
    "layer2step44note4",
    "layer2step45note4",
    "layer2step46note4",
    "layer2step47note4",
    "layer2step48note4",
    "layer2step49note4",
    "layer2step50note4",
    "layer2step51note4",
    "layer2step52note4",
    "layer2step53note4",
    "layer2step54note4",
    "layer2step55note4",
    "layer2step56note4",
    "layer2step57note4",
    "layer2step58note4",
    "layer2step59note4",
    "layer2step60note4",
    "layer2step61note4",
    "layer2step62note4",
    "layer2step63note4",
    "layer2step64note4",
    
    "layer2step1velocity4",             // 724-787  Poly Seq Track4 Velocities (bit0-7;velocity, bit8;rest)  
    "layer2step2velocity4",
    "layer2step3velocity4",
    "layer2step4velocity4",
    "layer2step5velocity4",
    "layer2step6velocity4",
    "layer2step7velocity4",
    "layer2step8velocity4",
    "layer2step9velocity4",
    "layer2step10velocity4",
    "layer2step11velocity4",
    "layer2step12velocity4",
    "layer2step13velocity4",
    "layer2step14velocity4",
    "layer2step15velocity4",
    "layer2step16velocity4",
    "layer2step17velocity4",
    "layer2step18velocity4",
    "layer2step19velocity4",
    "layer2step20velocity4",
    "layer2step21velocity4",
    "layer2step22velocity4",
    "layer2step23velocity4",
    "layer2step24velocity4",
    "layer2step25velocity4",
    "layer2step26velocity4",
    "layer2step27velocity4",
    "layer2step28velocity4",
    "layer2step29velocity4",
    "layer2step30velocity4",
    "layer2step31velocity4",
    "layer2step32velocity4",
    "layer2step33velocity4",
    "layer2step34velocity4",
    "layer2step35velocity4",
    "layer2step36velocity4",
    "layer2step37velocity4",
    "layer2step38velocity4",
    "layer2step39velocity4",
    "layer2step40velocity4",
    "layer2step41velocity4",
    "layer2step42velocity4",
    "layer2step43velocity4",
    "layer2step44velocity4",
    "layer2step45velocity4",
    "layer2step46velocity4",
    "layer2step47velocity4",
    "layer2step48velocity4",
    "layer2step49velocity4",
    "layer2step50velocity4",
    "layer2step51velocity4",
    "layer2step52velocity4",
    "layer2step53velocity4",
    "layer2step54velocity4",
    "layer2step55velocity4",
    "layer2step56velocity4",
    "layer2step57velocity4",
    "layer2step58velocity4",
    "layer2step59velocity4",
    "layer2step60velocity4",
    "layer2step61velocity4",
    "layer2step62velocity4",
    "layer2step63velocity4",
    "layer2step64velocity4",
    
    "layer2step1note5",                 // 788-851  Poly Seq Track5 Notes (bit0-7;note, bit8;tie)
    "layer2step2note5",
    "layer2step3note5",
    "layer2step4note5",
    "layer2step5note5",
    "layer2step6note5",
    "layer2step7note5",
    "layer2step8note5",
    "layer2step9note5",
    "layer2step10note5",
    "layer2step11note5",
    "layer2step12note5",
    "layer2step13note5",
    "layer2step14note5",
    "layer2step15note5",
    "layer2step16note5",
    "layer2step17note5",
    "layer2step18note5",
    "layer2step19note5",
    "layer2step20note5",
    "layer2step21note5",
    "layer2step22note5",
    "layer2step23note5",
    "layer2step24note5",
    "layer2step25note5",
    "layer2step26note5",
    "layer2step27note5",
    "layer2step28note5",
    "layer2step29note5",
    "layer2step30note5",
    "layer2step31note5",
    "layer2step32note5",
    "layer2step33note5",
    "layer2step34note5",
    "layer2step35note5",
    "layer2step36note5",
    "layer2step37note5",
    "layer2step38note5",
    "layer2step39note5",
    "layer2step40note5",
    "layer2step41note5",
    "layer2step42note5",
    "layer2step43note5",
    "layer2step44note5",
    "layer2step45note5",
    "layer2step46note5",
    "layer2step47note5",
    "layer2step48note5",
    "layer2step49note5",
    "layer2step50note5",
    "layer2step51note5",
    "layer2step52note5",
    "layer2step53note5",
    "layer2step54note5",
    "layer2step55note5",
    "layer2step56note5",
    "layer2step57note5",
    "layer2step58note5",
    "layer2step59note5",
    "layer2step60note5",
    "layer2step61note5",
    "layer2step62note5",
    "layer2step63note5",
    "layer2step64note5",
    
    "layer2step1velocity5",             // 852-915  Poly Seq Track5 Velocities (bit0-7;velocity, bit8;rest)
    "layer2step2velocity5",
    "layer2step3velocity5",
    "layer2step4velocity5",
    "layer2step5velocity5",
    "layer2step6velocity5",
    "layer2step7velocity5",
    "layer2step8velocity5",
    "layer2step9velocity5",
    "layer2step10velocity5",
    "layer2step11velocity5",
    "layer2step12velocity5",
    "layer2step13velocity5",
    "layer2step14velocity5",
    "layer2step15velocity5",
    "layer2step16velocity5",
    "layer2step17velocity5",
    "layer2step18velocity5",
    "layer2step19velocity5",
    "layer2step20velocity5",
    "layer2step21velocity5",
    "layer2step22velocity5",
    "layer2step23velocity5",
    "layer2step24velocity5",
    "layer2step25velocity5",
    "layer2step26velocity5",
    "layer2step27velocity5",
    "layer2step28velocity5",
    "layer2step29velocity5",
    "layer2step30velocity5",
    "layer2step31velocity5",
    "layer2step32velocity5",
    "layer2step33velocity5",
    "layer2step34velocity5",
    "layer2step35velocity5",
    "layer2step36velocity5",
    "layer2step37velocity5",
    "layer2step38velocity5",
    "layer2step39velocity5",
    "layer2step40velocity5",
    "layer2step41velocity5",
    "layer2step42velocity5",
    "layer2step43velocity5",
    "layer2step44velocity5",
    "layer2step45velocity5",
    "layer2step46velocity5",
    "layer2step47velocity5",
    "layer2step48velocity5",
    "layer2step49velocity5",
    "layer2step50velocity5",
    "layer2step51velocity5",
    "layer2step52velocity5",
    "layer2step53velocity5",
    "layer2step54velocity5",
    "layer2step55velocity5",
    "layer2step56velocity5",
    "layer2step57velocity5",
    "layer2step58velocity5",
    "layer2step59velocity5",
    "layer2step60velocity5",
    "layer2step61velocity5",
    "layer2step62velocity5",
    "layer2step63velocity5",
    "layer2step64velocity5",

    "layer2step1note6",                 // 916-979  Poly Seq Track6 Notes (bit0-7;note, bit8;tie)  
    "layer2step2note6",
    "layer2step3note6",
    "layer2step4note6",
    "layer2step5note6",
    "layer2step6note6",
    "layer2step7note6",
    "layer2step8note6",
    "layer2step9note6",
    "layer2step10note6",
    "layer2step11note6",
    "layer2step12note6",
    "layer2step13note6",
    "layer2step14note6",
    "layer2step15note6",
    "layer2step16note6",
    "layer2step17note6",
    "layer2step18note6",
    "layer2step19note6",
    "layer2step20note6",
    "layer2step21note6",
    "layer2step22note6",
    "layer2step23note6",
    "layer2step24note6",
    "layer2step25note6",
    "layer2step26note6",
    "layer2step27note6",
    "layer2step28note6",
    "layer2step29note6",
    "layer2step30note6",
    "layer2step31note6",
    "layer2step32note6",
    "layer2step33note6",
    "layer2step34note6",
    "layer2step35note6",
    "layer2step36note6",
    "layer2step37note6",
    "layer2step38note6",
    "layer2step39note6",
    "layer2step40note6",
    "layer2step41note6",
    "layer2step42note6",
    "layer2step43note6",
    "layer2step44note6",
    "layer2step45note6",
    "layer2step46note6",
    "layer2step47note6",
    "layer2step48note6",
    "layer2step49note6",
    "layer2step50note6",
    "layer2step51note6",
    "layer2step52note6",
    "layer2step53note6",
    "layer2step54note6",
    "layer2step55note6",
    "layer2step56note6",
    "layer2step57note6",
    "layer2step58note6",
    "layer2step59note6",
    "layer2step60note6",
    "layer2step61note6",
    "layer2step62note6",
    "layer2step63note6",
    "layer2step64note6",
    
    "layer2step1velocity6",             // 980-1043 Poly Seq Track6 Velocities (bit0-7;velocity, bit8;rest)  
    "layer2step2velocity6",
    "layer2step3velocity6",
    "layer2step4velocity6",
    "layer2step5velocity6",
    "layer2step6velocity6",
    "layer2step7velocity6",
    "layer2step8velocity6",
    "layer2step9velocity6",
    "layer2step10velocity6",
    "layer2step11velocity6",
    "layer2step12velocity6",
    "layer2step13velocity6",
    "layer2step14velocity6",
    "layer2step15velocity6",
    "layer2step16velocity6",
    "layer2step17velocity6",
    "layer2step18velocity6",
    "layer2step19velocity6",
    "layer2step20velocity6",
    "layer2step21velocity6",
    "layer2step22velocity6",
    "layer2step23velocity6",
    "layer2step24velocity6",
    "layer2step25velocity6",
    "layer2step26velocity6",
    "layer2step27velocity6",
    "layer2step28velocity6",
    "layer2step29velocity6",
    "layer2step30velocity6",
    "layer2step31velocity6",
    "layer2step32velocity6",
    "layer2step33velocity6",
    "layer2step34velocity6",
    "layer2step35velocity6",
    "layer2step36velocity6",
    "layer2step37velocity6",
    "layer2step38velocity6",
    "layer2step39velocity6",
    "layer2step40velocity6",
    "layer2step41velocity6",
    "layer2step42velocity6",
    "layer2step43velocity6",
    "layer2step44velocity6",
    "layer2step45velocity6",
    "layer2step46velocity6",
    "layer2step47velocity6",
    "layer2step48velocity6",
    "layer2step49velocity6",
    "layer2step50velocity6",
    "layer2step51velocity6",
    "layer2step52velocity6",
    "layer2step53velocity6",
    "layer2step54velocity6",
    "layer2step55velocity6",
    "layer2step56velocity6",
    "layer2step57velocity6",
    "layer2step58velocity6",
    "layer2step59velocity6",
    "layer2step60velocity6",
    "layer2step61velocity6",
    "layer2step62velocity6",
    "layer2step63velocity6",
    "layer2step64velocity6"         
    };


    public byte[] emit(Model tempModel, boolean toWorkingMemory, boolean toFile) 
        {
        if (tempModel == null)
            tempModel = getModel();

        byte[] data = null;
        byte[] d = new byte[2046];
        for(int i = 0; i < 2046; i++)
            {
            int j = i;
            if (!parameters[i].equals("---"))
                {
                // Note: DSI isn't 2's complement.  So everything in the model is being stored starting at 0
                int q = model.get(parameters[i], 0);
                if (Pattern.matches("layer\\dstep\\d+velocity\\d", parameters[i]))
                    {
                    if (q > 0) q += 127;
                    }
                if (q > 127) q -= 256;  // push to signed (not 2's complement)
                d[j] = (byte)q;
                }
            }
        // handle name specially
        char[] name = (model.get("name", "Untitled") + "                    " ).toCharArray();
        for(int i = 0; i < 20; i++)
            d[235 + i] = (byte)(name[i] & 127);
        
        char[] name2 = (model.get("layer2name", "Untitled") + "                    " ).toCharArray();
        for(int i = 0; i < 20; i++)
            d[1259 + i] = (byte)(name2[i] & 127);
            
        data = convertTo7Bit(d); 
  
        if (toWorkingMemory)
            {
            byte[] emit = new byte[2344];
            emit[0] = (byte)0xF0;
            emit[1] = (byte)0x01;  // DSI
            emit[2] = PROPHET_REV2_ID;
            emit[3] = (byte)0x03;  // Edit Buffer Data Dump
            System.arraycopy(data, 0, emit, 4, data.length);
            emit[emit.length - 1] = (byte)0xF7;
            return emit;
            }
        else
            {
            byte[] emit = new byte[2346];
            emit[0] = (byte)0xF0;
            emit[1] = (byte)0x01;  // DSI
            emit[2] = PROPHET_REV2_ID;
            emit[3] = (byte)0x02;  // Program Data Dump
            emit[4] = (byte)tempModel.get("bank", 0);
            emit[5] = (byte)tempModel.get("number", 0);
            System.arraycopy(data, 0, emit, 6, data.length);
            emit[emit.length - 1] = (byte)0xF7;
            return emit;
            }
        }

    public byte[] requestDump(Model tempModel) 
        { 
        if (tempModel == null)
            tempModel = getModel();

        byte[] data = new byte[7];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x01;   // Sequential
        data[2] = PROPHET_REV2_ID;
        data[3] = (byte)0x05;
        data[4] = (byte)(tempModel.get("bank", 0));
        data[5] = (byte)(tempModel.get("number", 0));
        data[6] = (byte)0xF7;
        return data;
        }

    public byte[] requestCurrentDump()
        { 
        byte[] data = new byte[5];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x01;   // Sequential
        data[2] = (byte)0x2F;   // Rev2
        data[3] = (byte)0x06;
        data[4] = (byte)0xF7;                   
        return data;
        }

    
    public Object[] emitAll(String key)
        {
        if (key.equals("bank")) return new Object[0];  // this is not emittable
        if (key.equals("number")) return new Object[0];  // this is not emittable
        
        int index;
        int value;
        
        if (key.equals("name"))
            {
            Object[] ret = new Object[4 * 20];
            char[] name = (model.get("name", "Untitled") + "                    ").toCharArray();
            for(int i = 0; i < 20; i++)
                {
                Object[] nrpn = buildNRPN(getChannelOut(), i + 256, name[i]);
                System.arraycopy(nrpn, 0, ret, i * 4, 4);
                }
            return ret;
            }
        else
            {
            int val = model.get(key, 0);
            
            if (Pattern.matches("layer\\dstep\\d+velocity\\d", key))
                {
                if (val > 0) val += 127;
                }
                
            int nrpnkey = (Integer)(nrpnparametersToIndex.get(key));
            if (nrpnkey > 1043)
                nrpnkey += 1004;
                                
            return buildNRPN(getChannelOut(), ((Integer)(nrpnkey)).intValue(), val);
            }
        }

    public void handleSynthCCOrNRPN(Midi.CCData data)
        {
        if (data.type == Midi.CCDATA_TYPE_NRPN && data.number <= 3091)
            {
            if (data.number >= 256 && data.number <= 275)  // Name
                {
                char[] name = (model.get("name", "Untitled") + "                ").toCharArray();
                name[data.number - 256] = (char)(data.value);
                model.set("name", new String(name).substring(0, MAXIMUM_NAME_LENGTH));
                }
            else if (data.number >= 2304 && data.number <= 2323)  // Layer 2 Name
                {
                char[] name = (model.get("layer2name", "Untitled") + "                ").toCharArray();
                name[data.number - 2304] = (char)(data.value);
                model.set("layer2name", new String(name).substring(0, MAXIMUM_NAME_LENGTH));
                }
            else    
                {
                String key;
                if (data.number <= 1043) key = nrpnparameters[data.number];   // Layer 1
                else key = nrpnparameters[data.number - 1004];                           // Layer 2
                    
                if (key == "---")
                    return;
                else
                    if ((Pattern.matches("layer\\dstep\\d+note\\d", key)) && (data.value > 128)) data.value = 128;
                if (Pattern.matches("layer\\dstep\\d+velocity\\d", key))
                    {
                    data.value -= 127;
                    if (data.value < 0) data.value = 0;
                    }
                model.set(key, data.value);
                }
            }
        }
    
    public JFrame sprout()
        {
        JFrame frame = super.sprout();
        addProphetMenu();
                        
        return frame;
        }

    public void addProphetMenu()
        {
        JMenu menu = new JMenu("Prophet Rev2");
        menubar.add(menu);
        
        JMenuItem a2b = new JMenuItem("Copy A -> B");
        menu.add(a2b);
        a2b.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt)
                {
                undo.push(getModel());
                setSendMIDI(false);
                boolean currentPush = undo.getWillPush();
                undo.setWillPush(false);

                for(int i = 0; i < parameters.length; i++)
                    {
                    if (parameters[i].startsWith("layer1"))
                        {
                        model.set("layer2" + parameters[i].substring(6), model.get(parameters[i]));
                        }
                    }
                
                undo.setWillPush(currentPush);
                setSendMIDI(true);
                sendAllParameters();
                }
            });
        
        JMenuItem b2a = new JMenuItem("Copy A <- B");
        menu.add(b2a);
        b2a.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt)
                {
                undo.push(getModel());
                setSendMIDI(false);
                boolean currentPush = undo.getWillPush();
                undo.setWillPush(false);
    
                for(int i = 0; i < parameters.length; i++)
                    {
                    if (parameters[i].startsWith("layer2"))
                        {
                        model.set("layer1" + parameters[i].substring(6), model.get(parameters[i]));
                        }
                    }
                
                undo.setWillPush(currentPush);
                setSendMIDI(true);
                sendAllParameters();
                }
            });

        JMenuItem swap = new JMenuItem("Swap A <-> B");
        menu.add(swap);
        swap.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt)
                {
                undo.push(getModel());
                setSendMIDI(false);
                boolean currentPush = undo.getWillPush();
                undo.setWillPush(false);

                for(int i = 0; i < parameters.length; i++)
                    {
                    if (parameters[i].startsWith("layer2"))
                        {
                        int val2 = model.get(parameters[i]);
                        int val1 = model.get("layer1" + parameters[i].substring(6));
                        model.set("layer1" + parameters[i].substring(6), val2);
                        model.set(parameters[i], val1);
                        }
                    }
                
                undo.setWillPush(currentPush);
                setSendMIDI(true);
                sendAllParameters();
                }
            });
        
        menu.addSeparator();
        
        JRadioButtonMenuItem loadBoth = new JRadioButtonMenuItem("Load Both Layers");
        loadBoth.setSelected(true);
        menu.add(loadBoth);
        loadBoth.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt)
                {
                load = LOAD_BOTH;
                }
            });
        
        JRadioButtonMenuItem restrictA = new JRadioButtonMenuItem("Load Only A");
        menu.add(restrictA);
        restrictA.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt)
                {
                load = LOAD_A;
                }
            });
        
        JRadioButtonMenuItem restrictB = new JRadioButtonMenuItem("Load Only B");
        menu.add(restrictB);
        restrictB.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt)
                {
                load = LOAD_B;
                }
            });
        
        ButtonGroup group = new ButtonGroup();
        group.add(loadBoth);
        group.add(restrictA);
        group.add(restrictB);

        menu.addSeparator();
        JMenuItem prophet08 = new JMenuItem("To Prophet '08 / Mopho / Tetra");
        menu.add(prophet08);
        prophet08.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt)
                {
                toProphet08();
                }
            });     
        }

    public void convertToP8(Model model, int[] suboctave, int[] sequencer, int[] octaves, int[][] shapemod)
        {
        // For each layer...
        for(int layer = 1; layer <= 2; layer++)
            {
            for(int osc = 1; osc <= 2; osc++)
                {
                // Map Oscillator
                String key = "layer" + layer + "dco" + osc + "shape";
                int shape = (model.get(key));
                if (shape == 2) // SAW + TRI
                    {
                    model.set(key, 3);      // SAW + TRI
                    }
                else if (shape == 3)    // TRI
                    {
                    model.set(key, 2);      // TRI                          
                    }
                else if (shape == 4)    // PULSE
                    {
                    model.set(key, 4 + shapemod[layer - 1][osc - 1]);                       
                    }
                }
                        
            // Map Arpeggiator
            // NOTE: We're mapping to the proper Tetra/Mopho octave mapping.  This means that
            // octave mappings larger than 1 will not map properly on the P8.
            model.set("layer" + layer + "arpeggiatormode", 5 * octaves[layer - 1] + RP_ARPEGGIATOR_MODES[model.get("layer" + layer + "arpeggiatormode")]); 

            // Map Sequencer.  Note that this assumes that the name "layer1sequencer" etc.
            // has been changed or removed.
            model.set("layer" + layer + "sequencer", sequencer[layer- 1]);

            // Map Sub Octave.  The Prophet '08 doesn't have a sub-octave, but the Mopho/Tetra do
            model.set("layer" + layer + "tetrasuboscillator1level", suboctave[layer- 1]);

            // Map Slop.  It appears that Prophet '08 0...5 maps to the Rev2's 0...10.
            model.set("layer" + layer + "slop", Math.min(model.get("layer" + layer + "slop") / 2, 5));
                        
            // Map Unison Key Mode
            if (model.exists("layer" + layer + "unisonkeymode"))
                {
                model.set("layer" + layer + "unisonkeymode", 
                    RP_KEY_MODES[model.get("layer" + layer + "unisonkeymode")]);
                }
                        
            // Map Sequencer Trigger Mode
            if (model.exists("layer" + layer + "sequencertrigger"))
                {
                model.set("layer" + layer + "sequencertrigger", 
                    RP_SEQUENCER_TRIGGERS[model.get("layer" + layer + "sequencertrigger")]);
                }
                        
            // Map LFO shapes
            for(int l = 1; l <= 4; l++)
                {
                if (model.exists("layer" + layer + "lfo" + l + "shape"))
                    {
                    model.set("layer" + layer + "lfo" + l + "shape", 
                        RP_LFO_SHAPES[model.get("layer" + layer + "lfo" + l + "shape")]);
                    }
                }
                        
            // Map sources
            for(int m = 1; m <= 8; m++)
                {
                if (model.exists("layer" + layer + "mod" + m + "source"))
                    {
                    model.set("layer" + layer + "mod" + m + "source", 
                        RP_MODULATION_SOURCES[model.get("layer" + layer + "mod" + m + "source")]);
                    }
                }
                        
            // Map destinations
            for(int d = 0; d < DESTINATION_TAGS.length; d++)
                {
                if (model.exists("layer" + layer + DESTINATION_TAGS[d] + "destination"))
                    {
                    model.set("layer" + layer + DESTINATION_TAGS[d] + "destination", 
                        RP_MODULATION_DESTINATIONS[model.get("layer" + layer + DESTINATION_TAGS[d] + "destination")]);
                    }
                }
            }
        }

    public void toProphet08()
        {
        final DSIProphet08 synth = new DSIProphet08();
        if (tuple != null)
            synth.tuple = new Midi.Tuple(tuple, synth.buildInReceiver(), synth.buildKeyReceiver(), synth.buildKey2Receiver());
        synth.sprout();
        JFrame frame = ((JFrame)(SwingUtilities.getRoot(synth)));
        frame.setVisible(true);
        SwingUtilities.invokeLater(new Runnable()
            {
            public void run() 
                { 
                synth.setSendMIDI(false);
                synth.getUndo().setWillPush(false);

                for(int i = 0; i < DSIProphet08.parameters.length; i++)
                    {
                    String key = DSIProphet08.parameters[i];
                    if (model.exists(key))
                        {
                        if (model.isString(key))                 // can only happen with "name"
                            {
                            synth.getModel().set(key, model.get(key, ""));
                            }
                        else
                            {
                            synth.getModel().set(key, model.get(key, 0));
                            }
                        }
                    }

                // now fix up
                convertToP8(synth.getModel(), 
                    new int[] { model.get("layer1subosc"), model.get("layer2subosc") },
                    new int[] { model.get("layer1gatedpolyseq"), model.get("layer2gatedpolyseq") },
                    new int[] { model.get("layer1arpeggiatoroctaves"), model.get("layer2arpeggiatoroctaves") },
                    new int[][] 
                        {
                        { model.get("layer1dco1shapemod"), model.get("layer1dco2shapemod") },
                        { model.get("layer2dco1shapemod"), model.get("layer2dco2shapemod") },
                        });

                synth.getUndo().setWillPush(true);
                synth.setSendMIDI(true);
                }
            });
        };


    public boolean testVerify(Synth synth2, String key, Object obj1, Object obj2)
        {
        if (key.equals("layer2step63velocity6")) return true;            // Sequential bug
        else if (key.equals("layer2step64velocity6")) return true;    // Sequential bug
        else return false;
        }

    }








/*******
        CONVERSION TO/FROM PROPHET 08
        
        According to this posting:
        https://forum.sequential.com/index.php/topic,2712.msg29509.html#msg29509
        
        There are two major differences between the Prophet 08 and the Rev2
        1. Noise in the P08 is approximately twice the equivalent on the Rev2
        2. Clock synced LFOs are affected by Clock Divide on the Rev2.  
        Set Clock Divide to 'Quarter' to match the P'08.
        
        I believe there is one other major difference: slop.  But I do not know
        the mapping between slop on the Rev2 and slop on the Prophet '08.  This
        same URL states the following:
        
        "The values for each parameter are the same on both on the Rev2 and the 
        P'08, with the exception of the 'slop' parameter which is doubled on the 
        Rev2."
        
        This goes along with our testing in loading Prophet '08 patches into the
        Rev2 and then downloading them as Rev2 patches.  However I have been told
        by people online that a 5 on the Prophet '08 sounds the same as a 5 on
        the Rev2.        
        
*/
        
        
        


/*******
        SEQUENTIAL PROPHET REV2 SYSEX FORMAT
        
        Sequential never published the sysex patch format for the Rev2 (incredibly).  This is a huge omission
        for a company that normally prides itself in its MIDI documentation.  Go figure.
        
        At the following URL, the format was reverse engineered as follows
        https://forum.sequential.com/index.php/topic,2056.20.html



        PATCH DUMP SYSEX is described as in the manual, but the data is 2046 bytes as follows:
        
        LAYER A:
        0      OSC1 Freq (0-120)
        1      OSC1 Freq (0-120)
        2      OSC1 Finetune (0-100)
        3      OSC2 Finetune (0-100)
        4      OSC1 Shape (0-4, off/Saw/SawTri/Tri/Pul)
        5      OSC2 Shape (0-4, off/Saw/SawTri/Tri/Pul)
        6      OSC1 ShapeMod (0-99)
        7      OSC2 ShapeMod (0-99)
        8      OSC1 Glide (0-127)
        9      OSC2 Glide (0-127)
        10      OSC1 Key on/off (0-1, off/on)
        11      OSC2 Key on/off (0-1, off/on)
        12      OSC1 Note Reset (0-1, off/on)
        13      OSC2 Note Reset (0-1, off/on)
        14      OSC Mix (0-127)
        15      SubOct Level (0-127)
        16      Noise Level (0-127)
        17      Sync (0-1, off/on)
        18      Glide Mode (0-3, Fixed Rate/Fixed Rate A/Fixed Time/Fixed Time A)
        19      Glide on/off (0-1, off/on)
        20      Pitch Bend Range (0-12)
        21      OSC Slop (0-127)
        22      Cutoff Freq (0-164)
        23      Resonance (0-127)
        24      Filter Key Amount (0-127)
        25      Filter Audio Mod (0-127)
        26      Filter Poles (0-1, 2pole/4pole)
        27      VCA Level (0-127)
        28      Program Volume (0-127)
        29      Pan Spread (0-127)
        30      Env3 Destination (0-52)
        31      Env3 Repeat (0-1, off/on)
        32      EnvF Amount (0-254)
        33      EnvA Amount (0-127)
        34      Env3 Amount (0-254)
        35      EnvF Velocity (0-127)
        36      EnvA Velocity (0-127)
        37      Env3 Velocity (0-127)
        38      EnvF Delay (0-127)
        39      EnvA Delay (0-127)
        40      Env3 Delay (0-127)
        41      EnvF Attack (0-127)
        42      EnvA Attack (0-127)
        43      Env3 Attack (0-127)
        44      EnvF Decay (0-127)
        45      EnvA Decay (0-127)
        46      Env3 Decay (0-127)
        47      EnvF Sustain (0-127)
        48      EnvA Sustain (0-127)
        49      Env3 Sustain (0-127)
        50      EnvF Release (0-127)
        51      EnvA Release (0-127)
        52      Env3 Release (0-127)
        53      LFO1 Rate (0-150)
        54      LFO2 Rate (0-150)
        55      LFO3 Rate (0-150)
        56      LFO4 Rate (0-150)
        57      LFO1 Shape (0-5, off/Tri/Saw/RevSaw/Square/Random)
        58      LFO2 Shape (0-5, off/Tri/Saw/RevSaw/Square/Random)
        59      LFO3 Shape (0-5, off/Tri/Saw/RevSaw/Square/Random)
        60      LFO4 Shape (0-5, off/Tri/Saw/RevSaw/Square/Random)
        61      LFO1 Amount (0-127)
        62      LFO2 Amount (0-127)
        63      LFO3 Amount (0-127)
        64      LFO4 Amount (0-127)
        65      LFO1 Destination (0-52)
        66      LFO2 Destination (0-52)
        67      LFO3 Destination (0-52)
        68      LFO4 Destination (0-52)
        69      LFO1 ClkSync (0-1, 0ff/on)
        70      LFO2 ClkSync (0-1, 0ff/on)
        71      LFO3 ClkSync (0-1, 0ff/on)
        72      LFO4 ClkSync (0-1, 0ff/on)
        73      LFO1 KeySync (0-1, off/on)
        74      LFO2 KeySync (0-1, off/on)
        75      LFO3 KeySync (0-1, off/on)
        76      LFO4 KeySync (0-1, off/on)
        77      Mod1 Source (0-22)
        78      Mod2 Source (0-22)
        79      Mod3 Source (0-22)
        80      Mod4 Source (0-22)
        81      Mod5 Source (0-22)
        82      Mod6 Source (0-22)
        83      Mod7 Source (0-22)
        84      Mod8 Source (0-22)
        85      Mod1 Amount (0-254)
        86      Mod2 Amount (0-254)
        87      Mod3 Amount (0-254)
        88      Mod4 Amount (0-254)
        89      Mod5 Amount (0-254)
        90      Mod6 Amount (0-254)
        91      Mod7 Amount (0-254)
        92      Mod8 Amount (0-254)
        93      Mod1 Destination (0-52)
        94      Mod2 Destination (0-52)
        95      Mod3 Destination (0-52)
        96      Mod4 Destination (0-52)
        97      Mod5 Destination (0-52)
        98      Mod6 Destination (0-52)
        99      Mod7 Destination (0-52)
        100      Mod8 Destination (0-52)
        101      Mod Wheel Amount (0-254)
        102      Mod Wheel Destination (0-52)
        103      Pressure Mod Amount (0-254)
        104      Pressure Mod Destination (0-52)
        105      Breath Mod Amount (0-254)
        106      Breath Mod Destination (0-52)
        107      Velocity Mod Amount (0-254)
        108      Velocity Mod Destination (0-52)
        109      MIDI Foot Mod Amount (0-254)
        110      MIDI Foot Mod Destination (0-52)
        111      Gated Seq1 Destination (0-52)
        112      Gated Seq2 Destination (0-53)
        113      Gated Seq3 Destination (0-52)
        114      Gated Seq4 Destination (0-53)
        115      FX Select (0-13, Off/DM/DDS/BBD/Ch/PH/PL/PM/F1/F2/Rvb/RM/Dst/HPF)
        116      FX on/off (0-1, off/on)
        117      FX Mix (0-127)
        118      FX Parameter 1 (0-255)
        119      FX Parameter 2 (0-127)
        120      FX Clock Sync on/off (0-1, off/on)
        121      -
        122      Key Mode (0-5, Low/Hi/Last/LowR/HiR/LastR)
        123      Unison on/off (0-1, off/on)
        124      Unison Mode (0-15/Chord)
        125      -
        126      -
        127      -
        128      -
        129      -
        130      BPM (30-250)
        131      Divide (0-12, H/Q/8th/8H/8S/8T/16th/16H/16S/16T/32nd/32T/64T
        132      Arp Mode (0-4, Up/Down/Up+Down/Random/assign)
        133      Arp Range (0-2, 1Oct/2Oct/3Oct)
        134      Arp Repeats (0-3)
        135      Arp Relatch on/off (0-1, off/on)
        136      Arp on/off (0-1, off/on)
        137      -
        138      Sequencer Mode (0-4, Normal/NoReset/NoGate/NoGateReset/KeyStep)
        139      Sequencer Type (0-1, Gated/Poly)
        140-155   Gated Seq1 Step 1-16 (0-125/reset/rest)
        156-171   Gated Seq2 Step 1-16 (0-125/reset/rest)
        172-187   Gated Seq3 Step 1-16 (0-125/reset/rest)
        188-203   Gated Seq4 Step 1-16 (0-125/reset/rest)
        204      -
        205      -
        206      -
        207      -
        208      Unison Detune (0-16)
        209      Pan Mod Mode (0-1, Alternate/Fixed)
        210      -
        211      -
        212      -
        213      -
        214      -
        215      -
        216      -
        217      -
        218      -
        219      -
        220      -
        221      -
        222      -
        223      -
        224      -
        225      -
        226      -
        227      -
        228      -
        229      -
        230      -
        231      Layer Mode (0-2, LayerA/SplitAB/StackAB)
        232      Split Point (0-120)
        233      -
        234      -
        235-254   Layer A Name
        255      -
        256-319   Poly Seq Track1 Notes (bit0-7;note, bit8;tie)
        320-383   Poly Seq Track1 Velocities (bit0-7;velocity, bit8;rest)
        384-447   Poly Seq Track2 Notes (bit0-7;note, bit8;tie)
        448-511   Poly Seq Track2 Velocities (bit0-7;velocity, bit8;rest)
        512-575   Poly Seq Track3 Notes (bit0-7;note, bit8;tie)
        576-639   Poly Seq Track3 Velocities (bit0-7;velocity, bit8;rest)
        640-703   Poly Seq Track4 Notes (bit0-7;note, bit8;tie)
        704-767   Poly Seq Track4 Velocities (bit0-7;velocity, bit8;rest)
        768-831   Poly Seq Track5 Notes (bit0-7;note, bit8;tie)
        832-895   Poly Seq Track5 Velocities (bit0-7;velocity, bit8;rest)
        896-959   Poly Seq Track6 Notes (bit0-7;note, bit8;tie)
        960-1023   Poly Seq Track6 Velocities (bit0-7;velocity, bit8;rest)

        LAYER B:
        Same as above, just add 1024 to the index value.
        Last four bytes (2044-2047) does not exist in SysEx dump.





        GLOBAL SYSEX is *not* byte encoded but is just provided directly as it's all 0-127.
        It is:


        SYSEX BYTE POS  NRPN    NAME                    VALUES      DISPLAY_VALUES
        0               4097    "Master Coarse Tune"    0-24        -12 - +12
        1               4096    "Master Fine Tune"      0-100       -50 - +50
        2               4098    "MIDI Channel"          0-16        0=ALL,1-16
        3               4099    "MIDI Clock Mode"       0-4         0=OFF,1=MASTER,2=SLAVE,3=SLAVE THRU,4=SLAVE NO S/S
        4               4100    "MIDI Clock Cable"      0-1         0=MIDI,1=USB
        5               4101    "MIDI Param Send"       0-2         0=OFF,1=CC,2=NRPN
        6               4102    "MIDI Param Receive"    0-2         0=OFF,1=CC,2=NRPN
        7               4103    "MIDI Control Enable"   0-1         0=OFF,1=ON
        8               4104    "MIDI SysEx Cable"      0-1         0=MIDI,1=USB
        9               4105    "MIDI Out Select"       0-1         0=MIDI,1=USB
        10               --     "UNKNOWN"
        11               --     "UNKNOWN"
        12              4107    "Local Control"         0-1         0=OFF,1=ON
        13              4111    "Seq Pedal Mode"        0-3         0=NORMAL,1=TRIGGER,2=GATE,3=TRIG-GATE
        14              4109    "Pot Mode"              0-2         0=RELATIVE,1=PASSTHRU,2=JUMP
        15              4112    "Sustain Polarity"      0-1         0=NORMAL,1=REVERSE
        16              4116    "Alt. Tunings"          0-16        (see list in manual)
        17              4113    "Velocity Curve"        0-7         velocity curve 1,2,..8
        18              4114    "Pressure Curve"        0-3         pressure curve 1,2,..4
        19              4115    "Mono/Stereo"           0-1         0=stereo,1=mono
        20              4120    "Screen Saver"          0-1         0=OFF,1=ON
        21              4119    "Multi Mode"            0-1         0=OFF,1=ON
        22              4118    "MIDI Prog Enable"      0-1         0=OFF,1=ON
        23              4121    "Sustain/Arp"           0-1         0=ARP HOLD,1=SUSTAIN
        24              4122    "Foot Assign"           0-5         0=Breath CC2,1=Foot CC4,2=Exp CC11,3=Volume,4=LPF Full,5=LPF Half
        25               --     "UNKNOWN"
        26               --     "UNKNOWN"
        27               --     "UNKNOWN"
        28               --     "UNKNOWN"

******/


