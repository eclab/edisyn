/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.yamahafb01;

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
   A patch editor for the Yamaha FB-01.
        
   @author Sean Luke
*/

/**
   The sysex for the Yamaha FB01 is documented very poorly. There are two sources:
   the User Manual and the Service Manual, and both of them are highly cryptic,
   missing (different!) procedures, and contradictory.   
**/

public class YamahaFB01Multi extends Synth
    {
    public static final String[] PITCH_MOD_CONTROLS = { "Off", "Aftertouch", "Pitch Wheel", "Breath Controller", "Foot Controller" };    
    public static final String[] KEY_RECEIVE_MODES = { "All", "Even", "Odd" };
    public static final String[] LFO_WAVES = { "Sawtooth", "Square", "Triangle", "Sample and Hold" };
    public static final String[] VOICE_BANKS = { "1 (A)", "2 (B)", "3 General", "4 Keyboards", "5 Brass/Strings/Wood", "6 Synth/Bass/Drum", "7 Organ/Pluck/FX" };
    public static final String[] KEYS = new String[] { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };


    public YamahaFB01Multi()
        {
        model.set("number", 0);
                
        /// SOUND PANEL
                
        JComponent soundPanel = new SynthPanel(this);
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        hbox.addLast(addGlobal(Style.COLOR_A()));
        vbox.add(hbox);
        
        vbox.add(addInstrument(1, Style.COLOR_A()));
        vbox.add(addInstrument(2, Style.COLOR_A()));
        vbox.add(addInstrument(3, Style.COLOR_A()));
        vbox.add(addInstrument(4, Style.COLOR_A()));

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Global, Instruments 1-4", soundPanel);

        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        
        vbox.add(addInstrument(5, Style.COLOR_A()));
        vbox.add(addInstrument(6, Style.COLOR_A()));
        vbox.add(addInstrument(7, Style.COLOR_A()));
        vbox.add(addInstrument(8, Style.COLOR_A()));

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Instruments 5-8", soundPanel);
        
        model.set("name", "INIT");
        
        loadDefaults();     
        }
                
    public JFrame sprout()
        {
        JFrame frame = super.sprout();
        transmitTo.setEnabled(false);
        addYamahaFB01Menu();
        return frame;
        }         


    public void addYamahaFB01Menu()
        {
        JMenu menu = new JMenu("FB-01");
        menubar.add(menu);
        JMenuItem turnMenuProtectOffMenu = new JMenuItem("Turn Memory Protect Off");
        turnMenuProtectOffMenu.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                turnMemoryProtectOff();
                }
            });
        menu.add(turnMenuProtectOffMenu);
        }

