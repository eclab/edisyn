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
   A patch editor for Yamaha FS1R Fseqs.
   
   @author Sean Luke
*/

public class YamahaFS1RFseq extends Synth
    {
    public static final double MAXFREQ = 23597.0;

    public static final String[] BANKS = new String[] { "Internal", "Preset" };
    public static final String[] LOOP_MODES = new String[] { "One Way", "Round" };
    public static final String[] PITCH_MODES = new String[] { "Pitch", "Non-Pitch" };
    public static final String[] FORMATS = new String[] { "128 Frames", "256 Frames", "384 Frames", "512 Frames" };
    public static final String[] NOTES = {"A", "A#", "B", "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#" };
    public static final String[] TRACKS = {"1", "2", "3", "4", "5", "6", "7", "8"};
    public static final String[] TRACKS_PLUS = {"1", "2", "3", "4", "5", "6", "7", "8", "All"};

    Box hi = null;
    Box lo = null;
    Box mid = null;
    
    static Model pasteboard = null;
    int pasteboardLo = -1;
    int pasteboardHi = -1;
        

    public YamahaFS1RFseq()
        {
        JComponent soundPanel = new SynthPanel(this);
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        hbox.addLast(addGlobal(Style.COLOR_A()));
        vbox.add(hbox);

        vbox.add(Strut.makeVerticalStrut(10));
        vbox.add(new Category(this, null, Style.COLOR_A()));
        vbox.add(Strut.makeVerticalStrut(10));
        vbox.addLast(addFrames(Style.COLOR_B(), Style.COLOR_A(), Style.COLOR_B()));
        
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Common", soundPanel);

        soundPanel = new SynthPanel(this);
        soundPanel.add(addFrameControls(1), BorderLayout.NORTH);
        JPanel panel = new JPanel();
        panel.setBackground(Style.BACKGROUND_COLOR());
        panel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.fill = c.BOTH;
        c.weightx = 1;
        c.weighty = 3;
        lo = new Box(BoxLayout.Y_AXIS);
        lo.add(addFrameDisplay(1, true, Style.COLOR_A()));
        lo.add(addFrameDisplay(1, false, Style.COLOR_B()));
        panel.add(lo, c);
        c.weighty = 1;
        c.gridy = 1;
        panel.add(addPitchDisplay(1, Style.COLOR_C()), c);
        soundPanel.add(panel, BorderLayout.CENTER);
        addTab("1-256", soundPanel);

        soundPanel = new SynthPanel(this);
        soundPanel.add(addFrameControls(129), BorderLayout.NORTH);
        panel = new JPanel();
        panel.setBackground(Style.BACKGROUND_COLOR());
        panel.setLayout(new GridBagLayout());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.fill = c.BOTH;
        c.weightx = 1;
        c.weighty = 3;
        mid = new Box(BoxLayout.Y_AXIS);
        mid.add(addFrameDisplay(129, true, Style.COLOR_B()));
        mid.add(addFrameDisplay(129, false, Style.COLOR_B()));
        panel.add(mid, c);
        c.weighty = 1;
        c.gridy = 1;
        panel.add(addPitchDisplay(129, Style.COLOR_C()), c);
        soundPanel.add(panel, BorderLayout.CENTER);
        addTab("129-386", soundPanel);

        soundPanel = new SynthPanel(this);
        soundPanel.add(addFrameControls(257), BorderLayout.NORTH);
        panel = new JPanel();
        panel.setBackground(Style.BACKGROUND_COLOR());
        panel.setLayout(new GridBagLayout());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.fill = c.BOTH;
        c.weightx = 1;
        c.weighty = 3;
        hi = new Box(BoxLayout.Y_AXIS);
        hi.add(addFrameDisplay(257, true, Style.COLOR_B()));
        hi.add(addFrameDisplay(257, false, Style.COLOR_B()));
        panel.add(hi, c);
        c.weighty = 1;
        c.gridy = 1;
        panel.add(addPitchDisplay(257, Style.COLOR_C()), c);
        soundPanel.add(panel, BorderLayout.CENTER);
        addTab("257-512", soundPanel);

        model.set("number", 0);
        model.set("bank", 0);
                
        loadDefaults();        
        }
                
    public String getDefaultResourceFileName() { return "YamahaFS1RFseq.init"; }
    public String getHTMLResourceFileName() { return "YamahaFS1RFseq.html"; }

    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        JComboBox bank = new JComboBox(BANKS);
        bank.setSelectedIndex(model.get("bank"));
        if (writing)
            {
            bank = new JComboBox(new String[] { "Internal" });
            bank.setEnabled(false);
            bank.setSelectedIndex(0);
            }
        bank.setEditable(false);
        bank.setMaximumRowCount(32);
                
        JTextField number = new JTextField("" + (model.get("number") + 1), 3);
                
        while(true)
            {
            boolean result = showMultiOption(this, new String[] { "Bank", "Patch Number"}, 
                new JComponent[] { bank, number }, title, "Enter the Bank and Patch number.");
                
            if (result == false) 
                return false;
                          
            int b = bank.getSelectedIndex();      
            int n;
            try { n = Integer.parseInt(number.getText()); }
            catch (NumberFormatException e)
                {
                showSimpleError(title, (b == 0 ? "For the Internal bank, the Patch Number must be an integer 1 ... 6" :
                        "For the Preset bank, the Patch Number must be an integer 1 ... 90"));
                continue;
                }
            if (n < 1 || (b == 0 && n > 6) || (b == 1 && n > 90))
                {
                showSimpleError(title, (b == 0 ? "For the Internal bank, the Patch Number must be an integer 1 ... 6" :
                        "For the Preset bank, the Patch Number must be an integer 1 ... 90"));
                continue;
                }
                                
            change.set("bank", bank.getSelectedIndex());
            change.set("number", n - 1);
                        
            return true;
            }
        }

    public void showMulti()
        {
        if (tuple == null)
            if (!setupMIDI(tuple))
                return;

        if (tuple != null)
            {
            final YamahaFS1RMulti synth = new YamahaFS1RMulti();
            synth.tuple = tuple.copy(synth.buildInReceiver(), synth.buildKeyReceiver(), synth.buildKey2Receiver());
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
                            synth.performRequestCurrentDump();
                            }
                        });
                }
            }
        }

    public void showVoice()
        {
        if (tuple == null)
            if (!setupMIDI(tuple))
                return;

        if (tuple != null)
            {
            final YamahaFS1R synth = new YamahaFS1R();
            synth.tuple = tuple.copy(synth.buildInReceiver(), synth.buildKeyReceiver(), synth.buildKey2Receiver());
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
                
                // we should be Voice 1 by default

                SwingUtilities.invokeLater(
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


    public void setupTestPerformance(int base)
        {
        if (tuple == null)
            if (!setupMIDI(tuple))
                return;

        if (tuple != null)
            {
                {
                // Set up Performance
                YamahaFS1RMulti synth = new YamahaFS1RMulti();            
                synth.tuple = tuple.copy(synth.buildInReceiver(), synth.buildKeyReceiver(), synth.buildKey2Receiver());
                if (synth.tuple != null)
                    {
                    synth.loadDefaults();
                    synth.getModel().set("name", "Edisyn");
                    synth.getModel().set("fseqpart", 1);                            // Part 1                
                    synth.getModel().set("part1rcvchannel", 16);            // Perf
                    synth.getModel().set("part2rcvchannel", 17);            // Off
                    synth.getModel().set("part3rcvchannel", 17);            // Off
                    synth.getModel().set("part4rcvchannel", 17);            // Off
                    synth.getModel().set("part1notereserve", 32);           // Perf
                    synth.getModel().set("part2notereserve", 0);            // Off
                    synth.getModel().set("part3notereserve", 0);            // Off
                    synth.getModel().set("part4notereserve", 0);            // Off
                    synth.getModel().set("part1banknumber", 3);                         // B
                    synth.getModel().set("part1programnumber", 113 + base);     // program number
                    synth.sendAllParameters();
                    }
                }
        
            /*
              {
              // Set up Voice
              YamahaFS1R synth = new YamahaFS1R();            
              synth.tuple = tuple.copy(synth.buildInReceiver(), synth.buildKeyReceiver(), synth.buildKey2Receiver());
              if (synth.tuple != null)
              {
              synth.loadDefaults();
              // part is already 0
              synth.getModel().set("name", "Edisyn");
              synth.getModel().set("operator1vspectralform", 7);      // Formant                
              synth.getModel().set("operator2vspectralform", 7);      // Formant                
              synth.getModel().set("operator3vspectralform", 7);      // Formant                
              synth.getModel().set("operator4vspectralform", 7);      // Formant                
              synth.getModel().set("operator5vspectralform", 7);      // Formant                
              synth.getModel().set("operator6vspectralform", 7);      // Formant                
              synth.getModel().set("operator7vspectralform", 7);      // Formant                
              synth.getModel().set("operator8vspectralform", 7);      // Formant                
              synth.getModel().set("operator1vfrequencyfseqtracknumber", 0);  // Operator 1                
              synth.getModel().set("operator2vfrequencyfseqtracknumber", 1);  // Operator 2                
              synth.getModel().set("operator3vfrequencyfseqtracknumber", 2);  // Operator 3                
              synth.getModel().set("operator4vfrequencyfseqtracknumber", 3);  // Operator 4                
              synth.getModel().set("operator5vfrequencyfseqtracknumber", 4);  // Operator 5                
              synth.getModel().set("operator6vfrequencyfseqtracknumber", 5);  // Operator 6                
              synth.getModel().set("operator7vfrequencyfseqtracknumber", 6);  // Operator 7                
              synth.getModel().set("operator8vfrequencyfseqtracknumber", 7);  // Operator 8                
              synth.getModel().set("operator1vswitch", 1);       // On         
              synth.getModel().set("operator2vswitch", 1);       // On
              synth.getModel().set("operator3vswitch", 1);       // On
              synth.getModel().set("operator4vswitch", 1);       // On
              synth.getModel().set("operator5vswitch", 1);       // On
              synth.getModel().set("operator6vswitch", 1);       // On
              synth.getModel().set("operator7vswitch", 1);       // On
              synth.getModel().set("operator8vswitch", 1);       // On
              synth.getModel().set("operator1uswitch", 1);       // On
              synth.getModel().set("operator2uswitch", 1);       // On
              synth.getModel().set("operator3uswitch", 1);       // On
              synth.getModel().set("operator4uswitch", 1);       // On
              synth.getModel().set("operator5uswitch", 1);       // On
              synth.getModel().set("operator6uswitch", 1);       // On
              synth.getModel().set("operator7uswitch", 1);       // On
              synth.getModel().set("operator8uswitch", 1);       // On
              synth.sendAllParameters();
              }
              }
            */
            
            // Resend Fseq
            sendAllParameters();
            }
        }


    public JFrame sprout()
        {
        JFrame frame = super.sprout();  
        transmitTo.setEnabled(false);           // Though that doesn't matter any more
        addYamahaFS1RMenu();
        return frame;
        }         

    int grab(Model m, int frame, String key)
        {
        if (frame < 1) frame = 1;
        if (frame > 512) frame = 512;
        return m.get("frame" + frame + key);
        }
                
    int filter(Model model, int frame, String key, boolean log)
        {
        if (log)
            {
            return (int)frequencyToInt((
                    intToFrequency(grab(model, frame-3, key)) + 
                    2 * intToFrequency(grab(model, frame-2, key)) + 
                    3 * intToFrequency(grab(model, frame-1, key)) + 
                    4 * intToFrequency(grab(model, frame, key)) +  
                    3 * intToFrequency(grab(model, frame+1, key)) + 
                    2 * intToFrequency(grab(model, frame+2, key)) +
                    intToFrequency(grab(model, frame+3, key)))
                / 16.0);
            }
        else
            {
            return (int)((grab(model, frame-3, key) + 2 * grab(model, frame-2, key) + 3 * grab(model, frame-1, key) + 4 * grab(model, frame, key) + 3 * grab(model, frame+1, key) + 2 * grab(model, frame+2, key) + grab(model, frame+3, key)) / 16.0);
            }
        }

    public void buildPhonemes()
        {
        YamahaFS1RPhonemes.buildModel(YamahaFS1RFseq.this);
        }
                
    public void addYamahaFS1RMenu()
        {
        JMenu menu = new JMenu("FS1R");
        menubar.add(menu);

        JMenuItem text = new JMenuItem("Text");
        text.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                buildPhonemes();
                }
            });
        menu.add(text);

        JMenuItem smooth = new JMenuItem("Smooth");
        smooth.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                Model old = model.copy();
                
                Model backup = (Model)(model.clone());
                setSendMIDI(false);
                undo.setWillPush(false);
                                        
                for(int f = 1; f <= 512; f++)
                    {
                    for(int i = 1; i <= 8; i++)
                        {
                        model.set("frame" + f + "voicedfrequency" + i, filter(old, f, "voicedfrequency" + i, true));
                        model.set("frame" + f + "voicedlevel" + i, filter(old, f, "voicedlevel" + i, false));
                        model.set("frame" + f + "unvoicedfrequency" + i, filter(old, f, "unvoicedfrequency" + i, true));
                        model.set("frame" + f + "unvoicedlevel" + i, filter(old, f, "unvoicedlevel" + i, false));
                        model.set("frame" + f + "pitch" + i, filter(old, f, "pitch" + i, true));
                        }
                    }

                undo.setWillPush(true);
                if (!backup.keyEquals(getModel()))  // it's changed, do an undo push
                    undo.push(backup);
                repaint();      // generally forces repaints to all happen at once
                setSendMIDI(true);
                sendAllParameters();
                }
            });
        menu.add(smooth);

        JMenuItem dump = new JMenuItem("Dump");
        dump.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        dump.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                System.err.println(lastIndex);
                System.err.println(
                    "{\n" +
                    "\t{" + 
                    model.get("frame" + (lastIndex + 1) + "voicedfrequency" + 1) + ", " +
                    model.get("frame" + (lastIndex + 1) + "voicedlevel" + 1) + ", " + 
                    model.get("frame" + (lastIndex + 1) + "unvoicedfrequency" + 1) + ", " +
                    model.get("frame" + (lastIndex + 1) + "unvoicedlevel" + 1) + ", " + 
                    model.get("frame" + (lastIndex + 1) + "pitch") + "},\n" + 
                    "\t{" + 
                    model.get("frame" + (lastIndex + 1) + "voicedfrequency" + 2) + ", " +
                    model.get("frame" + (lastIndex + 1) + "voicedlevel" + 2) + ", " + 
                    model.get("frame" + (lastIndex + 1) + "unvoicedfrequency" + 2) + ", " +
                    model.get("frame" + (lastIndex + 1) + "unvoicedlevel" + 2) + ", " + 
                    model.get("frame" + (lastIndex + 1) + "pitch") + "},\n" + 
                    "\t{" + 
                    model.get("frame" + (lastIndex + 1) + "voicedfrequency" + 3) + ", " +
                    model.get("frame" + (lastIndex + 1) + "voicedlevel" + 3) + ", " + 
                    model.get("frame" + (lastIndex + 1) + "unvoicedfrequency" + 3) + ", " +
                    model.get("frame" + (lastIndex + 1) + "unvoicedlevel" + 3) + ", " + 
                    model.get("frame" + (lastIndex + 1) + "pitch") + "},\n" + 
                    "\t{" + 
                    model.get("frame" + (lastIndex + 1) + "voicedfrequency" + 4) + ", " +
                    model.get("frame" + (lastIndex + 1) + "voicedlevel" + 4) + ", " + 
                    model.get("frame" + (lastIndex + 1) + "unvoicedfrequency" + 4) + ", " +
                    model.get("frame" + (lastIndex + 1) + "unvoicedlevel" + 4) + ", " + 
                    model.get("frame" + (lastIndex + 1) + "pitch") + "},\n" + 
                    "\t{" + 
                    model.get("frame" + (lastIndex + 1) + "voicedfrequency" + 5) + ", " +
                    model.get("frame" + (lastIndex + 1) + "voicedlevel" + 5) + ", " + 
                    model.get("frame" + (lastIndex + 1) + "unvoicedfrequency" + 5) + ", " +
                    model.get("frame" + (lastIndex + 1) + "unvoicedlevel" + 5) + ", " + 
                    model.get("frame" + (lastIndex + 1) + "pitch") + "},\n" + 
                    "\t{" + 
                    model.get("frame" + (lastIndex + 1) + "voicedfrequency" + 6) + ", " +
                    model.get("frame" + (lastIndex + 1) + "voicedlevel" + 6) + ", " + 
                    model.get("frame" + (lastIndex + 1) + "unvoicedfrequency" + 6) + ", " +
                    model.get("frame" + (lastIndex + 1) + "unvoicedlevel" + 6) + ", " + 
                    model.get("frame" + (lastIndex + 1) + "pitch") + "},\n" + 
                    "\t{" + 
                    model.get("frame" + (lastIndex + 1) + "voicedfrequency" + 7) + ", " +
                    model.get("frame" + (lastIndex + 1) + "voicedlevel" + 7) + ", " + 
                    model.get("frame" + (lastIndex + 1) + "unvoicedfrequency" + 7) + ", " +
                    model.get("frame" + (lastIndex + 1) + "unvoicedlevel" + 7) + ", " + 
                    model.get("frame" + (lastIndex + 1) + "pitch") + "},\n" + 
                    "\t{" + 
                    model.get("frame" + (lastIndex + 1) + "voicedfrequency" + 8) + ", " +
                    model.get("frame" + (lastIndex + 1) + "voicedlevel" + 8) + ", " + 
                    model.get("frame" + (lastIndex + 1) + "unvoicedfrequency" + 8) + ", " +
                    model.get("frame" + (lastIndex + 1) + "unvoicedlevel" + 8) + ", " + 
                    model.get("frame" + (lastIndex + 1) + "pitch") + "},\n" + 
                    "}"                     
                    );
                }
            });
        menu.add(dump);

        JMenuItem current = new JMenuItem("Show Current Performance");
        current.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                showMulti();                
                }
            });
        menu.add(current);

        JMenuItem voice = new JMenuItem("Show Current Part 1");
        voice.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                showVoice();                
                }
            });
        menu.add(voice);
        
        JMenu testPerformance = new JMenu("Set up Test Performance and Part 1");
        menu.add(testPerformance);
        for(int i = 1; i <= 14; i++)
            {
            final int _i = i;
            JMenuItem test = new JMenuItem("Use Voice \"FseqBase" + (i < 10 ? "0" : "") + i + "\"");
            test.addActionListener(new ActionListener()
                {
                public void actionPerformed(ActionEvent e)
                    {
                    setupTestPerformance(_i);                
                    }
                });
            testPerformance.add(test);
            }
        
        menu.addSeparator();
        JMenuItem swap = new JMenuItem("Swap Tracks...");
        menu.add(swap);
        swap.addActionListener(new ActionListener()
            {
            JComboBox part1 = new JComboBox(TRACKS);
            JComboBox part2 = new JComboBox(TRACKS);
            LabelledSlider frame1 = new LabelledSlider(1, 512, 1);
            LabelledSlider frame2 = new LabelledSlider(1, 512, 512);
                
            public void actionPerformed(ActionEvent evt)
                {
                part1.setMaximumRowCount(8);
                part2.setMaximumRowCount(8);
                boolean result = showMultiOption(YamahaFS1RFseq.this, new String[] { "Swap", "With", "Start Frame", "End Frame" }, 
                    new JComponent[] { part1, part2, frame1, frame2 }, "Swap Tracks...", "Enter the tracks to swap with one another.");

                if (result)
                    {
                    int p1 = part1.getSelectedIndex() + 1;
                    int p2 = part2.getSelectedIndex() + 1;
                    int from = frame1.getValue();
                    int to = frame2.getValue();
                    if (from > to)
                        {
                        int temp = from;
                        from = to;
                        to = temp;
                        }

                    Model backup = (Model)(model.clone());
                    setSendMIDI(false);
                    undo.setWillPush(false);
                                        
                    String[] parameters = model.getKeys();
                    for(int i = 0; i < parameters.length; i++)
                        {
                        if (parameters[i].startsWith("frame") && parameters[i].endsWith("" + p2))
                            {
                            int frame = StringUtility.getFirstInt(parameters[i]);
                            if (frame >= from && frame <= to)
                                {
                                String p1key = parameters[i].substring(0, parameters[i].length() - 1) + p1;             // strip off the last number
                                int val2 = model.get(parameters[i]);
                                int val1 = model.get(p1key);  
                                model.set(p1key, val2);
                                model.set(parameters[i], val1);
                                }
                            }
                        }
                                                                                
                    undo.setWillPush(true);
                    if (!backup.keyEquals(getModel()))  // it's changed, do an undo push
                        undo.push(backup);
                    repaint();      // generally forces repaints to all happen at once
                    setSendMIDI(true);
                    sendAllParameters();
                    }
                }
            });

        JMenuItem copyTrack = new JMenuItem("Copy Track To...");
        menu.add(copyTrack);
        copyTrack.addActionListener(new ActionListener()
            {
            JComboBox part1 = new JComboBox(TRACKS);
            JComboBox part2 = new JComboBox(TRACKS_PLUS);
            LabelledSlider frame1 = new LabelledSlider(1, 512, 1);
            LabelledSlider frame2 = new LabelledSlider(1, 512, 512);
                
            public void actionPerformed(ActionEvent evt)
                {
                part1.setMaximumRowCount(8);
                part2.setMaximumRowCount(9);
                boolean result = showMultiOption(YamahaFS1RFseq.this, new String[] { "Copy", "To", "Start Frame", "End Frame"}, 
                    new JComponent[] { part1, part2, frame1, frame2, }, "Copy Track To...", "Enter the tracks to copy from and to.");

                if (result)
                    {
                    int p1 = part1.getSelectedIndex() + 1;
                    int p2 = part2.getSelectedIndex() + 1;
                    int from = frame1.getValue();
                    int to = frame2.getValue();
                    if (from > to)
                        {
                        int temp = from;
                        from = to;
                        to = temp;
                        }

                    Model backup = (Model)(model.clone());
                    setSendMIDI(false);
                    undo.setWillPush(false);
                                        
                    String[] parameters = model.getKeys();
                    for(int i = 0; i < parameters.length; i++)
                        {
                        if (p2 == TRACK_ALL && parameters[i].startsWith("frame"))        // "All"
                            {
                            int frame = StringUtility.getFirstInt(parameters[i]);
                            if (frame >= from && frame <= to)
                                {
                                String p1key = parameters[i].substring(0, parameters[i].length() - 1) + p1;         // strip off the last number
                                int val1 = model.get(p1key);  
                                model.set(parameters[i], val1);
                                }
                            }
                        else if (parameters[i].startsWith("frame") && parameters[i].endsWith("" + p2))
                            {
                            int frame = StringUtility.getFirstInt(parameters[i]);
                            if (frame >= from && frame <= to)
                                {
                                String p1key = parameters[i].substring(0, parameters[i].length() - 1) + p1;         // strip off the last number
                                int val1 = model.get(p1key);  
                                model.set(parameters[i], val1);
                                }
                            }
                        }
                                                                                
                    undo.setWillPush(true);
                    if (!backup.keyEquals(getModel()))  // it's changed, do an undo push
                        undo.push(backup);
                    repaint();      // generally forces repaints to all happen at once
                    setSendMIDI(true);
                    sendAllParameters();
                    }
                }
            });
 
        menu.addSeparator();

        JMenuItem cut = new JMenuItem("Cut Frames...");
        menu.add(cut);
        cut.addActionListener(new ActionListener()
            {
            LabelledSlider frame1 = new LabelledSlider(1, 512, 1);
            LabelledSlider frame2 = new LabelledSlider(1, 512, 512);
            
            public void actionPerformed(ActionEvent evt)
                {
                boolean result = showMultiOption(YamahaFS1RFseq.this, new String[] { "Start Frame", "End Frame" }, 
                    new JComponent[] { frame1, frame2 }, "Cut Frames...", "Enter the frame range to copy onto the clipboard.");

                if (result)
                    {
                    int from = frame1.getValue();
                    int to = frame2.getValue();
                    if (from > to)
                        {
                        int temp = from;
                        from = to;
                        to = temp;
                        }
                        
                    // grab range
                    pasteboard = buildModel();
                    pasteboardLo = from;
                    pasteboardHi = to;
                    
                    Model backup = (Model)(model.clone());
                    setSendMIDI(false);
                    undo.setWillPush(false);
                    
                    for(int i = from; i <= to; i++)
                        {
                        move(model, pasteboard, i, i, TRACK_ALL, true, true, true);
                        }
                                                
                    // Shift upper range down
                    for(int i = to + 1; i <= 512; i++)
                        {
                        move(model, model, i, i - (to - from + 1), TRACK_ALL, true, true, true);
                        }
                                        
                    // Clear highest range
                    for(int i = 512 - (to - from + 1) + 1; i <= 512; i++)
                        {
                        clear(model, i, TRACK_ALL, true, true, true);
                        }
                                        
                    undo.setWillPush(true);
                    if (!backup.keyEquals(getModel()))  // it's changed, do an undo push
                        {
                        undo.push(backup);
                        System.err.println("pushed!");
                        }
                    else System.err.println("The same!");
                    repaint();      // generally forces repaints to all happen at once
                    setSendMIDI(true);
                    sendAllParameters();
                    }
                }
            });


        JMenuItem copy = new JMenuItem("Copy Frames...");
        menu.add(copy);
        copy.addActionListener(new ActionListener()
            {
            LabelledSlider frame1 = new LabelledSlider(1, 512, 1);
            LabelledSlider frame2 = new LabelledSlider(1, 512, 512);
            
            public void actionPerformed(ActionEvent evt)
                {
                boolean result = showMultiOption(YamahaFS1RFseq.this, new String[] { "Start Frame", "End Frame" }, 
                    new JComponent[] { frame1, frame2 }, "Copy Frames...", "Enter the frame range to copy onto the clipboard.");

                if (result)
                    {
                    int from = frame1.getValue();
                    int to = frame2.getValue();
                    if (from > to)
                        {
                        int temp = from;
                        from = to;
                        to = temp;
                        }

                    // grab range
                    pasteboard = buildModel();
                    pasteboardLo = from;
                    pasteboardHi = to;

                    for(int i = from; i <= to; i++)
                        {
                        move(model, pasteboard, i, i, TRACK_ALL, true, true, true);
                        }
                    }
                }
            });


        JMenuItem paste = new JMenuItem("Paste Frames...");
        menu.add(paste);
        paste.addActionListener(new ActionListener()
            {
            LabelledSlider frame1 = new LabelledSlider(1, 512, 1);
            
            public void actionPerformed(ActionEvent evt)
                {
                if (pasteboard == null)
                    {
                    showSimpleError("Overwrite Frames...", "No frames have been cut/copied yet.");
                    return;
                    }
                
                boolean result = showMultiOption(YamahaFS1RFseq.this, new String[] { "At" }, 
                    new JComponent[] { frame1 }, "Paste Frames...", "Enter where to insert the clipboard frames.");

                if (result)
                    {
                    int at = frame1.getValue();
                    
                    Model backup = (Model)(model.clone());
                    setSendMIDI(false);
                    undo.setWillPush(false);
                    
                    // make room
                    for(int i = 512; i >= at; i--)
                        {
                        move(model, model, i, i + (pasteboardHi - pasteboardLo + 1), TRACK_ALL, true, true, true);
                        }
                    
                    // paste range
                    for(int i = pasteboardLo; i <= pasteboardHi; i++)
                        {
                        move(pasteboard, model, i, i - pasteboardLo + at, TRACK_ALL, true, true, true);
                        }

                    undo.setWillPush(true);
                    if (!backup.keyEquals(getModel()))  // it's changed, do an undo push
                        undo.push(backup);
                    repaint();      // generally forces repaints to all happen at once
                    setSendMIDI(true);
                    sendAllParameters();
                    }
                }
            });

        JMenuItem overwrite = new JMenuItem("Overwrite Frames...");
        menu.add(overwrite);
        overwrite.addActionListener(new ActionListener()
            {
            JComboBox part1 = new JComboBox(TRACKS_PLUS);
            LabelledSlider frame1 = new LabelledSlider(1, 512, 1);
            JCheckBox voiced = new JCheckBox("", true);
            JCheckBox unvoiced = new JCheckBox("", true);
            JCheckBox includePitch = new JCheckBox("", true);
            
            public void actionPerformed(ActionEvent evt)
                {
                if (pasteboard == null)
                    {
                    showSimpleError("Overwrite Frames...", "No frames have been cut/copied yet.");
                    return;
                    }
                
                part1.setMaximumRowCount(9);
                part1.setSelectedIndex(8);  // "All"
                JPanel panel = new JPanel();
                panel.setLayout(new BorderLayout());
                panel.add(part1, BorderLayout.WEST);
                boolean result = showMultiOption(YamahaFS1RFseq.this, new String[] { "Track", "At", "Voiced", "Unvoiced", "Pitch" }, 
                    new JComponent[] { panel, frame1, voiced, unvoiced, includePitch }, "Overwrite Frames...", "Enter where to overwrite frames, and which tracks to overwrite.");

                if (result)
                    {
                    boolean pitch = includePitch.isSelected();
                    boolean v = voiced.isSelected();
                    boolean u = unvoiced.isSelected();
                    int p1 = part1.getSelectedIndex() + 1;
                    int at = frame1.getValue();

                    Model backup = (Model)(model.clone());
                    setSendMIDI(false);
                    undo.setWillPush(false);
                                        
                    // paste range
                    for(int i = pasteboardLo; i <= pasteboardHi; i++)
                        {
                        move(pasteboard, model, i, i - pasteboardLo + at, p1, v, u, pitch);
                        }
                                                                                                                                
                    undo.setWillPush(true);
                    if (!backup.keyEquals(getModel()))  // it's changed, do an undo push
                        undo.push(backup);
                    repaint();      // generally forces repaints to all happen at once
                    setSendMIDI(true);
                    sendAllParameters();
                    }
                }
            });
            
        menu.addSeparator();
    
        JMenuItem clear = new JMenuItem("Clear Frames...");
        menu.add(clear);
        clear.addActionListener(new ActionListener()
            {
            JComboBox part1 = new JComboBox(TRACKS_PLUS);
            LabelledSlider frame1 = new LabelledSlider(1, 512, 1);
            LabelledSlider frame2 = new LabelledSlider(1, 512, 512);
            JCheckBox voiced = new JCheckBox("", true);
            JCheckBox unvoiced = new JCheckBox("", true);
            JCheckBox includePitch = new JCheckBox("", true);
                
            public void actionPerformed(ActionEvent evt)
                {
                part1.setMaximumRowCount(9);
                part1.setSelectedIndex(8);  // "All"
                JPanel panel = new JPanel();
                panel.setLayout(new BorderLayout());
                panel.add(part1, BorderLayout.WEST);
                boolean result = showMultiOption(YamahaFS1RFseq.this, new String[] { "Track", "Start Frame", "End Frame", "Voiced", "Unvoiced", "Pitch" }, 
                    new JComponent[] { panel, frame1, frame2, voiced, unvoiced, includePitch }, "Clear Frames...", "Enter the frames and tracks to clear.");

                if (result)
                    {
                    boolean pitch = includePitch.isSelected();
                    boolean v = voiced.isSelected();
                    boolean u = unvoiced.isSelected();
                    int p1 = part1.getSelectedIndex() + 1;
                    int from = frame1.getValue();
                    int to = frame2.getValue();
                    if (from > to)
                        {
                        int temp = from;
                        from = to;
                        to = temp;
                        }

                    Model backup = (Model)(model.clone());
                    setSendMIDI(false);
                    undo.setWillPush(false);
                                        
                    String[] parameters = model.getKeys();
                    for(int i = 0; i < parameters.length; i++)
                        {
                        if (parameters[i].startsWith("frame"))
                            {
                            int frame = StringUtility.getFirstInt(parameters[i]);
                            if (frame >= from && frame <= to)
                                {
                                if (parameters[i].endsWith("pitch"))
                                    {
                                    if (pitch) model.set(parameters[i], 0);
                                    }
                                else if (p1 == TRACK_ALL || parameters[i].endsWith("" + p1))
                                    {
                                    if (parameters[i].contains("unvoiced"))
                                        {
                                        if (u) model.set(parameters[i], 0);
                                        }
                                    else            // voiced                       -- notice I searched for "unvoiced", think hard about that...
                                        {
                                        if (v) model.set(parameters[i], 0);
                                        }
                                    }
                                }
                            }
                        }
                                                                                
                    undo.setWillPush(true);
                    if (!backup.keyEquals(getModel()))  // it's changed, do an undo push
                        undo.push(backup);
                    repaint();      // generally forces repaints to all happen at once
                    setSendMIDI(true);
                    sendAllParameters();
                    }
                }
            });
 
        JMenuItem distribute = new JMenuItem("Distribute Frame");
        menu.add(distribute);
        distribute.addActionListener(new ActionListener()
            {
            JComboBox part1 = new JComboBox(TRACKS_PLUS);
            LabelledSlider frame0 = new LabelledSlider(1, 512, 1);
            LabelledSlider frame1 = new LabelledSlider(1, 512, 1);
            LabelledSlider frame2 = new LabelledSlider(1, 512, 512);
            JCheckBox voiced = new JCheckBox("", true);
            JCheckBox unvoiced = new JCheckBox("", true);
            JCheckBox includePitch = new JCheckBox("", true);
                
            public void actionPerformed(ActionEvent evt)
                {
                part1.setMaximumRowCount(9);
                part1.setSelectedIndex(8);  // "All"
                JPanel panel = new JPanel();
                panel.setLayout(new BorderLayout());
                panel.add(part1, BorderLayout.WEST);
                boolean result = showMultiOption(YamahaFS1RFseq.this, new String[] { "Track", "Distribute", "To (Start)", "To (End)", "Voiced", "Unvoiced", "Pitch" }, 
                    new JComponent[] { panel, frame0, frame1, frame2, voiced, unvoiced, includePitch }, "Distribute Frames...", "Enter the frame to extract values from and the frame range to copy them to.");

                if (result)
                    {
                    boolean pitch = includePitch.isSelected();
                    boolean v = voiced.isSelected();
                    boolean u = unvoiced.isSelected();
                    int p1 = part1.getSelectedIndex() + 1;
                    int original = frame0.getValue();
                    int from = frame1.getValue();
                    int to = frame2.getValue();
                    if (from > to)
                        {
                        int temp = from;
                        from = to;
                        to = temp;
                        }

                    Model backup = (Model)(model.clone());
                    setSendMIDI(false);
                    undo.setWillPush(false);
                                        
                    String[] parameters = model.getKeys();
                    for(int i = from; i <= to; i++)
                        {
                        move(model, model, original, i, p1, v, u, pitch);
                        }
                                                                                
                    undo.setWillPush(true);
                    if (!backup.keyEquals(getModel()))  // it's changed, do an undo push
                        undo.push(backup);
                    repaint();      // generally forces repaints to all happen at once
                    setSendMIDI(true);
                    sendAllParameters();
                    }
                }
            });           


        JMenuItem normalize = new JMenuItem("Normalize Frames...");
        normalize.addActionListener(new ActionListener()
            {
            JComboBox part1 = new JComboBox(TRACKS_PLUS);
            LabelledSlider frame1 = new LabelledSlider(1, 512, 1);
            LabelledSlider frame2 = new LabelledSlider(1, 512, 512);
            JCheckBox voiced = new JCheckBox("", true);
            JCheckBox unvoiced = new JCheckBox("", true);
                
            public void actionPerformed(ActionEvent evt)
                {
                part1.setMaximumRowCount(9);
                part1.setSelectedIndex(8);  // "All"
                JPanel panel = new JPanel();
                panel.setLayout(new BorderLayout());
                panel.add(part1, BorderLayout.WEST);
                boolean result = showMultiOption(YamahaFS1RFseq.this, new String[] { "Track", "Start", "End", "Voiced", "Unvoiced" }, 
                    new JComponent[] { panel, frame1, frame2, voiced, unvoiced }, "Normalize Frames...", "Enter the frames to normalize levels.");

                if (result)
                    {
                    boolean v = voiced.isSelected();
                    boolean u = unvoiced.isSelected();
                    int p1 = part1.getSelectedIndex() + 1;
                    int from = frame1.getValue();
                    int to = frame2.getValue();
                    if (from > to)
                        {
                        int temp = from;
                        from = to;
                        to = temp;
                        }

                    Model backup = (Model)(model.clone());
                    setSendMIDI(false);
                    undo.setWillPush(false);
                                  
                    // determine max  
                    double max = 0;    
                    for(int i = from; i <= to; i++)
                        {
                        if (p1 == TRACK_ALL)
                            {
                            for(int op = 1; op <= 8; op++)
                                {
                                if (v) max = Math.max(max, model.get("frame" + i + "voicedlevel" + op));
                                if (u) max = Math.max(max, model.get("frame" + i + "unvoicedlevel" + op));
                                }
                            }
                        else
                            {
                            if (v) max = Math.max(max, model.get("frame" + i + "voicedlevel" + p1));
                            if (u) max = Math.max(max, model.get("frame" + i + "unvoicedlevel" + p1));
                            }
                        }

                    // normalize  
                    if (max > 0)
                        {   
                        for(int i = from; i <= to; i++)
                            {
                            if (p1 == TRACK_ALL)
                                {
                                for(int op = 1; op <= 8; op++)
                                    {
                                    if (v) model.set("frame" + i + "voicedlevel" + op,
                                        (int)(127.0 * model.get("frame" + i + "voicedlevel" + op) / max));
                                    if (u) model.set("frame" + i + "unvoicedlevel" + op,
                                        (int)(127.0 * model.get("frame" + i + "unvoicedlevel" + op) / max));
                                    }
                                }
                            else
                                {
                                if (v) model.set("frame" + i + "voicedlevel" + p1,
                                    (int)(127.0 * model.get("frame" + i + "voicedlevel" + p1) / max));
                                if (u) model.set("frame" + i + "unvoicedlevel" + p1,
                                    (int)(127.0 * model.get("frame" + i + "unvoicedlevel" + p1) / max));
                                }
                            }
                        }
                                                                                
                    undo.setWillPush(true);
                    if (!backup.keyEquals(getModel()))  // it's changed, do an undo push
                        undo.push(backup);
                    repaint();      // generally forces repaints to all happen at once
                    setSendMIDI(true);
                    sendAllParameters();
                    }
                }
            });
        menu.add(normalize);


        JMenuItem setpitch = new JMenuItem("Set Pitch...");
        setpitch.addActionListener(new ActionListener()
            {
            LabelledSlider frame1 = new LabelledSlider(1, 512, 1);
            LabelledSlider frame2 = new LabelledSlider(1, 512, 512);
            JTextField pitch = new JTextField("440.00");
                
            public void actionPerformed(ActionEvent evt)
                {
                pitch.setColumns(8);
                boolean result = showMultiOption(YamahaFS1RFseq.this, new String[] { "Start", "End", "Pitch (Hz)" }, 
                    new JComponent[] { frame1, frame2, pitch }, "Set Pitch...", "Enter the frames and the pitch (0..." + MAXFREQ + ") to set them to.");

                if (result)
                    {
                    double pitchVal = -1;
                    try 
                        { 
                        pitchVal = Double.parseDouble(pitch.getText());
                        }
                    catch (Exception ex) { }
                    if (pitchVal < 0 || pitchVal > MAXFREQ)
                        {
                        showSimpleError("Invalid Pitch", "The pitch must be between 0 and " + MAXFREQ);
                        }
                    else
                        {
                        int from = frame1.getValue();
                        int to = frame2.getValue();
                        if (from > to)
                            {
                            int temp = from;
                            from = to;
                            to = temp;
                            }

                        int val = frequencyToInt(pitchVal);
                        if (val < 0)
                            {
                            System.err.println("Low val " + val);
                            val = 0;
                            }
                        else if (val > 16383)
                            {
                            System.err.println("High val " + val);
                            val = 16383;
                            }

                        Model backup = (Model)(model.clone());
                        setSendMIDI(false);
                        undo.setWillPush(false);
                                                                  
                        for(int i = from; i <= to; i++)
                            {
                            model.set("frame" + i + "pitch", val);
                            }
                                                                                                                                                                
                        undo.setWillPush(true);
                        if (!backup.keyEquals(getModel()))  // it's changed, do an undo push
                            undo.push(backup);
                        repaint();      // generally forces repaints to all happen at once
                        setSendMIDI(true);
                        sendAllParameters();
                        }
                    }
                }
            });
        menu.add(setpitch);
        }

    public static final int TRACK_ALL = 9;
    void move(Model old, Model current, int from, int to, int track, boolean voiced, boolean unvoiced, boolean includePitch)
        {
        int start = 1;
        int end = 8;
        if (track != TRACK_ALL)
            {
            start = track;
            end = track;
            }
                
        if (to > 512)
            {
//              System.err.println("Stupid move to " + to);
            return; 
            }

        if (to < 1)
            {
            System.err.println("WARNING, bad move to " + to);
            return; 
            }
        
        String frameFrom = "frame" + from;
        String frameTo = "frame" + to;
        
        if (includePitch)
            {
            String keyFrom = frameFrom + "pitch";
            String keyTo = frameTo + "pitch";
            current.set(keyTo, old.get(keyFrom));
            }
                
        for(int i = start; i <= end; i++)
            {
            if (voiced)
                {
                String keyFrom = frameFrom + "voicedfrequency" + i;
                String keyTo = frameTo + "voicedfrequency" + i;
                current.set(keyTo, old.get(keyFrom));
                keyFrom = frameFrom + "voicedlevel" + i;
                keyTo = frameTo + "voicedlevel" + i;
                current.set(keyTo, old.get(keyFrom));
                }
            if (unvoiced)
                {
                String keyFrom = frameFrom + "unvoicedfrequency" + i;
                String keyTo = frameTo + "unvoicedfrequency" + i;
                current.set(keyTo, old.get(keyFrom));
                keyFrom = frameFrom + "unvoicedlevel" + i;
                keyTo = frameTo + "unvoicedlevel" + i;
                current.set(keyTo, old.get(keyFrom));
                }
            }
        }
       
    void clear(Model current, int to, int track, boolean voiced, boolean unvoiced, boolean includePitch)
        {
        int start = 1;
        int end = 8;
        if (track != TRACK_ALL)
            {
            start = track;
            end = track;
            }
        
        String frameTo = "frame" + to;
        
        if (includePitch)
            {
            String keyTo = frameTo + "pitch";
            current.set(keyTo, 0);
            }
                
        System.err.println("Clearing " + to);
        for(int i = start; i <= end; i++)
            {
            if (voiced)
                {
                String keyTo = frameTo + "voicedfrequency" + i;
                current.set(keyTo, 0);
                keyTo = frameTo + "voicedlevel" + i;
                current.set(keyTo, 0);
                }
            if (unvoiced)
                {
                String keyTo = frameTo + "unvoicedfrequency" + i;
                current.set(keyTo, 0);
                keyTo = frameTo + "unvoicedlevel" + i;
                current.set(keyTo, 0);
                }
            }
        }

        
    /* 
    // These were the original functions Thor had dug up.  They're not inverses unfortunately:
    
    public static final double C1 = 181378.0422;
    public static final double C2 = 1.001344206;
    public double intToFrequency(int val)
    {
    int hi = (val >>> 7) & 127;
    int lo = val & 127;
        
    return Math.pow(2, hi/4.0) / C1 * Math.pow(C2, lo);
    }
        
    public static final double K = 738.5;
    public static final double P0 = 8983.3;
    public static final double FIX = 1.0637;
    
    public int frequencyToInt(double frequency)
    {
    return (int)(K * Math.log(frequency / FIX) + P0);
    }
    */
        
    /// However this function is a close match for the intToFrequency above and is much simpler */
    public double intToFrequency(int val)
        {
        return Math.exp(-12.1104 + 0.0013538 * val);
        }
    
    /// This is the inverse according to Mathematica
    public int frequencyToInt(double frequency)
        {
        return (int)(738.662 * Math.log(181752 * frequency));
        }
        
    /*
    // On a log graph, the original intToFrequency() is a nearly perfect line going from -12.1083 ... 10.0697,
    // which suggests some other very simple arrangements.  Such as just going from -12 to +10, which yields:
        
    public double intToFrequency(int val)
    {
    return Math.exp(val/16384.0 * 22 - 12);
    }

    // or perhaps Yamaha might actually just be going from -10 to +10, so we'd have:
    public double intToFrequency(int val)
    {
    return Math.exp(val/16384.0 * 20 - 10);
    }
    */
    
    public String intToFrequencyString(int val, boolean minimize)
        {
        double d = intToFrequency(val);
        if (val == 0) return "0";
        else if (d >= 10000)
            {
            return "" + (int)d;
            }
        else if (d >= 1000)
            {
            return String.format("%5.1f", d);
            }
        else if (d >= 100)
            {
            return String.format("%5.2f", d);
            }
        else if (d >= 10)
            {
            return String.format("%5.3f", d);
            }
        else if (d >= 1)
            {
            return String.format("%5.4f", d);
            }
        else if (d >= 0.01)
            {
            return String.format("%5.5f", d).substring(1);
            }
        else            // here we have a problem, the resolution is too small even for scientific notation, so we have to change the font size
            {
            if (minimize)
                return "<html><font size=-2>" + String.format("%4.2e", d) + "</font></html>";
            else
                return String.format("%4.2e", d);
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
        HBox hbox2 = new HBox();
        comp = new PatchDisplay(this, 8);
        hbox2.add(comp);
        vbox.add(hbox2);
        
        comp = new StringComponent("Patch Name", this, "name", 8, "Name must be up to 8 ASCII characters.")
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
        hbox.addLast(Strut.makeHorizontalStrut(80));

        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }


    public JComponent addFrame(int frame, Color color)
        {
        Category category = new Category(this, "Frame " + frame, color);

        JComponent comp;
        String[] params;
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        
        LabelledDial pitch = new LabelledDial("Pitch", this, "frame" + frame + "pitch", color, 0, 16383)
            {
            public String map(int val)
                {
                return intToFrequencyString(val, true);
                }
            };
        comp = pitch;
        hbox.add(comp);

        for(int i = 1; i <= 8; i++)
            {
            comp = new LabelledDial("V Freq " + i, this, "frame" + frame + "voicedfrequency" + i, color, 0, 16383)
                {
                public String map(int val)
                    {
                    return intToFrequencyString(val, true);
                    }
                };
            hbox.add(comp);
            comp = new LabelledDial("V Level " + i, this, "frame" + frame + "voicedlevel" + i, color, 0, 127);
            hbox.add(comp);
            }
                        
        vbox.add(hbox);
        hbox = new HBox();
                        
        hbox.add(Strut.makeStrut(pitch));
                
        for(int i = 1; i <= 8; i++)
            {
            comp = new LabelledDial("U Freq " + i, this, "frame" + frame + "unvoicedfrequency" + i, color, 0, 16383)
                {
                public String map(int val)
                    {
                    return intToFrequencyString(val, true);
                    }
                };
            hbox.add(comp);
            comp = new LabelledDial("U Level " + i, this, "frame" + frame + "unvoicedlevel" + i, color, 0, 127);
            hbox.add(comp);
            }
                        
        vbox.add(hbox);
        category.add(vbox, BorderLayout.CENTER);
        return category;
        }


    public static final int VERTICAL_SIZE = 500;

    public JComponent addFrames(Color color, Color colorA, Color colorB)
        {
        JComponent comp;
        String[] params;
        
        
        final JComponent typical = addFrame(0, color);          // throwaway
        final int h = typical.getPreferredSize().height;
        final int w = typical.getPreferredSize().width;
        
        ScrollableVBox frames = new ScrollableVBox()
            {
            public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
                {
                if (orientation == SwingConstants.VERTICAL)
                    return w;
                else
                    return h;
                }

            public Dimension getPreferredScrollableViewportSize()
                {
                Dimension size = getPreferredSize();
                size.height = h * 3;
                return size;
                }
            };
                
        for(int i = 1; i <= 512; i++)
            {
            JComponent frame = addFrame(i, (i % 2 == 0 ? colorA : colorB));
            frames.add(frame);
            }


        JScrollPane pane = new JScrollPane(frames, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        pane.setBorder(null);
                
        return pane;
        }


    public JComponent addFrameControls(int pos)
        {
        String[] params;
        HBox hbox = new HBox();
        JComponent comp;
                
        params = TRACKS;
        comp = new Chooser("Edit", this, "manipulate", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                // this should do the job
                rebuildFrameDisplays();
                }
            };
        hbox.add(comp);
        model.setStatus("manipulate", Model.STATUS_IMMUTABLE);

        for(int i = 1; i <= 8; i++)
            {
            comp = new CheckBox("Show " + i, this, "show" + i)
                {
                public void update(String key, Model model)
                    {
                    super.update(key, model);
                    // this should do the job
                    rebuildFrameDisplays();
                    }
                };
            hbox.add(comp);
            model.set("show" + i, 1);
            model.setStatus("show" + i, Model.STATUS_IMMUTABLE);
            }

        comp = new CheckBox("Display in Hz", this, "showfreq")
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                // this should do the job
                rebuildFrameDisplays();
                }
            };
        hbox.add(comp);
        model.set("showfreq", 1);
        model.setStatus("showfreq", Model.STATUS_IMMUTABLE);

        PushButton button = new PushButton("Show All")
            {
            public void perform()
                {
                for(int i = 1; i <= 8; i++)
                    {
                    model.set("show" + i, 1);
                    }
                }
            };
        hbox.add(button);
        
        button = new PushButton("Hide All")
            {
            public void perform()
                {
                for(int i = 1; i <= 8; i++)
                    {
                    model.set("show" + i, 0);
                    }
                }
            };
        hbox.add(button);
        
        hbox.add(Strut.makeHorizontalStrut(20));
        
        VBox labels = new VBox();
        
        if (pos == 1)
            {
            posx1 = new TextLabel("");
            labels.add(posx1);
            posy1 = new TextLabel("");
            labels.add(posy1);              
            }
        else if (pos == 257)
            {
            posx2 = new TextLabel("");
            labels.add(posx2);
            posy2 = new TextLabel("");
            labels.add(posy2);              
            }
        else    // (pos == 129)
            {
            posx3 = new TextLabel("");
            labels.add(posx3);
            posy3 = new TextLabel("");
            labels.add(posy3);              
            }
        hbox.addLast(labels);
        
        return hbox;
        }
 
    TextLabel posx1;
    TextLabel posy1;
    TextLabel posx3;
    TextLabel posy3;
    TextLabel posx2;
    TextLabel posy2;
 
    int lastIndex;
 

    public EnvelopeDisplay buildFrequencyDisplay(int pos, boolean voiced, Color color)
        {
        EnvelopeDisplay parent = null;
        JComponent comp;
        ArrayList<EnvelopeDisplay> kids = new ArrayList<EnvelopeDisplay>();
        

        double[] widths = new double[256];
        for(int i = 1; i < widths.length; i++)
            widths[i] = 1.0 / (256 - 1);

        double[] heights = new double[256];
        for(int i = 0; i < heights.length; i++)
            heights[i] = 1.0 / 16383;
                

        for(int o = 1; o <= 8; o++)
            {
            if (model.get("show" + o, 0) == 0 &&
                model.get("manipulate", 0) + 1 != o)
                continue;
                        
            final String[] mods = new String[256];
            for(int i = 0; i < mods.length; i++)
                {
                mods[i] = "frame" + (i + pos) + (voiced ? "" : "un") + "voicedfrequency" + o;
                }
                                
            final int _o = o;
        
            comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), new String[256], mods, widths, heights)
                {
                // The mouseDown and mouseUp code here enables us to only do undo()
                // ONCE.
                public void mouseDown()
                    {
                    getUndo().push(getModel());
                    getUndo().setWillPush(false);
                    }
                
                public void mouseUp()
                    {
                    getUndo().setWillPush(true);
                    }
                        
                public void postProcess(double[] xVals, double[] yVals)
                    {
                    if (YamahaFS1RFseq.this.model.get("showfreq") == 1)
                        {
                        for(int i = 0; i < yVals.length; i++)
                            {
                            yVals[i] = intToFrequency((int)(yVals[i] * 16383)) / MAXFREQ;
                            if (yVals[i] > MAXFREQ) yVals[i] = MAXFREQ;
                            if (yVals[i] < 0) yVals[i] = 0;
                            }
                        }
                    }

                public void updateFromMouse(double x, double y, boolean continuation)
                    {
                    if (x < 0)
                        x = 0;
                    else if (x > 1)
                        x = 1.0;

                    if (y <= 0.0) y = 0.0;
                    if (y >= 1.0) y = 1.0;
                    int frame = (int)(x * (256.0 - 1) + 0.5);

                    if (YamahaFS1RFseq.this.model.get("showfreq") == 1)
                        {
                        double val = (int)(frequencyToInt(y * MAXFREQ));
                        if (val > 16383)
                            {
                            //System.err.println("Val was " + val + " " + y);
                            val = 16383;
                            }
                        if (val < 0)
                            {
                            //System.err.println("Val was " + val + " " + y);
                            val = 0;
                            }
                        YamahaFS1RFseq.this.model.set("frame" + (frame + pos) + (voiced ? "" : "un") + "voicedfrequency" + _o, (int)val);
                        }
                    else
                        {
                        double val = y * 16383;                                                                    
                        YamahaFS1RFseq.this.model.set("frame" + (frame + pos) + (voiced ? "" : "un") + "voicedfrequency" + _o, (int)val);
                        }
                    }

                public void updateHighlightIndex(int index)
                    {
                    lastIndex = index;
                                
                    if (pos == 1)
                        {
                        int track = model.get("manipulate") + 1;
                        int level = YamahaFS1RFseq.this.model.get("frame" + (index + 1) + (voiced ? "" : "un") + "voicedlevel" + track);
                        int freq = YamahaFS1RFseq.this.model.get("frame" + (index + 1) + (voiced ? "" : "un") + "voicedfrequency" + track);
                        posx1.setText("Frame " + (index + 1) + "    Track " + track  + "    Level " + level);
                        posy1.setText("Raw Freq " + freq + "    Hz " + intToFrequencyString(freq, false));
                        }
                    else if (pos == 257)
                        {
                        int track = model.get("manipulate") + 1;
                        int level = YamahaFS1RFseq.this.model.get("frame" + (index + 257) + (voiced ? "" : "un") + "voicedlevel" + track);
                        int freq = YamahaFS1RFseq.this.model.get("frame" + (index + 257) + (voiced ? "" : "un") + "voicedfrequency" + track);
                        posx2.setText("Frame " + (index + 1) + "    Track " + track + "    Level " + level);
                        posy2.setText("Raw Freq " + freq + "    Hz " + intToFrequencyString(freq, false));
                        }
                    else //if (pos == 129)
                        {
                        int track = model.get("manipulate") + 1;
                        int level = YamahaFS1RFseq.this.model.get("frame" + (index + 129) + (voiced ? "" : "un") + "voicedlevel" + track);
                        int freq = YamahaFS1RFseq.this.model.get("frame" + (index + 129) + (voiced ? "" : "un") + "voicedfrequency" + track);
                        posx3.setText("Frame " + (index + 1) + "    Track " + track + "    Level " + level);
                        posy3.setText("Raw Freq " + freq + "    Hz " + intToFrequencyString(freq, false));
                        }
                    }
                                        
                public int highlightIndex(double x, double y, boolean continuation)
                    {
                    if (x < 0) x = 0;
                    if (x > 1.0) x = 1.0;
                    return (int)(x * (256.0 - 1) + 0.5);
                    }

                public int verticalBorderThickness() { return 4; }
                };
            ((EnvelopeDisplay)comp).addVerticalDivider(0.5);
            ((EnvelopeDisplay)comp).setPreferredHeight(150);
                        
            if (model.get("manipulate", 0) + 1 == o)
                {
                parent = (EnvelopeDisplay)comp;
                }
            else
                {
                kids.add((EnvelopeDisplay)comp);
                }

            ((EnvelopeDisplay)comp).setFilled(false);
            }
                
        for(int i = 0; i < kids.size(); i++)
            {
            parent.link(kids.get(i));
            }
                
        return parent;
        }
                
                
    public EnvelopeDisplay buildLevelDisplay(int pos, boolean voiced, Color color)
        {
        EnvelopeDisplay parent = null;
        JComponent comp;
        ArrayList<EnvelopeDisplay> kids = new ArrayList<EnvelopeDisplay>();

        double[] widths = new double[256];
        for(int i = 1; i < widths.length; i++)
            widths[i] = 1.0 / (256 - 1);

        double[] heights2 = new double[256];
        for(int i = 0; i < heights2.length; i++)
            heights2[i] = 1.0 / 127;

        for(int o = 1; o <= 8; o++)
            {
            if (model.get("show" + o, 0) == 0 &&
                model.get("manipulate", 0) + 1 != o)
                continue;
                        
            String[] mods2 = new String[256];
            for(int i = 0; i < mods2.length; i++)
                {
                mods2[i] = "frame" + (i + pos) + (voiced ? "" : "un") + "voicedlevel" + o;
                }
            final int _o = o;

            comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), new String[256], mods2, widths, heights2)
                {
                // The mouseDown and mouseUp code here enables us to only do undo()
                // ONCE.
                public void mouseDown()
                    {
                    getUndo().push(getModel());
                    getUndo().setWillPush(false);
                    }
                
                public void mouseUp()
                    {
                    getUndo().setWillPush(true);
                    }
                        
                public void updateFromMouse(double x, double y, boolean continuation)
                    {
                    if (x < 0)
                        x = 0;
                    else if (x > 1)
                        x = 1.0;

                    if (y <= 0.0) y = 0.0;
                    if (y >= 1.0) y = 1.0;
                    int frame = (int)(x * (256.0 - 1) + 0.5);

                    double val = y * 127;
                                                                        
                    YamahaFS1RFseq.this.model.set("frame" + (frame + pos) + (voiced ? "" : "un") + "voicedlevel" + _o, (int)val);
                    }

                public void updateHighlightIndex(int index)
                    {
                    lastIndex = index;
                    if (pos == 1)
                        {
                        int track = model.get("manipulate") + 1;
                        int level = YamahaFS1RFseq.this.model.get("frame" + (index + 1) + (voiced ? "" : "un") + "voicedlevel" + track);
                        int freq = YamahaFS1RFseq.this.model.get("frame" + (index + 1) + (voiced ? "" : "un") + "voicedfrequency" + track);
                        posx1.setText("Frame " + (index + 1) + "    Track " + track + "    Level " + level);
                        posy1.setText("Raw Freq " + freq + "    Hz " + intToFrequencyString(freq, false));
                        }
                    else if (pos == 257)
                        {
                        int track = model.get("manipulate") + 1;
                        int level = YamahaFS1RFseq.this.model.get("frame" + (index + 257) + (voiced ? "" : "un") + "voicedlevel" + track);
                        int freq = YamahaFS1RFseq.this.model.get("frame" + (index + 257) + (voiced ? "" : "un") + "voicedfrequency" + track);
                        posx2.setText("Frame " + (index + 1) + "    Track " + track + "    Level " + level);
                        posy2.setText("Raw Freq " + freq + "    Hz " + intToFrequencyString(freq, false));
                        }
                    else    // (pos == 129)
                        {
                        int track = model.get("manipulate") + 1;
                        int level = YamahaFS1RFseq.this.model.get("frame" + (index + 129) + (voiced ? "" : "un") + "voicedlevel" + track);
                        int freq = YamahaFS1RFseq.this.model.get("frame" + (index + 129) + (voiced ? "" : "un") + "voicedfrequency" + track);
                        posx3.setText("Frame " + (index + 1) + "    Track " + track + "    Level " + level);
                        posy3.setText("Raw Freq " + freq + "    Hz " + intToFrequencyString(freq, false));
                        }
                    }
                                        
                public int highlightIndex(double x, double y, boolean continuation)
                    {
                    if (x < 0) x = 0;
                    if (x > 1.0) x = 1.0;
                    return (int)(x * (256.0 - 1) + 0.5);
                    }

                public int verticalBorderThickness() { return 4; }
                };
            ((EnvelopeDisplay)comp).addVerticalDivider(0.5);
            ((EnvelopeDisplay)comp).setPreferredHeight(60);

            if (model.get("manipulate", 0) + 1 == o)
                {
                parent = (EnvelopeDisplay)comp;
                }
            else
                {
                kids.add((EnvelopeDisplay)comp);
                }

            ((EnvelopeDisplay)comp).setFilled(false);
            }

        for(int i = 0; i < kids.size(); i++)
            {
            parent.link(kids.get(i));
            }
                
        return parent;
        }


    public JComponent addPitchDisplay(int pos, Color color)
        {
        Category category = new Category(this, "Pitch " + pos + " - " + (pos + 255), color);

        JComponent comp;
        String[] params;
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        
        EnvelopeDisplay parent = null;

        double[] widths = new double[256];
        for(int i = 1; i < widths.length; i++)
            widths[i] = 1.0 / (256 - 1);

        double[] heights2 = new double[256];
        for(int i = 0; i < heights2.length; i++)
            heights2[i] = 1.0 / 16383;

        String[] mods2 = new String[256];
        for(int i = 0; i < mods2.length; i++)
            {
            mods2[i] = "frame" + (i + pos) + "pitch";
            }

        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), new String[256], mods2, widths, heights2)
            {
            // The mouseDown and mouseUp code here enables us to only do undo()
            // ONCE.
            public void mouseDown()
                {
                getUndo().push(getModel());
                getUndo().setWillPush(false);
                }
                        
            public void mouseUp()
                {
                getUndo().setWillPush(true);
                }
                                        
            public void updateFromMouse(double x, double y, boolean continuation)
                {
                if (x < 0)
                    x = 0;
                else if (x > 1)
                    x = 1.0;

                if (y <= 0.0) y = 0.0;
                if (y >= 1.0) y = 1.0;
                int frame = (int)(x * (256.0 - 1) + 0.5);

                double val = y * 16383;
                                                                                                                                        
                YamahaFS1RFseq.this.model.set("frame" + (frame + pos) + "pitch", (int)val);
                }

            public void updateHighlightIndex(int index)
                {
                lastIndex = index;
                if (pos == 1)
                    {
                    int track = model.get("manipulate") + 1;
                    int pitch = YamahaFS1RFseq.this.model.get("frame" + (index + 1) + "pitch");
                    posx1.setText("Frame " + (index + 1));
                    posy1.setText("Raw Pitch " + pitch + "    Hz " + intToFrequencyString(pitch, false));
                    }
                else if (pos == 257)
                    {
                    int track = model.get("manipulate") + 1;
                    int pitch = YamahaFS1RFseq.this.model.get("frame" + (index + 257) + "pitch");
                    posx2.setText("Frame " + (index + 1));
                    posy2.setText("Raw Pitch " + pitch + "    Hz " + intToFrequencyString(pitch, false));
                    }
                else    // (pos == 129)
                    {
                    int track = model.get("manipulate") + 1;
                    int pitch = YamahaFS1RFseq.this.model.get("frame" + (index + 129) + "pitch");
                    posx3.setText("Frame " + (index + 1));
                    posy3.setText("Raw Pitch " + pitch + "    Hz " + intToFrequencyString(pitch, false));
                    }
                }
                                
            public int highlightIndex(double x, double y, boolean continuation)
                {
                if (x < 0) x = 0;
                if (x > 1.0) x = 1.0;
                return (int)(x * (256.0 - 1) + 0.5);
                }

            public int verticalBorderThickness() { return 4; }
            };
        ((EnvelopeDisplay)comp).addVerticalDivider(0.5);
        ((EnvelopeDisplay)comp).setPreferredHeight(60);
        ((EnvelopeDisplay)comp).setFilled(false);
        
        parent = (EnvelopeDisplay)comp;

        category.add(parent, BorderLayout.CENTER);
        return category;
        }

    public JComponent addFrameDisplay(int pos, boolean voiced, Color color)
        {
        Category category = new Category(this, (voiced ? "Voiced " : "Unvoiced ") + pos + " - " + (pos + 255), color);

        JComponent comp;
        String[] params;
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        
        JPanel panel = new JPanel();
        panel.setBackground(Style.BACKGROUND_COLOR());
        panel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.fill = c.BOTH;
        c.weightx = 1;
        c.weighty = 3;
        
        panel.add(buildFrequencyDisplay(pos, voiced, color), c);

        c.weighty = 1;
        c.gridy = 1;

        panel.add(buildLevelDisplay(pos, voiced, color), c);
            
        category.add(panel, BorderLayout.CENTER);
        return category;
        }
        
        
    public void rebuildFrameDisplays()
        {
        if (hi == null || lo == null || mid == null)  // not ready yet
            return;
                
        lo.removeAll();
        lo.add(addFrameDisplay(1, true, Style.COLOR_A()));
        lo.add(addFrameDisplay(1, false, Style.COLOR_B()));
        lo.revalidate();
        lo.repaint();
        mid.removeAll();
        mid.add(addFrameDisplay(129, true, Style.COLOR_A()));
        mid.add(addFrameDisplay(129, false, Style.COLOR_B()));
        mid.revalidate();
        mid.repaint();
        hi.removeAll();
        hi.add(addFrameDisplay(257, true, Style.COLOR_A()));
        hi.add(addFrameDisplay(257, false, Style.COLOR_B()));
        hi.revalidate();
        hi.repaint();
        
        // unregister the envelopes
        for(int op = 1; op <= 8; op++)
            {
            ArrayList listeners = model.getListeners("frame1voicedfrequency" + op);
            for(int i = listeners.size() - 1; i >= 0; i--)
                {
                if (listeners.get(i) instanceof EnvelopeDisplay)
                    listeners.remove(i);
                }
                        
            listeners = model.getListeners("frame1voicedlevel" + op);
            for(int i = listeners.size() - 1; i >= 0; i--)
                {
                if (listeners.get(i) instanceof EnvelopeDisplay)
                    listeners.remove(i);
                }
                        
            listeners = model.getListeners("frame257voicedfrequency" + op);
            for(int i = listeners.size() - 1; i >= 0; i--)
                {
                if (listeners.get(i) instanceof EnvelopeDisplay)
                    listeners.remove(i);
                }
                        
            listeners = model.getListeners("frame257voicedlevel" + op);
            for(int i = listeners.size() - 1; i >= 0; i--)
                {
                if (listeners.get(i) instanceof EnvelopeDisplay)
                    listeners.remove(i);
                }
            }
        }



    public JComponent addGlobal( Color color)
        {
        Category category = new Category(this, "Global", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
                
        params = FORMATS;
        comp = new Chooser("Format", this, "format", params);
        vbox.add(comp);
        
        hbox.add(vbox);
        vbox = new VBox();

        params = LOOP_MODES;
        comp = new Chooser("Loop Mode", this, "loopmode", params);
        vbox.add(comp);

        params = PITCH_MODES;
        comp = new Chooser("Pitch Mode", this, "pitchmode", params);
        vbox.add(comp);

        hbox.add(vbox);

        comp = new LabelledDial("Length", this, "endstep", color, 0, 511, -1);
        hbox.add(comp);

        comp = new LabelledDial("Loop", this, "loopstart", color, 0, 511, -1);
        ((LabelledDial)comp).addAdditionalLabel("Start");
        hbox.add(comp);

        comp = new LabelledDial("Loop", this, "loopend", color, 0, 511, -1);
        ((LabelledDial)comp).addAdditionalLabel("End");
        hbox.add(comp);
        
        comp = new LabelledDial("Speed", this, "speedadjust", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Adjust");
        hbox.add(comp);

        comp = new LabelledDial("Velocity Sens.", this, "velocityensitivityfortempo", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel(" For Tempo ");
        hbox.add(comp);

        comp = new LabelledDial("Note", this, "noteassign", color, 0, 127)
            {
            public String map(int val)
                {
                // not sure if this is right.  Needs to start at C-2
                return (NOTES[(val + 3) % 12] + (((val + 9) / 12) - 2));
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Pitch", this, "pitchtuning", color, 0, 126, 63)
            {
            public boolean isSymmetric() { return true; }
            };
        ((LabelledDial)comp).addAdditionalLabel("Tuning");
        hbox.add(comp);

        comp = new LabelledDial("Sequence", this, "sequencedelay", color, 0, 63);
        ((LabelledDial)comp).addAdditionalLabel("Delay");
        hbox.add(comp);
       
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public byte[] emit(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        if (tempModel == null)
            tempModel = getModel();
            
        int format = model.get("format");
        int byteCount = 32 + 50 * (format == 0 ? 128 : (format == 1 ? 256 : (format == 2 ? 384 : 512)));
        int val = 0;
        
        byte[] data = new byte[byteCount + 11];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x43;
        data[2] = (byte)(getID() - 1);
        data[3] = (byte)0x5E;
        data[4] = (byte)((byteCount >>> 7) & 127);
        data[5] = (byte)(byteCount & 127);
        data[6] = (byte)(toWorkingMemory ? 0x60 : 0x61);                // "current" vs "internal"
        data[7] = (byte)0x0;
        data[8] = (byte)(toWorkingMemory ? 0x00 :                               // sending to current working memory
                (toFile ? 0x00 :                                                                // writing a preset to a file, we should write it as Internal 00
                tempModel.get("number")));                                              // writing internal to a file, we can write it at its real number
        
        int pos = 9;
        
        String name = model.get("name", "INIT VOICE") + "          ";
        for(int i = 0; i < 8; i++)     
            {
            data[i + pos] = (byte)(name.charAt(i));
            }
        pos += 8;
        
        // "reserved"
        pos += 8;
        
        val = model.get("loopstart");
        data[pos] = (byte)((val >>> 7) & 127);
        data[pos+1] = (byte)(val & 127);
        pos += 2;
        
        val = model.get("loopend");
        data[pos] = (byte)((val >>> 7) & 127);
        data[pos+1] = (byte)(val & 127);
        pos += 2;
        
        val = model.get("loopmode");
        data[pos] = (byte)val;
        pos += 1;

        val = model.get("speedadjust");
        data[pos] = (byte)val;
        pos += 1;

        val = model.get("velocityensitivityfortempo");
        data[pos] = (byte)val;
        pos += 1;

        val = model.get("pitchmode");
        data[pos] = (byte)val;
        pos += 1;

        val = model.get("noteassign");
        data[pos] = (byte)val;
        pos += 1;

        val = model.get("pitchtuning");
        data[pos] = (byte)val;
        pos += 1;

        val = model.get("sequencedelay");
        data[pos] = (byte)val;
        pos += 1;

        val = model.get("format");
        data[pos] = (byte)val;
        pos += 1;
        
        // "reserved"
        pos += 2;

        val = model.get("endstep");
        data[pos] = (byte)((val >>> 7) & 127);
        data[pos+1] = (byte)(val & 127);
        pos += 2;
                
        for(int i = 0; i < (format + 1) * 128; i++)
            {
            val = model.get("frame" + (i + 1) + "pitch");
            data[pos] = (byte)((val >>> 7) & 127);
            data[pos+1] = (byte)(val & 127);
            pos += 2;
                        
            for(int j = 1; j <= 8; j++)
                {
                val = model.get("frame" + (i + 1) + "voicedfrequency" + j);
                data[pos] = (byte)((val >>> 7) & 127);
                data[pos+8] = (byte)(val & 127);
                pos += 1;
                }

            pos += 8;           // skip lsb
                                          
            for(int j = 1; j <= 8; j++)
                {
                // Thor says that this is inverted
                val = 127 - model.get("frame" + (i + 1) + "voicedlevel" + j);
                data[pos] = (byte)val;
                pos += 1;
                }

            for(int j = 1; j <= 8; j++)
                {
                val = model.get("frame" + (i + 1) + "unvoicedfrequency" + j);
                data[pos] = (byte)((val >>> 7) & 127);
                data[pos+8] = (byte)(val & 127);
                pos += 1;
                }

            pos += 8;           // skip lsb
                                          
            for(int j = 1; j <= 8; j++)
                {
                // Thor says that this is inverted
                val = 127 - model.get("frame" + (i + 1) + "unvoicedlevel" + j);
                data[pos] = (byte)val;
                pos += 1;
                }
            }
                
        data[data.length - 2] = produceChecksum(data, 4);
        data[data.length - 1] = (byte)0xF7;
        return data;
        }
    
    
    public int parse(byte[] data, boolean fromFile)
        {
        // attempt to load the bank / number
        int bank = data[4] - 0x60;
        int number = data[6];
        
        if (fromFile) // need to update bank/number
            {
            model.set("bank", 0);           // Internal
            if (number < 0) number = 0;
            if (number >= 6) number = 0;
            model.set("number", number);
            }
        
        int pos = 9;
                                
        char[] name = new char[8];
        for(int i = 0; i < 8; i++)
            {
            name[i] = (char)data[i + pos];
            model.set("name", new String(name));
            }
        pos += 8;
        
        // "reserved"
        pos += 8;
        
        model.set("loopstart", (data[pos] << 7) | data[pos+1]);
        pos += 2;
        
        model.set("loopend", (data[pos] << 7) | data[pos+1]);
        pos += 2;
        
        model.set("loopmode", data[pos]);
        pos += 1;

        model.set("speedadjust", data[pos]);
        pos += 1;

        model.set("velocityensitivityfortempo", data[pos]);
        pos += 1;

        model.set("pitchmode", data[pos]);
        pos += 1;

        model.set("noteassign", data[pos]);
        pos += 1;

        model.set("pitchtuning", data[pos]);
        pos += 1;

        model.set("sequencedelay", data[pos]);
        pos += 1;

        int format = data[pos];
        model.set("format", format);
        pos += 1;
        
        // "reserved"
        pos += 2;

        model.set("endstep", (data[pos] << 7) | data[pos+1]);
        pos += 2;
        
        //// Clear frames first
        for(int i = 0; i < 512; i++)
            {
            model.set("frame" + (i + 1) + "pitch", 0);
            for(int j = 1; j <= 8; j++)
                {
                model.set("frame" + (i + 1) + "voicedfrequency" + j, 0);
                }
                        
            for(int j = 1; j <= 8; j++)
                {
                model.set("frame" + (i + 1) + "voicedlevel" + j, 0);
                }

            for(int j = 1; j <= 8; j++)
                {
                model.set("frame" + (i + 1) + "unvoicedfrequency" + j, 0);
                }
                        
            for(int j = 1; j <= 8; j++)
                {
                model.set("frame" + (i + 1) + "unvoicedlevel" + j, 0);
                }
            }

        // Now load
        for(int i = 0; i < (format + 1) * 128; i++)
            {
            if (pos >= data.length - 2) // uh, 
                {
                System.err.println("Warning: truncated Fseq file");
                break;
                }
            model.set("frame" + (i + 1) + "pitch", (data[pos] << 7) | data[pos+1]);
            pos += 2;
                        
            for(int j = 1; j <= 8; j++)
                {
                model.set("frame" + (i + 1) + "voicedfrequency" + j, (data[pos] << 7) | data[pos+8]);
                pos += 1;
                }
            
            pos += 8;           // skip lsb
                  
            for(int j = 1; j <= 8; j++)
                {
                // Thor says that this is inverted
                model.set("frame" + (i + 1) + "voicedlevel" + j, 127 - data[pos]);
                pos += 1;
                }

            for(int j = 1; j <= 8; j++)
                {
                model.set("frame" + (i + 1) + "unvoicedfrequency" + j, (data[pos] << 7) | data[pos+8]);
                pos += 1;
                }
                        
            pos += 8;           // skip lsb
                  
            for(int j = 1; j <= 8; j++)
                {
                // Thor says that this is inverted
                model.set("frame" + (i + 1) + "unvoicedlevel" + j, 127 - data[pos]);
                pos += 1;
                }
            }
        
        if (pos != data.length - 2)
            {
            System.err.println("Warning: overlong Fseq file");
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

        // We ALWAYS change the patch no matter what.  We have to.  We have to force it for merging
        changePatch(tempModel);
        tryToSendSysex(requestDump(tempModel));
        }

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
            (byte)(0x60),
            0, 
            0, 
            (byte)0xF7
            };
        }

    public static boolean recognize(byte[] data)
        {
        return ((
                (data.length == 11 + 32 + 50 * 128) ||
            (data.length == 11 + 32 + 50 * 256) ||
            (data.length == 11 + 32 + 50 * 384) ||
            (data.length == 11 + 32 + 50 * 512)) &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x43 &&
            data[3] == (byte)0x5E);
        }
               
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
        
    public static String getSynthName() { return "Yamaha FS1R [Fseq]"; }

    public void changePatch(Model tempModel) 
        {
        // bank
        tryToSendSysex(new byte[] { (byte)0xf0, 0x43, (byte)(16 + (getID() - 1)), 0x5e, 0x10, 0x00, 0x16, 0x00, (byte)tempModel.get("bank"), (byte)0xf7});

        // number
        tryToSendSysex(new byte[] { (byte)0xf0, 0x43, (byte)(16 + (getID() - 1)), 0x5e, 0x10, 0x00, 0x17, 0x00, (byte)tempModel.get("number"), (byte)0xf7});
        
        // we assume that we successfully did it
        if (!isMerging())  // we're actually loading the patch, not merging with it
            {
            setSendMIDI(false);
            model.set("number", tempModel.get("number"));
            model.set("bank", tempModel.get("bank"));
            setSendMIDI(true);
            }
        }
    
    public String getPatchName(Model model) { return model.get("name", "Init"); }

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
        return (BANKS[model.get("bank")]) + 
            (number > 99 ? "" : (number > 9 ? "0" : "00")) + number;
        }
        

    public boolean testVerify(Synth synth2, 
        String key,
        Object obj1, Object obj2) 
        {
        if (key.startsWith("frame0"))  // our throwaway
            return true;
        
        if (key.startsWith("frame"))
            {
            int numFrames = (synth2.getModel().get("format", 0) + 1) * 128;
            int frame = StringUtility.getFirstInt(key);
            if (frame > numFrames)  // we're all done!
                return true;
            }
        
        return false;
        }

    // Writing takes a while to process.  However the FS1R has a huge buffer and can handle an entire
    // bank's worth of writes -- but then it's very slow to process through all of them, constantly displaying
    // "Bulk Received".  With about a 170ms delay or so, this message disappears right when Edisyn finishes,
    // so it's a good compromise from a UI standpoint.
    public int getPauseAfterWritePatch() { return 170; }            // don't know if we need any


    // The FS1R is VERY slow to respond and also queues up responses (beware!)
    public int getBulkDownloadWaitTime() { return 3000; }
    public int getBulkDownloadFailureCountdown() { return 5; }
    }


class ScrollableVBox extends VBox implements javax.swing.Scrollable
    {
    public Dimension getPreferredScrollableViewportSize()
        {
        return null;
        }
                
    // for now we're not doing a snap to the nearest category
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
        {
        if (orientation == SwingConstants.VERTICAL)
            return 1;
        else
            return 1;
        }

    public boolean getScrollableTracksViewportHeight()
        {
        return false;
        }

    public boolean getScrollableTracksViewportWidth()
        {
        return true;
        }
        
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction)
        {
        return 1;
        }
    }





