/***
    Copyright 2020 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.maudiovenom;

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
   A patch editor for the M-Audio Venom global parameters
   
   @author Sean Luke
*/

public class MAudioVenomGlobal extends Synth
{
    public static final byte DEFAULT_ID = (byte)0x7F;           // this seems to be what the venom returns
    
    public static final String[] VELOCITY_CURVES = new String[] { "Low", "Normal", "High", "Fixed" };
    public static final String[] RECEIVE_CLOCK_MODES = new String[] { "Off", "On", "Auto" };
    public static final String[] TRANSMIT_CLOCK_MODES = new String[] { "Off", "On / Arp", "On / Always" };
    public static final String[] ARPEGIATOR_ROUTINGS = new String[] { "Local + Keyboard", "Local + MIDI" };
    public static final String[] MIDI_OUT_MODES = new String[] { "USB", "Key" };
    public static final String[] EXTRA_CC = new String[] { "PB Sens", "F Tune", "C Tune", "AT" };

    /// Various collections of parameter names for pop-up menus
        
    static HashMap parametersToIndex = null;
        
    public MAudioVenomGlobal()
    {
        if (parametersToIndex == null)
            {
                parametersToIndex = new HashMap();

                for(int i = 0; i < parameters.length; i++)
                    {
                        parametersToIndex.put(parameters[i], Integer.valueOf(i));
                    }
            }

        /// SOUND PANEL
                
        SynthPanel soundPanel = new SynthPanel(this);
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        hbox.addLast(addKeyboard(Style.COLOR_A()));
        vbox.add(hbox);
        
        vbox.add(addMIDI(Style.COLOR_B()));
        
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Global", soundPanel);
                
        loadDefaults();        
    }
                
    public String getDefaultResourceFileName() { return "MAudioVenomGlobal.init"; }
    public String getHTMLResourceFileName() { return "MAudioVenomGlobal.html"; }

    public JFrame sprout()
    {
        JFrame frame = super.sprout();
        blend.setEnabled(false);
        receivePatch.setEnabled(false);
        transmitTo.setEnabled(false);
        writeTo.setEnabled(false);
        merge.setEnabled(false);
        return frame;
    }

    /** Add the global patch category (name, id, number, etc.) */
    public JComponent addNameGlobal(Color color)
    {
        Category globalCategory = new Category(this, "M-Audio Venom [Global]", color);
        //globalCategory.makeUnresettable();
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();

        VBox vbox = new VBox();
        
        comp = new CheckBox("USB Record", this, "usbrecord");
        vbox.add(comp);
       
        comp = new CheckBox("Mono Record", this, "monorecord");
        ((CheckBox)comp).addToWidth(1);
        vbox.add(comp);
       
        hbox.add(vbox);
                                
        hbox.add(Strut.makeHorizontalStrut(150));
        
        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
    }

