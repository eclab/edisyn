/***
    Copyright 2021 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.waldorfrocket;

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
   A patch editor for the Waldorf Rocket.
        
   @author Sean Luke
*/

public class WaldorfRocket extends Synth
{
    public static final String[] OSC_SHAPES = { "Saw", "Pulse" };
    public static final String[] KEYTRACKS = { "Off", "Half", "Full" };
    public static final String[] LFO_SHAPES = { "Saw Down", "Triangle", "Pulse" };
    public static final String[] LFO_TARGETS = { "Arpeggiator", "Cutoff (VCF)", "Pitch (Osc)" };
    public static final String[] ARP_DIRECTIONS = { "Rand", "Alt", "Up" };
    public static final String[] CC_MAPS = { "Default", "Alternate" };
    public static final String[] CHANNELS = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16" };
    public static final String[] INTERVALS = { "III", "IV", "V", "Oct", "OctIII", "OctIV", "OctV", "2 Oct" };
    public static final String[] CHORDS = { "A", "B", "C", "D", "E", "F", "G", "H" };
    public static final String[] OCTAVES = { "1", "2", "2", "3", "3", "4", "4", "4" };              // weird
        
    final boolean altMap = false;           // always, due to the Waldorf bug
        
    public WaldorfRocket()
    {
        // altMap = getLastXAsBoolean("altMap", getSynthName(), false, true);

        for(int i = 0; i < parameters.length; i++)
            {
                parametersToIndex.put(parameters[i], Integer.valueOf(i));
            }
                                

        /// SOUND PANEL
                
        JComponent soundPanel = new SynthPanel(this);
        VBox vbox = new VBox();
        
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        hbox.addLast(addOscillator(Style.COLOR_A()));
        vbox.add(hbox);

        hbox = new HBox();
        hbox.add(addFilter(Style.COLOR_A()));
        hbox.addLast(addEnvelope(Style.COLOR_B()));
        vbox.add(hbox);

        hbox = new HBox();
        hbox.add(addLFO(Style.COLOR_B()));
        hbox.addLast(addArpeggiator(Style.COLOR_A()));
        vbox.add(hbox);

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Rocket", soundPanel);
                
        loadDefaults();        
    }
                
    public JFrame sprout()     
    {
        JFrame frame = super.sprout();
        receivePatch.setEnabled(false);
        blend.setEnabled(false);
        merge.setEnabled(false);
        writeTo.setEnabled(false);
        getAll.setEnabled(false);                       // this is turned off anyway
        addRocketMenu();
        return frame;
    }

