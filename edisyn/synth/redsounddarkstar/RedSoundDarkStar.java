/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.redsounddarkstar;

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
   A patch editor for the Red Sound Darkstar.
        
   @author Sean Luke
*/

public class RedSoundDarkStar extends Synth
    {
    public static final String[] LFO_WAVES = { "Ramp", "Triangle", "Square", "Sine", "Pulse", "Sample & Hold", "Random" };
    public static final String[] MIDI_SYNC = { "Off", "1/8 beat", "1/6 beat", "1/4 beat", "1/3 beat", "1/2 beat", "2/3 beat", "3/4 beat", "1 beat", "2 beats", "3 beats", "1 bar", "1.5 bars", "2 bars", "3 bars", "4 bars" };
    public static final String[] MOD_SOURCES = { "Envelope 1", "Envelope 2", "LFO 1", "LFO 2" };
    public static final String[] PITCH_OFFSETS = { "None", "Fourth", "Fifth", "Octave", "Octave + Maj 3rd", "Octave + Fourth", "Octave + Fifth", "2 Octaves" };
    public static final String[] OSCILLATOR_SOURCES = { "Normal", "Formant", "White Noise", "Pink Noise", "Blue Noise", "Eternal 1", "External 2" };
    public static final String[] KEY_TRACKING = { "Off", "25%", "50%", "100%", "150%", "-25%", "-50%" };
    public static final String[] FILTER_TYPES = { "Low Pass", "Band Pass", "High Pass" };
    public static final String[] TREMOLO_SOURCES = { "LFO 1", "LFO 2" };
    public static final String[] PAN_MOD_SOURCES = { "Pot", "Envelope 1", "Envelope 2", "LFO 1", "LFO 2" };
    public static final String[] PORTAMENTO_TYPES = { "Off", "Type 1", "Type 2", "Pre-glide 1", "Pre-glide 2", "Pre-glide 3", "Pre-glide 4", "Pre-glide 5", "Pre-glide 6" };
    public static final String[] JOYSTICK_ASSIGNMENTS = { "Normal", "Filter", "Mix [XP2]", "Envelope 1 [XP2]", "Envelope 2 [XP2]", "LFO [XP2]" };
    public static final String[] OUTPUT_ASSIGNMENTS = { "Main Outputs", "Aux Outputs" };
    public static final String[] AUDITION_TYPES = { "Arp 3", "Note 1", "Note 2", "Note 3", "Bass 1", "Bass 2", "Bass 3", "Bass Drum", "Chord 1", "Chord 2", "Snare", "Arp 1", "Arp 2" };

    boolean xp2;
    public static final String XP2_KEY = "XP2";
    
    public boolean isXP2() { return xp2; }
    public void setXP2(boolean val)
        {
        setLastX("" + (!val), XP2_KEY, getSynthName(), true);
        xp2 = val;
        updateTitle();
        }
    

    public JFrame sprout()
        {
        JFrame frame = super.sprout();
        getAll.setEnabled(false);
        merge.setEnabled(false);
        writeTo.setEnabled(false);
        receiveCurrent.setEnabled(false);
        receivePatch.setEnabled(false);
        transmitParameters.setEnabled(false);
        transmitParameters.setSelected(false);
        return frame;
        }         

    public RedSoundDarkStar()
        {
        model.set("number", 0);
                
        String m = getLastX(XP2_KEY, getSynthName());
        xp2 = (m == null ? false : !Boolean.parseBoolean(m));

        VBox vbox = new VBox();
        HBox hbox = new HBox();

        for(int i = 1; i <= 5; i++)
            {
            JComponent sourcePanel = new SynthPanel(this);
            vbox = new VBox();
                
            hbox = new HBox();
            if (i == 1)
                {
                hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
                }
                                
            hbox.addLast(addGeneral(i, Style.COLOR_A()));
            vbox.add(hbox);
                        
            hbox = new HBox();
            hbox.add(addOscillatorsGeneral(i, Style.COLOR_B()));
            hbox.addLast(addOscillator(i, 1, Style.COLOR_B()));
            vbox.add(hbox);
            hbox = new HBox();
            hbox.add(addLFO(i, 1, Style.COLOR_A()));
            hbox.add(addOscillator(i, 2, Style.COLOR_B()));
            vbox.add(hbox);
            hbox = new HBox();
            hbox.add(addLFO(i, 2, Style.COLOR_A()));
            hbox.addLast(addFilter(i, Style.COLOR_C()));
            vbox.add(hbox);
                        
            hbox = new HBox();
            hbox.add(addEnvelope(i, 1, Style.COLOR_A()));
            hbox.addLast(addEnvelope(i, 2, Style.COLOR_A()));
            vbox.add(hbox);
            vbox.add(addModulation(1, Style.COLOR_A()));
                        
            sourcePanel.add(vbox, BorderLayout.CENTER);
            if (i == 1)
                {
                addTab("Global and Part 1", sourcePanel);
                }
            else
                {
                addTab("Part " + i, sourcePanel);
                }
            }

        model.set("name", "INIT VOICE");
        
        loadDefaults();        
        }
                
    //public String getDefaultResourceFileName() { return "YamahaDX7.init"; }
    //public String getHTMLResourceFileName() { return "YamahaDX7.html"; }

    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        JTextField number = new JTextField("" + (model.get("number") + 1), 3);

        while(true)
            {
            boolean result = showMultiOption(this, new String[] { "Patch Number"}, 
                new JComponent[] { number }, title, "Enter the Patch number");
                
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
        globalCategory.makeUnresettable();
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
                
        VBox vbox = new VBox();
        comp = new PatchDisplay(this, 2);
        vbox.add(comp);

        HBox hbox2 = new HBox();
        JCheckBox check = new JCheckBox("XP2");
        check.setSelected(xp2);
        check.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                setXP2(check.isSelected());
                }
            });
        check.setFont(Style.SMALL_FONT());
        check.setOpaque(false);
        check.setForeground(Style.TEXT_COLOR());
        hbox2.add(check);
        hbox2.add(Stretch.makeHorizontalStretch());
        vbox.add(Stretch.makeVerticalStretch());
        vbox.add(hbox2);
        hbox.add(vbox);

        comp = new LabelledDial("Current", this, "currenteditpart", color, 0, 4);
        ((LabelledDial)comp).addAdditionalLabel("Edit Part");
        hbox.add(comp);
        
        comp = new LabelledDial("Chorus", this, "chorusdepth", color, 0, 15);
        ((LabelledDial)comp).addAdditionalLabel("Depth [XP2]");
        hbox.add(comp);
        
        comp = new LabelledDial("Chorus", this, "chorusrate", color, 0, 15);
        ((LabelledDial)comp).addAdditionalLabel("Rate [XP2]");
        hbox.add(comp);
        
        
        // Not enough space to show the title
