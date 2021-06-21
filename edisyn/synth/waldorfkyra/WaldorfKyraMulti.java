/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.waldorfkyra;

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
   A patch editor for the Waldorf Kyra (Multimode).
        
   @author Sean Luke
*/

public class WaldorfKyraMulti extends Synth
    {
    public static final String[] OUTPUTS = new String[] { "A", "B", "C", "D" };
    public static final String[] NOTES = new String[] { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
    
    public WaldorfKyraMulti()
        {
        for(int i = 0; i < parameters.length; i++)
            {
            parametersToIndex.put(parameters[i], Integer.valueOf(i));
            }

        /// SOUND PANEL
                
        JComponent soundPanel = new SynthPanel(this);
        VBox vbox = new VBox();
        
        vbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        vbox.add(addPart(1, Style.COLOR_A()));
        vbox.add(addPart(2, Style.COLOR_B()));
        vbox.add(addPart(3, Style.COLOR_A()));
        vbox.add(addPart(4, Style.COLOR_B()));

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("General, Parts 1-4", soundPanel);
                

        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        vbox.add(addPart(5, Style.COLOR_B()));
        vbox.add(addPart(6, Style.COLOR_A()));
        vbox.add(addPart(7, Style.COLOR_B()));
        vbox.add(addPart(8, Style.COLOR_A()));
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Parts 5-8", soundPanel);
        
        model.set("number", 0);
        model.set("name", "Default Multi");

        loadDefaults();        
        }
                
    public String getDefaultResourceFileName() { return "WaldorfKyraMulti.init"; }
    public String getHTMLResourceFileName() { return "WaldorfKyraMulti.html"; }

    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        JTextField number = new JTextField("" + model.get("number"), 3);
                
        while(true)
            {
            boolean result = showMultiOption(this, new String[] {"Patch Number"}, 
                new JComponent[] { number }, title, "Enter the Patch number.");
                
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
        comp = new PatchDisplay(this, 6);
        hbox.add(comp);
 
        vbox.add(hbox);
        
        comp = new StringComponent("Patch Name", this, "name", 16, "Name must be up to 16 ASCII characters.")
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
                
        globalCategory.add(vbox, BorderLayout.WEST);
        return globalCategory;
        }

    public String getPatchName(Model model) { return model.get("name", "Default Multi"); }

    public static final int MAXIMUM_NAME_LENGTH = 16;
    public String revisePatchName(String name)
        {
        name = super.revisePatchName(name);  // trim first time
        if (name.length() > MAXIMUM_NAME_LENGTH)
            name = name.substring(0, MAXIMUM_NAME_LENGTH);
        
        StringBuffer nameb = new StringBuffer(name);                            
        for(int i = 0 ; i < nameb.length(); i++)
            {
            char c = nameb.charAt(i);
            if (c < ' ' || c > 126)             // It appears that 127 (DEL) is not permitted
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
        
        String nm = model.get("name", "Default Multi");
        String newnm = revisePatchName(nm);
        if (!nm.equals(newnm))
            model.set("name", newnm);
        }
        


    public JComponent addPart(int part, Color color)
        {
        Category category = new Category(this, "Part " + part, color);
        //        category.makePasteable("part" + part);
        category.makePasteable("part");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = OUTPUTS;
        comp = new Chooser("Part Output", this, "part" + part + "outputchannel", params);
        vbox.add(comp);

        vbox.add(Stretch.makeVerticalStretch());

        comp = new PushButton("Show Patch")
            {
            public void perform()
                {
                final WaldorfKyra synth = new WaldorfKyra();
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
                                
                                tempModel.set("bank", WaldorfKyraMulti.this.model.get("part" + part + "patchbank"));
                                tempModel.set("number", WaldorfKyraMulti.this.model.get("part" + part + "patchnumber"));
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
        vbox.addLast(comp);

        hbox.add(vbox);


        vbox = new VBox();
        comp = new CheckBox("Receive Volume", this, "part" + part + "rxvolume");
        vbox.add(comp);

        comp = new CheckBox("Delay", this, "part" + part + "enabledelay");
        vbox.add(comp);

        comp = new CheckBox("Chorus", this, "part" + part + "enablemdfx");
        vbox.add(comp);

        hbox.add(vbox);


        vbox = new VBox();
        comp = new CheckBox("Receive Program", this, "part" + part + "rxprogram");
        vbox.add(comp);

        comp = new CheckBox("EQ / Formant", this, "part" + part + "enableeq");
        vbox.add(comp);

        comp = new CheckBox("Reverb", this, "part" + part + "enablereverb");
        vbox.add(comp);

        hbox.add(vbox);

        comp = new LabelledDial("MIDI", this, "part" + part + "midichannel", color, 0, 16)
            {
            public String map(int value)
                {
                if (value == 16) return "Off";
                else return "" + (value + 1);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Channel");
        hbox.add(comp);

        comp = new LabelledDial("Volume", this, "part" + part + "volume", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Pan", this, "part" + part + "pan", color, 0, 127)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                if (value < 64) return "< " + (64 - value);
                else if (value > 64) return "" + (value - 64) + " >";
                else return "--";
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Patch", this, "part" + part + "patchbank", color, 0, 25)
            {
            public String map(int value)
                {
                return "" + (char)(value + 'A');
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Bank");
        hbox.add(comp);

        comp = new LabelledDial("Patch", this, "part" + part + "patchnumber", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Number");
        hbox.add(comp);

        comp = new LabelledDial("Transpose", this, "part" + part + "transpose", color, 0, 48)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                if (value < 24) return "< " + (24 - value);
                else if (value > 24) return "" + (value - 24) + " >";
                else return "--";
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Detune", this, "part" + part + "detune", color, 0, 127)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                if (value < 64) return "< " + (64 - value);
                else if (value > 64) return "" + (value - 64) + " >";
                else return "--";
                }
            };
        hbox.add(comp);

        // Apparently MUST NOT BE 0
        comp = new LabelledDial("Low Key", this, "part" + part + "lowerkeyrange", color, 1, 127)
            {
            public String map(int value)
                {
                return NOTES[value % 12] + (value / 12 - 1);                    
                }
            };
        hbox.add(comp);

        // I'm guessing this must not be 0?
        comp = new LabelledDial("High Key", this, "part" + part + "upperkeyrange", color, 1, 127)
            {
            public String map(int value)
                {
                return NOTES[value % 12] + (value / 12 - 1);                    
                }
            };
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }






    public byte getID() 
        { 
        try 
            { 
            byte b = (byte)(Byte.parseByte(tuple.id));
            if (b >= 0) return b;
            }
        catch (NullPointerException e) { } // expected.  Happens when tuple's not built yet
        catch (NumberFormatException e) { Synth.handleException(e); }
        return 17;
        }
        
    public String reviseID(String id)
        {
        try 
            { 
            byte b =(byte)(Byte.parseByte(id)); 
            if (b >= 0) return "" + b;
            } 
        catch (NumberFormatException e) { }             // expected
        return "" + getID();
        }


    public Model getNextPatchLocation(Model model)
        {
        int number = model.get("number");
        
        number++;
        if (number >= 128)
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
        
        int number = (model.get("number"));
        return "" + ((number > 99 ? "" : (number > 9 ? "0" : "00")) + number);
        }
        


    public void changePatch(Model tempModel)
        {
        // channel out should be the multi channel, and multi program change should be turned on
        
        int number = tempModel.get("number", 0);
        tryToSendMIDI(buildPC(getChannelOut(), number));
        }

    // Change Patch can get stomped if we do a request immediately afterwards
    public int getPauseAfterChangePatch() { return 200; }

    boolean currentDump = false;
    public byte[] requestCurrentDump()
        {
        currentDump = true;
        // The documentation is not clear on this.  But I think it should be:
        
        byte[] data = new byte[9];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x3e;
        data[2] = (byte)0x22;
        data[3] = (byte)getID();
        data[4] = (byte)0x21;                   // Request Multi (8 parts)
        data[5] = (byte)0x01;                   // Current Version
        data[6] = (byte)0x7f;           // MSB > 0x00 is the edit buffer
        data[7] = (byte)0x7f;           // ignored when loading the edit buffer
        data[8] = (byte)0xF7;
        return data;
        }

    public byte[] requestDump(Model tempModel)
        {
        currentDump = false;
        if (tempModel == null)
            tempModel = getModel();

        int NN = tempModel.get("number", 0);
        
        // We need to set the model number because responses don't include it.  :-(
        model.set("number", NN);
        
        byte[] data = new byte[9];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x3e;
        data[2] = (byte)0x22;
        data[3] = (byte)getID();
        data[4] = (byte)0x21;                   // Request Multi (8 parts)
        data[5] = (byte)0x01;                   // Current Version
        data[6] = (byte)0x00;                   // MSB > 0x00 is the edit buffer
        data[7] = (byte)NN;
        data[8] = (byte)0xF7;
        return data;
        }

    int getPart(String key)
        {
        int param = ((Integer)parametersToIndex.get(key)).intValue();
        return param / 16;
        }

    int getParameterNumber(String key)
        {
        int param = ((Integer)parametersToIndex.get(key)).intValue();
        return param % 16;
        }

    public Object[] emitAll(String key)
        {
        if (key.equals("-") || key.equals("bank") || key.equals("number"))
            {
            return new Object[0];           // do nothing
            }
        else if (key.equals("name"))
            {
            String val = model.get(key, "") + "                      ";

            byte[] data = new byte[10 + 16];
            data[0] = (byte)0xF0;
            data[1] = (byte)0x3e;
            data[2] = (byte)0x22;
            data[3] = (byte)getID();
            data[4] = (byte)0x19;                   // Multi Name
            data[5] = (byte)0x01;                   // Current Version
            data[6] = (byte)0x7f;           // MSB > 0x00 is the edit buffer
            data[7] = (byte)0x7f;           // ignored when using the edit buffer
                
            for(int j = 0; j < 16; j++)
                {
                data[j + 8] = (byte)(val.charAt(j) & 127);
                }
                
            // compute checksum
            int checksum = 0;
            for(int i = 8; i < data.length - 2; i++)
                {
                checksum += data[i];
                }

            data[data.length - 2] = (byte)(checksum & 127);
            data[data.length - 1] = (byte)0xF7;
            return new Object[] { data };
            }
        else
            {
            byte[] data = new byte[10];
            data[0] = (byte)0xF0;
            data[1] = (byte)0x3e;
            data[2] = (byte)0x22;
            data[3] = (byte)getID();
            data[4] = (byte)0x11;                   // Send Part Parameter to patch Edit Buffer
            data[5] = (byte)0x01;                   // Current Version
            data[6] = (byte)((getPart(key)) & 127);
            data[7] = (byte)((getParameterNumber(key)) & 127);
            data[8] = (byte)model.get(key, 0);
            data[9] = (byte)0xF7;
            return new Object[] { data };
            }
        }

    public Object[] emitAll(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        if (tempModel == null)
            tempModel = getModel();
                
        byte NN = (byte) tempModel.get("number");
        byte BB = (byte) 0;             // Stored patch
        if (toWorkingMemory) { BB = 0x7F; }             // > 0 is edit buffer

        byte[] data = new byte[128 + 10];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x3e;
        data[2] = (byte)0x22;
        data[3] = (byte)getID();
        data[4] = (byte)0x01;                   // Send Multi           -- we must always do 0x01, not 0x41
        data[5] = (byte)0x01;                   // Current Version
        data[6] = (byte)BB;
        data[7] = (byte)NN;

        for(int i = 0; i < 128; i++)
            {
            data[i + 8] = (byte)(model.get(parameters[i], 0));
            }
                
        // compute checksum
        int checksum = 0;
        for(int i = 8; i < data.length - 2; i++)
            {
            checksum += data[i];
            }

        data[data.length - 2] = (byte)(checksum & 127);
        data[data.length - 1] = (byte)0xF7;

        // Handle name
        
        String val = model.get("name", "") + "                      ";

        byte[] data2 = new byte[10 + 16];
        data2[0] = (byte)0xF0;
        data2[1] = (byte)0x3e;
        data2[2] = (byte)0x22;
        data2[3] = (byte)getID();
        data2[4] = (byte)0x19;                  // Multi Name
        data2[5] = (byte)0x01;                  // Current Version
        data2[6] = (byte)BB;
        data2[7] = (byte)NN;

        for(int j = 0; j < 16; j++)
            {
            data2[j + 8] = (byte)(val.charAt(j) & 127);
            }
        
        // compute checksum
        checksum = 0;
        for(int i = 8; i < data2.length - 2; i++)
            {
            checksum += data2[i];
            }

        data2[data2.length - 2] = (byte)(checksum & 127);
        data2[data2.length - 1] = (byte)0xF7;
        
        return new Object[] { data, data2 };
        }

    // The Kyra doesn't load into memory when you do a single-patch write.  To be
    // safe, I'm assuming the same about multimode as well.
    public boolean getSendsParametersAfterWrite() { return true; }

    // NOTE that the Kyra Mutimode doesn't emit sysex parameters.  :-(

    public int parse(byte[] data, boolean fromFile)
        {
        // There are potentially two sysex messages in a file, so we break it up and parse both of 'em
        byte[][] cutup = cutUpSysex(data);
        int result = PARSE_FAILED;
        for(int i = 0; i < cutup.length; i++)
            {
            int res = subparse(cutup[i], fromFile);
            if (res == PARSE_SUCCEEDED_UNTITLED && result == PARSE_FAILED)
                result = PARSE_SUCCEEDED_UNTITLED;
            else if (res == PARSE_SUCCEEDED) 
                result = PARSE_SUCCEEDED;
            }
        return result;
        }

    public int subparse(byte[] data, boolean fromFile)
        {               
        // is it the name or the main data?
        if (data[4] == 0x01 || data[4] == 0x41)
            {
            // main data
  
  			// It doesn't matter what bank we're in (even the edit buffer) since there's only one bank...
            model.set("number", data[7]);

            // handle non-name parameters
            for(int i = 0; i < 128; i++)
                {
                model.set(parameters[i], data[i+8]);
                }
                        
            // Send request for name
                
            if (!fromFile)
                {
                byte[] d = new byte[9];
                d[0] = (byte)0xF0;
                d[1] = (byte)0x3e;
                d[2] = (byte)0x22;
                d[3] = (byte)getID();
                if (currentDump || data[6] == 0x7F || data[7] == 0x7F)           // FIXME: maybe we don't need the current dump?  Just rely on data[6] or data[7]?
                    {
                    d[4] = (byte)0x39;                      // Request Multi Name
                    d[5] = (byte)0x01;                      // Current Version
                    d[6] = (byte)0x7f;           // MSB > 0x00 is the edit buffer
                    d[7] = (byte)0x7f;           // ignored when loading the edit buffer
                    }
                else
                    {
                    d[4] = (byte)0x39;                      // Request Multi Name
                    d[5] = (byte)0x01;                      // Current Version
                    d[6] = (byte)0x00;                      // MSB > 0x00 is the edit buffer
                    d[7] = (byte)(model.get("number", 0));
                    }
                d[8] = (byte)0xF7;
                boolean val = getSendMIDI();
                setSendMIDI(true);
                tryToSendSysex(d);
                setSendMIDI(val);
                }
            return PARSE_SUCCEEDED_UNTITLED;     
            }
        else
            {
            // handle name
            char[] name = new char[16];
            for(int i = 0; i < 16; i++)
                {
                name[i] = (char)(data[i + 8] & 127);
                }
            model.set("name", new String(name));
            return PARSE_SUCCEEDED;     
            }
        }




    public static String getSynthName() { return "Waldorf Kyra [Multi]"; }
    
    public static final String[] basicParameters = new String[]
    {
    "outputchannel",
    "midichannel",
    "volume",
    "pan",
    "patchbank",
    "patchnumber",
    "transpose",
    "detune",
    "lowerkeyrange",
    "upperkeyrange",
    "rxvolume",
    "rxprogram",
    "enabledelay",
    "enablemdfx",
    "enableeq",
    "enablereverb",
    };

    HashMap parametersToIndex = new HashMap();
    public static final String[] parameters = new String[] 
    {
    "part1outputchannel",
    "part1midichannel",
    "part1volume",
    "part1pan",
    "part1patchbank",
    "part1patchnumber",
    "part1transpose",
    "part1detune",
    "part1lowerkeyrange",
    "part1upperkeyrange",
    "part1rxvolume",
    "part1rxprogram",
    "part1enabledelay",
    "part1enablemdfx",
    "part1enableeq",
    "part1enablereverb",
    "part2outputchannel",
    "part2midichannel",
    "part2volume",
    "part2pan",
    "part2patchbank",
    "part2patchnumber",
    "part2transpose",
    "part2detune",
    "part2lowerkeyrange",
    "part2upperkeyrange",
    "part2rxvolume",
    "part2rxprogram",
    "part2enabledelay",
    "part2enablemdfx",
    "part2enableeq",
    "part2enablereverb",
    "part3outputchannel",
    "part3midichannel",
    "part3volume",
    "part3pan",
    "part3patchbank",
    "part3patchnumber",
    "part3transpose",
    "part3detune",
    "part3lowerkeyrange",
    "part3upperkeyrange",
    "part3rxvolume",
    "part3rxprogram",
    "part3enabledelay",
    "part3enablemdfx",
    "part3enableeq",
    "part3enablereverb",
    "part4outputchannel",
    "part4midichannel",
    "part4volume",
    "part4pan",
    "part4patchbank",
    "part4patchnumber",
    "part4transpose",
    "part4detune",
    "part4lowerkeyrange",
    "part4upperkeyrange",
    "part4rxvolume",
    "part4rxprogram",
    "part4enabledelay",
    "part4enablemdfx",
    "part4enableeq",
    "part4enablereverb",
    "part5outputchannel",
    "part5midichannel",
    "part5volume",
    "part5pan",
    "part5patchbank",
    "part5patchnumber",
    "part5transpose",
    "part5detune",
    "part5lowerkeyrange",
    "part5upperkeyrange",
    "part5rxvolume",
    "part5rxprogram",
    "part5enabledelay",
    "part5enablemdfx",
    "part5enableeq",
    "part5enablereverb",
    "part6outputchannel",
    "part6midichannel",
    "part6volume",
    "part6pan",
    "part6patchbank",
    "part6patchnumber",
    "part6transpose",
    "part6detune",
    "part6lowerkeyrange",
    "part6upperkeyrange",
    "part6rxvolume",
    "part6rxprogram",
    "part6enabledelay",
    "part6enablemdfx",
    "part6enableeq",
    "part6enablereverb",
    "part7outputchannel",
    "part7midichannel",
    "part7volume",
    "part7pan",
    "part7patchbank",
    "part7patchnumber",
    "part7transpose",
    "part7detune",
    "part7lowerkeyrange",
    "part7upperkeyrange",
    "part7rxvolume",
    "part7rxprogram",
    "part7enabledelay",
    "part7enablemdfx",
    "part7enableeq",
    "part7enablereverb",
    "part8outputchannel",
    "part8midichannel",
    "part8volume",
    "part8pan",
    "part8patchbank",
    "part8patchnumber",
    "part8transpose",
    "part8detune",
    "part8lowerkeyrange",
    "part8upperkeyrange",
    "part8rxvolume",
    "part8rxprogram",
    "part8enabledelay",
    "part8enablemdfx",
    "part8enableeq",
    "part8enablereverb",
    };

    
    public boolean testVerify(Synth synth2, String key, Object obj1, Object obj2)
        {
        if (key.equals("name")) return true;		// the name gets padded with space
        else return false;
        }
    }
    
