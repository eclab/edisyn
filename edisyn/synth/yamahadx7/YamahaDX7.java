/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.yamahadx7;

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
   A patch editor for the Yamaha DX7.
        
   @author Sean Luke
*/

public class YamahaDX7 extends Synth
    {
    /// Various collections of parameter names for pop-up menus
        
    public static final ImageIcon[] ALGORITHM_ICONS = 
        {
        new ImageIcon(YamahaDX7.class.getResource("Algorithm1.png")),
        new ImageIcon(YamahaDX7.class.getResource("Algorithm2.png")),
        new ImageIcon(YamahaDX7.class.getResource("Algorithm3.png")),
        new ImageIcon(YamahaDX7.class.getResource("Algorithm4.png")),
        new ImageIcon(YamahaDX7.class.getResource("Algorithm5.png")),
        new ImageIcon(YamahaDX7.class.getResource("Algorithm6.png")),
        new ImageIcon(YamahaDX7.class.getResource("Algorithm7.png")),
        new ImageIcon(YamahaDX7.class.getResource("Algorithm8.png")),
        new ImageIcon(YamahaDX7.class.getResource("Algorithm9.png")),
        new ImageIcon(YamahaDX7.class.getResource("Algorithm10.png")),
        new ImageIcon(YamahaDX7.class.getResource("Algorithm11.png")),
        new ImageIcon(YamahaDX7.class.getResource("Algorithm12.png")),
        new ImageIcon(YamahaDX7.class.getResource("Algorithm13.png")),
        new ImageIcon(YamahaDX7.class.getResource("Algorithm14.png")),
        new ImageIcon(YamahaDX7.class.getResource("Algorithm15.png")),
        new ImageIcon(YamahaDX7.class.getResource("Algorithm16.png")),
        new ImageIcon(YamahaDX7.class.getResource("Algorithm17.png")),
        new ImageIcon(YamahaDX7.class.getResource("Algorithm18.png")),
        new ImageIcon(YamahaDX7.class.getResource("Algorithm19.png")),
        new ImageIcon(YamahaDX7.class.getResource("Algorithm20.png")),
        new ImageIcon(YamahaDX7.class.getResource("Algorithm21.png")),
        new ImageIcon(YamahaDX7.class.getResource("Algorithm22.png")),
        new ImageIcon(YamahaDX7.class.getResource("Algorithm23.png")),
        new ImageIcon(YamahaDX7.class.getResource("Algorithm24.png")),
        new ImageIcon(YamahaDX7.class.getResource("Algorithm25.png")),
        new ImageIcon(YamahaDX7.class.getResource("Algorithm26.png")),
        new ImageIcon(YamahaDX7.class.getResource("Algorithm27.png")),
        new ImageIcon(YamahaDX7.class.getResource("Algorithm28.png")),
        new ImageIcon(YamahaDX7.class.getResource("Algorithm29.png")),
        new ImageIcon(YamahaDX7.class.getResource("Algorithm30.png")),
        new ImageIcon(YamahaDX7.class.getResource("Algorithm31.png")),
        new ImageIcon(YamahaDX7.class.getResource("Algorithm32.png")),
        };
    public static final String[] LFO_WAVES = { "Triangle", "Saw Down", "Saw Up", "Square", "Sine", "Sample & Hold" };

    // These four are from https://github.com/kroger/csound-instruments/blob/master/dx7/dx72csnd.c
    public static final String[] RATIO_COARSE = { "0.5", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31" };
    public static final String[] RATIO_FINE = { "1.00", "1.01", "1.02", "1.03", "1.04", "1.05", "1.06", "1.07", "1.08", "1.09", "1.10", "1.11", "1.12", "1.13", "1.14", "1.15", "1.16", "1.17", "1.18", "1.19", "1.20", "1.21", "1.22", "1.23", "1.24", "1.25", "1.26", "1.27", "1.28", "1.29", "1.30", "1.31", "1.32", "1.33", "1.34", "1.35", "1.36", "1.37", "1.38", "1.39", "1.40", "1.41", "1.42", "1.43", "1.44", "1.45", "1.46", "1.47", "1.48", "1.49", "1.50", "1.51", "1.52", "1.53", "1.54", "1.55", "1.56", "1.57", "1.58", "1.59", "1.60", "1.61", "1.62", "1.63", "1.64", "1.65", "1.66", "1.67", "1.68", "1.69", "1.70", "1.71", "1.72", "1.73", "1.74", "1.75", "1.76", "1.77", "1.78", "1.79", "1.80", "1.81", "1.82", "1.83", "1.84", "1.85", "1.86", "1.87", "1.88", "1.89", "1.90", "1.91", "1.92", "1.93", "1.94", "1.95", "1.96", "1.97", "1.98", "1.99" };
    // maybe change this one
    public static final String[] FIXED_COARSE = { "1", "10", "100", "1000", "1", "10", "100", "1000", "1", "10", "100", "1000", "1", "10", "100", "1000", "1", "10", "100", "1000", "1", "10", "100", "1000", "1", "10", "100", "1000", "1", "10", "100", "1000" };
    public static final String[] FIXED_FINE = { "1.000", "1.023", "1.047", "1.072", "1.096", "1.122", "1.148", "1.175", "1.202", "1.230", "1.259", "1.288", "1.318", "1.349", "1.380", "1.413", "1.445", "1.479", "1.514", "1.549", "1.585", "1.622", "1.660", "1.698", "1.738", "1.778", "1.820", "1.862", "1.905", "1.950", "1.995", "2.042", "2.089", "2.138", "2.188", "2.239", "2.291", "2.344", "2.399", "2.455", "2.512", "2.570", "2.630", "2.692", "2.716", "2.818", "2.884", "2.951", "3.020", "3.090", "3.162", "3.236", "3.311", "3.388", "3.467", "3.548", "3.631", "3.715", "3.802", "3.890", "3.981", "4.074", "4.169", "4.266", "4.365", "4.467", "4.571", "4.677", "4.786", "4.898", "5.012", "5.129", "5.248", "5.370", "5.495", "5.623", "5.754", "5.888", "6.026", "6.166", "6.310", "6.457", "6.607", "6.761", "6.918", "7.079", "7.244", "7.413", "7.586", "7.762", "7.943", "8.128", "8.318", "8.511", "8.718", "8.913", "9.120", "9.333", "9.550", "9.772" };
    
    public static final int NEGLINEAR = 0;
    public static final int NEGEXP = 1;
    public static final int POSEXP = 2;
    public static final int POSLINEAR = 3;
    
    public static final String[] KS_CURVES = { "- Linear", "- Exp", "+ Exp", "+ Linear" };
    public static final String[] NOTES = {"A", "A#", "B", "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#" };


    public JFrame sprout()
        {
        JFrame frame = super.sprout();
        transmitTo.setEnabled(false);
        writeTo.setEnabled(false);
        return frame;
        }         

    public YamahaDX7()
        {
        model.set("number", 0);
                
        for(int i = 0; i < allParameters.length; i++)
            {
            allParametersToIndex.put(allParameters[i], Integer.valueOf(i));
            }

        /// SOUND PANEL
                
        JComponent soundPanel = new SynthPanel(this);
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        hbox.addLast(addGlobal(Style.COLOR_A()));
        vbox.add(hbox);
        
        vbox.add(addLFO(Style.COLOR_B()));
        
        vbox.add(addPitchEnvelope(Style.COLOR_C()));
        
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Global", soundPanel);
                

        JComponent sourcePanel = new SynthPanel(this);
        vbox = new VBox();
        
        hbox = new HBox();
        hbox.add(addOperator(1, Style.COLOR_A()));
        hbox.add(addKeyScaling(1, Style.COLOR_A()));
        vbox.add(hbox);
        vbox.add(addEnvelope(1, Style.COLOR_B()));

        hbox = new HBox();
        hbox.add(addOperator(2, Style.COLOR_A()));
        hbox.add(addKeyScaling(2, Style.COLOR_A()));
        vbox.add(hbox);
        vbox.add(addEnvelope(2, Style.COLOR_B()));

        sourcePanel.add(vbox, BorderLayout.CENTER);
        addTab("Operators 1-2", sourcePanel);

        sourcePanel = new SynthPanel(this);
        vbox = new VBox();
        
        hbox = new HBox();
        hbox.add(addOperator(3, Style.COLOR_A()));
        hbox.add(addKeyScaling(3, Style.COLOR_A()));
        vbox.add(hbox);
        vbox.add(addEnvelope(3, Style.COLOR_B()));

        hbox = new HBox();
        hbox.add(addOperator(4, Style.COLOR_A()));
        hbox.add(addKeyScaling(4, Style.COLOR_A()));
        vbox.add(hbox);
        vbox.add(addEnvelope(4, Style.COLOR_B()));

        sourcePanel.add(vbox, BorderLayout.CENTER);
        addTab("Operators 3-4", sourcePanel);
        
        sourcePanel = new SynthPanel(this);
        vbox = new VBox();
        
        hbox = new HBox();
        hbox.add(addOperator(5, Style.COLOR_A()));
        hbox.add(addKeyScaling(5, Style.COLOR_A()));
        vbox.add(hbox);
        vbox.add(addEnvelope(5, Style.COLOR_B()));

        hbox = new HBox();
        hbox.add(addOperator(6, Style.COLOR_A()));
        hbox.add(addKeyScaling(6, Style.COLOR_A()));
        vbox.add(hbox);
        vbox.add(addEnvelope(6, Style.COLOR_B()));

        sourcePanel.add(vbox, BorderLayout.CENTER);
        addTab("Operators 5-6", sourcePanel);

        model.set("name", "INIT VOICE");
        
        loadDefaults();        
        }
                
    public String getDefaultResourceFileName() { return "YamahaDX7.init"; }
    public String getHTMLResourceFileName() { return "YamahaDX7.html"; }

    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        JTextField number = new JTextField("" + (model.get("number") + 1), 3);

        while(true)
            {
            boolean result = showMultiOption(this, new String[] { "Patch Number"}, 
                new JComponent[] { number }, title, "Enter the Patch number");
                
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
        vbox.add(comp);  // doesn't work right :-(
        vbox.addBottom(Stretch.makeVerticalStretch()); 
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
        
        
        comp = new LabelledDial("Algorithm", this, "algorithm", color, 0, 31, -1);
        model.removeMetricMinMax("algorithm");  // it's a set
        hbox.add(comp);
        
        hbox.add(Strut.makeHorizontalStrut(10));

        comp = new IconDisplay(null, ALGORITHM_ICONS, this, "algorithm", 104, 104);
        hbox.add(comp);

        hbox.add(Strut.makeHorizontalStrut(10));

        VBox vbox = new VBox();

        HBox hbox2 = new HBox();
        comp = new LabelledDial("Feedback", this, "feedback", color, 0, 7);
        hbox2.add(comp);

        comp = new LabelledDial("Transpose", this, "transpose", color, 0, 48, 24)
            {
            public boolean isSymmetric() { return true; }
            };
        hbox2.add(comp);
        
        vbox.add(hbox2);
        
        vbox.add(Strut.makeVerticalStrut(10));
        
        hbox2 = new HBox();
        
        comp = new CheckBox("Oscillator Key Sync", this, "oscillatorkeysync");
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

        comp = new CheckBox("Sync", this, "lfokeysync");
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
        
        comp = new LabelledDial("Amplitude Mod", this, "lfoamplitudemodulationdepth", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("Depth");
        hbox.add(comp);
        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
    

    public JComponent addPitchEnvelope(Color color)
        {
        Category category = new Category(this, "Pitch Envelope", color);
        category.makeDistributable("pitcheg");
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
               

        comp = new LabelledDial("Rate 1", this,  "pitchegrate1", color, 0, 99);
        hbox.add(comp);

        comp = new LabelledDial("Level 1", this,  "pitcheglevel1", color, 0, 99)
            {
            public boolean isSymmetric() { return true; }
            public int getDefaultValue() { return 50; }
            };
        hbox.add(comp);

        comp = new LabelledDial("Rate 2", this,  "pitchegrate2", color, 0, 99);
        hbox.add(comp);

        comp = new LabelledDial("Level 2", this,  "pitcheglevel2", color, 0, 99)
            {
            public boolean isSymmetric() { return true; }
            public int getDefaultValue() { return 50; }
            };
        hbox.add(comp);

        comp = new LabelledDial("Rate 3", this,  "pitchegrate3", color, 0, 99);
        hbox.add(comp);

        comp = new LabelledDial("Level 3", this,  "pitcheglevel3", color, 0, 99)
            {
            public boolean isSymmetric() { return true; }
            public int getDefaultValue() { return 50; }
            };
        hbox.add(comp);


        comp = new LabelledDial("Rate 4", this,  "pitchegrate4", color, 0, 99);
        hbox.add(comp);

        comp = new LabelledDial("Level 4", this,  "pitcheglevel4", color, 0, 99)
            {
            public boolean isSymmetric() { return true; }
            public int getDefaultValue() { return 50; }
            };
        ((LabelledDial)comp).addAdditionalLabel("(Start/End) ");
        hbox.add(comp);

        // ADSR
        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null,   "pitchegrate1",   "pitchegrate2",   "pitchegrate3",   "pitchegrate4" },
            new String[] {   "pitcheglevel4",   "pitcheglevel1",   "pitcheglevel2",   "pitcheglevel3",   "pitcheglevel4" },
            new double[] { 0, 0.25/99, 0.25/99, 0.25/99, 0.25/99 },
            new double[] { 1.0 / 99, 1.0 / 99, 1.0 / 99, 1.0 / 99, 1.0 / 99 })
            {
            public void postProcess(double[] xVals, double[] yVals)
                {
                // The DX7 uses 99 for SHORT and 0 for LONG, weird
                for(int i = 1; i < 5; i++)
                    xVals[i] = 0.25 - xVals[i];
                }
            };
        ((EnvelopeDisplay)comp).setAxis(50.0 / 99.0);  // it seems 50 is the standard midpoint
        hbox.addLast(comp);
                
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
        

    public JComponent addKeyScaling(final int src, Color color)
        {
        final Category category = new Category(this, "Keyboard Level Scaling " + src, color);
        category.makePasteable("operator" + src);

        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = KS_CURVES;
        comp = new Chooser("Left Curve", this, "operator" + src + "keyboardlevelscalingleftcurve", params);
        vbox.add(comp);
        
        comp = new Chooser("Right Curve", this, "operator" + src + "keyboardlevelscalingrightcurve", params);
        vbox.add(comp);
        
        hbox.add(vbox);

        comp = new LabelledDial("Breakpoint", this, "operator" + src + "keyboardlevelscalingbreakpoint", color, 0, 99)
            {
            public String map(int val)
                {
                int oct = (val - 3) / 12;
                return NOTES[val % 12] + oct;
                }
            public boolean isSymmetric() { return true; }
            public int getDefaultValue() { return 50; }
                
            };
        hbox.add(comp);
        
        comp = new LabelledDial("Left Depth", this, "operator" + src + "keyboardlevelscalingleftdepth", color, 0, 99);
        hbox.add(comp);
        
        comp = new LabelledDial("Right Depth", this, "operator" + src + "keyboardlevelscalingrightdepth", color, 0, 99);
        hbox.add(comp);
        
        hbox.add(new YamahaDX7Curve(this, 
                "operator" + src + "keyboardlevelscalingbreakpoint",
                "operator" + src + "keyboardlevelscalingleftcurve",
                "operator" + src + "keyboardlevelscalingrightcurve",
                "operator" + src + "keyboardlevelscalingleftdepth",
                "operator" + src + "keyboardlevelscalingrightdepth"
                ));

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }




    public JComponent addOperator(final int src, Color color)
        {
        final Category category = new Category(this, "Operator " + src, color);
        category.makePasteable("operator" + src);

        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        comp = new CheckBox("Fixed", this, "operator" + src + "oscillatormode");
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Amp. Mod", this, "operator" + src + "amplitudemodulationsensitivity", color, 0, 3);
        ((LabelledDial)comp).addAdditionalLabel("Sensivity");
        hbox.add(comp);
    
        comp = new LabelledDial("Keyboard", this, "operator" + src + "keyboardratescaling", color, 0, 7);
        ((LabelledDial)comp).addAdditionalLabel("Rate Scaling");
        hbox.add(comp);
    
        comp = new LabelledDial("Key Velocity", this, "operator" + src + "keyvelocitysensitivity", color, 0, 7);
        ((LabelledDial)comp).addAdditionalLabel("Sensivity");
        hbox.add(comp);
    
        comp = new LabelledDial("Detune", this, "operator" + src + "frequencydetune", color, 0, 14, 7)
            {
            public boolean isSymmetric() { return true; }
            };
        hbox.add(comp);

        comp = new LabelledDial("Frequency", this, "operator" + src + "frequencycoarse", color, 0, 31)
            {
            public String map(int val)
                {
                if (model.get("operator" + src + "oscillatormode", 0) == 0)
                    {
                    return RATIO_COARSE[val];
                    }
                else
                    {
                    return FIXED_COARSE[val];
                    }
                }               
            };
        ((LabelledDial)comp).addAdditionalLabel("Coarse");
        model.register("operator" + src + "oscillatormode", ((LabelledDial) comp));
        hbox.add(comp);

        comp = new LabelledDial("Frequency", this, "operator" + src + "frequencyfine", color, 0, 99)
            {
            public String map(int val)
                {
                if (model.get("operator" + src + "oscillatormode", 0) == 0)
                    {
                    return RATIO_FINE[val];
                    }
                else
                    {
                    return FIXED_FINE[val];
                    }
                }               
            };
        ((LabelledDial)comp).addAdditionalLabel("Fine");
        model.register("operator" + src + "oscillatormode", ((LabelledDial) comp));
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
        
                
    public JComponent addEnvelope(final int envelope, Color color)
        {
        Category category = new Category(this, "Operator Envelope " + envelope, color);
        category.makePasteable("operator" + envelope);
        category.makeDistributable("operator" + envelope);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
               
        comp = new LabelledDial("Output", this, "operator" + envelope + "operatoroutputlevel", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);
        
        hbox.add(Strut.makeHorizontalStrut(30));
    
        comp = new LabelledDial("Rate 1", this, "operator" + envelope + "rate1", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("(Attack)");
        hbox.add(comp);

        comp = new LabelledDial("Level 1", this, "operator" + envelope + "level1", color, 0, 99);
        hbox.add(comp);

        comp = new LabelledDial("Rate 2", this, "operator" + envelope + "rate2", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("(Decay)");
        hbox.add(comp);

        comp = new LabelledDial("Level 2", this, "operator" + envelope + "level2", color, 0, 99);
        hbox.add(comp);

        comp = new LabelledDial("Rate 3", this, "operator" + envelope + "rate3", color, 0, 99);
        hbox.add(comp);

        comp = new LabelledDial("Level 3", this, "operator" + envelope + "level3", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("(Sustain)");
        hbox.add(comp);

        comp = new LabelledDial("Rate 4", this, "operator" + envelope + "rate4", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("(Release)");
        hbox.add(comp);

        comp = new LabelledDial("Level 4", this, "operator" + envelope + "level4", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("(Start/End) ");
        hbox.add(comp);
        
        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "operator" + envelope + "rate1", "operator" + envelope + "rate2", "operator" + envelope + "rate3", null, "operator" + envelope + "rate4" },
            new String[] { "operator" + envelope + "level4", "operator" + envelope + "level1", "operator" + envelope + "level2", "operator" + envelope + "level3",  "operator" + envelope + "level3", "operator" + envelope + "level4" },
            new double[] { 0, 1.0/5, 1.0/5, 1.0/5, 1.0/5, 1.0/5 },
            new double[] { 1.0 / 99, 1.0 / 99, 1.0 / 99, 1.0 / 99, 1.0 / 99, 1.0 / 99 },
            new double[] { 0, (Math.PI/4/99), (Math.PI/4/99), (Math.PI/4/99), (Math.PI/4/99), (Math.PI/4/99) })
            {
            public double preprocessXKey(int index, String key, double value)
                {
                return 99.0 - value;
                }
            };

        /*
        // ADSR
        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
        new String[] { null, "operator" + envelope + "rate1", "operator" + envelope + "rate2", "operator" + envelope + "rate3", null, "operator" + envelope + "rate4" },
        new String[] { "operator" + envelope + "level4", "operator" + envelope + "level1", "operator" + envelope + "level2", "operator" + envelope + "level3",  "operator" + envelope + "level3", "operator" + envelope + "level4" },
        new double[] { 0, 0.2/99, 0.2/99, 0.2/99, 0.0, 0.2/99 },
        new double[] { 1.0 / 99, 1.0 / 99, 1.0 / 99, 1.0 / 99, 1.0 / 99, 1.0 / 99 })
        {
        public void postProcess(double[] xVals, double[] yVals)
        {
        // The DX7 uses 99 for SHORT and 0 for LONG, weird
        for(int i = 1; i < 6; i++)
        xVals[i] = 0.2 - xVals[i];
        }
        };
        */
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
    "operator6rate1",
    "operator6rate2",
    "operator6rate3",
    "operator6rate4",
    "operator6level1",
    "operator6level2",
    "operator6level3",
    "operator6level4",
    "operator6keyboardlevelscalingbreakpoint",
    "operator6keyboardlevelscalingleftdepth",
    "operator6keyboardlevelscalingrightdepth",
    "operator6keyboardlevelscalingleftcurve",
    "operator6keyboardlevelscalingrightcurve",
    "operator6keyboardratescaling",
    "operator6amplitudemodulationsensitivity",
    "operator6keyvelocitysensitivity",
    "operator6operatoroutputlevel",
    "operator6oscillatormode",
    "operator6frequencycoarse",
    "operator6frequencyfine",
    "operator6frequencydetune",

    "operator5rate1",
    "operator5rate2",
    "operator5rate3",
    "operator5rate4",
    "operator5level1",
    "operator5level2",
    "operator5level3",
    "operator5level4",
    "operator5keyboardlevelscalingbreakpoint",
    "operator5keyboardlevelscalingleftdepth",
    "operator5keyboardlevelscalingrightdepth",
    "operator5keyboardlevelscalingleftcurve",
    "operator5keyboardlevelscalingrightcurve",
    "operator5keyboardratescaling",
    "operator5amplitudemodulationsensitivity",
    "operator5keyvelocitysensitivity",
    "operator5operatoroutputlevel",
    "operator5oscillatormode",
    "operator5frequencycoarse",
    "operator5frequencyfine",
    "operator5frequencydetune",

    "operator4rate1",
    "operator4rate2",
    "operator4rate3",
    "operator4rate4",
    "operator4level1",
    "operator4level2",
    "operator4level3",
    "operator4level4",
    "operator4keyboardlevelscalingbreakpoint",
    "operator4keyboardlevelscalingleftdepth",
    "operator4keyboardlevelscalingrightdepth",
    "operator4keyboardlevelscalingleftcurve",
    "operator4keyboardlevelscalingrightcurve",
    "operator4keyboardratescaling",
    "operator4amplitudemodulationsensitivity",
    "operator4keyvelocitysensitivity",
    "operator4operatoroutputlevel",
    "operator4oscillatormode",
    "operator4frequencycoarse",
    "operator4frequencyfine",
    "operator4frequencydetune",

    "operator3rate1",
    "operator3rate2",
    "operator3rate3",
    "operator3rate4",
    "operator3level1",
    "operator3level2",
    "operator3level3",
    "operator3level4",
    "operator3keyboardlevelscalingbreakpoint",
    "operator3keyboardlevelscalingleftdepth",
    "operator3keyboardlevelscalingrightdepth",
    "operator3keyboardlevelscalingleftcurve",
    "operator3keyboardlevelscalingrightcurve",
    "operator3keyboardratescaling",
    "operator3amplitudemodulationsensitivity",
    "operator3keyvelocitysensitivity",
    "operator3operatoroutputlevel",
    "operator3oscillatormode",
    "operator3frequencycoarse",
    "operator3frequencyfine",
    "operator3frequencydetune",

    "operator2rate1",
    "operator2rate2",
    "operator2rate3",
    "operator2rate4",
    "operator2level1",
    "operator2level2",
    "operator2level3",
    "operator2level4",
    "operator2keyboardlevelscalingbreakpoint",
    "operator2keyboardlevelscalingleftdepth",
    "operator2keyboardlevelscalingrightdepth",
    "operator2keyboardlevelscalingleftcurve",
    "operator2keyboardlevelscalingrightcurve",
    "operator2keyboardratescaling",
    "operator2amplitudemodulationsensitivity",
    "operator2keyvelocitysensitivity",
    "operator2operatoroutputlevel",
    "operator2oscillatormode",
    "operator2frequencycoarse",
    "operator2frequencyfine",
    "operator2frequencydetune",

    "operator1rate1",
    "operator1rate2",
    "operator1rate3",
    "operator1rate4",
    "operator1level1",
    "operator1level2",
    "operator1level3",
    "operator1level4",
    "operator1keyboardlevelscalingbreakpoint",
    "operator1keyboardlevelscalingleftdepth",
    "operator1keyboardlevelscalingrightdepth",
    "operator1keyboardlevelscalingleftcurve",
    "operator1keyboardlevelscalingrightcurve",
    "operator1keyboardratescaling",
    "operator1amplitudemodulationsensitivity",
    "operator1keyvelocitysensitivity",
    "operator1operatoroutputlevel",
    "operator1oscillatormode",
    "operator1frequencycoarse",
    "operator1frequencyfine",
    "operator1frequencydetune",

    "pitchegrate1",
    "pitchegrate2",
    "pitchegrate3",
    "pitchegrate4",
    "pitcheglevel1",
    "pitcheglevel2",
    "pitcheglevel3",
    "pitcheglevel4",
    
    "algorithm",
    "feedback",
    "oscillatorkeysync",
    "lfospeed",
    "lfodelay",
    "lfopitchmodulationdepth",
    "lfoamplitudemodulationdepth",
    "lfokeysync",
    "lfowave",
    "lfopitchmodulationsensitivity",
    "transpose",

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

    };

    public Object[] emitAll(String key)
        {
        simplePause(50);  // this is needed for TX81Z, but for the DX7?  Dunno
        
        if (key.equals("number")) return new Object[0];  // this is not emittable

        byte channel = (byte)(16 + getChannelOut());
        
        if (key.equals("name"))
            {
            Object[] result = new Object[10];
            
            String name = model.get("name", "INIT VOICE") + "          ";

            for(int i = 0; i < 10; i++)
                {
                byte PP = (byte)i;
                result[i] = new byte[] { (byte)0xF0, (byte)0x43, channel, (byte)0x0, PP, (byte)(name.charAt(i)), (byte)0xF7 };
                }
            return result;
            }
        else if (allParametersToIndex.containsKey(key))
            {
            int index = ((Integer)(allParametersToIndex.get(key))).intValue();
            int value = model.get(key);
            
            byte PP = (byte) (index & 127);
            byte HH = (byte) ((index >> 7) & 1);  // technically & 3...
            byte VV = (byte) value;
            
            byte[] data = new byte[] { (byte)0xF0, (byte)0x43, channel, HH, PP, VV, (byte)0xF7 };
            return new Object[] { data };
            }
        else 
            {
            System.err.println("Warning (YamahaDX7): Can't emit key " + key);
            return new Object[0];
            }
        }




    public int parse(byte[] data, boolean fromFile)
        {
        if (data[3] == 0)  // 1 single
            {
            // yay for DX7 simplicity
            for(int i = 0; i < allParameters.length - 10; i++)
                {
                model.set(allParameters[i], data[i + 6]);
                }
                
            char[] name = new char[10];
            for(int i = 0; i < 10; i ++)
                {
                name[i] = (char)(data[allParameters.length - 10 + i + 6] & 127);
                }
            model.set("name", new String(name));
                
            revise();
            return PARSE_SUCCEEDED;
            }
        else                        // bulk
            {
            // extract names
            char[][] names = new char[32][10];
            for(int i = 0; i < 32; i++)
                {
                for (int j = 0; j < 10; j++)
                    {
                    names[i][j] = (char)(data[6 + (i * 128) + 118 + j] & 127);
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
            if (patchNum < 0) return PARSE_CANCELLED;
                
            // okay, we're loading and editing patch number patchNum.  Here we go.
            int patch = patchNum * 128;
            int pos = 0;
                                                        
            for(int op = 0; op < 6; op++)
                {
                // operatorNrate1 ... operatorNkeyboardlevelscalingrightdepth
                for(int i = 0; i < 11; i++)
                    {
                    model.set(allParameters[pos++], data[patch + op * 17 + i + 6]);
                    }
                                        
                // scaling left curve
                model.set(allParameters[pos++], data[patch + op * 17 + 11 + 6] & 3);
                // scaling right curve
                model.set(allParameters[pos++], (data[patch + op * 17 + 11 + 6] >>> 2) & 3);
                                
                // rate scaling
                model.set(allParameters[pos++], data[patch + op * 17 + 12 + 6] & 7);
                                
                // amp mod sensitivity
                model.set(allParameters[pos++], data[patch + op * 17 + 13 + 6] & 3);
                // key velocity
                model.set(allParameters[pos++], (data[patch + op * 17 + 13 + 6] >>> 2) & 7);

                // output level
                model.set(allParameters[pos++], data[patch + op * 17 + 14 + 6]);
                                        
                // osc mode
                model.set(allParameters[pos++], data[patch + op * 17 + 15 + 6] & 1);
                // freq coarse
                model.set(allParameters[pos++], (data[patch + op * 17 + 15 + 6] >>> 1) & 31);
                // freq fine
                model.set(allParameters[pos++], data[patch + op * 17 + 16 + 6]);
                // detune  [note this one is out of position, why Yamaha why?]
                model.set(allParameters[pos++], (data[patch + op * 17 + 12 + 6] >>> 3) & 15);
                }
                        
            // pitchegrate1 ... pitcheglevel4
            for(int i = 102; i < 110; i++)
                {
                model.set(allParameters[pos++], data[patch + i + 6]);
                }
                                
            // algorithm select
            model.set(allParameters[pos++], data[patch + 110 + 6] & 31);
            // feedback
            model.set(allParameters[pos++], data[patch + 111 + 6] & 7);
            // osc key sync
            model.set(allParameters[pos++], (data[patch + 111 + 6] >>> 3) & 1);

            // lfospeed ... lfoamplitudemodulationdepth
            for(int i = 112; i < 116; i++)
                {
                model.set(allParameters[pos++], data[patch + i + 6]);
                }
                                
            // key sync
            model.set(allParameters[pos++], data[patch + 116 + 6] & 1);
            // wave
            model.set(allParameters[pos++], (data[patch + 116 + 6] >>> 1) & 7);
            // lfo pitch mod sens
            model.set(allParameters[pos++], (data[patch + 116 + 6] >>> 4) & 7);
            // transpose
            model.set(allParameters[pos++], (data[patch + 117 + 6]) & 63);
                                
            model.set("name", new String(names[patchNum]));
                
            model.set("number", patchNum);

            revise();
            return PARSE_SUCCEEDED_UNTITLED;
            }
        }
 
    
    public byte[] emit(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        simplePause(50);                // dunno if I need it, it was needed for the TX81Z
        
        byte[] data = new byte[163];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x43;
        data[2] = (byte)(getChannelOut());
        data[3] = (byte)0x00;
        data[4] = (byte)0x01;
        data[5] = (byte)0x1B;
                                
        // yay for DX7 simplicity
        for(int i = 0; i < allParameters.length - 10; i++)
            {
            data[i + 6] = (byte)(model.get(allParameters[i], 0));
            }

        for(int i = 0; i < 10; i ++)
            {
            data[allParameters.length - 10 + i + 6] = (byte)((model.get("name", "          ") + "          ").charAt(i));
            }
                
        data[161] = produceChecksum(data, 6);
        data[162] = (byte)0xF7;
        
        return data;
        }

    byte produceChecksum(byte[] bytes, int start)
        {
        //      The TX816 owner's manual (p. 54) says the checksum is:
        //                              "the least significant 7 bits of the 2's complement sum of 155 data bytes.
        //                               0eeeeeee must be determined so that the least significant 7 bits of the
        //                              sum of the 155 data bytes and checksum equal zero."

        int checksum = 0;
        for(int i = start; i < bytes.length; i++)
            checksum = (checksum + bytes[i]) & 127;
        return (byte)((128 - checksum) & 127);
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
        byte channel = (byte)(32 + getChannelOut());
        return new byte[] { (byte)0xF0, (byte)0x43, channel, 0x00, (byte)0xF7 };
        }

    public static boolean recognize(byte[] data)
        {
        return (
            // 1 single
                
                (data.length == 163 &&
                data[0] == (byte)0xF0 &&
                data[1] == (byte)0x43 &&
                // don't care about 2, it's the channel
                data[3] == (byte)0x00 &&
                data[4] == (byte)0x01 &&
                data[5] == (byte)0x1B) 
                
            || recognizeBulk(data));
            
        }

    public static boolean recognizeBulk(byte[] data)
        {
        return  (
            // 32 bulk
            
            data.length == 4104 &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x43 &&
            // don't care about 2, it's the channel
            data[3] == (byte)0x09 &&
            //data[4] == (byte)0x20 &&          // sometimes this is 0x10 by mistake
            data[5] == (byte)0x00);
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
        
    public static String getSynthName() { return "Yamaha DX7"; }

    public void changePatch(Model tempModel) 
        {
        tryToSendMIDI(buildPC(getChannelOut(), tempModel.get("number")));

        // we assume that we successfully did it
        if (!isMerging())  // we're actually loading the patch, not merging with it
            {
            setSendMIDI(false);
            model.set("number", tempModel.get("number"));
            setSendMIDI(true);
            }
        }
    
    public String getPatchName(Model model) { return model.get("name", "INIT VOICE"); }

    public Model getNextPatchLocation(Model model)
        {
        int number = model.get("number");
        
        number++;
        if (number >= 32)
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
        
        int number = model.get("number") + 1;
        return "" + (number > 9 ? "" : "0") + number;
        }
        

    }
