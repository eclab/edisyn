/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.korgmicrosampler;

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
   A patch editor, of sorts, for the Korg Microsampler.  Can't send or receive dumps, just
   change a few parameters
        
   @author Sean Luke
*/

public class KorgMicrosampler extends Synth
    {
    public static final int EFFECTS_DIVIDER = 10;
    public static final int OCTAVE = 3;  // starts at octave 3? 
    
    
    ///// NOTE
    ///// The nightmare of settings below are various tables that enable Edisyn to convert
    ///// values of 0...127 into the custom displays that the Microsampler uses for all of
    ///// its parameters.  And that's not all -- many parameters are displayed using a
    ///// complex mathematical conversion function or a sparse but nontrivial table lookup
    ///// in the form of a switch statment.  Korg should be highly embarassed by the low
    ///// quality of its awful sysex.

    public static final int FX_NONE = 0;
    public static final int FX_COMPRESSOR = 1;
    public static final int FX_FILTER = 2;
    public static final int FX_EQ = 3;
    public static final int FX_DISTORTION = 4;
    public static final int FX_DECIMATOR = 5;
    public static final int FX_REVERB = 6;
    public static final int FX_DELAY = 7;
    public static final int FX_LCRDELAY = 8;
    public static final int FX_PANDELAY = 9;
    public static final int FX_MODDELAY = 10;
    public static final int FX_TAPEECHO = 11;
    public static final int FX_CHORUS = 12;
    public static final int FX_FLANGER = 13;
    public static final int FX_VIBRATO = 14;
    public static final int FX_PHASER = 15;
    public static final int FX_TREMOLO = 16;
    public static final int FX_RINGMOD = 17;
    public static final int FX_GRAINSHIFTER = 18;
    public static final int FX_PITCHSHIFTER = 19;
    public static final int FX_TALKMOD = 20;
    public static final int FX_LOOPER = 21;
        
    public static final int[][] FX_PARAMETER_CONTROL_DEFAULT = new int[][] 
    {
    { 0, 1, 1, 4, 2, 1, 1, 1, 1, 1, 1, 1, 2, 4, 2, 4, 2, 1, 2, 1, 1, 1 },
    { 0, 2, 2, 1, 1, 2, 0, 2, 2, 2, 2, 4, 1, 3, 1, 3, 1, 0, 1, 0, 5, 0 }
    };
    public static final String[] INPUT_SELECT = { "Audio In", "Resample" };
    public static final String[] SAMPLE_BPM_SYNC = { "Off", "Time Stretch", "Pitch Change" };
    public static final String[] FX_COMPRESSOR_ENV_SEL_LR = { "Mix", "Individual" };
    public static final String[] FX_FILTER_TYPE = { "LP 24", "LP 18", "LP 12", "HP 12", "BP 12" };
    public static final String[] FX_EQ_TYPE = { "Peaking", "Shelving Low" };
    public static final String[] FX_FILTER_LFO_WAVE = { "Sawtooth", "Square", "Triangle", "Sine", "S&H" };
    public static final String[] FX_DELAY_TYPE = { "Stereo", "Cross" };
    public static final String[] FX_REVERB_TYPE = { "Hall", "Smooth Hall", "Wet Plate", "Dry Plate", "Room", "Bright Room" };
    public static final String[] FX_PHASER_TYPE = { "Blue", "UV-B" };
    public static final String[] FX_RING_MODULATOR_OSC_MODE = { "Fixed", "Note" };
    public static final String[] FX_RING_MODULATOR_OSC_WAVE = { "Saw", "Triangle", "Sine" };
    public static final String[] FX_PITCH_SHIFTER_FEEDBACK_POS = { "Pre", "Post" };
    public static final String[] FX_PITCH_SHIFTER_MODE = { "Slow", "Medium", "Fast" };
    public static final String[] FX_TALK_MODULATOR_VOICE = { "A", "I", "U", "E", "O" };
    public static final String[] FX_LOOPER_SWITCH = { "Record", "Looped Play", "Overdub" };
    public static final String[] FX_LOOPER_LENGTH = { "1/32", "1/16", "1/8", "1/4", "1/2", "1/1", "2/1", "4/1" };
    public static final String[] FX_TYPE = { "None", "Compressor", "Filter", "4-Band EQ", "Distortion", "Decimator", "Reverb", "Delay", "L/C/R Delay", "Auto Panning Delay", "Modulation Delay", "Tape Echo", "Chorus", "Flanger", "Vibrato", "Phaser", "Tremolo", "Ring Modulator", "Grain Shifter", "Pitch Shifter", "Talk Modulator", "Looper" };
    public static final String[][] FX_PARAMETER_CONTROL = 
        { 
        { },
        { "Dry/Wet", "Sensitivity", "Attack" },
        { "Dry/Wet", "Cutoff", "Resonance", "Mod Depth", "Mod Response", "LFO Sync Note " },
        { "Dry/Wet", "B1 Gain", "B2 Gain", "B3 Gain", "B4 Gain" },
        { "Dry/Wet", "Gain", "Pre EQ Gain", "B1 Gain", "B2 Gain", "B3 Frequency" }, // DOUBLE CHECK.  B3 Frequency?  Not Gain?
        { "Dry/Wet", "FS", "Bit", "FS Mod Intensity", "LFO Frquency" },
        { "Dry/Wet", "Time" }, // DOUBLE CHECK.  Does this vary depending on WHICH time?   Ugh.
        { "Dry/Wet", "Time Ratio", "Feedback" },
        { "Dry/Wet", "Time Ratio", "C Feedback" },  // DOUBLE CHECK.  Just C Feedback?
        { "Dry/Wet", "Time Ratio", "Feedback", "Mod Depth", "LFO Frequency" },
        { "Dry/Wet", "Time Ratio", "Feedback", "Mod Depth", "LFO Frequency" }, 
        { "Dry/Wet", "Time Ratio", "Tap 1 Level", "Tap 2 Level", "Feedback", "Saturation" },
        { "Dry/Wet", "Mod Depth", "LFO Frequency" },
        { "Dry/Wet", "Delay", "Mod Depth", "Feedback", "LFO Sync Note" },
        { "Dry/Wet", "Mod Depth", "LFO Frequency" },
        { "Dry/Wet", "Manual", "Mod Depth", "Resonance", "LFO Sync Note" },
        { "Dry/Wet", "Mod Depth", "LFO Sync Note" },
        { "Dry/Wet", "Fixed Frequency", "LFO Intensity", "LFO Frequency" },
        { "Dry/Wet", "Time Ratio", "LFO Frequency" },
        { "Dry/Wet", "Pitch" },
        { "Dry/Wet", "Voice Control", "Voice Top", "Voice Center", "Voice Bottom", "Resonance", "Drive", "Mod Depth", "LFO Sync Note " },
        { "Switch", "Length", "Speed" }
        };
    public static final int[][] FX_PARAMETER_CONTROL_VAL =
        {
        { },
        { 0, 2, 3 },
        { 0, 2, 3, 5, 6, 9 },
        { 0, 5, 8, 11, 15 },
        { 0, 1, 4, 7, 10, 11 },
        { 0, 3, 4, 6, 8 },
        { 0, 2 },
        { 0, 3, 9 },
        { 0, 2, 13 },
        { 0, 2, 8, 9, 11 },
        { 0, 2, 8, 9, 10 },
        { 0, 2, 8, 9, 10, 14 },
        { 0, 1, 2 },
        { 0, 1, 2, 3, 7 },
        { 0, 1, 3 },
        { 0, 2, 3, 4, 8 },
        { 0, 1, 4 },
        { 0, 2, 6, 8 },
        { 0, 2, 7 },
        { 0, 1 },
        { 0, 1, 2, 3, 4, 5, 6, 7, 10 },
        { 0, 1, 2 },
        };
                
    // Completely arbitrary.  :-(
    public static final double[] FX_COMPRESSOR_ATTACK_VAL = 
        {
        0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0, 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 1.9, 2.0, 2.2, 2.4, 2.6, 2.8,
        3.0, 3.2, 3.4, 3.6, 3.8, 4.0, 4.2, 4.4, 4.7, 5.0, 5.3, 5.6, 5.9, 6.2, 6.5, 6.8, 7.1, 7.4, 7.8, 8.2, 8.6, 9.0, 9.4,
        10.0, 10.5, 11.0, 11.5, 12.0, 12.5, 13.0, 13.5, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 29, 30, 31,
        32, 34, 36, 37, 39, 41, 43, 46, 48, 50, 53, 55, 58, 61, 64, 68, 71, 75, 78, 82, 86, 91, 95, 99, 104, 110, 115, 121, 127, 133,
        140, 147, 155, 162, 170, 179, 188, 197, 207, 218, 229, 240, 252, 265, 278, 292, 307, 322, 338, 355, 373, 391, 411, 432, 476, 500
        };              
                
    public static final double[] FX_FILTER_LFO_FREQUENCY =  
        {
        0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09, 0.10, 0.11, 0.12, 0.13, 0.14, 0.15, 0.16, 0.17, 0.18, 0.19, 0.20, 0.21, 0.22, 0.23, 0.24, 0.25,
        .29, .33, .42, .50, .58, .67, .75, .83, .92, 1.00, 1.13, 1.25, 1.38, 1.50, 1.63, 1.75, 1.88, 2.00, 2.13, 2.25, 2.38, 2.50, 2.63, 2.75, 2.88, 3.00, 3.13, 3.25, 3.38, 3.50, 3.63, 3.75, 3.88, 4.00, 4.13, 4.25, 4.38, 4.5, 4.63, 4.75, 4.88,
        5.00, 5.25, 5.50, 5.75, 6.00, 6.25, 6.5, 6.75, 7, 7.25, 7.5, 7.75, 8.0, 8.25, 8.5, 8.75, 9.0, 9.25, 9.5, 9.75,
        10, 10.5, 11, 11.5, 12, 12.5, 13, 13.5, 14, 14.5,
        15, 16, 17, 18, 19,
        20, 21.5, 23, 24.5, 26, 27.5,
        29, 31, 33, 35, 37, 39, 
        41, 44, 47, 50, 53, 
        57, 61,
        65, 70, 75, 80, 85, 90, 95, 100
        };
                
    public static final String[] FX_FILTER_LFO_SYNC_NOTE = { "8/1", "4/1", "2/1", "1/1", "3/4", "1/2", "3/8", "1/3", "1/4", "3/16", "1/6", "1/8", "1/12", "1/16", "1/24", "1/32", "1/64" };

    public static final double[] FX_BAND_EQ_FREQUENCY = 
        {
        .020, .022, 0.25, .028, .032, .036, .040, .045, .050, .056, .063, .071, .080, .090, .100, .112, .125, .140, .160, .180, 
        .200, .224, .250, .280, .315, /* */ .400, .450, .500, .560, .630, .710, .800, .900, 1.00, 1.12, 1.25, 1.40, 1.60, 1.80, 
        2.00, 2.24, 2.50, 2.80, 3.15, /* */ 4.00, 4.50, 5.00, 5.60, 6.30, 7.10, 8.00, 9.00, 10.00, 11.2, 12.5, 14.0, 16.0, 18.0, 
        20.0
        };
                
    // this likely has errors
    public static final double[] FX_DELAY_TIME_RATIO =
        {
        0.5, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0, 5.5, 6.0, 6.5, 7.0, 7.5, 8.0, 8.5, 9.0, 9.5, 10.0,
        11, 12.5, 13, 14, 16.7, 17, 18, 19, 20, 21, 22, 23, 
        25, 28, 30, 32, 33.3, 36, 38, 40, 42, 44, 46, 48, 50,
        53, 56, 59, 62, 65,
        66.6, 72, 75, 78, 81, 85, 89, 92, 95, 98,
        100, 101, 102, 103, 104, 105, 106, 107, 108, 109,
        110, 112, 114, 116, 118, 120, 122,
        123, 125, 128, 130, 133.3, 134, 136, 138, 140, 143, 146, 150,
        152, 155, 158, 161, 164, 167, 170, 173, 176, 179,
        182, 185, 188, 191, 194, 197, 
        200, 205, 210, 215, 220, 225,
        230, 240, 250, 260, 270, 280,
        300, 320, 340, 360, 380, 400
        };
        
    public static final double[] FX_DELAY_TIME_RATIO_SYNC = { 12.5, 16.7, 25.0, 33.3, 50.0, 66.6, 75.5, 100, 125, 133, 150, 200, 250, 300, 400 };
        
    public static final String[] FX_DELAY_DELAY_SYNC = { "1/64", "1/32", "1/24", "1/16", "1/12", "1/8", "1/6", "3/16", "1/4", "1/3", "3/8", "1/2", "3/4", "1/1" };

    public static final int[][] FX_DEFAULT_SETTINGS = new int[][]
    {
    { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // none
    { 100, 0, 70, 19, 50, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
    { 100, 2, 63, 0, 127, 0, 90, 1, 4, 8, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
    { 100, 127, 0, 12, 15, 18, 28, 25, 18, 35, 5, 18, 0, 0, 5, 18, 0, 0, 0, 0 },
    { 100, 50, 56, 0, 0, 20, 20, 0, 50, 30, 0, 55, 8, 0, 30, 0, 0, 0, 0, 0 },
    { 100, 0, 0, 22, 8, 127, 0, 0, 19, 5, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
    { 90, 1, 19, 0, 10, 20, 0, 63, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
    { 70, 0, 1, 60, 45, 45, 7, 5, 5, 90, 0, 127, 127, 0, 0, 0, 0, 0, 0, 0 },
    { 70, 1, 60, 59, 79, 49, 7, 7, 8, 5, 100, 50, 100, 90, 127, 63, 0, 0, 0, 0 },
    { 30, 1, 60, 53, 45, 7, 5, 5, 90, 110, 0, 28, 3, 3, 0, 0, 0, 9, 10, 127 },
    { 70, 1, 60, 53, 45, 7, 7, 5, 90, 31, 0, 34, 18, 0, 0, 0, 0, 0, 0, 0 },
    { 70, 0, 60, 77, 61, 7, 7, 5, 100, 100, 85, 20, 20, 127, 50, 19, 0, 80, 127, 0 },
    { 50, 63, 28, 18, 53, 55, 100, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
    { 50, 10, 63, 63, 0, 1, 28, 3, 3, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0 },
    { 100, 30, 0, 70, 13, 3, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
    { 50, 110, 40, 50, 0, 1, 30, 3, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
    { 100, 127, 0, 52, 5, 3, 0, 0, 0, 18, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
    { 100, 1, 47, 0, 0, 2, 0, 0, 28, 8, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
    { 100, 0, 60, 30, 7, 0, 0, 66, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
    { 100, -12, 0, 0, 60, 0, 7, 8, 0, 0, 2, 0, 127, 0, 0, 0, 0, 0, 0, 0 },
    { 100, 0, 1, 0, 2, 63, 0, 63, 127, 1, 28, 5, 3, 0, 0, 0, 0, 0, 0, 0 },
    { 0, 7, 63, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }
    };
        
    /** DEFAULT SETTINGS for each effect
        1 2 Wet Mix     70 2.0 50
        1 2 Wet LPF12 63 0 127 0 90 on [syncnote: 1/4 lfofreq 0.5] tri 0 off [initphase 0]
        4 1 Wet 127 peak 80 2.0 0 560 3 0 1250 1 0 peak 6300 1 0 
        2 1 Wet 50 16 0.5 0 200 2.5 0 8.0 3.5 0 14.0 13 0 30 
        1 2 Wet off 0 12.0 12 127 0 off [syncnote: 1/2 lfofreq 0.2] sine 0 off [initphase 0]
        1 0 90:10 SmithHall 2.0 10 20 0 63 
        1 2 70:30 stereo on [tr 100 l 1/8 r 1/8, tr 100 l 160 r 160] 90 0 127 127 
        1 2 70:30 on [tr 100 l 3/16 c 1/4 4 1/8, tr 100 l 300 c 500 r 200]100 50 100 90 127 63
        1 2 30:70 on [tr 100 l 1/8 r 1/8, tr 100 l 240 r 160] 90 110 off [syncnote 1/1 lfofreq 0.5] sine 0 off 0 90 10 127
        1 2 70:30 on [tr 100 l 3/16 r 1/8, tr 100 l 240 r 160] 90 30 1.0 180 
        1 4 70:30 on [tr 100 t1d 3/16 td1 1/8, tr 100 t1d 480 t2d 320] 100 100 85 20 20 127 50 0.2 0 80 127
        2 1 50:50 63 0.5 180 9.9 10.5 100 4.0 
        4 3 50:50 1.0 63 63 + on [syncnote: 1/1 lfofreq 0.5] sine 0 off [initphase 0] 20 0 
        2 1 Wet 30 off [syncnote: 1/16 lfofreq 6.0] sine 0 off [initphase 0] 10 
        4 3 50:50 blue 110 40 50 + on [syncnote: 1/1 lfofreq 0.67] tri 0 off [initphase 0] 0 0 
        2 1 Wet 127 on [syncnote: 1/2 lofreq: 3.25] sine 0 off [initphase 0] 180 
        1 0 Wet Fixed [freq: 1.0 offset: 0 fine: 0] sine 0 off [syncnote: 1/4 lfofreq: 0.5] tri 0 off [initphase 0] 0 
        2 1 Wet off [tr 100 d 30, tr 100 d 1/64] off [syncnote: 1/8 lfofreq: 5.0] off [initphase 0]
        1 0 Wet -12 0 off [tr 100 d 0, tr 100 d 1/4] pre 0 fast 0 127 
        1 5 Wet center i a u 63 0 63 127 on [syncnote: 1/2 lfofreq: 0.5] sine 0 off [initphase 0]
        1 0 rec 4/1 1.0 off 
    */      
        
    public KorgMicrosampler()
        {

        // set default parameters for fx
        for(int type = 0; type < FX_TYPE.length; type++)
            for(int param = 0; param < 20; param++)
                {
                model.set("fx" + type + "param" + param, FX_DEFAULT_SETTINGS[type][param]);
                model.setMin("fx" + type + "param" + param, 0);
                model.setMax("fx" + type + "param" + param, 0);
                }

        /// SOUND PANEL
                
        JComponent soundPanel = new SynthPanel(this);
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        hbox.add(addGlobal(Style.COLOR_A()));
        hbox.addLast(addPattern(Style.COLOR_B()));
        vbox.add(hbox);
        
        vbox.add(addSample(Style.COLOR_B()));
                
        vbox.add(addFX(Style.COLOR_C()));

        soundPanel.add(vbox, BorderLayout.CENTER);
        tabs.addTab("MicroSampler", soundPanel);
        
        model.set("name", "Untitled");
        for(int i = 1; i < 37; i ++)
            model.set("sample" + i + "name", "Untitled");
                        
        loadDefaults();        
        }
          
    public String getHTMLResourceFileName() { return "KorgMicrosampler.html"; }      
    public String getDefaultResourceFileName() { return "KorgMicrosampler.init"; }

    public boolean gatherInfo(String title, Model change, boolean writing)
        {
        // can't gather any bank info, so...
        return true;
        }

    /** Add the global patch category (name, id, number, etc.) */
    public JComponent addNameGlobal(Color color)
        {
        Category globalCategory = new Category(this, "Korg Microsampler", color);
        globalCategory.makeUnresettable();

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
                
        comp = new StringComponent("Bank Name", this, "name", 8, "Name must be up to 8 ASCII characters.")
            {
            public boolean isValid(String val)
                {
                if (val.length() > 8) return false;
                for(int i = 0 ; i < val.length(); i++)
                    {
                    char c = val.charAt(i);
                    if (c < 32 || c > 127) return false;
                    }
                return true;
                }
                                
            public void update(String key, Model model)
                {
                super.update(key, model);
                updateTitle();
                }
            };
        VBox vbox = new VBox();
        vbox.add(comp);


        hbox.add(vbox);
        hbox.add(Strut.makeHorizontalStrut(60));
        
        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }
                
                                        
    /** Add the global patch category (name, id, number, etc.) */
    public JComponent addGlobal(Color color)
        {
        Category globalCategory = new Category(this, "Global", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
                
        comp = new LabelledDial("Bank", this, "bankbpm", color, 200, 3000)
            {
            public String map(int val)
                {
                return ("" + (val / 10) + "." + (val % 10));
                }
            public int getDefaultValue() { return 1200; }
            };
        ((LabelledDial)comp).addAdditionalLabel("BPM");
        hbox.add(comp);
        
        comp = new LabelledDial("Current", this, "currentpattern", color, 1, 16);
        ((LabelledDial)comp).addAdditionalLabel("Pattern");
        hbox.add(comp);

        VBox vbox = new VBox();
        comp = new PushButton("Push Record Button")
            {
            public void perform()
                {
                // Microsampler is MSB-only
                Object[] nrpn = buildNRPN(getChannelOut(), (32 << 7) | (2), 127 << 7);
                tryToSendMIDI(nrpn);
                }
            };
        vbox.add(comp);

        comp = new PushButton("Push Sampling Button")
            {
            public void perform()
                {
                // Microsampler is MSB-only
                Object[] nrpn = buildNRPN(getChannelOut(), (32 << 7) | (17), 127 << 7);
                tryToSendMIDI(nrpn);
                }
            };
        vbox.add(comp);

        hbox.add(vbox);

        vbox = new VBox();

        params = INPUT_SELECT;
        comp = new Chooser("Input", this, "inputselect", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new CheckBox("Audio In Uses FX", this, "bankaudioinfxsw");
        ((CheckBox)comp).addToWidth(2);
        vbox.add(comp);

        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }

    
    
    public HBox[] patterns = new HBox[16];
        
    public JComponent addPattern(Color color)
        {
        Category category = new Category(this, "Pattern", color);
        category.makePasteable("pattern1");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        for(int i = 1; i < 17; i++)
            {
            patterns[i - 1] = new HBox();

            comp = new LabelledDial("Length", this, "pattern" + i + "length", color, 1, 99);
            patterns[i - 1].add(comp);

            comp = new LabelledDial("Keyboard", this, "pattern" + i + "keyboardsample", color, 0, 35, -1);
            ((LabelledDial)comp).addAdditionalLabel("Sample");
            patterns[i - 1].addLast(comp);
            }
        
        comp = new LabelledDial("Pattern", this, "pattern", color, 1, 16)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                hbox.removeLast();
                hbox.addLast(patterns[model.get(key, 0) - 1]);
                hbox.revalidate();
                hbox.repaint();
                category.makePasteable("pattern" + model.get(key, 1));
                }
            };
        ((LabelledDial)comp).addAdditionalLabel(" Number ");
        hbox.add(comp);

        category.add(hbox, BorderLayout.WEST);
        return category;
        }


    public HBox[] samples = new HBox[36];
    public JComponent addSample(Color color)
        {
        Category category = new Category(this, "Sample", color);
        category.makePasteable("sample1");
                
        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();
        
        for(int i = 1; i <= 36; i++)
            {
            samples[i - 1] = new HBox();

            VBox vbox = new VBox();
            comp = new StringComponent("Sample Name", this, "sample" + i + "name", 8, "Name must be up to 8 ASCII characters.")
                {
                public boolean isValid(String val)
                    {
                    if (val.length() > 8) return false;
                    for(int i = 0 ; i < val.length(); i++)
                        {
                        char c = val.charAt(i);
                        if (c < 32 || c > 127) return false;
                        }
                    return true;
                    }
                                                                
                public void update(String key, Model model)
                    {
                    super.update(key, model);
                    updateTitle();
                    }
                };
            vbox.add(comp);
                
            params = SAMPLE_BPM_SYNC;
            comp = new Chooser("BPM Sync", this, "sample" + i + "bpmsync", params);
            vbox.add(comp);
            samples[i - 1].add(vbox);
                        
            vbox = new VBox();
            comp = new CheckBox("Loop", this, "sample" + i + "loop");
            vbox.add(comp);    
                
            comp = new CheckBox("Reverse", this, "sample" + i + "reverse");
            vbox.add(comp);    
                
            comp = new CheckBox("FX SW", this, "sample" + i + "fxsw");
            ((CheckBox)comp).addToWidth(1);
            vbox.add(comp);    
                
            samples[i - 1].add(vbox);


            comp = new LabelledDial("Decay", this, "sample" + i + "decay", color, 0, 127);
            samples[i - 1].add(comp);

            comp = new LabelledDial("Release", this, "sample" + i + "release", color, 0, 127);
            samples[i - 1].add(comp);
                
            comp = new LabelledDial("Semitone", this, "sample" + i + "semitone", color, -24, 24)
                {
                public boolean isSymmetric() { return true; }
                };
            samples[i - 1].add(comp);

            //-99                           0 ... 2                 // asymmetric note
            //-95...-50 by 5        3 ... 12
            //-49...-1 by 1         13 ... 61
            //0                                     62 ... 66               // 64 is center?
            //1...49 by 1           67 ... 115
            //50...95 by 5          116 ... 125
            //99                            126 ... 127

            comp = new LabelledDial("Tune", this, "sample" + i + "tune", color, 1, 127, 64)
                {
                public String map(int val)
                    {
                    if (val < 3)
                        return "-99";
                    else if (val < 13)
                        return "" + ((val - 3) * 5 + -95);
                    else if (val < 62)
                        return "" + ((val - 13) + -49);
                    else if (val < 66)
                        return "0";
                    else if (val < 116)
                        return "" + ((val - 67) + 1);
                    else if (val < 126)
                        return "" + ((val - 116) * 5 + 50);
                    else
                        return "99";
                    }
                public boolean isSymmetric() { return true; }   // ALMOST symmetric
                };
            samples[i - 1].add(comp);

            // THIS IS NONLINEAR AND REQUIRES A CUSTOM MAP
            // It's weird :-(
            // -Inf DB                          0               0
            // -74db to -24db   by 1            1 ... 51
            // -23.5db to -0.5  by 0.5          52 ... 98
            // 0                                                        99 ... 103              101 is center?
            // 0.5 to 12db              by 0.5          104 ... 127
    
            comp = new LabelledDial("Level (db)", this, "sample" + i + "level", color, 0, 127)
                {
                public String map(int val)
                    {
                    if (val == 0)
                        return "-Infty";
                    else if (val < 52)
                        return "" + ((val - 1) - 74);
                    else if (val < 99)
                        return "" + (((val - 52) * 0.5) - 23.5);
                    else if (val < 104)
                        return "0";
                    else
                        return "" + (((val - 104) * 0.5) + 0.5);
                    }
                public double getStartAngle() { return 79.7244094350 + 90 + 135; }
                public int getDefaultValue() { return 101; }
                };
            samples[i - 1].add(comp);

            // THIS WILL REQUIRE CUSTOM EMITTING
            comp = new LabelledDial("Velocity", this, "sample" + i + "velint", color, -63, 63)
                {
                public boolean isSymmetric() { return true; }
                };
            ((LabelledDial)comp).addAdditionalLabel("Sensitivity");
            samples[i - 1].add(comp);

            comp = new LabelledDial("Pan", this, "sample" + i + "pan", color, 1, 127)
                {
                public String map(int val)
                    {
                    if (val < 64)
                        return "L " + (64 - val);
                    else if (val > 64)
                        return "R " + (val - 64);
                    else return "--";
                    }
                public boolean isSymmetric() { return true; }
                };
            samples[i - 1].add(comp);
            }

        final VBox v = new VBox();

        // THIS ISN'T EMITTED
        comp = new LabelledDial("Sample", this, "samplenumber", color, 1, 36)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                v.removeLast();
                v.addBottom(samples[model.get(key, 1) - 1]);
                v.revalidate();
                v.repaint();
                category.makePasteable("sample" + model.get(key, 1));
                }
            };
        ((LabelledDial)comp).addAdditionalLabel(" Number ");
        hbox.add(comp);

        hbox.add(Strut.makeHorizontalStrut(10));

        comp = new KeyDisplay("Sample Number", this, "samplenumber", color, 0, 35, 1)
            {
            public void redoTitle(int state) { getLabel().setText(getTitle()); }
            };
        ((KeyDisplay)comp).setDynamicUpdate(true);
        hbox.add(comp);
        v.add(hbox);
        v.add(Strut.makeVerticalStrut(10));
                
    
        category.add(v, BorderLayout.WEST);
        return category;
        }


    public Chooser[] fxControlChoosers = new Chooser[2];
    HBox effects[] = new HBox[22];

    /** Add the FX category */    
    public JComponent addFX(Color color)
        {
        Category category = new Category(this, "Effects", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        HBox next = new HBox();
        VBox levels = new VBox();
        
        
        /// 0. NO EFFECTS
        
        int i = 0;
        effects[i] = new HBox();
        
        
        /// 1. COMPRESSOR
        
        i++;
        effects[i] = new HBox();
        
        comp = new LabelledDial("Dry/Wet", this, "fx" + i + "param0", color, 0, 100)
            {
            public String map(int val) 
                {
                if (val == 0) return "Dry";
                else if (val == 100) return "Wet"; 
                else return "" + (100 - val) + ":" + val;
                }
            };
        effects[i].add(comp);

        vbox = new VBox();
        params = FX_COMPRESSOR_ENV_SEL_LR;
        comp = new Chooser("Envelope Sel LR", this, "fx" + i + "param1", params);
        vbox.add(comp);
        effects[i].add(vbox);

        comp = new LabelledDial("Sensitivity", this, "fx" + i + "param2", color, 0, 127);
        effects[i].add(comp);

        comp = new LabelledDial("Attack", this, "fx" + i + "param3", color, 0, 127)
            {
            public String map(int val) { return "" + FX_COMPRESSOR_ATTACK_VAL[val]; }
            };
        effects[i].add(comp);
                
        comp = new LabelledDial("Output Level", this, "fx" + i + "param4", color, 0, 127);
        effects[i].add(comp);



        /// 2. FILTER

        i++;
        effects[i] = new HBox();
        
        levels = new VBox();
        effects[i].add(levels);

        next = new HBox();
        levels.add(next);
        levels.add(Strut.makeVerticalStrut(EFFECTS_DIVIDER));

        comp = new LabelledDial("Dry/Wet", this, "fx" + i + "param0", color, 0, 100)
            {
            public String map(int val) 
                {
                if (val == 0) return "Dry";
                else if (val == 100) return "Wet"; 
                else return "" + (100 - val) + ":" + val;
                }
            };
        next.add(comp);

        vbox = new VBox();
        params = FX_FILTER_TYPE;
        comp = new Chooser("Filter Type", this, "fx" + i + "param1", params);
        vbox.add(comp);
        next.add(vbox);

        comp = new LabelledDial("Cutoff", this, "fx" + i + "param2", color, 0, 127);
        next.add(comp);

        comp = new LabelledDial("Resonance", this, "fx" + i + "param3", color, 0, 127);
        next.add(comp);
                
        comp = new LabelledDial("Trim", this, "fx" + i + "param4", color, 0, 127);
        next.add(comp);

        comp = new LabelledDial("Mod Depth", this, "fx" + i + "param5", color, -63, 63)
            {
            public boolean isSymmetric() { return true; }
            };
        next.add(comp);

        comp = new LabelledDial("Mod Response", this, "fx" + i + "param6", color, 0, 127);
        next.add(comp);



        next = new HBox();
        levels.add(next);

        final Component compz1 = new LabelledDial(" LFO Frequency ", this, "fx" + i + "param8", color, 0, 127)
            {
            public String map(int val) { return "" + FX_FILTER_LFO_FREQUENCY[val]; }
            };

        final Component compz2 = new LabelledDial(" LFO Sync Note ", this, "fx" + i + "param9", color, 0, 16)
            {
            public String map(int val) { return FX_FILTER_LFO_SYNC_NOTE[val]; }
            };

        final HBox syncbox1 = new HBox();
        comp = new CheckBox("LFO Sync", this, "fx" + i + "param7")      
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                syncbox1.removeAll();
                syncbox1.add(model.get(key, 0) == 0 ? compz1 : compz2);
                syncbox1.revalidate();
                syncbox1.repaint();
                }
            };
        ((CheckBox)comp).addToWidth(2);
        next.add(comp);
        next.add(syncbox1);

        vbox = new VBox();
        params = FX_FILTER_LFO_WAVE;
        comp = new Chooser("LFO Wave", this, "fx" + i + "param10", params);
        vbox.add(comp);
        next.add(vbox);

        comp = new LabelledDial("LFO Shape", this, "fx" + i + "param11", color, -63, 63)
            {
            public boolean isSymmetric() { return true; }
            };
        next.add(comp);

        comp = new CheckBox("LFO Key Sync", this, "fx" + i + "param12");
        ((CheckBox)comp).addToWidth(1);
        next.add(comp);

        comp = new LabelledDial("LFO Init Phase", this, "fx" + i + "param13", color, 0, 18)
            {
            public String map(int val) { return "" + (val * 10); }
            };
        next.add(comp);

                
                

                        
        /// 3. 4-BAND EQ
                
        i++;
        effects[i] = new HBox();
        
        levels = new VBox();
        effects[i].add(levels);

        next = new HBox();
        levels.add(next);
        levels.add(Strut.makeVerticalStrut(EFFECTS_DIVIDER));

        comp = new LabelledDial("Dry/Wet", this, "fx" + i + "param0", color, 0, 100)
            {
            public String map(int val) 
                {
                if (val == 0) return "Dry";
                else if (val == 100) return "Wet"; 
                else return "" + (100 - val) + ":" + val;
                }
            };
        next.add(comp);

        comp = new LabelledDial("Trim", this, "fx" + i + "param1", color, 0, 127);
        next.add(comp);

        vbox = new VBox();
        params = FX_EQ_TYPE;
        comp = new Chooser("B1 Type", this, "fx" + i + "param2", params);
        vbox.add(comp);
        next.add(vbox);

        comp = new LabelledDial("B1 Frequency", this, "fx" + i + "param3", color, 0, 58)
            {
            public String map(int val) { return "" + FX_BAND_EQ_FREQUENCY[val]; }
            };
        next.add(comp);

        comp = new LabelledDial("B1 Q", this, "fx" + i + "param4", color, 0, 95)
            {
            public String map(int val) { return String.format("%2.1f", (val * 0.1 + 0.5)); }
            };
        next.add(comp);

        // irritating that these gains are positive only, but distortion's gains are +/-
        comp = new LabelledDial("B1 Gain", this, "fx" + i + "param5", color, 0, 36);
        next.add(comp);

        comp = new LabelledDial("B2 Frequency", this, "fx" + i + "param6", color, 0, 58)
            {
            public String map(int val) { return "" + FX_BAND_EQ_FREQUENCY[val]; }
            };
        next.add(comp);

        comp = new LabelledDial("B2 Q", this, "fx" + i + "param7", color, 0,  95)
            {
            public String map(int val) { return String.format("%2.1f", (val * 0.1 + 0.5)); }
            };
        next.add(comp);

        comp = new LabelledDial("B2 Gain", this, "fx" + i + "param8", color, 0,  36);
        next.add(comp);

        next = new HBox();
        levels.add(next);

        comp = new LabelledDial("B3 Frequency", this, "fx" + i + "param9", color, 0, 58)
            {
            public String map(int val) { return "" + (FX_BAND_EQ_FREQUENCY[val]); }
            };
        next.add(comp);

        comp = new LabelledDial("B3 Q", this, "fx" + i + "param10", color, 0,  95)
            {
            public String map(int val) { return String.format("%2.1f", (val * 0.1 + 0.5)); }
            };
        next.add(comp);

        comp = new LabelledDial("B3 Gain", this, "fx" + i + "param11", color, 0,  36);
        next.add(comp);

        vbox = new VBox();
        params = FX_EQ_TYPE;
        comp = new Chooser("B4 Type", this, "fx" + i + "param12", params);
        vbox.add(comp);
        next.add(vbox);

        comp = new LabelledDial("B4 Frequency", this, "fx" + i + "param13", color, 0, 58)
            {
            public String map(int val) { return "" + FX_BAND_EQ_FREQUENCY[val]; }
            };
        next.add(comp);

        comp = new LabelledDial("B4 Q", this, "fx" + i + "param14", color, 0, 95)
            {
            public String map(int val) { return String.format("%2.1f", (val * 0.1 + 0.5)); }
            };
        next.add(comp);

        comp = new LabelledDial("B4 Gain", this, "fx" + i + "param15", color, 0, 36);
        next.add(comp);



                        


        /// 4. DISTORTION

        i++;
        effects[i] = new HBox();
        
        levels = new VBox();
        effects[i].add(levels);

        next = new HBox();
        levels.add(next);
        levels.add(Strut.makeVerticalStrut(EFFECTS_DIVIDER));

        comp = new LabelledDial("Dry/Wet", this, "fx" + i + "param0", color, 0, 100)
            {
            public String map(int val) 
                {
                if (val == 0) return "Dry";
                else if (val == 100) return "Wet"; 
                else return "" + (100 - val) + ":" + val;
                }
            };
        next.add(comp);

        comp = new LabelledDial("Gain", this, "fx" + i + "param1", color, 0, 127);
        next.add(comp);

        comp = new LabelledDial("Pre EQ Frequency", this, "fx" + i + "param2", color, 0, 58)
            {
            public String map(int val) { return "" + FX_BAND_EQ_FREQUENCY[val]; }
            };
        next.add(comp);

        comp = new LabelledDial("Pre EQ Q", this, "fx" + i + "param3", color, 0, 95)
            {
            public String map(int val) { return "" + (val * 0.1 + 0.5); }
            };
        next.add(comp);
                
        comp = new LabelledDial("Pre EQ Gain", this, "fx" + i + "param4", color, -36, 36)
            {
            public String map(int val) { return "" + ((val) / 2.0); }
            };
        next.add(comp);

        comp = new LabelledDial("B1 Frequency", this, "fx" + i + "param5", color, 0, 58)
            {
            public String map(int val) { return "" + FX_BAND_EQ_FREQUENCY[val]; }
            };
        next.add(comp);

        comp = new LabelledDial("B1 Q", this, "fx" + i + "param6", color, 0, 95)
            {
            public String map(int val) { return String.format("%2.1f", (val * 0.1 + 0.5)); }
            };
        next.add(comp);

        comp = new LabelledDial("B1 Gain", this, "fx" + i + "param7", color, -36, 36)
            {
            public String map(int val) { return "" + ((val) / 2.0); }
            };
        next.add(comp);

        next = new HBox();
        levels.add(next);

        comp = new LabelledDial("B2 Frequency", this, "fx" + i + "param8", color, 0, 58)
            {
            public String map(int val) { return "" + FX_BAND_EQ_FREQUENCY[val]; }
            };
        next.add(comp);

        comp = new LabelledDial("B2 Q", this, "fx" + i + "param9", color, 0, 95)
            {
            public String map(int val) { return String.format("%2.1f", (val * 0.1 + 0.5)); }
            };
        next.add(comp);

        comp = new LabelledDial("B2 Gain", this, "fx" + i + "param10", color, -36, 36)
            {
            public String map(int val) { return "" + ((val) / 2.0); }
            };
        next.add(comp);

        comp = new LabelledDial("B3 Frequency", this, "fx" + i + "param11", color, 0, 58)
            {
            public String map(int val) { return "" + FX_BAND_EQ_FREQUENCY[val]; }
            };
        next.add(comp);

        comp = new LabelledDial("B3 Q", this, "fx" + i + "param12", color, 0, 95)
            {
            public String map(int val) { return String.format("%2.1f", (val * 0.1 + 0.5)); }
            };
        next.add(comp);

        comp = new LabelledDial("B3 Gain", this, "fx" + i + "param13", color, -36, 36)
            {
            public String map(int val) { return "" + ((val) / 2.0); }
            };
        next.add(comp);

        comp = new LabelledDial("Output Level", this, "fx" + i + "param14", color, 0, 127);
        next.add(comp);
                


                        
                

        /// 5. DECIMATOR

        i++;
        effects[i] = new HBox();
        
        levels = new VBox();
        effects[i].add(levels);

        next = new HBox();
        levels.add(next);
        levels.add(Strut.makeVerticalStrut(EFFECTS_DIVIDER));

        comp = new LabelledDial("Dry/Wet", this, "fx" + i + "param0", color, 0, 100)
            {
            public String map(int val) 
                {
                if (val == 0) return "Dry";
                else if (val == 100) return "Wet"; 
                else return "" + (100 - val) + ":" + val;
                }
            };
        next.add(comp);

        comp = new CheckBox("Pre LPF", this, "fx" + i + "param1");
        next.add(comp);

        comp = new LabelledDial(" High Damp ", this, "fx" + i + "param2", color, 0, 100)
            {
            public String map(int val) { return "" + val + "%"; }
            };
        next.add(comp);

        comp = new LabelledDial("FS", this, "fx" + i + "param3", color, 0, 94)
            {
            public String map(int val) { return "" + (val / 2.0 + 1.0); }
            };
        next.add(comp);
                
        comp = new LabelledDial("Bit", this, "fx" + i + "param4", color, 0, 20, -4);
        next.add(comp);

        comp = new LabelledDial("Output Level", this, "fx" + i + "param5", color, 0, 127);
        next.add(comp);

        comp = new LabelledDial("FS Mod Intensity", this, "fx" + i + "param6", color, -63, 63)
            {
            public boolean isSymmetric() { return true; }
            };
        next.add(comp);

        next = new HBox();
        levels.add(next);

        final Component compy1 = new LabelledDial(" LFO Frequency ", this, "fx" + i + "param8", color, 0, 127)
            {
            public String map(int val) { return "" + FX_FILTER_LFO_FREQUENCY[val]; }
            };

        final Component compy2 = new LabelledDial(" LFO Sync Note ", this, "fx" + i + "param9", color, 0, 16)
            {
            public String map(int val) { return FX_FILTER_LFO_SYNC_NOTE[val]; }
            };

        final HBox syncbox2 = new HBox();
        comp = new CheckBox("LFO Sync", this, "fx" + i + "param7")      
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                syncbox2.removeAll();
                syncbox2.add(model.get(key, 0) == 0 ? compy1 : compy2);
                syncbox2.revalidate();
                syncbox2.repaint();
                }
            };
        ((CheckBox)comp).addToWidth(2);
        next.add(comp);
        next.add(syncbox2);

        vbox = new VBox();
        params = FX_FILTER_LFO_WAVE;
        comp = new Chooser("LFO Wave", this, "fx" + i + "param10", params);
        vbox.add(comp);
        next.add(vbox);

        comp = new LabelledDial("LFO Shape", this, "fx" + i + "param11", color, -63, 63)
            {
            public boolean isSymmetric() { return true; }
            };
        next.add(comp);

        comp = new CheckBox("LFO Key Sync", this, "fx" + i + "param12");
        ((CheckBox)comp).addToWidth(1);
        next.add(comp);

        comp = new LabelledDial("LFO Init Phase", this, "fx" + i + "param13", color, 0, 18)
            {
            public String map(int val) { return "" + (val * 10); }
            };
        next.add(comp);
                

                        


        /// 6. REVERB
                
        i++;
        effects[i] = new HBox();
        
        comp = new LabelledDial("Dry/Wet", this, "fx" + i + "param0", color, 0, 100)
            {
            public String map(int val) 
                {
                if (val == 0) return "Dry";
                else if (val == 100) return "Wet"; 
                else return "" + (100 - val) + ":" + val;
                }
            };
        effects[i].add(comp);


        HBox timehbox = new HBox();
        final JComponent comp1 = new LabelledDial("Time", this, "fx" + i + "param2", color, 0, 99)
            {
            public String map(int val) { return String.format("%2.1f s", ((val + 1) * 0.1)); }
            };
        timehbox.add(comp1);
                
        final JComponent comp2 = new LabelledDial("Time", this, "fx" + i + "param3", color, 0, 100)
            {
            public String map(int val)
                {
                // 0...49       20 ... 1000 by 20
                // 50...100     1000 ... 3000 by 40
                if (val < 50) return "" + ((val + 1) * 20) ;
                else return "" + ((val - 50) * 40 + 1000) ;
                }
            };
                
        vbox = new VBox();
        params = FX_REVERB_TYPE;
        comp = new Chooser("Reverb Type", this, "fx" + i + "param1", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                timehbox.removeAll();
                        
                if (model.get(key, 0) < 4)
                    {
                    timehbox.add(comp1);
                    }
                else
                    {
                    timehbox.add(comp2);
                    }
                timehbox.revalidate();
                timehbox.repaint();
                }
            };
        vbox.add(comp);
        effects[i].add(vbox);
                
        effects[i].add(timehbox);

        comp = new LabelledDial(" High Damp ", this, "fx" + i + "param4", color, 0, 100)
            {
            public String map(int val) { return "" + val + "%"; }
            };
        effects[i].add(comp);

        comp = new LabelledDial("Low Damp", this, "fx" + i + "param5", color, 0, 100)
            {
            public String map(int val) { return "" + val + "%"; }
            };
        effects[i].add(comp);

        comp = new LabelledDial("Pre Delay", this, "fx" + i + "param6", color, 0, 70)
            {
            public String map(int val) { return "" + val ; }
            };
        effects[i].add(comp);

        comp = new LabelledDial("Trim", this, "fx" + i + "param7", color, 0, 127);
        effects[i].add(comp);



                        


        /// 7. DELAY

        i++;
        effects[i] = new HBox();
        
        comp = new LabelledDial("Dry/Wet", this, "fx" + i + "param0", color, 0, 100)
            {
            public String map(int val) 
                {
                if (val == 0) return "Dry";
                else if (val == 100) return "Wet"; 
                else return "" + (100 - val) + ":" + val;
                }
            };
        effects[i].add(comp);


        final HBox timehbox1 = new HBox();

        /*
          0.5  ... 10.0   by 0.5
          11, 12.5, 13. 14, 16.7, 17, 18, 19, 
          10...30 by 10
          33.3, 36, 38, 40...50 by 2
          53...65 by 3
          66.6, 72, 75, 78, 81, 85, 89, 92, 95, 98,
          100 ... 109 by 1
          110 ... 122 by 2
          123, 125, 128, 130, 133.3, 134, 136, 138, 140, 143, 146, 150
          152, 155, 158, 161, 164, 167, 170, 173, 176, 179
          182, 185, 188, 191, 194, 197, 
          200 ... 225 by 5
          230 ... 270 by 10
          280 ... 400 by 20
        */
                
        final JComponent comp1a = new LabelledDial("Time Ratio", this, "fx" + i + "param3", color, 0, 122)    // This is probably wrong, may be missing some?
            {
            public String map(int val) { return FX_DELAY_TIME_RATIO[val] + "%"; }
            };
        timehbox1.add(comp1a);
                
        /*
          0...9   by 1
          10..28  by 2
          30...145        by 5
          150...550       by 10
          560...1400      by 20
        */
        
        final JComponent comp2a = new LabelledDial("L Delay", this, "fx" + i + "param4", color, 0, 127)
            {
            public String map(int val)
                {
                if (val < 10) return "" + val ;
                else if (val < 20) return "" + (((val - 10) * 2) + 10) ;
                else if (val < 44) return "" + (((val - 20) * 5) + 30) ;
                else if (val < 85) return "" + (((val - 44) * 10) + 150) ;
                else return "" + (((val - 84) * 20) + 560) ;
                }
            };
        timehbox1.add(comp2a);

        final JComponent comp3a = new LabelledDial("R Delay", this, "fx" + i + "param5", color, 0, 127)
            {
            public String map(int val)
                {
                if (val < 10) return "" + val ;
                else if (val < 20) return "" + (((val - 10) * 2) + 10) ;
                else if (val < 44) return "" + (((val - 20) * 5) + 30) ;
                else if (val < 85) return "" + (((val - 44) * 10) + 150) ;
                else return "" + (((val - 84) * 20) + 560) ;
                }
            };
        timehbox1.add(comp3a);

        final JComponent comp4a = new LabelledDial("Time Ratio", this, "fx" + i + "param6", color, 0, 14)
            {
            public String map(int val) { return FX_DELAY_TIME_RATIO_SYNC[val] + "%"; }
            };

        final JComponent comp5a = new LabelledDial("L Delay", this, "fx" + i + "param7", color, 0, 13)
            {
            public String map(int val) { return FX_DELAY_DELAY_SYNC[val]; }
            };

        final JComponent comp6a = new LabelledDial("R Delay", this, "fx" + i + "param8", color, 0, 13)
            {
            public String map(int val) { return FX_DELAY_DELAY_SYNC[val]; }
            };


        vbox = new VBox();
        params = FX_DELAY_TYPE;
        comp = new Chooser("Delay Type", this, "fx" + i + "param1", params);
        vbox.add(comp);
        effects[i].add(vbox);

        comp = new CheckBox("BPM Sync", this, "fx" + i + "param2")
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                timehbox1.removeAll();
                if (model.get(key, 0) == 0)
                    {
                    timehbox1.add(comp1a);
                    timehbox1.add(comp2a);
                    timehbox1.add(comp3a);
                    }
                else
                    {
                    timehbox1.add(comp4a);
                    timehbox1.add(comp5a);
                    timehbox1.add(comp6a);
                    }
                timehbox1.revalidate();
                timehbox1.repaint();
                }
            };
        vbox.add(comp);
        effects[i].add(vbox);
                
        effects[i].add(timehbox1);

        comp = new LabelledDial("Feedback", this, "fx" + i + "param9", color, 0, 127);
        effects[i].add(comp);

        comp = new LabelledDial(" High Damp ", this, "fx" + i + "param10", color, 0, 100)
            {
            public String map(int val) { return "" + val + "%"; }
            };
        effects[i].add(comp);

        comp = new LabelledDial("Trim", this, "fx" + i + "param11", color, 0, 127);
        effects[i].add(comp);

        comp = new LabelledDial("Spread", this, "fx" + i + "param12", color, 0, 127);
        effects[i].add(comp);



                        


        /// 8. L/C/R DELAY

        i++;
        effects[i] = new HBox();
        

        levels = new VBox();
        effects[i].add(levels);

        next = new HBox();
        levels.add(next);
        levels.add(Strut.makeVerticalStrut(EFFECTS_DIVIDER));

        comp = new LabelledDial("Dry/Wet", this, "fx" + i + "param0", color, 0, 100)
            {
            public String map(int val) 
                {
                if (val == 0) return "Dry";
                else if (val == 100) return "Wet"; 
                else return "" + (100 - val) + ":" + val;
                }
            };
        next.add(comp);



        final HBox timehbox2 = new HBox();

        final JComponent comp1b = new LabelledDial("Time Ratio", this, "fx" + i + "param2", color, 0, 122)    // This is probably wrong, may be missing some?
            {
            public String map(int val) { return FX_DELAY_TIME_RATIO[val] + "%"; }
            };
        timehbox2.add(comp1b);
                
 
        final JComponent comp2b = new LabelledDial("L Delay", this, "fx" + i + "param3", color, 0, 127)
            {
            public String map(int val)
                {
                if (val < 10) return "" + val ;
                else if (val < 20) return "" + (((val - 10) * 2) + 10) ;
                else if (val < 44) return "" + (((val - 20) * 5) + 30) ;
                else if (val < 85) return "" + (((val - 44) * 10) + 150) ;
                else return "" + (((val - 84) * 20) + 560) ;
                }
            };
        timehbox2.add(comp2b);

        final JComponent comp3b = new LabelledDial("C Delay", this, "fx" + i + "param4", color, 0, 127)
            {
            public String map(int val)
                {
                if (val < 10) return "" + val ;
                else if (val < 20) return "" + (((val - 10) * 2) + 10) ;
                else if (val < 44) return "" + (((val - 20) * 5) + 30) ;
                else if (val < 85) return "" + (((val - 44) * 10) + 150) ;
                else return "" + (((val - 84) * 20) + 560) ;
                }
            };
        timehbox2.add(comp3b);

        final JComponent comp4b = new LabelledDial("R Delay", this, "fx" + i + "param5", color, 0, 127)
            {
            public String map(int val)
                {
                if (val < 10) return "" + val ;
                else if (val < 20) return "" + (((val - 10) * 2) + 10) ;
                else if (val < 44) return "" + (((val - 20) * 5) + 30) ;
                else if (val < 85) return "" + (((val - 44) * 10) + 150) ;
                else return "" + (((val - 84) * 20) + 560) ;
                }
            };
        timehbox2.add(comp4b);

        final JComponent comp5b = new LabelledDial("Time Ratio", this, "fx" + i + "param6", color, 0, 14)
            {
            public String map(int val) { return FX_DELAY_TIME_RATIO_SYNC[val] + "%"; }
            };

        final JComponent comp6b = new LabelledDial("L Delay", this, "fx" + i + "param7", color, 0, 13)
            {
            public String map(int val) { return FX_DELAY_DELAY_SYNC[val]; }
            };

        final JComponent comp7b = new LabelledDial("C Delay", this, "fx" + i + "param8", color, 0, 13)
            {
            public String map(int val) { return FX_DELAY_DELAY_SYNC[val]; }
            };

        final JComponent comp8b = new LabelledDial("R Delay", this, "fx" + i + "param9", color, 0, 13)
            {
            public String map(int val) { return FX_DELAY_DELAY_SYNC[val]; }
            };



        comp = new CheckBox("BPM Sync", this, "fx" + i + "param1")
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                timehbox2.removeAll();
                if (model.get(key, 0) == 0)
                    {
                    timehbox2.add(comp1b);
                    timehbox2.add(comp2b);
                    timehbox2.add(comp3b);
                    timehbox2.add(comp4b);
                    }
                else
                    {
                    timehbox2.add(comp5b);
                    timehbox2.add(comp6b);
                    timehbox2.add(comp7b);
                    timehbox2.add(comp8b);
                    }
                timehbox2.revalidate();
                timehbox2.repaint();
                }
            };
        next.add(comp);

        next.add(timehbox2);


        next = new HBox();
        levels.add(next);

        comp = new LabelledDial("L Level", this, "fx" + i + "param10", color, 0, 127);
        next.add(comp);

        comp = new LabelledDial("C Level", this, "fx" + i + "param11", color, 0, 127);
        next.add(comp);

        comp = new LabelledDial("R Level", this, "fx" + i + "param12", color, 0, 127);
        next.add(comp);

        comp = new LabelledDial("C Feedback", this, "fx" + i + "param13", color, 0, 127);
        next.add(comp);

        comp = new LabelledDial("Trim", this, "fx" + i + "param14", color, 0, 127);
        next.add(comp);

        comp = new LabelledDial("Spread", this, "fx" + i + "param15", color, 0, 127);
        next.add(comp);



                        



        /// 9. AUTO PANNING DELAY

        i++;
        effects[i] = new HBox();
        
        levels = new VBox();
        effects[i].add(levels);

        next = new HBox();
        levels.add(next);
        levels.add(Strut.makeVerticalStrut(EFFECTS_DIVIDER));

        comp = new LabelledDial("Dry/Wet", this, "fx" + i + "param0", color, 0, 100)
            {
            public String map(int val) 
                {
                if (val == 0) return "Dry";
                else if (val == 100) return "Wet"; 
                else return "" + (100 - val) + ":" + val;
                }
            };
        next.add(comp);




        final HBox timehbox3 = new HBox();

        final JComponent comp1c = new LabelledDial("Time Ratio", this, "fx" + i + "param2", color, 0, 122)    // This is probably wrong, may be missing some?
            {
            public String map(int val) { return FX_DELAY_TIME_RATIO[val] + "%"; }
            };
        timehbox3.add(comp1c);
                
        final JComponent comp2c = new LabelledDial("L Delay", this, "fx" + i + "param3", color, 0, 127)
            {
            public String map(int val)
                {
                if (val < 10) return "" + val ;
                else if (val < 20) return "" + (((val - 10) * 2) + 10) ;
                else if (val < 44) return "" + (((val - 20) * 5) + 30) ;
                else if (val < 85) return "" + (((val - 44) * 10) + 150) ;
                else return "" + (((val - 84) * 20) + 560) ;
                }
            };
        timehbox3.add(comp2c);

        final JComponent comp3c = new LabelledDial("R Delay", this, "fx" + i + "param4", color, 0, 127)
            {
            public String map(int val)
                {
                if (val < 10) return "" + val ;
                else if (val < 20) return "" + (((val - 10) * 2) + 10) ;
                else if (val < 44) return "" + (((val - 20) * 5) + 30) ;
                else if (val < 85) return "" + (((val - 44) * 10) + 150) ;
                else return "" + (((val - 84) * 20) + 560) ;
                }
            };
        timehbox3.add(comp3c);

        final JComponent comp4c = new LabelledDial("Time Ratio", this, "fx" + i + "param5", color, 0, 14)
            {
            public String map(int val) { return FX_DELAY_TIME_RATIO_SYNC[val] + "%"; }
            };

        final JComponent comp5c = new LabelledDial("L Delay", this, "fx" + i + "param6", color, 0, 13)
            {
            public String map(int val) { return FX_DELAY_DELAY_SYNC[val]; }
            };

        final JComponent comp6c = new LabelledDial("R Delay", this, "fx" + i + "param7", color, 0, 13)
            {
            public String map(int val) { return FX_DELAY_DELAY_SYNC[val]; }
            };






        comp = new CheckBox("BPM Sync", this, "fx" + i + "param1")
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                timehbox3.removeAll();
                if (model.get(key, 0) == 0)
                    {
                    timehbox3.add(comp1c);
                    timehbox3.add(comp2c);
                    timehbox3.add(comp3c);
                    }
                else
                    {
                    timehbox3.add(comp4c);
                    timehbox3.add(comp5c);
                    timehbox3.add(comp6c);
                    }
                timehbox3.revalidate();
                timehbox3.repaint();
                }
            };
        next.add(comp);
                
        next.add(timehbox3);


        comp = new LabelledDial("Feedback", this, "fx" + i + "param8", color, 0, 127);
        next.add(comp);

        comp = new LabelledDial("Mod Depth", this, "fx" + i + "param9", color, 0, 127);
        next.add(comp);


        next = new HBox();
        levels.add(next);



        final Component compx1 = new LabelledDial(" LFO Frequency ", this, "fx" + i + "param11", color, 0, 127)
            {
            public String map(int val) { return "" + FX_FILTER_LFO_FREQUENCY[val]; }
            };

        final Component compx2 = new LabelledDial(" LFO Sync Note ", this, "fx" + i + "param12", color, 0, 16)
            {
            public String map(int val) { return FX_FILTER_LFO_SYNC_NOTE[val]; }
            };

        final HBox syncbox3 = new HBox();
        comp = new CheckBox("LFO Sync", this, "fx" + i + "param10")     
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                syncbox3.removeAll();
                syncbox3.add(model.get(key, 0) == 0 ? compx1 : compx2);
                syncbox3.revalidate();
                syncbox3.repaint();
                }
            };
        ((CheckBox)comp).addToWidth(2);
        next.add(comp);
        next.add(syncbox3);

        vbox = new VBox();
        params = FX_FILTER_LFO_WAVE;
        comp = new Chooser("LFO Wave", this, "fx" + i + "param13", params);
        vbox.add(comp);
        next.add(vbox);

        comp = new LabelledDial("LFO Shape", this, "fx" + i + "param14", color, -63, 63)
            {
            public boolean isSymmetric() { return true; }
            };
        next.add(comp);

        comp = new CheckBox("LFO Key Sync", this, "fx" + i + "param15");
        ((CheckBox)comp).addToWidth(1);
        next.add(comp);

        comp = new LabelledDial("LFO Init Phase", this, "fx" + i + "param16", color, 0, 18)
            {
            public String map(int val) { return "" + (val * 10); }
            };
        next.add(comp);

        comp = new LabelledDial("LFO Spread", this, "fx" + i + "param17", color, -18, 18)
            {
            public String map(int val) { return "" + ((val) * 10); }
            public boolean isSymmetric() { return true; }
            };
        next.add(comp);

        comp = new LabelledDial(" High Damp ", this, "fx" + i + "param18", color, 0, 100)
            {
            public String map(int val) { return "" + val + "%"; }
            };
        next.add(comp);

        comp = new LabelledDial("Trim", this, "fx" + i + "param19", color, 0, 127);
        next.add(comp);



                        



        /// 10. MODULATION DELAY

        i++;
        effects[i] = new HBox();

        levels = new VBox();
        next = new HBox();
                
        comp = new LabelledDial("Dry/Wet", this, "fx" + i + "param0", color, 0, 100)
            {
            public String map(int val) 
                {
                if (val == 0) return "Dry";
                else if (val == 100) return "Wet"; 
                else return "" + (100 - val) + ":" + val;
                }
            };
        next.add(comp);






        final HBox timehbox4 = new HBox();

        final JComponent comp1d = new LabelledDial("Time Ratio", this, "fx" + i + "param2", color, 0, 122)    // This is probably wrong, may be missing some?
            {
            public String map(int val) { return FX_DELAY_TIME_RATIO[val] + "%"; }
            };
        timehbox4.add(comp1d);
                
        final JComponent comp2d = new LabelledDial("L Delay", this, "fx" + i + "param3", color, 0, 127)
            {
            public String map(int val)
                {
                if (val < 10) return "" + val ;
                else if (val < 20) return "" + (((val - 10) * 2) + 10) ;
                else if (val < 44) return "" + (((val - 20) * 5) + 30) ;
                else if (val < 85) return "" + (((val - 44) * 10) + 150) ;
                else return "" + (((val - 84) * 20) + 560) ;
                }
            };
        timehbox4.add(comp2d);

        final JComponent comp3d = new LabelledDial("R Delay", this, "fx" + i + "param4", color, 0, 127)
            {
            public String map(int val)
                {
                if (val < 10) return "" + val ;
                else if (val < 20) return "" + (((val - 10) * 2) + 10) ;
                else if (val < 44) return "" + (((val - 20) * 5) + 30) ;
                else if (val < 85) return "" + (((val - 44) * 10) + 150) ;
                else return "" + (((val - 84) * 20) + 560) ;
                }
            };
        timehbox4.add(comp3d);

        final JComponent comp4d = new LabelledDial("Time Ratio", this, "fx" + i + "param5", color, 0, 14)
            {
            public String map(int val) { return FX_DELAY_TIME_RATIO_SYNC[val] + "%"; }
            };

        final JComponent comp5d = new LabelledDial("L Delay", this, "fx" + i + "param6", color, 0, 13)
            {
            public String map(int val) { return FX_DELAY_DELAY_SYNC[val]; }
            };

        final JComponent comp6d = new LabelledDial("R Delay", this, "fx" + i + "param7", color, 0, 13)
            {
            public String map(int val) { return FX_DELAY_DELAY_SYNC[val]; }
            };






        comp = new CheckBox("BPM Sync", this, "fx" + i + "param1")
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                timehbox4.removeAll();
                if (model.get(key, 0) == 0)
                    {
                    timehbox4.add(comp1d);
                    timehbox4.add(comp2d);
                    timehbox4.add(comp3d);
                    }
                else
                    {
                    timehbox4.add(comp4d);
                    timehbox4.add(comp5d);
                    timehbox4.add(comp6d);
                    }
                timehbox4.revalidate();
                timehbox4.repaint();
                }
            };
        next.add(comp);
                
        next.add(timehbox4);


        comp = new LabelledDial("Feedback", this, "fx" + i + "param8", color, 0, 127);
        next.add(comp);

        comp = new LabelledDial("Mod Depth", this, "fx" + i + "param9", color, 0, 127);
        next.add(comp);
        levels.add(next);
                
        next = new HBox();
        comp = new LabelledDial(" LFO Frequency ", this, "fx" + i + "param11", color, 0, 127)
            {
            public String map(int val) { return "" + FX_FILTER_LFO_FREQUENCY[val]; }
            };
        next.add(comp);

        comp = new LabelledDial("LFO Spread", this, "fx" + i + "param12", color, -18, 18)
            {
            public String map(int val) { return "" + ((val) * 10); }
            public boolean isSymmetric() { return true; }
            };
        next.add(comp);
                
        levels.add(next);
        effects[i].add(levels);

                        
                
                
        /// 11. TAPE ECHO

        i++;
        effects[i] = new HBox();
        
        levels = new VBox();
        effects[i].add(levels);

        next = new HBox();
        levels.add(next);
        levels.add(Strut.makeVerticalStrut(EFFECTS_DIVIDER));

        comp = new LabelledDial("Dry/Wet", this, "fx" + i + "param0", color, 0, 100)
            {
            public String map(int val) 
                {
                if (val == 0) return "Dry";
                else if (val == 100) return "Wet"; 
                else return "" + (100 - val) + ":" + val;
                }
            };
        next.add(comp);




        final HBox timehbox5 = new HBox();

        final JComponent comp1e = new LabelledDial("Time Ratio", this, "fx" + i + "param2", color, 0, 122)    // This is probably wrong, may be missing some?
            {
            public String map(int val) { return FX_DELAY_TIME_RATIO[val] + "%"; }
            };
        timehbox5.add(comp1e);
                
        final JComponent comp2e = new LabelledDial("Tap 1 Delay", this, "fx" + i + "param3", color, 0, 127)
            {
            public String map(int val)
                {
                if (val < 10) return "" + val ;
                else if (val < 20) return "" + (((val - 10) * 2) + 10) ;
                else if (val < 44) return "" + (((val - 20) * 5) + 30) ;
                else if (val < 85) return "" + (((val - 44) * 10) + 150) ;
                else return "" + (((val - 84) * 20) + 560) ;
                }
            };
        timehbox5.add(comp2e);

        final JComponent comp3e = new LabelledDial("Tap 2 Delay", this, "fx" + i + "param4", color, 0, 127)
            {
            public String map(int val)
                {
                if (val < 10) return "" + val ;
                else if (val < 20) return "" + (((val - 10) * 2) + 10) ;
                else if (val < 44) return "" + (((val - 20) * 5) + 30) ;
                else if (val < 85) return "" + (((val - 44) * 10) + 150) ;
                else return "" + (((val - 84) * 20) + 560) ;
                }
            };
        timehbox5.add(comp3e);

        final JComponent comp4e = new LabelledDial("Time Ratio", this, "fx" + i + "param5", color, 0, 14)
            {
            public String map(int val) { return FX_DELAY_TIME_RATIO_SYNC[val] + "%"; }
            };

        final JComponent comp5e = new LabelledDial("Tap 1 Delay", this, "fx" + i + "param6", color, 0, 13)
            {
            public String map(int val) { return FX_DELAY_DELAY_SYNC[val]; }
            };

        final JComponent comp6e = new LabelledDial("Tap 2 Delay", this, "fx" + i + "param7", color, 0, 13)
            {
            public String map(int val) { return FX_DELAY_DELAY_SYNC[val]; }
            };






        comp = new CheckBox("BPM Sync", this, "fx" + i + "param1")
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                timehbox5.removeAll();
                if (model.get(key, 0) == 0)
                    {
                    timehbox5.add(comp1e);
                    timehbox5.add(comp2e);
                    timehbox5.add(comp3e);
                    }
                else
                    {
                    timehbox5.add(comp4e);
                    timehbox5.add(comp5e);
                    timehbox5.add(comp6e);
                    }
                timehbox5.revalidate();
                timehbox5.repaint();
                }
            };
        next.add(comp);
                
        next.add(timehbox5);




        comp = new LabelledDial("Tap 1 Level", this, "fx" + i + "param8", color, 0, 127);
        next.add(comp);

        comp = new LabelledDial("Tap 2 Level", this, "fx" + i + "param9", color, 0, 127);
        next.add(comp);

        comp = new LabelledDial("Feedback", this, "fx" + i + "param10", color, 0, 127);
        next.add(comp);

        next = new HBox();
        levels.add(next);

        comp = new LabelledDial(" High Damp ", this, "fx" + i + "param11", color, 0, 100)
            {
            public String map(int val) { return "" + val + "%"; }
            };
        next.add(comp);

        comp = new LabelledDial("Low Damp", this, "fx" + i + "param12", color, 0, 100)
            {
            public String map(int val) { return "" + val + "%"; }
            };
        next.add(comp);

        comp = new LabelledDial("Trim", this, "fx" + i + "param13", color, 0, 127);
        next.add(comp);

        comp = new LabelledDial("Saturation", this, "fx" + i + "param14", color, 0, 127);
        next.add(comp);

        comp = new LabelledDial("Wow Frequency", this, "fx" + i + "param15", color, 0, 127)
            {
            public String map(int val) { return "" + FX_FILTER_LFO_FREQUENCY[val]; }
            };
        next.add(comp);

        comp = new LabelledDial("Wow Depth", this, "fx" + i + "param16", color, 0, 127);
        next.add(comp);

        comp = new LabelledDial("Pre Tone", this, "fx" + i + "param17", color, 0, 127);
        next.add(comp);

        comp = new LabelledDial("Spread", this, "fx" + i + "param18", color, 0, 127);
        next.add(comp);



                        



        /// 12. CHORUS

        i++;
        effects[i] = new HBox();
        
        comp = new LabelledDial("Dry/Wet", this, "fx" + i + "param0", color, 0, 100)
            {
            public String map(int val) 
                {
                if (val == 0) return "Dry";
                else if (val == 100) return "Wet"; 
                else return "" + (100 - val) + ":" + val;
                }
            };
        effects[i].add(comp);

        comp = new LabelledDial("Mod Depth", this, "fx" + i + "param1", color, 0, 127);
        effects[i].add(comp);

        comp = new LabelledDial(" LFO Frequency ", this, "fx" + i + "param2", color, 0, 127)
            {
            public String map(int val) { return "" + FX_FILTER_LFO_FREQUENCY[val]; }
            };
        effects[i].add(comp);
                
        comp = new LabelledDial("LFO Spread", this, "fx" + i + "param3", color, -18, 18)
            {
            public String map(int val) { return "" + ((val) * 10); }
            public boolean isSymmetric() { return true; }
            };
        effects[i].add(comp);

        //      0.0 to 1.9 by 0.1               // 20
        //  2.0 to 5.8 by 0.2           // 20
        //  6.0 to 11.7 by 0.3          // 20
        //  12 to 32.5 by 0.5           // 42
        //  33 to 50 by 1.0                     // 18           == 120 total

        comp = new LabelledDial("Pre Delay L", this, "fx" + i + "param4", color, 0, 119)
            {
            public String map(int val)
                {
                if (val < 20) return String.format("%2.1f", (val * 0.1));
                else if (val < 40) return String.format("%2.1f", ((val - 20) * 0.2 + 2.0));
                else if (val < 60) return String.format("%2.1f", ((val - 40) * 0.3 + 6.0));
                else if (val < 102) return String.format("%2.1f", ((val - 60) * 0.5 + 12.0));
                else return String.format("%2.1f", ((val - 102) + 33) * 1.0);
                }
            };
        effects[i].add(comp);

        comp = new LabelledDial("Pre Delay R", this, "fx" + i + "param5", color, 0, 120)
            {
            public String map(int val)
                {
                if (val < 20) return String.format("%2.1f", (val * 0.1));
                else if (val < 40) return String.format("%2.1f", ((val - 20) * 0.2 + 2.0));
                else if (val < 60) return String.format("%2.1f", ((val - 40) * 0.3 + 6.0));
                else if (val < 102) return String.format("%2.1f", ((val - 60) * 0.5 + 12.0));
                else return String.format("%2.1f", ((val - 102) + 33) * 1.0);
                }
            };
        effects[i].add(comp);

        comp = new LabelledDial("Trim", this, "fx" + i + "param6", color, 0, 127);
        effects[i].add(comp);

        comp = new LabelledDial("Hi EQ Gain", this, "fx" + i + "param7", color, -30, 30)
            {
            public String map(int val) { return "" + ((val) / 2.0); }
            };
        effects[i].add(comp);



                        


        /// 13. FLANGER

        i++;
        effects[i] = new HBox();
        
        levels = new VBox();
        effects[i].add(levels);

        next = new HBox();
        levels.add(next);
        levels.add(Strut.makeVerticalStrut(EFFECTS_DIVIDER));

        comp = new LabelledDial("Dry/Wet", this, "fx" + i + "param0", color, 0, 100)
            {
            public String map(int val) 
                {
                if (val == 0) return "Dry";
                else if (val == 100) return "Wet"; 
                else return "" + (100 - val) + ":" + val;
                }
            };
        next.add(comp);

        //      0.0 to 5.9 by 0.1               // 60           0 ... 59
        //  6.0 to 8.8 by 0.2           // 15           60 ... 74
        //  9 to 16.2 by 0.3            // 25           75 ... 99
        //  16.5 to 19.5 by 0.5         // 7            100 ... 106
        //      20 to 21 by 1.0                 // 2            107 ... 108
        //  22 to 30 by 2.0                     // 5            109 ... 113

        comp = new LabelledDial("Delay", this, "fx" + i + "param1", color, 0, 117)
            {
            public String map(int val)
                {
                if (val < 60) return String.format("%2.1f", (val * 0.1));
                else if (val < 75) return String.format("%2.1f", ((val - 60) * 0.2 + 2.0));
                else if (val < 100) return String.format("%2.1f", ((val - 75) * 0.3 + 9.0));
                else if (val < 107) return String.format("%2.1f", ((val - 100) * 0.5 + 16.5));
                else if (val < 109) return String.format("%2.1f", (val - 107) * 1.0 + 20.0);
                else return String.format("%2.1f", ((val - 109) * 2.0 + 22.0) * 1.0);
                }
            };
        next.add(comp);

        comp = new LabelledDial("Mod Depth", this, "fx" + i + "param2", color, 0, 127);
        next.add(comp);
                
        comp = new LabelledDial("Feedback", this, "fx" + i + "param3", color, 0, 127);
        next.add(comp);

        comp = new CheckBox("Positive Phase", this, "fx" + i + "param4", true);
        next.add(comp);


        next = new HBox();
        levels.add(next);





        final Component compw1 = new LabelledDial(" LFO Frequency ", this, "fx" + i + "param6", color, 0, 127)
            {
            public String map(int val) { return "" + FX_FILTER_LFO_FREQUENCY[val]; }
            };

        final Component compw2 = new LabelledDial(" LFO Sync Note ", this, "fx" + i + "param7", color, 0, 16)
            {
            public String map(int val) { return FX_FILTER_LFO_SYNC_NOTE[val]; }
            };

        final HBox syncbox4 = new HBox();
        comp = new CheckBox("LFO Sync", this, "fx" + i + "param5")      
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                syncbox4.removeAll();
                syncbox4.add(model.get(key, 0) == 0 ? compw1 : compw2);
                syncbox4.revalidate();
                syncbox4.repaint();
                }
            };
        ((CheckBox)comp).addToWidth(2);
        next.add(comp);
        next.add(syncbox4);

        vbox = new VBox();
        params = FX_FILTER_LFO_WAVE;
        comp = new Chooser("LFO Wave", this, "fx" + i + "param8", params);
        vbox.add(comp);
        next.add(vbox);

        comp = new LabelledDial("LFO Shape", this, "fx" + i + "param9", color, -63, 63)
            {
            public boolean isSymmetric() { return true; }
            };
        next.add(comp);

        comp = new CheckBox("LFO Key Sync", this, "fx" + i + "param10");
        ((CheckBox)comp).addToWidth(1);
        next.add(comp);

        comp = new LabelledDial("LFO Init Phase", this, "fx" + i + "param11", color, 0, 18)
            {
            public String map(int val) { return "" + (val * 10); }
            };
        next.add(comp);

        comp = new LabelledDial("LFO Spread", this, "fx" + i + "param12", color, -18, 18)
            {
            public String map(int val) { return "" + ((val) * 10); }
            public boolean isSymmetric() { return true; }
            };
        next.add(comp);

        comp = new LabelledDial(" High Damp ", this, "fx" + i + "param13", color, 0,100)
            {
            public String map(int val) { return "" + val + "%"; }
            };
        next.add(comp);



                        
                

        /// 14. VIBRATO

        i++;
        effects[i] = new HBox();
        
        levels = new VBox();
        effects[i].add(levels);

        next = new HBox();
        levels.add(next);
        levels.add(Strut.makeVerticalStrut(EFFECTS_DIVIDER));

        comp = new LabelledDial("Dry/Wet", this, "fx" + i + "param0", color, 0, 100)
            {
            public String map(int val) 
                {
                if (val == 0) return "Dry";
                else if (val == 100) return "Wet"; 
                else return "" + (100 - val) + ":" + val;
                }
            };
        next.add(comp);

        comp = new LabelledDial("Mod Depth", this, "fx" + i + "param1", color, 0, 127);
        next.add(comp);


        next = new HBox();
        levels.add(next);




        final Component compv1 = new LabelledDial(" LFO Frequency ", this, "fx" + i + "param3", color, 0, 127)
            {
            public String map(int val) { return "" + FX_FILTER_LFO_FREQUENCY[val]; }
            };

        final Component compv2 = new LabelledDial(" LFO Sync Note ", this, "fx" + i + "param4", color, 0, 16)
            {
            public String map(int val) { return FX_FILTER_LFO_SYNC_NOTE[val]; }
            };

        final HBox syncbox5 = new HBox();
        comp = new CheckBox("LFO Sync", this, "fx" + i + "param2")      
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                syncbox5.removeAll();
                syncbox5.add(model.get(key, 0) == 0 ? compv1 : compv2);
                syncbox5.revalidate();
                syncbox5.repaint();
                }
            };
        ((CheckBox)comp).addToWidth(2);
        next.add(comp);
        next.add(syncbox5);

        vbox = new VBox();
        params = FX_FILTER_LFO_WAVE;
        comp = new Chooser("LFO Wave", this, "fx" + i + "param5", params);
        vbox.add(comp);
        next.add(vbox);

        comp = new LabelledDial("LFO Shape", this, "fx" + i + "param6", color, -63, 63)
            {
            public boolean isSymmetric() { return true; }
            };
        next.add(comp);

        comp = new CheckBox("LFO Key Sync", this, "fx" + i + "param7");
        ((CheckBox)comp).addToWidth(1);
        next.add(comp);

        comp = new LabelledDial("LFO Init Phase", this, "fx" + i + "param8", color, 0, 18)
            {
            public String map(int val) { return "" + (val * 10); }
            };
        next.add(comp);

        comp = new LabelledDial("LFO Spread", this, "fx" + i + "param9", color, -18, 18)
            {
            public String map(int val) { return "" + ((val) * 10); }
            public boolean isSymmetric() { return true; }
            };
        next.add(comp);



                        
                

        /// 15. PHASER

        i++;
        effects[i] = new HBox();
        
        levels = new VBox();
        effects[i].add(levels);

        next = new HBox();
        levels.add(next);
        levels.add(Strut.makeVerticalStrut(EFFECTS_DIVIDER));

        comp = new LabelledDial("Dry/Wet", this, "fx" + i + "param0", color, 0, 100)
            {
            public String map(int val) 
                {
                if (val == 0) return "Dry";
                else if (val == 100) return "Wet"; 
                else return "" + (100 - val) + ":" + val;
                }
            };
        next.add(comp);

        vbox = new VBox();
        params = FX_PHASER_TYPE;
        comp = new Chooser("Phaser Type", this, "fx" + i + "param1", params);
        vbox.add(comp);
        next.add(vbox);

        comp = new LabelledDial("Manual", this, "fx" + i + "param2", color, 0, 127);
        next.add(comp);

        comp = new LabelledDial("Mod Depth", this, "fx" + i + "param3", color, 0, 127);
        next.add(comp);

        comp = new LabelledDial("Resonance", this, "fx" + i + "param4", color, 0, 127);
        next.add(comp);

        comp = new CheckBox("Positive Phase", this, "fx" + i + "param5", true);
        next.add(comp);



        next = new HBox();
        levels.add(next);



        final Component compu1 = new LabelledDial(" LFO Frequency ", this, "fx" + i + "param7", color, 0, 127)
            {
            public String map(int val) { return "" + FX_FILTER_LFO_FREQUENCY[val]; }
            };

        final Component compu2 = new LabelledDial(" LFO Sync Note ", this, "fx" + i + "param8", color, 0, 16)
            {
            public String map(int val) { return FX_FILTER_LFO_SYNC_NOTE[val]; }
            };

        final HBox syncbox6 = new HBox();
        comp = new CheckBox("LFO Sync", this, "fx" + i + "param6")      
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                syncbox6.removeAll();
                syncbox6.add(model.get(key, 0) == 0 ? compu1 : compu2);
                syncbox6.revalidate();
                syncbox6.repaint();
                }
            };
        ((CheckBox)comp).addToWidth(2);
        next.add(comp);
        next.add(syncbox6);

        vbox = new VBox();
        params = FX_FILTER_LFO_WAVE;
        comp = new Chooser("LFO Wave", this, "fx" + i + "param9", params);
        vbox.add(comp);
        next.add(vbox);

        comp = new LabelledDial("LFO Shape", this, "fx" + i + "param10", color, -63, 63)
            {
            public boolean isSymmetric() { return true; }
            };
        next.add(comp);

        comp = new CheckBox("LFO Key Sync", this, "fx" + i + "param11");
        ((CheckBox)comp).addToWidth(1);
        next.add(comp);

        comp = new LabelledDial("LFO Init Phase", this, "fx" + i + "param12", color, 0, 18)
            {
            public String map(int val) { return "" + (val * 10); }
            };
        next.add(comp);

        comp = new LabelledDial("LFO Spread", this, "fx" + i + "param13", color, -18, 18)
            {
            public String map(int val) { return "" + ((val) * 10); }
            public boolean isSymmetric() { return true; }
            };
        next.add(comp);

        comp = new LabelledDial(" High Damp ", this, "fx" + i + "param14", color, 0, 100)
            {
            public String map(int val) { return "" + val + "%"; }
            };
        next.add(comp);



                        


        /// 16. TREMOLO

        i++;
        effects[i] = new HBox();
        

        levels = new VBox();
        effects[i].add(levels);

        next = new HBox();
        levels.add(next);
        levels.add(Strut.makeVerticalStrut(EFFECTS_DIVIDER));


        comp = new LabelledDial("Dry/Wet", this, "fx" + i + "param0", color, 0, 100)
            {
            public String map(int val) 
                {
                if (val == 0) return "Dry";
                else if (val == 100) return "Wet"; 
                else return "" + (100 - val) + ":" + val;
                }
            };
        next.add(comp);

        comp = new LabelledDial("Mod Depth", this, "fx" + i + "param1", color, 0, 127);
        next.add(comp);


        next = new HBox();
        levels.add(next);



        final Component compt1 = new LabelledDial(" LFO Frequency ", this, "fx" + i + "param3", color, 0, 127)
            {
            public String map(int val) { return "" + FX_FILTER_LFO_FREQUENCY[val]; }
            };

        final Component compt2 = new LabelledDial(" LFO Sync Note ", this, "fx" + i + "param4", color, 0, 16)
            {
            public String map(int val) { return FX_FILTER_LFO_SYNC_NOTE[val]; }
            };

        final HBox syncbox7 = new HBox();
        comp = new CheckBox("LFO Sync", this, "fx" + i + "param2")      
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                syncbox7.removeAll();
                syncbox7.add(model.get(key, 0) == 0 ? compt1 : compt2);
                syncbox7.revalidate();
                syncbox7.repaint();
                }
            };
        ((CheckBox)comp).addToWidth(2);
        next.add(comp);
        next.add(syncbox7);

        vbox = new VBox();
        params = FX_FILTER_LFO_WAVE;
        comp = new Chooser("LFO Wave", this, "fx" + i + "param5", params);
        vbox.add(comp);
        next.add(vbox);

        comp = new LabelledDial("LFO Shape", this, "fx" + i + "param6", color, -63, 63)
            {
            public boolean isSymmetric() { return true; }
            };
        next.add(comp);

        comp = new CheckBox("LFO Key Sync", this, "fx" + i + "param7");
        ((CheckBox)comp).addToWidth(1);
        next.add(comp);

        comp = new LabelledDial("LFO Init Phase", this, "fx" + i + "param8", color, 0, 18)
            {
            public String map(int val) { return "" + (val * 10); }
            };
        next.add(comp);

        comp = new LabelledDial("LFO Spread", this, "fx" + i + "param9", color, -18, 18)
            {
            public String map(int val) { return "" + ((val) * 10); }
            public boolean isSymmetric() { return true; }
            };
        next.add(comp);



                        


        /// 17. RING MODULATOR

        i++;
        effects[i] = new HBox();
        

        levels = new VBox();
        effects[i].add(levels);

        next = new HBox();
        levels.add(next);
        levels.add(Strut.makeVerticalStrut(EFFECTS_DIVIDER));


        comp = new LabelledDial("Dry/Wet", this, "fx" + i + "param0", color, 0, 100)
            {
            public String map(int val) 
                {
                if (val == 0) return "Dry";
                else if (val == 100) return "Wet"; 
                else return "" + (100 - val) + ":" + val;
                }
            };
        next.add(comp);



        final HBox oscbox = new HBox();

        /*       Yet another custom table
                 0.00 to 0.008   by 0.002                0-4             5
                 0.010 to 0.025  by 0.005                5-8             4
                 0.030 to 0.140  by 0.010                9-20    12
                 0.150 to 0.430  by 0.020                21-35   15
                 0.450 to 1.650  by 0.050                36-60   25
                 1.700 to 4.500  by 0.100                61-89   29
                 4.600 to 12.00  by 0.200                90-127  38
        */
        final Component fixedfreq = new LabelledDial(" Fixed Frequency ", this, "fx" + i + "param2", color, 0, 127)
            {
            public String map(int val)
                {
                if (val < 5)
                    return String.format("%2.3f", ((val - 0) * 0.002 + 0.000));
                else if (val < 9)
                    return String.format("%2.3f", ((val - 5) * 0.005 + 0.010));
                else if (val < 21)
                    return String.format("%2.3f", ((val - 9) * 0.010 + 0.030));
                else if (val < 36)
                    return String.format("%2.3f", ((val - 21) * 0.020 + 0.150));
                else if (val < 61)
                    return String.format("%2.3f", ((val - 36) * 0.050 + 0.450));
                else if (val < 90)
                    return String.format("%2.3f", ((val - 61) * 0.100 + 1.700));
                else
                    return String.format("%2.3f", ((val - 90) * 0.200 + 4.600));
                }
            };
        oscbox.add(comp);

        final Component noteoffset = new LabelledDial("Note Offset", this, "fx" + i + "param3", color, -48, 48)
            {
            public boolean isSymmetric() { return true; }
            };

        final Component notefine = new LabelledDial("Note Fine", this, "fx" + i + "param4", color, -50, 50)
            {
            public String map(int val) { return "" + (val) * 2; } 
            public boolean isSymmetric() { return true; }
            };



        vbox = new VBox();
        params = FX_RING_MODULATOR_OSC_MODE;
        comp = new Chooser("Osc Mode", this, "fx" + i + "param1", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                oscbox.removeAll();
                if (model.get(key, 0) == 0)
                    {
                    oscbox.add(fixedfreq);
                    }
                else
                    {
                    oscbox.add(noteoffset);
                    oscbox.add(notefine);
                    }
                oscbox.revalidate();
                oscbox.repaint();
                }
            };
        vbox.add(comp);
        next.add(vbox);
        next.add(oscbox);


        vbox = new VBox();
        params = FX_RING_MODULATOR_OSC_WAVE;
        comp = new Chooser("Osc Wave", this, "fx" + i + "param5", params);
        vbox.add(comp);
        next.add(vbox);

        next = new HBox();
        levels.add(next);

        comp = new LabelledDial("LFO Intensity", this, "fx" + i + "param6", color, -63, 63)
            {
            public boolean isSymmetric() { return true; }
            };
        next.add(comp);

        final Component comps1 = new LabelledDial(" LFO Frequency ", this, "fx" + i + "param8", color, 0, 127)
            {
            public String map(int val) { return "" + FX_FILTER_LFO_FREQUENCY[val]; }
            };

        final Component comps2 = new LabelledDial(" LFO Sync Note ", this, "fx" + i + "param9", color, 0, 16)
            {
            public String map(int val) { return FX_FILTER_LFO_SYNC_NOTE[val]; }
            };

        final HBox syncbox8 = new HBox();
        comp = new CheckBox("LFO Sync", this, "fx" + i + "param7")      
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                syncbox8.removeAll();
                syncbox8.add(model.get(key, 0) == 0 ? comps1 : comps2);
                syncbox8.revalidate();
                syncbox8.repaint();
                }
            };
        ((CheckBox)comp).addToWidth(2);
        next.add(comp);
        next.add(syncbox8);

        vbox = new VBox();
        params = FX_FILTER_LFO_WAVE;
        comp = new Chooser("LFO Wave", this, "fx" + i + "param10", params);
        vbox.add(comp);
        next.add(vbox);

        comp = new LabelledDial("LFO Shape", this, "fx" + i + "param11", color, -63, 63)
            {
            public boolean isSymmetric() { return true; }
            };
        next.add(comp);

        comp = new CheckBox("LFO Key Sync", this, "fx" + i + "param12");
        ((CheckBox)comp).addToWidth(1);
        next.add(comp);

        comp = new LabelledDial("LFO Init Phase", this, "fx" + i + "param13", color, 0, 18)
            {
            public String map(int val) { return "" + (val * 10); }
            };
        next.add(comp);

        comp = new LabelledDial("Pre LPF", this, "fx" + i + "param14", color, 0, 127);
        next.add(comp);



                        

        /// 18. GRAIN SHIFTER

        i++;
        effects[i] = new HBox();
        
        levels = new VBox();
        effects[i].add(levels);

        next = new HBox();
        levels.add(next);
        levels.add(Strut.makeVerticalStrut(EFFECTS_DIVIDER));

        comp = new LabelledDial("Dry/Wet", this, "fx" + i + "param0", color, 0, 100)
            {
            public String map(int val) 
                {
                if (val == 0) return "Dry";
                else if (val == 100) return "Wet"; 
                else return "" + (100 - val) + ":" + val;
                }
            };
        next.add(comp);



        final HBox timehbox10 = new HBox();

        final JComponent comp1m = new LabelledDial("Time Ratio", this, "fx" + i + "param2", color, 0, 122)    // This is probably wrong, may be missing some?
            {
            public String map(int val) { return FX_DELAY_TIME_RATIO[val] + "%"; }
            };
        timehbox10.add(comp1m);
                
                
        // 0  ... 49    by 1            50
        // 50  ... 108 by 2                     30
        // 110  ... 340 by 5            47
        // 350                                          1
                
        final JComponent comp2m = new LabelledDial("Duration", this, "fx" + i + "param3", color, 0, 127)
            {
            public String map(int val)
                {
                if (val < 50) return "" + val ;
                else if (val < 80) return "" + (((val - 50) * 2) + 50) ;
                else if (val < 127) return "" + (((val - 80) * 5) + 110) ;
                else return "350 ms";
                }
            };
        timehbox10.add(comp2m);

        final JComponent comp4m = new LabelledDial("Time Ratio", this, "fx" + i + "param4", color, 0, 14)
            {
            public String map(int val) { return FX_DELAY_TIME_RATIO_SYNC[val] + "%"; }
            };

        final JComponent comp5m = new LabelledDial("Duration", this, "fx" + i + "param5", color, 0, 13)
            {
            public String map(int val) { return FX_DELAY_DELAY_SYNC[val]; }
            };
                
        comp = new CheckBox("BPM Sync", this, "fx" + i + "param1")
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                timehbox10.removeAll();
                if (model.get(key, 0) == 0)
                    {
                    timehbox10.add(comp1m);
                    timehbox10.add(comp2m);
                    }
                else
                    {
                    timehbox10.add(comp4m);
                    timehbox10.add(comp5m);
                    }
                timehbox10.revalidate();
                timehbox10.repaint();
                }
            };
        next.add(comp);
                
        next.add(timehbox10);    



        next = new HBox();
        levels.add(next);

        final Component compr1 = new LabelledDial(" LFO Frequency ", this, "fx" + i + "param7", color, 0, 127)
            {
            public String map(int val) { return "" + FX_FILTER_LFO_FREQUENCY[val]; }
            };

        final Component compr2 = new LabelledDial(" LFO Sync Note ", this, "fx" + i + "param8", color, 0, 16)
            {
            public String map(int val) { return FX_FILTER_LFO_SYNC_NOTE[val]; }
            };

        final HBox syncbox9 = new HBox();
        comp = new CheckBox("LFO Sync", this, "fx" + i + "param6")      
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                syncbox9.removeAll();
                syncbox9.add(model.get(key, 0) == 0 ? compr1 : compr2);
                syncbox9.revalidate();
                syncbox9.repaint();
                }
            };
        ((CheckBox)comp).addToWidth(2);
        next.add(comp);
        next.add(syncbox9);

        comp = new CheckBox("LFO Key Sync", this, "fx" + i + "param9");
        ((CheckBox)comp).addToWidth(1);
        next.add(comp);

        comp = new LabelledDial("LFO Init Phase", this, "fx" + i + "param13", color, 0, 18)
            {
            public String map(int val) { return "" + (val * 10); }
            };
        next.add(comp);



                        


        /// 19. PITCH SHIFTER

        i++;
        effects[i] = new HBox();
        
        levels = new VBox();
        effects[i].add(levels);

        next = new HBox();
        levels.add(next);
        levels.add(Strut.makeVerticalStrut(EFFECTS_DIVIDER));

        comp = new LabelledDial("Dry/Wet", this, "fx" + i + "param0", color, 0, 100)
            {
            public String map(int val) 
                {
                if (val == 0) return "Dry";
                else if (val == 100) return "Wet"; 
                else return "" + (100 - val) + ":" + val;
                }
            };
        next.add(comp);

        comp = new LabelledDial("Pitch", this, "fx" + i + "param1", color, -24, 24)     
            {
            public boolean isSymmetric() { return true; }
            };
        next.add(comp);

        comp = new LabelledDial("Pitch Fine", this, "fx" + i + "param2", color, -50, 50)
            {
            public String map(int val) { return "" + (val) * 2; } 
            public boolean isSymmetric() { return true; }
            };
        next.add(comp);



        final HBox timehbox11 = new HBox();

        final JComponent comp1n = new LabelledDial("Time Ratio", this, "fx" + i + "param4", color, 0, 122)    // This is probably wrong, may be missing some?
            {
            public String map(int val) { return FX_DELAY_TIME_RATIO[val] + "%"; }
            };
        timehbox11.add(comp1n);
                
                
        // 0...29       by 1    30
        // 30..78       by 2    25
        // 80..375      by 5    60
        // 380...500 by 10      13
                
        final JComponent comp2n = new LabelledDial("Delay", this, "fx" + i + "param5", color, 0, 127)
            {
            public String map(int val)
                {
                if (val < 30) return "" + val ;
                else if (val < 55) return "" + (((val - 30) * 2) + 30) ;
                else if (val < 115) return "" + (((val - 55) * 5) + 80) ;
                else return "" + (((val - 115) * 10) + 380) ;
                }
            };
        timehbox11.add(comp2n);

        final JComponent comp4n = new LabelledDial("Time Ratio", this, "fx" + i + "param6", color, 0, 14)
            {
            public String map(int val) { return FX_DELAY_TIME_RATIO_SYNC[val] + "%"; }
            };

        final JComponent comp5n = new LabelledDial("Delay", this, "fx" + i + "param7", color, 0, 13)
            {
            public String map(int val) { return FX_DELAY_DELAY_SYNC[val]; }
            };
                
        comp = new CheckBox("BPM Sync", this, "fx" + i + "param3")
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                timehbox11.removeAll();
                if (model.get(key, 0) == 0)
                    {
                    timehbox11.add(comp1n);
                    timehbox11.add(comp2n);
                    }
                else
                    {
                    timehbox11.add(comp4n);
                    timehbox11.add(comp5n);
                    }
                timehbox11.revalidate();
                timehbox11.repaint();
                }
            };
        next.add(comp);
                
        next.add(timehbox11);    


        next = new HBox();
        levels.add(next);


        vbox = new VBox();
        params = FX_PITCH_SHIFTER_FEEDBACK_POS;
        comp = new Chooser("Feedback Position", this, "fx" + i + "param8", params);
        vbox.add(comp);
        next.add(vbox);

        comp = new LabelledDial("Feedback", this, "fx" + i + "param9", color, 0, 127);
        next.add(comp);

        vbox = new VBox();
        params = FX_PITCH_SHIFTER_MODE;
        comp = new Chooser("Mode", this, "fx" + i + "param10", params);
        vbox.add(comp);
        next.add(vbox);

        comp = new LabelledDial(" High Damp ", this, "fx" + i + "param11", color, 0, 100)
            {
            public String map(int val) { return "" + val + "%"; }
            };
        next.add(comp);

        comp = new LabelledDial("Trim", this, "fx" + i + "param12", color, 0, 127);
        next.add(comp);



                        



        /// 20. TALK MODULATOR

        i++;
        effects[i] = new HBox();
        
        levels = new VBox();
        effects[i].add(levels);

        next = new HBox();
        levels.add(next);
        levels.add(Strut.makeVerticalStrut(EFFECTS_DIVIDER));

        comp = new LabelledDial("Dry/Wet", this, "fx" + i + "param0", color, 0, 100)
            {
            public String map(int val) 
                {
                if (val == 0) return "Dry";
                else if (val == 100) return "Wet"; 
                else return "" + (100 - val) + ":" + val;
                }
            };
        next.add(comp);

        comp = new LabelledDial("Voice Control", this, "fx" + i + "param1", color, -63, 63)
            {
            public String map(int val)
                {
                if (val == 0) return "Center";
                else if (val == -63) return "Bottom";
                else if (val == 63) return "Top";
                else return "" + (val);
                }
            public boolean isSymmetric() { return true; }
            };
        next.add(comp);

        vbox = new VBox();
        params = FX_TALK_MODULATOR_VOICE;
        comp = new Chooser("Voice Top", this, "fx" + i + "param2", params);
        vbox.add(comp);
        next.add(vbox);

        vbox = new VBox();
        params = FX_TALK_MODULATOR_VOICE;
        comp = new Chooser("Voice Center", this, "fx" + i + "param3", params);
        vbox.add(comp);
        next.add(vbox);

        vbox = new VBox();
        params = FX_TALK_MODULATOR_VOICE;
        comp = new Chooser("Voice Bottom", this, "fx" + i + "param4", params);
        vbox.add(comp);
        next.add(vbox);

        comp = new LabelledDial("Resonance", this, "fx" + i + "param5", color, 0, 127);
        next.add(comp);

        comp = new LabelledDial("Drive", this, "fx" + i + "param6", color, 0, 127);
        next.add(comp);

        comp = new LabelledDial("Mod Depth", this, "fx" + i + "param7", color, -63, 63)
            {
            public boolean isSymmetric() { return true; }
            };
        next.add(comp);

        comp = new LabelledDial("Mod Response", this, "fx" + i + "param8", color, 0, 127);
        next.add(comp);

        next = new HBox();
        levels.add(next);

        final Component compq1 = new LabelledDial(" LFO Frequency ", this, "fx" + i + "param10", color, 0, 127)
            {
            public String map(int val) { return "" + FX_FILTER_LFO_FREQUENCY[val]; }
            };

        final Component compq2 = new LabelledDial(" LFO Sync Note ", this, "fx" + i + "param11", color, 0, 16)
            {
            public String map(int val) { return FX_FILTER_LFO_SYNC_NOTE[val]; }
            };

        final HBox syncbox10 = new HBox();
        comp = new CheckBox("LFO Sync", this, "fx" + i + "param9")      
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                syncbox10.removeAll();
                syncbox10.add(model.get(key, 0) == 0 ? compq1 : compq2);
                syncbox10.revalidate();
                syncbox10.repaint();
                }
            };
        ((CheckBox)comp).addToWidth(2);
        next.add(comp);
        next.add(syncbox10);

        vbox = new VBox();
        params = FX_FILTER_LFO_WAVE;
        comp = new Chooser("LFO Wave", this, "fx" + i + "param12", params);
        vbox.add(comp);
        next.add(vbox);

        comp = new LabelledDial("LFO Shape", this, "fx" + i + "param13", color, -63, 63)
            {
            public boolean isSymmetric() { return true; }
            };
        next.add(comp);

        comp = new CheckBox("LFO Key Sync", this, "fx" + i + "param14");
        ((CheckBox)comp).addToWidth(1);
        next.add(comp);

        comp = new LabelledDial("LFO Init Phase", this, "fx" + i + "param15", color, 0, 18)
            {
            public String map(int val) { return "" + (val * 10); }
            };
        next.add(comp);



                        


        /// 21. LOOPER

        i++;
        effects[i] = new HBox();
        
        levels = new VBox();
        next = new HBox();
        
        vbox = new VBox();
        params = FX_LOOPER_SWITCH;
        comp = new Chooser("Switch", this, "fx" + i + "param0", params);
        vbox.add(comp);
        next.add(vbox);

        vbox = new VBox();
        params = FX_LOOPER_LENGTH;
        comp = new Chooser("Length", this, "fx" + i + "param1", params);
        vbox.add(comp);
        next.add(vbox);

        /// HIGHLY custom.  :-(
        // -1.0 to -0.8 by 0.1                  0 .. 2
        // -0.7 to -0.45 by 0.05                3 .. 8
        // -0.4 to -0.125 by 0.025              9 .. 20
        // -0.1 to 0.09 by 0.01                 21 .. 40
        // 0.1 to 0.375 by 0.025                41 .. 52
        // 0.4 to 0.65 by 0.05                  53 .. 58
        // 0.7 to 0.9 by 0.1                    59 .. 61
        // 1.0                                                  62 .. 65        // center is 64 ?               But in fact the default is 7.5
        // 1.01 to 1.06 by 0.01 (wow!)  66 .. 71
        // 1.08 to 1.28 by 0.02                 72 .. 82
        // 1.30 to 1.475 by 0.025               83 .. 90
        // 1.50 to 1.95 by 0.05                 91 .. 100
        // 2.0 to 2.4 by 0.1                    101 .. 105
        // 2.5 to 2.875 by 0.125                106 .. 109
        // 3.00 to 3.75 by 0.25                 110 .. 113
        // 4.0 to 7.5 by 0.5                    114 .. 121
        // 8.0 to 9.0 by 1.0                    122 .. 123
        // 10.0 to 16.0 by 2.0                  124 .. 127

        comp = new LabelledDial("Speed", this, "fx" + i + "param2", color, 0, 127)
            {
            public String map(int val)
                {
                if (val < 3)
                    return String.format("%2.3f", ( val * 0.1 + -1.0 ));
                else if (val < 9)
                    return String.format("%2.3f", ( (val - 3) * 0.05 + -0.7 ));
                else if (val < 21)
                    return String.format("%2.3f", ( (val - 9 ) * 0.025  + -0.4  ));
                else if (val < 41)
                    return String.format("%2.3f", ( (val - 21 ) * 0.01  + -0.1  ));                         
                else if (val < 53)                      
                    return String.format("%2.3f", ( (val - 41 ) * 0.025  + 0.1 ));
                else if (val < 59 )                             
                    return String.format("%2.3f", ( (val - 53 ) * 0.05  + 0.4  ));
                else if (val < 62 )                             
                    return String.format("%2.3f", ( (val - 59 ) * 0.1  +  0.7 ));
                else if (val < 66 )                             
                    return "1.000";
                else if (val < 72 )                             
                    return String.format("%2.3f", ( (val - 66 ) * 0.01 +  1.01 ));
                else if (val < 83 )                             
                    return String.format("%2.3f", ( (val - 72 ) * 0.02  +  1.08 ));
                else if (val < 91 )                             
                    return String.format("%2.3f", ( (val - 83 ) * 0.025  + 1.30  ));
                else if (val < 101 )                            
                    return String.format("%2.3f", ( (val - 91 ) * 0.05  +  1.5 ));
                else if (val < 106 )                            
                    return String.format("%2.3f", ( (val - 101 ) * 0.1  +  2.0 ));
                else if (val < 110 )                            
                    return String.format("%2.3f", ( (val - 106 ) * 0.125  + 2.5  ));
                else if (val < 114 )                            
                    return String.format("%2.3f", ( (val - 110 ) * 0.25  + 3.0  ));
                else if (val < 122 )                            
                    return String.format("%2.3f", ( (val - 114 ) * 0.5  +  4.0 ));
                else if (val < 124 )                            
                    return String.format("%2.3f", ( (val - 122 ) * 1.0  +  8.0 ));
                else
                    return String.format("%2.3f", ( (val - 124 ) * 2.0  +  10.0 ));
                }
            };
        next.add(comp);

        comp = new CheckBox("Direct", this, "fx" + i + "param3");
        next.add(comp);

        levels.add(next);
        effects[i].add(levels);
                
                
        params = FX_PARAMETER_CONTROL[i];
        fxControlChoosers[0] = new Chooser("Parameter Control 1", this, "fxparametercontrol1", params);
        fxControlChoosers[1] = new Chooser("Parameter Control 2", this, "fxparametercontrol2", params);
        final JComponent controlStrut = Strut.makeVerticalStrut(20);
                
        final VBox typebox = new VBox();
        params = FX_TYPE;
        comp = new Chooser("Type", this, "fxtype", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                int type = model.get(key, 0);

                setSendMIDI(false);
                // Reset default settings
                for(int param = 0; param < 20; param++)
                    {
                    model.set("fx" + type + "param" + param, FX_DEFAULT_SETTINGS[type][param]);
                    }
                setSendMIDI(true);

                // replace widgets
                hbox.removeLast();
                hbox.addLast(effects[type]);
                hbox.revalidate();
                hbox.repaint();

                // update control choosers
                typebox.remove(controlStrut);
                typebox.remove(fxControlChoosers[0]);
                typebox.remove(fxControlChoosers[1]);
                if (type == 0)  // "None", remove options
                    {
                    // done
                    }
                else
                    {
                    typebox.add(controlStrut);
                    typebox.add(fxControlChoosers[0]);
                    typebox.add(fxControlChoosers[1]);
                    fxControlChoosers[0].setElements("Parameter Control 1", FX_PARAMETER_CONTROL[type]);
                    fxControlChoosers[0].setIndex(FX_PARAMETER_CONTROL_DEFAULT[0][type]);
                    fxControlChoosers[1].setElements("Parameter Control 2", FX_PARAMETER_CONTROL[type]);
                    fxControlChoosers[1].setIndex(FX_PARAMETER_CONTROL_DEFAULT[1][type]);
                    }
                typebox.revalidate();
                typebox.repaint();
                }
            };
        typebox.add(comp);
            
        // Not needed, since the Type chooser immediately adds them anyway
        //typebox.add(fxControlChoosers[0]);
        //typebox.add(fxControlChoosers[1]);

        hbox.add(typebox);


        // setup None
        effects[0].add(Strut.makeStrut(effects));



        category.add(hbox, BorderLayout.WEST);
        return category;
        }
        
    public int getSigned(String key, int def)
        {
        int val = model.get(key, def);
        if (val < 0)
            val += 128;
        return val;
        }
        
    public void setSigned(String key, int val)
        {
        if (val < 64)
            model.set(key, val);
        else
            model.set(key, val - 128);
        }
        
    public Object[] emitAll(String key)
        {
        /// Handle NRPN
        if (key.equals("currentpattern"))
            {
            return buildNRPN(getChannelOut(), (32 << 7) | (1), (model.get(key, 1) - 1) << 10);
            }
        else if (key.equals("inputselect"))
            {
            return buildNRPN(getChannelOut(), (32 << 7) | (18), (model.get(key, 0) == 0 ? 0 : 127) << 7);
            }

        else   /// Handle Sysex
            {
            byte[] data = new byte[0];

            /// BANK AND GLOBAL
            if (key.equals("name"))
                {
                byte[] name = new byte[0];
                try {  name = model.get("name", "").getBytes("US-ASCII"); }
                catch (UnsupportedEncodingException ex) { ex.printStackTrace(); }
                                
                byte[][] d = new byte[8][12];
                for(int i = 0; i < 8; i++)
                    {
                    d[i] = new byte[] { (byte)0xF0, 0x42, (byte)(48 + getChannelOut()), 0x7F, 0x41, 0x00, 0x00, (byte)i, 0x00, i < name.length ? name[i] : 0x20, 0x00, (byte)0xF7 };
                    }
                return (Object[]) d;
                }
            else if (key.equals("bankbpm"))
                {
                int val = model.get(key, 0);
                byte msb = (byte)((val >>> 7) & 127);
                byte lsb = (byte)(val & 127);
                data = new byte[] { (byte)0xF0, 0x42, (byte)(48 + getChannelOut()), 0x7F, 0x41, 0x00, 0x00, 0x10, 0x00, lsb, msb, (byte)0xF7 };
                }
            else if (key.equals("bankaudioinfxsw"))
                {
                data = new byte[] { (byte)0xF0, 0x42, (byte)(48 + getChannelOut()), 0x7F, 0x41, 0x00, 0x00, 0x12, 0x00, (byte)(model.get(key, 0)), 0x00, (byte)0xF7 };
                }
                
            // PATTERNS
            else if (key.startsWith("pattern"))
                {
                // very inefficient, but whatever
                for(int i = 1; i < 17; i++)
                    {
                    if (key.equals("pattern" + i + "length"))
                        {
                        data = new byte[] { (byte)0xF0, 0x42, (byte)(48 + getChannelOut()), 0x7F, 0x41, 0x62, 0x00, (byte)i, 0x00, (byte)(model.get(key, 0)), 0x00, (byte)0xF7 };
                        break;
                        }
                    else if (key.equals("pattern" + i + "keyboardsample"))
                        {
                        data = new byte[] { (byte)0xF0, 0x42, (byte)(48 + getChannelOut()), 0x7F, 0x41, 0x62, 0x00, (byte)(i + 16), 0x00, (byte)(model.get(key, 0)), 0x00, (byte)0xF7 };
                        break;
                        }
                    }
                }
                        
            //// SAMPLES
            else if (key.startsWith("sample"))
                {
                for(int i = 36; i >= 0; i--)  // work backwards so we can do long numbers first :-)
                    {
                    if (key.startsWith("sample" + i))
                        {
                        if (key.equals("sample" + i + "name"))
                            {
                            byte[] name = new byte[0];
                            try {  name = model.get("sample" + i + "name", "").getBytes("US-ASCII"); }
                            catch (UnsupportedEncodingException ex) { ex.printStackTrace(); }
                                
                            byte[][] d = new byte[8][12];
                            for(int j = 0; j < 8; j++)
                                {
                                d[j] = new byte[] { (byte)0xF0, 0x42, (byte)(48 + getChannelOut()), 0x7F, 0x41, (byte)(i + 15), 0x00, (byte)j, 0x00, j < name.length ? name[j] : 0x20, 0x00, (byte)0xF7 };
                                }
                            return (Object[]) d;
                            }
                        else if (key.equals("sample" + i + "loop"))
                            {
                            data = new byte[] { (byte)0xF0, 0x42, (byte)(48 + getChannelOut()), 0x7F, 0x41, (byte)(i+15), 0x00, 0x10, 0x00, (byte)(model.get(key, 0)), 0x00, (byte)0xF7 };
                            }
                        else if (key.equals("sample" + i + "bpmsync"))
                            {
                            data = new byte[] { (byte)0xF0, 0x42, (byte)(48 + getChannelOut()), 0x7F, 0x41, (byte)(i+15), 0x00, 0x11, 0x00, (byte)(model.get(key, 0)), 0x00, (byte)0xF7 };
                            }
                        else if (key.equals("sample" + i + "reverse"))
                            {
                            data = new byte[] { (byte)0xF0, 0x42, (byte)(48 + getChannelOut()), 0x7F, 0x41, (byte)(i+15), 0x00, 0x12, 0x00, (byte)(model.get(key, 0)), 0x00, (byte)0xF7 };
                            }
                        else if (key.equals("sample" + i + "decay"))
                            {
                            data = new byte[] { (byte)0xF0, 0x42, (byte)(48 + getChannelOut()), 0x7F, 0x41, (byte)(i+15), 0x00, 0x15, 0x00, (byte)(model.get(key, 0)), 0x00, (byte)0xF7 };
                            }
                        else if (key.equals("sample" + i + "release"))
                            {
                            data = new byte[] { (byte)0xF0, 0x42, (byte)(48 + getChannelOut()), 0x7F, 0x41, (byte)(i+15), 0x00, 0x16, 0x00, (byte)(model.get(key, 0)), 0x00, (byte)0xF7 };
                            }
                        else if (key.equals("sample" + i + "semitone"))
                            {
                            data = new byte[] { (byte)0xF0, 0x42, (byte)(48 + getChannelOut()), 0x7F, 0x41, (byte)(i+15), 0x00, 0x1B, 0x00, (byte)(getSigned(key, 0)), (byte)(model.get(key, 0) >= 0 ? 0 : 0x7F), (byte)0xF7 };
                            }
                        else if (key.equals("sample" + i + "tune"))
                            {
                            data = new byte[] { (byte)0xF0, 0x42, (byte)(48 + getChannelOut()), 0x7F, 0x41, (byte)(i+15), 0x00, 0x1C, 0x00, (byte)(model.get(key, 0)), 0x00, (byte)0xF7 };
                            }
                        else if (key.equals("sample" + i + "level"))
                            {
                            data = new byte[] { (byte)0xF0, 0x42, (byte)(48 + getChannelOut()), 0x7F, 0x41, (byte)(i+15), 0x00, 0x18, 0x00, (byte)(model.get(key, 0)), 0x00, (byte)0xF7 };
                            }
                        else if (key.equals("sample" + i + "velint"))
                            {
                            data = new byte[] { (byte)0xF0, 0x42, (byte)(48 + getChannelOut()), 0x7F, 0x41, (byte)(i+15), 0x00, 0x1D, 0x00, (byte)(getSigned(key, 0)), (byte)(model.get(key, 0) >= 0 ? 0 : 0x7F), (byte)0xF7 };
                            }
                        else if (key.equals("sample" + i + "pan"))
                            {
                            data = new byte[] { (byte)0xF0, 0x42, (byte)(48 + getChannelOut()), 0x7F, 0x41, (byte)(i+15), 0x00, 0x19, 0x00, (byte)(model.get(key, 0)), 0x00, (byte)0xF7 };
                            }
                        else if (key.equals("sample" + i + "fxsw"))
                            {
                            data = new byte[] { (byte)0xF0, 0x42, (byte)(48 + getChannelOut()), 0x7F, 0x41, (byte)(i+15), 0x00, 0x1A, 0x00, (byte)(model.get(key, 0)), 0x00, (byte)0xF7 };
                            }
                                                                                                        
                        break;
                        }
                    }
                }
                                
            else if (key.startsWith("fx"))
                {
                if (key.equals("fxtype"))
                    {
                    data = new byte[] { (byte)0xF0, 0x42, (byte)(48 + getChannelOut()), 0x7F, 0x41, 0x50, 0x00, 0x01, 0x00, (byte)(model.get(key, 0)), 0x00, (byte)0xF7 };
                    }
                else if (key.equals("fxparametercontrol1"))
                    {
                    data = new byte[] { (byte)0xF0, 0x42, (byte)(48 + getChannelOut()), 0x7F, 0x41, 0x50, 0x00, 0x02, 0x00, (byte)(model.get(key, 0)), 0x00, (byte)0xF7 };
                    }
                else if (key.equals("fxparametercontrol2"))
                    {
                    data = new byte[] { (byte)0xF0, 0x42, (byte)(48 + getChannelOut()), 0x7F, 0x41, 0x50, 0x00, 0x03, 0x00, (byte)(model.get(key, 0)), 0x00, (byte)0xF7 };
                    }
                else
                    {
                    int type = model.get("fxtype", 0);
                    String prefix = "fx" + type + "param";
                    if (key.startsWith(prefix))
                        {
                        // we're good.  Else a non-displayed widget is being changed, which shouldn't get emitted
                        try
                            {
                            int index = Integer.parseInt(key.substring(prefix.length()));
                            data = new byte[] { (byte)0xF0, 0x42, (byte)(48 + getChannelOut()), 0x7F, 0x41, 0x50, 0x00, (byte)(index + 16), 0x00, (byte)(getSigned(key, 0)), (byte)(model.get(key, 0) >= 0 ? 0 : 0x7F), (byte)0xF7 };
                            }
                        catch (NumberFormatException ex) { ex.printStackTrace(); }
                        }
                    }
                }
                        
                        
            return new Object[] { data };
            }       
        }
    
    public byte[] emit(Model tempModel, boolean toWorkingMemory, boolean toFile) 
        {
        byte[] data = new byte[780];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x7D;
        data[2] = (byte)'E';
        data[3] = (byte)'D';
        data[4] = (byte)'I';
        data[5] = (byte)'S';
        data[6] = (byte)'Y';
        data[7] = (byte)'N';
        data[8] = (byte)' ';
        data[9] = (byte)'K';
        data[10] = (byte)'O';
        data[11] = (byte)'R';
        data[12] = (byte)'G';
        data[13] = (byte)' ';
        data[14] = (byte)'M';
        data[15] = (byte)'I';
        data[16] = (byte)'C';
        data[17] = (byte)'R';
        data[18] = (byte)'O';
        data[19] = (byte)'S';
        data[20] = (byte)'A';
        data[21] = (byte)'M';
        data[22] = (byte)'P';
        data[23] = (byte)'L';
        data[24] = (byte)'E';
        data[25] = (byte)'R';
        data[26] = (byte)0;
                
        // Emit NRPN data
        data[27] = (byte)model.get("currentpattern", 0);
        data[28] = (byte)model.get("inputselect", 0);
                
        // Emit Bank and Global data
        byte[] name = new byte[0];
        try {  name = model.get("name", "").getBytes("US-ASCII"); }
        catch (UnsupportedEncodingException ex) { ex.printStackTrace(); }
        for(int i = 0; i < 8; i++)
            {
            data[29 + i] = i < name.length ? name[i] : 0x20;
            }
        data[37] = (byte)((model.get("bankbpm", 0) >>> 7) & 127);
        data[38] = (byte)(model.get("bankbpm", 0) & 127);
        data[39] = (byte)model.get("bankaudioinfxsw", 0);
                
        // Emit Pattern data
        for(int i = 0; i < 16; i++)
            {
            data[40 + i * 2] = (byte)model.get("pattern" + (i + 1) + "length", 0);
            data[40 + i * 2 + 1] = (byte)model.get("pattern" + (i + 1) + "keyboardsample", 0);
            }
                
        // Emit Sample data
        for(int i = 0; i < 36; i++)
            {
            name = new byte[0];
            try {  name = model.get("sample" + (i + 1) + "name", "").getBytes("US-ASCII"); }
            catch (UnsupportedEncodingException ex) { ex.printStackTrace(); }
            for(int j = 0; j < 8; j++)
                {
                data[72 + i * 19 + j] = j < name.length ? name[j] : 0x20;
                }

            data[72 + i * 19 + 8] = (byte)model.get("sample" + (i + 1) + "loop", 0);
            data[72 + i * 19 + 9] = (byte)model.get("sample" + (i + 1) + "bpmsync", 0);
            data[72 + i * 19 + 10] = (byte)model.get("sample" + (i + 1) + "reverse", 0);
            data[72 + i * 19 + 11] = (byte)model.get("sample" + (i + 1) + "decay", 0);
            data[72 + i * 19 + 12] = (byte)model.get("sample" + (i + 1) + "release", 0);
            data[72 + i * 19 + 13] = (byte)getSigned("sample" + (i + 1) + "semitone", 0);
            data[72 + i * 19 + 14] = (byte)model.get("sample" + (i + 1) + "tune", 0);
            data[72 + i * 19 + 15] = (byte)model.get("sample" + (i + 1) + "level", 0);
            data[72 + i * 19 + 16] = (byte)getSigned("sample" + (i + 1) + "velint", 0);
            data[72 + i * 19 + 17] = (byte)model.get("sample" + (i + 1) + "pan", 0);
            data[72 + i * 19 + 18] = (byte)model.get("sample" + (i + 1) + "fxsw", 0);
            }
                
        // Emit FX data
        data[756] = (byte)model.get("fxtype", 0);
        data[757] = (byte)model.get("fxparametercontrol1", 0);
        data[758] = (byte)model.get("fxparametercontrol2", 0);
        for(int i = 0; i < 20; i++)
            {
            int sub = i;
            data[759 + i] = (byte)getSigned("fx" + data[756] + "param" + sub, 0);
            }
                        
        data[779] = (byte)0xF7;
                
        return data;
        }

    public static boolean recognize(byte[] data)
        {
        boolean val = (data.length == 780 &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x7D &&
            data[2] == (byte)'E' &&
            data[3] == (byte)'D' &&
            data[4] == (byte)'I' &&
            data[5] == (byte)'S' &&
            data[6] == (byte)'Y' &&
            data[7] == (byte)'N' &&
            data[8] == (byte)' ' &&
            data[9] == (byte)'K' &&
            data[10] == (byte)'O' &&
            data[11] == (byte)'R' &&
            data[12] == (byte)'G' &&
            data[13] == (byte)' ' &&
            data[14] == (byte)'M' &&
            data[15] == (byte)'I' &&
            data[16] == (byte)'C' &&
            data[17] == (byte)'R' &&
            data[18] == (byte)'O' &&
            data[19] == (byte)'S' &&
            data[20] == (byte)'A' &&
            data[21] == (byte)'M' &&
            data[22] == (byte)'P' &&
            data[23] == (byte)'L' &&
            data[24] == (byte)'E' &&
            data[25] == (byte)'R' &&
            data[26] == (byte)0);
        return val;
        }
        
    public void parseParameter(byte[] data)
        {
        if (data.length < 8)
            {
            System.err.println("Warning (KorgMicrosampler): Invalid sysex message received");
            }
        else if (data[5] == 0x0)  // BANK AND GLOBAL
            {
            if (data[7] <= 0x07)  // Name
                {
                byte[] name = new byte[0];
                try {  name = (model.get("name", "") + "        ").substring(0, 8).getBytes("US-ASCII"); }
                catch (UnsupportedEncodingException ex) { ex.printStackTrace(); }
                name[data[7]] = data[9];
                try { model.set("name", new String(name, "US-ASCII")); }
                catch (UnsupportedEncodingException ex) { ex.printStackTrace(); }                               
                }
            else if (data[7] == 0x10)  // Bank BPM
                {
                model.set("bankpbm", (data[10] << 7) | data[9]);
                }
            else if (data[7] == 0x12) // Bank Audio In FXSW
                {
                model.set("bankaudioinfxsw", data[9]);
                }
            }
        else if (data[5] == 0x62)  // PATTERN
            {
            if (data[7] < 17)
                {
                int pattern = data[7];
                int value = data[9];
                model.set("pattern" + pattern + "length", value);
                }
            else if (data[7] < 33)
                {
                int pattern = data[7] - 16;
                int value = data[9];
                model.set("pattern" + pattern + "keyboardsample", value);
                }
            }
        else if (data[5] == 0x50) // FX
            {
            if (data[7] == 0x01)  // type
                {
                model.set("fxtype", data[9]);  // should reset the other parameters
                }
            else if (data[7] == 0x02) 
                {
                model.set("fxparametercontrol1", data[9]); 
                }
            else if (data[7] == 0x03) 
                {
                model.set("fxparametercontrol2", data[9]);
                }
            else if (data[7] < 36)
                {
                int type = model.get("fxtype", 0);
                int sub = (data[7] - 16);
                String key = "fx" + type + "param" + sub;
                int val = data[9];
                                
                // here comes the fun bit.  Custom signed
                model.set(key, val);
                if (type == 1)
                    {
                    }
                else if (type == 2)
                    {
                    if (sub == 5 || sub == 11)
                        setSigned(key, val);
                    }
                else if (type == 3)
                    {
                    if (sub == 5 || sub == 8 || sub == 11 || sub == 15)
                        setSigned(key, val);
                    }
                else if (type == 4)
                    {
                    if (sub == 4 || sub == 7 || sub == 10 || sub == 13)
                        setSigned(key, val);
                    }
                else if (type == 5)
                    {
                    if (sub == 6 || sub == 11)
                        setSigned(key, val);
                    }
                else if (type == 6)
                    {
                    }
                else if (type == 7)
                    {
                    }
                else if (type == 8)
                    {
                    }
                else if (type == 9)
                    {
                    if (sub == 14 || sub == 17)
                        setSigned(key, val);
                    }
                else if (type == 10)
                    {
                    if (sub == 11)
                        setSigned(key, val);
                    }
                else if (type == 11)
                    {
                    if (sub == 11)
                        setSigned(key, val);
                    }
                else if (type == 12)
                    {
                    if (sub == 3 || sub == 7)
                        setSigned(key, val);
                    }
                else if (type == 13)
                    {
                    if (sub == 9 || sub == 12)
                        setSigned(key, val);
                    }
                else if (type == 14)
                    {
                    if (sub == 6 || sub == 9)
                        setSigned(key, val);
                    }
                else if (type == 15)
                    {
                    if (sub == 10 || sub == 13)
                        setSigned(key, val);
                    }
                else if (type == 16)
                    {
                    if (sub == 6 || sub == 9)
                        setSigned(key, val);
                    }
                else if (type == 17)
                    {
                    if (sub == 3 || sub == 4 || sub == 6 || sub == 11)
                        setSigned(key, val);
                    }
                else if (type == 18)
                    {
                    }
                else if (type == 19)
                    {
                    if (sub == 1 || sub == 2 )
                        setSigned(key, val);
                    }
                else if (type == 20)
                    {
                    if (sub == 1 || sub == 7 || sub == 13 )
                        setSigned(key, val);
                    }
                else if (type == 21)
                    {
                    if (sub == 1 || sub == 7 || sub == 13 )
                        setSigned(key, val);
                    }
                }
            }
        else if (data[5] < 52)  // samples
            {
            int sn = (data[5] - 15);
                
            if (data[7] <= 0x07)  // Name
                {
                byte[] name = new byte[0];
                try {  name = (model.get("sample" + sn + "name", "") + "        ").substring(0, 8).getBytes("US-ASCII"); }
                catch (UnsupportedEncodingException ex) { ex.printStackTrace(); }
                name[data[7]] = data[9];
                try { model.set("sample" + sn + "name", new String(name, "US-ASCII")); }
                catch (UnsupportedEncodingException ex) { ex.printStackTrace(); }                               
                }
            else if (data[7] == 0x10)
                {
                model.set("sample" + sn + "loop", data[9]);
                }
            else if (data[7] == 0x11)
                {
                model.set("sample" + sn + "bpmsync", data[9]);
                }
            else if (data[7] == 0x12)
                {
                model.set("sample" + sn + "reverse", data[9]);
                }
            else if (data[7] == 0x15)
                {
                model.set("sample" + sn + "decay", data[9]);
                }
            else if (data[7] == 0x16)
                {
                model.set("sample" + sn + "release", data[9]);
                }
            else if (data[7] == 0x1B)
                {
                setSigned("sample" + sn + "semitone", data[9]);
                }
            else if (data[7] == 0x1C)
                {
                model.set("sample" + sn + "tune", data[9]);
                }
            else if (data[7] == 0x18)  // out of order, but it's correct!
                {
                model.set("sample" + sn + "level", data[9]);
                }
            else if (data[7] == 0x1D)
                {
                setSigned("sample" + sn + "velint", data[9]);
                }
            else if (data[7] == 0x19)
                {
                model.set("sample" + sn + "pan", data[9]);
                }
            else if (data[7] == 0x1A)
                {
                model.set("sample" + sn + "fxsw", data[9]);
                }
            }
        }

    public int parse(byte[] data, boolean fromFile)
        {
        // NRPN Data
        model.set("currentpattern", data[27]);
        model.set("inputselect", data[28]);
        
        // Bank and Global Data
        byte[] name = new byte[8];
        System.arraycopy(data, 29, name, 0, 8);
        try {  model.set("name", new String(name, "US-ASCII")); }
        catch (UnsupportedEncodingException ex) { ex.printStackTrace(); }

        model.set("bankbpm", (data[37] << 7) | data[38]);
        model.set("bankaudioinfxsw", data[39]);
        
        // Pattern Data
        for(int i = 0; i < 16; i++)
            {
            model.set("pattern" + (i + 1) + "length", data[40 + i * 2]);
            model.set("pattern" + (i + 1) + "keyboardsample", data[40 + i * 2 + 1]);
            }
                
        // Sample Data
        for(int i = 0; i < 36; i++)
            {
            name = new byte[8];
            System.arraycopy(data, 72 + i * 19, name, 0, 8);
            try {  model.set("sample" + (i + 1) + "name", new String(name, "US-ASCII")); }
            catch (UnsupportedEncodingException ex) { ex.printStackTrace(); }

            model.set("sample" + (i + 1) + "loop", data[72 + i * 19 + 8]);
            model.set("sample" + (i + 1) + "bpmsync", data[72 + i * 19 + 9]);
            model.set("sample" + (i + 1) + "reverse", data[72 + i * 19 + 10]);
            model.set("sample" + (i + 1) + "decay", data[72 + i * 19 + 11]);
            model.set("sample" + (i + 1) + "release", data[72 + i * 19 + 12]);
            setSigned("sample" + (i + 1) + "semitone", data[72 + i * 19 + 13]);
            model.set("sample" + (i + 1) + "tune", data[72 + i * 19 + 14]);
            model.set("sample" + (i + 1) + "level", data[72 + i * 19 + 15]);
            setSigned("sample" + (i + 1) + "velint", data[72 + i * 19 + 16]);
            model.set("sample" + (i + 1) + "pan", data[72 + i * 19 + 17]);
            model.set("sample" + (i + 1) + "fxsw", data[72 + i * 19 + 18]);
            }
                        
        // FX Data
        model.set("fxtype", data[756]);
        model.set("fxparametercontrol1", data[757]);
        model.set("fxparametercontrol2", data[758]);
        for(int i = 0; i < 20; i++)
            {
            int sub = i;
            int type = data[756];
            String key = "fx" + type + "param" + sub;
            int val = data[759 + i];

            // here comes the fun bit again.  Custom signed
            model.set(key, val);
            if (type == FX_NONE)
                {
                new RuntimeException("FX for type 'none' spotted: fx" + type + "param" + sub);
                }
            if (type == FX_COMPRESSOR)
                {
                }
            else if (type == FX_FILTER)
                {
                if (sub == 5 || sub == 11)
                    setSigned(key, val);
                }
            else if (type == FX_EQ)
                {
                if (sub == 5 || sub == 8 || sub == 11 || sub == 15)
                    setSigned(key, val);
                }
            else if (type == FX_DISTORTION)
                {
                if (sub == 4 || sub == 7 || sub == 10 || sub == 13)
                    setSigned(key, val);
                }
            else if (type == FX_DECIMATOR)
                {
                if (sub == 6 || sub == 11)
                    setSigned(key, val);
                }
            else if (type == FX_REVERB)
                {
                }
            else if (type == FX_DELAY)
                {
                }
            else if (type == FX_LCRDELAY)
                {
                }
            else if (type == FX_PANDELAY)
                {
                if (sub == 14 || sub == 17)
                    setSigned(key, val);
                }
            else if (type == FX_MODDELAY)
                {
                if (sub == 11)
                    setSigned(key, val);
                }
            else if (type == 11)
                {
                if (sub == FX_TAPEECHO)
                    setSigned(key, val);
                }
            else if (type == FX_CHORUS)
                {
                if (sub == 3 || sub == 7)
                    setSigned(key, val);
                }
            else if (type == FX_FLANGER)
                {
                if (sub == 9 || sub == 12)
                    setSigned(key, val);
                }
            else if (type == FX_VIBRATO)
                {
                if (sub == 6 || sub == 9)
                    setSigned(key, val);
                }
            else if (type == FX_PHASER)
                {
                if (sub == 10 || sub == 13)
                    setSigned(key, val);
                }
            else if (type == FX_TREMOLO)
                {
                if (sub == 6 || sub == 9)
                    setSigned(key, val);
                }
            else if (type == FX_RINGMOD)
                {
                if (sub == 3 || sub == 4 || sub == 6 || sub == 11)
                    setSigned(key, val);
                }
            else if (type == FX_GRAINSHIFTER)
                {
                }
            else if (type == FX_PITCHSHIFTER)
                {
                if (sub == 1 || sub == 2 )
                    setSigned(key, val);
                }
            else if (type == FX_TALKMOD)
                {
                if (sub == 1 || sub == 7 || sub == 13 )
                    setSigned(key, val);
                }
            else if (type == FX_LOOPER)
                {
                if (sub == 1 || sub == 7 || sub == 13 )
                    setSigned(key, val);
                }
            }
                
        return PARSE_SUCCEEDED;
        }

    public static String getSynthName() { return "Korg Microsampler"; }
    
    public String getPatchName() { return model.get("name", "Untitled"); }

    public boolean getSendsAllParametersInBulk()
        {
        return false;
        }
                
    public JFrame sprout()
        {
        JFrame frame = super.sprout();
        receiveCurrent.setEnabled(false);
        receivePatch.setEnabled(false);
        transmitTo.setEnabled(false);
        writeTo.setEnabled(false);
        merge.setEnabled(false);
        return frame;
        }
    }
