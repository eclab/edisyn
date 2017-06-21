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

/**
   A patch editor for the Waldorf Microwave XT.  Does not deal with Multi mode, global parameters,
   modifying wavetables, or uploading samples.  Only Single mode patches.
        
   @author Sean Luke
*/


/**
I am *guessing* that the MULTI REQUEST format is as follows.

F0
3E		Waldorf ID
13		Blofeld ID
DD		Device ID
01		Message ID	[Multi Request]
HH**	Location High Byte
LL**	Location Low Byte
F7

** Where HH is (this is stolen from the Microwave XT Manual):

00 00 .. 00 7F		Locations M1 ... M128
40 00				All Multis
7F 00				Multi Mode Edit Buffer?  On the Microwave it's 20 FF

I think there may be no checksum, since there's no checksum on a single sound request.
However on the Microwave II/XT/XTk Multi Request, there *is* a checksum.

*/



/** 
From my experiments, I believe the MULTI DUMP format is as follows.

F0
3E		Waldorf ID
13		Blofeld ID
00		Device ID
11		Message ID	[Multi Dump]
HH**	Location High Byte
LL**	Location Low Byte
name 	[16 bytes]
00		[reserved]
0-127	Multi Volume
0-127		Tempo [same format as Arpegiator Tempo]
01***		[reserved, probably "Transmit Keyboard: global / multi", which I don't know what that is]
00		[reserved]
02****	[reserved, probably CONTROL W, dunno why this is here]
04****	[reserved, probably CONTROL X, dunno why this is here]
0B****	reserved, probably CONTROL Y, dunno why this is here]
0C****	[reserved, probably CONTROL Z, dunno why this is here]
00 		[7 times]

Then the following 16 times:
0-7 	Bank
0-127 	Number
0-127 	Volume
0-127 	Pan			[in -64 .. 63 as L64 ... R63]
00 		[reserved]
0-127	Transpose	[in -64 .. 63]
16-112	Detune		[in -48 .. 48]
0-17 	MIDI Channel [Global = 0, Omni = 1, otherwise Channel + 1]
0-127 	Low Key
0-127 	Hi Key
1-127 	Low Vel
1-127	Hi Vel
XXX*	Local/Midi/USB/Status{0=play, 1=mute}		0S00 0LUM
XXX*	Pressure/Bend/Wheel/Sustain/Edits/Change	00CE SPWB
01 		[reserved?]
3F 		[reserved?]
00		[8 times]

Then finally:
CHECKSUM
F7

* All bits are 0=ignore, 1=receive, except Status, which is 0=play, 1=mute

** Multi Instrument Buffer is HH=7F LL = 00.  I don't know about other
   stuff, since there's no bank.  Maybe HH=00 LL=n?

*** I don't know what this is, it's not in the manual.

**** These values are defined by GOFTER, but I don't know what purpose they serve if any.
     Also GOFTER has an option for the Free Button, but it doesn't seem to do anything.

*/




