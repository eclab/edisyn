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
    public static final String[] MODULATION_SOURCES = new String[] { "None", "Env 1", "Env 2", "Env 3", "LFO 1", "LFO 2", "Vibrato", "Ramp 1", "Ramp 2", "Keyboard", "Portamento", "Tracking Generator", "Keyboard Gate", "Velocity", "Release Velocity", "Pressure", "Pedal 1", "Pedal 2", "Bend", "Mod Wheel", "Lever 3" };
    public static final String[] TRACKING_GENERATOR_SOURCES = new String[] { "Env 1", "Env 2", "Env 3", "LFO 1", "LFO 2", "Vibrato", "Ramp 1", "Ramp 2", "Keyboard", "Portamento", "Tracking Generator", "Keyboard Gate", "Velocity", "Release Velocity", "Pressure", "Pedal 1", "Pedal 2", "Bend", "Mod Wheel", "Lever 3" };
    public static final String[] ENV_TRIGGER_MODES = new String[] { "Single", "Single Reset", "Multi", "Multi Reset", "External Single", "External Single Reset", "External Multi", "External Multi Reset" }; 
    public static final String[] ENV_MODES = new String[] { "Normal", "DADR", "Free Run", "DADR + Free Run" }; 
    // There are actually 2 bits here, so we're missing one
    public static final String[] ENV_LFO_TRIGGER_MODES = new String[] { "Normal", "LFO 1", "Gated LFO 1" }; 
    public static final String[] RAMP_TRIGGER_MODES = new String[] { "Single", "Multi", "External", "External Gated" };
    // VCA2 is not mentioned anywhere else
    // I may need to say VCA rather than Amplifier elsewhere
    public static final String[] MODULATION_DESTINATIONS = new String[] { "None", "DCO 1 Frequency", "DCO 1 Pulsewidth", "DCO 1 Wave Shape",  "DCO 2 Frequency", "DCO 2 Pulsewidth", "DCO 2 Wave Shape", "Mix Level", "Filter FM", "Filter Frequency", "Filter Resonance", "VCA 1 Level", "VCA 2 Level", "Env 1 Delay", "Env 1 Attack", "Env 1 Decay", "Env 1 Release", "Env 1 Amplitude", "Env 2 Delay", "Env 2 Attack", "Env 2 Decay", "Env 2 Release", "Env 2 Amplitude", "Env 3 Delay", "Env 3 Attack", "Env 3 Decay", "Env 3 Release", "Env 3 Amplitude", "LFO 1 Speed", "LFO 1 Amplitude", "LFO 2 Speed", "LFO 2 Amplitude", "Portamento Time" };

    public OberheimMatrix1000()
        {
        for(int i = 0; i < allParameters.length; i++)
            {
            allParametersToIndex.put(allParameters[i], Integer.valueOf(i));
            }

        for(int i = 0; i < internalParameters.length; i++)
            {
            internalParametersToIndex.put(internalParameters[i], Integer.valueOf(i));
            }
                        
        /// SOUND PANEL
                
        SynthPanel soundPanel = new SynthPanel();
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
        addTab("Oscillators and Filters", soundPanel);
                
                
        // ENVELOPE PANEL
                
        SynthPanel envelopePanel = new SynthPanel();
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
        addTab("LFOs and Envelopes", envelopePanel);

        
        
        // MODULATION PANEL
                
        SynthPanel modulationPanel = new SynthPanel();
        
        vbox = new VBox();
        vbox.add(addModulation(Style.COLOR_A));
        vbox.add(addTracking(Style.COLOR_B));
                

        modulationPanel.add(vbox, BorderLayout.CENTER);
        addTab("Modulation", modulationPanel);

        model.set("name", "UNTITLED");
        model.set("bank", 0);
        model.set("number", 0);
        
        loadDefaults();        
        }
                
    public String getDefaultResourceFileName() { return "OberheimMatrix1000.init"; }
    public String getHTMLResourceFileName() { return "OberheimMatrix1000.html"; }

    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        JTextField bank = new JTextField("" + (model.get("bank")), 3);
        JTextField number = new JTextField("" + (model.get("number")), 3);

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
                showSimpleError(title, "The Patch Number must be an integer 0...99");
                continue;
                }
            if (n < 0 || n > 99)
                {
                showSimpleError(title, "The Patch Number must be an integer 0...99");
                continue;
                }
                                
            int i;
            try { i = Integer.parseInt(bank.getText()); }
            catch (NumberFormatException e)
                {
                showSimpleError(title, "The Bank must be an integer 0 ... 9");
                continue;
                }
            if (i < 0 || (writing ? (i > 1) : (i > 9)))
                {
                showSimpleError(title, writing ? "The Bank must be either 0 or 1" : "The Bank must be an integer 0 ... 9");
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
        receiveCurrent.setEnabled(false);               // can't receive current patch
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
        /*
          if (n < 32 || n > 95)
          n = (byte)32;
          if (n >= 64)
          n -= 64;
        */
        return n;
        }
                
    byte unpackNameByte(byte n)
        {
        /*
          n = (byte)(n & 63);
          if (n < 32)
          n = (byte)(n + 64);
        */
        return n;
        }

    /** Add the global patch category (name, id, number, etc.) */
    public JComponent addNameGlobal(Color color)
        {
        Category globalCategory = new Category(this, getSynthName(), color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
                                
        VBox vbox = new VBox();
        comp = new PatchDisplay(this, "Patch", "bank", "number", 4)
            {
            public String numberString(int number) { return (number > 9 ? "" : "0") + number; }
            public String bankString(int bank) { return "" + bank; }
            };
        vbox.add(comp);
        
        comp = new StringComponent("Patch Name", this, "name", 8, "Name must be up to 8 letters, numbers, spaces, or !\"#$%&'()*+,-./:;<=>?[\\]^_.")
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

        hbox.addLast(Strut.makeHorizontalStrut(100));

        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }

    public JComponent addOscillatorGlobal(Color color)
        {
        Category category = new Category(this, "Keyboard", color);

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

        comp = new LabelledDial("Mix", this, "mix", color, 0, 62, 31)
            {
            public boolean isSymmetric() { return true; }
            };
        ((LabelledDial)comp).addAdditionalLabel("DCO 2 <> 1");
        hbox.add(comp);

        comp = new LabelledDial("Portamento", this, "portamento", color, 0, 63);
        ((LabelledDial)comp).addAdditionalLabel("Rate");
        hbox.add(comp);

        comp = new LabelledDial("Portamento", this, "portamentomod", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Vel Mod");
        hbox.add(comp);
                
        vbox = new VBox();
        comp = new CheckBox("Legato Portamento", this, "portamentolegato");
        vbox.add(comp);
        hbox.add(vbox);
       
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
                
    /** Add an Oscillator category */
    public JComponent addOscillator(int osc, Color color)
        {
        Category category = new Category(this, "Oscillator " + osc, color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
               
        VBox vbox = new VBox();
        comp = new CheckBox("Bend", this, "dco" + osc + "bend");
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


        //// Sysex documentation is inconsistent here, it's not clear if it's 5-bit or 6-bit.
        //// But the Matrix 1000 is providing 6-bit values, so we're going with that (0...63)
                
        comp = new LabelledDial("Wave Shape", this, "dco" + osc + "shape", color, 0, 63);
        ((LabelledDial)comp).addAdditionalLabel("Saw <> Tri");
        hbox.add(comp);

        comp = new LabelledDial("Frequency", this, "dco" + osc + "frequency", color, 0, 63);
        hbox.add(comp);
                
        comp = new LabelledDial("Frequency", this, "dco" + osc + "frequencymod", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("LFO 1 Mod");
        hbox.add(comp);

        comp = new LabelledDial("Pulse Width", this, "dco" + osc + "pulsewidth", color, 0, 63);
        hbox.add(comp);

        comp = new LabelledDial("Pulse Width", this, "dco" + osc + "pulsewidthmod", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("LFO 2 Mod");
        hbox.add(comp);

        if (osc==2)
            {
            comp = new LabelledDial("Detune", this, "dco" + osc + "detune", color, 1, 63, 31)
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
        Category category = new Category(this, "Filter", color);
                
        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        
        comp = new CheckBox("Bend", this, "vcfbend");
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
                
        comp = new LabelledDial("Frequency", this, "vcffrequencyenv1mod", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Env 1 Mod");
        hbox.add(comp);
                
        comp = new LabelledDial("Frequency", this, "vcffrequencypressuremod", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Press Mod");
        hbox.add(comp);
        
        comp = new LabelledDial("Resonance", this, "vcfresonance", color, 0, 63);
        hbox.add(comp);
                
        comp = new LabelledDial("FM", this, "vcffm", color, 0, 63);
        hbox.add(comp);

        comp = new LabelledDial("FM", this, "vcffmenv3mod", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Env 3 Mod");
        hbox.add(comp);

        comp = new LabelledDial("FM", this, "vcffmpressuremod", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Press Mod");
        hbox.add(comp);
                
        category.add(hbox, BorderLayout.WEST);
        return category;
        }







    /** Add Amplifier and Pan category */
    public JComponent addAmplifier(Color color)
        {
        Category category = new Category(this, "Amplifier", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        comp = new LabelledDial("Volume", this, "vca1", color, 0, 63);
        hbox.add(comp);

        comp = new LabelledDial("Volume", this, "vca1velmod", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Vel Mod");
        hbox.add(comp);
                
        comp = new LabelledDial("Volume", this, "vca2env2mod", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Env 2 Mod");
        hbox.add(comp);
        
        category.add(hbox, BorderLayout.WEST);
        return category;
        }


    /** Add an LFO category */
    public JComponent addLFO(final int lfo, Color color)
        {
        Category category = new Category(this, "LFO " + lfo + 
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

                
        // The manual says this is 0...63, but the sysex website says this is 5-bit.
        // The Matrix 1000 is providing 6-bit values so we're going with that.
        
        comp = new LabelledDial("Retrigger", this, "lfo" + lfo + "retrigger", color, 0, 63);
        ((LabelledDial)comp).addAdditionalLabel("Point");
        hbox.add(comp);
                
        comp = new LabelledDial("Amplitude", this, "lfo" + lfo + "amplitude", color, 0, 63);
        hbox.add(comp);
                
        comp = new LabelledDial("Amplitude", this, "lfo" + lfo + "amplitudemod", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Ramp " + lfo + " Mod");
        hbox.add(comp);
                
        comp = new LabelledDial("Speed", this, "lfo" + lfo + "speed", color, 0, 63);
        hbox.add(comp);

        comp = new LabelledDial("Speed", this, "lfo" + lfo + "speedmod", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel(lfo == 1 ? "Press Mod" : "Key Mod");
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
        Category category = new Category(this, "Envelope " + env + 
                (env == 1 ? "   (Filter Frequency)" :
                (env == 2 ?  "   (Amplitude)" : "   (Filter FM)")), color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();


        // separate CheckBoxes maybe?
        VBox vbox = new VBox();
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
        envelopeBox[env - 1].addLast(dadsr[env - 1]);

        params = ENV_MODES;
        comp = new Chooser("Envelope Mode", this, "env" + env + "mode", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                envelopeBox[env - 1].removeLast();
                int val = model.get(key);
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

        comp = new LabelledDial("Amplitude", this, "env" + env + "amplitudemod", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Vel Mod");
        hbox.add(comp);
        
        hbox.addLast(envelopeBox[env - 1]);
                
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
        







    /** Add free envelope category */
    public JComponent addTracking(Color color)
        {
        Category category = new Category(this, "Tracking Generator", color);
                
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


        comp = new EnvelopeDisplay(this, Color.red, 
            new String[] { null, null, null, null, null },
            new String[] { "trackingpoint1", "trackingpoint2", "trackingpoint3", "trackingpoint4", "trackingpoint5" },
            new double[] { 0, 0.25, 0.25, 0.25, 0.25},
            new double[] { 1.0/63, 1.0/63, 1.0/63, 1.0/63, 1.0/63 });
        hbox.add(comp);
                           
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    /** Add free envelope category */
    public JComponent addRamp(int ramp, Color color)
        {
        Category category = new Category(this, "Ramp " + ramp +
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
        Category category  = new Category(this, "Modulation", color);
                        
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
                comp = new Chooser("" + i + " Source", this, "mod" + i + "source", params);
                vbox2.add(comp);

                params = MODULATION_DESTINATIONS;
                comp = new Chooser("" + i + " Destination", this, "mod" + i + "destination", params);
                vbox2.add(comp);

                comp = new LabelledDial("" + i + " Amount", this, "mod" + i + "amount", color, 1, 127, 64);  // it's Level, not Amount, so we save some horizontal space
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
    HashMap internalParametersToIndex = new HashMap();


    /** List of all 100 internal Oberheim numerical parameters in order.  Note that this is DIFFERENT, ugh,
        from the order of parameters in the sysex list, and is missing stuff like modulation and name. */
                
    final static String[] internalParameters = new String[]
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
    "vcffrequencyenv1mod", 
    "vcffrequencypressuremod", 
    "vcfresonance", 
    "vcffixedmods1", 
    "vcffixedmods2", 
    "vca1", 
    "vca1velmod", 
    "vca2env2mod", 
    "vcffm", 
    "vcffmenv3mod", 
    "vcffmpressuremod", 
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


    /** The Matrix 1000 sign-extends into its 7th bit.  Basically this means 
        that if the value is N bits, then remaining high bits should have the
        value of the high (Nth) bit.  For example, the value 1011 should be
        converted to (0) 111011.  We also may need to know signed or insigned
        values.  So to do this, we have two arrays, bitmasks and signed.  We
        compute them from the initial values stored in bitmasks (which are then
        changed to actual bitmasks).  If the value below is negative, then the
        parameter is expected to be signed. */

    final static int[] bitmasks = new int[/*100 or so*/]
    {
    7,
    7,
    7,
    7,
    7,
    7,
    7,
    7,
    2,
    6,
    6,
    6,
    2,
    2,
    6,
    6,
    6,
    2,
    3,
    -6,
    6,
    2,
    1,
    2,
    1,
    2,
    7,
    6,
    2,
    2,
    6,
    6,
    6,
    2,
    1,
    6,
    2,
    1,
    3,
    5,
    5,
    6,
    6,
    2,
    1,
    3,
    5,
    5,
    6,
    3,
    6,
    6,
    6,
    6,
    6,
    6,
    2,
    2,
    3,
    6,
    6,
    6,
    6,
    6,
    6,
    2,
    2,
    3,
    6,
    6,
    6,
    6,
    6,
    6,
    2,
    2,
    5,
    6,
    6,
    6,
    6,
    6,
    6,
    2,
    6,
    2,
    -7,
    -7,
    -7,
    -7,
    -7,
    -7,
    -7,
    -7,
    -7,
    -7,
    -7,
    -7,
    -7,
    -7,
    -7,
    -7,
    -7,
    -7,
    5,
    -7,
    5,
    5,
    -7,
    5,
    5,
    -7,
    5,
    5,
    -7,
    5,
    5,
    -7,
    5,
    5,
    -7,
    5,
    5,
    -7,
    5,
    5,
    -7,
    5,
    5,
    -7,
    5,
    5,
    -7,
    5
    };
        
    final static boolean signed[] = new boolean[bitmasks.length];
        
    static
        {
        for(int i = 0; i < bitmasks.length; i++)
            {
            if (bitmasks[i] < 0)
                {
                bitmasks[i] = -bitmasks[i];
                signed[i] = true;
                }
                                
            bitmasks[i] = (1 << bitmasks[i]) - 1;
            }
        }
                
        
        
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
    "vcffrequencyenv1mod",
    "vcffrequencypressuremod",
    "vca1velmod",
    "vca2env2mod",
    "env1amplitudemod",
    "env2amplitudemod",
    "env3amplitudemod",
    "lfo1amplitudemod",
    "lfo2amplitudemod",
    "portamentomod",
    "vcffmenv3mod",
    "vcffmpressuremod",
    "lfo1speedmod",
    "lfo2speedmod",
    "mod1source",
    "mod1amount",
    "mod1destination",
    "mod2source",
    "mod2amount",
    "mod2destination",
    "mod3source",
    "mod3amount",
    "mod3destination",
    "mod4source",
    "mod4amount",
    "mod4destination",
    "mod5source",
    "mod5amount",
    "mod5destination",
    "mod6source",
    "mod6amount",
    "mod6destination",
    "mod7source",
    "mod7amount",
    "mod7destination",
    "mod8source",
    "mod8amount",
    "mod8destination",
    "mod9source",
    "mod9amount",
    "mod9destination",
    "mod10source",
    "mod10amount",
    "mod10destination"
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
        if (key.equals("dco1bend") || key.equals("dco1vibrato"))
            {
            index = ((Integer)(internalParametersToIndex.get("dco1fixedmods1"))).intValue();
            value = model.get("dco1bend") |  (model.get("dco1vibrato") << 1);
            }
        else if (key.equals("dco1portamento"))
            {
            index = ((Integer)(internalParametersToIndex.get("dco1fixedmods2"))).intValue();
            value = model.get("dco1portamento");
            }
        else if (key.equals("dco2bend") || key.equals("dco2vibrato"))
            {
            index = ((Integer)(internalParametersToIndex.get("dco2fixedmods1"))).intValue();
            value = model.get("dco2bend") | (model.get("dco2vibrato") << 1);
            }
        else if (key.equals("dco2portamento") || key.equals("dco2keytracking"))
            {
            index = ((Integer)(internalParametersToIndex.get("dco2fixedmods2"))).intValue();
            value = model.get("dco2portamento") | (model.get("dco2keytracking") << 1);
            }
        else if (key.equals("dco1wave") || key.equals("dco1pulse"))
            {
            index = ((Integer)(internalParametersToIndex.get("dco1waveenable"))).intValue();
            value = model.get("dco1wave") | (model.get("dco1pulse") << 1);
            }
        else if (key.equals("dco2wave") || key.equals("dco2pulse") || key.equals("dco2noise"))
            {
            index = ((Integer)(internalParametersToIndex.get("dco2waveenable"))).intValue();
            value = model.get("dco2wave") | (model.get("dco2pulse") << 1) | (model.get("dco2noise") << 2);
            }
        else if (key.equals("vcfbend") || key.equals("vcfvibrato"))
            {
            index = ((Integer)(internalParametersToIndex.get("vcffixedmods1"))).intValue();
            value = model.get("vcfbend") | (model.get("vcfvibrato") << 1);
            }
        else if (key.equals("vcfportamento") || key.equals("vcfkeytracking"))
            {
            index = ((Integer)(internalParametersToIndex.get("vcffixedmods2"))).intValue();
            value = model.get("vcfportamento") | (model.get("vcfkeytracking") << 1);
            }
        else if (key.equals("dco2detune"))
            {
            index = ((Integer)(internalParametersToIndex.get(key))).intValue();
            value = convertToSixBitsSigned(model.get(key));
            }
        else if (key.endsWith("mod"))  // 7 bit signed
            {
            index = ((Integer)(internalParametersToIndex.get(key))).intValue();
            value = convertToSevenBitsSigned(model.get(key));
            }
        else if (key.startsWith("mod"))
            {
            int modnumber = (int)(key.charAt(3) - '0');
            if (key.charAt(4) == '0') // it's 10
                modnumber = 10;

            int modsource = model.get("mod" + modnumber  + "source");
            int moddestination = model.get("mod" + modnumber  + "destination");
            int modamount = convertToSevenBitsSigned(model.get("mod" + modnumber  + "amount"));

            // if one is "None", then the other must be as well            
            if (modsource == 0) moddestination = 0;
            else if (moddestination == 0) modsource = 0;
            
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
            index = ((Integer)(internalParametersToIndex.get(key))).intValue();
            value = model.get(key);
            }
        
        byte VV = (byte)(value);
        byte PP = (byte)(index & 127);
        return new byte[] { (byte)0xF0, 0x10, 0x06, 0x06, PP, VV, (byte)0xF7 };
        }
    

    byte convertFromSixBitsSigned(int val)
        {
        // strip old signed extension in bit 7
        val = (val & 63);
                
        val += 32;
        if (val > 64)
            val -= 64;
        return (byte) val;
        }

    byte convertToSixBitsSigned(int val)
        {
        val -= 32;
        if (val < 0)
            val += 64;

        // do signed extension
        if ((val & 32) == 32)  // 6th bit is set
            val = val | 64;  // set the 7th bit
        else
            val = val & 63;  // clear the 7th bit

        return (byte) val;
        }

    byte convertFromSevenBitsSigned(int val)
        {
        val += 64;
        if (val > 128)
            val -= 128;
        return (byte) val;
        }

    byte convertToSevenBitsSigned(int val)
        {
        val -= 64;
        if (val < 0)
            val += 128;
        return (byte) val;
        }

    /** 
     // Maybe for Matrix 6?  Also this code needs to be updated with
     // parameter conversions and other stuff.
                
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
                
     String key = internalParameters[parameter];
     if (key.equals("dco1fixedmods1"))
     {
     model.set("dco1bend", value & 1);
     model.set("dco1vibrato", (value >> 1) & 1);
     }
     else if (key.equals("dco1fixedmods2"))
     {
     model.set("dco1portamento", value & 1);
     }
     else if (key.equals("dco2fixedmods1"))
     {
     model.set("dco2bend", value & 1);
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
     model.set("vcfbend", value & 1);
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
     else if (key.equals("dco2detune"))
     {
     value = convertFromSixBitsSigned(value);
     model.set("dco2detune", value);
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
    */
        
    public boolean parse(byte[] data, boolean ignorePatch, boolean fromFile)
        {
        //  packing by two nibbles per byte (see http://www.youngmonkey.ca/nose/audio_tech/synth/Oberheim-OberheimMatrix1000.html)
        
        byte[] name = new byte[8];
        
        // we don't know the bank, just the number.  :-(
        int number = data[4];
        if (!ignorePatch)
            model.set("number", number);
                        
        for(int i = 0; i < 134; i++)
            {
            String key = allParameters[i];

            // unpack from nybbles
            byte lonybble = data[i * 2 + 5];
            byte hinybble = data[i * 2 + 5 + 1];
            byte value = (byte)(((hinybble << 4) | (lonybble & 15)) & 127);

            if (i < 8)  // it's the name
                name[i] = unpackNameByte(value);
            else if (key.equals("dco1fixedmods1"))
                {
                model.set("dco1bend", value & 1);
                model.set("dco1vibrato", (value >> 1) & 1);
                }
            else if (key.equals("dco1fixedmods2"))
                {
                model.set("dco1portamento", value & 1);
                }
            else if (key.equals("dco2fixedmods1"))
                {
                model.set("dco2bend", value & 1);
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
                model.set("vcfbend", value & 1);
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
                model.set(key, value);
                }
                
            // Note Bug in Matrix 1000 will fill in bit *8* with a 1 (this is possible because it's split into two nybbles).
            // It's okay because convertFromSixBitsSigned(...) strips out both bits 7 and 8.  But it means that the dumps
            // won't be the same.
                        
            else if (key.equals("dco2detune"))
                {
                value = convertFromSixBitsSigned(value);
                model.set(key, value);
                }
            else if (key.endsWith("mod"))  // 7 bit signed
                {
                value = convertFromSevenBitsSigned(value);
                model.set(key, value);
                }
            else if (key.startsWith("mod") && key.endsWith("amount"))
                {
                value = convertFromSevenBitsSigned(value);
                model.set(key, value);
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
                
        // to get the bank, we'll extract it from the name.  It appears to be the fourth character
        int bank = name[3] - '0';
        if (bank >= 0 && bank <= 9)  // we're okay
            model.set("bank", bank);
        
        
        revise();
        return true;            // change this as appropriate
        }
    

    public byte[] emit(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        if (tempModel == null)
            tempModel = getModel();

        byte[] data = new byte[268];
        String nm = model.get("name", "UNTITLED") + "        ";
        byte[] name = null;
        try { name = nm.getBytes("US-ASCII"); } catch (Exception e ) { }
        int value;
        byte check = 0;
                
        for(int i = 0; i < 134; i++)
            {
            String key = allParameters[i];
                
            if (i < 8)  // it's the name
                value = packNameByte(name[i]);
            else if (key.equals("dco1fixedmods1"))
                {
                value = (model.get("dco1vibrato") << 1) |
                    (model.get("dco1bend"));
                }
            else if (key.equals("dco1fixedmods2"))
                {
                value = (model.get("dco1portamento"));
                }
            else if (key.equals("dco2fixedmods1"))
                {
                value = (model.get("dco2vibrato") << 1) |
                    (model.get("dco2bend"));
                }
            else if (key.equals("dco2fixedmods2"))
                {
                value = (model.get("dco2keytracking") << 1) |
                    (model.get("dco2portamento"));
                }
            else if (key.equals("dco1waveenable"))
                {
                value = (model.get("dco1pulse") << 1) |
                    (model.get("dco1wave"));
                }
            else if (key.equals("dco2waveenable"))
                {
                value = (model.get("dco2noise") << 2) |
                    (model.get("dco2pulse") << 1) |
                    (model.get("dco2wave"));
                }
            else if (key.equals("vcffixedmods1"))
                {
                value = (model.get("vcfvibrato") << 1) |
                    (model.get("vcfbend"));
                }
            else if (key.equals("vcffixedmods2"))
                {
                value = (model.get("vcfkeytracking") << 1) |
                    (model.get("vcfportamento"));
                }
            else if (key.equals("dco2detune"))      // 6 bit signed
                {
                value = convertToSixBitsSigned(model.get(key));
                }
            else if (key.endsWith("mod"))  // 7 bit signed
                {
                value = convertToSevenBitsSigned(model.get(key));
                }
            // Note: no need to handle portamentomode specially, but we DO have to parse it specially
                        
            // Ugh, all this below is to deal with the source=destination=0 requirement.  Yuck.
                
            else if (key.equals("mod1source") || key.equals("mod1destination"))
                {
                value = model.get(key);
                if (model.get("mod1source") == 0 || model.get("mod1destination") == 0)
                    value = 0;
                }
            else if (key.equals("mod2source") || key.equals("mod2destination"))
                {
                value = model.get(key);
                if (model.get("mod2source") == 0 || model.get("mod2destination") == 0)
                    value = 0;
                }
            else if (key.equals("mod3source") || key.equals("mod3destination"))
                {
                value = model.get(key);
                if (model.get("mod3source") == 0 || model.get("mod3destination") == 0)
                    value = 0;
                }
            else if (key.equals("mod4source") || key.equals("mod4destination"))
                {
                value = model.get(key);
                if (model.get("mod4source") == 0 || model.get("mod4destination") == 0)
                    value = 0;
                }
            else if (key.equals("mod5source") || key.equals("mod5destination"))
                {
                value = model.get(key);
                if (model.get("mod5source") == 0 || model.get("mod5destination") == 0)
                    value = 0;
                }
            else if (key.equals("mod6source") || key.equals("mod6destination"))
                {
                value = model.get(key);
                if (model.get("mod6source") == 0 || model.get("mod6destination") == 0)
                    value = 0;
                }
            else if (key.equals("mod7source") || key.equals("mod7destination"))
                {
                value = model.get(key);
                if (model.get("mod7source") == 0 || model.get("mod7destination") == 0)
                    value = 0;
                }
            else if (key.equals("mod8source") || key.equals("mod8destination"))
                {
                value = model.get(key);
                if (model.get("mod8source") == 0 || model.get("mod8destination") == 0)
                    value = 0;
                }
            else if (key.equals("mod9source") || key.equals("mod9destination"))
                {
                value = model.get(key);
                if (model.get("mod9source") == 0 || model.get("mod9destination") == 0)
                    value = 0;
                }
            else if (key.equals("mod10source") || key.equals("mod10destination"))
                {
                value = model.get(key);
                if (model.get("mod10source") == 0 || model.get("mod10destination") == 0)
                    value = 0;
                }
            else if (key.startsWith("mod") && key.endsWith("amount"))       // 7 bits signed
                {
                value = convertToSevenBitsSigned(model.get(key));
                }
            else
                {
                value = model.get(key);
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
            // I think this means to add into a byte, and then mask to 127.
            
            check += value;
                
            // write
            data[i * 2] = lonybble;
            data[i * 2 + 1] = hinybble;
            }
    
        byte checksum = (byte)(check & 127);
        byte[] d = new byte[275];
        d[0] = (byte)0xF0;
        d[1] = (byte)0x10;
        d[2] = (byte)0x06;

        if (toWorkingMemory)
            {
            // 0DH - SINGLE PATCH DATA TO EDIT BUFFER
            d[3] = (byte)0x0D;
            d[4] = (byte)0x00;
            }
        else
            {
            // 01H-SINGLE PATCH DATA
            d[3] = (byte)0x01;
            d[4] = (byte)model.get("number");
            }

        System.arraycopy(data, 0, d, 5, 268);
        d[273] = checksum;
        d[274] = (byte)0xF7;
                
        return d;
        }
        
        
        
    public void changePatch(Model tempModel)
        {
        // first change the bank
                
        // 0AH - SET BANK
        // we write this store-command as a sysex command 
        // so it gets stripped when we do a save to file
        byte[] data2 = new byte[6];
        data2[0] = (byte)0xF0;
        data2[1] = (byte)0x10;
        data2[2] = (byte)0x06;  
        data2[3] = (byte)0x0A;
        data2[4] = (byte)(tempModel.get("bank"));
        data2[5] = (byte)0xF7;

        tryToSendSysex(data2);

        // 0CH - UNLOCK BANK
        // we write this store-command as a sysex command 
        // so it gets stripped when we do a save to file
        // annoying that this gets re-locked by SET BANK
        byte[] data = new byte[5];
        data2[0] = (byte)0xF0;
        data2[1] = (byte)0x10;
        data2[2] = (byte)0x06;  
        data2[3] = (byte)0x0C;
        data2[4] = (byte)0xF7;
                        
        // Next do a program change
                
        byte NN = (byte)tempModel.get("number");
        tryToSendMIDI(buildPC(getChannelOut(), NN));
        }


    public byte[] requestDump(Model tempModel)
        {               
        // Next do a dump request
        byte[] data = new byte[7];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x10;
        data[2] = (byte)0x06;
        data[3] = (byte)0x04;
        data[4] = (byte)0x01;           // request single patch
        data[5] = (byte)(tempModel.get("number"));
        data[6] = (byte)0xF7;
        return data;
        }
                
    public static final int EXPECTED_SYSEX_LENGTH = 275;        
        
    public static boolean recognize(byte[] data)
        {
        boolean v = (
            // The Matrix 1000 doesn't transmit the checksum!
            // So it could be one of two lengths:
                (data.length == EXPECTED_SYSEX_LENGTH ||
                data.length == EXPECTED_SYSEX_LENGTH - 1) &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x10 &&
            data[2] == (byte)0x06 &&
            (data[3] == (byte)0x01 || data[3] == (byte)0x0d));
        return v;
        }
        

    public static final int MAXIMUM_NAME_LENGTH = 8;
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
        
        String nm = model.get("name", "UNTITLED");
        String newnm = revisePatchName(nm);
        if (!nm.equals(newnm))
            model.set("name", newnm);
        }
        
    public int getPauseBetweenMIDISends() { return 50; }

    public static String getSynthName() { return "Oberheim Matrix 1000"; }
    
    public String getPatchName() { return model.get("name", "UNTITLED"); }
    
    }
