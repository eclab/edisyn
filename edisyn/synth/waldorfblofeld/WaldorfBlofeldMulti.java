/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.waldorfblofeld;

import edisyn.*;
import edisyn.gui.*;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.border.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

/**
   A patch editor for the Waldorf Blofeld in Multimode.
        
   @author Sean Luke
*/


/**
   Multi-mode sysex is entirely undocumented.  Here's my best shot at it, which is working
   well in Edisyn (https://github.com/eclab/edisyn/)
        
   -- Sean Luke sean@cs.gmu.edu
        
   PARAMETER CHANGE

   There is no parameter change in Multimode so far as I can tell.  If in multimode you
   have set certain patches to receive parameter changes via (for example) CC, then that
   will occur to their individual programs. 
        
   MULTI DUMP REQUEST

   F0
   3E              Waldorf ID
   13              Blofeld ID
   DD              Device ID
   01              Message ID                   [Multi Request]
   HH**                    Location High Byte   [Bank]
   LL**                    Location Low Byte    [Number]
   F7

   ** Where HH is (this is stolen from the Microwave XT Manual):

   00 00 .. 00 7F          Locations M1 ... M128
   40 00                   All Multis
   7F 00                   Multi Mode Edit Buffer?  On the Microwave it's 20 FF

   I think there is no checksum, since there's no checksum on a single sound request.
   However on the Microwave II/XT/XTk's Multi Request, there *is* a checksum, so go figure.


   MULTI DUMP

   F0
   3E              Waldorf ID
   13              Blofeld ID
   00              Device ID
   11              Message ID      [Multi Dump]
   HH**                Location High Byte      [Bank]
   LL**                Location Low Byte       [Number]
    
   --- CHECKSUM STARTS HERE ---
   name                [16 bytes]
   00              [reserved]
   0-127               Multi Volume
   0-127           Tempo [same format as Arpeggiator Tempo]
   01***           ["Transmit Keyboard: global / multi": I don't know what that is.  Help?]
   00              [reserved]
   02****              [maybe CONTROL W, don't know why this is here, help is welcome]
   04****              [maybe CONTROL X, don't know why this is here, help is welcome]
   0B****              [maybe CONTROL Y, don't know why this is here, help is welcome]
   0C****              [maybe CONTROL Z, don't know why this is here, help is welcome]
   00              [7 times]

   Then the following 16 times:
   0-7                 Bank
   0-127               Number
   0-127               Volume
   0-127               Pan                     [in -64 .. 63 as L64 ... R63]
   00              [reserved]
   0-127               Transpose       [in -64 .. 63]
   16-112              Detune          [in -48 .. 48]
   0-17                MIDI Channel [Global = 0, Omni = 1, otherwise Channel + 1]
   0-127               Low Key
   0-127               Hi Key
   1-127               Low Vel
   1-127               Hi Vel
   bits 0S000LUM*  Bits: Local/Midi/USB/Status {for Status: play=0, mute=1}
   bits 00CESPWB*  Bits: Pressure/Bend/Wheel/Sustain/Edits/Change
   01              [reserved?]
   3F              [reserved?]
   00              [8 times]
    
   --- CHECKSUM ENDS HERE ---

   Then finally:
   CHECKSUM
   F7

   * All bits are 0=ignore, 1=receive, except Status, which is 0=play, 1=mute

   ** Multi Instrument Buffer is HH=7F LL = 00.  I don't know about other
   stuff, since there's no bank.  Maybe HH=00 LL=n?

   *** In GOFTER I don't know what this is, it's not in the manual.

   **** These values are defined by the GOFTER patch editor, but I don't know what 
   purpose they serve if any. Also GOFTER has an option for the Free Button, but it 
   doesn't seem to do anything.
*/




