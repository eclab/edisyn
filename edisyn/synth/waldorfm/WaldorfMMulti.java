/***
    Copyright 2022 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.waldorfm;

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
   A patch editor for the Waldorf M (Multimode).
        
   @author Sean Luke
*/

public class WaldorfMMulti extends Synth
    {
    public static final String[] BANKS = { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15" };
    public static final String[] OUTPUTS = new String[] { "A", "B", "C", "D" };
    public static final String[] NOTES = new String[] { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
    public static final String[] VELOCITY_CURVES = new String[] { "+ Linear", "- Linear", "+ Exp", "- Exp", "+ C/F", "- C/F", "+ M. Exp", "- M. Exp", "+ M. C/F", "- M. C/F" };
    public static final String[] ALLOCATOR_STEAL_TYPES = { "Mono Latest", "Mono Earliest", "Lowest", "Highest" };

    public WaldorfMMulti()
        {
        if (parametersToIndex == null)
            {
            parametersToIndex = new HashMap();
            for(int i = 0; i < parameters.length; i++)
                {
                parametersToIndex.put(parameters[i], Integer.valueOf(i));
                }
            }

        /// SOUND PANEL
                
        JComponent soundPanel = new SynthPanel(this);
        VBox vbox = new VBox();
        
        vbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        vbox.add(addPart(1, Style.COLOR_A()));
        vbox.add(addPart(2, Style.COLOR_B()));

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("General, Parts 1-2", soundPanel);
                

        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        vbox.add(addPart(3, Style.COLOR_B()));
        vbox.add(addPart(4, Style.COLOR_A()));
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Parts 3-4", soundPanel);
        
        model.set("number", 0);
        model.set("name", "Default Multi");

        loadDefaults();        
        }
                
    public String getDefaultResourceFileName() { return "WaldorfMMulti.init"; }
    public String getHTMLResourceFileName() { return "WaldorfMMulti.html"; }

    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        JTextField number = new SelectedTextField("" + (model.get("number") + 1), 3);
                
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
                showSimpleError(title, "The Patch Number must be an integer 1 ... 128");
                continue;
                }
            if (n < 1 || n > 128)
                {
                showSimpleError(title, "The Patch Number must be an integer 1 ... 128");
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
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();

        comp = new PatchDisplay(this, 6);
        hbox.add(comp);
 
        comp = new StringComponent("Patch Name", this, "name", 23, "Name must be up to 23 ASCII characters.")
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
        hbox.add(comp);
                
        globalCategory.add(hbox, BorderLayout.WEST);
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
        


    public JComponent addPart(final int part, Color color)
        {
        Category category = new Category(this, "Part " + part, color);
        //        category.makePasteable("part" + part);
        category.makePasteable("part");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();

        comp = new PushButton("Show Patch")
            {
            public void perform()
                {
                final WaldorfM synth = new WaldorfM();
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
                                                
                    synth.setTitleBarAux("[Part " + part + " of " + WaldorfMMulti.this.model.get("name", "") + "]");
                    synth.sprout();
                    JFrame frame = ((JFrame)(SwingUtilities.getRoot(synth)));
                    frame.setVisible(true);

                    SwingUtilities.invokeLater(
                        new Runnable()
                            {
                            public void run() 
                                { 
                                Model tempModel = buildModel();
                                
                                tempModel.set("bank", WaldorfMMulti.this.model.get("part" + part + "bank"));
                                tempModel.set("number", WaldorfMMulti.this.model.get("part" + part + "sound") - 1);             // FIXME: is this offset right?
                                // This will only aid the musician in updating individual parameters
                                synth.setPart(part - 1);
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


        comp = new PushButton("Update Part")
            {
            public void perform()
                {
                int channel = WaldorfMMulti.this.model.get("part" + part + "midichannel") - 1;
                int bank = WaldorfMMulti.this.model.get("part" + part + "bank");
                int number = WaldorfMMulti.this.model.get("part" + part + "sound") - 1;
                
                // It's not clear if this will work
                tryToSendMIDI(buildCC(channel, 32, bank));
                tryToSendMIDI(buildPC(channel, number));
                }
            };
        vbox.add(comp);

        comp = new CheckBox("Enable", this, "part" + part + "enable");
        vbox.add(comp);

        hbox.add(vbox);


        vbox = new VBox();
        params = OUTPUTS;
        comp = new Chooser("Routing", this, "part" + part + "routing", params);
        vbox.add(comp);

        params = VELOCITY_CURVES;
        comp = new Chooser("Velocity Curve", this, "part" + part + "velocitycurve", params);
        vbox.add(comp);

        params = ALLOCATOR_STEAL_TYPES;
        comp = new Chooser("Voice Steal Mode", this, "part" + part + "voicesteal", params);
        vbox.add(comp);
        hbox.add(vbox);


        vbox = new VBox();
        comp = new CheckBox("Panning Mod", this, "part" + part + "panningmodswitch");
        vbox.add(comp);

        comp = new CheckBox("Bank Change", this, "part" + part + "bchangefilter");
        vbox.add(comp);

        comp = new CheckBox("Program Change", this, "part" + part + "prchangefilter");
        vbox.add(comp);

        comp = new CheckBox("Pitch Bend", this, "part" + part + "pwheelfilter");
        vbox.add(comp);

        comp = new CheckBox("Mod Wheel", this, "part" + part + "mwheelfilter");
        vbox.add(comp);

        hbox.add(vbox);


        vbox = new VBox();
        comp = new CheckBox("Aftertouch", this, "part" + part + "atouchfilter");
        vbox.add(comp);

        comp = new CheckBox("Poly Aftertouch", this, "part" + part + "ppressfilter");
        vbox.add(comp);

        comp = new CheckBox("Volume CC", this, "part" + part + "volccfilter");
        vbox.add(comp);

        comp = new CheckBox("Pan CC", this, "part" + part + "panccfilter");
        vbox.add(comp);

        comp = new CheckBox("Lock VCF Control", this, "part" + part + "lockvcfcontrol");
        ((CheckBox)comp).addToWidth(1);
        vbox.add(comp);

        hbox.add(vbox);


        VBox outer = new VBox();
        HBox inner = new HBox();
                
        // Bank goes 0...15
        comp = new LabelledDial("Bank", this, "part" + part + "bank", color, 0, 15)
            {
            public String map(int value)
                {
                return BANKS[value];
                }
            };
        inner.add(comp);

        // Note goes 1...128
        comp = new LabelledDial("Number", this, "part" + part + "sound", color, 1, 128);
        inner.add(comp);

        comp = new LabelledDial("Channel", this, "part" + part + "midichannel", color, 1, 16);
        inner.add(comp);

        comp = new LabelledDial("Voices", this, "part" + part + "voicepool", color, 1, 8);
        inner.add(comp);

        comp = new LabelledDial("Volume", this, "part" + part + "volume", color, 0, 127);
        inner.add(comp);

        comp = new LabelledDial("Pan", this, "part" + part + "panning", color, -64, 63)
            {
            public String map(int value)
                {
                if (value < 0) return "< " + (0 - value);
                else if (value > 0) return "" + value + " >";
                else return "--";
                }
            };
        inner.add(comp);
        outer.add(inner);
                
        inner = new HBox();
        comp = new LabelledDial("Transpose", this, "part" + part + "transpose", color, -24, 24)
            {
            public boolean isSymmetric() { return true; }
            };
        inner.add(comp);

        comp = new LabelledDial("Detune", this, "part" + part + "detune", color, -64, 63);
        inner.add(comp);

        comp = new LabelledDial("Low Key", this, "part" + part + "keylimitlow", color, 1, 108)
            {
            public String map(int value)
                {
                return NOTES[value % 12] + (value / 12 - 1);                    
                }
            };
        inner.add(comp);

        comp = new LabelledDial("High Key", this, "part" + part + "keylimithigh", color, 1, 108)
            {
            public String map(int value)
                {
                return NOTES[value % 12] + (value / 12 - 1);                    
                }
            };
        inner.add(comp);

        comp = new LabelledDial("Low Vel", this, "part" + part + "velolimitlow", color, 1, 127);
        inner.add(comp);

        comp = new LabelledDial("High Vel", this, "part" + part + "velolimithigh", color, 1, 127);
        inner.add(comp);
        outer.add(inner);
        hbox.add(outer);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JFrame sprout()     
        {
        JFrame frame = super.sprout();
        addMMenu();
        return frame;
        }

    boolean updateScreen;

    public void addMMenu()
        {
        JMenu menu = new JMenu("M");
        menubar.add(menu);
        
        final JCheckBoxMenuItem updateScreenMenu = new JCheckBoxMenuItem("Update Screen When Changing Parameters");
        updateScreenMenu.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                updateScreen = updateScreenMenu.isSelected();
                }
            });
        menu.add(updateScreenMenu);
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
        return "A" + ((number > 99 ? "" : (number > 9 ? "0" : "00")) + number);
        }
        

    public void changePatch(Model tempModel)
        {
        // FIXME: does this work?
        
        int number = tempModel.get("number", 0);
        tryToSendMIDI(buildPC(getChannelOut(), number));
        }

/*
// Change Patch can get stomped if we do a request immediately afterwards
public int getPauseAfterChangePatch() { return 200; }
*/

    public byte[] requestCurrentDump()
        {
        byte[] data = new byte[9];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x3e;                   // Waldorf
        data[2] = (byte)0x30;                   // M
        data[3] = (byte)0x00;
        data[4] = (byte)0x75;           		// Request Multi Arrangement
        data[5] = (byte)0x01;                   // Request Current Arrangement
        data[6] = (byte)0x00;
        data[7] = (byte)0x00;
        data[8] = (byte)0xF7;
        return data;
        }

    public byte[] requestDump(Model tempModel)
        {
        if (tempModel == null)
            tempModel = getModel();

        int NN = tempModel.get("number", 0);
        
        byte[] data = new byte[9];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x3e;                   // Waldorf
        data[2] = (byte)0x30;                   // M
        data[3] = (byte)0x00;
        data[4] = (byte)0x75;           		// Request Multi Arrangement
        data[5] = (byte)0x00;                   // Request a Specific Arrangement (anything but 0x01)
        data[6] = (byte)NN;
        data[7] = (byte)0x00;
        data[8] = (byte)0xF7;
        return data;
        }


    public Object[] emitAll(String key)
        {
        if (key.equals("--") || key.equals("number") || key.equals("name"))
            {
            return new Object[0];           // do nothing
            }
        else
            {
            int pos = ((Integer)parametersToIndex.get(key)).intValue();
            int part = pos / 4;
            int param = (pos % 4) + 8192;
            int val = model.get(key, 0) + 8192;
        
            byte[] data = new byte[12];
            data[0] = (byte)0xF0;
            data[1] = (byte)0x3e;                                       // Waldorf
            data[2] = (byte)0x22;                                       // M
            data[3] = (byte)0x00;
            data[4] = (byte)0x7B;                   // Send Multi Parameter
            data[5] = (byte)part;            
            data[6] = (byte)(param & 127);
            data[7] = (byte)((param >>> 7) & 127);
            data[8] = (byte)(val & 127);
            data[9] = (byte)((val >>> 7) & 127);
            data[10] = (byte)(updateScreen ? 1 : 0);
            data[11] = (byte)0xF7;
            return new Object[] { data };
            }
        }
        
        

    public Object[] emitAll(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        if (tempModel == null)
            tempModel = getModel();
                
        byte NN = (byte) tempModel.get("number");

        byte[] data = new byte[320];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x3e;
        data[2] = (byte)0x30;
        data[3] = (byte)0x00;                                   // maybe later getID();
        data[4] = (byte)0x73;                   // Dump Multi Arrangement
        data[5] = (byte)0x00;                   // FIXME: It's unknown what value this should be

        // Handle Name
        String str = model.get("name", "") + "                       ";
        for(int i = 0; i < 23; i++)
            {
            data[i + 6] = (byte)((str.charAt(i)) & 127);
            }
        data[29] = 0x00;        // Extra padded Name byte
        data[30] = 0x00;        // Extra padded Name byte
        data[31] = 0x00;        // Extra padded Name byte
        
        // Handle Bank and Number, which are after the name oddly
        data[32] = 0x00;                                // FIXME: this is the "exact" flag. What is that?
        data[33] = (byte)NN;
        data[34] = 0x00;
        data[35] = 0x00;

        int pos = 36;
        for(int i = 0; i < parameters.length; i++)
            {
            int val = model.get(parameters[i]) + 8192;
            int lsb = (byte)(val & 0x7F);
            int msb = (byte)((val >>> 7) & 0x7F);
            data[pos] = (byte)lsb;
            data[pos + 1] = (byte)msb;
            pos += 2;
            }
                        
        // bytes 292 through 317 are 0x00
                
        data[data.length - 1] = 0x7F;
        return new Object[] { data };
        }
        

    public int parse(byte[] data, boolean fromFile)
        {
        // Handle Name
        char[] name = new char[23];
        for(int i = 0; i < 23; i++)
            {
            name[i] = (char)(data[i + 6] & 127);
            }
        model.set("name", new String(StringUtility.rightTrim(new String(name))));

        // Handle Bank and Number, which are after the name oddly
        if (data[32] == 0)               // Edit Buffer
            {
            // Might as well set the patch to 1/00
            model.set("bank", 0);
            model.set("number", 0);
            }
        else
            {
            model.set("bank", data[32] - 1);
            model.set("number", data[33]);
            }

        // Remaining parameters
        int pos = 36;
        for(int i = 0; i < parameters.length; i++)
            {
            int lsb = data[pos];
            int msb = data[pos + 1];
            model.set(parameters[i], ((msb << 7) | lsb) - 8192);
            pos += 2;
            }
                        
        return PARSE_SUCCEEDED;     
        }



    public static String getSynthName() { return "Waldorf M [Multi]"; }
    
    static HashMap parametersToIndex = null;
    
    public static final String[] parameters = new String[] 
    {
    "part1midichannel",
    "part1volume",
    "part1bank",
    "part1sound",
    "part1enable",
    "part1routing",
    "part1panning",
    "part1panningmodswitch",
    "part1keylimitlow",
    "part1keylimithigh",
    "part1velolimitlow",
    "part1velolimithigh",
    "part1transpose",
    "part1detune",
    "part1velocitycurve",
    "part1bchangefilter",
    "part1prchangefilter",
    "part1pwheelfilter",
    "part1mwheelfilter",
    "part1atouchfilter",
    "part1ppressfilter",
    "part1volccfilter",
    "part1panccfilter",
    "--",                                       //_____INSTRUMENT_SUSTAIN_FILTER, not used
    "part1voicesteal",
    "part1voicepool",
    "part1lockvcfcontrol",
    "--",                                       // RESERVED1
    "--",                                       // RESERVED2
    "--",                                       // RESERVED3
    "--",                                       // RESERVED4
    "--",                                       // RESERVED5
    "part2midichannel",
    "part2volume",
    "part2bank",
    "part2sound",
    "part2enable",
    "part2routing",
    "part2panning",
    "part2panningmodswitch",
    "part2keylimitlow",
    "part2keylimithigh",
    "part2velolimitlow",
    "part2velolimithigh",
    "part2transpose",
    "part2detune",
    "part2velocitycurve",
    "part2bchangefilter",
    "part2prchangefilter",
    "part2pwheelfilter",
    "part2mwheelfilter",
    "part2atouchfilter",
    "part2ppressfilter",
    "part2volccfilter",
    "part2panccfilter",
    "--",                                       //_____INSTRUMENT_SUSTAIN_FILTER, not used
    "part2voicesteal",
    "part2voicepool",
    "part2lockvcfcontrol",
    "--",                                       // RESERVED1
    "--",                                       // RESERVED2
    "--",                                       // RESERVED3
    "--",                                       // RESERVED4
    "--",                                       // RESERVED5
    "part3midichannel",
    "part3volume",
    "part3bank",
    "part3sound",
    "part3enable",
    "part3routing",
    "part3panning",
    "part3panningmodswitch",
    "part3keylimitlow",
    "part3keylimithigh",
    "part3velolimitlow",
    "part3velolimithigh",
    "part3transpose",
    "part3detune",
    "part3velocitycurve",
    "part3bchangefilter",
    "part3prchangefilter",
    "part3pwheelfilter",
    "part3mwheelfilter",
    "part3atouchfilter",
    "part3ppressfilter",
    "part3volccfilter",
    "part3panccfilter",
    "--",                                       //_____INSTRUMENT_SUSTAIN_FILTER, not used
    "part3voicesteal",
    "part3voicepool",
    "part3lockvcfcontrol",
    "--",                                       // RESERVED1
    "--",                                       // RESERVED2
    "--",                                       // RESERVED3
    "--",                                       // RESERVED4
    "--",                                       // RESERVED5
    "part4midichannel",
    "part4volume",
    "part4bank",
    "part4sound",
    "part4enable",
    "part4routing",
    "part4panning",
    "part4panningmodswitch",
    "part4keylimitlow",
    "part4keylimithigh",
    "part4velolimitlow",
    "part4velolimithigh",
    "part4transpose",
    "part4detune",
    "part4velocitycurve",
    "part4bchangefilter",
    "part4prchangefilter",
    "part4pwheelfilter",
    "part4mwheelfilter",
    "part4atouchfilter",
    "part4ppressfilter",
    "part4volccfilter",
    "part4panccfilter",
    "--",                                       //_____INSTRUMENT_SUSTAIN_FILTER, not used
    "part4voicesteal",
    "part4voicepool",
    "part4lockvcfcontrol",
    "--",                                       // RESERVED1
    "--",                                       // RESERVED2
    "--",                                       // RESERVED3
    "--",                                       // RESERVED4
    "--",                                       // RESERVED5
    };

    /*
      public boolean testVerify(Synth synth2, String key, Object obj1, Object obj2)
      {
      if (key.equals("name")) return true;            // the name gets padded with space
      else return false;
      }


      public String[] getPatchNumberNames()  
      { 
      return buildIntegerNames(127, 0);
      }

      public String[] getBankNames() { return new String[] { "Bank" }; }

      public boolean getSupportsPatchWrites() { return true; }

      public int getPatchNameLength() { return 16; }

      public boolean getPatchContainsLocation() { return true; }
    */
    }
    
    
