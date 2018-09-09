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
                
        SynthPanel soundPanel = new SynthPanel(this);
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        hbox.addLast(addOscillatorGlobal(Style.COLOR_A()));
        vbox.add(hbox);
        
        vbox.add(addOscillator(1, Style.COLOR_A()));
        vbox.add(addOscillator(2, Style.COLOR_A()));
        
        hbox = new HBox();
        hbox.add(addFilter(Style.COLOR_C()));
        hbox.addLast(addAmplifier(Style.COLOR_C()));
        vbox.add(hbox);
        

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Oscillators and Filters", soundPanel);
                
                
        // ENVELOPE PANEL
                
        SynthPanel envelopePanel = new SynthPanel(this);
        vbox = new VBox();

        hbox = new HBox();
        hbox.add(addLFO(1, Style.COLOR_A()));
        hbox.addLast(addRamp(1, Style.COLOR_A()));
        vbox.add(hbox);
                
        hbox = new HBox();
        hbox.add(addLFO(2, Style.COLOR_A()));
        hbox.addLast(addRamp(2, Style.COLOR_A()));
        vbox.add(hbox);
                        
        vbox.add(addEnvelope(1,Style.COLOR_B()));
        vbox.add(addEnvelope(2,Style.COLOR_B()));
        vbox.add(addEnvelope(3,Style.COLOR_B()));
        
        envelopePanel.add(vbox, BorderLayout.CENTER);
        addTab("LFOs and Envelopes", envelopePanel);

        
        
        // MODULATION PANEL
                
        SynthPanel modulationPanel = new SynthPanel(this);
        
        vbox = new VBox();
        vbox.add(addModulation(Style.COLOR_A()));
        vbox.add(addTracking(Style.COLOR_B()));
                

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
        JTextField val = new JTextField("" + model.get("bank") + (model.get("number") < 10 ? "0" : "") + model.get("number"), 3);
        
        while(true)
            {
            boolean result = showMultiOption(this, new String[] { "Patch Number"}, 
                new JComponent[] { val }, title, "Enter the 3-digit Patch Number");
                
            if (result == false) 
                return false;
                                
            int n;
            try { n = Integer.parseInt(val.getText()); }
            catch (NumberFormatException e)
                {
                showSimpleError(title, "The 3-digit Patch Number must be an integer 0...999");
                continue;
                }
            if (n < 0 || n > 999)
                {
                showSimpleError(title, "The 3-digit Patch Number must be an integer 0...999");
                continue;
                }
                                
            change.set("bank", n / 100);
            change.set("number", n % 100);
                        
            return true;
            }
        }
        
    public JFrame sprout()
        {
        JFrame frame = super.sprout();
        transmitTo.setEnabled(false);
        addOberheimMenu();
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
        globalCategory.makeUnresettable();
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
                                
        VBox vbox = new VBox();
        comp = new PatchDisplay(this, 4);
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

        comp = new LabelledDial("Mix", this, "mix", color, 0, 63, 31)  // yes, there are *64* values, but 31 is center.  It's not quite symmetric.
            {
            public double getStartAngle()
                {
                return (270 / 2) * (31.5 / 64.0) * 2  + 90;
                }
                
            public int getDefaultValue() { return 31; }

            public String map(int val)
                {
                if (val == 31) return "--";
                else if (val < 31) return "< " + (31 - val);
                else return "" + (val - 31) + " >";
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("DCO 2 <> 1");
        hbox.add(comp);

        comp = new LabelledDial("Portamento", this, "portamento", color, 0, 63);
        ((LabelledDial)comp).addAdditionalLabel("Rate");
        hbox.add(comp);

        comp = new LabelledDial("Portamento", this, "portamentomod", color, -63, 63);
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
        category.makePasteable("dco" + osc);

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
                
        comp = new LabelledDial("Wave Shape", this, "dco" + osc + "shape", color, 0, 63)
            {
            public boolean isSymmetric() { return true; }
            };
        ((LabelledDial)comp).addAdditionalLabel("Saw <> Tri");
        hbox.add(comp);

        comp = new LabelledDial("Frequency", this, "dco" + osc + "frequency", color, 0, 63);
        hbox.add(comp);
                
        comp = new LabelledDial("Frequency", this, "dco" + osc + "frequencymod", color, -63, 63);
        ((LabelledDial)comp).addAdditionalLabel("LFO 1 Mod");
        hbox.add(comp);

        comp = new LabelledDial("Pulse Width", this, "dco" + osc + "pulsewidth", color, 0, 63)
            {
            public boolean isSymmetric() { return true; }
            public int getDefaultValue() { return 31; }
            };
        hbox.add(comp);

        comp = new LabelledDial("Pulse Width", this, "dco" + osc + "pulsewidthmod", color, -63, 63)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("LFO 2 Mod"); 
        hbox.add(comp);

        if (osc==2)
            {
            comp = new LabelledDial("Detune", this, "dco" + osc + "detune", color, -31, 31)
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
                
        comp = new LabelledDial("Frequency", this, "vcffrequencyenv1mod", color, -63, 63);
        ((LabelledDial)comp).addAdditionalLabel("Env 1 Mod");
        hbox.add(comp);
                
        comp = new LabelledDial("Frequency", this, "vcffrequencypressuremod", color, -63, 63);
        ((LabelledDial)comp).addAdditionalLabel("Press Mod");
        hbox.add(comp);
        
        comp = new LabelledDial("Resonance", this, "vcfresonance", color, 0, 63);
        hbox.add(comp);
                
        comp = new LabelledDial("FM", this, "vcffm", color, 0, 63);
        hbox.add(comp);

        comp = new LabelledDial("FM", this, "vcffmenv3mod", color, -63, 63);
        ((LabelledDial)comp).addAdditionalLabel("Env 3 Mod");
        hbox.add(comp);

        comp = new LabelledDial("FM", this, "vcffmpressuremod", color, -63, 63);
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
        
        comp = new LabelledDial("VCA 1", this, "vca1", color, 0, 63);
        ((LabelledDial)comp).addAdditionalLabel("Volume");
        hbox.add(comp);

        comp = new LabelledDial("VCA 1", this, "vca1velmod", color, -63, 63);
        ((LabelledDial)comp).addAdditionalLabel("Volume");
        ((LabelledDial)comp).addAdditionalLabel("Vel Mod");
        hbox.add(comp);
                
        comp = new LabelledDial("VCA2", this, "vca2env2mod", color, -63, 63);
        ((LabelledDial)comp).addAdditionalLabel("Volume");
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
        category.makePasteable("lfo" + lfo);
                
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
                
        comp = new LabelledDial("Amplitude", this, "lfo" + lfo + "amplitudemod", color, -63, 63);
        ((LabelledDial)comp).addAdditionalLabel("Ramp " + lfo + " Mod");
        hbox.add(comp);
                
        comp = new LabelledDial("Speed", this, "lfo" + lfo + "speed", color, 0, 63);
        hbox.add(comp);

        comp = new LabelledDial("Speed", this, "lfo" + lfo + "speedmod", color, -63, 63);
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
        category.makePasteable("env" + env);
                
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
        dadr[env - 1] = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "env" + env + "delay", "env" + env + "attack", "env" + env + "decay", "env" + env + "release" },
            new String[] { null, null, null, "env" + env + "sustain", null },
            new double[] { 0, 0.25/63.0, 0.25/63.0, 0.25 / 63.0, 0.25/63.0},
            new double[] { 0, 0, 1.0, 1.0 / 63.0, 0 });

        // DADSR
        dadsr[env - 1] = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
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

        comp = new LabelledDial("Amplitude", this, "env" + env + "amplitudemod", color, -63, 63);
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
        category.makeDistributable("trackingpoint");
                
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


        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
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
        category.makePasteable("ramp" + ramp);
                
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
        category.makeDistributable("mod");
                        
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

                comp = new LabelledDial("" + i + " Amount", this, "mod" + i + "amount", color, -63, 63);  // it's Level, not Amount, so we save some horizontal space
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
        else if (key.equals("env1lfotriggermode") || key.equals("env2lfotriggermode") || key.equals("env3lfotriggermode"))
            {
            index = ((Integer)(internalParametersToIndex.get(key))).intValue();
            value = model.get(key);
            // convert
            if (value >= 1) value = value + 1;  // there is no value = 1, that's the same as value = 0
            }
        else if (key.equals("dco1bend") || key.equals("dco1vibrato"))
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
            value = model.get("dco1pulse") | (model.get("dco1wave") << 1);
            }
        else if (key.equals("dco2wave") || key.equals("dco2pulse") || key.equals("dco2noise"))
            {
            index = ((Integer)(internalParametersToIndex.get("dco2waveenable"))).intValue();
            value = model.get("dco2pulse") | (model.get("dco2wave") << 1) | (model.get("dco2noise") << 2);
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
            value = model.get(key) & 127;  // sign-extend to 7th bit only
            }
        else if (key.startsWith("mod"))
            {
            int modnumber = (int)(key.charAt(3) - '0');
            if (key.charAt(4) == '0') // it's 10
                modnumber = 10;

            int modsource = model.get("mod" + modnumber  + "source");
            int moddestination = model.get("mod" + modnumber  + "destination");
            int modamount = model.get("mod" + modnumber  + "amount") & 127;

            // if one is "None", then the other must be as well            
            if (modsource == 0) moddestination = 0;
            else if (moddestination == 0) modsource = 0;
            
            modnumber--;

            return new byte[] { (byte)0xF0, 0x10, 0x06, 0x0B, (byte)modnumber, (byte)modsource, (byte) modamount, (byte)moddestination, (byte)0xF7 };
            }
        else if (key.equals("trackingsource"))
            {
            index = ((Integer)(internalParametersToIndex.get(key))).intValue();
            value = model.get(key) + 1;  // tracking source has no "none"
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
        
        byte VV = (byte)(value & 127);
        byte PP = (byte)(index & 127);
        return new byte[] { (byte)0xF0, 0x10, 0x06, 0x06, PP, VV, (byte)0xF7 };
        }
    

    /// ERRORS IN MIDI SYSEX DESCRIPTION
    ///
    /// Though they're listed as "six bit (signed)" or "seven bit (signed)", all signed values
    /// are actually stored as signed 8-bit.  Six-bit signed values are just plain signed bytes
    /// which range from -32 to +31.  Similarly, 7-bit signed values are just plain signed bytes
    /// which range from -64 to +63.  When emitting or parsing a patch, the nybblization just breaks
    /// the byte into two nybbles and that's all.
    ///
    /// Note however that when sending INDIVIDUAL PARAMETERS, the sysex value is firsty masked to 
    /// 7 bits (& 127).  And in NRPN, all values, even unsigned ones, have 64 added to them to 
    /// push them to 0...127.
    
    public int parse(byte[] data, boolean fromFile)
        {
        //  packing by two nibbles per byte (see http://www.youngmonkey.ca/nose/audio_tech/synth/Oberheim-OberheimMatrix1000.html)
        
        byte[] name = new byte[8];
        
        // we don't know the bank, just the number.  :-(
        int number = data[4];
        model.set("number", number);
                        
        for(int i = 0; i < 134; i++)
            {
            String key = allParameters[i];

            // unpack from nybbles
            byte lonybble = data[i * 2 + 5];
            byte hinybble = data[i * 2 + 5 + 1];
            byte value = (byte)(((hinybble << 4) | (lonybble & 15)));

            if (i < 8)  // it's the name
                name[i] = unpackNameByte(value);
            else if (key.equals("env1lfotriggermode") || key.equals("env2lfotriggermode") || key.equals("env3lfotriggermode"))
                {
                // there is no value = 1, that's the same as value = 0
                if (value >= 1) value = (byte)(value - 1);
                }
            else if (key.equals("dco1fixedmods1"))
                {
                model.set("dco1bend", value & 1);
                model.set("dco1vibrato", (value >>> 1) & 1);
                }
            else if (key.equals("dco1fixedmods2"))
                {
                model.set("dco1portamento", value & 1);
                }
            else if (key.equals("dco2fixedmods1"))
                {
                model.set("dco2bend", value & 1);
                model.set("dco2vibrato", (value >>> 1) & 1);
                }
            else if (key.equals("dco2fixedmods2"))
                {
                model.set("dco2portamento", value & 1);
                model.set("dco2keytracking", (value >>> 1) & 1);
                }
            else if (key.equals("dco1waveenable"))
                {
                model.set("dco1pulse", value & 1);
                model.set("dco1wave", (value >>> 1) & 1);
                }
            else if (key.equals("dco2waveenable"))
                {
                model.set("dco2pulse", value & 1);
                model.set("dco2wave", (value >>> 1) & 1);
                model.set("dco2noise", (value >>> 2) & 1);
                }
            else if (key.equals("vcffixedmods1"))
                {
                model.set("vcfbend", value & 1);
                model.set("vcfvibrato", (value >>> 1) & 1);
                }
            else if (key.equals("vcffixedmods2"))
                {
                model.set("vcfportamento", value & 1);
                model.set("vcfkeytracking", (value >>> 1) & 1);
                }
            else if (key.equals("portamentomode"))
                {
                if (value == 4)
                    value = (byte)3;  // get rid of extra exponential
                model.set(key, value);
                }
            else if (key.equals("trackingsource"))
                {
                if (value > 0)  // Some Matrix 1000 patches have the source set to 0 even though it's not supposed to be!
                    model.set(key, (value - 1));  // tracking source has no "none"
                else
                    System.err.println("Warning (OberheimMatrix1000): Tracking Source was incorrectly 0.  Setting to 1.");
                }
            else
                {
                model.set(key, value);
                }
            }
        
        // to get the bank, we'll extract it from the name.  It appears to be the fourth character
        int bank = name[3] - '0';
        if (bank < 0 || bank > 9)
            bank = 0;
        model.set("bank", bank);
        
        if (!fromFile && useClassicPatchNames)
            {
            model.set("name", PATCH_NAMES[bank * 100 + number]);                    
            }
        else
            {
            // update name just for fun, it may be gibberish
            try 
                {
                model.set("name", new String(name, "US-ASCII"));
                }
            catch (UnsupportedEncodingException e)
                {
                e.printStackTrace();
                }
            }
                
        revise();
        return PARSE_SUCCEEDED;
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
            else if (key.equals("env1lfotriggermode") || key.equals("env2lfotriggermode") || key.equals("env3lfotriggermode"))
                {
                value = model.get(key);
                // convert
                if (value >= 1) value = value + 1;  // there is no value = 1, that's the same as value = 0
                }
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
                value = (model.get("dco1wave") << 1) |
                    (model.get("dco1pulse"));
                }
            else if (key.equals("dco2waveenable"))
                {
                value = (model.get("dco2noise") << 2) |
                    (model.get("dco2wave") << 1) |
                    (model.get("dco2pulse"));
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
            else if (key.equals("trackingsource"))
                {
                value = model.get(key) + 1;  // tracking source has no "none"
                }
            else
                {
                value = model.get(key);
                }
            
            // pack to nybbles
            
            if (value < 0) value += 256;  // so we're positive.
            byte lonybble = (byte)(value & 15);
            byte hinybble = (byte)((value >>> 4) & 15);
            
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
        changePatch(tempModel.get("bank"), tempModel.get("number"));
        }


    public void changePatch(int bank, int number)
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
        data2[4] = (byte)(bank);
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
                
        byte NN = (byte)number;
        tryToSendMIDI(buildPC(getChannelOut(), NN));
        }



    public byte[] requestCurrentDump()
        {               
        byte[] data = new byte[7];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x10;
        data[2] = (byte)0x06;
        data[3] = (byte)0x04;
        data[4] = (byte)0x04;           // request edit buffer
        data[5] = (byte)0x00;
        data[6] = (byte)0xF7;
        return data;
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
        
    boolean sendingMatrix100Parameters = false;
    public double getPauseBetweenMIDISends() { if (sendingMatrix100Parameters) return 75; else return 0; }

    public static String getSynthName() { return "Oberheim Matrix 1000"; }
    
    public String getPatchName(Model model) { return model.get("name", "UNTITLED"); }
    
    public int getPauseAfterSendAllParameters() { return 200; }
    
    
    public Model getNextPatchLocation(Model model)
        {
        int bank = model.get("bank");
        int number = model.get("number");
        
        number++;
        if (number >= 100)
            {
            bank++;
            number = 0;
            if (bank >= 10)
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
        
        int number = model.get("number");
        return ("" + model.get("bank")) + 
            (number > 9 ? "" : "0") + 
            (model.get("number"));
        }

    ///// A bug in the Matrix 1000 means that SINGLE PATCH DATA TO EDIT BUFFER apparently sends
    ///// corrupted data.  So we can't use it.  But we still need to send!  So we do this by 
    ///// writing to slot 199 when sending in bulk.  We have to modify sendAllParameters so that if
    ///// we're sending in bulk, we change the patch to 199 first so that we always have the
    ///// Matrix 1000 set up right.

    public static final int SEND_MATRIX_NUMBER = 99;
    public static final int SEND_MATRIX_BANK = 1;
    public boolean sendMatrixParametersInBulk = true;  // always for now

    public boolean getSendsAllParametersInBulk() { return sendMatrixParametersInBulk; }

    public void sendAllParameters()
        {
        // in case we send parameters individually, we'll add a pause between sending parameters here.
        sendingMatrix100Parameters = true;
        /*
          if (sendMatrixParametersInBulk)
          {
          // we need to ensure a changepatch to SEND_MATRIX_SLOT here
          changePatch(SEND_MATRIX_BANK, SEND_MATRIX_NUMBER);
          }
        */
        super.sendAllParameters();
        // now we turn off the pause
        sendingMatrix100Parameters = false;
        }

    public boolean useClassicPatchNames = true;

    public void addOberheimMenu()
        {
        JMenu menu = new JMenu("Matrix 1000");
        menubar.add(menu);

        // classic patch names
                
        JCheckBoxMenuItem useClassicPatchNamesMenu = new JCheckBoxMenuItem("Use Classic Patch Names");
        menu.add(useClassicPatchNamesMenu);

        useClassicPatchNamesMenu.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt)
                {
                useClassicPatchNames = useClassicPatchNamesMenu.isSelected();
                setLastX("" + useClassicPatchNames, "UseClassicPatchNames", getSynthName(), true);
                }
            });
        
        String str = getLastX("UseClassicPatchNames", getSynthName(), true);
        if (str == null)
            useClassicPatchNames = true;
        else if (str.equalsIgnoreCase("true"))
            useClassicPatchNames = true;
        else useClassicPatchNames = false;
        
        useClassicPatchNamesMenu.setSelected(useClassicPatchNames);
        
        menu.addSeparator();

        // load patch
        for(int i = 0; i < 1000; i += 50)
            {
            JMenu patchgroup = new JMenu("" + "Request Patch " + (i < 100 ? (i < 10 ? "00" : "0") : "" ) + i + "..." + (i < 100 ? "0" : "") + (i + 49));
            menu.add(patchgroup);
            for(int j = i; j < i + 50; j++)
                {
                final int _j = j;
                JMenuItem patch = new JMenuItem("" + 
                    (j < 100 ? (j < 10 ? "00" + j : "0" + j) : "" + j) + ": " + 
                    PATCH_NAMES[j]);
                patch.addActionListener(new ActionListener()
                    {
                    public void actionPerformed(ActionEvent evt)
                        {
                        Model tempModel = buildModel();
                        tempModel.set("number", _j % 100);
                        tempModel.set("bank", _j / 100);
                        performRequestDump(tempModel, true);
                        }
                    });
                patchgroup.add(patch);
                }
            }


        /*
        // we don't call this for the time being -- sending individual parameters is slow and fraught with problems
        JMenu sendParameters = new JMenu("Send Parameters...");
        menu.add(sendParameters);
                
        String str = getLastX("SendParameters", getSynthName(), true);
        if (str == null)
        sendMatrixParametersInBulk = true;
        else if (str.equalsIgnoreCase("BULK"))
        sendMatrixParametersInBulk = true;
        else if (str.equalsIgnoreCase("INDIVIDUALLY"))
        sendMatrixParametersInBulk = false;
        else sendMatrixParametersInBulk = true;

        ButtonGroup bg = new ButtonGroup();
        JRadioButtonMenuItem bulk = new JRadioButtonMenuItem("In Bulk, using Patch 199");
        bulk.addActionListener(new ActionListener()
        {
        public void actionPerformed(ActionEvent evt)
        {
        sendMatrixParametersInBulk = true;
        setLastX("BULK", "SendParameters", getSynthName(), true);
        }
        });
        sendParameters.add(bulk);
        bg.add(bulk);
        if (sendMatrixParametersInBulk == true) bulk.setSelected(true);
                
        JRadioButtonMenuItem separately = new JRadioButtonMenuItem("As Individual Parameters");
        separately.addActionListener(new ActionListener()
        {
        public void actionPerformed(ActionEvent evt)
        {
        sendMatrixParametersInBulk = false;
        setLastX("INDIVIDUALLY", "SendParameters", getSynthName(), true);
        }
        });
        sendParameters.add(separately);
        bg.add(separately);
        if (sendMatrixParametersInBulk == false) separately.setSelected(true);
        */
        }
        
        
    // These are drawn from the "Matrix 1000 Patchbook"
    public static final String[] PATCH_NAMES = 
        {
        "TOTOHORN",
        "1000STRG",
        "MOOOG_B",
        "EZYBRASS",
        "SYNTH",
        "MIBES",
        "CHUNK",
        "MINDSEAR",
        "CASTILLO",
        "DESTROY+",
        "BIG PIK",
        "M-CHOIR",
        "STRINGME",
        ")LIQUID(",
        "PNO-ELEC",
        "BED TRAK",
        "STELLAR",
        "SYNCAGE",
        "SHIVERS",
        "+ ZETA +",
        "STEELDR.",
        "TAURUS",
        "POWRSOLO",
        "INTERSTL",
        "REZTFUL",
        "WATRLNG",
        "BEELS",
        "LIKETHIS",
        "NTHENEWS",
        "SOFT MIX",
        "OBXA-A7",
        "BREATH",
        "MUTRONO",
        "SLOWATER",
        "HAUNTING",
        "FLANGED",
        "TENSION",
        "ECHOTRON",
        "PIRATES!",
        "EP SWEP",
        "DEJAVUE'",
        "DRAMA",
        "VIOLINCE",
        "BOUNCE",
        "SAGAN'Z",
        "OB LEAD",
        "FEEDGIT",
        "SAMPLE",
        "TINYPIAN",
        "GALACTIC",
        "DOU CIEL",
        "WA CLAV",
        "DREAMER",
        "XA STR",
        "CHURCH",
        "KIDDING?",
        "THUNDER",
        "ECHOWURL",
        "BLABINET",
        "STRUNGS",
        "AFRICAN",
        "B3+LSLIE",
        "CHIMES",
        "DIPIAN",
        "LAZ HARP",
        "SMTHSQ2",
        "TRUMPETS",
        "PAPANO 4",
        "WIPBASS",
        "LYLE-8VA",
        "SITAR",
        "VIOGITAR",
        "GOLIATH",
        "ANAXYLO",
        "FURY0",
        "SYNLUTH",
        "CHAMBER",
        "SPATBRS",
        "ETHEREE",
        "TBRAZZ",
        "NOBLE",
        "FLEXTONE",
        "GREEZY2",
        "ARPPEGT",
        "JUMP IES",
        "HARDVARK",
        "SWEETSKY",
        "SHIMRING",
        "TIMBOWS",
        "GALLOP",
        "PRELUDE1",
        "GROWLBRZ",
        "CLICKORG",
        "PRESLEZ1",
        "LYLE",
        "ARCANGEL",
        "BENSHIMR",
        "LUSHNESS",
        "NOISTRNG",
        "SOPIPES",
        "PAPANO 4",
        "WURLY 2",
        "TOTOHORN",
        "AGRESORN",
        "STRING S",
        "ODX 7",
        "JUMP IES",
        "PROFIT",
        "VOICES",
        "SAGAN'Z",
        "PA ANO 5",
        "MONSTER",
        "GENVIV",
        "CLAVINET",
        "STRINGER",
        "SIMONISK",
        "HOTBODOM",
        "XTASY",
        ")LIQUID(",
        "BED TRAK",
        "ROADS",
        "ECHOWURL",
        "POLYPHON",
        "DREAMER",
        "HISTRUNG",
        "DEJAVUE'",
        "WET BAZ",
        "PIPE",
        "OCTAVIA",
        "OB-STRGS",
        "KAWHY",
        "JOCKO",
        "FLUTES",
        "ATYPICAL",
        "BOW VIOL",
        "VERTABRA",
        "SLAP 1",
        "P.ORGAN5",
        "OCTAHORN",
        "LUSHNESS",
        "12\"GITAR",
        "STAND UP",
        "BERT'S B",
        "BRASS-11",
        "STRINGME",
        "LEED-2",
        "OW BASS",
        "ORGAN-1",
        "HORNISK",
        "STRING-1",
        "CARIPSO",
        "FAZ BASS",
        "ORGAN-2",
        "PHASEPAD",
        "STRING 2",
        "BATA",
        "BASS SYN",
        "SYNCAGE*",
        "DIGIHORN",
        "CELLO",
        "GLASSVOX",
        "FRET NOT",
        "ORGANISM",
        "LIMUIDZ",
        "STRANGER",
        "PAPANO 7",
        "WOW BASS",
        "P.ORGAN4",
        "MELOHORN",
        "LEED-1",
        "TINYPIAN",
        "SLAP 2",
        "FORESTS",
        "HONOCLAB",
        "STRING 7",
        "VELSYNC*",
        "RAINECHO",
        "ORGAN 9",
        "BRAZZ",
        "ACCORD",
        "DIGPIANO",
        "TENOR",
        "STORTGAN",
        "VIOGITAR",
        "STRUNGET",
        "BIRDLAND",
        "METAL-1",
        "METAL-8",
        "FUNK ART",
        "METAL-13",
        "VIBES",
        "STRING 6",
        "STRNGREZ",
        "SINGS",
        "TIMBOWS",
        "WHISTLE",
        "OBERHORN",
        "TOOTS ?",
        "FIREBALL",
        "SMPLTHIS",
        "OBXA-11",
        "OBXA-12",
        "OBXA-A2",
        "OBXA-A7",
        "OBXA-B7",
        "OBXA-B8",
        "OBXA-C2",
        "OBXA-C4",
        "OBXA-C6",
        "OBXA-C7",
        "OBXA-C8",
        "OBXA-D2",
        "OBXA-D3",
        "OBXA-D4",
        "OBXA-D5",
        "OBXA-D6",
        "OBXA-D7",
        "OBXA-D8",
        "OBXA-9\"",
        "OBCA-RE",
        "OBXJMP",
        "*'ANGEL",
        "+ ZETA +",
        "1984SWP6",
        "WAVES",
        "80MS DDL",
        "SYNTH",
        "AERIAL",
        "ALIENSWP",
        "AMBIANCE",
        "ANAFTST*",
        "ANAHARP",
        "ANALOG B",
        "ANAXYLO*",
        "ANGELS",
        "APOLLO",
        "ARCANGEL",
        "ARGEX-1",
        "ARGON7",
        "ATYPICAL",
        "AW WHY ?",
        "BENSHIMR",
        "BEOWCOMP",
        "BILLY",
        "BLASZZ",
        "BLOCKOUT",
        "BOEPTYN*",
        "BOTTLES",
        "BOUNCE*",
        "BRASSVOX",
        "BRILLANT",
        "BROADWAY",
        "BS ETAK*",
        "BURNHOUS",
        "CAMERA 1",
        "CHIME 1",
        "CHIME 2",
        "CHUNK",
        "CMI HIGH",
        "COEAUR 1",
        "COLONY 9",
        "CRYSLAKE",
        "CS-80",
        "DEACON",
        "DEJAVUE'",
        "DIDIER",
        "DISTANCE",
        "DMACHINE",
        "DREAMER",
        "DREEMER",
        "DUCKTIME",
        "DUNK IT",
        "E N O 1",
        "ECHOSYN",
        "ECHOTRON",
        "EGYPT",
        "EP SWEP*",
        "EPCH+BRZ",
        "EPDSTRT*",
        "ESQ-1",
        "ETHEREE",
        "FAKE DDL",
        "FIFTHS",
        "FLOATONG",
        "FLPFLOP*",
        "FLY TO",
        "FM BASS",
        "FUNDO",
        "*FUNK ART",
        "FUNKAY",
        "FURYO",
        "FWEEP",
        "S.1",
        "GALACTIC",
        "GALLOP *",
        "GENIVEEV",
        "GENVIV",
        "GENVIV*",
        "GIRLSWEP",
        "GOOD BED",
        "GOODTIME",
        "GROTTO",
        "HACKETT",
        "HALO",
        "HARMOVOX",
        "HARPOON",
        "HELI-IN",
        "HOMETOWN",
        "INTERSTL",
        "ITSONICE",
        "JAZZQUIT",
        "JM JARRE",
        "JOHN B'S",
        "KCEPMAX*",
        "KCEPSAW*",
        "KCHSYNC*",
        "KIRKLAND",
        "LDSUBHRM",
        "LIKETHIS",
        "LSTLAUGH",
        "LUN'AIR",
        "M-CHOIR",
        "MAGICAL",
        "MARIN",
        "MATMODUL",
        "MATRIX 1",
        "MATRIX 2",
        "METABOAD",
        "METABRD",
        "MINDSEAR",
        "MONSTER",
        "MR KYRIE",
        "MUSICBOX",
        "NAUTILUS",
        "NEW VOX",
        "NEWSOUND",
        "NIGHTPAD",
        "OB SWEEP",
        "OB VOX*",
        "OB-INTRO",
        "OBXA-B2",
        "OBXA-B4",
        "OCTAVIA",
        "OPEN AIR",
        "ORDINARY",
        "P CHORD",
        "P-PLUCK",
        "PAD",
        "PERC S",
        "PHASE 5",
        "PHASECHO",
        "PICKY",
        "PIPESTR",
        "PN/FMSWP",
        "POLCHOIR",
        "POWER",
        "PROPHET1",
        "PROPHET5",
        "PROPHETV",
        "PSYLITIS",
        "REZ*PULS",
        "REZTFUL",
        "SAMPLE",
        "SATURN",
        "SCIENCE",
        "SCRITTI+",
        "SECRETS",
        "SENSIT 2",
        "SENSITIV",
        "SEQUINOX",
        "SHANKAR",
        "SHIMMER",
        "SHIMRING",
        "SHIVERS",
        "SKRCHTN*",
        "SKTSOKY*",
        "SKY HIGH",
        "SKYVOICE",
        "SLAPBACH",
        "SLIDSTG",
        "SLOW CRY",
        "SLOWATER",
        "SMTHSQ2*",
        "SOFT MIX",
        "SOUNDPAD",
        "SOUNDTR[",
        "SPACE",
        "SPACE/CO",
        "SPARKLES",
        "SPLASH 1",
        "STELLAR*",
        "STUGROWL",
        "STYX",
        "SUBMARIN",
        "SUNDAY",
        "SUSSUDIO",
        "SWEPCORD",
        "SWRLEKO*",
        "SYN BOX*",
        "SYNCAGE*",
        "TOTOHORN",
        "OBXA-13",
        "OBXA-A1",
        "OBXA-B1",
        "OBXA-C1",
        "OBXA-D1",
        "6R BRASS",
        "AGRESORN",
        "ALASKA",
        "ANA HIT*",
        "ANASUTL*",
        "B'ARI/S2",
        "B/D-ANA*",
        "BAGPIPES",
        "BARISAX",
        "BASCLRNT",
        "BASSCLAR",
        "BASSOON",
        "BENDHORN",
        "BIGBRA$$",
        "BONES",
        "BRASRAMP",
        "BTRASSVOX",
        "BRASSY",
        "BRAZEN",
        "BRECHER",
        "BRTH FLT",
        "BRUTUS",
        "BRZIVIV*",
        "BUCHANN*",
        "BUZREED",
        "CHROMA-S",
        "CLARINET",
        "CRAZHORN",
        "CS-80",
        "CUIVRE((",
        "DBLREED*",
        "EASTREED",
        "EDGY",
        "ENSEMBL*",
        "EUROPE",
        "EWF HORN",
        "EZYBRASS",
        "FACTORY",
        "FIFTHS",
        "FLGLHORN",
        "FLOOT",
        "FLUGELHN",
        "FLUGLE",
        "FLUTE",
        "FLUTE TR",
        "FLUTE.",
        "FLUTES",
        "FLUTEY",
        "FLUX",
        "FM BRAZ",
        "FM DELAY",
        "FR.HORN",
        "FNRCHRN*",
        "FTHWEEL*",
        "FUE.JAPN",
        "FUSION",
        "FWEEP",
        "GABRIEL",
        "GO BED",
        "GOLIATH",
        "HORN'EM",
        "HORN-1",
        "HORNENS",
        "HORNFALL",
        "HORNSAS",
        "HORNY",
        "HRNSHAKE",
        "J HAMMER",
        "JTULLFLT",
        "JUBILEE",
        "KLARYNET",
        "KORGHORN",
        "LYRICON",
        "MATRONE",
        "MELFAZE*",
        "MELOHORN",
        "METHENY5",
        "MUTETRPT",
        "OB BRASS",
        "OB-8",
        "OBERHORN",
        "OBOE",
        "OCT.BRS",
        "OCTAFLUT",
        "OCTAHORN",
        "ORIENT",
        "PEDSWP*",
        "PEG-BRS",
        "PYRMFLT*",
        "RAHOOOL*",
        "RECORDER",
        "RELVELHO",
        "RICHCORD",
        "ROMAN",
        "SEXAFOAM",
        "SLO HRN",
        "SOPIPES",
        "SPATBRS*",
        "SQUARDOU",
        "STAB",
        "STAB-BRS",
        "STEPS 2.",
        "STUFLUTE",
        "SWRLYBRD",
        "SYN SAX*",
        "SYNBASS",
        "SYNBONE",
        "SYNBRSS*",
        "SYNHORN",
        "TBRAZZ",
        "TENOR",
        "TOTOAL",
        "TOUCH+GO",
        "TRILLFLT",
        "TRMBONE*",
        "TROMBONE",
        "TRUMPETS",
        "TRUPT-EU",
        "* 99 *",
        "TUBA 2",
        "OBXA-A8",
        "ELEAD*",
        "BDTH-2",
        "BIRDY",
        "BRECKERL",
        "CASTILLO",
        "CHICK",
        "DESTROY+",
        "DIGRUNGE",
        "DRAGON-3",
        "DXINDIAN",
        "FEEDBAK6",
        "FEEDBAK8",
        "FEEDGIT",
        "FIFTH I%",
        "FIFTHLIX",
        "GLASLEED",
        "GROWLBRS",
        "H-LEAD",
        "HILEED 6",
        "J HAMMER",
        "JAKOLEED",
        "JAN LEAD",
        "JAZZ",
        "JIMY'SRG",
        "KC LEAD*",
        "KIDDING?",
        "LEAD+PRT",
        "LEAD-1",
        "LEAD-3",
        "LEED-1",
        "LEED-2",
        "LYLE 2",
        "LYLE 3 M",
        "METHENEY",
        "METLSOLO",
        "MILESCOM",
        "MINIMOGG",
        "MINIMOOG",
        "MONOSTRG",
        "NASTY",
        "OB LEAD*",
        "OSC SYNC",
        "PANFLOET",
        "PINKLEAD",
        "POWRSOLO",
        "PRSSLIDE",
        "QUINCY",
        "RECORDER",
        "REZLEAD*",
        "SAWLEAD*",
        "SITAR",
        "SMOOTH",
        "SMUTHSQ*",
        "SOLO",
        "SOLODARM",
        "SOLOPROF",
        "SOLOSYNC",
        "SOLOW*",
        "SOPIPES",
        "SPITLEED",
        "SQARELED",
        "STUVIB",
        "SUSGUIT",
        "SNTHE 5",
        "UKSOLO",
        "UNIBASS",
        "UNIWAVE",
        "WAKEMANS",
        "WEIRDPRC",
        "WHISTLER",
        "WINAND 1",
        "XA'SOLO",
        "ZAW'QART",
        "OBXA-10",
        "OBXA-14",
        "OBXA-A3",
        "OBXA-B3",
        "OBXA-B6",
        "OBXA-C3",
        "OBXA",
        "OBXA-6",
        "(ARCO)01",
        "*'CANOPY",
        "1000STRG",
        "TOP",
        "2000STRG",
        "AGITATO*",
        "ALL LOVE",
        "ALT84TOP",
        "BED TRAK",
        "BLACSEAM",
        "BOW IT",
        "BOW VIOL",
        "CELLO",
        "CHAMBER",
        "CHILLO",
        "CLASSIKA",
        "CONCERT",
        "DEEPCAVE",
        "DEPTHS",
        "DLAYSTR*",
        "DONSTRIG",
        "DOU'CIEL",
        "DUNGEON",
        "DYNASTY",
        "E.VIOLIN",
        "FAMUS*OB",
        "FORESTS",
        "GRANULES",
        "GREAT\"OB",
        "HARMONIC",
        "ICY-CHRD",
        "INDNSTRG",
        "LOWSTRNG",
        "LOYAL",
        "LUSHNESS",
        "LYLE-8VA",
        "MELLO=14",
        "MKSINGS",
        "MONEY $$",
        "MUTEDSTR",
        "MZSTRING",
        "NOBLE",
        "NOISTGS",
        "OB A3PD*",
        "OB-STR1N",
        "OB-STRGS",
        "OBSTRING",
        "OBXA-A6",
        "OCARINA",
        "OCHESTRY",
        "OPENSTRG",
        "ORCH*",
        "ORIENT",
        "PITZ STR",
        "PIZZ^+P2",
        "PLANET P",
        "POLSTRG2",
        "PROHET-5",
        "PROPHET5",
        "RID ZEP",
        "ROYAL PH",
        "RP STRG5",
        "SECRETS'",
        "SHARPBOW",
        "SHIFT",
        "SHRTSTRG",
        "SINGS",
        "SLIDSTG",
        "SLOW BOW",
        "SLOW CRY",
        "SMASH",
        "SOLEMN",
        "SOLEMNIS",
        "SOLO",
        "SOUNDTR",
        "SOUNDTRK",
        "SOUNDTR[",
        "SRTRONGS",
        "STAND UP",
        "STR END*",
        "STR-8VA",
        "STRANGER",
        "STREENG",
        "STREENGS",
        "STRING 2",
        "STRING 6",
        "STRING 7",
        "STRING 8",
        "STRING S",
        "STRING\"8",
        "STRING-1",
        "STRINGER",
        "**A!A!**",
        "2600-2",
        "AGREBASS",
        "ANTEATER",
        "ARP-2",
        "ATYPBASS",
        "AXXE",
        "BARISAX",
        "BASS PAD",
        "BASS SYN",
        "BASS ZZT",
        "BASS-11",
        "BASSA",
        "BASSCLAR",
        "TUBULAR",
        "BASSE OA",
        "BASSGTAR",
        "BASSHIPO",
        "BASSHORN",
        "BASSVIOL",
        "BASSVOX",
        "BIG PIK",
        "BIRDLAND",
        "BOLUBASS",
        "BOTBASS",
        "BOWBASS",
        "BRAAS",
        "BS/STRG*",
        "UNI BASS",
        "CLAVBASS",
        "BOUBLEBS",
        "DUCK 2",
        "DUCKBASS",
        "EARTHESS",
        "ELC BASS",
        "ELEC BS*",
        "FANKNBAZ",
        "FAZ BASS",
        "FLOORIT",
        "FRET EKO",
        "FRET NOT",
        "FUNK BAZ",
        "HARMBAS5",
        "HISBASS",
        "HOTBODOM",
        "JAN BASS",
        "JOCKO",
        "JOCKO 2",
        "LEEDBASS",
        "LUMPBASS",
        "MINIBASS",
        "MONO BS*",
        "MOOGER",
        "MOOOG_B",
        "MUFFEL",
        "NOISBASS",
        "OCTABASS",
        "ORBASS",
        "OW BASS",
        "PABASS*",
        "PLUCK-BS",
        "POLBASS1",
        "POLYBASS",
        "PUKBASS",
        "R + B",
        "RAGABASS",
        "VELBASS",
        "RUBBER",
        "SEQUBASS",
        "VELGROWL",
        "SINCBASS",
        "WAPBASS",
        "SLAP 1",
        "SLAP 2",
        "SLIDER",
        "SLOWBASS",
        "SNTHBS1*",
        "SOFTBASS",
        "SPITBASS",
        "SQUISBAZ",
        "STAND UP",
        "STBASS",
        "STR.BASS",
        "STRANGTK",
        "STRIBASS",
        "WET BAZ",
        "STRINGBZ",
        "SUGITA\"",
        "SUPPORT",
        "SWELLBAZ",
        "SWP.BASS",
        "SYBASS 2",
        "SYN BS2*",
        "SYN BS3*",
        "SYN BS4*",
        "SYNCBASS",
        "TAURUS",
        "TENU'OB2",
        "WIPBASS",
        "TIKBASS",
        "AK-48",
        "APORT",
        "BALLGAME",
        "BANJO",
        "BASSDRUM",
        "BDTH-1",
        "BELL 1",
        "BELLIKE",
        "BELLS",
        "BELLS-GS",
        "BI-PLANE",
        "BOTTLES",
        "BTMEHRDR",
        "BURST 1",
        "CASCAD'4",
        "CHIMES",
        "CHIMES*",
        "CHOPPERZ",
        "COINOP 3",
        "COPOLIPS",
        "CRAZYMAN",
        "CRICKET",
        "CROZTALK",
        "DB BELL",
        "DIDIER",
        "DREAMING",
        "DRIFTER*",
        "DRUMPOP",
        "DUNDERZ",
        "DX-PLUCK",
        "TURBO",
        "FALLCHYM",
        "FIREBALL",
        "FLAME ON",
        "FLEXTONE",
        "FMPLUKS",
        "FURYO 2",
        "G.S.2",
        "G.S.3",
        "GLOCK",
        "WINDS",
        "HAUNTING",
        "HEART",
        "ZAP",
        "HORRORS",
        "HOWITZER",
        "HVN+HELL",
        "INDIAN",
        "TOP-GUN*",
        "INSIDES",
        "JETTZ 3",
        "JUNKANOO",
        "WETFEET",
        "KINGONG",
        "KONTAKTE",
        "LCTRCUTE",
        "WHIZZ",
        "LFO ART",
        "LFOMALET",
        "LIFTOFF",
        "LOOPBELL",
        "LYLE 3 P",
        "LYLES'",
        "THUNDRUS",
        "MACHINSM",
        "MANIAC*",
        "MARIMA",
        "MOFO",
        "MEMORIES",
        "MOUNTAIN",
        "MRIMBAH*",
        "NASTEEZ",
        "WARNINGS",
        "NOISE-DN",
        "NOISSWEP*",
        "NOIZGATE",
        "NTHENEWS",
        "NUKE EM'",
        "OCIEAN",
        "OCEANWAV",
        "OOZES 3",
        "PHASES*",
        "PINWHEEL",
        "PLUCK",
        "POLBELS2",
        "POLNOISE",
        "PORTAL",
        "PSYCHYM",
        "RAINECHO",
        ")RAPIST(",
        "WATER",
        "RUBRTOMS",
        "SATURDAY",
        "SCRATCH",
        "SEQEUNCE",
        "SGUSTING",
        "SHRNKRAY",
        "SIMONISK",
        "SMASH*",
        "SMPLTHIS",
        "PAPANO 4",
        "MIKPIANO",
        "HONOCLAB",
        "MR.ROGRS",
        "MTL PNO*",
        "MUSETTE",
        "MUTDCLV*",
        "MUTRONO",
        "NYLNPIK*",
        "NYLNPK2*",
        "NYLON 12",
        "OB8 JUMP",
        "OBNOXVOX",
        "OBXA-B5",
        "ODX 7",
        "OORGAN",
        "LAZ HARP",
        "ORGAN 9",
        "ORGAN-1",
        "ORGAN-1P",
        "ORGAN-2",
        "ORGANISM",
        "AKOUSTIK",
        "ORGNIZE*",
        "P.ORGAN",
        "P.ORGAN4",
        "P.ORGAN5",
        "PA ANO 5",
        "HARPO",
        "PAPANO 7",
        "LULLABOX",
        "PERCCLAV",
        "PERCPNO",
        "PIANITAR",
        "PIANO",
        "PIANO BO",
        "PIANOLA",
        "B-3.1",
        "PINPIANO",
        "PIPEORG.",
        "PIPEORG:",
        "PIPES",
        "PIPSTRNG",
        "PIRATES!",
        "PNO-ELEC",
        "POLPIANP",
        "PRELUDE1",
        "PRESLEZ1",
        "PROFIT",
        "PROPH W",
        "PROPHET",
        "RESPIANO",
        "RMIPIANO",
        "ROADS",
        "SAL00N 5",
        "SALOON 3",
        "B-3.2",
        "SALOON 7",
        "SAMPLORG",
        "SAMSGRND",
        "SITAR I",
        "SMTHSQ2*",
        "SPANIEL",
        "SPRPRTS*",
        "B-3.3",
        "B3+LSLIE",
        "STRGTR2*",
        "SYN CLAV",
        "BELLS",
        "SYNLUTH",
        "BLABINET",
        "SYNPIANO",
        "CELESTE",
        "CHIMES",
        "TINEOUT",
        "TONYPIAN",
        "TOYPIANO",
        "TWINSTRG",
        "CHURCH",
        "VIBECHOES",
        "VIBES",
        "CLAV B6",
        "CLAVI 2",
        "WA CLAB*",
        "CLAVINET",
        "WHAANO",
        "WHY FM",
        "CLICKORG",
        "WURLI8",
        "CLUBS",
        "WURLY 2",
        "WURLY 3",
        "X-GRAND",
        "XA'ORGAN",
        "YOUREYES",
        "ZITHER",
        "CORDINE1",
        "D\"AMMOND",
        "GREEZY1",
        "GRNDR 6*"
        };
    }
