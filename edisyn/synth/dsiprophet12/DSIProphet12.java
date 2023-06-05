/***
    Copyright 2023 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.dsiprophet12;

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
   A patch editor for the Dave Smith Instruments Prophet 12.
        
   @author Sean Luke
*/

public class DSIProphet12 extends Synth
    {
    public static final String[] BANKS = { "U1", "U2", "U3", "U4", "F1", "F2", "F3", "F4" };
    public static final String[] WRITEABLE_BANKS = { "U1", "U2", "U3", "U4" };
    public static final String[] OSC_SHAPES = { "Off", "Sawtooth", "Pulse", "Triangle", "Sine", "Tines", "Mellow", "Church", "Muted", "Nasal", "Boing", "Gothic", "Ahhh", "Shrill", "Ohhhh", "Buzzzz", "Meh", "Red Noise", "White Noise", "Violet Noise" };
    public static final String[] OSC_WAVES = { "Tines", "Mellow", "Church", "Muted", "Nasal", "Boing", "Gothic", "Ahhh", "Shrill", "Ohhhh", "Buzzzz", "Meh" };
    public static final String[] GLIDE_MODES = { "Fixed Rate", "Fixed Rate A", "Fixed Time", "Fixed Time A" };
    public static final String[] LFO_SYNCS = { "32 Q", "16 Q", "8 Q", "6 Q", "4 Q", "3 Q", "Half", "Qtr D", "Qtr", "4 T", "8", "8 T", "16", "16 T", "32", "32 T" };
    public static final String[] LFO_SHAPES = { "Triangle", "Reverse Saw", "Saw", "Square", "Pulse 1", "Pulse 2", "Pulse 3", "Random" };
    public static final String[] UNISON_KEY_ASSIGNMENTS = { "Low Note", "Low Retrigger", "High Note", "High Retrigger", "Last Note", "Last Retrigger" };
    public static final String[] A_B_MODES = { "Normal", "Split", "Stack" };
    public static final String[] DELAY_FILTER_MODES = { "Low-Pass", "High-Pass" };
    public static final String[] DELAY_SYNCS = { "Whole", "3 Q", "Half", "Qtr", "8 D", "8", "16 D", "16", "32 D", "32", "64" };
    public static final String[] UNISON_MODES = { "1 Voice", "2 Voices", "3 Voices", "4 Voices", "5 Voices", "6 Voices", "7 Voices", "8 Voices", "9 Voices", "10 Voices", "11 Voices", "12 Voices" };
    public static final String[] ARPEGGIATOR_MODES = { "Up", "Down", "Up + Down", "Assign", "Random" };
    public static final String[] ARPEGGIATOR_RANGES = { "1 Octave", "2 Octaves", "3 Octaves" };
    public static final String[] ARPEGGIATOR_CLOCK_DIVISIONS = { "1/2", "1/4", "1/8", "1/8 Half Swing", "1/8 Full Swing", "1/8 Triplet", "1/16", "1/16 Half Swing", "1/16 Full Swing", "1/16 Triplet", "1/32" };
    public static final String[] NOTES = new String[] { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
    public static final String[] FM_MODES = new String[] { "Linear", "Exponential" };
    public static final String[] MOD_SOURCES = { 
        "Off", "Osc 1", "Osc 2", "Osc 3", "Osc 4", "LFO 1", "LFO 2", "LFO 3", "LFO 4", "Lowpass Env", "VCA Env", "Env 3", "Env 4", 
        "Pitchbend", "Mod Wheel", "Slider 1 Position", "Slider 2 Position", "Slider 1 Pressure", "Slider 2 Pressure", "Aftertouch", 
        "CC#2: Breath", "CC#4: Foot Pedal", "CC#11: Expression", "Velocity", "Note Number", "Random", "DC", 
        "Audio Out", "Max"   // NEW
        };
    public static final String[] MOD_DESTINATIONS = { 
        "Off", "Osc 1 Freq", "Osc 2 Freq", "Osc 3 Freq", "Osc 4 Freq", "Osc All Freq", "Osc 1 Level", "Osc 2 Level", "Osc 3 Level", "Osc 4 Level", 
        "Osc 1 Shape Mod", "Osc 2 Shape Mod", "Osc 3 Shape Mod", "Osc 4 Shape Mod", 
        "Osc All Shape Mod",  // NEW
        "Sub Osc Level", "Osc 1 FM", "Osc 2 FM", "Osc 3 FM", "Osc 4 FM", "Osc All FM", 
        "Osc 1 AM", "Osc 2 AM", "Osc 3 AM", "Osc 4 AM", "Osc All AM",
        "Osc 1 Slop", "Osc 2 Slop", "Osc 3 Slop", "Osc 4 Slop", "Osc All Slop",  // NEW
        "Air", "Girth", "Hack", "Decimate", "Drive", "LPF Cutoff", "LPF Resonance", 
        "HPF Cutoff", "HPF Resonance", "VCA", "Pan", "Pan Spread", "Feedback Amount", "Feedback Tuning", "Delay 1 Amount", "Delay 2 Amount", "Delay 3 Amount", "Delay 4 Amount", 
        "Delay 1 Time", "Delay 2 Time", "Delay 3 Time", "Delay 4 Time", 
        "Delay All Time",   // NEW
        "Delay 1 Feedback", "Delay 2 Feedback", "Delay 3 Feedback", "Delay 4 Feedback", 
        "Delay 1 Pan", "Delay 2 Pan", "Delay 3 Pan", "Delay 4 Pan", "LFO 1 Freq", "LFO 2 Freq", "LFO 3 Freq", "LFO 4 Freq", "LFO 1 Amount", "LFO 2 Amount", "LFO 3 Amount", "LFO 4 Amount", 
        "LPF Env Amount", "Amp Env Amount", "Env 3 Amount", "Env 4 Amount", "LPF Env Attack", "Amp Env Attack", "Env 3 Attack", "Env 4 Attack", "All Env Attack", 
        "LPF Env Decay", "Amp Env Decay", "Env 3 Decay", "Env 4 Decay", "All Env Decay", "LPF Env Release", "Amp Env Release", "Env 3 Release", "Env 4 Release", "All Env Release", 
        "Mod 1 Amount", "Mod 2 Amount", "Mod 3 Amount", "Mod 4 Amount", "Mod 5 Amount", "Mod 6 Amount", "Mod 7 Amount", "Mod 8 Amount", "Mod 9 Amount", "Mod 10 Amount", 
        "Mod 11 Amount", "Mod 12 Amount", "Mod 13 Amount", "Mod 14 Amount", "Mod 15 Amount", "Mod 16 Amount" };
    
    public static final int LOAD_BOTH = 0;
    public static final int LOAD_A = 1;
    public static final int LOAD_B = 2;
    int load = LOAD_BOTH;
                
    boolean writeToF;
    public static final String WRITE_TO_F_KEY = "WriteToF";
    
    public DSIProphet12()
        {
        String m = getLastX(WRITE_TO_F_KEY, getSynthClassName());
        writeToF = (m == null ? false : Boolean.parseBoolean(m));
        
        if (parametersToIndex == null)
            {
            parametersToIndex = new HashMap();
            for(int i = 0; i < parameters.length; i++)
                {
                parametersToIndex.put(parameters[i], Integer.valueOf(i));
                }
            }
        
        /// SOUND PANEL
                
        SynthPanel soundPanel = new SynthPanel(this);
        
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        JComponent nameGlobal = addNameGlobal(Style.COLOR_GLOBAL());
        hbox.add(nameGlobal);
        hbox.addLast(addGeneral(1, Style.COLOR_B()));
        vbox.add(hbox);
        
        vbox.add(addOscillator(1, 1, Style.COLOR_A()));
        vbox.add(addOscillator(1, 2, Style.COLOR_A()));
        vbox.add(addOscillator(1, 3, Style.COLOR_A()));
        vbox.add(addOscillator(1, 4, Style.COLOR_A()));
        vbox.add(addCharacter(1, Style.COLOR_C()));

        soundPanel.add(vbox, BorderLayout.CENTER);
        soundPanel.makePasteable("layer");
        soundPanel.setSendsAllParameters(false);
        addTab("Osc A", soundPanel);
                
        /// ENVELOPES PANEL
                
        soundPanel = new SynthPanel(this);
        
        vbox = new VBox();

        hbox = new HBox();
        hbox.add(addAmplifier(1, Style.COLOR_B()));
        hbox.add(addLowPassFilter(1, Style.COLOR_A()));
        hbox.addLast(addHighPassFilter(1, Style.COLOR_A()));
        vbox.add(hbox);

        vbox.add(addEnvelope(1, 1, Style.COLOR_A()));
        vbox.add(addEnvelope(1, 2, Style.COLOR_B()));
        vbox.add(addEnvelope(1, 3, Style.COLOR_C()));
        vbox.add(addEnvelope(1, 4, Style.COLOR_C()));

        soundPanel.add(vbox, BorderLayout.CENTER);
        soundPanel.makePasteable("layer");
        soundPanel.setSendsAllParameters(false);
        addTab("Filter Amp Env A", soundPanel);
                

        /// LFO Delay PANEL
                
        soundPanel = new SynthPanel(this);
        
        hbox = new HBox();
        
        vbox = new VBox();
        vbox.add(addLFO(1, 1, Style.COLOR_A()));
        vbox.add(addLFO(1, 2, Style.COLOR_B()));
        vbox.add(addLFO(1, 3, Style.COLOR_A()));
        vbox.add(addLFO(1, 4, Style.COLOR_B()));
        hbox.add(vbox);
                
        vbox = new VBox();
        vbox.add(addDelay(1, 1, Style.COLOR_B()));
        vbox.add(addDelay(1, 2, Style.COLOR_A()));
        vbox.add(addDelay(1, 3, Style.COLOR_B()));
        vbox.add(addDelay(1, 4, Style.COLOR_A()));
        vbox.add(addDelays(1, Style.COLOR_B()));
        hbox.addLast(vbox);
                
        soundPanel.add(hbox, BorderLayout.CENTER);
        soundPanel.makePasteable("layer");
        soundPanel.setSendsAllParameters(false);
        addTab("LFO Delay A", soundPanel);
                

        // ARP PANEL
                
        SynthPanel arpPanel = new SynthPanel(this);
        
        vbox = new VBox();
        vbox.add(addArpeggiator(1, Style.COLOR_B()));
        vbox.add(addArpeggiatorNotes(1, Style.COLOR_B()));
        
        arpPanel.add(vbox, BorderLayout.CENTER);
        arpPanel.makePasteable("layer");
        addTab("Arp A", arpPanel);

        // MODULATION PANEL
                
        SynthPanel modulationPanel = new SynthPanel(this);
        
        vbox = new VBox();
        vbox.add(addModulation(1, Style.COLOR_B()));
        
        modulationPanel.add(vbox, BorderLayout.CENTER);
        modulationPanel.makePasteable("layer");
        addTab("Mod A", modulationPanel);


        /// SOUND PANEL
                
        soundPanel = new SynthPanel(this);
        
        vbox = new VBox();
        hbox = new HBox();
        VBox sizer = new VBox();
        sizer.add(Strut.makeHorizontalStrut(nameGlobal));
        nameGlobal = addLayerBName(Style.COLOR_A());
        sizer.add(nameGlobal);
        hbox.add(sizer);
        hbox.addLast(addGeneral(2, Style.COLOR_B()));
        vbox.add(hbox);
        
        vbox.add(addOscillator(2, 1, Style.COLOR_A()));
        vbox.add(addOscillator(2, 2, Style.COLOR_A()));
        vbox.add(addOscillator(2, 3, Style.COLOR_A()));
        vbox.add(addOscillator(2, 4, Style.COLOR_A()));
        vbox.add(addCharacter(2, Style.COLOR_C()));

        soundPanel.add(vbox, BorderLayout.CENTER);
        soundPanel.makePasteable("layer");
        soundPanel.setSendsAllParameters(false);
        addTab("Osc B", soundPanel);
                

        /// ENVELOPES PANEL
                
        soundPanel = new SynthPanel(this);
        
        vbox = new VBox();

        hbox = new HBox();
        hbox.add(addAmplifier(2, Style.COLOR_B()));
        hbox.add(addLowPassFilter(2, Style.COLOR_A()));
        hbox.addLast(addHighPassFilter(2, Style.COLOR_A()));
        vbox.add(hbox);

        vbox.add(addEnvelope(2, 1, Style.COLOR_A()));
        vbox.add(addEnvelope(2, 2, Style.COLOR_B()));
        vbox.add(addEnvelope(2, 3, Style.COLOR_C()));
        vbox.add(addEnvelope(2, 4, Style.COLOR_C()));

        soundPanel.add(vbox, BorderLayout.CENTER);
        soundPanel.makePasteable("layer");
        soundPanel.setSendsAllParameters(false);
        addTab("Filter Amp Env B", soundPanel);
                

        /// LFO Delay PANEL
                
        soundPanel = new SynthPanel(this);
        
        hbox = new HBox();
        
        vbox = new VBox();
        vbox.add(addLFO(2, 1, Style.COLOR_A()));
        vbox.add(addLFO(2, 2, Style.COLOR_B()));
        vbox.add(addLFO(2, 3, Style.COLOR_A()));
        vbox.add(addLFO(2, 4, Style.COLOR_B()));
        hbox.add(vbox);
                
        vbox = new VBox();
        vbox.add(addDelay(2, 1, Style.COLOR_B()));
        vbox.add(addDelay(2, 2, Style.COLOR_A()));
        vbox.add(addDelay(2, 3, Style.COLOR_B()));
        vbox.add(addDelay(2, 4, Style.COLOR_A()));
        vbox.add(addDelays(2, Style.COLOR_B()));
        hbox.addLast(vbox);
                
        soundPanel.add(hbox, BorderLayout.CENTER);
        soundPanel.makePasteable("layer");
        soundPanel.setSendsAllParameters(false);
        addTab("LFO Delay B", soundPanel);
                

        // ARP PANEL
                
        arpPanel = new SynthPanel(this);
        
        vbox = new VBox();
        vbox.add(addArpeggiator(2, Style.COLOR_B()));
        vbox.add(addArpeggiatorNotes(2, Style.COLOR_B()));
        
        arpPanel.add(vbox, BorderLayout.CENTER);
        arpPanel.makePasteable("layer");
        addTab("Arp B", arpPanel);

        // MODULATION PANEL
                
        modulationPanel = new SynthPanel(this);
        
        vbox = new VBox();
        vbox.add(addModulation(2, Style.COLOR_B()));
        
        modulationPanel.add(vbox, BorderLayout.CENTER);
        modulationPanel.makePasteable("layer");
        addTab("Mod B", modulationPanel);


        

        model.set("name", "Untitled");
        model.set("bank", 0);
        model.set("number", 0);
  
        loadDefaults();        
        }
         
    public String getDefaultResourceFileName() { return "DSIProphet12.init"; }
    public String getHTMLResourceFileName() { return "DSIProphet12.html"; }

    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        String[] banks = (writing ? WRITEABLE_BANKS : BANKS);
        if (writeToF) banks = BANKS;            // we're permitting everything
                
        JComboBox bank = new JComboBox(banks);
        bank.setEditable(false);
        bank.setMaximumRowCount(32);
        bank.setSelectedIndex(model.get("bank"));

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
                showSimpleError(title, "The Patch Number must be an integer 1...99");
                continue;
                }
            if (n < 1 || n > 99)
                {
                showSimpleError(title, "The Patch Number must be an integer 1...99");
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
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        HBox inner = new HBox();
        comp = new PatchDisplay(this, 4);
        inner.add(comp);
        inner.add(Strut.makeHorizontalStrut(40));

        VBox inner2 = new VBox();
        params = A_B_MODES;
        comp = new Chooser("A/B Mode", this, "abmode", params);
        inner2.add(comp);
        inner.addLast(inner2);
        vbox.add(inner);

        
        comp = new StringComponent("Patch Name", this, "name", 20, "Name must be up to 20 characters.")
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

        comp = new LabelledDial("Split Point", this, "splitpoint", color, 0, 127)
            {
            public String map(int val)
                {
                return (NOTES[val % 12] + " " + ((val / 12) - 2));
                }
            };
        hbox.add(comp);

        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }
        

    /** Add the global patch category (name, id, number, etc.) */
    public JComponent addLayerBName(Color color)
        {
        Category globalCategory = new Category(this, "Layer B", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
                                
        VBox vbox = new VBox(VBox.TOP_CONSUMES);
        vbox.addLast(Stretch.makeVerticalStretch());
        comp = new StringComponent("Layer B Name", this, "nameb", 20, "Name must be up to 20 characters.")
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

        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }

    public JComponent addArpeggiator(int layer, Color color)
        {
        Category category = new Category(this, "Arpeggiator", color);
        category.makePasteable("layer");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();

        params = ARPEGGIATOR_MODES;
        comp = new Chooser("Mode", this, "layer" + layer + "arpeggiatormode", params);
        vbox.add(comp);

        comp = new CheckBox("Enabled", this, "layer" + layer + "arpeggiator");
        vbox.add(comp);

        hbox.add(vbox);
        vbox = new VBox();

        params = ARPEGGIATOR_RANGES;
        comp = new Chooser("Range", this, "layer" + layer + "arpeggiatorrange", params);
        vbox.add(comp);

        comp = new CheckBox("Latch", this, "layer" + layer + "arpeggiatorautolatch");
        vbox.add(comp);

        hbox.add(vbox);
        vbox = new VBox();

        params = ARPEGGIATOR_CLOCK_DIVISIONS;
        comp = new Chooser("Clock Divide", this, "layer" + layer + "arpeggiatorclockdivide", params);
        vbox.add(comp);

        comp = new CheckBox("Lock Notes", this, "layer" + layer + "arpeggiatorlock");
        vbox.add(comp);

        hbox.add(vbox);

        comp = new LabelledDial("Repeats", this, "layer" + layer + "arpeggiatorrepeats", color, 0, 4);
        hbox.add(comp);

/*
  comp = new LabelledDial("Num Notes", this, "layer" + layer + "arpeggiatornumnotes", color, 0, 32);
  hbox.add(comp);
*/
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public static final int EDISYN_REST = -1;
    public static final int PROPHET_REST = 248;
    /*
      public static final int EDISYN_OFF = 129;
      public static final int PROPHET_OFF = 255;
    */
    public JComponent addArpeggiatorNotes(int layer, Color color)
        {
        Category category = new Category(this, "Notes", color);
        category.makePasteable("layer");
        category.makeDistributable("layer");

        JComponent comp;
        String[] params;
        VBox vbox = new VBox();

        final LabelledDial[] notes = new LabelledDial[32];
        final LabelledDial[] velocities = new LabelledDial[32];

        for(int i = 1; i < 32; i+= 16)
            {
            HBox hbox = new HBox();
            for(int j = i; j < i + 16; j++)
                {
                comp = new LabelledDial("Note " + j, this, "layer" + layer + "arpeggiatornote" + j, color, EDISYN_REST, 127)
                    {
                    public String map(int value)
                        {
                        if (value == EDISYN_REST)
                            {
                            return "--";
                            }
/*                        else if (value == EDISYN_OFF)
                          {
                          return "Off";
                          }
*/
                        else
                            {
                            return NOTES[value % 12] + (value / 12);
                            }
                        }
                    };
                hbox.add(comp);
                notes[j-1] = (LabelledDial)comp;
                }
            vbox.add(hbox);
            hbox = new HBox();
                        
            for(int j = i; j < i + 16; j++)
                {
                comp = new LabelledDial("Vel " + j, this, "layer" + layer + "arpeggiatorvelocity" + j, color, 0, 127)
                    {
                    public String map(int value)
                        {
                        return "" + value;
                        }
                    };
                hbox.add(comp);
                velocities[j-1] = (LabelledDial)comp;
                }
            vbox.add(hbox);
            hbox = new HBox();
            }

        vbox.add(Strut.makeVerticalStrut(12));


        String[] xs = new String[32];
        String[] ys = new String[32];
        double[] x = new double[32];
        double[] y = new double[32];
                
        for(int i = 0; i < 32; i++)
            {
            xs[i] = null;
            ys[i] = "layer" + layer + "arpeggiatorvelocity" + (i + 1);
            x[i] = (i == 0 ? 0 : 1.0 / 32);
            y[i] = 1.0 / 128;
            }

        final EnvelopeDisplay vel = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), xs, ys, x, y)
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
                                        
                int i = (int)(x * 32) + 1;
                if (i >= 32) i = 32;

                double val = (int)(y * 128);
                                
                int proposedState = (int) Math.round(val);

                model.set("layer" + layer + "arpeggiatorvelocity" + i, proposedState);
                }

            public void updateHighlightIndex(int index)
                {
                if (lastIndex >= 0)
                    {
                    velocities[lastIndex].setTextColor(Style.TEXT_COLOR());
                    lastIndex = -1;
                    }
                                                                        
                if (index >= 0)
                    {
                    velocities[index].setTextColor(Style.DYNAMIC_COLOR());
                    lastIndex = index;
                    }
                }
                                                                                                                        
            public int highlightIndex(double x, double y, boolean continuation)
                {
                if (x < 0) x = 0;
                if (x > 1.0) x = 1.0;
                int i = (int)(x * 32);
                if (i >= 31) i = 31;
                return i;
                }
                
            public Color getMarkerColor(int marker, Color defaultColor)
                {
                int val = model.get("layer" + layer + "arpeggiatornote" + (marker + 1));
                /*
                  if (val == EDISYN_OFF) 
                  return Style.ENVELOPE_UNSET_COLOR();
                  else 
                */
                if (val == EDISYN_REST) // 
                    return Style.ENVELOPE_UNSET_COLOR();
                else return defaultColor;
                }
            };

        vel.setStepping(true);
        // vel.setAxis(0.5);
        // vel.setPreferredHeight(disp.getPreferredHeight() * 2);
        
        
        
                        
        xs = new String[32];
        ys = new String[32];
        x = new double[32];
        y = new double[32];
                
        for(int i = 0; i < 32; i++)
            {
            xs[i] = null;
            ys[i] = "layer" + layer + "arpeggiatornote" + (i + 1);
            x[i] = (i == 0 ? 0 : 1.0 / 32);
            y[i] = 1.0 / 128;
            }

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
                                        
                int i = (int)(x * 32) + 1;
                if (i >= 32) i = 32;

                double val = (int)(y * 128);
                                
                int proposedState = (int) Math.round(val);
                                
                model.set("layer" + layer + "arpeggiatornote" + i, proposedState);
                }

            public void updateHighlightIndex(int index)
                {
                if (lastIndex >= 0)
                    {
                    notes[lastIndex].setTextColor(Style.TEXT_COLOR());
                    lastIndex = -1;
                    }
                                                                        
                if (index >= 0)
                    {
                    notes[index].setTextColor(Style.DYNAMIC_COLOR());
                    lastIndex = index;
                    }
                }
                                                                                                                        
            public int highlightIndex(double x, double y, boolean continuation)
                {
                if (x < 0) x = 0;
                if (x > 1.0) x = 1.0;
                int i = (int)(x * 32);
                if (i >= 31) i = 31;
                return i;
                }

            public Color getMarkerColor(int marker, Color defaultColor)
                {
                vel.repaint();
                
                int val = model.get("layer" + layer + "arpeggiatornote" + (marker + 1));
                /*
                  if (val == EDISYN_OFF) 
                  return Style.ENVELOPE_UNSET_COLOR();
                  else 
                */
                if (val == EDISYN_REST) // 
                    return Style.ENVELOPE_UNSET_COLOR();
                else return defaultColor;
                }
            };

        EnvelopeDisplay disp = (EnvelopeDisplay)comp;
        disp.setStepping(true);
        // disp.setAxis(0.5);
        // disp.setPreferredHeight(disp.getPreferredHeight() * 2);
        vbox.add(comp);
        
        vbox.add(Strut.makeVerticalStrut(16));

        vbox.add(vel);
    
        category.add(vbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addGeneral(int layer, Color color)
        {
        Category category = new Category(this, "General", color);
        category.makePasteable("layer");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();

        params = UNISON_KEY_ASSIGNMENTS;
        comp = new Chooser("Unison Key Assign", this, "layer" + layer + "unisonkeyassign", params);
        vbox.add(comp);

        params = UNISON_MODES;
        comp = new Chooser("Unison Mode", this, "layer" + layer + "unisonmode", params);
        vbox.add(comp);
        hbox.add(vbox);
        vbox = new VBox();

        params = GLIDE_MODES;
        comp = new Chooser("Glide Mode", this, "layer" + layer + "glidemode", params);
        vbox.add(comp);

        comp = new CheckBox("Glide", this, "layer" + layer + "glide");
        vbox.add(comp);
        hbox.add(vbox);
        vbox = new VBox();
 
        params = FM_MODES;
        comp = new Chooser("FM Mode", this, "layer" + layer + "oscallfmmode", params);
        vbox.add(comp);

        comp = new CheckBox("Unison", this, "layer" + layer + "unison");
        vbox.add(comp);

      	hbox.add(vbox);
        vbox = new VBox();
   
        comp = new CheckBox("Left Latch", this, "layer" + layer + "slider1mode");
        vbox.add(comp);

        comp = new CheckBox("Right Latch", this, "layer" + layer + "slider2mode");
        vbox.add(comp);

        hbox.add(vbox);

        comp = new LabelledDial("Pitch Bend", this, "layer" + layer + "pitchbendrangeup", color, 0, 12);
        ((LabelledDial)comp).addAdditionalLabel("Range Up");
        hbox.add(comp);
                        
        comp = new LabelledDial("Pitch Bend", this, "layer" + layer + "pitchbendrangedown", color, 0, 24);
        ((LabelledDial)comp).addAdditionalLabel("Range Down");
        hbox.add(comp);

        comp = new LabelledDial("Unison", this, "layer" + layer + "unisondetune", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Tempo");
        hbox.add(comp);

		comp = new LabelledDial("BPM", this, "layer" + layer + "bpm", color, 30, 250);
		hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
            
                
    /** Add an Oscillator category */
    public JComponent addOscillator(int layer, int osc, Color color)
        {
        Category category = new Category(this, "Oscillator " + osc, color);
        category.makePasteable("layer" + layer + "osc");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        params = OSC_SHAPES;
        comp = new Chooser("Shape", this, "layer" + layer + "osc" + osc + "shape", params);
        vbox.add(comp);

        hbox.add(vbox);
        vbox = new VBox();


        params = OSC_WAVES;
        comp = new Chooser("Left Wave", this, "layer" + layer + "osc" + osc + "waveleft", params);
        vbox.add(comp);

        params = OSC_WAVES;
        comp = new Chooser("Right Wave", this, "layer" + layer + "osc" + osc + "waveright", params);
        vbox.add(comp);
        hbox.add(vbox);
        vbox = new VBox();


        comp = new CheckBox("Key Follow", this, "layer" + layer + "osc" + osc + "keyfollow");
        vbox.add(comp);

        comp = new CheckBox("Wave Reset", this, "layer" + layer + "osc" + osc + "wavereset");
        vbox.add(comp);

        comp = new CheckBox("Sync", this, "layer" + layer + "osc" + osc + "sync");
        vbox.add(comp);
        hbox.add(vbox);

      
        comp = new LabelledDial("Pitch", this, "layer" + layer + "osc" + osc + "pitch", color, 0, 120)
        	{
        	public String map(int value)
        		{
        		return NOTES[value % 12] + (value / 12);
        		}
        	};
        hbox.add(comp);

        comp = new LabelledDial("Fine Tune", this, "layer" + layer + "osc" + osc + "finetune", color, 0, 100, 50)
            {
            public boolean isSymmetric() { return true; }
            };
        hbox.add(comp);

        comp = new LabelledDial("Level", this, "layer" + layer + "osc" + osc + "level", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Shape Mod", this, "layer" + layer + "osc" + osc + "shapemod", color, 0, 127, 64);
        hbox.add(comp);

        comp = new LabelledDial("FM", this, "layer" + layer + "osc" + osc + "fm", color, 0, 255);
        hbox.add(comp);

        comp = new LabelledDial("AM", this, "layer" + layer + "osc" + osc + "am", color, 0, 255);
        hbox.add(comp);

        comp = new LabelledDial("Slop", this, "layer" + layer + "osc" + osc + "slop", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Glide", this, "layer" + layer + "osc" + osc + "glideamount", color, 0, 127);
        hbox.add(comp);
                
        if (osc == 1)
            {
            comp = new LabelledDial("Sub Octave", this, "layer" + layer + "osc1suboscillator", color, 0, 127);
            hbox.add(comp);
            }
                
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addCharacter(int layer, Color color)
        {
        Category category = new Category(this, "Character", color);
        category.makePasteable("layer");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
               
        comp = new LabelledDial("Air", this, "layer" + layer + "air", color, 0, 127);
        hbox.add(comp);
                        
        comp = new LabelledDial("Girth", this, "layer" + layer + "girth", color, 0, 127);
        hbox.add(comp);
                        
        comp = new LabelledDial("Hack", this, "layer" + layer + "hack", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Decimate", this, "layer" + layer + "decimate", color, 0, 127);
        hbox.add(comp);
                        
        comp = new LabelledDial("Drive", this, "layer" + layer + "drive", color, 0, 127);
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }



    public JComponent addAmplifier(int layer, Color color)
        {
        Category category = new Category(this, "Amplifier", color);
        category.makePasteable("layer");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
               
        /*
          comp = new CheckBox("Distortion Noise Gate", this, "layer" + layer + "distortionnoisegate");
          vbox.add(comp);
          hbox.add(vbox);
        */

        comp = new LabelledDial("Feedback", this, "layer" + layer + "feedbackamount", color, 0, 254, 127)
            {
            public boolean isSymmetric() { return true; }
            };             
        hbox.add(comp);
        ((LabelledDial)comp).addAdditionalLabel("Amount");

        comp = new LabelledDial("Feedback", this, "layer" + layer + "feedbacktuning", color, 0, 48);
        ((LabelledDial)comp).addAdditionalLabel("Tuning");
        hbox.add(comp);

        comp = new LabelledDial("Voice", this, "layer" + layer + "voicevolume", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Volume");
        hbox.add(comp);

        comp = new LabelledDial("Pan", this, "layer" + layer + "panspread", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Spread");
        hbox.add(comp);

        comp = new LabelledDial("Distortion", this, "layer" + layer + "distortionamount", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addLowPassFilter(int layer, Color color)
        {
        Category category = new Category(this, "Low Pass Filter", color);
        category.makePasteable("layer");

        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        comp = new CheckBox("4-Pole", this, "layer" + layer + "lpf24pole");
        vbox.add(comp);
        hbox.add(vbox);
       
        comp = new LabelledDial("Frequency", this, "layer" + layer + "lpffrequency", color, 0, 164);
        hbox.add(comp);
                
        comp = new LabelledDial("Resonance", this, "layer" + layer + "lpfresonance", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Key Amount", this, "layer" + layer + "lpfkeyamount", color, 0, 127);
        hbox.add(comp);
                                
        category.add(hbox, BorderLayout.WEST);
        return category;
        }

    public JComponent addHighPassFilter(int layer, Color color)
        {
        Category category = new Category(this, "High Pass Filter", color);
        category.makePasteable("layer");

        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();
        VBox vbox = new VBox();

        comp = new LabelledDial("Frequency", this, "layer" + layer + "hpffrequency", color, 0, 164);
        hbox.add(comp);
                
        comp = new LabelledDial("Resonance", this, "layer" + layer + "hpfresonance", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Key Amount", this, "layer" + layer + "hpfkeyamount", color, 0, 127);
        hbox.add(comp);
                
        category.add(hbox, BorderLayout.WEST);
        return category;
        }

    public JComponent addEnvelope(int layer, final int env, Color color)
        {
        Category category = new Category(this, 
                (env == 1 ? "Amplifier Envelope" : 
                (env == 2 ? "Filter Envelope" : "Envelope " + env)), color);
        category.makePasteable("layer" + layer + "env");
                
        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        if (env > 2)
            {
            params = MOD_DESTINATIONS;
            comp = new Chooser("Destination", this, "layer" + layer + "env" + env + "destination", params);
            vbox.add(comp);
            }
        
        comp = new CheckBox("Repeat", this, "layer" + layer + "env" + env + "repeat");
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Amount", this, "layer" + layer + "env" + env + "amount", color, 0, 254, 127)
            {
            public boolean isSymmetric() { return true; }
            };              
        hbox.add(comp);
                
        comp = new LabelledDial("Velocity", this, "layer" + layer + "env" + env + "velocitytoamount", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("to Amount");
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



    /** Add an LFO category */
    public JComponent addLFO(int layer, final int lfo, Color color)
        {
        Category category = new Category(this, "LFO " + lfo, color);
        category.makePasteable("layer" + layer + "lfo");
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();

        CheckBox sync = new CheckBox("Sync", this, "layer" + layer + "lfo" + lfo + "sync");

        params = LFO_SHAPES;
        comp = new Chooser("Shape", this, "layer" + layer + "lfo" + lfo + "shape", params);
        vbox.add(comp);

        params = MOD_DESTINATIONS;
        comp = new Chooser("Destination", this, "layer" + layer + "lfo" + lfo + "destination", params);
        vbox.add(comp);
        vbox.add(Strut.makeStrut(sync));                // so it's the same height as Delay
        hbox.add(vbox);
        vbox = new VBox();

        params = LFO_SYNCS;
        comp = new Chooser("Sync Setting", this, "layer" + layer + "lfo" + lfo + "syncsetting", params);
        vbox.add(comp);
        vbox.add(sync);


        comp = new CheckBox("Wave Reset", this, "layer" + layer + "lfo" + lfo + "wavereset");
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Frequency", this, "layer" + layer + "lfo" + lfo + "frequency", color, 0, 255);
        hbox.add(comp);
        
        comp = new LabelledDial("Amount", this, "layer" + layer + "lfo" + lfo + "amount", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Slew Rate", this, "layer" + layer + "lfo" + lfo + "slewrate", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Phase", this, "layer" + layer + "lfo" + lfo + "phase", color, 0, 127);
        hbox.add(comp);
                                
        category.add(hbox, BorderLayout.WEST);
        return category;
        }


    public JComponent addDelays(int layer, Color color)
        {
        Category category = new Category(this, "Delays", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();

        params = DELAY_FILTER_MODES;
        comp = new Chooser("Delay Feedback Mode", this, "layer" + layer + "delayfeedbackmode", params);
        vbox.add(comp);
		hbox.add(vbox);

        category.add(hbox, BorderLayout.WEST);
        return category;
        }


    public JComponent addDelay(int layer, final int delay, Color color)
        {
        Category category = new Category(this, "Delay " + delay, color);
        category.makePasteable("layer" + layer + "delay");
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();

        params = DELAY_FILTER_MODES;
        comp = new Chooser("Filter Mode", this, "layer" + layer + "delay" + delay + "filtermode", params);
        vbox.add(comp);

        params = DELAY_SYNCS;
        comp = new Chooser("Sync Setting", this, "layer" + layer + "delay" + delay + "syncsetting", params);
        vbox.add(comp);

        comp = new CheckBox("Sync", this, "layer" + layer + "delay" + delay + "sync");
        vbox.add(comp);

        hbox.add(vbox);

        comp = new LabelledDial("Time", this, "layer" + layer + "delay" + delay + "time", color, 0, 255);
        hbox.add(comp);

        comp = new LabelledDial("Amount", this, "layer" + layer + "delay" + delay + "amount", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Feedback", this, "layer" + layer + "delay" + delay + "feedback", color, 0, 127);
        hbox.add(comp);
        
        comp = new LabelledDial("Pan", this, "layer" + layer + "delay" + delay + "pan", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Low Pass", this, "layer" + layer + "delay" + delay + "lpf", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Filter");
        hbox.add(comp);
        
        comp = new LabelledDial("High Pass", this, "layer" + layer + "delay" + delay + "hpf", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Filter");
        hbox.add(comp);
        

        category.add(hbox, BorderLayout.WEST);
        return category;
        }




    /** Add the Modulation category */
    public JComponent addModulation(int layer, Color color)
        {
        Category category  = new Category(this, "Modulation Matrix", color);
        category.makePasteable("layer" + layer + "mod");
        category.makeDistributable("layer" + layer + "mod");

        JComponent comp;
        String[] params;
        VBox outer = new VBox();
        for(int i = 1; i <= 16; i+= 4)
            {
            if (i != 1)
                outer.add(Strut.makeVerticalStrut(20));

            HBox hbox = new HBox();
            for(int mod = i; mod < i + 4; mod++)
                {
                if (mod % 4 != 1)
                    hbox.add(Strut.makeHorizontalStrut(8));
                                        
                VBox vbox = new VBox();

                params = MOD_SOURCES;
                comp = new Chooser("Source", this, "layer" + layer + "mod" + mod + "source", params);
                vbox.add(comp);

                params = MOD_DESTINATIONS;
                comp = new Chooser("Destination", this, "layer" + layer + "mod" + mod + "destination", params);
                vbox.add(comp);
                hbox.add(vbox);

                comp = new LabelledDial("Amount", this, "layer" + layer + "mod" + mod + "amount", color, 0, 254, 127)
                    {
                    public boolean isSymmetric() { return true; }
                    };              
                hbox.add(comp);
                }                                       
            outer.add(hbox);
            }
                                    
        category.add(outer, BorderLayout.WEST);
        return category;
        }




    /** Map of parameter -> index in the allParameters array. */
    static HashMap parametersToIndex = null;


    /** List of all DSI Prophet 12 parameters in order. */
                
    public final static String[] parameters = new String[]
    {
    "layer1osc1pitch",
    "layer1osc1finetune",
    "layer1osc1level",
    "layer1osc1shape",
    "layer1osc1shapemod",
    "layer1osc1waveleft",
    "layer1osc1waveright",
    "layer1osc1fm",
    "layer1osc1am",
    "layer1osc1slop",
    "layer1osc1glideamount",
    "layer1osc1sync",
    "layer1osc1keyfollow",
    "layer1osc1wavereset",
    "--",
    "--",
    "--",
    "--",
    "layer1osc2pitch",
    "layer1osc2finetune",
    "layer1osc2level",
    "layer1osc2shape",
    "layer1osc2shapemod",
    "layer1osc2waveleft",
    "layer1osc2waveright",
    "layer1osc2fm",
    "layer1osc2am",
    "layer1osc2slop",
    "layer1osc2glideamount",
    "layer1osc2sync",
    "layer1osc2keyfollow",
    "layer1osc2wavereset",
    "--",
    "--",
    "--",
    "--",
    "layer1osc3pitch",
    "layer1osc3finetune",
    "layer1osc3level",
    "layer1osc3shape",
    "layer1osc3shapemod",
    "layer1osc3waveleft",
    "layer1osc3waveright",
    "layer1osc3fm",
    "layer1osc3am",
    "layer1osc3slop",
    "layer1osc3glideamount",
    "layer1osc3sync",
    "layer1osc3keyfollow",
    "layer1osc3wavereset",
    "--",
    "--",
    "--",
    "--",
    "layer1osc4pitch",
    "layer1osc4finetune",
    "layer1osc4level",
    "layer1osc4shape",
    "layer1osc4shapemod",
    "layer1osc4waveleft",
    "layer1osc4waveright",
    "layer1osc4fm",
    "layer1osc4am",
    "layer1osc4slop",
    "layer1osc4glideamount",
    "layer1osc4sync",
    "layer1osc4keyfollow",
    "layer1osc4wavereset",
    "--",
    "--",
    "--",
    "--",
    "layer1osc1suboscillator",
    "layer1glidemode",
    "layer1glide",
    "layer1pitchbendrangeup",
    "layer1pitchbendrangedown",
    "layer1oscallfmmode",
    "--",
    "--",
    "layer1air",
    "layer1girth",
    "layer1hack",
    "layer1decimate",
    "layer1drive",
    "--",
    "--",
    "--",
    "--",
    "--",
    "layer1lpffrequency",
    "layer1lpfresonance",
    "layer1lpfkeyamount",
    "layer1lpf24pole",
    "layer1hpffrequency",
    "layer1hpfresonance",
    "layer1hpfkeyamount",
    "layer1feedbackamount",
    "layer1feedbacktuning",
    "layer1voicevolume",
    "layer1panspread",
    "layer1distortionamount",
    "--",                                               // layer1distortionnoisegate
    "layer1env1amount",
    "layer1env1velocitytoamount",
    "layer1env1delay",
    "layer1env1attack",
    "layer1env1decay",
    "layer1env1sustain",
    "layer1env1release",
    "layer1env1repeat",
    "--",
    "--",
    "--",
    "layer1env2amount",
    "layer1env2velocitytoamount",
    "layer1env2delay",
    "layer1env2attack",
    "layer1env2decay",
    "layer1env2sustain",
    "layer1env2release",
    "layer1env2repeat",
    "--",
    "--",
    "--",
    "layer1env3amount",
    "layer1env3velocitytoamount",
    "layer1env3delay",
    "layer1env3attack",
    "layer1env3decay",
    "layer1env3sustain",
    "layer1env3release",
    "layer1env3repeat",
    "layer1env3destination",
    "--",
    "--",
    "layer1env4amount",
    "layer1env4velocitytoamount",
    "layer1env4delay",
    "layer1env4attack",
    "layer1env4decay",
    "layer1env4sustain",
    "layer1env4release",
    "layer1env4repeat",
    "layer1env4destination",
    "--",
    "--",
    "layer1lfo1frequency",
    "layer1lfo1syncsetting",
    "layer1lfo1sync",
    "layer1lfo1shape",
    "layer1lfo1amount",
    "layer1lfo1slewrate",
    "layer1lfo1phase",
    "layer1lfo1wavereset",
    "layer1lfo1destination",
    "--",
    "layer1lfo2frequency",
    "layer1lfo2syncsetting",
    "layer1lfo2sync",
    "layer1lfo2shape",
    "layer1lfo2amount",
    "layer1lfo2slewrate",
    "layer1lfo2phase",
    "layer1lfo2wavereset",
    "layer1lfo2destination",
    "--",
    "layer1lfo3frequency",
    "layer1lfo3syncsetting",
    "layer1lfo3sync",
    "layer1lfo3shape",
    "layer1lfo3amount",
    "layer1lfo3slewrate",
    "layer1lfo3phase",
    "layer1lfo3wavereset",
    "layer1lfo3destination",
    "--",
    "layer1lfo4frequency",
    "layer1lfo4syncsetting",
    "layer1lfo4sync",
    "layer1lfo4shape",
    "layer1lfo4amount",
    "layer1lfo4slewrate",
    "layer1lfo4phase",
    "layer1lfo4wavereset",
    "layer1lfo4destination",
    "--",
    "layer1delay1time",
    "layer1delay1syncsetting",
    "layer1delay1sync",
    "layer1delay1amount",
    "layer1delay1feedback",
    "layer1delay1lpf",
    "layer1delay1hpf",
    "layer1delay1filtermode",
    "layer1delay2time",
    "layer1delay2syncsetting",
    "layer1delay2sync",
    "layer1delay2amount",
    "layer1delay2feedback",
    "layer1delay2lpf",
    "layer1delay2hpf",
    "layer1delay2filtermode",
    "layer1delay3time",
    "layer1delay3syncsetting",
    "layer1delay3sync",
    "layer1delay3amount",
    "layer1delay3feedback",
    "layer1delay3lpf",
    "layer1delay3hpf",
    "layer1delay3filtermode",
    "layer1delay4time",
    "layer1delay4syncsetting",
    "layer1delay4sync",
    "layer1delay4amount",
    "layer1delay4feedback",
    "layer1delay4lpf",
    "layer1delay4hpf",
    "layer1delay4filtermode",
    "layer1mod1source",
    "layer1mod1amount",
    "layer1mod1destination",
    "--",
    "layer1mod2source",
    "layer1mod2amount",
    "layer1mod2destination",
    "--",
    "layer1mod3source",
    "layer1mod3amount",
    "layer1mod3destination",
    "--",
    "layer1mod4source",
    "layer1mod4amount",
    "layer1mod4destination",
    "--",
    "layer1mod5source",
    "layer1mod5amount",
    "layer1mod5destination",
    "--",
    "layer1mod6source",
    "layer1mod6amount",
    "layer1mod6destination",
    "--",
    "layer1mod7source",
    "layer1mod7amount",
    "layer1mod7destination",
    "--",
    "layer1mod8source",
    "layer1mod8amount",
    "layer1mod8destination",
    "--",
    "layer1mod9source",
    "layer1mod9amount",
    "layer1mod9destination",
    "--",
    "layer1mod10source",
    "layer1mod10amount",
    "layer1mod10destination",
    "--",
    "layer1mod11source",
    "layer1mod11amount",
    "layer1mod11destination",
    "--",
    "layer1mod12source",
    "layer1mod12amount",
    "layer1mod12destination",
    "--",
    "layer1mod13source",
    "layer1mod13amount",
    "layer1mod13destination",
    "--",
    "layer1mod14source",
    "layer1mod14amount",
    "layer1mod14destination",
    "--",
    "layer1mod15source",
    "layer1mod15amount",
    "layer1mod15destination",
    "--",
    "layer1mod16source",
    "layer1mod16amount",
    "layer1mod16destination",
    "--",
    "layer1unison",
    "layer1unisondetune",
    "layer1unisonmode",
    "layer1unisonkeyassign",
    "splitpoint",
    "abmode",
    "layer1arpeggiator",
    "layer1arpeggiatormode",
    "layer1arpeggiatorrange",
    "layer1arpeggiatorclockdivide",
    "layer1arpeggiatorrepeats",
    "layer1arpeggiatorautolatch",
    "layer1arpeggiatorlock",
    "layer1bpm",                                                          // Manual says 288, looks like an error?
    "layer1delayfeedbackmode",
    "--",
    "--",
    "--",
    "layer1arpeggiatornote1",
    "layer1arpeggiatornote2",
    "layer1arpeggiatornote3",
    "layer1arpeggiatornote4",
    "layer1arpeggiatornote5",
    "layer1arpeggiatornote6",
    "layer1arpeggiatornote7",
    "layer1arpeggiatornote8",
    "layer1arpeggiatornote9",
    "layer1arpeggiatornote10",
    "layer1arpeggiatornote11",
    "layer1arpeggiatornote12",
    "layer1arpeggiatornote13",
    "layer1arpeggiatornote14",
    "layer1arpeggiatornote15",
    "layer1arpeggiatornote16",
    "layer1arpeggiatornote17",
    "layer1arpeggiatornote18",
    "layer1arpeggiatornote19",
    "layer1arpeggiatornote20",
    "layer1arpeggiatornote21",
    "layer1arpeggiatornote22",
    "layer1arpeggiatornote23",
    "layer1arpeggiatornote24",
    "layer1arpeggiatornote25",
    "layer1arpeggiatornote26",
    "layer1arpeggiatornote27",
    "layer1arpeggiatornote28",
    "layer1arpeggiatornote29",
    "layer1arpeggiatornote30",
    "layer1arpeggiatornote31",
    "layer1arpeggiatornote32",
    "layer1arpeggiatorvelocity1",
    "layer1arpeggiatorvelocity2",
    "layer1arpeggiatorvelocity3",
    "layer1arpeggiatorvelocity4",
    "layer1arpeggiatorvelocity5",
    "layer1arpeggiatorvelocity6",
    "layer1arpeggiatorvelocity7",
    "layer1arpeggiatorvelocity8",
    "layer1arpeggiatorvelocity9",
    "layer1arpeggiatorvelocity10",
    "layer1arpeggiatorvelocity11",
    "layer1arpeggiatorvelocity12",
    "layer1arpeggiatorvelocity13",
    "layer1arpeggiatorvelocity14",
    "layer1arpeggiatorvelocity15",
    "layer1arpeggiatorvelocity16",
    "layer1arpeggiatorvelocity17",
    "layer1arpeggiatorvelocity18",
    "layer1arpeggiatorvelocity19",
    "layer1arpeggiatorvelocity20",
    "layer1arpeggiatorvelocity21",
    "layer1arpeggiatorvelocity22",
    "layer1arpeggiatorvelocity23",
    "layer1arpeggiatorvelocity24",
    "layer1arpeggiatorvelocity25",
    "layer1arpeggiatorvelocity26",
    "layer1arpeggiatorvelocity27",
    "layer1arpeggiatorvelocity28",
    "layer1arpeggiatorvelocity29",
    "layer1arpeggiatorvelocity30",
    "layer1arpeggiatorvelocity31",
    "layer1arpeggiatorvelocity32",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--", 
    "--", 
    "--",
    "--", 
    "--", 
    "--", 
    "--",
    "--", 
    "--", 
    "--", 
    "--",
    "--", 
    "--", 
    "--", 
    "--",
    "--", 
    "--", 
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--", 
    "--", 
    "--",
    "--", 
    "--", 
    "--", 
    "--",                           // Suspect this shouldn't be here
    "--", 
    "--",
    "layer1delay1pan",
    "layer1delay2pan",
    "layer1delay3pan",
    "layer1delay4pan",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--", 
    "--",           // M_NAME_0 
    "--",           // M_NAME_1
    "--",           // M_NAME_2
    "--",           // M_NAME_3 
    "--",           // M_NAME_4
    "--",           // M_NAME_5
    "--",           // M_NAME_6 
    "--",           // M_NAME_7 
    "--",           // M_NAME_8 
    "--",           // M_NAME_9
    "--",           // M_NAME_10
    "--",           // M_NAME_11 
    "--",           // M_NAME_12
    "--",           // M_NAME_13
    "--",           // M_NAME_14 
    "--",           // M_NAME_15 
    "--",           // M_NAME_16 
    "--",           // M_NAME_17
    "--",           // M_NAME_18
    "--",           // M_NAME_19 
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "layer2osc1pitch",
    "layer2osc1finetune",
    "layer2osc1level",
    "layer2osc1shape",
    "layer2osc1shapemod",
    "layer2osc1waveleft",
    "layer2osc1waveright",
    "layer2osc1fm",
    "layer2osc1am",
    "layer2osc1slop",
    "layer2osc1glideamount",
    "layer2osc1sync",
    "layer2osc1keyfollow",
    "layer2osc1wavereset",
    "--",
    "--",
    "--",
    "--",
    "layer2osc2pitch",
    "layer2osc2finetune",
    "layer2osc2level",
    "layer2osc2shape",
    "layer2osc2shapemod",
    "layer2osc2waveleft",
    "layer2osc2waveright",
    "layer2osc2fm",
    "layer2osc2am",
    "layer2osc2slop",
    "layer2osc2glideamount",
    "layer2osc2sync",
    "layer2osc2keyfollow",
    "layer2osc2wavereset",
    "--",
    "--",
    "--",
    "--",
    "layer2osc3pitch",
    "layer2osc3finetune",
    "layer2osc3level",
    "layer2osc3shape",
    "layer2osc3shapemod",
    "layer2osc3waveleft",
    "layer2osc3waveright",
    "layer2osc3fm",
    "layer2osc3am",
    "layer2osc3slop",
    "layer2osc3glideamount",
    "layer2osc3sync",
    "layer2osc3keyfollow",
    "layer2osc3wavereset",
    "--",
    "--",
    "--",
    "--",
    "layer2osc4pitch",
    "layer2osc4finetune",
    "layer2osc4level",
    "layer2osc4shape",
    "layer2osc4shapemod",
    "layer2osc4waveleft",
    "layer2osc4waveright",
    "layer2osc4fm",
    "layer2osc4am",
    "layer2osc4slop",
    "layer2osc4glideamount",
    "layer2osc4sync",
    "layer2osc4keyfollow",
    "layer2osc4wavereset",
    "--",
    "--",
    "--",
    "--",
    "layer2osc1suboscillator",
    "layer2glidemode",
    "layer2glide",
    "layer2pitchbendrangeup",
    "layer2pitchbendrangedown",
    "layer2oscallfmmode",
    "--",
    "--",
    "layer2air",
    "layer2girth",
    "layer2hack",
    "layer2decimate",
    "layer2drive",
    "--",
    "--",
    "--",
    "--",
    "--",
    "layer2lpffrequency",
    "layer2lpfresonance",
    "layer2lpfkeyamount",
    "layer2lpf24pole",
    "layer2hpffrequency",
    "layer2hpfresonance",
    "layer2hpfkeyamount",
    "layer2feedbackamount",
    "layer2feedbacktuning",
    "layer2voicevolume",
    "layer2panspread",
    "layer2distortionamount",
    "--",                                                       // layer2distortionnoisegate
    "layer2env1amount",
    "layer2env1velocitytoamount",
    "layer2env1delay",
    "layer2env1attack",
    "layer2env1decay",
    "layer2env1sustain",
    "layer2env1release",
    "layer2env1repeat",
    "--",
    "--",
    "--",
    "layer2env2amount",
    "layer2env2velocitytoamount",
    "layer2env2delay",
    "layer2env2attack",
    "layer2env2decay",
    "layer2env2sustain",
    "layer2env2release",
    "layer2env2repeat",
    "--",
    "--",
    "--",
    "layer2env3amount",
    "layer2env3velocitytoamount",
    "layer2env3delay",
    "layer2env3attack",
    "layer2env3decay",
    "layer2env3sustain",
    "layer2env3release",
    "layer2env3repeat",
    "layer2env3destination",
    "--",
    "--",
    "layer2env4amount",
    "layer2env4velocitytoamount",
    "layer2env4delay",
    "layer2env4attack",
    "layer2env4decay",
    "layer2env4sustain",
    "layer2env4release",
    "layer2env4repeat",
    "layer2env4destination",
    "--",
    "--",
    "layer2lfo1frequency",
    "layer2lfo1syncsetting",
    "layer2lfo1sync",
    "layer2lfo1shape",
    "layer2lfo1amount",
    "layer2lfo1slewrate",
    "layer2lfo1phase",
    "layer2lfo1wavereset",
    "layer2lfo1destination",
    "--",
    "layer2lfo2frequency",
    "layer2lfo2syncsetting",
    "layer2lfo2sync",
    "layer2lfo2shape",
    "layer2lfo2amount",
    "layer2lfo2slewrate",
    "layer2lfo2phase",
    "layer2lfo2wavereset",
    "layer2lfo2destination",
    "--",
    "layer2lfo3frequency",
    "layer2lfo3syncsetting",
    "layer2lfo3sync",
    "layer2lfo3shape",
    "layer2lfo3amount",
    "layer2lfo3slewrate",
    "layer2lfo3phase",
    "layer2lfo3wavereset",
    "layer2lfo3destination",
    "--",
    "layer2lfo4frequency",
    "layer2lfo4syncsetting",
    "layer2lfo4sync",
    "layer2lfo4shape",
    "layer2lfo4amount",
    "layer2lfo4slewrate",
    "layer2lfo4phase",
    "layer2lfo4wavereset",
    "layer2lfo4destination",
    "--",
    "layer2delay1time",
    "layer2delay1syncsetting",
    "layer2delay1sync",
    "layer2delay1amount",
    "layer2delay1feedback",
    "layer2delay1lpf",
    "layer2delay1hpf",
    "layer2delay1filtermode",
    "layer2delay2time",
    "layer2delay2syncsetting",
    "layer2delay2sync",
    "layer2delay2amount",
    "layer2delay2feedback",
    "layer2delay2lpf",
    "layer2delay2hpf",
    "layer2delay2filtermode",
    "layer2delay3time",
    "layer2delay3syncsetting",
    "layer2delay3sync",
    "layer2delay3amount",
    "layer2delay3feedback",
    "layer2delay3lpf",
    "layer2delay3hpf",
    "layer2delay3filtermode",
    "layer2delay4time",
    "layer2delay4syncsetting",
    "layer2delay4sync",
    "layer2delay4amount",
    "layer2delay4feedback",
    "layer2delay4lpf",
    "layer2delay4hpf",
    "layer2delay4filtermode",
    "layer2mod1source",
    "layer2mod1amount",
    "layer2mod1destination",
    "--",
    "layer2mod2source",
    "layer2mod2amount",
    "layer2mod2destination",
    "--",
    "layer2mod3source",
    "layer2mod3amount",
    "layer2mod3destination",
    "--",
    "layer2mod4source",
    "layer2mod4amount",
    "layer2mod4destination",
    "--",
    "layer2mod5source",
    "layer2mod5amount",
    "layer2mod5destination",
    "--",
    "layer2mod6source",
    "layer2mod6amount",
    "layer2mod6destination",
    "--",
    "layer2mod7source",
    "layer2mod7amount",
    "layer2mod7destination",
    "--",
    "layer2mod8source",
    "layer2mod8amount",
    "layer2mod8destination",
    "--",
    "layer2mod9source",
    "layer2mod9amount",
    "layer2mod9destination",
    "--",
    "layer2mod10source",
    "layer2mod10amount",
    "layer2mod10destination",
    "--",
    "layer2mod11source",
    "layer2mod11amount",
    "layer2mod11destination",
    "--",
    "layer2mod12source",
    "layer2mod12amount",
    "layer2mod12destination",
    "--",
    "layer2mod13source",
    "layer2mod13amount",
    "layer2mod13destination",
    "--",
    "layer2mod14source",
    "layer2mod14amount",
    "layer2mod14destination",
    "--",
    "layer2mod15source",
    "layer2mod15amount",
    "layer2mod15destination",
    "--",
    "layer2mod16source",
    "layer2mod16amount",
    "layer2mod16destination",
    "--",
    "layer2unison",
    "layer2unisondetune",
    "layer2unisonmode",
    "layer2unisonkeyassign",
    "splitpoint",
    "abmode",
    "layer2arpeggiator",
    "layer2arpeggiatormode",
    "layer2arpeggiatorrange",
    "layer2arpeggiatorclockdivide",
    "layer2arpeggiatorrepeats",
    "layer2arpeggiatorautolatch",
    "layer2arpeggiatorlock",
    "layer2bpm",
    "layer2delayfeedbackmode",
    "--",
    "--",
    "--",
    "layer2arpeggiatornote1",
    "layer2arpeggiatornote2",
    "layer2arpeggiatornote3",
    "layer2arpeggiatornote4",
    "layer2arpeggiatornote5",
    "layer2arpeggiatornote6",
    "layer2arpeggiatornote7",
    "layer2arpeggiatornote8",
    "layer2arpeggiatornote9",
    "layer2arpeggiatornote10",
    "layer2arpeggiatornote11",
    "layer2arpeggiatornote12",
    "layer2arpeggiatornote13",
    "layer2arpeggiatornote14",
    "layer2arpeggiatornote15",
    "layer2arpeggiatornote16",
    "layer2arpeggiatornote17",
    "layer2arpeggiatornote18",
    "layer2arpeggiatornote19",
    "layer2arpeggiatornote20",
    "layer2arpeggiatornote21",
    "layer2arpeggiatornote22",
    "layer2arpeggiatornote23",
    "layer2arpeggiatornote24",
    "layer2arpeggiatornote25",
    "layer2arpeggiatornote26",
    "layer2arpeggiatornote27",
    "layer2arpeggiatornote28",
    "layer2arpeggiatornote29",
    "layer2arpeggiatornote30",
    "layer2arpeggiatornote31",
    "layer2arpeggiatornote32",
    "layer2arpeggiatorvelocity1",
    "layer2arpeggiatorvelocity2",
    "layer2arpeggiatorvelocity3",
    "layer2arpeggiatorvelocity4",
    "layer2arpeggiatorvelocity5",
    "layer2arpeggiatorvelocity6",
    "layer2arpeggiatorvelocity7",
    "layer2arpeggiatorvelocity8",
    "layer2arpeggiatorvelocity9",
    "layer2arpeggiatorvelocity10",
    "layer2arpeggiatorvelocity11",
    "layer2arpeggiatorvelocity12",
    "layer2arpeggiatorvelocity13",
    "layer2arpeggiatorvelocity14",
    "layer2arpeggiatorvelocity15",
    "layer2arpeggiatorvelocity16",
    "layer2arpeggiatorvelocity17",
    "layer2arpeggiatorvelocity18",
    "layer2arpeggiatorvelocity19",
    "layer2arpeggiatorvelocity20",
    "layer2arpeggiatorvelocity21",
    "layer2arpeggiatorvelocity22",
    "layer2arpeggiatorvelocity23",
    "layer2arpeggiatorvelocity24",
    "layer2arpeggiatorvelocity25",
    "layer2arpeggiatorvelocity26",
    "layer2arpeggiatorvelocity27",
    "layer2arpeggiatorvelocity28",
    "layer2arpeggiatorvelocity29",
    "layer2arpeggiatorvelocity30",
    "layer2arpeggiatorvelocity31",
    "layer2arpeggiatorvelocity32",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--", 
    "--", 
    "--",
    "--", 
    "--", 
    "--", 
    "--",
    "--", 
    "--", 
    "--", 
    "--",
    "--", 
    "--", 
    "--", 
    "--",
    "--", 
    "--", 
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--", 
    "--", 
    "--",
    "--", 
    "--", 
    "--", 
    "--", 
    "--", 
    "--", 
    "layer2delay1pan",
    "layer2delay2pan",
    "layer2delay3pan",
    "layer2delay4pan",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--", 
    "--",           // M_NAME_0 in layer 1
    "--",           // M_NAME_1 in layer 1
    "--",           // M_NAME_2 in layer 1
    "--",           // M_NAME_3 in layer 1
    "--",           // M_NAME_4 in layer 1
    "--",           // M_NAME_5 in layer 1
    "--",           // M_NAME_6 in layer 1
    "--",           // M_NAME_7 in layer 1
    "--",           // M_NAME_8 in layer 1
    "--",           // M_NAME_9 in layer 1
    "--",           // M_NAME_10 in layer 1
    "--",           // M_NAME_11 in layer 1
    "--",           // M_NAME_12 in layer 1
    "--",           // M_NAME_13 in layer 1
    "--",           // M_NAME_14 in layer 1
    "--",           // M_NAME_15 in layer 1
    "--",           // M_NAME_16 in layer 1 
    "--",           // M_NAME_17 in layer 1
    "--",           // M_NAME_18 in layer 1
    "--",           // M_NAME_19 in layer 1
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--",
    "--", 
    "--", 
    "--",
    };
    
    
    

    // The order of parameters in a sysex message turns out to be entirely different
    // than the order specified in NRPN in the manual.  Here is the correct order.
    public static final String[] sysexParameters = 
        {
        "layer1osc1pitch",
        "layer1osc2pitch",
        "layer1osc3pitch",
        "layer1osc4pitch",
        "layer1osc1finetune",
        "layer1osc2finetune",
        "layer1osc3finetune",
        "layer1osc4finetune",
        "layer1osc1level",
        "layer1osc2level",
        "layer1osc3level",
        "layer1osc4level",
        "layer1osc1shape",
        "layer1osc2shape",
        "layer1osc3shape",
        "layer1osc4shape",
        "layer1osc1waveleft",
        "layer1osc2waveleft",
        "layer1osc3waveleft",
        "layer1osc4waveleft",
        "--",
        "--",
        "--",
        "--",
        "layer1osc1waveright",
        "layer1osc2waveright",
        "layer1osc3waveright",
        "layer1osc4waveright",
        "layer1osc1shapemod",
        "layer1osc2shapemod",
        "layer1osc3shapemod",
        "layer1osc4shapemod",
        "layer1osc1fm",
        "layer1osc2fm",
        "layer1osc3fm",
        "layer1osc4fm",
        "layer1osc1am",
        "layer1osc2am",
        "layer1osc3am",
        "layer1osc4am",
        "layer1osc1suboscillator",
        "--",
        "--",
        "--",
        "layer1osc1slop",
        "layer1osc2slop",
        "layer1osc3slop",
        "layer1osc4slop",
        "layer1osc1glideamount",
        "layer1osc2glideamount",
        "layer1osc3glideamount",
        "layer1osc4glideamount",
        "layer1osc1sync",
        "layer1osc2sync",
        "layer1osc3sync",
        "layer1osc4sync",
        "layer1osc1keyfollow",
        "layer1osc2keyfollow",
        "layer1osc3keyfollow",
        "layer1osc4keyfollow",
        "layer1osc1wavereset",
        "layer1osc2wavereset",
        "layer1osc3wavereset",
        "layer1osc4wavereset",
        "layer1oscallfmmode",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "layer1glidemode",
        "layer1glide",
        "layer1pitchbendrangeup",
        "layer1pitchbendrangedown",
        "layer1air",
        "layer1girth",
        "layer1hack",
        "layer1decimate",
        "layer1drive",
        "--",
        "--",
        "--",
        "layer1delayfeedbackmode",
        "layer1lpffrequency",
        "layer1lpfresonance",
        "layer1lpfkeyamount",
        "layer1lpf24pole",
        "layer1hpffrequency",
        "layer1hpfresonance",
        "layer1hpfkeyamount",
        "layer1panspread",
        "layer1voicevolume",
        "layer1feedbackamount",
        "layer1feedbacktuning",
        "layer1delay1time",
        "layer1delay2time",
        "layer1delay3time",
        "layer1delay4time",
        "layer1delay1syncsetting",
        "layer1delay2syncsetting",
        "layer1delay3syncsetting",
        "layer1delay4syncsetting",
        "layer1delay1amount",
        "layer1delay2amount",
        "layer1delay3amount",
        "layer1delay4amount",
        "layer1delay1feedback",
        "layer1delay2feedback",
        "layer1delay3feedback",
        "layer1delay4feedback",
        "layer1delay1sync",
        "layer1delay2sync",
        "layer1delay3sync",
        "layer1delay4sync",
        "layer1delay1lpf",
        "layer1delay2lpf",
        "layer1delay3lpf",
        "layer1delay4lpf",
        "layer1delay1hpf",
        "layer1delay2hpf",
        "layer1delay3hpf",
        "layer1delay4hpf",
        "layer1delay1filtermode",
        "layer1delay2filtermode",
        "layer1delay3filtermode",
        "layer1delay4filtermode",
        "layer1lfo1frequency",
        "layer1lfo2frequency",
        "layer1lfo3frequency",
        "layer1lfo4frequency",
        "layer1lfo1syncsetting",
        "layer1lfo2syncsetting",
        "layer1lfo3syncsetting",
        "layer1lfo4syncsetting",
        "layer1lfo1shape",
        "layer1lfo2shape",
        "layer1lfo3shape",
        "layer1lfo4shape",
        "layer1lfo1amount",
        "layer1lfo2amount",
        "layer1lfo3amount",
        "layer1lfo4amount",
        "layer1lfo1destination",
        "layer1lfo2destination",
        "layer1lfo3destination",
        "layer1lfo4destination",
        "layer1lfo1sync",
        "layer1lfo2sync",
        "layer1lfo3sync",
        "layer1lfo4sync",
        "layer1lfo1wavereset",
        "layer1lfo2wavereset",
        "layer1lfo3wavereset",
        "layer1lfo4wavereset",
        "layer1lfo1slewrate",
        "layer1lfo2slewrate",
        "layer1lfo3slewrate",
        "layer1lfo4slewrate",
        "layer1lfo1phase",
        "layer1lfo2phase",
        "layer1lfo3phase",
        "layer1lfo4phase",
        "layer1delay1pan",
        "layer1delay2pan",
        "layer1delay3pan",
        "layer1delay4pan",
        "layer1env1amount",
        "layer1env2amount",
        "layer1env3amount",
        "layer1env4amount",
        "layer1env1velocitytoamount",
        "layer1env2velocitytoamount",
        "layer1env3velocitytoamount",
        "layer1env4velocitytoamount",
        "layer1env1delay",
        "layer1env2delay",
        "layer1env3delay",
        "layer1env4delay",
        "layer1env1attack",
        "layer1env2attack",
        "layer1env3attack",
        "layer1env4attack",
        "layer1env1decay",
        "layer1env2decay",
        "layer1env3decay",
        "layer1env4decay",
        "layer1env1sustain",
        "layer1env2sustain",
        "layer1env3sustain",
        "layer1env4sustain",
        "layer1env1release",
        "layer1env2release",
        "layer1env3release",
        "layer1env4release",
        "layer1env3destination",
        "layer1env4destination",
        "layer1env1repeat",
        "layer1env2repeat",
        "layer1env3repeat",
        "layer1env4repeat",
        "--",
        "--",
        "--",
        "--",
        "layer1mod1source",
        "layer1mod2source",
        "layer1mod3source",
        "layer1mod4source",
        "layer1mod5source",
        "layer1mod6source",
        "layer1mod7source",
        "layer1mod8source",
        "layer1mod9source",
        "layer1mod10source",
        "layer1mod11source",
        "layer1mod12source",
        "layer1mod13source",
        "layer1mod14source",
        "layer1mod15source",
        "layer1mod16source",
        "layer1mod1amount",
        "layer1mod2amount",
        "layer1mod3amount",
        "layer1mod4amount",
        "layer1mod5amount",
        "layer1mod6amount",
        "layer1mod7amount",
        "layer1mod8amount",
        "layer1mod9amount",
        "layer1mod10amount",
        "layer1mod11amount",
        "layer1mod12amount",
        "layer1mod13amount",
        "layer1mod14amount",
        "layer1mod15amount",
        "layer1mod16amount",
        "layer1mod1destination",
        "layer1mod2destination",
        "layer1mod3destination",
        "layer1mod4destination",
        "layer1mod5destination",
        "layer1mod6destination",
        "layer1mod7destination",
        "layer1mod8destination",
        "layer1mod9destination",
        "layer1mod10destination",
        "layer1mod11destination",
        "layer1mod12destination",
        "layer1mod13destination",
        "layer1mod14destination",
        "layer1mod15destination",
        "layer1mod16destination",
        "layer1distortionamount",
        "--",                                                   // layer1distortionnoisegate
        "layer1bpm",
        "layer1unison",
        "layer1unisondetune",
        "layer1unisonmode",
        "layer1unisonkeyassign",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",                   // Sound Version, must be 0x03
        "layer1arpeggiatormode",
        "layer1arpeggiatorrange",
        "layer1arpeggiator",
        "layer1arpeggiatorautolatch",
        "layer1arpeggiatorclockdivide",
        "layer1arpeggiatorrepeats",
        "layer1arpeggiatorlock",
        "--",                                                           // layer1arpeggiatornumnotes
        "--",
        "--",
        "--",
        "--",
        "layer1arpeggiatornote1",
        "layer1arpeggiatornote2",
        "layer1arpeggiatornote3",
        "layer1arpeggiatornote4",
        "layer1arpeggiatornote5",
        "layer1arpeggiatornote6",
        "layer1arpeggiatornote7",
        "layer1arpeggiatornote8",
        "layer1arpeggiatornote9",
        "layer1arpeggiatornote10",
        "layer1arpeggiatornote11",
        "layer1arpeggiatornote12",
        "layer1arpeggiatornote13",
        "layer1arpeggiatornote14",
        "layer1arpeggiatornote15",
        "layer1arpeggiatornote16",
        "layer1arpeggiatornote17",
        "layer1arpeggiatornote18",
        "layer1arpeggiatornote19",
        "layer1arpeggiatornote20",
        "layer1arpeggiatornote21",
        "layer1arpeggiatornote22",
        "layer1arpeggiatornote23",
        "layer1arpeggiatornote24",
        "layer1arpeggiatornote25",
        "layer1arpeggiatornote26",
        "layer1arpeggiatornote27",
        "layer1arpeggiatornote28",
        "layer1arpeggiatornote29",
        "layer1arpeggiatornote30",
        "layer1arpeggiatornote31",
        "layer1arpeggiatornote32",
        "layer1arpeggiatorvelocity1",
        "layer1arpeggiatorvelocity2",
        "layer1arpeggiatorvelocity3",
        "layer1arpeggiatorvelocity4",
        "layer1arpeggiatorvelocity5",
        "layer1arpeggiatorvelocity6",
        "layer1arpeggiatorvelocity7",
        "layer1arpeggiatorvelocity8",
        "layer1arpeggiatorvelocity9",
        "layer1arpeggiatorvelocity10",
        "layer1arpeggiatorvelocity11",
        "layer1arpeggiatorvelocity12",
        "layer1arpeggiatorvelocity13",
        "layer1arpeggiatorvelocity14",
        "layer1arpeggiatorvelocity15",
        "layer1arpeggiatorvelocity16",
        "layer1arpeggiatorvelocity17",
        "layer1arpeggiatorvelocity18",
        "layer1arpeggiatorvelocity19",
        "layer1arpeggiatorvelocity20",
        "layer1arpeggiatorvelocity21",
        "layer1arpeggiatorvelocity22",
        "layer1arpeggiatorvelocity23",
        "layer1arpeggiatorvelocity24",
        "layer1arpeggiatorvelocity25",
        "layer1arpeggiatorvelocity26",
        "layer1arpeggiatorvelocity27",
        "layer1arpeggiatorvelocity28",
        "layer1arpeggiatorvelocity29",
        "layer1arpeggiatorvelocity30",
        "layer1arpeggiatorvelocity31",
        "layer1arpeggiatorvelocity32",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "layer1slider1mode",                            // NOTE: There is apparently no NRPN for this
        "layer1slider2mode",                            // NOTE: There is apparently no NRPN for this
        "splitpoint",
        "abmode",
        "--",                   // editor byte
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",

        "layer2osc1pitch",
        "layer2osc2pitch",
        "layer2osc3pitch",
        "layer2osc4pitch",
        "layer2osc1finetune",
        "layer2osc2finetune",
        "layer2osc3finetune",
        "layer2osc4finetune",
        "layer2osc1level",
        "layer2osc2level",
        "layer2osc3level",
        "layer2osc4level",
        "layer2osc1shape",
        "layer2osc2shape",
        "layer2osc3shape",
        "layer2osc4shape",
        "layer2osc1waveleft",
        "layer2osc2waveleft",
        "layer2osc3waveleft",
        "layer2osc4waveleft",
        "--",
        "--",
        "--",
        "--",
        "layer2osc1waveright",
        "layer2osc2waveright",
        "layer2osc3waveright",
        "layer2osc4waveright",
        "layer2osc1shapemod",
        "layer2osc2shapemod",
        "layer2osc3shapemod",
        "layer2osc4shapemod",
        "layer2osc1fm",
        "layer2osc2fm",
        "layer2osc3fm",
        "layer2osc4fm",
        "layer2osc1am",
        "layer2osc2am",
        "layer2osc3am",
        "layer2osc4am",
        "layer2osc1suboscillator",
        "--",
        "--",
        "--",
        "layer2osc1slop",
        "layer2osc2slop",
        "layer2osc3slop",
        "layer2osc4slop",
        "layer2osc1glideamount",
        "layer2osc2glideamount",
        "layer2osc3glideamount",
        "layer2osc4glideamount",
        "layer2osc1sync",
        "layer2osc2sync",
        "layer2osc3sync",
        "layer2osc4sync",
        "layer2osc1keyfollow",
        "layer2osc2keyfollow",
        "layer2osc3keyfollow",
        "layer2osc4keyfollow",
        "layer2osc1wavereset",
        "layer2osc2wavereset",
        "layer2osc3wavereset",
        "layer2osc4wavereset",
        "layer2oscallfmmode",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "layer2glidemode",
        "layer2glide",
        "layer2pitchbendrangeup",
        "layer2pitchbendrangedown",
        "layer2air",
        "layer2girth",
        "layer2hack",
        "layer2decimate",
        "layer2drive",
        "--",
        "--",
        "--",
        "layer2delayfeedbackmode",
        "layer2lpffrequency",
        "layer2lpfresonance",
        "layer2lpfkeyamount",
        "layer2lpf24pole",
        "layer2hpffrequency",
        "layer2hpfresonance",
        "layer2hpfkeyamount",
        "layer2panspread",
        "layer2voicevolume",
        "layer2feedbackamount",
        "layer2feedbacktuning",
        "layer2delay1time",
        "layer2delay2time",
        "layer2delay3time",
        "layer2delay4time",
        "layer2delay1syncsetting",
        "layer2delay2syncsetting",
        "layer2delay3syncsetting",
        "layer2delay4syncsetting",
        "layer2delay1amount",
        "layer2delay2amount",
        "layer2delay3amount",
        "layer2delay4amount",
        "layer2delay1feedback",
        "layer2delay2feedback",
        "layer2delay3feedback",
        "layer2delay4feedback",
        "layer2delay1sync",
        "layer2delay2sync",
        "layer2delay3sync",
        "layer2delay4sync",
        "layer2delay1lpf",
        "layer2delay2lpf",
        "layer2delay3lpf",
        "layer2delay4lpf",
        "layer2delay1hpf",
        "layer2delay2hpf",
        "layer2delay3hpf",
        "layer2delay4hpf",
        "layer2delay1filtermode",
        "layer2delay2filtermode",
        "layer2delay3filtermode",
        "layer2delay4filtermode",
        "layer2lfo1frequency",
        "layer2lfo2frequency",
        "layer2lfo3frequency",
        "layer2lfo4frequency",
        "layer2lfo1syncsetting",
        "layer2lfo2syncsetting",
        "layer2lfo3syncsetting",
        "layer2lfo4syncsetting",
        "layer2lfo1shape",
        "layer2lfo2shape",
        "layer2lfo3shape",
        "layer2lfo4shape",
        "layer2lfo1amount",
        "layer2lfo2amount",
        "layer2lfo3amount",
        "layer2lfo4amount",
        "layer2lfo1destination",
        "layer2lfo2destination",
        "layer2lfo3destination",
        "layer2lfo4destination",
        "layer2lfo1sync",
        "layer2lfo2sync",
        "layer2lfo3sync",
        "layer2lfo4sync",
        "layer2lfo1wavereset",
        "layer2lfo2wavereset",
        "layer2lfo3wavereset",
        "layer2lfo4wavereset",
        "layer2lfo1slewrate",
        "layer2lfo2slewrate",
        "layer2lfo3slewrate",
        "layer2lfo4slewrate",
        "layer2lfo1phase",
        "layer2lfo2phase",
        "layer2lfo3phase",
        "layer2lfo4phase",
        "layer2delay1pan",
        "layer2delay2pan",
        "layer2delay3pan",
        "layer2delay4pan",
        "layer2env1amount",
        "layer2env2amount",
        "layer2env3amount",
        "layer2env4amount",
        "layer2env1velocitytoamount",
        "layer2env2velocitytoamount",
        "layer2env3velocitytoamount",
        "layer2env4velocitytoamount",
        "layer2env1delay",
        "layer2env2delay",
        "layer2env3delay",
        "layer2env4delay",
        "layer2env1attack",
        "layer2env2attack",
        "layer2env3attack",
        "layer2env4attack",
        "layer2env1decay",
        "layer2env2decay",
        "layer2env3decay",
        "layer2env4decay",
        "layer2env1sustain",
        "layer2env2sustain",
        "layer2env3sustain",
        "layer2env4sustain",
        "layer2env1release",
        "layer2env2release",
        "layer2env3release",
        "layer2env4release",
        "layer2env3destination",
        "layer2env4destination",
        "layer2env1repeat",
        "layer2env2repeat",
        "layer2env3repeat",
        "layer2env4repeat",
        "--",
        "--",
        "--",
        "--",
        "layer2mod1source",
        "layer2mod2source",
        "layer2mod3source",
        "layer2mod4source",
        "layer2mod5source",
        "layer2mod6source",
        "layer2mod7source",
        "layer2mod8source",
        "layer2mod9source",
        "layer2mod10source",
        "layer2mod11source",
        "layer2mod12source",
        "layer2mod13source",
        "layer2mod14source",
        "layer2mod15source",
        "layer2mod16source",
        "layer2mod1amount",
        "layer2mod2amount",
        "layer2mod3amount",
        "layer2mod4amount",
        "layer2mod5amount",
        "layer2mod6amount",
        "layer2mod7amount",
        "layer2mod8amount",
        "layer2mod9amount",
        "layer2mod10amount",
        "layer2mod11amount",
        "layer2mod12amount",
        "layer2mod13amount",
        "layer2mod14amount",
        "layer2mod15amount",
        "layer2mod16amount",
        "layer2mod1destination",
        "layer2mod2destination",
        "layer2mod3destination",
        "layer2mod4destination",
        "layer2mod5destination",
        "layer2mod6destination",
        "layer2mod7destination",
        "layer2mod8destination",
        "layer2mod9destination",
        "layer2mod10destination",
        "layer2mod11destination",
        "layer2mod12destination",
        "layer2mod13destination",
        "layer2mod14destination",
        "layer2mod15destination",
        "layer2mod16destination",
        "layer2distortionamount",
        "--",                                                   // layer2distortionnoisegate
        "layer2bpm",
        "layer2unison",
        "layer2unisondetune",
        "layer2unisonmode",
        "layer2unisonkeyassign",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",                                   // Sound Version, must be 0x03
        "layer2arpeggiatormode",
        "layer2arpeggiatorrange",
        "layer2arpeggiator",
        "layer2arpeggiatorautolatch",
        "layer2arpeggiatorclockdivide",
        "layer2arpeggiatorrepeats",
        "layer2arpeggiatorlock",
        "--",                                           // layer2arpeggiatornumnotes
        "--",
        "--",
        "--",
        "--",
        "layer2arpeggiatornote1",
        "layer2arpeggiatornote2",
        "layer2arpeggiatornote3",
        "layer2arpeggiatornote4",
        "layer2arpeggiatornote5",
        "layer2arpeggiatornote6",
        "layer2arpeggiatornote7",
        "layer2arpeggiatornote8",
        "layer2arpeggiatornote9",
        "layer2arpeggiatornote10",
        "layer2arpeggiatornote11",
        "layer2arpeggiatornote12",
        "layer2arpeggiatornote13",
        "layer2arpeggiatornote14",
        "layer2arpeggiatornote15",
        "layer2arpeggiatornote16",
        "layer2arpeggiatornote17",
        "layer2arpeggiatornote18",
        "layer2arpeggiatornote19",
        "layer2arpeggiatornote20",
        "layer2arpeggiatornote21",
        "layer2arpeggiatornote22",
        "layer2arpeggiatornote23",
        "layer2arpeggiatornote24",
        "layer2arpeggiatornote25",
        "layer2arpeggiatornote26",
        "layer2arpeggiatornote27",
        "layer2arpeggiatornote28",
        "layer2arpeggiatornote29",
        "layer2arpeggiatornote30",
        "layer2arpeggiatornote31",
        "layer2arpeggiatornote32",
        "layer2arpeggiatorvelocity1",
        "layer2arpeggiatorvelocity2",
        "layer2arpeggiatorvelocity3",
        "layer2arpeggiatorvelocity4",
        "layer2arpeggiatorvelocity5",
        "layer2arpeggiatorvelocity6",
        "layer2arpeggiatorvelocity7",
        "layer2arpeggiatorvelocity8",
        "layer2arpeggiatorvelocity9",
        "layer2arpeggiatorvelocity10",
        "layer2arpeggiatorvelocity11",
        "layer2arpeggiatorvelocity12",
        "layer2arpeggiatorvelocity13",
        "layer2arpeggiatorvelocity14",
        "layer2arpeggiatorvelocity15",
        "layer2arpeggiatorvelocity16",
        "layer2arpeggiatorvelocity17",
        "layer2arpeggiatorvelocity18",
        "layer2arpeggiatorvelocity19",
        "layer2arpeggiatorvelocity20",
        "layer2arpeggiatorvelocity21",
        "layer2arpeggiatorvelocity22",
        "layer2arpeggiatorvelocity23",
        "layer2arpeggiatorvelocity24",
        "layer2arpeggiatorvelocity25",
        "layer2arpeggiatorvelocity26",
        "layer2arpeggiatorvelocity27",
        "layer2arpeggiatorvelocity28",
        "layer2arpeggiatorvelocity29",
        "layer2arpeggiatorvelocity30",
        "layer2arpeggiatorvelocity31",
        "layer2arpeggiatorvelocity32",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "layer2slider1mode",                            // NOTE: There is apparently no NRPN for this
        "layer2slider2mode",                            // NOTE: There is apparently no NRPN for this
        "--",           // splitpoint
        "--",           // abmode
        "--",                   // editor byte
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--",
        "--"
        };
    
/// ADDITIONAL NRPN PROGRAM CHANGES
/// 500 Change the Program assigned to Layer A
/// 501 Change the Bank assigned to Layer A (zero-based, so bank 1 is 0)
/// 502 Change the Program assigned to Layer B
/// 503 Change the Bank assigned to Layer B (zero-based, so bank 1 is 0)


    public Object[] emitAll(String key)
        {
        if (key.equals("bank")) return new Object[0];  // this is not emittable
        if (key.equals("number")) return new Object[0];  // this is not emittable

        int index;
        int value;
        
        
        if (key.equals("name"))
            {
            Object[] ret = new Object[4 * 20];
            char[] name = (model.get("name", "Untitled") + "                ").toCharArray();
            for(int i = 0; i < 20; i++)
                {
                Object[] nrpn = buildNRPN(getChannelOut(), i + 480, name[i]);
                System.arraycopy(nrpn, 0, ret, i * 4, 4);
                }
            return ret;
            }
        else if (key.equals("nameb"))
            {
            Object[] ret = new Object[4 * 20];
            char[] name = (model.get("nameb", "Untitled") + "                ").toCharArray();
            for(int i = 0; i < 20; i++)
                {
                Object[] nrpn = buildNRPN(getChannelOut(), i + 992, name[i]);
                System.arraycopy(nrpn, 0, ret, i * 4, 4);
                }
            return ret;
            }
        else if (
            //key.equals("layer1arpeggiatornumnotes") ||
            //key.equals("layer2arpeggiatornumnotes") ||
            key.equals("layer1slider1mode") ||
            key.equals("layer2slider2mode") ||
            key.equals("layer1slider1mode") ||
            key.equals("layer2slider2mode"))        // no NRPN for these
            {
            return null;
            }
        else if (key.startsWith("layer1arpeggiatornote") ||
            key.startsWith("layer2arpeggiatornote"))
            {
            int val = model.get(key, 0);
            if (val == EDISYN_REST) val = PROPHET_REST;
            //else if (val == EDISYN_OFF) val = PROPHET_OFF;
            return buildNRPN(getChannelOut(), ((Integer)(parametersToIndex.get(key))).intValue(), val);
            }
        else 
            {
            int val = model.get(key, 0);
            Integer idx = ((Integer)(parametersToIndex.get(key)));
            if (idx == null) { System.err.println("Warning DSIProphet12.emitAll(String): Cannot Emit " + key); return new Object[0]; }
            else return buildNRPN(getChannelOut(), idx.intValue(), val);
            }
        }

    public void handleSynthCCOrNRPN(Midi.CCData data)
        {
        if (data.type == Midi.CCDATA_TYPE_NRPN && data.number <= 1011)          // 1012 is PC and 1013 is Bank Change for some reason
            {
            if (data.number >= 480 && data.number <= 499)  // Name
                {
                char[] name = (model.get("name", "Untitled") + "                ").toCharArray();
                name[data.number - 480] = (char)(data.value);
                model.set("name", new String(name).substring(0, 20));
                }
            else if (data.number >= 992 && data.number <= 1011)  // Name
                {
                char[] name = (model.get("nameb", "Untitled") + "                ").toCharArray();
                name[data.number - 992] = (char)(data.value);
                model.set("name", new String(name).substring(0, 20));
                }
            else
                {
                String key = parameters[data.number];
                if (key == "---")
                    return;
                else if (key.startsWith("layer1arpeggiatornote") ||
                    key.startsWith("layer2arpeggiatornote"))
                    {
                    if (data.value > 127)   data.value = EDISYN_REST;
                    //else if (data.value == PROPHET_OFF) data.value = EDISYN_OFF;
                    model.set(key, data.value);
                    }
                else
                    model.set(key, data.value);
                } 
            }               
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

    public void diff(byte[] a, byte[] b)
        {
        for(int i = 0; i < a.length; i++)
            {
            if (a[i] != b[i])
                System.out.println("" + i + " " + StringUtility.toHex(a[i]) + " " + StringUtility.toHex(b[i]) + " (" + a[i] + " " + b[i] + ")" + "\t" + sysexParameters[i]);
            }
        }

    public void dump(byte[] a)
        {
        for(int i = 0; i < a.length; i++)
            {
            System.out.println("" + i + " " + StringUtility.toHex(a[i]) +  " (" + a[i] + ") " + (a[i] >= 32 && a[i] < 127 ? (char)a[i] : " ") + "\t" + sysexParameters[i] ) ;
            }
        }

    byte[] firstPatch;    
    static final boolean REVERSE_ENGINEER = false;
    
    public int parse(byte[] data, boolean fromFile)
        {
        // unfortunately, the Prophet 12 doesn't provide number/bank info
        // with its edit buffer data dump.  :-(
        
        if (data[3] == 0x02)  // program data only, not (0x03) edit buffer
            {
            int bank = data[4];
            if (bank < 8)
                model.set("bank", bank);
            int number = data[5];
            if (number < 99)
                model.set("number", number);
            }

        byte[] d = null;
        if (data[3] == 0x02)
            d = convertTo8Bit(data, 6);
        else if (data[3] == 0x03)
            d = convertTo8Bit(data, 4);
        else
            System.err.println("Warning DSIProphet12.parse(): Unknown program data format " + data[3]);

        if (REVERSE_ENGINEER)
            {
            if (firstPatch == null)
                {
                System.out.println("INITIAL PATCH LOADED");
                firstPatch = d;
                }
            else 
                {
                System.out.println("DIFFERENCES");
                diff(firstPatch, d);
                }
            }

        //dump(d);
        for(int i = 0; i < 1024; i++)
            {
            if (load == LOAD_A && !(
                    sysexParameters[i].startsWith("layer1") ||
                    sysexParameters[i].equals("splitpoint") ||
                    sysexParameters[i].equals("abmode")))
                                                
                {
                // do nothing -- they're not layer 1 (A) parameters
                }
            else if (load == LOAD_B && !(
                    sysexParameters[i].startsWith("layer2") ||
                    sysexParameters[i].equals("splitpoint") ||
                    sysexParameters[i].equals("abmode")))
                {
                // do nothing -- they're not layer 2 (B) parameters
                }
            else if (!sysexParameters[i].equals("--"))
                {
                // Note: DSI isn't 2's complement.  So everything in the model is being stored starting at 0
                int q = d[i];
                if (q < 0) q += 256;  // push to unsigned (not 2's complement)

                if (sysexParameters[i].startsWith("layer1arpeggiatornote") ||
                    sysexParameters[i].startsWith("layer2arpeggiatornote"))
                    {
                    if (q > 127) q = EDISYN_REST;
/*
  if (q == PROPHET_REST) q = EDISYN_REST;
  else //if (q == PROPHET_OFF) q = EDISYN_OFF;
  if (q > 127) q = EDISYN_OFF;
*/
                    }

                model.set(sysexParameters[i], q);
                }
            }
                                
        if (load == LOAD_BOTH || load == LOAD_A) 
            {
            // handle name specially
            byte[] name = new byte[20];
            System.arraycopy(d, 402, name, 0, 20);
            try
                {
                model.set("name", new String(name, "US-ASCII"));
                }
            catch (UnsupportedEncodingException e)
                {
                Synth.handleException(e); 
                }
            }
                
        if (load == LOAD_BOTH || load == LOAD_B)
            {
            // handle name specially
            byte[] name = new byte[20];
            System.arraycopy(d, 402 + 512, name, 0, 20);
            try
                {
                model.set("nameb", new String(name, "US-ASCII"));
                }
            catch (UnsupportedEncodingException e)
                {
                Synth.handleException(e); 
                }
            }
                                
        revise();

        return PARSE_SUCCEEDED;
        }
    
    public static final int SYSEX_VERSION = 0x03;
    
    public byte[] emit(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        if (tempModel == null)
            tempModel = getModel();
        
        byte[] data = null;

        byte[] d = new byte[1024];
        for(int i = 0; i < 1024; i++)
            {
            // Make sure we're Version 3, else the prophet12 will misunderstand our data
            if (i == 293 || i == 293 + 512)
                d[i] = SYSEX_VERSION;   // not sure what that is

            // distribute AB Mode and SplitPoint to layer 2 for good measure
            else if (i == 425 + 512)
                d[i] = d[i-512];        // splitpoint
            else if (i == 426 + 512)
                d[i] = d[i-512];        // abmode

            else if (!sysexParameters[i].equals("--"))
                {
                // Note: DSI isn't 2's complement.  So everything in the model is being stored starting at 0
                int q = model.get(sysexParameters[i], 0);

                if (sysexParameters[i].startsWith("layer1arpeggiatornote") ||
                    sysexParameters[i].startsWith("layer2arpeggiatornote"))
                    {
                    if (q == EDISYN_REST) q = PROPHET_REST;
                    /*
                      else if (q == EDISYN_OFF) q = PROPHET_OFF;
                    */
                    }
                                                                                                                                                                                                                                                                             
                if (q > 127) q -= 256;  // push to signed (not 2's complement)
                d[i] = (byte)q;
                }
            }
                                                                                                
        // handle name specially
        char[] name = (model.get("name", "Untitled") + "                    " ).toCharArray();
        for(int i = 0; i < 20; i++)
            d[402 + i] = (byte)(name[i] & 127);
                                
        // handle nameb specially
        name = (model.get("nameb", "Untitled") + "                    " ).toCharArray();
        for(int i = 0; i < 20; i++)
            d[402 + 512 + i] = (byte)(name[i] & 127);

        if (REVERSE_ENGINEER)
            {
            System.err.println("EMITTED DIFFERENCES");
            //diff(firstPatch, d);
            }
                                
        data = convertTo7Bit(d);  
                
        if (toWorkingMemory)
            {
            byte[] emit = new byte[1171 + 5];
            emit[0] = (byte)0xF0;
            emit[1] = (byte)0x01;  // DSI
            emit[2] = (byte)0x2A;  // Prophet 12
            emit[3] = (byte)0x03;  // Edit Buffer Data Dump
            System.arraycopy(data, 0, emit, 4, data.length);
            emit[emit.length - 1] = (byte)0xF7;
            return emit;
            }
        else
            {
            byte[] emit = new byte[1171 + 7];
            emit[0] = (byte)0xF0;
            emit[1] = (byte)0x01;  // DSI
            emit[2] = (byte)0x2A;  // Prophet 12
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
        tryToSendMIDI(new Object[]
            {
            // bank select
            buildCC(getChannelOut(), 32, tempModel.get("bank"))[0],
            // PC
            buildPC(getChannelOut(), tempModel.get("number"))[0]
            });
        }

    public void writeAllParameters(Model model)
        {
        performChangePatch(model);     // we need to be at the start?
        tryToSendMIDI(emitAll(model, false, false));
        // we CANNOT do change patch after writing data -- it ruins the sysex write.  DSI Bug it would seem. 
        }

    public byte[] requestCurrentDump()
        {
        byte[] data = new byte[5];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x01;   // DSI
        data[2] = (byte)0x2A;   // Prophet 12
        data[3] = (byte)0x06;   // Request Current Dump
        data[4] = (byte)0xF7;                   
        return data;
        }

    public byte[] requestDump(Model tempModel)
        {
        if (tempModel == null)
            tempModel = getModel();

        byte[] data = new byte[7];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x01;   // DSI
        data[2] = (byte)0x2A;   // Prophet 12
        data[3] = (byte)0x05;   // Request Program Dump
        data[4] = (byte)(tempModel.get("bank", 0));
        data[5] = (byte)(tempModel.get("number", 0));
        data[6] = (byte)0xF7;
        return data;
        }
                
    public static final int MAXIMUM_NAME_LENGTH = 20;
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
        
    public static String getSynthName() { return "DSI Prophet 12"; }
    public String getPatchName(Model model) { return model.get("name", "Untitled"); }
    
    public Model getNextPatchLocation(Model model)
        {
        int bank = model.get("bank");
        int number = model.get("number");
        
        number++;
        if (number >= 99)             // weirdly there are only 99 patches per bank
            {
            bank++;
            number = 0;
            if (bank >= 8)
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
        return (BANKS[model.get("bank")] + " " + (number > 9 ? "" : "0") + number);
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

        JMenuItem check = new JCheckBoxMenuItem("Writeable Factory Banks");
        check.setSelected(writeToF);
        check.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                if (!check.isSelected() ||
                    showSimpleConfirm("Writeable Factory Banks", "Treat banks F1 ... F4 as writeable?"))
                    {
                    writeToF = check.isSelected();
                    setLastX("" + writeToF, WRITE_TO_F_KEY, getSynthClassName(), true);
                    }
                else check.setSelected(false);
                }
            });
        menu.add(check);
        menu.addSeparator();
        
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
        }


    public boolean testVerify(Synth synth2, String key, Object obj1, Object obj2)
        {
        // Lots of parameters are stuffed into the same space and so will fail verification.
        
        return (key.equals("nameb"));           // "" vs "                    "
        }


    public int getPauseAfterWritePatch() { return 10; }
       
    public String[] getPatchNumberNames() { return buildIntegerNames(99, 1); }
    public String[] getBankNames() { return BANKS; }
    public boolean[] getWriteableBanks() 
        { 
        if (writeToF)
            return new boolean[] { true, true, true, true, true, true, true, true }; 
        else
            return new boolean[] { true, true, true, true, false, false, false, false }; 
        }
    public boolean getSupportsPatchWrites() { return true; }
    public int getPatchNameLength() { return MAXIMUM_NAME_LENGTH; }
    public boolean librarianTested() { return true; }

    }




/***

    CORRECTED PROPHET 12 PATCH SYSEX DATA FORMAT

    The manual incorrectly suggests that the sysex data format follows
    the same order as the NRPN.  This is reasonable as the Prophet '08,
    Mopho series, and Tetra all do this.  But it is very incorrect.
    The actual data is 1024 bytes consisting of 512 bytes for the first
    layer, then 512 bytes for the second layer.  The data is in the same
    order for both layers with the following exceptions:

    - The first layer also contains the data splitpoint and abmode.
    The second layer does NOT have this data -- it should be just zeros.
          
    - The first layer's name also serves as the patch name.
    The second layer's name is just the layer name.

    The data is then 8-bit-to-7-bit converted into 1171 bytes as described
    in the manual (search for "Packed Data Format", page 89 of manual).  
    The convertTo7Bit(...) and convertTo8Bit(...) methods in this file 
    (DSIProphet12.java) will perform the necessary conversion in Java.

    The sysex messages "PROGRAM DATA DUMP" and "PROGRAM EDIT BUFFER DATA DUMP"
    on page 88 of the manual embed the 1171 byte, 7-bit converted data.

    Below is a listing of the first 512 bytes in the dump, that is, LAYER 1 ONLY.
    Layer 2 is exactly the same except that splitpoint and abmode are
    ignored (set them to 0) and the name bytes are only for layer 2's name.


    0       layer1osc1pitch
    1       layer1osc2pitch
    2       layer1osc3pitch
    3       layer1osc4pitch
    4       layer1osc1finetune
    5       layer1osc2finetune
    6       layer1osc3finetune
    7       layer1osc4finetune
    8       layer1osc1level
    9       layer1osc2level
    10      layer1osc3level
    11      layer1osc4level
    12      layer1osc1shape
    13      layer1osc2shape
    14      layer1osc3shape
    15      layer1osc4shape
    16      layer1osc1waveleft
    17      layer1osc2waveleft
    18      layer1osc3waveleft
    19      layer1osc4waveleft
    20      --
    21      --
    22      --
    23      --
    24      layer1osc1waveright
    25      layer1osc2waveright
    26      layer1osc3waveright
    27      layer1osc4waveright
    28      layer1osc1shapemod
    29      layer1osc2shapemod
    30      layer1osc3shapemod
    31      layer1osc4shapemod
    32      layer1osc1fm
    33      layer1osc2fm
    34      layer1osc3fm
    35      layer1osc4fm
    36      layer1osc1am
    37      layer1osc2am
    38      layer1osc3am
    39      layer1osc4am
    40      layer1osc1suboscillator
    41      --
    42      --
    43      --
    44      layer1osc1slop
    45      layer1osc2slop
    46      layer1osc3slop
    47      layer1osc4slop
    48      layer1osc1glideamount
    49      layer1osc2glideamount
    50      layer1osc3glideamount
    51      layer1osc4glideamount
    52      layer1osc1sync
    53      layer1osc2sync
    54      layer1osc3sync
    55      layer1osc4sync
    56      layer1osc1keyfollow
    57      layer1osc2keyfollow
    58      layer1osc3keyfollow
    59      layer1osc4keyfollow
    60      layer1osc1wavereset
    61      layer1osc2wavereset
    62      layer1osc3wavereset
    63      layer1osc4wavereset
    64      layer1oscallfmmode
    65      --
    66      --
    67      --
    68      --
    69      --
    70      --
    71      --
    72      layer1glidemode
    73      layer1glide
    74      layer1pitchbendrangeup
    75      layer1pitchbendrangedown
    76      layer1air
    77      layer1girth
    78      layer1hack
    79      layer1decimate
    80      layer1drive
    81      --
    82      --
    83      --
    84      layer1delayfeedbackmode
    85      layer1lpffrequency
    86      layer1lpfresonance
    87      layer1lpfkeyamount
    88      layer1lpf24pole
    89      layer1hpffrequency
    90      layer1hpfresonance
    91      layer1hpfkeyamount
    92      layer1panspread
    93      layer1voicevolume
    94      layer1feedbackamount
    95      layer1feedbacktuning
    96      layer1delay1time
    97      layer1delay2time
    98      layer1delay3time
    99      layer1delay4time
    100     layer1delay1syncsetting
    101     layer1delay2syncsetting
    102     layer1delay3syncsetting
    103     layer1delay4syncsetting
    104     layer1delay1amount
    105     layer1delay2amount
    106     layer1delay3amount
    107     layer1delay4amount
    108     layer1delay1feedback
    109     layer1delay2feedback
    110     layer1delay3feedback
    111     layer1delay4feedback
    112     layer1delay1sync
    113     layer1delay2sync
    114     layer1delay3sync
    115     layer1delay4sync
    116     layer1delay1lpf
    117     layer1delay2lpf
    118     layer1delay3lpf
    119     layer1delay4lpf
    120     layer1delay1hpf
    121     layer1delay2hpf
    122     layer1delay3hpf
    123     layer1delay4hpf
    124     layer1delay1filtermode
    125     layer1delay2filtermode
    126     layer1delay3filtermode
    127     layer1delay4filtermode
    128     layer1lfo1frequency
    129     layer1lfo2frequency
    130     layer1lfo3frequency
    131     layer1lfo4frequency
    132     layer1lfo1syncsetting
    133     layer1lfo2syncsetting
    134     layer1lfo3syncsetting
    135     layer1lfo4syncsetting
    136     layer1lfo1shape
    137     layer1lfo2shape
    138     layer1lfo3shape
    139     layer1lfo4shape
    140     layer1lfo1amount
    141     layer1lfo2amount
    142     layer1lfo3amount
    143     layer1lfo4amount
    144     layer1lfo1destination
    145     layer1lfo2destination
    146     layer1lfo3destination
    147     layer1lfo4destination
    148     layer1lfo1sync
    149     layer1lfo2sync
    150     layer1lfo3sync
    151     layer1lfo4sync
    152     layer1lfo1wavereset
    153     layer1lfo2wavereset
    154     layer1lfo3wavereset
    155     layer1lfo4wavereset
    156     layer1lfo1slewrate
    157     layer1lfo2slewrate
    158     layer1lfo3slewrate
    159     layer1lfo4slewrate
    160     layer1lfo1phase
    161     layer1lfo2phase
    162     layer1lfo3phase
    163     layer1lfo4phase
    164     layer1delay1pan
    165     layer1delay2pan
    166     layer1delay3pan
    167     layer1delay4pan
    168     layer1env1amount
    169     layer1env2amount
    170     layer1env3amount
    171     layer1env4amount
    172     layer1env1velocitytoamount
    173     layer1env2velocitytoamount
    174     layer1env3velocitytoamount
    175     layer1env4velocitytoamount
    176     layer1env1delay
    177     layer1env2delay
    178     layer1env3delay
    179     layer1env4delay
    180     layer1env1attack
    181     layer1env2attack
    182     layer1env3attack
    183     layer1env4attack
    184     layer1env1decay
    185     layer1env2decay
    186     layer1env3decay
    187     layer1env4decay
    188     layer1env1sustain
    189     layer1env2sustain
    190     layer1env3sustain
    191     layer1env4sustain
    192     layer1env1release
    193     layer1env2release
    194     layer1env3release
    195     layer1env4release
    196     layer1env3destination
    197     layer1env4destination
    198     layer1env1repeat
    199     layer1env2repeat
    200     layer1env3repeat
    201     layer1env4repeat
    202     --
    203     --
    204     --
    205     --
    206     layer1mod1source
    207     layer1mod2source
    208     layer1mod3source
    209     layer1mod4source
    210     layer1mod5source
    211     layer1mod6source
    212     layer1mod7source
    213     layer1mod8source
    214     layer1mod9source
    215     layer1mod10source
    216     layer1mod11source
    217     layer1mod12source
    218     layer1mod13source
    219     layer1mod14source
    220     layer1mod15source
    221     layer1mod16source
    222     layer1mod1amount
    223     layer1mod2amount
    224     layer1mod3amount
    225     layer1mod4amount
    226     layer1mod5amount
    227     layer1mod6amount
    228     layer1mod7amount
    229     layer1mod8amount
    230     layer1mod9amount
    231     layer1mod10amount
    232     layer1mod11amount
    233     layer1mod12amount
    234     layer1mod13amount
    235     layer1mod14amount
    236     layer1mod15amount
    237     layer1mod16amount
    238     layer1mod1destination
    239     layer1mod2destination
    240     layer1mod3destination
    241     layer1mod4destination
    242     layer1mod5destination
    243     layer1mod6destination
    244     layer1mod7destination
    245     layer1mod8destination
    246     layer1mod9destination
    247     layer1mod10destination
    248     layer1mod11destination
    249     layer1mod12destination
    250     layer1mod13destination
    251     layer1mod14destination
    252     layer1mod15destination
    253     layer1mod16destination
    254     layer1distortionamount
    255     --                                     // layer1distortionnoisegate
    256     layer1bpm                              // NOT IN LAYER 2
    257     layer1unison
    258     layer1unisondetune
    259     layer1unisonmode
    260     layer1unisonkeyassign
    261     --
    262     --
    263     --
    264     --
    265     --
    266     --
    267     --
    268     --
    269     --
    270     --
    271     --
    272     --
    273     --
    274     --
    275     --
    276     --
    277     --
    278     --
    279     --
    280     --
    281     --
    282     --
    283     --
    284     --
    285     --
    286     --
    287     --
    288     --
    289     --
    290     --
    291     --
    292     --
    293     --
    294     layer1arpeggiatormode
    295     layer1arpeggiatorrange
    296     layer1arpeggiator
    297     layer1arpeggiatorautolatch
    298     layer1arpeggiatorclockdivide
    299     layer1arpeggiatorrepeats
    300     layer1arpeggiatorlock
    301     --
    302     --
    303     --
    304     --
    305     --
    306     layer1arpeggiatornote1
    307     layer1arpeggiatornote2
    308     layer1arpeggiatornote3
    309     layer1arpeggiatornote4
    310     layer1arpeggiatornote5
    311     layer1arpeggiatornote6
    312     layer1arpeggiatornote7
    313     layer1arpeggiatornote8
    314     layer1arpeggiatornote9
    315     layer1arpeggiatornote10
    316     layer1arpeggiatornote11
    317     layer1arpeggiatornote12
    318     layer1arpeggiatornote13
    319     layer1arpeggiatornote14
    320     layer1arpeggiatornote15
    321     layer1arpeggiatornote16
    322     layer1arpeggiatornote17
    323     layer1arpeggiatornote18
    324     layer1arpeggiatornote19
    325     layer1arpeggiatornote20
    326     layer1arpeggiatornote21
    327     layer1arpeggiatornote22
    328     layer1arpeggiatornote23
    329     layer1arpeggiatornote24
    330     layer1arpeggiatornote25
    331     layer1arpeggiatornote26
    332     layer1arpeggiatornote27
    333     layer1arpeggiatornote28
    334     layer1arpeggiatornote29
    335     layer1arpeggiatornote30
    336     layer1arpeggiatornote31
    337     layer1arpeggiatornote32
    338     layer1arpeggiatorvelocity1
    339     layer1arpeggiatorvelocity2
    340     layer1arpeggiatorvelocity3
    341     layer1arpeggiatorvelocity4
    342     layer1arpeggiatorvelocity5
    343     layer1arpeggiatorvelocity6
    344     layer1arpeggiatorvelocity7
    345     layer1arpeggiatorvelocity8
    346     layer1arpeggiatorvelocity9
    347     layer1arpeggiatorvelocity10
    348     layer1arpeggiatorvelocity11
    349     layer1arpeggiatorvelocity12
    350     layer1arpeggiatorvelocity13
    351     layer1arpeggiatorvelocity14
    352     layer1arpeggiatorvelocity15
    353     layer1arpeggiatorvelocity16
    354     layer1arpeggiatorvelocity17
    355     layer1arpeggiatorvelocity18
    356     layer1arpeggiatorvelocity19
    357     layer1arpeggiatorvelocity20
    358     layer1arpeggiatorvelocity21
    359     layer1arpeggiatorvelocity22
    360     layer1arpeggiatorvelocity23
    361     layer1arpeggiatorvelocity24
    362     layer1arpeggiatorvelocity25
    363     layer1arpeggiatorvelocity26
    364     layer1arpeggiatorvelocity27
    365     layer1arpeggiatorvelocity28
    366     layer1arpeggiatorvelocity29
    367     layer1arpeggiatorvelocity30
    368     layer1arpeggiatorvelocity31
    369     layer1arpeggiatorvelocity32
    370     --
    371     --
    372     --
    373     --
    374     --
    375     --
    376     --
    377     --
    378     --
    379     --
    380     --
    381     --
    382     --
    383     --
    384     --
    385     --
    386     --
    387     --
    388     --
    389     --
    390     --
    391     --
    392     --
    393     --
    394     --
    395     --
    396     --
    397     --
    398     --
    399     --
    400     --
    401     --
    402     layer1name1                         // Also serves as patch name.  In contrast,
    403     layer1name2                         // Layer 2's name is only the name for Layer 2
    404     layer1name3
    405     layer1name4
    406     layer1name5
    407     layer1name6
    408     layer1name7
    409     layer1name8
    410     layer1name9
    411     layer1name10
    412     layer1name11
    413     layer1name12
    414     layer1name13
    415     layer1name14
    416     layer1name15
    417     layer1name16
    418     layer1name17
    419     layer1name18
    420     layer1name19
    421     layer1name20
    422     layer1slider1mode                   // Latch
    423     layer1slider2mode                   // Latch
    424     splitpoint                          // NOT IN LAYER 2
    425     abmode                              // NOT IN LAYER 2
    426     editorbyte                          // This is a free stored byte that can be used and set by a patch editor for whatever purpose
    427     --
    428     --
    429     --
    430     --
    431     --
    432     --
    433     --
    434     --
    435     --
    436     --
    437     --
    438     --
    439     --
    440     --
    441     --
    442     --
    443     --
    444     --
    445     --
    446     --
    447     --
    448     --
    449     --
    450     --
    451     --
    452     --
    453     --
    454     --
    455     --
    456     --
    457     --
    458     --
    459     --
    460     --
    461     --
    462     --
    463     --
    464     --
    465     --
    466     --
    467     --
    468     --
    469     --
    470     --
    471     --
    472     --
    473     --
    474     --
    475     --
    476     --
    477     --
    478     --
    479     --
    480     --
    481     --
    482     --
    483     --
    484     --
    485     --
    486     --
    487     --
    488     --
    489     --
    490     --
    491     --
    492     --
    493     --
    494     --
    495     --
    496     --
    497     --
    498     --
    499     --
    500     --
    501     --
    502     --
    503     --
    504     --
    505     --
    506     --
    507     --
    508     --
    509     --
    510     --
    511     --
**/
