/***
    Copyright 2017 by Sean Luke
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

/**
   A patch editor for the Roland JV-880.
        
   @author Sean Luke
*/

public class RolandJV880 extends Synth
    {
    /// Various collections of parameter names for pop-up menus
        
    public static final String[] BANKS = new String[] { "Internal", "Card", "Preset A", "Preset B" };
    public static final String[] WRITABLE_BANKS = new String[] { "Internal", "Card" };
    public static final String[] REVERB_TYPES = new String[] { "Room 1", "Room 2", "Stage 1", "Stage 2", "Hall 1", "Hall 2", "Delay", "Pan Delay" };
    public static final String[] CHORUS_TYPES = new String[] { "Chorus 1", "Chorus 2", "Chorus 3" };
    public static final String[] CHORUS_OUTPUTS = new String[] { "Mix", "Rev" };
    public static final String[] KEY_ASSIGNS = new String[] { "Poly", "Solo" };
    public static final String[] WAVE_GROUPS = new String[] { "Internal", "Expansion", "PCM" };
    public static final String[] MOD_DESTINATIONS = new String[] { "Off", "Pitch", "Cutoff", "Resonance", "Level", "Pitch LFO 1", "Pitch LFO 2", "TVF LFO 1", "TVF LFO 2", "TVA LFO 1", "TVA LFO 2", "LFO 1 Rate", "LFO2 Rate" };
    public static final String[] FILTER_MODES = new String[] { "Off", "LPF", "HPF" };
    public static final String[] RESONANCE_MODES = new String[] { "Soft", "Hard" };
    public static final String[] PORTAMENTO_MODES = new String[] { "Legato", "Normal" };
    public static final String[] PORTAMENTO_TYPES = new String[] { "Time", "Rate" };
    public static final String[] TONE_DELAY_MODES = new String[] { "Normal", "Hold", "Play-Mate" };
    public static final String[] LFO_FORMS = new String[] { "Triangle", "Sine", "Sawtooth", "Square", "Random 1", "Random2" };
    public static final String[] LFO_OFFSETS = new String[] { "-100", "-50", "0", "50", "100" };
    public static final String[] LFO_FADE_POLARITIES = new String[] { "In", "Out" };
    public static final String[] RANDOM_PITCH_DEPTHS = new String[] { "0", "5", "10", "20", "30", "40", "50", "70", "100", "200", "300", "400", "500", "600", "800", "1200" };
    public static final String[] KEY_FOLLOWS_1 = new String[] { "-100", "-70", "-50", "-30", "-10", "0", "10", "20", "30", "40", "50", "70", "100", "120", "150", "200" };
    public static final String[] TIME_SENSES = new String[] { "-100", "-70", "-50", "-40", "-30", "-20", "-10", "0", "10", "20", "30", "40", "50", "70", "100" };
    public static final String[] KEY_FOLLOWS_2 = TIME_SENSES;
    public static final String[] OUTPUT_SELECTS = new String[] { "Main", "Sub" };
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
    public static final int MAX_WAVE_NUMBER = 256;

    public RolandJV880()
        {
        for(int i = 0; i < allGlobalParameters.length; i++)
            {
            allGlobalParametersToIndex.put(allGlobalParameters[i], Integer.valueOf(i));
            }

        for(int i = 0; i < allToneParameters.length; i++)
            {
            allToneParametersToIndex.put(allToneParameters[i], Integer.valueOf(i));
            }

                
        /// SOUND PANEL
                
        JComponent soundPanel = new SynthPanel(this);
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        hbox.addLast(addGlobal(Style.COLOR_A()));
        vbox.add(hbox);
        
        vbox.add(addEffects(Style.COLOR_B()));
        
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Common", soundPanel);
                

        for(int i = 1; i < 5; i++)
            {
            JComponent sourcePanel = new SynthPanel(this);
            vbox = new VBox();
                
            hbox = new HBox();
            hbox.add(addWave(i, Style.COLOR_A()));
            hbox.addLast(addEffects(i, Style.COLOR_A()));
            vbox.add(hbox);

            hbox = new HBox();
            hbox.add(addPitch(i, Style.COLOR_A()));
            hbox.add(addFilter(i, Style.COLOR_B()));
            hbox.addLast(addAmplifier(i, Style.COLOR_C()));
            vbox.add(hbox);
                
            vbox.add(addPitchEnvelope(i, Style.COLOR_A()));
            vbox.add(addFilterEnvelope(i, Style.COLOR_B()));
            vbox.add(addAmplifierEnvelope(i, Style.COLOR_C()));

            sourcePanel.add(vbox, BorderLayout.CENTER);
            //            ((SynthPanel)sourcePanel).makePasteable("tone" + i);
            ((SynthPanel)sourcePanel).makePasteable("tone");
            addTab("Tone " + i, sourcePanel);


            sourcePanel = new SynthPanel(this);
            vbox = new VBox();
                
            vbox.add(addLFO(i, 1, Style.COLOR_A()));
            vbox.add(addLFO(i, 2, Style.COLOR_A()));
            vbox.add(addModulation(i, Style.COLOR_B()));
            vbox.add(addAftertouch(i, Style.COLOR_A()));
            vbox.add(addExpression(i, Style.COLOR_B()));

            sourcePanel.add(vbox, BorderLayout.CENTER);
            //            ((SynthPanel)sourcePanel).makePasteable("tone" + i);
            ((SynthPanel)sourcePanel).makePasteable("tone");
            addTab("Mod " + i, sourcePanel);
            }


        model.set("name", "Init Patch");  // has to be 10 long

        model.set("bank", 0);           // internal
        model.set("number", 0);
                        
        loadDefaults();        
        }
                
                
    /*
      public JFrame sprout()
      {
      JFrame frame = super.sprout();
      return frame;
      }        
    */ 

    public String getDefaultResourceFileName() { return "RolandJV880.init"; }
    public String getHTMLResourceFileName() { return "RolandJV880.html"; }

    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        String[] banks = (writing ? WRITABLE_BANKS : BANKS);
        JComboBox bank = new JComboBox(banks);
        int b = model.get("bank");
        if (b >= banks.length)
            b = 0;
        bank.setSelectedIndex(b);
        
        JTextField number = new SelectedTextField("" + (model.get("number") + 1), 3);

        while(true)
            {
            boolean result = showMultiOption(this, new String[] { "Bank", "Patch Number"}, 
                new JComponent[] { bank, number }, title, "Enter the Bank and Patch number");
                
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
            int i = bank.getSelectedIndex();
                        
            if (i >= 2 && writing)  // uh oh
                {
                showSimpleError("Bank Write Error", "You cannot write to a preset bank");
                }
            else
                {                                
                change.set("bank", i);
                change.set("number", n);
                                                
                return true;
                }
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
        vbox.addBottom(comp);
        hbox.add(vbox);

        hbox.add(Strut.makeHorizontalStrut(40));

        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }


    public JComponent addGlobal( Color color)
        {
        Category category = new Category(this, "Global", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();

        VBox vbox = new VBox();
        
        params = KEY_ASSIGNS;
        comp = new Chooser("Key Assign", this, "keyassign", params);
        vbox.add(comp);

        comp = new CheckBox("Solo Legato", this, "sololegato");
        vbox.add(comp);

        comp = new CheckBox("Velocity Switch", this, "velocityswitch");
        vbox.add(comp);

        hbox.add(vbox);

        vbox = new VBox();        
        params = PORTAMENTO_MODES;
        comp = new Chooser("Portamento Mode", this, "portamentomode", params);
        vbox.add(comp);
        
        params = PORTAMENTO_TYPES;
        comp = new Chooser("Portamento Type", this, "portamentotype", params);
        vbox.add(comp);

        comp = new CheckBox("Portamento Switch", this, "portamentoswitch");
        vbox.add(comp);

        hbox.add(vbox);

        comp = new LabelledDial("Portamento", this, "portamentotime", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Time");
        hbox.add(comp);

        comp = new LabelledDial("Analog", this, "analogfeel", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Feel");
        hbox.add(comp);

        comp = new LabelledDial("Level", this, "patchlevel", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Pan", this, "patchpanning", color, 0, 127)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                if (value < 64)
                    {
                    return "L" + (64 - value);
                    }
                else if (value == 64) return "--";
                else
                    {
                    return "R" + (value - 64);
                    }
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Bend Range", this, "bendrangedown", color, 16, 64, 64)
            {
            public double getStartAngle() { return 180; }
            };
        ((LabelledDial)comp).addAdditionalLabel("Down");
        hbox.add(comp);

        comp = new LabelledDial("Bend Range", this, "bendrangeup", color, 0, 12);
        ((LabelledDial)comp).addAdditionalLabel("Up");
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addEffects( Color color)
        {
        Category category = new Category(this, "Effects", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = REVERB_TYPES;
        comp = new Chooser("Reverb Type", this, "reverbtype", params);
        vbox.add(comp);
        
        params = CHORUS_TYPES;
        comp = new Chooser("Chorus Type", this, "chorustype", params);
        vbox.add(comp);
        
        params = CHORUS_OUTPUTS;
        comp = new Chooser("Chorus Output", this, "chorusoutput", params);
        vbox.add(comp);

        hbox.add(vbox);

        comp = new LabelledDial("Reverb", this, "reverblevel", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);

        comp = new LabelledDial("Reverb", this, "reverbtime", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Time");
        hbox.add(comp);

        comp = new LabelledDial("Reverb", this, "reverbfeedback", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Feedback");
        hbox.add(comp);

        comp = new LabelledDial("Chorus", this, "choruslevel", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);

        comp = new LabelledDial("Chorus", this, "chorusdepth", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Depth");
        hbox.add(comp);

        comp = new LabelledDial("Chorus", this, "chorusrate", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Rate");
        hbox.add(comp);

        comp = new LabelledDial("Chorus", this, "chorusfeedback", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Feedback");
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addWave(int tone, Color color)
        {
        Category category = new Category(this, "Wave", color);
        //        category.makePasteable("tone" + tone);
        category.makePasteable("tone");
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = WAVE_GROUPS;
        comp = new Chooser("Wave Group", this, "tone" + tone + "wavegroup", params);
        vbox.add(comp);

        params = new String[MAX_WAVE_NUMBER];
        for(int i = 0 ; i < WAVE_NAMES.length; i++)
            params[i] = "" + (i + 1) + " " + WAVE_NAMES[i];
        for(int i = WAVE_NAMES.length; i < MAX_WAVE_NUMBER; i++)
            params[i] = "" + (i + 1);
                
        comp = new Chooser("Wave Number", this, "tone" + tone + "wavenumber", params);
        vbox.add(comp);
        hbox.add(vbox);

        vbox = new VBox();
        comp = new CheckBox("Tone Switch", this, "tone" + tone + "toneswitch");
        vbox.add(comp);

        comp = new CheckBox("Volume Switch", this, "tone" + tone + "volumeswitch");
        vbox.add(comp);
        hbox.add(vbox);

        vbox = new VBox();
        comp = new CheckBox("Hold-1 Switch", this, "tone" + tone + "hold1switch");
        ((CheckBox)comp).addToWidth(2);
        vbox.add(comp);

        comp = new CheckBox("FXM Switch", this, "tone" + tone + "fxmswitch");
        vbox.add(comp);
        hbox.add(vbox);


        comp = new LabelledDial("FXM Depth", this, "tone" + tone + "fxmdepth", color, 0, 15, -1);
        hbox.add(comp);

        comp = new LabelledDial("Velocity", this, "tone" + tone + "velocityrangelower", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Range Lower");
        hbox.add(comp);

        comp = new LabelledDial("Velocity", this, "tone" + tone + "velocityrangeupper", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Range Upper");
        hbox.add(comp);
                
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addEffects(int tone, Color color)
        {
        Category category = new Category(this, "Effects", color);
        //        category.makePasteable("tone" + tone);
        category.makePasteable("tone");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = OUTPUT_SELECTS;
        comp = new Chooser("Output Select", this, "tone" + tone + "outputselect", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Dry Level", this, "tone" + tone + "drylevel", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Reverb", this, "tone" + tone + "reverbsendlevel", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Send Level");
        hbox.add(comp);

        comp = new LabelledDial("Chorus", this, "tone" + tone + "chorussendlevel", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Send Level");
        hbox.add(comp);
                
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addModulation(int tone, Color color)
        {
        Category category = new Category(this, "Modulation", color);
        //        category.makePasteable("tone" + tone);
        category.makePasteable("tone");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = MOD_DESTINATIONS;
        comp = new Chooser("Destination 1", this, "tone" + tone + "modulationdestination1", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Sense 1", this, "tone" + tone + "modulationsense1", color, 1, 127, 64);
        hbox.add(comp);

        vbox = new VBox();
        params = MOD_DESTINATIONS;
        comp = new Chooser("Destination 2", this, "tone" + tone + "modulationdestination2", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Sense 2", this, "tone" + tone + "modulationsense2", color, 1, 127, 64);
        hbox.add(comp);
                
        vbox = new VBox();
        params = MOD_DESTINATIONS;
        comp = new Chooser("Destination 3", this, "tone" + tone + "modulationdestination3", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Sense 3", this, "tone" + tone + "modulationsense3", color, 1, 127, 64);
        hbox.add(comp);

        vbox = new VBox();
        params = MOD_DESTINATIONS;
        comp = new Chooser("Destination 4", this, "tone" + tone + "modulationdestination4", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Sense 4", this, "tone" + tone + "modulationsense4", color, 1, 127, 64);
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addAftertouch(int tone, Color color)
        {
        Category category = new Category(this, "Aftertouch", color);
        //        category.makePasteable("tone" + tone);
        category.makePasteable("tone");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = MOD_DESTINATIONS;
        comp = new Chooser("Destination 1", this, "tone" + tone + "aftertouchdestination1", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Sense 1", this, "tone" + tone + "aftertouchsense1", color, 1, 127, 64);
        hbox.add(comp);

        vbox = new VBox();
        params = MOD_DESTINATIONS;
        comp = new Chooser("Destination 2", this, "tone" + tone + "aftertouchdestination2", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Sense 2", this, "tone" + tone + "aftertouchsense2", color, 1, 127, 64);
        hbox.add(comp);
                
        vbox = new VBox();
        params = MOD_DESTINATIONS;
        comp = new Chooser("Destination 3", this, "tone" + tone + "aftertouchdestination3", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Sense 3", this, "tone" + tone + "aftertouchsense3", color, 1, 127, 64);
        hbox.add(comp);

        vbox = new VBox();
        params = MOD_DESTINATIONS;
        comp = new Chooser("Destination 4", this, "tone" + tone + "aftertouchdestination4", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Sense 4", this, "tone" + tone + "aftertouchsense4", color, 1, 127, 64);
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addExpression(int tone, Color color)
        {
        Category category = new Category(this, "Expression", color);
        //        category.makePasteable("tone" + tone);
        category.makePasteable("tone");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = MOD_DESTINATIONS;
        comp = new Chooser("Destination 1", this, "tone" + tone + "expressiondestination1", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Sense 1", this, "tone" + tone + "expressionsense1", color, 1, 127, 64);
        hbox.add(comp);

        vbox = new VBox();
        params = MOD_DESTINATIONS;
        comp = new Chooser("Destination 2", this, "tone" + tone + "expressiondestination2", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Sense 2", this, "tone" + tone + "expressionsense2", color, 1, 127, 64);
        hbox.add(comp);
                
        vbox = new VBox();
        params = MOD_DESTINATIONS;
        comp = new Chooser("Destination 3", this, "tone" + tone + "expressiondestination3", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Sense 3", this, "tone" + tone + "expressionsense3", color, 1, 127, 64);
        hbox.add(comp);

        vbox = new VBox();
        params = MOD_DESTINATIONS;
        comp = new Chooser("Destination 4", this, "tone" + tone + "expressiondestination4", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Sense 4", this, "tone" + tone + "expressionsense4", color, 1, 127, 64);
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addLFO(int tone, int lfo, Color color)
        {
        Category category = new Category(this, "LFO " + lfo, color);
        //        category.makePasteable("tone" + tone);
        category.makePasteable("tone");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = LFO_FORMS;
        comp = new Chooser("Form", this, "tone" + tone + "lfo" + lfo + "form", params);
        vbox.add(comp);

        params = LFO_FADE_POLARITIES;
        comp = new Chooser("Fade Polarity", this, "tone" + tone + "lfo" + lfo + "fadepolarity", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Offset", this, "tone" + tone + "lfo" + lfo + "offset", color, 0, 4)
            {
            public boolean isSymmetric() { return true; }
                
            public String map(int value)
                {
                return LFO_OFFSETS[value];
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Rate", this, "tone" + tone + "lfo" + lfo + "rate", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Delay", this, "tone" + tone + "lfo" + lfo + "delay", color, 0, 128)
            {
            public String map(int value)
                {
                if (value < 128) return "" + value;
                else return "Key Off";
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Fade Time", this, "tone" + tone + "lfo" + lfo + "fadetime", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Pitch Depth", this, "tone" + tone + "lfo" + lfo + "pitchdepth", color, 4, 124) 
            {
            public boolean isSymmetric() { return true; }
                
            public String map(int value)
                {
                return "" + ((value - 64) * 10);
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("TFV Depth", this, "tone" + tone + "lfo" + lfo + "tvfdepth", color, 1, 127, 64);
        hbox.add(comp);

        comp = new LabelledDial("TFA Depth", this, "tone" + tone + "lfo" + lfo + "tvadepth", color, 1, 127, 64);
        hbox.add(comp);

        vbox = new VBox();
        comp = new CheckBox("Synchro", this, "tone" + tone + "lfo" + lfo + "synchro");
        ((CheckBox)comp).addToWidth(2);
        vbox.add(comp);
        hbox.add(vbox);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addPitch(int tone, Color color)
        {
        Category category = new Category(this, "Pitch", color);
        //        category.makePasteable("tone" + tone);
        category.makePasteable("tone");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        comp = new LabelledDial("Coarse", this, "tone" + tone + "pitchcoarse", color, 16, 112, 64);
        hbox.add(comp);

        comp = new LabelledDial("Fine", this, "tone" + tone + "pitchfine", color, 14, 114, 64);
        hbox.add(comp);

        comp = new LabelledDial("Random", this, "tone" + tone + "randompitchdepth", color, 0, 15)
            {
            public String map(int value)
                {
                return RANDOM_PITCH_DEPTHS[value];
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Pitch Depth");
        hbox.add(comp);

        comp = new LabelledDial("Pitch", this, "tone" + tone + "pitchkeyfollow", color, 0, 15)
            {
            public double getStartAngle() { return 180; }
                
            public String map(int value)
                {
                return KEY_FOLLOWS_1[value];
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Key Follow");
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addPitchEnvelope(int tone, Color color)
        {
        Category category = new Category(this, "Pitch Envelope", color);
        //        category.makePasteable("tone" + tone);
        category.makePasteable("tone");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        comp = new LabelledDial("Velocity", this, "tone" + tone + "penvvelocitylevelsense", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Level Sense");
        
        // add some space
        hbox.add(Strut.makeStrut(comp));
        
        hbox.add(comp);

        comp = new LabelledDial("Velocity On", this, "tone" + tone + "penvvelocityontimesense", color, 0, 14)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                return TIME_SENSES[value];
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Time Sense");
        hbox.add(comp);

        comp = new LabelledDial("Velocity Off", this, "tone" + tone + "penvvelocityofftimesense", color, 0, 14)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                return TIME_SENSES[value];
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Time Sense");
        hbox.add(comp);

        comp = new LabelledDial("Time", this, "tone" + tone + "penvtimekeyfollow", color, 0, 14)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                return KEY_FOLLOWS_2[value];
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Key Follow");
        hbox.add(comp);

        comp = new LabelledDial("Depth", this, "tone" + tone + "penvdepth", color, 52, 76, 64);
        hbox.add(comp);

        comp = new LabelledDial("Time 1", this, "tone" + tone + "penvtime1", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Level 1", this, "tone" + tone + "penvlevel1", color, 1, 127, 64);
        hbox.add(comp);

        comp = new LabelledDial("Time 2", this, "tone" + tone + "penvtime2", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Level 2", this, "tone" + tone + "penvlevel2", color, 1, 127, 64);
        hbox.add(comp);

        comp = new LabelledDial("Time 3", this, "tone" + tone + "penvtime3", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Level 3", this, "tone" + tone + "penvlevel3", color, 1, 127, 64);
        hbox.add(comp);

        comp = new LabelledDial("Time 4", this, "tone" + tone + "penvtime4", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Level 4", this, "tone" + tone + "penvlevel4", color, 1, 127, 64);
        hbox.add(comp);

        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "tone" + tone + "penvtime1", "tone" + tone + "penvtime2", "tone" + tone + "penvtime3", null, "tone" + tone + "penvtime4" },
            new String[] { null, "tone" + tone + "penvlevel1", "tone" + tone + "penvlevel2", "tone" + tone + "penvlevel3", "tone" + tone + "penvlevel3", "tone" + tone + "penvlevel4" },
            new double[] { 0, 0.2 / 127.0, 0.2 / 127.0, 0.2 / 127.0, 0.2, 0.2 / 127.0 },
            new double[] { 0, 1.0 / 127.0, 1.0 / 127.0, 1.0 / 127.0, 1.0 / 127.0, 1.0 / 127.0 });
            
        ((EnvelopeDisplay)comp).setAxis(1.0 / 127.0 * 64.0);  // is this centered right?
        hbox.addLast(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addFilter(int tone, Color color)
        {
        Category category = new Category(this, "Filter (TVF)", color);
        //        category.makePasteable("tone" + tone);
        category.makePasteable("tone");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
 
        VBox vbox = new VBox();
        params = FILTER_MODES;
        comp = new Chooser("Filter Mode", this, "tone" + tone + "filtermode", params);
        vbox.add(comp);

        params = RESONANCE_MODES;
        comp = new Chooser("Resonance Mode", this, "tone" + tone + "resonancemode", params);
        vbox.add(comp);
        
        hbox.add(vbox);

        comp = new LabelledDial("Cutoff", this, "tone" + tone + "cutofffrequency", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Resonance", this, "tone" + tone + "resonance", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Cutoff", this, "tone" + tone + "cutoffkeyfollow", color, 0, 15)
            {
            public double getStartAngle() { return 180; }
                
            public String map(int value)
                {
                return KEY_FOLLOWS_1[value];
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Key Follow");
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    JComponent tvfenvdepth = null;
    JComponent tvfenvlevel4 = null;
    public JComponent addFilterEnvelope(int tone, Color color)
        {
        Category category = new Category(this, "Filter (TVF) Envelope", color);
        //        category.makePasteable("tone" + tone);
        category.makePasteable("tone");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        comp = new LabelledDial("Velocity", this, "tone" + tone + "tvfenvvelocitycurve", color, 0, 6, -1);
        ((LabelledDial)comp).addAdditionalLabel("Curve");
        hbox.add(comp);

        comp = new LabelledDial("Velocity", this, "tone" + tone + "tvfenvvelocitylevelsense", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Level Sense");
        hbox.add(comp);

        comp = new LabelledDial("Velocity On", this, "tone" + tone + "tvfenvvelocityontimesense", color, 0, 14)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                return TIME_SENSES[value];
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Time Sense");
        hbox.add(comp);

        comp = new LabelledDial("Velocity Off", this, "tone" + tone + "tvfenvvelocityofftimesense", color, 0, 14)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                return TIME_SENSES[value];
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Time Sense");
        hbox.add(comp);

        comp = new LabelledDial("Time", this, "tone" + tone + "tvfenvtimekeyfollow", color, 0, 14)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                return KEY_FOLLOWS_2[value];
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Key Follow");
        hbox.add(comp);

        comp = new LabelledDial("Depth", this, "tone" + tone + "tvfenvdepth", color, 1, 127, 64);
        tvfenvdepth = comp;
        hbox.add(comp);

        comp = new LabelledDial("Time 1", this, "tone" + tone + "tvfenvtime1", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Level 1", this, "tone" + tone + "tvfenvlevel1", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Time 2", this, "tone" + tone + "tvfenvtime2", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Level 2", this, "tone" + tone + "tvfenvlevel2", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Time 3", this, "tone" + tone + "tvfenvtime3", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Level 3", this, "tone" + tone + "tvfenvlevel3", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Time 4", this, "tone" + tone + "tvfenvtime4", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Level 4", this, "tone" + tone + "tvfenvlevel4", color, 0, 127);
        tvfenvlevel4 = comp;
        hbox.add(comp);

        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "tone" + tone + "tvfenvtime1", "tone" + tone + "tvfenvtime2", "tone" + tone + "tvfenvtime3", null, "tone" + tone + "tvfenvtime4" },
            new String[] { null, "tone" + tone + "tvfenvlevel1", "tone" + tone + "tvfenvlevel2", "tone" + tone + "tvfenvlevel3", "tone" + tone + "tvfenvlevel3", "tone" + tone + "tvfenvlevel4" },
            new double[] { 0, 0.2 / 127.0, 0.2 / 127.0, 0.2 / 127.0, 0.2, 0.2 / 127.0 },
            new double[] { 0, 1.0 / 127.0, 1.0 / 127.0, 1.0 / 127.0, 1.0 / 127.0, 1.0 / 127.0 });
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addAmplifier(int tone, Color color)
        {
        Category category = new Category(this, "Amplifier (TVA)", color);
        //        category.makePasteable("tone" + tone);
        category.makePasteable("tone");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
 
        VBox vbox = new VBox();
        params = TONE_DELAY_MODES;
        comp = new Chooser("Tone Delay Mode", this, "tone" + tone + "tonedelaymode", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Tone Delay", this, "tone" + tone + "tonedelaytime", color, 0, 128)
            {
            public String map(int value)
                {
                if (value < 128) return "" + value;
                else return "Key Off";
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Time");
        hbox.add(comp);

        comp = new LabelledDial("Level", this, "tone" + tone + "level", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Level", this, "tone" + tone + "levelkeyfollow", color, 0, 14)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                return KEY_FOLLOWS_2[value];
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Key Follow");
        hbox.add(comp);

        comp = new LabelledDial("Pan", this, "tone" + tone + "pan", color, 0, 128)
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

        comp = new LabelledDial("Pan", this, "tone" + tone + "panningkeyfollow", color, 0, 14)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                return KEY_FOLLOWS_2[value];
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Key Follow");
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addAmplifierEnvelope(int tone, Color color)
        {
        Category category = new Category(this, "Amplifier (TVA) Envelope", color);
        //        category.makePasteable("tone" + tone);
        category.makePasteable("tone");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        comp = new LabelledDial("Velocity", this, "tone" + tone + "tvaenvvelocitycurve", color, 0, 6, -1);
        ((LabelledDial)comp).addAdditionalLabel("Curve");
        hbox.add(comp);

        comp = new LabelledDial("Velocity", this, "tone" + tone + "tvaenvvelocitylevelsense", color, 1, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Level Sense");
        hbox.add(comp);

        comp = new LabelledDial("Velocity On", this, "tone" + tone + "tvaenvvelocityontimesense", color, 0, 14)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                return TIME_SENSES[value];
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Time Sense");
        hbox.add(comp);

        comp = new LabelledDial("Velocity Off", this, "tone" + tone + "tvaenvvelocityofftimesense", color, 0, 14)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                return TIME_SENSES[value];
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Time Sense");
        hbox.add(comp);

        comp = new LabelledDial("Time", this, "tone" + tone + "tvaenvtimekeyfollow", color, 0, 14)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                return KEY_FOLLOWS_2[value];
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Key Follow");
        hbox.add(comp);

        //comp = new LabelledDial("Depth", this, "tone" + tone + "tvaenvdepth", color, 1, 127, 64);
        comp = Strut.makeStrut(tvfenvdepth);
        hbox.add(comp);

        comp = new LabelledDial("Time 1", this, "tone" + tone + "tvaenvtime1", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Level 1", this, "tone" + tone + "tvaenvlevel1", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Time 2", this, "tone" + tone + "tvaenvtime2", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Level 2", this, "tone" + tone + "tvaenvlevel2", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Time 3", this, "tone" + tone + "tvaenvtime3", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Level 3", this, "tone" + tone + "tvaenvlevel3", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Time 4", this, "tone" + tone + "tvaenvtime4", color, 0, 127);
        hbox.add(comp);

        //comp = new LabelledDial("Level 4", this, "tone" + tone + "tvaenvlevel4", color, 0, 127);
        comp = Strut.makeStrut(tvfenvlevel4);
        hbox.add(comp);

        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "tone" + tone + "tvaenvtime1", "tone" + tone + "tvaenvtime2", "tone" + tone + "tvaenvtime3", null, "tone" + tone + "tvaenvtime4" },
            new String[] { null, "tone" + tone + "tvaenvlevel1", "tone" + tone + "tvaenvlevel2", "tone" + tone + "tvaenvlevel3", "tone" + tone + "tvaenvlevel3", null },
            new double[] { 0, 0.2 / 127.0, 0.2 / 127.0, 0.2 / 127.0, 0.2, 0.2 / 127.0 },
            new double[] { 0, 1.0 / 127.0, 1.0 / 127.0, 1.0 / 127.0, 1.0 / 127.0, 0 });
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    HashMap allGlobalParametersToIndex = new HashMap();
        
    final static String[] allGlobalParameters = new String[]
    {
    "patchname1",
    "patchname2",
    "patchname3",
    "patchname4",
    "patchname5",
    "patchname6",
    "patchname7",
    "patchname8",
    "patchname9",
    "patchname10",
    "patchname11",
    "patchname12",
    "velocityswitch",
    "reverbtype",
    "reverblevel",
    "reverbtime",
    "reverbfeedback",
    "chorustype",
    "choruslevel",
    "chorusdepth",
    "chorusrate",
    "chorusfeedback",
    "chorusoutput",
    "analogfeel",
    "patchlevel",
    "patchpanning",             // note that it's NOT "patchpan", so it's distinguished from the others
    "bendrangedown",
    "bendrangeup",
    "keyassign",
    "sololegato",
    "portamentoswitch",
    "portamentomode",
    "portamentotype",
    "portamentotime",
    };
    
    
    HashMap allToneParametersToIndex = new HashMap();
        
    final static String[] allToneParameters = new String[]
    {
    "wavegroup",
    "wavenumber",
    "-",                // second byte
    "toneswitch",
    "fxmswitch",
    "fxmdepth",
    "velocityrangelower",
    "velocityrangeupper",
    "volumeswitch",
    "hold1switch",
    "modulationdestination1",
    "modulationsense1",
    "modulationdestination2",
    "modulationsense2",
    "modulationdestination3",
    "modulationsense3",
    "modulationdestination4",
    "modulationsense4",
    "aftertouchdestination1",
    "aftertouchsense1",
    "aftertouchdestination2",
    "aftertouchsense2",
    "aftertouchdestination3",
    "aftertouchsense3",
    "aftertouchdestination4",
    "aftertouchsense4",
    "expressiondestination1",
    "expressionsense1",
    "expressiondestination2",
    "expressionsense2",
    "expressiondestination3",
    "expressionsense3",
    "expressiondestination4",
    "expressionsense4",
    "lfo1form",
    "lfo1offset",
    "lfo1synchro",
    "lfo1rate",
    "lfo1delay",
    "-",                // second byte
    "lfo1fadepolarity",
    "lfo1fadetime",
    "lfo1pitchdepth",
    "lfo1tvfdepth",
    "lfo1tvadepth",
    "lfo2form",
    "lfo2offset",
    "lfo2synchro",
    "lfo2rate",
    "lfo2delay",
    "-",                // second byte
    "lfo2fadepolarity",
    "lfo2fadetime",
    "lfo2pitchdepth",
    "lfo2tvfdepth",
    "lfo2tvadepth",
    "pitchcoarse",
    "pitchfine",
    "randompitchdepth",
    "pitchkeyfollow",
    "penvvelocitylevelsense",
    "penvvelocityontimesense",
    "penvvelocityofftimesense",
    "penvtimekeyfollow",
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
    "cutofffrequency",
    "resonance",
    "resonancemode",
    "cutoffkeyfollow",
    "tvfenvvelocitycurve",
    "tvfenvvelocitylevelsense",
    "tvfenvvelocityontimesense",
    "tvfenvvelocityofftimesense",
    "tvfenvtimekeyfollow",
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
    "levelkeyfollow",
    "pan",
    "-",                // second byte
    "panningkeyfollow",
    "tonedelaymode",
    "tonedelaytime",
    "-",                // second byte
    "tvaenvvelocitycurve",
    "tvaenvvelocitylevelsense",
    "tvaenvvelocityontimesense",
    "tvaenvvelocityofftimesense",
    "tvaenvtimekeyfollow",
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
        
    public byte[] getData(String key)
        {
        if (key.equals("name"))                                // name is 12-byte
            {
            byte[] data = new byte[12];
            String name = model.get("name", "") + "            ";
            for(int i = 0; i < data.length; i++)
                {
                data[i] = (byte)(name.charAt(i));
                }
            return data;
            }
        else if (key.endsWith("wavenumber") ||          // Some data is 2-byte
            key.endsWith("lfo1delay") ||
            key.endsWith("lfo2delay") ||
            key.endsWith("pan") ||
            key.endsWith("tonedelaytime"))
            {
            int data = model.get(key);
            // MSB is first
            byte MSB = (byte)((data >> 4) & 127);
            byte LSB = (byte)(data & 15);
            return new byte[] { MSB, LSB };
            }
        else                                                                                    // Some data is 1-byte
            {
            return new byte[] { (byte) model.get(key) };
            }
        }
        
    public Object[] emitAll(String key)
        {
        if (key.equals("-")) return new Object[0];  // this is not emittable
        if (key.equals("bank")) return new Object[0];  // this is not emittable
        if (key.equals("number")) return new Object[0];  // this is not emittable
        
        // address for "patch mode temporary patch"
        byte AA = (byte)0x00;
        byte BB = (byte)0x08;
        byte CC = (byte)0x20;
        byte DD = (byte)0x00;
            
        // figure out the address
        if (key.equals("name"))
            {
            // no changes
            }
        else if (key.startsWith("tone1"))
            {
            CC = (byte)(CC + 0x08);
            DD = (byte)(DD + ((Integer)(allToneParametersToIndex.get(key.substring(5)))).intValue());  // get rid of the "tone1"
            }
        else if (key.startsWith("tone2"))
            {
            CC = (byte)(CC + 0x09);
            DD = (byte)(DD + ((Integer)(allToneParametersToIndex.get(key.substring(5)))).intValue());  // get rid of the "tone2"
            }
        else if (key.startsWith("tone3"))
            {
            CC = (byte)(CC + 0x0A);
            DD = (byte)(DD + ((Integer)(allToneParametersToIndex.get(key.substring(5)))).intValue());  // get rid of the "tone3"
            }
        else if (key.startsWith("tone4"))
            {
            CC = (byte)(CC + 0x0B);
            DD = (byte)(DD + ((Integer)(allToneParametersToIndex.get(key.substring(5)))).intValue());  // get rid of the "tone4"
            }
        else                // Common
            {
            DD = (byte)(DD + ((Integer)(allGlobalParametersToIndex.get(key))).intValue());
            }
        
        byte[] payload = getData(key);

        // Handle irregularities in multi-byte data
        if (payload.length == 12)
            {
            byte[] data = new byte[23];
                        
            // gather data which is checksummed
            byte[] checkdata = new byte[4 + 12];
            System.arraycopy(new byte[] { AA, BB, CC, DD }, 0, checkdata, 0, 4);
            System.arraycopy(payload, 0, checkdata, 4, payload.length);
                        
            // concatenate all data
            byte checksum = produceChecksum(checkdata);
            data[0] = (byte)0xF0;
            data[1] = (byte)0x41;
            data[2] = getID();
            data[3] = (byte)0x46;
            data[4] = (byte)0x12;
            System.arraycopy(checkdata, 0, data, 5, checkdata.length);
            data[21] = checksum;
            data[22] = (byte)0xF7;
                    
            return new Object[] { data };
            }
        else if (payload.length == 2)
            {
            byte checksum = produceChecksum(new byte[] { AA, BB, CC, DD, payload[0], payload[1] });
            byte[] data = new byte[] { (byte)0xF0, (byte)0x41, getID(), (byte)0x46, (byte)0x12, AA, BB, CC, DD, payload[0], payload[1], checksum, (byte)0xF7 };
            return new Object[] { data };
            }
        else                                                                                    // Some data is 1-byte
            {
            byte checksum = produceChecksum(new byte[] { AA, BB, CC, DD, payload[0] });
            byte[] data = new byte[] { (byte)0xF0, (byte)0x41, getID(), (byte)0x46, (byte)0x12, AA, BB, CC, DD, payload[0], checksum, (byte)0xF7 };
            return new Object[] { data };
            }
        }

    int parseStatus = 0;
        
    public int parse(byte[] data, boolean fromFile)
        {
        if (data.length == 553)
            {
            int pos = 0;

            // common
            byte[] buf = new byte[34 + 11];
            System.arraycopy(data, pos, buf, 0, buf.length);
            if (parse(buf, fromFile) != PARSE_INCOMPLETE) return PARSE_FAILED;
            pos += (34 + 11);

            // tones
            for(int i = 0; i < 4; i++)
                {
                buf = new byte[116 + 11];
                System.arraycopy(data, pos, buf, 0, buf.length);
                int ret = parse(buf, fromFile);
                if (!  ((i == 3 && ret == PARSE_SUCCEEDED) ||
                        (i != 3 && ret == PARSE_INCOMPLETE))) return PARSE_FAILED;
                pos += (116 + 11);
                }
            return PARSE_SUCCEEDED;
            }
        
        int pos = 9;
        
        if (data.length == 34 + 11)     // common
            {               
            String name = "";
            // Load patch name
            for(int i = 0; i < 12; i++)
                {
                name = name + ((char)data[pos++]);
                }
            model.set("name", name);
                        
            // Load other patch common
            for(int i = 12; i < allGlobalParameters.length; i++)  // skip name parameters
                {
                model.set(allGlobalParameters[i], data[pos++]);         // there are no two-byte parameters, so we're okay
                }
                                
            parseStatus = 1;
            revise();
            return PARSE_INCOMPLETE;
            }
        else if (data.length == 116 + 11 && parseStatus >= 1 && parseStatus <= 4)
            {
            for(int i = 0; i < allToneParameters.length; i++)
                {
                String key = allToneParameters[i];
                //System.err.println("" + String.format("%02X", (pos - 9)) + "  " + key);
                if (key.equals("-")) // two-byte character. Skip, don't increase pos
                    {
                    // do nothing
                    }
                else if (key.endsWith("wavenumber") ||          // Some data is 2-byte
                    key.endsWith("lfo1delay") ||
                    key.endsWith("lfo2delay") ||
                    key.endsWith("pan") ||
                    key.endsWith("tonedelaytime"))
                    {
                    model.set("tone" + parseStatus + key, (data[pos++] << 4) | data[pos++]);
                    }
                else
                    {
                    model.set("tone" + parseStatus + key, data[pos++]);
                    }
                }
                        
            if (parseStatus == 4)
                {
                revise();
                return PARSE_SUCCEEDED;
                }
            else
                {
                parseStatus++;
                revise();
                return PARSE_INCOMPLETE;
                }
            }
        else
            {
            System.err.println("Invalid tone number " + parseStatus + " or data length " + data.length);
            return PARSE_FAILED;
            }
        }
        
    byte[] prepareBuffer(byte[] buf, Model model, boolean toWorkingMemory, int t)
        {
        buf[0] = (byte)0xF0;
        buf[1] = (byte)0x41;
        buf[2] = (byte)getID();
        buf[3] = (byte)0x46;
        buf[4] = (byte)0x12;
        if (toWorkingMemory || model.get("bank", 0) >= 2)       // we can't write to preset banks
            {
            buf[5] = (byte)0x00;
            buf[6] = (byte)0x08;
            buf[7] = (byte)0x20;
            buf[8] = (byte)0x00;
            }
        else
            {
            int bank = model.get("bank", 0);
            int number = model.get("number", 0);
            buf[5] = (byte)(bank == 0 ? 0x01 : 0x02);
            buf[6] = (byte)(number + 0x40);
            buf[7] = (byte)(0x20);
            buf[8] = (byte)0x00;
            }
        buf[buf.length - 1] = (byte)0xF7;
                
        return buf;
        }

    public Object[] emitAll(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        if (tempModel == null)
            tempModel = getModel();

        Object[] data = new Object[5];
                
        // patch common
        byte[] buf = prepareBuffer(new byte[34 + 11], tempModel, toWorkingMemory, 0);
                
        int pos = 9;
        String name = model.get("name", "") + "            ";
        for(int i = 0; i < 12; i++)
            {
            buf[pos++] = (byte)(name.charAt(i) & 0x7F);
            }
        for(int i = 12; i < allGlobalParameters.length; i++)    // skip name parameters
            {
            buf[pos++] = (byte)(model.get(allGlobalParameters[i], 0) & 0x7F);
            }
        buf[buf.length - 2] = produceChecksum(buf, 9 - 4, 34 + 11 - 2);
        data[0] = buf;  
        
        
        // tones
        for(int t = 1; t < 5; t++)
            {
            buf = prepareBuffer(new byte[116 + 11], tempModel, toWorkingMemory, t);
            buf[7] += (t + 7);  // So tone 1 is 28, tone 2 is 29, tone 3 is 2A, etc.
                        
            pos = 9;
            for(int i = 0; i < allToneParameters.length; i++)
                {
                if (allToneParameters[i].equals("-")) continue;
                byte[] d = getData("tone" + t + allToneParameters[i]);
                for(int j = 0; j < d.length; j++)
                    {
                    buf[pos++] = d[j];
                    }
                }
            buf[buf.length - 2] = produceChecksum(buf, 9 - 4, 116 + 11 - 2);
            data[t] = buf;
            }
                
        return data;
        }


    //// I CAN GET ALL 4 PATCH SEGMENTS WITH
    ////    F0 41 10 46 11 01 48 20 00 00 00 0C 00 0B F7

    public byte[] requestDump(Model tempModel)
        {
        // performRequestDump has already changed the patch.  At this point the patch is in local memory, so we're
        // going to just call requestCurrentDump.  This allows us to grab presets A and B as well as internal and card.
        return requestCurrentDump();
        
        /*
        // update our patch, since Roland won't do it
        model.set("bank", tempModel.get("bank"));
        model.set("number", tempModel.get("number"));
        
        byte AA = ((tempModel.get("bank") == 0) ? (byte)0x01 : (byte)0x02);             // bank
        byte BB = (byte)(0x40 + (tempModel.get("number")));                                             // number
        byte CC = (byte)(0x20);
        byte DD = (byte)(0x00);
        
        byte checksum = produceChecksum(new byte[] { AA, BB, CC, DD, (byte)0x00, (byte)0x00, (byte)0x0C, (byte)0x00 });
        return new byte[] { (byte)0xF0, (byte)0x41, getID(), (byte)0x46, (byte)0x11, 
        AA, BB, CC , DD, (byte)0x00, (byte)0x00, (byte)0x0C, (byte)0x00, checksum, (byte)0xF7 }; 
        */
        }
    
    int requestPerformancePart = 0;
    public byte[] requestCurrentDump()
        {
        // Change the patch/performance button to "patch" -- this is parameter 0 in system
        tryToSendSysex(new byte[] { (byte)0xF0, 0x41, getID(), 0x46, 0x12, 0x00, 0x00, 0x00, 0x00, 0x01, 
            produceChecksum(new byte[] { 0x00, 0x00, 0x00, 0x00, 0x01}), (byte)0xF7 });

		// It takes a second for this to take effect
		simplePause(200);

        byte AA = (byte)(0x00);
        byte BB = (byte)(0x08);
        byte CC = (byte)(0x20);
        byte DD = (byte)(0x00);
        
        if (requestPerformancePart != 0)
            {
            BB = (byte) (0x01 * requestPerformancePart);
            }
        
        byte checksum = produceChecksum(new byte[] { AA, BB, CC, DD, (byte)0x00, (byte)0x00, (byte)0x0C, (byte)0x00 });
        byte[] b = new byte[] { (byte)0xF0, (byte)0x41, getID(), (byte)0x46, (byte)0x11, 
            AA, BB, CC , DD, (byte)0x00, (byte)0x00, (byte)0x0C, (byte)0x00, checksum, (byte)0xF7 }; 
        return b;
        }
    
    public static final int MAXIMUM_NAME_LENGTH = 12;
    public String revisePatchName(String name)
        {
        name = super.revisePatchName(name);  // trim first time
        if (name.length() > MAXIMUM_NAME_LENGTH)
            name = name.substring(0, MAXIMUM_NAME_LENGTH);
        
        StringBuffer nameb = new StringBuffer(name);  
        int len = nameb.length();                          
        for(int i = 0 ; i < len; i++)
            {
            char c = nameb.charAt(i);
            if (c < 32 || c > 127)
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
        
    public static String getSynthName() { return "Roland JV-80/880"; }
    
    public String getPatchName(Model model) { return model.get("name", "Untitled  "); }

	// It takes a long time (about 700ms) for this to take effect
    public int getPauseAfterChangePatch() { return 700; }

    public int getPauseAfterSendAllParameters() { return 300; }

    public void changePatch(Model tempModel)
        {
        byte BB = (byte)tempModel.get("bank");
        byte NN = (byte)tempModel.get("number");
        byte BC = (byte)(BB < 2 ? 80 : 81);
        byte PC = (byte)(BB == 0 || BB == 2 ? NN : NN + 64);
        
        model.set("bank", tempModel.get("bank"));
        model.set("number", tempModel.get("number"));
        
        try 
            {
            // Change the patch/performance button to "patch" -- this is parameter 0 in system
            tryToSendSysex(new byte[] { (byte)0xF0, 0x41, getID(), 0x46, 0x12, 0x00, 0x00, 0x00, 0x00, 0x01, 
                produceChecksum(new byte[] { 0x00, 0x00, 0x00, 0x00, 0x01}), (byte)0xF7 });

			// It takes a long time (about 700ms) for this to take effect
			simplePause(700);

            tryToSendMIDI(new ShortMessage(ShortMessage.CONTROL_CHANGE, getChannelOut(), 0, BC));

			// It takes a long time (about 700ms) for this to take effect
			simplePause(700);

            tryToSendMIDI(new ShortMessage(ShortMessage.PROGRAM_CHANGE, getChannelOut(), PC, 0));
            }
        catch (Exception e) { Synth.handleException(e); }
        }
    
    public Model getNextPatchLocation(Model model)
        {
        int bank = model.get("bank");
        int number = model.get("number");
        
        number++;
        if (number >= 64)
            {
            bank++;
            number = 0;
            if (bank >= 4)
                bank = 0;
            }
                
        Model newModel = buildModel();
        newModel.set("bank", bank);
        newModel.set("number", number);
        return newModel;
        }

    public String getPatchLocationName(Model model)
        {
        // getPatchLocationName() is called from sprout() as a test to see if we should enable
        // batch downloading.  If we haven't yet created an .init file, then parameters won't exist
        // yet and this method will bomb badly.  So we return null in this case.
        if (!model.exists("number")) return null;
        if (!model.exists("bank")) return null;
        
        return BANKS[model.get("bank")] + " " + (model.get("number") + 1 < 10 ? "0" : "") + ((model.get("number") + 1));
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
        
	// Takes a long time for batches to come in, particularly Orch Stab 2 (patch Internal 11, dunno why)
    public int getBatchDownloadWaitTime() { return 2000; }

    public String[] getBankNames() { return BANKS; }

	/** Return a list of all patch number names.  Default is { "Main" } */
	public String[] getPatchNumberNames()  { return buildIntegerNames(64, 1); }

	/** Return a list whether patches in banks are writeable.  Default is { false } */
	public boolean[] getWriteableBanks() { return new boolean[] { true, true, false, false }; }

	/** Return a list whether individual patches can be written.  Default is FALSE. */
	public boolean getSupportsPatchWrites() { return true; }

	public int getPatchNameLength() { return 12; }

    public boolean librarianTested() { return true; }
    }