    public void addRocketMenu()
    {
        JMenu menu = new JMenu("Rocket");
        menubar.add(menu);
        JMenuItem setMIDIChannelsMenu = new JMenuItem("Set MIDI Channels...");
        setMIDIChannelsMenu.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    doChannels();
                }
            });
        menu.add(setMIDIChannelsMenu);
        JMenuItem setDefaultMapMenu = new JMenuItem("Set to Default CC Map");
        setDefaultMapMenu.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    doCCMap();
                }
            });
        menu.add(setDefaultMapMenu);
    }

    public String getDefaultResourceFileName() { return "WaldorfRocket.init"; }
    public String getHTMLResourceFileName() { return "WaldorfRocket.html"; }

    public boolean gatherPatchInfo(String title, Model change, boolean writing)
    {
        return true;
    }
                                  
    /** Add the global patch category (name, id, number, etc.) */
    public JComponent addNameGlobal(Color color)
    {
        Category globalCategory = new Category(this, getSynthName(), color);
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        /*final JCheckBox check = new JCheckBox("Alt CC Map");
          check.setSelected(altMap);
          check.addActionListener(new ActionListener()
          {
          public void actionPerformed(ActionEvent e)
          {
          altMap = check.isSelected();
          setLastX("" + (altMap ? true : false), "altMap", getSynthName(), true);
          tryToSendSysex(new byte[]
          {
          (byte)0xF0, (byte)0x3E, (byte)0x17, (byte)0x00, 
          (byte)0x24, (byte)0x02, (byte)(altMap ? 1 :0), (byte)0xF7
          });
          }
          });
          check.setFont(Style.SMALL_FONT());
          check.setOpaque(false);
          check.setForeground(Style.TEXT_COLOR());
          vbox.add(check);
          hbox.add(vbox);
        */
                
        comp = new LabelledDial("Semitone", this, "semitone", color, 52, 76, 64);
        hbox.add(comp);

        comp = new LabelledDial("Detune", this, "detune", color, 0, 127, 64);
        hbox.add(comp);

        comp = new LabelledDial("Glide", this, "glide", color, 0, 127);
        hbox.add(comp);
        
        globalCategory.add(hbox, BorderLayout.CENTER);
        return globalCategory;
    }
               
    JLabel waveLabel = null; 
    JLabel tuneLabel = null; 
    public JComponent addOscillator(Color color)
    {
        Category category = new Category(this, "Oscillator", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = OSC_SHAPES;
        comp = new Chooser("Shape", this, "oscshape", params)
            {
                public void update(String key, Model model)
                {
                    super.update(key, model);
                    boolean m = getSendMIDI();
                    setSendMIDI(false);
                    model.set("oscwave", model.get("oscwave", 0));
                    setSendMIDI(m);
                }
            };
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("     Wave      ", this, "oscwave", color, 0, 127)
            {
                public boolean isSymmetric() { return true; }
                public void update(String key, Model model)
                {
                    super.update(key, model);
                    int value = model.get(key);
                    if (model.get("oscshape") == 0)         // saw
                        {
                            if (value < 64)
                                {
                                    if (waveLabel != null) waveLabel.setText(" Sync Env ");
                                }
                            else
                                {
                                    if (waveLabel != null) waveLabel.setText("Poly Saw");
                                }
                        }
                    else
                        {
                            if (value <= 64)
                                {
                                    if (waveLabel != null) waveLabel.setText("Pulse Width");
                                }
                            else if (value <= 64 + 14)      // dunno why 14 but it appears to be so
                                {
                                    if (waveLabel != null) waveLabel.setText("PWM Depth");
                                }
                            else
                                {
                                    if (waveLabel != null) waveLabel.setText("PWM Speed");
                                }
                        }
                    boolean m = getSendMIDI();
                    setSendMIDI(false);
                    model.set("osctune", model.get("osctune", 0));
                    setSendMIDI(m);
                }
                
                public String map(int value)
                {
                    if (model.get("oscshape") == 0)         // saw
                        {
                            if (value == 0) 
                                {
                                    return "Inf";
                                }
                            else if (value < 64)
                                {
                                    return "E " + (64 - value);
                                }
                            else
                                {
                                    return "N " +  ((value - 64) / 8);
                                }
                        }
                    else
                        {
                            if (value < 64)
                                {
                                    return "P " + value;
                                }
                            else if (value <= 64 + 14)      // dunno why 14 but it appears to be so
                                {
                                    return "D " + (value - 64);
                                }
                            else
                                {
                                    return "S " + (value - (64 + 14));
                                }
                        }
                }
            };
        waveLabel = ((LabelledDial)comp).addAdditionalLabel(" Sync Env  ");
        hbox.add(comp);

        comp = new LabelledDial("     Tune      ", this, "osctune", color, 0, 127)
            {
                public boolean isSymmetric() { return true; }
                public void update(String key, Model model)
                {
                    super.update(key, model);
                    int waveValue = model.get("oscwave");
                    int value = model.get(key);
                    if (model.get("oscshape") == 0)         // saw
                        {
                            if (waveValue < 64)
                                {
                                    if (tuneLabel != null) tuneLabel.setText("Slave Freq");
                                }
                            else
                                {
                                    if (value > 64)
                                        {
                                            if (value == 127 && waveValue == 127)
                                                {
                                                    if (tuneLabel != null) tuneLabel.setText("Unison");
                                                }
                                            else
                                                {
                                                    if (tuneLabel != null) tuneLabel.setText("Chord");
                                                }
                                        }
                                    else
                                        {
                                            if (tuneLabel != null) tuneLabel.setText("Poly Detune");
                                        }
                                }
                        }
                    else
                        {
                            if (value <= 64)
                                {
                                    if (tuneLabel != null) tuneLabel.setText("Detune");
                                }
                            else
                                {
                                    if (tuneLabel != null) tuneLabel.setText("Interval");
                                }
                        }
                }
                
                public String map(int value)
                {
                    int waveValue = model.get("oscwave");
                    if (model.get("oscshape") == 0)         // Saw
                        {
                            if (waveValue < 64)
                                {
                                    return "F " + value;
                                }
                            else
                                {
                                    if (value > 64)
                                        {
                                            if (value == 127 && waveValue == 127)
                                                {
                                                    return "Uni"; 
                                                }
                                            else
                                                {
                                                    return CHORDS[(value - 64) / 8];
                                                }
                                        }
                                    else
                                        {
                                            return "D " + value;
                                        }
                                }
                        }
                    else                                                            // Pulse
                        {
                            if (value <= 64)
                                {
                                    return "D " + value;
                                }
                            else
                                {
                                    return INTERVALS[(value - 64) / 8];
                                }
                        }
                }
            };
        tuneLabel = ((LabelledDial)comp).addAdditionalLabel("Sync Slave");
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
    }


    public JComponent addFilter(Color color)
    {
        Category category = new Category(this, "Filter", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = KEYTRACKS;
        comp = new Chooser("Keytrack", this, "keytrack", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Cutoff", this, "cutoff", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Resonance", this, "resonance", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Envelope", this, "envmod", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Mod");
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
    }

    JLabel depthlabel = null;
    public JComponent addLFO(Color color)
    {
        Category category = new Category(this, "LFO", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = LFO_SHAPES;
        comp = new Chooser("Shape", this, "lfoshape", params);
        vbox.add(comp);
        
        params = LFO_TARGETS;
        comp = new Chooser("Target", this, "lfotarget", params)
            {
                public void update(String key, Model model)
                {
                    super.update(key, model);
                    boolean m = getSendMIDI();
                    setSendMIDI(false);
                    model.set("lfodepth", model.get("lfodepth", 0));
                    model.set("arprange", model.get("arprange", 0));
                    setSendMIDI(m);
                }
            };
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Depth", this, "lfodepth", color, 0, 127)
            {
                public boolean isSymmetric() { return true; }
                public void update(String key, Model model)
                {
                    super.update(key, model);
                    int value = model.get(key);
                    int target = model.get("lfotarget");
                    if (target == 0)                // arp
                        {
                            if (depthlabel != null) depthlabel.setText("LFO Depth");
                        }
                    else if (target == 1)   // cutoff
                        {
                            if (depthlabel != null) depthlabel.setText("LFO Depth");
                        }
                    else
                        {
                            if (value < 64)
                                {
                                    if (depthlabel != null) depthlabel.setText("LFO Depth");
                                }
                            else
                                {
                                    if (depthlabel != null) depthlabel.setText("Semitones");
                                }                                       
                        }
                }

                public String map(int value)
                {
                    int target = model.get("lfotarget");
                    if (target == 0)                // arp
                        {
                            return "D " + value;
                        }
                    else if (target == 1)   // cutoff
                        {
                            return "D " + value;
                        }
                    else
                        {
                            if (value < 64)
                                {
                                    return "D " + value;
                                }
                            else
                                {
                                    return "S " + ((value - 64) / 2 + 1);
                                }                                       
                        }
                }
            };
        depthlabel = ((LabelledDial)comp).addAdditionalLabel("Octaves");
        hbox.add(comp);

        comp = new LabelledDial("Speed", this, "lfospeed", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Vibrato", this, "vibratomodlfo", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Mod");
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
    }


    public JComponent addEnvelope(Color color)
    {
        Category category = new Category(this, "Envelope", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        comp = new CheckBox("Sustain", this, "envsustain");
        vbox.add(comp);
        comp = new CheckBox("Release", this, "envrelease");
        vbox.add(comp);
        hbox.add(vbox);


        comp = new LabelledDial("Decay", this, "envdecay", color, 0, 127);
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
    }

    JLabel rangeLabel = null;
    public JComponent addArpeggiator(Color color)
    {
        Category category = new Category(this, "Arpeggiator", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = ARP_DIRECTIONS;
        comp = new Chooser("Direction", this, "arpdirection", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Tempo", this, "arptempo", color, 0, 127);
        hbox.add(comp);
        
        comp = new LabelledDial("Range", this, "arprange", color, 0, 127)
            {
                public boolean isSymmetric() { return true; }
                public void update(String key, Model model)
                {
                    super.update(key, model);
                    int value = model.get(key);
                    if (value < 63)                 // yes, *63*
                        {
                            if (rangeLabel != null) rangeLabel.setText("Octaves");
                        }
                    else
                        {
                            if (rangeLabel != null) rangeLabel.setText("Patterns");
                        }                                       
                }

                public String map(int value)
                {
                    if (value < 63)                 // yes, *63*
                        {
                            return "O " + OCTAVES[value / 8];
                        }
                    // The pattern divisions are kind of arbitrary. 
                    else if (value < 73)
                        {
                            return "P 1";
                        }
                    else if (value < 82)
                        {
                            return "P 2";
                        }
                    else if (value < 91)
                        {
                            return "P 3";
                        }
                    else if (value < 100)
                        {
                            return "P 4";
                        }
                    else if (value < 109)
                        {
                            return "P 5";
                        }
                    else if (value < 118)
                        {
                            return "P 6";
                        }
                    else if (value < 127)
                        {
                            return "P 7";
                        }
                    else            // value == 127
                        {
                            return "P 8";
                        }
                }
            };
        rangeLabel = ((LabelledDial)comp).addAdditionalLabel("Octaves");
        hbox.add(comp);


        category.add(hbox, BorderLayout.CENTER);
        return category;
    }

    public void doChannels()
    {
        JComboBox receive = new JComboBox(CHANNELS);
        receive.setSelectedIndex(getChannelOut());
        JComboBox transmit = new JComboBox(CHANNELS);
        transmit.setSelectedIndex(getChannelOut());
        int result = showMultiOption(this, new String[] { "Receive", "Transmit" }, new JComponent[] { receive, transmit },
                                     new String[] { "Set", "Cancel" }, 0, 
                                     "Set Channels", new JLabel("<html>Set the Rocket to use which MIDI Channels?<br>Receive is the channel on which the Rocket receives MIDI.<br>Transmit is the channel on which the Rocket transmits MIDI.<br><br><i>Note:</i> To communicate with Edisyn, the channels must <i>both</i><br>be the same as Edisyn's MIDI channel.</html>"));

        if (result == 0)
            {
                // receive
                tryToSendSysex(new byte[]
                    {
                        (byte)0xF0, (byte)0x3E, (byte)0x17, (byte)0x00, 
                        (byte)0x24, (byte)0x00, (byte)receive.getSelectedIndex(), (byte)0xF7
                    });
                // transmit
                tryToSendSysex(new byte[]
                    {
                        (byte)0xF0, (byte)0x3E, (byte)0x17, (byte)0x00, 
                        (byte)0x24, (byte)0x01, (byte)transmit.getSelectedIndex(), (byte)0xF7
                    });
            }
    }


    public void doCCMap()
    {
        tryToSendSysex(new byte[]
            {
                (byte)0xF0, (byte)0x3E, (byte)0x17, (byte)0x00, 
                (byte)0x24, (byte)0x02, (byte)0x00, (byte)0xF7
            });
    }


    public String getPatchName(Model model) { return "Rocket"; }

    public byte[] requestCurrentDump()
    {
        byte[] data = new byte[6];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x3E;
        data[2] = (byte)0x17;
        data[3] = (byte)0x00;
        data[4] = (byte)0x50;
        data[5] = (byte)0xF7;
        return data;
    }

    public boolean getSendsAllParametersAsDump() { return false; }

    public void handleSynthCCOrNRPN(Midi.CCData data)
    {
        if (data.channel == getChannelOut() && data.type == Midi.CCDATA_TYPE_RAW_CC)
            {
                int param = ccToParam(data.number, altMap ? 1 : 0);
                int val = data.value;
                if (param >= 0)
                    {
                        model.set(parameters[param], val);
                    }
            }
    }

    public Object[] emitAll(String key)
    {
        int param = ((Integer)parametersToIndex.get(key)).intValue();
        int val = model.get(key, 0);
        return buildCC(getChannelOut(), ccs[param][altMap ? 1 : 0], val);
    }

    public byte[] emit(Model tempModel, boolean toWorkingMemory, boolean toFile)
    {
        if (tempModel == null)
            tempModel = getModel();

        final int HEADER = WaldorfRocketRec.HEADER;             // 16

        byte[] sysex = new byte[parameters.length + HEADER + 1];
        sysex[0] = (byte)0xF0;
        sysex[1] = (byte)0x7D;
        sysex[2] = (byte)'E';
        sysex[3] = (byte)'D';
        sysex[4] = (byte)'I';
        sysex[5] = (byte)'S';
        sysex[6] = (byte)'Y';
        sysex[7] = (byte)'N';
        sysex[8] = (byte)'-';
        sysex[9] = (byte)'R';
        sysex[10] = (byte)'O';
        sysex[11] = (byte)'C';
        sysex[12] = (byte)'K';
        sysex[13] = (byte)'E';
        sysex[14] = (byte)'T';
        sysex[15] = (byte)0;                    // sysex version currently 0
        
        for(int i = 0; i < parameters.length ; i++)
            {
                sysex[HEADER + i] = (byte)(model.get(parameters[i]));
            }
        sysex[sysex.length - 1] = (byte)0xF7;
        return sysex;
    }

    public int parse(byte[] data, boolean fromFile)
    {
        final int HEADER = WaldorfRocketRec.HEADER;             // 16

        if (data[15] == 0)      // I only know how to handle version 0
            {
                for(int i = 0; i < parameters.length ; i++)
                    {
                        model.set(parameters[i], data[HEADER + i]);
                    }
            }
        return PARSE_SUCCEEDED;     
    }

    public static String getSynthName() { return "Waldorf Rocket"; }
    
    HashMap parametersToIndex = new HashMap();
    public static final String[] parameters = new String[] 
        {
            "oscwave",
            "osctune",
            "oscshape",
            "cutoff",
            "resonance",
            "envmod",
            "keytrack",
            "lfospeed",
            "lfodepth",
            "lfoshape",
            "vibratomodlfo",
            "lfotarget",
            "envdecay",
            "envsustain",
            "envrelease",
            "glide",
            "arptempo",
            "arprange",
            "arpdirection",
            "semitone",
            "detune",
        };

    // Yes, it's O(n) but it's a small list, likely faster
    // than using a hash table
    public int ccToParam(int cc, int alt)
    {
        for(int i = 0; i < ccs.length; i++)
            {
                if (ccs[i][alt] == cc)
                    return i;
            }
        return -1;
    }
                
    public static final int[][] ccs = new int[][] 
        {
            { 70  , 33  },
            { 79  , 29  },          //  Note that last cc is ALSO 29.  This is a Waldorf Rocket bug.
            { 31  , 31  },
            { 74  , 69  },
            { 71  , 70  },
            { 73  , 73  },
            { 83  , 72  },
            { 76  , 16  },
            { 77  , 50  },
            { 78  , 15  },
            { 80  , 18  },
            { 18  , 80  },
            { 75  , 102 },
            { 103 , 103 },
            { 72  , 106 },
            { 5   , 5   },
            { 14  , 14  },
            { 12  , 12  },
            { 13  , 13  },
            { 28  , 28  },          // probably ranges 52...76 corresponding to -12...+12 (like Blofeld)
            { 29  , 29  },          // probably ranges 0...127 corresponding to -64...+64 (like Blofeld)
        };

    
}
    
