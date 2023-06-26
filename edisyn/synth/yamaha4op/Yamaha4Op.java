/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.yamaha4op;

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
   A patch editor for several synthesizers in the Yamaha 4-Operator FM family.
   
   <p>Synthesizers supported: DX21, DX27, DX100, TX81Z, DX11, TQ5, YS100, YS200, B200
   <p>Synthesizers partially-supported: V50
   <p>Synthesizers <i>not</i> supported: DX9, FB01              (there are other patch editors in Edisyn for them)
   <p>Synthesizers which <i>might</i> work: WT11, DS55

   <p>Yamaha's 4-Op family have many parameters in common and later synthesizers have sysex
   protocols which are roughly backward-compatible with earlier ones, so we can support
   all of them here.  The most complex issue is that different synthesizers have (for unknown reasons)
   different commands for requesting patch dumps.  Another complicating factor: the various
   synthesizers have patches organized in banks of different size, number, and availability.
        
   <p>To maintain backward compatibility, the 4-Op family doesn't send a dump of a patch as a
   single sysex command but in fact up to four separate commands.  This complicates matters for
   Edisyn considerably when it attempts to do patch merging and loading.  Thankfully in general
   these commands come in a rational and predictable order, with the newer commands arriving
   first and the oldest-defined commands arriving last, always culminating in the "VCED" sysex
   command, so we can recognize when a patch has completed arrival.
        
   <p>The five sysex commands are:
   <ul>
   <li><b>VCED</b> is the original sysex dump command, containing the primary collection of
   voice parameters for the family.  The DX21, DX27, and DX100 only have this command.
   <li><b>ACED</b> is contains quite a lot of additional voice parameters.  The TX81Z uses ACED and VCED.
   <li><b>ACED2</b> is contains a few more voice parameters.  The DX11 uses ACED2, ACED, and VCED.
   <li><b>EFEDS</b> contains a few effects parameters.  The TQ5, YS100, YS200, and B200 use
   EFEDS, ACED2, ACED, and VCED.
   <li><b>ACED3</b> contains a few different effects parameters.  The V50 uses ACED3, ACED2, ACED, and VCED.
   </ul>
        
   <p>Thus commands arrive in the following possible strings, depending on the synthesizer:
   <ul>
   <li>VCED
   <li>ACED, VCED
   <li>ACED2, ACED, VCED
   <li>EFEDS, ACED2, ACED, VCED
   <li>ACED3, ACED2, ACED, VCED
   </ul>
                
   <p>Within a command, certain parameters are supported by certain synthesizers.  Notably, only
   certain parameters in VCED are supported by various synths.
        
   <p>To parse a patch, Edisyn requests the patch, then updates it piecemeal as various sysex commands
   arrive.  When VCED finally arrives, Edisyn then declares the patch completed (for merging purposes
   for example).  When emitting a patch, Edisyn always emits all four sysex commands in the order
   EFEDS, ACED2, ACED, and then VCED.

   <p>In addition to these commands, all 4-op synthesizers supported by Edisyn also provide VMEM,
   a special 4104-byte long sysex command which stores an entire bank of 32 patches.  VMEM is the
   same across all synthesizers, as it encompasses all the data in EFEDS, ACED2, ACED, and VCED,
   whether or not the synthesizer supports all of their features.  Edisyn can parse incoming VMEM
   as a bulk patch bank sysex command.  Edisyn does not emit VMEM.
        
   <p>Two synthesizers in this family (TX81Z and DX11) also support multitimbral facilities with
   up to 8 different sounds.  These are handled by a different sysex command called PCED.  Edisyn
   supports PCED in a different patch editor.  Four other synthesizers (TQ5, YS100, YS200, B200)
   are also 8-way multitimbral but via a different sysex command, and this is currently not supported,
   as their multitimbral feature is not really a patch mechanism so much as a global setting. 
        
   <p>Because these synthesizers have (unfortunately) different bank memory structures and
   (really unfortunately) different dump request commands and (really really unfortunately)
   different patch-change commands, we are forced to have the user specify which synthesizer
   he is emitting to.  These are the various synthesizer TYPES as shown below.  Were it not
   for these situations, we'd be able to have a truly unified patch editor.  Oh well!
   
   @author Sean Luke
*/

