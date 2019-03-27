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
   A patch editor for the E-Mu Morpheus and Ultra Proteus synthesizers [Hyperpresets]
        
   @author Sean Luke
*/

public class EmuMorpheusHyper extends Synth
    {
    public EmuMorpheusHyper()
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
        hbox.add(nameGlobal);
        hbox.addLast(addZone(1, Style.COLOR_B()));
        vbox.add(hbox);
        vbox.add(addFunctionGenerator(1, Style.COLOR_A()));
    
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Global, Zone 1", soundPanel);
                
        soundPanel = new SynthPanel(this);

        vbox = new VBox();
        vbox.add(addZone(2, Style.COLOR_A()));
        vbox.add(addZone(3, Style.COLOR_B()));
        vbox.add(addZone(4, Style.COLOR_A()));
        vbox.add(addZone(5, Style.COLOR_B()));
        vbox.add(addZone(6, Style.COLOR_A()));

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Zones 2-6", soundPanel);


        soundPanel = new SynthPanel(this);

        vbox = new VBox();
        vbox.add(addZone(7, Style.COLOR_B()));
        vbox.add(addZone(8, Style.COLOR_A()));
        vbox.add(addZone(9, Style.COLOR_B()));
        vbox.add(addZone(10, Style.COLOR_A()));
        vbox.add(addZone(11, Style.COLOR_B()));

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Zones 7-11", soundPanel);

                
        soundPanel = new SynthPanel(this);

        vbox = new VBox();
        vbox.add(addZone(12, Style.COLOR_A()));
        vbox.add(addZone(13, Style.COLOR_B()));
        vbox.add(addZone(14, Style.COLOR_A()));
        vbox.add(addZone(15, Style.COLOR_B()));
        vbox.add(addZone(16, Style.COLOR_A()));

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Zones 12-16", soundPanel);
        
                        
        model.set("name", "Untitled");
        model.set("bank", 0);
        model.set("number", 0);
                
        loadDefaults();        
        }
                
    public String getDefaultResourceFileName() { return "EmuMorpheusHyper.init"; }
    public String getHTMLResourceFileName() { return "EmuMorpheusHyper.html"; }

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
                showSimpleError(title, "The Patch Number must be an integer 0...127");
                continue;
                }
            if (n < 0 || n > 127)
                {
                showSimpleError(title, "The Patch Number must be an integer 0...127");
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
        Category globalCategory = new Category(this, "E-Mu Morpheus [Hyper]", color);           // Notice we've fixed the name
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

        comp = new LabelledDial("Port Mode", this, "portmode", color, 0, 5)
            {
            public String map(int val)
                {
                if (val == 0) return "Mono";
                else return "Poly " + val;
                }
            };
        getModel().setMetricMin("portmode", 1);
        hbox.add(comp);

        hbox.add(Strut.makeHorizontalStrut(5));

        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }                
                

    public JComponent addFunctionGenerator(int fg, Color color)
        {
        Category category = new Category(this, "Function Generator", color);

        JComponent comp;
        String[] params;
        VBox main = new VBox();
        HBox hbox = new HBox();
        
        for(int i = 1; i < 9; i++)
            {
            VBox vbox = new VBox();

            params = FUNCTION_GENERATOR__TYPES;
            comp = new Chooser("" + i + " Type", this, "fgseg" + i + "type", params);
            vbox.add(comp);

            params = FUNCTION_GENERATORS;
            comp = new Chooser("" + i + " Shape", this, "fgseg" + i + "shape", params);
            vbox.add(comp);

            params = FUNCTION_GENERATOR_CONDITIONS;
            comp = new Chooser("" + i + " Jump Condition", this, "fgseg" + i + "condjump", params);
            vbox.add(comp);
            hbox.add(vbox);
            
            VBox vbox2 = new VBox();
            HBox hbox2 = new HBox();
            comp = new LabelledDial("" + i + " Level", this, "fgseg" + i + "level", color, -127, 127);
            hbox2.add(comp);
                        
            comp = new LabelledDial("" + i + " Time", this, "fgseg" + i + "time", color, 0, 4095);
            hbox2.add(comp);
            vbox2.add(hbox2);
            
            hbox2 = new HBox();
            comp = new LabelledDial("" + i + " Cond Val", this, "fgseg" + i + "condval", color, -127, 127);
            hbox2.add(comp);

            comp = new LabelledDial("" + i +  " Dest Seg", this, "fgseg" + i + "destseg", color, 0, 7);
            hbox2.add(comp);
            vbox2.add(hbox2);
            hbox.add(vbox2);

            if (i == 4 || i == 8)
                {
                main.add(hbox);
                hbox = new HBox();
                }
            }

        main.add(Strut.makeVerticalStrut(10));

        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "fgseg1time", "fgseg2time", "fgseg3time", "fgseg4time", "fgseg5time", "fgseg6time", "fgseg7time", "fgseg8time" },
            new String[] { null, "fgseg1level", "fgseg2level", "fgseg3level", "fgseg4level", "fgseg5level", "fgseg6level", "fgseg7level", "fgseg8level" },
            new double[] { 0, 0.125 / 4095, 0.125 / 4095, 0.125 / 4095, 0.125 / 4095, 0.125 / 4095, 0.125 / 4095, 0.125 / 4095, 0.125 / 4095 },
            new double[] { 0, 1.0 / 128, 1.0 / 128,1.0 / 128,1.0 / 128,1.0 / 128,1.0 / 128,1.0 / 128,1.0 / 128 }
            );
        ((EnvelopeDisplay)comp).setSigned(true);
        ((EnvelopeDisplay)comp).setAxis(0.5);        
        hbox.addLast(comp);
        main.add(hbox);

        category.add(main, BorderLayout.CENTER);
        return category;
        }


    public JComponent addZone(int zone, Color color)
        {
        Category category = new Category(this, "Zone " + zone, color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
 
        params = BANKS;
        comp = new Chooser("Bank", this, "z" + zone + "bank", params);
        vbox.add(comp);
        hbox.add(vbox);
                
        comp = new LabelledDial("Number", this, "z" + zone + "number", color, -1, 127)
            {
            public String map(int val)
                {
                if (val == -1) return "None";
                else return "" + val;
                }
            };
        getModel().setMetricMin("z" + zone + "number", 0);
        hbox.add(comp);

        comp = new LabelledDial("Volume", this, "z" + zone + "volume", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Pan", this, "z" + zone + "pan", color, -14, 14);
        hbox.add(comp);

        comp = new LabelledDial("Low Key", this, "z" + zone + "lowkey", color, 0, 127)
            {
            public String map(int val)
                {
                return "" + NOTES[val % 12] + "" + (val / 12 - 2);
                }
            };
        hbox.add(comp);
                
        comp = new LabelledDial("Hi Key", this, "z" + zone + "highkey", color, 0, 127)
            {
            public String map(int val)
                {
                return "" + NOTES[val % 12] + "" + (val / 12 - 2);
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Low Vel", this, "z" + zone + "lowvel", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("High Vel", this, "z" + zone + "highvel", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Vel Offset", this, "z" + zone + "veloffset", color, -126, 126);
        hbox.add(comp);

        comp = new LabelledDial("Transpose", this, "z" + zone + "xpose", color, -36, 36);
        hbox.add(comp);

        comp = new LabelledDial("Coarse Tune", this, "z" + zone + "coarsetune", color, -36, 36);
        hbox.add(comp);

        comp = new LabelledDial("Fine Tune", this, "z" + zone + "finetune", color, -64, 64);
        hbox.add(comp);

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
                final int PARAM_OFFSET = 8704;
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
            final int PARAM_OFFSET = 8704;
            int param = 0;
            if (key.startsWith("fg") &&
                (key.endsWith("level") || key.endsWith("type")))
                {
                String base = key.substring(0,6);
                param = ((Integer)(parametersToIndex.get(base + "typelevel"))).intValue();
                }
            else if (key.startsWith("z") && (key.endsWith("bank") || key.endsWith("number")))
                {
                try
                    {
                    int zone = Integer.parseInt(key.replaceAll("[^0-9]+", " ").trim());
                    param = ((Integer)(parametersToIndex.get("z" + zone + "preset"))).intValue() + PARAM_OFFSET;
                    }
                catch (Exception ex)
                    {
                    // shouldn't ever happen
                    ex.printStackTrace();
                    }
                }
            else
                {
                param = ((Integer)(parametersToIndex.get(key))).intValue() + PARAM_OFFSET;
                }
                            
            data[5] = (byte)(param % 128);
            data[6] = (byte)(param / 128);
            
            int val = model.get(key, 0);

            if (key.startsWith("fg") &&
                (key.endsWith("level") || key.endsWith("type")))
                {
                String base = key.substring(0,6);
                int level = model.get(base + "level");
                if (level < 0) level += 256;
                                
                val = (model.get(base + "type", 0) << 8) | level;
                }
            else if (key.startsWith("z") && (key.endsWith("bank") || key.endsWith("number")))
                {
                try
                    {
                    int zone = Integer.parseInt(key.replaceAll("[^0-9]+", " ").trim());
                    if (model.get("z" + zone + "number") == -1)
                        val = -1;
                    else
                        val = model.get("z" + zone + "bank") * 128 + model.get("z" + zone + "number");
                    }
                catch (Exception ex)
                    {
                    // shouldn't ever happen
                    ex.printStackTrace();
                    }
                }
            else if (key.startsWith("fgseg") && (key.endsWith("condjump")))
                {
                if (val >= 6) val += 2;         // there's a gap that we can't do
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

        int NN = tempModel.get("bank", 0) * 128 + tempModel.get("number", 0);
        if (toWorkingMemory)
            {
            NN = 126;               // we write to the top patch number of bank 0 (RAM)
            // furthermore we have to change the patch because sendAllParameters
            // doesn't do it by default
            Model model = new Model();
            model.set("bank", 0);
            model.set("number", NN);
            changePatch(model);
            }
        byte[] data = new byte[parameters.length * 2 + 10];
        
        data[0] = (byte)0xF0;
        data[1] = (byte)0x18;
        data[2] = (byte)0x0C;
        data[3] = (byte)getID();
        data[4] = (byte)0x45;
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

            if (parameters[i].endsWith("typelevel"))
                {
                String base = parameters[i].substring(0,6);
                int level = model.get(base + "level");
                if (level < 0) level += 256;
                                
                val = (model.get(base + "type", 0) << 8) | level;
                }
            else if (parameters[i].startsWith("z") && (parameters[i].endsWith("preset")))
                {
                try
                    {
                    int zone = Integer.parseInt(parameters[i].replaceAll("[^0-9]+", " ").trim());
                    if (model.get("z" + zone + "number") == -1)
                        val = -1;
                    else
                        val = model.get("z" + zone + "bank") * 128 + model.get("z" + zone + "number");
                    }
                catch (Exception ex)
                    {
                    // shouldn't ever happen
                    ex.printStackTrace();
                    }
                }
            else if (parameters[i].startsWith("fgseg") && (parameters[i].endsWith("condjump")))
                {
                if (val >= 6) val += 2;         // there's a gap that we can't do
                }
                       
            if (val < 0) val = val + 16384;
            data[offset++] = (byte)(val % 128);
            checksum += data[offset-1];
            data[offset++] = (byte)(val / 128);
            checksum += data[offset-1];
            }
              
        data[offset++] = (byte)(checksum & 127);
        data[offset++] = (byte)0xF7;

        //if (toWorkingMemory | toFile)
        //    {
        Object[] result = new Object[] { data };
        return result;
        //    }
        /*
          else // need to do a write
          {
          Object[] result = new Object[2];
          result[0] = data;
          result[1] = new byte[] { (byte)0xF0, (byte)0x18, (byte)0x0C, (byte)getID(), (byte)0x22, (byte)0, (byte)NN, (byte)0xF7 };
          return result;
          }
        */
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

            if (parameters[i].endsWith("typelevel"))
                {
                int type = ((val >> 8) & 3);
                int level = (val & 255);
                if (level > 127)
                    level -= 256;
                String base = parameters[i].substring(0,6);
                model.set(base + "type", type);
                model.set(base + "level", level);
                }
            else if (parameters[i].startsWith("z") && (parameters[i].endsWith("preset")))
                {
                try
                    {
                    // this is just a guess...
                    int zone = Integer.parseInt(parameters[i].replaceAll("[^0-9]+", " ").trim());
                    model.set("z" + zone + "bank", val / 128);
                    model.set("z" + zone + "number", val % 128);
                    }
                catch (Exception ex)
                    {
                    // shouldn't ever happen
                    ex.printStackTrace();
                    }
                }
            else
                {                  
                if (parameters[i].startsWith("fgseg") && (parameters[i].endsWith("condjump")))
                    {
                    if (val >= 8) val -= 2;         // there's a gap that we can't do
                    }

                if (!parameters[i].equals("---"))
                    model.set(parameters[i], val);
                }
            }
                            
        revise();
        return PARSE_SUCCEEDED;
        }
    
        
    public void changePatch(Model tempModel)
        {
        int bank = tempModel.get("bank", 0);
        if (bank == 2)  // card
            bank++;  // because the card's actual bank is bank 3
        int number = tempModel.get("number", 0);
        // It appears that the Morpheus / Ultraproteus are among
        // the rare machines which use 14-bit CCs
        tryToSendMIDI(buildLongCC(getChannelOut(), 0x00, bank));
        tryToSendMIDI(buildPC(getChannelOut(), number));
        }

    public byte[] requestDump(Model tempModel)
        {
        int NN = tempModel.get("bank", 0) * 128 + tempModel.get("number", 0);
        byte[] data = new byte[8];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x18;
        data[2] = (byte)0x0C;
        data[3] = (byte)getID();
        data[4] = (byte)0x44;
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
            data[4] == (byte) 0x45;
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
            
    public static String getSynthName() { return "E-Mu Morpheus / Ultra Proteus [Hyper]"; }
    public String getPatchName(Model model) { return model.get("name", "Untitled"); }
    
    public Model getNextPatchLocation(Model model)
        {
        int bank = model.get("bank");
        int number = model.get("number");
        
        number++;
        if (number >= 128)
            {
            bank++;
            number = 0;
            if (bank >= 3)
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
    "portmode",
    "fgseg1typelevel",
    "fgseg1time",
    "fgseg1shape",
    "fgseg1condjump",
    "fgseg1condval",
    "fgseg1destseg",
    "fgseg2typelevel",
    "fgseg2time",
    "fgseg2shape",
    "fgseg2condjump",
    "fgseg2condval",
    "fgseg2destseg",
    "fgseg3typelevel",
    "fgseg3time",
    "fgseg3shape",
    "fgseg3condjump",
    "fgseg3condval",
    "fgseg3destseg",
    "fgseg4typelevel",
    "fgseg4time",
    "fgseg4shape",
    "fgseg4condjump",
    "fgseg4condval",
    "fgseg4destseg",
    "fgseg5typelevel",
    "fgseg5time",
    "fgseg5shape",
    "fgseg5condjump",
    "fgseg5condval",
    "fgseg5destseg",
    "fgseg6typelevel",
    "fgseg6time",
    "fgseg6shape",
    "fgseg6condjump",
    "fgseg6condval",
    "fgseg6destseg",
    "fgseg7typelevel",
    "fgseg7time",
    "fgseg7shape",
    "fgseg7condjump",
    "fgseg7condval",
    "fgseg7destseg",
    "fgseg8typelevel",
    "fgseg8time",
    "fgseg8shape",
    "fgseg8condjump",
    "fgseg8condval",
    "fgseg8destseg",
    "z1preset",
    "z1volume",
    "z1pan",
    "z1lowkey",
    "z1highkey",
    "z1lowvel",
    "z1highvel",
    "z1veloffset",
    "z1xpose",
    "z1coarsetune",
    "z1finetune",
    "z2preset",
    "z2volume",
    "z2pan",
    "z2lowkey",
    "z2highkey",
    "z2lowvel",
    "z2highvel",
    "z2veloffset",
    "z2xpose",
    "z2coarsetune",
    "z2finetune",
    "z3preset",
    "z3volume",
    "z3pan",
    "z3lowkey",
    "z3highkey",
    "z3lowvel",
    "z3highvel",
    "z3veloffset",
    "z3xpose",
    "z3coarsetune",
    "z3finetune",
    "z4preset",
    "z4volume",
    "z4pan",
    "z4lowkey",
    "z4highkey",
    "z4lowvel",
    "z4highvel",
    "z4veloffset",
    "z4xpose",
    "z4coarsetune",
    "z4finetune",
    "z5preset",
    "z5volume",
    "z5pan",
    "z5lowkey",
    "z5highkey",
    "z5lowvel",
    "z5highvel",
    "z5veloffset",
    "z5xpose",
    "z5coarsetune",
    "z5finetune",
    "z6preset",
    "z6volume",
    "z6pan",
    "z6lowkey",
    "z6highkey",
    "z6lowvel",
    "z6highvel",
    "z6veloffset",
    "z6xpose",
    "z6coarsetune",
    "z6finetune",
    "z7preset",
    "z7volume",
    "z7pan",
    "z7lowkey",
    "z7highkey",
    "z7lowvel",
    "z7highvel",
    "z7veloffset",
    "z7xpose",
    "z7coarsetune",
    "z7finetune",
    "z8preset",
    "z8volume",
    "z8pan",
    "z8lowkey",
    "z8highkey",
    "z8lowvel",
    "z8highvel",
    "z8veloffset",
    "z8xpose",
    "z8coarsetune",
    "z8finetune",
    "z9preset",
    "z9volume",
    "z9pan",
    "z9lowkey",
    "z9highkey",
    "z9lowvel",
    "z9highvel",
    "z9veloffset",
    "z9xpose",
    "z9coarsetune",
    "z9finetune",
    "z10preset",
    "z10volume",
    "z10pan",
    "z10lowkey",
    "z10highkey",
    "z10lowvel",
    "z10highvel",
    "z10veloffset",
    "z10xpose",
    "z10coarsetune",
    "z10finetune",
    "z11preset",
    "z11volume",
    "z11pan",
    "z11lowkey",
    "z11highkey",
    "z11lowvel",
    "z11highvel",
    "z11veloffset",
    "z11xpose",
    "z11coarsetune",
    "z11finetune",
    "z12preset",
    "z12volume",
    "z12pan",
    "z12lowkey",
    "z12highkey",
    "z12lowvel",
    "z12highvel",
    "z12veloffset",
    "z12xpose",
    "z12coarsetune",
    "z12finetune",
    "z13preset",
    "z13volume",
    "z13pan",
    "z13lowkey",
    "z13highkey",
    "z13lowvel",
    "z13highvel",
    "z13veloffset",
    "z13xpose",
    "z13coarsetune",
    "z13finetune",
    "z14preset",
    "z14volume",
    "z14pan",
    "z14lowkey",
    "z14highkey",
    "z14lowvel",
    "z14highvel",
    "z14veloffset",
    "z14xpose",
    "z14coarsetune",
    "z14finetune",
    "z15preset",
    "z15volume",
    "z15pan",
    "z15lowkey",
    "z15highkey",
    "z15lowvel",
    "z15highvel",
    "z15veloffset",
    "z15xpose",
    "z15coarsetune",
    "z15finetune",
    "z16preset",
    "z16volume",
    "z16pan",
    "z16lowkey",
    "z16highkey",
    "z16lowvel",
    "z16highvel",
    "z16veloffset",
    "z16xpose",
    "z16coarsetune",
    "z16finetune",
    };
    

    public static final String[] BANKS = new String[] { "RAM", "ROM", "Card" };
    public static final String[] NOTES = new String[] { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
        

    public static final String[] FUNCTION_GENERATORS = new String[]
    {
    "Linear",
    "Expo+1",
    "Expo+2",
    "Expo+3",
    "Expo+4",
    "Expo+5",
    "Expo+6",
    "Expo+7",
    "Circ1.4",
    "Circ1.6",
    "Circ1.8",
    "Cir1.16",
    "Squeeze",
    "FastLn1",
    "FastLn2",
    "FastLn3",
    "MedLn1",
    "MedLn2",
    "SlwRmp1",
    "SlwRmp2",
    "Bloom",
    "Bloom2",
    "Cr1.16R",
    "Cir1.8R24",
    "Cir1.6R",
    "Cir1.4R",
    "SlwCrv1",
    "SlwCrv2",
    "DelayDC",
    "DCdelay",
    "Curve2X",
    "Curv2XB",
    "Curv2XC",
    "ZizZag1",
    "ZizZag2",
    "ZizZag3",
    "Chaos03",
    "Chaos06",
    "Chaos12",
    "Chaos16",
    "Chaos25",
    "Chaos33",
    "Chaos37",
    "Chaos50",
    "Chaos66",
    "Chaos75",
    "Chaos95",
    "Chaos99",
    "LinShfl",
    "LnShfl2",
    "RandomA",
    "RandomB",
    "RandomC",
    "RandomD",
    "RandomE",
    "RandomF",
    "RandomG",
    "RandomH",
    "RandomI",
    "RandomJ",
    "RandomK",
    "RandomL",
    "RandomZ"
    };

    public static final String[] FUNCTION_GENERATOR_CONDITIONS = new String[]
    {
    "Never",
    "AlwaysEnd",
    "NoteOnEnd",
    "NoteOnImm",
    "NoteOffEnd",
    "NoteOffImm",
    "Foot1End",
    "Foot1Imm",
    "Foot2End",
    "Foot2Imm",
    "Foot3End",
    "Foot3Imm",
    };
        
    public static final String[] FUNCTION_GENERATOR__TYPES = new String[] { "Absolute", "Delta", "Rnd Absolute", "Rnd Delta" };
        
    }
