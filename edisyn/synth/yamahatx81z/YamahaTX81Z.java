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
        for(int i = 0; i < allParameters.length; i++)
            {
            allParametersToIndex.put(allParameters[i], Integer.valueOf(i));
            }
                
        for(int i = 0; i < allAdditionalParameters.length; i++)
            {
            allAdditionalParametersToIndex.put(allAdditionalParameters[i], Integer.valueOf(i));
            }
                
        /// SOUND PANEL
                
        JComponent soundPanel = new SynthPanel();
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL));
        hbox.addLast(addGlobal(Style.COLOR_A));
        vbox.add(hbox);
        
        vbox.add(addLFO(Style.COLOR_B));
        
        vbox.add(addModulation(Style.COLOR_C));
        
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Global", soundPanel);
                

        JComponent sourcePanel = new SynthPanel();
        vbox = new VBox();
        
        vbox.add(addOperator(1, Style.COLOR_A));
        vbox.add(addEnvelope(1, Style.COLOR_B));

        vbox.add(addOperator(2, Style.COLOR_A));
        vbox.add(addEnvelope(2, Style.COLOR_B));

        sourcePanel.add(vbox, BorderLayout.CENTER);
        addTab("Operators 1-2", sourcePanel);

        sourcePanel = new SynthPanel();
        vbox = new VBox();
        
        vbox.add(addOperator(3, Style.COLOR_A));
        vbox.add(addEnvelope(3, Style.COLOR_B));

        vbox.add(addOperator(4, Style.COLOR_A));
        vbox.add(addEnvelope(4, Style.COLOR_B));

        sourcePanel.add(vbox, BorderLayout.CENTER);
        addTab("Operators 3-4", sourcePanel);
        
        model.set("name", "INIT SOUND");
        
        loadDefaults();        
        }
                
    public JFrame sprout()
        {
        JFrame frame = super.sprout();
        transmitTo.setEnabled(false);
        writeTo.setEnabled(false);
        return frame;
        }         

    public String getDefaultResourceFileName() { return "YamahaTX81Z.init"; }
	public String getHTMLResourceFileName() { return "YamahaTX81Z.html"; }

    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        JComboBox bank = new JComboBox(BANKS);
        bank.setSelectedIndex(model.get("bank", 0));
        if (writing)
            {
            bank = new JComboBox(new String[] { "I" });
            bank.setSelectedIndex(0);
            }
                        
                        
        
        JTextField number = new JTextField("" + (model.get("number", 0) + 1), 3);

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

    /** Add the global patch category (name, id, number, etc.) */
    public JComponent addNameGlobal(Color color)
        {
        Category globalCategory = new Category(this, getSynthName(), color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
                
        VBox vbox = new VBox();
        HBox hbox2 = new HBox();
        comp = new PatchDisplay(this, "Patch", "bank", "number", 4)
            {
            public String numberString(int number) { number += 1; return (number > 9 ? "0" : "00") + number; }
            public String bankString(int bank) { return BANKS[bank]; }
            };
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

        comp = new CheckBox("Chorus", this, "chorus");
        ((CheckBox)comp).addToWidth(1);
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
        
        comp = new LabelledDial("Amplitude Mod", this, "lfoamplitudemodulationsensitivity", color, 0, 7);
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
        comp = new CheckBox("Sustain", this, "sustain");
        vbox.add(comp);
        comp = new CheckBox("Portamento", this, "portamento");
        vbox.add(comp);
        comp = new CheckBox("Full Time Portamento", this, "fulltimeportamentomode");
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
        
        comp = new LabelledDial("Breath Ctrl.", this, "breathcontrolenvelopbias", color, 0, 99);
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
	  double c = FREQUENCY_RATIOS[coarse];
	  if (coarse >= 8)                // ratio >= 2.0
		return c + (1 / 16.0) * fine;
	  else if (coarse >= 4)           // ratio >= 1.0
		return c + (c / 16.0) * fine;
	  else
		return Math.min(c + (c / 8.0) * fine, 
						c + (c / 8.0) * 7.0);
	  }
    
        
    // based on http://cd.textfiles.com/fredfish/v1.6/FF_Disks/571-600/FF_598/TX81z/TX81z.doc
    // in combination with the manual.
    public int computeCoarseFrequency(int range, int coarse)
        {
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
        
        // I think you can't go over 255
        if (base > 255)
            base = 255;
        
        // the others are just multiples of it
        for(int i = 0; i < range; i++)
            base *= 2;
        
        return base;
        }

    // based on http://cd.textfiles.com/fredfish/v1.6/FF_Disks/571-600/FF_598/TX81z/TX81z.doc
    // in combination with the manual.
    public int computeFrequency(int range, int coarse, int fine)
        {
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
        }


    LabelledDial[] frequencyRanges = new LabelledDial[4];
    JLabel[] coarseFrequencyLabels = new JLabel[4];
    JLabel[] fineFrequencyLabels = new JLabel[4];
	java.text.DecimalFormat format = new java.text.DecimalFormat("0.0#");

    public JComponent addOperator(final int src, Color color)
        {
        final Category category = new Category(this, "Operator " + src, color);

        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = WAVES;
        comp = new Chooser("Wave", this, "operator" + src + "operatorwaveform", params, WAVE_ICONS);
        vbox.add(comp);

        HBox hbox2  = new HBox();
        comp = new CheckBox("Enabled", this, "operator" + src + "enabled");
        hbox2.add(comp);

        comp = new CheckBox("AM", this, "operator" + src + "amplitudemodulationenable");
        hbox2.add(comp);
        vbox.add(hbox2);
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


        comp = new LabelledDial("Frequency ", this, "operator" + src + "frequencycoarse", color, 0, 63)  // extra space because OS X cuts off the 'y'
            {
            public String map(int val)
                {
                if (model.get("operator" + src + "fix", 0) == 0)  // not fixed
                    {
                    return "" + FREQUENCY_RATIOS[val];
                    }
                else
                    {
                    return "" + computeCoarseFrequency(
                        model.get("operator" + src + "fixedfrequencyrange", 0),
                        model.get("operator" + src + "frequencycoarse", 0));
                    }
                }               
            };
        coarseFrequencyLabels[src - 1 ] = ((LabelledDial)comp).addAdditionalLabel("Fixed");
        model.register("operator" + src + "fixedfrequencyrange", (Updatable)comp);
        model.register("operator" + src + "fix", (Updatable)comp);              
        hbox.add(comp);
        
        comp = new LabelledDial("Frequency ", this, "operator" + src + "frequencyfine", color, 0, 15)  // extra space because OS X cuts off the 'y'
            {
            public String map(int val)
                {
                if (model.get("operator" + src + "fix", 0) == 0)  // not fixed
                    {
                    return format.format(computeFineRatio(
                    	model.get("operator" + src + "frequencycoarse", 0), 
                    	model.get("operator" + src + "frequencyfine", 0)));
                    }
                else
                    {
                    return "" + computeFrequency(
                        model.get("operator" + src + "fixedfrequencyrange", 0),
                        model.get("operator" + src + "frequencycoarse", 0),
                        model.get("operator" + src + "frequencyfine", 0));
                    }
                }               
            };
        fineFrequencyLabels[src - 1] = ((LabelledDial)comp).addAdditionalLabel("Fine");
        model.register("operator" + src + "fixedfrequencyrange", (Updatable)comp);
        model.register("operator" + src + "frequencycoarse", (Updatable)comp);
        model.register("operator" + src + "fix", (Updatable)comp);
        hbox.add(comp);

        frequencyRanges[src - 1] = new LabelledDial("Frequency", this, "operator" + src + "fixedfrequencyrange", color, 0, 7)
            {
            public String map(int val)
                {
                return FIX_RANGES[val];
                }               
            };
        frequencyRanges[src - 1].addAdditionalLabel("Range");
        hbox.add(frequencyRanges[src - 1]);

    
        // we put this last so that by the time it's updating, the fine frequency dials have been built
    
        comp = new CheckBox("Fixed Frequency", this, "operator" + src + "fix")
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                if (model.get(key, 0) == 0)
                    {
                    hbox.remove(frequencyRanges[src - 1]);
                    coarseFrequencyLabels[src - 1].setText("Ratio");
                    fineFrequencyLabels[src - 1].setText("Ratio Fine");
                    }
                else
                    {
                    hbox.add(frequencyRanges[src - 1]);
                    coarseFrequencyLabels[src - 1].setText("Fixed");
                    fineFrequencyLabels[src - 1].setText("Fixed Fine");
                    }
                hbox.revalidate();
                }
            };
        ((CheckBox)comp).addToWidth(1);
        vbox.add(comp);

    
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
                
    public JComponent addEnvelope(int envelope, Color color)
        {
        Category category = new Category(this, "Operator Envelope " + envelope, color);

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

        comp = new LabelledDial("Release", this, "operator" + envelope + "releaserate", color, 0, 14, -1);
        ((LabelledDial)comp).addAdditionalLabel("Rate");
        hbox.add(comp);
        
        comp = new LabelledDial("Level", this, "operator" + envelope + "levelscaling", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("Scaling");
        hbox.add(comp);
        
        comp = new LabelledDial("Rate", this, "operator" + envelope + "ratescaling", color, 0, 3);
        ((LabelledDial)comp).addAdditionalLabel("Scaling");
        hbox.add(comp);
    
        // TODO: Modify envelope display to show shift
        comp = new LabelledDial("Shift", this, "operator" + envelope + "shift", color, 0, 3)
            {
            public String map(int val)
                {
                return SHIFTS[val];
                }
            };
        hbox.add(comp);
    

        // ADSR
        comp = new EnvelopeDisplay(this, Color.red, 
            new String[] { null, "operator" + envelope + "attackrate", "operator" + envelope + "decay1rate", "operator" + envelope + "decay2rate", "operator" + envelope + "releaserate" },
            new String[] { null, null, "operator" + envelope + "decay1level", "operator" + envelope + "decay1level", null },
            new double[] { 0, 0.25/31.0, 0.25/ 31.0,  0.25/31.0, 0.25/15.0 },
            new double[] { 0, 1.0,                1.0 / 15.0,    1.0 / 30.0, 0 })
            {
            public void postProcess(double[] xVals, double[] yVals)
                {
                if (model.get("operator" + envelope + "decay2rate", 0) == 0)
                    {
                    yVals[3] = yVals[2];
                    }

                xVals[1] = 0.25 - xVals[1];
                xVals[2] = 0.25 - xVals[2];
                xVals[3] = 0.25 - xVals[3];
                xVals[4] = 0.25 - xVals[4];
                                        
                int shift = model.get("operator" + envelope + "shift", 0);
                                        
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
    
    "algorithm",
    "feedback",         
    "lfospeed",
    "lfodelay",
    "lfopitchmodulationdepth",
    "lfoamplitudemodulationdepth",        
    "lfosync",        
    "lfodelay",
    "lfopitchmodulationsensitivity",
    "lfoamplitudemodulationsensitivity",    
    "transpose",
    
    "mono",
    "pitchbendrange",  
    "fulltimeportamentomode",
    "portamentotime",
    "footcontrolvolume",
    "sustain",
    "portamento",    
    "chorus",        
    "modulationwheelpitch",
    "modulationwheelamplitude",
    "breathcontrolpitch",
    "breathcontrolamplitude",          
    "breathcontrolpitchbias",
    "breathcontrolenvelopbias",
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
    "operatorenabled"                   /// * 
    };



    /** Map of parameter -> index in the allParameters array. */
    HashMap allAdditionalParametersToIndex = new HashMap();


    /** List of all Sysex parameters in order.  "-" is a reserved (unused and thus unnamed) parameter. */

    /// * indicates parameters which must be handled specially due to packing
    /// that Waldorf decided to do.  :-(

    final static String[] allAdditionalParameters = new String[] 
    {
    "operator1fix",
    "operator1fixedfrequencyrange",
    "operator1frequencyfine",
    "operator1operatorwaveform",
    "operator1shift",

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

    "operator4fix",
    "operator4fixedfrequencyrange",
    "operator4frequencyfine",
    "operator4operatorwaveform",
    "operator4shift",
    
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

    public byte[] emit(String key)
        {
        if (key.equals("bank")) return new byte[0];  // this is not emittable
        if (key.equals("number")) return new byte[0];  // this is not emittable

        byte channel = (byte)(32 + getChannelOut() - 1);
             
         
        if (key.equals("name"))
            {
            byte[] result = new byte[10 * 7];
            
            String name = model.get("name", "INIT SOUND") + "          ";

            for(int i = 0; i < 10; i++)
                {
                byte[] b = new byte[] { (byte)0xF0, 0x43, channel, VCED_GROUP, (byte)(77 + i), (byte)(name.charAt(i)), (byte)0xF7 };
                System.arraycopy(b, 0, result, i * 7, 7);
                }
            return result;
            }

        else if (key.equals("operator1enabled") || key.equals("operator2enabled") || key.equals("operator3enabled") || key.equals("operator4enabled"))
            {
        	int v1 = model.get("operator1enabled", 0);
        	int v2 = model.get("operator2enabled", 0);
        	int v3 = model.get("operator3enabled", 0);
        	int v4 = model.get("operator4enabled", 0);
                        
            byte PP = (byte) 93;
            // don't know if I got this in the right order, see parse()
            byte VV = (byte) ((v4 << 3) | (v3 << 2) | (v2 << 1) | v1);
            return new byte[] { (byte)0xF0, 0x43, channel, VCED_GROUP, PP, VV, (byte)0xF7 };
            }
        else if (allParametersToIndex.containsKey(key))
            {
            int index = ((Integer)(allParametersToIndex.get(key))).intValue();
            int value = model.get(key, 0);

            byte PP = (byte) index;
            byte VV = (byte) value;
            return new byte[] { (byte)0xF0, 0x43, channel, VCED_GROUP, PP, VV, (byte)0xF7 };
            }
        else if (allAdditionalParametersToIndex.containsKey(key))
            {
            int index = ((Integer)(allAdditionalParametersToIndex.get(key))).intValue();
            int value = model.get(key, 0);

            byte PP = (byte) index;
            byte VV = (byte) value;
            return new byte[] { (byte)0xF0, 0x43, channel, ACED_GROUP, PP, VV, (byte)0xF7 };
            }
        else 
            {
            System.err.println("Can't emit key " + key);
            return new byte[0];
            }
        }
    

    public boolean parse(byte[] data, boolean ignorePatch, boolean fromFile)
        {
        if (data.length == 41 + 101) // probably VCED + ACED, break up and call recursively
            {
            if (data[3] == 0x03)  // VCED?
                {
                byte[] d = new byte[101];
                System.arraycopy(data, 0, d, 0, 101);
                boolean result = parse(d, ignorePatch, fromFile);
                if (!result)
                    return false;
                d = new byte[41];
                System.arraycopy(data, 101, d, 0, 41); 
                result = parse(d, ignorePatch, fromFile);
                if (!result)
                    return false;
                return true;
                }
            else  // VCED?
                {
                byte[] d = new byte[41];
                System.arraycopy(data, 101, d, 0, 41); 
                boolean result = parse(d, ignorePatch, fromFile);
                if (!result)
                    return false;
                d = new byte[101];
                System.arraycopy(data, 0, d, 0, 101);
                result = parse(d, ignorePatch, fromFile);
                if (!result)
                    return false;
                return true;
                }
            }
        
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
            System.err.println("Can't parse data (length " + data.length + ").  First bytes:");
            int len = 16;
            if (len < data.length) len = data.length;
            for(int i = 0; i < len; i++)
                System.err.print(" " + Integer.toHexString(i));
            System.err.println();
            return false;
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
            // don't know if I got this in the right order, see emit()
            else if (params[i].equals("operatorenabled"))
                {
                model.set("operator" + 1 + "enabled", val & 1);
                model.set("operator" + 2 + "enabled", (val >> 1) & 1);
                model.set("operator" + 3 + "enabled", (val >> 2) & 1);
                model.set("operator" + 4 + "enabled", (val >> 3) & 1);
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
        return true;            // change this as appropriate
        }
    
    public int getPauseBetweenMIDISends()
        {
        // Wikipedia says that you have to have a 50ms wait-time between
        // sysex transmissions or the TX81Z has problems.  Maybe this might work?
        // Too crude?  Causes problems with real-time manipulation?

        return 50;
        }
            
    
    public Object[] emitAll(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
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
        
        for(int i = 0; i < allAdditionalParameters.length; i++)  // no name, no operatorenabled
            {
            data[i + 10] = (byte)(model.get(allAdditionalParameters[i], 0));
            }

        result[0][0] = (byte)0xF0;
        result[0][1] = 0x43;
        result[0][2] = (byte)(32 + getChannelOut() - 1);
        result[0][3] = (byte)0x7E;
        result[0][4] = 0x00;
        result[0][5] = 0x21;
        System.arraycopy(data, 0, result[0], 6, data.length);
        result[0][6 + data.length] = produceChecksum(data);
        result[0][7 + data.length] = (byte)0xF7;


        // Next the VCED
        result[1] = new byte[102];
        data = new byte[94];
        for(int i = 0; i < allParameters.length - 18; i++)  // no name, no operatorenabled
            {
            data[i] = (byte)(model.get(allParameters[i], 0));
            }
        
        String name = model.get("name", "INIT SOUND") + "          ";
                
        for(int i = 0; i < 10; i++)
            {
            data[allParameters.length - 18 + i] = (byte)(name.charAt(i));
            }
        
        int v1 = model.get("operator1enabled", 0);
        int v2 = model.get("operator2enabled", 0);
        int v3 = model.get("operator3enabled", 0);
        int v4 = model.get("operator4enabled", 0);
        
        byte VV = (byte) ((v4 << 3) | (v3 << 2) | (v2 << 1) | v1);
        
        data[allParameters.length - 1] = VV;
        
        result[1][0] = (byte)0xF0;
        result[1][1] = 0x43;
        result[1][2] = (byte)(32 + getChannelOut() - 1);
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


    public byte[] requestCurrentDump()
        {
        // ACED + VCED
        byte channel = (byte)(32 + getChannelOut() - 1);
        return new byte[] { (byte)0xF0, 0x43, channel, 0x7E, 
            (byte)'L', (byte)'M', (byte)' ', (byte)' ',
            (byte)'8', (byte)'9', (byte)'7', (byte)'6',
            (byte)'A', (byte)'E', (byte)0xF7 }; 
        }

    public static boolean recognize(byte[] data)
        {
        // VCED
        if (data.length == 101 &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x43 &&
            // don't care about 2, it's the channel
            data[3] == (byte)0x03 &&
            data[4] == (byte)0x00 &&
            data[4] == (byte)0x5D)
            return true;

        // *Probably* VCED + ACED
        if (data.length == 101 + 41 &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x43 &&
            // don't care about 2, it's the channel
            data[3] == (byte)0x03 &&
            data[4] == (byte)0x00 &&
            data[4] == (byte)0x5D)
            return true;
                
        // ACED
        else if (data.length == 41 &&        
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
        
        else return false;
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
        
    public boolean requestCloseWindow() { return true; }

    public static String getSynthName() { return "Yamaha TX81Z"; }

    public void changePatch(Model tempModel) 
        {
        int bank = tempModel.get("bank", 0);
        int number = tempModel.get("number", 0);
        int val = bank * 32 + number;
        byte lo = (byte)(val & 127);
        byte hi = (byte)(val >> 7);

        // Change program change table position 0 to what we want first
        byte[] table = new byte[9];
        table[0] = (byte)0xF0;
        table[1] = (byte)0x43;
        table[2] = (byte)(32 + getChannelOut() - 1);
        table[3] = (byte)0x10;
        table[4] = (byte)127;  // really!
        table[5] = (byte)127;  // we're changing table position 127
        table[6] = hi;
        table[7] = lo;
        table[8] = (byte)0xF7;
        
        tryToSendSysex(table);
        
        // Now let's do the program change to program 127
        
        tryToSendMIDI(buildPC(getChannelOut() - 1, 127));
        }
    
    public String getPatchName() { return model.get("name", "INIT SOUND"); }
    }
