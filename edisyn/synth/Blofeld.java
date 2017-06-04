/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth;

import edisyn.*;
import edisyn.gui.*;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.border.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;

/**
   A patch editor for the Waldorf Blofeld.  Does not deal with Multi mode, global parameters,
   modifying wavetables, or uploading samples.  Only Single mode patches.
        
   @author Sean Luke
*/

public class Blofeld extends Synth
    {
    // Tab pane for editor
    JTabbedPane tabs;
    // The sound (oscillator/filter) tab
    JComponent soundPanel;
    // The lfo/envelope tab
    JComponent lfoEnvelopePanel;
    // The modulation/modifier/effects tab
    JComponent modulationPanel;
    // The arpeggiator tab
    JComponent arpeggiationPanel;
        
        
    /// Various collections of parameter names for pop-up menus
        
    static final String[] FM_SOURCES = new String[] { "Off", "Osc1", "Osc2", "Osc3", "Noise", "LFO 1", "LFO 2", "LFO 3", "Filter Env", "Amp Env", "Env 3", "Env 4" };
    static final String[] MOD_SOURCES = new String[] {"Off", "LFO 1", "LFO1*MW", "LFO 2", "LFO2*Press.", "LFO 3", "Filter Env", "Amp Env", "Env 3", "Env 4", "Keytrack", "Velocity", "Rel. Velo", "Pressure", "Poly Press", "Pitch Bend", "Mod Wheel", "Sustain", "Foot Ctrl", "Breath Ctrl", "Control W", "Control X", "Control Y", "Control Z", "Unisono V.", "Modifier 1", "Modifier 2", "Modifier 3", "Modifier 4", "Minimum", "Maximum" };
    static final String[] MOD_DESTINATIONS = new String[] { "Pitch", "O1 Pitch", "O1 FM", "O1 PW/Wave", "O2 Pitch", "O2 FM", "O2 PW/Wave", "O3 Pitch", "O3 FM", "O3 PW", "O1 Level", "O1 Balance", "O2 Level", "O2 Balance", "O3 Level", "O3 Balance", "RMod Level", "RMod Bal.", "Noise Level", "Noise Bal.", "F1 Cutoff", "F1 Reson.", "F1 FM", "F1 Drive", "F1 Pan", "F2 Cutoff", "F2 Reson.", "F2 FM", "F2 Drive", "F2 Pan", "Volume", "LFO1 Speed", "LFO2 Speed", "LFO3 Speed", "FE Attack", "FE Decay", "FE Sustain", "FE Release", "AE Attack", "AE Decay", "AE Sustain", "AE Release", "E3 Attack", "E3 Decay", "E3 Sustain", "E3 Release", "E4 Attack", "E4 Decay", "E4 Sustain", "E4 Release", "M1 Amount", "M2 Amount", "M3 Amount", "M4 Amount" };
    static final String[] DRIVE_CURVES = new String[] {    "Clipping", "Tube", "Hard", "Medium", "Soft", "Pickup 1", "Pickup 2", "Rectifier", "Square", "Binary", "Overflow", "Sine Shaper", "Osc 1 Mod" };
    static final String[] ARPEGGIATOR_SPEEDS = new String[] { "1/96", "1/48", "1/32", "1/16 T", "1/32 .", "1/16", "1/8T", "1/16 .", "1/8", "1/4 T", "1/8 .", "1/4", "1/2 T", "1/4 .", "1/2", "1/1 T", "1/2 .", "1", "1.5", "2", "2.5", "3", "3.5", "4", "5", "6", "7", "8", "9", "10", "12", "14", "16", "18", "20", "24", "28", "32", "36", "40", "48", "56", "64" };
    static final String[] ARPEGGIATOR_LENGTHS = new String[] { "1/96", "1/48", "1/32", "1/16 T", "1/32 .", "1/16", "1/8T", "1/16 .", "1/8", "1/4 T", "1/8 .", "1/4", "1/2 T", "1/4 .", "1/2", "1/1 T", "1/2 .", "1", "1.5", "2", "2.5", "3", "3.5", "4", "5", "6", "7", "8", "9", "10", "12", "14", "16", "18", "20", "24", "28", "32", "36", "40", "48", "56", "64", "Legato" };
    static final String[] ARPEGGIATOR_MODES = new String[] { "Off", "On", "One Shot", "Hold" };
    static final String[] ARPEGGIATOR_DIRECTIONS = new String[] { "Up", "Down", "Alt Up", "Alt Down" };
    static final String[] ARPEGGIATOR_SORT_ORDERS =  new String[] { "As Played", "Reversed", "Key Lo>Hi", "Key Hi>Lo", "Vel Lo>Hi", "Vel Hi>Lo" };
    static final String[] ARPEGGIATOR_VELOCITY_MODES =  new String[] { "Each Note", "First Note", "Last Note", "Fix 32", "Fix 64", "Fix 100", "Fix 127" };
    static final String[] ARPEGGIATOR_PATTERN_STEPS = new String[] { "Normal", "Pause", "Previous", "First", "Last", "First + Last", "Chord", "Random" };
    static final String[] EFFECTS_SHORT = new String[] { "Bypass", "Chorus", "Flanger", "Phaser", "Overdrive", "Triple FX" };
    static final String[] EFFECTS_LONG = new String[] { "Bypass", "Chorus", "Flanger", "Phaser", "Overdrive", "Triple FX", "Delay", "Clk. Delay", "Reverb" };
    static final String[] MODIFIER_OPERATORS = new String[] { "+", "-", "*", "AND", "OR", "XOR", "MAX", "MIN" };
    static final String[] ENVELOPE_TYPES = new String[] { "ADSR", "ADS1DS2R", "One Shot", "Loop S1S2", "Loop All" };
    static final String[] LFO_SHAPES = new String[] { "Sine", "Triangle", "Square", "Saw", "Random", "S&H" };
    static final String[] LFO_SPEEDS = new String[] {      "1280", "1152", "1024", "896", "768", "640", "576", "512", 
                                                           "448", "384", "320", "288", "256", "224", "192",  "160", 
                                                           "144", "128", "112", "96", "80", "72", "64", "56", 
                                                           "48", "40", "36", "32", "28", "24", "20", "18", 
                                                           "16", "14", "12", "10", "9", "8", "7", "6", 
                                                           "5", "4", "3.5", "3", "2.5", "2", "1.5", "1", 
                                                           "1/2 .", "1/1 T", "1/2", "1/4 .", "1/2 T", "1/4", "1/8 .", "1/4 T", 
                                                           "1/8", "1/16 .", "1/8 T", "1/16", "1/32 .", "1/16 T", "1/32", "1/48" };
    static final String[] CATEGORIES = new String[] { "Init", "Arp", "Atmo", "Bass", "Drum", "FX", "Keys", "Lead", "Mono", "Pad", "Perc", "Poly", "Seq" };
    static final String[] BANKS = new String[] { "A", "B", "C", "D", "E", "F", "G", "H" };
    static final String[] OSCILLATOR_GLIDE_MODES = new String[] { "Portamento", "Fingered P", "Glissando", "Fingered G" };
    static final String[] FILTER_TYPES = new String[] {    "Bypass", "LP 24dB", "LP 12dB", "BP 24dB", "BP 12dB", "HP 24dB", "HP 12dB", "Notch 24dB", "Notch 12dB", "Comb+", "Comb-", "PPG LP" };   
    // Note that there is space after "Off".  This is to make sure that WAVES_LONG
    // Produces a popup menu the same width as WAVES_SHORT
    static final String[] WAVES_LONG = new String[] {      "Off               ", "Pulse", "Saw", "Triangle", "Sine", "Alt 1", "Alt 2", "Resonant", "Resonant2", "MalletSyn", 
                                                           "Sqr-Sweep", "Bellish", "Pul-Sweep", "Saw-Sweep", "MellowSaw", "Feedback", "Add Harm", "Reso 3 HP", 
                                                           "Wind Syn", "HighHarm", "Clipper", "OrganSyn", "SquareSaw", "Format1", "Polated", "Transient", 
                                                           "ElectricP", "Robotic", "StrongHrm", "PercOrgan", "ClipSweep", "ResoHarms", "2 Echoes", "Formant2", 
                                                           "FmntVocal", "MicroSync", "MicroPWM", "Glassy", "SquareHP", "SawSync1", "SawSync2", "SawSync3", 
                                                           "PulSync1", "PulSync2", "PulSync3", "SinSync1", "SinSync2", "SinSync3", "PWM Pulse", "PWM Saw", 
                                                           "Fuzz Wave", "Distorted", "HeavyFuzz", "Fuzz Sync", "K+Strong1", "K+Strong2", "K+Strong3", "1-2-3-4-5", 
                                                           "19/twenty", "Wavetrip1", "Wavetrip2", "Wavetrip3", "Wavetrip4", "MaleVoice", "Low Piano", "ResoSweep", 
                                                           "Xmas Bell", "FM Piano", "Fat Organ", "Vibes", "Chorus 2", "True PWM", "UpperWaves", };
    // Note that there is space after "Off".  This is to make sure that WAVES_SHORT
    // Produces a popup menu the same width as WAVES_LONG
    static final String[] WAVES_SHORT = new String[] { "Off               ", "Pulse", "Saw", "Triangle", "Sine" };
    static final String[] OSCILLATOR_OCTAVES = new String[] { "128'", "64'", "32'", "16'", "8'", "4'", "2'", "1'", "1/2'" };
        
        
    public Blofeld()
        {
        for(int i = 0; i < allParameters.length; i++)
            {
            allParametersToIndex.put(allParameters[i], Integer.valueOf(i));
            }
                
        setLayout(new BorderLayout());
                
        tabs = new JTabbedPane();
        tabs.setBackground(Style.BACKGROUND_COLOR);
                
        /// SOUND PANEL
                
        soundPanel = new SynthPanel();
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL));
        hbox.addLast(addOscillatorGlobal(Style.COLOR_A));
                
        vbox.add(hbox);


        vbox.add(addOscillator(1, Style.COLOR_A));
        vbox.add(addOscillator(2, Style.COLOR_A));
        vbox.add(addOscillator(3, Style.COLOR_A));
        vbox.add(addFilter(1, Style.COLOR_B));
        vbox.add(addFilter(2, Style.COLOR_B));

        soundPanel.add(vbox, BorderLayout.CENTER);
        tabs.addTab("Oscillators and Filters", soundPanel);
                
                
        // LFO and ENVELOPE PANEL
                
        lfoEnvelopePanel = new SynthPanel();
        vbox = new VBox();

        hbox = new HBox();
        hbox.add(addLFO(1, Style.COLOR_A));
        hbox.addLast(addLFO(3, Style.COLOR_A));
        vbox.add(hbox);
        
        hbox = new HBox();
        hbox.add(addLFO(2, Style.COLOR_A));
        hbox.addLast(addAmplifierGlobal(Style.COLOR_C));
        vbox.add(hbox);

        vbox.add(addEnvelope(1, Style.COLOR_B));
        vbox.add(addEnvelope(2, Style.COLOR_B));
        vbox.add(addEnvelope(3, Style.COLOR_B));
        vbox.add(addEnvelope(4, Style.COLOR_B));
                
        lfoEnvelopePanel.add(vbox, BorderLayout.CENTER);
        tabs.addTab("LFOs and Envelopes", lfoEnvelopePanel);
                
        // MODULATION PANEL
                
        modulationPanel = new SynthPanel();
                
        vbox = new VBox();
        vbox.add(addModulation(Style.COLOR_A));
        vbox.add(addModifiers(Style.COLOR_B));
                                
        vbox.add(addEffect(1, Style.COLOR_C));
        vbox.add(addEffect(2, Style.COLOR_C));

        modulationPanel.add(vbox, BorderLayout.CENTER);
        tabs.addTab("Modulation and Effects", modulationPanel);

        // ARPEGGIATOR PANEL
        arpeggiationPanel = new SynthPanel();
        
        vbox = new VBox();
        vbox.add(addArpeggiatorGlobal(Style.COLOR_A));
        vbox.add(addArpeggiatorPatterns(Style.COLOR_B));

        arpeggiationPanel.add(vbox, BorderLayout.CENTER);
        tabs.addTab("Arpeggiator", arpeggiationPanel);


        add(tabs, BorderLayout.CENTER);
                
                
        model.set("name", "Init            ");  // has to be 16 long
        
        addDefaults();
        getModel().resetToDefaults();
        }
                
                
               
                
    /// ARPEGGIATION


    // Adds the Global Arpeggiator category 
    public JComponent addArpeggiatorGlobal(Color color)
        {
        Category category = new Category("Arpeggiator", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
                        
        params = ARPEGGIATOR_MODES;
        comp = new Chooser("Mode", this, "arpeggiatormode", params);
        model.setSpecial("arpeggiatormode", 0);
        vbox.add(comp);

        params = ARPEGGIATOR_DIRECTIONS;
        comp = new Chooser("Direction", this, "arpeggiatordirection", params);
        vbox.add(comp);
                
        hbox.add(vbox);
        vbox = new VBox();

        params = ARPEGGIATOR_SORT_ORDERS;
        comp = new Chooser("Sort Order", this, "arpeggiatorsortorder", params);
        vbox.add(comp);

        params = ARPEGGIATOR_VELOCITY_MODES;
        comp = new Chooser("Velocity Mode", this, "arpeggiatorvelocitymode", params);
        vbox.add(comp);
                
        hbox.add(vbox);
        vbox = new VBox();
                
        comp = new CheckBox("Pattern Reset", this, "arpeggiatorpatternreset");
        vbox.add(comp);

        hbox.add(vbox);
                
        comp = new LabelledDial("Pattern", this, "arpeggiatorpatternlength", color, 0, 16)
            {
            public String map(int val)
                {
                return "" + (val + 1);
                }
            };
        ((LabelledDial)comp).setSecondLabel("Length");
        hbox.add(comp);

        comp = new LabelledDial("Pattern", this, "arpeggiatorpattern", color, 0, 16)
            {
            public String map(int val)
                {
                if (val == 0) return "Off";
                else if (val == 1) return "User";
                else return "" + (val - 1);
                }
            };
        model.setSpecial("arpeggiatorpattern", new int[] { 0, 1} );
        hbox.add(comp);

        comp = new LabelledDial("Clock", this, "arpeggiatorclock", color, 0, 42)
            {
            public String map(int val)
                {
                return ARPEGGIATOR_SPEEDS[val];
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Length", this, "arpeggiatorlength", color, 0, 43)
            {
            public String map(int val)
                {
                return ARPEGGIATOR_LENGTHS[val];
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Octave", this, "arpeggiatoroctave", color, 0, 9)
            {
            public String map(int val)
                {
                return "" + (val + 1);
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Timing", this, "arpeggiatortimingfactor", color, 0, 127);
        ((LabelledDial)comp).setSecondLabel("Factor");
        hbox.add(comp);

        comp = new LabelledDial("Tempo", this, "arpeggiatortempo", color, 0, 127)
            {
            // 40...300
            // BPM 165 and above we jump by 5
            // BPM 90 and below we jump by 2
                        
            // So 40...90 inclusive is 0...25
            // 91 ... 165 is 26 ... 100
            // 170 ... 300 is 101 ... 127
            public String map(int val)
                {
                if (val < 25)
                    return "" + ((val * 2) + 40);
                else if (val < 101)
                    return "" + (val + 65);
                else
                    return "" + (((val - 101) * 5) + 170);
                }
            };
        hbox.add(comp);


        category.add(hbox, BorderLayout.WEST);
        return category;
        }




    // Adds the Arpeggiator Patterns category       
    public JComponent addArpeggiatorPatterns(Color color)
        {
        Category category  = new Category("Patterns", color);
                        
        JComponent comp;
        String[] params;
        VBox main = new VBox();
        HBox hbox;
        VBox vbox;
        
        String[] steps = ARPEGGIATOR_PATTERN_STEPS;
        for(int row = 1; row < 17; row += 8)
            {
            hbox = new HBox();
            for(int i = row; i < row + 8; i++)
                {
                vbox = new VBox();
                params = steps;
                comp = new Chooser("Step " + i, this, "arp" + (i < 10 ? "0" : "") + i + "step", params);
                vbox.add(comp);

                comp = new CheckBox("Glide " + i, this, "arp" + (i < 10 ? "0" : "") + i + "glide");
                vbox.add(comp);

                comp = new LabelledDial("Accent " + i, this, "arp" + (i < 10 ? "0" : "") + i + "accent", color, 0, 7)
                    {
					public double getStartAngle() { return 270 / 7 * 4 + 90; }
                    public String map(int val)
                        {
                        // if 0, it's silent
                        // if >0 it's -96 ... +96 for 1...7
                        if (val == 0) return "Rest";
                        else return "" + (((val - 4) * 96) / 3);
                        }
                    };
                vbox.add(comp);

                // the little spaces cause Java to not slice off a bit of the last digit
                comp = new LabelledDial(" Length " + i + " ", this, "arp" + (i < 10 ? "0" : "") + i + "length", color, 0, 7)
                    {
					public double getStartAngle() { return 270 / 7 * 4 + 90; }
                    public String map(int val)
                        {
                        // if 0, it's legato
                        // if >0 it's -3 to +3 for 1...7
                        if (val == 0) return "Legato";
                        else return "" + (val - 4);
                        }
                    };
                vbox.add(comp);
                                
                // the little spaces cause Java to not slice off a bit of the last digit
                comp = new LabelledDial(" Timing " + i + " ", this, "arp" + (i < 10 ? "0" : "") + i + "timing", color, 0, 7)
                    {
					public double getStartAngle() { return 270 / 7 * 4 + 90; }
                    public String map(int val)
                        {
                        // if 0, it's random
                        // if >0 it's -3 to +3 for 1...7
                        if (val == 0) return "Rand";
                        else return "" + (val - 4);
                        }
                    };
                        
                vbox.add(comp);
                hbox.add(vbox);
                }

            main.add(hbox);
            }

                
        category.add(main, BorderLayout.WEST);
        return category;
        }








    //// EFFECTS
        
    // Effects are problematic because effect parameters are shared, and the same parameter
    // doesn't necessarily have the same range from effect type to effect type, grrr.
    // Additionally we have to remove and add various dials and other components depending
    // on the current effect being displayed, so we need to know the components we can show
    // and hide, and the boxes to put them in dynamically.

    // The two HBoxes for each effect (#1, #2)
    HBox[/*effect*/] parameters = new HBox[2];
        
    // The various JComponents for different effect parameters
    JComponent[/*effect*/][/*effect type*/][/*parameters*/] parametersByEffect = new JComponent[2][9][];
        
    // Various effect types
    public static final int BYPASS = 0;
    public static final int CHORUS = 1;
    public static final int FLANGER = 2;
    public static final int PHASER = 3;
    public static final int OVERDRIVE = 4;
    public static final int TRIPLEFX = 5;
    public static final int DELAY = 6;
    public static final int CLKDELAY = 7;
    public static final int REVERB = 8;


    /** Discards existing parameter widgets and loads new ones according to the
        effect type on the given effect number. */
    void setupEffect(int effect, int type)
        {
        if (parameters[effect - 1] == null) return;  // not ready yet
                
        parameters[effect - 1].removeAll();
        for(int i = 0; i < parametersByEffect[effect - 1][type].length; i++)
            {
            parameters[effect - 1].add(parametersByEffect[effect - 1][type][i]);
            }
        parameters[effect-1].revalidate();
        repaint();
        }

        
    /** Adds an Effect category.  */
    public JComponent addEffect(final int effect, Color color)
        {
        // The first thing we have to do is build all the effect parameters for all the effect types
        // and associate them with each effect type.  This is a lot of tedious work.
                
        parametersByEffect[effect - 1][BYPASS] = new JComponent[0];
        parametersByEffect[effect - 1][CHORUS] = new JComponent[2];
        parametersByEffect[effect - 1][FLANGER] = new JComponent[4];
        parametersByEffect[effect - 1][PHASER] = new JComponent[6];
        parametersByEffect[effect - 1][OVERDRIVE] = new JComponent[4];
        parametersByEffect[effect - 1][TRIPLEFX] = new JComponent[5];
        parametersByEffect[effect - 1][DELAY] = new JComponent[5];
        parametersByEffect[effect - 1][CLKDELAY] = new JComponent[5];
        parametersByEffect[effect - 1][REVERB] = new JComponent[7];
                
        JComponent comp;
        String[] params;
                
                
        comp = new LabelledDial("Speed", this, "effect" + effect + "parameter0", color, 0, 127);
        parametersByEffect[effect - 1][CHORUS][0] = comp;
        parametersByEffect[effect - 1][FLANGER][0] = comp;
        parametersByEffect[effect - 1][PHASER][0] = comp;
        parametersByEffect[effect - 1][TRIPLEFX][0] = comp;
                
        comp = new LabelledDial("Size", this, "effect" + effect + "parameter0", color, 0, 127);
        parametersByEffect[effect - 1][REVERB][0] = comp;

        comp = new LabelledDial("Depth", this, "effect" + effect + "parameter1", color, 0, 127);
        parametersByEffect[effect - 1][CHORUS][1] = comp;
        parametersByEffect[effect - 1][FLANGER][1] = comp;
        parametersByEffect[effect - 1][PHASER][1] = comp;
        parametersByEffect[effect - 1][TRIPLEFX][1] = comp;

        comp = new LabelledDial("Drive", this, "effect" + effect + "parameter1", color, 0, 127);
        parametersByEffect[effect - 1][OVERDRIVE][0] = comp;

        comp = new LabelledDial("Shape", this, "effect" + effect + "parameter1", color, 0, 127);
        parametersByEffect[effect - 1][REVERB][1] = comp;

        comp = new LabelledDial("Post Gain", this, "effect" + effect + "parameter2", color, 0, 127);
        parametersByEffect[effect - 1][OVERDRIVE][1] = comp;

        comp = new LabelledDial("Decay", this, "effect" + effect + "parameter2", color, 0, 127);
        parametersByEffect[effect - 1][REVERB][2] = comp;

        comp = new LabelledDial("Chorus Mix", this, "effect" + effect + "parameter3", color, 0, 127);
        parametersByEffect[effect - 1][TRIPLEFX][2] = comp;

        comp = new LabelledDial("Length", this, "effect" + effect + "parameter3", color, 0, 127);
        parametersByEffect[effect - 1][DELAY][0] = comp;

        comp = new LabelledDial("Feedback", this, "effect" + effect + "parameter4", color, 0, 127);
        parametersByEffect[effect - 1][FLANGER][2] = comp;
        parametersByEffect[effect - 1][PHASER][2] = comp;
        parametersByEffect[effect - 1][DELAY][1] = comp;
        parametersByEffect[effect - 1][CLKDELAY][0] = comp;

        comp = new LabelledDial("S&H", this, "effect" + effect + "parameter4", color, 0, 127);
        parametersByEffect[effect - 1][TRIPLEFX][3] = comp;

        comp = new LabelledDial("Center", this, "effect" + effect + "parameter5", color, 0, 127);
        parametersByEffect[effect - 1][PHASER][3] = comp;

        comp = new LabelledDial("Cutoff", this, "effect" + effect + "parameter5", color, 0, 127);
        parametersByEffect[effect - 1][OVERDRIVE][2] = comp;
        parametersByEffect[effect - 1][DELAY][2] = comp;
        parametersByEffect[effect - 1][CLKDELAY][1] = comp;

        comp = new LabelledDial("Overdrive", this, "effect" + effect + "parameter5", color, 0, 127);
        parametersByEffect[effect - 1][TRIPLEFX][4] = comp;

        comp = new LabelledDial("Lowpass", this, "effect" + effect + "parameter5", color, 0, 127);
        parametersByEffect[effect - 1][REVERB][3] = comp;

        comp = new LabelledDial("Spacing", this, "effect" + effect + "parameter6", color, 0, 127);
        parametersByEffect[effect - 1][PHASER][4] = comp;

        comp = new LabelledDial("Highpass", this, "effect" + effect + "parameter6", color, 0, 127);
        parametersByEffect[effect - 1][REVERB][4] = comp;

        comp = new LabelledDial("Diffusion", this, "effect" + effect + "parameter7", color, 0, 127);
        parametersByEffect[effect - 1][REVERB][5] = comp;

        // polarity (0=positive, don't ask why)
        VBox vbox = new VBox();
        comp = new CheckBox("Negative Polarity", this, "effect" + effect + "parameter8");
        vbox.add(comp);
        parametersByEffect[effect - 1][FLANGER][3] = vbox;
        parametersByEffect[effect - 1][PHASER][5] = vbox;
        parametersByEffect[effect - 1][DELAY][4] = vbox;
        parametersByEffect[effect - 1][CLKDELAY][4] = vbox;

        // we put this AFTER the Negative Polarity checkbox so that it revises the min/max
        comp = new LabelledDial("Damping", this, "effect" + effect + "parameter8", color, 0, 127);
        parametersByEffect[effect - 1][REVERB][6] = comp;
                        
        vbox = new VBox();
        params = DRIVE_CURVES;
        comp = new Chooser("Curve", this, "effect" + effect + "parameter9", params);
        vbox.add(comp);
        parametersByEffect[effect - 1][OVERDRIVE][3] = vbox;

        // we put this AFTER the Curve pop so that it revises the min/max
        comp = new LabelledDial("Spread", this, "effect" + effect + "parameter9", color, 0, 127, 64);
        parametersByEffect[effect - 1][DELAY][3] = comp;
        parametersByEffect[effect - 1][CLKDELAY][3] = comp;

        // A second Length!  Gagh
        final String[] vals = new String[30];
        System.arraycopy(ARPEGGIATOR_SPEEDS, 0, vals, 0, 30);  // just the first 30 
        comp = new LabelledDial("Length", this, "effect" + effect + "parameter10", color, 0, 29)
            {
            public String map(int val)
                {
                // it turns out that the Blofeld sends "127" when it doesn't have a setting yet for this
                // parameter.  Dumb dumb dumb.  So we have to check for that.
                if (val > 29)
                    val = 0;
                return vals[val];
                }
            };
        parametersByEffect[effect - 1][CLKDELAY][2] = comp;



        // Now we can set up the category as usual.
                
        Category category = new Category("Effect " + effect, color);
                        
        HBox main = new HBox();
        vbox = new VBox();
                
        if (effect == 1)
            params = EFFECTS_SHORT;
        else
            params = EFFECTS_LONG;
        comp = new Chooser("Type", this, "effect" + effect + "type", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                setupEffect(effect, getState());
                }
            };
        model.setSpecial("effect" + effect + "type", 0);
        vbox.add(comp);
        main.add(vbox);

        comp = new LabelledDial("Mix", this, "effect" + effect + "mix", color, 0, 127);
        main.add(comp);
                
        parameters[effect - 1] = new HBox();
        main.add(parameters[effect - 1]);

        category.add(main, BorderLayout.WEST);
                
        setupEffect(effect, BYPASS);
        return category;
        }




    /** Add the Modulation category */
    public JComponent addModulation(Color color)
        {
        Category category  = new Category("Modulation", color);
                        
        JComponent comp;
        String[] params;
        VBox main = new VBox();
        HBox hbox;
        VBox vbox;
        
        for(int row = 1; row < 17; row+= 8)
            {
            hbox = new HBox();
            for(int i = row; i < row + 8; i++)
                {
                vbox = new VBox();
                params = MOD_SOURCES;
                comp = new Chooser("Source " + i, this, "modulation" + i + "source", params);
                model.setSpecial("mod" + i + "source", 0);
                vbox.add(comp);

                params = MOD_DESTINATIONS;
                comp = new Chooser("Destination " + i, this, "modulation" + i + "destination", params);
                vbox.add(comp);

                comp = new LabelledDial("Level " + i, this, "modulation" + i + "amount", color, 0, 127, 64);  // it's Level, not Amount, so we save some horizontal space
                vbox.add(comp);

                hbox.add(vbox);
                }
                        
            main.add(hbox);
            }
                                
        category.add(main, BorderLayout.WEST);
        return category;
        }





    /** Add the Modifiers category */
    public JComponent addModifiers(Color color)
        {
        Category category  = new Category("Modifiers", color);
                        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox;
        
        for(int i = 1; i < 5; i++)
            {
            vbox = new VBox();
                        
                        
            params = MOD_SOURCES;
            comp = new Chooser("A Source " + i, this, "modifier" + i + "sourcea", params);
            model.setSpecial("modifier" + i + "sourcea", 0);
            vbox.add(comp);

            // gotta change the first one to "constant" from "off" if we're in Source B
            params = new String[MOD_SOURCES.length];
            System.arraycopy(MOD_SOURCES, 0, params, 0, MOD_SOURCES.length);
            params[0] = "Constant";

            comp = new Chooser("B Source " + i, this, "modifier" + i + "sourceb", params);
            model.setSpecial("modifier" + i + "sourceb", 0);
            vbox.add(comp);

            params = MODIFIER_OPERATORS;
            comp = new Chooser("Operation " + i, this, "modifier" + i + "operation", params);
            vbox.add(comp);

            // add some space
            if (i > 1)  // not the first one
                {
                JPanel panel = new JPanel()
                    {
                    public Dimension getMinimumSize() { return new Dimension(20,0); }
                    public Dimension getPreferredSize() { return new Dimension(20,0); }
                    public Dimension getMaximumSize() { return new Dimension(20, 0); }
                    };
                panel.setBackground(Style.BACKGROUND_COLOR);                    
                hbox.add(panel);
                }

            hbox.add(vbox);

            comp = new LabelledDial("Constant " + i, this, "modifier" + i + "constant", color, 0, 127, 64);
            hbox.add(comp);

            }
                                        
        category.add(hbox, BorderLayout.WEST);
        return category;
        }




    // Filter envelope is just envelope #1, Amplifier (why not amplitude?) envelope is just envelope #2
    public static final int FILTER_ENVELOPE = 1;
    public static final int AMPLIFIER_ENVELOPE = 2;

    EnvelopeDisplay[/*Env Number */][/*Envelope Type */] envelopeDisplays = new EnvelopeDisplay[4][5];
    HBox[/*Env Number*/] envelopeHBoxes = new HBox[4];
        
    /** Add an Envelope category */
    public JComponent addEnvelope(final int envelope, Color color)
        {
        Category category;
        if (envelope == FILTER_ENVELOPE)
            {
            category = new Category("Filter Envelope", color);
            }
        else if (envelope == AMPLIFIER_ENVELOPE)
            {
            category = new Category("Amplifier Envelope", color);
            }
        else 
            {
            category = new Category("Envelope " + envelope, color);
            }
                        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        params = ENVELOPE_TYPES;
        // adding this will call update immediately, but the envelope display's not in the HBox yet so we
        // need to wait a bit.  The use of a boolean[] rather than a boolean below is a trick to get around
        // the requirement that variables outside an anonymous class in Java must be final (a stupid thing
        // that better languages with closures don't require).
        
        final boolean[] goAhead = new boolean[] { false };
        Chooser comp1 = new Chooser("Mode", this, "envelope" + envelope + "mode", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                if (goAhead[0])
                    {
                    envelopeHBoxes[envelope - 1].removeLast();
                    envelopeHBoxes[envelope - 1].addLast(envelopeDisplays[envelope - 1][model.get(key, 0)]);
                    envelopeHBoxes[envelope - 1].revalidate();
                    envelopeHBoxes[envelope - 1].repaint();
                    }
                }
            };
        vbox.add(comp1);

        comp = new CheckBox("Single Trigger", this, "envelope" + envelope + "trigger");
        vbox.add(comp);

        hbox.add(vbox);

        comp = new LabelledDial("Attack", this, "envelope" + envelope + "attack", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Attack", this, "envelope" + envelope + "attacklevel", color, 0, 127);
        ((LabelledDial)comp).setSecondLabel("Level");
        hbox.add(comp);

        comp = new LabelledDial("Decay", this, "envelope" + envelope + "decay", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Sustain", this, "envelope" + envelope + "sustain", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Decay 2", this, "envelope" + envelope + "decay2", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Sustain 2", this, "envelope" + envelope + "sustain2", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Release", this, "envelope" + envelope + "release", color, 0, 127);
        hbox.add(comp);
        

        // ADSR
        envelopeDisplays[envelope - 1][0] = new EnvelopeDisplay(this, Color.red, 
            new String[] { null, "envelope" + envelope + "attack", "envelope" + envelope + "decay", null, "envelope" + envelope + "release" },
            new String[] { null, null, "envelope" + envelope + "sustain", "envelope" + envelope + "sustain", null },
            new double[] { 0, 0.25/127.0, 0.25 / 127.0,  0.25, 0.25/127.0},
            new double[] { 0, 1.0, 1.0 / 127.0, 1.0/127.0, 0 });

        // ADS1DS2R
        envelopeDisplays[envelope - 1][1] = new EnvelopeDisplay(this, Color.red, 
            new String[] { null, "envelope" + envelope + "attack", "envelope" + envelope + "decay", "envelope" + envelope + "decay2", null, "envelope" + envelope + "release" },
            new String[] { null, "envelope" + envelope + "attacklevel", "envelope" + envelope + "sustain", "envelope" + envelope + "sustain2", "envelope" + envelope + "sustain2", null },
            new double[] { 0, 0.2/127.0, 0.2 / 127.0,   0.2/127.0, 0.2, 0.2/127.0},
            new double[] { 0, 1.0/127.0, 1.0 / 127.0, 1.0 / 127.0, 1.0 / 127.0, 0 });

        // One Shot
        envelopeDisplays[envelope - 1][2] = envelopeDisplays[envelope - 1][1];
        /*
          new EnvelopeDisplay(this, Color.red, 
          new String[] { null, "envelope" + envelope + "attack", "envelope" + envelope + "decay", "envelope" + envelope + "release" },
          new String[] { null, null, "envelope" + envelope + "sustain", null },
          new double[] { 0, (1/3.0)/127.0, (1/3.0) / 127.0,   (1/3.0)/127.0 },
          new double[] { 0, 1.0, 1.0 / 127.0, 0 });
        */
                        
        // Loop S1S2
        envelopeDisplays[envelope - 1][3] = new EnvelopeDisplay(this, Color.red, 
            new String[] { null, "envelope" + envelope + "attack", "envelope" + envelope + "decay", "envelope" + envelope + "decay2", "envelope" + envelope + "release" },
            new String[] { null, "envelope" + envelope + "attacklevel", "envelope" + envelope + "sustain", "envelope" + envelope + "sustain2", null },
            new double[] { 0, 0.25/127.0, 0.25 / 127.0, 0.25/127.0, 0.25/127.0 },
            new double[] { 0, 1.0 / 127.0, 1.0 / 127.0, 1.0 / 127.0, 0 });

        // Loop All
        envelopeDisplays[envelope - 1][4] = envelopeDisplays[envelope - 1][3];
                
        comp = envelopeDisplays[envelope - 1][0];  // placeholder
        hbox.addLast(comp);
        envelopeHBoxes[envelope - 1] = hbox;
        
        // now we re-update the popup                
        goAhead[0] = true;
        comp1.update("envelope" + envelope + "mode", getModel());
                
        category.add(hbox, BorderLayout.WEST);
        return category;
        }




    /** Add an LFO category */
    public JComponent addLFO(final int lfo, Color color)
        {
        Category category = new Category("LFO " + lfo, color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
                        
        params = LFO_SHAPES;
        comp = new Chooser("Shape", this, "lfo" + lfo + "shape", params);
        vbox.add(comp);

        comp = new CheckBox("Sync", this, "lfo" + lfo + "sync");
        vbox.add(comp);

        comp = new CheckBox("Clocked", this, "lfo" + lfo + "clocked");
        vbox.add(comp);

        hbox.add(vbox);

        comp = new LabelledDial("Speed", this, "lfo" + lfo + "speed", color, 0, 127)
            {
            public String map(int val)
                {
                // we display this in two different ways depending on whether we're clocked or not
                if (model.get("lfo" + lfo + "clocked", 0) == 0)
                    return "" + val;
                else
                    {
                    val /= 2;  // we map to 0...63
                    String[] vals = LFO_SPEEDS;
                    return vals[val];
                    }
                }
            };
        model.register("lfo" + lfo + "clocked", (LabelledDial)comp);  // so we get updated if clocked changes
        hbox.add(comp);


        comp = new LabelledDial("Start Phase", this, "lfo" + lfo + "startphase", color, 0, 127)
            {
            public String map(int val)
                {
                // this one is complex.  LFO start phase is 0=off,
                // and 1=0 degrees, and .... 127 = 355 degrees
                if (val == 0) return "Free";
                else return "" + (int)(((val - 1) * 355.0) / 126.0); 
                }
            };
        model.setSpecial("lfo" + lfo + "startphase", 0);
        hbox.add(comp);

        comp = new LabelledDial("Delay", this, "lfo" + lfo + "delay", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Fade", this, "lfo" + lfo + "fade", color, 0, 127, 64);
        hbox.add(comp);

        comp = new LabelledDial("Keytrack", this, "lfo" + lfo + "keytrack", color, 0, 127, 64)
            {
            public boolean isSymmetric() { return true; } 
            public String map(int val)
                {
                return "" + (int)((val - 64) / 64.0 * 200.0) + "%";
                }
            };
        hbox.add(comp);


        category.add(hbox, BorderLayout.WEST);
        return category;
        }




    public boolean gatherInfo(String title, Model change)
        {
        JComboBox bank = new JComboBox(BANKS);
        bank.setEditable(false);
        bank.setMaximumRowCount(32);
        bank.setSelectedIndex(model.get("bank", 0));
                
        JTextField number = new JTextField("" + (model.get("number", 0) + 1), 3);

        JTextField id = new JTextField("" + model.get("id", 0), 3);
                
        while(true)
            {
            boolean result = doMultiOption(this, new String[] { "Bank", "Patch Number", "Blofeld ID" }, 
                new JComponent[] { bank, number, id }, title, "Enter the Bank, Patch number, and Blofeld ID.");
                
            if (result == false) 
                return false;
                                
            int n;
            try { n = Integer.parseInt(number.getText()); }
            catch (NumberFormatException e)
                {
                JOptionPane.showMessageDialog(null, "The Patch Number must be an integer 1 ... 128 ", title, JOptionPane.ERROR_MESSAGE);
                continue;
                }
            if (n < 1 || n > 128)
                {
                JOptionPane.showMessageDialog(null, "The Patch Number must be an integer 1 ... 128 ", title, JOptionPane.ERROR_MESSAGE);
                continue;
                }
                                
            int i;
            try { i = Integer.parseInt(id.getText()); }
            catch (NumberFormatException e)
                {
                JOptionPane.showMessageDialog(null, "The Blofeld ID must be an integer 0 ... 127 ", title, JOptionPane.ERROR_MESSAGE);
                continue;
                }
            if (i < 0 || i > 127)
                {
                JOptionPane.showMessageDialog(null, "The Blofeld ID  must be an integer 0 ... 127 ", title, JOptionPane.ERROR_MESSAGE);
                continue;
                }
                        
            change.set("bank", bank.getSelectedIndex());
            change.set("number", n - 1);
            change.set("id", i);
                        
            return true;
            }
        }

    public Category globalCategory;

    /** Add the global patch category (name, id, number, etc.) */
    public JComponent addNameGlobal(Color color)
        {
        globalCategory = new Category("Waldorf Blofeld", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
                
        comp = new StringComponent("Patch Name", this, "name", 16, "Name must be up to 16 ASCII characters.")
            {
            public boolean isValid(String val)
                {
                if (val.length() > 16) return false;
                for(int i = 0 ; i < val.length(); i++)
                    {
                    char c = val.charAt(i);
                    if (c < 32 || c > 127) return false;
                    }
                return true;
                }
                                
            public void update(String key, Model model)
                {
                super.update(key, model);
                updateTitle();
                }
            };
        model.setImmutable("name", true);
        vbox.add(comp);

        HBox hbox2 = new HBox();
                                
        params = CATEGORIES;
        comp = new Chooser("Category", this, "category", params);
        model.setImmutable("category", true);
        hbox2.add(comp);

        vbox.add(hbox2);
                
        hbox.add(vbox);

        comp = new LabelledDial("Bank", this, "bank", color, 0, 7)
            {
            public String map(int val)
                {
                String[] vals = BANKS;
                return vals[val];
                }
            };
        model.setImmutable("bank", true);
        hbox.add(comp);

        comp = new LabelledDial("Number", this, "number", color, 0, 127, -1);
        model.setImmutable("number", true);
        hbox.add(comp);

        comp = new LabelledDial("Blofeld ID", this, "id", color, 0, 127);
        model.setImmutable("id", true);
        hbox.add(comp);

        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }

        


    /** Add the Amplifier category. */
    public JComponent addAmplifierGlobal(Color color)
        {
        Category category = new Category("Amplifier", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
                        
        params = MOD_SOURCES;
        comp = new Chooser("Mod Source", this, "amplifiermodsource", params);
        model.setSpecial("amplifiermodsource", 0);
        vbox.add(comp);
                
        hbox.add(vbox);

        comp = new LabelledDial("Volume", this, "amplifiervolume", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Velocity", this, "amplifiervelocity", color, 0, 127, 64);
        hbox.add(comp);

        comp = new LabelledDial("Mod", this, "amplifiermodamount", color, 0, 127, 64);
        ((LabelledDial)comp).setSecondLabel("Amount");
        hbox.add(comp);

        category.add(hbox, BorderLayout.WEST);
        return category;
        }




    /** Add the Global Oscillator category */
    public JComponent addOscillatorGlobal(Color color)
        {
        Category category = new Category("Oscillators", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
                        
        params = OSCILLATOR_GLIDE_MODES; 
        comp = new Chooser("Glide Mode", this, "oscglidemode", params);
        vbox.add(comp);

        comp = new CheckBox("Glide", this, "oscglide");
        vbox.add(comp);

        comp = new CheckBox("Mono", this, "oscallocation");
        vbox.add(comp);

        hbox.add(vbox);
                
        comp = new LabelledDial("Glide Rate", this, "oscgliderate", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Unisono", this, "unisono", color, 0, 5)
            {
            public String map(int val)
                {
                if (val == 0) return "Off";
                else if (val == 1) return "Dual";
                else return ("" + (val + 1));
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Unisono", this, "unisonodetune", color, 0, 127);
        ((LabelledDial)comp).setSecondLabel("Detune");
        hbox.add(comp);
                
        comp = new LabelledDial("Noise", this, "noiselevel", color, 0, 127);
        ((LabelledDial)comp).setSecondLabel("Level");
        getModel().setSpecial("noiselevel", 0);
        hbox.add(comp);

        comp = new LabelledDial("Noise", this, "noisebalance", color, 0, 127)
            {
            public boolean isSymmetric() { return true; } 
            public String map(int val)
                {
                val -= 64;
                if (val < 0) return "F1 " + Math.abs(val);
                else if (val > 0) return "F2 " + val;
                else return "--";
                }
            };
        ((LabelledDial)comp).setSecondLabel("Balance");
        hbox.add(comp);

        comp = new LabelledDial("Noise", this, "noisecolour", color, 0, 127, 64);
        ((LabelledDial)comp).setSecondLabel("Colour");
        hbox.add(comp);


        comp = new LabelledDial("Ringmod", this, "ringmodlevel", color, 0, 127);
        ((LabelledDial)comp).setSecondLabel("Level");
        getModel().setSpecial("ringmodlevel", 0);
        hbox.add(comp);

        comp = new LabelledDial("Ringmod", this, "ringmodbalance", color, 0, 127)
            {
            public boolean isSymmetric() { return true; } 
            public String map(int val)
                {
                val -= 64;
                if (val < 0) return "F1 " + Math.abs(val);
                else if (val > 0) return "F2 " + val;
                else return "--";
                }
            };
        ((LabelledDial)comp).setSecondLabel("Balance");
        hbox.add(comp);

        category.add(hbox, BorderLayout.WEST);
        return category;
        }



    /** Add a Filter category */
    public JComponent addFilter(int filter, Color color)
        {
        Category category = new Category("Filter " + filter, color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
                
        params = FILTER_TYPES;
                
        comp = new Chooser("Type", this, "filter" + filter + "type", params);
        model.setSpecial("filter" + filter + "type", 0);
        vbox.add(comp);
                
        params = DRIVE_CURVES;
        comp = new Chooser("Drive Curve", this, "filter" + filter + "drivecurve", params);
        vbox.add(comp);

        hbox.add(vbox);
        vbox = new VBox();
                
        params = MOD_SOURCES;
        comp = new Chooser("Mod Source", this, "filter" + filter + "modsource", params);
        model.setSpecial("filter" + filter + "modsource", 0);
        vbox.add(comp);

        params = MOD_SOURCES;
        comp = new Chooser("Pan Source", this, "filter" + filter + "pansource", params);
        model.setSpecial("filter" + filter + "pansource", 0);
        vbox.add(comp);

        hbox.add(vbox);
        vbox = new VBox();

        params = FM_SOURCES;
        comp = new Chooser("FM Source", this, "filter" + filter + "fmsource", params);
        model.setSpecial("filter" + filter + "fmsource", 0);
        vbox.add(comp);

        if (filter == 2)
            {
            comp = new CheckBox("Serial", this, "filterrouting");
            vbox.add(comp);
            }

        hbox.add(vbox);
                
        comp = new LabelledDial("Cutoff", this, "filter" + filter + "cutoff", color, 0, 127);
        hbox.add(comp);
                
        comp = new LabelledDial("Resonance", this, "filter" + filter + "resonance", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Drive", this, "filter" + filter + "drive", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Keytrack", this, "filter" + filter + "keytrack", color, 0, 127)
            {
            public boolean isSymmetric() { return true; } 
            public String map(int val)
                {
                return "" + (int)((val - 64) / 64.0 * 200.0) + "%";
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Env", this, "filter" + filter + "envamount", color, 0, 127, 64);
        ((LabelledDial)comp).setSecondLabel("Amount");
        hbox.add(comp);

        comp = new LabelledDial("Env", this, "filter" + filter + "envvelocity", color, 0, 127, 64);
        ((LabelledDial)comp).setSecondLabel("Velocity");
        hbox.add(comp);
                
        comp = new LabelledDial("Mod", this, "filter" + filter + "modamount", color, 0, 127, 64);
        ((LabelledDial)comp).setSecondLabel("Amount");
        hbox.add(comp);
                
        comp = new LabelledDial("FM", this, "filter" + filter + "fmamount", color, 0, 127);
        ((LabelledDial)comp).setSecondLabel("Amount");
        hbox.add(comp);

        comp = new LabelledDial("Pan", this, "filter" + filter + "pan", color, 0, 127)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val)
                {
                val -= 64;
                if (val < 0) return "L " + Math.abs(val);
                else if (val > 0) return "R " + val;
                else return "--";
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Pan", this, "filter" + filter + "panamount", color, 0, 127, 64);
        ((LabelledDial)comp).setSecondLabel("Amount");
        hbox.add(comp);

        category.add(hbox, BorderLayout.WEST);
        return category;
        }
                


    /** Add an Oscillator category */
    public JComponent addOscillator(int osc, Color color)
        {
        Category category = new Category("Oscillator " + osc, color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
                
        String[] params1 = WAVES_LONG;
        params = new String[125];
        System.arraycopy(params1, 0, params, 0, 73);
        for(int i = 73; i < 86; i++)
            params[i] = "Reserved " + (i - 6);
        for(int i = 86; i < 125; i++)
            params[i] = "User " + (i - 6);
        if (osc == 3) params = WAVES_SHORT;
        comp = new Chooser("Wave", this, "osc" + osc + "shape", params);
        if (osc == 3)
            model.setSpecial("osc" + osc + "shape", 0);
        else
            model.setImmutable("osc" + osc + "shape", true);
        vbox.add(comp);

        if (osc != 3)
            {
            // 0 is ON for Limit WT, 1 is OFF.  It's flipped relative to other switches
            comp = new CheckBox("Limit WT", this, "osc" + osc + "limitwt", true);
            vbox.add(comp);
            }
                
        if (osc == 2)
            {
            comp = new CheckBox("Sync to Osc 3", this, "osc" + osc + "synctoosc3");
            vbox.add(comp);
            }

        hbox.add(vbox);
        vbox = new VBox();
                
        params = FM_SOURCES;
        comp = new Chooser("FM Source", this, "osc" + osc + "fmsource", params);
        model.setSpecial("osc" + osc + "fmsource", 0);
        vbox.add(comp);

        params = MOD_SOURCES;
        comp = new Chooser("PWM Source", this, "osc" + osc + "pwmsource", params);
        model.setSpecial("osc" + osc + "pwmsource", 0);
        vbox.add(comp);

        hbox.add(vbox);
                
        comp = new LabelledDial("Octave", this, "osc" + osc + "octave", color, 0, 8)
            {
            public String map(int val)
                {
                String[] oct = OSCILLATOR_OCTAVES;
                return oct[val];
                }
            };
        hbox.add(comp);
                
        comp = new LabelledDial("Semitone", this, "osc" + osc + "semitone", color, 52, 76, 64);
        hbox.add(comp);

        comp = new LabelledDial("Detune", this, "osc" + osc + "detune", color, 0, 127, 64);
        hbox.add(comp);

        comp = new LabelledDial("Bend", this, "osc" + osc + "bendrange", color, 40, 88, 64);
        ((LabelledDial)comp).setSecondLabel("Range");
        hbox.add(comp);

        comp = new LabelledDial("Keytrack", this, "osc" + osc + "keytrack", color, 0, 127)
            {
            public boolean isSymmetric() { return true; } 
            public String map(int val)
                {
                return "" + (int)((val - 64) / 64.0 * 200.0) + "%";
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("FM", this, "osc" + osc + "fmamount", color, 0, 127);
        ((LabelledDial)comp).setSecondLabel("Amount");
        hbox.add(comp);

        comp = new LabelledDial("Pulsewidth", this, "osc" + osc + "pulsewidth", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("PWM", this, "osc" + osc + "pwmamount", color, 0, 127, 64);
        ((LabelledDial)comp).setSecondLabel("Amount");
        hbox.add(comp);
                
        comp = new LabelledDial("Brilliance", this, "osc" + osc + "brilliance", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Level", this, "osc" + osc + "level", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Filter", this, "osc" + osc + "balance", color, 0, 127)
            {
            public boolean isSymmetric() { return true; } 
            public String map(int val)
                {
                val -= 64;
                if (val < 0) return "F1 " + Math.abs(val);
                else if (val > 0) return "F2 " + val;
                else return "--";
                }
            };
        ((LabelledDial)comp).setSecondLabel("Balance");
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
        


    /** Map of parameter -> index in the allParameters array. */
    HashMap allParametersToIndex = new HashMap();


    /** List of all Waldorf parameters in order.  "-" is a reserved (unused and thus unnamed) parameter. */

    /// * indicates parameters which must be handled specially due to packing
    /// that Waldorf decided to do.  :-(

    final static String[] allParameters = new String[] 
    {
    "-",
    "osc1octave",                   // *
    "osc1semitone",
    "osc1detune",
    "osc1bendrange",
    "osc1keytrack",
    "osc1fmsource",
    "osc1fmamount",
    "osc1shape",
    "osc1pulsewidth",
    "osc1pwmsource",
    "osc1pwmamount",
    "-",
    "-",
    "osc1limitwt",
    "-",
    "osc1brilliance",
    "osc2octave",                   // *
    "osc2semitone",
    "osc2detune",
    "osc2bendrange",
    "osc2keytrack",
    "osc2fmsource",
    "osc2fmamount",
    "osc2shape",
    "osc2pulsewidth",
    "osc2pwmsource",
    "osc2pwmamount",
    "-",
    "-",
    "osc2limitwt",
    "-",
    "osc2brilliance",
    "osc3octave",                   // *
    "osc3semitone",
    "osc3detune",
    "osc3bendrange",
    "osc3keytrack",
    "osc3fmsource",
    "osc3fmamount",
    "osc3shape",
    "osc3pulsewidth",
    "osc3pwmsource",
    "osc3pwmamount",
    "-",
    "-",
    "-",
    "-",
    "osc3brilliance",
    "osc2synctoosc3",
    "oscpitchsource",
    "oscpitchamount",
    "-",
    "oscglide",
    "-",
    "-",
    "oscglidemode",
    "oscgliderate",
    "oscallocation, unisono",       // *
    "unisonodetune",
    "-",
    "osc1level",
    "osc1balance",
    "osc2level",
    "osc2balance",
    "osc3level",
    "osc3balance",
    "noiselevel",
    "noisebalance",
    "noisecolour",
    "-",
    "ringmodlevel",
    "ringmodbalance",
    "-",
    "-",
    "-",
    "-",
    "filter1type",
    "filter1cutoff",
    "-",
    "filter1resonance",
    "filter1drive",
    "filter1drivecurve",
    "-",
    "-",
    "-",
    "filter1keytrack",
    "filter1envamount",
    "filter1envvelocity",
    "filter1modsource",
    "filter1modamount",
    "filter1fmsource",
    "filter1fmamount",
    "filter1pan",
    "filter1pansource",
    "filter1panamount",
    "-",
    "filter2type",
    "filter2cutoff",
    "-",
    "filter2resonance",
    "filter2drive",
    "filter2drivecurve",
    "-",
    "-",
    "-",
    "filter2keytrack",
    "filter2envamount",
    "filter2envvelocity",
    "filter2modsource",
    "filter2modamount",
    "filter2fmsource",
    "filter2fmamount",
    "filter2pan",
    "filter2pansource",
    "filter2panamount",
    "-",
    "filterrouting",
    "-",
    "-",
    "-",
    "amplifiervolume",
    "amplifiervelocity",
    "amplifiermodsource",
    "amplifiermodamount",
    "-",
    "-",
    "-",
    "effect1type",
    "effect1mix",

    // Note that we start at 0 -- this makes things easier in the code
    "effect1parameter0",
    "effect1parameter1",
    "effect1parameter2",
    "effect1parameter3",
    "effect1parameter4",
    "effect1parameter5",
    "effect1parameter6",
    "effect1parameter7",
    "effect1parameter8",
    "effect1parameter9",
    "effect1parameter10",
    "effect1parameter11",
    "effect1parameter12",
    "effect1parameter13",
    "effect2type",
    "effect2mix",

    // Note that we start at 0 -- this makes things easier in the code
    "effect2parameter0",
    "effect2parameter1",
    "effect2parameter2",
    "effect2parameter3",
    "effect2parameter4",
    "effect2parameter5",
    "effect2parameter6",
    "effect2parameter7",
    "effect2parameter8",
    "effect2parameter9",
    "effect2parameter10",
    "effect2parameter11",
    "effect2parameter12",
    "effect2parameter13",
    "lfo1shape",
    "lfo1speed",
    "-",
    "lfo1sync",
    "lfo1clocked",
    "lfo1startphase",
    "lfo1delay",
    "lfo1fade",
    "-",
    "-",
    "lfo1keytrack",
    "-",
    "lfo2shape",
    "lfo2speed",
    "-",
    "lfo2sync",
    "lfo2clocked",
    "lfo2startphase",
    "lfo2delay",
    "lfo2fade",
    "-",
    "-",
    "lfo2keytrack",
    "-",
    "lfo3shape",
    "lfo3speed",
    "-",
    "lfo3sync",
    "lfo3clocked",
    "lfo3startphase",
    "lfo3delay",
    "lfo3fade",
    "-",
    "-",
    "lfo3keytrack",
    "-",
    "envelope1mode, envelope1trigger",              // *
    "-",
    "-",
    "envelope1attack",
    "envelope1attacklevel",
    "envelope1decay",
    "envelope1sustain",
    "envelope1decay2",
    "envelope1sustain2",
    "envelope1release",
    "-",
    "-",
    "envelope2mode, envelope2trigger",              // *
    "-",
    "-",
    "envelope2attack",
    "envelope2attacklevel",
    "envelope2decay",
    "envelope2sustain",
    "envelope2decay2",
    "envelope2sustain2",
    "envelope2release",
    "-",
    "-",
    "envelope3mode, envelope3trigger",              // *
    "-",
    "-",
    "envelope3attack",
    "envelope3attacklevel",
    "envelope3decay",
    "envelope3sustain",
    "envelope3decay2",
    "envelope3sustain2",
    "envelope3release",
    "-",
    "-",
    "envelope4mode, envelope4trigger",              // *
    "-",
    "-",
    "envelope4attack",
    "envelope4attacklevel",
    "envelope4decay",
    "envelope4sustain",
    "envelope4decay2",
    "envelope4sustain2",
    "envelope4release",
    "-",
    "-",
    "-",
    "modifier1sourcea",
    "modifier1sourceb",
    "modifier1operation",
    "modifier1constant",
    "modifier2sourcea",
    "modifier2sourceb",
    "modifier2operation",
    "modifier2constant",
    "modifier3sourcea",
    "modifier3sourceb",
    "modifier3operation",
    "modifier3constant",
    "modifier4sourcea",
    "modifier4sourceb",
    "modifier4operation",
    "modifier4constant",
    "modulation1source",
    "modulation1destination",
    "modulation1amount",
    "modulation2source",
    "modulation2destination",
    "modulation2amount",
    "modulation3source",
    "modulation3destination",
    "modulation3amount",
    "modulation4source",
    "modulation4destination",
    "modulation4amount",
    "modulation5source",
    "modulation5destination",
    "modulation5amount",
    "modulation6source",
    "modulation6destination",
    "modulation6amount",
    "modulation7source",
    "modulation7destination",
    "modulation7amount",
    "modulation8source",
    "modulation8destination",
    "modulation8amount",
    "modulation9source",
    "modulation9destination",
    "modulation9amount",
    "modulation10source",
    "modulation10destination",
    "modulation10amount",
    "modulation11source",
    "modulation11destination",
    "modulation11amount",
    "modulation12source",
    "modulation12destination",
    "modulation12amount",
    "modulation13source",
    "modulation13destination",
    "modulation13amount",
    "modulation14source",
    "modulation14destination",
    "modulation14amount",
    "modulation15source",
    "modulation15destination",
    "modulation15amount",
    "modulation16source",
    "modulation16destination",
    "modulation16amount",
    "-",
    "-",
    "arpeggiatormode",
    "arpeggiatorpattern",
    "-",
    "arpeggiatorclock",
    "arpeggiatorlength",
    "arpeggiatoroctave",
    "arpeggiatordirection",
    "arpeggiatorsortorder",
    "arpeggiatorvelocitymode",
    "arpeggiatortimingfactor",
    "-",
    "arpeggiatorpatternreset",
    "arpeggiatorpatternlength",
    "-",
    "-",
    "arpeggiatortempo",
    "arp01step, arp01glide, arp01accent",                   // *
    "arp02step, arp02glide, arp02accent",                   // *
    "arp03step, arp03glide, arp03accent",                   // *
    "arp04step, arp04glide, arp04accent",                   // *
    "arp05step, arp05glide, arp05accent",                   // *
    "arp06step, arp06glide, arp06accent",                   // *
    "arp07step, arp07glide, arp07accent",                   // *
    "arp08step, arp08glide, arp08accent",                   // *
    "arp09step, arp09glide, arp09accent",                   // *
    "arp10step, arp10glide, arp10accent",                   // *
    "arp11step, arp11glide, arp11accent",                   // *
    "arp12step, arp12glide, arp12accent",                   // *
    "arp13step, arp13glide, arp13accent",                   // *
    "arp14step, arp14glide, arp14accent",                   // *
    "arp15step, arp15glide, arp15accent",                   // *
    "arp16step, arp16glide, arp16accent",                   // *
    "arp01timing, arp01length",                     // *
    "arp02timing, arp02length",                     // *
    "arp03timing, arp03length",                     // *
    "arp04timing, arp04length",                     // *
    "arp05timing, arp05length",                     // *
    "arp06timing, arp06length",                     // *
    "arp07timing, arp07length",                     // *
    "arp08timing, arp08length",                     // *
    "arp09timing, arp09length",                     // *
    "arp10timing, arp10length",                     // *
    "arp11timing, arp11length",                     // *
    "arp12timing, arp12length",                     // *
    "arp13timing, arp13length",                     // *
    "arp14timing, arp14length",                     // *
    "arp15timing, arp15length",                     // *
    "arp16timing, arp16length",                     // *
    "-",
    "-",
    "-",
    "-",
    "name",         // *
    "name",         // *
    "name",         // *
    "name",         // *
    "name",         // *
    "name",         // *
    "name",         // *
    "name",         // *
    "name",         // *
    "name",         // *
    "name",         // *
    "name",         // *
    "name",         // *
    "name",         // *
    "name",         // *
    "name",         // *
    "category",
    "-",
    "-",
    "-"
    };





    public byte[] emit(String key)
        {
        if (key.equals("id")) return new byte[0];  // this is not emittable
        if (key.equals("bank")) return new byte[0];  // this is not emittable
        if (key.equals("number")) return new byte[0];  // this is not emittable
        byte DEV = (byte)model.get("id", 0);
        if (key.equals("osc1octave") || key.equals("osc2octave") || key.equals("osc3octave"))
            {
            int index = ((Integer)(allParametersToIndex.get(key))).intValue();
            byte HH = (byte)((index >> 7) & 127);
            byte PP = (byte)(index & 127);
            byte XX = (byte)(16 + model.get(key, 0) * 12);
            return new byte[] { (byte)0xF0, 0x3E, 0x13, DEV, 0x20, 0x00, HH, PP, XX, (byte)0xF7 };
            }
        else if (key.equals("oscallocation") || key.equals("unisono"))
            {
            int index = 58;
            byte HH = (byte)((index >> 7) & 127);
            byte PP = (byte)(index & 127);
            byte XX = (byte)((model.get("oscallocation", 0) << 4) | (model.get("unisono", 0)));
            return new byte[] { (byte)0xF0, 0x3E, 0x13, DEV, 0x20, 0x00, HH, PP, XX, (byte)0xF7 };
            }
        else if (key.equals("envelope1mode") || key.equals("envelope1trigger") ||
            key.equals("envelope2mode") || key.equals("envelope2trigger") ||
            key.equals("envelope3mode") || key.equals("envelope3trigger") ||
            key.equals("envelope4mode") || key.equals("envelope4trigger"))
            {
            int i;
            try { i = Integer.parseInt(key.substring(8, 9)); }
            catch (Exception e) { e.printStackTrace(); return new byte[0]; }
            int index = 196 + (i - 1) * 12;
            byte HH = (byte)((index >> 7) & 127);
            byte PP = (byte)(index & 127);
            byte XX = (byte)((model.get("envelope" + i + "trigger", 0) << 5) | (model.get("envelope" + i + "mode", 0)));
            return new byte[] { (byte)0xF0, 0x3E, 0x13, DEV, 0x20, 0x00, HH, PP, XX, (byte)0xF7 };
            }
        else if (key.startsWith("arp") && !key.startsWith("arpegg"))
            {
            int i;
            try { i = Integer.parseInt(key.substring(3, 5)); }
            catch (Exception e) { e.printStackTrace(); return new byte[0]; }
            if (key.endsWith("length") || key.endsWith("timing"))
                {
                int index = i + 342;
                byte HH = (byte)((index >> 7) & 127);
                byte PP = (byte)(index & 127);
                byte XX = (byte)((model.get("arp" + (i < 10 ? "0" : "") + i + "length", 0) << 4) | 
                    (model.get("arp" + (i < 10 ? "0" : "") + i + "timing", 0)));
                return new byte[] { (byte)0xF0, 0x3E, 0x13, DEV, 0x20, 0x00, HH, PP, XX, (byte)0xF7 };
                }
            else
                {
                int index = i + 326;
                byte HH = (byte)((index >> 7) & 127);
                byte PP = (byte)(index & 127);
                byte XX = (byte)((model.get("arp" + (i < 10 ? "0" : "") + i + "step", 0) << 4) |
                    (model.get("arp" + (i < 10 ? "0" : "") + i + "glide", 0) << 3) |
                    (model.get("arp" + (i < 10 ? "0" : "") + i + "accent", 0)));
                return new byte[] { (byte)0xF0, 0x3E, 0x13, DEV, 0x20, 0x00, HH, PP, XX, (byte)0xF7 };
                }
            }
        else if (key.equals("name"))
            {
            byte[] bytes = new byte[16 * 10];
            String name = model.get(key, "Init            ") + "                ";  // just to be safe, has to be 16 long
            for(int i = 0; i < 15; i++)
                {
                byte c = (byte)(name.charAt(i));
                int index = i + 363;
                byte HH = (byte)((index >> 7) & 127);
                byte PP = (byte)(index & 127);
                byte XX = c;
                byte[] b = new byte[] { (byte)0xF0, 0x3E, 0x13, DEV, 0x20, 0x00, HH, PP, XX, (byte)0xF7 };
                System.arraycopy(b, 0, bytes, 10 * i, 10);
                }
            return bytes;
            }
        else
            {
            int index = ((Integer)(allParametersToIndex.get(key))).intValue();
            byte HH = (byte)((index >> 7) & 127);
            byte PP = (byte)(index & 127);
            byte XX = (byte)model.get(key, 0);
            return new byte[] { (byte)0xF0, 0x3E, 0x13, DEV, 0x20, 0x00, HH, PP, XX, (byte)0xF7 };
            }
        }
    
    
    
    
    public byte[] emit(Model tempModel)
        {
        if (tempModel == null)
            tempModel = getModel();
        byte DEV = (byte) tempModel.get("id", 0);
        byte BB = (byte) tempModel.get("bank", 0);
        byte NN = (byte) tempModel.get("number", 0);
        
        byte[] bytes = new byte[383];
        
        for(int i = 0; i < 363; i++)
            {
            String key = allParameters[i];
            if (key.equals("-"))
                {
                bytes[i] = 0;
                }
            else if (key.equals("osc1octave") || key.equals("osc2octave") || key.equals("osc3octave"))
                {
                bytes[i] = (byte)(16 + model.get(key, 0) * 12);
                }
            else if (key.equals("oscallocation, unisono"))
                {
                bytes[i] = (byte)((model.get("oscallocation", 0) << 4) | (model.get("unisono", 0)));
                }
            else if (key.equals("envelope1mode, envelope1trigger") ||
                key.equals("envelope2mode, envelope2trigger") ||
                key.equals("envelope3mode, envelope3trigger") ||
                key.equals("envelope4mode, envelope4trigger"))
                {
                int j;
                bytes[i] = 0;  // or whatever
                try { 
                    j = Integer.parseInt(key.substring(8, 9)); 
                    bytes[i] = (byte)((model.get("envelope" + j + "trigger", 0) << 5) | (model.get("envelope" + j + "mode", 0)));
                    }
                catch (Exception e) { e.printStackTrace(); }
                }
            else if (i >= 327 && i <= 342) // step/glide/accent
                {
                int j = i - 326;
                bytes[i] = (byte)((model.get("arp" + (j < 10 ? "0" : "") + j + "step", 0) << 4) |
                    (model.get("arp" + (j < 10 ? "0" : "") + j + "glide", 0) << 3) |
                    (model.get("arp" + (j < 10 ? "0" : "") + j + "accent", 0)));
                }
            else if (i >= 343 && i <= 358) // timing/length
                {
                int j = i - 342;
                bytes[i] = (byte)((model.get("arp" + (j < 10 ? "0" : "") + j + "length", 0) << 4) | 
                    (model.get("arp" + (j < 10 ? "0" : "") + j + "timing", 0)));
                }
            else
                {
                bytes[i] = (byte)(model.get(key, 0));
                }
            }

        String name = model.get("name", "Init            ");  // has to be 16 long
                                
        for(int i = 363; i < 379; i++)
            {
            bytes[i] = (byte)(name.charAt(i - 363));
            }
                
        bytes[379] = (byte)(model.get("category", 0));
                
        bytes[380] = 0;
        bytes[381] = 0;
        bytes[382] = 0;


        byte[] full = new byte[392];
        full[0] = (byte)0xF0;
        full[1] = 0x3E;
        full[2] = 0x13;
        full[3] = DEV;
        full[4] = 0x10;
        full[5] = BB;
        full[6] = NN;
        System.arraycopy(bytes, 0, full, 7, bytes.length);
        full[390] = produceChecksum(bytes);
        full[391] = (byte)0xF7;

        return full;
        }


    /** Generate a Waldorf checksum of the data bytes */
    byte produceChecksum(byte[] bytes)
        {
        //      From the sysex document:
        //
        //      "Sum of all databytes truncated to 7 bits.
        //  The addition is done in 8 bit format, the result is    
        //  masked to 7 bits (00h to 7Fh). A checksum of 7Fh is
        //  always accepted as valid.
        //  IMPORTANT: the MIDI status-bytes as well as the 
        //  ID's are not used for computing the checksum."
                
        byte b = 0;  // I *think* signed will work
        for(int i = 0; i < bytes.length; i++)
            b += bytes[i];
        
        b = (byte)(b & (byte)127);
        
        return b;
        }


    public byte[] requestDump(Model tempModel)
        {
        if (tempModel == null)
            tempModel = getModel();
        byte DEV = (byte)tempModel.get("id", 0);
        byte BB = (byte)tempModel.get("bank", 0);
        byte NN = (byte)tempModel.get("number", 0);
        return new byte[] { (byte)0xF0, 0x3E, 0x13, DEV, 0x00, BB, NN, 0x00, (byte)0xF7 };
        }
        
    public static boolean recognize(byte[] data)
        {
        boolean v = (data[0] == (byte)0xF0 &&
            data[1] == (byte)0x3E &&
            data[2] == (byte)0x13 &&
            data.length == 392);
        return v;
        }
        
        
    public int getExpectedSysexLength() { return 392; }
        
        
    /** Verify that all the parameters are within valid values, and tweak them if not. */
    void revise()
        {
        for(int i = 0; i < allParameters.length; i++)
            {
            String key = allParameters[i];
            if (!model.isString(key))
                {
                if (model.minExists(key) && model.maxExists(key))
                    {
                    int val = model.get(key, 0);
                    if (val < model.getMin(key))
                        model.set(key, model.getMin(key));
                    if (val > model.getMax(key))
                        model.set(key, model.getMax(key));
                    }
                }
            }
        // handle "name" specially
        StringBuffer name = new StringBuffer(model.get("name", "Init            "));  // has to be 16 long
        for(int i = 0; i < name.length(); i++)
            {
            char c = name.charAt(i);
            if (c < 32 || c > 127)
                name.setCharAt(i, (char)32);
            }
        model.set("name", name.toString());
        }
        
    public void parse(byte[] data)
        {
        model.set("id", data[3]);
        model.set("bank", data[5]);
        model.set("number", data[6]);
        
        byte[] bytes = new byte[383];
        System.arraycopy(data, 7, bytes, 0, 383);
        
        for(int i = 0; i < 363; i++)
            {
            byte b = data[i + 7];
            bytes[i] = b;
                
            String key = allParameters[i];
            if (key.equals("-"))
                {
                // do nothing
                }
            else if (key.equals("osc1octave") || key.equals("osc2octave") || key.equals("osc3octave"))
                {
                model.set(key, (b - 16) / 12);
                }
            else if (key.equals("oscallocation, unisono"))
                {
                model.set("oscallocation", b >> 4);
                model.set("unisono", b & 7);
                }
            else if (key.equals("envelope1mode, envelope1trigger") ||
                key.equals("envelope2mode, envelope2trigger") ||
                key.equals("envelope3mode, envelope3trigger") ||
                key.equals("envelope4mode, envelope4trigger"))
                {
                try { 
                    int j = Integer.parseInt(key.substring(8, 9)); 
                    model.set("envelope" + j + "trigger", b >> 5);
                    model.set("envelope" + j + "mode", b & 7);  // even though it's supposed to be 5 bits, only 3 are used!
                    }
                catch (Exception e) { e.printStackTrace(); }
                }
            else if (i >= 327 && i <= 342) // step/glide/accent
                {
                int j = i - 326;
                model.set("arp" + (j < 10 ? "0" : "") + j + "step", b >> 4);
                model.set("arp" + (j < 10 ? "0" : "") + j + "glide", (b >> 3) & 1);
                model.set("arp" + (j < 10 ? "0" : "") + j + "accent", (b & 7));
                }
            else if (i >= 343 && i <= 358) // timing/length
                {
                int j = i - 342;
                model.set("arp" + (j < 10 ? "0" : "") + j + "length", b >> 4);
                model.set("arp" + (j < 10 ? "0" : "") + j + "timing", b & 7);
                }
            else
                {
                model.set(key, b);
                }
            }

        byte[] name_b = new byte[16];
        System.arraycopy(data, 363 + 7, name_b, 0, 16);
                
        String name = new String(name_b); // default encoding should be sufficient
        model.set("name", name);

        model.set("category", data[379 + 7]);   
                
        revise();       
        }
        
    public void merge(Model otherModel, double probability)
        {
        String[] keys = getModel().getKeys();
        for(int i = 0; i < keys.length; i++)
            {
            if (keys[i].equals("id")) continue;
            if (keys[i].equals("number")) continue;
            if (keys[i].equals("bank")) continue;
            if (keys[i].equals("name")) continue;
            if (keys[i].equals("category")) continue;
                
            if (coinToss(probability))
                {
                if (otherModel.isString(keys[i]))
                    {
                    getModel().set(keys[i], otherModel.get(keys[i], getModel().get(keys[i], "")));
                    }
                else
                    {
                    getModel().set(keys[i], otherModel.get(keys[i], getModel().get(keys[i], 0)));
                    }
                }
            }
        }
    
    public void immutableMutate(String key)
        {
        // we randomize these specially, taking care not to do the high waves
        if (key.equals("osc1shape") || key.equals("osc2shape"))
            {
            if (coinToss(0.5))
                model.set(key, 0);
            else
                model.set(key, random.nextInt(WAVES_LONG.length -1) + 1);
            }
        }
        
    public Synth doNew()
        {
        return new Blofeld();
        }

    public boolean requestCloseWindow() { return true; }

    public String getSynthName() { return "Waldorf Blofeld"; }
    
    public String getPatchName() { return model.get("name", "Init            "); }
    
    
    /** Adds all the defaults in DEFAULT_PARAMS to the Model's defaults storage. */
    void addDefaults()
        {
        for(int i = 0; i < DEFAULT_PARAMS.length; i++)
            {
            if (model.isString(DEFAULT_PARAMS[i][0]))
                {
                model.addDefault(DEFAULT_PARAMS[i][0], DEFAULT_PARAMS[i][1]);
                }
            else
                {
                try { model.addDefault(DEFAULT_PARAMS[i][0], Integer.parseInt(DEFAULT_PARAMS[i][1])); }
                catch (NumberFormatException e) { e.printStackTrace(); } // shouldn't ever happen
                }
            }
        }
    
    /** These are the parameters stored in the Blofeld when it is reset to the Init patch */
    public final static String[][] DEFAULT_PARAMS = new String[][]
    {
    {"category", "0"},    
    {"bank", "0"},
    {"number", "0"},    
    {"id", "0"},    
    {"oscglidemode", "0"},    
    {"oscglide", "0"},    
    {"oscallocation", "0"},    
    {"oscgliderate", "20"},    
    {"unisono", "0"},    
    {"unisonodetune", "0"},    
    {"noiselevel", "0"},    
    {"noisebalance", "0"},    
    {"noisecolour", "64"},    
    {"ringmodlevel", "0"},    
    {"ringmodbalance", "0"},    
    {"osc1shape", "2"},    
    {"osc1limitwt", "0"},    
    {"osc1fmsource", "0"},    
    {"osc1pwmsource", "1"},    
    {"osc1octave", "4"},    
    {"osc1semitone", "64"},    
    {"osc1detune", "64"},    
    {"osc1bendrange", "66"},    
    {"osc1keytrack", "96"},    
    {"osc1fmamount", "0"},    
    {"osc1pulsewidth", "127"},    
    {"osc1pwmamount", "64"},    
    {"osc1brilliance", "0"},    
    {"osc1level", "127"},    
    {"osc1balance", "0"},    
    {"osc2shape", "0"},    
    {"osc2limitwt", "0"},    
    {"osc2synctoosc3", "0"},    
    {"osc2fmsource", "0"},    
    {"osc2pwmsource", "3"},    
    {"osc2octave", "4"},    
    {"osc2semitone", "64"},    
    {"osc2detune", "64"},    
    {"osc2bendrange", "66"},    
    {"osc2keytrack", "96"},    
    {"osc2fmamount", "0"},    
    {"osc2pulsewidth", "127"},    
    {"osc2pwmamount", "64"},    
    {"osc2brilliance", "0"},    
    {"osc2level", "127"},    
    {"osc2balance", "0"},    
    {"osc3shape", "0"},    
    {"osc3fmsource", "0"},    
    {"osc3pwmsource", "5"},    
    {"osc3octave", "3"},    
    {"osc3semitone", "64"},    
    {"osc3detune", "64"},    
    {"osc3bendrange", "66"},    
    {"osc3keytrack", "96"},    
    {"osc3fmamount", "0"},    
    {"osc3pulsewidth", "127"},    
    {"osc3pwmamount", "64"},    
    {"osc3brilliance", "0"},    
    {"osc3level", "127"},    
    {"osc3balance", "0"},    
    {"filter1type", "1"},    
    {"filter1drivecurve", "0"},    
    {"filter1modsource", "1"},    
    {"filter1pansource", "1"},    
    {"filter1fmsource", "0"},    
    {"filter1cutoff", "127"},    
    {"filter1resonance", "0"},    
    {"filter1drive", "0"},    
    {"filter1keytrack", "64"},    
    {"filter1envamount", "64"},    
    {"filter1envvelocity", "64"},    
    {"filter1modamount", "64"},    
    {"filter1fmamount", "0"},    
    {"filter1pan", "64"},    
    {"filter1panamount", "64"},    
    {"filter2type", "0"},    
    {"filter2drivecurve", "0"},    
    {"filter2modsource", "0"},    
    {"filter2pansource", "3"},    
    {"filter2fmsource", "0"},    
    {"filterrouting", "0"},    
    {"filter2cutoff", "127"},    
    {"filter2resonance", "0"},    
    {"filter2drive", "0"},    
    {"filter2keytrack", "64"},    
    {"filter2envamount", "64"},    
    {"filter2envvelocity", "64"},    
    {"filter2modamount", "64"},    
    {"filter2fmamount", "0"},    
    {"filter2pan", "64"},    
    {"filter2panamount", "64"},    
    {"lfo1shape", "0"},    
    {"lfo1sync", "0"},    
    {"lfo1clocked", "0"},    
    {"lfo1speed", "50"},    
    {"lfo1startphase", "0"},    
    {"lfo1delay", "0"},    
    {"lfo1fade", "64"},    
    {"lfo1keytrack", "64"},    
    {"lfo3shape", "0"},    
    {"lfo3sync", "0"},    
    {"lfo3clocked", "0"},    
    {"lfo3speed", "30"},    
    {"lfo3startphase", "0"},    
    {"lfo3delay", "0"},    
    {"lfo3fade", "64"},    
    {"lfo3keytrack", "64"},    
    {"lfo2shape", "0"},    
    {"lfo2sync", "0"},    
    {"lfo2clocked", "0"},    
    {"lfo2speed", "40"},    
    {"lfo2startphase", "0"},    
    {"lfo2delay", "0"},    
    {"lfo2fade", "64"},    
    {"lfo2keytrack", "64"},    
    {"amplifiermodsource", "5"},    
    {"amplifiervolume", "127"},    
    {"amplifiervelocity", "114"},    
    {"amplifiermodamount", "64"},    
    {"envelope1mode", "0"},    
    {"envelope1trigger", "0"},    
    {"envelope1attack", "0"},    
    {"envelope1attacklevel", "127"},    
    {"envelope1decay", "50"},    
    {"envelope1sustain", "0"},    
    {"envelope1decay2", "0"},    
    {"envelope1sustain2", "127"},    
    {"envelope1release", "0"},    
    {"envelope2mode", "0"},    
    {"envelope2trigger", "0"},    
    {"envelope2attack", "0"},    
    {"envelope2attacklevel", "127"},    
    {"envelope2decay", "52"},    
    {"envelope2sustain", "127"},    
    {"envelope2decay2", "0"},    
    {"envelope2sustain2", "127"},    
    {"envelope2release", "0"},    
    {"envelope3mode", "0"},    
    {"envelope3trigger", "0"},    
    {"envelope3attack", "0"},    
    {"envelope3attacklevel", "64"},    
    {"envelope3decay", "64"},    
    {"envelope3sustain", "64"},    
    {"envelope3decay2", "64"},    
    {"envelope3sustain2", "64"},    
    {"envelope3release", "64"},    
    {"envelope4mode", "0"},    
    {"envelope4trigger", "0"},    
    {"envelope4attack", "0"},    
    {"envelope4attacklevel", "64"},    
    {"envelope4decay", "64"},    
    {"envelope4sustain", "64"},    
    {"envelope4decay2", "64"},    
    {"envelope4sustain2", "64"},    
    {"envelope4release", "64"},    
    {"modulation1source", "1"},    
    {"modulation1destination", "1"},    
    {"modulation1amount", "64"},    
    {"modulation2source", "0"},    
    {"modulation2destination", "0"},    
    {"modulation2amount", "64"},    
    {"modulation3source", "0"},    
    {"modulation3destination", "0"},    
    {"modulation3amount", "64"},    
    {"modulation4source", "0"},    
    {"modulation4destination", "0"},    
    {"modulation4amount", "64"},    
    {"modulation5source", "0"},    
    {"modulation5destination", "0"},    
    {"modulation5amount", "64"},    
    {"modulation6source", "0"},    
    {"modulation6destination", "0"},    
    {"modulation6amount", "64"},    
    {"modulation7source", "0"},    
    {"modulation7destination", "0"},    
    {"modulation7amount", "64"},    
    {"modulation8source", "0"},    
    {"modulation8destination", "0"},    
    {"modulation8amount", "64"},    
    {"modulation9source", "0"},    
    {"modulation9destination", "0"},    
    {"modulation9amount", "64"},    
    {"modulation10source", "0"},    
    {"modulation10destination", "0"},    
    {"modulation10amount", "64"},    
    {"modulation11source", "0"},    
    {"modulation11destination", "0"},    
    {"modulation11amount", "64"},    
    {"modulation12source", "0"},    
    {"modulation12destination", "0"},    
    {"modulation12amount", "64"},    
    {"modulation13source", "0"},    
    {"modulation13destination", "0"},    
    {"modulation13amount", "64"},    
    {"modulation14source", "0"},    
    {"modulation14destination", "0"},    
    {"modulation14amount", "64"},    
    {"modulation15source", "0"},    
    {"modulation15destination", "0"},    
    {"modulation15amount", "64"},    
    {"modulation16source", "0"},    
    {"modulation16destination", "0"},    
    {"modulation16amount", "64"},    
    {"modifier1sourcea", "0"},    
    {"modifier1sourceb", "0"},    
    {"modifier1operation", "0"},    
    {"modifier1constant", "64"},    
    {"modifier2sourcea", "0"},    
    {"modifier2sourceb", "0"},    
    {"modifier2operation", "0"},    
    {"modifier2constant", "64"},    
    {"modifier3sourcea", "0"},    
    {"modifier3sourceb", "0"},    
    {"modifier3operation", "0"},    
    {"modifier3constant", "64"},    
    {"modifier4sourcea", "0"},    
    {"modifier4sourceb", "0"},    
    {"modifier4operation", "0"},    
    {"modifier4constant", "64"},    
    {"effect1parameter0", "20"},    
    {"effect1parameter1", "64"},    
    {"effect1parameter2", "64"},    
    {"effect1parameter3", "0"},    
    {"effect1parameter4", "127"},    
    {"effect1parameter5", "127"},    
    {"effect1parameter6", "127"},    
    {"effect1parameter7", "127"},    
    {"effect1parameter8", "127"},    
    {"effect1parameter9", "127"},    
    {"effect1parameter10", "29"},    
    {"effect1type", "1"},    
    {"effect1mix", "0"},    
    {"effect2parameter0", "53"},    
    {"effect2parameter1", "64"},    
    {"effect2parameter2", "100"},    
    {"effect2parameter3", "0"},    
    {"effect2parameter4", "64"},    
    {"effect2parameter5", "100"},    
    {"effect2parameter6", "0"},    
    {"effect2parameter7", "100"},    
    {"effect2parameter8", "110"},    
    {"effect2parameter9", "0"},    
    {"effect2parameter10", "15"},    
    {"effect2type", "8"},    
    {"effect2mix", "0"},    
    {"arpeggiatormode", "0"},    
    {"arpeggiatordirection", "0"},    
    {"arpeggiatorsortorder", "0"},    
    {"arpeggiatorvelocitymode", "1"},    
    {"arpeggiatorpatternreset", "0"},    
    {"arpeggiatorpatternlength", "15"},    
    {"arpeggiatorpattern", "0"},    
    {"arpeggiatorclock", "8"},    
    {"arpeggiatorlength", "5"},    
    {"arpeggiatoroctave", "0"},    
    {"arpeggiatortimingfactor", "12"},    
    {"arpeggiatortempo", "55"},    
    {"arp01step", "0"},    
    {"arp01glide", "0"},    
    {"arp01accent", "4"},    
    {"arp01length", "4"},    
    {"arp01timing", "4"},    
    {"arp02step", "0"},    
    {"arp02glide", "0"},    
    {"arp02accent", "4"},    
    {"arp02length", "4"},    
    {"arp02timing", "4"},    
    {"arp03step", "0"},    
    {"arp03glide", "0"},    
    {"arp03accent", "4"},    
    {"arp03length", "4"},    
    {"arp03timing", "4"},    
    {"arp04step", "0"},    
    {"arp04glide", "0"},    
    {"arp04accent", "4"},    
    {"arp04length", "4"},    
    {"arp04timing", "4"},    
    {"arp05step", "0"},    
    {"arp05glide", "0"},    
    {"arp05accent", "4"},    
    {"arp05length", "4"},    
    {"arp05timing", "4"},    
    {"arp06step", "0"},    
    {"arp06glide", "0"},    
    {"arp06accent", "4"},    
    {"arp06length", "4"},    
    {"arp06timing", "4"},    
    {"arp07step", "0"},    
    {"arp07glide", "0"},    
    {"arp07accent", "4"},    
    {"arp07length", "4"},    
    {"arp07timing", "4"},    
    {"arp08step", "0"},    
    {"arp08glide", "0"},    
    {"arp08accent", "4"},    
    {"arp08length", "4"},    
    {"arp08timing", "4"},    
    {"arp09step", "0"},    
    {"arp09glide", "0"},    
    {"arp09accent", "4"},    
    {"arp09length", "4"},    
    {"arp09timing", "4"},    
    {"arp10step", "0"},    
    {"arp10glide", "0"},    
    {"arp10accent", "4"},    
    {"arp10length", "4"},    
    {"arp10timing", "4"},    
    {"arp11step", "0"},    
    {"arp11glide", "0"},    
    {"arp11accent", "4"},    
    {"arp11length", "4"},    
    {"arp11timing", "4"},    
    {"arp12step", "0"},    
    {"arp12glide", "0"},    
    {"arp12accent", "4"},    
    {"arp12length", "4"},    
    {"arp12timing", "4"},    
    {"arp13step", "0"},    
    {"arp13glide", "0"},    
    {"arp13accent", "4"},    
    {"arp13length", "4"},    
    {"arp13timing", "4"},    
    {"arp14step", "0"},    
    {"arp14glide", "0"},    
    {"arp14accent", "4"},    
    {"arp14length", "4"},    
    {"arp14timing", "4"},    
    {"arp15step", "0"},    
    {"arp15glide", "0"},    
    {"arp15accent", "4"},    
    {"arp15length", "4"},    
    {"arp15timing", "4"},    
    {"arp16step", "0"},    
    {"arp16glide", "0"},    
    {"arp16accent", "4"},    
    {"arp16length", "4"},    
    {"arp16timing", "4"},    
    {"name", "Init            "},    
    {"oscpitchsource", "2"},    
    {"oscpitchamount", "64"},    
    {"effect1parameter11", "127"},    
    {"effect1parameter12", "127"},    
    {"effect1parameter13", "127"},    
    {"effect2parameter11", "64"},    
    {"effect2parameter12", "127"},    
    {"effect2parameter13", "127"},
    };
                
    }
