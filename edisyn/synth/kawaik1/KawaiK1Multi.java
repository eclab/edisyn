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
   A patch editor for the Kawai K1/K1m/K1r [Multi mode].
        
   @author Sean Luke
*/

public class KawaiK1Multi extends Synth
    {
    /// Various collections of parameter names for pop-up menus
        
    // These are the MULTI Banks
    public static final String[] BANKS = { "IA", "IB", "IC", "ID", "EA", "EB", "EC", "ED"};
    
    // These are the SINGLE banks
    public static final String[] INTERNAL_BANKS = { "IA", "IB", "IC", "ID", "iA", "iB", "iC", "iD"};
    public static final String[] EXTERNAL_BANKS = { "EA", "EB", "EC", "ED", "eA", "eB", "eC", "eD"};
    public static final String[] KEYS = new String[] { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
    public static final String[] VELOCITY_SWITCHES = { "All", "Soft", "Loud" };
    public static final String[] PLAY_MODES = { "Keyboard", "MIDI", "Both" };
    public static final String[] OUTPUT = { "Right", "Both", "Left" };

    public JFrame sprout()
        {
        JFrame frame = super.sprout();
        // We can't request the current working memory (don't ask why)
        receiveCurrent.setEnabled(false);
        transmitParameters.setEnabled(false);
        transmitParameters.setSelected(false);        
        return frame;
        }         

    public KawaiK1Multi()
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
        hbox.addLast(addGlobal(Style.COLOR_C()));
        vbox.add(hbox);
        
        vbox.add(addSection(1, Style.COLOR_A()));
        vbox.add(addSection(2, Style.COLOR_B()));

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Global, Sections 1-2", soundPanel);
                

        SynthPanel sourcePanel = new SynthPanel(this);
        vbox = new VBox();
        
        vbox.add(addSection(3, Style.COLOR_A()));
        vbox.add(addSection(4, Style.COLOR_B()));
        vbox.add(addSection(5, Style.COLOR_A()));
            
        sourcePanel.add(vbox, BorderLayout.CENTER);
        addTab("Sections 3-5", sourcePanel);

        sourcePanel = new SynthPanel(this);
        vbox = new VBox();
        
        vbox.add(addSection(6, Style.COLOR_B()));
        vbox.add(addSection(7, Style.COLOR_A()));
        vbox.add(addSection(8, Style.COLOR_B()));

        sourcePanel.add(vbox, BorderLayout.CENTER);
        addTab("Sections 6-8", sourcePanel);
        
        model.set("name", "Init Patch");  // has to be 10 long
        
        model.set("number", 0);
        model.set("bank", 0);

        loadDefaults();        
        }
                
    public String getDefaultResourceFileName() { return "KawaiK1Multi.init"; }
    public String getHTMLResourceFileName() { return "KawaiK1Multi.html"; }

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
        hbox.addLast(Strut.makeHorizontalStrut(110));

        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }


    public JComponent addGlobal( Color color)
        {
        Category category = new Category(this, "Global", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        comp = new LabelledDial("Volume", this, "volume", color, 0, 99, -1);
        hbox.add(comp);

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
        comp = new Chooser("Play Mode [K1]", this, "section" + src + "playmode", params);
        vbox.addBottom(comp);
        hbox.add(vbox);
        
        vbox = new VBox();

        comp = new PushButton("Show")
            {
            public void perform()
                {
                final KawaiK1 synth = new KawaiK1();
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
                                tempModel.set("bank", KawaiK1Multi.this.model.get("section" + src + "bank"));
                                tempModel.set("number", KawaiK1Multi.this.model.get("section" + src + "number"));
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

        params = OUTPUT;
        comp = new Chooser("Output", this, "section" + src + "output", params);
        vbox.addBottom(comp);

        hbox.add(vbox);
                
                
                                
        comp = new LabelledDial("Bank", this, "section" + src + "bank", color, 0, 7)
            {
            public String map(int val)
                {
                int multibank = model.get("bank", 0);
                if (multibank < 4)
                    return INTERNAL_BANKS[val];
                else
                    return EXTERNAL_BANKS[val];
                }
            };
        model.removeMetricMinMax("section" + src + "bank");
        hbox.add(comp);
        
        comp = new LabelledDial("Number", this, "section" + src + "number", color, 0, 7, -1);
        model.removeMetricMinMax("section" + src + "number");
        hbox.add(comp);

        comp = new LabelledDial("Poly", this, "section" + src + "poly", color, 0, 9)
            {
            public String map(int val)
                {
                if (val == 0)
                    return "VR";
                else return "" + (val - 1);
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Level", this, "section" + src + "level", color, 0, 100);
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
        
    // The K1r multimode sysex description is dead wrong.  It states that
    // the parameters are:
    //
    // name1
    // ...
    // name10
    // volume
    //
    // Voice 1 Parameter A
    // Voice 1 Parameter B
    // ...
    // Voice 1 Parameter H
    //
    // Voice 2 Parameter A
    // Voice 2 Parameter B
    // ...
    // Voice 2 Parameter H
    //
    //
    //
    // ... and so on.  However this is false.  It is in fact:
    //
    // name1
    // ...
    // name10
    // volume
    //
    // Voice 1 Parameter A
    // Voice 2 Parameter A
    // ...
    // Voice 8 Parameter A
    //
    // Voice 1 Parameter B
    // Voice 2 Parameter B
    // ...
    // Voice 2 Parameter B
    //
    // ...etc.
                
                
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
                
    "section1singleno",                     // *
    "section2singleno",                     // *
    "section3singleno",                     // *
    "section4singleno",                     // *
    "section5singleno",                     // *
    "section6singleno",                     // *
    "section7singleno",                     // *
    "section8singleno",                     // *

    "section1lowkey",
    "section2lowkey",
    "section3lowkey",
    "section4lowkey",
    "section5lowkey",
    "section6lowkey",
    "section7lowkey",
    "section8lowkey",

    "section1highkey",
    "section2highkey",
    "section3highkey",
    "section4highkey",
    "section5highkey",
    "section6highkey",
    "section7highkey",
    "section8highkey",

    "section1poly_output_playmode1",    // *
    "section2poly_output_playmode1",    // *
    "section3poly_output_playmode1",    // *
    "section4poly_output_playmode1",    // *
    "section5poly_output_playmode1",    // *
    "section6poly_output_playmode1",    // *
    "section7poly_output_playmode1",    // *
    "section8poly_output_playmode1",    // *

    "section1channel_velocitysw_playmode2",       //*
    "section2channel_velocitysw_playmode2",       //*
    "section3channel_velocitysw_playmode2",       //*
    "section4channel_velocitysw_playmode2",       //*
    "section5channel_velocitysw_playmode2",       //*
    "section6channel_velocitysw_playmode2",       //*
    "section7channel_velocitysw_playmode2",       //*
    "section8channel_velocitysw_playmode2",       //*

    "section1transpose",
    "section2transpose",
    "section3transpose",
    "section4transpose",
    "section5transpose",
    "section6transpose",
    "section7transpose",
    "section8transpose",

    "section1tune",
    "section2tune",
    "section3tune",
    "section4tune",
    "section5tune",
    "section6tune",
    "section7tune",
    "section8tune",

    "section1level",
    "section2level",
    "section3level",
    "section4level",
    "section5level",
    "section6level",
    "section7level",
    "section8level",

    };


    public int parse(byte[] data, boolean fromFile)
        {
        if (data[3] == (byte)0x20) // single
            {
            model.set("bank", ((data[7] - 64) / 8) + (data[6] == 0x00 ? 0 : 4));
            model.set("number", (data[7] - 64) % 8);
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
                    names[i][j] = (char)(data[8 + (i * 76) + j] & 127);
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
            
            boolean internal = (data[6] == 0);
            
            model.set("bank", (patchNum / 8) + (internal ? 0 : 4));
            model.set("number", patchNum % 8);

            // okay, we're loading and editing patch number patchNum.  Here we go.
            return subparse(data, patchNum * 76 + 8);         
            }
        }               
                
    public int subparse(byte[] data, int pos)
        {
        byte[] name = new byte[10];

        for(int i = 0; i < 75; i++)
            {
            int section = (i - 11) % 8 + 1;

            String key = allParameters[i];
                                                        
            if (i < 10)  // name
                {
                name[i] = data[i + pos];
                }
            else if (key.endsWith("singleno"))
                {
                int bank = data[i + pos] / 8;
                int number = data[i + pos] % 8;
                
                model.set("section" + section + "bank", bank);
                model.set("section" + section + "number", number);
                }
            else if (key.endsWith("poly_output_playmode1"))
                {
                final int NEXT_SECTION = 8;
                model.set("section" + section + "poly", data[i + pos] & 15);
                model.set("section" + section + "output", (data[i + pos] >>> 4) & 3);
                model.set("section" + section + "playmode", 
                    (((data[i + pos + NEXT_SECTION] >>> 6) & 1) << 1) |       /// next byte sequence
                    ((data[i + pos] >>> 6) & 1));
                }
            else if (key.endsWith("channel_velocitysw_playmode2"))
                {
                model.set("section" + section + "channel", data[i + pos] & 15);
                model.set("section" + section + "velocitysw", (data[i + pos] >>> 4) & 3);
                // ignore playmode2, we already handled it
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
        
    // ALWAYS sent in bulk via patch iD-8
    boolean sendKawaiParametersInBulk = true;

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

        byte[] data = new byte[75];
    
        String name = model.get("name", "Untitled") + "          ";
        
        for(int i = 0; i < 75; i++)
            {
            int section = (i - 11) % 8 + 1;

            String key = allParameters[i];
                                
            if (i < 10)  // name
                {
                data[i] = (byte)name.charAt(i);
                }
            else if (key.endsWith("singleno"))
                {
                data[i] = (byte)(model.get("section" + section + "bank") * 8 + model.get("section" + section + "number"));
                }
            else if (key.endsWith("poly_output_playmode1"))
                {
                data[i] = (byte)(((model.get("section" + section + "playmode") & 1) << 6) | (model.get("section" + section + "output") << 4) | (model.get("section" + section + "poly")));
                } 
            else if (key.endsWith("channel_velocitysw_playmode2"))
                {
                data[i] = (byte)((((model.get("section" + section + "playmode") >>> 1) & 1) << 6) | (model.get("section" + section + "velocitysw") << 4) | (model.get("section" + section + "channel")));
                }
            else
                {
                data[i] = (byte)(model.get(key));
                }
            }

        boolean external;
        byte position;
        
        external = (tempModel.get("bank") > 3);
        position = (byte)((tempModel.get("bank") & 3) * 8 + (tempModel.get("number")) + 64);
                        
        byte[] result = new byte[EXPECTED_SYSEX_LENGTH];
        result[0] = (byte)0xF0;
        result[1] = (byte)0x40;
        result[2] = (byte)getChannelOut();
        result[3] = (byte)0x20;
        result[4] = (byte)0x00;
        result[5] = (byte)0x03;
        result[6] = (byte)(external ? 0x01 : 0x00);
        
        if (toWorkingMemory && sendKawaiParametersInBulk)
            result[7] = (byte)95;
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
        byte position = (byte)((tempModel.get("bank") & 3) * 8 + (tempModel.get("number")) + 64);  // 64 for "multi", that is, 64...127 for A1 .... D16
        return new byte[] { (byte)0xF0, 0x40, (byte)getChannelOut(), 0x00, 0x00, 0x03, 
            (byte)(external ? 0x02 : 0x00),
            (byte)position, (byte)0xF7};
        }
                
    public static boolean recognize(byte[] data)
        {
        return ((data.length == EXPECTED_SYSEX_LENGTH &&
                data[0] == (byte)0xF0 &&
                data[1] == (byte)0x40 &&
                data[3] == (byte)0x20 &&
                data[4] == (byte)0x00 &&
                data[5] == (byte)0x03 &&
                (data[6] == (byte)0x00 || data[6] == (byte)0x01) &&
                data[7] >= 64)  // that is, it's multi, not single

            || recognizeBulk(data));
        }
        
    public static boolean recognizeBulk(byte[] data)
        {
        return (
            // Block Multi Data Dump
            
            data.length == 2441 &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x40 &&
            // don't care about 2, it's the channel
            data[3] == (byte)0x21 &&    // block
            data[4] == (byte)0x00 &&
            data[5] == (byte)0x03 &&
            // don't care about 6, we'll use it later
            data[7] == (byte)0x40);    // multi
        } 


    public static final int EXPECTED_SYSEX_LENGTH = 85;        
    
    
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
        
    public static String getSynthName() { return "Kawai K1/K1r/K1m [Multi]"; }
    
    public String getPatchName(Model model) { return model.get("name", "Untitled  "); }

    public int getPauseAfterChangePatch() { return 500; } // 150; }   // Seem to need about > 100ms
    public double getPauseBetweenMIDISends() { return 50;  }  // :-(  :-(
  
    public void changePatch(Model tempModel)
        {
        byte BB = (byte)tempModel.get("bank");
        byte NN = (byte)tempModel.get("number");
        
        /// The K1 cannot change patches to and from internal or external I believe.  :-(
        /// So I'm not sure what to do here.        
        if (BB >= 8) BB -= 8;
        int PC = (BB * 8 + NN) + 64;
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
