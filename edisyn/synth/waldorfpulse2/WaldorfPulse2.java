/***
    Copyright 2021 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.waldorfpulse2;

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
   A patch editor for the Waldorf Pulse2. 
        
   @author Sean Luke
*/

public class WaldorfPulse2 extends Synth
    {
    public static final String[] OSC1_WAVE = new String[] { "Pulse Width", "Sawtooth", "Triangle", "Alternate Pulse Width", "Unison Monophonic", "Unison Polyphonic", "Unison Monophonic Alternate Pulse Width", "Unison Polyphonic Alternate Pulse Width", "Paraphonic 8 Voice", "Paraphonic 4 Voice" };
    public static final String[] OSC2_WAVE = new String[] { "Pulse Width", "Sawtooth", "Triangle", "Pulse Width Synced to OSC 3", "Pulse Width Synced to OSC 1" };
    public static final String[] OSC3_WAVE = new String[] { "Square", "Sawtooth", "Triangle", "External Input", "Feedback" };
    public static final String[] OSC3_SOURCE = new String[] { "Osc Mixer", "Osc 2 Level", "Filter Cutoff", "Drive Level" };
    public static final String[] LFO_BARS = new String[] { "32", "24", "16", "12", "8", "6", "5", "4", "3", "2", "3/2", "4/3", "1", "3/4", "2/3", "1/2", "3/8", "1/3", "1/4", "3/16", "1/6", "1/8", "3/32", "1/12", "1/16", "3/64", "1/24", "1/32", "3/128", "1/48", "1/64", "1/96" };
    public static final String[] LFO_WAVE = new String[] { "Sine", "Triangle", "Sawtooth", "Square", "S & H", "Clocked Sine", "Clocked Triangle", "Clocked Sawtooth", "Clocked Square", "Clocked S & H" };
    public static final String[] GLIDE_MODE = new String[] { "Off", "Continuous", "Fingered", "Fingered on Press", "Fingered on Release" };
    public static final String[] LOOPING_MODE = new String[] { "Off", "Attack-Decay", "Decay-Decay" };
    public static final String[] TRIGGER_MODE = new String[] { "Single", "Single w/o Reset", "Trigger Every Time", "Trigger Every Time w/o Reset", "Trigger on Press" };
    public static final String[] FILTER_TYPE = new String[] { "LP 24dB", "LP 12dB", "BP 12dB", "HP 12dB"};
    public static final String[] DRIVE_TYPE = new String[] { "None", "Tube Distortion", "Fuzz Distortion", "Effects Board Output" };
    public static final String[] MODULATION_SOURCE = new String[] { "Off", "LFO1 Output", "LFO1 x Mod Wheel", "LFO1 x Pressure", "LFO2", "LFO2 x Amplifier Envelope", "Filter Envelope", "Amplifier Envelope", "Keyboard Velocity", "Pitch Keytracking", "Pitch Follower (with Glide)", "Pitch Bend", "Mod Wheel", "Pressure", "Breath Controller", "Control X", "Keyboard Release Velocity", "Modulation Accumulator", "Modulation Productor", "Modulation Delay Line", "Modulation Smoother", "Modulation Minimizer", "Modulation Maximizer", "Highest Note Pressed", "Lowest Note Pressed" };
    // The manual doesn't list the destinations after CV Out, but they're in the unit
    public static final String[] MODULATION_DESTINATION = new String[] { "Global Oscillator Pitch", "Oscillator 1 Pitch", "Oscillator 2 Pitch", "Oscillator 3 Pitch", "Oscillator 1 Pulse Width", "Oscillator 2 Pulse Width", "Oscillator 1 Level", "Oscillator 2 Level", "Oscillator 3 Level", "Noise Level", "Filter Cutoff", "Filter Resonance", "Volume", "Panning", "LFO 1 Speed", "Mod 1 Amount", "Drive", "Glide Rate", "Filter Env Rates", "Amp Env Rates", "Unison Detune", "Paraphonic Fade 1", "Paraphonic Fade 2", "Arpeggiator Swing", "CV Out", "Modulation Accumulator", "Modulation Productor", "Modulation Delay Line", "Modulation Smoother", "Modulation Minimizer", "Modulation Maximizer", "VCF Env Amount" };
    public static final String[] SOUND_CATEGORY = new String[] { "None", "Arpeggiated", "Atmospheric", "Bass", "Drum Kit", "Special Effect", "Keys", "Lead", "Monophonic", "Pad", "Percussive", "Polyphonic", "Sound Sequence", "Dub", "Lushy", "String", "Puck", "Wind", "Noise", "External Input", "Init" };
    public static final String[] ARPEGGIATOR_STATE = new String[] { "Off", "On", "Hold" };
    public static final String[] ARPEGGIATOR_MODE = new String[] { "Up Time-Ordered", "Down Time-Ordered", "Up/Down Time-Ordered", "Random", "Up Pitch-Ordered", "Down Pitch-Ordered", "Up/Down Pitch-Ordered" };
    public static final String[] ARPEGGIATOR_CLOCK = new String[] { "1", "3/4", "2/3", "1/2", "3/8", "1/3", "1/4", "3/16", "1/6", "1/8", "3/32", "1/12", "1/16", "3/64", "1/24", "1/32", "3/128", "1/48", "1/64", "1/96" };
    public static final String[] ARPEGGIATOR_STEP_TYPE = new String[] { "Step Off", "Soft Step", "Step", "Hard Step", "Even Note", "Odd Note", "1 Octave Above", "First Chord Note", "Reset" };
    public static final String[] ARPEGGIATOR_STEP_LENGTH = new String[] { "25%", "50%", "75%", "100%" };    
    public static final String[] ARPEGGIATOR_STEP_DURATION = new String[] { "12%", "19%", "25%", "38%", "50%", "75%", "100%", "150%", "200%", "300%", "400%", "500%", "800%", "1200%", "1600%" };

    public static final int INVALID = -1;
    /** Parameters corresponding to incoming CC values */
    public static final int[] ccToParameter = 
        {
        INVALID, INVALID, INVALID, INVALID, INVALID, 22, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, 24, 25, 
        26, 27, 30, 31, 32, 33, INVALID, INVALID, 18, 19, 20, 21, INVALID, 29, INVALID, 35, 
        INVALID, INVALID, 0, 1, 8, 9, 6, 7, 10, 16, 14, 15, 12, 5, 11, 17, 
        51, 45, 42, 46, 44, 47, INVALID, INVALID, 43, 52, 53, 50, INVALID, INVALID, 23, INVALID, 
        INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, 2, 3, 13, 4, 28, 34, 48, 49, INVALID, 36, 
        37, 38, 39, 41, 83, 84, 85, 86, 86, INVALID, 66, 67, 68, 69, 70, 71, 
        72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, INVALID, 54, 55, 56, 57, 
        58, 59, 60, 61, 62, 63, 64, 65, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID, INVALID
        };


    public WaldorfPulse2()
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
        hbox.addLast(addGeneral(Style.COLOR_B()));
        vbox.add(hbox);

        vbox.add(addOscillator(1, Style.COLOR_A()));
        vbox.add(addOscillator(2, Style.COLOR_A()));

        vbox.add(addOscillator(3, Style.COLOR_A()));
        
        hbox = new HBox();
        hbox.add(addFilter(Style.COLOR_B()));
        hbox.addLast(addLFO(1, Style.COLOR_C()));
        vbox.add(hbox);

        hbox = new HBox();
        hbox.add(addAmplifier(Style.COLOR_B()));
        hbox.addLast(addLFO(2, Style.COLOR_C()));
        vbox.add(hbox);

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("General", soundPanel);
                
                
        // LFO and ENVELOPE PANEL
                
        JComponent lfoEnvelopePanel = new SynthPanel(this);
        vbox = new VBox();

        vbox.add(addEnvelope(1, Style.COLOR_B()));
        vbox.add(addEnvelope(2, Style.COLOR_B()));
        

        vbox.add(addModulation(Style.COLOR_A()));

        lfoEnvelopePanel.add(vbox, BorderLayout.CENTER);
        addTab("Envelopes and Modulation", lfoEnvelopePanel);
                
        // ARPEGGIATOR PANEL
        JComponent arpeggiationPanel = new SynthPanel(this);
        
        vbox = new VBox();
        vbox.add(addArpeggiator(Style.COLOR_A()));
        vbox.add(addArpeggiatorPattern(Style.COLOR_B()));
        arpeggiationPanel.add(vbox, BorderLayout.CENTER);
        addTab("Arpeggiator", arpeggiationPanel);
                
        model.set("name", "Init");
        
        loadDefaults();
        }
                
    
    public String getDefaultResourceFileName() { return "WaldorfPulse2.init"; }
    public String getHTMLResourceFileName() { return "WaldorfPulse2.html"; }
                
                


        
    public JComponent addNameGlobal(Color color)
        {
        Category globalCategory = new Category(this, "Waldorf Pulse 2", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
                
        VBox vbox = new VBox();
        HBox hbox2 = new HBox();
        comp = new PatchDisplay(this, 4, false);
        hbox2.add(comp);
        vbox.add(hbox2);
        
        comp = new StringComponent("Patch Name", this, "name", 14, "Name must be up to 14 ASCII characters.")
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

        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }

        
    public JComponent addGeneral(Color color)
        {
        Category category = new Category(this, "General", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();

        params = SOUND_CATEGORY;
        comp = new Chooser("Category", this, "class", params);
        model.setStatus("class", Model.STATUS_IMMUTABLE);
        vbox.add(comp);

        params = GLIDE_MODE;
        comp = new Chooser("Glide Mode", this, "glidemode", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Glide Rate", this, "gliderate", color, 0, 127);
        hbox.add(comp);
                
        comp = new LabelledDial("Bend Up", this, "bendupwards", color, 0, 36);
        hbox.add(comp);

        comp = new LabelledDial("Bend Down", this, "benddown", color, 0, 36);
        hbox.add(comp);

        comp = new LabelledDial("Unison", this, "unisondetune", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Detune");
        hbox.add(comp);

        category.add(hbox, BorderLayout.WEST);
        return category;
        }


    public JComponent addLFO(final int lfo, Color color)
        {
        Category category = new Category(this, "LFO " + lfo, color);
        category.makePasteable("lfo");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();

        final LabelledDial comp1 = new LabelledDial("Speed", this, "lfo" + lfo + "speed", color, 0, 127)
            {
            public String map(int value)
                {
                if (lfo == 2)
                    {
                    return "" + value;
                    }
                else if (model.get("lfo" + lfo + "shape", 0) >= 5)      // clocked
                    {
                    return LFO_BARS[value / 4];
                    }
                else
                    {
                    return "" + value;
                    }
                }
            };

        
        if (lfo == 1)
            {
            params = LFO_WAVE;
            comp = new Chooser("Shape", this, "lfo" + lfo + "shape", params)
                {
                public void update(String key, Model model)
                    {
                    super.update(key, model);
                    comp1.update("lfo" + lfo + "speed", model);
                    }
                };
            vbox.add(comp);
            hbox.add(vbox);
            }

        hbox.add(comp1);
                
        if (lfo == 2)
            {
            comp = new LabelledDial("Delay", this, "lfo" + lfo + "delay", color, 0, 127);
            hbox.add(comp);    
            }

        category.add(hbox, BorderLayout.WEST);
        return category;
        }


    public JComponent addOscillator(final int osc, Color color)
        {
        Category category = new Category(this, "Oscillator " + osc, color);
        category.makePasteable("osc");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
                
        params = (osc == 1 ? OSC1_WAVE : (osc == 2 ? OSC2_WAVE : OSC3_WAVE));
        comp = new Chooser("Shape", this, "osc" + osc + "shape", params);
        vbox.add(comp);

        if (osc != 3)
            {
            comp = new CheckBox("Keytrack", this, "osc" + osc + "keytrack");
            vbox.add(comp); 
            hbox.add(vbox);
            }
        else
            {
            comp = new CheckBox("Sync to Osc 2", this, "osc" + osc + "syncosc2");
            vbox.add(comp);
            hbox.add(vbox);
                        
            vbox = new VBox();
            params = OSC3_SOURCE;
            comp = new Chooser("Routing", this, "osc" + osc + "routing", params);
            vbox.add(comp);
            hbox.add(vbox);
            }
            
        if (osc != 3)
            {
            comp = new LabelledDial("Pulsewidth", this, "osc" + osc + "pulsewidth", color, 0, 127);
            hbox.add(comp);

            comp = new LabelledDial("Amp Env Fade ", this, "osc" + osc + "envafade", color, 0, 127);
            hbox.add(comp);
            }
                
        comp = new LabelledDial("Semitone", this, "osc" + osc + "semitone", color, 16, 112);
        hbox.add(comp);

        comp = new LabelledDial("Detune", this, "osc" + osc + "detune", color, 0, 127, 64);
        hbox.add(comp);

        comp = new LabelledDial("Level", this, "osc" + osc + "level", color, 0, 127);
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
        

    public JComponent addFilter(Color color)
        {
        Category category = new Category(this, "Filter", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();

        params = FILTER_TYPE;
        comp = new Chooser("Type", this, "vcftype", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Cutoff", this, "vcfcutoff", color, 0, 127);
        hbox.add(comp);
                
        comp = new LabelledDial("Resonance", this, "vcfresonance", color, 0, 127);
        hbox.add(comp);
                
        comp = new LabelledDial("Filter Env Amount", this, "vcfenv1amount", color, 0, 127);
        hbox.add(comp);                

        comp = new LabelledDial("Keytrack", this, "vcfkeytrack", color, 0, 127, 64);
        hbox.add(comp);
                
        comp = new LabelledDial("Velocity", this, "vcfvelocity", color, 0, 127, 64);
        hbox.add(comp);                

        category.add(hbox, BorderLayout.WEST);
        return category;
        }
        
    public JComponent addAmplifier(Color color)
        {
        Category category = new Category(this, "Amplifier", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();

        params = DRIVE_TYPE;
        comp = new Chooser("Drive Curve", this, "vcadrivecurve", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Drive", this, "vcadrive", color, 0, 127);
        hbox.add(comp);
                
        comp = new LabelledDial("Panning", this, "vcapanning", color, 0, 127)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val)
                {
                val -= 64;
                if (val < 0) return "L " + Math.abs(val);
                else if (val > 0) return "R " + val;
                else return "--";
                }
            };
        hbox.add(comp);
                
        comp = new LabelledDial("Noise", this, "noiselevel", color, 0, 127);
        hbox.add(comp);
                
        comp = new LabelledDial("Volume", this, "vcavolume", color, 0, 127);
        hbox.add(comp);                

        comp = new LabelledDial("Velocity", this, "vcavelocity", color, 0, 127, 64);
        hbox.add(comp);                

        category.add(hbox, BorderLayout.WEST);
        return category;
        }
        
        

    public JComponent addEnvelope(final int envelope, Color color)
        {
        Category category = new Category(this, (envelope == 1 ? "Filter" : "Amplifier") + " Envelope", color);
        category.makePasteable("env");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        params = TRIGGER_MODE;
        comp = new Chooser("Trigger", this, "env" + envelope + "trigger", params);
        vbox.add(comp);

        params = LOOPING_MODE;
        comp = new Chooser("Loop", this, "env" + envelope + "loop", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Attack", this, "env" + envelope + "attack", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Decay", this, "env" + envelope + "decay", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Sustain", this, "env" + envelope + "sustain", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Release", this, "env" + envelope + "release", color, 0, 127);
        hbox.add(comp);

        // ADSR
        /*
          comp = new EnvelopeDisplay(this, Color.red, 
          new String[] { null, "env" + envelope + "attack", "env" + envelope + "decay", null, "env" + envelope + "release" },
          new String[] { null, null, "env" + envelope + "sustain", "env" + envelope + "sustain", null },
          new double[] { 0, 0.3333, 0.3333,  0.3333, 0.3333},
          new double[] { 0, 1.0, 1.0 / 127.0, 1.0/127.0, 0 },
          new double[] { 0, (Math.PI/4/127),   (Math.PI/4/127), 0, (Math.PI/4/127)});
        */
        comp = new EnvelopeDisplay(this, Color.red, 
            new String[] { null, "env" + envelope + "attack", "env" + envelope + "decay", null, "env" + envelope + "release" },
            new String[] { null, null, "env" + envelope + "sustain", "env" + envelope + "sustain", null },
            new double[] { 0, 0.25/127.0, 0.25 / 127.0,  0.25, 0.25/127.0},
            new double[] { 0, 1.0, 1.0 / 127.0, 1.0/127.0, 0 });

        hbox.addLast(comp);
                        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addModulation(Color color)
        {
        Category category  = new Category(this, "Modulation", color);
        category.makeDistributable("mod");
                        
        JComponent comp;
        String[] params;
        VBox main = new VBox();
        HBox hbox;
        VBox vbox;
        
        for(int row = 1; row < 9; row += 2)
            {
            hbox = new HBox();
            for(int i = row; i < row + 2; i++)
                {
                if (i != row)
                    {
                    hbox.add(Strut.makeHorizontalStrut(16));
                    }
                    
                vbox = new VBox();
                params = MODULATION_SOURCE;
                comp = new Chooser("Source " + i, this, "mod" + i + "source", params);
                vbox.add(comp);

                params = MODULATION_DESTINATION;
                comp = new Chooser("Target " + i, this, "mod" + i + "target", params);
                vbox.add(comp);
                hbox.add(vbox);

                comp = new LabelledDial("Level " + i, this, "mod" + i + "amount", color, 0, 127, 64);
                hbox.add(comp);
                }
            main.add(hbox);
            if (row < 7)
                {
                main.add(Strut.makeVerticalStrut(12));
                }
            }
                                
        category.add(main, BorderLayout.WEST);
        return category;
        }

    public JComponent addArpeggiator(Color color)
        {
        Category category  = new Category(this, "Arpeggiator", color);
                        
        JComponent comp;
        String[] params;
        HBox hbox;
        VBox vbox;

        /*
         *  36            24h    : ARP ACTIVE                    (  0 -   2)        0    CC : 79 
         // ARPEGGIATOR_STATE
         *  87            57h    : STEP DURATION                 (  0 -  14)        6    CC : 84 
         // values 12% ... 1600%    12 19 25 38 50 75 100 150 200 300 400 500 800 1200 1600 
         *  88            58h    : ARP SWING                     ( 14 - 114)       64    CC : 85 
         // 0% ... 100%
         *  89            59h    : ARP DELAY                     ( 14 - 114)       64    CC : 86
         // -50% ... 50%
         *  106           6ah    : PATTERN LENGTH                (  0 -  15)       15    CC : 87 
         // 1 ..16
         *  107           6bh    : ACCENT CONTROL                (  0 -  22)       13    CC : 88 
         // mod sources
         *  37            25h    : ARP RANGE                     (  0 -   9)        1    CC : 80 
         // 1 .. 10
         *  38            26h    : ARP TEMPO                     (  0 - 127)       37    CC : 81 
         // 46 ... 300 this is 46 + val*2
         *  39            27h    : ARP CLOCK                     (  0 -  19)        9    CC : 82 
         // ARPEGGIATOR_CLOCK
         *  40            28h    : ARP PATTERN                   (  0 -  14)        0    
         // -- UNKNOWN, BELIEVED TO BE OBSOLETE --
         *  41            29h    : ARP MODE                      (  0 -   6)        0    CC : 83 
         // ARPEGGIATOR_MODE
         */

        hbox = new HBox();
                
        vbox = new VBox();
        params = ARPEGGIATOR_STATE;
        comp = new Chooser("Active", this, "arpactive", params);
        vbox.add(comp);


        params = ARPEGGIATOR_STEP_DURATION;
        comp = new Chooser("Step Duration", this, "stepduration", params);
        vbox.add(comp);
        model.setMetricMinMax("stepduration", 0, ARPEGGIATOR_STEP_DURATION.length - 1);

        hbox.add(vbox);

        vbox = new VBox();

        params = ARPEGGIATOR_MODE;
        comp = new Chooser("Mode", this, "arpmode", params);
        vbox.add(comp);

        params = MODULATION_SOURCE;
        comp = new Chooser("Accent Control", this, "accentcontrol", params);
        vbox.add(comp);

        hbox.add(vbox);

        comp = new LabelledDial("Range", this, "arprange", color, 0, 9, -1);
        hbox.add(comp);                

        comp = new LabelledDial("Tempo", this, "arptempo", color, 0, 127)
            {
            public String map(int value)
                {
                return "" + (46 + value * 2);
                }
            };
        hbox.add(comp);                
                        
        comp = new LabelledDial("Clock", this, "arpclock", color, 0, 19)
            {
            public String map(int value)
                {
                return ARPEGGIATOR_CLOCK[value];
                }
            };
        hbox.add(comp);                

        comp = new LabelledDial("Swing", this, "arpswing", color, 14, 114)
            {
            public String map(int value)
                {
                return "" + (value - 14) + "%";
                }
            };
        hbox.add(comp);                
                        
        comp = new LabelledDial("Delay", this, "arpdelay", color, 14, 114)
            {
            public boolean isSymmetric()
                {
                return true;
                }
                        
            public String map(int value)
                {
                return "" + (value - 50 -  14) + "%";
                }
            };
        hbox.add(comp);                
                        
        comp = new LabelledDial("Pattern Length", this, "patternlength", color, 0, 15, -1);
        hbox.add(comp);      
        
        category.add(hbox, BorderLayout.WEST);
        return category;
        }               
                
    public JComponent addArpeggiatorPattern(Color color)
        {
        Category category  = new Category(this, "Arpeggiator Pattern", color);
        category.makeDistributable("patternstep");
                        
        JComponent comp;
        String[] params;
        VBox main = new VBox();
        HBox hbox;
        VBox vbox;

        for(int row = 1; row < 17; row += 4)
            {
            hbox = new HBox();
            for(int i = row; i < row + 4; i++)
                {
                if (i != row)
                    {
                    hbox.add(Strut.makeHorizontalStrut(16));
                    }
                    
                vbox = new VBox();
                params = ARPEGGIATOR_STEP_TYPE;
                comp = new Chooser("Type " + i, this, "patternstep" + i + "steptype", params);
                vbox.add(comp);

                params = ARPEGGIATOR_STEP_LENGTH;
                comp = new Chooser("Length " + i, this, "patternstep" + i + "steplength", params);
                vbox.add(comp);

                comp = new CheckBox("Glide", this, "patternstep" + i + "stepglide");
                vbox.add(comp);
                hbox.add(vbox);
                }
            main.add(hbox);
            if (row < 13)
                {
                main.add(Strut.makeVerticalStrut(16));
                }
            }
                                
        category.add(main, BorderLayout.WEST);
        return category;
        }

    /** Map of parameter -> index in the allParameters array. */
    HashMap allParametersToIndex = new HashMap();


    /** List of all Waldorf parameters in order.  "-" is a reserved (unused and thus unnamed) parameter. */

    final static String[] allParameters = new String[] 
    {
    "osc1shape",
    "osc1pulsewidth",
    "osc1semitone",
    "osc1detune",
    "osc1keytrack",
    "osc1level",
    "osc2shape",
    "osc2pulsewidth",
    "osc2semitone",
    "osc2detune",
    "osc2keytrack",
    "osc2level",
    "osc3shape",
    "osc3routing",
    "osc3semitone",
    "osc3detune",
    "osc3syncosc2",
    "osc3level",
    "lfo1speed",
    "lfo1shape",
    "lfo2speed",
    "lfo2delay",
    "gliderate",
    "glidemode",
    "env1attack",
    "env1decay",
    "env1sustain",                                   
    "env1release",
    "env1loop",                   
    "env1trigger",
    "env2attack",
    "env2decay",
    "env2sustain",                                   
    "env2release",
    "env2loop",                   
    "env2trigger",
    "arpactive",
    "arprange",
    "arptempo",
    "arpclock",
    "-",                                                        // "arppattern",
    "arpmode",
    "vcfcutoff",
    "vcfresonance",
    "vcfenv1amount",
    "vcftype",
    "vcfkeytrack",
    "vcfvelocity",
    "vcadrive",
    "vcadrivecurve",
    "vcapanning",
    "noiselevel",
    "vcavolume",
    "vcavelocity",
    "mod1source",
    "mod1amount",                                   
    "mod1target",
    "mod2source",
    "mod2amount",                                   
    "mod2target",
    "mod3source",
    "mod3amount",                                   
    "mod3target",
    "mod4source",
    "mod4amount",                                   
    "mod4target",
    "mod5source",
    "mod5amount",                                   
    "mod5target",
    "mod6source",
    "mod6amount",                                   
    "mod6target",
    "mod7source",
    "mod7amount",                                   
    "mod7target",
    "mod8source",
    "mod8amount",                                   
    "mod8target",
    "unisondetune",                   
    "osc1envafade",
    "osc2envafade",
    "-",
    "-",
    "-",
    "-",
    "bendupwards",
    "benddown",
    "stepduration",
    "arpswing",
    "arpdelay",
    "patstep1",
    "patstep2",
    "patstep3",
    "patstep4",
    "patstep5",
    "patstep6",
    "patstep7",
    "patstep8",
    "patstep9",
    "patstep10",
    "patstep11",
    "patstep12",
    "patstep13",
    "patstep14",
    "patstep15",
    "patstep16",
    "patternlength",
    "accentcontrol",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",  // "name1",
    "-",  // "name2",
    "-",  // "name3",
    "-",  // "name4",
    "-",  // "name5",
    "-",  // "name6",
    "-",  // "name7",
    "-",  // "name8",
    "-",  // "name9",
    "-",  // "name10",
    "-",  // "name11",
    "-",  // "name12",
    "-",  // "name13",
    "-",  // "name14",
    "class",
    };





    // READING AND WRITING

    public Object[] emitAll(String key)
        {
        if (!getSendMIDI()) return new Object[0];  // MIDI turned off, don't bother
        if (key.equals("number")) return new Object[0];  // this is not emittable
        
        byte DEV = (byte)(getID());
        
        if (key.startsWith("patternstep"))
            {
            int i = StringUtility.getInt(key);
            int stepType = model.get("patternstep" + i + "steptype");
            int stepLength = model.get("patternstep" + i + "steplength");
            int stepGlide = model.get("patternstep" + i + "stepglide");
            int val = ((stepGlide & 0x1) << 6) | ((stepLength & 0x3) << 4) | ((stepType & 0xF) << 0);

            byte[] data = new byte[] { (byte)0xF0, 0x3E, 0x16, DEV, 0x20, (byte)(89 + i), (byte)val, (byte)0xF7 };
            return new Object[] { data };
            }
        else if (key.equals("name"))
            {
            Object[] data = new Object[14];
            String name = model.get(key, "Init") + "             "; 
            for(int i = 0; i < 14; i++)
                {
                int index = i + 113;
                byte XX = (byte)(name.charAt(i));
                byte[] b = new byte[] { (byte)0xF0, 0x3E, 0x16, DEV, 0x20, (byte)index, XX, (byte)0xF7 };
                data[i] = b;
                }
            return data;
            }
        else
            {
            int index = ((Integer)(allParametersToIndex.get(key))).intValue();
            byte XX = (byte)model.get(key);
            byte[] data = new byte[] { (byte)0xF0, 0x3E, 0x16, DEV, 0x20, (byte)index, XX, (byte)0xF7 };
            return new Object[] { data };
            }
        }
    

    
    public byte[] emit(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        if (tempModel == null)
            tempModel = getModel();
        byte DEV = (byte)(getID());
        int num = tempModel.get("number");
        byte NN = (byte)(num % 128);
        byte BB = (byte)(num / 128);
        if (toWorkingMemory) { BB = 0x7F; NN = 0x0; }
        String name = model.get("name", "Init") + "              ";  // has to be 14 long

        byte[] bytes = new byte[128];
        
        for(int i = 0; i < 128; i++)
            {
            if (i == 40)            // arppattern
                {
                bytes[i] = 0;           // I think this one is vestigial
                }
            else if (i >= 81 && i <= 84) 
                {
                bytes[i] = 0;                   // empty
                }
            else if (i >= 90 && i <= 105)   // pattern steps
                {
                int stepType = model.get("patternstep" + (i - 90 + 1) + "steptype");
                int stepLength = model.get("patternstep" + (i - 90 + 1) + "steplength");
                int stepGlide = model.get("patternstep" + (i - 90 + 1) + "stepglide");
                int val = ((stepGlide & 0x1) << 6) | ((stepLength & 0x3) << 4) | ((stepType & 0xF) << 0);
                bytes[i] = (byte)val;
                }
            else if (i >= 108 && i <= 112) 
                {
                if (i == 112) bytes[i] = 0x01;          // dunno why
                else bytes[i] = 0;                      // empty
                }
            else if (i >= 113 && i <= 126)  // name
                {
                bytes[i] = (byte)(name.charAt(i - 113));
                }
            else
                {
                String key = allParameters[i];
                bytes[i] = (byte)(model.get(key));
                }
            }

        byte[] full = new byte[9 + 128];
        full[0] = (byte)0xF0;
        full[1] = 0x3E;
        full[2] = 0x16;
        full[3] = DEV;
        full[4] = 0x10;
        full[5] = BB;
        full[6] = NN;
        System.arraycopy(bytes, 0, full, 7, bytes.length);
        // checksum range is different on the Pulse than on other waldorf units
        full[9 + 128 - 2] = produceChecksum(full, 1, full.length - 3);
        full[9 + 128 - 1] = (byte)0xF7;

        return full;
        }

                
    public void handleSynthCCOrNRPN(Midi.CCData data)
        {
        int parameter = ccToParameter[data.number];
        int value = data.value;
        if (parameter == INVALID) return;       // ugh
        else model.set(allParameters[parameter], value);
        }
        
    public boolean getExpectsRawCCFromSynth()
        {
        return true;
        }

    public void parseParameter(byte[] data) 
        {
        if (data.length == 8 &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x3E &&
            data[2] == (byte)0x16 &&
            data[4] == (byte)0x20 &&
            data[7] == (byte)0xF7)
            {
            int parameter = data[5];
            int value = data[6];
            if (parameter == INVALID) return;       // ugh
            else model.set(allParameters[parameter], value);
            }
        }

    public int parse(byte[] data, boolean fromFile)
        {
        if (data[5] != 0x7F)  
            {
            model.set("number", (data[5] * 128 + data[6]));
            }
        
        int offset = 7;
        
        char[] name = new char[14];
        
        for(int i = 0; i < 128; i++)
            {
            if (i == 40)            // arppattern
                {
                // do nothing           // I think this one is vestigial
                }
            else if (i >= 81 && i <= 84) 
                {
                // do nothing                   // empty
                }
            else if (i >= 90 && i <= 105)   // pattern steps
                {
                int val = (data[i + offset] & 127);
                int steptype = val & 0xF;
                if (steptype > 8)       // this happens, let's set it to "reset", which appears to be what was intended
                    {
                    System.err.println("Warning (WaldorfPulse2.parse): Revised " + 
                        "patternstep" + (i - 90 + 1) + "steptype" + " from " + steptype + " to 8 (Reset)");
                    steptype = 8;
                    }
                model.set("patternstep" + (i - 90 + 1) + "steptype", steptype);
                model.set("patternstep" + (i - 90 + 1) + "steplength", (val >> 4) & 0x3);
                model.set("patternstep" + (i - 90 + 1) + "stepglide", (val >> 6) & 0x1);
                }
            else if (i >= 108 && i <= 112) 
                {
                // do nothing                   // empty
                }
            else if (i >= 113 && i <= 126)  // name
                {
                // do nothing
                name[i - 113] = (char)data[i + offset];
                }
            else
                {
                String key = allParameters[i];
                model.set(key, data[i + offset]);
                }
            }
        model.set("name", new String(name));
        revise();       
        return PARSE_SUCCEEDED;     
        }




    /** Generate a Waldorf checksum of the data bytes from start to end inclusive */
    byte produceChecksum(byte[] bytes, int start, int end)
        {
        //      From the sysex document:
        //
        //      "Sum of all databytes truncated to 7 bits.
        //  The addition is done in 8 bit format, the result is    
        //  masked to 7 bits (00h to 7Fh). A checksum of 7Fh is
        //  always accepted as valid.
        //  IMPORTANT: the MIDI status-bytes as well as the 
        //  ID's are not used for computing the checksum."
        
        // NOTE: it appears that the WaldorfPulse2's sysex does NOT include
        // the NN or DD data bytes.        
        
        byte b = 0;  // I *think* signed will work
        for(int i = start; i < end + 1; i++)
            b += bytes[i];
        
        b = (byte)(b & (byte)127);
        
        return b;
        }

    public int getPauseAfterChangePatch() { return 200; }

    public void changePatch(Model tempModel)
        {
        int num = tempModel.get("number");
        byte NN = (byte)(num % 100);                            // notice this is 100, not 128, unlike requestDump
        byte BB = (byte)(num / 100);
        try 
            {
            // Bank change is CC 32
            tryToSendMIDI(new ShortMessage(ShortMessage.CONTROL_CHANGE, getChannelOut(), 32, BB));
            // Number change is PC
            tryToSendMIDI(new ShortMessage(ShortMessage.PROGRAM_CHANGE, getChannelOut(), NN, 0));
            }
        catch (Exception e) 
            { 
            e.printStackTrace(); 
            }
        }

    public void performRequestDump(Model tempModel, boolean changePatch)
        {
        /// The Pulse 2 has a serious bug in how it downloads patches.  If you request
        /// a patch, and it's an *init patch*, the Pulse 2 will simply not respond at all.
        /// That's bad.
        ///
        /// So the strategy I'm taking is this: if changePatch is true
        /// (we're allowed to change the patch) then we'll do a change patch
        /// and then a REQUEST CURRENT PATCH.  That should work for stuff like
        /// batch downloads.
        ///
        /// Otherwise we just do a standard request dump and hope that it's not an init patch.
        
        if (changePatch)
            {
            performChangePatch(tempModel);

            // tempModel has to be non-null for performChangePatch to work anyway, but
            // just in case...
            if (tempModel == null)
                tempModel = getModel();

            // now we set the number properly.  Yucky hack.
            model.set("number", tempModel.get("number"));
            tryToSendSysex(requestCurrentDump());
            }
        else
            {            
            tryToSendSysex(requestDump(tempModel));
            }
        }


    public byte[] requestDump(Model tempModel)
        {
        if (tempModel == null)
            tempModel = getModel();
        byte DEV = (byte)(getID());
        int num = tempModel.get("number");
        byte NN = (byte)(num % 128);
        byte BB = (byte)(num / 128);
        model.set("number", num);
        return new byte[] { (byte)0xF0, 0x3E, 0x16, DEV, 0x00, BB, NN, (byte)0xF7 };
        }
    
    public byte[] requestCurrentDump()
        {
        byte DEV = (byte)(getID());
        return new byte[] { (byte)0xF0, 0x3E, 0x16, DEV, 0x00, 0x7F, 0x00, (byte)0xF7 };
        }
    
        
    
    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        JTextField number = new SelectedTextField("" + (model.get("number") + 1), 3);
                
        while(true)
            {
            boolean result = showMultiOption(this, new String[] { "Patch Number"}, 
                new JComponent[] { number }, title, "Enter the Patch number.");
                
            if (result == false) 
                return false;
                                
            int n;
            try { n = Integer.parseInt(number.getText()); }
            catch (NumberFormatException e)
                {
                showSimpleError(title, "The Patch Number must be an integer 1 ... " + (writing ? 499 : 500));
                continue;
                }
            if (n < 1 || n > 500 || (writing && n > 499))
                {
                showSimpleError(title, "The Patch Number must be an integer 1 ... " + (writing ? 499 : 500));
                continue;
                }
                                
            change.set("number", n - 1);
                        
            return true;
            }
        }

    public static String getSynthName() { return "Waldorf Pulse 2"; }
    
    public String getPatchName(Model model) { return model.get("name", "Init"); }
    
    public byte getID() 
        { 
        try 
            { 
            byte b = (byte)(Byte.parseByte(tuple.id));
            if (b >= 0) return b;
            }
        catch (NullPointerException e) { } // expected.  Happens when tuple's not built yet
        catch (NumberFormatException e) { e.printStackTrace(); }
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
        int number = model.get("number");
        
        number++;
        if (number > 499)
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
        return "" + (number > 99 ? "" : (number > 9 ? "0" : "00")) + number;
        }

    public boolean getSendsParametersAfterWrite() { return true; }
    }
    