public class Yamaha4Op extends Synth
    {
    /// Various collections of parameter names for pop-up menus
        
    public static final int TYPE_DX21 = 0;
    public static final int TYPE_DX27_DX100 = 1;
    public static final int TYPE_TX81Z = 2;
    public static final int TYPE_DX11 = 3;
    public static final int TYPE_TQ5_YS100_YS200_B200 = 4;
    public static final int TYPE_V50 = 5;
    public static final String[] TYPES = { "DX21", "DX27, DX100", "TX81Z", "DX11", "TQ5, YSx00, B200 (>)", "V50" };
    public static final String[] BANKS = { "I", "A", "B", "C", "D" };
    public static final String[] TQ5_BANKS = { "Preset", "User", "Card" };
    public static final String[] V50_BANKS = { "Internal", "Card", "Preset" };
    public static final String[] WAVES = {"W1", "W2", "W3", "W4", "W5", "W6", "W7", "W8"};
    public static final ImageIcon[] WAVE_ICONS = 
        {
        new ImageIcon(Yamaha4Op.class.getResource("Wave1.png")),
        new ImageIcon(Yamaha4Op.class.getResource("Wave2.png")),
        new ImageIcon(Yamaha4Op.class.getResource("Wave3.png")),
        new ImageIcon(Yamaha4Op.class.getResource("Wave4.png")),
        new ImageIcon(Yamaha4Op.class.getResource("Wave5.png")),
        new ImageIcon(Yamaha4Op.class.getResource("Wave6.png")),
        new ImageIcon(Yamaha4Op.class.getResource("Wave7.png")),
        new ImageIcon(Yamaha4Op.class.getResource("Wave8.png"))
        };
    public static final ImageIcon[] ALGORITHM_ICONS = 
        {
        new ImageIcon(Yamaha4Op.class.getResource("Algorithm1.png")),
        new ImageIcon(Yamaha4Op.class.getResource("Algorithm2.png")),
        new ImageIcon(Yamaha4Op.class.getResource("Algorithm3.png")),
        new ImageIcon(Yamaha4Op.class.getResource("Algorithm4.png")),
        new ImageIcon(Yamaha4Op.class.getResource("Algorithm5.png")),
        new ImageIcon(Yamaha4Op.class.getResource("Algorithm6.png")),
        new ImageIcon(Yamaha4Op.class.getResource("Algorithm7.png")),
        new ImageIcon(Yamaha4Op.class.getResource("Algorithm8.png"))
        };
    public static final String[] LFO_WAVES = { "Sawtooth", "Square", "Triangle", "Sample & Hold" };
    public static final String[] SHIFTS = { "96dB", "48dB", "24dB", "12dB" };
    public static final String[] FIX_HI_RANGES = { "255Hz", "510Hz", "1KHz", "2KHz", "4KHz", "8KHz", "16KHz", "32KHz" };
    public static final String[] FIX_LO_RANGES = { "1Hz", "2Hz", "4Hz", "7Hz", "14Hz", "25Hz", "50Hz", "100Hz" };               // not sure if it's 25Hz
    public static final int[] FIX_HI_RANGE_VALS = { 8, 16, 32, 64, 128, 256, 512, 1024 };
    public static final double[] FREQUENCY_RATIOS = { 0.50, 0.71, 0.78, 0.87, 1.00, 1.41, 1.57, 1.73, 2.00, 2.82, 3.00, 3.14, 3.46, 4.00, 4.24, 4.71, 5.00, 5.19, 5.65, 6.00, 6.28, 6.92, 7.00, 7.07, 7.85, 8.00, 8.48, 8.65, 9.00, 9.42, 9.89, 10.00, 10.38, 10.99, 11.00, 11.30, 12.00, 12.11, 12.56, 12.72, 13.00, 13.84, 14.00, 14.10, 14.13, 15.00, 15.55, 15.57, 15.70, 16.96, 17.27, 17.30, 18.37, 18.84, 19.03, 19.78, 20.41, 20.76, 21.20, 21.98, 22.49, 23.55, 24.22, 25.95 };
    public static final double[] FREQUENCY_RATIOS_MAX = { 0.93, 1.32, 1.37, 1.62, 1.93, 2.73, 3.04, 3.35, 2.93, 4.14, 3.93, 4.61, 5.08, 4.93, 5.55, 6.18, 5.93, 6.81, 6.96, 6.93, 7.75, 8.54, 7.93, 8.37, 9.32, 8.93, 9.78, 10.27, 9.93, 10.89, 11.19, 10.93, 12.00, 12.46, 11.93, 12.60, 12.93, 13.73, 14.03, 14.01, 13.93, 15.46, 14.93, 15.42, 15.60, 15.93, 16.83, 17.19, 17.17, 18.24, 18.74, 18.92, 19.65, 20.31, 20.65, 21.06, 21.88, 22.38, 22.47, 23.45, 24.11, 25.02, 25.84, 27.57 };
    public static final double[] FREQUENCY_RATIO_NEAREST_INTS = { 0.50, 0.50, 1.00, 1.00, 1.00, 1.00, 2.00, 2.00, 2.00, 3.00, 3.00, 3.00, 3.00, 4.00, 4.00, 5.00, 5.00, 5.00, 6.00, 6.00, 6.00, 7.00, 7.00, 7.00, 8.00, 8.00, 8.00, 9.00, 9.00, 9.00, 10.00, 10.00, 10.00, 11.00, 11.00, 11.00, 12.00, 12.00, 13.00, 13.00, 13.00, 14.00, 14.00, 14.00, 14.00, 15.00, 15.00, 15.00, 15.00, 15.00, 15.00, 15.00, 15.00, 15.00, 15.00, 15.00, 15.00, 15.00, 15.00, 15.00, 15.00, 15.00, 15.00, 15.00 };
    
    public static final String[] KS_CURVES = { "Linear", "Exponential", "Logarithmic", "Ramped", "Spit", "Triangle", "Late", "Early" };
    public static final String[] VELOCITY_CURVES = { "Linear", "Logarithmic", "Exponential", "Exponential Strong", "Linear Then Off", "Off Then Linear", "Slow Middle", "Fast Middle" };
    public static final String[] KEYS = new String[] { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
    public static final String[] LFO_SHAPES = new String[] { "Triangle", "Sawtooth", "Square", "Random" };
    public static final String[] SOURCE_MODES = new String[] { "Normal", "Twin", "Double" };
    public static final String[] POLY_MODES = new String[] { "Poly 1", "Poly 2", "Solo 1", "Solo 2" };
    public static final String[] OUT_SELECTS = new String[] { "A", "B", "C", "D", "E", "F", "G", "H" };
    public static final String[] WHEEL_ASSIGNMENTS = new String[] { "Vibrato", "LFO", "Filter" };
    public static final String[] EFFECTS = new String[] { "Off", "Reverb - Hall", "Reverb - Room", "Reverb - Plate", "Delay", "Delay - Left/Right", "Stereo Echo", "Distortion + Reverb", "Distortion + Echo", "Gated Reverb", "Reverse Gate" };

    public static final String[] V50_EFFECTS = new String[] 
    { 
    "Reverb Hall", "Reverb Room", "Reverb Plate", "Delay", "Delay L/R", "Stereo Echo", "Distortion Reverb", 
    "Distortion Echo", "Gate Reverb", "Reverse Gate", "Early Reflections", "Tone Control 1", "Delay and Reverb", 
    "Delay L/R and Reverb", "Distortion Delay", "Church", "Club", "Stage", "Bath Room", "Metal", "Tunnel", 
    "Doubler 1", "Doubler 2", "Feed Back Gate", "Feed Back Reverse", "Feed Back E/R", "Delay and Tone Control 1", 
    "Delay L/R and Tone Control 1", "Tone Control 2", "Delay and Tone Control 2", "Delay L/R and Tone Control 2", 
    "Distortion", 
    };
 
 
    public static final String[][] V50_EFFECTS_PARAMETERS = new String[][]
    {
    { "Time", "LPF", "Delay", },                    // Reverb Hall
    { "Time", "LPF", "Delay", },                    // Reverb Room
    { "Time", "LPF", "Delay", },                    // Reverb Plate
    { "Time", "FB Delay", "FB Gain", },                     // Delay
    { "Lch Dly", "Rch Dly", "FB Gain", },                   // Delay L/R
    { "Lch Dly", "Rch Dly", "FB Gain", },                   // Stereo Echo
    { "Time", "Dist.", "Reverb", },                 // Distortion Reverb
    { "Time", "FB Gain", "Dist.", },                        // Distortion Echo
    { "Size", "LPF", "Delay", },                    // Gate Reverb
    { "Size", "LPF", "Delay", },                    // Reverse Gate
    { "Size", "LPF", "Delay", },                    // Early Reflections
    { "Low", "Middle", "High", },                   // Tone Control 1
    { "RevTime", "Delay", "FB Gain", },                     // Delay and Reverb
    { "RevTime", "Lch Dly", "Rch Dly", },                   // Delay L/R and Reverb
    { "Time", "FB Gain", "Dist.", },                        // Distortion Delay
    { "Time", "LPF", "Delay", },                    // Church
    { "Time", "LPF", "Delay", },                    // Club
    { "Time", "LPF", "Delay", },                    // Stage
    { "Time", "LPF", "Delay", },                    // Bath Room
    { "Time", "LPF", "Delay", },                    // Metal
    { "RevTime", "Delay", "FB Gain", },                     // Tunnel
    { "DlyTime", "HPF", "LPF", },                   // Doubler 1
    { "Lch Dly", "Rch Dly", "LPF", },                       // Doubler 2
    { "Size", "LPF", "FB Gain", },                  // Feed Back Gate
    { "Size", "LPF", "FB Gain", },                  // Feed Back Reverse
    { "Size", "LPF", "FB Gain", },                  // Feed Back E/R
    { "Bri.", "Delay", "FB Gain", },                        // Delay and Tone Control 1
    { "Bri.", "Delay", "FB Gain", },                        // Delay L/R and Tone Control 1
    { "HPF", "Middle", "LPF", },                    // Tone Control 2
    { "Bri.", "Delay", "FB Gain", },                        // Delay and Tone Control 2
    { "Bri.", "Delay", "FB Gain", },                        // Delay L/R and Tone Control 2
    { "Dist.", "HPF", "LPF" }                       // Distortion
    };
 
 
 

    public static final String TYPE_KEY = "type";
    int synthType = TYPE_TX81Z;
    JComboBox synthTypeCombo;
        
    public int getSynthType() { return synthType; }
    public void setSynthType(int val, boolean save)
        {
        if (save)
            {
            setLastX("" + val, TYPE_KEY, getSynthClassName(), true);
            }
        synthType = val;
        synthTypeCombo.setSelectedIndex(val);  // hopefully this isn't recursive
        updateTitle();
        }

    public Yamaha4Op()
        {
        String m = getLastX(TYPE_KEY, getSynthClassName());
        try
            {
            synthType = (m == null ? TYPE_TX81Z : Integer.parseInt(m));
            if (synthType < TYPE_DX21 || synthType > TYPE_V50)
                {
                synthType = TYPE_TX81Z;
                }
            }
        catch (NumberFormatException ex)
            {
            synthType = TYPE_TX81Z;
            }
        
        model.set("bank", 0);
        model.set("number", 0);
                
        if (vcedParametersToIndex == null)
            {
            vcedParametersToIndex = new HashMap();
            for(int i = 0; i < vcedParameters.length; i++)
                {
                vcedParametersToIndex.put(vcedParameters[i], Integer.valueOf(i));
                }
                
            acedParametersToIndex = new HashMap();
            for(int i = 0; i < acedParameters.length; i++)
                {
                acedParametersToIndex.put(acedParameters[i], Integer.valueOf(i));
                }
                
            aced2ParametersToIndex = new HashMap();
            for(int i = 0; i < aced2Parameters.length; i++)
                {
                aced2ParametersToIndex.put(aced2Parameters[i], Integer.valueOf(i));
                }
                
            aced3ParametersToIndex = new HashMap();
            for(int i = 0; i < aced3Parameters.length; i++)
                {
                aced3ParametersToIndex.put(aced3Parameters[i], Integer.valueOf(i));
                }
                
            efedsParametersToIndex = new HashMap();
            for(int i = 0; i < efedsParameters.length; i++)
                {
                efedsParametersToIndex.put(efedsParameters[i], Integer.valueOf(i));
                }
            }
                
        /// SOUND PANEL
                
        JComponent soundPanel = new SynthPanel(this);
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        hbox.addLast(addGlobal(Style.COLOR_A()));
        vbox.add(hbox);
        
        vbox.add(addLFO(Style.COLOR_B()));
        
        vbox.add(addModulation(Style.COLOR_C()));
        
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Global", soundPanel);
                

        JComponent sourcePanel = new SynthPanel(this);
        vbox = new VBox();
        
        vbox.add(addOperator(1, Style.COLOR_A()));
        vbox.add(addEnvelope(1, Style.COLOR_B()));

        vbox.add(addOperator(2, Style.COLOR_A()));
        vbox.add(addEnvelope(2, Style.COLOR_B()));

        sourcePanel.add(vbox, BorderLayout.CENTER);
        addTab("Operators 1-2", sourcePanel);

        sourcePanel = new SynthPanel(this);
        vbox = new VBox();
        
        vbox.add(addOperator(3, Style.COLOR_A()));
        vbox.add(addEnvelope(3, Style.COLOR_B()));

        vbox.add(addOperator(4, Style.COLOR_A()));
        vbox.add(addEnvelope(4, Style.COLOR_B()));

        sourcePanel.add(vbox, BorderLayout.CENTER);
        addTab("Operators 3-4", sourcePanel);
        
        sourcePanel = new SynthPanel(this);
        vbox = new VBox();
        vbox.add(addPitchEnvelope(Style.COLOR_A()));
        vbox.add(addEffects(Style.COLOR_B()));
        vbox.add(addV50Effects(Style.COLOR_A()));
        sourcePanel.add(vbox, BorderLayout.CENTER);
        addTab("Other", sourcePanel);
        
        model.set("name", "INIT VOICE");
        
        // unused parameters still need min/max
        model.setMin("sustain", 0);
        model.setMax("sustain", 1);
        model.setMin("chorus", 0);
        model.setMax("chorus", 1);
        model.setMin("portamento", 0);
        model.setMax("portamento", 1);

        loadDefaults();        
        }
                
    public JFrame sprout()
        {
        JFrame frame = super.sprout();
        writeTo.setEnabled(false);
        addYamaha4OpMenu();
        return frame;
        }         

    public String getDefaultResourceFileName() { return "Yamaha4Op.init"; }
    public String getHTMLResourceFileName() { return "Yamaha4Op.html"; }


    // DX21 Change Patch                [Though there are banks, we can only access the first one, just 32 values]
    // DX27/100 Dump Request    [We can access voices 0...23 in banks I, A, B, C, D, but only write to I]
    // DX11 Dump Request                [We can access voices 0...31 in banks I, A, B, C, D, but only write to I]
    // TX81Z Dump Request               [We can access voices 0...32 in banks I, A, B, C, D, but only write to I]
    // TQ5 Dump Request                 [We can access voices 0...99.  There are three BANKS, Internal, Preset, and Card.]

    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        int type = getSynthType();
        JComboBox bank = null;
        
        if (!writing && (type == TYPE_TX81Z || type == TYPE_DX11 || type == TYPE_DX27_DX100))
            {
            bank = new JComboBox(BANKS);
            bank.setSelectedIndex(model.get("bank"));
            }
        else if (!writing && (type == TYPE_TQ5_YS100_YS200_B200))
            {
            bank = new JComboBox(TQ5_BANKS);
            if (model.get("bank") > 3)
                {
                System.err.println("Warning (Yamaha4Op): bank is invalid (" + bank + "), changing to 0");
                bank.setSelectedIndex(0);
                }
            else
                bank.setSelectedIndex(model.get("bank"));
            }
        else if (!writing && (type == TYPE_V50))
            {
            bank = new JComboBox(V50_BANKS);
            if (model.get("bank") > 3)
                {
                System.err.println("Warning (Yamaha4Op): bank is invalid (" + bank + "), changing to 0");
                bank.setSelectedIndex(0);
                }
            else
                bank.setSelectedIndex(model.get("bank"));
            }
        
        int maxNumber =
            (type == TYPE_DX21 ? 32 :
                (type == TYPE_DX27_DX100 ? 24 :
                    (type == TYPE_DX11 ? 32 :
                        (type == TYPE_TX81Z ? 32 :
                        100))));                // TQ5 and also V50
        
        int num = model.get("number");
        if (num < 0 || num >= maxNumber)
            num = 0;
        
        JTextField number = new SelectedTextField("" + 
            ((type == TYPE_TQ5_YS100_YS200_B200 || type == TYPE_V50) ? num : num + 1), 3);

        while(true)
            {
            boolean result = false;
            if (bank == null)
                result = showMultiOption(this, new String[] { "Patch Number" }, new JComponent[] { number }, title, "Enter the Patch number.");
            else
                result = showMultiOption(this, new String[] { "Bank", "Patch Number"}, new JComponent[] { bank, number }, title, "Enter the Bank and Patch number.");
                
            if (result == false) 
                return false;
                                
            int n;
            try { n = Integer.parseInt(number.getText()); }
            catch (NumberFormatException e)
                {
                if (type == TYPE_TQ5_YS100_YS200_B200 || type == TYPE_V50)
                    showSimpleError(title, "The Patch Number must be an integer 0...99");
                else
                    showSimpleError(title, "The Patch Number must be an integer 1..." + maxNumber);
                continue;
                }
            if (type == TYPE_TQ5_YS100_YS200_B200 || type == TYPE_V50)
                {
                if (n < 0 || n >= maxNumber)    // note >=
                    {
                    showSimpleError(title, "The Patch Number must be an integer 0...99");
                    continue;
                    }
                }
            else
                {
                if (n < 1 || n > maxNumber)             // note >
                    {
                    showSimpleError(title, "The Patch Number must be an integer 1..." + maxNumber);
                    continue;
                    }
                n--;
                }
                                
            int i = (bank == null ? 0 : bank.getSelectedIndex());
                        
            change.set("bank", i);
            change.set("number", n);
                        
            return true;
            }
        }

    public static final int OFF = 0;
    public static final int COARSE = 1;
    public static final int INTEGERS = 2;
        
    int mutationRestriction = OFF;
        
    public void addYamaha4OpMenu()
        {
        JMenu menu = new JMenu("4-Op FM");
        menubar.add(menu);

        JMenu restrictMutation = new JMenu("Restrict Mutated Frequency Ratios...");
        menu.add(restrictMutation);
                
        String str = getLastX("MutationRestriction", getSynthClassName(), true);
        if (str == null)
            mutationRestriction = OFF;
        else if (str.equalsIgnoreCase("COARSE"))
            mutationRestriction = COARSE;
        else if (str.equalsIgnoreCase("INTEGERS"))
            mutationRestriction = INTEGERS;
        else mutationRestriction = OFF;
                
        ButtonGroup bg = new ButtonGroup();
        JRadioButtonMenuItem off = new JRadioButtonMenuItem("Off");
        off.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt)
                {
                mutationRestriction = OFF;
                setLastX("OFF", "MutationRestriction", getSynthClassName(), true);
                }
            });
        restrictMutation.add(off);
        bg.add(off);
        if (mutationRestriction == OFF) off.setSelected(true);
                
        JRadioButtonMenuItem coarse = new JRadioButtonMenuItem("To Coarse Values Only");
        coarse.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt)
                {
                mutationRestriction = COARSE;
                setLastX("COARSE", "MutationRestriction", getSynthClassName(), true);
                }
            });
        restrictMutation.add(coarse);
        bg.add(coarse);
        if (mutationRestriction == COARSE) coarse.setSelected(true);

        JRadioButtonMenuItem integers = new JRadioButtonMenuItem("To Integer Coarse Values Only");
        integers.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt)
                {
                mutationRestriction = INTEGERS;
                setLastX("INTEGERS", "MutationRestriction", getSynthClassName(), true);
                }
            });
        restrictMutation.add(integers);
        bg.add(integers);
        if (mutationRestriction == INTEGERS) integers.setSelected(true);
        }
                

    public class SubModel extends Model
        {
        public int reviseMutatedValue(String key, int old, int current)
            {
            if (mutationRestriction == OFF)
                return current;
            else if (key.startsWith("operator") && key.endsWith("frequencyfine"))
                {
                return 0;
                }
            else if (key.startsWith("operator") && key.endsWith("frequencycoarse"))
                {
                if (mutationRestriction == COARSE)
                    return current;
                else if (mutationRestriction == INTEGERS)
                    {
                    double val = FREQUENCY_RATIO_NEAREST_INTS[current];
                    for(int i = 0; i < FREQUENCY_RATIOS.length; i++)
                        {
                        if (FREQUENCY_RATIOS[i] == val)  // got it
                            return i;
                        }
                    // never happens
                    return current;
                    }
                else  // never happens
                    return current;
                }
            else
                return current;
            }
        }
                
    public Model buildModel()
        {
        return new SubModel();
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
        final PatchDisplay pd = new PatchDisplay(this, 10);
        comp = pd;
        hbox2.add(comp);
        vbox.add(hbox2);

        JLabel label = new JLabel("Synth Type");
        label.setFont(Style.SMALL_FONT());
        label.setBackground(Style.BACKGROUND_COLOR()); // TRANSPARENT);
        label.setForeground(Style.TEXT_COLOR());

        synthTypeCombo = new JComboBox(TYPES);
        synthTypeCombo.setSelectedIndex(getSynthType());
        synthTypeCombo.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                setSynthType(synthTypeCombo.getSelectedIndex(), true);
                pd.update("bank", model);  // doesn't matter what the key is, so I put in "bank"
                }
            });
        synthTypeCombo.putClientProperty("JComponent.sizeVariant", "small");
        synthTypeCombo.setEditable(false);
        synthTypeCombo.setFont(Style.SMALL_FONT());
        VBox st = new VBox();
        st.add(label);
        st.addLast(synthTypeCombo);
        vbox.add(st);
        
        comp = new StringComponent("Patch Name", this, "name", 10, "Name must be up to 10 ASCII characters.")
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
        vbox.addBottom(comp);  // doesn't work right :-(
        hbox.add(vbox);

        // Not enough space to show the title
        hbox.addLast(Strut.makeHorizontalStrut(30));

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
        
        comp = new LabelledDial("Algorithm", this, "algorithm", color, 0, 7, -1);
        model.removeMetricMinMax("algorithm");  // it's a set
        vbox.add(comp);
                
        comp = new CheckBox("Mono", this, "mono");
        vbox.add(comp);

        hbox.add(vbox);
        
        hbox.add(Strut.makeHorizontalStrut(10));

        comp = new IconDisplay(null, ALGORITHM_ICONS, this, "algorithm", 104, 104);
        hbox.add(comp);

        hbox.add(Strut.makeHorizontalStrut(10));

        vbox = new VBox();

        HBox hbox2 = new HBox();
        comp = new LabelledDial("Feedback", this, "feedback", color, 0, 7);
        hbox2.add(comp);

        comp = new LabelledDial("Reverb Rate", this, "reverbrate", color, 0, 7);
        ((LabelledDial)comp).addAdditionalLabel(" [11,TX,>,V] ");
        hbox2.add(comp);

        comp = new LabelledDial("Transpose", this, "transpose", color, 0, 48, 24)
            {
            public boolean isSymmetric() { return true; }
            };
        hbox2.add(comp);
        
        comp = new LabelledDial("Pitch Bend", this, "pitchbendrange", color, 0, 12);
        ((LabelledDial)comp).addAdditionalLabel("Range");
        hbox2.add(comp);

        vbox.add(hbox2);
                
        vbox.add(Strut.makeVerticalStrut(10));
        
        hbox.add(vbox);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addPitchEnvelope(Color color)
        {
        Category category = new Category(this, "Pitch Envelope", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
  
        comp = new LabelledDial("Attack Rate", this, "pitchattackrate", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("PR1 [21,11,V]");
        hbox.add(comp);

        comp = new LabelledDial("Attack Level", this, "pitchattacklevel", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("PL1 [21,11,V]");
        hbox.add(comp);

        comp = new LabelledDial("Decay Rate", this, "pitchdecayrate", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("PR2 [21,11,V]");
        hbox.add(comp);

        comp = new LabelledDial("Decay Level", this, "pitchdecaylevel", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("PL2 [21,11,V]");
        hbox.add(comp);

        comp = new LabelledDial("Release Rate", this, "pitchreleaserate", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("PR3 [21,11,V] ");
        hbox.add(comp);
        
        comp = new LabelledDial("Release Level", this, "pitchreleaselevel", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("PL3 [21,11,V]");
        hbox.add(comp);
        
        // ADSR
        // This will *more or less* work, though the release rate will be slightly short
        // for reasons beyond me.
        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "pitchattackrate", "pitchdecayrate", null, "pitchreleaserate" },
            new String[] { "pitchreleaselevel", "pitchattacklevel", "pitchdecaylevel", "pitchdecaylevel", "pitchreleaselevel" },
            new double[] { 0, 1.0/3, 1.0/3, 1.0/4, 1.0/3 },
            new double[] { 1.0 / 99, 1.0 / 99, 1.0 / 99, 1.0/99 ,1.0 / 99},
            new double[] { 0, (Math.PI/4/99), (Math.PI/4/99), (Math.PI/4/99), (Math.PI/4/99) })
            {
            public double preprocessXKey(int index, String key, double value)
                {
                return 99.0 - value;
                }

            public void postProcess(double[] xVals, double[] yVals)
                {
                }
            };
            
        ((EnvelopeDisplay)comp).setAxis(0.5);

        hbox.addLast(comp);

        VBox vbox2 = new VBox();
        vbox2.add(hbox);
        vbox2.addLast(new HBox());
        
        category.add(vbox2, BorderLayout.CENTER);
        return category;
        }

    public JComponent addEffects(Color color)
        {
        Category category = new Category(this, "Effects", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
  
        params = EFFECTS;
        comp = new Chooser("Preset [>]", this, "effectpreset", params);
        vbox.add(comp);
                
        comp = new CheckBox("Chorus [21,27s]", this, "chorus");
        ((CheckBox)comp).addToWidth(3);
        vbox.add(comp);
        hbox.add(vbox);
                
        final JLabel[] lab = new JLabel[1];
        comp = new LabelledDial("     Time [>]     ", this, "effecttime", color, 0, 75)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                if (lab[0] != null) lab[0].setText(model.get(key) < 41 ? "                     " : " (Delay/Echo) ");
                }
            };
        lab[0] = ((LabelledDial)comp).addAdditionalLabel(" ");
        hbox.add(comp);

        comp = new LabelledDial("Balance [>]", this, "effectbalance", color, 0, 99);
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addV50Effects(Color color)
        {
        Category category = new Category(this, "V50 Effects", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
  
        final LabelledDial param1 = new LabelledDial("Parameter 1", this, "veffectparam1", color, 0, 75);

        final LabelledDial param2 = new LabelledDial("Parameter 2", this, "veffectparam2", color, 0, 99);

        final LabelledDial param3 = new LabelledDial("Parameter 3", this, "veffectparam3", color, 0, 99);

        params = V50_EFFECTS;
        comp = new Chooser("Effect [V]", this, "veffectsel", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                param1.setLabel(V50_EFFECTS_PARAMETERS[model.get(key)][0] + " [V]");
                param2.setLabel(V50_EFFECTS_PARAMETERS[model.get(key)][1] + " [V]");
                param3.setLabel(V50_EFFECTS_PARAMETERS[model.get(key)][2] + " [V]");
                }
            };
        vbox.add(comp);
                
        comp = new CheckBox("Stereo Mix [V]", this, "veffectstereomix");
        ((CheckBox)comp).addToWidth(3);
        vbox.add(comp);
        hbox.add(vbox);
                
        comp = new LabelledDial("Balance [V]", this, "veffectbalance", color, 0, 100, 50)
            {
            public boolean isSymmetric() { return true; }
            };
        hbox.add(comp);
        
        comp = new LabelledDial("Out Level [V]", this, "veffectoutlevel", color, 0, 100);
        hbox.add(comp);

        hbox.add(param1);
        hbox.add(param2);
        hbox.add(param3);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addLFO(Color color)
        {
        Category category = new Category(this, "LFO ", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = LFO_WAVES;
        comp = new Chooser("Wave", this, "lfowave", params);
        vbox.add(comp);

        comp = new CheckBox("Sync", this, "lfosync");
        vbox.add(comp);

        hbox.add(vbox);

        comp = new LabelledDial("Speed", this, "lfospeed", color, 0, 99);
        hbox.add(comp);
        
        comp = new LabelledDial("Delay", this, "lfodelay", color, 0, 99);
        hbox.add(comp);
        
        comp = new LabelledDial("Pitch Mod", this, "lfopitchmodulationsensitivity", color, 0, 7);
        ((LabelledDial)comp).addAdditionalLabel("Sensitivity");
        hbox.add(comp);
        
        comp = new LabelledDial("Pitch Mod", this, "lfopitchmodulationdepth", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("Depth");
        hbox.add(comp);
        
        comp = new LabelledDial("Amplitude Mod", this, "lfoamplitudemodulationsensitivity", color, 0, 3);
        ((LabelledDial)comp).addAdditionalLabel("Sensitivity");
        hbox.add(comp);
        
        comp = new LabelledDial("Amplitude Mod", this, "lfoamplitudemodulationdepth", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("Depth");
        hbox.add(comp);
        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
    
    public JComponent addModulation(Color color)
        {
        Category category = new Category(this, "Controllers", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        vbox = new VBox();
        comp = new CheckBox("Full Time Portamento", this, "fulltimeportamentomode", true);
        vbox.add(comp);

        comp = new CheckBox("Sustain [21,11]", this, "sustain");
        ((CheckBox)comp).addToWidth(3);
        vbox.add(comp);

        comp = new CheckBox("Portamento [21,11]", this, "portamento");
        ((CheckBox)comp).addToWidth(3);
        vbox.add(comp);

        hbox.add(vbox);

        vbox = new VBox();
        HBox hbox2 = new HBox();

        comp = new LabelledDial("Breath Ctrl.", this, "breathcontrolpitch", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("Pitch");
        hbox2.add(comp);
        
        comp = new LabelledDial("Breath Ctrl.", this, "breathcontrolamplitude", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("Amplitude");
        hbox2.add(comp);
        
        comp = new LabelledDial("Breath Ctrl.", this, "breathcontrolpitchbias", color, 0, 99, 50);
        ((LabelledDial)comp).addAdditionalLabel("Pitch Bias");
        hbox2.add(comp);
        
        comp = new LabelledDial("Breath Ctrl.", this, "breathcontrolenvelopebias", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("Env. Bias");
        hbox2.add(comp);

        comp = new LabelledDial("Mod Wheel", this, "modulationwheelpitch", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("Pitch");
        hbox2.add(comp);
        
        comp = new LabelledDial("Mod Wheel", this, "modulationwheelamplitude", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("Amplitude");
        hbox2.add(comp);
        
        comp = new LabelledDial("Portamento Time", this, "portamentotime", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("[21,27,100,11,TX,V]  ");
        hbox2.add(comp);
        
        vbox.add(hbox2);
        vbox.add(Strut.makeVerticalStrut(15));
        
        hbox2 = new HBox();
        
        comp = new LabelledDial("Aftertouch", this, "aftertouchpitch", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("Pitch");
        ((LabelledDial)comp).addAdditionalLabel(" [11,>,V] ");
        hbox2.add(comp);
        
        comp = new LabelledDial("Aftertouch", this, "aftertouchamplitude", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("Amplitude");
        ((LabelledDial)comp).addAdditionalLabel(" [11,>,V] ");
        hbox2.add(comp);
        
        comp = new LabelledDial("Aftertouch", this, "aftertouchpitchbias", color, 0, 99, 50);
        ((LabelledDial)comp).addAdditionalLabel("Pitch Bias");
        ((LabelledDial)comp).addAdditionalLabel(" [11,>,V] ");
        hbox2.add(comp);
        
        comp = new LabelledDial("Aftertouch", this, "aftertouchenvelopebias", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("Env. Bias");
        ((LabelledDial)comp).addAdditionalLabel(" [11,>,V] ");
        hbox2.add(comp);

        comp = new LabelledDial("Foot Ctrl.", this, "footcontrolvolume", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("Volume");
        ((LabelledDial)comp).addAdditionalLabel(" [21,11,TX,>,V] ");
        hbox2.add(comp);
        
        comp = new LabelledDial("Foot Ctrl.", this, "footcontrolpitch", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("Pitch");
        ((LabelledDial)comp).addAdditionalLabel(" [11,TX,>,V] ");
        hbox2.add(comp);
        
        comp = new LabelledDial("Foot Ctrl.", this, "footcontrolamplitude", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("Amplitude");
        ((LabelledDial)comp).addAdditionalLabel(" [11,TX,>,V] ");
        hbox2.add(comp);
        
        vbox.add(hbox2);

        hbox.add(vbox);        

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
    
    // From discussion with Matt Gregory (mgregory22@gmail.com),
    // who runs the TX81Z website http://the-all.org/tx81z/
    public double computeFineRatio(int coarse, int fine)
        {
        double min = FREQUENCY_RATIOS[coarse];
        double max = FREQUENCY_RATIOS_MAX[coarse];
          
        if (min < 1.0)
            {
            return Math.min(max, min + ((max - min) / 7.0) * fine);
            }
        else
            {
            return min + ((max - min) / 15.0) * fine;
            }
        }

    // based on http://cd.textfiles.com/fredfish/v1.6/FF_Disks/571-600/FF_598/TX81z/TX81z.doc
    // in combination with the manual.
    public double computeCoarseFrequency(int range, int coarse, int vshift)
        {
        // the others are just multiples of it
        int base = 1;
        for(int i = 0; i < range; i++)
            base *= 2;

        if (coarse < 4)
            return 8 * base / Math.pow(300, vshift);
        else 
            return 16 * (coarse / 4) * base / Math.pow(300, vshift);
        }

    // based on http://cd.textfiles.com/fredfish/v1.6/FF_Disks/571-600/FF_598/TX81z/TX81z.doc
    // in combination with the manual.
    public double computeFrequency(int range, int coarse, int fine, int vshift)
        {
        // the others are just multiples of it
        int base = 1;

        for(int i = 0; i < range; i++)
            base *= 2;

        if (coarse < 4)
            fine = Math.min(fine, 7);
      
        if (coarse < 4) 
            return (8 + fine) * base / Math.pow(300, vshift); // if V50 FIX RANGE MODE = LO divide by 300
        else 
            return (16 * (coarse / 4) + fine) * base / Math.pow(300, vshift);
        }

    LabelledDial[] frequencyRanges = new LabelledDial[4];
    JLabel[] coarseFrequencyLabels = new JLabel[4];
    JLabel[] fineFrequencyLabels = new JLabel[4];
    java.text.DecimalFormat format = new java.text.DecimalFormat("0.0#");

    public JComponent addOperator(final int src, Color color)
        {
        final Category category = new Category(this, "Operator " + src, color);
        //        category.makePasteable("operator" + src);
        category.makePasteable("operator");

        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = WAVES;
        comp = new Chooser("Wave [11,TX,>,V]", this, "operator" + src + "operatorwaveform", params, WAVE_ICONS);
        vbox.add(comp);
        
        HBox hbox1 = new HBox();
        comp = new CheckBox("AM", this, "operator" + src + "amplitudemodulationenable");
        hbox1.add(comp);
        comp = new CheckBox("Shift Lo [V]", this, "operator" + src + "vshift");
        ((CheckBox)comp).addToWidth(2);
        hbox1.add(comp);
        vbox.add(hbox1);

        hbox.add(vbox);

        comp = new LabelledDial("Env. Bias", this, "operator" + src + "egbiassensitivity", color, 0, 7);
        ((LabelledDial)comp).addAdditionalLabel("Sensitivity");
        hbox.add(comp);

        VBox vbox2 = new VBox();        
        comp = new LabelledDial("Key Velocity", this, "operator" + src + "keyvelocitysensitivity", color, 0, 7);
        ((LabelledDial)comp).addAdditionalLabel("Sensitivity");
        vbox2.add(comp);
        comp = new CheckBox("Neg [V]", this, "operator" + src + "vkeyvelocitysensitivitysign");
        ((CheckBox)comp).addToWidth(2);
        vbox2.add(comp);
        hbox.add(vbox2);
        
        comp = new LabelledDial("Output", this, "operator" + src + "outputlevel", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);

        comp = new LabelledDial("Detune", this, "operator" + src + "detune", color, 0, 6, 3)
            {
            public boolean isSymmetric() { return true; }
            };
        hbox.add(comp);


        // we define this first so that it populates the model for the other widgets  But we'll add it at the end.

        frequencyRanges[src - 1] = new LabelledDial("Frequency", this, "operator" + src + "fixedfrequencyrange", color, 0, 7)
            {
            public String map(int val)
                {
                if (model.get("operator" + src + "vshift") == 0)   
                    return FIX_HI_RANGES[val];
                else
                    return FIX_LO_RANGES[val]; // V50 fix range lo
                }               
            };
        frequencyRanges[src - 1].addAdditionalLabel("Range");



        // we define this next so that it populates the model for the other widgets  But we'll add it at the end.
    
        CheckBox fixcomp = new CheckBox("Fixed [11,TX,>,V]", this, "operator" + src + "fix")
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                if (model.get(key) == 0)
                    {
                    hbox.remove(frequencyRanges[src - 1]);
                    if (coarseFrequencyLabels[src - 1] != null)                         // won't be the case initially
                        coarseFrequencyLabels[src - 1].setText("Ratio");
                    if (fineFrequencyLabels[src - 1] != null)                           // won't be the case initially
                        fineFrequencyLabels[src - 1].setText("Ratio Fine");
                    }
                else
                    {
                    hbox.add(frequencyRanges[src - 1]);
                    if (coarseFrequencyLabels[src - 1] != null)                         // won't be the case initially
                        coarseFrequencyLabels[src - 1].setText("Fixed");
                    if (fineFrequencyLabels[src - 1] != null)                           // won't be the case initially
                        fineFrequencyLabels[src - 1].setText("Fixed Fine");
                    }
                hbox.revalidate();
                }
            };
        ((CheckBox)fixcomp).addToWidth(2);


        comp = new LabelledDial("Frequency ", this, "operator" + src + "frequencycoarse", color, 0, 63)  // extra space because OS X cuts off the 'y'
            {
            public String map(int val)
                {
                if (model.get("operator" + src + "fix") == 0)  // not fixed
                    {
                    return "" + FREQUENCY_RATIOS[val];
                    }
                else
                    {
                    if (model.get("operator" + src + "vshift") == 0)
                        {
                        return String.format("%.0f", computeCoarseFrequency(
                            model.get("operator" + src + "fixedfrequencyrange"),
                            model.get("operator" + src + "frequencycoarse"),
                            model.get("operator" + src + "vshift")));
                        }
                    else
                        {
                        return String.format("%.2f", computeCoarseFrequency(
                            model.get("operator" + src + "fixedfrequencyrange"),
                            model.get("operator" + src + "frequencycoarse"),
                            model.get("operator" + src + "vshift")));

                        }
                    }
                }               
            };
        coarseFrequencyLabels[src - 1 ] = ((LabelledDial)comp).addAdditionalLabel("Fixed");
        model.register("operator" + src + "fixedfrequencyrange", (Updatable)comp);
        model.register("operator" + src + "fix", (Updatable)comp);              
        hbox.add(comp);
        
        format.setRoundingMode(java.math.RoundingMode.FLOOR);
        comp = new LabelledDial("Frequency", this, "operator" + src + "frequencyfine", color, 0, 15)  // extra space because OS X cuts off the 'y'
            {
            public String map(int val)
                {
                if (model.get("operator" + src + "fix") == 0)  // not fixed
                    {
                    return format.format(computeFineRatio(
                            model.get("operator" + src + "frequencycoarse"), 
                            model.get("operator" + src + "frequencyfine")));
                    }
                else
                    {
                    String fixformat;
                    double computedFrequency = computeFrequency(
                           model.get("operator" + src + "fixedfrequencyrange"),
                           model.get("operator" + src + "frequencycoarse"),
                           model.get("operator" + src + "frequencyfine"),
                           model.get("operator" + src + "vshift"));

                    if (model.get("operator" + src + "vshift") == 1) //V50 FIX RANGE MODE (SHIFT LO)
                        {
                        if (computedFrequency < 100)
                            {
                                fixformat = "%.3f";
                            }
                        else
                            {
                                fixformat = "%.2f";
                            }
                        }
                    else
                        {
                            fixformat = "%.0f";
                        }
                    return String.format(fixformat, computedFrequency);
                    }
                }
            };               

        fineFrequencyLabels[src - 1] = ((LabelledDial)comp).addAdditionalLabel("Fixed Fine");
        model.register("operator" + src + "fixedfrequencyrange", (Updatable)comp);
        model.register("operator" + src + "frequencycoarse", (Updatable)comp);
        model.register("operator" + src + "fix", (Updatable)comp);
        model.register("operator" + src + "vshift", (Updatable)comp);
        hbox.add(comp);


        // now we add the frequency range
        hbox.add(frequencyRanges[src - 1]);

    
        // we put this last so that by the time it's updating, the fine frequency dials have been built
        vbox.add(fixcomp);
        // update its labels just in case
        fixcomp.update("operator" + src + "fix", model);

    
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
                
    public JComponent addEnvelope(final int envelope, Color color)
        {
        Category category = new Category(this, "Operator Envelope " + envelope, color);
        //        category.makePasteable("operator" + envelope);
        category.makePasteable("operator");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
               
        comp = new LabelledDial("Attack", this, "operator" + envelope + "attackrate", color, 0, 31);
        ((LabelledDial)comp).addAdditionalLabel("Rate (AR)");
        hbox.add(comp);

        comp = new LabelledDial("Decay 1", this, "operator" + envelope + "decay1rate", color, 0, 31);
        ((LabelledDial)comp).addAdditionalLabel("Rate (D1R)");
        hbox.add(comp);

        comp = new LabelledDial("Decay 1", this, "operator" + envelope + "decay1level", color, 0, 15);
        ((LabelledDial)comp).addAdditionalLabel("Level (D1L)");
        hbox.add(comp);

        comp = new LabelledDial("Decay 2", this, "operator" + envelope + "decay2rate", color, 0, 31)
            {
            public String map(int val)
                {
                if (val == 0) return "Sustain";
                else return "" + val;
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Rate (D2R)");
        model.setMetricMin("operator" + envelope + "decay2rate", 1);
        hbox.add(comp);

        comp = new LabelledDial("Release", this, "operator" + envelope + "releaserate", color, 1, 15);
        ((LabelledDial)comp).addAdditionalLabel("Rate (RR)");
        hbox.add(comp);
        
        VBox vbox2 = new VBox();
        comp = new LabelledDial("Level", this, "operator" + envelope + "levelscaling", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("Scaling");
        vbox2.add(comp);
        comp = new CheckBox("Neg [V]", this, "operator" + envelope + "vlevelscalingsign");
        ((CheckBox)comp).addToWidth(2);
        vbox2.add(comp);
        hbox.add(vbox2);
        
        comp = new LabelledDial("Rate", this, "operator" + envelope + "ratescaling", color, 0, 3);
        ((LabelledDial)comp).addAdditionalLabel("Scaling");
        hbox.add(comp);
    
        if (envelope == 1)  // operator 1 doesn't have EG Shift
            {
            comp = new LabelledDial("Shift", this, "operator" + envelope + "shift", color, 0, 0)  // fixed to 0
                {
                public String map(int val)
                    {
                    return SHIFTS[val];
                    }
                };
            ((LabelledDial)comp).addAdditionalLabel(" [11,TX,>,V] ");
            // we're not going to add it.  Instead we're just going to put a space in.
            comp = Strut.makeStrut(comp);
            model.setStatus("operator" + envelope + "shift", Model.STATUS_IMMUTABLE);
            }
        else
            {
            // TODO: Modify envelope display to show shift
            comp = new LabelledDial("Shift", this, "operator" + envelope + "shift", color, 0, 3)
                {
                public String map(int val)
                    {
                    return SHIFTS[val];
                    }
                };
            ((LabelledDial)comp).addAdditionalLabel(" [11,TX,>,V] ");
            }
        hbox.add(comp);
    

        // ADSR
        // This will *more or less* work, though the release rate will be slightly short
        // for reasons beyond me.
        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "operator" + envelope + "attackrate", "operator" + envelope + "decay1rate", "operator" + envelope + "decay2rate", "operator" + envelope + "releaserate" },
            new String[] { null, null, "operator" + envelope + "decay1level", "operator" + envelope + "decay1level", null },
            new double[] { 0, 1.0/3, 1.0/3, 1.0/3, 1.0/3 },
            new double[] { 0, 1.0, 1.0 / 15.0, 1.0 / 30.0, 0 },
            new double[] { 0, (Math.PI/4/31), (Math.PI/4/31), (Math.PI/4/31), (Math.PI/4/31) })  // note we convert the release rate to 31
            {
            public double preprocessXKey(int index, String key, double value)
                {
                if (key.equals("operator" + envelope + "releaserate"))
                    {
                    return (31.0 - ( (value - 1) * 31.0 / 14.0 ));
                    }
                else 
                    return 31.0 - value;
                }

            public void postProcess(double[] xVals, double[] yVals)
                {
                if (model.get("operator" + envelope + "decay2rate") == 0)
                    {
                    yVals[3] = yVals[2];
                    xVals[3] = 1.0 / 3;
                    }

                int shift = model.get("operator" + envelope + "shift");
                                        
                if (shift > 0)
                    {
                    for(int i = 0; i < 5; i++)
                        {
                        yVals[i] = 1 - yVals[i];
                                                
                        for(int j = 0; j < shift; j++)  
                            yVals[i] /= 2.0;
                                                                                                        
                        yVals[i] = 1 - yVals[i];
                        }
                    }
                }
            };

        model.register("operator" + envelope + "shift", (Updatable)comp);
        hbox.addLast(comp);
                
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
        



    /** Map of parameter -> index in the vcedParameters array. */
    static HashMap vcedParametersToIndex = null;


    /** List of all Sysex parameters in order.  "-" is a reserved (unused and thus unnamed) parameter. */

    /// * indicates parameters which must be handled specially due to packing
    /// that Waldorf decided to do.  :-(

    final static String[] vcedParameters = new String[] 
    {
    "operator4attackrate",
    "operator4decay1rate",
    "operator4decay2rate",
    "operator4releaserate",
    "operator4decay1level",
    "operator4levelscaling",                            // vlevelscalingsign
    "operator4ratescaling",
    "operator4egbiassensitivity",
    "operator4amplitudemodulationenable",                   
    "operator4keyvelocitysensitivity",
    "operator4outputlevel",
    "operator4frequencycoarse",
    "operator4detune",

    "operator2attackrate",
    "operator2decay1rate",
    "operator2decay2rate",
    "operator2releaserate",
    "operator2decay1level",
    "operator2levelscaling",
    "operator2ratescaling",
    "operator2egbiassensitivity",
    "operator2amplitudemodulationenable",                   
    "operator2keyvelocitysensitivity",
    "operator2outputlevel",
    "operator2frequencycoarse",
    "operator2detune",

    "operator3attackrate",
    "operator3decay1rate",
    "operator3decay2rate",
    "operator3releaserate",
    "operator3decay1level",
    "operator3levelscaling",
    "operator3ratescaling",
    "operator3egbiassensitivity",
    "operator3amplitudemodulationenable",                   
    "operator3keyvelocitysensitivity",
    "operator3outputlevel",
    "operator3frequencycoarse",
    "operator3detune",

    "operator1attackrate",
    "operator1decay1rate",
    "operator1decay2rate",
    "operator1releaserate",
    "operator1decay1level",
    "operator1levelscaling",
    "operator1ratescaling",
    "operator1egbiassensitivity",
    "operator1amplitudemodulationenable",                   
    "operator1keyvelocitysensitivity",
    "operator1outputlevel",
    "operator1frequencycoarse",
    "operator1detune",
    
    "algorithm",
    "feedback",         
    "lfospeed",
    "lfodelay",
    "lfopitchmodulationdepth",
    "lfoamplitudemodulationdepth",        
    "lfosync",        
    "lfowave",
    "lfopitchmodulationsensitivity",
    "lfoamplitudemodulationsensitivity",    
    "transpose",
    
    "mono",
    "pitchbendrange",  
    "fulltimeportamentomode",
    "portamentotime",
    "footcontrolvolume",
    "sustain",                  // unused as it turns out
    "portamento",       // unused as it turns out
    "chorus",                   // unused as it turns out        
    "modulationwheelpitch",
    "modulationwheelamplitude",
    "breathcontrolpitch",
    "breathcontrolamplitude",          
    "breathcontrolpitchbias",
    "breathcontrolenvelopebias",
    "name1",
    "name2",
    "name3",
    "name4",
    "name5",
    "name6",
    "name7",
    "name8",
    "name9",
    "name10",
    "pitchattackrate",                          // Yamaha DX21 only
    "pitchdecayrate",                           // Yamaha DX21 only
    "pitchreleaserate",                         // Yamaha DX21 only
    "pitchattacklevel",                 // Yamaha DX21 only
    "pitchdecaylevel",                  // Yamaha DX21 only
    "pitchreleaselevel",                        // Yamaha DX21 only
    };



    /** Map of parameter -> index in the acedParameters array. */
    static HashMap acedParametersToIndex = null;

    final static String[] acedParameters = new String[] 
    {
    "operator4fix",
    "operator4fixedfrequencyrange",
    "operator4frequencyfine",
    "operator4operatorwaveform",
    "operator4shift",

    "operator2fix",
    "operator2fixedfrequencyrange",
    "operator2frequencyfine",
    "operator2operatorwaveform",
    "operator2shift",

    "operator3fix",
    "operator3fixedfrequencyrange",
    "operator3frequencyfine",
    "operator3operatorwaveform",
    "operator3shift",

    "operator1fix",
    "operator1fixedfrequencyrange",
    "operator1frequencyfine",
    "operator1operatorwaveform",
    "operator1shift",
    
    "reverbrate",
    "footcontrolpitch",         
    "footcontrolamplitude"
    };
    

    /** Map of parameter -> index in the aced2Parameters array. */
    static HashMap aced2ParametersToIndex = null;

    final static String[] aced2Parameters = new String[] 
    {
    "aftertouchpitch",
    "aftertouchamplitude",
    "aftertouchpitchbias",
    "aftertouchenvelopebias",
    "operator4vshift",
    "operator2vshift",          // weirdly out of place
    "operator3vshift",
    "operator1vshift",
    "vlevelscalingsign",                // 1, 2, 3, 4.  I don't know the ordering but I presume 1 is bit 0
    "-"
    };


    /** Map of parameter -> index in the aced3Parameters array. */
    static HashMap aced3ParametersToIndex = null;

    final static String[] aced3Parameters = new String[] 
    {
    "veffectsel",
    "veffectbalance",
    "veffectoutlevel",
    "veffectstereomix",
    "veffectparam1",
    "veffectparam2",
    "veffectparam3",
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
    };


    /** Map of parameter -> index in the efedsParameters array. */
    static HashMap efedsParametersToIndex = null;

    final static String[] efedsParameters = new String[] 
    {
    "effectpreset",
    "effecttime",
    "effectbalance",
    };


    /*
      From mgregory22@gmail.com:

      I wrote an editor for the TX81Z awhile back.  Yeah, I think there are a couple bugs in the manual if I remember correctly. I see you wrote Gizmo in C++, and I worked out all the MIDI bugs in my editor, so I'm going to refer to that.

      Here is a file of constants with subgroup numbers on line 173:
      https://github.com/mgregory22/tx81z-programmer/blob/master/src/tx81z.h#L173

      So, the PCED has a subgroup of 0.

      There's also this file, which describes the different dump types in the TX81Z:
      https://github.com/mgregory22/tx81z-programmer/blob/master/src/tx81z_meta.c

      reqStr is a request string, and dumpHdr is the header of the received dump.

      Here's the code to interface with the TX81Z.  I implemented it as a window that sends messages, so I wouldn't have to write threading code, so it might be kind of confusing in places, but I think you'll be able to get it:
      https://github.com/mgregory22/tx81z-programmer/blob/master/src/tx81z.c

      TX81Z_SendParamChange() is probably what you're interested in, if you want to know how I did things:
      https://github.com/mgregory22/tx81z-programmer/blob/master/src/tx81z.c#L507

      I haven't worked on it in a long time, so I can't remember all that much about it, but if you have any questions, let me know and I'll try to answer them.

      Hope that helps,
      Matt

    */

    public static final int VCED_GROUP = 18; // 00010010
    public static final int ACED_GROUP = 19; // 00010011                                // ACED, ACED2, and ACED3
    public static final int EFEDS_GROUP = 36; // 00100100
    public static final int PCED_GROUP = 16; // 00010000        says 00010011 in the manual, wrong
    public static final int REMOTE_SWITCH_GROUP = 19; // 00010011        same as ACED_GROUP
    public static final int TQ5_REMOTE_SWITCH_GROUP = 0x24;

    public Object[] emitAll(String key)
        {
        simplePause(50);
        if (key.equals("bank")) return new Object[0];  // this is not emittable
        if (key.equals("number")) return new Object[0];  // this is not emittable

        byte channel = (byte)(16 + getChannelOut());
        
        if (key.equals("name"))
            {
            Object[] result = new Object[10];
            
            String name = model.get("name", "INIT VOICE") + "          ";

            for(int i = 0; i < 10; i++)
                {
                result[i] = new byte[] { (byte)0xF0, 0x43, channel, VCED_GROUP, (byte)(77 + i), (byte)(name.charAt(i)), (byte)0xF7 };
                }
            return result;
            }
        else if (vcedParametersToIndex.containsKey(key) || key.endsWith("vkeyvelocitysensitivitysign"))
            {
            int index = -1;
            int value = -1;
            
            for(int i = 0; i < 4; i++)
                {
                if ((key.equals("operator" + (i + 1) + "vkeyvelocitysensitivitysign") ||
                        key.equals("operator" + (i + 1) + "vkeyvelocitysensitivitysign")) && synthType == TYPE_V50)
                    {
                    index = ((Integer)(vcedParametersToIndex.get("operator" + (i + 1) + "keyvelocitysensitivity"))).intValue();
                    value = (model.get("operator" + (i + 1) + "keyvelocitysensitivity") | 
                        (model.get("operator" + (i + 1) + "vkeyvelocitysensitivitysign") << 3));
                    break;
                    }
                }
                                
            if (index == -1)
                {
                if (key.endsWith("vkeyvelocitysensitivitysign"))  // invalid
                    return new Object[0];
                else
                    {
                    index = ((Integer)(vcedParametersToIndex.get(key))).intValue();
                    value = model.get(key);
                    }
                }
                            
            byte PP = (byte) index;
            byte VV = (byte) value;
            byte[] data = new byte[] { (byte)0xF0, 0x43, channel, VCED_GROUP, PP, VV, (byte)0xF7 };
            return new Object[] { data };
            }
        else if (acedParametersToIndex.containsKey(key))
            {
            int index = ((Integer)(acedParametersToIndex.get(key))).intValue();
            int value = model.get(key);

            for(int i = 0; i < 4; i++)
                {
                if (key.equals("operator" + (i+1) + "frequencyfine"))
                    {
                    if (model.get("operator" + (i+1) + "frequencycoarse") < 4)  // it's < 1.0
                        {
                        value = Math.min(value, 7);  //  only first 8 values are legal
                        break;
                        }
                    }
                }
                            
            byte PP = (byte) index;
            byte VV = (byte) value;
            byte[] data = new byte[] { (byte)0xF0, 0x43, channel, ACED_GROUP, PP, VV, (byte)0xF7 };
            return new Object[] { data };
            }
        else if (aced2ParametersToIndex.containsKey(key) || key.endsWith("vlevelscalingsign"))
            {
            int index = -1;
            int value = -1;
            
            if (key.endsWith("vlevelscalingsign") && synthType == TYPE_V50)
                {
                index = 8;
                value = ( model.get("operator1vlevelscalingsign") |
                    (model.get("operator2vlevelscalingsign") << 1) |
                    (model.get("operator3vlevelscalingsign") << 2) |
                    (model.get("operator4vlevelscalingsign") << 3));
                }

            if (index == -1)
                {
                if (key.endsWith("vlevelscalingsign"))  // invalid
                    return new Object[0];
                else
                    {
                    index = ((Integer)(aced2ParametersToIndex.get(key))).intValue();
                    value = model.get(key);
                    }
                }
            

            // There is no ACED2_GROUP.  We just continue the parameters from 22
            byte PP = (byte) (index + 23);
            byte VV = (byte) value;
            byte[] data = new byte[] { (byte)0xF0, 0x43, channel, ACED_GROUP, PP, VV, (byte)0xF7 };
            return new Object[] { data };
            }
        else if (aced3ParametersToIndex.containsKey(key))
            {
            int index = ((Integer)(aced3ParametersToIndex.get(key))).intValue();
            int value = model.get(key);
            
            // There is no ACED3_GROUP.  We just continue the parameters from 32
            byte PP = (byte) (index + 33);
            byte VV = (byte) value;
            byte[] data = new byte[] { (byte)0xF0, 0x43, channel, ACED_GROUP, PP, VV, (byte)0xF7 };
            return new Object[] { data };
            }
        else if (efedsParametersToIndex.containsKey(key))
            {
            int index = ((Integer)(efedsParametersToIndex.get(key))).intValue();
            int value = model.get(key);
            
            byte PP = (byte) (index + 4);
            byte VV = (byte) value;
//            if (key.equals("effectpreset")) VV = (byte)(VV + 1);                                                      // 0...9 -> 1...10
            byte[] data = new byte[] { (byte)0xF0, 0x43, channel, EFEDS_GROUP, PP, VV, (byte)0xF7 };
            return new Object[] { data };
            }
        else 
            {
            System.err.println("Warning (Yamaha4Op): Can't emit key " + key);
            return new Object[0];
            }
        }
    

    public int parse(byte[] data, boolean fromFile)
        {
        if (data.length == 4104)  // VMEM
            {
            return parseVMEM(data, fromFile);
            }
        else
            {
            int pos = 0;
            boolean foundSomething = false;
            boolean foundVCED = false;
            
            // Look for ACED3
            int val = parseACED3(data, pos);
            if (val != FAIL)
                {
                pos = val;
                foundSomething = true;
                }

            // Look for EFEDS
            val = parseEFEDS(data, pos);
            if (val != FAIL)
                {
                pos = val;
                foundSomething = true;
                }

            // Look for ACED2
            val = parseACED2(data, pos);
            if (val != FAIL)
                {
                pos = val;
                foundSomething = true;
                }

            // Look for ACED
            val = parseACED(data, pos);
            if (val != FAIL)
                {
                pos = val;
                foundSomething = true;
                }
            
            // Look for VCED
            val = parseVCED(data, pos);
            if (val != FAIL)
                {
                pos = val;
                foundSomething = true;
                foundVCED = true;
                }
            
            if (!foundSomething) return PARSE_FAILED;
            else if (!foundVCED) return PARSE_INCOMPLETE;
            else 
                {
                revise(); 
                return PARSE_SUCCEEDED;
                }
            }
        }
        
    int parseVCED(byte[] data, int offset)
        {
        if (findSysexLength(data, offset) == offset + 101 &&       // 6-byte header + 93 + 2 byte footer
            data[offset + 3] == 0x03 &&
            data[offset + 4] == 0x00 &&
            data[offset + 5] == 0x5D) // VCED?
            {
            byte[] name = new byte[11];
            name[10] = '\0';  // yes I know it's that already...
                
            for(int i = 0; i < vcedParameters.length; i++)
                {
                byte val = data[offset + i + 6];
                                
                if (vcedParameters[i].equals("-"))
                    continue;
                else if (i >= 77 && i <= 86) // name
                    {
                    name[i - 77] = val;
                    }
                else if (vcedParameters[i].endsWith("keyvelocitysensitivity"))  // gotta break out the sign for the V50
                    {
                    model.set(vcedParameters[i], val & 7);
                    model.set(vcedParameters[i].substring(0,9) + "v" + vcedParameters[i].substring(9) + "sign", (val >>> 3) & 1);
                    }
                else
                    {
                    model.set(vcedParameters[i], val);
                    }
                }
            try { model.set("name", new String(name, "US-ASCII")); }
            catch (Exception e) { Synth.handleException(e); return FAIL; }

            return offset + 101;    
            }
        else 
            {
            return FAIL;
            }
        }

    int parseACED(byte[] data, int offset)
        {
        if (findSysexLength(data, offset) == offset + 41 &&   // 6-byte header + "LM  8976AE" + 23 + 2-byte footer
            data[offset + 3] == 0x7E &&
            data[offset + 4] == 0x00 &&
            data[offset + 5] == 0x21 &&
            // next it spits out the header "LM  8976AE"
            data[offset + 6] == 'L' &&
            data[offset + 7] == 'M' &&
            data[offset + 8] == ' ' &&
            data[offset + 9] == ' ' &&
            data[offset + 10] == '8' &&
            data[offset + 11] == '9' &&
            data[offset + 12] == '7' &&
            data[offset + 13] == '6' &&
            data[offset + 14] == 'A' &&
            data[offset + 15] == 'E')
            {
            for(int i = 0; i < acedParameters.length; i++)
                {
                byte val = data[offset + i + 16];
                                
                if (acedParameters[i].equals("-"))
                    continue;
                else
                    {
                    model.set(acedParameters[i], val);
                    }
                }
            return offset + 41;    
            }
        else 
            {
            return FAIL;
            }
        }

                
    int parseACED2(byte[] data, int offset)
        {
        if (findSysexLength(data, offset) == offset + 28 &&   // 6-byte header + "LM  8023AE" + 10 + 2-byte footer
            data[offset + 3] == 0x7E &&
            data[offset + 4] == 0x00 &&
            data[offset + 5] == 0x14 &&
            // next it spits out the header "LM  8023AE"
            data[offset + 6] == 'L' &&
            data[offset + 7] == 'M' &&
            data[offset + 8] == ' ' &&
            data[offset + 9] == ' ' &&
            data[offset + 10] == '8' &&
            data[offset + 11] == '0' &&
            data[offset + 12] == '2' &&
            data[offset + 13] == '3' &&
            data[offset + 14] == 'A' &&
            data[offset + 15] == 'E')
            {
            for(int i = 0; i < aced2Parameters.length; i++)
                {
                byte val = data[offset + i + 16];
                                
                if (aced2Parameters[i].equals("-"))
                    continue;
                else if (aced2Parameters[i].equals("vlevelscalingsign"))
                    {
                    // I *think* this is the right order?  that is, op 1 is bit 0
                    model.set("operator1vlevelscalingsign", val & 1);
                    model.set("operator2vlevelscalingsign", (val >>> 1) & 1);
                    model.set("operator3vlevelscalingsign", (val >>> 2) & 1);
                    model.set("operator4vlevelscalingsign", (val >>> 3) & 1);
                    }
                else
                    {
                    model.set(aced2Parameters[i], val);
                    }
                }
            return offset + 28;    
            }
        else 
            {
            return FAIL;
            }
        }

    int parseACED3(byte[] data, int offset)
        {
        if (findSysexLength(data, offset) == offset + 38 &&   // 6-byte header + "LM  8073AE" + 20 + 2-byte footer
            data[offset + 3] == 0x7E &&
            data[offset + 4] == 0x00 &&
            data[offset + 5] == 0x1E &&
            // next it spits out the header "LM  8073AE"
            data[offset + 6] == 'L' &&
            data[offset + 7] == 'M' &&
            data[offset + 8] == ' ' &&
            data[offset + 9] == ' ' &&
            data[offset + 10] == '8' &&
            data[offset + 11] == '0' &&
            data[offset + 12] == '7' &&
            data[offset + 13] == '3' &&
            data[offset + 14] == 'A' &&
            data[offset + 15] == 'E')
            {
            for(int i = 0; i < aced3Parameters.length; i++)
                {
                byte val = data[offset + i + 16];
                                
                if (aced3Parameters[i].equals("-"))
                    continue;
                else
                    {
                    model.set(aced3Parameters[i], val);
                    }
                }
            return offset + 38;    
            }
        else 
            {
            return FAIL;
            }
        }


    int parseEFEDS(byte[] data, int offset)
        {
        if (findSysexLength(data, offset) == offset + 21 &&   // 3 + "LM  8036EF" + 8 byte wrapper
            data[offset + 3] == 0x7E &&
            data[offset + 4] == 0x00 &&
            data[offset + 5] == 0x0D &&
            // next it spits out the header "LM  8036EF"
            data[offset + 6] == 'L' &&
            data[offset + 7] == 'M' &&
            data[offset + 8] == ' ' &&
            data[offset + 9] == ' ' &&
            data[offset + 10] == '8' &&
            data[offset + 11] == '0' &&
            data[offset + 12] == '3' &&
            data[offset + 13] == '6' &&
            data[offset + 14] == 'E' &&
            data[offset + 15] == 'F')
            {
            for(int i = 0; i < efedsParameters.length; i++)
                {
                byte val = data[offset + i + 16];
                                
                if (efedsParameters[i].equals("-"))                                                                     // never happens
                    continue;
                else
                    {
                    /*
                      if (efedsParameters[i].equals("effectpreset"))
                      {
                      val = (byte)(val - 1);                                                                                  // 1...10 -> 0...9
                      }
                    */
                    model.set(efedsParameters[i], val);
                    }
                }
            return offset + 21;    
            }
        else 
            {
            return FAIL;
            }
        }

    /** Parses a given patch from the provided bank sysex, and returns 
        PARSE_SUCCEEDED or PARSE_SUCCEEDED_UNTITLED if successful, else PARSE_FAILED (the default). */
    public int parseFromBank(byte[] data, int number)
        {
        // okay, we're loading and editing patch number.  Here we go.
        int patch = number * 128;
        int pos = 0;
                                                                                
        // extract name
        char[] name = new char[10];
        for (int j = 0; j < 10; j++)
            {
            name[j] = (char)(data[patch + 57 + j + 6] & 127);
            }
                        
        model.set("name", new String(name));
        model.set("number", number);
        model.set("bank", 0);                   // we don't know what the bank is in reality
                

        for(int op = 0; op < 4; op++)
            {
            // attack rate
            model.set(vcedParameters[pos++], data[patch + op * 10 + 0 + 6] & 31);
            // decay 1 rate
            model.set(vcedParameters[pos++], data[patch + op * 10 + 1 + 6] & 31);
            // decay 2 rate
            model.set(vcedParameters[pos++], data[patch + op * 10 + 2 + 6] & 31);
            // release rate
            model.set(vcedParameters[pos++], data[patch + op * 10 + 3 + 6] & 15);
            // decay 1 level
            model.set(vcedParameters[pos++], data[patch + op * 10 + 4 + 6] & 15);
            // level scaling
            model.set(vcedParameters[pos++], data[patch + op * 10 + 5 + 6] & 127);
            // rate scaling
            model.set(vcedParameters[pos++], (data[patch + op * 10 + 9 + 6] >>> 3) & 3);                        // out of order?
            // eg bias sensitivity
            model.set(vcedParameters[pos++], (data[patch + op * 10 + 6 + 6] >>> 3) & 7);
            // amplitude modulation
            model.set(vcedParameters[pos++], (data[patch + op * 10 + 6 + 6] >>> 6) & 1);
            // key velocity sensitivity
            model.set(vcedParameters[pos++], (data[patch + op * 10 + 6 + 6] >>> 0) & 7);
            // operator output level
            model.set(vcedParameters[pos++], data[patch + op * 10 + 7 + 6] & 127);
            // frequency
            model.set(vcedParameters[pos++], data[patch + op * 10 + 8 + 6] & 63);
            // detune
            model.set(vcedParameters[pos++], (data[patch + op * 10 + 9 + 6] >>> 0) & 7);
            // key velocity sensitivity sign (V50 only)
            model.set("operator" + (op + 1) + "vkeyvelocitysensitivitysign", (data[patch + op * 10 + 9 + 6] >>> 3) & 1);
            // level scaing sign (V50 only)
            model.set("operator" + (op + 1) + "vlevelscalingsign", (data[patch + op * 10 + 9 + 6] >>> 6) & 1);
            }

        // algorithm
        model.set(vcedParameters[pos++], (data[patch + 40 + 6] >>> 0) & 7);
        // feedback
        model.set(vcedParameters[pos++], (data[patch + 40 + 6] >>> 3) & 7);
        // lfo speed
        model.set(vcedParameters[pos++], data[patch + 41 + 6] & 127);
        // lfo delay
        model.set(vcedParameters[pos++], data[patch + 42 + 6] & 127);
        // pitch modulation depth
        model.set(vcedParameters[pos++], data[patch + 43 + 6] & 127);
        // amplitude modulation depth
        model.set(vcedParameters[pos++], data[patch + 44 + 6] & 127);
        // lfo sync
        model.set(vcedParameters[pos++], (data[patch + 40 + 6] >>> 6) & 1);                             // out of order
        // lfo wave
        model.set(vcedParameters[pos++], (data[patch + 45 + 6] >>> 0) & 3);
        // pitch modulation sensitivity
        model.set(vcedParameters[pos++], (data[patch + 45 + 6] >>> 4) & 7);
        // amplitude modulation sensitivity
        model.set(vcedParameters[pos++], (data[patch + 45 + 6] >>> 2) & 3);
        // transpose
        model.set(vcedParameters[pos++], data[patch + 46 + 6] & 63);  // not marked in the documentation, but it goes 0...48
        // poly/mono
        model.set(vcedParameters[pos++], (data[patch + 48 + 6] >>> 3) & 1);                             // out of order
        // pitch bend range
        model.set(vcedParameters[pos++], data[patch + 47 + 6] & 15);
        // portamento mode
        model.set(vcedParameters[pos++], (data[patch + 48 + 6] >>> 0) & 1);
        // portamento time
        model.set(vcedParameters[pos++], data[patch + 49 + 6] & 127);
        // foot control volume
        model.set(vcedParameters[pos++], data[patch + 50 + 6] & 127);
        // sustain
        model.set(vcedParameters[pos++], (data[patch + 48 + 6] >>> 2) & 1);                             // out of order
        // portamento
        model.set(vcedParameters[pos++], (data[patch + 48 + 6] >>> 1) & 1);                             // out of order
        // chorus
        model.set(vcedParameters[pos++], (data[patch + 48 + 6] >>> 4) & 1);                             // out of order
        // modulation wheel pitch
        model.set(vcedParameters[pos++], data[patch + 51 + 6] & 127);
        // modulation wheel amplitude
        model.set(vcedParameters[pos++], data[patch + 52 + 6] & 127);
        // breath control pitch
        model.set(vcedParameters[pos++], data[patch + 53 + 6] & 127);
        // breath control amplitude
        model.set(vcedParameters[pos++], data[patch + 54 + 6] & 127);
        // breath control pitch bias
        model.set(vcedParameters[pos++], data[patch + 55 + 6] & 127);
        // breath control eg bias
        model.set(vcedParameters[pos++], data[patch + 56 + 6] & 127);
        
        //// We skip the name (we did it earlier)
        
        //// Pitch Rate 1
        model.set(vcedParameters[pos++], data[patch + 67 + 6] & 127);
        //// Pitch Rate 2
        model.set(vcedParameters[pos++], data[patch + 68 + 6] & 127);
        //// Pitch Rate 3
        model.set(vcedParameters[pos++], data[patch + 69 + 6] & 127);
        //// Pitch Level 1
        model.set(vcedParameters[pos++], data[patch + 70 + 6] & 127);
        //// Pitch Level 2
        model.set(vcedParameters[pos++], data[patch + 71 + 6] & 127);
        //// Pitch Level 3
        model.set(vcedParameters[pos++], data[patch + 72 + 6] & 127);
        
        pos = 0;        // reset, we're now doing ACED
        for(int op = 0; op < 4; op++)
            {
            // fixed frequency
            model.set(acedParameters[pos++], (data[patch + op * 2 + 73 + 6] >>> 3) & 1);
            // fixed frequency range
            model.set(acedParameters[pos++], (data[patch + op * 2 + 73 + 6] >>> 0) & 7);
            // frequency range fine
            model.set(acedParameters[pos++], (data[patch + op * 2 + 74 + 6] >>> 0) & 15);
            // operator waveform
            model.set(acedParameters[pos++], (data[patch + op * 2 + 74 + 6] >>> 4) & 7);
            // eg shift
            model.set(acedParameters[pos++], (data[patch + op * 2 + 73 + 6] >>> 4) & 3);                                // out of order
            // fixed range mode (V50 only)
            model.set("operator" + (op + 1) + "vshift", (data[patch + op * 2 + 73 + 6] >>> 6) & 1);  // out of order    // in docs as "FIXRM" for "FIX RANGE MODE"
            }

        // reverb rate
        model.set(acedParameters[pos++], data[patch + 81 + 6] & 7);
        // foot controller pitch
        model.set(acedParameters[pos++], data[patch + 82 + 6] & 127);
        // foot controller amplitude
        model.set(acedParameters[pos++], data[patch + 83 + 6] & 127);
        
        
        pos = 0;        // reset, we're now doing ACED2
        // Aftertouch Pitch
        model.set(aced2Parameters[pos++], data[patch + 84 + 6] & 7);        
        // Aftertouch Amplitude
        model.set(aced2Parameters[pos++], data[patch + 85 + 6] & 7);
        // Aftertouch Pitch Bias
        //model.set(aced2Parameters[pos++], data[patch + 86 + 6] & 7);
        int ATPB = data[patch + 86 + 6] & 7;
        if (ATPB > 50) 
            model.set(aced2Parameters[pos++], ATPB - 51);
        else
            model.set(aced3Parameters[pos++], ATPB + 50);
        // Aftertouch EG BBias
        model.set(aced2Parameters[pos++], data[patch + 87 + 6] & 7);

        // skip bytes 88..90

        pos = 0;        // reset, we're now doing EFEDS
        // Effect Preset Number
        // model.set(efedsParameters[pos++], data[patch + 91 + 6] & 15 - 1);    // 1...10 -> 0...9     
        model.set(efedsParameters[pos++], data[patch + 91 + 6] & 15);                      
        // Effect Time
        model.set(efedsParameters[pos++], data[patch + 92 + 6] & 127);                  // could go clear to 75
        // Effect Balance
        model.set(efedsParameters[pos++], data[patch + 93 + 6] & 127);
                
                
        pos = 0;        // reset, we're now doing ACED3, V50 only
        // V Effect Sel
        model.set(aced3Parameters[pos++], data[patch + 94 + 6] & 63);           // 0...32, so 64 bit argh     
        // V Balance
        model.set(aced3Parameters[pos++], data[patch + 95 + 6] & 127);          // 0...100        
        // V Out Level
        model.set(aced3Parameters[pos++], data[patch + 96 + 6] & 127);          // 0...100        
        // V Effect Stereo Mix
        model.set(aced3Parameters[pos++], data[patch + 97 + 6] & 1);            // 0...1        
        // V Param 1
        model.set(aced3Parameters[pos++], data[patch + 98 + 6] & 127);          // 0...75     
        // V Param 2
        model.set(aced3Parameters[pos++], data[patch + 99 + 6] & 127);          // 0...99     
        // V Param 3
        model.set(aced3Parameters[pos++], data[patch + 100 + 6] & 127);         // 0...99
        
        revise();  
        return PARSE_SUCCEEDED;         
        }



    public int parseVMEM(byte[] data, boolean fromFile)
        {
        // extract names
        char[][] names = new char[32][10];
        for(int i = 0; i < 32; i++)
            {
            for (int j = 0; j < 10; j++)
                {
                names[i][j] = (char)(data[i * 128 + 57 + j + 6] & 127);
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
        if (patchNum < 0) 
            return PARSE_CANCELLED;

        return parseFromBank(data, patchNum);
        }
 
 
 
 
    public Object[] emitBank(Model[] models, int bank, boolean toFile)
        {
        byte[] data = new byte[4104];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x43;
        data[2] = (byte)(getChannelOut());;
        data[3] = (byte)0x04;
        data[4] = (byte)0x20;                           // manual says 10 but this is wrong
        data[5] = (byte)0x00;
        
        for(int number = 0; number < 32; number++)
            {
            // okay, we're loading and editing patch number.  Here we go.
            int patch = number * 128;
            int pos = 0;
                                                                                                                                                                
            // emit name
            char[] name = (models[number].get("name", "INIT VOICE") + "          ").toCharArray();
            for (int j = 0; j < 10; j++)
                {
                data[patch + 57 + j + 6] = (byte)(name[j] & 127);
                }
                        
            for(int op = 0; op < 4; op++)
                {
                // attack rate
                data[patch + op * 10 + 0 + 6] = (byte) (models[number].get(vcedParameters[pos++]) & 31);
                // decay 1 rate
                data[patch + op * 10 + 1 + 6] = (byte) (models[number].get(vcedParameters[pos++]) & 31);
                // decay 2 rate
                data[patch + op * 10 + 2 + 6] = (byte) (models[number].get(vcedParameters[pos++]) & 31);
                // release rate
                data[patch + op * 10 + 3 + 6] = (byte) (models[number].get(vcedParameters[pos++]) & 15);
                // decay 1 level
                data[patch + op * 10 + 4 + 6] = (byte) (models[number].get(vcedParameters[pos++]) & 15);
                // level scaling
                data[patch + op * 10 + 5 + 6] = (byte) (models[number].get(vcedParameters[pos++]) & 127);
                // rate scaling
                data[patch + op * 10 + 9 + 6] |= (byte) (((models[number].get(vcedParameters[pos++]) & 3) << 3));                      // out of order?
                // eg bias sensitivity
                data[patch + op * 10 + 6 + 6] |= (byte) (((models[number].get(vcedParameters[pos++]) & 7) << 3));
                // amplitude modulation
                data[patch + op * 10 + 6 + 6] |= (byte) (((models[number].get(vcedParameters[pos++]) & 1) << 6));
                // key velocity sensitivity
                data[patch + op * 10 + 6 + 6] |= (byte) (((models[number].get(vcedParameters[pos++]) & 7) << 0));
                // operator output level
                data[patch + op * 10 + 7 + 6] = (byte) (models[number].get(vcedParameters[pos++]) & 127);
                // frequency
                data[patch + op * 10 + 8 + 6] = (byte) (models[number].get(vcedParameters[pos++]) & 63);
                // detune
                data[patch + op * 10 + 9 + 6] |= (byte) (((models[number].get(vcedParameters[pos++]) & 7) << 0));
                // key velocity sensitivity sign (V50 only)
                data[patch + op * 10 + 9 + 6] |= (byte) (((models[number].get("operator" + (op + 1) + "vkeyvelocitysensitivitysign") & 1) << 3));
                // level scaing sign (V50 only)
                data[patch + op * 10 + 9 + 6] |= (byte) (((models[number].get("operator" + (op + 1) + "vlevelscalingsign") & 1) << 6));
                }

            // algorithm
            data[patch + 40 + 6] |= (byte) (((models[number].get(vcedParameters[pos++]) & 7) << 0));
            // feedback
            data[patch + 40 + 6] |= (byte) (((models[number].get(vcedParameters[pos++]) & 7) << 3));
            // lfo speed
            data[patch + 41 + 6] = (byte) (models[number].get(vcedParameters[pos++]) & 127);
            // lfo delay
            data[patch + 42 + 6] = (byte) (models[number].get(vcedParameters[pos++]) & 127);
            // pitch modulation depth
            data[patch + 43 + 6] = (byte) (models[number].get(vcedParameters[pos++]) & 127);
            // amplitude modulation depth
            data[patch + 44 + 6] = (byte) (models[number].get(vcedParameters[pos++]) & 127);
            // lfo sync
            data[patch + 40 + 6] |= (byte) (((models[number].get(vcedParameters[pos++]) & 1) << 6));                               // out of order
            // lfo wave
            data[patch + 45 + 6] |= (byte) (((models[number].get(vcedParameters[pos++]) & 3) << 0));
            // pitch modulation sensitivity
            data[patch + 45 + 6] |= (byte) (((models[number].get(vcedParameters[pos++]) & 7) << 4));
            // amplitude modulation sensitivity
            data[patch + 45 + 6] |= (byte) (((models[number].get(vcedParameters[pos++]) & 3) << 2));
            // transpose
            data[patch + 46 + 6] = (byte) (models[number].get(vcedParameters[pos++]) & 63);  // not marked in the documentation, but it goes 0...48
            // poly/mono
            data[patch + 48 + 6] |= (byte) (((models[number].get(vcedParameters[pos++]) & 1) << 3));                               // out of order
            // pitch bend range
            data[patch + 47 + 6] = (byte) (models[number].get(vcedParameters[pos++]) & 15);
            // portamento mode
            data[patch + 48 + 6] |= (byte) (((models[number].get(vcedParameters[pos++]) & 1) << 0));
            // portamento time
            data[patch + 49 + 6] = (byte) (models[number].get(vcedParameters[pos++]) & 127);
            // foot control volume
            data[patch + 50 + 6] = (byte) (models[number].get(vcedParameters[pos++]) & 127);
            // sustain
            data[patch + 48 + 6] |= (byte) (((models[number].get(vcedParameters[pos++]) & 1) << 2));                               // out of order
            // portamento
            data[patch + 48 + 6] |= (byte) (((models[number].get(vcedParameters[pos++]) & 1) << 1));                               // out of order
            // chorus
            data[patch + 48 + 6] |= (byte) (((models[number].get(vcedParameters[pos++]) & 1) << 4));                               // out of order
            // modulation wheel pitch
            data[patch + 51 + 6] = (byte) (models[number].get(vcedParameters[pos++]) & 127);
            // modulation wheel amplitude
            data[patch + 52 + 6] = (byte) (models[number].get(vcedParameters[pos++]) & 127);
            // breath control pitch
            data[patch + 53 + 6] = (byte) (models[number].get(vcedParameters[pos++]) & 127);
            // breath control amplitude
            data[patch + 54 + 6] = (byte) (models[number].get(vcedParameters[pos++]) & 127);
            // breath control pitch bias
            data[patch + 55 + 6] = (byte) (models[number].get(vcedParameters[pos++]) & 127);
            // breath control eg bias
            data[patch + 56 + 6] = (byte) (models[number].get(vcedParameters[pos++]) & 127);
                
            //// We skip the name (we did it earlier)
                
            //// Pitch Rate 1
            data[patch + 67 + 6] = (byte) (models[number].get(vcedParameters[pos++]) & 127);
            //// Pitch Rate 2
            data[patch + 68 + 6] = (byte) (models[number].get(vcedParameters[pos++]) & 127);
            //// Pitch Rate 3
            data[patch + 69 + 6] = (byte) (models[number].get(vcedParameters[pos++]) & 127);
            //// Pitch Level 1
            data[patch + 70 + 6] = (byte) (models[number].get(vcedParameters[pos++]) & 127);
            //// Pitch Level 2
            data[patch + 71 + 6] = (byte) (models[number].get(vcedParameters[pos++]) & 127);
            //// Pitch Level 3
            data[patch + 72 + 6] = (byte) (models[number].get(vcedParameters[pos++]) & 127);
                
            pos = 0;        // reset, we're now doing ACED
            for(int op = 0; op < 4; op++)
                {
                // fixed frequency
                data[patch + op * 2 + 73 + 6] |= (byte) (((models[number].get(acedParameters[pos++]) & 1) << 3));
                // fixed frequency range
                data[patch + op * 2 + 73 + 6] |= (byte) (((models[number].get(acedParameters[pos++]) & 7) << 0));
                // frequency range fine
                data[patch + op * 2 + 74 + 6] |= (byte) (((models[number].get(acedParameters[pos++]) & 15) << 0));
                // operator waveform
                data[patch + op * 2 + 74 + 6] |= (byte) (((models[number].get(acedParameters[pos++]) & 7) << 4));
                // eg shift
                data[patch + op * 2 + 73 + 6] |= (byte) (((models[number].get(acedParameters[pos++]) & 3) << 4));                              // out of order
                // fixed range mode (V50 only)
                data[patch + op * 2 + 73 + 6] |= (byte) (((models[number].get("operator" + (op + 1) + "vshift") & 1) << 6));  // out of order  // in docs as "FIXRM" for "FIX RANGE MODE"
                }

            // reverb rate
            data[patch + 81 + 6] = (byte) (models[number].get(acedParameters[pos++]) & 7);
            // foot controller pitch
            data[patch + 82 + 6] = (byte) (models[number].get(acedParameters[pos++]) & 127);
            // foot controller amplitude
            data[patch + 83 + 6] = (byte) (models[number].get(acedParameters[pos++]) & 127);
                
                
            pos = 0;        // reset, we're now doing ACED2
            // Aftertouch Pitch
            data[patch + 84 + 6] = (byte) (models[number].get(aced2Parameters[pos++]) & 7);        
            // Aftertouch Amplitude
            data[patch + 85 + 6] = (byte) (models[number].get(aced2Parameters[pos++]) & 7);
            // Aftertouch Pitch Bias
            //data[patch + 86 + 6] = (byte) (models[number].get(aced2Parameters[pos++]) & 7);
            int atpb = models[number].get(aced2Parameters[pos++]) & 7;
            if (atpb < 50)
                data[patch + 86 + 6] = (byte) (atpb + 51);
            else
                data[patch + 85 + 6] = (byte) (atpb - 50);
            // Aftertouch EG BBias
            data[patch + 87 + 6] = (byte) (models[number].get(aced2Parameters[pos++]) & 7);

            // skip bytes 88..90

            pos = 0;        // reset, we're now doing EFEDS
            // Effect Preset Number
//            data[patch + 91 + 6] = (byte) (models[number].get(efedsParameters[pos++] + 1) & 15);       // 0...9 -> 1...10
            data[patch + 91 + 6] = (byte) (models[number].get(efedsParameters[pos++]) & 15);
            // Effect Time
            data[patch + 92 + 6] = (byte) (models[number].get(efedsParameters[pos++]) & 127);                   // could go clear to 75
            // Effect Balance
            data[patch + 93 + 6] = (byte) (models[number].get(efedsParameters[pos++]) & 127);
                                
                                
            pos = 0;        // reset, we're now doing ACED3, V50 only
            // V Effect Sel
            data[patch + 94 + 6] = (byte) (models[number].get(aced3Parameters[pos++]) & 63);           // 0...32, so 64 bit argh     
            // V Balance
            data[patch + 95 + 6] = (byte) (models[number].get(aced3Parameters[pos++]) & 127);          // 0...100        
            // V Out Level
            data[patch + 96 + 6] = (byte) (models[number].get(aced3Parameters[pos++]) & 127);          // 0...100        
            // V Effect Stereo Mix
            data[patch + 97 + 6] = (byte) (models[number].get(aced3Parameters[pos++]) & 1);                    // 0...1        
            // V Param 1
            data[patch + 98 + 6] = (byte) (models[number].get(aced3Parameters[pos++]) & 127);          // 0...75     
            // V Param 2
            data[patch + 99 + 6] = (byte) (models[number].get(aced3Parameters[pos++]) & 127);          // 0...99     
            // V Param 3
            data[patch + 100 + 6] = (byte) (models[number].get(aced3Parameters[pos++]) & 127);         // 0...99
            }
                        
        data[data.length - 2] = produceChecksum(data, 6);
        data[data.length - 1] = (byte)0xF7;
        return new Object[] { data };
        }
        
        
        
        
        
    
    public Object[] emitAll(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        if (tempModel == null)
            tempModel = getModel();
        
//        if (!toFile) simplePause(50);
        byte[][] result = new byte[4][];
  
  
        // we can only emit ACED3 *or* EFEDS because if we're emitting
        // either of them, then we should only emit *4* sysex chunks 
        // (ACED3 + ACED2 + ACED + VCED, or EFEDS + ACED2 + ACED + VCED)
        // so that if we save them it makes sense to the loader when trying
        // to guess how many sysex chunks are loaded for each patch.
        // Yamaha really screwed up here.
        
        byte[] data = null;
        if (getSynthType() == TYPE_V50)
            {
            // ACED3
            result[0] = new byte[38];
            data = new byte[30];
        
            data[0] = (byte)'L';
            data[1] = (byte)'M';
            data[2] = (byte)' ';
            data[3] = (byte)' ';
            data[4] = (byte)'8';
            data[5] = (byte)'0';
            data[6] = (byte)'7';
            data[7] = (byte)'3';
            data[8] = (byte)'A';
            data[9] = (byte)'E';
                
            for(int i = 0; i < aced3Parameters.length; i++)
                {
                if (!(aced3Parameters[i].equals("-")))
                    data[i + 10] = (byte)(model.get(aced3Parameters[i]));
                }
        
            result[0][0] = (byte)0xF0;
            result[0][1] = 0x43;
            result[0][2] = (byte)(getChannelOut()); //(byte)(32 + getChannelOut());
            result[0][3] = (byte)0x7E;
            result[0][4] = 0x00;
            result[0][5] = 0x1E;
            System.arraycopy(data, 0, result[0], 6, data.length);
            result[0][6 + data.length] = produceChecksum(data);
            result[0][7 + data.length] = (byte)0xF7;
            }
        else
            {
            // EFEDS
            result[0] = new byte[21];
            data = new byte[13];

            data[0] = (byte)'L';
            data[1] = (byte)'M';
            data[2] = (byte)' ';
            data[3] = (byte)' ';
            data[4] = (byte)'8';
            data[5] = (byte)'0';
            data[6] = (byte)'3';
            data[7] = (byte)'6';
            data[8] = (byte)'E';
            data[9] = (byte)'F';
                
            for(int i = 0; i < efedsParameters.length; i++)
                {
                //if (efedsParameters[i].equals("effectpreset"))
                //      {
                //      data[i + 10] = (byte)(model.get(efedsParameters[i]) + 1);                               // 0...9 -> 1...10
                //      }
                //else
                //      {
                data[i + 10] = (byte)(model.get(efedsParameters[i]));
                //  }
                }

            result[0][0] = (byte)0xF0;
            result[0][1] = 0x43;
            result[0][2] = (byte)(getChannelOut());
            result[0][3] = (byte)0x7E;
            result[0][4] = 0x00;
            result[0][5] = 0x0D;
            System.arraycopy(data, 0, result[0], 6, data.length);
            result[0][6 + data.length] = produceChecksum(data);
            result[0][7 + data.length] = (byte)0xF7;
            }
        
        // ACED2
        result[1] = new byte[28];
        data = new byte[20];

        data[0] = (byte)'L';
        data[1] = (byte)'M';
        data[2] = (byte)' ';
        data[3] = (byte)' ';
        data[4] = (byte)'8';
        data[5] = (byte)'0';
        data[6] = (byte)'2';
        data[7] = (byte)'3';
        data[8] = (byte)'A';
        data[9] = (byte)'E';
        
        for(int i = 0; i < aced2Parameters.length; i++)
            {
            if (i == 8)  // "LS SIGN" (level scaling sign), handle specially
                {
                data[i + 10] = (byte)(  model.get("operator1vlevelscalingsign") |
                    (model.get("operator2vlevelscalingsign") << 1) |
                    (model.get("operator3vlevelscalingsign") << 2) |
                    (model.get("operator4vlevelscalingsign") << 3));
                }
            else if (!(aced2Parameters[i].equals("-")))
                data[i + 10] = (byte)(model.get(aced2Parameters[i]));
            }

        result[1][0] = (byte)0xF0;
        result[1][1] = 0x43;
        result[1][2] = (byte)(getChannelOut()); //(byte)(32 + getChannelOut());
        result[1][3] = (byte)0x7E;
        result[1][4] = 0x00;
        result[1][5] = 0x14;
        System.arraycopy(data, 0, result[1], 6, data.length);
        result[1][6 + data.length] = produceChecksum(data);
        result[1][7 + data.length] = (byte)0xF7;

        // ACED
        result[2] = new byte[41];
        data = new byte[33];

        data[0] = (byte)'L';
        data[1] = (byte)'M';
        data[2] = (byte)' ';
        data[3] = (byte)' ';
        data[4] = (byte)'8';
        data[5] = (byte)'9';
        data[6] = (byte)'7';
        data[7] = (byte)'6';
        data[8] = (byte)'A';
        data[9] = (byte)'E';
        
        for(int i = 0; i < acedParameters.length; i++)
            {
            data[i + 10] = (byte)(model.get(acedParameters[i]));

            // handle low fine ratio values
            for(int j = 0; j < 4; j++)
                {
                if (acedParameters[i].equals("operator" + j + "frequencyfine"))
                    {
                    if (model.get("operator" + j + "frequencycoarse") < 4)  // it's < 1.0
                        data[i + 10] = (byte)(Math.min(data[i + 10] , 7));  //  only first 8 values are legal
                    }
                }
            }

        result[2][0] = (byte)0xF0;
        result[2][1] = 0x43;
        result[2][2] = (byte)(getChannelOut()); //(byte)(32 + getChannelOut());
        result[2][3] = (byte)0x7E;
        result[2][4] = 0x00;
        result[2][5] = 0x21;
        System.arraycopy(data, 0, result[2], 6, data.length);
        result[2][6 + data.length] = produceChecksum(data);
        result[2][7 + data.length] = (byte)0xF7;

        // VCED
        result[3] = new byte[101];
        data = new byte[93];
        int pos = 0;
        for(int i = 0; i < vcedParameters.length - 16; i++)  // no name, no extra gunk
            {
            if (vcedParameters[i].endsWith("keyvelocitysensitivity"))  // gotta break out the sign for the V50
                {
                data[pos++] = (byte)(model.get(vcedParameters[i]) | 
                    (model.get(vcedParameters[i].substring(0,9) + "v" + vcedParameters[i].substring(9) + "sign") << 3));
                }
            else
                {
                data[pos++] = (byte)(model.get(vcedParameters[i]));
                }
            }
        
        String name = model.get("name", "INIT VOICE") + "          ";
        
        for(int i = 0; i < 10; i++)
            {
            data[pos++] = (byte)(name.charAt(i));
            }

        for(int i = vcedParameters.length - 6; i < vcedParameters.length; i++)
            {
            data[pos++] = (byte)(model.get(vcedParameters[i]));
            }
        
        result[3][0] = (byte)0xF0;
        result[3][1] = 0x43;
        result[3][2] = (byte)(getChannelOut());
        result[3][3] = 0x03;
        result[3][4] = 0x00;
        result[3][5] = 0x5D;
        System.arraycopy(data, 0, result[3], 6, data.length);
        result[3][6 + data.length] = produceChecksum(data);
        result[3][7 + data.length] = (byte)0xF7;

        return result;
        }

    /** Generate a TX81Z checksum of the data bytes */
    byte produceChecksum(byte[] bytes, int start)
        {
        //      The TX81Z manual says the checksum is the
        //              "Twos complement of the lower 7 bits of the sum of all databytes".
        //
        //              Apparently this is mistaken.  Based on the function "Snapshot_Checksum" here...
        //              https://raw.githubusercontent.com/mgregory22/tx81z-programmer/master/src/snapshot.c
        //
        //              It may be otherwise.  So here's my shot.

        int checksum = 0;
        for(int i = start; i < bytes.length; i++)
            checksum = (checksum + bytes[i]) & 255;
        return (byte)((256 - checksum) & 127);
        }

    /** Generate a TX81Z checksum of the data bytes */
    byte produceChecksum(byte[] bytes)
        {
        return produceChecksum(bytes, 0);
        }

    public void performRequestDump(Model tempModel, boolean changePatch)
        {
        if (tempModel == null)
            tempModel = getModel();

        // We ALWAYS change the patch no matter what.  We have to.
        performChangePatch(tempModel);
        tryToSendSysex(requestDump(tempModel));
        }

    // We have to force a change patch always because we're doing the equivalent of requestCurrentDump here
    public boolean getAlwaysChangesPatchesOnRequestDump() { return true; }

    public byte[] requestDump(Model tempModel) 
        {
        // since performRequestDump ALWAYS changes the patch, we might
        // as well just call requestCurrentDump() here 
        return requestCurrentDump(); 
        }
    

    // DX21 Dump Request                F0 43 2CH 03 F7
    // DX27/100 Dump Request            F0 43 2CH 03 F7
    // DX11 Dump Request                F0 43 2CH 7E "LM  8023AE" F7    [ACED2 + ACED + VCED]
    // TX81Z Dump Request               F0 43 2CH 7E "LM  8976AE" F7    [ACED + VCED]           // It says 0CH but that's wrong
    // TQ5 Dump Request                 F0 43 2CH 7E "LM  8036EF" F7    [EFEDS + ACED2 + ACED + VCED]
    // V50 Dump Request                 F0 43 2CH 7E "LM  8037AE" F7    [ACED3 + ACED2 + ACED + VCED]

    // DX21 Change Patch                [Though there are banks, we can only access the first one, just 32 values]
    // DX27/100 Dump Request            [We can access voices 0...32 in banks I, A, B, C, D, but only write to I]
    // DX11 Dump Request                [We can access voices 0...31 in banks I, A, B, C, D, but only write to I]
    // TX81Z Dump Request               [We can access voices 0...32 in banks I, A, B, C, D, but only write to I]
    // TQ5 Dump Request                 [We can access voices 0...99.  There are three BANKS, Internal, Preset, and Card.]
    // V50 Dump Request                 [PC 122 to switch to Internal, 123 for Card, 124 for Preset.  Then 0...99 for the voice in question]

    public byte[] requestCurrentDump()
        {
        byte channel = (byte)(getChannelOut());
        switch (getSynthType())
            {
            case TYPE_DX21: 
            case TYPE_DX27_DX100:
                {
                return new byte[] { (byte)0xF0, 0x43, (byte)(32 + channel), 0x03, (byte)0xF7 }; 
                }
            case TYPE_DX11:
                {
                return new byte[] { (byte)0xF0, 0x43, (byte)(32 + channel), 0x7E, 
                    (byte)'L', (byte)'M', (byte)' ', (byte)' ',
                    (byte)'8', (byte)'0', (byte)'2', (byte)'3',
                    (byte)'A', (byte)'E', (byte)0xF7 }; 
                }
            case TYPE_TX81Z:
                {
                return new byte[] { (byte)0xF0, 0x43, (byte)(32 + channel), 0x7E, 
                    (byte)'L', (byte)'M', (byte)' ', (byte)' ',
                    (byte)'8', (byte)'9', (byte)'7', (byte)'6',
                    (byte)'A', (byte)'E', (byte)0xF7 }; 
                }
            case TYPE_TQ5_YS100_YS200_B200:
                {
                return new byte[] { (byte)0xF0, 0x43, (byte)(32 + channel), 0x7E, 
                    (byte)'L', (byte)'M', (byte)' ', (byte)' ',
                    (byte)'8', (byte)'0', (byte)'3', (byte)'6',
                    (byte)'E', (byte)'F', (byte)0xF7 }; 
                }
            case TYPE_V50:
                {
                return new byte[] { (byte)0xF0, 0x43, (byte)(32 + channel), 0x7E, 
                    (byte)'L', (byte)'M', (byte)' ', (byte)' ',
                    (byte)'8', (byte)'0', (byte)'7', (byte)'3',
                    (byte)'A', (byte)'E', (byte)0xF7 }; 
                }
            }
        System.err.println("Warning (Yamaha4Op): Invalid synth type in requestCurrentDump(): " + getSynthType());
        return new byte[] { }; // just in case
        }

    public int getPauseAfterSendAllParameters() { return 50; }                      // also goes for write patch
        
    public int getPauseAfterSendOneParameter() { return 50; }
    
    public int getPauseAfterChangePatch() { return 100; }               // TX81Z fails if it's less than 50 or so
    
    final static int FAIL = -1;

    public static int findSysexLength(byte[] data, int pos)
        {
        for(int i = pos; i < data.length; i++)
            {
            if (data[i] == (byte)0xF7)
                return i + 1;
            }
        return FAIL;
        }
    
    ArrayList<byte[]> resultsSoFar = new ArrayList<byte[]>();
        
    public void doRequestMerge(double percentage)
        {
        resultsSoFar = new ArrayList<byte[]>();
        super.doRequestMerge(percentage);
        }

    // gotta get two merge results
    public int merge(byte[] data, double probability)
        {
        resultsSoFar.add(data);
        if (data.length == 101 &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x43 &&
            // don't care about 2, it's the channel
            data[3] == (byte)0x03 &&
            data[4] == (byte)0x00 &&
            data[5] == (byte)0x5D)                      // VCED, flatten
            {
            // Flatten
            int count = 0;
            for(int i = 0; i < resultsSoFar.size(); i++)
                {
                count += resultsSoFar.get(i).length;
                }
            byte[] total = new byte[count];
            count = 0;
            for(int i = 0; i < resultsSoFar.size(); i++)
                {
                byte[] d = resultsSoFar.get(i);
                System.arraycopy(d, 0, total, count, d.length);
                count += d.length;
                }
            return super.merge(total, probability);
            }
        else
            {
            return PARSE_INCOMPLETE;
            }
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
        
    public static String getSynthName() { return "Yamaha 4-Op FM"; }



    // DX21 Change Patch                [Though there are banks, we can only access the first one, just 32 values]
    // DX27/100 Dump Request                    [We can access voices 0...23 in banks I, A, B, C, D, but only write to I]
    // DX11 Dump Request                [We can access voices 0...31 in banks I, A, B, C, D, but only write to I]
    // TX81Z Dump Request               [We can access voices 0...32 in banks I, A, B, C, D, but only write to I]
    // TQ5 Dump Request                 [We can access voices 0...99.  There are three BANKS, Internal, Preset, and Card.]
    // V50 Dump Request                 [We can access voices 0...99.  There are three BANKS, Internal, Preset, and Card.]

    // DX21 Change Patch                PC
    // DX27/100 Dump Request            PC
    // DX11 Dump Request                Unknown: maybe modify slot 127 in PC table to PC value, press SINGLE, send PC 127.  See page 50 of service manual
    // TX81Z Dump Request               Modify slot 127 in PC table to PC value, press PLAY/PERFORM, send PC 127
    // TQ5 Dump Request                 Send command to press the "preset", "card", or "internal" button, then PC  
    // V50 Dump Request                 [PC 122 to switch to Internal, 123 for Card, 124 for Preset.  Then 0...99 for the voice in question]

    public void changePatch(Model tempModel) 
        {
        int bank = tempModel.get("bank");
        int number = tempModel.get("number");

        switch (getSynthType())
            {
            case TYPE_DX21:
                {
                if (number >= 32)
                    {
                    System.err.println("Warning (Yamaha4Op): Patch number is invalid (" + number + ", changing to " + (number % 32));
                    number = number % 32;
                    }
                tryToSendMIDI(buildPC(getChannelOut(), number));
                }
            case TYPE_DX27_DX100:
                {
                if (number >= 24)
                    {
                    System.err.println("Warning (Yamaha4Op): Patch number is invalid (" + number + ", changing to " + (number % 24));
                    number = number % 24;
                    }
                number = number + bank * 24;
                tryToSendMIDI(buildPC(getChannelOut(), number));
                }
            break;
            case TYPE_DX11:
            case TYPE_TX81Z:
                {
                // [Note: I don't know if this will work for the DX11, but I'm taking a shot here.]
                // 
                // A program change in the TX81Z is a complicated affair.  We need to do three things:
                //
                // 1. Modify a slot in the program change table to the patch we want.  We'll modify slot 127.
                //
                // 2. At this point the TX81Z is in a strange "I got edited via MIDI" mode.  We need to get
                //    out of that and into standard program mode.  We do this by using sysex commands to virtually press
                //    the PLAY/PERFORM switch.
                //
                // 3. Now we're either in PLAY mode or we're in PERFORM mode.  At this point we send a PC 127, which
                //    causes the system to look up slot 127 in its table, discover it's a performance patch,
                //    and switch to that, while also changing to PERFORM mode. 

                // Change program change table position 127 to our desired patch
                int val = bank * 32 + number;
                byte lo = (byte)(val & 127);
                byte hi = (byte)(val >>> 7);
                byte[] table = new byte[9];
                table[0] = (byte)0xF0;
                table[1] = (byte)0x43;
                table[2] = (byte)(16 + getChannelOut());
                table[3] = (byte)0x10;
                table[4] = (byte)127;  // really!
                table[5] = (byte)127;  // we're changing table position 127
                table[6] = hi;
                table[7] = lo;
                table[8] = (byte)0xF7;
                tryToSendSysex(table);

                // Instruct the TX81Z to press its "PLAY/PERFORM" button.  Or "SINGLE" on the DX11
                byte PP = getSynthType() == TYPE_TX81Z ? (byte) 68 : (byte) 118;                // 119 is "PERFORM", 118 is "SINGLE"
                byte VV = (byte) 0;
                byte[] data = new byte[] { (byte)0xF0, (byte)0x43, (byte)(16 + getChannelOut()), REMOTE_SWITCH_GROUP, PP, (byte)0x7F, (byte)0xF7 };
                tryToSendSysex(data);

                // Do the program change to program 127
                tryToSendMIDI(buildPC(getChannelOut(), 127));
                }
            break;
            case TYPE_TQ5_YS100_YS200_B200:
                {
                if (bank > 3) 
                    {
                    System.err.println("Warning (Yamaha4Op): bank is invalid (" + bank + "), changing to 0");
                    bank = 0;
                    }
                                
                if (number >= 100)
                    {
                    System.err.println("Warning (Yamaha4Op): Patch number is invalid (" + number + ", changing to " + (number % 100));
                    number = number % 100;
                    }
                        
                // First we'll attempt to switch to the right bank by pressing magic butons
                byte[] data = new byte[] { (byte)0xF0, (byte)0x43, (byte)(16 + getChannelOut()), TQ5_REMOTE_SWITCH_GROUP,
                                
                    // documentation is wrong here.  The manual says 116 = card 117 = user 118 = preset
                    // but in fact it's 116 = preset 117 = user 118 = card
                    (byte)(bank == 0 ? 116 :                // user
                            (bank == 1 ? 117 :      // preset
                            118)),                  // card
                    (byte)127, (byte)0xF7 };
                tryToSendSysex(data);
                                
                // Do the program change
                tryToSendMIDI(buildPC(getChannelOut(), number));
                }
            break;                  
            case TYPE_V50:
                {
                if (bank > 3) 
                    {
                    System.err.println("Warning (Yamaha4Op): bank is invalid (" + bank + "), changing to 0");
                    bank = 0;
                    }
                                
                if (number >= 100)
                    {
                    System.err.println("Warning (Yamaha4Op): Patch number is invalid (" + number + ", changing to " + (number % 100));
                    number = number % 100;
                    }
                
                // We do two PCs
                // First to change banks, this is a weird way to do it.
                tryToSendMIDI(buildPC(getChannelOut(), bank + 122));
                // Next to select the patcb                
                tryToSendMIDI(buildPC(getChannelOut(), number));
                }
            break;                  
            }
                
        
        // we assume that we successfully did it
        if (!isMerging())  // we're actually loading the patch, not merging with it
            {
            setSendMIDI(false);
            model.set("number", number);
            model.set("bank", bank);
            setSendMIDI(true);
            }
        }
    
    public String getPatchName(Model model) { return model.get("name", "INIT VOICE"); }


    // DX21 Change Patch                [Though there are banks, we can only access the first one, just 32 values]
    // DX27/100 Dump Request    [We can access voices 0...23 in banks I, A, B, C, D, but only write to I]
    // DX11 Dump Request                [We can access voices 0...31 in banks I, A, B, C, D, but only write to I]
    // TX81Z Dump Request               [We can access voices 0...32 in banks I, A, B, C, D, but only write to I]
    // TQ5 Dump Request                 [We can access voices 0...99.  There are three BANKS, Internal, Preset, and Card.]

    public Model getNextPatchLocation(Model model)
        {
        int bank = model.get("bank");
        int number = model.get("number");
        
        int type = getSynthType();
        int maxNumber =
            (type == TYPE_DX21 ? 32 :
                (type == TYPE_DX27_DX100 ? 24 :
                    (type == TYPE_DX11 ? 32 :
                        (type == TYPE_TX81Z ? 32 :
                        100))));                // TQ5 and V50

        int maxBank =
            (type == TYPE_DX21 ? 1 :
                (type == TYPE_DX27_DX100 ? 5 :
                    (type == TYPE_DX11 ? 5 :
                        (type == TYPE_TX81Z ? 5 :
                        3))));                  // TQ5 and V50
                                                
        number++;
        if (number >= maxNumber)
            {
            bank++;
            number = 0;
            if (bank >= maxBank)
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
        
        int number = model.get("number") + 1;
        
        int type = getSynthType();
        if (type == TYPE_DX27_DX100 || type == TYPE_DX11 || type == TYPE_TX81Z)          
            return BANKS[model.get("bank")] + (number > 9 ? "" : "0") + number;
        else if (type == TYPE_TQ5_YS100_YS200_B200)
            {
            number -= 1;    // we start at 00
            int bank = model.get("bank");
            if (bank > 3) bank = 0;
            return TQ5_BANKS[bank] + " " + (number > 9 ? "" : "0") + number;
            }
        else if (type == TYPE_V50)
            {
            number -= 1;    // we start at 00
            int bank = model.get("bank");
            if (bank > 3) bank = 0;
            return V50_BANKS[bank] + " " + (number > 9 ? "" : "0") + number;
            }
        else
            return "" + (number > 9 ? "" : "0") + number;
        }
        

    public Object adjustBankSysexForEmit(byte[] data, Model model, int bank)
        { 
        data[2] = (byte) getChannelOut();
        return data; 
        }


    public boolean testVerify(Synth synth2, String key, Object val1, Object val2)
        {
        if (key.endsWith("frequencyfine"))  // this gets restricted to 7 if frequencycoarse is small, resulting in sanitycheck errors
            return true;
        if (((Yamaha4Op)synth2).getSynthType() == TYPE_V50)
            {
            // Obviously won't have EFEDS parameters
            return (
                key.equals("effectpreset") ||
                key.equals("effecttime") ||
                key.equals("effectbalance"));                   
            }
        else
            {
            // Obviously won't have ACED3 parameters
            return (
                key.equals("veffectparam1") ||
                key.equals("veffectparam2") ||
                key.equals("veffectparam3") ||
                key.equals("veffectsel") ||
                key.equals("veffectstereomix") ||
                key.equals("veffectbalance") ||
                key.equals("veffectoutlevel"));                 
            }
        }


    public String[] getBankNames()
        {
        if (getSynthType() == TYPE_DX21)
            {
            return new String[] { "A and B" };
            }
        else if (getSynthType() == TYPE_DX27_DX100 ||
            getSynthType() == TYPE_TX81Z ||
            getSynthType() == TYPE_DX11)
            {
            return new String[] { "I", "A", "B", "C", "D" };
            }
        else if (getSynthType() == TYPE_TQ5_YS100_YS200_B200)
            {
            return new String[] { "Preset", "User", "Card" };
            }
        else if (getSynthType() == TYPE_V50)
            {
            return new String[] { "Internal", "Card", "Preset" };
            }
        else    // uh...
            {
            return new String[] { "Bank" };
            }
        }

    /** Return a list of all patch number names.  Default is { "Main" } */
    public String[] getPatchNumberNames()  
        {
        if (getSynthType() == TYPE_DX21)
            {
            String[] names = new String[32];
            for(int i = 0; i < 16; i++)
                {
                names[i] = "A " + (i + 1);
                names[i + 16] = "B " + (i + 1);
                }
            return names;
            }
        else if (getSynthType() == TYPE_DX21 || 
            getSynthType() == TYPE_DX27_DX100 || 
            getSynthType() == TYPE_TX81Z || 
            getSynthType() == TYPE_DX11 )
            {
            return buildIntegerNames(32, 1); 
            }
        else
            {
            return buildIntegerNames(32, 0); 
            }
        }
                
    /** Return a list whether patches in banks are writeable.  Default is { false } */
    public boolean[] getWriteableBanks() 
        {
        if (getSynthType() == TYPE_DX21)
            {
            return new boolean[] { true };
            }
        else if (getSynthType() == TYPE_DX27_DX100 ||
            getSynthType() == TYPE_TX81Z ||
            getSynthType() == TYPE_DX11)
            {
            return new boolean[] {  true, false, false, false, false };
            }
        else if (getSynthType() == TYPE_TQ5_YS100_YS200_B200)
            {
            return new boolean[] { false, true, true };
            }
        else if (getSynthType() == TYPE_V50)
            {
            return new boolean[] { true, true, false };
            }
        else    // uh...
            {
            return new boolean[] {  true  };
            }
        }

    public boolean getSupportsBankWrites() { return true; }

    public int getPatchNameLength() { return 10; }
        
    public boolean isValidPatchLocation(int bank, int num) 
        { 
        if (getSynthType() == TYPE_DX27_DX100)
            {
            return (num < 24);
            }
        else 
            {
            return true; 
            }
        }

    public int getValidBankSize(int bank)
        {
        if (getSynthType() == TYPE_DX27_DX100)
            {
            return 24;
            }
        else 
            {
            return super.getValidBankSize(bank); 
            }
        }
    
    public byte[] requestBankDump(int bank) 
        {
        return new byte[] { (byte)0xF0, 0x43, (byte)(0x20 + getChannelOut()), 0x04, (byte)0xF7 }; 
        }

    public int getRequestableBank() 
        { 
        return 0;               // can only request internal bank
        }

    public boolean librarianTested() { return true; }
    }