    public JComponent addKeyboard(Color color)
    {
        Category category = new Category(this, "Keyboard", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();

        VBox vbox = new VBox();
        params = VELOCITY_CURVES;
        comp = new Chooser("Velocity Curve", this, "velcurve", params);
        vbox.add(comp);
        
        comp = new CheckBox("Local Mode", this, "localmode");
        vbox.add(comp);
       
        hbox.add(vbox);
        vbox = new VBox();
        
        comp = new LabelledDial("Fixed", this, "fixedvel", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Velocity");
        hbox.add(comp);

        //// The manual says this is 61...68, but that's 8 values and 
        //// the octaves are -3 ... +3
        comp = new LabelledDial("Octave", this, "octave", color, 61, 67, 64)
            {
                public boolean isSymmetric() { return true; }
            };
        hbox.add(comp);


        // Docs say transpose goes 48...72, but that's wrong.  It goes 52 ... 76 with
        // a center on 64

        comp = new LabelledDial("Transpose", this, "transpose", color, 52, 76, 64)
            {
                public boolean isSymmetric() { return true; }
            };
        hbox.add(comp);

        comp = new LabelledDial("Tune", this, "mastertune", color, 14, 114, 64)
            {
                public boolean isSymmetric() { return true; }
            };
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
    }
    
    public JComponent addMIDI(Color color)
    {
        Category category = new Category(this, "MIDI", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = RECEIVE_CLOCK_MODES;
        comp = new Chooser("MIDI Receive Clock", this, "midireceiveclockmode", params);
        vbox.add(comp);

        params = TRANSMIT_CLOCK_MODES;
        comp = new Chooser("MIDI Transmit Clock", this, "miditransmitclockmode", params);
        vbox.add(comp);

        hbox.add(vbox);
        vbox = new VBox();
        
        params = ARPEGIATOR_ROUTINGS;
        comp = new Chooser("Arpeggiator Routing", this, "arpeggiatorroute", params);
        vbox.add(comp);

        comp = new CheckBox("Single Select", this, "midisingleselect");
        vbox.add(comp);
       
        //hbox.add(vbox);
        //vbox = new VBox();
                
        params = MIDI_OUT_MODES;
        comp = new Chooser("MIDI Out", this, "midioutmode", params);
        //vbox.add(comp);
        

        comp = new CheckBox("Multi Select", this, "midimultiselect");
        vbox.add(comp);
       
        hbox.add(vbox);
        vbox = new VBox();
        
        comp = new LabelledDial("Global MIDI", this, "globalchannel", color, 0, 15, -1);
        ((LabelledDial)comp).addAdditionalLabel("Channel");
        hbox.add(comp);
        
        comp = new LabelledDial("Master", this, "mastertempo", color, 50, 300);
        ((LabelledDial)comp).addAdditionalLabel("Tempo");
        hbox.add(comp);

        /// The "CC" values 128 ... 131 are actually RPN 0, 1, 2, and aftertouch.
        /// This is from the M-Audio Oxygen manual.  Thanks to Jan Bote for his hint.
        /// Also: Sustain is listed as 0...134 in the sysex docs but elsewhere in the
        /// manual it's 0...131.  It doesn't make sense to be 0...134 given the
        /// M-Audio Oxygen manual, so I'm presuming it's just 0...131.
        
        comp = new LabelledDial("Sustain", this, "sustainpedalcc", color, 0, 131)
            {
                public String map(int val)
                {
                    if (val < 128) return "" + val;
                    else return EXTRA_CC[val - 128];
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("CC");
        hbox.add(comp);

        comp = new LabelledDial("Expression", this, "expressionpedalcc", color, 0, 131)
            {
                public String map(int val)
                {
                    if (val < 128) return "" + val;
                    else return EXTRA_CC[val - 128];
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("CC");
        hbox.add(comp);

        comp = new LabelledDial("Mod Wheel", this, "modulationwheelcc", color, 0, 131)
            {
                public String map(int val)
                {
                    if (val < 128) return "" + val;
                    else return EXTRA_CC[val - 128];
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("CC");
        hbox.add(comp);


        category.add(hbox, BorderLayout.CENTER);
        return category;
    }

                


    /** Map of parameter -> index in the parameters array. */
    HashMap parametersTopIndex = new HashMap();
                
                
    /// BUG IN DOCUMENTATION
    /// The docs say that parameter 03 is mastertempo.  But it's wrong.
    /// parameter 03 is the *msb* of mastertempo and parameter 04 is the lsb,
    /// and everything is shifted from there.

    final static String[] parameters = new String[] 
        {
            "octave",
            "transpose",                   
            "mastertune",
            "mastertempomsb",
            "mastertempolsb",
            "globalchannel",
            "midioutmode",
            "localmode",
            "velcurve",
            "fixedvel",
            "midireceiveclockmode",
            "miditransmitclockmode",
            "midisingleselect",
            "midimultiselect",
            "arpeggiatorroute",
            "sustainpedalcc",
            "expressionpedalcc",                                                
            "modulationwheelcc",
            "usbrecord",
            "monorecord",
        };

    
    public int parse(byte[] data, boolean fromFile)
    {
        // First denybblize
        byte[] d = convertTo8Bit(data, 10, data.length - 2);
        
        //        model.debug = true;
        int offset = 10;
        
        // Load parameters
        for(int i = 0; i < parameters.length; i++)
            {
                String key = parameters[i];
            
                if (key.equals("mastertempomsb"))
                    {
                        model.set("mastertempo", ((((int)(d[i] & 0xFF)) << 8) | (d[i + 1] & 0xFF)) & 0xFFFF);
                    }
                else if (key.equals("mastertempolsb"))
                    {
                        // already done
                    }
                // arpeggiatorroute is a 0 vs 127 even though the docs don't say it?
                // localmode is a 0 vs 127 even though the docs don't say it
                else if (key.equals("midisingleselect") || key.equals("midimultiselect") || key.equals("usbrecord") || key.equals("monorecord") || key.equals("arpeggiatorroute") || key.equals("localmode"))
                    {
                        model.set(key, (d[i] < 64 ? 0 : 1));
                    }
                else if (key.equals("sustainpedalcc") || key.equals("expresionpedalcc") || key.equals("modulationwheelcc"))
                    {
                        /// FIXME: Sustain appears to have a typo, on p. 102 it says 0-134 but elsewhere it says 0-131
                        int val = (d[i] & 0xFF);
                        model.set(key, val);
                    }
                else
                    {
                        model.set(key, d[i] & 0xFF);
                    }
            }

        //        model.debug = false;          
                        
        // we must now let the venom know to shut up or else it'll keep sending us junk

        if (!fromFile)
            {
                boolean sendMIDI = getSendMIDI();
                setSendMIDI(true);
                                
                // CANCEL
                // We send a CANCEL instead of an ACK because the Venom seems to respond to ACKs by often resending
                // the data.
                tryToSendSysex(new byte[] { (byte)0xF0, 0x00, 0x01, 0x05, 0x21, 
                                            (byte)DEFAULT_ID, //getID(), 
                                            0x7D,                       // ACK is 0x7F
                                            (byte)0xF7 });
                                
            
                setSendMIDI(sendMIDI);
            }

        revise();
        return PARSE_SUCCEEDED;
    }

    // manual says: 
    // The checksum is calculated as the sum of all bytes taken from the <cmd> byte 
    // and stores 0-Total with the top bit set to 0. When a SysEx is received, it 
    // totals up all values from the <cmd> byte including the checksum and the 
    // result in the bot- tom 7 bits should be 0.

    // FIXME: is this right?
    public byte checksum(byte[] data, int start, int end)
    {
        int sum = 0;
        for(int i = start; i < end; i++)
            {
                sum += data[i];
            }
        return (byte)((0-sum) & 0x7F);
    }
        

    // we're going to send parameters individually so we can control whether we send out midioutmode
    public boolean getSendsAllParametersAsDump() { return false; }

    public Object[] emitAll(String key)
    {
        int val = model.get(key);
        int param = 0;
                
        // arpeggiatorroute is a 0 vs 127 even though the docs don't say it
        // localmode is a 0 vs 127 even though the docs don't say it
        if (key.equals("midisingleselect") || key.equals("midimultiselect") || key.equals("usbrecord") || key.equals("monorecord") || key.equals("arpeggiatorroute") || key.equals("localmode") )
            {
                val = (val == 0 ? 0 : 127);
            }
        else if (key.equals("midioutmode"))
            {
                // Right now we're going to refuse to emit this one
                return new Object[] { };
            }
                
                
        if (key.equals("mastertempo"))
            {
                param = ((Integer)(parametersToIndex.get("mastertempomsb"))).intValue();                // 0x03 appears to be the right value
            }
        else
            {
                // what a mess.
                param = ((Integer)(parametersToIndex.get(key))).intValue();
                if (param >= 5)         // global channel, need to reduce
                    param -= 1;             // so it's what the docs (incorrectly) say but apparently individual parameters use
            }
                        

        byte paramMSB = (byte)(param >>> 7);
        byte paramLSB = (byte)(param & 127);
        byte valMSB = (byte)(val >>> 7);
        byte valLSB = (byte)(val & 127);
                
        byte[] data = new byte[] 
            { 
                (byte)0xF0, 
                (byte)0x00,             // M-Audio
                (byte)0x01, 
                (byte)0x05, 
                (byte)0x21,                     // Venom 
                (byte)DEFAULT_ID,               //(byte)getID(), 
                (byte)0x02,                     // Write Data Dump 
                (byte)0x08,             // Edit Global Param
                paramMSB,
                paramLSB,
                valMSB,                         // This is only useful for Master Tempo
                valLSB,
                (byte)0xF7
            };
        return new Object[] { data };
                
    }




    //// These are a lot like the Prophet '08 7<-->8 bit conversions.
    ////
    //// The Venom manual describes conversion from 8 bit to 7 bit as
    //// taking 7 bytes at a time, removing the top bits and putting them
    //// in the first byte, producing 8 7-bit bytes as a result.  That's all
    //// fine and good, except that the data doesn't come in 7 byte chunks.
    //// It's 198 bytes long, which isn't a multiple of 7.  It appears that
    //// the answer is to take the remainder (2 bytes), strip off the high bits
    //// and make 3 more 7-bit bytes.


    // converts up to but not including 'to'
    byte[] convertTo8Bit(byte[] data, int from, int to)
    {
        // How big?
        int main = ((to - from) / 8) * 8;
        int remainder = (to - from) - main;
        int size = ((to - from) / 8) * 7 + (remainder - 1);
        
        byte[] newd = new byte[size];
        
        int j = 0;
        for(int i = from; i < to; i += 8)
            {            
                for(int x = 0; x < 7; x++)
                    {
                        if (j + x < newd.length)
                            newd[j + x] = (byte)(data[i + x + 1] | (byte)(((data[i] >>> x) & 0x1) << 7));
                    }
                j += 7;
            }
        
        return newd;
    }
        
        
    // converts all bytes
    byte[] convertTo7Bit(byte[] data)
    {
        // How big?
        int main = ((data.length) / 7) * 7;
        int remainder = data.length - main;
        int size = (data.length) / 7 * 8 + (remainder + 1);
        byte[] newd = new byte[size];   
             
        int j = 0;
        for(int i = 0; i < data.length; i+=7)
            {
                // First load the top bits
                for(int x = 0; x < 7; x++)
                    {
                        if (i+x < data.length)
                            newd[j] = (byte)(newd[j] | (((data[i + x] >>> 7) & 1) << x));
                    }
                j++;
                // Next load the data
                for(int x = 0; x < 7; x++)
                    {
                        if (i+x < data.length)
                            newd[j+x] = (byte)(data[i+x] & 127);
                    }
                j+=7;
            }
        return newd;
    }

    public Object[] emitAll(Model tempModel, boolean toWorkingMemory, boolean toFile)
    {
        if (tempModel == null)
            tempModel = getModel();

        byte[] data = new byte[36];
        
        data[0] = (byte)0xF0;
        data[1] = (byte)0x00;           // M-audio
        data[2] = (byte)0x01;
        data[3] = (byte)0x05;
        data[4] = (byte)0x21;           // Venom
        data[5] = (byte)DEFAULT_ID;             //(byte)getID();
        data[6] = (byte)0x02;           // Write Data Dump
        data[7] = (byte)0x00;           // Edit Buffer
        data[8] = (byte)0x00;           // Global Edit
        data[9] = (byte)0x00;           // [Ignored]
        data[data.length - 1] = (byte)0xF7;     
        
        int offset = 10;
        
        byte[] d = new byte[20];
        for(int i = 0; i < parameters.length; i++)
            {
                String key = parameters[i];
                
                if (key.equals("mastertempomsb"))
                    {
                        d[i] = (byte)((model.get("mastertempo") >>> 8) & 255);
                    }
                else if (key.equals("mastertempolsb"))
                    {
                        d[i] = (byte)(model.get("mastertempo") & 255);
                    }
                else if (key.equals("midisingleselect") || key.equals("midimultiselect") || key.equals("usbrecord") || 
                         key.equals("monorecord") || key.equals("localmode") || key.equals("arpeggiatorroute"))
                    {
                        d[i] = (byte)(model.get(key) == 0 ? 0 : 127);
                    }
                else
                    {
                        d[i] = (byte)model.get(key);
                    }
            }
                                
        // Nybblize, or whatever you'd call it, into data
        d = convertTo7Bit(d);
        System.arraycopy(d, 0, data, 10, d.length);
                
        // Compute checksum
        data[data.length - 2] = checksum(data, 6, data.length - 2);             // starting at command

        Object[] result = new Object[] { data };
        return result;
    }

    public byte[] requestCurrentDump()
    {
        byte[] data = new byte[11];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x00;
        data[2] = (byte)0x01;
        data[3] = (byte)0x05;
        data[4] = (byte)0x21;
        data[5] = (byte)DEFAULT_ID; //(byte)getID();
        data[6] = (byte)0x01;           // data dump request
        data[7] = (byte)0x00;           // current buffer
        data[8] = (byte)0x00;           // global
        data[9] = (byte)0x00;           // doesn't matter
        data[10] = (byte)0xF7;
        return data;
    }

            
    public static String getSynthName() { return "M-Audio Venom [Global]"; }
    
}
