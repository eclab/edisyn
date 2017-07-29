/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.oberheimmatrix1000;

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
   A patch editor for the Oberheim Matrix 1000.
        
   @author Sean Luke
*/

public class OberheimMatrix1000 extends Synth
    {
    /// Various collections of parameter names for pop-up menus
        
    public static final String[] KEYBOARD_MODES = new String[] { "Reassign", "Rotate", "Unison", "Reassign w/Rob" };
    public static final String[] SYNC = new String[] { "Off", "Soft", "Medium", "Hard" };
    // Note actually there are 4 lag modes, the fourth one is also exponential!                                                                                                                                                     
    public static final String[] PORTAMENTO_MODES = new String[] { "Constant Speed", "Constant Time", "Exponential" }; 
    public static final String[] LFO_SHAPES = new String[] { "Triangle", "Up Saw", "Down Saw", "Square", "Random", "Noise", "S&H" }; 
    public static final String[] LFO_TRIGGERS = new String[] { "None", "Single", "Multi", "External" }; 
    public static final String[] MODULATION_SOURCES = new String[] { "None", "Env 1", "Env 2", "Env 3", "LFO 1", "LFO 2", "Vibrato", "Ramp 1", "Ramp 2", "Keyboard", "Portamento", "Tracking Generator", "Keyboard Gate", "Velocity", "Release Velocity", "Pressure", "Pedal 1", "Pedal 2", "Lever 1", "Lever 2", "Lever 3" };
    public static final String[] TRACKING_GENERATOR_SOURCES = new String[] { "Env 1", "Env 2", "Env 3", "LFO 1", "LFO 2", "Vibrato", "Ramp 1", "Ramp 2", "Keyboard", "Portamento", "Tracking Generator", "Keyboard Gate", "Velocity", "Release Velocity", "Pressure", "Pedal 1", "Pedal 2", "Lever 1", "Lever 2", "Lever 3" };
    public static final String[] ENV_TRIGGER_MODES = new String[] { "Single", "Single Reset", "Multi", "Multi Reset", "External Single", "External Single Reset", "External Multi", "External Multi Reset" }; 
    public static final String[] ENV_MODES = new String[] { "Normal", "DADR", "Free Run", "DADR + Free Run" }; 
    // There are actually 2 bits here, so we're missing one
    public static final String[] ENV_LFO_TRIGGER_MODES = new String[] { "Normal", "LFO 1", "Gated LFO 1" }; 
    public static final String[] RAMP_TRIGGER_MODES = new String[] { "Single", "Multi", "External", "External Gated" };
    // VCA2 is not mentioned anywhere else
    // I may need to say VCA rather than Amplifier elsewhere
    public static final String[] MODULATION_DESTINATIONS = new String[] { "DCO 1 Frequency", "DCO 1 Pulsewidth", "DCO 1 Wave Shape",  "DCO 2 Frequency", "DCO 2 Pulsewidth", "DCO 2 Wave Shape", "Mix Level", "Filter FM", "Filter Frequency", "Filter Resonance", "VCA 1 Level", "VCA 2 Level", "Env 1 Delay", "Env 1 Attack", "Env 1 Decay", "Env 1 Release", "Env 1 Amplitude", "Env 2 Delay", "Env 2 Attack", "Env 2 Decay", "Env 2 Release", "Env 2 Amplitude", "Env 3 Delay", "Env 3 Attack", "Env 3 Decay", "Env 3 Release", "Env 3 Amplitude", "LFO 1 Speed", "LFO 1 Amplitude", "LFO 2 Speed", "LFO 2 Amplitude", "Portamento Time" };

    public OberheimMatrix1000()
        {
        for(int i = 0; i < allParameters.length; i++)
            {
            allParametersToIndex.put(allParameters[i], Integer.valueOf(i));
            }

        for(int i = 0; i < allMatrixParameters.length; i++)
            {
            allMatrixParametersToIndex.put(allMatrixParameters[i], Integer.valueOf(i));
            }
                
        setSendsAllParametersInBulk(true);
        
        /// SOUND PANEL
                
        JComponent soundPanel = new SynthPanel();
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL));
        hbox.addLast(addOscillatorGlobal(Style.COLOR_A));
        vbox.add(hbox);
        
        vbox.add(addOscillator(1, Style.COLOR_A));
        vbox.add(addOscillator(2, Style.COLOR_A));
        
        hbox = new HBox();
        hbox.add(addFilter(Style.COLOR_C));
        hbox.addLast(addAmplifier(Style.COLOR_C));
        vbox.add(hbox);
        

        soundPanel.add(vbox, BorderLayout.CENTER);
        tabs.addTab("Oscillators and Filters", soundPanel);
                
                
        // ENVELOPE PANEL
                
        JComponent envelopePanel = new SynthPanel();
        vbox = new VBox();

        hbox = new HBox();
        hbox.add(addLFO(1, Style.COLOR_A));
        hbox.addLast(addRamp(1, Style.COLOR_A));
        vbox.add(hbox);
                
        hbox = new HBox();
        hbox.add(addLFO(2, Style.COLOR_A));
        hbox.addLast(addRamp(2, Style.COLOR_A));
        vbox.add(hbox);
                        
        vbox.add(addEnvelope(1,Style.COLOR_B));
        vbox.add(addEnvelope(2,Style.COLOR_B));
        vbox.add(addEnvelope(3,Style.COLOR_B));
        
        envelopePanel.add(vbox, BorderLayout.CENTER);
        tabs.addTab("LFOs and Envelopes", envelopePanel);

        
        
        // MODULATION PANEL
                
        JComponent modulationPanel = new SynthPanel();
        
        vbox = new VBox();
        vbox.add(addModulation(Style.COLOR_A));
        vbox.add(addTracking(Style.COLOR_B));
                

        modulationPanel.add(vbox, BorderLayout.CENTER);
        tabs.addTab("Modulation", modulationPanel);

        tabs.addTab("About", new HTMLBrowser(this.getClass().getResourceAsStream("OberheimMatrix1000.html")));

        model.set("name", "UNTITLED");
        
        loadDefaults();        
        }
                
    public String getDefaultResourceFileName() { return "OberheimMatrix1000.init"; }

    public boolean gatherInfo(String title, Model change, boolean writing)
        {
        JTextField bank = new JTextField("" + (model.get("bank", 0)), 3);
        JTextField number = new JTextField("" + (model.get("number", 0)), 3);

        while(true)
            {
            boolean result = doMultiOption(this, new String[] { "Bank", "Patch Number"}, 
                new JComponent[] { bank, number }, title, "Enter the Bank and Patch number");
                
            if (result == false) 
                return false;
                                
            int n;
            try { n = Integer.parseInt(number.getText()); }
            catch (NumberFormatException e)
                {
                JOptionPane.showMessageDialog(null, "The Patch Number must be an integer 0...99", title, JOptionPane.ERROR_MESSAGE);
                continue;
                }
            if (n < 0 || n > 99)
                {
                JOptionPane.showMessageDialog(null, "The Patch Number must be an integer 0..99", title, JOptionPane.ERROR_MESSAGE);
                continue;
                }
                                
            int i;
            try { i = Integer.parseInt(bank.getText()); }
            catch (NumberFormatException e)
                {
                JOptionPane.showMessageDialog(null, "The Bank must be an integer 0 ... 9", title, JOptionPane.ERROR_MESSAGE);
                continue;
                }
            if (i < 0 || i > 9)
                {
                JOptionPane.showMessageDialog(null, "The Bank must be an integer 0 ... 9", title, JOptionPane.ERROR_MESSAGE);
                continue;
                }
                        
            change.set("bank", i);
            change.set("number", n);
                        
            return true;
            }
        }
        
    public JFrame sprout()
        {
        JFrame frame = super.sprout();
        receiveCurrent.setEnabled(false);		// can't receive current patch
        return frame;
        }         

    // I believe (hope?) that Matrix 6/6R names are probably the char values 32...95,
    // which represent all-caps letters, all numbers, and most punctuation and space.
    // The claim here http://www.youngmonkey.ca/nose/audio_tech/synth/Oberheim-Matrix6R.html
    // is that names are stored with the "lower 6 bits of their ASCII representation".
    // If the bytes start at 32, then this makes sense:  bytes 32...63 are stored as-is,
    // and bytes 64..95 get 64 subtracted from them, so they become 0...31.  Clever.

	byte packNameByte(byte n)
		{
		if (n < 32 || n > 95)
			n = (byte)32;
		n = (byte)(n & 63);
		return n;
		}
		
	byte unpackNameByte(byte n)
		{
		n = (byte)(n & 63);
		if (n < 32)
			n = (byte)(n + 64);
		return n;
		}

    /** Add the global patch category (name, id, number, etc.) */
    public JComponent addNameGlobal(Color color)
        {
        Category globalCategory = new Category(getSynthName(), color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
                                
        VBox vbox = new VBox();
        comp = new PatchDisplay(this, "Patch", "bank", "number", 4)
            {
            public String numberString(int number) { number += 1; return ( number > 99 ? "" : (number > 9 ? "0" : "00")) + number; }
            public String bankString(int bank) { return "" + bank; }
            };
        vbox.add(comp);
        
        comp = new StringComponent("Patch Name", this, "name", 8, "Name must be up to 8 letters, numbers, spaces, or !\"#$%&'()*+,-./:;<=>?[\\]^_.")
            {
            public String replace(String val)
            	{
            	return reviseName(val);
            	}
                                
            public void update(String key, Model model)
                {
                super.update(key, model);
                updateTitle();
                }
            };
        model.setImmutable("name", true);
        vbox.add(comp);
        hbox.add(vbox);

        hbox.addLast(Strut.makeHorizontalStrut(100));

        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }

    public JComponent addOscillatorGlobal(Color color)
        {
        Category category = new Category("Keyboard", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = KEYBOARD_MODES;
        comp = new Chooser("Keyboard Mode", this, "keyboardmode", params);
        vbox.add(comp);

        // Maybe this should go with the LFOs?
        params = PORTAMENTO_MODES;
        comp = new Chooser("Portamento Mode", this, "portamentomode", params);
        vbox.add(comp);
        hbox.add(vbox);

        vbox = new VBox();
        comp = new CheckBox("Legato Portamento", this, "portamentolegato");
        vbox.add(comp);
       
        comp = new LabelledDial("Mix", this, "mix", color, 0, 62, 31)
            {
            public boolean isSymmetric() { return true; }
            };
        ((LabelledDial)comp).setSecondLabel("DCO 1 <> 2");
        hbox.add(comp);

        comp = new LabelledDial("Portamento", this, "portamento", color, 0, 63);
        ((LabelledDial)comp).setSecondLabel("Rate");
        hbox.add(comp);

        comp = new LabelledDial("Portamento", this, "portamentomod", color, 0, 127, 64);
        ((LabelledDial)comp).setSecondLabel("Vel Mod");
        hbox.add(comp);
                

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
                
    /** Add an Oscillator category */
    public JComponent addOscillator(int osc, Color color)
        {
        Category category = new Category("Oscillator " + osc, color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
               
        VBox vbox = new VBox();
        comp = new CheckBox("Lever 1", this, "dco" + osc + "lever1");
        vbox.add(comp);
       
        comp = new CheckBox("Vibrato", this, "dco" + osc + "vibrato");
        vbox.add(comp);
              
        comp = new CheckBox("Portamento   ", this, "dco" + osc + "portamento");
        vbox.add(comp);

        vbox.add(comp);
        if (osc==2)
            {
            comp = new CheckBox("Key Tracking", this, "dco" + osc + "keytracking");
            vbox.add(comp);
            }
        hbox.add(vbox);

        vbox = new VBox();

        comp = new CheckBox("Pulse  ", this, "dco" + osc + "pulse");  // add some spaces so this is longer than Wave
        vbox.add(comp);

        comp = new CheckBox("Wave", this, "dco" + osc + "wave");
        vbox.add(comp);
       
        if (osc==2)
            {
            comp = new CheckBox("Noise", this, "dco" + osc + "noise");
            vbox.add(comp);
            }

        comp = new CheckBox("Click", this, "dco" + osc + "click");
        vbox.add(comp);

        hbox.add(vbox);

        comp = new LabelledDial("Wave Shape", this, "dco" + osc + "shape", color, 0, 31);
        ((LabelledDial)comp).setSecondLabel("Saw <> Tri");
        hbox.add(comp);

        comp = new LabelledDial("Frequency", this, "dco" + osc + "frequency", color, 0, 63);
        hbox.add(comp);
                
        comp = new LabelledDial("Frequency", this, "dco" + osc + "frequencymod", color, 0, 127, 64);
        ((LabelledDial)comp).setSecondLabel("LFO 1 Mod");
        hbox.add(comp);

        comp = new LabelledDial("Pulse Width", this, "dco" + osc + "pulsewidth", color, 0, 63);
        hbox.add(comp);

        comp = new LabelledDial("Pulse Width", this, "dco" + osc + "pulsewidthmod", color, 0, 127, 64);
        ((LabelledDial)comp).setSecondLabel("LFO 2 Mod");
        hbox.add(comp);

        if (osc==2)
            {
            comp = new LabelledDial("Detune", this, "dco" + osc + "detune", color, 0, 62, 31)
                {
                public boolean isSymmetric() { return true; }
                };
            hbox.add(comp);
            }


        if (osc==1)
            {
            vbox = new VBox();
            params = SYNC;
            comp = new Chooser("Sync", this, "dco" + osc + "sync", params);
            vbox.add(comp);
            hbox.add(vbox);
            }
                
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
        

    public JComponent addFilter(Color color)
        {
        Category category = new Category("Filter", color);
                
        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        
        comp = new CheckBox("Lever 1", this, "vcflever1");
        vbox.add(comp);
       
        comp = new CheckBox("Vibrato", this, "vcfvibrato");
        vbox.add(comp);
        
        comp = new CheckBox("Portamento", this, "vcfportamento");
        vbox.add(comp);

        comp = new CheckBox("Key Tracking", this, "vcfkeytracking");
        vbox.add(comp);
        hbox.add(vbox);
        
        comp = new LabelledDial("Frequency", this, "vcffrequency", color, 0, 127);
        hbox.add(comp);
                
        comp = new LabelledDial("Frequency", this, "vcffrequencymodenv1", color, 0, 127, 64);
        ((LabelledDial)comp).setSecondLabel("Env 1 Mod");
        hbox.add(comp);
                
        comp = new LabelledDial("Frequency", this, "vcffrequencymodpressure", color, 0, 127, 64);
        ((LabelledDial)comp).setSecondLabel("Press Mod");
        hbox.add(comp);
        
        comp = new LabelledDial("Resonance", this, "vcfresonance", color, 0, 63);
        hbox.add(comp);
                
        comp = new LabelledDial("FM", this, "vcffm", color, 0, 63);
        hbox.add(comp);

        comp = new LabelledDial("FM", this, "vcffmmodenv3", color, 0, 127, 64);
        ((LabelledDial)comp).setSecondLabel("Env 3 Mod");
        hbox.add(comp);

        comp = new LabelledDial("FM", this, "vcffmmodpressure", color, 0, 127, 64);
        ((LabelledDial)comp).setSecondLabel("Press Mod");
        hbox.add(comp);
                
        category.add(hbox, BorderLayout.WEST);
        return category;
        }







    /** Add Amplifier and Pan category */
    public JComponent addAmplifier(Color color)
        {
        Category category = new Category("Amplifier", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        comp = new LabelledDial("Volume", this, "vca1", color, 0, 63);
        hbox.add(comp);

        comp = new LabelledDial("Volume", this, "vca1modvel", color, 0, 127, 64);
        ((LabelledDial)comp).setSecondLabel("Vel Mod");
        hbox.add(comp);
                
        comp = new LabelledDial("Volume", this, "vca2modenv2", color, 0, 127, 64);
        ((LabelledDial)comp).setSecondLabel("Env 2 Mod");
        hbox.add(comp);
        
        category.add(hbox, BorderLayout.WEST);
        return category;
        }


    /** Add an LFO category */
    public JComponent addLFO(final int lfo, Color color)
        {
        Category category = new Category("LFO " + lfo + 
            (lfo == 1 ? "   (Oscillator Frequency)" : "   (Oscillator Pulsewidth)"), color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();

        params = LFO_SHAPES;
        comp = new Chooser("Shape", this, "lfo" + lfo + "shape", params);
        vbox.add(comp);

        params = LFO_TRIGGERS;
        comp = new Chooser("Trigger", this, "lfo" + lfo + "trigger", params);
        vbox.add(comp);
        hbox.add(vbox);

        vbox = new VBox();
        params = MODULATION_SOURCES;
        comp = new Chooser("Sampled Source", this, "lfo" + lfo + "source", params);
        vbox.add(comp);

        comp = new CheckBox("Lag", this, "lfo" + lfo + "lag");
        vbox.add(comp);
        hbox.add(vbox);

                
        // The manual says this is 0...63, but the sysex website says this is 5-bit
        comp = new LabelledDial("Retrigger", this, "lfo" + lfo + "retrigger", color, 0, 31);
        ((LabelledDial)comp).setSecondLabel("Point");
        hbox.add(comp);
                
        comp = new LabelledDial("Amplitude", this, "lfo" + lfo + "amplitude", color, 0, 63);
        hbox.add(comp);
                
        comp = new LabelledDial("Amplitude", this, "lfo" + lfo + "amplitudemod", color, 0, 63);
        ((LabelledDial)comp).setSecondLabel("Ramp " + lfo + " Mod");
        hbox.add(comp);
                
        comp = new LabelledDial("Speed", this, "lfo" + lfo + "speed", color, 0, 63);
        hbox.add(comp);

        comp = new LabelledDial("Speed", this, "lfo" + lfo + "speedmod", color, 0, 63);
        ((LabelledDial)comp).setSecondLabel(lfo == 1 ? "Press Mod" : "Key Mod");
        hbox.add(comp);
                
        category.add(hbox, BorderLayout.WEST);
        return category;
        }
        
    EnvelopeDisplay[] dadr = new EnvelopeDisplay[3];
    EnvelopeDisplay[] dadsr = new EnvelopeDisplay[3];
    HBox[] envelopeBox = new HBox[3];

    /** Add a "standard" envelope category */
    public JComponent addEnvelope(final int env, Color color)
        {
        Category category = new Category("Envelope " + env + 
                (env == 1 ? "   (Filter Frequency)" :
                (env == 2 ?  "   (Amplitude)" : "   (Filter FM)")), color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();


        // separate CheckBoxes maybe?
        vbox = new VBox();
        params = ENV_TRIGGER_MODES;
        comp = new Chooser("Trigger Mode", this, "env" + env + "triggermode", params);
        vbox.add(comp);

        envelopeBox[env - 1] = new HBox();
        // DADR
        dadr[env - 1] = new EnvelopeDisplay(this, Color.red, 
            new String[] { null, "env" + env + "delay", "env" + env + "attack", "env" + env + "decay", "env" + env + "release" },
            new String[] { null, null, null, "env" + env + "sustain", null },
            new double[] { 0, 0.25/63.0, 0.25/63.0, 0.25 / 63.0, 0.25/63.0},
            new double[] { 0, 0, 1.0, 1.0 / 63.0, 0 });

        // DADSR
        dadsr[env - 1] = new EnvelopeDisplay(this, Color.red, 
            new String[] { null, "env" + env + "delay", "env" + env + "attack", "env" + env + "decay", null, "env" + env + "release" },
            new String[] { null, null, null, "env" + env + "sustain", "env" + env + "sustain", null },
            new double[] { 0, 0.2/63.0, 0.2/63.0, 0.2 / 63.0,  0.2, 0.2/63.0},
            new double[] { 0, 0, 1.0, 1.0 / 63.0, 1.0/63.0, 0 });
        envelopeBox[env - 1].addLast(comp);

        params = ENV_MODES;
        comp = new Chooser("Envelope Mode", this, "env" + env + "mode", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                envelopeBox[env - 1].removeLast();
                int val = model.get(key, 0);
                if (val == 0 || val == 2)
                    envelopeBox[env - 1].addLast(dadsr[env - 1]);
                else
                    envelopeBox[env - 1].addLast(dadr[env - 1]);
                envelopeBox[env - 1].revalidate();
                envelopeBox[env - 1].repaint();
                }
            };
        vbox.add(comp);
        hbox.add(vbox);

        vbox = new VBox();
        params = ENV_LFO_TRIGGER_MODES;
        comp = new Chooser("LFO Trigger Mode", this, "env" + env + "lfotriggermode", params);
        vbox.add(comp);
        hbox.add(vbox);
                
        comp = new LabelledDial("Delay", this, "env" + env + "delay", color, 0, 63);
        hbox.add(comp);
        
        comp = new LabelledDial("Attack", this, "env" + env + "attack", color, 0, 63);
        hbox.add(comp);
        
        comp = new LabelledDial("Decay", this, "env" + env + "decay", color, 0, 63);
        hbox.add(comp);
        
        comp = new LabelledDial("Sustain", this, "env" + env + "sustain", color, 0, 63);
        hbox.add(comp);
        
        comp = new LabelledDial("Release", this, "env" + env + "release", color, 0, 63);
        hbox.add(comp);

        comp = new LabelledDial("Amplitude", this, "env" + env + "amplitude", color, 0, 63);
        hbox.add(comp);

        comp = new LabelledDial("Amplitude", this, "env" + env + "amplitudemod", color, 0, 127, 64);
        ((LabelledDial)comp).setSecondLabel("Vel Mod");
        hbox.add(comp);
        
        hbox.addLast(envelopeBox[env - 1]);
                
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
        







    /** Add free envelope category */
    public JComponent addTracking(Color color)
        {
        Category category = new Category("Tracking Generator", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = TRACKING_GENERATOR_SOURCES;
        comp = new Chooser("Input Source", this, "trackingsource", params);
        vbox.add(comp);
        hbox.add(vbox);
                
        comp = new LabelledDial("Point 1", this, "trackingpoint1", color, 0, 63);
        hbox.add(comp);
        
        comp = new LabelledDial("Point 2", this, "trackingpoint2", color, 0, 63);
        hbox.add(comp);

        comp = new LabelledDial("Point 3", this, "trackingpoint3", color, 0, 63);
        hbox.add(comp);

        comp = new LabelledDial("Point 4", this, "trackingpoint4", color, 0, 63);
        hbox.add(comp);

        comp = new LabelledDial("Point 5", this, "trackingpoint5", color, 0, 63);
        hbox.add(comp);
                           
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    /** Add free envelope category */
    public JComponent addRamp(int ramp, Color color)
        {
        Category category = new Category("Ramp " + ramp +
            (ramp == 1 ? "   (LFO 1 Amplitude)" : "   (LFO 2 Amplitude)"), color);
                
        JComponent comp;
        String[] params;
        
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = RAMP_TRIGGER_MODES;
        comp = new Chooser("Trigger Mode", this, "ramp" + ramp + "mode", params);
        vbox.add(comp);
        hbox.add(vbox);

       
        comp = new LabelledDial("Rate", this, "ramp" + ramp + "rate", color, 0, 63);
        hbox.add(comp);
        
        hbox.addLast(Strut.makeHorizontalStrut(70));

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    /** Add the Modulation category */
    public JComponent addModulation(Color color)
        {
        Category category  = new Category("Modulation", color);
                        
        JComponent comp;
        String[] params;
        VBox vbox = new VBox();
        
        for(int row = 0; row < 2; row++)
            {
            if (row == 1)
                {
                vbox.add(Strut.makeVerticalStrut(30));
                }
                        
            HBox hbox = new HBox();
            for(int j = 1; j < 6; j++)
                {
                int i = row * 5 + j;
                                
                VBox vbox2 = new VBox();
                params = MODULATION_SOURCES;
                comp = new Chooser("" + i + " Source", this, "modulation" + i + "source", params);
                // model.setSpecial("mod" + i + "source", 0);
                vbox2.add(comp);

                params = MODULATION_DESTINATIONS;
                comp = new Chooser("" + i + " Destination", this, "modulation" + i + "destination", params);
                vbox2.add(comp);

                comp = new LabelledDial("" + i + " Amount", this, "modulation" + i + "amount", color, 0, 127, 64);  // it's Level, not Amount, so we save some horizontal space
                vbox2.add(comp);
                hbox.add(vbox2);
                }
            vbox.add(hbox);
            }

                                    
        category.add(vbox, BorderLayout.WEST);
        return category;
        }



	//// MATRIX PARAMETERS
	////
	//// These are the (roughly 100) Oberheim Matrix parameters by parameter number.
	//// Note that this parameter number is not the same as the one in the sysex dump
	//// (that one is specified in allParametersToIndex).




    /** Map of parameter -> index in the allParameters array. */
    HashMap allMatrixParametersToIndex = new HashMap();


    /** List of all 100 internal Oberheim numerical parameters in order.  Note that this is DIFFERENT, ugh,
        from the order of parameters in the sysex list, and is missing stuff like modulation and name. */
                
    final static String[] allMatrixParameters = new String[]
    {
    "dco1frequency", 
    "dco1frequencymod", 
    "dco1sync", 
    "dco1pulsewidth", 
    "dco1pulsewidthmod", 
    "dco1shape", 
    "dco1waveenable", 
    "dco1fixedmods1", 
    "dco1fixedmods2", 
    "dco1click", 
    "dco2frequency", 
    "dco2frequencymod", 
    "dco2detune", 
    "dco2pulsewidth", 
    "dco2pulsewidthmod", 
    "dco2shape", 
    "dco2waveenable", 
    "dco2fixedmods1", 
    "dco2fixedmods2", 
    "dco2click", 
    "mix", 
    "vcffrequency", 
    "vcffrequencymodenv1", 
    "vcffrequencymodpressure", 
    "vcfresonance", 
    "vcffixedmods1", 
    "vcffixedmods2", 
    "vca1", 
    "vca1modvel", 
    "vca2modenv2", 
    "vcffm", 
    "vcffmmodenv3", 
    "vcffmmodpressure", 
    "trackingsource", 
    "trackingpoint1", 
    "trackingpoint2", 
    "trackingpoint3", 
    "trackingpoint4", 
    "trackingpoint5", 
    "-", 
    "ramp1rate", 
    "ramp1mode", 
    "ramp2rate", 
    "ramp2mode", 
    "portamento", 
    "portamentomod", 
    "portamentomode", 
    "portamentolegato", 
    "keyboardmode", 
    "-", 
    "env1delay", 
    "env1attack", 
    "env1decay", 
    "env1sustain", 
    "env1release", 
    "env1amplitude", 
    "env1amplitudemod", 
    "env1triggermode", 
    "env1mode", 
    "env1lfotriggermode", 
    "env2delay", 
    "env2attack", 
    "env2decay", 
    "env2sustain", 
    "env2release", 
    "env2amplitude", 
    "env2amplitudemod", 
    "env2triggermode", 
    "env2mode", 
    "env2lfotriggermode", 
    "env3delay", 
    "env3attack", 
    "env3decay", 
    "env3sustain", 
    "env3release", 
    "env3amplitude", 
    "env3amplitudemod", 
    "env3triggermode", 
    "env3mode", 
    "env3lfotriggermode", 
    "lfo1speed", 
    "lfo1speedmod", 
    "lfo1shape", 
    "lfo1retrigger", 
    "lfo1amplitude", 
    "lfo1amplitudemod", 
    "lfo1trigger", 
    "lfo1lag", 
    "lfo1source", 
    "-", 
    "lfo2speed", 
    "lfo2speedmod", 
    "lfo2shape", 
    "lfo2retrigger", 
    "lfo2amplitude", 
    "lfo2amplitudemod", 
    "lfo2trigger", 
    "lfo2lag", 
    "lfo2source", 
    "-"
    };
    
    
    


    /** Map of parameter -> index in the allParameters array. */
    HashMap allParametersToIndex = new HashMap();


    /** List of all Oberheim Sysex parameters in order.  "-" is a reserved (unused and thus unnamed) parameter. */

    /// * indicates parameters which must be handled specially due to packing
    /// that Waldorf decided to do.  :-(

    final static String[] allParameters = new String[/*100 or so*/] 
    {
    "-",                // this is the name, but the Matrix 1000 doesn't recognize names
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "keyboardmode",                   
    "dco1frequency",
    "dco1shape",
    "dco1pulsewidth",
    "dco1fixedmods1",           // *
    "dco1waveenable",           // *
    "dco2frequency",
    "dco2shape",
    "dco2pulsewidth",
    "dco2fixedmods1",           // *
    "dco2waveenable",           // *
    "dco2detune",
    "mix",
    "dco1fixedmods2",           // *
    "dco1click",
    "dco2fixedmods2",           // *
    "dco2click",
    "dco1sync",
    "vcffrequency",
    "vcfresonance",
    "vcffixedmods1",                // *
    "vcffixedmods2",                // *
    "vcffm",
    "vca1",
    "portamento",
    "portamentomode",               // *
    "portamentolegato",
    "lfo1speed",
    "lfo1trigger",
    "lfo1lag",
    "lfo1shape",
    "lfo1retrigger",
    "lfo1source",
    "lfo1amplitude",
    "lfo2speed",
    "lfo2trigger",
    "lfo2lag",
    "lfo2shape",
    "lfo2retrigger",
    "lfo2source",
    "lfo2amplitude",
    "env1triggermode",
    "env1delay",
    "env1attack",
    "env1decay",
    "env1sustain",
    "env1release",
    "env1amplitude",
    "env1lfotriggermode",
    "env1mode",
    "env2triggermode",
    "env2delay",
    "env2attack",
    "env2decay",
    "env2sustain",
    "env2release",
    "env2amplitude",
    "env2lfotriggermode",
    "env2mode",
    "env3triggermode",
    "env3delay",
    "env3attack",
    "env3decay",
    "env3sustain",
    "env3release",
    "env3amplitude",
    "env3lfotriggermode",
    "env3mode",
    "trackingsource",
    "trackingpoint1",
    "trackingpoint2",
    "trackingpoint3",
    "trackingpoint4",
    "trackingpoint5",
    "ramp1rate",
    "ramp1mode",
    "ramp2rate",
    "ramp2mode",
    "dco1frequencymod",
    "dco1pulsewidthmod",
    "dco2frequencymod",
    "dco2pulsewidthmod",
    "vcffrequencymodenv1",
    "vcffrequencymodpressure",
    "vca1modvel",
    "vca2modenv2",
    "envelope1amplitudemod",
    "envelope2amplitudemod",
    "envelope3amplitudemod",
    "lfo1amplitudemod",
    "lfo2amplitudemod",
    "portamentomod",
    "vcffmmodenv3",
    "vcffmmodpressure",
    "lfo1speedmod",
    "lfo2speedmod",
    "mod1source",
    "mod1destination",
    "mod1amount",
    "mod2source",
    "mod2destination",
    "mod2amount",
    "mod3source",
    "mod3destination",
    "mod3amount",
    "mod4source",
    "mod4destination",
    "mod4amount",
    "mod5source",
    "mod5destination",
    "mod5amount",
    "mod6source",
    "mod6destination",
    "mod6amount",
    "mod7source",
    "mod7destination",
    "mod7amount",
    "mod8source",
    "mod8destination",
    "mod8amount",
    "mod9source",
    "mod9destination",
    "mod9amount",
    "mod10source",
    "mod10destination",
    "mod10amount"
    };



    public byte[] emit(String key)
        {
        if (key.equals("bank")) return new byte[0];  // this is not emittable
        if (key.equals("number")) return new byte[0];  // this is not emittable

        int index;
        int value;

        if (key.equals("name"))
        	{
        	return new byte[0];  // ignore
        	}
        if (key.equals("dco1lever1") || key.equals("dco1vibrato"))
            {
            index = ((Integer)(allMatrixParametersToIndex.get("dco1fixedmods1"))).intValue();
            value = model.get("dco1lever1", 0) |  (model.get("dco1vibrato", 0) << 1);
            }
        else if (key.equals("dco1portamento") || key.equals("dco1keytracking"))
            {
            index = ((Integer)(allMatrixParametersToIndex.get("dco1fixedmods2"))).intValue();
            value = model.get("dco1portamento", 0) | (model.get("dco1keytracking", 0) << 1);
            }
        else if (key.equals("dco2lever1") || key.equals("dco2vibrato"))
            {
            index = ((Integer)(allMatrixParametersToIndex.get("dco2fixedmods1"))).intValue();
            value = model.get("dco2lever1", 0) | (model.get("dco2vibrato", 0) << 1);
            }
        else if (key.equals("dco2portamento") || key.equals("dco2keytracking"))
            {
            index = ((Integer)(allMatrixParametersToIndex.get("dco2fixedmods2"))).intValue();
            value = model.get("dco2portamento", 0) | (model.get("dco2keytracking", 0) << 1);
            }
        else if (key.equals("dco1wave") || key.equals("dco1pulse"))
            {
            index = ((Integer)(allMatrixParametersToIndex.get("dco1waveenable"))).intValue();
            value = model.get("dco1wave", 0) | (model.get("dco1pulse", 0) << 1);
            }
        else if (key.equals("dco2wave") || key.equals("dco2pulse") || key.equals("dco2noise"))
            {
            index = ((Integer)(allMatrixParametersToIndex.get("dco2waveenable"))).intValue();
            value = model.get("dco2wave", 0) | (model.get("dco2pulse", 0) << 1) | (model.get("dco2noise", 0) << 2);
            }
        else if (key.equals("vcflever1") || key.equals("vcfvibrato"))
            {
            index = ((Integer)(allMatrixParametersToIndex.get("vcffixedmods1"))).intValue();
            value = model.get("vcflever1", 0) | (model.get("vcfvibrato", 0) << 1);
            }
        else if (key.equals("vcfportamento") || key.equals("vcfkeytracking"))
            {
            index = ((Integer)(allMatrixParametersToIndex.get("vcffixedmods2"))).intValue();
            value = model.get("vcfportamento", 0) | (model.get("vcfkeytracking", 0) << 1);
            }
        else if (key.startsWith("mod"))
            {
            int modnumber = (int)(key.charAt(3) - '0');
            if (key.charAt(4) == '0') // it's 10
                modnumber = 10;

            int modsource = model.get("mod" + modnumber  + "source", 0);
            int moddestination = model.get("mod" + modnumber  + "destination", 0) + 1;  // IMPORTANT it  goes 1--32, not 0--31
            int modamount = model.get("mod" + modnumber  + "amount", 0);
            modnumber--;

            return new byte[] { (byte)0xF0, 0x10, 0x06, 0x0B, (byte)modnumber, (byte)modsource, (byte) modamount, (byte)moddestination, (byte)0xF7 };
            }
        // don't need to customize portamentomode though we'll have to do it on parse
                
        //else if (key.equals("portamentomode"))
        //              {
        //              // two things are both exponential
        //              }
        else
        	{
         	index = ((Integer)(allMatrixParametersToIndex.get(key))).intValue();
        	value = model.get(key, 0);
        	}

        byte VV = (byte)(value);
        byte PP = (byte)(index & 127);
        return new byte[] { (byte)0xF0, 0x10, 0x06, PP, VV, (byte)0xF7 };
        }
    


    public void parseParameter(byte[] data)
        {
        if (data[3] == 0x0B)            // remote modulation parameter edit
            {
            byte modulation = (byte)(data[4] + 1);  // stored as 1...10, not 0...9
            byte source = data[5];
            byte amount = data[6];
            byte destination = data[7];
                
            model.set("mod" + modulation + "source", source);
            model.set("mod" + modulation + "destination", destination);
            model.set("mod" + modulation + "amount", amount);
            }
        else if (data[3] == 0x06)               // remote basic parameter edit
            {
            byte parameter = data[4];
            byte value = data[5];
                
            String key = allMatrixParameters[parameter];
            if (key.equals("dco1fixedmods1"))
                {
                model.set("dco1lever1", value & 1);
                model.set("dco1vibrato", (value >> 1) & 1);
                }
            else if (key.equals("dco1fixedmods2"))
                {
                model.set("dco1portamento", value & 1);
                model.set("dco1keytracking", (value >> 1) & 1);
                }
            else if (key.equals("dco2fixedmods1"))
                {
                model.set("dco2lever1", value & 1);
                model.set("dco2vibrato", (value >> 1) & 1);
                }
            else if (key.equals("dco2fixedmods2"))
                {
                model.set("dco2portamento", value & 1);
                model.set("dco2keytracking", (value >> 1) & 1);
                }
            else if (key.equals("dco1waveenable"))
                {
                model.set("dco1wave", value & 1);
                model.set("dco1pulse", (value >> 1) & 1);
                }
            else if (key.equals("dco2waveenable"))
                {
                model.set("dco2wave", value & 1);
                model.set("dco2pulse", (value >> 1) & 1);
                model.set("dco2noise", (value >> 2) & 1);
                }
            else if (key.equals("vcffixedmods1"))
                {
                model.set("vcflever1", value & 1);
                model.set("vcfvibrato", (value >> 1) & 1);
                }
            else if (key.equals("vcffixedmods2"))
                {
                model.set("vcfportamento", value & 1);
                model.set("vcfkeytracking", (value >> 1) & 1);
                }
            else if (key.equals("portamentomode"))
                {
                if (value == 4)
                    value = (byte)3;  // get rid of extra exponential
                model.set("portamentomode", value);
                }
            else
                {
                model.set(key, value);
                }
            }
        else
            {
            // we'll put CC here later
            }
        revise();
        }
        
    public boolean parse(byte[] data, boolean ignorePatch)
        {
        //  packing by two nibbles per byte (see http://www.youngmonkey.ca/nose/audio_tech/synth/Oberheim-OberheimMatrix1000.html)
        
        byte[] name = new byte[8];
        
        // we don't know the bank, just the number.  :-(
        int number = data[4];
		if (!ignorePatch)
			model.set("number", number);
			
        for(int i = 0; i < 134; i++)
        	{
        	// unpack from nybbles
        	byte lonybble = data[i * 2 + 5];
        	byte hinybble = data[i * 2 + 5 + 1];
        	byte value = (byte)(((hinybble << 4) | (lonybble & 15)) & 127);

        	String key = allParameters[i];
        	
        	if (i < 8)  // it's the name
        		name[i] = unpackNameByte(value);
        	else if (key.equals("dco1fixedmods1"))
        		{
                model.set("dco1lever1", value & 1);
                model.set("dco1vibrato", (value >> 1) & 1);
        		}
        	else if (key.equals("dco1fixedmods2"))
        		{
                model.set("dco1portamento", value & 1);
                model.set("dco1keytracking", (value >> 1) & 1);
        		}
        	else if (key.equals("dco2fixedmods1"))
        		{
                model.set("dco2lever1", value & 1);
                model.set("dco2vibrato", (value >> 1) & 1);
        		}
        	else if (key.equals("dco2fixedmods2"))
        		{
                model.set("dco2portamento", value & 1);
                model.set("dco2keytracking", (value >> 1) & 1);
        		}
        	else if (key.equals("dco1waveenable"))
        		{
                model.set("dco1wave", value & 1);
                model.set("dco1pulse", (value >> 1) & 1);
        		}
        	else if (key.equals("dco2waveenable"))
        		{
                model.set("dco2wave", value & 1);
                model.set("dco2pulse", (value >> 1) & 1);
                model.set("dco2noise", (value >> 2) & 1);
        		}
        	else if (key.equals("vcffixedmods1"))
        		{
                model.set("vcflever1", value & 1);
                model.set("vcfvibrato", (value >> 1) & 1);
        		}
        	else if (key.equals("vcffixedmods2"))
        		{
                model.set("vcfportamento", value & 1);
                model.set("vcfkeytracking", (value >> 1) & 1);
        		}
        	else if (key.equals("portamentomode"))
        		{
                if (value == 4)
                    value = (byte)3;  // get rid of extra exponential
                model.set("portamentomode", value);
        		}
        	else
        		{
        		model.set(key, value);
        		}
        	}
        
        // update name just for fun, it may be gibberish
        	try 
                {
       			model.set("name", new String(name, "US-ASCII"));
                }
            catch (UnsupportedEncodingException e)
            	{
            	e.printStackTrace();
            	}
        
        revise();
        return true;            // change this as appropriate
        }
    

    public Object[] emitAll(Model tempModel, boolean toWorkingMemory)
        {
        // this stuff requires a checksum
        // and required packing by two nibbles per byte (see http://www.youngmonkey.ca/nose/audio_tech/synth/Oberheim-OberheimMatrix1000.html)
        // also note that writes are probably always to working memory.
        // In order to STORE the working memory to the backing store
        // You do STORE EDIT BUFFER:
        // F0H 10H 06H 0EH PATCHNUMBER BANKNUMBER 0x0 F7H

		byte[] data = new byte[268];
		String nm = model.get("name", "UNTITLED") + "        ";
		byte[] name = null;
		try { name = nm.getBytes("US-ASCII"); } catch (Exception e ) { }
		int value;
		int check = 0;
		
        for(int i = 0; i < 134; i++)
        	{
        	String key = allParameters[i];
        	
        	if (i < 8)  // it's the name
        		value = packNameByte(name[i]);
        	else if (key.equals("dco1fixedmods1"))
        		{
                value = (model.get("dco1vibrato", 0) << 1) |
                		(model.get("dco1lever1", 0));
        		}
        	else if (key.equals("dco1fixedmods2"))
        		{
                value = (model.get("dco1keytracking", 0) << 1) |
                		(model.get("dco1portamento", 0));
        		}
        	else if (key.equals("dco2fixedmods1"))
        		{
                value = (model.get("dco2vibrato", 0) << 1) |
                		(model.get("dco2lever1", 0));
        		}
        	else if (key.equals("dco2fixedmods2"))
        		{
                value = (model.get("dco2keytracking", 0) << 1) |
                		(model.get("dco2portamento", 0));
        		}
        	else if (key.equals("dco1waveenable"))
        		{
                value = (model.get("dco1pulse", 0) << 1) |
                		(model.get("dco1wave", 0));
        		}
        	else if (key.equals("dco2waveenable"))
        		{
                value = (model.get("dco2noise", 0) << 2) |
                		(model.get("dco2pulse", 0) << 1) |
                		(model.get("dco2wave", 0));
        		}
        	else if (key.equals("vcffixedmods1"))
        		{
                value = (model.get("vcfvibrato", 0) << 1) |
                		(model.get("vcflever1", 0));
        		}
        	else if (key.equals("vcffixedmods2"))
        		{
                value = (model.get("vcfkeytracking", 0) << 1) |
                		(model.get("vcfportamento", 0));
        		}
        	// no need to handle portamentomode specially, but we DO have to parse it specially
/*        	else if (key.equals("portamentomode"))
        		{
        		}
*/
        	else
        		{
        		value = model.get(key, 0);
        		}

        	// pack to nybbles
        	byte lonybble = (byte)(value & 15);
        	byte hinybble = (byte)(value >> 4);
        	
        	// From here:  http://www.youngmonkey.ca/nose/audio_tech/synth/Oberheim-Matrix6R.html
        	// it says this about the checksum:
        	//
            // Checksum.
            // The original (not transmitted) data is summed in seven bits ignoring overflows
            //
            // From this I gather that we add into an int, not a byte, so we don't
            // overflow at all.  Though it might be a byte instead, I'm not sure.
            // Or maybe it doesn't make a difference?  It's late...  Anyway:

        	check += value;
        	
        	// write
        	data[i * 2] = lonybble;
        	data[i * 2] = hinybble;
        	}
    
        byte checksum = (byte)(check & 127);

		if (toWorkingMemory)
			{
			// 0DH - SINGLE PATCH DATA TO EDIT BUFFER
			byte[] d = new byte[275];
			d[0] = (byte)0xF0;
			d[1] = (byte)0x10;
			d[2] = (byte)0x06;
			d[3] = (byte)0x0D;
			d[4] = (byte)0x00;
			System.arraycopy(data, 0, d, 5, 268);
			d[273] = checksum;
			d[274] = (byte)0xF7;

			Object[] completed = new Object[1];
			completed[0] = d;
			return completed;
			}
		else
			{
			Object[] completed = new Object[2];

			// 0AH - SET BANK
			// we write this store-command as a sysex command 
			// so it gets stripped when we do a save to file
			byte[] data2 = new byte[8];
			data2[0] = (byte)0xF0;
			data2[1] = (byte)0x10;
			data2[2] = (byte)0x06;	
			data2[3] = (byte)0x0A;
			data2[5] = (byte)(model.get("bank", 0));
			data2[7] = (byte)0xF7;

			try
				{
				completed[0] = new SysexMessage(data2, 8);
				}
			catch (InvalidMidiDataException e) 
				{
				e.printStackTrace();
				}

			// 01H-SINGLE PATCH DATA

			byte[] d = new byte[273];
			d[0] = (byte)0xF0;
			d[1] = (byte)0x10;
			d[2] = (byte)0x06;
			d[3] = (byte)0x01;
			System.arraycopy(data, 0, d, 4, 268);
			d[271] = checksum;
			d[272] = (byte)0xF7;
			
			completed[1] = d;
			return completed;
			}
        }
        
        
        
    public void changePatch(Model tempModel)
    	{
 		// first change the bank
		
			// 0AH - SET BANK
			// we write this store-command as a sysex command 
			// so it gets stripped when we do a save to file
			byte[] data2 = new byte[8];
			data2[0] = (byte)0xF0;
			data2[1] = (byte)0x10;
			data2[2] = (byte)0x06;	
			data2[3] = (byte)0x0A;
			data2[5] = (byte)(tempModel.get("bank", 0));
			data2[7] = (byte)0xF7;

			tryToSendSysex(data2);
			
		// Next do a program change
		
        byte NN = (byte)tempModel.get("number", 0);
        tryToSendSysex(buildPC(getChannelOut() - 1, NN));
    	}


	public void performRequestDump(Model tempModel)
		{
		/*
		// first change the bank
		
			// 0AH - SET BANK
			// we write this store-command as a sysex command 
			// so it gets stripped when we do a save to file
			byte[] data2 = new byte[8];
			data2[0] = (byte)0xF0;
			data2[1] = (byte)0x10;
			data2[2] = (byte)0x06;	
			data2[3] = (byte)0x0A;
			data2[5] = (byte)(tempModel.get("bank", 0));
			data2[7] = (byte)0xF7;

			tryToSendSysex(data2);
		*/
		
		// Next do a dump request
		byte[] data = new byte[7];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x10;
        data[2] = (byte)0x06;
        data[3] = (byte)0x04;
        data[4] = (byte)1;		// request single patch
        data[5] = (byte)(tempModel.get("number", 0));
        data[6] = (byte)0xF7;
        
        tryToSendSysex(data);
		}
		
    public byte[] requestCurrentDump(Model tempModel)
        {
        // this is not available as far as I can tell
        new RuntimeException("This should never be called").printStackTrace();
        return new byte[0];
        }

    public static final int EXPECTED_SYSEX_LENGTH = 273;        
        
    public static boolean recognize(byte[] data)
        {
        boolean v = (
        	// The Matrix 1000 doesn't transmit the checksum!
        	// So it could be one of two lengths:
            (data.length == EXPECTED_SYSEX_LENGTH ||
             data.length == EXPECTED_SYSEX_LENGTH - 1) &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x3E &&
            data[2] == (byte)0x0E &&
            data[4] == (byte)0x10);
        return v;
        }
        

    public static final int MAXIMUM_NAME_LENGTH = 8;
    public String reviseName(String name)
    	{
    	name = super.reviseName(name);  // trim first time
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
		return super.reviseName(name);  // trim again
    	}


    /** Verify that all the parameters are within valid values, and tweak them if not. */
    public void revise()
        {
        // check the easy stuff -- out of range parameters
        super.revise();
        
		String nm = model.get("name", "UNTITLED");
		String newnm = reviseName(nm);
		if (!nm.equals(newnm))
	        model.set("name", newnm);
        }
        

    public static String getSynthName() { return "Oberheim Matrix 1000"; }
    
    public String getPatchName() { return model.get("name", "UNTITLED"); }
    
    }
