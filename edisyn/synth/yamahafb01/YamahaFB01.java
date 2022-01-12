/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.yamahafb01;

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


/**h
   A patch editor for the Yamaha FB-01.
        
   @author Sean Luke
*/

/**
   The sysex for the Yamaha FB01 is documented very poorly. There are two sources:
   the User Manual and the Service Manual, and both of them are highly cryptic,
   missing (different!) procedures, rife with amazing errors, and contradictory.  
   Fun fact: the bulk voice
   dump commands do not even explain that the first "packet" in the sysex is in fact
   the *bank name*, a concept not even brought up in either manual.  
**/


public class YamahaFB01 extends Synth
    {
    /// Various collections of parameter names for pop-up menus
    
    public static final String[] PITCH_MOD_CONTROLS = { "Off", "Aftertouch", "Pitch Wheel", "Breath Controller", "Foot Controller" };    
    public static final String[] LFO_WAVES = { "Sawtooth", "Square", "Triangle", "Sample and Hold" };
    public static final String[] KEYBOARD_LEVEL_SCALING_TYPES = new String[] { "1", "2", "3", "4" };
    public static final String[] AMPLITUDE_MODULATION = new String[] { "Modulator (Off)", "Carrier (On)" };
    public static final String[] WRITABLE_BANKS = { "1 (A)", "2 (B)" };
    public static final String[] BANKS = { "1 (A)", "2 (B)", "3 (ROM 1)", "4 (ROM 2)", "5 (ROM 3)", "6 (ROM 4)", "7 (ROM 5)" };

    // It is not fully clear that these are the algorithms.  I suspect they are for two reasons:
    // First, I believe the FB01 uses the same FM chip as the TX81Z, so it's likely to use
    // the same algorithm set.  Second, MidiQuest displays these algorithms, though MidiQuest makes a
    // lot of mistakes overall, so that's not a guarantee of correctness.  As the
    // FB01 manual has no information at all on it, I must just make this assumption.
        
    public static final ImageIcon[] ALGORITHM_ICONS = 
        {
        new ImageIcon(YamahaFB01.class.getResource("Algorithm1.png")),
        new ImageIcon(YamahaFB01.class.getResource("Algorithm2.png")),
        new ImageIcon(YamahaFB01.class.getResource("Algorithm3.png")),
        new ImageIcon(YamahaFB01.class.getResource("Algorithm4.png")),
        new ImageIcon(YamahaFB01.class.getResource("Algorithm5.png")),
        new ImageIcon(YamahaFB01.class.getResource("Algorithm6.png")),
        new ImageIcon(YamahaFB01.class.getResource("Algorithm7.png")),
        new ImageIcon(YamahaFB01.class.getResource("Algorithm8.png"))
        };

    // Unlike the D-110, we're going to permit only one emit location: slot 0.
    public static final byte EMIT_LOCATION = 0;
        
    public YamahaFB01()
        {
        model.set("bank", 0);
        model.set("number", 0);
                
        /// SOUND PANEL
                
        JComponent soundPanel = new SynthPanel(this);
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        hbox.addLast(addLFO(Style.COLOR_B()));
        vbox.add(hbox);
        
        
        vbox.add(addGlobal(Style.COLOR_A()));
        
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
        
        model.set("name", "INIT");
        
        loadDefaults();     
        }
                
    public JFrame sprout()
        {
        JFrame frame = super.sprout();
        transmitTo.setEnabled(false);
        addYamahaFB01Menu();
        return frame;
        }         

    public void addYamahaFB01Menu()
        {
        JMenu menu = new JMenu("FB-01");
        menubar.add(menu);
        JMenuItem setupTestPatchMenu = new JMenuItem("Set up Configuration for Instrument 1");
        setupTestPatchMenu.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                setupTestPatch();
                }
            });
        menu.add(setupTestPatchMenu);
        JMenuItem turnMenuProtectOffMenu = new JMenuItem("Turn Memory Protect Off");
        turnMenuProtectOffMenu.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                turnMemoryProtectOff();
                }
            });
        menu.add(turnMenuProtectOffMenu);
        }

    /// This site makes the following claims that we might wish to look into:
    /// https://www.vogons.org/viewtopic.php?p=362894#p362894
    /// 
    ///     F0 43 75 00 10 21 00 F7 - Turns memory-protect off
    /// F0 43 75 00 10 20 00 F7 - Sets the system channel to 1

    public void turnMemoryProtectOff()
        {
        tryToSendSysex(new byte[] { (byte)0xF0, 0x43, 0x75, 0x00, 0x10, 0x21, 0x00, (byte)0xF7 });
        }

    public void setupTestPatch()
        {
        if (tuple == null)
            if (!setupMIDI(tuple))
                return;

        if (tuple != null)
            {
            final YamahaFB01Multi synth = new YamahaFB01Multi();
            synth.tuple = new Midi.Tuple(tuple, synth.buildInReceiver(), synth.buildKeyReceiver(), synth.buildKey2Receiver());
            if (synth.tuple != null)
                {
                synth.loadDefaults();
                synth.getModel().set("name", "Edisyn");
                synth.getModel().set("op" + 1 + "midichannel", getChannelOut());
               
                /*
                  for(int i = 1; i <= 8; i++)
                  {
                  synth.getModel().set("op" + i + "outputlevel", 0);
                  synth.getModel().set("op" + i + "numberofnotes", 0);
                  synth.getModel().set("op" + i + "midichannel", (getChannelOut() + 1) % 16);  // so nobody is getChannelOut()
                  }
                
                  // prepare part1
                  if (part1)
                  {
                  //synth.getModel().set("op" + 1 + "outputlevel", 100);
                  //synth.getModel().set("op" + 1 + "numberofnotes", 8);
                  }
                */
     
                synth.sendAllParameters();
                sendAllParameters();
                }
            }
        }


    public String getDefaultResourceFileName() { return "YamahaFB01.init"; }
    public String getHTMLResourceFileName() { return "YamahaFB01.html"; }

    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        JComboBox bank = new JComboBox(BANKS);
        bank.setSelectedIndex(model.get("bank"));
        if (writing)
            {
            bank = new JComboBox(WRITABLE_BANKS);
            bank.setSelectedIndex(0);
            }                        
        
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
                showSimpleError(title, "The Patch Number must be an integer 1...48");
                continue;
                }
            if (n < 1 || n > 48)
                {
                showSimpleError(title, "The Patch Number must be an integer 1...48");
                continue;
                }
                
            n--;
                                
            int i = bank.getSelectedIndex();
                        
            change.set("bank", i);
            change.set("number", n);
                        
            return true;
            }
        }
                                
    public JComponent addNameGlobal(Color color)
        {
        Category globalCategory = new Category(this, getSynthName(), color);
        //globalCategory.makeUnresettable();
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
                
        VBox vbox = new VBox();
        HBox hbox2 = new HBox();
        comp = new PatchDisplay(this, 10);
        hbox2.add(comp);
        vbox.add(hbox2);
        
        comp = new StringComponent("Patch Name", this, "name", 7, "Name must be up to 7 ASCII characters.")
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

        comp = new LabelledDial("User Code", this, "usercode", color, 0, 255);
        hbox.add(comp);

        /*
        // Not enough space to show the title
        hbox.addLast(Strut.makeHorizontalStrut(30));
        */
        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }


    public JComponent addGlobal( Color color)
        {
        Category category = new Category(this, "Global", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        comp = new LabelledDial("Algorithm", this, "algorithm", color, 0, 7, -1);
        model.removeMetricMinMax("algorithm");  // it's a set
        hbox.add(comp);
        
        hbox.add(Strut.makeHorizontalStrut(10));
        
        comp = new IconDisplay(null, ALGORITHM_ICONS, this, "algorithm", 104, 104);
        hbox.add(comp);

        hbox.add(Strut.makeHorizontalStrut(10));

        VBox vbox = new VBox();
        params = PITCH_MOD_CONTROLS;
        comp = new Chooser("Pitch Mod Control", this, "pitchmodcontrol", params);
        vbox.add(comp);
        comp = new CheckBox("Mono", this, "mono");
        vbox.add(comp);
        comp = new CheckBox("Left Output", this, "leftoutputenable");
        vbox.add(comp);
        comp = new CheckBox("Right Output", this, "rightoutputenable");
        vbox.add(comp);
        hbox.add(vbox);

        // the service manual says this is 0...6, but the sysex patches go to 7
        comp = new LabelledDial("Feedback", this, "feedback", color, 0, 7);
        hbox.add(comp);

        comp = new LabelledDial("Pitch Bend", this, "pitchhbendrange", color, 0, 12);
        ((LabelledDial)comp).addAdditionalLabel("Range");
        hbox.add(comp);

        comp = new LabelledDial("Portamento", this, "portamentotime", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Time");
        hbox.add(comp);

        comp = new LabelledDial("Transpose", this, "transpose", color, -128, 127) // two's complement
            {
            public boolean isSymmetric() { return true; }
            };
        hbox.add(comp);
        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addLFO(Color color)
        {
        Category category = new Category(this, "LFO", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = LFO_WAVES;
        comp = new Chooser("Wave", this, "lfowave", params);
        vbox.add(comp);

        comp = new CheckBox("Sync", this, "lfosync");
        vbox.add(comp);
        comp = new CheckBox("Load Data", this, "loadlfodata");
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Speed", this, "lfospeed", color, 0, 255);
        hbox.add(comp);
                
        comp = new LabelledDial("Pitch Mod", this, "lfopitchmodulationsensitivity", color, 0, 7);
        ((LabelledDial)comp).addAdditionalLabel("Sensitivity");
        hbox.add(comp);
        
        comp = new LabelledDial("Pitch Mod", this, "lfopitchmodulationdepth", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Depth");
        hbox.add(comp);
        
        comp = new LabelledDial("Amplitude Mod", this, "lfoamplitudemodulationsensitivity", color, 0, 3);
        ((LabelledDial)comp).addAdditionalLabel("Sensitivity");
        hbox.add(comp);
        
        comp = new LabelledDial("Amplitude Mod", this, "lfoamplitudemodulationdepth", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Depth");
        hbox.add(comp);
        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
    
    public JComponent addOperator(final int src, Color color)
        {
        final Category category = new Category(this, "Operator " + src, color);
        //        category.makePasteable("op" + src);
        category.makePasteable("op");

        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = KEYBOARD_LEVEL_SCALING_TYPES;
        comp = new Chooser("Keyboard Level Scaling Type", this, "op" + src + "keyboardlevelscalingtype", params);
        vbox.add(comp);

        params = AMPLITUDE_MODULATION;
        comp = new Chooser("Amplitude Modulation", this, "op" + src + "carrieram", params);
        vbox.add(comp);

        comp = new CheckBox("On", this,  "op" + src + "enable");
        vbox.add(comp);
        
        hbox.add(vbox);

        comp = new LabelledDial("Level", this, "op" + src + "level", color, 0, 127);
        hbox.add(comp);
        
        comp = new LabelledDial("Vel. Sensitivity", this, "op" + src + "velocitysensitivityforlevel", color, 0, 7);
        ((LabelledDial)comp).addAdditionalLabel("[Level]");
        hbox.add(comp);
        
        comp = new LabelledDial("Key Scaling", this, "op" + src + "keyboardlevelscalingdepth", color, 0, 15);
        ((LabelledDial)comp).addAdditionalLabel("[Level]");
        hbox.add(comp);
        
        comp = new LabelledDial("Key Scaling", this, "op" + src + "keyboardscalingadjustforlevel", color, 0, 15);
        ((LabelledDial)comp).addAdditionalLabel("[Level Adjust]");
        hbox.add(comp);
        
        comp = new LabelledDial("Key Scaling", this, "op" + src + "keyboardratescalingdepth", color, 0, 3);
        ((LabelledDial)comp).addAdditionalLabel("[Rate]");
        hbox.add(comp);
        
        // FIXME: is this really signed?  Or two's complement?  The manual says this is 0...7, but 
        // other editors suggest -3...+3
        // Several incoming patches set 7
        // Also several patches stored in the FB01 also set 7.  So I'm going with -3...+4,
        // dunno if that's right.
        comp = new LabelledDial("Detune", this, "op" + src + "detune", color, 0, 7, 3)
            {
            public boolean isSymmetric() { return true; }
            };
                
        hbox.add(comp);
        
        // FIXME: PatchBase does this as 0.5 for 0.  Dunno if this is true.
        comp = new LabelledDial("Frequency", this, "op" + src + "frequency", color, 0, 15)
            {
            public String map(int value)
                {
                if (value == 0) return "0.5";
                return "" + value;
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Ratio");
        hbox.add(comp);
        
        comp = new LabelledDial("Inharmonic", this, "op" + src + "inharmonic", color, 0, 3);
        ((LabelledDial)comp).addAdditionalLabel("Frequency");
        hbox.add(comp);
        
    
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
                
    public JComponent addEnvelope(final int envelope, Color color)
        {
        Category category = new Category(this, "Operator Envelope " + envelope, color);
        //        category.makePasteable("op" + envelope);
        category.makePasteable("op");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
               
        comp = new LabelledDial("Attack", this, "op" + envelope + "attackrate", color, 0, 31);
        ((LabelledDial)comp).addAdditionalLabel("Rate");
        hbox.add(comp);

        comp = new LabelledDial("Decay 1", this, "op" + envelope + "decay1rate", color, 0, 31);
        ((LabelledDial)comp).addAdditionalLabel("Rate");
        hbox.add(comp);

        comp = new LabelledDial("Sustain", this, "op" + envelope + "sustainlevel", color, 0, 15);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);

        comp = new LabelledDial("Decay 2", this, "op" + envelope + "decay2rate", color, 0, 31);
        ((LabelledDial)comp).addAdditionalLabel("Rate");
        hbox.add(comp);

        comp = new LabelledDial("Release", this, "op" + envelope + "releaserate", color, 0, 15);
        ((LabelledDial)comp).addAdditionalLabel("Rate");
        hbox.add(comp);
        
        comp = new LabelledDial("Vel. Sensitivity", this, "op" + envelope + "velocitysensitivityforattackrate", color, 0, 3);
        ((LabelledDial)comp).addAdditionalLabel("For Attack Rate");
        hbox.add(comp);

        // ADSR Sort of
        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "op" + envelope + "attackrate", "op" + envelope + "decay1rate", "op" + envelope + "decay2rate", "op" + envelope + "releaserate" },
            new String[] { null, null, "op" + envelope + "sustainlevel", "op" + envelope + "sustainlevel", null },
            new double[] { 0, 1.0/2, 1.0/2, 1.0/2, 1.0/2 },
            new double[] { 0, 1.0, 1.0 / 15.0, 1.0 / 30.0, 0 },
            new double[] { 0, (Math.PI/4/31), (Math.PI/4/31), (Math.PI/4/31), (Math.PI/4/15) })
            {
            public double preprocessXKey(int index, String key, double value)
                {
                if (key.equals("op" + envelope + "releaserate"))
                    {
                    return 15.0 - value;
                    }
                else
                    {
                    return 31.0 - value;
                    }
                }
            public double preprocessYKey(int index, String key, double value)
                {
                if (key.equals("op" + envelope + "sustainlevel"))
                    {
                    return 15.0 - value;
                    }
                else
                    {
                    return value;
                    }
                }
            };
        hbox.addLast(comp);
                
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    // According to here:
    // https://yamahadx.yahoogroups.narkive.com/5zxnjROM/fb01-dx100-compatibility
    // ... the FM operators are reversed, so 1-4 becomes 4-1.  This jibes with what MIDIQuest does.
     
    public Object[] emitAll(String key)
        {
        if (key.equals("bank") || key.equals("number"))
            return new Object[0];
                
        // The FB01 is ALL PACKED BYTE SYSEX, even for single parameters, ugh.
        // So we have to do custom emits for every single freakin' key.  :-(  :-(
        
        int param = 0;
        int val = 0;
        if (key.equals("op1enable") || key.equals("op2enable") || key.equals("op3enable") || key.equals("op4enable"))
            {
            param = 0x4B;
            val = ((model.get("op1enable") << 6) | (model.get("op2enable") << 5) | (model.get("op3enable") << 4) | (model.get("op4enable") << 3));
            }
        else if (key.startsWith("op"))
            {
            int op = StringUtility.getFirstInt(key);
            String k = key.substring(3);            // remove "op4" etc.
            if (k.equals("level"))
                {
                param = 0x50;
                val = model.get("op" + op + "level");
                }
            else if (k.equals("keyboardlevelscalingtype"))
                {
                return new Object[]
                    {
                    emitAll("op" + op + "velocitysensitivityforlevel")[0],
                    emitAll("op" + op + "detune")[0]
                    };
                }
            else if (k.equals("velocitysensitivityforlevel"))
                {
                param = 0x51;
                // get zeroth bit of velocitysensitivityforlevel, put at top
                val = (((model.get("op" + op + "keyboardlevelscalingtype") >>> 0) & 1) << 7) | 
                    model.get("op" + op + "velocitysensitivityforlevel");
                }
            else if (k.equals("keyboardlevelscalingdepth") || k.equals("keyboardscalingadjustforlevel"))
                {
                param = 0x52;
                val = (model.get("op" + op + "keyboardlevelscalingdepth") << 4) | 
                    model.get("op" + op + "keyboardscalingadjustforlevel");
                }
            else if (k.equals("detune") || k.equals("frequency"))
                {
                param = 0x53;
                // get first bit of velocitysensitivityforlevel, put at top
                val = (((model.get("op" + op + "keyboardlevelscalingtype") >>> 1) & 1) << 7) | 
                    (model.get("op" + op + "detune") << 4) | 
                    model.get("op" + op + "frequency");
                }
            else if (k.equals("keyboardratescalingdepth") || k.equals("attackrate"))
                {
                param = 0x54;
                val = (model.get("op" + op + "keyboardratescalingdepth") << 6) | 
                    model.get("op" + op + "attackrate");
                }
            else if (k.equals("carrieram") || k.equals("velocitysensitivityforattackrate") || k.equals("decay1rate"))
                {
                param = 0x55;
                val = (model.get("op" + op + "carrieram") << 7) | 
                    (model.get("op" + op + "velocitysensitivityforattackrate") << 5) | 
                    model.get("op" + op + "decay1rate");
                }
            else if (k.equals("inharmonic") || k.equals("decay2rate"))
                {
                param = 0x56;
                val = (model.get("op" + op + "inharmonic") << 6) | 
                    model.get("op" + op + "decay2rate");
                }
            else if (k.equals("sustainlevel") || k.equals("releaserate"))
                {
                param = 0x57;
                val = (model.get("op" + op + "sustainlevel") << 4) | 
                    model.get("op" + op + "releaserate");
                }
            else
                {
                System.err.println("Unknown Key " + key);
                return new Object[0];
                }
                
            // adjust for operator.  Note that operators are in reverse order
            param += (4 - op) * 8;
            }
        else
            {
            if (key.equals("name"))
                {
                // handle specially
                String name = model.get("name", "Init") + "       ";
                Object[] obj = new Object[7];
                for(int i = 0; i < 7; i++)
                    {
                    param = 0x40 + i;
                    val = (byte)(name.charAt(i) & 127);
                    // parameter change by System Channel + Instrument Number page 49 of user manual
                    byte[] data = new byte[] { (byte)0xF0, 0x43, 0x75, (byte)(getID() - 1), (byte)(24 + EMIT_LOCATION),
                        (byte)param, (byte)(val & 15), (byte)((val >>> 4) & 15), (byte)0xF7 };
                    obj[i] = data;
                    }
                return obj;
                }
            else if (key.equals("usercode"))
                {
                param = 0x47;
                val = (model.get(key));
                }
            else if (key.equals("lfospeed"))
                {
                param = 0x48;
                val = (model.get(key));
                }
            else if (key.equals("loadlfodata") || key.equals("lfoamplitudemodulationdepth"))
                {
                param = 0x49;
                val = ((model.get("loadlfodata") << 7) | model.get("lfoamplitudemodulationdepth"));
                }
            else if (key.equals("lfosync") || key.equals("lfopitchmodulationdepth"))
                {
                param = 0x4A;
                val = ((model.get("lfosync") << 7) | model.get("lfopitchmodulationdepth"));
                }
            else if (key.equals("leftoutputenable") || key.equals("rightoutputenable") || key.equals("feedback") || key.equals("algorithm"))
                {
                param = 0x4C;
                val = ((model.get("leftoutputenable") << 7) | (model.get("rightoutputenable") << 6) | (model.get("feedback") << 3) | model.get("algorithm"));
                }
            else if (key.equals("lfopitchmodulationsensitivity") || key.equals("lfoamplitudemodulationsensitivity"))
                {
                param = 0x4D;
                val = ((model.get("lfopitchmodulationsensitivity") << 4) | model.get("lfoamplitudemodulationsensitivity"));
                }
            else if (key.equals("lfowave"))
                {
                param = 0x4E;
                val = (model.get("lfowave") << 5);
                }
            else if (key.equals("transpose"))               // this is in two's complement
                {
                param = 0x4F;
                val = (model.get("transpose"));
                }
            else if (key.equals("mono") || key.equals("portamentotime"))
                {
                param = 0x7A;
                val = ((model.get("mono") << 7) | model.get("portamentotime"));
                }
            else if (key.equals("pitchmodcontrol") || key.equals("pitchhbendrange"))
                {
                param = 0x7B;
                val = ((model.get("pitchmodcontrol") << 4) | model.get("pitchhbendrange"));
                }
            else
                {
                System.err.println("Unknown Key " + key);
                return new Object[0];
                }
            }
            
        // parameter change by System Channel + Instrument Number page 49 of user manual
        byte[] data = new byte[] { (byte)0xF0, 0x43, 0x75, (byte)(getID() - 1), (byte)(24 + EMIT_LOCATION),
            (byte)param, (byte)(val & 15), (byte)((val >>> 4) & 15), (byte)0xF7 };
        return new Object[] { data };
        }




    public void parseOne(byte[] d)
        {
        int pos = 0;
        char[] chars = new char[7];
        for(int i = 0; i < 7; i++)
            {
            chars[i] = (char)d[pos++];
            }
        model.set("name", new String(chars));
        model.set("usercode", d[pos++] & 255);
        model.set("lfospeed", d[pos++] & 255);
        model.set("loadlfodata", (d[pos] >>> 7) & 1);
        model.set("lfoamplitudemodulationdepth", (d[pos] >>> 0) & 127);
        pos++;
        model.set("lfosync", (d[pos] >>> 7) & 1);
        model.set("lfopitchmodulationdepth", (d[pos] >>> 0) & 127);
        pos++;
        model.set("op1enable", (d[pos] >>> 6) & 1);
        model.set("op2enable", (d[pos] >>> 5) & 1);
        model.set("op3enable", (d[pos] >>> 4) & 1);
        model.set("op4enable", (d[pos] >>> 3) & 1);
        pos++;

        // The service manual has the left and right output enable flags,
        // but the user manual does not
        model.set("leftoutputenable", (d[pos] >>> 7) & 1);
        model.set("rightoutputenable", (d[pos] >>> 6) & 1);
        model.set("feedback", (d[pos] >>> 3) & 7);
        model.set("algorithm", (d[pos] >>> 0) & 7);
        pos++;
        model.set("lfopitchmodulationsensitivity", (d[pos] >>> 4) & 7);
        model.set("lfoamplitudemodulationsensitivity", (d[pos] >>> 0) & 3);
        pos++;
        model.set("lfowave", d[pos++] >>> 5);
        model.set("transpose", d[pos++]);
        
        for(int op = 4; op >= 1; op--)          // operators are reversed
            {
            model.set("op" + op + "level", d[pos++] & 127);
            // this is the most complex -- an operator split across two different bytes
            model.set("op" + op + "keyboardlevelscalingtype", ((d[pos] >>> 7) & 1) | (((d[pos + 2] >>> 7) & 1) << 1));
            model.set("op" + op + "velocitysensitivityforlevel", (d[pos] >>> 4) & 7);
            pos++;
            model.set("op" + op + "keyboardlevelscalingdepth", (d[pos] >>> 4) & 15);
            model.set("op" + op + "keyboardscalingadjustforlevel", (d[pos] >>> 0) & 15);
            pos++;
            model.set("op" + op + "detune", (d[pos] >>> 4) & 7);
            model.set("op" + op + "frequency", (d[pos] >>> 0) & 15);
            pos++;
            model.set("op" + op + "keyboardratescalingdepth", (d[pos] >>> 6) & 3);
            model.set("op" + op + "attackrate", (d[pos] >>> 0) & 31);
            pos++;
            model.set("op" + op + "carrieram", (d[pos] >>> 7) & 1);
            model.set("op" + op + "velocitysensitivityforattackrate", (d[pos] >>> 5) & 3);
            model.set("op" + op + "decay1rate", (d[pos] >>> 0) & 31);
            pos++;
            model.set("op" + op + "inharmonic", (d[pos] >>> 6) & 3);
            model.set("op" + op + "decay2rate", (d[pos] >>> 0) & 31);
            pos++;
            model.set("op" + op + "sustainlevel", (d[pos] >>> 4) & 15);
            model.set("op" + op + "releaserate", (d[pos] >>> 0) & 15);
            pos++;
            }
        
        pos += 10;              // reserved
        model.set("mono", (d[pos] >>> 7) & 1);
        model.set("portamentotime", (d[pos] >>> 0) & 127);
        pos++;
        model.set("pitchmodcontrol", (d[pos] >>> 4) & 7);
        model.set("pitchhbendrange", (d[pos] >>> 0) & 15);
        pos++;
        }
        
    
    //// BULK VOICE (48 PATCH) READ
    //// Both manuals are wrong or at least highly misleading.  The correct format is:
    ////
    //// F0
    //// 43
    //// 75
    //// 0n             n = "System Channel Number" (ID)
    //// 00
    //// 00
    //// 0m             m = Bank number
    //// 00             byte count
    //// 40             byte count
    //// 16 BYTES       Bank Name [nybblized 8 bytes, low first, then high]
    //// 48 ZEROS       "Reserved" [nybblized 24 bytes, low first, then high]
    //// CHECKSUM on the previous 16 bytes and 48 zeros
    //// 48 PACKETS
    //// F7
    ////
    ////
    //// We might also see:
    ////
    //// F0
    //// 43
    //// 0n             n = "System Channel Number" (ID)
    //// 0C
    //// 00             byte count
    //// 40             byte count
    //// 16 BYTES       Bank Name [nybblized 8 bytes, low first, then high]
    //// 48 ZEROS       "Reserved" [nybblized 24 bytes, low first, then high]
    //// CHECKSUM on the previous 16 bytes and 48 zeros
    //// 48 PACKETS
    //// F7
    ////
    ////
    //// Or....
    ////
    //// F0
    //// 43
    //// 75
    //// 0n             n = "System Channel Number" (ID)
    //// 0m             m = 8 + "Instrument Number"
    //// 00
    //// 00
    //// 1 PACKET
    //// F7
    ////
    ////
    //// A PACKET is the data for one voice, and defined as:
    //// 01             byte count
    //// 00             byte count
    //// 128 BYTES      Voice Data [nybblized 64 bytes, low first, then high] (Parameter List 6, page 19 Service Manual, or Voice Data Format, page 53 User Manual)
    //// CHECKSUM on the Voice Data
    
        
    public int parse(byte[] data, boolean fromFile)
        {
        if (data.length > 2 * 64 + 11)
            {
            int start = 0;
            if (data.length == 8 + 16 + 48 + 48 * (64 * 2 + 3) )       // bank 0
                {
                start = 7 + 16 + 48;
                }
            else
                {
                start = 10 + 16 + 48;
                }
                        
            // extract the names
            char[][] names = new char[48][7];
            for(int i = 0; i < 48; i++)
                {
                int pos = start + i * (2 * 64 + 3) + 2;
                for(int j = 0; j < 7; j++)
                    {
                    names[i][j] = (char)(data[pos + j * 2] | (data[pos + j * 2 + 1] << 4));
                    }
                }
            String[] n = new String[48];
            for(int i = 0; i < 48; i++)
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

            model.set("number", patchNum);
            model.set("bank", 0);                   // we don't know what the bank is in reality
            // denybblize
            byte[] d = new byte[64];
            for(int i = 0; i < d.length; i++)
                {
                d[i] = (byte)((data[start + patchNum * (2 * 64 + 3) + i * 2 + 2 + 1] << 4) | (data[start + patchNum * (2 * 64 + 3) + 2 + i * 2]));
                }
            parseOne(d);
            revise();
            return PARSE_SUCCEEDED;
            }
        else
            {
            // denybblize
            byte[] d = new byte[64];
            for(int i = 0; i < d.length; i++)
                {
                d[i] = (byte)((data[9 + i * 2 + 1] << 4) | (data[9 + i * 2]));
                }
                
            parseOne(d);
            revise();
            return PARSE_SUCCEEDED;
            }
        }
        
    
    //// To send a patch to current we need to
    //// do 1 voice bulk data
    
    //// To write a patch to RAM we need to
    //// send a patch to current
    //// write current to RAM
    
    public Object[] emitAll(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        if (tempModel == null)
            tempModel = getModel();
        
        int packetlen = 128;
        byte[] data = new byte[packetlen + 11];
        
        // 1 voice bulk data, page 16 of service manual
        // ...
        // instrument i voice data, page 57 of user manual
        //
        data[0] = (byte)0xF0;
        data[1] = 0x43;
        data[2] = 0x75;
        data[3] = (byte)(getID() - 1);
        data[4] = 8 + EMIT_LOCATION;
        data[5] = 0;
        data[6] = 0;
        // Service manual has data[7] = 0x01, data[8] = 0x00, which jibes with a packet length of 128
        data[7] = (byte)(packetlen >>> 7);
        data[8] = (byte)(packetlen & 127);
        data[data.length - 1] = (byte)0xF7;
        
        byte[] d = new byte[64];
        int pos = 0;
        
        String name = model.get("name", "       ") + "       ";
        for(int i = 0; i < 7; i++)
            {
            d[pos++] = (byte)(name.charAt(i) & 127);
            }
        
        d[pos++] = (byte)(model.get("usercode"));
        d[pos++] = (byte)(model.get("lfospeed"));
        d[pos++] = (byte)((model.get("loadlfodata") << 7) | model.get("lfoamplitudemodulationdepth"));
        d[pos++] = (byte)((model.get("lfosync") << 7) | model.get("lfopitchmodulationdepth"));
        d[pos++] = (byte)((model.get("op1enable") << 6) | (model.get("op2enable") << 5) | (model.get("op3enable") << 4) | (model.get("op4enable") << 3));

        // The service manual has the left and right output enable flags,
        // but the user manual does not
        d[pos++] = (byte)((model.get("leftoutputenable") << 7) | (model.get("rightoutputenable") << 6) | (model.get("feedback") << 3) | model.get("algorithm"));
        d[pos++] = (byte)((model.get("lfopitchmodulationsensitivity") << 4) | model.get("lfoamplitudemodulationsensitivity"));
        d[pos++] = (byte)(model.get("lfowave") << 5);
        d[pos++] = (byte)(model.get("transpose"));
        
        for(int op = 4; op >= 1; op--)          // operators are reversed
            {
            d[pos++] = (byte)(model.get("op" + op + "level"));
            d[pos++] = (byte)((((model.get("op" + op + "keyboardlevelscalingtype") >>> 0) & 1) << 7) | 
                (model.get("op" + op + "velocitysensitivityforlevel") << 4));
            d[pos++] = (byte)((model.get("op" + op + "keyboardlevelscalingdepth") << 4) | 
                model.get("op" + op + "keyboardscalingadjustforlevel"));
            d[pos++] = (byte)((((model.get("op" + op + "keyboardlevelscalingtype") >>> 1) & 1) << 7) | 
                (model.get("op" + op + "detune") << 4) | 
                model.get("op" + op + "frequency"));
            d[pos++] = (byte)((model.get("op" + op + "keyboardratescalingdepth") << 6) | 
                model.get("op" + op + "attackrate"));
            d[pos++] = (byte)((model.get("op" + op + "carrieram") << 7) | 
                (model.get("op" + op + "velocitysensitivityforattackrate") << 5) | 
                model.get("op" + op + "decay1rate"));
            d[pos++] = (byte)((model.get("op" + op + "inharmonic") << 6) | 
                model.get("op" + op + "decay2rate"));
            d[pos++] = (byte)((model.get("op" + op + "sustainlevel") << 4) | 
                model.get("op" + op + "releaserate"));
            }
                
        pos += 10;              // reserved
        d[pos++] = (byte)((model.get("mono") << 7) | model.get("portamentotime"));
        d[pos++] = (byte)((model.get("pitchmodcontrol") << 4) | model.get("pitchhbendrange"));
        pos+= 4;                // reserved

        // nybblize
        for(int i = 0; i < d.length; i++)
            {
            data[9 + i * 2] = (byte)(d[i] & 15);
            data[9 + i * 2 + 1] = (byte)((d[i] >>> 4) & 15);
            }

        // compute checksum
        data[data.length - 2] = (byte)produceChecksum(data, 9, 9 + 128);
        
        if (toWorkingMemory || toFile)
            {
            return new Object[] { data };
            }
        else
            {
            // we have to dump the data to current working memory, then
            // issue a special command (page 13 of service manual, #4 on left column,
            // "Store into voice RAM (voice RAM 1~96) of current voice")
            // which stores the data to voice RAM, duh.
            //
            // However there is an error in this command.  The manual says that the 
            // Operation Number is 0x00.  This is wrong, as it'd be indistinguishable
            // from Voice Bulk Data Dump (which also has an error in it, see elsewhere,
            // in this Java file).  Instead, it's actually supposed to be 0x40,
            // similar to "Configuration data store", which does the same thing only
            // for configurations.
            
            byte[] data2 = new byte[] {(byte)0xF0, 0x43, 0x75, (byte)(getID() - 1), (byte)(40 + EMIT_LOCATION),
                0x40, (byte)(tempModel.get("number") + tempModel.get("bank") * 48), (byte)0xF7 };
            return new Object[] { data, data2 };
            }
        }


    /** Generate a TX81Z checksum of the data bytes */
    byte produceChecksum(byte[] bytes, int start, int end)
        {
        //      The TX81Z manual says the checksum is the
        //              "Twos complement of the lower 7 bits of the sum of all databytes".
        //
        //              Apparently this is mistaken.  Based on the function "Snapshot_Checksum" here...
        //              https://raw.githubusercontent.com/mgregory22/tx81z-programmer/master/src/snapshot.c
        //
        //              It may be otherwise.  So here's my shot.

        int checksum = 0;
        for(int i = start; i < end; i++)
            checksum = (checksum + bytes[i]) & 255;
        return (byte)((256 - checksum) & 127);
        }
        
    
    public void performRequestDump(Model tempModel, boolean changePatch)
        {
        if (tempModel == null)
            tempModel = getModel();

        // we always have to change the patch
        changePatch(tempModel);
        tryToSendSysex(requestDump(tempModel));
        }

    public byte[] requestDump(Model tempModel) 
        {
        // since performRequestDump ALWAYS changes the patch, we might
        // as well just call requestCurrentDump() here 
        model.set("bank", tempModel.get("bank"));
        model.set("number", tempModel.get("number"));
        return requestCurrentDump(); 
        }
        
    public byte[] requestCurrentDump()
        {
        // Voice Bulk Data Dump (command 3 of left column), page 13, service manual
        // See the complaint elsewhere about "Store into Voice RAM" -- it appears that
        // some joker swapped the operation numbers of these two commands: it's supposed
        // to be 0x00 here and 0x40 there, but the manual says the opposite.  These were
        // showstopper documentation bugs.
        return new byte[] { (byte)0xF0, 0x43, 0x75, (byte)(getID() - 1), (byte)(40 + EMIT_LOCATION),
            0x00,           // user manual (p. 47) says 0x00, service manual says 0x40, user manual is right
            0x00, (byte)0xF7 }; 
        }
    
    public static final int MAXIMUM_NAME_LENGTH = 7;
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

        String nm = model.get("name", "INIT");
        String newnm = revisePatchName(nm);
        if (!nm.equals(newnm))
            model.set("name", newnm);
        }
        
    public static String getSynthName() { return "Yamaha FB-01"; }


    //// It is not clear how to change patches.  The PROGRAM CHANGE command
    //// changes a value from 1..48.  This might correspond to the current
    //// number of the patch of Instrument 1 for all we know.  However the FB-01
    //// does not respond to a BANK SELECT command to change the *bank* of that
    //// patch.  And changing the patch for other instruments is out of the question.
    ////
    //// I think what we will have to do is send a special CONFIGURATION PARAMETER CHANGE.
    //// To do this, we send two parameter change commands, one to change the bank of instrument
    //// 1, and one to change the voice number of instrument 1.
    
    public void changePatch(Model tempModel) 
        {
        byte[] bankData = new byte[] 
            { 
            // Parameter Change (1 byte), command #1 of left column, page 13, service manual
            (byte)0xF0, 0x43, 0x75, (byte)(getID() - 1), (byte)(24 + EMIT_LOCATION),
            0x04,   // "bank", instrument 1
            (byte)(tempModel.get("bank")),
            (byte)0xF7
            };
                
        byte[] numberData = new byte[] 
            { 
            // Parameter Change (1 byte), command #1 of left column, page 13, service manual
            (byte)0xF0, 0x43, 0x75, (byte)(getID() - 1), (byte)(24 + EMIT_LOCATION),
            0x05,   // "number" (voice), instrument 1
            (byte)(tempModel.get("number")),
            (byte)0xF7
            };
        
        // dunno if this is gonna work.
        tryToSendMIDI(new Object[] { bankData, new Integer(getPauseAfterChangePatch()), numberData });
        }
    
    /// FIXME -- don't know what this should be, let's say 150
    public int getPauseAfterChangePatch() { return 150; }
    
    public String getPatchName(Model model) { return model.get("name", "INIT"); }

    public Model getNextPatchLocation(Model model)
        {
        int bank = model.get("bank");
        int number = model.get("number");
        
        number++;
        if (number >= 24)
            {
            bank++;
            number = 0;
            if (bank >= 8)
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
        return BANKS[model.get("bank")] + " : " + (number > 9 ? "" : "0") + number;
        }
        
    //// This corresponds to the FB01's so-called "System MIDI Channel"
    public byte getID() 
        { 
        try 
            { 
            byte b = (byte)(Byte.parseByte(tuple.id));
            if (b >= 1 && b <= 16) return b;
            }
        catch (NullPointerException e) { } // expected.  Happens when tuple's not built yet
        catch (NumberFormatException e) { Synth.handleException(e); }
        return 1;
        }
        
    public String reviseID(String id)
        {
        try 
            { 
            byte b =(byte)(Byte.parseByte(id)); 
            if (b >= 1 && b <= 16) return "" + b;
            } 
        catch (NumberFormatException e) { }             // expected
        return "" + getID();
        }





    ///// BANK SYSEX OPTIONS
    /////
    ///// The FB01 is unusual in three respects when it comes to Bank Sysex.
    /////
    ///// 0. Bank sysex isn't for the whole synthesizer, but rather for just a single bank in
    /////    the synthesizer.  Though there are 9 banks we can read from, there are only two
    /////    banks we can write to.  This means that we need to allow the user to select which
    /////    bank he wants, and modify the sysex file to reflect this.
    /////
    ///// 1. There are two different sysex commands for bank sysex.  One stipulates Voice Bank 0.
    /////    The other stipulates Voice Bank X (though there are really only two voice banks we can
    /////    write to!  0 and 1.  Of course, we have 9 we can read from).
    /////
    ///// 2. Banks can have names.  That's kind of weird.
    /////
    ///// So the code below (1) offers the user a bank name that he can change, (2) offers the user
    ///// two different banks he can choose to write to, and (2) modifies all bank sysex uploads to use
    ///// the Voice Bank X sysex version.
        

    /// We add a bank name field so the user can change it.
    JTextField nameField;
    public JComponent getAdditionalBankSysexOptionsComponents(byte[] data, String[] names) 
        {
        int pos = 6;
        if (data[2] == 0x75)
            {
            pos = 9;
            }
        char[] n = new char[8];
        for(int i = 0; i < 8; i++)
            {
            n[i] = (char)(data[pos + i * 2] | (data[pos + i * 2 + 1] << 4));
            }
        
        Box box = new Box(BoxLayout.X_AXIS);
        box.add(new JLabel("Bank Name  "));
        nameField = new SelectedTextField(new String(n));
        box.add(nameField);
        return box;
        }

    public String[] getBanksForBankSysex(byte[] data, Model model) 
        { 
        return WRITABLE_BANKS;
        }

    public int getDefaultBankForBankSysex(byte[] data, Model model) 
        { 
        if (data[2] == 0x75)
            {
            if (data[6] < 2) return data[6];
            }
        return 0;
        }

    public Object adjustBankSysexForEmit(byte[] data, Model model, int bank)
        {
        /*
        // Extract the chosen bank
        int b = model.get("bank", 0);
        if (b > 1) 
        {
        System.err.println("Warning (YamahaFB01): " + "Bad bank " + b);
        b = 0;
        }
        */
            
        // First, we're going to convert this entirely to Voice Bank X
        if (data[2] != 0x75)
            {
            byte[] data2 = new byte[6363];
            data2[0] = (byte) 0xF0;
            data2[1] = 0x43;
            data2[2] = 0x75;
            data2[3] = (byte)(getID() - 1);  
            data2[4] = 0;
            data2[5] = 0;
            data2[6] = (byte) bank;
            data2[7] = 0;
            data2[8] = 0x40;
            System.arraycopy(data, 6, data2, 9, 6354);
            data = data2;
            }
        else
            {
            data[3] = (byte) (getID() - 1);
            data[6] = (byte) bank;
            }
        
        // Change the bank patch name
        int pos = (data[2] == 0x75 ? 9 : 6);
        String name = revisePatchName(nameField.getText()) + "        ";
        for(int i = 0; i < 8; i++)
            {
            data[pos + i * 2] = (byte)(name.charAt(i) & 15);
            data[pos + i * 2 + 1] = (byte)((name.charAt(i) >>> 4) & 7);
            }
                        
        // Break into pieces: the FB01 requires that each packet be separated by 100ms
        byte[][] d = new byte[49][];
        int start = 74;
                
        d[0] = new byte[start];
        System.arraycopy(data, 0, d[0], 0, start);              // Bank packet
                
        for(int i = 0; i < 48; i++)
            {
            if (i == 47)            // last one
                {
                d[i + 1] = new byte[1 + 131];
                System.arraycopy(data, start + i * 131 , d[i + 1], 0, 1 + 131);         // include 0xF7
                }
            else
                {
                d[i + 1] = new byte[131];
                System.arraycopy(data, start + i * 131 , d[i + 1], 0, 131);
                }
            }

        Object[] div = Midi.DividedSysex.create(d);             // build the divided sysex
        /*
          for(int i = 0; i < div.length; i++)
          {
          System.err.println("\n" + i);
          System.err.println(Midi.DividedSysex.toString((SysexMessage)div[i]));
          }
        */
                
        Object[] obj = new Object[49 * 2 - 1];                  // insert 120ms (100ms is the minimum) in-between the 49 divided packets
        for(int i = 0; i < d.length; i++)
            {
            obj[i * 2] = div[i];
            if (i < d.length - 1)           // we're not the last one
                obj[i * 2 + 1] = new Integer(120);
            }

        return obj; 
        }

    public String[] getBankNames() { return BANKS; }

	/** Return a list of all patch number names.  Default is { "Main" } */
	public String[] getPatchNumberNames()  { return buildIntegerNames(48, 1); }

	/** Return a list whether patches in banks are writeable.  Default is { false } */
	public boolean[] getWriteableBanks() { return new boolean[] { true, true, false, false, false, false, false }; }

	/** Return a list whether individual patches can be written.  Default is FALSE. */
	public boolean getSupportsPatchWrites() { return true; }

	public int getPatchNameLength() { return 7; }

    public byte[] requestBankDump(int bank) 
    	{ 
    	// The manual says that the banks are 0...6, but the service manual says that they're 1...7
    	// Given that the service manual has multiple other errors, I'm going with 0...6
    	return new byte[] { (byte)0xF0, (byte)0x43, (byte)0x75, (byte)getChannelOut(), 0x20, 0x00, (byte)bank, (byte)0xF7 };
    	}
    }
