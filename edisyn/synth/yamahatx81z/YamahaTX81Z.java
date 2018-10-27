/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.yamahatx81z;

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


/**h
   A patch editor for the Yamaha TX81Z.
        
   @author Sean Luke
*/

public class YamahaTX81Z extends Synth
    {
    /// Various collections of parameter names for pop-up menus
        
    public static final String[] CHANNELS = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16" };
    public static final String[] BANKS = { "I", "A", "B", "C", "D" };
    public static final String[] WAVES = {"W1", "W2", "W3", "W4", "W5", "W6", "W7", "W8"};
    public static final ImageIcon[] WAVE_ICONS = 
        {
        new ImageIcon(YamahaTX81Z.class.getResource("Wave1.png")),
        new ImageIcon(YamahaTX81Z.class.getResource("Wave2.png")),
        new ImageIcon(YamahaTX81Z.class.getResource("Wave3.png")),
        new ImageIcon(YamahaTX81Z.class.getResource("Wave4.png")),
        new ImageIcon(YamahaTX81Z.class.getResource("Wave5.png")),
        new ImageIcon(YamahaTX81Z.class.getResource("Wave6.png")),
        new ImageIcon(YamahaTX81Z.class.getResource("Wave7.png")),
        new ImageIcon(YamahaTX81Z.class.getResource("Wave8.png"))
        };
    public static final ImageIcon[] ALGORITHM_ICONS = 
        {
        new ImageIcon(YamahaTX81Z.class.getResource("Algorithm1.png")),
        new ImageIcon(YamahaTX81Z.class.getResource("Algorithm2.png")),
        new ImageIcon(YamahaTX81Z.class.getResource("Algorithm3.png")),
        new ImageIcon(YamahaTX81Z.class.getResource("Algorithm4.png")),
        new ImageIcon(YamahaTX81Z.class.getResource("Algorithm5.png")),
        new ImageIcon(YamahaTX81Z.class.getResource("Algorithm6.png")),
        new ImageIcon(YamahaTX81Z.class.getResource("Algorithm7.png")),
        new ImageIcon(YamahaTX81Z.class.getResource("Algorithm8.png"))
        };
    public static final String[] LFO_WAVES = { "Sawtooth", "Square", "Triangle", "Sample & Hold" };
    public static final String[] SHIFTS = { "96dB", "48dB", "24dB", "12dB" };
    public static final String[] FIX_RANGES = { "255Hz", "510Hz", "1KHz", "2KHz", "4KHz", "8KHz", "16KHz", "32KHz" };
    public static final int[] FIX_RANGE_VALS = { 8, 16, 32, 64, 128, 256, 512, 1024 };
    public static final double[] FREQUENCY_RATIOS = { 0.50, 0.71, 0.78, 0.87, 1.00, 1.41, 1.57, 1.73, 2.00, 2.82, 3.00, 3.14, 3.46, 4.00, 4.24, 4.71, 5.00, 5.19, 5.65, 6.00, 6.28, 6.92, 7.00, 7.07, 7.85, 8.00, 8.48, 8.65, 9.00, 9.42, 9.89, 10.00, 10.38, 10.99, 11.00, 11.30, 12.00, 12.11, 12.56, 12.72, 13.00, 13.84, 14.00, 14.10, 14.13, 15.00, 15.55, 15.57, 15.70, 16.96, 17.27, 17.30, 18.37, 18.84, 19.03, 19.78, 20.41, 20.76, 21.20, 21.98, 22.49, 23.55, 24.22, 25.95 };
    public static final double[] FREQUENCY_RATIOS_MAX = { 0.93, 1.32, 1.37, 1.62, 1.93, 2.73, 3.04, 3.35, 2.93, 4.14, 3.93, 4.61, 5.08, 4.93, 5.55, 6.18, 5.93, 6.81, 6.96, 6.93, 7.75, 8.54, 7.93, 8.37, 9.32, 8.93, 9.78, 10.27, 9.93, 10.89, 11.19, 10.93, 12.00, 12.46, 11.93, 12.60, 12.93, 13.73, 14.03, 14.01, 13.93, 15.46, 14.93, 15.42, 15.60, 15.93, 16.83, 17.19, 17.17, 18.24, 18.74, 18.92, 19.65, 20.31, 20.65, 21.06, 21.88, 22.38, 22.47, 23.45, 24.11, 25.02, 25.84, 27.57 };
    public static final double[] FREQUENCY_RATIO_NEAREST_INTS = { 0.50, 0.50, 1.00, 1.00, 1.00, 1.00, 2.00, 2.00, 2.00, 3.00, 3.00, 3.00, 3.00, 4.00, 4.00, 5.00, 5.00, 5.00, 6.00, 6.00, 6.00, 7.00, 7.00, 7.00, 8.00, 8.00, 8.00, 9.00, 9.00, 9.00, 10.00, 10.00, 10.00, 11.00, 11.00, 11.00, 12.00, 12.00, 13.00, 13.00, 13.00, 14.00, 14.00, 14.00, 14.00, 15.00, 15.00, 15.00, 15.00, 15.00, 15.00, 15.00, 15.00, 15.00, 15.00, 15.00, 15.00, 15.00, 15.00, 15.00, 15.00, 15.00, 15.00, 15.00 };
    
    public static final String[] KS_CURVES = { "Linear", "Exponential", "Logarithmic", "Ramped", "Spit", "Triangle", "Late", "Early" };
    public static final String[] VELOCITY_CURVES = { "Linear", "Logarithmic", "Exponential", "Exponential Strong", "Linear Then Off", "Off Then Linear", "Slow Middle", "Fast Middle" };
    public static final String[] KEYS = new String[] { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
    public static final String[] LFO_SHAPES = new String[] { "Triangle", "Sawtooth", "Square", "Random" };
    public static final String[] SOURCE_MODES = new String[] { "Normal", "Twin", "Double" };
    public static final String[] POLY_MODES = new String[] { "Poly 1", "Poly 2", "Solo 1", "Solo 2" };
    public static final String[] OUT_SELECTS = new String[] { "A", "B", "C", "D", "E", "F", "G", "H" };
    public static final String[] WHEEL_ASSIGNMENTS = new String[] { "Vibrato", "LFO", "Filter" };


    public YamahaTX81Z()
        {
        model.set("bank", 0);
        model.set("number", 0);
                
        for(int i = 0; i < allParameters.length; i++)
            {
            allParametersToIndex.put(allParameters[i], Integer.valueOf(i));
            }
                
        for(int i = 0; i < allAdditionalParameters.length; i++)
            {
            allAdditionalParametersToIndex.put(allAdditionalParameters[i], Integer.valueOf(i));
            }
                
        /// SOUND PANEL
                
        JComponent soundPanel = new SynthPanel(this);
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        hbox.addLast(addGlobal(Style.COLOR_A()));
        vbox.add(hbox);
        
        vbox.add(addLFO(Style.COLOR_B()));
        
        vbox.add(addModulation(Style.COLOR_C()));
        
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Global", soundPanel);
                

        JComponent sourcePanel = new SynthPanel(this);
        vbox = new VBox();
        
        vbox.add(addOperator(1, Style.COLOR_A()));
        vbox.add(addEnvelope(1, Style.COLOR_B()));

        vbox.add(addOperator(2, Style.COLOR_A()));
        vbox.add(addEnvelope(2, Style.COLOR_B()));

        sourcePanel.add(vbox, BorderLayout.CENTER);
        addTab("Operators 1-2", sourcePanel);

        sourcePanel = new SynthPanel(this);
        vbox = new VBox();
        
        vbox.add(addOperator(3, Style.COLOR_A()));
        vbox.add(addEnvelope(3, Style.COLOR_B()));

        vbox.add(addOperator(4, Style.COLOR_A()));
        vbox.add(addEnvelope(4, Style.COLOR_B()));

        sourcePanel.add(vbox, BorderLayout.CENTER);
        addTab("Operators 3-4", sourcePanel);
        
        model.set("name", "INIT VOICE");
        
        loadDefaults();        
        }
                
    public JFrame sprout()
        {
        JFrame frame = super.sprout();
        transmitTo.setEnabled(false);
        writeTo.setEnabled(false);
        addYamahaTX81ZMenu();
        return frame;
        }         

    public String getDefaultResourceFileName() { return "YamahaTX81Z.init"; }
    public String getHTMLResourceFileName() { return "YamahaTX81Z.html"; }

    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        JComboBox bank = new JComboBox(BANKS);
        bank.setSelectedIndex(model.get("bank"));
        if (writing)
            {
            bank = new JComboBox(new String[] { "I" });
            bank.setSelectedIndex(0);
            }
                        
                        
        
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
                showSimpleError(title, "The Patch Number must be an integer 1...32");
                continue;
                }
            if (n < 1 || n > 32)
                {
                showSimpleError(title, "The Patch Number must be an integer 1...32");
                continue;
                }
                
            n--;
                                
            int i = bank.getSelectedIndex();
                        
                        
            change.set("bank", i);
            change.set("number", n);
                        
            return true;
            }
        }

    public static final int OFF = 0;
    public static final int COARSE = 1;
    public static final int INTEGERS = 2;
        
    int mutationRestriction = OFF;
        
    public void addYamahaTX81ZMenu()
        {
        JMenu menu = new JMenu("TX81Z");
        menubar.add(menu);

        JMenu restrictMutation = new JMenu("Restrict Mutated Frequency Ratios...");
        menu.add(restrictMutation);
                
        String str = getLastX("MutationRestriction", getSynthName(), true);
        if (str == null)
            mutationRestriction = OFF;
        else if (str.equalsIgnoreCase("COARSE"))
            mutationRestriction = COARSE;
        else if (str.equalsIgnoreCase("INTEGERS"))
            mutationRestriction = INTEGERS;
        else mutationRestriction = OFF;
                
        ButtonGroup bg = new ButtonGroup();
        JRadioButtonMenuItem off = new JRadioButtonMenuItem("Off");
        off.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt)
                {
                mutationRestriction = OFF;
                setLastX("OFF", "MutationRestriction", getSynthName(), true);
                }
            });
        restrictMutation.add(off);
        bg.add(off);
        if (mutationRestriction == OFF) off.setSelected(true);
                
        JRadioButtonMenuItem tx81z = new JRadioButtonMenuItem("To Coarse Values Only");
        tx81z.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt)
                {
                mutationRestriction = COARSE;
                setLastX("COARSE", "MutationRestriction", getSynthName(), true);
                }
            });
        restrictMutation.add(tx81z);
        bg.add(tx81z);
        if (mutationRestriction == COARSE) tx81z.setSelected(true);

        JRadioButtonMenuItem integers = new JRadioButtonMenuItem("To Integer Coarse Values Only");
        integers.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt)
                {
                mutationRestriction = INTEGERS;
                setLastX("INTEGERS", "MutationRestriction", getSynthName(), true);
                }
            });
        restrictMutation.add(integers);
        bg.add(integers);
        if (mutationRestriction == INTEGERS) integers.setSelected(true);
        }
                

    public Model buildModel()
        {
        return new Model()
            {
            public int reviseMutatedValue(String key, int old, int current)
                {
                if (mutationRestriction == OFF)
                    return current;
                else if (key.startsWith("operator") && key.endsWith("frequencyfine"))
                    {
                    return 0;
                    }
                else if (key.startsWith("operator") && key.endsWith("frequencycoarse"))
                    {
                    if (mutationRestriction == COARSE)
                        return current;
                    else if (mutationRestriction == INTEGERS)
                        {
                        double val = FREQUENCY_RATIO_NEAREST_INTS[current];
                        for(int i = 0; i < FREQUENCY_RATIOS.length; i++)
                            {
                            if (FREQUENCY_RATIOS[i] == val)  // got it
                                return i;
                            }
                        // never happens
                        return current;
                        }
                    else  // never happens
                        return current;
                    }
                else
                    return current;
                }
            };
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
        HBox hbox2 = new HBox();
        comp = new PatchDisplay(this, 4);
        hbox2.add(comp);
        vbox.add(hbox2);
        
        comp = new StringComponent("Patch Name", this, "name", 10, "Name must be up to 10 ASCII characters.")
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

        // Not enough space to show the title
        hbox.addLast(Strut.makeHorizontalStrut(30));

        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }


    public JComponent addGlobal( Color color)
        {
        Category category = new Category(this, "Global", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        
        comp = new LabelledDial("Algorithm", this, "algorithm", color, 0, 7, -1);
        model.removeMetricMinMax("algorithm");  // it's a set
        hbox.add(comp);
        
        hbox.add(Strut.makeHorizontalStrut(10));

        comp = new IconDisplay(null, ALGORITHM_ICONS, this, "algorithm");
        hbox.add(comp);

        hbox.add(Strut.makeHorizontalStrut(10));

        VBox vbox = new VBox();

        HBox hbox2 = new HBox();
        comp = new LabelledDial("Feedback", this, "feedback", color, 0, 7);
        hbox2.add(comp);

        comp = new LabelledDial("Reverb Rate", this, "reverbrate", color, 0, 7);
        hbox2.add(comp);

        comp = new LabelledDial("Transpose", this, "transpose", color, 0, 48, 24)
            {
            public boolean isSymmetric() { return true; }
            };
        hbox2.add(comp);
        
        vbox.add(hbox2);
        
        vbox.add(Strut.makeVerticalStrut(10));
        
        hbox2 = new HBox();
        
        comp = new CheckBox("Mono", this, "mono");
        hbox2.add(comp);

        vbox.add(hbox2);
        hbox.add(vbox);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }





    public JComponent addLFO(Color color)
        {
        Category category = new Category(this, "LFO ", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = LFO_WAVES;
        comp = new Chooser("Wave", this, "lfowave", params);
        vbox.add(comp);

        comp = new CheckBox("Sync", this, "lfosync");
        vbox.add(comp);

        hbox.add(vbox);

        comp = new LabelledDial("Speed", this, "lfospeed", color, 0, 99);
        hbox.add(comp);
        
        comp = new LabelledDial("Delay", this, "lfodelay", color, 0, 99);
        hbox.add(comp);
        
        comp = new LabelledDial("Pitch Mod", this, "lfopitchmodulationsensitivity", color, 0, 7);
        ((LabelledDial)comp).addAdditionalLabel("Sensitivity");
        hbox.add(comp);
        
        comp = new LabelledDial("Pitch Mod", this, "lfopitchmodulationdepth", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("Depth");
        hbox.add(comp);
        
        comp = new LabelledDial("Amplitude Mod", this, "lfoamplitudemodulationsensitivity", color, 0, 3);
        ((LabelledDial)comp).addAdditionalLabel("Sensitivity");
        hbox.add(comp);
        
        comp = new LabelledDial("Amplitude Mod", this, "lfoamplitudemodulationdepth", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("Depth");
        hbox.add(comp);
        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
    
    public JComponent addModulation(Color color)
        {
        Category category = new Category(this, "Controllers", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();

        //comp = new CheckBox("Sustain", this, "sustain");
        //vbox.add(comp);
        //comp = new CheckBox("Portamento", this, "portamento");
        //vbox.add(comp);

        comp = new CheckBox("Full Time Portamento", this, "fulltimeportamentomode", true);
        vbox.add(comp);

        hbox.add(vbox);

        vbox = new VBox();
        HBox hbox2 = new HBox();

        comp = new LabelledDial("Portamento", this, "portamentotime", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("Time");
        hbox2.add(comp);
        
        comp = new LabelledDial("Pitch Bend", this, "pitchbendrange", color, 0, 12);
        ((LabelledDial)comp).addAdditionalLabel("Range");
        hbox2.add(comp);

        comp = new LabelledDial("Foot Ctrl.", this, "footcontrolvolume", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("Volume");
        hbox2.add(comp);
        
        comp = new LabelledDial("Foot Ctrl.", this, "footcontrolpitch", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("Pitch");
        hbox2.add(comp);
        
        comp = new LabelledDial("Foot Ctrl.", this, "footcontrolamplitude", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("Amplitude");
        hbox2.add(comp);
        
        comp = new LabelledDial("Mod Wheel", this, "modulationwheelpitch", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("Pitch");
        hbox2.add(comp);
        
        comp = new LabelledDial("Mod Wheel", this, "modulationwheelamplitude", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("Amplitude");
        hbox2.add(comp);
        
        vbox.add(hbox2);
        hbox2 = new HBox();
        
        comp = new LabelledDial("Breath Ctrl.", this, "breathcontrolpitch", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("Pitch");
        hbox2.add(comp);
        
        comp = new LabelledDial("Breath Ctrl.", this, "breathcontrolamplitude", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("Amplitude");
        hbox2.add(comp);
        
        comp = new LabelledDial("Breath Ctrl.", this, "breathcontrolpitchbias", color, 0, 99, 50);
        ((LabelledDial)comp).addAdditionalLabel("Pitch Bias");
        hbox2.add(comp);
        
        comp = new LabelledDial("Breath Ctrl.", this, "breathcontrolenvelopebias", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("Env. Bias");
        hbox2.add(comp);

        vbox.add(hbox2);

        hbox.add(vbox);        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
    
    // From discussion with Matt Gregory (mgregory22@gmail.com),
    // who runs the TX81Z website http://the-all.org/tx81z/
    public double computeFineRatio(int coarse, int fine)
        {
        double min = FREQUENCY_RATIOS[coarse];
        double max = FREQUENCY_RATIOS_MAX[coarse];
          
        if (min < 1.0)
            {
            return Math.min(max, min + ((max - min) / 7.0) * fine);
            }
        else
            {
            return min + ((max - min) / 15.0) * fine;
            }
        }

    // based on http://cd.textfiles.com/fredfish/v1.6/FF_Disks/571-600/FF_598/TX81z/TX81z.doc
    // in combination with the manual.
    public int computeCoarseFrequency(int range, int coarse)
        {
        // the others are just multiples of it
        int base = 1;
        for(int i = 0; i < range; i++)
            base *= 2;

        if (coarse < 4) return 8 * base;
        else return 16 * (coarse / 4) * base;
        }

    // based on http://cd.textfiles.com/fredfish/v1.6/FF_Disks/571-600/FF_598/TX81z/TX81z.doc
    // in combination with the manual.
    public int computeFrequency(int range, int coarse, int fine)
        {
        // the others are just multiples of it
        int base = 1;
        for(int i = 0; i < range; i++)
            base *= 2;

        if (coarse < 4)
            fine = Math.min(fine, 7);

        if (coarse < 4) return (8 + fine) * base;
        else return (16 * (coarse / 4) + fine) * base;

        /*

        // compute the base for range = 255
        
        int base = coarse * 4;
        
        // looks like the first region is shorter than the others...
        if (coarse == 0)
        base = 8;
        else if (coarse == 1)
        base = 10;
        else if (coarse == 2)
        base = 12;
        else if (coarse == 3)
        base = 14;
                
        base += fine;
        
        // I think you can't go over 255
        if (base > 255)
        base = 255;
        
        // the others are just multiples of it
        for(int i = 0; i < range; i++)
        base *= 2;
        
        return base;
        */
        }


    LabelledDial[] frequencyRanges = new LabelledDial[4];
    JLabel[] coarseFrequencyLabels = new JLabel[4];
    JLabel[] fineFrequencyLabels = new JLabel[4];
    java.text.DecimalFormat format = new java.text.DecimalFormat("0.0#");

    public JComponent addOperator(final int src, Color color)
        {
        final Category category = new Category(this, "Operator " + src, color);
        category.makePasteable("operator" + src);

        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = WAVES;
        comp = new Chooser("Wave", this, "operator" + src + "operatorwaveform", params, WAVE_ICONS);
        vbox.add(comp);

        comp = new CheckBox("AM", this, "operator" + src + "amplitudemodulationenable");
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Env. Bias", this, "operator" + src + "egbiassensitivity", color, 0, 7);
        ((LabelledDial)comp).addAdditionalLabel("Sensitivity");
        hbox.add(comp);
        
        comp = new LabelledDial("Key Velocity", this, "operator" + src + "keyvelocitysensitivity", color, 0, 7);
        ((LabelledDial)comp).addAdditionalLabel("Sensitivity");
        hbox.add(comp);
        
        comp = new LabelledDial("Output", this, "operator" + src + "outputlevel", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);

        comp = new LabelledDial("Detune", this, "operator" + src + "detune", color, 0, 6, 3)
            {
            public boolean isSymmetric() { return true; }
            };
        hbox.add(comp);


        // we define this first so that it populates the model for the other widgets  But we'll add it at the end.

        frequencyRanges[src - 1] = new LabelledDial("Frequency", this, "operator" + src + "fixedfrequencyrange", color, 0, 7)
            {
            public String map(int val)
                {
                return FIX_RANGES[val];
                }               
            };
        frequencyRanges[src - 1].addAdditionalLabel("Range");





        // we define this next so that it populates the model for the other widgets  But we'll add it at the end.
    
        CheckBox fixcomp = new CheckBox("Fixed Frequency", this, "operator" + src + "fix")
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                if (model.get(key) == 0)
                    {
                    hbox.remove(frequencyRanges[src - 1]);
                    if (coarseFrequencyLabels[src - 1] != null)                         // won't be the case initially
                        coarseFrequencyLabels[src - 1].setText("Ratio");
                    if (fineFrequencyLabels[src - 1] != null)                           // won't be the case initially
                        fineFrequencyLabels[src - 1].setText("Ratio Fine");
                    }
                else
                    {
                    hbox.add(frequencyRanges[src - 1]);
                    if (coarseFrequencyLabels[src - 1] != null)                         // won't be the case initially
                        coarseFrequencyLabels[src - 1].setText("Fixed");
                    if (fineFrequencyLabels[src - 1] != null)                           // won't be the case initially
                        fineFrequencyLabels[src - 1].setText("Fixed Fine");
                    }
                hbox.revalidate();
                }
            };
        ((CheckBox)fixcomp).addToWidth(1);




        comp = new LabelledDial("Frequency ", this, "operator" + src + "frequencycoarse", color, 0, 63)  // extra space because OS X cuts off the 'y'
            {
            public String map(int val)
                {
                if (model.get("operator" + src + "fix") == 0)  // not fixed
                    {
                    return "" + FREQUENCY_RATIOS[val];
                    }
                else
                    {
                    return "" + computeCoarseFrequency(
                        model.get("operator" + src + "fixedfrequencyrange"),
                        model.get("operator" + src + "frequencycoarse"));
                    }
                }               
            };
        coarseFrequencyLabels[src - 1 ] = ((LabelledDial)comp).addAdditionalLabel("Fixed");
        model.register("operator" + src + "fixedfrequencyrange", (Updatable)comp);
        model.register("operator" + src + "fix", (Updatable)comp);              
        hbox.add(comp);
        
        format.setRoundingMode(java.math.RoundingMode.FLOOR);
        comp = new LabelledDial("Frequency ", this, "operator" + src + "frequencyfine", color, 0, 15)  // extra space because OS X cuts off the 'y'
            {
            public String map(int val)
                {
                if (model.get("operator" + src + "fix") == 0)  // not fixed
                    {
                    return format.format(computeFineRatio(
                            model.get("operator" + src + "frequencycoarse"), 
                            model.get("operator" + src + "frequencyfine")));
                    }
                else
                    {
                    return "" + computeFrequency(
                        model.get("operator" + src + "fixedfrequencyrange"),
                        model.get("operator" + src + "frequencycoarse"),
                        model.get("operator" + src + "frequencyfine"));
                    }
                }               
            };
        fineFrequencyLabels[src - 1] = ((LabelledDial)comp).addAdditionalLabel("Fine");
        model.register("operator" + src + "fixedfrequencyrange", (Updatable)comp);
        model.register("operator" + src + "frequencycoarse", (Updatable)comp);
        model.register("operator" + src + "fix", (Updatable)comp);
        hbox.add(comp);


        // now we add the frequency range
        hbox.add(frequencyRanges[src - 1]);

    
        // we put this last so that by the time it's updating, the fine frequency dials have been built
        vbox.add(fixcomp);
        // update its labels just in case
        fixcomp.update("operator" + src + "fix", model);

    
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
                
    public JComponent addEnvelope(final int envelope, Color color)
        {
        Category category = new Category(this, "Operator Envelope " + envelope, color);
        category.makePasteable("operator" + envelope);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
               
        comp = new LabelledDial("Attack", this, "operator" + envelope + "attackrate", color, 0, 31);
        ((LabelledDial)comp).addAdditionalLabel("Rate");
        hbox.add(comp);

        comp = new LabelledDial("Decay 1", this, "operator" + envelope + "decay1rate", color, 0, 31);
        ((LabelledDial)comp).addAdditionalLabel("Rate");
        hbox.add(comp);

        comp = new LabelledDial("Decay 1", this, "operator" + envelope + "decay1level", color, 0, 15);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);

        comp = new LabelledDial("Decay 2", this, "operator" + envelope + "decay2rate", color, 0, 31)
            {
            public String map(int val)
                {
                if (val == 0) return "Sustain";
                else return "" + val;
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Rate");
        model.setMetricMin("operator" + envelope + "decay2rate", 1);
        hbox.add(comp);

        comp = new LabelledDial("Release", this, "operator" + envelope + "releaserate", color, 1, 15);
        ((LabelledDial)comp).addAdditionalLabel("Rate");
        hbox.add(comp);
        
        comp = new LabelledDial("Level", this, "operator" + envelope + "levelscaling", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("Scaling");
        hbox.add(comp);
        
        comp = new LabelledDial("Rate", this, "operator" + envelope + "ratescaling", color, 0, 3);
        ((LabelledDial)comp).addAdditionalLabel("Scaling");
        hbox.add(comp);
    
        if (envelope == 1)  // operator 1 doesn't have EG Shift
            {
            comp = new LabelledDial("Shift", this, "operator" + envelope + "shift", color, 0, 0)  // fixed to 0
                {
                public String map(int val)
                    {
                    return SHIFTS[val];
                    }
                };
            // we're not going to add it.  Instead we're just going to put a space in.
            comp = Strut.makeStrut(comp);
            model.setStatus("operator" + envelope + "shift", Model.STATUS_IMMUTABLE);
            }
        else
            {
            // TODO: Modify envelope display to show shift
            comp = new LabelledDial("Shift", this, "operator" + envelope + "shift", color, 0, 3)
                {
                public String map(int val)
                    {
                    return SHIFTS[val];
                    }
                };
            }
        hbox.add(comp);
    

        // ADSR
        // This will *more or less* work, though the release rate will be slightly short
        // for reasons beyond me.
        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "operator" + envelope + "attackrate", "operator" + envelope + "decay1rate", "operator" + envelope + "decay2rate", "operator" + envelope + "releaserate" },
            new String[] { null, null, "operator" + envelope + "decay1level", "operator" + envelope + "decay1level", null },
            new double[] { 0, 1.0/3, 1.0/3, 1.0/3, 1.0/3 },
            new double[] { 0, 1.0, 1.0 / 15.0, 1.0 / 30.0, 0 },
            new double[] { 0, (Math.PI/4/31), (Math.PI/4/31), (Math.PI/4/31), (Math.PI/4/31) })  // note we convert the release rate to 31
            {
            public double preprocessXKey(int index, String key, double value)
                {
                if (key.equals("operator" + envelope + "releaserate"))
                    {
                    return (31.0 - ( (value - 1) * 31.0 / 14.0 ));
                    }
                else 
                    return 31.0 - value;
                }

            public void postProcess(double[] xVals, double[] yVals)
                {
                if (model.get("operator" + envelope + "decay2rate") == 0)
                    {
                    yVals[3] = yVals[2];
                    xVals[3] = 1.0 / 3;
                    }

                int shift = model.get("operator" + envelope + "shift");
                                        
                if (shift > 0)
                    {
                    for(int i = 0; i < 5; i++)
                        {
                        yVals[i] = 1 - yVals[i];
                                                
                        for(int j = 0; j < shift; j++)  
                            yVals[i] /= 2.0;
                                                                                                        
                        yVals[i] = 1 - yVals[i];
                        }
                    }
                }
            };

        /*
          comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
          new String[] { null, "operator" + envelope + "attackrate", "operator" + envelope + "decay1rate", "operator" + envelope + "decay2rate", "operator" + envelope + "releaserate" },
          new String[] { null, null, "operator" + envelope + "decay1level", "operator" + envelope + "decay1level", null },
          new double[] { 0, 0.25/31.0, 0.25/ 31.0,  0.25/31.0, 0.25/15.0 },
          new double[] { 0, 1.0,                1.0 / 15.0,    1.0 / 30.0, 0 })
          {
          public void postProcess(double[] xVals, double[] yVals)
          {
          if (model.get("operator" + envelope + "decay2rate") == 0)
          {
          yVals[3] = yVals[2];
          }

          xVals[1] = 0.25 - xVals[1];
          xVals[2] = 0.25 - xVals[2];
          xVals[3] = 0.25 - xVals[3];
          xVals[4] = 0.25 - xVals[4];
                                        
          int shift = model.get("operator" + envelope + "shift");
                                        
          if (shift > 0)
          {
          for(int i = 0; i < 5; i++)
          {
          yVals[i] = 1 - yVals[i];
                                                
          for(int j = 0; j < shift; j++)  
          yVals[i] /= 2.0;
                                                                                                        
          yVals[i] = 1 - yVals[i];
          }
          }
          }
          };
        */
        model.register("operator" + envelope + "shift", (Updatable)comp);
        hbox.addLast(comp);
                
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
        



    /** Map of parameter -> index in the allParameters array. */
    HashMap allParametersToIndex = new HashMap();


    /** List of all Sysex parameters in order.  "-" is a reserved (unused and thus unnamed) parameter. */

    /// * indicates parameters which must be handled specially due to packing
    /// that Waldorf decided to do.  :-(

    final static String[] allParameters = new String[] 
    {
    "operator4attackrate",
    "operator4decay1rate",
    "operator4decay2rate",
    "operator4releaserate",
    "operator4decay1level",
    "operator4levelscaling",
    "operator4ratescaling",
    "operator4egbiassensitivity",
    "operator4amplitudemodulationenable",                   
    "operator4keyvelocitysensitivity",
    "operator4outputlevel",
    "operator4frequencycoarse",
    "operator4detune",

    "operator2attackrate",
    "operator2decay1rate",
    "operator2decay2rate",
    "operator2releaserate",
    "operator2decay1level",
    "operator2levelscaling",
    "operator2ratescaling",
    "operator2egbiassensitivity",
    "operator2amplitudemodulationenable",                   
    "operator2keyvelocitysensitivity",
    "operator2outputlevel",
    "operator2frequencycoarse",
    "operator2detune",

    "operator3attackrate",
    "operator3decay1rate",
    "operator3decay2rate",
    "operator3releaserate",
    "operator3decay1level",
    "operator3levelscaling",
    "operator3ratescaling",
    "operator3egbiassensitivity",
    "operator3amplitudemodulationenable",                   
    "operator3keyvelocitysensitivity",
    "operator3outputlevel",
    "operator3frequencycoarse",
    "operator3detune",

    "operator1attackrate",
    "operator1decay1rate",
    "operator1decay2rate",
    "operator1releaserate",
    "operator1decay1level",
    "operator1levelscaling",
    "operator1ratescaling",
    "operator1egbiassensitivity",
    "operator1amplitudemodulationenable",                   
    "operator1keyvelocitysensitivity",
    "operator1outputlevel",
    "operator1frequencycoarse",
    "operator1detune",
    
    "algorithm",
    "feedback",         
    "lfospeed",
    "lfodelay",
    "lfopitchmodulationdepth",
    "lfoamplitudemodulationdepth",        
    "lfosync",        
    "lfowave",
    "lfopitchmodulationsensitivity",
    "lfoamplitudemodulationsensitivity",    
    "transpose",
    
    "mono",
    "pitchbendrange",  
    "fulltimeportamentomode",
    "portamentotime",
    "footcontrolvolume",
    "sustain",                  // unused as it turns out
    "portamento",       // unused as it turns out
    "chorus",                   // unused as it turns out        
    "modulationwheelpitch",
    "modulationwheelamplitude",
    "breathcontrolpitch",
    "breathcontrolamplitude",          
    "breathcontrolpitchbias",
    "breathcontrolenvelopebias",
    "name1",
    "name2",
    "name3",
    "name4",
    "name5",
    "name6",
    "name7",
    "name8",
    "name9",
    "name10",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    };



    /** Map of parameter -> index in the allParameters array. */
    HashMap allAdditionalParametersToIndex = new HashMap();


    /** List of all Sysex parameters in order.  "-" is a reserved (unused and thus unnamed) parameter. */

    /// * indicates parameters which must be handled specially due to packing
    /// that Waldorf decided to do.  :-(

    final static String[] allAdditionalParameters = new String[] 
    {
    "operator4fix",
    "operator4fixedfrequencyrange",
    "operator4frequencyfine",
    "operator4operatorwaveform",
    "operator4shift",

    "operator2fix",
    "operator2fixedfrequencyrange",
    "operator2frequencyfine",
    "operator2operatorwaveform",
    "operator2shift",

    "operator3fix",
    "operator3fixedfrequencyrange",
    "operator3frequencyfine",
    "operator3operatorwaveform",
    "operator3shift",

    "operator1fix",
    "operator1fixedfrequencyrange",
    "operator1frequencyfine",
    "operator1operatorwaveform",
    "operator1shift",
    
    "reverbrate",
    "footcontrolpitch",         
    "footcontrolamplitude"
    };

    /*
      From mgregory22@gmail.com:

      I wrote an editor for the TX81Z awhile back.  Yeah, I think there are a couple bugs in the manual if I remember correctly. I see you wrote Gizmo in C++, and I worked out all the MIDI bugs in my editor, so I'm going to refer to that.

      Here is a file of constants with subgroup numbers on line 173:
      https://github.com/mgregory22/tx81z-programmer/blob/master/src/tx81z.h#L173

      So, the PCED has a subgroup of 0.

      There's also this file, which describes the different dump types in the TX81Z:
      https://github.com/mgregory22/tx81z-programmer/blob/master/src/tx81z_meta.c

      reqStr is a request string, and dumpHdr is the header of the received dump.

      Here's the code to interface with the TX81Z.  I implemented it as a window that sends messages, so I wouldn't have to write threading code, so it might be kind of confusing in places, but I think you'll be able to get it:
      https://github.com/mgregory22/tx81z-programmer/blob/master/src/tx81z.c

      TX81Z_SendParamChange() is probably what you're interested in, if you want to know how I did things:
      https://github.com/mgregory22/tx81z-programmer/blob/master/src/tx81z.c#L507

      I haven't worked on it in a long time, so I can't remember all that much about it, but if you have any questions, let me know and I'll try to answer them.

      Hope that helps,
      Matt

    */

    public static final int VCED_GROUP = 2 + 16; // 00010010
    public static final int ACED_GROUP = 3 + 16; // 00010011
    public static final int PCED_GROUP = 0 + 16; // 00010000        says 00010011 in the manual, wrong
    public static final int REMOTE_SWITCH_GROUP = 3 + 16; // 00010011        same as ACED_GROUP

    public Object[] emitAll(String key)
        {
        simplePause(50);
        if (key.equals("bank")) return new Object[0];  // this is not emittable
        if (key.equals("number")) return new Object[0];  // this is not emittable

        byte channel = (byte)(16 + getChannelOut());
         
        if (key.equals("name"))
            {
            Object[] result = new Object[10];
            
            String name = model.get("name", "INIT VOICE") + "          ";

            for(int i = 0; i < 10; i++)
                {
                result[i] = new byte[] { (byte)0xF0, 0x43, channel, VCED_GROUP, (byte)(77 + i), (byte)(name.charAt(i)), (byte)0xF7 };
                }
            return result;
            }
        else if (allParametersToIndex.containsKey(key))
            {
            int index = ((Integer)(allParametersToIndex.get(key))).intValue();
            int value = model.get(key);
            
            byte PP = (byte) index;
            byte VV = (byte) value;
            byte[] data = new byte[] { (byte)0xF0, 0x43, channel, VCED_GROUP, PP, VV, (byte)0xF7 };
            return new Object[] { data };
            }
        else if (allAdditionalParametersToIndex.containsKey(key))
            {
            int index = ((Integer)(allAdditionalParametersToIndex.get(key))).intValue();
            int value = model.get(key);

            for(int i = 0; i < 4; i++)
                {
                if (key.equals("operator" + i + "frequencyfine"))
                    {
                    if (model.get("operator" + i + "frequencycoarse") < 4)  // it's < 1.0
                        value = Math.min(value, 7);  //  only first 8 values are legal
                    }
                }
                            
            byte PP = (byte) index;
            byte VV = (byte) value;
            byte[] data = new byte[] { (byte)0xF0, 0x43, channel, ACED_GROUP, PP, VV, (byte)0xF7 };
            return new Object[] { data };
            }
        else 
            {
            System.err.println("Warning (YamahaTX81Z): Can't emit key " + key);
            return new Object[0];
            }
        }
    

    // returns true if the parse was *successful*, not just complete
    final static int FAIL = 0;
    final static int VCED = 1;
    final static int ACED = 2;
        
    int subparse(byte[] data, boolean fromFile)
        {
        // okay we're not recursing, let's parse
        
        boolean vced = true;
        // Is it VCED or ACED?  -- manual says "SCED", oops
        if (data.length == 101 &&       // 93 + 8 byte wrapper
            data[3] == 0x03 &&
            data[4] == 0x00 &&
            data[5] == 0x5D) // VCED?
            {
            vced = true;
            }
        else if (data.length == 41 &&   // 23 + "LM  8976AE" + 8 byte wrapper
            data[3] == 0x7E &&
            data[4] == 0x00 &&
            data[5] == 0x21 &&
            // next it spits out the header "LM  8976AE"
            data[6] == 'L' &&
            data[7] == 'M' &&
            data[8] == ' ' &&
            data[9] == ' ' &&
            data[10] == '8' &&
            data[11] == '9' &&
            data[12] == '7' &&
            data[13] == '6' &&
            data[14] == 'A' &&
            data[15] == 'E')
            {
            vced = false;
            }
        else    
            {
            System.err.println("Warning (YamahaTX81Z): Can't parse data (length " + data.length + ").  First bytes:");
            int len = 16;
            if (len > data.length) 
                len = data.length;
            for(int i = 0; i < len; i++)
                System.err.print(" " + String.format("%02X", data[i]));
            System.err.println();
            return FAIL;
            }
        
        String[] params = (vced ? allParameters : allAdditionalParameters);
        int start = (vced ? 6 : 16);
        
        byte[] name = new byte[11];
        name[10] = '\0';  // yes I know it's that already...
        
        for(int i = 0; i < params.length; i++)
            {
            byte val = data[i + start];
                
            if (params[i].equals("-"))
                continue;
            else if (i >= 77 && i <= 86) // name
                {
                name[i - 77] = val;
                }
            else
                {
                model.set(params[i], val);
                }
            }
                
        if (vced)
            {
            try { model.set("name", new String(name, "US-ASCII")); }
            catch (Exception e) { e.printStackTrace(); }
            revise();  // vced comes AFTER aced
            }
        return (vced ? VCED : ACED);    
        }
                

    public int parse(byte[] data, boolean fromFile)
        {
        if (data.length == 4104)  // VMEM
            {
            return parseVMEM(data, fromFile);
            }
        else if (data.length >= 41 + 101) // probably ACED + VCED + [optional more junk] break up and call recursively
            {
            if (data.length > 41 + 101)
                {
                showSimpleMessage("Multiple Patches", 
                    "<html>This file may contain multiple patches, but not in bank format.<br>" +
                    "Because of the TX81Z's strange way of loading patches (two sysex files)<br>" +
                    "Edisyn can currently only open the <b>first patch</b> in this file.");
                }
                
            byte[] d = new byte[41];
            System.arraycopy(data, 0, d, 0, 41); 
            int result = subparse(d, fromFile);
            if (result == FAIL)
                return PARSE_FAILED;
            d = new byte[101];
            System.arraycopy(data, 41, d, 0, 101);
            result = subparse(d, fromFile);
            if (result == FAIL)
                return PARSE_FAILED;
            return PARSE_SUCCEEDED;
            }
        else
            {
            int result = subparse(data, fromFile);
            if (result == FAIL)
                return PARSE_FAILED;
            else if (result == ACED)
                return PARSE_INCOMPLETE;
            else // if (result == VCED)
                return PARSE_SUCCEEDED;
            }
                        
        }
        
    public int parseVMEM(byte[] data, boolean fromFile)
        {
        // extract names
        char[][] names = new char[32][10];
        for(int i = 0; i < 32; i++)
            {
            for (int j = 0; j < 10; j++)
                {
                names[i][j] = (char)(data[i * 128 + 57 + j + 6] & 127);
                }
            }
                        
        String[] n = new String[32];
        for(int i = 0; i < 32; i++)
            {
            n[i] = "" + (i + 1) + "   " + new String(names[i]);
            }
            
        // Now that we have an array of names, one per patch, we present the user with options;
        // 0. Cancel [handled automatically]
        // 1. Save the bank data [handled automatically]
        // 2. Upload the bank data [handled automatically] 
        // 3. Load and edit a certain patch number
        int patchNum = showBankSysexOptions(data, n);
        if (patchNum < 0) 
            return PARSE_CANCELLED;

        model.set("name", new String(names[patchNum]));
        model.set("number", patchNum);
        model.set("bank", 0);                   // we don't know what the bank is in reality
                
        // okay, we're loading and editing patch number patchNum.  Here we go.
        int patch = patchNum * 128;
        int pos = 0;
                                                                                
        for(int op = 0; op < 4; op++)
            {
            // attack rate
            model.set(allParameters[pos++], data[patch + op * 10 + 0 + 6] & 31);
            // decay 1 rate
            model.set(allParameters[pos++], data[patch + op * 10 + 1 + 6] & 31);
            // decay 2 rate
            model.set(allParameters[pos++], data[patch + op * 10 + 2 + 6] & 31);
            // release rate
            model.set(allParameters[pos++], data[patch + op * 10 + 3 + 6] & 15);
            // decay 1 level
            model.set(allParameters[pos++], data[patch + op * 10 + 4 + 6] & 15);
            // level scaling
            model.set(allParameters[pos++], data[patch + op * 10 + 5 + 6] & 127);
            // rate scaling
            model.set(allParameters[pos++], (data[patch + op * 10 + 9 + 6] >>> 3) & 3);
            // eg bias sensitivity
            model.set(allParameters[pos++], (data[patch + op * 10 + 6 + 6] >>> 3) & 7);
            // amplitude modulation
            model.set(allParameters[pos++], (data[patch + op * 10 + 6 + 6] >>> 6) & 1);
            // key velocity sensitivity
            model.set(allParameters[pos++], (data[patch + op * 10 + 6 + 6] >>> 0) & 7);
            // operator output level
            model.set(allParameters[pos++], data[patch + op * 10 + 7 + 6] & 127);
            // frequency
            model.set(allParameters[pos++], data[patch + op * 10 + 8 + 6] & 63);
            // detune
            model.set(allParameters[pos++], (data[patch + op * 10 + 9 + 6] >>> 0) & 7);
            }


        // algorithm
        model.set(allParameters[pos++], (data[patch + 40 + 6] >>> 0) & 7);
        // feedback
        model.set(allParameters[pos++], (data[patch + 40 + 6] >>> 3) & 7);
        // lfo speed
        model.set(allParameters[pos++], data[patch + 41 + 6] & 127);
        // lfo delay
        model.set(allParameters[pos++], data[patch + 42 + 6] & 127);
        // pitch modulation depth
        model.set(allParameters[pos++], data[patch + 43 + 6] & 127);
        // amplitude modulation depth
        model.set(allParameters[pos++], data[patch + 44 + 6] & 127);
        // lfo sync
        model.set(allParameters[pos++], (data[patch + 40 + 6] >>> 6) & 1);
        // lfo wave
        model.set(allParameters[pos++], (data[patch + 45 + 6] >>> 0) & 3);
        // pitch modulation sensitivity
        model.set(allParameters[pos++], (data[patch + 45 + 6] >>> 4) & 7);
        // amplitude modulation sensitivity
        model.set(allParameters[pos++], (data[patch + 45 + 6] >>> 2) & 3);
        // transpose
        model.set(allParameters[pos++], data[patch + 46 + 6] & 63);  // not marked in the documentation, but it goes 0...48


        // poly/mono
        model.set(allParameters[pos++], (data[patch + 48 + 6] >>> 3) & 1);
        // pitch bend range
        model.set(allParameters[pos++], data[patch + 47 + 6] & 15);
        // portamento mode
        model.set(allParameters[pos++], (data[patch + 48 + 6] >>> 0) & 1);
        // portamento time
        model.set(allParameters[pos++], data[patch + 49 + 6] & 127);
        // foot control volume
        model.set(allParameters[pos++], data[patch + 50 + 6] & 127);
        // sustain -- nonexistent
        model.set(allParameters[pos++], (data[patch + 48 + 6] >>> 2) & 1);
        // portamento -- nonexistent
        model.set(allParameters[pos++], (data[patch + 48 + 6] >>> 1) & 1);
        // chorus -- nonexistent
        model.set(allParameters[pos++], (data[patch + 48 + 6] >>> 4) & 1);
        // modulation wheel pitch
        model.set(allParameters[pos++], data[patch + 51 + 6] & 127);
        // modulation wheel amplitude
        model.set(allParameters[pos++], data[patch + 52 + 6] & 127);
        // breath control pitch
        model.set(allParameters[pos++], data[patch + 53 + 6] & 127);
        // breath control amplitude
        model.set(allParameters[pos++], data[patch + 54 + 6] & 127);
        // breath control pitch bias
        model.set(allParameters[pos++], data[patch + 55 + 6] & 127);
        // breath control eg bias
        model.set(allParameters[pos++], data[patch + 56 + 6] & 127);
        
        //// Names appear here
        
        //// Then parameters NOT used in the TX81Z
        
        //// Then Operator 4...1 On/Off
        
        //// Then additional parameters
        pos = 0;  // reset
        for(int op = 0; op < 4; op++)
            {
            // eg shift
            model.set(allAdditionalParameters[pos++], (data[patch + op * 2 + 73 + 6] >>> 4) & 3);
            // fixed frequency
            model.set(allAdditionalParameters[pos++], (data[patch + op * 2 + 73 + 6] >>> 3) & 1);
            // fixed frequency range
            model.set(allAdditionalParameters[pos++], (data[patch + op * 2 + 73 + 6] >>> 0) & 7);

            // operator waveform
            model.set(allAdditionalParameters[pos++], (data[patch + op * 2 + 74 + 6] >>> 4) & 7);
            // frequency range fine
            model.set(allAdditionalParameters[pos++], (data[patch + op * 2 + 74 + 6] >>> 0) & 15);
            }

        // reverb rate
        model.set(allAdditionalParameters[pos++], data[patch + 81 + 6] & 7);
        // foot controller pitch
        model.set(allAdditionalParameters[pos++], data[patch + 82 + 6] & 127);
        // foot controller amplitude
        model.set(allAdditionalParameters[pos++], data[patch + 83 + 6] & 127);
             
        revise();
        return PARSE_SUCCEEDED;
        }
 
    
    public Object[] emitAll(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        simplePause(50);
        byte[][] result = new byte[2][];
        
        // First the ACED
        result[0] = new byte[41];
        byte[] data = new byte[33];

        data[0] = (byte)'L';
        data[1] = (byte)'M';
        data[2] = (byte)' ';
        data[3] = (byte)' ';
        data[4] = (byte)'8';
        data[5] = (byte)'9';
        data[6] = (byte)'7';
        data[7] = (byte)'6';
        data[8] = (byte)'A';
        data[9] = (byte)'E';
        
        for(int i = 0; i < allAdditionalParameters.length; i++)
            {
            data[i + 10] = (byte)(model.get(allAdditionalParameters[i]));

            // handle low fine ratio values
            for(int j = 0; j < 4; j++)
                {
                if (allAdditionalParameters[i].equals("operator" + j + "frequencyfine"))
                    {
                    if (model.get("operator" + j + "frequencycoarse") < 4)  // it's < 1.0
                        data[i + 10] = (byte)(Math.min(data[i + 10] , 7));  //  only first 8 values are legal
                    }
                }
            }

        result[0][0] = (byte)0xF0;
        result[0][1] = 0x43;
        result[0][2] = (byte)(getChannelOut()); //(byte)(32 + getChannelOut());
        result[0][3] = (byte)0x7E;
        result[0][4] = 0x00;
        result[0][5] = 0x21;
        System.arraycopy(data, 0, result[0], 6, data.length);
        result[0][6 + data.length] = produceChecksum(data);
        result[0][7 + data.length] = (byte)0xF7;


        // Next the VCED
        result[1] = new byte[101];
        data = new byte[93];
        for(int i = 0; i < allParameters.length - 16; i++)  // no name, no extra gunk
            {
            data[i] = (byte)(model.get(allParameters[i]));
            }
        
        String name = model.get("name", "INIT VOICE") + "          ";
                
        for(int i = 0; i < 10; i++)
            {
            data[allParameters.length - 16 + i] = (byte)(name.charAt(i));
            }
        
        result[1][0] = (byte)0xF0;
        result[1][1] = 0x43;
        result[1][2] = (byte)(getChannelOut());
        result[1][3] = 0x03;
        result[1][4] = 0x00;
        result[1][5] = 0x5D;
        System.arraycopy(data, 0, result[1], 6, data.length);
        result[1][6 + data.length] = produceChecksum(data);
        result[1][7 + data.length] = (byte)0xF7;
        
        return result;
        }

    /** Generate a TX81Z checksum of the data bytes */
    byte produceChecksum(byte[] bytes)
        {
        //      The TX81Z manual says the checksum is the
        //              "Twos complement of the lower 7 bits of the sum of all databytes".
        //
        //              Apparently this is mistaken.  Based on the function "Snapshot_Checksum" here...
        //              https://raw.githubusercontent.com/mgregory22/tx81z-programmer/master/src/snapshot.c
        //
        //              It may be otherwise.  So here's my shot.

        int checksum = 0;
        for(int i = 0; i < bytes.length; i++)
            checksum = (checksum + bytes[i]) & 255;
        return (byte)((256 - checksum) & 127);
        }

    public void performRequestDump(Model tempModel, boolean changePatch)
        {
        // We ALWAYS change the patch no matter what.  We have to.
        changePatch(tempModel);
        tryToSendSysex(requestDump(tempModel));
        }

    public byte[] requestDump(Model tempModel) 
        {
        // since performRequestDump ALWAYS changes the patch, we might
        // as well just call requestCurrentDump() here 
        return requestCurrentDump(); 
        }
        
    public byte[] requestCurrentDump()
        {
        // ACED + VCED
        byte channel = (byte)(32 + getChannelOut());
        return new byte[] { (byte)0xF0, 0x43, channel, 0x7E, 
            (byte)'L', (byte)'M', (byte)' ', (byte)' ',
            (byte)'8', (byte)'9', (byte)'7', (byte)'6',
            (byte)'A', (byte)'E', (byte)0xF7 }; 
        }
        
    public static boolean recognizeBulk(byte[] data)
        {
        // VMEM
        boolean b = (data.length == 4104 &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x43 &&
            // don't care about 2, it's the channel
            data[3] == (byte)0x04 &&
            data[4] == (byte)0x20 &&        // manual says 10 but this is wrong
            data[5] == (byte)0x00);
        return b;
        }

    static boolean recognizeBasic(byte[] data)
        {
        // VCED
        if (data.length == 101 &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x43 &&
            // don't care about 2, it's the channel
            data[3] == (byte)0x03 &&
            data[4] == (byte)0x00 &&
            data[5] == (byte)0x5D)
            return true;

        // *Probably* ACED + VCED
        else if (data.length == 41 + 101 &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x43 &&
            // don't care about 2, it's the channel
            data[3] == 0x7E &&
            data[4] == 0x00 &&
            data[5] == 0x21 &&
            // next it spits out the header "LM  8976AE"
            data[6] == 'L' &&
            data[7] == 'M' &&
            data[8] == ' ' &&
            data[9] == ' ' &&
            data[10] == '8' &&
            data[11] == '9' &&
            data[12] == '7' &&
            data[13] == '6' &&
            data[14] == 'A' &&
            data[15] == 'E')
            return true;
                
        // ACED
        else if (isACED(data))
            return true;
        
        else return false;
        }
        
    public static int getNumSysexDumpsPerPatch(byte[] data) 
        {
        if (recognizeBasic(data)) return 2;
        else return 1;
        }

    public static boolean recognize(byte[] data)
        {
        return recognizeBasic(data) || recognizeBulk(data);
        }
        
    static boolean isACED(byte[] data)
        {
        return (data.length == 41 &&        
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x43 &&
            // don't care about 2, it's the channel
            data[3] == 0x7E &&
            data[4] == 0x00 &&
            data[5] == 0x21 &&
            // next it spits out the header "LM  8976AE"
            data[6] == 'L' &&
            data[7] == 'M' &&
            data[8] == ' ' &&
            data[9] == ' ' &&
            data[10] == '8' &&
            data[11] == '9' &&
            data[12] == '7' &&
            data[13] == '6' &&
            data[14] == 'A' &&
            data[15] == 'E');
        }
    
    byte[] lastACED;
    // gotta get two merge results
    public boolean merge(byte[] data, double probability)
        {
        if (isACED(data))
            { 
            lastACED = (byte[]) data.clone(); 
            return false;
            }
        else
            {
            if (lastACED == null)  // uh oh, didn't get it
                {
                return false;
                }
            else
                {
                byte[] newData = new byte[lastACED.length + data.length];
                System.arraycopy(lastACED, 0, newData, 0, lastACED.length);
                System.arraycopy(data, 0, newData, lastACED.length, data.length);
                lastACED = null;
                return super.merge(newData, probability);
                }
            }
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

        String nm = model.get("name", "Init");
        String newnm = revisePatchName(nm);
        if (!nm.equals(newnm))
            model.set("name", newnm);
        }
        
    public static String getSynthName() { return "Yamaha TX81Z"; }

    public void changePatch(Model tempModel) 
        {
        int bank = tempModel.get("bank");
        int number = tempModel.get("number");
        int val = bank * 32 + number;
        byte lo = (byte)(val & 127);
        byte hi = (byte)(val >>> 7);
        
        // A program change in the TX81Z is a complicated affair.  We need to do three things:
        //
        // 1. Modify a slot in the program change table to the patch we want.  We'll modify slot 127.
        //
        // 2. At this point the TX81Z is in a strange "I got edited via MIDI" mode.  We need to get
        //    out of that and into standard program mode.  We do this by using sysex commands to virtually press
        //    the PLAY/PERFORM switch.
        //
        // 3. Now we're either in PLAY mode or we're in PERFORM mode.  At this point we send a PC 127, which
        //    causes the system to look up slot 127 in its table, discover it's a performance patch,
        //    and switch to that, while also changing to PERFORM mode.

        // Change program change table position 127 to our desired patch
        byte[] table = new byte[9];
        table[0] = (byte)0xF0;
        table[1] = (byte)0x43;
        table[2] = (byte)(16 + getChannelOut());
        table[3] = (byte)0x10;
        table[4] = (byte)127;  // really!
        table[5] = (byte)127;  // we're changing table position 127
        table[6] = hi;
        table[7] = lo;
        table[8] = (byte)0xF7;
        tryToSendSysex(table);
        
        // Instruct the TX81Z to press its "PLAY/PERFORM" button
        byte PP = (byte) 68;
        byte VV = (byte) 0;
        byte[] data = new byte[] { (byte)0xF0, (byte)0x43, (byte)(16 + getChannelOut()), REMOTE_SWITCH_GROUP, PP, (byte)0x7F, (byte)0xF7 };
        tryToSendSysex(data);

        // Do the program change to program 127
        tryToSendMIDI(buildPC(getChannelOut(), 127));

        // we assume that we successfully did it
        if (!isMerging())  // we're actually loading the patch, not merging with it
            {
            setSendMIDI(false);
            model.set("number", number);
            model.set("bank", bank);
            setSendMIDI(true);
            }
        }
    
    public String getPatchName(Model model) { return model.get("name", "INIT VOICE"); }

    public Model getNextPatchLocation(Model model)
        {
        int bank = model.get("bank");
        int number = model.get("number");
        
        number++;
        if (number >= 32)
            {
            bank++;
            number = 0;
            if (bank >= 5)
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
        return BANKS[model.get("bank")] + (number > 9 ? "" : "0") + number;
        }
        

    }
