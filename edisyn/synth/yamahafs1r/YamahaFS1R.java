/***
    Copyright 2020 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.yamahafs1r;

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
   A patch editor for the Yamaha FS1R.
   
   <p>Some portions of this code were copied, with permission, from the source code of FS1REditor by K_Take (https://synth-voice.sakura.ne.jp/fs1r_editor_english.html)
   to whom I am very grateful. 
        
   @author Sean Luke
*/

public class YamahaFS1R extends Synth
    {
    /// Various collections of parameter names for pop-up menus
        
    public static final ImageIcon[] ALGORITHM_ICONS = 
        {
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm1.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm2.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm3.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm4.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm5.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm6.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm7.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm8.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm9.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm10.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm11.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm12.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm13.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm14.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm15.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm16.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm17.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm18.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm19.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm20.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm21.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm22.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm23.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm24.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm25.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm26.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm27.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm28.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm29.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm30.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm31.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm32.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm33.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm34.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm35.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm36.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm37.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm38.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm39.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm40.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm41.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm42.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm43.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm44.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm45.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm46.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm47.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm48.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm49.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm50.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm51.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm52.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm53.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm54.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm55.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm56.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm57.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm58.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm59.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm60.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm61.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm62.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm63.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm64.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm65.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm66.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm67.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm68.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm69.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm70.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm71.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm72.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm73.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm74.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm75.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm76.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm77.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm78.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm79.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm80.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm81.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm82.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm83.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm84.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm85.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm86.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm87.png")),
        new ImageIcon(YamahaFS1R.class.getResource("Algorithm88.png")),
        };
    
    public static final String[] WAVES = { "Sine", "All 1", "All 2", "Odd 1", "Odd 2", "Res 1", "Res 2", "Formant" };
    public static final String[] FILTERS = { "LPF 24", "LPF 18", "LPF 12", "HPF", "BPF", "BEF" };
    public static final String[] LFO_WAVES = { "Triangle", "Saw Down", "Saw Up", "Square", "Sine", "Sample & Hold" };
    public static final String[] PHASES = { "0", "90", "180", "270" };
    public static final String[] FREQ_MODES = { "Normal", "Fundamental Pitch", "Formant Pitch" };  // Normal, LinkFO, LinkFF
    public static final String[] CONTROL_OPERATORS = { "Voiced 1", "Voiced 2", "Voiced 3", "Voiced 4", "Voiced 5", "Voiced 6", "Voiced 7", "Voiced 8", "Unvoiced 1", "Unvoiced 2", "Unvoiced 3", "Unvoiced 4", "Unvoiced 5", "Unvoiced 6", "Unvoiced 7", "Unvoiced 8" };
    public static final String[] CONTROL_PARAMETERS = { "Off", "Out", "Freq", "Width" };
    // public static final String[] OSC_MODES = { "Ratio", "Fixed" };
    public static final String[] EG_RANGES = { "8 Oct", "2 Oct", "1 Oct", "1/2 Oct" };
    public static final String[] BANKS = { "Internal", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K" };
    // These four are from https://github.com/kroger/csound-instruments/blob/master/dx7/dx72csnd.c
    public static final String[] RATIO_COARSE = { "0.5", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31" };
    public static final String[] RATIO_FINE = { "1.00", "1.01", "1.02", "1.03", "1.04", "1.05", "1.06", "1.07", "1.08", "1.09", "1.10", "1.11", "1.12", "1.13", "1.14", "1.15", "1.16", "1.17", "1.18", "1.19", "1.20", "1.21", "1.22", "1.23", "1.24", "1.25", "1.26", "1.27", "1.28", "1.29", "1.30", "1.31", "1.32", "1.33", "1.34", "1.35", "1.36", "1.37", "1.38", "1.39", "1.40", "1.41", "1.42", "1.43", "1.44", "1.45", "1.46", "1.47", "1.48", "1.49", "1.50", "1.51", "1.52", "1.53", "1.54", "1.55", "1.56", "1.57", "1.58", "1.59", "1.60", "1.61", "1.62", "1.63", "1.64", "1.65", "1.66", "1.67", "1.68", "1.69", "1.70", "1.71", "1.72", "1.73", "1.74", "1.75", "1.76", "1.77", "1.78", "1.79", "1.80", "1.81", "1.82", "1.83", "1.84", "1.85", "1.86", "1.87", "1.88", "1.89", "1.90", "1.91", "1.92", "1.93", "1.94", "1.95", "1.96", "1.97", "1.98", "1.99" };
    public static final String[] OPERATORS = { "1", "2", "3", "4", "5", "6", "7", "8" };
    public static final String[] OPERATORS_PLUS = { "1", "2", "3", "4", "5", "6", "7", "8", "All" };
    public static final String[] KS_CURVES = { "-Lin", "-Exp", "+Exp", "+Lin" };
    public static final String[] NOTES = {"A", "A#", "B", "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#" };
    // Taken in part, then modified, with permission from FS1REditor by K_Take (https://synth-voice.sakura.ne.jp/fs1r_editor_english.html
    public static final String[] CATEGORIES = { "[None]", "Piano (Pf)", "Chromatic Percussion (Cp)", "Organ (Or)", "Guitar (Gt)", "Bass (Ba)", "Strings/Orchestral (St)", "Ensemble (En)", "Brass (Br)", "Reed (Rd)", "Pipe (Pi)", "Synth Lead (Ld)", "Synth Pad (Pd)", "Synth Sound Effects (Fx)", "Ethnic (Et)", "Percussive (Pc)", "Sound Effects (Se)", "Drums (Dr)", "Synth Comping (Sc)", "Vocal (Vo)", "Combination (Co)", "Material Wave (Wv)", "Sequence (Sq)" };


    JCheckBoxMenuItem[] sendToPart = new JCheckBoxMenuItem[4];
    int part = 0;
    
    // FIXME: the breaker won't work because this is invokeLater
    boolean breaker = false;
    public void setPart(int val) 
        {
        if (breaker) return;
        breaker = true;
        if (sendToPart[val] != null)
            {
            // may not be in same thread
            invokeLater(new Runnable()
                {
                public void run()
                    {
                    sendToPart[val].setSelected(true);              // could call use recursively?  Hence breaker
                    }
                });
            }
        breaker = false;
        part = val; 
        }
                
    public void preparePartChannels(boolean filter)
        {
        // Turn OFF all the Part channels
        // F0 43 1n 5E 3p 00 ll vv vv F7
        for(int p = 0; p < 4; p++)
            {
            if (p == part) continue;
            // Rcv
            tryToSendSysex(new byte[] { (byte)0xF0, 0x43, (byte)(16 + getID() - 1), 0x5E, (byte)(0x30 + p), 0, 0x04, 0, 0x7F, (byte)0xF7 });
            // Rcv Max
            //tryToSendSysex(new byte[] { (byte)0xF0, 0x43, (byte)(16 + getID() - 1), 0x5E, (byte)(0x30 + p), 0, 0x03, 0, 0x7F, (byte)0xF7 });
            }
                        
        // Set part channel to the same as the performance channel.
        // Keep Rcv Max *off*
        // F0 43 1n 5E 3p 00 ll vv vv F7
        // Rcv
        tryToSendSysex(new byte[] { (byte)0xF0, 0x43, (byte)(16 + getID() - 1), 0x5E, (byte)(0x30 + part), 0, 0x04, 0, 0x10, (byte)0xF7 });
        

        if (filter)
            {
            /// For some reason FS1REditor also sends out note reserve information whenever
            /// the filter switch is turned ON.  This presumably has something to do with the
            /// fact that the filter switch reduces the total number of notes, but it doesn't
            /// seem necessary to me.
                
            // Turn ON Filter
            // F0 43 1n 5E 3p 00 ll vv vv F7
            tryToSendSysex(new byte[] { (byte)0xF0, 0x43, (byte)(16 + getID() - 1), 0x5E, (byte)(0x30 + part), 0, 0x07, 0, 0x01, (byte)0xF7 });
            }
        else
            {
            // Turn OFF Filter
            // F0 43 1n 5E 3p 00 ll vv vv F7
            tryToSendSysex(new byte[] { (byte)0xF0, 0x43, (byte)(16 + getID() - 1), 0x5E, (byte)(0x30 + part), 0, 0x07, 0, 0x00, (byte)0xF7 });
            }
        }

    public void stripEffects()
        {
        // Technically we don't need to do the insertion stuff as it's routed
        // into the reverb and variation, whose returns are zeroed anyway, 
        // and similarlysending variation to reverb..., but for good measure...
        
        // Reverb Type = OFF
        // F0 43 1n 5E 3p 00 ll vv vv F7
        tryToSendSysex(new byte[] { (byte)0xF0, 0x43, (byte)(16 + getID() - 1), 0x5E, 0x10, 0x01, 0x28, 0, 0, (byte)0xF7 });

        // Variation Type = OFF
        // F0 43 1n 5E 3p 00 ll vv vv F7
        tryToSendSysex(new byte[] { (byte)0xF0, 0x43, (byte)(16 + getID() - 1), 0x5E, 0x10, 0x01, 0x2B, 0, 0, (byte)0xF7 });

        // Insertion Type = THRU
        // F0 43 1n 5E 3p 00 ll vv vv F7
        tryToSendSysex(new byte[] { (byte)0xF0, 0x43, (byte)(16 + getID() - 1), 0x5E, 0x10, 0x01, 0x2F, 0, 0, (byte)0xF7 });

        // Reverb Return = 0
        // F0 43 1n 5E 3p 00 ll vv vv F7
        tryToSendSysex(new byte[] { (byte)0xF0, 0x43, (byte)(16 + getID() - 1), 0x5E, 0x10, 0x01, 0x2A, 0, 0, (byte)0xF7 });

        // Variation Return = 0
        // F0 43 1n 5E 3p 00 ll vv vv F7
        tryToSendSysex(new byte[] { (byte)0xF0, 0x43, (byte)(16 + getID() - 1), 0x5E, 0x10, 0x01, 0x2D, 0, 0, (byte)0xF7 });

        // Variation to Reverb = 0
        // F0 43 1n 5E 3p 00 ll vv vv F7
        tryToSendSysex(new byte[] { (byte)0xF0, 0x43, (byte)(16 + getID() - 1), 0x5E, 0x10, 0x01, 0x2E, 0, 0, (byte)0xF7 });

        // Send Insertion to Reverb = 0
        // F0 43 1n 5E 3p 00 ll vv vv F7
        tryToSendSysex(new byte[] { (byte)0xF0, 0x43, (byte)(16 + getID() - 1), 0x5E, 0x10, 0x01, 0x31, 0, 0, (byte)0xF7 });

        // Send Insertion to Variation = 0
        // F0 43 1n 5E 3p 00 ll vv vv F7
        tryToSendSysex(new byte[] { (byte)0xF0, 0x43, (byte)(16 + getID() - 1), 0x5E, 0x10, 0x01, 0x32, 0, 0, (byte)0xF7 });
        }
        
    public void setupTestPerformance(boolean filter)
        {
        if (tuple == null)
            if (!setupMIDI())
                return;

        if (tuple != null)
            {
            final YamahaFS1RMulti synth = new YamahaFS1RMulti();
            synth.tuple = new Midi.Tuple(tuple, synth.buildInReceiver(), synth.buildKeyReceiver(), synth.buildKey2Receiver());
            if (synth.tuple != null)
                {
                synth.loadDefaults();
                synth.getModel().set("name", "Edisyn");
                synth.sendAllParameters();
                setPart(0);
                preparePartChannels(filter);
                sendAllParameters();
                }
            }
        }

    public void showMulti()
        {
        if (tuple == null)
            if (!setupMIDI())
                return;

        if (tuple != null)
            {
            final YamahaFS1RMulti synth = new YamahaFS1RMulti();
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

                invokeLater(
                    new Runnable()
                        {
                        public void run() 
                            { 
                            synth.performRequestCurrentDump();
                            }
                        });
                }
            }
        }
        
    public void showMuteDialog()
        {
        JCheckBox[] voiced = new JCheckBox[8];
        JCheckBox[] unvoiced = new JCheckBox[8];
        Box voicedBox = new Box(BoxLayout.X_AXIS);
        Box unvoicedBox = new Box(BoxLayout.X_AXIS);
        for(int i = 0; i < voiced.length; i++)
            {
            voiced[i] = new JCheckBox("" + (i + 1));
            voicedBox.add(voiced[i]);
            unvoiced[i] = new JCheckBox("" + (i + 1));
            unvoicedBox.add(unvoiced[i]);
            }
        voicedBox.add(Box.createGlue());
        unvoicedBox.add(Box.createGlue());
        
        JPanel voicedPanel = new JPanel();
        voicedPanel.setLayout(new BorderLayout());
        voicedPanel.add(voicedBox, BorderLayout.NORTH);
        Box vbox = new Box(BoxLayout.X_AXIS);
        JButton vcheck = new JButton("Check All");
        vcheck.putClientProperty("JComponent.sizeVariant", "small");
        vcheck.setFont(Style.SMALL_FONT());
        vcheck.setHorizontalAlignment(SwingConstants.CENTER);
        vcheck.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                for(int i = 0; i < voiced.length; i++)
                    voiced[i].setSelected(true);
                }
            });
        vbox.add(vcheck);
        JButton vuncheck = new JButton("Uncheck All");
        vuncheck.putClientProperty("JComponent.sizeVariant", "small");
        vuncheck.setFont(Style.SMALL_FONT());
        vuncheck.setHorizontalAlignment(SwingConstants.CENTER);
        vuncheck.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                for(int i = 0; i < voiced.length; i++)
                    voiced[i].setSelected(false);
                }
            });
        vbox.add(vuncheck);
        vbox.add(Box.createGlue());
        voicedPanel.add(vbox, BorderLayout.CENTER);

        JPanel unvoicedPanel = new JPanel();
        unvoicedPanel.setLayout(new BorderLayout());
        unvoicedPanel.add(unvoicedBox, BorderLayout.NORTH);
        Box ubox = new Box(BoxLayout.X_AXIS);
        JButton ucheck = new JButton("Check All");
        ucheck.putClientProperty("JComponent.sizeVariant", "small");
        ucheck.setFont(Style.SMALL_FONT());
        ucheck.setHorizontalAlignment(SwingConstants.CENTER);
        ucheck.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                for(int i = 0; i < voiced.length; i++)
                    unvoiced[i].setSelected(true);
                }
            });
        ubox.add(ucheck);
        JButton uuncheck = new JButton("Uncheck All");
        uuncheck.putClientProperty("JComponent.sizeVariant", "small");
        uuncheck.setFont(Style.SMALL_FONT());
        uuncheck.setHorizontalAlignment(SwingConstants.CENTER);
        uuncheck.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                for(int i = 0; i < voiced.length; i++)
                    unvoiced[i].setSelected(false);
                }
            });
        ubox.add(uuncheck);
        JButton likevoiced = new JButton("Like Voiced");
        likevoiced.putClientProperty("JComponent.sizeVariant", "small");
        likevoiced.setFont(Style.SMALL_FONT());
        likevoiced.setHorizontalAlignment(SwingConstants.CENTER);
        likevoiced.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                for(int i = 0; i < voiced.length; i++)
                    unvoiced[i].setSelected(voiced[i].isSelected());
                }
            });
        ubox.add(likevoiced);
        ubox.add(Box.createGlue());
        unvoicedPanel.add(ubox, BorderLayout.CENTER);
        
        IconDisplay iconDisplay = new IconDisplay(null, ALGORITHM_ICONS, this, "algorithmpresetnumber", 140, 140, false);
        iconDisplay.update("algorithmpresetnumber", getModel());
        JPanel iconPanel = new JPanel();
        iconPanel.setLayout(new BorderLayout());
        iconPanel.add(iconDisplay, BorderLayout.EAST);
                        
        JComponent message = new JPanel();
        message.setLayout(new BorderLayout());
        JPanel pan = new JPanel();
        pan.setLayout(new BorderLayout());
        pan.add(new JLabel("<html>Selected operators will temporarily&nbsp;&nbsp;&nbsp;<br>have Output Level=0 and Fseq=OFF.<br><br>Unselected operators will have<br>Output Level and Fseq restored.<br><br>Current Algorithm:</html>"), BorderLayout.NORTH);
        message.add(pan, BorderLayout.CENTER);
        message.add(iconPanel, BorderLayout.EAST);
        
        boolean result = showMultiOption(YamahaFS1R.this, new String[] { "<html>Voiced<br>Operators</html>", "", "<html>Unvoiced<br>Operators</html>"}, 
            new JComponent[] { voicedPanel, new JPanel(), unvoicedPanel }, "Mute Operators", message);
        
        if (result)
            {
            int[] vswitch = new int[8];
            int[] uswitch = new int[8];

            for(int op = 0; op < 8; op++)
                {
                muteOperator(op, true, voiced[op].isSelected());
                muteOperator(op, false, unvoiced[op].isSelected());
                vswitch[op] = (voiced[op].isSelected() ? 0 : model.get("operator" + (op + 1) + "v" + "switch"));
                uswitch[op] = (unvoiced[op].isSelected()? 0 : model.get("operator" + (op + 1) + "u" + "switch"));
                }
            setFseqSwitches(vswitch, uswitch);
            }
        }


    // For each operator from[i], copies that operator to all operators in to[i][].
    void copyOperators(int from[], int to[][])
        {
        undo.push(getModel());
        setSendMIDI(false);
        boolean currentPush = undo.getWillPush();
        undo.setWillPush(false);

        String[] parameters = model.getKeys();
        for(int a = 0; a < from.length; a++)
            {
            int p1 = from[a];
            for(int b = 0; b < to[a].length; b++)
                {
                int p2 = to[a][b];
                for(int i = 0; i < parameters.length; i++)
                    {
                    if (parameters[i].startsWith("operator" + p1))           // only copy voiced/unvoiced ops if the user requested it
                        {
                        int val2 = model.get(parameters[i]);
                        model.set(("operator" + p2) + parameters[i].substring(9), val2);
                        }
                    }
                }
            }
                                                                                                                                        
        undo.setWillPush(currentPush);
        setSendMIDI(true);
        sendAllParameters();
        }


    public void addYamahaFS1RMenu()
        {
        JMenu menu = new JMenu("FS1R");
        menubar.add(menu);

        JMenuItem current = new JMenuItem("Show Current Performance");
        current.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                showMulti();                
                }
            });
        menu.add(current);
        
        menu.addSeparator();
        JMenuItem initialize = new JMenuItem("Set Up Test Performance for Part 1 with Filter");
        initialize.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                setupTestPerformance(true);                
                }
            });
        menu.add(initialize);
        
        initialize = new JMenuItem("Set Up Test Performance for Part 1 without Filter");
        initialize.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                setupTestPerformance(false);                
                }
            });
        menu.add(initialize);
        
        JMenuItem focus = new JMenuItem("Audition Part with Filter");
        focus.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                preparePartChannels(true);                
                }
            });
        menu.add(focus);
        
        focus = new JMenuItem("Audition Part without Filter");
        focus.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                preparePartChannels(false);                
                }
            });
        menu.add(focus);
        
        JMenuItem strip = new JMenuItem("Strip Effects");
        strip.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                stripEffects();                
                }
            });
        menu.add(strip);

        JMenuItem mute = new JMenuItem("Mute Operators...");
        mute.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                showMuteDialog();                
                }
            });
        menu.add(mute);
        menu.addSeparator();
        
        ButtonGroup group = new ButtonGroup();
        for(int i = 0; i < 4; i++)
            {
            final int _i = i;
            sendToPart[i] = new JCheckBoxMenuItem("Send/Receive Part " + (i + 1));
            sendToPart[i].addActionListener(new ActionListener()
                {
                public void actionPerformed(ActionEvent e)
                    {
                    setPart(_i); 
                    }
                });
            menu.add(sendToPart[i]);
            group.add(sendToPart[i]);
            }
        sendToPart[0].setSelected(true);
        
        menu.addSeparator();

        JMenuItem swap = new JMenuItem("Swap Operators...");
        menu.add(swap);
        swap.addActionListener(new ActionListener()
            {
            JComboBox part1 = new JComboBox(OPERATORS);
            JComboBox part2 = new JComboBox(OPERATORS);
            JCheckBox voiced = new JCheckBox("", true);
            JCheckBox unvoiced = new JCheckBox("", true);
                
            public void actionPerformed(ActionEvent evt)
                {
                part1.setMaximumRowCount(8);
                part2.setMaximumRowCount(8);

                boolean result = showMultiOption(YamahaFS1R.this, new String[] { "Swap", "With", "Voiced", "Unvoiced"}, 
                    new JComponent[] { part1, part2, voiced, unvoiced }, "Swap Operators...", "Enter the operators to swap with one another.");

                if (result)
                    {
                    int p1 = part1.getSelectedIndex() + 1;
                    int p2 = part2.getSelectedIndex() + 1;
                    boolean v = voiced.isSelected();
                    boolean u = unvoiced.isSelected();

                    undo.push(getModel());
                    setSendMIDI(false);
                    boolean currentPush = undo.getWillPush();
                    undo.setWillPush(false);

                    String[] parameters = model.getKeys();
                    for(int i = 0; i < parameters.length; i++)
                        {
                        int op = getOperator(parameters[i]);
                                                
                        if (parameters[i].startsWith("operator" + p2) &&
                            ((v && (op > 0)) || (u && (op < 0))))           // only swap voiced/unvoiced ops if the user requested it
                            {
                            int val2 = model.get(parameters[i]);
                            int val1 = model.get(("operator" + p1) + parameters[i].substring(9));
                            model.set(("operator" + p1) + parameters[i].substring(9), val2);
                            model.set(parameters[i], val1);
                            }
                        }
                                                                                
                    undo.setWillPush(currentPush);
                    setSendMIDI(true);
                    sendAllParameters();
                    }
                }
            });
            
            

        JMenuItem copy = new JMenuItem("Copy Operator To...");
        menu.add(copy);
        copy.addActionListener(new ActionListener()
            {
            JComboBox part1 = new JComboBox(OPERATORS);
            JComboBox part2 = new JComboBox(OPERATORS_PLUS);
            JCheckBox voiced = new JCheckBox("", true);
            JCheckBox unvoiced = new JCheckBox("", true);
                
            public void actionPerformed(ActionEvent evt)
                {
                part1.setMaximumRowCount(8);
                part2.setMaximumRowCount(9);
                boolean result = showMultiOption(YamahaFS1R.this, new String[] { "Copy", "To", "Voiced", "Unvoiced"}, 
                    new JComponent[] { part1, part2, voiced, unvoiced }, "Copy Operator To...", "Enter the operators to copy from and to.");

                if (result)
                    {
                    int p1 = part1.getSelectedIndex() + 1;
                    int p2 = part2.getSelectedIndex() + 1;
                    boolean v = voiced.isSelected();
                    boolean u = unvoiced.isSelected();

                    undo.push(getModel());
                    setSendMIDI(false);
                    boolean currentPush = undo.getWillPush();
                    undo.setWillPush(false);

                    String[] parameters = model.getKeys();
                    for(int i = 0; i < parameters.length; i++)
                        {
                        int op = getOperator(parameters[i]);
                                                
                        if (parameters[i].startsWith("operator" + p1) &&
                            ((v && (op > 0)) || (u && (op < 0))))           // only copy voiced/unvoiced ops if the user requested it
                            {
                            if (p2 == 9) // "All"
                                {
                                for(int j = 1; j <= 8; j++)
                                    {
                                    int val2 = model.get(parameters[i]);
                                    model.set(("operator" + j) + parameters[i].substring(9), val2);
                                    }
                                }
                            else
                                {
                                int val2 = model.get(parameters[i]);
                                model.set(("operator" + p2) + parameters[i].substring(9), val2);
                                }
                            }
                        }
                                                                                
                    undo.setWillPush(currentPush);
                    setSendMIDI(true);
                    sendAllParameters();
                    }
                }
            });

        JMenuItem voicedToUnvoiced = new JMenuItem("Copy Voiced -> Unvoiced");
        menu.add(voicedToUnvoiced);
        voicedToUnvoiced.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt)
                {
                int operator = getCurrentTab();
                if (operator == 0 || operator > 8)
                    {
                    showSimpleError("Cannot Copy", "Go to an operator tab first."); 
                    }
                else
                    {
                    undo.push(getModel());
                    setSendMIDI(false);
                    boolean currentPush = undo.getWillPush();
                    undo.setWillPush(false);

                    String[] parameters = model.getKeys();
                    for(int i = 0; i < parameters.length; i++)
                        {
                        if (parameters[i].startsWith("operator" + operator + "v"))
                            {
                            int val2 = model.get(parameters[i]);
                            if (model.exists(("operator" + operator + "u") + parameters[i].substring(10)))
                                {
                                model.set(("operator" + operator + "u") + parameters[i].substring(10), val2);
                                }
                            }
                        }
                                                                                
                    undo.setWillPush(currentPush);
                    setSendMIDI(true);
                    sendAllParameters();
                    }
                }
            });

        JMenuItem unvoicedToVoiced = new JMenuItem("Copy Unvoiced -> Voiced");
        menu.add(unvoicedToVoiced);
        unvoicedToVoiced.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt)
                {
                int operator = getCurrentTab();
                if (operator == 0 || operator > 8)
                    {
                    showSimpleError("Cannot Copy", "Go to an operator tab first."); 
                    }
                else
                    {
                    undo.push(getModel());
                    setSendMIDI(false);
                    boolean currentPush = undo.getWillPush();
                    undo.setWillPush(false);

                    String[] parameters = model.getKeys();
                    for(int i = 0; i < parameters.length; i++)
                        {
                        if (parameters[i].startsWith("operator" + operator + "u"))
                            {
                            int val2 = model.get(parameters[i]);
                            if (model.exists(("operator" + operator + "v") + parameters[i].substring(10)))
                                {
                                model.set(("operator" + operator + "v") + parameters[i].substring(10), val2);
                                }
                            }
                        }
                                                                                
                    undo.setWillPush(currentPush);
                    setSendMIDI(true);
                    sendAllParameters();
                    }
                }
            });


        menu.addSeparator();
        JMenuItem copy1 = new JMenuItem("Copy Operator 1");
        menu.add(copy1);
        copy1.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt)
                {
                copyOperators(new int[] { 1 }, new int[][] { { 2, 3, 4, 5, 6, 7, 8 } });
                }
            });

        JMenuItem copy2 = new JMenuItem("Copy Operators 1, 2");
        menu.add(copy2);
        copy2.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt)
                {
                copyOperators(new int[] { 1, 2 }, new int[][] { { 3, 5, 7 }, { 4, 6, 8 } });
                }
            });
            
        JMenuItem copy3 = new JMenuItem("Copy Operators 1, 2, 3, 4");
        menu.add(copy3);
        copy3.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt)
                {
                copyOperators(new int[] { 1, 2, 3, 4 }, new int[][] { { 5 }, { 6 }, { 7 }, { 8 } });
                }
            });
        }

    public JFrame sprout()
        {
        JFrame frame = super.sprout();
        addYamahaFS1RMenu();
        return frame;
        }         

    public YamahaFS1R()
        {
        model.set("number", 0);
        model.set("bank", 0);
                
        if (allParametersToIndex == null)
            {
            allParametersToIndex = new HashMap();
            for(int i = 0; i < allParameters.length; i++)
                {
                allParametersToIndex.put(allParameters[i], Integer.valueOf(i));
                }
            }

        JComponent soundPanel = new SynthPanel(this);
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        hbox.add(addGlobal(Style.COLOR_A()));
        hbox.addLast(addFilter(Style.COLOR_B()));
        vbox.add(hbox);

        hbox = new HBox();
        hbox.addLast(addFilterEnvelope(Style.COLOR_B()));
        vbox.add(hbox);
        hbox = new HBox();
        hbox.addLast(addPitchEnvelope(Style.COLOR_A()));
        vbox.add(hbox);

        hbox = new HBox();
        hbox.add(addLFO(1, Style.COLOR_B())); 
        hbox.addLast(addLFO(2, Style.COLOR_B())); 
        vbox.add(hbox);

        hbox = new HBox();
        hbox.add(addAlgorithm(Style.COLOR_A()));
                
        VBox vbox2 = new VBox();
        vbox2.add(addFormantControl(Style.COLOR_C()));
        vbox2.add(addFMControl(Style.COLOR_C()));
        hbox.addLast(vbox2);
        vbox.add(hbox);
        
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Common", soundPanel);
                        
        for(int op = 1; op <= 8; op++)
            {
            soundPanel = new SynthPanel(this);
            vbox = new VBox();
            hbox = new HBox();

            VBox left = new VBox();
            left.add(addVoicedOperator(op, Style.COLOR_A()));
            left.add(addVoicedAmpEnvelope(op, Style.COLOR_B()));
            hbox.add(left);
                
            hbox.addLast(addVoicedKeyScaling(op, Style.COLOR_C()));
                
            vbox.add(hbox);
            hbox = new HBox();

            hbox.add(addVoicedSensitivity(op, Style.COLOR_C()));
            hbox.addLast(addVoicedFrequencyEnvelope(op, Style.COLOR_B()));
                
            vbox.add(hbox);
            hbox = new HBox();

            vbox.add(addUnvoicedOperator(op, Style.COLOR_A()));
            vbox.add(addUnvoicedAmpEnvelope(op, Style.COLOR_B()));

            hbox.add(addUnvoicedSensitivity(op, Style.COLOR_C()));
            hbox.addLast(addUnvoicedFrequencyEnvelope(op, Style.COLOR_B()));
                
            vbox.add(hbox);
            hbox = new HBox();
                
            soundPanel.add(vbox, BorderLayout.CENTER);
            //            ((SynthPanel)soundPanel).makePasteable("operator" + op);
            ((SynthPanel)soundPanel).makePasteable("operator");
            addTab("Operator " + op, soundPanel);
            }

        loadDefaults();        
        }
                
    public String getDefaultResourceFileName() { return "YamahaFS1R.init"; }
    public String getHTMLResourceFileName() { return "YamahaFS1R.html"; }

    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        JComboBox bank = new JComboBox(BANKS);
        bank.setEditable(false);
        bank.setMaximumRowCount(32);
        if (writing)
            {
            bank = new JComboBox(new String[] { "Internal" });
            bank.setEnabled(false);
            bank.setSelectedIndex(0);
            }
        else
            {
            bank.setSelectedIndex(model.get("bank"));
            }
                
        JTextField number = new SelectedTextField("" + (model.get("number") + 1), 3);
                
        while(true)
            {
            boolean result = showMultiOption(this, new String[] { "Bank", "Patch Number"}, 
                new JComponent[] { bank, number }, title, "Enter the Bank and Patch number.");
                
            if (result == false) 
                return false;
                                
            int n;
            try { n = Integer.parseInt(number.getText()); }
            catch (NumberFormatException e)
                {
                showSimpleError(title, "The Patch Number must be an integer 1 ... 128");
                continue;
                }
            if (n < 1 || n > 128)
                {
                showSimpleError(title, "The Patch Number must be an integer 1 ... 128");
                continue;
                }
                                
            change.set("bank", bank.getSelectedIndex());
            change.set("number", n - 1);
                        
            return true;
            }
        }
                                    
    /** Add the global patch category (name, id, number, etc.) */
    public JComponent addNameGlobal(Color color)
        {
        Category globalCategory = new Category(this, getSynthName(), color);
        ////globalCategory.makeUnresettable();
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
                
        VBox vbox = new VBox();
        HBox hbox2 = new HBox();
        comp = new PatchDisplay(this, 8);
        hbox2.add(comp);
        vbox.add(hbox2);
        
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
        vbox.add(comp);  // doesn't work right :-(

        hbox.add(vbox);

        // Not enough space to show the title
        hbox.addLast(Strut.makeHorizontalStrut(20));

        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }


    public JComponent addAlgorithm( Color color)
        {
        Category category = new Category(this, "Algorithm", color);

        JComponent comp;
        String[] params;
        VBox vbox = new VBox();
        
        HBox hbox = new HBox();
        hbox.add(Strut.makeHorizontalStrut(5));
        comp = new IconDisplay(null, ALGORITHM_ICONS, this, "algorithmpresetnumber", 140, 140);
        hbox.add(comp);
        hbox.addLast(Strut.makeHorizontalStrut(5));
        vbox.add(hbox);
        vbox.add(Strut.makeVerticalStrut(10));
        
        
        VBox buttons = new VBox();
        PushButton button = new PushButton("\u2191")
            {
            public void perform()
                {
                int val = model.get("algorithmpresetnumber") + 1;
                if (val <= 87) model.set("algorithmpresetnumber", val);
                }
            };
        buttons.add(button);
        button = new PushButton("\u2193")
            {
            public void perform()
                {
                int val = model.get("algorithmpresetnumber") - 1;
                if (val >= 0) model.set("algorithmpresetnumber", val);
                }
            };
        buttons.add(button);
        
        HBox extras = new HBox();
        comp = new LabelledDial("Algorithm", this, "algorithmpresetnumber", color, 0, 87, -1);
        extras.add(comp);
        extras.addLast(buttons);                
        vbox.add(extras);

        model.removeMetricMinMax("algorithmpresetnumber");
                
        category.add(vbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addGlobal( Color color)
        {
        Category category = new Category(this, "Global", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
                
        params = CATEGORIES;
        comp = new Chooser("Category", this, "category", params);
        vbox.add(comp);

        hbox.add(vbox);

        comp = new LabelledDial("Feedback", this, "voicedfeedbacklevel", color, 0, 7);
        hbox.add(comp);

        comp = new LabelledDial("Note Shift", this, "noteshift", color, 0, 48, 24)
            {
            public boolean isSymmetric() { return true; }
            };
        hbox.add(comp);
        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

        
    public JComponent addLFO(int lfo, Color color)
        {
        Category category = new Category(this, "LFO " + lfo, color);
        //        category.makePasteable("lfo" + lfo);
        category.makePasteable("lfo");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = LFO_WAVES;
        comp = new Chooser("Wave", this, "lfo" + lfo + "waveform", params);
        vbox.add(comp);

        comp = new CheckBox("Sync", this, "lfo" + lfo + "keysync");
        vbox.add(comp);

        hbox.add(vbox);

        comp = new LabelledDial("Speed", this, "lfo" + lfo + "speed", color, 0, 127);
        hbox.add(comp);
        
        comp = new LabelledDial("Filter", this,  "filtercutofffrequencylfo" + lfo + "depth", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("Mod Depth");
        hbox.add(comp);

        if (lfo == 2)
            {
            comp = new LabelledDial("Phase", this, "lfo" + lfo + "phase", color, 0, 3)
                {
                public String map(int val)
                    {
                    return PHASES[val];
                    }
                };
            hbox.add(comp);
            }
                
        if (lfo == 1)
            {
            comp = new LabelledDial("Delay", this, "lfo" + lfo + "delay", color, 0, 99);
            ((LabelledDial)comp).addAdditionalLabel("Depth");
            hbox.add(comp);
            comp = new LabelledDial("Pitch", this, "lfo" + lfo + "pitchmodulationdepth", color, 0, 99);
            ((LabelledDial)comp).addAdditionalLabel("Mod Depth");
            hbox.add(comp);
            comp = new LabelledDial("Amplitude", this, "lfo" + lfo + "amplitudemodulationdepth", color, 0, 99);
            ((LabelledDial)comp).addAdditionalLabel("Mod Depth");
            hbox.add(comp);
            comp = new LabelledDial("Frequency", this, "lfo" + lfo + "frequencymodulationdepth", color, 0, 99);
            ((LabelledDial)comp).addAdditionalLabel("Mod Depth");
            hbox.add(comp);         
            }

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
    



    public JComponent addPitchEnvelope(Color color)
        {
        Category category = new Category(this, "Pitch Envelope", color);
        category.makeDistributable("pitcheg");
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
               
        VBox vbox = new VBox();
        params = EG_RANGES;
        comp = new Chooser("Range", this, "pitchegrange", params);
        vbox.add(comp);

        hbox.add(vbox);

        comp = new LabelledDial("Level 0", this,  "pitcheg0level", color, 0, 100, 50);
        hbox.add(comp);

        comp = new LabelledDial("Time 1", this,  "pitcheg1time", color, 0, 99);
        hbox.add(comp);

        comp = new LabelledDial("Level 1", this,  "pitcheg1level", color, 0, 100, 50)
            {
            public boolean isSymmetric() { return true; }
            public int getDefaultValue() { return 50; }
            };
        hbox.add(comp);

        comp = new LabelledDial("Time 2", this,  "pitcheg2time", color, 0, 99);
        hbox.add(comp);

        comp = new LabelledDial("Level 2", this,  "pitcheg2level", color, 0, 100, 50)
            {
            public boolean isSymmetric() { return true; }
            public int getDefaultValue() { return 50; }
            };
        hbox.add(comp);

        comp = new LabelledDial("Time 3", this,  "pitcheg3time", color, 0, 99);
        hbox.add(comp);

        comp = new LabelledDial("Level 3", this,  "pitcheg3level", color, 0, 100, 50)
            {
            public boolean isSymmetric() { return true; }
            public int getDefaultValue() { return 50; }
            };
        ((LabelledDial)comp).addAdditionalLabel("Sustain");
        hbox.add(comp);


        comp = new LabelledDial("Time 4", this,  "pitcheg4time", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("Release");
        hbox.add(comp);

        comp = new LabelledDial("Level 4", this,  "pitcheg4level", color, 0, 100, 50)
            {
            public boolean isSymmetric() { return true; }
            public int getDefaultValue() { return 50; }
            };
        ((LabelledDial)comp).addAdditionalLabel("Release");
        hbox.add(comp);

        comp = new LabelledDial("Velocity", this,  "pitchegvelocitysensitivity", color, 0, 7);
        ((LabelledDial)comp).addAdditionalLabel("Sensitivity");
        hbox.add(comp);

        comp = new LabelledDial("Time", this,  "pitchegtimescalingdepth", color, 0, 7);
        ((LabelledDial)comp).addAdditionalLabel("Scaling");
        hbox.add(comp);


        // ADSR
        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null,                                "pitcheg1time",         "pitcheg2time",   "pitcheg3time",       null,                           "pitcheg4time" },
            new String[] {   "pitcheg0level",   "pitcheg1level",   "pitcheg2level",   "pitcheg3level",  "pitcheg3level",        "pitcheg4level" },
            new double[] { 0, 1.0/5/99, 1.0/5/99, 1.0/5/99, 1.0/5, 1.0/5/99 },
            new double[] { 1.0 / 100, 1.0 / 100, 1.0 / 100, 1.0 / 100, 1.0 / 100, 1.0 / 100 });
        ((EnvelopeDisplay)comp).setAxis(0.5);  // I think 50 is the midpoint
        hbox.addLast(comp);
                
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
        
        
    public JComponent addFormantControl(Color color)
        {
        Category category = new Category(this, "Formant Control", color);
        category.makeDistributable("formantcontrol");
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
               
        for(int i = 1; i <= 5; i++)
            {
            VBox vbox = new VBox();
        
            params = CONTROL_PARAMETERS;
            comp = new Chooser("Destination " + i, this, "formantcontrol" + i + "dest", params);
            vbox.add(comp);

            params = OPERATORS;
            comp = new Chooser("Operator " + i, this, "formantcontrol" + i + "op", params);
            vbox.add(comp);

            comp = new CheckBox("Voiced", this, "formantcontrol" + i + "voiced");
            vbox.add(comp);
            hbox.add(vbox);
                        
            comp = new LabelledDial("Depth " + i, this,  "formantcontrol" + i + "depth", color, 0, 127, 64)
                {
                public boolean isSymmetric() { return true; }
                };
            hbox.add(comp);
            }
                                
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addFMControl(Color color)
        {
        Category category = new Category(this, "FM Control", color);
        category.makeDistributable("fmcontrol");
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
               
        for(int i = 1; i <= 5; i++)
            {
            VBox vbox = new VBox();
        
            params = CONTROL_PARAMETERS;
            comp = new Chooser("Destination " + i, this, "fmcontrol" + i + "dest", params);
            vbox.add(comp);

            params = OPERATORS;
            comp = new Chooser("Operator " + i, this, "fmcontrol" + i + "op", params);
            vbox.add(comp);

            comp = new CheckBox("Voiced", this, "fmcontrol" + i + "voiced");
            vbox.add(comp);
            hbox.add(vbox);

            comp = new LabelledDial("Depth " + i, this,  "fmcontrol" + i + "depth", color, 0, 127, 64)
                {
                public boolean isSymmetric() { return true; }
                };
            hbox.add(comp);
            }
                                
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addFilter(Color color)
        {
        Category category = new Category(this, "Filter", color);
        category.makeDistributable("filter");
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
               
        VBox vbox = new VBox();
        
        params = FILTERS;
        comp = new Chooser("Type", this, "filtertype", params);
        vbox.add(comp);
        hbox.add(vbox);

        // wonder what the zero point would be for THIS                 
        comp = new LabelledDial("Resonance", this,  "filterresonance", color, 0, 116, 16)
            {
            public double getStartAngle() { return 312; }
            public int getDefaultValue() { return 16; }
            };
        hbox.add(comp);

        comp = new LabelledDial("Resonance", this,  "filterresonancevelocitysensitivity", color, 0, 14, 7)
            {
            public boolean isSymmetric() { return true; }
            };
        ((LabelledDial)comp).addAdditionalLabel("Vel Sensitivity");
        hbox.add(comp);

        comp = new LabelledDial("Cutoff", this,  "filtercutofffrequency", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Frequency");
        hbox.add(comp);

        comp = new LabelledDial("Cutoff Freq", this,  "filtercutofffrequencykeyscaledepth", color, 0, 127, 64)  
            {
            public boolean isSymmetric() { return true; }
            };
        ((LabelledDial)comp).addAdditionalLabel("Scaling");
        hbox.add(comp);
                
        comp = new LabelledDial("Cutoff Freq", this,  "filtercutofffrequencykeyscalepoint", color, 0, 127)      
            {
            public String map(int val)
                {
                // not sure if this is right.  Needs to start at C-2
                return (NOTES[(val + 3) % 12] + (((val + 9) / 12) - 2));
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Breakpoint");
        hbox.add(comp);
                                
        comp = new LabelledDial("Input", this,  "filterinputgain", color, 0, 24, 12)    
            {
            public boolean isSymmetric() { return true; }
            };
        ((LabelledDial)comp).addAdditionalLabel("Gain");
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addFilterEnvelope(Color color)
        {
        Category category = new Category(this, "Filter Envelope", color);
        category.makeDistributable("filtereg");
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        comp = new LabelledDial("Depth", this,  "filteregdepth", color, 0, 127, 64)
            {
            public boolean isSymmetric() { return true; }
            };
        hbox.add(comp);

        comp = new LabelledDial("Time 1", this,  "filtereg1time", color, 0, 99);
        hbox.add(comp);

        comp = new LabelledDial("Level 1", this,  "filtereg1level", color, 0, 100, 50)
            {
            public boolean isSymmetric() { return true; }
            };
        hbox.add(comp);

        comp = new LabelledDial("Time 2", this,  "filtereg2time", color, 0, 99);
        hbox.add(comp);

        comp = new LabelledDial("Level 2", this,  "filtereg2level", color, 0, 100, 50)
            {
            public boolean isSymmetric() { return true; }
            };
        hbox.add(comp);

        comp = new LabelledDial("Time 3", this,  "filtereg3time", color, 0, 99);
        hbox.add(comp);

        comp = new LabelledDial("Level 3", this,  "filtereg3level", color, 0, 100, 50)
            {
            public boolean isSymmetric() { return true; }
            };
        ((LabelledDial)comp).addAdditionalLabel("Sustain");
        hbox.add(comp);

        comp = new LabelledDial("Time 4", this,  "filtereg4time", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("Release");
        hbox.add(comp);

        comp = new LabelledDial("Level 4", this,  "filtereg4level", color, 0, 100, 50)
            {
            public boolean isSymmetric() { return true; }
            };
        ((LabelledDial)comp).addAdditionalLabel("Release");
        hbox.add(comp);

        comp = new LabelledDial("Attack Time", this,  "filteregattacktimevel", color, 0, 7);
        ((LabelledDial)comp).addAdditionalLabel("Vel Sens");
        hbox.add(comp);

        comp = new LabelledDial("Time", this,  "filteregtimescale", color, 0, 7);
        ((LabelledDial)comp).addAdditionalLabel("Scaling");
        hbox.add(comp);

        comp = new LabelledDial("Depth", this,  "filteregdepthvelocitysensitivity", color, 0, 14, 7)
            {
            public boolean isSymmetric() { return true; }
            };
        ((LabelledDial)comp).addAdditionalLabel("Vel Sens");
        hbox.add(comp);
                
        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null,                                                        "filtereg1time",        "filtereg2time",        "filtereg3time",        null,                           "filtereg4time" },
            new String[] { null,                                                        "filtereg1level",       "filtereg2level",       "filtereg3level",       "filtereg3level",       "filtereg4level" },
            new double[] { 0, 1.0/5/99, 1.0/5/99, 1.0/5/99, 1.0/5, 1.0/5/99 },
            new double[] { 0.5, 1.0 / 99, 1.0 / 99, 1.0 / 99, 1.0 / 99, 1.0 / 99 });
        ((EnvelopeDisplay)comp).setAxis(0.5);
        hbox.addLast(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }



    public JComponent addVoicedKeyScaling(final int src, Color color)
        {
        final Category category = new Category(this, "Voiced Key Scaling " + src, color);
        category.makePasteable("operator" + src + "v");

        JComponent comp;
        String[] params;
        VBox outer = new VBox();
        
        final HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = KS_CURVES;
        comp = new Chooser("Left Curve", this, "operator" + src + "v" + "eglevelscalingleftcurve", params);
        vbox.add(comp);

        params = KS_CURVES;
        comp = new Chooser("Right Curve", this, "operator" + src + "v" + "eglevelscalingrightcurve", params);
        vbox.add(comp);
        hbox.add(vbox);
                
        comp = new LabelledDial("Left", this, "operator" + src + "v" + "eglevelscalingleftdepth", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("Depth");
        hbox.add(comp);
        
        comp = new LabelledDial("Right", this, "operator" + src + "v" + "eglevelscalingrightdepth", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("Depth");
        hbox.add(comp);
        
        outer.add(hbox);
        
        outer.add(Strut.makeVerticalStrut(5));

        VBox inner = new VBox(VBox.TOP_CONSUMES);
                
        HBox second = new HBox();
                
        VBox breakbox = new VBox();
        breakbox.add(Strut.makeVerticalStrut(10));
        comp = new LabelledDial("Breakpoint", this, "operator" + src + "v" + "eglevelscalingbreakpoint", color, 0, 99)
            {
            public String map(int val)
                {
                return (NOTES[val % 12] + ((val + 9) / 12 - 1));
                }
            };
        breakbox.add(comp);
        second.add(breakbox);
        
        second.addLast(new edisyn.synth.yamahadx7.YamahaDX7Curve(this, 
                "operator" + src + "v" + "eglevelscalingbreakpoint",
                "operator" + src + "v" + "eglevelscalingleftcurve",
                "operator" + src + "v" + "eglevelscalingrightcurve",
                "operator" + src + "v" + "eglevelscalingleftdepth",
                "operator" + src + "v" + "eglevelscalingrightdepth"
                ));
        inner.addLast(second);
        inner.add(Strut.makeVerticalStrut(10));
        outer.add(inner);

        category.add(outer, BorderLayout.CENTER);
        return category;
        }


    public JComponent addVoicedSensitivity(final int src, Color color)
        {
        final Category category = new Category(this, "Voiced Sensitivity " + src, color);
        category.makePasteable("operator" + src + "v");

        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();
        
        comp = new LabelledDial("Frequency", this, "operator" + src + "v" + "frequencybiassense", color, 0, 14, 7)
            {
            public boolean isSymmetric() { return true; }
            };
        ((LabelledDial)comp).addAdditionalLabel("Bias");
        hbox.add(comp);
                
        comp = new LabelledDial("Bandwidth", this, "operator" + src + "v" + "bwbiassense", color, 0, 14, 7)
            {
            public boolean isSymmetric() { return true; }
            };
        ((LabelledDial)comp).addAdditionalLabel("Bias");
        hbox.add(comp);
                
        comp = new LabelledDial("Pitch", this, "operator" + src + "v" + "pitchmodsense", color, 0, 7);
        ((LabelledDial)comp).addAdditionalLabel("Mod");
        hbox.add(comp);
                
        comp = new LabelledDial("Frequency", this, "operator" + src + "v" + "frequencymodsense", color, 0, 7);
        ((LabelledDial)comp).addAdditionalLabel("Mod");
        hbox.add(comp);
                
        comp = new LabelledDial("Frequency", this, "operator" + src + "v" + "frequencyvelocitysense", color, 0, 14, 7)
            {
            public boolean isSymmetric() { return true; }
            };
        ((LabelledDial)comp).addAdditionalLabel("Velocity");
        hbox.add(comp);
                
        comp = new LabelledDial("Amp", this, "operator" + src + "v" + "ampmodsense", color, 0, 7);
        ((LabelledDial)comp).addAdditionalLabel("Mod");
        hbox.add(comp);
                
        comp = new LabelledDial("Amp", this, "operator" + src + "v" + "ampvelocitysense", color, 0, 14, 7)
            {
            public boolean isSymmetric() { return true; }
            };
        ((LabelledDial)comp).addAdditionalLabel("Velocity");
        hbox.add(comp);
                
        comp = new LabelledDial("Amp EG", this, "operator" + src + "v" + "egbiassense", color, 0, 14, 7)
            {
            public boolean isSymmetric() { return true; }
            };
        ((LabelledDial)comp).addAdditionalLabel("Bias");
        hbox.add(comp);
                
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addVoicedAmpEnvelope(final int src, Color color)
        {
        final Category category = new Category(this, "Voiced Amplitude Envelope " + src, color);
        category.makePasteable("operator" + src + "v");
        category.makeDistributable("operator" + src + "v" + "eg");

        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();
        
        comp = new LabelledDial("Hold", this, "operator" + src + "v" + "egholdtime", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("Time");
        hbox.add(comp);
        
        comp = new LabelledDial("Time 1", this, "operator" + src + "v" + "eg1time", color, 0, 99);
        hbox.add(comp);
        
        comp = new LabelledDial("Level 1", this, "operator" + src + "v" + "eg1level", color, 0, 99);
        hbox.add(comp);
        
        comp = new LabelledDial("Time 2", this, "operator" + src + "v" + "eg2time", color, 0, 99);
        hbox.add(comp);
        
        comp = new LabelledDial("Level 2", this, "operator" + src + "v" + "eg2level", color, 0, 99);
        hbox.add(comp);
        
        comp = new LabelledDial("Time 3", this, "operator" + src + "v" + "eg3time", color, 0, 99);
        hbox.add(comp);
        
        comp = new LabelledDial("Level 3", this, "operator" + src + "v" + "eg3level", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("Sustain");
        hbox.add(comp);
        
        comp = new LabelledDial("Time 4", this, "operator" + src + "v" + "eg4time", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("Release");
        hbox.add(comp);

        comp = new LabelledDial("Level 4", this, "operator" + src + "v" + "eg4level", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("Start/Rel");
        hbox.add(comp);
        
        comp = new LabelledDial("Time", this, "operator" + src + "v" + "egtimescaling", color, 0, 7);
        ((LabelledDial)comp).addAdditionalLabel("Scaling");
        hbox.add(comp);


        int envelope = 0;
        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null,                                                        "operator" + src + "v" + "egholdtime",       "operator" + src + "v" + "eg1time",  "operator" + src + "v" + "eg2time",  "operator" + src + "v" + "eg3time",          null,                                                   "operator" + src + "v" + "eg4time" },
            new String[] { "operator" + src + "v" + "eg4level",      "operator" + src + "v" + "eg4level",         "operator" + src + "v" + "eg1level",         "operator" + src + "v" + "eg2level", "operator" + src + "v" + "eg3level", "operator" + src + "v" + "eg3level", "operator" + src + "v" + "eg4level" },
            new double[] { 0, 1.0/6/99, 1.0/6/99, 1.0/6/99, 1.0/6/99, 1.0/6, 1.0/6/99 },
            new double[] { 1.0 / 99, 1.0 / 99, 1.0 / 99, 1.0 / 99, 1.0 / 99, 1.0 / 99, 1.0 / 99 });
        hbox.addLast(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addVoicedFrequencyEnvelope(final int src, Color color)
        {
        final Category category = new Category(this, "Voiced Frequency Envelope " + src, color);
        category.makePasteable("operator" + src + "v");
        category.makeDistributable("operator" + src + "v" + "frequencyeg");

        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();
        
        comp = new LabelledDial("Initial", this, "operator" + src + "v" + "frequencyeginitialvalue", color, 0, 100, 50);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);
        
        comp = new LabelledDial("Attack", this, "operator" + src + "v" + "frequencyegattacktime", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("Time");
        hbox.add(comp);
        
        comp = new LabelledDial("Attack", this, "operator" + src + "v" + "frequencyegattackvalue", color, 0, 100, 50);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);
        
        comp = new LabelledDial("Decay", this, "operator" + src + "v" + "frequencyegdecaytime", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("Time");
        hbox.add(comp);        

        int envelope = 0;
        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null,                                                                                        "operator" + src + "v" + "frequencyegattacktime",  "operator" + src + "v" + "frequencyegdecaytime" },
            new String[] { "operator" + src + "v" + "frequencyeginitialvalue",       "operator" + src + "v" + "frequencyegattackvalue", null },
            new double[] { 0, 1.0/2/99, 1.0/2/99 },
            new double[] { 1.0 / 100, 1.0 / 100, 0.5 });
        ((EnvelopeDisplay)comp).setAxis(0.5);
        hbox.addLast(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addVoicedOperator(final int src, Color color)
        {
        final Category category = new Category(this, "Voiced Operator " + src, color);
        category.makePasteable("operator" + src + "v");

        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();
        

        // We start by defining the coarse and fine knobs
        final HBox boxesContainer = new HBox();
        final HBox[] boxes = new HBox[] { new HBox(), new HBox() };


        // From Line 4923 in Main.cpp
        // If RATIO
        //              If Coarse = 0   // Special case
        //                      Text = 0.5 + Fine * 0.005                                       [0.000]
        //              Else If Coarse + Fine * Coarse * 0.01 >= 10
        //                      Text = Coarse + Fine * Coarse * 0.01            [0.00]
        //              Else
        //                      Text = Coarse + Fine * Coarse * 0.01            [0.000]
        // Else (FIXED)
        //              If Coarse == 0
        //                      Text = 0                                                                        [0.000]
        //              Else
        //                      Text = FREQUENCIES[Fine][Coarse - 1]


        final LabelledDial fineRatio = new LabelledDial("Frequency", this, "operator" + src + "v" + "frequencyfineratio", color, 0, 127)
            {
            public String map(int val)
                {
                int c = model.get("operator" + src + "v" + "frequencycoarseratio", 0);
                if (c == 0)
                    return String.format("%2.2f", (0.5 + val * 0.005));
                else
                    return String.format("%2.2f", (c + val * c * 0.01));
                }               
            };
        fineRatio.addAdditionalLabel("Fine");

        final LabelledDial coarseRatio = new LabelledDial("Frequency", this, "operator" + src + "v" + "frequencycoarseratio", color, 0, 31)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                fineRatio.repaint();
                }
                
            public String map(int val)
                {
                return (val == 0 ? "0.5" : "" + val);
                }               
            };
        coarseRatio.addAdditionalLabel("Coarse");
        boxes[0].add(coarseRatio);
        boxes[0].add(fineRatio);
        boxesContainer.add(boxes[0]);

        final LabelledDial fineFixed = new LabelledDial("Frequency", this, "operator" + src + "v" + "frequencyfinefixed", color, 0, 127)
            {
            public String map(int val)
                {
                int c = model.get("operator" + src + "v" + "frequencycoarsefixed", 0);
                if (c == 0) return "0";
                else return FREQUENCIES[val][c - 1];
                }               
            };
        fineFixed.addAdditionalLabel("Fine");

        final LabelledDial coarseFixed = new LabelledDial("Frequency", this, "operator" + src + "v" + "frequencycoarsefixed", color, 0, 21)          // amazing, it's 21
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                fineFixed.repaint();
                }
                
            public String map(int val)
                {
                if (val == 0) return "0";
                else return FREQUENCIES[0][val - 1];
                }               
            };
        coarseFixed.addAdditionalLabel("Coarse");
        boxes[1].add(coarseFixed);
        boxes[1].add(fineFixed);
        
        

        // strange name for bandwidth in the sysex docs
                
        final LabelledDial bandwidthResonance = new LabelledDial("Bandwidth/", this, "operator" + src + "v" + "frequencyratioofbandspectrum", color, 0, 99);
        bandwidthResonance.addAdditionalLabel("Resonance");

        final LabelledDial frequencyNoteScaling = new LabelledDial("Frequency", this, "operator" + src + "v" + "frequencynotescaling", color, 0, 99);
        ((LabelledDial)frequencyNoteScaling).addAdditionalLabel("Key Scaling");

        final LabelledDial transpose = new LabelledDial("Transpose", this, "operator" + src + "v" + "transpose", color, 0, 48, 24)
            {
            public boolean isSymmetric() { return true; }
            };

        final LabelledDial skirt = new LabelledDial("Skirt", this, "operator" + src + "v" + "spectralskirt", color, 0, 7);

        final HBox extraContainer = new HBox();
                
        CheckBox keySync = new CheckBox("Key Sync", this, "operator" + src + "v" + "keysync");
        
        final HBox keySyncContainer = new HBox();
        keySyncContainer.add(keySync);

        VBox vbox = new VBox();

        final CheckBox mode = new CheckBox("Fixed", this, "operator" + src + "v" + "frequencyoscillatormode")
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                int val = model.get(key, 0);
                boxesContainer.remove(boxes[0]);
                boxesContainer.remove(boxes[1]);
                // Note that vspectralform may not exist yet
                if (model.get("operator" + src + "v" + "spectralform", 0) == 7)  // formant
                    {
                    boxesContainer.add(boxes[1]);   // always fixed
                    }
                else
                    {
                    boxesContainer.add(boxes[val]);
                    }
                boxesContainer.revalidate();
                boxesContainer.repaint();
                }
            };
        /*        
                  params = OSC_MODES;
                  final Chooser mode = new Chooser("Mode", this, "operator" + src + "v" + "frequencyoscillatormode", params) 
                  {
                  public void update(String key, Model model)
                  {
                  super.update(key, model);
                  int val = model.get(key, 0);
                  boxesContainer.remove(boxes[0]);
                  boxesContainer.remove(boxes[1]);
                  boxesContainer.add(boxes[val]);
                  boxesContainer.revalidate();
                  boxesContainer.repaint();
                  }
                  };
        */
        final VBox modeContainer = new VBox();
        modeContainer.add(mode);

        params = WAVES;
        comp = new Chooser("Spectral Form", this, "operator" + src + "v" + "spectralform", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                int val = model.get(key);
                
                extraContainer.removeAll();
                
                modeContainer.removeAll();
                keySyncContainer.removeAll();
                if (val == 7)
                    {
                    modeContainer.add(Strut.makeStrut(mode));
                    keySyncContainer.add(Strut.makeStrut(keySync));
                    }
                else
                    {
                    modeContainer.add(mode);
                    keySyncContainer.add(keySync);
                    mode.repaint();
                    }
                modeContainer.revalidate();
                modeContainer.repaint();
                keySyncContainer.revalidate();
                keySyncContainer.repaint();
                        
                //                keySync.setEnabled(val != 7); // fmt
                        
                if (val == 0)           // Sine
                    {
                    extraContainer.add(Strut.makeStrut(skirt));
                    extraContainer.add(Strut.makeStrut(bandwidthResonance));
                    extraContainer.add(Strut.makeStrut(frequencyNoteScaling));
                    extraContainer.add(Strut.makeStrut(transpose));
                    }
                else if (val == 1 || val == 2 || val == 3 || val == 4)          // all1, all2, odd1, odd2
                    {
                    extraContainer.add(skirt);
                    extraContainer.add(Strut.makeStrut(bandwidthResonance));
                    extraContainer.add(Strut.makeStrut(frequencyNoteScaling));
                    extraContainer.add(Strut.makeStrut(transpose));
                    }
                else if (val == 5 || val == 6)                  // res1, res2
                    {
                    extraContainer.add(skirt);
                    extraContainer.add(bandwidthResonance);
                    extraContainer.add(Strut.makeStrut(frequencyNoteScaling));
                    extraContainer.add(Strut.makeStrut(transpose));
                    }
                else //if (val == 7)                    // fmt
                    {
                    extraContainer.add(skirt);
                    extraContainer.add(bandwidthResonance);
                    extraContainer.add(frequencyNoteScaling);
                    extraContainer.add(transpose);
                    }
                mode.update("operator" + src + "v" + "frequencyoscillatormode", model); // so it updates fixed/ratio frequency

                extraContainer.revalidate();
                extraContainer.repaint();
                }
            };
        vbox.add(comp);

        HBox hbox2 = new HBox();
        comp = new CheckBox("Fseq", this, "operator" + src + "v" + "switch");
        hbox2.add(comp);
        hbox2.addLast(modeContainer);
        vbox.add(hbox2);
        vbox.add(keySyncContainer);
        hbox.add(vbox);

        comp = new LabelledDial("Output", this, "operator" + src + "v" + "level", color, 0, 99);                // level
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);

        // this is actually in COMMON
        comp = new LabelledDial("Attenuation", this, "operator" + src + "v" + "carrierlevelcorrection", color, 0, 15)
            {
            public String map(int val)
                {
                return "" + -1.5 * val;
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("(dB)");
        hbox.add(comp);

        comp = new LabelledDial("Fseq", this, "operator" + src + "v" + "frequencyfseqtracknumber", color, 0, 7, -1);
        ((LabelledDial)comp).addAdditionalLabel("Track");
        hbox.add(comp);
        
        hbox.add(boxesContainer);               // coarse and fine
        
        comp = new LabelledDial("Detune", this, "operator" + src + "v" + "detune", color, 0, 30, 15)
            {
            public boolean isSymmetric() { return true; }
            };
        hbox.add(comp);

        hbox.add(extraContainer);
 
        /*
          hbox.add(frequencyNoteScaling);
    
          hbox.add(bandwidthResonance);
                                
          hbox.add(skirt);

          hbox.add(transpose);
        */
    
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
        
                
                

























                
                
                
     
    public JComponent addUnvoicedSensitivity(final int src, Color color)
        {
        final Category category = new Category(this, "Unvoiced Sensitivity " + src, color);
        category.makePasteable("operator" + src + "u");

        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();
        
        comp = new LabelledDial("Frequency", this, "operator" + src + "u" + "frequencybiassense", color, 0, 14, 7)
            {
            public boolean isSymmetric() { return true; }
            };
        ((LabelledDial)comp).addAdditionalLabel("Bias");
        hbox.add(comp);
                
        comp = new LabelledDial("Bandwidth", this, "operator" + src + "u" + "formantshapebwbiassense", color, 0, 14, 7)
            {
            public boolean isSymmetric() { return true; }
            };
        ((LabelledDial)comp).addAdditionalLabel("Bias");
        hbox.add(comp);
                
        comp = new LabelledDial("Frequency", this, "operator" + src + "u" + "frequencymodsense", color, 0, 7);
        ((LabelledDial)comp).addAdditionalLabel("Mod");
        hbox.add(comp);
                
        comp = new LabelledDial("Frequency", this, "operator" + src + "u" + "frequencyvelocitysense", color, 0, 14, 7)
            {
            public boolean isSymmetric() { return true; }
            };
        ((LabelledDial)comp).addAdditionalLabel("Velocity");
        hbox.add(comp);
                
        comp = new LabelledDial("Amp", this, "operator" + src + "u" + "ampmodsense", color, 0, 7);
        ((LabelledDial)comp).addAdditionalLabel("Mod");
        hbox.add(comp);
                
        comp = new LabelledDial("Amp", this, "operator" + src + "u" + "ampvelocitysense", color, 0, 14, 7)
            {
            public boolean isSymmetric() { return true; }
            };
        ((LabelledDial)comp).addAdditionalLabel("Velocity");
        hbox.add(comp);
                
        comp = new LabelledDial("Amp EG", this, "operator" + src + "u" + "egbiassense", color, 0, 14, 7)
            {
            public boolean isSymmetric() { return true; }
            };
        ((LabelledDial)comp).addAdditionalLabel("Bias");
        hbox.add(comp);
                
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addUnvoicedAmpEnvelope(final int src, Color color)
        {
        final Category category = new Category(this, "Unvoiced Amplitude Envelope " + src, color);
        category.makePasteable("operator" + src + "u");
        category.makeDistributable("operator" + src + "u" + "eg");

        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();
        
        comp = new LabelledDial("Hold", this, "operator" + src + "u" + "egholdtime", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("Time");
        hbox.add(comp);
        
        comp = new LabelledDial("Time 1", this, "operator" + src + "u" + "eg1time", color, 0, 99);
        hbox.add(comp);
        
        comp = new LabelledDial("Level 1", this, "operator" + src + "u" + "eg1level", color, 0, 99);
        hbox.add(comp);
        
        comp = new LabelledDial("Time 2", this, "operator" + src + "u" + "eg2time", color, 0, 99);
        hbox.add(comp);
        
        comp = new LabelledDial("Level 2", this, "operator" + src + "u" + "eg2level", color, 0, 99);
        hbox.add(comp);
        
        comp = new LabelledDial("Time 3", this, "operator" + src + "u" + "eg3time", color, 0, 99);
        hbox.add(comp);
        
        comp = new LabelledDial("Level 3", this, "operator" + src + "u" + "eg3level", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("Sustain");
        hbox.add(comp);
        
        comp = new LabelledDial("Time 4", this, "operator" + src + "u" + "eg4time", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("Release");
        hbox.add(comp);

        comp = new LabelledDial("Level 4", this, "operator" + src + "u" + "eg4level", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("Start/Rel");
        hbox.add(comp);
        
        comp = new LabelledDial("Time", this, "operator" + src + "u" + "egtimescaling", color, 0, 7);
        ((LabelledDial)comp).addAdditionalLabel("Scaling");
        hbox.add(comp);


        int envelope = 0;
        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null,                                                        "operator" + src + "u" + "egholdtime",       "operator" + src + "u" + "eg1time",  "operator" + src + "u" + "eg2time",  "operator" + src + "u" + "eg3time",          null,                                                   "operator" + src + "u" + "eg4time" },
            new String[] { "operator" + src + "u" + "eg4level",      "operator" + src + "u" + "eg4level",         "operator" + src + "u" + "eg1level",         "operator" + src + "u" + "eg2level", "operator" + src + "u" + "eg3level", "operator" + src + "u" + "eg3level", "operator" + src + "u" + "eg4level" },
            new double[] { 0, 1.0/6/99, 1.0/6/99, 1.0/6/99, 1.0/6/99, 1.0/6, 1.0/6/99 },
            new double[] { 1.0 / 99, 1.0 / 99, 1.0 / 99, 1.0 / 99, 1.0 / 99, 1.0 / 99, 1.0 / 99 });
        hbox.addLast(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addUnvoicedFrequencyEnvelope(final int src, Color color)
        {
        final Category category = new Category(this, "Unvoiced Frequency Envelope " + src, color);
        category.makePasteable("operator" + src + "u");
        category.makeDistributable("operator" + src + "u" + "frequencyeg");

        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();
        
        comp = new LabelledDial("Initial", this, "operator" + src + "u" + "frequencyeginitialvalue", color, 0, 100, 50);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);
        
        comp = new LabelledDial("Attack", this, "operator" + src + "u" + "frequencyegattacktime", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("Time");
        hbox.add(comp);
        
        comp = new LabelledDial("Attack", this, "operator" + src + "u" + "frequencyegattackvalue", color, 0, 100, 50);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);
        
        comp = new LabelledDial("Decay", this, "operator" + src + "u" + "frequencyegdecaytime", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("Time");
        hbox.add(comp);        

        int envelope = 0;
        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null,                                                                                        "operator" + src + "u" + "frequencyegattacktime",  "operator" + src + "u" + "frequencyegdecaytime" },
            new String[] { "operator" + src + "u" + "frequencyeginitialvalue",       "operator" + src + "u" + "frequencyegattackvalue", null },
            new double[] { 0, 1.0/2/99, 1.0/2/99 },
            new double[] { 1.0 / 100, 1.0 / 100, 0.5 });
        ((EnvelopeDisplay)comp).setAxis(0.5);
        hbox.addLast(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }




    public JComponent addUnvoicedOperator(final int src, Color color)
        {
        final Category category = new Category(this, "Unvoiced Operator " + src, color);
        category.makePasteable("operator" + src + "u");

        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();
    


        final HBox extraContainer = new HBox();
                

        final LabelledDial fineFixed = new LabelledDial("Frequency", this, "operator" + src + "u" + "frequencyfine", color, 0, 127)                     // frequencyfine
            {
            public String map(int val)
                {
                int c = model.get("operator" + src + "u" + "frequencycoarse", 0);
                if (c == 0) return "0";
                else return FREQUENCIES[val][c - 1];
                }               
            };
        fineFixed.addAdditionalLabel("Fine");

        final LabelledDial coarseFixed = new LabelledDial("Frequency", this, "operator" + src + "u" + "frequencycoarse", color, 0, 21)            // amazing, it's 21           // frequencycoarse
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                fineFixed.repaint();
                }
                
            public String map(int val)
                {
                if (val == 0) return "0";
                else return FREQUENCIES[0][val - 1];
                }               
            };
        coarseFixed.addAdditionalLabel("Coarse");
        
        LabelledDial frequencyScaling = new LabelledDial("Frequency", this, "operator" + src + "u" + "formantpitchnotescaling", color, 0, 99);
        ((LabelledDial)frequencyScaling).addAdditionalLabel("Key Scaling");


        VBox vbox = new VBox();
        params = FREQ_MODES;
        comp = new Chooser("Mode", this, "operator" + src + "u" + "formantpitchmode", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                int val = model.get(key);
                
                extraContainer.removeAll();

                if (val == 0)           // Normal
                    {
                    extraContainer.add(coarseFixed);
                    extraContainer.add(fineFixed);
                    extraContainer.add(frequencyScaling);
                    }
                else if (val == 2)              // Fundamental
                    {
                    extraContainer.add(Strut.makeStrut(coarseFixed));
                    extraContainer.add(Strut.makeStrut(fineFixed));
                    extraContainer.add(Strut.makeStrut(frequencyScaling));
                    }
                else // if (val == 3)                   // Formant
                    {
                    extraContainer.add(coarseFixed);
                    extraContainer.add(fineFixed);
                    extraContainer.add(Strut.makeStrut(frequencyScaling));
                    }
                        
                extraContainer.revalidate();
                extraContainer.repaint();
                }
            };
        vbox.add(comp);

        // This is actually in COMMON 
        
        comp = new CheckBox("Fseq", this, "operator" + src + "u" + "switch");
        vbox.add(comp);
        
        hbox.add(vbox);

        comp = new LabelledDial("Output", this, "operator" + src + "u" + "level", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);

        comp = new LabelledDial("Transpose", this, "operator" + src + "u" + "transpose", color, 0, 48, 24)      // transpose
            {
            public boolean isSymmetric() { return true; }
            };
        hbox.add(comp);
    

        extraContainer.add(coarseFixed);
        extraContainer.add(fineFixed);
        extraContainer.add(frequencyScaling);
        hbox.add(extraContainer);
    
        comp = new LabelledDial("Resonance", this, "operator" + src + "u" + "formantresonance", color, 0, 7);
        hbox.add(comp);

        comp = new LabelledDial("Bandwidth", this, "operator" + src + "u" + "formantshapebandwidth", color, 0, 99);
        hbox.add(comp);

        comp = new LabelledDial("Skirt", this, "operator" + src + "u" + "spectralskirt", color, 0, 7);          // spectralskirt
        hbox.add(comp);

        comp = new LabelledDial("Level Key", this, "operator" + src + "u" + "levelkeyscaling", color, 0, 14, 7)
            {
            public boolean isSymmetric() { return true; }
            };
        ((LabelledDial)comp).addAdditionalLabel("Scaling");
        hbox.add(comp);
    
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
        
                
                
     
     
     
     
                



    /** Map of parameter -> index in the allParameters array. */
    static HashMap allParametersToIndex = null;


    /** List of all Sysex parameters in order.  "-" is a reserved (unused and thus unnamed) parameter. */

    /// * indicates parameters which must be handled specially due to packing
    /// that Waldorf decided to do.  :-(

    final static String[] allParameters = new String[] 
    {
    "-",        // Name
    "-",        // Name
    "-",        // Name
    "-",        // Name
    "-",        // Name
    "-",        // Name
    "-",        // Name
    "-",        // Name
    "-",        // Name
    "-",        // Name
    "-",    // Reserved    
    "-",    // Reserved    
    "-",    // Reserved    
    "-",    // Reserved    
    "category",
    "-",    // Reserved    
    "lfo1waveform",
    "lfo1speed",
    "lfo1delay",
    "lfo1keysync",    
    "-",    // Reserved    
    "lfo1pitchmodulationdepth",
    "lfo1amplitudemodulationdepth",
    "lfo1frequencymodulationdepth",
    "lfo2waveform",
    "lfo2speed",  
    "-",    // Reserved    
    "-",    // Reserved    
    "lfo2phase",
    "lfo2keysync",
    "noteshift",
    "pitcheg0level",
    "pitcheg1level",
    "pitcheg2level",                                // missing 3?  See below
    "pitcheg4level",
    "pitcheg1time",
    "pitcheg2time",
    "pitcheg3time",
    "pitcheg4time",
    "pitchegvelocitysensitivity",
    // These deviate from the names in the sysex documentation, but by making them operatorv... we can do copy/paste
    "operator8vswitch",
    "operator17vswitch",                         // switches 1-7
    // These deviate from the names in the sysex documentation, but by making them operatoru... we can do copy/paste
    "operator8uswitch",
    "operator17uswitch",                       // switches 1-7
    "algorithmpresetnumber",
    // These deviate from the names in the sysex documentation, but by making them operatorv... we can do copy/paste
    "operator1vcarrierlevelcorrection",        
    "operator2vcarrierlevelcorrection",        
    "operator3vcarrierlevelcorrection",        
    "operator4vcarrierlevelcorrection",        
    "operator5vcarrierlevelcorrection",        
    "operator6vcarrierlevelcorrection",        
    "operator7vcarrierlevelcorrection",        
    "operator8vcarrierlevelcorrection",        
    "-",    // Reserved    
    "-",    // Reserved    
    "-",    // Reserved    
    "-",    // Reserved    
    "-",    // Reserved    
    "-",    // Reserved    
    "pitchegrange",
    "pitchegtimescalingdepth",
    "voicedfeedbacklevel",
    "pitcheg3level",                                // weird, way out of position, see above
    "-",    // Reserved    
    "formantcontrol1destination",                   // formantcontrol1dest          formantcontrol1voiced   formantcontrol1op
    "formantcontrol2destination",                   // likewise
    "formantcontrol3destination",                   // likewise
    "formantcontrol4destination",                   // likewise
    "formantcontrol5destination",                   // likewise
    "formantcontrol1depth",
    "formantcontrol2depth",
    "formantcontrol3depth",
    "formantcontrol4depth",
    "formantcontrol5depth",
    "fmcontrol1destination",                                // likewise
    "fmcontrol2destination",                                // likewise
    "fmcontrol3destination",                                // likewise
    "fmcontrol4destination",                                // likewise
    "fmcontrol5destination",                                // likewise
    "fmcontrol1depth",
    "fmcontrol2depth",
    "fmcontrol3depth",
    "fmcontrol4depth",
    "fmcontrol5depth",
    "filtertype",
    "filterresonance",
    "filterresonancevelocitysensitivity",
    "filtercutofffrequency",
    "filteregdepthvelocitysensitivity",
    "filtercutofffrequencylfo1depth",
    "filtercutofffrequencylfo2depth",
    "filtercutofffrequencykeyscaledepth",
    "filtercutofffrequencykeyscalepoint",
    "filterinputgain",
    "-",    // Reserved    
    "-",    // Reserved    
    "-",    // Reserved    
    "-",    // Reserved    
    "-",    // Reserved    
    "-",    // Reserved    
    "filteregdepth",
    "filtereg4level",               // dunno why
    "filtereg1level",
    "filtereg2level",
    "filtereg3level",
    "filtereg1time",
    "filtereg2time",
    "filtereg3time",
    "filtereg4time",
    "-",    // Reserved    
    "filteregattacktimeveltimescale",               // filteregattacktimevel        filteregtimescale
    "-",    // Reserved    



            /// voiced oscillators
        
    "operator1vkeysynctranspose",           // operator1vkeysync    operator1vtranspose
    "operator1vfrequencycoarse",
    "operator1vfrequencyfine",
    "operator1vfrequencynotescaling",
    "operator1vbwbiassensespectralform",                                            // operator1vbwbiassense                operator1vspectralform
    "operator1vfrequencyoscillatormodespectralskirtfseqtracknumber",        // operator1vfrequencyoscillatormode     operator1vspectralskirt        operator1vfrequencyfseqtracknumber
    "operator1vfrequencyratioofbandspectrum",
    "operator1vdetune",
    "operator1vfrequencyeginitialvalue",
    "operator1vfrequencyegattackvalue",
    "operator1vfrequencyegattacktime",
    "operator1vfrequencyegdecaytime",
    "operator1veg1level",
    "operator1veg2level",
    "operator1veg3level",
    "operator1veg4level",
    "operator1veg1time",
    "operator1veg2time",
    "operator1veg3time",
    "operator1veg4time",
    "operator1vegholdtime",
    "operator1vegtimescaling",
    "operator1vlevel",
    "operator1veglevelscalingbreakpoint",
    "operator1veglevelscalingleftdepth",
    "operator1veglevelscalingrightdepth",
    "operator1veglevelscalingleftcurve",
    "operator1veglevelscalingrightcurve",
    "-",    // Reserved    
    "-",    // Reserved    
    "-",    // Reserved    
    "operator1vfrequencybiassensepitchmodsense",                    // operator1vfrequencybiassense operator1vpitchmodsense
    "operator1vfrequencymodsensefrequencyvelocitysense",                    // operator1vfrequencymodsense  operator1vfrequencyvelocitysense
    "operator1vampmodsenseampvelocitysense",                                //      operator1vampmodsense   operator1vampvelocitysense
    "operator1vegbiassense",


    "operator1utranspose",
    "operator1uformantpitchmodecoarse",                     // operator1uformantpitchmode           operator1ufrequencycoarse
    "operator1ufrequencyfine",
    "operator1uformantpitchnotescaling",
    "operator1uformantshapebandwidth",
    "operator1uformantshapebwbiassense",
    "operator1uformantresonancespectralskirt",               // operator1uformantresonance   operator1uspectralskirt  operator1uformantnskt???
    "operator1ufrequencyeginitialvalue",
    "operator1ufrequencyegattackvalue",
    "operator1ufrequencyegattacktime",
    "operator1ufrequencyegdecaytime",
    "operator1ulevel",
    "operator1ulevelkeyscaling",
    "operator1ueg1level",
    "operator1ueg2level",
    "operator1ueg3level",
    "operator1ueg4level",
    "operator1ueg1time",
    "operator1ueg2time",
    "operator1ueg3time",
    "operator1ueg4time",
    "operator1uegholdtime",
    "operator1uegtimescaling",
    "operator1ufrequencybiassense",
    "operator1ufrequencymodsensefrequencyvelocitysense",            // operator1ufrequencymodsense          operator1ufrequencyvelocitysense
    "operator1uampmodsenseampvelocitysense",                                        // operator1uampmodsense                        operator1uampvelocitysense
    "operator1uegbiassense",



    "operator2vkeysynctranspose",
    "operator2vfrequencycoarse",
    "operator2vfrequencyfine",
    "operator2vfrequencynotescaling",
    "operator2vbwbiassensespectralform",
    "operator2vfrequencyoscillatormodespectralskirtfseqtracknumber",
    "operator2vfrequencyratioofbandspectrum",
    "operator2vdetune",
    "operator2vfrequencyeginitialvalue",
    "operator2vfrequencyegattackvalue",
    "operator2vfrequencyegattacktime",
    "operator2vfrequencyegdecaytime",
    "operator2veg1level",
    "operator2veg2level",
    "operator2veg3level",
    "operator2veg4level",
    "operator2veg1time",
    "operator2veg2time",
    "operator2veg3time",
    "operator2veg4time",
    "operator2vegholdtime",
    "operator2vegtimescaling",
    "operator2vlevel",
    "operator2veglevelscalingbreakpoint",
    "operator2veglevelscalingleftdepth",
    "operator2veglevelscalingrightdepth",
    "operator2veglevelscalingleftcurve",
    "operator2veglevelscalingrightcurve",
    "-",    // Reserved    
    "-",    // Reserved    
    "-",    // Reserved    
    "operator2vfrequencybiassensepitchmodsense",
    "operator2vfrequencymodsensefrequencyvelocitysense",
    "operator2vampmodsenseampvelocitysense",
    "operator2vegbiassense",


    "operator2utranspose",
    "operator2uformantpitchmodecoarse",
    "operator2ufrequencyfine",
    "operator2uformantpitchnotescaling",
    "operator2uformantshapebandwidth",
    "operator2uformantshapebwbiassense",
    "operator2uformantresonancespectralskirt",
    "operator2ufrequencyeginitialvalue",
    "operator2ufrequencyegattackvalue",
    "operator2ufrequencyegattacktime",
    "operator2ufrequencyegdecaytime",
    "operator2ulevel",
    "operator2ulevelkeyscaling",
    "operator2ueg1level",
    "operator2ueg2level",
    "operator2ueg3level",
    "operator2ueg4level",
    "operator2ueg1time",
    "operator2ueg2time",
    "operator2ueg3time",
    "operator2ueg4time",
    "operator2uegholdtime",
    "operator2uegtimescaling",
    "operator2ufrequencybiassense",
    "operator2ufrequencymodsensefrequencyvelocitysense",
    "operator2uampmodsenseampvelocitysense",
    "operator2uegbiassense",


    "operator3vkeysynctranspose",
    "operator3vfrequencycoarse",
    "operator3vfrequencyfine",
    "operator3vfrequencynotescaling",
    "operator3vbwbiassensespectralform",
    "operator3vfrequencyoscillatormodespectralskirtfseqtracknumber",
    "operator3vfrequencyratioofbandspectrum",
    "operator3vdetune",
    "operator3vfrequencyeginitialvalue",
    "operator3vfrequencyegattackvalue",
    "operator3vfrequencyegattacktime",
    "operator3vfrequencyegdecaytime",
    "operator3veg1level",
    "operator3veg2level",
    "operator3veg3level",
    "operator3veg4level",
    "operator3veg1time",
    "operator3veg2time",
    "operator3veg3time",
    "operator3veg4time",
    "operator3vegholdtime",
    "operator3vegtimescaling",
    "operator3vlevel",
    "operator3veglevelscalingbreakpoint",
    "operator3veglevelscalingleftdepth",
    "operator3veglevelscalingrightdepth",
    "operator3veglevelscalingleftcurve",
    "operator3veglevelscalingrightcurve",
    "-",    // Reserved    
    "-",    // Reserved    
    "-",    // Reserved    
    "operator3vfrequencybiassensepitchmodsense",
    "operator3vfrequencymodsensefrequencyvelocitysense",
    "operator3vampmodsenseampvelocitysense",
    "operator3vegbiassense",


    "operator3utranspose",
    "operator3uformantpitchmodecoarse",
    "operator3ufrequencyfine",
    "operator3uformantpitchnotescaling",
    "operator3uformantshapebandwidth",
    "operator3uformantshapebwbiassense",
    "operator3uformantresonancespectralskirt",
    "operator3ufrequencyeginitialvalue",
    "operator3ufrequencyegattackvalue",
    "operator3ufrequencyegattacktime",
    "operator3ufrequencyegdecaytime",
    "operator3ulevel",
    "operator3ulevelkeyscaling",
    "operator3ueg1level",
    "operator3ueg2level",
    "operator3ueg3level",
    "operator3ueg4level",
    "operator3ueg1time",
    "operator3ueg2time",
    "operator3ueg3time",
    "operator3ueg4time",
    "operator3uegholdtime",
    "operator3uegtimescaling",
    "operator3ufrequencybiassense",
    "operator3ufrequencymodsensefrequencyvelocitysense",
    "operator3uampmodsenseampvelocitysense",
    "operator3uegbiassense",


    "operator4vkeysynctranspose",
    "operator4vfrequencycoarse",
    "operator4vfrequencyfine",
    "operator4vfrequencynotescaling",
    "operator4vbwbiassensespectralform",
    "operator4vfrequencyoscillatormodespectralskirtfseqtracknumber",
    "operator4vfrequencyratioofbandspectrum",
    "operator4vdetune",
    "operator4vfrequencyeginitialvalue",
    "operator4vfrequencyegattackvalue",
    "operator4vfrequencyegattacktime",
    "operator4vfrequencyegdecaytime",
    "operator4veg1level",
    "operator4veg2level",
    "operator4veg3level",
    "operator4veg4level",
    "operator4veg1time",
    "operator4veg2time",
    "operator4veg3time",
    "operator4veg4time",
    "operator4vegholdtime",
    "operator4vegtimescaling",
    "operator4vlevel",
    "operator4veglevelscalingbreakpoint",
    "operator4veglevelscalingleftdepth",
    "operator4veglevelscalingrightdepth",
    "operator4veglevelscalingleftcurve",
    "operator4veglevelscalingrightcurve",
    "-",    // Reserved    
    "-",    // Reserved    
    "-",    // Reserved    
    "operator4vfrequencybiassensepitchmodsense",
    "operator4vfrequencymodsensefrequencyvelocitysense",
    "operator4vampmodsenseampvelocitysense",
    "operator4vegbiassense",



    "operator4utranspose",
    "operator4uformantpitchmodecoarse",
    "operator4ufrequencyfine",
    "operator4uformantpitchnotescaling",
    "operator4uformantshapebandwidth",
    "operator4uformantshapebwbiassense",
    "operator4uformantresonancespectralskirt",
    "operator4ufrequencyeginitialvalue",
    "operator4ufrequencyegattackvalue",
    "operator4ufrequencyegattacktime",
    "operator4ufrequencyegdecaytime",
    "operator4ulevel",
    "operator4ulevelkeyscaling",
    "operator4ueg1level",
    "operator4ueg2level",
    "operator4ueg3level",
    "operator4ueg4level",
    "operator4ueg1time",
    "operator4ueg2time",
    "operator4ueg3time",
    "operator4ueg4time",
    "operator4uegholdtime",
    "operator4uegtimescaling",
    "operator4ufrequencybiassense",
    "operator4ufrequencymodsensefrequencyvelocitysense",
    "operator4uampmodsenseampvelocitysense",
    "operator4uegbiassense",


    "operator5vkeysynctranspose",
    "operator5vfrequencycoarse",
    "operator5vfrequencyfine",
    "operator5vfrequencynotescaling",
    "operator5vbwbiassensespectralform",
    "operator5vfrequencyoscillatormodespectralskirtfseqtracknumber",
    "operator5vfrequencyratioofbandspectrum",
    "operator5vdetune",
    "operator5vfrequencyeginitialvalue",
    "operator5vfrequencyegattackvalue",
    "operator5vfrequencyegattacktime",
    "operator5vfrequencyegdecaytime",
    "operator5veg1level",
    "operator5veg2level",
    "operator5veg3level",
    "operator5veg4level",
    "operator5veg1time",
    "operator5veg2time",
    "operator5veg3time",
    "operator5veg4time",
    "operator5vegholdtime",
    "operator5vegtimescaling",
    "operator5vlevel",
    "operator5veglevelscalingbreakpoint",
    "operator5veglevelscalingleftdepth",
    "operator5veglevelscalingrightdepth",
    "operator5veglevelscalingleftcurve",
    "operator5veglevelscalingrightcurve",
    "-",    // Reserved    
    "-",    // Reserved    
    "-",    // Reserved    
    "operator5vfrequencybiassensepitchmodsense",
    "operator5vfrequencymodsensefrequencyvelocitysense",
    "operator5vampmodsenseampvelocitysense",
    "operator5vegbiassense",


    "operator5utranspose",
    "operator5uformantpitchmodecoarse",
    "operator5ufrequencyfine",
    "operator5uformantpitchnotescaling",
    "operator5uformantshapebandwidth",
    "operator5uformantshapebwbiassense",
    "operator5uformantresonancespectralskirt",
    "operator5ufrequencyeginitialvalue",
    "operator5ufrequencyegattackvalue",
    "operator5ufrequencyegattacktime",
    "operator5ufrequencyegdecaytime",
    "operator5ulevel",
    "operator5ulevelkeyscaling",
    "operator5ueg1level",
    "operator5ueg2level",
    "operator5ueg3level",
    "operator5ueg4level",
    "operator5ueg1time",
    "operator5ueg2time",
    "operator5ueg3time",
    "operator5ueg4time",
    "operator5uegholdtime",
    "operator5uegtimescaling",
    "operator5ufrequencybiassense",
    "operator5ufrequencymodsensefrequencyvelocitysense",
    "operator5uampmodsenseampvelocitysense",
    "operator5uegbiassense",


    "operator6vkeysynctranspose",
    "operator6vfrequencycoarse",
    "operator6vfrequencyfine",
    "operator6vfrequencynotescaling",
    "operator6vbwbiassensespectralform",
    "operator6vfrequencyoscillatormodespectralskirtfseqtracknumber",
    "operator6vfrequencyratioofbandspectrum",
    "operator6vdetune",
    "operator6vfrequencyeginitialvalue",
    "operator6vfrequencyegattackvalue",
    "operator6vfrequencyegattacktime",
    "operator6vfrequencyegdecaytime",
    "operator6veg1level",
    "operator6veg2level",
    "operator6veg3level",
    "operator6veg4level",
    "operator6veg1time",
    "operator6veg2time",
    "operator6veg3time",
    "operator6veg4time",
    "operator6vegholdtime",
    "operator6vegtimescaling",
    "operator6vlevel",
    "operator6veglevelscalingbreakpoint",
    "operator6veglevelscalingleftdepth",
    "operator6veglevelscalingrightdepth",
    "operator6veglevelscalingleftcurve",
    "operator6veglevelscalingrightcurve",
    "-",    // Reserved    
    "-",    // Reserved    
    "-",    // Reserved    
    "operator6vfrequencybiassensepitchmodsense",
    "operator6vfrequencymodsensefrequencyvelocitysense",
    "operator6vampmodsenseampvelocitysense",
    "operator6vegbiassense",


    "operator6utranspose",
    "operator6uformantpitchmodecoarse",
    "operator6ufrequencyfine",
    "operator6uformantpitchnotescaling",
    "operator6uformantshapebandwidth",
    "operator6uformantshapebwbiassense",
    "operator6uformantresonancespectralskirt",
    "operator6ufrequencyeginitialvalue",
    "operator6ufrequencyegattackvalue",
    "operator6ufrequencyegattacktime",
    "operator6ufrequencyegdecaytime",
    "operator6ulevel",
    "operator6ulevelkeyscaling",
    "operator6ueg1level",
    "operator6ueg2level",
    "operator6ueg3level",
    "operator6ueg4level",
    "operator6ueg1time",
    "operator6ueg2time",
    "operator6ueg3time",
    "operator6ueg4time",
    "operator6uegholdtime",
    "operator6uegtimescaling",
    "operator6ufrequencybiassense",
    "operator6ufrequencymodsensefrequencyvelocitysense",
    "operator6uampmodsenseampvelocitysense",
    "operator6uegbiassense",


    "operator7vkeysynctranspose",
    "operator7vfrequencycoarse",
    "operator7vfrequencyfine",
    "operator7vfrequencynotescaling",
    "operator7vbwbiassensespectralform",
    "operator7vfrequencyoscillatormodespectralskirtfseqtracknumber",
    "operator7vfrequencyratioofbandspectrum",
    "operator7vdetune",
    "operator7vfrequencyeginitialvalue",
    "operator7vfrequencyegattackvalue",
    "operator7vfrequencyegattacktime",
    "operator7vfrequencyegdecaytime",
    "operator7veg1level",
    "operator7veg2level",
    "operator7veg3level",
    "operator7veg4level",
    "operator7veg1time",
    "operator7veg2time",
    "operator7veg3time",
    "operator7veg4time",
    "operator7vegholdtime",
    "operator7vegtimescaling",
    "operator7vlevel",
    "operator7veglevelscalingbreakpoint",
    "operator7veglevelscalingleftdepth",
    "operator7veglevelscalingrightdepth",
    "operator7veglevelscalingleftcurve",
    "operator7veglevelscalingrightcurve",
    "-",    // Reserved    
    "-",    // Reserved    
    "-",    // Reserved    
    "operator7vfrequencybiassensepitchmodsense",
    "operator7vfrequencymodsensefrequencyvelocitysense",
    "operator7vampmodsenseampvelocitysense",
    "operator7vegbiassense",


    "operator7utranspose",
    "operator7uformantpitchmodecoarse",
    "operator7ufrequencyfine",
    "operator7uformantpitchnotescaling",
    "operator7uformantshapebandwidth",
    "operator7uformantshapebwbiassense",
    "operator7uformantresonancespectralskirt",
    "operator7ufrequencyeginitialvalue",
    "operator7ufrequencyegattackvalue",
    "operator7ufrequencyegattacktime",
    "operator7ufrequencyegdecaytime",
    "operator7ulevel",
    "operator7ulevelkeyscaling",
    "operator7ueg1level",
    "operator7ueg2level",
    "operator7ueg3level",
    "operator7ueg4level",
    "operator7ueg1time",
    "operator7ueg2time",
    "operator7ueg3time",
    "operator7ueg4time",
    "operator7uegholdtime",
    "operator7uegtimescaling",
    "operator7ufrequencybiassense",
    "operator7ufrequencymodsensefrequencyvelocitysense",
    "operator7uampmodsenseampvelocitysense",
    "operator7uegbiassense",


    "operator8vkeysynctranspose",
    "operator8vfrequencycoarse",
    "operator8vfrequencyfine",
    "operator8vfrequencynotescaling",
    "operator8vbwbiassensespectralform",
    "operator8vfrequencyoscillatormodespectralskirtfseqtracknumber",
    "operator8vfrequencyratioofbandspectrum",
    "operator8vdetune",
    "operator8vfrequencyeginitialvalue",
    "operator8vfrequencyegattackvalue",
    "operator8vfrequencyegattacktime",
    "operator8vfrequencyegdecaytime",
    "operator8veg1level",
    "operator8veg2level",
    "operator8veg3level",
    "operator8veg4level",
    "operator8veg1time",
    "operator8veg2time",
    "operator8veg3time",
    "operator8veg4time",
    "operator8vegholdtime",
    "operator8vegtimescaling",
    "operator8vlevel",
    "operator8veglevelscalingbreakpoint",
    "operator8veglevelscalingleftdepth",
    "operator8veglevelscalingrightdepth",
    "operator8veglevelscalingleftcurve",
    "operator8veglevelscalingrightcurve",
    "-",    // Reserved    
    "-",    // Reserved    
    "-",    // Reserved    
    "operator8vfrequencybiassensepitchmodsense",
    "operator8vfrequencymodsensefrequencyvelocitysense",
    "operator8vampmodsenseampvelocitysense",
    "operator8vegbiassense",



    "operator8utranspose",
    "operator8uformantpitchmodecoarse",
    "operator8ufrequencyfine",
    "operator8uformantpitchnotescaling",
    "operator8uformantshapebandwidth",
    "operator8uformantshapebwbiassense",
    "operator8uformantresonancespectralskirt",
    "operator8ufrequencyeginitialvalue",
    "operator8ufrequencyegattackvalue",
    "operator8ufrequencyegattacktime",
    "operator8ufrequencyegdecaytime",
    "operator8ulevel",
    "operator8ulevelkeyscaling",
    "operator8ueg1level",
    "operator8ueg2level",
    "operator8ueg3level",
    "operator8ueg4level",
    "operator8ueg1time",
    "operator8ueg2time",
    "operator8ueg3time",
    "operator8ueg4time",
    "operator8uegholdtime",
    "operator8uegtimescaling",
    "operator8ufrequencybiassense",
    "operator8ufrequencymodsensefrequencyvelocitysense",
    "operator8uampmodsenseampvelocitysense",
    "operator8uegbiassense",
    };
    
        
    // common = 0, +op is voiced, -op is unvoiced
    public int getOperator(String key)
        {
        if (key.startsWith("operator"))
            {
            // these are common
            if (key.startsWith("operator17") || key.equals("operator8vswitch") || key.equals("operator8uswitch"))
                {
                return 0;
                }
            else if (key.endsWith("levelcorrection"))              // these are common
                {
                return 0;
                }
            else if (key.charAt(9) == 'u')
                {
                return 0 - StringUtility.getFirstInt(key);
                }
            else
                {
                return StringUtility.getFirstInt(key);
                }
            }
        else return 0;
        }
        
    int extractAddress(String key)
        {
        Object obj = allParametersToIndex.get(key);
        if (obj == null)
            {
            System.err.println("Warning (YamahaFS1R) extractAddress: no such key " + key);
            return -1;
            }
        else 
            {
            return ((Integer)obj).intValue();
            }
        }
                
    public int getAddress(String key)               // returns the COMBINED address appropriate to emit
        {
        int op = getOperator(key);
        if (op == 0)  // common
            {
            if (key.startsWith("formantcontrol") && (key.endsWith("dest") || key.endsWith("voiced") ||  key.endsWith("op")))
                {
                int num = StringUtility.getFirstInt(key);
                return extractAddress("formantcontrol" + num + "destination");
                }
            else if (key.startsWith("fmcontrol") && (key.endsWith("dest") || key.endsWith("voiced") ||  key.endsWith("op")))
                {
                int num = StringUtility.getFirstInt(key);
                return extractAddress("fmcontrol" + num + "destination");
                }
            else if (key.equals("filteregattacktimevel") || key.equals("filteregtimescale"))
                {
                return extractAddress("filteregattacktimeveltimescale");
                }
            else 
                {
                return extractAddress(key);
                }
            }
        else if (op < 0)                // unvoiced
            {
            op = 0 - op;

            // note that for this if-statment, op will be wrong but that's okay, we don't need it
            if (key.endsWith("switch"))
                {
                int num = StringUtility.getFirstInt(key);
                if (num < 8)
                    return extractAddress("operator17uswitch");
                else
                    return extractAddress(key);
                }

            String baseKey = key.substring(10);  // strip "operator3u"

            if (baseKey.equals("formantpitchmode") || baseKey.startsWith("frequencycoarse"))
                {
                return extractAddress("operator" + op + "u" + "formantpitchmodecoarse");
                }
            else if (baseKey.equals("formantresonance") || baseKey.equals("spectralskirt"))
                {
                return extractAddress("operator" + op + "u" + "formantresonancespectralskirt");
                }
            else if (baseKey.equals("frequencymodsense") || baseKey.equals("frequencyvelocitysense"))
                {
                return extractAddress("operator" + op + "u" + "frequencymodsensefrequencyvelocitysense");
                }
            else if (baseKey.equals("ampmodsense") || baseKey.equals("ampvelocitysense"))
                {
                return extractAddress("operator" + op + "u" + "ampmodsenseampvelocitysense");
                }
            else
                {
                return extractAddress(key);
                }
            }
        else                                    // voiced
            {
            // note that for this if-statment, op will be wrong but that's okay, we don't need it
            if (key.endsWith("switch"))
                {
                int num = StringUtility.getFirstInt(key);
                if (num < 8)
                    return extractAddress("operator17vswitch");
                else
                    return extractAddress(key);
                }

            String baseKey = key.substring(10);  // strip "operator3v"
                        
            if (baseKey.startsWith("frequencycoarse"))
                {
                if (baseKey.equals("frequencycoarseratio") && model.get("operator" + op + "v" + "frequencyoscillatormode") == 0)
                    {
                    return extractAddress("operator" + op + "v" + "frequencycoarse");
                    }
                else if (baseKey.equals("frequencycoarsefixed") && 
                        ((model.get("operator" + op + "v" + "frequencyoscillatormode") == 1) ||
                        (model.get("operator" + op + "v" + "spectralform", 0) == 7)))                          // formant forces this to be be fixed
                    {
                    return extractAddress("operator" + op + "v" + "frequencycoarse");
                    }
                else
                    {
                    //System.err.println("Um... " + baseKey);
                    //                    Synth.handleException(new Throwable());
                    return -1;
                    }
                }
            else if (baseKey.startsWith("frequencyfine"))
                {
                if (baseKey.equals("frequencyfineratio") && model.get("operator" + op + "v" + "frequencyoscillatormode") == 0)
                    {
                    return extractAddress("operator" + op + "v" + "frequencyfine");
                    }
                else if (baseKey.equals("frequencyfinefixed") && 
                        ((model.get("operator" + op + "v" + "frequencyoscillatormode") == 1) ||
                        (model.get("operator" + op + "v" + "spectralform", 0) == 7)))
                    {
                    return extractAddress("operator" + op + "v" + "frequencyfine");
                    }
                else
                    {
                    //System.err.println("Um... " + baseKey);
                    return -1;
                    }
                }
            else if (baseKey.equals("keysync") || baseKey.equals("transpose"))
                {
                return extractAddress("operator" + op + "v" + "keysynctranspose");
                }
            else if (baseKey.equals("bwbiassense") || baseKey.equals("spectralform"))
                {
                return extractAddress("operator" + op + "v" + "bwbiassensespectralform");
                }
            else if (baseKey.equals("frequencyoscillatormode") || baseKey.equals("spectralskirt") || baseKey.equals("frequencyfseqtracknumber"))
                {
                return extractAddress("operator" + op + "v" + "frequencyoscillatormodespectralskirtfseqtracknumber");
                }
            else if (baseKey.equals("frequencybiassense") || baseKey.equals("pitchmodsense"))
                {
                return extractAddress("operator" + op + "v" + "frequencybiassensepitchmodsense");
                }
            else if (baseKey.equals("frequencymodsense") || baseKey.equals("frequencyvelocitysense"))
                {
                return extractAddress("operator" + op + "v" + "frequencymodsensefrequencyvelocitysense");
                }
            else if (baseKey.equals("ampmodsense") || baseKey.equals("ampvelocitysense"))
                {
                return extractAddress("operator" + op + "v" + "ampmodsenseampvelocitysense");
                }
            else
                {
                return extractAddress(key);
                }
            }
        }
        
    public int getValue(String key)         // returns the COMBINED value appropriate to emit
        {
        if (key.equals("-")) return 0;
                
        int op = getOperator(key);
        if (op == 0)            // common
            {
            if (key.startsWith("formantcontrol") && (key.endsWith("destination")))
                {
                int num = StringUtility.getFirstInt(key);
                return  (model.get("formantcontrol" + num + "dest") << 4) |
                    (model.get("formantcontrol" + num + "voiced") << 3) |
                    (model.get("formantcontrol" + num + "op") << 0);
                }
            else if (key.startsWith("fmcontrol") && (key.endsWith("destination")))
                {
                int num = StringUtility.getFirstInt(key);
                return  (model.get("fmcontrol" + num + "dest") << 4) |
                    (model.get("fmcontrol" + num + "voiced") << 3) |
                    (model.get("fmcontrol" + num + "op") << 0);
                }
            else if (key.equals("filteregattacktimeveltimescale"))
                {
                return  (model.get("filteregattacktimevel") << 3) |
                    (model.get("filteregtimescale") << 0);
                }
            // note that for this if-statment, op will be wrong but that's okay, we don't need it
            else if (key.equals("operator17uswitch"))
                {
                return  (model.get("operator1uswitch") << 0) |
                    (model.get("operator2uswitch") << 1) |
                    (model.get("operator3uswitch") << 2) |
                    (model.get("operator4uswitch") << 3) |
                    (model.get("operator5uswitch") << 4) |
                    (model.get("operator6uswitch") << 5) |
                    (model.get("operator7uswitch") << 6);
                }
            // note that for this if-statment, op will be wrong but that's okay, we don't need it
            else if (key.equals("operator17vswitch"))
                {
                return  (model.get("operator1vswitch") << 0) |
                    (model.get("operator2vswitch") << 1) |
                    (model.get("operator3vswitch") << 2) |
                    (model.get("operator4vswitch") << 3) |
                    (model.get("operator5vswitch") << 4) |
                    (model.get("operator6vswitch") << 5) |
                    (model.get("operator7vswitch") << 6);
                }
            else
                {
                return model.get(key);
                }
            }
        else if (op < 0)        // unvoiced
            {
            op = 0 - op;
            if (key.equals("operator" + op + "u" + "formantpitchmodecoarse"))
                {
                return  (model.get("operator" + op + "u" + "formantpitchmode") << 5) |
                    (model.get("operator" + op + "u" + "frequencycoarse") << 0);
                }
            else if (key.equals("operator" + op + "u" + "formantresonancespectralskirt"))
                {
                return  (model.get("operator" + op + "u" + "formantresonance") << 3) |
                    (model.get("operator" + op + "u" + "spectralskirt") << 0);
                }
            else if (key.equals("operator" + op + "u" + "frequencymodsensefrequencyvelocitysense"))
                {
                return  (model.get("operator" + op + "u" + "frequencymodsense") << 4) |
                    (model.get("operator" + op + "u" + "frequencyvelocitysense") << 0);
                }
            else if (key.equals("operator" + op + "u" + "ampmodsenseampvelocitysense"))
                {
                return  (model.get("operator" + op + "u" + "ampmodsense") << 4) |
                    (model.get("operator" + op + "u" + "ampvelocitysense") << 0);
                }
            else
                {
                return model.get(key);
                }
            }
        else                            //voiced
            {
            if (key.equals("operator" + op + "v" + "frequencycoarse"))
                {
                if (model.get("operator" + op + "v" + "frequencyoscillatormode") == 0)  // ratio
                    {
                    return (model.get("operator" + op + "v" + "frequencycoarseratio"));
                    }
                else
                    {
                    return (model.get("operator" + op + "v" + "frequencycoarsefixed"));
                    }
                }
            else if (key.equals("operator" + op + "v" + "frequencyfine"))
                {
                if (model.get("operator" + op + "v" + "frequencyoscillatormode") == 0)  // ratio
                    {
                    return (model.get("operator" + op + "v" + "frequencyfineratio"));
                    }
                else
                    {
                    return (model.get("operator" + op + "v" + "frequencyfinefixed"));
                    }
                }
            else if (key.equals("operator" + op + "v" + "keysynctranspose"))
                {
                return  (model.get("operator" + op + "v" + "keysync") << 6) |
                    (model.get("operator" + op + "v" + "transpose") << 0);
                }
            else if (key.equals("operator" + op + "v" + "bwbiassensespectralform"))
                {
                return  (model.get("operator" + op + "v" + "bwbiassense") << 3) |
                    (model.get("operator" + op + "v" + "spectralform") << 0);
                }
            else if (key.equals("operator" + op + "v" + "frequencyoscillatormodespectralskirtfseqtracknumber"))
                {
                return  (model.get("operator" + op + "v" + "frequencyoscillatormode") << 6) |
                    (model.get("operator" + op + "v" + "spectralskirt") << 3) |
                    (model.get("operator" + op + "v" + "frequencyfseqtracknumber") << 0);
                }
            else if (key.equals("operator" + op + "v" + "frequencybiassensepitchmodsense"))
                {
                return  (model.get("operator" + op + "v" + "frequencybiassense") << 3) |
                    (model.get("operator" + op + "v" + "pitchmodsense") << 0);
                }
            else if (key.equals("operator" + op + "v" + "frequencymodsensefrequencyvelocitysense"))
                {
                return  (model.get("operator" + op + "v" + "frequencymodsense") << 4) |
                    (model.get("operator" + op + "v" + "frequencyvelocitysense") << 0);
                }
            else if (key.equals("operator" + op + "v" + "ampmodsenseampvelocitysense"))
                {
                return  (model.get("operator" + op + "v" + "ampmodsense") << 4) |
                    (model.get("operator" + op + "v" + "ampvelocitysense") << 0);
                }
            else
                {
                return model.get(key);
                }
            }
        }


    void muteOperator(int op, boolean voiced, boolean mute)
        {
        // F0 43 1n 5E 6p mm ll vv vv F7
        // n: id
        // p: part
        // mm: operator
        // ll: parameter
        // vv vv:   00 mute 
        byte[] data = null;
        if (voiced)
            {
            data = new byte[] { (byte)0xF0, (byte)0x43, (byte)(16 + getID() - 1), (byte)0x5E, 
                (byte)(96 + part),                      // HIGH: part and common
                (byte)op,
                (byte)0x16,                                                         // voiced total level
                (byte)0,
                (byte)(mute ? 0 : model.get("operator" + (op + 1) + "vlevel")),
                (byte)0xF7 };      
            }
        else
            {
            data = new byte[] { (byte)0xF0, (byte)0x43, (byte)(16 + getID() - 1), (byte)0x5E, 
                (byte)(96 + part),                      // HIGH: part and common
                (byte)op,
                (byte)0x2E,                                                                 //unvoiced total level
                (byte)0,
                (byte)(mute ? 0 : model.get("operator" + (op + 1) + "ulevel")),
                (byte)0xF7 };
            }
        tryToSendSysex(data);
        }        

    void setFseqSwitches(int[] voiced, int[] unvoiced)
        {
        // F0 43 1n 5E 4p 00 ll vv vv F7
        // n: id
        // p: part
        // ll: parameter
        // vv vv:   00 mute 
        byte[] data = new byte[] { (byte)0xF0, (byte)0x43, (byte)(16 + getID() - 1), (byte)0x5E, 
            (byte)(64 + part),                      // HIGH: part and common
            (byte)0,
            (byte)0x28,                                                             // voiced switch 8
            (byte)0,
            (byte)voiced[7],
            (byte)0xF7 };      
        tryToSendSysex(data);
        data = new byte[] { (byte)0xF0, (byte)0x43, (byte)(16 + getID() - 1), (byte)0x5E, 
            (byte)(64 + part),                      // HIGH: part and common
            (byte)0,
            (byte)0x29,                                                             // voiced switches 1..7
            (byte)0,
            (byte)((voiced[0]) | (voiced[1] << 1) | (voiced[2] << 2) | (voiced[3] << 3) | (voiced[4] << 4) | (voiced[5] << 5) | (voiced[6] << 6)),
            (byte)0xF7 };      
        tryToSendSysex(data);
        data = new byte[] { (byte)0xF0, (byte)0x43, (byte)(16 + getID() - 1), (byte)0x5E, 
            (byte)(64 + part),                      // HIGH: part and common
            (byte)0,
            (byte)0x2A,                                                             // unvoiced switch 8
            (byte)0,
            (byte)unvoiced[7],
            (byte)0xF7 };      
        tryToSendSysex(data);
        data = new byte[] { (byte)0xF0, (byte)0x43, (byte)(16 + getID() - 1), (byte)0x5E, 
            (byte)(64 + part),                      // HIGH: part and common
            (byte)0,
            (byte)0x2B,                                                             // unvoiced switches 1..7
            (byte)0,
            (byte)((unvoiced[0]) | (unvoiced[1] << 1) | (unvoiced[2] << 2) | (unvoiced[3] << 3) | (unvoiced[4] << 4) | (unvoiced[5] << 5) | (unvoiced[6] << 6)),
            (byte)0xF7 };      
        tryToSendSysex(data);
        }        
            
    
    public Object[] emitAll(String key)
        {
        if (key.equals("number")) return new Object[0];  // this is not emittable
        if (key.equals("bank")) return new Object[0];  // this is not emittable

        if (key.equals("name"))
            {
            Object[] result = new Object[10];
            String name = model.get("name", "INIT VOICE") + "          ";

            for(int i = 0; i < 10; i++)
                {
                int ADDRESS = i;                // we're at the very beginning, so our addresses just happen to be 0...9
                int LSB = (byte)(name.charAt(i));
                byte[] data = new byte[] { (byte)0xF0, (byte)0x43, (byte)(16 + getID() - 1), (byte)0x5E, 
                    (byte)(64 + part),                      // HIGH: part and common
                    (byte)0,                                // MEDIUM : operator (common)
                    (byte)ADDRESS,                          // LOW: relative address
                    (byte)0,
                    (byte)LSB, 
                    (byte)0xF7 };
                result[i] = data;
                }
            return result;
            }
        else
            {
            int ADDRESS = getAddress(key);
            if (ADDRESS == -1)
                {
                System.err.println("Warning (YamahaFS1R): Can't emit key " + key);
                return new Object[0];
                }
            else
                {
                int op = getOperator(allParameters[ADDRESS]);
                int val = getValue(allParameters[ADDRESS]);
                int MSB = (val >>> 7) & 127;
                int LSB = (val & 127);
                
                int address = (ADDRESS < 112 ? ADDRESS :  // common
                    ((ADDRESS - 112) % 62));        // per-op (112 common vals, 62 op vals, 8 ops = 608)
                        
                byte[] data = new byte[] { (byte)0xF0, (byte)0x43, (byte)(16 + getID() - 1), (byte)0x5E, 
                    (byte)((op == 0 ? 64 : 96) + part),                             // HIGH : part and common
                    (byte)((op == 0 ? 0 : (op < 0 ? -op - 1 : op - 1))),    // MEDIUM: operator
                    (byte)address,                          // LOW: relative address
                    (byte)MSB,
                    (byte)LSB, 
                    (byte)0xF7 };

                return new Object[] { data };
                }
            }
        }


    public byte[] emit(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        if (tempModel == null)
            tempModel = getModel();

        final int BYTE_COUNT = 608;
                
        byte[] data = new byte[BYTE_COUNT + 11];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x43;
        data[2] = (byte)(getID() - 1);
        data[3] = (byte)0x5E;
        data[4] = (byte)((BYTE_COUNT >>> 7) & 127);
        data[5] = (byte)(BYTE_COUNT & 127);
        data[6] = (byte)(toWorkingMemory ? 0x40 + part : 0x51);
        data[7] = (byte)0x0;
        data[8] = (byte)(toWorkingMemory ? 0x00 : tempModel.get("number"));
        
        String name = model.get("name", "INIT VOICE") + "          ";
        for(int i = 0; i < 10; i++)     
            {
            data[i + 9] = (byte)(name.charAt(i));
            }

        // this is simple but very computationally costly due to the calls to getValue(), which are expensive. 
        // It may take too long to compute, in which case we'll have to use the same approach
        // as parse(), ugh.
        // It's about 20ms on my machine
        for(int i = 10; i < BYTE_COUNT; i++)
            {
            // All parameters are 1 byte each so this is straightforward.
            // This won't be for the performance editor
            data[i + 9] = (byte)getValue(allParameters[i]);
            }
                
        data[data.length - 2] = produceChecksum(data, 4);
        data[data.length - 1] = (byte)0xF7;
        return data;
        }


    public static final int POS_operator17vswitch = 41;
    public static final int POS_operator17uswitch = 43;
    public static final int POS_formantcontrol1destination = 64;
    public static final int POS_fmcontrol1destination = 74;
    public static final int POS_filteregattacktimeveltimescale = 110;
    public static final int POS_OPERATOR_START = 112;
    public static final int POS_OPERATOR_LENGTH = 62;
    public static final int POS_operator1vkeysynctranspose = 112 - POS_OPERATOR_START;
    public static final int POS_operator1vfrequencycoarse = 113 - POS_OPERATOR_START;
    public static final int POS_operator1vfrequencyfine = 114 - POS_OPERATOR_START;
    public static final int POS_operator1vbwbiassensespectralform = 116 - POS_OPERATOR_START;
    public static final int POS_operator1vfrequencyoscillatormodespectralskirtfseqtracknumber = 117 - POS_OPERATOR_START;
    public static final int POS_operator1vfrequencybiassensepitchmodsense = 143 - POS_OPERATOR_START;
    public static final int POS_operator1vfrequencymodsensefrequencyvelocitysense = 144 - POS_OPERATOR_START;
    public static final int POS_operator1vampmodsenseampvelocitysense = 145 - POS_OPERATOR_START;
    public static final int POS_operator1uformantpitchmodecoarse = 148 - POS_OPERATOR_START;
    public static final int POS_operator1uformantresonancespectralskirt = 153 - POS_OPERATOR_START;
    public static final int POS_operator1ufrequencymodsensefrequencyvelocitysense = 171 - POS_OPERATOR_START;
    public static final int POS_operator1uampmodsenseampvelocitysense = 172 - POS_OPERATOR_START;
        
    public int parse(byte[] data, boolean fromFile)
        {
        if (data[6] == 0x51)  // internal voice, bank is relevant
            {
            model.set("number", data[8]);
            }
        // automatically update part
        else if (data[6] >= 0x40 && data[6] <= 0x43)
            {
            setPart(data[6] - 0x40);
            }
                
        char[] name = new char[10];
        for(int i = 0; i < 10; i++)
            {
            name[i] = (char)data[i + 9];
            model.set("name", new String(name));
            }
        for(int i = 10; i < POS_operator17vswitch; i++)
            {
            if (!allParameters[i].equals("-")) model.set(allParameters[i], data[i + 9]);
            }
        
        // Handle POS_operator17vswitch
        model.set("operator1vswitch", (data[POS_operator17vswitch + 9] >>> 0) & 1);
        model.set("operator2vswitch", (data[POS_operator17vswitch + 9] >>> 1) & 1);
        model.set("operator3vswitch", (data[POS_operator17vswitch + 9] >>> 2) & 1);
        model.set("operator4vswitch", (data[POS_operator17vswitch + 9] >>> 3) & 1);
        model.set("operator5vswitch", (data[POS_operator17vswitch + 9] >>> 4) & 1);
        model.set("operator6vswitch", (data[POS_operator17vswitch + 9] >>> 5) & 1);
        model.set("operator7vswitch", (data[POS_operator17vswitch + 9] >>> 6) & 1);
        
        // There's really only one of these...
        for(int i = POS_operator17vswitch + 1; i < POS_operator17uswitch; i++)
            {
            if (!allParameters[i].equals("-")) model.set(allParameters[i], data[i + 9]);
            }

        // Handle POS_operator17uswitch
        model.set("operator1uswitch", (data[POS_operator17uswitch + 9] >>> 0) & 1);
        model.set("operator2uswitch", (data[POS_operator17uswitch + 9] >>> 1) & 1);
        model.set("operator3uswitch", (data[POS_operator17uswitch + 9] >>> 2) & 1);
        model.set("operator4uswitch", (data[POS_operator17uswitch + 9] >>> 3) & 1);
        model.set("operator5uswitch", (data[POS_operator17uswitch + 9] >>> 4) & 1);
        model.set("operator6uswitch", (data[POS_operator17uswitch + 9] >>> 5) & 1);
        model.set("operator7uswitch", (data[POS_operator17uswitch + 9] >>> 6) & 1);
        
        for(int i = POS_operator17uswitch + 1; i < POS_formantcontrol1destination; i++)
            {
            if (!allParameters[i].equals("-")) model.set(allParameters[i], data[i + 9]);
            }
                
        // Handle POS_formantcontrol1destination ... POS_formantcontrol5destination
        model.set("formantcontrol1dest", (data[POS_formantcontrol1destination + 9 + 0] >>> 4) & 3);
        model.set("formantcontrol1voiced", (data[POS_formantcontrol1destination + 9 + 0] >>> 3) & 1);
        model.set("formantcontrol1op", (data[POS_formantcontrol1destination + 9 + 0] >>> 0) & 7);
        model.set("formantcontrol2dest", (data[POS_formantcontrol1destination + 9 + 1] >>> 4) & 3);
        model.set("formantcontrol2voiced", (data[POS_formantcontrol1destination + 9 + 1] >>> 3) & 1);
        model.set("formantcontrol2op", (data[POS_formantcontrol1destination + 9 + 1] >>> 0) & 7);
        model.set("formantcontrol3dest", (data[POS_formantcontrol1destination + 9 + 2] >>> 4) & 3);
        model.set("formantcontrol3voiced", (data[POS_formantcontrol1destination + 9 + 2] >>> 3) & 1);
        model.set("formantcontrol3op", (data[POS_formantcontrol1destination + 9 + 2] >>> 0) & 7);
        model.set("formantcontrol4dest", (data[POS_formantcontrol1destination + 9 + 3] >>> 4) & 3);
        model.set("formantcontrol4voiced", (data[POS_formantcontrol1destination + 9 + 3] >>> 3) & 1);
        model.set("formantcontrol4op", (data[POS_formantcontrol1destination + 9 + 3] >>> 0) & 7);
        model.set("formantcontrol5dest", (data[POS_formantcontrol1destination + 9 + 4] >>> 4) & 3);
        model.set("formantcontrol5voiced", (data[POS_formantcontrol1destination + 9 + 4] >>> 3) & 1);
        model.set("formantcontrol5op", (data[POS_formantcontrol1destination + 9 + 4] >>> 0) & 7);
        
        for(int i = POS_formantcontrol1destination + 5; i < POS_fmcontrol1destination; i++)
            {
            if (!allParameters[i].equals("-")) model.set(allParameters[i], data[i + 9]);
            }

        // Handle POS_fmcontrol1destination ... POS_fmcontrol5destination
        model.set("fmcontrol1dest", (data[POS_fmcontrol1destination + 9 + 0] >>> 4) & 3);
        model.set("fmcontrol1voiced", (data[POS_fmcontrol1destination + 9 + 0] >>> 3) & 1);
        model.set("fmcontrol1op", (data[POS_fmcontrol1destination + 9 + 0] >>> 0) & 7);
        model.set("fmcontrol2dest", (data[POS_fmcontrol1destination + 9 + 1] >>> 4) & 3);
        model.set("fmcontrol2voiced", (data[POS_fmcontrol1destination + 9 + 1] >>> 3) & 1);
        model.set("fmcontrol2op", (data[POS_fmcontrol1destination + 9 + 1] >>> 0) & 7);
        model.set("fmcontrol3dest", (data[POS_fmcontrol1destination + 9 + 2] >>> 4) & 3);
        model.set("fmcontrol3voiced", (data[POS_fmcontrol1destination + 9 + 2] >>> 3) & 1);
        model.set("fmcontrol3op", (data[POS_fmcontrol1destination + 9 + 2] >>> 0) & 7);
        model.set("fmcontrol4dest", (data[POS_fmcontrol1destination + 9 + 3] >>> 4) & 3);
        model.set("fmcontrol4voiced", (data[POS_fmcontrol1destination + 9 + 3] >>> 3) & 1);
        model.set("fmcontrol4op", (data[POS_fmcontrol1destination + 9 + 3] >>> 0) & 7);
        model.set("fmcontrol5dest", (data[POS_fmcontrol1destination + 9 + 4] >>> 4) & 3);
        model.set("fmcontrol5voiced", (data[POS_fmcontrol1destination + 9 + 4] >>> 3) & 1);
        model.set("fmcontrol5op", (data[POS_fmcontrol1destination + 9 + 4] >>> 0) & 7);
        
        for(int i = POS_fmcontrol1destination + 5; i < POS_filteregattacktimeveltimescale; i++)
            {
            if (!allParameters[i].equals("-")) model.set(allParameters[i], data[i + 9]);
            }
                
        // handle POS_filteregattacktimeveltimescale
        model.set("filteregattacktimevel", (data[POS_filteregattacktimeveltimescale + 9] >>> 3) & 7);
        model.set("filteregtimescale", (data[POS_filteregattacktimeveltimescale + 9] >>> 0) & 7);


                
        // the next parameter is "-" so we ignore it.  Then:
        for(int op = 0; op < 8; op++)
            {
            int base = POS_OPERATOR_START + op * POS_OPERATOR_LENGTH;
                
            // handle base + POS_operator1vkeysynctranspose
            model.set("operator" + (op + 1) + "vkeysync", (data[base + POS_operator1vkeysynctranspose + 9] >>> 6) & 1);
            model.set("operator" + (op + 1) + "vtranspose", (data[base + POS_operator1vkeysynctranspose + 9] >>> 0) & 63);
                
            // handle base + POS_operator1vfrequencycoarse
            int val = data[base + POS_operator1vfrequencycoarse + 9];
            model.set("operator" + (op + 1) + "vfrequencycoarseratio", val);
            model.set("operator" + (op + 1) + "vfrequencycoarsefixed", val <= 21 ? val : 0);                // bound to 21, bleah

            val = data[base + POS_operator1vfrequencyfine + 9];
            model.set("operator" + (op + 1) + "vfrequencyfineratio", val);
            model.set("operator" + (op + 1) + "vfrequencyfinefixed", val);

            for(int i = POS_operator1vfrequencyfine + 1; i < POS_operator1vbwbiassensespectralform; i++)
                {
                if (!allParameters[base + i].equals("-")) model.set(allParameters[base + i], data[base + i + 9]);
                }
                                
            // handle base + POS_operator1vbwbiassensespectralform
            model.set("operator" + (op + 1) + "vbwbiassense", (data[base + POS_operator1vbwbiassensespectralform + 9] >>> 3) & 15);
            model.set("operator" + (op + 1) + "vspectralform", (data[base + POS_operator1vbwbiassensespectralform + 9] >>> 0) & 7);

            // handle base + POS_operator1vfrequencyoscillatormodespectralskirtfseqtracknumber
            model.set("operator" + (op + 1) + "vfrequencyoscillatormode", (data[base + POS_operator1vfrequencyoscillatormodespectralskirtfseqtracknumber + 9] >>> 6) & 1);
            model.set("operator" + (op + 1) + "vspectralskirt", (data[base + POS_operator1vfrequencyoscillatormodespectralskirtfseqtracknumber + 9] >>> 3) & 7);
            model.set("operator" + (op + 1) + "vfrequencyfseqtracknumber", (data[base + POS_operator1vfrequencyoscillatormodespectralskirtfseqtracknumber + 9] >>> 0) & 7);
                        
            for(int i = POS_operator1vfrequencyoscillatormodespectralskirtfseqtracknumber + 1; i < POS_operator1vfrequencybiassensepitchmodsense; i++)
                {
                if (!allParameters[base + i].equals("-")) model.set(allParameters[base + i], data[base + i + 9]);
                }
                                
            // handle base + POS_operator1vfrequencybiassensepitchmodsense
            model.set("operator" + (op + 1) + "vfrequencybiassense", (data[base + POS_operator1vfrequencybiassensepitchmodsense + 9] >>> 3) & 15);
            model.set("operator" + (op + 1) + "vpitchmodsense", (data[base + POS_operator1vfrequencybiassensepitchmodsense + 9] >>> 0) & 7);

            // handle base + POS_operator1vfrequencymodsensefrequencyvelocitysense
            model.set("operator" + (op + 1) + "vfrequencymodsense", (data[base + POS_operator1vfrequencymodsensefrequencyvelocitysense + 9] >>> 4) & 7);
            model.set("operator" + (op + 1) + "vfrequencyvelocitysense", (data[base + POS_operator1vfrequencymodsensefrequencyvelocitysense + 9] >>> 0) & 15);

            // handle base + POS_operator1vampmodsenseampvelocitysense
            model.set("operator" + (op + 1) + "vampmodsense", (data[base + POS_operator1vampmodsenseampvelocitysense + 9] >>> 4) & 7);
            model.set("operator" + (op + 1) + "vampvelocitysense", (data[base + POS_operator1vampmodsenseampvelocitysense + 9] >>> 0) & 15);
                        
            for(int i = POS_operator1vampmodsenseampvelocitysense + 1; i < POS_operator1uformantpitchmodecoarse; i++)
                {
                if (!allParameters[base + i].equals("-")) model.set(allParameters[base + i], data[base + i + 9]);
                }

            // handle base + POS_operator1uformantpitchmodecoarse
            model.set("operator" + (op + 1) + "uformantpitchmode", (data[base + POS_operator1uformantpitchmodecoarse + 9] >>> 5) & 3);
            model.set("operator" + (op + 1) + "ufrequencycoarse", (data[base + POS_operator1uformantpitchmodecoarse + 9] >>> 0) & 31);

            for(int i = POS_operator1uformantpitchmodecoarse + 1; i < POS_operator1uformantresonancespectralskirt; i++)
                {
                if (!allParameters[base + i].equals("-")) model.set(allParameters[base + i], data[base + i + 9]);
                }

            // handle base + POS_operator1uformantresonancespectralskirt
            model.set("operator" + (op + 1) + "uformantresonance", (data[base + POS_operator1uformantresonancespectralskirt + 9] >>> 3) & 7);
            model.set("operator" + (op + 1) + "uspectralskirt", (data[base + POS_operator1uformantresonancespectralskirt + 9] >>> 0) & 7);

            for(int i = POS_operator1uformantresonancespectralskirt + 1; i < POS_operator1ufrequencymodsensefrequencyvelocitysense; i++)
                {
                if (!allParameters[base + i].equals("-")) model.set(allParameters[base + i], data[base + i + 9]);
                }

            // handle base + POS_operator1ufrequencymodsensefrequencyvelocitysense
            model.set("operator" + (op + 1) + "ufrequencymodsense", (data[base + POS_operator1ufrequencymodsensefrequencyvelocitysense + 9] >>> 4) & 7);
            model.set("operator" + (op + 1) + "ufrequencyvelocitysense", (data[base + POS_operator1ufrequencymodsensefrequencyvelocitysense + 9] >>> 0) & 15);

            // handle base + POS_operator1uampmodsenseampvelocitysense

            model.set("operator" + (op + 1) + "uampmodsense", (data[base + POS_operator1uampmodsenseampvelocitysense + 9] >>> 4) & 7);
            model.set("operator" + (op + 1) + "uampvelocitysense", (data[base + POS_operator1uampmodsenseampvelocitysense + 9] >>> 0) & 15);
                        
            // there's only one left
            int i = POS_operator1uampmodsenseampvelocitysense + 1;
            model.set(allParameters[base + i], data[base + i + 9]);
            }
        
        revise();
        return PARSE_SUCCEEDED_UNTITLED;
        }
    
    
    
    public byte getID() 
        { 
        try 
            { 
            byte b = (byte)(Byte.parseByte(tuple.id));
            if (b >= 1 && b < 16) return b;
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
            if (b >= 1 && b < 16) return "" + b;
            } 
        catch (NumberFormatException e) { }             // expected
        return "" + getID();
        }


    byte produceChecksum(byte[] bytes, int start)
        {
        //      The TX816 owner's manual (p. 54) says the checksum is:
        //                              "the least significant 7 bits of the 2's complement sum of 155 data bytes.
        //                               0eeeeeee must be determined so that the least significant 7 bits of the
        //                              sum of the 155 data bytes and checksum equal zero."
        //
        //              The FS1R manual says "Check-sum is a value that makes "0" (zero) in lower 7 bits of an added value 
        //                                                              of Byte Count, Address, Data, and Check-sum itself"
                
        int checksum = 0;
        for(int i = start; i < bytes.length; i++)
            checksum = (checksum + bytes[i]) & 127;
        return (byte)((128 - checksum) & 127);
        }

    public void performRequestDump(Model tempModel, boolean changePatch)
        {
        if (tempModel == null)
            tempModel = getModel();

        // We ALWAYS change the patch no matter what.  We have to.  We have to force it for merging.
        changePatch(tempModel);
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
    
    // Will request the current part
    public byte[] requestCurrentDump()
        {
        return new byte[]
            {
            (byte)0xF0,
            (byte)0x43,
            (byte)(32 + getID() - 1),
            (byte)0x5E,
            (byte)(0x40 + part),
            0, 
            0, 
            (byte)0xF7
            };
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
        
    public static String getSynthName() { return "Yamaha FS1R"; }

    public void changePatch(Model tempModel) 
        {
        // We'll change the bank and program number of the given voice.
        // When we do a request, it'll load from here
        
        // Change Bank
        // Remember, that one option in the performance is "off", which is option 0, so we have to add 1
        tryToSendSysex(new byte[] { (byte)0xf0, 0x43, (byte)(16 + (getID() - 1)), 0x5e, (byte)(0x30 + part), 0x00, 0x01, 0x00, (byte)(tempModel.get("bank") + 1), (byte)0xf7});
        
        // Change Number
        tryToSendSysex(new byte[] { (byte)0xf0, 0x43, (byte)(16 + (getID() - 1)), 0x5e, (byte)(0x30 + part), 0x00, 0x02, 0x00, (byte)(tempModel.get("number")), (byte)0xf7});
        
        // we assume that we successfully did it
        if (!isMerging())  // we're actually loading the patch, not merging with it
            {
            setSendMIDI(false);
            model.set("number", tempModel.get("number"));
            model.set("bank", tempModel.get("bank"));
            setSendMIDI(true);
            }
        }
    
    public String getPatchName(Model model) { return model.get("name", "INIT VOICE"); }

    public Model getNextPatchLocation(Model model)
        {
        int bank = model.get("bank");
        int number = model.get("number");
        
        number++;
        if (number >= 128)
            {
            bank++;
            number = 0;
            if (bank >= 13)             // K = 12, Internal = 0
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
        int bank = model.get("bank");
        return (bank == 0 ? "Int " : BANKS[model.get("bank")]) + 
            (number > 99 ? "" : (number > 9 ? "0" : "00")) + number;
        }
        

    // Taken with permission from FS1REditor by K_Take (https://synth-voice.sakura.ne.jp/fs1r_editor_english.html
    public static final String[][] FREQUENCIES = new String[][] {
        { "0.013", "0.026", "0.053", "0.107", "0.215", "0.429", "0.859", "1.719", "3.439", "6.879", "13.75", "27.51", "55.03", "110.0", "220.1", "440.2", "880.5", "1761", "3522", "7044", "14088" },
        { "0.013", "0.027", "0.054", "0.108", "0.216", "0.432", "0.864", "1.729", "3.458", "6.916", "13.83", "27.66", "50.33", "110.6", "221.3", "442.6", "885.2", "1770", "3541", "7082", "14165" },
        { "0.013", "0.027", "0.054", "0.108", "0.217", "0.434", "0.869", "1.738", "3.476", "6.953", "13.90", "27.81", "55.63", "111.2", "222.5", "445.0", "890.1", "1780", "3560", "7120", "14242" },
        { "0.013", "0.027", "0.054", "0.109", "0.218", "0.437", "0.874", "1.747", "3.495", "6.991", "13.98", "27.96", "55.93", "111.8", "223.7", "447.4", "894.9", "1789", "3579", "7159", "14319" },
        { "0.013", "0.027", "0.054", "0.109", "0.219", "0.439", "0.878", "1.757", "3.514", "7.029", "14.05", "28.11", "56.23", "112.4", "224.9", "449.8", "899.7", "1799", "3599", "7198", "14397" },
        { "0.013", "0.027", "0.055", "0.110", "0.220", "0.441", "0.883", "1.766", "3.533", "7.067", "14.13", "28.27", "56.54", "113.0", "226.1", "452.3", "904.6", "1809", "3618", "7237", "14475" },
        { "0.013", "0.027", "0.055", "0.111", "0.222", "0.444", "0.888", "1.776", "3.553", "7.106", "14.21", "28.42", "56.84", "113.7", "227.4", "454.7", "909.5", "1819", "3638", "7276", "14553" },
        { "0.014", "0.027", "0.055", "0.111", "0.223", "0.446", "0.893", "1.786", "3.572", "7.144", "14.28", "28.57", "57.15", "114.3", "228.6", "457.2", "914.5", "1829", "3658", "7316", "14632" },
        { "0.014", "0.028", "0.056", "0.112", "0.224", "0.449", "0.897", "1.795", "3.591", "7.183", "14.36", "28.73", "57.46", "114.9", "229.8", "459.7", "919.4", "1839", "3678", "7355", "14712" },
        { "0.014", "0.028", "0.056", "0.112", "0.225", "0.451", "0.902", "1.805", "3.611", "7.222", "14.44", "28.89", "57.78", "115.5", "231.1", "462.2", "924.4", "1849", "3697", "7395", "14792" },
        { "0.014", "0.028", "0.056", "0.113", "0.226", "0.453", "0.907", "1.815", "3.630", "7.261", "14.52", "29.04", "58.09", "116.1", "232.3", "464.7", "929.5", "1859", "3718", "7436", "14872" },
        { "0.014", "0.028", "0.057", "0.114", "0.228", "0.456", "0.912", "1.825", "3.650", "7.301", "14.60", "29.20", "58.41", "116.8", "233.6", "467.2", "934.5", "1869", "3738", "7476", "14953" },
        { "0.014", "0.028", "0.057", "0.114", "0.229", "0.458", "0.917", "1.835", "3.670", "7.340", "14.68", "29.36", "58.72", "117.4", "234.9", "469.8", "939.6", "1879", "3758", "7517", "15034" },
        { "0.014", "0.028", "0.057", "0.115", "0.230", "0.461", "0.922", "1.845", "3.690", "7.380", "14.76", "29.52", "59.04", "118.0", "236.1", "472.3", "944.7", "1889", "3778", "7557", "15116" },
        { "0.014", "0.029", "0.058", "0.115", "0.231", "0.463", "0.927", "1.855", "3.710", "7.420", "14.84", "29.68", "59.36", "118.7", "237.4", "474.9", "949.8", "1899", "3799", "7598", "15198" },
        { "0.014", "0.029", "0.058", "0.116", "0.233", "0.466", "0.932", "1.865", "3.730", "7.461", "14.92", "29.84", "59.68", "119.3", "238.7", "477.5", "955.0", "1910", "3820", "7640", "15280" },
        { "0.014", "0.029", "0.058", "0.117", "0.234", "0.468", "0.937", "1.875", "3.750", "7.501", "15.00", "30.00", "60.01", "120.0", "240.0", "480.1", "960.2", "1920", "3840", "7681", "15363" },
        { "0.014", "0.029", "0.058", "0.117", "0.235", "0.471", "0.942", "1.885", "3.771", "7.542", "15.08", "30.16", "60.33", "120.6", "241.3", "482.7", "965.4", "1930", "3861", "7723", "15447" },
        { "0.014", "0.029", "0.059", "0.118", "0.237", "0.474", "0.947", "1.895", "3.791", "7.583", "15.16", "30.33", "60.66", "121.3", "242.6", "485.3", "970.6", "1941", "3882", "7765", "15531" },
        { "0.014", "0.029", "0.059", "0.119", "0.238", "0.476", "0.953", "1.906", "3.812", "7.624", "15.24", "30.49", "60.99", "121.9", "243.9", "487.9", "975.9", "1951", "3903", "7807", "15615" },
        { "0.015", "0.029", "0.059", "0.119", "0.239", "0.479", "0.958", "1.916", "3.832", "7.665", "15.33", "30.66", "61.32", "122.6", "245.3", "490.6", "981.2", "1962", "3924", "7849", "15700" },
        { "0.015", "0.030", "0.060", "0.120", "0.240", "0.481", "0.963", "1.926", "3.853", "7.707", "15.41", "30.83", "61.66", "123.3", "246.6", "493.2", "986.5", "1973", "3946", "7892", "15785" },
        { "0.015", "0.030", "0.060", "0.121", "0.242", "0.484", "0.968", "1.937", "3.874", "7.749", "15.49", "30.99", "61.99", "123.9", "247.9", "495.9", "991.9", "1983", "3967", "7935", "15871" },
        { "0.015", "0.030", "0.060", "0.121", "0.243", "0.487", "0.973", "1.947", "3.895", "7.791", "15.58", "31.16", "62.33", "124.6", "249.3", "498.6", "997.3", "1994", "3989", "7978", "15957" },
        { "0.015", "0.030", "0.061", "0.122", "0.244", "0.489", "0.979", "1.958", "3.916", "7.833", "15.66", "31.33", "62.67", "125.3", "250.6", "501.3", "1002", "2005", "4010", "8021", "16043" },
        { "0.015", "0.030", "0.061", "0.123", "0.246", "0.492", "0.984", "1.969", "3.938", "7.876", "15.75", "31.50", "63.01", "126.0", "252.0", "504.0", "1008", "2016", "4032", "8065", "16131" },
        { "0.015", "0.030", "0.061", "0.123", "0.247", "0.494", "0.989", "1.979", "3.959", "7.919", "15.83", "31.67", "63.35", "126.7", "253.4", "506.8", "1013", "2027", "4054", "8109", "16218" },
        { "0.015", "0.031", "0.062", "0.124", "0.248", "0.497", "0.995", "1.990", "3.981", "7.962", "15.92", "31.84", "63.69", "127.3", "254.7", "509.5", "1019", "2038", "4076", "8153", "16306" },
        { "0.015", "0.031", "0.062", "0.125", "0.250", "0.500", "1.000", "2.001", "4.002", "8.005", "16.01", "32.02", "64.04", "128.0", "256.1", "512.3", "1024", "2049", "4098", "8197", "16395" },
        { "0.015", "0.031", "0.062", "0.125", "0.251", "0.503", "1.006", "2.012", "4.024", "8.048", "16.09", "32.19", "64.39", "128.7", "257.5", "515.1", "1030", "2060", "4120", "8241", "16484" },
        { "0.015", "0.031", "0.063", "0.126", "0.252", "0.505", "1.011", "2.023", "4.046", "8.092", "16.18", "32.37", "64.73", "129.4", "258.9", "517.9", "1035", "2071", "4143", "8286", "16573" },
        { "0.015", "0.031", "0.063", "0.127", "0.254", "0.508", "1.017", "2.034", "4.068", "8.136", "16.27", "32.54", "65.09", "130.1", "260.3", "520.7", "1041", "2082", "4165", "8331", "16663" },
        { "0.016", "0.032", "0.063", "0.127", "0.255", "0.511", "1.022", "2.045", "4.090", "8.180", "16.36", "32.72", "65.44", "130.8", "261.7", "523.5", "1047", "2094", "4188", "8376", "16754" },
        { "0.016", "0.032", "0.064", "0.128", "0.257", "0.514", "1.028", "2.056", "4.112", "8.224", "16.45", "32.90", "65.80", "131.6", "263.2", "526.4", "1052", "2105", "4211", "8422", "16845" },
        { "0.016", "0.032", "0.064", "0.129", "0.258", "0.516", "1.033", "2.067", "4.134", "8.269", "16.53", "33.07", "66.15", "132.3", "264.6", "529.2", "1058", "2117", "4234", "8468", "16936" },
        { "0.016", "0.032", "0.065", "0.129", "0.259", "0.519", "1.039", "2.078", "4.157", "8.314", "16.62", "33.25", "66.51", "133.0", "266.0", "532.1", "1064", "2128", "4257", "8514", "17028" },
        { "0.016", "0.032", "0.065", "0.130", "0.261", "0.522", "1.045", "2.089", "4.179", "8.359", "16.71", "33.43", "66.87", "133.7", "267.5", "535.0", "1070", "2140", "4280", "8560", "17121" },
        { "0.016", "0.032", "0.065", "0.131", "0.262", "0.525", "1.050", "2.101", "4.202", "8.405", "16.81", "33.62", "67.24", "134.4", "268.9", "537.9", "1075", "2151", "4303", "8606", "17214" },
        { "0.016", "0.033", "0.066", "0.132", "0.264", "0.528", "1.056", "2.112", "4.225", "8.450", "16.90", "33.80", "67.60", "135.2", "270.4", "540.8", "1081", "2163", "4326", "8653", "17307" },
        { "0.016", "0.033", "0.066", "0.132", "0.265", "0.531", "1.062", "2.124", "4.248", "8.496", "16.99", "33.98", "67.97", "135.9", "271.8", "543.7", "1087", "2175", "4350", "8700", "17401" },
        { "0.016", "0.033", "0.066", "0.133", "0.267", "0.533", "1.067", "2.135", "4.271", "8.542", "17.08", "34.17", "68.34", "136.6", "273.3", "546.7", "1093", "2186", "4373", "8747", "17495" },
        { "0.016", "0.033", "0.067", "0.134", "0.268", "0.536", "1.073", "2.147", "4.294", "8.589", "17.17", "34.35", "68.71", "137.4", "274.8", "549.7", "1099", "2198", "4397", "8795", "17590" },
        { "0.016", "0.033", "0.067", "0.134", "0.269", "0.539", "1.079", "2.158", "4.317", "8.635", "17.27", "34.54", "69.08", "138.1", "276.3", "552.6", "1105", "2210", "4421", "8843", "17686" },
        { "0.017", "0.033", "0.067", "0.135", "0.271", "0.542", "1.085", "2.170", "4.341", "8.682", "17.36", "34.73", "69.46", "138.9", "277.8", "555.6", "1111", "2222", "4445", "8891", "17782" },
        { "0.017", "0.034", "0.068", "0.136", "0.272", "0.545", "1.091", "2.182", "4.364", "8.729", "17.46", "34.91", "69.83", "139.6", "279.3", "558.7", "1117", "2234", "4469", "8939", "17879" },
        { "0.017", "0.034", "0.068", "0.137", "0.274", "0.548", "1.097", "2.194", "4.388", "8.777", "17.55", "35.10", "70.21", "140.4", "280.8", "561.7", "1123", "2247", "4493", "8987", "17976" },
        { "0.017", "0.034", "0.068", "0.137", "0.275", "0.551", "1.103", "2.206", "4.412", "8.824", "17.65", "35.29", "70.59", "141.2", "282.3", "564.7", "1129", "2259", "4518", "9036", "18073" },
        { "0.017", "0.034", "0.069", "0.138", "0.277", "0.554", "1.109", "2.218", "4.436", "8.872", "17.74", "35.49", "70.98", "141.9", "283.9", "567.8", "1135", "2271", "4542", "9085", "18171" },
        { "0.017", "0.034", "0.069", "0.139", "0.278", "0.557", "1.115", "2.230", "4.460", "8.920", "17.84", "35.68", "71.36", "142.7", "285.4", "570.9", "1141", "2283", "4567", "9135", "18270" },
        { "0.017", "0.035", "0.070", "0.140", "0.280", "0.560", "1.121", "2.242", "4.484", "8.969", "17.93", "35.87", "71.75", "143.5", "287.0", "574.0", "1148", "2296", "4592", "9184", "18369" },
        { "0.017", "0.035", "0.070", "0.140", "0.281", "0.563", "1.127", "2.254", "4.509", "9.018", "18.03", "36.07", "72.14", "144.2", "288.5", "577.1", "1154", "2308", "4617", "9234", "18469" },
        { "0.017", "0.035", "0.070", "0.141", "0.283", "0.566", "1.133", "2.266", "4.533", "9.067", "18.13", "36.26", "72.53", "145.0", "290.1", "580.2", "1160", "2321", "4642", "9284", "18569" },
        { "0.017", "0.035", "0.071", "0.142", "0.284", "0.569", "1.139", "2.279", "4.558", "9.116", "18.23", "36.46", "72.93", "145.8", "291.7", "583.4", "1166", "2333", "4667", "9335", "18670" },
        { "0.017", "0.035", "0.071", "0.143", "0.286", "0.572", "1.145", "2.291", "4.582", "9.165", "18.33", "36.66", "73.32", "146.6", "293.3", "586.6", "1173", "2346", "4692", "9385", "18772" },
        { "0.018", "0.036", "0.072", "0.144", "0.288", "0.576", "1.151", "2.303", "4.607", "9.215", "18.43", "36.86", "73.72", "147.4", "294.9", "589.7", "1179", "2359", "4718", "9436", "18873" },
        { "0.018", "0.036", "0.072", "0.144", "0.289", "0.579", "1.158", "2.316", "4.632", "9.265", "18.53", "37.06", "74.12", "148.2", "296.5", "593.0", "1186", "2372", "4744", "9488", "18976" },
        { "0.018", "0.036", "0.072", "0.145", "0.291", "0.582", "1.164", "2.329", "4.657", "9.315", "18.63", "37.26", "74.52", "149.0", "298.1", "596.2", "1192", "2384", "4769", "9539", "19079" },
        { "0.018", "0.036", "0.073", "0.146", "0.292", "0.585", "1.170", "2.341", "4.683", "9.366", "18.73", "37.46", "74.93", "149.8", "299.7", "599.4", "1198", "2397", "4795", "9591", "19183" },
        { "0.018", "0.036", "0.073", "0.147", "0.294", "0.588", "1.177", "2.354", "4.708", "9.417", "18.83", "37.66", "75.33", "150.6", "301.3", "602.7", "1205", "2410", "4821", "9643", "19287" },
        { "0.018", "0.037", "0.074", "0.147", "0.295", "0.591", "1.183", "2.367", "4.734", "9.468", "18.93", "37.87", "75.74", "151.5", "302.9", "605.9", "1212", "2423", "4847", "9695", "19391" },
        { "0.018", "0.037", "0.074", "0.148", "0.297", "0.595", "1.190", "2.380", "4.759", "9.519", "19.04", "38.08", "76.15", "152.3", "304.6", "609.2", "1218", "2437", "4874", "9748", "19497" },
        { "0.018", "0.037", "0.074", "0.149", "0.299", "0.598", "1.196", "2.392", "4.785", "9.571", "19.14", "38.28", "76.57", "153.1", "306.2", "612.5", "1225", "2450", "4900", "9801", "19603" },
        { "0.018", "0.037", "0.075", "0.150", "0.300", "0.601", "1.202", "2.405", "4.811", "9.623", "19.24", "38.49", "76.98", "153.9", "307.9", "615.9", "1231", "2463", "4927", "9854", "19709" },
        { "0.018", "0.037", "0.075", "0.151", "0.302", "0.604", "1.209", "2.419", "4.837", "9.675", "19.35", "38.70", "77.40", "154.8", "309.6", "619.2", "1238", "2477", "4954", "9908", "19816" },
        { "0.019", "0.038", "0.076", "0.152", "0.304", "0.608", "1.216", "2.432", "4.864", "9.728", "19.45", "38.91", "77.82", "155.6", "311.3", "622.6", "1245", "2490", "4980", "9961", "19924" },
        { "0.019", "0.038", "0.076", "0.152", "0.305", "0.611", "1.222", "2.445", "4.890", "9.781", "19.56", "39.12", "78.24", "156.5", "313.0", "625.9", "1252", "2504", "5008", "10016", "20032" },
        { "0.019", "0.038", "0.076", "0.153", "0.307", "0.614", "1.229", "2.458", "4.917", "9.834", "19.66", "39.33", "78.67", "157.3", "314.7", "629.3", "1258", "2517", "5035", "10070", "20141" },
        { "0.019", "0.038", "0.077", "0.154", "0.309", "0.618", "1.236", "2.471", "4.943", "9.887", "19.77", "39.55", "79.10", "158.2", "316.4", "632.8", "1265", "2531", "5062", "10125", "20250" },
        { "0.019", "0.038", "0.077", "0.155", "0.310", "0.621", "1.242", "2.485", "4.970", "9.941", "19.88", "39.76", "79.53", "159.0", "318.1", "636.2", "1272", "2545", "5090", "10180", "20360" },
        { "0.019", "0.039", "0.078", "0.156", "0.312", "0.624", "1.249", "2.498", "4.997", "9.995", "19.99", "39.98", "79.96", "159.9", "319.8", "639.7", "1279", "2558", "5117", "10235", "20470" },
        { "0.019", "0.039", "0.078", "0.157", "0.314", "0.628", "1.256", "2.512", "5.024", "10.05", "20.09", "40.19", "80.39", "160.7", "321.5", "643.1", "1286", "2572", "5145", "10291", "20582" },
        { "0.019", "0.039", "0.078", "0.157", "0.315", "0.631", "1.263", "2.526", "5.052", "10.10", "20.20", "40.41", "80.83", "161.6", "323.3", "646.6", "1293", "2586", "5173", "10347", "20693" },
        { "0.019", "0.039", "0.079", "0.158", "0.317", "0.634", "1.269", "2.539", "5.079", "10.15", "20.31", "40.63", "81.27", "162.5", "325.0", "650.1", "1300", "2600", "5201", "10403", "20806" },
        { "0.019", "0.039", "0.079", "0.159", "0.319", "0.638", "1.276", "2.553", "5.107", "10.21", "20.42", "40.85", "81.71", "163.4", "326.8", "653.7", "1307", "2614", "5229", "10459", "20919" },
        { "0.020", "0.040", "0.080", "0.160", "0.320", "0.641", "1.283", "2.567", "5.134", "10.27", "20.53", "41.07", "82.15", "164.3", "328.6", "657.2", "1314", "2629", "5258", "10516", "21032" },
        { "0.020", "0.040", "0.080", "0.161", "0.322", "0.645", "1.290", "2.581", "5.162", "10.32", "20.65", "41.30", "82.60", "165.2", "330.4", "660.8", "1321", "2643", "5286", "10573", "21147" },
        { "0.020", "0.040", "0.081", "0.162", "0.324", "0.648", "1.297", "2.595", "5.190", "10.38", "20.76", "41.52", "83.05", "166.1", "332.2", "664.4", "1328", "2657", "5315", "10631", "21261" },
        { "0.020", "0.040", "0.081", "0.163", "0.326", "0.652", "1.304", "2.609", "5.218", "10.43", "20.87", "41.75", "83.50", "167.0", "334.0", "668.0", "1336", "2672", "5344", "10688", "21377" },
        { "0.020", "0.041", "0.082", "0.164", "0.328", "0.655", "1.311", "2.623", "5.247", "10.49", "20.98", "41.97", "83.95", "167.9", "335.8", "671.6", "1343", "2686", "5373", "10746", "21493" },
        { "0.020", "0.041", "0.082", "0.164", "0.329", "0.659", "1.318", "2.637", "5.275", "10.55", "21.10", "42.20", "84.41", "168.8", "337.6", "675.3", "1350", "2701", "5402", "10805", "21610" },
        { "0.020", "0.041", "0.082", "0.165", "0.331", "0.663", "1.326", "2.652", "5.304", "10.60", "21.21", "42.43", "84.87", "169.7", "339.4", "678.9", "1357", "2715", "5431", "10863", "21727" },
        { "0.020", "0.041", "0.083", "0.166", "0.333", "0.666", "1.333", "2.666", "5.333", "10.66", "21.33", "42.66", "85.33", "170.6", "341.3", "682.6", "1365", "2730", "5461", "10922", "21845" },
        { "0.020", "0.041", "0.083", "0.167", "0.335", "0.670", "1.340", "2.681", "5.362", "10.72", "21.44", "42.89", "85.79", "171.5", "343.1", "686.3", "1372", "2745", "5490", "10982", "21963" },
        { "0.021", "0.042", "0.084", "0.168", "0.337", "0.673", "1.347", "2.695", "5.391", "10.78", "21.56", "43.13", "86.26", "172.5", "345.0", "690.0", "1380", "2760", "5520", "11041", "22083" },
        { "0.021", "0.042", "0.084", "0.169", "0.338", "0.677", "1.355", "2.710", "5.420", "10.84", "21.68", "43.36", "86.72", "173.4", "346.9", "693.8", "1387", "2775", "5550", "11101", "22203" },
        { "0.021", "0.042", "0.085", "0.170", "0.340", "0.681", "1.362", "2.725", "5.450", "10.90", "21.80", "43.60", "87.20", "174.4", "348.8", "697.6", "1395", "2790", "5580", "11162", "22323" },
        { "0.021", "0.042", "0.085", "0.171", "0.342", "0.684", "1.369", "2.739", "5.479", "10.95", "21.91", "43.83", "87.67", "175.3", "350.6", "701.3", "1402", "2805", "5611", "11222", "22444" },
        { "0.021", "0.043", "0.086", "0.172", "0.344", "0.688", "1.377", "2.754", "5.509", "11.01", "22.03", "44.07", "88.15", "176.3", "352.6", "705.2", "1410", "2820", "5641", "11283", "22566" },
        { "0.021", "0.043", "0.086", "0.173", "0.346", "0.692", "1.384", "2.769", "5.539", "11.07", "22.15", "44.31", "88.62", "177.2", "354.5", "709.0", "1418", "2836", "5672", "11344", "22689" },
        { "0.021", "0.043", "0.087", "0.174", "0.348", "0.696", "1.392", "2.784", "5.569", "11.13", "22.27", "44.55", "89.10", "178.2", "356.4", "712.8", "1425", "2851", "5703", "11406", "22812" },
        { "0.021", "0.043", "0.087", "0.175", "0.350", "0.699", "1.399", "2.799", "5.599", "11.19", "22.39", "44.79", "89.59", "179.1", "358.3", "716.7", "1433", "2867", "5734", "11468", "22936" },
        { "0.022", "0.044", "0.088", "0.175", "0.351", "0.703", "1.407", "2.815", "5.630", "11.26", "22.52", "45.04", "90.08", "180.1", "360.3", "720.6", "1441", "2882", "5765", "11530", "23060" },
        { "0.022", "0.044", "0.088", "0.176", "0.353", "0.707", "1.415", "2.830", "5.660", "11.32", "22.64", "45.28", "90.56", "181.1", "362.2", "724.5", "1449", "2898", "5796", "11593", "23186" },
        { "0.022", "0.044", "0.088", "0.177", "0.355", "0.711", "1.422", "2.845", "5.691", "11.38", "22.76", "45.53", "91.06", "182.1", "364.2", "728.4", "1457", "2913", "5827", "11656", "23312" },
        { "0.022", "0.044", "0.089", "0.178", "0.357", "0.715", "1.430", "2.861", "5.722", "11.44", "22.88", "45.77", "91.55", "183.1", "366.2", "732.4", "1464", "2929", "5859", "11719", "23438" },
        { "0.022", "0.044", "0.089", "0.179", "0.359", "0.719", "1.438", "2.876", "5.753", "11.50", "23.01", "46.02", "92.05", "184.1", "368.2", "736.4", "1472", "2945", "5891", "11783", "23565" },
        { "0.022", "0.045", "0.090", "0.180", "0.361", "0.723", "1.446", "2.892", "5.784", "11.56", "23.13", "46.27", "92.55", "185.1", "370.2", "740.4", "1480", "2961", "5923", "11847", "23693" },
        { "0.022", "0.045", "0.090", "0.181", "0.363", "0.727", "1.454", "2.908", "5.815", "11.63", "23.26", "46.52", "93.05", "186.1", "372.2", "744.4", "1488", "2977", "5955", "11911", "23822" },
        { "0.022", "0.045", "0.091", "0.182", "0.365", "0.730", "1.461", "2.923", "5.847", "11.69", "23.39", "46.78", "93.56", "187.1", "374.2", "748.4", "1497", "2993", "5987", "11976", "23951" },
        { "0.023", "0.045", "0.091", "0.183", "0.367", "0.734", "1.469", "2.939", "5.879", "11.75", "23.51", "47.03", "94.06", "188.1", "376.2", "752.5", "1505", "3010", "6020", "12041", "24081" },
        { "0.023", "0.046", "0.092", "0.184", "0.369", "0.738", "1.477", "2.955", "5.911", "11.82", "23.64", "47.28", "94.57", "189.1", "378.3", "756.6", "1513", "3026", "6053", "12106", "24212" },
        { "0.023", "0.046", "0.092", "0.185", "0.371", "0.742", "1.485", "2.971", "5.943", "11.88", "23.77", "47.54", "95.09", "190.1", "380.3", "760.7", "1521", "3043", "6085", "12172", "24344" },
        { "0.023", "0.046", "0.093", "0.186", "0.373", "0.746", "1.493", "2.987", "5.975", "11.95", "23.90", "47.80", "95.60", "191.2", "382.4", "764.8", "1529", "3059", "6119", "12238", "24476" },
        { "0.023", "0.046", "0.093", "0.187", "0.375", "0.751", "1.502", "3.004", "6.008", "12.01", "24.03", "48.06", "96.12", "192.2", "384.5", "769.0", "1538", "3076", "6152", "12304", "24609" },
        { "0.023", "0.047", "0.094", "0.188", "0.377", "0.755", "1.510", "3.020", "6.040", "12.08", "24.16", "48.32", "96.65", "193.3", "386.6", "773.2", "1546", "3092", "6185", "12371", "24742" },
        { "0.023", "0.047", "0.094", "0.189", "0.379", "0.759", "1.518", "3.036", "6.073", "12.14", "24.29", "48.58", "97.17", "194.3", "388.7", "777.4", "1554", "3109", "6219", "12438", "24877" },
        { "0.023", "0.047", "0.095", "0.190", "0.381", "0.763", "1.526", "3.053", "6.106", "12.21", "24.42", "48.85", "97.70", "195.4", "390.8", "781.6", "1563", "3126", "6252", "12506", "25012" },
        { "0.024", "0.048", "0.095", "0.191", "0.383", "0.767", "1.534", "3.069", "6.139", "12.27", "24.55", "49.11", "98.23", "196.4", "392.9", "785.8", "1571", "3143", "6286", "12574", "25148" },
        { "0.024", "0.048", "0.096", "0.192", "0.385", "0.771", "1.543", "3.086", "6.172", "12.34", "24.69", "49.38", "98.76", "197.5", "395.0", "790.1", "1580", "3160", "6321", "12642", "25284" },
        { "0.024", "0.048", "0.097", "0.193", "0.387", "0.775", "1.551", "3.103", "6.206", "12.41", "24.82", "49.65", "99.30", "198.6", "397.2", "794.4", "1588", "3177", "6355", "12711", "25421" },
        { "0.024", "0.048", "0.097", "0.195", "0.390", "0.780", "1.560", "3.120", "6.240", "12.48", "24.96", "49.92", "99.84", "199.6", "399.3", "798.7", "1597", "3194", "6389", "12780", "25559" },
        { "0.024", "0.049", "0.098", "0.196", "0.392", "0.784", "1.568", "3.137", "6.274", "12.54", "25.09", "50.19", "100.3", "200.7", "401.5", "803.0", "1606", "3212", "6424", "12849", "25698" },
        { "0.024", "0.049", "0.098", "0.197", "0.394", "0.788", "1.577", "3.154", "6.308", "12.61", "25.23", "50.46", "100.9", "201.8", "403.7", "807.4", "1614", "3229", "6459", "12919", "25838" },
        { "0.024", "0.049", "0.099", "0.198", "0.396", "0.792", "1.585", "3.171", "6.342", "12.68", "25.36", "50.73", "101.4", "202.9", "405.9", "811.8", "1623", "3247", "6494", "12989", "25978" },
        { "0.024", "0.049", "0.099", "0.199", "0.398", "0.797", "1.594", "3.188", "6.376", "12.75", "25.50", "51.01", "102.0", "204.0", "408.1", "816.2", "1632", "3264", "6529", "13060", "26119" },
        { "0.025", "0.050", "0.100", "0.200", "0.400", "0.801", "1.602", "3.205", "6.411", "12.82", "25.64", "51.29", "102.5", "205.1", "410.3", "820.6", "1641", "3282", "6565", "13130", "26261" },
        { "0.025", "0.050", "0.100", "0.201", "0.402", "0.805", "1.611", "3.223", "6.446", "12.89", "25.78", "51.56", "103.1", "206.2", "412.5", "825.1", "1650", "3300", "6600", "13202", "26404" },
        { "0.025", "0.050", "0.101", "0.202", "0.405", "0.810", "1.620", "3.240", "6.481", "12.96", "25.92", "51.84", "103.7", "207.4", "414.8", "829.5", "1659", "3318", "6636", "13273", "26547" },
        { "0.025", "0.050", "0.101", "0.203", "0.407", "0.814", "1.629", "3.258", "6.516", "13.03", "26.06", "52.13", "104.2", "208.5", "417.0", "834.1", "1668", "3336", "6672", "13346", "26691" },
        { "0.025", "0.051", "0.102", "0.204", "0.409", "0.819", "1.637", "3.275", "6.551", "13.10", "26.20", "52.41", "104.8", "209.6", "419.3", "838.6", "1677", "3354", "6709", "13418", "26836" },
        { "0.025", "0.051", "0.102", "0.205", "0.411", "0.823", "1.646", "3.293", "6.587", "13.17", "26.34", "52.69", "105.4", "210.7", "421.5", "843.1", "1686", "3372", "6745", "13491", "26982" },
        { "0.025", "0.051", "0.103", "0.207", "0.413", "0.827", "1.655", "3.311", "6.623", "13.24", "26.49", "52.98", "105.9", "211.9", "423.8", "847.7", "1695", "3391", "6782", "13564", "27128" },
        { "0.026", "0.052", "0.104", "0.208", "0.416", "0.832", "1.664", "3.329", "6.659", "13.31", "26.63", "53.27", "106.5", "213.0", "426.1", "852.3", "1704", "3409", "6818", "13638", "27276" },
        { "0.026", "0.052", "0.104", "0.209", "0.418", "0.836", "1.673", "3.347", "6.695", "13.39", "26.78", "53.56", "107.1", "214.2", "428.4", "856.9", "1714", "3428", "6855", "13712", "27424" },
        { "0.026", "0.052", "0.105", "0.210", "0.420", "0.841", "1.682", "3.365", "6.731", "13.46", "26.92", "53.85", "107.7", "215.4", "430.8", "861.6", "1723", "3446", "6893", "13786", "27573" },
        { "0.026", "0.052", "0.105", "0.211", "0.423", "0.846", "1.692", "3.384", "6.768", "13.53", "27.07", "54.14", "108.2", "216.5", "433.1", "866.3", "1732", "3465", "6930", "13861", "27722" },
        { "0.026", "0.053", "0.106", "0.212", "0.425", "0.850", "1.701", "3.402", "6.804", "13.61", "27.22", "54.43", "108.8", "217.7", "435.5", "871.0", "1742", "3484", "6968", "13936", "27873" },
        { "0.026", "0.053", "0.106", "0.213", "0.427", "0.855", "1.710", "3.420", "6.841", "13.68", "27.36", "54.73", "109.4", "218.9", "437.8", "875.7", "1751", "3503", "7006", "14012", "28024" },
        };

    public boolean testVerify(Synth synth2, 
        String key,
        Object obj1, Object obj2) 
        {
        // These overlap, so if finefixed is the right thing, then fineratio will be wrong and vice versa.
        // Same for coarse
        return (key.endsWith("finefixed") ||
            key.endsWith("fineratio") ||
            key.endsWith("coarsefixed") ||
            key.endsWith("coarseratio"));
        }

    // Writing takes a while to process.  However the FS1R has a huge buffer and can handle an entire
    // bank's worth of writes -- but then it's very slow to process through all of them, constantly displaying
    // "Bulk Received".  With about a 170ms delay or so, this message disappears right when Edisyn finishes,
    // so it's a good compromise from a UI standpoint.
    public int getPauseAfterWritePatch() { return 170; }            // don't know if we need any

    // The FS1R is VERY slow to respond and also queues up responses (beware!)
    public int getBatchDownloadWaitTime() { return 2750; }
    public int getBatchDownloadFailureCountdown() { return 5; }

    public String[] getBankNames() { return BANKS; }
    public String[] getPatchNumberNames()  { return buildIntegerNames(128, 1); }
    public boolean[] getWriteableBanks() { return new boolean[] { true, false, false, false, false, false, false, false, false, false, false, false }; }
    public boolean getSupportsPatchWrites() { return true; }
    public int getPatchNameLength() { return 10; }
    public boolean librarianTested() { return true; }
    }