/// This site makes the following claims that we might wish to look into:
/// https://www.vogons.org/viewtopic.php?p=362894#p362894
/// 
///     F0 43 75 00 10 21 00 F7 - Turns memory-protect off
/// F0 43 75 00 10 20 00 F7 - Sets the system channel to 1

    public void turnMemoryProtectOff()
        {
        tryToSendSysex(new byte[] { (byte)0xF0, 0x43, 0x75, 0x00, 0x10, 0x21, 0x00, (byte)0xF7 });
        }




    public String getDefaultResourceFileName() { return "YamahaFB01Multi.init"; }
    public String getHTMLResourceFileName() { return "YamahaFB01Multi.html"; }

    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        JTextField number = new JTextField("" + (model.get("number") + 1), 3);

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
                showSimpleError(title, "The Patch Number must be an integer 1...20");
                continue;
                }
            if (writing && (n < 1 || n > 16))
                {
                showSimpleError(title, "The Patch Number must be an integer 1...16");
                continue;
                }
            else if (n < 1 || n > 20)
                {
                showSimpleError(title, "The Patch Number must be an integer 1...20");
                continue;
                }
                
            n--;
                                
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
        
        comp = new StringComponent("Patch Name", this, "name", 8, "Name must be up to 8 ASCII characters.")
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

        // Not enough space to show the title
        hbox.addLast(Strut.makeHorizontalStrut(100));

        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }


    public JComponent addGlobal( Color color)
        {
        Category category = new Category(this, "Global", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();

        VBox vbox = new VBox();
        params = LFO_WAVES;
        comp = new Chooser("LFO Wave", this, "lfowave", params);
        vbox.add(comp);

        params = KEY_RECEIVE_MODES;
        comp = new Chooser("Key Number Recieve Mode", this, "keycodenumberreceivemode", params);
        vbox.add(comp);

        comp = new CheckBox("Voice Function Combine Mode", this, "voicefunctioncombinemode");
        ((CheckBox)comp).addToWidth(2);
        vbox.add(comp);
        hbox.add(vbox);

        // The manual says this is 1-127,
        // But the service manual says this is 0-127
        comp = new LabelledDial("LFO speed", this, "lfospeed", color, 0, 127);
        hbox.add(comp);

        // The manual says this is 1-127,
        // But the service manual says this is 0-127
        // Even weirder: individual LFO speeds for voices are 0-255!
        comp = new LabelledDial("Amplitude", this, "amplitudemodulationdepth", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Modulation Depth");
        hbox.add(comp);

        // The manual says this is 1-127,
        // But the service manual says this is 0-127
        comp = new LabelledDial("Pitch", this, "pitchmodulationdepth", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Modulation Depth");
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    JLabel romLabel[] = new JLabel[8];
        
    public JComponent addInstrument(final int src, Color color)
        {
        final Category category = new Category(this, "Instrument " + src, color);
        category.makePasteable("inst" + src);

        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();
        
        VBox vbox = new VBox();

        params = VOICE_BANKS;
        comp = new Chooser("Voice Bank", this, "inst" +  src + "voicebank", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                if (romLabel[src - 1] == null) return;          // may not be ready yet
                int num = model.get("inst" + src + "voicenumber", 0);
                int bank = model.get("inst" + src + "voicebank", 0);
                if (bank < 2)
                    {
                    romLabel[src - 1].setText(" ");
                    }
                else
                    {
                    romLabel[src -1 ].setText("" + ROM_VOICE_NAMES[(bank - 2) * 48 + num]);
                    }
                }
            };
        vbox.add(comp);

        params = PITCH_MOD_CONTROLS;
        comp = new Chooser("Pitch Mod Control", this, "inst" +  src + "pitchmodcontrol", params);
        vbox.add(comp);
        hbox.add(vbox);
        vbox = new VBox();
                
        comp = new CheckBox("LFO On", this,  "inst" +  src + "lfoenable");
        ((CheckBox)comp).addToWidth(2);
        vbox.add(comp);
        
        comp = new CheckBox("Mono", this,  "inst" +  src + "monopoly");
        vbox.add(comp);
        
        final PushButton showButton = new PushButton("Show")
            {
            public void perform()
                {
                final YamahaFB01 synth = new YamahaFB01();
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
                                tempModel.set("bank", YamahaFB01Multi.this.model.get("inst" + src + "voicebank"));
                                tempModel.set("number", YamahaFB01Multi.this.model.get("inst" + src + "voicenumber"));
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
        


        hbox.add(vbox);

        comp = new LabelledDial("Voice Number", this, "inst" +  src + "voicenumber", color, 0, 47, -1)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                if (romLabel[src - 1] == null) return;          // may not be ready yet
                int num = model.get("inst" + src + "voicenumber", 0);
                int bank = model.get("inst" + src + "voicebank", 0);
                if (bank < 2)
                    {
                    romLabel[src - 1].setText(" ");
                    }
                else
                    {
                    romLabel[src - 1].setText("" + ROM_VOICE_NAMES[(bank - 2) * 48 + num]);
                    }
                }
            };
        romLabel[src - 1] = ((LabelledDial) comp).addAdditionalLabel(" ");
                
        hbox.add(comp);

        comp = new LabelledDial("Num Notes", this, "inst" +  src + "numberofnotes", color, 0, 8);
        hbox.add(comp);
        
        comp = new LabelledDial("MIDI Channel", this, "inst" +  src + "midichannel", color, 0, 15, -1);
        hbox.add(comp);
        
        comp = new LabelledDial("Low Key", this, "inst" +  src + "keycodenumberlimitlow", color, 0, 127)
            {
            public String map(int val)
                {
                return KEYS[val % 12] + (val / 12 - 2);  // note integer division
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("High Key", this, "inst" +  src + "keycodenumberlimithigh", color, 0, 127)
            {
            public String map(int val)
                {
                return KEYS[val % 12] + (val / 12 - 2);  // note integer division
                }
            };
        hbox.add(comp);
        
        comp = new LabelledDial("Detune", this, "inst" +  src + "detune", color, -64, 63)
            {
            public boolean isSymmetric() { return true; }
            };
        hbox.add(comp);
        
        comp = new LabelledDial("Octave", this, "inst" +  src + "octavetranspose", color, 0, 4, 2)
            {
            public boolean isSymmetric() { return true; }
            };
        ((LabelledDial)comp).addAdditionalLabel("Transpose");
        hbox.add(comp);
        
        comp = new LabelledDial("Level", this, "inst" +  src + "outputlevel", color, 0, 127);
        hbox.add(comp);
        
        comp = new LabelledDial("Pan", this, "inst" +  src + "pan", color, 0, 127)
            {
            public String map(int val)
                {
                if (val == 64) return "--";
                else if (val < 64) return "< " + (64 - val);
                else return "" + (val - 64) + " >";
                }
            public boolean isSymmetric() { return true; }
            };
        hbox.add(comp);
        
        comp = new LabelledDial("Portamento", this, "inst" +  src + "portamentotime", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Time");
        hbox.add(comp);
        
        comp = new LabelledDial("Pitch Bend", this, "inst" +  src + "pitchbendrange", color, 0, 12);
        ((LabelledDial)comp).addAdditionalLabel("Range");
        hbox.add(comp);
        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public Object[] emitAll(String key)
        {
        int param = 0;
        int val = 0;
        if (key.startsWith("inst"))
            {
            int inst = StringUtility.getFirstInt(key);
            String k = key.substring(5);            // remove "inst4" etc.
            if (k.equals("numberofnotes"))
                {
                param = 0x00;
                val = model.get(key);
                }
            else if (k.equals("midichannel"))
                {
                param = 0x01;
                val = model.get(key);
                }
            else if (k.equals("keycodenumberlimithigh"))
                {
                param = 0x02;
                val = model.get(key);
                }
            else if (k.equals("keycodenumberlimitlow"))
                {
                param = 0x03;
                val = model.get(key);
                }
            else if (k.equals("voicebank"))
                {
                param = 0x04;
                val = model.get(key);
                }
            else if (k.equals("voicenumber"))
                {
                param = 0x05;
                val = model.get(key);
                }
            else if (k.equals("detune"))
                {
                param = 0x06;
                val = model.get(key);
                // detune is 7-bit two's complement
                if (val < 0) val += 128;
                }
            else if (k.equals("octavetranspose"))
                {
                param = 0x07;
                val = model.get(key);
                }
            else if (k.equals("outputlevel"))
                {
                param = 0x08;
                val = model.get(key);
                }
            else if (k.equals("pan"))
                {
                param = 0x09;
                val = model.get(key);
                }
            else if (k.equals("lfoenable"))
                {
                param = 0x0A;
                val = model.get(key);
                }
            else if (k.equals("portamentotime"))
                {
                param = 0x0B;
                val = model.get(key);
                }
            else if (k.equals("pitchbendrange"))
                {
                param = 0x0C;
                val = model.get(key);
                }
            else if (k.equals("monopoly"))
                {
                param = 0x0D;
                val = model.get(key);
                }
            else if (k.equals("pitchmodcontrol"))
                {
                param = 0x0E;
                val = model.get(key);
                }
            else
                {
                System.err.println("Unknown Key " + key);
                return new Object[0];
                }

            // Parameter change by System Channel + Instrument Number (Table 1), page 48, user manual
            byte[] data = new byte[] { (byte)0xF0, 0x43, 0x75, (byte)(getID() - 1),
                (byte)((inst - 1) + 24), (byte)param, (byte)val, (byte)0xF7 };  // val & 127 so we can trim for two's complement
            return new Object[] { data };
            }
        else
            {
            // can't do non-instrument parameters
            return new Object[0];
            }
        }
    

    public void parseOne(byte[] d)
        {
        int pos = 0;
        char[] chars = new char[8];
        for(int i = 0; i < 8; i++)
            {
            chars[i] = (char)d[pos++];
            }
        model.set("name", new String(chars));
        model.set("voicefunctioncombinemode", d[pos++] & 1);
        model.set("lfospeed", d[pos++] & 127);
        model.set("amplitudemodulationdepth", d[pos++] & 127);
        model.set("pitchmodulationdepth", d[pos++] & 127);
        model.set("lfowave", d[pos++] & 3);
        model.set("keycodenumberreceivemode", d[pos++] & 3);
        pos+= 18;           // "reserved"
            
        for(int op = 1; op <= 8; op++)
            {
            model.set("inst" +  op + "numberofnotes", d[pos++] & 15);
            model.set("inst" +  op + "midichannel", d[pos++] & 15);
            model.set("inst" +  op + "keycodenumberlimithigh", d[pos++] & 127);
            model.set("inst" +  op + "keycodenumberlimitlow", d[pos++] & 127);
            model.set("inst" +  op + "voicebank", d[pos++] & 7);
            model.set("inst" +  op + "voicenumber", d[pos++] & 63);
            int val = d[pos++];
            if (val >= 64)
                val -= 128;
            model.set("inst" +  op + "detune", val);                // 2's complement
            model.set("inst" +  op + "octavetranspose", d[pos++] & 7);
            model.set("inst" +  op + "outputlevel", d[pos++] & 127);
            model.set("inst" +  op + "pan", d[pos++] & 127);
            model.set("inst" +  op + "lfoenable", d[pos++] & 1);
            model.set("inst" +  op + "portamentotime", d[pos++] & 127);
            model.set("inst" +  op + "pitchbendrange", d[pos++] & 15);
            model.set("inst" +  op + "monopoly", d[pos++] & 1);
            model.set("inst" +  op + "pitchmodcontrol", d[pos++] & 7);
            pos++;                  // "reserved"
            }
        }
        
    public int parse(byte[] data, boolean fromFile)
        {
        if (data.length > 160 + 11)
            {
            int start = 7;
                        
            // extract the names
            char[][] names = new char[16][8];
            for(int i = 0; i < 16; i++)
                {
                int pos = start + i * 163 + 2;
                for(int j = 0; j < 8; j++)
                    {
                    names[i][j] = (char)data[pos + j];
                    }
                }
            String[] n = new String[16];
            for(int i = 0; i < 16; i++)
                {
                n[i] = "" + (i + 1) + "   " + new String(names[i]);
                }

            // Now that we have an array of names, one per patch, we present the user with options;
            // 0. Cancel [handled automatically]
            // 1. Save the bank data [handled automatically]
            // 2. Upload the bank data [handled automatically] 
            // 3. Load and edit a certain patch number
            int patchNum = showBankSysexOptions(data, n);
            if (patchNum < 0) 
                return PARSE_CANCELLED;

            model.set("number", patchNum);
                
            byte[] d = new byte[160];
            System.arraycopy(data, start + patchNum * 163 + 2, d, 0, 160);
            parseOne(d);
            revise();
            return PARSE_SUCCEEDED;
            }
        else
            {
            int start = 7;
            model.set("number", data[6]);
                
            byte[] d = new byte[160];
            System.arraycopy(data, start + 2, d, 0, 160);
            parseOne(d);
            revise();
            return PARSE_SUCCEEDED;
            }
        }
        
    public byte[] emit(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        if (tempModel == null)
            tempModel = getModel();

        int packetlen = 160;
        byte[] data = new byte[packetlen + 11];
        
        // Current Configuration (#3 on Page 15 of service manual)
        // or Configuration Memry (#4 on Page 16 of service manual)
        // ...
        // or Current Configuration Buffer (page 55 of user manual)
        // or Configuration Memory XX (page 56 of user manual)
                        
        data[0] = (byte)0xF0;
        data[1] = 0x43;
        data[2] = 0x75;
        data[3] = (byte)(getID() - 1);
        data[4] = 0;
        data[5] = (byte)(toWorkingMemory || toFile ? 0x01 : 0x02);
        data[6] = (byte)(toWorkingMemory || toFile ? 0x00 : tempModel.get("number"));
        // Service manual says this should be data[7] = 0x01, data[8] = 0x20, which is 160...
        data[7] = (byte)(packetlen >>> 7);
        data[8] = (byte)(packetlen & 127);
        data[data.length - 1] = (byte)0xF7;
        
        int pos = 9;
        
        String name = model.get("name", "        ") + "        ";
        for(int i = 0; i < 8; i++)
            {
            data[pos++] = (byte)(name.charAt(i) & 127);
            }
        
        data[pos++] = (byte)(model.get("voicefunctioncombinemode"));
        data[pos++] = (byte)(model.get("lfospeed"));
        data[pos++] = (byte)(model.get("amplitudemodulationdepth"));
        data[pos++] = (byte)(model.get("pitchmodulationdepth"));
        data[pos++] = (byte)(model.get("lfowave"));
        data[pos++] = (byte)(model.get("keycodenumberreceivemode"));
        pos+= 18;           // "reserved"
            
        for(int op = 1; op <= 8; op++)
            {
            data[pos++] = (byte)(model.get("inst" +  op + "numberofnotes"));
            data[pos++] = (byte)(model.get("inst" +  op + "midichannel"));
            data[pos++] = (byte)(model.get("inst" +  op + "keycodenumberlimithigh"));
            data[pos++] = (byte)(model.get("inst" +  op + "keycodenumberlimitlow"));
            data[pos++] = (byte)(model.get("inst" +  op + "voicebank"));
            data[pos++] = (byte)(model.get("inst" +  op + "voicenumber"));
            // detune is 2's complement to 7 bits
            data[pos++] = (byte)(model.get("inst" +  op + "detune") & 127);
            data[pos++] = (byte)(model.get("inst" +  op + "octavetranspose"));
            data[pos++] = (byte)(model.get("inst" +  op + "outputlevel"));
            data[pos++] = (byte)(model.get("inst" +  op + "pan"));
            data[pos++] = (byte)(model.get("inst" +  op + "lfoenable"));
            data[pos++] = (byte)(model.get("inst" +  op + "portamentotime"));
            data[pos++] = (byte)(model.get("inst" +  op + "pitchbendrange"));
            data[pos++] = (byte)(model.get("inst" +  op + "monopoly"));
            data[pos++] = (byte)(model.get("inst" +  op + "pitchmodcontrol"));
            pos++;                  // "reserved"
            }
                        
        data[data.length - 2] = (byte)produceChecksum(data, 9, 9 + 160);
                        
        return data;
        }

    /** Generate a TX81Z checksum of the data bytes */
    byte produceChecksum(byte[] bytes, int start, int end)
        {
        //      The TX81Z manual says the checksum is the
        //              "Twos complement of the lower 7 bits of the sum of all databytes".
        //
        //              Apparently this is mistaken.  Based on the function "Snapshot_Checksum" here...
        //              https://raw.githubusercontent.com/mgregory22/tx81z-programmer/master/src/snapshot.c
        //
        //              It may be otherwise.  So here's my shot.

        int checksum = 0;
        for(int i = start; i < end; i++)
            checksum = (checksum + bytes[i]) & 255;
        return (byte)((256 - checksum) & 127);
        }
    
    public byte[] requestDump(Model tempModel) 
        {
        if (tempModel == null)
            tempModel = getModel();

        // Configuration Memory XX, "Dump Requests", p. 47 User manual
        // Configuration Data Dump, #5 left column of page 14, service manual
        return new byte[] { (byte)0xF0, 0x43, 0x75, (byte)(getID() - 1), 0x20, 0x02, 
            (byte)tempModel.get("number"), (byte)0xF7 }; 
        }
        
    public byte[] requestCurrentDump()
        {
        // Current Configuration Buffer, "Dump Requests", p. 47 User manual
        // WRONG: Current Configuration Data Dump, #4 right column of page 13, service manual
        return new byte[] { (byte)0xF0, 0x43, 0x75, (byte)(getID() - 1), 0x20, 0x01, 0x00, (byte)0xF7 }; 
        }

    public static boolean recognize(byte[] data)
        {
        return 
            // current configuration buffer,
            // see bottom of page 55, user manual
            (data.length == 8 + 160 + 2 + 1 &&
            data[1] == 0x43 &&
            data[2] == 0x75 &&
            data[4] == 0x00 &&
            data[5] == 0x01 &&
            data[6] == 0x00) ||
            // configuration memory xx
            // see top of page 56, user manual
            (data.length == 8 + 160 + 2 + 1 &&
            data[1] == 0x43 &&
            data[2] == 0x75 &&
            data[4] == 0x00 &&
            data[5] == 0x02) ||
            // all configuration memory,
            // see page 56, user manual
            (data.length == 8 + 16 * (160 + 2 + 1) &&
            data[1] == 0x43 &&
            data[2] == 0x75 &&
            data[4] == 0x00 &&
            data[5] == 0x03 &&
            data[6] == 0x00);
        }
     
    public static final int MAXIMUM_NAME_LENGTH = 8;
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


    public void revise()
        {
        // check the easy stuff -- out of range parameters
        super.revise();

        String nm = model.get("name", "Init");
        String newnm = revisePatchName(nm);
        if (!nm.equals(newnm))
            model.set("name", newnm);
        }
        
    public static String getSynthName() { return "Yamaha FB-01 [Multi]"; }


    /// CHANGING PATCHES
    /// The FB01 can't change multi patches.
    ///
    /// There's a command called (8) Configuration Data Store, page 14, service manual,
    /// which sounds like it'd do that job.
    /// But it's the other direction: it stores the current configuration into a RAM slot.
    /// There's no command to load the current configuration from a RAM slot.
    ///
    /// So what to do?
    /// The two places where change patch is primarily called are in writeAllParameters()
    /// and performRequestDump().  writeAllParameters() is dealt with easily: we just
    /// call sendAllParameters() afterwards, so we've written the RAM and also copied
    /// it into the current memory.  performRequestDump() needs more explanation:
    /// we can't load the patch into current memory and then perform a request.  Instead
    /// we will have Edisyn call sendAllParameters after a non-merge parse.
                
    public boolean getSendsParametersAfterNonMergeParse() { return true; }
        
    public void writeAllParameters(Model model)
        {
        super.writeAllParameters(model);
        sendAllParameters();
        }

    public void changePatch(Model tempModel) 
        {
        // do nothing -- we have no ability to do this, see above
        }
    
    
    public String getPatchName(Model model) { return model.get("name", "INIT"); }

    public Model getNextPatchLocation(Model model)
        {
        int number = model.get("number");
        
        number++;
        if (number >= 20)
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
        
        return "" + (model.get("number") + 1);
        }
        
    public byte getID() 
        { 
        try 
            { 
            byte b = (byte)(Byte.parseByte(tuple.id));
            if (b >= 1 && b <= 16) return b;
            }
        catch (NullPointerException e) { } // expected.  Happens when tuple's not built yet
        catch (NumberFormatException e) { e.printStackTrace(); }
        return 1;
        }
        
    public String reviseID(String id)
        {
        try 
            { 
            byte b =(byte)(Byte.parseByte(id)); 
            if (b >= 1 && b <= 16) return "" + b;
            } 
        catch (NumberFormatException e) { }             // expected
        return "" + getID();
        }
        
        
    public Object adjustBankSysexForEmit(byte[] data, Model model, int bank)
        {
        data[3] = (byte)(getID() - 1);
        return data; 
        }

    public static final String[] ROM_VOICE_NAMES = new String[]
    {
    "Brass",
    "Horn",
    "Trumpet",
    "LoStrig",
    "Strings",
    "Piano",
    "NewEP",
    "EGrand",
    "Jazz Gt",
    "EBass",
    "WodBass",
    "EOrgan1",
    "EOrgan2",
    "POrgan1",
    "POrgan2",
    "Flute",
    "Picolo",
    "Oboe",
    "Clarine",
    "Glocken",
    "Vibes",
    "Xylophn",
    "Koto",
    "Zither",
    "Clav",
    "Harpsic",
    "Bells",
    "Harp",
    "SmadSyn",
    "Harmoni",
    "SteelDr",
    "Timapny",
    "LoStrg2",
    "Horn Lo",
    "Whistle",
    "zingPlp",
    "Metal",
    "Heavy",
    "FunkSyn",
    "Voices",
    "Marimba",
    "EBass2",
    "SnareDr",
    "RD Cymb",
    "Tom Tom",
    "Mars to",
    "Storm",
    "Windbel",
    "UpPiano",
    "Spiano",
    "Piano2",
    "Piano3",
    "Piano4",
    "Piano5",
    "PhGrand",
    "Grand",
    "DpGrand",
    "LPiano1",
    "LPiano22",
    "EGrand2",
    "Honkey1",
    "Honkey2",
    "Pfball",
    "PfVibe",
    "NewEP2",
    "NewEP3",
    "NewEP4",
    "NewEP5",
    "EPiano1",
    "EPiano2",
    "EPiano3",
    "EPiano4",
    "EPiano5",
    "HighTin",
    "HardTin",
    "PercPf",
    "WoodPf",
    "EPStrng",
    "EPBass",
    "Clav2",
    "Clav3",
    "Clav4",
    "FuzzClv",
    "MuteClv",
    "MuteCl2",
    "SynClv1",
    "SynClv2",
    "SynClv3",
    "SynClv4",
    "Harpsi2",
    "Harpsi3",
    "Harpsi4",
    "Harpsi5",
    "Circust",
    "Celeste",
    "Squeeze",
    "Horn2",
    "Horn3",
    "Horns",
    "Fugelh",
    "Trombon",
    "Trumpt2",
    "Brass2",
    "Brass3",
    "HardBr1",
    "HardBr2",
    "HardBr3",
    "HardBr4",
    "HuffBrs",
    "PercBr1",
    "PercBr2",
    "String1",
    "String2",
    "String3",
    "String4",
    "SoloVio",
    "RichSt1",
    "RichSt2",
    "RichSt3",
    "RichSt4",
    "Cello1",
    "Cello2",
    "LoStrg3",
    "LoStrg4",
    "LoStrg5",
    "Ochest",
    "5th Str",
    "Pizzic1",
    "Pizzic2",
    "Flute2",
    "Flute3",
    "Flute4",
    "Pan Flt",
    "SlowFlt",
    "5th Flt",
    "Oboe2",
    "Bassoon",
    "Reed",
    "Harmon2",
    "Harmon3",
    "Harmon4",
    "MonoSax",
    "Sax 1",
    "Sax 2",
    "FrnkSyn2",
    "FrnkSyn3",
    "SynOrgn",
    "SynFeed",
    "SynHarm",
    "SynClar",
    "SynLead",
    "HuffTak",
    "SoHeavy",
    "Hollow",
    "Schmooh",
    "MonoSyn",
    "Cheeky",
    "SynBell",
    "SynPluk",
    "EBass3",
    "RbBass",
    "SolBass",
    "PlukBas",
    "UprtBas",
    "Fretles",
    "FlapBs",
    "MonoBas",
    "SynBas1",
    "SynBas2",
    "SynBas3",
    "SynBas4",
    "SynBas5",
    "SynBas6",
    "SynBas7",
    "Marimb2",
    "Marimb3",
    "Xyloph2",
    "Vibe2",
    "Vibe3",
    "Glockn2",
    "TubeBe1",
    "TubeBe2",
    "Bells 2",
    "TempleG",
    "SteelDr",
    "ElectDr",
    "HandDr",
    "SynTimp",
    "clock",
    "Heiver",
    "SnareD2",
    "SnareD3",
    "JOrgan1",
    "JOrgan2",
    "COgan1",
    "COgan2",
    "EOrgan3",
    "EOrgan4",
    "EOrgan5",
    "EOrgan6",
    "EOrgan7",
    "EOrgan8",
    "SmlPipe",
    "MidPipe",
    "BigPipe",
    "SftPipe",
    "Organ",
    "Guitar",
    "Folk Gt",
    "PluckGt",
    "BriteGt",
    "Fuzz Gt",
    "Zither2",
    "Lute",
    "Banjo",
    "SftHarp",
    "Harp2",
    "Harp3",
    "SftKoto",
    "HitKoto",
    "Sitar1",
    "Sitar2",
    "HufSyn",
    "Fantasy",
    "Synvoic",
    "M.Voice",
    "VSAR",
    "Racing",
    "Water",
    "WildWar",
    "Ghostie",
    "Wave",
    "Space 1",
    "SpChime",
    "SpTalk",
    "Winds",
    "Smash",
    "Alarm",
    "Helicop",
    "SineWave"
    };
                
    }
