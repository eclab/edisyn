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
   A patch editor for the Kawai K4/K4r [Multi mode].
        
   @author Sean Luke
*/

public class KawaiK4Multi extends Synth
    {
    /// Various collections of parameter names for pop-up menus
        
    public static final String[] BANKS = { "A", "B", "C", "D", "Ext. A", "Ext. B", "Ext. C", "Ext. D" };
    public static final String[] BANKS_SHORT = { "A", "B", "C", "D" };
    public static final String[] KEYS = new String[] { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
    public static final String[] VELOCITY_SWITCHES = { "All", "Soft", "Loud" };
    public static final String[] PLAY_MODES = { "Keyboard", "MIDI", "Both" };
    public static final String[] SUBMIX_CHANNELS = { "A", "B", "C", "D", "E", "F", "G", "H" };

    public JFrame sprout()
        {
        JFrame frame = super.sprout();
        // We can't request the current working memory (don't ask why)
        receiveCurrent.setEnabled(false);
        transmitParameters.setEnabled(false);
        transmitParameters.setSelected(false);        
        return frame;
        }         

    public KawaiK4Multi()
        {
        for(int i = 0; i < allParameters.length; i++)
            {
            allParametersToIndex.put(allParameters[i], Integer.valueOf(i));
            }
                        
        /// SOUND PANEL
                
        JComponent soundPanel = new SynthPanel(this);
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        hbox.addLast(addGlobal(Style.COLOR_B()));
        vbox.add(hbox);
        
        vbox.add(addSection(1, Style.COLOR_A()));
        vbox.add(addSection(2, Style.COLOR_A()));

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Global, Sections 1-2", soundPanel);
                

        SynthPanel sourcePanel = new SynthPanel(this);
        vbox = new VBox();
        
        vbox.add(addSection(3, Style.COLOR_A()));
        vbox.add(addSection(4, Style.COLOR_A()));
        vbox.add(addSection(5, Style.COLOR_A()));
            
        sourcePanel.add(vbox, BorderLayout.CENTER);
        addTab("Sections 3-5", sourcePanel);

        sourcePanel = new SynthPanel(this);
        vbox = new VBox();
        
        vbox.add(addSection(6, Style.COLOR_A()));
        vbox.add(addSection(7, Style.COLOR_A()));
        vbox.add(addSection(8, Style.COLOR_A()));

        sourcePanel.add(vbox, BorderLayout.CENTER);
        addTab("Sections 6-8", sourcePanel);
        
        model.set("name", "Init Patch");  // has to be 10 long
        
        model.set("number", 0);
        model.set("bank", 0);

        loadDefaults();        
        }
                
    public String getDefaultResourceFileName() { return "KawaiK4Multi.init"; }
    public String getHTMLResourceFileName() { return "KawaiK4Multi.html"; }

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
        comp = new PatchDisplay(this, 4);
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

        // Not enough space to show the title
        hbox.addLast(Strut.makeHorizontalStrut(60));

        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }


    public JComponent addGlobal( Color color)
        {
        Category category = new Category(this, "Global", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        comp = new LabelledDial("Volume", this, "volume", color, 0, 100);
        hbox.add(comp);

        comp = new LabelledDial("Effect [K4]", this, "effect", color, 0, 31, -1);
        ((LabelledDial)comp).addAdditionalLabel("Output [K4r]");
        model.removeMetricMinMax("effect");
        hbox.add(comp);


        VBox vbox = new VBox();
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
                                tempModel.set("number", KawaiK4Multi.this.model.get("effect"));
                                tempModel.set("bank", KawaiK4Multi.this.model.get("bank"));
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



    public JComponent addSection(final int src, Color color)
        {
        Category category = new Category(this, "Section " + src, color);
        category.makePasteable("section" + src);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = VELOCITY_SWITCHES;
        comp = new Chooser("Velocity Switch", this, "section" + src + "velocitysw", params);
        vbox.add(comp);

        params = PLAY_MODES;
        comp = new Chooser("Play Mode [K4]", this, "section" + src + "playmode", params);
        vbox.add(comp);
        hbox.add(vbox);
        
        vbox = new VBox();
        HBox hbox2 = new HBox();
        comp = new PushButton("Show")
            {
            public void perform()
                {
                final KawaiK4 synth = new KawaiK4();
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
                                tempModel.set("bank", KawaiK4Multi.this.model.get("section" + src + "bank"));
                                tempModel.set("number", KawaiK4Multi.this.model.get("section" + src + "number"));
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
        hbox2.addLast(comp);
        vbox.add(hbox2);

        // Normally this is in global, but I think it makes more sense here
        comp = new CheckBox("Mute", this, "section" + src + "mute");
        vbox.add(comp);

        hbox.add(vbox);


        hbox.add(vbox);
                
                
                
                                
        comp = new LabelledDial("Bank", this, "section" + src + "bank", color, 0, 3)
            {
            public String map(int val)
                {
                // I believe that you can only refer to patches in your own memory region
                return BANKS_SHORT[val];
                }
            };
        model.removeMetricMinMax("section" + src + "bank");
        hbox.add(comp);
        
        comp = new LabelledDial("Number", this, "section" + src + "number", color, 0, 16, -1);
        model.removeMetricMinMax("section" + src + "number");
        hbox.add(comp);

        comp = new LabelledDial("Lowest", this, "section" + src + "lowkey", color, 0, 127)
            {
            public String map(int val)
                {
                return KEYS[val % 12] + (val / 12 - 2);  // note integer division
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Key");
        hbox.add(comp);

        comp = new LabelledDial("Highest", this, "section" + src + "highkey", color, 0, 127)
            {
            public String map(int val)
                {
                return KEYS[val % 12] + (val / 12 - 2);  // note integer division
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Key");
        hbox.add(comp);

        comp = new LabelledDial("Receive", this, "section" + src + "channel", color, 0, 15, -1);
        ((LabelledDial)comp).addAdditionalLabel("Channel");
        model.removeMetricMinMax("section" + src + "channel");
        hbox.add(comp);

        comp = new LabelledDial("Submix", this, "section" + src + "submix", color, 0, 7)
            {
            public String map(int val)
                {
                return SUBMIX_CHANNELS[val];
                }
            };
        model.removeMetricMinMax("section" + src + "submix");
        hbox.add(comp);

        comp = new LabelledDial("Level", this, "section" + src + "level", color, 0, 100);
        hbox.add(comp);

        comp = new LabelledDial("Transpose", this, "section" + src + "transpose", color, 0, 48, 24)
            {
            public boolean isSymmetric() { return true; }
            };
        hbox.add(comp);

        comp = new LabelledDial("Tune", this, "section" + src + "tune", color, 0, 100, 50);
        hbox.add(comp);


        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    HashMap allParametersToIndex = new HashMap();
        
    final static String[] allParameters = new String[]
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
                
    "section1singleno",                     // *
    "section1lowkey",
    "section1highkey",
    "section1rcvch_velosw_mute",    // *
    "section1mode_outselect",       //*
    "section1level",
    "section1transpose",
    "section1tune",
                
    "section2singleno",                     // *
    "section2lowkey",
    "section2highkey",
    "section2rcvch_velosw_mute",    // *
    "section2mode_outselect",       //*
    "section2level",
    "section2transpose",
    "section2tune",

    "section3singleno",                     // *
    "section3lowkey",
    "section3highkey",
    "section3rcvch_velosw_mute",    // *
    "section3mode_outselect",       //*
    "section3level",
    "section3transpose",
    "section3tune",

    "section4singleno",                     // *
    "section4lowkey",
    "section4highkey",
    "section4rcvch_velosw_mute",    // *
    "section4mode_outselect",       //*
    "section4level",
    "section4transpose",
    "section4tune",

    "section5singleno",                     // *
    "section5lowkey",
    "section5highkey",
    "section5rcvch_velosw_mute",    // *
    "section5mode_outselect",       //*
    "section5level",
    "section5transpose",
    "section5tune",

    "section6singleno",                     // *
    "section6lowkey",
    "section6highkey",
    "section6rcvch_velosw_mute",    // *
    "section6mode_outselect",       //*
    "section6level",
    "section6transpose",
    "section6tune",

    "section7singleno",                     // *
    "section7lowkey",
    "section7highkey",
    "section7rcvch_velosw_mute",    // *
    "section7mode_outselect",       //*
    "section7level",
    "section7transpose",
    "section7tune",

    "section8singleno",                     // *
    "section8lowkey",
    "section8highkey",
    "section8rcvch_velosw_mute",    // *
    "section8mode_outselect",       //*
    "section8level",
    "section8transpose",
    "section8tune"
                
    };




    public int parse(byte[] data, boolean fromFile)
        {
        if (data[3] == (byte)0x20) // single
            {
            model.set("bank", ((data[7] - 64) / 16) + (data[6] == 0x00 ? 0 : 4));
            model.set("number", (data[7] - 64) % 16);
            return subparse(data, 8);
            }
        else
            {
            // extract names
            char[][] names = new char[64][10];
            for(int i = 0; i < 64; i++)
                {
                for (int j = 0; j < 10; j++)
                    {
                    names[i][j] = (char)(data[8 + (i * 77) + j] & 127);
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
            return subparse(data, patchNum * 77 + 8);                                                   
            }
        }
                
    public int subparse(byte[] data, int pos)
        {                    
        byte[] name = new byte[10];

        // The K4 is riddled with byte-mangling.  :-(
        
        for(int i = 0; i < 76; i++)
            {
            int section = (i - 12) / 8 + 1;

            String key = allParameters[i];
                                
            if (i < 10)  // name
                {
                name[i] = data[i + pos];
                }
            else if (key.endsWith("singleno"))
                {
                model.set("section" + section + "bank", data[i + pos] / 16);
                model.set("section" + section + "number", data[i + pos] % 16);
                }
            else if (key.endsWith("rcvch_velosw_mute"))
                {
                model.set("section" + section + "channel", data[i + pos] & 15);
                model.set("section" + section + "velocitysw", (data[i + pos] >>> 4) & 3);
                model.set("section" + section + "mute", (data[i + pos] >>> 6) & 1);
                }
            else if (key.endsWith("mode_outselect"))
                {
                model.set("section" + section + "submix", data[i + pos] & 7);
                model.set("section" + section + "playmode", (data[i + pos] >>> 3) & 3);
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

    public byte[] emit(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        if (tempModel == null)
            tempModel = getModel();

        byte[] data = new byte[76];
    
        String name = model.get("name", "Untitled") + "          ";
        
        // The K4 is riddled with byte-mangling.  :-(
        
        for(int i = 0; i < 76; i++)
            {
            int section = (i - 12) / 8 + 1;
            String key = allParameters[i];
                                
            if (i < 10)  // name
                {
                data[i] = (byte)name.charAt(i);
                }
            else if (key.endsWith("singleno"))
                {
                data[i] = (byte)(model.get("section" + section + "bank") * 16 + model.get("section" + section + "number"));
                }
            else if (key.endsWith("rcvch_velosw_mute"))
                {
                data[i] = (byte)((model.get("section" + section + "mute") << 6) | (model.get("section" + section + "velocitysw") << 4) | (model.get("section" + section + "channel")));
                } 
            else if (key.endsWith("mode_outselect"))
                {
                data[i] = (byte)((model.get("section" + section + "playmode") << 3) | (model.get("section" + section + "submix")));
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
        position = (byte)((tempModel.get("bank") & 3) * 16 + (tempModel.get("number")) + 64);  // 0...63 for A1 .... D16
                        
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
            result[6] = 0x00;       // Error in Section 5-12: missing parameter value (should be 0 for toWorkingMemory)
        else
            result[6] = (byte)(external ? 0x02 : 0x00);
        if (toWorkingMemory)
            result[7] = (byte)(0x40);  // indicates multi.  Error in the manual, it should be 0x000000 not 000x0000
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
        byte position = (byte)((tempModel.get("bank") & 3) * 16 + (tempModel.get("number")) + 64);  // 64 for "multi", that is, 64...127 for A1 .... D16
        return new byte[] { (byte)0xF0, 0x40, (byte)getChannelOut(), 0x00, 0x00, 0x04, 
            (byte)(external ? 0x02 : 0x00),
            position, (byte)0xF7};
        }
                
    public static boolean recognize(byte[] data)
        {
        return ((data.length == EXPECTED_SYSEX_LENGTH &&
                data[0] == (byte)0xF0 &&
                data[1] == (byte)0x40 &&
                data[3] == (byte)0x20 &&
                data[4] == (byte)0x00 &&
                data[5] == (byte)0x04 &&
                (data[6] == (byte)0x00 || data[6] == (byte)0x02) &&
                data[7] >= 64)  // that is, it's multi, not single
            
            || recognizeBulk(data));
        }
        
    public static boolean recognizeBulk(byte[] data)
        {
        return  (
            // Block Multi Data Dump (5-9).
            // We don't do All-Patch data Dump as that would conflict with the K4 Single editor
            
            data.length == 4937 &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x40 &&
            // don't care about 2, it's the channel
            data[3] == (byte)0x21 &&    // block
            data[4] == (byte)0x00 &&
            data[5] == (byte)0x04 &&
            // don't care about 6, we'll use it later
            data[7] == (byte)0x40);  // Multi
        }

    public static final int EXPECTED_SYSEX_LENGTH = 77 + 9;        
    
    
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
        
    public static String getSynthName() { return "Kawai K4/K4r [Multi]"; }
    
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
        int PC = (BB * 16 + NN) + 64;  // 64 for Multi
        try 
            {
            tryToSendMIDI(new ShortMessage(ShortMessage.PROGRAM_CHANGE, getChannelOut(), PC, 0));
            }
        catch (Exception e) { e.printStackTrace(); }
        }

    public int getBulkDownloadWaitTime()
        {
        return 1000;
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
