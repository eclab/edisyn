/***
    Copyright 2021 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.waldorfmicrowave;

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
   A patch editor for the Waldorf MicroWave.  Does not deal with Multi mode, global parameters,
   modifying wavetables, or uploading samples.  Only Single mode patches.
        
   @author Sean Luke
*/

public class WaldorfMicrowave extends Synth
    {
        
    static final String[] BANKS = new String[] { "A", "B" };
    static final String[] MOD_SOURCES = new String[] {"LFO1", "LFO 1", "LFO 2", "Volume Envelope", "Filter Envelope", "Wave Envelope", "LFO1 Envelope", "Keytrack", "Velocity", "Release Velocity", "Aftertouch", "Poly Pressure", "Pitch Bend", "Modulation Wheel", "Sustain Pedal", "Volume Controller", "Panning Controller", "Breath Controller", "Controller W", "Controller X", "Controller Y", "Controller Z", "Maximum", "Minimum", "MIDI Clock Rate" };
    static final String[] WAVE_MODES = new String[] { "Stepped", "Smooth" };
    static final String[] PITCH_MODES = new String[] { "Normal", "Fixed" };
    static final String[] LFO_SHAPES = new String[] { "Sine", "Sawtooth", "Pulse", "Random", "Sample & Hold" };
    static final String[] GLIDE_TYPES = new String[] { "Off", "Glissando", "Portamento", "MIDI Glissando", "MIDI Portamento", "Fingered Glissando", "Fingered Portamento" };
    static final String[] GLIDE_MODES = new String[] { "Equal Time", "Equal Distance" };
    static final String[] TUNING_TABLES = new String[] { "Positive", "Negative", "Slight Detune", "Honky Tonk", "User Table 1", "User Table 2", "User Table 3", "User Table 4" };
    static final String[] WAVE_TABLES = new String[] {    "R01 Resonant", "R02 Resonant2", "R03 MalletSyn",  "R04 Sqr-Sweep", "R05 Bellish", "R06 Pul-Sweep", "R07 Saw-Sweep", "R08 MellowSaw", 
                                                          "R09 Feedback", "R10 Add Harm", "R11 Reso 3 HP", "R12 Wind Syn", "R13 HighHarm", "R14 Clipper", "R15 OrganSyn", "R16 SquareSaw",
                                                          "R17 Format1", "R18 Polated", "R19 Transient", "R20 ElectricP", "R21 Robotic", "R22 StrongHrm", "R23 PercOrgan", "R24 ClipSweep", 
                                                          "R25 ResoHarms", "R26 2 Echoes", "R27 Formant2", "R28 FmntVocal", "R29 MicroSync", // Is this called MicroSync or just Sync?  on the PPG it's Sync
                                                          "R30 MicroPWM", // Is this called MicroPWM or just PWM?  on the PPG it's PWM 
                                                          "R31 Upper Wavetable",               // Is this right?
                                                          "R32 Piano/Sax",
                                                          "I33 User 1", "I34 User 2", "I35 User 3", "I36 User 4", "I37 User 5", "I38 User 6", "I39 User 7", "I40 User 8", "I41 User 9", "I42 User 10", "I43 User 11", "I44 User 12",
                                                          "C45 Card 1", "C46 Card 2", "C47 Card 3", "C48 Card 4", "C49 Card 5", "C50 Card 6", "C51 Card 7", "C52 Card 8", "C53 Card 9", "C54 Card 10", "C55 Card 11", "C56 Card 12",
        }; 
    
    public WaldorfMicrowave()
        {
        for(int i = 0; i < allParameters.length; i++)
            {
            allParametersToIndex.put(allParameters[i], Integer.valueOf(i));
            }
        
        /// SOUND PANEL
                
        JComponent soundPanel = new SynthPanel(this);
        VBox vbox = new VBox();
        
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        hbox.addLast(addWavetable(Style.COLOR_A()));
                
        vbox.add(hbox);
        
        vbox.add(addOscillator(1, Style.COLOR_A()));
        vbox.add(addWave(1, Style.COLOR_A()));

        vbox.add(addOscillator(2, Style.COLOR_B()));
        vbox.add(addWave(2, Style.COLOR_B()));

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Oscillators", soundPanel);
                
                
        /// SOUND PANEL
                
        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        vbox.add(addVolume(Style.COLOR_A()));
        vbox.add(addEnvelope(1, Style.COLOR_A()));
        hbox = new HBox();
        hbox.add(addGlide(Style.COLOR_C()));
        hbox.addLast(addPanning(Style.COLOR_C()));
        vbox.add(hbox);
        vbox.add(addFilter(Style.COLOR_B()));
        vbox.add(addEnvelope(2, Style.COLOR_B()));
        
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Volume and Filter", soundPanel);


        // ENVELOPE PANEL
                
        JComponent envelopePanel = new SynthPanel(this);
        vbox = new VBox();
        vbox.add(addLFO(1, Style.COLOR_B()));
        vbox.add(addLFO(2, Style.COLOR_B()));
        vbox.add(addWaveEnvelope(Style.COLOR_A()));
        envelopePanel.add(vbox, BorderLayout.CENTER);
        addTab("Modulation", envelopePanel);

        
        model.set("name", "Init Sound V1.1 ");

        model.set("number", 0);
        model.set("bank", 0);
        
        loadDefaults();        
        }
                               
    public String getDefaultResourceFileName() { return "WaldorfMicrowave.init"; }
    public String getHTMLResourceFileName() { return "WaldorfMicrowave.html"; }

    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        JComboBox bank = new JComboBox(BANKS);
        bank.setEditable(false);
        bank.setMaximumRowCount(32);
        bank.setSelectedIndex(model.get("bank"));
                
        JTextField number = new SelectedTextField("" + (model.get("number") + 1), 3);

        while(true)
            {
            boolean result = showMultiOption(this, new String[] { "Bank", "Patch Number"}, 
                new JComponent[] { bank, number }, title, "Enter the Bank and Patch Number");
                
            if (result == false) 
                return false;
                                
            int n;
            try { n = Integer.parseInt(number.getText()); }
            catch (NumberFormatException e)
                {
                showSimpleError(title, "The Patch Number must be an integer 1 ... 32");
                continue;
                }
            if (n < 1 || n > 32)
                {
                showSimpleError(title, "The Patch Number must be an integer 1 ... 32");
                continue;
                }
                                                        
            change.set("bank", bank.getSelectedIndex());
            change.set("number", n - 1);
                        
            return true;
            }
        }

    /** Add the global patch category (name, id, number, etc.) */
    public JComponent addNameGlobal(Color color)
        {
        Category globalCategory = new Category(this, "Waldorf Microwave", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
                
        VBox vbox = new VBox();
        HBox hbox2 = new HBox();
        comp = new PatchDisplay(this, 8);
        hbox2.add(comp);
        vbox.add(hbox2);
        hbox.add(vbox);
        
        comp = new StringComponent("Patch Name", this, "name", 16, "Name must be up to 16 ASCII characters.")
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
                        
        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }

        

    /** Add the Global Oscillator category */
    public JComponent addWavetable(Color color)
        {
        Category category = new Category(this, "Waves", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        params = WAVE_TABLES;
        comp = new Chooser("Wavetable", this, "wavetable", params);

        vbox.add(comp);

        hbox.add(vbox);
        category.add(hbox, BorderLayout.WEST);
        return category;
        }


    /** Add the Quality category */    

    public JComponent addVolume(Color color)
        {
        Category category = new Category(this, "Volume", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();

        VBox vbox = new VBox();
        params = MOD_SOURCES;
        comp = new Chooser("Modifier 1 Source", this, "volumemodifier1source", params);
        vbox.add(comp);

        params = MOD_SOURCES;
        comp = new Chooser("Modifier 1 Control", this, "volumemodifier1control", params);
        vbox.add(comp);
        hbox.add(vbox);
        
        vbox = new VBox();
        params = MOD_SOURCES;
        comp = new Chooser("Modifier 2 Source", this, "volumemodifier2source", params);
        vbox.add(comp);

        params = MOD_SOURCES;
        comp = new Chooser("Noise Level Mod", this, "noiselevelmodifiersource", params);
        vbox.add(comp);
        hbox.add(vbox);
        
        comp = new LabelledDial("Wave 1", this, "wave1volume", color, 0, 7);
        hbox.add(comp);

        comp = new LabelledDial("Wave 2", this, "wave2volume", color, 0, 7);
        hbox.add(comp);

        comp = new LabelledDial("Noise", this, "noisevolume", color, 0, 7);
        hbox.add(comp);

        comp = new LabelledDial("Sound", this, "soundvolume", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Envelope", this, "volumeenvelopeamount", color, 0, 127, 64);            
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        comp = new LabelledDial("Envelope", this, "volumeenvelopevelocity", color, 0, 127, 64);            
        ((LabelledDial)comp).addAdditionalLabel("Velocity");
        hbox.add(comp);

        comp = new LabelledDial("Keytrack", this, "volumekeytrackamount", color, 0, 127, 64);            
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        comp = new LabelledDial("Modifier 1", this, "volumemodifier1amount", color, 0, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);
        
        comp = new LabelledDial("Modifier 2", this, "volumemodifier2amount", color, 0, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);
        
        comp = new LabelledDial("Noise Level Mod", this, "noiselevelmodifieramount", color, -7, 7);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);
        
        category.add(hbox, BorderLayout.WEST);
        return category;
        }


    public JComponent addPanning(Color color)
        {
        Category category = new Category(this, "Panning", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();

        VBox vbox = new VBox();
        params = MOD_SOURCES;
        comp = new Chooser("Modifier Source", this, "panmodifiersource", params);
        vbox.add(comp);
        hbox.add(vbox);
        
        comp = new LabelledDial("Panning", this, "panning", color, 0, 127)
            {
            public boolean isSymmetric()
                {
                return true;
                }
                        
            public String map(int value)
                {
                if (value < 64)
                    {
                    return "<" + (64 - value);
                    }
                else if (value > 64)
                    {
                    return "" + (value - 64) + ">";
                    }
                else return "--";
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Modifier", this, "panmodifieramount", color, 0, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);
        
        category.add(hbox, BorderLayout.WEST);
        return category;
        }

    /** Add an Oscillator category */
    public JComponent addOscillator(int osc, Color color)
        {
        Category category = new Category(this, "Oscillator " + osc, color);
        category.makePasteable("osc");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        
        VBox vbox = new VBox();
        params = MOD_SOURCES;
        comp = new Chooser("Modifier 1 Source", this, "osc" + osc + "modifier1source", params);
        vbox.add(comp);

        params = MOD_SOURCES;
        comp = new Chooser("Modifier 1 Control", this, "osc" + osc + "modifier1control", params);
        vbox.add(comp);
        hbox.add(vbox);
        
        vbox = new VBox();

        params = MOD_SOURCES;
        comp = new Chooser("Modifier 2 Source", this, "osc" + osc + "modifier2source", params);
        vbox.add(comp);


        if (osc==1)
            {
	        params = PITCH_MODES;
	        comp = new Chooser("Pitch Mode", this, "osc" + osc + "pitchmode", params);
	        vbox.add(comp);
            }
		else
            {
			HBox hbox2 = new HBox();
	        params = PITCH_MODES;
	        comp = new Chooser("Pitch Mode", this, "osc" + osc + "pitchmode", params);
	        hbox2.add(comp);

            comp = new CheckBox("Link ", this, "osc" + osc + "linkmode");
            hbox2.add(comp);    
			vbox.add(hbox2);
            }
		
        hbox.add(vbox);

        comp = new LabelledDial("Octave", this, "osc" + osc + "octave", color, 0, 4, 2)
            {
            public boolean isSymmetric()
                {
                return true;
                }
            };
        hbox.add(comp);
                
        comp = new LabelledDial("Semitone", this, "osc" + osc + "semitone", color, 0, 12);
        hbox.add(comp);

        comp = new LabelledDial("Detune", this, "osc" + osc + "detune", color, 0, 127, 64);
        hbox.add(comp);
        
        comp = new LabelledDial("Pitch Bend", this, "osc" + osc + "bendrange", color, 0, 12);
        ((LabelledDial)comp).addAdditionalLabel("Range");
        hbox.add(comp);

        comp = new LabelledDial("Modifier 1", this, "osc" + osc + "modifier1amount", color, 0, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);
        
        comp = new LabelledDial("Modifier 2", this, "osc" + osc + "modifier2amount", color, 0, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);
        
        comp = new LabelledDial("Modifier 2", this, "osc" + osc + "modifier2quantize", color, 0, 7);
        ((LabelledDial)comp).addAdditionalLabel("Quantize");
        hbox.add(comp);
        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
        
        
    /** Add an Oscillator category */
    public JComponent addWave(int wave, Color color)
        {
        Category category = new Category(this, "Wave " + wave, color);
        category.makePasteable("wave");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = MOD_SOURCES;
        comp = new Chooser("Modifier 1 Source", this, "wave" + wave + "modifier1source", params);
        vbox.add(comp);

        params = MOD_SOURCES;
        comp = new Chooser("Modifier 1 Control", this, "wave" + wave + "modifier1control", params);
        vbox.add(comp);
        hbox.add(vbox);

        vbox = new VBox();
        params = MOD_SOURCES;
        comp = new Chooser("Modifier 2 Source", this, "wave" + wave + "modifier2source", params);
        vbox.add(comp);

        params = MOD_SOURCES;
        comp = new Chooser("Level Modifier Source", this, "wave" + wave + "levelmodifiersource", params);
        vbox.add(comp);
        hbox.add(vbox);

        vbox = new VBox();
        params = WAVE_MODES;
        comp = new Chooser("Mode", this, "wave" + wave + "mode", params);
        vbox.add(comp);

        if (wave==2)
            {
            comp = new CheckBox("Link", this, "wave2linkmode");
            vbox.add(comp);    
            }
        
        hbox.add(vbox);

        comp = new LabelledDial("Start Wave", this, "wave" + wave + "startwave", color, 0, 63)
            {
            public String map(int val)
                {
                if (val < 61)
                    {
                    return "" + val;
                    }
                else if (val == 61)
                    {
                    return "Tri";
                    }
                else if (val == 62)
                    {
                    return "Square";
                    }
                else // if (val == 63)
                    {
                    return "Saw";
                    }
                }
            };
        model.setMetricMax( "wave" + wave + "startwave", 60);
        hbox.add(comp);

        comp = new LabelledDial("Start Sample", this, "wave" + wave + "startsample", color, 0, 127)
            {
            public String map(int val)
                {
                if (val == 0)
                    {
                    return "Free";
                    }
                else
                    {
                    return "" + val;
                    }
                }
            };
        model.setMetricMin( "wave" + wave + "startsample", 1);
        hbox.add(comp);

        comp = new LabelledDial("Envelope", this, "wave" + wave + "envelopeamount", color, 0, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        comp = new LabelledDial("Envelope", this, "wave" + wave + "envelopevelocity", color, 0, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Velocity");
        hbox.add(comp);

        comp = new LabelledDial("Keytrack", this, "wave" + wave + "keytrackamount", color, 0, 127, 64);            
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        comp = new LabelledDial("Modifier 1", this, "wave" + wave + "modifier1amount", color, 0, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);
        
        comp = new LabelledDial("Modifier 2", this, "wave" + wave + "modifier2amount", color, 0, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);
        
        comp = new LabelledDial("Level Mod", this, "wave" + wave + "levelmodifieramount", color, -7, 7);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);
        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }



    /** Add a Filter1 category */
    public JComponent addFilter(Color color)
        {
        Category category = new Category(this, "Filter", color);
                
        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();

        VBox vbox = new VBox();
        params = MOD_SOURCES;
        comp = new Chooser("Cutoff Modifier 1 Source", this, "cutoffmodifier1source", params);
        vbox.add(comp);

        params = MOD_SOURCES;
        comp = new Chooser("Cutoff Modifier 1 Control", this, "cutoffmodifier1control", params);
        vbox.add(comp);
        hbox.add(vbox);

        vbox = new VBox();
        params = MOD_SOURCES;
        comp = new Chooser("Cutoff Modifier 2 Source", this, "cutoffmodifier2source", params);
        vbox.add(comp);

        vbox = new VBox();
        params = MOD_SOURCES;
        comp = new Chooser("Resonance Modifier 1 Source", this, "resonancemodifier1source", params);
        vbox.add(comp);

        hbox.add(vbox);
        
        comp = new LabelledDial("Cutoff", this, "filtercutofffrequency", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Resonance", this, "filterresonance", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Cutoff Keytrack", this, "cutoffkeytrackamount", color, 0, 127, 64);            
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        comp = new LabelledDial("Cutoff Env", this, "cutoffenvelopeamount", color, 0, 127, 64);            
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        comp = new LabelledDial("Cutoff Env", this, "cutoffenvelopevelocity", color, 0, 127, 64);            
        ((LabelledDial)comp).addAdditionalLabel("Velocity");
        hbox.add(comp);

        comp = new LabelledDial("Cutoff Mod 1", this, "cutoffmodifier1amount", color, 0, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);
        
        comp = new LabelledDial("Cutoff Mod 2", this, "cutoffmodifier2amount", color, 0, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);
        
        comp = new LabelledDial("Resonance Mod 1", this, "resonancemodifier1amount", color, 0, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);
        
        category.add(hbox, BorderLayout.WEST);
        return category;
        }


    /** Add Glid Category */
    public JComponent addGlide(Color color)
        {
        Category category = new Category(this, "Glide and Tuning", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();

        VBox vbox = new VBox();
        params = GLIDE_TYPES;
        comp = new Chooser("Type", this, "glide", params);
        vbox.add(comp);

        params = GLIDE_MODES;
        comp = new Chooser("Mode", this, "glidemode", params);
        vbox.add(comp);
           
        hbox.add(vbox);
        vbox = new VBox();
             
        params = TUNING_TABLES;
        comp = new Chooser("Tuning Table", this, "tuningtable", params);
        vbox.add(comp);
        hbox.add(vbox);
        
        comp = new LabelledDial("Rate", this, "gliderate", color, 0, 127);
        hbox.add(comp);
        
        category.add(hbox, BorderLayout.WEST);
        return category;
        }

    /** Add an LFO category */
    public JComponent addLFO(final int lfo, Color color)
        {
        Category category = new Category(this, "LFO " + lfo, color);
        category.makePasteable("lfo");
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();

        VBox vbox = new VBox();
        params = LFO_SHAPES;
        comp = new Chooser("Shape", this, "lfo" + lfo + "shape", params);
        vbox.add(comp);

        if (lfo == 1)
            {
            comp = new CheckBox("Sync ", this, "lfo" + lfo + "sync");
            vbox.add(comp);    
            }
        hbox.add(vbox);
               
        if (lfo == 1)
            { 
            vbox = new VBox();
            params = MOD_SOURCES;
            comp = new Chooser("Rate Modifier Source", this, "lfo" + lfo + "ratemodifiersource", params);
            vbox.add(comp);

            params = MOD_SOURCES;
            comp = new Chooser("Level Modifier Source", this, "lfo" + lfo + "levelmodifiersource", params);
            vbox.add(comp);
            hbox.add(vbox);
            }

        comp = new LabelledDial("Rate", this, "lfo" + lfo + "rate", color, 0, 127);
        hbox.add(comp);
        
        comp = new LabelledDial("Symmetry", this, "lfo" + lfo + "symmetry", color, 0, 127, 64);
        hbox.add(comp);

        comp = new LabelledDial("Humanize", this, "lfo" + lfo + "humanize", color, 0, 4)
            {
            public String map(int val)
                {
                if (val == 0)
                    return "Off";
                else return "" + val;
                }
            };
        hbox.add(comp);

        if (lfo == 1)
            {
            comp = new LabelledDial("Rate Mod", this, "lfo" + lfo + "ratemodifieramount", color, 0, 127, 64);
            ((LabelledDial)comp).addAdditionalLabel("Amount");
            hbox.add(comp);
                
            comp = new LabelledDial("Level Mod", this, "lfo" + lfo + "levelmodifieramount", color, 0, 127, 64);
            ((LabelledDial)comp).addAdditionalLabel("Amount");
            hbox.add(comp);

            comp = new LabelledDial("Delay", this, "lfo" + lfo + "delay", color, 0, 127)
                {
                public String map(int val)
                    {
                    if (val == 0)
                        return "Off";
                    else if (val == 1)
                        return "Retrig";
                    else return "" + (val - 1);
                    }
                };
            model.setMetricMin( "lfo" + lfo + "delay", 2);
            hbox.add(comp);

            comp = new LabelledDial("Attack", this, "lfo" + lfo + "attack", color, 0, 127);
            hbox.add(comp);
                
            comp = new LabelledDial("Decay", this, "lfo" + lfo + "decay", color, 0, 127)
                {
                public String map(int val)
                    {
                    if (val == 0)
                        return "Env";
                    else if (val == 127)
                        return "Inf";
                    else 
                        return "" + val;
                    }
                };
            model.setMetricMin( "lfo" + lfo + "decay", 1);
            hbox.add(comp);
            }

        if (lfo == 2)
            {
            comp = new LabelledDial("Phase Shift", this, "lfo" + lfo + "phaseshift", color, 0, 90)
                {
                public String map(int val)
                    {
                    if (val == 0)
                        return "Free";
                    else 
                        return "" + (val * 2);
                    }
                };
                        
            model.setMetricMin( "lfo" + lfo + "phaseshift", 1);
            hbox.add(comp);
            }
        
        if (lfo == 1)
            {
            comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
                new String[] { null, "lfo" + lfo + "delay", "lfo" + lfo + "attack", "lfo" + lfo + "decay" },
                new String[] { null, null, null, null },
                new double[] { 0, (1.0 / 3.0) / 127.0,  (1.0 / 3.0)/127.0, (1.0 / 3.0)/127.0},
                new double[] { 0, 0.0, 1.0, 0.0 })
                {
                public void postProcess(double[] xVals, double[] yVals)
                    {
                    if (model.get("lfo" + lfo + "delay") == 1)      // "Retrig
                        {
                        // Make just like delay = 0
                        xVals[1] = 0;
                        yVals[1] = 0;
                        }
                    if (model.get("lfo" + lfo + "decay") == 0)      // "Off"
                        {
                        xVals[3] = 1.0 / 3.0;           // fix to right
                        yVals[3] = 1.0;         // fix high
                        }
                    }
                };
            hbox.addLast(comp);
            }

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
        
        
        
    /** Add a "standard" envelope category */
    public JComponent addEnvelope(final int env, Color color)
        {
        String prefix = (env == 1 ? "volumeenvelope" : "filterenvelope");
        Category category = new Category(this, env == 1 ? 
            "Volume Envelope" : "Filter Envelope", color);
        category.makePasteable("envelope");
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();

        if (env == 2)
            {
            params = MOD_SOURCES;
            comp = new Chooser("Decay Modifier Source", this, prefix + "delaymodifiersource", params);
            vbox.add(comp);
            }

        vbox = new VBox();
        params = MOD_SOURCES;
        comp = new Chooser("Attack Modifier Source", this, prefix + "attackmodifiersource", params);
        vbox.add(comp);

        params = MOD_SOURCES;
        comp = new Chooser("Decay Modifier Source", this, prefix + "decaymodifiersource", params);
        vbox.add(comp);
        hbox.add(vbox);

        vbox = new VBox();
        params = MOD_SOURCES;
        comp = new Chooser("Sustain Modifier Source", this, prefix + "sustainmodifiersource", params);
        vbox.add(comp);

        vbox = new VBox();
        params = MOD_SOURCES;
        comp = new Chooser("Release Modifier Source", this, prefix + "releasemodifiersource", params);
        vbox.add(comp);

        if (env == 2)
            {
            comp = new LabelledDial("Delay", this, prefix + "delaytime", color, 0, 127);
            hbox.add(comp);
            }
        
        comp = new LabelledDial("Attack", this, prefix + "attacktime", color, 0, 127);
        hbox.add(comp);
        
        comp = new LabelledDial("Decay", this, prefix + "decaytime", color, 0, 127);
        hbox.add(comp);
        
        comp = new LabelledDial("Sustain", this, prefix + "sustainlevel", color, 0, 127);
        hbox.add(comp);
        
        comp = new LabelledDial("Release", this, prefix + "releasetime", color, 0, 127);
        hbox.add(comp);

        if (env == 2)
            {
            comp = new LabelledDial("Delay Mod", this, prefix + "delaymodifieramount", color, 0, 127, 64);
            ((LabelledDial)comp).addAdditionalLabel("Amount");
            hbox.add(comp);
            }
        
        comp = new LabelledDial("Attack Mod", this, prefix + "attackmodifieramount", color, 0, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);
        
        comp = new LabelledDial("Decay Mod", this, prefix + "decaymodifieramount", color, 0, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);
        
        comp = new LabelledDial("Sustain Mod", this, prefix + "sustainmodifieramount", color, 0, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);
        
        comp = new LabelledDial("Release Mod", this, prefix + "releasemodifieramount", color, 0, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        if (env == 1)
            {
            comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
                new String[] { null, prefix + "attacktime", prefix + "decaytime", null, prefix + "releasetime" },
                new String[] { null, null, prefix + "sustainlevel", prefix + "sustainlevel", null },
                new double[] { 0, 0.25/127.0, 0.25 / 127.0,  0.25, 0.25/127.0},
                new double[] { 0, 1.0, 1.0 / 127.0, 1.0/127.0, 0 });
            hbox.addLast(comp);
            }
        else
            {
            comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
                new String[] { null, prefix + "delaytime", prefix + "attacktime", prefix + "decaytime", null, prefix + "releasetime" },
                new String[] { null, null, null, prefix + "sustainlevel", prefix + "sustainlevel", null },
                new double[] { 0, 0.2/127.0, 0.2/127.0, 0.2 / 127.0,  0.2, 0.2/127.0},
                new double[] { 0, 0.0, 1.0, 1.0 / 127.0, 1.0/127.0, 0 });
            hbox.addLast(comp);
            }
        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
        
        

    /** Add wave envelope category */
    public JComponent addWaveEnvelope(Color color)
        {
        Category category = new Category(this, "Wave Envelope", color);
        category.makeDistributable("waveenv");
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        HBox inner = new HBox();
        
        VBox vbox = new VBox();
        params = MOD_SOURCES;
        comp = new Chooser("Time Modifier Source", this, "waveenvelopetimemodifiersource", params);
        vbox.add(comp);

        params = MOD_SOURCES;
        comp = new Chooser("Level Modifier Source", this, "waveenvelopelevelmodifiersource", params);
        vbox.add(comp);

        comp = new CheckBox("Loop", this, "waveenvelopeloopmode");
        vbox.add(comp);
        inner.add(vbox);
        
        vbox = new VBox();
        comp = new LabelledDial("Loop Start", this, "waveenvelopeloopstartpoint", color, 0, 7, -1);
        inner.add(comp);

        comp = new LabelledDial("Key Off", this, "waveenvelopekeyoffpoint", color, 0, 7, -1);
        inner.add(comp);
        
        hbox.add(inner);

        for(int i = 1; i < 9; i++)
            {
            vbox = new VBox();
            comp = new LabelledDial("Time " + i, this, "waveenvelopetime" + i, color, 0, 127);
            ((LabelledDial)comp).addAdditionalLabel(" ");
            vbox.add(comp);
            comp = new LabelledDial("Level " + i, this, "waveenvelopelevel" + i, color, 0, 127);
            vbox.add(comp);
            hbox.add(vbox);
            }

        vbox = new VBox();
        comp = new LabelledDial("Time Mod", this, "waveenvelopetimemodifieramount", color, 0, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        vbox.add(comp);

        comp = new LabelledDial("Level Mod", this, "waveenvelopelevelmodifieramount", color, 0, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        vbox.add(comp);
        hbox.add(vbox);
        
        VBox outer = new VBox();
        outer.add(hbox);
        
        HBox displayBox = new HBox();
        displayBox.add(Strut.makeStrut(inner, false, true));
        EnvelopeDisplay disp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "waveenvelopetime1", "waveenvelopetime2", "waveenvelopetime3", "waveenvelopetime4", "waveenvelopetime5", "waveenvelopetime6", "waveenvelopetime7", "waveenvelopetime8", null },
            new String[] { null, "waveenvelopelevel1", "waveenvelopelevel2", "waveenvelopelevel3", "waveenvelopelevel4", "waveenvelopelevel5", "waveenvelopelevel6", "waveenvelopelevel7", "waveenvelopelevel8", null },
            new double[] { 0, 0.25/127.0/2.0, 0.25 / 127.0/2.0,  0.25/127.0/2.0, 0.25/127.0/2.0,  0.25/127.0/2.0, 0.25 / 127.0/2.0,  0.25/127.0/2.0, 0.25/127.0/2.0, 0},
            new double[] { 0, 1.0/127.0, 1.0/127.0, 1.0/127.0, 1.0/127.0, 1.0/127.0, 1.0/127.0, 1.0/127.0, 1.0/127.0, 0 })
            {
            public int postProcessLoopOrStageKey(String key, int val)
                {
                // they'll all be off by 1
                return val + 1;
                }
            };
        disp.setPreferredWidth(disp.getPreferredWidth() * 2);
        disp.setLoopKeys(0, "waveenvelopeloopstartpoint", "waveenvelopekeyoffpoint");
        // dunno if we should do this, if only for extra visibility...
        disp.setSustainStageKey("waveenvelopeloopstartpoint");
        disp.setFinalStageKey("waveenvelopekeyoffpoint");
        comp = disp;
        displayBox.addLast(comp);
        outer.add(displayBox);

        category.add(outer, BorderLayout.CENTER);
        return category;
        }


    /** Map of parameter -> index in the allParameters array. */
    HashMap allParametersToIndex = new HashMap();


    /** List of all Waldorf parameters in order.  "-" is a reserved (unused and thus unnamed) parameter. */

    /// * indicates parameters which must be handled specially due to packing
    /// that Waldorf decided to do.  :-(

    final static String[] allParameters = new String[/*256*/] 
    {
    "osc1octave",                   
    "osc1semitone",                                     // has to be handled specially
    "osc1detune",
    "osc1bendrange",
    "osc1pitchmode",
    "osc1modifier1source",
    "osc1modifier1control",
    "osc1modifier1amount",
    "osc1modifier2source",
    "osc1modifier2amount",
    "osc1modifier2quantize",
    "osc2octave",                   
    "osc2semitone",                                     // has to be handled specially
    "osc2detune",
    "osc2bendrange",
    "osc2pitchmode",
    "osc2modifier1source",
    "osc2modifier1control",
    "osc2modifier1amount",
    "osc2modifier2source",
    "osc2modifier2amount",
    "osc2modifier2quantize",
    "osc2linkmode",
    "wavetable",
    "wave1startwave",
    "wave1startsample",
    "wave1envelopeamount",
    "wave1envelopevelocity",
    "wave1keytrackamount",
    "wave1modifier1source",
    "wave1modifier1control",
    "wave1modifier1amount",
    "wave1modifier2source",
    "wave1modifier2amount",
    "wave1mode",
    "-",
    "wave2startwave",
    "wave2startsample",
    "wave2envelopeamount",
    "wave2envelopevelocity",
    "wave2keytrackamount",
    "wave2modifier1source",
    "wave2modifier1control",
    "wave2modifier1amount",
    "wave2modifier2source",
    "wave2modifier2amount",
    "wave2mode",
    "wave2linkmode",
    "wave1volume",                                                      // has to be handled specially
    "wave2volume",                                                      // has to be handled specially
    "noisevolume",                                                      // has to be handled specially
    "soundvolume",
    "volumeenvelopeamount",
    "volumeenvelopevelocity",
    "volumekeytrackamount",
    "volumemodifier1source",
    "volumemodifier1control",
    "volumemodifier1amount",
    "volumemodifier2source",
    "volumemodifier2amount",
    "filtercutofffrequency",
    "filterresonance",
    "cutoffenvelopeamount",
    "cutoffenvelopevelocity",
    "cutoffkeytrackamount",
    "cutoffmodifier1source",
    "cutoffmodifier1control",
    "cutoffmodifier1amount",
    "cutoffmodifier2source",
    "cutoffmodifier2amount",
    "resonancemodifier1source",
    "resonancemodifier1amount",
    "volumeenvelopeattacktime",
    "volumeenvelopedecaytime",
    "volumeenvelopesustainlevel",
    "volumeenvelopereleasetime",
    "volumeenvelopeattackmodifiersource",
    "volumeenvelopeattackmodifieramount",
    "volumeenvelopedecaymodifiersource",
    "volumeenvelopedecaymodifieramount",
    "volumeenvelopesustainmodifiersource",
    "volumeenvelopesustainmodifieramount",
    "volumeenvelopereleasemodifiersource",
    "volumeenvelopereleasemodifieramount",
    "-",
    "filterenvelopedelaytime",
    "filterenvelopeattacktime",
    "filterenvelopedecaytime",
    "filterenvelopesustainlevel",
    "filterenvelopereleasetime",
    "filterenvelopedelaymodifiersource",
    "filterenvelopedelaymodifieramount",
    "filterenvelopeattackmodifiersource",
    "filterenvelopeattackmodifieramount",
    "filterenvelopedecaymodifiersource",
    "filterenvelopedecaymodifieramount",
    "filterenvelopesustainmodifiersource",
    "filterenvelopesustainmodifieramount",
    "filterenvelopereleasemodifiersource",
    "filterenvelopereleasemodifieramount",
    "-",
    "waveenvelopetime1",
    "waveenvelopelevel1",
    "waveenvelopetime2",
    "waveenvelopelevel2",
    "waveenvelopetime3",
    "waveenvelopelevel3",
    "waveenvelopetime4",
    "waveenvelopelevel4",
    "waveenvelopetime5",
    "waveenvelopelevel5",
    "waveenvelopetime6",
    "waveenvelopelevel6",
    "waveenvelopetime7",
    "waveenvelopelevel7",
    "waveenvelopetime8",
    "waveenvelopelevel8",
    "waveenvelopetimemodifiersource",
    "waveenvelopetimemodifieramount",
    "waveenvelopelevelmodifiersource",
    "waveenvelopelevelmodifieramount",
    "waveenvelopekeyoffpoint",
    "waveenvelopeloopstartpoint",
    "waveenvelopeloopmode",
    "lfo1rate",
    "lfo1shape",
    "lfo1symmetry",
    "lfo1humanize",
    "lfo1ratemodifiersource",
    "lfo1ratemodifieramount",
    "lfo1levelmodifiersource",
    "lfo1levelmodifieramount",
    "lfo1sync",
    "lfo1delay",
    "lfo1attack", 
    "lfo1decay",
    "lfo2rate",
    "lfo2shape",
    "lfo2symmetry",
    "lfo2humanize",
    "lfo2phaseshift",
    "-",
    "panning",
    "panmodifiersource",
    "panmodifieramount",
    "glide",
    "gliderate",
    "glidemode",
    "tuningtable",
    "name",         // *
    "name",         // *
    "name",         // *
    "name",         // *
    "name",         // *
    "name",         // *
    "name",         // *
    "name",         // *
    "name",         // *
    "name",         // *
    "name",         // *
    "name",         // *
    "name",         // *
    "name",         // *
    "name",         // *
    "name",         // *
    "wave1levelmodifiersource",
    "wave1levelmodifieramount",                         // has to be handled specially
    "wave2levelmodifiersource",
    "wave2levelmodifieramount",                         // has to be handled specially
    "noiselevelmodifiersource",
    "noiselevelmodifieramount",                         // has to be handled specially
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    };



    public Object[] emitAll(String key)
        {
        if (key.equals("bank")) return new Object[0];  // this is not emittable
        if (key.equals("number")) return new Object[0];  // this is not emittable

		int instrument = 0;		// need to change this
		byte id = (byte)getID();
		
		if (key.equals("name"))
			{
			char[] name = (model.get("name", "Untitled") + "                ").toCharArray();
			Object[] obj = new Object[16];
			for(int i = 0; i < obj.length; i++)
				{
				int param = 148 + i;
				// nybblize param number
				int HH = (param >>> 4) & 15;
				int LL = param & 15;

				byte[] data = new byte[] 
					{ 
					(byte)0xF0, 
					0x3E, 				// Waldorf
					0x00,				// Microwave 1 
					id,
					0x60, 				// RTBP (Real Time BPR Edit)
					(byte)instrument,
					(byte)HH,
					(byte)LL,
					(byte)(byte)name[i],
					0,	// checksum
					(byte)0xF7 
					};
				data[9] = produceChecksum(data, 5, 9);
				
				obj[i] = data;
				}
			return obj;
			}
		else
			{
			// The parameter numbers are 0...179
			// corresponding to (I think) bytes 5...184 in the dump
			int param = ((Integer)(allParametersToIndex.get(key))).intValue();
			int val = model.get(key);
			
			if (key.equals("osc1octave") || key.equals("osc2octave"))
				{
				// handle specially
				val = (val + 2) * 16;
				}
			else if (key.equals("osc1semitone") || key.equals("osc2semitone"))
				{
				// handle specially
				// Range 0...12 maps to 0...96 I think, but the docs say 0...120
				val = val * 8;
				}
			else if (key.equals("wave1volume") || key.equals("wave2volume") || key.equals("noisevolume"))
				{
				// handle specially
				// Range 0...7 maps to 0...112
				val = val * 16;
				}
			else if (key.equals("wave1levelmodifieramount") || key.equals("wave2levelmodifieramount") || key.equals("noiselevelmodifieramount"))
				{
				// handle specially
				// Range -7 ... + 7 maps to 8 ... 120
				val = (val + 7) * 8 + 8;
				}
			
			// nybblize param number
			int HH = (param >>> 4) & 15;
			int LL = param & 15;
			
            byte[] data = new byte[] 
            	{ 
            	(byte)0xF0, 
            	0x3E, 				// Waldorf
            	0x00,				// Microwave 1 
            	id,
            	0x60, 				// RTBP (Real Time BPR Edit)
            	(byte)instrument,
            	(byte)HH,
            	(byte)LL,
            	(byte)val,
            	0,	// checksum
            	(byte)0xF7 
            	};
            data[9] = produceChecksum(data, 5, 9);
            
            return new Object[] { data };
			}
        }
    
    
    
    
    public Object[] emitAll(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        // It's not clear if we can do a write at all, or if we can only do a send.
        // If we can do a write,
        // We need to issue a Sound Dump (BPRD) or Arrangement Sound Program Dump (ABPD)
        // Followed by a Store BPR (STRB)
        
        if (tempModel == null)
            tempModel = getModel();
            
        byte DEV = (byte)(getID());
        byte NN = (byte) tempModel.get("number");
        byte BB = (byte) tempModel.get("bank");
        
        byte[] bytes = new byte[187];
        
        bytes[0] = (byte)0xF0;		
        bytes[1] = (byte)0x3E;		// Waldorf
        bytes[2] = (byte)0x00;		// Microwave 1
        bytes[3] = DEV;
        bytes[4] = (byte)0x42;		// BPRD
        bytes[184] = (byte)0x55;	// the "Valid" flag
        bytes[186] = (byte)0xF7;
        
		char[] name = (model.get("name", "Untitled") + "                ").toCharArray();
        
        for(int i = 0; i < allParameters.length; i++)
            {
            String key = allParameters[i];
            int val = model.get(key);
            if (key.equals("-"))
            	{
            	val = 0;
            	}
            else if (key.equals("name"))
            	{
				val = (byte)name[i - 153 + 5];
            	}
            else if (key.equals("osc1octave") || key.equals("osc2octave"))
                {
                val = (val + 2) * 16;
                }
			else if (key.equals("osc1semitone") || key.equals("osc2semitone"))
				{
				// handle specially
				// Range 0...12 maps to 0...96 I think, but the docs say 0...120
				val = val * 8;
				}
			else if (key.equals("wave1volume") || key.equals("wave2volume") || key.equals("noisevolume"))
				{
				// handle specially
				// Range 0...7 maps to 0...112
				val = val * 16;
				}
			else if (key.equals("wave1levelmodifieramount") || key.equals("wave2levelmodifieramount") || key.equals("noiselevelmodifieramount"))
				{
				// handle specially
				// Range -7 ... + 7 maps to 8 ... 120
				val = (val + 7) * 8 + 8;
				}
				
			bytes[i + 5] = (byte)val;
            }
        
        bytes[185] = produceChecksum(bytes, 5, 185);
        
		// if we're just doing a send, or writing to a file, return this
		if (toWorkingMemory || toFile)
			return new Object[] { bytes };

		// Otherwise we need to build an STRB
		else
			{
			byte instrument = 0;
			byte[] strb = new byte[] { (byte)0xF0, (byte)0x3E, (byte)0x00, DEV, (byte)0x71,
				instrument, (byte)(BB * 64 + NN), 0, (byte)0xF7 };
			strb[7] = produceChecksum(strb, 5, 7);
			return new Object[] { bytes, strb };
			}
        }


    /** Generate a Waldorf checksum of the data bytes */
    byte produceChecksum(byte[] bytes, int start, int end)
        {
        // From the sysex document:
        //
		// CHKSUM    : Sum of all data bytes truncated to 7 bits. The addition is done
		//             in 8 bit format, the result is masked to 7 bits (00h to 7Fh).
		//             IMPORTANT: The MIDI status bytes as well as the ID's are not
		//                        used for computing the checksum.
		//                        If there are no data bytes in the message (simple
		//                        request), the checksum will always be 00h.

        byte b = 0;
        for(int i = start; i < end; i++)
            b += bytes[i];
        return (byte)(b & (byte)127);
        }
        
    public int getPauseAfterChangePatch()
        {
        // perhaps just a smidgen?
        return 50;
        }

    public void changePatch(Model tempModel)
        {
        byte BB = (byte)tempModel.get("bank");
        byte NN = (byte)tempModel.get("number");
        try {
            // Bank change is CC 32
            tryToSendMIDI(new ShortMessage(ShortMessage.CONTROL_CHANGE, getChannelOut(), 32, BB));
            simplePause(getPauseAfterChangePatch());
            // Number change is PC
            tryToSendMIDI(new ShortMessage(ShortMessage.PROGRAM_CHANGE, getChannelOut(), NN, 0));
            }
        catch (Exception e) { Synth.handleException(e); }
        }

    public byte[] requestDump(Model tempModel)
        {
        if (tempModel == null)
            tempModel = getModel();
            
        byte DEV = (byte)(getID());
        byte BB = (byte)tempModel.get("bank");
        byte NN = (byte)tempModel.get("number");
        //(BB + NN)&127 is checksum
        return new byte[] { (byte)0xF0, 0x3E, 0x0E, DEV, 0x00, BB, NN, (byte)((BB + NN)&127), (byte)0xF7 };
        }
        
    public byte[] requestDump(int bank, int number, int id)
        {
        byte DEV = (byte)id;
        byte BB = (byte)bank;
        byte NN = (byte)number;
        //(BB + NN)&127 is checksum
        return new byte[] { (byte)0xF0, 0x3E, 0x0E, DEV, 0x00, BB, NN, (byte)((BB + NN)&127), (byte)0xF7 };
        }
        
    public byte[] requestCurrentDump()
        {
        byte DEV = (byte)(getID());
        //(0x75 + 0x00)&127 is checksum
        return new byte[] { (byte)0xF0, 0x3E, 0x0E, DEV, 0x00, 0x20, 0x00, (byte)((0x20 + 0x00)&127), (byte)0xF7 };
        }



    public static final int MAXIMUM_NAME_LENGTH = 16;
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

        
    
    public void parseParameter(byte[] data)
    	{
        if (data.length == 11 &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x3E &&
            data[2] == (byte)0x00 &&
            data[4] == (byte)0x60)  		// RTBP Real Time BPR Edit          
            {
    		int param = ((data[6] & 15) << 4) | (data[7] & 15);
            String key = allParameters[param];
            int val = data[8];
            
            if (key.equals("-"))
            	{
            	System.err.println("WARNING WaldorfMicrowave.parseParameter(): key was - for param " + param);
            	return;
            	}
            else if (key.equals("name"))
            	{
				char[] name = (model.get("name", "Untitled") + "                ").toCharArray();
            	name[param - 153 + 5] = (char)val;
				model.set("name", String.valueOf(name).trim());
            	}
            else if (key.equals("osc1octave") || key.equals("osc2octave"))
                {
    			model.set(key, val/16 - 2);
                }
			else if (key.equals("osc1semitone") || key.equals("osc2semitone"))
				{
    			model.set(key, val/8);
				}
			else if (key.equals("wave1volume") || key.equals("wave2volume") || key.equals("noisevolume"))
				{
    			model.set(key, val/16);
				}
			else if (key.equals("wave1levelmodifieramount") || key.equals("wave2levelmodifieramount") || key.equals("noiselevelmodifieramount"))
				{
    			model.set(key, (val - 8) / 8 - 7);
				}
			else
				{
    			model.set(key, val);
				}
// should we do this?
//    		revise();
            }
    	}
    	
    public int parse(byte[] data, boolean fromFile)
        {
        int index = -1;
        byte b = 0;
                
    	if (data.length == 11527)  // bulk
    		{
    		// extract names
            char[][] names = new char[64][16];		// both "banks"
            for(int i = 0; i < 64; i++)
                {
                for (int j = 0; j < 16; j++)
                    {
                    names[i][j] = (char)(data[180 * i + j + 153 - 5]);
                    }
                }

            String[] n = new String[64];
            for(int i = 0; i < 64; i++)
                {
                if (i < 32)
                	{
                	n[i] = "A" + (i < 9 ? "0" : "") + (i + 1) + "  " + String.valueOf(names[i]);
                	}
                else
                	{
                	n[i] = "B" + (i < 41 ? "0" : "") + (i - 32 + 1) + "  " + String.valueOf(names[i]);
                	}
                }
             
            // Now that we have an array of names, one per patch, we present the user with options;
            // 0. Cancel [handled automatically]
            // 1. Save the bank data [handled automatically]
            // 2. Upload the bank data [handled automatically] 
            // 3. Load and edit a certain patch number
            int patchNum = showBankSysexOptions(data, n);
            if (patchNum < 0) return PARSE_CANCELLED;
            else return extractPatch(data, 5 + 180 * patchNum, patchNum);
    		}
    	else if (data[4] == 0x4B)	// ABPD, includes instrument number which we must skip
    		return extractPatch(data, 6, -1);
    	else return extractPatch(data, 5, -1);
    	}
    	
    	
    	// Extracts a single patch from either single or "bank" patch data
    	public int extractPatch(byte[] data, int offset, int number)
    		{
    		if (number >= 0)	// valid number
    			{
    			model.set("bank", number / 32);
    			model.set("number", number % 32);
    			}
    			
    		char[] name = new char[16];
    		for(int i = 0; i < 16; i++)
    			{
    			name[i] = (char)(data[i + 153 - 5 + offset]);
    			}
    		model.set("name", String.valueOf(name).trim());
    			
    		for(int i = 0; i < allParameters.length; i++)
    			{
				String key = allParameters[i];
				int val = data[i +  offset];
				if (key.equals("-"))
					{
					continue;
					}
				else if (key.equals("name"))
					{
					continue;
					}
				else if (key.equals("osc1octave") || key.equals("osc2octave"))
					{
					model.set(key, val / 16 - 2);
					}
				else if (key.equals("osc1semitone") || key.equals("osc2semitone"))
					{
					model.set(key, val / 8);
					}
				else if (key.equals("wave1volume") || key.equals("wave2volume") || key.equals("noisevolume"))
					{
					model.set(key, val / 16);
					}
				else if (key.equals("wave1levelmodifieramount") || key.equals("wave2levelmodifieramount") || key.equals("noiselevelmodifieramount"))
					{
					model.set(key, (val - 8) / 8 - 7);
					}
				else
					{
					model.set(key, val);
					}
    			}
        revise();
        return PARSE_SUCCEEDED;     
        }
  
    
    public static String getSynthName() { return "Waldorf MicroWave"; }
    
    public String getPatchName(Model model) { return model.get("name", "Init Sound V1.1 "); }
    
    public byte getID() 
        { 
        try 
            { 
            byte b = (byte)(Byte.parseByte(tuple.id));
            if (b >= 0) return b;
            }
        catch (NullPointerException e) { } // expected.  Happens when tuple's not built yet
        catch (NumberFormatException e) { Synth.handleException(e); }
        return 0;
        }
        
    public String reviseID(String id)
        {
        try 
            { 
            byte b =(byte)(Byte.parseByte(id)); 
            if (b >= 0) return "" + b;
            } 
        catch (NumberFormatException e) { }             // expected
        return "" + getID();
        }

    public Model getNextPatchLocation(Model model)
        {
        int bank = model.get("bank");
        int number = model.get("number");
        
        number++;
        if (number >= 32)
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

    public String getPatchLocationName(Model model)
        {
        // getPatchLocationName() is called from sprout() as a test to see if we should enable
        // batch downloading.  If we haven't yet created an .init file, then parameters won't exist
        // yet and this method will bomb badly.  So we return null in this case.
        if (!model.exists("number")) return null;
        if (!model.exists("bank")) return null;
        
        int number = model.get("number") + 1;
        return BANKS[model.get("bank")] +  (number > 99 ? "" : (number > 9 ? "0" : "00")) + number;
        }




// Librarian Support
    public String[] getBankNames() { return BANKS; }
    public boolean[] getWriteableBanks() { return new boolean[]{ true, true }; }
    public String[] getPatchNumberNames() { return buildIntegerNames(32, 1); }
    // Though we *can* do bank writes, weirdly the MicroWave requires that
    // we write both banks in the same message.  We're not going to permit that.
    public boolean getSupportsPatchWrites() { return true; }
    public int getPatchNameLength() { return 16; }
	public int parseFromBank(byte[] bankSysex, int number) 
		{ 
		return extractPatch(bankSysex, 5 + 180 * number, number);
		}
	public int[] getBanks(byte[] bankSysex) { return new int[] { 0, 1 }; }
		
    public byte[] requestAllDump() 
        { 
        return new byte[] 
        	{ 
			(byte)0xF0, 
			0x3E, 				// Waldorf
			0x00,				// Microwave 1 
			(byte)getID(),
			0x10, 				// BPBR (Sound Program (BPR) Bank Dump Request)
			0,	// checksum
			(byte)0xF7
			};
		}

    public boolean librarianTested() { return true; }
    }




/*
Notes

SEMITONE
	"6     Osc 1 Semitone (0...120, 1 Semitone equals a value of 8)"
	However the manual says semitone goes 0...12.  12*8 is not 120.  What?
	For the moment I am assuming that semitone only goes to 96.
	
*/