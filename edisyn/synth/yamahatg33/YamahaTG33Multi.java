/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.yamahatg33;

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
   A patch editor for the Yamaha TG33 [Multimode]
        
   @author Sean Luke
*/

public class YamahaTG33Multi extends Synth
    {
    public static final String[] CENTS = new String[] { "-50", "-47", "-44", "-41", "-38", "-34", "-31", "-28", "-25", "-22", "-19", "-16", "-12", "-9", "-6", "-3",
                                                        "0", "3", "6", "9", "12", "16", "19", "22", "25", "28", "31", "34", "38", "41", "44", "47", "50" };
    public static final String[] EFFECTS = new String[] 
    {
    "Reverb Hall", "Reverb Room", "Reverb Plate", "Reverb Club", 
    "Reverb Metal", "Short Single Delay (1)", "Long Delay (2)", "Long Delay (3)", 
    "Doubler", "Ping Pong Delay", "Panned Reflections", "Early Reflections", 
    "Gated Reverb", "Delay and Reverb (1)", "Delay and Reverb (2)", "Distortion and Reverb",
    };

    public static final String[] PANS = new String[] { "<<", "<", "--", ">", ">>", "Voice"};

    public static final String[] GROUP_ASSIGNS = new String[] { "32/0", "24/8", "16/16" };

    public static final String[] GROUP_OUTPUTS = new String[] { "1", "2" };

    public static final String[] GROUPS = new String[] { "1", "2" };

    public static final String[] VOICE_BANKS = new String[] { "Internal/Card", "Preset 1", "Preset 2 [TG33]" };

    public static final String[] BANKS = new String[] { "Internal", "Card 1", "Card 2" };

    public static final String[] VOICE_PRESETS = new String[] 
    {
    "SP*Pro33", "SP*Echo", "SP*BelSt", "SP*Fuji", "SP*Ice", "SP*Dandy", "SP*Arkle", "SP*BrVec", 
    "SP*Matrx", "SP*Gut", "SP*Omni", "SP*Oiled", "SP*Ace", "SP*Quire", "SP*Digit", "SP*Swell", 
    "SC:Groov", "SC*Airy", "SC*Solid", "SC*Sweep", "SC*Drops", "SC*Euro", "SC*Decay", "SC:Steel", 
    "SC*Rude", "SC*Bellz", "SC*Pluck", "SC*Glass", "SC*Wood", "SC*Wire", "SC*Cave", "SC*Wispa", 
    "SL*Sync", "SL*VCO", "SL*Chic", "SL:Mini", "SL*Wisul", "SL*Blues", "SL:Cosmo", "SL*Super", 
    "ME*Vecta", "ME*NuAge", "ME*Hil+", "ME*Glace", "ME*Astro", "E*Vger", "ME*Hitch", "ME*Indus", 
    "SE*Mount", "SE*5.PM", "SE*FlyBy", "SE*Fear", "SE:Wolvs", "SE*Hades", "SE*Neuro", "SE*Angel", 
    "SQ:MrSeq", "SQ:It", "SQ*Id", "SO*Wrapa", "SO*TG809", "SO*Devol", "DR:Kit", "DIR*EFX", 
    "EP*Arlad", "AP:Piano", "EP*Malet", "AP*ApStr", "EP:Dx6op", "EP*Pin", "EP*NewDX", "EP*Fosta", 
    "OR*Gospl", "OR*Rock", "OR*Pipe", "OR*Perc", "KY*Squez", "KY:Hrpsi", "KY*Celst", "KY:Clavi", 
    "BA*Slap", "BA*Atack", "BA*Seq", "BA*Trad", "BA:Pick", "BA*Syn", "BA:Rezz", "BA*Unisn", 
    "BA:Fingr", "BA*Frtls", "BA:Wood", "PL*Foksy", "PL*12Str", "PL*Mute", "PL*Nylon", "PL*Dist", 
    "BP*Power", "BR*Fanfr", "BR:Class", "BR*Reeds", "BR*Chill", "BR*Zeus", "BR*Moot", "BR*Anlog", 
    "BR:FrHm", "BR:Trmpt", "BR*Tromb", "WN*Sax", "WN:Pan", "WN:Oboe", "WN:Clart", "WN:Flute", 
    "ST*Arco", "ST:Chmbr", "ST*Full", "ST:Pizza", "ST*CelSt", "ST*Exel", "ST*Synth", "ST*Eroid", 
    "CH*Modrn", "CH*Duwop", "CH*Itopy", "CH*Astiz", "PC:Marim", "PC:Vibes", "PC*Bells", "PC*Clang"
    };

    public JFrame sprout()
        {
        JFrame frame = super.sprout();
        transmitTo.setEnabled(false);
        writeTo.setEnabled(false);
        return frame;
        }         

    public YamahaTG33Multi()
        {
        model.set("number", 0);
        model.set("bank", 0);
                
        /// SOUND PANEL
                
        JComponent soundPanel = new SynthPanel(this);
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        hbox.addLast(addGlobal(Style.COLOR_A()));
        vbox.add(hbox);
        vbox.add(addChannel(1, Style.COLOR_A()));
        vbox.add(addChannel(2, Style.COLOR_A()));
        vbox.add(addChannel(3, Style.COLOR_A()));
        vbox.add(addChannel(4, Style.COLOR_A()));

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Global, Channels 1-4", soundPanel);
                

        JComponent sourcePanel = new SynthPanel(this);
        vbox = new VBox();

        vbox.add(addChannel(5, Style.COLOR_A()));
        vbox.add(addChannel(6, Style.COLOR_A()));
        vbox.add(addChannel(7, Style.COLOR_A()));
        vbox.add(addChannel(8, Style.COLOR_A()));
        vbox.add(addChannel(9, Style.COLOR_A()));
        vbox.add(addChannel(10, Style.COLOR_A()));

        sourcePanel.add(vbox, BorderLayout.CENTER);
        addTab("Channels 5-10", sourcePanel);
        
        sourcePanel = new SynthPanel(this);
        vbox = new VBox();

        vbox.add(addChannel(11, Style.COLOR_A()));
        vbox.add(addChannel(12, Style.COLOR_A()));
        vbox.add(addChannel(13, Style.COLOR_A()));
        vbox.add(addChannel(14, Style.COLOR_A()));
        vbox.add(addChannel(15, Style.COLOR_A()));
        vbox.add(addChannel(16, Style.COLOR_A()));

        sourcePanel.add(vbox, BorderLayout.CENTER);
        addTab("Channels 11-16", sourcePanel);
        

        model.set("name", "UNTITLED");
        
        loadDefaults();        
        }
                
    public String getDefaultResourceFileName() { return "YamahaTG33Multi.init"; }
    public String getHTMLResourceFileName() { return "YamahaTG33Multi.html"; }

    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        JComboBox bank = new JComboBox(BANKS);
        bank.setEditable(false);
        bank.setMaximumRowCount(32);
        bank.setSelectedIndex(model.get("bank"));
                
        int original = model.get("number");
                
        JTextField number = new JTextField("" + ((original / 8 + 1) * 10 + (original % 8 + 1)), 3);

        while(true)
            {
            boolean result = showMultiOption(this, new String[] { "Bank", "Patch Number"}, 
                new JComponent[] { bank, number }, title, "Enter Patch number");
                
            if (result == false)
                return false;
                                
            int n;
            try { n = Integer.parseInt(number.getText()); }
            catch (NumberFormatException e)
                {
                showSimpleError(title, "The Patch Number must be an integer 11...28.\nDigits 9 and 0 are not permitted.");
                continue;
                }
            if (n < 1 || n > 28 || (n % 10 == 9) || (n % 10 == 0))
                {
                showSimpleError(title, "The Patch Number must be an integer 11...28.\nDigits 9 and 0 are not permitted.");
                continue;
                }
            
            n = ((n / 10) - 1) * 8 + (n % 10 - 1);
            
            change.set("bank", bank.getSelectedIndex());
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
        vbox.add(comp);  // doesn't work right :-(
        vbox.addBottom(Stretch.makeVerticalStretch()); 
        hbox.add(vbox);

        // Not enough space to show the title
        hbox.addLast(Strut.makeHorizontalStrut(80));

        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }




    public JComponent addGlobal(Color color)
        {
        Category category = new Category(this, "Common", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();

        params = EFFECTS;
        comp = new Chooser("Effect Type", this, "effecttype", params);
        vbox.add(comp);
        
        // this is called "asin" in sysex
        params = GROUP_ASSIGNS;
        comp = new Chooser("Assignment (G1/G2)", this, "assignmode", params);
        vbox.add(comp);
        
        hbox.add(vbox);
        vbox = new VBox();

        params = GROUP_OUTPUTS;
        comp = new Chooser("Group 1 Output", this, "group1output", params);
        vbox.add(comp);

        params = GROUP_OUTPUTS;
        comp = new Chooser("Group 2 Output", this, "group2output", params);
        vbox.add(comp);

        hbox.add(vbox);

        comp = new LabelledDial("Effect Balance", this, "effectbalance", color, 0, 127);
        hbox.add(comp);
        
        comp = new LabelledDial("Group 1", this, "group1effectsend", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Effect Send");
        hbox.add(comp);

        comp = new LabelledDial("Group 2", this, "group2effectsend", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Effect Send");
        hbox.add(comp);
        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
        
        
    public JComponent addChannel(final int channel, Color color)
        {
        Category category = new Category(this, "Channel " + channel, color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();

        // The bits for this are called "MED" in the sysex spec
        params = VOICE_BANKS;
        comp = new Chooser("Bank", this, "c" + channel + "bank", params);
        vbox.add(comp);

        // The bit for this is called "VSW" in the sysex spec        
        comp = new CheckBox("On", this, "c" + channel + "on");
        vbox.add(comp);
        
        hbox.add(vbox);
        vbox = new VBox();

        // The bit for this is called "1/2" or "GRP 1/2" in the sysex spec (per-channel)
        params = GROUPS;
        comp = new Chooser("Group", this, "c" + channel + "group", params);
        vbox.add(comp);

        final PushButton showButton = new PushButton("Show")
            {
            public void perform()
                {
                final YamahaTG33 synth = new YamahaTG33();
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
                                tempModel.set("number", YamahaTG33Multi.this.model.get("c" + channel + "number"));
                                int bank = YamahaTG33Multi.this.model.get("c" + channel + "bank");
                                if (bank < 3)  // internal, preset 1, preset 2
                                    {
                                    tempModel.set("bank", 0);
                                    }
                                else  // card1, card2
                                    {
                                    tempModel.set("bank", YamahaTG33Multi.this.model.get("bank"));
                                    }
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

        comp = new LabelledDial("Voice Number", this, "c" + channel + "number", color, 0, 63)
            {
            public String map(int value)
                {
                return "" + (value / 8 + 1) + (value % 8 + 1);
                }
            };
        hbox.add(comp);
        
        // 0...99, probably mapped to 0...127 again
        comp = new LabelledDial("Volume", this, "c" + channel + "volume", color, 0, 127);
        hbox.add(comp);
        
        // Probably 2's complement
        comp = new LabelledDial("Detune", this, "c" + channel + "detune", color, 0, 32, 16)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                return CENTS[value];
                }
            };
        hbox.add(comp);

        // -24...24, probably in 2's complement
        comp = new LabelledDial("Note Shift", this, "c" + channel + "noteshift", color, 0, 48, 24)
            {
            public boolean isSymmetric() { return true; }
            };
        hbox.add(comp);

        comp = new LabelledDial("Pan", this, "c" + channel +  "pan", color, 0, 5)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                return PANS[value];
                }
            };
        hbox.add(comp);
        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }



    public Object[] emitAll(String key)
        {
        //simplePause(50);  // this is needed for TX81Z, but for the DX7?  Dunno
        
        if (key.equals("number")) return new Object[0];  // this is not emittable
        if (key.equals("bank")) return new Object[0];  // this is not emittable

        byte id = (byte)(16 + getID());
        if (key.equals("name"))
            {
            String val = model.get("name","") + "        ";
            byte channel = (byte)getChannelOut();
            Object[] data = new Object[8];
            for(int i = 0; i < 8; i++)
                {
                data[i] = new byte[] { (byte)0xF0, 0x43, id, 0x26, 0x04, 0x0b, channel, 0x00, 
                    (byte)(i + 0x0d), 0x01, 0x7F, 0x00, (byte)(val.charAt(i)), (byte)0xF7 };
                }
            return data;
            }
        else
            {
            int st = 0;
            int f1 = 0x00;
            int f2 = 0;
            int b1 = 0x01;
            int b2 = 0;
            int v1 = 0;
            int v2 = 0;
            int c = 0;              // dunno what the channel number should be for global keys
                
            if (key.equals("effecttype"))
                {
                st = 0x08; f2 = 0x00; b2 = 0x7f; v2 = model.get(key);
                }
            else if (key.equals("effectbalance"))
                {
                st = 0x09; f2 = 0x01; b2 = 0x7f; v2 = model.get(key);
                }
            else if (key.equals("group1effectsend"))
                {
                st = 0x0a; f2 = 0x05; b2 = 0x7f; v2 = model.get(key);
                }
            else if (key.equals("group2effectsend"))
                {
                st = 0x0a; f2 = 0x06; b2 = 0x7f; v2 = model.get(key);
                }
            else if (key.equals("group1output"))
                {
                st = 0x07; f2 = 0x07; b2 = 0x7d; v2 = (model.get(key) == 0 ? 0 : 2);    // weird
                }
            else if (key.equals("group2output"))
                {
                st = 0x07; f2 = 0x07; b2 = 0x7e; v2 = (model.get(key) == 0 ? 0 : 1);    // weird
                }
            else if (key.equals("assignmode"))
                {
                st = 0x05; f2 = 0x15; b2 = 0x7f; v2 = model.get(key) * 8;
                }
            else
                {
                // we need to extract the midi channel
                c = StringUtility.getInt(key.substring(0, 3));
                String reduced = StringUtility.reduceFirstDigitsAfterPreamble(key, "c");
                if (reduced.equals("con"))
                    {
                    st = 0x00; f2 = 0x00; b2 = 0x77; v2 = (model.get(key) == 0 ? 0 : 8);
                    }
                else if (reduced.equals("cgroup"))
                    {
                    st = 0x06; f2 = 0x00; b2 = 0x7b; v2 = (model.get(key) == 0 ? 0 : 4);
                    }
                else if (reduced.equals("cbank"))
                    {
                    st = 0x00; f2 = 0x01; b2 = 0x7f; v2 = model.get(key);
                    }
                else if (reduced.equals("cnumber"))
                    {
                    st = 0x00; f2 = 0x02; b2 = 0x7f; v2 = model.get(key);
                    }
                else if (reduced.equals("cvolume"))
                    {
                    st = 0x01; f2 = 0x03; b2 = 0x7d; v2 = model.get(key);
                    }
                else if (reduced.equals("cdetune"))
                    {
                    int val = model.get(key);
                    val -= 16;
                    st = 0x02; f2 = 0x04; b2 = 0x7e; v1 = ((val & 255) >>> 7) & 0x01; v2 = val & 127;       // 2's complement
                    }
                else if (reduced.equals("cnoteshift"))
                    {
                    int val = model.get(key);
                    val -= 24;
                    st = 0x03; f2 = 0x05; b2 = 0x7f; v1 = ((val & 255) >>> 7) & 0x01; v2 = val & 127;       // 2's complement
                    }
                else if (reduced.equals("cpan"))
                    {
                    st = 0x04; f2 = 0x06; b2 = 0x7f; v2 = model.get(key);
                    }
                }
                
            byte[] data = new byte[] { (byte)0xF0, 0x43, id, 0x26, 0x04, (byte)st, (byte)c, (byte)f1, (byte)f2, (byte)b1, (byte)b2, (byte)v1, (byte)v2, (byte)0xF7 };
            return new Object[] { data };
            }
        }


    public int parse(byte[] data, boolean fromFile)
        {
        byte a;
        byte b;
        int pos = 16;
        b = data[pos++];
        model.set("effecttype", (b >>> 0) & 15);                // "EFFECT"
        b = data[pos++];
        model.set("effectbalance", (b >>> 0) & 127);            // EFFECT BALANCE
        pos+=3; // skip
        b = data[pos++];
        model.set("group1effectsend", (b >>> 0) & 127);          // GROUP1 EFFECT SEND
        b = data[pos++];
        model.set("group2effectsend", (b >>> 0) & 127);          // GROUP2 EFFECT SEND
        b = data[pos++];
        model.set("group1output", (b >>> 0) & 1);                       // GRP1 1/2
        model.set("group2output", (b >>> 1) & 1);                       // GRP2 1/2
        pos+=5; // skip
        char[] name = new char[8];
        for(int i = 0; i < name.length; i++)
            name[i] = (char)(data[pos++] & 127);
        model.set("name", new String(name));
        b = data[pos++];
        model.set("assignmode", (b >>> 0) & 3);                                 // ASIN
        pos+=10; // skip

        for(int c = 1; c <= 16; c++)
            {
            b = data[pos++];
            model.set("c" + c + "group", (b >>> 2) & 1);                // "GRP 1/2"
            model.set("c" + c + "on", (b >>> 3) & 1);                // "VSW"               (for "voice switch" I think)
            b = data[pos++];
            model.set("c" + c + "bank", (b >>> 0) & 3);                // "MED"             (for "media" I think)
            b = data[pos++];
            model.set("c" + c + "number", (b >>> 0) & 63);          // VOICE NUMBER
            b = data[pos++];
            model.set("c" + c + "volume", (b >>> 0) & 127);          // VOLUME
            a = data[pos++];
            b = data[pos++];
            model.set("c" + c + "detune", (byte)((b & 127) | (a << 7)) + 16);          // DETUNE
            a = data[pos++];
            b = data[pos++];
            model.set("c" + c + "noteshift", (byte)((b & 127) | (a << 7)) + 24);          // NOTE SHIFT
            b = data[pos++];
            model.set("c" + c + "pan", (b >>> 0) & 7);          // PAN
            pos+=2; // skip
            }

        revise();
        return PARSE_SUCCEEDED_UNTITLED;
        }
 
    
    public byte[] emit(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        byte[] data = new byte[226];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x43;
        data[2] = (byte)(getID());
        data[3] = (byte)0x7E;
        data[4] = (byte)(data.length / 128);
        data[5] = (byte)(data.length % 128);
        data[6] = (byte)'L';
        data[7] = (byte)'M';
        data[8] = (byte)' ';
        data[9] = (byte)' ';
        data[10] = (byte)'0';
        data[11] = (byte)'0';
        data[12] = (byte)'1';
        data[13] = (byte)'2';
        data[14] = (byte)'M';
        data[15] = (byte)'E';

        int pos = 16;
        int val;

        data[pos++] = (byte)(model.get("effecttype"));
        data[pos++] = (byte)(model.get("effectbalance"));
        pos += 3;  // skip
        data[pos++] = (byte)(model.get("group1effectsend"));
        data[pos++] = (byte)(model.get("group2effectsend"));
        data[pos++] = (byte)((model.get("group2output") << 1) | (model.get("group1output") << 0));
        pos += 5;       // skip
        String nm = model.get("name", "Untitled") + "        ";
        for(int i = 0; i < 8; i++)
            data[pos++] = (byte)(nm.charAt(i));
        data[pos++] = (byte)(model.get("assignmode"));
        pos += 10;       // skip

        for(int c = 1; c <= 16; c++)
            {
            data[pos++] = (byte)((model.get("c" + c + "on") << 3) | (model.get("c" + c + "group") << 2));
            data[pos++] = (byte)(model.get("c" + c + "bank"));
            data[pos++] = (byte)(model.get("c" + c + "number"));
            data[pos++] = (byte)(model.get("c" + c + "volume"));
            val = model.get("c" + c + "detune") - 16;
            data[pos++] = (byte)((val & 255) / 128);
            data[pos++] = (byte)((val & 255) % 128);
            val = model.get("c" + c + "noteshift") - 24;
            data[pos++] = (byte)((val & 255) / 128);
            data[pos++] = (byte)((val & 255) % 128);
            data[pos++] = (byte)(model.get("c" + c + "pan"));
            pos += 2;       // skip
            }
        
        data[data.length - 2] = produceChecksum(data, 6, data.length - 2);
        data[data.length - 1] = (byte)0xF7;                  
    
        return data;
        }


    byte produceChecksum(byte[] bytes, int start, int end)
        {
        //              Presume it's the same procedure as 4-op.
        //      The TX816 owner's manual (p. 54) says the checksum is:
        //                              "the least significant 7 bits of the 2's complement sum of 155 data bytes.
        //                               0eeeeeee must be determined so that the least significant 7 bits of the
        //                              sum of the 155 data bytes and checksum equal zero."

        int checksum = 0;
        for(int i = start; i < end; i++)
            checksum = (checksum + bytes[i]) & 127;
        return (byte)((128 - checksum) & 127);
        }

    public void performRequestDump(Model tempModel, boolean changePatch)
        {
        // We ALWAYS change the patch no matter what.  We have to.
        changePatch(tempModel);
        tryToSendSysex(requestDump(tempModel));
        }

    public byte[] requestDump(Model tempModel) 
        {
        // since performRequestDump ALWAYS changes the patch, we might
        // as well just call requestCurrentDump() here 
        return requestCurrentDump(); 
        }
    
    public byte[] requestCurrentDump()
        {
        byte id = (byte)(32 + getID());
        return new byte[] { (byte)0xF0, (byte)0x43, id, (byte)0x7E, (byte)'L', (byte)'M', (byte)' ', (byte)' ', (byte)'0', (byte)'0', (byte)'1', (byte)'2', (byte)'M', (byte)'E', (byte)0xF7 };
        }

    public static boolean recognize(byte[] data)
        {
        return  ((
                data[0] == (byte)0xF0 &&
                data[1] == (byte)0x43 &&
                // don't care about 2, it's the id
                data[3] == (byte)0x7E &&
                data[6] == (byte)'L' &&
                data[7] == (byte)'M' &&
                data[8] == (byte)' ' &&
                data[9] == (byte)' ' &&
                data[10] == (byte)'0' &&
                data[11] == (byte)'0' &&
                data[12] == (byte)'1' &&
                data[13] == (byte)'2' &&
                data[14] == (byte)'M' &&
                data[15] == (byte)'E')
//            || recognizeBulk(data)
            );
        }

/*
  public static boolean recognizeBulk(byte[] data)
  {
  return  (
  data.length == 15 + (208 * 16 + 3) && 
  data[0] == (byte)0xF0 &&
  data[1] == (byte)0x43 &&
  // don't care about 2, it's the channel
  data[3] == (byte)0x7E );
  }
*/
               
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
        
    public byte getID() 
        { 
        try 
            { 
            byte b = (byte)(Byte.parseByte(tuple.id));
            if (b > 16) b = 1;
            if (b >= 1) return b;
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
            if (b > 16) b = 1;
            if (b >= 1) return "" + b;
            } 
        catch (NumberFormatException e) { }             // expected
        return "" + getID();
        }


    public static String getSynthName() { return "Yamaha TG33 [Multi]"; }

    public void changePatch(Model tempModel) 
        {
        int bank = tempModel.get("bank");
        // Banks are stored in Edisyn as INTERNAL Card1 Card2
        // But on the TG33 the [multi] bank select data values are
        // INTERNAL=16 Card1=17 CARD2=20
        // Weird.
        final int[] bankvals = new int[] { 16, 17, 20 };
        
        // bank select 
        tryToSendMIDI(buildCC(getChannelOut(), 0, bankvals[tempModel.get("bank")]));
        tryToSendMIDI(buildPC(getChannelOut(), 64 + tempModel.get("number")));

        // we assume that we successfully did it
        if (!isMerging())  // we're actually loading the patch, not merging with it
            {
            setSendMIDI(false);
            model.set("bank", tempModel.get("bank"));
            model.set("number", tempModel.get("number"));
            setSendMIDI(true);
            }
        }
    
    public String getPatchName(Model model) { return model.get("name", "UNTITLED"); }

    public Model getNextPatchLocation(Model model)
        {
        int bank = model.get("bank");
        int number = model.get("number");
        
        number++;
        if (number >= 16)
            {
            bank++;
            number = 0;
            if (bank >= 6)
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
        
        int number = model.get("number") + 1;
        return "" + (number > 9 ? "" : "0") + number;
        }

    public byte[] adjustBankSysexForEmit(byte[] data, Model model)
        { 
        data[2] = (byte) getChannelOut();
        return data; 
        }
    }
