/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.kawaik1;

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
   A patch editor for the Kawai K1/K1r.
        
   @author Sean Luke
*/

public class KawaiK1 extends Synth
    {
    /// Various collections of parameter names for pop-up menus
        
    public static final String[] BANKS = { "IA", "IB", "IC", "ID", "iA", "iB", "iC", "iD", "EA", "EB", "EC", "ED", "eA", "eB", "eC", "eD"};
    public static final String[] WAVES = { "Sine 1st", "Sine 2nd", "Sine 3rd", "Sine 4th", "Sine 5th", "Sine 6th", "Sine 7th", "Sine 8th", "Sine 9th", "Sine 10th", "Sine 11th", "Sine 12th", "Sine 16th", 
                                           "Saw 1", "Saw 2", "Saw 3", "Saw 4", "Saw 5", "Saw 6", "Saw 7", "Saw 8", "Saw 9", "Saw 10", "Saw 11", "Saw 12", "Saw 13", "Saw 14", "Saw 15", "Saw 16", "Saw 17", "Saw 18", "Saw 19", 
                                           "Square 1", "Square 2", "Square 3", "Square 4", "Square 5", "Inverse", "Triangle", "Random", 
                                           "French Horn", "String 1", "String 2", "String Pad", "Piano 1", "El Grand 1", "El Piano 1", "El Piano 2", "El Piano 3", "Clavinet 1", "Vibe 1", 
                                           "A Guitar 1", "F Guitar 1", "F Guitar 2", "Ac Bass 1", "Ac Bass 2", "Digi Bass 1", "Pick Bass 1", "Digi Bass 2", "Round Bass 1", "Fretless 1", "Fretless 2", 
                                           "Flute 1", "PanFlute", "Harmonica 1", "Glocken", "Tine 1", "Harp 1", "Marimba 1", "El TomTom 1", "Log Drum", "Jazz Organ 1", "Mello Pad", "Synth Solo 1", "Synth 2", 
                                           "French Horn", "French Horn", "Brass 1", "Brass 2", "Brass 3", "Brass 4", "Trumpet 1", "Trumpet 2", "Violin", "String 3", 
                                           "Piano 2", "Piano 3", "Piano 4", "Piano 5", "Piano 6", "Piano 7", "Piano 8", "El Grand 2", "El Piano 4", "El Piano 5", "El Piano 6", "Clavinet 2", "Harpsicrd 1", "Vibe 2", 
                                           "A Guitar 2", "F Guitar 3", "Strat 1", "Strat 2", "Ac Bass", "Pull Bass", "Pull Bass 2", "Round Bass 2", "Slap Bass", "Slap Bass 2", "Slap Bass 3", "Fretless 3", "Fretless 4", 
                                           "Synth Bass 1", "Synth Bass 2", "Harmonica 2", "Clarinet 1", "Clarinet 2", "Oboe 1", "Oboe 2", "ShakuHachi", "Orient Bell 1", "Orient Bell 2", "Bell", "Koto 1", "Sitar", 
                                           "El TomTom 2", "Log Drum 1", "Log Drum 2", "Steel Drum 1", "Steel Drum 2", "Voice 1", "Voice 2", 
                                           "Accordion 1", "Accordion 2", "Jazz Organ 2", "Rock Organ 1", "Draw Bar 1", "Draw Bar 2", "Pipe Organ 1", "Pipe Organ 2", "Rock Organ 2", 
                                           "Synth Solo 2", "Synth Solo 3", "Synth 2", "Synth 3", "Synth 4", "Brass", "Brass 2", "Orchestra", 
                                           "Piano 9", "Piano 10", "El Piano 7", "El Piano 8", "El Piano 9", "El Piano 10", "Clavinet 3", "Harpsichd 2", "Harpsichd 3", "Vibe 3", "Digi Bass 3", "Digi Bass 4", "Digi Bass 5", "Pick Bass 2", 
                                           "Glocken", "Glocken 2", "Tine 2", "Tine 3", "Tine 4", "Tube Bell 1", "Tube Bell 2", "Tube Bell 3", "Xylophone 1", "Xylophone 2", "Harp 2", "Koto 2", "Sitar 2", "Sitar 3", 
                                           "Kalimba 1", "Kalimba 2", "Kalimba 3", "Log Drum", "Steel Drum", "Pipe Organ 3", "Pipe Organ 4", "Synth 5", "Synth 6", "Synth 7", "Synth 8", "Synth 9", "Synth 10", "Clavinet 4", 
                                           "Digi Bass 6", "Digi Bass 7", "Pick Bass 3", "Pick Bass 4", "Round Bass 3", "Round Bass 4", "Harmonica 3", "Harmonica 4", "Harp 3", "Koto 3", "Sitar 1", 
                                           "Marimba 2", "Synth 11", "Bass Drum", "Ac Snare", "Tight Snare", "E. Snare", "Rim", "Ac Tom", "High Hat", "Crash", "Ride", "Strat Guitar", "Fuzz Mute", "A Guitar 3", "F Guitar 4", "Guitar Harmonic", "Pull Bass", "Bass Harmonic", 
                                           "Bowed String", "String Attack", "String Sustain", "Pizzacato", "Piano PCM", "Electric Grand", "Piano Noise", "Trumpet", "Shakuhachi Attack", "Shakuhachi Sustain", "Pan Flute attack", "Pan Flute sustain", "Voice 3", "White Noise", 
                                           "String Loop", "Shakuhachi Loop", "Pan Flute Loop", "Voice Loop", "White Noise Loop", "Ac Snare Loop", "F. Guitar loop", "Pull Bass Loop", 
                                           "Omnibus Loop 1", "Omnibus Loop 2", "Omnibus Loop 3", "Omnibus Loop 4", "Omnibus Loop 5", "Omnibus Loop 6", "Omnibus Loop 7", "Omnibus Loop 8", 
                                           "Ac Snare Reverse", "Ac Tom Reverse", "F Guitar Reverse", "High Hat Alternate", "Crash Alternate", "Piano Noise Alternate" };
    public static final String[] KS_CURVES = { "Linear", "Exponential", "Logarithmic", "Ramped", "Split" };
    public static final String[] VELOCITY_CURVES = { "Linear", "Logarithmic", "Exponential", "Exponential Strong", "Off Then Linear", "Fast Middle", "Slow Middle", "Off Then Linear Down" };
    public static final String[] KEYS = new String[] { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
    public static final String[] LFO_SHAPES = new String[] { "Triangle", "Sawtooth", "Square", "Random" };
    public static final String[] SOURCES = new String[] { "2", "4" };
    public static final String[] POLY_MODES = new String[] { "Poly 1", "Poly 2", "Solo" };
    public static final String[] OUT_SELECTS = new String[] { "A", "B", "C", "D", "E", "F", "G", "H" };
    public static final String[] WHEEL_ASSIGNMENTS = new String[] { "Vibrato Depth", "Vibrato Speed" };
    public static final String[] AM_S1_S2 = new String[] { "Off", "AM S1 -> S2", "AM S2 -> S1" };
    public static final String[] AM_S3_S4 = new String[] { "Off", "AM S3 -> S4", "AM S4 -> S3" };

    public KawaiK1()
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
                
        JComponent soundPanel = new SynthPanel(this);
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        hbox.addLast(addSourceGlobal(Style.COLOR_A()));
        vbox.add(hbox);
        
        hbox = new HBox();
        hbox.add(addVibrato(Style.COLOR_C()));
        hbox.addLast(addAutoBend(Style.COLOR_C()));
        vbox.add(hbox);
        
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Global", soundPanel);
                

        JComponent sourcePanel = new SynthPanel(this);
        vbox = new VBox();
        
        vbox.add(addSource(1, Style.COLOR_A()));
        vbox.add(addEnvelope(1, Style.COLOR_B()));

        vbox.add(addSource(2, Style.COLOR_A()));
        vbox.add(addEnvelope(2, Style.COLOR_B()));

        sourcePanel.add(vbox, BorderLayout.CENTER);
        addTab("Sources 1-2", sourcePanel);

        sourcePanel = new SynthPanel(this);
        vbox = new VBox();
        
        vbox.add(addSource(3, Style.COLOR_A()));
        vbox.add(addEnvelope(3, Style.COLOR_B()));

        vbox.add(addSource(4, Style.COLOR_A()));
        vbox.add(addEnvelope(4, Style.COLOR_B()));

        sourcePanel.add(vbox, BorderLayout.CENTER);
        addTab("Sources 3-4", sourcePanel);
        

        model.set("name", "Init Patch");  // has to be 10 long

        model.set("bank", 0);
        model.set("number", 0);
        
        loadDefaults();        
        }
                
                
    public JFrame sprout()
        {
        JFrame frame = super.sprout();
        // We can't request the current working memory (don't ask why)
        receiveCurrent.setEnabled(false);

        // We can't reasonably send to patches if we send in bulk.
        transmitTo.setEnabled(false);

        addKawaiK1Menu();
        return frame;
        }         

    public String getDefaultResourceFileName() { return "KawaiK1.init"; }
    public String getHTMLResourceFileName() { return "KawaiK1.html"; }

    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        JComboBox bank = new JComboBox(BANKS);
        bank.setSelectedIndex(model.get("bank"));
        
        JTextField number = new JTextField("" + (model.get("number") + 1), 3);

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
                showSimpleError(title, "The Patch Number must be an integer 1...8");
                continue;
                }
            if (n < 1 || n > 8)
                {
                showSimpleError(title, "The Patch Number must be an integer 1...8");
                continue;
                }
                
            n--;
                                
            int i = bank.getSelectedIndex();
                        
            change.set("bank", i);
            change.set("number", n);
                        
            return true;
            }
        }

    boolean sendKawaiParametersInBulk = true;
        
    public void addKawaiK1Menu()
        {
        JMenu menu = new JMenu("Kawai K1");
        menubar.add(menu);

        // classic patch names
                
        JMenu sendParameters = new JMenu("Send Parameters");
        menu.add(sendParameters);
                
        String str = getLastX("SendParameters", getSynthName(), true);
        if (str == null)
            sendKawaiParametersInBulk = true;
        else if (str.equalsIgnoreCase("BULK"))
            sendKawaiParametersInBulk = true;
        else if (str.equalsIgnoreCase("INDIVIDUALLY"))
            sendKawaiParametersInBulk = false;
        else sendKawaiParametersInBulk = true;

        ButtonGroup bg = new ButtonGroup();

        JRadioButtonMenuItem separately = new JRadioButtonMenuItem("Individually");
        separately.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt)
                {
                sendKawaiParametersInBulk = false;
                setLastX("INDIVIDUALLY", "SendParameters", getSynthName(), true);
                }
            });
        sendParameters.add(separately);
        bg.add(separately);
        if (sendKawaiParametersInBulk == false) 
            separately.setSelected(true);

        JRadioButtonMenuItem bulk = new JRadioButtonMenuItem("In Bulk, Overwriting Patch iD-8");
        bulk.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt)
                {
                sendKawaiParametersInBulk = true;
                setLastX("BULK", "SendParameters", getSynthName(), true);
                }
            });
        sendParameters.add(bulk);
        bg.add(bulk);
        if (sendKawaiParametersInBulk == true) 
            bulk.setSelected(true);
                
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

        hbox.add(Strut.makeHorizontalStrut(50));

        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }


    public JComponent addSourceGlobal( Color color)
        {
        Category category = new Category(this, "Sources Global", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        HBox hbox2 = new HBox();
        VBox vbox = new VBox();
        params = SOURCES;
        comp = new Chooser("Sources", this, "sources", params);
        vbox.add(comp);

        params = POLY_MODES;
        comp = new Chooser("Poly Mode", this, "polymode", params);
        vbox.addBottom(comp);
        
        params = WHEEL_ASSIGNMENTS;
        comp = new Chooser("Mod Wheel", this, "wheelassign", params);
        vbox.add(comp);  
        hbox2.add(vbox);
        
        vbox = new VBox();
        vbox.addBottom(hbox2);
        hbox.add(vbox);

        comp = new LabelledDial("Pitch Bend", this, "pitchbend", color, 0, 12);
        ((LabelledDial)comp).addAdditionalLabel("Range");
        hbox.add(comp);

        // this appears to be poorly named in the manual (page 36)
        comp = new LabelledDial("Pressure", this, "pres>freq", color, 0, 100, 50);
        ((LabelledDial)comp).addAdditionalLabel("Pitch Mod");
        hbox.add(comp);

        comp = new LabelledDial("Volume", this, "volume", color, 0, 99, -1);
        hbox.add(comp);

        vbox = new VBox();
        params = KS_CURVES;
        comp = new Chooser("Key Scaling Curve", this, "kscurve", params);
        vbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }



    public JComponent addVibrato(Color color)
        {
        Category category = new Category(this, "Vibrato LFO", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = LFO_SHAPES;
        comp = new Chooser("Shape", this, "lfo1shape", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Speed", this, "lfo1speed", color, 0, 100);
        hbox.add(comp);
        
        comp = new LabelledDial("Depth", this, "lfo1dep", color, 0, 100, 50);
        hbox.add(comp);
        
        comp = new LabelledDial("Pressure", this, "lfo1prs>dep", color, 0, 100, 50);
        ((LabelledDial)comp).addAdditionalLabel("Depth Mod");
        hbox.add(comp);
        
        // separate a bit from Auto Bend
        hbox.add(Strut.makeHorizontalStrut(60));
        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addAutoBend(Color color)
        {
        Category category = new Category(this, "Auto Bend", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        comp = new LabelledDial("Time", this, "autobendtime", color, 0, 100);
        hbox.add(comp);
        
        comp = new LabelledDial("Depth", this, "autobenddepth", color, 0, 100, 50);
        hbox.add(comp);
        
        comp = new LabelledDial("Key Scaling", this, "autobendks>time", color, 0, 100, 50);
        ((LabelledDial)comp).addAdditionalLabel("Time Mod");
        hbox.add(comp);
        
        comp = new LabelledDial("Velocity", this, "autobendvel>dep", color, 0, 100, 50);
        ((LabelledDial)comp).addAdditionalLabel("Depth Mod");
        hbox.add(comp);
                
        EnvelopeDisplay disp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "autobendtime" }, 
            new String[] { "autobenddepth", null  },
            new double[] { 0, 1.0/100.0 },
            new double[] { 1.0 / 100.0, 50 / 100.0}
            );
        disp.setAxis(50 / 100.0);
        comp = disp;
        hbox.addLast(comp);    
        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addSource(int src, Color color)
        {
        Category category = new Category(this, "Source " + src, color);
        category.makePasteable("s" + src);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = WAVES;
        comp = new Chooser("Wave", this, "s" + src + "waveselect", params);
        vbox.add(comp);
             
        params = VELOCITY_CURVES;
        comp = new Chooser("Velocity Curve", this, "s" + src + "velcurve", params);
        vbox.add(comp);
        hbox.add(vbox);

        vbox = new VBox();
        if (src == 1)
            {
            params = AM_S1_S2;
            comp = new Chooser("AM (Ring Modulation)", this, "s1ams1>s2", params);
            vbox.add(comp);
            }
        else if (src == 3)
            {
            params = AM_S3_S4;
            comp = new Chooser("AM (Ring Modulation)", this, "s3ams3>s4", params);
            vbox.add(comp);
            }

        // Normally this is in global, but I think it makes more sense here
        comp = new CheckBox("Mute", this, "s" + src + "mute");
        vbox.add(comp);
        
        hbox.add(vbox);
                
        vbox = new VBox();
        comp = new CheckBox("Keytrack", this, "s" + src + "keytrack");
        vbox.add(comp);

        comp = new CheckBox("Pressure -> Pitch", this, "s" + src + "prs>frqsw");
        vbox.add(comp);

        comp = new CheckBox("Vibrato/Auto Bend", this, "s" + src + "vib/a.bendsw");
        vbox.add(comp);
        hbox.add(vbox);
                
        // these merge... :-(
        comp = new LabelledDial("Fixed Key", this, "s" + src + "fix", color, 0, 127)
            {
            public String map(int val)
                {
                return KEYS[val % 12] + (val / 12 + 1);  // note integer division
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Coarse", this, "s" + src + "coarse", color, 60, 108, 60 + 24)
            {
            public boolean isSymmetric() { return true; }
            };
        hbox.add(comp);

        comp = new LabelledDial("Fine", this, "s" + src + "fine", color, 0, 100, 50);
        hbox.add(comp);

        // yeah, I know this should be in the DCA, but it
        // makes the window smaller here.  Also src = envelope so we're okay
        comp = new LabelledDial("Level", this, "s" + src + "envelopelevel", color, 0, 100);
        hbox.add(comp);

        comp = new LabelledDial("Key Scaling", this, "s" + src + "freqks>freq", color, 0, 100, 50);
        ((LabelledDial)comp).addAdditionalLabel("Pitch Mod");
        hbox.add(comp);

        // DADSR

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
                
    /** Add add a DCA category */
    public JComponent addEnvelope(int envelope, Color color)
        {
        Category category = new Category(this, "Envelope " + envelope, color);
        category.makePasteable("s" + envelope);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
               
        comp = new LabelledDial("Delay", this, "s" + envelope + "envelopedelay", color, 0, 100);
        hbox.add(comp);
        
        comp = new LabelledDial("Attack", this, "s" + envelope + "envelopeattack", color, 0, 100);
        hbox.add(comp);

        comp = new LabelledDial("Decay", this, "s" + envelope + "envelopedecay", color, 0, 100);
        hbox.add(comp);

        comp = new LabelledDial("Sustain", this, "s" + envelope + "envelopesustain", color, 0, 100);
        hbox.add(comp);

        comp = new LabelledDial("Release", this, "s" + envelope + "enveloperelease", color, 0, 100);
        hbox.add(comp);
        
        comp = new LabelledDial("Velocity", this, "s" + envelope + "levelmodvel", color, 0, 100, 50);
        ((LabelledDial)comp).addAdditionalLabel("Level Mod");
        hbox.add(comp);
        
        comp = new LabelledDial("Pressure", this, "s" + envelope + "levelmodprs", color, 0, 100, 50);
        ((LabelledDial)comp).addAdditionalLabel("Level Mod");
        hbox.add(comp);
        
        comp = new LabelledDial("Key Scaling", this, "s" + envelope + "levelmodks", color, 0, 100, 50);
        ((LabelledDial)comp).addAdditionalLabel("Level Mod");
        hbox.add(comp);
        
        comp = new LabelledDial("Velocity", this, "s" + envelope + "timemodvel", color, 0, 100, 50);
        ((LabelledDial)comp).addAdditionalLabel("Time Mod");
        hbox.add(comp);
        
        comp = new LabelledDial("Key Scaling", this, "s" + envelope + "timemodks", color, 0, 100, 50);
        ((LabelledDial)comp).addAdditionalLabel("Time Mod");
        hbox.add(comp);


        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "s" + envelope + "delay", "s" + envelope + "envelopeattack", "s" + envelope + "envelopedecay", null, "s" + envelope + "enveloperelease" },
            new String[] { null, null, null, "s" + envelope + "envelopesustain", "s" + envelope + "envelopesustain", null },
            new double[] { 0, 0.2/100.0, 0.2/100.0, 0.2 / 100.0,  0.2, 0.2/100.0},
            new double[] { 0, 0.0, 1.0, 1.0 / 100.0, 1.0/100.0, 0 });
        hbox.addLast(comp);
                
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
        

    HashMap internalParametersToIndex = new HashMap();
        
    final static String[] internalParameters = new String[]
    {
    "-",
    "-",
    "-",
    "volume",
    "name1",
    "name2",
    "name3",
    "name4",
    "name5",
    "name6",
    "name7",
    "name8",
    "name9",
    "name10",
    "sources",
    "lfo1dep",
    "lfo1speed",
    "lfo1shape",
    "lfo1prs>dep",
    "wheelassign",
    "autobenddepth",
    "autobendtime",
    "autobendvel>dep",
    "autobendks>time",
    "pres>freq",
    "pitchbend",
    "kscurve",
    "polymode",
    "s:coarse",
    "s:fix",
    "s:fine",
    "s:keytrack",
    "s:vib/a.bendsw",
    "s:prs>frqsw",
    "s:freqks>freq",
    "s:waveselect",
    "s1ams1>s2",
    "s3ams3>s4",
    "-",
    "-",
    "-",
    "s:envelopelevel",
    "s:envelopedelay",
    "s:envelopeattack",
    "s:envelopedecay",
    "s:envelopesustain",
    "s:enveloperelease",
    "s:velcurve",
    "s:levelmodvel",
    "s:levelmodprs",
    "s:levelmodks",
    "s:timemodvel",
    "s:timemodks"
    };


    /** Map of parameter -> index in the allParameters array. */
    HashMap allParametersToIndex = new HashMap();

    /** List of all K1 parameters in order.  "-" is a reserved (unused and thus unnamed) parameter. */

    /// * indicates parameters which must be handled specially

    final static String[] allParameters = new String[/*100 or so*/] 
    {
    "name1",                // this is the name
    "name2",
    "name3",
    "name4",
    "name5",
    "name6",
    "name7",
    "name8",
    "name9",
    "name10",
    "volume",                   
    "polymode_sources_s1ams1>s2_s3ams3>s4",              // *
    "pres>freq",
    "lfo1dep",
    "lfo1prs>dep",
    "pitchbend",
    "lfo1speed",
    "lfo1shape_kscurve_wheelassign",                    // * 
    "autobenddepth",
    "autobendtime",           
    "autobendvel>dep",
    "autobendks>time",           
    "s1mute_s2mute_s3mute_s4mute",              // *
    "s1fine",
    "s2fine",
    "s3fine",
    "s4fine",
    "s1coarsefix",                      // *
    "s2coarsefix",                      // *
    "s3coarsefix",                      // *
    "s4coarsefix",                      // *
    "s1waveselectlo",                
    "s2waveselectlo",                
    "s3waveselectlo",                
    "s4waveselectlo",                
    "s1waveselecthi_keytrack_vib/a.bendsw_prs>frqsw_velcurve",          //*
    "s2waveselecthi_keytrack_vib/a.bendsw_prs>frqsw_velcurve",          //*
    "s3waveselecthi_keytrack_vib/a.bendsw_prs>frqsw_velcurve",          //*
    "s4waveselecthi_keytrack_vib/a.bendsw_prs>frqsw_velcurve",          //*
    "s1envelopelevel",     
    "s2envelopelevel",     
    "s3envelopelevel",     
    "s4envelopelevel",     
    "s1envelopedelay",
    "s2envelopedelay",
    "s3envelopedelay",
    "s4envelopedelay",
    "s1envelopeattack", 
    "s2envelopeattack", 
    "s3envelopeattack", 
    "s4envelopeattack", 
    "s1envelopedecay",
    "s2envelopedecay",
    "s3envelopedecay",
    "s4envelopedecay",
    "s1envelopesustain",
    "s2envelopesustain",
    "s3envelopesustain",
    "s4envelopesustain",
    "s1enveloperelease",
    "s2enveloperelease",
    "s3enveloperelease",
    "s4enveloperelease",
    "s1levelmodvel",
    "s2levelmodvel",
    "s3levelmodvel",
    "s4levelmodvel",
    "s1levelmodprs",
    "s2levelmodprs",
    "s3levelmodprs",
    "s4levelmodprs",
    "s1levelmodks",
    "s2levelmodks",
    "s3levelmodks",
    "s4levelmodks",
    "s1timemodvel",
    "s2timemodvel",
    "s3timemodvel",
    "s4timemodvel",
    "s1timemodks",
    "s2timemodks",
    "s3timemodks",
    "s4timemodks",
    "s1freqks>freq",
    "s2freqks>freq",
    "s3freqks>freq",
    "s4freqks>freq",
    };


    // The K1 can't send to temporary memory
    public boolean getSendsAllParametersInBulk() { return sendKawaiParametersInBulk; }

    public Object[] emitAll(String key)
        {
        if (key.equals("bank")) return new Object[0];  // this is not emittable
        if (key.equals("number")) return new Object[0];  // this is not emittable
        if (key.equals("-")) return new Object[0];  // uh...
                
        if (key.equals("name"))
            {
            // you can't emit a name as a parameter: it's ignored
            return new Object[0];
            }
        else 
            {
            int source = 0;
            int index = 0;
            String newkey = key;
            byte msb = (byte)(model.get(key) >>> 7);         // particularly for "waveselect"
            byte lsb = (byte)(model.get(key) & 127);

            // These CANNOT be set directly as parameters, but they can be simulated by turning the volume to 0.
            if (key.equals("s1mute") || key.equals("s2mute") || key.equals("s3mute") || key.equals("s4mute"))
                {
                if (key.startsWith("s1"))
                    source = 0;
                else if (key.startsWith("s2"))
                    source = 1;
                else if (key.startsWith("s3"))
                    source = 2;
                else source = 3;
                                
                index = ((Integer)(internalParametersToIndex.get("s:envelopelevel")));
                msb = (byte)0;
                if (lsb == 1)  // mute is ON
                    lsb = 0;        // set level to 0
                else                    // mute is OFF
                    lsb = (byte)(model.get("s" + (source + 1) + "envelopelevel"));
                }
            else if (key.equals("s1ams1>s2") || key.equals("s3ams3>s4"))
                {
                index = ((Integer)(internalParametersToIndex.get(newkey))).intValue();
                }
            else if (key.startsWith("s1"))
                {
                source = 0;
                newkey = "s:" + key.substring(2);
                }
            else if (key.startsWith("s2"))
                {
                source = 1;
                newkey = "s:" + key.substring(2);
                }
            else if (key.startsWith("s3"))
                {
                source = 2;
                newkey = "s:" + key.substring(2);
                }
            else if (key.startsWith("s4"))
                {
                source = 3;
                newkey = "s:" + key.substring(2);
                }
            else if (key.startsWith("f1"))
                {
                source = 0;
                newkey = "f:" + key.substring(2);
                }
            else if (key.startsWith("f2"))
                {
                source = 1;
                newkey = "f:" + key.substring(2);
                }
                
            // handle envelopelevel specially due to mutes above
            if (newkey.equals("s:envelopelevel"))
                {
                int mute = model.get("s" + (source + 1) + "mute");
                if (mute == 1)  // mute is ON
                    lsb = 0;        // set level to 0
                index = ((Integer)(internalParametersToIndex.get(newkey))).intValue();
                }
            else if (key.equals("s1mute") || key.equals("s2mute") || key.equals("s3mute") || key.equals("s4mute"))
                {
                // index already handled
                }
            else
                {
                index = ((Integer)(internalParametersToIndex.get(newkey))).intValue();
                }
            byte[] data = new byte[] { (byte)0xF0, 0x40, (byte)getChannelOut(), 0x10, 0x00, 0x03, (byte)index, (byte)((source << 1) | msb), (byte)lsb, (byte)0xF7 };
            return new Object[] { data };
            }
        }
    

    public void parseParameter(byte[] data)
        {
        if (data.length == 9 &&                 // write error report
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x40 &&
            data[3] >= (byte)0x41 &&
            data[3] <= (byte)0x43 &&
            data[4] == (byte)0x00 &&
            data[5] == (byte)0x03)
            {
            String error = "Write Failed (Maybe Transmission Failure)";
            // dump failed
            if (data[3] == 0x42)
                error = "Patch is Write-Protected";
            else if (data[3] == 0x43)
                error = "External Data Card is Not Inserted";
                        
            showSimpleError("Write Failed", error);
            }
        }

    public int parse(byte[] data, boolean fromFile)
        {
        if (data[3] == (byte)0x20) // single
            {               
            model.set("bank", (data[7] / 8) + (data[6] == 0x00 ? 0 : 8));
            model.set("number", data[7] % 8);
            return subparse(data, 8);
            }
        else                            // block 
            {
            // extract names
            char[][] names = new char[32][10];
            for(int i = 0; i < 32; i++)
                {
                for (int j = 0; j < 10; j++)
                    {
                    names[i][j] = (char)(data[8 + (i * 88) + j] & 127);
                    }
                }
                        
            String[] n = new String[32];
            for(int i = 0; i < 32; i++)
                {
                n[i] = "" + (i + 1) + "   " + new String(names[i]);
                } 
                
            // Now that we have an array of names, one per patch, we present the user with options;
            // 0. Cancel [handled automatically]
            // 1. Save the bank data [handled automatically]
            // 2. Upload the bank data [handled automatically] 
            // 3. Load and edit a certain patch number
            int patchNum = showBankSysexOptions(data, n);
            if (patchNum < 0) return PARSE_CANCELLED;
            
            boolean upper = (data[7] == 0);  // == 0 is I or E, == 0x20 is i or e
            boolean internal = (data[6] == 0);
            
            model.set("bank", (patchNum / 8) + (internal ? 0 : 8) + (upper ? 0 : 4));
            model.set("number", patchNum % 8);

            // okay, we're loading and editing patch number patchNum.  Here we go.
            return subparse(data, patchNum * 88 + 8);         
            }
                
        }
        
    public int subparse(byte[] data, int pos)
        {
        byte[] name = new byte[10];
                        
        // The K1 is riddled with byte-mangling.  :-(
        
        // gotta store these because they appear first
        byte[] coarsefix = new byte[4];
        
        for(int i = 0; i < 87; i++)
            {
            String key = allParameters[i];
                                
            if (i < 10)  // name
                {
                name[i] = data[i + pos];
                }
            else if (key.equals("s1coarsefix"))
                {
                coarsefix[0] = data[i + pos];
                }
            else if (key.equals("s2coarsefix"))
                {
                coarsefix[1] = data[i + pos];
                }
            else if (key.equals("s3coarsefix"))
                {
                coarsefix[2] = data[i + pos];
                }
            else if (key.equals("s4coarsefix"))
                {
                coarsefix[3] = data[i + pos];
                }
            else if (key.equals("polymode_sources_s1ams1>s2_s3ams3>s4"))
                {
                model.set("polymode", data[i + pos] & 3);
                model.set("sources", (data[i + pos] >>> 2) & 1);
                model.set("s1ams1>s2", (data[i + pos] >>> 3) & 3);
                model.set("s3ams3>s4", (data[i + pos] >>> 5) & 3);
                }
            else if (key.equals("s1mute_s2mute_s3mute_s4mute"))
                {
                // In the Kawai *K4* manual, there is an error: 0 is mute OFF and 1 is MUTE ON.
                // I don't know if this holds for the K1.  We'll find out soon....
                model.set("s1mute", (data[i + pos] & 1));
                model.set("s2mute", ((data[i + pos] >>> 1) & 1));
                model.set("s3mute", ((data[i + pos] >>> 2) & 1));
                model.set("s4mute", ((data[i + pos] >>> 3) & 1));
                }
            else if (key.equals("lfo1shape_kscurve_wheelassign"))
                {
                model.set("lfo1shape", data[i + pos] & 3);
                model.set("kscurve", (data[i + pos] >>> 2) & 7);
                model.set("wheelassign", (data[i + pos] >>> 5) & 3);
                }
            else if (key.equals("s1waveselecthi_keytrack_vib/a.bendsw_prs>frqsw_velcurve"))
                {
                model.set("s1waveselect", ((data[i + pos] & 1) << 7) | (data[i + pos - 4]));                // hi and lo
                model.set("s1keytrack", (data[i + pos] >>> 1) & 1);
                model.set("s1vib/a.bendsw", (data[i + pos] >>> 2) & 1);
                model.set("s1prs>frqsw", (data[i + pos] >>> 3) & 1);
                model.set("s1velcurve", (data[i + pos] >>> 4) & 7);
                
                // now that we know keytrack, we can compute coarsefix
                if (model.get("s1keytrack", 0) == 0)
                    {
                    model.set("s1fix", coarsefix[0]);
                    model.set("s1coarse", 84);  // centered
                    }
                else
                    {
                    model.set("s1fix", 0);
                    model.set("s1coarse", coarsefix[0]);
                    }
                }
            else if (key.equals("s2waveselecthi_keytrack_vib/a.bendsw_prs>frqsw_velcurve"))
                {
                model.set("s2waveselect", ((data[i + pos] & 1) << 7) | (data[i + pos - 4]));                // hi and lo
                model.set("s2keytrack", (data[i + pos] >>> 1) & 1);
                model.set("s2vib/a.bendsw", (data[i + pos] >>> 2) & 1);
                model.set("s2prs>frqsw", (data[i + pos] >>> 3) & 1);
                model.set("s2velcurve", (data[i + pos] >>> 4) & 7);

                // now that we know keytrack, we can compute coarsefix
                if (model.get("s2keytrack", 0) == 0)
                    {
                    model.set("s2fix", coarsefix[1]);
                    model.set("s2coarse", 84);  // centered
                    }
                else
                    {
                    model.set("s2fix", 0);
                    model.set("s2coarse", coarsefix[1]);
                    }
                }
            else if (key.equals("s3waveselecthi_keytrack_vib/a.bendsw_prs>frqsw_velcurve"))
                {
                model.set("s3waveselect", ((data[i + pos] & 1) << 7) | (data[i + pos - 4]));                // hi and lo
                model.set("s3keytrack", (data[i + pos] >>> 1) & 1);
                model.set("s3vib/a.bendsw", (data[i + pos] >>> 2) & 1);
                model.set("s3prs>frqsw", (data[i + pos] >>> 3) & 1);
                model.set("s3velcurve", (data[i + pos] >>> 4) & 7);

                // now that we know keytrack, we can compute coarsefix
                if (model.get("s3keytrack", 0) == 0)
                    {
                    model.set("s3fix", coarsefix[2]);
                    model.set("s3coarse", 84);  // centered
                    }
                else
                    {
                    model.set("s3fix", 0);
                    model.set("s3coarse", coarsefix[2]);
                    }
                }
            else if (key.equals("s4waveselecthi_keytrack_vib/a.bendsw_prs>frqsw_velcurve"))
                {
                model.set("s4waveselect", ((data[i + pos] & 1) << 7) | (data[i + pos - 4]));                // hi and lo
                model.set("s4keytrack", (data[i + pos] >>> 1) & 1);
                model.set("s4vib/a.bendsw", (data[i + pos] >>> 2) & 1);
                model.set("s4prs>frqsw", (data[i + pos] >>> 3) & 1);
                model.set("s4velcurve", (data[i + pos] >>> 4) & 7);

                // now that we know keytrack, we can compute coarsefix
                if (model.get("s4keytrack", 0) == 0)
                    {
                    model.set("s4fix", coarsefix[3]);
                    model.set("s4coarse", 84);  // centered
                    }
                else
                    {
                    model.set("s4fix", 0);
                    model.set("s4coarse", coarsefix[3]);
                    }
                }
            else if (key.equals("s1waveselectlo"))
                {
                // do nothing, already done
                }
            else if (key.equals("s2waveselectlo"))
                {
                // do nothing, already done
                }
            else if (key.equals("s3waveselectlo"))
                {
                // do nothing, already done
                }
            else if (key.equals("s4waveselectlo"))
                {
                // do nothing, already done
                }
            else
                {
                model.set(key, data[i + pos]);
                }
            }

        try { model.set("name", new String(name, "US-ASCII")); }
        catch (UnsupportedEncodingException e) { e.printStackTrace(); }

        revise();
        return PARSE_SUCCEEDED;
        }
    
        
    /** Generate a K4 checksum of the data bytes */
    byte produceChecksum(byte[] bytes)
        {
        //      The K4 manual says the checksum is the
        //              "Sum of the A5H and s0~s129".
        //              I believe this is A5 + sum(s0...s129) ignoring overflow, cut to 7 bits

        int checksum = 0xA5;
        for(int i = 0; i < bytes.length; i++)
            checksum = (checksum + bytes[i]) & 255;
        return (byte)(checksum & 127);
        }


        
    public int getSysexFragmentSize() 
        {
        return 16;
        }
        
    public int getPauseBetweenSysexFragments()
        {
        return 70;
        }
        
    public void sendAllParameters()
        {
        super.sendAllParameters();
        
        // we change patch to #63 if we're sending in bulk.
        if (sendKawaiParametersInBulk)
            {
            Model tempModel = new Model();
            tempModel.set("bank", 7);
            tempModel.set("number", 7);
            changePatch(tempModel);
            simplePause(getPauseAfterChangePatch());
            }
        }

    public byte[] emit(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        if (tempModel == null)
            tempModel = getModel();

        byte[] data = new byte[87];
    
        String name = model.get("name", "Untitled") + "          ";
        
        // The K1 is riddled with byte-mangling.  :-(
        
        for(int i = 0; i < 87; i++)
            {
            String key = allParameters[i];
                                
            if (i < 10)  // name
                {
                data[i] = (byte)name.charAt(i);
                }
            else if (key.equals("s1coarsefix"))
                {
                data[i] = (byte)(model.get("s1keytrack", 0) == 0 ? model.get("s1fix") : model.get("s1coarse"));
                }
            else if (key.equals("s2coarsefix"))
                {
                data[i] = (byte)(model.get("s2keytrack", 0) == 0 ? model.get("s2fix") : model.get("s2coarse"));
                }
            else if (key.equals("s3coarsefix"))
                {
                data[i] = (byte)(model.get("s3keytrack", 0) == 0 ? model.get("s3fix") : model.get("s3coarse"));
                }
            else if (key.equals("s4coarsefix"))
                {
                data[i] = (byte)(model.get("s4keytrack", 0) == 0 ? model.get("s4fix") : model.get("s4coarse"));
                }
            else if (key.equals("polymode_sources_s1ams1>s2_s3ams3>s4"))
                {
                data[i] = (byte)(model.get("polymode") | (model.get("sources") << 2) | (model.get("s1ams1>s2") << 3) | (model.get("s3ams3>s4") << 5));
                }
            else if (key.equals("s1mute_s2mute_s3mute_s4mute"))
                {
                // In the Kawai *K4* manual, there is an error: 0 is mute OFF and 1 is MUTE ON.
                // I don't know if this holds for the K1.  I presume so below.  We'll find out soon....
                // Perhaps our strategy should be to eliminate mute entirely and just turn the level down.
                // That way when we load a patch that's got a mute in it, we're not confused.
                data[i] = (byte)0;  // all four of them are o ("mute OFF" -- assuming the manual is wrong)
                }
            else if (key.equals("s1envelopelevel"))
                {
                // Perhaps our strategy should be to eliminate mute entirely and just turn the level down.
                // That way when we load a patch that's got a mute in it, we're not confused.
                data[i] = (byte)((model.get("s1envelopelevel")));
                if (model.get("s1mute") == 1)  // mute is on
                    data[i] = 0;  // turn level off
                }
            else if (key.equals("s2envelopelevel"))
                {
                // Perhaps our strategy should be to eliminate mute entirely and just turn the level down.
                // That way when we load a patch that's got a mute in it, we're not confused.
                data[i] = (byte)((model.get("s2envelopelevel")));
                if (model.get("s2mute") == 1)  // mute is on
                    data[i] = 0;  // turn level off
                }
            else if (key.equals("s3envelopelevel"))
                {
                // Perhaps our strategy should be to eliminate mute entirely and just turn the level down.
                // That way when we load a patch that's got a mute in it, we're not confused.
                data[i] = (byte)((model.get("s3envelopelevel")));
                if (model.get("s3mute") == 1)  // mute is on
                    data[i] = 0;  // turn level off
                }
            else if (key.equals("s4envelopelevel"))
                {
                // Perhaps our strategy should be to eliminate mute entirely and just turn the level down.
                // That way when we load a patch that's got a mute in it, we're not confused.
                data[i] = (byte)((model.get("s4envelopelevel")));
                if (model.get("s4mute") == 1)  // mute is on
                    data[i] = 0;  // turn level off
                }
            else if (key.equals("lfo1shape_kscurve_wheelassign"))
                {
                data[i] = (byte)(model.get("lfo1shape") | (model.get("kscurve") << 2) | (model.get("wheelassign") << 5));
                }
            else if (key.equals("s1waveselecthi_keytrack_vib/a.bendsw_prs>frqsw_velcurve"))
                {
                data[i] = (byte)((model.get("s1waveselect") >>> 7) |     // hi bit put in lo position
                    (model.get("s1keytrack") << 1) | (model.get("s1vib/a.bendsw") << 2) | (model.get("s1prs>frqsw") << 3) | (model.get("s1velcurve") << 4) );
                }
            else if (key.equals("s2waveselecthi_keytrack_vib/a.bendsw_prs>frqsw_velcurve"))
                {
                data[i] = (byte)((model.get("s2waveselect") >>> 7) |     // hi bit put in lo position
                    (model.get("s2keytrack") << 1) | (model.get("s2vib/a.bendsw") << 2) | (model.get("s2prs>frqsw") << 3) | (model.get("s2velcurve") << 4) );
                }
            else if (key.equals("s3waveselecthi_keytrack_vib/a.bendsw_prs>frqsw_velcurve"))
                {
                data[i] = (byte)((model.get("s3waveselect") >>> 7) |     // hi bit put in lo position
                    (model.get("s3keytrack") << 1) | (model.get("s3vib/a.bendsw") << 2) | (model.get("s3prs>frqsw") << 3) | (model.get("s3velcurve") << 4) );
                }
            else if (key.equals("s4waveselecthi_keytrack_vib/a.bendsw_prs>frqsw_velcurve"))
                {
                data[i] = (byte)((model.get("s4waveselect") >>> 7) |     // hi bit put in lo position
                    (model.get("s4keytrack") << 1) | (model.get("s4vib/a.bendsw") << 2) | (model.get("s4prs>frqsw") << 3) | (model.get("s4velcurve") << 4) );
                }
            else if (key.equals("s1waveselectlo"))
                {
                data[i] = (byte)(model.get("s1waveselect") & 127);
                }
            else if (key.equals("s2waveselectlo"))
                {
                data[i] = (byte)(model.get("s2waveselect") & 127);
                }
            else if (key.equals("s3waveselectlo"))
                {
                data[i] = (byte)(model.get("s3waveselect") & 127);
                }
            else if (key.equals("s4waveselectlo"))
                {
                data[i] = (byte)(model.get("s4waveselect") & 127);
                }
            else
                {
                data[i] = (byte)(model.get(key));
                }
            }

        boolean external;
        byte position;
                
        external = (tempModel.get("bank") > 7);
        position = (byte)((tempModel.get("bank") & 7) * 8 + (tempModel.get("number")));  // 0...63 for IA1...iD8
                        
        byte[] result = new byte[EXPECTED_SYSEX_LENGTH];
        result[0] = (byte)0xF0;
        result[1] = (byte)0x40;
        result[2] = (byte)getChannelOut();
        result[3] = (byte)0x20;
        result[4] = (byte)0x00;
        result[5] = (byte)0x03;
        result[6] = (byte)(external ? 0x01 : 0x00);
        
        if (toWorkingMemory && sendKawaiParametersInBulk)
            result[7] = (byte)63;
        else
            result[7] = (byte)position;
        
        System.arraycopy(data, 0, result, 8, data.length);
        result[8 + data.length] = (byte)produceChecksum(data);
        result[9 + data.length] = (byte)0xF7;
        return result;
        }


    public byte[] requestDump(Model tempModel)
        {
        boolean external = (tempModel.get("bank") > 7);
        byte position = (byte)((tempModel.get("bank") & 7) * 8 + (tempModel.get("number")));  // 0...63 for IA1...iD8
        return new byte[] { (byte)0xF0, 0x40, (byte)getChannelOut(), 0x00, 0x00, 0x03, 
            (byte)(external ? 0x01 : 0x00),
            position, (byte)0xF7};
        }
    
    public static boolean recognize(byte[] data)
        {
        return ((data.length == EXPECTED_SYSEX_LENGTH) &&
            (data[0] == (byte)0xF0) &&
            (data[1] == (byte)0x40) &&
            (data[3] == (byte)0x20) &&
            (data[4] == (byte)0x00) &&
            (data[5] == (byte)0x03) &&  // K1
            (data[6] == (byte)0x00 || data[6] == (byte)0x01) &&
            (data[7] < (byte)64)  // that is, it's single, not multi

            || recognizeBulk(data));
        }
        
    public static boolean recognizeBulk(byte[] data)
        {
        return (
            // Block Multi Data Dump (5-9)
            
            data.length == 2825 &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x40 &&
            // don't care about 2, it's the channel
            data[3] == (byte)0x21 &&    // block
            data[4] == (byte)0x00 &&
            data[5] == (byte)0x03);
        // don't care about 6, we'll use it later
        // don't care about 7, we'll use it later
        } 


    public static final int EXPECTED_SYSEX_LENGTH = 97;        
    
    
    public static final int MAXIMUM_NAME_LENGTH = 10;
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

        String nm = model.get("name", "Init");
        String newnm = revisePatchName(nm);
        if (!nm.equals(newnm))
            model.set("name", newnm);
        }
        
    public static String getSynthName() { return "Kawai K1/K1r/K1m"; }
    
    public String getPatchName(Model model) { return model.get("name", "Untitled  "); }

    public int getPauseAfterChangePatch() { return 150; }   // Seem to need about > 100ms
    public double getPauseBetweenMIDISends() { return 50;  }  // :-(  :-(
 
    public void changePatch(Model tempModel)
        {
        byte BB = (byte)tempModel.get("bank");
        byte NN = (byte)tempModel.get("number");
        
        /// The K1 cannot change patches to and from internal or external I believe.  :-(
        /// So I'm not sure what to do here.        
        if (BB >= 8) BB -= 8;
        int PC = (BB * 8 + NN);
        try 
            {
            tryToSendMIDI(new ShortMessage(ShortMessage.PROGRAM_CHANGE, getChannelOut(), PC, 0));
            }
        catch (Exception e) { e.printStackTrace(); }
        }
    
    public Model getNextPatchLocation(Model model)
        {
        int bank = model.get("bank");
        int number = model.get("number");
        
        number++;
        if (number >= 8)
            {
            bank++;
            number = 0;
            if (bank >= 16)
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
        
        return BANKS[model.get("bank")] + (model.get("number") + 1 < 10 ? "0" : "") + ((model.get("number") + 1));
        }
    }
