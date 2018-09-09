/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.waldorfmicrowavext;

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
   A patch editor for the Waldorf Microwave XT.  Does not deal with Multi mode, global parameters,
   modifying wavetables, or uploading samples.  Only Single mode patches.
        
   @author Sean Luke
*/

public class WaldorfMicrowaveXTMulti extends Synth
    {
    // NOTES:
    // Sound Arp?
    // "Alternating"?
    // Is the dump length really the same as the dump length for the XT?


    /// Various collections of parameter names for pop-up menus
        
    static final String[] KEYS = new String[] { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
    static final String[] BANKS = new String[] { "A", "B" };
    static final String[] PAN_MOD = new String[] { "Off", "On", "Inverse" };
    static final String[] ARPEGGIATOR_ACTIVE = new String[] { "Off", "On", "Hold", "Sound Arp" };
    static final String[] ARP_CLOCK = new String[] { "1/1", "1/2 .", "1/2 T", "1/2", "1/4 .", "1/4 T", "1/4", "1/8 .", "1/8 T", "1/8", "1/16 .", "1/16 T", "1/16", "1/32 .", "1/32 T", "1/32"};
    static final String[] ARPEGGIATOR_DIRECTION = new String[] { "Up", "Down", "Alternating", "Random" };  // is it Alternating?
    static final String[] ARPEGGIATOR_ORDER = new String[] { "By Note", "By Note Reversed", "As Played", "As Played Reversed" };
    static final String[] ARPEGGIATOR_VELOCITY = new String[] { "Root Note", "Last Note" }; 
    static final String[] MIDI_SEND = new String[] { "Global", "Specific" };
        
    public WaldorfMicrowaveXTMulti()
        {
        for(int i = 0; i < allParameters.length; i++)
            {
            allParametersToIndex.put(allParameters[i], Integer.valueOf(i));
            }

        for(int j = 1; j < 9; j++)
            {
            for(int i = 0; i < allInstrumentParameters.length; i++)
                {
                allInstrumentParametersToIndex.put("inst" + j + allInstrumentParameters[i], Integer.valueOf(i));
                }
            }
        
        JComponent soundPanel = new SynthPanel(this);
        VBox vbox = new VBox();
        vbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        vbox.add(addMultiData(Style.COLOR_A()));

        for(int i = 1; i < 3; i++)
            {
            vbox.add(addInstrument(i, Style.COLOR_B()));
            }
                
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Multi and Instruments 1 - 2", soundPanel);

        soundPanel = new SynthPanel(this);
        vbox = new VBox();

        for(int i = 3; i < 6; i++)
            {
            vbox.add(addInstrument(i, Style.COLOR_B()));
            }
                
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Instruments 3 - 5", soundPanel);

        soundPanel = new SynthPanel(this);
        vbox = new VBox();

        for(int i = 6; i < 9; i++)
            {
            vbox.add(addInstrument(i, Style.COLOR_B()));
            }
                
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Instruments 6 - 8", soundPanel);

        model.set("name", "Init            ");  // has to be 16 long
        
        model.set("number", 0);
                        
        loadDefaults();
        }
    
    public JFrame sprout()
        {
        JFrame frame = super.sprout();
        // multi-mode on the Microwave can't switch patches
        transmitTo.setEnabled(false);
        return frame;
        }         

    public void windowBecameFront() { updateMode(); }
    
    public void updateMode()
        {
        boolean send = getSendMIDI();
        setSendMIDI(true);
        byte DEV = (byte)(getID());
        // we'll send a mode dump to change the mode to Single
        tryToSendSysex(new byte[] { (byte)0xF0, 0x3E, 0x0E, DEV, 0x17, 0x01, (byte)0xF7 });
        setSendMIDI(send);
        }
               
    public void changePatch(Model tempModel)
        {
        // Not possible in Multi Mode
        }

    public String getDefaultResourceFileName() { return "WaldorfMicrowaveXTMulti.init"; }
    public String getHTMLResourceFileName() { return "WaldorfMicrowaveXTMulti.html"; }

    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        JTextField number = new JTextField("" + (model.get("number") + 1), 3);
                
        while(true)
            {
            boolean result = showMultiOption(this, new String[] { "Patch Number" }, 
                new JComponent[] { number }, title, "Enter the Patch Number");
                
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
        Category globalCategory = new Category(this, "Waldorf Microwave II/XT/XTk [Multi]", color);
        globalCategory.makeUnresettable();
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
                
        VBox vbox = new VBox();
        HBox hbox2 = new HBox();
        comp = new PatchDisplay(this, 4);
        hbox2.add(comp);
        vbox.add(hbox2);
        hbox.add(vbox);
        
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
        hbox.add(comp);
                
        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }

        

    public JComponent addMultiData(Color color)
        {
        Category category = new Category(this, "General", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
    
        params = MIDI_SEND;
        comp = new Chooser("MIDI Send [XTk]", this, "midisend", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Volume", this, "volume", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Control W", this, "controlw", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Control X", this, "controlx", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Control Y", this, "controly", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Control Z", this, "controlz", color, 0, 127);
        hbox.add(comp);


        comp = new LabelledDial("Arp Tempo", this, "arptempo", color, 1, 127)
            {
            public String map(int val)
                {
                if (val == 1)
                    return "Extern";
                else
                    {
                    return "" + (50 + (val - 2) * 2);
                    }
                }
            public int getDefaultValue() { return 37; } // 120 BPM
            };
        model.setMetricMin( "arptempo", 2);
        //((LabelledDial)comp).addAdditionalLabel("Tempo");
        hbox.add(comp);
        
        category.add(hbox, BorderLayout.WEST);
        return category;
        }


    public JComponent addInstrument(final int inst, Color color)
        {
        Category category = new Category(this, "Instrument " + inst, color);
        category.makePasteable("inst" + inst);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();

        comp = new LabelledDial("Bank", this, "inst" + inst + "bank", color, 0, 1)
            {
            public String map(int val)
                {
                String[] vals = BANKS;
                return vals[val];
                }
            };
        ((LabelledDial)comp).addAdditionalLabel(" ");
        model.removeMetricMinMax("inst" + inst + "bank");
        vbox.add(comp);


        comp = new LabelledDial("Number", this, "inst" + inst + "number", color, 0, 127, -1);
        model.removeMetricMinMax("inst" + inst + "number");
        vbox.add(comp);
        
        hbox.add(vbox);


        vbox = new VBox();

        params = PAN_MOD;
        comp = new Chooser("Pan Mod", this, "inst" + inst + "panmod", params);
        vbox.add(comp);

        comp = new CheckBox("Active", this, "inst" + inst + "status");
        vbox.add(comp);    

        comp = new CheckBox("Sub Out", this, "inst" + inst + "output");
        vbox.add(comp);    

        comp = new CheckBox("Reset Arp on Start", this, "inst" + inst + "arpreset");
        vbox.add(comp);
        hbox.add(vbox);
                
        comp = new CheckBox("MIDI Send [XTk]", this, "inst" + inst + "midisend");
        vbox.add(comp);    

                
        HBox hbox2 = new HBox();
        comp = new PushButton("Show")
            {
            public void perform()
                {
                final WaldorfMicrowaveXT synth = new WaldorfMicrowaveXT();
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
                                tempModel.set("bank", WaldorfMicrowaveXTMulti.this.model.get("inst" + inst + "bank"));
                                tempModel.set("number", WaldorfMicrowaveXTMulti.this.model.get("inst" + inst + "number"));
                                synth.tryToSendSysex(synth.requestDump(tempModel));
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


        vbox = new VBox();
        params = ARPEGGIATOR_ACTIVE;
        comp = new Chooser("Arp Active", this, "inst" + inst + "arp", params);
        vbox.add(comp);
        
        params = ARPEGGIATOR_DIRECTION;
        comp = new Chooser("Arp Direction", this, "inst" + inst + "arpdirection", params);
        vbox.add(comp);
        
        params = ARPEGGIATOR_ORDER;
        comp = new Chooser("Arp Note Order", this, "inst" + inst + "arporder", params);
        vbox.add(comp);
        
        params = ARPEGGIATOR_VELOCITY;
        comp = new Chooser("Arp Velocity", this, "inst" + inst + "arpvel", params);
        vbox.add(comp);

        hbox.add(vbox);
        
        
        vbox = new VBox();
        hbox2 = new HBox();

        comp = new LabelledDial("Volume", this, "inst" + inst + "volume", color, 0, 127);
        hbox2.add(comp);

        comp = new LabelledDial("Transpose", this, "inst" + inst + "transpose", color, 16, 112, 64);
        hbox2.add(comp);

        comp = new LabelledDial("Detune", this, "inst" + inst + "detune", color, 0, 127, 64);
        hbox2.add(comp);

        comp = new LabelledDial("Panning", this, "inst" + inst + "panning", color, 0, 127, 64)
            {
            public String map(int val)
                {
                if ((val - 64) < 0) return "L " + Math.abs(val - 64);
                else if ((val - 64) > 0) return "R " + (val - 64);
                else return "--";
                }
            };
        hbox2.add(comp);
        
        comp = new LabelledDial("Highest", this, "inst" + inst + "hivel", color, 1, 127);
        ((LabelledDial)comp).addAdditionalLabel("Velocity");
        hbox2.add(comp);

        comp = new LabelledDial("Highest", this, "inst" + inst + "hikey", color, 0, 127)
            {
            public String map(int val)
                {
                return KEYS[val % 12] + (val / 12 - 2);  // note integer division
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Key");
        hbox2.add(comp);

        comp = new LabelledDial("MIDI", this, "inst" + inst + "channel", color, 0, 17)
            {
            public String map(int val)
                {
                if (val == 0)
                    return "Omni";
                else if (val == 1)
                    return "Global";
                else return "" + (val - 1);
                }
            };
        model.setMetricMin( "inst" + inst + "channel", 2);
        ((LabelledDial)comp).addAdditionalLabel("Channel");
        hbox2.add(comp);

        vbox.add(hbox2);
        hbox2 = new HBox();
                
        comp = new LabelledDial("Arp", this, "inst" + inst + "arpclock", color, 0, 15)
            {
            public String map(int val)
                {
                return ARP_CLOCK[val];
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Clock");
        hbox2.add(comp);

        comp = new LabelledDial("Arp", this, "inst" + inst + "arprange", color, 1, 10);
        ((LabelledDial)comp).addAdditionalLabel("Range");
        hbox2.add(comp);

        comp = new LabelledDial("Arp", this, "inst" + inst + "arppattern", color, 0, 16)
            {
            public String map(int val)
                {
                if (val == 0)
                    return "Off";
                else if (val == 1)
                    return "User";
                else return "" + (val - 1);
                }
            };
        model.setMetricMin( "inst" + inst + "arppattern", 2);
        ((LabelledDial)comp).addAdditionalLabel("Pattern");
        hbox2.add(comp);

        comp = new LabelledDial("Arp", this, "inst" + inst + "arpnotesout", color, 0, 18)
            {
            public String map(int val)
                {
                if (val == 0)
                    return "Off";
                else if (val == 17)
                    return "Inst";
                else if (val == 18)
                    return "Global";
                else return "" + (val);
                }
            };
        model.setMetricMax( "inst" + inst + "arpnotesout", 16);
        model.setMetricMin( "inst" + inst + "arpnotesout", 1);
        ((LabelledDial)comp).addAdditionalLabel("Notes Out");
        hbox2.add(comp);
        
        comp = new LabelledDial("Lowest", this, "inst" + inst + "lowvel", color, 1, 127);
        ((LabelledDial)comp).addAdditionalLabel("Velocity");
        hbox2.add(comp);

        comp = new LabelledDial("Lowest", this, "inst" + inst + "lowkey", color, 0, 127)
            {
            public String map(int val)
                {
                return KEYS[val % 12] + (val / 12 - 2);  // note integer division
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Key");
        hbox2.add(comp);


        vbox.add(hbox2);
        hbox.add(vbox);

        category.add(hbox, BorderLayout.WEST);
        return category;
        }


    /** Map of parameter -> index in the allInstrumentParameters array. */
    HashMap allInstrumentParametersToIndex = new HashMap();

    final static String[] allInstrumentParameters = new String[]
    {
    "bank",
    "number",
    "channel",
    "volume",
    "transpose",
    "detune",
    "output",
    "status",
    "panning",
    "panmod",
    "-",
    "-",
    "lowvel",
    "hivel",
    "lowkey",
    "hikey",
    "arp",
    "arpclock",
    "arprange",
    "arppattern",
    "arpdirection",
    "arporder",
    "arpvel",
    "arpreset",
    "arpnotesout",
    "-",
    "midisend",
    "-"
    };




    /** Map of parameter -> index in the allParameters array. */
    HashMap allParametersToIndex = new HashMap();


    /** List of all Waldorf parameters in order.  "-" is a reserved (unused and thus unnamed) parameter. */

    /// * indicates parameters which must be handled specially due to packing
    /// that Waldorf decided to do.  :-(

    final static String[] allParameters = new String[/*256*/] 
    {
    "volume",
    "controlw",                   
    "controlx",
    "controly",
    "controlz",
    "arptempo",
    "midisend",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",                   
    "-",
    "-",
    "name",         // *
    "name",         // *
    "name",         // *
    "name",         // *
    "name",         // *
    "name",         // *
    "name",         // *
    "name",         // *
    "name",         // *
    "name",         // *
    "name",         // *
    "name",         // *
    "name",         // *
    "name",         // *
    "name",         // *
    "name",         // *
    
    
    "inst1bank",
    "inst1number",
    "inst1channel",
    "inst1volume",
    "inst1transpose",
    "inst1detune",
    "inst1output",
    "inst1status",
    "inst1panning",
    "inst1panmod",
    "-",
    "-",
    "inst1lowvel",
    "inst1hivel",
    "inst1lowkey",
    "inst1hikey",
    "inst1arp",
    "inst1arpclock",
    "inst1arprange",
    "inst1arppattern",
    "inst1arpdirection",
    "inst1arporder",
    "inst1arpvel",
    "inst1arpreset",
    "inst1arpnotesout",
    "-",
    "inst1midisend",
    "-",

    "inst2bank",
    "inst2number",
    "inst2channel",
    "inst2volume",
    "inst2transpose",
    "inst2detune",
    "inst2output",
    "inst2status",
    "inst2panning",
    "inst2panmod",
    "-",
    "-",
    "inst2lowvel",
    "inst2hivel",
    "inst2lowkey",
    "inst2hikey",
    "inst2arp",
    "inst2arpclock",
    "inst2arprange",
    "inst2arppattern",
    "inst2arpdirection",
    "inst2arporder",
    "inst2arpvel",
    "inst2arpreset",
    "inst2arpnotesout",
    "-",
    "inst2midisend",
    "-",


    "inst3bank",
    "inst3number",
    "inst3channel",
    "inst3volume",
    "inst3transpose",
    "inst3detune",
    "inst3output",
    "inst3status",
    "inst3panning",
    "inst3panmod",
    "-",
    "-",
    "inst3lowvel",
    "inst3hivel",
    "inst3lowkey",
    "inst3hikey",
    "inst3arp",
    "inst3arpclock",
    "inst3arprange",
    "inst3arppattern",
    "inst3arpdirection",
    "inst3arporder",
    "inst3arpvel",
    "inst3arpreset",
    "inst3arpnotesout",
    "-",
    "inst3midisend",
    "-",


    "inst4bank",
    "inst4number",
    "inst4channel",
    "inst4volume",
    "inst4transpose",
    "inst4detune",
    "inst4output",
    "inst4status",
    "inst4panning",
    "inst4panmod",
    "-",
    "-",
    "inst4lowvel",
    "inst4hivel",
    "inst4lowkey",
    "inst4hikey",
    "inst4arp",
    "inst4arpclock",
    "inst4arprange",
    "inst4arppattern",
    "inst4arpdirection",
    "inst4arporder",
    "inst4arpvel",
    "inst4arpreset",
    "inst4arpnotesout",
    "-",
    "inst4midisend",
    "-",


    "inst5bank",
    "inst5number",
    "inst5channel",
    "inst5volume",
    "inst5transpose",
    "inst5detune",
    "inst5output",
    "inst5status",
    "inst5panning",
    "inst5panmod",
    "-",
    "-",
    "inst5lowvel",
    "inst5hivel",
    "inst5lowkey",
    "inst5hikey",
    "inst5arp",
    "inst5arpclock",
    "inst5arprange",
    "inst5arppattern",
    "inst5arpdirection",
    "inst5arporder",
    "inst5arpvel",
    "inst5arpreset",
    "inst5arpnotesout",
    "-",
    "inst5midisend",
    "-",


    "inst6bank",
    "inst6number",
    "inst6channel",
    "inst6volume",
    "inst6transpose",
    "inst6detune",
    "inst6output",
    "inst6status",
    "inst6panning",
    "inst6panmod",
    "-",
    "-",
    "inst6lowvel",
    "inst6hivel",
    "inst6lowkey",
    "inst6hikey",
    "inst6arp",
    "inst6arpclock",
    "inst6arprange",
    "inst6arppattern",
    "inst6arpdirection",
    "inst6arporder",
    "inst6arpvel",
    "inst6arpreset",
    "inst6arpnotesout",
    "-",
    "inst6midisend",
    "-",


    "inst7bank",
    "inst7number",
    "inst7channel",
    "inst7volume",
    "inst7transpose",
    "inst7detune",
    "inst7output",
    "inst7status",
    "inst7panning",
    "inst7panmod",
    "-",
    "-",
    "inst7lowvel",
    "inst7hivel",
    "inst7lowkey",
    "inst7hikey",
    "inst7arp",
    "inst7arpclock",
    "inst7arprange",
    "inst7arppattern",
    "inst7arpdirection",
    "inst7arporder",
    "inst7arpvel",
    "inst7arpreset",
    "inst7arpnotesout",
    "-",
    "inst7midisend",
    "-",


    "inst8bank",
    "inst8number",
    "inst8channel",
    "inst8volume",
    "inst8transpose",
    "inst8detune",
    "inst8output",
    "inst8status",
    "inst8panning",
    "inst8panmod",
    "-",
    "-",
    "inst8lowvel",
    "inst8hivel",
    "inst8lowkey",
    "inst8hikey",
    "inst8arp",
    "inst8arpclock",
    "inst8arprange",
    "inst8arppattern",
    "inst8arpdirection",
    "inst8arporder",
    "inst8arpvel",
    "inst8arpreset",
    "inst8arpnotesout",
    "-",
    "inst8midisend",
    "-"

    };



    public byte[] emit(String key)
        {
        if (key.equals("number")) return new byte[0];  // this is not emittable
        byte DEV = (byte)(getID());
             
        if (key.equals("-"))
            {
            return new byte[0];
            }   
        if (key.equals("name"))
            {
            byte[] bytes = new byte[16 * 9];
            String name = model.get(key, "Init            ") + "                ";            for(int i = 0; i < 16; i++)
                                                                                                  {
                                                                                                  int index = i;
                                                                                                  byte PP = (byte)(index & 127);
                                                                                                  byte XX = (byte)(name.charAt(i));
                                                                                                  byte LL = 0x20;
                                                                                                  if (index > 32)
                                                                                                      {
                                                                                                      // In Section 2.23 of sysex document, the locations are listed as going 1...7.
                                                                                                      // It's actually 0...7
                
                                                                                                      LL = (byte)((index - 32) / 28);  // hope that's right
                                                                                                      }
                        
                                                                                                  // In Section 2.23 of sysex document, MULP is declared to be 0x20, but then in the
                                                                                                  // format example, it's written as 0x21.  It's actually 0x21.
                
                                                                                                  byte[] b = new byte[] { (byte)0xF0, 0x3E, 0x0E, DEV, 0x21, LL, PP, XX, (byte)0xF7 };
                                                                                                  System.arraycopy(b, 0, bytes, i * 9, 9);
                                                                                                  }
            return bytes;
            }
        else
            {
            int index = ((Integer)(allParametersToIndex.get(key))).intValue();

            byte PP = (byte)(index & 127);
            if (index >= 32)  // uh oh, it's an instrument parameter, handle it specially
                {
                PP = (byte)(((Integer)(allInstrumentParametersToIndex.get(key))).intValue() & 127);
                }
                
            byte XX = (byte)model.get(key);
            byte LL = 0x20;
            if (index >= 32)
                {
                LL = (byte)((index - 32) / 28);  // hope that's right
                }

            // In Section 2.23 of sysex document, MULP is declared to be 0x20, but then in the
            // format example, it's written as 0x21.  It's actually 0x21.
                
            return new byte[] { (byte)0xF0, 0x3E, 0x0E, DEV, 0x21, LL, PP, XX, (byte)0xF7 };
            }
        }
    
    
    
    
    public byte[] emit(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        if (tempModel == null)
            tempModel = getModel();
        byte DEV = (byte)(getID());
        byte NN = (byte) tempModel.get("number");
        byte BB = 0x0;
        if (toWorkingMemory) { BB = 0x20; NN = 0x0; }
        
        String name = model.get("name", "Init Sound V1.1 ") + "                ";  // has to be 16 long
                                
        byte[] bytes = new byte[256];
        
        for(int i = 0; i < 256; i++)
            {
            String key = allParameters[i];

            if (key.equals("-"))
                {
                bytes[i] = 0;
                }
            else if (key.equals("name"))
                {
                bytes[i] = (byte)(name.charAt(i - 16));
                }
            else
                {
                bytes[i] = (byte)(model.get(key));
                }
            }
                        
        // In Section 2.22 of sysex document, MULD is declared to be 0x21, but then in the
        // format example, it's written as 0x11.  It's actually 0x11.
                
                
        byte[] full = new byte[EXPECTED_SYSEX_LENGTH];
        full[0] = (byte)0xF0;
        full[1] = 0x3E;
        full[2] = 0x0E;
        full[3] = DEV;
        full[4] = 0x11;
        full[5] = BB;
        full[6] = NN;
        // next comes the MDATA, followed by all 8 IDATA slots
        System.arraycopy(bytes, 0, full, 7, bytes.length);
        full[263] = produceChecksum(BB, NN, bytes);
        full[264] = (byte)0xF7;

        return full;
        }


    /** Generate a Waldorf checksum of the data bytes */
    byte produceChecksum(byte bb, byte nn, byte[] bytes)
        {
        //      From the sysex document:
        //
        //      "Sum of all databytes truncated to 7 bits.
        //  The addition is done in 8 bit format, the result is    
        //  masked to 7 bits (00h to 7Fh). A checksum of 7Fh is
        //  always accepted as valid.
        //  IMPORTANT: the MIDI status-bytes as well as the 
        //  ID's are not used for computing the checksum."

        byte b = 0;
        for(int i = 0; i < bytes.length; i++)
            b += bytes[i];


        // Section 2.12 says that the checksum includes BB and NN.
        // But this produces a checksum error on the Microwave XT!
        // Additionally Section 1 says that the checksum is the
        // "sum of all databytes", and in its format it's clear that
        // BB and NN (location info) are NOT databytes.  Not including BB
        // and NN below produces valid dumps.

        //b += bb;
        //b += nn;  // I *think* signed will work
        
        
        b = (byte)(b & (byte)127);
        
        return b;
        }


    public byte[] requestDump(Model tempModel)
        {
        // In Section 2.21 of sysex document, MULR is declared to be 0x11, but then in the
        // format example, it's written as 0x01.  It's actually 0x01.
                
        if (tempModel == null)
            tempModel = getModel();
        byte DEV = (byte)(getID());
        byte BB = 0;  // only 1 bank
        byte NN = (byte)tempModel.get("number");
        //(BB + NN)&127 is checksum
        return new byte[] { (byte)0xF0, 0x3E, 0x0E, DEV, 0x01, BB, NN, (byte)((BB + NN)&127), (byte)0xF7 };
        }
        
    public byte[] requestCurrentDump()
        {
        // In Section 2.21 of sysex document, MULR is declared to be 0x11, but then in the
        // format example, it's written as 0x01.  It's actually 0x01.
                
        byte DEV = (byte)(getID());
        //(0x75 + 0x00)&127 is checksum
        return new byte[] { (byte)0xF0, 0x3E, 0x0E, DEV, 0x01, 0x20, 0x00, (byte)((0x20 + 0x00)&127), (byte)0xF7 };
        }

    public static boolean recognize(byte[] data)
        {
        // In Section 2.22 of sysex document, MULD is declared to be 0x21, but then in the
        // format example, it's written as 0x11.  It's actually 0x11.
                
        boolean v = (
            data.length == EXPECTED_SYSEX_LENGTH &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x3E &&
            data[2] == (byte)0x0E &&
            data[4] == (byte)0x11);
        return v;
        }
        
    
    public static final int EXPECTED_SYSEX_LENGTH = 265;
        
        


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
        




    public void setParameterByIndex(int i, byte b)
        {
        String key = allParameters[i];
        if (key.equals("-"))
            {
            // do nothing
            }
        else if (i >= 16 && i < 32)  // name
            {
            try 
                {
                String name = model.get("name", "Init            ") + "                ";;
                byte[] str = name.getBytes("US-ASCII");
                byte[] newstr = new byte[] { 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20 };
                System.arraycopy(str, 0, newstr, 0, 16);
                newstr[i - 16] = b;
                model.set("name", new String(newstr, "US-ASCII"));
                }
            catch (UnsupportedEncodingException e)
                {
                e.printStackTrace();
                }
            }
        else
            {
            model.set(key, b);
            }
        }

        
    public void parseParameter(byte[] data)
        {
        int index = -1;
        byte b = 0;
                
        // In Section 2.23 of sysex document, MULP is declared to be 0x20, but then in the
        // format example, it's written as 0x21
                
        // Section 2.23 also has incorrect index labels in its format example (skipping index 6)
        // There are thus only 9 bytes

        // is it a sysex parameter change?
        if (data[0] == (byte)0xF0 &&
            data[1] == (byte)0x3E &&
            data[2] == (byte)0x0E &&            // Microwave
            // filter by ID?  Presently I'm not
            data[4] == (byte)0x20 &&
            data[5] == 0x00 &&  // only Sound Mode Edit Bufer
            data.length == 9)
            {
            int hi = (int)(data[6] & 127);
            int lo = (int)(data[7] & 127);
             
            index = (hi << 7) | (lo);
            b = (byte)(data[8] & 127);
            setParameterByIndex(index, b);
            }
        else
            {
            // we'll put CC here later
            }
        revise();
        }
        

    public int parse(byte[] data, boolean fromFile)
        {
        // In Section 2.22 of sysex document, MULD is declared to be 0x21, but then in the
        // format example, it's written as 0x11.  It's actually 0x11, though we don't check for it here.
                
        boolean retval = true;
        if (data[5] < 8)  // or < 1 ? Anyway, otherwise it's probably just local patch data.  Too bad they do this. :-(
            {
            model.set("number", data[6]);
            }
        else
            {
            retval = false;
            }

        for(int i = 0; i < 255; i++)
            {
            setParameterByIndex(i, data[i + 7]);
            }
        revise();  
        updateMode();
        return PARSE_SUCCEEDED;   
        }

    public static String getSynthName() { return "Waldorf Microwave II/XT/XTk [Multi]"; }
    
    public String getPatchName(Model model) { return model.get("name", "Init Sound V1.1 "); }

    public byte getID() 
        { 
        try 
            { 
            byte b = (byte)(Byte.parseByte(tuple.id));
            if (b >= 0) return b;
            }
        catch (NullPointerException e) { } // expected.  Happens when tuple's not built yet
        catch (NumberFormatException e) { e.printStackTrace(); }
        return 0;
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

    public int getBulkDownloadWaitTime() { return 1000; }

    public String getPatchLocationName(Model model)
        {
        // getPatchLocationName() is called from sprout() as a test to see if we should enable
        // batch downloading.  If we haven't yet created an .init file, then parameters won't exist
        // yet and this method will bomb badly.  So we return null in this case.
        if (!model.exists("number")) return null;
        
        int number = model.get("number") + 1;
        return "M" + ( number > 99 ? "" : (number > 9 ? "0" : "00")) + number;
        }

    public boolean getSendsParametersAfterNonMergeParse()
        {
        return true;
        }
    }
