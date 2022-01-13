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
        removeTab("Timbre 1");
        removeTab("Timbre 2");
        }
                
    public static String getSynthName() { return "Korg MicroKorg [Vocoder]"; }
    public String getDefaultResourceFileName() { return "KorgMicroKorgVocoder.init"; }
    public String getHTMLResourceFileName() { return "KorgMicroKorgVocoder.html"; }
    
    public JComponent addGeneral(boolean vocoder, Color color)
        {
        return super.addGeneral(true, color);
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
        JTextField number = new SelectedTextField("" + (numberv / 8 + 1) + "" + (numberv % 8 + 1), 3);
                
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


    protected void subparse(byte[] data)
        {
        int offset = 38;
        int i = 3;
        model.set("timbre" + i + "assignmode", (data[offset + 1] >>> 6) & 3);
        model.set("timbre" + i + "ampeg2reset", (data[offset + 1] >>> 5) & 1);
        model.set("timbre" + i + "triggermode", (data[offset + 1] >>> 3) & 1);
        model.set("timbre" + i + "unisondetune", data[offset + 2]);
        model.set("timbre" + i + "tune", data[offset + 3]);
        model.set("timbre" + i + "bendrange", data[offset + 4]);
        model.set("timbre" + i + "transpose", data[offset + 5]);
        model.set("timbre" + i + "vibratoint", data[offset + 6]);
        model.set("timbre" + i + "osc1wave", data[offset + 7]);
        model.set("timbre" + i + "osc1ctrl1", data[offset + 8]);
        model.set("timbre" + i + "osc1ctrl2", data[offset + 9]);
        model.set("timbre" + i + "osc1dwgswave", data[offset + 10]);
        model.set("timbre" + i + "osc2hpfgate", (data[offset + 12]) & 1);
        model.set("timbre" + i + "portamentotime", data[offset + 14] & 127);
        model.set("timbre" + i + "osc1level", data[offset + 15]);
        model.set("timbre" + i + "osc2level", data[offset + 16]);
        model.set("timbre" + i + "noise", data[offset + 17]);
        model.set("timbre" + i + "osc2hpflevel", data[offset + 18]);
        model.set("timbre" + i + "osc2gatesense", data[offset + 19]);
        model.set("timbre" + i + "osc2threshold", data[offset + 20]);
        model.set("timbre" + i + "filtershift", data[offset + 21]);
        model.set("timbre" + i + "filtercutoff", data[offset + 22]);
        model.set("timbre" + i + "filterresonance", data[offset + 23]);
        model.set("timbre" + i + "filtermodsource", data[offset + 24]);
        model.set("timbre" + i + "filterintensity", data[offset + 25]);
        model.set("timbre" + i + "filterefsense", data[offset + 26]);
        model.set("timbre" + i + "amplevel", data[offset + 27]);
        model.set("timbre" + i + "ampdirectlevel", data[offset + 28]);
        model.set("timbre" + i + "ampdistortion", data[offset + 29] & 1);
        model.set("timbre" + i + "ampkeyboardtrack", data[offset + 31]);
        model.set("timbre" + i + "env1" + "sustain", data[offset + 34]);
        model.set("timbre" + i + "env2" + "attack", data[offset + 36]);
        model.set("timbre" + i + "env2" + "decay", data[offset + 37]);
        model.set("timbre" + i + "env2" + "sustain", data[offset + 38]);
        model.set("timbre" + i + "env2" + "release", data[offset + 39]);
        model.set("timbre" + i + "lfo1" + "keysync", (data[offset + 40] >>> 4) & 3);
        model.set("timbre" + i + "lfo1" + "wave", (data[offset + 40]) & 3);
        model.set("timbre" + i + "lfo1" + "frequency", data[offset + 41]);
        model.set("timbre" + i + "lfo1" + "temposync", (data[offset + 42] >>> 7) & 1);
        // documentation says this is bits 0...4 but this has to be wrong, since the values only go 0...14
        model.set("timbre" + i + "lfo1" + "syncnote", (data[offset + 42]) & 15);
        model.set("timbre" + i + "lfo2" + "keysync", (data[offset + 43] >>> 4) & 3);
        model.set("timbre" + i + "lfo2" + "wave", (data[offset + 43]) & 3);
        model.set("timbre" + i + "lfo2" + "frequency", data[offset + 44]);
        model.set("timbre" + i + "lfo2" + "temposync", (data[offset + 45] >>> 7) & 1);
        // documentation says this is bits 0...4 but this has to be wrong, since the values only go 0...14
        model.set("timbre" + i + "lfo2" + "syncnote", (data[offset + 45]) & 15);
        for(int j = 1; j <= 8; j++)
            {
            // the documentation is very unclear here.  It appears that it says that pairs of MIDI values
            // should be exactly the same.
            model.set("channel" + j + "level", data[offset + ((j-1) * 2) + 46]);
            if (data[offset + ((j-1) * 2) + 46] != data[offset + ((j-1) * 2) + 47])
                System.err.println("Warning (KorgMicroKorg): Channel" + j + "level inconsistent in pair: " + data[offset + ((j-1) * 2) + 47]);
            }
        for(int j = 1; j <= 8; j++)
            {
            // the documentation is very unclear here.  It appears that it says that pairs of MIDI values
            // should be exactly the same.
            model.set("channel" + j + "pan", data[offset + ((j-1) * 2) + 62]);
            if (data[offset + ((j-1) * 2) + 62] != data[offset + ((j-1) * 2) + 63])
                System.err.println("Warning (KorgMicroKorg): Channel" + j + "pan inconsistent in pair: " + data[offset + ((j-1) * 2) + 63]);
            }
        for(int j = 1; j <= 16; j++)
            {
            // the documentation is very unclear here.  It appears that it says that pairs of MIDI values
            // should be exactly the same.
            int a = data[offset + ((j-1) * 4) + 78];
            if (a < 0) a += 256;
            int b = data[offset + ((j-1) * 4) + 79];
            if (b < 0) b += 256;
            int c = data[offset + ((j-1) * 4) + 80];
            if (c < 0) c += 256;
            int d = data[offset + ((j-1) * 4) + 81];
            if (d < 0) d += 256;
            int val = (a << 24) | (b << 16) | (c << 8) | d;
            if (val >= 0x7FFFFF00)
                {
                System.err.println("Warning (KorgMicroKorg): Too large value for hold number " + j + ": " + val);
                val = 0x7FFFFF00;
                }
            if (val < 0) 
                {
                System.err.println("Warning (KorgMicroKorg): Hold number " + j + " is negative: " + val);
                val = 0;
                }
            model.set("channel" + j + "hold", val);
            }
        }
            
    protected void subemit(byte[] data)
        {
        int offset = 38;
        int i = 3;
        data[offset + 0] = (byte)-1;
        data[offset + 1] = (byte)((model.get("timbre" + i + "assignmode") << 6) |
            (model.get("timbre" + i + "ampeg2reset") << 5) | 
            (model.get("timbre" + i + "triggermode") << 3));
        data[offset + 2] = (byte)model.get("timbre" + i + "unisondetune");
        data[offset + 3] = (byte)model.get("timbre" + i + "tune");
        data[offset + 4] = (byte)model.get("timbre" + i + "bendrange");
        data[offset + 5] = (byte)model.get("timbre" + i + "transpose");
        data[offset + 6] = (byte)model.get("timbre" + i + "vibratoint");
        data[offset + 7] = (byte)model.get("timbre" + i + "osc1wave");
        data[offset + 8] = (byte)model.get("timbre" + i + "osc1ctrl1");
        data[offset + 9] = (byte)model.get("timbre" + i + "osc1ctrl2");
        data[offset + 10] = (byte)model.get("timbre" + i + "osc1dwgswave");
        data[offset + 11] = 0;
        data[offset + 12] = (byte)model.get("timbre" + i + "osc2hpfgate");
        data[offset + 13] = 0;
        data[offset + 14] = (byte)model.get("timbre" + i + "portamentotime");
        data[offset + 15] = (byte)model.get("timbre" + i + "osc1level");
        data[offset + 16] = (byte)model.get("timbre" + i + "osc2level");
        data[offset + 17] = (byte)model.get("timbre" + i + "noise");
        data[offset + 18] = (byte)model.get("timbre" + i + "osc2hpflevel");
        data[offset + 19] = (byte)model.get("timbre" + i + "osc2gatesense");
        data[offset + 20] = (byte)model.get("timbre" + i + "osc2threshold");
        data[offset + 21] = (byte)model.get("timbre" + i + "filtershift");
        data[offset + 22] = (byte)model.get("timbre" + i + "filtercutoff");
        data[offset + 23] = (byte)model.get("timbre" + i + "filterresonance");
        data[offset + 24] = (byte)model.get("timbre" + i + "filtermodsource");
        data[offset + 25] = (byte)model.get("timbre" + i + "filterintensity");
        data[offset + 26] = (byte)model.get("timbre" + i + "filterefsense");
        data[offset + 27] = (byte)model.get("timbre" + i + "amplevel");
        data[offset + 28] = (byte)model.get("timbre" + i + "ampdirectlevel");
        data[offset + 29] = (byte)model.get("timbre" + i + "ampdistortion");
        data[offset + 30] = (byte)64;
        data[offset + 31] = (byte)model.get("timbre" + i + "ampkeyboardtrack");
        data[offset + 32] = data[offset + 33] = 0;
        data[offset + 34] = (byte)model.get("timbre" + i + "env1" + "sustain");
        data[offset + 35] = 0;
        data[offset + 36] = (byte)model.get("timbre" + i + "env2" + "attack");
        data[offset + 37] = (byte)model.get("timbre" + i + "env2" + "decay");
        data[offset + 38] = (byte)model.get("timbre" + i + "env2" + "sustain");
        data[offset + 39] = (byte)model.get("timbre" + i + "env2" + "release");
        data[offset + 40] = (byte)((model.get("timbre" + i + "lfo1" + "keysync") << 4 ) | 
            model.get("timbre" + i + "lfo1" + "wave"));
        data[offset + 41] = (byte)model.get("timbre" + i + "lfo1" + "frequency");
        data[offset + 42] = (byte)((model.get("timbre" + i + "lfo1" + "temposync") << 7) |
            // documentation says this is bits 0...4 but this has to be wrong, since the values only go 0...14
            model.get("timbre" + i + "lfo1" + "syncnote"));
        data[offset + 43] = (byte)((model.get("timbre" + i + "lfo2" + "keysync") << 4 ) |
            model.get("timbre" + i + "lfo2" + "wave"));
        data[offset + 44] = (byte)model.get("timbre" + i + "lfo2" + "frequency");
        data[offset + 45] = (byte)((model.get("timbre" + i + "lfo2" + "temposync") << 7) |
            // documentation says this is bits 0...4 but this has to be wrong, since the values only go 0...14
            model.get("timbre" + i + "lfo2" + "syncnote"));
        for(int j = 1; j <= 8; j++)
            {
            // the documentation is very unclear here.  It appears that it says that pairs of MIDI values
            // should be exactly the same.
            data[offset + ((j-1) * 2) + 47] = data[offset + ((j-1) * 2) + 46] = (byte)model.get("channel" + j + "level");
            }
        for(int j = 1; j <= 8; j++)
            {
            // the documentation is very unclear here.  It appears that it says that pairs of MIDI values
            // should be exactly the same.
            data[offset + ((j-1) * 2) + 63] = data[offset + ((j-1) * 2) + 62] = (byte)model.get("channel" + j + "pan");
            }
        for(int j = 1; j <= 16; j++)
            {
            int val = model.get("channel" + j + "hold");
            data[offset + ((j-1) * 4) + 78] =  (byte)((val >>> 24) & 0xFF);
            data[offset + ((j-1) * 4) + 79] =  (byte)((val >>> 16) & 0xFF);
            data[offset + ((j-1) * 4) + 80] =  (byte)((val >>> 8) & 0xFF);
            data[offset + ((j-1) * 4) + 81] =  (byte)((val >>> 0) & 0xFF);
            }
        }

    public boolean testVerify(Synth synth2, 
        String key,
        Object obj1, Object obj2) 
        {
        // we define timbre 1 and 2 even though we don't load them
        if (key.startsWith("timbre1")) return true;
        if (key.startsWith("timbre2")) return true;
        // voicemode is unused
        if (key.equals("voicemode")) return true;
        return false;
        }

    public String[] getBankNames() { return BANKS; }

    /** Return a list of all patch number names.  Default is { "Main" } */
    public String[] getPatchNumberNames()  
        {
        return new String[] { "81", "82", "83", "84", "85", "86", "87", "88" };
        }

    /** Return a list whether patches in banks are writeable.  Default is { false } */
    public boolean[] getWriteableBanks() { return new boolean[] { true, true }; }

    /** Return a list whether individual patches can be written.  Default is FALSE. */
    public boolean getSupportsPatchWrites() { return true; }

    public int getPatchNameLength() { return 12; }
    }
    
