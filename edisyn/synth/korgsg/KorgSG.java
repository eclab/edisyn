/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.korgsg;

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
   A patch editor for the Korg SG RACK and the Korg SG X PRO.
        
   @author Sean Luke
*/

public class KorgSG extends Synth
    {
    /// Various collections of parameter names for pop-up menus
    
    
    // It seems to me that the right ordering of these would be COLUMN order, not ROW order
    /*
      Concert, Studio, DancePiano, Dyna-Stage, Wurly EP, FM EP 1, Piano & EP, Funkamatic, SGX Organ, R&B Organ, Clav, Vibraphone, TheStrings, WhisperVox, SynthFlute, Acoustic,
      Bright, Rock Piano, Chorused, Classic EP, Dyno Bell, Wave EP 1, PF&Strings, FM&Analog, Velo \"B\", CX-3 Organ, PhaserClav, Bellphonic, Symphonic, Slow Waves, SynthBrass, Fretless,
      Classic, Jazz Piano, Mix Piano, Stage Bell, FM EP 2, Motion EP, MIDI Grant, EP&Strings, Perc Organ, Gospel Org, Mutronics, Crystaline, PadStrings, BreathyVox, Synth Air, FingerBass,
      Dynamic, Ballad, StagePiano, Stage Time, FM EP 3, Wave EP 2, Power Keys, EP Magic, Full Organ, Pipe Organ, Clavitar, BellString, StringsL&R, Voices, Synth Horn, Synth Bass
    */
    
    static final String[] PROGRAMS = new String[] { "Concert", "Studio", "DancePiano", "Dyna-Stage", "Wurly EP", "FM EP 1", "Piano & EP", "Funkamatic", "SGX Organ", "R&B Organ", "Clav", "Vibraphone", "TheStrings", "WhisperVox", "SynthFlute", "Acoustic",
                                                    "Bright", "Rock Piano", "Chorused", "Classic EP", "Dyno Bell", "Wave EP 1", "PF&Strings", "FM&Analog", "Velo \"B\"", "CX-3 Organ", "PhaserClav", "Bellphonic", "Symphonic", "Slow Waves", "SynthBrass", "Fretless",
                                                    "Classic", "Jazz Piano", "Mix Piano", "Stage Bell", "FM EP 2", "Motion EP", "MIDI Grant", "EP&Strings", "Perc Organ", "Gospel Org", "Mutronics", "Crystaline", "PadStrings", "BreathyVox", "Synth Air", "FingerBass",
                                                    "Dynamic", "Ballad", "StagePiano", "Stage Time", "FM EP 3", "Wave EP 2", "Power Keys", "EP Magic", "Full Organ", "Pipe Organ", "Clavitar", "BellString", "StringsL&R", "Voices", "Synth Horn", "Synth Bass" };
    static final String[] BANKS = new String[] { "A", "B", "C", "D" };
    static final String[] SCALE_TYPES = new String[] { "Equal Temperment", "Pure Major", "Pure Minor", "Pythagoras", "Werckmeister", "Kirnberger", "Stretch" };
    static final String[] EFFECT_TYPES_1 = new String[] { "None", "Reverb", "Early Reflection", "Stereo Delay", "Stereo Chorus", "Stereo Flanger", "Overdrive", "Stereo Phaser", "Rotary Speaker", "Auto Pan", "Wah", "Flanger-Delay", "Hyper Enhancer" };
    static final String[] EFFECT_TYPES_2 = new String[] { "None", "Reverb", "Early Reflection", "Stereo Delay", "Stereo Chorus", "Stereo Flanger", "Overdrive", "Stereo Phaser", "Rotary Speaker", "Auto Pan", "Wah", "Flanger-Delay" };
    static final String[] SCALE_KEYS = new String[] { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
    static final String[] CHORUS_LFO_TYPES = new String[] { "Sine", "Triangle" };
    static final String[] ROTARY_MOD_SOURCES = new String[] { "None", "[ProX] Wheel 1", "[ProX] Wheel 2", "[ProX] Slider 1", "[ProX] Slider 2", "[ProX] Slider 3", "[ProX] Slider 4", "[ProX] Pedal", "[ProX] Pedal SW", "Damper", "Ctrl #12", "[Rack] Ctrl #13", "[Rack] JS(+Y) #01", "[Rack] JS(-Y) #02", "[Rack] Aftertouch"};
        
    static final String[] PROGRAMS_REARRANGED;
    static
        {
        int x = 0;
        String[] pg = new String[PROGRAMS.length];
        for(int i = 0; i < PROGRAMS.length / 4; i++)
            {
            for(int j = 0; j < 4; j++)
                {
                int _i = i + 1;
                pg[x++] = BANKS[j] + (_i < 9 ? "0" + _i : _i)  + ": " + PROGRAMS[j * PROGRAMS.length/4 + i];
                }
            }
        PROGRAMS_REARRANGED = pg;
        }
            


    public KorgSG()
        {
        JComponent soundPanel = new SynthPanel(this);
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        hbox.addLast(addMain(Style.COLOR_A()));
        vbox.add(hbox);
        hbox = new HBox();
        hbox.add(addScale(Style.COLOR_A()));
        hbox.addLast(addEnvelope(Style.COLOR_A()));
        vbox.add(hbox);
        vbox.add(addEffect(1, Style.COLOR_B()));
        vbox.add(addEffect(2, Style.COLOR_B()));

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Program", soundPanel);
                
        model.set("name", "Init");
        model.set("number", 0);
        model.set("bank", 0);
        
        loadDefaults();
        }
                
    
    public String getDefaultResourceFileName() { return "KorgSG.init"; }
    public String getHTMLResourceFileName() { return "KorgSG.html"; }
                
                
              
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
        
        hbox.add(Strut.makeHorizontalStrut(60));

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
            if (chars[i] < 32 || chars[i] > 127)
                chars[i] = ' ';
            }
        return new String(chars);
        }

              
    public JComponent addMain(Color color)
        {
        Category category  = new Category(this, "Main", color);
                        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        // I honestly have no idea what this is.  It appears to have no effect.  So I am hiding it.
        params = PROGRAMS_REARRANGED;
        comp = new Chooser("Original Program", this, "program", params);
        //        vbox.add(comp);
        model.setStatus("program", Model.STATUS_IMMUTABLE);

        comp = new CheckBox("Piano-Style Damp", this, "dampmode");
        vbox.add(comp);
        
        comp = new CheckBox("Piano-Style High Damp", this, "highnotesdamp");
        ((CheckBox)comp).addToWidth(1);
        vbox.add(comp);

        hbox.add(vbox);

        comp = new LabelledDial("Brightness", this, "brightness", color, -99, 99)
            {
            public int getDefaultValue() { return 99; }
            };
        hbox.add(comp);

        comp = new LabelledDial("Key Touch", this, "velocity", color, -99, 99)
            {
            public int getDefaultValue() { return 99; }
            };
        ((LabelledDial)comp).addAdditionalLabel("(Velocity)");
        hbox.add(comp);

        comp = new LabelledDial("Pitch Bend", this, "pitchbendrange", color, -12, 12)
            {
            public boolean isSymmetric() { return true; }
            };
        ((LabelledDial)comp).addAdditionalLabel("Range");
        hbox.add(comp);

        // we need to add a big spacer here so were as wide as the widest effect (Flanger-Delay)
        // otherwise we get scrollbars.  Three dials wide should be enough.
                
        hbox.add(Strut.makeStrut(comp));
        hbox.add(Strut.makeStrut(comp));
        hbox.addLast(Strut.makeStrut(comp));

        category.add(hbox);
        return category;
        }
        
    public JComponent addScale(Color color)
        {
        Category category  = new Category(this, "Scale", color);
                        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        params = SCALE_TYPES;
        comp = new Chooser("Scale", this, "scaletype", params);
        vbox = new VBox();
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Scale Key", this, "scalekey", color, 0, 11)
            {
            public String map(int val)
                {
                return SCALE_KEYS[val];
                }
            };
        hbox.add(comp);

        category.add(hbox);
        return category;
        }
        

    public JComponent addEnvelope(Color color)
        {
        Category category  = new Category(this, "Envelope", color);
                        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        comp = new LabelledDial("Level", this, "level", color, -99, 99);
        hbox.add(comp);

        comp = new LabelledDial("Attack", this, "attacktime", color, -99, 99);
        hbox.add(comp);

        comp = new LabelledDial("Decay", this, "decaytime", color, -99, 99);
        hbox.add(comp);

        comp = new LabelledDial("Release", this, "releasetime", color, -99, 99);
        hbox.add(comp);

        // AD[S]R
        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "attacktime", "decaytime", null, "releasetime" },
            new String[] { null, "level", "level", "level", null },
            new double[] { 0, 0.25 / 198, 0.25 / 198, 0.25, 0.25 / 198 },
            new double[] { 0, 1.0 / 198, 0.5 / 198, 0.5 / 198, 0 })
            {
            public void postProcess(double[] xs, double[] ys)
                {
                // because we use +/-99 for our envelope values just
                // like the SG, we need to postprocess this so that
                // we don't have negative envelope coordinates
                xs[1] += 0.125;
                xs[2] += 0.125;
                xs[4] += 0.125;
                ys[1] += 0.5;                           
                ys[2] += 0.25;                          
                ys[3] += 0.25;                          
                }
            };
        hbox.addLast(comp);
        
        category.add(hbox);
        return category;
        }


    //// EFFECTS
        
    // Various effect types
    public static final int NONE = 0;
    public static final int REVERB = 1;
    public static final int EARLY_REFLECTION = 2;
    public static final int STEREO_DELAY = 3;
    public static final int STEREO_CHORUS = 4;
    public static final int STEREO_FLANGER = 5;
    public static final int OVERDRIVE = 6;
    public static final int STEREO_PHASER = 7;
    public static final int ROTARY_SPEAKER = 8;
    public static final int AUTO_PAN = 9;
    public static final int WAH = 10;
    public static final int FLANGER_DELAY = 11;
    public static final int HYPER_ENHANCER = 12;







    public void addDepth(final int effect, Color color, HBox hbox)
        {
        JComponent comp = new LabelledDial("Depth", this, "effect" + effect + "ldepth", color, 0, 100)
            {
            public String map(int val)      
                {
                if (val == 0) return "Dry";
                else if (val == 100) return "Wet";
                else return "" + val + "%";
                }
                                
            public int reviseToAltValue(int val)
                {
                model.setBounded("effect" + effect + "rdepth", val);
                return val;
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("(Left)");
        // not sure why, but later effects aren't updating the first time.  So I force it here
        ((LabelledDial)comp).update("effect" + effect + "ldepth", model);
        hbox.add(comp);

        comp = new LabelledDial("Depth", this, "effect" + effect + "rdepth", color, 0, 100)
            {
            public String map(int val)      
                {
                if (val == 0) return "Dry";
                else if (val == 100) return "Wet";
                else return "" + val + "%";
                }

            public int reviseToAltValue(int val)
                {
                model.setBounded("effect" + effect + "ldepth", val);
                return val;
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("(Right)");
        // not sure why, but later effects aren't updating the first time.  So I force it here
        ((LabelledDial)comp).update("effect" + effect + "rdepth", model);
        hbox.add(comp);
        }

    public static final int EFFECT_NONE = 0;
    public static final int EFFECT_REVERB = 1;
    public static final int EFFECT_EARLY_RELFECTION = 2;
    public static final int EFFECT_STEREO_DELAY = 3;
    public static final int EFFECT_STEREO_CHORUS = 4;
    public static final int EFFECT_FLANGER = 5;
    public static final int EFFECT_OVERDRIVE = 6;
    public static final int EFFECT_STEREO_PHASER = 7;
    public static final int EFFECT_ROTARY_SPEAKER = 8;
    public static final int EFFECT_AUTO_PAN = 9;
    public static final int EFFECT_WAH = 10;
    public static final int EFFECT_FLANGER_DELAY = 11;
    public static final int EFFECT_HYPER_ENHANCER = 12;

    public JComponent addEffect(int effect, Color color)
        {
        JComponent comp;
        String[] params;
        
        final HBox[] effects = new HBox[effect == 1 ? 13 : 12];
        
        // NONE
        effects[EFFECT_NONE] = new HBox();
        
        // REVERB
        effects[REVERB] = new HBox();
        addDepth(effect, color, effects[REVERB]);
                                
        comp = new LabelledDial("Reverb Time", this, "effect" + effect + "reverb" + "time", color, 0, 97)
            {
            public String map(int val)
                {
                return String.format("%3.1f", val / 10.0);
                }
            };
        effects[REVERB].add(comp);

        comp = new LabelledDial("High", this, "effect" + effect + "reverb" + "highdamp", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("Damp");
        effects[REVERB].add(comp);

        comp = new LabelledDial("Pre Delay", this, "effect" + effect + "reverb" + "predelay", color, 0, 200);
        effects[REVERB].add(comp);

        comp = new LabelledDial("Early Reflection", this, "effect" + effect + "reverb" + "erlevel", color, 0, 99);
        effects[REVERB].add(comp);

        comp = new LabelledDial(" EQ High ", this, "effect" + effect + "reverb" + "eqhigh", color, -12, 12)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val) { return "" + val + "dB"; }
            };
        effects[REVERB].add(comp);

        comp = new LabelledDial(" EQ Low ", this, "effect" + effect + "reverb" + "eqlow", color, -12, 12)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val) { return "" + val + "dB"; }
            };
        effects[REVERB].add(comp);


        // EARLY REFLECTION
        effects[EFFECT_EARLY_RELFECTION] = new HBox();
        addDepth(effect, color, effects[EFFECT_EARLY_RELFECTION]);
                                
        comp = new LabelledDial("Early Reflection", this, "effect" + effect + "earlyreflection" + "ertime", color, 0, 70)
            {
            public String map(int val)
                {
                return "" + (val * 10 + 100);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Time");
        effects[EFFECT_EARLY_RELFECTION].add(comp);

        comp = new LabelledDial("Pre Delay", this, "effect" + effect + "earlyreflection" + "predelay", color, 0, 200);
        effects[EFFECT_EARLY_RELFECTION].add(comp);

        comp = new LabelledDial(" EQ High ", this, "effect" + effect + "earlyreflection" + "eqhigh", color, -12, 12)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val) { return "" + val + "dB"; }
            };
        effects[EFFECT_EARLY_RELFECTION].add(comp);

        comp = new LabelledDial(" EQ Low ", this, "effect" + effect + "earlyreflection" + "eqlow", color, -12, 12)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val) { return "" + val + "dB"; }
            };
        effects[EFFECT_EARLY_RELFECTION].add(comp);



        // STEREO DELAY
        effects[EFFECT_STEREO_DELAY] = new HBox();
        addDepth(effect, color, effects[EFFECT_STEREO_DELAY]);
                                
        comp = new LabelledDial("Delay Time", this, "effect" + effect + "stereodelay" + "delaytimeleft", color, 0, 500)
            {
            public int reviseToAltValue(int val)
                {
                model.setBounded("effect" + effect + "stereodelay" + "delaytimeright", val);
                return val;
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Left");
        effects[EFFECT_STEREO_DELAY].add(comp);

        comp = new LabelledDial("Delay Time", this, "effect" + effect + "stereodelay" + "delaytimeright", color, 0, 500)
            {
            public int reviseToAltValue(int val)
                {
                model.setBounded("effect" + effect + "stereodelay" + "delaytimeleft", val);
                return val;
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Right");
        effects[EFFECT_STEREO_DELAY].add(comp);

        comp = new LabelledDial("Feedback", this, "effect" + effect + "stereodelay" + "feedback", color, -99, 99)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val) { return "" + val + "%"; }
            };
        effects[EFFECT_STEREO_DELAY].add(comp);

        comp = new LabelledDial("High", this, "effect" + effect + "stereodelay" + "highdamp", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("Damp");
        effects[EFFECT_STEREO_DELAY].add(comp);

        comp = new LabelledDial(" EQ High ", this, "effect" + effect + "stereodelay" + "eqhigh", color, -12, 12)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val) { return "" + val + "dB"; }
            };
        effects[EFFECT_STEREO_DELAY].add(comp);

        comp = new LabelledDial(" EQ Low ", this, "effect" + effect + "stereodelay" + "eqlow", color, -12, 12)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val) { return "" + val + "dB"; }
            };
        effects[EFFECT_STEREO_DELAY].add(comp);



        // STEREO CHORUS
        effects[EFFECT_STEREO_CHORUS] = new HBox();
        addDepth(effect, color, effects[EFFECT_STEREO_CHORUS]);
                                
        comp = new LabelledDial("Mod Depth", this, "effect" + effect + "stereochorus" + "moddepth", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("");
        effects[EFFECT_STEREO_CHORUS].add(comp);

        comp = new LabelledDial("Mod Speed", this, "effect" + effect + "stereochorus" + "modspeed", color, 0, 216)
            {
            public String map(int val)
                {
                if (val < 100)
                    return String.format("%4.2f", ( val / 100.0 * 3 + 0.03));
                else if (val < 200)
                    return String.format("%3.1f", ((val - 100) * 0.1 + 3.1));
                else 
                    return String.format("%4.1f", ((val - 200) + 14));
                }
            };
        effects[EFFECT_STEREO_CHORUS].add(comp);


        comp = new LabelledDial("Delay Time", this, "effect" + effect + "stereochorus" + "delaytime", color, 0, 200);
        ((LabelledDial)comp).addAdditionalLabel("");
        effects[EFFECT_STEREO_CHORUS].add(comp);

        comp = new LabelledDial(" EQ High ", this, "effect" + effect + "stereochorus" + "eqhigh", color, -12, 12)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val) { return "" + val + "dB"; }
            };
        effects[EFFECT_STEREO_CHORUS].add(comp);

        comp = new LabelledDial(" EQ Low ", this, "effect" + effect + "stereochorus" + "eqlow", color, -12, 12)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val) { return "" + val + "dB"; }
            };
        effects[EFFECT_STEREO_CHORUS].add(comp);

        /// Note: the actual values are, for some reason, 2 and 3, not 0 and 1
        params = CHORUS_LFO_TYPES;
        comp = new Chooser("LFO", this, "effect" + effect + "stereochorus" + "mgshape", params);
        VBox vbox = new VBox();
        vbox.add(comp);
        effects[EFFECT_STEREO_CHORUS].add(vbox);


        // STEREO FLANGER
        effects[EFFECT_FLANGER] = new HBox();
        addDepth(effect, color, effects[EFFECT_FLANGER]);
                                
        comp = new LabelledDial("Delay Time", this, "effect" + effect + "stereoflanger" + "delaytime", color, 0, 200)
            {
            public String map(int val)
                {
                return String.format("%3.1f", val / 10.0);
                }
            };
        effects[EFFECT_FLANGER].add(comp);

        comp = new LabelledDial("Mod Depth", this, "effect" + effect + "stereoflanger" + "moddepth", color, 0, 99);
        effects[EFFECT_FLANGER].add(comp);

        comp = new LabelledDial("Mod Speed", this, "effect" + effect + "stereoflanger" + "modspeed", color, 1, 99);
        effects[EFFECT_FLANGER].add(comp);

        comp = new LabelledDial("Resonance", this, "effect" + effect + "stereoflanger" + "resonance", color, -99, 99)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val) { return "" + val + "%"; }
            };
        effects[EFFECT_FLANGER].add(comp);

        comp = new LabelledDial(" EQ High ", this, "effect" + effect + "stereoflanger" + "eqhigh", color, -12, 12)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val) { return "" + val + "dB"; }
            };
        effects[EFFECT_FLANGER].add(comp);

        comp = new LabelledDial(" EQ Low ", this, "effect" + effect + "stereoflanger" + "eqlow", color, -12, 12)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val) { return "" + val + "dB"; }
            };
        ((LabelledDial)comp).addAdditionalLabel(" ");  // So we're the same height as the others
        effects[EFFECT_FLANGER].add(comp);


        // OVERDRIVE
        effects[OVERDRIVE] = new HBox();
        addDepth(effect, color, effects[OVERDRIVE]);
                                
        comp = new LabelledDial("Drive", this, "effect" + effect + "overdrive" + "drive", color, 1, 111);
        effects[OVERDRIVE].add(comp);

        comp = new LabelledDial("Hot Spot", this, "effect" + effect + "overdrive" + "hotspot", color, 0, 99);
        effects[OVERDRIVE].add(comp);

        comp = new LabelledDial("Resonance", this, "effect" + effect + "overdrive" + "resonance", color, 0, 99);
        effects[OVERDRIVE].add(comp);

        comp = new LabelledDial("Out Level", this, "effect" + effect + "overdrive" + "outlevel", color, 0, 99);
        effects[OVERDRIVE].add(comp);

        comp = new LabelledDial(" EQ High ", this, "effect" + effect + "overdrive" + "eqhigh", color, -12, 12)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val) { return "" + val + "dB"; }
            };
        effects[OVERDRIVE].add(comp);

        comp = new LabelledDial(" EQ Low ", this, "effect" + effect + "overdrive" + "eqlow", color, -12, 12)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val) { return "" + val + "dB"; }
            };
        ((LabelledDial)comp).addAdditionalLabel(" ");  // So we're the same height as the others
        effects[OVERDRIVE].add(comp);


        // STEREO PHASER
        effects[STEREO_PHASER] = new HBox();
        addDepth(effect, color, effects[STEREO_PHASER]);
                                
        comp = new LabelledDial("Mod Depth", this, "effect" + effect + "stereophaser" + "moddepth", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("");
        effects[STEREO_PHASER].add(comp);

        comp = new LabelledDial("Mod Speed", this, "effect" + effect + "stereophaser" + "modspeed", color, 0, 216)
            {
            public String map(int val)
                {
                if (val < 100)
                    return String.format("%4.2f", ( val / 100.0 * 3 + 0.03));
                else if (val < 200)
                    return String.format("%3.1f", ((val - 100) * 0.1 + 3.1));
                else 
                    return String.format("%4.1f", ((val - 200) + 14));
                }
            };
        effects[STEREO_PHASER].add(comp);

        comp = new LabelledDial("Feedback", this, "effect" + effect + "stereophaser" + "feedback", color, -99, 99)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val) { return "" + val + "%"; }
            };
        effects[STEREO_PHASER].add(comp);

        comp = new LabelledDial("Manual", this, "effect" + effect + "stereophaser" + "manual", color, 0, 99);
        effects[STEREO_PHASER].add(comp);

        /// Note: the actual values are, for some reason, 2 and 3, not 0 and 1
        params = CHORUS_LFO_TYPES;
        comp = new Chooser("LFO", this, "effect" + effect + "stereophaser" + "mgshape", params);
        vbox = new VBox();
        vbox.add(comp);
        effects[STEREO_PHASER].add(vbox);


        // ROTARY SPEAKER
        effects[ROTARY_SPEAKER] = new HBox();
        addDepth(effect, color, effects[ROTARY_SPEAKER]);
                                
        comp = new LabelledDial("Vibrato", this, "effect" + effect + "rotaryspeaker" + "vibratodepth", color, 0, 15);
        ((LabelledDial)comp).addAdditionalLabel("Depth");
        effects[ROTARY_SPEAKER].add(comp);

        comp = new LabelledDial("Acceleration", this, "effect" + effect + "rotaryspeaker" + "acceleration", color, 1, 15);
        effects[ROTARY_SPEAKER].add(comp);

        comp = new LabelledDial("Slow Speed", this, "effect" + effect + "rotaryspeaker" + "slowspeed", color, 1, 99);
        effects[ROTARY_SPEAKER].add(comp);

        comp = new LabelledDial("Fast Speed", this, "effect" + effect + "rotaryspeaker" + "fastspeed", color, 1, 99);
        ((LabelledDial)comp).addAdditionalLabel(" ");  // So we're the same height as the others
        effects[ROTARY_SPEAKER].add(comp);

        params = ROTARY_MOD_SOURCES;
        comp = new Chooser("Dynamic Mod Source", this, "effect" + effect + "rotaryspeaker" + "dynamicmodsource", params);
        vbox = new VBox();
        vbox.add(comp);
        effects[ROTARY_SPEAKER].add(vbox);


        // AUTO PAN
        effects[AUTO_PAN] = new HBox();
        addDepth(effect, color, effects[AUTO_PAN]);
                                
        comp = new LabelledDial("Depth", this, "effect" + effect + "autopan" + "depth", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel(" ");  // So we're the same height as the others
        effects[AUTO_PAN].add(comp);

        comp = new LabelledDial("Speed", this, "effect" + effect + "autopan" + "speed", color, 0, 216)
            {
            public String map(int val)
                {
                if (val < 100)
                    return String.format("%4.2f", ( val / 100.0 * 3 + 0.03));
                else if (val < 200)
                    return String.format("%3.1f", ((val - 100) * 0.1 + 3.1));
                else 
                    return String.format("%4.1f", ((val - 200) + 14));
                }
            };
        effects[AUTO_PAN].add(comp);

        comp = new LabelledDial("Mod Shape", this, "effect" + effect + "autopan" + "shape", color, -99, 99)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val) { return "" + val + "%"; }
            };
        effects[AUTO_PAN].add(comp);

        comp = new LabelledDial(" EQ High ", this, "effect" + effect + "autopan" + "eqhigh", color, -12, 12)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val) { return "" + val + "dB"; }
            };
        effects[AUTO_PAN].add(comp);

        comp = new LabelledDial(" EQ Low ", this, "effect" + effect + "autopan" + "eqlow", color, -12, 12)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val) { return "" + val + "dB"; }
            };
        effects[AUTO_PAN].add(comp);

        /// Note: the actual values are, for some reason, 2 and 3, not 0 and 1
        params = CHORUS_LFO_TYPES;
        comp = new Chooser("LFO", this, "effect" + effect + "autopan" + "mgshape", params);
        vbox = new VBox();
        vbox.add(comp);
        effects[AUTO_PAN].add(vbox);


        // WAH
        effects[WAH] = new HBox();
        addDepth(effect, color, effects[WAH]);
                                
        comp = new LabelledDial("Frequency", this, "effect" + effect + "wah" + "frequency", color, 0, 99);
        effects[WAH].add(comp);

        comp = new LabelledDial("Peak Gain", this, "effect" + effect + "wah" + "peakgain", color, -12, 12)

            {
            public boolean isSymmetric() { return true; }
            public String map(int val) { return "" + val + "dB"; }
            };
        effects[WAH].add(comp);

        comp = new LabelledDial("Peak Width", this, "effect" + effect + "wah" + "peakwidth", color, 0, 99);
        effects[WAH].add(comp);

        comp = new LabelledDial("Intensity", this, "effect" + effect + "wah" + "dynamicmodint", color, -15, 15)
            {
            public boolean isSymmetric() { return true; }
            };
        effects[WAH].add(comp);

        params = ROTARY_MOD_SOURCES;
        comp = new Chooser("Source", this, "effect" + effect + "wah" + "dynamicmodsource", params);
        vbox = new VBox();
        vbox.add(comp);
        effects[WAH].add(vbox);


        // FLANGER-DELAY
        effects[FLANGER_DELAY] = new HBox();
        addDepth(effect, color, effects[FLANGER_DELAY]);
                                
        comp = new LabelledDial("Flanger", this, "effect" + effect + "flangerdelay" + "flangerdelaytime", color, 0, 200)
            {
            public String map(int val)
                {
                return String.format("%3.1f", val / 10.0);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Delay Time");
        effects[FLANGER_DELAY].add(comp);

        comp = new LabelledDial("Flanger", this, "effect" + effect + "flangerdelay" + "flangermoddepth", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("Mod Depth");
        effects[FLANGER_DELAY].add(comp);

        comp = new LabelledDial("Flanger", this, "effect" + effect + "flangerdelay" + "flangermodspeed", color, 1, 99);
        ((LabelledDial)comp).addAdditionalLabel("Mod Speed");
        effects[FLANGER_DELAY].add(comp);

        comp = new LabelledDial("Flanger", this, "effect" + effect + "flangerdelay" + "flangerfeedback", color, -99, 99)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val) { return "" + val + "%"; }
            };
        ((LabelledDial)comp).addAdditionalLabel("Feedback");
        effects[FLANGER_DELAY].add(comp);

        comp = new LabelledDial("Delay", this, "effect" + effect + "flangerdelay" + "delaytime", color, 0, 450);
        ((LabelledDial)comp).addAdditionalLabel("Time");
        effects[FLANGER_DELAY].add(comp);

        comp = new LabelledDial("Delay", this, "effect" + effect + "flangerdelay" + "delayfeedback", color, -99, 99)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val) { return "" + val + "%"; }
            };
        ((LabelledDial)comp).addAdditionalLabel("Feedback");
        effects[FLANGER_DELAY].add(comp);

        comp = new LabelledDial("Delay", this, "effect" + effect + "flangerdelay" + "delaylevel", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        effects[FLANGER_DELAY].add(comp);



        // HYPER ENHANCER
        if (effect == 1)
            {
            effects[HYPER_ENHANCER] = new HBox();
                                                                
            comp = new LabelledDial("Depth", this, "effect" + effect + "hyperenhancer" + "ldepth", color, 0, 1)
                {
                public String map(int val)      
                    {
                    if (val == 0) return "Dry";
                    else return "Wet";
                    }
                public int reviseToAltValue(int val)
                    {
                    model.setBounded("effect" + effect + "hyperenhancer" + "rdepth", val);
                    return val;
                    }
                };
            ((LabelledDial)comp).addAdditionalLabel("(Left)");
            effects[HYPER_ENHANCER].add(comp);

            comp = new LabelledDial("Depth", this, "effect" + effect + "hyperenhancer" + "rdepth", color, 0, 1)
                {
                public String map(int val)      
                    {
                    if (val == 0) return "Dry";
                    else return "Wet";
                    }
                public int reviseToAltValue(int val)
                    {
                    model.setBounded("effect" + effect + "hyperenhancer" + "ldepth", val);
                    return val;
                    }
                };
            ((LabelledDial)comp).addAdditionalLabel("(Right)");
            effects[HYPER_ENHANCER].add(comp);

            comp = new LabelledDial("Trim", this, "effect" + effect + "hyperenhancer" + "trim", color, 0, 100);
            effects[HYPER_ENHANCER].add(comp);

            comp = new LabelledDial("Low", this, "effect" + effect + "hyperenhancer" + "lowfrequency", color, 1, 70);
            ((LabelledDial)comp).addAdditionalLabel("Frequency");
            effects[HYPER_ENHANCER].add(comp);

            comp = new LabelledDial("Low", this, "effect" + effect + "hyperenhancer" + "lowblend", color, 0, 100);
            ((LabelledDial)comp).addAdditionalLabel("Blend");
            effects[HYPER_ENHANCER].add(comp);

            comp = new LabelledDial("High", this, "effect" + effect + "hyperenhancer" + "highfrequency", color, 1, 40);
            ((LabelledDial)comp).addAdditionalLabel("Frequency");
            effects[HYPER_ENHANCER].add(comp);

            comp = new LabelledDial("High", this, "effect" + effect + "hyperenhancer" + "highblend", color, 0, 100);
            ((LabelledDial)comp).addAdditionalLabel("Blend");
            effects[HYPER_ENHANCER].add(comp);
            }

        // Force empty to be the same size
        effects[EFFECT_NONE].addLast(Strut.makeStrut(effects));


        final HBox main = new HBox();
        params = (effect == 1 ? EFFECT_TYPES_1 : EFFECT_TYPES_2);
        comp = new Chooser("Type", this, "effect" + effect + "type", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                main.removeLast();
                main.addLast(effects[model.get(key, 0)]);
                main.revalidate();
                main.repaint();
                }
            };
        vbox = new VBox();
        vbox.add(comp);
        main.add(vbox);
                
        Category category = new Category(this, "Effect " + effect, color);
        category.add(main, BorderLayout.CENTER);
        category.makePasteable("effect" + effect);
                
        return category;
        }




    


    public byte EFFECT_TYPE_BYTES[] = new byte[] { 0x00, 0x01, 0x0A, 0x0D, 0x13, 0x19, 0x1F, 0x20, 0x22, 0x23, 0x25, 0x27, 0x30 };
    public int findEffectType(byte b)
        {
        for(int i = 0; i < EFFECT_TYPE_BYTES.length; i++)
            if (EFFECT_TYPE_BYTES[i] == b) 
                return i;
        System.err.println("Warning (KorgSG): unknown effect type byte " + b);
        return 0;
        }
            
    int range(int a)
        {
        while (a > 255) a -= 256;
        while (a < 0) a += 256;
        return a;
        }
        
    // converts all but last byte (F7)
    byte[] convertTo8Bit(byte[] data, int offset)
        {
        // How big?
        int size = (data.length - offset - 1) / 8 * 7;
        if ((data.length - offset - 1) % 8 > 0)
            size += ((data.length - offset - 1) % 8 - 1);           
        byte[] newd = new byte[size];
        
        int j = 0;
        for(int i = offset; i < data.length; i += 8)
            {
            for(int x = 0; x < 7; x++)
                {
                if (j + x < newd.length)
                    newd[j + x] = (byte)(data[i + x + 1] | (byte)(((data[i] >>> x) & 0x1) << 7));
                }
            j += 7;
            }
        return newd;
        }
        
    // converts all bytes
    byte[] convertTo7Bit(byte[] data)
        {
        // How big?
        int size = (data.length) / 7 * 8;
        if (data.length % 7 > 0)
            size += (1 + data.length % 7);
        byte[] newd = new byte[size];
        
        int j = 0;
        for(int i = 0; i < data.length; i+=7)
            {
            for(int x = 0; x < 7; x++)
                {
                if (j + x + 1 < newd.length)
                    {
                    newd[j + x + 1] = (byte)(data[i + x] & 127);
                    // Note that I have do to & 1 because data[i + x] is promoted to an int
                    // first, and then shifted, and that makes a BIG NUMBER which requires
                    // me to mask out the 1.  I hope this isn't the case for other stuff (which
                    // is typically 7-bit).
                    newd[j] = (byte)(newd[j] | (((data[i + x] >>> 7) & 1) << x));
                    }
                }
            j += 8;
            }
        return newd;
        }
        
    public int parse(byte[] data, boolean fromFile)
        {
        // The data is F0, 42, 3[CHANNEL], 4A, 40, ... DATA ..., F7
        
        data = convertTo8Bit(data, 5);
        
        char[] namec = new char[10];
        String name;
        for(int i = 0; i < 10; i++)
            {
            namec[i] = (char)data[i];
            }
        name = new String(namec);
        model.set("name", name);
        
        // this will have to be set entirely custom.  :-(  Stupid Korg.  Really bad sysex.
        
        model.set("program", data[10]);
        model.set("dampmode", data[11] & 1);
        model.set("highnotesdamp", (data[11] & 2) >>> 1);
        model.set("brightness", data[12]);
        model.set("level", data[13]);
        model.set("attacktime", data[14]);
        model.set("decaytime", data[15]);
        model.set("releasetime", data[16]);
        model.set("velocity", data[17]);
        model.set("scaletype", data[18]);
        model.set("scalekey", data[19]);
        model.set("pitchbendrange", data[20]);
        model.set("effect1type", findEffectType(data[21]));             // what a mess
        model.set("effect2type", findEffectType(data[22]));             // what a mess
        
        if (model.get("effect1type", 0) == 12)  // hyperenhancer has a custom effect depth, grrrrrrrr
            {
            model.set("effect1hyperenhancerldepth", data[23] == 0 ? 0 : 100);
            model.set("effect1hyperenhancerrdepth", data[24] == 0 ? 0 : 100);
            model.set("effect1ldepth", 0);
            model.set("effect1rdepth", 0);
            }
        else
            {
            model.set("effect1ldepth", data[23]);
            model.set("effect1rdepth", data[24]);
            model.set("effect1hyperenhancerldepth", 0);
            model.set("effect1hyperenhancerrdepth", 0);
            }
                
        model.set("effect2ldepth", data[25]);
        model.set("effect2rdepth", data[26]);
                
        // skip data slots 32, 33, 34, then...
                
        parseEffects(data, 1, model.get("effect1type", 0));
        parseEffects(data, 2, model.get("effect2type", 0));

        revise();       
        return PARSE_SUCCEEDED;     
        }
    
    void parseEffects(byte[] data, int effect, int effectType)
        {
        int offset = (effect == 1 ? 30 : 40);
        
        // all custom, gagh
        switch (effectType)
            {
            case EFFECT_NONE:
                break;
            case EFFECT_REVERB:
                model.set("effect" + effect + "reverb" + "time", data[offset + 0]);
                model.set("effect" + effect + "reverb" + "highdamp", data[offset + 2]);
                model.set("effect" + effect + "reverb" + "predelay", range(data[offset + 3]));
                model.set("effect" + effect + "reverb" + "erlevel", data[offset + 4]);
                model.set("effect" + effect + "reverb" + "eqhigh", data[offset + 6]);
                model.set("effect" + effect + "reverb" + "eqlow", data[offset + 7]);
                break;
            case EFFECT_EARLY_RELFECTION:
                model.set("effect" + effect + "earlyreflection" + "ertime", data[offset + 0]);
                model.set("effect" + effect + "earlyreflection" + "predelay", range(data[offset + 1]));
                model.set("effect" + effect + "earlyreflection" + "eqhigh", data[offset + 6]);
                model.set("effect" + effect + "earlyreflection" + "eqlow", data[offset + 7]);
                break;
            case EFFECT_STEREO_DELAY:
                model.set("effect" + effect + "stereodelay" + "delaytimel", (data[offset + 1] << 8) | data[offset + 0]);
                model.set("effect" + effect + "stereodelay" + "feedback", data[offset + 2]);
                model.set("effect" + effect + "stereodelay" + "highdamp", data[offset + 3]);
                model.set("effect" + effect + "stereodelay" + "delaytimer", (data[offset + 5] << 8) | data[offset + 4]);
                model.set("effect" + effect + "stereodelay" + "eqhigh", data[offset + 6]);
                model.set("effect" + effect + "stereodelay" + "eqlow", data[offset + 7]);
                break;
            case EFFECT_STEREO_CHORUS:
                model.set("effect" + effect + "stereochorus" + "moddepth", data[offset + 0]);
                model.set("effect" + effect + "stereochorus" + "modspeed", range(data[offset + 1]));
                model.set("effect" + effect + "stereochorus" + "mgshape", (data[offset + 2] == 2 ? 0 : 1));  // is either 02 or 03
                model.set("effect" + effect + "stereochorus" + "delaytime", data[offset + 4]);
                model.set("effect" + effect + "stereochorus" + "eqhigh", data[offset + 6]);
                model.set("effect" + effect + "stereochorus" + "eqlow", data[offset + 7]);
                break;
            case EFFECT_FLANGER:
                model.set("effect" + effect + "stereoflanger" + "delaytime", data[offset + 0]);
                model.set("effect" + effect + "stereoflanger" + "moddepth", data[offset + 1]);
                model.set("effect" + effect + "stereoflanger" + "modspeed", data[offset + 2]);
                model.set("effect" + effect + "stereoflanger" + "resonance", data[offset + 3]);
                model.set("effect" + effect + "stereoflanger" + "eqhigh", data[offset + 6]);
                model.set("effect" + effect + "stereoflanger" + "eqlow", data[offset + 7]);
                break;
            case EFFECT_OVERDRIVE:
                model.set("effect" + effect + "overdrive" + "drive", data[offset + 0]);
                model.set("effect" + effect + "overdrive" + "hotspot", data[offset + 1]);
                model.set("effect" + effect + "overdrive" + "resonance", data[offset + 2]);
                model.set("effect" + effect + "overdrive" + "outlevel", data[offset + 3]);
                model.set("effect" + effect + "overdrive" + "eqhigh", data[offset + 6]);
                model.set("effect" + effect + "overdrive" + "eqlow", data[offset + 7]);
                break;
            case EFFECT_STEREO_PHASER:
                model.set("effect" + effect + "stereophaser" + "moddepth", data[offset + 0]);
                model.set("effect" + effect + "stereophaser" + "modspeed", range(data[offset + 1]));
                model.set("effect" + effect + "stereophaser" + "mgshape", (data[offset + 2] == 2 ? 0 : 1));  // is either 02 or 03
                model.set("effect" + effect + "stereophaser" + "feedback", data[offset + 3]);
                model.set("effect" + effect + "stereophaser" + "manual", data[offset + 4]);
                break;
            case EFFECT_ROTARY_SPEAKER:
                model.set("effect" + effect + "rotaryspeaker" + "vibratodepth", data[offset + 0]);
                model.set("effect" + effect + "rotaryspeaker" + "acceleration", data[offset + 1]);
                model.set("effect" + effect + "rotaryspeaker" + "slowspeed", data[offset + 2]);
                model.set("effect" + effect + "rotaryspeaker" + "fastspeed", data[offset + 3]);
                model.set("effect" + effect + "rotaryspeaker" + "dynamicmodsource", data[offset + 8]);
                break;
            case EFFECT_AUTO_PAN:
                model.set("effect" + effect + "autopan" + "depth", data[offset + 0]);
                model.set("effect" + effect + "autopan" + "speed", range(data[offset + 1]));
                model.set("effect" + effect + "autopan" + "mgshape", (data[offset + 2] == 2 ? 0 : 1));  // is either 02 or 03
                model.set("effect" + effect + "autopan" + "shape", data[offset + 3]);
                model.set("effect" + effect + "autopan" + "eqhigh", data[offset + 6]);
                model.set("effect" + effect + "autopan" + "eqlow", data[offset + 7]);
                break;
            case EFFECT_WAH:
                model.set("effect" + effect + "wah" + "frequency", data[offset + 2]);
                model.set("effect" + effect + "wah" + "peakgain", data[offset + 3]);
                model.set("effect" + effect + "wah" + "peakwidth", data[offset + 4]);
                model.set("effect" + effect + "wah" + "dynamicmodsource", data[offset + 8]);
                model.set("effect" + effect + "wah" + "dynamicmodint", data[offset + 9]);
                break;
            case EFFECT_FLANGER_DELAY:
                model.set("effect" + effect + "flangerdelay" + "flangerdelaytime", data[offset + 0]);
                model.set("effect" + effect + "flangerdelay" + "flangermodspeed", data[offset + 1]);
                model.set("effect" + effect + "flangerdelay" + "flangermoddepth", data[offset + 2]);
                model.set("effect" + effect + "flangerdelay" + "flangerfeedback", data[offset + 3]);
                model.set("effect" + effect + "flangerdelay" + "delaytime", range(data[offset + 4]));
                model.set("effect" + effect + "flangerdelay" + "delayfeedback", data[offset + 5]);
                model.set("effect" + effect + "flangerdelay" + "delaylevel", data[offset + 6]);
                break;
            case EFFECT_HYPER_ENHANCER:
                if (effect != 1)
                    System.err.println("Warning (KorgSG) 2: Effect " + effect + " included hyper enhancer.");
                model.set("effect" + effect + "hyperenhancer" + "trim", data[offset + 0]);
                model.set("effect" + effect + "hyperenhancer" + "lowfreq", data[offset + 1]);
                model.set("effect" + effect + "hyperenhancer" + "lowblend", data[offset + 2]);
                model.set("effect" + effect + "hyperenhancer" + "highfreq", data[offset + 3]);
                model.set("effect" + effect + "hyperenhancer" + "highblend", data[offset + 4]);
                break;
            default: 
                System.err.println("Warning (KorgSG) 2: Effect " + effect + " has an invalid effect type " + effectType);
                break;
            }
        }
        
    void emitEffects(byte[] data, int effect, int effectType)
        {
        int offset = (effect == 1 ? 30 : 40);
        
        // all custom, gagh
        switch (effectType)
            {
            case EFFECT_NONE:
                break;
            case EFFECT_REVERB:
                data[offset + 0] = (byte)model.get("effect" + effect + "reverb" + "time", 0);
                data[offset + 2] = (byte)model.get("effect" + effect + "reverb" + "highdamp", 0);
                data[offset + 3] = (byte)model.get("effect" + effect + "reverb" + "predelay", 0);
                data[offset + 4] = (byte)model.get("effect" + effect + "reverb" + "erlevel", 0);
                data[offset + 6] = (byte)model.get("effect" + effect + "reverb" + "eqhigh", 0);
                data[offset + 7] = (byte)model.get("effect" + effect + "reverb" + "eqlow", 0);
                break;
            case EFFECT_EARLY_RELFECTION:
                data[offset + 0] = (byte)model.get("effect" + effect + "earlyreflection" + "ertime");
                data[offset + 1] = (byte)model.get("effect" + effect + "earlyreflection" + "predelay");
                data[offset + 6] = (byte)model.get("effect" + effect + "earlyreflection" + "eqhigh");
                data[offset + 7] = (byte)model.get("effect" + effect + "earlyreflection" + "eqlow");
                break;
            case EFFECT_STEREO_DELAY:
                data[offset + 0] = (byte)(model.get("effect" + effect + "stereodelay" + "delaytimel") & 255);
                data[offset + 1] = (byte)(model.get("effect" + effect + "stereodelay" + "delaytimel") >>> 8);
                data[offset + 2] = (byte)model.get("effect" + effect + "stereodelay" + "feedback");
                data[offset + 3] = (byte)model.get("effect" + effect + "stereodelay" + "highdamp");
                data[offset + 4] = (byte)(model.get("effect" + effect + "stereodelay" + "delaytimer") & 255);
                data[offset + 5] = (byte)(model.get("effect" + effect + "stereodelay" + "delaytimer") >>> 8);
                data[offset + 6] = (byte)model.get("effect" + effect + "stereodelay" + "eqhigh");
                data[offset + 7] = (byte)model.get("effect" + effect + "stereodelay" + "eqlow");
                break;
            case EFFECT_STEREO_CHORUS:
                data[offset + 0] = (byte)model.get("effect" + effect + "stereochorus" + "moddepth");
                data[offset + 1] = (byte)model.get("effect" + effect + "stereochorus" + "modspeed");
                data[offset + 2] = (byte)(model.get("effect" + effect + "stereochorus" + "mgshape") == 0 ? 2 : 3);
                data[offset + 4] = (byte)model.get("effect" + effect + "stereochorus" + "delaytime");
                data[offset + 6] = (byte)model.get("effect" + effect + "stereochorus" + "eqhigh");
                data[offset + 7] = (byte)model.get("effect" + effect + "stereochorus" + "eqlow");
                break;
            case EFFECT_FLANGER:
                data[offset + 0] = (byte)model.get("effect" + effect + "stereoflanger" + "delaytime");
                data[offset + 1] = (byte)model.get("effect" + effect + "stereoflanger" + "moddepth");
                data[offset + 2] = (byte)model.get("effect" + effect + "stereoflanger" + "modspeed");
                data[offset + 3] = (byte)model.get("effect" + effect + "stereoflanger" + "resonance");
                data[offset + 6] = (byte)model.get("effect" + effect + "stereoflanger" + "eqhigh");
                data[offset + 7] = (byte)model.get("effect" + effect + "stereoflanger" + "eqlow");
                break;
            case EFFECT_OVERDRIVE:
                data[offset + 0] = (byte)model.get("effect" + effect + "overdrive" + "drive");
                data[offset + 1] = (byte)model.get("effect" + effect + "overdrive" + "hotspot");
                data[offset + 2] = (byte)model.get("effect" + effect + "overdrive" + "resonance");
                data[offset + 3] = (byte)model.get("effect" + effect + "overdrive" + "outlevel");
                data[offset + 6] = (byte)model.get("effect" + effect + "overdrive" + "eqhigh");
                data[offset + 7] = (byte)model.get("effect" + effect + "overdrive" + "eqlow");
                break;
            case EFFECT_STEREO_PHASER:
                data[offset + 0] = (byte)model.get("effect" + effect + "stereophaser" + "moddepth");
                data[offset + 1] = (byte)model.get("effect" + effect + "stereophaser" + "modspeed");
                data[offset + 2] = (byte)(model.get("effect" + effect + "stereophaser" + "mgshape") == 0 ? 2 : 3);
                data[offset + 3] = (byte)model.get("effect" + effect + "stereophaser" + "feedback");
                data[offset + 4] = (byte)model.get("effect" + effect + "stereophaser" + "manual");
                break;
            case EFFECT_ROTARY_SPEAKER:
                data[offset + 0] = (byte)model.get("effect" + effect + "rotaryspeaker" + "vibratodepth");
                data[offset + 1] = (byte)model.get("effect" + effect + "rotaryspeaker" + "acceleration");
                data[offset + 2] = (byte)model.get("effect" + effect + "rotaryspeaker" + "slowspeed");
                data[offset + 3] = (byte)model.get("effect" + effect + "rotaryspeaker" + "fastspeed");
                data[offset + 8] = (byte)model.get("effect" + effect + "rotaryspeaker" + "dynamicmodsource");
                break;
            case EFFECT_AUTO_PAN:
                data[offset + 0] = (byte)model.get("effect" + effect + "autopan" + "depth");
                data[offset + 1] = (byte)model.get("effect" + effect + "autopan" + "speed");
                data[offset + 2] = (byte)(model.get("effect" + effect + "autopan" + "mgshape") == 0 ? 2 : 3);
                data[offset + 3] = (byte)model.get("effect" + effect + "autopan" + "shape");
                data[offset + 6] = (byte)model.get("effect" + effect + "autopan" + "eqhigh");
                data[offset + 7] = (byte)model.get("effect" + effect + "autopan" + "eqlow");
                break;
            case EFFECT_WAH:
                data[offset + 2] = (byte)model.get("effect" + effect + "wah" + "frequency");
                data[offset + 3] = (byte)model.get("effect" + effect + "wah" + "peakgain");
                data[offset + 4] = (byte)model.get("effect" + effect + "wah" + "peakwidth");
                data[offset + 8] = (byte)model.get("effect" + effect + "wah" + "dynamicmodsource");
                data[offset + 9] = (byte)model.get("effect" + effect + "wah" + "dynamicmodint");
                break;
            case EFFECT_FLANGER_DELAY:
                data[offset + 0] = (byte)model.get("effect" + effect + "flangerdelay" + "flangerdelaytime");
                data[offset + 1] = (byte)model.get("effect" + effect + "flangerdelay" + "flangermodspeed");
                data[offset + 2] = (byte)model.get("effect" + effect + "flangerdelay" + "flangermoddepth");
                data[offset + 3] = (byte)model.get("effect" + effect + "flangerdelay" + "flangerfeedback");
                data[offset + 4] = (byte)model.get("effect" + effect + "flangerdelay" + "delaytime");
                data[offset + 5] = (byte)model.get("effect" + effect + "flangerdelay" + "delayfeedback");
                data[offset + 6] = (byte)model.get("effect" + effect + "flangerdelay" + "delaylevel");
                break;
            case EFFECT_HYPER_ENHANCER:
                if (effect != 1)
                    System.err.println("Warning (KorgSG): Effect " + effect + " included hyper enhancer.");
                data[offset + 0] = (byte)model.get("effect" + effect + "hyperenhancer" + "trim");
                data[offset + 1] = (byte)model.get("effect" + effect + "hyperenhancer" + "lowfreq");
                data[offset + 2] = (byte)model.get("effect" + effect + "hyperenhancer" + "lowblend");
                data[offset + 3] = (byte)model.get("effect" + effect + "hyperenhancer" + "highfreq");
                data[offset + 4] = (byte)model.get("effect" + effect + "hyperenhancer" + "highblend");
                break;
            default: 
                System.err.println("Warning (KorgSG): Effect " + effect + " has an invalid effect type " + effectType);
                break;
            }
        }



    public Object[] emitAll(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        if (tempModel == null)
            tempModel = getModel();
            
        Object[] d = new Object[1];
        if (!toWorkingMemory && !toFile)
            {
            // we also have to write it out with a transfer.
            d = new Object[2];
            }
        
        byte[] data = new byte[50];
        
        // LOAD DATA HERE
        
        String name = model.get("name", "Untitled");
        char[] namec = new char[10];
        char[] b = name.toCharArray();
        System.arraycopy(b, 0, namec, 0, b.length);
        for(int i = 0; i < 10; i++)
            {
            data[i] = (byte)(namec[i] & 127);
            }

        data[10] = (byte)model.get("program", 0);
        data[11] = (byte)((model.get("dampmode", 0) & 1) | ((model.get("highnotesdamp", 0) << 1) & 2));
        data[12] = (byte)model.get("brightness", 0);
        data[13] = (byte)model.get("level", 0);
        data[14] = (byte)model.get("attacktime", 0);
        data[15] = (byte)model.get("decaytime", 0);
        data[16] = (byte)model.get("releasetime", 0);
        data[17] = (byte)model.get("velocity", 0);
        data[18] = (byte)model.get("scaletype", 0);
        data[19] = (byte)model.get("scalekey", 0);
        data[20] = (byte)model.get("pitchbendrange", 0);
        data[21] = EFFECT_TYPE_BYTES[model.get("effect1type", 0)];
        data[22] = EFFECT_TYPE_BYTES[model.get("effect2type", 0)];
                
        if (model.get("effect1type", 0) == 12)  // hyperenhancer has a custom effect depth, grrrrrrrr
            {
            data[23] = (byte)model.get("effect1hyperenhancerldepth", 0);
            data[24] = (byte)model.get("effect1hyperenhancerrdepth", 0);
            }
        else
            {
            data[23] = (byte)model.get("effect1ldepth", 0);
            data[24] = (byte)model.get("effect1rdepth", 0);
            }

        data[25] = (byte)model.get("effect2ldepth", 0);
        data[26] = (byte)model.get("effect2rdepth", 0);

        data[27] = (byte)0x41;          // It would appear that although Korg says this is always 0x41, in fact the SG always returns 0x65.  Appears to not matter.
        data[28] = (byte)0x01;
        data[29] = (byte)0x0F;
        
        // skip data slots 32, 33, 34, then...
                
        emitEffects(data, 1, model.get("effect1type", 0));
        emitEffects(data, 2, model.get("effect2type", 0));
        
        // Convert 
        
        byte[] data2 = convertTo7Bit(data);
        data = new byte[6 + data2.length];  // resetting data to new value
        data[0] = (byte)0xF0;
        data[1] = (byte)0x42;
        data[2] = (byte)(48 + getChannelOut());
        data[3] = (byte)0x4A;
        data[4] = (byte)0x40;
        data[data.length - 1] = (byte)0xF7;
        System.arraycopy(data2, 0, data, 5, data2.length);
        d[0] = data;
        
        if (!toWorkingMemory && !toFile)
            {
            // The SG cannot write to a patch directly.  We have to emit to current memory, then save
            // to a patch, so we'll tack some extra sysex on in that situation
                    
            byte BB = (byte) tempModel.get("bank");
            byte NN = (byte) tempModel.get("number");
        
            data = new byte[] { (byte)0xF0, (byte)0x42, (byte)(48 + getChannelOut()), (byte)0x4A, (byte)0x11, (byte)0,
                (byte)(BB * 16 + NN), (byte)0xF7 };
            d[1] = data;
            }
        return d;
        }

        
    public void parseParameter(byte[] data)
        {
        if (data.length == 6 &&                 // write error report
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x42 &&
            data[3] == (byte)0x4A &&
            data[4] == (byte)0x24 &&
            data[5] == (byte)0xF7)
            {
            showSimpleError("Write/Send Error", "Send or Write Failed (Not Sure Why)");
            }
        else if (data.length == 6 &&                 // write error report
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x42 &&
            data[3] == (byte)0x4A &&
            data[4] == (byte)0x22 &&
            data[5] == (byte)0xF7)
            {
            showSimpleError("Write/Send Error", "Write Failed (Probably Write-Protect Is On?)");
            }
        }


    public int getPauseAfterChangePatch() { return 200; }

    public void changePatch(Model tempModel)
        {
        // enter program mode, which goes back out
        tryToSendSysex(new byte[] { (byte)0xF0, 0x42, (byte)(48 + getChannelOut()), 0x4A, 0x4E, 0x02, 0x0, (byte)0xF7 });

        byte BB = (byte)tempModel.get("bank");
        byte NN = (byte)tempModel.get("number");
        try {
            // Number change is PC
            tryToSendMIDI(new ShortMessage(ShortMessage.PROGRAM_CHANGE, getChannelOut(), BB * 16 + NN, 0));
            }
        catch (Exception e) { e.printStackTrace(); }

        // we assume that we successfully did it
        if (!isMerging())  // we're actually loading the patch, not merging with it
            {
            setSendMIDI(false);
            model.set("number", tempModel.get("number"));
            model.set("bank", tempModel.get("bank"));
            setSendMIDI(true);
            }
        }

    public void performRequestDump(Model tempModel, boolean changePatch)
        {
        // we always change the patch no matter what
        changePatch(tempModel);

        // enter program edit mode, which loads the patch into edit buffer memory
        tryToSendSysex(new byte[] { (byte)0xF0, 0x42, (byte)(48 + getChannelOut()), 0x4A, 0x4E, 0x03, 0x0, (byte)0xF7 });
        tryToSendSysex(requestCurrentDump());
        }
            
    public void performRequestCurrentDump()
        {
        // enter program mode, which goes back out
        tryToSendSysex(new byte[] { (byte)0xF0, 0x42, (byte)(48 + getChannelOut()), 0x4A, 0x4E, 0x02, 0x0, (byte)0xF7 });

        // enter program edit mode, which loads the patch into edit buffer memory
        tryToSendSysex(new byte[] { (byte)0xF0, 0x42, (byte)(48 + getChannelOut()), 0x4A, 0x4E, 0x03, 0x0, (byte)0xF7 });
        tryToSendSysex(requestCurrentDump());
        }
            
    public byte[] requestCurrentDump()
        {
        return new byte[] { (byte)0xF0, 0x42, (byte)(48 + getChannelOut()), 0x4A, 0x10, (byte)0xF7 };
        }
    
    
    public static final int EXPECTED_SYSEX_LENGTH = 64;
    public static boolean recognize(byte[] data)
        {
        boolean v = (
            data.length == EXPECTED_SYSEX_LENGTH &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x42 &&
            // don't care
            //data[2] == (byte)(48 + getChannelOut()) &&
            data[3] == (byte)0x4A &&
            data[4] == (byte)0x40);
        return v;
        }
    
    
    
    
    /////// OTHER ABSTRACT METHODS
    
    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        JComboBox bank = new JComboBox(BANKS);
        bank.setEditable(false);
        bank.setMaximumRowCount(32);
        bank.setSelectedIndex(model.get("bank"));
                
        JTextField number = new JTextField("" + (model.get("number") + 1), 3);
                
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
                showSimpleError(title, "The Patch Number must be an integer 1 ... 16");
                continue;
                }
            if (n < 1 || n > 16)
                {
                showSimpleError(title, "The Patch Number must be an integer 1 ... 16");
                continue;
                }
                                
            change.set("bank", bank.getSelectedIndex());
            change.set("number", n - 1);
                        
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
        
    public static String getSynthName() { return "Korg SG Rack"; }
    
    public String getPatchName(Model model) { return model.get("name", "Init"); }
    

    public Model getNextPatchLocation(Model model)
        {
        int bank = model.get("bank");
        int number = model.get("number");
        
        number++;
        if (number >= 16)
            {
            bank++;
            number = 0;
            if (bank >= 4)
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
    
