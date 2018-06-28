/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.korgmicrokorg;

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
   A patch editor for the Korg MicroKorg Vocoder.
        
   @author Sean Luke
*/

public class KorgMicroKorgVocoder extends KorgMicroKorg
    {
    public KorgMicroKorgVocoder()
        {
        super();
        model.set("number", 56);
        model.set("arptarget", 0); // set it to something
        }
                
    void setVoiceMode()
        {
        model.set("voicemode", 3);
        removeTab("Timbre 1");
        removeTab("Timbre 2");
        super.setVoiceMode();
        }

    public static String getSynthName() { return "Korg MicroKorg [Vocoder]"; }
    public String getDefaultResourceFileName() { return "KorgMicroKorgVocoder.init"; }
    public String getHTMLResourceFileName() { return "KorgMicroKorgVocoder.html"; }
    
    public JComponent addGeneral(boolean vocoder, Color color)
        {
        return super.addGeneral(true, color);
        }
        
    public static boolean recognize(byte[] data)
        {
        boolean v = (
            data.length == EXPECTED_SYSEX_LENGTH &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x42 &&
            data[3] == (byte)0x58 &&
            data[4] == (byte)0x40);
        if (v == false) return false;
        
        // now decode.  Are we synth or vocoder?
        data = convertTo8Bit(data, 5);
        int voicemode = (data[16] >>> 4) & 3;
        return (voicemode == 3);  // vocoder
        }

    /** Add the global patch category (name, id, number, etc.) */
    public JComponent addNameGlobal(Color color)
        {
        Category globalCategory = new Category(this, getSynthName(), color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
                
        VBox vbox = new VBox();
        HBox hbox2 = new HBox();
        comp = new PatchDisplay(this, 3);
        hbox2.add(comp);
        vbox.add(hbox2);
        
        comp = new StringComponent("Patch Name", this, "name", 12, "Name must be up to 12 ASCII characters.")
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
        
        hbox.add(Strut.makeHorizontalStrut(90));

        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }


    /////// OTHER ABSTRACT METHODS
    
    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        JComboBox bank = new JComboBox(BANKS);
        bank.setEditable(false);
        bank.setMaximumRowCount(32);
        bank.setSelectedIndex(model.get("bank"));
                
        int numberv = model.get("number");
        JTextField number = new JTextField("" + (numberv / 8 + 1) + "" + (numberv % 8 + 1), 3);
                
        while(true)
            {
            boolean result = showMultiOption(this, new String[] { "Bank", "Patch Number"}, 
                new JComponent[] { bank, number }, title, "Enter the Bank and Patch number.");
                
            if (result == false) 
                return false;
                                
            int n;
            try { n = Integer.parseInt(number.getText()); }
            catch (NumberFormatException e)
                {
                showSimpleError(title, "The Patch Number must be an integer 81 ... 88, with no zero or nine digits\n(11...78 are reserved for Synthesizer Patches)");
                continue;
                }
            if (n < 81 || n > 88 || n % 10 == 0 || n % 10 == 9)
                {
                showSimpleError(title, "The Patch Number must be an integer 81 ... 88, with no zero or nine digits\n(11...78 are reserved for Synthesizer Patches)");
                continue;
                }
                                
            change.set("bank", bank.getSelectedIndex());
            change.set("number", (n / 10 - 1) * 8 + (n % 10 - 1));  // yuk, magic equation
                        
            return true;
            }
        }
     
    public Model getNextPatchLocation(Model model)
        {
        int bank = model.get("bank");
        int number = model.get("number");
        
        number++;
        if (number >= 64)
            {
            bank++;
            number = 56;
            if (bank >= 2)
                bank = 0;
            }
                
        Model newModel = buildModel();
        newModel.set("bank", bank);
        newModel.set("number", number);
        return newModel;
        }


    }
    
