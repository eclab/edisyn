/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.rolandjv880;

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
   A patch editor for the Roland JV-880.
        
   @author Sean Luke
*/

public class RolandJV880Multi extends Synth
    {
    /// Various collections of parameter names for pop-up menus
        
    public static final String[] BANKS = new String[] { "Internal", "Card", "Preset A", "Preset B" };
    public static final String[] WRITABLE_BANKS = new String[] { "Internal", "Card" };
    public static final String[] KEY_MODES = new String[] { "Layer", "Zone", "Single" };
    public static final String[] KEYS = new String[] { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
    public static final String[] REVERB_TYPES = new String[] { "Room 1", "Room 2", "Stage 1", "Stage 2", "Hall 1", "Hall 2", "Delay", "Pan Delay" };
    public static final String[] CHORUS_TYPES = new String[] { "Chorus 1", "Chorus 2", "Chorus 3" };
    public static final String[] CHORUS_OUTPUTS = new String[] { "Mix", "Rev" };
    public static final String[] OUTPUT_SELECTS = new String[] { "Main", "Sub", "Pat" };

    public RolandJV880Multi()
        {
        for(int i = 0; i < allGlobalParameters.length; i++)
            {
            allGlobalParametersToIndex.put(allGlobalParameters[i], Integer.valueOf(i));
            }

        for(int i = 0; i < allToneParameters.length; i++)
            {
            allToneParametersToIndex.put(allToneParameters[i], Integer.valueOf(i));
            }

                
        /// SOUND PANEL
                
        JComponent soundPanel = new SynthPanel(this);
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        hbox.addLast(addGlobal(Style.COLOR_A()));
        vbox.add(hbox);
        
        vbox.add(addEffects(Style.COLOR_B()));
        vbox.add(addReserve(Style.COLOR_C()));
        
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Common", soundPanel);
                

        for(int i = 1; i < 9; i += 2)
            {
            JComponent sourcePanel = new SynthPanel(this);
            vbox = new VBox();
                
            vbox.add(addPart(i, Style.COLOR_A()));
            vbox.add(addTransmit(i, Style.COLOR_B()));
            vbox.add(addInternal(i, Style.COLOR_C()));

            vbox.add(addPart(i + 1, Style.COLOR_A()));
            vbox.add(addTransmit(i + 1, Style.COLOR_B()));
            vbox.add(addInternal(i + 1, Style.COLOR_C()));

            sourcePanel.add(vbox, BorderLayout.CENTER);
            addTab((i == 1 ? "Parts " : "") + i + "-" + (i + 1), sourcePanel);
            }


        model.set("performancename", "Init Patch");  // has to be 10 long

        model.set("bank", 0);           // internal
        model.set("number", 0);
                        
        loadDefaults();        
        }
                
                
    public JFrame sprout()
        {
        JFrame frame = super.sprout();
        // It doesn't make sense to send to current patch
        transmitTo.setEnabled(false);
        return frame;
        }         

    public String getDefaultResourceFileName() { return "RolandJV880Multi.init"; }
    public String getHTMLResourceFileName() { return "RolandJV880Multi.html"; }

    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        String[] banks = (writing ? WRITABLE_BANKS : BANKS);
        JComboBox bank = new JComboBox(banks);
        int b = model.get("bank");
        if (b >= banks.length)
            b = 0;
        bank.setSelectedIndex(b);
        
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
                showSimpleError(title, "The Patch Number must be an integer 1...64");
                continue;
                }
            if (n < 1 || n > 64)
                {
                showSimpleError(title, "The Patch Number must be an integer 1...64");
                continue;
                }
                
            n--;
            int i = bank.getSelectedIndex();
                                                                                
            if (i >= 2 && writing)  // uh oh
                {
                showSimpleError("Bank Write Error", "You cannot write to a preset bank");
                }
            else
                {                                
                change.set("bank", i);
                change.set("number", n);
                                                
                return true;
                }
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
        
        comp = new StringComponent("Patch Name", this, "performancename", 12, "Name must be up to 12 ASCII characters.")
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
        
        hbox.add(Strut.makeHorizontalStrut(80));

        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }


    public JComponent addGlobal( Color color)
        {
        Category category = new Category(this, "Global [JV-80]", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();

        VBox vbox = new VBox();
        
        params = KEY_MODES;
        comp = new Chooser("Key Mode", this, "keymode", params);
        vbox.add(comp);
        hbox.add(vbox);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addEffects( Color color)
        {
        Category category = new Category(this, "Effects", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = REVERB_TYPES;
        comp = new Chooser("Reverb Type", this, "reverbtype", params);
        vbox.add(comp);
        
        params = CHORUS_TYPES;
        comp = new Chooser("Chorus Type", this, "chorustype", params);
        vbox.add(comp);
        
        params = CHORUS_OUTPUTS;
        comp = new Chooser("Chorus Output", this, "chorusoutput", params);
        vbox.add(comp);

        hbox.add(vbox);

        comp = new LabelledDial("Reverb", this, "reverblevel", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);

        comp = new LabelledDial("Reverb", this, "reverbtime", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Time");
        hbox.add(comp);

        comp = new LabelledDial("Reverb", this, "reverbfeedback", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Feedback");
        hbox.add(comp);

        comp = new LabelledDial("Chorus", this, "choruslevel", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);

        comp = new LabelledDial("Chorus", this, "chorusdepth", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Depth");
        hbox.add(comp);

        comp = new LabelledDial("Chorus", this, "chorusrate", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Rate");
        hbox.add(comp);

        comp = new LabelledDial("Chorus", this, "chorusfeedback", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Feedback");
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addReserve(Color color)
        {
        final Category category = new Category(this, "Voice Reserve", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        for(int i = 0; i < 8; i++)
            {
            final int _i = i;
            comp = new LabelledDial("Part " + (i + 1), this, "voicereserve" + (_i + 1), color, 0, 28)
                {
                public void update(String key, Model model) 
                    {
                    // disable listeners
                    boolean li = model.getUpdateListeners();
                    model.setUpdateListeners(false);
                    boolean un = undo.getWillPush();
                    undo.setWillPush(false);
                                
                    // how big are we?
                    int over = 0;
                    for(int j = 0; j < 8; j++)
                        {
                        over = over + model.get("voicereserve" + (j + 1), 0);
                        }
                                        
                    if (over > 28)
                        {
                        over -= 28;
                                         
                        // we're over
                        while(over > 0)
                            {
                            for(int j = 0; j < 8; j++)
                                {
                                if (over == 0) break;  // all done
                                if (j == _i) continue;  // don't decrease myself
                                int v = model.get("voicereserve" + (j + 1), 0);
                                if (v > 0)
                                    {
                                    model.set("voicereserve" + (j + 1), v - 1);
                                    over--;
                                    }
                                }
                            }
                        }

                    // reenable listeners
                    model.setUpdateListeners(li);
                    undo.setWillPush(un);                           
                                
                    // update, which forces an undo snapshot
                    super.update(key, model);               
                    category.repaint();  // otherwise the dials won't update                        
                    }
                };
            hbox.add(comp);
            }
                
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }





    public JComponent addTransmit(int part, Color color)
        {
        Category category = new Category(this, "Part " + part + " Transmit [JV-80]", color);
        category.makePasteable("part" + part);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        comp = new CheckBox("Switch", this, "part" + part + "transmitswitch");
        vbox.add(comp);
        hbox.add(vbox);
                

        comp = new LabelledDial("Key Range", this, "part" + part + "transmitkeyrangelower", color, 0, 127)
            {
            public String map(int value)
                {
                return KEYS[value % 12] + (value / 12 + 1);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Lower");
        hbox.add(comp);

        comp = new LabelledDial("Key Range", this, "part" + part + "transmitkeyrangeupper", color, 0, 127)
            {
            public String map(int value)
                {
                return KEYS[value % 12] + (value / 12 + 1);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Upper");
        hbox.add(comp);

        comp = new LabelledDial("Key", this, "part" + part + "transmitkeytranspose", color, 28, 100, 64);
        ((LabelledDial)comp).addAdditionalLabel("Transpose");
        hbox.add(comp);

        comp = new LabelledDial("Velocity", this, "part" + part + "transmitvelocitysense", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Sense");
        hbox.add(comp);

        comp = new LabelledDial("Velocity", this, "part" + part + "transmitvelocitymax", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Max");
        hbox.add(comp);

        comp = new LabelledDial("Velocity", this, "part" + part + "transmitvelocitycurve", color, 0, 6, -1);
        ((LabelledDial)comp).addAdditionalLabel("Curve");
        hbox.add(comp);

        comp = new LabelledDial("Channel", this, "part" + part + "transmitchannel", color, 0, 15, -1);
        hbox.add(comp);
                
        comp = new LabelledDial("Program", this, "part" + part + "transmitprogramchange", color, 0, 128)
            {
            public String map(int val)
                {
                if (val == 128) return "Off";
                return ((val < 64 ? "A" : "B") +
                    ("" + (val / 8 + 1)) +
                    ("" + (val % 8 + 1)));
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Change");
        hbox.add(comp);
                
        comp = new LabelledDial("Volume", this, "part" + part + "transmitvolume", color, 0, 128)
            {
            public String map(int val)
                {
                if (val == 128) return "Off";
                return ("" + val);
                }
            };
        hbox.add(comp);
                
        comp = new LabelledDial("Pan", this, "part" + part + "transmitpan", color, 0, 128)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                if (value < 64)
                    {
                    return "L" + (64 - value);
                    }
                else if (value == 64) return "--";
                else if (value < 128)
                    {
                    return "R" + (value - 64);
                    }
                else return "Off";
                }
            };
        hbox.add(comp);
                

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }




    public JComponent addInternal(int part, Color color)
        {
        Category category = new Category(this, "Part " + part + " Internal [JV-80]", color);
        category.makePasteable("part" + part);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        comp = new CheckBox("Switch", this, "part" + part + "internalswitch");
        vbox.add(comp);
        hbox.add(vbox);
                

        comp = new LabelledDial("Key Range", this, "part" + part + "internalkeyrangelower", color, 0, 127)
            {
            public String map(int value)
                {
                return KEYS[value % 12] + (value / 12 + 1);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Lower");
        hbox.add(comp);

        comp = new LabelledDial("Key Range", this, "part" + part + "internalkeyrangeupper", color, 0, 127)
            {
            public String map(int value)
                {
                return KEYS[value % 12] + (value / 12 + 1);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Upper");
        hbox.add(comp);

        comp = new LabelledDial("Key", this, "part" + part + "internalkeytranspose", color, 28, 100, 64);
        ((LabelledDial)comp).addAdditionalLabel("Transpose");
        hbox.add(comp);

        comp = new LabelledDial("Velocity", this, "part" + part + "internalvelocitysense", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Sense");
        hbox.add(comp);

        comp = new LabelledDial("Velocity", this, "part" + part + "internalvelocitymax", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Max");
        hbox.add(comp);

        comp = new LabelledDial("Velocity", this, "part" + part + "internalvelocitycurve", color, 0, 6, -1);
        ((LabelledDial)comp).addAdditionalLabel("Curve");
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }



    public JComponent addPart(final int part, Color color)
        {
        Category category = new Category(this, "Part " + part, color);
        category.makePasteable("part" + part);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();

        comp = new LabelledDial("Patch Number", this, "part" + part + "patchnumber", color, 0, 255)
            {
            public String map(int value)
                {
                String s = (value < 64 ? "I" : (value < 128 ? "C" : (value < 192 ? "A" : "B")));
                String p = "" + (value % 64 + 1);
                if (p.length() == 1) 
                    p = "0" + p;
                return s + p;
                }
            };
        hbox.add(comp);

        VBox vbox = new VBox();

        comp = new PushButton("Show")
            {
            public void perform()
                {
                final RolandJV880 synth = new RolandJV880();
                if (tuple != null)
                    synth.tuple = tuple.copy(synth.buildInReceiver(), synth.buildKeyReceiver(), synth.buildKey2Receiver());
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
                                Model tempModel = buildModel();
                                
                                int bn = RolandJV880Multi.this.model.get("part" + part + "patchnumber");
                                int bank = (bn / 64);
                                int number = (bn % 64);
                                
                                tempModel.set("bank", bank);
                                tempModel.set("number", number);
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

        params = OUTPUT_SELECTS;
        comp = new Chooser("Output Select", this, "part" + part + "outputselect", params);
        vbox.add(comp);
        hbox.add(vbox);

        vbox = new VBox();        
        comp = new CheckBox("Receive Switch", this, "part" + part + "receiveswitch");
        vbox.add(comp);

        comp = new CheckBox("Reverb Switch", this, "part" + part + "reverbswitch");
        vbox.add(comp);

        comp = new CheckBox("Chorus Switch", this, "part" + part + "chorusswitch");
        vbox.add(comp);
        hbox.add(vbox);

        vbox = new VBox();
        comp = new CheckBox("Receive Program Change", this, "part" + part + "receiveprogramchange");
        vbox.add(comp);

        comp = new CheckBox("Receive Volume", this, "part" + part + "receivevolume");
        vbox.add(comp);
                
        comp = new CheckBox("Receive Hold-1", this, "part" + part + "receivehold1");
        vbox.add(comp);
        hbox.add(vbox);


        comp = new LabelledDial("Channel", this, "part" + part + "receivechannel", color, 0, 15, -1);
        hbox.add(comp);

        comp = new LabelledDial("Level", this, "part" + part + "partlevel", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Pan", this, "part" + part + "partpan", color, 0, 127)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                if (value < 64)
                    {
                    return "L" + (64 - value);
                    }
                else if (value == 64) return "--";
                else 
                    {
                    return "R" + (value - 64);
                    }
                }
            };
        hbox.add(comp);
                
        comp = new LabelledDial("Coarse Tune", this, "part" + part + "partcoarsetune", color, 16, 112, 64);
        hbox.add(comp);

        comp = new LabelledDial("Fine Tune", this, "part" + part + "partfinetune", color, 14, 114, 64);
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    HashMap allGlobalParametersToIndex = new HashMap();
        
    final static String[] allGlobalParameters = new String[]
    {
    "performancename1",
    "performancename2",
    "performancename3",
    "performancename4",
    "performancename5",
    "performancename6",
    "performancename7",
    "performancename8",
    "performancename9",
    "performancename10",
    "performancename11",
    "performancename12",
    "keymode",
    "reverbtype",
    "reverblevel",
    "reverbtime",
    "reverbfeedback",
    "chorustype",
    "choruslevel",
    "chorusdepth",
    "chorusrate",
    "chorusfeedback",
    "chorusoutput",
    "voicereserve1",
    "voicereserve2",
    "voicereserve3",
    "voicereserve4",
    "voicereserve5",
    "voicereserve6",
    "voicereserve7",
    "voicereserve8",
    };
    
    
    HashMap allToneParametersToIndex = new HashMap();
        
    final static String[] allToneParameters = new String[]
    {
    "transmitswitch",
    "transmitchannel",
    "transmitprogramchange",
    "-",                // second byte
    "transmitvolume",
    "-",                // second byte
    "transmitpan",
    "-",                // second byte
    "transmitkeyrangelower",
    "transmitkeyrangeupper",
    "transmitkeytranspose",
    "transmitvelocitysense",
    "transmitvelocitymax",
    "transmitvelocitycurve",
    "internalswitch",
    "internalkeyrangelower",
    "internalkeyrangeupper",
    "internalkeytranspose",
    "internalvelocitysense",
    "internalvelocitymax",
    "internalvelocitycurve",
    "receiveswitch",
    "receivechannel",
    "patchnumber",
    "-",                // second byte
    "partlevel",
    "partpan",
    "partcoarsetune",
    "partfinetune",
    "reverbswitch",
    "chorusswitch",
    "receiveprogramchange",
    "receivevolume",
    "receivehold1",
    "outputselect"
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
        
    public byte[] getData(String key)
        {
        if (key.startsWith("performancename"))                          // name is 12-byte
            {
            byte[] data = new byte[12];
            String name = model.get(key, "Untitled");
            for(int i = 0; i < name.length(); i++)
                {
                data[i] = (byte)(name.charAt(i));
                }
            return data;
            }
        else if (key.endsWith("transmitprogramchange") ||               // Some data is 2-byte
            key.endsWith("transmitvolume") ||
            key.endsWith("transmitpan") ||
            key.endsWith("patchnumber"))
            {
            int data = model.get(key);
            // MSB is first
            byte MSB = (byte)((data >> 4) & 127);
            byte LSB = (byte)(data & 15);
            return new byte[] { MSB, LSB };
            }
        else                                                                                    // Some data is 1-byte
            {
            return new byte[] { (byte) model.get(key) };
            }
        }
        
    public Object[] emitAll(String key)
        {
        if (key.equals("-")) return new Object[0];  // this is not emittable
        if (key.equals("bank")) return new Object[0];  // this is not emittable
        if (key.equals("number")) return new Object[0];  // this is not emittable

        // address for "temporary performance"
        byte AA = (byte)0x00;
        byte BB = (byte)0x00;
        byte CC = (byte)0x10;
        byte DD = (byte)0x00;
            
        // figure out the address
        if (key.startsWith("part1"))
            {
            CC = (byte)(CC + 0x08);
            DD = (byte)(DD + ((Integer)(allToneParametersToIndex.get(key.substring(5)))).intValue());  // get rid of the "part1"
            }
        else if (key.startsWith("part2"))
            {
            CC = (byte)(CC + 0x09);
            DD = (byte)(DD + ((Integer)(allToneParametersToIndex.get(key.substring(5)))).intValue());  // get rid of the "part2"
            }
        else if (key.startsWith("part3"))
            {
            CC = (byte)(CC + 0x0A);
            DD = (byte)(DD + ((Integer)(allToneParametersToIndex.get(key.substring(5)))).intValue());  // get rid of the "part3"
            }
        else if (key.startsWith("part4"))
            {
            CC = (byte)(CC + 0x0B);
            DD = (byte)(DD + ((Integer)(allToneParametersToIndex.get(key.substring(5)))).intValue());  // get rid of the "part4"
            }
        else if (key.startsWith("part5"))
            {
            CC = (byte)(CC + 0x0C);
            DD = (byte)(DD + ((Integer)(allToneParametersToIndex.get(key.substring(5)))).intValue());  // get rid of the "part5"
            }
        else if (key.startsWith("part6"))
            {
            CC = (byte)(CC + 0x0D);
            DD = (byte)(DD + ((Integer)(allToneParametersToIndex.get(key.substring(5)))).intValue());  // get rid of the "part6"
            }
        else if (key.startsWith("part7"))
            {
            CC = (byte)(CC + 0x0E);
            DD = (byte)(DD + ((Integer)(allToneParametersToIndex.get(key.substring(5)))).intValue());  // get rid of the "part7"
            }
        else if (key.startsWith("part8"))
            {
            CC = (byte)(CC + 0x0F);
            DD = (byte)(DD + ((Integer)(allToneParametersToIndex.get(key.substring(5)))).intValue());  // get rid of the "part8"
            }
        else                // Common
            {
            DD = (byte)(DD + ((Integer)(allGlobalParametersToIndex.get(key))).intValue());
            }
        
        byte[] payload = getData(key);

        // Handle irregularities in multi-byte data
        if (payload.length == 12)
            {
            byte[] data = new byte[23];
                        
            // gather data which is checksummed
            byte[] checkdata = new byte[4 + 12];
            System.arraycopy(new byte[] { AA, BB, CC, DD }, 0, data, 0, 4);
            System.arraycopy(payload, 0, checkdata, 4, payload.length);
                        
            // concatenate all data
            byte checksum = produceChecksum(checkdata);
            data[0] = (byte)0xF0;
            data[1] = (byte)0x41;
            data[2] = getID();
            data[3] = (byte)0x46;
            data[4] = (byte)0x12;
            System.arraycopy(checkdata, 0, data, 5, checkdata.length);
            data[21] = checksum;
            data[22] = (byte)0xF7;
                    
            return new Object[] { data };
            }
        else if (payload.length == 2)
            {
            byte checksum = produceChecksum(new byte[] { AA, BB, CC, DD, payload[0], payload[1] });
            byte[] data = new byte[] { (byte)0xF0, (byte)0x41, getID(), (byte)0x46, (byte)0x12, AA, BB, CC, DD, payload[0], payload[1], checksum, (byte)0xF7 };
            return new Object[] { data };
            }
        else                                                                                    // Some data is 1-byte
            {
            byte checksum = produceChecksum(new byte[] { AA, BB, CC, DD, payload[0] });
            byte[] data = new byte[] { (byte)0xF0, (byte)0x41, getID(), (byte)0x46, (byte)0x12, AA, BB, CC, DD, payload[0], checksum, (byte)0xF7 };
            return new Object[] { data };
            }
        }
    
    int parseStatus = 0;
        
    public int parse(byte[] data, boolean fromFile)
        {
        if (data.length == 410)
            {
            int pos = 0;

            // common
            byte[] buf = new byte[31 + 11];
            System.arraycopy(data, pos, buf, 0, buf.length);
            if (parse(buf, fromFile) != PARSE_INCOMPLETE) return PARSE_FAILED;
            pos += (31 + 11);

            // tones
            for(int i = 0; i < 8; i++)
                {
                buf = new byte[35 + 11];
                System.arraycopy(data, pos, buf, 0, buf.length);
                int ret = parse(buf, fromFile);
                if (!  ((i == 7 && ret == PARSE_SUCCEEDED) ||
                        (i != 7 && ret == PARSE_INCOMPLETE))) return PARSE_FAILED;
                pos += (35 + 11);
                }
            return PARSE_SUCCEEDED;
            }
        
        int pos = 9;
        
        if (data.length == 31 + 11)     // common
            {               
            String name = "";
            // Load patch name
            for(int i = 0; i < 12; i++)
                {
                name = name + ((char)data[pos++]);
                }
            model.set("performancename", name);
                        
            // Load other patch common
            for(int i = 12; i < allGlobalParameters.length; i++)  // skip name parameters
                {
                model.set(allGlobalParameters[i], data[pos++]);         // there are no two-byte parameters, so we're okay
                }
                                
            parseStatus = 1;
            return PARSE_INCOMPLETE;
            }
        else if (data.length == 35 + 11 && parseStatus >= 1 && parseStatus <= 8)
            {
            for(int i = 0; i < allToneParameters.length; i++)
                {
                String key = allToneParameters[i];
                if (key.equals("-")) // two-byte character. Skip, don't increase pos
                    {
                    }
                else if (key.endsWith("transmitprogramchange") ||               // Some data is 2-byte
                    key.endsWith("transmitvolume") ||
                    key.endsWith("transmitpan") ||
                    key.endsWith("patchnumber"))
                    {
                    model.set("part" + parseStatus + key, (data[pos++] << 4) | data[pos++]);
                    }
                else
                    {
                    model.set("part" + parseStatus + key, data[pos++]);
                    }
                }
                        
            if (parseStatus == 8)
                return PARSE_SUCCEEDED;
            else
                {
                parseStatus++;
                return PARSE_INCOMPLETE;
                }
            }
        else
            {
            System.err.println("Invalid tone number " + parseStatus + " or data length " + data.length);
            return PARSE_FAILED;
            }
        }
        
    byte[] prepareBuffer(byte[] buf, Model model, boolean toWorkingMemory, int t)
        {
        buf[0] = (byte)0xF0;
        buf[1] = (byte)0x41;
        buf[2] = (byte)getID();
        buf[3] = (byte)0x46;
        buf[4] = (byte)0x12;
        if (true || toWorkingMemory || model.get("bank", 0) >= 2)       // we can't write to preset banks)
            {
            buf[5] = (byte)0x00;
            buf[6] = (byte)0x00;
            buf[7] = (byte)0x10;
            buf[8] = (byte)0x00;
            }
        else
            {
            int bank = model.get("bank", 0);
            int number = model.get("number", 0);
            buf[5] = (byte)(bank == 0 ? 0x01 : 0x02);
            buf[6] = (byte)(number);
            buf[7] = (byte)(0x10);
            buf[8] = (byte)0x00;
            }
        buf[buf.length - 1] = (byte)0xF7;
                
        return buf;
        }

    public Object[] emitAll(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        if (tempModel == null)
            tempModel = getModel();

        Object[] data = new Object[9];
                
        // patch common
        byte[] buf = prepareBuffer(new byte[31 + 11], tempModel, toWorkingMemory, 0);
                
        int pos = 9;
        String name = model.get("performancename", "") + "            ";
        for(int i = 0; i < 12; i++)
            {
            buf[pos++] = (byte)(name.charAt(i) & 0x7F);
            }
        for(int i = 12; i < allGlobalParameters.length; i++)    // skip name parameters
            {
            buf[pos++] = (byte)(model.get(allGlobalParameters[i], 0) & 0x7F);
            }
        buf[buf.length - 2] = produceChecksum(buf, 9 - 4, 31 + 11 - 2);
        data[0] = buf;  
        
        
        // tones
        for(int t = 1; t < 9; t++)
            {
            buf = prepareBuffer(new byte[35 + 11], tempModel, toWorkingMemory, t);
        
            buf[7] += (t + 7);  // So part 1 is 28, part 2 is 29, part 3 is 2A, etc.
                        
            pos = 9;
            for(int i = 0; i < allToneParameters.length; i++)
                {
                if (allToneParameters[i].equals("-")) continue;
                byte[] d = getData("part" + t + allToneParameters[i]);
                for(int j = 0; j < d.length; j++)
                    {
                    buf[pos++] = d[j];
                    }
                }
            buf[buf.length - 2] = produceChecksum(buf, 9 - 4, 35 + 11 - 2);
            data[t] = buf;
            }
                
        return data;
        }


    public byte[] requestDump(Model tempModel)
        {
        // performRequestDump has already changed the patch.  At this point the patch is in local memory, so we're
        // going to just call requestCurrentDump.  This allows us to grab presets A and B as well as internal and card.
        return requestCurrentDump();

        /*
          byte AA = ((tempModel.get("bank") == 0) ? (byte)0x01 : (byte)0x02);             // bank
          byte BB = (byte)(0x40 + (tempModel.get("number")));                                             // number
          byte CC = (byte)(0x20);
          byte DD = (byte)(0x00);
        
          byte checksum = produceChecksum(new byte[] { AA, BB, CC, DD, (byte)0x00, (byte)0x00, (byte)0x10, (byte)0x00 });
          return new byte[] { (byte)0xF0, (byte)0x41, getID(), (byte)0x46, (byte)0x11, 
          AA, BB, CC , DD, (byte)0x00, (byte)0x00, (byte)0x10, (byte)0x00, checksum, (byte)0xF7 }; 
        */
        }

    public byte[] requestCurrentDump()
        {
        byte AA = (byte)(0x00);
        byte BB = (byte)(0x00);
        byte CC = (byte)(0x10);
        byte DD = (byte)(0x00);
        
        byte checksum = produceChecksum(new byte[] { AA, BB, CC, DD, (byte)0x00, (byte)0x00, (byte)0x10, (byte)0x00 });
        return new byte[] { (byte)0xF0, (byte)0x41, getID(), (byte)0x46, (byte)0x11, 
            AA, BB, CC , DD, (byte)0x00, (byte)0x00, (byte)0x10, (byte)0x00, checksum, (byte)0xF7 }; 
        }
    
    
    public static boolean recognize(byte[] data)
        {
        return ((data[0] == (byte)0xF0) &&
            (data[1] == (byte)0x41) &&
            (data[3] == (byte)0x46) &&
            (data[4] == (byte)0x12) &&
                
            // Internal performance
                ((data[5] == 0x01 && (data[7] == 0x10 || data[7] == 0x18 || data[7] == 0x19 || data[7] == 0x1A || data[7] == 0x1B || data[7] == 0x1C || data[7] == 0x1D || data[7] == 0x1E || data[7] == 0x1F)) || 
                // Card performance
                (data[5] == 0x02 && (data[7] == 0x10 || data[7] == 0x18 || data[7] == 0x19 || data[7] == 0x1A || data[7] == 0x1B || data[7] == 0x1C || data[7] == 0x1D || data[7] == 0x1E || data[7] == 0x1F)) ||
                // Temporary Performance
                (data[5] == 0x00 && data[6] == 0x00 && (data[7] == 0x10 || data[7] == 0x18 || data[7] == 0x19 || data[7] == 0x1A || data[7] == 0x1B || data[7] == 0x1C || data[7] == 0x1D || data[7] == 0x1E || data[7] == 0x1F))) &&
                 
            (data.length == 410 || data.length == 31 + 11 || data.length == 35 + 11));
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
        
    public static String getSynthName() { return "Roland JV-80/880 [Multi]"; }
    
    public String getPatchName(Model model) { return model.get("name", "Untitled  "); }

    public int getPauseAfterChangePatch() { return 200; }   // Seem to need about > 100ms

    public int getPauseAfterSendAllParameters() { return 100; } 
 
    public void changePatch(Model tempModel)
        {
        byte BB = (byte)tempModel.get("bank");
        byte NN = (byte)tempModel.get("number");
        byte BC = (byte)(BB < 2 ? 80 : 81);
        byte PC = (byte)(BB == 0 || BB == 2 ? NN : NN + 64);
        
        try 
            {
            tryToSendMIDI(new ShortMessage(ShortMessage.CONTROL_CHANGE, getChannelOut(), 0, BC));
            tryToSendMIDI(new ShortMessage(ShortMessage.PROGRAM_CHANGE, getChannelOut(), PC, 0));
            }
        catch (Exception e) { Synth.handleException(e); }
        }
        
    public Model getNextPatchLocation(Model model)
        {
        int bank = model.get("bank");
        int number = model.get("number");
        
        number++;
        if (number >= 64)
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
        
        return BANKS[model.get("bank")] + " " + (model.get("number") + 1 < 10 ? "0" : "") + ((model.get("number") + 1));
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
        
    public int getBulkDownloadWaitTime() { return 750; }
    }
