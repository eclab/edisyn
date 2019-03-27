/***
    Copyright 2018 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.emumorpheus;

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
   A patch editor for the E-Mu Morpheus and Ultra Proteus synthesizers [MidiMap]
        
   @author Sean Luke
*/

public class EmuMorpheusMap extends Synth
    {

    public static final String[] BANKS = new String[] { "RAM", "Card" };

    public EmuMorpheusMap()
        {
        for(int i = 0; i < parameters.length; i++)
            {
            parametersToIndex.put(parameters[i], Integer.valueOf(i));
            }
            
        /// SOUND PANEL
                
        SynthPanel soundPanel = new SynthPanel(this);
        
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        JComponent nameGlobal = addNameGlobal(Style.COLOR_GLOBAL());
        vbox.add(nameGlobal);
        vbox.addLast(addChannel(1, Style.COLOR_B()));
        vbox.add(addFX(1, Style.COLOR_A()));
        vbox.add(addFX(2, Style.COLOR_A()));
    
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Global, Channel 1", soundPanel);
                
        soundPanel = new SynthPanel(this);

        vbox = new VBox();
        vbox.add(addChannel(2, Style.COLOR_A()));
        vbox.add(addChannel(3, Style.COLOR_B()));
        vbox.add(addChannel(4, Style.COLOR_A()));
        vbox.add(addChannel(5, Style.COLOR_B()));
        vbox.add(addChannel(6, Style.COLOR_A()));

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Channels 2-6", soundPanel);


        soundPanel = new SynthPanel(this);

        vbox = new VBox();
        vbox.add(addChannel(7, Style.COLOR_B()));
        vbox.add(addChannel(8, Style.COLOR_A()));
        vbox.add(addChannel(9, Style.COLOR_B()));
        vbox.add(addChannel(10, Style.COLOR_A()));
        vbox.add(addChannel(11, Style.COLOR_B()));

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Channels 7-11", soundPanel);

                
        soundPanel = new SynthPanel(this);

        vbox = new VBox();
        vbox.add(addChannel(12, Style.COLOR_A()));
        vbox.add(addChannel(13, Style.COLOR_B()));
        vbox.add(addChannel(14, Style.COLOR_A()));
        vbox.add(addChannel(15, Style.COLOR_B()));
        vbox.add(addChannel(16, Style.COLOR_A()));

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Channels 12-16", soundPanel);
        
                        
        model.set("name", "Untitled");
        model.set("bank", 0);
        model.set("number", 0);
                
        loadDefaults();        
        }
                
    public String getDefaultResourceFileName() { return "EmuMorpheusMap.init"; }
    public String getHTMLResourceFileName() { return "EmuMorpheusMap.html"; }

    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        JComboBox bank = new JComboBox(BANKS);
        int num = model.get("number");
        JTextField number = new JTextField("" + (num < 10 ? "00" : (num < 100 ? "0" : "")) + num, 3);
        
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
                showSimpleError(title, "The Patch Number must be an integer 0...15");
                continue;
                }
            if (n < 0 || n > 15)
                {
                showSimpleError(title, "The Patch Number must be an integer 0...15");
                continue;
                }
                                                
            int i = bank.getSelectedIndex();
                        
            change.set("bank", i);
            change.set("number", n);
                        
            return true;
            }
        }
        
    /** Add the global patch category (name, id, number, etc.) */
    public JComponent addNameGlobal(Color color)
        {
        Category globalCategory = new Category(this, "E-Mu Morpheus [Midimap]", color);           // Notice we've fixed the name
        globalCategory.makeUnresettable();
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
                                
        VBox vbox = new VBox();
        comp = new PatchDisplay(this, 7);
        vbox.add(comp);
        
        comp = new StringComponent("Patch Name", this, "name", 12, "Name must be up to 12 characters.")
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

        comp = new LabelledDial("Program Change Map", this, "progmap", color, -1, 3)
            {
            public String map(int val)
                {
                if (val == -1) return "None";
                else return "" + val;
                }
            };
        getModel().setMetricMin("progmap", 0);
        hbox.add(comp);


        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }   
        
    public static String[] FX_BUSES = new String[] { "Main", "Sub1", "Sub2" };             
    
    public int getFXType(int fx, int id)
        {
        int[] types = (fx == 1 ? FX_A_TYPES : FX_B_TYPES);
        for(int i = 0; i < types.length; i++)
            {
            if (types[i] == id) return i;
            }
        System.err.println("WARNING (EmuMorpheusMap getFXType): type " + id + " does not exist for FX " + fx);
        return 0;  // "No Effect"
        }
    
    public static int[] FX_A_TYPES = new int[] { 0, 1, 2, 16, 17, 7, 8, 18, 5, 6, 3, 4, 19, 20, 21, 22, 24, 23, 25, 11, 13, 12, 9, 10, 14 };
    
    public static String[] FX_A_NAMES = new String[]
    {
    "No Effect",
    "Room",
    "Warm Room",
    "Small Room 1",
    "Small Room 2",
    "Hall 1",
    "Hall 2",
    "Hall 3",
    "Chamber 1",
    "Chamber 2",
    "Plate 1",
    "Plate 2",
    "Early Reflections 1",
    "Early Reflections 2",
    "Early Reflections 3",
    "Early Reflections 4",
    "Reverse Early Reflections",
    "Rain",
    "Shimmer",
    "Stereo Flanger",
    "Phaser",
    "Stereo Chorus",
    "Delay",
    "Cross Delay",
    "Echo",
    };
        
    public static int[] FX_B_TYPES = new int[] { 128, 132, 138, 130, 133, 131, 134, 135, 129 };
    
    public static String[] FX_B_NAMES = new String[]
    {
    "No Effect",
    "Fuzz",
    "Fuzz Lite",
    "Stereo Flanger",
    "Phaser",
    "Stereo Chorus",
    "Delay",
    "Cross Delay",
    "Ring Modulate",
    };

    public static String[][] FX_A_PARAMETERS = new String[][]
    {
    { },
    { "Decay Time" },
    { "Decay Time" },
    { "Decay Time" },
    { "Decay Time" },
    { "Decay Time" },
    { "Decay Time" },
    { "Decay Time" },
    { "Decay Time" },
    { "Decay Time" },
    { "Decay Time" },
    { "Decay Time" },
    { "Ambience" },
    { "Ambience" },
    { "Ambience" },
    { "Ambience" },
    { "Ambience" },
    { "Decay Time" },
    { "Decay Time" },
    { "LFO Rate", "LFO Depth", "Min Delay", "Feedback" },
    { "LFO Rate", "LFO Depth", "Min Freq", "Feedback" },
    { "LFO Rate", "LFO Depth", "Min Delay", "Feedback" },
    { "L Delay Time", "L Tap Level", "R Delay Time", "R Tap Level" },
    { "R Delay Time", "R Tap Level", "L Delay Time", "L Tap Level" },       // yes, this appears to be backwards
    { "L Delay Time", "L Tap Level", "R Delay Time", "R Tap Level" },
    };

    public static int[][] FX_A_VALUES = new int[][]
    {
    { },
    { 50, 250, 150 },
    { 50, 250, 150 },
    { 10, 128, 100 },
    { 10, 128, 100 },
    { 100, 255, 140 },
    { 100, 255, 140 },
    { 100, 255, 140 },
    { 50, 200, 100 },
    { 50, 200, 100 },
    { 80, 200, 125 },
    { 80, 250, 180 },
    { 0, 100, 100 },
    { 0, 100, 50 },
    { 0, 100, 50 },
    { 0, 100, 50 },
    { 0, 100, 0 },
    { 100, 255, 180 },
    { 50, 250, 180 },
    { 0, 255, 50, 0, 255, 50, 1, 255, 1, -127, 127, -64 },
    { 0, 255, 50, 0, 255, 0, 1, 255, 1, 0, 127, 64 },
    { 0, 255, 50, 0, 255, 50, 1, 255, 2, -127, 127, 0 },
    { 1, 255, 255, 0, 127, 127, 1, 255, 200, 0, 127, 127, 0, 255, 200 },
    { 1, 255, 255, 0, 127, 127, 1, 255, 200, 0, 127, 127, 0, 255, 200 },
    { 1, 255, 255, 0, 127, 127, 1, 255, 200, 0, 127, 127, 0, 255, 200 }
    };

    public static String[][] FX_B_PARAMETERS = new String[][]
    {
    { },
    { "Input Filter", "Output Filter", "Output Volume" },
    { "Input Filter", "Output Filter" },
    { "LFO Rate", "LFO Depth", "Min Delay", "Feedback" },
    { "LFO Rate", "LFO Depth", "Min Freq", "Feedback" },
    { "LFO Rate", "LFO Depth", "Min Delay", "Feedback" },
    { "L Delay Time", "L Tap Level", "R Delay Time", "R Tap Level" },
    { "R Delay Time", "R Tap Level", "L Delay Time", "L Tap Level" },       // yes, this appears to be backwards
    { }
    };
        
    public static int[][] FX_B_VALUES = new int[][]
    {
    { },
    { 0, 255, 100, 0, 255, 100, 0, 127, 100 },
    { 0, 255, 100, 0, 255, 100 },
    { 0, 255, 50, 0, 255, 50, 1, 255, 1, -127, 127, -64 },
    { 0, 255, 50, 0, 255, 0, 1, 255, 1, 0, 127, 64 },
    { 0, 255, 50, 0, 255, 50, 1, 255, 1, -127, 127, 0 },
    { 1, 255, 255, 0, 127, 127, 1, 255, 200, 0, 127, 127, 0, 255, 200 },
    { 1, 255, 255, 0, 127, 127, 1, 255, 200, 0, 127, 127, 0, 255, 200 },
    { }
    };

    public JComponent generateFX(int fx, String[] params, final int[] values, Color color)
        {
        HBox hbox = new HBox();
        for(int i = 0; i < params.length; i++)
            {
            hbox.add(new LabelledDial(params[i], this, "fx" + fx + "parmvals" + (i + 1), color, values[0], values[1])
                {
                public int getDefaultValue() { return values[2]; }
                });
            }
        return hbox;
        }

    JComponent[][] fxComponents = new JComponent[2][];
    public JComponent addFX(final int fx, Color color)
        {
        Category category = new Category(this, "FX " + (fx == 1 ? "A" : "B"), color);

        int[][] fxvals = (fx == 1 ? FX_A_VALUES : FX_B_VALUES);
        String[][] fxparams = (fx == 1 ? FX_A_PARAMETERS: FX_B_PARAMETERS);
        String[] fxnames = (fx == 1 ? FX_A_NAMES: FX_B_NAMES);

        fxComponents[fx - 1] = new JComponent[fxvals.length];
        
        for(int i = 0; i < fxvals.length; i++)
            {
            fxComponents[fx - 1][i] = generateFX(fx, fxparams[i], fxvals[i], color);
            }

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        final HBox fxcomp = new HBox();
        
        VBox vbox = new VBox();
        params = fxnames;
        comp = new Chooser("Type", this, "fx" + fx + "type", params)
            {
            public void update(String key, Model model) 
                {
                super.update(key, model);
                fxcomp.removeAll();
                fxcomp.add(fxComponents[fx - 1][model.get(key, 0)]);
                fxcomp.revalidate();
                fxcomp.repaint();
                }
            };
        vbox.add(comp);
                
        params = FX_BUSES;
        comp = new Chooser("Bus", this, "fx" + fx + "bus", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Amount", this, "fx" + fx + "amt", color, 0, 100);
        hbox.add(comp);

        if (fx == 1)
            {
            comp = new LabelledDial("B -> A", this, "fx" + fx + "baamt", color, 0, 101)
                {
                public String map(int val)
                    {
                    if (val == 101)
                        return "B->A";
                    else return "" + val;
                    }
                };
        	getModel().setMetricMax("fx" + fx + "baamt", 100);
            hbox.add(comp);
            }
                
        hbox.add(fxcomp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public static final String[] PROGRAM_BANKS = new String[] { "RAM Presets", "ROM Presets", "RAM Hypers", "Card Presets", "Card Hypers" };
    public static final String[] MIX_BUS = new String[] { "Preset", "Main", "Sub1", "FX A", "FX B" };

    public JComponent addChannel(int ch, Color color)
        {
        Category category = new Category(this, "Channel " + ch, color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
 
        params = PROGRAM_BANKS;
        comp = new Chooser("Bank", this, "ch" + ch + "bank", params);
        vbox.add(comp);

        params = MIX_BUS;
        comp = new Chooser("Mix Bus", this, "ch" + ch + "mixbus", params);
        vbox.add(comp);
        hbox.add(vbox);
                
        comp = new LabelledDial("Number", this, "ch" + ch + "number", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Volume", this, "ch" + ch + "volume", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Pan", this, "ch" + ch + "pan", color, -8, 7)
            {
            public String map(int val)
                {
                if (val == -8) return "Preset";
                else return "" + val;
                }
            };
        getModel().setMetricMin("ch" + ch + "pan", -7);
        hbox.add(comp);

        vbox = new VBox();
        params = PROGRAM_BANKS;
        comp = new Chooser("MIDI Bank Select", this, "ch" + ch + "bankselect", params);
        vbox.add(comp);
        hbox.add(vbox);

        vbox = new VBox();
        comp = new CheckBox("Enable All", this, "ch" + ch + "enable");
        vbox.add(comp);

        comp = new CheckBox("Program Change", this, "ch" + ch + "progchgenbl");
        vbox.add(comp);

        comp = new CheckBox("Bank Change", this, "ch" + ch + "bankchgenbl");
        vbox.add(comp);
        hbox.add(vbox);

        vbox = new VBox();
        comp = new CheckBox("Volume Control", this, "ch" + ch + "volctlenbl");
        vbox.add(comp);

        comp = new CheckBox("Pan Control", this, "ch" + ch + "panctlenbl");
        vbox.add(comp);

        comp = new CheckBox("Pitch Wheel", this, "ch" + ch + "pwhlenbl");
        vbox.add(comp);
        hbox.add(vbox);

        vbox = new VBox();
        comp = new CheckBox("Mono Pressure", this, "ch" + ch + "mpressenbl");
        vbox.add(comp);

        comp = new CheckBox("Key Pressure", this, "ch" + ch + "ppressenbl");
        vbox.add(comp);

        comp = new CheckBox("Control A", this, "ch" + ch + "ctlaenbl");
        vbox.add(comp);
        hbox.add(vbox);

        vbox = new VBox();

        comp = new CheckBox("Control B", this, "ch" + ch + "ctlbenbl");
        vbox.add(comp);

        comp = new CheckBox("Control C", this, "ch" + ch + "ctlcenbl");
        vbox.add(comp);

        comp = new CheckBox("Control D", this, "ch" + ch + "ctldenbl");
        ((CheckBox)comp).addToWidth(2);
        vbox.add(comp);
        hbox.add(vbox);

        vbox = new VBox();
        comp = new CheckBox("Footswitch 1", this, "ch" + ch + "ftsw1enbl");
        ((CheckBox)comp).addToWidth(2);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new CheckBox("Footswitch 2", this, "ch" + ch + "ftsw2enbl");
        ((CheckBox)comp).addToWidth(2);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new CheckBox("Footswitch 3", this, "ch" + ch + "ftsw3enbl");
        ((CheckBox)comp).addToWidth(2);
        vbox.add(comp);
        hbox.add(vbox);

        category.add(hbox, BorderLayout.WEST);
        return category;
        }
                

    
        
    public byte getID() 
        { 
        try 
            { 
            byte b = (byte)(Byte.parseByte(tuple.id));
            if (b >= 0 && b < 16) return b;
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
            if (b >= 0 && b < 16) return "" + b;
            } 
        catch (NumberFormatException e) { }             // expected
        return "" + getID();
        }


    public Object[] emitAll(String key)
        {
        if (key.equals("bank")) return new Object[0];  // this is not emittable
        if (key.equals("number")) return new Object[0];  // this is not emittable

        if (key.equals("name"))
            {
            String name = model.get(key, "") + "            ";
            Object[] ret = new Object[12];
            for(int i = 0; i < 12; i++)
                {
                byte[] data = new byte[] { (byte)0xF0, (byte)0x18, (byte)0x0C, getID(), (byte)0x03, 0, 0, 0, 0, (byte)0xF7 };
                final int PARAM_OFFSET = 2048;
                int param = i + PARAM_OFFSET;
                        
                data[5] = (byte)(param % 128);
                data[6] = (byte)(param / 128);
                        
                int val = (byte)(name.charAt(i));
                if (val < 0) val = val + 16384;
                data[7] = (byte)(val % 128);
                data[8] = (byte)(val / 128);
                ret[i] = data;
                }
            return ret;
            }
        else 
            {            
            byte[] data = new byte[] { (byte)0xF0, (byte)0x18, (byte)0x0C, getID(), (byte)0x03, 0, 0, 0, 0, (byte)0xF7 };
            final int PARAM_OFFSET = 2048;
            int param = 0;
            
            if (key.startsWith("ch") && (key.endsWith("bank") || key.endsWith("number")))
                {
                try
                    {
                    int zone = Integer.parseInt(key.replaceAll("[^0-9]+", " ").trim());
                    param = ((Integer)(parametersToIndex.get("ch" + zone + "program"))).intValue() + PARAM_OFFSET;
                    }
                catch (Exception ex)
                    {
                    // shouldn't ever happen
                    ex.printStackTrace();
                    }
                }
            else param = ((Integer)(parametersToIndex.get(key))).intValue() + PARAM_OFFSET;;
                  
            data[5] = (byte)(param % 128);
            data[6] = (byte)(param / 128);
            
            int val = model.get(key, 0);

            if (key.startsWith("ch") && (key.endsWith("bank") || key.endsWith("number")))
                {
                try
                    {
                    int zone = Integer.parseInt(key.replaceAll("[^0-9]+", " ").trim());
                    val = model.get("ch" + zone + "bank") * 128 + model.get("ch" + zone + "number");
                    }
                catch (Exception ex)
                    {
                    // shouldn't ever happen
                    ex.printStackTrace();
                    }
                }
            else if (key.endsWith("mixbus"))
                {
                val =  val - 1;
                }
            else if (key.endsWith("fx1type"))
                {
                val = FX_A_TYPES[val];
                }
            else if (key.endsWith("fx2type"))
                {
                val = FX_B_TYPES[val];
                }

            if (val < 0) val = val + 16384;
            data[7] = (byte)(val % 128);
            data[8] = (byte)(val / 128);
            return new Object[] { data };
            }
        }



    public Object[] emitAll(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        if (tempModel == null)
            tempModel = getModel();

        int NN = tempModel.get("bank", 0) * 16 + tempModel.get("number", 0);
        byte[] data = new byte[parameters.length * 2 + 10];
        
        data[0] = (byte)0xF0;
        data[1] = (byte)0x18;
        data[2] = (byte)0x0C;
        data[3] = (byte)getID();
        data[4] = (byte)0x47;
        data[5] = (byte)0x01;
        data[6] = (byte)(NN % 128);
        data[7] = (byte)(NN / 128);
        
        int offset = 8;
        
        //// The E-Mu Morpheus and UltraProteus manuals do NOT say how to calculate
        //// the checksum.  But it appears to be simply the sum of the parameter bytes.
        
        int checksum = 0;
                
        // Load name
        String name = model.get("name", "") + "            ";
        for(int i = 0; i < 12; i++)
            {
            int val = (byte)(name.charAt(i));
            if (val < 0) val = val + 16384;
            data[offset++] = (byte)(val % 128);
            checksum += data[offset-1];
            data[offset++] = (byte)(val / 128);
            checksum += data[offset-1];
            }
                                
        // Load remaining parameters
        for(int i = 12; i < parameters.length; i++)
            {
            int val = model.get(parameters[i], 0);

            if (parameters[i].startsWith("ch") && (parameters[i].endsWith("program")))
                {
                try
                    {
                    int zone = Integer.parseInt(parameters[i].replaceAll("[^0-9]+", " ").trim());
                    val = model.get("ch" + zone + "bank") * 128 + model.get("ch" + zone + "number");
                    }
                catch (Exception ex)
                    {
                    // shouldn't ever happen
                    ex.printStackTrace();
                    }
                }
            else if (parameters[i].endsWith("mixbus"))
                {
                val =  val - 1;
                }
            else if (parameters[i].endsWith("fx1type"))
                {
                val = FX_A_TYPES[val];
                }
            else if (parameters[i].endsWith("fx2type"))
                {
                val = FX_B_TYPES[val];
                }
                        
            if (val < 0) val = val + 16384;
            data[offset++] = (byte)(val % 128);
            checksum += data[offset-1];
            data[offset++] = (byte)(val / 128);
            checksum += data[offset-1];
            }
              
        data[offset++] = (byte)(checksum & 127);
        data[offset++] = (byte)0xF7;

        Object[] result = new Object[] { data };
        return result;
        }
        
    public boolean getSendsParametersAfterNonMergeParse() { return true; }


    public int parse(byte[] data, boolean fromFile)
        {
        int NN = data[6] + data[7] * 128;
        model.set("bank", NN / 128);
        model.set("number", NN % 128);
        
        int offset = 8;
        
        // Load name
        char[] name = new char[12];
        for(int i = 0; i < 12; i++)
            {
            int val = (data[offset++] + data[offset++] * 128);
            name[i] = (char)val;
            }
        model.set("name", new String(name));
                                
        // Load remaining parameters
        for(int i = 12; i < parameters.length; i++)
            {
            int o = offset;
            int val = (data[offset++] + data[offset++] * 128);
            if (val >= 8192)
                val = val - 16384;

            if (parameters[i].startsWith("ch") && (parameters[i].endsWith("program")))
                {
                try
                    {
                    // this is just a guess...
                    int zone = Integer.parseInt(parameters[i].replaceAll("[^0-9]+", " ").trim());
                    model.set("ch" + zone + "bank", val / 128);
                    model.set("ch" + zone + "number", val % 128);
                    }
                catch (Exception ex)
                    {
                    // shouldn't ever happen
                    ex.printStackTrace();
                    }
                }
            else if (parameters[i].endsWith("mixbus"))
                val = val + 1;
                
            if (parameters[i].endsWith("fx1type"))
                {
                model.set(parameters[i], getFXType(1, val));
                }
            else if (parameters[i].endsWith("fx2type"))
                {
                model.set(parameters[i], getFXType(2, val));
                }
            else if (!parameters[i].equals("---"))
                {
                model.set(parameters[i], val);
                }
            }
                            
        revise();
        return PARSE_SUCCEEDED;
        }
    
        
    public void changePatch(Model tempModel)
        {
        // I don't think you can change the patch at all
        
        /*
          int bank = tempModel.get("bank", 0);
          if (bank == 2)  // card
          bank++;  // because the card's actual bank is bank 3
          int number = tempModel.get("number", 0);
          // It appears that the Morpheus / Ultraproteus are among
          // the rare machines which use 14-bit CCs
          tryToSendMIDI(buildLongCC(getChannelOut(), 0x00, bank));
          tryToSendMIDI(buildPC(getChannelOut(), number));
        */
        }

    public byte[] requestDump(Model tempModel)
        {
        int NN = tempModel.get("bank", 0) * 16 + tempModel.get("number", 0);
        byte[] data = new byte[8];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x18;
        data[2] = (byte)0x0C;
        data[3] = (byte)getID();
        data[4] = (byte)0x46;
        data[5] = (byte)(NN % 128);
        data[6] = (byte)(NN / 128);
        data[7] = (byte)0xF7;
        return data;
        }
        
    public JFrame sprout()
        {
        JFrame frame = super.sprout();
        receiveCurrent.setEnabled(false); // can't request current
        transmitTo.setEnabled(false);  // can't send to a given patch
        return frame;
        }
                
    public static boolean recognize(byte[] data)
        {
        return  data.length == 10 + parameters.length * 2 &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte) 0x18 &&
            data[2] == (byte) 0x0C &&
            data[4] == (byte) 0x47;
        }
        
    public static final int MAXIMUM_NAME_LENGTH = 12;
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
        
        String nm = model.get("name", "Untitled");
        String newnm = revisePatchName(nm);
        if (!nm.equals(newnm))
            model.set("name", newnm);
        }
            
    public static String getSynthName() { return "E-Mu Morpheus / Ultra Proteus [MidiMap]"; }
    public String getPatchName(Model model) { return model.get("name", "Untitled"); }
    
    public Model getNextPatchLocation(Model model)
        {
        int bank = model.get("bank");
        int number = model.get("number");
        
        number++;
        if (number >= 16)
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
        
        int number = (model.get("number"));
        return (BANKS[model.get("bank")] + "-" + (number > 99 ? "" : (number > 9 ? "0" : "00")) + number);
        }


    /** Map of parameter -> index in the allParameters array. */
    HashMap parametersToIndex = new HashMap();


    /** List of all Emu Morpheus parameters in order. */
                
    final static String[] parameters = new String[]
    {
    "---",    
    "---",    
    "---",    
    "---",    
    "---",    
    "---",    
    "---",    
    "---",    
    "---",    
    "---",    
    "---",    
    "---",   
    "ch1program",
    "ch1bankselect",
    "ch1volume",
    "ch1pan",
    "ch1mixbus",
    "ch1enable",
    "ch1progchgenbl",
    "ch1bankchgenbl",
    "ch1volctlenbl",
    "ch1panctlenbl",
    "ch1pwhlenbl",
    "ch1mpressenbl",
    "ch1ppressenbl",
    "ch1ctlaenbl",
    "ch1ctlbenbl",
    "ch1ctlcenbl",
    "ch1ctldenbl",
    "ch1ftsw1enbl",
    "ch1ftsw2enbl",
    "ch1ftsw3enbl",

    "ch2program",
    "ch2bankselect",
    "ch2volume",
    "ch2pan",
    "ch2mixbus",
    "ch2enable",
    "ch2progchgenbl",
    "ch2bankchgenbl",
    "ch2volctlenbl",
    "ch2panctlenbl",
    "ch2pwhlenbl",
    "ch2mpressenbl",
    "ch2ppressenbl",
    "ch2ctlaenbl",
    "ch2ctlbenbl",
    "ch2ctlcenbl",
    "ch2ctldenbl",
    "ch2ftsw1enbl",
    "ch2ftsw2enbl",
    "ch2ftsw3enbl",

    "ch3program",
    "ch3bankselect",
    "ch3volume",
    "ch3pan",
    "ch3mixbus",
    "ch3enable",
    "ch3progchgenbl",
    "ch3bankchgenbl",
    "ch3volctlenbl",
    "ch3panctlenbl",
    "ch3pwhlenbl",
    "ch3mpressenbl",
    "ch3ppressenbl",
    "ch3ctlaenbl",
    "ch3ctlbenbl",
    "ch3ctlcenbl",
    "ch3ctldenbl",
    "ch3ftsw1enbl",
    "ch3ftsw2enbl",
    "ch3ftsw3enbl",

    "ch4program",
    "ch4bankselect",
    "ch4volume",
    "ch4pan",
    "ch4mixbus",
    "ch4enable",
    "ch4progchgenbl",
    "ch4bankchgenbl",
    "ch4volctlenbl",
    "ch4panctlenbl",
    "ch4pwhlenbl",
    "ch4mpressenbl",
    "ch4ppressenbl",
    "ch4ctlaenbl",
    "ch4ctlbenbl",
    "ch4ctlcenbl",
    "ch4ctldenbl",
    "ch4ftsw1enbl",
    "ch4ftsw2enbl",
    "ch4ftsw3enbl",

    "ch5program",
    "ch5bankselect",
    "ch5volume",
    "ch5pan",
    "ch5mixbus",
    "ch5enable",
    "ch5progchgenbl",
    "ch5bankchgenbl",
    "ch5volctlenbl",
    "ch5panctlenbl",
    "ch5pwhlenbl",
    "ch5mpressenbl",
    "ch5ppressenbl",
    "ch5ctlaenbl",
    "ch5ctlbenbl",
    "ch5ctlcenbl",
    "ch5ctldenbl",
    "ch5ftsw1enbl",
    "ch5ftsw2enbl",
    "ch5ftsw3enbl",

    "ch6program",
    "ch6bankselect",
    "ch6volume",
    "ch6pan",
    "ch6mixbus",
    "ch6enable",
    "ch6progchgenbl",
    "ch6bankchgenbl",
    "ch6volctlenbl",
    "ch6panctlenbl",
    "ch6pwhlenbl",
    "ch6mpressenbl",
    "ch6ppressenbl",
    "ch6ctlaenbl",
    "ch6ctlbenbl",
    "ch6ctlcenbl",
    "ch6ctldenbl",
    "ch6ftsw1enbl",
    "ch6ftsw2enbl",
    "ch6ftsw3enbl",

    "ch7program",
    "ch7bankselect",
    "ch7volume",
    "ch7pan",
    "ch7mixbus",
    "ch7enable",
    "ch7progchgenbl",
    "ch7bankchgenbl",
    "ch7volctlenbl",
    "ch7panctlenbl",
    "ch7pwhlenbl",
    "ch7mpressenbl",
    "ch7ppressenbl",
    "ch7ctlaenbl",
    "ch7ctlbenbl",
    "ch7ctlcenbl",
    "ch7ctldenbl",
    "ch7ftsw1enbl",
    "ch7ftsw2enbl",
    "ch7ftsw3enbl",

    "ch8program",
    "ch8bankselect",
    "ch8volume",
    "ch8pan",
    "ch8mixbus",
    "ch8enable",
    "ch8progchgenbl",
    "ch8bankchgenbl",
    "ch8volctlenbl",
    "ch8panctlenbl",
    "ch8pwhlenbl",
    "ch8mpressenbl",
    "ch8ppressenbl",
    "ch8ctlaenbl",
    "ch8ctlbenbl",
    "ch8ctlcenbl",
    "ch8ctldenbl",
    "ch8ftsw1enbl",
    "ch8ftsw2enbl",
    "ch8ftsw3enbl",

    "ch9program",
    "ch9bankselect",
    "ch9volume",
    "ch9pan",
    "ch9mixbus",
    "ch9enable",
    "ch9progchgenbl",
    "ch9bankchgenbl",
    "ch9volctlenbl",
    "ch9panctlenbl",
    "ch9pwhlenbl",
    "ch9mpressenbl",
    "ch9ppressenbl",
    "ch9ctlaenbl",
    "ch9ctlbenbl",
    "ch9ctlcenbl",
    "ch9ctldenbl",
    "ch9ftsw1enbl",
    "ch9ftsw2enbl",
    "ch9ftsw3enbl",

    "ch10program",
    "ch10bankselect",
    "ch10volume",
    "ch10pan",
    "ch10mixbus",
    "ch10enable",
    "ch10progchgenbl",
    "ch10bankchgenbl",
    "ch10volctlenbl",
    "ch10panctlenbl",
    "ch10pwhlenbl",
    "ch10mpressenbl",
    "ch10ppressenbl",
    "ch10ctlaenbl",
    "ch10ctlbenbl",
    "ch10ctlcenbl",
    "ch10ctldenbl",
    "ch10ftsw1enbl",
    "ch10ftsw2enbl",
    "ch10ftsw3enbl",

    "ch11program",
    "ch11bankselect",
    "ch11volume",
    "ch11pan",
    "ch11mixbus",
    "ch11enable",
    "ch11progchgenbl",
    "ch11bankchgenbl",
    "ch11volctlenbl",
    "ch11panctlenbl",
    "ch11pwhlenbl",
    "ch11mpressenbl",
    "ch11ppressenbl",
    "ch11ctlaenbl",
    "ch11ctlbenbl",
    "ch11ctlcenbl",
    "ch11ctldenbl",
    "ch11ftsw1enbl",
    "ch11ftsw2enbl",
    "ch11ftsw3enbl",

    "ch12program",
    "ch12bankselect",
    "ch12volume",
    "ch12pan",
    "ch12mixbus",
    "ch12enable",
    "ch12progchgenbl",
    "ch12bankchgenbl",
    "ch12volctlenbl",
    "ch12panctlenbl",
    "ch12pwhlenbl",
    "ch12mpressenbl",
    "ch12ppressenbl",
    "ch12ctlaenbl",
    "ch12ctlbenbl",
    "ch12ctlcenbl",
    "ch12ctldenbl",
    "ch12ftsw1enbl",
    "ch12ftsw2enbl",
    "ch12ftsw3enbl",

    "ch13program",
    "ch13bankselect",
    "ch13volume",
    "ch13pan",
    "ch13mixbus",
    "ch13enable",
    "ch13progchgenbl",
    "ch13bankchgenbl",
    "ch13volctlenbl",
    "ch13panctlenbl",
    "ch13pwhlenbl",
    "ch13mpressenbl",
    "ch13ppressenbl",
    "ch13ctlaenbl",
    "ch13ctlbenbl",
    "ch13ctlcenbl",
    "ch13ctldenbl",
    "ch13ftsw1enbl",
    "ch13ftsw2enbl",
    "ch13ftsw3enbl",

    "ch14program",
    "ch14bankselect",
    "ch14volume",
    "ch14pan",
    "ch14mixbus",
    "ch14enable",
    "ch14progchgenbl",
    "ch14bankchgenbl",
    "ch14volctlenbl",
    "ch14panctlenbl",
    "ch14pwhlenbl",
    "ch14mpressenbl",
    "ch14ppressenbl",
    "ch14ctlaenbl",
    "ch14ctlbenbl",
    "ch14ctlcenbl",
    "ch14ctldenbl",
    "ch14ftsw1enbl",
    "ch14ftsw2enbl",
    "ch14ftsw3enbl",

    "ch15program",
    "ch15bankselect",
    "ch15volume",
    "ch15pan",
    "ch15mixbus",
    "ch15enable",
    "ch15progchgenbl",
    "ch15bankchgenbl",
    "ch15volctlenbl",
    "ch15panctlenbl",
    "ch15pwhlenbl",
    "ch15mpressenbl",
    "ch15ppressenbl",
    "ch15ctlaenbl",
    "ch15ctlbenbl",
    "ch15ctlcenbl",
    "ch15ctldenbl",
    "ch15ftsw1enbl",
    "ch15ftsw2enbl",
    "ch15ftsw3enbl",

    "ch16program",
    "ch16bankselect",
    "ch16volume",
    "ch16pan",
    "ch16mixbus",
    "ch16enable",
    "ch16progchgenbl",
    "ch16bankchgenbl",
    "ch16volctlenbl",
    "ch16panctlenbl",
    "ch16pwhlenbl",
    "ch16mpressenbl",
    "ch16ppressenbl",
    "ch16ctlaenbl",
    "ch16ctlbenbl",
    "ch16ctlcenbl",
    "ch16ctldenbl",
    "ch16ftsw1enbl",
    "ch16ftsw2enbl",
    "ch16ftsw3enbl",

    "progmap",
    "fx1type",
    "fx1parmvals1",
    "fx1parmvals2",
    "fx1parmvals3",
    "fx1parmvals4",
    "fx1parmvals5",
    "fx1parmvals6",
    "fx1parmvals7",
    "fx1parmvals7",
    "fx1parmvals9",
    "fx1parmvals10",
    "fx2type",
    "fx2parmvals1",
    "fx2parmvals2",
    "fx2parmvals3",
    "fx2parmvals4",
    "fx2parmvals5",
    "fx2parmvals6",
    "fx2parmvals7",
    "fx2parmvals7",
    "fx2parmvals9",
    "fx2parmvals10",
    "fx1amt",
    "fx2amt",
    "fx1baamt",
    "fx1bus",
    "fx2bus"
    };
            
    }
