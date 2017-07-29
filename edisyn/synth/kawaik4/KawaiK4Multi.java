/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.kawaik4;

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
   A patch editor for the Kawai K4 [Multi mode].
        
   @author Sean Luke
*/

public class KawaiK4Multi extends Synth
    {
    /// Various collections of parameter names for pop-up menus
        
    public static final String[] BANKS = { "A", "B", "C", "D", "Ext. A", "Ext. B", "Ext. C", "Ext. D" };
    public static final String[] BANKS_SHORT = { "A", "B", "C", "D" };
    public static final String[] KEYS = new String[] { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
    public static final String[] VELOCITY_SWITCHES = { "All", "Soft", "Loud" };
    public static final String[] PLAY_MODES = { "Keyboard", "MIDI", "Both" };
    public static final String[] SUBMIX_CHANNELS = { "A", "B", "C", "D", "E", "F", "G", "H" };

    public KawaiK4Multi()
        {
        for(int i = 0; i < allParameters.length; i++)
            {
            allParametersToIndex.put(allParameters[i], Integer.valueOf(i));
            }
                
        setSendsAllParametersInBulk(true);
        
        /// SOUND PANEL
                
        JComponent soundPanel = new SynthPanel();
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL));
        hbox.addLast(addGlobal(Style.COLOR_B));
        vbox.add(hbox);
        
        vbox.add(addSection(1, Style.COLOR_A));
        vbox.add(addSection(2, Style.COLOR_A));

        soundPanel.add(vbox, BorderLayout.CENTER);
        tabs.addTab("Global, Sections 1-2", soundPanel);
                

        SynthPanel sourcePanel = new SynthPanel();
        vbox = new VBox();
        
        vbox.add(addSection(3, Style.COLOR_A));
        vbox.add(addSection(4, Style.COLOR_A));
        vbox.add(addSection(5, Style.COLOR_A));
            
        sourcePanel.add(vbox, BorderLayout.CENTER);
        tabs.addTab("Sections 3-5", sourcePanel);

        sourcePanel = new SynthPanel();
        vbox = new VBox();
        
        vbox.add(addSection(6, Style.COLOR_A));
        vbox.add(addSection(7, Style.COLOR_A));
        vbox.add(addSection(8, Style.COLOR_A));

        sourcePanel.add(vbox, BorderLayout.CENTER);
        tabs.addTab("Sections 6-8", sourcePanel);
        
        tabs.addTab("About", new HTMLBrowser(this.getClass().getResourceAsStream("KawaiK4Multi.html")));

        model.set("name", "Init Sound V1.1 ");  // has to be 16 long
        
        //loadDefaults();        
        }
                
    public String getDefaultResourceFileName() { return "KawaiK4Multi.init"; }

    public boolean gatherInfo(String title, Model change, boolean writing)
        {
        JComboBox bank = new JComboBox(BANKS);
        bank.setSelectedIndex(model.get("bank", 0));
        
        JTextField number = new JTextField("" + (model.get("number", 0) + 1), 3);

        while(true)
            {
            boolean result = doMultiOption(this, new String[] { "Bank", "Patch Number"}, 
                new JComponent[] { bank, number }, title, "Enter the Bank and Patch number");
                
            if (result == false) 
                return false;
                                
            int n;
            try { n = Integer.parseInt(number.getText()); }
            catch (NumberFormatException e)
                {
                doSimpleError(title, "The Patch Number must be an integer 1...16");
                continue;
                }
            if (n < 1 || n > 16)
                {
                doSimpleError(title, "The Patch Number must be an integer 1...16");
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
        Category globalCategory = new Category(getSynthName(), color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
                
        VBox vbox = new VBox();
        HBox hbox2 = new HBox();
        comp = new PatchDisplay(this, "Patch", "bank", "number", 4)
            {
            public String numberString(int number) { number += 1; return (number > 9 ? "0" : "00") + number; }
            public String bankString(int bank) { return BANKS[bank]; }
            };
        hbox2.add(comp);
        vbox.add(hbox2);
        
        comp = new StringComponent("Patch Name", this, "name", 10, "Name must be up to 10 ASCII characters.")
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
        vbox.addBottom(comp);  // doesn't work right :-(
        hbox.add(vbox);

        // Not enough space to show the title
        hbox.addLast(Strut.makeHorizontalStrut(40));

        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }


    public JComponent addGlobal( Color color)
        {
        Category category = new Category("Global", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        comp = new LabelledDial("Volume", this, "volume", color, 0, 100);
        hbox.add(comp);

        comp = new LabelledDial("Effect", this, "effect", color, 0, 31, -1);
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }



    public JComponent addSection(int src, Color color)
        {
        Category category = new Category("Section " + src, color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = VELOCITY_SWITCHES;
        comp = new Chooser("Velocity Switch", this, "section" + src + "velocitysw", params);
        vbox.add(comp);

        params = PLAY_MODES;
        comp = new Chooser("Play Mode [K4]", this, "section" + src + "playmode", params);
        vbox.add(comp);
        hbox.add(vbox);
        
        vbox = new VBox();
        // Normally this is in global, but I think it makes more sense here
        comp = new CheckBox("Mute", this, "section" + src + "mute", true);
        vbox.add(comp);

        HBox hbox2 = new HBox();
        comp = new PushButton("Show")
            {
            public void perform()
                {
                final KawaiK4 synth = new KawaiK4();
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
                                tempModel.set("bank", KawaiK4Multi.this.model.get("instrument" + src + "bank", 0));
                                tempModel.set("number", KawaiK4Multi.this.model.get("instrument" + src + "number", 0));
                                synth.performRequestDump(tempModel, false);
                                }
                            });
                    }
                else
                	{
                	doSimpleError("Disconnected", "You can't show a patch when disconnected.");
                	}
                }
            };
        hbox2.addLast(comp);
        vbox.addBottom(hbox2);
        hbox.add(vbox);


        hbox.add(vbox);
                
                
                
                                
        comp = new LabelledDial("Bank", this, "section" + src + "bank", color, 0, 3)
            {
            public String map(int val)
                {
                // I believe that you can only refer to patches in your own memory region
                return BANKS_SHORT[val];
                }
            };
        hbox.add(comp);
        
        comp = new LabelledDial("Number", this, "section" + src + "number", color, 0, 16);
        hbox.add(comp);

        comp = new LabelledDial("Lowest", this, "section" + src + "lowkey", color, 0, 127)
            {
            public String map(int val)
                {
                return KEYS[val % 12] + (val / 12 - 2);  // note integer division
                }
            };
        ((LabelledDial)comp).setSecondLabel("Key");
        hbox.add(comp);

        comp = new LabelledDial("Highest", this, "section" + src + "highkey", color, 0, 127)
            {
            public String map(int val)
                {
                return KEYS[val % 12] + (val / 12 - 2);  // note integer division
                }
            };
        ((LabelledDial)comp).setSecondLabel("Key");
        hbox.add(comp);

        comp = new LabelledDial("Receive", this, "section" + src + "channel", color, 0, 15, -1);
        ((LabelledDial)comp).setSecondLabel("Channel");
        hbox.add(comp);

        comp = new LabelledDial("Submix", this, "section" + src + "submix", color, 0, 7)
            {
            public String map(int val)
                {
                return SUBMIX_CHANNELS[val];
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Level", this, "section" + src + "level", color, 0, 100);
        hbox.add(comp);

        comp = new LabelledDial("Transpose", this, "section" + src + "transpose", color, 0, 48, 24)
            {
            public boolean isSymmetric() { return true; }
            };
        hbox.add(comp);

        comp = new LabelledDial("Tune", this, "section" + src + "tune", color, 0, 100, 50);
        hbox.add(comp);


        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    /** Map of parameter -> index in the allParameters array. */
    HashMap allMatrixParametersToIndex = new HashMap();


    /** List of all 100 internal Oberheim numerical parameters in order.  Note that this is DIFFERENT, ugh,
        from the order of parameters in the sysex list, and is missing stuff like modulation and name. */
                
    final static String[] allMatrixParameters = new String[]
    {
    "dco1frequency", 
    "dco1frequencymod", 
    "dco1sync", 
    "dco1pulsewidth", 
    "dco1pulsewidthmod", 
    "dco1shape", 
    "dco1waveenable", 
    "dco1fixedmods1", 
    "dco1fixedmods2", 
    "dco1click", 
    "dco2frequency", 
    "dco2frequencymod", 
    "dco2detune", 
    "dco2pulsewidth", 
    "dco2pulsewidthmod", 
    "dco2shape", 
    "dco2waveenable", 
    "dco2fixedmods1", 
    "dco2fixedmods2", 
    "dco2click", 
    "mix", 
    "vcffrequency", 
    "vcffrequencymodenv", 
    "vcffrequencymodpressure", 
    "vcfresonance", 
    "vcffixedmods1", 
    "vcffixedmods2", 
    "vca1", 
    "vca1modvel", 
    "vca2modenv2", 
    "vcffm", 
    "vcffmmodenv3", 
    "vcffmmodpressure", 
    "trackingsource", 
    "trackingpoint1", 
    "trackingpoint2", 
    "trackingpoint3", 
    "trackingpoint4", 
    "trackingpoint5", 
    "-", 
    "ramp1rate", 
    "ramp1mode", 
    "ramp2rate", 
    "ramp2mode", 
    "portamento", 
    "portamentomod", 
    "portamentomode", 
    "portamentolegato", 
    "keyboardmode", 
    "-", 
    "env1delay", 
    "env1attack", 
    "env1decay", 
    "env1sustain", 
    "env1release", 
    "env1amplitude", 
    "envelope1amplitudemod", 
    "env1triggermode", 
    "env1mode", 
    "env1lfotriggermode", 
    "env2delay", 
    "env2attack", 
    "env2decay", 
    "env2sustain", 
    "env2release", 
    "env2amplitude", 
    "envelope2amplitudemod", 
    "env2triggermode", 
    "env2mode", 
    "env2lfotriggermode", 
    "env3delay", 
    "env3attack", 
    "env3decay", 
    "env3sustain", 
    "env3release", 
    "env3amplitude", 
    "envelope3amplitudemod", 
    "env3triggermode", 
    "env3mode", 
    "env3lfotriggermode", 
    "lfo1speed", 
    "lfo1speedmod", 
    "lfo1shape", 
    "lfo1retrigger", 
    "lfo1amplitude", 
    "lfo1amplitudemod", 
    "lfo1trigger", 
    "lfo1lag", 
    "lfo1source", 
    "-", 
    "lfo2speed", 
    "lfo2speedmod", 
    "lfo2shape", 
    "lfo2retrigger", 
    "lfo2amplitude", 
    "lfo2amplitudemod", 
    "lfo2trigger", 
    "lfo2lag", 
    "lfo2source", 
    "-"
    };


    /** Map of parameter -> index in the allParameters array. */
    HashMap allParametersToIndex = new HashMap();


    /** List of all Oberheim Sysex parameters in order.  "-" is a reserved (unused and thus unnamed) parameter. */

    /// * indicates parameters which must be handled specially due to packing
    /// that Waldorf decided to do.  :-(

    final static String[] allParameters = new String[/*100 or so*/] 
    {
    "-",                // this is the name, but the Matrix 1000 doesn't recognize names
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "keyboardmode",                   
    "dco1frequency",
    "dco1shape",
    "dco1pulsewidth",
    "dco1fixedmods1",           // *
    "dco1waveenable",           // *
    "dco2frequency",
    "dco2shape",
    "dco2pulsewidth",
    "dco2fixedmods1",           // *
    "dco2waveenable",           // *
    "dco2detune",
    "mix",
    "dco1fixedmods2",           // *
    "dco1click",
    "dco2fixedmods2",           // *
    "dco2click",
    "dco1sync",
    "vcffrequency",
    "vcfresonance",
    "vcffixedmods1",                // *
    "vcffixedmods2",                // *
    "vcffm",
    "vca1",
    "portamento",
    "portamentomode",               // *
    "portamentolegato",
    "lfo1speed",
    "lfo1trigger",
    "lfo1lag",
    "lfo1shape",
    "lfo1retrigger",
    "lfo1source",
    "lfo1amplitude",
    "lfo2speed",
    "lfo2trigger",
    "lfo2lag",
    "lfo2shape",
    "lfo2retrigger",
    "lfo2source",
    "lfo2amplitude",
    "env1triggermode",
    "env1delay",
    "env1attack",
    "env1decay",
    "env1sustain",
    "env1release",
    "env1amplitude",
    "env1lfotriggermode",
    "env1mode",
    "env2triggermode",
    "env2delay",
    "env2attack",
    "env2decay",
    "env2sustain",
    "env2release",
    "env2amplitude",
    "env2lfotriggermode",
    "env2mode",
    "env3triggermode",
    "env3delay",
    "env3attack",
    "env3decay",
    "env3sustain",
    "env3release",
    "env3amplitude",
    "env3lfotriggermode",
    "env3mode",
    "trackingsource",
    "trackingpoint1",
    "trackingpoint2",
    "trackingpoint3",
    "trackingpoint4",
    "trackingpoint5",
    "ramp1rate",
    "ramp1mode",
    "ramp2rate",
    "ramp2mode",
    "dco1frequencymod",
    "dco1pulsewidthmod",
    "dco2frequencymod",
    "dco2pulsewidthmod",
    "vcffrequencymodenv1",
    "vcffrequencymodpressure",
    "vca1modvel",
    "vca2modenv2",
    "envelope1amplitudemod",
    "envelope2amplitudemod",
    "envelope3amplitudemod",
    "lfo1amplitudemod",
    "lfo2amplitudemod",
    "portamentomod",
    "vcffmmodenv3",
    "vcffmmodpressure",
    "lfo1speedmod",
    "lfo2speedmod",
    "mod1source",
    "mod1destination",
    "mod1amount",
    "mod2source",
    "mod2destination",
    "mod2amount",
    "mod3source",
    "mod3destination",
    "mod3amount",
    "mod4source",
    "mod4destination",
    "mod4amount",
    "mod5source",
    "mod5destination",
    "mod5amount",
    "mod6source",
    "mod6destination",
    "mod6amount",
    "mod7source",
    "mod7destination",
    "mod7amount",
    "mod8source",
    "mod8destination",
    "mod8amount",
    "mod9source",
    "mod9destination",
    "mod9amount",
    "mod10source",
    "mod10destination",
    "mod10amount"
    };



    public byte[] emit(String key)
        {
        if (key.equals("bank")) return new byte[0];  // this is not emittable
        if (key.equals("number")) return new byte[0];  // this is not emittable

        int index = ((Integer)(allMatrixParametersToIndex.get(key))).intValue();
        int value = model.get(key, 0);

        if (key.equals("dco1lever1") || key.equals("dco1vibrato"))
            {
            index = ((Integer)(allMatrixParametersToIndex.get("dco1fixedmods1"))).intValue();
            value = model.get("dco1lever1", 0) |  (model.get("dco1vibrato", 0) << 1);
            }
        else if (key.equals("dco1portamento") || key.equals("dco1keytracking"))
            {
            index = ((Integer)(allMatrixParametersToIndex.get("dco1fixedmods2"))).intValue();
            value = model.get("dco1portamento", 0) | (model.get("dco1keytracking", 0) << 1);
            }
        if (key.equals("dco2lever1") || key.equals("dco2vibrato"))
            {
            index = ((Integer)(allMatrixParametersToIndex.get("dco2fixedmods1"))).intValue();
            value = model.get("dco2lever1", 0) | (model.get("dco2vibrato", 0) << 1);
            }
        else if (key.equals("dco2portamento") || key.equals("dco2keytracking"))
            {
            index = ((Integer)(allMatrixParametersToIndex.get("dco2fixedmods2"))).intValue();
            value = model.get("dco2portamento", 0) | (model.get("dco2keytracking", 0) << 1);
            }
        else if (key.equals("dco1wave") || key.equals("dco1pulse"))
            {
            index = ((Integer)(allMatrixParametersToIndex.get("dco1waveenable"))).intValue();
            value = model.get("dco1wave", 0) | (model.get("dco1pulse", 0) << 1);
            }
        else if (key.equals("dco2wave") || key.equals("dco2pulse") || key.equals("dco2noise"))
            {
            index = ((Integer)(allMatrixParametersToIndex.get("dco2waveenable"))).intValue();
            value = model.get("dco2wave", 0) | (model.get("dco2pulse", 0) << 1) | (model.get("dco2noise", 0) << 2);
            }
        else if (key.equals("vcflever1") || key.equals("vcfvibrato"))
            {
            index = ((Integer)(allMatrixParametersToIndex.get("vcffixedmods1"))).intValue();
            value = model.get("vcflever1", 0) | (model.get("vcfvibrato", 0) << 1);
            }
        else if (key.equals("vcfportamento") || key.equals("vcfkeytracking"))
            {
            index = ((Integer)(allMatrixParametersToIndex.get("vcffixedmods2"))).intValue();
            value = model.get("vcfportamento", 0) | (model.get("vcfkeytracking", 0) << 1);
            }
        else if (key.startsWith("mod"))
            {
            int modnumber = (int)(key.charAt(3) - '0');
            if (key.charAt(4) == '0') // it's 10
                modnumber = 10;

            int modsource = model.get("mod" + modnumber  + "source", 0);
            int moddestination = model.get("mod" + modnumber  + "destination", 0) + 1;  // IMPORTANT it  goes 1--32, not 0--31
            int modamount = model.get("mod" + modnumber  + "amount", 0);
            modnumber--;

            return new byte[] { (byte)0xF0, 0x10, 0x06, 0x0B, (byte)modnumber, (byte)modsource, (byte) modamount, (byte)moddestination, (byte)0xF7 };
            }
        // don't need to customize portamentomode though we'll have to do it on parse
                
        //else if (key.equals("portamentomode"))
        //              {
        //              // two things are both exponential
        //              }

        byte VV = (byte)(value);
        byte PP = (byte)(index & 127);
        return new byte[] { (byte)0xF0, 0x10, 0x06, PP, VV, (byte)0xF7 };
        }
    


    public void parseParameter(byte[] data)
        {
        if (data[3] == 0x0B)            // remote modulation parameter edit
            {
            byte modulation = (byte)(data[4] + 1);  // stored as 1...10, not 0...9
            byte source = data[5];
            byte amount = data[6];
            byte destination = data[7];
                
            model.set("mod" + modulation + "source", source);
            model.set("mod" + modulation + "destination", destination);
            model.set("mod" + modulation + "amount", amount);
            }
        else if (data[3] == 0x06)               // remote basic parameter edit
            {
            byte parameter = data[4];
            byte value = data[5];
                
            String key = allMatrixParameters[parameter];
            if (key.equals("dco1fixedmods1"))
                {
                model.set("dco1lever1", value & 1);
                model.set("dco1vibrato", (value >> 1) & 1);
                }
            else if (key.equals("dco1fixedmods2"))
                {
                model.set("dco1portamento", value & 1);
                model.set("dco1keytracking", (value >> 1) & 1);
                }
            if (key.equals("dco2fixedmods1"))
                {
                model.set("dco2lever1", value & 1);
                model.set("dco2vibrato", (value >> 1) & 1);
                }
            else if (key.equals("dco2fixedmods2"))
                {
                model.set("dco2portamento", value & 1);
                model.set("dco2keytracking", (value >> 1) & 1);
                }
            else if (key.equals("dco1waveenable"))
                {
                model.set("dco1wave", value & 1);
                model.set("dco1pulse", (value >> 1) & 1);
                }
            else if (key.equals("dco2waveenable"))
                {
                model.set("dco2wave", value & 1);
                model.set("dco2pulse", (value >> 1) & 1);
                model.set("dco2noise", (value >> 2) & 1);
                }
            else if (key.equals("vcffixedmods1"))
                {
                model.set("vcflever1", value & 1);
                model.set("vcfvibrato", (value >> 1) & 1);
                }
            else if (key.equals("vcffixedmods2"))
                {
                model.set("vcfportamento", value & 1);
                model.set("vcfkeytracking", (value >> 1) & 1);
                }
            else if (key.equals("portamentomode"))
                {
                if (value == 4)
                    value = (byte)3;  // get rid of extra exponential
                model.set("portamentomode", value);
                }
            else
                {
                model.set(key, value);
                }
            }
        else
            {
            // we'll put CC here later
            }
        revise();
        }
        
    public boolean parse(byte[] data, boolean ignorePatch)
        {
        // this stuff requires a checksum
        // and required packing by two nibbles per byte (see http://www.youngmonkey.ca/nose/audio_tech/synth/Oberheim-KawaiK4.html)
        revise();
        return true;            // change this as appropriate
        }
    
    
    public byte[] emit(Model tempModel, boolean toWorkingMemory)
        {
        // this stuff requires a checksum
        // and required packing by two nibbles per byte (see http://www.youngmonkey.ca/nose/audio_tech/synth/Oberheim-KawaiK4.html)
        // also note that writes are probably always to working memory.
        // In order to STORE the working memory to the backing store
        // You do STORE EDIT BUFFER:
        // F0H 10H 06H 0EH PATCHNUMBER BANKNUMBER 0x0 F7H
        // At present we do NOT SUPPORT GROUP MODE (which is some kind of ID mechanism with retransmission)
        return new byte[0];
        }


    public byte[] requestDump(Model tempModel)
        {
        // It is not clear what this format is.
        // The 6R transmits 0xF0, 0x10, 0x06, 0x04,
        // then transmits 0=ALL PATCHES, 1=SINGLE PATCH,
        // 2=SPLIT PATCH, 3=MASTER DATA,
        // then transmits PATCH NUMBER (or 0 if ALL PATCHES or MASTER DATA)
        // See http://www.youngmonkey.ca/nose/audio_tech/synth/Oberheim-Matrix6R.html
        //
        // This isn't enough for the Matrix 1000, where the patch
        // number must be 1000 values.  
        // The Matrix 1000 format is vague
        // See http://www.youngmonkey.ca/nose/audio_tech/synth/Oberheim-KawaiK4.html
        // One possibility is: 0xF0, 0x10, 0x06, 0x04,
        // then 0x01 (for single patch),
        // then PATCH BANK
        // then PATCH NUMBER
        // but it's not clear, we have to do some testing
        return new byte[0];
        }
        
    public byte[] requestDump(int bank, int number, int id)
        {
        // It is not clear what this format is.
        // The 6R transmits 0xF0, 0x10, 0x06, 0x04,
        // then transmits 0=ALL PATCHES, 1=SINGLE PATCH,
        // 2=SPLIT PATCH, 3=MASTER DATA,
        // then transmits PATCH NUMBER (or 0 if ALL PATCHES or MASTER DATA)
        // See http://www.youngmonkey.ca/nose/audio_tech/synth/Oberheim-Matrix6R.html
        //
        // This isn't enough for the Matrix 1000, where the patch
        // number must be 1000 values.  
        // The Matrix 1000 format is vague
        // See http://www.youngmonkey.ca/nose/audio_tech/synth/Oberheim-KawaiK4.html
        // One possibility is: 0xF0, 0x10, 0x06, 0x04,
        // then 0x01 (for single patch),
        // then PATCH BANK
        // then PATCH NUMBER
        // Another more likely possibility is to SET THE BANK,
        // with F0H 10H 06H 0AH BANKNUMBER F7H
        // and then issue a PATCH REQUEST
        // as  0xF0, 0x10, 0x06, 0x04,
        // then 0x01 (for single patch),
        // then PATCH NUMBER
        return new byte[0];
        }
        
    public byte[] requestCurrentDump(Model tempModel)
        {
        // There is no current dump command.  We probably should
        // just do a requestDump on the model, assuming that the
        // bank and number are right
        if (tempModel == null)
            tempModel = getModel();
        return requestDump(tempModel);
        }

    public static boolean recognize(byte[] data)
        {
        boolean v = (
            data.length == EXPECTED_SYSEX_LENGTH &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x3E &&
            data[2] == (byte)0x0E &&
            data[4] == (byte)0x10);
        return v;
        }
        

    public static final int EXPECTED_SYSEX_LENGTH = 265;        
        
        
    public static final int MAXIMUM_NAME_LENGTH = 10;
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
                
        if (key.equals("effecttype"))
            {
            // handle XT effects specially
            if (b == 32) // delay
                b = 8;
            else if (b == 33)  // pan delay
                b = 9;
            else if (b == 34)  // mod delay
                b = 10;
            }


        if (key.equals("-"))
            {
            // do nothing
            }
        else if (key.equals("osc1octave") || key.equals("osc2octave"))
            {
            model.set(key, (b - 16) / 12);
            }
        else if (key.equals("lfo1sync") || key.equals("lfo2sync"))
            {
            if (b == 3)
                b = 2;          // because it's of/on/on/Clock, I dunno why
            model.set(key, b);
            }
        else if (key.equals("arpuser1"))
            {
            model.set("arpuser1", (b >> 3) & 1);                    /// Do I have these backwards?
            model.set("arpuser2", (b >> 2) & 1);
            model.set("arpuser3", (b >> 1) & 1);
            model.set("arpuser4", (b) & 1);
            }
        else if (key.equals("arpuser5"))
            {
            model.set("arpuser5", (b >> 3) & 1);                    /// Do I have these backwards?
            model.set("arpuser6", (b >> 2) & 1);
            model.set("arpuser7", (b >> 1) & 1);
            model.set("arpuser8", (b) & 1);
            }
        else if (key.equals("arpuser9"))
            {
            model.set("arpuser9", (b >> 3) & 1);                    /// Do I have these backwards?
            model.set("arpuser10", (b >> 2) & 1);
            model.set("arpuser11", (b >> 1) & 1);
            model.set("arpuser12", (b) & 1);
            }
        else if (key.equals("arpuser13"))
            {
            model.set("arpuser13", (b >> 3) & 1);                   /// Do I have these backwards?
            model.set("arpuser14", (b >> 2) & 1);
            model.set("arpuser15", (b >> 1) & 1);
            model.set("arpuser16", (b) & 1);
            }
        else if (i >= 240 && i < 240 + 16)  // name
            {
            try 
                {
                String name = model.get("name", "Init Sound V1.1 ") + "          ";
                byte[] str = name.getBytes("US-ASCII");
                byte[] newstr = new byte[] { 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20 };
                System.arraycopy(str, 0, newstr, 0, 16);
                newstr[i - 240] = b;
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

            
    public boolean requestCloseWindow() { return true; }

    public static String getSynthName() { return "Kawai K4 [Multi]"; }
    
    public String getPatchName() { return null; }
    

    public void changePatch(Model tempModel)
    	{
    	byte BB = (byte)tempModel.get("bank", 0);
        byte NN = (byte)tempModel.get("number", 0);
        
        // first switch to internal or external
        byte[] data = new byte[8];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x40;
        data[2] = (byte)getChannelOut();
        data[3] = (byte)0x30;
        data[4] = (byte)0x04;
        data[5] = (byte)(BB < 4 ? 0x00 : 0x02);	// 0x00 is internal, 0x02 is external
        data[7] = (byte)0xF7;
        
        tryToSendSysex(data);
        
        // Next do a PC
        
        if (BB > 4) BB -= 4;
        int PC = (BB * 16 + NN) + 64;		// + 64 for "Multi"
        try 
        	{
            tryToSendMIDI(new ShortMessage(ShortMessage.PROGRAM_CHANGE, getChannelOut() - 1, PC, 0));
            }
        catch (Exception e) { e.printStackTrace(); }
    	}
    }