public class WaldorfBlofeldMulti extends Synth
    {
    /// Various collections of parameter names for pop-up menus
        
    public static final String[] BANKS = new String[] { "A", "B", "C", "D", "E", "F", "G", "H" };
    public static final String[] KEYS = new String[] { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
        
        
    public WaldorfBlofeldMulti()
        {
        JComponent soundPanel = new SynthPanel(this);
        VBox vbox = new VBox();
        vbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        vbox.add(addMultiData(Style.COLOR_A()));

        for(int i = 1; i < 5; i++)
            {
            vbox.add(addInstrument(i, Style.COLOR_B()));
            }
        
                
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Multi and Parts 1 - 4", soundPanel);

        soundPanel = new SynthPanel(this);
        vbox = new VBox();

        for(int i = 5; i < 11; i++)
            {
            vbox.add(addInstrument(i, Style.COLOR_B()));
            }
                
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Parts 5 - 10", soundPanel);

        soundPanel = new SynthPanel(this);
        vbox = new VBox();

        for(int i = 11; i < 17; i++)
            {
            vbox.add(addInstrument(i, Style.COLOR_B()));
            }
                
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Parts 11 - 16", soundPanel);
                
        model.set("number", 0);

        model.set("name", "Init Multi");  // has to be 16 long
        
        loadDefaults();
        }
    
    public JFrame sprout()
        {
        JFrame frame = super.sprout();
        // multi-mode on the Blofeld can't switch patches
        transmitTo.setEnabled(false);    
        transmitParameters.setEnabled(false);
        transmitParameters.setSelected(false);        
        return frame;   
        }         
                
    public void changePatch(Model tempModel)
        {
        // Not possible in Multi Mode
        }

    public String getDefaultResourceFileName() { return "WaldorfBlofeldMulti.init"; }
    public String getHTMLResourceFileName() { return "WaldorfBlofeldMulti.html"; }

    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        JTextField number = new JTextField("" + (model.get("number") + 1), 3);

        while(true)
            {
            boolean result = showMultiOption(this, new String[] { "Patch Number" }, 
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
        Category globalCategory = new Category(this, "Waldorf Blofeld [Multi]", color);
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



    public JComponent addMultiData(Color color)
        {
        Category category = new Category(this, "General", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        //        VBox vbox = new VBox();
    
        comp = new LabelledDial("Volume", this, "volume", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Tempo", this, "arptempo", color, 1, 127)
            {
            // 40...300
            // BPM 165 and above we jump by 5
            // BPM 90 and below we jump by 2
                        
            // So 40...90 inclusive is 0...25
            // 91 ... 165 is 26 ... 100
            // 170 ... 300 is 101 ... 127
            public String map(int val)
                {
                if (val < 25)
                    return "" + ((val * 2) + 40);
                else if (val < 101)
                    return "" + (val + 65);
                else
                    return "" + (((val - 101) * 5) + 170);
                }
            public int getDefaultValue() { return 55; }             // 120 BPM
            };
        hbox.add(comp);
        
        category.add(hbox, BorderLayout.WEST);
        return category;
        }


    public JComponent addInstrument(final int inst, Color color)
        {
        Category category = new Category(this, "Part " + inst, color);
        category.makePasteable("inst" + inst);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();

        comp = new LabelledDial("Bank", this, "inst" + inst + "bank", color, 0, 7)
            {
            public String map(int val)
                {
                String[] vals = BANKS;
                return vals[val];
                }
            };
        ((LabelledDial)comp).addAdditionalLabel(" ");
        model.removeMetricMinMax("inst" + inst + "bank");
        hbox.add(comp);


        comp = new LabelledDial("Number", this, "inst" + inst + "number", color, 0, 127, -1);
        model.removeMetricMinMax("inst" + inst + "number");
        hbox.add(comp);
        
        VBox main = new VBox();
        HBox hbox2 = new HBox();
        
        comp = new PushButton("Show")
            {
            public void perform()
                {
                final WaldorfBlofeld synth = new WaldorfBlofeld();
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
                                tempModel.set("bank", WaldorfBlofeldMulti.this.model.get("inst" + inst + "bank"));
                                tempModel.set("number", WaldorfBlofeldMulti.this.model.get("inst" + inst + "number"));
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
        hbox2.add(comp);
        
        comp = new CheckBox("Play", this, "inst" + inst + "status", true);
        hbox2.add(comp);    

        comp = new CheckBox("Local", this, "inst" + inst + "local");
        hbox2.add(comp);    

        comp = new CheckBox("MIDI", this, "inst" + inst + "midi");
        hbox2.add(comp);    
        
        comp = new CheckBox("USB", this, "inst" + inst + "usb");
        hbox2.add(comp);
        
        main.add(hbox2);

        HBox hbox3 = new HBox();
                
        vbox = new VBox();
        
        comp = new CheckBox("Pressure", this, "inst" + inst + "pressure");
        vbox.add(comp);    

        comp = new CheckBox("Pitch Bend", this, "inst" + inst + "bend");
        vbox.add(comp);    
        
        hbox3.add(vbox);
        vbox = new VBox();

        comp = new CheckBox("Sustain", this, "inst" + inst + "sustain");
        vbox.add(comp);    
        
        comp = new CheckBox("Edits", this, "inst" + inst + "edits");
        vbox.add(comp);    

        hbox3.add(vbox);
        vbox = new VBox();
        
        comp = new CheckBox("Mod Wheel", this, "inst" + inst + "modwheel");
        vbox.add(comp);    

        comp = new CheckBox("Prg Change", this, "inst" + inst + "progchange");
        vbox.add(comp);    

        hbox3.add(vbox);
        main.add(hbox3);
        hbox.add(main);

        comp = new LabelledDial("Volume", this, "inst" + inst + "volume", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Panning", this, "inst" + inst + "panning", color, 0, 127, 64)
            {
            public String map(int val)
                {
                if ((val - 64) < 0) return "L " + Math.abs(val - 64);
                else if ((val - 64) > 0) return "R " + (val - 64);
                else return "--";
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Transpose", this, "inst" + inst + "transpose", color, 16, 112, 64);
        hbox.add(comp);

        comp = new LabelledDial("Detune", this, "inst" + inst + "detune", color, 0, 127, 64);
        hbox.add(comp);

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
        model.removeMetricMinMax( "inst" + inst + "channel");
        ((LabelledDial)comp).addAdditionalLabel("Channel");
        hbox.add(comp);

        comp = new LabelledDial("Lowest", this, "inst" + inst + "lowvel", color, 1, 127);
        ((LabelledDial)comp).addAdditionalLabel("Velocity");
        hbox.add(comp);

        comp = new LabelledDial("Highest", this, "inst" + inst + "hivel", color, 1, 127);
        ((LabelledDial)comp).addAdditionalLabel("Velocity");
        hbox.add(comp);

        comp = new LabelledDial("Lowest", this, "inst" + inst + "lowkey", color, 0, 127) 
            {
            public String map(int val)
                {
                return KEYS[val % 12] + (val / 12 - 2);  // note integer division
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Key");
        hbox.add(comp);

        comp = new LabelledDial("Highest", this, "inst" + inst + "hikey", color, 0, 127)
            {
            public String map(int val)
                {
                return KEYS[val % 12] + (val / 12 - 2);  // note integer division
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Key");
        hbox.add(comp);

        category.add(hbox, BorderLayout.WEST);
        return category;
        }


    /** List of all Waldorf parameters in order.  "-" is a reserved (unused and thus unnamed) parameter. */

    /// * indicates parameters which must be handled specially due to packing
    /// that Waldorf decided to do.  :-(

    final static String[] allParameters = new String[/*248?*/] 
    {
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
    "-",
    "volume",
    "arptempo",                   
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    
    "inst1bank",
    "inst1number",
    "inst1volume",
    "inst1panning",
    "-",
    "inst1transpose",
    "inst1detune",
    "inst1channel",
    "inst1lowkey",
    "inst1hikey",
    "inst1lowvel",
    "inst1hivel",
    "inst1abits",
    "inst1bbits",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",

    "inst2bank",
    "inst2number",
    "inst2volume",
    "inst2panning",
    "-",
    "inst2transpose",
    "inst2detune",
    "inst2channel",
    "inst2lowkey",
    "inst2hikey",
    "inst2lowvel",
    "inst2hivel",
    "inst2abits",
    "inst2bbits",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",

    "inst3bank",
    "inst3number",
    "inst3volume",
    "inst3panning",
    "-",
    "inst3transpose",
    "inst3detune",
    "inst3channel",
    "inst3lowkey",
    "inst3hikey",
    "inst3lowvel",
    "inst3hivel",
    "inst3abits",
    "inst3bbits",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",

    "inst4bank",
    "inst4number",
    "inst4volume",
    "inst4panning",
    "-",
    "inst4transpose",
    "inst4detune",
    "inst4channel",
    "inst4lowkey",
    "inst4hikey",
    "inst4lowvel",
    "inst4hivel",
    "inst4abits",
    "inst4bbits",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",

    "inst5bank",
    "inst5number",
    "inst5volume",
    "inst5panning",
    "-",
    "inst5transpose",
    "inst5detune",
    "inst5channel",
    "inst5lowkey",
    "inst5hikey",
    "inst5lowvel",
    "inst5hivel",
    "inst5abits",
    "inst5bbits",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",

    "inst6bank",
    "inst6number",
    "inst6volume",
    "inst6panning",
    "-",
    "inst6transpose",
    "inst6detune",
    "inst6channel",
    "inst6lowkey",
    "inst6hikey",
    "inst6lowvel",
    "inst6hivel",
    "inst6abits",
    "inst6bbits",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",

    "inst7bank",
    "inst7number",
    "inst7volume",
    "inst7panning",
    "-",
    "inst7transpose",
    "inst7detune",
    "inst7channel",
    "inst7lowkey",
    "inst7hikey",
    "inst7lowvel",
    "inst7hivel",
    "inst7abits",
    "inst7bbits",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",

    "inst8bank",
    "inst8number",
    "inst8volume",
    "inst8panning",
    "-",
    "inst8transpose",
    "inst8detune",
    "inst8channel",
    "inst8lowkey",
    "inst8hikey",
    "inst8lowvel",
    "inst8hivel",
    "inst8abits",
    "inst8bbits",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",

    "inst9bank",
    "inst9number",
    "inst9volume",
    "inst9panning",
    "-",
    "inst9transpose",
    "inst9detune",
    "inst9channel",
    "inst9lowkey",
    "inst9hikey",
    "inst9lowvel",
    "inst9hivel",
    "inst9abits",
    "inst9bbits",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",

    "inst10bank",
    "inst10number",
    "inst10volume",
    "inst10panning",
    "-",
    "inst10transpose",
    "inst10detune",
    "inst10channel",
    "inst10lowkey",
    "inst10hikey",
    "inst10lowvel",
    "inst10hivel",
    "inst10abits",
    "inst10bbits",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",

    "inst11bank",
    "inst11number",
    "inst11volume",
    "inst11panning",
    "-",
    "inst11transpose",
    "inst11detune",
    "inst11channel",
    "inst11lowkey",
    "inst11hikey",
    "inst11lowvel",
    "inst11hivel",
    "inst11abits",
    "inst11bbits",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",

    "inst12bank",
    "inst12number",
    "inst12volume",
    "inst12panning",
    "-",
    "inst12transpose",
    "inst12detune",
    "inst12channel",
    "inst12lowkey",
    "inst12hikey",
    "inst12lowvel",
    "inst12hivel",
    "inst12abits",
    "inst12bbits",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",

    "inst13bank",
    "inst13number",
    "inst13volume",
    "inst13panning",
    "-",
    "inst13transpose",
    "inst13detune",
    "inst13channel",
    "inst13lowkey",
    "inst13hikey",
    "inst13lowvel",
    "inst13hivel",
    "inst13abits",
    "inst13bbits",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",

    "inst14bank",
    "inst14number",
    "inst14volume",
    "inst14panning",
    "-",
    "inst14transpose",
    "inst14detune",
    "inst14channel",
    "inst14lowkey",
    "inst14hikey",
    "inst14lowvel",
    "inst14hivel",
    "inst14abits",
    "inst14bbits",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",

    "inst15bank",
    "inst15number",
    "inst15volume",
    "inst15panning",
    "-",
    "inst15transpose",
    "inst15detune",
    "inst15channel",
    "inst15lowkey",
    "inst15hikey",
    "inst15lowvel",
    "inst15hivel",
    "inst15abits",
    "inst15bbits",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",

    "inst16bank",
    "inst16number",
    "inst16volume",
    "inst16panning",
    "-",
    "inst16transpose",
    "inst16detune",
    "inst16channel",
    "inst16lowkey",
    "inst16hikey",
    "inst16lowvel",
    "inst16hivel",
    "inst16abits",
    "inst16bbits",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",


    };


    // extracts a 1 or 2-digit integer starting at 'start'
    int extractInteger(String str, int start)
        {
        int val = (int)(str.charAt(start) - '0');
        int val2 = (int)(str.charAt(start + 1) - '0');
        if (val2 >= 0 && val2 <= 9)
            {
            val = val * 10 + val2;
            }
        return val;
        }

    
    public byte[] emit(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        if (tempModel == null)
            tempModel = getModel();
        byte DEV = (byte)(getID());
        byte BB = (byte) 0x0;  // multis have only 1 bank
        byte NN = (byte) tempModel.get("number");
        if (toWorkingMemory) { BB = 0x7F; NN = 0x0; }                   // don't know if this is right
        
        byte[] bytes = new byte[416];
        
        for(int i = 16; i < 416; i++)   // skip the name
            {
            String key = allParameters[i];
            if (key.equals("-"))
                {
                bytes[i] = 0;
                }
            else if (key.endsWith("abits"))
                {
                int part = extractInteger(key, 4);  // starts with "inst"
                        
                byte status = (byte)model.get("inst" + part + "status");
                byte local = (byte)model.get("inst" + part + "local");
                byte usb = (byte)model.get("inst" + part + "usb");
                byte midi = (byte)model.get("inst" + part + "midi");
                        
                bytes[i] = (byte)((status << 6) | (local << 2) | (usb << 1) | midi);
                }
            else if (key.endsWith("bbits"))
                {
                int part = extractInteger(key, 4);  // starts with "inst"
                        
                byte pressure = (byte)model.get("inst" + part + "pressure");
                byte bend = (byte)model.get("inst" + part + "bend");
                byte wheel = (byte)model.get("inst" + part + "modwheel");
                byte sustain = (byte)model.get("inst" + part + "sustain");
                byte edits = (byte)model.get("inst" + part + "edits");
                byte change = (byte)model.get("inst" + part + "progchange");
                        
                bytes[i] = (byte)((change << 5) | (edits << 4) | (sustain << 3) | (pressure << 2) | (wheel << 1) | bend);
                }
            else
                {
                bytes[i] = (byte)(model.get(key));
                }
            }

        String name = model.get("name", "Init Multi") + "                ";  // has to be 16 long
                                
        for(int i = 0; i < 16; i++)
            {
            bytes[i] = (byte)(name.charAt(i));
            }
                
        byte[] full = new byte[EXPECTED_SYSEX_LENGTH];
        full[0] = (byte)0xF0;
        full[1] = 0x3E;
        full[2] = 0x13;
        full[3] = DEV;
        full[4] = 0x11;         // Supposedly Multi Dump
        full[5] = BB;
        full[6] = NN;
        System.arraycopy(bytes, 0, full, 7, bytes.length);
        full[423] = produceChecksum(bytes);
        full[424] = (byte)0xF7;

        return full;
        }


    /** Generate a Waldorf checksum of the data bytes */
    byte produceChecksum(byte[] bytes)
        {
        //      From the sysex document:
        //
        //      "Sum of all databytes truncated to 7 bits.
        //  The addition is done in 8 bit format, the result is    
        //  masked to 7 bits (00h to 7Fh). A checksum of 7Fh is
        //  always accepted as valid.
        //  IMPORTANT: the MIDI status-bytes as well as the 
        //  ID's are not used for computing the checksum."
        
        // NOTE: it appears that the Blofeld's sysex does NOT include
        // the NN or DD data bytes.        
        
        byte b = 0;  // I *think* signed will work
        for(int i = 0; i < bytes.length; i++)
            b += bytes[i];
        
        b = (byte)(b & (byte)127);
        
        return b;
        }


    public byte[] requestDump(Model tempModel)
        {
        if (tempModel == null)
            tempModel = getModel();
        byte DEV = (byte)(getID());
        byte BB = 0;  // only 1 bank
        byte NN = (byte)tempModel.get("number");

        return new byte[] { (byte)0xF0, 0x3E, 0x13, DEV, 0x01, BB, NN, (byte)0xF7 };
        }
        
    public byte[] requestCurrentDump()
        {
        byte DEV = (byte)(getID());

        return new byte[] { (byte)0xF0, 0x3E, 0x13, DEV, 0x01, 0x7F, 0x00, (byte)0xF7 };
        }

    public static boolean recognize(byte[] data)
        {
        boolean v = (
            data.length == EXPECTED_SYSEX_LENGTH &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x3E &&
            data[2] == (byte)0x13 &&
            data[4] == (byte)0x11);
        return v;
        }
        
    public static final int EXPECTED_SYSEX_LENGTH = 425;
        
        
    /** Verify that all the parameters are within valid values, and tweak them if not. */
    public void revise()
        {
        // check the easy stuff -- out of range parameters
        super.revise();

        // handle "name" specially
        String nm = model.get("name", "Init Multi");
        String newnm = revisePatchName(nm);
        if (!nm.equals(newnm))
            model.set("name", newnm);
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




    public void setParameterByIndex(int i, byte b)
        {
        String key = allParameters[i];
        int part = (i - 32) / 24 + 1;
        if (key.equals("-"))
            {
            // do nothing
            }
        else if (i >= 0 && i < 16)              // name
            {
            try 
                {
                String name = model.get("name", "Init Multi") + "                ";
                byte[] str = name.getBytes("US-ASCII");
                byte[] newstr = new byte[] { 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20 };
                System.arraycopy(str, 0, newstr, 0, 16);
                newstr[i] = b;
                model.set("name", new String(newstr, "US-ASCII"));
                }
            catch (UnsupportedEncodingException e)
                {
                e.printStackTrace();
                }
            }
        else if (key.endsWith("abits"))
            {
            model.set("inst" + part + "status", (b >>> 6) & 1);
            model.set("inst" + part + "local", (b >>> 2) & 1);
            model.set("inst" + part + "usb", (b >>> 1) & 1);
            model.set("inst" + part + "midi", (b) & 1);
            }
        else if (key.endsWith("bbits"))
            {
            model.set("inst" + part + "progchange", (b >>> 5) & 1);
            model.set("inst" + part + "edits", (b >>> 4) & 1);
            model.set("inst" + part + "sustain", (b >>> 3) & 1);
            model.set("inst" + part + "pressure", (b >>> 2) & 1);
            model.set("inst" + part + "modwheel", (b >>> 1) & 1);
            model.set("inst" + part + "bend", (b) & 1);
            }
        else
            {
            model.set(key, b);
            }
        }

        
    public void parseParameter(byte[] data)
        {
        // This doesn't work for multi mode on the Blofeld
        }
        

    public int parse(byte[] data, boolean fromFile)
        {
        boolean retval = true;
        if (data[5] < 8)  // 8?  Maybe 1.  Anyway otherwise it's probably just local patch data.  Too bad they do this. :-(
            {
            model.set("number", data[6]);
            }
        else
            {
            retval = false;
            }
        
        for(int i = 0; i < 416; i++)
            {
            setParameterByIndex(i, data[i + 7]);
            }
        revise();  
        return PARSE_SUCCEEDED;    
        }

    public static String getSynthName() { return "Waldorf Blofeld [Multi]"; }
    
    public String getPatchName(Model model) { return model.get("name", "Init Multi"); }
    
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
