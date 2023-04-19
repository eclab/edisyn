/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.rolandu110;

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
   A patch editor for Roland U-110 Timbres
*/

public class RolandU110 extends Synth
    {

    public static final String[] PCM_NAMES = 
        {
        "Internal",
        "01 Pipe Organ & Harpsichord",
        "02 Latin & FX Percussion",
        "03 Ethnic",
        "04 Electric Grand and Clavi",
        "05 Orchestral Strings",
        "06 Orchestral Winds",
        "07 Electric Guitar",
        "08 Synthesizer",
        "09 Guitar & Keyboards",
        "10 Rock Drums",
        "11 Sound Effects",
        "12 Sax & Trombone",
        "13 Super Strings",
        "14 Super Ac Guitar",
        "15 Super Brass"
        };

    public static final String[][] PCM =
        {
            {
            // U-110 INTERNAL
            "A. Piano 1", "A. Piano 2", "A. Piano 3", "A. Piano 4", "A. Piano 5", 
            "A. Piano 6", "A. Piano 7", "A. Piano 8", "A. Piano 9", "A. Piano 10", 
            "E. Piano 1", "E. Piano 2", "E. Piano 3", "E. Piano 4", "E. Piano 5", 
            "Vib 1", "Vib 2", "Vib 3", "Bell 1", "Bell 2", "Bell 3", "Bell 4", "Marimba", 
            "A. Guitar 1", "A. Guitar 2", "A. Guitar 3", "A. Guitar 4", "A. Guitar 5", 
            "E. Guitar 1", "E. Guitar 2", "E. Guitar 3", "E. Guitar 4", 
            "Slap 1", "Slap 2", "Slap 3", "Slap 4", "Slap 5", "Slap 6", 
            "Slap 7", "Slap 8", "Slap 9", "Slap 10", "Slap 11", "Slap 12", 
            "Fingered 1", "Fingered 2", "Picked 1", "Picked 2", "Fretless 1", "Fretless 2",
            "AC. Bass", "Syn. Bass 1", "Syn. Bass 2", "Syn. Bass 3", 
            "Choir 1", "Choir 2", "Choir 3", "Choir 4", 
            "Strings 1", "Strings 2", "Strings 3", "Strings 4", 
            "E. Organ 1", "E. Organ 2", "E. Organ 3", "E. Organ 4", "E. Organ 5", 
            "E. Organ 6", "E. Organ 7", "E. Organ 8", "E. Organ 9", "E. Organ 10", 
            "E. Organ 11", "E. Organ 12", "E. Organ 13", 
            "Soft TP 1", "Soft TP 2", "Soft TP 3", 
            "TP / TRB 1", "TP / TRB 2", "TP / TRB 3", "TP / TRB 4", "TP / TRB 5", "TP / TRB 6", 
            "Sax 1", "Sax 2", "Sax 3", "Sax 4", "Sax 5", 
            "Brass 1", "Brass 2", "Brass 3", "Brass 4", "Brass 5", 
            "Flute 1", "Flute 2", "Shaku 1", "Shaku 2", "Drums [SETUP]"            },
            { 
            // SN-U110-01 - Pipe Organ and Harpsichord
            "Harpsichord 1", "Harpsichord 2", "Harpsichord 3", "Harpsichord 4", "Harpsichord 5", "Harpsichord 6",
            "Positive 1", "Positive 2", "Positive 3", "Positive 4", "Positive 5", "Positive 6", 
            "Church 1", "Church 2", "Church 3", "Church 4", "Church 5", "Church 6", "Church 7", "Church 8", "Church Reverb", 
            },
            { 
            // SN-U110-02 - Latin and FX Percussion
            "Latin 1 [SETUP]", "Latin 2 [SETUP]", "Latin 3 [SETUP]", "FX 1 [SETUP]", "FX 2 [SETUP]", "FX 3 [SETUP]", "FX 4 [SETUP]", 
            "Conga 1", "Conga 2", "Conga 3", "Bongo", "Claves", "Timbale", 
            "Tambourine", "Wood Block", "Whistle", "Triangle", "Belltree", 
            "Jingle Bell", "Vibraslap", "Castanet", "Maracas", "Agogo 1", "Agogo 2", 
            "Cuica 1", "Cuica 2", "Guiro 1", "Guiro 2", "Guiro 3", "Berimbau", 
            "Shekele", "Steel Drum", "Log Drum", "Orch Hit", "Siren", 
            "Type 1", "Type 2", "Clock", "Pinball", "Telephone", "Smsh Glass", 
            "Rezno", "Eerie", "Ambia Jr", "Templ Blk", "Zing!", "Boing!", 
            "Mod Zap", "Interface", "Scratch", "Stake", "Zappu"
            },
            {
            // SN-U110-03 - Ethnic
            "Tabla", "Tabla-Ga", "Tabla-Te", "Tabla-Na", "Tabla-Trkt", "Tabla-Tun", 
            "Tsuzumi 1", "Tsuzumi 2", "Tsuzumi 3", "Hyosigi", "Gender 1", "Gender 2", 
            "Sanza 1", "Sanza 2", "Barafon 1", "Barafon 2", "Barafon 3", "Barafon 4", 
            "Sitar 1", "Sitar 2", "Sitar 3", "Santur 1", "Santur 2", "Santur 3", 
            "Koto 1", "Koto 2", "Koto 3", "Koto 4", "Koto 5", "Koto 6", "Koto 7", "Koto 8", "Koto Tremo", 
            "Sicu 1", "Sicu 2", "Shanai 1", "Shanai 2", "Shanai 3"
            },
            {
            // SN-U110-04 - Electric Grand and Clavi
            "Electric Grand 1", "Electric Grand 2", "Electric Grand 3", "Electric Grand 4", 
            "Electric Grand 5", "Electric Grand 6", "Electric Grand 7", "Electric Grand 8", 
            "Clavichord 1", "Clavichord 2", "Clavichord 3", "Clavichord 4", 
            },
            {
            // SN-U110-05 - Orchestral Strings
            "Violin 1", "Violin 2", "Violin 3",
            "Cello 1", "Cello 2", "Cello 3", "Cello 4", "Cello / Violin", "Contrabass / Cello", "Pizzicato", 
            "Harp 1", "Harp 2", 
            },
            {
            // SN-U110-06 - Orchestral Winds
            "Oboe 1", "Oboe 2", "Oboe 3", "Oboe 4", "Oboe 5", "Oboe 6", 
            "Bassoon 1", "Bassoon 2", "Bassoon 3", "Bassoon 4", "Bassoon 5", 
            "Clarinet 1", "Clarinet 2", "Clarinet 3", "Clarinet 4", "Clarinet 5", "Clarinet 6", 
            "Bass Clarinet 1", "Bass Clarinet 2", "Bass Clarinet 3", "Bass Clarinet 4", "Bass Clarinet 5", 
            "French Horn 1", "French Horn 2", "French Horn 3", "French Horn 4", "French Horn 5", "French Horn 6", 
            "Tuba 1", "Tuba 2", "Tuba 3", "Tuba 4", "Tuba 5", 
            "Timpani 1", "Timpani 2"
            },
            {
            // SN-U110-07 - Electric Guitar
            "Jazz Guitar SW 1", "Jazz Guitar SW 2", "Jazz Guitar SW 3", "Jazz Guitar P", "Jazz Guitar F", 
            "Jazz Guitar DT P", "Jazz Guitar DT F", "Jazz Guitar OCT P1", "Jazz Guitar OCT P2", "Jazz Guitar OCT F1", 
            "Jazz Guitar OCT F2", "Jazz Guitar SW S/F", "Jazz Guitar COMP 1", "Jazz Guitar COMP 1", "Jazz Guitar COMP 1", 
            "Overdrive Guitar SW 1", "Overdrive Guitar SW 2", "Overdrive Guitar SW 3", "Overdrive Guitar SW 4", 
            "Overdrive Guitar SW 5", "Overdrive Guitar SW HM", "Overdrive Guitar P", "Overdrive Guitar F", 
            "Overdrive Guitar DT P", "Overdrive Guitar DT F", "Overdrive Guitar OCT P1", "Overdrive Guitar OCT P2", 
            "Overdrive Guitar OCT F1", "Overdrive Guitar OCT F2", "Overdrive Guitar SW S/F", "Overdrive Guitar FB 1", 
            "Overdrive Guitar FB 2", "Overdrive Guitar FB 3", "Overdrive Guitar FB 4", "Overdrive Guitar FB 5", 
            "Overdrive Guitar FB 6", "Overdrive Guitar FB 7", "Overdrive Guitar FB 8", "Overdrive Guitar FB 9", 
            "Overdrive Guitar FB 10", "Overdrive Guitar FB 11", "Overdrive Guitar FB 12", 
            "Distortion Guitar SW 1", "Distortion Guitar SW 2", "Distortion Guitar SW 3", "Distortion Guitar SW 4", 
            "Distortion Guitar SW 5", "Distortion Guitar SW HM", "Distortion Guitar P", "Distortion Guitar F", 
            "Distortion Guitar DT", "Distortion Guitar +4TH 1", "Distortion Guitar +4TH 2", "Distortion Guitar -5TH 1", 
            "Distortion Guitar -5TH 2", "Distortion Guitar OCT 1", "Distortion Guitar OCT 2", "Distortion Guitar SW S/F", 
            "Distortion Guitar FB 1", "Distortion Guitar FB 2", "Distortion Guitar FB 3", "Distortion Guitar FB 4", 
            "Distortion Guitar FB 5", "Distortion Guitar FB 6", "Distortion Guitar FB 7", "Distortion Guitar FB 8", 
            "Distortion Guitar FB 9", "Distortion Guitar FB 10", "Distortion Guitar FB 11", "Distortion Guitar FB 12", 
            "Picking Harmonics"
            },
            {
            // SN-U110-08 - Synthesizer
            // NOTE -- already available on the U-220 internal
            "Fantasia", "Bell Pad", "Syn Choir", "Breath Vox", "L. Calliope", "Calliope", 
            "Metal Hit", "Rich Brass", "Brass Strings", "String Pad 1", "String Pad 2", 
            "Pizzagogo", "Fanta Bell", "Spect Bell", "Bell Drum", "Synth Harp", 
            "Pulse Wave 1", "Pulse Wave 2", "Pulse Wave 3", "Saw Wave 1", "Saw Wave 2", 
            "Pizz", "Metal", "Breath", "Nails", "Spectrum 1", "Spectrum 2", "N. Dance"
            },
            {
            // SN-U110-09 - Guitar & Keyboards
            // NOTE -- already available on the U-220 internal
            "Bright EP 1", "Bright EP 2", "Syn. Vox 1", "Syn. Vox 2", 
            "Syn. Bass 4", "Syn. Bass 5", "Syn. Bass 6", "Syn. Bass 7", "Syn. Bass 8", 
            "Heavy EG 1", "Heavy EG 2", "JP. Strings", "JP. Brass 1", "JP. Brass 2", 
            "R. Organ 1", "R. Organ 2"
            },
            {
            // SN-U110-10 - Rock Drums
            "Rock Drums [SETUP]",
            "Electronic Drums [SETUP]"
            },
            {
            // SN-U110-11 - Sound Effects
            "Creaking", "Door", "Footsteps", "Waterphone", "S-Strings", 
            "Screaming", "Laughing", "Dog", "Wave", "Stream", "Bird", 
            "Drop", "Rain", "Thunder", "Car Door", "Car Stop", "Car Crash", 
            "Train", "Pistol", "Machine Gun", "Missile", "Explosion", 
            "Big Foot", "Godzilla", "Telephone Call", "Chime", "Applause", 
            "From Radio", "Bubble 1", "Bubble 2", "Toy", "Fantasy Hit", 
            "S-Set", "C-Set",
            },
            {
            // SN-U110-12 - Sax and Trombone
            // NOTE -- The tones labelled Trumpet/Trombone were originally labelled "TP/TRB",
            // which I *assume* is Trumpet and Trombone
            "Saxophone SW 1", "Saxophone SW 2", "Saxophone SW 3", "Saxophone SW 4", "Saxophone P 1", 
            "Saxophone P 2", "Saxophone P 3", "Saxophone MF 1", "Saxophone MF 2", "Saxophone FF", 
            "Trombone SW 1", "Trombone SW 2", "Trombone P", "Trombone MF", "Trombone FF", 
            "Trumpet/Trombone SW 1", "Trumpet/Trombone SW 2", "Trumpet/Trombone P", "Trumpet/Trombone MF", "Trumpet/Trombone FF",
            },
            {
            // SN-U110-13 - Super Strings (From JV-80)
            "Super Strings 1", "Super Strings 1L", "Super Strings 1R", 
            "Super Strings 2", "Super Strings 2L", "Super Strings 2R", 
            "Super Strings 3", "Super Strings 3L", "Super Strings 3R", 
            "Super Strings 4", "Super Strings 4L", "Super Strings 4R",
            },
            {
            // SN-U110-14 - Super Acoustic Guitar (From JV-80)
            "Steel Guitar 1", "Steel Soft", "Steel Hard", "Steel Guitar 2", "Steel (L)", "Steel (R)", 
            "Nylon Guitar 1", "Nylon Soft", "Nylon Hard", "Nylon Guitar 2", "Nylon (L)", "Nylon (R)", 
            "12-String Guitar 1", "12-String Guitar 2", "12-String Guitar 3", "12-String Guitar 4", "12-String Guitar 5", 
            "Harmonics", "Squeak",
            },
            {
            // SN-U110-15 - Super Brass (From JV-80)
            "High Brass 1", "High Brass 2", "High Brass SF", 
            "Low Brass 1", "Low Brass 2", "Low Brass SF", 
            "Brass Combo 1", "Brass Combo 1L", "Brass Combo 1R", 
            "Brass Combo 2", "Brass Combo 2L", "Brass Combo 2R", 
            "Brass Combo SF",
            },
        };
        
    public static final int[] POLY_MOD_RANGES = { -24, -12, -7, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 7, 12 }; 
    public static final String[] NOTES = new String[] { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
        
    public static final String[] OUTPUT_MODES = 
        {
        "1.   1:31",
        "2.   1:27  2:4",
        "3.   1:23  2:8",
        "4.   1:23  2:4  3:4",
        "5.   1:19  2:12",
        "6.   1:19  2:8  3:4",
        "7.   1:19  2:4  3:4  4:4",
        "8.   1:15  2:16",
        "9.   1:15  2:12  3:4",
        "10.   1:15  2:8  3:8",                                                                 //// Documentation is wrong.  Thanks to Istvan Kadar on facebook.
        "11.   1:15  2:8  3:4  4:4",
        "12.   1:15  2:4  3:4  4:4  5:4",
        "13.   1:11  2:12  3:8",
        "14.   1:11  2:12  3:4  4:4",
        "15.   1:11  2:8  3:8  4:4",
        "16.   1:11  2:8  3:4  4:4  5:4",
        "17.   1:11  2:4  3:4  4:4  5:4  6:4",
        "18.   1:7  2:8  3:8  4:8",
        "19.   1:7  2:8  3:8  4:4  5:4",
        "20.   1:7  2:8  3:4  4:4  5:4  6:4",
        "21.   1-2:S31",
        "22.   1-2:M31",
        "23.   1-2:S16  3:15",
        "24.   1-2:M16  3:15",
        "25.   1-2:S16  3:11  4:4",
        "26.   1-2:M16  3:11  4:4",
        "27.   1-2:S16  3:7  4:8",
        "28.   1-2:M16  3:7  4:8",
        "29.   1-2:S16  3:7  4:4  5:4",
        "30.   1-2:M16  3:7  4:4  5:4",
        "31.   1-2:S16  3:3  4:4  5:4  6:4",
        "32.   1-2:M16  3:3  4:4  5:4  6:4",
        "33.   1-2:S8  3:23",
        "34.   1-2:M8  3:23",
        "35.   1-2:S8  3:19  4:4",
        "36.   1-2:M8  3:19  4:4",
        "37.   1-2:S8  3:15  4:8",
        "38.   1-2:M8  3:15  4:8",
        "39.   1-2:S8  3:15  4:4  5:4",
        "40.   1-2:M8  3:15  4:4  5:4",
        "41.   1-2:S8  3:11  4:12",
        "42.   1-2:M8  3:11  4:12",
        "43.   1-2:S8  3:11  4:8  5:4",
        "44.   1-2:M8  3:11  4:8  5:4",
        "45.   1-2:S8  3:11  4:4  5:4  6:4",
        "46.   1-2:M8  3:11  4:4  5:4  6:4",
        "47.   1-2:S8  3:7  4:8  5:8",
        "48.   1-2:M8  3:7  4:8  5:8",
        "49.   1-2:S8  3:7  4:8  5:4  6:4",
        "50.   1-2:M8  3:7  4:8  5:4  6:4"
        };

    static HashMap allParametersToIndex = null;
        
    // This just contains the unique parameters, not the individal part parameters 
    final static String[] allParameters = new String[]
    {
    "outputmode",
    "chorusrate",
    "chorusdepth",
    "tremolorate",
    "tremolodepth",
    
    "outputassign",
    "receivechannel",
    "tonemedia",
    "tonenumber",
    "bendrange",
    "keyrangelo",
    "keyrangehi",
    "partlevel",
    "velocitysens",
    "levelpresssens",
    "envattackrate",
    "envreleaserate",
    "pitchshiftcoarse",
    "pitchshiftfine",
    "lforate",
    "lfoautodelaytime",
    "lfoautorisetime",
    "lfoautodepth",
    "lfomanrisetime",
    "lfomandepth",
    "lfochpresssens",
    "pgmchange",
    "pgmchangemap",
    "detunedepth",
    "pitchpolypresssens",
    "lfopolypresssens",
    };

    public RolandU110()
        {
        if (allParametersToIndex == null)
            {
            allParametersToIndex = new HashMap();
            for(int i = 0; i < allParameters.length; i++)
                {
                allParametersToIndex.put(allParameters[i], Integer.valueOf(i));
                }
            }

        JComponent panel = new SynthPanel(this);
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        hbox.addLast(addGeneral(Style.COLOR_C()));
        vbox.add(hbox);

        vbox.add(addPart(1, Style.COLOR_A()));
        vbox.add(addPart(2, Style.COLOR_B()));
        panel.add(vbox, BorderLayout.CENTER);
        addTab("General, Parts 1-2", panel);                

        vbox = new VBox();
        panel = new SynthPanel(this);
        vbox.add(addPart(3, Style.COLOR_A()));
        vbox.add(addPart(4, Style.COLOR_B()));
        panel.add(vbox, BorderLayout.CENTER);
        addTab("Parts 3-4", panel);                

        vbox = new VBox();
        panel = new SynthPanel(this);
        vbox.add(addPart(5, Style.COLOR_A()));
        vbox.add(addPart(6, Style.COLOR_B()));
        panel.add(vbox, BorderLayout.CENTER);
        addTab("Parts 5-6", panel);                

        model.set("name", "Init Patch");  // has to be 10 long
        model.set("number", 0);
        loadDefaults();        
        }
                
                
    public String getDefaultResourceFileName() { return "RolandU110.init"; }
    public String getHTMLResourceFileName() { return "RolandU110.html"; }

    // There are no banks
    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        int original = model.get("number");
                
        JTextField number = new SelectedTextField("" + (original + 1), 3);

        while(true)
            {
            boolean result = showMultiOption(this, new String[] { "Patch Number"}, 
                new JComponent[] { number }, title, "Enter Patch Number.");
                
            if (result == false)
                return false;
                                
            int n;
            try { n = Integer.parseInt(number.getText()); }
            catch (NumberFormatException e)
                {
                showSimpleError(title, "The Patch Number must be an integer 1...64");
                continue;
                }
            if (n < 1 || n > 64)
                {
                showSimpleError(title, "The Patch Number must be an integer 1...64");
                continue;
                }
                
            n--;
            change.set("number", n);
            return true;
            }
        }

    /** Add the global patch category (name, id, number, etc.) */
    public JComponent addNameGlobal(Color color)
        {
        Category globalCategory = new Category(this, getSynthName(), color);
        //globalCategory.makeUnresettable();
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
                
        VBox vbox = new VBox();
        HBox hbox2 = new HBox();
        comp = new PatchDisplay(this, 9);
        hbox2.add(comp);
        vbox.add(hbox2);
        
        comp = new StringComponent("Patch Name", this, "name", MAXIMUM_NAME_LENGTH, "Name must be up to 10 ASCII characters.")
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

        hbox.add(Strut.makeHorizontalStrut(100));

        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }


    public JComponent addGeneral(Color color)
        {
        Category category = new Category(this, "General", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        params = OUTPUT_MODES;
        comp = new Chooser("Output Mode", this, "outputmode", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Chorus", this, "chorusrate", color, 0, 15);
        ((LabelledDial)comp).addAdditionalLabel("Rate");
        hbox.add(comp);
        
        comp = new LabelledDial("Chorus", this, "chorusdepth", color, 0, 15);
        ((LabelledDial)comp).addAdditionalLabel("Depth");
        hbox.add(comp);
        
        comp = new LabelledDial("Tremolo", this, "tremolorate", color, 0, 15);
        ((LabelledDial)comp).addAdditionalLabel("Rate");
        hbox.add(comp);
        
        comp = new LabelledDial("Tremolo", this, "tremolodepth", color, 0, 15);
        ((LabelledDial)comp).addAdditionalLabel("Depth");
        hbox.add(comp);
        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }



    public JComponent addPart(final int part, Color color)
        {
        Category category = new Category(this, "Part " + part, color);
        category.makePasteable("part");
                
        JComponent comp;
        String[] params;
        HBox left = new HBox();
        HBox hbox = new HBox();
        VBox outer = new VBox();
        
        params = PCM[0];
        Chooser pcm = new Chooser("Tone", this, "part" + part + "tonenumber", params);
        
        VBox vbox = new VBox();
        params = PCM_NAMES;
        comp = new Chooser("Tone Medium", this, "part" + part + "tonemedia", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                int val = model.get(key, 0);
                int cur = model.get("part" + part + "tonenumber", 0);
                pcm.setElements(PCM[val]);
                if (cur >= PCM[val].length)
                    model.set("part" + part + "tonenumber", 0);
                }
            };
        vbox.add(comp);
        
        vbox.add(pcm);

        comp = new CheckBox("Program Change", this, "part" + part + "pgmchange");
        vbox.add(comp);

        left.add(vbox);
  
        comp = new LabelledDial("Output", this, "part" + part + "outputassign", color, 0, 6)
            {
            public String map(int val)
                {
                if (val == 6) return "Off";
                else return "" + (val + 1);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Assign");
        hbox.add(comp);
        
        comp = new LabelledDial("Receive", this, "part" + part + "receivechannel", color, 0, 15)
            {
            public String map(int val)
                {
                return "" + (val + 1);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Channel");
        hbox.add(comp);

        comp = new LabelledDial("Program", this, "part" + part + "pgmchangemap", color, 0, 5)
            {
            public String map(int val)
                {
                return "" + (val + 1);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Tone Map");
        hbox.add(comp);

        comp = new LabelledDial("Level", this, "part" + part + "partlevel", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Level", this, "part" + part + "velocitysens", color, 0, 15);
        ((LabelledDial)comp).addAdditionalLabel("Vel Sens");
        hbox.add(comp);
        
        comp = new LabelledDial("Level Ch", this, "part" + part + "levelpresssens", color, 0, 15);
        ((LabelledDial)comp).addAdditionalLabel("Press Sens");
        hbox.add(comp);
        

        comp = new LabelledDial("Attack", this, "part" + part + "envattackrate", color, 1, 15)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val)
                {
                return "" + (val - 8);
                }
            };
        hbox.add(comp);
        
        comp = new LabelledDial("Release", this, "part" + part + "envreleaserate", color, 1, 15)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val)
                {
                return "" + (val - 8);
                }
            };
        hbox.add(comp);
        
        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "part" + part + "envattackrate", null, "part" + part + "envreleaserate" },
            new String[] { null, null, null, null },
            new double[] { 0, 0.3333 / 14, 0.3333, 0.3333 / 14 },
            new double[] { 0, 1.0, 1.0, 0})
            {
            public void postProcess(double[] xVals, double[] yVals)
                {
                xVals[1] -= 0.3333 / 14;
                xVals[3] -= 0.3333 / 14;

                // attack, decay, and release are BACKWARDS -- lower values are SLOWER
                xVals[1] = 0.3333 - xVals[1];
                xVals[3] = 0.3333 - xVals[3];
                }
            };
        hbox.addLast(comp);
            
        outer.add(hbox);
        hbox = new HBox();





        comp = new LabelledDial("Bend", this, "part" + part + "bendrange", color, 0, 12);
        ((LabelledDial)comp).addAdditionalLabel("Range");
        hbox.add(comp);

        comp = new LabelledDial("Key Range", this, "part" + part + "keyrangelo", color, 0, 127)
            {
            public String map(int value)
                {
                return NOTES[value % 12] + (value / 12 - 1);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Low");
        hbox.add(comp);




        comp = new LabelledDial("Key Range", this, "part" + part + "keyrangehi", color, 0, 127)
            {
            public String map(int value)
                {
                return NOTES[value % 12] + (value / 12 - 1);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("High");
        hbox.add(comp);
        
        comp = new LabelledDial("Pitch Shift", this, "part" + part + "pitchshiftcoarse", color, 52, 76)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val)
                {
                return "" + (val - 64);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Coarse");
        hbox.add(comp);
        
        comp = new LabelledDial("Pitch Shift", this, "part" + part + "pitchshiftfine", color, 14, 114)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val)
                {
                return "" + (val - 64);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Fine");
        hbox.add(comp);

        comp = new LabelledDial("Detune", this, "part" + part + "detunedepth", color, 0, 15);
        ((LabelledDial)comp).addAdditionalLabel("Depth");
        hbox.add(comp);

        comp = new LabelledDial("Pitch Poly", this, "part" + part + "pitchpolypresssens", color, 0, 15)
            {
            public String map(int val)
                {
                return "" + POLY_MOD_RANGES[val];
                }
            public int getDefaultValue() { return 8; }
            public double getStartAngle() { return 237; }
            };
        ((LabelledDial)comp).addAdditionalLabel("Press Sens");
        hbox.add(comp);

        comp = new LabelledDial("LFO", this, "part" + part + "lfochpresssens", color, 0, 15);
        ((LabelledDial)comp).addAdditionalLabel("Press Sens");
        hbox.add(comp);

        comp = new LabelledDial("LFO Poly", this, "part" + part + "lfopolypresssens", color, 0, 7);
        ((LabelledDial)comp).addAdditionalLabel("Press Sens");
        hbox.add(comp);

        outer.add(hbox);
        hbox = new HBox();

        comp = new LabelledDial("LFO Rate", this, "part" + part + "lforate", color, 0, 15);
        hbox.add(comp);

        comp = new LabelledDial("LFO Manual", this, "part" + part + "lfomanrisetime", color, 0, 15);
        ((LabelledDial)comp).addAdditionalLabel("Rise Time");
        hbox.add(comp);

        comp = new LabelledDial("LFO Manual", this, "part" + part + "lfomandepth", color, 0, 15);
        ((LabelledDial)comp).addAdditionalLabel("Depth");
        hbox.add(comp);

        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "part" + part + "lfomanrisetime", null },
            new String[] { null, "part" + part + "lfomandepth", "part" + part + "lfomandepth" },
            new double[] { 0, 0.5 / 15, 0.5 },
            new double[] { 0, 1.0 / 15, 1.0 / 15});
        //((EnvelopeDisplay)comp).setAxis(0.5);
        hbox.add(comp);
                    
        comp = new LabelledDial("LFO Auto", this, "part" + part + "lfoautodelaytime", color, 0, 15);
        ((LabelledDial)comp).addAdditionalLabel("Delay Time");
        hbox.add(comp);

        comp = new LabelledDial("LFO Auto", this, "part" + part + "lfoautorisetime", color, 0, 15);
        ((LabelledDial)comp).addAdditionalLabel("Rise Time");
        hbox.add(comp);

        comp = new LabelledDial("LFO Auto", this, "part" + part + "lfoautodepth", color, 0, 15);
        ((LabelledDial)comp).addAdditionalLabel("Depth");
        hbox.add(comp);

        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "part" + part + "lfoautodelaytime", "part" + part + "lfoautorisetime", null },
            new String[] { null, null, "part" + part + "lfoautodepth", "part" + part + "lfoautodepth" },
            new double[] { 0, 0.3333 / 15, 0.3333 / 15, 0.3333 },
            new double[] { 0, 0.0, 1.0 / 15, 1.0 / 15});
        //((EnvelopeDisplay)comp).setAxis(0.5);
        hbox.addLast(comp);
            

        outer.add(hbox);  
        left.addLast(outer);      
        category.add(left, BorderLayout.CENTER);
        return category;
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
    
    public byte[] emit(String key)
        {
        if (key.equals("number")) return new byte[0];  // this is not emittable
        
        if (key.equals("name"))
            {
            byte[] data = new byte[30];
            data[0] = (byte) 0xF0;
            data[1] = (byte) 0x41;
            data[2] = (byte) (byte)getChannelOut();
            data[3] = (byte) 0x23;              // U-110
            data[4] = (byte) 0x12;
            data[5] = (byte) 0x00;
            data[6] = (byte) 0x01;
            data[7] = (byte) 0x00;
                
            char[] name = (model.get("name", "") + "          ").toCharArray();
            for(int i = 0; i < 10; i++)
                {
                data[i * 2 + 8]= (byte)(name[i] & 15);
                data[i * 2 + 8 + 1] = (byte)((name[i] >>> 4) & 15);
                }
            data[data.length - 2] = produceChecksum(data, 5, data.length - 2);
            data[data.length - 1] = (byte)0xF7;
            return data;
            }
        else if (key.startsWith("part"))
            {
            int part = StringUtility.getFirstInt(key);
            String param = key.substring(5);        // remove partN
                
            byte AA = (byte)(0x00);
            byte BB = (byte)(0x10 + (part - 1));
            byte CC = (byte)(((Integer)(allParametersToIndex.get(param))).intValue() - 5);
            byte val = (byte)(model.get(key));
                
            byte checksum = produceChecksum(new byte[] { AA, BB, CC, val });
            return new byte[] { (byte)0xF0, 0x41, (byte)getChannelOut(), 0x23, 0x12, AA, BB, CC, val, checksum, (byte)0xF7 };
            }
        else
            {
            int pos = ((Integer)(allParametersToIndex.get(key))).intValue() + 24;
            
            byte AA = (byte)(0x00);
            byte BB = (byte)(0x01);
            byte CC = (byte)(pos);
            byte val = (byte)(model.get(key));
                
            byte checksum = produceChecksum(new byte[] { AA, BB, CC, val });
            return new byte[] { (byte)0xF0, 0x41, (byte)getChannelOut(), 0x23, 0x12, AA, BB, CC, val, checksum, (byte)0xF7 };
            }
        }

   
    /** If the user is editing the patch on the synth, the U-110 won't change patches!
        So just in case we send this. */
    public boolean getSendsParametersAfterNonMergeParse() { return true; }

    
    byte[] parseData = null;
    
    public int parse(byte[] data, boolean fromFile)
        {
        if (numSysexMessages(data) > 1)
            {
            int result = PARSE_FAILED;
            byte[][] d = cutUpSysex(data);
            for(int i = 0; i < d.length; i++)
                {
                result = parse(d[i], fromFile);
                if (result != PARSE_INCOMPLETE) break;
                }
            return result;
            }
                
        // What is the patch number?
        int AA = data[5];
        int BB = data[6];
        int CC = data[7];

        int parseDataPosition = (AA == 0x01 ? (BB == 0x01 ? 0 : 128) : (BB % 2 == 0 ? 0 : 128));

        if (parseDataPosition == 0)
            {
            parseData = new byte[256];
            for(int x = 0; x < parseData.length; x++)
                parseData[x] = 0;
            }
        else if (parseData == null) 
            return PARSE_FAILED;                            // bad initial data

        System.arraycopy(data, 8, parseData, parseDataPosition, Math.min(parseData.length - parseDataPosition, 128));

        if (parseDataPosition + 128 >= 232)    // last position, may only go to 232, not sure
            {
            // are we offset?
        
            if (AA == 0x02)         // Write to Patch Banks
                {
                model.set("number", BB / 2);
                }
        
            // The U-110 is entirely byte-packed :-(  So we have to do this by hand.

            int pos = 0;

            pos += 8;       // padding
                
            String name = "";
            for(int i = 0; i < 10; i++)
                {
                int lsb = parseData[pos++];
                int msb = parseData[pos++];
                name = name + (char)(lsb | (msb << 4));
                }
            model.set("name", name);

            int lsb = parseData[pos++];
            int msb = parseData[pos++];
            int val = lsb | (msb << 4);
            model.set("outputmode", val & 63);

            lsb = parseData[pos++];
            msb = parseData[pos++];
            val = lsb | (msb << 4);
            model.set("chorusrate", val & 15);

            lsb = parseData[pos++];
            msb = parseData[pos++];
            val = lsb | (msb << 4);
            model.set("chorusdepth", val & 15);

            lsb = parseData[pos++];
            msb = parseData[pos++];
            val = lsb | (msb << 4);
            model.set("tremolorate", val & 15);

            lsb = parseData[pos++];
            msb = parseData[pos++];
            val = lsb | (msb << 4);
            model.set("tremolodepth", val & 15);

            pos += 2;       // padding
                
            for(int i = 1 ; i <= 6; i++)
                {
                lsb = parseData[pos++];
                msb = parseData[pos++];
                val = lsb | (msb << 4);
                model.set("part" + i + "tonemedia", val & 31);
                model.set("part" + i + "lfopolypresssens", (val >>> 5) & 7);

                lsb = parseData[pos++];
                msb = parseData[pos++];
                val = lsb | (msb << 4);
                model.set("part" + i + "tonenumber", val & 127);

                lsb = parseData[pos++];
                msb = parseData[pos++];
                val = lsb | (msb << 4);
                model.set("part" + i + "receivechannel", val & 15);
                model.set("part" + i + "bendrange", (val >>> 4) & 15);

                lsb = parseData[pos++];
                msb = parseData[pos++];
                val = lsb | (msb << 4);
                model.set("part" + i + "keyrangelo", val);

                lsb = parseData[pos++];
                msb = parseData[pos++];
                val = lsb | (msb << 4);
                model.set("part" + i + "keyrangehi", val);

                lsb = parseData[pos++];
                msb = parseData[pos++];
                val = lsb | (msb << 4);
                model.set("part" + i + "partlevel", val);

                lsb = parseData[pos++];
                msb = parseData[pos++];
                val = lsb | (msb << 4);
                model.set("part" + i + "velocitysens", val & 15);
                model.set("part" + i + "levelpresssens", (val >>> 4) & 15);

                lsb = parseData[pos++];
                msb = parseData[pos++];
                val = lsb | (msb << 4);
                model.set("part" + i + "envattackrate", val & 15);
                model.set("part" + i + "envreleaserate", (val >>> 4) & 15);

                lsb = parseData[pos++];
                msb = parseData[pos++];
                val = lsb | (msb << 4);
                model.set("part" + i + "pitchshiftcoarse", val);

                lsb = parseData[pos++];
                msb = parseData[pos++];
                val = lsb | (msb << 4);
                model.set("part" + i + "pitchshiftfine", val);

                lsb = parseData[pos++];
                msb = parseData[pos++];
                val = lsb | (msb << 4);
                model.set("part" + i + "detunedepth", val & 15);
                model.set("part" + i + "pitchpolypresssens", (val >>> 4) & 15);

                lsb = parseData[pos++];
                msb = parseData[pos++];
                val = lsb | (msb << 4);
                model.set("part" + i + "pgmchangemap", val & 7);
                model.set("part" + i + "pgmchange", (val >>> 3) & 1);
                model.set("part" + i + "outputassign", (val >>> 5) & 7);

                lsb = parseData[pos++];
                msb = parseData[pos++];
                val = lsb | (msb << 4);
                model.set("part" + i + "lforate", val & 15);

                lsb = parseData[pos++];
                msb = parseData[pos++];
                val = lsb | (msb << 4);
                model.set("part" + i + "lfoautodepth", val & 15);
                model.set("part" + i + "lfomandepth", (val >>> 4) & 15);

                lsb = parseData[pos++];
                msb = parseData[pos++];
                val = lsb | (msb << 4);
                model.set("part" + i + "lfoautorisetime", val & 15);
                model.set("part" + i + "lfomanrisetime", (val >>> 4) & 15);

                lsb = parseData[pos++];
                msb = parseData[pos++];
                val = lsb | (msb << 4);
                model.set("part" + i + "lfochpresssens", val & 15);
                model.set("part" + i + "lfoautodelaytime", (val >>> 4) & 15);
                }

            revise();
            parseData = null;
            return PARSE_SUCCEEDED;
            }
        else return PARSE_INCOMPLETE;
        }
    

    public Object[] emitAll(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {             
        if (tempModel == null)
            tempModel = getModel();

        // The U-110 is entirely byte-packed :-(  So we have to do this by hand.
                
        byte[] buf = new byte[256];

        int pos = 0;
        
        pos += 8;       // padding
                
        String name = model.get("name", "Untitled") + "          ";
        for(int i = 0; i < 10; i++)
            {
            char c = name.charAt(i);
            buf[pos++] = (byte)(c & 15);
            buf[pos++] = (byte)((c >>> 4) & 15);
            }
        
        int d = 
            (model.get("outputmode") << 0x0);
        buf[pos++] = (byte)(d & 15);
        buf[pos++] = (byte)((d >>> 4) & 15);
        
        d = 
            (model.get("chorusrate") << 0);
        buf[pos++] = (byte)(d & 15);
        buf[pos++] = (byte)((d >>> 4) & 15);
        
        d = 
            (model.get("chorusdepth") << 0);
        buf[pos++] = (byte)(d & 15);
        buf[pos++] = (byte)((d >>> 4) & 15);
        
        d = 
            (model.get("tremolorate") << 0);
        buf[pos++] = (byte)(d & 15);
        buf[pos++] = (byte)((d >>> 4) & 15);

        d = 
            (model.get("tremolodepth") << 0);
        buf[pos++] = (byte)(d & 15);
        buf[pos++] = (byte)((d >>> 4) & 15);

        pos += 2;       // padding
                

        for(int i = 1 ; i <= 6; i++)
            {
            d = 
                (model.get("part" + i + "tonemedia") << 0) |                    // 5 bit
                (model.get("part" + i + "lfopolypresssens") << 5);              // 3 bit
            buf[pos++] = (byte)(d & 15);
            buf[pos++] = (byte)((d >>> 4) & 15);

            d = 
                (model.get("part" + i + "tonenumber") << 0x0);
            buf[pos++] = (byte)(d & 15);
            buf[pos++] = (byte)((d >>> 4) & 15);
        
            d = 
                (model.get("part" + i + "receivechannel") << 0) |
                (model.get("part" + i + "bendrange") << 4);
            buf[pos++] = (byte)(d & 15);
            buf[pos++] = (byte)((d >>> 4) & 15);

            d = 
                (model.get("part" + i + "keyrangelo") << 0x0);
            buf[pos++] = (byte)(d & 15);
            buf[pos++] = (byte)((d >>> 4) & 15);

            d = 
                (model.get("part" + i + "keyrangehi") << 0x0);
            buf[pos++] = (byte)(d & 15);
            buf[pos++] = (byte)((d >>> 4) & 15);

            d = 
                (model.get("part" + i + "partlevel") << 0x0);
            buf[pos++] = (byte)(d & 15);
            buf[pos++] = (byte)((d >>> 4) & 15);
        
            d = 
                (model.get("part" + i + "velocitysens") << 0) |
                (model.get("part" + i + "levelpresssens") << 4);
            buf[pos++] = (byte)(d & 15);
            buf[pos++] = (byte)((d >>> 4) & 15);
                        
            d = 
                (model.get("part" + i + "envattackrate") << 0) |
                (model.get("part" + i + "envreleaserate") << 4);
            buf[pos++] = (byte)(d & 15);
            buf[pos++] = (byte)((d >>> 4) & 15);

            d = 
                (model.get("part" + i + "pitchshiftcoarse") << 0x0);
            buf[pos++] = (byte)(d & 15);
            buf[pos++] = (byte)((d >>> 4) & 15);
        
            d = 
                (model.get("part" + i + "pitchshiftfine") << 0x0);
            buf[pos++] = (byte)(d & 15);
            buf[pos++] = (byte)((d >>> 4) & 15);

            d = 
                (model.get("part" + i + "detunedepth") << 0) |
                (model.get("part" + i + "pitchpolypresssens") << 4);
            buf[pos++] = (byte)(d & 15);
            buf[pos++] = (byte)((d >>> 4) & 15);

            d = 
                (model.get("part" + i + "pgmchangemap") << 0) |                 // 3 bits
                (model.get("part" + i + "pgmchange") << 3) |                    // 1 bit
                (model.get("part" + i + "outputassign") << 5);                  // 3 bits
            buf[pos++] = (byte)(d & 15);
            buf[pos++] = (byte)((d >>> 4) & 15);

            d = 
                (model.get("part" + i + "lforate") << 0x0);
            buf[pos++] = (byte)(d & 15);
            buf[pos++] = (byte)((d >>> 4) & 15);

            d = 
                (model.get("part" + i + "lfoautodepth") << 0) |
                (model.get("part" + i + "lfomandepth") << 4);
            buf[pos++] = (byte)(d & 15);
            buf[pos++] = (byte)((d >>> 4) & 15);

            d = 
                (model.get("part" + i + "lfoautorisetime") << 0) |
                (model.get("part" + i + "lfomanrisetime") << 4);
            buf[pos++] = (byte)(d & 15);
            buf[pos++] = (byte)((d >>> 4) & 15);

            d = 
                (model.get("part" + i + "lfochpresssens") << 0) |
                (model.get("part" + i + "lfoautodelaytime") << 4);
            buf[pos++] = (byte)(d & 15);
            buf[pos++] = (byte)((d >>> 4) & 15);
            }
                        
        // There should be 24 bytes remaining
        // FIXME: This appears to be gibberish but I'm not sure!  I'm assuming it's zeros
                
        int num = tempModel.get("number") * 2;
        int AA = (toWorkingMemory ? 0x01 : 0x02);
        int BB = (toWorkingMemory ? 0x01 : num );
        int CC = (toWorkingMemory ? 0x00 : 0x00 );

        int AA2 = (toWorkingMemory ? 0x01 : 0x02);
        int BB2 = (toWorkingMemory ? 0x02 : num + 1 );
        int CC2 = (toWorkingMemory ? 0x00 : 0x00 );
                
        byte[][] data = new byte[2][138];

        data[0][0] = (byte)0xF0;
        data[0][1] = (byte)0x41;
        data[0][2] = (byte)getChannelOut();
        data[0][3] = (byte)0x23;
        data[0][4] = (byte)0x12;
        data[0][5] = (byte) AA;
        data[0][6] = (byte) BB;
        data[0][7] = (byte) CC;
        System.arraycopy(buf, 0, data[0], 8, 128);
        data[0][data[0].length - 2] = produceChecksum(data[0], 5, data[0].length - 2);
        data[0][data[0].length - 1] = (byte)0xF7;

        data[1][0] = (byte)0xF0;
        data[1][1] = (byte)0x41;
        data[1][2] = (byte)getChannelOut();
        data[1][3] = (byte)0x23;
        data[1][4] = (byte)0x12;
        data[1][5] = (byte) AA2;
        data[1][6] = (byte) BB2;
        data[1][7] = (byte) CC2;
        System.arraycopy(buf, 128, data[1], 8, 128);
        data[1][data[1].length - 2] = produceChecksum(data[1], 5, data[0].length - 2);
        data[1][data[1].length - 1] = (byte)0xF7;

        return new Object[] { data[0], data[1] };
        }

    public void changePatch(Model tempModel) 
        {
        if (tempModel == null)
            tempModel = getModel();
                
        // we'll assume we got it
        model.set("number", tempModel.get("number"));
                
        tryToSendMIDI(buildPC((byte)getChannelOut(), tempModel.get("number")));
        }


/// NOTE: Request Dump does NOT WORK if the user has pressed the EDIT button.

    public byte[] requestDump(Model tempModel)
        {        
        // This doesn't appear to work at all.  :-(
        // We have to change the patch and do a current patch request
        
        /*
          if (tempModel == null)
          tempModel = getModel();

          int number = tempModel.get("number");
                
          byte AA = (byte)(0x02);
          byte BB = (byte)(number * 2);
          byte CC = (byte)0x00;
          byte LSB = (byte)0x00;
          byte MSB = (byte)0x02;          // 256 bytes I believe
                
          byte checksum = produceChecksum(new byte[] { AA, BB, CC, (byte)0x00, LSB, MSB });
          byte[] b = new byte[] { (byte)0xF0, (byte)0x41, (byte)getChannelOut(), (byte)0x23, (byte)0x11, 
          AA, BB, CC, (byte)0x00, MSB, LSB, checksum, (byte)0xF7 }; 
          return b;
        */
                
        changePatch(tempModel);
        return requestCurrentDump();
        }
  
    public byte[] requestCurrentDump()
        {
        byte AA = (byte)(0x01);
        byte BB = (byte)(0x01);
        byte CC = (byte)(0x00);
        byte LSB = (byte)0x00;
        byte MSB = (byte)0x02;          // 256 bytes I believe
                
        byte checksum = produceChecksum(new byte[] { AA, BB, CC, (byte)0x00, LSB, MSB });
        byte[] b = new byte[] { (byte)0xF0, (byte)0x41, (byte)getChannelOut(), (byte)0x23, (byte)0x11, 
            AA, BB, CC, (byte)0x00, MSB, LSB, checksum, (byte)0xF7 }; 
        return b;
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

        String nm = model.get("name", "Init");
        String newnm = revisePatchName(nm);
        if (!nm.equals(newnm))
            model.set("name", newnm);
        }
        
    public static String getSynthName() { return "Roland U-110"; }
    
    public String getPatchName(Model model) { return model.get("name", "Untitled  "); }

    public int getPauseAfterSendAllParameters() { return 100; } 
 
    public int getPauseAfterSendOneParameter() { return 25; }       // In the 1.07 firmware notes it says "at least 20ms" (http://llamamusic.com/d110/ROM_IC_Bug_Fixes.html).  In my firmware (1.10) the D-110 can handle changes thrown at it full blast, but earlier firmware (1.07) cannot.
        
    public int getPauseAfterChangePatch() { return 0; }
        
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
        
        return "P-" + (model.get("number") + 1 < 10 ? "0" : "") + ((model.get("number") + 1));
        }

    public int getBatchDownloadWaitTime() { return 300; }

    /** Default is null, which indicates that the patch editor does not support librarians.  */
    public String[] getPatchNumberNames() { return buildIntegerNames(64, 1); }

    /** Return a list of each bank, indicating which are writeable.  Default is an array, all true, the size of getBankNames(). */
    public boolean[] getWriteableBanks() { return new boolean[] { true }; }

    /** Return whether individual patches can be written.  Default is FALSE. */
    public boolean getSupportsPatchWrites() { return true; }

    /** Return the maximum number of characters a patch name may hold. The default returns 16. */
    public int getPatchNameLength() { return MAXIMUM_NAME_LENGTH; }

    /** Return true if individual (non-bank) patches on the synthesizer contain location information (bank, number). 
        This will matter when reading patches from disk, rather than loading them from the synth, so as to put them
        in the right place. */
    public boolean getPatchContainsLocation() { return true; }

    public byte[] requestBankDump(int bank) 
        { 
        return new byte[] { (byte)0xF0, 0x41, (byte)getChannelOut(), 0x23, 0x11, 0x02, 0x00, 0x00, 0x01, 0x00, 0x00, 0x7D, (byte)0xF7 }; 
        }

    public boolean librarianTested() { return true; }
    }


/**** 
      ROLAND U-110 SYSEX SPECIFICATION
      Sean Luke, sean@cs.gmu.edu, November 2022

      Roland has a specification in the back of the U-110 manual, but it is missing huge chunks,
      such as the entire format for uploaded and downloaded patches!  Pretty absurd.  (And if you
      think that the online PDF scans of the manual are impossible to read: I have an *original*
      Roland 1997 reprint manual and it's just as illegible, like a bad xerox copy -- it appears
      even Roland didn't have a copy of the *original* original).  Here I'm going to try to remedy 
      this to some degree.  Below I describe the sysex involving patch and parameter requests and 
      dumps.  Note that this just deals with patches and individual parameters, not the global 
      setup region, nor program change maps.


      REQUEST AND DUMP SYSEX FORMATS

      There are both synchronous and asynchronous data transfers: here I only discuss asynchronous.
      Seriously, why would anyone do synchronous?

      REQUEST A DATA DUMP
      F0 
      41      Roland
      DV      Device ID
      23      U-110
      11      Request Data
      LL      Address MSB     Address is of the form LLMMNN.  Remember that these are 7-bit values.
      MM      ...
      NN      Address LSB
      XX      Data Size MSB   Size is of the form XXYYZZ.   Remember that these are 7-bit values.
      YY      ...
      ZZ      Data Size LSB
      CH      Checksum                Checksum is on the ADDRESS and DATA SIZE.  See CHECKSUM below.
      F7
        
      DATA DUMP
      F0 
      41      Roland
      DV      Device ID
      23      U-110
      12      Data Dump
      LL      Address LSB     Address is of the form LLMMNN.   Remember that these are 7-bit values.
      MM      ...
      NN      Address MSB
      DATA                            DATA can be up to 128 bytes long
      CH      Checksum                Checksum is on the ADDRESS and DATA.  See CHECKSUM below.
      F7

      CHECKSUM
      The checksum applies to all messages:
      1. Add all the data in question
      2. mod by 128 (that is, & 127)
      3. Subtract from 128
      4. If the result is 128, return 0
      5. Else return the result

      NOTE ON MULTI-SYSEX-MESSAGE DUMPS
      If a dump needs to send more than 128 bytes of DATA -- which is very often the case --
      then the dump takes the form of multiple DATA DUMP messages, each with its own starting
      address.  For example, to send out the current working patch memory (located at position 010100),
      the unit would send  F0 41 DV 23 12 01 01 00 ...First 128 bytes... CH F7 followed by
      F0 41 DV 23 12 01 02 00 ..Second 128 bytes.... CH F7.




      REQUESTING AND DUMPING INDIVIDUAL PARAMETERS

      Parameters are typically written as single bytes: in this case the data would
      be one byte long and the size would be 000001.  You can write a chunk of the
      parameter address space (for the name, say) if you like.  
      
      The NAME characters are broken into 4-byte nybbles.  Thus NAME LSB contains
      the lower 4 bits of the character and the NAME MSB contains the upper, well,
      3 bits (since it's ASCII).

      To request or dump an individual parameter, the address is:

      ADDRESS ITEM
      --------------------------------------------------------------------------
      000100  Name[0] LSB
      000101  Name[0] MSB
      000102  Name[1] LSB
      000103  Name[1] MSB
      000104  Name[2] LSB
      000105  Name[2] MSB
      000106  Name[3] LSB
      000107  Name[3] MSB
      000108  Name[4] LSB
      000109  Name[4] MSB
      00010A  Name[5] LSB
      00010B  Name[5] MSB
      00010C  Name[6] LSB
      00010D  Name[6] MSB
      00010E  Name[7] LSB
      00010F  Name[7] MSB
      000110  Name[8] LSB
      000111  Name[8] MSB
      000112  Name[9] LSB
      000113  Name[9] MSB
      000113  [Empty, set to zero]
      000114  [Empty, set to zero]
      000115  [Empty, set to zero]
      000116  [Empty, set to zero]
      000117  [Empty, set to zero]
      000118  Output Mode             0-49    (see TABLE 1)
      000119  Chorus Rate             0-15    (shown as 0-15)
      00011A  Chorus Depth            0-15    (shown as 0-15)
      00011B  Tremolo Rate            0-15    (shown as 0-15)
      00011C  Tremolo Depth           0-15    (shown as 0-15)

      Then for each Part n = 0 ... 5 for Parts 1...6

      001n00  Output Assign           0-6     (shown as 1-6, OFF)
      001n01  Receive Channel         0-15    (shown as 1-16)
      001n02  Tone Media              0-31    (shown as INT, C01-C31, see TABLE 2)
      001n03  Tone Number             0-98    (shown as 1-99, see TABLE 3)
      001n04  Bend Range              0-12    (shown as 0-12)
      001n05  Key Range Lo            0-127   (shown as C-1 ... G9)
      001n06  Key Range Hi            0-127   (shown as C-1 ... G9)
      001n07  Part Level              0-127   (shown as 0-127)
      001n08  Velocity Sens           0-15    (shown as 0-15)
      001n09  Level Pressure Sens     0-15    (shown as 0-15)
      001n0A  Env Attack Rate         0-15    (shown as -7 ... +7)
      001n0B  Env Release Rate        0-15    (shown as -7 ... +7)
      001n0C  Pitch Shift Coarse      52-76   (shown as -12 ... +12)
      001n0D  Pitch Shift Fine        14-114  (shown as -50 ... +50)
      001n0E  LFO Rate                0-15    (shown as 0-15)
      001n0F  LFO Auto Delay Time     0-15    (shown as 0-15)
      001n10  LFO Auto Rise Time      0-15    (shown as 0-15)
      001n11  LFO Auto Depth          0-15    (shown as 0-15)
      001n12  LFO Man Rise Time       0-15    (shown as 0-15)
      001n13  LFO Man Depth           0-15    (shown as 0-15)
      001n14  LFO Ch Press Sens       0-7     (shown as 0-7)
      001n15  Program Change          0-1     (shown as OFF, ON)
      001n16  Program Change Map      0-5     (shown as 1-6)
      001n17  Detune Depth            0-15    (shown as 0-15)
      001n18  Pitch Poly Press Sens   0-15    (shown as [-24, -12, -7 -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 7, 12])
      001n19  LFO Poly Press Sens     0-7     (shown as 0-7)



      REQUESTING AND DUMPING PATCHES

      (This had to be entirely reverse engineered)

      To access a full patch in current working memory the address is 010100 and the
      size is 000100. 

      There are 64 patches.  To write a full patch in permanent storage the address 
      is 020000 + 128 * patchnum and the size is 000100 (assuming patches start
      at 0).   You cannot request a patch; the U-110 doesn't seem to respond to this 
      properly.  Instead you can only request all 64 patches (address 020000, size 
      010000).  To get a single patch, you will have to do a program change to load 
      the patch into current memory, then request from current memory.

      Data is bit-packed into entire 8-bit bytes.  Then each byte is then split into 
      two nybbles, which hold the LSB and the MSB of the byte respectively (LSB first). 
      The stream of these nybbles forms the sysex payload.  Finally, the sysex payload
      (256 bytes all told) is broken into two 128-byte-long chunks. These are the 
      "DATA..." chunks in two DATA DUMP sysex messages respectively.
      
      To dump a patch to current working memory, the address of the first messsage
      is 010100, and the address of the second message is 010200 (it's the next 256 
      bytes).  To dump a patch to a given patch slot, the address of the first message
      is 020000 + 128 * patchnum (assuming the first patch is number 0), and the 
      address of the second message is 020100 + 128 * patchnum (it's the next 256 bytes).

      Below are the data elements, listed with "POS", which is the position of the 
      element in the data byte array before being nybblized, and "SYSEX", which is
      the position of the LSB nybble of the byte in the final sysex messages one 
      after another.  The MSB for each byte is not listed, but it is the odd 
      numbered position immediately after the LSB.

      The value ranges for each of the items is the same as for the individual
      parameters above.

      SYSEX           POS     ITEM                                                           
      ------------------------------------------------------------------------------
      8               0       00                                                [Padding?]
      10              1       00                                                [Padding?]
      12              2       00                                                [Padding?]
      14              3       00                                                [Padding?]
      16              4       Name[0]
      18              5       Name[1]
      20              6       Name[2]
      22              7       Name[3]
      24              8       Name[4]
      26              9       Name[5]
      28              10      Name[6]
      30              11      Name[7]
      32              12      Name[8]
      34              13      Name[9]
      36              14      Output Mode                                       [TABLE 1]
      38              15      Chorus Rate
      40              16      Chorus Depth
      42              17      Tremolo Rate
      44              18      Tremolo Depth
      46              19      00                                                [Padding?]

      PART 1 (16 bytes nybblized into 32 sysex bytes)

      48              20      Tone Media (bits 0 1 2 3 4)                       [TABLE 2]
      ...                     LFO Poly Press Sens (bits 5 6 7)
      50              21      Tone Number                                       [TABLE 3]
      52              22      Receive Channel (bits 0 1 2 3)
      ...                     Bend Range (bits 4 5 6 7)
      54              23      Key Range Lo 
      56              24      Key Range Hi
      58              25      Level
      60              26      Velocity Sens (bits 0 1 2 3)
      ...                     Level Ch Press Sens (bits 4 5 6 7)
      62              27      Attack Rate (bits 0 1 2 3)
      ...                     Release Rate (bits 4 5 6 7)
      64              28      Pitch Shift Coarse
      66              29      Pitch Shift Fine
      68              30      Detune Depth (bits 0 1 2 3)
      ...                     Poly Press Sens (bits 4 5 6 7)
      70              31      Program Tone Map (bits 0 1 2)
      ...                     Program Change (bit 3)
      ...                     [Unused, set to 0] (bit 4)
      ...                     Output Assign (bits 5 6 7)
      72              32      LFO Rate (bits 0 1 2 3)
      74              33      Auto Depth (bits 0 1 2 3)
      ...                     Man Depth (bits 4 5 6 7)
      76              34      Auto Rise Time (bits 0 1 2 3)
      ...                     Man Rise Time (bits 4 5 6 7)
      78              35      LFO Channel Depth (bits 0 1 2 3)
      ...                     Auto Delay Time (bits 4 5 6 7)

      PART 2 is similar for the next 32 sysex bytes, starting at byte 80, position 36
      PART 3 is similar for the next 32 sysex bytes
      PART 4 is similar for the next 32 sysex bytes
      PART 5 is similar for the next 32 sysex bytes
      PART 6 is similar for the next 32 sysex bytes

      This totals 232 bytes so far
      
      Finally then come 24 bytes of unknown data, seemingly gibberish.

      256 bytes of data total








      TABLE 1. OUTPUT MODES
      Output modes describe how many voices are assigned to each part.
      There are six parts 1...6.  An assignment of the form A:B means
      that B voices are attached to part A.  You will also see stuff
      like 1-2:S31.  This means that parts 1 and 2 are used as a single
      stereo part, and that 31 voices are assigned to them.  Or 1-2:M31 means
      that the  parts 1 and 2 are used as a single monophonic part (that is,
      they output the same value, dunno why you'd want to do this) and 
      that 31 voices are assigned to them.

      1   1:31
      2   1:27  2:4
      3   1:23  2:8
      4   1:23  2:4  3:4
      5   1:19  2:12
      6   1:19  2:8  3:4
      7   1:19  2:4  3:4  4:4
      8   1:15  2:16
      9   1:15  2:12  3:4
      10   1:15  2:8  3:8             Note: U-110 documentation is wrong here
      11   1:15  2:8  3:4  4:4
      12   1:15  2:4  3:4  4:4  5:4
      13   1:11  2:12  3:8
      14   1:11  2:12  3:4  4:4
      15   1:11  2:8  3:8  4:4
      16   1:11  2:8  3:4  4:4  5:4
      17   1:11  2:4  3:4  4:4  5:4  6:4
      18   1:7  2:8  3:8  4:8
      19   1:7  2:8  3:8  4:4  5:4
      20   1:7  2:8  3:4  4:4  5:4  6:4
      21   1-2:S31
      22   1-2:M31
      23   1-2:S16  3:15
      24   1-2:M16  3:15
      25   1-2:S16  3:11  4:4
      26   1-2:M16  3:11  4:4
      27   1-2:S16  3:7  4:8
      28   1-2:M16  3:7  4:8
      29   1-2:S16  3:7  4:4  5:4
      30   1-2:M16  3:7  4:4  5:4
      31   1-2:S16  3:3  4:4  5:4  6:4
      32   1-2:M16  3:3  4:4  5:4  6:4
      33   1-2:S8  3:23
      34   1-2:M8  3:23
      35   1-2:S8  3:19  4:4
      36   1-2:M8  3:19  4:4
      37   1-2:S8  3:15  4:8
      38   1-2:M8  3:15  4:8
      39   1-2:S8  3:15  4:4  5:4
      40   1-2:M8  3:15  4:4  5:4
      41   1-2:S8  3:11  4:12
      42   1-2:M8  3:11  4:12
      43   1-2:S8  3:11  4:8  5:4
      44   1-2:M8  3:11  4:8  5:4
      45   1-2:S8  3:11  4:4  5:4  6:4
      46   1-2:M8  3:11  4:4  5:4  6:4
      47   1-2:S8  3:7  4:8  5:8
      48   1-2:M8  3:7  4:8  5:8
      49   1-2:S8  3:7  4:8  5:4  6:4
      50   1-2:M8  3:7  4:8  5:4  6:4"



      TABLE 2
      TONE MEDIA.  These are the values indicating the internal sounds or
      sounds from a given PCM ROM Card.  In theory there can be more than
      this but it's unlikely to ever happen now.

      0       Internal
      1       Pipe Organ & Harpsichord
      2       Latin & FX Percussion
      3       Ethnic
      4       Electric Grand and Clavi
      5       Orchestral Strings
      6       Orchestral Winds
      7       Electric Guitar
      8       Synthesizer
      9       Guitar & Keyboards
      10      Rock Drums
      11      Sound Effects
      12      Sax & Trombone
      13      Super Strings
      14      Super Ac Guitar
      15      Super Brass



      TABLE 3
      TONES.  These are the PCM waves on each card, along with a few notes.
      Tones labelled "[Drumset]" are, in fact, drumsets rather than individual
      tones.

      U-110 INTERNAL
      0       A. Piano 1
      1       A. Piano 2
      2       A. Piano 3
      3       A. Piano 4
      4       A. Piano 5
      5       A. Piano 6
      6       A. Piano 7
      7       A. Piano 8
      8       A. Piano 9
      9       A. Piano 10
      10      E. Piano 1
      11      E. Piano 2
      12      E. Piano 3
      13      E. Piano 4
      14      E. Piano 5
      15      Vib 1
      16      Vib 2
      17      Vib 3
      18      Bell 1
      19      Bell 2
      20      Bell 3
      21      Bell 4
      22      Marimba
      23      A. Guitar 1
      24      A. Guitar 2
      25      A. Guitar 3
      26      A. Guitar 4
      27      A. Guitar 5
      28      E. Guitar 1
      29      E. Guitar 2
      30      E. Guitar 3
      31      E. Guitar 4
      32      Slap 1
      33      Slap 2
      34      Slap 3
      35      Slap 4
      36      Slap 5
      37      Slap 6
      38      Slap 7
      39      Slap 8
      40      Slap 9
      41      Slap 10
      42      Slap 11
      43      Slap 12
      44      Fingered 1
      45      Fingered 2
      46      Picked 1
      47      Picked 2
      48      Fretless 1
      49      Fretless 2
      50      AC. Bass
      51      Syn. Bass 1
      52      Syn. Bass 2
      53      Syn. Bass 3
      54      Choir 1
      55      Choir 2
      56      Choir 3
      57      Choir 4
      58      Strings 1
      59      Strings 2
      60      Strings 3
      61      Strings 4
      62      E. Organ 1
      63      E. Organ 2
      64      E. Organ 3
      65      E. Organ 4
      66      E. Organ 5
      67      E. Organ 6
      68      E. Organ 7
      69      E. Organ 8
      70      E. Organ 9
      71      E. Organ 10
      72      E. Organ 11
      73      E. Organ 12
      74      E. Organ 13
      75      Soft TP 1
      76      Soft TP 2
      77      Soft TP 3
      78      TP / TRB 1
      79      TP / TRB 2
      80      TP / TRB 3
      81      TP / TRB 4
      82      TP / TRB 5
      83      TP / TRB 6
      84      Sax 1
      85      Sax 2
      86      Sax 3
      87      Sax 4
      88      Sax 5
      89      Brass 1
      90      Brass 2
      91      Brass 3
      92      Brass 4
      93      Brass 5
      94      Flute 1
      95      Flute 2
      96      Shaku 1
      97      Shaku 2
      98      Drums                           [Drumset]

      SN-U110-01 - Pipe Organ and Harpsichord
      0       Harpsichord 1
      1       Harpsichord 2
      2       Harpsichord 3
      3       Harpsichord 4
      4       Harpsichord 5
      5       Harpsichord 6
      6       Positive 1
      7       Positive 2
      8       Positive 3
      9       Positive 4
      10      Positive 5
      11      Positive 6
      12      Church 1
      13      Church 2
      14      Church 3
      15      Church 4
      16      Church 5
      17      Church 6
      18      Church 7
      19      Church 8
      20      Church Reverb            

      SN-U110-02 - Latin and FX Percussion
      0       Latin 1                         [Drumset]
      1       Latin 2                         [Drumset]
      2       Latin 3                         [Drumset]
      3       FX 1                            [Drumset]
      4       FX 2                            [Drumset]
      5       FX 3                            [Drumset]
      6       FX 4                            [Drumset]
      7       Conga 1
      8       Conga 2
      9       Conga 3
      10      Bongo
      11      Claves
      12      Timbale
      13      Tambourine
      14      Wood Block
      15      Whistle
      16      Triangle
      17      Belltree
      18      Jingle Bell
      19      Vibraslap
      20      Castanet
      21      Maracas
      22      Agogo 1
      23      Agogo 2
      24      Cuica 1
      25      Cuica 2
      26      Guiro 1
      27      Guiro 2
      28      Guiro 3
      29      Berimbau
      30      Shekele
      31      Steel Drum
      32      Log Drum
      33      Orch Hit
      34      Siren
      35      Type 1
      36      Type 2
      37      Clock
      38      Pinball
      39      Telephone
      40      Smsh Glass
      41      Rezno
      42      Eerie
      43      Ambia Jr
      44      Templ Blk
      45      Zing!
      46      Boing!
      47      Mod Zap
      48      Interface
      49      Scratch
      50      Stake
      51      Zappu

      SN-U110-03 - Ethnic
      0       Tabla
      1       Tabla-Ga
      2       Tabla-Te
      3       Tabla-Na
      4       Tabla-Trkt
      5       Tabla-Tun
      6       Tsuzumi 1
      7       Tsuzumi 2
      8       Tsuzumi 3
      9       Hyosigi
      10      Gender 1
      11      Gender 2
      12      Sanza 1
      13      Sanza 2
      14      Barafon 1
      15      Barafon 2
      16      Barafon 3
      17      Barafon 4
      18      Sitar 1
      19      Sitar 2
      20      Sitar 3
      21      Santur 1
      22      Santur 2
      23      Santur 3
      24      Koto 1
      25      Koto 2
      26      Koto 3
      27      Koto 4
      28      Koto 5
      29      Koto 6
      30      Koto 7
      31      Koto 8
      32      Koto Tremo
      33      Sicu 1
      34      Sicu 2
      35      Shanai 1
      36      Shanai 2
      37      Shanai 3

      SN-U110-04 - Electric Grand and Clavi
      0       Electric Grand 1
      1       Electric Grand 2
      2       Electric Grand 3
      3       Electric Grand 4
      4       Electric Grand 5
      5       Electric Grand 6
      6       Electric Grand 7
      7       Electric Grand 8
      8       Clavichord 1
      9       Clavichord 2
      10      Clavichord 3
      11      Clavichord 4             

      SN-U110-05 - Orchestral Strings
      0       Violin 1
      1       Violin 2
      2       Violin 3
      3       Cello 1
      4       Cello 2
      5       Cello 3
      6       Cello 4
      7       Cello / Violin
      8       Contrabass / Cello
      9       Pizzicato
      10      Harp 1
      11      Harp 2            

      SN-U110-06 - Orchestral Winds
      0       Oboe 1
      1       Oboe 2
      2       Oboe 3
      3       Oboe 4
      4       Oboe 5
      5       Oboe 6
      6       Bassoon 1
      7       Bassoon 2
      8       Bassoon 3
      9       Bassoon 4
      10      Bassoon 5
      11      Clarinet 1
      12      Clarinet 2
      13      Clarinet 3
      14      Clarinet 4
      15      Clarinet 5
      16      Clarinet 6
      17      Bass Clarinet 1
      18      Bass Clarinet 2
      19      Bass Clarinet 3
      20      Bass Clarinet 4
      21      Bass Clarinet 5
      22      French Horn 1
      23      French Horn 2
      24      French Horn 3
      25      French Horn 4
      26      French Horn 5
      27      French Horn 6
      28      Tuba 1
      29      Tuba 2
      30      Tuba 3
      31      Tuba 4
      32      Tuba 5
      33      Timpani 1
      34      Timpani 2

      SN-U110-07 - Electric Guitar
      0       Jazz Guitar SW 1
      1       Jazz Guitar SW 2
      2       Jazz Guitar SW 3
      3       Jazz Guitar P
      4       Jazz Guitar F
      5       Jazz Guitar DT P
      6       Jazz Guitar DT F
      7       Jazz Guitar OCT P1
      8       Jazz Guitar OCT P2
      9       Jazz Guitar OCT F1
      10      Jazz Guitar OCT F2
      11      Jazz Guitar SW S/F
      12      Jazz Guitar COMP 1
      13      Jazz Guitar COMP 1
      14      Jazz Guitar COMP 1
      15      Overdrive Guitar SW 1
      16      Overdrive Guitar SW 2
      17      Overdrive Guitar SW 3
      18      Overdrive Guitar SW 4
      19      Overdrive Guitar SW 5
      20      Overdrive Guitar SW HM
      21      Overdrive Guitar P
      22      Overdrive Guitar F
      23      Overdrive Guitar DT P
      24      Overdrive Guitar DT F
      25      Overdrive Guitar OCT P1
      26      Overdrive Guitar OCT P2
      27      Overdrive Guitar OCT F1
      28      Overdrive Guitar OCT F2
      29      Overdrive Guitar SW S/F
      30      Overdrive Guitar FB 1
      31      Overdrive Guitar FB 2
      32      Overdrive Guitar FB 3
      33      Overdrive Guitar FB 4
      34      Overdrive Guitar FB 5
      35      Overdrive Guitar FB 6
      36      Overdrive Guitar FB 7
      37      Overdrive Guitar FB 8
      38      Overdrive Guitar FB 9
      39      Overdrive Guitar FB 10
      40      Overdrive Guitar FB 11
      41      Overdrive Guitar FB 12
      42      Distortion Guitar SW 1
      43      Distortion Guitar SW 2
      44      Distortion Guitar SW 3
      45      Distortion Guitar SW 4
      46      Distortion Guitar SW 5
      47      Distortion Guitar SW HM
      48      Distortion Guitar P
      49      Distortion Guitar F
      50      Distortion Guitar DT
      51      Distortion Guitar +4TH 1
      52      Distortion Guitar +4TH 2
      53      Distortion Guitar -5TH 1
      54      Distortion Guitar -5TH 2
      55      Distortion Guitar OCT 1
      56      Distortion Guitar OCT 2
      57      Distortion Guitar SW S/F
      58      Distortion Guitar FB 1
      59      Distortion Guitar FB 2
      60      Distortion Guitar FB 3
      61      Distortion Guitar FB 4
      62      Distortion Guitar FB 5
      63      Distortion Guitar FB 6
      64      Distortion Guitar FB 7
      65      Distortion Guitar FB 8
      66      Distortion Guitar FB 9
      67      Distortion Guitar FB 10
      68      Distortion Guitar FB 11
      69      Distortion Guitar FB 12
      70      Picking Harmonics

      SN-U110-08 - Synthesizer                [NOTE -- already available on the U-110 internal]
      0       Fantasia
      1       Bell Pad
      2       Syn Choir
      3       Breath Vox
      4       L. Calliope
      5       Calliope
      6       Metal Hit
      7       Rich Brass
      8       Brass Strings
      9       String Pad 1
      10      String Pad 2
      11      Pizzagogo
      12      Fanta Bell
      13      Spect Bell
      14      Bell Drum
      15      Synth Harp
      16      Pulse Wave 1
      17      Pulse Wave 2
      18      Pulse Wave 3
      19      Saw Wave 1
      20      Saw Wave 2
      21      Pizz
      22      Metal
      23      Breath
      24      Nails
      25      Spectrum 1
      26      Spectrum 2
      27      N. Dance

      SN-U110-09 - Guitar & Keyboards [NOTE -- already available on the U-110 internal]
      0       Bright EP 1
      1       Bright EP 2
      2       Syn. Vox 1
      3       Syn. Vox 2
      4       Syn. Bass 4
      5       Syn. Bass 5
      6       Syn. Bass 6
      7       Syn. Bass 7
      8       Syn. Bass 8
      9       Heavy EG 1
      10      Heavy EG 2
      11      JP. Strings
      12      JP. Brass 1
      13      JP. Brass 2
      14      R. Organ 1
      15      R. Organ 2

      SN-U110-10 - Rock Drums
      0       Rock Drums                      [Drumset]
      1       Electronic Drums                [Drumset]

      SN-U110-11 - Sound Effects
      0       Creaking
      1       Door
      2       Footsteps
      3       Waterphone
      4       S-Strings
      5       Screaming
      6       Laughing
      7       Dog
      8       Wave
      9       Stream
      10      Bird
      11      Drop
      12      Rain
      13      Thunder
      14      Car Door
      15      Car Stop
      16      Car Crash
      17      Train
      18      Pistol
      19      Machine Gun
      20      Missile
      21      Explosion
      22      Big Foot
      23      Godzilla
      24      Telephone Call
      25      Chime
      26      Applause
      27      From Radio
      28      Bubble 1
      29      Bubble 2
      30      Toy
      31      Fantasy Hit
      32      S-Set
      33      C-Set

      SN-U110-12 - Sax and Trombone
      0       Saxophone SW 1
      1       Saxophone SW 2
      2       Saxophone SW 3
      3       Saxophone SW 4
      4       Saxophone P 1
      5       Saxophone P 2
      6       Saxophone P 3
      7       Saxophone MF 1
      8       Saxophone MF 2
      9       Saxophone FF
      10      Trombone SW 1
      11      Trombone SW 2
      12      Trombone P
      13      Trombone MF
      14      Trombone FF
      15      TP/TRB SW 1                     [I presume this is "Trumpet/Trombone"]
      16      TP/TRB SW 2                     [I presume this is "Trumpet/Trombone"]
      17      TP/TRB P                        [I presume this is "Trumpet/Trombone"]
      18      TP/TRB MF                       [I presume this is "Trumpet/Trombone"]
      19      TP/TRB FF                       [I presume this is "Trumpet/Trombone"]

      SN-U110-13 - Super Strings                      [From JV-80]
      0       Super Strings 1
      1       Super Strings 1L
      2       Super Strings 1R
      3       Super Strings 2
      4       Super Strings 2L
      5       Super Strings 2R
      6       Super Strings 3
      7       Super Strings 3L
      8       Super Strings 3R
      9       Super Strings 4
      10      Super Strings 4L
      11      Super Strings 4R

      SN-U110-14 - Super Acoustic Guitar              [From JV-80]
      0       Steel Guitar 1
      1       Steel Soft
      2       Steel Hard
      3       Steel Guitar 2
      4       Steel (L)
      5       Steel (R)
      6       Nylon Guitar 1
      7       Nylon Soft
      8       Nylon Hard
      9       Nylon Guitar 2
      10      Nylon (L)
      11      Nylon (R)
      12      12-String Guitar 1
      13      12-String Guitar 2
      14      12-String Guitar 3
      15      12-String Guitar 4
      16      12-String Guitar 5
      17      Harmonics
      18      Squeak

      SN-U110-15 - Super Brass                        [From JV-80]
      0       High Brass 1
      1       High Brass 2
      2       High Brass SF
      3       Low Brass 1
      4       Low Brass 2
      5       Low Brass SF
      6       Brass Combo 1
      7       Brass Combo 1L
      8       Brass Combo 1R
      9       Brass Combo 2
      10      Brass Combo 2L
      11      Brass Combo 2R
      12      Brass Combo SF

*****/
