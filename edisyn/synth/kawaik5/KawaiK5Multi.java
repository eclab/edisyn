/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.kawaik5;

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
   A patch editor for the Kawai K5/K5m (multimode)
        
   @author Sean Luke
*/

public class KawaiK5Multi extends Synth
    {
    // This is much, much, MUCH shorter and simpler than the single-patch editor!
    // It's about 690 lines, compared to the single-patch editor at 4050 lines (!)
        
    public static final String[] BANKS = { "MIA", "MIB", "MIC", "MID", "MEA", "MEB", "MEC", "MED" };
    public static final String[] SINGLE_BANKS = { "A", "B", "C", "D" };
    public static final String[] MODES = { "Mix", "MIDI", "Keyboard [K5]" };
    public static final char[] LEGAL_CHARACTERS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '-', ':', '/', '*', '?', '!', '#', '*', '(', ')', '\"', '+', '.', '=', ' '};
    public static final String[] KEYS = new String[] { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
    
    public KawaiK5Multi()
        {
        /// SOUND PANEL
                
        JComponent soundPanel = new SynthPanel(this);
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        hbox.addLast(addGlobal(Style.COLOR_A()));
        vbox.add(hbox);
        vbox.add(addTrack(1,Style.COLOR_A()));
        vbox.add(addTrack(2, Style.COLOR_B()));
        vbox.add(addTrack(3,Style.COLOR_A()));

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Global, Tracks 1-3", soundPanel);
        
        
        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        
        vbox.add(addTrack(4, Style.COLOR_B()));
        vbox.add(addTrack(5,Style.COLOR_A()));
        vbox.add(addTrack(6, Style.COLOR_B()));
        vbox.add(addTrack(7,Style.COLOR_A()));

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Tracks 4-7", (SynthPanel)soundPanel);
                

        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        
        vbox.add(addTrack(8, Style.COLOR_B()));
        vbox.add(addTrack(9,Style.COLOR_A()));
        vbox.add(addTrack(10,Style.COLOR_B()));
        vbox.add(addTrack(11,Style.COLOR_A()));

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Tracks 8-11", (SynthPanel)soundPanel);


        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        
        vbox.add(addTrack(12, Style.COLOR_B()));
        vbox.add(addTrack(13,Style.COLOR_A()));
        vbox.add(addTrack(14, Style.COLOR_B()));
        vbox.add(addTrack(15,Style.COLOR_A()));
 
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Tracks 12-15", (SynthPanel)soundPanel);

        model.set("name", "INIT");  // has to be 8 long
        model.set("bank", 0);
        model.set("number", 0);
        
        loadDefaults();        
        }
                
                
    public String getDefaultResourceFileName() { return "KawaiK5Multi.init"; }
    public String getHTMLResourceFileName() { return "KawaiK5Multi.html"; }

    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        JComboBox bank = new JComboBox(BANKS);
        bank.setSelectedIndex(model.get("bank"));
        
        JTextField number = new SelectedTextField("" + (model.get("number") + 1), 3);

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
                showSimpleError(title, "The Patch Number must be an integer 1...12");
                continue;
                }
            if (n < 1 || n > 12)
                {
                showSimpleError(title, "The Patch Number must be an integer 1...12");
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
        //globalCategory.makeUnresettable();
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
                
        VBox vbox = new VBox();
        HBox hbox2 = new HBox();
        comp = new PatchDisplay(this, 9);
        hbox2.add(comp);
        vbox.add(hbox2);
        
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
        vbox.add(comp);
        hbox.add(vbox);
        
        hbox.add(Strut.makeHorizontalStrut(100));
                
        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }


    public JComponent addGlobal(Color color)
        {
        Category category = new Category(this, "Global", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();

        comp = new LabelledDial("Volume", this, "volume", color, 0, 63);
        hbox.add(comp);
                
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
        
    public JComponent addTrack(final int track, Color color)
        {
        Category category = new Category(this, "Track " + track, color);
        //        category.makePasteable("t" + track);
        category.makePasteable("t");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = SINGLE_BANKS;
        comp = new Chooser("Voice Bank", this, "t" + track + "singlebank", params);
        vbox.add(comp);

        params = MODES;
        comp = new Chooser("Mode", this, "t" + track + "mode", params);
        vbox.add(comp);
        hbox.add(vbox);
        
        final PushButton showButton = new PushButton("Show")
            {
            public void perform()
                {
                final KawaiK5 synth = new KawaiK5();
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
                                                
                    synth.setTitleBarAux("[Track " + track + " of " + KawaiK5Multi.this.model.get("name", "") + "]");
                    synth.sprout();
                    JFrame frame = ((JFrame)(SwingUtilities.getRoot(synth)));
                    frame.setVisible(true);

                    SwingUtilities.invokeLater(
                        new Runnable()
                            {
                            public void run() 
                                { 
                                Model tempModel = buildModel();
                                tempModel.set("number", KawaiK5Multi.this.model.get("t" + track + "singlenumber"));
                                int bank = KawaiK5Multi.this.model.get("t" + track + "singlebank") + 
                                    (KawaiK5Multi.this.model.get("bank") < 4 ? 0 : 4); 
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

        
        vbox = new VBox();
        comp = new CheckBox("Portamento", this, "t" + track + "portamento");
        vbox.add(comp); 

        comp = new CheckBox("Hold", this, "t" + track + "hold");
        vbox.add(comp); 

        comp = new CheckBox("Volume", this, "t" + track + "volume");
        vbox.add(comp); 

        comp = new CheckBox("Modulation", this, "t" + track + "modulation");
        vbox.add(comp); 

        comp = new CheckBox("Pitch Bend", this, "t" + track + "benderrange");
        vbox.add(comp); 
        hbox.add(vbox);
        
        vbox = new VBox();

        comp = new CheckBox("Pressure", this, "t" + track + "pressure");
        vbox.add(comp); 

        comp = new CheckBox("Expression", this, "t" + track + "expression");
        ((CheckBox)comp).addToWidth(2);
        vbox.add(comp); 

        comp = new CheckBox("Pedal", this, "t" + track + "pedal");
        vbox.add(comp); 

        comp = new CheckBox("Velocity", this, "t" + track + "velocity");
        vbox.add(comp); 

        comp = new CheckBox("Program", this, "t" + track + "program");
        vbox.add(comp); 

        hbox.add(vbox);
        
        comp = new LabelledDial("Voice Number", this, "t" + track + "singlenumber", color, 1, 12);
        hbox.add(comp);

        comp = new LabelledDial("Zone Low", this, "t" + track + "zonelow", color, 0, 127)
            {
            public String map(int val)
                {
                return KEYS[val % 12] + (val / 12 - 2);  // note integer division
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Zone High", this, "t" + track + "zonehigh", color, 0, 127)
            {
            public String map(int val)
                {
                return KEYS[val % 12] + (val / 12 - 2);  // note integer division
                }
            };
        hbox.add(comp);


        // FIXME: this one has a hole in it, 1-16 and then 255, which we'll may to 17 here
        comp = new LabelledDial("Polyphony", this, "t" + track + "polyphony", color, 0, 17)
            {
            public String map(int val)
                {
                if (val == 17)
                    return "VR";
                else return "" + val;
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Channel", this, "t" + track + "channel", color, 0, 15, -1);
        hbox.add(comp);

        comp = new LabelledDial("Velocity Low", this, "t" + track + "veloswlow", color, 0, 7);
        hbox.add(comp);

        comp = new LabelledDial("Velocity High", this, "t" + track + "veloswhigh", color, 0, 7);
        hbox.add(comp);

        // FIXME: this is two's complement 
        comp = new LabelledDial("Transpose", this, "t" + track + "transpose", color, -48, 48)
            {
            public boolean isSymmetric() { return true; }
            };
        hbox.add(comp);

        // FIXME: this is two's complement 
        comp = new LabelledDial("Tune", this, "t" + track + "tune", color, -31, 31)
            {
            public boolean isSymmetric() { return true; }
            };
        hbox.add(comp);

        comp = new LabelledDial("Output", this, "t" + track + "out", color, 0, 3, -1);
        hbox.add(comp);

        comp = new LabelledDial("Level", this, "t" + track + "level", color, 0, 63);
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
        
        
        
    public static final int POLYPHONY_VR = 17;

    public int parse(byte[] result, boolean fromFile)
        {
        model.set("bank", result[7] / 12);
        model.set("number", result[7] % 12);

        // denybblize
        byte[] data = new byte[176];

        int v = 8;
        for(int i = 0; i < data.length; i++)
            {
            data[i] = (byte)(((result[v++] & 0xF) << 4) | (result[v++] & 0xF));
            }

        int pos = 0;
        
        for(int i = 1; i < 16; i++)
            {
            model.set("t" + i + "singlebank", (data[pos] >>> 4) & 3);
            model.set("t" + i + "singlenumber", (data[pos] >>> 0) & 15);
            pos++;
            model.set("t" + i + "zonelow", (data[pos] >>> 0) & 127);
            pos++;
            model.set("t" + i + "zonehigh", (data[pos] >>> 0) & 127);
            pos++;
            int poly = (data[pos] >>> 0) & 255;
            if (poly == 255)
                model.set("t" + i + "polyphony", POLYPHONY_VR);
            else
                model.set("t" + i + "polyphony", poly);
            pos++;
            model.set("t" + i + "channel", (data[pos] >>> 4) & 15);
            model.set("t" + i + "mode", (data[pos] >>> 0) & 3);
            pos++;
            model.set("t" + i + "veloswhigh", (data[pos] >>> 4) & 7);
            model.set("t" + i + "veloswlow", (data[pos] >>> 0) & 7);
            pos++;
            model.set("t" + i + "transpose", (byte)((data[pos] >>> 0) & 255));  // 2's complement, so we cast to a byte
            pos++;
            model.set("t" + i + "tune", (byte)((data[pos] >>> 0) & 255));  // 2's complement, so we cast to a byte
            pos++;
            model.set("t" + i + "out", (data[pos] >>> 6) & 3);
            model.set("t" + i + "level", (data[pos] >>> 0) & 63);
            pos++;
            model.set("t" + i + "portamento", (data[pos] >>> 7) & 1);
            model.set("t" + i + "hold", (data[pos] >>> 6) & 1);
            model.set("t" + i + "volume", (data[pos] >>> 5) & 1);
            model.set("t" + i + "modulation", (data[pos] >>> 4) & 1);
            model.set("t" + i + "benderrange", (data[pos] >>> 3) & 1);
            model.set("t" + i + "pressure", (data[pos] >>> 2) & 1);
            model.set("t" + i + "expression", (data[pos] >>> 1) & 1);
            model.set("t" + i + "pedal", (data[pos] >>> 0) & 1);
            pos++;
            model.set("t" + i + "velocity", (data[pos] >>> 1) & 1);
            model.set("t" + i + "program", (data[pos] >>> 0) & 1);
            pos++;
            }

        // Name ...
        try
            {
            model.set("name", new String(data, pos, 8, "US-ASCII").trim());
            }
        catch (UnsupportedEncodingException ex) { } // won't happen
        pos += 8;
                
        model.set("volume", (data[pos] >>> 0) & 63);
        pos++;

        return PARSE_SUCCEEDED;                 
        }
    
        
    public boolean sendAllParametersInternal()
        {
        boolean val = super.sendAllParametersInternal();        

        // we change patch to MID-12 if we're sending in bulk.
        // for some insane reason, we must pause somewhat AFTER we have written the patch but 
        // BEFORE we change the patch to MID-12 or else it won't get
        // properly loaded into the patch.  I cannot explain it.  And it's a lot!
                                        
        simplePause(400);  // think this is the right amount -- 300 won't cut it

        Model tempModel = buildModel();
        tempModel.set("bank", 3);
        tempModel.set("number", 11);
        changePatch(tempModel);
        simplePause(getPauseAfterChangePatch());
        return val;
        }

    public byte[] emit(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        if (tempModel == null)
            tempModel = getModel();

        byte[] data = new byte[176];
                
        int pos = 0;
        
        for(int i = 1; i < 16; i++)
            {
            data[pos] = (byte)((model.get("t" + i + "singlebank") << 4) | (model.get("t" + i + "singlenumber") << 0));
            pos++;
            data[pos] = (byte)((model.get("t" + i + "zonelow") << 0));
            pos++;
            data[pos] = (byte)((model.get("t" + i + "zonehigh") << 0));
            pos++;
            int poly = model.get("t" + i + "polyphony");
            if (poly == POLYPHONY_VR) poly = 255;
            data[pos] = (byte)(poly);
            pos++;
            data[pos] = (byte)((model.get("t" + i + "channel") << 4) | (model.get("t" + i + "mode") << 0));
            pos++;
            data[pos] = (byte)((model.get("t" + i + "veloswhigh") << 4) | (model.get("t" + i + "veloswlow") << 0));
            pos++;
            data[pos] = (byte)((model.get("t" + i + "transpose") << 0));
            pos++;
            data[pos] = (byte)((model.get("t" + i + "tune") << 0));
            pos++;
            data[pos] = (byte)((model.get("t" + i + "out") << 6) | (model.get("t" + i + "level") << 0));
            pos++;
            data[pos] = (byte)((model.get("t" + i + "portamento") << 7) | 
                (model.get("t" + i + "hold") << 6) | 
                (model.get("t" + i + "volume") << 5) | 
                (model.get("t" + i + "modulation") << 4) | 
                (model.get("t" + i + "benderrange") << 3) | 
                (model.get("t" + i + "pressure") << 2) | 
                (model.get("t" + i + "expression") << 1) | 
                (model.get("t" + i + "pedal") << 0));
            pos++;
            data[pos] = (byte)((model.get("t" + i + "velocity") << 1) | 
                (model.get("t" + i + "program") << 0));
            pos++;
            }

        // Name ...
        String name = model.get("name", "INIT") + "          ";
        for(int i = 0; i < 8; i++)
            data[pos++] = (byte)(name.charAt(i));

        data[pos++] = (byte)((model.get("volume") << 0));

        /// Compute Kawai's crazy Checksum
                
        int sum = 0;
        for(int i = 0; i < data.length; i += 2)
            {
            sum += (((data[i + 1] & 0xFF) << 8) | (data[i] & 0xFF));
            }
                
        sum = sum & 0xFFFF;
        sum = (0x5A3C - sum) & 0xFFFF;

        data[pos++] = (byte)(sum & 0xFF);
        data[pos++] = (byte)((sum >>> 8) & 0xFF);
                
        // Load payload

        byte[] result = new byte[EXPECTED_SYSEX_LENGTH];
        result[0] = (byte)0xF0;
        result[1] = (byte)0x40;
        result[2] = (byte)getChannelOut();
        result[3] = (byte)0x20;
        result[4] = (byte)0x00;
        result[5] = (byte)0x02;
        result[6] = (byte)0x01;
        result[7] = (byte)(tempModel.get("bank") * 12 + (tempModel.get("number")));
        
        if (toWorkingMemory) // we're gonna write to MID-12 instead
            result[7] = (byte)(47);
                
        int v = 8;
        for(int i = 0; i < pos; i++)
            {
            result[v++] = (byte)((data[i] & 0xFF) >>> 4);
            result[v++] = (byte)(data[i] & 0xF);
            }
        result[v] = (byte)0xF7;



        return result;
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
            (byte)0x02,
            (byte)0x01,  // multi
            (byte)(tempModel.get("bank") * 12 + (tempModel.get("number"))),
            (byte)0xF7
            };
        }

        
    public static final int EXPECTED_SYSEX_LENGTH = 361;        
    
    boolean isLegalCharacter(char c)
        {
        for(int i = 0; i < LEGAL_CHARACTERS.length; i++)
            {
            if (c == LEGAL_CHARACTERS[i])
                return true;
            }
        return false;
        }
    
    public static final int MAXIMUM_NAME_LENGTH = 8;
    public String revisePatchName(String name)
        {
        name = super.revisePatchName(name);  // trim first time
        if (name.length() > MAXIMUM_NAME_LENGTH)
            name = name.substring(0, MAXIMUM_NAME_LENGTH);
        name = name.trim();
        name = name.toUpperCase();
        
        StringBuffer nameb = new StringBuffer(name);                            
        for(int i = 0 ; i < nameb.length(); i++)
            {
            if (!isLegalCharacter(nameb.charAt(i)))
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

        String nm = model.get("name", "INIT");
        String newnm = revisePatchName(nm);
        if (!nm.equals(newnm))
            model.set("name", newnm);
        }
        
    public static String getSynthName() { return "Kawai K5/K5m [Multi]"; }
    
    public String getPatchName(Model model) { return model.get("name", "INIT"); }

    public int getPauseAfterChangePatch() { return 10; }
    public int getPauseAfterSendOneParameter() { return 30; }  // Sad, needs 30ms
 
    public void changePatch(Model tempModel)
        {
        byte BB = (byte)tempModel.get("bank");
        byte NN = (byte)tempModel.get("number");
        
        /// NOTE: the K5 can't change to multi-mode with a program change, you have to send a special
        /// sysex command.  
        int PC = (BB * 12 + NN);
        try 
            {
            tryToSendSysex(new byte[] { (byte)0xF0, 0x40, (byte)getChannelOut(), 0x30, 0x00, 0x02, 0x01, (byte)PC, (byte)0xF7 } );
            }
        catch (Exception e) { Synth.handleException(e); }
        }
    
    public Model getNextPatchLocation(Model model)
        {
        int bank = model.get("bank");
        int number = model.get("number");
        
        number++;
        if (number >= 12)
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
        
        return BANKS[model.get("bank")] + "-" +  (model.get("number") + 1);
        }
        
    public JFrame sprout()
        {
        JFrame frame = super.sprout();
        // We can't request the current working memory (don't ask why)
        receiveCurrent.setEnabled(false);

        return frame;
        }         


    public String[] getBankNames() { return BANKS; }

    /** Return a list of all patch number names.  Default is { "Main" } */
    public String[] getPatchNumberNames() { return buildIntegerNames(12, 0); }

    /** Return a list whether patches in banks are writeable.  Default is { false } */
    public boolean[] getWriteableBanks() { return new boolean[] { true, true, true, true, true, true, true, true }; }

    /** Return a list whether individual patches can be written.  Default is FALSE. */
    public boolean getSupportsPatchWrites() { return true; }

    public int getPatchNameLength() { return 8; }
    }
                                        
