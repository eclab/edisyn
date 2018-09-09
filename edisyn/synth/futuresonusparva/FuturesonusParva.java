/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.futuresonusparva;

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
   A patch editor for the Futuresonus Parva.
        
   @author Sean Luke
*/

public class FuturesonusParva extends Synth
    {
    static final String[] BANKS = new String[] { "A", "B", "C", "D", "E", "F" };
    static final String[] VOICE_CONFIGURATIONS = new String[] { "Poly", "Unison", "Mono" };
    static final String[] CURVES = new String[] { "Exponential         ", "Linear" };  // note the extra space after Exponential, so the menu is big enough to maintain consistency in its column
    static final String[] GLIDE_MODES = new String[] { "Off", "Rate", "Time" };
    static final String[] LFO_WAVES = new String[] { "Triangle", "Sawtooth", "Ramp", "Square", "Sample & Hold" };
    static final String[] VCF_TYPE = new String[] { "24dB LP", "12dB LP", "24dB HP", "12dB HP", "12dB BP" };
    static final String[] TICKS = { "Off", "8 Bars", "4 Bars", "2 bars", "1.5 Bars", "Whole Note", "Dotted Half", "Half Note", "Quarter Note", "Dotted 8th", "Quarter Triplet", "8th Note", "Dotted 16th", "8th Triplet", "16th Note", "16th Triplet" };
    static final String[] MOD_SOURCES = new String[] { "Off", "Velocity", "Aftertouch", "Mod Wheel", "LFO1", "LFO2", "LFO3", "LFO4", "ENV1", "ENV2", "ENV3", "ENV4", "Breath", "Note" };
    static final String[] MOD_DESTINATIONS = new String[] { 
        "Off", 
        "OSC1 Freq.", "OSC2 Freq.", "OSC3 Freq.", "OSC* Freq.", 
        "OSC1 Level", "OSC2 Level", "OSC3 Level", "Noise Level", 
        "OSC1 PWM", "OSC2 PWM", "OSC3 PWM", "OSC* PWM", 
        "VCF Cutoff", "VCF Resonance", "VCF OSC3 Mod", 
        "VCA Level", "Pan",
        "LFO1 Freq.", "LFO2 Freq.", "LFO3 Freq.", "LFO4 Freq.", "LFO* Freq.", 
        "LFO1 Level", "LFO2 Level", "LFO3 Level", "LFO4 Level", "LFO* Level",
        "ENV1 Level", "ENV2 Level", "ENV3 Level", "ENV4 Level", "ENV* Level", 
        "ENV1 Attack", "ENV2 Attack", "ENV3 Attack", "ENV4 Attack", "ENV* Attack",
        "ENV1 Decay", "ENV2 Decay", "ENV3 Decay", "ENV4 Decay", "ENV* Decay",
        "ENV1 Sustain", "ENV2 Sustain", "ENV3 Sustain", "ENV4 Sustain", "ENV* Sustain",
        "ENV1 Release", "ENV2 Release", "ENV3 Release", "ENV4 Release", "ENV* Release" };

    public FuturesonusParva()
        {
        for(int i = 0; i < allParameters.length; i++)
            {
            allParametersToIndex.put(allParameters[i], Integer.valueOf(i));
            }
        
        JComponent soundPanel = new SynthPanel(this);
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        hbox.addLast(addVoice(Style.COLOR_C()));
        vbox.add(hbox);
        
        hbox = new HBox();
        hbox.add(addOscillator(1, Style.COLOR_A()));
        hbox.addLast(addOscillators(Style.COLOR_A()));
        vbox.add(hbox);
        
        hbox = new HBox();
        hbox.addLast(addOscillator(2, Style.COLOR_A()));
        vbox.add(hbox);
        
        hbox = new HBox();
        hbox.addLast(addOscillator(3, Style.COLOR_A()));
        vbox.add(hbox);

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Sound", soundPanel);

        soundPanel = new SynthPanel(this);
        vbox = new VBox();

        vbox.add(addFilter(Style.COLOR_B()));
        
        hbox = new HBox();
        hbox.add(addLFO(1, Style.COLOR_A()));
        hbox.add(addLFO(2, Style.COLOR_A()));
        vbox.add(hbox);
        
        hbox = new HBox();
        hbox.add(addLFO(3, Style.COLOR_A()));
        hbox.add(addLFO(4, Style.COLOR_A()));
        vbox.add(hbox);

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Filter and LFOs", soundPanel);

        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        
        vbox.add(addEnvelope(1, Style.COLOR_B()));
        vbox.add(addEnvelope(2, Style.COLOR_B()));
        vbox.add(addEnvelope(3, Style.COLOR_B()));
        vbox.add(addEnvelope(4, Style.COLOR_B()));

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Envelopes", soundPanel);


        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        vbox.add(addModulation(Style.COLOR_A()));
                
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Modulation", soundPanel);

        model.set("name", "Init");
        model.set("number", 0);
        model.set("bank", 0);
        
        loadDefaults();
        }
                
    
    public String getDefaultResourceFileName() { return "FuturesonusParva.init"; }
    public String getHTMLResourceFileName() { return "FuturesonusParva.html"; }
                
                
              
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
        vbox.addBottom(comp);  // doesn't work right :-(
        hbox.add(vbox);
        
        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }
        
    public String revisePatchName(String name)
        {
        name = super.revisePatchName(name);
        if (name == null) name = "";
        char[] chars = name.toCharArray();
        for(int i = 0; i < chars.length; i++)
            {
            if (chars[i] < 32 || chars[i] > 126)        // DEL is not permitted
                chars[i] = ' ';
            }
        return new String(chars);
        }

              
              
    public JComponent addOscillators(Color color)
        {
        Category category  = new Category(this, "All Oscillators", color);
                        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();

        comp = new LabelledDial("Global", this, "range", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Range");
        hbox.add(comp);        

        comp = new LabelledDial("Global", this, "glide", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Glide");
        hbox.add(comp);        

        comp = new LabelledDial("Slop", this, "slop", color, 0, 15);
        hbox.add(comp);        

        comp = new LabelledDial("Noise", this, "noise", color, 0, 127);
        hbox.add(comp);        

        category.add(hbox);
        return category;
        }        

    public JComponent addVoice(Color color)
        {
        Category category  = new Category(this, "Voice", color);
                        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();

        params = VOICE_CONFIGURATIONS;
        comp = new Chooser("Voice Configuration", this, "voicecfg", params);
        vbox.add(comp);

        params = GLIDE_MODES;
        comp = new Chooser("Glide Mode", this, "glidemode", params);
        vbox.add(comp);
        
        hbox.add(vbox);

        comp = new CheckBox("Clean Oscillators", this, "cleanosc");
        vbox.add(comp);

        comp = new LabelledDial("Pan", this, "pan", color, 0, 127, 64);
        hbox.add(comp);        

        comp = new LabelledDial("Spread", this, "spread", color, 0, 127);
        hbox.add(comp);        

        comp = new LabelledDial("Detune", this, "detune", color, 0, 24);
        hbox.add(comp);        

        comp = new LabelledDial("Bend Up", this, "bendup", color, 0, 96);
        hbox.add(comp);        

        comp = new LabelledDial("Bend Down", this, "benddown", color, 0, 96);
        hbox.add(comp);        

        category.add(hbox);
        return category;
        }        
        

    public JComponent addOscillator(int osc, Color color)
        {
        Category category  = new Category(this, "Oscillator " + osc, color);
        category.makePasteable("osc" + osc);
                        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        comp = new CheckBox("Sawtooth Pulse Wave", this, "osc" + osc + "pulse");
        ((CheckBox) comp).addToWidth(2);
        vbox.add(comp);

        comp = new CheckBox("Key Sync", this, "osc" + osc + "key");
        ((CheckBox) comp).addToWidth(2);
        vbox.add(comp);

        if (osc > 1)
            {
            comp = new CheckBox("Sync to OSC1", this, "osc" + osc + "sync");
            ((CheckBox) comp).addToWidth(2);
            vbox.add(comp);
            }
        hbox.add(vbox);


        comp = new LabelledDial("Range", this, "osc" + osc + "range", color, 0, 120, 60)
            {
            public boolean isSymmetric() { return true; }
            };
        hbox.add(comp);        

        comp = new LabelledDial("Fine", this, "osc" + osc + "fine", color, 0, 100, 50)
            {
            public boolean isSymmetric() { return true; }
            };
        hbox.add(comp);        

        comp = new LabelledDial("Wave", this, "osc" + osc + "wave", color, 1, 53)
            {
            public String map(int val)
                {
                if (val == 1) return "Saw";
                else if (val == 2) return "Tri";
                else return "PW " + (val - 3);
                }
            };
        hbox.add(comp);        
        
        comp = new LabelledDial("Level", this, "osc" + osc + "level", color, 0, 127);
        hbox.add(comp);        

        comp = new LabelledDial("Glide", this, "osc" + osc + "glide", color, 0, 127);
        hbox.add(comp);        

        if (osc == 1)  // add some extra space to make room for "All Oscillators
            hbox.add(Strut.makeStrut(comp));  // one labelled dial's worth of space
                        
        category.add(hbox);
        return category;
        }

    public JComponent addEnvelope(int env, Color color)
        {
        Category category  = new Category(this, "Envelope " + env + 
                (env == 1 ? " (Amplitude)" : 
                (env == 2 ? " (Filter Cutoff)" : "")), color);
        category.makePasteable("env" + env);
                        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();

        params = CURVES;
        comp = new Chooser("Curve", this, "env" + env + "curve", params);
        vbox.add(comp);

        if (env > 2)
            {
            params = MOD_DESTINATIONS;
            comp = new Chooser("Mod Destination", this, "env" + env + "moddst", params);
            vbox.add(comp);
            }
        hbox.add(vbox);
                
        vbox = new VBox();
        comp = new CheckBox("Loop", this, "env" + env + "loop");
        ((CheckBox) comp).addToWidth(2);
        vbox.add(comp);
        hbox.add(vbox);


        comp = new LabelledDial("Attack", this, "env" + env + "attack", color, 0, 127);
        hbox.add(comp);        

        comp = new LabelledDial("Decay", this, "env" + env + "decay", color, 0, 127);
        hbox.add(comp);        

        comp = new LabelledDial("Sustain", this, "env" + env + "sustain", color, 0, 127);
        hbox.add(comp);        

        comp = new LabelledDial("Release", this, "env" + env + "release", color, 0, 127);
        hbox.add(comp);        

        comp = new LabelledDial("Amount", this, "env" + env + "amount", color, 0, 127, 64)
            {
            public boolean isSymmetric() { return true; }
            };
        hbox.add(comp);        

        comp = new LabelledDial("Velocity", this, "env" + env + "velocity", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Sensitivity");
        hbox.add(comp);        

        // ADSR
        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "env" + env + "attack", "env" + env + "decay", null, "env" + env + "release" },
            new String[] { null, null, "env" + env + "sustain", "env" + env + "sustain", null },
            new double[] { 0, 0.3333, 0.3333, 0.3333, 0.3333 },
            new double[] { 0, 1.0, 1.0 / 127, 1.0 / 127, 0 },
            new double[] { 0, (Math.PI/4.0/127), (Math.PI/4.0/127), 0, (Math.PI/4.0/127) });
        hbox.addLast(comp);        
                
        category.add(hbox);
        return category;
        }
        

    public JComponent addLFO(int lfo, Color color)
        {
        Category category  = new Category(this, "LFO " + lfo, color);
        category.makePasteable("lfo" + lfo);
                        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        params = LFO_WAVES;
        comp = new Chooser("Wave", this, "lfo" + lfo + "wave", params);
        vbox.add(comp);

        comp = new CheckBox("Unipolar", this, "lfo" + lfo + "polar");
        vbox.add(comp);

        comp = new CheckBox("Key Sync", this, "lfo" + lfo + "keysync");
        vbox.add(comp);

        hbox.add(vbox);

        vbox = new VBox();
        params = MOD_DESTINATIONS;
        comp = new Chooser("Mod Destination", this, "lfo" + lfo + "moddst", params);
        vbox.add(comp);

        params = TICKS;
        comp = new Chooser("MIDI Clock Sync", this, "lfo" + lfo + "midiclk", params);
        vbox.add(comp);

        hbox.add(vbox);

        comp = new LabelledDial("Rate", this, "lfo" + lfo + "rate", color, 0, 127)
            {
            public String map(int val)
                {
                if (val == 0) return "Off";
                else return "" + val;
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Depth", this, "lfo" + lfo + "depth", color, 0, 127);
        hbox.add(comp);

        if (lfo == 1 || lfo == 3)
            hbox.add(Strut.makeHorizontalStrut(20));

        category.add(hbox);
        return category;
        }

    public JComponent addFilter(Color color)
        {
        Category category  = new Category(this, "Filter", color);
                        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        params = VCF_TYPE;
        comp = new Chooser("Type", this, "vcftype", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Cutoff", this, "vcfcutoff", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Cutoff", this, "vcfcutofffine", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Fine Tune");
        hbox.add(comp);

        comp = new LabelledDial("Resonance", this, "vcfresonance", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Key Amount", this, "vcfkeyamt", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("OSC3", this, "vcfaudamt", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Modulation");
        hbox.add(comp);

        category.add(hbox);
        return category;
        }
        
    public JComponent addModulation(Color color)
        {
        Category category  = new Category(this, "Modulation", color);
        category.makeDistributable("mod");
                        
        JComponent comp;
        String[] params;
        VBox main = new VBox();
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        for(int row = 1; row < 17; row+= 4)
            {
            hbox = new HBox();
            for(int i = row; i < row + 4; i++)
                {
                vbox = new VBox();
                params = MOD_SOURCES;
                comp = new Chooser("Source " + i, this, "mod" + i + "source", params);
                vbox.add(comp);

                params = MOD_DESTINATIONS;
                comp = new Chooser("Destination " + i, this, "mod" + i + "destination", params);
                vbox.add(comp);
                hbox.add(vbox);

                comp = new LabelledDial("Amount " + i, this, "mod" + i + "amount", color, 0, 127, 64)
                    {
                    public boolean isSymmetric() { return true; }
                    };
                hbox.add(comp);
                }
                        
            main.add(hbox);
            if (row < 13)
                main.add(Strut.makeVerticalStrut(30));
            }
                                
        category.add(main, BorderLayout.WEST);
        return category;
        }


    /** Map of parameter -> index in the allParameters array. */
    HashMap allParametersToIndex = new HashMap();


    final static String[] allParameters = new String[/*217 or so*/] 
    {
    "-",                // bank
    "-",                // mod wheel
    "-",                // breath
    "-",                // bpm
    "-",                // foot
    "-",
    "-",
    "-",                // volume
    "-",
    "-",
    "spread",
    "-",                    // expression
    "slop",
    "pan",
    "noise",
    "osc1range",
    "osc1fine",
    "osc1wave",
    "osc1level",
    "osc1glide",
    "osc2range",
    "osc2fine",
    "osc2wave",
    "osc2level",
    "osc2glide",
    "osc2sync",
    "osc3range",
    "osc3fine",
    "osc3wave",
    "osc3level",
    "osc3glide",
    "osc3sync",
    "-",
    "-",                            // mod wheel lsb
    "glide",                        // (global osc glide)
    "-",
    "-",
    "-",
    "-",
    "lfo1rate",                   
    "lfo1wave",
    "lfo1depth",
    "lfo1moddst",
    "lfo1keysync",           
    "lfo2rate",                   
    "lfo2wave",
    "lfo2depth",
    "lfo2moddst",
    "lfo2keysync",         
    "lfo3rate",                   
    "lfo3wave",
    "lfo3depth",
    "lfo3moddst",
    "lfo3keysync",        
    "lfo4rate",                   
    "lfo4wave",
    "lfo4depth",
    "lfo4moddst",
    "lfo4keysync",       
    "-",                                // VCA Init      
    "range",                    // (global osc range)     
    "-",       
    "-",       
    "-",       
    "-",                        // sustain
    "-",                        // portamento
    "-",                        // sostenuto
    "-",                        // soft
    "-",                        // legato
    "vcfcutofffine",
    "vcfcutoff",
    "vcfresonance",
    "vcfkeyamt",
    "vcfaudamt",
    "-",                            // brightness
    "vcftype",
    "env1amount",
    "env1velocity",
    "env1attack",
    "env1decay",
    "env1sustain",
    "env1release",
    "env2amount",
    "env2velocity",
    "env2attack",
    "env2decay",
    "env2sustain",
    "env2release",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "env3moddst",
    "env3amount",
    "env3velocity",
    "env3attack",
    "env3decay",
    "env3sustain",
    "env3release",
    "env4moddst",
    "env4amount",
    "env4velocity",
    "env4attack",
    "env4decay",
    "env4sustain",
    "env4release",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
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
    "mod10amount",
    "mod11source",
    "mod11destination",
    "mod11amount",
    "mod12source",
    "mod12destination",
    "mod12amount",
    "mod13source",
    "mod13destination",
    "mod13amount",
    "mod14source",
    "mod14destination",
    "mod14amount",
    "mod15source",
    "mod15destination",
    "mod15amount",
    "mod16source",
    "mod16destination",
    "mod16amount",
    "voicecfg",
    "bendup",
    "benddown",
    "detune",
    "glidemode",
    "-",                                        // no sustain
    "cleanosc",
    "lfo1polar",
    "lfo2polar",
    "lfo3polar",
    "lfo4polar",
    "lfo1midiclk",
    "lfo2midiclk",
    "lfo3midiclk",
    "lfo4midiclk",
    "env1loop",
    "env2loop",
    "env3loop",
    "env4loop",
    "osc1pulse",
    "osc2pulse",
    "osc3pulse",
    "osc1key",
    "osc2key",
    "osc3key",
    "env1curve",
    "env2curve",
    "env3curve",
    "env4curve",
    "-",
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

    public boolean getSendsAllParametersInBulk() { return false; }

    // this is MSB first
    public byte[] nybblize(byte[] val)
        {
        byte[] data = new byte[val.length * 2];
        for(int i = 0; i < val.length; i++)
            {
            data[i * 2] = (byte)(((val[i] & 127) >>> 4) & 127);
            data[i * 2 + 1] = (byte)(val[i] & 15);
            }
        return data;
        }
                
    public byte[] unnybblize(byte[] val)
        {
        byte[] data = new byte[val.length / 2];
        for(int i = 0; i < data.length; i++)
            {
            data[i] = (byte)((val[i * 2] << 4) | val[i * 2 + 1]);
            }
        return data;
        }


    public byte[] emit(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        if (tempModel == null)
            tempModel = getModel();
                
        if (toWorkingMemory)
            {
            System.err.println("Warning (FuturesonusParva): request to send to working memory, that shouldn't happen for the time being");
            return new byte[0];
            }
                  
        // LOAD NAME
        String n = model.get("name", "Untitled        ") + "                ";
        char[] b = n.toCharArray();
        byte[] name = new byte[16];
        for(int i = 0; i < 16; i++)
            {
            name[i] = (byte)(b[i] & 127);
            }
        name = nybblize(name);
                  
        // LOAD DATA
        byte[] data = new byte[allParameters.length];
        for(int i = 0; i < allParameters.length; i++)
            {
            if (allParameters[i].equals("-"))
                data[i] = 0;
            else
                data[i] = (byte)model.get(allParameters[i], 0);
            }
        data = nybblize(data);

        byte[] d = new byte[data.length + name.length + 7];
        d[0] = (byte) 0xF0;
        d[1] = (byte) 0x7D;
        d[2] = (byte) 0x00;
        d[3] = (byte) 0x7F;
        d[4] = (byte) (0x40 | model.get("bank"));
        d[5] = (byte) model.get("number");
        System.arraycopy(name, 0, d, 6, name.length);
        System.arraycopy(data, 0, d, 6 + name.length, data.length);
        d[d.length - 1] = (byte) 0xF7;

        return d;
        }
        

    public int parse(byte[] data, boolean fromFile)
        {
        model.set("bank", (data[4] & 15));
        model.set("number", data[5]);
                
        byte[] n = new byte[32];
        byte[] d = new byte[data.length - n.length - 7];  // minus name length (nibblized 16-byte), minus 7
        System.arraycopy(data, 6 + n.length, d, 0, d.length);
        System.arraycopy(data, 6, n, 0, n.length);
                
        // LOAD DATA
        d = unnybblize(d);
        for(int i = 0; i < d.length; i++)       // we use d.length because we might have something shorter than allParameters.length
            {
            if (!allParameters[i].equals("-"))
                {
                model.set(allParameters[i], d[i]);
                }
            }
                        
        // LOAD NAME
        try
            {
            model.set("name", new String(unnybblize(n), "US-ASCII"));
            }
        catch (UnsupportedEncodingException ex) { }

        revise();
        return PARSE_SUCCEEDED;
        }


    public Object[] emitAll(String key)
        {
        if (key.equals("bank")) return new Object[0];  // this is not emittable
        if (key.equals("number")) return new Object[0];  // this is not emittable
        if (key.equals("name")) return new Object[0];  // this is not emittable

        int index = ((Integer)(allParametersToIndex.get(key))).intValue();
        int value = model.get(key);
        
        if (index < 128)  // it's a CC
            return buildCC(getChannelOut(), index, value);
        else    // it's NRPN.  We're sending MSB first, which is what the Parva expects
            return buildNRPN(getChannelOut(), index - 128, value);
        }

    Model mergeModel;

    public void handleSynthCCOrNRPN(Midi.CCData data)
        {
        final int LAST_MERGE_NUMBER = allParameters.length - 1;
        
        if (data.type == Midi.CCDATA_TYPE_RAW_CC)
            {
            if (isMerging())
                {
                // build a model if we haven't yet
                if (mergeModel == null)
                    mergeModel = new Model();
                        
                // Load the key, they come in one at a time
                setSendMIDI(false);
                if (!(allParameters[data.number].equals("-")))
                    {
                    mergeModel.set(allParameters[data.number], data.value);
                    }
                setSendMIDI(true);
                }
            else
                {
                if (!(allParameters[data.number].equals("-")))
                    {
                    setSendMIDI(false);
                    model.set(allParameters[data.number], data.value);
                    setSendMIDI(true);
                    }
                }
            }
        else if (data.type == Midi.CCDATA_TYPE_NRPN)
            {
            if (isMerging())
                {
                // build a model if we haven't yet
                if (mergeModel == null)
                    mergeModel = new Model();
                        
                // Load the key, they come in one at a time
                setSendMIDI(false);
                if (!(allParameters[data.number + 128].equals("-")))
                    {
                    mergeModel.set(allParameters[data.number + 128], data.value);
                    }
                setSendMIDI(true);

                // if it's the last key, do the merge
                if (data.number + 128 == LAST_MERGE_NUMBER)
                    {
                    setSendMIDI(false);
                    Model backup = (Model)(model.clone());
                    model.recombine(random, mergeModel, getMutationKeys(), getMergeProbability());
                    if (!backup.keyEquals(getModel()))
                        undo.push(backup);
                    setSendMIDI(true);
                    sendAllParameters();
                    setMergeProbability(0.0);
                    mergeModel = null;
                    }
                }
            else
                {
                if (!(allParameters[data.number + 128].equals("-")))
                    {
                    setSendMIDI(false);
                    model.set(allParameters[data.number + 128], data.value);
                    setSendMIDI(true);
                    }
                }
            }
        else // RPN
            {
            // do nothing
            }
        }


    public void changePatch(Model tempModel)
        {
        byte BB = (byte)tempModel.get("bank");
        byte NN = (byte)tempModel.get("number");
        try {
            // Bank change is CC 0
            tryToSendMIDI(buildCC(getChannelOut(), 0, BB));
            simplePause(getPauseAfterBankChange());
            // Number change is PC
            tryToSendMIDI(buildPC(getChannelOut(), NN));
            }
        catch (Exception e) { e.printStackTrace(); }
        }

    public byte[] requestDump(Model tempModel)
        {
        return new byte[] { (byte)0xF0, (byte)0x7D, (byte)0x00, (byte)0x7F, (byte)(0x50 | model.get("bank")), (byte)(model.get("number")), (byte)0xF7 };
        }
    
    public byte[] requestCurrentDump()
        {
        return new byte[] { (byte)0xF0, (byte)0x7D, (byte)0x00, (byte)0x7F, (byte)0x5F, (byte)0x00, (byte)0xF7 };
        }
    
    public static boolean recognize(byte[] data)
        {
        // At present this is all I really have to go on.
        boolean v = (
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x7D &&
            data[2] == (byte)0x00 &&
            data[3] == (byte)0x7F &&
            (data[4] & 0xF0) == 0x40 &&
            data[5] < 64 &&
            data[data.length - 1] == (byte)0xF7);
        return v;
        }
    
    
    
    
    /////// OTHER ABSTRACT METHODS
    
    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        JComboBox bank = new JComboBox(BANKS);
        bank.setEditable(false);
        bank.setMaximumRowCount(32);
        bank.setSelectedIndex(model.get("bank"));
                
        JTextField number = new JTextField("" + (model.get("number")), 3);
                
        while(true)
            {
            boolean result = showMultiOption(this, new String[] { "Bank", "Patch Number"}, 
                new JComponent[] { bank, number }, title, "Enter the Bank and Patch number.");
                
            if (result == false) 
                return false;
                                
            int n;
            try { n = Integer.parseInt(number.getText()); }
            catch (NumberFormatException e)
                {
                showSimpleError(title, "The Patch Number must be an integer 0 ... 63");
                continue;
                }
            if (n < 0 || n > 63)
                {
                showSimpleError(title, "The Patch Number must be an integer 0 ... 63");
                continue;
                }
                                
            change.set("bank", bank.getSelectedIndex());
            change.set("number", n);
                        
            return true;
            }
        }
        

    public void revise()
        {
        // check the easy stuff -- out of range parameters
        super.revise();

        String nm = model.get("name", "Init");
        String newnm = revisePatchName(nm);
        if (!nm.equals(newnm))
            model.set("name", newnm);
        }
        
    public static String getSynthName() { return "Futuresonus Parva"; }
    
    public String getPatchName(Model model) { return model.get("name", "Init"); }

    public Model getNextPatchLocation(Model model)
        {
        int bank = model.get("bank");
        int number = model.get("number");
        
        number++;
        if (number >= 64)
            {
            bank++;
            number = 0;
            if (bank >= 6)
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
        return BANKS[model.get("bank")] + (number > 9 ? "" : "0") + number;
        }





    // The patch editor does a bank change first, then a PC.
    // Override this to pause after the bank change
    public int getPauseAfterBankChange()
        {
        return 0;  // in ms
        }

    // override this to pause after the PC
    public int getPauseAfterChangePatch()
        {
        return 0; // in ms
        }

    // Override this to pause after every NRPN or CC message
    // So as to slow down bulk transmission.  We want to slow it down
    // to JUST BARELY slow enough that the Parva can handle it.
    // Note it's a double, we can pause for less than 1ms
    public double getPauseBetweenMIDISends() 
        {
        return 0.0;  // in ms
        }

    }
    
