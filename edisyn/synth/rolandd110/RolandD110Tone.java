/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.rolandd110;

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
   A patch editor for Roland D-110 Tones
   
   <p>The D-110 is structured strangely.  The basic sound structure, which in any sane synthesizer would be a "single" patch, is the TONE.  A tone
   is combination of up to four PARTIALS (voices).  However you cannot play a tone: instead, you have to wrap it in a traditional multimode patch 
   called for some reason a PATCH, which holds up to 8 tones assigned to different MIDI channels.  Patches don't hold tones, but rather hold pointers
   to tones plus some additional wrapper information such as per-tone panning and volume.  64 tones are stored in memory, and some 128 patches are 
   also in memory.  You cannot play tones, you can only play patches.  So to play a single tone, you basicaly need to make a patch whose first
   slot points to the tone, at a given MIDI channel and volume etc., and whose other slots are turned off.
   When you load a patch into temporary memory, the tones it refers to are also recalled and copied into temporary memory.  You can edit them
   directly from there.
   
   <p>Multimode patches can also refer to additional tones in their slots: a bank of RHYTHM tones (which can be edited but it's not very useful) -- 
   essentially a drum bank -- and two 64-tone banks of preset tones stored in ROM. 
   
   <p>The D-110 also has a weird and worthless notion of a TIMBRE.  A timbre is essentially the portion of a patch's slot: it contains a pointer to
   a tone, plus volume and panning etc., but not the MIDI channel.  You have 128 timbres stored in memory.  Patches do not refer to timbres -- they
   refer directly to tones.  And furthermore, you can't play timbres.  So what's the point of a timbre at all?  It appears that the idea was that
   you could wrap tones in timbres and organize the timbres you liked, then select from them to copy their data into the slots of patches you create.
   As far as I can tell, this entirely useless and just serves to make the D-110 needlessly complex.
   
   <p>Thus tones are stored in 64 different spots in memory, and also when a patch is loaded from RAM into temporary (current) memory, the tones referred
   to by its eight slots are copied from these spots in memory to 8 temporary memory slots.  So whereas in most synthesizers you'd have a single patch
   stored in some N spots in permanent memory, and also 1 location of current memory, in the D-110 there are *eight* locations in current memory we have
   to keep track of because tones cannot be played directly but must be played in the context of a (multimode) patch.
   
   
   <p>I'm doing that as follows:
   
   <ul>
   <li>A special D-110 menu specifies which of the 8 temporary slots is what I'm going to call the "current location".
   <li>REQUEST CURRENT PATCH loads from the "current location"
   <li>REQUEST PATCH... loads from the 64 permanent memory locations.
   <li>SEND TO CURRENT PATCH sends to the "current location"
   <li>SEND TO PATCH... is disabled
   <li>WRITE TO PATCH... writes to a permanent memory location.
   </ul>
   
   <p>You can have a card in the Roland D-110 but it just hijacks the memory.  Edisyn cannot copy from cards to internal memory, nor can it see internal memory
   once the cards are copied over.  So there while there are effectively several "banks" of tones (internal, card, preset A, preset B, rhythm), 
   they can't be accessed independently of one another (or at all), so we're treating the D-110 as not having banks of tones, just numbers.
        
   @author Sean Luke
*/

public class RolandD110Tone extends Synth
    {
    public static final String[] PCM = new String[] { 
        "Bass Drum-1", "Bass Drum-2", "Bass Drum-3", "Snare Drum-1", "Snare Drum-2", "Snare Drum-3", "Snare Drum-4", "Tom Tom-1", "Tom Tom-2", "High Hat",
        "High Hat (Loop)", "Crash Cymbal-1", "Crash Cymbal-2 (Loop)", "Ride Cymbal-1", "Ride Cymbal-2 (Loop)", "Cup", "China Cymbal-1", "China Cymbal-2 (Loop)",
        "Rim Shot", "Hand Clap", "Mute High Conga", "Conga", "Bongo", "Cowbell", "Tambourine", "Agogo", "Claves", "Timbale High", "Timable Low", "Cabasa",
        "Timpani Attack", "Timpani", "Acoustic Piano High", "Acoustic Piano Low", "Piano Forte Thump", "Organ Percussion", "Trumpet", "Lips", "Trombone",
        "Clarinet", "Flute High", "Flute Low", "Steamer", "Indian Flute", "Breath", "Vibraphone High", "Vibraphone Low", "Marmimba", "Xylophone high",
        "Xyophone Low", "Kalimba", "Wind Bell", "Chime Bar", "Hammer", "Guiro", "Chink", "Nails", "Fretless Bass", "Pull Bass", "Slap Bass", "Thump Bass",
        "Acoustic Bass", "Electric Bass", "Gut Guitar", "Steel Guitar", "Dirty Guitar", "Pizzicato", "Harp", "Contrabass", "Cello", "Violin-1", "Violin-2",
        "Koto", "Draw Bars (Loop)", "High Organ (Loop)", "Low Organ (Loop)", "Trumpet (Loop)", "Trombone (Loop)", "Sax-1 (Loop)", "Sax-2 (Loop)", "Reed (Loop)",
        "Slap Bass (Loop)", "Acoustic Bass (Loop)", "Electric Bass-1 (Loop)", "Electric Bass-2 (Loop)", "Gut Guitar (Loop)", "Steel Guitar (Loop)",
        "Electric Guitar (Loop)", "Clav (Loop)", "Cello (Loop)", "Violin (Loop)", "Electric Piano-1 (Loop)", "Electric Piano-2 (Loop)", "Harpsichord-1 (Loop)",
        "Harpsichord-2 (Loop)", "Telephone Bell (Loop)", "Female Voice-1 (Loop)", "Female Voice-2 (Loop)", "Male Voice-1 (Loop)", "Male Voice-2 (Loop)",
        "Spectrum-1 (Loop)", "Spectrum-2 (Loop)", "Spectrum-3 (Loop)", "Spectrum-4 (Loop)", "Spectrum-5 (Loop)", "Spectrum-6 (Loop)", "Spectrum-7 (Loop)",
        "Spectrum-8 (Loop)", "Spectrum-9 (Loop)", "Spectrum-10 (Loop)", "Noise (Loop)", "Shot-1", "Shot-2", "Shot-3", "Shot-4", "Shot-5", "Shot-6",
        "Shot-7", "Shot-8", "Shot-9", "Shot-10", "Shot-11", "Shot-12", "Shot-13", "Shot-14", "Shot-15", "Shot-16", "Shot-17", "Bass Drum-1", "Bass Drum-2",
        "Bass Drum-3", "Snare Drum-1", "Snare Drum-2", "Snare Drum-3", "Snare Drum-4", "Tom Tom-1", "Tom Tom-2", "High Hat", "High Hat (Loop)",
        "Crash Cymbal-1", "Crash Cymbal-2 (Loop)", "Ride Cymbal-1", "Ride Cymbal-2 (Loop)", "Cup", "China Cymbal-1", "China Cymbal-2 (Loop)", "Rim Shot",
        "Hand Clap", "Mute High Conga", "Conga", "Bongo", "Cowbell", "Tambourine", "Agogo", "Claves", "Timbale High", "Timable Low", "Cabasa", "Loop-1",
        "Loop-2", "Loop-3", "Loop-4", "Loop-5", "Loop-6", "Loop-7", "Loop-8", "Loop-9", "Loop-10", "Loop-11", "Loop-12", "Loop-13", "Loop-14", "Loop-15",
        "Loop-16", "Loop-17", "Loop-18", "Loop-19", "Loop-20", "Loop-21", "Loop-22", "Loop-23", "Loop-24", "Loop-25", "Loop-26", "Loop-27", "Loop-28",
        "Loop-29", "Loop-30", "Loop-31", "Loop-32", "Loop-33", "Loop-34", "Loop-35", "Loop-36", "Loop-37", "Loop-38", "Loop-39", "Loop-40", "Loop-41",
        "Loop-42", "Loop-43", "Loop-44", "Loop-45", "Loop-46", "Loop-47", "Loop-48", "Loop-49", "Loop-50", "Loop-51", "Loop-52", "Loop-53", "Loop-54",
        "Loop-55", "Loop-56", "Loop-57", "Loop-58", "Loop-59", "Loop-60", "Loop-61", "Loop-62", "Loop-63", "Loop-64", "Jam-1", "Jam-2", "Jam-3", "Jam-4",
        "Jam-5", "Jam-6", "Jam-7", "Jam-8", "Jam-9", "Jam-10", "Jam-11", "Jam-12", "Jam-13", "Jam-14", "Jam-15", "Jam-16", "Jam-17", "Jam-18", "Jam-19",
        "Jam-20", "Jam-21", "Jam-22", "Jam-23", "Jam-24", "Jam-25", "Jam-26", "Jam-27", "Jam-28", "Jam-29", "Jam-30", "Jam-31", "Jam-32", "Jam-33", "Jam-34" };
                
    public static final String[] TONE_GROUP_SHORT = new String[] { "a", "b", "i", "r" };
    public static final String[] TONE_GROUP = new String[] { "Preset A", "Preset B", "Internal/Card", "Rhythm" };
    public static final String[] WRITEABLE_TONE_GROUP = new String[] { "Internal/Card" };
    public static final String[] WG_KEYFOLLOW = new String[] { "-1", "-1/2", "-1/4", "0", "1/8", "1/4", "3/8", "1/2", "5/8", "3/4", "7/8", "1", "5/4", "3/2", "2", "S1", "S2" };    
    public static final String[] TVF_KEYFOLLOW = new String[] { "-1", "-1/2", "-1/4", "0", "1/8", "1/4", "3/8", "1/2", "5/8", "3/4", "7/8", "1", "5/4", "3/2", "2" };       
    public static final String[] WG_WAVEFORM = new String[] { "Square", "Sawtooth" };       
    public static final String[] NOTES = new String[] { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
        
    public static final ImageIcon[] STRUCTURE_ICONS = 
        {
        new ImageIcon(RolandD110Tone.class.getResource("Structure1.png")),
        new ImageIcon(RolandD110Tone.class.getResource("Structure2.png")),
        new ImageIcon(RolandD110Tone.class.getResource("Structure3.png")),
        new ImageIcon(RolandD110Tone.class.getResource("Structure4.png")),
        new ImageIcon(RolandD110Tone.class.getResource("Structure5.png")),
        new ImageIcon(RolandD110Tone.class.getResource("Structure6.png")),
        new ImageIcon(RolandD110Tone.class.getResource("Structure7.png")),
        new ImageIcon(RolandD110Tone.class.getResource("Structure8.png")),
        new ImageIcon(RolandD110Tone.class.getResource("Structure9.png")),
        new ImageIcon(RolandD110Tone.class.getResource("Structure10.png")),
        new ImageIcon(RolandD110Tone.class.getResource("Structure11.png")),
        new ImageIcon(RolandD110Tone.class.getResource("Structure12.png")),
        new ImageIcon(RolandD110Tone.class.getResource("Structure13.png"))
        };


    ///// LOCATIONS
    /////
    ///// part is the temporary memory tone which is considered to be the "current patch"

    public static final int PART_1 = 0;
    public static final int PART_2 = 1;
    public static final int PART_3 = 2;
    public static final int PART_4 = 3;
    public static final int PART_5 = 4;
    public static final int PART_6 = 5;
    public static final int PART_7 = 6;
    public static final int PART_8 = 7;
    public static final int PART_9 = 8;
    int part = PART_1;
    boolean altLayout = false;
    
    public static final String ALT_LAYOUT_KEY = "AltLayout";
        
    public static final int TEMP_TIMBRE_LENGTH = 16;  
    // Sysex dumps from the part are TEMP_TONE_LENGTH long
    public static final int TEMP_TONE_LENGTH = 246;  
    // Sysex dumps from a RAM slot are MEMORY_TONE_LENGTH long
    public static final int MEMORY_TONE_LENGTH = 256;  

    public RolandD110Tone()
        {
        String m = getLastX(ALT_LAYOUT_KEY, getSynthClassName());
        altLayout = (m == null ? false : Boolean.parseBoolean(m));
        
        for(int i = 0; i < allPartialParameters.length; i++)
            {
            allPartialParametersToIndex.put(allPartialParameters[i], Integer.valueOf(i));
            }

        for(int i = 0; i < allCommonParameters.length; i++)
            {
            allCommonParametersToIndex.put(allCommonParameters[i], Integer.valueOf(i));
            }

        if (altLayout)
            {
            JComponent sourcePanel = new SynthPanel(this);
            VBox vbox = new VBox();
            HBox hbox = new HBox();
            hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
            hbox.addLast(addGlobal(Style.COLOR_C()));
            vbox.add(hbox);
            hbox = new HBox();
            hbox.add(addWaveGroup(1, Style.COLOR_A()));
            hbox.addLast(addWaveGroup(2, Style.COLOR_B()));
            vbox.add(hbox);
            hbox = new HBox();
            hbox.add(addWaveGroup(3, Style.COLOR_A()));
            hbox.addLast(addWaveGroup(4, Style.COLOR_B()));
            vbox.add(hbox);
            sourcePanel.add(vbox, BorderLayout.CENTER);
            addTab("Wave Group", sourcePanel);                

            sourcePanel = new SynthPanel(this);
            vbox = new VBox();
            hbox = new HBox();
            hbox.add(addPitch(1, Style.COLOR_A()));
            hbox.addLast(addPitch(2, Style.COLOR_B()));
            vbox.add(hbox);
            vbox.add(addPitchEnvelope(1, Style.COLOR_A()));
            vbox.add(addPitchEnvelope(2, Style.COLOR_B()));
            hbox = new HBox();
            hbox.add(addPitch(3, Style.COLOR_A()));
            hbox.addLast(addPitch(4, Style.COLOR_B()));
            vbox.add(hbox);
            vbox.add(addPitchEnvelope(3, Style.COLOR_A()));
            vbox.add(addPitchEnvelope(4, Style.COLOR_B()));
            sourcePanel.add(vbox, BorderLayout.CENTER);
            addTab("Pitch", sourcePanel);                


            sourcePanel = new SynthPanel(this);
            vbox = new VBox();
            hbox = new HBox();
            hbox.add(addFilter(1, Style.COLOR_A()));
            hbox.addLast(addFilter(2, Style.COLOR_B()));
            vbox.add(hbox);
            vbox.add(addFilterEnvelope(1, Style.COLOR_A()));
            vbox.add(addFilterEnvelope(2, Style.COLOR_B()));
            hbox = new HBox();
            hbox.add(addFilter(3, Style.COLOR_A()));
            hbox.addLast(addFilter(4, Style.COLOR_B()));
            vbox.add(hbox);
            vbox.add(addFilterEnvelope(3, Style.COLOR_A()));
            vbox.add(addFilterEnvelope(4, Style.COLOR_B()));
            sourcePanel.add(vbox, BorderLayout.CENTER);
            addTab("Filter", sourcePanel);                

            sourcePanel = new SynthPanel(this);
            vbox = new VBox();
            hbox = new HBox();
            hbox.add(addAmplifier(1, Style.COLOR_A()));
            hbox.addLast(addAmplifier(2, Style.COLOR_B()));
            vbox.add(hbox);
            vbox.add(addAmplifierEnvelope(1, Style.COLOR_A()));
            vbox.add(addAmplifierEnvelope(2, Style.COLOR_B()));
            hbox = new HBox();
            hbox.add(addAmplifier(3, Style.COLOR_A()));
            hbox.addLast(addAmplifier(4, Style.COLOR_B()));
            vbox.add(hbox);
            vbox.add(addAmplifierEnvelope(3, Style.COLOR_A()));
            vbox.add(addAmplifierEnvelope(4, Style.COLOR_B()));
            sourcePanel.add(vbox, BorderLayout.CENTER);
            addTab("Amplifier", sourcePanel);                
            }
        else
            {
            JComponent sourcePanel = new SynthPanel(this);
            VBox vbox = new VBox();
            HBox hbox = new HBox();
            hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
            hbox.add(addGlobal(Style.COLOR_A()));
            hbox.addLast(addWaveGroup(1, Style.COLOR_A()));
            vbox.add(hbox);

            HBox hbox2 = new HBox();
            hbox2.add(addPitch(1, Style.COLOR_A()));
            hbox2.addLast(addFilter(1, Style.COLOR_B()));
            vbox.add(hbox2);
            vbox.add(addPitchEnvelope(1, Style.COLOR_A()));
            vbox.add(addFilterEnvelope(1, Style.COLOR_B()));
            vbox.add(addAmplifier(1, Style.COLOR_C()));
            vbox.add(addAmplifierEnvelope(1, Style.COLOR_C()));

            sourcePanel = new SynthPanel(this);
            sourcePanel.add(vbox, BorderLayout.CENTER);
            //        ((SynthPanel)sourcePanel).makePasteable("p" + 1);
            ((SynthPanel)sourcePanel).makePasteable("p");
            addTab("Common and Partial " + 1, sourcePanel);                

            for(int i = 2; i < 5; i++)
                {
                sourcePanel = new SynthPanel(this);
                vbox = new VBox();
                
                vbox.add(addWaveGroup(i, Style.COLOR_A()));
                
                hbox2 = new HBox();
                hbox2.add(addPitch(i, Style.COLOR_A()));
                hbox2.addLast(addFilter(i, Style.COLOR_B()));
                vbox.add(hbox2);
                vbox.add(addPitchEnvelope(i, Style.COLOR_A()));
                vbox.add(addFilterEnvelope(i, Style.COLOR_B()));
                vbox.add(addAmplifier(i, Style.COLOR_C()));
                vbox.add(addAmplifierEnvelope(i, Style.COLOR_C()));

                sourcePanel = new SynthPanel(this);
                sourcePanel.add(vbox, BorderLayout.CENTER);
                //            ((SynthPanel)sourcePanel).makePasteable("p" + i);
                ((SynthPanel)sourcePanel).makePasteable("p");
                addTab("Partial " + i, sourcePanel);
                }
            }

        model.set("name", "Init Patch");  // has to be 10 long
        model.set("number", 0);
        model.set("bank", 2);           // Internal
        loadDefaults();        
        }
                
                
    public JFrame sprout()
        {
        JFrame frame = super.sprout();
        addD110ToneMenu();
        return frame;
        }         

    public void addD110ToneMenu()
        {
        JMenu menu = new JMenu("D-110");
        menubar.add(menu);
        final JCheckBoxMenuItem altLayoutMenu = new JCheckBoxMenuItem("Alternate Layout");
        altLayoutMenu.setSelected(altLayout);
        altLayoutMenu.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                setLastX("" + altLayoutMenu.isSelected(), ALT_LAYOUT_KEY, getSynthClassName(), true);
                }
            });
        menu.add(altLayoutMenu);

        JMenuItem showCurrentMultiPatchMenu = new JMenuItem("Show Current Multi Patch");
        showCurrentMultiPatchMenu.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                final RolandD110Multi synth = new RolandD110Multi();
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
        JMenuItem writeMultiPatchesMenu = new JMenuItem("Write Multi Patches, One per Tone");
        writeMultiPatchesMenu.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                disableMenuBar();
                JComboBox combo = new JComboBox(TONE_GROUP);
                combo.setSelectedIndex(2);  // Internal/Card
                boolean result = Synth.showMultiOption(RolandD110Tone.this, 
                    new String[] { "Tone Group" },  
                    new JComponent[] { combo }, 
                    "Write Multi Patches", 
                    "<html>Select the Tone Group to write Multi Patches for.<br><br>" + 
                    "<font size=-2><font color=red><b>Warning:</b></font> continuing will overwrite all Multi Patches on your D-110.<br>" + 
                    "This action will freeze Edisyn for about 13 seconds.  Hang tight.</font></html>");
                enableMenuBar();
                if (result) writeMultiPatches(combo.getSelectedIndex());
                }
            });
        menu.add(writeMultiPatchesMenu);
        menu.addSeparator();
        ButtonGroup g = new ButtonGroup();
        for(int i = 0; i < 8; i++)
            {
            final int _i = i;
            JRadioButtonMenuItem m = new JRadioButtonMenuItem("Current Patch is Part " + (i + 1));
            if (i == 0)
                m.setSelected(true);
            m.addActionListener(new ActionListener()
                {
                public void actionPerformed(ActionEvent e)
                    {
                    part = _i;
                    }
                });
            g.add(m);
            menu.add(m);
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
            final RolandD110Multi synth = new RolandD110Multi();
            synth.tuple = new Midi.Tuple(tuple, synth.buildInReceiver(), synth.buildKeyReceiver(), synth.buildKey2Receiver());
            if (synth.tuple != null)
                {
                synth.loadDefaults();
                
                for(int i = 1; i <= 8; i++)
                    {
                    synth.getModel().set("p" + i + "outputlevel", 100);
                    
                    if (timbre1)
                        {
                        // turn off everybody
                        synth.getModel().set("p" + i + "midichannel", RolandD110Multi.MIDI_CHANNEL_OFF);
                        synth.getModel().set("p" + i + "partialreserve", 0);
                        }
                    else
                        {
                        // turn on everybody, sharing equally
                        synth.getModel().set("p" + i + "midichannel", i);                       // Thus Patch 1 has Midi Channel 2
                        synth.getModel().set("p" + i + "partialreserve", 4);
                        }
                    }
                
                // prepare timbre1
                if (timbre1)
                    {
                    synth.getModel().set("p" + (part + 1) + "midichannel", getChannelOut());
                    synth.getModel().set("p" + (part + 1) + "partialreserve", 32);
                    }
            
                // turn off rhythm
                synth.getModel().set("rhythmmidichannel", RolandD110Multi.MIDI_CHANNEL_OFF);
                synth.getModel().set("rhythmoutputlevel", 0);
                
                synth.sendAllParameters();
                sendAllParameters();
                }
            }
        }
    
    // Write 64 Multi patches, each of which points to the corresponding tone
    // in its timbre 1.
        
    public void writeMultiPatches(int bank)
        {
        if (tuple == null)
            if (!setupMIDI())
                return;

        if (tuple != null)
            {
            final RolandD110Multi synth = new RolandD110Multi();
            synth.tuple = new Midi.Tuple(tuple, synth.buildInReceiver(), synth.buildKeyReceiver(), synth.buildKey2Receiver());
            
            // we need to set me to be the active synth because the little confirmation window that
            // pops up prior to this causes me to NOT be the active synth, grrr...
            synth.setActiveSynth(true);
            if (synth.tuple != null)
                {
                for(int p = 0; p < 64; p++)
                    {
                    synth.loadDefaults();
                                
                    for(int i = 1; i <= 8; i++)
                        {
                        synth.getModel().set("p" + i + "midichannel", RolandD110Multi.MIDI_CHANNEL_OFF);
                        synth.getModel().set("p" + i + "partialreserve", 0);
                        synth.getModel().set("p" + i + "outputlevel", 100);
                        }
                                                
                    synth.getModel().set("p1midichannel", getChannelOut());
                    synth.getModel().set("p1partialreserve", 32);
                    synth.getModel().set("p1tonegroup", bank);
                    synth.getModel().set("p1tonenumber", p);
                    synth.getModel().set("name", "Patch " + p);
                    synth.getModel().set("number", p);
                                
                    synth.writeAllParameters(synth.getModel());
                    }
                synth.getModel().set("number", model.get("number"));
                synth.performChangePatch(synth.getModel());
                }
            }
        }
               
    public String getDefaultResourceFileName() { return "RolandD110Tone.init"; }
    public String getHTMLResourceFileName() { return "RolandD110Tone.html"; }

    // There are no banks
    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        JComboBox bank = new JComboBox(writing ? WRITEABLE_TONE_GROUP : TONE_GROUP);
        JTextField number = new SelectedTextField("" + (model.get("number") + 1), 3);
        if (!writing) bank.setSelectedIndex(model.get("bank"));

        while(true)
            {
            boolean result = showMultiOption(this, new String[] { "Bank", "Tone Number"}, 
                new JComponent[] { bank, number }, title, "<html>Enter Bank and Tone Number.<br>These will be updated in Part " + (part + 1) + ".</html>");
                
            if (result == false)
                return false;
                                
            int n;
            try { n = Integer.parseInt(number.getText()); }
            catch (NumberFormatException e)
                {
                showSimpleError(title, "The Tone Number must be an integer 1...64");
                continue;
                }
            if (n < 1 || n > 64)
                {
                showSimpleError(title, "The Tone Number must be an integer 1...64");
                continue;
                }
                
            n--;
            change.set("number", n);
            change.set("bank", writing ? 2 : bank.getSelectedIndex());
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

        hbox.add(Strut.makeHorizontalStrut(70));

        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }


    public JComponent addGlobal( Color color)
        {
        Category category = new Category(this, "Global", color);

        JComponent comp;
        String[] params;
        VBox vbox = new VBox();
        HBox hbox = new HBox();

        comp = new LabelledDial("Structure 1-2", this, "structure1and2", color, 0, 12, -1);
        model.removeMetricMinMax("structure1and2");  // it's a set
        hbox.add(comp);
        
        hbox.add(Strut.makeHorizontalStrut(8));

        comp = new IconDisplay(null, STRUCTURE_ICONS, this, "structure1and2", 106, 80);
        hbox.add(comp);

        hbox.add(Strut.makeHorizontalStrut(8));

        comp = new LabelledDial("Structure 3-4", this, "structure3and4", color, 0, 12, -1);
        model.removeMetricMinMax("structure3and4");  // it's a set
        hbox.add(comp);

        hbox.add(Strut.makeHorizontalStrut(8));

        comp = new IconDisplay(null, STRUCTURE_ICONS, this, "structure3and4", 106, 80);
        hbox.add(comp);
        vbox.add(hbox);
        vbox.add(Strut.makeVerticalStrut(8));

        HBox hbox2 = new HBox();
        comp = new CheckBox("Env No Sustain", this, "envmode");
        ((CheckBox)comp).addToWidth(2);
        hbox2.add(comp);
        comp = new CheckBox("Mute 1", this, "p1mute", true);
        hbox2.add(comp);
        comp = new CheckBox("Mute 2", this, "p2mute", true);
        hbox2.add(comp);
        comp = new CheckBox("Mute 3", this, "p3mute", true);
        hbox2.add(comp);
        comp = new CheckBox("Mute 4", this, "p4mute", true);
        hbox2.add(comp);

        vbox.add(hbox2);

        category.add(vbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addWaveGroup(int partial, Color color)
        {
        Category category = new Category(this, "Wavegroup" + (altLayout ? " " + partial : ""), color);
        //        category.makePasteable("p" + partial);
        category.makePasteable("p");
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = WG_WAVEFORM;
        comp = new Chooser("[S] Synthesizer Waveform", this, "p" + partial + "wgwaveform", params);
        vbox.add(comp);

        params = PCM;
        comp = new Chooser("[P] PCM Wave", this, "p" + partial + "wgpcmwavenumber", params);
        vbox.add(comp);
        
        HBox hbox2 = new HBox();
        comp = new CheckBox("Pitch Bend", this, "p" + partial + "wgpitchbendersw");
        hbox2.add(comp);
        vbox.add(hbox2);

        hbox.add(vbox);

        comp = new LabelledDial("Pulse Width", this, "p" + partial + "wgpulsewidth", color, 0, 100);
        hbox.add(comp);

        comp = new LabelledDial("Pulse Width", this, "p" + partial + "wgpwvelosens", color, 0, 14, 7)
            {
            public boolean isSymmetric() { return true; }
            };
                
        ((LabelledDial)comp).addAdditionalLabel("Velocity Sensitivity");
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addPitch(int partial, Color color)
        {
        Category category = new Category(this, "Pitch" + (altLayout ? " " + partial : ""), color);
        //        category.makePasteable("p" + partial);
        category.makePasteable("p");
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();

        comp = new LabelledDial("Coarse", this, "p" + partial + "wgpitchcoarse", color, 0, 96)
            {
            public String map(int value)
                {
                return NOTES[value % 12] + (value / 12 + 1);
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Fine", this, "p" + partial + "wgpitchfine", color, 0, 100, 50);
        hbox.add(comp);

        comp = new LabelledDial("Keyfollow", this, "p" + partial + "wgpitchkeyfollow", color, 0, 16)
            {
            public String map(int value)
                {
                return WG_KEYFOLLOW[value];
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("LFO Rate", this, "p" + partial + "plforate", color, 0, 100);
        hbox.add(comp);
        
        comp = new LabelledDial("LFO Depth", this, "p" + partial + "plfodepth", color, 0, 100);
        hbox.add(comp);
        
        comp = new LabelledDial("LFO Mod", this, "p" + partial + "plfomodsens", color, 0, 100);
        ((LabelledDial)comp).addAdditionalLabel("Sensitivity");
        hbox.add(comp);
        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addPitchEnvelope(int partial, Color color)
        {
        Category category = new Category(this, "Pitch Envelope" + (altLayout ? " " + partial : ""), color);
        //        category.makePasteable("p" + partial);
        category.makePasteable("p");
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();

        comp = new LabelledDial("Depth", this, "p" + partial + "penvdepth", color, 0, 10);
        hbox.add(comp);
        
        // error in sysex docs, this only goes to 3
        comp = new LabelledDial("Velocity", this, "p" + partial + "penvvelosens", color, 0, 3);
        ((LabelledDial)comp).addAdditionalLabel("Sensitivity");
        hbox.add(comp);
        
        comp = new LabelledDial("Time", this, "p" + partial + "penvtimekeyf", color, 0, 4);
        ((LabelledDial)comp).addAdditionalLabel("Key Follow");
        hbox.add(comp);
        
        comp = new LabelledDial("Level 0", this, "p" + partial + "penvlevel0", color, 0, 100, 50);
        hbox.add(comp);

        comp = new LabelledDial("Time 1", this, "p" + partial + "penvtime1", color, 0, 100);
        hbox.add(comp);

        comp = new LabelledDial("Level 1", this, "p" + partial + "penvlevel1", color, 0, 100, 50);
        hbox.add(comp);

        comp = new LabelledDial("Time 2", this, "p" + partial + "penvtime2", color, 0, 100);
        hbox.add(comp);

        comp = new LabelledDial("Level 2", this, "p" + partial + "penvlevel2", color, 0, 100, 50);
        hbox.add(comp);

        comp = new LabelledDial("Time 3", this, "p" + partial + "penvtime3", color, 0, 100);
        hbox.add(comp);

		// The sysex docs say that this parameter (I believe like the D-10) is fixed to 50.
		// But the manual has this as an actual parameter.
	
          comp = new LabelledDial("Sustain Level", this, "p" + partial + "penvsustainlevel", color, 0, 100, 50);
          hbox.add(comp);

        comp = new LabelledDial("Time 4", this, "p" + partial + "penvtime4", color, 0, 100);
        hbox.add(comp);

        comp = new LabelledDial("End Level", this, "p" + partial + "endlevel", color, 0, 100, 50);
        hbox.add(comp);

        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "p" + partial + "penvtime1", "p" + partial + "penvtime2", "p" + partial + "penvtime3", null, "p" + partial + "penvtime4" },
            new String[] { "p" + partial + "penvlevel0", "p" + partial + "penvlevel1", "p" + partial + "penvlevel2", 

		// The sysex docs say that this parameter (I believe like the D-10) is fixed to 50.
		// But the manual has this as an actual parameter.
            "p" + partial + "penvsustainlevel", "p" + partial + "penvsustainlevel",
            /*null , null,*/ "p" + partial + "endlevel" },
            new double[] { 0, 0.2 / 100.0, 0.2 / 100.0, 0.2 / 100.0, 0.2, 0.2 / 100.0 },
            new double[] { 1.0 / 100.0, 1.0 / 100.0, 1.0 / 100.0, 
            
        // Similarly 
        1.0 / 100.0, 1.0 / 100.0,
        /* 0.5, 0.5, */ 
        1.0 / 100.0 });
            
        ((EnvelopeDisplay)comp).setAxis(1.0 / 100.0 * 50.0);  // is this centered right?
        hbox.addLast(comp);
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addFilter(int partial, Color color)
        {
        Category category = new Category(this, "Filter" + (altLayout ? " " + partial : ""), color);
        //        category.makePasteable("p" + partial);
        category.makePasteable("p");
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();

        comp = new LabelledDial("Cutoff", this, "p" + partial + "tvfcutofffreq", color, 0, 100);
        hbox.add(comp);
        
        comp = new LabelledDial("Resonance", this, "p" + partial + "tvfresonance", color, 0, 30);
        hbox.add(comp);
        
        comp = new LabelledDial("Keyfollow", this, "p" + partial + "tvfkeyfollow", color, 0, 14)
            {
            public String map(int value)
                {
                return TVF_KEYFOLLOW[value];
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Bias Point", this, "p" + partial + "tvfbiaspoint", color, 0, 127)
            {
            public boolean isSymmetric() { return true; }

            public String map(int value)
                {
                if (value < 64)
                    {
                    if (value == 0)
                        return "<A1";
                    else if (value == 1)
                        return "<A#1";
                    else if (value == 2)
                        return "<B1";
                    else
                        return "<" + NOTES[(value  - 3) % 12] + ((value - 3) / 12 + 2);
                    }
                else
                    {
                    value -= 64;
                    if (value == 0)
                        return ">A1";
                    else if (value == 1)
                        return ">A#1";
                    else if (value == 2)
                        return ">B1";
                    else
                        return ">" + NOTES[(value  - 3) % 12] + ((value - 3) / 12 + 2);
                    }
                }
            };
        hbox.add(comp);
        
        comp = new LabelledDial("Bias Level", this, "p" + partial + "tvfbiaslevel", color, 0, 14, 7)
            {
            public boolean isSymmetric() { return true; }
            };
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addFilterEnvelope(int partial, Color color)
        {
        Category category = new Category(this, "Filter Envelope"+ (altLayout ? " " + partial : ""), color);
        //        category.makePasteable("p" + partial);
        category.makePasteable("p");
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();

        comp = new LabelledDial("Depth", this, "p" + partial + "tvfenvdepth", color, 0, 100);
        hbox.add(comp);
        
        comp = new LabelledDial("Velocity", this, "p" + partial + "tvfenvvelosens", color, 0, 100);
        ((LabelledDial)comp).addAdditionalLabel("Sensitivity");
        hbox.add(comp);
        
        comp = new LabelledDial("Depth", this, "p" + partial + "tvfenvdepthkeyfollow", color, 0, 4);
        ((LabelledDial)comp).addAdditionalLabel("Key Follow");
        hbox.add(comp);
        
        comp = new LabelledDial("Time", this, "p" + partial + "tvfenvtimekeyfollow", color, 0, 4);
        ((LabelledDial)comp).addAdditionalLabel("Key Follow");
        hbox.add(comp);
        
        comp = new LabelledDial("Time 1", this, "p" + partial + "tvfenvtime1", color, 0, 100);
        hbox.add(comp);

        comp = new LabelledDial("Level 1", this, "p" + partial + "tvfenvlevel1", color, 0, 100);
        hbox.add(comp);

        comp = new LabelledDial("Time 2", this, "p" + partial + "tvfenvtime2", color, 0, 100);
        hbox.add(comp);

        comp = new LabelledDial("Level 2", this, "p" + partial + "tvfenvlevel2", color, 0, 100);
        hbox.add(comp);

        comp = new LabelledDial("Time 3", this, "p" + partial + "tvfenvtime3", color, 0, 100);
        hbox.add(comp);

        comp = new LabelledDial("Level 3", this, "p" + partial + "tvfenvlevel3", color, 0, 100);
        hbox.add(comp);

        comp = new LabelledDial("Time 4", this, "p" + partial + "tvfenvtime4", color, 0, 100);
        hbox.add(comp);

        comp = new LabelledDial("Sustain Level", this, "p" + partial + "tvfenvsustainlevel", color, 0, 100);
        hbox.add(comp);

        comp = new LabelledDial("Time 5", this, "p" + partial + "tvfenvtime5", color, 0, 100);
        hbox.add(comp);

        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "p" + partial + "tvfenvtime1", "p" + partial + "tvfenvtime2", "p" + partial + "tvfenvtime3", "p" + partial + "tvfenvtime4", null, "p" + partial + "tvfenvtime5" },
            new String[] { null, "p" + partial + "tvfenvlevel1", "p" + partial + "tvfenvlevel2", "p" + partial + "tvfenvlevel3", "p" + partial + "tvfenvsustainlevel", "p" + partial + "tvfenvsustainlevel", null },
            new double[] { 0, 0.2 / 100.0, 0.2 / 100.0, 0.2 / 100.0, 0.2 / 100.0, 0.2, 0.2 / 100.0 },
            new double[] { 0, 1.0 / 100.0, 1.0 / 100.0, 1.0 / 100.0, 1.0 / 100.0, 1.0 / 100.0, 0 });
            
        hbox.addLast(comp);
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }



    public JComponent addAmplifier(int partial, Color color)
        {
        Category category = new Category(this, "Amplifier" + (altLayout ? " " + partial : ""), color);
        //        category.makePasteable("p" + partial);
        category.makePasteable("p");
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();

        comp = new LabelledDial("Level", this, "p" + partial + "tvalevel", color, 0, 100);
        hbox.add(comp);
        
        comp = new LabelledDial("Velocity", this, "p" + partial + "tvavelosens", color, 0, 100, 50);
        ((LabelledDial)comp).addAdditionalLabel("Sensitivity");
        hbox.add(comp);

        comp = new LabelledDial("Bias Point 1", this, "p" + partial + "tvabiaspoint1", color, 0, 127)
            {
            public boolean isSymmetric() { return true; }
                
            public String map(int value)
                {
                if (value < 64)
                    {
                    if (value == 0)
                        return "<A1";
                    else if (value == 1)
                        return "<A#1";
                    else if (value == 2)
                        return "<B1";
                    else
                        return "<" + NOTES[(value  - 3) % 12] + ((value - 3) / 12 + 2);
                    }
                else
                    {
                    value -= 64;
                    if (value == 0)
                        return ">A1";
                    else if (value == 1)
                        return ">A#1";
                    else if (value == 2)
                        return ">B1";
                    else
                        return ">" + NOTES[(value  - 3) % 12] + ((value - 3) / 12 + 2);
                    }
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Bias Level 1", this, "p" + partial + "tvabiaslevel1", color, 0, 12, 12)
            {
            public int getDefaultValue()
                {
                return 12;
                }
                                
            public double getStartAngle()
                {
                return 180;
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Bias Point 2", this, "p" + partial + "tvabiaspoint2", color, 0, 127)
            {
            public boolean isSymmetric() { return true; }
                
            public String map(int value)
                {
                if (value < 64)
                    {
                    if (value == 0)
                        return "<A1";
                    else if (value == 1)
                        return "<A#1";
                    else if (value == 2)
                        return "<B1";
                    else
                        return "<" + NOTES[(value  - 3) % 12] + ((value - 3) / 12 + 2);
                    }
                else
                    {
                    value -= 64;
                    if (value == 0)
                        return ">A1";
                    else if (value == 1)
                        return ">A#1";
                    else if (value == 2)
                        return ">B1";
                    else
                        return ">" + NOTES[(value  - 3) % 12] + ((value - 3) / 12 + 2);
                    }
                }
            };
        hbox.add(comp);        

        comp = new LabelledDial("Bias Level 2", this, "p" + partial + "tvabiaslevel2", color, 0, 12, 12)
            {
            public int getDefaultValue()
                {
                return 12;
                }
                                
            public double getStartAngle()
                {
                return 180;
                }
            };
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addAmplifierEnvelope(int partial, Color color)
        {
        Category category = new Category(this, "Amplifier Envelope" + (altLayout ? " " + partial : ""), color);
        //        category.makePasteable("p" + partial);
        category.makePasteable("p");
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();

        comp = new LabelledDial("Time", this, "p" + partial + "tvaenvtimekeyfollow", color, 0, 4);
        ((LabelledDial)comp).addAdditionalLabel("Key Follow");
        hbox.add(comp);
        
        comp = new LabelledDial("Time 1", this, "p" + partial + "tvaenvtime1velfollow", color, 0, 4);
        ((LabelledDial)comp).addAdditionalLabel("Velocity Follow");
        hbox.add(comp);
        
        comp = new LabelledDial("Time 1", this, "p" + partial + "tvaenvtime1", color, 0, 100);
        hbox.add(comp);

        comp = new LabelledDial("Level 1", this, "p" + partial + "tvaenvlevel1", color, 0, 100);
        hbox.add(comp);

        comp = new LabelledDial("Time 2", this, "p" + partial + "tvaenvtime2", color, 0, 100);
        hbox.add(comp);

        comp = new LabelledDial("Level 2", this, "p" + partial + "tvaenvlevel2", color, 0, 100);
        hbox.add(comp);

        comp = new LabelledDial("Time 3", this, "p" + partial + "tvaenvtime3", color, 0, 100);
        hbox.add(comp);

        comp = new LabelledDial("Level 3", this, "p" + partial + "tvaenvlevel3", color, 0, 100);
        hbox.add(comp);

        comp = new LabelledDial("Time 4", this, "p" + partial + "tvaenvtime4", color, 0, 100);
        hbox.add(comp);

        comp = new LabelledDial("Sustain Level", this, "p" + partial + "tvaenvsustainlevel", color, 0, 100);
        hbox.add(comp);

        comp = new LabelledDial("Time 5", this, "p" + partial + "tvaenvtime5", color, 0, 100);
        hbox.add(comp);

        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "p" + partial + "tvaenvtime1", "p" + partial + "tvaenvtime2", "p" + partial + "tvaenvtime3", "p" + partial + "tvaenvtime4", null, "p" + partial + "tvaenvtime5" },
            new String[] { null, "p" + partial + "tvaenvlevel1", "p" + partial + "tvaenvlevel2", "p" + partial + "tvaenvlevel3", "p" + partial + "tvaenvsustainlevel", "p" + partial + "tvaenvsustainlevel", null },
            new double[] { 0, 0.2 / 100.0, 0.2 / 100.0, 0.2 / 100.0, 0.2 / 100.0, 0.2, 0.2 / 100.0 },
            new double[] { 0, 1.0 / 100.0, 1.0 / 100.0, 1.0 / 100.0, 1.0 / 100.0, 1.0 / 100.0, 0 });
            
        hbox.addLast(comp);
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }



    // these don't have their "p1..." etc. attached
    HashMap allPartialParametersToIndex = new HashMap();
    final static String[] allPartialParameters = new String[]
    {
    "wgpitchcoarse",
    "wgpitchfine",
    "wgpitchkeyfollow",
    "wgpitchbendersw",
    "wgwaveform",                                       /// *** this one has to be handled specially.
    "wgpcmwavenumber",                          /// *** this one has to be handled specially.
    "wgpulsewidth",
    "wgpwvelosens",
    "penvdepth",
    "penvvelosens",
    "penvtimekeyf",
    "penvtime1",
    "penvtime2",
    "penvtime3",
    "penvtime4",
    "penvlevel0",
    "penvlevel1",
    "penvlevel2",
		// The sysex docs say that this parameter (I believe like the D-10) is fixed to 50.
		// But the manual has this as an actual parameter.

    "penvsustainlevel",                       // The MT-32 has this but not the D-110
    //"-",
    "endlevel",
    "plforate",
    "plfodepth",
    "plfomodsens",
    "tvfcutofffreq",
    "tvfresonance",
    "tvfkeyfollow",
    "tvfbiaspoint",
    "tvfbiaslevel",
    "tvfenvdepth",
    "tvfenvvelosens",
    "tvfenvdepthkeyfollow",
    "tvfenvtimekeyfollow",
    "tvfenvtime1",
    "tvfenvtime2",
    "tvfenvtime3",
    "tvfenvtime4",
    "tvfenvtime5",
    "tvfenvlevel1",
    "tvfenvlevel2",
    "tvfenvlevel3",
    "tvfenvsustainlevel",
    "tvalevel",
    "tvavelosens",
    "tvabiaspoint1",
    "tvabiaslevel1",
    "tvabiaspoint2",
    "tvabiaslevel2",
    "tvaenvtimekeyfollow",
    "tvaenvtime1velfollow",                      // note the 1
    "tvaenvtime1",
    "tvaenvtime2",
    "tvaenvtime3",
    "tvaenvtime4",
    "tvaenvtime5",
    "tvaenvlevel1",
    "tvaenvlevel2",
    "tvaenvlevel3",
    "tvaenvsustainlevel",
    };
    
    
    HashMap allCommonParametersToIndex = new HashMap();
        
    final static String[] allCommonParameters = new String[]
    {
    "name",                                        ///// **** Has to be handled specially
    "structure1and2",
    "structure3and4",
    "p1mute",
    "p2mute",
    "p3mute",
    "p4mute",
    "envmode",
    };

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
    
    
    // Builds a byte[] consisting of the data associated with the given key,
    // which can be then inserted into an emit
    public byte[] getData(String key)
        {
        if (key.endsWith("mute"))
            {
            return new byte[]
                {
                (byte)(
                    (model.get("p4mute") << 3) |
                    (model.get("p3mute") << 2) |
                    (model.get("p2mute") << 1) |
                    (model.get("p1mute") << 0))             
                };
            }
        else if (key.equals("name"))                                // name is 10-byte
            {
            byte[] data = new byte[10];
            String name = model.get(key, "Untitled");
            for(int i = 0; i < name.length(); i++)
                {
                data[i] = (byte)(name.charAt(i));
                }
            return data;
            }
        // We need to move the high bit from wgpcmwavenumber to bit #2 in wgwaveform.
        else if (key.endsWith("wgwaveform") || key.endsWith("wgpcmwavenumber"))
            {
            int partial = (int)(key.charAt(1) - '0');       // 1...8
            int wf = model.get("p" + partial + "wgwaveform", 0);
            int wn = model.get("p" + partial + "wgpcmwavenumber", 0);
            int bnk = wn / 128;
            int num = wn % 128;
            
            // MSB is first
            byte wfbank = (byte)((bnk << 1) | wf);
            byte pcmnum = (byte)(num);
            return new byte[] { wfbank, pcmnum };
            }
        else
            {
            return new byte[] { (byte) model.get(key) };
            }
        }



    public byte[] emit(String key)
        {
        if (key.equals("number")) return new byte[0];  // this is not emittable
        if (key.equals("bank")) return new byte[0];  // this is not emittable
                
        byte AA = (byte)(0x04);
        int loc = part * TEMP_TONE_LENGTH;
        byte BB = (byte)((loc >>> 7) & 127);
        byte CC = (byte)(loc & 127);
        
        // figure out the address

        if (key.endsWith("mute"))
            {
            CC += (byte)0x0C;
            if (CC < 0) { CC = (byte)(CC & 127); BB += 1; }
            }
        else if (key.startsWith("p1"))
            {
            CC = (byte)(CC + 0x0E);
            if (CC < 0) { CC = (byte)(CC & 127); BB += 1; }
            
            if (key.endsWith("wgwaveform") || key.endsWith("wgpcmwavenumber"))
                {
                CC += (byte)0x04;               // we'll start at wgwaveform and do both of them
                if (CC < 0) { CC = (byte)(CC & 127); BB += 1; }
                }
            else
                {
                int offset = CC + ((Integer)(allPartialParametersToIndex.get(key.substring(2)))).intValue();  // get rid of the "p1"
                BB += (byte)(offset / 128);
                CC = (byte)(offset % 128);
                }
            }
        else if (key.startsWith("p2"))
            {
            CC = (byte)(CC + 0x48);
            if (CC < 0) { CC = (byte)(CC & 127); BB += 1; }
            
            if (key.endsWith("wgwaveform") || key.endsWith("wgpcmwavenumber"))
                {
                CC += (byte)0x04;               // we'll start at wgwaveform and do both of them
                if (CC < 0) { CC = (byte)(CC & 127); BB += 1; }
                }
            else
                {
                int offset = CC + ((Integer)(allPartialParametersToIndex.get(key.substring(2)))).intValue();  // get rid of the "p1"
                BB += (byte)(offset / 128);
                CC = (byte)(offset % 128);
                }
            }
        else if (key.startsWith("p3"))
            {
            BB = (byte)(BB + 0x01);
            CC = (byte)(CC + 0x02);
            if (CC < 0) { CC = (byte)(CC & 127); BB += 1; }
            
            if (key.endsWith("wgwaveform") || key.endsWith("wgpcmwavenumber"))
                {
                CC += (byte)0x04;               // we'll start at wgwaveform and do both of them
                if (CC < 0) { CC = (byte)(CC & 127); BB += 1; }
                }
            else
                {
                int offset = CC + ((Integer)(allPartialParametersToIndex.get(key.substring(2)))).intValue();  // get rid of the "p1"
                BB += (byte)(offset / 128);
                CC = (byte)(offset % 128);
                }
            }
        else if (key.startsWith("p4"))
            {
            BB = (byte)(BB + 0x01);
            CC = (byte)(CC + 0x3C);
            if (CC < 0) { CC = (byte)(CC & 127); BB += 1; }
            
            if (key.endsWith("wgwaveform") || key.endsWith("wgpcmwavenumber"))
                {
                CC += (byte)0x04;               // we'll start at wgwaveform and do both of them
                if (CC < 0) { CC = (byte)(CC & 127); BB += 1; }
                }
            else
                {
                int offset = CC + ((Integer)(allPartialParametersToIndex.get(key.substring(2)))).intValue();  // get rid of the "p1"
                BB += (byte)(offset / 128);
                CC = (byte)(offset % 128);
                }
            }
        else                // Common
            {
            if (key.equals("name"))
                {
                // do nothing, we're at the right spot
                }
            else if (key.equals("envmode"))
                {
                CC = (byte)0x0D;
                }
            else
                {
                // The first parameter will be 1 (patchname is 0).  So we need to skip to 0x0A - 1
                CC += (byte)(0x0A - 1);
                CC = (byte)(CC + ((Integer)(allCommonParametersToIndex.get(key))).intValue());
                if (CC < 0) { CC = (byte)(CC & 127); BB += 1; }
                }
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
        else if (payload.length == 2)
            {
            byte checksum = produceChecksum(new byte[] { AA, BB, CC, payload[0], payload[1] });
            return new byte[] { (byte)0xF0, (byte)0x41, getID(), (byte)0x16, (byte)0x12, AA, BB, CC, payload[0], payload[1], checksum, (byte)0xF7 };
            }
        else                                                                                    // Some data is 1-byte
            {
            byte checksum = produceChecksum(new byte[] { AA, BB, CC, payload[0] });
            return new byte[] { (byte)0xF0, (byte)0x41, getID(), (byte)0x16, (byte)0x12, AA, BB, CC, payload[0], checksum, (byte)0xF7 };
            }
        }
   
    // We send parameters after a parse because you can't do a program change to change
    // to a diffrent tone.
    public boolean getSendsParametersAfterNonMergeParse()
        {
        return true;
        } 
    
    public int parse(byte[] data, boolean fromFile)
        {
        // What is the tone patch number?
        int AA = data[5];
        int BB = data[6];
        int CC = data[7];
        
        if (AA == 0x08)
            {
            model.set("number", BB / 2);
            model.set("bank", 2);           // internal
            }
        
        int pos = 8;
        String name = "";
        for(int i = 0; i < 10; i++)
            {
            name = name + ((char)data[pos++]);
            }
        model.set("name", name);
        model.set("structure1and2", data[pos++]);
        model.set("structure3and4", data[pos++]);
        model.set("p1mute", (data[pos] >>> 0) & 1);
        model.set("p2mute", (data[pos] >>> 1) & 1);
        model.set("p3mute", (data[pos] >>> 2) & 1);
        model.set("p4mute", (data[pos] >>> 3) & 1);
        pos++;
        model.set("envmode", data[pos++]);
                    
        // partials
        for(int t = 1; t < 5; t++)
            {
            for(int i = 0; i < allPartialParameters.length; i++)
                {
                if (allPartialParameters[i].equals("-"))
                    {
                    pos++;
                    }
                else if (allPartialParameters[i].endsWith("wgpcmwavenumber"))
                    {
                    model.set("p" + t + "wgpcmwavenumber", data[pos] | ((data[pos-1] >>> 1) << 7));
                    pos++;
                    }
                else if (allPartialParameters[i].endsWith("wgwaveform"))
                    {
                    model.set("p" + t + "wgwaveform", data[pos++] & 0x01);
                    }
                else
                    {
                    model.set("p" + t + allPartialParameters[i], data[pos++]);
                    }
                }
            }
        revise();
        return PARSE_SUCCEEDED;
        }
    
    
    // If toWorkingMemory, then we emit to the given part.
    // othewise we emit to a RAM location.  For reasons I cannot explain, the length
    // of Tones in RAM locations are 10 bytes longer than those in temporary memory.
        
    public byte[] emit(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {             
        if (tempModel == null)
            tempModel = getModel();

        // set up buffer
        byte[] buf = new byte[(toWorkingMemory ? TEMP_TONE_LENGTH : MEMORY_TONE_LENGTH) + 10];          // need 10 extra for the header, checksum, and 0xF7
        
        buf[0] = (byte)0xF0;
        buf[1] = (byte)0x41;
        buf[2] = (byte)getID();
        buf[3] = (byte)0x16;
        buf[4] = (byte)0x12;
        if (toWorkingMemory)
            {
            int loc = part * TEMP_TONE_LENGTH;
            byte LSB = (byte)(loc & 127);
            byte MSB = (byte)((loc >>> 7) & 127);
            buf[5] = (byte)0x04;
            buf[6] = MSB;
            buf[7] = LSB;
            }
        else
            {
            int number = tempModel.get("number", 0);
            buf[5] = (byte) 0x08;
            buf[6] = (byte) (number * 2);
            buf[7] = (byte)(0x00);
            }
                
        // tone common
        int pos = 8;
        byte[] d = getData("name");
        System.arraycopy(d, 0, buf, pos, d.length);
        pos += d.length;
        buf[pos++] = getData("structure1and2")[0];
        buf[pos++] = getData("structure3and4")[0];
        buf[pos++] = getData("p1mute")[0];                      // will be enough for all 4 mutes
        buf[pos++] = getData("envmode")[0];
        
        // tones
        for(int t = 1; t < 5; t++)
            {
            for(int i = 0; i < allPartialParameters.length; i++)
                {
                if (allPartialParameters[i].equals("-"))
                    {
                    buf[pos++] = 0;         // pitch sustain level
                    }
                else if (allPartialParameters[i].endsWith("wgpcmwavenumber")) continue; // we just did wgwaveform, which included this
                else
                    {
                    d = getData("p" + t + allPartialParameters[i]);
                    for(int j = 0; j < d.length; j++)
                        {
                        buf[pos++] = d[j];
                        }
                    }
                }
            }
        buf[buf.length - 2] = produceChecksum(buf, 5, buf.length - 2);
        buf[buf.length - 1] = (byte)0xF7;
        return buf;
        }



//// This editor used to download tones by grabbing them from tone memory.
//// That only permitted grabbing internal/card tones, not presets or rhythm tones.
//// This has been changed (September 2021) to changing the tone bank and number
//// in the part/timbre and then doing a requestCurrentDump.

    public void changePatch(Model tempModel) 
        {
        if (tempModel == null)
            tempModel = getModel();

        int number = tempModel.get("number");
        int bank = tempModel.get("bank");
        
        // Change the tone group and number at the part
        byte AA = (byte)(0x03);
        byte BB = (byte)(part * TEMP_TIMBRE_LENGTH);
        byte CC = (byte)(0x00);

        byte checksum = produceChecksum(new byte[] { AA, BB, CC, (byte)bank, (byte)number });
        byte[] b = new byte[] { (byte)0xF0, (byte)0x41, getID(), (byte)0x16, (byte)0x12, 
            AA, BB, CC, (byte)bank, (byte)number, checksum, (byte)0xF7 }; 
        tryToSendSysex(b);

        model.set("number", number);
        model.set("bank", bank);
        }

	// We have to force a change patch always because we're doing the equivalent of requestCurrentDump here
	public boolean getAlwaysChangesPatchesOnRequestDump() { return true; }

    public byte[] requestDump(Model tempModel)
        {
        return requestCurrentDump();
        }

/*
// Requests a Tone from a specific RAM slot (1...64)
public byte[] requestDump(Model tempModel)
{
if (tempModel == null)
tempModel = getModel();

int number = tempModel.get("number");
byte AA = (byte)(0x08);
byte BB = (byte)(number * 2);
byte CC = (byte)(0x00);
byte LSB = (byte)118;           // 0x76
byte MSB = (byte)1; 
        
byte checksum = produceChecksum(new byte[] { AA, BB, CC, (byte)0x00, LSB, MSB });
byte[] b = new byte[] { (byte)0xF0, (byte)0x41, getID(), (byte)0x16, (byte)0x11, 
AA, BB, CC, (byte)0x00, MSB, LSB, checksum, (byte)0xF7 }; 
return b;
}
*/
   
    // Requests a Tone from the current part
    public byte[] requestCurrentDump()
        {
        byte AA = (byte)(0x04);
        int loc = part * TEMP_TONE_LENGTH;
        byte BB = (byte)((loc >>> 7) & 127);
        byte CC = (byte)(loc & 127);

        // total length is 246.  Not sure why it's not 256
        byte LSB = (byte)118;           // 0x76
        byte MSB = (byte)1; 
        
        byte checksum = produceChecksum(new byte[] { AA, BB, CC, (byte)0x00, LSB, MSB });
        byte[] b = new byte[] { (byte)0xF0, (byte)0x41, getID(), (byte)0x16, (byte)0x11, 
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
        
    public static String getSynthName() { return "Roland D-110 [Tone]"; }
    
    public String getPatchName(Model model) { return model.get("name", "Untitled  "); }

    public int getPauseAfterSendAllParameters() { return 100; } 
 
    public int getPauseAfterSendOneParameter() { return 25; }       // In the 1.07 firmware notes it says "at least 20ms" (http://llamamusic.com/d110/ROM_IC_Bug_Fixes.html).  In my firmware (1.10) the D-110 can handle changes thrown at it full blast, but earlier firmware (1.07) cannot.
        
    public Model getNextPatchLocation(Model model)
        {
        int number = model.get("number");
        int bank = model.get("bank");
        
        number++;
        if (number >= 64)
            {
            number = 0;
            bank++;
            if (bank > 3)
                {
                bank = 0;
                }
            }
                
        Model newModel = buildModel();
        newModel.set("number", number);
        newModel.set("bank", bank);
        return newModel;
        }

    public String getPatchLocationName(Model model)
        {
        // getPatchLocationName() is called from sprout() as a test to see if we should enable
        // batch downloading.  If we haven't yet created an .init file, then parameters won't exist
        // yet and this method will bomb badly.  So we return null in this case.
        if (!model.exists("number")) return null;
        
        return TONE_GROUP_SHORT[model.get("bank")] + 
            (model.get("number") + 1 < 10 ? "0" : "") + ((model.get("number") + 1));
        }

    public int getBatchDownloadWaitTime() { return 275; }

    /** Default is null, which indicates that the patch editor does not support librarians.  */
    public String[] getPatchNumberNames() { return buildIntegerNames(64, 1); }

    /** Return a list of all bank names.  Default is null, indicating no banks are supported.  */
    public String[] getBankNames() { return TONE_GROUP; }       // or TONE_GROUP_SHORT?

    /** Return a list of each bank, indicating which are writeable.  Default is an array, all true, the size of getBankNames(). */
    public boolean[] getWriteableBanks() { return new boolean[] { false, false, true, false }; }

    /** Return whether individual patches can be written.  Default is FALSE. */
    public boolean getSupportsPatchWrites() { return true; }

    /** Return the maximum number of characters a patch name may hold. The default returns 16. */
    public int getPatchNameLength() { return 10; }

    /** Return true if individual (non-bank) patches on the synthesizer contain location information (bank, number). 
        This will matter when reading patches from disk, rather than loading them from the synth, so as to put them
        in the right place. */
    public boolean getPatchContainsLocation() { return true; }

    public boolean librarianTested() { return true; }
    }
