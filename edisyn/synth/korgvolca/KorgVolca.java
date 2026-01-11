/***
    Copyright 2021 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.korgvolca;

import edisyn.*;
import edisyn.gui.*;
import edisyn.util.*;
import edisyn.synth.yamahadx7.*;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.border.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javax.sound.midi.*;
import java.util.zip.*;


/**
   A patch editor for the Korg Volca series.
        
   @author Sean Luke
*/

public class KorgVolca extends Synth
    {
    public static final int TYPE_VOLCA_BASS = 0;
    public static final int TYPE_VOLCA_BEATS = 1;
    public static final int TYPE_VOLCA_DRUM_SINGLE = 2;
    public static final int TYPE_VOLCA_DRUM_SPLIT = 3;
    public static final int TYPE_VOLCA_FM = 4;
    public static final int TYPE_VOLCA_KEYS = 5;
    public static final int TYPE_VOLCA_KICK = 6;
    public static final int TYPE_VOLCA_NUBASS = 7;
    public static final int TYPE_VOLCA_SAMPLE = 8;
    //public static final int TYPE_VOLCA_SAMPLE_PAJEN_11 = 9;
    public static final int TYPE_VOLCA_SAMPLE_2 = 9;        
    public static final int TYPE_VOLCA_MODULAR = 10;                        // No MIDI
    public static final int TYPE_VOLCA_MIX = 11;                            // No MIDI
    public static final int NUM_EDITABLE_VOLCAS = TYPE_VOLCA_MODULAR;

    public static final String[] VOLCAS = { "Bass", "Beats", "Drum (Single)", "Drum (Split)", "FM", "Keys", "Kick", "NuBass", "Sample/Sample2 (Multi)", // "Sample (Pajen Ch 11)", 
        "Sample2 (Single)" };
    public static final String[] PREFIXES = { "bass", "beats", "drumsingle", "drumsplit", "fm", "keys", "kick", "nubass", "sample1", // "samplepajen", 
        "sample2" };
        
    // Minimum value corresponding to octaves 1-6
    public static final int[] BASS_OCTAVES = { 00, 22, 44, 66, 88, 110 };
    // Notes corresponding to kick, snare, lo tom, hi tom, cl hat, op hat, clap, claves, agogo, crash
    public static final int[] BEATS_NOTES = { 36, 38, 43, 50, 42, 46, 39, 75, 67, 49 };
    // Notes corresponding to kick, snare, lo tom, hi tom, cl hat, op hat, clap, claves, agogo, crash
    public static final String[] BEATS_DRUMS = { "Kick", "Snare", "Lo Tom", "Hi Tom", "Closed Hat", "Open Hat", "Handclap", "Claves", "Agogo", "Crash" };

    // Notes corresponding to 6 parts
    public static final int[] DRUM_SINGLE_NOTES = { 60, 62, 64, 65, 67, 69 };
    // The six parts
    public static final String[] DRUM_SINGLE_PARTS = { "1", "2", "3", "4", "5", "6" };
    
    // Notes corresponding to the Drum (split) parts
    public static final String[] DRUM_SPLIT_PARTS = DRUM_SINGLE_PARTS;
        
    // The ten parts
    public static final String[] SAMPLE_PARTS = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" };
        
    public static final String[] DRUM_SOURCES = { "Sine", "Saw", "H Noise", "L Noise", "B Noise" };
    public static final String[] DRUM_MODULATORS = { "Rise-Fall", "Oscillate", "Random" };
    public static final String[] DRUM_ENVELOPES = { "Lin AR", "Exp AR", "Mult AR" };
    
    // Transpose values for the Volca FM
    public static final int[] FM_TRANSPOSE = 
        { 
        -36, -36, -35, -35, -34, -34, -33, -32,
        -32, -31, -31, -30, -29, -29, -28, -28,
        -27, -26, -26, -25, -25, -24, -23, -23,
        -22, -22, -21, -20, -20, -19, -19, -18,
        -17, -17, -16, -16, -15, -14, -14, -13,
        -13, -12, -11, -11, -10, -10,  -9,  -8,
        -8,  -7,  -7,  -6,  -5,  -5,  -4,  -4,
        -3,  -2,  -2,  -1,  -1,   0,   0,   0,
        0,   0,   0,   0,   1,   1,   2,   2,
        3,   4,   4,   5,   5,   6,   7,   7,
        8,   8,   9,  10,  10,  11,  11,  12,
        13,  13,  14,  14,  15,  16,  16,  17,
        17,  18,  19,  19,  20,  20,  21,  22,
        22,  23,  23,  24,  25,  25,  26,  26,
        27,  28,  28,  29,  29,  30,  31,  31,
        32,  32,  33,  34,  34,  35,  35,  36
        };  
        
    
    // LFO values for the Volca FM
    public static final int[] FM_LFO =
        {
        0,  0,  1,  2,  3,  3,  4,  5,
        6,  7,  7,  8,  9, 10, 10, 11,
        12, 13, 14, 14, 15, 16, 17, 17,
        18, 19, 20, 21, 21, 22, 23, 24,
        25, 25, 26, 27, 28, 28, 29, 30,
        31, 32, 32, 33, 34, 35, 35, 36,
        37, 38, 39, 39, 40, 41, 42, 42,
        43, 44, 45, 46, 46, 47, 48, 49,
        50, 50, 51, 52, 53, 53, 54, 55,
        56, 57, 57, 58, 59, 60, 60, 61,
        62, 63, 64, 64, 65, 66, 67, 67,
        68, 69, 70, 71, 71, 72, 73, 74,
        75, 75, 76, 77, 78, 78, 79, 80,
        81, 82, 82, 83, 84, 85, 85, 86,
        87, 88, 89, 89, 90, 91, 92, 92,
        93, 94, 95, 96, 96, 97, 98, 99,
        };
        
    // Attack and Release values for the Volca FM
    public static final int[] FM_ATTACK = 
        {
        -63, -63, -61, -60, -58, -57, -55, -54, 
        -52, -51, -49, -48, -46, -45, -43, -42, 
        -40, -39, -37, -36, -34, -33, -31, -30, 
        -28, -27, -25, -24, -22, -21, -19, -18, 
        -16, -15, -15, -14, -14, -13, -13, -12, 
        -12, -11, -11, -10, -10, -9, -9, -8, 
        -8, -7, -7, -6, -6, -5, -5, -4, 
        -4, -3, -3, -2, -2, -1, -1, 0, 
        0, 0, 1, 1, 2, 2, 3, 3, 
        4, 4, 5, 5, 6, 6, 7, 7, 
        8, 8, 9, 9, 10, 10, 11, 11, 
        12, 12, 13, 13, 14, 14, 15, 15, 
        16, 18, 19, 21, 22, 24, 25, 27, 
        28, 30, 31, 33, 34, 36, 37, 39, 
        40, 42, 43, 45, 46, 48, 49, 51, 
        52, 54, 55, 57, 58, 60, 61, 63, 
        };
        
    // This is according to Dave Mac on the Volca Facebook, thanks Dave.
    public static final int[] NUBASS_PITCH = 
        {
        -1200,
        -1200, -1175, -1150, -1125, -1100, -1075, -1050, -1025, 
        -1000, -975, -950, -925, -900, -875, -850, -825, 
        -800, -775, -750, -725, -700, -675, -650, -625, 
        -600, -575, -550, -525, -500, -475, -450, -425, 
        -400, -375, -350, -325, -300, -275, -250, -225, 
        -200, -180, -160, -140, -120, -100, -90, -72, 
        -64, -56, -48, -40, -32, -24, -16, -8, 
        0, 0, 0, 0, 0, 0, 0,            // I suspect this is actually -7 ... -1 --- Sean
        0,
        0, 0, 0, 0, 0, 0, 0,            // I suspect this is actually +1 ... + 7 --- Sean
        8, 16, 24, 32, 40, 48, 56, 64, 
        72, 90, 100, 120, 140, 160, 180, 200,
        225, 250, 275, 300, 325, 350, 375, 400,
        425, 450, 475, 500, 525, 550, 575, 600,
        625, 650, 675, 700, 725, 750, 775, 800,
        825, 850, 875, 900, 925, 950, 975, 1000,
        1025, 1050, 1075, 1100, 1125, 1150, 1175, 1200
        };
        
    // Various Volca Drum values that go -100...+100
    public static final int[] DRUM_100 = 
        {
        -100, -98, -97, -96, -93, -92, -90, -89,
        -87, -86, -84, -83, -81, -79, -78, -76,
        -75, -73, -72, -70, -68, -67, -65, -64,
        -62, -61, -59, -58, -56, -54, -53, -51,
        -50, -48, -47, -45, -43, -42, -40, -39,
        -37, -36, -34, -33, -31, -29, -28, -26,
        -25, -23, -22, -20, -18, -17, -15, -14,
        -12, -11, -9, -8, -6, -4, -3, -1, 
        0, 2, 3, 5, 7, 8, 10, 11, 
        13, 14, 16, 17, 19, 21, 22, 24, 
        25, 27, 28, 30, 32, 33, 35, 36, 
        38, 39, 41, 42, 44, 46, 47, 49, 
        50, 52, 53, 55, 57, 58, 60, 61, 
        63, 64, 66, 67, 69, 71, 72, 74, 
        75, 77, 78, 80, 82, 83, 85, 86, 
        88, 89, 91, 92, 94, 96, 97, 100
        };

    // Volca Drum "Select" combinations. There are a 5 x 3 x 3 = 45 combinations total.
    public static final int[] DRUM_SELECT = 
        {
        0, 0, 0, 1, 1, 1, 2, 2, 
        2, 3, 3, 3, 4, 4, 4, 5, 
        5, 5, 6, 6, 7, 7, 7, 8, 
        8, 8, 9, 9, 9, 10, 10, 10, 
        11, 11, 11, 12, 12, 13, 13, 13, 
        14, 14, 14, 15, 15, 15, 16, 16, 
        16, 17, 17, 17, 18, 18, 18, 19, 
        19, 20, 20, 20, 21, 21, 21, 22, 
        22, 22, 23, 23, 23, 24, 24, 24, 
        25, 25, 25, 26, 26, 27, 27, 27, 
        28, 28, 28, 29, 29, 29, 30, 30, 
        30, 31, 31, 31, 32, 32, 32, 33, 
        33, 34, 34, 34, 35, 35, 35, 36, 
        36, 36, 37, 37, 37, 38, 38, 38, 
        39, 39, 39, 40, 40, 41, 41, 41, 
        42, 42, 42, 43, 43, 43, 44, 44, 
        };

                
        
    public static final ImageIcon[] ALGORITHM_ICONS;
    
    static
        {
        ALGORITHM_ICONS = new ImageIcon[128];
        for(int i = 0; i < ALGORITHM_ICONS.length; i++)
            {
            ALGORITHM_ICONS[i] = edisyn.synth.yamahadx7.YamahaDX7.ALGORITHM_ICONS[i / 4];
            }
        }
        
    public static final String TYPE_KEY = "type";
    int synthType = TYPE_VOLCA_BASS;
    JComboBox synthTypeCombo;
    
    JComboBox beatsCombo;                                       // which drum is beats playing?
    JComboBox drumSingleCombo;
    public int getTestNotePitch()
        {
        int type = getSynthType();
        switch(type)
            {
            case TYPE_VOLCA_BEATS:
            {
            return BEATS_NOTES[beatsCombo.getSelectedIndex()];
            }
            case TYPE_VOLCA_DRUM_SINGLE:
            {
            return DRUM_SINGLE_NOTES[drumSingleCombo.getSelectedIndex()];
            }
            default: return super.getTestNotePitch();
            }
        }

    JComboBox drumCombo;                                                                                // which part is drum playing?
    JComboBox sampleCombo;                                                                                // which part is drum playing?
    public int getTestNoteChannel()
        {
        int type = getSynthType();
        switch(type)
            {
            case TYPE_VOLCA_DRUM_SPLIT:
            {
            return drumCombo.getSelectedIndex();
            }
            case TYPE_VOLCA_SAMPLE:
            {
            return sampleCombo.getSelectedIndex();
            }
            default: return super.getTestNoteChannel();
            }
        }

        
    public int getSynthType() { return synthType; }
    public void setSynthType(int val, boolean save)
        {
        if (save)
            {
            setLastX("" + val, TYPE_KEY, getSynthClassName(), true);
            }
        synthType = val;
        if (synthTypeCombo != null) 
            {
            synthTypeCombo.setSelectedIndex(val);  // hopefully this isn't recursive
            }
        if (volcaPanel != null)
            {
            volcaPanel.removeAll();
            volcaPanel.add(volcas[val], BorderLayout.CENTER);
            }
        revalidate();
        repaint();
        updateTitle();
        }

    public static String getSynthName() { return "Korg Volca Series"; }

    public JComponent[] volcas = new JComponent[NUM_EDITABLE_VOLCAS];
    public JPanel volcaPanel;
        
    public KorgVolca()
        {
        if (allParametersToCC == null)
            {
            allParametersToCC = new HashMap();
            for(int i = 0; i < allParameters.length; i++)
                {
                if (i == TYPE_VOLCA_SAMPLE)
                    {
                    int count = 0;
                    for(int j = 0; j < allParameters[i].length; j++)
                        {
                        allParametersToCC.put(allParameters[i][j], Integer.valueOf(CC[i][count++]));
                        if (count >= CC[i].length)
                            count = 0;
                        }
                    }
                else if (i == TYPE_VOLCA_DRUM_SPLIT)
                    {
                    int count = 0;
                    for(int j = 0; j < allParameters[i].length - 4; j++)  // we skip the last four
                        {
                        allParametersToCC.put(allParameters[i][j], Integer.valueOf(CC[i][count++]));
                        if (count >= CC[i].length - 4)         // we skip the last four 
                            count = 0;
                        }
                    allParametersToCC.put("drumsplitwaveguidemodel", Integer.valueOf(CC[i][CC[i].length - 4]));
                    allParametersToCC.put("drumsplitdecay", Integer.valueOf(CC[i][CC[i].length - 3]));
                    allParametersToCC.put("drumsplitbody", Integer.valueOf(CC[i][CC[i].length - 2]));
                    allParametersToCC.put("drumsplittune", Integer.valueOf(CC[i][CC[i].length - 1]));
                    }
                else
                    {
                    for(int j = 0; j < allParameters[i].length; j++)
                        {
                        allParametersToCC.put(allParameters[i][j], Integer.valueOf(CC[i][j]));
                        }
                    }
                }
            }
                
        String m = getLastX(TYPE_KEY, getSynthClassName());
        try
            {
            synthType = (m == null ? TYPE_VOLCA_BASS : Integer.parseInt(m));
            if (synthType < TYPE_VOLCA_BASS || synthType > TYPE_VOLCA_SAMPLE_2)
                {
                synthType = TYPE_VOLCA_BASS;
                }
            }
        catch (NumberFormatException ex)
            {
            synthType = TYPE_VOLCA_BASS;
            }

        volcas[TYPE_VOLCA_BASS] = addVolcaBass(Style.COLOR_A());        
        volcas[TYPE_VOLCA_BEATS] = addVolcaBeats(Style.COLOR_B());        
        volcas[TYPE_VOLCA_DRUM_SINGLE] = addVolcaDrumSingle(Style.COLOR_A());        
        volcas[TYPE_VOLCA_DRUM_SPLIT] = addVolcaDrumSplit(Style.COLOR_B());        
        volcas[TYPE_VOLCA_FM] = addVolcaFM(Style.COLOR_A());        
        volcas[TYPE_VOLCA_KEYS] = addVolcaKeys(Style.COLOR_B());        
        volcas[TYPE_VOLCA_KICK] = addVolcaKick(Style.COLOR_A());        
        volcas[TYPE_VOLCA_NUBASS] = addVolcaNuBass(Style.COLOR_B());        
        volcas[TYPE_VOLCA_SAMPLE] = addVolcaSample(Style.COLOR_A());        
        //volcas[TYPE_VOLCA_SAMPLE_PAJEN_11] = addVolcaSamplePajen(Style.COLOR_A());    
        volcas[TYPE_VOLCA_SAMPLE_2] = addVolcaSample2(Style.COLOR_B());
        
        JComponent soundPanel = new SynthPanel(this);
        VBox vbox = new VBox();
        vbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        
        HBox outer = new HBox(HBox.LEFT_CONSUMES);
        VBox inner = new VBox();
        volcaPanel = new JPanel();
        volcaPanel.setLayout(new BorderLayout());
        setSynthType(getSynthType(), false);            // load the panel
        inner.add(Strut.makeHorizontalStrut(volcas[TYPE_VOLCA_SAMPLE].getPreferredSize().width));
        inner.addLast(volcaPanel);
        outer.add(Strut.makeVerticalStrut(volcas[TYPE_VOLCA_SAMPLE].getPreferredSize().height));
        outer.addLast(inner);
        vbox.addLast(outer);
        
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Volca", soundPanel);

        loadDefaults();        
        }

    public JFrame sprout()
        {
        JFrame frame = super.sprout();
        writeTo.setEnabled(false);
        receiveCurrent.setEnabled(false);
        receivePatch.setEnabled(false);
        receiveNextPatch.setEnabled(false);
        getAll.setEnabled(false);
        merge.setEnabled(false);
        blend.setEnabled(false);
        return frame;
        }         

                
    public String getDefaultResourceFileName() { return "KorgVolca.init"; }
    public String getHTMLResourceFileName() { return "KorgVolca.html"; }

    /** Add the global patch category (name, id, number, etc.) */
    public JComponent addNameGlobal(Color color)
        {
        Category globalCategory = new Category(this, getSynthName(), color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
                
        JLabel label = new JLabel("  Synth Type ");
        label.setFont(Style.SMALL_FONT());
        label.setBackground(Style.BACKGROUND_COLOR()); // TRANSPARENT);
        label.setForeground(Style.TEXT_COLOR());

        synthTypeCombo = new JComboBox(VOLCAS);
        synthTypeCombo.setMaximumRowCount(NUM_EDITABLE_VOLCAS);
        synthTypeCombo.setSelectedIndex(getSynthType());
        synthTypeCombo.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                setSynthType(synthTypeCombo.getSelectedIndex(), true);
                //pd.update("beatskicklevel", model);  // doesn't matter what the key is, so I put in "beatskicklevel"
                }
            });
        synthTypeCombo.putClientProperty("JComponent.sizeVariant", "small");
        synthTypeCombo.setEditable(false);
        synthTypeCombo.setFont(Style.SMALL_FONT());
        VBox st = new VBox();
        st.add(label);
        st.addLast(synthTypeCombo);
        hbox.add(st);
  
        comp = new StringComponent("Patch Name", this, "name", 16, "Name must be up to 16 ASCII characters.")
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
        hbox.add(comp);  // doesn't work right :-(
        //vbox.addBottom(Stretch.makeVerticalStretch()); 
        //hbox.add(vbox);

        hbox.addLast(Strut.makeHorizontalStrut(70));
        
        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }



    public JComponent addVolcaBass(Color color)
        {
        Category category = new Category(this, "Bass", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
               
        comp = new LabelledDial("Slide", this, "bassslidetime", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Time");
        hbox.add(comp);
        
        comp = new LabelledDial("Expression", this, "bassexpression", color, 0, 127);
        hbox.add(comp);
        
        comp = new LabelledDial("Octave", this, "bassoctave", color, 0, 127)
            {
            public String map(int val)
                {
                if (val >= 110) return "6";
                else if (val >= 88) return "5";
                else if (val >= 66) return "4";
                else if (val >= 44) return "3";
                else if (val >= 22) return "2";
                else    // if (val >= 00) 
                    return "1";
                }
            };
        hbox.add(comp);
        
        comp = new LabelledDial("LFO", this, "basslforate", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Rate");
        hbox.add(comp);
        
        comp = new LabelledDial("LFO", this, "basslfointensity", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Intensity");
        hbox.add(comp);
        
        for(int i = 1; i <= 3; i++)
            {
            comp = new LabelledDial("VCO " + i, this, "bassvco" + i + "pitch", color, 0, 127)
                {
                // The CC to pitch mapping is entirely undocumented.  But I believe it is:
                // 126, 127             12n
                // 115-125              1n ... 11n
                // 114                  100c (1n)
                // 113                  98c
                // 112                  96c
                // ...
                // 66                   4c
                // 65                   2c
                // 64                   OFF
                // 63                   OFF
                // 62                   -2c
                // 61                   -4c
                // ...
                // 15                   -96c
                // 14                   -98c
                // 13                   -100c (-1n)
                // 2-12                 -11n ... -1n
                // 0, 1                 -12n
                        
                public boolean isSymmetric() { return true; }
                public String map(int value)
                    {
                    if (value >= 126)       return "+12n";
                    if (value >= 115)       return "+" + (value - 114) + "n";
                    if (value == 114)       return "+1n";
                    if (value >= 65)        return "+" + ((value - 64) * 2) + "c";
                    if (value >= 63)        return "Off";
                    if (value >= 14)        return "-" + ((63 - value) * 2) + "c";
                    if (value == 13)        return "-1n";
                    if (value >= 2)         return "-" + (13 - value) + "n";
                    else                            return "-12n";
                    }
                };
            ((LabelledDial)comp).addAdditionalLabel("Pitch");
            hbox.add(comp);
            }
        
        vbox.add(hbox);
        hbox = new HBox();
        
        comp = new LabelledDial("Envelope", this, "bassegattack", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Attack");
        hbox.add(comp);
        
        comp = new LabelledDial("Envelope", this, "bassegdecayrelease", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Decay/Rel");
        hbox.add(comp);
        
        comp = new LabelledDial("Cutoff", this, "basscutoffegintensity", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("EG Intensity");
        hbox.add(comp);
        
        comp = new LabelledDial("Gate", this, "bassgatetime", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Time");
        hbox.add(comp);

        // AR
        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "bassegattack",   "bassegdecayrelease" },
            new String[] { null, null,             null                 },
            new double[] { 0,    0.5/127,          0.5/127              },
            new double[] { 0.0,  1.0,              0.0                  });
        hbox.add(comp);
        
        vbox.add(hbox);
        
        category.add(vbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addVolcaBeats(Color color)
        {
        Category category = new Category(this, "Beats", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();

        LabelledDial kick = new LabelledDial("Kick", this, "beatskicklevel", color, 0, 127);
        comp = kick;
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);

        comp = new LabelledDial("Snare", this, "beatssnarelevel", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);

        comp = new LabelledDial(" Lo Tom ", this, "beatslotomlevel", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);

        comp = new LabelledDial(" Hi Tom ", this, "beatshitomlevel", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);

        comp = new LabelledDial("Closed Hat", this, "beatsclosedhatlevel", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);

        comp = new LabelledDial("Open Hat", this, "beatsopenhatlevel", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);

        comp = new LabelledDial("Clap", this, "beatsclaplevel", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);

        comp = new LabelledDial("Claves", this, "beatsclaveslevel", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);

        comp = new LabelledDial("Agogo", this, "beatsagogolevel", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);

        comp = new LabelledDial("Crash", this, "beatscrashlevel", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);
        
        vbox.add(hbox);
        hbox = new HBox();
        
        hbox.add(Strut.makeStrut(kick));        // kick
        hbox.add(Strut.makeStrut(kick));        // snare
        
        comp = new LabelledDial("Tom", this, "beatstomdecay", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Decay");
        hbox.add(comp);

        hbox.add(Strut.makeStrut(kick));        // hi tom
        
        comp = new LabelledDial("Closed Hat", this, "beatsclosedhatdecay", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Decay");
        hbox.add(comp);

        comp = new LabelledDial("Open Hat", this, "beatsopenhatdecay", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Decay");
        hbox.add(comp);
        
        comp = new LabelledDial("Clap", this, "beatsclappcmspeed", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("PCM Speed");
        hbox.add(comp);

        comp = new LabelledDial("Claves", this, "beatsclavespcmspeed", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("PCM Speed");
        hbox.add(comp);

        comp = new LabelledDial("Agogo", this, "beatsagogopcmspeed", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("PCM Speed");
        hbox.add(comp);

        comp = new LabelledDial("Crash", this, "beatscrashpcmspeed", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("PCM Speed");
        hbox.add(comp);
        
        vbox.add(hbox);
        hbox = new HBox();
        
        comp = new LabelledDial("Stutter", this, "beatsstuttertime", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Time");
        hbox.add(comp);
        vbox.add(hbox);

        comp = new LabelledDial("Stutter", this, "beatsstutterdepth", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Depth");
        hbox.add(comp);
        vbox.add(hbox);

        hbox.add(Strut.makeStrut(kick));        // lo tom
        hbox.add(Strut.makeStrut(kick));        // hi tom
        
        comp = new LabelledDial("Hat", this, "beatshatgrain", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Grain");
        hbox.add(comp);
        vbox.add(hbox);
 
 
        JLabel label = new JLabel("  Test Notes Play ");
        label.setFont(Style.SMALL_FONT());
        label.setBackground(Style.BACKGROUND_COLOR()); // TRANSPARENT);
        label.setForeground(Style.TEXT_COLOR());

        beatsCombo = new JComboBox(BEATS_DRUMS);
        beatsCombo.setMaximumRowCount(BEATS_DRUMS.length);
        beatsCombo.setSelectedIndex(0);
        beatsCombo.putClientProperty("JComponent.sizeVariant", "small");
        beatsCombo.setEditable(false);
        beatsCombo.setFont(Style.SMALL_FONT());
        VBox st = new VBox();
        st.add(label);
        st.addLast(beatsCombo);
        hbox = new HBox();
        hbox.add(st);
        vbox.add(Strut.makeVerticalStrut(8));
        vbox.add(hbox);
 
        
        category.add(vbox, BorderLayout.CENTER);
        return category;
        }
    

    public static final int[] KORG_VOLCA_SINGLE_PART_SELECT_CC = new int[] { 14, 23, 46, 55, 80, 89, 116 };
                
    public static final int[] KORG_VOLCA_SPLIT_LAYER_SELECT_CC = new int[] { 14, 15, 16 };

    public JComponent addVolcaDrumSinglePart(final int part, Color color)
        {
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        Category category = new Category(this, "Part " + part, color);
        category.makePasteable("drumsinglepart");

        
        comp = new LabelledDial("Select", this, "drumsinglepart" + part + "select", color, 0, 127)
            {
            public String map(int value)
                {
                int v = DRUM_SELECT[value];
                int env = v % 3;
                int mod = (v / 3) % 3;
                int src = (v / 3) / 3;
                return "<html><center><font size=-2>" + DRUM_SOURCES[src] + "<br>" + DRUM_MODULATORS[mod] + "<br>" + DRUM_ENVELOPES[env] + "</font></center></html>";
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Level", this, "drumsinglepart" + part + "level", color, 0, 127)
            {
            public String map(int value)
                {
                if (value == 127) return "255";
                else return "" + (2 * value);
                }
            };
        hbox.add(comp);

// See here:   https://www.reddit.com/r/volcas/comments/cxzyq7/korg_volca_drum_pitch_values_converted_to_note/
        comp = new LabelledDial("Pitch", this, "drumsinglepart" + part + "pitch", color, 0, 127)
            {
            public String map(int value)
                {
                if (value == 127) return "255";
                else return "" + (2 * value);
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Pan", this, "drumsinglepart" + part + "pan", color, 0, 127)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                int v = DRUM_100[value];
                if (v == 0) return "--";
                if (v < 0) return "< " + (0 - v);
                else return "" + v + " >";
                }
            };
        hbox.add(comp);
        
        comp = new LabelledDial("Send", this, "drumsinglepart" + part + "send", color, 0, 127)
            {
            public String map(int value)
                {
                if (value == 127) return "255";
                else return "" + (2 * value);
                }
            };
        hbox.add(comp);
        
        comp = new LabelledDial("Mod Amt", this, "drumsinglepart" + part + "modamt", color, 0, 127)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                return "" + DRUM_100[value];
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Mod Rate", this, "drumsinglepart" + part + "modrate", color, 0, 127)
            {
            public String map(int value)
                {
                if (value == 127) return "255";
                else return "" + (2 * value);
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Attack", this, "drumsinglepart" + part + "attack", color, 0, 127)
            {
            public String map(int value)
                {
                if (value == 127) return "255";
                else return "" + (2 * value);
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Release", this, "drumsinglepart" + part + "release", color, 0, 127)
            {
            public String map(int value)
                {
                if (value == 127) return "255";
                else return "" + (2 * value);
                }
            };
        hbox.add(comp);

        // AR
        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "drumsinglepart" + part + "attack",   "drumsinglepart" + part + "release" },
            new String[] { null, null,             null                 },
            new double[] { 0,    0.5/127,          0.5/127              },
            new double[] { 0.0,  1.0,              0.0                  });
        hbox.add(comp);

        category.add(hbox);
        return category;
        }


    public JComponent addVolcaDrumSingle(Color color)
        {
        Category category = new Category(this, "Drum [Single]", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
               
        comp = new LabelledDial("Waveguide", this, "drumsinglewaveguidemodel", color, 0, 127)
            {
            public String map(int value)
                {
                if (value < 64) return "1";
                else return "2";
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Model");
        hbox.add(comp);
        
        comp = new LabelledDial("Decay", this, "drumsingledecay", color, 0, 127)
            {
            public String map(int value)
                {
                if (value == 127) return "255";
                else return "" + (2 * value);
                }
            };
        hbox.add(comp);
        
        comp = new LabelledDial("Body", this, "drumsinglebody", color, 0, 127)
            {
            public String map(int value)
                {
                if (value == 127) return "255";
                else return "" + (2 * value);
                }
            };
        hbox.add(comp);
        
        comp = new LabelledDial("Tune", this, "drumsingletune", color, 0, 127)
            {
            public String map(int value)
                {
                if (value == 127) return "255";
                else return "" + (2 * value);
                }
            };
        hbox.add(comp);


        JLabel label = new JLabel("  Test Notes Play Part ");
        label.setFont(Style.SMALL_FONT());
        label.setBackground(Style.BACKGROUND_COLOR()); // TRANSPARENT);
        label.setForeground(Style.TEXT_COLOR());

        drumSingleCombo = new JComboBox(DRUM_SINGLE_PARTS);
        drumSingleCombo.setMaximumRowCount(DRUM_SINGLE_PARTS.length);
        drumSingleCombo.setSelectedIndex(0);
        drumSingleCombo.putClientProperty("JComponent.sizeVariant", "small");
        drumSingleCombo.setEditable(false);
        drumSingleCombo.setFont(Style.SMALL_FONT());
        VBox st = new VBox();
        HBox hbox2 = new HBox();
        hbox2.add(label);
        st.add(hbox2);
        st.add(drumSingleCombo);
        hbox.add(st);

        vbox.add(hbox);
        
        vbox.add(Strut.makeVerticalStrut(10));
        vbox.add(new Category(this, "", Style.COLOR_A()));
        vbox.add(Strut.makeVerticalStrut(10));
                        
        final JComponent typical = addVolcaDrumSinglePart(1, color);
        final int h = typical.getPreferredSize().height;
        final int w = typical.getPreferredSize().width;

        VBox svbox = new VBox()
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
                
        JScrollPane pane = new JScrollPane(svbox, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        pane.getViewport().setBackground(Style.BACKGROUND_COLOR());
        pane.setBorder(null);
                

        for(int part = 1; part <= 6; part++)
            {
            svbox.add(addVolcaDrumSinglePart(part, (part % 2 == 1 ? Style.COLOR_B() : Style.COLOR_A())));
            }
                
        vbox.addLast(pane);
        category.add(vbox, BorderLayout.CENTER);
        return category;
        }
        

    public JComponent addVolcaDrumSplitLayer(int part, int layer, Color color)
        {
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
               
        String[] label = new String[] { null, "1", "2", "1-2" };
                
        comp = new LabelledDial("Select", this, "drumsplitpart" + part + "layer" + layer + "select", color, 0, 127)
            {
            public String map(int value)
                {
                int v = DRUM_SELECT[value];
                int env = v % 3;
                int mod = (v / 3) % 3;
                int src = (v / 3) / 3;
                return "<html><center><font size=-2>" + DRUM_SOURCES[src] + "<br>" + DRUM_MODULATORS[mod] + "<br>" + DRUM_ENVELOPES[env] + "</font></center></html>";
                }
            };
        ((LabelledDial)comp).addAdditionalLabel(label[layer]);
        hbox.add(comp);

        comp = new LabelledDial("Level", this, "drumsplitpart" + part + "layer" + layer + "level", color, 0, 127)
            {
            public String map(int value)
                {
                if (value == 127) return "255";
                else return "" + (2 * value);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel(label[layer]);
        hbox.add(comp);

        comp = new LabelledDial("Pitch", this, "drumsplitpart" + part + "layer" + layer + "pitch", color, 0, 127)
            {
            public String map(int value)
                {
                if (value == 127) return "255";
                else return "" + (2 * value);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel(label[layer]);
        hbox.add(comp);

        comp = new LabelledDial("Mod Amt", this, "drumsplitpart" + part + "layer" + layer + "modamt", color, 0, 127)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                return "" + DRUM_100[value];
                }
            };
        
        ((LabelledDial)comp).addAdditionalLabel(label[layer]);
        hbox.add(comp);

        comp = new LabelledDial("Mod Rate", this, "drumsplitpart" + part + "layer" + layer + "modrate", color, 0, 127)
            {
            public String map(int value)
                {
                if (value == 127) return "255";
                else return "" + (2 * value);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel(label[layer]);
        hbox.add(comp);

        comp = new LabelledDial("Attack", this, "drumsplitpart" + part + "layer" + layer + "attack", color, 0, 127)
            {
            public String map(int value)
                {
                if (value == 127) return "255";
                else return "" + (2 * value);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel(label[layer]);
        hbox.add(comp);

        comp = new LabelledDial("Release", this, "drumsplitpart" + part + "layer" + layer + "release", color, 0, 127)
            {
            public String map(int value)
                {
                if (value == 127) return "255";
                else return "" + (2 * value);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel(label[layer]);
        hbox.add(comp);

        // AR
        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "drumsplitpart" + part + "layer" + layer + "attack",   "drumsplitpart" + part + "layer" + layer + "release" },
            new String[] { null, null,             null                 },
            new double[] { 0,    0.5/127,          0.5/127              },
            new double[] { 0.0,  1.0,              0.0                  });
        hbox.add(comp);
        return hbox;                            
        }
    

    public JComponent addVolcaDrumSplitPart(final int part, Color color)
        {
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        Category category = new Category(this, "Part " + part, color);
        category.makePasteable("drumsplitpart");

                
        vbox.add(addVolcaDrumSplitLayer(part, 3, color));
        vbox.add(addVolcaDrumSplitLayer(part, 1, color));
        vbox.add(addVolcaDrumSplitLayer(part, 2, color));
        hbox.add(vbox);
        // hbox.add(Strut.makeHorizontalStrut(10));
        
        vbox = new VBox();
        HBox hbox2 = new HBox();
        
        comp = new LabelledDial("Pan", this, "drumsplitpart" + part + "pan", color, 0, 127)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                int v = DRUM_100[value];
                if (v == 0) return "--";
                if (v < 0) return "< " + (0 - v);
                else return "" + v + " >";
                }
            };
        hbox2.add(comp);
        
        comp = new LabelledDial("Bit", this, "drumsplitpart" + part + "bitreduction", color, 0, 127)
            {
            public String map(int value)
                {
                if (value == 127) return "255";
                else return "" + (2 * value);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Reduction");
        hbox2.add(comp);

        vbox.add(hbox2);
        hbox2 = new HBox();
        
        comp = new LabelledDial("Fold", this, "drumsplitpart" + part + "fold", color, 0, 127)
            {
            public String map(int value)
                {
                if (value == 127) return "255";
                else return "" + (2 * value);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel(" ");                    // stub for spacing
        hbox2.add(comp);
        
        comp = new LabelledDial("Drive", this, "drumsplitpart" + part + "drive", color, 0, 127)
            {
            public String map(int value)
                {
                if (value == 127) return "255";
                else return "" + (2 * value);
                }
            };
        hbox2.add(comp);
        
        vbox.add(hbox2);
        hbox2 = new HBox();
        
        comp = new LabelledDial("Dry", this, "drumsplitpart" + part + "drygain", color, 0, 127)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                return "" + DRUM_100[value];
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Gain");
        hbox2.add(comp);
        
        comp = new LabelledDial("Send", this, "drumsplitpart" + part + "send", color, 0, 127)
            {
            public String map(int value)
                {
                if (value == 127) return "255";
                else return "" + (2 * value);
                }
            };
        hbox2.add(comp);
        vbox.add(hbox2);
        hbox.add(vbox);
                
        category.add(hbox);
        return category;
        }
                        
                        
    public JComponent addVolcaDrumSplit(Color color)
        {
        Category category = new Category(this, "Drum [Split]", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        comp = new LabelledDial("Waveguide", this, "drumsplitwaveguidemodel", color, 0, 127)
            {
            public String map(int value)
                {
                if (value < 64) return "1";
                else return "2";
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Model");
        hbox.add(comp);
        
        comp = new LabelledDial("Decay", this, "drumsplitdecay", color, 0, 127)
            {
            public String map(int value)
                {
                if (value == 127) return "255";
                else return "" + (2 * value);
                }
            };
        hbox.add(comp);
        
        comp = new LabelledDial("Body", this, "drumsplitbody", color, 0, 127)
            {
            public String map(int value)
                {
                if (value == 127) return "255";
                else return "" + (2 * value);
                }
            };
        hbox.add(comp);
        
        comp = new LabelledDial("Tune", this, "drumsplittune", color, 0, 127)
            {
            public String map(int value)
                {
                if (value == 127) return "255";
                else return "" + (2 * value);
                }
            };
        hbox.add(comp);
        
        JLabel label = new JLabel("  Test Notes Play Part ");
        label.setFont(Style.SMALL_FONT());
        label.setBackground(Style.BACKGROUND_COLOR()); // TRANSPARENT);
        label.setForeground(Style.TEXT_COLOR());

        drumCombo = new JComboBox(DRUM_SPLIT_PARTS);
        drumCombo.setMaximumRowCount(DRUM_SPLIT_PARTS.length);
        drumCombo.setSelectedIndex(0);
        drumCombo.putClientProperty("JComponent.sizeVariant", "small");
        drumCombo.setEditable(false);
        drumCombo.setFont(Style.SMALL_FONT());
        VBox st = new VBox();
        HBox stb = new HBox();
        stb.add(label);
        st.add(stb);
        st.add(drumCombo);
        hbox.add(st);
        vbox.add(hbox);
        
        vbox.add(Strut.makeVerticalStrut(10));
        vbox.add(new Category(this, "", Style.COLOR_A()));
        vbox.add(Strut.makeVerticalStrut(10));
                        
        final JComponent typical = addVolcaDrumSplitPart(1, color);
        final int h = typical.getPreferredSize().height;
        final int w = typical.getPreferredSize().width;

        VBox svbox = new VBox()
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
                size.height = h;
                return size;
                }
            };
                
        JScrollPane pane = new JScrollPane(svbox, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        pane.getViewport().setBackground(Style.BACKGROUND_COLOR());
        pane.setBorder(null);
                

        for(int part = 1; part <= 6; part++)
            {
            svbox.add(addVolcaDrumSplitPart(part, (part % 2 == 0 ? Style.COLOR_A() : Style.COLOR_B())));
            }
                
        vbox.addLast(pane);
        category.add(vbox, BorderLayout.CENTER);
        return category;
        }
        


    public JComponent addVolcaFM(Color color)
        {
        Category category = new Category(this, "FM", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
               
        comp = new LabelledDial("Algorithm", this, "fmalgorithm", color, 0, 127)
            {
            // The Volca FM MIDIImp documentation is very wrong here.  It says that
            // the values 0... 127 map to algorithms "0...32".  But that's 33 algorithms
            // and the DX7 obviously has only 32 algorithms.  Thus they have this weird
            // and incorrect offset mapping as shown below.  
            //
            // *7 : [H] [D]    [H] [D]  : [D]
            // 00 (0)   ~ 07 (07)  :  0,  0,  0,  0,  1,  1,  1,  1
            // 08 (8)   ~ 0F (15)  :  2,  2,  2,  2,  3,  3,  3,  3
            // 10 (16)  ~ 17 (23)  :  4,  4,  4,  4,  5,  5,  5,  5
            // 18 (24)  ~ 1B (27)  :  6,  6,  6,  6,  7,  7,  7,  7
            // 20 (32)  ~ 27 (39)  :  8,  8,  8,  9,  9,  9,  9, 10
            // 28 (40)  ~ 2F (47)  : 10, 10, 10, 11, 11, 11, 11, 12
            // 30 (48)  ~ 37 (55)  : 12, 12, 12, 13, 13, 13, 13, 14
            // 38 (56)  ~ 3F (63)  : 14, 14, 14, 15, 15, 15, 15, 16
            // 40 (64)  ~ 47 (71)  : 16, 16, 17, 17, 17, 17, 18, 18
            // 48 (72)  ~ 4F (79)  : 18, 18, 19, 19, 19, 19, 20, 20
            // 50 (80)  ~ 57 (87)  : 20, 20, 21, 21, 21, 21, 22, 22
            // 58 (88)  ~ 5F (95)  : 22, 22, 23, 23, 23, 23, 24, 24
            // 60 (96)  ~ 67 (103) : 24, 25, 25, 25, 25, 26, 26, 26
            // 68 (104) ~ 6F (111) : 26, 27, 27, 27, 27, 28, 28, 28
            // 70 (112) ~ 77 (119) : 28, 29, 29, 29, 29, 30, 30, 30
            // 78 (120) ~ 7F (127) : 30, 31, 31, 31, 31, 32, 32, 32
            //
            // However the correct answer appears to map
            // 0...127 to algorithm 0...31 just by dividing the value by 4.  This was
            // the case at least as of firmware 004, and certainly by 007.  So I dunno
            // what this mapping is supposed to do, maybe it was an outdated internal
            // test thing or something.
                
            public String map(int value)
                {
                return "" + ((value / 4) + 1);
                }

            };
        hbox.add(comp);
        
        comp = new LabelledDial("Transpose", this, "fmtranspose", color, 0, 127)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                // Octave Transpose values
                int oct = -3;
                if (value >= 110) oct = 3;
                else if (value >= 90) oct = 2;
                else if (value >= 74) oct = 1;
                else if (value >= 55) oct = 0;
                else if (value >= 37) oct = -1;
                else if (value >= 19) oct = -2;

                // Semitone Transpose values
                int semi = FM_TRANSPOSE[value];
                        
                if (oct == 0 && semi == 1) return "--";
                else return "" + oct + ":" + semi;
                }
            };
        hbox.add(comp);
        
        comp = new LabelledDial("Velocity", this, "fmevelocity", color, 0, 127) 
            {
            public String map(int value)
                {
                if (value == 0) value = 1;              // no zero velocity
                return "" + value;
                }
            };
        hbox.add(comp);
        
        comp = new LabelledDial("LFO", this, "fmlforate", color, 0, 127)
            {
            public String map(int value)
                {
                return "" + FM_LFO[value];
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Rate");
        hbox.add(comp);
        
        comp = new LabelledDial("LFO", this, "fmlfopitchdepth", color, 0, 127)
            {
            public String map(int value)
                {
                return "" + FM_LFO[value];
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Pitch Depth");
        hbox.add(comp);
        
        comp = new LabelledDial("Arpeggiator", this, "fmarpeggiatortype", color, 0, 127)
            {
            public String map(int value)
                {
                if (value >= 116) return "Rand3";
                if (value >= 103) return "Rand2";
                if (value >= 90) return "Rand1";
                if (value >= 77) return "Fall3";
                if (value >= 64) return "Fall2";
                if (value >= 52) return "Fall1";
                if (value >= 39) return "Rise3";
                if (value >= 26) return "Rise2";
                if (value >= 13) return "Rise1";
                return "Off";                   
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Type");
        hbox.add(comp);
        
        comp = new LabelledDial("Arpeggiator", this, "fmarpeggiatordiv", color, 0, 127)
            {
            public String map(int value)
                {
                // bizarrely this is significantly different from Arpeggiator Type
                if (value >= 117) return "4-1";
                if (value >= 105) return "3-1";
                if (value >= 94) return "2-1";
                if (value >= 82) return "3-2";
                if (value >= 70) return "1-1";
                if (value >= 59) return "2-3";
                if (value >= 47) return "1-2";
                if (value >= 35) return "1-3";
                if (value >= 24) return "1-4";
                if (value >= 12) return "1-8";
                return "1-12";                  
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Div");
        hbox.add(comp);
        
        comp = new LabelledDial("Modulator", this, "fmmodulatorattack", color, 0, 127)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                return "" + FM_ATTACK[value];
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Attack");
        hbox.add(comp);
        
        comp = new LabelledDial("Modulator", this, "fmmodulatordecay", color, 0, 127)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                return "" + FM_ATTACK[value];
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Decay");
        hbox.add(comp);
        
        comp = new LabelledDial("Carrier", this, "fmcarrierattack", color, 0, 127)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                return "" + FM_ATTACK[value];
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Attack");
        hbox.add(comp);
        
        comp = new LabelledDial("Carrier", this, "fmcarrierdecay", color, 0, 127)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                return "" + FM_ATTACK[value];
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Decay");
        hbox.add(comp);
        
        vbox.add(hbox);
        vbox.add(Strut.makeVerticalStrut(10));
        
        hbox = new HBox();
        hbox.add(new TextLabel("  Algorithm Display "));
        vbox.add(hbox);
        
        vbox.add(Strut.makeVerticalStrut(6));

        hbox = new HBox();
        hbox.add(new TextLabel("  "));
        comp = new IconDisplay(null, ALGORITHM_ICONS, this, "fmalgorithm", 104, 104);
        hbox.add(comp);
        vbox.add(hbox);
        
        vbox.add(Strut.makeVerticalStrut(10));


        hbox = new HBox();
        comp = new PushButton("DX7 Editor")
            {
            public void perform()
                {
                final YamahaDX7 synth = new YamahaDX7();
                if (tuple != null)
                    synth.tuple = new Midi.Tuple(tuple, synth.buildInReceiver(), synth.buildKeyReceiver(), synth.buildKey2Receiver());
                if (synth.tuple != null)
                    {       
                    synth.sprout();
                    JFrame frame = ((JFrame)(SwingUtilities.getRoot(synth)));
                    frame.setVisible(true);
                    }
                else
                    {
                    showSimpleError("Disconnected", "You can't show a patch when disconnected.");
                    }
                }            
            };
        hbox.add(comp);

        hbox.add(new TextLabel("      The Yamaha DX7 editor can upload patches and banks to the Volca.  See Volca and Yamaha DX7 About Panels. "));
        vbox.add(hbox);

        vbox.add(Strut.makeVerticalStrut(10));

        Category category2 = new Category(this, "Pajen FM Firmware", Style.COLOR_B());
        hbox = new HBox();
               
        comp = new LabelledDial("Chorus", this, "fmpajenchorus", color, 0, 127)
            {
            public String map(int value)
                {
                if (value < 64) return "Off";
                return "On";
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Chorus", this, "fmpajenchorusstereowidth", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Stereo Width");
        hbox.add(comp);

        comp = new LabelledDial("Chorus", this, "fmpajenchorusspeed", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Speed");
        hbox.add(comp);

        comp = new LabelledDial("Load", this, "fmpajenloadpatch", color, 0, 127)
            {
            // The documentation at https://www.reddit.com/r/volcas/comments/dj7f7v/volca_fm_firmware_108_unofficial_velocity_on_note/
            // May be off by one.  A fencepost error?  Pajen semi-confirmed it to me.
            public String map(int value)
                {
                return "" + ((value / 4) + 1);                  // 
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Patch");
        hbox.add(comp);

        comp = new LabelledDial("Load", this, "fmpajenloadpattern", color, 0, 127)
            {
            // The documentation at https://www.reddit.com/r/volcas/comments/dj7f7v/volca_fm_firmware_108_unofficial_velocity_on_note/
            // May be off by one.  A fencepost error?  Pajen semi-confirmed it to me.
            public String map(int value)
                {
                return "" + ((value / 8) + 1);                  // 
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Pattern");
        hbox.add(comp);

        comp = new LabelledDial("Tempo", this, "fmpajentempodivisor", color, 0, 127)
            {
            public String map(int value)
                {
                if (value < 32) return "1/1";
                if (value < 64) return "1/2";
                if (value < 96) return "1/4";
                return "1/8";
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Divisor");
        hbox.add(comp);

        comp = new LabelledDial("Mod Wheel", this, "fmpajenmodwheelcc", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("CC");
        hbox.add(comp);
        category2.add(hbox);

        vbox.add(category2);

        vbox.add(Strut.makeVerticalStrut(10));

        hbox = new HBox();
        hbox.add(new TextLabel(" With the Pajen firmware you can also make real-time parameter changes via the Yamaha DX7 editor. "));
        vbox.add(hbox);

        category.add(vbox, BorderLayout.CENTER);
        return category;
        }


// See https://midi.user.camp/d/korg/volca-keys/ for interpretation

    public JComponent addVolcaKeys(Color color)
        {
        Category category = new Category(this, "Keys", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
               
        comp = new LabelledDial("Portamento", this, "keysportamento", color, 0, 127);
        hbox.add(comp);
        
        comp = new LabelledDial("Detune", this, "keysdetune", color, 0, 127);
        hbox.add(comp);
        
        comp = new LabelledDial("VCO", this, "keysenvelopeintensity", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("EG Int");
        hbox.add(comp);
        
        // default should be 127
        comp = new LabelledDial("Expression", this, "keysexpression", color, 0, 127);
        hbox.add(comp);
        
        // eventually change this to a chooser?
        comp = new LabelledDial("Voice", this, "keysvoice", color, 0, 127)
            {
            public String map(int val)
                {
                if (val >= 113) return "PolyR";
                else if (val >= 88) return "UniR";
                else if (val >= 63) return "5th";
                else if (val >= 38) return "Oct";
                else if (val >= 13) return "Uni";
                else    // if (val >= 00) 
                    return "Poly";
                }
            };
        hbox.add(comp);
        
        comp = new LabelledDial("Octave", this, "keysoctave", color, 0, 127)
            {
            public String map(int val)
                {
                if (val >= 110) return "1'";
                else if (val >= 88) return "2'";
                else if (val >= 66) return "4'";
                else if (val >= 44) return "8'";
                else if (val >= 22) return "16'";
                else    // if (val >= 00) 
                    return "32'";
                }
            };
        hbox.add(comp);
        
        comp = new LabelledDial("VCF", this, "keysvcfcutoff", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Cutoff");
        hbox.add(comp);
        
        comp = new LabelledDial("VCF", this, "keysvcfenvelopeintensity", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("EG Int");
        hbox.add(comp);
        
        comp = new LabelledDial("LFO", this, "keyslforate", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Rate");
        hbox.add(comp);
        
        comp = new LabelledDial("LFO", this, "keyslfopitchintensity", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Pitch Int");
        hbox.add(comp);

        comp = new LabelledDial("LFO", this, "keyslfocutoffintensity", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Cutoff Int");
        hbox.add(comp);
                
        vbox.add(hbox);
        hbox = new HBox();
        
        comp = new LabelledDial("Delay", this, "keysdelaytime", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Time");
        hbox.add(comp);

        comp = new LabelledDial("Delay", this, "keysdelayfeedback", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Feedback");
        hbox.add(comp);
        
        comp = new LabelledDial("Envelope", this, "keysenvelopeattack", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Attack");
        hbox.add(comp);
        
        comp = new LabelledDial("Envelope", this, "keysenvelopedecayrelease", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Decay/Release");
        hbox.add(comp);
        
        comp = new LabelledDial("Envelope", this, "keysenvelopesustain", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Sustain");
        hbox.add(comp);

        // ASR
        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "keysenvelopeattack",   "keysenvelopedecayrelease", null,  "keysenvelopedecayrelease"},
            new String[] { null, null,             "keysenvelopesustain",       "keysenvelopesustain",                null },
            new double[] { 0,    0.25/127,         0.25/127,            0.25,           0.25/127  },
            new double[] { 0.0,  1.0,              1.0/127.0,       1.0/127.0,  0  });
        hbox.add(comp);
                
        vbox.add(hbox);

        category.add(vbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addVolcaKick(Color color)
        {
        Category category = new Category(this, "Kick", color);
                
        JComponent comp;
        String[] params;
        VBox vbox = new VBox();
        HBox hbox = new HBox();
               
        comp = new LabelledDial("Pulse", this, "kickpulsecolor", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Color");
        hbox.add(comp);
        
        comp = new LabelledDial("Pulse", this, "kickpulselevel", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);
        
        comp = new LabelledDial("Drive", this, "kickdrive", color, 0, 127);
        hbox.add(comp);
        
        comp = new LabelledDial("Tone", this, "kicktone", color, 0, 127);
        hbox.add(comp);
        
        comp = new LabelledDial("Resonator", this, "kickresonatorpitch", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Pitch");
        hbox.add(comp);
        
        comp = new LabelledDial("Resonator", this, "kickresonatorbend", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Bend");
        hbox.add(comp);
        
        comp = new LabelledDial("Resonator", this, "kickresonatortime", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Time");
        hbox.add(comp);
        
        vbox.add(hbox);
        hbox = new HBox();
        
        comp = new LabelledDial("Accent", this, "kickaccent", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Amp", this, "kickampattack", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Attack");
        hbox.add(comp);
        
        comp = new LabelledDial("Amp", this, "kickampdecay", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Decay");
        hbox.add(comp);

        // AR
        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "kickampattack",   "kickampdecay" },
            new String[] { null, null,             null                 },
            new double[] { 0,    0.5/127,          0.5/127              },
            new double[] { 0.0,  1.0,              0.0                  });
        hbox.add(comp);
        vbox.add(hbox);
        
        category.add(vbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addVolcaNuBass(Color color)
        {
        Category category = new Category(this, "NuBass", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
              
        comp = new LabelledDial("VTO", this, "nubassvtopitch", color, 0, 127)
            {
            public boolean isSymmetric() { return true; }

            public String map(int value)
                {
                int v = NUBASS_PITCH[value];
                if (value <= 56)
                    {
                    if (v % 100 == 0)
                        return "" + (v / 100) + "n";
                    else return "" + v + "c";
                    }
                else if (value >= 72)
                    {
                    if (v % 100 == 0)
                        return "+" + (v / 100) + "n";
                    else return "+" + v + "c";
                    }
                else
                    {
                    return "Off";
                    }
                }               
            };
        ((LabelledDial)comp).addAdditionalLabel("Pitch");
        hbox.add(comp);
        
        comp = new LabelledDial("VTO", this, "nubassvtosaturation", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Saturation");
        hbox.add(comp);
        
        comp = new LabelledDial("VTO", this, "nubassvtolevel", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);
        
        comp = new LabelledDial("Accent", this, "nubassaccent", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("LFO", this, "nubasslforate", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Rate");
        hbox.add(comp);

        comp = new LabelledDial("LFO", this, "nubasslfointensity", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Intensity");
        hbox.add(comp);

        vbox.add(hbox);
        hbox = new HBox();
                
        comp = new LabelledDial("VCF", this, "nubassvcfcutoff", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Cutoff");
        hbox.add(comp);
        
        comp = new LabelledDial("VCF", this, "nubassvcfpeak", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Peak");
        hbox.add(comp);
        
        comp = new LabelledDial("VCF", this, "nubassvcfattack", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Attack");
        hbox.add(comp);
        
        comp = new LabelledDial("VCF", this, "nubassvcfdecay", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Decay");
        hbox.add(comp);
        
        comp = new LabelledDial("VCF", this, "nubassvcfegintensity", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("EG Int");
        hbox.add(comp);
        
        // AR
        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "nubassvcfattack",   "nubassvcfdecay" },
            new String[] { null, null,             null                 },
            new double[] { 0,    0.5/127,          0.5/127              },
            new double[] { 0.0,  1.0,              0.0                  });
        hbox.add(comp);
                
        vbox.add(hbox);
        
        category.add(vbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addVolcaSamplePart(int part, Color color)
        {
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        Category category = new Category(this, "Part " + part, color);
        category.makePasteable("sample1part");

                
        comp = new LabelledDial("Level", this, "sample1part" + part + "level", color, 0, 127);
        hbox.add(comp);
                
        comp = new LabelledDial("Pan", this, "sample1part" + part + "pan", color, 0, 127)
            {
            public boolean isSymmetric() { return true; }
                                
            public String map(int value)
                {
                if (value == 64) return "--";
                else if (value == 0) return "< 63";
                else if (value < 64) return "< " + (64 - value);
                else return  (value - 64) + " >";
                }
            };
        hbox.add(comp);
                
        comp = new LabelledDial("Hi", this, "sample1part" + part + "hicutoff", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Cutoff");
        hbox.add(comp);

        comp = new LabelledDial(" Amp Env ", this, "sample1part" + part + "ampegattack", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Attack");
        hbox.add(comp);
                
        comp = new LabelledDial(" Amp Env ", this, "sample1part" + part + "ampegdecay", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Decay");
        hbox.add(comp);
                
        // AR
        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "sample1part" + part + "ampegattack",   "sample1part" + part + "ampegdecay" },
            new String[] { null, null,             null                 },
            new double[] { 0,    0.5/127,          0.5/127              },
            new double[] { 0.0,  1.0,              0.0                  });
        hbox.add(comp);

        comp = new LabelledDial("Speed", this, "sample1part" + part + "speed", color, 0, 127)
            {
            public boolean isSymmetric()
                {
                return true;
                }
                
            public String map(int value)
                {
                if (value == 0) return "-63";
                else return "" + (value - 64);
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Reverse", this, "sample1part" + part + "pajenreversepart", color, 0, 127)
            {
            public String map(int value)
                {
                if (value == 0) return "Off";
                else if (value > 63) return "Toggle";
                else return "On";
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Part [P]");
        hbox.add(comp);

        comp = new LabelledDial("Mute", this, "sample1part" + part + "pajenmutepart", color, 0, 127)
            {
            public String map(int value)
                {
                if (value == 0) return "Off";
                else if (value > 63) return "Toggle";
                else return "On";
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Part [P]");
        hbox.add(comp);

        comp = new LabelledDial("Solo", this, "sample1part" + part + "pajensolopart", color, 0, 127)
            {
            public String map(int value)
                {
                if (value == 0) return "Off";
                else if (value > 63) return "Toggle";
                else return "On";
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Part [P]");
        hbox.add(comp);


        vbox.add(hbox);
        hbox = new HBox();
                                                                        
        comp = new LabelledDial("Sample", this, "sample1part" + part + "startpoint", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Start");
        hbox.add(comp);
                        
        comp = new LabelledDial("Sample", this, "sample1part" + part + "length", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Length");
        hbox.add(comp);

        comp = new LabelledDial("Pitch Env", this, "sample1part" + part + "pitchegintensity", color, 0, 127)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                if (value == 0) return "-63";
                else return "" + (value - 64);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Intensity");
        hbox.add(comp);
                        
        comp = new LabelledDial("Pitch Env", this, "sample1part" + part + "pitchegattack", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Attack");
        hbox.add(comp);
                        
        comp = new LabelledDial("Pitch Env", this, "sample1part" + part + "pitchegdecay", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Decay");
        hbox.add(comp);
                        
        // AR
        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "sample1part" + part + "pitchegattack",   "sample1part" + part + "pitchegdecay" },
            new String[] { null, null,             null                 },
            new double[] { 0,    0.5/127,          0.5/127              },
            new double[] { 0.0,  1.0,              0.0                  });
        hbox.add(comp);


        comp = new LabelledDial("Sample", this, "sample1part" + part + "pajensampleselect", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Select [P]");
        hbox.add(comp);
                
        comp = new LabelledDial("Reverb", this, "sample1part" + part + "pajenreverbonoff", color, 0, 127)
            {
            public String map(int value)
                {
                if (value == 0) return "Off";
                else if (value > 63) return "Toggle";
                else return "On";
                }
            };
        
        ((LabelledDial)comp).addAdditionalLabel("On/Off [P]");
        hbox.add(comp);
        
        comp = new LabelledDial("Loop", this, "sample1part" + part + "pajenloop", color, 0, 127)
            {
            public String map(int value)
                {
                if (value == 0) return "Off";
                else if (value > 63) return "Toggle";
                else return "On";
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Part [P]");
        hbox.add(comp);

        vbox.add(hbox);
        category.add(vbox);

        return category;
        }

    public JComponent addVolcaSample(Color color)
        {
        Category category = new Category(this, "Sample / Sample2 [Multi Channel]", color);
                
        JComponent comp;
        VBox vbox;
        HBox hbox;
        String[] params;
        
        vbox = new VBox();
        hbox = new HBox();
        
        comp = new LabelledDial("Part", this, "sample1part" + 1 + "pajenpartselect", color, 0, 127)
            {
            public String map(int value)
                {
                if (value <= 12) return "1";
                else if (value <= 25) return "2";
                else if (value <= 38) return "3";
                else if (value <= 51) return "4";
                else if (value <= 63) return "5";
                else if (value <= 76) return "6";
                else if (value <= 89) return "7";
                else if (value <= 102) return "8";
                else if (value <= 115) return "9";
                else return "10";
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Select [P]");
        hbox.add(comp);
        
        // make some dummy labelled dials
        for(int i = 2; i <= 10; i++)
            {
            comp = new LabelledDial("Part", this, "sample1part" + i + "pajenpartselect", color, 0, 127);
            }

        comp = new LabelledDial("Reverb", this, "sample1part" + 1 + "pajenreverbtype", color, 0, 127)
            {
            public String map(int value)
                {
                if (value <= 42) return "0";
                else if (value <= 85) return "1";
                else return "2";
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Type [P]");
        hbox.add(comp);

        // make some dummy labelled dials
        for(int i = 2; i <= 10; i++)
            {
            comp = new LabelledDial("Reverb", this, "sample1part" + i + "pajenreverbtype", color, 0, 127);
            }

        comp = new LabelledDial("Reverb", this, "sample1part" + 1 + "pajenreverblevel", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Level [P]");
        hbox.add(comp);

        // make some dummy labelled dials
        for(int i = 2; i <= 10; i++)
            {
            comp = new LabelledDial("Reverb", this, "sample1part" + i + "pajenreverblevel", color, 0, 127);
            }

        JLabel label = new JLabel("  Test Notes Play Part ");
        label.setFont(Style.SMALL_FONT());
        label.setBackground(Style.BACKGROUND_COLOR()); // TRANSPARENT);
        label.setForeground(Style.TEXT_COLOR());

        sampleCombo = new JComboBox(SAMPLE_PARTS);
        sampleCombo.setMaximumRowCount(SAMPLE_PARTS.length);
        sampleCombo.setSelectedIndex(0);
        sampleCombo.putClientProperty("JComponent.sizeVariant", "small");
        sampleCombo.setEditable(false);
        sampleCombo.setFont(Style.SMALL_FONT());
        VBox st = new VBox();
        HBox stb = new HBox();
        stb.add(label);
        st.add(stb);
        st.add(sampleCombo);
        hbox.add(st);
        vbox.add(hbox);
        
        vbox.add(Strut.makeVerticalStrut(10));
        vbox.add(new Category(this, "", Style.COLOR_A()));
        vbox.add(Strut.makeVerticalStrut(10));
                        

        final JComponent typical = addVolcaSamplePart(1, color);
        final int h = typical.getPreferredSize().height;
        final int w = typical.getPreferredSize().width;
        

        VBox vbox2 = new VBox()
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
                size.height = h * 2;
                return size;
                }
            };
                
        for(int part = 1; part <= 10; part++)
            {
            vbox2.add(addVolcaSamplePart(part, (part % 2 == 0 ? Style.COLOR_A() : Style.COLOR_B())));
            }
                
        JScrollPane pane = new JScrollPane(vbox2, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        pane.getViewport().setBackground(Style.BACKGROUND_COLOR());
        pane.setBorder(null);
        
        vbox.add(pane);
                                                
        category.add(vbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addVolcaSample2Part(int part, Color color)
        {
        String prefix = "sample2";
                        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        Category category = new Category(this, "Part " + part, color);
        category.makePasteable("sample2part");
                                
        comp = new LabelledDial("Level", this, prefix + "part" + part + "level", color, 0, 127);
        hbox.add(comp);
                
        comp = new LabelledDial("Pan", this, prefix + "part" + part + "pan", color, 0, 127)
            {
            public boolean isSymmetric() { return true; }
                                
            public String map(int value)
                {
                if (value == 64) return "--";
                else if (value == 0) return "< 63";
                else if (value < 64) return "< " + (64 - value);
                else return  (value - 64) + " >";
                }
            };
        hbox.add(comp);
                
        comp = new LabelledDial("Hi", this, prefix + "part" + part + "hicutoff", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Cutoff");
        hbox.add(comp);

        comp = new LabelledDial(" Amp Env ", this, prefix + "part" + part + "ampegattack", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Attack");
        hbox.add(comp);
                
        comp = new LabelledDial(" Amp Env ", this, prefix + "part" + part + "ampegdecay", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Decay");
        hbox.add(comp);
                
        // AR
        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, prefix + "part" + part + "ampegattack",   prefix + "part" + part + "ampegdecay" },
            new String[] { null, null,             null                 },
            new double[] { 0,    0.5/127,          0.5/127              },
            new double[] { 0.0,  1.0,              0.0                  });
        hbox.add(comp);
        
        vbox.add(hbox);
        hbox = new HBox();

        if (part == 1)
            {
            hbox = new HBox();

            comp = new LabelledDial("Sample", this, prefix + "part" + part + "startpoint", color, 0, 127);
            ((LabelledDial)comp).addAdditionalLabel("Start");
            hbox.add(comp);
                
            comp = new LabelledDial("Sample", this, prefix + "part" + part + "length", color, 0, 127);
            ((LabelledDial)comp).addAdditionalLabel("Length");
            hbox.add(comp);

            comp = new LabelledDial("Speed", this, prefix + "part" + part + "speed", color, 0, 127)
                {
                public boolean isSymmetric()
                    {
                    return true;
                    }
                
                public String map(int value)
                    {
                    if (value == 0) return "-63";
                    else return "" + (value - 64);
                    }
                };
            hbox.add(comp);

            comp = new LabelledDial("Pitch Env", this, prefix + "part" + part + "pitchegintensity", color, 0, 127)
                {
                public boolean isSymmetric() { return true; }
                public String map(int value)
                    {
                    if (value == 0) return "-63";
                    else return "" + (value - 64);
                    }
                };
            ((LabelledDial)comp).addAdditionalLabel("Intensity");
            hbox.add(comp);
                
            comp = new LabelledDial("Pitch Env", this, prefix + "part" + part + "pitchegattack", color, 0, 127);
            ((LabelledDial)comp).addAdditionalLabel("Attack");
            hbox.add(comp);
                
            comp = new LabelledDial("Pitch Env", this, prefix + "part" + part + "pitchegdecay", color, 0, 127);
            ((LabelledDial)comp).addAdditionalLabel("Decay");
            hbox.add(comp);
                
            // AR
            comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
                new String[] { null, prefix + "part" + part + "pitchegattack",   prefix + "part" + part + "pitchegdecay" },
                new String[] { null, null,             null                 },
                new double[] { 0,    0.5/127,          0.5/127              },
                new double[] { 0.0,  1.0,              0.0                  });
            hbox.add(comp);
            
            vbox.add(hbox);
            }
        
        category.add(vbox);
                                                                
        return category;
        }

    public JComponent addVolcaSample2(Color color)
        {
        Category category = new Category(this, "Sample2 [Single Channel]", color);
                
        JComponent comp;
        String[] params;
               
        final JComponent typical = addVolcaSample2Part(1, color);
        final int h = typical.getPreferredSize().height;
        final int w = typical.getPreferredSize().width;
        

        VBox vbox = new VBox()
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
                size.height = h * 2;
                return size;
                }
            };
                
        for(int part = 1; part <= 10; part++)
            {
            vbox.add(addVolcaSample2Part(part, (part % 2 == 0 ? Style.COLOR_B() : Style.COLOR_A())));
            }
                
        JScrollPane pane = new JScrollPane(vbox, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        pane.getViewport().setBackground(Style.BACKGROUND_COLOR());
        pane.setBorder(null);
                                                
        category.add(pane, BorderLayout.CENTER);
        return category;
        }        
        

    final static int[][] CC = new int[][]
    {
    // BASS
        { 
        5, 11, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49 
        }, 
    // BEATS
        { 
        40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59 
        }, 
    // DRUM (SINGLE)
        { 
        14, 15, 16, 17, 18, 19, 20, 103, 109, 23, 24, 25, 26, 27, 28, 29, 104, 110, 46, 47, 48, 49, 50, 51, 52, 105, 111, 55, 56, 57, 58, 59, 60, 61, 106, 112, 80, 81, 82, 83, 84, 85, 86, 107, 113, 89, 90, 96, 97, 98, 99, 100, 108, 114, 116, 117, 118, 119 
        }, 
    // DRUM (SPLIT)
        { 
        // Note that this is PER MIDI CHANNEL 1-6
        10, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 46, 47, 48, 49, 50, 51, 52, 103, 
        // Globals
        116, 117, 118, 119 
        }, 
    // FM
        { 
        40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50,
        // PAJEN FIRMWARE
        85, 86, 87, 88, 89, 90, 91 
        }, 
    // KEYS
        { 
        5, 11, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53 
        }, 
    // KICK
        { 
        40, 41, 42, 43, 44, 45, 46, 47, 48, 49 
        }, 
    // NUBASS
        { 
        40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50 
        }, 
    // SAMPLE
        { 
        // Note that this is PER MIDI CHANNEL 1-10
        7, 10, 40, 41, 42, 43, 44, 45, 46, 47, 48, 
        // Pajen (in the user guide order)
        50, 54, 56, 58, 59, 55, 52, 51, 57              // note no 53                   // THIS IS NOT GOING TO BE THE FINAL VERSION
        }, 
    // SAMPLE PAJEN-7 FIRMWARE CHANNEL 11
        { 
        0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100,
        1, 11, 21, 31, 41, 51, 61, 71, 81, 91, 101,
        2, 12, 22, 32, 42, 52, 62, 72, 82, 92, 102,
        3, 13, 23, 33, 43, 53, 63, 73, 83, 93, 103,
        4, 14, 24, 34, 44, 54, 64, 74, 84, 94, 104,
        5, 15, 25, 35, 45, 55, 65, 75, 85, 95, 105,
        6, 16, 26, 36, 46, 56, 66, 76, 86, 96, 106,
        7, 17, 27, 37, 47, 57, 67, 77, 87, 97, 107,
        8, 18, 28, 38, 48, 58, 68, 78, 88, 98, 108,
        9, 19, 29, 39, 49, 59, 69, 79, 89, 99, 109,
        }, 
    // SAMPLE2
        { 
        // According to https://www.reddit.com/r/volcas/comments/l9mp26/volca_sample_v2_midi_cc_not_really_working/
        // "I don't understand why korg doesn't release the official midi implementation chart 
        // for sample2. Anyways, here are the mappings for parts other than part1:"
        // attrib: [level, pan, ampAttack, ampDelay, hicut]
        // CC range: [14 - 19] - Part2 [20 - 25] - Part3 [26 - 31] - Part4 [50 - 55] - 
        // Part5 [56 - 61] - Part6 [76 - 81] - Part7 [82 - 87] - Part8 [102 - 107] - 
        // Part9 [108 - 113] - Part10
        // -- Sean: I'm guessing Part 1 is the standard CCs for the original Sample
        // -- Sean: maybe there's a bug? The CC ranges are 6 long but there are only 5 attributes.
        //    Did he forget one?

        // Note that these aren't in increasing order so as to keep the parts together
        7, 10, 40, 41, 42, 43, 44, 45, 46, 47, 48, 14, 15, 16, 17, 18, 20, 21, 22, 23, 24, 26, 27, 28, 29, 30, 50, 51, 52, 53, 54, 56, 57, 58, 59, 60, 76, 77, 78, 79, 80, 82, 83, 84, 85, 86, 102, 103, 104, 105, 106, 108, 109, 110, 112, 113  
        }, 
    };

    static HashMap allParametersToCC = null;

    final static String[][] allParameters = new String[][]
    {
        {
        "bassslidetime",
        "bassexpression",
        "bassoctave",
        "basslforate",
        "basslfointensity",
        "bassvco1pitch",
        "bassvco2pitch",
        "bassvco3pitch",
        "bassegattack",
        "bassegdecayrelease",
        "basscutoffegintensity",
        "bassgatetime",
        },
        {
        "beatskicklevel",
        "beatssnarelevel",
        "beatslotomlevel",
        "beatshitomlevel",
        "beatsclosedhatlevel",
        "beatsopenhatlevel",
        "beatsclaplevel",
        "beatsclaveslevel",
        "beatsagogolevel",
        "beatscrashlevel",
        "beatsclappcmspeed",
        "beatsclavespcmspeed",
        "beatsagogopcmspeed",
        "beatscrashpcmspeed",
        "beatsstuttertime",
        "beatsstutterdepth",
        "beatstomdecay",
        "beatsclosedhatdecay",
        "beatsopenhatdecay",
        "beatshatgrain",
        },
        {
        "drumsinglepart1select",
        "drumsinglepart1level",
        "drumsinglepart1modamt",
        "drumsinglepart1modrate",
        "drumsinglepart1pitch",
        "drumsinglepart1attack",
        "drumsinglepart1release",
        "drumsinglepart1send",
        "drumsinglepart1pan",
        "drumsinglepart2select",
        "drumsinglepart2level",
        "drumsinglepart2modamt",
        "drumsinglepart2modrate",
        "drumsinglepart2pitch",
        "drumsinglepart2attack",
        "drumsinglepart2release",
        "drumsinglepart2send",
        "drumsinglepart2pan",
        "drumsinglepart3select",
        "drumsinglepart3level",
        "drumsinglepart3modamt",
        "drumsinglepart3modrate",
        "drumsinglepart3pitch",
        "drumsinglepart3attack",
        "drumsinglepart3release",
        "drumsinglepart3send",
        "drumsinglepart3pan",
        "drumsinglepart4select",
        "drumsinglepart4level",
        "drumsinglepart4modamt",
        "drumsinglepart4modrate",
        "drumsinglepart4pitch",
        "drumsinglepart4attack",
        "drumsinglepart4release",
        "drumsinglepart4send",
        "drumsinglepart4pan",
        "drumsinglepart5select",
        "drumsinglepart5level",
        "drumsinglepart5modamt",
        "drumsinglepart5modrate",
        "drumsinglepart5pitch",
        "drumsinglepart5attack",
        "drumsinglepart5release",
        "drumsinglepart5send",
        "drumsinglepart5pan",
        "drumsinglepart6select",
        "drumsinglepart6level",
        "drumsinglepart6modamt",
        "drumsinglepart6modrate",
        "drumsinglepart6pitch",
        "drumsinglepart6attack",
        "drumsinglepart6release",
        "drumsinglepart6send",
        "drumsinglepart6pan",
        "drumsinglewaveguidemodel",
        "drumsingledecay",
        "drumsinglebody",
        "drumsingletune",
        },
        {
        "drumsplitpart1pan",
        "drumsplitpart1layer1select",
        "drumsplitpart1layer2select",
        "drumsplitpart1layer3select",
        "drumsplitpart1layer1level",
        "drumsplitpart1layer2level",
        "drumsplitpart1layer3level",
        "drumsplitpart1layer1attack",
        "drumsplitpart1layer2attack",
        "drumsplitpart1layer3attack",
        "drumsplitpart1layer1release",
        "drumsplitpart1layer2release",
        "drumsplitpart1layer3release",
        "drumsplitpart1layer1pitch",
        "drumsplitpart1layer2pitch",
        "drumsplitpart1layer3pitch",
        "drumsplitpart1layer1modamt",
        "drumsplitpart1layer2modamt",
        "drumsplitpart1layer3modamt",
        "drumsplitpart1layer1modrate",
        "drumsplitpart1layer2modrate",
        "drumsplitpart1layer3modrate",
        "drumsplitpart1bitreduction",
        "drumsplitpart1fold",
        "drumsplitpart1drive",
        "drumsplitpart1drygain",
        "drumsplitpart1send",
        "drumsplitpart2pan",
        "drumsplitpart2layer1select",
        "drumsplitpart2layer2select",
        "drumsplitpart2layer3select",
        "drumsplitpart2layer1level",
        "drumsplitpart2layer2level",
        "drumsplitpart2layer3level",
        "drumsplitpart2layer1attack",
        "drumsplitpart2layer2attack",
        "drumsplitpart2layer3attack",
        "drumsplitpart2layer1release",
        "drumsplitpart2layer2release",
        "drumsplitpart2layer3release",
        "drumsplitpart2layer1pitch",
        "drumsplitpart2layer2pitch",
        "drumsplitpart2layer3pitch",
        "drumsplitpart2layer1modamt",
        "drumsplitpart2layer2modamt",
        "drumsplitpart2layer3modamt",
        "drumsplitpart2layer1modrate",
        "drumsplitpart2layer2modrate",
        "drumsplitpart2layer3modrate",
        "drumsplitpart2bitreduction",
        "drumsplitpart2fold",
        "drumsplitpart2drive",
        "drumsplitpart2drygain",
        "drumsplitpart2send",
        "drumsplitpart3pan",
        "drumsplitpart3layer1select",
        "drumsplitpart3layer2select",
        "drumsplitpart3layer3select",
        "drumsplitpart3layer1level",
        "drumsplitpart3layer2level",
        "drumsplitpart3layer3level",
        "drumsplitpart3layer1attack",
        "drumsplitpart3layer2attack",
        "drumsplitpart3layer3attack",
        "drumsplitpart3layer1release",
        "drumsplitpart3layer2release",
        "drumsplitpart3layer3release",
        "drumsplitpart3layer1pitch",
        "drumsplitpart3layer2pitch",
        "drumsplitpart3layer3pitch",
        "drumsplitpart3layer1modamt",
        "drumsplitpart3layer2modamt",
        "drumsplitpart3layer3modamt",
        "drumsplitpart3layer1modrate",
        "drumsplitpart3layer2modrate",
        "drumsplitpart3layer3modrate",
        "drumsplitpart3bitreduction",
        "drumsplitpart3fold",
        "drumsplitpart3drive",
        "drumsplitpart3drygain",
        "drumsplitpart3send",
        "drumsplitpart4pan",
        "drumsplitpart4layer1select",
        "drumsplitpart4layer2select",
        "drumsplitpart4layer3select",
        "drumsplitpart4layer1level",
        "drumsplitpart4layer2level",
        "drumsplitpart4layer3level",
        "drumsplitpart4layer1attack",
        "drumsplitpart4layer2attack",
        "drumsplitpart4layer3attack",
        "drumsplitpart4layer1release",
        "drumsplitpart4layer2release",
        "drumsplitpart4layer3release",
        "drumsplitpart4layer1pitch",
        "drumsplitpart4layer2pitch",
        "drumsplitpart4layer3pitch",
        "drumsplitpart4layer1modamt",
        "drumsplitpart4layer2modamt",
        "drumsplitpart4layer3modamt",
        "drumsplitpart4layer1modrate",
        "drumsplitpart4layer2modrate",
        "drumsplitpart4layer3modrate",
        "drumsplitpart4bitreduction",
        "drumsplitpart4fold",
        "drumsplitpart4drive",
        "drumsplitpart4drygain",
        "drumsplitpart4send",
        "drumsplitpart5pan",
        "drumsplitpart5layer1select",
        "drumsplitpart5layer2select",
        "drumsplitpart5layer3select",
        "drumsplitpart5layer1level",
        "drumsplitpart5layer2level",
        "drumsplitpart5layer3level",
        "drumsplitpart5layer1attack",
        "drumsplitpart5layer2attack",
        "drumsplitpart5layer3attack",
        "drumsplitpart5layer1release",
        "drumsplitpart5layer2release",
        "drumsplitpart5layer3release",
        "drumsplitpart5layer1pitch",
        "drumsplitpart5layer2pitch",
        "drumsplitpart5layer3pitch",
        "drumsplitpart5layer1modamt",
        "drumsplitpart5layer2modamt",
        "drumsplitpart5layer3modamt",
        "drumsplitpart5layer1modrate",
        "drumsplitpart5layer2modrate",
        "drumsplitpart5layer3modrate",
        "drumsplitpart5bitreduction",
        "drumsplitpart5fold",
        "drumsplitpart5drive",
        "drumsplitpart5drygain",
        "drumsplitpart5send",
        "drumsplitpart6pan",
        "drumsplitpart6layer1select",
        "drumsplitpart6layer2select",
        "drumsplitpart6layer3select",
        "drumsplitpart6layer1level",
        "drumsplitpart6layer2level",
        "drumsplitpart6layer3level",
        "drumsplitpart6layer1attack",
        "drumsplitpart6layer2attack",
        "drumsplitpart6layer3attack",
        "drumsplitpart6layer1release",
        "drumsplitpart6layer2release",
        "drumsplitpart6layer3release",
        "drumsplitpart6layer1pitch",
        "drumsplitpart6layer2pitch",
        "drumsplitpart6layer3pitch",
        "drumsplitpart6layer1modamt",
        "drumsplitpart6layer2modamt",
        "drumsplitpart6layer3modamt",
        "drumsplitpart6layer1modrate",
        "drumsplitpart6layer2modrate",
        "drumsplitpart6layer3modrate",
        "drumsplitpart6bitreduction",
        "drumsplitpart6fold",
        "drumsplitpart6drive",
        "drumsplitpart6drygain",
        "drumsplitpart6send",
        "drumsplitwaveguidemodel",
        "drumsplitdecay",
        "drumsplitbody",
        "drumsplittune",
        },
        {
        "fmtranspose",
        "fmevelocity",
        "fmmodulatorattack",
        "fmmodulatordecay",
        "fmcarrierattack",
        "fmcarrierdecay",
        "fmlforate",
        "fmlfopitchdepth",
        "fmalgorithm",
        "fmarpeggiatortype",
        "fmarpeggiatordiv",
        "fmpajenchorus",
        "fmpajenchorusstereowidth",
        "fmpajenchorusspeed",
        "fmpajenloadpatch",
        "fmpajenloadpattern",
        "fmpajentempodivisor",
        "fmpajenmodwheelcc",
        },
        {
        "keysportamento",
        "keysexpression",
        "keysvoice",
        "keysoctave",
        "keysdetune",
        "keysenvelopeintensity",
        "keysvcfcutoff",
        "keysvcfenvelopeintensity",
        "keyslforate",
        "keyslfopitchintensity",
        "keyslfocutoffintensity",
        "keysenvelopeattack",
        "keysenvelopedecayrelease",
        "keysenvelopesustain",
        "keysdelaytime",
        "keysdelayfeedback",
        },
        {
        "kickpulsecolor",
        "kickpulselevel",
        "kickampattack",
        "kickampdecay",
        "kickdrive",
        "kicktone",
        "kickresonatorpitch",
        "kickresonatorbend",
        "kickresonatortime",
        "kickaccent",
        },
        {
        "nubassvtopitch",
        "nubassvtosaturation",
        "nubassvtolevel",
        "nubassvcfcutoff",
        "nubassvcfpeak",
        "nubassvcfattack",
        "nubassvcfdecay",
        "nubassvcfegintensity",
        "nubassaccent",
        "nubasslforate",
        "nubasslfointensity",
        },
        {
        "sample1part1level",
        "sample1part1pan",
        "sample1part1startpoint",
        "sample1part1length",
        "sample1part1hicutoff",
        "sample1part1speed",
        "sample1part1pitchegintensity",
        "sample1part1pitchegattack",
        "sample1part1pitchegdecay",
        "sample1part1ampegattack",
        "sample1part1ampegdecay",
        "sample1part1pajensampleselect",
        "sample1part1pajenpartselect",
        "sample1part1pajenreversepart",
        "sample1part1pajenmutepart",
        "sample1part1pajensolopart",
        "sample1part1pajenloop",
        "sample1part1pajenreverbtype",
        "sample1part1pajenreverblevel",
        "sample1part1pajenreverbonoff",
        "sample1part2level",
        "sample1part2pan",
        "sample1part2startpoint",
        "sample1part2length",
        "sample1part2hicutoff",
        "sample1part2speed",
        "sample1part2pitchegintensity",
        "sample1part2pitchegattack",
        "sample1part2pitchegdecay",
        "sample1part2ampegattack",
        "sample1part2ampegdecay",
        "sample1part2pajensampleselect",
        "sample1part2pajenpartselect",
        "sample1part2pajenreversepart",
        "sample1part2pajenmutepart",
        "sample1part2pajensolopart",
        "sample1part2pajenloop",
        "sample1part2pajenreverbtype",
        "sample1part2pajenreverblevel",
        "sample1part2pajenreverbonoff",
        "sample1part3level",
        "sample1part3pan",
        "sample1part3startpoint",
        "sample1part3length",
        "sample1part3hicutoff",
        "sample1part3speed",
        "sample1part3pitchegintensity",
        "sample1part3pitchegattack",
        "sample1part3pitchegdecay",
        "sample1part3ampegattack",
        "sample1part3ampegdecay",
        "sample1part3pajensampleselect",
        "sample1part3pajenpartselect",
        "sample1part3pajenreversepart",
        "sample1part3pajenmutepart",
        "sample1part3pajensolopart",
        "sample1part3pajenloop",
        "sample1part3pajenreverbtype",
        "sample1part3pajenreverblevel",
        "sample1part3pajenreverbonoff",
        "sample1part4level",
        "sample1part4pan",
        "sample1part4startpoint",
        "sample1part4length",
        "sample1part4hicutoff",
        "sample1part4speed",
        "sample1part4pitchegintensity",
        "sample1part4pitchegattack",
        "sample1part4pitchegdecay",
        "sample1part4ampegattack",
        "sample1part4ampegdecay",
        "sample1part4pajensampleselect",
        "sample1part4pajenpartselect",
        "sample1part4pajenreversepart",
        "sample1part4pajenmutepart",
        "sample1part4pajensolopart",
        "sample1part4pajenloop",
        "sample1part4pajenreverbtype",
        "sample1part4pajenreverblevel",
        "sample1part4pajenreverbonoff",
        "sample1part5level",
        "sample1part5pan",
        "sample1part5startpoint",
        "sample1part5length",
        "sample1part5hicutoff",
        "sample1part5speed",
        "sample1part5pitchegintensity",
        "sample1part5pitchegattack",
        "sample1part5pitchegdecay",
        "sample1part5ampegattack",
        "sample1part5ampegdecay",
        "sample1part5pajensampleselect",
        "sample1part5pajenpartselect",
        "sample1part5pajenreversepart",
        "sample1part5pajenmutepart",
        "sample1part5pajensolopart",
        "sample1part5pajenloop",
        "sample1part5pajenreverbtype",
        "sample1part5pajenreverblevel",
        "sample1part5pajenreverbonoff",
        "sample1part6level",
        "sample1part6pan",
        "sample1part6startpoint",
        "sample1part6length",
        "sample1part6hicutoff",
        "sample1part6speed",
        "sample1part6pitchegintensity",
        "sample1part6pitchegattack",
        "sample1part6pitchegdecay",
        "sample1part6ampegattack",
        "sample1part6ampegdecay",
        "sample1part6pajensampleselect",
        "sample1part6pajenpartselect",
        "sample1part6pajenreversepart",
        "sample1part6pajenmutepart",
        "sample1part6pajensolopart",
        "sample1part6pajenloop",
        "sample1part6pajenreverbtype",
        "sample1part6pajenreverblevel",
        "sample1part6pajenreverbonoff",
        "sample1part7level",
        "sample1part7pan",
        "sample1part7startpoint",
        "sample1part7length",
        "sample1part7hicutoff",
        "sample1part7speed",
        "sample1part7pitchegintensity",
        "sample1part7pitchegattack",
        "sample1part7pitchegdecay",
        "sample1part7ampegattack",
        "sample1part7ampegdecay",
        "sample1part7pajensampleselect",
        "sample1part7pajenpartselect",
        "sample1part7pajenreversepart",
        "sample1part7pajenmutepart",
        "sample1part7pajensolopart",
        "sample1part7pajenloop",
        "sample1part7pajenreverbtype",
        "sample1part7pajenreverblevel",
        "sample1part7pajenreverbonoff",
        "sample1part8level",
        "sample1part8pan",
        "sample1part8startpoint",
        "sample1part8length",
        "sample1part8hicutoff",
        "sample1part8speed",
        "sample1part8pitchegintensity",
        "sample1part8pitchegattack",
        "sample1part8pitchegdecay",
        "sample1part8ampegattack",
        "sample1part8ampegdecay",
        "sample1part8pajensampleselect",
        "sample1part8pajenpartselect",
        "sample1part8pajenreversepart",
        "sample1part8pajenmutepart",
        "sample1part8pajensolopart",
        "sample1part8pajenloop",
        "sample1part8pajenreverbtype",
        "sample1part8pajenreverblevel",
        "sample1part8pajenreverbonoff",
        "sample1part9level",
        "sample1part9pan",
        "sample1part9startpoint",
        "sample1part9length",
        "sample1part9hicutoff",
        "sample1part9speed",
        "sample1part9pitchegintensity",
        "sample1part9pitchegattack",
        "sample1part9pitchegdecay",
        "sample1part9ampegattack",
        "sample1part9ampegdecay",
        "sample1part9pajensampleselect",
        "sample1part9pajenpartselect",
        "sample1part9pajenreversepart",
        "sample1part9pajenmutepart",
        "sample1part9pajensolopart",
        "sample1part9pajenloop",
        "sample1part9pajenreverbtype",
        "sample1part9pajenreverblevel",
        "sample1part9pajenreverbonoff",
        "sample1part10level",
        "sample1part10pan",
        "sample1part10startpoint",
        "sample1part10length",
        "sample1part10hicutoff",
        "sample1part10speed",
        "sample1part10pitchegintensity",
        "sample1part10pitchegattack",
        "sample1part10pitchegdecay",
        "sample1part10ampegattack",
        "sample1part10ampegdecay",
        "sample1part10pajensampleselect",
        "sample1part10pajenpartselect",
        "sample1part10pajenreversepart",
        "sample1part10pajenmutepart",
        "sample1part10pajensolopart",
        "sample1part10pajenloop",
        "sample1part10pajenreverbtype",
        "sample1part10pajenreverblevel",
        "sample1part10pajenreverbonoff",
        },
        {
        "sample2part1level",
        "sample2part1pan",
        "sample2part1startpoint",
        "sample2part1length",
        "sample2part1hicutoff",
        "sample2part1speed",
        "sample2part1pitchegintensity",
        "sample2part1pitchegattack",
        "sample2part1pitchegdecay",
        "sample2part1ampegattack",
        "sample2part1ampegdecay",
        "sample2part2level",
        "sample2part2pan",
        "sample2part2ampegattack",
        "sample2part2ampegdecay",
        "sample2part2hicutoff",
        "sample2part3level",
        "sample2part3pan",
        "sample2part3hicutoff",
        "sample2part3ampegattack",
        "sample2part3ampegdecay",
        "sample2part4level",
        "sample2part4pan",
        "sample2part4ampegattack",
        "sample2part4ampegdecay",
        "sample2part4hicutoff",
        "sample2part5level",
        "sample2part5pan",
        "sample2part5ampegattack",
        "sample2part5ampegdecay",
        "sample2part5hicutoff",
        "sample2part6level",
        "sample2part6pan",
        "sample2part6hicutoff",
        "sample2part6ampegattack",
        "sample2part6ampegdecay",
        "sample2part7level",
        "sample2part7pan",
        "sample2part7ampegattack",
        "sample2part7ampegdecay",
        "sample2part7hicutoff",
        "sample2part8level",
        "sample2part8pan",
        "sample2part8ampegattack",
        "sample2part8ampegdecay",
        "sample2part8hicutoff",
        "sample2part9level",
        "sample2part9pan",
        "sample2part9ampegattack",
        "sample2part9ampegdecay",
        "sample2part9hicutoff",
        "sample2part10level",
        "sample2part10pan",
        "sample2part10ampegattack",
        "sample2part10ampegdecay",
        "sample2part10hicutoff",
        }
    };

    public boolean getSendsAllParametersAsDump() { return false; }

    public Object[] emitAll(String key)
        {        
        if (key.equals("name")) return new Object[0];  // this is not emittable
        
        // Which panel is being displayed?
        String panel = PREFIXES[getSynthType()];
        if (key.startsWith(panel))
            {
            // doit
            if (key.startsWith("sample1"))
                {
                int num = StringUtility.getSecondInt(key);
                return buildCC(num - 1, ((Integer)allParametersToCC.get(key)).intValue(), model.get(key, 0));
                }
            else if (key.startsWith("drumsplit") &&
                !key.equals("drumsplitwaveguidemodel") &&
                !key.equals("drumsplitdecay") &&
                !key.equals("drumsplitbody") &&
                !key.equals("drumsplittune"))
                {
                int num = StringUtility.getFirstInt(key);               // part
                return buildCC(num - 1, ((Integer)allParametersToCC.get(key)).intValue(), model.get(key, 0));
                }
            else
                {
                return buildCC(getChannelOut(), ((Integer)allParametersToCC.get(key)).intValue(), model.get(key, 0));
                }
            }
        else return new Object[0];
        }



    public int parse(byte[] data, boolean fromFile)
        {
        // grab name
        char[] name = new char[16];
        for(int i = 0; i < 16; i++)
            {
            name[i] = (char)(data[i + 20] & 127);
            if (name[i] >= 127 || name[i] < ' ')
                name[i] = ' ';
            }
        model.set("name", String.valueOf(name));
                
        // determine type               
        int type = data[20 + 16];

        int pos = 21 + 16;
        if (type == 0)                  // "All Volcas"
            {
            for(int j = 0; j < allParameters.length; j++)
                {
                for(int i = 0; i < allParameters[j].length; i++)
                    {
                    model.set(allParameters[j][i], data[pos++] & 127);
                    }
                }
                                
            }
        else
            {
            for(int i = 0; i < allParameters[type - 1].length; i++)
                {
                model.set(allParameters[type - 1][i], data[pos++] & 127);
                }
            }
            
        if (type > 0)
            {
            setSynthType(type - 1, true);
            }
                
        return PARSE_SUCCEEDED;
        }


             
    public static final int INIT_PATCH_DATA_LENGTH = 567;
       
    public byte[] emit(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        if (tempModel == null)
            tempModel = getModel();
        
        final boolean initPatch = false;
        int type = getSynthType();

        byte[] data = new byte[(initPatch ? INIT_PATCH_DATA_LENGTH : allParameters[type].length) + 38];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x7D;
        data[2] = (byte)'E';
        data[3] = (byte)'D';
        data[4] = (byte)'I';
        data[5] = (byte)'S';
        data[6] = (byte)'Y';
        data[7] = (byte)'N';
        data[8] = (byte)' ';
        data[9] = (byte)'K';
        data[10] = (byte)'O';
        data[11] = (byte)'R';
        data[12] = (byte)'G';
        data[13] = (byte)' ';
        data[14] = (byte)'V';
        data[15] = (byte)'O';
        data[16] = (byte)'L';
        data[17] = (byte)'C';
        data[18] = (byte)'A';

        // Load version
        data[19] = (byte) 0;
        
        // Set end
        data[data.length - 1] = (byte)0xF7;
                
        // Load name
        char[] name = (model.get("name", "") + "                ").substring(0, 16).toCharArray();
        for(int i = 0; i < name.length; i++)
            {
            if (name[i] < ' ' || name[i] >= 127)
                name[i] = ' ';
            data[i + 20] = (byte)(name[i]);
            }
            
        if (initPatch)
            {
            // This code is used to generate the init patch
            data[20 + 16] = (byte) 0;
            int pos = 21 + 16;
            for(int j = 0; j < allParameters.length; j++)
                {
                for(int i = 0; i < allParameters[j].length; i++)
                    {
                    data[pos++] = (byte)(model.get(allParameters[j][i], 0));
                    }
                }
            }
        else
            {
            // This is the standard data output load type
            data[20 + 16] = (byte) (type + 1);
                                
            for(int i = 0; i < allParameters[type].length; i++)
                {
                data[21 + 16 + i] = (byte)(model.get(allParameters[type][i], 0));
                }
            }
              
        return data;
        }

    public static final int MAXIMUM_NAME_LENGTH = 16;
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

    public void revise()
        {
        // check the easy stuff -- out of range parameters
        super.revise();

        String nm = model.get("name", "Untitled");
        String newnm = revisePatchName(nm);
        if (!nm.equals(newnm))
            model.set("name", newnm);
        }
        

    public String getPatchName(Model model) { return model.get("name", "Untitled"); }
        
    public boolean testVerify(Synth synth2, String key, Object val1, Object val2)
        {
        if (key.equals("name"))
            { 
            return ((String)val1).trim() != ((String)val2).trim();
            }               
        // we ignore keys that don't start with the prefix we're interested in
        else if (!key.startsWith(PREFIXES[getSynthType()]))
            return true;
        return false;
        }

    }










/***

    EDISYN'S TOTALLY MADE UP KORG VOLCA SYSEX SPECIFICATION FOR PURPOSES OF FILE STORAGE

    The data in this specification can vary in length depending on the Volca unit

    0xF0
    0x7D
    17 bytes comprising the ASCII string "EDISYN KORG VOLCA" (not including quotes)
    Version number  (presently 0x0)
    16 bytes comprising an ASCII string of the patch name (internal to Edisyn), DEL and control characters not permitted except space
    Volca type [see Table 1]
    Data [variable length, depends on Volca type.  Volca type 0 is the data of all Volcas concatenated in order, 677 bytes]
    0xF7



    All data are 1 byte each, defined as a value from 0-127

    DATA for Volca Bass [12 bytes]
    bassslidetime
    bassexpression
    bassoctave                                      ** See Table 2
    basslforate
    basslfointensity
    bassvco1pitch
    bassvco2pitch
    bassvco3pitch
    bassegattack
    bassegdecayrelease
    basscutoffegintensity
    bassgatetime


    DATA for Volca Beats [20 bytes]
    beatskicklevel
    beatssnarelevel
    beatslotomlevel
    beatshitomlevel
    beatsclosedhatlevel
    beatsopenhatlevel
    beatsclaplevel
    beatsclaveslevel
    beatsagogolevel
    beatscrashlevel
    beatsclappcmspeed
    beatsclavespcmspeed
    beatsagogopcmspeed
    beatscrashpcmspeed
    beatsstuttertime
    beatsstutterdepth
    beatstomdecay
    beatsclosedhatdecay
    beatsopenhatdecay
    beatshatgrain


    DATA for Volca Drum (Single) [58 bytes]
    drumsinglepart1select
    drumsinglepart1level
    drumsinglepart1modamt
    drumsinglepart1modrate
    drumsinglepart1pitch
    drumsinglepart1attack
    drumsinglepart1release
    drumsinglepart1send
    drumsinglepart1pan
    drumsinglepart2select
    drumsinglepart2level
    drumsinglepart2modamt
    drumsinglepart2modrate
    drumsinglepart2pitch
    drumsinglepart2attack
    drumsinglepart2release
    drumsinglepart2send
    drumsinglepart2pan
    drumsinglepart3select
    drumsinglepart3level
    drumsinglepart3modamt
    drumsinglepart3modrate
    drumsinglepart3pitch
    drumsinglepart3attack
    drumsinglepart3release
    drumsinglepart3send
    drumsinglepart3pan
    drumsinglepart4select
    drumsinglepart4level
    drumsinglepart4modamt
    drumsinglepart4modrate
    drumsinglepart4pitch
    drumsinglepart4attack
    drumsinglepart4release
    drumsinglepart4send
    drumsinglepart4pan
    drumsinglepart5select
    drumsinglepart5level
    drumsinglepart5modamt
    drumsinglepart5modrate
    drumsinglepart5pitch
    drumsinglepart5attack
    drumsinglepart5release
    drumsinglepart5send
    drumsinglepart5pan
    drumsinglepart6select
    drumsinglepart6level
    drumsinglepart6modamt
    drumsinglepart6modrate
    drumsinglepart6pitch
    drumsinglepart6attack
    drumsinglepart6release
    drumsinglepart6send
    drumsinglepart6pan
    drumsinglewaveguidemodel
    drumsingledecay
    drumsinglebody
    drumsingletune

    DATA for Volca Drum (Split) [166 bytes]          * note that "1-2" is called "3" internally in Edisyn for reasons that aren't worth explaining here
    drumsplitpart1pan
    drumsplitpart1layer1select
    drumsplitpart1layer2select
    drumsplitpart1layer3select
    drumsplitpart1layer1level
    drumsplitpart1layer2level
    drumsplitpart1layer3level
    drumsplitpart1layer1attack
    drumsplitpart1layer2attack
    drumsplitpart1layer3attack
    drumsplitpart1layer1release
    drumsplitpart1layer2release
    drumsplitpart1layer3release
    drumsplitpart1layer1pitch
    drumsplitpart1layer2pitch
    drumsplitpart1layer3pitch
    drumsplitpart1layer1modamt
    drumsplitpart1layer2modamt
    drumsplitpart1layer3modamt
    drumsplitpart1layer1modrate
    drumsplitpart1layer2modrate
    drumsplitpart1layer3modrate
    drumsplitpart1bitreduction
    drumsplitpart1fold
    drumsplitpart1drive
    drumsplitpart1drygain
    drumsplitpart1send
    drumsplitsend
    drumsplitwaveguidemodel
    drumsplitdecay
    drumsplitbody
    drumsplittune
    drumsplitpart2pan
    drumsplitpart2layer1select
    drumsplitpart2layer2select
    drumsplitpart2layer3select
    drumsplitpart2layer1level
    drumsplitpart2layer2level
    drumsplitpart2layer3level
    drumsplitpart2layer1attack
    drumsplitpart2layer2attack
    drumsplitpart2layer3attack
    drumsplitpart2layer1release
    drumsplitpart2layer2release
    drumsplitpart2layer3release
    drumsplitpart2layer1pitch
    drumsplitpart2layer2pitch
    drumsplitpart2layer3pitch
    drumsplitpart2layer1modamt
    drumsplitpart2layer2modamt
    drumsplitpart2layer3modamt
    drumsplitpart2layer1modrate
    drumsplitpart2layer2modrate
    drumsplitpart2layer3modrate
    drumsplitpart2bitreduction
    drumsplitpart2fold
    drumsplitpart2drive
    drumsplitpart2drygain
    drumsplitpart2send
    drumsplitpart3pan
    drumsplitpart3layer1select
    drumsplitpart3layer2select
    drumsplitpart3layer3select
    drumsplitpart3layer1level
    drumsplitpart3layer2level
    drumsplitpart3layer3level
    drumsplitpart3layer1attack
    drumsplitpart3layer2attack
    drumsplitpart3layer3attack
    drumsplitpart3layer1release
    drumsplitpart3layer2release
    drumsplitpart3layer3release
    drumsplitpart3layer1pitch
    drumsplitpart3layer2pitch
    drumsplitpart3layer3pitch
    drumsplitpart3layer1modamt
    drumsplitpart3layer2modamt
    drumsplitpart3layer3modamt
    drumsplitpart3layer1modrate
    drumsplitpart3layer2modrate
    drumsplitpart3layer3modrate
    drumsplitpart3bitreduction
    drumsplitpart3fold
    drumsplitpart3drive
    drumsplitpart3drygain
    drumsplitpart3send
    drumsplitpart4pan
    drumsplitpart4layer1select
    drumsplitpart4layer2select
    drumsplitpart4layer3select
    drumsplitpart4layer1level
    drumsplitpart4layer2level
    drumsplitpart4layer3level
    drumsplitpart4layer1attack
    drumsplitpart4layer2attack
    drumsplitpart4layer3attack
    drumsplitpart4layer1release
    drumsplitpart4layer2release
    drumsplitpart4layer3release
    drumsplitpart4layer1pitch
    drumsplitpart4layer2pitch
    drumsplitpart4layer3pitch
    drumsplitpart4layer1modamt
    drumsplitpart4layer2modamt
    drumsplitpart4layer3modamt
    drumsplitpart4layer1modrate
    drumsplitpart4layer2modrate
    drumsplitpart4layer3modrate
    drumsplitpart4bitreduction
    drumsplitpart4fold
    drumsplitpart4drive
    drumsplitpart4drygain
    drumsplitpart4send
    drumsplitpart5pan
    drumsplitpart5layer1select
    drumsplitpart5layer2select
    drumsplitpart5layer3select
    drumsplitpart5layer1level
    drumsplitpart5layer2level
    drumsplitpart5layer3level
    drumsplitpart5layer1attack
    drumsplitpart5layer2attack
    drumsplitpart5layer3attack
    drumsplitpart5layer1release
    drumsplitpart5layer2release
    drumsplitpart5layer3release
    drumsplitpart5layer1pitch
    drumsplitpart5layer2pitch
    drumsplitpart5layer3pitch
    drumsplitpart5layer1modamt
    drumsplitpart5layer2modamt
    drumsplitpart5layer3modamt
    drumsplitpart5layer1modrate
    drumsplitpart5layer2modrate
    drumsplitpart5layer3modrate
    drumsplitpart5bitreduction
    drumsplitpart5fold
    drumsplitpart5drive
    drumsplitpart5drygain
    drumsplitpart5send
    drumsplitpart6pan
    drumsplitpart6layer1select
    drumsplitpart6layer2select
    drumsplitpart6layer3select
    drumsplitpart6layer1level
    drumsplitpart6layer2level
    drumsplitpart6layer3level
    drumsplitpart6layer1attack
    drumsplitpart6layer2attack
    drumsplitpart6layer3attack
    drumsplitpart6layer1release
    drumsplitpart6layer2release
    drumsplitpart6layer3release
    drumsplitpart6layer1pitch
    drumsplitpart6layer2pitch
    drumsplitpart6layer3pitch
    drumsplitpart6layer1modamt
    drumsplitpart6layer2modamt
    drumsplitpart6layer3modamt
    drumsplitpart6layer1modrate
    drumsplitpart6layer2modrate
    drumsplitpart6layer3modrate
    drumsplitpart6bitreduction
    drumsplitpart6fold
    drumsplitpart6drive
    drumsplitpart6drygain
    drumsplitpart6send


    DATA for Volca FM [18 bytes]
    fmtranspose
    fmevelocity
    fmmodulatorattack
    fmmodulatordecay
    fmcarrierattack
    fmcarrierdecay
    fmlforate
    fmlfopitchdepth
    fmalgorithm
    fmarpeggiatortype
    fmarpeggiatordiv
    fmpajenchorus
    fmpajenchorusstereowidth
    fmpajenchorusspeed
    fmpajenloadpatch
    fmpajenloadpattern
    fmpajentempodivisor
    fmpajenmodwheelcc


    DATA for Volca Keys [16 bytes]
    keysportamento
    keysexpression
    keysvoice
    keysoctave
    keysdetune
    keysenvelopeintensity
    keysvcfcutoff
    keysvcfenvelopeintensity
    keyslforate
    keyslfopitchintensity
    keyslfocutoffintensity
    keysenvelopeattack
    keysenvelopedecayrelease
    keysenvelopesustain
    keysdelaytime
    keysdelayfeedback


    DATA for Volca Kick [10 bytes]
    kickpulsecolor
    kickpulselevel
    kickampattack
    kickampdecay
    kickdrive
    kicktone
    kickresonatorpitch
    kickresonatorbend
    kickresonatortime
    kickaccent


    DATA for Volca NuBass [11 bytes]
    nubassvtopitch
    nubassvtosaturation
    nubassvtolevel
    nubassvcfcutoff
    nubassvcfpeak
    nubassvcfattack
    nubassvcfdecay
    nubassvcfegintensity
    nubassaccent
    nubasslforate
    nubasslfointensity


    DATA for Volca Sample [200 bytes]               
    * Note that the Sample is called the "Sample 1" internally in Edisyn for reasons that aren't worth explaining here
    * Note that pajen partselect, pajen reverblevel, and pajen reverbtype are actually globals though they work in
    *      different MIDI channels and so I have elected to keep them as separate parameters here for the time being
    sample1part1level
    sample1part1pan
    sample1part1startpoint
    sample1part1length
    sample1part1hicutoff
    sample1part1speed
    sample1part1pitchegintensity
    sample1part1pitchegattack
    sample1part1pitchegdecay
    sample1part1ampegattack
    sample1part1ampegdecay
    sample1part1pajensampleselect
    sample1part1pajenpartselect
    sample1part1pajenreversepart
    sample1part1pajenmutepart
    sample1part1pajensolopart
    sample1part1pajenloop
    sample1part1pajenreverbtype
    sample1part1pajenreverblevel
    sample1part1pajenreverbonoff
    sample1part2level
    sample1part2pan
    sample1part2startpoint
    sample1part2length
    sample1part2hicutoff
    sample1part2speed
    sample1part2pitchegintensity
    sample1part2pitchegattack
    sample1part2pitchegdecay
    sample1part2ampegattack
    sample1part2ampegdecay
    sample1part2pajensampleselect
    sample1part2pajenpartselect
    sample1part2pajenreversepart
    sample1part2pajenmutepart
    sample1part2pajensolopart
    sample1part2pajenloop
    sample1part2pajenreverbtype
    sample1part2pajenreverblevel
    sample1part2pajenreverbonoff
    sample1part3level
    sample1part3pan
    sample1part3startpoint
    sample1part3length
    sample1part3hicutoff
    sample1part3speed
    sample1part3pitchegintensity
    sample1part3pitchegattack
    sample1part3pitchegdecay
    sample1part3ampegattack
    sample1part3ampegdecay
    sample1part3pajensampleselect
    sample1part3pajenpartselect
    sample1part3pajenreversepart
    sample1part3pajenmutepart
    sample1part3pajensolopart
    sample1part3pajenloop
    sample1part3pajenreverbtype
    sample1part3pajenreverblevel
    sample1part3pajenreverbonoff
    sample1part4level
    sample1part4pan
    sample1part4startpoint
    sample1part4length
    sample1part4hicutoff
    sample1part4speed
    sample1part4pitchegintensity
    sample1part4pitchegattack
    sample1part4pitchegdecay
    sample1part4ampegattack
    sample1part4ampegdecay
    sample1part4pajensampleselect
    sample1part4pajenpartselect
    sample1part4pajenreversepart
    sample1part4pajenmutepart
    sample1part4pajensolopart
    sample1part4pajenloop
    sample1part4pajenreverbtype
    sample1part4pajenreverblevel
    sample1part4pajenreverbonoff
    sample1part5level
    sample1part5pan
    sample1part5startpoint
    sample1part5length
    sample1part5hicutoff
    sample1part5speed
    sample1part5pitchegintensity
    sample1part5pitchegattack
    sample1part5pitchegdecay
    sample1part5ampegattack
    sample1part5ampegdecay
    sample1part5pajensampleselect
    sample1part5pajenpartselect
    sample1part5pajenreversepart
    sample1part5pajenmutepart
    sample1part5pajensolopart
    sample1part5pajenloop
    sample1part5pajenreverbtype
    sample1part5pajenreverblevel
    sample1part5pajenreverbonoff
    sample1part6level
    sample1part6pan
    sample1part6startpoint
    sample1part6length
    sample1part6hicutoff
    sample1part6speed
    sample1part6pitchegintensity
    sample1part6pitchegattack
    sample1part6pitchegdecay
    sample1part6ampegattack
    sample1part6ampegdecay
    sample1part6pajensampleselect
    sample1part6pajenpartselect
    sample1part6pajenreversepart
    sample1part6pajenmutepart
    sample1part6pajensolopart
    sample1part6pajenloop
    sample1part6pajenreverbtype
    sample1part6pajenreverblevel
    sample1part6pajenreverbonoff
    sample1part7level
    sample1part7pan
    sample1part7startpoint
    sample1part7length
    sample1part7hicutoff
    sample1part7speed
    sample1part7pitchegintensity
    sample1part7pitchegattack
    sample1part7pitchegdecay
    sample1part7ampegattack
    sample1part7ampegdecay
    sample1part7pajensampleselect
    sample1part7pajenpartselect
    sample1part7pajenreversepart
    sample1part7pajenmutepart
    sample1part7pajensolopart
    sample1part7pajenloop
    sample1part7pajenreverbtype
    sample1part7pajenreverblevel
    sample1part7pajenreverbonoff
    sample1part8level
    sample1part8pan
    sample1part8startpoint
    sample1part8length
    sample1part8hicutoff
    sample1part8speed
    sample1part8pitchegintensity
    sample1part8pitchegattack
    sample1part8pitchegdecay
    sample1part8ampegattack
    sample1part8ampegdecay
    sample1part8pajensampleselect
    sample1part8pajenpartselect
    sample1part8pajenreversepart
    sample1part8pajenmutepart
    sample1part8pajensolopart
    sample1part8pajenloop
    sample1part8pajenreverbtype
    sample1part8pajenreverblevel
    sample1part8pajenreverbonoff
    sample1part9level
    sample1part9pan
    sample1part9startpoint
    sample1part9length
    sample1part9hicutoff
    sample1part9speed
    sample1part9pitchegintensity
    sample1part9pitchegattack
    sample1part9pitchegdecay
    sample1part9ampegattack
    sample1part9ampegdecay
    sample1part9pajensampleselect
    sample1part9pajenpartselect
    sample1part9pajenreversepart
    sample1part9pajenmutepart
    sample1part9pajensolopart
    sample1part9pajenloop
    sample1part9pajenreverbtype
    sample1part9pajenreverblevel
    sample1part9pajenreverbonoff
    sample1part10level
    sample1part10pan
    sample1part10startpoint
    sample1part10length
    sample1part10hicutoff
    sample1part10speed
    sample1part10pitchegintensity
    sample1part10pitchegattack
    sample1part10pitchegdecay
    sample1part10ampegattack
    sample1part10ampegdecay
    sample1part10pajensampleselect
    sample1part10pajenpartselect
    sample1part10pajenreversepart
    sample1part10pajenmutepart
    sample1part10pajensolopart
    sample1part10pajenloop
    sample1part10pajenreverbtype
    sample1part10pajenreverblevel
    sample1part10pajenreverbonoff


    DATA for Volca Sample2 [56 bytes]
    sample2part1level
    sample2part1pan
    sample2part1startpoint
    sample2part1length
    sample2part1hicutoff
    sample2part1speed
    sample2part1pitchegintensity
    sample2part1pitchegattack
    sample2part1pitchegdecay
    sample2part1ampegattack
    sample2part1ampegdecay
    sample2part2level
    sample2part2pan
    sample2part2ampegattack
    sample2part2ampegdecay
    sample2part2hicutoff
    sample2part3level
    sample2part3pan
    sample2part3hicutoff
    sample2part3ampegattack
    sample2part3ampegdecay
    sample2part4level
    sample2part4pan
    sample2part4ampegattack
    sample2part4ampegdecay
    sample2part4hicutoff
    sample2part5level
    sample2part5pan
    sample2part5ampegattack
    sample2part5ampegdecay
    sample2part5hicutoff
    sample2part6level
    sample2part6pan
    sample2part6hicutoff
    sample2part6ampegattack
    sample2part6ampegdecay
    sample2part7level
    sample2part7pan
    sample2part7ampegattack
    sample2part7ampegdecay
    sample2part7hicutoff
    sample2part8level
    sample2part8pan
    sample2part8ampegattack
    sample2part8ampegdecay
    sample2part8hicutoff
    sample2part9level
    sample2part9pan
    sample2part9ampegattack
    sample2part9ampegdecay
    sample2part9hicutoff
    sample2part10level
    sample2part10pan
    sample2part10ampegattack
    sample2part10ampegdecay
    sample2part10hicutoff


    TABLE 1:        VOLCA TYPES     ** Note that Modular and Mix are missing as they do not have MIDI
    0       All types (in order)    ** This is mostly so I can have a format that lets Edisyn initialize its editor
    1       Bass
    2       Beats
    3       Drum Single
    4       Drum Split
    5       FM
    6       Keys
    7       Kick
    8       NuBass
    9       Sample
    10      Sample (Pajen-7 Firmware) Channel 11
    11      Sample2


    =***/
