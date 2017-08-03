/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.yamahatx81z;

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
   A patch editor for the Yamaha TX81Z (Multi Mode).
        
   @author Sean Luke
*/

public class YamahaTX81ZMulti extends Synth
    {
    /// Various collections of parameter names for pop-up menus
        
    public static final String[] KEYS = new String[] { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
    public static final String[] CHANNELS = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16" };
    public static final String[] BANKS = { "I", "A", "B", "C", "D" };
    public static final String[] EFFECTS = { "Off", "Delay", "Pan", "Chord Memory" };
    public static final String[] MICROTUNE_TABLES = { "Equal", "Pure (Major)", "Pure (Minor)", "Mean Tone", "Pythagorean", "Werckmeister", "Kirnberger", "Vallotti & Young", "1/4 Shift Equal", "1/4 Tone", "1/8 Tone", "User (Octave)", "User (Full)" };
    public static final String[] MICROTUNE_KEYS = { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
    public static final String[] LFO_SELECT = { "Off", "Instrument 1", "Instrument 2", "Vibrato" };
    public static final String[] OUT_ASSIGN = { "Off", "I", "II", "I and II" };

    public YamahaTX81ZMulti()
        {
        for(int i = 0; i < allParameters.length; i++)
            {
            allParametersToIndex.put(allParameters[i], Integer.valueOf(i));
            }
                        
        /// SOUND PANEL
                
        JComponent soundPanel = new SynthPanel();
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL));
        hbox.addLast(addGlobal(Style.COLOR_B));
        vbox.add(hbox);
        
        vbox.add(addInstrument(1, Style.COLOR_A));
        vbox.add(addInstrument(2, Style.COLOR_A));

        soundPanel.add(vbox, BorderLayout.CENTER);
        tabs.addTab("Global, Instruments 1-2", soundPanel);
                

        SynthPanel sourcePanel = new SynthPanel();
        vbox = new VBox();
        
        vbox.add(addInstrument(3, Style.COLOR_A));
        vbox.add(addInstrument(4, Style.COLOR_A));
        vbox.add(addInstrument(5, Style.COLOR_A));
            
        sourcePanel.add(vbox, BorderLayout.CENTER);
        tabs.addTab("Instruments 3-5", sourcePanel);

        sourcePanel = new SynthPanel();
        vbox = new VBox();
        
        vbox.add(addInstrument(6, Style.COLOR_A));
        vbox.add(addInstrument(7, Style.COLOR_A));
        vbox.add(addInstrument(8, Style.COLOR_A));

        sourcePanel.add(vbox, BorderLayout.CENTER);
        tabs.addTab("Instruments 6-8", sourcePanel);
        
        model.set("name", "INIT SOUND");
        
        //loadDefaults();        
        }
                
    public String getDefaultResourceFileName() { return "YamahaTX81ZMulti.init"; }
	public String getHTMLResourceFileName() { return "YamahaTX81ZMulti.html"; }

    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        JTextField number = new JTextField("" + (model.get("number", 0) + 1), 3);

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
                showSimpleError(title, "The Patch Number must be an integer 1...24");
                continue;
                }
            if (n < 1 || n > 24)
                {
                showSimpleError(title, "The Patch Number must be an integer 1...24");
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
        Category globalCategory = new Category(getSynthName(), color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
                
        VBox vbox = new VBox();
        HBox hbox2 = new HBox();
        comp = new PatchDisplay(this, "Patch", null, "number", 4)
            {
            public String numberString(int number) { number += 1; return (number > 9 ? "0" : "00") + number; }
            public String bankString(int bank) { return ""; }
            };
        hbox2.add(comp);
        vbox.add(hbox2);
        
        comp = new StringComponent("Patch Name", this, "name", 10, "Name must be up to 10 ASCII characters.")
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
        vbox.addBottom(comp);  // doesn't work right :-(
        hbox.add(vbox);

        // Not enough space to show the title
        hbox.addLast(Strut.makeHorizontalStrut(90));

        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }


    public JComponent addGlobal( Color color)
        {
        Category category = new Category("Global", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = EFFECTS;
        comp = new Chooser("Effect", this, "effect", params);
        vbox.add(comp);
        
        params = MICROTUNE_TABLES;
        comp = new Chooser("Microtune Table", this, "microtunetable", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Microtune", this, "microtunekey", color, 0, 11)
            {
            public String map(int val)
                {
                return MICROTUNE_KEYS[val];
                }
            };
        ((LabelledDial)comp).setSecondLabel("Key");
        hbox.add(comp);

        vbox = new VBox();
        comp = new CheckBox("Alternate Assign Mode", this, "assignmode");
        vbox.add(comp);
        hbox.add(vbox);
        
        

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }



    public JComponent addInstrument(final int src, Color color)
        {
        final Category category = new Category("Instrument " + src, color);

        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = OUT_ASSIGN;
        comp = new Chooser("Output", this, "instrument" + src + "outassign", params);
        vbox.add(comp);

        params = LFO_SELECT;
        comp = new Chooser("LFO", this, "instrument" + src + "lfoselect", params);
        vbox.add(comp);
        hbox.add(vbox);

        vbox = new VBox();
        comp = new CheckBox("Microtune On", this, "instrument" + src + "microtuneanbled");
        vbox.add(comp);

        HBox hbox2 = new HBox();
        comp = new PushButton("Show")
            {
            public void perform()
                {
                final YamahaTX81Z synth = new YamahaTX81Z();
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
                                int bank = YamahaTX81ZMulti.this.model.get("instrument" + src + "bank", 0);
                                int number = YamahaTX81ZMulti.this.model.get("instrument" + src + "number", 0);
                                                                
                                Model tempModel = new Model();
                                tempModel.set("bank", bank);
                                tempModel.set("number", number);
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
        hbox2.addLast(comp);
        vbox.addBottom(hbox2);
        hbox.add(vbox);


        comp = new LabelledDial("Voice", this, "instrument" + src + "voicebank", color, 0, 4)
            {
            public String map(int val)
                {
                return BANKS[val];
                }
            };
        ((LabelledDial)comp).setSecondLabel("Bank");
        hbox.add(comp);
        
        comp = new LabelledDial("Voice", this, "instrument" + src + "voicenumber", color, 0, 31)
            {
            public String map(int val)
                {
                return "" + (val + 1);
                }
            };
        ((LabelledDial)comp).setSecondLabel("Number");
        hbox.add(comp);
        
        comp = new LabelledDial("Receive", this, "instrument" + src + "channel", color, 0, 16)
            {
            public String map(int val)
                {
                if (val == 16) return "Omni";
                else return "" + (val + 1);
                }
            };
        ((LabelledDial)comp).setSecondLabel("Channel");
        hbox.add(comp);
        
        comp = new LabelledDial("Max Notes", this, "instrument" + src + "maxnotes", color, 0, 8);
        hbox.add(comp);
        
        comp = new LabelledDial("Lowest", this, "instrument" + src + "lowkey", color, 0, 127)
            {
            public String map(int val)
                {
                return KEYS[val % 12] + (val / 12 - 2);  // note integer division
                }
            };
        ((LabelledDial)comp).setSecondLabel("Key");
        hbox.add(comp);

        comp = new LabelledDial("Highest", this, "instrument" + src + "highkey", color, 0, 127)
            {
            public String map(int val)
                {
                return KEYS[val % 12] + (val / 12 - 2);  // note integer division
                }
            };
        ((LabelledDial)comp).setSecondLabel("Key");
        hbox.add(comp);
        

        comp = new LabelledDial("Detune", this, "instrument" + src + "detune", color, 0, 14, 7)
            {
            public boolean isSymmetric() { return true; }
            };
        hbox.add(comp);

        comp = new LabelledDial("Note Shift", this, "instrument" + src + "noteshift", color, 0, 48, 24)
            {
            public boolean isSymmetric() { return true; }
            };
        hbox.add(comp);
        
        comp = new LabelledDial("Volume", this, "instrument" + src + "volume", color, 0, 99);
        hbox.add(comp);
    
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
                



    /** Map of parameter -> index in the allParameters array. */
    HashMap allParametersToIndex = new HashMap();


    /** List of all Sysex parameters in order.  "-" is a reserved (unused and thus unnamed) parameter. */

    /// * indicates parameters which must be handled specially due to packing
    /// that Waldorf decided to do.  :-(

    final static String[] allParameters = new String[] 
    {
    "instrument1maxnotes",
    "instrument1voicenumbermsb",
    "instrument1voicenumberlsb",
    "instrument1channel",
    "instrument1lowkey",
    "instrument1highkey",
    "instrument1detune",
    "instrument1noteshift",
    "instrument1volume",
    "instrument1outassign",
    "instrument1lfoselect",
    "instrument1microtuneanbled",

    "instrument2maxnotes",
    "instrument2voicenumbermsb",
    "instrument2voicenumberlsb",
    "instrument2channel",
    "instrument2lowkey",
    "instrument2highkey",
    "instrument2detune",
    "instrument2noteshift",
    "instrument2volume",
    "instrument2outassign",
    "instrument2lfoselect",
    "instrument2microtuneanbled",

    "instrument3maxnotes",
    "instrument3voicenumbermsb",
    "instrument3voicenumberlsb",
    "instrument3channel",
    "instrument3lowkey",
    "instrument3highkey",
    "instrument3detune",
    "instrument3noteshift",
    "instrument3volume",
    "instrument3outassign",
    "instrument3lfoselect",
    "instrument3microtuneanbled",

    "instrument4maxnotes",
    "instrument4voicenumbermsb",
    "instrument4voicenumberlsb",
    "instrument4channel",
    "instrument4lowkey",
    "instrument4highkey",
    "instrument4detune",
    "instrument4noteshift",
    "instrument4volume",
    "instrument4outassign",
    "instrument4lfoselect",
    "instrument4microtuneanbled",

    "instrument5maxnotes",
    "instrument5voicenumbermsb",
    "instrument5voicenumberlsb",
    "instrument5channel",
    "instrument5lowkey",
    "instrument5highkey",
    "instrument5detune",
    "instrument5noteshift",
    "instrument5volume",
    "instrument5outassign",
    "instrument5lfoselect",
    "instrument5microtuneanbled",

    "instrument6maxnotes",
    "instrument6voicenumbermsb",
    "instrument6voicenumberlsb",
    "instrument6channel",
    "instrument6lowkey",
    "instrument6highkey",
    "instrument6detune",
    "instrument6noteshift",
    "instrument6volume",
    "instrument6outassign",
    "instrument6lfoselect",
    "instrument6microtuneanbled",

    "instrument7maxnotes",
    "instrument7voicenumbermsb",
    "instrument7voicenumberlsb",
    "instrument7channel",
    "instrument7lowkey",
    "instrument7highkey",
    "instrument7detune",
    "instrument7noteshift",
    "instrument7volume",
    "instrument7outassign",
    "instrument7lfoselect",
    "instrument7microtuneanbled",

    "instrument8maxnotes",
    "instrument8voicenumbermsb",
    "instrument8voicenumberlsb",
    "instrument8channel",
    "instrument8lowkey",
    "instrument8highkey",
    "instrument8detune",
    "instrument8noteshift",
    "instrument8volume",
    "instrument8outassign",
    "instrument8lfoselect",
    "instrument8microtuneanbled",

	"microtunetable",
	"assignmode",
	"effect",
	"microtunekey",

	// name
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



/*
  From mgregory22@gmail.com:

  I wrote an editor for the TX81Z awhile back.  Yeah, I think there are a couple bugs in the manual if I remember correctly. I see you wrote Gizmo in C++, and I worked out all the MIDI bugs in my editor, so I'm going to refer to that.

  Here is a file of constants with subgroup numbers on line 173:
  https://github.com/mgregory22/tx81z-programmer/blob/master/src/tx81z.h#L173

  So, the PCED has a subgroup of 0.

  There's also this file, which describes the different dump types in the TX81Z:
  https://github.com/mgregory22/tx81z-programmer/blob/master/src/tx81z_meta.c

  reqStr is a request string, and dumpHdr is the header of the received dump.

  Here's the code to interface with the TX81Z.  I implemented it as a window that sends messages, so I wouldn't have to write threading code, so it might be kind of confusing in places, but I think you'll be able to get it:
  https://github.com/mgregory22/tx81z-programmer/blob/master/src/tx81z.c

  TX81Z_SendParamChange() is probably what you're interested in, if you want to know how I did things:
  https://github.com/mgregory22/tx81z-programmer/blob/master/src/tx81z.c#L507

  I haven't worked on it in a long time, so I can't remember all that much about it, but if you have any questions, let me know and I'll try to answer them.

  Hope that helps,
  Matt

*/

    public static final int VCED_GROUP = 2 + 16; // 00010010
    public static final int ACED_GROUP = 3 + 16; // 00010011
    public static final int PCED_GROUP = 0 + 16; // 00010000        says 00010011 in the manual, wrong

    public Object[] emitAll(String key)
        {
        if (key.equals("number")) return new byte[0][0];  // this is not emittable

        byte channel = (byte)(32 + getChannelOut() - 1);
             
        // maybe we don't want to do this
        if (key.equals("name"))  // ugh
            {
            String name = model.get("name", "INIT SOUND") + "          ";
            byte[][] result = new byte[10][];                       
            for(int i = 0; i < 10; i++)
                {
                result[i] = new byte[] { (byte)0xF0, 0x43, channel, PCED_GROUP, (byte)(100 + i), (byte)(name.charAt(i)), (byte)0xF7 };
                }
            return result;
            }
        for(int i = 1; i < 9; i++)
        	{
        	if (key.equals("operator" + i + "voicenumber"))
        		{
        		int val = model.get(key, 0);
        		byte lsb = (byte)(val & 127);
        		byte msb = (byte)((val >> 7) & 127);

            	return new byte[][] 
            		{
            			{ (byte)0xF0, 0x43, channel, PCED_GROUP, 1, msb, (byte)0xF7 },
            			{ (byte)0xF0, 0x43, channel, PCED_GROUP, 2, lsb, (byte)0xF7 },
            		};
        		}
        	}        
        
        if (allParametersToIndex.containsKey(key))
            {
            int index = ((Integer)(allParametersToIndex.get(key))).intValue();
            int value = model.get(key, 0);

            byte PP = (byte) index;
            byte VV = (byte) value;
            return new byte[][] {{ (byte)0xF0, 0x43, channel, PCED_GROUP, PP, VV, (byte)0xF7 }};
            }
        else 
            {
            System.err.println("Can't emit key " + key);
            return new byte[0][0];
            }
        }
        

    public boolean parse(byte[] data, boolean ignorePatch)
        {
        // data starts at byte 16
        
        byte name[] = new byte[10];
        
        for(int i = 0; i < allParameters.length; i++)
            {
            byte val = data[i + 16];
                
            if (i >= 96) // name
                {
                name[i - 96] = val;
                }
            else if (allParameters[i].equals("instrument1voicenumbermsb") ||
            		allParameters[i].equals("instrument2voicenumbermsb") ||
            		allParameters[i].equals("instrument3voicenumbermsb") ||
            		allParameters[i].equals("instrument4voicenumbermsb") ||
            		allParameters[i].equals("instrument5voicenumbermsb") ||
            		allParameters[i].equals("instrument6voicenumbermsb") ||
            		allParameters[i].equals("instrument7voicenumbermsb") ||
            		allParameters[i].equals("instrument8voicenumbermsb"))
            	{
            	// ignore, we handle in lsb
            	}
            else if (allParameters[i].equals("instrument1voicenumberlsb") ||
            		allParameters[i].equals("instrument2voicenumberlsb") ||
            		allParameters[i].equals("instrument3voicenumberlsb") ||
            		allParameters[i].equals("instrument4voicenumberlsb") ||
            		allParameters[i].equals("instrument5voicenumberlsb") ||
            		allParameters[i].equals("instrument6voicenumberlsb") ||
            		allParameters[i].equals("instrument7voicenumberlsb") ||
            		allParameters[i].equals("instrument8voicenumberlsb"))
                {
                int instrument = (i / 12);
                int msb = data[16 + instrument * 12 + 1];
                int lsb = data[16 + instrument * 12 + 2];
				int combined = ((msb << 7) | lsb);

                model.set("instrument" + instrument + "voicenumber", combined);
                }
            else
                {
                model.set(allParameters[i], val);
                }
            }
                
		try { model.set("name", new String(name, "US-ASCII")); }
		catch (Exception e) { e.printStackTrace(); }
		
		revise();
        return true;
        }
    
    public int getPauseBetweenMIDISends()
        {
        // Wikipedia says that you have to have a 50ms wait-time between
        // sysex transmissions or the TX81Z has problems.  Maybe this might work?
        // Too crude?  Causes problems with real-time manipulation?

        return 50;
        }

    public byte[] emit(String key)
        {
        // we override emitAll(...) instead
        new RuntimeException("emit(key) should never have been called").printStackTrace();
        return new byte[0];
        }
    

    public byte[] emit(Model tempModel, boolean toWorkingMemory)
        {
        byte data[] = new byte[120];
        data[0] = (byte)'L';
        data[1] = (byte)'M';
        data[2] = (byte)' ';
        data[3] = (byte)' ';
        data[4] = (byte)'8';
        data[5] = (byte)'9';
        data[6] = (byte)'7';
        data[7] = (byte)'6';
        data[8] = (byte)'A';
        data[9] = (byte)'E';
		
        String name = model.get("name", "INIT SOUND") + "          ";
        // Next the PCED
        for(int i = 0; i < allParameters.length; i++)  // no name, no operatorenabled
            {
            if (allParameters[i].equals("instrument1voicenumbermsb") ||
            		allParameters[i].equals("instrument2voicenumbermsb") ||
            		allParameters[i].equals("instrument3voicenumbermsb") ||
            		allParameters[i].equals("instrument4voicenumbermsb") ||
            		allParameters[i].equals("instrument5voicenumbermsb") ||
            		allParameters[i].equals("instrument6voicenumbermsb") ||
            		allParameters[i].equals("instrument7voicenumbermsb") ||
            		allParameters[i].equals("instrument8voicenumbermsb"))
            	{
                int instrument = (i / 12);
            	data[i + 10] = (byte)(model.get("instrument" + i + "voicenumber", 0) >> 7);
            	}
            else if (allParameters[i].equals("instrument1voicenumberlsb") ||
            		allParameters[i].equals("instrument2voicenumberlsb") ||
            		allParameters[i].equals("instrument3voicenumberlsb") ||
            		allParameters[i].equals("instrument4voicenumberlsb") ||
            		allParameters[i].equals("instrument5voicenumberlsb") ||
            		allParameters[i].equals("instrument6voicenumberlsb") ||
            		allParameters[i].equals("instrument7voicenumberlsb") ||
            		allParameters[i].equals("instrument8voicenumberlsb"))
                {
                int instrument = (i / 12);
            	data[i + 10] = (byte)(model.get("instrument" + i + "voicenumber", 0) & 127);
	            }
	        else if (i >= 100)	// name
	        	{
	        	data[i + 10] = (byte)(name.charAt(i));
	        	}
	        else
	        	{
	        	data[i + 10] = (byte)(model.get(allParameters[i], 0));
	        	}
	        }
        
        byte[] result = new byte[128];
        result[0] = (byte)0xF0;
        result[1] = 0x43;
        result[2] = (byte)(32 + getChannelOut() - 1);
        result[3] = 0x7E;
        result[4] = 0x00;
        result[5] = 0x78;
        System.arraycopy(data, 0, result, 6, data.length);
        result[6 + data.length] = produceChecksum(data);
        result[6 + data.length + 1] = (byte)0xF7;
        
        return result;
        }

    /** Generate a TX81Z checksum of the data bytes */
    byte produceChecksum(byte[] bytes)
        {
        //      The TX81Z manual says the checksum is the
        //              "Twos complement of the lower 7 bits of the sum of all databytes".
        //
        //              Apparently this is mistaken.  Based on the function "Snapshot_Checksum" here...
        //              https://raw.githubusercontent.com/mgregory22/tx81z-programmer/master/src/snapshot.c
        //
        //              It may be otherwise.  So here's my shot.

        int checksum = 0;
        for(int i = 0; i < bytes.length; i++)
            checksum = (checksum + bytes[i]) & 255;
        return (byte)((256 - checksum) & 127);
        }


    public byte[] requestCurrentDump()
        {
        // PCED
        byte channel = (byte)(32 + getChannelOut() - 1);
        return new byte[] { (byte)0xF0, 0x43, channel, 0x7E, 
            (byte)'L', (byte)'M', (byte)' ', (byte)' ',
            (byte)'8', (byte)'9', (byte)'7', (byte)'6',
            (byte)'P', (byte)'E', (byte)0xF7 }; 
        }

    public static boolean recognize(byte[] data)
        {
        // PCED
        return (data.length == 128 &&        
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x43 &&
            // don't care about 2, it's the channel
            data[3] == 0x7E &&
            data[4] == 0x00 &&
            data[5] == 0x78 &&
            // next it spits out the header "LM  8976PE"
            data[6] == 'L' &&
            data[7] == 'M' &&
            data[8] == ' ' &&
            data[9] == ' ' &&
            data[10] == '8' &&
            data[11] == '9' &&
            data[12] == '7' &&
            data[13] == '6' &&
            data[14] == 'P' &&
            data[15] == 'E');
        }
        

    public static final int MAXIMUM_NAME_LENGTH = 10;
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
        
    public boolean requestCloseWindow() { return true; }

    public static String getSynthName() { return "Yamaha TX81Z [Multi]"; }
    
    public void changePatch(Model tempModel) 
        {
        int number = tempModel.get("number", 0);
        // Performance numbers PF1 ... PF24, corresponding to 161 .. 184
        byte lo = (byte)((number + 160) & 127);
        byte hi = (byte)((number + 160) >> 7);

        // Change program change table position 0 to what we want first
        byte[] table = new byte[9];
        table[0] = (byte)0xF0;
        table[1] = (byte)0x43;
        table[2] = (byte)(32 + getChannelOut() - 1);
        table[3] = (byte)0x10;
        table[4] = (byte)0xF7;  // really!
        table[5] = (byte)0xF7;  // we're changing table position 127
        table[6] = hi;
        table[7] = lo;
        table[8] = (byte)0xF7;
        
        tryToSendSysex(table);
        
        // Now let's do the PC
        
        try {
            tryToSendMIDI(new ShortMessage(ShortMessage.PROGRAM_CHANGE, getChannelOut() - 1, 0xF7, 0));
            }
        catch (Exception e) { e.printStackTrace(); }
        }

    public String getPatchName() { return null; }
    }
