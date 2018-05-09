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
    public static final String[] HARMONIC_CONSTRAINTS = { "All", "Odd", "Even", "First Third", "Second Third", "Third Third", "Octaves", "Fifths", "Major Thirds", "Minor Sevenths", "Major Seconds" };
    public static final String[] HARMONIC_MOD_CONSTRAINTS = { "None", "1", "2", "3", "4" };
    public static final String[] DISPLAY_PRESETS = { "Sawtooth Wave", "Square Wave", "Pseudo-Triangle", "All Off", "All On" };
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
    public static final int THIRD = 8;
    public static final int SEVENTH = 9;
    public static final int SECOND = 10; 
    
    // Harmonic Mod Constraints
    public static final int SAWTOOTH = 0;
    public static final int SQUARE = 1;
    public static final int PSEUDO_TRIANGLE = 2;
    public static final int ALL_OFF = 3;
    public static final int ALL_ON = 4;
    
    // Various harmonics
    public static final int[] OCTAVE_HARMONICS = { 1, 2, 4, 8, 16, 32, 64 };
    public static final int[] FIFTH_HARMONICS = { 3, 6, 12, 24, 48, 96 };               // -1 because we're in the 64..127 
    public static final int[] MAJOR_THIRD_HARMONICS = { 5, 10, 20, 40, 80 };
    public static final int[] MINOR_SEVENTH_HARMONICS = { 7, 14, 28, 56 };
    public static final int[] MAJOR_SECOND_HARMONICS = { 9, 18, 36, 72 };
        
	SynthPanel[] synthPanels = new SynthPanel[6];

    public KawaiK5()
        {
        for(int i = 0; i < parameters.length; i++)
            {
//            System.err.println("" + parameters[i] + " " + Integer.valueOf(paramNumbers[i]));
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
        addTab("General 1", synthPanels[0] = (SynthPanel)soundPanel);
                
        
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
        addTab("General 2", synthPanels[1] = (SynthPanel)soundPanel);
                
        
               
        harmonicsSources[0] = addDHGDisplay(0, Style.COLOR_B());
        harmonicsSources[1] = addDHGDisplay(0, Style.COLOR_B());
        harmonicsSources[2] = addDHGDisplay(1, Style.COLOR_B());
        harmonicsSources[3] = addDHGDisplay(2, Style.COLOR_B());

        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        vbox.add(addDHG(1, Style.COLOR_A()));
        hbox = new HBox();
        hbox.add(addDHG2(1, Style.COLOR_A()));
        harmonics[0] = new HBox();
        harmonics[0].addLast(harmonicsSources[0]);
        hbox.addLast(harmonics[0]);
        vbox.add(hbox);
        
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Harmonics 1", synthPanels[2] = (SynthPanel)soundPanel);
                
        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        vbox.add(addDHG(2, Style.COLOR_A()));
        hbox = new HBox();
        hbox.add(addDHG2(2, Style.COLOR_A()));
        harmonics[1] = new HBox();
        harmonics[1].addLast(harmonicsSources[1]);
        hbox.addLast(harmonics[1]);
        vbox.add(hbox);
        
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Harmonics 2", synthPanels[3] = (SynthPanel)soundPanel);
                

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
        addTab("Envelopes/KS 1", synthPanels[4] = (SynthPanel)soundPanel);
                

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
        addTab("Envelopes/KS 2", synthPanels[5] = (SynthPanel)soundPanel);

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

        addKawaiK5Menu();
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

    //boolean sendKawaiParametersInBulk = true;
        
    public void addKawaiK5Menu()
        {
        JMenu menu = new JMenu("Kawai K5");
        menubar.add(menu);

/*
        // classic patch names
                
        JMenu sendParameters = new JMenu("Send Parameters");
        menu.add(sendParameters);
                
        String str = getLastX("SendParameters", getSynthName(), true);
        if (str == null)
            sendKawaiParametersInBulk = true;
        else if (str.equalsIgnoreCase("BULK"))
            sendKawaiParametersInBulk = true;
        else if (str.equalsIgnoreCase("INDIVIDUALLY"))
            sendKawaiParametersInBulk = false;
        else sendKawaiParametersInBulk = true;

        ButtonGroup bg = new ButtonGroup();

        JRadioButtonMenuItem separately = new JRadioButtonMenuItem("Individually");
        separately.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt)
                {
                sendKawaiParametersInBulk = false;
                setLastX("INDIVIDUALLY", "SendParameters", getSynthName(), true);
                }
            });
        sendParameters.add(separately);
        bg.add(separately);
        if (sendKawaiParametersInBulk == false) 
            separately.setSelected(true);

        JRadioButtonMenuItem bulk = new JRadioButtonMenuItem("In Bulk, Overwriting Patch SID-12");
        bulk.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt)
                {
                sendKawaiParametersInBulk = true;
                setLastX("BULK", "SendParameters", getSynthName(), true);
                }
            });
        sendParameters.add(bulk);
        bg.add(bulk);
        if (sendKawaiParametersInBulk == true) 
            bulk.setSelected(true);
*/

        menu.add(copy);
        copy.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                SynthPanel p = findPanel();
                if (p != null) p.copyPanel(true);
                }
            });
        menu.add(paste);
        paste.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                SynthPanel p = findPanel();
                if (p != null) p.pastePanel(true);
                }
            });
        menu.addSeparator();
        menu.add(copyMutable);
        copyMutable.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                SynthPanel p = findPanel();
                if (p != null) p.copyPanel(false);
                }
            });
        menu.add(pasteMutable);
        pasteMutable.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                SynthPanel p = findPanel();
                if (p != null) p.pastePanel(false);
                }
            });
        menu.addSeparator();
        reset.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                SynthPanel p = findPanel();
                if (p != null) p.resetPanel();
                }
            });
        menu.add(reset);
        }

    public void tabChanged()
        {
        super.tabChanged();
        boolean isOscillator = (getSelectedTabTitle().startsWith("General") ||
        						getSelectedTabTitle().startsWith("Harmonics") ||
        						getSelectedTabTitle().startsWith("Envelopes") );
        copy.setEnabled(isOscillator);
        paste.setEnabled(isOscillator);
        copyMutable.setEnabled(isOscillator);
        pasteMutable.setEnabled(isOscillator);
        reset.setEnabled(isOscillator);
        }
    
    public SynthPanel findPanel()
        {
        String title = getSelectedTabTitle();
        if (title.equals("General 1"))
            return synthPanels[0];
        else if (title.equals("General 2"))
            return synthPanels[1];
        else if (title.equals("Harmonics 1"))
            return synthPanels[2];
        else if (title.equals("Harmonics 1"))
            return synthPanels[3];
        else if (title.equals("Envelopes/KS 1"))
            return synthPanels[4];
        else if (title.equals("Envelopes/KS 2"))
            return synthPanels[5];
        else return null;
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
        category.makePasteable("basics");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = MOD_DESTINATIONS;
        comp = new Chooser("Pedal Destination", this, "basics" + source + "pedalassign", params);
        vbox.add(comp);

        params = MOD_DESTINATIONS;
        comp = new Chooser("Mod Wheel", this, "basics" + source + "wheelassign", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Delay", this, "basics" + source + "delay", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Pedal", this, "basics" + source + "pedaldep", color, -31, 31);
        ((LabelledDial)comp).addAdditionalLabel("Depth");
        hbox.add(comp);

        comp = new LabelledDial("Mod Wheel", this, "basics" + source + "wheeldep", color, -31, 31);
        ((LabelledDial)comp).addAdditionalLabel("Depth");
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addKSCurve(int source, Color color)
        {
        Category category = new Category(this, "Key Scaling Curve", color);
        category.makePasteable("kss");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        comp = new LabelledDial("Breakpoint", this, "kss" + source + "breakpoint", color, 0, 127)
            {
            public String map(int val)
                {
                return KEYS[val % 12] + (val / 12 + 1);  // note integer division
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Left", this, "kss" + source + "left", color, -31, 31);
        hbox.add(comp);

        comp = new LabelledDial("Right", this, "kss" + source + "right", color, -31, 31);
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addPitch(int source, Color color)
        {
        Category category = new Category(this, "Pitch (DFG)", color);
        category.makePasteable("dfgs");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        comp = new CheckBox("Fixed", this, "dfgs" + source + "key", false);
        vbox.add(comp); 
        hbox.add(vbox);   

        comp = new LabelledDial("Fixed Key", this, "dfgs" + source + "fixno", color, 0, 127)
            {
            public String map(int val)
                {
                return KEYS[val % 12] + (val / 12 + 1);  // note integer division
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Coarse", this, "dfgs" + source + "coarse", color, -48, 48);
        ((LabelledDial)comp).addAdditionalLabel("Tune");
        hbox.add(comp);

        comp = new LabelledDial("Fine", this, "dfgs" + source + "fine", color, -31, 31);
        ((LabelledDial)comp).addAdditionalLabel("Tune");
        hbox.add(comp);

        comp = new LabelledDial("Pitch Bend", this, "dfgs" + source + "benderdep", color, 0, 24);
        hbox.add(comp);

        comp = new LabelledDial("Aftertouch", this, "dfgs" + source + "prsdep", color, -31, 31);
        ((LabelledDial)comp).addAdditionalLabel("Mod");
        hbox.add(comp);
                
        comp = new LabelledDial("LFO", this, "dfgs" + source + "lfodep", color, 0, 31);
        ((LabelledDial)comp).addAdditionalLabel("Mod");
        hbox.add(comp);

        comp = new LabelledDial("Envelope", this, "dfgs" + source + "envdep", color, -24, 24);
        ((LabelledDial)comp).addAdditionalLabel("Mod");
        hbox.add(comp);

        comp = new LabelledDial("Aftertouch", this, "dfgs" + source + "preslfodep", color, -31, 31);
        ((LabelledDial)comp).addAdditionalLabel("LFO Mod");
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addPitchEnvelope(int source, Color color)
        {
        Category category = new Category(this, "Pitch (DFG) Envelope", color);
        category.makePasteable("dfgs");
        category.makeDistributable("dfgs");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        comp = new CheckBox("Loop", this, "dfgs" + source + "envloop", false);
        ((CheckBox)comp).addToWidth(2);
        vbox.add(comp); 
        hbox.add(vbox);   

        comp = new LabelledDial("Velocity", this, "dfgs" + source + "veloenvdep", color, -31, 31);
        ((LabelledDial)comp).addAdditionalLabel("Mod");
        hbox.add(comp);

        comp = new LabelledDial("Rate 1", this, "dfgs" + source + "envrateseg1", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Level 1", this, "dfgs" + source + "envlevelseg1", color, -31, 31);
        hbox.add(comp);

        comp = new LabelledDial("Rate 2", this, "dfgs" + source + "envrateseg2", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Level 2", this, "dfgs" + source + "envlevelseg2", color, -31, 31);
        hbox.add(comp);

        comp = new LabelledDial("Rate 3", this, "dfgs" + source + "envrateseg3", color, 0, 31);
        ((LabelledDial)comp).addAdditionalLabel("(Loop Start) ");
        hbox.add(comp);

        comp = new LabelledDial("Level 3", this, "dfgs" + source + "envlevelseg3", color, -31, 31);
        hbox.add(comp);

        comp = new LabelledDial("Rate 4", this, "dfgs" + source + "envrateseg4", color, 0, 31);
        ((LabelledDial)comp).addAdditionalLabel("(Loop End) ");
        hbox.add(comp);

        comp = new LabelledDial("Level 4", this, "dfgs" + source + "envlevelseg4", color, -31, 31);
        ((LabelledDial)comp).addAdditionalLabel("(Sustain)");
        hbox.add(comp);

        comp = new LabelledDial("Rate 5", this, "dfgs" + source + "envrateseg5", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Level 5", this, "dfgs" + source + "envlevelseg5", color, -31, 31);
        hbox.add(comp);

        comp = new LabelledDial("Rate 6", this, "dfgs" + source + "envrateseg6", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Level 6", this, "dfgs" + source + "envlevelseg6", color, -31, 31);
        hbox.add(comp);

        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null,  
                           "dfgs" + source + "envrateseg1",
                           "dfgs" + source + "envrateseg2",
                           "dfgs" + source + "envrateseg3",
                           "dfgs" + source + "envrateseg4",
                           "dfgs" + source + "envrateseg5",
                           "dfgs" + source + "envrateseg6" },
            new String[] { null,  
                           "dfgs" + source + "envlevelseg1",
                           "dfgs" + source + "envlevelseg2",
                           "dfgs" + source + "envlevelseg3",
                           "dfgs" + source + "envlevelseg4",
                           "dfgs" + source + "envlevelseg5",
                           "dfgs" + source + "envlevelseg6" },
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
        category.makePasteable("dhgs");
        category.makeDistributable("dhgs");

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
                //// we'll read in the two parameters and compress them to a single stored parameter called, say, dhgs2harm16envselmodyn.
                //// Additionally we'll store the envelope parameter as dhgs2harm16envselmodyn-env.  When we need to emit the parameter
                //// or dump the patch, if the mod is ON, then we just write the chosen envelope.  But if the mod is OFF, then we write
                //// out the envelope in dhgs2harm16envselmodyn-env.  Note that while dhgs2harm16envselmodyn goes 0:OFF, 1:1, 2:2, 3:3, 4:4,
                //// the dhgs2harm16envselmodyn-env parameter goes 0:1, 1:2, 2:3, 3:4.

                params = ENVELOPES;
                comp = new Chooser("" + (j * 16 + i + 1) + " Env", this, "dhgs" + source + "harm" + (j * 16 + i + 1) + "envselmodyn", params);
                vbox2.add(comp);  
                
                model.set("dhgs" + source + "harm" + (j * 16 + i + 1) + "envselmodyn-env", 0);    // this will be our hidden env parameter (see emitting/parsing later)
                model.setStatus("dhgs" + source + "harm" + (j * 16 + i + 1) + "envselmodyn-env", model.STATUS_IMMUTABLE);
                
                comp = new LabelledDial("" + (j * 16 + i + 1) + " Level", this, "dhgs" + source + "harm" + (j * 16 + i + 1) + "level", color, 0, 99);
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
                comp = new Chooser("" + (48 + j * 8 + i + 1) + " Env", this, "dhgs" + source + "harm" + (48 + j * 8 + i + 1) + "envselmodyn", params);
                vbox2.add(comp);  
                
                model.set("dhgs" + source + "harm" + (48 + j * 8 + i + 1) + "envselmodyn-env", 0);    // this will be our hidden env parameter (see emitting/parsing later)
                model.setStatus("dhgs" + source + "harm" + (48 + j * 8 + i + 1) + "envselmodyn-env", model.STATUS_IMMUTABLE);
                                
                comp = new LabelledDial("" + (48 + j * 8 + i + 1) + " Level", this, "dhgs" + source + "harm" + (48 + j * 8 + i + 1) + "level", color, 0, 99);
                dials[(source - 1) * 63 + (48 + j * 8 + i)] = (LabelledDial) comp;
                vbox2.add(comp);
                hbox.add(vbox2);
                }
            vbox.add(hbox);
            //if (j < 3)
            vbox.add(Strut.makeVerticalStrut(20));
            }

        comp = new CheckBox("Mod", this, "dhgs" + source + "modonoff", false);
        hbox.addLast(comp);
                
        return vbox;
        }
 
 
    int lastIndex = -1;
 
    void setHarmonic(int index, double val)
        {
        if (index >= 0 && index < 63)
            {
            model.set("dhgs" + 1 + "harm" + (index + 1) + "level", (int)(val * 99));
            }
        else if (index > 63 && index < 127)
            {
            model.set("dhgs" + 2 + "harm" + (index + 1 - 64) + "level", (int)(val * 99));
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
                        for(int h = start; h < end; h += 2)
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
                    levels[i] = "dhgs" + 1 + "harm" + (i + 1) + "level";
                    mods[i] = "dhgs" + 1 + "harm" + (i + 1) + "envselmodyn";
                    }
                else
                    {
                    levels[i] = "dhgs" + 2 + "harm" + (i + 1 - 63) + "level";
                    mods[i] = "dhgs" + 2 + "harm" + (i + 1 - 63) + "envselmodyn";
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
                        KawaiK5.this.model.set("dhgs" + 1 + "harm" + (harmonic + 1) + "envselmodyn", (int)val);
                    else
                        KawaiK5.this.model.set("dhgs" + 2 + "harm" + (harmonic + 1 - 63) + "envselmodyn", (int)val);
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
                        KawaiK5.this.model.set("dhgs" + 1 + "harm" + (harmonic + 1) + "level", (int)val);
                    else
                        KawaiK5.this.model.set("dhgs" + 2 + "harm" + (harmonic + 1 - 63) + "level", (int)val);
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
                levels[i] = "dhgs" + source + "harm" + (i + 1) + "level";
                mods[i] = "dhgs" + source + "harm" + (i + 1) + "envselmodyn";
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
                                        
                    KawaiK5.this.model.set("dhgs" + source + "harm" + (harmonic + 1) + "envselmodyn", (int)val);
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

                public boolean constrainTo(int index) { return _constrainTo(index + (source == 1 ? 0 : 63)); }

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
                                
                    KawaiK5.this.model.set("dhgs" + source + "harm" + (harmonic + 1) + "level", (int)val);
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
                        
                public boolean constrainTo(int index) { return _constrainTo(index + (source == 1 ? 0 : 63)); }
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
            case THIRD:
                {
                for(int i = 0; i < MAJOR_THIRD_HARMONICS.length; i++)
                    if (MAJOR_THIRD_HARMONICS[i] == (index + 1)) return true;
                return false;                                           
                }
            case SEVENTH:
                {
                for(int i = 0; i < MINOR_SEVENTH_HARMONICS.length; i++)
                    if (MINOR_SEVENTH_HARMONICS[i] == (index + 1)) return true;
                return false;                                           
                }
            case SECOND:
                {
                for(int i = 0; i < MAJOR_SECOND_HARMONICS.length; i++)
                    if (MAJOR_SECOND_HARMONICS[i] == (index + 1)) return true;
                return false;                                           
                }
            }
        return false;
        }
        
        
    public JComponent addDHGEnvelope(int source, int envelope, Color color)
        {
        Category category = new Category(this, "Harmonics (DHG) Envelope " + envelope, color);
        category.makePasteable("dhgs");
        category.makeDistributable("dhgs");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = MAX_SEGMENTS;
        comp = new Chooser("Max Segment", this, "dhgs" + source + "env" + envelope + "maxsegonoff", params);
        vbox.add(comp);
        hbox.add(vbox);
                
        comp = new CheckBox("Active", this, "dhgs" + source + "env" + envelope + "onoff", false);
        vbox.add(comp); 
        hbox.add(vbox);   

        comp = new LabelledDial("Mod Depth", this, "dhgs" + source + "env" + envelope + "moddepth", color, 0, 31)
            {
            public String map(int val)
                {
                if (val == 0) return "Off";
                else return "" + val;
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Rate 1", this, "dhgs" + source + "env" + envelope + "seg1rate", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Level 1", this, "dhgs" + source + "env" + envelope + "seg1level", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Rate 2", this, "dhgs" + source + "env" + envelope + "seg2rate", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Level 2", this, "dhgs" + source + "env" + envelope + "seg2level", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Rate 3", this, "dhgs" + source + "env" + envelope + "seg3rate", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Level 3", this, "dhgs" + source + "env" + envelope + "seg3level", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Rate 4", this, "dhgs" + source + "env" + envelope + "seg4rate", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Level 4", this, "dhgs" + source + "env" + envelope + "seg4level", color, 0, 31);
        ((LabelledDial)comp).addAdditionalLabel("(Sustain)");
        hbox.add(comp);

        comp = new LabelledDial("Rate 5", this, "dhgs" + source + "env" + envelope + "seg5rate", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Level 5", this, "dhgs" + source + "env" + envelope + "seg5level", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Rate 6", this, "dhgs" + source + "env" + envelope + "seg6rate", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Level 6", this, "dhgs" + source + "env" + envelope + "seg6level", color, 0, 31);
        hbox.add(comp);

        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null,  
                           "dhgs" + source + "env" + envelope + "seg1rate",
                           "dhgs" + source + "env" + envelope + "seg2rate",
                           "dhgs" + source + "env" + envelope + "seg3rate",
                           "dhgs" + source + "env" + envelope + "seg4rate",
                           "dhgs" + source + "env" + envelope + "seg5rate",
                           "dhgs" + source + "env" + envelope + "seg6rate" },
            new String[] { null,  
                           "dhgs" + source + "env" + envelope + "seg1level",
                           "dhgs" + source + "env" + envelope + "seg2level",
                           "dhgs" + source + "env" + envelope + "seg3level",
                           "dhgs" + source + "env" + envelope + "seg4level",
                           "dhgs" + source + "env" + envelope + "seg5level",
                           "dhgs" + source + "env" + envelope + "seg6level" },
            new double[] { 0, .1666666666 / 31, .1666666666 / 31, .1666666666 / 31, .1666666666 / 31, .1666666666 / 31, .1666666666 / 31 },
            new double[] { 0, 1.0 / 31, 1.0 / 31, 1.0 / 31, 1.0 / 31, 1.0 / 31, 1.0 / 31 });
        hbox.addLast(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
                
                

        
    public JComponent addDHGEnvelopeGlobal(int source, Color color)
        {
        Category category = new Category(this, "Harmonics (DHG) Envelope Global", color);
        category.makePasteable("dhgs");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        /*
          params = DHG_SELECT;
          comp = new Chooser("Select", this, "dhgs" + source + "harmsel", params);
          vbox.add(comp);
          hbox.add(vbox);   

          params = ANGLES;
          comp = new Chooser("Angle", this, "dhgs" + source + "angle", params);
          vbox.add(comp);
        */

        params = SIMPLE_ENVELOPES;
        comp = new Chooser("All Env", this, "dhgs" + source + "allenv", params);
        vbox.add(comp);
        comp = new CheckBox("Mod", this, "dhgs" + source + "allmodonoff", false);
        vbox.add(comp);
        hbox.add(vbox);

        vbox = new VBox();
        params = SIMPLE_ENVELOPES;
        comp = new Chooser("Odd Env", this, "dhgs" + source + "oddenv", params);
        vbox.add(comp);
        comp = new CheckBox("Mod", this, "dhgs" + source + "oddmodonoff", false);
        vbox.add(comp);
        hbox.add(vbox);

        vbox = new VBox();
        params = SIMPLE_ENVELOPES;
        comp = new Chooser("Even Env", this, "dhgs" + source + "evenenv", params);
        vbox.add(comp);
        comp = new CheckBox("Mod", this, "dhgs" + source + "evenmodonoff", false);
        vbox.add(comp);
        hbox.add(vbox);

        vbox = new VBox();
        params = SIMPLE_ENVELOPES;
        comp = new Chooser("Octave Env", this, "dhgs" + source + "octaveenv", params);
        vbox.add(comp);
        comp = new CheckBox("Mod", this, "dhgs" + source + "octavemodonoff", false);
        vbox.add(comp);
        hbox.add(vbox);

        vbox = new VBox();
        params = SIMPLE_ENVELOPES;
        comp = new Chooser("Fifth Env", this, "dhgs" + source + "fifthenv", params);
        vbox.add(comp);
        comp = new CheckBox("Mod", this, "dhgs" + source + "fifthmodonoff", false);
        vbox.add(comp);
        hbox.add(vbox);

        vbox = new VBox();
        comp = new CheckBox("Shadow", this, "dhgs" + source + "shadowonoff", false);
        vbox.add(comp); 
        hbox.add(vbox);   
        

        // we don't display these
                
        model.set("dhgs" + source + "all", 0);
        model.setStatus("dhgs" + source + "all", Model.STATUS_IMMUTABLE);
        model.set("dhgs" + source + "odd", 0);
        model.setStatus("dhgs" + source + "odd", Model.STATUS_IMMUTABLE);
        model.set("dhgs" + source + "even", 0);
        model.setStatus("dhgs" + source + "even", Model.STATUS_IMMUTABLE);
        model.set("dhgs" + source + "octave", 0);
        model.setStatus("dhgs" + source + "octave", Model.STATUS_IMMUTABLE);
        model.set("dhgs" + source + "fifth", 0);
        model.setStatus("dhgs" + source + "fifth", Model.STATUS_IMMUTABLE);
        model.set("dhgs" + source + "rangefrom", 0);
        model.setStatus("dhgs" + source + "rangefrom", Model.STATUS_IMMUTABLE);
        model.set("dhgs" + source + "rangeto", 0);
        model.setStatus("dhgs" + source + "rangeto", Model.STATUS_IMMUTABLE);
        model.set("dhgs" + source + "select", 0);
        model.setStatus("dhgs" + source + "select", Model.STATUS_IMMUTABLE);
        model.set("dhgs" + source + "angle", 0);
        model.setStatus("dhgs" + source + "angle", Model.STATUS_IMMUTABLE);
        model.set("dhgs" + source + "harmsel", 0);
        model.setStatus("dhgs" + source + "harmsel", Model.STATUS_IMMUTABLE);


/*
  comp = new LabelledDial("All", this, "dhgs" + source + "all", color, 0, 99);
  model.setStatus("dhgs" + source + "all", Model.STATUS_IMMUTABLE);
  hbox.add(comp);

  comp = new LabelledDial("Odd", this, "dhgs" + source + "odd", color, 0, 99);
  model.setStatus("dhgs" + source + "odd", Model.STATUS_IMMUTABLE);
  hbox.add(comp);

  comp = new LabelledDial("Even", this, "dhgs" + source + "even", color, 0, 99);
  model.setStatus("dhgs" + source + "even", Model.STATUS_IMMUTABLE);
  hbox.add(comp);

  comp = new LabelledDial("Octave", this, "dhgs" + source + "octave", color, 0, 99);
  model.setStatus("dhgs" + source + "octave", Model.STATUS_IMMUTABLE);
  hbox.add(comp);

  comp = new LabelledDial("Fifth", this, "dhgs" + source + "fifth", color, 0, 99);
  model.setStatus("dhgs" + source + "fifth", Model.STATUS_IMMUTABLE);
  hbox.add(comp);

  comp = new LabelledDial("Range Low", this, "dhgs" + source + "rangefrom", color, 1, 63);
  hbox.add(comp);

  comp = new LabelledDial("Range High", this, "dhgs" + source + "rangeto", color, 1, 63);
  hbox.add(comp);
*/      


        comp = new LabelledDial("Velocity", this, "dhgs" + source + "velodep", color, -31, 31);
        ((LabelledDial)comp).addAdditionalLabel("Depth");
        hbox.add(comp);

        comp = new LabelledDial("Pressure", this, "dhgs" + source + "presdep", color, -31, 31);
        ((LabelledDial)comp).addAdditionalLabel("Depth");
        hbox.add(comp);

        comp = new LabelledDial("Key", this, "dhgs" + source + "ksdep", color, -31, 31);
        ((LabelledDial)comp).addAdditionalLabel("Scaling");
        hbox.add(comp);

        comp = new LabelledDial("LFO", this, "dhgs" + source + "lfodep", color, 0, 31);
        ((LabelledDial)comp).addAdditionalLabel("Depth");
        hbox.add(comp);
                
        // These four we don't have widgets for
        
        model.set("dhgs1harm", 1);
        model.setStatus("dhgs1harm", Model.STATUS_IMMUTABLE);
        model.set("dhgs2harm", 1);
        model.setStatus("dhgs2harm", Model.STATUS_IMMUTABLE);

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

        comp = new LabelledDial("-1", this, "dftcneg1level", color, 0, 63);
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
                "dftcneg1level",
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
        category.makePasteable("ddfs");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        comp = new CheckBox("Active", this, "ddfs" + source + "ddfonoff", false);
        vbox.add(comp); 

        comp = new CheckBox("Global Mod", this, "ddfs" + source + "ddfmodonoff", false);
        vbox.add(comp); 
        hbox.add(vbox);   

        comp = new LabelledDial("Flat Level", this, "ddfs" + source + "flatlevel", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Cutoff", this, "ddfs" + source + "cutoff", color, 0, 99);
        hbox.add(comp);

        comp = new LabelledDial("Slope", this, "ddfs" + source + "slope", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Cutoff Mod", this, "ddfs" + source + "cutoffmod", color, 0, 31);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        comp = new LabelledDial("Slope Mod", this, "ddfs" + source + "slopemod", color, 0, 31);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        comp = new LabelledDial("Envelope", this, "ddfs" + source + "envdep", color, -31, 31);
        ((LabelledDial)comp).addAdditionalLabel("Mod");
        hbox.add(comp);

        comp = new LabelledDial("Velocity", this, "ddfs" + source + "veloenvdep", color, -31, 31);
        ((LabelledDial)comp).addAdditionalLabel("Env Mod");
        hbox.add(comp);

        comp = new LabelledDial("Velocity", this, "ddfs" + source + "velodep", color, -31, 31);
        ((LabelledDial)comp).addAdditionalLabel("Mod");
        hbox.add(comp);

        comp = new LabelledDial("Aftertouch", this, "ddfs" + source + "presdep", color, -31, 31);
        ((LabelledDial)comp).addAdditionalLabel("Mod");
        hbox.add(comp);
                
        comp = new LabelledDial("Key Scale", this, "ddfs" + source + "ksdep", color, -31, 31);
        ((LabelledDial)comp).addAdditionalLabel("Mod");
        hbox.add(comp);
                
        comp = new LabelledDial("LFO", this, "ddfs" + source + "lfodep", color, 0, 31);
        ((LabelledDial)comp).addAdditionalLabel("Mod");
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
        
    public JComponent addFilterEnvelope(int source, Color color)
        {
        Category category = new Category(this, "Filter (DDF) Envelope", color);
        category.makePasteable("ddfs");
        category.makeDistributable("ddfs");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = MAX_SEGMENTS;
        comp = new Chooser("Max Segment", this, "ddfs" + source + "envmaxsegonoff", params);
        vbox.add(comp);
        hbox.add(vbox);
                
        comp = new LabelledDial("Rate 1", this, "ddfs" + source + "envseg1rate", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Level 1", this, "ddfs" + source + "envseg1level", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Rate 2", this, "ddfs" + source + "envseg2rate", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Level 2", this, "ddfs" + source + "envseg2level", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Rate 3", this, "ddfs" + source + "envseg3rate", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Level 3", this, "ddfs" + source + "envseg3level", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Rate 4", this, "ddfs" + source + "envseg4rate", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Level 4", this, "ddfs" + source + "envseg4level", color, 0, 31);
       ((LabelledDial)comp).addAdditionalLabel("(Sustain)");
        hbox.add(comp);

        comp = new LabelledDial("Rate 5", this, "ddfs" + source + "envseg5rate", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Level 5", this, "ddfs" + source + "envseg5level", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Rate 6", this, "ddfs" + source + "envseg6rate", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Level 6", this, "ddfs" + source + "envseg6level", color, 0, 31);
        hbox.add(comp);

        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null,  
                           "ddfs" + source + "envseg1rate",
                           "ddfs" + source + "envseg2rate",
                           "ddfs" + source + "envseg3rate",
                           "ddfs" + source + "envseg4rate",
                           "ddfs" + source + "envseg5rate",
                           "ddfs" + source + "envseg6rate" },
            new String[] { null,  
                           "ddfs" + source + "envseg1level",
                           "ddfs" + source + "envseg2level",
                           "ddfs" + source + "envseg3level",
                           "ddfs" + source + "envseg4level",
                           "ddfs" + source + "envseg5level",
                           "ddfs" + source + "envseg6level" },
            new double[] { 0, .1666666666 / 31, .1666666666 / 31, .1666666666 / 31, .1666666666 / 31, .1666666666 / 31, .1666666666 / 31 },
            new double[] { 0, 1.0 / 31, 1.0 / 31, 1.0 / 31, 1.0 / 31, 1.0 / 31, 1.0 / 31 });
        hbox.addLast(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addDDA(int source, Color color)
        {
        Category category = new Category(this, "Amplifier (DDA)", color);
        category.makePasteable("ddas");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        comp = new CheckBox("Active", this, "ddas" + source + "ddaonoff", false);
        vbox.add(comp); 
		hbox.add(vbox);
		
        comp = new LabelledDial("Velocity", this, "ddas" + source + "attackvelodep", color, -31, 31);
        ((LabelledDial)comp).addAdditionalLabel("Mod");
        hbox.add(comp);

        comp = new LabelledDial("Aftertouch", this, "ddas" + source + "presdep", color, -31, 31);
        ((LabelledDial)comp).addAdditionalLabel("Mod");
        hbox.add(comp);
                
        comp = new LabelledDial("Key Scale", this, "ddas" + source + "ksdep", color, -31, 31);
        ((LabelledDial)comp).addAdditionalLabel("Mod");
        hbox.add(comp);
                
        comp = new LabelledDial("LFO", this, "ddas" + source + "lfodep", color, 0, 31);
        ((LabelledDial)comp).addAdditionalLabel("Mod");
        hbox.add(comp);

        comp = new LabelledDial("Attack Vel", this, "ddas" + source + "attackvelrate", color, -15, 15);
        ((LabelledDial)comp).addAdditionalLabel("Rate Mod");
        hbox.add(comp);

        comp = new LabelledDial("Release Vel", this, "ddas" + source + "releasevelrate", color, -15, 15);
        ((LabelledDial)comp).addAdditionalLabel("Rate Mod");
        hbox.add(comp);

        comp = new LabelledDial("Key Scale", this, "ddas" + source + "ksrate", color, -15, 15);
        ((LabelledDial)comp).addAdditionalLabel("Rate Mod");
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
        
    public JComponent addDDAEnvelope(int source, Color color)
        {
        Category category = new Category(this, "Amplifier (DDA) Envelope", color);
        category.makePasteable("ddas");
        category.makeDistributable("ddas");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = MAX_SEGMENTS;
        comp = new Chooser("Max Segment", this, "ddas" + source + "envmaxsegonoff", params);
        vbox.add(comp);
        hbox.add(vbox);

        vbox = new VBox();
        comp = new LabelledDial("Rate 1", this, "ddas" + source + "envseg1rate", color, 0, 31);
        vbox.add(comp);
        comp = new CheckBox("Mod", this, "ddas" + source + "envseg1modonoff", false);
        vbox.add(comp); 
        hbox.add(vbox);
        vbox = new VBox();


        comp = new LabelledDial("Level 1", this, "ddas" + source + "envseg1level", color, 0, 31);
        hbox.add(comp); 
                
        comp = new LabelledDial("Rate 2", this, "ddas" + source + "envseg2rate", color, 0, 31);
        vbox.add(comp);
        comp = new CheckBox("Mod", this, "ddas" + source + "envseg2modonoff", false);
        vbox.add(comp); 
        hbox.add(vbox);
        vbox = new VBox();


        comp = new LabelledDial("Level 2", this, "ddas" + source + "envseg2level", color, 0, 31);
        hbox.add(comp); 

        comp = new LabelledDial("Rate 3", this, "ddas" + source + "envseg3rate", color, 0, 31);
        vbox.add(comp);
        comp = new CheckBox("Mod", this, "ddas" + source + "envseg3modonoff", false);
        vbox.add(comp); 
        hbox.add(vbox);
        vbox = new VBox();


        comp = new LabelledDial("Level 3", this, "ddas" + source + "envseg3level", color, 0, 31);
        hbox.add(comp); 

        comp = new LabelledDial("Rate 4", this, "ddas" + source + "envseg4rate", color, 0, 31);
        vbox.add(comp);
        comp = new CheckBox("Mod", this, "ddas" + source + "envseg4modonoff", false);
        vbox.add(comp); 
        hbox.add(vbox);
        vbox = new VBox();


        comp = new LabelledDial("Level 4", this, "ddas" + source + "envseg4level", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Rate 5", this, "ddas" + source + "envseg5rate", color, 0, 31);
        vbox.add(comp); 
        comp = new CheckBox("Mod", this, "ddas" + source + "envseg5modonoff", false);
        vbox.add(comp); 
        hbox.add(vbox);
        vbox = new VBox();


        comp = new LabelledDial("Level 5", this, "ddas" + source + "envseg5level", color, 0, 31);
        ((LabelledDial)comp).addAdditionalLabel("(Sustain)");
        hbox.add(comp);

        comp = new LabelledDial("Rate 6", this, "ddas" + source + "envseg6rate", color, 0, 31);
        vbox.add(comp);
        comp = new CheckBox("Mod", this, "ddas" + source + "envseg6modonoff", false);
        vbox.add(comp); 
        hbox.add(vbox);
        vbox = new VBox();


        comp = new LabelledDial("Level 6", this, "ddas" + source + "envseg6level", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Rate 7", this, "ddas" + source + "envseg7rate", color, 0, 31);
        vbox.add(comp);
        comp = new CheckBox("Mod", this, "ddas" + source + "envseg7modonoff", false);
        vbox.add(comp); 
        hbox.add(vbox);
        vbox = new VBox();

        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null,  
                           "ddas" + source + "envseg1rate",
                           "ddas" + source + "envseg2rate",
                           "ddas" + source + "envseg3rate",
                           "ddas" + source + "envseg4rate",
                           "ddas" + source + "envseg5rate",
                           "ddas" + source + "envseg6rate",
                           "ddas" + source + "envseg7rate",
                },
            new String[] { null,  
                           "ddas" + source + "envseg1level",
                           "ddas" + source + "envseg2level",
                           "ddas" + source + "envseg3level",
                           "ddas" + source + "envseg4level",
                           "ddas" + source + "envseg5level",
                           "ddas" + source + "envseg6level",
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
        if (key.equals("dhgs1harm")) return new Object[0];
        if (key.equals("dhgs2harm")) return new Object[0];
        if (key.equals("dhgs1odd") ||
            key.equals("dhgs1even") ||
            key.equals("dhgs1octave") ||
            key.equals("dhgs1fifth") ||
            key.equals("dhgs1select") ||
            key.equals("dhgs1all") ||
            key.equals("dhgs2odd") ||
            key.equals("dhgs2even") ||
            key.equals("dhgs2octave") ||
            key.equals("dhgs2fifth") ||
            key.equals("dhgs2select") ||
            key.equals("dhgs2all")) return new Object[0];
        if (key.equals("constrainharmonics") || 
            key.equals("constrainmodharmonics")) return new Object[0];
        if (key.equals("bank")) return new Object[0];
        if (key.equals("number")) return new Object[0];
        
        
        // WE don't bother with bank,  number, and a few others.
        // Also we don't presently emit 181, 182, 183, 183, or 185 (inc/dec the odd/even/oct/5th/all options), see Note 6-4)
        
        Object[] data = new Object[1];

        // determine source
        int source = 0;  // doesn't matter
        if (key.startsWith("basics"))
            {
            source = (key.charAt(6) == '1' ? 0 : 1);
            }
        else if (key.startsWith("kss"))
            {
            source = (key.charAt(3) == '1' ? 0 : 1);
            }
        else if (key.startsWith("dhgs") || 
            key.startsWith("ddas") || 
            key.startsWith("ddfs") ||
            key.startsWith("dhgs") || 
            key.startsWith("dfgs"))
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
        else if (key.startsWith("dhgs") && key.endsWith("maxsegonoff") ||
            key.startsWith("ddfs") && key.endsWith("maxsegonoff") ||
            key.startsWith("ddas") && key.endsWith("maxsegonoff"))
            {
            int paramNum = 0;
            if (key.startsWith("ddf")) paramNum = 131;
            else if (key.startsWith("dda")) paramNum = 161;
            else if (key.startsWith("dhgs1env1") ||
                key.startsWith("dhgs2env1")) paramNum = 52;
            else if (key.startsWith("dhgs1env2") ||
                key.startsWith("dhgs2env2")) paramNum = 53;
            else if (key.startsWith("dhgs1env3") ||
                key.startsWith("dhgs2env3")) paramNum = 54;
            else if (key.startsWith("dhgs1env4") ||
                key.startsWith("dhgs2env4")) paramNum = 55;
            else System.err.println("Weird Key? " + key);
                
            int c = model.get(key, 0);
            data[0] = new byte[] { (byte)0xF0, 0x40, (byte)(getChannelOut()), 0x10, 0x00, 0x02, (byte)((source << 1) | ((paramNum >>> 7) & 0x1)), (byte)(paramNum & 127),(byte)((c >>> 4) & 15), (byte)(c & 15), (byte)0xF7 };
            return data;
            }
        else if ((key.startsWith("dhgs1harm") || key.startsWith("dhgs2harm")) && !(key.equals("dhgs1harmsel") || key.equals("dhgs2harmsel")))  // harmonics
            {
            String[] numbers = key.split("[\\D]+");
            //System.err.println(key);
            //for(int i = 0; i < numbers.length; i++)
            //      System.err.println(numbers[i]);
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
            System.err.println("Unknown Key " + key);
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


    public int parse(byte[] result, boolean ignorePatch, boolean fromFile)
        {
        model.set("bank", result[7] / 12);
        model.set("number", result[7] % 12);

        // denybblize
        byte[] data = new byte[492];

        int v = 8;
        for(int i = 0; i < data.length; i++)
            {
            data[i] = (byte)(((result[v++] & 0xF) << 4) | (result[v++] & 0xF));
//                      System.err.println("" + i + " " + data[i]);
            }

        // Name ...
        
        try
            {
            model.set("name", new String(data, 0, 8, "US-ASCII"));
            }
        catch (UnsupportedEncodingException ex) { } // won't happen
        
        int pos = 8;
        
        // Pre-DHG Parameters...

        pos = unloadData(data, pos, volume, basics1pedalassign);
        pos = unloadData(data, pos, basics1pedalassign, portamentosw, new int[] { 4, 0 }, new int[] { 4, 4 } );
        pos = unloadData(data, pos, portamentosw, mode, new int[] { 7, 0 }, new int[] { 1, 6 } );
        pos = unloadData(data, pos, mode, dfgs1coarse, new int[] { 2, 0 }, new int[] { 1, 2 } );
        pos = unloadData(data, pos, dfgs1coarse, dfgs1key);
        pos = unloadData(data, pos, dfgs1key, dfgs1envdep, new int[] { 7, 0 }, new int[] { 1, 7 } );
        pos = unloadData(data, pos, dfgs1envdep, dfgs1envloop);
        pos = unloadData(data, pos, dfgs1envloop, dfgs1envrateseg1, new int[] { 7, 0 }, new int[] { 1, 7 } );
        pos = unloadData(data, pos, dfgs1envrateseg1, dhgs1velodep);  // in fact it just goes to the harmonics
        
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
        
        pos = unloadData(data, pos, dhgs1velodep, dhgs1env1onoff);
        pos = unloadData(data, pos, dhgs1env1onoff, dhgs1modonoff, new int[] { 7, 0 }, new int[] { 1, 5 } );
        pos = unloadData(data, pos, dhgs1modonoff, dhgs1rangefrom, new int[] { 7, 0 }, new int[] { 1, 2 } );
        pos = unloadData(data, pos, dhgs1rangefrom, dhgs1oddmodonoff);
        pos = unloadData(data, pos, dhgs1oddmodonoff, dhgs1allmodonoff, new int[] { 7, 4, 3, 0 }, new int[] { 1, 2, 1, 2 } );
        pos = unloadData(data, pos, dhgs1allmodonoff, dhgs1angle, new int[] { 7, 4 }, new int[] { 1, 2 } );
        pos = unloadData(data, pos, dhgs1angle, dhgs1shadowonoff);

        int shadowpos = pos;

        pos = unloadDataUnified(data, pos, dhgs1env1maxseg1onoff, dhgs1env1seg1rate, new int[] { 6, 0 }, new int[] { 1, 6 }, new int[] { dhgs1shadowonoff, dhgs2shadowonoff } );
                
        // handle shadow specially
        model.set("dhgs1shadowonoff", (data[shadowpos] & 255) >>> 7);
        model.set("dhgs2shadowonoff", (data[shadowpos + 1] & 255) >>> 7);
        
        pos = unloadData(data, pos, dhgs1env1seg1rate, dhgs1env2maxseg1onoff);
        pos = unloadDataUnified(data, pos, dhgs1env2maxseg1onoff, dhgs1env2seg1rate, new int[] { 6, 0 }, new int[] { 1, 6 } );
        pos = unloadData(data, pos, dhgs1env2seg1rate, dhgs1env3maxseg1onoff);
        pos = unloadDataUnified(data, pos, dhgs1env3maxseg1onoff, dhgs1env3seg1rate, new int[] { 6, 0 }, new int[] { 1, 6 } );
        pos = unloadData(data, pos, dhgs1env3seg1rate, dhgs1env4maxseg1onoff);
        pos = unloadDataUnified(data, pos, dhgs1env4maxseg1onoff, dhgs1env4seg1rate, new int[] { 6, 0 }, new int[] { 1, 6 } );
        pos = unloadData(data, pos, dhgs1env4seg1rate, ddfs1ddfonoff);
        pos = unloadData(data, pos, ddfs1ddfonoff, ddfs1envseg1rate, new int[] { 7, 6, 0 }, new int[] { 1, 1, 5 } );
        pos = unloadData(data, pos, ddfs1envseg1rate, ddfs1envmaxseg1onoff);
        pos = unloadDataUnified(data, pos, ddfs1envmaxseg1onoff, ddas1attackvelodep, new int[] { 6, 0 }, new int[] { 1, 6 } );
        pos = unloadData(data, pos, ddas1attackvelodep, ddas1ddaonoff);
        pos = unloadData(data, pos, ddas1ddaonoff, ddas1attackvelrate, new int[] { 7, 0 }, new int[] { 1, 7 } );
        pos = unloadData(data, pos, ddas1attackvelrate, ddas1envseg1modonoff);
        pos = unloadData(data, pos, ddas1envseg1modonoff, ddas1envmaxseg1onoff, new int[] { 6, 0 }, new int[] { 1, 6 } );
        pos = unloadDataUnified(data, pos, ddas1envmaxseg1onoff, lfoshape, new int[] { 6, 0 }, new int[] { 1, 6 } );
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

        pos = loadData(data, pos, volume, basics1pedalassign);
        pos = loadData(data, pos, basics1pedalassign, portamentosw, new int[] { 4, 0 }, new int[] { 4, 4 } );
        pos = loadData(data, pos, portamentosw, mode, new int[] { 7, 0 }, new int[] { 1, 6 } );
        pos = loadData(data, pos, mode, dfgs1coarse, new int[] { 2, 0 }, new int[] { 1, 2 } );
        pos = loadData(data, pos, dfgs1coarse, dfgs1key);
        pos = loadData(data, pos, dfgs1key, dfgs1envdep, new int[] { 7, 0 }, new int[] { 1, 7 } );
        pos = loadData(data, pos, dfgs1envdep, dfgs1envloop);
        pos = loadData(data, pos, dfgs1envloop, dfgs1envrateseg1, new int[] { 7, 0 }, new int[] { 1, 7 } );
        pos = loadData(data, pos, dfgs1envrateseg1, dhgs1velodep);  // in fact it just goes to the harmonics
        
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
        
        pos = loadData(data, pos, dhgs1velodep, dhgs1env1onoff);
        pos = loadData(data, pos, dhgs1env1onoff, dhgs1modonoff, new int[] { 7, 0 }, new int[] { 1, 5 } );
        pos = loadData(data, pos, dhgs1modonoff, dhgs1rangefrom, new int[] { 7, 0 }, new int[] { 1, 2 } );
        pos = loadData(data, pos, dhgs1rangefrom, dhgs1oddmodonoff);
        pos = loadData(data, pos, dhgs1oddmodonoff, dhgs1allmodonoff, new int[] { 7, 4, 3, 0 }, new int[] { 1, 2, 1, 2 } );
        pos = loadData(data, pos, dhgs1allmodonoff, dhgs1angle, new int[] { 7, 4 }, new int[] { 1, 2 } );
        pos = loadData(data, pos, dhgs1angle, dhgs1shadowonoff);

        int shadowpos = pos;

        pos = loadDataUnified(data, pos, dhgs1env1maxseg1onoff, dhgs1env1seg1rate, new int[] { 6, 0 }, new int[] { 1, 6 }, new int[] { dhgs1shadowonoff, dhgs2shadowonoff } );
        
        // handle shadow specially
        data[shadowpos] = (byte)(data[shadowpos] | (model.get("dhgs1shadowonoff", 0) << 7));
        data[shadowpos + 1] = (byte)(data[shadowpos + 1] | (model.get("dhgs2shadowonoff", 0) << 7));
        
        pos = loadData(data, pos, dhgs1env1seg1rate, dhgs1env2maxseg1onoff);
        pos = loadDataUnified(data, pos, dhgs1env2maxseg1onoff, dhgs1env2seg1rate, new int[] { 6, 0 }, new int[] { 1, 6 } );
        pos = loadData(data, pos, dhgs1env2seg1rate, dhgs1env3maxseg1onoff);
        pos = loadDataUnified(data, pos, dhgs1env3maxseg1onoff, dhgs1env3seg1rate, new int[] { 6, 0 }, new int[] { 1, 6 } );
        pos = loadData(data, pos, dhgs1env3seg1rate, dhgs1env4maxseg1onoff);
        pos = loadDataUnified(data, pos, dhgs1env4maxseg1onoff, dhgs1env4seg1rate, new int[] { 6, 0 }, new int[] { 1, 6 } );
        pos = loadData(data, pos, dhgs1env4seg1rate, ddfs1ddfonoff);
        pos = loadData(data, pos, ddfs1ddfonoff, ddfs1envseg1rate, new int[] { 7, 6, 0 }, new int[] { 1, 1, 5 } );
        pos = loadData(data, pos, ddfs1envseg1rate, ddfs1envmaxseg1onoff);
        pos = loadDataUnified(data, pos, ddfs1envmaxseg1onoff, ddas1attackvelodep, new int[] { 6, 0 }, new int[] { 1, 6 } );
        pos = loadData(data, pos, ddas1attackvelodep, ddas1ddaonoff);
        pos = loadData(data, pos, ddas1ddaonoff, ddas1attackvelrate, new int[] { 7, 0 }, new int[] { 1, 7 } );
        pos = loadData(data, pos, ddas1attackvelrate, ddas1envseg1modonoff);
        pos = loadData(data, pos, ddas1envseg1modonoff, ddas1envmaxseg1onoff, new int[] { 6, 0 }, new int[] { 1, 6 } );
        pos = loadDataUnified(data, pos, ddas1envmaxseg1onoff, lfoshape, new int[] { 6, 0 }, new int[] { 1, 6 } );
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
//              System.err.println("" + (i + 8) + " " + data[i]);
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
            (data[3] == (byte)0x20) &&
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
    "basics1delay",                         // 10
    "basics2delay",
    "basics1pedaldep",
    "basics2pedaldep",
    "basics1wheeldep",
    "basics2wheeldep",
    "basics1pedalassign",
    "basics1wheelassign",
    "basics2pedalassign",
    "basics2wheelassign",
    "portamentosw",                         // 20
    "portamentospeed",
    "mode",
    "picmode",
        
/// DFG
        
    "dfgs1coarse",
    "dfgs2coarse",
    "dfgs1fine",
    "dfgs2fine",
    "dfgs1key",
    "dfgs1fixno",
    "dfgs2key",                                     // 30
    "dfgs2fixno",
    "dfgs1envdep",
    "dfgs2envdep",
    "dfgs1prsdep",
    "dfgs2prsdep",
    "dfgs1benderdep",
    "dfgs2benderdep",
    "dfgs1veloenvdep",
    "dfgs2veloenvdep",
    "dfgs1lfodep",                          // 40
    "dfgs2lfodep",
    "dfgs1preslfodep",
    "dfgs2preslfodep",
    "dfgs1envloop",
    "dfgs1envrateseg1",
    "dfgs2envloop",
    "dfgs2envrateseg1",
    "dfgs1envrateseg2",
    "dfgs2envrateseg2",
    "dfgs1envrateseg3",                             // 50
    "dfgs2envrateseg3",
    "dfgs1envrateseg4",
    "dfgs2envrateseg4",
    "dfgs1envrateseg5",
    "dfgs2envrateseg5",
    "dfgs1envrateseg6",
    "dfgs2envrateseg6",
    "dfgs1envlevelseg1",
    "dfgs2envlevelseg1",
    "dfgs1envlevelseg2",                    // 60
    "dfgs2envlevelseg2",
    "dfgs1envlevelseg3",
    "dfgs2envlevelseg3",
    "dfgs1envlevelseg4",
    "dfgs2envlevelseg4",
    "dfgs1envlevelseg5",
    "dfgs2envlevelseg5",
    "dfgs1envlevelseg6",
    "dfgs2envlevelseg6",
        
/// DHG
/// Harmonics not listed, but then...
        
    "dhgs1velodep",                                 // 70
    "dhgs2velodep",
    "dhgs1presdep",
    "dhgs2presdep",
    "dhgs1ksdep",
    "dhgs2ksdep",
    "dhgs1lfodep",
    "dhgs2lfodep",
    "dhgs1env1onoff",                               // 80
    "dhgs1env1moddepth",    // mistake,
    "dhgs2env1onoff",
    "dhgs2env1moddepth",    // mistake,
    "dhgs1env2onoff",
    "dhgs1env2moddepth",    // mistake,
    "dhgs2env2onoff",
    "dhgs2env2moddepth",    // mistake,
    "dhgs1env3onoff",
    "dhgs1env3moddepth",    // mistake,
    "dhgs2env3onoff",
    "dhgs2env3moddepth",    // mistake,
    "dhgs1env4onoff",                               // 90
    "dhgs1env4moddepth",    // mistake,
    "dhgs2env4onoff",
    "dhgs2env4moddepth",    // mistake,
    "dhgs1modonoff",
    "dhgs1harmsel",
    "dhgs2modonoff",
    "dhgs2harmsel",
    "dhgs1rangefrom",
    "dhgs2rangefrom",
    "dhgs1rangeto",                         // 100
    "dhgs2rangeto",
    "dhgs1oddmodonoff",
    "dhgs1oddenv",
    "dhgs1evenmodonoff",    // this must be an error -- was 
    "dhgs1evenenv",
    "dhgs2oddmodonoff",
    "dhgs2oddenv",
    "dhgs2evenmodonoff",    // this must be an error -- was 
    "dhgs2evenenv",
    "dhgs1octavemodonoff",                  // 110
    "dhgs1octaveenv",
    "dhgs1fifthmodonoff",
    "dhgs1fifthenv",
    "dhgs2octavemodonoff",
    "dhgs2octaveenv",
    "dhgs2fifthmodonoff",
    "dhgs2fifthenv",
    "dhgs1allmodonoff",
    "dhgs1allenv",                          // 120
    "dhgs2allmodonoff",
    "dhgs2allenv",
    "dhgs1angle",
    "dhgs2angle",
    "dhgs1harm",
    "dhgs2harm",
    "dhgs1shadowonoff",     // mistake,
    "dhgs1env1maxsegonoff", // mistake,
    "dhgs1env1seg1level",
    "dhgs2shadowonoff",     // mistake,
    "dhgs2env1maxsegonoff", // mistake,                     130
    "dhgs2env1seg1level",
    "dhgs1env1maxsegonoff", // mistake,
    "dhgs1env1seg2level",
    "dhgs2env1maxsegonoff", // mistake,
    "dhgs2env1seg2level",
    "dhgs1env1maxsegonoff", // mistake,
    "dhgs1env1seg3level",
    "dhgs2env1maxsegonoff", // mistake,
    "dhgs2env1seg3level",
    "dhgs1env1maxsegonoff", // mistake,                     140
    "dhgs1env1seg4level",
    "dhgs2env1maxsegonoff", // mistake,
    "dhgs2env1seg4level",
    "dhgs1env1maxsegonoff", // mistake,
    "dhgs1env1seg5level",
    "dhgs2env1maxsegonoff", // mistake,
    "dhgs2env1seg5level",
    "dhgs1env1maxsegonoff", // mistake,
    "dhgs1env1seg6level",
    "dhgs2env1maxsegonoff", // mistake,                     // 150
    "dhgs2env1seg6level",
    "dhgs1env1seg1rate",                                    
    "dhgs2env1seg1rate",
    "dhgs1env1seg2rate",
    "dhgs2env1seg2rate",
    "dhgs1env1seg3rate",
    "dhgs2env1seg3rate",
    "dhgs1env1seg4rate",
    "dhgs2env1seg4rate",
    "dhgs1env1seg5rate",                                    // 160
    "dhgs2env1seg5rate",
    "dhgs1env1seg6rate",
    "dhgs2env1seg6rate",
    "dhgs1env2maxsegonoff", // mistake,
    "dhgs1env2seg1level",
    "dhgs2env2maxsegonoff", // mistake,
    "dhgs2env2seg1level",
    "dhgs1env2maxsegonoff", // mistake,
    "dhgs1env2seg2level",
    "dhgs2env2maxsegonoff", // mistake,             // 170
    "dhgs2env2seg2level",
    "dhgs1env2maxsegonoff", // mistake,
    "dhgs1env2seg3level",
    "dhgs2env2maxsegonoff", // mistake,
    "dhgs2env2seg3level",
    "dhgs1env2maxsegonoff", // mistake,
    "dhgs1env2seg4level",
    "dhgs2env2maxsegonoff", // mistake,
    "dhgs2env2seg4level",
    "dhgs1env2maxsegonoff", // mistake,             // 180
    "dhgs1env2seg5level",
    "dhgs2env2maxsegonoff", // mistake,
    "dhgs2env2seg5level",
    "dhgs1env2maxsegonoff", // mistake,
    "dhgs1env2seg6level",
    "dhgs2env2maxsegonoff", // mistake,
    "dhgs2env2seg6level",
    "dhgs1env2seg1rate",
    "dhgs2env2seg1rate",
    "dhgs1env2seg2rate",                                    // 190
    "dhgs2env2seg2rate",
    "dhgs1env2seg3rate",
    "dhgs2env2seg3rate",
    "dhgs1env2seg4rate",
    "dhgs2env2seg4rate",
    "dhgs1env2seg5rate",
    "dhgs2env2seg5rate",
    "dhgs1env2seg6rate",
    "dhgs2env2seg6rate",
    "dhgs1env3maxsegonoff", // mistake,             // 200
    "dhgs1env3seg1level",
    "dhgs2env3maxsegonoff", // mistake,
    "dhgs2env3seg1level",
    "dhgs1env3maxsegonoff", // mistake,
    "dhgs1env3seg2level",
    "dhgs2env3maxsegonoff", // mistake,
    "dhgs2env3seg2level",
    "dhgs1env3maxsegonoff", // mistake,
    "dhgs1env3seg3level",
    "dhgs2env3maxsegonoff", // mistake,                     // 210
    "dhgs2env3seg3level",
    "dhgs1env3maxsegonoff", // mistake,
    "dhgs1env3seg4level",
    "dhgs2env3maxsegonoff", // mistake,
    "dhgs2env3seg4level",
    "dhgs1env3maxsegonoff", // mistake,
    "dhgs1env3seg5level",
    "dhgs2env3maxsegonoff", // mistake,
    "dhgs2env3seg5level",
    "dhgs1env3maxsegonoff", // mistake,                     // 220
    "dhgs1env3seg6level",
    "dhgs2env3maxsegonoff", // mistake,
    "dhgs2env3seg6level",
    "dhgs1env3seg1rate",
    "dhgs2env3seg1rate",
    "dhgs1env3seg2rate",
    "dhgs2env3seg2rate",
    "dhgs1env3seg3rate",
    "dhgs2env3seg3rate",
    "dhgs1env3seg4rate",                            // 230
    "dhgs2env3seg4rate",
    "dhgs1env3seg5rate",
    "dhgs2env3seg5rate",
    "dhgs1env3seg6rate",
    "dhgs2env3seg6rate",
    "dhgs1env4maxsegonoff", // mistake,
    "dhgs1env4seg1level",
    "dhgs2env4maxsegonoff", // mistake,
    "dhgs2env4seg1level",
    "dhgs1env4maxsegonoff", // mistake,             // 240
    "dhgs1env4seg2level",
    "dhgs2env4maxsegonoff", // mistake,
    "dhgs2env4seg2level",
    "dhgs1env4maxsegonoff", // mistake,
    "dhgs1env4seg3level",
    "dhgs2env4maxsegonoff", // mistake,
    "dhgs2env4seg3level",
    "dhgs1env4maxsegonoff", // mistake,
    "dhgs1env4seg4level",
    "dhgs2env4maxsegonoff", // mistake,             // 250
    "dhgs2env4seg4level",
    "dhgs1env4maxsegonoff", // mistake,
    "dhgs1env4seg5level",
    "dhgs2env4maxsegonoff", // mistake,
    "dhgs2env4seg5level",
    "dhgs1env4maxsegonoff", // mistake,
    "dhgs1env4seg6level",
    "dhgs2env4maxsegonoff", // mistake,
    "dhgs2env4seg6level",
    "dhgs1env4seg1rate",                                            // 260
    "dhgs2env4seg1rate",
    "dhgs1env4seg2rate",
    "dhgs2env4seg2rate",
    "dhgs1env4seg3rate",
    "dhgs2env4seg3rate",
    "dhgs1env4seg4rate",
    "dhgs2env4seg4rate",
    "dhgs1env4seg5rate",
    "dhgs2env4seg5rate",
    "dhgs1env4seg6rate",                                            // 270
    "dhgs2env4seg6rate",
        
/// DDF
        
    "ddfs1cutoff",
    "ddfs2cutoff",
    "ddfs1cutoffmod",
    "ddfs2cutoffmod",
    "ddfs1slope",
    "ddfs2slope",
    "ddfs1slopemod",
    "ddfs2slopemod",
    "ddfs1flatlevel",                                       // 280
    "ddfs2flatlevel",
    "ddfs1velodep",
    "ddfs2velodep",
    "ddfs1presdep",
    "ddfs2presdep",
    "ddfs1ksdep",
    "ddfs2ksdep",
    "ddfs1envdep",
    "ddfs2envdep",
    "ddfs1veloenvdep",                              // 290
    "ddfs2veloenvdep",
    "ddfs1ddfonoff",
    "ddfs1ddfmodonoff",
    "ddfs1lfodep",
    "ddfs2ddfonoff",
    "ddfs2ddfmodonoff",
    "ddfs2lfodep",
    "ddfs1envseg1rate",
    "ddfs2envseg1rate",
    "ddfs1envseg2rate",                             // 300
    "ddfs2envseg2rate",
    "ddfs1envseg3rate",
    "ddfs2envseg3rate",
    "ddfs1envseg4rate",
    "ddfs2envseg4rate",
    "ddfs1envseg5rate",
    "ddfs2envseg5rate",
    "ddfs1envseg6rate",
    "ddfs2envseg6rate",
    "ddfs1envmaxsegonoff",  // mistake,                     // 310
    "ddfs1envseg1level",
    "ddfs2envmaxsegonoff",  // mistake,
    "ddfs2envseg1level",
    "ddfs1envmaxsegonoff",  // mistake,
    "ddfs1envseg2level",
    "ddfs2envmaxsegonoff",  // mistake,
    "ddfs2envseg2level",
    "ddfs1envmaxsegonoff",  // mistake,
    "ddfs1envseg3level",
    "ddfs2envmaxsegonoff",  // mistake,                     // 320
    "ddfs2envseg3level",
    "ddfs1envmaxsegonoff",  // mistake,
    "ddfs1envseg4level",
    "ddfs2envmaxsegonoff",  // mistake,
    "ddfs2envseg4level",
    "ddfs1envmaxsegonoff",  // mistake,
    "ddfs1envseg5level",
    "ddfs2envmaxsegonoff",  // mistake,
    "ddfs2envseg5level",
    "ddfs1envmaxsegonoff",  // mistake,                     // 330  
    "ddfs1envseg6level",
    "ddfs2envmaxsegonoff",  // mistake,
    "ddfs2envseg6level",
        
/// DDA
        
    "ddas1attackvelodep",   // mistake,
    "ddas2attackvelodep",   // mistake,
    "ddas1presdep",
    "ddas2presdep",
    "ddas1ksdep",
    "ddas2ksdep",
    "ddas1ddaonoff",                                                        // 340
    "ddas1lfodep",
    "ddas2ddaonoff",
    "ddas2lfodep",
    "ddas1attackvelrate",   // mistake,
    "ddas2attackvelrate",   // mistake,
    "ddas1releasevelrate",
    "ddas2releasevelrate",
    "ddas1ksrate",
    "ddas2ksrate",
    "ddas1envseg1modonoff",                                         // 350
    "ddas1envseg1rate",
    "ddas2envseg1modonoff",
    "ddas2envseg1rate",
    "ddas1envseg2modonoff",
    "ddas1envseg2rate",
    "ddas2envseg2modonoff",
    "ddas2envseg2rate",
    "ddas1envseg3modonoff",
    "ddas1envseg3rate",
    "ddas2envseg3modonoff",                                         // 360
    "ddas2envseg3rate",
    "ddas1envseg4modonoff",
    "ddas1envseg4rate",
    "ddas2envseg4modonoff",
    "ddas2envseg4rate",
    "ddas1envseg5modonoff",
    "ddas1envseg5rate",
    "ddas2envseg5modonoff",
    "ddas2envseg5rate",
    "ddas1envseg6modonoff",                                         // 370
    "ddas1envseg6rate",
    "ddas2envseg6modonoff",
    "ddas2envseg6rate",
    "ddas1envseg7modonoff",
    "ddas1envseg7rate",
    "ddas2envseg7modonoff",
    "ddas2envseg7rate",
    "ddas1envmaxsegonoff",  // mistake,
    "ddas1envseg1level",
    "ddas2envmaxsegonoff",  // mistake,                     // 380
    "ddas2envseg1level",
    "ddas1envmaxsegonoff",  // mistake,
    "ddas1envseg2level",
    "ddas2envmaxsegonoff",  // mistake,
    "ddas2envseg2level",
    "ddas1envmaxsegonoff",  // mistake,
    "ddas1envseg3level",
    "ddas2envmaxsegonoff",  // mistake,
    "ddas2envseg3level",
    "ddas1envmaxsegonoff",  // mistake,                     // 390
    "ddas1envseg4level",
    "ddas2envmaxsegonoff",  // mistake,
    "ddas2envseg4level",
    "ddas1envmaxsegonoff",  // mistake,
    "ddas1envseg5level",
    "ddas2envmaxsegonoff",  // mistake,
    "ddas2envseg5level",
    "ddas1envmaxsegonoff",  // mistake,
    "ddas1envseg6level",
    "ddas2envmaxsegonoff",  // mistake,
    "ddas2envseg6level",
        
/// LFO
        
    "lfoshape",
    "lfospeed",
    "lfodelay",
    "lfotrend",
        
/// KS
        
    "kss1right",
    "kss2right",
    "kss1left",
    "kss2left",
    "kss1breakpoint",                                                       // 410
    "kss2breakpoint",
        
/// DFT
        
    "dftonoff",
    "dftcneg1level",        // mistake,
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
    "dhgs1harm1level",
    "dhgs2harm1level",
    "dhgs1harm2level",
    "dhgs2harm2level",
    "dhgs1harm3level",
    "dhgs2harm3level",
    "dhgs1harm4level",
    "dhgs2harm4level",
    "dhgs1harm5level",
    "dhgs2harm5level",
    "dhgs1harm6level",
    "dhgs2harm6level",
    "dhgs1harm7level",
    "dhgs2harm7level",
    "dhgs1harm8level",
    "dhgs2harm8level",
    "dhgs1harm9level",
    "dhgs2harm9level",
    "dhgs1harm10level",
    "dhgs2harm10level",
    "dhgs1harm11level",
    "dhgs2harm11level",
    "dhgs1harm12level",
    "dhgs2harm12level",
    "dhgs1harm13level",
    "dhgs2harm13level",
    "dhgs1harm14level",
    "dhgs2harm14level",
    "dhgs1harm15level",
    "dhgs2harm15level",
    "dhgs1harm16level",
    "dhgs2harm16level",
    "dhgs1harm17level",
    "dhgs2harm17level",
    "dhgs1harm18level",
    "dhgs2harm18level",
    "dhgs1harm19level",
    "dhgs2harm19level",
    "dhgs1harm20level",
    "dhgs2harm20level",
    "dhgs1harm21level",
    "dhgs2harm21level",
    "dhgs1harm22level",
    "dhgs2harm22level",
    "dhgs1harm23level",
    "dhgs2harm23level",
    "dhgs1harm24level",
    "dhgs2harm24level",
    "dhgs1harm25level",
    "dhgs2harm25level",
    "dhgs1harm26level",
    "dhgs2harm26level",
    "dhgs1harm27level",
    "dhgs2harm27level",
    "dhgs1harm28level",
    "dhgs2harm28level",
    "dhgs1harm29level",
    "dhgs2harm29level",
    "dhgs1harm30level",
    "dhgs2harm30level",
    "dhgs1harm31level",
    "dhgs2harm31level",
    "dhgs1harm32level",
    "dhgs2harm32level",
    "dhgs1harm33level",
    "dhgs2harm33level",
    "dhgs1harm34level",
    "dhgs2harm34level",
    "dhgs1harm35level",
    "dhgs2harm35level",
    "dhgs1harm36level",
    "dhgs2harm36level",
    "dhgs1harm37level",
    "dhgs2harm37level",
    "dhgs1harm38level",
    "dhgs2harm38level",
    "dhgs1harm39level",
    "dhgs2harm39level",
    "dhgs1harm40level",
    "dhgs2harm40level",
    "dhgs1harm41level",
    "dhgs2harm41level",
    "dhgs1harm42level",
    "dhgs2harm42level",
    "dhgs1harm43level",
    "dhgs2harm43level",
    "dhgs1harm44level",
    "dhgs2harm44level",
    "dhgs1harm45level",
    "dhgs2harm45level",
    "dhgs1harm46level",
    "dhgs2harm46level",
    "dhgs1harm47level",
    "dhgs2harm47level",
    "dhgs1harm48level",
    "dhgs2harm48level",
    "dhgs1harm49level",
    "dhgs2harm49level",
    "dhgs1harm50level",
    "dhgs2harm50level",
    "dhgs1harm51level",
    "dhgs2harm51level",
    "dhgs1harm52level",
    "dhgs2harm52level",
    "dhgs1harm53level",
    "dhgs2harm53level",
    "dhgs1harm54level",
    "dhgs2harm54level",
    "dhgs1harm55level",
    "dhgs2harm55level",
    "dhgs1harm56level",
    "dhgs2harm56level",
    "dhgs1harm57level",
    "dhgs2harm57level",
    "dhgs1harm58level",
    "dhgs2harm58level",
    "dhgs1harm59level",
    "dhgs2harm59level",
    "dhgs1harm60level",
    "dhgs2harm60level",
    "dhgs1harm61level",
    "dhgs2harm61level",
    "dhgs1harm62level",
    "dhgs2harm62level",
    "dhgs1harm63level",
    "dhgs2harm63level",
    "dhgs1harm2envselmodyn",
    "dhgs1harm1envselmodyn",
    "dhgs2harm2envselmodyn",
    "dhgs2harm1envselmodyn",
    "dhgs1harm4envselmodyn",
    "dhgs1harm3envselmodyn",
    "dhgs2harm4envselmodyn",
    "dhgs2harm3envselmodyn",
    "dhgs1harm6envselmodyn",
    "dhgs1harm5envselmodyn",
    "dhgs2harm6envselmodyn",
    "dhgs2harm5envselmodyn",
    "dhgs1harm8envselmodyn",
    "dhgs1harm7envselmodyn",
    "dhgs2harm8envselmodyn",
    "dhgs2harm7envselmodyn",
    "dhgs1harm10envselmodyn",
    "dhgs1harm9envselmodyn",
    "dhgs2harm10envselmodyn",
    "dhgs2harm9envselmodyn",
    "dhgs1harm12envselmodyn",
    "dhgs1harm11envselmodyn",
    "dhgs2harm12envselmodyn",
    "dhgs2harm11envselmodyn",
    "dhgs1harm14envselmodyn",
    "dhgs1harm13envselmodyn",
    "dhgs2harm14envselmodyn",
    "dhgs2harm13envselmodyn",
    "dhgs1harm16envselmodyn",
    "dhgs1harm15envselmodyn",
    "dhgs2harm16envselmodyn",
    "dhgs2harm15envselmodyn",
    "dhgs1harm18envselmodyn",
    "dhgs1harm17envselmodyn",
    "dhgs2harm18envselmodyn",
    "dhgs2harm17envselmodyn",
    "dhgs1harm20envselmodyn",
    "dhgs1harm19envselmodyn",
    "dhgs2harm20envselmodyn",
    "dhgs2harm19envselmodyn",
    "dhgs1harm22envselmodyn",
    "dhgs1harm21envselmodyn",
    "dhgs2harm22envselmodyn",
    "dhgs2harm21envselmodyn",
    "dhgs1harm24envselmodyn",
    "dhgs1harm23envselmodyn",
    "dhgs2harm24envselmodyn",
    "dhgs2harm23envselmodyn",
    "dhgs1harm26envselmodyn",
    "dhgs1harm25envselmodyn",
    "dhgs2harm26envselmodyn",
    "dhgs2harm25envselmodyn",
    "dhgs1harm28envselmodyn",
    "dhgs1harm27envselmodyn",
    "dhgs2harm28envselmodyn",
    "dhgs2harm27envselmodyn",
    "dhgs1harm30envselmodyn",
    "dhgs1harm29envselmodyn",
    "dhgs2harm30envselmodyn",
    "dhgs2harm29envselmodyn",
    "dhgs1harm32envselmodyn",
    "dhgs1harm31envselmodyn",
    "dhgs2harm32envselmodyn",
    "dhgs2harm31envselmodyn",
    "dhgs1harm34envselmodyn",
    "dhgs1harm33envselmodyn",
    "dhgs2harm34envselmodyn",
    "dhgs2harm33envselmodyn",
    "dhgs1harm36envselmodyn",
    "dhgs1harm35envselmodyn",
    "dhgs2harm36envselmodyn",
    "dhgs2harm35envselmodyn",
    "dhgs1harm38envselmodyn",
    "dhgs1harm37envselmodyn",
    "dhgs2harm38envselmodyn",
    "dhgs2harm37envselmodyn",
    "dhgs1harm40envselmodyn",
    "dhgs1harm39envselmodyn",
    "dhgs2harm40envselmodyn",
    "dhgs2harm39envselmodyn",
    "dhgs1harm42envselmodyn",
    "dhgs1harm41envselmodyn",
    "dhgs2harm42envselmodyn",
    "dhgs2harm41envselmodyn",
    "dhgs1harm44envselmodyn",
    "dhgs1harm43envselmodyn",
    "dhgs2harm44envselmodyn",
    "dhgs2harm43envselmodyn",
    "dhgs1harm46envselmodyn",
    "dhgs1harm45envselmodyn",
    "dhgs2harm46envselmodyn",
    "dhgs2harm45envselmodyn",
    "dhgs1harm48envselmodyn",
    "dhgs1harm47envselmodyn",
    "dhgs2harm48envselmodyn",
    "dhgs2harm47envselmodyn",
    "dhgs1harm50envselmodyn",
    "dhgs1harm49envselmodyn",
    "dhgs2harm50envselmodyn",
    "dhgs2harm49envselmodyn",
    "dhgs1harm52envselmodyn",
    "dhgs1harm51envselmodyn",
    "dhgs2harm52envselmodyn",
    "dhgs2harm51envselmodyn",
    "dhgs1harm54envselmodyn",
    "dhgs1harm53envselmodyn",
    "dhgs2harm54envselmodyn",
    "dhgs2harm53envselmodyn",
    "dhgs1harm56envselmodyn",
    "dhgs1harm55envselmodyn",
    "dhgs2harm56envselmodyn",
    "dhgs2harm55envselmodyn",
    "dhgs1harm58envselmodyn",
    "dhgs1harm57envselmodyn",
    "dhgs2harm58envselmodyn",
    "dhgs2harm57envselmodyn",
    "dhgs1harm60envselmodyn",
    "dhgs1harm59envselmodyn",
    "dhgs2harm60envselmodyn",
    "dhgs2harm59envselmodyn",
    "dhgs1harm62envselmodyn",
    "dhgs1harm61envselmodyn",
    "dhgs2harm62envselmodyn",
    "dhgs2harm61envselmodyn",
    "dhgs1harm63envselmodyn",
    "dhgs2harm63envselmodyn",
    "dhgs2harm63envselmodyn"
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
//              System.err.println("" + pos +  "(" + data[pos] + "): 0 - 7");
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
//                      System.err.println("" + pos + "(" + data[pos] + "): " + bit[b] + " - " + (bit[b] + len[b]));
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
//                      System.err.println("" + pos + "(" + data[pos] + "): " + bit[b] + " - " + (bit[b] + len[b]));
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
    public static final int basics1pedalassign = 16;
    public static final int portamentosw = 20;
    public static final int mode = 22;
    public static final int dfgs1coarse = 24;
    public static final int dfgs1key = 28;
    public static final int dfgs1envdep = 32;
    public static final int dfgs1envloop = 44;
    public static final int dfgs1envrateseg1 = 48;

// harmonics go here -- we handle them specially

    public static final int dhgs1velodep = 70;
    public static final int dhgs1env1onoff = 78;
    public static final int dhgs1modonoff = 94;
    public static final int dhgs1rangefrom = 98;
    public static final int dhgs1oddmodonoff = 102;
    public static final int dhgs1allmodonoff = 118;
    public static final int dhgs1angle = 122;
    public static final int dhgs1shadowonoff = 126;
    public static final int dhgs1env1maxseg1onoff = 127;
    public static final int dhgs2shadowonoff = 129;
    public static final int dhgs2env1maxseg1onoff = 130;
    public static final int dhgs1env1maxseg2onoff = 132;
    public static final int dhgs1env1seg1rate = 152;
    public static final int dhgs1env2maxseg1onoff = 164;
    public static final int dhgs1env2seg1rate = 188;
    public static final int dhgs1env3maxseg1onoff = 200;
    public static final int dhgs1env3seg1rate = 224;
    public static final int dhgs1env4maxseg1onoff = 236;
    public static final int dhgs1env4seg1rate = 260;
    public static final int ddfs1ddfonoff = 292;
    public static final int ddfs1envseg1rate = 298;
    public static final int ddfs1envmaxseg1onoff = 310;
    public static final int ddas1attackvelodep = 334;
    public static final int ddas1ddaonoff = 340;
    public static final int ddas1attackvelrate = 344;
    public static final int ddas1envseg1modonoff = 350;
    public static final int ddas1envmaxseg1onoff = 378;

// some zeros go here!

    public static final int lfoshape = 402;
    public static final int dftonoff = 412;
    public static final int dftc0level = 414;

// a zero goes here! 
    
    
    }
