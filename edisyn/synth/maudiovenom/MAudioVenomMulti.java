/***
    Copyright 2018 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.maudiovenom;

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
   A patch editor for the M-Audio Venom (Multimode)
        
   @author Sean Luke
*/

public class MAudioVenomMulti extends Synth
    {
    public static final byte DEFAULT_ID = (byte)0x7F;           // this seems to be what the venom returns
    
    public static final String[] BANKS = new String[] { "A", "B" };
    public static final String[] WRITEABLE_BANKS = new String[] { "B" };
    public static final String[] SINGLE_BANKS = new String[] { "A", "B", "C", "D" };
    public static final String[] AUX_PARAM_SOURCES = new String[] { "Multi", "Part 1", "Part 2", "Part 3", "Part 4" };

    public static final String[] VOICE_SOURCES = new String[] { "Multi", "Single" };
    
    public static final String[] ARP_MODES = new String[] { "Standard", "Phrase", "Drum" };
    public static final String[] ARP_SOURCES = new String[] { "Multi", "Single", "Pattern" };
    public static final String[] ARP_BANKS = new String[] { "A", "B" };
    public static final String[] AUX_FX_1_TYPES = new String[] 
    { 
    "Plate Reverb", "Room Reverb", "Hall Reverb", "Mono Echo", "Stereo Echo",
    "Mono 3/4 Echo", "Stereo 3/4 Echo", "Mono 4/4 Echo", "Stereo 4/4 Echo",
    "Mono Triplet", "Stereo Triplet", "Long Mono Delay", "Long Ping Pong"
    };
    public static final String[] AUX_FX_2_TYPES = new String[] {  "Chorus", "Flanger", "Phaser", "Delay" };
    public static final String[] INSERT_FX_TYPES = new String[]  { "Off", "EQ Bandpass", "Compressor", "Auto Wah", "Distortion", "Reducer" };
    public static final String[] ARP_NOTE_ORDERS = new String[]  { "Up", "Down", "Up/Down Excl.",  "Up/Down Incl.", "Down/Up Excl.",  "Down/Up Incl.", "Chord" };
                
    /* ARP goes 50...300 BPM.  That's 251 values, probably 8 bits */
        
    public static final String[] NOTES = new String[] { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };

        
    public MAudioVenomMulti()
        {
        if (parametersToIndex == null)
            parametersToIndex = new HashMap();
        for(int i = 0; i < parameters.length; i++)
            {
            parametersToIndex.put(parameters[i], Integer.valueOf(i));
            }

        /// SOUND PANEL
                
        SynthPanel soundPanel = new SynthPanel(this);
        
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        hbox.add(addGlobal(Style.COLOR_A()));
        hbox.addLast(addMasterEQ(Style.COLOR_B()));
        vbox.add(hbox);
        
        vbox.add(addAux1(Style.COLOR_B()));
        vbox.add(addAux2(Style.COLOR_B()));

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Global", soundPanel);


        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        hbox = new HBox();

        hbox.add(addPart(1, Style.COLOR_C()));
        hbox.addLast(addKeymap(1, Style.COLOR_A()));
        vbox.add(hbox);

        hbox = new HBox();
        hbox.add(addVoice(1, Style.COLOR_B()));
        hbox.addLast(addChannel(1, Style.COLOR_A()));
        vbox.add(hbox);
        vbox.add(addArpeggiator(1, Style.COLOR_B()));

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Part 1", soundPanel);

        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        hbox = new HBox();
                
        hbox.add(addPart(2, Style.COLOR_C()));
        hbox.addLast(addKeymap(2, Style.COLOR_B()));
        vbox.add(hbox);

        hbox = new HBox();
        hbox.add(addVoice(2, Style.COLOR_A()));
        hbox.addLast(addChannel(2, Style.COLOR_B()));
        vbox.add(hbox);
        vbox.add(addArpeggiator(2, Style.COLOR_A()));

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Part 2", soundPanel);

        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        hbox = new HBox();
                
        hbox.add(addPart(3, Style.COLOR_C()));
        hbox.addLast(addKeymap(3, Style.COLOR_A()));
        vbox.add(hbox);

        hbox = new HBox();
        hbox.add(addVoice(3, Style.COLOR_B()));
        hbox.addLast(addChannel(3, Style.COLOR_A()));
        vbox.add(hbox);
        vbox.add(addArpeggiator(3, Style.COLOR_B()));

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Part 3", soundPanel);

        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        hbox = new HBox();
                
        hbox.add(addPart(4, Style.COLOR_C()));
        hbox.addLast(addKeymap(4, Style.COLOR_B()));
        vbox.add(hbox);

        hbox = new HBox();
        hbox.add(addVoice(4, Style.COLOR_A()));
        hbox.addLast(addChannel(4, Style.COLOR_B()));
        vbox.add(hbox);
        vbox.add(addArpeggiator(4, Style.COLOR_A()));

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Part 4", soundPanel);


                        
        model.set("name", "Untitled");
        model.set("number", 0);
        model.set("bank", 0);
        
        loadDefaults();
        }
                
    public String getDefaultResourceFileName() { return "MAudioVenomMulti.init"; }
    public String getHTMLResourceFileName() { return "MAudioVenomMulti.html"; }

    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        JComboBox bank = new JComboBox(writing? WRITEABLE_BANKS : BANKS);
        bank.setEditable(false);
        bank.setMaximumRowCount(32);
        int b = model.get("bank");
        if (writing)
            {
            b -= 1;
            if (b < 0) b = 0;
            }
        bank.setSelectedIndex(b);
                
        JTextField number = new SelectedTextField("" + (model.get("number")), 3);
                
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
                showSimpleError(title, "The Patch Number must be an integer 0 ... 127");
                continue;
                }
            if (n < 0 || n > 127)
                {
                showSimpleError(title, "The Patch Number must be an integer 0 ... 127");
                continue;
                }
                                
            change.set("bank", bank.getSelectedIndex() + (writing ? 1 : 0));
            change.set("number", n);
                        
            return true;
            }
        }
                                                                      
        
    /** Add the global patch category (name, id, number, etc.) */
    public JComponent addNameGlobal(Color color)
        {
        Category globalCategory = new Category(this, "M-Audio Venom [Multi]", color);
        //globalCategory.makeUnresettable();
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        final PatchDisplay pd = new PatchDisplay(this, 3);
        comp = pd;
        vbox.add(comp);
                
        comp = new StringComponent("Patch Name", this, "name", 10, "Name must be up to 10 characters.")
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
        vbox.add(comp);
        hbox.add(vbox);
        hbox.add(Strut.makeHorizontalStrut(100));
                
        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }
        
        
    public JComponent addGlobal(Color color)
        {
        Category category = new Category(this, "General", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        comp = new LabelledDial("Volume", this, "progvolume", color, 0, 127);
        hbox.add(comp);

        hbox.add(Strut.makeHorizontalStrut(20));

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addPart(final int part, Color color)
        {
        final Category category = new Category(this, "Part " + part, color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        params = SINGLE_BANKS;
        comp = new Chooser("Bank", this, "part" + part + "bank", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                int bank = model.get("part" + part + "bank");
                int program = model.get("part" + part + "program");
                //System.err.println("part " + part + " bank = " + bank + " program = " + program);
                if (bank >= 0 && program >= 0)
                    category.setName("Part " + part + " (" + DEFAULT_SINGLE_PATCH_NAMES[bank][program] + ")");
                }
            };
        vbox.add(comp);

        comp = new CheckBox("Enable", this, "part" + part + "enable");
        vbox.add(comp);
        hbox.add(vbox);
                
        comp = new LabelledDial("Number", this, "part" + part + "program", color, 0, 127)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                int bank = model.get("part" + part + "bank");
                int program = model.get("part" + part + "program");
                if (bank >= 0 && program >= 0)
                    category.setName("Part " + part + " (" + DEFAULT_SINGLE_PATCH_NAMES[bank][program] + ")");
                }
            };
        hbox.add(comp);
        
        vbox = new VBox();
        comp = new PushButton("Show")
            {
            public void perform()
                {
                final MAudioVenom synth = new MAudioVenom();
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
                                                
                    synth.setTitleBarAux("[Part " + part + " of " + MAudioVenomMulti.this.model.get("name", "") + "]");
                    synth.sprout();
                    JFrame frame = ((JFrame)(SwingUtilities.getRoot(synth)));
                    frame.setVisible(true);

                    SwingUtilities.invokeLater(
                        new Runnable()
                            {
                            public void run() 
                                { 
                                Model tempModel = buildModel();
                                tempModel.set("bank", MAudioVenomMulti.this.model.get("part" + part + "bank"));
                                tempModel.set("number", MAudioVenomMulti.this.model.get("part" + part + "program"));
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
        vbox.add(comp);
        hbox.add(vbox);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addVoice(int part, Color color)
        {
        Category category = new Category(this, "Voice " + part, color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        final HBox out = new HBox();
        final HBox in = new HBox();
        final VBox outv = new VBox();
        final VBox inv = new VBox();
                
        params = VOICE_SOURCES;
        comp = new Chooser("Source", this, "part" + part + "voicesingle", params)       // I *think* this is insert
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                out.removeLast();
                outv.removeLast();
                if (model.get(key) == 0)        // MULTI
                    {
                    out.addLast(in);
                    outv.addLast(inv);
                    }
                out.revalidate();
                out.repaint();
                outv.revalidate();
                outv.repaint();
                }
            };
        vbox.add(comp);
        

        comp = new CheckBox("Polyphonic", this,  "part" + part + "voicemode");
        ((CheckBox)comp).addToWidth(1);
        inv.add(comp);
        comp = new CheckBox("Unison", this,  "part" + part + "unisonmode");
        inv.add(comp);
        
        outv.addLast(inv);              // need it so I can compute
        vbox.add(Strut.makeStrut(outv, false, true));
        vbox.addLast(outv);
        hbox.add(vbox);
        hbox.add(Strut.makeStrut(vbox, true, false));

        comp = new LabelledDial("Unison", this,  "part" + part + "unisoncount", color, 2, 12);
        ((LabelledDial)comp).addAdditionalLabel("Voices");
        in.add(comp);
                
        comp = new LabelledDial("Unison", this,  "part" + part + "unisondetune", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Detune");
        in.add(comp);
                
        comp = new LabelledDial("Coarse", this, "part" + part + "coarsetune", color, 0, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Tune");
        in.add(comp);

        comp = new LabelledDial("Fine", this, "part" + part + "finetune", color, 0, 127)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                if (value <= 64) return String.format("%2.1f", ((value - 64) / 64.0 * 50.0));
                else return String.format("%2.1f", ((value - 64) / 63.0) * 50.0);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Tune");
        in.add(comp);
        
        VBox inout = new VBox();
        inout.add(Strut.makeStrut(in, false, true));
        inout.add(out);
        
        //out.addLast(in);
        hbox.addLast(inout);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addChannel(int part, Color color)
        {
        Category category = new Category(this, "Channel " + part, color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();

        final HBox out = new HBox();
        final HBox in = new HBox();
        final VBox outv = new VBox();
        final VBox inv = new VBox();
        
        params = VOICE_SOURCES;
        comp = new Chooser("Source", this, "part" + part + "channelsingle", params)     // I *think* this is insert
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                out.removeLast();
                outv.removeLast();
                if (model.get(key) == 0)        // SINGLE (vs Multi) -- this is backwards from the ARP's Pattern/Single
                    {
                    out.addLast(in);
                    outv.addLast(inv);
                    }
                out.revalidate();
                out.repaint();
                outv.revalidate();
                outv.repaint();
                }
            };
        vbox.add(comp);
         
        params = INSERT_FX_TYPES;
        comp = new Chooser("Insert FX Type", this, "part" + part + "fxtype", params);   // I *think* this is insert
        inv.add(comp);
        
        outv.addLast(inv);              // need it so I can compute
        vbox.addLast(outv);
        hbox.add(vbox);
        hbox.add(Strut.makeStrut(vbox, true, false));

                        
        comp = new LabelledDial("Volume", this, "part" + part + "volume", color, 0, 127);
        in.add(comp);
                
        comp = new LabelledDial("Pan", this, "part" + part + "pan", color, 0, 127)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                if (value < 64) return "< " + (0 - (((value - 64) * 100) / 64));
                else if (value > 64) return "" + (((value - 64) * 100) / 63) + " >";
                else return "--";
                }
            };
        in.add(comp);
                
        comp = new LabelledDial("Direct", this, "part" + part + "direct", color, 0, 127);
        in.add(comp);
                
        comp = new LabelledDial("Aux 1", this, "part" + part + "aux1send", color, 0, 127);
        in.add(comp);
                
        comp = new LabelledDial("Aux 2", this, "part" + part + "aux2send", color, 0, 127);
        in.add(comp);

        //out.addLast(in);
        hbox.addLast(out);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addKeymap(int part, Color color)
        {
        Category category = new Category(this, "Key Map " + part, color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        comp = new CheckBox("Pitch Bend", this,  "part" + part + "pitchbendenable");
        ((CheckBox)comp).addToWidth(1);
        vbox.add(comp);
        comp = new CheckBox("Mod Wheel", this,  "part" + part + "modwheelenable");
        vbox.add(comp);
        comp = new CheckBox("Sustain", this,  "part" + part + "sustainenable");
        vbox.add(comp);
        hbox.add(vbox);
        
        vbox = new VBox();
        comp = new CheckBox("Expression", this,  "part" + part + "expressionenable");
        vbox.add(comp);
        comp = new CheckBox("Keyboard", this,  "part" + part + "keyboardenable");
        vbox.add(comp);
        comp = new CheckBox("External MIDI In", this,  "part" + part + "externalmidiinputenable");
        ((CheckBox)comp).addToWidth(1);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Channel", this, "part" + part + "channel", color, 0, 16)       
            {
            public String map(int value)
                {
                if (value == 0) return "Omni";
                else return "" + value;
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Low", this, "part" + part + "keylow", color, 0, 127)
            {
            public String map(int value)
                {
                return NOTES[value % 12] + (value / 12 - 2);                    
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Note");
        hbox.add(comp);

        comp = new LabelledDial("High", this, "part" + part + "keyhigh", color, 0, 127)
            {
            public String map(int value)
                {
                return NOTES[value % 12] + (value / 12 - 2);                    
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Note");
        hbox.add(comp);

        comp = new LabelledDial("Low", this, "part" + part + "vellow", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Velocity");
        hbox.add(comp);

        comp = new LabelledDial("High", this, "part" + part + "velhigh", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Velocity");
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }



    // aux1mode (enable) is either 0 or 127
    // Docs say aux1type is 0 or 1, but it's actually 0...12 (0 is NOT off)

    public JComponent addAux1(Color color)
        {
        Category category = new Category(this, "Aux 1 FX", color);
              
        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();

        final HBox out = new HBox();
        final HBox in = new HBox();

        final HBox reverb = new HBox();
                
        comp = new LabelledDial("Time", this, "aux1time", color, 0, 127);
        reverb.add(comp);
                
        comp = new LabelledDial("Tone", this, "aux1tonegain", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Gain");
        reverb.add(comp);
                
        comp = new LabelledDial("Tone", this, "aux1tonefrequency", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Frequency");
        reverb.add(comp);

        comp = new LabelledDial("Gate", this, "aux1gatethresh", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Threshold");
        reverb.add(comp);
        
        final HBox echo = new HBox();
                
        comp = new LabelledDial("Feedback", this, "aux1feedback", color, 0, 127);
        reverb.add(comp);
                
        VBox vbox = new VBox();

        params = AUX_PARAM_SOURCES;
        comp = new Chooser("Source", this, "aux1source", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                out.removeLast();
                if (model.get(key) == 0)        // MULTI
                    {
                    out.addLast(in);
                    }
                out.revalidate();
                out.repaint();
                }
            };
        vbox.add(comp);
        hbox.add(vbox);

        vbox = new VBox();
                
        params = AUX_FX_1_TYPES;
        comp = new Chooser("Type", this, "aux1type", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                in.removeLast();
                if (model.get("aux1type", 0) <= 2)      // reverb
                    {
                    in.addLast(reverb);
                    }
                else    // echo
                    {
                    in.addLast(echo);
                    }
                in.revalidate();
                in.repaint();
                }
            };              
        vbox.add(comp);
        
        comp = new CheckBox("Enable", this, "aux1mode");
        vbox.add(comp);

        in.add(vbox);
        
        in.addLast(reverb);
        
        comp = new LabelledDial("Depth", this, "aux1depth", color, 0, 127);
        in.add(comp);
                
        comp = new LabelledDial("Pre HP", this, "aux1prehp", color, 0, 127);
        in.add(comp);
                
        comp = new LabelledDial("Pre Delay", this, "aux1predelay", color, 0, 127);
        in.add(comp);

        comp = new LabelledDial("High", this, "aux1highdamp", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Damp");
        in.add(comp);
                
        // out.addLast(in);
        hbox.add(Strut.makeStrut(in, true, false));
        hbox.addLast(out);
        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }



    // Docs say aux1type is 0...4, but it's actually 0...3 (0 is NOT off)

    public JComponent addAux2(Color color)
        {
        Category category = new Category(this, "Aux 2 FX", color);
              
        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();

        final HBox out = new HBox();
        final HBox in = new HBox();

        final HBox chorus = new HBox();
                
        comp = new LabelledDial("High", this, "aux2highdamp", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Damp");
        chorus.add(comp);
                
        comp = new LabelledDial("Time", this, "aux2time", color, 0, 127);
        chorus.add(comp);
                
        VBox vbox = new VBox();
 
        params = AUX_PARAM_SOURCES;
        comp = new Chooser("Source", this, "aux2source", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                out.removeLast();
                if (model.get(key) == 0)        // MULTI
                    {
                    out.addLast(in);
                    }
                out.revalidate();
                out.repaint();
                }
            };
        vbox.add(comp);
        hbox.add(vbox);
        vbox = new VBox();
                
        params = AUX_FX_2_TYPES;
        comp = new Chooser("Type", this, "aux2type", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                in.removeLast();
                if (model.get("aux2type", 0) != 2)      // anything but phaser
                    {
                    in.addLast(chorus);
                    }
                in.revalidate();
                in.repaint();
                }
            };              
        vbox.add(comp);
        comp = new CheckBox("Enable", this, "aux2mode");
        vbox.add(comp);

        in.add(vbox);
        
        in.addLast(chorus);
        
        comp = new LabelledDial("Send", this, "aux2toaux1", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("To Aux 1");
        in.add(comp);
                
        comp = new LabelledDial("Depth", this, "aux2depth", color, 0, 127);
        in.add(comp);
                
        comp = new LabelledDial("Pre LP", this, "aux2prelp", color, 0, 127);
        in.add(comp);
                
        comp = new LabelledDial("Pre HP", this, "aux2prehp", color, 0, 127);
        in.add(comp);
                
        comp = new LabelledDial("Feedback", this, "aux2feedback", color, 0, 127);
        in.add(comp);

        comp = new LabelledDial("LFO", this, "aux2lforate", color, 0, 123);
        ((LabelledDial)comp).addAdditionalLabel("Rate");
        in.add(comp);

        comp = new LabelledDial("LFO", this, "aux2lfodepth", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Depth");
        in.add(comp);
                
        // out.addLast(in);
        hbox.add(Strut.makeStrut(in, true, false));
        hbox.addLast(out);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addMasterEQ(Color color)
        {
        Category category = new Category(this, "Master EQ", color);
              
        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();

        final HBox out = new HBox();
        final HBox in = new HBox();

        VBox vbox = new VBox();
        params = AUX_PARAM_SOURCES;
        comp = new Chooser("Source", this, "mastersource", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                out.removeLast();
                if (model.get(key) == 0)        // MULTI
                    {
                    out.addLast(in);
                    }
                out.revalidate();
                out.repaint();
                }
            };
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Low", this, "mastereqlowfreq", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Freq");
        in.add(comp);

        comp = new LabelledDial("Low", this, "mastereqlowgain", color, 0, 127)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                if (value <= 64) return "" + (((value - 64) * 100) / 64) + "%";
                else return "" + (((value - 64) * 100) / 63) + "%";
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Gain");
        in.add(comp);

        comp = new LabelledDial("Mid", this, "mastereqmidfreq", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Freq");
        in.add(comp);

        comp = new LabelledDial("Mid", this, "mastereqmidgain", color, 0, 127)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                if (value <= 64) return "" + (((value - 64) * 100) / 64) + "%";
                else return "" + (((value - 64) * 100) / 63) + "%";
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Gain");
        in.add(comp);

        comp = new LabelledDial("High", this, "mastereqhighfreq", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Freq");
        in.add(comp);

        comp = new LabelledDial("High", this, "mastereqhighgain", color, 0, 127)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                if (value <= 64) return "" + (((value - 64) * 100) / 64) + "%";
                else return "" + (((value - 64) * 100) / 63) + "%";
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Gain");
        in.add(comp);

        //out.addLast(in);
        hbox.add(Strut.makeStrut(in, true, false));
        hbox.addLast(out);
        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    /// ARP SOURCES
    ///
    /// There are THREE possible sources for arp header parameters:
    /// (1) Set in the multi, (2) derived from the underlying source, (3) derived from the source's pattern
    /// These are determined as follows

    /// Source              MltParam.Source bits            ArpPatch.Arpsrc_f
    ///     Multi           00                                                      7F
    ///     Single          04                                                      00
    /// Pattern             00                                                      00

    /// We'll store this in arpsource but we'll have to modify it


    // arpbipolar is 0 or 127

    // arplatchkeys is 0 or 127

    // docs say octave is -4...4, but it is 60...68

    public JComponent addArpeggiator(int part, Color color)
        {
        final Category category = new Category(this, "Arpeggiator " + part, color);
              
        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();

        final HBox out = new HBox();
        final HBox in = new HBox();
        final VBox outv = new VBox();
        final VBox inv = new VBox();

        final LabelledDial octaverange = new LabelledDial("Octave", this, "part" + part + "arpoctaverange", color, 60, 68, 64);
        ((LabelledDial)octaverange).addAdditionalLabel("Range");

        final LabelledDial rootnote = new LabelledDial("Root", this, "part" + part + "arprootnote", color, 0, 127)
            {
            public String map(int value)
                {
                return NOTES[value % 12] + (value / 12 - 2);                    
                }
            };
        ((LabelledDial)rootnote).addAdditionalLabel("Note");

        final VBox noteorder_bipolar = new VBox();
        params = ARP_NOTE_ORDERS;
        comp = new Chooser("Note Order", this, "part" + part + "arpnoteorder", params);
        noteorder_bipolar.add(comp);

        comp = new CheckBox("Bipolar", this, "part" + part + "arpbipolar");
        noteorder_bipolar.add(comp);

        VBox vbox = new VBox();
        params = ARP_SOURCES;
        comp = new Chooser("Source", this, "part" + part + "arpsource", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);

                if (model.get(key) == 0)                // MULTI"part" + part + "arpsource"
                    {
                    int bank = model.get("part" + part + "arpbank");
                    int pattern = model.get("part" + part + "arppattern");
                    if (bank >= 0 && pattern >= 0)
                        {
                        category.setName("Arpeggiator " + part + " (" + DEFAULT_ARP_PATTERN_NAMES[bank][pattern] + ")");
                        }
                    }
                else
                    {
                    category.setName("Arpeggiator " + part);
                    }

                out.removeLast();
                outv.removeLast();
                if (model.get(key) == 0)        // MULTI
                    {
                    out.addLast(in);
                    outv.addLast(inv);
                    }
                out.revalidate();
                out.repaint();
                outv.revalidate();
                outv.repaint();
                }
            };
        vbox.add(comp);

        comp = new CheckBox("Enable", this, "part" + part + "arpenable");
        vbox.add(comp);
        hbox.add(vbox);
                
        vbox = new VBox();

        HBox inout = new HBox();
                
        params = ARP_BANKS;
        comp = new Chooser("Bank", this, "part" + part + "arpbank", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                int bank = model.get("part" + part + "arpbank");
                int pattern = model.get("part" + part + "arppattern");
                if (bank >= 0 && pattern >= 0)
                    {
                    category.setName("Arpeggiator " + part + " (" + DEFAULT_ARP_PATTERN_NAMES[bank][pattern] + ")");
                    }
                }
            };
        inv.add(comp);
        inout.add(Strut.makeStrut(inv, true, false));
        inout.addLast(outv);
        vbox.add(inout);

        comp = new PushButton("Show Arp")
            {
            public void perform()
                {
                final MAudioVenomArp synth = new MAudioVenomArp();
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
                                                
                    synth.setTitleBarAux("[Arp of " + MAudioVenomMulti.this.model.get("name", "") + "]");
                    synth.sprout();
                    JFrame frame = ((JFrame)(SwingUtilities.getRoot(synth)));
                    frame.setVisible(true);

                    SwingUtilities.invokeLater(
                        new Runnable()
                            {
                            public void run() 
                                { 
                                Model tempModel = buildModel();
                                tempModel.set("bank", MAudioVenomMulti.this.model.get("part" + part + "arpbank"));
                                tempModel.set("number", MAudioVenomMulti.this.model.get("part" + part + "arppattern"));
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
        inv.add(comp);
        hbox.add(vbox);

        hbox.addLast(out);
        //out.addLast(in);

        vbox = new VBox();        
        params = ARP_MODES;
        comp = new Chooser("Mode", this, "part" + part + "arpmode", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);

                in.remove(rootnote);
                in.remove(octaverange);
                in.remove(noteorder_bipolar);

                int val = model.get("part" + part + "arpmode", 0);
                if (val == 0)           // standard
                    {
                    in.add(octaverange);
                    in.add(noteorder_bipolar);
                    }
                else if (val == 1)      // phrase
                    {
                    in.add(octaverange);
                    in.add(rootnote);
                    }
                else                            // drum 
                    {
                    // addnothing
                    }
                        
                in.revalidate();
                in.repaint();
                }
            };
        vbox.add(comp);

        comp = new CheckBox("Latch", this, "part" + part + "arplatchkeys");
        vbox.add(comp);
        in.add(vbox);

        comp = new LabelledDial("Pattern", this, "part" + part + "arppattern", color, 0, 127)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);

                if (model.get("part" + part + "arpsource") == 0)                // MULTI
                    {
                    int bank = model.get("part" + part + "arpbank");
                    int pattern = model.get("part" + part + "arppattern");
                    if (bank >= 0 && pattern >= 0)
                        category.setName("Arpeggiator " + part + " (" + DEFAULT_ARP_PATTERN_NAMES[bank][pattern] + ")");
                    }
                else
                    {
                    category.setName("Arpeggiator " + part);
                    }
                }
            };
        in.add(comp);

        in.add(octaverange);
        in.add(noteorder_bipolar);
        hbox.add(Strut.makeStrut(octaverange, true, false));            //so we keep the same height

        //category.setName("Arpeggiator " + part);
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    /*
      public byte getID() 
      { 
      try 
      { 
      byte b = (byte)(Byte.parseByte(tuple.id));
      if (b >= 0 && b < 16) return b;
      }
      catch (NullPointerException e) { } // expected.  Happens when tuple's not built yet
      catch (NumberFormatException e) { Synth.handleException(e); }
      return 0;
      }
        
      public String reviseID(String id)
      {
      try 
      { 
      byte b =(byte)(Byte.parseByte(id)); 
      if (b >= 0 && b < 16) return "" + b;
      } 
      catch (NumberFormatException e) { }             // expected
      return "" + getID();
      }
    */


    public Object[] emitAll(String key)
        {
        if (key.equals("number")) return new Object[0];  // this is not emittable
        if (key.equals("bank")) return new Object[0];
        
        if (key.equals("name"))
            {
            String name = model.get(key, "") + "            ";
            Object[] ret = new Object[10];
            for(int i = 0; i < 10; i++)
                {
                int val = (int)(name.charAt(i));
                byte valMSB = (byte)(val >>> 7);
                byte valLSB = (byte)(val & 127);
                byte paramMSB = (byte)0x01;
                byte paramLSB = (byte)(0x23 + i);               // PatchName[0...9], p. 96

                byte[] data = new byte[] 
                    { 
                    (byte)0xF0, 
                    (byte)0x00,             // M-Audio
                    (byte)0x01, 
                    (byte)0x05, 
                    (byte)0x21,                     // Venom 
                    (byte)DEFAULT_ID, //(byte)getID(), 
                    (byte)0x02,                     // Write Data Dump 
                    (byte)0x0A,             // Edit Multi Param
                    paramMSB,
                    paramLSB,
                    valMSB,                         // This is useless because the data is always 7-bit
                    valLSB,
                    (byte)0xF7
                    };

                ret[i] = data;
                }
            return ret;
            }
        else if (key.endsWith("controlenable"))
            {
            int part = 4;
            if (key.startsWith("part1"))
                {
                part = 1;
                }
            else if (key.startsWith("part2"))
                {
                part = 2;
                }
            else if (key.startsWith("part3"))
                {
                part = 3;
                }

            // map 
            // 0    pitchbend
            // 1    mod wheel
            // 2    sustain
            // 3    expression
            // 4    keyboard
            // 5    external midi input
                
            byte paramMSB = (byte)0x00;
            byte paramLSB = (byte)(0x0D + 20 * (part - 1) + 19);        // 0x20, 0x34, 0x48, 0x5C
            int val = (model.get("part" + part + "pitchbendenable") << 0) |
                (model.get("part" + part + "modwheelenable") << 1) |
                (model.get("part" + part + "sustainenable") << 2) |
                (model.get("part" + part + "expressionenable") << 3) |
                (model.get("part" + part + "keyboardenable") << 4) |
                (model.get("part" + part + "externalmidiinputenable") << 5);
            
            byte valMSB = (byte)(val >>> 7);
            byte valLSB = (byte)(val & 127);
            
            byte[] data = new byte[] 
                { 
                (byte)0xF0, 
                (byte)0x00,             // M-Audio
                (byte)0x01, 
                (byte)0x05, 
                (byte)0x21,                     // Venom 
                (byte)DEFAULT_ID, //(byte)getID(), 
                (byte)0x02,                     // Write Data Dump 
                (byte)0x0A,             // Edit Multi Param
                paramMSB,
                paramLSB,
                valMSB,                         // This is useless because the data is always 7-bit
                valLSB,
                (byte)0xF7
                };
            return new Object[] { data };
            }
        else if (key.endsWith("channelsingle") || key.endsWith("voicesingle") || key.endsWith("arpsource"))
            {
            int part = 4;
            if (key.startsWith("part1"))
                {
                part = 1;
                }
            else if (key.startsWith("part2"))
                {
                part = 2;
                }
            else if (key.startsWith("part3"))
                {
                part = 3;
                }
                        
            // map 
            // Flags appear to be:
            // 0    channel single
            // 1    voice single
            // 2    arpsource
                
            byte paramMSB = (byte)0x00;
            byte paramLSB = (byte)(part - 1);   // 00 ... 04
            int val = (model.get("part" + part + "channelsingle") << 0) |
                (model.get("part" + part + "voicesingle") << 1) |
                ((model.get("part" + part + "arpsource") == 1 ? 1 : 0) << 2);           // if it's single (1), set 1, else set 0
            byte valMSB = (byte)(val >>> 7);
            byte valLSB = (byte)(val & 127);
            
            byte[] data = new byte[] 
                { 
                (byte)0xF0, 
                (byte)0x00,             // M-Audio
                (byte)0x01, 
                (byte)0x05, 
                (byte)0x21,                     // Venom 
                (byte)DEFAULT_ID,               //(byte)getID(), 
                (byte)0x02,                     // Write Data Dump 
                (byte)0x0A,             // Edit Multi Param
                paramMSB,
                paramLSB,
                valMSB,                         // This is useless because the data is always 7-bit
                valLSB,
                (byte)0xF7
                };
            
            // we also need to emit Arpsrc_f    

            paramMSB = (byte)0x00;
            paramLSB = (byte)(0x7C + 10 * (part - 1)); // 00 ... 04
            val = (model.get("part" + part + "arpsource") == 0 ? 0x7F : 0);                    // if it's Multi (0), set 0x7F else set 0
            valMSB = (byte)(val >>> 7);
            valLSB = (byte)(val & 127);
            
            byte[] data2 = new byte[] 
                { 
                (byte)0xF0, 
                (byte)0x00,             // M-Audio
                (byte)0x01, 
                (byte)0x05, 
                (byte)0x21,                     // Venom 
                (byte)DEFAULT_ID,               //(byte)getID(), 
                (byte)0x02,                     // Write Data Dump 
                (byte)0x0A,             // Edit Multi Param
                paramMSB,
                paramLSB,
                valMSB,                         // This is useless because the data is always 7-bit
                valLSB,
                (byte)0xF7
                };
            
             
            return new Object[] { data, data2 };
            }
        else
            {
            int val = model.get(key);
                
            if (key.equals("part1enable") || 
                key.equals("part2enable") || 
                key.equals("part3enable") || 
                key.equals("part4enable") || 
                key.equals("part1unisonmode") || 
                key.equals("part2unisonmode") || 
                key.equals("part3unisonmode") || 
                key.equals("part4unisonmode") || 
                key.equals("aux1mode") || 
                key.equals("aux2mode") || 
                key.equals("part1arpenable") || 
                key.equals("part2arpenable") || 
                key.equals("part3arpenable") || 
                key.equals("part4arpenable") || 
                key.equals("part1arpbipolar") || 
                key.equals("part2arpbipolar") || 
                key.equals("part3arpbipolar") || 
                key.equals("part4arpbipolar") || 
                key.equals("part1latchkeys") || 
                key.equals("part2latchkeys") || 
                key.equals("part3latchkeys") || 
                key.equals("part4latchkeys"))
                {
                if (val == 1) 
                    val = 127;
                }

            //System.err.println(key);
            int param = ((Integer)(parametersToIndex.get(key))).intValue();
            byte paramMSB = (byte)(param >>> 7);
            byte paramLSB = (byte)(param & 127);
            byte valMSB = (byte)(val >>> 7);
            byte valLSB = (byte)(val & 127);
            
            byte[] data = new byte[] 
                { 
                (byte)0xF0, 
                (byte)0x00,             // M-Audio
                (byte)0x01, 
                (byte)0x05, 
                (byte)0x21,                     // Venom 
                (byte)DEFAULT_ID,               //(byte)getID(), 
                (byte)0x02,                     // Write Data Dump 
                (byte)0x0A,             // Edit Multi Param
                paramMSB,
                paramLSB,
                valMSB,                         // This is useless because the data is always 7-bit
                valLSB,
                (byte)0xF7
                };
            return new Object[] { data };
            }
                
        }

    public int processArpOctave(byte val)
        {
        //System.err.println("arp octave val " + val);
        int o = val;
        if (o >= 60 && o <= 68) // we're good
            return o;
        //System.err.println("Unusual arp octave value " + o);
        if (o >= -4 && o <= 4)
            return 64 + o;
        else if (o >= 124 && o <= 127)          // (-4 ... -1)
            return (o - 128 + 64);
        else if (o >= -128 && o <= -124)        // (0 ... 
            return (o + 128 + 64);
        else 
            {
            System.err.println("BAD unconverted arp octave value " + o);
            return 0;
            }
        }



    
    //// These are a lot like the Prophet '08 7<-->8 bit conversions.
    ////
    //// The Venom manual describes conversion from 8 bit to 7 bit as
    //// taking 7 bytes at a time, removing the top bits and putting them
    //// in the first byte, producing 8 7-bit bytes as a result.  That's all
    //// fine and good, except that the data doesn't come in 7 byte chunks.
    //// It's 198 bytes long, which isn't a multiple of 7.  It appears that
    //// the answer is to take the remainder (2 bytes), strip off the high bits
    //// and make 3 more 7-bit bytes.


    // converts up to but not including 'to'
    byte[] convertTo8Bit(byte[] data, int from, int to)
        {
        // How big?
        int main = ((to - from) / 8) * 8;
        int remainder = (to - from) - main;
        int size = ((to - from) / 8) * 7 + (remainder - 1);
        
        byte[] newd = new byte[size];
        
        int j = 0;
        for(int i = from; i < to; i += 8)
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
        int main = ((data.length) / 7) * 7;
        int remainder = data.length - main;
        int size = (data.length) / 7 * 8 + (remainder + 1);
        byte[] newd = new byte[size];   
             
        int j = 0;
        for(int i = 0; i < data.length; i+=7)
            {
            // First load the top bits
            for(int x = 0; x < 7; x++)
                {
                if (i+x < data.length)
                    newd[j] = (byte)(newd[j] | (((data[i + x] >>> 7) & 1) << x));
                }
            j++;
            // Next load the data
            for(int x = 0; x < 7; x++)
                {
                if (i+x < data.length)
                    newd[j+x] = (byte)(data[i+x] & 127);
                }
            j+=7;
            }
        return newd;
        }




    public Object[] emitAll(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        if (tempModel == null)
            tempModel = getModel();

        int BB = model.get("bank", 0);
        int NN = model.get("number", 0);

        byte[] data = new byte[210];
        
        data[0] = (byte)0xF0;
        data[1] = (byte)0x00;           // M-audio
        data[2] = (byte)0x01;
        data[3] = (byte)0x05;
        data[4] = (byte)0x21;           // Venom
        data[5] = (byte)DEFAULT_ID;             //(byte)getID();
        data[6] = (byte)0x02;           // Write Data Dump
        //data[7] = (byte)(toWorkingMemory ? 0x00 : 0x02);
        //data[8] = (byte)(toWorkingMemory ? 0x02 : BB + 1);              // it's literally "1..2"
        //data[9] = (byte)NN;             // ignored when toWorkingMemory
        data[7] = (byte)0x00;
        data[8] = (byte)0x02;
        data[9] = (byte)0x00;
        data[data.length - 1] = (byte)0xF7;     
                
        byte[] d = new byte[parameters.length];
        for(int i = 0; i < parameters.length; i++)
            {
            String key = parameters[i];
                
            if (key.equals("-"))
                {
                // nothing
                }
            else if (key.equals("part1source"))
                {
                d[i] = (byte)(
                    (model.get("part1channelsingle") << 0) |
                    (model.get("part1voicesingle") << 1) |
                    ((model.get("part1arpsource") == 1 ? 1 : 0) << 2));                     // if it's single (1), set1, else set 0
                }
            else if (key.equals("part2source"))
                {
                d[i] = (byte)(
                    (model.get("part2channelsingle") << 0) |
                    (model.get("part2voicesingle") << 1) |
                    ((model.get("part2arpsource") == 1 ? 1 : 0) << 2));                     // if it's single (1), set1, else set 0
                }
            else if (key.equals("part3source"))
                {
                d[i] = (byte)(
                    (model.get("part3channelsingle") << 0) |
                    (model.get("part3voicesingle") << 1) |
                    ((model.get("part3arpsource") == 1 ? 1 : 0) << 2));                     // if it's single (1), set1, else set 0
                }
            else if (key.equals("part4source"))
                {
                d[i] = (byte)(
                    (model.get("part4channelsingle") << 0) |
                    (model.get("part4voicesingle") << 1) |
                    ((model.get("part4arpsource") == 1 ? 1 : 0) << 2));                     // if it's single (1), set1, else set 0
                }
            else if (key.equals("part1controlenable"))
                {
                d[i] = (byte)(
                    (model.get("part1pitchbendenable") << 0) |
                    (model.get("part1modwheelenable") << 1) |
                    (model.get("part1sustainenable") << 2) |
                    (model.get("part1expressionenable") << 3) |
                    (model.get("part1keyboardenable") << 4) |
                    (model.get("part1externalmidiinputenable") << 5));
                }
            else if (key.equals("part2controlenable"))
                {
                d[i] = (byte)(
                    (model.get("part2pitchbendenable") << 0) |
                    (model.get("part2modwheelenable") << 1) |
                    (model.get("part2sustainenable") << 2) |
                    (model.get("part2expressionenable") << 3) |
                    (model.get("part2keyboardenable") << 4) |
                    (model.get("part2externalmidiinputenable") << 5));
                }
            else if (key.equals("part3controlenable"))
                {
                d[i] = (byte)(
                    (model.get("part3pitchbendenable") << 0) |
                    (model.get("part3modwheelenable") << 1) |
                    (model.get("part3sustainenable") << 2) |
                    (model.get("part3expressionenable") << 3) |
                    (model.get("part3keyboardenable") << 4) |
                    (model.get("part3externalmidiinputenable") << 5));
                }
            else if (key.equals("part4controlenable"))
                {
                d[i] = (byte)(
                    (model.get("part4pitchbendenable") << 0) |
                    (model.get("part4modwheelenable") << 1) |
                    (model.get("part4sustainenable") << 2) |
                    (model.get("part4expressionenable") << 3) |
                    (model.get("part4keyboardenable") << 4) |
                    (model.get("part4externalmidiinputenable") << 5));
                }
            else if (key.equals("part1arpsource") ||
                key.equals("part2arpsource") || 
                key.equals("part3arpsource") || 
                key.equals("part4arpsource"))
                {
                d[i] = (byte)(model.get(key) == 0 ? 127 : 0);                           // if it's Multi (0), set 0x7F, else 0
                }
            else if (key.equals("part1enable") || 
                key.equals("part2enable") || 
                key.equals("part3enable") || 
                key.equals("part4enable") || 
                key.equals("part1unisonmode") || 
                key.equals("part2unisonmode") || 
                key.equals("part3unisonmode") || 
                key.equals("part4unisonmode") || 
                key.equals("aux1mode") || 
                key.equals("aux2mode") || 
                key.equals("part1arpenable") || 
                key.equals("part2arpenable") || 
                key.equals("part3arpenable") || 
                key.equals("part4arpenable") || 
                key.equals("part1arpbipolar") || 
                key.equals("part2arpbipolar") || 
                key.equals("part3arpbipolar") || 
                key.equals("part4arpbipolar") || 
                key.equals("part1latchkeys") || 
                key.equals("part2latchkeys") || 
                key.equals("part3latchkeys") || 
                key.equals("part4latchkeys"))
                {
                d[i] = (byte)(model.get(key) == 0 ? 0 : 127);
                }
            else
                {
                d[i] = (byte)(model.get(key));
                }
                
            //System.err.println("" + i + " " + key + " " + d[i]);
            }
                                
        // Load name
        String name = model.get("name", "") + "          ";
        for(int i = 0; i < 10; i++)
            {
            d[163 + i] = (byte)(name.charAt(i));
            }

        // Nybblize, or whatever you'd call it, into data
        d = convertTo7Bit(d);
        System.arraycopy(d, 0, data, 10, d.length);
                
        // Compute checksum
        data[data.length - 2] = checksum(data, 6, data.length - 2);             // starting at command

        Object[] result = new Object[] { data };

        // Now we need to write properly
        if (!toWorkingMemory)
            {
            result = new Object[] 
                { 
                data, 
                // Issue a Store Patch
                new byte[] 
                    { 
                    (byte)0xF0, 
                    0x00,
                    0x01,
                    0x05, 
                    0x21, 
                    (byte)DEFAULT_ID, //getID(), 
                    0x06,                       // Store Patch
                    0x02,                                           // Multi Patch Dump
                    (byte)(BB + 1),
                    (byte)NN,
                    (byte)0xF7 
                    } 
                };
            }

        return result;
        }

    // manual says: 
    // The checksum is calculated as the sum of all bytes taken from the <cmd> byte 
    // and stores 0-Total with the top bit set to 0. When a SysEx is received, it 
    // totals up all values from the <cmd> byte including the checksum and the 
    // result in the bot- tom 7 bits should be 0.

    // FIXME: is this right?
    public byte checksum(byte[] data, int start, int end)
        {
        int sum = 0;
        for(int i = start; i < end; i++)
            {
            sum += data[i];
            }
        return (byte)((0-sum) & 0x7F);
        }
        

    public int parse(byte[] data, boolean fromFile)
        {
        if (data[7] == 0x02)     // it's going to a specific patch (0x02 == Multi Patch Dump as opposed to 0x00 == Edit Buffer Dump)
            {
            int bank = data[8] - 1;
            if (bank < 0 || bank > 1) bank = 0;
            model.set("bank", bank);
            int number = data[9];
            model.set("number", number);
            }

        // First denybblize
        byte[] d = convertTo8Bit(data, 10, data.length - 2);
        
        //        model.debug = true;
                        
        // Load name
        char[] name = new char[10];
        for(int i = 0; i < 10; i++)
            {
            name[i] = (char)(d[163 + i] & 127);
            }
        model.set("name", new String(name));
                                
        // Load remaining parameters
        for(int i = 0; i < parameters.length; i++)
            {
            String key = parameters[i];
                        
            // no else here
            if (key.equals("-"))
                {
                // nothing
                }
            else if (key.equals("part1source"))
                {
                model.set("part1channelsingle", (d[i] >>> 0) & 0x1);
                model.set("part1voicesingle", (d[i] >>> 1) & 0x1);
                //model.set("part1arpsource", (d[i] >>> 2) & 0x1);                      // we don't set it here, we set it below
                }
            else if (key.equals("part2source"))
                {
                model.set("part2channelsingle", (d[i] >>> 0) & 0x1);
                model.set("part2voicesingle", (d[i] >>> 1) & 0x1);
                //model.set("part2arpsource", (d[i] >>> 2) & 0x1);                      // we don't set it here, we set it below
                }
            else if (key.equals("part3source"))
                {
                model.set("part3channelsingle", (d[i] >>> 0) & 0x1);
                model.set("part3voicesingle", (d[i] >>> 1) & 0x1);
                //model.set("part3arpsource", (d[i] >>> 2) & 0x1);                      // we don't set it here, we set it below
                }
            else if (key.equals("part4source"))
                {
                model.set("part4channelsingle", (d[i] >>> 0) & 0x1);
                model.set("part4voicesingle", (d[i] >>> 1) & 0x1);
                //model.set("part4arpsource", (d[i] >>> 2) & 0x1);                      // we don't set it here, we set it below
                }
            else if (key.equals("part1controlenable"))
                {
                model.set("part1pitchbendenable", (d[i] >>> 0) & 0x1);
                model.set("part1modwheelenable", (d[i] >>> 1) & 0x1);
                model.set("part1sustainenable", (d[i] >>> 2) & 0x1);
                model.set("part1expressionenable", (d[i] >>> 3) & 0x1);
                model.set("part1keyboardenable", (d[i] >>> 4) & 0x1);
                model.set("part1externalmidiinputenable", (d[i] >>> 5) & 0x1);
                }
            else if (key.equals("part2controlenable"))
                {
                model.set("part2pitchbendenable", (d[i] >>> 0) & 0x1);
                model.set("part2modwheelenable", (d[i] >>> 1) & 0x1);
                model.set("part2sustainenable", (d[i] >>> 2) & 0x1);
                model.set("part2expressionenable", (d[i] >>> 3) & 0x1);
                model.set("part2keyboardenable", (d[i] >>> 4) & 0x1);
                model.set("part2externalmidiinputenable", (d[i] >>> 5) & 0x1);
                }
            else if (key.equals("part3controlenable"))
                {
                model.set("part3pitchbendenable", (d[i] >>> 0) & 0x1);
                model.set("part3modwheelenable", (d[i] >>> 1) & 0x1);
                model.set("part3sustainenable", (d[i] >>> 2) & 0x1);
                model.set("part3expressionenable", (d[i] >>> 3) & 0x1);
                model.set("part3keyboardenable", (d[i] >>> 4) & 0x1);
                model.set("part3externalmidiinputenable", (d[i] >>> 5) & 0x1);
                }
            else if (key.equals("part4controlenable"))
                {
                model.set("part4pitchbendenable", (d[i] >>> 0) & 0x1);
                model.set("part4modwheelenable", (d[i] >>> 1) & 0x1);
                model.set("part4sustainenable", (d[i] >>> 2) & 0x1);
                model.set("part4expressionenable", (d[i] >>> 3) & 0x1);
                model.set("part4keyboardenable", (d[i] >>> 4) & 0x1);
                model.set("part4externalmidiinputenable", (d[i] >>> 5) & 0x1);
                }
            else if (key.equals("part1arpsource") ||
                key.equals("part2arpsource") || 
                key.equals("part3arpsource") || 
                key.equals("part4arpsource"))
                {
                if (d[i] == 0x7F)                                                                               // it's multi for sure
                    {
                    model.set(key, 0);
                    }
                else if (key.equals("part1arpsource"))
                    {
                    model.set(key, ((d[0] >>> 2) & 0x1) == 1 ? 1 : 2);              // d[0] = part1source.  If it's got an 0x04, set SINGLE, else set PATTERN
                    }
                else if (key.equals("part2arpsource"))
                    {
                    model.set(key, ((d[1] >>> 2) & 0x1) == 1 ? 1 : 2);              // d[1] = part2source.  If it's got an 0x04, set SINGLE, else set PATTERN
                    }
                else if (key.equals("part3arpsource"))
                    {
                    model.set(key, ((d[2] >>> 2) & 0x1) == 1 ? 1 : 2);              // d[2] = part3source.  If it's got an 0x04, set SINGLE, else set PATTERN
                    }
                else if (key.equals("part4arpsource"))
                    {
                    model.set(key, ((d[3] >>> 2) & 0x1) == 1 ? 1 : 2);              // d[3] = part4source.  If it's got an 0x04, set SINGLE, else set PATTERN
                    }
                }
            else if (key.equals("part1enable") || 
                key.equals("part2enable") || 
                key.equals("part3enable") || 
                key.equals("part4enable") || 
                key.equals("part1unisonmode") || 
                key.equals("part2unisonmode") || 
                key.equals("part3unisonmode") || 
                key.equals("part4unisonmode") || 
                key.equals("aux1mode") || 
                key.equals("aux2mode") || 
                key.equals("part1arpenable") || 
                key.equals("part2arpenable") || 
                key.equals("part3arpenable") || 
                key.equals("part4arpenable") || 
                key.equals("part1arpbipolar") || 
                key.equals("part2arpbipolar") || 
                key.equals("part3arpbipolar") || 
                key.equals("part4arpbipolar") || 
                key.equals("part1latchkeys") || 
                key.equals("part2latchkeys") || 
                key.equals("part3latchkeys") || 
                key.equals("part4latchkeys"))
                {
                model.set(key, d[i] < 64 ? 0 : 1);
                }
            else
                {
                model.set(key, d[i]);
                }
                
            //System.err.println("" + i + " " + key + " " + d[i]);
            }
                        
                        
        // we must now let the venom know to shut up or else it'll keep sending us junk

        //        model.debug = false;
                
        if (!fromFile)
            {
            boolean sendMIDI = getSendMIDI();
            setSendMIDI(true);
                                
            // CANCEL
            // We send a CANCEL instead of an ACK because the Venom seems to respond to ACKs by often resending
            // the data.
            tryToSendSysex(new byte[] { (byte)0xF0, 0x00, 0x01, 0x05, 0x21, 
                (byte)DEFAULT_ID, //getID(), 
                0x7D,                       // ACK is 0x7F
                (byte)0xF7 });
                                
            
            setSendMIDI(sendMIDI);
            }

        revise();
        return PARSE_SUCCEEDED;
        }
        
        
    void switchScreen()
        {
        boolean sendMIDI = getSendMIDI();
        setSendMIDI(true);
                                
        // Then Vyzex seems to send the venom the following undocumented commands to switch the internal state to "Multi"

        byte[] data = new byte[13];     // I think this command does the Multi switch
        data[0] = (byte)0xF0;
        data[1] = (byte)0x00;
        data[2] = (byte)0x01;
        data[3] = (byte)0x05;
        data[4] = (byte)0x21;
        data[5] = (byte)DEFAULT_ID; //(byte)getID();
        data[6] = (byte)0x02;           // "Write Dump" What???  Really???
        data[7] = (byte)0x15;           // UNDOCUMENTED LOCATION
        data[8] = (byte)0x00;           // UNDOCUMENTED LOCATION
        data[9] = (byte)0x01;           // UNDOCUMENTED LOCATION
        data[10] = (byte)0x00;  
        data[11] = (byte)0x01;          // <-- This appears to mean "Multi" rather than "Single"
        data[12] = (byte)0xF7;
        tryToSendSysex(data);

        data = new byte[13];            // I don't know what this
        data[0] = (byte)0xF0;
        data[1] = (byte)0x00;
        data[2] = (byte)0x01;
        data[3] = (byte)0x05;
        data[4] = (byte)0x21;
        data[5] = (byte)DEFAULT_ID; //(byte)getID();
        data[6] = (byte)0x02;           // "Write Dump" What???  Really???
        data[7] = (byte)0x15;           // UNDOCUMENTED LOCATION
        data[8] = (byte)0x00;           // UNDOCUMENTED LOCATION
        data[9] = (byte)0x02;           // UNDOCUMENTED LOCATION
        data[10] = (byte)0x00;  
        data[11] = (byte)0x00;
        data[12] = (byte)0xF7;
        tryToSendSysex(data);
        
        setSendMIDI(sendMIDI);
        }
        
    public void changePatch(Model tempModel)
        {
        int bank = tempModel.get("bank", 0);
        int number = tempModel.get("number", 0);

        // Vyzex seems to send to the venom the following undocumented command to change the patch:

        byte[] data = new byte[13];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x00;
        data[2] = (byte)0x01;
        data[3] = (byte)0x05;
        data[4] = (byte)0x21;
        data[5] = (byte)DEFAULT_ID; //(byte)getID();
        data[6] = (byte)0x02;           // "Write Dump" What???  Really???
        data[7] = (byte)0x15;           // UNDOCUMENTED LOCATION
        data[8] = (byte)0x00;           // UNDOCUMENTED LOCATION
        data[9] = (byte)0x0D;           // UNDOCUMENTED LOCATION
        data[10] = (byte)(bank + 0x40); 
        data[11] = (byte)number;
        data[12] = (byte)0xF7;
        tryToSendSysex(data);
                
        switchScreen();
        }

    public void performRequestDump(Model tempModel, boolean changePatch)
        {
        // The venom has the ability to load a specific patch.  However its multi sysex seems
        // to be broken -- invalid data is returned when you request a patch.  But it's fine
        // if you request the current patch.
        //
        // So for the time being, we're going to do a requestCurrentDump instead of
        // a requestDump.  This means we must always do a changePatch regardless,
        // so that's why we're overriding this method.
        // we are always changing patches because we have to do a request current patch,
        
        performChangePatch(tempModel);
        tryToSendSysex(requestCurrentDump());
        model.set("number", tempModel.get("number"));
        model.set("bank", tempModel.get("bank"));
        }

    /// This switches the screen when we send to a curent dump but don't change the patch
    public boolean sendAllParametersInternal()
        {
        boolean val = super.sendAllParametersInternal();
        if (val)
            {
            switchScreen();
            }
        return val;
        }


    public byte[] requestCurrentDump()
        {
        byte[] data = new byte[11];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x00;
        data[2] = (byte)0x01;
        data[3] = (byte)0x05;
        data[4] = (byte)0x21;
        data[5] = (byte)DEFAULT_ID; //(byte)getID();
        data[6] = (byte)0x01;           // data dump request
        data[7] = (byte)0x00;           // current buffer
        data[8] = (byte)0x02;           // multi patch
        data[9] = (byte)0x00;           // doesn't matter
        data[10] = (byte)0xF7;
        return data;
        }

    public int getPauseAfterChangePatch() { return 500; }                               // quite a long time
    public int getPauseAfterSendAllParameters() { return 750; }

    // This is how you'd request a patch, but we're not using it because we have
    // overridden performRequestDump above.
    public byte[] requestDump(Model tempModel)
        {
        if (tempModel == null)
            tempModel = getModel();

        int NN = tempModel.get("number", 0);                    // The numbers are 0...127 but...
        int BB = tempModel.get("bank", 0) + 1;                  // Believe it or not, the banks are literally 1...4
        
        byte[] data = new byte[11];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x00;
        data[2] = (byte)0x01;
        data[3] = (byte)0x05;
        data[4] = (byte)0x21;
        data[5] = (byte)DEFAULT_ID; //(byte)getID();
        data[6] = (byte)0x01;           // data dump request
        data[7] = (byte)0x02;           // multi patch
        data[8] = (byte)BB;     
        data[9] = (byte)NN;
        data[10] = (byte)0xF7;
        return data;
        }

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
        
        String nm = model.get("name", "Untitled");
        String newnm = revisePatchName(nm);
        if (!nm.equals(newnm))
            model.set("name", newnm);
        }
    
    public static String getSynthName() { return "M-Audio Venom [Multi]"; }
    public String getPatchName(Model model) { return model.get("name", "Untitled"); }
    
    public Model getNextPatchLocation(Model model)
        {
        int number = model.get("number");
        int bank = model.get("bank");
        
        number++;
        if (number >= 128)
            {
            number = 0;
            bank++;
            if (bank >= 2)
                bank = 0;
            }
                
        Model newModel = buildModel();
        newModel.set("number", number);
        newModel.set("bank", bank);
        return newModel;
        }

    public String getPatchLocationName(Model model)
        {
        // getPatchLocationName() is called from sprout() as a test to see if we should enable
        // batch downloading.  If we haven't yet created an .init file, then parameters won't exist
        // yet and this method will bomb badly.  So we return null in this case.
        if (!model.exists("number")) return null;
        
        int number = (model.get("number"));
        int bank = (model.get("bank"));
        return BANKS[bank] + " " + ((number > 99 ? "" : (number > 9 ? "0" : "00")) + number);
        }

    /** Map of parameter -> index in the allParameters array. */
    static HashMap parametersToIndex = null;


    /** List of all M-audio Venom multi parameters in order. */
                
    final static String[] parameters = new String[]
    {
    "part1source",
    "part2source",
    "part3source",
    "part4source",
    "aux1source",
    "aux2source",
    "mastersource",
    "-",
    "-",
    "part1enable",
    "part2enable",
    "part3enable",
    "part4enable",
    "part1bank",
    "part1program",
    "part1coarsetune",
    "part1finetune",
    "part1voicemode",
    "part1unisonmode",
    "part1unisoncount",
    "part1unisondetune",
    "part1volume",
    "part1pan",
    "part1direct",
    "part1aux1send",
    "part1aux2send",
    "part1fxtype",
    "part1channel",
    "part1keylow",
    "part1keyhigh",
    "part1vellow",
    "part1velhigh",
    "part1controlenable",
    "part2bank",
    "part2program",
    "part2coarsetune",
    "part2finetune",
    "part2voicemode",
    "part2unisonmode",
    "part2unisoncount",
    "part2unisondetune",
    "part2volume",
    "part2pan",
    "part2direct",
    "part2aux1send",
    "part2aux2send",
    "part2fxtype",
    "part2channel",
    "part2keylow",
    "part2keyhigh",
    "part2vellow",
    "part2velhigh",
    "part2controlenable",
    "part3bank",
    "part3program",
    "part3coarsetune",
    "part3finetune",
    "part3voicemode",
    "part3unisonmode",
    "part3unisoncount",
    "part3unisondetune",
    "part3volume",
    "part3pan",
    "part3direct",
    "part3aux1send",
    "part3aux2send",
    "part3fxtype",
    "part3channel",
    "part3keylow",
    "part3keyhigh",
    "part3vellow",
    "part3velhigh",
    "part3controlenable",
    "part4bank",
    "part4program",
    "part4coarsetune",
    "part4finetune",
    "part4voicemode",
    "part4unisonmode",
    "part4unisoncount",
    "part4unisondetune",
    "part4volume",
    "part4pan",
    "part4direct",
    "part4aux1send",
    "part4aux2send",
    "part4fxtype",
    "part4channel",
    "part4keylow",
    "part4keyhigh",
    "part4vellow",
    "part4velhigh",
    "part4controlenable",
    "aux1mode",
    "aux1type",
    "aux1depth",
    "aux1prehp",
    "aux1predelay",
    "aux1highdamp",
    "aux1time",
    "aux1feedback",
    "aux1time",
    "aux1gatethresh",
    "aux1tonegain",
    "aux1tonefrequency",
    "aux2mode",
    "aux2type",
    "aux2depth",
    "aux2toaux1",
    "aux2prehp",
    "aux2prelp",
    "aux2time",
    "aux2feedback",
    "aux2highdamp",
    "aux2lforate",
    "aux2lfodepth",
    "progvolume",
    "mastereqlowfreq",
    "mastereqlowgain",
    "mastereqmidfreq",
    "mastereqmidgain",
    "mastereqhighfreq",
    "mastereqhighgain",
    "part1arpenable",
    "part1arpsource",
    "part1arpbank",
    "part1arppattern",
    "part1arpmode",
    "part1arpnoteorder",
    "part1arpoctaverange",
    "part1arpbipolar",
    "part1arplatchkeys",
    "part1arprootnote",
    "part2arpenable",
    "part2arpsource",
    "part2arpbank",
    "part2arppattern",
    "part2arpmode",
    "part2arpnoteorder",
    "part2arpoctaverange",
    "part2arpbipolar",
    "part2arplatchkeys",
    "part2arprootnote",
    "part3arpenable",
    "part3arpsource",
    "part3arpbank",
    "part3arppattern",
    "part3arpmode",
    "part3arpnoteorder",
    "part3arpoctaverange",
    "part3arpbipolar",
    "part3arplatchkeys",
    "part3arprootnote",
    "part4arpenable",
    "part4arpsource",
    "part4arpbank",
    "part4arppattern",
    "part4arpmode",
    "part4arpnoteorder",
    "part4arpoctaverange",
    "part4arpbipolar",
    "part4arplatchkeys",
    "part4arprootnote",
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

    public static final String[][] DEFAULT_SINGLE_PATCH_NAMES = new String[][]
    {
    { "Venomous", "reSEMble", "Enophile", "HoldNSolo", "2 Much Saw", "Warm Lead", "Dyna Perk", "PPGee", "LFO Freak", "oh8 to oh9", "KnarzBass", "Bass JaX", "SawBass", "C Hard Bas", "TheRare SH", "BigBadBob", "Silverfish", "Sink Bass", "PlanetHoov", "doombass", "psyKnokBaS", "Monolith", "Hysteria", "Hertz Volt", "RunMeBass", "Uncle AL", "GreyMeanie", "WormRider", "Big Pulse", "WoW WoW", "Ugly Mess", "Bit Solo", "Fatality", "UltraNasty", "Voll Fuzz", "FiniteElem", "agttht", "8BIT Tremo", "Glass Harp", "halfWatt", "Pomp Lead", "Noise Lead", "Picardy", "Ralf Lead", "NRVusSAWz", "Whistler", "feedback", "Bumble B", "Rusty Lead", "Thin Line", "anGlzWingz", "Reso Perk", "SyncReleas", "TurnItOn", "accension", "fmgrit", "Wonderous", "NervousBee", "Jalisco", "FerisWheel", "Catbox", "ClassicV", "Bryt Bits", "GlassPiano", "EuroRacked", "Waveshaper", "Tesla Clav", "Ultra Bell", "Mortality", "Ana Piano", "Grain Pad", "MaudDib", "Big Sweep", "Quickening", "70sStrings", "Werkstate", "Reversed", "Spiralis", "breakuppad", "S&H Bed", "Gentleman", "Currents", "Spacey", "Sprinkler", "mellowtorn", "Shivers", "Glassical", "Oxidizers", "SoftSquare", "Cnidaria", "dialTune", "Klangons", "Gong Show", "Shattered", "abbynormal", "Insectoid", "swallow", "TimeStop", "WorldsEnd", "Skreps", "Starzzy", "LFO DNA", "TransMissn", "Teletubby", "Jellyfish", "Spectre", "Gibberish", "I Feel NZ", "PrimoTech", "Werms", "HalluciKit", "Glostix", "BoomSlang", "PunchDrunk", "Downgrade", "KITChen", "DeadlyToad", "Attack 9", "Attack 55", "Corruptor", "MicToComp", "MicToWah", "MicToDist", "MicToBit", "MicToDeci", "MicToDelay", "Mic Input", "Inst Input", }, 
    { "Devilish", "2600 Saw", "Fractalize", "LFO Flow", "SmotLead", "2 MuchNoiz", "Factors", "Simple J", "Brothers", "FM Attack", "LFO Bass", "AutoDub", "3DBass", "phatBSTRD", "Thick Saw", "BriarPatch", "TB Shot", "XLR8R Bass", "Multiplied", "fmfacebass", "Plunky", "OneBig OSC", "Liquid Bass", "Holo Base", "sawzallBS", "Sine Thump", "RoundSound", "ModelBass", "Density", "RezSquared", "bumBUMbump", "Singee", "Monopoly", "NastyLead", "Straight", "Atari", "RiffLead", "Sizzlelead", "thUUwaRp", "Twinkler", "PsychoCity", "Tinker", "Reso BITz", "M-saw Lead", "Nu Form", "VintageSaw", "UniBombR", "BrainLaser", "Weerd Sync", "AttackLead", "distlulaby", "AM Bell", "Newts", "Dyno Sync", "Nocturnal", "Paris", "Wurlesque", "Disintegr8", "FiltR Clav", "SDivisions", "AnalogSeq", "morphclav", "fake FM", "Vibe-R-8", "Plinky", "Polystab", "Grinder", "Plucky One", "Far Away", "Big Profit", "Mysterio", "TimeLess", "bwahahaha", "Ecosystems", "hi sweep", "Nayarit", "Racers", "Sonora", "Feed Me", "Inventions", "Trigger", "Power Saw", "ThruThEthr", "Coma", "Vittles", "Reactivate", "Octavide", "Tremo Pad", "LFO Soup", "Serum", "cutABitRug", "Destructor", "merganzer", "FMEels", "8BIT Alarm", "Randomly", "The Whoosh", "Theatrical", "Poor Me", "MoonRabbit", "Rotorvate", "DiodeDrops", "Bouncer", "Tricorder", "Balloon", "conVers8n", "Chopper", "CrispyTop", "FrozenGras", "Critters", "CH2SingKit", "ROMcheck", "MondoDrum1", "Sewer Drum", "Bite Kit", "DelayReact", "SyncFM Kit", "Straight 8", "Strict 9", "Chameleon", "InstToComp", "InstToWah", "InstToDist", "InstToBit", "InstToDeci", "InstToDelay", "AuxL Input", "AuxR Input", }, 
    { "1978 Bass", "Bass Mode", "NeutralizR", "LFO Mashup", "Chip Comp", "MoonLead", "Venom Sync", "FM Fry", "geology", "Bent oh8", "Taurean", "DCO Bass", "DubStep", "fake ebass", "Nasal Bass", "FurryPurry", "Acid TaB", "techBass", "Paper Bass", "Weebel", "Physical", "WetWetBass", "Python", "Mighty MG", "Warehouse", "DeciBass", "Basilisk", "SphereBass", "OBer Bass", "mOBius SQR", "Harvested", "5th Lead", "Telemetry", "Fizzy Lead", "Blister", "Coiled", "Amphibians", "eQuations", "squalkbox", "thirds", "Bliss Bell", "BluesHarp", "Tuvan Lead", "nice hat", "Psaltery", "Thlead", "WakaJawaka", "Cassini", "Game Over", "Mondo", "Dilate", "ghostbell", "Viridis", "Paisley", "MK Venom", "Planets", "DreamBells", "ourMIhrpsi", "softHammer", "Dirty Sink", "Drawbs", "Malletica", "N-Sonic", "fat horns", "Stucco", "PingSeq", "detuneClav", "Squishy", "ChnReactn", "Pianica", "LineOSight", "grime pad", "SawPad", "Territory", "MorningFog", "Salamander", "Old Times", "Membranes", "Slate", "LifeCycles", "Score Pad", "Lineages", "Startup", "Dissolver", "Sea Chelss", "Molecules", "Spookier", "Neurotoxin", "B O C PAD", "Oblique", "PowerTools", "chimeTimes", "Concentr8", "robopop", "Converge", "Gyrations", "SheprdTone", "Unknown", "bentbroke", "dirtmod", "grainnoise", "Spasms", "submerged", "worstsiren", "Cartoons", "ERROR?", "spAceMouNt", "Internot", "Descendnt", "Flux U Ate", "Assault808", "Toi Kit", "Terror Kit", "Bonebreak", "MondoDrum2", "FactoryDRM", "PercolateD", "StraightFM", "Straight55", "RingModKit", "AuxToComp", "AuxToWah", "AuxToDist", "AuxToBit", "AuxToDeci", "AuxToDelay", "AuxLRInput", "INIT", }, 
    { "Prolectro", "BlueMarvin", "Flutterby", "Ebb & Flow", "Mosquito", "Pro-D-G", "Nightly", "DrippinOut", "Asteroids", "plasterDRM", "UnisonBass", "HunterBass", "HeavyB", "KaizoBass", "Hypodermic", "RubberBass", "Silver SQR", "tsunamiBS", "Earthshake", "Bit Acidic", "bassment", "Resorama", "Ceramic", "AnaGoodnes", "shakespear", "Injected", "SH SH SH", "sync bass", "SampNgold", "BeThereOrB", "Sleepwalk", "Chitlins", "Dirty Bird", "doLEADnar", "StrangLead", "Dance SQ", "FunToPlay", "IsIt Sync?", "Prog-Prog", "TheClassic", "Raw Square", "Weenerdog", "Bit Lead", "HornetNest", "Kheon", "RingLead", "Woop", "rightring", "uglymother", "Rave", "SouperSaw", "glasstear", "Fossa", "ClaveKey", "Organ MW", "JX Sqr Seq", "mallets", "dreamer", "TremoClav", "Confusist", "Patter", "RunnyNose", "BurninUp", "SpiritBowl", "BannedJoe", "prettykeys", "tense", "Hypoxia", "55gallons", "Suitcase", "Sweeper", "Bit Bed", "Nice+Wide", "Lethargy", "Pulsations", "RingWorlds", "DOSBassPad", "Mythic", "FuzzyTail", "AdaptEvolv", "Amber", "Floodplain", "LFO Games", "Copperhead", "AliasPad", "Oceania", "Jade", "Sidewinder", "S+H World", "Specimens", "INSECTS", "Percolate", "whipper", "robofunk", "ErrorThis", "Modders", "TileFX", "Driftage", "EndOfSideB", "GhostYards", "Sandworm", "PlasmaDriv", "SeaWolf", "TunedIn", "Vacuous", "Wobbletron", "Droplets", "disorientd", "SlayDay", "Organisms", "FRIED FM", "VeloDistor", "Lizard Kit", "Slappy Kit", "Warblekit", "Apocalypse", "alienated", "bendybones", "Attack 8", "Morph Kit", "Mic To LPF", "Mic To HPF", "InstTo LPF", "InstTo HPF", "Aux To LPF", "Aux To HPF", "USB To LPF", "USB To HPF", }, 
    };
    
    /*
      public static final String[][] DEFAULT_MULTI_PATCH_NAMES = new String[][]
      {
      { "Stingray", "Diamonds", "Vertigo", "Creeper", "Abscess", "Timber", "Renegade", "Ambush", "Durango", "Absorbed", "Stomping", "CottonMath", "Readout", "Mojave", "Scorpion", "Kraits", "Bazaar", "Variance", "Solution", "Nematocyst", "Laceration", "Mamba Gruv", "Nerve Ends", "Dense", "Sentient", "Enzymes", "Serpentine", "Rio Grande", "Predator", "Skeletons", "Saliva", "Spines", "Dragon", "ConstrictR", "Machin8", "Vortex", "Volcanic", "Dwelling", "Fuel", "Dosage", "Elusive", "Arboreal", "Oviducts", "Puncture", "Chemicals", "Devoid", "Galapagos", "Recognize", "Slither", "Tlaxcala", "Distance", "Abundance", "Immune", "Microbes", "RikkiTikki", "Vascular", "Toxins", "Captivate", "2HapyFingr", "Scattered", "Wanderer", "Native", "Molting", "Digi Frog", "Cnidaria", "Eardrums", "BeatBlastR", "Vertebr8s", "Contagious", "MitoKndria", "PadLayer01", "PadLayer02", "PadLayer03", "PadLayer04", "PadLayer05", "PadLayer06", "PadLayer07", "PadLayer08", "PadLayer09", "PadLayer10", "ArpLayer01", "ArpLayer02", "ArpLayer03", "ArpLayer04", "ArpLayer05", "ArpLayer06", "ArpLayer07", "ArpLayer08", "ArpLayer09", "ArpLayer10", "MixLayer01", "MixLayer02", "MixLayer03", "BasLayer01", "BasLayer02", "BasLayer03", "BasLayer04", "BasLayer05", "BasLayer06", "BasLayer07", "VeloArps01", "VeloArps02", "VeloArps03", "VeloArps04", "VeloArps05", "VeloArps06", "VeloArps07", "VeloArps08", "VeloArps09", "VeloArps10", "KeySplit01", "KeySplit02", "KeySplit03", "KeySplit04", "KeySplit05", "KeySplit06", "KeySplit07", "KeySplit08", "KeySplit09", "KeySplit10", "KeyLayer01", "KeyLayer02", "KeyLayer03", "KeyLayer04", "KeyLayer05", "KeyLayer06", "KeyLayer07", "Demo Multi", }, 
      { "Pit Viper", "Carapace", "Metabolize", "Ensnared", "Protozoa", "Relentless", "Komodo", "Digestion", "Synaptic", "Squamata", "Mangroove", "Nuclear", "Creature", "Monsoon", "Corrosive", "Bio Logic", "Inhabited", "Potency", "Spiders", "Radioactiv", "Steroids", "Hatchlings", "Alveoli", "Catalyst", "Panic", "Viscera", "Shelter", "Puebla", "Monster", "Harlequin", "Amnesia", "Carnivore", "Physiology", "Tremors", "Ventricle", "Tourniquet", "Muscular", "Latex", "Ooze", "Polluted", "UndRgrowth", "Reptiles", "Connectiv", "Villains", "Crawler", "Mutagen", "Interferon", "Paradigm", "Quarantine", "Tribe", "Rainforest", "Maxilla", "Arachnid", "Locomotion", "Primitive", "Symbiote", "Visions", "Embryo", "Hypothesis", "Irritant", "Secretions", "Cobras", "Arteries", "Scenarios", "Symptoms", "Naja", "Gecko", "Krotalon", "Lair", "Organism", "PadLayer11", "PadLayer12", "PadLayer13", "PadLayer14", "PadLayer15", "PadLayer16", "PadLayer17", "PadLayer18", "PadLayer19", "PadLayer20", "ArpLayer11", "ArpLayer12", "ArpLayer13", "ArpLayer14", "ArpLayer15", "ArpLayer16", "ArpLayer17", "ArpLayer18", "ArpLayer19", "ArpLayer20", "MixLayer04", "MixLayer05", "MixLayer06", "BasLayer08", "BasLayer09", "BasLayer10", "BasLayer11", "BasLayer12", "BasLayer13", "BasLayer14", "VeloArps11", "VeloArps12", "VeloArps13", "VeloArps14", "VeloArps15", "VeloArps16", "VeloArps17", "VeloArps18", "VeloArps19", "VeloArps20", "KeySplit11", "KeySplit12", "KeySplit13", "KeySplit14", "KeySplit15", "KeySplit16", "KeySplit17", "KeySplit18", "KeySplit19", "KeySplit20", "KeyLayer08", "KeyLayer09", "KeyLayer10", "KeyLayer11", "KeyLayer12", "KeyLayer13", "KeyLayer14", "Multitmbrl", }, 
      };
    */

    public static final  String[][] DEFAULT_ARP_PATTERN_NAMES = new String[][]
    {
    { "Dance Club", "Warming Up", "Solid Gruv", "LaynItDown", "BreakItUp", "Fun-kay", "GruvHound", "StreetWise", "Vampin’", "New Hats", "GetInGear", "Many Hands", "Clave City", "Hip Hip", "Everything", "Rollin’", "Classic", "Tight Enuf", "TwoFisted", "U know it", "SleuthClik", "Reminds Me", "MorCowbell", "On The Rim", "Low-n-slow", "RnB ballad", "WaitingFor", "TalkItOver", "Callback", "TurnAround", "UR Famous", "1stRespons", "Hip Click", "YerBasicBt", "Bommm", "Move Along", "Three Seas", "RollWithIt", "InTheAlley", "Skip It", "Baby Me", "And Now---", "3za Crowd", "3sqrChurch", "Traces Of-", "RelyOnMe", "EbbNFlow", "MeltingPot", "QuickClub", "TriplePark", "Octavator", "Chance It", "FrmTheEast", "Lazy Bass", "Offbeat", "RightWithU", "Lockstep", "Dovetail", "Skipper", "Psycho", "Ponger", "DownDirty", "Thruster", "Homeboy", "Nite Fever", "SwinginHrd", "OneOfAKind", "WaitForIt", "Roll a Six", "Invader", "Driver", "Poppy", "Sneaky", "Pensive", "HammerHnd", "Careful", "Ubiquitous", "ThinkAbout", "Bizness", "Interim", "Attention", "Strummer", "Mowdown", "TyrinNotTo", "BackMeUp", "Latismo", "Relentless", "FastTalker", "DrillNfill", "Blistering", "Slow Jump", "Fast Jump", "Beeper", "Tension", "OneFourAll", "Minor Hit", "Jump Over", "Downward", "OverTheTop", "Angelic", "Two bar L", "One bar L", "Half bar L", "Quarter L", "Q-trip L", "8th L", "8-trip L", "16th L", "16-trip L", "32nd L", "Offbeat Q", "Q-dot 1", "Q-dot 2", "Q-dot 3", "8-dot 1", "8-dot 2", "8-dot 3", "8-dot 4", "16 swing 1", "16 swing 2", "16-dot 1", "16-dot 2", "16-dot 3", "16-dot 4", "Offbeat 16", "Session", "Stepper", "GotDaBenz", }, 
    { "Bounce", "Metropolis", "Percolate", "Robotheque", "GottaDance", "HipCricket", "Tradeoff", "MadeTheCut", "Drop Out", "InThPocket", "HoldSteady", "oh8-n-see", "Restart", "Coastin’", "PlastiPerc", "oh8daGreat", "Shufflin’", "Sambalicis", "ClappTrapp", "Happnin", "WooferTest", "Congregate", "Take Over", "Refocus", "RockinRide", "Thermometr", "HotPotato", "Contrasts", "Undertow", "StandClear", "Mid Scheme", "Back Hat", "Determin8n", "Hipalong", "KickTheCow", "Misfire", "Drop Kick", "Gypsy Clap", "BackNForth", "ClaveMastr", "Boot Scoot", "Power Of 3", "Nativitan", "AllYouNeed", "TriplThret", "Euro Club", "ThePunches", "PrettyCool", "MixinMatch", "MissingMan", "OneNFive", "OneNSeven", "PrettyHary", "Dozen Off", "BustedMain", "Assertive", "MattrOFact", "Omination", "BlackCoptr", "TooMuch4U", "DigginHard", "Radical", "16 Stage", "YouTakeIt", "JmaicaThis", "WhichCntry", "HalfWhole", "ZunkItUp", "TurtleSix", "SixShooter", "OctoFunk", "Malevolent", "Poly Fill", "Royalty", "MellowOut", "Contempl8", "3rdWorldly", "Backlash", "Nine Drop", "Climbin 9", "Ripper", "Funkworthy", "Tremulous", "ShufflWalk", "FunkyBend", "Finish ‘em", "Flame-n-Co", "Stutterfly", "Overwhelm", "Outpost", "ShuffleOff", "Paralyzer", "Provocativ", "Funky fill", "Dangerous", "Pressurize", "Recursive", "Off Kilter", "Plink", "Retrospect", "Two bar S", "One bar S", "Half bar S", "Quarter S", "Q-trip S", "8th S", "8-trip S", "16th S", "16-trip S", "32nd S", "Offbeat H", "Syncop8ion", "Q-LaidBack", "Q-FulSwing", "8-LaidBack", "8 swing 1", "8 swing 2", "8 swing 3", "16 swing 3", "HausToHaus", "Pluck 1", "Pluck 2", "Pluck 3", "Diggin In", "Climber", "Toads", "AspirinPlz", "BendUrWill", }, 
    };

    public String[] getBankNames() { return BANKS; }

	/** Return a list of all patch number names.  Default is { "Main" } */
	public String[] getPatchNumberNames()  { return buildIntegerNames(128, 0); }

	/** Return a list whether patches in banks are writeable.  Default is { false } */
	public boolean[] getWriteableBanks() { return new boolean[] { false, true }; }

	/** Return a list whether individual patches can be written.  Default is FALSE. */
	public boolean getSupportsPatchWrites() { return true; }

	public int getPatchNameLength() { return 10; }

    }
