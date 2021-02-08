/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.yamahatg33;

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
   A patch editor for the Yamaha TG33.
        
   @author Sean Luke
*/

public class YamahaTG33 extends Synth
    {
    /// Various collections of parameter names for pop-up menus
    
    public static final int TYPE_TG33 = 0;
    public static final int TYPE_SY22 = 1;
    public static final int TYPE_SY35 = 2;
    public static final String[] TYPES = { "TG33", "SY22", "SY35" };


    public final byte VOICE_BUTTON = 0x06;
    public final byte MULTI_BUTTON = 0x07;

    public static final String[] BANKS = new String[] { "I", "P1", "P2", "C1", "C2" };

    public static final String[] RATES = new String[]
    { "1", "2", "3", "4", "5", "6", "7", "8" };
    public static final ImageIcon[] RATE_ICONS = 
        {
        new ImageIcon(YamahaTG33.class.getResource("Rate1.png")),
        new ImageIcon(YamahaTG33.class.getResource("Rate2.png")),
        new ImageIcon(YamahaTG33.class.getResource("Rate3.png")),
        new ImageIcon(YamahaTG33.class.getResource("Rate4.png")),
        new ImageIcon(YamahaTG33.class.getResource("Rate5.png")),
        new ImageIcon(YamahaTG33.class.getResource("Rate6.png")),
        new ImageIcon(YamahaTG33.class.getResource("Rate7.png")),
        new ImageIcon(YamahaTG33.class.getResource("Rate8.png")),
        };

    public static final String[] LEVELS = new String[]
    { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16" };

    public static final String[] ALGORITHMS = new String[] 
    { "M->C->", "M,C ->" };
                
    public static final ImageIcon[] LEVEL_ICONS = 
        {
        new ImageIcon(YamahaTG33.class.getResource("Level1.png")),
        new ImageIcon(YamahaTG33.class.getResource("Level2.png")),
        new ImageIcon(YamahaTG33.class.getResource("Level3.png")),
        new ImageIcon(YamahaTG33.class.getResource("Level4.png")),
        new ImageIcon(YamahaTG33.class.getResource("Level5.png")),
        new ImageIcon(YamahaTG33.class.getResource("Level6.png")),
        new ImageIcon(YamahaTG33.class.getResource("Level7.png")),
        new ImageIcon(YamahaTG33.class.getResource("Level8.png")),
        new ImageIcon(YamahaTG33.class.getResource("Level9.png")),
        new ImageIcon(YamahaTG33.class.getResource("Level10.png")),
        new ImageIcon(YamahaTG33.class.getResource("Level11.png")),
        new ImageIcon(YamahaTG33.class.getResource("Level12.png")),
        new ImageIcon(YamahaTG33.class.getResource("Level13.png")),
        new ImageIcon(YamahaTG33.class.getResource("Level14.png")),
        new ImageIcon(YamahaTG33.class.getResource("Level15.png")),
        new ImageIcon(YamahaTG33.class.getResource("Level16.png")),
        };

    public static final String[] WAVES = {"W1", "W2", "W3", "W4", "W5", "W6", "W7", "W8"};
    public static final ImageIcon[] WAVE_ICONS = 
        {
        new ImageIcon(YamahaTG33.class.getResource("Wave1.png")),
        new ImageIcon(YamahaTG33.class.getResource("Wave2.png")),
        new ImageIcon(YamahaTG33.class.getResource("Wave3.png")),
        new ImageIcon(YamahaTG33.class.getResource("Wave4.png")),
        new ImageIcon(YamahaTG33.class.getResource("Wave5.png")),
        new ImageIcon(YamahaTG33.class.getResource("Wave6.png")),
        new ImageIcon(YamahaTG33.class.getResource("Wave7.png")),
        new ImageIcon(YamahaTG33.class.getResource("Wave8.png"))
        };
        
    public static final String[] LFO_TYPES = new String[] { "Saw Down", "Triangle", "Square", "Sample & Hold", "Saw Up" };
    public static final String[] ENVELOPE_TYPES = new String[] { "User", "Preset", "Piano", "Guitar", "Pluck", "Brass", "Strings", "Organ" };
        
    public static final String[] AWM_WAVES = new String[] 
    {
    "Piano", "E.piano", "Clavi", "Cembalo", "Celeste", "P.organ", "E.organ1", "E.organ2", 
    "Reed", "Trumpet", "Mute Trp", "Trombone", "Flugel", "Fr horn", "BrasAtak", "SynBrass", 
    "Flute", "Clarinet", "Oboe", "Sax", "Gut", "Steel", "E.Gtr 1", "E.Gtr 2", "Mute Gtr", 
    "Sitar", "Pluck 1", "Pluck 2", "Wood B 1", "Wood B 2", "E.Bass 1", "E.Bass 2", 
    "E.Bass 3", "E.Bass 4", "Slap", "Fretless", "SynBass1", "SynBass2", "Strings", 
    "Vn.Ens.", "Cello", "Pizz.", "Syn Str", "Choir", "Itopia", "Ooo!", "Vibes", "Marimba", 
    "Bells", "Timapi", "Tom", "E. Tom", "Cuica", "Whistle", "Claps", "Hit", "Harmonic", 
    "Mix", "Sync", "Bell Mix", "Styroll", "DigiAtak", "Noise 1", "Noise 2", "Oh Hit", 
    "Water 1", "Water 2", "Stream", "Coin", "Crash", "Bottle", "Tear", "Cracker", 
    "Scratch", "Metal 1", "Metal 2", "Metal 3", "Metal 4", "Wood", "Bamboo", "Slam", 
    "Tp. Body", "Tb. Body", "HornBody", "Fl. Body", "Str.Body", "AirBlown", "Reverse1", 
    "Reverse2", "Reverse3", "EP wv", "Organ wv", "M.Tp wv", "Gtr wv", "Str wv 1", 
    "Str wv 2", "Pad wv", "Digital1", "Digital2", "Digital3", "Digital4", "Digital5", 
    "Saw 1", "Saw 2", "Saw 3", "Saw 4", "Square 1", "Square 2", "Square 3", "Square 4", 
    "Pulse 1", "Pulse 2", "Pulse 3", "Pulse 4", "Pulse 5", "Pulse 6", "Tri", "Sin8'", 
    "Sin8'+4'", "SEQ 1", "SEQ 2", "SEQ 3", "SEQ 4", "SEQ 5", "SEQ 6", "SEQ 7", "SEQ 8", "Drum set"
    };

//// From original code in file sy_names.c,
//// by R. P. Hanson, at http://storage.atari-source.org/atari/mirrors/ATARI-MIDI-ARCHIVES/SOUNDS/Yamaha/
////
//// My deep thanks to Rich Hanson, who allowed me to use this code as public domain and offered
//// some tips about the TG33 implementation.

    public static final String[] SY35_AWM_WAVES = new String[] {
        "Piano", "E Piano", "Clavi", "Cembalo", "Celesta",
        "P.Organ", "E.Organ1", "E.Organ2", "Bandneon", "Trumpet",
        "Mute Trp", "Trombone", "Flugel", "Fr. Horn", "Bras Ens",
        "SynBrass", "Flute", "Clarinet", "Oboe", "Sax",
        "Gut", "Steel", "E.Gtr 1", "E.Gtr 2", "Mute Gtr",
        "Sitar", "Pluck 1", "Pluck 2", "Wood B 1", "Wood B 2",
        "E Bass 1", "E Bass 2", "E Bass 3", "E Bass 4", "Slap",
        "Fretless", "SynBass1", "SynBass2", "Strings", "Vn Ens",
        "Cello", "Pizz", "Syn Str", "Choir", "Itopia",
        "Choir pa", "Vibes", "Marimba", "Bells", "Timpani",
        "Tom", "E.Tom", "Cuica", "Whistle", "ThumbStr",
        "Syn Pad", "Harmonic", "SynLead1", "SynLead2", "Bell Mix",
        "Sweep", "HumanAtk", "Noise 1", "Noise 2", "Pops Hit",
        "NoisPad1", "NoisPad2", "NoisPad3", "Coin", "Crash",
        "Bottle", "BottlOpn", "Cracker", "Scratch", "Metal 1",
        "Metal 2", "Metal 3", "Metal 4", "Wood", "Bamboo",
        "Slam", "Tp. Body", "Tb. Body", "HornBody", "Fl.Body",
        "Str.Body", "AirBlown", "Reverse1", "Reverse2", "Reverse3",
        "EP wv", "Organ wv", "M.Tp wv", "Gtr wv", "Str wv 1",
        "Str wv 2", "Pad wv", "Digital1", "Digital2", "Digital3",
        "Digital4", "Digital5", "Saw 1", "Saw 2", "Saw 3",
        "Saw 4", "Square 1", "Square 2", "Square 3", "Square 4",
        "Pulse 1", "Pulse 2", "Pulse 3", "Pulse 4", "Pulse 5",
        "Pulse 6", "Triangle", "Sin 8'", "Sin8'+4'", "SEQ 1",
        "SEQ 1", "SEQ 2", "SEQ 3", "SEQ 4", "SEQ 5",
        "SEQ 6", "SEQ 7", "Drum Set" } ;

    public static final String[] FM_WAVES = new String[] 
    {
    "E.Piano1", "E.Piano2", "E.Piano3", "E.Plano4", "E.Piano5", "E.Piano6", "E.organ1", "E.organ2", 
    "E.organ3", "E.organ4", "E.organS", "E.organ6", "E.organ7", "E.organ8", "Brass 1", "Brass 2", 
    "Brass 3", "Brass 4", "Brass 5", "Brass 6", "Brass 7", "Brass 8", "Brass 9", "Brass 10", 
    "Brass 11", "Brass 12", "Brass 13", "Brass 14", "Wood 1", "Wood 2", "Wood 3", "Wood 4", "Wood 5", 
    "Wood 6", "Wood 7", "Wood 8", "Reed 1", "Reed 2", "Reed 3", "Reed 4", "Reed 5", "Reed 6", 
    "Clavi 1", "Clavi 2", "Clavi 3", "Clavi 4", "Guitar 1", "Guitar 2", "Guitar 3", "Guitar 4", 
    "Guitar 5", "Guitar 6", "Guitar 7", "Guitar 8", "Bass 1", "Bass 2", "Bass 3", "Bass 4", "Bass 5", 
    "Bass 6", "Bass 7", "Bass 8", "Bass 9", "Str 1", "Str 2", "Str 3", "Str 4", "Str 5", "Str 6", 
    "Str 7", "Vibes 1", "Vibes 2", "Vibes 3", "Vibes 4", "Marimba1", "Marimba2", "Marimba3", 
    "Bells 1", "Bells 2", "Bells 3", "Bells 4", "Bells 5", "Bells 6", "Bells 7", "Bells 8", 
    "Metal 1", "Metal 2", "Metal 3", "Metal 4", "Metal 5", "Metal 6", "Lead 1", "Lead 2", "Lead 3", 
    "Lead 4", "Lead 5", "Lead 6", "Lead 7", "Sus. 1", "Sus. 2", "Sus. 3", "Sus. 4", "Sus. 5", 
    "Sus. 6", "Sus. 7", "Sus. 8", "Sus. 9", "Sus. 10", "Sus. 11", "Sus. 12", "Sus. 13", "Sus. 14", 
    "Sus. 15", "Attack 1", "Attack 2", "Attack 3", "Attack 4", "Attack 5", "Move 1", "Move 2", 
    "Move 3", "Move 4", "Move 5", "Move 6", "Move 7", "Decay 1", "Decay 2", "Decay 3", "Decay 4", 
    "Decay 5", "Decay 6", "Decay 7", "Decay 8", "Decay 9", "Decay 10", "Decay 11", "Decay 12", 
    "Decay 13", "Decay 14", "Decay 15", "Decay 16", "Decay 17", "Decay 18", "SFX 1", "SFX 2", 
    "SFX 3", "SFX 4", "SFX 5", "SFX 6", "SFX 7", "Sin 16'", "Sin 8'", "Sin 4'", "Sin2 2/3", 
    "Sin 2'", "Saw 1", "Saw 2", "Square", "LFOnoise", "Noise 1", "Noise 2", "Digi 1", "Digi 2", 
    "Digi 3", "Digi 4", "Digi 5", "Digi 6", "Digi 7", "Digi 8", "Digi 9", "Digi 10", "Digi 11", 
    "wave1-1", "wave1-2", "wave1-3", "wave2-1", "wave2-2", "wave2-3", "wave3-1", "wave3-2", 
    "wave3-3", "wave4-1", "wave4-2", "wave4-3", "wave5-1", "wave5-2", "wave5-3", "wave6-1", 
    "wave6-2", "wave6-3", "wave7-1", "wave7-2", "wave7-3", "wave8-1", "wave8-2", "wave8-3", 
    "wave9-1", "wave9-2", "wave9-3", "wave10-1", "wave10-2", "wave10-3", "wave11-1", "wave11-2", 
    "wave11-3", "wave12-1", "wave12-2", "wave12-3", "wave13-1", "wave13-2", "wave13-3", "wave14-1", 
    "wave14-2", "wave14-3", "wave15-1", "wave15-2", "wave15-3", "wave16-1", "wave16-2", "wave16-3", 
    "wave17-1", "wave17-2", "wave17-3", "wave18-1", "wave18-2", "wave18-3", "wave19-1", "wave19-2", 
    "wave19-3", "wave20-1", "wave20-2", "wave20-3", "wave21-1", "wave21-2", "wave21-3", "wave22-1", 
    "wave22-2", "wave22-3", "wave23-1", "wave23-2", "wave23-3", "wave24-1", "wave24-2", "wave24-3", 
    "wave25-1", "wave25-2", "wave25-3", "wave26-1", "wave26-2", "wave26-3", "wave27-1", "wave27-2", 
    "wave27-3", "wave28", "wave29", "wave30"
    };

    public static final String[] PANS = new String[] { "<<", "<", "--", ">", ">>" };

    public static final String[] EFFECTS = new String[] 
    {
    "Reverb Hall", "Reverb Room", "Reverb Plate", "Reverb Club", 
    "Reverb Metal", "Short Single Delay (1)", "Long Delay (2)", "Long Delay (3)", 
    "Doubler", "Ping Pong Delay", "Panned Reflections", "Early Reflections", 
    "Gated Reverb", "Delay and Reverb (1)", "Delay and Reverb (2)", "Distortion and Reverb",
    };
        
    public static final String[] CONFIGURATIONS = new String[] 
    {
    "A-B", "A-B-C-D"
    };
        

    boolean isParsingVectorOnly = false;
        
    public static final String TYPE_KEY = "type";
    int synthType = TYPE_TG33;
    JComboBox synthTypeCombo;
        
    public int getSynthType() { return synthType; }
    public void setSynthType(int val, boolean save)
        {
        if (save)
            {
            setLastX("" + val, TYPE_KEY, getSynthName(), true);
            }
        synthType = val;
        synthTypeCombo.setSelectedIndex(val);  // hopefully this isn't recursive
        updateChoosers();
        updateTitle();
        }


    Chooser[] waveChoosers = new Chooser[4];

    public void updateChoosers()
        {
        String[] elts = ( getSynthType() == TYPE_SY35 ? SY35_AWM_WAVES : AWM_WAVES );
        JComboBox box = waveChoosers[TONE_A].getCombo();
        int sel = box.getSelectedIndex();
        box.removeAllItems();
        for(int i = 0; i < elts.length; i++)
            box.addItem(elts[i]);
        box.setSelectedIndex(sel);
                
        box = waveChoosers[TONE_C].getCombo();
        sel = box.getSelectedIndex();
        box.removeAllItems();
        for(int i = 0; i < elts.length; i++)
            box.addItem(elts[i]);
        box.setSelectedIndex(sel);
        }

    public static final int TONE_A = 0;
    public static final int TONE_B = 1;
    public static final int TONE_C = 2;
    public static final int TONE_D = 3;

    public JFrame sprout()
        {
        JFrame frame = super.sprout();
        transmitTo.setEnabled(false);
        writeTo.setEnabled(false);
        addTG33Menu();
        return frame;
        }         

    public YamahaTG33()
        {
        String m = getLastX(TYPE_KEY, getSynthName());
        try
            {
            synthType = (m == null ? TYPE_TG33 : Integer.parseInt(m));
            if (synthType < TYPE_TG33 || synthType > TYPE_SY35)
                {
                synthType = TYPE_TG33;
                }
            }
        catch (NumberFormatException ex)
            {
            synthType = TYPE_TG33;
            }

        model.set("number", 0);
        model.set("bank", 0);
                
        if (voiceCommonParametersToIndex == null)
            {
            voiceCommonParametersToIndex = new HashMap();
            voiceVectorInitialParametersToIndex = new HashMap();
            voiceToneACParametersToIndex = new HashMap();
            voiceToneBDParametersToIndex = new HashMap();
            voiceEnvelopeACParametersToIndex = new HashMap();
            voiceEnvelopeBDParametersToIndex = new HashMap();
            voiceVectorParametersToIndex = new HashMap();

            for(int i = 0; i < voiceCommonParameters.length; i++)
                {
                voiceCommonParametersToIndex.put(voiceCommonParameters[i], Integer.valueOf(i));
                }

            for(int i = 0; i < voiceVectorInitialParameters.length; i++)
                {
                voiceVectorInitialParametersToIndex.put(voiceVectorInitialParameters[i], Integer.valueOf(i));
                }

            for(int i = 0; i < voiceToneACParameters.length; i++)
                {
                voiceToneACParametersToIndex.put(voiceToneACParameters[i], Integer.valueOf(i));
                }

            for(int i = 0; i < voiceToneBDParameters.length; i++)
                {
                voiceToneBDParametersToIndex.put(voiceToneBDParameters[i], Integer.valueOf(i));
                }

            for(int i = 0; i < voiceEnvelopeACParameters.length; i++)
                {
                voiceEnvelopeACParametersToIndex.put(voiceEnvelopeACParameters[i], Integer.valueOf(i));
                }

            for(int i = 0; i < voiceEnvelopeBDParameters.length; i++)
                {
                voiceEnvelopeBDParametersToIndex.put(voiceEnvelopeBDParameters[i], Integer.valueOf(i));
                }

            for(int i = 0; i < voiceVectorParameters.length; i++)
                {
                voiceVectorParametersToIndex.put(voiceVectorParameters[i], Integer.valueOf(i));
                }
            }


        /// SOUND PANEL
                
        JComponent soundPanel = new SynthPanel(this);
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        hbox.addLast(addGlobal(Style.COLOR_A()));
        vbox.add(hbox);
        vbox.add(addVectorGlobal(Style.COLOR_A()));
            
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Global", soundPanel);
                

        JComponent sourcePanel = new SynthPanel(this);
        vbox = new VBox();

        vbox.add(addTone(TONE_A, Style.COLOR_A()));
        vbox.add(addEnvelope(TONE_A, true, Style.COLOR_A()));
        vbox.add(addTone(TONE_C, Style.COLOR_B()));
        vbox.add(addEnvelope(TONE_C, true, Style.COLOR_B()));

        sourcePanel.add(vbox, BorderLayout.CENTER);
        addTab("Tones A-C", sourcePanel);
        
        sourcePanel = new SynthPanel(this);
        vbox = new VBox();

        vbox.add(addTone(TONE_B, Style.COLOR_A()));
        vbox.add(addFM(TONE_B, true, Style.COLOR_B()));
        vbox.add(addEnvelope(TONE_B, true, Style.COLOR_B()));
        vbox.add(addFM(TONE_B, false, Style.COLOR_C()));
        vbox.add(addEnvelope(TONE_B, false, Style.COLOR_C()));
            
        sourcePanel.add(vbox, BorderLayout.CENTER);
        addTab("Tone B", sourcePanel);
        
        sourcePanel = new SynthPanel(this);
        vbox = new VBox();

        vbox.add(addTone(TONE_D, Style.COLOR_A()));
        vbox.add(addFM(TONE_D, true, Style.COLOR_B()));
        vbox.add(addEnvelope(TONE_D, true, Style.COLOR_B()));
        vbox.add(addFM(TONE_D, false, Style.COLOR_C()));
        vbox.add(addEnvelope(TONE_D, false, Style.COLOR_C()));
                        
        sourcePanel.add(vbox, BorderLayout.CENTER);
        addTab("Tone D", sourcePanel);
        
        sourcePanel = new SynthPanel(this);
        vbox = new VBox();
        
        vbox.add(addVector(true, 1, Style.COLOR_A()));

        sourcePanel.add(vbox, BorderLayout.CENTER);
        addTab("Level 1-25", sourcePanel);


        sourcePanel = new SynthPanel(this);
        vbox = new VBox();
        
        vbox.add(addVector(true, 26, Style.COLOR_A()));

        sourcePanel.add(vbox, BorderLayout.CENTER);
        addTab("Level 26-50", sourcePanel);

        sourcePanel = new SynthPanel(this);
        vbox = new VBox();
        
        vbox.add(addVector(false, 1, Style.COLOR_A()));

        sourcePanel.add(vbox, BorderLayout.CENTER);
        addTab("Detune 1-25", sourcePanel);

        sourcePanel = new SynthPanel(this);
        vbox = new VBox();
        
        vbox.add(addVector(false, 26, Style.COLOR_A()));

        sourcePanel.add(vbox, BorderLayout.CENTER);
        addTab("Detune 26-50", sourcePanel);

        model.set("name", "Untitled");

        updateChoosers();        
        loadDefaults();     
        }
                
    public void addTG33Menu()
        {
        JMenu menu = new JMenu("TG33");
        menubar.add(menu);

        JMenuItem oneMPEMenu = new JMenuItem("Send Patch as Pseudo-MPE");
        oneMPEMenu.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                JComboBox bank = new JComboBox(YamahaTG33Multi.VOICE_BANKS);
                int b = model.get("bank");
                if (b == 0 || b == 3 || b == 4)
                    bank.setSelectedIndex(0);
                else
                    bank.setSelectedIndex(b);
                JTextField number = new JTextField("" + (model.get("number") + 1), 3);
                
                int n = 0;
                String title = "Send Patch as Pseudo-MPE";
                while(true)
                    {
                    boolean result = showMultiOption(YamahaTG33.this, new String[] { "Bank", "Patch Number"}, 
                        new JComponent[] { bank, number }, title, "Enter the patch bank and number.");
                    if (!result) return;

                    try { n = Integer.parseInt(number.getText()); }
                    catch (NumberFormatException ex)
                        {
                        showSimpleError(title, "The Patch Number must be an integer 11 ... 88 (no 9s or 0s)");
                        continue;
                        }
                        
                    if (n % 10 == 0 || n % 10 == 9 || n < 11 || n > 88)      
                        {
                        showSimpleError(title, "The Patch Number must be an integer 11 ... 88 (no 9s or 0s)");
                        continue;
                        }
                        
                    break;
                    }           
                    
                n = ((n / 10) - 1) * 8 + ((n % 10) - 1);

                int i = bank.getSelectedIndex();
                boolean send = getSendMIDI();
                setSendMIDI(true);
                tryToSendSysex(getMPEForPatch(i, n, model.get("name", "")));
                setSendMIDI(send);
                }
            });

        menu.add(oneMPEMenu);

        final JCheckBoxMenuItem vectorMenu = new JCheckBoxMenuItem("Receive Vector Data Only");
        vectorMenu.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                isParsingVectorOnly = vectorMenu.isSelected();
                }
            });
        menu.add(vectorMenu);

        }

    public byte[] getMPEForPatch(int bank, int number, String name)
        {
        YamahaTG33Multi multi = (YamahaTG33Multi)
            instantiate(YamahaTG33Multi.class, true, false, null);
        
        multi.setSendMIDI(false);
        multi.getUndo().setWillPush(false);
        multi.getModel().setUpdateListeners(false);
        for(int j = 1; j <= 16; j++)
            {
            multi.getModel().set("c" + j + "bank", bank);
            multi.getModel().set("c" + j + "number", number);
            multi.getModel().set("c" + j + "on", 1);
            multi.getModel().set("c" + j + "volume", 127);
            }
        multi.getModel().set("name", name);
        return multi.emit(null, false, false);
        }

    
    public String getDefaultResourceFileName() { return "YamahaTG33.init"; }
    public String getHTMLResourceFileName() { return "YamahaTG33.html"; }

    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        JComboBox bank = new JComboBox(BANKS);
        bank.setEditable(false);
        bank.setMaximumRowCount(32);
        bank.setSelectedIndex(model.get("bank"));
                
        int original = model.get("number");
                
        JTextField number = new JTextField("" + ((original / 8 + 1) * 10 + (original % 8 + 1)), 3);

        while(true)
            {
            boolean result = showMultiOption(this, new String[] { "Bank", "Patch Number"}, 
                new JComponent[] { bank, number }, title, "Enter Patch number");
                
            if (result == false)
                return false;
                                
            int n;
            try { n = Integer.parseInt(number.getText()); }
            catch (NumberFormatException e)
                {
                showSimpleError(title, "The Patch Number must be an integer 11...88.\nDigits 9 and 0 are not permitted.");
                continue;
                }
            if (n % 10 == 0 || n % 10 == 9 || n < 11 || n > 88)      
                {
                showSimpleError(title, "The Patch Number must be an integer 11...88.\nDigits 9 and 0 are not permitted.");
                continue;
                }
            
            n = ((n / 10) - 1) * 8 + (n % 10 - 1);
            
            change.set("bank", bank.getSelectedIndex());
            change.set("number", n);
            return true;
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
        final PatchDisplay pd = new PatchDisplay(this, 4);
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
        vbox.addBottom(Stretch.makeVerticalStretch()); 
        hbox.add(vbox);

        // Not enough space to show the title
        hbox.addLast(Strut.makeHorizontalStrut(130));

        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }




    public JComponent addGlobal(Color color)
        {
        Category category = new Category(this, "Common", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();

        params = CONFIGURATIONS;
        comp = new Chooser("Configuration", this, "configuration", params);
        vbox.add(comp);

        params = EFFECTS;
        comp = new Chooser("Effect Type", this, "effecttype", params);
        vbox.add(comp);

        comp = new CheckBox("Aftertouch", this, "aftertouchlevel");
        vbox.add(comp);

        hbox.add(vbox);

        comp = new CheckBox("Aftertouch Pitch Mod", this, "aftertouchpm");
        vbox.add(comp);
        comp = new CheckBox("Aftertouch Amplitude Mod", this, "aftertoucham");
        vbox.add(comp);
        comp = new CheckBox("Mod Wheel Pitch Mod", this, "modulationwheelpm");
        vbox.add(comp);
        comp = new CheckBox("Mod Wheel Amplitude Mod", this, "modulationwheelam");
        vbox.add(comp);

        hbox.add(vbox);

        comp = new LabelledDial("Effect Balance", this, "effectbalance", color, 0, 127);
        hbox.add(comp);
        
        comp = new LabelledDial("Effect Level", this, "effectsendlevel", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel(" [TG33] ");
        hbox.add(comp);
        
        comp = new LabelledDial("Effect Depth", this, "effectsenddepth", color, 0, 7);
        ((LabelledDial)comp).addAdditionalLabel(" [SY22/SY35] ");
        hbox.add(comp);
        
        comp = new LabelledDial("Pitch Bend", this, "pitchbendrange", color, 0, 12);
        ((LabelledDial)comp).addAdditionalLabel("Range");
        hbox.add(comp);
        
        comp = new LabelledDial("Pitch Bias", this, "pitchbias", color, 0, 24, 12)
            {
            public boolean isSymmetric() { return true; }
            };
        hbox.add(comp);
        
        comp = new LabelledDial("Envelope Delay", this, "egdelayrate", color, 0, 127);
        hbox.add(comp);
        
        // WARNING: This must be converted
        comp = new LabelledDial("Envelope", this, "egattackrate", color, 0, 126, 63)
            {
            public boolean isSymmetric() { return true; }
            };
        ((LabelledDial)comp).addAdditionalLabel("Attack Rate");
        hbox.add(comp);

        // WARNING: This must be converted
        vbox = new VBox();
        comp = new LabelledDial("Envelope", this, "egreleaserate", color, 0, 126, 63)
            {
            public boolean isSymmetric() { return true; }
            };
        ((LabelledDial)comp).addAdditionalLabel("Release Rate");
        vbox.add(comp);
        hbox.add(vbox);
        
        /*
          vbox = new VBox();
          // It's not clear if these are really rate or time.  I'm going with time.
          comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
          new String[] { null, "egdelayrate", "egattackrate", "egreleaserate" },
          new String[] { null, null, null, null },
          new double[] { 0, 0.33333 / 127, 0.33333 / 126, 0.33333 / 126 },
          new double[] { 0, 0, 1, 0 } );
          vbox.add(comp);
          hbox.addLast(vbox);
        */
        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
    
    public JComponent addVectorGlobal(Color color)
        {
        Category category = new Category(this, "Vector", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();

        // WARNING: 160ms must be remapped to 0 in parsing
        comp = new LabelledDial("Level Speed", this, "levelspeed", color, 0, 15)
            {
            public String map(int value)
                {
                return "" + (value * 10 + 10) + " ms";
                }
            };
        hbox.add(comp);
        
        // WARNING: 160ms must be remapped to 0 in parsing
        comp = new LabelledDial("Detune Speed", this, "detunespeed", color, 0, 15)
            {
            public String map(int value)
                {
                return "" + (value * 10 + 10) + " ms";
                }
            };
        hbox.add(comp);
                
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
    
    public JComponent addVector(boolean level, int start, Color color)
        {
        Category category = new Category(this, (level ? "Level Vectors " : "Detune Vectors ") + start + "-" + (start + 24), color);
                
        JComponent comp;
        String[] params;
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        String param = (level ? "level" : "detune");
        category.makeDistributable(param);

            
        for(int i = start; i < start + 25; i+=5)
            {
            if (i != start) vbox.add(Strut.makeVerticalStrut(10));
            for (int j = i; j < i + 5; j++)
                {
                if (j != i) hbox.add(Strut.makeHorizontalStrut(10));
                final int _j = j;
                        
                // FIXME: we need to modify the value for #1 during parse/emit...
                comp = new LabelledDial("Time " + j, this,  param + j + "time", color, 0, (j == 1 ? 254 : 255))
                    {
                    public String map(int value)
                        {
                        if (value < 254) return "" + value;
                        else if (value == 254) return (_j == 1 ? "End" : "Repeat");
                        // value == 255:
                        else return "End";
                        }
                    };
                hbox.add(comp);
                comp = new LabelledDial("X " + j + "  D-C", this,  param + j + "xaxis", color, 0, 62, 31)
                    {
                    public boolean isSymmetric() { return true; }
                    };
                hbox.add(comp);
                comp = new LabelledDial("Y " + j + "  B-A", this,  param + j + "yaxis" , color, 0, 62, 31)
                    {
                    public boolean isSymmetric() { return true; }
                    };
                hbox.add(comp);
                }
            vbox.add(hbox);
            hbox = new HBox();
            }
                
        category.add(vbox, BorderLayout.CENTER);
        return category;
        }
    
    /*
      COMMON
      wave number
      freq shift
      aftertouch sens
      velocity sens
      lfo type
      lfo speed
      lfo delay time
      lfo rate
      AM Depth
      PM Depth
      EG Type
      Pan
      Connect
      Feedback
    */

    public JComponent addTone(final int tone, Color color)
        {
        Category category = new Category(this, "Tone " + new String[] { "A", "B", "C", "D" }[tone], color);
//        category.makePasteable("tone" + tone);
        category.makePasteable("tone");

        String h = "tone" + tone;
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();

        params = ((tone == TONE_A || tone == TONE_C) ? AWM_WAVES : FM_WAVES);           
        comp = new Chooser("Wave Type", this, h + "wavetype", params)
            {
            // Notice NOT update().  This is because when we load from a file
            // or from sysex lots of FM stuff gets updated, but then we might
            // erase it all when this one is updated.  We only want to revise
            // the FM stuff when the user has selected directly from the widget.
            public void userSelected(String key, Model model)
                {
                if ((tone == TONE_B || tone == TONE_D))
                    {
                    // we are going to update a several parameters here so we should do
                    // it as a single revision.  As it turns out only one parameter will
                    // output just a few MIDI values, but whatever.
                    undo.setWillPush(false);
                    Model backup = (Model)(model.clone());

                    // notice update(...) is inside here
                    super.update(key, model);
                    updateFMData(tone, model.get(key, 0));
                    undo.setWillPush(true);
                    if (!backup.keyEquals(getModel()))  // it's changed, do an undo push
                        undo.push(backup);
                    repaint();
                    }
                else
                    {
                    super.update(key, model);
                    }
                }               
            };
        waveChoosers[tone] = (Chooser)comp;
        vbox.add(comp);

        params = LFO_TYPES;
        comp = new Chooser("LFO Type", this, h + "lfotype", params);
        vbox.add(comp);

        hbox.add(vbox);
        vbox = new VBox();
        if ((tone == TONE_B || tone == TONE_D))
            {
            params = ALGORITHMS;
            comp = new Chooser("Algorithm", this, h + "algorithm", params);
            vbox.add(comp);
            }

        hbox.add(vbox);
                
        if ((tone == TONE_A || tone == TONE_C))
            {
            vbox = new VBox();
            ImageIcon[] icons = LEVEL_ICONS;
            params = LEVELS;
            comp = new Chooser("Level Scaling", this, h + "levelscaling", params, icons);
            vbox.add(comp);
        
            icons = RATE_ICONS;
            params = RATES;
            comp = new Chooser("Rate Scaling", this, h + "ratescaling", params, icons);
            vbox.add(comp);
            hbox.add(vbox);

            vbox = new VBox();

            comp = new CheckBox("Amplitude", this, h + "amplitudemod");
            vbox.add(comp);
                
            comp = new CheckBox("Pitch", this, h + "pitchmod");
            vbox.add(comp);
                
            hbox.add(vbox);
            }
                

        comp = new LabelledDial("Frequency", this, h + "frequencyshift", color, 0, 24, 12)
            {
            public boolean isSymmetric() { return true; }
            };
        ((LabelledDial)comp).addAdditionalLabel("Shift");
        hbox.add(comp);
        
        comp = new LabelledDial("Aftertouch", this, h + "aftertouchsensitivity", color, 0, 6, 3)
            {
            public boolean isSymmetric() { return true; }
            };
        ((LabelledDial)comp).addAdditionalLabel("Sensitivity");
        hbox.add(comp);
        
        comp = new LabelledDial("Velocity", this, h + "velocitysensitivity", color, 0, 10, 5)
            {
            public boolean isSymmetric() { return true; }
            };
        ((LabelledDial)comp).addAdditionalLabel("Sensitivity");
        hbox.add(comp);
        
        comp = new LabelledDial("LFO Speed", this, h + "lfospeed", color, 0, 31);
        hbox.add(comp);
        
        comp = new LabelledDial("LFO Delay", this, h + "lfodelay", color, 0, 127);
        hbox.add(comp);
        
        // watch out, it's backwards
        comp = new LabelledDial("LFO Rate", this, h + "lforate", color, 0, 127);
        hbox.add(comp);
        
        comp = new LabelledDial("LFO", this, h + "lfoam", color, 0, 15);
        ((LabelledDial)comp).addAdditionalLabel("Amplitude Mod");
        hbox.add(comp);
        
        comp = new LabelledDial("LFO", this, h + "lfopm", color, 0, 31);
        ((LabelledDial)comp).addAdditionalLabel("Pitch Mod");
        hbox.add(comp);

        comp = new LabelledDial("Pan", this, h + "pan", color, 0, 4)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                return PANS[value];
                }
            };
        hbox.add(comp);

        if ((tone == TONE_A || tone == TONE_C))
            {
            comp = new LabelledDial("Temperment", this, h + "temperment", color, 0, 3);
            hbox.add(comp);
            comp = new LabelledDial("Detune", this, h + "detune", color, 0, 15);
            hbox.add(comp);
            }
        else if ((tone == TONE_B || tone == TONE_D))
            {
            comp = new LabelledDial("Feedback", this, h + "feedback", color, 0, 7);
            hbox.add(comp);
            }
        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
    

/*
  FM Mod and Carrier
  Wave
  Fixed
  Delay Switch
  Max
  AM Switch
  PM Switch
  Multi   (harmonics?)
  Level
  DT1
  DT2
  Level Scaling
  Rate Scaling
  Envelope stuff...
*/

    public JComponent addFM(int tone, boolean carrier, Color color)
        {
        Category category = new Category(this, (carrier ? "Carrier " : "Modulator " ) + new String[] { "A", "B", "C", "D" }[tone], color);
//        category.makePasteable("tone" + tone);
        category.makePasteable("tone");

        String h = "tone" + tone + (carrier ? "0" : "1");
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();

        params = WAVES;         
        comp = new Chooser("Wave", this, h + "wave", params, WAVE_ICONS);
        vbox.add(comp);

        comp = new CheckBox("Fixed Frequency", this, h + "fixed");
        ((CheckBox)comp).addToWidth(2);
        vbox.add(comp);

        hbox.add(vbox);
        vbox = new VBox();

        params = LEVELS;
        comp = new Chooser("Level Scaling", this, h + "levelscaling", params, LEVEL_ICONS);
        vbox.add(comp);

        params = RATES;
        comp = new Chooser("Rate Scaling", this, h + "ratescaling", params, RATE_ICONS);
        vbox.add(comp);

        hbox.add(vbox);

/*
  vbox = new VBox();

  comp = new CheckBox("Max", this, h + "max");
  vbox.add(comp);
        
  hbox.add(vbox);
*/
        vbox = new VBox();

        comp = new CheckBox("Amplitude", this, h + "amplitudemod");
        vbox.add(comp);
        
        comp = new CheckBox("Pitch", this, h + "pitchmod");
        vbox.add(comp);
        
        hbox.add(vbox);

        comp = new LabelledDial("Temperment", this, h + "temperment", color, 0, 3);
        hbox.add(comp);
        comp = new LabelledDial("Detune", this, h + "detune", color, 0, 15);
        hbox.add(comp);

        // This is "Harmonic" or "Multi" or "Mult"
        comp = new LabelledDial("Frequency Ratio", this, h + "frequencyratio", color, 0, 15);
        hbox.add(comp);
        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
    

    public JComponent addEnvelope(int tone, boolean carrier, Color color)
        {
        Category category = new Category(this, 
                (tone == TONE_A  ? "Envelope A" :
                    (tone == TONE_C  ? "Envelope C" :
                        ((carrier ? "Carrier " : "Modulator ") + 
                        (tone == TONE_B ? "B" : "D") + " Envelope"))), color);

//        category.makePasteable("tone" + tone);
        category.makePasteable("tone");

        String h = "tone" + tone + ((tone == TONE_B || tone == TONE_D) ? (carrier ? "0" : "1") : "");
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();


        if (tone == TONE_A || tone == TONE_C || carrier)
            {
            params = ENVELOPE_TYPES;
            comp = new Chooser("Envelope Type", this, "tone" + tone + "egtype", params);
            vbox.add(comp);
            }

        comp = new CheckBox("Delay", this, h + "delayonoff");
        vbox.add(comp);
        
        hbox.add(vbox);
        vbox = new VBox();

        // watch out, it's backwards
        // NOTE that this one is inconsistent compared to the others -- it doesn't
        // say tone30level or tone31level, but rather it says tone0volume, tone1volume, but tone1level etc.
        // This is because it's one of the keys which can be outputted in real time and is so part of the
        // lists below.  It's basically unique in this respect.
        comp = new LabelledDial("Amplitude", this, "tone" + tone + 
            ((tone == TONE_A || tone == TONE_C || carrier) ? "volume" : "level"), color, 0, 127);
        hbox.add(comp);

        // Note different range AND backwards
        comp = new LabelledDial("Initial Level", this, h + "initiallevel", color, 0, 127);
        hbox.add(comp);
        
        // Note different range
        comp = new LabelledDial("Attack Rate", this, h + "attackrate", color, 0, 63);
        hbox.add(comp);
        
        // Note different range AND backwards
        comp = new LabelledDial("Attack Level", this, h + "attacklevel", color, 0, 127);
        hbox.add(comp);

        // Note different range
        comp = new LabelledDial("Decay 1 Rate", this, h + "decay1rate", color, 0, 63);
        hbox.add(comp);
        
        // Note different range AND backwards
        comp = new LabelledDial("Decay 1 Level", this, h + "decay1level", color, 0, 127);
        hbox.add(comp);

        // Note different range
        comp = new LabelledDial("Decay 2 Rate", this, h + "decay2rate", color, 0, 63);
        hbox.add(comp);
        
        // Note different range AND backwards
        comp = new LabelledDial("Decay 2 Level", this, h + "decay2level", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("(Sustain)");
        hbox.add(comp);
        
        // Note different range
        comp = new LabelledDial("Release Rate", this, h + "releaserate", color, 0, 63);
        hbox.add(comp);
        
        // It's not clear if these are really rate or time.  I'm going with time.
        // Yamaha is backwards, so we preproces
        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, h + "attackrate", h + "decay1rate", h + "decay2rate", null, h + "releaserate" },
            new String[] { h + "initiallevel", h + "attacklevel", h + "decay1level", h + "decay2level",  h + "decay2level", null },
            new double[] { 0, 0.2 / 63, 0.2 / 63, 0.2 / 63, 0.25, 0.2 / 63 },
            new double[] { 1.0 / 127, 1.0 / 127, 1.0 / 127, 1.0 / 127, 1.0 / 127, 0 } )
            {
            public double preprocessXKey(int index, String key, double value)
                {
                return 63.0 - value;
                }
            };
        hbox.addLast(comp);
        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }



    /** Map of parameter -> index in the allParameters array. */


    // voice common
    public static final int[][] voiceCommon = new int[][] 
    {
    { 0x00, 0x01, 0x02, 0x02, 0x09, 0x09, 0x09, 0x09, 0x09, 0x09, 0x09, 0x09, 0x03, 0x06, 0x05, 0x05, 0x04, 0x04, 0x06, 0x01, 0x07, 0x07 },
    { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 },
    { 0x00, 0x01, 0x02, 0x06, 0x0C, 0x0D, 0x0E, 0x0F, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x15, 0x15, 0x15, 0x15, 0x16, 0x17, 0x18, 0x19 },
    { 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01 },
    { 0x7E, 0x7F, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x3f, 0x5f, 0x6f, 0x7d, 0x7e, 0x7f, 0x7f, 0x7f, 0x7f },
    };
                
    static HashMap voiceCommonParametersToIndex;

    public static final String[] voiceCommonParameters = new String[]
    {
    "configuration",
    "effecttype",
    "effectbalance",
    "effectsendlevel",
    "name1",                                    // patch name
    "name2",
    "name3",
    "name4",
    "name5",
    "name6",
    "name7",
    "name8",
    "pitchbendrange",
    "aftertouchlevel",
    "aftertouchpm",
    "aftertoucham",
    "modulationwheelpm",
    "modulationwheelam",
    "pitchbias",
    "egdelayrate",
    "egattackrate",
    "egreleaserate",
    };
        
    // Voice Tone A or C
    public static final int[][] voiceToneAC = new int[][] 
    {
    { 0x00, 0x01, 0x05, 0x04, 0x07, 0x09, 0x08, 0x08, 0x07, 0x07, 0x03, 0x02 },
    { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 }, 
    { 0x00, 0x01, 0x02, 0x02, 0x03, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09 }, 
    { 0x01, 0x01, 0x01, 0x01, 0x00, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01 },
    { 0x7f, 0x7f, 0x0f, 0x70, 0x1f, 0x60, 0x7f, 0x7f, 0x70, 0x60, 0x78, 0x7f },
    };
        
    static HashMap voiceToneACParametersToIndex;

    // these will start with tone0 or tone2
    public static final String[] voiceToneACParameters = new String[]
    {
    "wavetype",
    "frequencyshift",
    "aftertouchsensitivity",
    "velocitysensitivity",
    "lfotype",
    "lfospeed",
    "lfodelay",
    "lforate",
    "lfoam",
    "lfopm",
    "pan",
    "volume",
    };
                
        
    // Voice Tone B or D
    public static final int[][] voiceToneBD = new int[][] 
    {
    { 0x00, 0x01, 0x05, 0x04, 0x07, 0x09, 0x08, 0x08, 0x07, 0x07, 0x03, 0x06, 0x08, 0x02 },
    { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 },
    { 0x16, 0x17, 0x18, 0x18, 0x19, 0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F, 0x21, 0x2D },
    { 0x01, 0x01, 0x01, 0x01, 0x01, 0x00, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01 },
    { 0x7F, 0x7F, 0x0F, 0x70, 0x1F, 0x60, 0x7F, 0x7F, 0x70, 0x60, 0x78, 0x78, 0x7F, 0x7F },
    };
         
    static HashMap voiceToneBDParametersToIndex;

    // these will start with tone1 or tone3
    public static final String[] voiceToneBDParameters = new String[]
    {
    "wavetype",
    "frequencyshift",
    "aftertouchsensitivity",
    "velocitysensitivity",
    "lfotype",
    "lfospeed",
    "lfodelay",
    "lforate",
    "lfoam",
    "lfopm",
    "pan",
    "feedback",
    "level",
    "volume",
    };
                
        
    // Voice Element Envelope A or C
    public static final int[][] voiceEnvelopeAC = new int[][] 
    {
    { 0x00, 0x07, 0x08, 0x01, 0x03, 0x04, 0x05, 0x06, 0x02, 0x03, 0x04, 0x05 },
    { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 },
    { 0x08, 0x0B, 0x0B, 0x0C, 0x0C, 0x0D, 0x0E, 0x0F, 0x10, 0x11, 0x12, 0x13 },
    { 0x01, 0x00, 0x01, 0x00, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01 },
    { 0x0F, 0x0F, 0x78, 0x7F, 0x40, 0x40, 0x40, 0x40, 0x00, 0x00, 0x00, 0x00 },
    };
        
    static HashMap voiceEnvelopeACParametersToIndex;

    // these will start with tone0 or tone2
    public static final String[] voiceEnvelopeACParameters = new String[]
    {
    "egtype",
    "levelscaling",
    "ratescaling",
    "delayonoff",
    "attackrate",
    "decay1rate",
    "decay2rate",
    "releaserate",
    "initiallevel",
    "attacklevel",
    "decay1level",
    "decay2level",
    };

    // Voice Element Envelope B or D
    public static final int[][] voiceEnvelopeBD = new int[][] 
    {
    { 0x00, 0x07, 0x08, 0x01, 0x03, 0x04, 0x05, 0x06, 0x02, 0x03, 0x04, 0x05 },
    { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 },
    { 0x1E, 0x2F, 0x2F, 0x30, 0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37 },
    { 0x01, 0x00, 0x01, 0x00, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01 },
    { 0x0F, 0x0F, 0x78, 0x7F, 0x40, 0x40, 0x40, 0x40, 0x00, 0x00, 0x00, 0x00 },
    };
         
    static HashMap voiceEnvelopeBDParametersToIndex = voiceEnvelopeACParametersToIndex;

    // these will start with tone10, tone11, tone30, or tone31
    public static final String[] voiceEnvelopeBDParameters = voiceEnvelopeACParameters;

    // Voice Vector Initial Parameters
    public static final int[][] voiceVectorInitial = new int[][] 
    {
    { 0x00, 0x03 },
    { 0x00, 0x00 },
    { 0x00, 0x01 },
    { 0x01, 0x01 }, 
    { 0x7f, 0x7f },
    };

    static HashMap voiceVectorInitialParametersToIndex;
    public static final String[] voiceVectorInitialParameters = new String[]
    {
    "levelspeed",
    "detunespeed",
    };

    public static final int[][] voiceVector = new int[300][5];
    static HashMap voiceVectorParametersToIndex;
    public static final String[] voiceVectorParameters = new String[300];

    static
        {
        // I think there is a bug.  lines 254 and on are ST=$02, "Level..." in the manual.
        // But I think they should be ST=$05, "Detune..."
        for(int i = 0; i < 300; i++)
            {
            voiceVector[i][0] = (i < 150 ? 0x02 : 0x05);
            voiceVector[i][1] = i / 128;
            voiceVector[i][2] = (i + 2) % 128;
            voiceVector[i][3] = 0x01;
            voiceVector[i][4] = 0x7F;
            int q = i % 3;
            voiceVectorParameters[i] = (i < 150 ? "level" : "detune") + ((i < 150 ? i : i - 150) / 3 + 1) + (q == 0 ? "time" : (q == 1 ? "xaxis" : "yaxis")) ;
            }
        }

    public byte getID() 
        { 
        try 
            { 
            byte b = (byte)(Byte.parseByte(tuple.id));
            if (b > 16) b = 1;
            if (b >= 1) return b;
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
            if (b > 16) b = 1;
            if (b >= 1) return "" + b;
            } 
        catch (NumberFormatException e) { }             // expected
        return "" + getID();
        }


    byte findOn(byte[] map, byte val) 
        { 
        for(int i = 0; i < map.length; i++) 
            {
            if (map[i] == val) return (byte)i;
            }
        return 0;
        }
                
                



    //// PARSING AND EMITTING
    ////
    //// This is kind of a mess.  The TG33/SY22/SY35 have pretty complex, problematic, and somewhat incomplete sysex:
    ////
    //// 1. The SY22 and SY35 differ in their AWM waves
    //// 2. The TG33 differs from the SY22 and SY85 in its sysex voice common section, which is more detailed
    //// 3. The TG33 also has three unknown bits marked in its voice-1 bank sysex documentation: DRM (at byte 0),
    ////    MAX (at bytes 30, 51, and so on), and PIT -WHEEL- TYP (at byte 15).  I have heard people say that 
    ////    MAX limits the envelope to the maximum volume, and not in a good way, but it's not documented anywhere.
    ////    DRM is entirely unknown.  PIT -WHEEL- TYP is also entirely unknown.
    //// 4. A few parameters have holes in their ranges.  :-(  Notably LEVEL TIME 1, DETUNE TIME 1, and something else,
    ////    I forget what.
    //// 5. A number of parameters are 2's complement.  :-(
    //// 6. Several parameters are 2's complement *and* smaller than a byte.
    //// 7. The send-one-parameter sysex commands send the parameters in their shifted positions as prepared for
    ////    bank sends.  This is bad for edisyn as it means we have to convert them from simple ordinal values
    ////    like 0, 1, 2, 3 to stuff like 0x00, 0x20, 0x40, and whatnot.  On top of it a few of these are also
    ////    2's complement on top of it.
    //// 8. Several parameters are inverted for no reason.  That is, for a desired x value you actually store 127-x.
    //// 9. The SY22 documentation is almost zero.  We're relying on the SY35 documentation.
    //// 10. The TG33 alone can do single-parameter sends, but the commands for sending individual Modulation
    ////     portion FM parameters is unknown.  
    //// 11. Names change radically from one synth to the other with no explanation.  In fact in the TG33 manual
    ////     names change from the single-parameter section to the bank-send section (such as "MODULATION WHEEL PM" to
    ////     "PIT -WHEEL- PM", what the...).  A great many names in the bank-send section are cryptic.
    //// 12. Some parameters, or groups of parameters, are 2-bytes and others are 1-byte.  And the explanations and
    ////     instructions regarding the are cryptic.  The TG-33 is slightly better described.  The SY-35 sysex is completely
    ////     wrong in its byte numbering.
    ////
    //// So pardon me if the code below is complex and convoluted.  This is what we're dealing with.
        
        
    /// These four maps convert Edisyn values (0, 1, 2, ...) into the actual values for send(key).  They're also used in the
    /// bank send but often are immediately counteracted (by dividing by 16 etc.).  This is just for my own debugging sanity.
                
    static final byte[] AFTERTOUCH_SENSITIVITY_MAP =  new byte[] { 0x50, 0x60, 0x70, 0x00, 0x10, 0x20, 0x30 };
    static final byte[] VELOCITY_SENSITIVITY_MAP = new byte[] { 0x06, 0x07, 0x08, 0x09, 0x0A, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05 };
    static final byte[] LFO_TYPE_MAP = new byte[] { 0x00, 0x20, 0x40, 0x60, (byte)0x80 };
    static final byte[] ENVELOPE_TYPE_MAP = new byte[] { 0x00, 0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70 };


    // On the TG33, different categories of parameters have different sysex strings.
    // These sysex strings are defined in the tables voiceCommon, voiceToneAC, voiceToneBD, voiceVectorInitial, and voiceVector
    // Note that some parameters must be preprocessed with math or with the above maps because they are
    // 2's complement etc.
    //
    // There are 6 tone areas: A, B Mod, B carrier, C, D Mod, D Carrier, plus two regions that both mods and carriers share.
    // The parameter names in Edisyn have certain prefixes meant to easy copy/paste procedures.
    //
    // tone0                Voice A
    // tone1                Voice B [parameters for both modulation and carrier]
    //              tone10  Voice B [carrier parameters]
    //              tone11  Voice B [modulator parameters]
    // tone2                Voice C
    // tone3                Voice D [parameters for both modulation and carrier]
    //              tone30  Voice D [carrier parameters]
    //              tone31  Voice D [modulator parameters]
    //
        
    // This helper function is called by emitAll(key)
    public byte[] send(String key)
        {
        int val = model.get(key, 0);
        if (voiceCommonParametersToIndex.containsKey(key))
            {
            int i = ((Integer)(voiceCommonParametersToIndex.get(key))).intValue();
            
            if (key.equals("aftertouchlevel") && val > 0) val = 0x40;
            else if (key.equals("aftertouchpm") && val > 0) val = 0x20;
            else if (key.equals("aftertoucham") && val > 0) val = 0x10;
            else if (key.equals("modulationwheelpm") && val > 0) val = 0x02;
            else if (key.equals("pitchbias")) val -= 12;                                        // 2's complement
            else if (key.equals("egattackrate")) val -= 63;                                     // 2's complement
            else if (key.equals("egreleaserate")) val -= 63;                                    // 2's complement
            
            return(new byte[]
                {
                (byte)0xF0, 0x43, (byte)(16 + getID()), 0x26, 0x00,
                (byte)voiceCommon[0][i], 0x00,                  // ST
                (byte)voiceCommon[1][i],                        // F1
                (byte)voiceCommon[2][i],                        // F2
                (byte)voiceCommon[3][i],                        // B1
                (byte)voiceCommon[4][i],                        // B2
                (byte)((val >>> 7) & 1),                        // V1
                (byte)(val & 127), (byte)0xF7                   // V2
                });
            }
        else if (voiceVectorInitialParametersToIndex.containsKey(key))
            {
            int i = ((Integer)(voiceVectorInitialParametersToIndex.get(key))).intValue();
            // need to move the 160
            if (val == 15) val = 0;
            else val = val + 1;
            return(new byte[]
                {
                (byte)0xF0, 0x43, (byte)(16 + getID()), 0x26, 0x01,
                (byte)voiceVectorInitial[0][i], 0x00,           // ST
                (byte)voiceVectorInitial[1][i],                 // F1
                (byte)voiceVectorInitial[2][i],                 // F2
                (byte)voiceVectorInitial[3][i],                 // B1
                (byte)voiceVectorInitial[4][i],                 // B2
                (byte)((val >>> 7) & 1),                        // V1
                (byte)(val & 127), (byte)0xF7                   // V2
                });
            }
        else if (voiceVectorParametersToIndex.containsKey(key))
            {
            int i = ((Integer)(voiceVectorParametersToIndex.get(key))).intValue();
            
            if (key.equals("level1time") && val == 254) val = 255;
            else if (key.equals("detune1time") && val == 254) val = 255;
            
            return(new byte[]
                {
                (byte)0xF0, 0x43, (byte)(16 + getID()), 0x26, 0x01,
                (byte)voiceVector[i][0], 0x00,                          // ST
                (byte)voiceVector[i][1],                        // F1
                (byte)voiceVector[i][2],                        // F2
                (byte)voiceVector[i][3],                        // B1
                (byte)voiceVector[i][4],                        // B2
                (byte)((val >>> 7) & 1),                        // V1
                (byte)(val & 127), (byte)0xF7                   // V2
                });
            }
        else if (key.startsWith("tone"))
            {
            int tone = 0;
            if (key.startsWith("tone0")) { tone = 0; key = key.substring(5);}
            else if (key.startsWith("tone10")) { tone = 4; key = key.substring(6); }
            else if (key.startsWith("tone2")) { tone = 2; key = key.substring(5); }
            else if (key.startsWith("tone30")) { tone = 5; key = key.substring(6); }
            else if (key.startsWith("tone1")) { tone = 1; key = key.substring(5); }
            else if (key.startsWith("tone3")) { tone = 3; key = key.substring(5); }
            else { return new byte[0]; } // it's a modulator key we can't change
                        
            if ((tone == 0 || tone == 2) && voiceToneACParametersToIndex.containsKey(key))
                {
                int i = ((Integer)(voiceToneACParametersToIndex.get(key))).intValue();

                if (key.endsWith("aftertouchsensitivity")) val = AFTERTOUCH_SENSITIVITY_MAP[val];
                else if (key.endsWith("velocitysensitivity"))  val = VELOCITY_SENSITIVITY_MAP[val];
                else if (key.endsWith("lfotype"))  val = LFO_TYPE_MAP[val];
                else if (key.endsWith("lforate")) val = 127 - val;                                       // reversed
                else if (key.endsWith("volume")) val = 127 - val;                                       // reversed
                else if (key.endsWith("frequencyshift")) val -= 12;                                                                     // 2's complement

                return(new byte[]
                    {
                    (byte)0xF0, 0x43, (byte)(16 + getID()), 0x26, 0x02,
                    (byte)voiceToneAC[0][i], (byte)tone,// ST
                    (byte)voiceToneAC[1][i],                        // F1
                    (byte)voiceToneAC[2][i],                        // F2
                    (byte)voiceToneAC[3][i],                        // B1
                    (byte)voiceToneAC[4][i],                        // B2
                    (byte)((val >>> 7) & 1),                              // V1
                    (byte)(val & 127), (byte)0xF7                       // V2
                    });
                }
            else if ((tone == 1 || tone == 3) && voiceToneBDParametersToIndex.containsKey(key))
                {
                int i = ((Integer)(voiceToneBDParametersToIndex.get(key))).intValue();

                if (key.endsWith("aftertouchsensitivity")) val = AFTERTOUCH_SENSITIVITY_MAP[val];
                else if (key.endsWith("velocitysensitivity"))  val = VELOCITY_SENSITIVITY_MAP[val];
                else if (key.endsWith("lfotype"))  val = LFO_TYPE_MAP[val];
                else if (key.endsWith("lforate")) val = 127 - val;                                       // reversed
                else if (key.endsWith("volume")) val = 127 - val;                                       // reversed
                else if (key.endsWith("level")) val = 127 - val;                                        // reversed
                else if (key.endsWith("frequencyshift")) val -= 12;                                                                     // 2's complement

                return(new byte[]
                    {
                    (byte)0xF0, 0x43, (byte)(16 + getID()), 0x26, 0x02,
                    (byte)voiceToneBD[0][i], (byte)tone,// ST
                    (byte)voiceToneBD[1][i],                        // F1
                    (byte)voiceToneBD[2][i],                        // F2
                    (byte)voiceToneBD[3][i],                        // B1
                    (byte)voiceToneBD[4][i],                        // B2
                    (byte)((val >>> 7) & 1),                              // V1
                    (byte)(val & 127), (byte)0xF7                       // V2
                    });
                }
            else if ((tone == 0 || tone == 2) && voiceEnvelopeACParametersToIndex.containsKey(key))
                {
                int i = ((Integer)(voiceEnvelopeACParametersToIndex.get(key))).intValue();

                if (key.endsWith("egtype")) val = ENVELOPE_TYPE_MAP[val];
                else if (key.endsWith("delayonoff") && val > 0)  val = 0x80;
                else if (key.endsWith("initiallevel")) val = 127 - val;                                 // reversed
                else if (key.endsWith("attacklevel")) val = 127 - val;                                  // reversed
                else if (key.endsWith("decay1level")) val = 127 - val;                                  // reversed
                else if (key.endsWith("decay2level")) val = 127 - val;                                  // reversed
                else if (key.endsWith("levelscaling")) val = val * 16;
                else if (key.endsWith("delay")) val = val * 0x80;

                return(new byte[]
                    {
                    (byte)0xF0, 0x43, (byte)(16 + getID()), 0x26, 0x03,
                    (byte)voiceEnvelopeAC[0][i], (byte)tone,// ST
                    (byte)voiceEnvelopeAC[1][i],                    // F1
                    (byte)voiceEnvelopeAC[2][i],                    // F2
                    (byte)voiceEnvelopeAC[3][i],                    // B1
                    (byte)voiceEnvelopeAC[4][i],                    // B2
                    (byte)((val >>> 7) & 1),                              // V1
                    (byte)(val & 127), (byte)0xF7                   // V2
                    });
                }
            else if ((tone == 4 || tone == 5) && voiceEnvelopeBDParametersToIndex.containsKey(key))
                {
                int i = ((Integer)(voiceEnvelopeBDParametersToIndex.get(key))).intValue();

                if (key.endsWith("egtype")) val = ENVELOPE_TYPE_MAP[val];
                else if (key.endsWith("delayonoff") && val > 0)  val = 0x80;
                else if (key.endsWith("initiallevel")) val = 127 - val;                                 // reversed
                else if (key.endsWith("attacklevel")) val = 127 - val;                                  // reversed
                else if (key.endsWith("decay1level")) val = 127 - val;                                  // reversed
                else if (key.endsWith("decay2level")) val = 127 - val;                                  // reversed
                else if (key.endsWith("levelscaling")) val = val * 16;
                else if (key.endsWith("delay")) val = val * 0x80;
                
                return(new byte[]
                    {
                    (byte)0xF0, 0x43, (byte)(16 + getID()), 0x26, 0x03,
                    (byte)voiceEnvelopeBD[0][i], (byte)(tone == 4 ? 1 : 3),// ST
                    (byte)voiceEnvelopeBD[1][i],                    // F1
                    (byte)voiceEnvelopeBD[2][i],                    // F2
                    (byte)voiceEnvelopeBD[3][i],                    // B1
                    (byte)voiceEnvelopeBD[4][i],                    // B2
                    (byte)((val >>> 7) & 1),                              // V1
                    (byte)(val & 127), (byte)0xF7                   // V2
                    });
                }
            }
        return new byte[0];
        }


    public Object[] emitAll(String key)
        {
        if (key.equals("number")) return new Object[0];  // this is not emittable
        if (key.equals("bank")) return new Object[0];  // this is not emittable
        
        if (key.equals("name"))
            {
            Object[] result = new Object[8];
            
            String name = model.get("name", "UNTITLED") + "          ";

            int pos = 4;            // start of name in voice common array
            for(int i = 0; i < 8; i++)
                {
                result[i] = (new byte[]
                    {
                    (byte)0xF0, 0x43, (byte)(16 + getID()), 0x26, 0x00,
                    (byte)voiceCommon[0][pos + i], 0x00,                  // ST
                    (byte)voiceCommon[1][pos + i],                        // F1
                    (byte)voiceCommon[2][pos + i],                        // F2
                    (byte)voiceCommon[3][pos + i],                        // B1
                    (byte)voiceCommon[4][pos + i],                        // B2
                    (byte)0x00,                                                                                     // V1
                    (byte)(name.charAt(i)), (byte)0xF7                                      // V2
                    });
                }
            return result;
            }
        else 
            {
            byte[] data = send(key);
            if (data.length == 0)
                {
//                System.err.println("Warning (YamahaTG33): Can't emit key " + key);
                return new Object[0];
                }
            else 
                {
                return new Object[] { data };
                }
            }
        }


    // For the time being, we're just doing single-patch parses, not bank parses.
    // They're different for different synths.  Maybe later.
    public int parse(byte[] data, boolean fromFile)
        {
        if (YamahaTG33Rec.recognizeBank(data))
            return parseBank(data, fromFile);
        else
            return parseOne(16, data, fromFile);
        }

    // Bank data is stored as 14 bytes of header,
    // plus 16 blobs of 4 patches each.
    // Each blob is 2 bytes size, then 4 patches concatenated, then 1 byte checksum
    // For Sy22 and SY35 there's also multi data stored afterwards
    // finally there's an F7
    // this is kind of a lie as the first blob has its 2 size bytes embedded earlier in the header,
    // but it'll work for our purposes here.
    //
    // A patch size for TG is 587.  For SY22 and SY35 it's 574
    //
    // Total size for TG is 37631
    // Total size for SY (including multi data) is 38306
        
    int[] blobs = new int[16];
    public int parseBank(byte[] data, boolean fromFile)
        {
        //      Extract names
        String[] names = new String[64];
        int[] patches = new int[64];
                
        boolean isTG = YamahaTG33Rec.recognizeTGBank(data);
                
        int pos = 14;
        for(int i = 0; i < 16; i++)
            {
            blobs[i] = pos;
            // Advance 2 for the data bytes
            pos += 2;
            // patches are in blobs of 4
            for(int ii = i * 4; ii < (i + 1) * 4; ii++)
                {
                char[] n = new char[8];
                for(int p = 0; p < 8; p++)
                    {
                    n[p] = (char)data[pos + (isTG ? 12 : 3) + p];           // name is offset by 12 from pos in TG, 2 in SY
                    }
                names[ii] = new String(n);
                patches[ii] = pos;
                if (isTG) 
                    pos += 587;
                else
                    pos += 574;             // I think?
                }
            // Advance one more for the blob checksum
            pos += 1;
            }
                                
        // Now that we have an array of names, one per patch, we present the user with options;
        // 0. Cancel [handled automatically]
        // 1. Save the bank data [handled automatically]
        // 2. Upload the bank data [handled automatically] 
        // 3. Load and edit a certain patch number
        int patchNum = showBankSysexOptions(data, names);
        if (patchNum < 0) 
            return PARSE_CANCELLED;

        model.set("name", new String(names[patchNum]));
        model.set("number", patchNum);
        model.set("bank", 0);                   // we don't know what the bank is in reality
                        
        // okay, we're loading and editing patch number patchNum.  Here we go.
        return parseOne(patches[patchNum], data, fromFile);
        }

    public static final int BANK_PAUSE_INTERVAL = 100;
    public Object adjustBankSysexForEmit(byte[] data, Model model, int bank) 
        { 
        if (getSynthType() == TYPE_TG33)
            data[2] = (byte)(getID());
        else
            data[2] = (byte)(getChannelOut());
            
        int[] p = new int[15];
        p[0] = blobs[1];
        for(int i = 1; i < p.length; i++)
            {
            p[i] = blobs[i + 1] - blobs[i];
            }
                
        // At this point each value in P should contain the SIZE of the chunk except the final chunk.
        int pos = 0;
        byte[][] sysex = new byte[16][];
        for(int i = 0; i < sysex.length - 1; i++)
            {
            sysex[i] = new byte[p[i]];
            System.arraycopy(data, pos, sysex[i], 0, p[i]);
            pos += p[i];
            }
        sysex[sysex.length - 1] = new byte[data.length - pos];
        System.arraycopy(data, pos, sysex[sysex.length - 1], 0, data.length - pos);
                
        // Now we need to build the divided sysex with spaces in-between
        Midi.DividedSysex[] div = Midi.DividedSysex.create(sysex);
        Object[] d = new Object[div.length * 2 - 1];
        for(int i = 0; i < div.length; i++)
            {
            d[i * 2] = div[i];
            if (i != div.length - 1)
                d[i * 2 + 1] = new Integer(BANK_PAUSE_INTERVAL);
            }
        return d;
        }

    public JComponent getAdditionalBankSysexOptionsComponents(byte[] data, String[] names)
        {
        if (YamahaTG33Rec.recognizeTGBank(data))
            {
            VBox vbox = new VBox();
            vbox.add(Strut.makeVerticalStrut(10));
            vbox.add(new JLabel("This is a TG-33 Bank Sysex file.  You can't write it to the SY-22/35."));
            return vbox;
            }
        else
            {
            VBox vbox = new VBox();
            vbox.add(Strut.makeVerticalStrut(10));
            vbox.add(new JLabel("This is an SY-22/35 Bank Sysex file.  This file contains multimode"));
            vbox.add(new JLabel("patches as well as single patches.  If you write it to your SY-22/35,"));
            vbox.add(new JLabel("it will overwrite your multimode patches as well.  You can write this"));
            vbox.add(new JLabel("file to your TG-33, but it will only respond to the single patches"));
            vbox.add(new JLabel("in the file, not the multimode patches"));
            return vbox;
            }
        }


    /// One gizmo I've added is the ability to ONLY parse the vector from a patch.  This allows the user
    /// to create a new vector on the machine, then upload it and merge it into his existing patch that
    /// he's editing.  Not sure how useful that is, but...
        
    public int parseOne(int pos, byte[] data, boolean fromFile)
        {
        byte a;
        byte b;
        byte c;
        
        if (isParsingVectorOnly)
            {
            if (YamahaTG33Rec.recognizeTG(data))
                pos += 0xBB;
            else
                pos += 174;             // 0xBB - 13.  The difference in Voice common lengths is 13
            }
        else
            {        
            //// COMMON
            if (YamahaTG33Rec.recognizeTG(data) || YamahaTG33Rec.recognizeTGBank(data))
                {
                b = data[pos++];
                model.set("configuration", (b >>> 0) & 1);              // "2/4"
                // model.set("DUNNO", (b >>> 1) & 1);           // DRM  // dunno
                b = data[pos++];
                model.set("effecttype", (b >>> 0) & 15);                // "EFFECT"
                b = data[pos++];
                model.set("effectbalance", (b >>> 0) & 127);            // EFFECT BALANCE
                pos+=3; // skip
                b = data[pos++];
                model.set("effectsendlevel", (b >>> 0) & 127);          // EFFECT SEND
                pos+=5; // skip
                char[] name = new char[8];
                for(int i = 0; i < name.length; i++)
                    name[i] = (char)(data[pos++] & 127);
                model.set("name", new String(name));
                b = data[pos++];
                model.set("pitchbendrange", (b >>> 0) & 15);            // PITCH BEND R
                        
                // clearing:
                model.set("effectsenddepth", 0);
                }
            else
                {
                pos++;          // the first byte is  0 0 0 0 0 0 1
                pos++;          // the second byte is 0 1 0 0 1 0 1
                b = data[pos++];
                model.set("effecttype", (b >>> 0) & 15);                // EFFECT
                model.set("effectsenddepth", (b >>> 4) & 7);                // DEPTH
                char[] name = new char[8];
                for(int i = 0; i < name.length; i++)
                    name[i] = (char)(data[pos++] & 127);
                model.set("name", new String(name));
                a = data[pos++];
                b = data[pos++];
                model.set("configuration", (a >>> 0) & 1);              // "2/4"
                model.set("pitchbendrange", (b >>> 0) & 15);            // PITCH BEND R

                // clearing:
                model.set("effectbalance", 0);
                model.set("effectsendlevel", 0);
                }
        
            b = data[pos++];
            model.set("modulationwheelam", (b >>> 0) & 1);          // PIT -WHEEL- AM
            model.set("modulationwheelpm", (b >>> 1) & 1);          // PIT -WHEEL- PM
            // model.set("DUNNO", (b >>> 2) & 1);                                   // PIT -WHEEL- TYP  // dunno
            model.set("aftertoucham", (b >>> 4) & 1);               // AFTER TUCH AM
            model.set("aftertouchpm", (b >>> 5) & 1);               // AFTER TUCH PM  
            model.set("aftertouchlevel", (b >>> 6) & 1);            // AFTER TUCH LEV  
            a = data[pos++];
            b = data[pos++];
            c = (byte)((a << 7) | b);                                                                   // AFTER PITCH
            model.set("pitchbias", c + 12);                                                     // 2's complement, 12 off
            b = data[pos++];
            model.set("egdelayrate", (b >>> 0) & 127);              // EG DELAY RATE
            a = data[pos++];
            b = data[pos++];
            c = (byte)((a << 7) | b);                                                                   // COMMON ENV. ATTACK
            model.set("egattackrate", c + 63);                                          // 2's complement, 63 off               
            a = data[pos++];
            b = data[pos++];
            c = (byte)((a << 7) | b);                                                                   // COMMON ENV. RELEASE
            model.set("egreleaserate", c + 63);                                         // 2's complement 63 off
                
                
            // TONES
            
            pos = parseAC(TONE_A, data, pos);
            pos = parseBD(TONE_B, data, pos);
            pos = parseAC(TONE_C, data, pos);
            pos = parseBD(TONE_D, data, pos);
                
                
            // VECTOR COMMON
                
            b = data[pos++];
            model.set("levelspeed", (b >>> 0) & 127);               // LEVEL SPEED
            b = data[pos++];
            model.set("detunespeed", (b >>> 0) & 127);              // DETUNE SPEED
                
            }
        
        // VECTOR
        
        for(int i = 0; i < 50; i++)
            {
            a = data[pos++];
            b = data[pos++];
            int val = ((a & 127) << 7) | (b & 127);
            if (i == 0 && val == 255) val = 254;
            model.set("level" + (i + 1) + "time", val);                                                 // LEVEL TIME INTERVAL STEP
            b = data[pos++];
            model.set("level" + (i + 1) + "xaxis", (b >>> 0) & 127);                // LEVEL X-axis
            b = data[pos++];
            model.set("level" + (i + 1) + "yaxis", (b >>> 0) & 127);                // LEVEL Y-axis
            }
        for(int i = 0; i < 50; i++)
            {
            a = data[pos++];
            b = data[pos++];
            int val = ((a & 127) << 7) | (b & 127);
            if (i == 0 && val == 255) val = 254;
            model.set("detune" + (i + 1) + "time", val);                                                // DETUNE TIME INTERVAL STEP
            b = data[pos++];
            model.set("detune" + (i + 1) + "xaxis", (b >>> 0) & 127);               // DETUNE X-axis
            b = data[pos++];
            model.set("detune" + (i + 1) + "yaxis", (b >>> 0) & 127);               // DETUNE Y-axis
            }
        
        revise();
        
        return PARSE_SUCCEEDED;  
        }

    public int parseAC(int tone, byte[] data, int pos)
        {
        byte a;
        byte b;
        byte c;
        
        String t = "tone" + tone;
        b = data[pos++];
        model.set(t + "wavetype", (b >>> 0) & 127);                                                     // WAVE NO.
        a = data[pos++];
        b = data[pos++];
        c = (byte)((a << 7) | b);                                                                       // FREQUENCY SHIFT
        model.set(t + "frequencyshift", c + 12);                                                                // 2's complement, 12 offf
        b = data[pos++];
        c = (byte)((b >>> 0) & 15);                                                                             // VELOCITY TYP
        c = findOn(VELOCITY_SENSITIVITY_MAP, c);
        model.set(t + "velocitysensitivity", c);
        c = (byte)((b >>> 4) & 7);                                                                                      // AFTER SNS
        c = findOn(AFTERTOUCH_SENSITIVITY_MAP, (byte)(c * 16));
        model.set(t + "aftertouchsensitivity", c); 
        a = data[pos++];
        b = data[pos++];
        model.set(t + "lfospeed", (b >>> 0) & 31);                         // LFO SPEED
        c = (byte)(((b >>> 5) & 3) | ((a & 1) << 2));
        c = findOn(LFO_TYPE_MAP, (byte)(c * 32));
        model.set(t + "lfotype", c);                                                                    // LFO TYP
        a = data[pos++];
        b = data[pos++];
        model.set(t + "lfodelay", (a << 7) | b);                        // LFO DELAY TIME
        a = data[pos++];
        b = data[pos++];
        model.set(t + "lforate", 127 - ((a << 7) | b)); // LFO DELAY RATE
        b = data[pos++];
        model.set(t + "lfoam", (b >>> 0) & 15);         // AM DEPTH
        model.set(t + "amplitudemod", (b >>> 4) & 1);           // AM
        b = data[pos++];
        model.set(t + "lfopm", (b >>> 0) & 31);         // PM DEPTH
        model.set(t + "pitchmod", (b >>> 5) & 1);               // PM
        b = data[pos++];
        model.set(t + "pan", (b >>> 0) & 7);                   // PAN
        c = (byte)(((b >>> 4) & 15) * 16);
        c = findOn(ENVELOPE_TYPE_MAP, c);
        model.set(t + "egtype", c);                // EG TYPE
        b = data[pos++];
        model.set(t + "volume", 127 - ((b >>> 0) & 127));                     // VOLUME
        b = data[pos++];
        model.set(t + "detune", ((b >>> 0) & 15));                      // DT1
        model.set(t + "temperment", ((b >>> 4) & 3));           // DT2
        a = data[pos++];
        b = data[pos++];
        model.set(t + "ratescaling", ((b >>> 0) & 15));         // RATE SCALING
        model.set(t + "levelscaling", (a << 3) | ((b >>> 4) & 7));              // LEVEL SCALING
        a = data[pos++];
        b = data[pos++];
        model.set(t + "attackrate", ((b >>> 0) & 63));          // EG AR
        model.set(t + "delayonoff", ((a >>> 0) & 1));           // DLAY
        a = data[pos++];
        b = data[pos++];
        model.set(t + "decay1rate", ((b >>> 0) & 63));          // EG D1R
//              { int max = (a << 1) | (b >>> 6); }                                     // MAX or EG MAX -- we're ignoring this one             
        b = data[pos++];
        model.set(t + "decay2rate", ((b >>> 0) & 63));          // EG D2R
        b = data[pos++];
        model.set(t + "releaserate", ((b >>> 0) & 63));         // EG RR
        b = data[pos++];
        model.set(t + "initiallevel", 127 - ((b >>> 0) & 127));         // EG IL
        b = data[pos++];
        model.set(t + "attacklevel", 127 - ((b >>> 0) & 127));          // EG AL
        b = data[pos++];
        model.set(t + "decay1level", 127 - ((b >>> 0) & 127));          // EG D1L
        b = data[pos++];
        model.set(t + "decay2level", 127 - ((b >>> 0) & 127));          // EG D2L
        
        if (YamahaTG33Rec.recognizeTG(data) || YamahaTG33Rec.recognizeTGBank(data))
            {
            pos += 2;
            }
                
        return pos;
        }
        
    public int parseBD(int tone, byte[] data, int pos)
        {
        byte a;
        byte b;
        byte c;
        
        String t = "tone" + tone;
        a = data[pos++];
        b = data[pos++];
        c = (byte)((a << 7) | b);                                                                       // FREQUENCY SHIFT
        model.set(t + "wavetype", c & 255);                         // WAVE NO.
        a = data[pos++];
        b = data[pos++];
        c = (byte)((a << 7) | b);                                                                       // FREQUENCY SHIFT
        model.set(t + "frequencyshift", c + 12);                                                // 2's complement, 12 off
        b = data[pos++];
        c = (byte)((b >>> 0) & 15);                                                                             // VELOCITY TYP
        c = findOn(VELOCITY_SENSITIVITY_MAP, c);
        model.set(t + "velocitysensitivity", c);
        c = (byte)((b >>> 4) & 7);                                                                                      // AFTER SNS
        c = findOn(AFTERTOUCH_SENSITIVITY_MAP, (byte)(c * 16));
        model.set(t + "aftertouchsensitivity", c); 
        a = data[pos++];
        b = data[pos++];
        model.set(t + "lfospeed", (b >>> 0) & 31);                                            // LFO SPEED
        c = (byte)(((b >>> 5) & 3) | ((a & 1) << 2));
        c = findOn(LFO_TYPE_MAP, (byte)(c * 32));
        model.set(t + "lfotype", c);                                                            // LFO TYP
        a = data[pos++];
        b = data[pos++];
        model.set(t + "lfodelay", (a << 7) | b);                        // LFO DELAY TIME
        a = data[pos++];
        b = data[pos++];
        model.set(t + "lforate", 127 - ((a << 7) | b)); // LFO DELAY RATE
        b = data[pos++];
        model.set(t + "lfoam", (b >>> 0) & 15);         // AM DEPTH
        model.set(t + "1amplitudemod", (b >>> 4) & 1);          // AM or MAM
        model.set(t + "0amplitudemod", (b >>> 5) & 1);          // CAM -- only appears in SY35 manual
        b = data[pos++];
        model.set(t + "lfopm", (b >>> 0) & 31);         // PM DEPTH
        model.set(t + "1pitchmod", (b >>> 5) & 1);              // PM or MPM
        model.set(t + "0pitchmod", (b >>> 6) & 1);              // CPM -- only appears in SY35 manual
        b = data[pos++];
        model.set(t + "pan", (b >>> 0) & 7);                   // PAN
        c = (byte)(((b >>> 4) & 15) * 16);
        c = findOn(ENVELOPE_TYPE_MAP, c);
        model.set(t + "egtype", c);                                     // EG TYPE
        b = data[pos++];
        model.set(t + "feedback", (b >>> 0) & 7);               // FEEDBACK
        model.set(t + "algorithm", (b >>> 4) & 1);              // CONNECT -- note only 1 bit is used?
        a = data[pos++];
        b = data[pos++];
        model.set(t + "1fixed", (a >>> 0) & 1);                         // MFX          
        model.set(t + "1frequencyratio", (b >>> 0) & 15);       // M MULTI
        model.set(t + "1wave", (b >>> 4) & 7);                          // M WAVE
        b = data[pos++];
        model.set(t + "level", 127 - ((b >>> 0) & 127));                      // TONE LEVEL           // note no "1"
        b = data[pos++];
        model.set(t + "1detune", ((b >>> 0) & 15));                     // M DT1
        model.set(t + "1temperment", ((b >>> 4) & 3));          // M DT2
        a = data[pos++];
        b = data[pos++];
        model.set(t + "1ratescaling", ((b >>> 0) & 15));                // M RATE SCALING
        model.set(t + "1levelscaling", (a << 3) | ((b >>> 4) & 7));             // M LEVEL SCALING
        a = data[pos++];
        b = data[pos++];
        model.set(t + "1attackrate", ((b >>> 0) & 63));         // M EG AR
        model.set(t + "1delayonoff", ((a >>> 0) & 1));          // MDY
        a = data[pos++];
        b = data[pos++];
        model.set(t + "1decay1rate", ((b >>> 0) & 63));         // M EG D1R
//              { int max = (a << 1) | (b >>> 6); }                                     // MAX or EG MAX -- we're ignoring this one             
        b = data[pos++];
        model.set(t + "1decay2rate", ((b >>> 0) & 63));         // M EG D2R
        b = data[pos++];
        model.set(t + "1releaserate", ((b >>> 0) & 63));                // M EG RR
        b = data[pos++];
        model.set(t + "1initiallevel", 127 - ((b >>> 0) & 127));        // M EG IL
        b = data[pos++];
        model.set(t + "1attacklevel", 127 - ((b >>> 0) & 127));         // M EG AL
        b = data[pos++];
        model.set(t + "1decay1level", 127 - ((b >>> 0) & 127));         // M EG D1L
        b = data[pos++];
        model.set(t + "1decay2level", 127 - ((b >>> 0) & 127));         // M EG D2L
        a = data[pos++];
        b = data[pos++];
        model.set(t + "0fixed", (a >>> 0) & 1);                         // CFX          
        model.set(t + "0frequencyratio", (b >>> 0) & 15);               // C MULTI
        model.set(t + "0wave", (b >>> 4) & 7);                          // C WAVE
        b = data[pos++];
        model.set(t + "volume", 127 - ((b >>> 0) & 127));                     // VOLUME               // not no "0"
        b = data[pos++];
        model.set(t + "0detune", ((b >>> 0) & 15));                     // C DT1
        model.set(t + "0temperment", ((b >>> 4) & 3));          // C DT2
        a = data[pos++];
        b = data[pos++];
        model.set(t + "0ratescaling", ((b >>> 0) & 15));                // C RATE SCALING
        model.set(t + "0levelscaling", (a << 3) | ((b >>> 4) & 7));             // C LEVEL SCALING
        a = data[pos++];
        b = data[pos++];
        model.set(t + "0attackrate", ((b >>> 0) & 63));         // C EG AR
        model.set(t + "0delayonoff", ((a >>> 0) & 1));          // CDY
        a = data[pos++];
        b = data[pos++];
        model.set(t + "0decay1rate", ((b >>> 0) & 63));         // C EG D1R
//              { int max = (a << 1) | (b >>> 6); }                                     // MAX or EG MAX -- we're ignoring this one             
        b = data[pos++];
        model.set(t + "0decay2rate", ((b >>> 0) & 63));         // C EG D2R
        b = data[pos++];
        model.set(t + "0releaserate", ((b >>> 0) & 63));                // C EG RR
        b = data[pos++];
        model.set(t + "0initiallevel", 127 - ((b >>> 0) & 127));                // C EG IL
        b = data[pos++];
        model.set(t + "0attacklevel", 127 - ((b >>> 0) & 127));         // C EG AL
        b = data[pos++];
        model.set(t + "0decay1level", 127 - ((b >>> 0) & 127));         // C EG D1L
        b = data[pos++];
        model.set(t + "0decay2level", 127 - ((b >>> 0) & 127));         // C EG D2L             pos += 2;

        if (YamahaTG33Rec.recognizeTG(data) || YamahaTG33Rec.recognizeTGBank(data))
            {
            pos += 2;
            }
                        
        return pos;
        }
                

    public Object[] emitAll(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        if (tempModel == null)
            tempModel = getModel();

        byte[] data = new byte[((getSynthType() == TYPE_TG33) ? 605 : 592)];
        boolean[] overflow = new boolean[data.length];
        
        int len = data.length - 8;      // minus header, minus checksum, minus F7
        
        data[0] = (byte)0xF0;
        data[1] = (byte)0x43;

        if (getSynthType() == TYPE_TG33)
            {
            data[2] = (byte)(getID());
            data[3] = (byte)0x7E;
            data[4] = (byte)(len / 128);
            data[5] = (byte)(len % 128);
            data[6] = (byte)'L';
            data[7] = (byte)'M';
            data[8] = (byte)' ';
            data[9] = (byte)' ';
            data[10] = (byte)'0';
            data[11] = (byte)'0';
            data[12] = (byte)'1';
            data[13] = (byte)'2';
            data[14] = (byte)'V';
            data[15] = (byte)'E';
            }
        else
            {
            data[2] = (byte)(getChannelOut());
            data[3] = (byte)0x7E;
            data[4] = (byte)(len / 128);
            data[5] = (byte)(len % 128);
            data[6] = (byte)'P';
            data[7] = (byte)'K';
            data[8] = (byte)' ';
            data[9] = (byte)' ';
            data[10] = (byte)'2';
            data[11] = (byte)'2';
            data[12] = (byte)'0';
            data[13] = (byte)'3';
            data[14] = (byte)'A';
            data[15] = (byte)'E';
            }
        
        int pos = 16;
        int val;
        
        //// COMMON
                
        if (getSynthType() == TYPE_TG33)
            {
            int unknown_drm = 0;
            data[pos++] = (byte)((unknown_drm << 1) | model.get("configuration"));
            data[pos++] = (byte)(model.get("effecttype"));
            data[pos++] = (byte)(model.get("effectbalance"));
            pos += 3;  // skip
            data[pos++] = (byte)(model.get("effectsendlevel"));
            pos += 5;       // skip
            String nm = model.get("name", "Untitled") + "        ";
            for(int i = 0; i < 8; i++)
                data[pos++] = (byte)(nm.charAt(i));
            data[pos++] = (byte)(model.get("pitchbendrange"));
            }
        else
            {
            overflow[pos] = true;
            data[pos++] = 1;                // the first byte is  0 0 0 0 0 0 1
            data[pos++] = 37;               // the second byte is 0 1 0 0 1 0 1
            data[pos++] = (byte)((model.get("effectsenddepth") << 4) | model.get("effecttype"));
            String nm = model.get("name", "Untitled") + "        ";
            for(int i = 0; i < 8; i++)
                data[pos++] = (byte)(nm.charAt(i));
            overflow[pos] = true;
            data[pos++] = (byte)(model.get("configuration"));                   // MSB
            data[pos++] = (byte)(model.get("pitchbendrange"));
            }
        
        int pit_type_dunno = 0;
        data[pos++] = (byte)((model.get("modulationwheelam") << 0) |
            (model.get("modulationwheelpm") << 1) | 
            (pit_type_dunno << 2) | 
            (model.get("aftertoucham") << 4) | 
            (model.get("aftertouchpm") << 5) | 
            (model.get("aftertouchlevel") << 6)); 
        val = model.get("pitchbias") - 12;
        overflow[pos] = true;
        data[pos++] = (byte)((val & 255) / 128);                                        // MSB
        data[pos++] = (byte)((val & 255) % 128);
        data[pos++] = (byte)(model.get("egdelayrate"));
        val = model.get("egattackrate") - 63;
        overflow[pos] = true;
        data[pos++] = (byte)((val & 255) / 128);                                        // MSB
        data[pos++] = (byte)((val & 255) % 128);
        val = model.get("egreleaserate") - 63;
        overflow[pos] = true;
        data[pos++] = (byte)((val & 255) / 128);                                        // MSB
        data[pos++] = (byte)((val & 255) % 128);
                                
        pos = emitAC(TONE_A, data, overflow, pos);
        pos = emitBD(TONE_B, data, overflow, pos);
        pos = emitAC(TONE_C, data, overflow, pos);
        pos = emitBD(TONE_D, data, overflow, pos);
                
        // VECTOR COMMON
                
        data[pos++] = (byte)(model.get("levelspeed"));
        data[pos++] = (byte)(model.get("detunespeed"));
        
        
        // VECTOR
        
        for(int i = 0; i < 50; i++)
            {
            val = model.get("level" + (i + 1) + "time");
            if (i == 0 && val == 254) val = 255;
            overflow[pos] = true;
            data[pos++] = (byte)((val & 255) / 128);                            // MSB
            data[pos++] = (byte)((val & 255) % 128);
            data[pos++] = (byte)(model.get("level" + (i + 1) + "xaxis"));
            data[pos++] = (byte)(model.get("level" + (i + 1) + "yaxis"));
            }
        for(int i = 0; i < 50; i++)
            {
            val = model.get("detune" + (i + 1) + "time");
            if (i == 0 && val == 254) val = 255;
            overflow[pos] = true;
            data[pos++] = (byte)((val & 255) / 128);                            // MSB
            data[pos++] = (byte)((val & 255) % 128);
            data[pos++] = (byte)(model.get("detune" + (i + 1) + "xaxis"));
            data[pos++] = (byte)(model.get("detune" + (i + 1) + "yaxis"));
            }

        // for SY data, insert the "8-bit" checksum
        if (getSynthType() != TYPE_TG33)
            {
            pos++;
            byte bb = produce8BitChecksum(data, overflow, 6, pos);
            data[pos++] = (byte)((bb >>> 7) & 1);
            data[pos++] = (byte)(bb & 127);
            }
        
        // Now the standard checksum
        data[data.length - 2] = produceChecksum(data, 6, data.length - 2);
        data[data.length - 1] = (byte)0xF7;
        
        return new Object[] { data };           // this is Object[] instead of byte[] because I was experimenting with pauses before and after during debugging        
        }

    public int emitAC(int tone, byte[] data, boolean[] overflow, int pos)
        {
        int val;
        int val2;
        
        String t = "tone" + tone;
        data[pos++] = (byte)(model.get(t + "wavetype"));
        overflow[pos] = true;
        data[pos++] = (byte)(((model.get(t + "frequencyshift") - 12) & 255) / 128);             // MSB
        data[pos++] = (byte)(((model.get(t + "frequencyshift") - 12) & 255) % 128);
        val = model.get(t + "aftertouchsensitivity");
        val = (AFTERTOUCH_SENSITIVITY_MAP[val] & 255) / 16;
        val2 = model.get(t + "velocitysensitivity");
        val2 = (VELOCITY_SENSITIVITY_MAP[val2] & 255);
        data[pos++] = (byte)((val << 4) | val2);
        val = model.get(t + "lfotype") ;
        val = (LFO_TYPE_MAP[val] & 255) / 32;
        overflow[pos] = true;
        data[pos++] = (byte)(val / 4);                                                                                                  // MSB
        data[pos++] = (byte)(((val % 4) << 5) | model.get(t + "lfospeed"));
        overflow[pos] = true;
        data[pos++] = (byte)((model.get(t + "lfodelay") / 128));                                                // MSB
        data[pos++] = (byte)((model.get(t + "lfodelay") % 128));
        overflow[pos] = true;
        data[pos++] = (byte)(((127 - (model.get(t + "lforate")) & 255) / 128));                 // MSB
        data[pos++] = (byte)(((127 - (model.get(t + "lforate")) & 255) % 128));
        data[pos++] = (byte)((model.get(t + "amplitudemod") << 4) | model.get(t + "lfoam"));
        data[pos++] = (byte)((model.get(t + "pitchmod") << 5) | model.get(t + "lfopm"));
        val = ENVELOPE_TYPE_MAP[model.get(t + "egtype")] / 16;
        data[pos++] = (byte)((val << 4) | model.get(t + "pan"));
        val = 127 - model.get(t + "volume"); 
        data[pos++] = (byte)val;
        data[pos++] = (byte)((model.get(t + "temperment") << 4) | model.get(t + "detune"));
        overflow[pos] = true;
        data[pos++] = (byte)(model.get(t + "levelscaling") / 8);                                                // MSB
        data[pos++] = (byte)(((model.get(t + "levelscaling") % 8) << 4) | model.get(t + "ratescaling"));
        overflow[pos] = true;
        data[pos++] = (byte)(model.get(t + "delayonoff"));               // note MSB
        data[pos++] = (byte)(model.get(t + "attackrate"));               // note LSB
        
        // "MAX" is the top two bits.  According to "SY-22 / SY-35 MIDI System Exclusive" by Matt Thorman,
        // documentation accompanying Sy-Edit, it's the peak level (initial=0, attack=1, decay 1=2, decay 2=3)
        // It's also worth mentioning that it appears that the SY-22/35 breaks ties by the later level
                
        int max = 0;
        val = model.get(t + "initiallevel");
        if (model.get(t + "attacklevel") >= val) { max = 1; val = model.get(t + "attacklevel"); }
        if (model.get(t + "decay1level") >= val) { max = 2; val = model.get(t + "decay1level"); }
        if (model.get(t + "decay2level") >= val) { max = 3; }
        overflow[pos] = true;

        data[pos++] = (byte)((max >>> 1) & 1);                                                                          // MSB of MAX
        data[pos++] = (byte)(((max & 1) << 6) | model.get(t + "decay1rate"));   // LSB of MAX etc.
        data[pos++] = (byte)(model.get(t + "decay2rate"));
        data[pos++] = (byte)(model.get(t + "releaserate"));
        data[pos++] = (byte)(127 - model.get(t + "initiallevel"));
        data[pos++] = (byte)(127 - model.get(t + "attacklevel"));
        data[pos++] = (byte)(127 - model.get(t + "decay1level"));
        data[pos++] = (byte)(127 - model.get(t + "decay2level"));
        if (getSynthType() == TYPE_TG33) pos += 2;                      // Not the case for SY35?
        return pos;
        }
        
    public static String toHex(int val)
        {
        return String.format("0x%08X", val);
        }


    public int emitBD(int tone, byte[] data, boolean[] overflow, int pos)
        {
        int val;
        int val2;
        
        String t = "tone" + tone;
        overflow[pos] = true;
        data[pos++] = (byte)((model.get(t + "wavetype")) / 128);                                                // MSB
        data[pos++] = (byte)((model.get(t + "wavetype")) % 128);
        overflow[pos] = true;
        data[pos++] = (byte)(((model.get(t + "frequencyshift") - 12) & 255) / 128);             // MSB
        data[pos++] = (byte)(((model.get(t + "frequencyshift") - 12) & 255) % 128);
        val = model.get(t + "aftertouchsensitivity");
        val = (AFTERTOUCH_SENSITIVITY_MAP[val] & 255) / 16;
        val2 = model.get(t + "velocitysensitivity");
        val2 = (VELOCITY_SENSITIVITY_MAP[val2] & 255);
        data[pos++] = (byte)((val << 4) | val2);
        val = model.get(t + "lfotype") ;
        val = (LFO_TYPE_MAP[val] & 255) / 32;
        overflow[pos] = true;
        data[pos++] = (byte)(val / 4);                                                                                                  // MSB
        data[pos++] = (byte)(((val % 4) << 5) | model.get(t + "lfospeed"));
        overflow[pos] = true;
        data[pos++] = (byte)((model.get(t + "lfodelay") / 128));                                                // MSB
        data[pos++] = (byte)((model.get(t + "lfodelay") % 128));
        overflow[pos] = true;
        data[pos++] = (byte)(((127 - (model.get(t + "lforate")) & 255) / 128));                 // MSB
        data[pos++] = (byte)(((127 - (model.get(t + "lforate")) & 255) % 128));
        data[pos++] = (byte)((model.get(t + "0amplitudemod") << 5) | (model.get(t + "1amplitudemod") << 4) | model.get(t + "lfoam"));
        data[pos++] = (byte)((model.get(t + "0pitchmod") << 6) | (model.get(t + "1pitchmod") << 5) | model.get(t + "lfopm"));
        val = ENVELOPE_TYPE_MAP[model.get(t + "egtype")] / 16;
        data[pos++] = (byte)((val << 4) | model.get(t + "pan"));
        data[pos++] = (byte)((model.get(t + "algorithm") << 4) | model.get(t + "feedback"));
        overflow[pos] = true;
        data[pos++] = (byte)model.get(t + "1fixed");                                            // MSB
        data[pos++] = (byte)((model.get(t + "1wave") << 4) | model.get(t + "1frequencyratio"));         // LSB
        val = 127 - model.get(t + "level");
        data[pos++] = (byte)val;
        data[pos++] = (byte)((model.get(t + "1temperment") << 4) | model.get(t + "1detune"));
        overflow[pos] = true;
        data[pos++] = (byte)(model.get(t + "1levelscaling") / 8);                                               // MSB
        data[pos++] = (byte)(((model.get(t + "1levelscaling") % 8) << 4) | model.get(t + "1ratescaling"));
        overflow[pos] = true;
        data[pos++] = (byte)(model.get(t + "1delayonoff"));              // note MSB
        data[pos++] = (byte)(model.get(t + "1attackrate"));              // note LSB
        
        // "MAX" is the top two bits.  According to "SY-22 / SY-35 MIDI System Exclusive" by Matt Thorman,
        // documentation accompanying Sy-Edit, it's the peak level (initial=0, attack=1, decay 1=2, decay 2=3)
        // It's also worth mentioning that it appears that the SY-22/35 breaks ties by the later level

        int max = 0;
        val = model.get(t + "1initiallevel");
        if (model.get(t + "1attacklevel") > val) { max = 1; val = model.get(t + "1attacklevel"); }
        if (model.get(t + "1decay1level") > val) { max = 2; val = model.get(t + "1decay1level"); }
        if (model.get(t + "1decay2level") > val) { max = 3; }
        overflow[pos] = true;
        data[pos++] = (byte)((max >>> 1) & 1);                                                                          // MSB of MAX
        data[pos++] = (byte)(((max & 1) << 6) | model.get(t + "1decay1rate"));   // LSB of MAX etc.
        
        data[pos++] = (byte)(model.get(t + "1decay2rate"));
        data[pos++] = (byte)(model.get(t + "1releaserate"));
        data[pos++] = (byte)(127 - model.get(t + "1initiallevel"));
        data[pos++] = (byte)(127 - model.get(t + "1attacklevel"));
        data[pos++] = (byte)(127 - model.get(t + "1decay1level"));
        data[pos++] = (byte)(127 - model.get(t + "1decay2level"));
        overflow[pos] = true;
        data[pos++] = (byte)model.get(t + "0fixed");                        // MSB
        data[pos++] = (byte)((model.get(t + "0wave") << 4) | model.get(t + "0frequencyratio"));         // LSB
        val = 127 - model.get(t + "volume");
        data[pos++] = (byte)val;        
        data[pos++] = (byte)((model.get(t + "0temperment") << 4) | model.get(t + "0detune"));
        overflow[pos] = true;
        data[pos++] = (byte)(model.get(t + "0levelscaling") / 8);                                       // MSB
        data[pos++] = (byte)(((model.get(t + "0levelscaling") % 8) << 4) | model.get(t + "0ratescaling"));
        overflow[pos] = true;
        data[pos++] = (byte)(model.get(t + "0delayonoff"));              // note MSB
        data[pos++] = (byte)(model.get(t + "0attackrate"));              // note LSB

        // "MAX" is the top two bits.  According to "SY-22 / SY-35 MIDI System Exclusive" by Matt Thorman,
        // documentation accompanying Sy-Edit, it's the peak level (initial=0, attack=1, decay 1=2, decay 2=3)
        // It's also worth mentioning that it appears that the SY-22/35 breaks ties by the later level

        max = 0;
        val = model.get(t + "0initiallevel");
        if (model.get(t + "0attacklevel") >= val) { max = 1; val = model.get(t + "0attacklevel"); }
        if (model.get(t + "0decay1level") >= val) { max = 2; val = model.get(t + "0decay1level"); }
        if (model.get(t + "0decay2level") >= val) { max = 3; }
        overflow[pos] = true;
        data[pos++] = (byte)((max >>> 1) & 1);                                                                          // MSB of MAX
        data[pos++] = (byte)(((max & 1) << 6) | model.get(t + "0decay1rate"));   // LSB of MAX etc.
        
        data[pos++] = (byte)(model.get(t + "0decay2rate"));
        data[pos++] = (byte)(model.get(t + "0releaserate"));
        data[pos++] = (byte)(127 - model.get(t + "0initiallevel"));
        data[pos++] = (byte)(127 - model.get(t + "0attacklevel"));
        data[pos++] = (byte)(127 - model.get(t + "0decay1level"));
        data[pos++] = (byte)(127 - model.get(t + "0decay2level"));
        if (getSynthType() == TYPE_TG33) pos += 2;                      // Not the case for SY35?
        return pos;
        }

    byte produce8BitChecksum(byte[] bytes, boolean[] overflow, int start, int end)
        {
        int checksum = 0;
        for(int i = start; i < end; i++)
            {
            if (overflow[i])
                {
                checksum = (checksum + (bytes[i] * 128));
                }
            else
                {
                checksum = (checksum + bytes[i]) & 255;
                }
            }
        return (byte)((- checksum) & 255);
        }

    byte produceChecksum(byte[] bytes, int start, int end)
        {
        //              Presume it's the same procedure as 4-op.
        //      The TX816 owner's manual (p. 54) says the checksum is:
        //                              "the least significant 7 bits of the 2's complement sum of 155 data bytes.
        //                               0eeeeeee must be determined so that the least significant 7 bits of the
        //                              sum of the 155 data bytes and checksum equal zero."

        int checksum = 0;
        for(int i = start; i < end; i++)
            checksum = (checksum + bytes[i]) & 127;
        return (byte)((128 - checksum) & 127);
        }

    public void performRequestDump(Model tempModel, boolean changePatch)
        {
        if (tempModel == null)
            tempModel = getModel();

        // We ALWAYS change the patch no matter what.  We have to.
        changePatch(tempModel);
        simplePause(getPauseAfterChangePatch());
        tryToSendSysex(requestDump(tempModel));
        }

    public byte[] requestDump(Model tempModel) 
        {
        // since performRequestDump ALWAYS changes the patch, we might
        // as well just call requestCurrentDump() here 
        return requestCurrentDump(); 
        }
    
    public byte[] requestCurrentDump()
        {
        byte id = 0;
        
        if (getSynthType() == TYPE_TG33)
            {
            return new byte[] { (byte)0xF0, (byte)0x43, (byte)(32 + getID()), (byte)0x7E, 
                (byte)'L', (byte)'M', (byte)' ', (byte)' ', (byte)'0', 
                (byte)'0', (byte)'1', (byte)'2', (byte)'V', (byte)'E', (byte)0xF7 };
            }
        else
            {
            return new byte[] { (byte)0xF0, (byte)0x43, (byte)(32 + getChannelOut()), (byte)0x7E, 
                (byte)'P', (byte)'K', (byte)' ', (byte)' ', (byte)'2', 
                (byte)'2', (byte)'0', (byte)'3', (byte)'A', (byte)'E', (byte)0xF7 };
            }
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
        
    public String getTitleBarSynthName()
        {
        return "Yamaha " + TYPES[synthType];
        }
        
    public static String getSynthName() { return "Yamaha TG33/SY22/SY35"; }

    public int getPauseAfterChangePatch()
        {
        // I notice that sometimes we can't load immediately after a change patch, so...
        // Perhaps just a smidgen?
        return 1000;
        }


    public void changePatch(Model tempModel) 
        {
        // Banks are stored in Edisyn as INTERNAL P1 P2 Card1 Card2
        // But on the TG33 the bank select data values are
        // INTERNAL=0 Card1=1 Preset1=2 Card2=4 PRESET2=5
        // Weird.
        final int[] bankvals = new int[] { 0, 2, 5, 1, 4 };
        
        // The TG33/SY22/SY35 requires that bank selects be both MSB and LSB (MSB first), but the documentation
        // doesn't explain this.  Furthermore, you have to do bank selects prior to PC.  Furthermore,
        // if the synth is in Edit mode, all bank selects are IGNORED.  So...
        //  
        // The secret to successful bank selects on the TG33 is to first get out of Edit mode by
        // pressing the VOICE or MULTI buttons, then do the MSB (0) bank
        // select (value = 0), then the LSB (32) bank select (value = bank), THEN do the PC.
        // Yes.  Nuts.
        tryToSendSysex(new byte[] { (byte)0xF0, 0x43, (byte)(getID() + 16), 0x26, 0x07, VOICE_BUTTON, (byte)0xF7 });
        simplePause(getPauseAfterChangePatch());
        tryToSendMIDI(buildCC(getChannelOut(), 0, 0));
        tryToSendMIDI(buildCC(getChannelOut(), 32, bankvals[tempModel.get("bank")]));
        tryToSendMIDI(buildPC(getChannelOut(), tempModel.get("number")));

        // we assume that we successfully did it
        if (!isMerging())  // we're actually loading the patch, not merging with it
            {
            setSendMIDI(false);
            model.set("bank", tempModel.get("bank"));
            model.set("number", tempModel.get("number"));
            setSendMIDI(true);
            }
        }
    
    public String getPatchName(Model model) { return model.get("name", "UNTITLED"); }

    public Model getNextPatchLocation(Model model)
        {
        int bank = model.get("bank");
        int number = model.get("number");
        
        number++;
        if (number >= 64)
            {
            bank++;
            number = 0;
            if (bank >= 6)
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

        int original = model.get("number");
        return (BANKS[model.get("bank")] + " " + ((original / 8 + 1) * 10 + (original % 8 + 1)));
        }

//// From original code in file edit_scr.c,
//// by R. P. Hanson, at http://storage.atari-source.org/atari/mirrors/ATARI-MIDI-ARCHIVES/SOUNDS/Yamaha/
////
//// My deep thanks to Rich Hanson, who allowed me to use this code as public domain and offered
//// some tips about the TG33 implementation.
//
//                osc_type *osc_pt ;
//                osc_pt = &el_pt->osc_2 ;
//                osc_pt->fixed_frequency = fm_data[el_pt->wave][0] ;
//                osc_pt->waveform = fm_data[el_pt->wave][1] ;
//                osc_pt->frequency = fm_data[el_pt->wave][2] ;
//                osc_pt = &el_pt->osc_1 ;
//                osc_pt->level = fm_data[el_pt->wave][3] ;
//                osc_pt->temperament = fm_data[el_pt->wave][4] ;
//                osc_pt->detune = fm_data[el_pt->wave][5] ;
//                osc_pt->level_scaling = fm_data[el_pt->wave][6] ;
//                osc_pt->rate_scaling = fm_data[el_pt->wave][7] ;
//                osc_pt->envelope_def.delay_switch = fm_data[el_pt->wave][8] ;
//                osc_pt->envelope_def.attack_rate = fm_data[el_pt->wave][9] ;
//                osc_pt->envelope_def.drate_1 = fm_data[el_pt->wave][10] ;
//                osc_pt->envelope_def.drate_2 = fm_data[el_pt->wave][11] ;
//                osc_pt->envelope_def.release_rate = fm_data[el_pt->wave][12] ;
//                osc_pt->envelope_def.initial_level = fm_data[el_pt->wave][13] ;
//                osc_pt->envelope_def.attack_level = fm_data[el_pt->wave][14] ;
//                osc_pt->envelope_def.dlevel_1 = fm_data[el_pt->wave][15] ;
//                osc_pt->envelope_def.dlevel_2 = fm_data[el_pt->wave][16] ;
//                osc_pt->fixed_frequency = fm_data[el_pt->wave][17] ;
//                osc_pt->waveform = fm_data[el_pt->wave][18] ;
//                osc_pt->frequency = fm_data[el_pt->wave][19] ;
//                el_pt->feedback = fm_data[el_pt->wave][20] ;
//

    /// updates the FM data that would be changed as a result of changing to a new FM preset wave.
    /// The preset wave FM definitions are in FM_DATA.  This is only called when the user physically
    /// changes the preset Chooser, not when the Chooser updates programmatically.
    public void updateFMData(int tone, int w)
        {
        int[] data = FM_DATA[w];
        String c = "tone" + tone + "0";
        String m = "tone" + tone + "1";
        String t = "tone" + tone;
        model.set(c + "fixed", data[0]);        
        model.set(c + "wave", data[1]);         
        model.set(c + "frequencyratio", data[2]);       
        model.set(t + "level", 127 - data[3]);          
        model.set(m + "temperment", data[4]);           
        model.set(m + "detune", data[5]);       
        model.set(m + "levelscaling", data[6]);         
        model.set(m + "ratescaling", data[7]);          
        model.set(m + "delayonoff", data[8]);           
        model.set(m + "attackrate", data[9]);           
        model.set(m + "decay1rate", data[10]);          
        model.set(m + "decay2rate", data[11]);          
        model.set(m + "releaserate", data[12]);         
        model.set(m + "initiallevel", 127 - data[13]);          
        model.set(m + "attacklevel", 127 - data[14]);           
        model.set(m + "decay1level", 127 - data[15]);           
        model.set(m + "decay2level", 127 - data[16]);           
        model.set(m + "fixed", data[17]);       
        model.set(m + "wave", data[18]);        
        model.set(m + "frequencyratio", data[19]);      
        model.set(t + "feedback", data[20]); 
        model.set(t + "algorithm", 0);                  // I presume 
        revise();       
        }
    
//// From original code in file sy_names.c,
//// by R. P. Hanson, at http://storage.atari-source.org/atari/mirrors/ATARI-MIDI-ARCHIVES/SOUNDS/Yamaha/
////
//// My deep thanks to Rich Hanson, who allowed me to use this code as public domain and offered
//// some tips about the TG33 implementation.

// 256 x 21
// Note levels are inverted: envelopes and volumes
    public static final int FM_DATA[][] = new int[][] {
        { 0,0,1,8,0,3,1,0,0,63,33,16,0,49,49,88,127,0,0,9,0 },
        { 0,0,2,8,0,0,7,3,0,45,52,19,1,127,19,21,24,0,0,1,6 },
        { 0,0,1,8,0,4,7,3,0,46,52,9,0,31,17,26,30,0,0,1,7 },
        { 0,0,1,8,0,1,8,3,0,63,22,16,0,113,47,50,95,0,0,3,5 },
        { 0,0,1,8,0,0,1,3,0,44,28,20,1,66,27,42,97,0,0,1,7 },
        { 0,0,1,8,0,2,8,3,0,63,12,5,0,127,21,34,42,0,7,1,4 },
        { 0,0,2,8,0,3,1,0,0,57,39,0,0,112,17,24,24,0,4,1,6 },
        { 0,0,1,8,0,3,1,0,0,63,51,0,0,53,4,20,20,0,0,1,0 },
        { 0,4,1,8,0,2,4,1,0,53,42,0,0,81,33,40,40,0,0,7,4 },
        { 0,1,1,8,0,4,10,3,0,13,63,13,0,127,37,37,29,1,0,0,0 },
        { 0,0,1,8,0,0,0,0,0,63,49,63,0,0,0,23,127,1,6,15,7 },
        { 0,0,1,8,0,2,2,0,0,63,55,0,0,127,0,32,32,0,0,1,7 },
        { 0,1,0,8,0,1,5,3,0,63,51,0,0,127,11,31,31,0,1,1,5 },
        { 0,0,0,8,0,1,1,3,0,50,36,0,0,127,27,67,67,0,0,3,5 },
        { 0,4,1,8,0,0,1,3,0,41,36,0,0,14,53,19,19,0,0,1,7 },
        { 0,2,1,8,0,0,3,2,0,32,43,0,0,127,18,20,20,0,2,1,6 },
        { 0,0,1,8,0,3,0,1,0,35,3,9,0,127,18,19,20,0,0,1,7 },
        { 0,0,1,8,0,1,0,0,0,36,0,0,0,127,22,22,22,0,0,1,7 },
        { 0,5,1,8,0,0,1,2,0,36,11,0,0,66,22,51,51,0,4,1,7 },
        { 0,4,1,8,0,0,2,5,0,33,11,0,0,108,21,73,73,0,5,1,7 },
        { 0,4,1,8,0,0,0,5,0,33,9,0,0,113,26,75,87,0,5,1,7 },
        { 0,0,1,8,0,0,1,2,0,34,24,0,0,127,20,25,25,0,0,1,7 },
        { 0,0,1,8,0,0,0,2,0,35,26,0,0,114,17,27,27,0,1,1,7 },
        { 0,0,1,8,0,0,0,1,0,20,27,23,0,28,27,20,24,0,0,1,7 },
        { 0,0,1,8,0,0,0,1,0,40,32,20,0,19,51,19,26,0,0,1,7 },
        { 0,0,1,8,0,0,2,1,0,48,20,10,0,8,57,19,24,0,0,1,7 },
        { 0,0,1,8,0,1,0,2,0,28,11,7,0,40,19,23,24,0,5,1,7 },
        { 0,4,0,8,0,1,0,0,0,33,17,0,0,48,18,24,24,0,0,0,7 },
        { 0,0,1,8,0,0,7,0,0,63,0,0,0,29,29,29,29,0,0,1,1 },
        { 0,0,1,8,0,0,0,2,0,30,35,0,0,118,30,32,32,0,0,1,5 },
        { 0,0,1,8,0,7,7,2,0,36,35,3,0,72,20,63,67,0,7,0,2 },
        { 0,4,3,8,0,0,5,0,0,40,10,0,0,127,25,45,45,0,4,2,7 },
        { 0,0,1,8,0,0,7,2,0,45,38,30,0,127,24,21,21,0,0,2,7 },
        { 0,0,1,8,0,1,8,0,0,38,30,0,0,64,23,33,33,0,6,2,5 },
        { 0,0,1,8,0,0,5,2,0,40,38,40,0,127,24,49,47,0,6,2,7 },
        { 0,0,1,8,0,0,0,0,0,40,40,0,0,59,29,48,48,0,0,4,7 },
        { 0,0,5,8,0,0,5,1,0,52,63,63,0,127,3,13,13,0,1,1,6 },
        { 0,5,4,8,0,0,2,1,0,55,38,30,0,127,24,20,20,0,4,1,2 },
        { 0,2,1,8,0,0,6,2,0,45,38,30,0,127,24,20,20,0,4,1,7 },
        { 0,0,1,8,0,1,0,0,0,40,25,0,0,127,22,24,24,0,4,1,7 },
        { 0,5,2,8,0,0,2,2,0,56,33,30,0,0,23,10,11,0,5,0,3 },
        { 0,2,1,8,0,0,2,2,0,32,38,30,0,30,23,21,21,0,5,0,7 },
        { 0,2,0,8,0,9,1,5,0,63,27,9,0,33,42,70,103,0,6,11,7 },
        { 0,7,0,8,0,0,3,3,0,63,21,0,0,127,39,62,62,0,7,12,5 },
        { 0,2,0,8,0,9,5,5,0,63,13,0,0,127,35,52,52,0,6,5,4 },
        { 0,5,0,8,0,10,1,5,0,46,38,14,0,100,27,43,64,0,3,4,7 },
        { 0,5,1,8,0,0,5,5,0,54,59,16,0,127,8,24,42,0,5,3,7 },
        { 0,5,1,8,0,0,5,5,0,54,59,16,0,127,8,24,42,0,5,5,7 },
        { 0,6,1,8,0,0,5,5,0,54,59,16,0,127,8,24,42,0,5,3,7 },
        { 0,7,1,8,0,0,8,0,0,63,40,30,0,127,15,25,45,0,7,3,6 },
        { 0,5,1,8,0,0,11,4,0,39,34,25,0,115,23,38,65,0,4,5,7 },
        { 0,5,1,8,0,0,11,4,0,39,34,17,0,101,1,24,51,0,7,1,7 },
        { 0,5,1,8,0,1,5,3,0,51,37,19,0,0,27,36,50,0,3,1,7 },
        { 0,5,1,8,0,9,5,3,0,63,30,5,26,101,5,5,127,0,6,0,0 },
        { 0,0,0,8,0,3,6,2,0,39,19,14,0,127,13,23,32,0,0,0,6 },
        { 0,0,1,8,0,4,5,2,0,45,18,9,0,127,18,24,28,0,0,0,6 },
        { 0,0,2,8,0,4,5,3,0,49,58,9,0,0,34,15,22,0,0,0,5 },
        { 0,6,0,8,0,0,2,1,0,63,50,19,0,21,21,43,52,0,0,6,7 },
        { 0,0,1,8,0,10,1,0,0,63,17,3,0,19,19,29,61,0,0,0,7 },
        { 0,0,0,8,0,9,1,0,0,63,29,3,0,22,22,39,49,0,3,0,7 },
        { 0,0,0,8,0,0,1,4,0,63,13,3,0,127,11,32,50,0,4,0,6 },
        { 0,4,1,8,0,0,1,4,0,63,9,6,0,2,2,23,50,0,4,0,4 },
        { 0,0,1,8,0,0,0,3,0,63,45,17,0,9,9,14,27,0,6,0,6 },
        { 0,1,1,8,0,0,1,1,0,63,63,63,23,127,19,23,23,0,0,1,7 },
        { 0,0,1,8,0,0,2,0,0,63,13,0,0,21,21,24,24,0,0,1,7 },
        { 0,2,0,8,0,0,6,2,0,33,24,63,20,127,27,32,32,0,1,3,7 },
        { 0,0,1,8,0,0,1,0,0,63,9,0,0,20,20,23,23,0,0,0,7 },
        { 0,0,1,8,0,0,2,1,0,63,17,0,0,19,19,24,24,0,0,0,7 },
        { 0,4,1,8,0,10,0,5,0,30,9,0,0,127,32,81,93,0,6,1,7 },
        { 0,5,0,8,0,2,0,4,0,22,10,0,0,64,40,26,38,0,5,1,7 },
        { 0,0,1,8,0,0,2,1,0,63,27,15,0,127,26,57,127,0,0,7,4 },
        { 0,0,1,8,0,0,2,1,0,63,29,10,0,27,27,80,89,0,2,7,0 },
        { 0,0,1,8,0,0,3,1,0,63,53,22,0,10,10,37,104,0,0,11,5 },
        { 0,0,2,8,1,0,0,5,0,40,27,10,0,35,44,87,96,0,6,9,7 },
        { 0,0,1,8,0,0,11,4,0,39,34,25,20,115,23,38,65,0,0,5,4 },
        { 0,0,2,8,2,4,4,1,0,55,39,10,0,127,20,72,127,0,0,3,0 },
        { 0,0,2,8,1,4,1,2,0,39,52,9,0,19,52,71,65,0,0,4,6 },
        { 0,0,1,8,3,0,7,0,0,63,32,10,0,6,6,16,96,0,4,2,4 },
        { 0,0,1,8,3,0,6,0,0,63,32,10,0,19,19,29,109,0,0,2,0 },
        { 0,0,1,8,1,0,7,0,0,63,32,10,0,29,29,39,127,0,2,6,0 },
        { 0,0,0,8,3,7,5,3,0,55,25,0,0,127,20,23,15,0,0,2,4 },
        { 0,0,2,8,1,4,3,3,0,53,52,9,0,29,13,26,18,0,0,4,5 },
        { 0,0,1,8,0,0,0,0,0,63,40,20,0,39,39,69,127,0,4,13,0 },
        { 0,0,1,8,3,0,0,0,0,63,32,10,0,19,19,39,109,0,0,1,0 },
        { 0,0,1,8,1,0,7,2,0,33,32,10,0,59,19,29,109,0,0,4,0 },
        { 0,0,2,8,2,4,0,1,0,63,49,29,12,10,10,21,86,0,7,2,5 },
        { 0,0,2,8,1,4,5,3,0,49,52,9,6,0,15,24,28,0,0,5,7 },
        { 0,0,2,8,1,4,5,3,0,39,52,9,40,0,15,24,28,0,3,7,6 },
        { 0,0,4,8,0,10,1,4,0,44,26,30,0,127,26,42,69,0,6,3,5 },
        { 0,0,11,8,1,4,5,3,0,3,63,63,0,0,11,9,2,1,1,2,7 },
        { 0,4,4,8,2,4,7,3,0,3,63,63,0,0,115,6,6,1,7,1,2 },
        { 0,0,1,8,0,0,0,0,0,63,0,0,0,16,16,16,16,0,5,1,7 },
        { 0,0,1,8,0,9,1,0,0,63,0,0,0,20,20,20,20,0,0,2,7 },
        { 0,4,1,8,0,3,5,0,0,63,10,0,0,116,19,39,39,0,4,2,7 },
        { 0,1,1,8,0,2,5,3,0,63,55,0,0,127,0,19,19,0,1,2,7 },
        { 0,0,1,8,0,2,5,3,0,63,63,0,0,111,0,22,22,0,0,1,7 },
        { 0,0,1,8,0,1,5,3,0,63,63,0,0,127,0,20,20,0,5,1,7 },
        { 0,0,1,8,0,0,0,0,0,63,0,0,0,24,24,24,24,0,0,2,7 },
        { 0,2,0,8,0,1,5,3,0,63,63,0,0,127,0,27,27,0,7,5,7 },
        { 0,1,3,8,0,0,0,0,0,63,0,0,0,11,11,11,11,0,0,0,0 },
        { 0,1,1,8,0,5,1,3,0,57,59,9,0,127,15,38,25,0,0,0,7 },
        { 0,4,1,8,0,10,0,5,0,9,9,9,0,127,32,72,117,0,6,1,7 },
        { 0,0,1,8,0,5,0,2,0,26,0,0,0,127,78,78,78,0,6,13,7 },
        { 0,0,1,8,0,5,0,1,0,26,0,0,0,84,34,34,34,0,0,1,7 },
        { 0,5,1,8,0,3,0,0,0,24,11,0,0,33,23,33,33,0,4,1,7 },
        { 0,5,1,8,0,4,7,3,0,30,22,9,0,53,40,42,24,0,2,12,5 },
        { 0,5,1,8,1,4,7,3,0,30,52,9,0,53,50,50,50,0,4,5,5 },
        { 0,4,0,8,0,0,0,0,0,53,11,0,0,44,16,21,21,0,4,0,6 },
        { 0,0,1,8,0,4,7,3,0,30,52,9,0,53,18,18,18,0,4,0,6 },
        { 0,0,1,8,0,4,8,3,0,30,52,9,0,53,10,10,10,0,4,0,3 },
        { 0,0,1,8,0,0,7,0,0,26,0,0,0,76,26,26,26,0,4,0,7 },
        { 0,1,1,8,0,4,2,2,0,42,23,23,0,127,18,26,26,0,6,1,7 },
        { 1,0,0,8,0,0,0,0,0,63,0,0,0,24,24,24,24,0,0,1,7 },
        { 0,0,1,8,0,4,1,3,0,11,8,13,0,17,51,29,28,0,0,0,7 },
        { 0,4,1,8,0,1,5,3,0,30,12,3,0,11,30,29,25,0,2,0,6 },
        { 0,0,0,8,0,0,5,3,0,22,4,3,0,13,32,21,22,0,0,0,7 },
        { 0,2,1,8,0,0,5,0,0,45,63,9,0,0,7,25,30,0,2,0,6 },
        { 0,4,1,8,0,0,5,3,0,27,4,3,0,15,63,53,59,0,1,3,6 },
        { 0,5,0,8,0,1,5,3,0,12,12,8,0,16,52,16,32,0,1,0,7 },
        { 0,0,0,8,0,2,5,3,0,14,8,10,0,11,55,22,36,0,1,1,5 },
        { 0,6,0,8,0,1,5,3,0,6,20,13,23,127,24,43,50,0,1,7,7 },
        { 0,0,0,8,0,2,3,3,0,8,8,8,23,127,17,34,20,0,0,0,7 },
        { 0,5,0,8,0,2,5,3,0,8,8,8,23,127,8,34,8,0,2,1,5 },
        { 0,4,2,8,0,2,5,3,0,8,8,8,13,127,8,34,10,0,0,0,6 },
        { 0,0,1,8,0,2,5,3,0,10,10,10,23,127,14,34,22,0,0,2,4 },
        { 0,0,1,8,0,4,7,3,0,45,52,9,0,127,26,32,33,0,0,2,7 },
        { 0,4,1,8,0,2,5,0,0,63,30,6,0,10,10,21,57,0,4,1,6 },
        { 0,0,1,8,0,4,5,1,0,57,32,9,0,127,13,21,28,0,6,1,6 },
        { 0,1,1,8,0,1,4,0,0,63,42,15,0,80,32,39,39,0,0,5,7 },
        { 0,0,1,8,0,1,5,3,0,27,22,3,0,19,21,32,30,0,1,2,6 },
        { 0,4,1,8,0,0,7,1,0,48,39,23,0,59,29,49,58,0,6,5,7 },
        { 0,4,1,8,0,0,4,2,0,44,22,5,0,61,13,20,70,0,3,7,4 },
        { 0,5,2,8,0,1,5,3,0,58,46,9,0,127,2,35,44,0,6,7,4 },
        { 0,6,2,8,0,0,5,4,0,63,63,11,0,127,3,22,28,0,5,1,4 },
        { 0,0,8,8,0,0,5,0,0,51,40,5,0,45,0,4,54,0,7,0,4 },
        { 0,1,3,8,0,0,5,3,0,18,8,13,0,12,42,25,30,0,0,0,7 },
        { 0,0,2,8,0,0,1,0,0,63,40,5,0,4,0,4,54,0,6,0,2 },
        { 0,3,2,8,0,1,11,2,0,63,63,17,0,127,6,15,26,0,2,0,6 },
        { 0,4,0,8,0,0,5,0,0,44,22,5,0,48,0,7,57,0,7,0,4 },
        { 0,7,0,8,0,0,8,5,0,63,10,6,0,127,44,90,99,0,5,8,7 },
        { 0,0,1,8,0,1,5,2,0,63,22,3,0,8,13,26,24,0,0,0,7 },
        { 0,0,0,8,0,3,0,1,0,53,47,10,0,127,15,30,34,0,6,0,7 },
        { 0,4,0,8,0,0,5,1,0,53,34,20,0,79,16,34,41,0,5,1,7 },
        { 0,0,1,8,0,4,7,3,0,36,63,21,63,22,127,22,127,0,2,11,7 },
        { 0,0,2,8,0,0,0,0,0,63,0,0,0,0,0,0,0,1,4,0,0 },
        { 1,5,0,8,0,0,0,0,0,63,0,0,0,39,39,39,39,0,6,2,0 },
        { 0,0,1,8,3,3,5,0,0,25,38,20,0,15,52,16,32,0,0,1,7 },
        { 0,0,1,8,1,4,0,0,0,13,63,13,3,127,57,57,49,0,0,3,7 },
        { 0,0,0,8,1,1,0,0,0,13,63,13,3,127,32,32,32,0,1,1,7 },
        { 0,0,0,8,2,4,0,0,0,3,63,13,3,127,17,17,17,1,0,13,0 },
        { 0,0,0,8,0,0,0,0,0,0,0,0,0,127,127,127,127,0,0,0,0 },
        { 0,0,1,8,0,0,0,0,0,0,0,0,0,127,127,127,127,0,0,1,0 },
        { 0,0,2,8,0,0,0,0,0,0,0,0,0,127,127,127,127,0,0,2,0 },
        { 0,0,3,8,0,0,0,0,0,0,0,0,0,127,127,127,127,0,0,3,0 },
        { 0,0,4,8,0,0,0,0,0,0,0,0,0,127,127,127,127,0,0,4,0 },
        { 0,0,0,8,0,0,0,0,0,0,0,0,0,24,24,24,24,0,0,0,7 },
        { 0,5,0,8,0,0,0,0,0,0,0,0,0,27,27,27,27,0,5,0,7 },
        { 0,0,0,8,0,0,0,0,0,0,0,0,0,26,26,26,26,0,0,1,7 },
        { 0,4,1,8,0,0,0,0,0,0,0,0,0,42,42,42,42,0,0,2,0 },
        { 1,0,15,8,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,15,7 },
        { 0,7,15,8,3,7,0,0,0,0,0,0,0,0,0,0,0,0,6,15,4 },
        { 0,5,3,8,0,0,0,0,0,0,0,0,0,31,31,31,31,0,6,0,7 },
        { 0,1,4,8,0,0,0,0,0,0,0,0,0,19,19,19,19,0,1,0,7 },
        { 0,5,0,8,0,0,0,0,0,0,0,0,0,45,45,45,45,0,7,5,5 },
        { 0,4,0,8,0,0,0,0,0,0,0,0,0,39,39,39,39,0,3,3,7 },
        { 0,0,1,8,0,0,0,0,0,63,0,0,0,80,57,57,57,0,0,15,0 },
        { 0,2,1,8,0,0,1,0,0,0,0,0,0,42,42,42,42,0,1,4,0 },
        { 0,2,1,8,0,0,0,0,0,0,0,0,0,42,42,42,42,0,1,6,0 },
        { 0,2,1,8,0,0,4,0,0,0,0,0,0,42,42,42,42,0,1,8,0 },
        { 0,2,1,8,0,0,1,0,0,0,0,0,0,42,42,42,42,0,1,10,0 },
        { 0,2,1,8,0,0,2,0,0,0,0,0,0,42,42,42,42,0,1,12,0 },
        { 0,2,1,8,0,0,4,0,0,0,0,0,0,42,42,42,42,0,1,14,0 },
        { 0,0,0,8,0,0,0,0,0,0,0,0,0,48,48,48,48,0,4,3,3 },
        { 0,0,1,8,0,0,0,0,0,0,0,0,0,48,48,48,48,0,4,6,3 },
        { 0,0,2,8,0,0,0,0,0,0,0,0,0,48,48,48,48,0,4,6,3 },
        { 0,4,0,8,0,0,0,0,0,0,0,0,0,42,42,42,42,0,0,1,3 },
        { 0,4,1,8,0,0,0,0,0,0,0,0,0,42,42,42,42,0,0,2,3 },
        { 0,4,2,8,0,0,0,0,0,0,0,0,0,42,42,42,42,0,0,4,3 },
        { 0,0,0,8,0,0,0,0,0,0,0,0,0,42,42,42,42,0,3,0,0 },
        { 0,0,1,8,0,0,0,0,0,0,0,0,0,42,42,42,42,0,3,1,0 },
        { 0,0,2,8,0,0,4,0,0,0,0,0,0,42,42,42,42,0,3,2,0 },
        { 0,0,0,8,0,0,0,0,0,0,0,0,0,42,42,42,42,0,4,1,3 },
        { 0,0,1,8,0,0,0,0,0,0,0,0,0,42,42,42,42,0,4,2,3 },
        { 0,0,2,8,0,0,0,0,0,0,0,0,0,42,42,42,42,0,4,4,3 },
        { 0,0,0,8,0,0,0,0,0,0,0,0,0,38,38,38,38,0,0,1,3 },
        { 0,0,1,8,0,0,0,0,0,0,0,0,0,42,42,42,42,0,0,2,3 },
        { 0,0,2,8,0,0,0,0,0,0,0,0,0,39,39,39,39,0,0,4,3 },
        { 0,0,0,8,0,0,2,0,0,0,0,0,0,24,24,24,24,0,7,4,0 },
        { 0,0,1,8,0,0,8,0,0,0,0,0,0,24,24,24,24,0,7,8,0 },
        { 0,0,2,8,1,0,8,0,0,0,0,0,0,24,24,24,24,0,7,15,0 },
        { 0,4,2,8,0,0,2,0,0,0,0,0,0,7,7,7,7,0,7,0,4 },
        { 0,4,4,8,0,0,4,0,0,0,0,0,0,7,7,7,7,0,7,1,4 },
        { 0,4,8,8,0,0,8,0,0,0,0,0,0,7,7,7,7,0,7,2,4 },
        { 0,2,0,8,0,0,1,0,0,0,0,0,0,12,12,12,12,0,4,0,5 },
        { 0,2,1,8,0,0,3,0,0,0,0,0,0,12,12,12,12,0,4,1,5 },
        { 0,2,2,8,0,0,4,0,0,0,0,0,0,12,12,12,12,0,4,2,5 },
        { 0,3,1,8,0,0,0,0,0,0,0,0,0,19,19,19,19,0,0,0,6 },
        { 0,3,2,8,0,0,3,0,0,0,0,0,0,19,19,19,19,0,0,1,6 },
        { 0,3,4,8,0,0,4,0,0,0,0,0,0,19,19,19,19,0,0,2,5 },
        { 0,7,0,8,0,0,1,0,0,0,0,0,0,33,33,33,33,0,4,3,6 },
        { 0,7,1,8,0,0,4,0,0,0,0,0,0,33,33,33,33,0,4,6,6 },
        { 0,7,2,8,0,0,4,0,0,0,0,0,0,33,33,33,33,0,4,12,6 },
        { 0,4,0,8,0,0,0,0,0,0,0,0,0,12,12,12,12,0,4,0,5 },
        { 0,4,1,8,0,0,3,0,0,0,0,0,0,12,12,12,12,0,4,1,5 },
        { 0,4,2,8,0,0,4,0,0,0,0,0,0,12,12,12,12,0,4,2,5 },
        { 0,1,0,8,0,0,0,0,0,0,0,0,0,20,20,20,20,0,4,1,0 },
        { 0,1,1,8,0,0,2,0,0,0,0,0,0,20,20,20,20,0,4,2,0 },
        { 0,1,2,8,0,0,3,0,0,0,0,0,0,20,20,20,20,0,4,4,0 },
        { 0,3,0,8,0,0,0,0,0,0,0,0,0,42,42,42,42,0,1,0,0 },
        { 0,3,1,8,0,0,0,0,0,0,0,0,0,42,42,42,42,0,1,1,0 },
        { 0,3,2,8,0,0,9,0,0,0,0,0,0,42,42,42,42,0,1,2,0 },
        { 0,3,0,8,0,0,0,0,0,0,0,0,0,42,42,42,42,0,3,0,3 },
        { 0,3,1,8,0,0,0,0,0,0,0,0,0,42,42,42,42,0,3,1,3 },
        { 0,3,2,8,0,0,8,0,0,0,0,0,0,48,48,48,48,0,3,2,3 },
        { 0,2,0,8,0,0,0,0,0,0,0,0,0,42,42,42,42,0,2,0,3 },
        { 0,2,1,8,0,0,0,0,0,0,0,0,0,42,42,42,42,0,2,1,3 },
        { 0,2,2,8,0,0,0,0,0,0,0,0,0,42,42,42,42,0,2,2,3 },
        { 0,6,0,8,0,0,0,0,0,0,0,0,0,42,42,42,42,0,2,0,3 },
        { 0,6,1,8,0,0,0,0,0,0,0,0,0,42,42,42,42,0,2,1,3 },
        { 0,6,2,8,0,0,4,0,0,0,0,0,0,42,42,42,42,0,2,2,3 },
        { 0,1,0,8,0,0,0,0,0,0,0,0,0,42,42,42,42,0,3,0,0 },
        { 0,1,1,8,0,0,0,0,0,0,0,0,0,42,42,42,42,0,3,1,0 },
        { 0,1,2,8,0,0,6,0,0,0,0,0,0,42,42,42,42,0,3,2,0 },
        { 0,6,1,8,0,0,4,0,0,0,0,0,0,3,3,3,3,0,6,0,4 },
        { 0,6,2,8,0,0,8,0,0,0,0,0,0,3,3,3,3,0,6,1,4 },
        { 0,6,4,8,0,0,13,0,0,0,0,0,0,9,9,9,9,0,6,2,4 },
        { 0,0,0,8,0,0,0,0,0,0,0,0,0,12,12,12,12,0,5,0,5 },
        { 0,0,1,8,0,0,2,0,0,0,0,0,0,12,12,12,12,0,5,1,5 },
        { 0,0,2,8,0,0,3,0,0,0,0,0,0,12,12,12,12,0,5,2,5 },
        { 0,3,0,8,0,0,0,0,0,0,0,0,0,127,127,127,127,0,0,0,0 },
        { 0,3,1,8,0,0,0,0,0,0,0,0,0,127,127,127,127,0,0,1,0 },
        { 0,3,2,8,0,0,3,0,0,0,0,0,0,127,127,127,127,0,0,2,0 },
        { 0,2,0,8,0,0,0,0,0,0,0,0,0,127,127,127,127,0,0,0,0 },
        { 0,2,1,8,0,0,0,0,0,0,0,0,0,127,127,127,127,0,0,1,0 },
        { 0,2,2,8,0,0,0,0,0,0,0,0,0,127,127,127,127,0,0,2,0 },
        { 0,6,0,8,0,0,0,0,0,0,0,0,0,42,42,42,42,0,1,0,0 },
        { 0,6,1,8,0,0,0,0,0,0,0,0,0,42,42,42,42,0,1,1,0 },
        { 0,6,2,8,0,0,4,0,0,0,0,0,0,42,42,42,42,0,1,2,0 },
        { 0,6,0,8,0,0,0,0,0,0,0,0,0,42,42,42,42,0,3,0,0 },
        { 0,6,1,8,0,0,0,0,0,0,0,0,0,42,42,42,42,0,3,1,0 },
        { 0,6,2,8,0,0,4,0,0,0,0,0,0,42,42,42,42,0,3,2,0 },
        { 0,7,0,8,0,0,0,0,0,0,0,0,0,42,42,42,42,0,2,0,0 },
        { 0,7,1,8,0,0,0,0,0,0,0,0,0,42,42,42,42,0,1,1,0 },
        { 0,7,2,8,0,0,4,0,0,0,0,0,0,42,42,42,42,0,1,2,0 },
        { 0,1,0,8,0,0,0,0,0,0,0,0,0,127,127,127,127,0,0,0,0 },
        { 0,1,1,8,0,0,0,0,0,0,0,0,0,127,127,127,127,0,0,1,0 },
        { 0,1,2,8,0,0,0,0,0,0,0,0,0,127,127,127,127,0,0,2,0 },
        { 0,5,0,8,0,0,0,0,0,0,0,0,0,42,42,42,42,0,1,0,3 },
        { 0,5,1,8,0,0,0,0,0,0,0,0,0,42,42,42,42,0,1,1,3 },
        { 0,5,4,8,0,0,0,0,0,0,0,0,0,42,42,42,42,0,1,4,3 },
        { 0,4,0,8,0,0,0,0,0,0,0,0,0,42,42,42,42,0,3,0,0 },
        { 0,4,1,8,0,0,0,0,0,0,0,0,0,42,42,42,42,0,3,1,0 },
        { 0,4,2,8,0,0,1,0,0,0,0,0,0,42,42,42,42,0,3,2,0 },
        { 0,0,4,8,0,0,1,0,0,0,0,0,0,19,19,19,19,0,1,0,0 },
        { 0,0,1,8,0,15,0,0,0,63,0,0,0,19,19,19,19,0,4,1,7 },
        { 0,6,2,8,0,0,0,0,0,10,10,10,0,2,2,2,2,1,4,9,0 } 
        };

    public boolean testVerify(Synth synth2, 
        String key,
        Object obj1, Object obj2) 
        {
        // The TG33 has these but not the SY22 or SY85, or vice versa, so one or more
        // of the will not be set or will be reset
        if (key.equals("effectsenddepth")) return true;
        if (key.equals("effectbalance")) return true;
        if (key.equals("effectsendlevel")) return true;
        return false;
        }
        
    }
