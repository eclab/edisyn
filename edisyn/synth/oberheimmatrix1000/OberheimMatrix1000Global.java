/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.oberheimmatrix1000;

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
   A patch editor for the Oberheim Matrix 1000 global parameters
   
   @author Sean Luke
*/

public class OberheimMatrix1000Global extends Synth
    {
    /// Various collections of parameter names for pop-up menus
    
    public static final String[] MOD_SOURCES = new String[] { "Off", "Lever 2", "Pedal 1" };
    public static final String[] WAVEFORMS = new String[] { "Triangle", "Up Saw", "Down Saw", "Square", "Random" }; 
    public static final String[] SCALE_TYPES = new String[] { "Linear", "Exponential 1", "Exponential 2" }; 
    
    public OberheimMatrix1000Global()
        {
        for(int i = 0; i < allParameters.length; i++)
            {
            allParametersToIndex.put(allParameters[i], Integer.valueOf(i));
            }

        /// SOUND PANEL
                
        SynthPanel soundPanel = new SynthPanel(this);
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        hbox.addLast(addGeneral(Style.COLOR_A()));
        vbox.add(hbox);
        
        vbox.add(addMIDI(Style.COLOR_B()));
        vbox.add(addVibrato(Style.COLOR_A()));
        vbox.add(addGroups(Style.COLOR_B()));
        
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("General", soundPanel);
                
                
        SynthPanel groupPanel = new SynthPanel(this);
        vbox = new VBox();

        vbox.add(addGroupsEnable(0, Style.COLOR_B()));
        
        groupPanel.add(vbox, BorderLayout.CENTER);
        addTab("Groups 0-499", groupPanel);

        groupPanel = new SynthPanel(this);
        vbox = new VBox();

        vbox.add(addGroupsEnable(500, Style.COLOR_B()));
        
        groupPanel.add(vbox, BorderLayout.CENTER);
        addTab("Groups 500-999", groupPanel);

        loadDefaults();        
        }
                
    public String getDefaultResourceFileName() { return "OberheimMatrix1000Global.init"; }
    public String getHTMLResourceFileName() { return "OberheimMatrix1000Global.html"; }

    public JFrame sprout()
        {
        JFrame frame = super.sprout();
        receivePatch.setEnabled(false);
        receiveNextPatch.setEnabled(false);
        transmitTo.setEnabled(false);
        writeTo.setEnabled(false);
        merge.setEnabled(false);
        return frame;
        }

    /** Add the global patch category (name, id, number, etc.) */
    public JComponent addNameGlobal(Color color)
        {
        Category globalCategory = new Category(this, "Oberheim Matrix 1000 [Global]", color);
        //globalCategory.makeUnresettable();
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
                                
        hbox.add(Strut.makeHorizontalStrut(270));
        
        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }

    public JComponent addVibrato(Color color)
        {
        Category category = new Category(this, "Vibrato", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = WAVEFORMS;
        comp = new Chooser("Waveform", this, "vibratowaveform", params);
        vbox.add(comp);
        
        hbox.add(vbox);
        vbox = new VBox();

        params = MOD_SOURCES;
        comp = new Chooser("Speed Mod Source", this, "vibratospeedmodsourcecode", params);
        vbox.add(comp);

        params = MOD_SOURCES;
        comp = new Chooser("Amplitude Mod Source", this, "vibratoampmodsourcecode", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Speed", this, "vibratospeed", color, 0, 63);
        hbox.add(comp);

        comp = new LabelledDial("Speed Mod", this, "vibratospeedmodulationamount", color, 0, 63);
        hbox.add(comp);

        comp = new LabelledDial("Amplitude", this, "vibratoamplitude", color, 0, 63);
        hbox.add(comp);

        comp = new LabelledDial("Amp Mod", this, "vibratoampmodulationamount", color, 0, 63);
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
                

    public JComponent addMIDI(Color color)
        {
        Category category = new Category(this, "MIDI", color);
                
        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        
        comp = new CheckBox("Omni Mode", this, "midiomnimodeenable");
        vbox.add(comp);
       
        comp = new CheckBox("Controllers", this, "midicontrollersenable");
        vbox.add(comp);
        
        comp = new CheckBox("Patch Changes", this, "midipatchchangesenable");
        vbox.add(comp);
        
        hbox.add(vbox);
        vbox = new VBox();

        comp = new CheckBox("Echo", this, "midiechoenable");
        vbox.add(comp);

        comp = new CheckBox("Mono", this, "midimonomodeenable");
        vbox.add(comp);
        hbox.add(vbox);
        
        comp = new LabelledDial("Basic", this, "midibasicchannel", color, 0, 15, -1);
        ((LabelledDial)comp).addAdditionalLabel("Channel");
        hbox.add(comp);
                
        comp = new LabelledDial("Pedal 1", this, "midipedal1controller", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("MIDI CC");
        hbox.add(comp);

        comp = new LabelledDial("Pedal 2", this, "midipedal2controller", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("MIDI CC");
        hbox.add(comp);
        
        comp = new LabelledDial("Lever 2", this, "midilever2controller", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("MIDI CC");
        hbox.add(comp);
        
        comp = new LabelledDial("Lever 3", this, "midilever3controller", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("MIDI CC");
        hbox.add(comp);
                
        category.add(hbox, BorderLayout.WEST);
        return category;
        }


    public JComponent addGeneral(Color color)
        {
        Category category = new Category(this, "General", color);
                
        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();
        VBox vbox = new VBox();
       
        comp = new CheckBox("Bank Lock", this, "banklockenable");
        vbox.add(comp);
        
        comp = new CheckBox("Unison", this, "unisonenable");
        vbox.add(comp);

        hbox.add(vbox);
        vbox = new VBox();

        comp = new CheckBox("Volume Invert", this, "volumeinvertenable");
        vbox.add(comp);

        comp = new CheckBox("Memory Protect", this, "memoryprotectenable");
        vbox.add(comp);
        hbox.add(vbox);
        
        comp = new LabelledDial("Transpose", this, "mastertranspose", color, -31, 31);          // not -32...
        hbox.add(comp);
                
        comp = new LabelledDial("Tune", this, "mastertune", color, -31, 31);            // not -32...
        hbox.add(comp);

        comp = new LabelledDial("Bend Range", this, "bendrange", color, 0, 24); // the docs say 1 bit, which is obviously wrong
        hbox.add(comp);
        category.add(hbox, BorderLayout.WEST);
        return category;
        }


    /*
    // From https://github.com/AlpesMachines/Matrix-Ctrlr-1.00/blob/AlpesMachines-1.10a/chaosmatrix.ino
    const unsigned char DefaultGlobalParameters[172] PROGMEM = // PROGMEM
    {
    0, // not used [byte 0]
    48, // vibrato speed : 0 - 63
    0, // vibrato speed mod source code :  0ff, Lev2, Ped1
    0, // vibrato speed modulation amount : 0-63
    0, // vibrato waveform : tri, upsaw, dnsaw, squ, randm, noise
    0, // vibrato amplitude : 0 - 63
    1, // vibrato amp mode source code :  0ff, Lev2, Ped1
    63, // vibrato amp modulation amount : 0-63
    0, // Master tune (signed6) : -32 +32
    0, // not used
    0, // not used
    0, // MIDI basic channel : 0 - 15
    1, // MIDI OMNI mode : enabled = 1
    1, // MIDI controllers enabled : 0/1
    0, // MIDI patch change enabled : 0/1
    0, // not used
    0, // not used
    4, // MIDI Pedal1 controller (4 Foot) : 0-127
    64, // MIDI Pedal2 controller (64 sustain) : 0-127
    1, // MIDI Lever2 controller (1 Modwheel) : 0-127
    2, // MIDI Lever3 controller (2 Breath) : 0-127
    0, // not used [bytes 21 à 31]
    0, // not used
    0, // not used
    0, // not used
    0, // not used
    0, // not used
    0, // not used
    0, // not used
    0, // not used
    0, // not used
    0, // not used (byte31)
    0, // MIDI Echo Enabled : 0/1 (false/true)
    0, // not used (byte 33)
    0, // Master Transpose (signed7) : -63 +63 [byte 34]
    0, // MIDI Mono Mode enable : 0/1
    255, // Group enables [byte 36]
    255, 255, 255, 255, 255, 255, 255, 255,
    255, 255, 255, 255, 255, 255, 255, 255,
    255, 255, 255, 255, 255, 255, 255, 255,
    255, 255, 255, 255, 255, 255, 255, 255,
    255, 255, 255, 255, 255, 255, 255, 255,
    255, 255, 255, 255, 255, 255, 255, 255,
    255, 255, 255, 255, 255, 255, 255, 255,
    255, 255, 255, 255, 255, 255, 255, 255,
    255, 255, 255, 255, 255, 255, 255, 255,
    255, 255, 255, 255, 255, 255, 255, 255,
    255, 255, 255, 255, 255, 255, 255, 255,
    255, 255, 255, 255, 255, 255, 255, 255,
    255, 255, 255, 255, 255, 255, 255, 255,
    255, 255, 255, 255, 255, 255, 255, 255,
    255, 255, 255, 255, 255, 255, 255, 255,
    255, 255, 255, 255,
    255, // Group Enables end [byte 161]
    0, // not used [byte 162]
    0, // not used [byte 163]
    12, // Bend range in semi tones (1bit ?? user manual is false) : 0 - 24
    0, // Bank lock enable (in MSB Only)
    1, // Number of units (group mode) (1bit)
    0, // Current unit number (group mode) (1bit)
    0, // Group mode enable (in MSB only) (1bit)
    0, // Unison enable (1bit)
    0, // Volume invert enable (1bit)
    0, // Memory protect enable (1bit)
    };
    */


        
    public JComponent addGroups(Color color)
        {
        Category category = new Category(this, "Group Mode", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox2 = new VBox();
        comp = new CheckBox("Enable", this, "groupmodeenable");
        vbox2.add(comp);
        hbox.add(vbox2);
        
        comp = new LabelledDial("Units", this, "numberofunits", color, 1, 6);
        hbox.add(comp);
                
        comp = new LabelledDial(" My Number ", this, "currentunitnumber", color, 0, 15);
        hbox.add(comp);
                        
        category.add(hbox, BorderLayout.WEST);
        return category;
        }

    public JComponent addGroupsEnable(int start, Color color)
        {
        Category category = new Category(this, "Group Mode Patches " + (start == 0 ? "0-499" : "500-999"), color);
        category.makeDistributable("group");
                                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        for(int i = start; i < start + 500; i += 20)
            {
            HBox hbox2 = new HBox();
            for(int j = i; j < i + 20; j++)
                {
                comp = new CheckBox(j < 10 ? "00" + j : (j < 100 ? "0" + j : "" + j),  this, "group" + j);
                ((CheckBox)comp).addToWidth(1);
                hbox2.add(comp);
                }
            vbox.add(hbox2);
            }
                        
        category.add(vbox, BorderLayout.WEST);
        return category;
        }




    /** Map of parameter -> index in the allParameters array. */
    HashMap allParametersToIndex = new HashMap();
                
    final static String[] allParameters = new String[/*100 or so*/] 
    {
    "-",
    "vibratospeed",                   
    "vibratospeedmodsourcecode",
    "vibratospeedmodulationamount",
    "vibratowaveform",
    "vibratoamplitude",
    "vibratoampmodsourcecode",
    "vibratoampmodulationamount",
    "mastertune",                                                       //   ** signed **
    "-",
    "-",
    "midibasicchannel",
    "midiomnimodeenable",
    "midicontrollersenable",
    "midipatchchangesenable",
    "-",                                                
    "-",
    "midipedal1controller",
    "midipedal2controller",
    "midilever2controller",
    "midilever3controller",
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
    "midiechoenable",
    "-",                                                        // byte 33.  Not directly stated in docs
    "mastertranspose",                                          //      ** signed **
    "midimonomodeenable",
    
    // Skip 125 group enables, then...
    
    "-",
    "-",
    "bendrange",
    "banklockenable",
    "numberofunits",
    "currentunitnumber",
    "groupmodeenable",
    "unisonenable",
    "volumeinvertenable",
    "memoryprotectenable",
    };


    /// ERRORS IN MIDI SYSEX DESCRIPTION
    ///
    /// Though they're listed as "six bit (signed)" or "seven bit (signed)", all signed values
    /// are actually stored as signed 8-bit.  Six-bit signed values are just plain signed bytes
    /// which range from -32 to +31.  Similarly, 7-bit signed values are just plain signed bytes
    /// which range from -64 to +63.  When emitting or parsing a patch, the nybblization just breaks
    /// the byte into two nybbles and that's all.
    
    public int parse(byte[] data, boolean fromFile)
        {
        int pos = 5;
        
        for(int i = 0; i < 36; i++)
            {
            String key = allParameters[i];
            if (key.equals("-")) { pos += 2; continue; }
            
            // unpack from nybbles
            byte lonybble = data[pos++];
            byte hinybble = data[pos++];
            byte value = (byte)(((hinybble << 4) | (lonybble & 15)));
                        
            model.set(key, value);          // this *should* work for the signed ones too
            }
                
        int group = 0;
        // handle group enables
        for(int i = 0; i < 125; i++)
            {
            // unpack from nybbles
            byte lonybble = data[pos++];
            byte hinybble = data[pos++];
            byte value = (byte)(((hinybble << 4) | (lonybble & 15)));
                        
            model.set("group" + group++, (value >>> 0) & 1);
            model.set("group" + group++, (value >>> 1) & 1);
            model.set("group" + group++, (value >>> 2) & 1);
            model.set("group" + group++, (value >>> 3) & 1);
            model.set("group" + group++, (value >>> 4) & 1);
            model.set("group" + group++, (value >>> 5) & 1);
            model.set("group" + group++, (value >>> 6) & 1);
            model.set("group" + group++, (value >>> 7) & 1);
            }
                        
        // we have one extra byte from the group enables.  Not sure why.  We will be ignoring it.
        pos+=2;
                
        for(int i = 36; i < 46; i++)
            {
            String key = allParameters[i];
            if (key.equals("-")) { pos += 2; continue; }
            
            // unpack from nybbles
            byte lonybble = data[pos++];
            byte hinybble = data[pos++];
            byte value = (byte)(((hinybble << 4) | (lonybble & 15)));
            
            if (key.equals("banklockenable"))
                model.set(key, hinybble > 0 ? 1 : 0);   // MSB only.  The M1000 sets 0x08 vs 0x00
            else if (key.equals("groupmodeenable"))
                model.set(key, hinybble);               // MSB only
            else
                model.set(key, value);          // this *should* work for the signed ones too
            }
                
        revise();
        return PARSE_SUCCEEDED;
        }
    

    public int getPauseAfterWritePatch() { return 300; }        // Less than 200 and I'll get failures to PC the second time: at 250 I got a failure to write the patch.  250 might be enough but let's go for 300, yeah, it's a lot
    
    public byte[] emit(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        if (tempModel == null)
            tempModel = getModel();

        byte[] data = new byte[351];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x10;
        data[2] = (byte)0x06;
        data[3] = (byte)0x03;
        data[4] = (byte)0x03;
        data[data.length - 1] = (byte)0xF7;

        byte[] d = new byte[172];
                
        int pos = 0;
        for(int i = 0; i < 36; i++)
            {
            String key = allParameters[i];
            if (key.equals("-")) { pos++; continue; }
            
            d[pos++] = (byte)model.get(key, 0);                 // should work for signed too
            }
                
        int group = 0;
        // handle group enables
        for(int i = 0; i < 125; i++)
            {
            d[pos++] = (byte)(      (model.get("group" + group++, 0) << 0) |
                (model.get("group" + group++, 0) << 1) |
                (model.get("group" + group++, 0) << 2) |
                (model.get("group" + group++, 0) << 3) |
                (model.get("group" + group++, 0) << 4) |
                (model.get("group" + group++, 0) << 5) |
                (model.get("group" + group++, 0) << 6) |
                (model.get("group" + group++, 0) << 7));
            }

        // we have one extra byte from the group enables.  Not sure why.  CTRLR suggests
        // that it should be set to 255, so that's what I'm doing.
                
        d[pos++] = (byte)255;
                
        for(int i = 36; i < 46; i++)
            {
            String key = allParameters[i];
            if (key.equals("-")) { pos++; continue; }
            if (key.equals("banklockenable") || key.equals("groupmodeenable"))
                d[pos++] = (byte)(model.get(key, 0) << 4);  // Shift to MSB only
            else       
                d[pos++] = (byte)model.get(key, 0);                     // should work for signed too
            }
                
        int check = 0;
                
        // pack to nybbles
        for(int i = 0; i < 172; i++)
            {
            int value = (d[i] & 255);  // make positive
            data[i * 2 + 5] = (byte)(value & 15);
            data[i * 2 + 5 + 1] = (byte)((value >>> 4) & 15);
            
            // From here:  http://www.youngmonkey.ca/nose/audio_tech/synth/Oberheim-Matrix6R.html
            // it says this about the checksum:
            //
            // Checksum.
            // The original (not transmitted) data is summed in seven bits ignoring overflows
            //
            // I think this means to add into a byte, and then mask to 127.
            
            check += value;
            }
    
        data[data.length - 2] = (byte)(check & 127);
        return data;
        }
        

    public byte[] requestCurrentDump()
        {               
        byte[] data = new byte[7];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x10;
        data[2] = (byte)0x06;
        data[3] = (byte)0x04;
        data[4] = (byte)0x03;           // request master parameters
        data[5] = (byte)0x00;
        data[6] = (byte)0xF7;
        return data;
        }
                        
    public static String getSynthName() { return "Oberheim Matrix 1000 [Global]"; }
    
    }
