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

public class KawaiK4Drum extends Synth
    {
    public static final String[] BANKS = { "Internal", "External" };

    public static final String[] WAVES = { "Sin 1st", "Sin 2nd", "Sin 3rd", "Sin 4th", "Sin 5th", "Sin 6th", "Sin 7th", "Sin 8th", "Sin 9th", 
                                           "Saw 1", "Saw 2", "Saw 3", "Saw 4", "Saw 5", "Saw 6", "Saw 7", "Saws", 
                                           "Pulse", "Triangle", "Square", "Rectangular 1", "Rectangular 2", "Rectangular 3", "Rectangular 4", "Rectangular 5", "Rectangular 6", 
                                           "Pure Horn L", "Punch Brass 1", "Oboe 1", "Oboe 2", "Classic Grand", 
                                           "Electric Piano 1", "Electric Piano 2", "Electric Piano 3", "Electric Organ 1", "Electric Organ 2", 
                                           "Positif", "Electric Organ 3", "Electric Organ 4", "Electric Organ 5", "Electric Organ 6", "Electric Organ 7", 
                                           "Electric Organ 8", "Electric Organ 9", "Classic Guitar", "Steel Strings", "Harp", "Wood Bass", "Synth Bass 3", 
                                           "Digibass", "Finger Bass", "Marimba", "Synth Voice", "Glass Harp 1", "Cello", "Xylophone", "Electric Piano 4", 
                                           "Synclavier M", "Electric Piano 5", "Electric Organ 10", "Electric Organ 11", "Electric Organ 12", "Big Pipe", 
                                           "Glass Harp 2", "Random", "Electric Piano 6", "Synth Bass 4", "Synth Bass 1", "Synth Bass 2", "Quena", "Oboe 3", 
                                           "Pure Horn H", "Fat Brass", "Punch Brass 2", "Electric Piano 7", "Electric Piano 8", "Synclavier 2", 
                                           "Harpsichord M", "Harpsichord L", "Harpsichord H", "Electric Organ 13", "Koto", "Sitar L", "Sitar H", 
                                           "Pick Bass", "Synth Bass 5", "Synth Bass 6", "Vibraphone Attack", "Vibraphone 1", "Horn Vibe", 
                                           "Steel Drum 1", "Steel Drum 2", "Vibraphone 2", "Marimba Attack", "Harmonica", "Synth", "Kick", 
                                           "Gated Kick", "Snare Tite", "Snare Deep", "Snare Hi", "Rim Snare", "Rim Shot", "Tom", "Tom VR", 
                                           "Electric Tom", "High Hat Closed", "High Hat Open", "High Hatopen VR", "High Hat Foot", "Crash", "Crash VR", "Crash VR 2",
                                           "Ride Edge", "Ride Edge VR", "Ride Cup", "Ride Cup VR", "Claps", "Cowbell", "Conga", "Conga Slap", 
                                           "Tambourine", "Tambourine VR", "Claves", "Timbale", "Shaker", "Shaker VR", "Timpani", "Timpani VR", 
                                           "Sleighbell", "Bell", "Metal Hit", "Click", "Pole", "Glocken", "Marimba", "Piano Attack", "Water Drop", 
                                           "Char", "Piano Normal", "Piano VR", "Cello Normal", "Cello VR 1", "Cello VR 2", "Cello 1-Shot", 
                                           "Strings Normal", "Strings VR", "Slap Bass L Normal", "Slap Bass L VR", "Slap Bass L 1-Shot", 
                                           "Slap Bass H Normal", "Slap Bass H VR", "Slap Bass H 1-Shot", "Pick Bass Normal", "Pick Bass VR", 
                                           "Pick Bass 1-Shot", "Wood Bass Attack", "Wood Bass Normal", "Wood Bass VR", "Fretless Normal", 
                                           "Fretless VR", "Synth Bass Normal", "Synth Bass VR", "Electric Guitar Mute Normal", 
                                           "Electric Guitar Mute VR", "Electric Guitar Mute 1-Shot", "Dist Mute Normal", "Dist Mute VR", 
                                           "Dist Mute 1-Shot", "Dist Lead Normal", "Dist Lead VR", "Electric Guitar Normal", "Gut Guitar Normal", 
                                           "Gut Guitar VR", "Gut Guitar 1-Shot", "Flute Normal", "Flute 1-Shot", "Bottle Blow Normal", 
                                           "Bottle Blow VR", "Sax Normal", "Sax VR 1", "Sax VR 2", "Sax 1-Shot", "Trumpet Normal", 
                                           "Trumpet VR 1", "Trumpet VR 2", "Trumpet 1-Shot", "Trombone Normal", "Trombone VR", 
                                           "Trombone 1-Shot", "Voice", "Noise", "Piano 1", "Piano 2", "Piano 3", "Piano 4", "Pianos", 
                                           "Cello 1", "Cello 2", "Cello 3", "Cello 4", "Cello 5", "Cello 6", "Strings 1", "Strings 2", 
                                           "Slap Bass L", "Slap Bass L 1-Shot", "Slap Bass H", "Slap Bass H 1-Shot", "Pick Bass 1", 
                                           "Pick Bass 2 1-Shot", "Pick Bass 3 1-Shot", "Electric Guitar Mute", "Electric Guitar Mute 1-Shot", 
                                           "Dist Lead 1", "Dist Lead 2", "Dist Lead 3", "Gut Guitar 1", "Gut Guitar 2", "Gut Guitar 3 1-Shot", 
                                           "Gut Guitar 4 1-Shot", "Flute 1", "Flute 2", "Sax 1", "Sax 2", "Sax 3", "Sax 4 1-Shot", "Sax 5 1-Shot", 
                                           "Sax 6 1-Shot", "Trumpet", "Trumpet 1-Shot", "Voice 1", "Voice 2", "Reverse 1", "Reverse 2", 
                                           "Reverse 3", "Reverse 4", "Reverse 5", "Reverse 6", "Reverse 7", "Reverse 8", "Reverse 9", 
                                           "Reverse 10", "Reverse 11", "Loop 1", "Loop 2", "Loop 3", "Loop 4", "Loop 5", "Loop 6", 
                                           "Loop 7", "Loops", "Loop 9", "Loop 10", "Loop 11", "Loop 12"};

    public static final String[] KEYS = new String[] { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
    public static final String[] SUBMIX_CHANNELS = { "A", "B", "C", "D", "E", "F", "G", "H" };

    
    public KawaiK4Drum()
        {
        for(int i = 0; i < allParameters.length; i++)
            {
            allParametersToIndex.put(allParameters[i], Integer.valueOf(i));
            }

        for(int i = 0; i < internalParameters.length; i++)
            {
            internalParametersToIndex.put(internalParameters[i], Integer.valueOf(i));
            }

        JComponent soundPanel = new SynthPanel(this);
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        hbox.addLast(addGlobal(Style.COLOR_A()));
        vbox.add(hbox);
        
        vbox.add(addKeys(Style.COLOR_B()));

        soundPanel.add(vbox);

        addTab("Drum", soundPanel);
        
        model.set("bank", 0);           // internal

        loadDefaults();
        }
        


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


    public JComponent addGlobal(Color color)
        {
        Category category = new Category(this, "Global", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        comp = new LabelledDial("MIDI", this, "channel", color, 0, 15)
            {
            public String map(int val)
                {
                return "" + (val + 1);
                }
            };
        hbox.add(comp);
        model.removeMetricMinMax("channel");
        ((LabelledDial)comp).addAdditionalLabel("Channel");

        comp = new LabelledDial("Volume", this, "volume", color, 0, 100);
        hbox.add(comp);

        // Error in MIDI spec, Section 8.
        // Velocity depth is actually -50...50
                
        comp = new LabelledDial("Velocity", this, "velocitydepth", color, 0, 100, 50);
        hbox.add(comp);
        ((LabelledDial)comp).addAdditionalLabel("Depth");

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public HBox buildKey(final int key, Color color)
        {
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
                
        VBox vbox = new VBox();
        comp = new LabelledDial("Submix", this, "key" + key + "submix", color, 0, 7)
            {
            public String map(int val)
                {
                return SUBMIX_CHANNELS[val];
                }
            };
        vbox.add(comp);
        model.removeMetricMinMax("key" + key + "submix");

        comp = new PushButton("Apply to All")
            {
            public void perform()
                {
                int submix = model.get("key" + key + "submix");
                for(int i = 1; i < 62; i++)
                    {
                    model.set("key" + i + "submix", submix);
                    }
                }
            };
        vbox.add(comp);
        hbox.add(vbox);



        vbox = new VBox();
                
        HBox hbox2 = new HBox();
        VBox vbox2 = new VBox();
        params = WAVES;
        comp = new Chooser("Wave 1", this, "key" + key + "waveselect1", params);
        vbox2.add(comp);
        hbox2.add(vbox2);

        comp = new LabelledDial("Decay 1", this, "key" + key + "decay1", color, 0, 100);
        hbox2.add(comp);
                
        comp = new LabelledDial("Tune 1", this, "key" + key + "tune1", color, 0, 100, 50);
        hbox2.add(comp);
                
        // Error in MIDI spec, Section 8.
        // Level actually goes 0...100, not 0...99
                
        comp = new LabelledDial("Level 1", this, "key" + key + "level1", color, 0, 100);
        hbox2.add(comp);
        vbox.add(hbox2);

        hbox2 = new HBox();
        vbox2 = new VBox();
        params = WAVES;
        comp = new Chooser("Wave 2", this, "key" + key + "waveselect2", params);
        vbox2.add(comp);
        hbox2.add(vbox2);
                
        comp = new LabelledDial("Decay 2", this, "key" + key + "decay2", color, 0, 100);
        hbox2.add(comp);
                
        comp = new LabelledDial("Tune 2", this, "key" + key + "tune2", color, 0, 100, 50);
        hbox2.add(comp);
                
        // Error in MIDI spec, Section 8.
        // Level actually goes 0...100, not 0...99
                
        comp = new LabelledDial("Level 2", this, "key" + key + "level2", color, 0, 100);
        hbox2.add(comp);
        vbox.add(hbox2);
        
        hbox.add(vbox);
                
        return hbox;
        }


    HBox[] keys = new HBox[61];

    public JComponent addKeys(Color color)
        {
        final Category category = new Category(this, "Drum Key", color);
        category.makePasteable("key1");
        category.makeDistributable("key1");  // pretty useless

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        final VBox vbox = new VBox();
 
        for(int i = 0; i < keys.length; i++)
            {
            keys[i] = buildKey(i+1, color);
            }
                
        vbox.addBottom(keys[0]);
                       
        comp = new LabelledDial("Note", this, "note", color, 36, 96)
            {
            public String map(int val)
                {
                return KEYS[val % 12] + (val / 12 + 1);  // note integer division
                }
            };
        hbox.add(comp);
        
        comp = new KeyDisplay("Note", this, "note", color, 36, 96, 0)
            {
            public void userPressed(int key)
                {
                doSendTestNote(key, true, false);
                }
            };
        ((KeyDisplay)comp).setDynamicUpdate(true);
        vbox.add(comp);
                                
        vbox.add(Strut.makeVerticalStrut(20));
                
        model.register("note", new Updatable()
            {
            public void update(String key, Model model)
                {
                vbox.removeLast();
                vbox.addBottom(keys[model.get(key, 36) - 36]);
                vbox.revalidate();
                vbox.repaint();
                category.makePasteable("key" + (model.get(key, 36) - 35));  // so we reset the preamble
                }
            });
                        
        hbox.add(vbox);

        // set it once                  
        model.set("note", model.get("note", 36));
                
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
                
                
    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        JComboBox bank = new JComboBox(BANKS);
        bank.setSelectedIndex(model.get("bank"));
        
        while(true)
            {
            boolean result = showMultiOption(this, new String[] { "Bank"}, 
                new JComponent[] { bank }, title, "Enter the Bank");
                
            if (result == false) 
                return false;
                                
            int i = bank.getSelectedIndex();
                        
            change.set("bank", i);
                        
            return true;
            }
        }

    public int parse(byte[] data, boolean fromFile)
        {
        model.set("bank", data[6] == 0x01 ? 0 : 1);
                        
        int b = 0;
        for(int i = 0; i < 682; i++)
            {
            String key = allParameters[i];

            int note = (i / 11);
                
            if (b == 10)  // sub-checksum
                {
                // do nothing
                b = -1;
                }
            else if (key.equals("-"))
                {
                // do nothing
                }
            else if (key.endsWith("submix_waveselectmsb1"))
                {
                model.set("key" + note + "submix", data[i + 8] >>> 4);
                model.set("key" + note + "waveselect1", ((data[i + 8] & 1) << 7) | (data[i + 8 + 2]));
                }
            else if (key.endsWith("waveselectmsb2"))
                {
                model.set("key" + note + "waveselect2", ((data[i + 8] & 1) << 7) | (data[i + 8 + 2]));
                }
            else if (key.endsWith("waveselectlsb1"))
                {
                // do nothing
                }
            else if (key.endsWith("waveselectlsb2"))
                {
                // do nothing
                }
            else if (key.equals("channel") && !fromFile)
                {
                // Kawai Bug: MIDI channel is not properly returned on parse: it always returns 9 (channel "10").
                //
                // model.set("channel", getChannelOut());  // gotta do *something*
                // do nothing
                }
            else
                {
                model.set(key, data[i + 8]);
                }

            b++;
            }

        revise();
        return PARSE_SUCCEEDED;
        }
    
    public static final int EXPECTED_SYSEX_LENGTH = 682 + 9;
    
    public static boolean recognize(byte[] data)
        {
        return (data.length == EXPECTED_SYSEX_LENGTH &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x40 &&
            data[3] == (byte)0x20 &&
            data[4] == (byte)0x00 &&
            data[5] == (byte)0x04 &&
            (data[6] == (byte)0x01 || data[6] == (byte)0x03) &&
            data[7] == 32);
        }

    public static String getSynthName() { return "Kawai K4/K4r [Drum]"; }
    
    public String getDefaultResourceFileName() { return "KawaiK4Drum.init"; }
        
    public String getHTMLResourceFileName() 
        { 
        return "KawaiK4Drum.html";
        }

    
    
    
    
    
    
    

    // not even sure if I need this

    public String getPatchName(Model model) 
        {
        return "Drum";
        }






    /** Map of parameter -> index in the allParameters array. */
    HashMap allParametersToIndex = new HashMap();
    HashMap internalParametersToIndex = new HashMap();

    /** List of all parameters in order.  "-" is a reserved (unused and thus unnamed) parameter. */

    final static String[] allParameters = new String[682];
    final static String[] internalParameters = new String[552];
        
    static
        {
        System.arraycopy(new String[]
            {
            "channel",                   
            "volume",
            "velocitydepth",
            "-",
            "-",
            "-",
            "-",
            "-",
            "-",
            "-",
            "-",                                    // sub-checksum
            }, 0, allParameters, 0, 11);
        
        System.arraycopy(new String[]
            {
            "channel",
            "volume",
            "velocitydepth"
            }, 0, internalParameters, 0, 3);
        
        for(int i = 1; i < 62; i++)
            {
            System.arraycopy(new String[]
                {
                "key" + i + "submix_waveselectmsb1",
                "key" + i + "waveselectmsb2",
                "key" + i + "waveselectlsb1",
                "key" + i + "waveselectlsb2",
                "key" + i + "decay1",
                "key" + i + "decay2",
                "key" + i + "tune1",
                "key" + i + "tune2",
                "key" + i + "level1",
                "key" + i + "level2",
                "-",                            // sub-checksum
                }, 0, allParameters, i * 11, 11);

            System.arraycopy(new String[]
                {
                "key" + i + "submix",
                "key" + i + "waveselect1",
                "key" + i + "waveselect2",
                "key" + i + "decay1",
                "key" + i + "decay2",
                "key" + i + "tune1",
                "key" + i + "tune2",
                "key" + i + "level1",
                "key" + i + "level2",
                }, 0, internalParameters, i * 9 - 6, 9);

            }
        }

    public byte[] emit(String key) 
        { 
        if (key.equals("bank")) return new byte[0];  // this is not emittable
        if (key.equals("note")) return new byte[0];  // this is not emittable
                
        int source = 0;
        byte msb = (byte)(model.get(key) >>> 7);
        byte lsb = (byte)(model.get(key) & 127);

        int index = ((Integer)(internalParametersToIndex.get(key))).intValue();

        int note = 0;
        if (index > 3)
            {
            note = (index - 3) / 9;
            index = (index - 3) % 9 + 73;
            }
        else
            index = index + 70;
                        
        return new byte[] { (byte)0xF0, 0x40, (byte)getChannelOut(), 0x10, 0x00, 0x04, (byte)index, (byte)((note << 1) | msb), (byte)lsb, (byte)0xF7 };
        }


    public void messageFromController(MidiMessage message, boolean interceptedForInternalUse, boolean routedToSynth)
        { 
        if (message instanceof ShortMessage)
            {
            ShortMessage s = (ShortMessage)message;
            int status = s.getStatus();
            
            // NOTE_ON has a status from 0x90 to 0x9F (for all 16 channels)
            // and also cannot be velocity=0, since that would be equivalent to a NOTE OFF
            if (status >= ShortMessage.NOTE_ON && status <= ShortMessage.NOTE_ON + 15 && s.getData2() > 0)  // 0x90 to 0x9F
                {
                int key = s.getData1();
                if (key >= 36 && key <= 96)
                    {
                    model.set("note", key);
                    }           
                }
            }
        }

    public int getVoiceMessageRoutedChannel(int incomingChannel, int synthChannel)
        {
        return model.get("channel", incomingChannel);
        }

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

        byte[] data = new byte[682];
        byte[] bytes = new byte[10];
    
        int b = 0;
        for(int i = 0; i < 682; i++)
            {
            String key = allParameters[i];
                        
            int note = (i / 11);
                
            if (b == 10)  // sub-checksum 
                {
                data[i] = produceChecksum(bytes);
                b = -1;
                }
            else if (key.equals("-"))
                {
                bytes[b] = (data[i] = (byte)0x0);
                }
            else if (key.endsWith("submix_waveselectmsb1"))
                {
                bytes[b] = (data[i] = (byte)((model.get("key" + note + "submix") << 4) | (model.get("key" + note + "waveselect1") >>> 7)));
                }
            else if (key.endsWith("waveselectmsb2"))
                {
                bytes[b] = (data[i] = (byte)((model.get("key" + note + "waveselect2") >>> 7)));
                }
            else if (key.endsWith("waveselectlsb1"))
                {
                bytes[b] = (data[i] = (byte)((model.get("key" + note + "waveselect1") & 127)));
                }
            else if (key.endsWith("waveselectlsb2"))
                {
                bytes[b] = (data[i] = (byte)((model.get("key" + note + "waveselect2") & 127)));
                }
            else
                bytes[b] = (data[i] = (byte)(model.get(key)));
            b++;
            }

        // Error in Section 4-1, see "Corrected MIDI Implementation"

        boolean external;
        
        external = (tempModel.get("bank") > 4);

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
        result[7] = (byte)(0x20);       // indicates drum
        System.arraycopy(data, 0, result, 8, data.length);
        result[8 + data.length] = (byte)0xF7;
        return result;
        }



    public byte[] requestDump(Model tempModel) 
        { 
        boolean external = (tempModel.get("bank") == 1);
        return new byte[] { (byte)0xF0, 0x40, (byte)getChannelOut(), 0x00, 0x00, 0x04, 
            (byte)(external ? 0x03 : 0x01),
            0x20, (byte)0xF7};
        }
    
    
    


    ////// YOU MAY WANT TO IMPLEMENT SOME OF THE FOLLOWING

    public int getTestNotePitch() { return model.get("note"); }
    public int getTestNoteChannel() { return model.get("channel"); }

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


    public int getPauseAfterChangePatch() { return 200; }   // Seem to need about > 100ms

    public int getPauseAfterSendAllParameters() { return 100; } 
 
    public Model getNextPatchLocation(Model model)
        {
        Model newModel = buildModel();
        newModel.set("bank", 1 - model.get("bank"));
        return newModel;
        }

    public String getPatchLocationName(Model model)
        {
        // getPatchLocationName() is called from sprout() as a test to see if we should enable
        // batch downloading.  If we haven't yet created an .init file, then parameters won't exist
        // yet and this method will bomb badly.  So we return null in this case.
        if (!model.exists("bank")) return null;
        
        return BANKS[model.get("bank")];
        }
    }
