/***
    Copyright 2026 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.kawaik5000;

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
import edisyn.util.*;

/**
   A multimode patch editor for the Kawai K5000S and K5000R, compatible with the K5000W (Bank B not supported).
   
   The K5000, like the K5, does not support Send-To-Current-Patch.  Ordinarily that would not be a 
   problem, but it is for the K5000.  A huge problem.  We could use a scratch patch like we do in the K5, 
   except that the K5000 has Flash RAM rather than battery-backed RAM, so if we did that we'd burn out
   the Flash.  Alternatively we could send each parameter separately in its own send-parameter sysex
   message (which the K5000 does support).  But there are upwards of 6000 parameters!  That's about 25
   seconds worth of sysex messages.  I have no solution to this: we have to turn off many Edisyn features
   and the user has to decide whether or not to send a patch manually via a scratch patch or individual
   parameters.
      
   The K5000 has special files called FOO.KA1 (for individual patches) and FOO.KAA (for bank patches).
   We do not support them at this time but may later if we can figure out how they work.
        
   @author Sean Luke
*/

public class KawaiK5000Multi extends Synth
    {
    
    // Pictures of the effect algorithms
    public static final ImageIcon[] EFFECT_ALGORITHM_ICONS = 
        {
        new ImageIcon(KawaiK5000.class.getResource("Algorithm1.png")),
        new ImageIcon(KawaiK5000.class.getResource("Algorithm2.png")),
        new ImageIcon(KawaiK5000.class.getResource("Algorithm3.png")),
        new ImageIcon(KawaiK5000.class.getResource("Algorithm4.png")),
        };

    // The Patch Name can be no longer than 8
    public static final int MAXIMUM_NAME_LENGTH = 8;
    // Various tables below have a "none" slot
    public static final int NONE = -1;                                      // indicates "nothing" in effects lists
        
    public static final String[] BANKS = { "Bank" };
    // Number of effects
    public static final int NUM_EFFECTS = 4;
    // Number of parameters per effect
    public static final int NUM_EFFECT_PARAMETERS = 5;
    
    /// FIXME: Multi has this as Off/Loud/Soft, but Single has this as Off/Low/High
    
    public static final String[] VELO_SWITCH_TYPES = { "Off", "Loud", "Soft" };         

    // Names of effect parameters.  An underscore _ indicates a suggested non-breaking space
    public static final String[][] EFFECT_PARAMETER_NAMES = 
        {
        { "Dry/Wet", "Dry/Wet", "Delay Level", "Delay Level", "Delay Level", "Delay Level", " Delay Level", "Delay Level", "Dry/Wet", "Dry/Wet", "Dry/Wet", "Dry/Wet", "Dry/Wet", "Dry/Wet", "Dry/Wet", "Dry/Wet", "Dry/Wet", "Dry/Wet", "Dry/Wet", "Dry/Wet", "Dry/Wet", "Dry/Wet", "Dry/Wet", "Dry/Wet", "Dry/Wet", "Dry/Wet", "Dry/Wet", "Dry/Wet", "Dry/Wet", "Dry/Wet", "Dry/Wet", "Dry/Wet", "Dry/Wet", "Dry/Wet", "Dry/Wet", "Dry/Wet", "Dry/Wet" },
        { "Slope", "Slope", "Delay Time_1", "Delay Time_1", "", "Delay Time_L", "", "", "Speed", "Speed", "Speed", "Speed", "Speed", "Speed", "Speed", "Speed", "Speed", "Speed", "", "", "Speed", "Speed", "Speed", "Speed", "Speed", "Speed", "Speed", "Speed", "Slow Speed", "Sense", "Center Frequency", "EQ_Low", "EQ_Low", "EQ_Low", "EQ_Low", "EQ_Low", "EQ_Low" },
        { "", "", "Tap Level", "Tap Level", "Delay Fine", "Feedback_L", "", "", "Depth", "Depth", "Depth", "Depth", "Depth", "Depth", "Depth", "Depth", "Depth", "Depth", "Depth", "Depth", "Depth", "Depth", "Depth", "Depth", "Depth", "Depth", "Depth", "Depth", "Fast Speed", "Frequency Bottom", "Bandwidth", "EQ_High", "EQ_High", "EQ_High", "EQ_High", "EQ_High", "EQ_High" },
        { "Predelay Time", "Predelay Time", "Delay Time_2", "Delay Time_2", "Delay Coarse", "Delay Time_R", "Delay Time", "Delay Time", "Predelay Time", "Delay Time", "Predelay Time", "Predelay Time", "Delay Time", "Delay Time", "Predelay Time", "Predelay Time", "Delay Time", "Delay Time", "Predelay Time", "Delay Time", "Predelay Time", "Delay Time", "Predelay Time", "Delay Time", "Predelay Time", "Predelay Time", "Delay Time", "Delay Time", "Accel", "Frequency Top", "", "", "", "Output Level", "Output Level", "Delay Time", "Delay Time" },
        { "Feedback", "Feedback", "Feedback", "Feedback", "Feedback", "Feedback_R", " Feedback", "Feedback", "Wave", "Wave", "Wave", "Wave", "Wave", "Wave", "Feedback", "Feedback", "Feedback", "Feedback", "", "", "", "", "Wave", "Wave", "Feedback", "Feedback", "Feedback", "Feedback", "Slow/Fast", "Resonance", "", "Intensity", "Intensity", "Drive", "Drive", "Drive", "Drive" },
        };

    // Units used in the effect paramters
    public static final String[][] EFFECT_PARAMETER_UNITS = 
        {
        { "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "" },
        { "", "", "ms", "ms", "", "ms", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "dB", "dB", "dB", "dB", "dB", "dB" },
        { "", "", "", "", "ms", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "dB", "dB", "dB", "dB", "dB", "dB" },
        { "sec", "sec", "ms", "ms", "ms", "ms", "ms", "ms", "ms", "ms", "ms", "ms", "ms", "ms", "ms", "ms", "ms", "ms", "ms", "ms", "ms", "ms", "ms", "ms", "ms", "ms", "ms", "ms", "", "", "", "", "", "", "", "ms", "ms" },
        { "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "" },
        };

    // Default values for the effect parameters.  Presently not using this.
    public static final int[][] EFFECT_PARAMETER_DEFAULTS = 
        {
        { 30, 60, 100, 100, 25, 35, 40, 50, 24, 24, 50, 60, 0, 0, 52, 50, 52, 50, 35, 35, 50, 50, 30, 30, 10, 60, 30, 80, 15, 0, 0, 0, 0, 0, 0, 0, 0 },
        { 1, 19, 56, 56, NONE, 56, NONE, NONE, 14, 14, 6, 6, 6, 6, 9, 6, 9, 6, NONE, NONE, 6, 6, 53, 53, 5, 4, 6, 7, 8, 60, 50, 0, 0, 0, 0, 0, 0 },
        { NONE, NONE, 90, 90, 0, 60, NONE, NONE, 91, 91, 76, 76, 75, 75, 54, 75, 54, 75, 75, 75, 75, 75, 49, 48, 93, 75, 75, 100, 71, 30, 50, 0, 0, 0, 0, 0, 0 },
        { 5, 36, 66, 81, 51, 77, 81, 71, 32, 150, 28, 28, 150, 150, 0, 0, 100, 60, 40, 200, 80, 200, 0, 170, 0, 80, 200, 200, 5, 80, NONE, NONE, NONE, 50, 50, 150, 150 },
        { 10, 50, 50, 50, 50, 33, 55, 60, 0, 0, 0, 0, 0, 0, 46, 50, 46, 50, NONE, NONE, NONE, NONE, 0, 0, 80, 80, 80, 50, 1, 50, NONE, 50, 50, 50, 50, 50, 50 }
        };

    // Min values for the effect parameters
    public static final int[][] EFFECT_PARAMETER_MINS = 
        {
        // The manual says that TAP DELAY 1 ... CROSS DELAY have min values of 0 for Delay Level (dry/wet), but they do not.  They are 0.
        // Though we go -12db .. +12db, the value are actually just 0...25
        { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,  },
        { 0, 0, 0, 0, NONE, 0, NONE, NONE, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, NONE, NONE, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0,  },
        { NONE, NONE, 0, 0, 0, 0, NONE, NONE, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 0, 0, 0, 0, 0, 0, 0, 0,  },
        { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NONE, NONE, NONE, 0, 0, 0, 0,  },
        { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NONE, NONE, NONE, NONE, 0, 0, 0, 0, 0, 0, 0, 0, NONE, 0, 0, 0, 0, 0, 0,  },
        };

    // Max values for the effect parameters
    public static final int[][] EFFECT_PARAMETER_MAXES = 
        {
        // 0...99 is actually displayed as 1..100
        // 25 is actually +12db
        { 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100,  },
        { 100, 100, 720, 720, NONE, 720, NONE, NONE, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, NONE, NONE, 100, 100, 100, 100, 100, 100, 100, 100, 20, 99, 99, 25, 25, 25, 25, 25, 25,  },
        { NONE, NONE, 99, 99, 9, 99, NONE, NONE, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 100, 99, 99, 25, 25, 25, 25, 25, 25,  },
        // The 9 below is actually displayed as (1...10).  it is Rotary Acceleration
        { 100, 100, 720, 720, 1270, 720, 720, 720, 100, 200, 100, 200, 200, 200, 100, 100, 200, 200, 100, 200, 100, 200, 100, 200, 100, 100, 200, 200, 9, 99, NONE, NONE, NONE, 99, 99, 200, 200,  },
        { 99, 99, 99, 99, 99, 99, 99, 99, 1, 1, 1, 1, 1, 100, 99, 99, 99, 99, NONE, NONE, NONE, NONE, 1, 1, 99, 99, 99, 99, 1, 99, NONE, 99, 99, 99, 99, 99, 99,  },
        };
        
    static HashMap multiDataParamsToIndex = null;
    static HashMap sectionParamsToIndex = null;

    // Types of reverbs
    public static final String[] REVERB_TYPES = new String[]
    {
    "Hall 1 (Standard)",
    "Hall 2 (Small)",
    "Hall 3 (Bright)",
    "Room 1 (Standard)",
    "Room 2 (Large)",
    "Room 3 (Bright)",
    "Plate 1 (Large)",
    "Plate 2 (Small)",
    "Plate 3 (Mellow)",
    "Reverse",              // "No Reverb" in the sysex docs, dunno what that means
    "Long Delay",               // "No Reverb" in the sysex docs, dunno what that means
    };
                
    // Types of Effects
    public static final String[] EFFECT_TYPES = new String[]
    {
    "Early Reflections 1",
    "Early Reflections 2",
    "Tap Delay 1",
    "Tap Delay 2",
    "Single Delay",
    "Dual Delay",
    "Stereo Delay",
    "Cross Delay",
    "Auto Pan",
    "Auto Pan and Delay",
    "Chorus 1",
    "Chorus 2",
    "Chorus 1 and Delay",
    "Chorus 2 and Delay",
    "Flanger 1",
    "Flanger 2",
    "Flanger 1 and Delay",
    "Flanger 2 and Delay",
    "Ensemble",
    "Ensemble and Delay",
    "Celeste",
    "Celeste and Delay",
    "Tremelo",
    "Tremelo and Delay",
    "Phaser 1",
    "Phaser 2",
    "Phaser 1 and Delay",
    "Phaser 2 and Delay",
    "Rotary",
    "Auto Wah",
    "Bandpass Filter",
    "Exciter",
    "Enhancer",
    "Overdrive",
    "Distortion",
    "Overdrive and Delay",
    "Distortion and Delay"
    };
        
    // Effect Controller and Assignable Controller Sources
    public static final String[] SOURCES = new String[]     
    {
    "Bender",
    "Channel Pressure",
    "Modulation Wheel",
    "Expression Pedal",
    "MIDI Volume",
    "Panpot",
    "Controller 1",
    "Controller 2",
    "Controller 3",
    "Controller 4",
    "Controller 5",
    "Controller 6",
    "Controller 7",
    "Controller 8"
    };      
        

    // Effect Controller Destinations
    public static final String[] EFFECT_DESTINATIONS = new String[]
    {
    "Effect 1 Dry/Wet",
    "Effect 1 Para",
    "Effect 2 Dry/Wet",
    "Effect 2 Para",
    "Effect 3 Dry/Wet",
    "Effect 3 Para",
    "Effect 4 Dry/Wet",
    "Effect 4 Para",
    "Reverb Dry/Wet 1",
    "Reverb Dry/Wet 2",
    };


    public KawaiK5000Multi()
        {
        if (multiDataParamsToIndex == null)
            {
            multiDataParamsToIndex = new HashMap();
            for(int i = 0; i < multiDataParams.length; i++)
                {
                multiDataParamsToIndex.put(multiDataParams[i], i);
                }
            }

        if (sectionParamsToIndex == null)
            {
            sectionParamsToIndex = new HashMap();
            for(int i = 0; i < sectionParams.length; i++)
                {
                sectionParamsToIndex.put(sectionParams[i], i);
                }
            }

        /// SOUND PANEL
                
        JComponent soundPanel = new SynthPanel(this);
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        hbox.addLast(addSections(Style.COLOR_C()));
        vbox.add(hbox);
        hbox = new HBox();
        makeAllEffectParameters();
        hbox.add(addEffect(0, Style.COLOR_B()));
        hbox.addLast(addEffect(1, Style.COLOR_A()));
        vbox.add(hbox);
        hbox = new HBox();
        
        VBox a = new VBox();
        a.add(addEffect(2, Style.COLOR_B()));
        a.add(addReverb(Style.COLOR_B()));
        hbox.add(a);
                
        a = new VBox();
        a.add(addEffect(3, Style.COLOR_A()));
        a.add(addEQ(Style.COLOR_A()));
        hbox.addLast(a);

        vbox.add(hbox);

        vbox.add(addEffectsGeneral(Style.COLOR_A()));

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("General", soundPanel);
        
        soundPanel = new SynthPanel(this);
        vbox = new VBox();

        for(int section = 1; section <= 4; section++)
            {
            vbox.add(addSection(section, Style.COLOR_A()));
            }

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Sections", (SynthPanel)soundPanel);
        
        model.set("name", "INIT");  // has to be 10 long
        model.set("number", 0);
        loadDefaults();     
        }
                
    public String getDefaultResourceFileName() { return "KawaiK5000Multi.init"; }
    public String getHTMLResourceFileName() { return "KawaiK5000Multi.html"; }

    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        JTextField number = new SelectedTextField("" + (model.get("number") + 1), 3);

        while(true)
            {
            boolean result = showMultiOption(this, new String[] { "Patch Number"}, 
                new JComponent[] { number }, title, "Enter the Patch number");
                
            if (result == false) 
                return false;
                                
            int n;
            try { n = Integer.parseInt(number.getText()); }
            catch (NumberFormatException e)
                {
                showSimpleError(title, "The Patch Number must be an integer 1...64");
                continue;
                }
            if (n < 1 || n > 64)
                {
                showSimpleError(title, "The Patch Number must be an integer 1...64");
                continue;
                }
                
            n--;
                                
            change.set("number", n);
                        
            return true;
            }
        }
        
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


    /** Add the global patch category (name, id, number, etc.) */
    public JComponent addNameGlobal(Color color)
        {
        Category globalCategory = new Category(this, getSynthName(), color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
                
        VBox vbox = new VBox();
        comp = new PatchDisplay(this, 8);
        vbox.add(comp);
        hbox.add(vbox);
        
        vbox = new VBox();
        
        comp = new StringComponent("Patch Name", this, "name", MAXIMUM_NAME_LENGTH, "Name must be up to " + MAXIMUM_NAME_LENGTH + " ASCII characters.")
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
        vbox.add(comp);  // doesn't work right :-(
        hbox.add(vbox);

        vbox = new VBox();
        comp = new LabelledDial("Volume", this, "volume", color, 0, 127);
        hbox.add(comp);
        
        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }
        
    public JComponent addSections(Color color)
        {
        Category category = new Category(this, "Sections", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        comp = new CheckBox("Section 1", this, "secmute1", true);
        vbox.add(comp); 
        comp = new CheckBox("Section 2", this, "secmute2", true);
        vbox.add(comp); 
        hbox.add(vbox);   
        vbox = new VBox();
        comp = new CheckBox("Section 3", this, "secmute3", true);
        vbox.add(comp); 
        comp = new CheckBox("Section 4", this, "secmute4", true);
        vbox.add(comp); 
        hbox.add(vbox);
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
        
    public JComponent addEffectsGeneral(Color color)
        {
        Category category = new Category(this, "Effects", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        comp = new LabelledDial("Algorithm", this, "algorithm", color, 0, 3, -1);
        hbox.add(comp);

        hbox.add(Strut.makeHorizontalStrut(16));
        comp = new IconDisplay(null, EFFECT_ALGORITHM_ICONS, this, "algorithm", 192, 84);
        hbox.add(comp);
        hbox.add(Strut.makeHorizontalStrut(16));

        VBox vbox = new VBox();
        params = SOURCES;
        comp = new Chooser("Control Source 1", this, "effectcontrol1source", params);
        vbox.add(comp);
        params = EFFECT_DESTINATIONS;
        comp = new Chooser("Control Destination 1", this, "effectcontrol1destination", params);
        vbox.add(comp);
        hbox.add(vbox);

        vbox = new VBox();
        params = SOURCES;
        comp = new Chooser("Control Source 2", this, "effectcontrol2source", params);
        vbox.add(comp);
        params = EFFECT_DESTINATIONS;
        comp = new Chooser("Control Destination 2", this, "effectcontrol2destination", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Control 1", this, "effectcontrol1depth", color, 33, 95, 64);
        ((LabelledDial)comp).addAdditionalLabel("Depth");
        hbox.add(comp);
        comp = new LabelledDial("Control 2", this, "effectcontrol2depth", color, 33, 95, 64);
        ((LabelledDial)comp).addAdditionalLabel("Depth");
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
 
        
    /*
      public static final int[] REVERB_DEFAULT_1 = { 85, 40, 70, 85, 80, 65, 70, 80, 70, 0, 40 };
      public static final int[] REVERB_DEFAULT_2 = { 85, 40, 70, 85, 80, 65, 70, 80, 70, 0, 40 };
      public static final int[] REVERB_DEFAULT_3 = { 13, 13, 28, 4, 8, 13, 8, 18, 28, 10, 100 };
      public static final int[] REVERB_DEFAULT_4 = { 30, 50, 60, 5, 30, 40, 5, 30, 40, 0, 500 };
      public static final int[] REVERB_DEFAULT_5 = { 25, 5, 25, 15, 5, 40, 5, 20, 40, 20, 20 };
    */
        
    HBox reverbBox = new HBox();

    public void rebuildReverb(Color color)
        {
        // This updates a lot of stuff, which sends out parameters that we don't want.
        // So we're blocking midi here
        boolean midi = getSendMIDI();
        setSendMIDI(false);
        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        reverbBox.removeAll();
        int type = this.getModel().get("reverbtype");
        
        comp = new LabelledDial("Dry/Wet 1", this, "reverb" + (type + 1) + "drywet1", color, 0, 100);
        reverbBox.add(comp);

        comp = new LabelledDial("Dry/Wet 2", this, "reverb" + (type + 1) + "para1", color, 0, 100);
        reverbBox.add(comp);

        if (type == 9 || type == 10)  // reverse, long delay
            {
            comp = new LabelledDial("Feedback", this, "reverb" + (type + 1) + "para2", color, 0, 99, -1);
            }
        else
            {
            comp = new LabelledDial("Reverb", this, "reverb" + (type + 1) + "para2", color, 3, 50)
                {
                public String map(int val) { int sec = val * 10 + 3; return "" + (sec / 10) + "." + (sec % 10) + " s"; }
                };
            ((LabelledDial)comp).addAdditionalLabel("Time");
            }
        reverbBox.add(comp);

        if (type == 10)  // long delay
            {
            comp = new LabelledDial("Delay", this, "reverb" + (type + 1) + "para3", color, 0, 100)
                {
                public String map(int val) { return "" + val + " ms"; }
                };
            ((LabelledDial)comp).addAdditionalLabel("Time");
            }
        else
            {
            comp = new LabelledDial("Predelay", this, "reverb" + (type + 1) + "para3", color, 0, 100)
                {
                public String map(int val) { return "<html><font size=-1>" + (val * 10 + 200) + " ms</font></html>"; }
                };
            ((LabelledDial)comp).addAdditionalLabel("Time");
            }
        reverbBox.add(comp);

        comp = new LabelledDial("High Freq", this, "reverb" + (type + 1) + "para4", color, 0, 99, -1);
        ((LabelledDial)comp).addAdditionalLabel("Damping");
        reverbBox.add(comp);

        reverbBox.revalidate(); 
        reverbBox.repaint(); 
        
        setSendMIDI(midi);
        }
 
    public JComponent addReverb(Color color)
        {
        Category category = new Category(this, "Reverb", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        // Make some dummies
        for(int type = 0; type < REVERB_TYPES.length; type++)
            {
            String key = "reverb" + (type + 1) + "drywet1";
            model.set(key, 0);
            int max = 100;
            model.setMinMax(key, 0, max);
            model.setMetricMinMax(key, 0, max);
                                
            key = "reverb" + (type + 1) + "para1";
            model.set(key, 0);
            max = 100;
            model.setMinMax(key, 0, max);
            model.setMetricMinMax(key, 0, max);

            key = "reverb" + (type + 1) + "para2";
            model.set(key, 0);
            max = (type == 9 || type == 10 ? 100 : 47);
            model.setMinMax(key, 0, max);
            model.setMetricMinMax(key, 0, max);

            key = "reverb" + (type + 1) + "para3";
            model.set(key, 0);
            max = (type == 10 ? 100 : 127);
            model.setMinMax(key, 0, max);
            model.setMetricMinMax(key, 0, max);

            key = "reverb" + (type + 1) + "para4";
            model.set(key, 0);
            max = 100;
            model.setMinMax(key, 0, max);
            model.setMetricMinMax(key, 0, max);
            }
        
        params = REVERB_TYPES;
        comp = new Chooser("Type", this, "reverbtype", params)
            {
            public void update(String key, Model model) 
                { 
                super.update(key, model); 
                rebuildReverb(color); 
                int type = model.get("reverbtype");
                // We also want to send the reverb params
                for(int i = 0; i < 5; i++)
                    {
                    String paramkey = "reverb" + (type + 1) + (i == 0 ? "drywet1" : "para" + i);
                    model.set(paramkey, model.get(paramkey));
                    }
                }
            };
        vbox.add(comp);
        hbox.add(vbox);
                
        hbox.addLast(reverbBox);
                        
        rebuildReverb(color);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addEQ(Color color)
        {
        Category category = new Category(this, "EQ", color);
	category.makeDistributable("geqfreq");
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        comp = new LabelledDial("100 Hz", this, "geqfreq1", color, 58, 70, 64);
        hbox.add(comp);
        comp = new LabelledDial("200 Hz", this, "geqfreq2", color, 58, 70, 64);
        hbox.add(comp);
        comp = new LabelledDial("450 Hz", this, "geqfreq3", color, 58, 70, 64);
        hbox.add(comp);
        comp = new LabelledDial("1 KHz", this, "geqfreq4", color, 58, 70, 64);
        hbox.add(comp);
        comp = new LabelledDial("2 KHz", this, "geqfreq5", color, 58, 70, 64);
        hbox.add(comp);
        comp = new LabelledDial("4 KHz", this, "geqfreq6", color, 58, 70, 64);
        hbox.add(comp);
        comp = new LabelledDial("8 KHz", this, "geqfreq7", color, 58, 70, 64);
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
       
        
        
    HBox effectBox[] = { new HBox(), new HBox(), new HBox(), new HBox() };

    LabelledDial lastDial = null;           // will always be filled in by the time we need it
        
    //HBox allEffects[][] = new HBox[NUM_EFFECTS][EFFECT_PARAMETER_MINS[0].length];
        
    public void makeAllEffectParameters()              // ugh this is expensive
        {
        int numEffectTypes = EFFECT_PARAMETER_MINS[0].length;
        //allEffects = new HBox[NUM_EFFECTS][numEffectTypes];
        for(int i = 0; i < NUM_EFFECTS; i++)
            {
            for(int j = 0; j < numEffectTypes; j++)
                {
                makeEffectParameters(i, j);
                }
            }
        }
        
    public void makeEffectParameters(int effect, int type)
        {
        for(int i = 0; i < 5; i++)
            {                        
            // Next, 1270, 720, and 200 are NOT the real "max value" -- they are placeholders for three
            // display functions.  The real max values are 127, 127, and 100 respectively.
            int trueMax = EFFECT_PARAMETER_MAXES[i][type];
            if (EFFECT_PARAMETER_MAXES[i][type] == 1270)
                {
                trueMax = 127;
                }
            else if (EFFECT_PARAMETER_MAXES[i][type] == 720)
                {
                trueMax = 127;
                }
            else if (EFFECT_PARAMETER_MAXES[i][type] == 200)
                {
                trueMax = 100;
                }
            
            int max = EFFECT_PARAMETER_MAXES[i][type];
            if (max == -1)  // Not a parameter
                {
                // make some dummies
                model.set("effect" + (effect + 1) + "type" + (type + 1) + (i == 0 ? "depth" : "para" + i), 0);
                model.setMinMax("effect" + (effect + 1) + "type" + (type + 1) + (i == 0 ? "depth" : "para" + i), 0, 0);
                model.setMetricMinMax("effect" + (effect + 1) + "type" + (type + 1) + (i == 0 ? "depth" : "para" + i), 0, 0);
                }
            else 
                {
                int _min = EFFECT_PARAMETER_MINS[i][type];
                int _max = trueMax;
                model.set("effect" + (effect + 1) + "type" + (type + 1) + (i == 0 ? "depth" : "para" + i), _min);
                model.setMinMax("effect" + (effect + 1) + "type" + (type + 1) + (i == 0 ? "depth" : "para" + i), _min, _max);
                model.setMetricMinMax("effect" + (effect + 1) + "type" + (type + 1) + (i == 0 ? "depth" : "para" + i), _min, _max);
                }
            }
        }
 
    public HBox buildEffect(int effect, int type, Color color)
        {
        /*
          if (allEffects[effect][type] != null)
          {
          return allEffects[effect][type];
          }
        */
        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();

        int count = 0;
        for(int i = 0; i < 5; i++)
            {
            final int _i = i;
            // First we need to determine how to split up the labelled dial name into multiple lines
            String s[] = EFFECT_PARAMETER_NAMES[i][type].split(" ");
            for(int j = 0; j < s.length; j++) s[j] = s[j].replace("_", " ");
                        
            // We make sure the first dial name has two lines, so the effects categories sync up vertically
            if (s.length == 1)
                {
                String[] oldS = s;
                s = new String[2];
                s[0] = oldS[0];
                s[1] = "<html>&nbsp;</html>";  // this seems to work
                }
                        
            // Next, 1270, 720, and 200 are NOT the real "max value" -- they are placeholders for three
            // display functions.  The real max values are 127, 127, and 100 respectively.
            int trueMax = EFFECT_PARAMETER_MAXES[i][type];
            if (EFFECT_PARAMETER_MAXES[i][type] == 1270)
                {
                trueMax = 127;
                }
            else if (EFFECT_PARAMETER_MAXES[i][type] == 720)
                {
                trueMax = 127;
                }
            else if (EFFECT_PARAMETER_MAXES[i][type] == 200)
                {
                trueMax = 100;
                }
            
            int max = EFFECT_PARAMETER_MAXES[_i][type];
            if (max == -1)  // Not a parameter
                {
                // make some dummies
                model.set("effect" + (effect + 1) + "type" + (type + 1) + (i == 0 ? "depth" : "para" + i), 0);
                model.setMinMax("effect" + (effect + 1) + "type" + (type + 1) + (i == 0 ? "depth" : "para" + i), 0, 0);
                model.setMetricMinMax("effect" + (effect + 1) + "type" + (type + 1) + (i == 0 ? "depth" : "para" + i), 0, 0);
                }
            else 
                {
                count++;
                String key = "effect" + (effect + 1) + "type" + (type + 1) + (i == 0 ? "depth" : "para" + i);
                boolean addOne = (max == 99 || // all the 0...99 are displayed as 1-100
                    EFFECT_PARAMETER_NAMES[i][type].equals("Acceleration"));                // Rotary Acceleration is 0...9 but displayed 1...10            
                comp = lastDial = new LabelledDial(s[0], this, key, color, EFFECT_PARAMETER_MINS[i][type], trueMax, addOne ? -1 : 0)
                    {
                    public boolean isSymmetric()
                        {
                        return (max == 76);             // -12dB ... +12dB
                        }
                                        
                    public String map(int val)
                        {
                        // Based on the placeholder "max value", we need to determine the actual value to display
                        // on the LabelledDial.  The complicated one is 720, which means 0-100 by 2, 100-250 by 5, 250-720 by 10.
                        // The others are fairly easy.
                        if (max == 1)
                            {
                            return (val == 0 ? "Sin" : "Tri");
                            }
                        else if (max == 76)
                            {
                            return "" + (val - 64) + EFFECT_PARAMETER_UNITS[_i][type];
                            }
                        else if (max <= 100)
                            {
                            return String.valueOf(val) + EFFECT_PARAMETER_UNITS[_i][type];
                            }
                        else if (max == 1270)
                            {
                            return "<html><center>" + (val * 10) + "<br>" + EFFECT_PARAMETER_UNITS[_i][type] + "</center></html>";
                            }
                        else if (max == 720)
                            {
                            return "" + 
                                (val < 50 ? val * 2 :
                                    (val < 50 + 30 ? (val - 50) * 5 + 100 :
                                    (val - 50 - 30) * 10 + 250)) + EFFECT_PARAMETER_UNITS[_i][type];
                            }
                        else if (max == 200)
                            {
                            return "" + (val * 2) + EFFECT_PARAMETER_UNITS[_i][type];
                            }
                        else
                            {
                            return "What?";
                            }
                        }
                    };
                                
                        
                // Add the additional labels and put in the box
                for(int j = 1; j < s.length; j++) ((LabelledDial)comp).addAdditionalLabel(s[j]);
                hbox.add(comp);
                }
            }
        // Add filler
        for(int i = count; i < 5; i++)
            {
            hbox.add(Strut.makeStrut(lastDial));
            }

        //allEffects[effect][type] = hbox;        
        return hbox;
        }
 
    public JComponent addEffect(final int effect, Color color)
        {
        Category category = new Category(this, "Effect " + (effect + 1), color);
category.makePasteable("effect");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        params = EFFECT_TYPES;
        comp = new Chooser("Type", this, "effect" + (effect + 1) + "type", params)
            {
            public void update(String key, Model model) 
                { 
                super.update(key, model); 
                hbox.removeLast();
                hbox.addLast(buildEffect(effect, model.get("effect" + (effect + 1) + "type"), color));
                int type = model.get("effect" + (effect + 1) + "type");
                // We also want to send the effects
                for(int i = 0; i < 5; i++)
                    {
                    String paramkey = "effect" + (effect + 1) + "type" + (type + 1) + (i == 0 ? "depth" : "para" + i);
                    model.set(paramkey, model.get(paramkey));
                    }
                hbox.revalidate();
                hbox.repaint();         
                }
            };
        vbox.add(comp);
        hbox.add(vbox);

        hbox.revalidate();
        hbox.repaint(); 

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
        


    /// INSTRUMENTS GO:
        
    ///     VALUE           BANK    MODEL
    ///     0...127         "G"             No idea what this is
    /// 128...255   B               We don't support this
    ///     256-383         A               256-383
    ///     384-511         D               384-511
    ///                             E               Unknown at present
    ///                             F               Unknown at present
                        
    public JComponent addSection(int section, Color color)
        {
        Category category = new Category(this, "Section " + section, color);
category.makePasteable("section");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();

        final PushButton showButton = new PushButton("Show")
            {
            public void perform()
                {
                final KawaiK5000 synth = new KawaiK5000();
                if (tuple != null)
                    synth.tuple = new Midi.Tuple(tuple, synth.buildInReceiver(), synth.buildKeyReceiver(), synth.buildKey2Receiver());
                if (synth.tuple != null)
                    {       
                    // This is a little tricky.  When the dump comes in from the synth,
                    // Edisyn will only send it to the topmost panel.  So we first sprout
                    // the panel and show it, and THEN send the dump request.  But this isn't
                    // enough, because what setVisible(...) does is post an event on the
                    // Swing Event Queue to build the window at a later time.  This later time
                    // happens to be after the dump comes in, so it's ignored.  So what we
                    // ALSO do is post the dump request to occur at the end of the Event Queue,
                    // so by the time the dump request has been made, the window is shown and
                    // frontmost.
                                                
                    synth.setTitleBarAux("[Section " + section + " of " + KawaiK5000Multi.this.model.get("name", "") + "]");
                    synth.sprout();
                    JFrame frame = ((JFrame)(SwingUtilities.getRoot(synth)));
                    frame.setVisible(true);

                    invokeLater(
                        new Runnable()
                            {
                            public void run() 
                                { 
                                Model tempModel = buildModel();
                                int instrument = KawaiK5000Multi.this.model.get("section" + section + "inst") - 256;
                                int bank = instrument / 128;
                                int number = instrument % 128;
                                tempModel.set("number", number);
                                tempModel.set("bank", bank);     
                                synth.performRequestDump(tempModel, false);
                                }
                            });
                    }
                else
                    {
                    showSimpleError("Disconnected", "You can't show a patch when disconnected.");
                    }
                }
            };
        vbox.add(showButton);


        params = VELO_SWITCH_TYPES;
        comp = new Chooser("Velo Switch Type", this, "section" + section + "veloswtype", params);
        vbox.add(comp);
        hbox.add(vbox);        

        comp = new LabelledDial("Instrument", this, "section" + section + "inst", color, 256, 511 + 256)
            {
            public String map(int val) 
                {
                int v = val;
                if (val <= 383) v -= 256;
                else if (val <= 511) v -= 384;
                else if (val <= 639) v -= 512;
                else v -= 640;
                v += 1;
                
                if (val <= 383) return "A" + (v < 10 ? "00" : (v < 100 ? "0" : "")) + v;
                else if (val <= 511) return "D" + (v < 10 ? "00" : (v < 100 ? "0" : "")) + v;
                else if (val <= 639) return "E" + (v < 10 ? "00" : (v < 100 ? "0" : "")) + v;
                else return "F" + (v < 10 ? "00" : (v < 100 ? "0" : "")) + v;
                }
            };
        ((LabelledDial)comp).setMaxExtent(512);
        hbox.add(comp);

        comp = new LabelledDial("Volume", this, "section" + section + "volume", color, 0, 127);
        hbox.add(comp);
                
        /// FIXME: PAN on the MULTI GOES 0...127, NOT 1...127 LIKE ON SINGLES????
                
        comp = new LabelledDial("Pan", this, "section" + section + "pan", color, 0, 127, 64)
            {
            public String map(int val) 
                { 
                if (val == 64) return "--"; 
                else if (val < 64) return "<" + (64 - val);
                else return "" + (val - 64) + ">";
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Effect", this, "section" + section + "effectpath", color, 0, 3, -1);
        hbox.add(comp);

        comp = new LabelledDial("Transpose", this, "section" + section + "transpose", color, 40, 88, 64);
        hbox.add(comp);

        comp = new LabelledDial("Tune", this, "section" + section + "tune", color, 1, 127, 64);
        hbox.add(comp);

        comp = new LabelledDial("Zone Low", this, "section" + section + "zonelo", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Zone Hi", this, "section" + section + "zonehi", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Velo Switch", this, "section" + section + "veloswvalue", color, 1, 127);
        ((LabelledDial)comp).addAdditionalLabel("Value");
        hbox.add(comp);

        comp = new LabelledDial("Channel", this, "section" + section + "rcvch", color, 0, 15, -1);
        hbox.add(comp);
                
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
                

  
    public JFrame sprout()     
        {
        JFrame frame = super.sprout();
        receiveCurrent.setEnabled(false);                       // The K5000 can't do this
        //addKawaiK5000Menu();
        return frame;
        }

    public String getPatchName(Model model) { return model.get("name", "INIT    "); }

    public static final int BANK_M_MSB = 0x65;                // only has 64 PC values

    public void changePatch(Model tempModel)
        {
        byte NN = (byte)tempModel.get("number");
                
        int bankMSB = BANK_M_MSB;
        int bankLSB = 0;
        
        try 
            {
            // Bank Change
            tryToSendMIDI(new ShortMessage(ShortMessage.CONTROL_CHANGE, getChannelOut(), 0, bankMSB));
            tryToSendMIDI(new ShortMessage(ShortMessage.CONTROL_CHANGE, getChannelOut(), 32, bankLSB));
                
            // PC
            tryToSendMIDI(new ShortMessage(ShortMessage.PROGRAM_CHANGE, getChannelOut(), NN, 0));
            }
        catch (Exception e) { Synth.handleException(e); }
        }

    
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
        
        String n = null;
        int number = model.get("number") + 1;
        if (number < 10) n = "00" + number;
        else if (number < 100) n = "0" + number;
        else n = "" + number; 
        return n;                       // FIXME:               or should this be "C" + n?
        }

    // Verify that all the parameters are within valid values, and tweak them if not.
    public void revise()
        {
        // check the easy stuff -- out of range parameters
        super.revise();

        String nm = model.get("name", "INIT    ");
        String newnm = revisePatchName(nm);
        if (!nm.equals(newnm))
            model.set("name", newnm);
        }
    
    public void fix(Model model, String key, int val, int min, int max)
        {
        // Bugs in Kawai's patches result in 0 provided when it should be 33 for signed parameters (like -31 ... +31,
        // where 0 == 33).  For now we will assume that it meant 33
        if ((key.startsWith("effectcontrol") || key.startsWith("macrocontroller")) && 
            (key.contains("depth")) && val == 0)            // effectcontrol1depth, macrocontroller4depth2, etc.
            {
            model.set(key, 33);
            }
        else            // we also have one patch with source1formantlfodepth = 85, ugh
            {
            // bound
            int newVal = val;
            if (newVal < min) newVal = min;
            if (newVal > max) newVal = max;
            model.set(key, newVal);
            if (getPrintRevised()) System.out.println("Warning (Synth): Revised " + key + " from " + val + " to " + newVal + " (range " + min + " ... " + max + ")");             
            }
        }

    public static String getSynthName() { return "Kawai K5000S/K5000R [Multi]"; }
    
    // The K5000 can't send to temporary memory.  Presently we are only sending as individual parameters.
    public boolean getSendsAllParametersAsDump() { return false; }

    public int[] getNameAsBytes(Model model)
        {
        String name = model.get("name", "        ") + "        ";
        int[] bytes = new int[8];
        for(int i = 0; i < 8; i ++)
            {
            bytes[i] = name.charAt(i);
            }
        return bytes;
        }

    public Object[] emitAll(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        if (toWorkingMemory) return new Object[0];                      // For the time being
        
        if (tempModel == null)
            tempModel = getModel();
    
        byte[] data = new byte[9 + 54 + 4 * 12 + 1];
        
        data[0] = (byte)0xF0;
        data[1] = (byte)0x40;                                               // Kawai
        data[2] = (byte)getChannelOut();                    // Channel
        data[3] = (byte)0x20;
        data[4] = (byte)0x00;
        data[5] = (byte)0x0A;
        data[6] = (byte)0x20;
        data[7] = (byte)tempModel.get("number");            // patch
    
        int pos = 8;

        pos = emitMulti(model, data, pos);

        data[data.length - 1] = (byte)0xF7;
        return new Object[] { data };
        }


    // Given a model (which will never be null), writes out the full Single Tone of a patch, including all sources and wavekits, and all checksums,
    // starting at the given position in data
    public int emitMulti(Model model, byte[] data, int pos)
        {
        pos++;                      // skip checksum space
        int start = pos;

        // LOAD EFFECTS
    
        data[pos++] = (byte)model.get("algorithm");
        int reverbtype = model.get("reverbtype");
        data[pos++] = (byte)reverbtype;
        data[pos++] = (byte)model.get("reverb" + (reverbtype + 1) + "drywet1");
        data[pos++] = (byte)model.get("reverb" + (reverbtype + 1) + "para1");
        data[pos++] = (byte)model.get("reverb" + (reverbtype + 1) + "para2");
        data[pos++] = (byte)model.get("reverb" + (reverbtype + 1) + "para3");
        data[pos++] = (byte)model.get("reverb" + (reverbtype + 1) + "para4");
    
        for(int effect = 1; effect <= 4; effect++)                                  // NOTE <=
            {
            int effecttype = model.get("effect" + effect + "type");
            data[pos++] = (byte)(effecttype + 11);                                                      // effects start at 11
            data[pos++] = (byte)model.get("effect" + effect + "type" + (effecttype + 1) + "depth");
            data[pos++] = (byte)model.get("effect" + effect + "type" + (effecttype + 1) + "para1");
            data[pos++] = (byte)model.get("effect" + effect + "type" + (effecttype + 1) + "para2");
            data[pos++] = (byte)model.get("effect" + effect + "type" + (effecttype + 1) + "para3");
            data[pos++] = (byte)model.get("effect" + effect + "type" + (effecttype + 1) + "para4");
            }
                
        for(int freq = 1; freq <= 7; freq++)                                            // NOTE <=
            {
            data[pos++] = (byte)model.get("geqfreq" + freq);
            }
                
        // LOAD COMMON
        
        int[] name = getNameAsBytes(model);
        for(int i = 0; i < 8; i++)
            {
            data[pos++] = (byte)name[i];
            }
        data[pos++] = (byte)model.get("volume");

        int secmute = (byte)((1 - (model.get("secmute1")) << 0) |
            (1 - (model.get("secmute2")) << 1) |
            (1 - (model.get("secmute3")) << 2) |
            (1 - (model.get("secmute4")) << 3));
        data[pos++] = (byte)secmute;                                                    // bitpacking

        data[pos++] = (byte)model.get("effectcontrol1source");
        data[pos++] = (byte)model.get("effectcontrol1destination");
        data[pos++] = (byte)model.get("effectcontrol1depth");
        data[pos++] = (byte)model.get("effectcontrol2source");
        data[pos++] = (byte)model.get("effectcontrol2destination");
        data[pos++] = (byte)model.get("effectcontrol2depth");

        // LOAD SECTION DATA
    
        for(int i = 1; i <= 4; i++)                                           // note <=
            {
            for(int j = 0; j < sectionParams.length; j++)
                {
                String key = sectionParams[j];

                if (j == 0)             // instmsb
                    {
                    data[pos++] = (byte)(model.get("section" + i + "inst") >>> 7);
                    }
                else if (j == 1)        // instlsb
                    {
                    data[pos++] = (byte)(model.get("section" + i + "inst") & 127);
                    }
                else
                    {
                    data[pos++] = (byte)(model.get("section" + i + key));
                    }
                }
            }

        // COMMON AND SOURCE DATA CHECKSUM
    
        data[start - 1] = checksum(data, start, pos);
        return pos;
        }


    public Object[] emitBank(Model[] models, int bank, boolean toFile) 
        {
        byte[] data = new byte[7 + 64 * (1 + 54 + 4 * 12) + 1];
        
        data[0] = (byte)0xF0;
        data[1] = (byte)0x40;                                               // Kawai
        data[2] = (byte)getChannelOut();                    // Channel
        data[3] = (byte)0x21;
        data[4] = (byte)0x00;
        data[5] = (byte)0x0A;
        data[6] = (byte)0x20;
    
        int pos = 7;

        for(int i = 0; i < models.length; i++)
            {
            pos = emitMulti(models[i], data, pos);
            }

        data[data.length - 1] = (byte)0xF7;
        
        return new Object[] { data };
        }


    public int parseMulti(Model model, byte[] result, int pos)
        {   
        pos++;                                          // skip checksum space
                
        model.set("algorithm", result[pos++]);
        int reverbtype = result[pos++];
        model.set("reverbtype", reverbtype);
        model.set("reverb" + (reverbtype + 1) + "drywet1", result[pos++]);
        model.set("reverb" + (reverbtype + 1) + "para1", result[pos++]);
        model.set("reverb" + (reverbtype + 1) + "para2", result[pos++]);
        model.set("reverb" + (reverbtype + 1) + "para3", result[pos++]);
        model.set("reverb" + (reverbtype + 1) + "para4", result[pos++]);

        int missingEffectParams = 0;
        for(int effect = 1; effect <= 4; effect++)                                  // NOTE <=
            {
            // EFFECTS are PACKED.  So if an effect doesn't have a particular parameter, the NEXT PARAMETER fits in its slot.  
            // This is of course NOT documented

            int effectType = result[pos++] - 11;                                        // effects go 11...47
            if (effectType < 0 || effectType >= EFFECT_PARAMETER_MINS[0].length) effectType = 0;                        // this happens for Patch 71, which has a value of 0 for one of its effect types 
            
            model.set("effect" + effect + "type", effectType);
            if (EFFECT_PARAMETER_MINS[0][effectType] == NONE)
                { missingEffectParams++; }
            else { model.set("effect" + effect + "type" + (effectType + 1) + "depth", result[pos++]); }
            if (EFFECT_PARAMETER_MINS[1][effectType] == NONE)
                {  missingEffectParams++; }
            else { model.set("effect" + effect + "type" + (effectType + 1) + "para1", result[pos++]); }
            if (EFFECT_PARAMETER_MINS[2][effectType] == NONE)
                { missingEffectParams++; }
            else { model.set("effect" + effect + "type" + (effectType + 1) + "para2", result[pos++]); }
            if (EFFECT_PARAMETER_MINS[3][effectType] == NONE)
                { missingEffectParams++; }
            else { model.set("effect" + effect + "type" + (effectType + 1) + "para3", result[pos++]); }
            if (EFFECT_PARAMETER_MINS[4][effectType] == NONE)
                { missingEffectParams++; }
            else { model.set("effect" + effect + "type" + (effectType + 1) + "para4", result[pos++]); }
            pos += missingEffectParams;                                 // fill in the missing spaces
            missingEffectParams = 0;
            }
                
        for(int freq = 1; freq <= 7; freq++)                                            // NOTE <=
            {
            model.set("geqfreq" + freq, result[pos++]);
            }
        
        byte[] name = new byte[8];
        for(int i = 0; i < 8; i++)
            {
            name[i] = result[pos++];
            }
        model.set("name", new String(name));
        
        model.set("volume", result[pos++]);
        
        int secmute = result[pos++];
        model.set("secmute1", (1 - ((secmute >>> 0) & 1)));
        model.set("secmute2", (1 - ((secmute >>> 1) & 1)));
        model.set("secmute3", (1 - ((secmute >>> 2) & 1)));
        model.set("secmute4", (1 - ((secmute >>> 3) & 1)));
        
        model.set("effectcontrol1source", result[pos++]);
        model.set("effectcontrol1destination", result[pos++]);
        model.set("effectcontrol1depth", result[pos++]);
        model.set("effectcontrol2source", result[pos++]);
        model.set("effectcontrol2destination", result[pos++]);
        model.set("effectcontrol2depth", result[pos++]);

        for(int i = 1; i <= 4; i++)                                           // note <=
            {
            for(int j = 0; j < sectionParams.length; j++)
                {
                String key = sectionParams[j];

                if (j == 0)             // instmsb
                    {
                    int msb = result[pos++] << 7;
                    int lsb = result[pos++];
                    model.set("section" + i + "inst", msb | lsb);
                    }
                else if (j == 1)        // instlsb
                    {
                    // do nothing
                    }
                else
                    {
                    model.set("section" + i + key, result[pos++]);
                    }
                }
            }

        return pos;
        }
        
    public int parse(byte[] result, boolean fromFile)
        {
        if (result[3] == 0x20)          // single
            {
            model.set("number", result[7]);
                                
            int pos = 8;
            parseMulti(model, result, pos);

            revise();
            return PARSE_SUCCEEDED;
            }
        else
            {
            // gather bank names
            String[] patchBankNames = new String[64];
            for(int i = 0; i < 64; i++)
                {
                int pos = 7 + i * (1 + 54 + 4 * 12) + 1 + 38;           // 38 is offset to name
                byte[] name = new byte[8];
                for(int j = 0; j < 8; j++)
                    {
                    name[j] = result[pos++];
                    }
                patchBankNames[i] = new String(name);
                }

            // Now that we have an array of names, one per patch, we present the user with options;
            // 0. Cancel [handled automatically]
            // 1. Save the bank data [handled automatically]
            // 2. Upload the bank data [handled automatically] 
            // 3. Load and edit a certain patch number
            int patchNum = showBankSysexOptions(result, patchBankNames);
            if (patchNum < 0) return PARSE_CANCELLED;
            
            model.set("number", patchNum);

            return parseFromBank(result, patchNum);
            }
        }
        
    public int parseFromBank(byte[] bankSysex, int number) 
        {
        int pos = 7 + number * (1 + 54 + 4 * 12);
        
        parseMulti(model, bankSysex, pos);
        revise();
        return PARSE_SUCCEEDED;
        }

        
    public int getBatchDownloadFailureCountdown() { return 5; }
        
        
    // Computes the checksum on data[start] ... data[end - 1]
    public byte checksum(byte[] data, int start, int end)
        {
        // FIXME: the text appears to suggest that it is 0xa5 + sum mod 127, but it is unclear
        int sum = 0xa5;
        for(int i = start; i < end; i++)
            {
            sum += (data[i] & 0xFF);
            }
        return (byte)(sum & 127);
        }

    public Object[] emitAll(String key)
        {
        int[] data = null;              // { sub1, sub2, sub3, sub4, sub5, datahi, datalo };
        int[][] data8 = null;
        int sub5 = 0;
        Integer index = null;
        
        if (key.equals("number")) return new Object[0];
        else if (key.equals("bank")) return new Object[0];

        else if (key.startsWith("section"))
            {
            int section = StringUtility.getFirstInt(key);
            String remainder = StringUtility.removePreambleAndFirstDigits(key, "section");
            if (remainder.equals("inst"))
                {
                int val = model.get(key);
                data = new int[] { 0x04, 0x01, (section - 1), 0x00, 0x00, val >>> 7, val & 127 };
                }
            else
                {
                int val = model.get(key);
                if ((index = (Integer)(sectionParamsToIndex.get(remainder))) != null)
                    {
                    data = new int[] { 0x04, 0x01, (section - 1), 0x00, index.intValue() - 1, 0x00, val };
                    }
                else
                    {
                    System.err.println("Unknown Parameter " + key);
                    return new Object[0];
                    }
                }
            }
        else if (key.equals("name"))
            {
            data8 = new int[8][];
            int[] name = getNameAsBytes(model);
            for(int i = 0; i < data8.length; i++)
                {
                data8[i] = new int[] { 0x04, 0x00, 0x00, 0x00, i, 0x00, name[i] };
                }
            }
        else if (key.equals("algorithm"))
            {
            data = new int[] { 0x03, 0x00, 0x00, 0x00, 0x00, 0x00, model.get(key) };
            }
        else if (key.startsWith("reverb"))
            {
            int type = model.get("reverbtype");
            if (key.endsWith("type"))
                {
                data = new int[] { 0x03, 0x00, 0x01, 0x00, 0x00, 0x00, model.get(key) + 11 };           // effects start at 11
                }
            else 
                {
                if (key.endsWith("drywet1"))
                    {
                    if (type + 1 != StringUtility.getSecondInt(key))  return new Object[0]; // we're the wrong type
                    sub5 = 1;
                    }
                else if (key.endsWith("para1"))
                    {
                    if (type + 1 != StringUtility.getSecondInt(key))  return new Object[0]; // we're the wrong type
                    sub5 = 2;
                    }
                else if (key.endsWith("para2"))
                    {
                    if (type + 1 != StringUtility.getSecondInt(key))  return new Object[0]; // we're the wrong type
                    sub5 = 3;
                    }
                else if (key.endsWith("para3"))
                    {
                    if (type + 1 != StringUtility.getSecondInt(key))  return new Object[0]; // we're the wrong type
                    sub5 = 4;
                    }
                else if (key.endsWith("para4"))
                    {
                    if (type + 1 != StringUtility.getSecondInt(key))  return new Object[0]; // we're the wrong type
                    sub5 = 5;
                    }
                data = new int[] { 0x03, 0x00, 0x01, 0x00, sub5, 0x00, model.get(key) };
                }
            }
        else if (key.startsWith("effectcontrol"))
            {
            int effect = StringUtility.getFirstInt(key);
            boolean source = key.endsWith("source");
            boolean depth = key.endsWith("depth");
            if (source)
                {
                data = new int[] { 0x04, 0x00, 0x00, 0x00, effect == 1 ? 0x0B : 0x0E, 0x00, model.get(key) };
                }
            else if (depth)
                {
                data = new int[] { 0x04, 0x00, 0x00, 0x00, effect == 1 ? 0x0D : 0x10, 0x00, model.get(key) };
                }
            else
                {
                data = new int[] { 0x04, 0x00, 0x00, 0x00, effect == 1 ? 0x0C : 0x0F, 0x00, model.get(key) };
                }
            }
        else if (key.startsWith("effect"))
            {
            int effect = StringUtility.getFirstInt(key);
            int type = model.get("effect" + effect + "type");
            String reduced = StringUtility.removePreambleAndFirstDigits(key, "effect");
            if (key.endsWith("type"))
                {
                sub5 = 0;
                }
            else if (key.endsWith("depth"))
                {
                if (type + 1 != StringUtility.getSecondInt(key))  return new Object[0]; // we're the wrong type
                sub5 = 1;
                }
            else if (key.endsWith("para1"))
                {
                if (type + 1 != StringUtility.getSecondInt(key))  return new Object[0]; // we're the wrong type
                sub5 = 2;
                }
            else if (key.endsWith("para2"))
                {
                if (type + 1 != StringUtility.getSecondInt(key))  return new Object[0]; // we're the wrong type
                sub5 = 3;
                }
            else if (key.endsWith("para3"))
                {
                if (type + 1 != StringUtility.getSecondInt(key))  return new Object[0]; // we're the wrong type
                sub5 = 4;
                }
            else if (key.endsWith("para4"))
                {
                if (type + 1 != StringUtility.getSecondInt(key))  return new Object[0]; // we're the wrong type
                sub5 = 5;
                }
            data = new int[] { 0x03, 0x00, effect + 1, 0x00, sub5, 0x00, model.get(key) };
            }
        else if (key.startsWith("geq"))
            {
            int eq = StringUtility.getFirstInt(key);
            data = new int[] { 0x03, 0x01, 0x00, 0x00, eq, 0x00, model.get(key) };
            }
        else if (key.startsWith("secmute"))
            {
            // send all the secmutes as one blob
            data = new int[] { 0x04, 0x00, 0x00, 0x00, 0x0A, 0x00, 
                ((1 - model.get("secmute1")) << 0) |
                ((1 - model.get("secmute2")) << 1) |
                ((1 - model.get("secmute3")) << 2) |
                ((1 - model.get("secmute4")) << 3) };
            }
        else if ((index = (Integer)(multiDataParamsToIndex.get(key))) != null)
            {
            data = new int[] { 0x04, 0x00, 0x00, 0x00, index.intValue() - COMMON_START, 0x00, model.get(key) };
            }
        else
            {
            System.err.println("ERROR: Unknown parameter " + key);
            return new Object[0];
            }
                
        
        /// At this point we either have data or we have data8
                
        if (data8 != null)
            {
            Object[] retval = new Object[data8.length];
            for(int i = 0; i < data8.length; i++)
                {
                retval[i] = new byte[] 
                    { 
                    (byte)0xF0, 0x40, 0x00, 0x10, 0x00, 0x0A, 
                    (byte)data8[i][0], (byte)data8[i][1], (byte)data8[i][2], (byte)data8[i][3], (byte)data8[i][4], (byte)data8[i][5], (byte)data8[i][6],
                    (byte)0xF7 
                    };
                }
            return retval;
            }
        else
            {
            byte[] payload = new byte[] 
                { 
                (byte)0xF0, 0x40, 0x00, 0x10, 0x00, 0x0A, 
                (byte)data[0], (byte)data[1], (byte)data[2], (byte)data[3], (byte)data[4], (byte)data[5], (byte)data[6],
                (byte)0xF7 
                };
            return new Object[] { payload };
            }
        }


    public byte[] requestDump(Model tempModel)
        {
        if (tempModel == null)
            tempModel = getModel();

        return new byte[] 
            { 
            (byte)0xF0, 
            (byte)0x40, 
            (byte)getChannelOut(), 
            (byte)0x00, 
            (byte)0x00, 
            (byte)0x0A,
            (byte)0x20,
            (byte)0x00, 
            (byte)(tempModel.get("number")),
            (byte)0xF7
            };
        }

    public String[] getBankNames() { return BANKS; }

// Return a list of all patch number names.  
    public String[] getPatchNumberNames() { return buildIntegerNames(64, 1); }

// Return a list whether patches in banks are writeable.
    public boolean[] getWriteableBanks() { return new boolean[] { true }; }

// Return whether individual patches can be written.
    public boolean getSupportsPatchWrites() { return true; }

    public boolean getSupportsBankWrites() { return true; }

    public boolean getSupportsDownloads() { return true; }

    public int getPatchNameLength() { return 8; }

    public byte[] requestBankDump(int bank) 
        {
        return new byte[] 
            { 
            (byte)0xF0, 
            (byte)0x40, 
            (byte)getChannelOut(), 
            (byte)0x01, 
            (byte)0x00, 
            (byte)0x0A,
            (byte)0x20,
            (byte)0x00,
            (byte)0x00,
            (byte)0xF7
            };
        }

    public int getBank(byte[] bankSysex) 
        { 
        return 0;               // there's only one bank
        }

    public int getPauseAfterWriteBank() 
        {
        // Returns the pause, in milliseconds, after writing a bank sysex message
        // to the synthesizer.  By default this returns the value of 
        // getPauseAfterWritePatch();   This method only needs to be implemented 
        // if your patch editor supports bank reads (see documentation for 
        // getSupportsBankReads() and getSupportsBankWrites()).
        return getPauseAfterWritePatch(); 
        }    


    // General (non-source) parameter names
    public static final int COMMON_START = 38;
    public static final String[] multiDataParams = new String[] 
    {
    "algorithm",
    "reverbtype",
    "--",   // "reverbdrywet1",         // the actual parameter stored is "reverbNdrywet1" where N is the reverb type
    "--",   // "reverbpara1",           // ... and so on ...
    "--",   // "reverbpara2",
    "--",   // "reverbpara3",
    "--",   // "reverbpara4",
    "effect1type",
    "--",   // "effect1depth",          // the actual parameter stored is "effect1typeNdepth" where N is the effect type
    "--",   // "effect1para1",          // ... and so on ...
    "--",   // "effect1para2",
    "--",   // "effect1para3",
    "--",   // "effect1para4",
    "effect2type",
    "--",   // "effect2depth",
    "--",   // "effect2para1",
    "--",   // "effect2para2",
    "--",   // "effect2para3",
    "--",   // "effect2para4",
    "effect3type",
    "--",   // "effect3depth",
    "--",   // "effect3para1",
    "--",   // "effect3para2",
    "--",   // "effect3para3",
    "--",   // "effect3para4",
    "effect4type",
    "--",   // "effect4depth",
    "--",   // "effect4para1",
    "--",   // "effect4para2",
    "--",   // "effect4para3",
    "--",   // "effect4para4",
    "geqfreq1",
    "geqfreq2",
    "geqfreq3",
    "geqfreq4",
    "geqfreq5",
    "geqfreq6",
    "geqfreq7",
    "name",         // name1
    "--",           // name2
    "--",           // name3
    "--",           // name4
    "--",           // name5
    "--",           // name6
    "--",           // name7
    "--",           // name8
    "volume",
    "--",                       // this will be mute 1...4
    "effectcontrol1source",
    "effectcontrol1destination",
    "effectcontrol1depth",
    "effectcontrol2source",
    "effectcontrol2destination",
    "effectcontrol2depth",
    };
    
    // Names of various parameters (other than formants, harmonics, and harmonics envelopes, and the "wave kit") for a given source
    // The full name is "sectionNPARAM" where N is a number 1...6, and PARAM a string below.
    public static final String[] sectionParams = new String[] 
    {
    "--",               // instmsb
    "--",               // instlsb
    "volume",
    "pan",
    "effectpath",
    "transpose",
    "tune",
    "zonelo",
    "zonehi",
    "veloswtype",
    "veloswvalue",
    "rcvch",
    };
        
    public boolean testVerify(Synth synth2, String key, Object obj1, Object obj2) 
        {
        // We have lots of effects that will be zero regardless
        if (key.startsWith("effect")) return true;
        if (key.startsWith("reverb")) return true;
                        
        return false; 
        }
    }
                                        
