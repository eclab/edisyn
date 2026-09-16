/***
    Copyright 2026 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.novationbassstation2;

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
import java.util.List;

/**
   A patch editor for the Novation Bass Station II. 
   
   See end of file for reverse-engineered sysex protocol.  You'd think that the Bass Station II, being
   a pretty simple synthesizer, would have a well-designed, consistent, and rational sysex protocol.
   But no.
        
   @author Sean Luke
*/

public class NovationBassStation2 extends Synth
    {
    public static final String[] OSC_WAVES = { "Sine", "Triangle", "Sawtooth", "Square" };
    public static final String[] OSC_OCTAVES = { "16'", "8'", "4'", "2'" };
    public static final String[] SUB_OSC_WAVES = { "Sine", "Pulse", "Square" };
    public static final String[] SUB_OSC_OCTAVES = { "1", "2" };
    public static final String[] FILTER_SLOPES = { "12dB", "24dB" };
    public static final String[] FILTER_TYPES = { "Classic", "Acid" };
    public static final String[] FILTER_SHAPES = { "Low Pass", "Band Pass", "High Pass" };
    public static final String[] TRIGGERS = { "Single", "Multi", "Autoglide" };
    public static final String[] LFO_WAVES = { "Triangle", "Sawtooth", "Square", "Sample & Hold" };
    public static final String[] LFO_SYNC_SWITCH = { "Speed", "Sync" };
    public static final String[] ARP_MODES = { "Up", "Down", "Up-Down", "Up-Down2", "Played", "Random", "Record", "Play" };
    public static final String[] LFO_SYNC_VALUES =
        {
        "64 Beats",
        "48 Beats",
        "42 Beats",
        "36 Beats",
        "32 Beats", 
        "30 Beats",
        "28 Beats",
        "24 Beats",
        "21 1/3 Beats (3 Cycles Per 16 Bars)",
        "20 Beats",
        "18 2/3 Beats (3 Cycles Per 14 Bars)",
        "18 Beats",
        "16 Beats",
        "13 1/3 Beats (3 Cycles Per 10 Bars)",
        "12 Beats",
        "10 2/3 Beats (3 Cycles Per 8 Bars)",
        "8 Beats",
        "6 Beats",
        "5 1/3 Beats (3 Cycles Per 4 Bars)",
        "4 Beats",
        "3 Beats",
        "2 2/3 Beats (3 Cycles Per 2 Bars)",
        "Half Note",
        "Dotted Quarter",
        "Quarter + Triplet",
        "Quarter Note",
        "Dotted Eighth",
        "Triplet Quarter",
        "Eighth Note",
        "Dotted 16th",
        "Triplet 8th",
        "16th Note",
        "Triplet 16th",
        "32nd Note",
        "Triplet 32nd"
        };
                
    public static final int[] OSC_PULSE_MODULATION_VALUES = 
        {
        -90, -88, -86, -85, -84, -82,  
        -80, -78, -76, -75, -74, -73, -71,  
        -70, -68, -66, -65, -64, -63, -61,  
        -60, -59, -57, -56, -55, -53, -51,  
        -50, -49, -47, -46, -45, -44, -42,  
        -40, -39, -38, -36, -35, -34, -32,  
        -30, -28, -26, -25, -24, -23, -22,  
        -20, -19, -17, -16, -15, -14, -12,  
        -10,  -9,  -7,  -5,  -4,  -3,  -2, -1,
        0,   1,   2,   3,   4,   5,   7,  9,  
        10,  12,  14,  15,  16,  17,  19,  
        20,  22,  23,  24,  25,  26,  28,  
        30,  32,  34,  35,  36,  38,  39,  
        40,  42,  44,  45,  46,  47,  49,  
        50,  51,  53,  55,  56,  57,  59,
        60,  61,  63,  64,  65,  66,  68,
        70,  71,  73,  74,  75,  76,  78,
        80,  82,  84,  85,  86,  88,
        90,
        };
                
    public static final int[] OSC_PULSEWIDTH_ENCODINGS = 
        {
        0,   1,   3,   5,   7,                              // 5
        8,   9,  10,  12,  13,  15,  16,  17,  18,  20,     // 10
        22,  23,  24,  25,  27,  29,  30,  31,  32,  34,    // 20
        35,  37,  38,  39,  41,  42,  44,  45,  46,  48,    // 30
        50,  52,  53,  54,  55,  57,  59,  60,  61,  63,    // 40
        64,  66,  67,  68,  69,  71,  72,  73,  75,  76,    // 50
        77,  79,  81,  82,  84,  85,  86,  88,  89,  91,    // 60
        92,  93,  95,  96,  97,  99, 101, 102, 103, 104,    // 70
        106, 107, 108, 110, 111, 113, 114, 115, 117, 119,   // 80
        120, 121, 122, 124, 126, 127                        // 90
        };
                
    public int getOscPWParam(int value)
        {
        // search for it, ugh O(n)
        for(int i = 0; i < OSC_PULSEWIDTH_ENCODINGS.length; i++)
            {
            if (OSC_PULSEWIDTH_ENCODINGS[i] == value) 
                {
                return (i + 5);
                }
            else if (OSC_PULSEWIDTH_ENCODINGS[i] > value)  // dunno what we should do
                {
                return (i + 5) - 1;             // maybe?
                }
            }
        return 5;               // never happens
        }
                
    public static final int[] OCTAVE_ENCODINGS = { 0x78, 0x7A, 0x7C, 0x7E, 0x80, 0x82, 0x84, 0x86, 0x88, 0x8A };
        
    public static final int[] RANGE_ENCODINGS = { 0x3F, 0x40, 0x41, 0x42 };

    public static final String[] COARSE_VALUES = 
        {
        "-12.0",                
        "-11.9",                
        "-11.8",                
        "-11.7",                
        "-11.6",                
        "-11.5",                
        "-11.4",                
        "-11.3",                
        "-11.2",                
        "-11.1",                
        "-11.0",        // no dup
        "-10.9",                
        "-10.8",                
        "-10.7",                
        "-10.6",                
        "-10.5",                
        "-10.4",                
        "-10.3",                
        "-10.2",        // notice -10.1 is missing
        "-10.0",                
        "-10.0",        // dup
        "-9.9",         
        "-9.8",         
        "-9.7",         
        "-9.6",         
        "-9.5",         
        "-9.4",         
        "-9.3",         
        "-9.2",         
        "-9.1",         
        "-9.0",         
        "-9.0",         // dup
        "-8.9",         
        "-8.8",         
        "-8.7",         
        "-8.6",         
        "-8.5",         
        "-8.4",         
        "-8.3",         
        "-8.2",         
        "-8.1",         
        "-8.0",         // dup
        "-8.0",         
        "-7.9",         
        "-7.8",         
        "-7.7",         
        "-7.6",         
        "-7.5",         
        "-7.4",         
        "-7.3",         
        "-7.2",         
        "-7.1",         
        "-7.0",         
        "-7.0",         // dup
        "-6.8",         // notice -6.9 is missing
        "-6.7",         
        "-6.6",         
        "-6.5",         
        "-6.4",         
        "-6.3",         
        "-6.2",         
        "-6.1",         
        "-6.0",         
        "-6.0",         // dup
        "-5.9",         
        "-5.8",         
        "-5.7",         
        "-5.6",         
        "-5.5",         
        "-5.4",         
        "-5.3",         
        "-5.2",         
        "-5.1",         
        "-5.0",         
        "-5.0",         // dup
        "-4.9",         
        "-4.8",         
        "-4.7",         
        "-4.6",         
        "-4.5",         
        "-4.4",         
        "-4.3",         
        "-4.2",         
        "-4.1",         
        "-4.0",         // dup
        "-4.0",         
        "-3.9",         
        "-3.8",         
        "-3.7",         
        "-3.5",         // notice that -3.6 is missing
        "-3.4",         
        "-3.3",         
        "-3.2",         
        "-3.1",         
        "-3.0",         // dup
        "-3.0",         
        "-2.9",         
        "-2.8",         
        "-2.7",         
        "-2.6",         
        "-2.5",         
        "-2.4",         
        "-2.3",         
        "-2.2",         
        "-2.1",         
        "-2.0",         
        "-2.0",         // dup
        "-1.9",         
        "-1.8",         
        "-1.7",         
        "-1.6",         
        "-1.5",         
        "-1.4",         
        "-1.3",         
        "-1.2",         
        "-1.1",         
        "-1.0",         
        "-1.0",         // dup
        "-0.9",         
        "-0.8",         
        "-0.7",         
        "-0.6",         
        "-0.5",         
        "-0.4",         
        "-0.3",         
        "-0.2",         
        "-0.1",         
        "0",            
        "0",            // dup
        "0.1",          
        "0.2",          
        "0.3",          
        "0.4",          
        "0.5",          
        "0.6",          
        "0.7",          
        "0.8",          
        "0.9",          
        "1.0",          // dup
        "1.0",          
        "1.1",          
        "1.2",          
        "1.3",          
        "1.4",          
        "1.5",          
        "1.6",          
        "1.7",          
        "1.8",          
        "1.9",          
        "2.0",          // dup
        "2.0",          
        "2.1",          
        "2.2",          
        "2.3",          
        "2.4",          
        "2.5",          
        "2.6",          
        "2.7",          
        "2.8",          
        "2.9",          
        "3.0",          // dup
        "3.0",          
        "3.1",          
        "3.2",          
        "3.3",          
        "3.4",          
        "3.5",          
        "3.6",          
        "3.7",          
        "3.8",          
        "3.9",          
        "4.0",          
        "4.1",          // no dup!
        "4.2",          
        "4.3",          
        "4.4",          
        "4.5",          
        "4.6",          
        "4.7",          
        "4.8",          
        "4.9",          
        "5.0",          // dip
        "5.0",          
        "5.1",          
        "5.2",          
        "5.3",          
        "5.4",          
        "5.5",          
        "5.6",          
        "5.7",          
        "5.8",          
        "5.9",          
        "6.0",          // dup
        "6.0",          
        "6.1",          
        "6.2",          
        "6.3",          
        "6.4",          
        "6.5",          
        "6.6",          
        "6.7",          
        "6.8",          
        "7.0",          // notice that 6.9 is missing
        "7.0",          
        "7.1",          
        "7.2",          
        "7.3",          
        "7.4",          
        "7.5",          
        "7.6",          
        "7.7",          
        "7.8",          
        "7.9",          
        "8.0",          // dup
        "8.0",          
        "8.1",          
        "8.2",          
        "8.3",          
        "8.4",          
        "8.5",          
        "8.6",          
        "8.7",          
        "8.8",          
        "8.9",          
        "9.0",          // dup
        "9.0",          
        "9.1",          
        "9.2",          
        "9.3",          
        "9.4",          
        "9.5",          
        "9.6",          
        "9.7",          
        "9.8",          
        "9.9",  
        "10.0",         // dup
        "10.0",         
        "10.1",         
        "10.2",         
        "10.4",         // notice that 10.3 is missing
        "10.5",         
        "10.6",         
        "10.7",         
        "10.8",         
        "10.9",         
        "11.0",         // no dup!
        "11.1",         
        "11.2",         
        "11.3",         
        "11.4",         
        "11.5",         
        "11.6",         
        "11.7",         
        "11.8",         
        "11.9",         
        "12.0",         
        };

    public JFrame sprout()
        {
        JFrame frame = super.sprout();
        return frame;
        }         

    public NovationBassStation2()
        {
        /// SOUND PANEL
                
        JComponent soundPanel = new SynthPanel(this);
        VBox vbox = new VBox();
        
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        hbox.addLast(addOscs(Style.COLOR_A()));
        vbox.add(hbox);
        
        hbox = new HBox(); 
        hbox.add(addOsc(1, Style.COLOR_A()));
        hbox.addLast(addArp(Style.COLOR_B()));
        vbox.add(hbox);

        hbox = new HBox(); // HBox.LEFT_CONSUMES);
        hbox.addLast(addMixer(Style.COLOR_B()));
        hbox.add(addOsc(2, Style.COLOR_A()));
        vbox.add(hbox);

        hbox = new HBox();
        hbox.add(addFilter(Style.COLOR_B()));
        hbox.addLast(addEffects(Style.COLOR_C()));
        vbox.add(hbox);
        

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Audio", soundPanel);
                

        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        vbox.add(addAmpEnvelope(Style.COLOR_B()));
        vbox.add(addModEnvelope(Style.COLOR_B()));

        hbox = new HBox();        
        hbox.add(addLFO(1, Style.COLOR_A()));
        hbox.addLast(addModWheel(Style.COLOR_C()));
        
        vbox.add(hbox);
        hbox = new HBox();
        hbox.add(addLFO(2, Style.COLOR_A()));
        hbox.addLast(addAftertouch(Style.COLOR_C()));

        vbox.add(hbox);

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Modulation", soundPanel);
        
        model.set("number", 0);
        
        loadDefaults();        
        }
                
    public String getDefaultResourceFileName() { return "NovationBassStation2.init"; }
    public String getHTMLResourceFileName() { return "NovationBassStation2.html"; }

                                    
    public static String getSynthName() { return "Novation Bass Station II"; }

    public String revisePatchName(String name)
        {
        name = super.revisePatchName(name);
        if (name == null) name = "";
        name = name + "                ";             // 16 spaces
        char[] chars = name.toCharArray();
        for(int i = 0; i < chars.length; i++)
            {
            if (chars[i] < 32 || chars[i] > 127)
                chars[i] = ' ';
            }
        return new String(chars).substring(0, 16);
        }
                                        
    /** Add the global patch category (name, id, number, etc.) */
    public JComponent addNameGlobal(Color color)
        {
        Category globalCategory = new Category(this, getSynthName(), color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        comp = new PatchDisplay(this, 3);
        vbox.add(comp);
        hbox.add(vbox);
        
        vbox = new VBox();

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
        model.set("name", "Untitled");
        vbox.add(comp);
 
        comp = new CheckBox("Paraphonic", this, "paraphonic");
        vbox.add(comp);

       
        hbox.add(vbox);

        comp = new LabelledDial("Pitch Bend ", this, "pitchbendrange", color, 40, 88, 64)
            {
            public boolean isSymmetric() { return true; }
            };
        ((LabelledDial)comp).addAdditionalLabel("Range");
        hbox.add(comp);

        comp = new LabelledDial("Tuning Table", this, "tuningtable", color, 0, 8);
        hbox.add(comp);

        comp = new LabelledDial("Octave", this, "octave", color, -4, 5)
            {
            public boolean isSymmetric() { return true; }
            };
        hbox.add(comp);

        comp = new LabelledDial("Portamento", this, "portamentotime", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Glide Time");
        hbox.add(comp);

        comp = new LabelledDial("Glide", this, "portamentodivergence", color, 0, 15);
        ((LabelledDial)comp).addAdditionalLabel("Divergence");
        hbox.add(comp);

        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;        
        }

    public JComponent addOsc(int osc, Color color)
        {
        Category category = new Category(this, "Oscillator " + osc, color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = OSC_WAVES;
        comp = new Chooser("Wave", this, "osc" + osc + "wave", params);
        vbox.add(comp);

        params = OSC_OCTAVES;
        comp = new Chooser("Range", this, "osc" + osc + "range", params);
        vbox.add(comp);

        hbox.add(vbox);

        comp = new LabelledDial("Coarse", this, "osc" + osc + "coarse", color, 0, 255)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val) { return COARSE_VALUES[val]; }
            };
        hbox.add(comp);

        comp = new LabelledDial("Fine", this, "osc" + osc + "fine", color, 27, 228, 127)
            {
            public boolean isSymmetric() { return true; }
            public int getDefaultValue() { return 126; }
            };
        hbox.add(comp);

        comp = new LabelledDial("Manual PW", this, "osc" + osc + "manualpw", color, 5, 95);
        hbox.add(comp);

        comp = new LabelledDial("Mod Wheel", this, "osc" + osc + "modenvdepth", color, 0, 126, 63)
            {
            public boolean isSymmetric() { return true; }
            };
        ((LabelledDial)comp).addAdditionalLabel("Env Depth");
        hbox.add(comp);

        comp = new LabelledDial("Mod Wheel", this, "osc" + osc + "modenvpwmod", color, 0, 126)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val) { return "" + OSC_PULSE_MODULATION_VALUES[val]; }
            };
        ((LabelledDial)comp).addAdditionalLabel("Env PW Mod");
        hbox.add(comp);

        comp = new LabelledDial("LFO 1", this, "osc" + osc + "lfo1depth", color, 0, 254, 127)
            {
            public boolean isSymmetric() { return true; }
            };
        ((LabelledDial)comp).addAdditionalLabel("Depth");
        hbox.add(comp);

        comp = new LabelledDial("LFO 2", this, "osc" + osc + "lfo2pwmod", color, 0, 126)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val) { return "" + OSC_PULSE_MODULATION_VALUES[val]; }
            };
        ((LabelledDial)comp).addAdditionalLabel("PW Mod");
        hbox.add(comp);
        
        category.add(hbox, BorderLayout.WEST);
        return category;
        }
    
    public JComponent addOscs(Color color)
        {
        Category category = new Category(this, "Oscillators", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = SUB_OSC_WAVES;
        comp = new Chooser("Sub Osc Wave", this, "oscsuboscwave", params);
        vbox.add(comp);

        params = SUB_OSC_OCTAVES;
        comp = new Chooser("Sub Osc Octave", this, "oscsuboscoctave", params);
        vbox.add(comp);

        hbox.add(vbox);
        vbox = new VBox();
                
        comp = new CheckBox("Sync 1->2", this, "oscsync");
        vbox.add(comp);

        hbox.add(vbox);

        comp = new LabelledDial("Sub Osc", this, "subosccoarse", color, 0, 255)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val) { return COARSE_VALUES[val]; }
            };
        ((LabelledDial)comp).addAdditionalLabel("Coarse");
        hbox.add(comp);

        comp = new LabelledDial("Sub Osc", this, "suboscfine", color, 27, 228, 127)
            {
            public boolean isSymmetric() { return true; }
            public int getDefaultValue() { return 126; }
            };
        ((LabelledDial)comp).addAdditionalLabel("Fine");
        hbox.add(comp);
        
        comp = new LabelledDial("Osc Error", this, "oscerror", color, 0, 7);
        hbox.add(comp);

        category.add(hbox, BorderLayout.WEST);
        return category;
        }
   
    
    public JComponent addMixer(Color color)
        {
        Category category = new Category(this, "Mixer", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        comp = new LabelledDial("Osc 1", this, "mixerosc1level", color, 0, 255);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);

        comp = new LabelledDial("Osc 2", this, "mixerosc2level", color, 0, 255);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);

        comp = new LabelledDial("Sub Osc", this, "mixersubosclevel", color, 0, 255);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);

        comp = new LabelledDial("Noise", this, "mixernoiselevel", color, 0, 255);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);

        comp = new LabelledDial("Ring Mod", this, "mixerringmodlevel", color, 0, 255);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);

        comp = new LabelledDial("Ext. Signal", this, "mixerexternalsignallevel", color, 0, 255);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);

        category.add(hbox, BorderLayout.WEST);
        return category;
        }
   
   
    VBox shapeVBox = new VBox();
    VBox outerVBox = new VBox();
   
    public JComponent addFilter(Color color)
        {
        Category category = new Category(this, "Filter", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        
        params = FILTER_TYPES;
        comp = new Chooser("Type", this, "filtertype", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                outerVBox.removeAll();
                if (model.get(key) == 0)
                    {
                    outerVBox.add(shapeVBox);
                    }
                outerVBox.revalidate();
                outerVBox.repaint();
                }
            };
        vbox.add(comp);

        hbox.add(vbox);
                
        params = FILTER_SHAPES;
        comp = new Chooser("Shape", this, "filtershape", params);
        shapeVBox.add(comp);

        params = FILTER_SLOPES;
        comp = new Chooser("Slope", this, "filterslope", params);
        shapeVBox.add(comp);

        outerVBox.add(shapeVBox);

        hbox.add(outerVBox);

        comp = new LabelledDial("Frequency", this, "filterfrequency", color, 0, 255);
        hbox.add(comp);

        comp = new LabelledDial("Resonance", this, "filterresonance", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Overdrive", this, "filteroverdrive", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Tracking", this, "filtertracking", color, 0, 7);
        hbox.add(comp);

        comp = new LabelledDial("LFO2 Depth", this, "filterlfo2depth", color, 0, 255, 127)
            {
            public boolean isSymmetric() { return true; }
            };
        hbox.add(comp);

        comp = new LabelledDial("Mod Wheel", this, "filtermodenvdepth", color, 0, 126, 63)
            {
            public boolean isSymmetric() { return true; }
            };
        ((LabelledDial)comp).addAdditionalLabel("Env Depth");
        hbox.add(comp);

        category.add(hbox, BorderLayout.WEST);
        return category;
        }

    public JComponent addEffects(Color color)
        {
        Category category = new Category(this, "Effects", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        comp = new LabelledDial("Distortion", this, "fxdistortion", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Osc Filter Mod", this, "fxoscfiltermod", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("VCA Limiter", this, "vcalimiter", color, 0, 127);
        hbox.add(comp);

        category.add(hbox, BorderLayout.WEST);
        return category;
        }
   
    HBox ampOuter = new HBox();
    EnvelopeDisplay ampNormal = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
        new String[] { null, "ampenvattack", "ampenvdecay", null, "ampenvrelease" },
        new String[] { null, null, "ampenvsustain", "ampenvsustain", null },
        new double[] { 0, 0.25/127.0, 0.25/127.0,  0.25, 0.25/127.0},
        new double[] { 0, 1.0, 1.0 / 127.0, 1.0/127.0, 0 });
                
    EnvelopeDisplay ampFixed = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
        new String[] { null, "ampenvattack", "ampenvdecay", "ampenvrelease" },
        new String[] { null, "ampenvsustain", "ampenvsustain", null },
        new double[] { 0, 0.333/127.0, 0.333/127.0, 0.333/127.0},
        new double[] { 0, 1.0 / 127.0, 1.0 / 127.0, 0 });
                        
    HBox ampRetrigBox = new HBox();
    JComponent ampRetrig;
                        
    public JComponent addAmpEnvelope(Color color)
        {
        Category category = new Category(this, "Amplifier Envelope", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();

        params = TRIGGERS;
        comp = new Chooser("Trigger", this, "ampenvtrigger", params);
        vbox.add(comp);

        comp = new CheckBox("Retrigger", this, "ampenvretrigger")
        	{
            public void update(String key, Model model)
                {
                super.update(key, model);
                ampRetrigBox.removeAll();
                if (model.get(key) == 1)
                	{
                	ampRetrigBox.add(ampRetrig);
                	}
                ampRetrigBox.revalidate();
                ampRetrigBox.repaint();
                }
        	};
        vbox.add(comp);

        comp = new CheckBox("Fixed Sustain", this, "ampfixedsustain")
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                ampOuter.removeAll();
                if (model.get(key) == 0)
                    {
                    ampOuter.addLast(ampNormal);
                    }
                else
                    {
                    ampOuter.addLast(ampFixed);
                    }
                ampOuter.revalidate();
                ampOuter.repaint();
                }
            };
        vbox.add(comp);

        hbox.add(vbox);

        comp = new LabelledDial("Attack", this, "ampenvattack", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Decay", this, "ampenvdecay", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Sustain", this, "ampenvsustain", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Release", this, "ampenvrelease", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Velocity ->", this, "velocityampenv", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Env");
        hbox.add(comp);

        ampRetrig = new LabelledDial("Retrigger", this, "ampenvretriggercount", color, 0, 16)
        	{
        	public String map(int val)
        		{
        		if (val == 0) return "Inf";
        		else return "" + val;
        		}
        	};
        ((LabelledDial)ampRetrig).addAdditionalLabel("Count");
        ampRetrigBox.add(ampRetrig);
        hbox.add(ampRetrigBox);

        ampOuter.addLast(ampNormal);
        hbox.addLast(ampOuter);
                
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    HBox modOuter = new HBox();
    EnvelopeDisplay modNormal = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
        new String[] { null, "modenvattack", "modenvdecay", null, "modenvrelease" },
        new String[] { null, null, "modenvsustain", "modenvsustain", null },
        new double[] { 0, 0.25/127.0, 0.25/127.0,  0.25, 0.25/127.0},
        new double[] { 0, 1.0, 1.0 / 127.0, 1.0/127.0, 0 });
                
    EnvelopeDisplay modFixed = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
        new String[] { null, "modenvattack", "modenvdecay", "modenvrelease" },
        new String[] { null, "modenvsustain", "modenvsustain", null },
        new double[] { 0, 0.333/127.0, 0.333/127.0, 0.333/127.0},
        new double[] { 0, 1.0 / 127.0, 1.0 / 127.0, 0 });
        
    HBox modRetrigBox = new HBox();
    JComponent modRetrig;
         
    public JComponent addModEnvelope(Color color)
        {
        Category category = new Category(this, "Modulation Envelope", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();

        params = TRIGGERS;
        comp = new Chooser("Trigger", this, "modenvtrigger", params);
        vbox.add(comp);

        comp = new CheckBox("Retrigger", this, "modenvretrigger")
        	{
            public void update(String key, Model model)
                {
                super.update(key, model);
                modRetrigBox.removeAll();
                if (model.get(key) == 1)
                	{
                	modRetrigBox.add(modRetrig);
                	}
                modRetrigBox.revalidate();
                modRetrigBox.repaint();
                }
        	};
        vbox.add(comp);

        comp = new CheckBox("Fixed Sustain", this, "modfixedsustain")
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                modOuter.removeAll();
                if (model.get(key) == 0)
                    {
                    modOuter.addLast(modNormal);
                    }
                else
                    {
                    modOuter.addLast(modFixed);
                    }
                modOuter.revalidate();
                modOuter.repaint();
                }
            };
        vbox.add(comp);

        hbox.add(vbox);

        comp = new LabelledDial("Attack", this, "modenvattack", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Decay", this, "modenvdecay", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Sustain", this, "modenvsustain", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Release", this, "modenvrelease", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Velocity ->", this, "velocitymodenv", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Env");
        hbox.add(comp);
        
        modRetrig = new LabelledDial("Retrigger", this, "modenvretriggercount", color, 0, 16)
        	{
        	public String map(int val)
        		{
        		if (val == 0) return "Inf";
        		else return "" + val;
        		}
        	};
        ((LabelledDial)modRetrig).addAdditionalLabel("Count");
        modRetrigBox.add(modRetrig);
        hbox.add(modRetrigBox);

        modOuter.addLast(modNormal);
        hbox.addLast(modOuter);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addLFO(int lfo, Color color)
        {
        Category category = new Category(this, "LFO " + lfo, color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = LFO_WAVES;
        comp = new Chooser("Wave", this, "lfo" + lfo + "wave", params);
        vbox.add(comp);

        params = LFO_SYNC_SWITCH;
        comp = new Chooser("Speed/Sync", this, "lfo" + lfo + "speedsync", params);
        vbox.add(comp);

        hbox.add(vbox);
        vbox = new VBox();

        params = LFO_SYNC_VALUES;
        comp = new Chooser("Sync", this, "lfo" + lfo + "syncvalue", params);
        vbox.add(comp);

        comp = new CheckBox("Key Sync", this, "lfo" + lfo + "keysync");
        vbox.add(comp);
        
        hbox.add(vbox);

        comp = new LabelledDial("Delay", this, "lfo" + lfo + "delay", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Slew", this, "lfo" + lfo + "slew", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Speed", this, "lfo" + lfo + "speed", color, 0, 255);
        hbox.add(comp);

        category.add(hbox, BorderLayout.WEST);
        return category;
        }

    public JComponent addArp(Color color)
        {
        Category category = new Category(this, "Arpeggiator", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        
        comp = new CheckBox("On", this, "arpon");
        vbox.add(comp);

        comp = new CheckBox("Latch", this, "arplatch");
        vbox.add(comp);

        hbox.add(vbox);
        vbox = new VBox();

        params = ARP_MODES;
        comp = new Chooser("Mode", this, "arpmode", params);
        vbox.add(comp);

        comp = new CheckBox("Sequence Retrigger", this, "arpseqretrig");
        vbox.add(comp);

        hbox.add(vbox);

        comp = new LabelledDial("Octaves", this, "arpoctaves", color, 0, 3, -1);
        hbox.add(comp);

        comp = new LabelledDial("Rhythm", this, "arprhythm", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Swing", this, "arpswing", color, 3, 97)
            {
            public String map(int val) { return "" + val + "%"; }
            };
        hbox.add(comp);

        category.add(hbox, BorderLayout.WEST);
        return category;
        }

    public JComponent addModWheel(Color color)
        {
        Category category = new Category(this, "Modulation Wheel", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        
        comp = new LabelledDial("Filter", this, "modwheelfilterfreq", color, 0, 127, 64);               
        ((LabelledDial)comp).addAdditionalLabel("Frequency");
        hbox.add(comp);

        comp = new LabelledDial("LFO 1 ->", this, "modwheellfo1tooscpitch", color, 0, 127, 64);         
        ((LabelledDial)comp).addAdditionalLabel("Osc Pitch");
        hbox.add(comp);

        comp = new LabelledDial("LFO 2 ->", this, "modwheellfo2tofilterfreq", color, 0, 127, 64);               
        ((LabelledDial)comp).addAdditionalLabel("Filter Freq");
        hbox.add(comp);

        comp = new LabelledDial("Osc 2 Pitch", this, "modwheelosc2pitch", color, 0, 127, 64);           
        hbox.add(comp);

        category.add(hbox, BorderLayout.WEST);
        return category;
        }

    public JComponent addAftertouch(Color color)
        {
        Category category = new Category(this, "Aftertouch", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        
        comp = new LabelledDial("Filter", this, "aftertouchfilterfreq", color, 0, 127, 64);             
        ((LabelledDial)comp).addAdditionalLabel("Frequency");
        hbox.add(comp);

        comp = new LabelledDial("LFO 1 ->", this, "aftertouchlfo1toosc1and2pitch", color, 0, 127, 64);          
        ((LabelledDial)comp).addAdditionalLabel("Osc 1/2 Pitch");
        hbox.add(comp);

        comp = new LabelledDial("LFO 2 Speed", this, "aftertouchlfo2speed", color, 0, 127, 64);         
        hbox.add(comp);

        category.add(hbox, BorderLayout.WEST);
        return category;
        }

    public void changePatch(Model tempModel)
        {
        byte NN = (byte)tempModel.get("number");
        try {
            tryToSendMIDI(new ShortMessage(ShortMessage.PROGRAM_CHANGE, getChannelOut(), NN, 0));
            }
        catch (Exception e) { Synth.handleException(e); }
        }

    public byte[] requestDump(Model tempModel)
        {
        if (tempModel == null)
            tempModel = getModel();
            
        byte NN = (byte)tempModel.get("number");
        return new byte[] { (byte)0xF0, 0x00, 0x20, 0x29, 0x00, 0x33, 0x00, 0x41, NN, (byte)0xF7 };
        }
    
    public byte[] requestCurrentDump()
        {
        return new byte[] { (byte)0xF0, 0x00, 0x20, 0x29, 0x00, 0x33, 0x00, 0x40, (byte)0xF7 };
        }

    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        JTextField number = new SelectedTextField("" + model.get("number"), 3);
                
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
                showSimpleError(title, "The Patch Number must be an integer 0 ... 127");
                continue;
                }
            if (n < 0 || n > 127)
                {
                showSimpleError(title, "The Patch Number must be an integer 0 ... 127");
                continue;
                }
                                
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
        
    public String getPatchName(Model model) { return model.get("name", "Init"); }

    public Model getNextPatchLocation(Model model)
        {
        int number = model.get("number");
        
        number++;
        if (number >= 128)
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
        
        return ("" + model.get("number"));
        }
        
    public String[] getPatchNumberNames() 
        {
        return buildIntegerNames(128, 0);
        }

    public String[] getBankNames() 
        {
        return null;
        }

    public boolean getSupportsPatchWrites() 
        {
        return true;
        }

    public int getPatchNameLength() 
        {
        return 16;
        }

    public int parse(byte[] data, boolean fromFile)
        {
        if (data[7] == 0x01)                        // it's a numbered patch
            {
            model.set("number", data[8]);           // should we change the patch number when it's NOT numbered?
            }
                
        byte[] n = new byte[16];
        System.arraycopy(data, 137, n, 0, 16);
        try
            {
            model.set("name", new String(n, "US-ASCII"));
            }
        catch (UnsupportedEncodingException ex) { }

        for(int i = 0; i < PARAMETERS.length; i++)
            {
            int param = extractParameter(data, BYTE_OFFSETS[i], BIT_OFFSETS[i][0], BIT_OFFSETS[i][1], LENGTHS[i]);

            if (PARAMETERS[i].equals("octave"))
                {
                // Map 78...8A [by 2] to -4...+5
                model.set(PARAMETERS[i], (param - 0x78) / 2 - 4);
                }
            else if (PARAMETERS[i].equals("osc1manualpw") ||
                PARAMETERS[i].equals("osc2manualpw"))
                {
                model.set(PARAMETERS[i], getOscPWParam(param));
                }
            else if (PARAMETERS[i].equals("osc1range") || 
                PARAMETERS[i].equals("osc2range"))
                {
                // Map 3F...42 to 0...4
                model.set(PARAMETERS[i], param - 0x3F);
                }
            else if (PARAMETERS[i].equals("osc1fine") ||
                PARAMETERS[i].equals("osc2fine") ||
                PARAMETERS[i].equals("suboscfine") ||
                PARAMETERS[i].equals("osc1lfo1depth") ||
                PARAMETERS[i].equals("osc2lfo1depth") ||
                PARAMETERS[i].equals("filterlfo2depth"))
                {
                // There is a hole in the parameters, so we have to deal with that.
                // Map 0...126 to 0...126, 128 [and I guess 127] to 127, and 129...255 to 128...254
                if (param <= 127)
                    {
                    model.set(PARAMETERS[i], param);
                    }
                else 
                    {
                    model.set(PARAMETERS[i], param - 1);
                    }
                }
            else if (PARAMETERS[i].equals("osc1modenvdepth") ||
                PARAMETERS[i].equals("osc2modenvdepth") ||
                PARAMETERS[i].equals("filtermodenvdepth") ||
                PARAMETERS[i].equals("osc1lfo2pwmod") ||
                PARAMETERS[i].equals("osc2lfo2pwmod") ||
                PARAMETERS[i].equals("osc1modenvpwmod") ||
                PARAMETERS[i].equals("osc2modenvpwmod"))
                {
                // There is a hole in the parameters, so we have to deal with that.
                // Map 0...62 to 0...62, 64 [and I guess 63] to 63, and 65...127 to 64...126
                if (param <= 63)
                    {
                    model.set(PARAMETERS[i], param);
                    }
                else
                    {
                    model.set(PARAMETERS[i], param - 1);
                    }
                }
            else
                {
                model.set(PARAMETERS[i], param);
                }
            }
                        
        revise();
        return PARSE_SUCCEEDED;
        }

    // public boolean getPrintRevised() { return true; }

    public byte[] emit(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        if (tempModel == null)
            tempModel = getModel();

        byte[] data = new byte[137 + 16 + 1];
        data[0] = (byte)0xF0;
        data[1] = 0x00;
        data[2] = 0x20;
        data[3] = 0x29;
        data[4] = 0x00;
        data[5] = 0x33;
        data[6] = 0x00;
        if (toWorkingMemory) data[7] = 0x00;
        else data[7] = 0x01;
        if (toWorkingMemory) data[8] = 0x00;
        else data[8] = (byte)model.get("number");
        data[9] = 0x00;
        data[10] = 0x00;
        data[11] = 0x00;
        data[12] = 0x00;
        data[30] = 0x01;
        data[31] = 0x00;
        data[32] = 0x43;
        data[35] = 0x00;
        data[96] = 0x40;
        data[104] = 0x40;
        data[data.length - 1] = (byte)0xF7;
                        
        for(int i = 0; i < PARAMETERS.length; i++)
            {
            int val = model.get(PARAMETERS[i]);
            //System.err.println(PARAMETERS[i] + " -> " + val);

            if (PARAMETERS[i].equals("octave"))
                {
                // Map -4...+5 to 78...8A
                val = (val + 4) * 2 + 0x78;
                }
            else if (PARAMETERS[i].equals("osc1manualpw") ||
                PARAMETERS[i].equals("osc2manualpw"))
                {
                val = OSC_PULSEWIDTH_ENCODINGS[val - 5];
                }
            else if (PARAMETERS[i].equals("osc1range") || 
                PARAMETERS[i].equals("osc2range"))
                {
                // Map 0...4 to 3F...42
                val = val + 0x3F;
                }
            else if (PARAMETERS[i].equals("osc1fine") ||
                PARAMETERS[i].equals("osc2fine") ||
                PARAMETERS[i].equals("suboscfine") ||
                PARAMETERS[i].equals("osc1lfo1depth") ||
                PARAMETERS[i].equals("osc2lfo1depth") ||
                PARAMETERS[i].equals("filterlfo2depth"))
                {
                // There is a hole in the parameters, so we have to deal with that.
                // Map 0...126 to 0...126, 128 [and I guess 127] to 127, and 129...255 to 128...254
                if (val <= 126)
                    {
                    // do nothing
                    }
                else 
                    {
                    val++;
                    }
                }
            else if (PARAMETERS[i].equals("osc1modenvdepth") ||
                PARAMETERS[i].equals("osc2modenvdepth") ||
                PARAMETERS[i].equals("filtermodenvdepth") ||
                PARAMETERS[i].equals("osc1lfo2pwmod") ||
                PARAMETERS[i].equals("osc2lfo2pwmod") ||
                PARAMETERS[i].equals("osc1modenvpwmod") ||
                PARAMETERS[i].equals("osc2modenvpwmod"))
                {
                // There is a hole in the parameters, so we have to deal with that.
                // Map 0...62 to 0...62, 63 to 64, and 64...126 to 65...127
                if (val <= 62)
                    {
                    // do nothing
                    }
                else
                    {
                    val++;
                    }
                }
            else
                {
                // do nothing
                }
                                
            embedParameter(data, BYTE_OFFSETS[i], BIT_OFFSETS[i][0], BIT_OFFSETS[i][1], LENGTHS[i], val);
            }
                
        try
            {       
            String n = model.get("name", "Init") + "                ";
            System.arraycopy(n.getBytes("US-ASCII"), 0, data, 137, 16);
            }
        catch (UnsupportedEncodingException ex) { }

        return data;
        }
        
/*
// For the time being, we're emitting the whole current patch

public byte[] emit(String key) 
{ 
return emit(null, true, false); 
}
*/


////// Extracts a parameter from the bits in SYSEX given the byte offset of its first byte, and the
////// high bit offsets of each of its bytes (MSB byte = -1 if there's only one byte), and the
////// length of the parameter.

// byteoffset is the index in sysex where the first involved byte lives
// bitoffsetMSB is the highest bit in the first byte (bit 0 is the first bit).
//              If there is only one byte, bitoffsetMSB is -1 and should be ignored.
// bitoffsetLSB is the higherst bit in the second byte (bit 0 is the first bit).

    public int extractParameter(byte[] sysex, int byteOffset, int bitOffsetMSB, int bitOffsetLSB, int len)
        {
        if (bitOffsetMSB < 0)           // One byte
            {
            /*
              Examples
              00001000   len = 1 bitOffsetLSB = 3  bLen = len
              10000000   push up:   val = val << (7 - bitOffsetLSB)
              00000001   push down: val = val >>> (8 - bLen)
        
              00111110   len = 5 bitOffsetLSB = 5  bLen = len
              11111000   push up:   val = val << (7 - bitOffsetLSB)
              00011111   push down: val = val >>> (8 - bLen)

              00001100   len = 2 bitOffsetLSB = 3  bLen = len
              11000000   push up:   val = val << (7 - bitOffsetLSB)
              00000011   push down: val = val >>> (8 - bLen)
            */
        
            int bLen = len;
            int b = ((int)sysex[byteOffset]) & 0xFF;
            b = ((b << (7 - bitOffsetLSB)) & 0xFF) >>> (8 - bLen);          // shift up to push things out, then shift down to pos 0
            return b;
            }
        else                                            // two bytes
            {
            /*
              Examples
              00011111 01000000   len = 6 bitOffsetLSB = 6 bitOffsetMSB = 4  aLen = bitOffsetMSB + 1 = 5   bLen = len - aLen = 1
              00011111            a
              11111000            push up a:    a << (7 - bitOffsetMSB)
              00011111            push down a:  a >>> (8 - aLen)        we do all this to clear out the other bits
              01000000   b
              10000000   push up b:    b << (7 - bitOffsetLSB)
              00000001   push down b:  b >>> (8 - bLen)        we do all this to clear out the other bits

        
              00000011 01111100   len = 7 bitOffsetLSB = 6 bitOffsetMSB = 1  aLen = bitOffsetMSB + 1 = 2   bLen = len - aLen = 5
              00000011            a
              11000000            push up a:    a << (7 - bitOffsetMSB)
              00000011            push down a:  a >>> (8 - aLen)        we do all this to clear out the other bits
              01111100   b
              11111000   push up b:    b << (7 - bitOffsetLSB)
              00011111   push down b:  b >>> (8 - bLen)        we do all this to clear out the other bits
            */
        
            int aLen = bitOffsetMSB + 1;                                            // MSB always starts at bit 0
            int bLen = len - aLen;
                
            int a = ((int)sysex[byteOffset]) & 0xFF;
            a = ((a << (7 - bitOffsetMSB)) & 0xFF) >>> (8 - aLen);          // shift up to push things out, then shift down to pos 0
            int b = ((int)sysex[byteOffset + 1]) & 0xFF;
            b = ((b << (7 - bitOffsetLSB)) & 0xFF) >>> (8 - bLen);          // shift up to push things out, then shift down to pos 0
                
            return (a << bLen) | b;
            }
        }
        
////// Sets a parameter from the bits in SYSEX to a given val given the byte offset of its first byte, and the
////// high bit offsets of each of its bytes (MSB byte = -1 if there's only one byte), and the
////// length of the parameter.

// byteoffset is the index in sysex where the first involved byte lives
// bitoffsetMSB is the highest bit in the first byte (bit 0 is the first bit).
//              If there is only one byte, bitoffsetMSB is -1 and should be ignored.
// bitoffsetLSB is the higherst bit in the second byte (bit 0 is the first bit).

    public void embedParameter(byte[] sysex, int byteOffset, int bitOffsetMSB, int bitOffsetLSB, int len, int val)
        {
        if (bitOffsetMSB < 0)           // one byte
            {
            /*
              Examples
              00000001   len = 1 bitOffsetLSB = 3
              00001000
        
              00011111
              00111110   len = 5 bitOffsetLSB = 5

              00000011
              00001100   len = 2 bitOffsetLSB = 3
        
            */
        
        
            int bLen = len;
            int b = (val << (bitOffsetLSB + 1 - len)) & 0xFF;
            int bv = ((int)sysex[byteOffset]) & 0xFF;
            sysex[byteOffset] = (byte)((bv | b) & 0xFF);
            }
        else
            {
            /*
              Examples
              00011111 01000000
              00000000 00111111   len = 6 bitOffsetLSB = 6 bitOffsetMSB = 4
              00000000 00011111   aLen = bitOffsetMSB + 1 = 5    a = val >>> (len - aLen)
              00000000 00111111   [start again]
              00000000 00000001   val >>> aLen      bLen = (len - aLen) = 1
              00000000 01000000   val << (7 - bLen) = 6
        
              00000011 01111100
              00000000 01111111   len = 7 bitOffsetLSB = 6 bitOffsetMSB = 1
              00000000 00000011   aLen = bitOffsetMSB + 1 = 2    a = val >>> (len - aLen)
              00000000 01111111   [start again]
              00000000 00011111   val >>> aLen      bLen = (len - aLen) = 5
              00000000 01111100   val << (7 - bLen) = 2
            */
                
            int aLen = bitOffsetMSB + 1;
            int a = (val >>> (len - aLen)) & 0xFF;
            int av = ((int)sysex[byteOffset]) & 0xFF;
            sysex[byteOffset] = (byte)((av | a) & 0xFF);
                
            int bLen = (len - aLen);
            int b = (val << (8 - bLen)) & 0xFF;     // push up to remove the MSB bits
            b = b >>> (7 - bitOffsetLSB);                           // push down to slot
            int bv = ((int)sysex[byteOffset + 1]) & 0xFF;
            sysex[byteOffset + 1] = (byte)((bv | b) & 0xFF);
            }
        }
        
        

/** Byte offsets start at the beginning of the sysex message (F0) */
    public static final int[] BYTE_OFFSETS = 
        {
        13, 14, 16, 18, 19, 19, 20, 21, 
        22, 24, 25, 26, 27, 28, 36, 37, 
        37, 38, 39, 41, 42, 43, 44, 45, 
        46, 48, 48, 48, 49, 50, 51, 52, 
        53, 55, 56, 57, 58, 59, 60, 62, 
        63, 64, 65, 66, 67, 69, 69, 70, 
        70, 72, 73, 74, 76, 76, 77, 77, 
        77, 78, 79, 80, 81, 82, 83, 84, 
        85, 86, 88, 89, 90, 91, 93, 94, 
        97, 98, 99, 101, 102, 105, 106, 107, 
        108, 111, 112, 114, 115, 115, 117, 118, 
        119, 120, 121, 122, 33, 34
        };


// Low bit values for the packed bit ranges
// {x, y}   x is the high bit in the first byte (0 is the first bit), y is the high bit in the second byte
//          If there is only one byte, then x is -1
//          Note that if there are two bytes, then the low bit of the first byte is always 0
    public static final int[][] BIT_OFFSETS = 
        {
        {1, 6}, {0, 6}, {-1, 6}, {-1, 6}, {-1, 6}, {3, 6}, {2, 6}, {2, 6},              // 0
        {1, 6}, {-1, 1}, {5, 6}, {4, 6}, {4, 6}, {3, 6}, {-1, 5}, {-1, 3},              // 8
        {2, 6}, {1, 6}, {0, 6}, {6, 6}, {5, 6}, {4, 6}, {3, 6}, {1, 6},                 //16
        {0, 6}, {-1, 3}, {-1, 2}, {-1, 1}, {5, 6}, {4, 6}, {3, 6}, {2, 6},              //24
        {1, 6}, {-1, 2}, {-1, 6}, {5, 6}, {4, 6}, {3, 6}, {2, 6}, {-1, 3},              //32
        {-1, 2}, {-1, 6}, {5, 6}, {5, 6}, {2, 6}, {-1, 3}, {-1, 4}, {-1, 3},            //40
        {0, 6}, {-1, 6}, {6, 6}, {3, 6}, {-1, 4}, {-1, 5}, {-1, 3}, {-1, 4},            //48
        {-1, 5}, {-1, 4}, {-1, 3}, {-1, 4}, {5, 6}, {4, 6}, {3, 6}, {2, 6},             //56
        {1, 6}, {0, 6}, {-1, 6}, {5, 6}, {5, 6}, {4, 6}, {1, 6}, {0, 6},                //64
        {6, 6}, {4, 6}, {3, 6}, {1, 6}, {0, 6}, {5, 6}, {4, 6}, {3, 6},                 //72
        {2, 6}, {-1, 1}, {-1, 2}, {-1, 6}, {-1, 5}, {0, 6}, {-1, 5}, {-1, 5},           //80
        {-1, 1}, {-1, 0}, {3, 6}, {2, 6}, {6, 6}, {5, 6}                                //88
        };
        
// Total bitlengths for the packed bit ranges
    public static final int[] LENGTHS = 
        { 
        7, 8, 7, 1, 2, 7, 7, 8,         //  0
        8, 2, 7, 7, 8, 8, 2, 1,         //  8
        8, 8, 8, 8, 8, 8, 8, 7,         // 16
        7, 1, 1, 2, 7, 7, 7, 7,         // 24
        7, 2, 7, 7, 7, 7, 7, 2,         // 32
        2, 7, 7, 8, 6, 1, 1, 2,         // 40
        7, 7, 8, 6, 1, 1, 1, 1,         // 48
        1, 3, 3, 5, 7, 7, 7, 7,         // 56
        7, 7, 7, 7, 8, 8, 7, 7,         // 64
        8, 7, 7, 7, 7, 7, 7, 7,         // 72
        7, 1, 3, 1, 1, 4, 3, 4,         // 80
        1, 1, 5, 5, 8, 8                // 88
        }; 
        
// Parameter Names
    public static final String[] PARAMETERS = { 
        "portamentotime",                       // 0
        "octave",
        "pitchbendrange",
        "oscsync",
        "osc1wave",
        "osc1manualpw",
        "osc1range",
        "osc1coarse",

        "osc1fine",                                     // 8
        "osc2wave",
        "osc2manualpw",
        "osc2range",
        "osc2coarse",
        "osc2fine",
        "oscsuboscwave",
        "oscsuboscoctave",

        "mixerosc1level",                       //16
        "mixerosc2level",
        "mixersubosclevel",
        "mixernoiselevel",
        "mixerringmodlevel",
        "mixerexternalsignallevel",
        "filterfrequency",
        "filterresonance",

        "filteroverdrive",                      //24
        "filterslope",
        "filtertype",
        "filtershape",
        "velocityampenv",
        "ampenvattack",
        "ampenvdecay",
        "ampenvsustain",

        "ampenvrelease",                        //32
        "ampenvtrigger",
        "velocitymodenv",
        "modenvattack",
        "modenvdecay",
        "modenvsustain",
        "modenvrelease",
        "modenvtrigger",

        "lfo1wave",                                     //40
        "lfo1delay",
        "lfo1slew",
        "lfo1speed",
        "lfo1syncvalue",
        "lfo1speedsync",
        "lfo1keysync",
        "lfo2wave",

        "lfo2delay",                            //48
        "lfo2slew",
        "lfo2speed",
        "lfo2syncvalue",
        "lfo2speedsync",
        "lfo2keysync",
        "arpon",
        "arplatch",
        
        "arpseqretrig",                         //56
        "arpoctaves",
        "arpmode",
        "arprhythm",
        "arpswing",
        "modwheelfilterfreq",
        "modwheellfo1tooscpitch",
        "modwheellfo2tofilterfreq",
        
        "modwheelosc2pitch",            //64
        "aftertouchfilterfreq",
        "aftertouchlfo1toosc1and2pitch",
        "aftertouchlfo2speed",
        "osc1lfo1depth",
        "osc2lfo1depth",
        "osc1lfo2pwmod",
        "osc2lfo2pwmod",
        
        "filterlfo2depth",                      //72
        "osc1modenvdepth",
        "osc2modenvdepth",
        "osc1modenvpwmod",
        "osc2modenvpwmod",
        "filtermodenvdepth",
        "fxoscfiltermod",
        "fxdistortion",
        
        "vcalimiter",                           //80
        "paraphonic",
        "filtertracking",
        "ampenvretrigger",
        "modenvretrigger",
        "tuningtable",
        "oscerror",
        "portamentodivergence",
        
        "ampfixedsustain",                      //88
        "modfixedsustain",
        "ampenvretriggercount",
        "modenvretriggercount",
        "subosccoarse",							// these are out of order but I don't want to rejigger everything to insert them...
        "suboscfine"
        };

    }







/*
  NOVATION BASS STATION II SYSEX PROTOCOL

  This text describes the reverse-engineered sysex protocol for the Novation
  Bass Station II as of Firmware v4.15.  The Bass Station II sysex is not documented
  by Novation, and its data encoding is a complete mess, inconsistent in bizarre ways 
  and filled with bit packing.  This description is based in part on earlier work 
  by Francois Gregory (francois.georgy@gmail.com).  See https://github.com/francoisgeorgy/BS2-SysEx
  To this I have fixed many errors and added missing items.
  
  This text does not yet describe the AFX protocol, as it has not yet been reverse engineered.



  COMMANDS

  The Bass Station II responds to at least these four sysex commands
  (and also transmits the two Dump commnds below):

  Request Current Patch
  F0 00 20 29 00 33 00 40 F7

  Dump Current Patch
  F0 00 20 29 00 33 00 00 00 00 00 00 00 DATA... F7

  Request Patch
  F0 00 20 29 00 33 00 41 PATCHNUMBER F7

  Dump Patch
  F0 00 20 29 00 33 00 01 PATCHNUMBER 00 00 00 00 DATA... F7

  Change Patch
  Use Program Change. There is a single bank, and patch numbers are 00...7F.
  



  PATCH DATA 
  (in Dump Patch and Dump Current Patch commands)

  DATA... is as follows.  Note that the SYSEX OFFSET is from the start of the
  message (0xF0 is sysex offset 0).

 

  SYSEX  NUM   BITMASKS BITMASKS IN BINARY   NUM  PARAMETER
  OFFSET BYTES IN HEX   FIRST    SECOND      BITS NAME                              VALUES
  --------------------------------------------------------------------------------------------------------------------------------------------
  13     2     03 7C    00000011 01111100    7    Portamento Time                   0-127
  14     2     01 7F    00000001 01111111    8    Octaves                           78, 7A, 7C, 7E, 80, 82, 84, 86, 88, 8A, which encode -4...+5
  16     1     7F       01111111             7    Pitch Bend Range                  0...48        -24...+24
  18     1     40       01000000             1    Osc 1-2 Sync                      0/1                                       
  19     1     60       01100000             2    Osc 1 Waveform                    0-3           Sine/Triangle/Saw/Square
  19     2     0F 70    00001111 01110000    7    Osc 1 Manual PW                   0-127         [SEE TABLE 3]
  20     2     07 78    00000111 01111000    7    Osc 1 Range                       3F, 40, 41, 42, which encode 16'/8'/4'/2'       
  21     2     07 7C    00000111 01111100    8    Osc 1 Coarse                      0-255         [SEE TABLE 4]

  22     2     03 7E    00000011 01111110    8    Osc 1 Fine                        27 = -100 ... 126 = -1, 127 = 0, 128 = 0, 129 = 1 ... 288 = 100
  24     1     03       00000011             2    Osc 2 Waveform                    0-3           Sine/Triangle/Saw/Square
  25     2     3F 40    00111111 01000000    7    Osc 2 Manual PW                   0-127         [SEE TABLE 3]
  26     2     1F 60    00011111 01100000    7    Osc 2 Range                       3F, 40, 41, 42, which encode 16'/8'/4'/2'
  27     2     1F 70    00011111 01110000    8    Osc 2 Coarse                      0-255         [SEE TABLE 4]
  28     2     0F 78    00001111 01111000    8    Osc 2 Fine                        27 = -100 ... 126 = -1, 127 = 0, 128 = 0, 129 = 1 ... 288 = 100
  30     [Always 0x01]
  31     [Always 0x00]
  32     [Always 0x43]
  33     2     7F 40    01111111 01000000    8    Sub Osc Coarse                   0-255         [SEE TABLE 4]
  34     2     3F 60    00111111 01100000    8    Sub Osc Fine                     27 = -100 ... 126 = -1, 127 = 0, 128 = 0, 129 = 1 ... 288 = 100
  35     [Always 0x00]
  36     1     30       00110000             2    Sub Osc Wave                     0-3            Sine/Tri/Saw/Square
  37     1     08       00001000             1    Sub Osc Oct                      0/1            One/two octaves

  37     2     07 7C    00000111 01111100    8    Mixer Osc 1 Level                0-255          Level
  38     2     03 7E    00000011 01111110    8    Mixer Osc 2 Level                0-255          Level
  39     2     01 7F    00000001 01111111    8    Mixer Sub Osc Level              0-255          Level
  41     2     7F 40    01111111 01000000    8    Mixer Noise Level                0-255          Level
  42     2     3F 60    00111111 01100000    8    Mixer Ring Mod Level             0-255          Level
  43     2     1F 70    00011111 01110000    8    Mixer External Signal Level      0-255          Level
  44     2     0F 78    00001111 01111000    8    Filter Frequency                 0-255          Frequency
  45     2     03 7C    00000011 01111100    7    Filter Resonance                 0-127          Resonance

  46     2     01 7E    00000001 01111110    7    Filter Overdrive                 0-127          Level
  48     1     08       00001000             1    Filter Slope                     0/1            12/24dB
  48     1     04       00000100             1    Filter Type                      0/1            Classic/Acid
  48     1     03       00000011             2    Filter Shape                     0-2            LP/BP/HP        
  49     2     3F 40    00111111 01000000    7    Velocity Amp Env                 1-127          -63 ... +63
  50     2     1F 60    00011111 01100000    7    Amp Env Attack                   0-127
  51     2     0F 70    00001111 01110000    7    Amp Env Decay                    0-127
  52     2     07 78    00000111 01111000    7    Amp Env Sustain                  0-127

  53     2     03 7C    00000011 01111100    7    Amp Env Release                  0-127
  55     1     06       00000110             2    Amp Env Trigger                  0-2            Single/Multi/Autoglide
  56     1     7F       01111111             7    Velocity Mod Env                 1-127          -63 ... +63
  57     2     3F 40    00111111 01000000    7    Mod Env Attack                   0-127
  58     2     1F 60    00011111 01100000    7    Mod Env Decay                    0-127
  59     2     0F 70    00001111 01110000    7    Mod Env Sustain                  0-127
  60     2     07 78    00000111 01111000    7    Mod Env Release                  0-127
  62     1     0C       00001100             2    Mod Env Trigger                  0-2            Single/Multi/Autoglide

  63     1     06       00000110             2    LFO1 Wave                        0-3            Triangle/Sawtooth/Square/Sample and Hold
  64     1     7F       01111111             7    LFO1 Delay                       0-127
  65     2     3F 40    00111111 01000000    7    LFO1 Slew                        0-127
  66     2     3F 60    00111111 01100000    8    LFO1 Speed                       0-255          0 ... 190 Hz but not displayed as such
  67     2     07 70    00000111 01110000    6    LFO1 Sync Value                  0-34           [SEE TABLE 1]
  69     1     08       00001000             1    LFO1 Speed/Sync                  0/1            Speed/Sync
  69     1     10       00010000             1    LFO1 Key Sync                    0/1
  70     1     0C       00001100             2    LFO2 Wave                        0-3            Triangle/Sawtooth/Square/Sample and Hold

  70     2     01 7E    00000001 01111110    7    LFO2 Delay                       0-127
  72     1     7F       01111111             7    LFO2 Slew                        0-127
  73     2     7F 40    01111111 01000000    8    LFO2 Speed                       0-255          0 ... 190 Hz but not displayed as such
  74     2     0F 60    00001111 01100000    6    LFO2 Sync Value                  0-34           [SEE TABLE 1]
  76     1     10       00010000             1    LFO2 Speed/Sync                  0/1            Speed/Sync
  76     1     20       00100000             1    LFO2 Key Sync                    0/1
  77     1     08       00001000             1    Arp On                           0/1
  77     1     1C       00010000             1    Arp Latch                        0/1

  77     1     20       00100000             1    Arp Seq Retrig                   0/1
  78     1     1C       00011100             3    Arp Octaves / Sequence           1...4
  79     1     0E       00001110             3    Arp Mode                         0-7            Up/Down/Up-Down/Up-Down2/Played/Random/Record/Play
  80     1     1F       00011111             5    Arp Rhythm                       0-31           32 undocumented rhythms
  81     2     3F 40    00111111 01000000    7    Arp Swing                        3...97         3% ... 97%
  82     2     1F 60    00011111 01100000    7    Mod Wheel Filter Freq            0-127          -64...+63
  83     2     0F 70    00001111 01110000    7    Mod Wheel LFO1 to Osc Pitch      0-127          -64...+63
  84     2     07 78    00000111 01111000    7    Mod Wheel LFO2 to Filter Freq    0-127          -64...+63

  85     2     03 7C    00000011 01111100    7    Mod Wheel Osc2 Pitch             0-127          -64...+63
  86     2     01 7E    00000001 01111110    7    Aftertouch Filter Freq           0-127          -64...+63
  88     1     7F       01111111             7    Aftertouch LFO1 to Osc 1+2 Pitch 0-127          -64...+63
  89     2     3F 40    00111111 01000000    7    Aftertouch LFO2 Speed            0-127          -64...+63
  90     2     3F 60    00111111 01100000    8    Osc1 LFO1 Depth                  0-255          0 = -127 ... 127 = 0, 128 = 0, 129 = 1 ... 255 = +127   Notice that both 127 and 128 are 0
  91     2     1F 70    00011111 01110000    8    Osc2 LFO1 Depth                  0-255          0 = -127 ... 127 = 0, 128 = 0, 129 = 1 ... 255 = +127   Notice that both 127 and 128 are 0
  93     2     03 7C    00000011 01111100    7    Osc1 LFO2 PW Mod                 0-127          [SEE TABLE 2]
  94     2     01 7E    00000001 01111110    7    Osc2 LFO2 PW Mod                 0-127          [SEE TABLE 2]
  97     2     7F 40    01111111 01000000    8    Filter LFO2 Depth                0-255          0 = -127 ... 127 = 0, 128 = 0, 129 = 1 ... 255 = +127   Notice that both 127 and 128 are 0
  96     [Always 0x40]
  98     2     1F 60    00011111 01100000    7    Osc1 Mod Env Depth               0...127        0 = -63 ... 63 = 0, 64 = 0, 65 = 1 ... 127 = +63   Notice that both 63 and 64 are 0
  99     2     0F 70    00001111 01110000    7    Osc2 Mod Env Depth               0...127        0 = -63 ... 63 = 0, 64 = 0, 65 = 1 ... 127 = +63   Notice that both 63 and 64 are 0
  101    2     01 7C    00000011 01111100    7    Osc1 Mod Env PW Mod              0-127          [SEE TABLE 2]      NOTE: the earlier reverse engineering has an error here
  102    2     01 7E    00000001 01111110    7    Osc2 Mod Env PW Mod              0-127          [SEE TABLE 2]
  104    [Always 0x40]
  105    2     3F 40    00111111 01000000    7    Filter Mod Env Depth             0...127        0 = -63 ... 63 = 0, 64 = 0, 65 = 1 ... 127 = +63   Notice that both 63 and 64 are 0
  106    2     1F 60    00011111 01100000    7    Fx Osc Filter Mod                0-127
  107    2     0F 70    00001111 01110000    7    Fx Distortion                    0-127
  108    2     07 78    00000111 01111000    7    VCA Limiter                      0-127
  111    1     02       00000010             1    Paraphonic                       0/1
  112    1     07       00000111             3    Filter tracking                  0-7
  114    1     40       01000000             1    Amp Env Retrigger                0/1
  115    1     20       00100000             1    Mod Env Retrigger                0/1
  115    2     01 70    00000001 01110000    4    Tuning table                     0-8            Novation's documentation says 0-9, which appears to be wrong
  117    1     38       00111000             3    Osc Error                        0-7
  118    1     3C       00111100             4    Glide Divergence                 0-15
  119    1     02       00000010             1    Fixed Amp Sustain Envelope       0-1
  120    1     01       00000001             1    Fixed Mod Sustain Envelope       0-1
  121    2     0F 40    00001111 01000000    5    Amp Env Retrigger Count          0-16           0=Infinite, 1...16
  122    2     07 60    00000111 01100000    5    Mod Env Retrigger Count          0-16           0=Infinite, 1...16
  137    16    16x 7F   16x 01111111     16x 8    Patch name                       16 ASCII chars      Not displayed on unit!

  Note that the Bass Station II manual states that Select Noise/Ring/Ext, Key Transpose,
  Mod Wheel, Midi Channel, Local, and Input Gain are part of the initial settings of a
  patch, implying that they're stored with the patch.  But they are not.  Volume is also
  not part of the patch.




  TABLE 1
  LFO Sync Values

  Value   Display   Display Meaning   Musical Description                             MIDI Ticks
  ----------------------------------------------------------------------------------------------
  0       64b       64 beats          1 cycle per 16 bars                             1536        
  1       48b       48 beats          1 cycle per 12 bars                             1152        
  2       42b       42 beats          2 cycles per 21 bars                            1002        
  3       36b       36 beats          1 cycle per 9 bars                              864     
  4       32b       32 beats          1 cycle per 8 bars                              768     
  5       30b       30 beats          2 cycles per 15 bars                            720     
  6       28b       28 beats          1 cycle per 7 bars                              672     
  7       24b       24 beats          1 cycle per 6 bars                              576     
  8       213       21 + 1/3          3 cycles per 16 bars                            512            // Manual incorrectly says 21 + 2/3
  9       20b       20 beats          1 cycle per 5 bars                              480     
  10      183       18 + 2/3          3 cycles per 14 bars                            448     
  11      18b       18 beats          1 cycle per 18 beats (2 cycles per 9 bars)      432     
  12      16b       16 beats          1 cycle per 4 bars                              384     
  13      133       13 + 1/3          3 cycles per 10 bars                            320            // Manual incorrectly says 3 cycles per 4 bars 
  14      12b       12 beats          1 cycle per 12 beats (1 cycle per 3 bars)       288     
  15      102       10 + 2/3          3 cycles per 8 bars                             256     
  16      8b        8 beats           1 cycle per 2 bars                              192     
  17      6b        6 beats           1 cycle per 6 beats (2 cycles per 3 bars)       144     
  18      5b3       5 + 1/3           3 cycles per 4 bars                             128     
  19      4b        4 beats           1 cycle per 1 bar                               96      
  20      3b        3 beats           1 cycle per 3 beats (4 cycles per 3 bars)       72      
  21      8x3       2 + 2/3           3 cycles per 2 bars                             64      
  22      2n        2nd               2 cycles per 1 bar                              48      
  23      4d        4th dotted        2 cycles per 3 beats (8 cycles per 3 bars)      36      
  24      4x3       1 + 1/3           3 cycles per 1 bar                              32      
  25      4n        4th               4 cycles per 1 bar                              24      
  26      8d        8th dotted        4 cycles per 3 beats (16 cycles per 3 bars)     18      
  27      4t        4th triplet       6 cycles per 1 bar                              16      
  28      8n        8th               8 cycles per 1 bar                              12      
  29      16d       16th dotted       8 cycles per 3 beats (32 cycles per 3 bars)     9       
  30      8t        8th triplet       12 cycles per 1 bar                             8       
  31      16n       16th              16 cycles per 1 bar                             6       
  32      16t       16th triplet      24 cycles per 1 bar                             4       
  33      32n       32nd              32 cycles per 1 bar                             3       
  34      32t       32nd triplet      48 cycles per 1 bar                             2




  TABLE 2
  Osc Pulsewidth Modulation Values 
  (127 total, but spread through the range 0...127, because 63 is missing -- presumably it's "-0"! )

  Values 0...62                   [63 values in total]     These are simply the negation of the positive values
  -90 
  -88 -86 -85 -84 -82 -80 
  -78 -76 -75 -74 -73 -71 -70 
  -68 -66 -65 -64 -63 -61 -60 
  -59 -57 -56 -55 -53 -51 -50 
  -49 -47 -46 -45 -44 -42 -40 
  -39 -38 -36 -35 -34 -32 -30 
  -28 -26 -25 -24 -23 -22 -20 
  -19 -17 -16 -15 -14 -12 -10
  -9  -7  -5  -4  -3  -2  -1

  Value 64
  0    
 
  Values 65...127                 [63 values in total]
  1   2   3   4   5   7   9  
  10  12  14  15  16  17  19  
  20  22  23  24  25  26  28  
  30  32  34  35  36  38  39  
  40  42  44  45  46  47  49  
  50  51  53  55  56  57  59
  60  61  63  64  65  66  68
  70  71  73  74  75  76  78
  80  82  84  85  86  88
  90
 
 
 
 
  TABLE 3
  Osc Manual Pulsewidth Values

  PW        Sysex Value
  ---------------------
  0         0
  1         1
  2         3
  3         5
  4         7
  5         8
  6         9
  7         10
  8         12
  9         13
  10        15
  11        16
  12        17
  13        18
  14        20
  15        22
  16        23
  17        24
  18        25
  19        27
  20        29
  21        30
  22        31
  23        32
  24        34
  25        35
  26        37
  27        38
  28        39
  29        41
  30        42
  31        44
  32        45
  33        46
  34        48
  35        50
  36        52
  37        53
  38        54
  39        55
  40        57
  41        59
  42        60
  43        61
  44        63
  45        64
  46        66
  47        67
  48        68
  49        69
  50        71
  51        72
  52        73
  53        75
  54        76
  55        77
  56        79
  57        81
  58        82
  59        84
  60        85
  61        86
  62        88
  63        89
  64        91
  65        92
  66        93
  67        95
  68        96
  69        97
  70        99
  71        101
  72        102
  73        103
  74        104
  75        106
  76        107
  77        108
  78        110
  79        111
  80        113
  81        114
  82        115
  83        117
  84        119
  85        120
  86        121
  87        122
  88        124
  89        126
  90        127



  TABLE 4
  Coarse Tuning Values
  This is a total mess, and Novation should be absolutely ashamed.  In short, there are
  tuning values missing for no good reason, and there are duplicates in strange places,
  and places where duplicates ought to be (for consistency) but aren't.  It's like it 
  was generated by a monkey.
  

  Sysex   Display Value
  ---------------------
  0       -12.0
  1               -11.9
  2               -11.8
  3               -11.7
  4               -11.6
  5               -11.5
  6               -11.4
  7               -11.3
  8               -11.2
  9               -11.1
  10              -11.0   // no dup
  11              -10.9
  12              -10.8
  13              -10.7
  14              -10.6
  15              -10.5
  16              -10.4
  17              -10.3
  18              -10.2   // notice -10.1 is missing
  19              -10.0   
  20              -10.0   // dup
  21              -9.9
  22              -9.8
  23              -9.7
  24              -9.6
  25              -9.5
  26              -9.4
  27              -9.3
  28              -9.2
  29              -9.1
  30              -9.0
  31              -9.0    // dup
  32              -8.9
  33              -8.8
  34              -8.7
  35              -8.6
  36              -8.5
  37              -8.4
  38              -8.3
  39              -8.2
  40              -8.1
  41              -8.0    // dup
  42              -8.0
  43              -7.9
  44              -7.8
  45              -7.7
  46              -7.6
  47              -7.5
  48              -7.4
  49              -7.3
  50              -7.2
  51              -7.1
  52              -7.0
  53              -7.0    // dup
  54              -6.8    // notice -6.9 is missing
  55              -6.7
  56              -6.6
  57              -6.5
  58              -6.4
  59              -6.3
  60              -6.2
  61              -6.1
  62              -6.0
  63              -6.0    // dup
  64              -5.9
  65              -5.8
  66              -5.7
  67              -5.6
  68              -5.5
  69              -5.4
  70              -5.3
  71              -5.2
  72              -5.1
  73              -5.0
  74              -5.0    // dup
  75              -4.9
  76              -4.8
  77              -4.7
  78              -4.6
  79              -4.5
  80              -4.4
  81              -4.3
  82              -4.2
  83              -4.1
  84              -4.0    // dup
  85              -4.0
  86              -3.9
  87              -3.8
  88              -3.7
  89              -3.5    // notice that -3.6 is missing
  90              -3.4
  91              -3.3
  92              -3.2
  93              -3.1
  94              -3.0    // dup
  95              -3.0
  96              -2.9
  97              -2.8
  98              -2.7
  99              -2.6
  100             -2.5
  101             -2.4
  102             -2.3
  103             -2.2
  104             -2.1
  105             -2.0
  106             -2.0    // dup
  107             -1.9
  108             -1.8
  109             -1.7
  110             -1.6
  111             -1.5
  112             -1.4
  113             -1.3
  114             -1.2
  115             -1.1
  116             -1.0
  117             -1.0    // dup
  118             -0.9
  119             -0.8
  120             -0.7
  121             -0.6
  122             -0.5
  123             -0.4
  124             -0.3
  125             -0.2
  126             -0.1
  127             0
  128             0               // dup
  129             0.1
  130             0.2
  131             0.3
  132             0.4
  133             0.5
  134             0.6
  135             0.7
  136             0.8
  137             0.9
  138             1.0             // dup
  139             1.0
  140             1.1
  141             1.2
  142             1.3
  143             1.4
  144             1.5
  145             1.6
  146             1.7
  147             1.8
  148             1.9
  149             2.0             // dup
  150             2.0
  151             2.1
  152             2.2
  153             2.3
  154             2.4
  155             2.5
  156             2.6
  157             2.7
  158             2.8
  159             2.9
  160             3.0             // dup
  161             3.0
  162             3.1
  163             3.2
  164             3.3
  165             3.4
  166             3.5
  167             3.6
  168             3.7
  169             3.8
  170             3.9
  171             4.0
  172             4.1             // no dup!
  173             4.2
  174             4.3
  175             4.4
  176             4.5
  177             4.6
  178             4.7
  179             4.8
  180             4.9
  181             5.0     // dip
  182             5.0
  183             5.1
  184             5.2
  185             5.3
  186             5.4
  187             5.5
  188             5.6
  189             5.7
  190             5.8
  191     5.9
  192     6.0             // dup
  193     6.0
  194     6.1
  195     6.2
  196     6.3
  197     6.4
  198     6.5
  199     6.6
  200     6.7
  201     6.8
  202     7.0             // notice that 6.9 is missing
  203     7.0
  204     7.1
  205     7.2
  206     7.3
  207     7.4
  208     7.5
  209     7.6
  210     7.7
  211     7.8
  212     7.9
  213     8.0             // dup
  214     8.0
  215     8.1
  216     8.2
  217     8.3
  218     8.4
  219     8.5
  220     8.6
  221     8.7
  222     8.8
  223     8.9
  224     9.0             // dup
  225     9.0
  226     9.1
  227     9.2
  228     9.3
  229     9.4
  230     9.5
  231     9.6
  232     9.7
  233     9.8
  234     9.9     
  235     10.0    // dup
  236     10.0
  237     10.1
  238     10.2
  239     10.4    // notice that 10.3 is missing
  240     10.5
  241     10.6
  242     10.7
  243     10.8
  244     10.9
  245     11.0    // no dup!
  246     11.1
  247     11.2
  248     11.3
  249     11.4
  250     11.5
  251     11.6
  252     11.7
  253     11.8
  254     11.9
  255     12.0



*/
 
