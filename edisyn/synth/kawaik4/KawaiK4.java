/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.kawaik4;

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
   A patch editor for the Kawai K4/K4r.
        
   @author Sean Luke
*/

public class KawaiK4 extends Synth
    {
    /// Various collections of parameter names for pop-up menus
        
    public static final String[] BANKS = { "A", "B", "C", "D", "Ext. A", "Ext. B", "Ext. C", "Ext. D" };
    public static final String[] WAVES = { "Sin 1st", "Sin 2nd", "Sin 3rd", "Sin 4th", "Sin 5th", "Sin 6th", "Sin 7th", "Sin 8th", "Sin 9th", 
                                           "Saw 1", "Saw 2", "Saw 3", "Saw 4", "Saw 5", "Saw 6", "Saw 7", "Saw 8", 
                                           "Pulse", "Triangle", "Square", "Rectangular 1", "Rectangular 2", "Rectangular 3", "Rectangular 4", "Rectangular 5", "Rectangular 6", 
                                           "Pure Horn L", "Punch Brass 1", "Oboe 1", "Oboe 2", "Classic Grand", 
                                           "Electric Piano 1", "Electric Piano 2", "Electric Piano 3", "Electric Organ 1", "Electric Organ 2", 
                                           "Positif", "Electric Organ 3", "Electric Organ 4", "Electric Organ 5", "Electric Organ 6", "Electric Organ 7", 
                                           "Electric Organ 8", "Electric Organ 9", "Classic Guitar", "Steel Strings", "Harp", "Wood Bass", "Synth Bass 3", 
                                           "Digibass", "Finger Bass", "Marimba", "Synth Voice", "Glass Harp 1", "Cello", "Xylophone", "Electric Piano 4", 
                                           "Synclavier M", "Electric Piano 5", "Electric Organ 10", "Electric Organ 11", "Electric Organ 12", "Big Pipe", 
                                           "Glass Harp 2", "Random", "Electric Piano 6", "Synth Bass 4", "Synth Bass 1", "Synth Bass 2", "Quena", "Oboe 3", 
                                           "Pure Horn H", "Fat Brass", "Punch Brass 2", "Electric Piano 7", "Electric Piano 8", "Synclavier 2", 
                                           "Harpsichord M", "Harpsichord L", "Harpsichord H", "Electric Organ 13", "Koto", "Sitar L", "Sitar H", 
                                           "Pick Bass", "Synth Bass 5", "Synth Bass 6", "Vibraphone Attack", "Vibraphone 1", "Horn Vibe", 
                                           "Steel Drum 1", "Steel Drum 2", "Vibraphone 2", "Marimba Attack", "Harmonica", "Synth", "Kick", 
                                           "Gated Kick", "Snare Tite", "Snare Deep", "Snare Hi", "Rim Snare", "Rim Shot", "Tom", "Tom VR", 
                                           "Electric Tom", "High Hat Closed", "High Hat Open", "High Hatopen VR", "High Hat Foot", "Crash", "Crash VR", "Crash VR 2",
                                           "Ride Edge", "Ride Edge VR", "Ride Cup", "Ride Cup VR", "Claps", "Cowbell", "Conga", "Conga Slap", 
                                           "Tambourine", "Tambourine VR", "Claves", "Timbale", "Shaker", "Shaker VR", "Timpani", "Timpani VR", 
                                           "Sleighbell", "Bell", "Metal Hit", "Click", "Pole", "Glocken", "Marimba", "Piano Attack", "Water Drop", 
                                           "Char", "Piano Normal", "Piano VR", "Cello Normal", "Cello VR 1", "Cello VR 2", "Cello 1-Shot", 
                                           "Strings Normal", "Strings VR", "Slap Bass L Normal", "Slap Bass L VR", "Slap Bass L 1-Shot", 
                                           "Slap Bass H Normal", "Slap Bass H VR", "Slap Bass H 1-Shot", "Pick Bass Normal", "Pick Bass VR", 
                                           "Pick Bass 1-Shot", "Wood Bass Attack", "Wood Bass Normal", "Wood Bass VR", "Fretless Normal", 
                                           "Fretless VR", "Synth Bass Normal", "Synth Bass VR", "Electric Guitar Mute Normal", 
                                           "Electric Guitar Mute VR", "Electric Guitar Mute 1-Shot", "Dist Mute Normal", "Dist Mute VR", 
                                           "Dist Mute 1-Shot", "Dist Lead Normal", "Dist Lead VR", "Electric Guitar Normal", "Gut Guitar Normal", 
                                           "Gut Guitar VR", "Gut Guitar 1-Shot", "Flute Normal", "Flute 1-Shot", "Bottle Blow Normal", 
                                           "Bottle Blow VR", "Sax Normal", "Sax VR 1", "Sax VR 2", "Sax 1-Shot", "Trumpet Normal", 
                                           "Trumpet VR 1", "Trumpet VR 2", "Trumpet 1-Shot", "Trombone Normal", "Trombone VR", 
                                           "Trombone 1-Shot", "Voice", "Noise", "Piano 1", "Piano 2", "Piano 3", "Piano 4", "Piano 5", 
                                           "Cello 1", "Cello 2", "Cello 3", "Cello 4", "Cello 5", "Cello 6", "Strings 1", "Strings 2", 
                                           "Slap Bass L", "Slap Bass L 1-Shot", "Slap Bass H", "Slap Bass H 1-Shot", "Pick Bass 1", 
                                           "Pick Bass 2 1-Shot", "Pick Bass 3 1-Shot", "Electric Guitar Mute", "Electric Guitar Mute 1-Shot", 
                                           "Dist Lead 1", "Dist Lead 2", "Dist Lead 3", "Gut Guitar 1", "Gut Guitar 2", "Gut Guitar 3 1-Shot", 
                                           "Gut Guitar 4 1-Shot", "Flute 1", "Flute 2", "Sax 1", "Sax 2", "Sax 3", "Sax 4 1-Shot", "Sax 5 1-Shot", 
                                           "Sax 6 1-Shot", "Trumpet", "Trumpet 1-Shot", "Voice 1", "Voice 2", "Reverse 1", "Reverse 2", 
                                           "Reverse 3", "Reverse 4", "Reverse 5", "Reverse 6", "Reverse 7", "Reverse 8", "Reverse 9", 
                                           "Reverse 10", "Reverse 11", "Loop 1", "Loop 2", "Loop 3", "Loop 4", "Loop 5", "Loop 6", 
                                           "Loop 7", "Loop 8", "Loop 9", "Loop 10", "Loop 11", "Loop 12"};
    public static final String[] KS_CURVES = { "Linear", "Exponential", "Logarithmic", "Ramped", "Split", "Triangle", "Late", "Early" };
    public static final String[] VELOCITY_CURVES = { "Linear", "Logarithmic", "Exponential", "Exponential Strong", "Linear Then Off", "Off Then Linear", "Slow Middle", "Fast Middle" };
    public static final String[] KEYS = new String[] { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
    public static final String[] LFO_SHAPES = new String[] { "Triangle", "Sawtooth", "Square", "Random" };
    public static final String[] SOURCE_MODES = new String[] { "Normal", "Twin", "Double" };
    public static final String[] POLY_MODES = new String[] { "Poly 1", "Poly 2", "Solo 1", "Solo 2" };
    public static final String[] OUT_SELECTS = new String[] { "A", "B", "C", "D", "E", "F", "G", "H" };
    public static final String[] WHEEL_ASSIGNMENTS = new String[] { "Vibrato LFO", "Filter LFO", "Filter" };

    public KawaiK4()
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
        
        vbox.add(addLFO(Style.COLOR_B()));
        
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
        

        JComponent filterPanel = new SynthPanel(this);
        vbox = new VBox();
        
        vbox.add(addFilter(1, Style.COLOR_A()));
        vbox.add(addFilterEnvelope(1, Style.COLOR_B()));

        vbox.add(addFilter(2, Style.COLOR_A()));
        vbox.add(addFilterEnvelope(2, Style.COLOR_B()));

        filterPanel.add(vbox, BorderLayout.CENTER);
        addTab("Filters", filterPanel);

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
        return frame;
        }         

    public String getDefaultResourceFileName() { return "KawaiK4.init"; }
    public String getHTMLResourceFileName() { return "KawaiK4.html"; }

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
                showSimpleError(title, "The Patch Number must be an integer 1...16");
                continue;
                }
            if (n < 1 || n > 16)
                {
                showSimpleError(title, "The Patch Number must be an integer 1...16");
                continue;
                }
                
            n--;
                                
            int i = bank.getSelectedIndex();
                        
            change.set("bank", i);
            change.set("number", n);
                        
            return true;
            }
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
        params = SOURCE_MODES;
        comp = new Chooser("Source Mode", this, "sourcemode", params);
        vbox.add(comp);

        params = POLY_MODES;
        comp = new Chooser("Poly Mode", this, "polymode", params);
        vbox.addBottom(comp);
        hbox2.add(vbox);
        
        VBox vbox2 = new VBox();
        comp = new PushButton("Show Effect/Ouput")
            {
            public void perform()
                {
                final KawaiK4Effect synth = new KawaiK4Effect();
                if (tuple != null)
                    synth.tuple = tuple.copy(synth.buildInReceiver(), synth.buildKeyReceiver());
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
                                                
                    synth.sprout();
                    JFrame frame = ((JFrame)(SwingUtilities.getRoot(synth)));
                    frame.setVisible(true);                                 

                    SwingUtilities.invokeLater(
                        new Runnable()
                            {
                            public void run() 
                                { 
                                Model tempModel = new Model();
                                tempModel.set("number", KawaiK4.this.model.get("effect"));
                                tempModel.set("bank", KawaiK4.this.model.get("bank"));
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
        vbox2.add(comp);

        params = WHEEL_ASSIGNMENTS;
        comp = new Chooser("Mod Wheel", this, "wheelassign", params);
        vbox2.add(comp);  
        
        vbox = new VBox();
        vbox.addBottom(vbox2);
        hbox2.add(vbox);
        
        vbox = new VBox();
        vbox.addBottom(hbox2);
        hbox.add(vbox);

        comp = new LabelledDial("Effect [K4] /", this, "effect", color, 0, 31, -1);
        ((LabelledDial)comp).addAdditionalLabel("Output [K4r]");
        model.removeMetricMinMax("effect");
        hbox.add(comp);

        comp = new LabelledDial("Out Select", this, "outselect", color, 0, 7)
            {
            public String map(int val)
                {
                return OUT_SELECTS[val];
                }
            };
        model.removeMetricMinMax("outselect");
        hbox.add(comp);

        comp = new LabelledDial("Pitch Bend", this, "pitchbend", color, 0, 12);
        ((LabelledDial)comp).addAdditionalLabel("Range");
        hbox.add(comp);

        comp = new LabelledDial("Mod Wheel", this, "wheeldep", color, 0, 100, 50);
        ((LabelledDial)comp).addAdditionalLabel("Depth");
        hbox.add(comp);

        // this appears to be poorly named in the manual (page 36)
        comp = new LabelledDial("Pressure", this, "pres>freq", color, 0, 100, 50);
        ((LabelledDial)comp).addAdditionalLabel("Pitch Mod");
        hbox.add(comp);

        comp = new LabelledDial("Volume", this, "volume", color, 0, 100);
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }



    public JComponent addVibrato(Color color)
        {
        Category category = new Category(this, "Vibrato LFO", color);
        category.makePasteable("lfo2");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = LFO_SHAPES;  // also vibrato
        comp = new Chooser("Shape", this, "lfo2shape", params);
        vbox.add(comp);
        hbox.add(vbox);
                
        comp = new LabelledDial("Speed", this, "lfo2speed", color, 0, 100);
        hbox.add(comp);
        
        comp = new LabelledDial("Depth", this, "lfo2dep", color, 0, 100, 50);
        hbox.add(comp);

        comp = new LabelledDial("Pressure", this, "lfo2prs>dep", color, 0, 100, 50);
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

    public JComponent addLFO(Color color)
        {
        Category category = new Category(this, "LFO", color);
        category.makePasteable("lfo1");

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
        
        comp = new LabelledDial("Delay", this, "lfo1delay", color, 0, 100);
        hbox.add(comp);
        
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
             
        HBox hbox2 = new HBox();   
        // Normally this is in global, but I think it makes more sense here
        comp = new CheckBox("Mute", this, "s" + src + "mute");
        hbox2.add(comp);
        
        if (src == 1)
            {
            comp = new CheckBox("AM S1 -> S2", this, "s1ams1>s2");
            ((CheckBox)comp).addToWidth(1);
            hbox2.add(comp);
            }
        else if (src == 3)
            {
            comp = new CheckBox("AM S3 -> S4", this, "s3ams3>s4");
            ((CheckBox)comp).addToWidth(1);
            hbox2.add(comp);
            }
        vbox.add(hbox2);        
        hbox.add(vbox);

        vbox = new VBox();
        params = KS_CURVES;
        comp = new Chooser("Key Scaling Curve", this, "s" + src + "kscurve", params);
        vbox.add(comp);

        params = VELOCITY_CURVES;
        comp = new Chooser("Velocity Curve", this, "s" + src + "velcurve", params);
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
                
        comp = new LabelledDial("Fixed Key", this, "s" + src + "fix", color, 0, 115)
            {
            public String map(int val)
                {
                return KEYS[val % 12] + (val / 12 + 1);  // note integer division
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Coarse", this, "s" + src + "coarse", color, 0, 48, 24)
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
               
        comp = new LabelledDial("Delay", this, "s" + envelope + "delay", color, 0, 100);
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
        
        comp = new LabelledDial("Velocity", this, "s" + envelope + "timemodonvel", color, 0, 100, 50);
        ((LabelledDial)comp).addAdditionalLabel("Time Mod");
        hbox.add(comp);
        
        comp = new LabelledDial("Release Vel", this, "s" + envelope + "timemodoffvel", color, 0, 100, 50);
        ((LabelledDial)comp).addAdditionalLabel("Time Mod");
        hbox.add(comp);

        comp = new LabelledDial("Key Scaling", this, "s" + envelope + "timemodks", color, 0, 100, 50);
        ((LabelledDial)comp).addAdditionalLabel("Time Mod");
        hbox.add(comp);

        // DADSR
        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "s" + envelope + "delay", "s" + envelope + "envelopeattack", "s" + envelope + "envelopedecay", null, "s" + envelope + "enveloperelease" },
            new String[] { null, null, null, "s" + envelope + "envelopesustain", "s" + envelope + "envelopesustain", null },
            new double[] { 0, 0.2/100.0, 0.2/100.0, 0.2 / 100.0,  0.2, 0.2/100.0},
            new double[] { 0, 0.0, 1.0, 1.0 / 100.0, 1.0/100.0, 0 });
        hbox.addLast(comp);
                
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
        

    public JComponent addFilter(int filter, Color color)
        {
        Category category = new Category(this, "Filter " + filter, color);
        category.makePasteable("f" + filter);
                
        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();

        comp = new LabelledDial("Cutoff", this,  "f" + filter + "cutoff", color, 0, 100);
        hbox.add(comp);
                
        comp = new LabelledDial("Resonance", this,  "f" + filter + "resonance", color, 0, 7);
        hbox.add(comp);

        comp = new LabelledDial("Velocity", this,  "f" + filter + "cutoffmodvel", color, 0, 100, 50);
        ((LabelledDial)comp).addAdditionalLabel("Cutoff Mod");
        hbox.add(comp);
                
        comp = new LabelledDial("Pressure", this,  "f" + filter + "cutoffmodprs", color, 0, 100, 50);
        ((LabelledDial)comp).addAdditionalLabel("Cutoff Mod");
        hbox.add(comp);
                
        comp = new LabelledDial("Key Scaling", this,  "f" + filter + "cutoffmodks", color, 0, 100, 50);
        ((LabelledDial)comp).addAdditionalLabel("Cutoff Mod");
        hbox.add(comp);
                  
        VBox vbox = new VBox();
        comp = new CheckBox("LFO Cutoff Mod", this, "f" + filter + "lfosw");
        vbox.add(comp);
        hbox.add(vbox);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addFilterEnvelope(int filterenv, Color color)
        {
        Category category = new Category(this, "Filter Envelope " + filterenv, color);
        category.makePasteable("f" + filterenv);
                
        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();

        comp = new LabelledDial("Envelope", this,  "f" + filterenv + "dcfenvdep", color, 0, 100, 50);
        ((LabelledDial)comp).addAdditionalLabel("Depth");
        hbox.add(comp);

        comp = new LabelledDial("Attack", this, "f" + filterenv + "dcfenvattack", color, 0, 100);
        hbox.add(comp);

        comp = new LabelledDial("Decay", this, "f" + filterenv + "dcfenvdecay", color, 0, 100);
        hbox.add(comp);

        comp = new LabelledDial("Sustain", this, "f" + filterenv + "dcfenvsustain", color, 0, 100);
        hbox.add(comp);

        comp = new LabelledDial("Release", this, "f" + filterenv + "dcfenvrelease", color, 0, 100);
        hbox.add(comp);
                        
        comp = new LabelledDial("Velocity", this,  "f" + filterenv + "dcfenvveldep", color, 0, 100, 50);
        ((LabelledDial)comp).addAdditionalLabel("Depth Mod");
        hbox.add(comp);
                
        comp = new LabelledDial("Velocity", this, "f" + filterenv + "dcftimemodonvel", color, 0, 100, 50);
        ((LabelledDial)comp).addAdditionalLabel("Time Mod");
        hbox.add(comp);
        
        comp = new LabelledDial("Release Vel", this, "f" + filterenv + "dcftimemodoffvel", color, 0, 100, 50);
        ((LabelledDial)comp).addAdditionalLabel("Time Mod");
        hbox.add(comp);

        comp = new LabelledDial("Key Scaling", this, "f" + filterenv + "dcftimemodks", color, 0, 100, 50);
        ((LabelledDial)comp).addAdditionalLabel("Time Mod");
        hbox.add(comp);
        
        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "f" + filterenv + "dcfenvattack", "f" + filterenv + "dcfenvdecay", null, "f" + filterenv + "dcfenvrelease" },
            new String[] { null, null, "f" + filterenv + "dcfenvsustain", "f" + filterenv + "dcfenvsustain", null },
            new double[] { 0, 0.25/100.0, 0.25 / 100.0,  0.25, 0.25/100.0},
            new double[] { 0, 1.0, 1.0 / 100.0, 1.0/100.0, 0 });
        hbox.addLast(comp);
                 
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    HashMap internalParametersToIndex = new HashMap();
        
    final static String[] internalParameters = new String[]
    {
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
    "volume",
    "effect",
    "outselect",
    "sourcemode",
    "polymode",
    "s1ams1>s2",
    "s3ams3>s4",
    "lfo2shape",
    "pitchbend",
    "wheelassign",
    "lfo2speed",
    "wheeldep",
    "autobendtime",
    "autobenddepth",
    "autobendks>time",
    "autobendvel>dep",
    "lfo2prs>dep",
    "lfo2dep",
    "lfo1shape",
    "lfo1speed",
    "lfo1delay",
    "lfo1dep",
    "lfo1prs>dep",
    "pres>freq",
    "s:delay",
    "s:kscurve",
    "s:waveselect",                 // *
    "s:coarse",
    "s:keytrack",
    "s:fix",
    "s:fine",
    "s:prs>frqsw",
    "s:vib/a.bendsw",
    "s:velcurve",
    "s:envelopelevel",
    "s:envelopeattack",
    "s:envelopedecay",
    "s:envelopesustain",
    "s:enveloperelease",
    "s:levelmodvel",
    "s:levelmodprs",
    "s:levelmodks",
    "s:timemodonvel",
    "s:timemodoffvel",
    "s:timemodks",
    "f:cutoff",
    "f:resonance",
    "f:lfosw",
    "f:cutoffmodvel",
    "f:cutoffmodprs",
    "f:cutoffmodks",
    "f:dcfenvdep",
    "f:dcfenvveldep",
    "f:dcfenvattack",
    "f:dcfenvdecay",
    "f:dcfenvsustain",
    "f:dcfenvrelease",
    "f:dcftimemodonvel",
    "f:dcftimemodoffvel",
    "f:dcftimemodks"
    };


    /** Map of parameter -> index in the allParameters array. */
    HashMap allParametersToIndex = new HashMap();

    /** List of all K4 parameters in order.  "-" is a reserved (unused and thus unnamed) parameter. */

    /// * indicates parameters which must be handled specially

    final static String[] allParameters = new String[/*100 or so*/] 
    {
    "-",                // this is the name
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "volume",                   
    "effect",
    "outselect",
    "sourcemode_polymode_s1ams1>s2_s3ams3>s4",              // *
    "s1mute_s2mute_s3mute_s4mute_lfo2shape",     // *
    "pitchbend_wheelassign",                                    // *
    "lfo2speed",
    "wheeldep",
    "autobendtime",
    "autobenddepth",           
    "autobendks>time",        
    "autobendvel>dep",
    "lfo2prs>dep",
    "lfo2dep",           
    "lfo1shape",
    "lfo1speed",           
    "lfo1delay",
    "lfo1dep",
    "lfo1prs>dep",
    "pres>freq",
    "s1delay",                
    "s2delay",                
    "s3delay",
    "s4delay",
    "s1waveselecthi_kscurve",           //*
    "s2waveselecthi_kscurve",       //*       
    "s3waveselecthi_kscurve",           //*
    "s4waveselecthi_kscurve",           //*
    "s1waveselectlo",                           // *
    "s2waveselectlo",                           // *
    "s3waveselectlo",                           // *
    "s4waveselectlo",                           // *
    "s1coarse_keytrack",                                //*
    "s2coarse_keytrack",                                //*
    "s3coarse_keytrack",                                //*
    "s4coarse_keytrack",                                //*
    "s1fix",
    "s2fix",
    "s3fix",
    "s4fix",
    "s1fine",
    "s2fine",
    "s3fine",
    "s4fine",
    "s1prs>frqsw_vib/a.bendsw_velcurve",                //*
    "s2prs>frqsw_vib/a.bendsw_velcurve",                //*
    "s3prs>frqsw_vib/a.bendsw_velcurve",                //*
    "s4prs>frqsw_vib/a.bendsw_velcurve",                //*
    "s1envelopelevel",
    "s2envelopelevel",
    "s3envelopelevel",
    "s4envelopelevel",
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
    "s1timemodonvel",
    "s2timemodonvel",
    "s3timemodonvel",
    "s4timemodonvel",
    "s1timemodoffvel",
    "s2timemodoffvel",
    "s3timemodoffvel",
    "s4timemodoffvel",
    "s1timemodks",
    "s2timemodks",
    "s3timemodks",
    "s4timemodks",
    "f1cutoff",
    "f2cutoff",
    "f1resonance_lfosw",                // *
    "f2resonance_lfosw",                // *
    "f1cutoffmodvel",
    "f2cutoffmodvel",
    "f1cutoffmodprs",
    "f2cutoffmodprs",
    "f1cutoffmodks",
    "f2cutoffmodks",
    "f1dcfenvdep",
    "f2dcfenvdep",
    "f1dcfenvveldep",
    "f2dcfenvveldep",
    "f1dcfenvattack",
    "f2dcfenvattack",
    "f1dcfenvdecay",
    "f2dcfenvdecay",
    "f1dcfenvsustain",
    "f2dcfenvsustain",
    "f1dcfenvrelease",
    "f2dcfenvrelease",
    "f1dcftimemodonvel",
    "f2dcftimemodonvel",
    "f1dcftimemodoffvel",
    "f2dcftimemodoffvel",
    "f1dcftimemodks",
    "f2dcftimemodks"
    };



    public Object[] emitAll(String key)
        {
        if (key.equals("bank")) return new Object[0];  // this is not emittable
        if (key.equals("number")) return new Object[0];  // this is not emittable

        if (key.equals("name"))
            {
            String name = model.get("key", "Untitled") + "          "; ;
            Object[] data = new Object[10];
            for(int i = 0; i < 10; i ++)
                {
                byte[] b = { (byte)0xF0, 0x40, (byte)getChannelOut(), 0x10, 0x00, 0x04, (byte)i, 0x0, (byte)(name.charAt(i)), (byte)0xF7 };
                data[i] = b;
                }
            return data;
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
            else if (key.startsWith("s1") && !key.equals("s1ams1>s2"))
                {
                source = 0;
                newkey = "s:" + key.substring(2);
                }
            else if (key.startsWith("s2"))
                {
                source = 1;
                newkey = "s:" + key.substring(2);
                }
            else if (key.startsWith("s3") && !key.equals("s3ams3>s4"))
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
            // handle waveselect specially
            /*
              else if (newkey.equals("s:waveselect"))
              {
              index = 36;      // this is waveselect's parameter
              }
            */
            else if (key.equals("s1mute") || key.equals("s2mute") || key.equals("s3mute") || key.equals("s4mute"))
                {
                // index already handled
                }
            else
                {
                index = ((Integer)(internalParametersToIndex.get(newkey))).intValue();
                }
            byte[] data = new byte[] { (byte)0xF0, 0x40, (byte)getChannelOut(), 0x10, 0x00, 0x04, (byte)index, (byte)((source << 1) | msb), (byte)lsb, (byte)0xF7 };
            return new Object[] { data };
            }
        }
    



    public void parseParameter(byte[] data)
        {
        if (data.length == 7 &&                 // write error report
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x40 &&
            data[3] >= (byte)0x41 &&
            data[3] <= (byte)0x43 &&
            data[4] == (byte)0x00 &&
            data[5] == (byte)0x04)
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
            model.set("bank", (data[7] / 16) + (data[6] == 0x00 ? 0 : 4));
            model.set("number", data[7] % 16);
            return subparse(data, 8);
            }
        else                            // block or All-patch, it'll work for both since singles are at the start
            {
            // extract names
            char[][] names = new char[64][10];
            for(int i = 0; i < 64; i++)
                {
                for (int j = 0; j < 10; j++)
                    {
                    names[i][j] = (char)(data[8 + (i * 131) + j] & 127);
                    }
                }
                        
            String[] n = new String[64];
            for(int i = 0; i < 64; i++)
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
                
            model.set("bank", (patchNum / 16) + (data[6] == 0x00 ? 0 : 4));
            model.set("number", patchNum % 16);

            // okay, we're loading and editing patch number patchNum.  Here we go.
            return subparse(data, patchNum * 131 + 8);                                                  
            }
        }


    public int subparse(byte[] data, int offset)
        {                        
        byte[] name = new byte[10];
                        
        // The K4 is riddled with byte-mangling.  :-(
        
        for(int i = 0; i < 130; i++)
            {
            String key = allParameters[i];
                                
            if (i < 10)  // name
                {
                name[i] = data[i + offset];
                }
            else if (key.equals("effect"))
                {
                // it looks like the K4 can send junk in the upper bits :-(
                model.set("effect", data[i + offset] & 31);
                }
            else if (key.equals("sourcemode_polymode_s1ams1>s2_s3ams3>s4"))
                {
                model.set("sourcemode", data[i + offset] & 3);
                model.set("polymode", (data[i + offset] >>> 2) & 3);
                model.set("s1ams1>s2", (data[i + offset] >>> 4) & 1);
                // it looks like the K4 can send junk in the upper bits :-(
                model.set("s3ams3>s4", (data[i + offset] >>> 5) & 1);
                }
            else if (key.equals("s1mute_s2mute_s3mute_s4mute_lfo2shape"))
                {
                // Error in Section 6.  0 is mute OFF and 1 is mute ON. 
                model.set("s1mute", (data[i + offset] & 1));
                model.set("s2mute", ((data[i + offset] >>> 1) & 1));
                model.set("s3mute", ((data[i + offset] >>> 2) & 1));
                model.set("s4mute", ((data[i + offset] >>> 3) & 1));
                // it looks like the K4 can send junk in the upper bits :-(
                model.set("lfo2shape", (data[i + offset] >>> 4) & 3);
                }
            else if (key.equals("pitchbend_wheelassign"))
                {
                model.set("pitchbend", data[i + offset] & 31);
                // it looks like the K4 can send junk in the upper bits :-(
                model.set("wheelassign", (data[i + offset] >>> 4) & 3);
                }
            else if (key.equals("s1waveselecthi_kscurve"))
                {
                model.set("s1waveselect", ((data[i + offset] & 1) << 7) | (data[i + offset + 4]));                // hi and lo
                model.set("s1kscurve", data[i + offset] >>> 4);
                }
            else if (key.equals("s2waveselecthi_kscurve"))
                {
                model.set("s2waveselect", ((data[i + offset] & 1) << 7) | (data[i + offset + 4]));                // hi and lo
                model.set("s2kscurve", data[i + offset] >>> 4);
                }
            else if (key.equals("s3waveselecthi_kscurve"))
                {
                model.set("s3waveselect", ((data[i + offset] & 1) << 7) | (data[i + offset + 4]));                // hi and lo
                model.set("s3kscurve", data[i + offset] >>> 4);
                }
            else if (key.equals("s4waveselecthi_kscurve"))
                {
                model.set("s4waveselect", ((data[i + offset] & 1) << 7) | (data[i + offset + 4]));                // hi and lo
                model.set("s4kscurve", data[i + offset] >>> 4);
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
            else if (key.equals("s1coarse_keytrack"))
                {
                model.set("s1coarse", data[i + offset] & 63);
                model.set("s1keytrack", data[i + offset] >>> 6);
                }
            else if (key.equals("s2coarse_keytrack"))
                {
                model.set("s2coarse", data[i + offset] & 63);
                model.set("s2keytrack", data[i + offset] >>> 6);
                }
            else if (key.equals("s3coarse_keytrack"))
                {
                model.set("s3coarse", data[i + offset] & 63);
                model.set("s3keytrack", data[i + offset] >>> 6);
                }
            else if (key.equals("s4coarse_keytrack"))
                {
                model.set("s4coarse", data[i + offset] & 63);
                model.set("s4keytrack", data[i + offset] >>> 6);
                }
            else if (key.equals("s1prs>frqsw_vib/a.bendsw_velcurve"))
                {
                model.set("s1prs>frqsw", data[i + offset] & 1);
                model.set("s1vib/a.bendsw", (data[i + offset] >>> 1) & 1);
                // it looks like the K4 can send junk in the upper bits :-(
                model.set("s1velcurve", (data[i + offset] >>> 2) & 7);
                }
            else if (key.equals("s2prs>frqsw_vib/a.bendsw_velcurve"))
                {
                model.set("s2prs>frqsw", data[i + offset] & 1);
                model.set("s2vib/a.bendsw", (data[i + offset] >>> 1) & 1);
                // it looks like the K4 can send junk in the upper bits :-(
                model.set("s2velcurve", (data[i + offset] >>> 2) & 7);
                }
            else if (key.equals("s3prs>frqsw_vib/a.bendsw_velcurve"))
                {
                model.set("s3prs>frqsw", data[i + offset] & 1);
                model.set("s3vib/a.bendsw", (data[i + offset] >>> 1) & 1);
                // it looks like the K4 can send junk in the upper bits :-(
                model.set("s3velcurve", (data[i + offset] >>> 2) & 7);
                }
            else if (key.equals("s4prs>frqsw_vib/a.bendsw_velcurve"))
                {
                model.set("s4prs>frqsw", data[i + offset] & 1);
                model.set("s4vib/a.bendsw", (data[i + offset] >>> 1) & 1);
                // it looks like the K4 can send junk in the upper bits :-(
                model.set("s4velcurve", (data[i + offset] >>> 2) & 7);
                }
            else if (key.equals("f1resonance_lfosw"))
                {
                model.set("f1resonance", data[i + offset] & 7);
                // it looks like the K4 can send junk in the upper bits :-(
                model.set("f1lfosw", (data[i + offset] >>> 3) & 1);
                }
            else if (key.equals("f2resonance_lfosw"))
                {
                model.set("f2resonance", data[i + offset] & 7);
                // it looks like the K4 can send junk in the upper bits :-(
                model.set("f2lfosw", (data[i + offset] >>> 3) & 1);
                }
            else
                {
                model.set(key, data[i + offset]);
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


    public byte[] emit(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        if (tempModel == null)
            tempModel = getModel();

        byte[] data = new byte[130];
    
        String name = model.get("name", "Untitled") + "          ";
        
        // The K4 is riddled with byte-mangling.  :-(
        
        for(int i = 0; i < 130; i++)
            {
            String key = allParameters[i];
                                
            if (i < 10)  // name
                {
                data[i] = (byte)name.charAt(i);
                }
            else if (key.equals("sourcemode_polymode_s1ams1>s2_s3ams3>s4"))
                {
                data[i] = (byte)(model.get("sourcemode") | (model.get("polymode") << 2) | (model.get("s1ams1>s2") << 4) | (model.get("s3ams3>s4") << 5));
                }
            else if (key.equals("s1mute_s2mute_s3mute_s4mute_lfo2shape"))
                {
                // Error in Section 6.  0 is mute OFF and 1 is mute ON. 
                // Perhaps our strategy should be to eliminate mute entirely and just turn the level down.
                // That way when we load a patch that's got a mute in it, we're not confused.
                data[i] = (byte)((model.get("lfo2shape") << 4));  // mutes are all zero (off)
                //data[i] = (byte)((model.get("s1mute")) | ((model.get("s2mute")) << 1) | ((model.get("s3mute")) << 2) | ((model.get("s4mute")) << 3) | (model.get("lfo2shape") << 4));
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
            else if (key.equals("pitchbend_wheelassign"))
                {
                data[i] = (byte)(model.get("pitchbend") | (model.get("wheelassign") << 4));
                }
            else if (key.equals("s1waveselecthi_kscurve"))
                {
                data[i] = (byte)((model.get("s1waveselect") >>> 7) |     // hi bit put in lo position
                    (model.get("s1kscurve") << 4));
                }
            else if (key.equals("s2waveselecthi_kscurve"))
                {
                data[i] = (byte)((model.get("s2waveselect") >>> 7) |     // hi bit put in lo position
                    (model.get("s2kscurve") << 4));
                }
            else if (key.equals("s3waveselecthi_kscurve"))
                {
                data[i] = (byte)((model.get("s3waveselect") >>> 7) |     // hi bit put in lo position
                    (model.get("s3kscurve") << 4));
                }
            else if (key.equals("s4waveselecthi_kscurve"))
                {
                data[i] = (byte)((model.get("s4waveselect") >>> 7) |     // hi bit put in lo position
                    (model.get("s4kscurve") << 4));
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
            else if (key.equals("s1coarse_keytrack"))
                {
                data[i] = (byte)(model.get("s1coarse") | (model.get("s1keytrack") << 6));
                }
            else if (key.equals("s2coarse_keytrack"))
                {
                data[i] = (byte)(model.get("s2coarse") | (model.get("s2keytrack") << 6));
                }
            else if (key.equals("s3coarse_keytrack"))
                {
                data[i] = (byte)(model.get("s3coarse") | (model.get("s3keytrack") << 6));
                }
            else if (key.equals("s4coarse_keytrack"))
                {
                data[i] = (byte)(model.get("s4coarse") | (model.get("s4keytrack") << 6));
                }
            else if (key.equals("s1prs>frqsw_vib/a.bendsw_velcurve"))
                {
                data[i] = (byte)(model.get("s1prs>frqsw") | (model.get("s1vib/a.bendsw") << 1) | (model.get("s1velcurve") << 2));
                }
            else if (key.equals("s2prs>frqsw_vib/a.bendsw_velcurve"))
                {
                data[i] = (byte)(model.get("s2prs>frqsw") | (model.get("s2vib/a.bendsw") << 1) | (model.get("s2velcurve") << 2));
                }
            else if (key.equals("s3prs>frqsw_vib/a.bendsw_velcurve"))
                {
                data[i] = (byte)(model.get("s3prs>frqsw") | (model.get("s3vib/a.bendsw") << 1) | (model.get("s3velcurve") << 2));
                }
            else if (key.equals("s4prs>frqsw_vib/a.bendsw_velcurve"))
                {
                data[i] = (byte)(model.get("s4prs>frqsw") | (model.get("s4vib/a.bendsw") << 1) | (model.get("s4velcurve") << 2));
                }
            else if (key.equals("f1resonance_lfosw"))
                {
                data[i] = (byte)(model.get("f1resonance") | (model.get("f1lfosw") << 3));
                }
            else if (key.equals("f2resonance_lfosw"))
                {
                data[i] = (byte)(model.get("f2resonance") | (model.get("f2lfosw") << 3));
                }
            else
                {
                data[i] = (byte)(model.get(key));
                }
            }

        // Error in Section 4-1, see "Corrected MIDI Implementation"

        boolean external;
        byte position;
                
        external = (tempModel.get("bank") > 3);
        position = (byte)((tempModel.get("bank") % 3) * 16 + (tempModel.get("number")));  // 0...63 for A1 .... D16
                        
        byte[] result = new byte[EXPECTED_SYSEX_LENGTH];
        result[0] = (byte)0xF0;
        result[1] = (byte)0x40;
        result[2] = (byte)getChannelOut();
        if (toWorkingMemory)
            result[3] = (byte)0x23;
        else
            result[3] = (byte)0x20;
        result[4] = (byte)0x00;
        result[5] = (byte)0x04;
                
        if (toWorkingMemory)
            result[6] = 0x00;               // Error in Section 5-12: missing parameter value (should be 0 for toWorkingMemory)
        else
            result[6] = (byte)(external ? 0x02 : 0x00);
        if (toWorkingMemory)
            result[7] = (byte)(0x00);  // indicates single
        else
            result[7] = (byte)position;
        System.arraycopy(data, 0, result, 8, data.length);
        result[8 + data.length] = (byte)produceChecksum(data);
        result[9 + data.length] = (byte)0xF7;
        return result;
        }


    public byte[] requestDump(Model tempModel)
        {
        boolean external = (tempModel.get("bank") > 3);
        byte position = (byte)((tempModel.get("bank") & 3) * 16 + (tempModel.get("number")));  // 0...63 for A1 .... D16
        return new byte[] { (byte)0xF0, 0x40, (byte)getChannelOut(), 0x00, 0x00, 0x04, 
            (byte)(external ? 0x02 : 0x00),
            position, (byte)0xF7};
        }
    
    public static boolean recognize(byte[] data)
        {
        return (((data.length == EXPECTED_SYSEX_LENGTH) &&
                (data[0] == (byte)0xF0) &&
                (data[1] == (byte)0x40) &&
                (data[3] == (byte)0x20) &&
                (data[4] == (byte)0x00) &&
                (data[5] == (byte)0x04) &&
                (data[6] == (byte)0x00 || data[6] == (byte)0x02) &&
                (data[7] < (byte)64))  // that is, it's single, not multi
            
            || recognizeBulk(data));
        }
        
    public static boolean recognizeBulk(byte[] data)
        {
        return  ((
                // Block Single Data Dump (5-9)
            
                data.length == 8393 &&
                data[0] == (byte)0xF0 &&
                data[1] == (byte)0x40 &&
                // don't care about 2, it's the channel
                data[3] == (byte)0x21 &&    // block
                data[4] == (byte)0x00 &&
                data[5] == (byte)0x04 &&
                // don't care about 6, we'll use it later
                data[7] == (byte)0x00)
            
            ||
            
                (
                // All Patch Data Dump (5-11)
            
                data.length == 15123 &&
                data[0] == (byte)0xF0 &&
                data[1] == (byte)0x40 &&
                // don't care about 2, it's the channel
                data[3] == (byte)0x22 &&    // All Patch
                data[4] == (byte)0x00 &&
                data[5] == (byte)0x04 &&
                // don't care about 6, we'll use it later
                data[7] == (byte)0x00));
        } 

    public static final int EXPECTED_SYSEX_LENGTH = 140;        
    
    
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
        
    public static String getSynthName() { return "Kawai K4/K4r"; }
    
    public String getPatchName(Model model) { return model.get("name", "Untitled  "); }

    public int getPauseAfterChangePatch() { return 200; }   // Seem to need about > 100ms

    public int getPauseAfterSendAllParameters() { return 100; } 
 
    public void changePatch(Model tempModel)
        {
        byte BB = (byte)tempModel.get("bank");
        byte NN = (byte)tempModel.get("number");
        
        // first switch to internal or external
        byte[] data = new byte[8];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x40;
        data[2] = (byte)getChannelOut();
        data[3] = (byte)0x30;
        data[4] = (byte)0x00;
        data[5] = (byte)0x04;
        data[6] = (byte)(BB < 4 ? 0x00 : 0x02); // 0x00 is internal, 0x02 is external
        data[7] = (byte)0xF7;
        
        tryToSendSysex(data);
        
        // Next do a PC
        
        if (BB >= 4) BB -= 4;
        int PC = (BB * 16 + NN);
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
        if (number >= 16)
            {
            bank++;
            number = 0;
            if (bank >= 8)
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
