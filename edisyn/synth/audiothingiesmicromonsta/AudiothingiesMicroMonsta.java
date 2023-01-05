/***
    Copyright 2022 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.audiothingiesmicromonsta;

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
   A patch editor for the Audiothingies MicroMonsta (original only)
        
   @author Sean Luke
*/

public class AudiothingiesMicroMonsta extends Synth
    {
    public static final String[] OSCILLATOR_WAVEFORMS = new String[] 
    {
    /*
      "mph   Morph Tri-Saw-Sq-Pulse",
      "sin   Sine (+ FM)",
      "tri   Triangle (+ Folding)",
      "saw   Sawtooth (+ Phased)",
      "squ   Square (+ Pulsewidth)",
      "ss1   Supersaw",
      "ss2   Supersaw Unbalanced",
      "sws   Synced Sawtooth",
      "sqs   Synced Square",
      "rz1   Saw Phase Distortion",
      "rz2   Tri Phase Distortion",
      "rz3   Trap Phase Distortion",
      "w01   WT Saw Formant",
      "w02   WT Squ Formant",
      "w03   WT Random",
      "w04   WT Sine + Partials",
      "w05   WT Guitar Formant",
      "w06   WT Sample 1",
      "w07   WT Sample 2",
      "w08   WT Sample 3",
      "w09   WT Sample 4",
      "w10   WT Organ",
      "w11   WT Sine + Even",
      "w12   WT Sine + Odd",
      "w13   WT Sine + Even + Odd",
      "w14   WT Vowels",
      "w15   WT I Love Synthesizers",
      "w16   User WT 1",
      "w17   User WT 2",
      "w18   User WT 3",
      "w19   User WT 4",
      "w20   User WT 5",
      "w21   User WT 6",
      "w22   User WT 7",
      "w23   User WT 8",
      "w24   User WT 9",
      "w25   User WT 10",
      "w26   User WT 11",
      "w27   User WT 12",
      "w28   User WT 13",
      "w29   User WT 14",
      "w30   User WT 15",
      "z01   WT- Saw Formant",
      "z02   WT- Squ Formant",
      "z03   WT- Random",
      "z04   WT- Sine + Partials",
      "z05   WT- Guitar Formant",
      "z06   WT- Sample 1",
      "z07   WT- Sample 2",
      "z08   WT- Sample 3",
      "z09   WT- Sample 4",
      "z10   WT- Organ",
      "z11   WT- Sine + Even",
      "z12   WT- Sine + Odd",
      "z13   WT- Sine + Even + Odd",
      "z14   WT- Vowels",
      "z15   WT- I Love Synthesizers",
    */
    "Morph Tri-Saw-Sq-Pulse",
    "Sine (+ FM)",
    "Triangle (+ Folding)",
    "Sawtooth (+ Phased)",
    "Square (+ Pulsewidth)",
    "Supersaw Balanced",
    "Supersaw Unbalanced",
    "Synced Sawtooth",
    "Synced Square",
    "PD Resonant Sawtooth",
    "PD Resonant Triangle",
    "PD Resonant Trapezoid",
    "WT 1 Saw Formant",
    "WT 2 Squ Formant",
    "WT 3 Random",
    "WT 4 Sine + Partials",
    "WT 5 Guitar Formant",
    "WT 6 Sample 1",
    "WT 7 Sample 2",
    "WT 8 Sample 3",
    "WT 9 Sample 4",
    "WT 10 Organ",
    "WT 11 Sine + Even",
    "WT 12 Sine + Odd",
    "WT 13 Sine + Even + Odd",
    "WT 14 Vowels",
    "WT 15 I Love Synthesizers",
    "WT 16 User",
    "WT 17 User",
    "WT 18 User",
    "WT 19 User",
    "WT 20 User",
    "WT 21 User",
    "WT 22 User",
    "WT 23 User",
    "WT 24 User",
    "WT 25 User",
    "WT 26 User",
    "WT 27 User",
    "WT 28 User",
    "WT 29 User",
    "WT 30 User",
    "WT- 1 Saw Formant",
    "WT- 2 Squ Formant",
    "WT- 3 Random",
    "WT- 4 Sine + Partials",
    "WT- 5 Guitar Formant",
    "WT- 6 Sample 1",
    "WT- 7 Sample 2",
    "WT- 8 Sample 3",
    "WT- 9 Sample 4",
    "WT- 10 Organ",
    "WT- 11 Sine + Even",
    "WT- 12 Sine + Odd",
    "WT- 13 Sine + Even + Odd",
    "WT- 14 Vowels",
    "WT- 15 I Love Synthesizers",
    "WT- 16 User",
    "WT- 17 User",
    "WT- 18 User",
    "WT- 19 User",
    "WT- 20 User",
    "WT- 21 User",
    "WT- 22 User",
    "WT- 23 User",
    "WT- 24 User",
    "WT- 25 User",
    "WT- 26 User",
    "WT- 27 User",
    "WT- 28 User",
    "WT- 29 User",
    "WT- 30 User",
    };

    public static final String[] SUB_WAVEFORMS = new String[] { "Sine", "Triangle", "Sawtooth", "Square" };
    public static final String[] FILTER_TYPES = new String[] { "1-Pole LP", "2-Pole LP", "3-Pole LP", "4-Pole LP", "2-Pole HP", "2-Pole BP", "Notch", "Phaser" };
    public static final String[] SYNTH_MODES = new String[] { "Poly 1", "Poly 2", "Mono", "Legato" };
    public static final String[] PAN_SPREAD_ALGORITHMS = new String[] { "Linear", "Equal" };
    public static final String[] ENCODER_ASSIGNMENTS = new String[]
    { 
    "None", 
    "Osc 1 Waveshape", 
    "Osc 1 LFO 1 Mod", 
    "Osc 2 Waveshape", 
    "Osc 2 Env 3 Mod", 
    "Filter Cutoff", 
    "Filter Resonance", 
    "Cutoff Env2 Mod", 
    "Cutoff LFO2 Mod", 
    "Env 1 Attack", 
    "Env 1 Decay", 
    "Env 1 Sustain", 
    "Env 1 Release", 
    "Env 2 Attack", 
    "Env 2 Decay", 
    "Env 2 Sustain", 
    "Env 2 Release", 
    "Env 3 Attack", 
    "Env 3 Decay", 
    "Env 3 Sustain", 
    "Env 3 Release", 
    "LFO 1 Speed", 
    "LFO 2 Speed", 
    "LFO 3 Speed", 
    "Patch 1 Amount", 
    "Patch 2 Amount", 
    "Patch 3 Amount", 
    "Patch 4 Amount", 
    "Patch 5 Amount", 
    "Patch 6 Amount", 
    "Scaler 1 Amount", 
    "Scaler 2 Amount", 
    "Scaler 3 Amount", 
    "Lag Amount", 
    "Arpeggiator", 
    "Arp Gate", 
    "Arp Octaves", 
    "Arp Speed", 
    "Arp Latch", 
    "Chorder", 
    "Chorder Key", 
    "Chorder Scale", 
    "Delay Time", 
    "Delay Feedback", 
    "FX LFO Speed", 
    "FX LFO Depth", 
    "Delay Balance", 
    "FX Filter Cutoff", 
    "FX Level", 
    "Glide Time", 
    "Patch Tempo", 
    };

    public static final String[] PATCH_SOURCES = new String[]
    { 
    "Keyboard Tracking + Bend",
    "Velocity",
    "Aftertouch",
    "Pitch Bend",
    "Mod Wheel",
    "Offset",
    "Random",
    "Env 1",
    "Env 2", 
    "Env 3",
    "LFO 1",
    "LFO 2", 
    "LFO 3",
    };
    public static final String[] LAG_SOURCES = PATCH_SOURCES;

    public static final String[] PATCH_DESTINATIONS = new String[]
    { 
    "Pitch",
    "Pitch (Fine)",
    "Osc 1 Tune",
    "Osc 1 Tune (Fine)",
    "Osc 2 Tune",
    "Osc 2 Tune (Fine)",
    "Osc 1 Waveshape",
    "Osc 2 Waveshape",
    "Noise Color",
    "Osc 1 Mix",
    "Sub Mix",
    "Ringmod Mix",
    "Noise Mix",
    "Filter Cutoff",
    "Filter Resonance",
    "Env 1 Attack Time",
    "Env 1 Decay Time",
    "Env 1 Release Time",
    "Env 2 Attack Time",
    "Env 2 Decay Time",
    "Env 2 Release Time",
    "Env 3 Attack Time",
    "Env 3 Decay Time",
    "Env 3 Release Time",
    "LFO 1 Speed",
    "LFO 2 Speed",
    "LFO 3 Speed",
    };

    public static final String[] SCALER_SOURCES = new String[]
    { 
    "Velocity",
    "Aftertouch",
    "Mod Wheel",
    "Env 1",
    "Env 2", 
    "Env 3",
    };

    public static final String[] SCALER_DESTINATIONS = new String[]
    { 
    "Env 1",
    "Env 2", 
    "Env 3",
    "LFO 1",
    "LFO 2", 
    "LFO 3",
    };

    public static final String[] FX_TYPES = new String[]
    { 
    "Stereoizer",
    "Ping-Pong Delay", 
    "Filtered Delay",
    "Modulated Delay",
    "Chorus / Flanger", 
    };

    public static final String[] ARP_STYLES = new String[] { "Up", "Triplet Up", "Down", "Triplet Down", "Up-Down", "Random", "As Played" };
    public static final String[] ARP_SPEEDS = new String[] { "1/1", "1/2", "1/3", "1/4", "1/6", "1/8", "1/12", "1/16", "1/24", "1/32" };

    public static final String[] ARP_NOTES = new String[]
    {
    "Arp Note",
    "Low -12",
    "Note 1",
    "Note 2",
    "Note 3",
    "Note 4",
    "Note 5",
    "Note 6",
    "Note 7",
    "Note 8",
    "High +12",
    "Chord"
    };

    public static final String[] ARP_EVENTS = new String[]
    {
    "Note",
    "Accent",
    "Slide",
    "Slide+Acc",
    "Tie",                  //      "Extend Previous",
    "Rest",
    };
                
    public static final String[] CHORDER_KEYS = new String[] { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
    public static final String[] CHORDER_SCALES = new String[] { "Major", "Minor", "Harmonic Minor", "Melodic Minor" };
    public static final String[] CHORDER_INTERVALS = new String[] { "-1 x Octave", "-1 x 7th", "-1 x 6th", "-1 x 5th", "-1 x 4th", "-1 x 3rd", "-1 x 2nd", "Off", "2nd", "3rd", "4th", "5th", "6th", "7th", "+1 x Octave", "1 Octave + 2nd", "1 Octave + 3rd", "1 Octave + 4th", "1 Octave + 5th", "1 Octave + 6th", "1 Octave + 7th", "+2 x Octave" };

    public static final String[] LFO_WAVEFORMS = new String[] { "Sine", "Triangle", "Sawtooth", "Square", "Sample & Hold", "Smooth Random", "Stepped Sequence" };
    public static final String[] LFO_SYNCED_SPEEDS = new String[] 
    {  
    "16 B", "12 B", "8 B", "6 B", "4 B", "3 B", "2 B", "1.5 B", 
    "1 B", "3/4 B", "2/3 B", "1/2 B", "3/8 B", "1/3 B", 
    "4th N", "6th N", "8th N", "12th N", "16th N", "24th N", "32nd N", "48th N", "64th N", "96th N", 
    };
    public static final String[] LFO_SYNCED_DELAYS = new String[] 
    {  
    "96th N", "64th N", "48th N", "32nd N", "24th N", "16th N", "12th N", "8th N", "6th N", "4th N", 
    "1/3 B", "3/8 B", "1/2 B", "2/3 B", "3/4 B", "1 B", 
    "1.5 B", "2 B", "3 B", "4 B", "6 B", "8 B", "12 B", "16 B", 
    };
                
    public static final String[] ENVELOPE_TYPES = new String[] { "Fast", "Exponential", "Linear" };
    // public static final String[] SUB_OCTAVES = new String[] { "One Octave Down", "Two Octaves Down" };


    public static HashMap<Integer, String> ccToArg = null;
    public static HashMap<String, Integer> argToCC = null;
    public static HashMap<Integer, String> nrpnToArg = null;
    public static HashMap<String, Integer> argToNRPN = null;
        
    public AudiothingiesMicroMonsta()
        {
        if (ccToArg == null)
            {
            ccToArg = new HashMap<>();
            argToCC = new HashMap<>();
            nrpnToArg = new HashMap<>();
            argToNRPN = new HashMap<>();
                
            for(int i = 0; i < CC.length; i++)
                {
                ccToArg.put(CC[i], CC_ARG[i]);
                argToCC.put(CC_ARG[i], CC[i]);
                }
                                
            for(int i = 0; i < NRPN.length; i++)
                {
                nrpnToArg.put(NRPN[i], NRPN_ARG[i]);
                argToNRPN.put(NRPN_ARG[i], NRPN[i]);
                }
            }

        SynthPanel soundPanel = new SynthPanel(this);
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        hbox.add(addVoice(Style.COLOR_B()));
        hbox.addLast(addGlobal(Style.COLOR_C()));
        vbox.add(hbox);
        
        hbox = new HBox();
        hbox.add(addOscillator(1, Style.COLOR_A()));
        hbox.addLast(addOscillator(2, Style.COLOR_A()));
        vbox.add(hbox);
        
        hbox = new HBox();
        hbox.add(addOscillators(Style.COLOR_A()));
        hbox.addLast(addMixer(Style.COLOR_A()));
        vbox.add(hbox);
        
        hbox = new HBox();
        hbox.add(addFilter(Style.COLOR_B()));
        hbox.addLast(addFX(Style.COLOR_B()));
        vbox.add(hbox);
        
        hbox = new HBox();
        hbox.add(addChorder(Style.COLOR_C()));
        hbox.addLast(addEncoders(Style.COLOR_C()));
        vbox.add(hbox);

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Sound", soundPanel);
                
                
        SynthPanel envelopePanel = new SynthPanel(this);
        vbox = new VBox();

        hbox = new HBox();
        hbox.add(addLFO(1, Style.COLOR_A()));
        hbox.addLast(addEnvelope(1, Style.COLOR_B()));
        vbox.add(hbox);
                
        hbox = new HBox();
        hbox.add(addLFO(2, Style.COLOR_A()));
        hbox.addLast(addEnvelope(2, Style.COLOR_B()));
        vbox.add(hbox);

        hbox = new HBox();
        hbox.add(addLFO(3, Style.COLOR_A()));
        hbox.addLast(addEnvelope(3, Style.COLOR_B()));
        vbox.add(hbox);
        
        vbox.add(addLFOSteps(Style.COLOR_A()));
        
        envelopePanel.add(vbox, BorderLayout.CENTER);
        addTab("LFOs and Envelopes", envelopePanel);


                
        SynthPanel modulationPanel = new SynthPanel(this);
        
        vbox = new VBox();
        vbox.add(addArpeggiator(Style.COLOR_B()));
        vbox.add(addMatrix(Style.COLOR_A()));
        hbox = new HBox();
        hbox.add(addScaler(Style.COLOR_B()));
        hbox.addLast(addLag(Style.COLOR_C()));
        vbox.add(hbox);
                
        modulationPanel.add(vbox, BorderLayout.CENTER);
        addTab("Modulation", modulationPanel);
        
        loadDefaults();        
        }
                
    public String getDefaultResourceFileName() { return "AudiothingiesMicroMonsta.init"; }
    public String getHTMLResourceFileName() { return "AudiothingiesMicroMonsta.html"; }

    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        JTextField val = new SelectedTextField("" + (model.get("number") + 1), 3);
        
        while(true)
            {
            boolean result = showMultiOption(this, new String[] { "Patch Number"}, 
                new JComponent[] { val }, title, "Enter the Patch Number (1 ... 384)");
                
            if (result == false) 
                return false;
                                
            int n;
            try { n = Integer.parseInt(val.getText()); }
            catch (NumberFormatException e)
                {
                showSimpleError(title, "The Patch Number must be an integer 1 ... 384");
                continue;
                }
            if ((n < 1) || (n > 384))
                {
                showSimpleError(title, "The Patch Number must be an integer 1 ... 384");
                continue;
                }
                                
            change.set("number", n - 1);
                        
            return true;
            }
        }
        
    public JFrame sprout()
        {
        JFrame frame = super.sprout();
        receiveCurrent.setEnabled(false);
        receivePatch.setEnabled(false);
        receiveNextPatch.setEnabled(false);
        getAll.setEnabled(false);
        
        addMicroMonstaMenu();
        return frame;
        }      
        
    JMenuItem download = new JMenuItem("Set Librarian Download Point (Now 1)" );

    public void addMicroMonstaMenu()
        {
        JMenu menu = new JMenu("MicroMonsta");
        menubar.add(menu);
        menu.add(download);

        download.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt)
                {
                if (isLibrarianOpen())
                    {
                    int col = getLibrarian().getCurrentColumn();
                    int row = getLibrarian().getCurrentRow();
                        
                    if (col == 1 && row >= 0 && row < 384)
                        {
                        nextPatchPosition = row;
                        download.setText("Set Librarian Download Point (Now " + (row + 1) + ")");
                        }
                    else
                        {
                        showSimpleError("Invalid Selection", "Please select a patch in the Micromonsta's Bank in the Librarian first." );
                        }
                    }
                else
                    {
                    showSimpleError("Librarian Closed", "Please open the Librarian and select a patch in the Micromonsta's Bank first." );
                    }
                }
            });
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
        comp = new PatchDisplay(this, 4);
        vbox.add(comp);
        
        comp = new StringComponent("Patch Name", this, "name", 8, 
            "<html>Name must be up to 8 ASCII characters, except { } | ~<br>" + 
            "The character \\ is displayed as " + '\u00A2' + " on the unit.</html>")
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
        
        hbox.add(Strut.makeHorizontalStrut(140));
                
        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }



    public JComponent addOscillator(int osc, Color color)
        {
        Category category = new Category(this, "Oscillator " + osc, color);
        category.makePasteable("osc");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = OSCILLATOR_WAVEFORMS;
        comp = new Chooser("Type", this, "osc" + osc + "algorithm", params);
        vbox.add(comp);
        hbox.add(vbox);

        if (osc == 1)
            {
            comp = new LabelledDial("Transpose", this, "oscstranspose", color, 40, 88, 64);
            hbox.add(comp);
            }
        else
            {
            comp = new LabelledDial("Range", this, "osc2range", color, 40, 88, 64);
            hbox.add(comp);
            }

        comp = new LabelledDial("Fine Tune", this, "osc" + osc + "finetune", color, 0, 127, 64);
        hbox.add(comp);

        comp = new LabelledDial("Wave Shape", this, "osc" + osc + "modifier", color, 0, 127);
        hbox.add(comp);

        if (osc == 1)
            {
            comp = new LabelledDial("LFO 1 Mod", this, "osc1lfo1amount", color, 0, 127, 64);
            hbox.add(comp);
            }
        else
            {
            comp = new LabelledDial("Env 3 Mod", this, "osc2env3amount", color, 0, 127, 64);
            hbox.add(comp);
            }

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addOscillators(Color color)
        {
        Category category = new Category(this, "Sub / Noise" , color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = SUB_WAVEFORMS;
        comp = new Chooser("Sub Waveform", this, "subwaveform", params);
        vbox.add(comp);
        hbox.add(vbox);
                
        comp = new LabelledDial("Sub Octave", this, "suboctave", color, 62, 63, 64);
        hbox.add(comp);

        comp = new LabelledDial("Noise Color", this, "noisecolor", color, 0, 127, 64);
        hbox.add(comp);
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addMixer(Color color)
        {
        Category category = new Category(this, "Mixer" , color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        comp = new LabelledDial("Osc 1", this, "osc1mix", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Osc 2", this, "osc2mix", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Sub", this, "submix", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Ring Mod", this, "ringmodmix", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Noise", this, "noisemix", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("VCA Drive", this, "amdrive", color, 58, 88, 64);
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

        

    public JComponent addFilter(Color color)
        {
        Category category = new Category(this, "Filter", color);
                
        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        params = FILTER_TYPES;
        comp = new Chooser("Type", this, "filtertype", params);
        vbox.add(comp);
        hbox.add(vbox);
                
        comp = new LabelledDial("Cutoff", this, "filterfrequency", color, 0, 127);
        hbox.add(comp);
                
        comp = new LabelledDial("Resonance", this, "filterresonance", color, 0, 127);
        hbox.add(comp);
                
        comp = new LabelledDial("Env 2 Mod", this, "filterenvamount", color, 0, 127);
        hbox.add(comp);
                
        comp = new LabelledDial("LFO 2 Mod", this, "filterlfoamount", color, 0, 127);
        hbox.add(comp);
                
        comp = new LabelledDial("Keybord", this, "filterkbdtracking", color, 0, 4)
            {
            public String map(int val)
                {
                return ("" + (val * 25) + "%");
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Tracking");
        hbox.add(comp);
                
        category.add(hbox, BorderLayout.WEST);
        return category;
        }


    public JComponent addVoice(Color color)
        {
        Category category = new Category(this, "Voice", color);
                
        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        params = SYNTH_MODES;
        comp = new Chooser("Synth Mode", this, "voicemode", params);
        vbox.add(comp);
        
        params = PAN_SPREAD_ALGORITHMS;
        comp = new Chooser("Pan Spread Algorithm", this, "panspreadalgo", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Unison", this, "voiceunison", color, 0, 2)
            {
            public String map(int val)
                {
                if (val == 0) return "1";
                else if (val == 1) return "2";
                else return "4";
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Voices");
        hbox.add(comp);
                
        comp = new LabelledDial("Voice", this, "voicedetune", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Detune");
        hbox.add(comp);
                
        comp = new LabelledDial("Osc 1/2", this, "oscdetune", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Detune");
        hbox.add(comp);
                
        comp = new LabelledDial("Pan Spread", this, "panspreadamount", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);
                
        category.add(hbox, BorderLayout.WEST);
        return category;
        }

    public JComponent addGlobal(Color color)
        {
        Category category = new Category(this, "Global", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        comp = new LabelledDial("Pitch Bend", this, "globalspitchbendrange", color, 0, 24);
        ((LabelledDial)comp).addAdditionalLabel("Range");
        hbox.add(comp);

        comp = new LabelledDial("Glide Time", this, "globalsglide", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("EQ Filter", this, "globalseqcutoff", color, 0, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Cutoff");
        hbox.add(comp);

        comp = new LabelledDial("Tempo", this, "globalstempo", color, 20, 255);
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addEncoders(Color color)
        {
        Category category = new Category(this, "Encoders", color);
        category.makePasteable("encoderassign");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = ENCODER_ASSIGNMENTS;
        comp = new Chooser("Encoder 1", this, "encoderassign1", params);
        vbox.add(comp);

        params = ENCODER_ASSIGNMENTS;
        comp = new Chooser("Encoder 3", this, "encoderassign3", params);
        vbox.add(comp);
        hbox.add(vbox);
                
        vbox = new VBox();
        params = ENCODER_ASSIGNMENTS;
        comp = new Chooser("Encoder 2", this, "encoderassign2", params);
        vbox.add(comp);

        params = ENCODER_ASSIGNMENTS;
        comp = new Chooser("Encoder 4", this, "encoderassign4", params);
        vbox.add(comp);
        hbox.add(vbox);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addMatrix(Color color)
        {
        Category category = new Category(this, "Patch Matrix", color);
        category.makeDistributable("patch");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        
        for(int i = 1; i <= 6; i++)             // note <=
            {
            VBox inner = new VBox();
            params = PATCH_SOURCES;
            comp = new Chooser("Source " + i, this, "patch" + i + "source", params);
            inner.add(comp);
                        
            params = PATCH_DESTINATIONS;
            comp = new Chooser("Destination " + i, this, "patch" + i + "destination", params);
            inner.add(comp);
            hbox.add(inner);

            comp = new LabelledDial("Amount " + i, this, "patch" + i + "amount", color, 0, 127, 64);
            hbox.add(comp);
                        
            if (i == 3 || i == 6)
                {
                vbox.add(hbox);
                hbox = new HBox();
                }
            else
                {
                hbox.add(Strut.makeHorizontalStrut(8));
                }
            }

        category.add(vbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addScaler(Color color)
        {
        Category category = new Category(this, "Scaler", color);
        category.makeDistributable("scaler");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        for(int i = 1; i <= 3; i++)             // note <=
            {
            VBox inner = new VBox();
            params = SCALER_SOURCES;
            comp = new Chooser("Scaler Source " + i, this, "scaler" + i + "source", params);
            inner.add(comp);
                        
            params = SCALER_DESTINATIONS;
            comp = new Chooser("Destination " + i, this, "scaler" + i + "destination", params);
            inner.add(comp);
            hbox.add(inner);

            comp = new LabelledDial("Amount " + i, this, "scaler" + i + "amount", color, 0, 127, 64);
            hbox.add(comp);
                        
            if (i == 3)
                {
                // do nothing
                }
            else
                {
                hbox.add(Strut.makeHorizontalStrut(8));
                }
            }
        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addLag(Color color)
        {
        Category category = new Category(this, "Lag", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox inner = new VBox();
        params = LAG_SOURCES;
        comp = new Chooser("Lag Source", this, "lagsource", params);
        inner.add(comp);
        hbox.add(inner);
                
        comp = new LabelledDial("Amount", this, "lagamount", color, 0, 127);
        hbox.add(comp);
                
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }



    public JComponent addChorder(Color color)
        {
        Category category = new Category(this, "Chorder", color);
        category.makePasteable("chorder");
                
        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        params = CHORDER_SCALES;
        comp = new Chooser("Scale", this, "chorderscale", params);
        vbox.add(comp);

        comp = new CheckBox("On", this, "chorderstatus");
        vbox.add(comp);
        hbox.add(vbox);

        vbox = new VBox();
        params = CHORDER_INTERVALS;
        comp = new Chooser("Interval 1", this, "chorderitvl1", params);
        vbox.add(comp);

        params = CHORDER_INTERVALS;
        comp = new Chooser("Interval 2", this, "chorderitvl2", params);
        vbox.add(comp);
        hbox.add(vbox);

        vbox = new VBox();
        params = CHORDER_INTERVALS;
        comp = new Chooser("Interval 3", this, "chorderitvl3", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Key", this, "chorderkey", color, 0, 11)
            {
            public String map(int val)
                {
                return CHORDER_KEYS[val];
                }
            };
        hbox.add(comp);
                                
        category.add(hbox, BorderLayout.WEST);
        return category;
        }




    LabelledDial fxParam[] = new LabelledDial[7];
        
    public JComponent addFX(Color color)
        {
        Category category = new Category(this, "Effects", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        fxParam[0] = new LabelledDial("Delay Time", this, "delaytime", color, 0, 127);
        fxParam[1] = new LabelledDial("Delay", this, "delayfeedback", color, 0, 127, 64);
        fxParam[1].addAdditionalLabel("Feedback");
        fxParam[2] = new LabelledDial("Delay", this, "fxbalance", color, 0, 127, 64);
        fxParam[2].addAdditionalLabel("Balance");
        fxParam[3] = new LabelledDial("Filter Cutoff", this, "fxcutoff", color, 0, 127, 64);
        fxParam[4] = new LabelledDial("LFO Speed", this, "fxspeed", color, 0, 127);
        fxParam[5] = new LabelledDial("LFO Depth", this, "fxdepth", color, 0, 127);
        fxParam[6] = new LabelledDial("Mix", this, "fxmix", color, 0, 127);           // or "Mix" ?
        
        HBox paramBox = new HBox();
        
        VBox vbox = new VBox();
        params = FX_TYPES;
        comp = new Chooser("Type", this, "fxalgorithm", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                int val = model.get(key);
                paramBox.removeAll();
                if (val == 0)   // stereoizer
                    {
                    paramBox.add(fxParam[3]);       // cut
                    paramBox.add(fxParam[6]);       // mix
                    }
                else if (val == 1)      // ping pong
                    {
                    paramBox.add(fxParam[0]);       // time
                    paramBox.add(fxParam[1]);       // feedback
                    paramBox.add(fxParam[2]);       // balance
                    paramBox.add(fxParam[3]);       // cut
                    paramBox.add(fxParam[6]);       // mix
                    }
                else if (val == 2)      // filtered delay
                    {
                    paramBox.add(fxParam[0]);       // time
                    paramBox.add(fxParam[1]);       // feedback
                    paramBox.add(fxParam[3]);       // cut
                    paramBox.add(fxParam[6]);       // mix
                    }
                else if (val == 3)      // modulated delay
                    {
                    paramBox.add(fxParam[0]);       // time
                    paramBox.add(fxParam[1]);       // feedback
                    paramBox.add(fxParam[4]);       // speed
                    paramBox.add(fxParam[5]);       // depth
                    paramBox.add(fxParam[6]);       // mix
                    }
                else if (val == 4)      // chorus/flanger
                    {
                    paramBox.add(fxParam[0]);       // time
                    paramBox.add(fxParam[1]);       // feedback
                    paramBox.add(fxParam[4]);       // speed
                    paramBox.add(fxParam[5]);       // depth
                    paramBox.add(fxParam[6]);       // mix
                    }
                else                    // uh....
                    {
                    // unreachable
                    }
                paramBox.revalidate();
                paramBox.repaint();
                }
            };
        vbox.add(comp);
        hbox.add(vbox);

        // default        
        paramBox.add(fxParam[3]);       // cut
        paramBox.add(fxParam[6]);       // mix

        hbox.add(paramBox);
                
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addLFO(int lfo, Color color)
        {
        Category category = new Category(this, "LFO " + lfo, color);
        category.makePasteable("lfo");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = LFO_WAVEFORMS;
        comp = new Chooser("Shape", this, "lfo" + lfo + "waveform", params);
        vbox.add(comp);

        comp = new CheckBox("Reset", this, "lfo" + lfo + "keysync");
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Speed", this, "lfo" + lfo + "rate", color, 0, 127 + LFO_SYNCED_SPEEDS.length)
            {
            public String map(int val)
                {
                if (val < 128) return "" + val;
                else return LFO_SYNCED_SPEEDS[val - 128];
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Delay", this, "lfo" + lfo + "delay", color, 0, 127 + LFO_SYNCED_DELAYS.length)
            {
            public String map(int val)
                {
                if (val < 128) return "" + val;
                else return LFO_SYNCED_DELAYS[val - 128];
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Attack", this, "lfo" + lfo + "attack", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Phase", this, "lfo" + lfo + "phase", color, 0, 7)
            {
            public String map(int val)
                {
                return "" + (val * 45);
                }
            };
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addLFOSteps(Color color)
        {
        Category category = new Category(this, "LFO Stepped Sequence", color);
        category.makeDistributable("lfostep");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        comp = new LabelledDial("Length", this, "lfostelength", color, 0, 7)
            {
            public String map(int val)
                {
                return "" + (val + 1);
                }
            };
        hbox.add(comp);

        for(int i = 1; i <= 8; i++)
            {
            comp = new LabelledDial("Step " + i, this, "lfostep" + i, color, 0, 127, 64);
            hbox.add(comp);
            }

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addEnvelope(int env, Color color)
        {
        Category category = new Category(this, "Envelope " + env, color);
        category.makePasteable("env");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = ENVELOPE_TYPES;
        comp = new Chooser("Type", this, "env" + env + "type", params);
        vbox.add(comp);

        comp = new CheckBox("Reset", this, "env" + env + "reset");
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Attack", this, "env" + env + "attack", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Decay", this, "env" + env + "decay", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Sustain", this, "env" + env + "sustain", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Release", this, "env" + env + "release", color, 0, 127);
        hbox.add(comp);

        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "env" + env + "attack", "env" + env + "decay", null, "env" + env + "release" },
            new String[] { null, null, "env" + env + "sustain", "env" + env + "sustain", null },
            new double[] { 0.0, 0.25 / 127, 0.25 / 127, 0.25, 0.25 / 127 },
            new double[] { 0.0, 1.0 / 127, 1.0 / 127, 1.0 / 127, 0.0 } 
            );
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addArpeggiator(Color color)
        {
        Category category = new Category(this, "Arpeggiator", color);
        category.makeDistributable("arptn");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox outer = new VBox();
        outer.add(hbox);
        
        VBox vbox = new VBox();
        params = ARP_STYLES;
        comp = new Chooser("Type", this, "arstyle", params);
        vbox.add(comp);

        HBox inner = new HBox();
        comp = new CheckBox("On", this, "armode");
        inner.add(comp);

        comp = new CheckBox("Latch", this, "arlatch");
        inner.add(comp);
        vbox.add(inner);
        hbox.add(vbox);

        comp = new LabelledDial("Gate", this, "argate", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Octaves", this, "aroctave", color, 0, 3);
        hbox.add(comp);

        comp = new LabelledDial("Speed", this, "arspeed", color, 0, 9)
            {
            public String map(int val)
                {
                return ARP_SPEEDS[val];
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Length", this, "arptnlength", color, 0, 15)
            {
            public String map(int val)
                {
                return "" + (val + 1);
                }
            };
        hbox.add(comp);

        hbox = new HBox();
        for(int i = 1; i <= 16; i++)
            {
            vbox = new VBox();
            params = ARP_NOTES;
            comp = new Chooser("Note " + i, this, "arptnstep" + i, params);
            vbox.add(comp);

            params = ARP_EVENTS;
            comp = new Chooser("Event " + i, this, "arptnrytm" + i, params);
            vbox.add(comp);
            hbox.add(vbox);
                        
            if (i == 8 || i == 16)
                {
                outer.add(hbox);
                hbox = new HBox();
                }
            else
                {
                hbox.add(Strut.makeHorizontalStrut(6));
                }
            }

        category.add(outer, BorderLayout.CENTER);
        return category;
        }



    public static String getSynthName() { return "Audiothingies MicroMonsta"; }
    

    


    public static final String[] CC_ARG = {
        "globalseqcutoff",
        "globalsglide",
        "oscstranspose",
        "delaytime",
        "delayfeedback",
        "fxspeed",
        "fxdepth",
        "fxbalance",
        "fxcutoff",
        "osc1finetune",
        "osc1modifier",
        "osc1lfo1amount",
        "osc1mix",
        "osc2range",
        "osc2finetune",
        "osc2modifier",
        "osc2env3amount",
        "osc2mix",
        "submix",
        "ringmodmix",
        "noisecolor",
        "noisemix",
        "amdrive",
        "lfostep1",
        "lfostep2",
        "lfostep3",
        "lfostep4",
        "lfostep5",
        "lfostep6",
        "lfostep7",
        "lfostep8",
        "lfostelength",
        "lfo1rate",
        "lfo1delay",
        "lfo1attack",
        "lfo3rate",
        "lfo3delay",
        "lfo3attack",
        "patch1amount",
        "patch2amount",
        "patch3amount",
        "patch4amount",
        "patch5amount",
        "patch6amount",
        "filterresonance",
        "env1release",
        "env1attack",
        "filterfrequency",
        "env1decay",
        "lfo2rate",
        "filterlfoamount",
        "lfo2delay",
        "env1sustain",
        "env2attack",
        "env2decay",
        "env2sustain",
        "env2release",
        "filterenvamount",
        "env3attack",
        "env3decay",
        "lfo2attack",
        "env3sustain",
        "env3release",
        "fxmix",
        "scaler1amount",
        "scaler2amount",
        "scaler3amount",
        "lagamount",
        "argate",
        "arptnlength",
        "panspreadamount",
        "voicedetune",
        "oscdetune",
        };

    public static final String[] NRPN_ARG = {
        "patchname0",
        "patchname1",
        "patchname2",
        "patchname3",
        "patchname4",
        "patchname5",
        "patchname6",
        "patchname7",
        "osc1algorithm",
        "osc2algorithm",
        "subwaveform",
        "suboctave",
        "filtertype",
        "filterkbdtracking",
        "env1type",
        "env1reset",
        "env2type",
        "env2reset",
        "env3type",
        "env3reset",
        "lfo1waveform",
        "lfo1rate",
        "lfo1delay",
        "lfo1phase",
        "lfo1keysync",
        "lfo2waveform",
        "lfo2rate",
        "lfo2delay",
        "lfo2phase",
        "lfo2keysync",
        "lfo3waveform",
        "lfo3rate",
        "lfo3delay",
        "lfo3phase",
        "lfo3keysync",
        "patch1source",
        "patch1destination",
        "patch2source",
        "patch2destination",
        "patch3source",
        "patch3destination",
        "patch4source",
        "patch4destination",
        "patch5source",
        "patch5destination",
        "patch6source",
        "patch6destination",
        "scaler1source",
        "scaler1destination",
        "scaler2source",
        "scaler2destination",
        "scaler3source",
        "scaler3destination",
        "lagsource",
        "fxalgorithm",
        "armode",
        "arstyle",
        "aroctave",
        "arspeed",
        "arlatch",
        "arptnstep1",
        "arptnstep2",
        "arptnstep3",
        "arptnstep4",
        "arptnstep5",
        "arptnstep6",
        "arptnstep7",
        "arptnstep8",
        "arptnstep9",
        "arptnstep10",
        "arptnstep11",
        "arptnstep12",
        "arptnstep13",
        "arptnstep14",
        "arptnstep15",
        "arptnstep16",
        "arptnrytm1",
        "arptnrytm2",
        "arptnrytm3",
        "arptnrytm4",
        "arptnrytm5",
        "arptnrytm6",
        "arptnrytm7",
        "arptnrytm8",
        "arptnrytm9",
        "arptnrytm10",
        "arptnrytm11",
        "arptnrytm12",
        "arptnrytm13",
        "arptnrytm14",
        "arptnrytm15",
        "arptnrytm16",
        "chorderstatus",
        "chorderkey",
        "chorderscale",
        "chorderitvl1",
        "chorderitvl2",
        "chorderitvl3",
        "voicemode",
        "voiceunison",
        "panspreadalgo",
        "globalspitchbendrange",
        "globalstempo",
        "encoderassign1",
        "encoderassign2",
        "encoderassign3",
        "encoderassign4",
        };

    public static final int[] CC = {
        3,
        5,
        9,
        12,
        13,
        14,
        15,
        20,
        21,
        22,
        23,
        24,
        25,
        26,
        27,
        28,
        29,
        30,
        31,
        33,
        34,
        35,
        36,
        39,
        40,
        41,
        42,
        43,
        44,
        45,
        46,
        47,
        52,
        53,
        54,
        55,
        56,
        57,
        58,
        59,
        60,
        61,
        62,
        63,
        71,
        72,
        73,
        74,
        75,
        76,
        77,
        78,
        79,
        80,
        81,
        82,
        83,
        85,
        86,
        87,
        88,
        89,
        90,
        91,
        92,
        93,
        94,
        95,
        102,
        103,
        117,
        118,
        119,
        };


    public static final int[] NRPN = {
        1,
        2,
        3,
        4,
        5,
        6,
        7,
        8,
        9,
        10,
        11,
        12,
        13,
        14,
        15,
        16,
        17,
        18,
        19,
        20,
        21,
        22,
        23,
        24,
        25,
        26,
        27,
        28,
        29,
        30,
        31,
        32,
        33,
        34,
        35,
        36,
        37,
        38,
        39,
        40,
        41,
        42,
        43,
        44,
        45,
        46,
        47,
        48,
        49,
        50,
        51,
        52,
        53,
        54,
        55,
        56,
        57,
        58,
        59,
        60,
        61,
        62,
        63,
        64,
        65,
        66,
        67,
        68,
        69,
        70,
        71,
        72,
        73,
        74,
        75,
        76,
        77,
        78,
        79,
        80,
        81,
        82,
        83,
        84,
        85,
        86,
        87,
        88,
        89,
        90,
        91,
        92,
        93,
        94,
        95,
        96,
        97,
        98,
        99,
        100,
        101,
        102,
        103,
        104,
        105,
        106,
        107,
        };

    public static final String[] SYSEX_PARAMETERS =
        {
        "oscstranspose",
        "osc1algorithm",
        "osc1finetune",
        "osc1modifier",
        "osc1lfo1amount",
        "osc1mix",
        "osc2algorithm",
        "osc2range",
        "osc2finetune",
        "osc2modifier",
        "osc2env3amount",
        "osc2mix",
        "subwaveform",
        "suboctave",
        "submix",
        "ringmodmix",
        "noisecolor",
        "noisemix",
        "filtertype",
        "filterfrequency",
        "filterresonance",
        "filterenvamount",
        "filterlfoamount",
        "filterkbdtracking",
        "amdrive",
        "env1type",
        "env1attack",
        "env1decay",
        "env1sustain",
        "env1release",
        "env1reset",
        "env2type",
        "env2attack",
        "env2decay",
        "env2sustain",
        "env2release",
        "env2reset",
        "env3type",
        "env3attack",
        "env3decay",
        "env3sustain",
        "env3release",
        "env3reset",
        "lfo1waveform",
        "lfo1rate",     // MSB
        "--",   // LSB
        "lfo1delay",    // MSB
        "--",   // LSB
        "lfo1phase",
        "lfo1attack",
        "lfo1keysync",
        "lfo2waveform",
        "lfo2rate",     // MSB
        "--",                   // LSB
        "lfo2delay",    // MSB
        "--",   // LSB
        "lfo2phase",
        "lfo2attack",
        "lfo2keysync",
        "lfo3waveform",
        "lfo3rate",     // MSB
        "--",                   // LSB
        "lfo3delay",    // MSB
        "--",   // LSB
        "lfo3phase",
        "lfo3attack",
        "lfo3keysync",
        "lfostep1",
        "lfostep2",
        "lfostep3",
        "lfostep4",
        "lfostep5",
        "lfostep6",
        "lfostep7",
        "lfostep8",
        "lfostelength",
        "patch1source",
        "patch1destination",
        "patch1amount",
        "patch2source",
        "patch2destination",
        "patch2amount",
        "patch3source",
        "patch3destination",
        "patch3amount",
        "patch4source",
        "patch4destination",
        "patch4amount",
        "patch5source",
        "patch5destination",
        "patch5amount",
        "patch6source",
        "patch6destination",
        "patch6amount",
        "scaler1source",
        "scaler1destination",
        "scaler1amount",
        "scaler2source",
        "scaler2destination",
        "scaler2amount",
        "scaler3source",
        "scaler3destination",
        "scaler3amount",
        "lagsource",
        "lagamount",
        "fxalgorithm",
        "delaytime",
        "delayfeedback",
        "fxspeed",
        "fxdepth",
        "fxbalance",
        "fxcutoff",
        "fxmix",
        "armode",
        "arstyle",
        "argate",
        "aroctave",
        "arspeed",
        "arlatch",
        "arptnstep1",
        "arptnstep2",
        "arptnstep3",
        "arptnstep4",
        "arptnstep5",
        "arptnstep6",
        "arptnstep7",
        "arptnstep8",
        "arptnstep9",
        "arptnstep10",
        "arptnstep11",
        "arptnstep12",
        "arptnstep13",
        "arptnstep14",
        "arptnstep15",
        "arptnstep16",
        "arptnrytm1",
        "arptnrytm2",
        "arptnrytm3",
        "arptnrytm4",
        "arptnrytm5",
        "arptnrytm6",
        "arptnrytm7",
        "arptnrytm8",
        "arptnrytm9",
        "arptnrytm10",
        "arptnrytm11",
        "arptnrytm12",
        "arptnrytm13",
        "arptnrytm14",
        "arptnrytm15",
        "arptnrytm16",
        "arptnlength",
        "chorderstatus",
        "chorderkey",
        "chorderscale",
        "chorderitvl1",
        "chorderitvl2",
        "chorderitvl3",
        "voicemode",
        "voiceunison",
        "voicedetune",
        "oscdetune",
        "panspreadalgo",
        "panspreadamount",
        "globalspitchbendrange",
        "globalsglide",
        "globalseqcutoff",
        "globalstempo", // MSB
        "--",                   // LSB
        "encoderassign1",
        "encoderassign2",
        "encoderassign3",
        "encoderassign4",
        };

    public static final int MAXIMUM_NAME_LENGTH = 8;
    public String revisePatchName(String name)
        {
        name = super.revisePatchName(name);  // trim first time
        if (name.length() > MAXIMUM_NAME_LENGTH)
            name = name.substring(0, MAXIMUM_NAME_LENGTH);
        
        StringBuffer nameb = new StringBuffer(name);                            
        for(int i = 0 ; i < nameb.length(); i++)
            {
            char c = nameb.charAt(i);
            if (c < ' ' || c >= 'z') c = ' ';
            nameb.setCharAt(i, c);
            }
        name = nameb.toString().trim();
        return super.revisePatchName(name);  // trim again
        }


    public void revise()
        {
        // check the easy stuff -- out of range parameters
        super.revise();
        
        String nm = model.get("name", "InitPgm");
        String newnm = revisePatchName(nm);
        if (!nm.equals(newnm))
            model.set("name", newnm);
        }
        



    public Object[] emitAll(String key)
        {
        if (key.equals("number")) return new Object[0];  // this is not emittable
        if (key.equals("bank")) return new Object[0];  // this should not even be possible!

        if (key.equals("name"))
            {
            char[] name = (model.get("name", "InitPgm") + "        ").toCharArray();
            Object[] obj = buildNRPNMSBOnly(getChannelOut(), 1, 'A');               // sacrificial
            Object[] objs = new Object[obj.length * 8];
            for(int i = 1; i <= 8; i++)
                {
                obj = buildNRPNMSBOnly(getChannelOut(), i, (name[i - 1] & 127));                        // we send val = MSB only, workaround for Micromonsta bug
                System.arraycopy(obj, 0, objs, (i - 1) * obj.length, obj.length);
                }
            return objs;
            }

        // Some arguments, such as LFO 2 Delay, are both NRPN *and* CC, even though we should
        // only be outputting NRPN because (in LFO 2 Delay's case) the range is larger than 128.
        // However we need to parse the CC values when they come in.  Grrrr.  So to do this we
        // FIRST check to see if the key is in our NRPN collection, and if not only THEN do we
        // emit a CC, not the other way around.
                
        Integer k = argToNRPN.get(key);
        int val = model.get(key);
        if (k != null)
            {
            if (k != null)
                {
                int param = k.intValue();
                // globalstempo, lfo1delay, lfo1rate, lfo2delay, lfo2rate, lfo3delay, lfo3rate
                if (key.equals("globalstempo") || 
                    (key.startsWith("lfo") && (key.endsWith("delay") || key.endsWith("rate"))))
                    {       // MSB+LSB
                    return buildNRPN(getChannelOut(), param, val);                          // we send val = MSB * 128 + LSB
                    }
                else
                    {
                    return buildNRPNMSBOnly(getChannelOut(), param, val);                   // we send val = MSB only, workaround for Micromonsta bug
                    }
                }
            else
                {
                System.err.println("AudiothingiesMicroMonsta.emitAll(key): invalid key " + key);
                return new Object[0];
                }
            }
        else
            {
            k = argToCC.get(key);
            int param = k.intValue();
            return buildCC(getChannelOut(), param, val);
            }
        }

    public byte[] emit(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        if (toWorkingMemory) return new byte[0];                // can't do it
        
        if (tempModel == null)
            tempModel = getModel();             // I won't use this though, as I can't extract the patch number

        byte[] data = new byte[191];
        
        data[0] = (byte)0xF0;
        data[1] = (byte)0x00;
        data[2] = (byte)0x21;
        data[3] = (byte)0x22;
        data[4] = (byte)0x4D;
        data[5] = (byte)0x4D;
        data[6] = (byte)0x03;
        data[188] = (byte)0x00;
        data[189] = (byte)0x00;
        data[190] = (byte)0xF7;
        
        // Load Name
        String name = model.get("name", "InitPgm") + "        ";
        for(int i = 0; i < 8; i++)
            {
            data[7 + i] = (byte)(name.charAt(i));
            }
        
        // Load Params
        for(int i = 0; i < SYSEX_PARAMETERS.length; i++)
            {
            if (i < SYSEX_PARAMETERS.length - 1 && SYSEX_PARAMETERS[i + 1].equals("--"))
                {
                // MSB + LSB
                int val = model.get(SYSEX_PARAMETERS[i]);
                data[15 + i] = (byte)((val >>> 7) & 127);
                data[15 + i + 1] = (byte)(val & 127);
                i++;    // skip LSB
                }
            else
                {
                // I am just 7-bit
                data[15 + i] = (byte)model.get(SYSEX_PARAMETERS[i]);
                }
            }
        
        return data;
        }

    public int parse(byte[] data, boolean fromFile)
        {
        // Load Name
        char[] name = new char[8];
        for(int i = 0; i < 8; i++)
            {
            name[i] = (char)(data[7 + i]);
            }
        model.set("name", new String(name));
        
        // Load Params
        for(int i = 0; i < SYSEX_PARAMETERS.length; i++)
            {
            if (i < SYSEX_PARAMETERS.length - 1 && SYSEX_PARAMETERS[i + 1].equals("--"))
                {
                // MSB + LSB
                model.set(SYSEX_PARAMETERS[i], (data[15 + i] << 7) | data[15 + i + 1]);
                i++;    // skip LSB
                }
            else
                {
                // I am just 7-bit
                model.set(SYSEX_PARAMETERS[i], data[15 + i]);
                }
            }
                
        revise();
        return PARSE_SUCCEEDED;      
        }


    public boolean getSendsAllParametersAsDump() { return false; }
        
    public void handleSynthCCOrNRPN(Midi.CCData data)
        {
        if (data.type == Midi.CCDATA_TYPE_NRPN)
            {
            if (data.number >= 1 && data.number <= 8)           // name
                {
                // The value is only provided as MSB
                int val = data.value / 128;
                
                char[] name = (model.get("name", "InitPgm") + "        ").toCharArray();
                name[data.number - 1] = (char)(val);
                model.set("name", new String(name).substring(0, 8));
                }
            else
                {
                String key = nrpnToArg.get(data.number);

                int val = data.value;
                // globalstempo, lfo1delay, lfo1rate, lfo2delay, lfo2rate, lfo3delay, lfo3rate
                if (!(key.equals("globalstempo") || 
                        (key.startsWith("lfo") && (key.endsWith("delay") || key.endsWith("rate")))))
                    {
                    // The value is only provided as MSB
                    val /= 128;
                    }
                
                if (key != null)
                    {
                    model.set(key, val);
                    }
                else
                    {
//                                      System.err.println("AudiothingiesMicroMonsta.handleSynthCCOrNRPN(key): invalid NRPN " + data.number);
                    }
                }
            }
        else if (data.type == Midi.CCDATA_TYPE_RAW_CC)
            {
            String key = ccToArg.get(data.number);
            if (key != null)
                {
                model.set(key, data.value);
                }
            else
                {
                // This would also happen if Edisyn is connected to itself, as it sends CC 123 etc.
//                              System.err.println("AudiothingiesMicroMonsta.handleSynthCCOrNRPN(key): invalid CC " + data.number);
                }
            }
        }
    
        

    public String getPatchName(Model model) { return model.get("name", "InitPgm"); }
        
    public Model getNextPatchLocation(Model model)
        {
        int number = model.get("number");
        
        number++;
        if (number >= 384)
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
        
        return "" + (model.get("number") + 1);
        }


        
    public String[] getPatchNumberNames()  
        { 
        return buildIntegerNames(384, 1);
        }

    public boolean getSupportsPatchWrites() { return true; }
    public boolean getSupportsDownloads() { return false; }

    public int getPatchNameLength() { return 8; }

    public boolean librarianTested() { return true; }

    int nextPatchPosition = 0;
        
    public void updateNumberAndBank(edisyn.Patch incoming)
        {
        incoming.number = nextPatchPosition;
        nextPatchPosition++;
        if (nextPatchPosition >= 384) 
            nextPatchPosition = 0;
        SwingUtilities.invokeLater(new Runnable()
            { 
            public void run() 
                { 
                download.setText("Set Librarian Download Point (Now " + nextPatchPosition +")" );
                }
            });
        }

    public int getPauseAfterWritePatch() { return 325; }                // The MicroMonsta sends them out at about 325 wait-time
    }





/*** 

     AUDIOTHINGIES MICROMONSTA SYSEX SPECIFICATION
     Sean Luke sean@cs.gmu.edu
     November 2022
     [Only for the Micromonsta, not the Micromonsta 2]

     The Micromonsta's CC and NRPN values are documented and available online here...

     https://www.audiothingies.com/dl/micromonsta/docs/old/micromonsta_midi_map_1.0.pdf
     
     [Note that there is an NRPN PARSING BUG in the Micromonsta, see the end of this spec.]
     [Also note that a few values are both CC *and* NRPN]
        
     The sysex however is not documented at all.  Below is a reverse-engineered specification
     for the sysex packet structure.  Note that the packet does not contain a checksum, nor does
     it (unfortunately) contain the patch number nor the ability to store the patch in current
     memory.  It is simply a dump of all the parameters.

     Most parameters are 7-bit and so fit in a single MIDI Byte but some are 14-bit and 
     are split between an MSB and an LSB, as noted below.  There is fortunately no bit-packing
     at all: just a straightforward layout.



     BYTE     PARAMETER

     0        0xF0
     1        0x00   (Audiothingies)
     2        0x21   (Audiothingies)
     3        0x22   (Audiothingies)
     4        0x4D
     5        0x4D
     6        0x03
     7        name0  [ASCII 32 -- ASCII 123]
     8        name1  [ASCII 32 -- ASCII 123]
     9        name2  [ASCII 32 -- ASCII 123]
     10       name3  [ASCII 32 -- ASCII 123]
     11       name4  [ASCII 32 -- ASCII 123]
     12       name5  [ASCII 32 -- ASCII 123]
     13       name6  [ASCII 32 -- ASCII 123]
     14       name7  [ASCII 32 -- ASCII 123]
     15       oscstranspose  [40 - 88 as -24 - +24]
     16       osc1type       [See Table 1]
     17       osc1finetune   [0 - 127 as -64 - +63]
     18       osc1waveshape  [0 - 127]
     19       osc1lfo1mod    [0 - 127 as -64 - +63]
     20       osc1mix        [0 - 127]
     21       osc2type       [See Table 1]
     22       osc2range      [40 - 88 as -24 - +24]
     23       osc2finetune   [0 - 127 as -64 - +63]
     24       osc2waveshape  [0 - 127]
     25       osc2env3mod    [0 - 127 as -64 - +63]
     26       osc2mix        [0 - 127]
     27       subtype        [See Table 2]
     28       suboctave      [62 - 63 as -1 - -2]
     29       submix         [0 - 127]
     30       ringmodmix             [0 - 127]
     31       noisecolor             [0 - 127 as -64 - +63]
     32       noisemix               [0 - 127]
     33       filtertype     [See Table 3]
     34       filtercutoff           [0 - 127]
     35       filterresonance        [0 - 127]
     36       filterenv2mod          [0 - 127]
     37       filterlfo2mod          [0 - 127]
     38       filtertracking         [See Table 4]
     39       vcadrive               [58 - 88 as -6 - +16]
     40       env1type               [See Table 5]
     41       env1attack             [0 - 127]
     42       env1decay              [0 - 127]
     43       env1sustain    [0 - 127]
     44       env1release    [0 - 127]
     45       env1reset              [0 - 1]         (or "keysync")
     46       env2type       [See Table 5]
     47       env2attack             [0 - 127]
     48       env2decay              [0 - 127]
     49       env2sustain    [0 - 127]
     50       env2release    [0 - 127]
     51       env2reset              [0 - 1]         (or "keysync")
     52       env3type               [See Table 5]
     53       env3attack             [0 - 127]
     54       env3decay              [0 - 127]
     55       env3sustain    [0 - 127]
     56       env3release    [0 - 127]
     57       env3reset              [0 - 1]         (or "keysync")
     58       lfo1shape              [See Table 6]
     59       lfo1speed MSB  [See Table 7]
     60       lfo1speed LSB  [See Table 7]
     61       lfo1delay MSB  [See Table 8]
     62       lfo1delay LSB  [See Table 8]
     63       lfo1phase              [See Table 9]
     64       lfo1attack             [0 - 127]
     65       lfo1reset              [0 - 1]
     66       lfo2shape              [See Table 6]
     67       lfo2speed MSB  [See Table 7]
     68       lfo2speed LSB  [See Table 7]
     69       lfo2delay MSB  [See Table 8]
     70       lfo2delay LSB  [See Table 8]
     71       lfo2phase              [See Table 9]
     72       lfo2attack             [0 - 127]
     73       lfo2reset              [0 - 1]
     74       lfo3shape              [See Table 6]
     75       lfo3speed MSB  [See Table 7]
     76       lfo3speed LSB  [See Table 7]
     77       lfo3delay MSB  [See Table 8]
     78       lfo3delay LSB  [See Table 8]
     79       lfo3phase              [See Table 9]
     80       lfo3attack             [0 - 127]
     81       lfo3reset              [0 - 1]
     82       lfostep1               [0 - 127 as -64 - +63]
     83       lfostep2               [0 - 127 as -64 - +63]
     84       lfostep3               [0 - 127 as -64 - +63]
     85       lfostep4               [0 - 127 as -64 - +63]
     86       lfostep5               [0 - 127 as -64 - +63]
     87       lfostep6               [0 - 127 as -64 - +63]
     88       lfostep7               [0 - 127 as -64 - +63]
     89       lfostep8               [0 - 127 as -64 - +63]
     90       lfosteplength  [0 - 7]
     91       matrix1source  [See Table 11]
     92       matrix1dest    [See Table 12]
     93       matrix1amount  [0 - 127 as -64 - +63]
     94       matrix2source  [See Table 11]
     95       matrix2dest    [See Table 12]
     96       matrix2amount  [0 - 127 as -64 - +63]
     97       matrix3source  [See Table 11]
     98       matrix3dest    [See Table 12]
     99       matrix3amount  [0 - 127 as -64 - +63]
     100      matrix4source  [See Table 11]
     101      matrix4dest    [See Table 12]
     102      matrix4amount  [0 - 127 as -64 - +63]
     103      matrix5source  [See Table 11]
     104      matrix5dest    [See Table 12]
     105      matrix5amount  [0 - 127 as -64 - +63]
     106      matrix6source  [See Table 11]
     107      matrix6dest    [See Table 12]
     108      matrix6amount  [0 - 127 as -64 - +63]
     109      scaler1source  [See Table 13]
     110      scaler1dest    [See Table 14]
     111      scaler1amount  [0 - 127 as -64 - +63]
     112      scaler2source  [See Table 13]
     113      scaler2dest    [See Table 14]
     114      scaler2amount  [0 - 127 as -64 - +63]
     115      scaler3source  [See Table 13]
     116      scaler3dest    [See Table 14]
     117      scaler3amount  [0 - 127 as -64 - +63]
     118      Lag Source     [See Table 10]
     119      Lag Amount     [0 - 127]
     120      FX Effect Type [See Table 16]
     121      FX Delay Time  [0 - 127]
     122      FX Delay Feedback      [0 - 127]
     123      FX LFO Speed   [0 - 127]
     124      FX LFO Depth   [0 - 127]
     125      FX Delay Balance       [0 - 127]
     126      FX Filter Cutoff       [0 - 127]
     127      FX Level ("Mix")       [0 - 127]
     128      arpeggiator on/off     [0 - 1]
     129      arp type       [See Table 17]
     130      arp gate       [0 - 127]
     131      arp octaves    [0 - 3]
     132      arp speed      [See Table 18]
     133      arp latch      [0 - 1]
     134      arp note1      [See Table 19]
     135      arp note2      [See Table 19]
     136      arp note3      [See Table 19]
     137      arp note4      [See Table 19]
     138      arp note5      [See Table 19]
     139      arp note6      [See Table 19]
     140      arp note7      [See Table 19]
     141      arp note8      [See Table 19]
     142      arp note9      [See Table 19]
     143      arp note10     [See Table 19]
     144      arp note11     [See Table 19]
     145      arp note12     [See Table 19]
     146      arp note13     [See Table 19]
     147      arp note14     [See Table 19]
     148      arp note15     [See Table 19]
     149      arp note16     [See Table 19]
     150      arp event1     [See Table 20]
     151      arp event2     [See Table 20]
     152      arp event3     [See Table 20]
     153      arp event4     [See Table 20]
     154      arp event5     [See Table 20]
     155      arp event6     [See Table 20]
     156      arp event7     [See Table 20]
     157      arp event8     [See Table 20]
     158      arp event9     [See Table 20]
     159      arp event10    [See Table 20]
     160      arp event11    [See Table 20]
     161      arp event12    [See Table 20]
     162      arp event13    [See Table 20]
     163      arp event14    [See Table 20]
     164      arp event15    [See Table 20]
     165      arp event16    [See Table 20]
     166      arp length     [0 - 15 as 1 - 16]
     167      chorder on/off [0 - 1]
     168      chorder key    [See Table 21]
     169      chorder scale  [See Table 22]
     170      chorder int1   [See Table 23]
     171      chorder int2   [See Table 23]
     172      chorder int3   [See Table 23]
     173      voice mode             [See Table 24]
     174      voiceunison    [See Table 15]
     175      voicedetune    [0 - 127]
     176      voiceoscsdetune        [0 - 127]
     177      voicepanspreadalgorithm        [See Table 10]
     178      voicepanspreadamount   [0 - 127]
     179      globalspitchbendrange  [0 - 24]
     180      globalsglidetime       [0 - 127]
     181      globalseqfiltercutoff  [0 - 127 as -64 - +63]
     182      globalstempo MSB       [20 - 255]
     183      globalstempo LSB       [20 - 255]
     184      encoder1       [See Table 25]
     185      encoder2       [See Table 25]
     186      encoder3       [See Table 25]
     187      encoder4       [See Table 25]
     188      0x00
     189      0x00
     190      0xF7



     TABLE 1: OSCILLATOR TYPES

     0 Morph Tri-Saw-Sq-Pulse
     1 Sine (+ FM)
     2 Triangle (+ Folding)
     3 Sawtooth (+ Phased)
     4 Square (+ Pulsewidth)
     5 Supersaw Balanced
     6 Supersaw Unbalanced
     7 Synced Sawtooth
     8 Synced Square
     9 PD Resonant Sawtooth
     10 PD Resonant Triangle
     11 PD Resonant Trapezoid
     12 WT 1 Saw Formant
     13 WT 2 Squ Formant
     14 WT 3 Random
     15 WT 4 Sine + Partials
     16 WT 5 Guitar Formant
     17 WT 6 Sample 1
     18 WT 7 Sample 2
     19 WT 8 Sample 3
     20 WT 9 Sample 4
     21 WT 10 Organ
     22 WT 11 Sine + Even
     23 WT 12 Sine + Odd
     24 WT 13 Sine + Even + Odd
     25 WT 14 Vowels
     26 WT 15 I Love Synthesizers
     27 WT 16 User
     28 WT 17 User
     29 WT 18 User
     30 WT 19 User
     31 WT 20 User
     32 WT 21 User
     33 WT 22 User
     34 WT 23 User
     35 WT 24 User
     36 WT 25 User
     37 WT 26 User
     38 WT 27 User
     39 WT 28 User
     40 WT 29 User
     41 WT 30 User
     42 WT No Interpolation 1 Saw Formant
     43 WT No Interpolation 2 Squ Formant
     44 WT No Interpolation 3 Random
     45 WT No Interpolation 4 Sine + Partials
     46 WT No Interpolation 5 Guitar Formant
     47 WT No Interpolation 6 Sample 1
     48 WT No Interpolation 7 Sample 2
     49 WT No Interpolation 8 Sample 3
     50 WT No Interpolation 9 Sample 4
     51 WT No Interpolation 10 Organ
     52 WT No Interpolation 11 Sine + Even
     53 WT No Interpolation 12 Sine + Odd
     54 WT No Interpolation 13 Sine + Even + Odd
     55 WT No Interpolation 14 Vowels
     56 WT No Interpolation 15 I Love Synthesizers
     57 WT No Interpolation 16 User
     58 WT No Interpolation 17 User
     59 WT No Interpolation 18 User
     60 WT No Interpolation 19 User
     61 WT No Interpolation 20 User
     62 WT No Interpolation 21 User
     63 WT No Interpolation 22 User
     64 WT No Interpolation 23 User
     65 WT No Interpolation 24 User
     66 WT No Interpolation 25 User
     67 WT No Interpolation 26 User
     68 WT No Interpolation 27 User
     69 WT No Interpolation 28 User
     70 WT No Interpolation 29 User
     71 WT No Interpolation 30 User


     TABLE 2: SUBOSCILLATOR TYPES

     0 Sine
     1 Triangle
     2 Sawtooth
     3 Square


     TABLE 3: FILTER TYPES

     0 1-Pole LP
     1 2-Pole LP
     2 3-Pole LP
     3 4-Pole LP
     4 2-Pole LP
     5 2-Pole BP
     6 Notch
     7 Phaser


     TABLE 4: FILTER KEYBOARD TRACKING

     0       0%
     1       25%
     2       50%
     3       75%
     4       100%


     TABLE 5: ENVELOPE TYPES

     0       Fast
     1       Exponential
     2       Linear


     TABLE 6: LFO SHAPES

     0       Sine
     1       Triangle
     2       Sawtooth
     3       Square
     4       Sample and Hold
     5       Smooth Random
     6       Stepped Sequence


     TABLE 7: LFO SPEEDS

     0-127   LFO speeds 0-127, then...
     128 16 Beat
     129 12 Beat
     130 8 Beat
     131 6 Beat
     132 4 Beat
     133 3 Beat
     134 2 Beat
     135 1.5 Beat 
     136 1 Beat
     137 3/4 Beat
     138 2/3 Beat
     139 1/2 Beat
     140 3/8 Beat
     141 1/3 Beat
     142 4th Note
     143 6th Note
     144 8th Note
     145 12th Note
     146 16th Note
     147 24th Note
     148 32nd Note
     149 48th Note
     150 64th Note
     151 96th Note 


     TABLE 8: LFO DELAYS

     0-127   LFO delays 0-127, then...
     128 96th Note
     129 64th Note
     130 48th Note
     131 32nd Note
     132 24th Note
     133 16th Note
     134 12th Note
     135 8th Note
     136 6th Note
     137 4th Note 
     138 1/3 Beat
     139 3/8 Beat
     140 1/2 Beat
     141 2/3 Beat
     142 3/4 Beat
     143 1 Beat
     144 1.5 Beat
     145 2 Beat
     146 3 Beat
     147 4 Beat
     148 6 Beat
     149 8 Beat
     150 12 Beat
     151 16 Beat


     TABLE 9: LFO PHASES

     0       0 Degrees
     1       45 Degrees
     2       90 Degrees
     3       135 Degrees
     4       180 Degrees
     5       225 Degrees
     6       270 Degrees
     7       315 Degrees


     TABLE 10: VOICE PAN SPREAD ALGORITHMS

     0       Linear
     1       Equal


     TABLE 10: MATRIX PATCH AND LAG SOURCES

     0 Keyboard Tracking + Bend
     1 Velocity
     2 Aftertouch
     3 Pitch Bend
     4 Mod Wheel
     5 Offset
     6 Random
     7 Env 1
     8 Env 2 
     9 Env 3
     10 LFO 1
     11 LFO 2 
     12 LFO 3


     TABLE 11: MATRIX PATCH DESTINATIONS

     0 Pitch
     1 Pitch (Fine)
     2 Osc 1 Tune
     3 Osc 1 Tune (Fine)
     4 Osc 2 Tune
     5 Osc 2 Tune (Fine)
     6 Osc 1 Waveshape
     7 Osc 2 Waveshape
     8 Noise Color
     9 Osc 1 Mix
     10 Sub Mix
     11 Ringmod Mix
     12 Noise Mix
     13 Filter Cutoff
     14 Filter Resonance
     15 Env 1 Attack Time
     16 Env 1 Decay Time
     17 Env 1 Release Time
     18 Env 2 Attack Time
     19 Env 2 Decay Time
     20 Env 2 Release Time
     21 Env 3 Attack Time
     22 Env 3 Decay Time
     23 Env 3 Release Time
     24 LFO 1 Speed
     25 LFO 2 Speed
     26 LFO 3 Speed


     TABLE 13: SCALER SOURCES

     0 Velocity
     1 Aftertouch
     2 Mod Wheel
     3 Env 1
     4 Env 2
     5 Env 3


     TABLE 14: SCALER DESTINATIONS

     0 Env 1
     1 Env 2
     3 Env 3
     4 LFO 1
     5 LFO 2
     6 LFO 3


     TABLE 15: VOICE UNISON VALUES

     0 1 Voice
     1 2 Voices
     2 4 Voices


     TABLE 16: FX EFFECT TYPES (ALGORITHMS)

     0 Stereoizer
     1 Ping-Pong Delay
     3 Filtered Delay
     4 Modulated Delay
     5 Chorus / Flanger


     TABLE 17: ARPEGGIATOR TYPES

     0 Up
     1 Triplet Up
     2 Down
     3 Triplet Down
     4 Up-Down
     5 Random
     6 As Played


     TABLE 18: ARPEGGIATOR SPEEDS

     0 1/1
     1 1/2
     2 1/3
     3 1/4
     4 1/6
     5 1/8
     6 1/12
     7 1/16
     8 1/24
     9 1/32


     TABLE 19: ARP NOTES

     0 Arp Note
     1 Low -12
     2 Note 1
     3 Note 2
     4 Note 3
     5 Note 4
     6 Note 5
     7 Note 6
     8 Note 7
     9 Note 8
     10 High +12
     11 Chord


     TABLE 20: ARP EVENTS

     0 Note
     1 Accent
     2 Slide
     3 Slide + Accent
     4 Tie ("Extend Previous")
     5 Rest


     TABLE 21: CHORDER KEYS

     0 C
     1 C#
     2 D
     3 D#
     4 E
     5 F
     6 F#
     7 G
     8 G#
     9 A
     10 A#
     11 B


     TABLE 22: CHORDER SCALES

     0 Major
     1 Minor
     2 Harmonic Minor
     3 Melodic Minor


     TABLE 23: CHORDER INTERVALS

     0 -1 x Octave
     1 -1 x 7th
     2 -1 x 6th
     3 -1 x 5th
     4 -1 x 4th
     5 -1 x 3rd
     6 -1 x 2nd
     7 Off
     8 2nd
     9 3rd
     10 4th
     11 5th
     12 6th
     13 7th
     14 +1 x Octave
     15 1 Octave + 2nd
     16 1 Octave + 3rd
     17 1 Octave + 4th
     18 1 Octave + 5th
     19 1 Octave + 6th
     20 1 Octave + 7th
     21 +2 x Octave


     TABLE 24: VOICE MODES

     0 Poly 1
     1 Poly 2
     2 Mono
     3 Legato


     TABLE 25: ENCODER ASSIGNMENTS

     0 None 
     1 Osc 1 Waveshape 
     2 Osc 1 LFO 1 Mod 
     3 Osc 2 Waveshape 
     4 Osc 2 Env 3 Mod 
     5 Filter Cutoff 
     6 Filter Resonance 
     7 Cutoff Env2 Mod 
     8 Cutoff LFO2 Mod 
     9 Env 1 Attack 
     10 Env 1 Decay 
     11 Env 1 Sustain 
     12 Env 1 Release 
     13 Env 2 Attack 
     14 Env 2 Decay 
     15 Env 2 Sustain 
     16 Env 2 Release 
     17 Env 3 Attack 
     18 Env 3 Decay 
     19 Env 3 Sustain 
     20 Env 3 Release 
     21 LFO 1 Speed 
     22 LFO 2 Speed 
     23 LFO 3 Speed 
     24 Patch 1 Amount 
     25 Patch 2 Amount 
     26 Patch 3 Amount 
     27 Patch 4 Amount 
     28 Patch 5 Amount 
     29 Patch 6 Amount 
     30 Scaler 1 Amount 
     31 Scaler 2 Amount 
     32 Scaler 3 Amount 
     33 Lag Amount 
     34 Arpeggiator 
     35 Arp Gate 
     36 Arp Octaves 
     37 Arp Speed 
     38 Arp Latch 
     39 Chorder 
     40 Chorder Key 
     41 Chorder Scale 
     42 Delay Time 
     43 Delay Feedback 
     44 FX LFO Speed 
     45 FX LFO Depth 
     46 Delay Balance 
     47 FX Filter Cutoff 
     48 FX Level 
     49 Glide Time 
     50 Patch Tempo 



     NRPN PARSING BUG

     The Micromonsta has an NRPN parsing bug you will have to work around.  The MIDI 1.0
     Specification is irritatingly vague and imprecise when it comes to NRPN, but there
     are two items that the MicroMonsta deviates from.  The first deviation is just a 
     strange oddity, but the second deviation is a serious bug you need to be aware of.

     In NRPN, you send data packets as MSB (CC 6) and LSB (CC 38).  It is not made 
     clear what the interpretation of these two packets should be: is the data to be 
     interpreted as the integer MSB * 128 + LSB?  Or is it to be interpreted essentially 
     as a "fine tuned" floating-point number MSB + LSB/128.0 ?  This is relevant because
     it changes the integer meaning of *only* sending an MSB or only an LSB.  Some 
     manufacturers (like DSI/Sequential) use the first interpretation -- which I prefer
     as well -- while the spec hints at the second interpretation.

     The MicroMonsta uses *both* interpretations.  If the range of the parameter is less
     than 128, then it expects and sends *just the MSB* to hold the parameter.  If the 
     range of the parameter is >= 128, then it expects and sends *both the LSB and MSB* 
     and the number is now interpreted as MSB * 128 + LSB.  So you'll have to handle the 
     parsing of the parameter based on its range.

     Second -- the bug -- the NRPN spec allows you to send the MSB and LSB in either order
     and to emit either one of them (it should be then intepreted initially as zero, and
     thereafter as its previous value until the parameter number is resent, at which time
     it should be interpreted as zero again).

     However the MicroMonsta's parsing varies depending on the order in which you send 
     the LSB and MSB.  

     - If you omit the LSB, the MicroMonsta will assume the parameter value is the MSB.

     - If you send the LSB *before* the MSB, it will be ignored regardless of its value
     and the MicroMonsta will again assume the parameter value is the MSB.

     - If you send the LSB *after* the MSB, the MicroMonsta will assume that the
     parameter value is MSB * 128 + LSB.

     - The MicroMonsta will emit MSB, then the LSB for ranges >= 128 and will emit MSB 
     only for ranges < 128.

     You'll be fine if you do the following:

     - For ranges < 128, send the value as a single MSB.

     - For ranges >= 128, send the value as MSB = val % 128 followed by LSB = val / 128.

     - If you receive an MSB, wait for the LSB only if the range is >= 128.  

     - For ranges >= 128, interpret the received value as MSB * 128 + LSB

     - For ranges < 128, interpret the received value as MSB.

     - For CC in all cases, interpret the value as the CC

*/
