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

public class KawaiK4Effect extends Synth
    {
    public static final String[] BANKS = { "Internal", "External" };

    public static final String[][] EFFECT_PARAMETERS = new String[][] 
    {
    { "Pre-Delay", "Time", "Tone" },
    { "Pre-Delay", "Time", "Tone" },
    { "Pre-Delay", "Time", "Tone" },
    { "Pre-Delay", "Time", "Tone" },
    { "Pre-Delay", "Time", "Tone" },
    { "Pre-Delay", "Time", "Tone" },
    { "Feedback", "Tone", "Delay" },
    { "Feedback", "L/R Delay", "Delay" },
    { "Width", "Feedback", "Rate" },
    { "Drive", "Flanger Type", "Balance" },
    { "Drive", "Delay Time", "Balance" },
    { "Drive", "Reverb Type", "Balance" },
    { "Delay 1", "Delay 2", "Balance" },
    { "Delay 1", "Delay 2", "Balance" },
    { "Chorus", "Delay", "Balance" },
    { "Chorus", "Delay", "Balance" } 
    };
                
    public static final String[] EFFECT_TYPES = { "Reverb 1", "Reverb 2", "Reverb 3", "Reverb 4", "Gate Reverb", "Reverse Gate", "Normal Delay", "Stereo Panpot Delay", "Chorus", "Overdrive + Flanger", "Overdrive + Normal Delay", "Overdrive + Reverb", "Normal Delay + Normal Delay", "Normal Delay + Stereo Panpot Delay", "Chorus + Normal Delay", "Chorus + Stereo Panpot Delay" };
    
    public static final String[] SUBMIXES = { "A", "B", "C", "D", "E", "F", "G", "H" };
    
    public KawaiK4Effect()
        {
        for(int i = 0; i < allParameters.length; i++)
            {
            allParametersToIndex.put(allParameters[i], Integer.valueOf(i));
            }

        JComponent soundPanel = new SynthPanel(this);
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        hbox.addLast(addGlobal(Style.COLOR_A()));
        vbox.add(hbox);
        
        for(int i = 0; i < 8; i+= 4)
            {
            hbox = new HBox();
            hbox.add(addSubmix(i + 1, Style.COLOR_B()));
            hbox.add(addSubmix(i + 2, Style.COLOR_B()));
            hbox.add(addSubmix(i + 3, Style.COLOR_B()));
            hbox.addLast(addSubmix(i + 4, Style.COLOR_B()));
            vbox.add(hbox);
            }

        soundPanel.add(vbox);

        addTab("Effect", soundPanel);
        
        model.set("number", 0);
        model.set("bank", 0);           // internal
        
        loadDefaults();
        }
        


    public int getPauseAfterChangePatch() { return 200; }   // Seem to need about > 100ms

    public int getPauseAfterSendAllParameters() { return 100; } 
 
    /** Add the global patch category (name, id, number, etc.) */
    public JComponent addNameGlobal(Color color)
        {
        Category globalCategory = new Category(this, getSynthName(), color);
        globalCategory.makeUnresettable();
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
                
        VBox vbox = new VBox();
        comp = new PatchDisplay(this, 8);
        vbox.add(comp);
        hbox.add(vbox);

        // Not enough space to show the title
        hbox.addLast(Strut.makeHorizontalStrut(90));

        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }


    public LabelledDial[] param = new LabelledDial[3];

    void updateEffects(int effectType)
        {
        for(int i = 0; i < param.length; i++)
            {
            param[i].setLabel(EFFECT_PARAMETERS[effectType][i]);
            param[i].repaint();
            }
        }       

    public JComponent addGlobal(Color color)
        {
        Category category = new Category(this, "Global", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        param[0] = new LabelledDial("Parameter 1", this, "param1", color, 0, 7);
        param[0].addAdditionalLabel("[K4]");
        param[1] = new LabelledDial("Parameter 2", this, "param2", color, 0, 7);  // this is always 0...7, even for parameter #12
        param[1].addAdditionalLabel("        [K4]        ");
        param[2] = new LabelledDial("Parameter 3", this, "param3", color, 0, 31);
        param[2].addAdditionalLabel("[K4]");

        params = EFFECT_TYPES;
        VBox vbox = new VBox();
        comp = new Chooser("Type [K4]", this, "type", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                int effectType = model.get("type");
                updateEffects(effectType);
                }
            };
        vbox.add(comp);
        hbox.add(vbox);

        hbox.add(param[0]);
        hbox.add(param[1]);
        hbox.add(param[2]);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addSubmix(int sub, Color color)
        {
        Category category = new Category(this, "Submix " + SUBMIXES[sub - 1], color);
        category.makePasteable("submix" + sub);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        comp = new LabelledDial("Pan", this, "submix" + sub + "pan", color, 0, 21)
            {
            public String map(int val)
                {
                if (val < 15)
                    return "" + (val - 7);
                else if (val == 15)  // unknown at this time
                    return "??";
                else return "I" + (val - 15);
                }
            public int getDefaultValue() { return 7; }
            public double getStartAngle() { return 180; }
            };
        getModel().setMetricMax("submix" + sub + "pan", 14);  // maybe this isn't even right.  Is it 15?  And how do we restrict the I1...I6???
        ((LabelledDial)comp).addAdditionalLabel("[K4r: I1-I6]");
        hbox.add(comp);

        comp = new LabelledDial("Send 1", this, "submix" + sub + "send1", color, 0, 100);
        ((LabelledDial)comp).addAdditionalLabel("[K4]");
        hbox.add(comp);

        comp = new LabelledDial("Send 2", this, "submix" + sub + "send2", color, 0, 100);
        ((LabelledDial)comp).addAdditionalLabel("[K4]");
        hbox.add(comp);
        
        hbox.add(Strut.makeHorizontalStrut(30));

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
                
                
    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        JComboBox bank = new JComboBox(BANKS);
        bank.setSelectedIndex(model.get("bank"));
        
        JTextField number = new JTextField("" + (model.get("number") + 1), 3);

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
                showSimpleError(title, "The Patch Number must be an integer 1...32");
                continue;
                }
            if (n < 1 || n > 32)
                {
                showSimpleError(title, "The Patch Number must be an integer 1...32");
                continue;
                }
                
            n--;
                                
            int i = bank.getSelectedIndex();
                        
                        
            change.set("bank", i);
            change.set("number", n);
                        
            return true;
            }
        }

    public int parse(byte[] data, boolean fromFile)
        {
        model.set("bank", data[6] == 0x01 ? 0 : 1);
        model.set("number", data[7]);
                        
        for(int i = 0; i < 34; i++)
            {
            String key = allParameters[i];
            if (key.equals("-"))
                continue;
            else
                model.set(key, data[i + 8]);
            }

        revise();
        return PARSE_SUCCEEDED;
        }
    
    public static final int EXPECTED_SYSEX_LENGTH = 34 + 10;
    
    public static boolean recognize(byte[] data)
        {
        return (data.length == EXPECTED_SYSEX_LENGTH &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x40 &&
            data[3] == (byte)0x20 &&
            data[4] == (byte)0x00 &&
            data[5] == (byte)0x04 &&
            (data[6] == (byte)0x01 || data[6] == (byte)0x03) &&
            data[7] < (byte)32);
        }

    public static String getSynthName() { return "Kawai K4/K4r [Effect]"; }
    
    public String getDefaultResourceFileName() { return "KawaiK4Effect.init"; }
        
    public String getHTMLResourceFileName() 
        { 
        return "KawaiK4Effect.html";
        }

    
    
    
    
    
    
    

    // not even sure if I need this

    public String getPatchName(Model model) 
        {
        return "Effect " + (model.get("number") + 1);
        }






    /** Map of parameter -> index in the allParameters array. */
    HashMap allParametersToIndex = new HashMap();

    /** List of all parameters in order.  "-" is a reserved (unused and thus unnamed) parameter. */

    final static String[] allParameters = new String[/*100 or so*/] 
    {
    "type",                   
    "param1",
    "param2",
    "param3",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "submix1pan",
    "submix1send1",
    "submix1send2",
    "submix2pan",
    "submix2send1",
    "submix2send2",
    "submix3pan",
    "submix3send1",
    "submix3send2",
    "submix4pan",
    "submix4send1",
    "submix4send2",
    "submix5pan",
    "submix5send1",
    "submix5send2",
    "submix6pan",
    "submix6send1",
    "submix6send2",
    "submix7pan",
    "submix7send1",
    "submix7send2",
    "submix8pan",
    "submix8send1",
    "submix8send2",
    };



    /** Generate a K4 checksum of the data bytes */
    byte produceChecksum(byte[] bytes)
        {
        //      The K4 manual says the checksum is the
        //              "Sum of the A5H and s0~s129".
        //              I believe this is A5 + sum(s0...s129) ignoring overflow, cut to 7 bits

        int checksum = 0xA5;
        for(int i = 0; i < bytes.length; i++)
            checksum = (checksum + bytes[i]) & 255;
        return (byte)(checksum & 127);
        }



    public byte[] emit(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        if (tempModel == null)
            tempModel = getModel();

        byte[] data = new byte[34];
    
        for(int i = 0; i < 34; i++)
            {
            String key = allParameters[i];
            if (key.equals("-"))
                data[i] = (byte)0x0;
            else
                data[i] = (byte)(model.get(key));
            }

        // Error in Section 4-1, see "Corrected MIDI Implementation"

        boolean external;
        byte position;
                
        external = (tempModel.get("bank") > 4);
        position = (byte)(tempModel.get("number"));
                        
        byte[] result = new byte[EXPECTED_SYSEX_LENGTH];
        result[0] = (byte)0xF0;
        result[1] = (byte)0x40;
        result[2] = (byte)getChannelOut();
        if (toWorkingMemory)
            result[3] = (byte)0x23;
        else
            result[3] = (byte)0x20;
        result[4] = (byte)0x00;
        result[5] = (byte)0x04;
        if (toWorkingMemory)
            result[6] = 0x01;
        else
            result[6] = (byte)(external ? 0x03 : 0x01);
        if (toWorkingMemory)
            result[7] = (byte)(0x00);       // indicates effect
        else
            result[7] = (byte)position;
        System.arraycopy(data, 0, result, 8, data.length);
        result[8 + data.length] = (byte)produceChecksum(data);
        result[9 + data.length] = (byte)0xF7;
        return result;
        }
    
    
    
    
    public byte[] emit(String key) 
        { 
        if (key.equals("bank")) return new byte[0];  // this is not emittable
        if (key.equals("number")) return new byte[0];  // this is not emittable
        if (key.equals("-")) return new byte[0];  // hmmm
                
        int source = 0;
        byte msb = (byte)(model.get(key) >>> 7);
        byte lsb = (byte)(model.get(key) & 127);


        int index = ((Integer)(allParametersToIndex.get(key))).intValue();
        int submix = 0;
                
        if (index >= 10)
            {
            submix = (index - 10) / 3;
            index = (index - 10) % 3 + 86;
            }
        else
            {
            index = index + 82;
            }
                        
        return new byte[] { (byte)0xF0, 0x40, (byte)getChannelOut(), 0x10, 0x00, 0x04, (byte)index, (byte)((submix << 1) | msb), (byte)lsb, (byte)0xF7 };
        }






    public byte[] requestDump(Model tempModel) 
        { 
        byte position = (byte)(tempModel.get("number"));
        boolean external = (tempModel.get("bank") == 1);
        return new byte[] { (byte)0xF0, 0x40, (byte)getChannelOut(), 0x00, 0x00, 0x04, 
            (byte)(external ? 0x03 : 0x01),
            position, (byte)0xF7};
        }
    
    
    


    ////// YOU MAY WANT TO IMPLEMENT SOME OF THE FOLLOWING


    public void parseParameter(byte[] data)
        {
        if (data.length == 7 &&                 // write error report
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x40 &&
            data[3] >= (byte)0x41 &&
            data[3] <= (byte)0x43 &&
            data[4] == (byte)0x00 &&
            data[5] == (byte)0x04)
            {
            String error = "Write Failed (Maybe Transmission Failure)";
            // dump failed
            if (data[3] == 0x42)
                error = "Patch is Write-Protected";
            else if (data[3] == 0x43)
                error = "External Data Card is Not Inserted";
                        
            showSimpleError("Write Failed", error);
            }
        }
    
    
    public JFrame sprout()
        {
        JFrame frame = super.sprout();
        // We can't request the current working memory (don't ask why)
        receiveCurrent.setEnabled(false);
        return frame;
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
        
        return BANKS[model.get("bank")] + (model.get("number") + 1 < 10 ? "0" : "") + ((model.get("number") + 1));
        }
    }