public class BlofeldMulti extends Synth
    {
    /// Various collections of parameter names for pop-up menus
        
    static final String[] BANKS = new String[] { "A", "B", "C", "D", "E", "F", "G", "H" };
    static final String[] KEYS = new String[] { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
        
        
    public BlofeldMulti()
        {
        for(int i = 0; i < allParameters.length; i++)
            {
            allParametersToIndex.put(allParameters[i], Integer.valueOf(i));
            }
                
        setSendsAllParametersInBulk(true);
        
        setLayout(new BorderLayout());
                
        JTabbedPane tabs = new JTabbedPane();
                
        JComponent soundPanel = new SynthPanel();
        VBox vbox = new VBox();
        vbox.add(addNameGlobal(Style.COLOR_GLOBAL));
        vbox.add(addMultiData(Style.COLOR_A));

		for(int i = 1; i < 5; i++)
			{
			vbox.add(addInstrument(i, Style.COLOR_B));
			}
                
        soundPanel.add(vbox, BorderLayout.CENTER);
        tabs.addTab("Multi and Parts 1 - 4", soundPanel);

        soundPanel = new SynthPanel();
        vbox = new VBox();

		for(int i = 5; i < 11; i++)
			{
			vbox.add(addInstrument(i, Style.COLOR_B));
			}
                
        soundPanel.add(vbox, BorderLayout.CENTER);
        tabs.addTab("Parts 5 - 10", soundPanel);

        soundPanel = new SynthPanel();
        vbox = new VBox();

		for(int i = 11; i < 17; i++)
			{
			vbox.add(addInstrument(i, Style.COLOR_B));
			}
                
        soundPanel.add(vbox, BorderLayout.CENTER);
        tabs.addTab("Parts 11 - 16", soundPanel);
		

		tabs.addTab("About", new HTMLBrowser(this.getClass().getResourceAsStream("BlofeldMulti.html")));

        add(tabs, BorderLayout.CENTER);

        model.set("name", "Init Multi      ");  // has to be 16 long
        
		loadDefaults();
        }
                
                
    public String getDefaultResourceFileName() { return "BlofeldMulti.init"; }

    public boolean gatherInfo(String title, Model change)
        {
        JTextField number = new JTextField("" + (model.get("number", 0) + 1), 3);

        JTextField id = new JTextField("" + model.get("id", 0), 3);
                
        while(true)
            {
            boolean result = doMultiOption(this, new String[] { "Patch Number", "Device ID" }, 
                new JComponent[] { number, id }, title, "Enter the Patch number and Device ID.");
                
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
                                
            int i;
            try { i = Integer.parseInt(id.getText()); }
            catch (NumberFormatException e)
                {
                JOptionPane.showMessageDialog(null, "The Device ID must be an integer 0 ... 127", title, JOptionPane.ERROR_MESSAGE);
                continue;
                }
            if (i < 0 || i > 127)
                {
                JOptionPane.showMessageDialog(null, "The Device ID must be an integer 0 ... 127", title, JOptionPane.ERROR_MESSAGE);
                continue;
                }
                        
            change.set("number", n - 1);
            change.set("id", i);
                        
            return true;
            }
        }


    /** Add the global patch category (name, id, number, etc.) */
    public JComponent addNameGlobal(Color color)
        {
        Category globalCategory = new Category("Blofeld Multi", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
                
        VBox vbox = new VBox();
        comp = new StringComponent("Patch Name", this, "name", 16, "Name must be up to 16 ASCII characters.")
            {
            public boolean isValid(String val)
                {
                if (val.length() > 16) return false;
                for(int i = 0 ; i < val.length(); i++)
                    {
                    char c = val.charAt(i);
                    if (c < 32 || c > 127) return false;
                    }
                return true;
                }
                                
            public void update(String key, Model model)
                {
                super.update(key, model);
                updateTitle();
                }
            };
        model.setImmutable("name", true);
        vbox.add(comp);
        hbox.add(vbox);
                
        comp = new LabelledDial("Number", this, "number", color, 0, 127, -1);
        model.setImmutable("number", true);
        hbox.add(comp);

        comp = new LabelledDial("Device ID", this, "id", color, 0, 127)
        	{
            public String map(int val)
                {
                if (val == 127)
                	return "All";
                else return "" + val;
                }
        	};
        model.setImmutable("id", true);
        hbox.add(comp);
        
        hbox.add(Strut.makeHorizontalStrut(17));

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
            };
        hbox.add(comp);
        
        category.add(hbox, BorderLayout.WEST);
        return category;
        }


    public JComponent addInstrument(int inst, Color color)
        {
        Category category = new Category("Part " + inst, color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();

       	comp = new LabelledDial("Bank", this, "bank" + inst, color, 0, 7)
            {
            public String map(int val)
                {
                String[] vals = BANKS;
                return vals[val];
                }
            };
        model.setImmutable("bank" + inst, true);
        ((LabelledDial)comp).setSecondLabel(" ");
        hbox.add(comp);


        comp = new LabelledDial("Number", this, "number" + inst, color, 0, 127, -1);
        model.setImmutable("number" + inst, true);
        hbox.add(comp);
        
        VBox main = new VBox();
        HBox hbox2 = new HBox();
        
        comp = new CheckBox("Mute", this, "status" + inst);
    	hbox2.add(comp);    

        comp = new CheckBox("Get Local", this, "local" + inst);
    	hbox2.add(comp);    

        comp = new CheckBox("Get MIDI", this, "midi" + inst);
    	hbox2.add(comp);    
    	
        comp = new CheckBox("Get USB", this, "usb" + inst);
    	hbox2.add(comp);
    	
    	main.add(hbox2);

		HBox hbox3 = new HBox();
		
    	vbox = new VBox();
    	
        comp = new CheckBox("Get Pressure", this, "pressure" + inst);
    	vbox.add(comp);    

        comp = new CheckBox("Get Pitch Bend", this, "bend" + inst);
    	vbox.add(comp);    
    	
    	hbox3.add(vbox);
    	vbox = new VBox();

        comp = new CheckBox("Get Sustain", this, "sustain" + inst);
    	vbox.add(comp);    
    	
        comp = new CheckBox("Get Edits", this, "edits" + inst);
    	vbox.add(comp);    

    	hbox3.add(vbox);
    	vbox = new VBox();
    	
        comp = new CheckBox("Get Mod Wheel", this, "modwheel" + inst);
    	vbox.add(comp);    

		comp = new CheckBox("Get Prog Change", this, "progchange" + inst);
    	vbox.add(comp);    

        hbox3.add(vbox);
        main.add(hbox3);
        hbox.add(main);
        
       	comp = new LabelledDial("Volume", this, "volume" + inst, color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Panning", this, "panning"  + inst, color, 0, 127, 64)
        	{
            public String map(int val)
                {
                if ((val - 64) < 0) return "L " + Math.abs(val - 64);
                else if ((val - 64) > 0) return "R " + (val - 64);
                else return "--";
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Transpose", this, "transpose" + inst, color, 16, 112, 64);
        hbox.add(comp);

        comp = new LabelledDial("Detune", this, "detune" + inst, color, 0, 127, 64);
        hbox.add(comp);

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
        ((LabelledDial)comp).setSecondLabel("Channel");
        hbox.add(comp);

    	comp = new LabelledDial("Lowest", this, "lowvel" + inst, color, 1, 127);
        ((LabelledDial)comp).setSecondLabel("Velocity");
        hbox.add(comp);

    	comp = new LabelledDial("Highest", this, "hivel" + inst, color, 1, 127);
        ((LabelledDial)comp).setSecondLabel("Velocity");
        hbox.add(comp);

    	comp = new LabelledDial("Lowest", this, "lowkey" + inst, color, 0, 127)	
    		{
            public String map(int val)
                {
                return KEYS[val % 12] + (val / 12 - 2);  // note integer division
                }
    		};
        ((LabelledDial)comp).setSecondLabel("Key");
        hbox.add(comp);

    	comp = new LabelledDial("Highest", this, "hikey" + inst, color, 0, 127)
    		{
            public String map(int val)
                {
                return KEYS[val % 12] + (val / 12 - 2);  // note integer division
                }
    		};
        ((LabelledDial)comp).setSecondLabel("Key");
        hbox.add(comp);

        category.add(hbox, BorderLayout.WEST);
        return category;
        }



    /** Map of parameter -> index in the allParameters array. */
    HashMap allParametersToIndex = new HashMap();


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
    
    "bank1",
    "number1",
    "volume1",
    "panning1",
	"-",
    "transpose1",
    "detune1",
    "channel1",
    "lowkey1",
    "hikey1",
    "lowvel1",
    "hivel1",
	"abits1",
	"bbits1",
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

    "bank2",
    "number2",
    "volume2",
    "panning2",
	"-",
    "transpose2",
    "detune2",
    "channel2",
    "lowkey2",
    "hikey2",
    "lowvel2",
    "hivel2",
	"abits2",
	"bbits2",
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

    "bank3",
    "number3",
    "volume3",
    "panning3",
	"-",
    "transpose3",
    "detune3",
    "channel3",
    "lowkey3",
    "hikey3",
    "lowvel3",
    "hivel3",
	"abits3",
	"bbits3",
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

    "bank4",
    "number4",
    "volume4",
    "panning4",
	"-",
    "transpose4",
    "detune4",
    "channel4",
    "lowkey4",
    "hikey4",
    "lowvel4",
    "hivel4",
	"abits4",
	"bbits4",
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

    "bank5",
    "number5",
    "volume5",
    "panning5",
	"-",
    "transpose5",
    "detune5",
    "channel5",
    "lowkey5",
    "hikey5",
    "lowvel5",
    "hivel5",
	"abits5",
	"bbits5",
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

    "bank6",
    "number6",
    "volume6",
    "panning6",
	"-",
    "transpose6",
    "detune6",
    "channel6",
    "lowkey6",
    "hikey6",
    "lowvel6",
    "hivel6",
	"abits6",
	"bbits6",
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

    "bank7",
    "number7",
    "volume7",
    "panning7",
	"-",
    "transpose7",
    "detune7",
    "channel7",
    "lowkey7",
    "hikey7",
    "lowvel7",
    "hivel7",
	"abits7",
	"bbits7",
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

    "bank8",
    "number8",
    "volume8",
    "panning8",
	"-",
    "transpose8",
    "detune8",
    "channel8",
    "lowkey8",
    "hikey8",
    "lowvel8",
    "hivel8",
	"abits8",
	"bbits8",
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

    "bank9",
    "number9",
    "volume9",
    "panning9",
	"-",
    "transpose9",
    "detune9",
    "channel9",
    "lowkey9",
    "hikey9",
    "lowvel9",
    "hivel9",
	"abits9",
	"bbits9",
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

    "bank10",
    "number10",
    "volume10",
    "panning10",
	"-",
    "transpose10",
    "detune10",
    "channel10",
    "lowkey10",
    "hikey10",
    "lowvel10",
    "hivel10",
	"abits10",
	"bbits10",
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

    "bank11",
    "number11",
    "volume11",
    "panning11",
	"-",
    "transpose11",
    "detune11",
    "channel11",
    "lowkey11",
    "hikey11",
    "lowvel11",
    "hivel11",
	"abits11",
	"bbits11",
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

    "bank12",
    "number12",
    "volume12",
    "panning12",
	"-",
    "transpose12",
    "detune12",
    "channel12",
    "lowkey12",
    "hikey12",
    "lowvel12",
    "hivel12",
	"abits12",
	"bbits12",
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

    "bank13",
    "number13",
    "volume13",
    "panning13",
	"-",
    "transpose13",
    "detune13",
    "channel13",
    "lowkey13",
    "hikey13",
    "lowvel13",
    "hivel13",
	"abits13",
	"bbits13",
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

    "bank14",
    "number14",
    "volume14",
    "panning14",
	"-",
    "transpose14",
    "detune14",
    "channel14",
    "lowkey14",
    "hikey14",
    "lowvel14",
    "hivel14",
	"abits14",
	"bbits14",
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

    "bank15",
    "number15",
    "volume15",
    "panning15",
	"-",
    "transpose15",
    "detune15",
    "channel15",
    "lowkey15",
    "hikey15",
    "lowvel15",
    "hivel15",
	"abits15",
	"bbits15",
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

    "bank16",
    "number16",
    "volume16",
    "panning16",
	"-",
    "transpose16",
    "detune16",
    "channel16",
    "lowkey16",
    "hikey16",
    "lowvel16",
    "hivel16",
	"abits16",
	"bbits16",
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



    public byte[] emit(String key)
        {
        // do nothing, this doesn't work for multi mode on the Blofeld
        return new byte[0];
        }
    
    
    
    
    public byte[] emit(Model tempModel, boolean toWorkingMemory)
        {
        if (tempModel == null)
            tempModel = getModel();
        byte DEV = (byte) tempModel.get("id", 0);
        byte BB = (byte) tempModel.get("bank", 0);
        byte NN = (byte) tempModel.get("number", 0);
        if (toWorkingMemory) { BB = 0x7F; NN = 0x0; }			// don't know if this is right
        
        byte[] bytes = new byte[416];
        
        for(int i = 16; i < 416; i++)	// skip the name
            {
            String key = allParameters[i];
        	if (key.equals("-"))
                {
                bytes[i] = 0;
                }
			else if (key.startsWith("abits"))
				{
				int part = (int)(key.charAt(5) - '0');
			
				byte status = (byte)model.get("status" + part, 0);
				byte local = (byte)model.get("local" + part, 0);
				byte usb = (byte)model.get("usb" + part, 0);
				byte midi = (byte)model.get("midi" + part, 0);
			
				bytes[i] = (byte)((status << 6) | (local << 2) | (usb << 1) | midi);
				}
			else if (key.startsWith("bbits"))
				{
				int part = (int)(key.charAt(5) - '0');
			
				byte pressure = (byte)model.get("pressure" + part, 0);
				byte bend = (byte)model.get("bend" + part, 0);
				byte wheel = (byte)model.get("modwheel" + part, 0);
				byte sustain = (byte)model.get("sustain" + part, 0);
				byte edits = (byte)model.get("edits" + part, 0);
				byte change = (byte)model.get("progchange" + part, 0);
			
				bytes[i] = (byte)((change << 5) | (edits << 4) | (sustain << 3) | (pressure << 2) | (wheel << 1) | bend);
				}
			else
				 {
				 bytes[i] = (byte)(model.get(key, 0));
				 }
        	}

        String name = model.get("name", "Init Sound V1.1 ");  // has to be 16 long
                                
        for(int i = 0; i < 16; i++)
            {
            if (i >= name.length())
            	bytes[i] = 0x20;  // space
            else
	            bytes[i] = (byte)(name.charAt(i));
            }
                
        byte[] full = new byte[getExpectedSysexLength()];
        full[0] = (byte)0xF0;
        full[1] = 0x3E;
        full[2] = 0x13;
        full[3] = DEV;
        full[4] = 0x11;		// Supposedly Multi Dump
        full[5] = BB;
        full[6] = NN;
        System.arraycopy(bytes, 0, full, 7, bytes.length);
        full[423] = produceChecksum(BB, NN, bytes);
        full[424] = (byte)0xF7;

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
                
        byte b = bb;
        b += nn;  // I *think* signed will work
        for(int i = 0; i < bytes.length; i++)
            b += bytes[i];
        
        b = (byte)(b & (byte)127);
        
        return b;
        }


    public byte[] requestDump(Model tempModel)
        {
        if (tempModel == null)
            tempModel = getModel();
        byte DEV = (byte)tempModel.get("id", 0);
        byte BB = 0;  // only 1 bank
        byte NN = (byte)tempModel.get("number", 0);

        return new byte[] { (byte)0xF0, 0x3E, 0x13, DEV, 0x01, BB, NN, (byte)0xF7 };
        }
        
    public byte[] requestCurrentDump(Model tempModel)
        {
        if (tempModel == null)
            tempModel = getModel();
        byte DEV = (byte)tempModel.get("id", 0);

        return new byte[] { (byte)0xF0, 0x3E, 0x13, DEV, 0x01, 0x7F, 0x00, (byte)0xF7 };
        }

    public static boolean recognize(byte[] data)
        {
        boolean v = (data[0] == (byte)0xF0 &&
            data[1] == (byte)0x3E &&
            data[2] == (byte)0x13 &&
            data.length == EXPECTED_SYSEX_LENGTH);
        return v;
        }
        
    public static final int EXPECTED_SYSEX_LENGTH = 425;
    public int getExpectedSysexLength() { return 425; }
        
        
    /** Verify that all the parameters are within valid values, and tweak them if not. */
    void revise()
        {
        for(int i = 0; i < allParameters.length; i++)
            {
            String key = allParameters[i];
            if (!model.isString(key))
                {
                if (model.minExists(key) && model.maxExists(key))
                    {
                    int val = model.get(key, 0);
                    if (val < model.getMin(key))
                        { model.set(key, model.getMin(key)); System.err.println("Warning: Revised " + key + " from " + val + " to " + model.get(key, 0));}
                    if (val > model.getMax(key))
                        { model.set(key, model.getMax(key)); System.err.println("Warning: Revised " + key + " from " + val + " to " + model.get(key, 0));}
                    }
                }
            }
        // handle "name" specially
        StringBuffer name = new StringBuffer(model.get("name", "Init Multi      "));  // has to be 16 long
        for(int i = 0; i < name.length(); i++)
            {
            char c = name.charAt(i);
            if (c < 32 || c > 127)
                { name.setCharAt(i, (char)32); System.err.println("Warning: Revised name from \"" + model.get("name", "Init Multi      ") + "\" to \"" + name.toString() + "\"");}
            }
        model.set("name", name.toString());
        }
        




	public void setParameterByIndex(int i, byte b)
		{
		String key = allParameters[i];
		int part = (i - 32) / 24;
		if (key.equals("-"))
			{
			// do nothing
			}
		else if (i >= 0 && i < 16)		// name
			{
			try 
				{
				String name = model.get("name", "Init Multi      ");
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
		else if (key.startsWith("abits"))
			{
			model.set("status" + part, (b >> 6) & 1);
			model.set("local" + part, (b >> 2) & 1);
			model.set("usb" + part, (b >> 1) & 1);
			model.set("midi" + part, (b) & 1);
			}
		else if (key.startsWith("bbits"))
			{
			model.set("progchange" + part, (b >> 5) & 1);
			model.set("edits" + part, (b >> 4) & 1);
			model.set("sustain" + part, (b >> 3) & 1);
			model.set("pressure" + part, (b >> 2) & 1);
			model.set("modwheel" + part, (b >> 1) & 1);
			model.set("bend" + part, (b) & 1);
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
        

    public boolean parse(byte[] data)
        {
        boolean retval = true;
        model.set("id", data[3]);
        if (data[5] < 8)  // 8?  Maybe 1.  Anyway otherwise it's probably just local patch data.  Too bad they do this. :-(
        	{
        	model.set("number", data[6]);
        	}
        else
        	{
        	model.set("number", 0);
        	retval = false;
        	}
        
        for(int i = 0; i < 416; i++)
            {
            setParameterByIndex(i, data[i + 7]);
            }
        revise();  
        return retval;     
        }

        
    public void merge(Model otherModel, double probability)
        {
        String[] keys = getModel().getKeys();
        for(int i = 0; i < keys.length; i++)
            {
            if (keys[i].equals("id")) continue;
            if (keys[i].equals("number")) continue;
            if (keys[i].equals("name")) continue;
                
            if (coinToss(probability))
                {
                if (otherModel.isString(keys[i]))
                    {
                    getModel().set(keys[i], otherModel.get(keys[i], getModel().get(keys[i], "")));
                    }
                else
                    {
                    getModel().set(keys[i], otherModel.get(keys[i], getModel().get(keys[i], 0)));
                    }
                }
            }
        }
    
    public void immutableMutate(String key)
        {
        /*
        // we randomize these specially, taking care not to do the high waves
        if (key.equals("osc1shape") || key.equals("osc2shape"))
            {
            if (coinToss(0.5))
                model.set(key, 0);
            else
                model.set(key, random.nextInt(WAVES_LONG.length -1) + 1);
            }
        */
        }
        

    public boolean requestCloseWindow() { return true; }

    public String getSynthName() { return "Blofeld [Multi]"; }
    
    public String getPatchName() { return model.get("name", "Init Multi      "); }
    

                
    }
