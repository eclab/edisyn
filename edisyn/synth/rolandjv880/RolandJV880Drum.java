/***
    Copyright 2021 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.rolandjv880;

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
import edisyn.util.*;

/**
   A patch editor for the Roland JV-880 and JV-80 (Drums)
        
   @author Sean Luke
*/

public class RolandJV880Drum extends Synth
    {
    public static final String[] BANKS = new String[] { "Internal", "Card" };
    public static final String[] DISPLAYABLE_BANKS = new String[] { "Internal", "Card", "Preset A", "Preset B" };
    public static final String[] WRITABLE_BANKS = new String[] { "Internal", "Card" };
    public static final String[] WAVE_NAMES = new String[] { "Ac Piano 1", "SA Rhodes 1", "SA Rhodes 2", "E. Piano 1", "E. Piano 2", "Clav 1", "Organ 1", "Jazz Organ", "Pipe Organ", "Nylon GTR", "6STR GTR", "GTR HARM", 
        "Mute GTR 1", "Pop Strat", "Stratus", "SYN GTR", "Harp 1", "SYN Bass", "Pick Bass", "E. Bass", "Fretless 1", "Upright BS", "Slap Bass 1", "Slap & Pop", 
        "Slap Bass 2", "Slap Bass 3", "Flute 1", "Trumpet 1", "Trombone 1", "Harmon Mute 1", "Alto Sax 1", "Tenor Sax 1", "French 1", "Blow Pipe", "Bottle", 
        "Trumpet SECT", "ST. Strings-R", "ST. Strings-L", "Mono Strings", "Pizz", "SYN VOX 1", "SYN VOX 2", "Male Ooh", "ORG VOX", "VOX Noise", "Soft Pad", 
        "JP Strings", "Pop Voice", "Fine Wine", "Fantasynth", "Fanta Bell", "ORG Bell", "Agogo", "Bottle Hit", "Vibes", "Marimba wave", "Log Drum", "DIGI Bell 1", "DIGI Chime", 
        "Steel Drums", "MMM VOX", "Spark VOX", "Wave Scan", "Wire String", "Lead Wave", "Synth Saw 1", "Synth Saw 2", "Synth Saw 3", "Synth Square", "Synth Pulse 2", "Synth Pulse 2", 
        "Triangle", "Sine", "ORG Click", "White Noise", "Wind Agogo", "Metal Wind", "Feedbackwave", "Anklungs", "Wind Chimes", "Rattles", "Tin Wave", "Spectrum 1", 
        "808 SNR 1", "90's Snare", "Piccolo SN", "LA Snare", "Whack Snare", "Rim Shot", "Bright Kick", "Verb Kick", "Round Kick", "808 Kick", "Closed HAT 1", "Closed HAT 2", 
        "Open HAT 1", "Crash 1", "Ride 1", "Ride Bell 1", "Power Tom Hi", "Power Tom Lo", "Cross Stick 1", "808 Claps", "Cowbell 1", "Tambourine", "Timbale", "CGA Mute Hi", 
        "CGA Mute Lo", "CGA Slap", "Conga Hi", "Conga Lo", "Maracas", "Cabasa Cut", "Cabasa Up", "Cabasa Down", "REV Steel DR", "REV Tin Wave", "REV SN 1", "REV SN 2", 
        "REV SN 3", "REV SN 4", "REV Kick 1", "REV Cup", "REV Tom", "REV Cow Bell", "REV TAMB", "REV Conga", "REV Maracas", "REV Crash 1" };

    public static final String[] KEYS = new String[] { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
    public static final String[] OUTPUT_SELECTS = new String[] { "Main", "Sub" };
    public static final String[] RANDOM_PITCH_DEPTHS = new String[] { "0", "5", "10", "20", "30", "40", "50", "70", "100", "200", "300", "400", "500", "600", "800", "1200" };
    public static final String[] TIME_SENSES = new String[] { "-100", "-70", "-50", "-40", "-30", "-20", "-10", "0", "10", "20", "30", "40", "50", "70", "100" };
    public static final String[] FILTER_MODES = new String[] { "Off", "LPF", "HPF" };
    public static final String[] RESONANCE_MODES = new String[] { "Soft", "Hard" };
    public static final String[] WAVE_GROUPS = new String[] { "Internal", "Expansion", "PCM" };

    public static final int MAX_WAVE_NUMBER = 256;

    
    public RolandJV880Drum()
        {
        if (drumParametersToIndex == null)
            {
            drumParametersToIndex = new HashMap();
            for(int i = 0; i < drumParameters.length; i++)
                {
                drumParametersToIndex.put(drumParameters[i], Integer.valueOf(i));
                }
            }

        JComponent soundPanel = new SynthPanel(this);
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        //hbox.add(addGlobal(Style.COLOR_A()));
        hbox.addLast(addKeys(Style.COLOR_B()));
        vbox.add(hbox);
        vbox.add(drumDisplay);
        soundPanel.add(vbox);

        addTab("Drum", soundPanel);
        
        model.set("bank", 0);           // internal

        model.setStatus("note", model.STATUS_IMMUTABLE);

        loadDefaults();
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
        comp = new PatchDisplay(this, 8);
        vbox.add(comp);
        hbox.add(vbox);

        // Not enough space to show the title
        hbox.addLast(Strut.makeHorizontalStrut(140));

        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }


    public JComponent addWave(int note, Color color)
        {
        Category category = new Category(this, "Wave", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = WAVE_GROUPS;
        comp = new Chooser("Wave Group", this, "note" + note + "wavegroup", params);
        vbox.add(comp);

        params = new String[MAX_WAVE_NUMBER];
        for(int i = 0 ; i < WAVE_NAMES.length; i++)
            params[i] = "" + (i + 1) + " " + WAVE_NAMES[i];
        for(int i = WAVE_NAMES.length; i < MAX_WAVE_NUMBER; i++)
            params[i] = "" + (i + 1);
                
        comp = new Chooser("Wave Number", this, "note" + note + "wavenumber", params);
        vbox.add(comp);
        hbox.add(vbox);

        vbox = new VBox();
        comp = new CheckBox("Tone Switch", this, "note" + note + "toneswitch");
        vbox.add(comp);

        comp = new LabelledDial("Mute Group", this, "note" + note + "mutegroup", color, 0, 31)
            {
            public String map(int val)
                {
                if (val == 0) return "Off";
                else return "" + val;
                }
            };
        hbox.add(comp);

        comp = new CheckBox("Envelope Sustain", this, "note" + note + "envelopemode");
        vbox.add(comp);
        hbox.add(vbox);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addEffects(int note, Color color)
        {
        Category category = new Category(this, "Effects", color);
        //        category.makePasteable("note" + note);
        category.makePasteable("tone");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = OUTPUT_SELECTS;
        comp = new Chooser("Output Select", this, "note" + note + "outputselect", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Dry Level", this, "note" + note + "drylevel", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Reverb", this, "note" + note + "reverbsendlevel", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Send Level");
        hbox.add(comp);

        comp = new LabelledDial("Chorus", this, "note" + note + "chorussendlevel", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Send Level");
        hbox.add(comp);
                
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addPitch(int note, Color color)
        {
        Category category = new Category(this, "Pitch", color);
        //        category.makePasteable("note" + note);
        category.makePasteable("tone");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        comp = new LabelledDial("Coarse", this, "note" + note + "coarsetune", color, 0, 127)
            {
            public String map(int val)
                {
                return KEYS[val % 12] + (val / 12 + 1);  // note integer division
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Fine", this, "note" + note + "pitchfine", color, 14, 114, 64);
        hbox.add(comp);

        comp = new LabelledDial("Random", this, "note" + note + "randompitchdepth", color, 0, 15)
            {
            public String map(int value)
                {
                return RANDOM_PITCH_DEPTHS[value];
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Pitch Depth");
        hbox.add(comp);

        comp = new LabelledDial("Pitch Bend Range", this, "note" + note + "pitchbendrange", color, 0, 12);
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addPitchEnvelope(int note, Color color)
        {
        Category category = new Category(this, "Pitch Envelope", color);
        //        category.makePasteable("note" + note);
        category.makePasteable("tone");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        comp = new LabelledDial("Velocity", this, "note" + note + "penvvelocitylevelsense", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Level Sense");
                
        hbox.add(comp);

/// IMPORTANT: Note that this is velocity time sense, not velocity ON time sense (as it is in the JV880 editor)
        comp = new LabelledDial("Velocity", this, "note" + note + "penvvelocitytimesense", color, 0, 14)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                return TIME_SENSES[value];
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Time Sense");
        hbox.add(comp);

        comp = new LabelledDial("Depth", this, "note" + note + "penvdepth", color, 52, 76, 64);
        hbox.add(comp);

        comp = new LabelledDial("Time 1", this, "note" + note + "penvtime1", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Level 1", this, "note" + note + "penvlevel1", color, 1, 127, 64);
        hbox.add(comp);

        comp = new LabelledDial("Time 2", this, "note" + note + "penvtime2", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Level 2", this, "note" + note + "penvlevel2", color, 1, 127, 64);
        hbox.add(comp);

        comp = new LabelledDial("Time 3", this, "note" + note + "penvtime3", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Level 3", this, "note" + note + "penvlevel3", color, 1, 127, 64);
        hbox.add(comp);

        comp = new LabelledDial("Time 4", this, "note" + note + "penvtime4", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Level 4", this, "note" + note + "penvlevel4", color, 1, 127, 64);
        hbox.add(comp);

        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "note" + note + "penvtime1", "note" + note + "penvtime2", "note" + note + "penvtime3", null, "note" + note + "penvtime4" },
            new String[] { null, "note" + note + "penvlevel1", "note" + note + "penvlevel2", "note" + note + "penvlevel3", "note" + note + "penvlevel3", "note" + note + "penvlevel4" },
            new double[] { 0, 0.2 / 127.0, 0.2 / 127.0, 0.2 / 127.0, 0.2, 0.2 / 127.0 },
            new double[] { 0, 1.0 / 127.0, 1.0 / 127.0, 1.0 / 127.0, 1.0 / 127.0, 1.0 / 127.0 });
            
        ((EnvelopeDisplay)comp).setAxis(1.0 / 127.0 * 64.0);  // is this centered right?
        hbox.addLast(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }



    public JComponent addFilter(int note, Color color)
        {
        Category category = new Category(this, "Filter (TVF)", color);
        //        category.makePasteable("note" + note);
        category.makePasteable("tone");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
 
        VBox vbox = new VBox();
        params = FILTER_MODES;
        comp = new Chooser("Filter Mode", this, "note" + note + "filtermode", params);
        vbox.add(comp);

        params = RESONANCE_MODES;
        comp = new Chooser("Resonance Mode", this, "note" + note + "resonancemode", params);
        vbox.add(comp);
        
        hbox.add(vbox);

        comp = new LabelledDial("Cutoff", this, "note" + note + "cutoff", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Resonance", this, "note" + note + "resonance", color, 0, 127);
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    JComponent tvfenvdepth = null;
    JComponent tvfenvlevel4 = null;
    public JComponent addFilterEnvelope(int note, Color color)
        {
        Category category = new Category(this, "Filter (TVF) Envelope", color);
        //        category.makePasteable("note" + note);
        category.makePasteable("tone");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        comp = new LabelledDial("Velocity", this, "note" + note + "tvfenvvelocitylevelsense", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Level Sense");
        hbox.add(comp);

/// IMPORTANT: Note that this is velocity time sense, not velocity ON time sense (as it is in the JV880 editor)
        comp = new LabelledDial("Velocity", this, "note" + note + "tvfenvvelocitytimesense", color, 0, 14)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                /// FIXME
                if (value < TIME_SENSES.length)
                    return TIME_SENSES[value];
                else return "??";
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Time Sense");
        hbox.add(comp);

        comp = new LabelledDial("Depth", this, "note" + note + "tvfenvdepth", color, 1, 127, 64);
        tvfenvdepth = comp;
        hbox.add(comp);

        comp = new LabelledDial("Time 1", this, "note" + note + "tvfenvtime1", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Level 1", this, "note" + note + "tvfenvlevel1", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Time 2", this, "note" + note + "tvfenvtime2", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Level 2", this, "note" + note + "tvfenvlevel2", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Time 3", this, "note" + note + "tvfenvtime3", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Level 3", this, "note" + note + "tvfenvlevel3", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Time 4", this, "note" + note + "tvfenvtime4", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Level 4", this, "note" + note + "tvfenvlevel4", color, 0, 127);
        tvfenvlevel4 = comp;
        hbox.add(comp);

        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "note" + note + "tvfenvtime1", "note" + note + "tvfenvtime2", "note" + note + "tvfenvtime3", null, "note" + note + "tvfenvtime4" },
            new String[] { null, "note" + note + "tvfenvlevel1", "note" + note + "tvfenvlevel2", "note" + note + "tvfenvlevel3", "note" + note + "tvfenvlevel3", "note" + note + "tvfenvlevel4" },
            new double[] { 0, 0.2 / 127.0, 0.2 / 127.0, 0.2 / 127.0, 0.2, 0.2 / 127.0 },
            new double[] { 0, 1.0 / 127.0, 1.0 / 127.0, 1.0 / 127.0, 1.0 / 127.0, 1.0 / 127.0 });
        hbox.addLast(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addAmplifier(int note, Color color)
        {
        Category category = new Category(this, "Amplifier (TVA)", color);
        //        category.makePasteable("note" + note);
        category.makePasteable("tone");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
 
        comp = new LabelledDial("Level", this, "note" + note + "level", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Pan", this, "note" + note + "pan", color, 0, 128)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                if (value < 64)
                    {
                    return "L" + (64 - value);
                    }
                else if (value == 64) return "--";
                else if (value < 128)
                    {
                    return "R" + (value - 64);
                    }
                else return "Rand";
                }
            };
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addAmplifierEnvelope(int note, Color color)
        {
        Category category = new Category(this, "Amplifier (TVA) Envelope", color);
        //        category.makePasteable("note" + note);
        category.makePasteable("tone");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        comp = new LabelledDial("Velocity", this, "note" + note + "tvaenvvelocitylevelsense", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Level Sense");
        hbox.add(comp);

/// IMPORTANT: Note that this is velocity time sense, not velocity ON time sense (as it is in the JV880 editor)
        comp = new LabelledDial("Velocity", this, "note" + note + "tvaenvvelocitytimesense", color, 0, 14)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                return TIME_SENSES[value];
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Time Sense");
        hbox.add(comp);


        //comp = new LabelledDial("Depth", this, "note" + note + "tvaenvdepth", color, 1, 127, 64);
        comp = Strut.makeStrut(tvfenvdepth);
        hbox.add(comp);

        comp = new LabelledDial("Time 1", this, "note" + note + "tvaenvtime1", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Level 1", this, "note" + note + "tvaenvlevel1", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Time 2", this, "note" + note + "tvaenvtime2", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Level 2", this, "note" + note + "tvaenvlevel2", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Time 3", this, "note" + note + "tvaenvtime3", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Level 3", this, "note" + note + "tvaenvlevel3", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Time 4", this, "note" + note + "tvaenvtime4", color, 0, 127);
        hbox.add(comp);

        //comp = new LabelledDial("Level 4", this, "note" + note + "tvaenvlevel4", color, 0, 127);
        comp = Strut.makeStrut(tvfenvlevel4);
        hbox.add(comp);

        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "note" + note + "tvaenvtime1", "note" + note + "tvaenvtime2", "note" + note + "tvaenvtime3", null, "note" + note + "tvaenvtime4" },
            new String[] { null, "note" + note + "tvaenvlevel1", "note" + note + "tvaenvlevel2", "note" + note + "tvaenvlevel3", "note" + note + "tvaenvlevel3", null },
            new double[] { 0, 0.2 / 127.0, 0.2 / 127.0, 0.2 / 127.0, 0.2, 0.2 / 127.0 },
            new double[] { 0, 1.0 / 127.0, 1.0 / 127.0, 1.0 / 127.0, 1.0 / 127.0, 0 });
        hbox.addLast(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }



    public VBox buildKey(final int key, Color color)
        {
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        hbox.add(addWave(key, Style.COLOR_A()));
        hbox.addLast(addEffects(key, Style.COLOR_A()));
        vbox.add(hbox);

        hbox = new HBox();
        hbox.add(addPitch(key, Style.COLOR_A()));
        hbox.add(addFilter(key, Style.COLOR_C()));
        hbox.addLast(addAmplifier(key, Style.COLOR_A()));
        vbox.add(hbox);

        vbox.add(addPitchEnvelope(key, Style.COLOR_B()));
        vbox.add(addFilterEnvelope(key, Style.COLOR_C()));
        vbox.add(addAmplifierEnvelope(key, Style.COLOR_A()));
                
        return vbox;
        }


    VBox[] keys = new VBox[61];
    VBox drumDisplay = new VBox();

    public JComponent addKeys(Color color)
        {
        final Category category = new Category(this, "Drum Key", color);
        //        category.makePasteable("key1");
        //        category.makeDistributable("key1");  // pretty useless
        category.makePasteable("key");
        category.makeDistributable("key");  // pretty useless

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        for(int i = 0; i < keys.length; i++)
            {
            keys[i] = buildKey(i, color);
            }
                
        comp = new LabelledDial("Note", this, "note", color, 36, 96)
            {
            public String map(int val)
                {
                return KEYS[val % 12] + ((val / 12) - 1);  // note integer division
                }
            };
        hbox.add(comp);
        
        comp = new KeyDisplay("Note", this, "note", color, 36, 96, 0)
            {
            public void userPressed(int key)
                {
                doSendTestNote(key, false);
                }
            };
        ((KeyDisplay)comp).setDynamicUpdate(true);
        ((KeyDisplay)comp).setOctavesBelowZero(KeyDisplay.OCTAVES_BELOW_ZERO_SPN);
        hbox.add(comp);
                                
        model.register("note", new Updatable()
            {
            public void update(String key, Model model)
                {
                drumDisplay.removeAll();
                drumDisplay.add(keys[model.get(key, 36) - 36]);
                drumDisplay.revalidate();
                drumDisplay.repaint();
                }
            });

        // set it once                  
        model.set("note", model.get("note", 36));
                
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
        

                
    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        String[] banks = BANKS;
        JComboBox bank = new JComboBox(banks);
        int b = model.get("bank");
        if (b >= banks.length)                  // as in "preset a" or "preset b"
            b = 0;
        bank.setSelectedIndex(b);
        
        while(true)
            {
            boolean result = showMultiOption(this, new String[] { "Bank" }, 
                new JComponent[] { bank }, title, "Enter the Bank");
                
            if (result == false)
                return false;
                                
            change.set("bank", bank.getSelectedIndex());
            return true;
            }
        }
        

    public int parse(byte[] data, boolean fromFile)
        {
        // I have set it up here so that we could load ONE or SEVERAL drum sounds from the data        
        
        int offset = 0;
        int note = -1;
        for(offset = 0; offset <= data.length - 63 && data[offset] == (byte)0xF0; offset += 63)
            {
            note = data[offset + 7] - 0x40; // this better be between 0x00 and 0x3C
            if (note < 0 || note > 60)
                { note = -1; break; }
                                
            int pos = offset + 9;
            for(int i = 0; i < drumParameters.length; i++)
                {
                if (drumParameters[i].equals("-"))
                    {
                    pos++;
                    }
                else if (drumParameters[i].equals("wavenumber"))
                    {
                    model.set("note" + note + drumParameters[i], (data[pos] << 4) + data[pos + 1]);
                    pos++;
                    }
                else if (drumParameters[i].equals("pan"))
                    {
                    model.set("note" + note + drumParameters[i], (data[pos] << 4) + data[pos + 1]);
                    pos++;
                    }
                else
                    {
                    model.set("note" + note + drumParameters[i], data[pos]);
                    pos++;
                    }
                }
            }

        revise();
        if (note == -1)
            {
            return PARSE_FAILED;
            }
        else if (note == 0x3C)
            {
            return PARSE_SUCCEEDED;
            }
        else
            {
            return PARSE_INCOMPLETE;
            }
        }
        
    public static String getSynthName() { return "Roland JV-80/880 [Drum]"; }
    
    String defaultResourceFileName = null;
    public String getDefaultResourceFileName() 
        {
        // See the Menu (preset options)
        if (defaultResourceFileName != null)
            return defaultResourceFileName;
        else
            return "RolandJV880Drum.init"; 
        }
        
    public String getHTMLResourceFileName() 
        { 
        return "RolandJV880Drum.html";
        }


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
        
    /** Map of parameter -> index in the drumParameters array. */
    static HashMap drumParametersToIndex = null;

    public byte[] emit(String key) 
        { 
        if (key.equals("bank")) return new byte[0];  // this is not emittable
        if (key.equals("note")) return new byte[0];  // this is not emittable
                
        int note = StringUtility.getFirstInt(key);
        // n, o, t, e, + digits
        int param = ((Integer)(drumParametersToIndex.get(key.substring(note > 9 ? 6 : 5)))).intValue();
        
        int dataSize = 12;
        byte dataVal1 = 0;
        byte dataVal2 = 0;
        
        if (drumParameters[param].equals("-"))
            {
            System.err.println("This shouldn't be able to happen");
            return new byte[0];             // can never happen
            }
        else if (drumParameters[param].equals("wavenumber") || drumParameters[param].equals("pan"))
            {
            dataSize = 13;
            dataVal1 = (byte)(model.get(key) / 16);
            dataVal2 = (byte)(model.get(key) % 16);
            }
        else
            {
            dataVal1 = (byte)model.get(key);
            }
        
        byte[] data = new byte[dataSize];
    
        data[0] = (byte)0xF0;
        data[1] = (byte)0x41;
        data[2] = (byte)getID();
        data[3] = (byte)0x46;
        data[4] = (byte)0x12;    
        data[5] = (byte)0x00;
        data[6] = (byte)0x07;
        data[7] = (byte)(0x40 + note);
        data[8] = (byte)param;
        if (dataSize == 12)
            {
            data[9] = (byte)dataVal1;
            data[10] = produceChecksum(data, 5, 10);
            data[11] = (byte) 0xF7;
            }
        else
            {
            data[9] = (byte)dataVal1;
            data[10] = (byte)dataVal2;
            data[11] = produceChecksum(data, 5, 11);
            data[12] = (byte) 0xF7;
            }
                
        return data;
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

    /** When we load Internal, the JV880 doesn't change the patch because there's no notion
        of "changing the patch".  So we have to explicitly send the information back.  */
//    public boolean getSendsParametersAfterNonMergeParse() { return true; }

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
        

    public Object[] emitAll(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        if (tempModel == null)
            tempModel = getModel();

        byte[][] data = new byte[61][52 + 11];
    
        for(int i = 0; i < 61; i++)
            {
            data[i][0] = (byte)0xF0;
            data[i][1] = (byte)0x41;
            data[i][2] = (byte)getID();
            data[i][3] = (byte)0x46;
            data[i][4] = (byte)0x12;
            
            if (toWorkingMemory)
                {
                data[i][5] = (byte)0x00;
                data[i][6] = (byte)0x07;
                data[i][7] = (byte)(0x40 + i);
                data[i][8] = (byte)0x00;
                }
            else if (tempModel.get("bank", 0) == 0)         // internal
                {
                data[i][5] = (byte)0x01;
                data[i][6] = (byte)0x7F;
                data[i][7] = (byte)(0x40 + i);
                data[i][8] = (byte)0x00;
                }
            else                    // card
                {
                data[i][5] = (byte)0x02;
                data[i][6] = (byte)0x7F;
                data[i][7] = (byte)(0x40 + i);
                data[i][8] = (byte)0x00;
                }
            
            int pos = 9;
            for(int j = 0; j < drumParameters.length; j++)
                {
                int val = model.get("note" + i + drumParameters[j]);
                if (drumParameters[j].equals("-"))
                    {
                    pos++;
                    }
                else if (drumParameters[j].equals("wavenumber"))
                    {
                    data[i][pos] = (byte)((val >> 4) & 0x0F);
                    data[i][pos + 1] = (byte)(val & 0x0F);
                    pos++;
                    }
                else if (drumParameters[j].equals("pan"))
                    {
                    data[i][pos] = (byte)((val >> 4) & 0x0F);
                    data[i][pos + 1] = (byte)(val & 0x0F);
                    pos++;
                    }
                else
                    {
                    data[i][pos] = (byte)val;
                    pos++;
                    }
                }

            data[i][data[i].length - 2] = produceChecksum(data[i], 5, data[i].length - 2);
            data[i][data[i].length - 1] = (byte) 0xF7;
            }
                        
        return (Object[])data;
        }


    public static final int PAUSE_AFTER_CHANGE_PATCH_PERFORMANCE_BUTTON = 200;

    //// I CAN GET ALL 4 PATCH SEGMENTS WITH
    ////    F0 41 10 46 11 01 48 20 00 00 00 0C 00 0B F7

    public byte[] requestDump(Model tempModel)
        {
        // Change the patch/performance button to "performance" -- this is parameter 0 in system
        tryToSendSysex(new byte[] { (byte)0xF0, 0x41, getID(), 0x46, 0x12, 0x00, 0x00, 0x00, 0x00, 0x00, 
            produceChecksum(new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00 }), (byte)0xF7 });

        // It takes a second for this to take effect
        simplePause(PAUSE_AFTER_CHANGE_PATCH_PERFORMANCE_BUTTON);

        // Internal
        byte AA = (byte)(0x01);
        byte BB = (byte)(0x7F);
        byte CC = (byte)(0x40);
        byte DD = (byte)(0x00);

        if (tempModel.get("bank") == 1)
            {
            AA = 0x02;
            }
        
        // 24 * 128 + 100 = 3172 = 61 * 52
        
        byte checksum = produceChecksum(new byte[] { AA, BB, CC, DD, (byte)0x00, (byte)0x00, (byte)61, (byte)00 });
        byte[] b = new byte[] { (byte)0xF0, (byte)0x41, getID(), (byte)0x46, (byte)0x11, 
            AA, BB, CC , DD, (byte)0x00, (byte)0x00, (byte)61, (byte)0, checksum, (byte)0xF7 }; 
        return b;
        }
    
    public byte[] requestCurrentDump()
        {
        // Change the patch/performance button to "performance" -- this is parameter 0 in system
        tryToSendSysex(new byte[] { (byte)0xF0, 0x41, getID(), 0x46, 0x12, 0x00, 0x00, 0x00, 0x00, 0x00, 
            produceChecksum(new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00 }), (byte)0xF7 });

        // It takes a second for this to take effect
        simplePause(PAUSE_AFTER_CHANGE_PATCH_PERFORMANCE_BUTTON);

        byte AA = (byte)(0x00);
        byte BB = (byte)(0x07);
        byte CC = (byte)(0x40);
        byte DD = (byte)(0x00);
        
        // 24 * 128 + 100 = 3172 = 61 * 52
        
        byte checksum = produceChecksum(new byte[] { AA, BB, CC, DD, (byte)0x00, (byte)0x00, (byte)61, (byte)00 });
        byte[] b = new byte[] { (byte)0xF0, (byte)0x41, getID(), (byte)0x46, (byte)0x11, 
            AA, BB, CC , DD, (byte)0x00, (byte)0x00, (byte)61, (byte)0, checksum, (byte)0xF7 }; 
        return b;
        }
    
    
    


    ////// YOU MAY WANT TO IMPLEMENT SOME OF THE FOLLOWING

    public int getTestNotePitch() { return model.get("note"); }
//    public int getTestNoteChannel() { return model.get("channel"); }

    
    public JFrame sprout()
        {
        JFrame frame = super.sprout();
        addJV880DrumMenu();
        return frame;
        }


    public void resetToA()
        {
        defaultResourceFileName = "DrumPresetA.init";
        doReset();
        defaultResourceFileName = null;
        model.set("bank", 2);
        }
                
    public void resetToB()
        {
        defaultResourceFileName = "DrumPresetB.init";
        doReset();
        defaultResourceFileName = null;
        model.set("bank", 3);
        }
                
    public void addJV880DrumMenu()
        {
        JMenu menu = new JMenu("JV-80/880");
        menubar.add(menu);

        JMenuItem preseta = new JMenuItem("Load Preset A");
        menu.add(preseta);
        preseta.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt)
                {
                resetToA();
                }
            });

        JMenuItem presetb = new JMenuItem("Load Preset B");
        menu.add(presetb);
        presetb.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt)
                {
                resetToB();
                }
            });

        menu.addSeparator();
                
        JMenuItem swap = new JMenuItem("Swap With Note...");
        menu.add(swap);
        swap.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt)
                {
                int current = model.get("note");
                
                String[] notes = new String[61];
                for(int i = 0; i < notes.length; i++)
                    {
                    notes[i] = KEYS[i % 12] + (i / 12 + 2);
                    }
                        
                JComboBox combo = new JComboBox(notes);
                combo.setSelectedIndex(current - 36);
                
                int res = showMultiOption(RolandJV880Drum.this, new String[] { "Note" }, new JComponent[] { combo },
                    new String[] { "Swap", "Cancel" },
                    0, "Swap With Note...", 
                    new JLabel("Enter Note to Swap with."));
                                    
                int select = combo.getSelectedIndex();
                
                if (res == 0 && select != (current - 36))       // swap with someone relevant
                    {
                    setSendMIDI(false);
                    undo.setWillPush(false);
                    Model backup = (Model)(model.clone());
                    for(int i = 0; i < drumParameters.length; i++)
                        {
                        if (!drumParameters[i].equals("-"))
                            {
                            String swap = "note" + (current - 36) + drumParameters[i];
                            String with = "note" + select + drumParameters[i];
                            int temp = model.get(swap);
                            model.set(swap, model.get(with));
                            model.set(with, temp);
                            }
                        }
                    setSendMIDI(true);
                    undo.setWillPush(true);
                    if (!backup.keyEquals(getModel()))  // it's changed, do an undo push
                        {
                        undo.push(backup);
                        sendAllParameters();                        // FIXME we could update the two notes in question
                        }
                    }
                }
            });

        JMenuItem copy = new JMenuItem("Copy To Note...");
        menu.add(copy);
        copy.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt)
                {
                int current = model.get("note");
                
                String[] notes = new String[61];
                for(int i = 0; i < notes.length; i++)
                    {
                    notes[i] = KEYS[i % 12] + (i / 12 + 2);
                    }
                        
                JComboBox combo = new JComboBox(notes);
                combo.setSelectedIndex(current - 36);
                
                int res = showMultiOption(RolandJV880Drum.this, new String[] { "Note" }, new JComponent[] { combo },
                    new String[] { "Swap", "Cancel" },
                    0, "Copy To Note...", 
                    new JLabel("Enter Note to Copy to."));
                                    
                int select = combo.getSelectedIndex();
                
                if (res == 0 && select != (current - 36))       // copy to someone relevant
                    {
                    setSendMIDI(false);
                    undo.setWillPush(false);
                    Model backup = (Model)(model.clone());
                    for(int i = 0; i < drumParameters.length; i++)
                        {
                        if (!drumParameters[i].equals("-"))
                            {
                            String swap = "note" + (current - 36) + drumParameters[i];
                            String with = "note" + select + drumParameters[i];
                            model.set(with, model.get(swap));
                            }
                        }
                    setSendMIDI(true);
                    undo.setWillPush(true);
                    if (!backup.keyEquals(getModel()))  // it's changed, do an undo push
                        {
                        undo.push(backup);
                        sendAllParameters();                        // FIXME we could update the two notes in question
                        }
                    }
                }
            });
        }

    // The problem with this is that if we pick a sound that's not a drum sound,
    // like an organ or whatnot, it will play forever
//      public boolean getClearsTestNotes() { return false; }
        
    public int getPauseAfterChangePatch() { return 100; }

    public int getPauseAfterSendAllParameters() { return 300; }         // works at 100 in 1.01 but I want to be careful 
 
    public String getPatchName(Model model) { return "Drum"; }
        
    public Model getNextPatchLocation(Model model)
        {
        int bank = model.get("bank");
        
        bank++;
        if (bank >= 4)
            bank = 0;
                
        Model newModel = buildModel();
        newModel.set("bank", bank);
        return newModel;
        }

    public String getPatchLocationName(Model model)
        {
        // getPatchLocationName() is called from sprout() as a test to see if we should enable
        // batch downloading.  If we haven't yet created an .init file, then parameters won't exist
        // yet and this method will bomb badly.  So we return null in this case.
        if (!model.exists("bank")) return null;
        
        return DISPLAYABLE_BANKS[model.get("bank")];
        }
        
    public static final String[] drumParameters =
        {
        "wavegroup",
        "wavenumber",
        "-",                            // also wavenumber
        "toneswitch",
        "coarsetune",
        "mutegroup",
        "envelopemode",
        "pitchfine",
        "randompitchdepth",
        "pitchbendrange",
        "penvvelocitylevelsense",
        "penvvelocitytimesense",
        "penvdepth",
        "penvtime1",
        "penvlevel1",
        "penvtime2",
        "penvlevel2",
        "penvtime3",
        "penvlevel3",
        "penvtime4",
        "penvlevel4",
        "filtermode",
        "cutoff",
        "resonance",
        "resonancemode",
        "tvfenvvelocitylevelsense",
        "tvfenvvelocitytimesense",
        "tvfenvdepth",
        "tvfenvtime1",
        "tvfenvlevel1",
        "tvfenvtime2",
        "tvfenvlevel2",
        "tvfenvtime3",
        "tvfenvlevel3",
        "tvfenvtime4",
        "tvfenvlevel4",
        "level",
        "pan",
        "-",                            // also pan
        "tvaenvvelocitylevelsense",
        "tvaenvvelocitytimesense",
        "tvaenvtime1",
        "tvaenvlevel1",
        "tvaenvtime2",
        "tvaenvlevel2",
        "tvaenvtime3",
        "tvaenvlevel3",
        "tvaenvtime4",
        "drylevel",
        "reverbsendlevel",
        "chorussendlevel",
        "outputselect"
        };

    public boolean librarianTested() { return true; }
    }