//        hbox.addLast(Strut.makeHorizontalStrut(150));

        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }

    public JComponent addOscillatorsGeneral(int part, Color color)
        {
        Category category = new Category(this, "Oscillators", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = TREMOLO_SOURCES;
        comp = new Chooser("Tremolo Mod Source", this, "part" + part + "tremelomodsource", params);
        vbox.add(comp);

        params = PAN_MOD_SOURCES;
        comp = new Chooser("Pan Mod Source", this, "part" + part + "panmodsource", params);
        vbox.add(comp);
        
        hbox.add(vbox);

        comp = new LabelledDial("Volume", this, "part" + part + "volume", color, 0, 127);
        hbox.add(comp);
        
        comp = new LabelledDial("Pan", this, "part" + part + "pan", color, 0, 127);
        hbox.add(comp);
        
        comp = new LabelledDial("Tremolo", this, "part" + part + "tremolo", color, 0, 127);
        hbox.add(comp);
        
        comp = new LabelledDial("Mix", this, "part" + part + "mix", color, 0, 127)
            {
            public boolean isSymmetric() { return true; }
            public int getDefaultValue() { return 64; }

            public String map(int val)
                {
                if (val == 64) return "--";
                else if (val < 64) return "< " + (64 - val);
                else return "" + (val - 64) + " >";
                }
            };
        hbox.add(comp);
        
        comp = new LabelledDial("Ring", this, "part" + part + "ring", color, 0, 127);
        hbox.add(comp);
        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }




    public JComponent addGeneral(int part, Color color)
        {
        Category category = new Category(this, "General", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = PORTAMENTO_TYPES;
        comp = new Chooser("Portamento Type", this, "part" + part + "portamentotype", params);
        vbox.add(comp);

        comp = new CheckBox("Sustain", this, "part" + part + "sustain");
        vbox.add(comp);

        comp = new CheckBox("Portamento Auto Glide", this, "part" + part + "autoglide");
        vbox.add(comp);

        hbox.add(vbox);

        comp = new LabelledDial("Bend Range", this, "part" + part + "bendrange", color, 0, 12);
        hbox.add(comp);
        
        comp = new LabelledDial("Channel", this, "part" + part + "channel", color, 0, 15, -1);
        hbox.add(comp);
        
        comp = new LabelledDial("Polyphony", this, "part" + part + "polyphony", color, 0, 8);
        hbox.add(comp);
        
        comp = new LabelledDial("Note Lo", this, "part" + part + "notelo", color, 0, 127);
        hbox.add(comp);
        
        comp = new LabelledDial("Note Hi", this, "part" + part + "notehi", color, 0, 127);
        hbox.add(comp);
        
        comp = new LabelledDial("Transpose", this, "part" + part + "transpose", color, 0, 48)
            {
            public String map(int val)
                {
                return "" + (val - 24);
                }
            };
        hbox.add(comp);
        
        comp = new LabelledDial("Portamento", this, "part" + part + "portamentotime", color, 0, 63);
        ((LabelledDial)comp).addAdditionalLabel("Time");
        hbox.add(comp);

        comp = new LabelledDial("FX Send", this, "fxsend", color, 0, 15);
        ((LabelledDial)comp).addAdditionalLabel("[XP2]");
        hbox.add(comp);
        
        
        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }




    public JComponent addModulation(int part, Color color)
        {
        Category category = new Category(this, "Modulation", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = JOYSTICK_ASSIGNMENTS;
        comp = new Chooser("Joystick Assign", this, "part" + part + "joystickassign", params);
        vbox.add(comp);

        params = OUTPUT_ASSIGNMENTS;
        comp = new Chooser("Output Assign [XP2]", this, "part" + part + "outputassign", params);
        vbox.add(comp);
        hbox.add(vbox);
        
        vbox = new VBox();

        params = AUDITION_TYPES;
        comp = new Chooser("Audition Type [XP2]", this, "part" + part + "auditiontype", params);
        vbox.add(comp);

        comp = new CheckBox("Part Output Shift", this, "part" + part + "partoutputshift");
        vbox.add(comp);

        comp = new CheckBox("Filter Shift", this, "part" + part + "filtershift");
        vbox.add(comp);
        hbox.add(vbox);
                
        vbox = new VBox();
        comp = new CheckBox("Env Shift", this, "part" + part + "envshift");
        vbox.add(comp);

        comp = new CheckBox("Env 1/2 Shift", this, "part" + part + "envhalfshift");
        ((CheckBox)comp).addToWidth(2);
        vbox.add(comp);

        comp = new CheckBox("Osc Shift", this, "part" + part + "oscshift");
        vbox.add(comp);

        hbox.add(vbox);
                
        vbox = new VBox();
        comp = new CheckBox("Osc 1/2 Shift", this, "part" + part + "oschalfshift");
        ((CheckBox)comp).addToWidth(2);
        vbox.add(comp);

        comp = new CheckBox("LFO Shift", this, "part" + part + "lfoshift");
        vbox.add(comp);

        comp = new CheckBox("LFO 1/2 Shift", this, "part" + part + "lfohalfshift");
        ((CheckBox)comp).addToWidth(2);
        vbox.add(comp);


        hbox.add(vbox);
                
        comp = new LabelledDial("Aftertouch", this, "part" + part + "aftertouchpitch", color, 0, 14, -7);
        ((LabelledDial)comp).addAdditionalLabel("Pitch");
        hbox.add(comp);
        
        comp = new LabelledDial("Aftertouch", this, "part" + part + "aftertouchfilter", color, 0, 14, -7);
        ((LabelledDial)comp).addAdditionalLabel("Filter");
        hbox.add(comp);
        
        comp = new LabelledDial("Mod Wheel", this, "part" + part + "modwheelpitch", color, 0, 14, -7);
        ((LabelledDial)comp).addAdditionalLabel("Pitch");
        hbox.add(comp);
        
        comp = new LabelledDial("Mod Wheel", this, "part" + part + "modwheelfilter", color, 0, 14, -7);
        ((LabelledDial)comp).addAdditionalLabel("Filter");
        hbox.add(comp);
        
        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }



    public JComponent addLFO(int part, int lfo, Color color)
        {
        Category category = new Category(this, "LFO " + lfo, color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = LFO_WAVES;
        comp = new Chooser("Shape", this, "part" + part + "lfo" + lfo + "shape", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Speed", this, "part" + part + "lfo" + lfo + "speed", color, 0, 127);
        hbox.add(comp);
        
        comp = new LabelledDial("Delay", this, "part" + part + "lfo" + lfo + "delay", color, 0, 127);
        hbox.add(comp);
        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
    

    public JComponent addEnvelope(int part, int env, Color color)
        {
        Category category = new Category(this, "Envelope " + env, color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
               

        comp = new LabelledDial("Attack", this, "part" + part +  "env" + env + "attack", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Decay", this, "part" + part +  "env" + env + "decay", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Sustain", this, "part" + part +  "env" + env + "sustain", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Release", this, "part" + part +  "env" + env + "release", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Velocity", this, "part" + part +  "env" + env + "velocity", color, 0, 7);
        hbox.add(comp);


        // ADSR
        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null,   "part" + part + "env" + env + "attack",   "part" + part + "env" + env + "decay",  null,   "part" + part + "env" + env + "release" },
            new String[] {   null,  null,   "part" + part + "env" + env + "sustain",    "part" + part + "env" + env + "sustain",  null },
            new double[] { 0, 0.25/127.0, 0.25 / 127.0,  0.25, 0.25/127.0},
            new double[] { 0, 1.0, 1.0 / 127.0, 1.0/127.0, 0 });
        hbox.addLast(comp);
                
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
        




    public JComponent addOscillator(int part, int osc, Color color)
        {
        final Category category = new Category(this, "Oscillator " + osc, color);

        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = MOD_SOURCES;
        comp = new Chooser("Pitch Mod", this, "part" + part + "osc" + osc + "pitchmodsource", params);
        vbox.add(comp);

        params = MOD_SOURCES;
        comp = new Chooser("Pulse Width Mod", this, "part" + part + "osc" + osc + "pulsewidthmodsource", params);
        vbox.add(comp);
        hbox.add(vbox);

        if (osc == 2)
            {
            vbox = new VBox();
            params = OSCILLATOR_SOURCES;
            comp = new Chooser("Source", this, "part" + part + "osc" + osc + "source", params);
            vbox.add(comp);

            params = PITCH_OFFSETS;
            comp = new Chooser("Pitch Offset", this, "part" + part + "osc" + osc + "pitchoffset", params);
            vbox.add(comp);

            hbox.add(vbox);
            }

        comp = new LabelledDial("Pitch Mod", this, "part" + part + "osc" + osc + "pulsewidthmod", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Detune", this, "part" + part + "osc" + osc + "detune", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Pulse Width", this, "part" + part + "osc" + osc + "pulsewidth", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Pulse Width", this, "part" + part + "osc" + osc + "pulsewidthmod", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Mod");
        hbox.add(comp);

        comp = new LabelledDial("Waveform", this, "part" + part + "osc" + osc + "waveform", color, 0, 127);
        hbox.add(comp);

        if (osc == 2)
            {
            vbox = new VBox();
            comp = new CheckBox("Sync", this, "part" + part + "osc" + osc + "sync");
            ((CheckBox)comp).addToWidth(2);
            vbox.add(comp);

            hbox.add(vbox);
            }

        comp = new LabelledDial("Pitch Mod", this, "part" + part + "osc" + osc + "pulsewidthmod", color, 0, 127);
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
        
    public JComponent addFilter(int part, Color color)
        {
        final Category category = new Category(this, "Filter", color);

        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();
        
        VBox vbox = new VBox();

        params = FILTER_TYPES;
        comp = new Chooser("Type", this, "part" + part + "filtertype", params);
        vbox.add(comp);

        comp = new CheckBox("On", this, "part" + part + "filteron");
        vbox.add(comp);
        hbox.add(vbox);
        vbox = new VBox();

        params = KEY_TRACKING;
        comp = new Chooser("Key Tracking", this, "part" + part + "filterkeytracking", params);
        vbox.add(comp);

        params = MOD_SOURCES;
        comp = new Chooser("Modulation Source", this, "part" + part + "filtermodsource", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Envelope Mod", this, "part" + part + "filterenvmod", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("LFO Mod", this, "part" + part + "filterlfomod", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Resonance", this, "part" + part + "filterresonance", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Resonance Mod", this, "part" + part + "filterresonancemode", color, 0, 127);
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }                
 
/*
  public int parse(byte[] data, boolean fromFile)
  {
  if (data[3] == 0)  // 1 single
  {
  // yay for DX7 simplicity
  for(int i = 0; i < allParameters.length - 10; i++)
  {
  model.set(allParameters[i], data[i + 6]);
  }
                
  char[] name = new char[10];
  for(int i = 0; i < 10; i ++)
  {
  name[i] = (char)(data[allParameters.length - 10 + i + 6] & 127);
  }
  model.set("name", new String(name));
                
  revise();
  return PARSE_SUCCEEDED;
  }
  else                        // bulk
  {
  // extract names
  char[][] names = new char[32][10];
  for(int i = 0; i < 32; i++)
  {
  for (int j = 0; j < 10; j++)
  {
  names[i][j] = (char)(data[6 + (i * 128) + 118 + j] & 127);
  }
  }
                        
  String[] n = new String[32];
  for(int i = 0; i < 32; i++)
  {
  n[i] = "" + (i + 1) + "   " + new String(names[i]);
  }
            
  // Now that we have an array of names, one per patch, we present the user with options;
  // 0. Cancel [handled automatically]
  // 1. Save the bank data [handled automatically]
  // 2. Upload the bank data [handled automatically] 
  // 3. Load and edit a certain patch number
  int patchNum = showBankSysexOptions(data, n);
  if (patchNum < 0) return PARSE_CANCELLED;
                
  // okay, we're loading and editing patch number patchNum.  Here we go.
  int patch = patchNum * 128;
  int pos = 0;
                                                        
  for(int op = 0; op < 6; op++)
  {
  // operatorNrate1 ... operatorNkeyboardlevelscalingrightdepth
  for(int i = 0; i < 11; i++)
  {
  model.set(allParameters[pos++], data[patch + op * 17 + i + 6]);
  }
                                        
  // scaling left curve
  model.set(allParameters[pos++], data[patch + op * 17 + 11 + 6] & 3);
  // scaling right curve
  model.set(allParameters[pos++], (data[patch + op * 17 + 11 + 6] >>> 2) & 3);
                                
  // rate scaling
  model.set(allParameters[pos++], data[patch + op * 17 + 12 + 6] & 7);
                                
  // amp mod sensitivity
  model.set(allParameters[pos++], data[patch + op * 17 + 13 + 6] & 3);
  // key velocity
  model.set(allParameters[pos++], (data[patch + op * 17 + 13 + 6] >>> 2) & 7);

  // output level
  model.set(allParameters[pos++], data[patch + op * 17 + 14 + 6]);
                                        
  // osc mode
  model.set(allParameters[pos++], data[patch + op * 17 + 15 + 6] & 1);
  // freq coarse
  model.set(allParameters[pos++], (data[patch + op * 17 + 15 + 6] >>> 1) & 31);
  // freq fine
  model.set(allParameters[pos++], data[patch + op * 17 + 16 + 6]);
  // detune  [note this one is out of position, why Yamaha why?]
  model.set(allParameters[pos++], (data[patch + op * 17 + 12 + 6] >>> 3) & 15);
  }
                        
  // pitchegrate1 ... pitcheglevel4
  for(int i = 102; i < 110; i++)
  {
  model.set(allParameters[pos++], data[patch + i + 6]);
  }
                                
  // algorithm select
  model.set(allParameters[pos++], data[patch + 110 + 6] & 31);
  // feedback
  model.set(allParameters[pos++], data[patch + 111 + 6] & 7);
  // osc key sync
  model.set(allParameters[pos++], (data[patch + 111 + 6] >>> 3) & 1);

  // lfospeed ... lfoamplitudemodulationdepth
  for(int i = 112; i < 116; i++)
  {
  model.set(allParameters[pos++], data[patch + i + 6]);
  }
                                
  // key sync
  model.set(allParameters[pos++], data[patch + 116 + 6] & 1);
  // wave
  model.set(allParameters[pos++], (data[patch + 116 + 6] >>> 1) & 7);
  // lfo pitch mod sens
  model.set(allParameters[pos++], (data[patch + 116 + 6] >>> 4) & 7);
  // transpose
  model.set(allParameters[pos++], (data[patch + 117 + 6]) & 63);
                                
  model.set("name", new String(names[patchNum]));
                
  model.set("number", patchNum);

  revise();
  return PARSE_SUCCEEDED_UNTITLED;
  }
  }
 
    
  public byte[] emit(Model tempModel, boolean toWorkingMemory, boolean toFile)
  {
  simplePause(50);                // dunno if I need it, it was needed for the TX81Z
        
  byte[] data = new byte[163];
  data[0] = (byte)0xF0;
  data[1] = (byte)0x43;
  data[2] = (byte)(getChannelOut());
  data[3] = (byte)0x00;
  data[4] = (byte)0x01;
  data[5] = (byte)0x1B;
                                
  // yay for DX7 simplicity
  for(int i = 0; i < allParameters.length - 10; i++)
  {
  data[i + 6] = (byte)(model.get(allParameters[i], 0));
  }

  for(int i = 0; i < 10; i ++)
  {
  data[allParameters.length - 10 + i + 6] = (byte)((model.get("name", "          ") + "          ").charAt(i));
  }
                
  data[161] = produceChecksum(data, 6);
  data[162] = (byte)0xF7;
        
  return data;
  }
*/

    public static boolean recognize(byte[] data)
        {
        // DarkStar data comes in the following forms
        // VOICE DATA is 100 bytes
        // Others are 1 byte each
        
        // DarkStar or XP2 Single Voice: 108 bytes
        // F0 00 20 3B 02 01 03 VOICEDATA f7
        
        // DarkStar Single Performance: 512 bytes
        // F0 00 20 3B 02 01 01 VOICEDATA(x5) 00 EDITPART 00 00 f7
        
        // XP2 Single Performance: 516 bytes
        // F0 00 20 3B 02 01 01 VOICEDATA(x5) FX1 FX2 FX3 FX4 FX5 CHORUS DEPTH EDITPART f7
        
        // DarkStar Bulk Dump Single Performance: 514 bytes
        // F0 00 20 3B 02 01 02 PERFNUM(x2) VOICEDATA(x5) 00 EDITPART 00 00 f7
        
        // XP2 Bulk Dump Single Performance: 518 bytes
        // F0 00 20 3B 02 01 02 PERFNUM(x2) VOICEDATA(x5) FX1 FX2 FX3 FX4 FX5 CHORUS DEPTH EDITPART f7

        // We're gonna try to recognize all of them
                
        return (
            data[0] == 0xF0 &&
            data[1] == 0x00 &&
            data[2] == 0x20 &&
            data[3] == 0x3B &&
            data[4] == 0x02 &&
            data[5] == 0x01 &&
                (data.length == 108 ||
                data.length == 512 ||
                data.length == 516 ||
                data.length == 514 ||
                data.length == 518));        
        }

    public static String getSynthName() { return "Red Sound DarkStar"; }

    public String getPatchLocationName(Model model)
        {
        int num = model.get("number", 0) + 1;
        if (num < 10) return "0" + num;
        else return "" + num;
        }
    
    public void changePatch(Model tempModel) 
        {
        tryToSendMIDI(buildPC(getChannelOut(), tempModel.get("number")));

        // we assume that we successfully did it
        if (!isMerging())  // we're actually loading the patch, not merging with it
            {
            setSendMIDI(false);
            model.set("number", tempModel.get("number"));
            setSendMIDI(true);
            }
        }
    }
