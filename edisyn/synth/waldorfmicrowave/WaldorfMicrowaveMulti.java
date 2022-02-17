/***
    Copyright 2021 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.waldorfmicrowave;

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
   A patch editor for the Waldorf Microwave.  Does not deal with Multi mode, global parameters,
   modifying wavetables, or uploading samples.  Only Single mode patches.
        
   @author Sean Luke
*/

public class WaldorfMicrowaveMulti extends Synth
    {
    // NOTES:
    // Sound Arp?
    // "Alternating"?
    // Is the dump length really the same as the dump length for the XT?


    /// Various collections of parameter names for pop-up menus
        
    static final String[] BANKS = new String[] { "A", "B" };
    static final String[] INSTRUMENT_BANKS = new String[] { "A", "B", "C", "D" };
    static final String[] ROUTINGS = new String[] { "L + R", "Out 1", "Out 2", "Out 3", "Out 4" };
    static final String[] PROGRAM_CHANGE_MODES = new String[] { "Multi Program Change", "Sound Program Change", "Combined" };
    static final String[] ENABLES = new String[] { "Off", "On", "Solo" };
    static final String[] VELOCITY_CURVES = new String[] { "Linear Pos", "Linear Neg", "Exp Pos", "Exp Neg", "Crossfade Pos", "Crossfade Neg", "User 1", "User 2", "User 3", "User 4" };
    static final String[] TUNING_TABLES = new String[] { "Positive", "Negative", "Slight Detune", "Honky Tonk", "User Table 1", "User Table 2", "User Table 3", "User Table 4" };
    static final String[] PAN_MOD = new String[] { "Off", "On", "Reverse" };
    static final String[] VOICE_ALLOCATION = new String[] { "Dynamic", "Retrigger", "Low Retrig", "High Retrig", "Single Trigger", "Low Single Trig", "High Single Trig" };
    static final String[] KEYS = new String[] { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
        
    public WaldorfMicrowaveMulti()
        {
        for(int i = 0; i < allParameters.length; i++)
            {
            allParametersToIndex.put(allParameters[i], Integer.valueOf(i));
            }

        JComponent soundPanel = new SynthPanel(this);
        VBox vbox = new VBox();
        
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        hbox.addLast(addGeneral(Style.COLOR_A()));
        vbox.add(hbox);
                
        for(int i = 1; i < 3; i++)
            {
            vbox.add(addInstrument(i, Style.COLOR_B()));
            }
                
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("General and Instruments 1 - 2", soundPanel);

        soundPanel = new SynthPanel(this);
        vbox = new VBox();

        for(int i = 3; i < 5; i++)
            {
            vbox.add(addInstrument(i, Style.COLOR_B()));
            }
                
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("3 - 4", soundPanel);

        soundPanel = new SynthPanel(this);
        vbox = new VBox();

        for(int i = 5; i < 7; i++)
            {
            vbox.add(addInstrument(i, Style.COLOR_B()));
            }
                
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("5 - 6", soundPanel);

        soundPanel = new SynthPanel(this);
        vbox = new VBox();

        for(int i = 7; i < 9; i++)
            {
            vbox.add(addInstrument(i, Style.COLOR_B()));
            }
                
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("7 - 8", soundPanel);

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

    //public String getDefaultResourceFileName() { return "WaldorfMicrowaveMulti.init"; }
    //public String getHTMLResourceFileName() { return "WaldorfMicrowaveMulti.html"; }

    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        JTextField number = new SelectedTextField("" + (model.get("number") + 1), 3);
                
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
        Category globalCategory = new Category(this, "Waldorf Microwave I [Multi]", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
                
        VBox vbox = new VBox();

        comp = new PatchDisplay(this, 4);
        vbox.add(comp);
        hbox.add(vbox);
                
        vbox = new VBox();
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
        vbox.add(comp);
        hbox.add(vbox);
                
        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }

        

    public JComponent addGeneral(Color color)
        {
        Category category = new Category(this, "General", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
    
        params = PROGRAM_CHANGE_MODES;
        comp = new Chooser("Program Change Mode", this, "programchangemode", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Volume", this, "mastervolumeofarrangement", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Control W", this, "midicontrollerw", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Control X", this, "midicontrollerx", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Control Y", this, "midicontrollery", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Control Z", this, "midicontrollerz", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Instrument Count", this, "instrumentcount", color, 0, 7, -1);
        hbox.add(comp);

        category.add(hbox, BorderLayout.WEST);
        return category;
        }


    public JComponent addInstrument(final int inst, Color color)
        {
        Category category = new Category(this, "Instrument " + inst, color);
        category.makePasteable("instrument");
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();

        VBox vbox = new VBox();
                
        final PushButton showButton = new PushButton("Show")
            {
            public void perform()
                {
                final WaldorfMicrowave synth = new WaldorfMicrowave();
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
                                                
                    synth.setTitleBarAux("[Inst " + inst + " of " + WaldorfMicrowaveMulti.this.model.get("name", "") + "]");
                    synth.sprout();
                    JFrame frame = ((JFrame)(SwingUtilities.getRoot(synth)));
                    frame.setVisible(true);                                 

                    SwingUtilities.invokeLater(
                        new Runnable()
                            {
                            public void run() 
                                { 
                                Model tempModel = buildModel();
                                tempModel.set("bank", WaldorfMicrowaveMulti.this.model.get("instrument" + inst + "bank"));
                                tempModel.set("number", WaldorfMicrowaveMulti.this.model.get("instrument" + inst + "number"));
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


        comp = new LabelledDial("Sound", this, "instrument" + inst + "sound", color, 0, 127)
            {
            public String map(int value)
                {
                int num = value % 32;
                return INSTRUMENT_BANKS[value / 32] + (num < 10 ? "0" : "") + num;
                }
                
            public void update(String key, Model model)
                {
                super.update(key, model);
                // >= 64 are banks C or D, which are cartridge banks and cannot be directly loaded
                showButton.getButton().setEnabled(model.get(key) < 64);                 
                }
            };
            
        model.removeMetricMinMax("instrument" + inst + "sound");
        vbox.add(comp);
        vbox.add(showButton);

        params = ENABLES;
        comp = new Chooser("Enable", this, "instrument" + inst + "enable", params);
        vbox.add(comp);

        comp = new CheckBox("Pan Mod", this, "instrument" + inst + "panningmod");
        vbox.add(comp);    

        hbox.add(vbox);
        vbox = new VBox();  

        params = ROUTINGS;
        comp = new Chooser("Routing", this, "instrument" + inst + "routing", params);
        vbox.add(comp);

        params = VELOCITY_CURVES;
        comp = new Chooser("Velocity Curve", this, "instrument" + inst + "velocitycurve", params);
        vbox.add(comp);

        params = TUNING_TABLES;
        comp = new Chooser("Tuning Table", this, "instrument" + inst + "tuningtable", params);
        vbox.add(comp);

        params = VOICE_ALLOCATION;
        comp = new Chooser("Voice Allocation", this, "instrument" + inst + "voiceallocationmode", params);
        vbox.add(comp);

        hbox.add(vbox);
        vbox = new VBox();  

        comp = new CheckBox("Program Change", this, "instrument" + inst + "programchangefilter");
        vbox.add(comp);    

        comp = new CheckBox("Pitch Wheel", this, "instrument" + inst + "pitchwheelfilter");
        vbox.add(comp);    

        comp = new CheckBox("Mod Wheel", this, "instrument" + inst + "modulationwheelfilter");
        vbox.add(comp);    

        comp = new CheckBox("Sustain Pedal", this, "instrument" + inst + "sustainpedalfilter");
        vbox.add(comp);    

        hbox.add(vbox);
        vbox = new VBox();  

        comp = new CheckBox("Aftertouch", this, "instrument" + inst + "aftertouchfilter");
        vbox.add(comp);    

        comp = new CheckBox("Poly Aftertouch", this, "instrument" + inst + "polypressurefilter");
        vbox.add(comp);    

        comp = new CheckBox("Volume Controller", this, "instrument" + inst + "volumecontrollerfilter");
        vbox.add(comp);    

        comp = new CheckBox("Pan Controller", this, "instrument" + inst + "panningcontrollerfilter");
        vbox.add(comp);    
        hbox.add(vbox);
                
        vbox = new VBox();
        HBox hbox2 = new HBox();
        comp = new LabelledDial("MIDI", this, "instrument" + inst + "midichannel", color, 0, 15)
            {
            public String map(int value)
                {
                return "" + (1 + value);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Channel");
        hbox2.add(comp);
        
        comp = new LabelledDial("Volume", this, "instrument" + inst + "volume", color, 0, 127);
        hbox2.add(comp);

        comp = new LabelledDial("Transpose", this, "instrument" + inst + "transposeoffset", color, 0, 48, 24)
            {
            public boolean isSymmetric() { return true; }
            };
        ((LabelledDial)comp).addAdditionalLabel("Offset");
        hbox2.add(comp);
        
        comp = new LabelledDial("Panning", this, "instrument" + inst + "panning", color, 0, 127)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val)
                {
                if (val < 64) return "< " + (64 - val);
                else if (val > 64) return "" + (val - 64) + ">";
                else return "--";
                }
            };
        hbox2.add(comp);

        comp = new LabelledDial("Detune", this, "instrument" + inst + "detune", color, 0, 127, 64);
        hbox2.add(comp);

        vbox.add(hbox2);
        hbox2 = new HBox();
                
        comp = new LabelledDial("Low", this, "instrument" + inst + "keylimitlow", color, 0, 127)
            {
            public String map(int val)
                {
                return KEYS[val % 12] + (val / 12 - 2);  // note integer division
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Key");
        hbox2.add(comp);

        comp = new LabelledDial("High", this, "instrument" + inst + "keylimithigh", color, 0, 127)
            {
            public String map(int val)
                {
                return KEYS[val % 12] + (val / 12 - 2);  // note integer division
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Key");
        hbox2.add(comp);

        comp = new LabelledDial("Low", this, "instrument" + inst + "velocitylimitlow", color, 1, 127);
        ((LabelledDial)comp).addAdditionalLabel("Velocity");
        hbox2.add(comp);

        comp = new LabelledDial("High", this, "instrument" + inst + "velocitylimithigh", color, 1, 127);
        ((LabelledDial)comp).addAdditionalLabel("Velocity");
        hbox2.add(comp);

        vbox.add(hbox2);
        hbox.add(vbox);
               
        category.add(hbox, BorderLayout.WEST);
        return category;
        }



    /** Map of parameter -> index in the allParameters array. */
    HashMap allParametersToIndex = new HashMap();


    /** List of all Waldorf parameters in order.  "-" is a reserved (unused and thus unnamed) parameter. */

    /// * indicates parameters which must be handled specially due to packing
    /// that Waldorf decided to do.  :-(

    final static String[] allParameters = new String[/*256*/] 
    {
    "mastervolumeofarrangement",
    "midicontrollerw",                   
    "midicontrollerx",
    "midicontrollery",
    "midicontrollerz",
    "programchangemode",
    "instrumentcount",
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
    
    "instrument1enable",
    "instrument1midichannel",
    "instrument1sound",
    "instrument1keylimitlow",
    "instrument1keylimithigh",
    "instrument1velocitylimitlow",
    "instrument1velocitylimithigh",
    "instrument1velocitycurve",
    "instrument1transposeoffset",
    "instrument1detune",
    "instrument1tuningtable",
    "instrument1volume",
    "instrument1panning",
    "instrument1panningmod",
    "instrument1routing",
    "instrument1programchangefilter",
    "instrument1pitchwheelfilter",
    "instrument1modulationwheelfilter",
    "instrument1aftertouchfilter",
    "instrument1polypressurefilter",
    "instrument1volumecontrollerfilter",
    "instrument1panningcontrollerfilter",
    "instrument1sustainpedalfilter",
    "instrument1voiceallocationmode",
    "-",

    "instrument2enable",
    "instrument2midichannel",
    "instrument2sound",
    "instrument2keylimitlow",
    "instrument2keylimithigh",
    "instrument2velocitylimitlow",
    "instrument2velocitylimithigh",
    "instrument2velocitycurve",
    "instrument2transposeoffset",
    "instrument2detune",
    "instrument2tuningtable",
    "instrument2volume",
    "instrument2panning",
    "instrument2panningmod",
    "instrument2routing",
    "instrument2programchangefilter",
    "instrument2pitchwheelfilter",
    "instrument2modulationwheelfilter",
    "instrument2aftertouchfilter",
    "instrument2polypressurefilter",
    "instrument2volumecontrollerfilter",
    "instrument2panningcontrollerfilter",
    "instrument2sustainpedalfilter",
    "instrument2voiceallocationmode",
    "-",

    "instrument3enable",
    "instrument3midichannel",
    "instrument3sound",
    "instrument3keylimitlow",
    "instrument3keylimithigh",
    "instrument3velocitylimitlow",
    "instrument3velocitylimithigh",
    "instrument3velocitycurve",
    "instrument3transposeoffset",
    "instrument3detune",
    "instrument3tuningtable",
    "instrument3volume",
    "instrument3panning",
    "instrument3panningmod",
    "instrument3routing",
    "instrument3programchangefilter",
    "instrument3pitchwheelfilter",
    "instrument3modulationwheelfilter",
    "instrument3aftertouchfilter",
    "instrument3polypressurefilter",
    "instrument3volumecontrollerfilter",
    "instrument3panningcontrollerfilter",
    "instrument3sustainpedalfilter",
    "instrument3voiceallocationmode",
    "-",

    "instrument4enable",
    "instrument4midichannel",
    "instrument4sound",
    "instrument4keylimitlow",
    "instrument4keylimithigh",
    "instrument4velocitylimitlow",
    "instrument4velocitylimithigh",
    "instrument4velocitycurve",
    "instrument4transposeoffset",
    "instrument4detune",
    "instrument4tuningtable",
    "instrument4volume",
    "instrument4panning",
    "instrument4panningmod",
    "instrument4routing",
    "instrument4programchangefilter",
    "instrument4pitchwheelfilter",
    "instrument4modulationwheelfilter",
    "instrument4aftertouchfilter",
    "instrument4polypressurefilter",
    "instrument4volumecontrollerfilter",
    "instrument4panningcontrollerfilter",
    "instrument4sustainpedalfilter",
    "instrument4voiceallocationmode",
    "-",

    "instrument5enable",
    "instrument5midichannel",
    "instrument5sound",
    "instrument5keylimitlow",
    "instrument5keylimithigh",
    "instrument5velocitylimitlow",
    "instrument5velocitylimithigh",
    "instrument5velocitycurve",
    "instrument5transposeoffset",
    "instrument5detune",
    "instrument5tuningtable",
    "instrument5volume",
    "instrument5panning",
    "instrument5panningmod",
    "instrument5routing",
    "instrument5programchangefilter",
    "instrument5pitchwheelfilter",
    "instrument5modulationwheelfilter",
    "instrument5aftertouchfilter",
    "instrument5polypressurefilter",
    "instrument5volumecontrollerfilter",
    "instrument5panningcontrollerfilter",
    "instrument5sustainpedalfilter",
    "instrument5voiceallocationmode",
    "-",

    "instrument6enable",
    "instrument6midichannel",
    "instrument6sound",
    "instrument6keylimitlow",
    "instrument6keylimithigh",
    "instrument6velocitylimitlow",
    "instrument6velocitylimithigh",
    "instrument6velocitycurve",
    "instrument6transposeoffset",
    "instrument6detune",
    "instrument6tuningtable",
    "instrument6volume",
    "instrument6panning",
    "instrument6panningmod",
    "instrument6routing",
    "instrument6programchangefilter",
    "instrument6pitchwheelfilter",
    "instrument6modulationwheelfilter",
    "instrument6aftertouchfilter",
    "instrument6polypressurefilter",
    "instrument6volumecontrollerfilter",
    "instrument6panningcontrollerfilter",
    "instrument6sustainpedalfilter",
    "instrument6voiceallocationmode",
    "-",

    "instrument7enable",
    "instrument7midichannel",
    "instrument7sound",
    "instrument7keylimitlow",
    "instrument7keylimithigh",
    "instrument7velocitylimitlow",
    "instrument7velocitylimithigh",
    "instrument7velocitycurve",
    "instrument7transposeoffset",
    "instrument7detune",
    "instrument7tuningtable",
    "instrument7volume",
    "instrument7panning",
    "instrument7panningmod",
    "instrument7routing",
    "instrument7programchangefilter",
    "instrument7pitchwheelfilter",
    "instrument7modulationwheelfilter",
    "instrument7aftertouchfilter",
    "instrument7polypressurefilter",
    "instrument7volumecontrollerfilter",
    "instrument7panningcontrollerfilter",
    "instrument7sustainpedalfilter",
    "instrument7voiceallocationmode",
    "-",

    "instrument8enable",
    "instrument8midichannel",
    "instrument8sound",
    "instrument8keylimitlow",
    "instrument8keylimithigh",
    "instrument8velocitylimitlow",
    "instrument8velocitylimithigh",
    "instrument8velocitycurve",
    "instrument8transposeoffset",
    "instrument8detune",
    "instrument8tuningtable",
    "instrument8volume",
    "instrument8panning",
    "instrument8panningmod",
    "instrument8routing",
    "instrument8programchangefilter",
    "instrument8pitchwheelfilter",
    "instrument8modulationwheelfilter",
    "instrument8aftertouchfilter",
    "instrument8polypressurefilter",
    "instrument8volumecontrollerfilter",
    "instrument8panningcontrollerfilter",
    "instrument8sustainpedalfilter",
    "instrument8voiceallocationmode",
    "-",


    };




    public Object[] emitAll(String key)
        {
        if (key.equals("bank")) return new Object[0];  // this is not emittable
        if (key.equals("number")) return new Object[0];  // this is not emittable

        int instrument = 0;             // need to change this
        byte id = (byte)getID();
                
        if (key.equals("name"))
            {
            char[] name = (model.get("name", "Untitled") + "                ").toCharArray();
            Object[] obj = new Object[16];
            for(int i = 0; i < obj.length; i++)
                {
                int param = 10 + i;
                // nybblize param number
                int HH = (param >>> 4) & 15;
                int LL = param & 15;

                byte[] data = new byte[] 
                    { 
                    (byte)0xF0, 
                    0x3E,                           // Waldorf
                    0x00,                           // Microwave 1 
                    id,
                    0x61,                           // RTAR (Real Time Arrangement Edit)
                    (byte)HH,
                    (byte)LL,
                    (byte)(byte)name[i],
                    0,      // checksum
                    (byte)0xF7 
                    };
                data[8] = produceChecksum(data, 5, 8);
                                
                obj[i] = data;
                }
            return obj;
            }
        else
            {
            int param = ((Integer)(allParametersToIndex.get(key))).intValue();
            int val = model.get(key);
                        
            // nybblize param number
            int HH = (param >>> 4) & 15;
            int LL = param & 15;
                        
            byte[] data = new byte[] 
                { 
                (byte)0xF0, 
                0x3E,                           // Waldorf
                0x00,                           // Microwave 1 
                id,
                0x61,                           // RTAR (Real Time Arrangement Edit)
                (byte)HH,
                (byte)LL,
                (byte)val,
                0,      // checksum
                (byte)0xF7 
                };
            data[9] = produceChecksum(data, 5, 8);
            
            return new Object[] { data };
            }
        }
    
    
    
    
    public Object[] emitAll(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        // It's not clear if we can do a write at all, or if we can only do a send.
        // If we can do a write,
        // We need to issue a Sound Dump (BPRD) or Arrangement Sound Program Dump (ABPD)
        // Followed by a Store BPR (STRB)
        
        if (tempModel == null)
            tempModel = getModel();
            
        byte DEV = (byte)(getID());
        byte NN = (byte) tempModel.get("number");
        byte BB = (byte) tempModel.get("bank");
        
        byte[] bytes = new byte[233];
        
        bytes[0] = (byte)0xF0;          
        bytes[1] = (byte)0x3E;          // Waldorf
        bytes[2] = (byte)0x00;          // Microwave 1
        bytes[3] = DEV;
        bytes[4] = (byte)0x43;          // ARPD
        
        char[] name = (model.get("name", "Untitled") + "                ").toCharArray();
        
        for(int i = 0; i < allParameters.length; i++)
            {
            String key = allParameters[i];
            int val = model.get(key);
            if (key.equals("-"))
                {
                val = 0;
                }
            else if (key.equals("name"))
                {
                val = (byte)name[i - 15 + 5];
                }

            bytes[i + 5] = (byte)val;
            }
        
        bytes[14] = 0x55;                       // "valid flag"
        bytes[231] = produceChecksum(bytes, 5, 231);
        bytes[232] = (byte)0xF7;
        
        return new Object[] { bytes };
        }


    /** Generate a Waldorf checksum of the data bytes */
    byte produceChecksum(byte[] bytes, int start, int end)
        {
        // From the sysex document:
        //
        // CHKSUM    : Sum of all data bytes truncated to 7 bits. The addition is done
        //             in 8 bit format, the result is masked to 7 bits (00h to 7Fh).
        //             IMPORTANT: The MIDI status bytes as well as the ID's are not
        //                        used for computing the checksum.
        //                        If there are no data bytes in the message (simple
        //                        request), the checksum will always be 00h.

        byte b = 0;
        for(int i = start; i < end; i++)
            b += bytes[i];
        return (byte)(b & (byte)127);
        }

    public int getPauseAfterChangePatch()
        {
        // perhaps just a smidgen?
        return 50;
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



    public void parseParameter(byte[] data)
        {
        if (data.length == 11 &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x3E &&
            data[2] == (byte)0x00 &&
            data[4] == (byte)0x61)              // RTAR Real Time BPR Edit          
            {
            int param = ((data[6] & 15) << 4) | (data[7] & 15);
            String key = allParameters[param];
            int val = data[8];
            
            if (key.equals("-"))
                {
                System.err.println("WARNING WaldorfMicrowaveMulti.parseParameter(): key was - for param " + param);
                return;
                }
            else if (key.equals("name"))
                {
                char[] name = (model.get("name", "Untitled") + "                ").toCharArray();
                name[param - 15 + 5] = (char)val;
                model.set("name", String.valueOf(name).trim());
                }
            else
                {
                model.set(key, val);
                }
// should we do this?
//              revise();
            }
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
        


    public int parse(byte[] data, boolean fromFile)
        {
        int index = -1;
        byte b = 0;
                
        if (data.length == 14471)  // bulk
            {
            // extract names
            char[][] names = new char[64][16];          // both "banks"
            for(int i = 0; i < 64; i++)
                {
                for (int j = 0; j < 16; j++)
                    {
                    names[i][j] = (char)(data[226 * i + j + 10]);
                    }
                }

            String[] n = new String[64];
            for(int i = 0; i < 64; i++)
                {
                if (i < 32)
                    {
                    n[i] = "A" + (i < 9 ? "0" : "") + (i + 1) + "  " + String.valueOf(names[i]);
                    }
                else
                    {
                    n[i] = "B" + (i < 41 ? "0" : "") + (i - 32 + 1) + "  " + String.valueOf(names[i]);
                    }
                }
             
            // Now that we have an array of names, one per patch, we present the user with options;
            // 0. Cancel [handled automatically]
            // 1. Save the bank data [handled automatically]
            // 2. Upload the bank data [handled automatically] 
            // 3. Load and edit a certain patch number
            int patchNum = showBankSysexOptions(data, n);
            if (patchNum < 0) return PARSE_CANCELLED;
            else return extractPatch(data, 5 + 226 * patchNum, patchNum);
            }
        else return extractPatch(data, 5, -1);
        }
        
        
    // Extracts a single patch from either single or "bank" patch data
    public int extractPatch(byte[] data, int offset, int number)
        {
        if (number >= 0)        // valid number
            {
            model.set("bank", number / 32);
            model.set("number", number % 32);
            }
        else
            {
            number = 0;             // number offset of -1 needs to be changed to 0
            }
                        
        char[] name = new char[16];
        for(int i = 0; i < 16; i++)
            {
            name[i] = (char)(data[i + 226 * number + 10 + offset]);
            }
        model.set("name", String.valueOf(name).trim());
                        
        for(int i = 0; i < allParameters.length; i++)
            {
            String key = allParameters[i];
            int val = data[i + 226 * number + offset];
            if (key.equals("-"))
                {
                continue;
                }
            else if (key.equals("name"))
                {
                continue;
                }
            else
                {
                model.set(key, val);
                }
            }
        revise();
        return PARSE_SUCCEEDED;     
        }
        

    public static String getSynthName() { return "Waldorf MicroWave [Multi]"; }
    
    public String getPatchName(Model model) { return model.get("name", "Init Sound V1.1 "); }

    public byte getID() 
        { 
        try 
            { 
            byte b = (byte)(Byte.parseByte(tuple.id));
            if (b >= 0) return b;
            }
        catch (NullPointerException e) { } // expected.  Happens when tuple's not built yet
        catch (NumberFormatException e) { Synth.handleException(e); }
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
        int bank = model.get("bank");
        int number = model.get("number");
        
        number++;
        if (number >= 32)
            {
            bank++;
            number = 0;
            if (bank >= 2)
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
        
        int number = model.get("number") + 1;
        return BANKS[model.get("bank")] +  (number > 99 ? "" : (number > 9 ? "0" : "00")) + number;
        }


// Librarian Support
    public String[] getBankNames() { return BANKS; }
    public boolean[] getWriteableBanks() { return new boolean[]{ true, true }; }
    public String[] getPatchNumberNames() { return buildIntegerNames(32, 1); }
    // Though we *can* do bank writes, weirdly the MicroWave requires that
    // we write both banks in the same message.  We're not going to permit that.
    public boolean getSupportsPatchWrites() { return true; }
    public int getPatchNameLength() { return 16; }
    public int parseFromBank(byte[] bankSysex, int number) 
        { 
        return extractPatch(bankSysex, 5 + 180 * number, number);
        }
    public int[] getBanks(byte[] bankSysex) { return new int[] { 0, 1 }; }
                
    public byte[] requestAllDump() 
        { 
        return new byte[] 
            { 
            (byte)0xF0, 
            0x3E,                           // Waldorf
            0x00,                           // Microwave 1 
            (byte)getID(),
            0x22,                           // ARBR (Multi Program (ARR) Bank Dump Request)
            0,      // checksum
            (byte)0xF7
            };
        }

    public boolean librarianTested() { return true; }
    }
