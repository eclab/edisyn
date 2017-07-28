/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth;

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

public class MicrowaveXTMulti extends Synth
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
        
    public MicrowaveXTMulti()
        {
        for(int i = 0; i < allParameters.length; i++)
            {
            allParametersToIndex.put(allParameters[i], Integer.valueOf(i));
            }

        for(int j = 1; j < 9; j++)
            {
            for(int i = 0; i < allInstrumentParameters.length; i++)
                {
                allInstrumentParametersToIndex.put(allInstrumentParameters[i] + j, Integer.valueOf(i));
                }
            }

                
        setSendsAllParametersInBulk(true);
        
        JComponent soundPanel = new SynthPanel();
        VBox vbox = new VBox();
        vbox.add(addNameGlobal(Style.COLOR_GLOBAL));
        vbox.add(addMultiData(Style.COLOR_A));

        for(int i = 1; i < 3; i++)
            {
            vbox.add(addInstrument(i, Style.COLOR_B));
            }
                
        soundPanel.add(vbox, BorderLayout.CENTER);
        tabs.addTab("Multi and Instruments 1 - 2", soundPanel);

        soundPanel = new SynthPanel();
        vbox = new VBox();

        for(int i = 3; i < 6; i++)
            {
            vbox.add(addInstrument(i, Style.COLOR_B));
            }
                
        soundPanel.add(vbox, BorderLayout.CENTER);
        tabs.addTab("Instruments 3 - 5", soundPanel);

        soundPanel = new SynthPanel();
        vbox = new VBox();

        for(int i = 6; i < 9; i++)
            {
            vbox.add(addInstrument(i, Style.COLOR_B));
            }
                
        soundPanel.add(vbox, BorderLayout.CENTER);
        tabs.addTab("Instruments 6 - 8", soundPanel);

        tabs.addTab("About", new HTMLBrowser(this.getClass().getResourceAsStream("MicrowaveXTMulti.html")));

        model.set("name", "Init            ");  // has to be 16 long
        
                        
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

    public String getDefaultResourceFileName() { return "MicrowaveXTMulti.init"; }

    public boolean gatherInfo(String title, Model change, boolean writing)
        {
        JTextField number = new JTextField("" + (model.get("number", 0) + 1), 3);
                
        while(true)
            {
            boolean result = doMultiOption(this, new String[] { "Patch Number" }, 
                new JComponent[] { number }, title, "Enter the Patch Number");
                
            if (result == false) 
                return false;
                                
            int n;
            try { n = Integer.parseInt(number.getText()); }
            catch (NumberFormatException e)
                {
                JOptionPane.showMessageDialog(null, "The Patch Number must be an integer 1 ... 128", title, JOptionPane.ERROR_MESSAGE);
                continue;
                }
            if (n < 1 || n > 128)
                {
                JOptionPane.showMessageDialog(null, "The Patch Number must be an integer 1 ... 128", title, JOptionPane.ERROR_MESSAGE);
                continue;
                }
                                
            change.set("number", n - 1);
                        
            return true;
            }
        }


    /** Add the global patch category (name, id, number, etc.) */
    public JComponent addNameGlobal(Color color)
        {
        Category globalCategory = new Category("Waldorf Microwave II/XT/XTk Multi", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
                
        VBox vbox = new VBox();
        HBox hbox2 = new HBox();
        comp = new PatchDisplay(this, "Patch", "bank", "number", 4)
            {
            public String numberString(int number) { number += 1; return ( number > 99 ? "" : (number > 9 ? "0" : "00")) + number; }
            public String bankString(int bank) { return BANKS[bank]; }
            };
        hbox2.add(comp);
        vbox.add(hbox2);
        hbox.add(vbox);
        
        comp = new StringComponent("Patch Name", this, "name", 16, "Name must be up to 16 ASCII characters.")
            {
            public String replace(String val)
            	{
            	return reviseName(val);
            	}
                                
            public void update(String key, Model model)
                {
                super.update(key, model);
                updateTitle();
                }
            };
        model.setImmutable("name", true);
        hbox.add(comp);
                
        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }

        

    public JComponent addMultiData(Color color)
        {
        Category category = new Category("General", color);
                
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
        //((LabelledDial)comp).setSecondLabel("Tempo");
        hbox.add(comp);
        
        category.add(hbox, BorderLayout.WEST);
        return category;
        }


    public JComponent addInstrument(final int inst, Color color)
        {
        Category category = new Category("Instrument " + inst, color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();

        comp = new LabelledDial("Bank", this, "bank" + inst, color, 0, 1)
            {
            public String map(int val)
                {
                String[] vals = BANKS;
                return vals[val];
                }
            };
        model.setImmutable("bank" + inst, true);
        ((LabelledDial)comp).setSecondLabel(" ");
        vbox.add(comp);


        comp = new LabelledDial("Number", this, "number" + inst, color, 0, 127, -1);
        model.setImmutable("number" + inst, true);
        vbox.add(comp);
        
        hbox.add(vbox);


        vbox = new VBox();

        params = PAN_MOD;
        comp = new Chooser("Pan Mod", this, "panmod" + inst, params);
        vbox.add(comp);

        comp = new CheckBox("Active", this, "status" + inst);
        vbox.add(comp);    

        comp = new CheckBox("Sub Out", this, "output" + inst);
        vbox.add(comp);    

        comp = new CheckBox("Reset Arp on Start", this, "arpreset" + inst);
        vbox.add(comp);
        hbox.add(vbox);
                
        comp = new CheckBox("MIDI Send [XTk]", this, "midisend" + inst);
        vbox.add(comp);    

                
        HBox hbox2 = new HBox();
        comp = new PushButton("Show")
            {
            public void perform()
                {
                final MicrowaveXT synth = new MicrowaveXT();
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
                                tempModel.set("bank", MicrowaveXTMulti.this.model.get("bank" + inst, 0));
                                tempModel.set("number", MicrowaveXTMulti.this.model.get("number" + inst, 0));
                                synth.tryToSendSysex(synth.requestDump(tempModel));
                                }
                            });
                    }
                }
            };
        hbox2.addLast(comp);
        vbox.add(hbox2);


        vbox = new VBox();
        params = ARPEGGIATOR_ACTIVE;
        comp = new Chooser("Arp Active", this, "arp" + inst, params);
        vbox.add(comp);
        
        params = ARPEGGIATOR_DIRECTION;
        comp = new Chooser("Arp Direction", this, "arpdirection" + inst, params);
        vbox.add(comp);
        
        params = ARPEGGIATOR_ORDER;
        comp = new Chooser("Arp Note Order", this, "arporder" + inst, params);
        vbox.add(comp);
        
        params = ARPEGGIATOR_VELOCITY;
        comp = new Chooser("Arp Velocity", this, "arpvel" + inst, params);
        vbox.add(comp);

        hbox.add(vbox);
        
        
        vbox = new VBox();
        hbox2 = new HBox();

        comp = new LabelledDial("Volume", this, "volume" + inst, color, 0, 127);
        hbox2.add(comp);

        comp = new LabelledDial("Transpose", this, "transpose" + inst, color, 16, 112, 64);
        hbox2.add(comp);

        comp = new LabelledDial("Detune", this, "detune" + inst, color, 0, 127, 64);
        hbox2.add(comp);

        comp = new LabelledDial("Panning", this, "panning"  + inst, color, 0, 127, 64)
            {
            public String map(int val)
                {
                if ((val - 64) < 0) return "L " + Math.abs(val - 64);
                else if ((val - 64) > 0) return "R " + (val - 64);
                else return "--";
                }
            };
        hbox2.add(comp);
        
        comp = new LabelledDial("Highest", this, "hivel" + inst, color, 1, 127);
        ((LabelledDial)comp).setSecondLabel("Velocity");
        hbox2.add(comp);

        comp = new LabelledDial("Highest", this, "hikey" + inst, color, 0, 127)
            {
            public String map(int val)
                {
                return KEYS[val % 12] + (val / 12 - 2);  // note integer division
                }
            };
        ((LabelledDial)comp).setSecondLabel("Key");
        hbox2.add(comp);

        comp = new LabelledDial("MIDI", this, "channel" + inst, color, 0, 17)
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
        model.setMetricMin( "channel" + inst, 2);
        ((LabelledDial)comp).setSecondLabel("Channel");
        hbox2.add(comp);

        vbox.add(hbox2);
        hbox2 = new HBox();
                
        comp = new LabelledDial("Arp", this, "arpclock" + inst, color, 0, 15)
            {
            public String map(int val)
                {
                return ARP_CLOCK[val];
                }
            };
        ((LabelledDial)comp).setSecondLabel("Clock");
        hbox2.add(comp);

        comp = new LabelledDial("Arp", this, "arprange" + inst, color, 1, 10);
        ((LabelledDial)comp).setSecondLabel("Range");
        hbox2.add(comp);

        comp = new LabelledDial("Arp", this, "arppattern" + inst, color, 0, 16)
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
        model.setMetricMin( "arppattern" + inst, 2);
        ((LabelledDial)comp).setSecondLabel("Pattern");
        hbox2.add(comp);

        comp = new LabelledDial("Arp", this, "arpnotesout" + inst, color, 0, 18)
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
        model.setMetricMax( "arpnotesout" + inst, 16);
        ((LabelledDial)comp).setSecondLabel("Notes Out");
        hbox2.add(comp);
        
        comp = new LabelledDial("Lowest", this, "lowvel" + inst, color, 1, 127);
        ((LabelledDial)comp).setSecondLabel("Velocity");
        hbox2.add(comp);

        comp = new LabelledDial("Lowest", this, "lowkey" + inst, color, 0, 127)
            {
            public String map(int val)
                {
                return KEYS[val % 12] + (val / 12 - 2);  // note integer division
                }
            };
        ((LabelledDial)comp).setSecondLabel("Key");
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
    
    
    "bank1",
    "number1",
    "channel1",
    "volume1",
    "transpose1",
    "detune1",
    "output1",
    "status1",
    "panning1",
    "panmod1",
    "-",
    "-",
    "lowvel1",
    "hivel1",
    "lowkey1",
    "hikey1",
    "arp1",
    "arpclock1",
    "arprange1",
    "arppattern1",
    "arpdirection1",
    "arporder1",
    "arpvel1",
    "arpreset1",
    "arpnotesout1",
    "-",
    "midisend1",
    "-",

    "bank2",
    "number2",
    "channel2",
    "volume2",
    "transpose2",
    "detune2",
    "output2",
    "status2",
    "panning2",
    "panmod2",
    "-",
    "-",
    "lowvel2",
    "hivel2",
    "lowkey2",
    "hikey2",
    "arp2",
    "arpclock2",
    "arprange2",
    "arppattern2",
    "arpdirection2",
    "arporder2",
    "arpvel2",
    "arpreset2",
    "arpnotesout2",
    "-",
    "midisend2",
    "-",


    "bank3",
    "number3",
    "channel3",
    "volume3",
    "transpose3",
    "detune3",
    "output3",
    "status3",
    "panning3",
    "panmod3",
    "-",
    "-",
    "lowvel3",
    "hivel3",
    "lowkey3",
    "hikey3",
    "arp3",
    "arpclock3",
    "arprange3",
    "arppattern3",
    "arpdirection3",
    "arporder3",
    "arpvel3",
    "arpreset3",
    "arpnotesout3",
    "-",
    "midisend3",
    "-",


    "bank4",
    "number4",
    "channel4",
    "volume4",
    "transpose4",
    "detune4",
    "output4",
    "status4",
    "panning4",
    "panmod4",
    "-",
    "-",
    "lowvel4",
    "hivel4",
    "lowkey4",
    "hikey4",
    "arp4",
    "arpclock4",
    "arprange4",
    "arppattern4",
    "arpdirection4",
    "arporder4",
    "arpvel4",
    "arpreset4",
    "arpnotesout4",
    "-",
    "midisend4",
    "-",


    "bank5",
    "number5",
    "channel5",
    "volume5",
    "transpose5",
    "detune5",
    "output5",
    "status5",
    "panning5",
    "panmod5",
    "-",
    "-",
    "lowvel5",
    "hivel5",
    "lowkey5",
    "hikey5",
    "arp5",
    "arpclock5",
    "arprange5",
    "arppattern5",
    "arpdirection5",
    "arporder5",
    "arpvel5",
    "arpreset5",
    "arpnotesout5",
    "-",
    "midisend5",
    "-",


    "bank6",
    "number6",
    "channel6",
    "volume6",
    "transpose6",
    "detune6",
    "output6",
    "status6",
    "panning6",
    "panmod6",
    "-",
    "-",
    "lowvel6",
    "hivel6",
    "lowkey6",
    "hikey6",
    "arp6",
    "arpclock6",
    "arprange6",
    "arppattern6",
    "arpdirection6",
    "arporder6",
    "arpvel6",
    "arpreset6",
    "arpnotesout6",
    "-",
    "midisend6",
    "-",


    "bank7",
    "number7",
    "channel7",
    "volume7",
    "transpose7",
    "detune7",
    "output7",
    "status7",
    "panning7",
    "panmod7",
    "-",
    "-",
    "lowvel7",
    "hivel7",
    "lowkey7",
    "hikey7",
    "arp7",
    "arpclock7",
    "arprange7",
    "arppattern7",
    "arpdirection7",
    "arporder7",
    "arpvel7",
    "arpreset7",
    "arpnotesout7",
    "-",
    "midisend7",
    "-",


    "bank8",
    "number8",
    "channel8",
    "volume8",
    "transpose8",
    "detune8",
    "output8",
    "status8",
    "panning8",
    "panmod8",
    "-",
    "-",
    "lowvel8",
    "hivel8",
    "lowkey8",
    "hikey8",
    "arp8",
    "arpclock8",
    "arprange8",
    "arppattern8",
    "arpdirection8",
    "arporder8",
    "arpvel8",
    "arpreset8",
    "arpnotesout8",
    "-",
    "midisend8",
    "-"

    };



    public byte[] emit(String key)
        {
        if (key.equals("number")) return new byte[0];  // this is not emittable
        byte DEV = (byte)(getID());
                
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
                
            byte XX = (byte)model.get(key, 0);
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
    
    
    
    
    public byte[] emit(Model tempModel, boolean toWorkingMemory)
        {
        if (tempModel == null)
            tempModel = getModel();
        byte DEV = (byte)(getID());
        byte NN = (byte) tempModel.get("number", 0);
        byte BB = 0x0;
        if (toWorkingMemory) { BB = 0x20; NN = 0x0; }
        
        String name = model.get("name", "Init Sound V1.1 ") + "                ";  // has to be 16 long
                                
        byte[] bytes = new byte[256];
        
        for(int i = 0; i < 256; i++)
            {
            String key = allParameters[i];

            if (key.equals("name"))
                {
                bytes[i] = (byte)(name.charAt(i - 16));
                }
            else
                {
                bytes[i] = (byte)(model.get(key, 0));
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
        //System.err.println("Checksum pre " + ((byte)(b & (byte)127)));


        // Section 2.12 says that the checksum includes BB and NN.
        // But this produces a checksum error on the Microwave XT!
        // Additionally Section 1 says that the checksum is the
        // "sum of all databytes", and in its format it's clear that
        // BB and NN (location info) are NOT databytes.  Not including BB
        // and NN below produces valid dumps.

        //b += bb;
        //b += nn;  // I *think* signed will work
        
        
        b = (byte)(b & (byte)127);
        //System.err.println("Checksum post " + b);
        
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
        byte NN = (byte)tempModel.get("number", 0);
        //(BB + NN)&127 is checksum
        return new byte[] { (byte)0xF0, 0x3E, 0x0E, DEV, 0x01, BB, NN, (byte)((BB + NN)&127), (byte)0xF7 };
        }
        
    public byte[] requestCurrentDump(Model tempModel)
        {
        // In Section 2.21 of sysex document, MULR is declared to be 0x11, but then in the
        // format example, it's written as 0x01.  It's actually 0x01.
                
        if (tempModel == null)
            tempModel = getModel();
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
    public String reviseName(String name)
    	{
    	name = super.reviseName(name);  // trim first time
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
		return super.reviseName(name);  // trim again
    	}

        
        

    /** Verify that all the parameters are within valid values, and tweak them if not. */
    public void revise()
        {
        // check the easy stuff -- out of range parameters
        super.revise();

		String nm = model.get("name", "Init");
		String newnm = reviseName(nm);
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
        

    public boolean parse(byte[] data, boolean ignorePatch)
        {
        // In Section 2.22 of sysex document, MULD is declared to be 0x21, but then in the
        // format example, it's written as 0x11.  It's actually 0x11, though we don't check for it here.
                
        boolean retval = true;
        if (!ignorePatch && data[5] < 8)  // or < 1 ? Anyway, otherwise it's probably just local patch data.  Too bad they do this. :-(
            {
            model.set("number", data[6]);
            }
        else
            {
            //model.set("number", 0);
            retval = false;
            }

        for(int i = 0; i < 255; i++)
            {
            setParameterByIndex(i, data[i + 7]);
            }
        revise();  
        updateMode();
        return false;  // we're never in sync because we can't move the patch number     
        }

    public static String getSynthName() { return "Waldorf Microwave II/XT/XTk [Multi]"; }
    
    public String getPatchName() { return model.get("name", "Init Sound V1.1 "); }

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
    	catch (NumberFormatException e) { }		// expected
    	return "" + getID();
    	}
                
    }
