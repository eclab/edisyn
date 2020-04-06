/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.rolandd110;

import edisyn.*;
import edisyn.gui.*;
import edisyn.util.*;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.border.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javax.sound.midi.*;

/**
   A patch editor for the Roland JV-880.
        
   @author Sean Luke
*/

public class RolandD110Multi extends Synth
    {
    public static final String[] REVERB_MODE = new String[] { "Room 1", "Room 2", "Hall 1", "Hall 2", "Plate", "Tap Delay 1", "Tap Delay 2", "Tap Delay 3", "Off" };
    public static final String[] TONE_GROUP = new String[] { "Preset A", "Preset B", "Internal/Card", "Rhythm" };
    public static final String[] ASSIGN_MODE = new String[] { "Single / Last Note", "Single / First Note", "Multi / Last Note", "Multi / First Note" };
    public static final String[] NOTES = new String[] { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
    public String[] PRESETS = new String[] {
        "Acou Piano 1", "Acou Piano 2", "Acou Piano 3", "Honky Tonk", "Elec Piano 1", "Elec Piano 2", "Elec Piano 3", "Elec Piano 4",
        "Elec Organ 1", "Elec Organ 2", "Elec Organ 3", "Elec Organ 4", "Pipe Organ 1", "Pipe Organ 2", "Pipe Organ 3", "Accordion",
        "Harpsi 1", "Harpsi 2", "Harpsi 3", "Clav 1", "Clav 2", "Clav 3", "Celesta 1", "Celesta 2", "Violin 1", "Violin 2", "Cello 1",
        "Cello 2", "Contrabass", "Pizzicata", "Harp 1", "Harp 2", "Strings 1", "Strings 2", "Strings 3", "Strings 4", "Brass 1",
        "Brass 2", "Brass 3", "Brass 4", "Trumpet 1", "Trumpet 2", "Trombone 1", "Trombone 2", "Horn", "Fr Horn", "Engl Horn", "Tuba",
        "Flute 1", "Flute 2", "Piccolo", "Recorder", "Pan P1pes", "Bottleblow", "Breathpipe", "Whistle", "Sax 1", "Sax 2", "Sax 3",
        "Clarinet 1", "Clarinet 2", "Oboe", "Bassoon", "Harmonica", "Fantasy", "Harmo Pan", "Chorale", "Glasses", "Soundtrack",
        "Atmosphere", "Warm Bell", "Space Horn", "Echo Bell", "Ice Rains", "Oboe 2002", "Echo Pan", "Bell Swing", "Reso Synth",
        "Steam Pad", "V1be String", "Syn Lead 1", "Syn Lead 2", "Syn Lead 3", "Syn Lead 4", "Syn Bass 1", "Syn Bass 2", "Syn Bass 3",
        "Syn Bass 4", "Acou Bass 1", "Acou Bass 2", "Elec Bass 1", "Elec Bass 2", "Slap Bass 1", "Slap Bass 2", "Fretless 1",
        "Fretless 2", "Vibe", "Glock", "Maromba", "Xylophone", "Guitar 1", "Guitar 2", "Elec Gtr 1", "Elec Gtr 2", "Koto", "Shamssen",
        "Jamisen", "Sho", "Shakuhachi", "Wadako Set", "Sitar", "Steel Drum", "Tech Snare", "Elec Tom", "Reverse Cym", "Ethno Hit",
        "Timpani", "Triangle", "Wind Bell", "Tube Bell", "Orche Hit", "Bird Tweet", "One Note Jam", "Telephone", "Typewriter", "Insect",
        "Water Bells", "Jungle Tune", "Closed High Hat 1", "Closed High Hat 2", "Open High Hat 1", "Open High Hat 2", "Crash Cymbal",
        "Crash Cymbal (Short)", "Crash Cymbal (Mute)", "Ride Cymbal", "Ride Cymbal (Short)", "Ride Cymbal (Mute)", "Cup", "Cup (Mute)",
        "China Cymbal", "Splash Cymbal", "Bass Drum 1", "Bass Drum 2", "Bass Drum 3", "Bass Drum 4", "Snare Drum 1", "Snare Drum 2",
        "Snare Drum 3", "Snare Drum 4", "Snare Drum 5", "Snare Drum 6", "Rim Shot", "Brush 1", "Brush 2", "High Tom Tom 1",
        "Middle Tom Tom 1", "Low Tom Tom 1", "High Tom Tom 2", "Middle Tom Tom 2", "Low Tom Tom 2", "High Tom Tom 3", "Middle Tom Tom 3",
        "Low Tom Tom 3", "High Pitch Tom Tom 1", "High Pitch Tom Tom 2", "Hand Clap", "Tambourine", "Cowbell", "High Bongo", "Low Bongo",
        "High Conga (Mute)", "High Conga", "Low Conga", "High Timbale", "Low Timbale", "High Agogo", "Low Agogo", "Cabasa", "Maracas",
        "Short Whistle", "Long Whistle", "Ouijada", "Claves", "Castanets", "Triangle", "Wood Block", "Bell", "Native Drum 1",
        "Native Drum 2", "Native Drum 3", "Off" };
        
    public static final int MIDI_CHANNEL_OFF = 16;
        
    public RolandD110Multi()
        {
        for(int i = 0; i < allPartParameters.length; i++)
            {
            allPartParametersToIndex.put(allPartParameters[i], Integer.valueOf(i));
            }

        for(int i = 0; i < allCommonParameters.length; i++)
            {
            allCommonParametersToIndex.put(allCommonParameters[i], Integer.valueOf(i));
            }

                
        /// SOUND PANEL
                
        JComponent sourcePanel = new SynthPanel(this);
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        hbox.addLast(addReserve(Style.COLOR_B()));
        vbox.add(hbox);
        hbox = new HBox();
        hbox.add(addReverb(Style.COLOR_A()));
        hbox.addLast(addRhythm(Style.COLOR_C()));
        vbox.add(hbox);
        vbox.add(addPart(1, Style.COLOR_B()));
        vbox.add(addPart(2, Style.COLOR_B()));
        vbox.add(addPart(3, Style.COLOR_B()));
        
        sourcePanel.add(vbox, BorderLayout.CENTER);
        addTab("Common and Parts 1-3", sourcePanel);                

        sourcePanel = new SynthPanel(this);
        vbox = new VBox();
        vbox.add(addPart(4, Style.COLOR_B()));
        vbox.add(addPart(5, Style.COLOR_B()));
        vbox.add(addPart(6, Style.COLOR_B()));
        vbox.add(addPart(7, Style.COLOR_B()));
        vbox.add(addPart(8, Style.COLOR_B()));
        sourcePanel.add(vbox, BorderLayout.CENTER);
        addTab("Parts 4-8", sourcePanel);                

        model.set("patchname", "Init Patch");  // has to be 10 long

        model.set("number", 0);
                        
        loadDefaults();        
        }
                
                
    public JFrame sprout()
        {
        JFrame frame = super.sprout();
        // It doesn't make sense to send to current patch
        receiveCurrent.setEnabled(false);
        transmitTo.setEnabled(false);
        return frame;
        }         

    public String getDefaultResourceFileName() { return "RolandD110Multi.init"; }
    public String getHTMLResourceFileName() { return "RolandD110Multi.html"; }

    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        int original = model.get("number");
                
        JTextField number = new JTextField("" + ((original / 8 + 1) * 10 + (original % 8 + 1)), 3);

        while(true)
            {
            boolean result = showMultiOption(this, new String[] { "Patch Number"}, 
                new JComponent[] { number }, title, "Enter Patch number");
                
            if (result == false)
                return false;
                                
            int n;
            try { n = Integer.parseInt(number.getText()); }
            catch (NumberFormatException e)
                {
                showSimpleError(title, "The Patch Number must be an integer 11...88.\nDigits 9 and 0 are not permitted.");
                continue;
                }
            if (n < 1 || n > 88 || (n % 10 == 9) || (n % 10 == 0))
                {
                showSimpleError(title, "The Patch Number must be an integer 11...88.\nDigits 9 and 0 are not permitted.");
                continue;
                }
            
            n = ((n / 10) - 1) * 8 + (n % 10 - 1);
            
            change.set("number", n);
            return true;
            }
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
        HBox hbox2 = new HBox();
        comp = new PatchDisplay(this, 9);
        hbox2.add(comp);
        vbox.add(hbox2);
        
        comp = new StringComponent("Patch Name", this, "patchname", 10, "Name must be up to 10 ASCII characters.")
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
        vbox.addBottom(comp);
        hbox.add(vbox);

        hbox.add(Strut.makeHorizontalStrut(70));

        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }


    public JComponent addReverb( Color color)
        {
        Category category = new Category(this, "Reverb", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();

        VBox vbox = new VBox();
        params = REVERB_MODE;
        comp = new Chooser("Mode", this, "reverbmode", params);
        vbox.add(comp);
        hbox.add(vbox);
                
        comp = new LabelledDial("Time", this, "reverbtime", color, 0, 7, -1);
        hbox.add(comp);

        comp = new LabelledDial("Level", this, "reverblevel", color, 0, 7);
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addRhythm( Color color)
        {
        Category category = new Category(this, "Rhythm", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();

        comp = new LabelledDial("Output Level", this, "rhythmoutputlevel", color, 0, 100);
        hbox.add(comp);

        comp = new LabelledDial("MIDI Channel", this, "rhythmmidichannel", color, 0, 16)        
            {
            public String map(int value)
                {
                if (value == 16) return "Off";
                else return "" + (value + 1);
                }
            };
        hbox.add(comp);
                
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addReserve(Color color)
        {
        final Category category = new Category(this, "Partial Reserve", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        for(int i = 0; i < 9; i++)
            {
            final int _i = i;
            comp = new LabelledDial(i == 8 ? "Rhythm" : "Part " + (i + 1), this, 
                i == 8 ? "rhythmpartialreserve" : "p" + (i + 1) + "partialreserve", color, 0, 32)
                {
                public void update(String key, Model model) 
                    {
                    // disable listeners
                    boolean li = model.getUpdateListeners();
                    model.setUpdateListeners(false);
                    boolean un = undo.getWillPush();
                    undo.setWillPush(false);
                                
                    // how big are we?
                    int over = 0;
                    for(int j = 0; j < 9; j++)
                        {
                        over = over + model.get(j == 8 ? "rhythmpartialreserve" : "p" + (j + 1) + "partialreserve", 0);
                        }
                                        
                    if (over > 32)
                        {
                        over -= 32;
                                         
                        // we're over
                        while(over > 0)
                            {
                            for(int j = 0; j < 9; j++)
                                {
                                if (over == 0) break;  // all done
                                if (j == _i) continue;  // don't decrease myself
                                int v = model.get(j == 8 ? "rhythmpartialreserve" : "p" + (j + 1) + "partialreserve", 0);
                                if (v > 0)
                                    {
                                    model.set(j == 8 ? "rhythmpartialreserve" : "p" + (j + 1) + "partialreserve", v - 1);
                                    over--;
                                    }
                                }
                            }
                        }

                    // reenable listeners
                    model.setUpdateListeners(li);
                    undo.setWillPush(un);                           
                                
                    // update, which forces an undo snapshot
                    super.update(key, model);               
                    category.repaint();  // otherwise the dials won't update                        
                    }
                };
            hbox.add(comp);
            }
                
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addPart(final int part, Color color)
        {
        Category category = new Category(this, "Part " + part, color);
        category.makePasteable("p" + part);

        JComponent comp;
        String[] params;
        VBox vbox = new VBox();
        HBox hbox = new HBox();


        final JLabel[] lab = new JLabel[1];
        final LabelledDial toneNumber = new LabelledDial("        Tone Number        ", this, "p" + part + "tonenumber", color, 0, 63, -1)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                if (lab[0] == null) return;  // not set up yet
                int group = model.get("p" + part + "tonegroup", -1);
                lab[0].setText(group == 0 ? " " + PRESETS[model.get(key)] + " " :
                        (group == 1 ? " " + PRESETS[model.get(key) + 64] + " " :
                        (group == 2 ? "" : " " + PRESETS[model.get(key) + 128] + " ")));
                }
            };
        lab[0] = toneNumber.addAdditionalLabel("");


        HBox hbox2 = new HBox();
        VBox vbox2 = new VBox(VBox.TOP_CONSUMES);
        vbox2.addLast(Stretch.makeVerticalStretch());
        final PushButton showButton = new PushButton("Show")
            {
            public void perform()
                {
                final RolandD110Tone synth = new RolandD110Tone();
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
                                tempModel.set("number", RolandD110Multi.this.model.get("p" + part + "tonenumber"));
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
        vbox2.add(showButton);
        
        params = TONE_GROUP;
        comp = new Chooser("Tone Group", this, "p" + part + "tonegroup", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                toneNumber.update("p" + part + "tonenumber", model);
                showButton.getButton().setEnabled(model.get(key) == 2);         // internal/card
                }
            };
        hbox2.add(comp);
        hbox2.add(vbox2);
        vbox.add(hbox2);

        params = ASSIGN_MODE;
        comp = new Chooser("Assign Mode", this, "p" + part + "assignmode", params);
        vbox.add(comp);
        hbox.add(vbox);

        hbox.add(toneNumber);

        comp = new LabelledDial("Key Shift", this, "p" + part + "keyshift", color, 0, 48, 24)
            {
            public boolean isSymmetric() { return true; }
            };
        hbox.add(comp);

        comp = new LabelledDial("Fine Tune", this, "p" + part + "finetune", color, 0, 100, 50)
            {
            public boolean isSymmetric() { return true; }
            };
        hbox.add(comp);

        comp = new LabelledDial("Bend Range", this, "p" + part + "benderrange", color, 0, 24);
        hbox.add(comp);

        comp = new LabelledDial("Output Assign", this, "p" + part + "outputassign", color, 0, 7)
            {
            public String map(int value)
                {
                if (value == 0) return "Mix";
                else if (value == 1) return "Min";
                else if (value == 2) return "Mult";
                else return "" + (value - 1);
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Output Level", this, "p" + part + "outputlevel", color, 0, 100);
        hbox.add(comp);

        comp = new LabelledDial("Pan", this, "p" + part + "panpot", color, 0, 14)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                if (value == 7) return "--";
                else if (value < 7) return "< " + (6 - value);
                else return "" + (value - 7) + " >";
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Key Range", this, "p" + part + "keyrangelower", color, 0, 127)
            {
            public String map(int value)
                {
                return NOTES[value % 12] + ((value / 12) - 1);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Lower");
        hbox.add(comp);

        comp = new LabelledDial("Key Range", this, "p" + part + "keyrangeupper", color, 0, 127)
            {
            public String map(int value)
                {
                return NOTES[value % 12] + ((value / 12) - 1);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Upper");
        hbox.add(comp);

        comp = new LabelledDial("MIDI Channel", this, "p" + part + "midichannel", color, 0, 16) 
            {
            public String map(int value)
                {
                if (value == 16) return "Off";
                else return "" + (value + 1);
                }
            };
                
        hbox.add(comp);


        category.add(hbox, BorderLayout.CENTER);
        return category;
        }



    HashMap allPartParametersToIndex = new HashMap();
        
    final static String[] allPartParameters = new String[]
    {
    "tonegroup",
    "tonenumber",
    "keyshift",
    "finetune",
    "benderrange",
    "assignmode",
    "outputassign",
    "-",
    "outputlevel",
    "panpot",
    "keyrangelower",
    "keyrangeupper",
    };
    
    
    HashMap allCommonParametersToIndex = new HashMap();
        
    final static String[] allCommonParameters = new String[]
    {
    "patchname",
    "reverbmode",
    "reverbtime",
    "reverblevel",
    "p1partialreserve",
    "p2partialreserve",
    "p3partialreserve",
    "p4partialreserve",
    "p5partialreserve",
    "p6partialreserve",
    "p7partialreserve",
    "p8partialreserve",
    "rhythmpartialreserve",
    "p1midichannel",
    "p2midichannel",
    "p3midichannel",
    "p4midichannel",
    "p5midichannel",
    "p6midichannel",
    "p7midichannel",
    "p8midichannel",
    "rhythmmidichannel",
    "rhythmoutputlevel",
    };

    final static String[] allSystemParameters = new String[]
    {
//    "-",                                // master tune
    "reverbmode",
    "reverbtime",
    "reverblevel",
    "p1partialreserve",
    "p2partialreserve",
    "p3partialreserve",
    "p4partialreserve",
    "p5partialreserve",
    "p6partialreserve",
    "p7partialreserve",
    "p8partialreserve",
    "rhythmpartialreserve",
    "p1midichannel",
    "p2midichannel",
    "p3midichannel",
    "p4midichannel",
    "p5midichannel",
    "p6midichannel",
    "p7midichannel",
    "p8midichannel",
    "p8midichannel",
    "rhythmmidichannel",
    "-",
    "patchname",
    };

    public byte getID() 
        { 
        try 
            { 
            byte b = (byte)(Byte.parseByte(tuple.id));
            if (b >= 17) return (byte)(b - 1);
            }
        catch (NullPointerException e) { } // expected.  Happens when tuple's not built yet
        catch (NumberFormatException e) { e.printStackTrace(); }
        return (byte)16;                // IDs start at 17
        }
        
        
    public byte produceChecksum(byte[] data)
        {
        return produceChecksum(data, 0, data.length);
        }
                
    /** The checksum is computed on all the ADDRESS and DATA data.
        Just add up the data, mod 128, and subtract the result from 128. Return that, unless it is 128, in which case return 0. */
    public byte produceChecksum(byte[] data, int start, int end)
        {
        // The checksum works as follows:
        // 1. Add all the data
        // 2. mod by 128 (that is, & 127)
        // 3. Subtract from 128
        // 4. If the result is 128, return 0
        // 5. Else return the result
                
        int check = 0;
        for(int i = start; i < end; i++)
            {
            check += data[i];
            }
        check = check & 0x7F;
        check = 0x80 - check;
        if (check == 0x80) check = 0;
        return (byte) check;
        }
        
    // Builds a byte[] consisting of the data associated with the given key,
    // which can be then inserted into an emit
    public byte[] getData(String key)
        {
        if (key.equals("patchname"))                                // name is 10-byte
            {
            byte[] data = new byte[10];
            String name = model.get(key, "Untitled");
            for(int i = 0; i < name.length(); i++)
                {
                data[i] = (byte)(name.charAt(i));
                }
            return data;
            }
        else
            {
            return new byte[] { (byte) model.get(key) };
            }
        }


    public byte[] emitPartials()
        {
        byte AA = (byte)0x10;
        byte BB = (byte)0x00;
        byte CC = (byte)0x04;           // Location of p1partialreserve in system memory

        byte[] payload = new byte[9];
        for(int i = 0; i < payload.length; i++)
            {
            payload[i] = (byte)(model.get(allCommonParameters[i + 4]));  // starting at p1partialreserve
            }

        byte checksum = produceChecksum(concatenate(new byte[] { AA, BB, CC }, payload));
        return concatenate(concatenate(new byte[] { (byte)0xF0, (byte)0x41, getID(), (byte)0x16, (byte)0x12, AA, BB, CC }, payload),
            new byte[] { checksum, (byte) 0xF7 });
        }
        

    public byte[] emit(String key)
        {
        if (key.equals("-")) return new byte[0];  // this is not emittable
        if (key.equals("number")) return new byte[0];  // this is not emittable
        if (key.equals("rhythmoutputlevel")) return new byte[0];  // this is not emittable it appears
        
        if (key.endsWith("reserve")) return emitPartials();
        
        byte AA = (byte)0x10;
        byte BB = (byte)0x00;
        byte CC = (byte)0x00;

        if (allCommonParametersToIndex.containsKey(key))
            {
            // need to load into system memory
            // So AA = 0x10
            CC = (byte)(CC + ((Integer)(allCommonParametersToIndex.get(key))).intValue());  
            }
        else
            {
            // Figure out the part
            int part = StringUtility.getInt(key);
            AA = (byte)0x03;
            CC = (byte)(0x10 * (part - 1));
            CC = (byte)(CC + ((Integer)(allPartParametersToIndex.get(key.substring(2)))).intValue());       // get rid of the "p1"...
            }

        byte[] payload = getData(key);

        // Handle irregularities in multi-byte data
        if (payload.length == 10)
            {
            byte[] data = new byte[20];
                        
            // gather data which is checksummed
            byte[] checkdata = new byte[3 + 10];
            System.arraycopy(new byte[] { AA, BB, CC }, 0, checkdata, 0, 3);
            System.arraycopy(payload, 0, checkdata, 3, payload.length);
                        
            // concatenate all data
            byte checksum = produceChecksum(checkdata);
            data[0] = (byte)0xF0;
            data[1] = (byte)0x41;
            data[2] = getID();
            data[3] = (byte)0x16;
            data[4] = (byte)0x12;
            System.arraycopy(checkdata, 0, data, 5, checkdata.length);
            data[18] = checksum;
            data[19] = (byte)0xF7;
                    
            return data;
            }
        else                                                                                    // Some data is 1-byte
            {
            byte checksum = produceChecksum(new byte[] { AA, BB, CC, payload[0] });
            return new byte[] { (byte)0xF0, (byte)0x41, getID(), (byte)0x16, (byte)0x12, AA, BB, CC, payload[0], checksum, (byte)0xF7 };
            }
        }
    
    
    public int parse(byte[] data, boolean fromFile)
        {
        // What is the patch number?
        int AA = data[5];
        int BB = data[6];
        int CC = data[7];
        if (AA == 0x06)
            {
            model.set("number", BB);
            }
        else
            {
            model.set("number", 0);
            }
        
        int pos = 8;
        String name = "";
        for(int i = 0; i < 10; i++)
            {
            name = name + ((char)data[pos++]);
            }
        model.set("patchname", name);
        model.set("reverbmode", data[pos++]);
        model.set("reverbtime", data[pos++]);
        model.set("reverblevel", data[pos++]);
        
        // partial reserve
        for(int p = 1; p < 9; p++)
            {
            model.set("p" + p + "partialreserve", data[pos++]);
            }
        model.set("rhythmpartialreserve", data[pos++]);

        // midi 
        for(int p = 1; p < 9; p++)
            {
            model.set("p" + p + "midichannel", data[pos++]);
            }
        model.set("rhythmmidichannel", data[pos++]);
        

        // parts
        for(int t = 1; t < 9; t++)
            {
            for(int i = 0; i < allPartParameters.length; i++)
                {
                if (allPartParameters[i].equals("-")) { pos++; continue; }  // there's a dummy in patch parameters
                else
                    {
                    model.set("p" + t + allPartParameters[i], data[pos++]);
                    }
                }
            }
        model.set("rhythmoutputlevel", data[pos++]);
        revise();
        return PARSE_SUCCEEDED;
        }
    
    
    public Object[] emitAll(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {          
        if (tempModel == null)
            tempModel = getModel();
        
        if (toWorkingMemory)
            {
            // we have to emit to two locations
            byte[] buf1 = new byte[33 + 10];
            buf1[0] = (byte)0xF0;
            buf1[1] = (byte)0x41;
            buf1[2] = (byte)getID();
            buf1[3] = (byte)0x16;
            buf1[4] = (byte)0x12;
            buf1[5] = (byte)0x10;
            buf1[6] = (byte)0x00;
            buf1[7] = (byte)0x01;               // skip master tune
            for(int i = 0; i < allSystemParameters.length; i++)
                {
                if (allSystemParameters[i].equals("-")) continue;
                if (allSystemParameters[i].equals("patchname"))         // it's last
                    {
                    String name = model.get(allSystemParameters[i], "Untitled") + "          ";
                    for(int c = 0; c < 10; c++)
                        {
                        buf1[i + 8 + c] = (byte)(name.charAt(c));
                        }
                    }
                else
                    {
                    buf1[i + 8] = (byte)model.get(allSystemParameters[i]);
                    }
                }
            buf1[buf1.length - 2] = produceChecksum(buf1, 5, buf1.length - 2);
            buf1[buf1.length - 1] = (byte)0xF7;

            byte[] buf2 = new byte[0x80 + 10];
            buf2[0] = (byte)0xF0;
            buf2[1] = (byte)0x41;
            buf2[2] = (byte)getID();
            buf2[3] = (byte)0x16;
            buf2[4] = (byte)0x12;
            buf2[5] = (byte)0x03;
            buf2[6] = (byte)0x00;
            buf2[7] = (byte)0x00;
            for(int p = 1; p <= 8; p++)
                {
                for(int i = 0; i < allPartParameters.length; i++)
                    {
                    if (allPartParameters[i] == "-") continue;
                    buf2[(p-1) * 0x10 + i + 8] = (byte)model.get("p" + p + allPartParameters[i]);
                    }
                }
            buf2[buf2.length - 2] = produceChecksum(buf2, 5, buf2.length - 2);
            buf2[buf2.length - 1] = (byte)0xF7;
            return new Object[] { buf1, buf2 };
            }
        else
            {
            // we emit to a single patch location
            byte[] buf1 = new byte[138];
            buf1[0] = (byte)0xF0;
            buf1[1] = (byte)0x41;
            buf1[2] = (byte)getID();
            buf1[3] = (byte)0x16;
            buf1[4] = (byte)0x12;
            buf1[5] = (byte)0x06;
            buf1[6] = (byte)tempModel.get("number");
            buf1[7] = (byte)0x00;
            String name = model.get("patchname", "Untitled") + "          ";
            for(int c = 0; c < 10; c++)
                {
                buf1[8 + c] = (byte)(name.charAt(c));
                }
            for(int i = 0; i < allCommonParameters.length; i++)
                {
                if (allCommonParameters[i].equals("patchname")) continue;  // already did it
                if (allCommonParameters[i].equals("-")) continue;
                if (allCommonParameters[i].equals("rhythmoutputlevel")) continue;       // this is done at the end
                buf1[10 + i + 8 - 1] = (byte)model.get(allCommonParameters[i]);
                }
                                
            for(int p = 1; p <= 8; p++)
                {
                for(int i = 0; i < allPartParameters.length; i++)
                    {
                    if (allPartParameters[i] == "-") continue;
                    buf1[0x1F + 8 + (p - 1) * 0x0C + i] = (byte)model.get("p" + p + allPartParameters[i]);
                    }
                }
            buf1[buf1.length - 3] = (byte)model.get("rhythmoutputlevel");
            buf1[buf1.length - 2] = produceChecksum(buf1, 5, buf1.length - 2);
            buf1[buf1.length - 1] = (byte)0xF7;
            return new Object[] { buf1 };
            }
        }

    // Requests a Patch from a specific RAM slot (1...64)
    public byte[] requestDump(Model tempModel)
        {
        if (tempModel == null)
            tempModel = getModel();

        int number = tempModel.get("number");
        // we're loading from Tone Temporary [synth]
        byte AA = (byte)(0x06);
        byte BB = (byte)(number * 2);
        byte CC = (byte)(0x00);
        byte LSB = (byte)0;
        byte MSB = (byte)1; 
        
        byte checksum = produceChecksum(new byte[] { AA, BB, CC, (byte)0x00, LSB, MSB });
        byte[] b = new byte[] { (byte)0xF0, (byte)0x41, getID(), (byte)0x16, (byte)0x11, 
            AA, BB, CC, (byte)0x00, MSB, LSB, checksum, (byte)0xF7 }; 
        return b;
        }
    
    public static boolean recognize(byte[] data)
        {
        return ((data[0] == (byte)0xF0) &&
            (data[1] == (byte)0x41) &&
            (data[3] == (byte)0x16) &&
            (data[4] == (byte)0x12) &&
            (data[5] == 0x06)) &&  // patches
            (data.length == 138);
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
            if (c >= 32 && c <= 127)
                continue;
            else
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

        String nm = model.get("patchname", "Init");
        String newnm = revisePatchName(nm);
        if (!nm.equals(newnm))
            model.set("patchname", newnm);
        }
        
    public static String getSynthName() { return "Roland D-110 [Multi]"; }
    
    public String getPatchName(Model model) { return model.get("patchname", "Untitled  "); }

    public int getPauseAfterChangePatch() { return 100; }

    public int getPauseAfterSendAllParameters() { return 100; } 

    public int getPauseAfterSendOneParameter() { return 25; }       // In the 1.07 firmware notes it says "at least 20ms" (http://llamamusic.com/d110/ROM_IC_Bug_Fixes.html).  In my firmware (1.10) the D-110 can handle changes thrown at it full blast, but earlier firmware (1.07) cannot.
 
    public void changePatch(Model tempModel)
        {
        // though you can change patch to a card patch, there's no way to read or write it via sysex,
        // so we only bother with internal.
        try 
            {
            tryToSendMIDI(new ShortMessage(ShortMessage.PROGRAM_CHANGE, getChannelOut(), tempModel.get("number"), 0));
            }
        catch (InvalidMidiDataException ex)
            {
            ex.printStackTrace();
            }
        }
    
    public Model getNextPatchLocation(Model model)
        {
        int number = model.get("number");
        
        number++;
        if (number >= 64)
            {
            number = 0;
            }
                
        Model newModel = buildModel();
        newModel.set("number", number);
        return newModel;
        }

    public String getPatchLocationName(Model model)
        {
        // getPatchLocationName() is called from sprout() as a test to see if we should enable
        // batch downloading.  If we haven't yet created an .init file, then parameters won't exist
        // yet and this method will bomb badly.  So we return null in this case.
        if (!model.exists("number")) return null;
        
        int original = model.get("number");
        return ("" + ((original / 8 + 1) * 10 + (original % 8 + 1)));
        }

    /** Roland only allows IDs from 17...32.  Don't ask. */
    public String reviseID(String id)
        {
        try
            {
            int val = Integer.parseInt(id);
            if (val < 17) val = 17;
            if (val > 32) val = 32;
            return "" + val;
            }
        catch (NumberFormatException ex)
            {
            return "" + (getID() + 1);
            }
        }
        
    public int getBulkDownloadWaitTime() { return 750; }
    }
