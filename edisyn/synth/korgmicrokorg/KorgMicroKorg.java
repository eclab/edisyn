/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.korgmicrokorg;

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
   A patch editor for the Korg MicroKorg.
        
   @author Sean Luke
*/

public class KorgMicroKorg extends Synth
    {
    public static final String[] BANKS = new String[] { "A", "B" };
    public static final String[] TIMBRE_ASSIGN_MODES = new String[] { "Mono", "Poly", "Unison" };
    // public static final String[] VOICE_MODES = new String[] { "Single (Timbre 1)", "Layer (Timbres 1 + 2)", "Vocoder" };
    public static final String[] VOICE_MODES = new String[] { "Single (Timbre 1)", "Layer (Timbres 1 + 2)" };
    public static final String[] ARPEGGIO_TARGETS = new String[] { "Timbres 1 + 2", "Timbre 1", "Timbre 2" };
    public static final String[] ARPEGGIO_TYPES = new String[] { "Up", "Down", "Alt1", "Alt2", "Random", "Trigger" };
    public static final String[] ARPEGGIO_RESOLUTIONS = new String[] { "1/24", "1/16", "1/12", "1/8", "1/6", "1/4" };
    public static final String[] DELAY_TIME_BASES = new String[] { "1/32", "1/24", "1/16", "1/12", "3/32", "1/8", "1/6", "3/16", "1/4", "1/3", "3/8", "1/2", "2/3", "3/4", "1/1" };
    public static final String[] DELAY_TYPES = new String[] { "Stereo", "Cross", "Left/Right" };
    public static final String[] MOD_TYPES = new String[] { "Chorus/Flanger", "Ensemble", "Phaser" };
    public static final String[] MOD_HIGH_FREQ = new String[] { "1.00", "1.25", "1.50", "1.75", "2.00", "2.25", "2.50", "2.75", "3.00", "3.25", "3.50", "3.75", "4.00", "4.25", "4.50", "4.75", "5.00", "5.25", "5.50", "5.75", "6.00", "7.00", "8.00", "9.00", "10.0", "11.0", "12.00", "14.0", "16.0", "18.0" };
    public static final String[] MOD_LOW_FREQ = new String[] { "40", "50", "60", "80", "100", "120", "140", "160", "180", "200", "220", "240", "260", "280", "300", "320", "340", "360", "380", "400", "420", "440", "460", "480", "500", "600", "700", "800", "900", "1000" };
    public static final String[] OSC1_WAVES = new String[] { "Sawtooth", "Pulse", "Triangle", "Sine (Cross)", "Vox", "DWGS", "Noise", "Audio In" };
    public static final String[] OSC2_WAVES = new String[] { "Sawtooth", "Square", "Triangle" };
    public static final String[] OSC2_MODS = new String[] { "Off", "Ring", "Sync", "Ring + Sync" };
    public static final String[] FILTER_TYPES = new String[] { "24 LP", "12 LP", "12 BP", "12 HP" };
    public static final String[] LFO_KEY_SYNC = new String[] { "Off", "Timbre", "Voice" };
    public static final String[] LFO_WAVES = new String[] { "Sawtooth", "Square", "Triangle", "S & H" };
    public static final String[] LFO_SYNC_NOTES = new String[] { "1/1", "3/4", "2/3", "1/2", "3/8", "1/3", "1/4", "3/16", "1/6", "1/8", "3/32", "1/12", "1/16", "1/24", "1/32" }; 
    public static final String[] PATCH_SOURCES = new String[] { "Filter Env", "Amp Env", "LFO 1", "LFO 2", "Velocity", "Keyboard", "Pitch Bend", "Mod Wheel" };
    public static final String[] PATCH_DESTINATIONS = new String[] { "Pitch", "Osc 2 Pitch", "Osc 1 Ctrl 1", "Noise", "Cutoff", "Amplifier", "Pan", "LFO 2 Freq" };
    public static final String[] FILTER_MOD_SOURCES = new String[] { "Off", "AEG", "LFO 1", "LFO 2", "Velocity", "Keyboard", "Pitch Bend", "Mod Wheel" };


    public SynthPanel[] panels = new SynthPanel[4];
        
    public KorgMicroKorg()
        {
        JComponent soundPanel = new SynthPanel(this);
        VBox vbox = new VBox();
        HBox hbox = new HBox();
    
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        hbox.addLast(addGeneral(false, Style.COLOR_A()));
        vbox.add(hbox);
        vbox.add(addArpeggiation(Style.COLOR_A()));

        hbox = new HBox();
        hbox.add(addMod(Style.COLOR_B()));
        hbox.addLast(addEQ(Style.COLOR_B()));
        vbox.add(hbox);
        vbox.add(addDelay(Style.COLOR_B()));
        
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("General", soundPanel);

        for(int timbre = 1; timbre < 4; timbre++)
            {
            soundPanel = panels[timbre - 1] = new SynthPanel(this);
            vbox = new VBox();
            hbox = new HBox();
            hbox.add(addTimbreGeneral(timbre, Style.COLOR_A()));
            hbox.add(addPitch(timbre, Style.COLOR_A()));
            hbox.addLast(addMixer(timbre, Style.COLOR_A()));
            vbox.add(hbox);
            hbox = new HBox();
            hbox.add(addOsc1(timbre, Style.COLOR_A()));
            hbox.addLast(addOsc2(timbre, Style.COLOR_A()));
            vbox.add(hbox);
            hbox = new HBox();
            hbox.add(addFilter(timbre, Style.COLOR_B()));
            hbox.addLast(addEnvelope(timbre, 1, Style.COLOR_B()));
            vbox.add(hbox);

            hbox = new HBox();
            hbox.add(addAmplifier(timbre, Style.COLOR_B()));
            hbox.addLast(addEnvelope(timbre, 2, Style.COLOR_B()));
            vbox.add(hbox);
                        
            hbox = new HBox();
            hbox.add(addLFO(timbre, 1, Style.COLOR_C()));
            hbox.addLast(addLFO(timbre, 2, Style.COLOR_C()));
            vbox.add(hbox);
                        
            if (timbre != 3)
                {
                vbox.add(addPatch(timbre, Style.COLOR_C()));
                }
                                
            soundPanel.add(vbox, BorderLayout.CENTER);
            ((SynthPanel)soundPanel).makePasteable("timbre" + timbre);
            addTab(timbre == 3 ? "Vocoder" : "Timbre " + timbre, soundPanel);
            }
                        
        soundPanel = panels[3] = new SynthPanel(this);
        vbox = new VBox();
        vbox.add(addChannels(Style.COLOR_A()));
        vbox.add(addHold(Style.COLOR_B()));
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Channels", soundPanel);

        model.set("name", "Init");
        model.set("number", 0);
        model.set("bank", 0);
        
        // at this point we have all of the tabs installed, yet several
        // will get removed by the next line, but that's okay because
        // Timbre 1 is the biggest tab pane in both dimensions and will
        // define the size of the window anyway.  If this wasn't the case
        // we'd need to do call the next line as part of sprout, doing a
        // pack() before we did so.

        setVoiceMode();
                
        loadDefaults();
        }
    
    public JFrame sprout()
        {
        JFrame frame = super.sprout();
        transmitParameters.setSelected(false);
        transmitParameters.setEnabled(false);
        return frame;
        }
    
    void setVoiceMode()
        {
        model.set("voicemode", model.get("voicemode", 0));
        }
                   
    public String getDefaultResourceFileName() { return "KorgMicroKorg.init"; }
    public String getHTMLResourceFileName() { return "KorgMicroKorg.html"; }
                
                
              
    /** Add the global patch category (name, id, number, etc.) */
    public JComponent addNameGlobal(Color color)
        {
        Category globalCategory = new Category(this, getSynthName(), color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
                
        VBox vbox = new VBox();
        HBox hbox2 = new HBox();
        comp = new PatchDisplay(this, 3);
        hbox2.add(comp);
        vbox.add(hbox2);
        
        comp = new StringComponent("Patch Name", this, "name", 12, "Name must be up to 12 ASCII characters.")
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
        
        hbox.add(Strut.makeHorizontalStrut(10));

        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }
        
    public String revisePatchName(String name)
        {
        if (name == null) name = "";
        char[] chars = name.toCharArray();
        for(int i = 0; i < chars.length; i++)
            {
            if (chars[i] < 32 || chars[i] > 127)
                chars[i] = ' ';
            }
        return new String(chars);
        }

 
    public JComponent addGeneral(boolean vocoder, Color color)
        {
        Category category  = new Category(this, "General", color);
                        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        if (!vocoder)
            {
            params = VOICE_MODES;
            comp = new Chooser("Voice Mode", this, "voicemode", params)
                {
                public void update(String key, Model model)
                    {
                    super.update(key, model);
                                
                    if (getNumTabs() == 0)  // haven't been set up yet
                        return;
                                
                    removeTab("Timbre 1");
                    removeTab("Timbre 2");
                    removeTab("Vocoder");
                    removeTab("Channels");

                    switch (model.get(key, 0))
                        {
                        case 0:
                            insertTab("Timbre 1", panels[0], 1);
                            break;
                        case 1:
                            insertTab("Timbre 1", panels[0], 1);
                            insertTab("Timbre 2", panels[1], 2);
                            break;
                        default:
                            insertTab("Vocoder", panels[2], 1);
                            insertTab("Channels", panels[3], 2);
                            break;
                        }
                    }
                };
            vbox.add(comp);
            hbox.add(vbox);
            }
        
        // obviously this may need to be handled specially
        comp = new LabelledDial("Keyboard", this, "octave", color, -3, 3)
            {
            public boolean isSymmetric() { return true; }
            };
        ((LabelledDial)comp).addAdditionalLabel("Octave");
        hbox.add(comp);        

        category.add(hbox);
        return category;
        }
                     
    public CheckBox[] arpeggiation = new CheckBox[8];
        
    public JComponent addArpeggiation(Color color)
        {
        Category category  = new Category(this, "Arpeggiator", color);
                        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox all = new VBox();
        VBox vbox = new VBox();
        
        params = ARPEGGIO_TYPES;
        comp = new Chooser("Type", this, "arptype", params);
        vbox.add(comp);        
        
        comp = new CheckBox("On", this, "arpon");
        vbox.add(comp);

        hbox.add(vbox);
        vbox = new VBox();
        
        if (!(this instanceof KorgMicroKorgVocoder))
            {
            params = ARPEGGIO_TARGETS;
            comp = new Chooser("Target", this, "arptarget", params);
            vbox.add(comp);
            }
        
        HBox hbox2 = new HBox();
        comp = new CheckBox("Latch", this, "arplatch");
        hbox2.add(comp);        

        comp = new CheckBox("Key Sync", this, "arpkeysync");
        hbox2.add(comp);
        vbox.add(hbox2);
        hbox.add(vbox);    

        for(int pattern = 0; pattern < 8; pattern++)
            {
            final int p = pattern;
            arpeggiation[pattern] = new CheckBox("", this, "arptriggerpattern" + pattern);
            }

        comp = new LabelledDial("Trigger Length", this, "arptriggerlength", color, 0, 7, -1)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                int len = model.get(key, 0);
                for(int i = 0; i < 8; i++)
                    {
                    arpeggiation[i].setEnabled(i <= len);
                    }
                }
            };
        hbox.add(comp);        
        
        vbox = new VBox();
        hbox2 = new HBox();
        hbox2.addLast(new TextLabel("Arpeggiator Pattern"));
        vbox.add(hbox2);
        hbox2 = new HBox();
        for(int pattern = 0; pattern < 8; pattern++)
            {
            hbox2.add(arpeggiation[pattern]);
            }
        vbox.add(hbox2);
        hbox.add(vbox);


        all.add(hbox);
        
        all.add(Strut.makeVerticalStrut(15));
        
        hbox = new HBox();      

        comp = new LabelledDial("Tempo", this, "arptempo", color, 20, 300);
        hbox.add(comp);        
        
        comp = new LabelledDial("Resolution", this, "arpresolution", color, 0, 5)
            {
            public String map(int val)
                {
                return ARPEGGIO_RESOLUTIONS[val];
                }
            };
        hbox.add(comp);        

        comp = new LabelledDial("Swing", this, "arpswing", color, -100, 100)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val)
                {
                return "" + val + "%";
                }
            };
        hbox.add(comp);  

        comp = new LabelledDial("Range", this, "arprange", color, 0, 3)
            {
            public String map(int val)
                {
                return "" + (val + 1) + " Oct";
                }
            };
        hbox.add(comp);        
        
        comp = new LabelledDial("Gate", this, "arpgate", color, 0, 100)
            {
            public String map(int val)
                {
                return "" + val + "%";
                }
            };
        hbox.add(comp);        
        all.add(hbox);

    
        category.add(all);
        return category;
        }
        
        
        
        
    public JComponent addDelay(Color color)
        {
        Category category  = new Category(this, "Delay FX", color);
                        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        params = DELAY_TYPES;
        comp = new Chooser("Type", this, "delaytype", params);
        vbox.add(comp);
        
        comp = new CheckBox("Sync", this, "delaysync");
        vbox.add(comp);        
        hbox.add(vbox);
                
        comp = new LabelledDial("Time Base", this, "delaytimebase", color, 0, 14)
            {
            public String map(int val)
                {
                return DELAY_TIME_BASES[val];
                }
            };
        hbox.add(comp);        

        comp = new LabelledDial("Time", this, "delaytime", color, 0, 127);
        hbox.add(comp);        

        comp = new LabelledDial("Depth", this, "delaydepth", color, 0, 127);
        hbox.add(comp);        

        category.add(hbox);
        return category;
        }
        
    public JComponent addMod(Color color)
        {
        Category category  = new Category(this, "Mod FX", color);
                        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        params = MOD_TYPES;
        comp = new Chooser("Type", this, "modtype", params);
        vbox.add(comp);
        hbox.add(vbox);
        
        comp = new LabelledDial("LFO Speed", this, "modlfospeed", color, 0, 127);
        hbox.add(comp);        

        comp = new LabelledDial("Depth", this, "moddepth", color, 0, 127);
        hbox.add(comp);        

        category.add(hbox);
        return category;
        }


    public JComponent addEQ(Color color)
        {
        Category category  = new Category(this, "EQ", color);
                        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        comp = new LabelledDial("Low Freq", this, "eqlowfreq", color, 0, 29)
            {
            public String map(int val)
                {
                return MOD_LOW_FREQ[val];
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("(Hz)");
        hbox.add(comp);        

        comp = new LabelledDial("Low Gain", this, "eqlowgain", color, 64-12, 64+12, 64);
        hbox.add(comp);        

        comp = new LabelledDial("High Freq", this, "eqhifreq", color, 0, 29)
            {
            public String map(int val)
                {
                return MOD_HIGH_FREQ[val];
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("(KHz)");
        hbox.add(comp);        

        comp = new LabelledDial("High Gain", this, "eqhigain", color, 64-12, 64+12,64);
        hbox.add(comp);        

        category.add(hbox);
        return category;
        }


    public JComponent addTimbreGeneral(int timbre, Color color)
        {
        Category category  = new Category(this, "General", color);
        category.makePasteable("timbre" + timbre);
                        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        params = TIMBRE_ASSIGN_MODES;
        comp = new Chooser("Assign Mode", this, "timbre" + timbre + "assignmode", params);
        vbox.add(comp);

        comp = new CheckBox("Multi Trigger", this, "timbre" + timbre + "triggermode");
        vbox.add(comp);  
        hbox.add(vbox);      

        comp = new LabelledDial("Unison", this, "timbre" + timbre + "unisondetune", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("Detune");
        hbox.add(comp);      

        category.add(hbox);
        return category;
        }


    public JComponent addPitch(int timbre, Color color)
        {
        Category category  = new Category(this, "Pitch", color);
        category.makePasteable("timbre" + timbre);
                       
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        comp = new LabelledDial("Tune", this, "timbre" + timbre + "tune", color, 64-50, 64+50, 64);
        hbox.add(comp);

        comp = new LabelledDial("Bend Range", this, "timbre" + timbre + "bendrange", color, 64-12, 64+12, 64);
        hbox.add(comp);

        comp = new LabelledDial("Transpose", this, "timbre" + timbre + "transpose", color, 64-24, 64+24, 64);
        hbox.add(comp);

        comp = new LabelledDial("Vibrato", this, "timbre" + timbre + "vibratoint", color, 64-63, 64+63, 64);
        ((LabelledDial)comp).addAdditionalLabel("Interval");
        hbox.add(comp);

        comp = new LabelledDial("Portamento", this, "timbre" + timbre + "portamentotime", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Time");
        hbox.add(comp);
                
        category.add(hbox);
        return category;
        }


    public JComponent addOsc1(int timbre, Color color)
        {
        Category category  = new Category(this, "Oscillator 1", color);
        category.makePasteable("timbre" + timbre);
                        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        params = OSC1_WAVES;
        comp = new Chooser("Wave", this, "timbre" + timbre + "osc1wave", params);
        vbox.add(comp);
        hbox.add(vbox);
                
        comp = new LabelledDial("Control 1", this, "timbre" + timbre + "osc1ctrl1", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Control 2", this, "timbre" + timbre + "osc1ctrl2", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("DWGS Wave", this, "timbre" + timbre + "osc1dwgswave", color, 0, 63)
            {
            public String map(int val)
                {
                return "" + (val + 1);
                }
            };
        hbox.add(comp);
                
        category.add(hbox);
        return category;
        }


    public JComponent addOsc2(int timbre, Color color)
        {
        Category category  = new Category(this, (timbre == 3 ? "Audio In" : "Oscillator 2"), color);
        category.makePasteable("timbre" + timbre);
                        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        if (timbre == 3)
            {
            comp = new CheckBox("HPF Gate", this, "timbre" + timbre + "osc2hpfgate");
            vbox.add(comp);        

            comp = new LabelledDial("HPF Level", this, "timbre" + timbre + "osc2hpflevel", color, 0, 127);
            hbox.add(comp);

            comp = new LabelledDial("Gate Sense", this, "timbre" + timbre + "osc2gatesense", color, 0, 127);
            hbox.add(comp);

            comp = new LabelledDial("Threshold", this, "timbre" + timbre + "osc2threshold", color, 0, 127);
            hbox.add(comp);
            }
        else
            {
            params = OSC2_WAVES;
            comp = new Chooser("Wave", this, "timbre" + timbre + "osc2wave", params);
            vbox.add(comp);

            params = OSC2_MODS;
            comp = new Chooser("Modulation", this, "timbre" + timbre + "osc2modselect", params);
            vbox.add(comp);
            hbox.add(vbox);

            comp = new LabelledDial("Semitone", this, "timbre" + timbre + "osc2semitone", color, 64 - 24, 64 + 24, 64);
            hbox.add(comp);

            comp = new LabelledDial("Tune", this, "timbre" + timbre + "osc2tune", color, 64 - 63, 64 + 63, 64);
            hbox.add(comp);
            }
                
        category.add(hbox);
        return category;
        }

    public JComponent addMixer(int timbre, Color color)
        {
        Category category  = new Category(this, "Mixer", color);
        category.makePasteable("timbre" + timbre);
                        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        comp = new LabelledDial("Osc 1", this, "timbre" + timbre + "osc1level", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial((timbre == 3 ? "Audio In" : "Osc 2"), this, "timbre" + timbre + "osc2level", color, 0, 127);
        hbox.add(comp);
                
        comp = new LabelledDial("Noise", this, "timbre" + timbre + "noise", color, 0, 127);
        hbox.add(comp);
                
        category.add(hbox);
        return category;
        }

    public JComponent addFilter(int timbre, Color color)
        {
        Category category  = new Category(this, "Filter", color);
        category.makePasteable("timbre" + timbre);
                        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        if (timbre == 3)
            {
            params = FILTER_MOD_SOURCES;
            comp = new Chooser("Mod Source", this, "timbre" + timbre + "filtermodsource", params);
            vbox.add(comp);
            hbox.add(vbox);

            comp = new LabelledDial("Shift", this, "timbre" + timbre + "filtershift", color, 0, 4, 2)
                {
                public boolean isSymmetric() { return true; }
                };
            hbox.add(comp);

            comp = new LabelledDial("Cutoff", this, "timbre" + timbre + "filtercutoff", color, 64-63, 64+63, 64);
            hbox.add(comp);

            comp = new LabelledDial("Resonance", this, "timbre" + timbre + "filterresonance", color, 0, 127);
            hbox.add(comp);

            comp = new LabelledDial("Filter", this, "timbre" + timbre + "filterintensity", color, 64-63, 64+63, 64);
            ((LabelledDial)comp).addAdditionalLabel("Intensity");
            hbox.add(comp);

            comp = new LabelledDial("EF Sense", this, "timbre" + timbre + "filterefsense", color, 0, 127)
                {
                public String map(int val)
                    {
                    if (val == 127) return "Hold";
                    else return "" + val;
                    }
                };
            hbox.add(comp);
            }
        else
            {
            params = FILTER_TYPES;
            comp = new Chooser("Type", this, "timbre" + timbre + "filtertype", params);
            vbox.add(comp);

            comp = new CheckBox("EG Reset", this, "timbre" + timbre + "filtereg1reset");
            vbox.add(comp);   
            hbox.add(vbox);     

            comp = new LabelledDial("Cutoff", this, "timbre" + timbre + "filtercutoff", color, 0, 127);
            hbox.add(comp);

            comp = new LabelledDial("Resonance", this, "timbre" + timbre + "filterresonance", color, 0, 127);
            hbox.add(comp);

            comp = new LabelledDial("Filter EG", this, "timbre" + timbre + "filtereg1intensity", color, 64-63, 64+63, 64);
            ((LabelledDial)comp).addAdditionalLabel("Intensity");
            hbox.add(comp);

            comp = new LabelledDial("Keyboard", this, "timbre" + timbre + "filterkeyboardtrack", color, 64-63, 64+63, 64);
            ((LabelledDial)comp).addAdditionalLabel("Tracking");
            hbox.add(comp);
            }
                                        
        category.add(hbox);
        return category;
        }



    public JComponent addAmplifier(int timbre, Color color)
        {
        Category category  = new Category(this, "Amplifier", color);
        category.makePasteable("timbre" + timbre);
                        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        comp = new CheckBox("Distortion", this, "timbre" + timbre + "ampdistortion");
        vbox.add(comp);        

        comp = new CheckBox("EG Reset", this, "timbre" + timbre + "ampeg2reset");
        vbox.add(comp);  
        hbox.add(vbox);      

        comp = new LabelledDial("Level", this, "timbre" + timbre + "amplevel", color, 0, 127);
        hbox.add(comp);

        if (timbre == 3)
            {
            comp = new LabelledDial("Direct", this, "timbre" + timbre + "ampdirectlevel", color, 0, 127);
            ((LabelledDial)comp).addAdditionalLabel("Level");
            hbox.add(comp);
            }
        else
            {
            comp = new LabelledDial("Panpot", this, "timbre" + timbre + "amppanpot", color, 0, 127)
                {
                public boolean isSymmetric() { return true; }
                public String map(int val)
                    {
                    if (val == 64)
                        return "--";
                    else if (val < 64)
                        return "L" + (64 - val);
                    else
                        return "R" + (val - 64);
                    }
                };
            hbox.add(comp);
            }

        comp = new LabelledDial("Keyboard", this, "timbre" + timbre + "ampkeyboardtrack", color, 64-63, 64+63, 64);
        ((LabelledDial)comp).addAdditionalLabel("Tracking");
        hbox.add(comp);
                
        category.add(hbox);
        return category;
        }

    public JComponent addEnvelope(int timbre, int envelope, Color color)
        {
        Category category  = new Category(this, (envelope == 1 ? "Filter Envelope" : "Amplifier Envelope"), color);
        category.makePasteable("timbre" + timbre + "env" + envelope);
                        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        if (timbre != 3 || envelope != 1)
            {
            comp = new LabelledDial("Attack", this, "timbre" + timbre + "env" + envelope + "attack", color, 0, 127);
            hbox.add(comp);

            comp = new LabelledDial("Decay", this, "timbre" + timbre + "env" + envelope + "decay", color, 0, 127);
            hbox.add(comp);
            }

        comp = new LabelledDial("Sustain", this, "timbre" + timbre + "env" + envelope + "sustain", color, 0, 127);
        hbox.add(comp);

        if (timbre != 3 || envelope != 1)
            {
            comp = new LabelledDial("Release", this, "timbre" + timbre + "env" + envelope + "release", color, 0, 127);
            hbox.add(comp);

            comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
                new String[] { null, "timbre" + timbre + "env" + envelope + "attack", "timbre" + timbre + "env" + envelope + "decay", null,"timbre" + timbre + "env" + envelope + "release" },
                new String[] { null, null, "timbre" + timbre + "env" + envelope + "sustain", "timbre" + timbre + "env" + envelope + "sustain", null },
                new double[] { 0, 0.25/127.0, 0.25 / 127.0,  0.25, 0.25/127.0},
                new double[] { 0, 1.0, 1.0 / 127.0, 1.0/127.0, 0 });
            hbox.addLast(comp);
            }
                
        category.add(hbox);
        return category;
        }

    public JComponent addLFO(int timbre, int lfo, Color color)
        {
        Category category  = new Category(this, "LFO " + lfo, color);
        category.makePasteable("timbre" + timbre + "lfo" + lfo);
                        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        params = LFO_WAVES;
        comp = new Chooser("Wave", this, "timbre" + timbre + "lfo" + lfo + "wave", params);
        vbox.add(comp);

        params = LFO_KEY_SYNC;
        comp = new Chooser("Key Sync", this, "timbre" + timbre + "lfo" + lfo + "keysync", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new CheckBox("Tempo Sync", this, "timbre" + timbre + "lfo" + lfo + "temposync");
        ((CheckBox)comp).addToWidth(2);
        hbox.add(comp);        

        comp = new LabelledDial("Frequency", this, "timbre" + timbre + "lfo" + lfo + "frequency", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Sync Note", this, "timbre" + timbre + "lfo" + lfo + "syncnote", color, 0, 14)
            {
            public String map(int val)
                {
                return LFO_SYNC_NOTES[val];
                }
            };
        hbox.add(comp);

        category.add(hbox);
        return category;
        }
        
    public JComponent addChannels(Color color)
        {
        Category category  = new Category(this, "Vocoder Channels", color);
        category.makeDistributable("channel");
                        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        for(int i = 1; i < 9; i++)
            {
            comp = new LabelledDial("Level " + i, this, "channel" + i + "level", color, 0, 127);
            hbox.add(comp);
            }
        vbox.add(hbox);
        hbox = new HBox();
        for(int i = 1; i < 9; i++)
            {
            comp = new LabelledDial("Pan " + i, this, "channel" + i + "pan", color, 0, 127)
                {
                public boolean isSymmetric() { return true; }
                public String map(int val)
                    {
                    if (val == 64)
                        return "--";
                    else if (val < 64)
                        return "L" + (64 - val);
                    else
                        return "R" + (val - 64);
                    }
                };
            hbox.add(comp);
            }
        vbox.add(hbox);
        category.add(vbox);
        return category;
        }

    public JComponent addHold(Color color)
        {
        Category category  = new Category(this, "Vocoder Hold Levels", color);
        category.makeDistributable("channel");
                        
        JComponent comp;
        String[] params;
        VBox vbox = new VBox();
        
        for(int i = 1; i < 17; i+=4)
            {
            HBox hbox = new HBox();
            for(int j = i; j < i + 4; j++)
                {
                comp = new NumberTextField("Hold " + j, this, 10, Style.ENVELOPE_COLOR(), "channel" + j + "hold");
                ((NumberTextField)comp).setMin(0);
                ((NumberTextField)comp).setMax(2147483392);
                hbox.add(comp);
                }
            vbox.add(hbox);
            }
                
        category.add(vbox);
        return category;
        }

    public JComponent addPatch(int timbre, Color color)
        {
        Category category  = new Category(this, "Patches", color);
        category.makePasteable("timbre" + timbre);
        category.makeDistributable("timbre" + timbre);
                        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        for(int i = 1; i < 5; i++)
            {
            VBox vbox = new VBox();
            params = PATCH_SOURCES;
            comp = new Chooser("Patch " + i + " Source", this, "timbre" + timbre + "patch" + i + "source", params);
            vbox.add(comp);

            params = PATCH_DESTINATIONS;
            comp = new Chooser("Patch " + i + " Destination", this, "timbre" + timbre + "patch" + i + "destination", params);
            vbox.add(comp);
            hbox.add(vbox);

            comp = new LabelledDial("Patch " + i, this, "timbre" + timbre + "patch" + i + "intensity", color, 64-63, 64+63, 64);
            ((LabelledDial)comp).addAdditionalLabel("Intensity");
            hbox.add(comp);
                        
            if (i < 4)
                hbox.add(Strut.makeHorizontalStrut(10));
            }

        category.add(hbox);
        return category;
        }











    // converts all but last byte (F7)
    static byte[] convertTo8Bit(byte[] data, int offset)
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
        data = convertTo8Bit(data, 5);

        char[] namec = new char[12];
        String name;
        for(int i = 0; i < 12; i++)
            {
            namec[i] = (char)data[i];
            }
        name = new String(namec);
        model.set("name", name);
        
        // this will have to be set entirely custom.  :-(  Stupid Korg.  Really bad sysex.
        
        model.set("arptriggerlength", data[14] & 7);
        for(int i = 0; i < 8; i++)
            {
            model.set("arptriggerpattern" + i, (data[15] >>> i) & 1);
            }
        int voicemode = (data[16] >>> 4) & 3;
        if (voicemode >= 2) voicemode--;        // voice mode goes 0, 2, 3 (!!) so we have to tweak it
        model.set("voicemode", voicemode);
        model.set("delaysync", (data[19] >>> 7) & 1);
        model.set("delaytimebase", data[19] & 7);
        model.set("delaytime", data[20]);
        model.set("delaydepth", data[21]);
        model.set("delaytype", data[22]);
        model.set("modlfospeed", data[23]);
        model.set("moddepth", data[24]);
        model.set("modtype", data[25]);
        model.set("eqhifreq", data[26]);
        model.set("eqhigain", data[27]);
        model.set("eqlowfreq", data[28]);
        model.set("eqlowgain", data[29]);
        int arptempo = (data[30] << 8) | data[31];
        if (arptempo < 0) arptempo += 256;
        model.set("arptempo", arptempo);
        model.set("arpon", (data[32] >>> 7) & 1);
        model.set("arplatch", (data[32] >>> 6) & 1);
        model.set("arptarget", (data[32] >>> 4) & 3);
        model.set("arpkeysync", data[32] & 1);
        // The documentation says this takes up 4 bytes, but it only goes 0...5.  ???
        model.set("arptype", data[33] & 7);
        // The documentation says this takes up 3 bytes, but it only goes 0...3.  ???
        model.set("arprange", (data[33] >>> 4) & 3);
        model.set("arpgate", data[34]);
        model.set("arpresolution", data[35]);
        int swing = data[36];
        if (swing >= 128) swing -= 256;  // don't think this is necessary
        model.set("arpswing", swing);
        model.set("octave", data[37]);
        
        if (voicemode == 0 || voicemode == 1)           // timbre 1 and timbre 2
            {
            for(int i = 1; i <= 2; i++)
                {
                int offset = 38 + 108 * (i - 1);
                model.set("timbre" + i + "assignmode", (data[offset + 1] >>> 6) & 3);
                model.set("timbre" + i + "ampeg2reset", (data[offset + 1] >>> 5) & 1);
                model.set("timbre" + i + "filtereg1reset", (data[offset + 1] >>> 4) & 1);
                model.set("timbre" + i + "triggermode", (data[offset + 1] >>> 3) & 1);
                model.set("timbre" + i + "unisondetune", data[offset + 2]);
                model.set("timbre" + i + "tune", data[offset + 3]);
                model.set("timbre" + i + "bendrange", data[offset + 4]);
                model.set("timbre" + i + "transpose", data[offset + 5]);
                model.set("timbre" + i + "vibratoint", data[offset + 6]);
                model.set("timbre" + i + "osc1wave", data[offset + 7]);
                model.set("timbre" + i + "osc1ctrl1", data[offset + 8]);
                model.set("timbre" + i + "osc1ctrl2", data[offset + 9]);
                model.set("timbre" + i + "osc1dwgswave", data[offset + 10]);
                model.set("timbre" + i + "osc2modselect", (data[offset + 12] >>> 4) & 3);
                model.set("timbre" + i + "osc2wave", data[offset + 12] & 3);
                model.set("timbre" + i + "osc2semitone", data[offset + 13]);
                model.set("timbre" + i + "osc2tune", data[offset + 14]);
                model.set("timbre" + i + "portamentotime", data[offset + 15] & 127);
                model.set("timbre" + i + "osc1level", data[offset + 16]);
                model.set("timbre" + i + "osc2level", data[offset + 17]);
                model.set("timbre" + i + "noise", data[offset + 18]);
                model.set("timbre" + i + "filtertype", data[offset + 19]);
                model.set("timbre" + i + "filtercutoff", data[offset + 20]);
                model.set("timbre" + i + "filterresonance", data[offset + 21]);
                model.set("timbre" + i + "filtereg1intensity", data[offset + 22]);
                model.set("timbre" + i + "filterkeyboardtrack", data[offset + 24]);
                model.set("timbre" + i + "amplevel", data[offset + 25]);
                model.set("timbre" + i + "amppanpot", data[offset + 26]);
                model.set("timbre" + i + "ampdistortion", data[offset + 27] & 1);
                model.set("timbre" + i + "ampkeyboardtrack", data[offset + 29]);
                model.set("timbre" + i + "env1" + "attack", data[offset + 30]);
                model.set("timbre" + i + "env1" + "decay", data[offset + 31]);
                model.set("timbre" + i + "env1" + "sustain", data[offset + 32]);
                model.set("timbre" + i + "env1" + "release", data[offset + 33]);
                model.set("timbre" + i + "env2" + "attack", data[offset + 34]);
                model.set("timbre" + i + "env2" + "decay", data[offset + 35]);
                model.set("timbre" + i + "env2" + "sustain", data[offset + 36]);
                model.set("timbre" + i + "env2" + "release", data[offset + 37]);
                model.set("timbre" + i + "lfo1" + "keysync", (data[offset + 38] >>> 4) & 3);
                model.set("timbre" + i + "lfo1" + "wave", (data[offset + 38]) & 3);
                model.set("timbre" + i + "lfo1" + "frequency", data[offset + 39]);
                model.set("timbre" + i + "lfo1" + "temposync", (data[offset + 40] >>> 7) & 1);
                // documentation says this is bits 0...4 but this has to be wrong, since the values only go 0...14
                model.set("timbre" + i + "lfo1" + "syncnote", (data[offset + 40]) & 15);
                model.set("timbre" + i + "lfo2" + "keysync", (data[offset + 41] >>> 4) & 3);
                model.set("timbre" + i + "lfo2" + "wave", (data[offset + 41]) & 3);
                model.set("timbre" + i + "lfo2" + "frequency", data[offset + 42]);
                model.set("timbre" + i + "lfo2" + "temposync", (data[offset + 43] >>> 7) & 1);
                // documentation says this is bits 0...4 but this has to be wrong, since the values only go 0...14
                model.set("timbre" + i + "lfo2" + "syncnote", (data[offset + 43]) & 15);
                for(int j = 1; j <= 4; j++)
                    {
                    // documentation says this is bits 4...7 but this has to be wrong, since the values only go 0...7
                    model.set("timbre" + i + "patch" + j + "destination", (data[offset + ((j-1) * 2) + 44] >>> 4) & 7);
                    // documentation says this is bits 0...3 but this has to be wrong, since the values only go 0...7
                    model.set("timbre" + i + "patch" + j + "source", (data[offset + ((j-1) * 2) + 44]) & 7);
                    model.set("timbre" + i + "patch" + j + "intensity", data[offset + ((j-1) * 2) + 45]);
                    }
                }
            }
        else    // vocoder
            {
            int offset = 38;
            int i = 3;
            model.set("timbre" + i + "assignmode", (data[offset + 1] >>> 6) & 3);
            model.set("timbre" + i + "ampeg2reset", (data[offset + 1] >>> 5) & 1);
            model.set("timbre" + i + "triggermode", (data[offset + 1] >>> 3) & 1);
            model.set("timbre" + i + "unisondetune", data[offset + 2]);
            model.set("timbre" + i + "tune", data[offset + 3]);
            model.set("timbre" + i + "bendrange", data[offset + 4]);
            model.set("timbre" + i + "transpose", data[offset + 5]);
            model.set("timbre" + i + "vibratoint", data[offset + 6]);
            model.set("timbre" + i + "osc1wave", data[offset + 7]);
            model.set("timbre" + i + "osc1ctrl1", data[offset + 8]);
            model.set("timbre" + i + "osc1ctrl2", data[offset + 9]);
            model.set("timbre" + i + "osc1dwgswave", data[offset + 10]);
            model.set("timbre" + i + "osc2hpfgate", (data[offset + 12]) & 1);
            model.set("timbre" + i + "portamentotime", data[offset + 14] & 127);
            model.set("timbre" + i + "osc1level", data[offset + 15]);
            model.set("timbre" + i + "osc2level", data[offset + 16]);
            model.set("timbre" + i + "noise", data[offset + 17]);
            model.set("timbre" + i + "osc2hpflevel", data[offset + 18]);
            model.set("timbre" + i + "osc2gatesense", data[offset + 19]);
            model.set("timbre" + i + "osc2threshold", data[offset + 20]);
            model.set("timbre" + i + "filtershift", data[offset + 21]);
            model.set("timbre" + i + "filtercutoff", data[offset + 22]);
            model.set("timbre" + i + "filterresonance", data[offset + 23]);
            model.set("timbre" + i + "filtermodsource", data[offset + 24]);
            model.set("timbre" + i + "filterintensity", data[offset + 25]);
            model.set("timbre" + i + "filterefsense", data[offset + 26]);
            model.set("timbre" + i + "amplevel", data[offset + 27]);
            model.set("timbre" + i + "ampdirectlevel", data[offset + 28]);
            model.set("timbre" + i + "ampdistortion", data[offset + 29] & 1);
            model.set("timbre" + i + "ampkeyboardtrack", data[offset + 31]);
            model.set("timbre" + i + "env1" + "sustain", data[offset + 34]);
            model.set("timbre" + i + "env2" + "attack", data[offset + 36]);
            model.set("timbre" + i + "env2" + "decay", data[offset + 37]);
            model.set("timbre" + i + "env2" + "sustain", data[offset + 38]);
            model.set("timbre" + i + "env2" + "release", data[offset + 39]);
            model.set("timbre" + i + "lfo1" + "keysync", (data[offset + 40] >>> 4) & 3);
            model.set("timbre" + i + "lfo1" + "wave", (data[offset + 40]) & 3);
            model.set("timbre" + i + "lfo1" + "frequency", data[offset + 41]);
            model.set("timbre" + i + "lfo1" + "temposync", (data[offset + 42] >>> 7) & 1);
            // documentation says this is bits 0...4 but this has to be wrong, since the values only go 0...14
            model.set("timbre" + i + "lfo1" + "syncnote", (data[offset + 42]) & 15);
            model.set("timbre" + i + "lfo2" + "keysync", (data[offset + 43] >>> 4) & 3);
            model.set("timbre" + i + "lfo2" + "wave", (data[offset + 43]) & 3);
            model.set("timbre" + i + "lfo2" + "frequency", data[offset + 44]);
            model.set("timbre" + i + "lfo2" + "temposync", (data[offset + 45] >>> 7) & 1);
            // documentation says this is bits 0...4 but this has to be wrong, since the values only go 0...14
            model.set("timbre" + i + "lfo2" + "syncnote", (data[offset + 45]) & 15);
            for(int j = 1; j <= 8; j++)
                {
                // the documentation is very unclear here.  It appears that it says that pairs of MIDI values
                // should be exactly the same.
                model.set("channel" + j + "level", data[offset + ((j-1) * 2) + 46]);
                if (data[offset + ((j-1) * 2) + 46] != data[offset + ((j-1) * 2) + 47])
                    System.err.println("Warning (KorgMicroKorg): Channel" + j + "level inconsistent in pair: " + data[offset + ((j-1) * 2) + 47]);
                }
            for(int j = 1; j <= 8; j++)
                {
                // the documentation is very unclear here.  It appears that it says that pairs of MIDI values
                // should be exactly the same.
                model.set("channel" + j + "pan", data[offset + ((j-1) * 2) + 62]);
                if (data[offset + ((j-1) * 2) + 62] != data[offset + ((j-1) * 2) + 63])
                    System.err.println("Warning (KorgMicroKorg): Channel" + j + "pan inconsistent in pair: " + data[offset + ((j-1) * 2) + 63]);
                }
            for(int j = 1; j <= 16; j++)
                {
                // the documentation is very unclear here.  It appears that it says that pairs of MIDI values
                // should be exactly the same.
                int a = data[offset + ((j-1) * 4) + 78];
                if (a < 0) a += 255;
                int b = data[offset + ((j-1) * 4) + 79];
                if (b < 0) b += 255;
                int c = data[offset + ((j-1) * 4) + 80];
                if (c < 0) c += 255;
                int d = data[offset + ((j-1) * 4) + 81];
                if (d < 0) d += 255;
                int val = (a << 24) | (b << 16) | (c << 8) | d;
                if (val >= 0x7FFFFF00)
                    {
                    System.err.println("Warning (KorgMicroKorg): Too large value for hold number " + j + ": " + val);
                    val = 0x7FFFFF00;
                    }
                if (val < 0) 
                    {
                    System.err.println("Warning (KorgMicroKorg): Hold number " + j + " is negative: " + val);
                    val = 0;
                    }
                model.set("channel" + j + "hold", val);
                }
            }

        revise();       
        return PARSE_SUCCEEDED;     
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

        byte[] data = new byte[254];
        
        // LOAD DATA HERE
        
        String name = model.get("name", "Untitled");
        char[] namec = new char[12];
        char[] b = name.toCharArray();
        System.arraycopy(b, 0, namec, 0, b.length);
        for(int i = 0; i < 12; i++)
            {
            data[i] = (byte)(namec[i] & 127);
            }

        // this will have to be set entirely custom.  :-(  Stupid Korg.  Really bad sysex.

        data[12] = data[13] = data[14] = 0;        
        data[14] = (byte)model.get("arptriggerlength");
        for(int i = 0; i < 8; i++)
            {
            data[15] = (byte)( data[15] | (model.get("arptriggerpattern" + i) << i) );
            }
        // set up voice mode
        data[16] = (byte)model.get("voicemode");
        if (data[16] >= 1) data[16]++;        // voice mode goes 0, 2, 3 (!!) so we have to tweak it
        data[16] = (byte)(data[16] << 4);
                
        data[17] = 0;
        data[18] = 60;
        data[19] = (byte)((model.get("delaysync") << 7) | (model.get("delaytimebase")));
        data[20] = (byte)model.get("delaytime");
        data[21] = (byte)model.get("delaydepth");
        data[22] = (byte)model.get("delaytype");
        data[23] = (byte)model.get("modlfospeed");
        data[24] = (byte)model.get("moddepth");
        data[25] = (byte)model.get("modtype");
        data[26] = (byte)model.get("eqhifreq");
        data[27] = (byte)model.get("eqhigain");
        data[28] = (byte)model.get("eqlowfreq");
        data[29] = (byte)model.get("eqlowgain");
        data[30] = (byte)(model.get("arptempo") >>> 8);
        data[31] = (byte)(model.get("arptempo") & 255);
        data[32] = (byte)((model.get("arpon") << 7) | (model.get("arplatch") << 6) | (model.get("arptarget") << 4) | (model.get("arpkeysync")));
        data[33] = (byte)((model.get("arptype")) | (model.get("arprange") << 4));
        data[34] = (byte)model.get("arpgate");
        data[35] = (byte)model.get("arpresolution");
        data[36] = (byte)model.get("arpswing");         // this is signed, but it should be okay
        data[37] = (byte)model.get("octave");           // this is signed, but it should be okay
        
        int voicemode = (byte)model.get("voicemode");
        if (voicemode == 0 || voicemode == 1)           // timbre 1 and timbre 2
            {
            for(int i = 1; i <= 2; i++)
                {
                int offset = 38 + 108 * (i - 1);
                data[offset + 0] = (byte)-1;
                data[offset + 1] = (byte)((model.get("timbre" + i + "assignmode") << 6) |
                    (model.get("timbre" + i + "ampeg2reset") << 5) | 
                    (model.get("timbre" + i + "filtereg1reset") << 4) |
                    (model.get("timbre" + i + "triggermode") << 3));
                data[offset + 2] = (byte)model.get("timbre" + i + "unisondetune");
                data[offset + 3] = (byte)model.get("timbre" + i + "tune");
                data[offset + 4] = (byte)model.get("timbre" + i + "bendrange");
                data[offset + 5] = (byte)model.get("timbre" + i + "transpose");
                data[offset + 6] = (byte)model.get("timbre" + i + "vibratoint");
                data[offset + 7] = (byte)model.get("timbre" + i + "osc1wave");
                data[offset + 8] = (byte)model.get("timbre" + i + "osc1ctrl1");
                data[offset + 9] = (byte)model.get("timbre" + i + "osc1ctrl2");
                data[offset + 10] = (byte)model.get("timbre" + i + "osc1dwgswave");
                data[offset + 11] = 0;
                data[offset + 12] = (byte)((model.get("timbre" + i + "osc2modselect") << 4) | 
                    ((byte)model.get("timbre" + i + "osc2wave")));
                data[offset + 13] = (byte)model.get("timbre" + i + "osc2semitone");
                data[offset + 14] = (byte)model.get("timbre" + i + "osc2tune");
                data[offset + 15] = (byte)model.get("timbre" + i + "portamentotime");
                data[offset + 16] = (byte)model.get("timbre" + i + "osc1level");
                data[offset + 17] = (byte)model.get("timbre" + i + "osc2level");
                data[offset + 18] = (byte)model.get("timbre" + i + "noise");
                data[offset + 19] = (byte)model.get("timbre" + i + "filtertype");
                data[offset + 20] = (byte)model.get("timbre" + i + "filtercutoff");
                data[offset + 21] = (byte)model.get("timbre" + i + "filterresonance");
                data[offset + 22] = (byte)model.get("timbre" + i + "filtereg1intensity");
                data[offset + 23] = 64;
                data[offset + 24] = (byte)model.get("timbre" + i + "filterkeyboardtrack");
                data[offset + 25] = (byte)model.get("timbre" + i + "amplevel");
                data[offset + 26] = (byte)model.get("timbre" + i + "amppanpot");
                data[offset + 27] = (byte)model.get("timbre" + i + "ampdistortion");
                data[offset + 28] = 64;
                data[offset + 29] = (byte)model.get("timbre" + i + "ampkeyboardtrack");
                data[offset + 30] = (byte)model.get("timbre" + i + "env1" + "attack");
                data[offset + 31] = (byte)model.get("timbre" + i + "env1" + "decay");
                data[offset + 32] = (byte)model.get("timbre" + i + "env1" + "sustain");
                data[offset + 33] = (byte)model.get("timbre" + i + "env1" + "release");
                data[offset + 34] = (byte)model.get("timbre" + i + "env2" + "attack");
                data[offset + 35] = (byte)model.get("timbre" + i + "env2" + "decay");
                data[offset + 36] = (byte)model.get("timbre" + i + "env2" + "sustain");
                data[offset + 37] = (byte)model.get("timbre" + i + "env2" + "release");
                data[offset + 38] = (byte)((model.get("timbre" + i + "lfo1" + "keysync") << 4 ) | 
                    (model.get("timbre" + i + "lfo1" + "wave")));
                data[offset + 39] = (byte)model.get("timbre" + i + "lfo1" + "frequency");
                data[offset + 40] = (byte)(model.get("timbre" + i + "lfo1" + "temposync") << 7);
                // documentation says this is bits 0...4 but this has to be wrong, since the values only go 0...14
                data[offset + 40] = (byte)model.get("timbre" + i + "lfo1" + "syncnote");
                data[offset + 41] = (byte)((model.get("timbre" + i + "lfo2" + "keysync") << 4 ) |
                    (model.get("timbre" + i + "lfo2" + "wave")));
                data[offset + 42] = (byte)model.get("timbre" + i + "lfo2" + "frequency");
                data[offset + 43] = (byte)(model.get("timbre" + i + "lfo2" + "temposync") << 7);
                // documentation says this is bits 0...4 but this has to be wrong, since the values only go 0...14
                data[offset + 43] = (byte)model.get("timbre" + i + "lfo2" + "syncnote");
                for(int j = 1; j <= 4; j++)
                    {
                    // documentation says this is bits 4...7 but this has to be wrong, since the values only go 0...7
                    data[offset + ((j-1) * 2) + 44] = (byte)((model.get("timbre" + i + "patch" + j + "destination") << 4) |
                        // documentation says this is bits 0...3 but this has to be wrong, since the values only go 0...7
                        (model.get("timbre" + i + "patch" + j + "source")));
                    data[offset + ((j-1) * 2) + 45] = (byte)model.get("timbre" + i + "patch" + j + "intensity");
                    }
                for(int j = 52; j < 108; j++)
                    data[offset + j] = 0;
                }
            }
        else    // vocoder
            {
            int offset = 38;
            int i = 3;
            data[offset + 0] = (byte)-1;
            data[offset + 1] = (byte)((model.get("timbre" + i + "assignmode") << 6) |
                (model.get("timbre" + i + "ampeg2reset") << 5) | 
                (model.get("timbre" + i + "triggermode") << 3));
            data[offset + 2] = (byte)model.get("timbre" + i + "unisondetune");
            data[offset + 3] = (byte)model.get("timbre" + i + "tune");
            data[offset + 4] = (byte)model.get("timbre" + i + "bendrange");
            data[offset + 5] = (byte)model.get("timbre" + i + "transpose");
            data[offset + 6] = (byte)model.get("timbre" + i + "vibratoint");
            data[offset + 7] = (byte)model.get("timbre" + i + "osc1wave");
            data[offset + 8] = (byte)model.get("timbre" + i + "osc1ctrl1");
            data[offset + 9] = (byte)model.get("timbre" + i + "osc1ctrl2");
            data[offset + 10] = (byte)model.get("timbre" + i + "osc1dwgswave");
            data[offset + 11] = 0;
            data[offset + 12] = (byte)model.get("timbre" + i + "osc2hpfgate");
            data[offset + 13] = 0;
            data[offset + 14] = (byte)model.get("timbre" + i + "portamentotime");
            data[offset + 15] = (byte)model.get("timbre" + i + "osc1level");
            data[offset + 16] = (byte)model.get("timbre" + i + "osc2level");
            data[offset + 17] = (byte)model.get("timbre" + i + "noise");
            data[offset + 18] = (byte)model.get("timbre" + i + "osc2hpflevel");
            data[offset + 19] = (byte)model.get("timbre" + i + "osc2gatesense");
            data[offset + 20] = (byte)model.get("timbre" + i + "osc2threshold");
            data[offset + 21] = (byte)model.get("timbre" + i + "filtershift");
            data[offset + 22] = (byte)model.get("timbre" + i + "filtercutoff");
            data[offset + 23] = (byte)model.get("timbre" + i + "filterresonance");
            data[offset + 24] = (byte)model.get("timbre" + i + "filtermodsource");
            data[offset + 25] = (byte)model.get("timbre" + i + "filterintensity");
            data[offset + 26] = (byte)model.get("timbre" + i + "filterefsense");
            data[offset + 27] = (byte)model.get("timbre" + i + "amplevel");
            data[offset + 28] = (byte)model.get("timbre" + i + "ampdirectlevel");
            data[offset + 29] = (byte)model.get("timbre" + i + "ampdistortion");
            data[offset + 30] = (byte)64;
            data[offset + 31] = (byte)model.get("timbre" + i + "ampkeyboardtrack");
            data[offset + 32] = data[offset + 33] = 0;
            data[offset + 34] = (byte)model.get("timbre" + i + "env1" + "sustain");
            data[offset + 35] = 0;
            data[offset + 36] = (byte)model.get("timbre" + i + "env2" + "attack");
            data[offset + 37] = (byte)model.get("timbre" + i + "env2" + "decay");
            data[offset + 38] = (byte)model.get("timbre" + i + "env2" + "sustain");
            data[offset + 39] = (byte)model.get("timbre" + i + "env2" + "release");
            data[offset + 40] = (byte)((model.get("timbre" + i + "lfo1" + "keysync") << 4 ) | 
                model.get("timbre" + i + "lfo1" + "wave"));
            data[offset + 41] = (byte)model.get("timbre" + i + "lfo1" + "frequency");
            data[offset + 42] = (byte)((model.get("timbre" + i + "lfo1" + "temposync") << 7) |
                // documentation says this is bits 0...4 but this has to be wrong, since the values only go 0...14
                model.get("timbre" + i + "lfo1" + "syncnote"));
            data[offset + 43] = (byte)((model.get("timbre" + i + "lfo2" + "keysync") << 4 ) |
                model.get("timbre" + i + "lfo2" + "wave"));
            data[offset + 44] = (byte)model.get("timbre" + i + "lfo2" + "frequency");
            data[offset + 45] = (byte)((model.get("timbre" + i + "lfo2" + "temposync") << 7) |
                // documentation says this is bits 0...4 but this has to be wrong, since the values only go 0...14
                model.get("timbre" + i + "lfo2" + "syncnote"));
            for(int j = 1; j <= 8; j++)
                {
                // the documentation is very unclear here.  It appears that it says that pairs of MIDI values
                // should be exactly the same.
                data[offset + ((j-1) * 2) + 47] = data[offset + ((j-1) * 2) + 46] = (byte)model.get("channel" + j + "level");
                }
            for(int j = 1; j <= 8; j++)
                {
                // the documentation is very unclear here.  It appears that it says that pairs of MIDI values
                // should be exactly the same.
                data[offset + ((j-1) * 2) + 63] = data[offset + ((j-1) * 2) + 62] = (byte)model.get("channel" + j + "pan");
                }
            for(int j = 1; j <= 16; j++)
                {
                int val = model.get("channel" + j + "hold");
                data[offset + ((j-1) * 4) + 78] =  (byte)((val >>> 24) & 255);
                data[offset + ((j-1) * 4) + 79] =  (byte)((val >>> 16) & 255);
                data[offset + ((j-1) * 4) + 80] =  (byte)((val >>> 8) & 255);
                data[offset + ((j-1) * 4) + 81] =  (byte)((val >>> 0) & 255);
                }
            }   
        
        // Convert 
        byte[] data2 = convertTo7Bit(data);
        data = new byte[6 + data2.length];  // resetting data to new value
        data[0] = (byte)0xF0;
        data[1] = (byte)0x42;
        data[2] = (byte)(48 + getChannelOut());
        data[3] = (byte)0x58;
        data[4] = (byte)0x40;
        data[data.length - 1] = (byte)0xF7;
        System.arraycopy(data2, 0, data, 5, data2.length);
        d[0] = data;
        
        if (!toWorkingMemory && !toFile)
            {
            // The MicroKorg cannot write to a patch directly.  We have to emit to current memory, then save
            // to a patch, so we'll tack some extra sysex on in that situation
                    
            byte BB = (byte) tempModel.get("bank");
            byte NN = (byte) tempModel.get("number");
        
            data = new byte[] { (byte)0xF0, (byte)0x42, (byte)(48 + getChannelOut()), (byte)0x58, (byte)0x11, (byte)0,
                (byte)(BB * 64 + NN), (byte)0xF7 };
            d[1] = data;
            }
        return d;
        }
        
        
    public void parseParameter(byte[] data)
        {
        if (data.length == 6 &&                 // write error report
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x42 &&
            data[3] == (byte)0x58 &&
                (data[4] == (byte)0x26 || 
                data[4] == (byte)0x24 ||
                data[4] == (byte)0x22))
            {
            String error = "Send or Write Failed";
            showSimpleError("Write Failed", error);
            }
        }



    public int getPauseAfterChangePatch() { return 200; }

    public void changePatch(Model tempModel)
        {
        byte NN = (byte)tempModel.get("number");
        byte BB = (byte)tempModel.get("bank");
        try {
            // Number change is PC
            tryToSendMIDI(new ShortMessage(ShortMessage.PROGRAM_CHANGE, getChannelOut(), BB * 64 + NN, 0));
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
        performRequestCurrentDump();
        }
            
    public byte[] requestCurrentDump()
        {
        return new byte[] { (byte)0xF0, 0x42, (byte)(48 + getChannelOut()), 0x58, 0x10, (byte)0xF7 };
        }
    
    
    public static final int EXPECTED_SYSEX_LENGTH = 297;
    public static boolean recognize(byte[] data)
        {
        boolean v = (
            data.length == EXPECTED_SYSEX_LENGTH &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x42 &&
            data[3] == (byte)0x58 &&
            data[4] == (byte)0x40);
        if (v == false) return false;
        
        // now decode.  Are we synth or vocoder?
        data = convertTo8Bit(data, 5);
        int voicemode = (data[16] >>> 4) & 3;
        return (voicemode < 3);  // single or layer (0 or 2)
        }
    
    
    
    
    /////// OTHER ABSTRACT METHODS
    
    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        JComboBox bank = new JComboBox(BANKS);
        bank.setEditable(false);
        bank.setMaximumRowCount(32);
        bank.setSelectedIndex(model.get("bank"));
        
        int numberv = model.get("number");
        JTextField number = new JTextField("" + (numberv / 8 + 1) + "" + (numberv % 8 + 1), 3);
                
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
                showSimpleError(title, "The Patch Number must be an integer 11 ... 78, with no zero or nine digits\n(81...88 are reserved for Vocoder Patches)");
                continue;
                }
            if (n < 11 || n > 78 || n % 10 == 0 || n % 10 == 9)
                {
                showSimpleError(title, "The Patch Number must be an integer 11 ... 78, with no zero or nine digits\n(81...88 are reserved for Vocoder Patches)");
                continue;
                }
                                
            change.set("bank", bank.getSelectedIndex());
            change.set("number", (n / 10 - 1) * 8 + (n % 10 - 1));  // yuk, magic equation
                        
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
        
    public static String getSynthName() { return "Korg MicroKorg"; }
    
    public String getPatchName(Model model) { return model.get("name", "Init"); }
    

    public Model getNextPatchLocation(Model model)
        {
        int bank = model.get("bank");
        int number = model.get("number");
        
        number++;
        if (number >= 56)
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
        
        int number = model.get("number");
        int bank = model.get("bank");
        return (bank == 0 ? "A." : "B.") + (number / 8 + 1) + "" + (number % 8 + 1);
        }
        
    }
    
