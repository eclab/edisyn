/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.rolandu220;

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
   A patch editor for the Roland U-220 Multimode patches.
        
   @author Sean Luke
*/

public class RolandU220Multi extends Synth
    {
    // I'm not using these anywhere, they're just here for posterity
    
    /*
      public String[] MULTI_PRESETS_220 = {
      "Acoust Piano", "Chorus Piano", "E.Piano", "Bright EP", "Vibraphone", "Marimba", "Bell", "Fanta Bell", "A.Guitar", "E.Guitar", 
      "Heavy Guitar", "E.Organ 1", "E.Organ 3", "E.Organ 7", "E.Organ 9", "Mad Organ", "Strings", "Syn.Strings", "JP8.Strings", 
      "Choir", "Syn.Vox 1", "Syn.Vox 2", "Syn.Choir 1", "Syn.Choir 2", "FlangingSlap", "FretlessBass", "Synth Bass 7", 
      "SynB-BellPad", "A.Bass-Piano", "SingingPiano", "Splits", "Velo Trumpet", "Soft Trumpet", "Tromborn", "BrassSection", 
      "Saxophone", "JP8.Brass", "Power Brass", "Flute", "Shakuhachi", "Fantasia", "Calliope", "Soundtrack", "Atmosphere", 
      "Future Pad", "Pomona", "Melodigan", "Photogene", "Endymion", "Prelusion", "Jupiters", "Selene", "Sacred Tree", 
      "Macho Lead", "Lunar Lead", "HarmonicLead", "Native Dance", "Percs Hit", "Velo Combi", "Split Combi", "Rotor Craft", 
      "Emergency", "Deepsea", "Catastrophe" };
    */
        
    public String[] TIMBRE_PRESETS = {
        "A.Piano 2", "A.Piano 4", "A.Piano 10", "E.Piano 1", "E.Piano 5", 
        "Bright EP", "Vib 1", "Marimba", "Bell", "Fanta Bell", "A.Guitar 1", 
        "E.Guitar 1", "Heavy Guitar", "E.Organ", "E.Organ 3", "E.Organ 5", 
        "E.Organ 7", "E.Organ 9", "R. Organ", "Strings 1", "Strings 2", 
        "String Pad 2", "JP.Strings", "Choir 1", "Choir 3", "Syn.Vox 1", 
        "Syn.Vox 2", "Syn.Choir", "Syn.Choir 2", "Slap 1", "Slap 7", 
        "Fingered 1", "Picked 1", "Fretless 2", "Ac.Bass", "Syn.Bass 4", 
        "Syn.Bass 5", "Syn.Bass 6", "Syn.Bass 7", "Soft TP 1", "TP/TRB 1", 
        "Brass 1", "Sax 1", "Synth Brs 1", "Synth Brs 2", "PowerBrass 1", 
        "PowerBrass 2", "JP.Brass 2", "Flute 1", "Shaku 1", "Bell Pad", 
        "Breath Vox", "Pizzagogo", "Spect ell", "Bell Drum", "Synth Harp", 
        "Pulse Wave1", "Pulse Wave2", "Pulse Wave3", "Saw Wave 1", 
        "Saw Wave 2", "Metal", "SingingPiano", "Syn.Marimba", "Fantasia", 
        "Calliope 1", "Calliope 2", "Soundtrack 1", "Soundtrack 2", 
        "Soundtrack 3", "Atmosphere 1", "Atmosphere 2", "Future 1", 
        "Future 2", "Pomona 1", "Pomona 2", "Melodigan 1", "Melodigan 2", 
        "Photogetne 1", "Photogene 2", "Endymion 1", "Endymion 2", 
        "Prelusion 1", "Prelusion 2", "JP8.Brass", "JP8.Strings", 
        "Selene 1", "Selene 2", "Sacred 1", "Sacred 2", "Macho 1", 
        "Macho 2", "Lunar 1", "Lunar 2", "Harmonic 1", "Harmonic 2", 
        "Harmonic 3", "Native 1", "Native 2", "Native 3", "Native 4",
        "Native 5", "Percs Hit 1", "Percs Hit 2", "Percs Hit 3", "Rotor 1",
        "Rotor 2", "Rotor 3", "Emergency 1", "Emergency 2", "Emergency 3", 
        "Emergency 4", "Emergency 5", "Deep 1", "Deep 2", "Deep 3", 
        "Catastrophe1", "Catastrophe2", "Catastrophe3", "Catastrophe4", 
        "Catastrophe5", "Pizz", "Breath", "Nails", "Spectrum 1", 
        "Spectrum 2", "N.Dance", "Drums" };
    public String[] DRUM_PRESETS = { "Standard", "Dry", "Electric", "FX" };
    public String[] CHORUS_TYPES = { "Chorus 1", "Chorus 2", "FB-Chorus", "Flanger", "Short Delay" };
    public String[] CHORUS_OUT_MODES = { "Pre-Reverb", "Post-Reverb" };
    public String[] REVERB_TYPES = { "Room 1", "Room 2", "Room 3", "Hall 1", "Hall 2", "Gate", "Delay", "Cross-Delay" };
    public String[] PARAMETERS = { "Timbre Level", "Env Attack", "Env Decay", "Env Sustain", "Env Release", "Auto Bend Depth", "Auto Bend Rate", "Detune Depth", "Vib Rate", "Vib Waveform", "Vib Depth", "Vib Delay", "Vib Rise Time", "Vib Mod Depth", "Chorus Level", "Chorus Rate", "Chorus Feedback", "Reverb Level", "Delay Feedback" };
    public String[] OUTPUT_ASSIGNS = { "Dry", "Reverb", "Chorus", "Dir 1", "Dir 2 [220]" };
    //public String[] VELOCITY_LEVELS = { "Above", "Below" };               // Not using at present, we've hard-coded in the dial
    public static final String[] NOTES = new String[] { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
    public static final int MIDI_CHANNEL_OFF = 16;
        
    public RolandU220Multi()
        {
        /// SOUND PANEL
                

        for(int i = 0; i < allCommonParameters.length; i++)
            {
            allCommonParametersToIndex.put(allCommonParameters[i], Integer.valueOf(i));
            }

        for(int i = 0; i < allDrumParameters.length; i++)
            {
            allDrumParametersToIndex.put(allDrumParameters[i], Integer.valueOf(i));
            }

        for(int i = 0; i < allPartParameters.length; i++)
            {
            allPartParametersToIndex.put(allPartParameters[i], Integer.valueOf(i));
            }

        JComponent sourcePanel = new SynthPanel(this);
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        hbox.addLast(addRhythm(Style.COLOR_B()));
        vbox.add(hbox);
        hbox = new HBox();
        hbox.add(addChorus(Style.COLOR_A()));
        hbox.addLast(addReverb(Style.COLOR_C()));
        vbox.add(hbox);
        vbox.add(addParameters(Style.COLOR_B()));
        
        sourcePanel.add(vbox, BorderLayout.CENTER);
        addTab("Common", sourcePanel);                

        sourcePanel = new SynthPanel(this);
        vbox = new VBox();
        vbox.add(addPart(1, Style.COLOR_A()));
        vbox.add(addPart(2, Style.COLOR_B()));
        vbox.add(addPart(3, Style.COLOR_A()));
        vbox.add(addPart(4, Style.COLOR_B()));
        vbox.add(addPart(5, Style.COLOR_A()));
        vbox.add(addPart(6, Style.COLOR_B()));
        sourcePanel.add(vbox, BorderLayout.CENTER);
        addTab("Parts", sourcePanel);                

        model.set("name", "Init Patch");  // has to be 10 long
        model.set("number", 0);
                        
        loadDefaults();        
        }
                
                
    public JFrame sprout()
        {
        JFrame frame = super.sprout();
        addU220MultiMenu();
        return frame;
        }         


    int playPart = 0;
    public void addU220MultiMenu()
        {
        JMenu menu = new JMenu("U-220");
        menubar.add(menu);

        ButtonGroup g = new ButtonGroup();
        JRadioButtonMenuItem m = new JRadioButtonMenuItem("Play on Default Channel");
        m.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                playPart = 0;
                }
            });
        g.add(m);
        m.setSelected(true);
        menu.add(m);

        for(int i = 0; i < 8; i++)
            {
            final int _i = i;
            m = new JRadioButtonMenuItem("Play on Part " + (i + 1) + " Channel");
            m.addActionListener(new ActionListener()
                {
                public void actionPerformed(ActionEvent e)
                    {
                    playPart = (_i + 1);
                    }
                });
            g.add(m);
            menu.add(m);
            }
        }
        
    public int getTestNoteChannel()
        {
        if (playPart == 0)
            return super.getTestNoteChannel();
        else
            {
            int chan = model.get("p" + playPart + "midichannel", 16);
            if (chan == 16)
                return super.getTestNoteChannel();
            else return chan;
            }
        }
        
    public String getDefaultResourceFileName() { return "RolandU220Multi.init"; }
    public String getHTMLResourceFileName() { return "RolandU220Multi.html"; }

    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        int original = model.get("number");
                
        JTextField number = new SelectedTextField("" + (original + 1), 3);

        while(true)
            {
            boolean result = showMultiOption(this, new String[] { "Patch Number"}, 
                new JComponent[] { number }, title, "Enter Patch number");
                
            if (result == false)
                return false;
                                
            int n;
            try { n = Integer.parseInt(number.getText()); }
            catch (NumberFormatException e)
                {
                showSimpleError(title, "The Patch Number must be an integer 1...128");
                continue;
                }
            if (n < 1 || n > 128)
                {
                showSimpleError(title, "The Patch Number must be an integer 1...128");
                continue;
                }
                        
            change.set("number", n - 1);
            return true;
            }
        }

    /** Add the global patch category (name, id, number, etc.) */
    public JComponent addNameGlobal(Color color)
        {
        Category globalCategory = new Category(this, getSynthName(), color);
        //globalCategory.makeUnresettable();
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
                
        VBox vbox = new VBox();
        HBox hbox2 = new HBox();
        comp = new PatchDisplay(this, 9, false);
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
        vbox.addBottom(comp);
        hbox.add(vbox);

        hbox.add(Strut.makeHorizontalStrut(100));

        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }


    public JComponent addChorus(Color color)
        {
        Category category = new Category(this, "Chorus", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();

        params = CHORUS_TYPES;
        comp = new Chooser("Chorus Type", this, "chorustype", params);
        vbox.add(comp);

        params = CHORUS_OUT_MODES;
        comp = new Chooser("Chorus Out Mode", this, "chorusoutmode", params);
        vbox.add(comp);
        
        hbox.add(vbox);
                
        comp = new LabelledDial("Level", this, "choruslevel", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Delay", this, "chorusdelay", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Rate", this, "chorusrate", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Depth", this, "chorusdepth", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Feedback", this, "chorusfeedback", color, 1, 63)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val)
                {
                return "" + (val - 32);
                }
            };
        hbox.add(comp);
                
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addReverb(Color color)
        {
        Category category = new Category(this, "Reverb", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();

        params = REVERB_TYPES;
        comp = new Chooser("Reverb Type", this, "reverbtype", params);
        vbox.add(comp);
        hbox.add(vbox);
                
        comp = new LabelledDial("Level", this, "reverblevel", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Time", this, "reverbtime", color, 0, 31);
        hbox.add(comp);

        comp = new LabelledDial("Delay", this, "reverbdelayfeedback", color, 0, 31);
        ((LabelledDial)comp).addAdditionalLabel("Feedback");
        hbox.add(comp);
                
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addParameters(Color color)
        {
        Category category = new Category(this, "Parameters", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        for(int i = 1; i <= 3; i++)
            {
            VBox vbox = new VBox();

            params = PARAMETERS;
            comp = new Chooser("Parameter " + i, this, "param" + i, params);
            vbox.add(comp);
            hbox.add(vbox);
                
            comp = new LabelledDial("CC " + i, this, "cc" + i, color, 0, 63)
                {
                public String map(int val)
                    {
                    if (val < 6) return "" + val;           // 0...5 -> 0...5
                    else if (val < 31) return "" + (val + 1);       // 6 ... 30 ->  7...31
                    else if (val < 63) return "" + (val + 33);      // 31 ... 62 -> 64 ... 95
                    else return "Off";
                    }
                };
            hbox.add(comp);
            }
                
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addRhythm(Color color)
        {
        Category category = new Category(this, "Rhythm", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();

        VBox vbox = new VBox();
        final PushButton showButton = new PushButton("Show")
            {
            public void perform()
                {
                final RolandU220Drum synth = new RolandU220Drum();
                if (model.get("rhythmreceivechannel") == MIDI_CHANNEL_OFF) // off
                    {
                    showSimpleMessage("No MIDI Channel", "This drumset's MIDI Channel is OFF: it will not play properly.");
                    }
                else if (model.get("rhythmvoicereserve") == 0)
                    {
                    showSimpleMessage("No Voice Reserve", "This drumset has no voice reserve: it will not play properly.");
                    }
                
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
                        
                    synth.setTitleBarAux("[Drum Part of " + RolandU220Multi.this.model.get("name", "") + "]");
                    synth.sprout();
                    JFrame frame = ((JFrame)(SwingUtilities.getRoot(synth)));
                    frame.setVisible(true);

                    SwingUtilities.invokeLater(
                        new Runnable()
                            {
                            public void run() 
                                { 
                                Model tempModel = buildModel();
                                tempModel.set("number", RolandU220Multi.this.model.get("rhythmsetup"));
                                // We also want to change the synth's MIDI channel since it will differ from the "Control MIDI Channel" of the Multi
                                if (RolandU220Multi.this.model.get("rhythmreceivechannel") != 16)
                                    synth.tuple.outChannel = RolandU220Multi.this.model.get("rhythmreceivechannel") + 1;                // tuple channels start at channel 1
                                synth.performRequestDump(tempModel, true);
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
        hbox.add(vbox);
        vbox = new VBox();
        
        comp = new CheckBox("Level Boost", this, "rhythmlevelboostsw");
        vbox.add(comp);
        
        comp = new CheckBox("Receive Volume", this, "rhythmrxvolume");
        vbox.add(comp);
        
        comp = new CheckBox("Receive Hold", this, "rhythmrxhold");
        vbox.add(comp);
        hbox.add(vbox);

        final JLabel[] rhythmLabel = new JLabel[1];
        comp = new LabelledDial("Setup", this, "rhythmsetup", color, 0, 3)
            {
            public String map(int value)
                {
                return "" + (value + 1);
                }
                
            public void update(String key, Model model)
                {
                if (rhythmLabel[0] != null) 
                    rhythmLabel[0].setText(DRUM_PRESETS[model.get(key, 0)]);
                }
            };
        rhythmLabel[0] = ((LabelledDial)comp).addAdditionalLabel(DRUM_PRESETS[0]);
        hbox.add(comp);

        comp = new LabelledDial("Voice", this, "rhythmvoicereserve", color, 0, 30);
        ((LabelledDial)comp).addAdditionalLabel("Reserve");
        hbox.add(comp);

        comp = new LabelledDial("MIDI Channel", this, "rhythmreceivechannel", color, 0, 16)        
            {
            public String map(int value)
                {
                if (value == MIDI_CHANNEL_OFF) return "Off";
                else return "" + (value + 1);
                }
            };
        hbox.add(comp);
                
        comp = new LabelledDial("Level", this, "rhythmlevel", color, 0, 127);
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addPart(int part, Color color)
        {
        Category category = new Category(this, "Part " + part, color);
        category.makePasteable("part");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();

        final PushButton showButton = new PushButton("Show")
            {
            public void perform()
                {
                final RolandU220Timbre synth = new RolandU220Timbre();
                if (model.get("part" + part + "receivechannel") == MIDI_CHANNEL_OFF) // off
                    {
                    showSimpleMessage("No MIDI Channel", "This timbre's MIDI Channel is OFF: it will not play properly.");
                    }
                else if (model.get("part" + part + "voicereserve") == 0)
                    {
                    showSimpleMessage("No Voice Reserve", "This timbre has no voice reserve: it will not play properly.");
                    }
                
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
                        
                    synth.setTitleBarAux("[Drum Part of " + RolandU220Multi.this.model.get("name", "") + "]");
                    synth.sprout();
                    JFrame frame = ((JFrame)(SwingUtilities.getRoot(synth)));
                    frame.setVisible(true);

                    SwingUtilities.invokeLater(
                        new Runnable()
                            {
                            public void run() 
                                { 
                                Model tempModel = buildModel();
                                tempModel.set("number", RolandU220Multi.this.model.get("part" + part + "timbrenumber"));
                                // We also want to change the synth's MIDI channel since it will differ from the "Control MIDI Channel" of the Multi
                                if (RolandU220Multi.this.model.get("part" + part + "receivechannel") != 16)
                                    synth.tuple.outChannel = RolandU220Multi.this.model.get("part" + part + "receivechannel") + 1;                // tuple channels start at channel 1
                                synth.performRequestDump(tempModel, true);
                                synth.setPart(part);
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
        params = OUTPUT_ASSIGNS;
        comp = new Chooser("Output Assign", this, "part" + part + "outputassign", params);
        vbox.add(comp);
        hbox.add(vbox);
                
        /*
        // At present we're going to unify this with the U-20 velolevel dial (see below).
        
        params = VELOCITY_LEVELS;
        comp = new Chooser("Velocity Level", this, "part" + part + "velolevel", params);
        vbox.add(comp);
        */

        vbox = new VBox();
        comp = new CheckBox("Receive Volume", this, "part" + part + "rxvolume");
        vbox.add(comp);
        
        comp = new CheckBox("Receive Pan", this, "part" + part + "rxpan");
        vbox.add(comp);
        hbox.add(vbox);

        comp = new CheckBox("Receive Hold", this, "part" + part + "rxhold");
        vbox.add(comp);

        final JLabel[] timbreLabel = new JLabel[1];
        comp = new LabelledDial("      Timbre      ", this, "part" + part + "timbrenumber", color, 0, 127)
            {
            public String map(int val)
                {
                return "" + (val + 1);
                }
                        
            public void update(String key, Model model)
                {
                int p = model.get(key, 0);
                /// FIXME: How many preset slots are there really?  The sysex docs say 128
                if (timbreLabel[0] == null) { } // do nothing for now
                else if (p >= TIMBRE_PRESETS.length) timbreLabel[0].setText("");
                else timbreLabel[0].setText(TIMBRE_PRESETS[p]);
                }
            };
        timbreLabel[0] = ((LabelledDial)comp).addAdditionalLabel(TIMBRE_PRESETS[0]);
        hbox.add(comp);

        // Docs say 31, but it's 30
        comp = new LabelledDial("Voice", this, "part" + part + "voicereserve", color, 0, 30);
        ((LabelledDial)comp).addAdditionalLabel("Reserve");
        hbox.add(comp);

        comp = new LabelledDial("MIDI", this, "part" + part + "receivechannel", color, 0, 16)        
            {
            public String map(int value)
                {
                if (value == MIDI_CHANNEL_OFF) return "Off";
                else return "" + (value + 1);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Channel");
        hbox.add(comp);
                
        comp = new LabelledDial("Level", this, "part" + part + "level", color, 0, 127);
        hbox.add(comp);

        // FIXME: The specs make it seem like 0...6 are RIGHT and 8...14 are LEFT, which would be backwards
        comp = new LabelledDial("Pan", this, "part" + part + "pan", color, 0, 15)
            {
            public int getDefaultValue() { return 7; }
            public double getStartAngle() { return 217; }
            public String map(int value)
                {
                if (value < 6) return "< " + (7 - value);
                else if (value == 7) return "--";
                else if (value < 15) return "" + (value - 7) + " >";
                else return "Rnd";
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Key Range", this, "part" + part + "keyrangelow", color, 0, 127)
            {
            public String map(int value)
                {
                return NOTES[value % 12] + (value / 12 - 1);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Low");
        hbox.add(comp);

        comp = new LabelledDial("Key Range", this, "part" + part + "keyrangehi", color, 0, 127)
            {
            public String map(int value)
                {
                return NOTES[value % 12] + (value / 12 - 1);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("High");
        hbox.add(comp);

        comp = new LabelledDial(" Velocity Low ", this, "part" + part + "velolevel", color, 0, 127)
            {
            public String map(int val)
                {
                if (val == 0)
                    return "<html><center><font size=-2>--<br>[220] Above</font></center></html>";
                else if (val == 1) return "<html><center><font size=-2>1<br>[220] Below</font></center></html>";
                else return "<html><center><font size=-2>" + val + " <br>[220] --</font></center></html>";
                }
            };
        ((LabelledDial)comp).addAdditionalLabel(" [220] Level ");
        hbox.add(comp);

        comp = new LabelledDial("Velocity High", this, "part" + part + "velothreshold", color, 1, 127);
        ((LabelledDial)comp).addAdditionalLabel(" [220] Threshold ");
        hbox.add(comp);


        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    HashMap allCommonParametersToIndex = new HashMap();
        
    final static String[] allCommonParameters = new String[]
    {
    // Name is 12 bytes but they're broken into two nibbles each
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
    "chorustype",
    "chorusoutmode",
// the next two are incorrectly swapped in the documentation
    "chorusdelay",
    "choruslevel",
    "chorusrate",
    "chorusdepth",
    "chorusfeedback",
    "reverbtype",
    "reverbtime",
    "reverblevel",
    "reverbdelayfeedback",
    "cc1",
    "param1",
    "cc2",
    "param2",
    "cc3",
    "param3",
    };


    HashMap allDrumParametersToIndex = new HashMap();
        
    final static String[] allDrumParameters = new String[]
    {
    "rhythmsetup",
    "rhythmvoicereserve",
    "rhythmreceivechannel",
    "rhythmlevel",
    "rhythmlevelboostsw",
    "rhythmrxvolume",
    "rhythmrxhold",
    };


    HashMap allPartParametersToIndex = new HashMap();
        
    final static String[] allPartParameters = new String[]
    {
    "timbrenumber",
    "voicereserve",
    "receivechannel",
    "keyrangelow",
    "keyrangehi",
    "velolevel",
    "velothreshold",
// the next two are incorrectly swapped in the documentation
    "level",
    "outputassign",
    "pan",
    "rxvolume",
    "rxpan",
    "rxhold",
    };





    public byte getID() 
        { 
        try 
            { 
            byte b = (byte)(Byte.parseByte(tuple.id));
            if (b >= 17) return (byte)(b - 1);
            }
        catch (NullPointerException e) { } // expected.  Happens when tuple's not built yet
        catch (NumberFormatException e) { Synth.handleException(e); }
        return (byte)16;                // IDs start at 17
        }
        

    /** If the user is editing the patch on the synth, the U-220 won't change patches!
        So just in case we send this. */
    public boolean getSendsParametersAfterNonMergeParse() { return true; }

        
    public byte produceChecksum(byte[] data)
        {
        return produceChecksum(data, 0, data.length);
        }
                
    /** The checksum is computed on all the ADDRESS and DATA data.
        Just add up the data, mod 128, and subtract the result from 128. Return that, unless it is 128, in which case return 0. */
    public byte produceChecksum(byte[] data, int start, int end)
        {
        // The checksum works as follows:
        // 1. Add all the data
        // 2. mod by 128 (that is, & 127)
        // 3. Subtract from 128
        // 4. If the result is 128, return 0
        // 5. Else return the result
                
        int check = 0;
        for(int i = start; i < end; i++)
            {
            check += data[i];
            }
        check = check & 0x7F;
        check = 0x80 - check;
        if (check == 0x80) check = 0;
        return (byte) check;
        }


    public byte[] emit(String key)
        {
        if (key.equals("number")) return new byte[0];  // this is not emittable
        
        if (key.equals("name"))
            {
            byte[] data = new byte[10 + 24];
            data[0] = (byte) 0xF0;
            data[1] = (byte) 0x41;
            data[2] = (byte) getID();
            data[3] = (byte) 0x2B;
            data[4] = (byte) 0x12;
            data[5] = (byte) 0x10;
            data[6] = (byte) 0x04;
            data[7] = (byte) 0x00;
                
            char[] name = (model.get("name", "") + "            ").toCharArray();
            for(int i = 0; i < 12; i++)
                {
                data[i * 2 + 8]= (byte)(name[i] & 15);
                data[i * 2 + 8 + 1] = (byte)((name[i] >>> 4) & 15);
                }
            data[data.length - 2] = produceChecksum(data, 5, data.length - 2);
            data[data.length - 1] = (byte)0xF7;
            return data;
            }
        else if (key.startsWith("rhythm"))
            {
            byte AA = (byte) 0x10;
            byte BB = (byte) 0x04;
            byte CC = (byte) (0x60 + ((Integer)(allDrumParametersToIndex.get(key))).intValue());
            byte val = (byte)(model.get(key));
                
            byte checksum = produceChecksum(new byte[] { AA, BB, CC, val });
            return new byte[] { (byte)0xF0, 0x41, getID(), 0x2B, 0x12, AA, BB, CC, val, checksum, (byte)0xF7 };
            }
        else if (key.startsWith("part"))
            {
            int part = StringUtility.getFirstInt(key);
            String rest = key.substring(5);
            byte AA = (byte) 0x10;
            byte BB = (byte) 0x05;
            byte CC = (byte) (((Integer)(allPartParametersToIndex.get(rest))).intValue() + (part - 1) * 0x10);
            byte val = (byte)(model.get(key));
                
            byte checksum = produceChecksum(new byte[] { AA, BB, CC, val });
            return new byte[] { (byte)0xF0, 0x41, getID(), 0x2B, 0x12, AA, BB, CC, val, checksum, (byte)0xF7 };
            }
        else    // More common
            {
            byte AA = (byte) 0x10;
            byte BB = (byte) 0x04;
            byte CC = (byte)((Integer)(allCommonParametersToIndex.get(key))).intValue();
            byte val = (byte)(model.get(key));
                
            byte checksum = produceChecksum(new byte[] { AA, BB, CC, val });
            return new byte[] { (byte)0xF0, 0x41, getID(), 0x2B, 0x12, AA, BB, CC, val, checksum, (byte)0xF7 };
            }
        }


    byte[] parseData = null;

    public int parse(byte[] data, boolean fromFile)
        {
        if (numSysexMessages(data) > 1)
            {
            int result = PARSE_FAILED;
            byte[][] d = cutUpSysex(data);
            for(int i = 0; i < d.length; i++)
                {
                result = parse(d[i], fromFile);
                if (result != PARSE_INCOMPLETE) break;
                }
            return result;
            }
                
        // What is the tone patch number?
        int AA = data[5];
        int BB = data[6];
        int CC = data[7];
        
        int parseDataPosition = (AA == 0x00 ? ((BB - 0x06) * 128 + CC) % 160 : (BB * 128 + CC) % 160);

        if (parseDataPosition == 0)
            {
            parseData = new byte[160];
            for(int x = 0; x < parseData.length; x++)
                parseData[x] = 0;
            }
        else if (parseData == null) 
            return PARSE_FAILED;                            // bad initial data
        
        System.arraycopy(data, 8, parseData, parseDataPosition, Math.min(parseData.length - parseDataPosition, 128));
        
        if (parseDataPosition + 128 >= 160)     // last position
            {
            /*
              if (data.length == 138)         // uh oh, it's a dense bank patch, we can't handle those
              {
              return PARSE_FAILED;
              }
            */
                        
            if (AA == 0x03)         // Write to Patch banks
                {
                model.set("number", (BB * 128 + CC) / 160);
                }
                
            // The U-220 is entirely byte-packed :-(  So we have to do this by hand.

            int pos = 0;
            String name = "";
            for(int i = 0; i < 12; i++)
                {
                int lsb = parseData[pos++];
                int msb = parseData[pos++];
                name = name + (char)(lsb | (msb << 4));
                }
            model.set("name", name);

            int lsb1 = parseData[pos++];
            int msb1 = parseData[pos++];
            int lsb2 = parseData[pos++];
            int msb2 = parseData[pos++];
            int val = lsb1 | (msb1 << 4) | (lsb2 << 8) | (msb2 << 12);
            model.set("chorusdepth", (val >>> 0xB) & 31);           // B C D E F
            model.set("choruslevel", (val >>> 0x6) & 31);           // 6 7 8 9 A
            model.set("chorusrate", (val >>> 0x0) & 31);                    // 0 1 2 3 4

            lsb1 = parseData[pos++];
            msb1 = parseData[pos++];
            lsb2 = parseData[pos++];
            msb2 = parseData[pos++];
            val = lsb1 | (msb1 << 4) | (lsb2 << 8) | (msb2 << 12);
            model.set("chorustype", (val >>> 0xD) & 7);                     // D E F
            model.set("chorusfeedback", (val >>> 0x7) & 63);                // 7 8 9 A B C
            model.set("reverbtime", (val >>> 0x0) & 63);                    // 0 1 2 3 4 5

            lsb1 = parseData[pos++];
            msb1 = parseData[pos++];
            lsb2 = parseData[pos++];
            msb2 = parseData[pos++];
            val = lsb1 | (msb1 << 4) | (lsb2 << 8) | (msb2 << 12);
            model.set("reverbdelayfeedback", (val >>> 0xB) & 31);           // B C D E F
            model.set("chorusdelay", (val >>> 0x0) & 31);                           // 0 1 2 3 4

            lsb1 = parseData[pos++];
            msb1 = parseData[pos++];
            lsb2 = parseData[pos++];
            msb2 = parseData[pos++];
            val = lsb1 | (msb1 << 4) | (lsb2 << 8) | (msb2 << 12);
            model.set("chorusoutmode", (val >>> 0xF) & 1);          // F
            model.set("reverbtype", (val >>> 0xC) & 7);                     // C D E
            model.set("reverbtime", (val >>> 0x6) & 31);                    // 6 7 8 9 A
            model.set("reverblevel", (val >>> 0x0) & 31);           // 0 1 2 3 4

            lsb1 = parseData[pos++];
            msb1 = parseData[pos++];
            lsb2 = parseData[pos++];
            msb2 = parseData[pos++];
            val = lsb1 | (msb1 << 4) | (lsb2 << 8) | (msb2 << 12);
            model.set("param1", (val >>> 0x8) & 31);                // 8 9 A B C
            model.set("cc1", (val >>> 0x0) & 127);          // 0 1 2 3 4 5 6

            lsb1 = parseData[pos++];
            msb1 = parseData[pos++];
            lsb2 = parseData[pos++];
            msb2 = parseData[pos++];
            val = lsb1 | (msb1 << 4) | (lsb2 << 8) | (msb2 << 12);
            model.set("param2", (val >>> 0x8) & 31);                // 8 9 A B C
            model.set("cc2", (val >>> 0x0) & 127);          // 0 1 2 3 4 5 6

            lsb1 = parseData[pos++];
            msb1 = parseData[pos++];
            lsb2 = parseData[pos++];
            msb2 = parseData[pos++];
            val = lsb1 | (msb1 << 4) | (lsb2 << 8) | (msb2 << 12);
            model.set("param3", (val >>> 0x8) & 31);                // 8 9 A B C
            model.set("cc3", (val >>> 0x0) & 127);          // 0 1 2 3 4 5 6

            lsb1 = parseData[pos++];
            msb1 = parseData[pos++];
            lsb2 = parseData[pos++];
            msb2 = parseData[pos++];
            val = lsb1 | (msb1 << 4) | (lsb2 << 8) | (msb2 << 12);
            model.set("rhythmrxvolume", (val >>> 0xE) & 1);                         // E
            model.set("rhythmrxhold", (val >>> 0xD) & 1);                           // D
            model.set("rhythmreceivechannel", (val >>> 0x8) & 31);          // 8 9 A B C
            model.set("rhythmsetup", (val >>> 0x5) & 3);                                    // 5 6
            model.set("rhythmvoicereserve", (val >>> 0x0) & 31);                    // 0 1 2 3 4

            lsb1 = parseData[pos++];
            msb1 = parseData[pos++];
            lsb2 = parseData[pos++];
            msb2 = parseData[pos++];
            val = lsb1 | (msb1 << 4) | (lsb2 << 8) | (msb2 << 12);
            model.set("rhythmlevelboostsw", (val >>> 0x7) & 1);             // 7
            model.set("rhythmlevel", (val >>> 0x0) & 127);                  // 0 1 2 3 4 5 6

            for(int i = 1; i <= 6; i++)             // notice 1...6, not 0...5
                {
                lsb1 = parseData[pos++];
                msb1 = parseData[pos++];
                lsb2 = parseData[pos++];
                msb2 = parseData[pos++];
                val = lsb1 | (msb1 << 4) | (lsb2 << 8) | (msb2 << 12);
                model.set("part" + i + "outputassign", (val >>> 0xD) & 7);              // D E F
                model.set("part" + i + "voicereserve", (val >>> 0x8) & 31);             // 8 9 A B C
                model.set("part" + i + "rxvolume", (val >>> 0x7) & 1);                  // 7
                model.set("part" + i + "timbrenumber", (val >>> 0x0) & 127);            // 0 1 2 3 4 5 6

                lsb1 = parseData[pos++];
                msb1 = parseData[pos++];
                lsb2 = parseData[pos++];
                msb2 = parseData[pos++];
                val = lsb1 | (msb1 << 4) | (lsb2 << 8) | (msb2 << 12);
                model.set("part" + i + "pan", (val >>> 0xC) & 15);                              // C D E F
                model.set("part" + i + "level", (val >>> 0x5) & 127);                   // 5 6 7 8 9 A B
                model.set("part" + i + "receivechannel", (val >>> 0x0) & 31);   // 0 1 2 3 4

                lsb1 = parseData[pos++];
                msb1 = parseData[pos++];
                lsb2 = parseData[pos++];
                msb2 = parseData[pos++];
                val = lsb1 | (msb1 << 4) | (lsb2 << 8) | (msb2 << 12);
                model.set("part" + i + "rxpan", (val >>> 0xF) & 1);                             // F
                model.set("part" + i + "keyrangehi", (val >>> 0x8) & 127);              // 8 9 A B C D E
                model.set("part" + i + "rxhold", (val >>> 0x7) & 1);                            // 7
                model.set("part" + i + "keyrangelow", (val >>> 0x0) & 127);             // 0 1 2 3 4 5 6

                lsb1 = parseData[pos++];
                msb1 = parseData[pos++];
                lsb2 = parseData[pos++];
                msb2 = parseData[pos++];
                val = lsb1 | (msb1 << 4) | (lsb2 << 8) | (msb2 << 12);
                model.set("part" + i + "velothreshold", (val >>> 0x8) & 127);   // 8 9 A B C D E
                model.set("part" + i + "velolevel", (val >>> 0x0) & 127);               // 0 1 2 3 4 5 6 in U-20, 0 in U-220
                }
            revise();
            parseData = null;
            return PARSE_SUCCEEDED;
            }
        else return PARSE_INCOMPLETE;
        }
    
    
    public Object[] emitAll(Model tempModel, boolean toWorkingMemory, boolean toFile)
        { 
        if (tempModel == null)
            tempModel = getModel();

        int start = tempModel.get("number");
        int AA = (toWorkingMemory ? 0x00 : 0x03);
        int BB = (toWorkingMemory ? 0x06 : (start * 160) / 128 );
        int CC = (toWorkingMemory ? 0x00 : (start * 160) % 128 );
                
        // The U-220 is entirely byte-packed :-(  So we have to do this by hand.
                
        byte[] buf = new byte[160];
        int pos = 0;
                
        String name = model.get("name", "Untitled") + "            ";
        for(int i = 0; i < 12; i++)
            {
            char c = name.charAt(i);
            buf[pos++] = (byte)(c & 15);
            buf[pos++] = (byte)((c >>> 4) & 15);
            }

        int d = 
            (model.get("chorusdepth") << 0xB) |
            (model.get("choruslevel") << 0x6) |
            (model.get("chorusrate") << 0x0);
        buf[pos++] = (byte)(d & 15);
        buf[pos++] = (byte)((d >>> 4) & 15);
        buf[pos++] = (byte)((d >>> 8) & 15);
        buf[pos++] = (byte)((d >>> 12) & 15);
                
        d = 
            (model.get("chorustype") << 0xD) |
            (model.get("chorusfeedback") << 0x7) |
            (model.get("reverbtime") << 0x0);
        buf[pos++] = (byte)(d & 15);
        buf[pos++] = (byte)((d >>> 4) & 15);
        buf[pos++] = (byte)((d >>> 8) & 15);
        buf[pos++] = (byte)((d >>> 12) & 15);
                
        d = 
            (model.get("reverbdelayfeedback") << 0xB) |
            (model.get("chorusdelay") << 0x0);
        buf[pos++] = (byte)(d & 15);
        buf[pos++] = (byte)((d >>> 4) & 15);
        buf[pos++] = (byte)((d >>> 8) & 15);
        buf[pos++] = (byte)((d >>> 12) & 15);
                
        d = 
            (model.get("chorusoutmode") << 0xF) |
            (model.get("reverbtype") << 0xC) |
            (model.get("reverbtime") << 0x6) |
            (model.get("reverblevel") << 0x0);
        buf[pos++] = (byte)(d & 15);
        buf[pos++] = (byte)((d >>> 4) & 15);
        buf[pos++] = (byte)((d >>> 8) & 15);
        buf[pos++] = (byte)((d >>> 12) & 15);
                
        d = 
            (model.get("param1") << 0x8) |
            (model.get("cc1") << 0x0);
        buf[pos++] = (byte)(d & 15);
        buf[pos++] = (byte)((d >>> 4) & 15);
        buf[pos++] = (byte)((d >>> 8) & 15);
        buf[pos++] = (byte)((d >>> 12) & 15);
                
        d = 
            (model.get("param2") << 0x8) |
            (model.get("cc2") << 0x0);
        buf[pos++] = (byte)(d & 15);
        buf[pos++] = (byte)((d >>> 4) & 15);
        buf[pos++] = (byte)((d >>> 8) & 15);
        buf[pos++] = (byte)((d >>> 12) & 15);

        d = 
            (model.get("param3") << 0x8) |
            (model.get("cc3") << 0x0);
        buf[pos++] = (byte)(d & 15);
        buf[pos++] = (byte)((d >>> 4) & 15);
        buf[pos++] = (byte)((d >>> 8) & 15);
        buf[pos++] = (byte)((d >>> 12) & 15);

        d = 
            (model.get("rhythmrxvolume") << 0xE) |
            (model.get("rhythmrxhold") << 0xD) |
            (model.get("rhythmreceivechannel") << 0x8) |
            (model.get("rhythmsetup") << 0x5) |
            (model.get("rhythmvoicereserve") << 0x0);
        buf[pos++] = (byte)(d & 15);
        buf[pos++] = (byte)((d >>> 4) & 15);
        buf[pos++] = (byte)((d >>> 8) & 15);
        buf[pos++] = (byte)((d >>> 12) & 15);
                
        d = 
            (model.get("rhythmlevelboostsw") << 0x7) |
            (model.get("rhythmlevel") << 0x0);
        buf[pos++] = (byte)(d & 15);
        buf[pos++] = (byte)((d >>> 4) & 15);
        buf[pos++] = (byte)((d >>> 8) & 15);
        buf[pos++] = (byte)((d >>> 12) & 15);
                        
        for(int i = 1; i <= 6; i++)             // note 1...6 not 0...5
            {
            d = 
                (model.get("part" + i + "outputassign") << 0xD) |
                (model.get("part" + i + "voicereserve") << 0x8) |
                (model.get("part" + i + "rxvolume") << 0x7) |
                (model.get("part" + i + "timbrenumber") << 0x0);
            buf[pos++] = (byte)(d & 15);
            buf[pos++] = (byte)((d >>> 4) & 15);
            buf[pos++] = (byte)((d >>> 8) & 15);
            buf[pos++] = (byte)((d >>> 12) & 15);
                
            d = 
                (model.get("part" + i + "pan") << 0xC) |
                (model.get("part" + i + "level") << 0x5) |
                (model.get("part" + i + "receivechannel") << 0x0);
            buf[pos++] = (byte)(d & 15);
            buf[pos++] = (byte)((d >>> 4) & 15);
            buf[pos++] = (byte)((d >>> 8) & 15);
            buf[pos++] = (byte)((d >>> 12) & 15);
                
            d = 
                (model.get("part" + i + "rxpan") << 0xF) |
                (model.get("part" + i + "keyrangehi") << 0x8) |
                (model.get("part" + i + "rxhold") << 0x7) |
                (model.get("part" + i + "keyrangelow") << 0x0);
            buf[pos++] = (byte)(d & 15);
            buf[pos++] = (byte)((d >>> 4) & 15);
            buf[pos++] = (byte)((d >>> 8) & 15);
            buf[pos++] = (byte)((d >>> 12) & 15);
                
            d = 
                (model.get("part" + i + "velothreshold") << 0x8) |
                (model.get("part" + i + "velolevel") << 0x0);
            buf[pos++] = (byte)(d & 15);
            buf[pos++] = (byte)((d >>> 4) & 15);
            buf[pos++] = (byte)((d >>> 8) & 15);
            buf[pos++] = (byte)((d >>> 12) & 15);
            }
                        
        // There are two extra nybblized bytes to pad out to an even 80 data bytes
                
        Object[] result = new Object[13];
        for(int i = 0; i < 2; i++)
            {
            byte[] dat1 = new byte[Math.min(128, 160 - 128 * i) + 10];
            dat1[0] = (byte)0xF0;
            dat1[1] = (byte)0x41;
            dat1[2] = (byte)getID();
            dat1[3] = (byte)0x2B;
            dat1[4] = (byte)0x12;
            dat1[5] = (byte) AA;
            dat1[6] = (byte) (BB + i);
            dat1[7] = (byte) CC;
            System.arraycopy(buf, 128 * i, dat1, 8, dat1.length - 10);
            dat1[dat1.length - 2] = produceChecksum(dat1, 5, dat1.length - 2);
            dat1[dat1.length - 1] = (byte)0xF7;
            result[i] = dat1;
            }
        return result;
        }

    public byte[] requestCurrentDump()
        {
        byte AA = (byte)(0x00);
        byte BB = (byte)(0x06);
        byte CC = (byte)(0x00);
        byte LSB = (byte)0x20;
        byte MSB = (byte)0x1;
        
        byte checksum = produceChecksum(new byte[] { AA, BB, CC, (byte)0x00, LSB, MSB });
        byte[] b = new byte[] { (byte)0xF0, (byte)0x41, getID(), (byte)0x2B, (byte)0x11, 
            AA, BB, CC, (byte)0x00, MSB, LSB, checksum, (byte)0xF7 }; 
        return b;
        }

    // Requests a Patch from a specific RAM slot (1...64)
    public byte[] requestDump(Model tempModel)
        {
        if (tempModel == null)
            tempModel = getModel();

        int number = tempModel.get("number");
        // we're loading from Tone Temporary [synth]
        byte AA = (byte)(0x03);
        byte BB = (byte)((160 * number) / 128);
        byte CC = (byte)((160 * number) % 128);
        byte LSB = (byte)(160 % 128);
        byte MSB = (byte)(160 / 128);
        
        byte checksum = produceChecksum(new byte[] { AA, BB, CC, (byte)0x00, LSB, MSB });
        byte[] b = new byte[] { (byte)0xF0, (byte)0x41, getID(), (byte)0x2B, (byte)0x11, 
            AA, BB, CC, (byte)0x00, MSB, LSB, checksum, (byte)0xF7 }; 
        return b;
        }
    
    public static final int MAXIMUM_NAME_LENGTH = 12;
    public String revisePatchName(String name)
        {
        name = super.revisePatchName(name);  // trim first time
        if (name.length() > MAXIMUM_NAME_LENGTH)
            name = name.substring(0, MAXIMUM_NAME_LENGTH);
        
        StringBuffer nameb = new StringBuffer(name);                            
        for(int i = 0 ; i < nameb.length(); i++)
            {
            char c = nameb.charAt(i);
            if (c >= 32 && c <= 127)
                continue;
            else
                nameb.setCharAt(i, ' ');
            }
        name = nameb.toString();
        return super.revisePatchName(name);  // trim again
        }        

        
    /** Verify that all the parameters are within valid values, and tweak them if not. */
    public void revise()
        {
        // Chorus Feedback, Output Assign, and Velo Threshold can be wrong.
        if (model.get("chorusfeedback") == 0) 
            model.set("chorusfeedback", 1);
        for(int i = 1; i <= 6; i++)
            {
            if (model.get("part" + i + "velothreshold") == 0) 
                model.set("part" + i + "velothreshold", 127);
            if (model.get("part" + i + "outputassign") >= OUTPUT_ASSIGNS.length) 
                model.set("part" + i + "outputassign", 0);
            }
        
        // check the easy stuff -- out of range parameters
        super.revise();

        String nm = model.get("name", "Init");
        String newnm = revisePatchName(nm);
        if (!nm.equals(newnm))
            model.set("name", newnm);
        }
        
    public static String getSynthName() { return "Roland U-20 / 220 [Multi]"; }
    
    public String getPatchName(Model model) { return model.get("name", "Untitled  "); }

    public int getPauseAfterChangePatch() { return 100; }               // May be too short?

    public int getPauseAfterSendAllParameters() { return 100; } 

    public int getPauseAfterSendOneParameter() { return 25; }       // In the 1.07 firmware notes it says "at least 20ms" (http://llamamusic.com/d110/ROM_IC_Bug_Fixes.html).  In my firmware (1.10) the D-110 can handle changes thrown at it full blast, but earlier firmware (1.07) cannot.
 
    public void changePatch(Model tempModel) 
        {
        if (tempModel == null)
            tempModel = getModel();

        // We do a PC to the Patch's Rx Channel (setp/midi/rx control ch),
        // which we assume to be the same as the existing channel
                
        tryToSendMIDI(buildPC(getChannelOut(), tempModel.get("number")));
        }
    
    
    public Model getNextPatchLocation(Model model)
        {
        int number = model.get("number");
        
        number++;
        if (number >= 64)
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
        
        return "P-" + (model.get("number") + 1 < 100 ? (model.get("number") + 1 < 10 ? "00" : "0") : "") + ((model.get("number") + 1));
        }

    /** Roland only allows IDs from 17...32.  Don't ask. */
    public String reviseID(String id)
        {
        try
            {
            int val = Integer.parseInt(id);
            if (val < 17) val = 17;
            if (val > 32) val = 32;
            return "" + val;
            }
        catch (NumberFormatException ex)
            {
            return "" + (getID() + 1);
            }
        }
        
    public int getBatchDownloadWaitTime() { return 500; }



    /** Return a list of all patch number names, such as "1", "2", "3", etc.
        Default is null, which indicates that the patch editor does not support librarians.  */
    public String[] getPatchNumberNames() 
        { 
        String[] str = new String[64];
        for(int i = 0; i < 8; i++)
            for(int j = 0; j < 8; j++)
                str[i * 8 + j] = "" + (i * 8 + j + 1) + " ("  + (i + 1) + (j + 1) + ")";                // weird
        return str;
        }

    /** Return whether individual patches can be written.  Default is FALSE. */
    public boolean getSupportsPatchWrites() { return true; }

    /** Return the maximum number of characters a patch name may hold. The default returns 16. */
    public int getPatchNameLength() { return MAXIMUM_NAME_LENGTH; }

    /** Return true if individual (non-bank) patches on the synthesizer contain location information (bank, number). */
    public boolean getPatchContainsLocation() { return true; }

    public boolean librarianTested() { return true; }
    }
    
