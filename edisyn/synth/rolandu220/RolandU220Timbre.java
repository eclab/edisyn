/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.rolandu220;

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
   A patch editor for Roland U-220 Timbres
*/

public class RolandU220Timbre extends Synth
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
            // U-220 INTERNAL
            "A. Piano 1", "A. Piano 2", "A. Piano 3", "A. Piano 4", "A. Piano 5", 
            "A. Piano 6", "A. Piano 7", "A. Piano 8", "A. Piano 9", "A. Piano 10", 
            "E. Piano 1", "E. Piano 2", "E. Piano 3", "E. Piano 4", "E. Piano 5", 
            "Bright EP 1", "Bright EP 2", "Vib 1", "Vib 2", "Vib 3", "Bell 1", "Bell 2", "Marimba", 
            "A. Guitar 1", "A. Guitar 2", "A. Guitar 3", "A. Guitar 4", "A. Guitar 5", 
            "E. Guitar 1", "E. Guitar 2", "E. Guitar 3", "E. Guitar 4", "Heavy EG 1", "Heavy EG 2", 
            "Slap 1", "Slap 2", "Slap 3", "Slap 4", "Slap 5", "Slap 6", 
            "Slap 7", "Slap 8", "Slap 9", "Slap 10", "Slap 11", "Slap 12", 
            "Fingered 1", "Fingered 2", "Picked 1", "Picked 2", "Fretless 1", "Fretless 2", "AC. Bass", 
            "Syn. Bass 1", "Syn. Bass 2", "Syn. Bass 3", "Syn. Bass 4", 
            "Syn. Bass 5", "Syn. Bass 6", "Syn. Bass 7", "Syn. Bass 8", 
            "Choir 1", "Choir 2", "Choir 3", "Choir 4", "Strings 1", "Strings 2", "Strings 3", "Strings 4", 
            "E. Organ 1", "E. Organ 2", "E. Organ 3", "E. Organ 4", "E. Organ 5", 
            "E. Organ 6", "E. Organ 7", "E. Organ 8", "E. Organ 9", "R. Organ 1", "R. Organ 2", 
            "Soft TP 1", "Soft TP 2", "TP / TRB 1", "TP / TRB 2", "TP / TRB 3", 
            "Sax 1", "Sax 2", "Sax 3", "Sax 4", "Sax 5", "Brass 1", "Flute 1", 
            "Shaku 1", "Shaku 2", "Fantasia", "Bell Pad", "Syn Choir", 
            "Breath Vox", "Syn. Vox 1", "Syn. Vox 2", "L. Calliope", "Calliope", 
            "Metal Hit", "Rich Brass", "JP. Brass 1", "JP. Brass 2", "Brass Strings", 
            "String Pad 1", "String Pad 2", "JP. Strings", "Pizzagogo", "Fanta Bell", 
            "Spect Bell", "Bell Drum", "Synth Harp", "Pulse Wave 1", "Pulse Wave 2", "Pulse Wave 3", 
            "Saw Wave 1", "Saw Wave 2", "Pizz", "Metal", "Breath", "Nails", "Spectrum 1", "Spectrum 2", "N. Dance", "Drums"
            },
            { 
            // SN-U110-01 - Pipe Organ and Harpsichord
            "Harpsichord 1", "Harpsichord 2", "Harpsichord 3", "Harpsichord 4", "Harpsichord 5", "Harpsichord 6",
            "Positive 1", "Positive 2", "Positive 3", "Positive 4", "Positive 5", "Positive 6", 
            "Church 1", "Church 2", "Church 3", "Church 4", "Church 5", "Church 6", "Church 7", "Church 8", "Church Reverb", 
            },
            { 
            // SN-U110-02 - Latin and FX Percussion
            "Latin 1", "Latin 2", "Latin 3", "FX 1", "FX 2", "FX 3", "FX 4", 
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
            "Rock Drums",
            "Electronic Drums"
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
        
    public static final String[] LFO_WAVEFORMS = { "Triangle", "Sine", "Square", "Saw Up", "Saw Down", "Trill 1", "Trill 2", "Random 1", "Random 2", "Random 3", "Random 4" };
    public static final int[] LOWER_BEND_RANGES = { -36, -24, -12, -11, -10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0 };         
    public static final int[] SENSITIVITY_RANGES = { -36, -24, -12, -11, -10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 };         

    HashMap allParametersToIndex = new HashMap();
        
    final static String[] allParameters = new String[]
    {
    // Name is 12 bytes but they're broken into two nibbles each
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
    "-",
    "-",
    "tonemedia",
    "tonenumber",
    "timbrelevel",
    "levelvelocitysens",
    "levelchannelpresssens",
    "envattackrate",
    "envdecayrate",
    "envsustainlevel",
    "envreleaserate",
    "pitchshiftcoarse",
    "pitchshiftfine",
    "bendrangelower",
    "bendrangeupper",
    "pitchpressuresens",
    "pitchpolysens",
    "autobenddepth",
    "autobendrate",
    "detunedepth",
    "lforate",
    "lfowaveform",
    "lfodepth",
    "lfodelay",
    "lforisetime",
    "lfomodulationdepth",
    "lfopressuresens",
    "lfopolysens",
    };

    ///// LOCATIONS
    /////
    ///// part is the temporary memory tone which is considered to be the "current patch"

    int part = 1;
    static final String PART_KEY = "part";
    
    public RolandU220Timbre()
        {
        part = getLastXAsInt(PART_KEY, getSynthClassName(), 1, true);
        for(int i = 0; i < allParameters.length; i++)
            {
            allParametersToIndex.put(allParameters[i], Integer.valueOf(i));
            }

        JComponent sourcePanel = new SynthPanel(this);
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        hbox.addLast(addTone(Style.COLOR_A()));
        vbox.add(hbox);

        vbox.add(addAmplifier(Style.COLOR_A()));
        vbox.add(addPitch(Style.COLOR_B()));
        vbox.add(addLFO(Style.COLOR_C()));
        sourcePanel.add(vbox, BorderLayout.CENTER);
        addTab("General", sourcePanel);                

        model.set("name", "Init Patch");  // has to be 10 long
        model.set("number", 0);
        loadDefaults();        
        }
                
                
    public JFrame sprout()
        {
        JFrame frame = super.sprout();
        addU220TimbreMenu();
        return frame;
        }         

    JRadioButtonMenuItem[] parts = new JRadioButtonMenuItem[6];
        
    public void setPart(int p)
        {
        part = p - 1;
        parts[part].setSelected(true);  // this won't trigger tha actionPerfored, and thus won't get saved
        }


    public void addU220TimbreMenu()
        {
        JMenu menu = new JMenu("U-220");
        menubar.add(menu);
        JMenuItem showCurrentMultiPatchMenu = new JMenuItem("Show Current Multi Patch");
        showCurrentMultiPatchMenu.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                final RolandU220Multi synth = new RolandU220Multi();
                if (tuple != null)
                    synth.tuple = new Midi.Tuple(tuple, synth.buildInReceiver(), synth.buildKeyReceiver(), synth.buildKey2Receiver());
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
                                Model tempModel = buildModel();
                                synth.performRequestCurrentDump();
                                }
                            });
                    }
                else
                    {
                    showSimpleError("Disconnected", "You can't show a patch when disconnected.");
                    }
                }
            });
        menu.add(showCurrentMultiPatchMenu);
        JMenuItem setupTestPatchMenu = new JMenuItem("Set up Test Patch for Part 1 Only");
        setupTestPatchMenu.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                setupTestPatch(true);
                }
            });
        menu.add(setupTestPatchMenu);
        JMenuItem setupTestPatchMenu2 = new JMenuItem("Set up Test Patch for All Parts");
        setupTestPatchMenu2.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                setupTestPatch(false);
                }
            });
        menu.add(setupTestPatchMenu2);

        ButtonGroup g = new ButtonGroup();
        for(int i = 1; i <= 6; i++)
            {
            int _i = i;
            parts[_i - 1] = new JRadioButtonMenuItem("Part " + i);
            g.add(parts[_i - 1] );
            parts[_i - 1].setSelected(part == i);
            parts[_i - 1].addActionListener(new ActionListener()
                {
                public void actionPerformed(ActionEvent e)
                    {
                    part = _i;
                    setLastX("" + _i, PART_KEY, getSynthClassName(), true);
                    }
                });
            menu.add(parts[_i - 1]);
            }
        }
                
    // Prepare a Patch whose slot N has the current MIDI channel, and has all the partials in reserve.
    // N is defined as the current emit location.  All other slots have zero partials and MIDI channel OFF.
        
    public void setupTestPatch(boolean timbre1)
        {
        if (tuple == null)
            if (!setupMIDI())
                return;

        if (tuple != null)
            {
            final RolandU220Multi synth = new RolandU220Multi();
            synth.tuple = new Midi.Tuple(tuple, synth.buildInReceiver(), synth.buildKeyReceiver(), synth.buildKey2Receiver());
            if (synth.tuple != null)
                {
                synth.loadDefaults();
                
                if (timbre1)
                    {
                    for(int i = 1; i <= 6; i++)
                        {
                        synth.getModel().set("p" + i + "outputlevel", 100);
                                        
                        // turn off everybody
                        synth.getModel().set("p" + i + "receivechannel", RolandU220Multi.MIDI_CHANNEL_OFF);
                        synth.getModel().set("p" + i + "voicereserve", 0);
                        }
                                
                    // turn off rhythm
                    synth.getModel().set("rhythmmidichannel", RolandU220Multi.MIDI_CHANNEL_OFF);
                    synth.getModel().set("rhythmoutputlevel", 0);
                        
                    // turn on part
                    synth.getModel().set("p" + (part + 1) + "rhythmreceivechannel", getChannelOut());
                    synth.getModel().set("p" + (part + 1) + "rhythmvoicereserve", 30);
                    }
                else
                    {
                    for(int i = 1; i <= 6; i++)
                        {
                        synth.getModel().set("p" + i + "outputlevel", 100);
                                        
                        // turn on everybody, sharing equally
                        synth.getModel().set("p" + i + "receivechannel", i - 1);
                        synth.getModel().set("p" + i + "voicereserve", 4);
                        }
                                                
                    // turn on rhythm                                       
                    synth.getModel().set("rhythmmidichannel", 9);           // channel 10
                    synth.getModel().set("rhythmoutputlevel", 6);       // remaining voices 
                    }

                synth.sendAllParameters();
                sendAllParameters();
                }
            }
        }

               
    public String getDefaultResourceFileName() { return "RolandU220Timbre.init"; }
    public String getHTMLResourceFileName() { return "RolandU220Timbre.html"; }

    // There are no banks
    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        int original = model.get("number");
                
        JTextField number = new SelectedTextField("" + (original + 1), 3);

        while(true)
            {
            boolean result = showMultiOption(this, new String[] { "Timbre Number"}, 
                new JComponent[] { number }, title, "<html>Enter Timbre Number.<br>This will be updated in Part " + part + ".</html>");
                
            if (result == false)
                return false;
                                
            int n;
            try { n = Integer.parseInt(number.getText()); }
            catch (NumberFormatException e)
                {
                showSimpleError(title, "The Timbre Number must be an integer 1...128");
                continue;
                }
            if (n < 1 || n > 64)
                {
                showSimpleError(title, "The Timbre Number must be an integer 1...128");
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
        
        comp = new StringComponent("Patch Name", this, "name", MAXIMUM_NAME_LENGTH, "Name must be up to 12 ASCII characters.")
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


    public JComponent addTone(Color color)
        {
        Category category = new Category(this, "Tone", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        params = PCM[0];
        Chooser pcm = new Chooser("Tone", this, "tonenumber", params);
        
        VBox vbox = new VBox();
        params = PCM_NAMES;
        comp = new Chooser("Tone Medium", this, "tonemedia", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                int val = model.get(key, 0);
                int cur = model.get("tonenumber", 0);
                pcm.setElements(PCM[val]);
                if (cur >= PCM[val].length)
                    model.set("tonenumber", 0);
                }
            };
        vbox.add(comp);
        
        vbox.add(pcm);
        hbox.add(vbox);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addAmplifier(Color color)
        {
        Category category = new Category(this, "Amplifier", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        comp = new LabelledDial("Level", this, "timbrelevel", color, 0, 127)
        	{
            public int getDefaultValue() { return 127; }
        	};
        hbox.add(comp);
        
        comp = new LabelledDial("Velocity", this, "levelvelocitysens", color, 1, 15)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val)
                {
                return "" + (val - 8);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Sensitivity");
        hbox.add(comp);

        comp = new LabelledDial("Aftertouch", this, "levelchannelpresssens", color, 1, 15)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val)
                {
                return "" + (val - 8);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Sensitivity");
        hbox.add(comp);
        
        comp = new LabelledDial("Attack", this, "envattackrate", color, 1, 15)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val)
                {
                return "" + (val - 8);
                }
            };
        hbox.add(comp);
        
        comp = new LabelledDial("Decay", this, "envdecayrate", color, 1, 15)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val)
                {
                return "" + (val - 8);
                }
            };
        hbox.add(comp);
        
        comp = new LabelledDial("Sustain", this, "envsustainlevel", color, 1, 15)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val)
                {
                return "" + (val - 8);
                }
            };
        hbox.add(comp);
        
        comp = new LabelledDial("Release", this, "envreleaserate", color, 1, 15)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val)
                {
                return "" + (val - 8);
                }
            };
        hbox.add(comp);
        
        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "envattackrate", "envdecayrate",        null,                      "envreleaserate" },
            new String[] { null, null,                    "envsustainlevel", "envsustainlevel", null },
            new double[] { 0, 0.25 / 14, 0.25 / 14, 0.25, 0.25 / 14 },
            new double[] { 0, 1.0, 1.0 / 14, 1.0 / 14, 0})
            {
            public void postProcess(double[] xVals, double[] yVals)
                {
                xVals[1] -= 0.25 / 14;
                xVals[2] -= 0.25 / 14;
                xVals[4] -= 0.25 / 14;
                yVals[2] -= 1.0 / 14;
                yVals[3] -= 1.0 / 14;

                // attack, decay, and release are BACKWARDS -- lower values are SLOWER
                xVals[1] = 0.25 - xVals[1];
                xVals[2] = 0.25 - xVals[2];
                xVals[4] = 0.25 - xVals[4];
                }
            };
        //((EnvelopeDisplay)comp).setAxis(0.5);
        hbox.add(comp);
            
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addPitch(Color color)
        {
        Category category = new Category(this, "Pitch", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        comp = new LabelledDial("Coarse", this, "pitchshiftcoarse", color, 8, 56)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val)
                {
                return "" + (val - 32);
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Fine", this, "pitchshiftfine", color, 14, 114)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val)
                {
                return "" + (val - 64);
                }
            };
        hbox.add(comp);
        
        // FIXME: docs say the range is 0...15 but there are only 15 elements
        comp = new LabelledDial("Bend Range", this, "bendrangelower", color, 0, 14)
            {
            public int getDefaultValue() { return 14; }
            public double getStartAngle() { return 180; }
            public String map(int val)
                {
                return "" + LOWER_BEND_RANGES[val];
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Lower");
        hbox.add(comp);
        
        comp = new LabelledDial("Bend Range", this, "bendrangeupper", color, 0, 12);
        ((LabelledDial)comp).addAdditionalLabel("Upper");
        hbox.add(comp);
        
        // FIXME: docs say the range is 0...27 but there are only 27 elements
        comp = new LabelledDial("Channel", this, "pitchpressuresens", color, 0, 26)
            {
            public int getDefaultValue() { return 14; }             // FIXME: test this
            public double getStartAngle() { return 240; }
            public String map(int val)
                {
                return "" + SENSITIVITY_RANGES[val];
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Aftertouch");
        hbox.add(comp);

        // FIXME: docs say the range is 0...27 but there are only 27 elements
        comp = new LabelledDial("Polyphonic", this, "pitchpolysens", color, 0, 26)
            {
            public int getDefaultValue() { return 14; }             // FIXME: test this
            public double getStartAngle() { return 240; }
            public String map(int val)
                {
                return "" + SENSITIVITY_RANGES[val];
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Aftertouch");
        hbox.add(comp);
        
        // FIXME: docs say the range is 0...27 but there are only 27 elements
        comp = new LabelledDial("Auto Bend", this, "autobenddepth", color, 0, 26)
            {
            public int getDefaultValue() { return 14; }             // FIXME: test this
            public double getStartAngle() { return 240; }
            public String map(int val)
                {
                return "" + SENSITIVITY_RANGES[val];
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Depth");
        hbox.add(comp);
        
        comp = new LabelledDial("Auto Bend", this, "autobendrate", color, 0, 15);
        ((LabelledDial)comp).addAdditionalLabel("Rate");
        hbox.add(comp);
        
        comp = new LabelledDial("Detune", this, "detunedepth", color, 0, 15);
        ((LabelledDial)comp).addAdditionalLabel("Depth");
        hbox.add(comp);
                
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addLFO(Color color)
        {
        Category category = new Category(this, "Vibrato LFO", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();

        VBox vbox = new VBox();
        params = LFO_WAVEFORMS;
        comp = new Chooser("Waveform", this, "lfowaveform", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Rate", this, "lforate", color, 0, 63);
        hbox.add(comp);

        comp = new LabelledDial("Depth", this, "lfodepth", color, 0, 15);
        hbox.add(comp);
        
        comp = new LabelledDial("Delay", this, "lfodelay", color, 0, 15);
        ((LabelledDial)comp).addAdditionalLabel("Time");
        hbox.add(comp);
        
        comp = new LabelledDial("Rise", this, "lforisetime", color, 0, 15);
        ((LabelledDial)comp).addAdditionalLabel("Time");
        hbox.add(comp);
        
        comp = new LabelledDial("Modulation", this, "lfomodulationdepth", color, 0, 15);
        ((LabelledDial)comp).addAdditionalLabel("Depth");
        hbox.add(comp);
        
        comp = new LabelledDial("Channel", this, "lfopressuresens", color, 0, 15);
        ((LabelledDial)comp).addAdditionalLabel("Aftertouch");
        hbox.add(comp);

        comp = new LabelledDial("Polyphonic", this, "lfopolysens", color, 0, 15);
        ((LabelledDial)comp).addAdditionalLabel("Aftertouch");
        hbox.add(comp);
                        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    // IDs are 17 and up
    public byte getID() 
        { 
        try 
            { 
            byte b = (byte)(Byte.parseByte(tuple.id));
            if (b >= 17) return (byte)(b - 1);
            }
        catch (NullPointerException e) { } // expected.  Happens when tuple's not built yet
        catch (NumberFormatException e) { Synth.handleException(e); }
        return (byte)16;                // IDs start at 17
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
            byte[] data = new byte[10 + 24];
            data[0] = (byte) 0xF0;
            data[1] = (byte) 0x41;
            data[2] = (byte) getID();
            data[3] = (byte) 0x2B;
            data[4] = (byte) 0x12;
            data[5] = (byte) 0x10;
            data[6] = (byte) (0x10 + part - 1);
            data[7] = (byte) 0x00;
                
            char[] name = (model.get("name", "") + "            ").toCharArray();
            for(int i = 0; i < 12; i++)
                {
                data[i * 2 + 8]= (byte)(name[i] & 15);
                data[i * 2 + 8 + 1] = (byte)((name[i] >>> 4) & 15);
                }
            data[data.length - 2] = produceChecksum(data, 5, data.length - 2);
            data[data.length - 1] = (byte)0xF7;
            return data;
            }
        else
            {
            byte AA = (byte)(0x10);
            byte BB = (byte)(0x10 + part - 1);
            byte CC = (byte)((Integer)(allParametersToIndex.get(key))).intValue();
            byte val = (byte)(model.get(key));
                
            byte checksum = produceChecksum(new byte[] { AA, BB, CC, val });
            return new byte[] { (byte)0xF0, 0x41, getID(), 0x2B, 0x12, AA, BB, CC, val, checksum, (byte)0xF7 };
            }
        }
   
    /** If the user is editing the patch on the synth, the U-220 won't change patches!
    	So just in case we send this. */
    public boolean getSendsParametersAfterNonMergeParse() { return true; }

    
    public int parse(byte[] data, boolean fromFile)
        {
        // What is the tone patch number?
        int AA = data[5];
        int BB = data[6];
        int CC = data[7];
        
        if (AA == 0x02)         // Write to Timbre banks
            {
            model.set("number", (BB * 128 + CC) / 64);
            }
        
        // The U-220 is entirely byte-packed :-(  So we have to do this by hand.

        int pos = 8;
        String name = "";
        for(int i = 0; i < 12; i++)
            {
            int lsb = data[pos++];
            int msb = data[pos++];
            name = name + (char)(lsb | (msb << 4));
            }
        model.set("name", name);

        int lsb1 = data[pos++];
        int msb1 = data[pos++];
        int lsb2 = data[pos++];
        int msb2 = data[pos++];
        int val = lsb1 | (msb1 << 4) | (lsb2 << 8) | (msb2 << 12);
        model.set("detunedepth", (val >>> 0xC) & 15);   // C D E F
        model.set("tonemedia", (val >>> 0x7) & 31);             // 7 8 9 A B
        model.set("tonenumber", (val >>> 0x0) & 127);   // 0 1 2 3 4 5 6

        lsb1 = data[pos++];
        msb1 = data[pos++];
        lsb2 = data[pos++];
        msb2 = data[pos++];
        val = lsb1 | (msb1 << 4) | (lsb2 << 8) | (msb2 << 12);
        model.set("timbrelevel", (val >>> 0x8) & 127);                  // 8 9 A B C D E
        model.set("levelchannelpresssens", (val >>> 0x4) & 15); // 4 5 6 7
        model.set("levelvelocitysens", (val >>> 0x0) & 15);             // 0 1 2 3

        lsb1 = data[pos++];
        msb1 = data[pos++];
        lsb2 = data[pos++];
        msb2 = data[pos++];
        val = lsb1 | (msb1 << 4) | (lsb2 << 8) | (msb2 << 12);
        model.set("envreleaserate", (val >>> 0xC) & 15);                // C D E F
        model.set("envsustainlevel", (val >>> 0x8) & 15);       // 8 9 A B
        model.set("envdecayrate", (val >>> 0x4) & 15);          // 4 5 6 7
        model.set("envattackrate", (val >>> 0x0) & 15);         // 0 1 2 3

        lsb1 = data[pos++];
        msb1 = data[pos++];
        lsb2 = data[pos++];
        msb2 = data[pos++];
        val = lsb1 | (msb1 << 4) | (lsb2 << 8) | (msb2 << 12);
        model.set("pitchshiftcoarse", (val >>> 0x8) & 63);              // 8 9 A B C D
        model.set("pitchshiftfine", (val >>> 0x0) & 127);               // 0 1 2 3 4 5 6

        lsb1 = data[pos++];
        msb1 = data[pos++];
        lsb2 = data[pos++];
        msb2 = data[pos++];
        val = lsb1 | (msb1 << 4) | (lsb2 << 8) | (msb2 << 12);
        model.set("autobenddepth", (val >>> 0x9) & 31);         // 9 A B C D
        model.set("bendrangeupper", (val >>> 0x5) & 15);                // 5 6 7 8
        model.set("bendrangelower", (val >>> 0x0) & 31);                // 0 1 2 3 4    -- not sure why we have 5 bits

        lsb1 = data[pos++];
        msb1 = data[pos++];
        lsb2 = data[pos++];
        msb2 = data[pos++];
        val = lsb1 | (msb1 << 4) | (lsb2 << 8) | (msb2 << 12);
        model.set("autobendrate", (val >>> 0xA) & 15);          // A B C D
        model.set("pitchpressuresens", (val >>> 0x5) & 31);     // 5 6 7 8 9
        model.set("pitchpolysens", (val >>> 0x0) & 31);         // 0 1 2 3 4

        lsb1 = data[pos++];
        msb1 = data[pos++];
        lsb2 = data[pos++];
        msb2 = data[pos++];
        val = lsb1 | (msb1 << 4) | (lsb2 << 8) | (msb2 << 12);
        model.set("lfodepth", (val >>> 0xC) & 15);                              // C D E F
        model.set("lfodelay", (val >>> 0x8) & 15);                              // 8 9 A B
        model.set("lfomodulationdepth", (val >>> 0x4) & 15);            // 0 1 2 3

        lsb1 = data[pos++];
        msb1 = data[pos++];
        lsb2 = data[pos++];
        msb2 = data[pos++];
        val = lsb1 | (msb1 << 4) | (lsb2 << 8) | (msb2 << 12);
        model.set("lfowaveform", (val >>> 0x8) & 15);   // 8 9 A B
        model.set("lforate", (val >>> 0x0) & 63);               // 0 1 2 3 4 5

        lsb1 = data[pos++];
        msb1 = data[pos++];
        lsb2 = data[pos++];
        msb2 = data[pos++];
        val = lsb1 | (msb1 << 4) | (lsb2 << 8) | (msb2 << 12);
        model.set("lfopolysens", (val >>> 0xC) & 15);           // C D E F
        model.set("lfopressuresens", (val >>> 0x8) & 15);       // 8 9 A B
        model.set("lforisetime", (val >>> 0x0) & 15);           // 0 1 2 3

        revise();
        return PARSE_SUCCEEDED;
        }
    

    public byte[] emit(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {             
        if (tempModel == null)
            tempModel = getModel();

        int start = tempModel.get("number") * 64;
        int AA = (toWorkingMemory ? 0x00 : 0x02);
        int BB = (toWorkingMemory ? 0x10 + (part - 1) : start / 128 );
        int CC = (toWorkingMemory ? 0x00 : start % 128 );
                
        // The U-220 is entirely byte-packed :-(  So we have to do this by hand.
                
        byte[] buf = new byte[74];
        buf[0] = (byte)0xF0;
        buf[1] = (byte)0x41;
        buf[2] = (byte)getID();
        buf[3] = (byte)0x2B;
        buf[4] = (byte)0x12;
        buf[5] = (byte) AA;
        buf[6] = (byte) BB;
        buf[7] = (byte) CC;

        int pos = 8;
                
        String name = model.get("name", "Untitled") + "            ";
        for(int i = 0; i < 12; i++)
            {
            char c = name.charAt(i);
            buf[pos++] = (byte)(c & 15);
            buf[pos++] = (byte)((c >>> 4) & 15);
            }
        
        int d = 
            (model.get("detunedepth") << 0xC) |
            (model.get("tonemedia") << 0x7) |
            (model.get("tonenumber") << 0x0);
        buf[pos++] = (byte)(d & 15);
        buf[pos++] = (byte)((d >>> 4) & 15);
        buf[pos++] = (byte)((d >>> 8) & 15);
        buf[pos++] = (byte)((d >>> 12) & 15);
        
        d = 
            (model.get("timbrelevel") << 0x8) |
            (model.get("levelchannelpresssens") << 0x4) |
            (model.get("levelvelocitysens") << 0x0);
        buf[pos++] = (byte)(d & 15);
        buf[pos++] = (byte)((d >>> 4) & 15);
        buf[pos++] = (byte)((d >>> 8) & 15);
        buf[pos++] = (byte)((d >>> 12) & 15);
        
        d = 
            (model.get("envreleaserate") << 0xC) |
            (model.get("envsustainlevel") << 0x8) |
            (model.get("envdecayrate") << 0x4) |
            (model.get("envattackrate") << 0x0);
        buf[pos++] = (byte)(d & 15);
        buf[pos++] = (byte)((d >>> 4) & 15);
        buf[pos++] = (byte)((d >>> 8) & 15);
        buf[pos++] = (byte)((d >>> 12) & 15);

        d = 
            (model.get("pitchshiftcoarse") << 0x8) |
            (model.get("pitchshiftfine") << 0x0);
        buf[pos++] = (byte)(d & 15);
        buf[pos++] = (byte)((d >>> 4) & 15);
        buf[pos++] = (byte)((d >>> 8) & 15);
        buf[pos++] = (byte)((d >>> 12) & 15);

        d = 
            (model.get("autobenddepth") << 0x9) |
            (model.get("bendrangeupper") << 0x5) |
            (model.get("bendrangelower") << 0x0);
        buf[pos++] = (byte)(d & 15);
        buf[pos++] = (byte)((d >>> 4) & 15);
        buf[pos++] = (byte)((d >>> 8) & 15);
        buf[pos++] = (byte)((d >>> 12) & 15);

        d = 
            (model.get("autobendrate") << 0xA) |
            (model.get("pitchpressuresens") << 0x5) |
            (model.get("pitchpolysens") << 0x0);
        buf[pos++] = (byte)(d & 15);
        buf[pos++] = (byte)((d >>> 4) & 15);
        buf[pos++] = (byte)((d >>> 8) & 15);
        buf[pos++] = (byte)((d >>> 12) & 15);

        d = 
            (model.get("lfodepth") << 0xC) |
            (model.get("lfodelay") << 0x8) |
            (model.get("lfomodulationdepth") << 0x4);
        buf[pos++] = (byte)(d & 15);
        buf[pos++] = (byte)((d >>> 4) & 15);
        buf[pos++] = (byte)((d >>> 8) & 15);
        buf[pos++] = (byte)((d >>> 12) & 15);

        d = 
            (model.get("lfowaveform") << 0x8) |
            (model.get("lforate") << 0x0);
        buf[pos++] = (byte)(d & 15);
        buf[pos++] = (byte)((d >>> 4) & 15);
        buf[pos++] = (byte)((d >>> 8) & 15);
        buf[pos++] = (byte)((d >>> 12) & 15);

        d = 
            (model.get("lfopolysens") << 0xC) |
            (model.get("lfopressuresens") << 0x8) |
            (model.get("lforisetime") << 0x0);
        buf[pos++] = (byte)(d & 15);
        buf[pos++] = (byte)((d >>> 4) & 15);
        buf[pos++] = (byte)((d >>> 8) & 15);
        buf[pos++] = (byte)((d >>> 12) & 15);

        // there are two extra nybblized bytes left over
                
        buf[buf.length - 2] = produceChecksum(buf, 5, buf.length - 2);
        buf[buf.length - 1] = (byte)0xF7;
        return buf;
        }

    public void changePatch(Model tempModel) 
        {
        if (tempModel == null)
            tempModel = getModel();

        // We do a PC to the Timbre's Rx Channel (patch/part/midi/rx ch),
        // which we assume to be the same as the existing channel
                
        tryToSendMIDI(buildPC(getChannelOut(), tempModel.get("number")));
        }


    // Requests a Timbre from a specific RAM slot (1...128)
    public byte[] requestDump(Model tempModel)
        {
        if (tempModel == null)
            tempModel = getModel();

        int number = tempModel.get("number");
                
        // We do a PC to the Timbre's Rx Channel (patch/part/midi/rx ch),
        // which we assume to be the same as the existing channel
                
        buildPC(getChannelOut(), number);
        byte AA = (byte)(0x02);
        int num = 0x40 * number;
        byte BB = (byte)(num / 128);
        byte CC = (byte)(num % 128);
        byte LSB = (byte)0x40;
        byte MSB = (byte)0x00; 
                
        byte checksum = produceChecksum(new byte[] { AA, BB, CC, (byte)0x00, LSB, MSB });
        byte[] b = new byte[] { (byte)0xF0, (byte)0x41, getID(), (byte)0x2B, (byte)0x11, 
            AA, BB, CC, (byte)0x00, MSB, LSB, checksum, (byte)0xF7 }; 
        return b;
        }
  
    public byte[] requestCurrentDump()
        {
        byte AA = (byte)(0x00);
        byte BB = (byte)(0x10 + (part - 1));
        byte CC = (byte)(0x00);
        byte LSB = (byte)0x40;
        byte MSB = (byte)0x00; 
                
        byte checksum = produceChecksum(new byte[] { AA, BB, CC, (byte)0x00, LSB, MSB });
        byte[] b = new byte[] { (byte)0xF0, (byte)0x41, getID(), (byte)0x2B, (byte)0x11, 
            AA, BB, CC, (byte)0x00, MSB, LSB, checksum, (byte)0xF7 }; 
        return b;
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
        
    public static String getSynthName() { return "Roland U-20 / 220 [Timbre]"; }
    
    public String getPatchName(Model model) { return model.get("name", "Untitled  "); }

    public int getPauseAfterSendAllParameters() { return 100; } 
 
    public int getPauseAfterSendOneParameter() { return 25; }       // In the 1.07 firmware notes it says "at least 20ms" (http://llamamusic.com/d110/ROM_IC_Bug_Fixes.html).  In my firmware (1.10) the D-110 can handle changes thrown at it full blast, but earlier firmware (1.07) cannot.
        
    public Model getNextPatchLocation(Model model)
        {
        int number = model.get("number");
        
        number++;
        if (number >= 128)
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
        
        return "T-" + (model.get("number") + 1 < 100 ? (model.get("number") + 1 < 10 ? "00" : "0") : "") + ((model.get("number") + 1));
        }

    public int getBatchDownloadWaitTime() { return 275; }

    /** Default is null, which indicates that the patch editor does not support librarians.  */
    public String[] getPatchNumberNames() { return buildIntegerNames(128, 1); }

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

    public boolean librarianTested() { return false; }
    }

/*** 
     ROLAND U-220 SYSEX SUMMARY

     EMIT PARAMETER
     F0 41 ID 2B 12 PARAM PARAM PARAM VALUE CHECKSUM F7
        
     See Tables 10 through 15 (P. 149) for the values of the params.  Note that
     the name parameters are split into nybbles.  This is essentially a special case
     of DUMP DATA.
        
     REQUEST DATA
     F0 41 ID 2B 11 ADDR ADDR ADDR SIZE SIZE SIZE CHECKSUM F7
        
     Note that the data returned is nybblized, so the SIZE will be twice what you expect.
        
     DUMP DATA
     F0 41 ID 2B 12 ADDR ADDR ADDR DATA... CHECKSUM F7
        
     No length is provided -- you have to give an entire region, such as a single patch
     or a single rhythm map etc.
        
*/
