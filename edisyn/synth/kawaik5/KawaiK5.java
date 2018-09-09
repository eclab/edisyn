/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.kawaik5;

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
   A patch editor for the Kawai K5/K5m.
        
   @author Sean Luke
*/

public class KawaiK5 extends Synth
    {
    /// Various collections of parameter names for pop-up menus
        
    public static final String[] BANKS = { "SIA", "SIB", "SIC", "SID", "SEA", "SEB", "SEC", "SED" };
    public static final String[] WHEEL_ASSIGNMENTS = new String[] { "Vibrato Depth", "Vibrato Speed" };
    public static final String[] DHG_SELECT = {"Live", "Die", "All"};
    public static final String[] ANGLES = {"-", "0", "+"};
    public static final String[] ENVELOPES = { "-", "1", "2", "3", "4" };
    public static final String[] SIMPLE_ENVELOPES = { "1", "2", "3", "4" };
    public static final String[] MAX_SEGMENTS = { "-", "1", "2", "3", "4", "5", "6" };
    public static final String[] MOD_DESTINATIONS = { "LFO", "Harmonics", "Cutoff", "Slope", "Off" };
    public static final String[] KEYS = new String[] { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
    public static final String[] LFO_SHAPES = { "Triangle", "Inv Triangle", "Square", "Inv Square", "Sawtooth", "Inv Sawtooth" };
    public static final String[] HARMONIC_CONSTRAINTS = { "All", "Odd", "Even", "First Third", "Second Third", "Third Third", "Octaves", "Fifths", "Major Thirds", "Minor Sevenths", "Major Seconds", "Major Sevenths", "Minor Seconds", "Minor Thirds", "Major Sixths" };
    public static final String[] HARMONIC_MOD_CONSTRAINTS = { "None", "1", "2", "3", "4" };
    public static final String[] DISPLAY_PRESETS = { "Sawtooth Wave", "Square Wave", "Triangle Wave", "All Off", "All On" };
    public static final char[] LEGAL_CHARACTERS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '-', ':', '/', '*', '?', '!', '#', '*', '(', ')', '\"', '+', '.', '=', ' '};

    // Harmonic Constraints
    public static final int ALL = 0;
    public static final int ODD = 1;
    public static final int EVEN = 2;
    public static final int FIRST_THIRD = 3;
    public static final int SECOND_THIRD = 4;
    public static final int THIRD_THIRD = 5;
    public static final int OCTAVE = 6;
    public static final int FIFTH = 7;
    public static final int MAJOR_THIRD = 8;
    public static final int MINOR_SEVENTH = 9;
    public static final int MAJOR_SECOND = 10; 
    public static final int MAJOR_SEVENTH = 11; 
    public static final int MINOR_SECOND = 12; 
    public static final int MINOR_THIRD = 13; 
    public static final int MAJOR_SIXTH = 14; 
    
    // Harmonic Mod Constraints
    public static final int SAWTOOTH = 0;
    public static final int SQUARE = 1;
    public static final int TRIANGLE = 2;
    public static final int ALL_OFF = 3;
    public static final int ALL_ON = 4;
    
    // Various harmonics
    public static final int[] OCTAVE_HARMONICS = { 1, 2, 4, 8, 16, 32, 64 };
    public static final int[] FIFTH_HARMONICS = { 3, 6, 12, 24, 48, 96 };               // -1 because we're in the 64..127 
    public static final int[] MAJOR_THIRD_HARMONICS = { 5, 10, 20, 40, 80 };
    public static final int[] MINOR_SEVENTH_HARMONICS = { 7, 14, 28, 56, 112 };
    public static final int[] MAJOR_SECOND_HARMONICS = { 9, 18, 36, 72 };
    public static final int[] MAJOR_SEVENTH_HARMONICS = { 15, 30, 60, 120 };
    public static final int[] MINOR_SECOND_HARMONICS = { 17, 34, 68 };
    public static final int[] MINOR_THIRD_HARMONICS = { 19, 38, 76 };
    public static final int[] MAJOR_SIXTH_HARMONICS = { 27, 54, 108 };
    
    
    public KawaiK5()
        {
        for(int i = 0; i < parameters.length; i++)
            {
            parametersToIndex.put(parameters[i], Integer.valueOf(paramNumbers[i]));
            }

        /// SOUND PANEL
                
        JComponent soundPanel = new SynthPanel(this);
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        hbox.addLast(addLFO(Style.COLOR_A()));
        vbox.add(hbox);
        vbox.add(addEqualizer(Style.COLOR_B()));

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Global", soundPanel);
        
        
        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        
        hbox = new HBox();
        hbox.add(addDDA(1, Style.COLOR_B()));
        hbox.addLast(addBasic(1, Style.COLOR_C()));
        vbox.add(hbox);
        vbox.add(addDDAEnvelope(1, Style.COLOR_B()));

        vbox.add(addFilter(1, Style.COLOR_A()));
        vbox.add(addFilterEnvelope(1, Style.COLOR_A()));


        vbox.add(addPitch(1, Style.COLOR_C()));
        vbox.add(addPitchEnvelope(1, Style.COLOR_C()));

        soundPanel.add(vbox, BorderLayout.CENTER);
        ((SynthPanel)soundPanel).makePasteable("s1");
        addTab("General 1", (SynthPanel)soundPanel);
                
        
        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        
        hbox = new HBox();
        hbox.add(addDDA(2, Style.COLOR_B()));
        hbox.addLast(addBasic(2, Style.COLOR_C()));
        vbox.add(hbox);
        vbox.add(addDDAEnvelope(2, Style.COLOR_B()));

        vbox.add(addFilter(2, Style.COLOR_A()));
        vbox.add(addFilterEnvelope(2, Style.COLOR_A()));


        vbox.add(addPitch(2, Style.COLOR_C()));
        vbox.add(addPitchEnvelope(2, Style.COLOR_C()));

        soundPanel.add(vbox, BorderLayout.CENTER);
        ((SynthPanel)soundPanel).makePasteable("s2");
        addTab("General 2", (SynthPanel)soundPanel);
                
        
               
        harmonicsSources[0] = addDHGDisplay(0, Style.COLOR_B());
        harmonicsSources[1] = addDHGDisplay(0, Style.COLOR_B());
        harmonicsSources[2] = addDHGDisplay(1, Style.COLOR_B());
        harmonicsSources[3] = addDHGDisplay(2, Style.COLOR_B());

        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        Category dhg1 = (Category)addDHG(1, Style.COLOR_A());
        vbox.add(dhg1);
        hbox = new HBox();
        JComponent dhg2 = addDHG2(1, Style.COLOR_A());
        dhg1.setAuxillary((Gatherable)dhg2);
        hbox.add(dhg2);
        harmonics[0] = new HBox();
        harmonics[0].addLast(harmonicsSources[0]);
        hbox.addLast(harmonics[0]);
        vbox.add(hbox);
        
        soundPanel.add(vbox, BorderLayout.CENTER);
        ((SynthPanel)soundPanel).makePasteable("s1");
        addTab("Harmonics 1", (SynthPanel)soundPanel);
                
        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        dhg1 = (Category)addDHG(2, Style.COLOR_A());
        vbox.add(dhg1);
        hbox = new HBox();
        dhg2 = addDHG2(2, Style.COLOR_A());
        dhg1.setAuxillary((Gatherable)dhg2);
        hbox.add(dhg2);
        harmonics[1] = new HBox();
        harmonics[1].addLast(harmonicsSources[1]);
        hbox.addLast(harmonics[1]);
        vbox.add(hbox);
        
        soundPanel.add(vbox, BorderLayout.CENTER);
        ((SynthPanel)soundPanel).makePasteable("s2");
        addTab("Harmonics 2", (SynthPanel)soundPanel);
                

        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        hbox = new HBox();
        hbox.add(addDHGEnvelopeGlobal(1, Style.COLOR_A()));
        hbox.addLast(addKSCurve(1, Style.COLOR_C()));
        vbox.add(hbox);
        vbox.add(addDHGEnvelope(1, 1, Style.COLOR_B()));
        vbox.add(addDHGEnvelope(1, 2, Style.COLOR_A()));
        vbox.add(addDHGEnvelope(1, 3, Style.COLOR_B()));
        vbox.add(addDHGEnvelope(1, 4, Style.COLOR_A()));
        
        soundPanel.add(vbox, BorderLayout.CENTER);
        ((SynthPanel)soundPanel).makePasteable("s1");
        addTab("Envelopes/KS 1", (SynthPanel)soundPanel);
                

        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        hbox = new HBox();
        hbox.add(addDHGEnvelopeGlobal(2, Style.COLOR_A()));
        hbox.addLast(addKSCurve(2, Style.COLOR_C()));
        vbox.add(hbox);
        vbox.add(addDHGEnvelope(2, 1, Style.COLOR_B()));
        vbox.add(addDHGEnvelope(2, 2, Style.COLOR_A()));
        vbox.add(addDHGEnvelope(2, 3, Style.COLOR_B()));
        vbox.add(addDHGEnvelope(2, 4, Style.COLOR_A()));

        soundPanel.add(vbox, BorderLayout.CENTER);
        ((SynthPanel)soundPanel).makePasteable("s2");
        addTab("Envelopes/KS 2", (SynthPanel)soundPanel);

        model.set("name", "INIT");  // has to be 10 long

        model.set("bank", 0);
        model.set("number", 0);
        
        loadDefaults();        
        }
                
                
    public JFrame sprout()
        {
        JFrame frame = super.sprout();
        // We can't request the current working memory (don't ask why)
        receiveCurrent.setEnabled(false);

        // We can't reasonably send to patches if we send in bulk.
        transmitTo.setEnabled(false);

        return frame;
        }         

    public String getDefaultResourceFileName() { return "KawaiK5.init"; }
    public String getHTMLResourceFileName() { return "KawaiK5.html"; }

    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        JComboBox bank = new JComboBox(BANKS);
        bank.setSelectedIndex(model.get("bank"));
        
        JTextField number = new JTextField("" + (model.get("number") + 1), 3);

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
                showSimpleError(title, "The Patch Number must be an integer 1...12");
                continue;
                }
            if (n < 1 || n > 12)
                {
                showSimpleError(title, "The Patch Number must be an integer 1...12");
                continue;
                }
                
            n--;
                                
            int i = bank.getSelectedIndex();
                        
            change.set("bank", i);
            change.set("number", n);
                        
            return true;
            }
        }
        
    HBox[] harmonics = new HBox[2];
    JComponent[] harmonicsSources = new JComponent[4];
        
        
    /** Add the global patch category (name, id, number, etc.) */
    public JComponent addNameGlobal(Color color)
        {
        Category globalCategory = new Category(this, getSynthName(), color);
        globalCategory.makeUnresettable();
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
                
        VBox vbox = new VBox();
        HBox hbox2 = new HBox();
        comp = new PatchDisplay(this, 9);
        hbox2.add(comp);
        vbox.add(hbox2);
        
        comp = new StringComponent("Patch Name", this, "name", MAXIMUM_NAME_LENGTH, "Name must be up to " + MAXIMUM_NAME_LENGTH + " ASCII characters.")
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
        vbox.addBottom(comp);  // doesn't work right :-(
        hbox.add(vbox);

        // FIXME: The MIDI Spec also has "pic mode" (s1, s2, or both), but I believe this
        // is just the edit mode.  We wouldn't be doing that, but we need to store it perhaps.
        model.set("picmode", 0);           // s1
        model.setMin("picmode", 0);
        model.setMax("picmode", 2);
        model.setStatus("picmode", Model.STATUS_IMMUTABLE); 

        vbox = new VBox();
        comp = new CheckBox("Full Mode", this, "mode")
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                // only do this if they've already been established
                if (harmonics[0] != null)
                    {
                    harmonics[0].removeLast();
                    harmonics[0].addLast(model.get(key, 0) == 1 ? harmonicsSources[0] : harmonicsSources[2]); 
                    harmonics[0].revalidate();
                    harmonics[0].repaint();
                    harmonics[1].removeLast();
                    harmonics[1].addLast(model.get(key, 0) == 1 ? harmonicsSources[1] : harmonicsSources[3]); 
                    harmonics[1].revalidate();
                    harmonics[1].repaint();
                    }
                }
            };
        model.set("mode", 1);
        vbox.add(comp); 
        hbox.add(vbox);   
        
        comp = new CheckBox("Portamento", this, "portamentosw", false);
        vbox.add(comp); 
        hbox.add(vbox);   

        comp = new LabelledDial("Portamento", this, "portamentospeed", color, 0, 63);
        ((LabelledDial)comp).addAdditionalLabel("Speed");
        hbox.add(comp);

        comp = new LabelledDial("Volume", this, "volume", color, 0, 63);
        hbox.add(comp);

        comp = new LabelledDial("Balance", this, "balance", color, -31, 31);
        hbox.add(comp);


        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }
        
    public JComponent addBasic(int source, Color color)
        {
        Category category = new Category(this, "Basic Edit", color);
        category.makePasteable("s" + source + "basic");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = MOD_DESTINATIONS;
        comp = new Chooser("Pedal Destination", this, "s" + source + "basic" + "pedalassign", params);
        vbox.add(comp);

        params = MOD_DESTINATIONS;
        comp = new Chooser("Mod Wheel", this, "s" + source + "basic" + "wheelassign", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Delay", this, "s" + source + "basic" + "delay", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Pedal", this, "s" + source + "basic" + "pedaldep", color, -31, 31);
        ((LabelledDial)comp).addAdditionalLabel("Depth");
        hbox.add(comp);

        comp = new LabelledDial("Mod Wheel", this, "s" + source + "basic" + "wheeldep", color, -31, 31);
        ((LabelledDial)comp).addAdditionalLabel("Depth");
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addKSCurve(int source, Color color)
        {
        Category category = new Category(this, "Key Scaling Curve", color);
        category.makePasteable("s" + source + "ks");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        comp = new LabelledDial("Breakpoint", this, "s" + source + "ks" + "breakpoint", color, 0, 127)
            {
            public String map(int val)
                {
                return KEYS[val % 12] + (val / 12 + 1);  // note integer division
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Left", this, "s" + source + "ks" + "left", color, -31, 31);
        hbox.add(comp);

        comp = new LabelledDial("Right", this, "s" + source + "ks" + "right", color, -31, 31);
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addPitch(int source, Color color)
        {
        Category category = new Category(this, "Pitch (DFG)", color);
        category.makePasteable("s" + source + "dfg");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        comp = new CheckBox("Fixed", this, "s" + source + "dfg" + "key", false);
        vbox.add(comp); 
        hbox.add(vbox);   

        comp = new LabelledDial("Fixed Key", this, "s" + source + "dfg" + "fixno", color, 0, 127)
            {
            public String map(int val)
                {
                return KEYS[val % 12] + (val / 12 + 1);  // note integer division
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Coarse", this, "s" + source + "dfg" + "coarse", color, -48, 48);
        ((LabelledDial)comp).addAdditionalLabel("Tune");
        hbox.add(comp);

        comp = new LabelledDial("Fine", this, "s" + source + "dfg" + "fine", color, -31, 31);
        ((LabelledDial)comp).addAdditionalLabel("Tune");
        hbox.add(comp);

        comp = new LabelledDial("Pitch Bend", this, "s" + source + "dfg" + "benderdep", color, 0, 24);
        hbox.add(comp);

        comp = new LabelledDial("Aftertouch", this, "s" + source + "dfg" + "prsdep", color, -31, 31);
        ((LabelledDial)comp).addAdditionalLabel("Mod");
        hbox.add(comp);
                
        comp = new LabelledDial("LFO", this, "s" + source + "dfg" + "lfodep", color, 0, 31);
        ((LabelledDial)comp).addAdditionalLabel("Mod");
        hbox.add(comp);

        comp = new LabelledDial("Envelope", this, "s" + source + "dfg" + "envdep", color, -24, 24);
        ((LabelledDial)comp).addAdditionalLabel("Mod");
        hbox.add(comp);

        comp = new LabelledDial("Aftertouch", this, "s" + source + "dfg" + "preslfodep", color, -31, 31);
        ((LabelledDial)comp).addAdditionalLabel("LFO Mod");
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addPitchEnvelope(int source, Color color)
        {
        Category category = new Category(this, "Pitch (DFG) Envelope", color);
        category.makePasteable("s" + source + "dfg");
        category.makeDistributable("s" + source + "dfg");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        comp = new CheckBox("Loop", this, "s" + source + "dfg" + "envloop", false);
        ((CheckBox)comp).addToWidth(2);
        vbox.add(comp); 
        hbox.add(vbox);   

        comp = new LabelledDial("Velocity", this, "s" + source + "dfg" + "veloenvdep", color, -31, 31);
        ((LabelledDial)comp).addAdditionalLabel("Mod");
        hbox.add(comp);

        comp = new LabelledDial("Rate 1", this, "s" + source + "dfg" + "envrateseg1", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Level 1", this, "s" + source + "dfg" + "envlevelseg1", color, -31, 31);
        hbox.add(comp);

        comp = new LabelledDial("Rate 2", this, "s" + source + "dfg" + "envrateseg2", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Level 2", this, "s" + source + "dfg" + "envlevelseg2", color, -31, 31);
        hbox.add(comp);

        comp = new LabelledDial("Rate 3", this, "s" + source + "dfg" + "envrateseg3", color, 0, 31);
        ((LabelledDial)comp).addAdditionalLabel("(Loop Start) ");
        hbox.add(comp);

        comp = new LabelledDial("Level 3", this, "s" + source + "dfg" + "envlevelseg3", color, -31, 31);
        hbox.add(comp);

        comp = new LabelledDial("Rate 4", this, "s" + source + "dfg" + "envrateseg4", color, 0, 31);
        ((LabelledDial)comp).addAdditionalLabel("(Loop End) ");
        hbox.add(comp);

        comp = new LabelledDial("Level 4", this, "s" + source + "dfg" + "envlevelseg4", color, -31, 31);
        ((LabelledDial)comp).addAdditionalLabel("(Sustain)");
        hbox.add(comp);

        comp = new LabelledDial("Rate 5", this, "s" + source + "dfg" + "envrateseg5", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Level 5", this, "s" + source + "dfg" + "envlevelseg5", color, -31, 31);
        hbox.add(comp);

        comp = new LabelledDial("Rate 6", this, "s" + source + "dfg" + "envrateseg6", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Level 6", this, "s" + source + "dfg" + "envlevelseg6", color, -31, 31);
        hbox.add(comp);

        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null,  
                           "s" + source + "dfg" + "envrateseg1",
                           "s" + source + "dfg" + "envrateseg2",
                           "s" + source + "dfg" + "envrateseg3",
                           "s" + source + "dfg" + "envrateseg4",
                           "s" + source + "dfg" + "envrateseg5",
                           "s" + source + "dfg" + "envrateseg6" },
            new String[] { null,  
                           "s" + source + "dfg" + "envlevelseg1",
                           "s" + source + "dfg" + "envlevelseg2",
                           "s" + source + "dfg" + "envlevelseg3",
                           "s" + source + "dfg" + "envlevelseg4",
                           "s" + source + "dfg" + "envlevelseg5",
                           "s" + source + "dfg" + "envlevelseg6" },
            new double[] { 0, .1666666666 / 31, .1666666666 / 31, .1666666666 / 31, .1666666666 / 31, .1666666666 / 31, .1666666666 / 31 },
            new double[] { 0, 1.0 / 63, 1.0 / 63, 1.0 / 63, 1.0 / 63, 1.0 / 63, 1.0 / 63 });
                
        ((EnvelopeDisplay)comp).setYOffset(0.5);        
        ((EnvelopeDisplay)comp).setAxis(0.5);   
        hbox.addLast(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addLFO( Color color)
        {
        Category category = new Category(this, "LFO", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = LFO_SHAPES;
        comp = new Chooser("Shape", this, "lfoshape", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Speed", this, "lfospeed", color,  0, 99);
        hbox.add(comp);

        comp = new LabelledDial("Delay", this, "lfodelay", color, 0, 31);
        hbox.add(comp);
                
        comp = new LabelledDial("Trend", this, "lfotrend", color, 0, 31);
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public LabelledDial[] dials = new LabelledDial[126];

    public JComponent addDHG(int source, Color color)
        {
        Category category = new Category(this, "Harmonics (DHG)", color);
        category.makePasteable("s" + source + "dhg");
        category.makeDistributable("s" + source + "dhg");
        category.setSendsAllParameters(true);                           // otherwise, it's painfully slow
        
        JComponent comp;
        String[] params;
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        
        for(int j = 0; j < 3; j++)
            {
            hbox = new HBox();
            for(int i = 0; i < 16; i++)
                {
                VBox vbox2 = new VBox();
                
                //// IMPORTANT NOTE.  The K5m has TWO parameters for the envelope: (1) whether the envelope modulates the harmonic, and 
                //// (2) what envelope it is.  This is foolish, they should have just had one single parameter of five values,
                //// OFF, 1, 2, 3, or 4.  This matters because in order to save space in the harmonics window I have elected to do the
                //// non-foolish thing with the chooser below.  This impacts on emitting and parsing, because if you compress these
                //// two parameters into one, when the envelope is NOT modulating the harmonic, what envelope is doing the, erm,
                //// non-modulating?  We don't have that information any more.  So what I'm doing is as follows.  When we parse a patch,
                //// we'll read in the two parameters and compress them to a single stored parameter called, say, s2dhgharm16envselmodyn.
                //// Additionally we'll store the envelope parameter as s2dhgharm16envselmodyn-env.  When we need to emit the parameter
                //// or dump the patch, if the mod is ON, then we just write the chosen envelope.  But if the mod is OFF, then we write
                //// out the envelope in s2dhgharm16envselmodyn-env.  Note that while s2dhgharm16envselmodyn goes 0:OFF, 1:1, 2:2, 3:3, 4:4,
                //// the s2dhgharm16envselmodyn-env parameter goes 0:1, 1:2, 2:3, 3:4.

                params = ENVELOPES;
                comp = new Chooser("" + (j * 16 + i + 1) + " Env", this, "s" + source + "dhg" + "harm" + (j * 16 + i + 1) + "envselmodyn", params);
                vbox2.add(comp);  
                
                model.set("s" + source + "dhg" + "harm" + (j * 16 + i + 1) + "envselmodyn-env", 0);    // this will be our hidden env parameter (see emitting/parsing later)
                model.setStatus("s" + source + "dhg" + "harm" + (j * 16 + i + 1) + "envselmodyn-env", model.STATUS_IMMUTABLE);
                
                comp = new LabelledDial("" + (j * 16 + i + 1) + " Level", this, "s" + source + "dhg" + "harm" + (j * 16 + i + 1) + "level", color, 0, 99);
                dials[(source - 1) * 63 + (j * 16 + i)] = (LabelledDial) comp;
                vbox2.add(comp);
                hbox.add(vbox2);
                }
            vbox.add(hbox);
            //if (j < 3)
            vbox.add(Strut.makeVerticalStrut(20));
            }

        category.add(vbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addDHG2(int source, Color color)
        {
        JComponent comp;
        String[] params;
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        
        for(int j = 0; j < 2; j++)
            {
            hbox = new HBox();
            for(int i = 0; i < 8; i++)
                {
                if (j == 1 && i == 7) continue;
                        
                VBox vbox2 = new VBox();
                
                
                ///// See IMPORTANT NOTE above.
                
                params = ENVELOPES;
                comp = new Chooser("" + (48 + j * 8 + i + 1) + " Env", this, "s" + source + "dhg" + "harm" + (48 + j * 8 + i + 1) + "envselmodyn", params);
                vbox2.add(comp);  
                
                model.set("s" + source + "dhg" + "harm" + (48 + j * 8 + i + 1) + "envselmodyn-env", 0);    // this will be our hidden env parameter (see emitting/parsing later)
                model.setStatus("s" + source + "dhg" + "harm" + (48 + j * 8 + i + 1) + "envselmodyn-env", model.STATUS_IMMUTABLE);
                                
                comp = new LabelledDial("" + (48 + j * 8 + i + 1) + " Level", this, "s" + source + "dhg" + "harm" + (48 + j * 8 + i + 1) + "level", color, 0, 99);
                dials[(source - 1) * 63 + (48 + j * 8 + i)] = (LabelledDial) comp;
                vbox2.add(comp);
                hbox.add(vbox2);
                }
            vbox.add(hbox);
            //if (j < 3)
            vbox.add(Strut.makeVerticalStrut(20));
            }

        comp = new CheckBox("Mod", this, "s" + source + "dhg" + "modonoff", false);
        hbox.addLast(comp);
                
        return vbox;
        }
 
 
    int lastIndex = -1;
 
    void setHarmonic(int index, double val)
        {
        if (index >= 0 && index < 63)
            {
            model.set("s1dhgharm" + (index + 1) + "level", (int)(val * 99));
            }
        else if (index > 63 && index < 127)
            {
            model.set("s2dhgharm" + (index + 1 - 64) + "level", (int)(val * 99));
            }
        }
                
    // source can be "0", meaning "both"
 
    public JComponent addDHGDisplay(final int source, Color color)
        {
        final Category category = new Category(this, "Display", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox main = new VBox();
        
        params = HARMONIC_CONSTRAINTS;
        comp = new Chooser("Constrain Harmonics: ", this, "constrainharmonics", params)
            {
            public void update(String key, Model model) { super.update(key, model); category.repaint(); }

            public boolean isLabelToLeft() { return true; }
            };
        hbox.add(comp);

        params = HARMONIC_MOD_CONSTRAINTS;
        comp = new Chooser("Constrain Env: ", this, "constrainmodharmonics", params)
            {
            public boolean isLabelToLeft() { return true; }
            };
        hbox.add(comp);

        VBox vbox = new VBox(VBox.TOP_CONSUMES);
        params = DISPLAY_PRESETS;
        comp = new PushButton("Presets", DISPLAY_PRESETS)
            {
            public void perform(int i)
                {
                // we'll avoid individual updates because they're so slow.  Instead we'll
                // do a bulk update at the end
                boolean currentMIDI = getSendMIDI();
                setSendMIDI(false);

                int start = 0;
                int end = 127;
                if (getModel().get("mode", 0) == 0)
                    {
                    if (source == 1)
                        { start = 0; end = 63; }
                    else
                        { start = 64; end = 127; }
                    }
                
                int initial = 0;
                switch (i)
                    {
                    case SAWTOOTH:
                        {
                        for(int h = start; h < end; h++)
                            {
                            setHarmonic(h, 1.0 / (initial + 1));
                            initial++;
                            }
                        }
                    break;
                    case SQUARE:
                        {
                        for(int h = start + 1; h < end; h += 2)
                            {
                            setHarmonic(h, 0);
                            }

                        for(int h = start; h < end; h += 2)
                            {
                            setHarmonic(h, 1.0 / (initial + 1));
                            initial++;
                            }
                        }
                    break;
                    // This assumes all positive harmonics of course, which 
                    // is wrong but since humans can't hear phase differences...
                    case TRIANGLE:
                        {
                        for(int h = start + 1; h < end; h += 2)
                            {
                            setHarmonic(h, 0);
                            }

                        for(int h = start; h < end; h += 2)
                            {
                            setHarmonic(h, 1.0 / ((initial + 1) * (initial + 1)));
                            initial++;
                            }
                        }
                    break;
                    /*
                      case PSEUDO_TRIANGLE:
                      {
                      for(int h = start; h < end; h++)
                      {
                      setHarmonic(h, 0);
                      }

                      for(int h = start; h < end; h += 4)
                      {
                      setHarmonic(h, 1.0 / ((initial + 1) * (initial + 1)));
                      initial++;
                      }
                      }
                      break;
                    */
                    case ALL_OFF:
                        {
                        for(int h = start; h < end; h++)
                            {
                            setHarmonic(h, 0.0);
                            }
                        }
                    break;
                    case ALL_ON:
                        {
                        for(int h = start; h < end; h++)
                            {
                            setHarmonic(h, 1.0);
                            }
                        }
                    break;
                    }
                    
                setSendMIDI(currentMIDI);
                sendAllParameters();
                }
            };
        vbox.add(comp);
        vbox.addLast(Stretch.makeVerticalStretch());
        hbox.add(vbox);        

        main.add(hbox);
        
        hbox = new HBox();
        
        if (source == 0)
            {
            VBox vbox2 = new VBox();
            String[] levels = new String[126];
            String[] mods = new String[126];
            for(int i = 0; i < levels.length; i++)
                {
                if (i < 63)
                    {
                    levels[i] = "s" + 1 + "dhgharm" + (i + 1) + "level";
                    mods[i] = "s" + 1 + "dhgharm" + (i + 1) + "envselmodyn";
                    }
                else
                    {
                    levels[i] = "s" + 2 + "dhgharm" + (i + 1 - 63) + "level";
                    mods[i] = "s" + 2 + "dhgharm" + (i + 1 - 63) + "envselmodyn";
                    }
                }

            double[] widths = new double[126];
            for(int i = 1; i < widths.length; i++)
                widths[i] = 1.0 / (126 - 1);

            double[] heights = new double[126];
            for(int i = 0; i < heights.length; i++)
                heights[i] = 1.0 / 99;

            double[] heights2 = new double[126];
            for(int i = 0; i < heights2.length; i++)
                heights2[i] = 1.0 / 4;

            comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), new String[63 * 2], mods, widths, heights2)
                {
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
                    if (x < 0)
                        x = 0;
                    else if (x > 1)
                        x = 1.0;

                    if (y <= 0.0) y = 0.0;
                    if (y >= 1.0) y = 1.0;
                    int harmonic = (int)(x * (126.0 - 1) + 0.5);

                    if (!constrainTo(harmonic))
                        return;
                                        
                    double val = y * 4.0;
                                
                    int modconstraint = KawaiK5.this.model.get("constrainmodharmonics", 0);

                    if (modconstraint > 0)
                        {
                        if (((int)val) != 0) 
                            val = modconstraint; 
                        }
                                        
                    if (harmonic < 63)
                        KawaiK5.this.model.set("s" + 1 + "dhgharm" + (harmonic + 1) + "envselmodyn", (int)val);
                    else
                        KawaiK5.this.model.set("s" + 2 + "dhgharm" + (harmonic + 1 - 63) + "envselmodyn", (int)val);
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
                    return (int)(x * (126.0 - 1) + 0.5);
                    }

                public boolean constrainTo(int index) { return _constrainTo(index); }

                public int verticalBorderThickness() { return 4; }
                };
            ((EnvelopeDisplay)comp).addVerticalDivider(0.5);
            ((EnvelopeDisplay)comp).setPreferredHeight(40);
            vbox2.add(comp);

            comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), new String[63 * 2], levels, widths, heights)
                {
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
                    if (x < 0)
                        x = 0;
                    else if (x > 1)
                        x = 1.0;

                    if (y <= 0.0) y = 0.0;
                    if (y >= 1.0) y = 1.0;
                    int harmonic = (int)(x * (126.0 - 1) + 0.5);

                    if (!constrainTo(harmonic))
                        return;
                                        
                    double val = y * 99.0;
                                
                    if (harmonic < 63)
                        KawaiK5.this.model.set("s" + 1 + "dhgharm" + (harmonic + 1) + "level", (int)val);
                    else
                        KawaiK5.this.model.set("s" + 2 + "dhgharm" + (harmonic + 1 - 63) + "level", (int)val);
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
                    return (int)(x * (126.0 - 1) + 0.5);
                    }

                public boolean constrainTo(int index) { return _constrainTo(index); }
                };
            ((EnvelopeDisplay)comp).addVerticalDivider(0.5);
            vbox2.addLast(comp);
            hbox.addLast(vbox2);
            }
        else
            {
            VBox vbox2 = new VBox();
            String[] levels = new String[63];
            String[] mods = new String[63];
            for(int i = 0; i < levels.length; i++)
                {
                levels[i] = "s" + source + "dhg" + "harm" + (i + 1) + "level";
                mods[i] = "s" + source + "dhg" + "harm" + (i + 1) + "envselmodyn";
                }

            double[] widths = new double[63];
            for(int i = 1; i < widths.length; i++)
                widths[i] = 1.0 / (63 - 1);

            double[] heights = new double[63];
            for(int i = 0; i < heights.length; i++)
                heights[i] = 1.0 / 99;
                        
            double[] heights2 = new double[63];
            for(int i = 0; i < heights2.length; i++)
                heights2[i] = 1.0 / 4;
                        
            comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), new String[63], mods, widths, heights2)
                {
                public void updateFromMouse(double x, double y, boolean continuation)
                    {
                    if (x < 0)
                        x = 0;
                    else if (x > 1)
                        x = 1.0;

                    if (y <= 0.0) y = 0.0;
                    if (y >= 1.0) y = 1.0;
                    int harmonic = (int)(x * (63.0 - 1) + 0.5);

                    if (!constrainTo(harmonic))
                        return;
                                        
                    double val = y * 4.0;
                                
                    int modconstraint = KawaiK5.this.model.get("constrainmodharmonics", 0);

                    if (modconstraint > 0)
                        {
                        if (((int)val) != 0) 
                            val = modconstraint; 
                        }
                                        
                    KawaiK5.this.model.set("s" + source + "dhg" + "harm" + (harmonic + 1) + "envselmodyn", (int)val);
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
                    return (int)(x * (63.0 - 1) + 0.5);
                    }

                public boolean constrainTo(int index) { return _constrainTo(index); }

                public int verticalBorderThickness() { return 4; }
                };
            ((EnvelopeDisplay)comp).setPreferredHeight(24);
            vbox2.add(comp);
                
            comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), new String[63], levels, widths, heights)
                {
                public void updateFromMouse(double x, double y, boolean continuation)
                    {
                    if (x < 0)
                        x = 0;
                    else if (x > 1)
                        x = 1.0;

                    if (y <= 0.0) y = 0.0;
                    if (y >= 1.0) y = 1.0;
                    int harmonic = (int)(x * (63.0 - 1) + 0.5);

                    if (!constrainTo(harmonic))
                        return;
                                        
                    double val = y * 99.0;
                                
                    KawaiK5.this.model.set("s" + source + "dhg" + "harm" + (harmonic + 1) + "level", (int)val);
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
                    return (int)(x * (63.0 - 1) + 0.5);
                    }
                        
                public boolean constrainTo(int index) { return _constrainTo(index); }
                };
            vbox2.addLast(comp);
            hbox.addLast(vbox2);
            }
        
        main.addBottom(hbox);
        category.add(main, BorderLayout.CENTER);
        return category;
        }



    boolean _constrainTo(int index)
        {
        int constraints = model.get("constrainharmonics", 0);
        switch(constraints)
            {
            case ALL:
                {
                return true;
                }
            case ODD:
                {
                return ((index & 0x1) == 0x0);  // yes, it looks backwards but it isn't
                }
            case EVEN:
                {
                return ((index & 0x1) == 0x1);  // yes, it looks backwards but it isn't
                }
            case FIRST_THIRD:
                {
                return (index % 3) == 0;
                }
            case SECOND_THIRD:
                {
                return (index % 3) == 1;
                }
            case THIRD_THIRD:
                {
                return (index % 3) == 2;
                }
            case OCTAVE:
                {
                for(int i = 0; i < OCTAVE_HARMONICS.length; i++)
                    if (OCTAVE_HARMONICS[i] == (index + 1)) return true;
                return false; 
                }
            case FIFTH:
                {
                for(int i = 0; i < FIFTH_HARMONICS.length; i++)
                    if (FIFTH_HARMONICS[i] == (index + 1)) return true;
                return false; 
                }
            case MAJOR_THIRD:
                {
                for(int i = 0; i < MAJOR_THIRD_HARMONICS.length; i++)
                    if (MAJOR_THIRD_HARMONICS[i] == (index + 1)) return true;
                return false;                                           
                }
            case MINOR_SEVENTH:
                {
                for(int i = 0; i < MINOR_SEVENTH_HARMONICS.length; i++)
                    if (MINOR_SEVENTH_HARMONICS[i] == (index + 1)) return true;
                return false;                                           
                }
            case MAJOR_SECOND:
                {
                for(int i = 0; i < MAJOR_SECOND_HARMONICS.length; i++)
                    if (MAJOR_SECOND_HARMONICS[i] == (index + 1)) return true;
                return false;                                           
                }
            case MAJOR_SEVENTH:
                {
                for(int i = 0; i < MAJOR_SEVENTH_HARMONICS.length; i++)
                    if (MAJOR_SEVENTH_HARMONICS[i] == (index + 1)) return true;
                return false;                                           
                }
            case MINOR_SECOND:
                {
                for(int i = 0; i < MINOR_SECOND_HARMONICS.length; i++)
                    if (MINOR_SECOND_HARMONICS[i] == (index + 1)) return true;
                return false;                                           
                }
            case MINOR_THIRD:
                {
                for(int i = 0; i < MINOR_THIRD_HARMONICS.length; i++)
                    if (MINOR_THIRD_HARMONICS[i] == (index + 1)) return true;
                return false;                                           
                }
            case MAJOR_SIXTH:
                {
                for(int i = 0; i < MAJOR_SIXTH_HARMONICS.length; i++)
                    if (MAJOR_SIXTH_HARMONICS[i] == (index + 1)) return true;
                return false;                                           
                }

            }
        return false;
        }
        
        
    public JComponent addDHGEnvelope(int source, int envelope, Color color)
        {
        Category category = new Category(this, "Harmonics (DHG) Envelope " + envelope, color);
        category.makePasteable("s" + source + "dhg");
        category.makeDistributable("s" + source + "dhg");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = MAX_SEGMENTS;
        comp = new Chooser("Max Segment", this, "s" + source + "dhg" + "env" + envelope + "maxsegonoff", params);
        vbox.add(comp);
        hbox.add(vbox);
                
        comp = new CheckBox("Active", this, "s" + source + "dhg" + "env" + envelope + "onoff", false);
        vbox.add(comp); 
        hbox.add(vbox);   

        comp = new LabelledDial("Mod Depth", this, "s" + source + "dhg" + "env" + envelope + "moddepth", color, 0, 31)
            {
            public String map(int val)
                {
                if (val == 0) return "Off";
                else return "" + val;
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Rate 1", this, "s" + source + "dhg" + "env" + envelope + "seg1rate", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Level 1", this, "s" + source + "dhg" + "env" + envelope + "seg1level", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Rate 2", this, "s" + source + "dhg" + "env" + envelope + "seg2rate", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Level 2", this, "s" + source + "dhg" + "env" + envelope + "seg2level", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Rate 3", this, "s" + source + "dhg" + "env" + envelope + "seg3rate", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Level 3", this, "s" + source + "dhg" + "env" + envelope + "seg3level", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Rate 4", this, "s" + source + "dhg" + "env" + envelope + "seg4rate", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Level 4", this, "s" + source + "dhg" + "env" + envelope + "seg4level", color, 0, 31);
        ((LabelledDial)comp).addAdditionalLabel("(Sustain)");
        hbox.add(comp);

        comp = new LabelledDial("Rate 5", this, "s" + source + "dhg" + "env" + envelope + "seg5rate", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Level 5", this, "s" + source + "dhg" + "env" + envelope + "seg5level", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Rate 6", this, "s" + source + "dhg" + "env" + envelope + "seg6rate", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Level 6", this, "s" + source + "dhg" + "env" + envelope + "seg6level", color, 0, 31);
        hbox.add(comp);

        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null,  
                           "s" + source + "dhg" + "env" + envelope + "seg1rate",
                           "s" + source + "dhg" + "env" + envelope + "seg2rate",
                           "s" + source + "dhg" + "env" + envelope + "seg3rate",
                           "s" + source + "dhg" + "env" + envelope + "seg4rate",
                           "s" + source + "dhg" + "env" + envelope + "seg5rate",
                           "s" + source + "dhg" + "env" + envelope + "seg6rate" },
            new String[] { null,  
                           "s" + source + "dhg" + "env" + envelope + "seg1level",
                           "s" + source + "dhg" + "env" + envelope + "seg2level",
                           "s" + source + "dhg" + "env" + envelope + "seg3level",
                           "s" + source + "dhg" + "env" + envelope + "seg4level",
                           "s" + source + "dhg" + "env" + envelope + "seg5level",
                           "s" + source + "dhg" + "env" + envelope + "seg6level" },
            new double[] { 0, .1666666666 / 31, .1666666666 / 31, .1666666666 / 31, .1666666666 / 31, .1666666666 / 31, .1666666666 / 31 },
            new double[] { 0, 1.0 / 31, 1.0 / 31, 1.0 / 31, 1.0 / 31, 1.0 / 31, 1.0 / 31 });
        hbox.addLast(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
                
                

        
    public JComponent addDHGEnvelopeGlobal(int source, Color color)
        {
        Category category = new Category(this, "Harmonics (DHG) Envelope Global", color);
        category.makePasteable("s" + source + "dhg");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        /*
          params = DHG_SELECT;
          comp = new Chooser("Select", this, "s" + source + "dhg" + "harmsel", params);
          vbox.add(comp);
          hbox.add(vbox);   

          params = ANGLES;
          comp = new Chooser("Angle", this, "s" + source + "dhg" + "angle", params);
          vbox.add(comp);
        */

        params = SIMPLE_ENVELOPES;
        comp = new Chooser("All Env", this, "s" + source + "dhg" + "allenv", params);
        vbox.add(comp);
        comp = new CheckBox("Mod", this, "s" + source + "dhg" + "allmodonoff", false);
        vbox.add(comp);
        hbox.add(vbox);

        vbox = new VBox();
        params = SIMPLE_ENVELOPES;
        comp = new Chooser("Odd Env", this, "s" + source + "dhg" + "oddenv", params);
        vbox.add(comp);
        comp = new CheckBox("Mod", this, "s" + source + "dhg" + "oddmodonoff", false);
        vbox.add(comp);
        hbox.add(vbox);

        vbox = new VBox();
        params = SIMPLE_ENVELOPES;
        comp = new Chooser("Even Env", this, "s" + source + "dhg" + "evenenv", params);
        vbox.add(comp);
        comp = new CheckBox("Mod", this, "s" + source + "dhg" + "evenmodonoff", false);
        vbox.add(comp);
        hbox.add(vbox);

        vbox = new VBox();
        params = SIMPLE_ENVELOPES;
        comp = new Chooser("Octave Env", this, "s" + source + "dhg" + "octaveenv", params);
        vbox.add(comp);
        comp = new CheckBox("Mod", this, "s" + source + "dhg" + "octavemodonoff", false);
        vbox.add(comp);
        hbox.add(vbox);

        vbox = new VBox();
        params = SIMPLE_ENVELOPES;
        comp = new Chooser("Fifth Env", this, "s" + source + "dhg" + "fifthenv", params);
        vbox.add(comp);
        comp = new CheckBox("Mod", this, "s" + source + "dhg" + "fifthmodonoff", false);
        vbox.add(comp);
        hbox.add(vbox);

        vbox = new VBox();
        comp = new CheckBox("Shadow", this, "s" + source + "dhg" + "shadowonoff", false);
        vbox.add(comp); 
        hbox.add(vbox);   
        

        // we don't display these
                
        model.set("s" + source + "dhg" + "all", 0);
        model.setStatus("s" + source + "dhg" + "all", Model.STATUS_IMMUTABLE);
        model.set("s" + source + "dhg" + "odd", 0);
        model.setStatus("s" + source + "dhg" + "odd", Model.STATUS_IMMUTABLE);
        model.set("s" + source + "dhg" + "even", 0);
        model.setStatus("s" + source + "dhg" + "even", Model.STATUS_IMMUTABLE);
        model.set("s" + source + "dhg" + "octave", 0);
        model.setStatus("s" + source + "dhg" + "octave", Model.STATUS_IMMUTABLE);
        model.set("s" + source + "dhg" + "fifth", 0);
        model.setStatus("s" + source + "dhg" + "fifth", Model.STATUS_IMMUTABLE);
        model.set("s" + source + "dhg" + "rangefrom", 0);
        model.setStatus("s" + source + "dhg" + "rangefrom", Model.STATUS_IMMUTABLE);
        model.set("s" + source + "dhg" + "rangeto", 0);
        model.setStatus("s" + source + "dhg" + "rangeto", Model.STATUS_IMMUTABLE);
        model.set("s" + source + "dhg" + "select", 0);
        model.setStatus("s" + source + "dhg" + "select", Model.STATUS_IMMUTABLE);
        model.set("s" + source + "dhg" + "angle", 0);
        model.setStatus("s" + source + "dhg" + "angle", Model.STATUS_IMMUTABLE);
        model.set("s" + source + "dhg" + "harmsel", 0);
        model.setStatus("s" + source + "dhg" + "harmsel", Model.STATUS_IMMUTABLE);


/*
  comp = new LabelledDial("All", this, "s" + source + "dhg" + "all", color, 0, 99);
  model.setStatus("s" + source + "dhg" + "all", Model.STATUS_IMMUTABLE);
  hbox.add(comp);

  comp = new LabelledDial("Odd", this, "s" + source + "dhg" + "odd", color, 0, 99);
  model.setStatus("s" + source + "dhg" + "odd", Model.STATUS_IMMUTABLE);
  hbox.add(comp);

  comp = new LabelledDial("Even", this, "s" + source + "dhg" + "even", color, 0, 99);
  model.setStatus("s" + source + "dhg" + "even", Model.STATUS_IMMUTABLE);
  hbox.add(comp);

  comp = new LabelledDial("Octave", this, "s" + source + "dhg" + "octave", color, 0, 99);
  model.setStatus("s" + source + "dhg" + "octave", Model.STATUS_IMMUTABLE);
  hbox.add(comp);

  comp = new LabelledDial("Fifth", this, "s" + source + "dhg" + "fifth", color, 0, 99);
  model.setStatus("s" + source + "dhg" + "fifth", Model.STATUS_IMMUTABLE);
  hbox.add(comp);

  comp = new LabelledDial("Range Low", this, "s" + source + "dhg" + "rangefrom", color, 1, 63);
  hbox.add(comp);

  comp = new LabelledDial("Range High", this, "s" + source + "dhg" + "rangeto", color, 1, 63);
  hbox.add(comp);
*/      


        comp = new LabelledDial("Velocity", this, "s" + source + "dhg" + "velodep", color, -31, 31);
        ((LabelledDial)comp).addAdditionalLabel("Depth");
        hbox.add(comp);

        comp = new LabelledDial("Pressure", this, "s" + source + "dhg" + "presdep", color, -31, 31);
        ((LabelledDial)comp).addAdditionalLabel("Depth");
        hbox.add(comp);

        comp = new LabelledDial("Key", this, "s" + source + "dhg" + "ksdep", color, -31, 31);
        ((LabelledDial)comp).addAdditionalLabel("Scaling");
        hbox.add(comp);

        comp = new LabelledDial("LFO", this, "s" + source + "dhg" + "lfodep", color, 0, 31);
        ((LabelledDial)comp).addAdditionalLabel("Depth");
        hbox.add(comp);
                
        // These four we don't have widgets for
        
        model.set("s1dhgharm", 1);
        model.setStatus("s1dhgharm", Model.STATUS_IMMUTABLE);
        model.set("s2dhgharm", 1);
        model.setStatus("s2dhgharm", Model.STATUS_IMMUTABLE);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
    




    public JComponent addEqualizer(Color color)
        {
        Category category = new Category(this, "Formant Equalizer (DFT)", color);
        category.makeDistributable("dft");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        comp = new CheckBox("Active", this, "dftonoff", false);
        vbox.add(comp); 
        hbox.add(vbox);   

        comp = new LabelledDial("-1", this, "dftc11level", color, 0, 63);  // we call it 11 rather than -1 so we can properly do category distribution
        hbox.add(comp);

        comp = new LabelledDial("0", this, "dftc0level", color, 0, 63);
        hbox.add(comp);

        comp = new LabelledDial("1", this, "dftc1level", color, 0, 63);
        hbox.add(comp);

        comp = new LabelledDial("2", this, "dftc2level", color, 0, 63);
        hbox.add(comp);

        comp = new LabelledDial("3", this, "dftc3level", color, 0, 63);
        hbox.add(comp);

        comp = new LabelledDial("4", this, "dftc4level", color, 0, 63);
        hbox.add(comp);

        comp = new LabelledDial("5", this, "dftc5level", color, 0, 63);
        hbox.add(comp);

        comp = new LabelledDial("6", this, "dftc6level", color, 0, 63);
        hbox.add(comp);

        comp = new LabelledDial("7", this, "dftc7level", color, 0, 63);
        hbox.add(comp);

        comp = new LabelledDial("8", this, "dftc8level", color, 0, 63);
        hbox.add(comp);

        comp = new LabelledDial("9", this, "dftc9level", color, 0, 63);
        hbox.add(comp);

        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, null, null, null, null, null, null, null, null, null, null },
            new String[] { 
                "dftc11level",
                "dftc0level",
                "dftc1level",
                "dftc2level",
                "dftc3level",
                "dftc4level",
                "dftc5level",
                "dftc6level",
                "dftc7level",
                "dftc8level",
                "dftc9level"
                },
            new double[] { 0, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1 },
            new double[] { 1.0 / 63, 1.0 / 63, 1.0 / 63, 1.0 / 63, 1.0 / 63, 1.0 / 63, 1.0 / 63, 1.0 / 63, 1.0 / 63, 1.0 / 63, 1.0 / 63 });
        hbox.addLast(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }



    public JComponent addFilter(int source, Color color)
        {
        Category category = new Category(this, "Filter (DDF)", color);
        category.makePasteable("s" + source + "ddf");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        comp = new CheckBox("Active", this, "s" + source + "ddf" + "ddfonoff", false);
        vbox.add(comp); 

        comp = new CheckBox("Global Mod", this, "s" + source + "ddf" + "ddfmodonoff", false);
        vbox.add(comp); 
        hbox.add(vbox);   

        comp = new LabelledDial("Flat Level", this, "s" + source + "ddf" + "flatlevel", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Cutoff", this, "s" + source + "ddf" + "cutoff", color, 0, 99);
        hbox.add(comp);

        comp = new LabelledDial("Slope", this, "s" + source + "ddf" + "slope", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Cutoff Mod", this, "s" + source + "ddf" + "cutoffmod", color, 0, 31);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        comp = new LabelledDial("Slope Mod", this, "s" + source + "ddf" + "slopemod", color, 0, 31);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        comp = new LabelledDial("Envelope", this, "s" + source + "ddf" + "envdep", color, -31, 31);
        ((LabelledDial)comp).addAdditionalLabel("Mod");
        hbox.add(comp);

        comp = new LabelledDial("Velocity", this, "s" + source + "ddf" + "veloenvdep", color, -31, 31);
        ((LabelledDial)comp).addAdditionalLabel("Env Mod");
        hbox.add(comp);

        comp = new LabelledDial("Velocity", this, "s" + source + "ddf" + "velodep", color, -31, 31);
        ((LabelledDial)comp).addAdditionalLabel("Mod");
        hbox.add(comp);

        comp = new LabelledDial("Aftertouch", this, "s" + source + "ddf" + "presdep", color, -31, 31);
        ((LabelledDial)comp).addAdditionalLabel("Mod");
        hbox.add(comp);
                
        comp = new LabelledDial("Key Scale", this, "s" + source + "ddf" + "ksdep", color, -31, 31);
        ((LabelledDial)comp).addAdditionalLabel("Mod");
        hbox.add(comp);
                
        comp = new LabelledDial("LFO", this, "s" + source + "ddf" + "lfodep", color, 0, 31);
        ((LabelledDial)comp).addAdditionalLabel("Mod");
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
        
    public JComponent addFilterEnvelope(int source, Color color)
        {
        Category category = new Category(this, "Filter (DDF) Envelope", color);
        category.makePasteable("s" + source + "ddf");
        category.makeDistributable("s" + source + "ddf");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = MAX_SEGMENTS;
        comp = new Chooser("Max Segment", this, "s" + source + "ddf" + "envmaxsegonoff", params);
        vbox.add(comp);
        hbox.add(vbox);
                
        comp = new LabelledDial("Rate 1", this, "s" + source + "ddf" + "envseg1rate", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Level 1", this, "s" + source + "ddf" + "envseg1level", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Rate 2", this, "s" + source + "ddf" + "envseg2rate", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Level 2", this, "s" + source + "ddf" + "envseg2level", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Rate 3", this, "s" + source + "ddf" + "envseg3rate", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Level 3", this, "s" + source + "ddf" + "envseg3level", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Rate 4", this, "s" + source + "ddf" + "envseg4rate", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Level 4", this, "s" + source + "ddf" + "envseg4level", color, 0, 31);
        ((LabelledDial)comp).addAdditionalLabel("(Sustain)");
        hbox.add(comp);

        comp = new LabelledDial("Rate 5", this, "s" + source + "ddf" + "envseg5rate", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Level 5", this, "s" + source + "ddf" + "envseg5level", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Rate 6", this, "s" + source + "ddf" + "envseg6rate", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Level 6", this, "s" + source + "ddf" + "envseg6level", color, 0, 31);
        hbox.add(comp);

        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null,  
                           "s" + source + "ddf" + "envseg1rate",
                           "s" + source + "ddf" + "envseg2rate",
                           "s" + source + "ddf" + "envseg3rate",
                           "s" + source + "ddf" + "envseg4rate",
                           "s" + source + "ddf" + "envseg5rate",
                           "s" + source + "ddf" + "envseg6rate" },
            new String[] { null,  
                           "s" + source + "ddf" + "envseg1level",
                           "s" + source + "ddf" + "envseg2level",
                           "s" + source + "ddf" + "envseg3level",
                           "s" + source + "ddf" + "envseg4level",
                           "s" + source + "ddf" + "envseg5level",
                           "s" + source + "ddf" + "envseg6level" },
            new double[] { 0, .1666666666 / 31, .1666666666 / 31, .1666666666 / 31, .1666666666 / 31, .1666666666 / 31, .1666666666 / 31 },
            new double[] { 0, 1.0 / 31, 1.0 / 31, 1.0 / 31, 1.0 / 31, 1.0 / 31, 1.0 / 31 });
        hbox.addLast(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addDDA(int source, Color color)
        {
        Category category = new Category(this, "Amplifier (DDA)", color);
        category.makePasteable("s" + source + "dda");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        comp = new CheckBox("Active", this, "s" + source + "dda" + "ddaonoff", false);
        vbox.add(comp); 
        hbox.add(vbox);
                
        comp = new LabelledDial("Velocity", this, "s" + source + "dda" + "attackvelodep", color, -31, 31);
        ((LabelledDial)comp).addAdditionalLabel("Mod");
        hbox.add(comp);

        comp = new LabelledDial("Aftertouch", this, "s" + source + "dda" + "presdep", color, -31, 31);
        ((LabelledDial)comp).addAdditionalLabel("Mod");
        hbox.add(comp);
                
        comp = new LabelledDial("Key Scale", this, "s" + source + "dda" + "ksdep", color, -31, 31);
        ((LabelledDial)comp).addAdditionalLabel("Mod");
        hbox.add(comp);
                
        comp = new LabelledDial("LFO", this, "s" + source + "dda" + "lfodep", color, 0, 31);
        ((LabelledDial)comp).addAdditionalLabel("Mod");
        hbox.add(comp);

        comp = new LabelledDial("Attack Vel", this, "s" + source + "dda" + "attackvelrate", color, -15, 15);
        ((LabelledDial)comp).addAdditionalLabel("Rate Mod");
        hbox.add(comp);

        comp = new LabelledDial("Release Vel", this, "s" + source + "dda" + "releasevelrate", color, -15, 15);
        ((LabelledDial)comp).addAdditionalLabel("Rate Mod");
        hbox.add(comp);

        comp = new LabelledDial("Key Scale", this, "s" + source + "dda" + "ksrate", color, -15, 15);
        ((LabelledDial)comp).addAdditionalLabel("Rate Mod");
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
        
    public JComponent addDDAEnvelope(int source, Color color)
        {
        Category category = new Category(this, "Amplifier (DDA) Envelope", color);
        category.makePasteable("s" + source + "dda");
        category.makeDistributable("s" + source + "dda");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = MAX_SEGMENTS;
        comp = new Chooser("Max Segment", this, "s" + source + "dda" + "envmaxsegonoff", params);
        vbox.add(comp);
        hbox.add(vbox);

        vbox = new VBox();
        comp = new LabelledDial("Rate 1", this, "s" + source + "dda" + "envseg1rate", color, 0, 31);
        vbox.add(comp);
        comp = new CheckBox("Mod", this, "s" + source + "dda" + "envseg1modonoff", false);
        vbox.add(comp); 
        hbox.add(vbox);
        vbox = new VBox();


        comp = new LabelledDial("Level 1", this, "s" + source + "dda" + "envseg1level", color, 0, 31);
        hbox.add(comp); 
                
        comp = new LabelledDial("Rate 2", this, "s" + source + "dda" + "envseg2rate", color, 0, 31);
        vbox.add(comp);
        comp = new CheckBox("Mod", this, "s" + source + "dda" + "envseg2modonoff", false);
        vbox.add(comp); 
        hbox.add(vbox);
        vbox = new VBox();


        comp = new LabelledDial("Level 2", this, "s" + source + "dda" + "envseg2level", color, 0, 31);
        hbox.add(comp); 

        comp = new LabelledDial("Rate 3", this, "s" + source + "dda" + "envseg3rate", color, 0, 31);
        vbox.add(comp);
        comp = new CheckBox("Mod", this, "s" + source + "dda" + "envseg3modonoff", false);
        vbox.add(comp); 
        hbox.add(vbox);
        vbox = new VBox();


        comp = new LabelledDial("Level 3", this, "s" + source + "dda" + "envseg3level", color, 0, 31);
        hbox.add(comp); 

        comp = new LabelledDial("Rate 4", this, "s" + source + "dda" + "envseg4rate", color, 0, 31);
        vbox.add(comp);
        comp = new CheckBox("Mod", this, "s" + source + "dda" + "envseg4modonoff", false);
        vbox.add(comp); 
        hbox.add(vbox);
        vbox = new VBox();


        comp = new LabelledDial("Level 4", this, "s" + source + "dda" + "envseg4level", color, 0, 31);
        ((LabelledDial)comp).addAdditionalLabel("(Sustain)");
        hbox.add(comp);

        comp = new LabelledDial("Rate 5", this, "s" + source + "dda" + "envseg5rate", color, 0, 31);
        vbox.add(comp); 
        comp = new CheckBox("Mod", this, "s" + source + "dda" + "envseg5modonoff", false);
        vbox.add(comp); 
        hbox.add(vbox);
        vbox = new VBox();


        comp = new LabelledDial("Level 5", this, "s" + source + "dda" + "envseg5level", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Rate 6", this, "s" + source + "dda" + "envseg6rate", color, 0, 31);
        vbox.add(comp);
        comp = new CheckBox("Mod", this, "s" + source + "dda" + "envseg6modonoff", false);
        vbox.add(comp); 
        hbox.add(vbox);
        vbox = new VBox();


        comp = new LabelledDial("Level 6", this, "s" + source + "dda" + "envseg6level", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Rate 7", this, "s" + source + "dda" + "envseg7rate", color, 0, 31);
        vbox.add(comp);
        comp = new CheckBox("Mod", this, "s" + source + "dda" + "envseg7modonoff", false);
        vbox.add(comp); 
        hbox.add(vbox);
        vbox = new VBox();

        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null,  
                           "s" + source + "dda" + "envseg1rate",
                           "s" + source + "dda" + "envseg2rate",
                           "s" + source + "dda" + "envseg3rate",
                           "s" + source + "dda" + "envseg4rate",
                           "s" + source + "dda" + "envseg5rate",
                           "s" + source + "dda" + "envseg6rate",
                           "s" + source + "dda" + "envseg7rate",
                },
            new String[] { null,  
                           "s" + source + "dda" + "envseg1level",
                           "s" + source + "dda" + "envseg2level",
                           "s" + source + "dda" + "envseg3level",
                           "s" + source + "dda" + "envseg4level",
                           "s" + source + "dda" + "envseg5level",
                           "s" + source + "dda" + "envseg6level",
                           null
                },
            new double[] { 0, .1428571428 / 31, .1428571428 / 31, .1428571428 / 31, .1428571428 / 31, .1428571428 / 31, .1428571428 / 31,  .1428571428 / 31 },
            new double[] { 0, 1.0 / 31, 1.0 / 31, 1.0 / 31, 1.0 / 31, 1.0 / 31, 1.0 / 31, 0 });
        hbox.addLast(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
    
    // The K5 can't send to temporary memory, so we write to SID-12
    public boolean getSendsAllParametersInBulk() { return true; } // sendKawaiParametersInBulk; }

    public Object[] emitAll(String key)
        {
        // we have to check for these or else they'll trigger later
        if (key.equals("s1dhgharm")) return new Object[0];
        if (key.equals("s2dhgharm")) return new Object[0];
        if (key.equals("s1dhgodd") ||
            key.equals("s1dhgeven") ||
            key.equals("s1dhgoctave") ||
            key.equals("s1dhgfifth") ||
            key.equals("s1dhgselect") ||
            key.equals("s1dhgall") ||
            key.equals("s2dhgodd") ||
            key.equals("s2dhgeven") ||
            key.equals("s2dhgoctave") ||
            key.equals("s2dhgfifth") ||
            key.equals("s2dhgselect") ||
            key.equals("s2dhgall")) return new Object[0];
        if (key.equals("constrainharmonics") || 
            key.equals("constrainmodharmonics")) return new Object[0];
        if (key.equals("bank")) return new Object[0];
        if (key.equals("number")) return new Object[0];
        
        
        // WE don't bother with bank,  number, and a few others.
        // Also we don't presently emit 181, 182, 183, 183, or 185 (inc/dec the odd/even/oct/5th/all options), see Note 6-4)
        
        Object[] data = new Object[1];

        // determine source
        int source = 0;  // doesn't matter
        if (key.startsWith("s1basic") || key.startsWith("s2basic"))
            {
            source = (key.charAt(6) == '1' ? 0 : 1);
            }
        else if (key.startsWith("s1ks") || key.startsWith("s2ks"))
            {
            source = (key.charAt(3) == '1' ? 0 : 1);
            }
        else if (key.startsWith("s1dhg") || key.startsWith("s2dhg") ||
            key.startsWith("s1dda") || key.startsWith("s2dda") ||
            key.startsWith("s1ddf") || key.startsWith("s2ddf") ||
            key.startsWith("s1dhg") || key.startsWith("s2dhg") ||
            key.startsWith("s1dfg") || key.startsWith("s2dfg"))
            {
            source = (key.charAt(4) == '1' ? 0 : 1);
            }        

        if (key.equals("name"))
            {
            String name = model.get(key, "        ") + "        ";
            data = new Object[15];
            for(int i = 0; i < data.length; i += 2)
                {
                byte c = (byte)(name.charAt(i/2));
                int paramNum = (byte)(200 + i/2);
                data[i] = new byte[] { (byte)0xF0, 0x40, (byte)(getChannelOut()), 0x10, 0x00, 0x02, 
                    (byte)((source << 1) | ((paramNum >>> 7) & 0x1)), (byte)(paramNum & 127), 
                    (byte)((c >>> 4) & 15), (byte)(c & 15), (byte)0xF7 };
                if (i + 1 < data.length) 
                    data[i + 1] = Integer.valueOf(30);
                }
            return data;
            }
        else if ((key.startsWith("s1dhg") || key.startsWith("s2dhg"))&& key.endsWith("maxsegonoff") ||
            (key.startsWith("s1ddf") || key.startsWith("s2ddf")) && key.endsWith("maxsegonoff") ||
            (key.startsWith("s1dda") || key.startsWith("s2dda")) && key.endsWith("maxsegonoff"))
            {
            int paramNum = 0;
            if (key.startsWith("s1ddf") || key.startsWith("s2ddf")) paramNum = 131;
            else if (key.startsWith("s1dda") || key.startsWith("s2dda")) paramNum = 161;
            else if (key.startsWith("s1dhgenv1") ||
                key.startsWith("s2dhgenv1")) paramNum = 52;
            else if (key.startsWith("s1dhgenv2") ||
                key.startsWith("s2dhgenv2")) paramNum = 53;
            else if (key.startsWith("s1dhgenv3") ||
                key.startsWith("s2dhgenv3")) paramNum = 54;
            else if (key.startsWith("s1dhgenv4") ||
                key.startsWith("s2dhgenv4")) paramNum = 55;
            else System.err.println("Warning (KawaiK5): Invalid Key " + key);
                
            int c = model.get(key, 0);
            data[0] = new byte[] { (byte)0xF0, 0x40, (byte)(getChannelOut()), 0x10, 0x00, 0x02, (byte)((source << 1) | ((paramNum >>> 7) & 0x1)), (byte)(paramNum & 127),(byte)((c >>> 4) & 15), (byte)(c & 15), (byte)0xF7 };
            return data;
            }
        else if ((key.startsWith("s1dhgharm") || key.startsWith("s2dhgharm")) && !(key.equals("s1dhgharmsel") || key.equals("s2dhgharmsel")))  // harmonics
            {
            String[] numbers = key.split("[\\D]+");
            int harmonic = Integer.parseInt(numbers[2]);
                
            if (key.endsWith("envselmodyn"))
                {
                int c = model.get(key, 0);
                int mod = (c == 0 ? 0 : 1);
                int env = (c == 0 ? model.get(key + "-env", 0) + 1 : c);  // use the default if need be.  Note that it's 1...4, not 0...3.  Don't ask.

                data = new Object[5];
                int paramNum = 40;
                data[0] = new byte[] { (byte)0xF0, 0x40, (byte)(getChannelOut()), 0x10, 0x00, 0x02, (byte)((source << 1) | ((paramNum >>> 7) & 0x1)), (byte)(paramNum & 127), (byte)((harmonic >>> 4) & 0xF), (byte)(harmonic & 0xF), (byte)0xF7 };
                data[1] = Integer.valueOf(30);
                paramNum = 42;
                data[2] = new byte[] { (byte)0xF0, 0x40, (byte)(getChannelOut()), 0x10, 0x00, 0x02, (byte)((source << 1) | ((paramNum >>> 7) & 0x1)), (byte)(paramNum & 127), (byte)((env >>> 4) & 0xF), (byte)(env & 0xF), (byte)0xF7 };
                data[3] = Integer.valueOf(30);
                paramNum = 43;
                data[4] = new byte[] { (byte)0xF0, 0x40, (byte)(getChannelOut()), 0x10, 0x00, 0x02, (byte)((source << 1) | ((paramNum >>> 7) & 0x1)), (byte)(paramNum & 127), (byte)((mod >>> 4) & 0xF), (byte)(mod & 0xF), (byte)0xF7 };
                }
            else if(key.endsWith("level"))
                {
                data = new Object[3];
                int level = model.get(key, 0);
                int paramNum = 40;
                data[0] = new byte[] { (byte)0xF0, 0x40, (byte)(getChannelOut()), 0x10, 0x00, 0x02, (byte)((source << 1) | ((paramNum >>> 7) & 0x1)), (byte)(paramNum & 127), (byte)((harmonic >>> 4) & 0xF), (byte)(harmonic & 0xF), (byte)0xF7 };
                data[1] = Integer.valueOf(30);
                paramNum = 41;
                data[2] = new byte[] { (byte)0xF0, 0x40, (byte)(getChannelOut()), 0x10, 0x00, 0x02, (byte)((source << 1) | ((paramNum >>> 7) & 0x1)), (byte)(paramNum & 127), (byte)((level >>> 4) & 0xF), (byte)(level & 0xF), (byte)0xF7 };
                }
            else 
                {
                // all the -env stuff will go here
                return new Object[0];
                }
                        
            return data;            
            }
        else if (parametersToIndex.containsKey(key))
            {
            int c = model.get(key, 0);
                                
            // handle lfoshape specially, it has to go out as 1--6 rather than 0--5
            if (key.equals("lfoshape"))
                c++;
                                        
            int paramNum = ((Integer)(parametersToIndex.get(key))).intValue();
            data[0] = new byte[] { (byte)0xF0, 0x40, (byte)(getChannelOut()), 0x10, 0x00, 0x02, (byte)((source << 1) | ((paramNum >>> 7) & 0x1)), (byte)(paramNum & 127), (byte)((c >>> 4) & 15), (byte)(c & 15), (byte)0xF7 };
            return data;
            }
        else 
            {
            System.err.println("Warning (KawaiK5): Unknown Key " + key);
            return new Object[0];
            }
        }


    public JMenuItem copy = new JMenuItem("Copy Tab");
    public JMenuItem paste = new JMenuItem("Paste Tab");
    public JMenuItem copyMutable = new JMenuItem("Copy Tab (Mutation Parameters Only)");
    public JMenuItem pasteMutable = new JMenuItem("Paste Tab (Mutation Parameters Only)");
    public JMenuItem reset = new JMenuItem("Reset Tab");
    
    

    public void parseParameter(byte[] data)
        {
        if (data.length == 9 &&                 // write error report
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x40 &&
            data[3] >= (byte)0x41 &&
            data[3] <= (byte)0x43 &&
            data[4] == (byte)0x00 &&
            data[5] == (byte)0x03)
            {
            String error = "Write Failed (Maybe Transmission Failure)";
            // dump failed
            if (data[3] == 0x42)
                error = "Patch is Write-Protected";
            else if (data[3] == 0x43)
                error = "External Data Card is Not Inserted";
                        
            showSimpleError("Write Failed", error);
            }
        }

    boolean validBulkSysex(byte[] result, int pos)
        {
        if (pos + EXPECTED_SYSEX_LENGTH >= result.length) return false;
        if (result[pos] != (byte)0xF0) return false;
        for(int i = pos + 1; i < pos + EXPECTED_SYSEX_LENGTH; i++)
            if (result[i] >= 128) return false;
        return (result[pos + EXPECTED_SYSEX_LENGTH - 1] == (byte)0xF7);
        }

    public int parse(byte[] result, boolean fromFile)
        {
        model.set("bank", result[7] / 12);
        model.set("number", result[7] % 12);

        // denybblize
        byte[] data = new byte[492];

        int v = 8;
        for(int i = 0; i < data.length; i++)
            {
            data[i] = (byte)(((result[v++] & 0xF) << 4) | (result[v++] & 0xF));
            }

        // Name ...
        
        try
            {
            model.set("name", new String(data, 0, 8, "US-ASCII"));
            }
        catch (UnsupportedEncodingException ex) { } // won't happen
        
        int pos = 8;
        
        // Pre-DHG Parameters...

        pos = unloadData(data, pos, volume, s1basicpedalassign);
        pos = unloadData(data, pos, s1basicpedalassign, portamentosw, new int[] { 4, 0 }, new int[] { 4, 4 } );
        pos = unloadData(data, pos, portamentosw, mode, new int[] { 7, 0 }, new int[] { 1, 6 } );
        pos = unloadData(data, pos, mode, s1dfgcoarse, new int[] { 2, 0 }, new int[] { 1, 2 } );
        pos = unloadData(data, pos, s1dfgcoarse, s1dfgkey);
        pos = unloadData(data, pos, s1dfgkey, s1dfgenvdep, new int[] { 7, 0 }, new int[] { 1, 7 } );
        pos = unloadData(data, pos, s1dfgenvdep, s1dfgenvloop);
        pos = unloadData(data, pos, s1dfgenvloop, s1dfgenvrateseg1, new int[] { 7, 0 }, new int[] { 1, 7 } );
        pos = unloadData(data, pos, s1dfgenvrateseg1, s1dhgvelodep);  // in fact it just goes to the harmonics
        
        // Harmonics levels...
                
        int x = 0;
        for( ; x < 63 * 2; x++)
            {
            model.set(HARMONICS_PARAMETERS[x], data[pos++] );
            }
                
        // Harmonics mod and env sel...
                
        for( ; x < 63 * 2 + 62 * 2; x += 2)
            {
            int mod2 = (data[pos] >>> 7) & 1;
            int env2 = (data[pos] >>> 4) & 3;
            int mod1 = (data[pos] >>> 3) & 1;
            int env1 = (data[pos] >>> 0) & 3;
                        
            if (mod2 == 1)
                model.set(HARMONICS_PARAMETERS[x + 0], env2 + 1);
            else
                model.set(HARMONICS_PARAMETERS[x + 0], 0);
                                
            model.set(HARMONICS_PARAMETERS[x + 0] + "-env", env2);
                        
            if (mod1 == 1)
                model.set(HARMONICS_PARAMETERS[x + 1], env1 + 1);
            else
                model.set(HARMONICS_PARAMETERS[x + 1], 0);

            model.set(HARMONICS_PARAMETERS[x + 1] + "-env", env1);
            pos++;
            }

        int mod2 = (data[pos] >>> 3) & 1;
        int env2 = (data[pos] >>> 0) & 3;
        pos++;

        int mod1 = (data[pos] >>> 3) & 1;
        int env1 = (data[pos] >>> 0) & 3;

        pos++;
                                
        if (mod2 == 1)
            model.set(HARMONICS_PARAMETERS[x + 0], env2 + 1);
        else
            model.set(HARMONICS_PARAMETERS[x + 0], 0);
                        
        model.set(HARMONICS_PARAMETERS[x + 0] + "-env", env2);

        if (mod1 == 1)
            model.set(HARMONICS_PARAMETERS[x + 1], env1 + 1);
        else
            model.set(HARMONICS_PARAMETERS[x + 1], 0);

        model.set(HARMONICS_PARAMETERS[x + 1] + "-env", env1);

                
        // DHG and Post-DHG parameters...
        
        pos = unloadData(data, pos, s1dhgvelodep, s1dhgenv1onoff);
        pos = unloadData(data, pos, s1dhgenv1onoff, s1dhgmodonoff, new int[] { 7, 0 }, new int[] { 1, 5 } );
        pos = unloadData(data, pos, s1dhgmodonoff, s1dhgrangefrom, new int[] { 7, 0 }, new int[] { 1, 2 } );
        pos = unloadData(data, pos, s1dhgrangefrom, s1dhgoddmodonoff);
        pos = unloadData(data, pos, s1dhgoddmodonoff, s1dhgallmodonoff, new int[] { 7, 4, 3, 0 }, new int[] { 1, 2, 1, 2 } );
        pos = unloadData(data, pos, s1dhgallmodonoff, s1dhgangle, new int[] { 7, 4 }, new int[] { 1, 2 } );
        pos = unloadData(data, pos, s1dhgangle, s1dhgshadowonoff);

        int shadowpos = pos;

        pos = unloadDataUnified(data, pos, s1dhgenv1maxseg1onoff, s1dhgenv1seg1rate, new int[] { 6, 0 }, new int[] { 1, 6 }, new int[] { s1dhgshadowonoff, s2dhgshadowonoff } );
                
        // handle shadow specially
        model.set("s1dhgshadowonoff", (data[shadowpos] & 255) >>> 7);
        model.set("s2dhgshadowonoff", (data[shadowpos + 1] & 255) >>> 7);
        
        pos = unloadData(data, pos, s1dhgenv1seg1rate, s1dhgenv2maxseg1onoff);
        pos = unloadDataUnified(data, pos, s1dhgenv2maxseg1onoff, s1dhgenv2seg1rate, new int[] { 6, 0 }, new int[] { 1, 6 } );
        pos = unloadData(data, pos, s1dhgenv2seg1rate, s1dhgenv3maxseg1onoff);
        pos = unloadDataUnified(data, pos, s1dhgenv3maxseg1onoff, s1dhgenv3seg1rate, new int[] { 6, 0 }, new int[] { 1, 6 } );
        pos = unloadData(data, pos, s1dhgenv3seg1rate, s1dhgenv4maxseg1onoff);
        pos = unloadDataUnified(data, pos, s1dhgenv4maxseg1onoff, s1dhgenv4seg1rate, new int[] { 6, 0 }, new int[] { 1, 6 } );
        pos = unloadData(data, pos, s1dhgenv4seg1rate, s1ddfddfonoff);
        pos = unloadData(data, pos, s1ddfddfonoff, s1ddfenvseg1rate, new int[] { 7, 6, 0 }, new int[] { 1, 1, 5 } );
        pos = unloadData(data, pos, s1ddfenvseg1rate, s1ddfenvmaxseg1onoff);
        pos = unloadDataUnified(data, pos, s1ddfenvmaxseg1onoff, s1ddaattackvelodep, new int[] { 6, 0 }, new int[] { 1, 6 } );
        pos = unloadData(data, pos, s1ddaattackvelodep, s1ddaddaonoff);
        pos = unloadData(data, pos, s1ddaddaonoff, s1ddaattackvelrate, new int[] { 7, 0 }, new int[] { 1, 7 } );
        pos = unloadData(data, pos, s1ddaattackvelrate, s1ddaenvseg1modonoff);
        pos = unloadData(data, pos, s1ddaenvseg1modonoff, s1ddaenvmaxseg1onoff, new int[] { 6, 0 }, new int[] { 1, 6 } );
        pos = unloadDataUnified(data, pos, s1ddaenvmaxseg1onoff, lfoshape, new int[] { 6, 0 }, new int[] { 1, 6 } );
        pos++;
        pos++;
                
        // handle lfoshape specially -- it comes in 1--6, we need to change to 0...5
        data[pos]--; 
                
        pos = unloadData(data, pos, lfoshape, dftonoff);
        pos = unloadData(data, pos, dftonoff, dftc0level, new int[] { 7, 0 }, new int[] { 1, 7 } );
        pos = unloadData(data, pos, dftc0level, dftc0level + 10);
        pos++;

        return PARSE_SUCCEEDED;                 
        }
    
        
    public void sendAllParameters()
        {
        super.sendAllParameters();        

        // we change patch to SID-12 if we're sending in bulk.
        //if (sendKawaiParametersInBulk)
            {
            // for some insane reason, we must pause somewhat AFTER we have written the patch but 
            // BEFORE we change the patch to SID-12 or else it won't get
            // properly loaded into the patch.  I cannot explain it.  And it's a lot!
                        
            simplePause(400);  // think this is the right amount -- 300 won't cut it

            Model tempModel = new Model();
            tempModel.set("bank", 3);
            tempModel.set("number", 11);
            changePatch(tempModel);
            simplePause(getPauseAfterChangePatch());
            }
        }

    public byte[] emit(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        if (tempModel == null)
            tempModel = getModel();

        byte[] data = new byte[492];
                
        String name = model.get("name", "INIT    ") + "          ";

        // The K5 is riddled with byte-mangling.  :-(
        
        // Name ...
        
        for(int i = 0; i < 8; i++)
            data[i] = (byte)(name.charAt(i));
        
        int pos = 8;
        
        // Pre-DHG Parameters...

        pos = loadData(data, pos, volume, s1basicpedalassign);
        pos = loadData(data, pos, s1basicpedalassign, portamentosw, new int[] { 4, 0 }, new int[] { 4, 4 } );
        pos = loadData(data, pos, portamentosw, mode, new int[] { 7, 0 }, new int[] { 1, 6 } );
        pos = loadData(data, pos, mode, s1dfgcoarse, new int[] { 2, 0 }, new int[] { 1, 2 } );
        pos = loadData(data, pos, s1dfgcoarse, s1dfgkey);
        pos = loadData(data, pos, s1dfgkey, s1dfgenvdep, new int[] { 7, 0 }, new int[] { 1, 7 } );
        pos = loadData(data, pos, s1dfgenvdep, s1dfgenvloop);
        pos = loadData(data, pos, s1dfgenvloop, s1dfgenvrateseg1, new int[] { 7, 0 }, new int[] { 1, 7 } );
        pos = loadData(data, pos, s1dfgenvrateseg1, s1dhgvelodep);  // in fact it just goes to the harmonics
        
        // Harmonics levels...
                
        int x = 0;
        for( ; x < 63 * 2; x++)
            {
            data[pos++] = (byte)model.get(HARMONICS_PARAMETERS[x], 0);
            }
                
        // Harmonics mod and env sel...
                
        for( ; x < 63 * 2 + 62 * 2; x += 2)
            {
            int defaultenv2 = model.get(HARMONICS_PARAMETERS[x + 0] + "-env", 0);
            int mod2 = model.get(HARMONICS_PARAMETERS[x + 0], 0) == 0 ? 0 : 1;
            int env2 = model.get(HARMONICS_PARAMETERS[x + 0], 0) == 0 ? defaultenv2 : (model.get(HARMONICS_PARAMETERS[x + 0], 0) - 1);
            int defaultenv1 = model.get(HARMONICS_PARAMETERS[x + 1] + "-env", 0);
            int mod1 = model.get(HARMONICS_PARAMETERS[x + 1], 0) == 0 ? 0 : 1;
            int env1 = model.get(HARMONICS_PARAMETERS[x + 1], 0) == 0 ? defaultenv1 : (model.get(HARMONICS_PARAMETERS[x + 1], 0) - 1);
                        
            data[pos++] = (byte)((mod2 << 7) | (env2 << 4) | (mod1 << 3) | (env1 << 0) );
            }

        int defaultenv2 = model.get(HARMONICS_PARAMETERS[x + 0] + "-env", 0);
        int mod2 = model.get(HARMONICS_PARAMETERS[x + 0], 0) == 0 ? 0 : 1;
        int env2 = model.get(HARMONICS_PARAMETERS[x + 0], 0) == 0 ? defaultenv2 : (model.get(HARMONICS_PARAMETERS[x + 0], 0) - 1);
        int defaultenv1 = model.get(HARMONICS_PARAMETERS[x + 1] + "-env", 0);
        int mod1 = model.get(HARMONICS_PARAMETERS[x + 1], 0) == 0 ? 0 : 1;
        int env1 = model.get(HARMONICS_PARAMETERS[x + 1], 0) == 0 ? defaultenv1 : (model.get(HARMONICS_PARAMETERS[x + 1], 0) - 1);

        data[pos++] = (byte)( (mod2 << 3) | (env2 << 0) | 48);  // the 48 is because for no reason 48 is added by the K5
        data[pos++] = (byte)( (mod1 << 3) | (env1 << 0) | 48);  // the 48 is because for no reason 48 is added by the K5
                
        // DHG and Post-DHG parameters...
        
        pos = loadData(data, pos, s1dhgvelodep, s1dhgenv1onoff);
        pos = loadData(data, pos, s1dhgenv1onoff, s1dhgmodonoff, new int[] { 7, 0 }, new int[] { 1, 5 } );
        pos = loadData(data, pos, s1dhgmodonoff, s1dhgrangefrom, new int[] { 7, 0 }, new int[] { 1, 2 } );
        pos = loadData(data, pos, s1dhgrangefrom, s1dhgoddmodonoff);
        pos = loadData(data, pos, s1dhgoddmodonoff, s1dhgallmodonoff, new int[] { 7, 4, 3, 0 }, new int[] { 1, 2, 1, 2 } );
        pos = loadData(data, pos, s1dhgallmodonoff, s1dhgangle, new int[] { 7, 4 }, new int[] { 1, 2 } );
        pos = loadData(data, pos, s1dhgangle, s1dhgshadowonoff);

        int shadowpos = pos;

        pos = loadDataUnified(data, pos, s1dhgenv1maxseg1onoff, s1dhgenv1seg1rate, new int[] { 6, 0 }, new int[] { 1, 6 }, new int[] { s1dhgshadowonoff, s2dhgshadowonoff } );
        
        // handle shadow specially
        data[shadowpos] = (byte)(data[shadowpos] | (model.get("s1dhgshadowonoff", 0) << 7));
        data[shadowpos + 1] = (byte)(data[shadowpos + 1] | (model.get("s2dhgshadowonoff", 0) << 7));
        
        pos = loadData(data, pos, s1dhgenv1seg1rate, s1dhgenv2maxseg1onoff);
        pos = loadDataUnified(data, pos, s1dhgenv2maxseg1onoff, s1dhgenv2seg1rate, new int[] { 6, 0 }, new int[] { 1, 6 } );
        pos = loadData(data, pos, s1dhgenv2seg1rate, s1dhgenv3maxseg1onoff);
        pos = loadDataUnified(data, pos, s1dhgenv3maxseg1onoff, s1dhgenv3seg1rate, new int[] { 6, 0 }, new int[] { 1, 6 } );
        pos = loadData(data, pos, s1dhgenv3seg1rate, s1dhgenv4maxseg1onoff);
        pos = loadDataUnified(data, pos, s1dhgenv4maxseg1onoff, s1dhgenv4seg1rate, new int[] { 6, 0 }, new int[] { 1, 6 } );
        pos = loadData(data, pos, s1dhgenv4seg1rate, s1ddfddfonoff);
        pos = loadData(data, pos, s1ddfddfonoff, s1ddfenvseg1rate, new int[] { 7, 6, 0 }, new int[] { 1, 1, 5 } );
        pos = loadData(data, pos, s1ddfenvseg1rate, s1ddfenvmaxseg1onoff);
        pos = loadDataUnified(data, pos, s1ddfenvmaxseg1onoff, s1ddaattackvelodep, new int[] { 6, 0 }, new int[] { 1, 6 } );
        pos = loadData(data, pos, s1ddaattackvelodep, s1ddaddaonoff);
        pos = loadData(data, pos, s1ddaddaonoff, s1ddaattackvelrate, new int[] { 7, 0 }, new int[] { 1, 7 } );
        pos = loadData(data, pos, s1ddaattackvelrate, s1ddaenvseg1modonoff);
        pos = loadData(data, pos, s1ddaenvseg1modonoff, s1ddaenvmaxseg1onoff, new int[] { 6, 0 }, new int[] { 1, 6 } );
        pos = loadDataUnified(data, pos, s1ddaenvmaxseg1onoff, lfoshape, new int[] { 6, 0 }, new int[] { 1, 6 } );
        data[pos++] = 0;
        data[pos++] = 0;

        int lfopos = pos;
        pos = loadData(data, pos, lfoshape, dftonoff);

        // handle lfoshape specially -- it's store as 0...5, we need to change to 1...6
        data[lfopos]++; 
                
        pos = loadData(data, pos, dftonoff, dftc0level, new int[] { 7, 0 }, new int[] { 1, 7 } );
        pos = loadData(data, pos, dftc0level, dftc0level + 10);
        data[pos++] = 0;
                
        /// Compute Kawai's crazy Checksum
                
        int sum = 0;
        for(int i = 0; i < data.length; i += 2)
            {
            sum += (((data[i + 1] & 0xFF) << 8) | (data[i] & 0xFF));
            }
                
        sum = sum & 0xFFFF;
        sum = (0x5A3C - sum) & 0xFFFF;

        data[pos++] = (byte)(sum & 0xFF);
        data[pos++] = (byte)((sum >>> 8) & 0xFF);
                
        // Load payload

        byte[] result = new byte[EXPECTED_SYSEX_LENGTH];
        result[0] = (byte)0xF0;
        result[1] = (byte)0x40;
        result[2] = (byte)getChannelOut();
        result[3] = (byte)0x20;
        result[4] = (byte)0x00;
        result[5] = (byte)0x02;
        result[6] = (byte)0x00;
        result[7] = (byte)(tempModel.get("bank") * 12 + (tempModel.get("number")));
        
        if (toWorkingMemory) // we're gonna write to SID-12 instead
            result[7] = (byte)(47);
                
        int v = 8;
        for(int i = 0; i < pos; i++)
            {
            result[v++] = (byte)((data[i] & 0xFF) >>> 4);
            result[v++] = (byte)(data[i] & 0xF);
            }
        result[v] = (byte)0xF7;
        
        return result;
        }


    public byte[] requestDump(Model tempModel)
        {
        return new byte[] 
            { 
            (byte)0xF0, 
            (byte)0x40, 
            (byte)getChannelOut(), 
            (byte)0x00, 
            (byte)0x00, 
            (byte)0x02,
            (byte)0x00,  // single
            (byte)(tempModel.get("bank") * 12 + (tempModel.get("number"))),
            (byte)0xF7
            };
        }
    
    public static boolean recognize(byte[] data)
        {
        return ((data.length == EXPECTED_SYSEX_LENGTH) &&
            (data[0] == (byte)0xF0) &&
            (data[1] == (byte)0x40) &&
            ((data[3] == (byte)0x20) || (data[3] == (byte)0x21)) &&
            (data[4] == (byte)0x00) &&
            (data[5] == (byte)0x02) &&  // K5
            (data[6] == (byte)0x00));
        }
        

    public static final int EXPECTED_SYSEX_LENGTH = 993;        
    
    boolean isLegalCharacter(char c)
        {
        for(int i = 0; i < LEGAL_CHARACTERS.length; i++)
            {
            if (c == LEGAL_CHARACTERS[i])
                return true;
            }
        return false;
        }
    
    public static final int MAXIMUM_NAME_LENGTH = 8;
    public String revisePatchName(String name)
        {
        name = super.revisePatchName(name);  // trim first time
        if (name.length() > MAXIMUM_NAME_LENGTH)
            name = name.substring(0, MAXIMUM_NAME_LENGTH);
        name = name.trim();
        name = name.toUpperCase();
        
        StringBuffer nameb = new StringBuffer(name);                            
        for(int i = 0 ; i < nameb.length(); i++)
            {
            if (!isLegalCharacter(nameb.charAt(i)))
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

        String nm = model.get("name", "INIT    ");
        String newnm = revisePatchName(nm);
        if (!nm.equals(newnm))
            model.set("name", newnm);
        }
        
    public static String getSynthName() { return "Kawai K5/K5m"; }
    
    public String getPatchName(Model model) { return model.get("name", "INIT    "); }

    public int getPauseAfterChangePatch() { return 10; }
    public int getPauseAfterSendOneParameter() { return 30; }  // Sad, needs 30ms
 
    public void changePatch(Model tempModel)
        {
        byte BB = (byte)tempModel.get("bank");
        byte NN = (byte)tempModel.get("number");
        
        /// NOTE: the K5 can't change to multi-mode with a program change, you have to send a special
        /// sysex command.  But since we're doing single-mode here we're okay.
        
        int PC = (BB * 12 + NN);
        try 
            {
            tryToSendMIDI(new ShortMessage(ShortMessage.PROGRAM_CHANGE, getChannelOut(), PC, 0));
            }
        catch (Exception e) { e.printStackTrace(); }
        }
    
    public Model getNextPatchLocation(Model model)
        {
        int bank = model.get("bank");
        int number = model.get("number");
        
        number++;
        if (number >= 12)
            {
            bank++;
            number = 0;
            if (bank >= 4)
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
        
        return BANKS[model.get("bank")] + "-" +  (model.get("number") + 1);
        }
        

    HashMap parametersToIndex = new HashMap();

    public static final String[] parameters = new String[]
    {
/// BASIC
    "name1",                                        // 0
    "name2",
    "name3",
    "name4",
    "name5",
    "name6",
    "name7",
    "name8",
    "volume",
    "balance",
    "s1basicdelay",                         // 10
    "s2basicdelay",
    "s1basicpedaldep",
    "s2basicpedaldep",
    "s1basicwheeldep",
    "s2basicwheeldep",
    "s1basicpedalassign",
    "s1basicwheelassign",
    "s2basicpedalassign",
    "s2basicwheelassign",
    "portamentosw",                         // 20 
    "portamentospeed",
    "mode",
    "picmode",
        
/// DFG
        
    "s1dfgcoarse",
    "s2dfgcoarse",
    "s1dfgfine",
    "s2dfgfine",
    "s1dfgkey",
    "s1dfgfixno",
    "s2dfgkey",                                     // 30
    "s2dfgfixno",
    "s1dfgenvdep",
    "s2dfgenvdep",
    "s1dfgprsdep",
    "s2dfgprsdep",
    "s1dfgbenderdep",
    "s2dfgbenderdep",
    "s1dfgveloenvdep",
    "s2dfgveloenvdep",
    "s1dfglfodep",                          // 40
    "s2dfglfodep",
    "s1dfgpreslfodep",
    "s2dfgpreslfodep",
    "s1dfgenvloop",
    "s1dfgenvrateseg1",
    "s2dfgenvloop",
    "s2dfgenvrateseg1",
    "s1dfgenvrateseg2",
    "s2dfgenvrateseg2",
    "s1dfgenvrateseg3",                             // 50
    "s2dfgenvrateseg3",
    "s1dfgenvrateseg4",
    "s2dfgenvrateseg4",
    "s1dfgenvrateseg5",
    "s2dfgenvrateseg5",
    "s1dfgenvrateseg6",
    "s2dfgenvrateseg6",
    "s1dfgenvlevelseg1",
    "s2dfgenvlevelseg1",
    "s1dfgenvlevelseg2",                    // 60
    "s2dfgenvlevelseg2",
    "s1dfgenvlevelseg3",
    "s2dfgenvlevelseg3",
    "s1dfgenvlevelseg4",
    "s2dfgenvlevelseg4",
    "s1dfgenvlevelseg5",
    "s2dfgenvlevelseg5",
    "s1dfgenvlevelseg6",
    "s2dfgenvlevelseg6",
        
/// DHG
/// Harmonics not listed, but then...
        
    "s1dhgvelodep",                                 // 70
    "s2dhgvelodep",
    "s1dhgpresdep",
    "s2dhgpresdep",
    "s1dhgksdep",
    "s2dhgksdep",
    "s1dhglfodep",
    "s2dhglfodep",
    "s1dhgenv1onoff",                               // 80
    "s1dhgenv1moddepth",    // mistake,
    "s2dhgenv1onoff",
    "s2dhgenv1moddepth",    // mistake,
    "s1dhgenv2onoff",
    "s1dhgenv2moddepth",    // mistake,
    "s2dhgenv2onoff",
    "s2dhgenv2moddepth",    // mistake,
    "s1dhgenv3onoff",
    "s1dhgenv3moddepth",    // mistake,
    "s2dhgenv3onoff",
    "s2dhgenv3moddepth",    // mistake,
    "s1dhgenv4onoff",                               // 90
    "s1dhgenv4moddepth",    // mistake,
    "s2dhgenv4onoff",
    "s2dhgenv4moddepth",    // mistake,
    "s1dhgmodonoff",
    "s1dhgharmsel",
    "s2dhgmodonoff",
    "s2dhgharmsel",
    "s1dhgrangefrom",
    "s2dhgrangefrom",
    "s1dhgrangeto",                         // 100
    "s2dhgrangeto",
    "s1dhgoddmodonoff",
    "s1dhgoddenv",
    "s1dhgevenmodonoff",    // this must be an error -- was 
    "s1dhgevenenv",
    "s2dhgoddmodonoff",
    "s2dhgoddenv",
    "s2dhgevenmodonoff",    // this must be an error -- was 
    "s2dhgevenenv",
    "s1dhgoctavemodonoff",                  // 110
    "s1dhgoctaveenv",
    "s1dhgfifthmodonoff",
    "s1dhgfifthenv",
    "s2dhgoctavemodonoff",
    "s2dhgoctaveenv",
    "s2dhgfifthmodonoff",
    "s2dhgfifthenv",
    "s1dhgallmodonoff",
    "s1dhgallenv",                          // 120
    "s2dhgallmodonoff",
    "s2dhgallenv",
    "s1dhgangle",
    "s2dhgangle",
    "s1dhgharm",
    "s2dhgharm",
    "s1dhgshadowonoff",     // mistake,
    "s1dhgenv1maxsegonoff", // mistake,
    "s1dhgenv1seg1level",
    "s2dhgshadowonoff",     // mistake,
    "s2dhgenv1maxsegonoff", // mistake,                     130
    "s2dhgenv1seg1level",
    "s1dhgenv1maxsegonoff", // mistake,
    "s1dhgenv1seg2level",
    "s2dhgenv1maxsegonoff", // mistake,
    "s2dhgenv1seg2level",
    "s1dhgenv1maxsegonoff", // mistake,
    "s1dhgenv1seg3level",
    "s2dhgenv1maxsegonoff", // mistake,
    "s2dhgenv1seg3level",
    "s1dhgenv1maxsegonoff", // mistake,                     140
    "s1dhgenv1seg4level",
    "s2dhgenv1maxsegonoff", // mistake,
    "s2dhgenv1seg4level",
    "s1dhgenv1maxsegonoff", // mistake,
    "s1dhgenv1seg5level",
    "s2dhgenv1maxsegonoff", // mistake,
    "s2dhgenv1seg5level",
    "s1dhgenv1maxsegonoff", // mistake,
    "s1dhgenv1seg6level",
    "s2dhgenv1maxsegonoff", // mistake,                     // 150
    "s2dhgenv1seg6level",
    "s1dhgenv1seg1rate",                                    
    "s2dhgenv1seg1rate",
    "s1dhgenv1seg2rate",
    "s2dhgenv1seg2rate",
    "s1dhgenv1seg3rate",
    "s2dhgenv1seg3rate",
    "s1dhgenv1seg4rate",
    "s2dhgenv1seg4rate",
    "s1dhgenv1seg5rate",                                    // 160
    "s2dhgenv1seg5rate",
    "s1dhgenv1seg6rate",
    "s2dhgenv1seg6rate",
    "s1dhgenv2maxsegonoff", // mistake,
    "s1dhgenv2seg1level",
    "s2dhgenv2maxsegonoff", // mistake,
    "s2dhgenv2seg1level",
    "s1dhgenv2maxsegonoff", // mistake,
    "s1dhgenv2seg2level",
    "s2dhgenv2maxsegonoff", // mistake,             // 170
    "s2dhgenv2seg2level",
    "s1dhgenv2maxsegonoff", // mistake,
    "s1dhgenv2seg3level",
    "s2dhgenv2maxsegonoff", // mistake,
    "s2dhgenv2seg3level",
    "s1dhgenv2maxsegonoff", // mistake,
    "s1dhgenv2seg4level",
    "s2dhgenv2maxsegonoff", // mistake,
    "s2dhgenv2seg4level",
    "s1dhgenv2maxsegonoff", // mistake,             // 180
    "s1dhgenv2seg5level",
    "s2dhgenv2maxsegonoff", // mistake,
    "s2dhgenv2seg5level",
    "s1dhgenv2maxsegonoff", // mistake,
    "s1dhgenv2seg6level",
    "s2dhgenv2maxsegonoff", // mistake,
    "s2dhgenv2seg6level",
    "s1dhgenv2seg1rate",
    "s2dhgenv2seg1rate",
    "s1dhgenv2seg2rate",                                    // 190
    "s2dhgenv2seg2rate",
    "s1dhgenv2seg3rate",
    "s2dhgenv2seg3rate",
    "s1dhgenv2seg4rate",
    "s2dhgenv2seg4rate",
    "s1dhgenv2seg5rate",
    "s2dhgenv2seg5rate",
    "s1dhgenv2seg6rate",
    "s2dhgenv2seg6rate",
    "s1dhgenv3maxsegonoff", // mistake,             // 200
    "s1dhgenv3seg1level",
    "s2dhgenv3maxsegonoff", // mistake,
    "s2dhgenv3seg1level",
    "s1dhgenv3maxsegonoff", // mistake,
    "s1dhgenv3seg2level",
    "s2dhgenv3maxsegonoff", // mistake,
    "s2dhgenv3seg2level",
    "s1dhgenv3maxsegonoff", // mistake,
    "s1dhgenv3seg3level",
    "s2dhgenv3maxsegonoff", // mistake,                     // 210
    "s2dhgenv3seg3level",
    "s1dhgenv3maxsegonoff", // mistake,
    "s1dhgenv3seg4level",
    "s2dhgenv3maxsegonoff", // mistake,
    "s2dhgenv3seg4level",
    "s1dhgenv3maxsegonoff", // mistake,
    "s1dhgenv3seg5level",
    "s2dhgenv3maxsegonoff", // mistake,
    "s2dhgenv3seg5level",
    "s1dhgenv3maxsegonoff", // mistake,                     // 220
    "s1dhgenv3seg6level",
    "s2dhgenv3maxsegonoff", // mistake,
    "s2dhgenv3seg6level",
    "s1dhgenv3seg1rate",
    "s2dhgenv3seg1rate",
    "s1dhgenv3seg2rate",
    "s2dhgenv3seg2rate",
    "s1dhgenv3seg3rate",
    "s2dhgenv3seg3rate",
    "s1dhgenv3seg4rate",                            // 230
    "s2dhgenv3seg4rate",
    "s1dhgenv3seg5rate",
    "s2dhgenv3seg5rate",
    "s1dhgenv3seg6rate",
    "s2dhgenv3seg6rate",
    "s1dhgenv4maxsegonoff", // mistake,
    "s1dhgenv4seg1level",
    "s2dhgenv4maxsegonoff", // mistake,
    "s2dhgenv4seg1level",
    "s1dhgenv4maxsegonoff", // mistake,             // 240
    "s1dhgenv4seg2level",
    "s2dhgenv4maxsegonoff", // mistake,
    "s2dhgenv4seg2level",
    "s1dhgenv4maxsegonoff", // mistake,
    "s1dhgenv4seg3level",
    "s2dhgenv4maxsegonoff", // mistake,
    "s2dhgenv4seg3level",
    "s1dhgenv4maxsegonoff", // mistake,
    "s1dhgenv4seg4level",
    "s2dhgenv4maxsegonoff", // mistake,             // 250
    "s2dhgenv4seg4level",
    "s1dhgenv4maxsegonoff", // mistake,
    "s1dhgenv4seg5level",
    "s2dhgenv4maxsegonoff", // mistake,
    "s2dhgenv4seg5level",
    "s1dhgenv4maxsegonoff", // mistake,
    "s1dhgenv4seg6level",
    "s2dhgenv4maxsegonoff", // mistake,
    "s2dhgenv4seg6level",
    "s1dhgenv4seg1rate",                                            // 260
    "s2dhgenv4seg1rate",
    "s1dhgenv4seg2rate",
    "s2dhgenv4seg2rate",
    "s1dhgenv4seg3rate",
    "s2dhgenv4seg3rate",
    "s1dhgenv4seg4rate",
    "s2dhgenv4seg4rate",
    "s1dhgenv4seg5rate",
    "s2dhgenv4seg5rate",
    "s1dhgenv4seg6rate",                                            // 270
    "s2dhgenv4seg6rate",
        
/// DDF
        
    "s1ddfcutoff",
    "s2ddfcutoff",
    "s1ddfcutoffmod",
    "s2ddfcutoffmod",
    "s1ddfslope",
    "s2ddfslope",
    "s1ddfslopemod",
    "s2ddfslopemod",
    "s1ddfflatlevel",                                       // 280
    "s2ddfflatlevel",
    "s1ddfvelodep",
    "s2ddfvelodep",
    "s1ddfpresdep",
    "s2ddfpresdep",
    "s1ddfksdep",
    "s2ddfksdep",
    "s1ddfenvdep",
    "s2ddfenvdep",
    "s1ddfveloenvdep",                              // 290
    "s2ddfveloenvdep",
    "s1ddfddfonoff",
    "s1ddfddfmodonoff",
    "s1ddflfodep",
    "s2ddfddfonoff",
    "s2ddfddfmodonoff",
    "s2ddflfodep",
    "s1ddfenvseg1rate",
    "s2ddfenvseg1rate",
    "s1ddfenvseg2rate",                             // 300
    "s2ddfenvseg2rate",
    "s1ddfenvseg3rate",
    "s2ddfenvseg3rate",
    "s1ddfenvseg4rate",
    "s2ddfenvseg4rate",
    "s1ddfenvseg5rate",
    "s2ddfenvseg5rate",
    "s1ddfenvseg6rate",
    "s2ddfenvseg6rate",
    "s1ddfenvmaxsegonoff",  // mistake,                     // 310
    "s1ddfenvseg1level",
    "s2ddfenvmaxsegonoff",  // mistake,
    "s2ddfenvseg1level",
    "s1ddfenvmaxsegonoff",  // mistake,
    "s1ddfenvseg2level",
    "s2ddfenvmaxsegonoff",  // mistake,
    "s2ddfenvseg2level",
    "s1ddfenvmaxsegonoff",  // mistake,
    "s1ddfenvseg3level",
    "s2ddfenvmaxsegonoff",  // mistake,                     // 320
    "s2ddfenvseg3level",
    "s1ddfenvmaxsegonoff",  // mistake,
    "s1ddfenvseg4level",
    "s2ddfenvmaxsegonoff",  // mistake,
    "s2ddfenvseg4level",
    "s1ddfenvmaxsegonoff",  // mistake,
    "s1ddfenvseg5level",
    "s2ddfenvmaxsegonoff",  // mistake,
    "s2ddfenvseg5level",
    "s1ddfenvmaxsegonoff",  // mistake,                     // 330  
    "s1ddfenvseg6level",
    "s2ddfenvmaxsegonoff",  // mistake,
    "s2ddfenvseg6level",
        
/// DDA
        
    "s1ddaattackvelodep",   // mistake,
    "s2ddaattackvelodep",   // mistake,
    "s1ddapresdep",
    "s2ddapresdep",
    "s1ddaksdep",
    "s2ddaksdep",
    "s1ddaddaonoff",                                                        // 340
    "s1ddalfodep",
    "s2ddaddaonoff",
    "s2ddalfodep",
    "s1ddaattackvelrate",   // mistake,
    "s2ddaattackvelrate",   // mistake,
    "s1ddareleasevelrate",
    "s2ddareleasevelrate",
    "s1ddaksrate",
    "s2ddaksrate",
    "s1ddaenvseg1modonoff",                                         // 350
    "s1ddaenvseg1rate",
    "s2ddaenvseg1modonoff",
    "s2ddaenvseg1rate",
    "s1ddaenvseg2modonoff",
    "s1ddaenvseg2rate",
    "s2ddaenvseg2modonoff",
    "s2ddaenvseg2rate",
    "s1ddaenvseg3modonoff",
    "s1ddaenvseg3rate",
    "s2ddaenvseg3modonoff",                                         // 360
    "s2ddaenvseg3rate",
    "s1ddaenvseg4modonoff",
    "s1ddaenvseg4rate",
    "s2ddaenvseg4modonoff",
    "s2ddaenvseg4rate",
    "s1ddaenvseg5modonoff",
    "s1ddaenvseg5rate",
    "s2ddaenvseg5modonoff",
    "s2ddaenvseg5rate",
    "s1ddaenvseg6modonoff",                                         // 370
    "s1ddaenvseg6rate",
    "s2ddaenvseg6modonoff",
    "s2ddaenvseg6rate",
    "s1ddaenvseg7modonoff",
    "s1ddaenvseg7rate",
    "s2ddaenvseg7modonoff",
    "s2ddaenvseg7rate",
    "s1ddaenvmaxsegonoff",  // mistake,
    "s1ddaenvseg1level",
    "s2ddaenvmaxsegonoff",  // mistake,                     // 380
    "s2ddaenvseg1level",
    "s1ddaenvmaxsegonoff",  // mistake,
    "s1ddaenvseg2level",
    "s2ddaenvmaxsegonoff",  // mistake,
    "s2ddaenvseg2level",
    "s1ddaenvmaxsegonoff",  // mistake,
    "s1ddaenvseg3level",
    "s2ddaenvmaxsegonoff",  // mistake,
    "s2ddaenvseg3level",
    "s1ddaenvmaxsegonoff",  // mistake,                     // 390
    "s1ddaenvseg4level",
    "s2ddaenvmaxsegonoff",  // mistake,
    "s2ddaenvseg4level",
    "s1ddaenvmaxsegonoff",  // mistake,
    "s1ddaenvseg5level",
    "s2ddaenvmaxsegonoff",  // mistake,
    "s2ddaenvseg5level",
    "s1ddaenvmaxsegonoff",  // mistake,
    "s1ddaenvseg6level",
    "s2ddaenvmaxsegonoff",  // mistake,
    "s2ddaenvseg6level",
        
/// LFO
        
    "lfoshape",
    "lfospeed",
    "lfodelay",
    "lfotrend",
        
/// KS
        
    "s1ksright",
    "s2ksright",
    "s1ksleft",
    "s2ksleft",
    "s1ksbreakpoint",                                                       // 410
    "s2ksbreakpoint",
        
/// DFT
        
    "dftonoff",
    "dftc11level",        // mistake,
    "dftc0level",
    "dftc1level",
    "dftc2level",
    "dftc3level",
    "dftc4level",
    "dftc5level",
    "dftc6level",                                                           // 420
    "dftc7level",
    "dftc8level",
    "dftc9level",
    };

    public static final int[] paramNumbers = new int[]
    {
/// BASIC,
        
    200,
    201,
    202,
    203,
    204,
    205,
    206,
    207,
    210,
    211,
    215,
    218,
    214,
    217,
    232,
    233,
    213,
    230,
    216,
    231,
    234,
    235,
    208,
    236,
        
/// DFG,
        
    0,
    0,
    1,
    1,
    2,
    3,
    2,
    3,
    4,
    4,
    5,
    5,
    6,
    6,
    8,
    8,
    10,
    10,
    12,
    12,
    26,
    14,
    26,
    14,
    15,
    15,
    16,
    16,
    17,
    17,
    18,
    18,
    19,
    19,
    20,
    20,
    21,
    21,
    22,
    22,
    23,
    23,
    24,
    24,
    25,
    25,
        
/// DHG,
/// Harmonics not listed, but then...,
        
    44,
    44,
    45,
    45,
    46,
    46,
    47,
    47,
    48,
    188,
    48,
    188,
    49,
    189,
    49,
    189,
    50,
    190,
    50,
    190,
    51,
    191,
    51,
    191,
    27,
    28,
    27,
    28,
    29,
    29,
    30,
    30,
    32,
    31,
    34,     // the text says this is parameter 33 but I think it may be wrong
    33,     // the text says this is parameter 35 but I think it may be wrong
    32,
    31,
    34,     // the text says this is parameter 33 but I think it may be wrong
    33,     // the text says this is parameter 35 but I think it may be wrong
    36,
    35,
    38,
    37,
    36,
    35,
    38,
    37,
    187,
    186,
    187,
    186,
    39,
    39,
    40,
    40,
    56,
    52,
    63,
    56,
    52,
    63,
    52,
    64,
    52,
    64,
    52,
    65,
    52,
    65,
    52,
    66,
    52,
    66,
    52,
    67,
    52,
    67,
    52,
    68,
    52,
    68,
    57,
    57,
    58,
    58,
    59,
    59,
    60,
    60,
    61,
    61,
    62,
    62,
    53,
    75,
    53,
    75,
    53,
    76,
    53,
    76,
    53,
    77,
    53,
    77,
    53,
    78,
    53,
    78,
    53,
    79,
    53,
    79,
    53,
    80,
    53,
    80,
    69,
    69,
    70,
    70,
    71,
    71,
    72,
    72,
    73,
    73,
    74,
    74,
    54,
    87,
    54,
    87,
    54,
    88,
    54,
    88,
    54,
    89,
    54,
    89,
    54,
    90,
    54,
    90,
    54,
    91,
    54,
    91,
    54,
    92,
    54,
    92,
    81,
    81,
    82,
    82,
    83,
    83,
    84,
    84,
    85,
    85,
    86,
    86,
    55,
    99,
    55,
    99,
    55,
    100,
    55,
    100,
    55,
    101,
    55,
    101,
    55,
    102,
    55,
    102,
    55,
    103,
    55,
    103,
    55,
    104,
    55,
    104,
    93,
    93,
    94,
    94,
    95,
    95,
    96,
    96,
    97,
    97,
    98,
    98,
        
/// DDF,
        
    107,
    107,
    111,
    111,
    108,
    108,
    112,
    112,
    109,
    109,
    113,
    113,
    114,
    114,
    115,
    115,
    116,
    116,
    118,
    118,
    105,
    106,
    117,
    105,
    106,
    117,
    119,
    119,
    120,
    120,
    121,
    121,
    122,
    122,
    123,
    123,
    124,
    124,
    131,
    125,
    131,
    125,
    131,
    126,
    131,
    126,
    131,
    127,
    131,
    127,
    131,
    128,
    131,
    128,
    131,
    129,
    131,
    129,
    131,
    130,
    131,
    130,
        
/// DDA,
        
    133,
    133,
    134,
    134,
    135,
    135,
    132,
    136,
    132,
    136,
    137,
    137,
    138,
    138,
    139,
    139,
    140,
    147,
    140,
    147,
    141,
    148,
    141,
    148,
    142,
    149,
    142,
    149,
    143,
    150,
    143,
    150,
    144,
    151,
    144,
    151,
    145,
    152,
    145,
    152,
    146,
    153,
    146,
    153,
    161,
    154,
    161,
    154,
    161,
    155,
    161,
    155,
    161,
    156,
    161,
    156,
    161,
    157,
    161,
    157,
    161,
    158,
    161,
    158,
    161,
    159,
    161,
    159,
        
/// LFO,
        
    174,
    175,
    176,
    177,
        
/// KS,
        
    180,
    180,
    178,
    178,
    179,
    179,
        
/// DFT,
        
    162,
    163,
    164,
    165,
    166,
    167,
    168,
    169,
    170,
    171,
    172,
    173,
    };


    public static final String[] HARMONICS_PARAMETERS = new String[]
    {
    "s1dhgharm1level",
    "s2dhgharm1level",
    "s1dhgharm2level",
    "s2dhgharm2level",
    "s1dhgharm3level",
    "s2dhgharm3level",
    "s1dhgharm4level",
    "s2dhgharm4level",
    "s1dhgharm5level",
    "s2dhgharm5level",
    "s1dhgharm6level",
    "s2dhgharm6level",
    "s1dhgharm7level",
    "s2dhgharm7level",
    "s1dhgharm8level",
    "s2dhgharm8level",
    "s1dhgharm9level",
    "s2dhgharm9level",
    "s1dhgharm10level",
    "s2dhgharm10level",
    "s1dhgharm11level",
    "s2dhgharm11level",
    "s1dhgharm12level",
    "s2dhgharm12level",
    "s1dhgharm13level",
    "s2dhgharm13level",
    "s1dhgharm14level",
    "s2dhgharm14level",
    "s1dhgharm15level",
    "s2dhgharm15level",
    "s1dhgharm16level",
    "s2dhgharm16level",
    "s1dhgharm17level",
    "s2dhgharm17level",
    "s1dhgharm18level",
    "s2dhgharm18level",
    "s1dhgharm19level",
    "s2dhgharm19level",
    "s1dhgharm20level",
    "s2dhgharm20level",
    "s1dhgharm21level",
    "s2dhgharm21level",
    "s1dhgharm22level",
    "s2dhgharm22level",
    "s1dhgharm23level",
    "s2dhgharm23level",
    "s1dhgharm24level",
    "s2dhgharm24level",
    "s1dhgharm25level",
    "s2dhgharm25level",
    "s1dhgharm26level",
    "s2dhgharm26level",
    "s1dhgharm27level",
    "s2dhgharm27level",
    "s1dhgharm28level",
    "s2dhgharm28level",
    "s1dhgharm29level",
    "s2dhgharm29level",
    "s1dhgharm30level",
    "s2dhgharm30level",
    "s1dhgharm31level",
    "s2dhgharm31level",
    "s1dhgharm32level",
    "s2dhgharm32level",
    "s1dhgharm33level",
    "s2dhgharm33level",
    "s1dhgharm34level",
    "s2dhgharm34level",
    "s1dhgharm35level",
    "s2dhgharm35level",
    "s1dhgharm36level",
    "s2dhgharm36level",
    "s1dhgharm37level",
    "s2dhgharm37level",
    "s1dhgharm38level",
    "s2dhgharm38level",
    "s1dhgharm39level",
    "s2dhgharm39level",
    "s1dhgharm40level",
    "s2dhgharm40level",
    "s1dhgharm41level",
    "s2dhgharm41level",
    "s1dhgharm42level",
    "s2dhgharm42level",
    "s1dhgharm43level",
    "s2dhgharm43level",
    "s1dhgharm44level",
    "s2dhgharm44level",
    "s1dhgharm45level",
    "s2dhgharm45level",
    "s1dhgharm46level",
    "s2dhgharm46level",
    "s1dhgharm47level",
    "s2dhgharm47level",
    "s1dhgharm48level",
    "s2dhgharm48level",
    "s1dhgharm49level",
    "s2dhgharm49level",
    "s1dhgharm50level",
    "s2dhgharm50level",
    "s1dhgharm51level",
    "s2dhgharm51level",
    "s1dhgharm52level",
    "s2dhgharm52level",
    "s1dhgharm53level",
    "s2dhgharm53level",
    "s1dhgharm54level",
    "s2dhgharm54level",
    "s1dhgharm55level",
    "s2dhgharm55level",
    "s1dhgharm56level",
    "s2dhgharm56level",
    "s1dhgharm57level",
    "s2dhgharm57level",
    "s1dhgharm58level",
    "s2dhgharm58level",
    "s1dhgharm59level",
    "s2dhgharm59level",
    "s1dhgharm60level",
    "s2dhgharm60level",
    "s1dhgharm61level",
    "s2dhgharm61level",
    "s1dhgharm62level",
    "s2dhgharm62level",
    "s1dhgharm63level",
    "s2dhgharm63level",
    "s1dhgharm2envselmodyn",
    "s1dhgharm1envselmodyn",
    "s2dhgharm2envselmodyn",
    "s2dhgharm1envselmodyn",
    "s1dhgharm4envselmodyn",
    "s1dhgharm3envselmodyn",
    "s2dhgharm4envselmodyn",
    "s2dhgharm3envselmodyn",
    "s1dhgharm6envselmodyn",
    "s1dhgharm5envselmodyn",
    "s2dhgharm6envselmodyn",
    "s2dhgharm5envselmodyn",
    "s1dhgharm8envselmodyn",
    "s1dhgharm7envselmodyn",
    "s2dhgharm8envselmodyn",
    "s2dhgharm7envselmodyn",
    "s1dhgharm10envselmodyn",
    "s1dhgharm9envselmodyn",
    "s2dhgharm10envselmodyn",
    "s2dhgharm9envselmodyn",
    "s1dhgharm12envselmodyn",
    "s1dhgharm11envselmodyn",
    "s2dhgharm12envselmodyn",
    "s2dhgharm11envselmodyn",
    "s1dhgharm14envselmodyn",
    "s1dhgharm13envselmodyn",
    "s2dhgharm14envselmodyn",
    "s2dhgharm13envselmodyn",
    "s1dhgharm16envselmodyn",
    "s1dhgharm15envselmodyn",
    "s2dhgharm16envselmodyn",
    "s2dhgharm15envselmodyn",
    "s1dhgharm18envselmodyn",
    "s1dhgharm17envselmodyn",
    "s2dhgharm18envselmodyn",
    "s2dhgharm17envselmodyn",
    "s1dhgharm20envselmodyn",
    "s1dhgharm19envselmodyn",
    "s2dhgharm20envselmodyn",
    "s2dhgharm19envselmodyn",
    "s1dhgharm22envselmodyn",
    "s1dhgharm21envselmodyn",
    "s2dhgharm22envselmodyn",
    "s2dhgharm21envselmodyn",
    "s1dhgharm24envselmodyn",
    "s1dhgharm23envselmodyn",
    "s2dhgharm24envselmodyn",
    "s2dhgharm23envselmodyn",
    "s1dhgharm26envselmodyn",
    "s1dhgharm25envselmodyn",
    "s2dhgharm26envselmodyn",
    "s2dhgharm25envselmodyn",
    "s1dhgharm28envselmodyn",
    "s1dhgharm27envselmodyn",
    "s2dhgharm28envselmodyn",
    "s2dhgharm27envselmodyn",
    "s1dhgharm30envselmodyn",
    "s1dhgharm29envselmodyn",
    "s2dhgharm30envselmodyn",
    "s2dhgharm29envselmodyn",
    "s1dhgharm32envselmodyn",
    "s1dhgharm31envselmodyn",
    "s2dhgharm32envselmodyn",
    "s2dhgharm31envselmodyn",
    "s1dhgharm34envselmodyn",
    "s1dhgharm33envselmodyn",
    "s2dhgharm34envselmodyn",
    "s2dhgharm33envselmodyn",
    "s1dhgharm36envselmodyn",
    "s1dhgharm35envselmodyn",
    "s2dhgharm36envselmodyn",
    "s2dhgharm35envselmodyn",
    "s1dhgharm38envselmodyn",
    "s1dhgharm37envselmodyn",
    "s2dhgharm38envselmodyn",
    "s2dhgharm37envselmodyn",
    "s1dhgharm40envselmodyn",
    "s1dhgharm39envselmodyn",
    "s2dhgharm40envselmodyn",
    "s2dhgharm39envselmodyn",
    "s1dhgharm42envselmodyn",
    "s1dhgharm41envselmodyn",
    "s2dhgharm42envselmodyn",
    "s2dhgharm41envselmodyn",
    "s1dhgharm44envselmodyn",
    "s1dhgharm43envselmodyn",
    "s2dhgharm44envselmodyn",
    "s2dhgharm43envselmodyn",
    "s1dhgharm46envselmodyn",
    "s1dhgharm45envselmodyn",
    "s2dhgharm46envselmodyn",
    "s2dhgharm45envselmodyn",
    "s1dhgharm48envselmodyn",
    "s1dhgharm47envselmodyn",
    "s2dhgharm48envselmodyn",
    "s2dhgharm47envselmodyn",
    "s1dhgharm50envselmodyn",
    "s1dhgharm49envselmodyn",
    "s2dhgharm50envselmodyn",
    "s2dhgharm49envselmodyn",
    "s1dhgharm52envselmodyn",
    "s1dhgharm51envselmodyn",
    "s2dhgharm52envselmodyn",
    "s2dhgharm51envselmodyn",
    "s1dhgharm54envselmodyn",
    "s1dhgharm53envselmodyn",
    "s2dhgharm54envselmodyn",
    "s2dhgharm53envselmodyn",
    "s1dhgharm56envselmodyn",
    "s1dhgharm55envselmodyn",
    "s2dhgharm56envselmodyn",
    "s2dhgharm55envselmodyn",
    "s1dhgharm58envselmodyn",
    "s1dhgharm57envselmodyn",
    "s2dhgharm58envselmodyn",
    "s2dhgharm57envselmodyn",
    "s1dhgharm60envselmodyn",
    "s1dhgharm59envselmodyn",
    "s2dhgharm60envselmodyn",
    "s2dhgharm59envselmodyn",
    "s1dhgharm62envselmodyn",
    "s1dhgharm61envselmodyn",
    "s2dhgharm62envselmodyn",
    "s2dhgharm61envselmodyn",
    "s1dhgharm63envselmodyn",
    "s2dhgharm63envselmodyn",
    "s2dhgharm63envselmodyn"
    };

    public int loadData(byte[] data, int pos, int start, int end)
        {
        for(int i = start; i < end; i++)
            data[pos++] = (byte)model.get(parameters[i], 0); 
        return pos;
        }

    public int loadData(byte[] data, int pos, int start, int end, int[] bit, int[] len)
        {
        int i = start;
        while(i < end)
            {
            data[pos] = 0;
            for(int b = 0; b < bit.length; b++)
                {
                int result = model.get(parameters[i], 0);
                int stub = (result & (255 >>> (8 - len[b])));
                data[pos] = (byte)(data[pos] | (stub << bit[b]));
                i++;
                }
            pos++;
            }
        return pos;
        }

    public int loadDataUnified(byte[] data, int pos, int start, int end, int[] bit, int[] len)
        {
        return loadDataUnified(data, pos, start, end, bit, len, new int[0]);
        }

    public int loadDataUnified(byte[] data, int pos, int start, int end, int[] bit, int[] len, int[] skip)
        {
        int count = 0;
        int i = start;
        while(i < end)
            {
            boolean cont = false;
            for(int j = 0; j < skip.length; j++)
                {
                if (i == skip[j]) { cont = true; break; }
                }
            if (cont) { i++; continue; }

            data[pos] = 0;
            for(int b = 0; b < bit.length; b++)
                {
                int result = (b == 0 ? 
                    (model.get(parameters[i], 0) == (count / 2 + 1) ? 1 : 0) :              // the + 1 is beacause maxsegon/off is 0 if "no one is on"
                    (model.get(parameters[i], 0)));
                int stub = (result & (255 >>> (8 - len[b])));
                data[pos] = (byte)(data[pos] | (stub << bit[b]));
                i++;
                }
            count++;
            pos++;
            }
        return pos;
        }
 
 
    public int unloadData(byte[] data, int pos, int start, int end)
        {
        for(int i = start; i < end; i++)
            {
            model.set(parameters[i], data[pos++]);
            }
        return pos;
        }

    public int unloadData(byte[] data, int pos, int start, int end, int[] bit, int[] len)
        {
        int i = start;
        while(i < end)
            {
            for(int b = 0; b < bit.length; b++)
                {
                model.set(parameters[i], (data[pos] >>> bit[b]) & (255 >>> (8 - len[b])));
                i++;
                }
            pos++;
            }
        return pos;
        }

    public int unloadDataUnified(byte[] data, int pos, int start, int end, int[] bit, int[] len)
        {
        return unloadDataUnified(data, pos, start, end, bit, len, new int[0]);
        }

    public int unloadDataUnified(byte[] data, int pos, int start, int end, int[] bit, int[] len, int[] skip)
        {
        int count = 0;
        int i = start;
        while(i < end)
            {
            boolean cont = false;
            for(int j = 0; j < skip.length; j++)
                {
                if (i == skip[j]) { cont = true; break; }
                }
            if (cont) { i++; continue; }
                        
            for(int b = 0; b < bit.length; b++)
                {
                int val = (data[pos] >>> bit[b]) & (255 >>> (8 - len[b]));
                if (b == 0)
                    {
                    if (val == 1)
                        {
                        model.set(parameters[i], count/2 + 1);
                        }
                    }
                else
                    {
                    model.set(parameters[i], val);
                    }
                i++;
                }
            count++;
            pos++;
            }
        return pos;
        }




///// These are certain offsets into the sysex data which start groups
///// of data that all have the same pattern to which we can apply loadData and unloadData.

    public static final int volume = 8;
    public static final int s1basicpedalassign = 16;
    public static final int portamentosw = 20;
    public static final int mode = 22;
    public static final int s1dfgcoarse = 24;
    public static final int s1dfgkey = 28;
    public static final int s1dfgenvdep = 32;
    public static final int s1dfgenvloop = 44;
    public static final int s1dfgenvrateseg1 = 48;

// harmonics go here -- we handle them specially

    public static final int s1dhgvelodep = 70;
    public static final int s1dhgenv1onoff = 78;
    public static final int s1dhgmodonoff = 94;
    public static final int s1dhgrangefrom = 98;
    public static final int s1dhgoddmodonoff = 102;
    public static final int s1dhgallmodonoff = 118;
    public static final int s1dhgangle = 122;
    public static final int s1dhgshadowonoff = 126;
    public static final int s1dhgenv1maxseg1onoff = 127;
    public static final int s2dhgshadowonoff = 129;
    public static final int s2dhgenv1maxseg1onoff = 130;
    public static final int s1dhgenv1maxseg2onoff = 132;
    public static final int s1dhgenv1seg1rate = 152;
    public static final int s1dhgenv2maxseg1onoff = 164;
    public static final int s1dhgenv2seg1rate = 188;
    public static final int s1dhgenv3maxseg1onoff = 200;
    public static final int s1dhgenv3seg1rate = 224;
    public static final int s1dhgenv4maxseg1onoff = 236;
    public static final int s1dhgenv4seg1rate = 260;
    public static final int s1ddfddfonoff = 292;
    public static final int s1ddfenvseg1rate = 298;
    public static final int s1ddfenvmaxseg1onoff = 310;
    public static final int s1ddaattackvelodep = 334;
    public static final int s1ddaddaonoff = 340;
    public static final int s1ddaattackvelrate = 344;
    public static final int s1ddaenvseg1modonoff = 350;
    public static final int s1ddaenvmaxseg1onoff = 378;

// some zeros go here!

    public static final int lfoshape = 402;
    public static final int dftonoff = 412;
    public static final int dftc0level = 414;

// a zero goes here! 
    
    
    }
