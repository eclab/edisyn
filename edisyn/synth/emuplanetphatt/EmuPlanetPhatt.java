/***
    Copyright 2022 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.emuplanetphatt;

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
   A patch editor for the E-Mu Planet Phatt, Orbit (V1 and V2), Carnaval, Vintage Keys, and Vintage Keys Plus synthesizers
        
   @author Sean Luke
*/

public class EmuPlanetPhatt extends Synth
    {
    Chooser[] instrumentChoosers = new Chooser[2];
    Chooser[] filterChoosers = new Chooser[2];
    JComboBox synthTypeCombo;
    int synthType;
    public static final int SYNTH_TYPE_PLANET_PHATT = 0;
    public static final int SYNTH_TYPE_ORBIT_V1 = 1;
    public static final int SYNTH_TYPE_ORBIT_V2 = 2;
    public static final int SYNTH_TYPE_CARNAVAL = 3;
    public static final int SYNTH_TYPE_VINTAGE_KEYS = 4;
    public static final int SYNTH_TYPE_VINTAGE_KEYS_PLUS = 5;
    public static final String SYNTH_TYPE_KEY = "SynthType";
    public static final String[] SYNTH_TYPE_NAMES = { "Planet Phatt", "Orbit V1 (9090)", "Orbit V2 (9095)", "Carnaval", "Vintage Keys", "Vintage Keys Plus" };
        
        
    public int getSynthType() { return synthType; }

    boolean settingSynthType = false;
    public void setSynthType(int val, boolean save)
        {
        if (settingSynthType) return;
        settingSynthType = true;
        if (save)
            {
            setLastX("" + val, SYNTH_TYPE_KEY, getSynthClassName(), true);
            }
        synthType = val;
        for(int i = 0; i < 2; i++)
            {
            instrumentChoosers[i].setElements("Instrument", getInstruments());
            model.setMax("layer" + (i + 1) + "instrument", getInstruments().length);
            filterChoosers[i].setElements("Filter Type", getFilters());
            model.setMax("layer" + (i + 1) + "filtertype", getFilters().length);
            }
        synthTypeCombo.setSelectedIndex(val);
        updateTitle();
        settingSynthType = false;
        }
        

    public void addEmuMenu()
        {
        JMenu menu = new JMenu("E-Mu");
        menubar.add(menu);

        JMenuItem oneMPEMenu = new JMenuItem("Set Up Patch as Pseudo-MPE");
        oneMPEMenu.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                JComboBox bank = new JComboBox(getSynthType() == SYNTH_TYPE_ORBIT_V1 || getSynthType() == SYNTH_TYPE_VINTAGE_KEYS || getSynthType() == SYNTH_TYPE_VINTAGE_KEYS_PLUS ? SHORT_BANKS : BANKS);
                bank.setSelectedIndex(model.get("bank", 0));
                int num = model.get("number");
                JTextField number = new SelectedTextField("" + (num < 10 ? "00" : (num < 100 ? "0" : "")) + num, 3);
                
                int n = 0;
                String title = "Set Up Patch as Pseudo-MPE";
                while(true)
                    {
                    boolean result = showMultiOption(EmuPlanetPhatt.this, new String[] { "Bank", "Patch Number"}, 
                        new JComponent[] { bank, number }, title, "Enter the Patch number.");
                                
                    try { n = Integer.parseInt(number.getText()); }
                    catch (NumberFormatException ex)
                        {
                        showSimpleError(title, "The Patch Number must be an integer 0 ... 127");
                        continue;
                        }
                                
                    if (n < 0 || n > 127)
                        {
                        showSimpleError(title, "The Patch Number must be an integer 0 ... 127");
                        continue;
                        }

                    if (result) 
                        break;
                    if (!result)
                        return;
                    }           

                int b = bank.getSelectedIndex();
                                                         
                boolean send = getSendMIDI();
                setSendMIDI(true);
                
                // set up all the channels
                for(int i = 0; i < 16; i++)
                    {
                    setUpChannelForBank(i, b);
                    tryToSendMIDI(buildPC(i, n));
                    }
                setSendMIDI(send);
                }
            });

        menu.add(oneMPEMenu);
        }



    public EmuPlanetPhatt()
        {
        String m = getLastX(SYNTH_TYPE_KEY, getSynthClassName());
        synthType = (m == null ? SYNTH_TYPE_PLANET_PHATT : Integer.parseInt(m));
        
        // we need to build the inverse hashtables but only once
        
        if (parametersToIndex == null)
            {
            parametersToIndex = new HashMap();
            for(int i = 0; i < parameters.length; i++)
                {
                parametersToIndex.put(parameters[i], Integer.valueOf(i));
                }
            }

        if (PLANET_PHATT_OFFSETS_INV == null)
            {
            PLANET_PHATT_OFFSETS_INV = new HashMap();
            for(int i = 0; i < PLANET_PHATT_OFFSETS.length; i++)
                {
                PLANET_PHATT_OFFSETS_INV.put(PLANET_PHATT_OFFSETS[i], Integer.valueOf(i));
                }
            }

        if (ORBIT_V2_OFFSETS_INV == null)
            {
            ORBIT_V2_OFFSETS_INV = new HashMap();
            for(int i = 0; i < ORBIT_V2_OFFSETS.length; i++)
                {
                ORBIT_V2_OFFSETS_INV.put(ORBIT_V2_OFFSETS[i], Integer.valueOf(i));
                }
            }
                                    
        if (CARNAVAL_OFFSETS_INV == null)
            {
            CARNAVAL_OFFSETS_INV = new HashMap();
            for(int i = 0; i < CARNAVAL_OFFSETS.length; i++)
                {
                CARNAVAL_OFFSETS_INV.put(CARNAVAL_OFFSETS[i], Integer.valueOf(i));
                }
            }
                                    
        if (VINTAGE_KEYS_OFFSETS_INV == null)
            {
            VINTAGE_KEYS_OFFSETS_INV = new HashMap();
            for(int i = 0; i < VINTAGE_KEYS_OFFSETS.length; i++)
                {
                VINTAGE_KEYS_OFFSETS_INV.put(VINTAGE_KEYS_OFFSETS[i], Integer.valueOf(i));
                }
            }
                
        if (VINTAGE_KEYS_PLUS_OFFSETS_INV == null)
            {
            VINTAGE_KEYS_PLUS_OFFSETS_INV = new HashMap();
            for(int i = 0; i < VINTAGE_KEYS_PLUS_OFFSETS.length; i++)
                {
                VINTAGE_KEYS_PLUS_OFFSETS_INV.put(VINTAGE_KEYS_PLUS_OFFSETS[i], Integer.valueOf(i));
                }
            }
                                    
        /// SOUND PANEL
                
        SynthPanel soundPanel = new SynthPanel(this);
        
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        JComponent nameGlobal = addNameGlobal(Style.COLOR_GLOBAL());
        vbox.add(nameGlobal);
        hbox.addLast(addPresetLinks(Style.COLOR_B()));
        vbox.add(hbox);
        
        hbox = new HBox();
        hbox.add(addCrossfade(Style.COLOR_A()));
        hbox.addLast(addAuxiliaryEnvelope(Style.COLOR_B()));
        vbox.add(hbox);

        hbox = new HBox();
        hbox.add(addLFO(1, Style.COLOR_A()));
        hbox.addLast(addLFO(2, Style.COLOR_C()));
        vbox.add(hbox);
        
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("General", soundPanel);
                
              
        soundPanel = new SynthPanel(this);

        vbox = new VBox();
        vbox.add(addInstrument(1, Style.COLOR_A()));
        vbox.add(addAlternateEnvelope(1, Style.COLOR_A()));
        vbox.add(addFilter(1, Style.COLOR_A()));

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Primary", soundPanel);
        soundPanel.makePasteable("layer");


        soundPanel = new SynthPanel(this);

        vbox = new VBox();
        vbox.add(addInstrument(2, Style.COLOR_A()));
        vbox.add(addAlternateEnvelope(2, Style.COLOR_A()));
        vbox.add(addFilter(2, Style.COLOR_A()));

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Secondary", soundPanel);
        soundPanel.makePasteable("layer");

        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        vbox.add(addKeyVelocityMatrix(Style.COLOR_A()));

        hbox = new HBox();
        hbox.add(addRealTimeMatrix(Style.COLOR_B()));
        hbox.addLast(addControllerMatrix(Style.COLOR_C()));
        vbox.add(hbox);
        
        vbox.add(addFootswitchMatrix(Style.COLOR_C()));

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Patching", soundPanel);
                        
//        model.set("name", "Untitled");
//        model.set("bank", 0);
//        model.set("number", 0);
        
        // loadDefaults will reset the synth type so here we're gonna reset it back
        int st = getSynthType();
        loadDefaults();        
        setSynthType(st, false);
        }
                
    public String getDefaultResourceFileName() { return "EmuPlanetPhatt.init"; }
    public String getHTMLResourceFileName() { return "EmuPlanetPhatt.html"; }

    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        JComboBox bank = new JComboBox(writing ? WRITABLE_BANKS : (getSynthType() == SYNTH_TYPE_ORBIT_V1 || getSynthType() == SYNTH_TYPE_VINTAGE_KEYS || getSynthType() == SYNTH_TYPE_VINTAGE_KEYS_PLUS ? SHORT_BANKS : BANKS) );
        bank.setSelectedIndex(model.get("bank", 0));
        int num = model.get("number");
        JTextField number = new SelectedTextField("" + (num < 10 ? "00" : (num < 100 ? "0" : "")) + num, 3);
        
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
                showSimpleError(title, "The Patch Number must be an integer 0...127");
                continue;
                }
            if (n < 0 || n > 127)
                {
                showSimpleError(title, "The Patch Number must be an integer 0...127");
                continue;
                }
                                                
            int i = bank.getSelectedIndex();
                        
            change.set("bank", i);
            change.set("number", n);
                        
            return true;
            }
        }
                
    /** Add the global patch category (name, id, number, etc.) */
    public JComponent addNameGlobal(Color color)
        {
        // Added a space after "Keys" because of an apparent measurement error.
        Category globalCategory = new Category(this, "E-Mu Planet Phatt / Orbit / Carnaval / Vintage Keys ", color); 
        //globalCategory.makeUnresettable();
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
                                
        VBox vbox = new VBox();
        comp = new PatchDisplay(this, 7);
        vbox.add(comp);
        hbox.add(vbox);
        
        comp = new StringComponent("Patch Name", this, "name", 12, "Name must be up to 12 characters.")
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
        
        JLabel label = new JLabel("  Synthesizer Type", SwingConstants.LEFT)
            {
            public Insets getInsets() { return new Insets(0, 0, 0, 0); }
            };
        label.setFont(Style.SMALL_FONT());
        label.setBackground(Style.BACKGROUND_COLOR()); // TRANSPARENT);
        label.setForeground(Style.TEXT_COLOR());

        synthTypeCombo = new JComboBox(SYNTH_TYPE_NAMES);
        synthTypeCombo.putClientProperty("JComponent.sizeVariant", "small");
        synthTypeCombo.setEditable(false);
        synthTypeCombo.setFont(Style.SMALL_FONT());
        synthTypeCombo.setSelectedIndex(getSynthType());
        synthTypeCombo.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                setSynthType(synthTypeCombo.getSelectedIndex(), true);
                }
            });

        vbox = new VBox();
        HBox labelBox = new HBox();
        labelBox.add(label);
        vbox.add(labelBox);
        vbox.add(synthTypeCombo);
        hbox.add(vbox);

        vbox = new VBox();
        params = SUBMIXES;
        comp = new Chooser("Mix Output", this, "submix", params);
        vbox.add(comp);
        
        params = KEYBOARD_TUNINGS;
        comp = new Chooser("Tuning", this, "keyboardtuning", params);
        vbox.add(comp);
        hbox.add(vbox);
        
        comp = new LabelledDial("Pressure", this, "pressureamount", color, -128, 127);
        hbox.add(comp);
        
        comp = new LabelledDial("Bend Range", this, "pitchbendrange", color, 0, 13)
            {
            public String map(int val)
                {
                if (val == 13) return "Global";
                else return "" + val;
                }
            };
        getModel().setMetricMax("pitchbendrange", 12);
        hbox.add(comp);

        comp = new LabelledDial("Velocity", this, "velocitycurve", color, 0, 5)
            {
            public String map(int val)
                {
                if (val == 0) return "Off";
                if (val == 5) return "Global";
                else return "" + val;
                }
            };
        getModel().setMetricMax("velocitycurve", 4);  // I presume "off" is metric
        ((LabelledDial)comp).addAdditionalLabel("Curve");
        hbox.add(comp);
        
        comp = new LabelledDial("Keyboard", this, "keyboardcenter", color, 0, 127)
            {
            public String map(int val)
                {
                return "" + NOTES[val % 12] + "" + (val / 12 + 1);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Center");
        hbox.add(comp);
        
        comp = new LabelledDial("Low Key", this, "rangelowkey", color, 0, 127)
            {
            public String map(int val)
                {
                return (NOTES[val % 12] + ((val / 12) - 2));
                }
            };
        hbox.add(comp);
        
        comp = new LabelledDial("High Key", this, "rangehighkey", color, 0, 127)
            {
            public String map(int val)
                {
                return (NOTES[val % 12] + ((val / 12) - 2));
                }
            };
        hbox.add(comp);
        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }
        
    public JComponent addPresetLinks(Color color)
        {
        Category category = new Category(this, "Preset Links", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        for(int i = 1; i <= 3; i++)
            {
            comp = new LabelledDial("Link " + i, this, "presetlink" + i, color, -1, 639)
                {
                public String map(int val)
                    {
                    return (val == -1 ? "Off" : "" + val);
                    }
                };
            ((LabelledDial)comp).addAdditionalLabel("[K,V1: <= 511]");
            hbox.add(comp);

            comp = new LabelledDial("Link " + i, this, "presetlinklowkey" + i, color, 0, 127)
                {
                public String map(int val)
                    {
                    return (NOTES[val % 12] + ((val / 12) - 2));
                    }
                };
            ((LabelledDial)comp).addAdditionalLabel("Low Key");
            hbox.add(comp);

            comp = new LabelledDial("Link " + i, this, "presetlinkhighkey" + i, color, 0, 127)
                {
                public String map(int val)
                    {
                    return (NOTES[val % 12] + ((val / 12) - 2));
                    }
                };
            ((LabelledDial)comp).addAdditionalLabel("High Key");
            hbox.add(comp);
                        
            hbox.add(Strut.makeHorizontalStrut(16));
            }

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
                
    
    public JComponent addInstrument(int i, Color color)
        {
        Category category = new Category(this, (i == 1 ? "Primary Instrument" : "Secondary Instrument"), color);
        category.makePasteable("layer");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        params = getInstruments();
        comp = instrumentChoosers[i - 1] = new Chooser("Instrument", this, "layer" + i + "instrument", params);
        vbox.add(comp);

        params = SOLO_MODES;
        comp = new Chooser("Solo Mode", this, "layer" + i + "solomode", params);
        vbox.add(comp);
        
        comp = new CheckBox("Reverse", this, "layer" + i + "reversesound");
        vbox.add(comp);

        hbox.add(vbox);

        comp = new LabelledDial("Offset", this, "layer" + i + "soundstartoffset", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Coarse", this, "layer" + i + "tuningcoarse", color, -36, 36)
            {
            public boolean isSymmetric() { return true; }
            };
        ((LabelledDial)comp).addAdditionalLabel("Tuning");
        hbox.add(comp);

        comp = new LabelledDial("Fine", this, "layer" + i + "tuningfine", color, -64, 64)
            {
            public boolean isSymmetric() { return true; }
            };
        ((LabelledDial)comp).addAdditionalLabel("Tuning");
        hbox.add(comp);

        comp = new LabelledDial("Volume", this, "layer" + i + "volume", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Pan", this, "layer" + i + "pan", color, -7, 7);
        hbox.add(comp);

        comp = new LabelledDial("Delay", this, "layer" + i + "delay", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Low Key", this, "layer" + i + "lowkey", color, 0, 127) 
            {
            public String map(int val)
                {
                return (NOTES[val % 12] + ((val / 12) - 2));
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("High Key", this, "layer" + i + "highkey", color, 0, 127)       
            {
            public String map(int val)
                {
                return (NOTES[val % 12] + ((val / 12) - 2));
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Chorus", this, "layer" + i + "chorus", color, 0, 15);
        hbox.add(comp);

        comp = new LabelledDial("Portamento", this, "layer" + i + "portamentorate", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Rate");
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
                

    public JComponent addFilter(int i, Color color)
        {
        Category category = new Category(this, (i == 1 ? "Primary Filter" : "Secondary Filter"), color);
        category.makePasteable("layer");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        params = getFilters();
        comp = filterChoosers[i - 1] = new Chooser("Filter Type", this, "layer" + i + "filtertype", params);
        vbox.add(comp);
        
        hbox.add(vbox);

        comp = new LabelledDial("Cutoff", this, "layer" + i + "filterfc", color, 0, 255);
        hbox.add(comp);

        comp = new LabelledDial("Resonance", this, "layer" + i + "filterq", color, 0, 15);
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
                


    public JComponent addAlternateEnvelope(int inst, Color color)
        {
        Category category = new Category(this, (inst == 1 ? "Primary Alternate Volume Envelope" : "Secondary Alternate Volume Envelope"), color);
        category.makePasteable("layer");
               
        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();

        VBox vbox = new VBox();
 
        comp = new CheckBox("Enable", this, "layer" + inst + "altvolumeenvelopeon");
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Attack", this, "layer" + inst + "altvolumeattack", color, 0, 99);
        hbox.add(comp);
                
        comp = new LabelledDial("Hold", this, "layer" + inst + "altvolumehold", color, 0, 99);
        hbox.add(comp);
                
        comp = new LabelledDial("Decay", this, "layer" + inst + "altvolumedecay", color, 0, 99);
        hbox.add(comp);
                
        comp = new LabelledDial("Sustain", this, "layer" + inst + "altvolumesustain", color, 0, 99);
        hbox.add(comp);
                
        comp = new LabelledDial("Release", this, "layer" + inst + "altvolumerelease", color, 0, 99);
        hbox.add(comp);
 
        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "layer" + inst + "altvolumeattack", "layer" + inst + "altvolumehold", "layer" + inst + "altvolumedecay", null, "layer" + inst + "altvolumerelease" },
            new String[] { null, null, null, "layer" + inst + "altvolumesustain", "layer" + inst + "altvolumesustain", null },
            new double[] { 0, 0.25, 0.25/99.0, 0.25, 0.25, 0.25 },
            new double[] { 0, 1.0, 1.0, 1.0/99.0, 1.0/99.0, 0 },
            new double[] { 0, (Math.PI/4/99.0), EnvelopeDisplay.TIME, (Math.PI/4/99.0), 0, (Math.PI/4/99.0) }
            );
        hbox.addLast(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
                


    public JComponent addAuxiliaryEnvelope(Color color)
        {
        Category category = new Category(this, "Auxiliary Envelope", color);
                
        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();
        VBox vbox = new VBox();
 
        comp = new LabelledDial("Amount", this, "auxenvelopeamount", color, -128, 127);
        hbox.add(comp);

        comp = new LabelledDial("Delay", this, "auxenvelopedelay", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Attack", this, "auxenvelopeattack", color, 0, 99);
        hbox.add(comp);
                
        comp = new LabelledDial("Hold", this, "auxenvelopehold", color, 0, 99);
        hbox.add(comp);
                
        comp = new LabelledDial("Decay", this, "auxenvelopedecay", color, 0, 99);
        hbox.add(comp);
                
        comp = new LabelledDial("Sustain", this, "auxenvelopesustain", color, 0, 99);
        hbox.add(comp);
                
        comp = new LabelledDial("Release", this, "auxenveloperelease", color, 0, 99);
        hbox.add(comp);
 
        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "auxenvelopedelay", "auxenvelopeattack", "auxenvelopehold", "auxenvelopedecay", null, "auxenveloperelease" },
            new String[] { null, null, null, null, "auxenvelopesustain", "auxenvelopesustain", null },
            new double[] { 0, 0.2/127.0, 0.2, 0.2/99.0, 0.2, 0.2, 0.2 },
            new double[] { 0, 0, 1.0, 1.0, 1.0/99.0, 1.0/99.0, 0 },
            new double[] { 0, EnvelopeDisplay.TIME, (Math.PI/4/99.0), EnvelopeDisplay.TIME, (Math.PI/4/99.0), 0, (Math.PI/4/99.0) }
            );
        hbox.addLast(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
        
        
    public JComponent addCrossfade(Color color)
        {
        Category category = new Category(this, "Crossfade", color);
                
        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();
        VBox vbox = new VBox();
 
        final LabelledDial crossFadeSwitchPoint = new LabelledDial("Switch Point", this, "switchpoint", color, 0, 127)
            {
            public String map(int val)
                {
                return (model.get("crossfademode", 0) < 2 ? "" + val : (NOTES[val % 12] + ((val / 12) - 2)));
                }
            };
        
        params = XFADE_MODES;
        comp = new Chooser("Mode", this, "crossfademode", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                crossFadeSwitchPoint.repaint();
                }
            };
        vbox.add(comp);
        
        params = XFADE_DIRECTIONS;
        comp = new Chooser("Direction", this, "crossfadedirection", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Amount", this, "crossfadeamount", color, 0, 255);
        hbox.add(comp);
                
        comp = new LabelledDial("Balance", this, "crossfadebalance", color, 0, 127);
        hbox.add(comp);

        hbox.add(crossFadeSwitchPoint);
                
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

        params = LFO_SHAPES;
        comp = new Chooser("Shape", this, "lfo" + lfo + "shape", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Rate", this, "lfo" + lfo + "rate", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Delay", this, "lfo" + lfo + "delay", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Variation", this, "lfo" + lfo + "variation", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Amount", this, "lfo" + lfo + "amount", color, -128, 127);
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


                
    public JComponent addKeyVelocityMatrix(Color color)
        {
        Category category = new Category(this, "Key-Velocity Matrix", color);
        category.makeDistributable("keyvel");

        JComponent comp;
        String[] params;
        VBox main = new VBox();
        HBox hbox = new HBox();

        for(int i = 1; i <= 6; i++)
            {
            VBox vbox = new VBox();
            params = KEYBOARD_AND_VELOCITY_MODULATION_SOURCES;
            comp = new Chooser("Source " + i, this, "keyvelsource" + i, params);
            vbox.add(comp);

            params = KEYBOARD_AND_VELOCITY_MODULATION_DESTINATIONS;
            comp = new Chooser("Destination " + i, this, "keyveldest" + i, params);
            vbox.add(comp);
            hbox.add(vbox);

            comp = new LabelledDial("Amount " + i, this, "keyvelamount" + i, color, -128, 127);
            hbox.add(comp);
            if (i == 3 || i == 6)
                {
                main.add(hbox);
                hbox = new HBox();
                if (i == 3) main.add(Strut.makeVerticalStrut(8));
                }
            else
                {
                // do nothing
                }
            }

        category.add(main, BorderLayout.CENTER);
        return category;
        }

                
                
    public JComponent addRealTimeMatrix(Color color)
        {
        Category category = new Category(this, "Real Time Matrix", color);
        category.makeDistributable("realtime");

        JComponent comp;
        String[] params;
        VBox main = new VBox();
        HBox hbox = new HBox();

        for(int i = 1; i <= 8; i++)
            {
            VBox vbox = new VBox();
            params = REALTIME_MODULATION_SOURCES;
            comp = new Chooser("Source " + i, this, "realtimesource" + i, params);
            vbox.add(comp);

            params = REALTIME_MODULATION_DESTINATIONS;
            comp = new Chooser("Destination " + i, this, "realtimedest" + i, params);
            vbox.add(comp);
            hbox.add(vbox);

            if (i == 4 || i == 8)
                {
                main.add(hbox);
                hbox = new HBox();
                if (i == 4) main.add(Strut.makeVerticalStrut(8));
                }
            else
                {
                // nothing for the moment
                }
            }

        category.add(main, BorderLayout.CENTER);
        return category;
        }

    public JComponent addFootswitchMatrix(Color color)
        {
        Category category = new Category(this, "Footswitch Matrix", color);
        category.makeDistributable("footswitchdest");

        JComponent comp;
        String[] params;
        VBox main = new VBox();
        HBox hbox = new HBox();

        for(int i = 1; i <= 3; i++)
            {
            VBox vbox = new VBox();
            params = FOOTSWITCH_MODULATION_DESTINATIONS;
            comp = new Chooser("Destination " + i, this, "footswitchdest" + i, params);
            vbox.add(comp);
            hbox.add(vbox);
            }

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addControllerMatrix(Color color)
        {
        Category category = new Category(this, "Controller Matrix", color);
        category.makeDistributable("controlleramount");

        JComponent comp;
        String[] params;
        VBox main = new VBox();
        HBox hbox = new HBox();

        comp = new LabelledDial("Amount A", this, "controlleramount1", color, -128, 127);
        hbox.add(comp);

        comp = new LabelledDial("Amount B", this, "controlleramount2", color, -128, 127);
        hbox.add(comp);

        comp = new LabelledDial("Amount C", this, "controlleramount3", color, -128, 127);
        hbox.add(comp);

        comp = new LabelledDial("Amount D", this, "controlleramount4", color, -128, 127);
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }





    
        
    public byte getID() 
        { 
        try 
            { 
            byte b = (byte)(Byte.parseByte(tuple.id));
            if (b >= 0 && b < 16) return b;
            }
        catch (NullPointerException e) { } // expected.  Happens when tuple's not built yet
        catch (NumberFormatException e) { Synth.handleException(e); }
        return 0;
        }
        
    public String reviseID(String id)
        {
        try 
            { 
            byte b =(byte)(Byte.parseByte(id)); 
            if (b >= 0 && b < 16) return "" + b;
            } 
        catch (NumberFormatException e) { }             // expected
        return "" + getID();
        }



    /*
      public static final int[] SYNTH_SOUNDSET_BANK_1_START = { 242, 236, 236, 223, -1, -1 };
      public static final int[] SYNTH_SOUNDSET_BANK_0_OFFSET = { 3328, 2816, 2816, 3840, 1536, 1536 };
      public static final int[] SYNTH_SOUNDSET_BANK_1_OFFSET = { 3584, 3072, 3072, 4096, -1, -1 };
    */
        
    public int convertInstrumentToMIDI(int inst)
        {
        // Note that all instruments in the SECOND bank are 1-based, since in the second bank 0 also represents "none".  What a mess.
        int s = getSynthType();

        if (s == SYNTH_TYPE_PLANET_PHATT)
            {
            return PLANET_PHATT_OFFSETS[inst];
            }
        else if (s == SYNTH_TYPE_ORBIT_V1 || s == SYNTH_TYPE_ORBIT_V2)
            {
            return ORBIT_V2_OFFSETS[inst];
            }
        else if (s == SYNTH_TYPE_CARNAVAL)
            {
            return CARNAVAL_OFFSETS[inst];
            }
        else
            {
            return VINTAGE_KEYS_PLUS_OFFSETS[inst];         // also works for vintage keys
            }
        }

    // note that if SYNTH_TYPE_VINTAGE_KEYS is reported, it could still be SYNTH_TYPE_VINTAGE_KEYS_PLUS
    // note that only SYNTH_TYPE_ORBIT_V2 is reported: but it could also be SYNTH_TYPE_ORBIT_V1
    public int getSynthForMIDIInstrument(int midi)
        {
        // Though the documentation doesn't say this, it appears that None is often midi = 0 regardless of offset.
        // So this doesn't tell us anything about what instrument we are
        if (midi == 0) return -1;
        
        if (midi == 3328 || midi == 3584) return SYNTH_TYPE_PLANET_PHATT;       // none
        else if (midi == 2816 || midi == 3072) return SYNTH_TYPE_ORBIT_V2;      // none
        else if (midi == 3840 || midi == 4096) return SYNTH_TYPE_CARNAVAL;      // none
        else if (midi == 1536) return SYNTH_TYPE_VINTAGE_KEYS;
                        
        if (PLANET_PHATT_OFFSETS_INV.containsKey(midi))
            return SYNTH_TYPE_PLANET_PHATT;
        else if (ORBIT_V2_OFFSETS_INV.containsKey(midi))
            return SYNTH_TYPE_ORBIT_V2;
        else if (CARNAVAL_OFFSETS_INV.containsKey(midi))
            return SYNTH_TYPE_CARNAVAL;
        else if (VINTAGE_KEYS_OFFSETS_INV.containsKey(midi))
            return SYNTH_TYPE_VINTAGE_KEYS;
        else if (VINTAGE_KEYS_PLUS_OFFSETS_INV.containsKey(midi))
            return SYNTH_TYPE_VINTAGE_KEYS_PLUS;
        else return -1;
        }

    // We return the instrument corresponding to the given midi data, or 0 if the midi data
    // is not in the range for our kind of synthesizer. 
    public int convertMIDIToInstrument(int midi)
        {
        int m = getSynthForMIDIInstrument(midi);
        Object inst = null;
                
        // Though the documentation doesn't say this, it appears that None is often midi = 0 regardless of offset.
        if (midi == 0) return 0;
        
        if (midi == 3328 || midi == 3584) return 0;     // none
        else if (midi == 2816 || midi == 3072) return 0;        // none
        else if (midi == 3840 || midi == 4096) return 0;        // none
        else if (midi == 1536) return 0;

        if (m == -1) // uh oh
            {
            System.err.println("EmuPlanetPhatt.convertMIDIToInstrument: Unknown synth for MIDI instrument " + midi);
            return 0;
            }
        else if (m == SYNTH_TYPE_PLANET_PHATT)
            {
            inst = PLANET_PHATT_OFFSETS_INV.get(midi);
            }
        else if (m == SYNTH_TYPE_ORBIT_V2 || m == SYNTH_TYPE_ORBIT_V1)  // will never be V1
            {
            inst = ORBIT_V2_OFFSETS_INV.get(midi);
            }
        else if (m == SYNTH_TYPE_CARNAVAL)
            {
            inst = CARNAVAL_OFFSETS_INV.get(midi);
            }
        else if (m == SYNTH_TYPE_VINTAGE_KEYS)
            {
            inst = VINTAGE_KEYS_OFFSETS_INV.get(midi);
            }
        else
            {
            inst = VINTAGE_KEYS_PLUS_OFFSETS_INV.get(midi);
            }
                        
        if (inst == null)               // probably shouldn't happen
            {
            System.err.println("EmuPlanetPhatt.convertMIDIToInstrument: Unknown MIDI instrument " + midi);
            return 0;
            }
        else return ((Integer)inst).intValue();
        }


    public Object[] emitAll(String key)
        {
        if (key.equals("bank")) return new Object[0];  // this is not emittable
        if (key.equals("number")) return new Object[0];  // this is not emittable

        if (key.equals("name"))
            {
            String name = model.get(key, "") + "            ";
            Object[] ret = new Object[12];
            for(int i = 0; i < 12; i++)
                {
                int param = i;
                int value =  (byte)(name.charAt(i));
                if (value < 0) value += 16384;          // convert 2's complement
                byte p1 = (byte)(param & 127);
                byte p2 = (byte)((param >>> 7) & 127);
                byte v1 = (byte)(value & 127);
                byte v2 = (byte)((value >>> 7) & 127);
                        
                ret[i] = new byte[]
                    {
                    (byte)0xF0,
                    (byte)0x18,
                    (byte)0x0A,
                    getID(),
                    (byte)0x03,
                    p1, p2, v1, v2,
                    (byte)0xF7
                    };
                }
            return ret;
            }
        else
            {
            int param = ((Integer)(parametersToIndex.get(key))).intValue();
            int value = model.get(key);
                
            if (key.startsWith("realtimedest"))
                {
                // gotta map
                value = REALTIME_MODULATION_DESTINATIONS_MAP[value];
                }
                        
            else if (key.equals("layer1instrument") || key.equals("layer2instrument"))
                {
                // we need to add the sound set offset to the value.
                value = convertInstrumentToMIDI(value);
                }
            if (value < 0) value += 16384;          // convert 2's complement
                
            byte p1 = (byte)(param & 127);
            byte p2 = (byte)((param >>> 7) & 127);
            byte v1 = (byte)(value & 127);
            byte v2 = (byte)((value >>> 7) & 127);
                
            byte[] data = new byte[]
                {
                (byte)0xF0,
                (byte)0x18,
                (byte)0x0A,
                getID(),
                (byte)0x03,
                p1, p2, v1, v2,
                (byte)0xF7
                };
            return new Object[] { data };
            }
        }


    public Object[] emitAll(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        if (tempModel == null)
            tempModel = getModel();

        int NN = tempModel.get("bank", 0) * 128 + tempModel.get("number", 0);
        if (toWorkingMemory)
            {
            NN = 127;               // we write to the top patch number of bank 0 (RAM)
            // furthermore we have to change the patch because sendAllParameters
            // doesn't do it by default
            Model model = buildModel();
            model.set("bank", 0);
            model.set("number", NN);
            changePatch(model);
            }
        byte[] data = new byte[parameters.length * 2 + 9];
        
        data[0] = (byte)0xF0;
        data[1] = (byte)0x18;
        data[2] = (byte)0x0A;
        data[3] = (byte)getID();
        data[4] = (byte)0x01;
        data[5] = (byte)(NN % 128);
        data[6] = (byte)(NN / 128);
        
        int offset = 7;
        
        int checksum = 0;
                
        // Load name
        String name = model.get("name", "") + "            ";
        for(int i = 0; i < 12; i++)
            {
            int val = (byte)(name.charAt(i));
            if (val < 0) val = val + 16384;
            data[offset++] = (byte)(val % 128);
            checksum += data[offset-1];
            data[offset++] = (byte)(val / 128);
            checksum += data[offset-1];
            }
                                
        // Load remaining parameters
        for(int i = 12; i < parameters.length; i++)
            {
            int val = model.get(parameters[i], 0);

            if (parameters[i].startsWith("realtimedest"))
                {
                // gotta map
                val = REALTIME_MODULATION_DESTINATIONS_MAP[val];
                }
                        
            else if (parameters[i].equals("layer1instrument") || parameters[i].equals("layer2instrument"))
                {
                // we need to add the sound set offset to the value.
                val = convertInstrumentToMIDI(val);
                }
                        
            if (val < 0) val = val + 16384;
            data[offset++] = (byte)(val % 128);
            checksum += data[offset-1];
            data[offset++] = (byte)(val / 128);
            checksum += data[offset-1];
            }
              
        data[offset++] = (byte)(checksum & 127);
        data[offset++] = (byte)0xF7;

        Object[] result = new Object[] { data };
        return result;
        }

    public int parse(byte[] data, boolean fromFile)
        {
        int NN = data[5] + data[6] * 128;
        model.set("bank", NN / 128);
        model.set("number", NN % 128);
        
        int offset = 7;
        
        // Load name
        char[] name = new char[12];
        for(int i = 0; i < 12; i++)
            {
            int val = (data[offset++] + data[offset++] * 128);
            name[i] = (char)val;
            }
        model.set("name", new String(name));
                                
        // Load remaining parameters
        for(int i = 12; i < parameters.length; i++)
            {
            int o = offset;
            int val = (data[offset++] + data[offset++] * 128);
            if (val >= 8192)
                val = val - 16384;

            if (parameters[i].startsWith("realtimedest"))
                {
                // gotta inverse map
                val = REALTIME_MODULATION_DESTINATIONS_INVMAP[val];
                }
                        
            else if (parameters[i].equals("layer1instrument") || 
                parameters[i].equals("layer2instrument"))
                {
                // Switch to proper synth type?
                int m = getSynthForMIDIInstrument(val);
                int s = getSynthType();

                if ((m != -1) &&                                // -1 indicates "I don't know based on this instrument"
                    ! (m == s || 
                        (m == SYNTH_TYPE_VINTAGE_KEYS && s == SYNTH_TYPE_VINTAGE_KEYS_PLUS) ||          // might be plus if keys is reported
                        (m == SYNTH_TYPE_ORBIT_V2 && s == SYNTH_TYPE_ORBIT_V1)))                                        // might be v1 if v2 is reported
                    {
                    // switch
                    setSynthType(m, false);
                    }

                // at any rate we need to remove the offset from the value.
                val = convertMIDIToInstrument(val);
                }
                        
            if (!parameters[i].equals("---"))
                model.set(parameters[i], val);
            }
        
        revise();
        return PARSE_SUCCEEDED;
        }
    
    void setUpChannelForBank(int channel, int bank)
        {
        // First we set the bank for the channel in question
        int chan = 367 + channel;
        tryToSendSysex(new byte[]
            {
            (byte)0xF0, 
            (byte)0x18, 
            (byte)0x0A, 
            getID(), 
            (byte)0x03, 
            (byte)(chan & 127),
            (byte)((chan >>> 7) & 127),             // will always be zero of course
            (byte)(bank & 127),
            (byte)((bank >>> 7) & 127),             // will always be zero of course
            (byte)0xF7
            });

        // Next we turn on the channel in question
        int enable = 384 + channel;
        tryToSendSysex(new byte[]
            {
            (byte)0xF0, 
            (byte)0x18, 
            (byte)0x0A, 
            getID(), 
            (byte)0x03, 
            (byte)(enable & 127),
            (byte)((enable >>> 7) & 127),           // will always be zero of course
            (byte)1,
            (byte)0,
            (byte)0xF7
            });

        // Next we turn on PC for the channel in question
        int enablePC = 400 + channel;
        tryToSendSysex(new byte[]
            {
            (byte)0xF0, 
            (byte)0x18, 
            (byte)0x0A, 
            getID(), 
            (byte)0x03, 
            (byte)(enablePC & 127),
            (byte)((enablePC >>> 7) & 127),         // will always be zero of course
            (byte)1,
            (byte)0,
            (byte)0xF7
            });
        }
        
    public void changePatch(Model tempModel)
        {
        int bank = tempModel.get("bank", 0);
        int number = tempModel.get("number", 0);

/*
// Changing patch is nasty on these machines.  We need to modify the program map,
// then do a PC.  Let's first emit a program map where EVERY SINGLE PC goes to our
// desired patch
                
byte[] data = new byte[262];
data[0] = (byte)0xF0;
data[1] = (byte)0x18;
data[2] = (byte)0x0A;
data[3] = (byte)getID();
data[4] = (byte)0x07;
data[261] = (byte)0xF7;
for(int i = 0; i < 128; i++)
{
int val = bank * 128 + number;
data[i*2+5] = (byte)(val & 127);        // I *think* this is lsb first?  It's not explained in the docs
data[i*2+5+1] = (byte)((val >>> 7) & 127);
}
tryToSendSysex(data);
                
// Do we need a pause here?  Dunno
//simplePause(1000);
                
// Now send a PC to 0
tryToSendMIDI(buildPC(getChannelOut(), 0));
*/

        // set up channel
        setUpChannelForBank(getChannelOut(), bank);
        // Last we do a PC to the number
        tryToSendMIDI(buildPC(getChannelOut(), number));
        }

    public byte[] requestDump(Model tempModel)
        {
        if (tempModel == null)
            tempModel = getModel();

        int NN = tempModel.get("bank", 0) * 128 + tempModel.get("number", 0);
        
        byte[] data = new byte[8];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x18;
        data[2] = (byte)0x0A;
        data[3] = (byte)getID();
        data[4] = (byte)0x00;
        data[5] = (byte)(NN % 128);
        data[6] = (byte)(NN / 128);
        data[7] = (byte)0xF7;
        return data;
        }
        
    public JFrame sprout()
        {
        JFrame frame = super.sprout();
        receiveCurrent.setEnabled(false); // can't request current
        addEmuMenu();
        return frame;
        }        
                
    public static final int MAXIMUM_NAME_LENGTH = 12;
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
        
        String nm = model.get("name", "Untitled");
        String newnm = revisePatchName(nm);
        if (!nm.equals(newnm))
            model.set("name", newnm);
        }
    
    public String getTitleBarSynthName()
        {
        return SYNTH_TYPE_NAMES[getSynthType()];
        }
        
    public static String getSynthName() { return "E-Mu Planet Phatt / Orbit / Carnaval / Vintage Keys"; }
    public String getPatchName(Model model) { return model.get("name", "Untitled"); }
    
    public Model getNextPatchLocation(Model model)
        {
        int bank = model.get("bank");
        int number = model.get("number");
        
        int numBanks = (getSynthType() == SYNTH_TYPE_ORBIT_V1 || getSynthType() == SYNTH_TYPE_VINTAGE_KEYS || 
            getSynthType() == SYNTH_TYPE_VINTAGE_KEYS_PLUS ? SHORT_BANKS : BANKS).length;
        number++;
        if (number >= 128)
            {
            bank++;
            number = 0;
            if (bank >= numBanks)
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
        
        int number = (model.get("number"));
        return (BANKS[model.get("bank")] + "-" + (number > 99 ? "" : (number > 9 ? "0" : "00")) + number);
        }
        
    
    /** Map of parameter -> index in the allParameters array. */
    public static HashMap parametersToIndex;


    /** List of all Emu Morpheus parameters in order. */
                
    final static String[] parameters =
        {
        "---",    
        "---",    
        "---",    
        "---",    
        "---",    
        "---",    
        "---",    
        "---",    
        "---",    
        "---",    
        "---",    
        "---",   
        "presetlink1",
        "presetlink2",
        "presetlink3",
        "rangelowkey",
        "presetlinklowkey1",
        "presetlinklowkey2",
        "presetlinklowkey3",
        "rangehighkey",
        "presetlinkhighkey1",
        "presetlinkhighkey2",
        "presetlinkhighkey3",
        "layer1instrument",
        "layer1soundstartoffset",
        "layer1tuningcoarse",
        "layer1tuningfine",
        "layer1volume",
        "layer1pan",
        "layer1delay",
        "layer1lowkey",
        "layer1highkey",
        "layer1altvolumeattack",
        "layer1altvolumehold",
        "layer1altvolumedecay",
        "layer1altvolumesustain",
        "layer1altvolumerelease",
        "layer1altvolumeenvelopeon",
        "layer1solomode",
        "layer1chorus",
        "layer1reversesound",
        "layer2instrument",
        "layer2soundstartoffset",
        "layer2tuningcoarse",
        "layer2tuningfine",
        "layer2volume",
        "layer2pan",
        "layer2delay",
        "layer2lowkey",
        "layer2highkey",
        "layer2altvolumeattack",
        "layer2altvolumehold",
        "layer2altvolumedecay",
        "layer2altvolumesustain",
        "layer2altvolumerelease",
        "layer2altvolumeenvelopeon",
        "layer2solomode",
        "layer2chorus",
        "layer2reversesound",
        "crossfademode",
        "crossfadedirection",
        "crossfadebalance",
        "crossfadeamount",
        "switchpoint",
        "lfo1shape",
        "lfo1rate",
        "lfo1delay",
        "lfo1variation",
        "lfo1amount",
        "lfo2shape",
        "lfo2rate",
        "lfo2delay",
        "lfo2variation",
        "lfo2amount",
        "auxenvelopedelay",
        "auxenvelopeattack",
        "auxenvelopehold",
        "auxenvelopedecay",
        "auxenvelopesustain",
        "auxenveloperelease",
        "auxenvelopeamount",
        "keyvelsource1",
        "keyvelsource2",
        "keyvelsource3",
        "keyvelsource4",
        "keyvelsource5",
        "keyvelsource6",
        "keyveldest1",
        "keyveldest2",
        "keyveldest3",
        "keyveldest4",
        "keyveldest5",
        "keyveldest6",
        "keyvelamount1",
        "keyvelamount2",
        "keyvelamount3",
        "keyvelamount4",
        "keyvelamount5",
        "keyvelamount6",
        "realtimesource1",
        "realtimesource2",
        "realtimesource3",
        "realtimesource4",
        "realtimesource5",
        "realtimesource6",
        "realtimesource7",
        "realtimesource8",
        "realtimedest1",
        "realtimedest2",
        "realtimedest3",
        "realtimedest4",
        "realtimedest5",
        "realtimedest6",
        "realtimedest7",
        "realtimedest8",
        "footswitchdest1",
        "footswitchdest2",
        "footswitchdest3",
        "controlleramount1",
        "controlleramount2",
        "controlleramount3",
        "controlleramount4",
        "pressureamount",
        "pitchbendrange",
        "velocitycurve",
        "keyboardcenter",
        "submix",
        "keyboardtuning",
        "layer1portamentorate",
        "layer2portamentorate",
        "layer1filtertype",
        "layer1filterfc",
        "layer1filterq",
        "layer2filtertype",
        "layer2filterfc",
        "layer2filterq",
        };
    
    public static final String[] NOTES = new String[] { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" }; // weird, starts at C
    public static final String[] SOLO_MODES = new String[] { "Off", "Wind", "Synth" };
    

    // Orbit V1, Vintage Keys, and Vintage Keys Plus don't have 4 ROM
    public static final String[] BANKS = new String[] { "0 RAM", "1 RAM", "2 ROM", "3 ROM", "4 ROM" };
    public static final String[] SHORT_BANKS = new String[] { "0 RAM", "1 RAM", "2 ROM", "3 ROM" };
    public static final String[] WRITABLE_BANKS = new String[] { "0 RAM", "1 RAM" };
    public static final String[] XFADE_MODES = new String[] { "Off", "XFade", "XSwitch" };
    public static final String[] XFADE_DIRECTIONS = new String[] { "Pri->Sec", "Sec->Pri" };
    // This is the correct order, different from what's indicated in the text
    public static final String[] LFO_SHAPES = new String[] { "Rand", "Tri", "Sine", "Saw", "Square", "Sync Tri", "Sync Sine", "Sync Saw", "Sync Square"};
    public static final String[] SUBMIXES = new String[] { "Main", "Sub 1", "Sub 2" };
    public static final String[] KEYBOARD_TUNINGS = { "Equal", "Just C", "Valotti", "19 Tone", "Gamelan", "User" };
    
    // This is just a guess
    public static final String[] FOOTSWITCH_MODULATION_DESTINATIONS =
        {
        "Off",
        "SustainS",
        "SustainP",
        "Sustain",
        "AltVolEnvS",
        "AltVolEnvP",
        "AltVolEnv",
        "AltVolRelS",
        "AltVolRelP",
        "AltVolRel",
        "Cross-Switch"
        };
                
    // This is just a guess
    public static final String[] KEYBOARD_AND_VELOCITY_MODULATION_SOURCES =
        {
        "Key",
        "Velocity"
        };
        
    public static final String[] KEYBOARD_AND_VELOCITY_MODULATION_DESTINATIONS =
        {
        "Off",
        "Pitch",
        "PitchP",
        "PitchS",
        "Volume",
        "VolumeP",
        "VolumeS",
        "Attack",
        "AttackP",
        "AttackS",
        "Decay",
        "DecayP",
        "DecayS",
        "Release",
        "RelP",
        "RelS",
        "XFade",
        "Lfo1Amt",
        "Lfo1Rt",
        "Lfo2Amt",
        "Lfo2Rt",
        "AuxAmt",
        "AuxAtt",
        "AuxDec",
        "AuxRel",
        "Sound Start",
        "P Sound Start",
        "S Sound Start",
        "Pan",
        "PanP",
        "PanS",
        "Tone",
        "ToneP",
        "ToneS",
        "FilterFc",
        "PFilterFc",
        "SFilterFc",
        "FilterQ",
        "P FilterQ",
        "S Filter Q",
        "Port Rate",
        "P Port Rate",
        "S Port Rate"
        };

    public static final String[] FILTER_TYPES =
        {
        "Off",
        "2 Pole LP",
        "4 Pole LP",
        "6 Pole LP",
        "2nd Ord HP",
        "4th Ord HP",
        "2nd Ord BP",
        "4th Ord BP",
        "SweptEQ 1 Oct",
        "Swept EQ 2->1 Oct",
        "Swept EQ 3->1 Oct",
        "Phaser 1",
        "Phaser 2",
        "Bat-Phaser",
        "Flanger Lite",
        "Vocal Ah-Ay-Ee",
        "Vocal Oo-Ah",
        "Bottom Feeder"
        };

    public static final String[] VINTAGE_KEYS_FILTER_TYPES =
        {
        "Off",
        "2 Pole LP",
        "4 Pole LP",
        };

    public static final String[] REALTIME_MODULATION_SOURCES =
        {
        "Pitch Wheel",
        "Ctrl A",
        "Ctrl B",
        "Ctrl C",
        "Ctrl D",
        "Mono Press.",
        "Poly Press.",
        "LFO 1",
        "LFO 2",
        "P Alt Vol Env",
        "S Alt Vol Env",
        "Aux Env",
        };
        

    public static final int[] REALTIME_MODULATION_DESTINATIONS_MAP =
        { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
          21, 22, 23, 24, 28, 29, 30, 34, 35, 36, 40, 41, 42 };
                
    public static final int[] REALTIME_MODULATION_DESTINATIONS_INVMAP =
        { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 
          21, 22, 23, 24, 0, 0, 0, 25, 26, 27, 0, 0, 0, 28, 29, 30, 0, 0, 0, 31, 32, 33 };
                
    public static final String[] REALTIME_MODULATION_DESTINATIONS =
        {
        "Off",
        "Pitch",
        "PitchP",
        "PitchS",
        "Volume",
        "VolumeP",
        "VolumeS",
        "Attack",
        "AttackP",
        "AttackS",
        "Decay",
        "DecayP",
        "DecayS",
        "Release",
        "RelP",
        "RelS",
        "XFade",
        "Lfo1Amt",
        "Lfo1Rt",
        "Lfo2Amt",
        "Lfo2Rt",
        "AuxAmt",
        "AuxAtt",
        "AuxDec",
        "AuxRel",
        "Pan",
        "PanP",
        "PanS",
        "FilterFc",
        "PFilterFc",
        "SFilterFc",
        "Port Rate",
        "P Port Rate",
        "S Port Rate"
        };
        
        

 
    public static final String[] PLANET_PHATT_INSTRUMENTS = 
        {
        "None",
        "001. SE Sub 1",
        "002. SE Sub 2",
        "003. SE Sub 3",
        "004. SE Sub 4",
        "005. SE Sub 5",
        "006. SE Sub 6",
        "007. SE Sub 7",
        "008. SE Sub 8",
        "009. SE Sub 9",
        "010. SE Sub 10",
        "011. SE Sub 11",
        "012. Subtle Bass",
        "013. Bass Slap 1",
        "014. Bass Slap 2",
        "015. FingerBass1",
        "016. FingerBass2",
        "017. E P Bass 1",
        "018. EPBass2a",
        "019. EPBass2b",
        "020. UprightBass",
        "021. Fretless 1",
        "022. Fretless 2",
        "023. Fretless 3",
        "024. Fretless 4",
        "025. Street Bass",
        "026. TBazz",
        "027. Dope Bass 1",
        "028. Dope Bass 2",
        "029. DopeBassHit",
        "030. Slider Bass",
        "031. Saw Bass",
        "032. Moog Saw",
        "033. Ultimate 2a",
        "034. Ultimate 2b",
        "035. BigSaw Bass",
        "036. BigMoogSaw1",
        "037. BigMoogSaw2",
        "038. Mini Moog",
        "039. Filter Bass",
        "040. Fat SynBass",
        "041. Jupiter Bass",
        "042. Syn Tone",
        "043. BassBalls1",
        "044. BassBalls2",
        "045. Bas Boy Syn",
        "046. All Purpose",
        "047. Standard",
        "048. Buzz Bass",
        "049. Home Bass",
        "050. Gtr Mutes",
        "051. Sine Wave",
        "052. Saw Wave",
        "053. Synth Axe 1",
        "054. Synth Axe 2",
        "055. Zippy Lead",
        "056. Mini OD2",
        "057. Bell Synth",
        "058. BuzzSynth 1",
        "059. BuzzSynth 2",
        "060. Dance Lead",
        "061. Worm Lead 1",
        "062. Worm Lead 2",
        "063. Worm Lead 3",
        "064. Worm Lead 4",
        "065. Worm Lead 5",
        "066. Worm Lead 6",
        "067. ElectriWorm",
        "068. Electron",
        "069. Tone Organ",
        "070. Disco Organ",
        "071. DX Organ",
        "072. JX Organ",
        "073. Oddd Organ",
        "074. Clavinet",
        "075. Wurlitzer",
        "076. FM EP 1a",
        "077. FM EP 1b",
        "078. Tine EP",
        "079. Rhodes",
        "080. EP Fog",
        "081. EP Roll1 C",
        "082. EP Roll2 F6",
        "083. EP Roll3Bbm",
        "084. HarmonTpt",
        "085. Harmon FX 1",
        "086. Harmon FX 2",
        "087. Bari Wave",
        "088. P5 Brass",
        "089. Spacy Tpt",
        "090. Sax Riff Eb",
        "091. WackTptC#m7",
        "092. Synth Flute",
        "093. Boink",
        "094. Brass Bb",
        "095. Harmonica",
        "096. Gtr Wah Bm",
        "097. Alt Gtr Wah",
        "098. DisTar Pad",
        "099. SynthHiStrg",
        "100. SynthEnsble",
        "101. Synth Vox",
        "102. Jupiter Syn",
        "103. MoodStrings",
        "104. Xylo Pad",
        "105. Jupiter",
        "106. Dreamyy C",
        "107. Phat Pad",
        "108. UnderPad Fm",
        "109. Science",
        "110. MusiCrowd",
        "111. Crowd 2",
        "112. Crowd 2 NTP",
        "113. Dirt 1",
        "114. Dirt NTP",
        "115. Dirt 2",
        "116. Dirt 2 NTP",
        "117. Dirt 3",
        "118. Dirt 3 NTP",
        "119. Oow",
        "120. Soul Oohs",
        "121. Dance Hits",
        "122. FX Hits",
        "123. Gtr Riffs",
        "124. Bass Hits 1",
        "125. Bass Hits 2",
        "126. Brass Hits",
        "127. Vox Hits1",
        "128. Vox Hits 2",
        "129. Scratches",
        "130. ScrtchLoops",
        "131. ScratchBits",
        "132. Kicks",
        "133. Snares",
        "134. Toms",
        "135. Timbales",
        "136. CongasBngos",
        "137. Hats",
        "138. Cymbals",
        "139. Shakers",
        "140. Bells",
        "141. Blocks",
        "142. Tams",
        "143. Claps",
        "144. Snaps",
        "145. Misc Perc",
        "146. DanceHit1 E",
        "147. DanceHit2F#",
        "148. DanceHit3G#",
        "149. DanceHt4C#m",
        "150. DanceHit5C#",
        "151. DanceHit6 G",
        "152. DanceHit7Gm",
        "153. DanceHit8 A",
        "154. DanceHit9 D#",
        "155. DanceHt10Cm",
        "156. FX Hit1",
        "157. FX Hit2",
        "158. FX Hit3",
        "159. FX Hit4",
        "160. FX Hit5",
        "161. FX Hit6",
        "162. FX Hit7",
        "163. GtrHit1",
        "164. GtrHit2",
        "165. GtrHit3",
        "166. GtrHit4 D",
        "167. GtrHit5 Bm",
        "168. GtrHit6 F",
        "169. GtrHit7 E",
        "170. GtrHit8 F#",
        "171. GtrHit9",
        "172. GtrHit10",
        "173. GtrHit11 D#",
        "174. GtrHit12 Am",
        "175. GtrHit13 E",
        "176. GtrHit14 G",
        "177. GtrHit15",
        "178. GtrHit16 D",
        "179. GtrHit17 G",
        "180. GtrHit18 A",
        "181. GtrHit19",
        "182. GtrHit20 D#",
        "183. GtrHit21",
        "184. GtrHit22 F#",
        "185. GtrHit23",
        "186. GtrHit24",
        "187. GtrHit25 A7",
        "188. GtrHit26 D",
        "189. GtrHit27",
        "190. GtrHit28 A",
        "191. GtrHit29 F7",
        "192. GtrHit30A#m",
        "193. Bass Hit1",
        "194. Bass Hit2",
        "195. Bass Hit3",
        "196. Bass Hit4",
        "197. BassHit5",
        "198. Tpt FX 1",
        "199. Tpt FX 2",
        "200. Tpt FX 3",
        "201. Tpt FX 4",
        "202. Tpt FX 5",
        "203. Sax FX 1",
        "204. Sax FX 2",
        "205. Brs Hit1Bbm",
        "206. Brs Hit2",
        "207. Brs Hit3",
        "208. Brs Hit4",
        "209. Brs Hit5",
        "210. Brs Hit6",
        "211. Brs Hit7",
        "212. Brs Hit8",
        "213. Brs Hit9",
        "214. Brs Ht10 E7",
        "215. Brs Hit11 B",
        "216. Brs Ht12 G#",
        "217. Brs Ht13A#m",
        "218. Brs Hit14",
        "219. Brs Ht15 G#",
        "220. Brs Ht16 G#",
        "221. Brs Hit17 D",
        "222. Brs Hit18 A",
        "223. Vox Hit 1",
        "224. Vox Hit 2",
        "225. Vox Hit 3",
        "226. Vox Hit 4",
        "227. Vox Hit 5",
        "228. Vox Hit 6",
        "229. Scratch 1",
        "230. Scratch 2",
        "231. Scratch 3",
        "232. Scratch 4",
        "233. Scratch 5",
        "234. Scratch 6",
        "235. Scratch 7",
        "236. Scratch 8",
        "237. Scratch 9",
        "238. Scratch 10",
        "239. Scratch 11",
        "240. Scratch 12",
        "241. Scratch 13",
        "242. Scratch 14",
/// START OF BANK 2
        "243. Scratch 15",
        "244. Scratch 16",
        "245. Scratch 17",
        "246. Kick 1",
        "247. Kick 2",
        "248. Kick 3",
        "249. Kick 4",
        "250. Kick 5",
        "251. Kick 6",
        "252. Kick 7",
        "253. Kick 8",
        "254. Kick 9",
        "255. Kick 10",
        "256. Kick 11",
        "257. Kick 12",
        "258. Kick 13",
        "259. Kick 14",
        "260. Kick 15",
        "261. Kick 16",
        "262. Kick 17",
        "263. Kick 18",
        "264. Kick 19",
        "265. Kick 20",
        "266. Kick 21",
        "267. Kick 22",
        "268. Kick 23",
        "269. Kick 24",
        "270. Kick 25",
        "271. Kick 26",
        "272. Kick 27",
        "273. Kick 28",
        "274. Kick 29",
        "275. Kick 30",
        "276. Snare 1",
        "277. Snare 2",
        "278. Snare 3",
        "279. Snare 4",
        "280. Snare 5",
        "281. Snare 6",
        "282. Snare 7",
        "283. Snare 8",
        "284. Snare 9",
        "285. Snare 10",
        "286. Snare 11",
        "287. Snare 12",
        "288. Snare 13",
        "289. Snare 14",
        "290. Snare 15",
        "291. Snare 16",
        "292. Snare 17",
        "293. Snare 18",
        "294. Snare 19",
        "295. Snare 20",
        "296. Snare 21",
        "297. Snare 22",
        "298. Snare 23",
        "299. Snare 24",
        "300. Snare 25",
        "301. Snare 26",
        "302. Snare 27",
        "303. Snare 28",
        "304. Snare 29",
        "305. Snare 30",
        "306. Snare 31",
        "307. Snare 32",
        "308. Snare 33",
        "309. Snare 33b",
        "310. Snare 34",
        "311. Snare 35",
        "312. Snare 36",
        "313. Snare 37",
        "314. Snare 38",
        "315. Snare 39",
        "316. Snare 40",
        "317. Snare 41",
        "318. Snare 42",
        "319. Snare 43",
        "320. Snare 44",
        "321. Snare 45",
        "322. Snare 46",
        "323. Snare 47",
        "324. Snare 48",
        "325. Snare 49",
        "326. Snare 50",
        "327. Snare 51",
        "328. Snare 52",
        "329. Snare 53",
        "330. Snare 54",
        "331. Snare 55",
        "332. Snare 56",
        "333. Snare 57",
        "334. Snare 58",
        "335. Snare 59",
        "336. Snare 60",
        "337. Snare 61",
        "338. Tom 1",
        "339. Tom 2",
        "340. Tom 3",
        "341. Tom 4",
        "342. Tom 5",
        "343. Tom 6",
        "344. Tom 7",
        "345. Tom 8",
        "346. Tom 9",
        "347. Timbale 1",
        "348. Timbale 2",
        "349. Conga 1",
        "350. Conga 2",
        "351. Conga 3",
        "352. Conga 4",
        "353. Conga 5",
        "354. Conga 6",
        "355. Conga 7",
        "356. Bongo 1",
        "357. Hat 1",
        "358. Hat 2",
        "359. Hat 3",
        "360. Hat 4",
        "361. Hat 5",
        "362. Hat 6",
        "363. Hat 7",
        "364. Hat 8",
        "365. Hat 9",
        "366. Hat 10",
        "367. Hat 11",
        "368. Hat 12",
        "369. Hat 13",
        "370. Hat 14",
        "371. Hat 15",
        "372. Hat 16",
        "373. Hat 17",
        "374. Hat 18",
        "375. Hat 19",
        "376. Hat 20",
        "377. Hat 21",
        "378. Hat 22",
        "379. Hat 23",
        "380. Hat 24",
        "381. Hat 25",
        "382. Hat 26",
        "383. Hat 27",
        "384. Hat 28",
        "385. Hat 29",
        "386. Hat 30",
        "387. Hat 31",
        "388. Hat 32",
        "389. Hat 33",
        "390. Cymbal 1",
        "391. Cymbal 2",
        "392. Cymbal 3",
        "393. Cymbal 4",
        "394. Cymbal 5",
        "395. Cymbal 6",
        "396. Cymbal 7",
        "397. Cymbal 8",
        "398. Cymbal 9",
        "399. Cymbal 10",
        "400. Cymbal 11",
        "401. Cymbal 12",
        "402. Shaker 1",
        "403. Shaker 2",
        "404. Shaker 3",
        "405. Shaker 4",
        "406. Shaker 5",
        "407. Shaker 6",
        "408. Shaker 7",
        "409. Shaker 8",
        "410. Shaker 9",
        "411. Shaker 10",
        "412. Shaker 11",
        "413. Shaker 12",
        "414. Shaker 13",
        "415. Shaker 14",
        "416. Shaker 15",
        "417. Shaker 16",
        "418. Shaker 17",
        "419. Bell 1",
        "420. Bell 2",
        "421. Bell 3",
        "422. Bell 4",
        "423. Bell 5",
        "424. Bell 6",
        "425. Bell 7",
        "426. Bell 8",
        "427. Bell 9",
        "428. Bell 10",
        "429. Bell 11",
        "430. Bell 12",
        "431. Bell 13",
        "432. Block 1",
        "433. Block 2",
        "434. Block 3",
        "435. Block 4",
        "436. Block 5",
        "437. Block 6",
        "438. Tam 1",
        "439. Tam 2",
        "440. Tam 3",
        "441. Tam 4",
        "442. Tam 5",
        "443. Clap 1",
        "444. Clap 2",
        "445. Clap 3",
        "446. Clap 4",
        "447. Clap 5",
        "448. Clap 6",
        "449. Clap 7",
        "450. Clap 8",
        "451. Clap 9",
        "452. Clap 10",
        "453. Clap 11",
        "454. Clap 12",
        "455. Scraa",
        "456. Snap 1",
        "457. Snap 2",
        "458. Snap 3",
        "459. Snap 4",
        "460. Snap 5",
        "461. Snap 6",
        "462. Misc Perc 1",
        "463. Misc Perc 2",
        "464. Misc Perc 3",
        "465. Misc Perc 4",
        "466. Misc Perc 5",
        "467. Misc Perc 6",
        "468. Misc Perc 7",
        "469. Misc Perc 8",
        "470. Misc Perc 9",
        "471. Kit 1",
        "472. Kit 2",
        "473. Kit 3",
        "474. Kit 4",
        "475. Kit 5",
        "476. Kit 6",
        "477. Kit 7",
        "478. Kit 8",
        "479. Kit 9",
        "480. Kit 10",
        "481. Rom Play"
        };

    public static final String[] ORBIT_V2_INSTRUMENTS = 
        {
        "None",
        "001. SuperSub",
        "002. BelowSub",
        "003. BassHum",
        "004. BassLowness",
        "005. BassSonics",
        "006. Bassssic",
        "007. SubBass1",
        "008. SubBass2",
        "009. SubBass3",
        "010. JunoSub",
        "011. SubBass4",
        "012. SubBass5",
        "013. LoSnthBass1",
        "014. LoSnthBass2",
        "015. Moog Tri",
        "016. AnalowBass",
        "017. BassEnd",
        "018. SynthBass3",
        "019. SynthBass4",
        "020. SynthBass5",
        "021. SynthBass6",
        "022. QBass",
        "023. PPGBass",
        "024. SynthBass7",
        "025. SynthBass8",
        "026. SynthBass9",
        "027. SynthBass10",
        "028. SynthBass11",
        "029. SynthBass12",
        "030. SynthBass13",
        "031. SynthBass14",
        "032. SynthBass15",
        "033. SynthBass16",
        "034. TB3031",
        "035. TB3032",
        "036. TB3033",
        "037. TB3034",
        "038. Bass2600",
        "039. CZ101Bass",
        "040. DXBass1",
        "041. DXBass2",
        "042. DXBass3",
        "043. JP4Bass",
        "044. MoogBass",
        "045. DB9Bass1",
        "046. DB9Bass2",
        "047. AnalogBass",
        "048. OrganBass",
        "049. TapBass",
        "050. Bass1",
        "051. Bass2",
        "052. FatSunbass",
        "053. Bass3",
        "054. UprightBass",
        "055. Perco",
        "056. Bass4",
        "057. MemMoogBass",
        "058. BassHit1",
        "059. BassHit2",
        "060. Syn Tone 1",
        "061. Syn Tone 2",
        "062. SynthBass17",
        "063. SynthBass18",
        "064. Micro Moog",
        "065. FunkBass",
        "066. CZ101 Digi",
        "067. Farfisa",
        "068. FarfisaLow",
        "069. Vox Org Low",
        "070. CZSynstring",
        "071. SynthCheeze",
        "072. SynthCheezH",
        "073. LeadSynth2",
        "074. Syn Tone 3",
        "075. Uroborus",
        "076. Hollow Deep",
        "077. PureH20",
        "078. Echo Synth",
        "079. SynthLead1",
        "080. SynthLead2",
        "081. SynthLead3",
        "082. SynthLd3Wkd",
        "083. AnotherLead",
        "084. DanceBlip",
        "085. Rast",
        "086. SaxWave",
        "087. P5Brass",
        "088. M12Lead",
        "089. Arp1",
        "090. Arp2",
        "091. Synth Gtr 1",
        "092. Synth Gtr 2",
        "093. Synth Gtr 3",
        "094. Whine",
        "095. DanceSynth1",
        "096. DanceSynth2",
        "097. SineWave",
        "098. CZSaw",
        "099. SawnicTooth",
        "100. JunoSaw",
        "101. OBXSaws",
        "102. SquareLead1",
        "103. SquareLead2",
        "104. SqrAttkLead",
        "105. JunoSquare",
        "106. SquareChrs",
        "107. CZsquare",
        "108. JunoPulse",
        "109. Sync Wave 1",
        "110. SyncWave1b",
        "111. Sync Wave 2",
        "112. Sync Wave 3",
        "113. Sync Wave 4",
        "114. Sync Wave 5",
        "115. Spacey Key",
        "116. Log Hit",
        "117. Organ1",
        "118. Organ2",
        "119. BassOrgan",
        "120. BreathyOrgn",
        "121. PipeOrgan",
        "122. Organ3",
        "123. Organ4",
        "124. Org Day",
        "125. Org Nod",
        "126. Tone Org",
        "127. Org Lite",
        "128. PianoWave",
        "129. DanceSynth3",
        "130. HiOct Synth",
        "131. JP6Pad",
        "132. RezzyWave",
        "133. RezSynth",
        "134. SynthBrass",
        "135. Brazz",
        "136. DanceSynth4",
        "137. DanceSynth5",
        "138. SynthPad",
        "139. PadLife",
        "140. PadClassic",
        "141. DreamPad1",
        "142. Paddy",
        "143. DreamPad2",
        "144. SoundTrack",
        "145. Zoom",
        "146. Heavy",
        "147. Keyngdom",
        "148. DanceChord",
        "149. CW Type",
        "150. LawnMower",
        "151. RiffTrip",
        "152. CyberPan",
        "153. GrooveThing",
        "154. SciFi",
        "155. SynthSiren",
        "156. MetalNoise",
        "157. CMIBreath",
        "158. Breathy",
        "159. VoxTarzana",
        "160. Vox Gothic",
        "161. Slow Goth",
        "162. Vox Synth 1",
        "163. Vox Synth 2",
        "164. JaxBreath",
        "165. Crowd",
        "166. CrowdNTP",
        "167. Seq Delay",
        "168. CMI Hot Air",
        "169. DanceHits",
        "170. StringHits",
        "171. HornHits",
        "172. StrHitBbmin",
        "173. TechnoHitAm",
        "174. StrHitAmin",
        "175. OrkHitCmin7",
        "176. StringHitD",
        "177. DanceStabC7",
        "178. BrassHitAbM",
        "179. HouseStabBm",
        "180. Classic7x9",
        "181. BigHitAugb9",
        "182. DanceStabb9",
        "183. ScreamingDM",
        "184. HitMeCsus7",
        "185. ClusterGbm9",
        "186. Honk Hit C",
        "187. PurpleDbm7",
        "188. LaserHitC",
        "189. OrganHitAm7",
        "190. SyntHitDbM7",
        "191. WarmHitEm",
        "192. HouseHitAm",
        "193. DanzChrdCm7",
        "194. Dance Hit G",
        "195. PurpleAmin",
        "196. CarHornHit",
        "197. DiscoHorn",
        "198. QuackAhhh",
        "199. BizarreGbm7",
        "200. HowsHornDm7",
        "201. Short Hit C",
        "202. OrgChordDm7",
        "203. OrgStabDm7",
        "204. OrgClassicD",
        "205. KleanHitEm",
        "206. KlangHitF7",
        "207. MetalHit",
        "208. GameHit",
        "209. BuzzBlip",
        "210. BlampHitD",
        "211. ShortQuack",
        "212. SpaceWhip",
        "213. SpaceWiggle",
        "214. Sqweel Rev",
        "215. Comon Vox",
        "216. Odd Vox Am",
        "217. DrumStall",
        "218. WindDown",
        "219. Wind Down 2",
        "220. Hip Hop Hit",
        "221. DissHit",
        "222. DrumStab",
        "223. Key FX",
        "224. L9000 Noise",
        "225. PinkNoise",
        "226. PinkNTP",
        "227. WhiteNoise",
        "228. WhiteNTP",
        "229. StringsDark",
        "230. HornsDark",
        "231. ShortHits",
        "232. ShortStrgs",
        "233. DarkDance",
        "234. DrkrStrings",
        "235. DrkrHorns",
        "236. DrkShrtHits",
/// START OF BANK 2
        "237. Scratches",
        "238. Kicks",
        "239. Snares",
        "240. Toms",
        "241. Timbales",
        "242. Congas etc",
        "243. Hats",
        "244. Cymbalsetc",
        "245. Claps",
        "246. Tambourine",
        "247. Clave",
        "248. Cowbell",
        "249. Maracas",
        "250. Agogos",
        "251. Vibraslap",
        "252. Guiro",
        "253. Blocks",
        "254. Bells",
        "255. MiscPerc",
        "256. Dance Kit 1",
        "257. Dance Kit 2",
        "258. BeatsGMStnd",
        "259. BeatsFlava",
        "260. BeatsHipHop",
        "261. BeatsHpHp2",
        "262. BeatsHpHp3",
        "263. BeatsJungle",
        "264. BeatsJngl2",
        "265. BeatsJngl3",
        "266. BeatsHrdcre",
        "267. BeatsHrdcr2",
        "268. BeatsHrdcr3",
        "269. BeatsHouse",
        "270. BeatsHouse2",
        "271. BeatsHouse3",
        "272. More Kits 1",
        "273. More Kits 2",
        "274. More Kits 3",
        "275. More Kits 4",
        "276. BeatsHrdFlr",
        "277. FunScratch",
        "278. MCScratch",
        "279. FastScratch",
        "280. SSSSystem1",
        "281. SSSSystem2",
        "282. ScratchHere",
        "283. ScratchOnIt",
        "284. ScratchDiss",
        "285. BowWow",
        "286. DivaScratch",
        "287. OrganBlippr",
        "288. ClassicTape",
        "289. ScratchOut!",
        "290. Chiffin",
        "291. BowserBark",
        "292. Stalled",
        "293. PunchIt",
        "294. Vinyl",
        "295. VinylNTP",
        "296. Kick#1",
        "297. Kick#2",
        "398. Kick#3",
        "399. Kick#4",
        "300. Kick#5",
        "301. Kick#6",
        "302 Kick#7",
        "303. Kick#8",
        "304. Kick#9",
        "305. Kick#10",
        "306. Kick#11",
        "307. Kick#12",
        "308. Kick#13",
        "309. Kick#14",
        "310. Kick#15",
        "311. Kick#16",
        "312. Kick#17",
        "313. Kick#18",
        "314. Kick#19",
        "315. Kick#20",
        "316. Kick#21",
        "317. Kick#22",
        "318. Kick#23",
        "319. Kick#24",
        "320. Kick#25",
        "321. Kick#26",
        "322. Kick#27",
        "323. Snare#1",
        "324. Snare#2",
        "325. Snare#3",
        "326. Snare#4",
        "327. Snare#5",
        "328. Snare#6",
        "329. Snare#7",
        "330. Snare#8",
        "331. Snare#9",
        "332. Snare#10",
        "333. Snare#11",
        "334. Snare#12",
        "335. Snare#13",
        "336. Snare#14",
        "337. Snare#15",
        "338. Snare#16",
        "339. Snare#17",
        "340. Snare#18",
        "341. Snare#19",
        "342. Snare#20",
        "343. Snare#21",
        "344. Snare#22",
        "345. Snare#23",
        "346. Snare#24",
        "347. Snare#25",
        "348. Snare#26",
        "349. Snare#27",
        "350. Snare#28",
        "351. Snare#29",
        "352. Snare#30",
        "353. Snare#31",
        "354. Snare#32",
        "355. Snare#33",
        "356. Snare#34",
        "357. Snare#35",
        "358. Snare#36",
        "359. Snare#37",
        "360. Snare#38",
        "361. Snare#39",
        "362. Snare#40",
        "363. Snare#41",
        "364. Snare#42",
        "365. Snare#43",
        "366. Tom #1",
        "367. Tom #2",
        "368. Tom #3",
        "369. Tom #4",
        "370. Tom #5",
        "371. Tom #6",
        "372. Tom #7",
        "373. Tom #8",
        "374. Tom #9",
        "375. Tom #10",
        "376. Tom #11",
        "377. Tom #12",
        "378. Tom #13",
        "379. Tom #14",
        "380. Tom #15",
        "381. Tom #16",
        "382. Tom #17",
        "383. SnareLoops",
        "384. Conga Loops"
        };
        
    public static final String[] ORBIT_V1_INSTRUMENTS = ORBIT_V2_INSTRUMENTS;

    public static final String[] CARNAVAL_INSTRUMENTS = 
        {
        "None",
        "001. Accordion1",
        "002. Accordion2",
        "003. AcousticBs1",
        "004. AcousticBs2",
        "005. BabyBassMag",
        "006. BabyBassBri",
        "007. ElectBass",
        "008. ElecDampBs",
        "009. Slapbass",
        "010. Synbass1",
        "011. Synbass2",
        "012. Synbass3",
        "013. Synbass4",
        "014. Synbass5",
        "015. Synbass6",
        "016. Subbass1",
        "017. Subbass2",
        "018. Qupbass1",
        "019. Qupbass2",
        "020. BrassHitz",
        "021. BrassSFX",
        "022. SynthBrass",
        "023. SoftTrumpet",
        "024. MedTrumpet",
        "025. HardTrumpet",
        "026. Trombone",
        "027. Tuba",
        "028. AltoSax",
        "029. BaritoneSax",
        "030. BariStaccat",
        "031. TenorJaz",
        "032. TenorSax",
        "033. FluteVib",
        "034. PanFlute",
        "035. AcousticStl",
        "036. FlamencoGtr",
        "037. StratGtr",
        "038. Violin",
        "039. VoiceCutz1",
        "040. VoiceCutz2",
        "041. Piano",
        "042. Organ1",
        "043. Organ2",
        "044. Organ3",
        "045. Farfisa",
        "046. Vox",
        "047. SpongyOrgan",
        "048. Organ4O",
        "049. OrganB31",
        "050. OrganB32",
        "051. OrganB33",
        "052. OrganB3Up",
        "053. OrganWave1",
        "054. Pulse50",
        "055. Sawtooth",
        "056. Triangle",
        "057. SineWave",
        "058. Square",
        "059. BrassyWave",
        "060. Silkveil",
        "061. kit:Traps",
        "062. kit:Salsa",
        "063. kit:Brazil",
        "064. kit:GMIDIsh",
        "065. kit:GMBraz",
        "066. prf:Congas",
        "067. prf:Bongos",
        "068. prf:Timbale",
        "069. prf:Guiro",
        "070. prf:Shekere",
        "071. prf:Tambora",
        "072. prf:Agogos",
        "073. prf:Tmborim",
        "074. prf:Surdo",
        "075. prf:Pndeiro",
        "076. prf:Repique",
        "077. prf:Ganza",
        "078. raw:Quinto",
        "079. raw:Conga",
        "080. raw:Congas",
        "081. raw:Bongos",
        "082. raw:Timbale",
        "083. raw:Bells",
        "084. raw:Claves",
        "085. raw:Guiro",
        "086. raw:Shekere",
        "087. raw:Tambora",
        "088. raw:Agogos",
        "089. raw:Tmborim",
        "090. raw:Surdo",
        "091. raw:Pndeiro",
        "092. raw:Repique",
        "093. raw:Ganza",
        "094. raw:Triangl",
        "095. raw:Kicks",
        "096. raw:Snares",
        "097. raw:Toms",
        "098. raw:Hats",
        "099. raw:Cymbals",
        "100. raw:Tambs",
        "101. raw:Shakers",
        "102. QuintoHeel",
        "103. QuintoTip",
        "104. QuintoSlap",
        "105. QuintoOpen",
        "106. QuintoPalm",
        "107. QuintoSolo",
        "108. CongaHeel",
        "109. CongaTip",
        "110. CongaSlap",
        "111. CongaOpen",
        "112. CongaPalm",
        "113. CongaSolo",
        "114. CongaSlide",
        "115. TumbaOpen",
        "116. BongoRim1R",
        "117. BongoFingL",
        "118. BongoRim2R",
        "119. BongoThumbL",
        "120. BongoLrgOpn",
        "121. BongoFngSlp",
        "122. BongoSolo",
        "123. TimbHandCls",
        "124. TimbHandOpn",
        "125. TimbLrgOpn",
        "126. TimbLrgRim",
        "127. TimbSmOpn",
        "128. TimbSmRim",
        "129. TimbCascara",
        "130. MamboBell",
        "131. ChaBellOpn",
        "132. ChaBellCls",
        "133. CharangaOp",
        "134. CharangaCl",
        "135. HandBellOp",
        "136. HandBellCl",
        "137. Woodblock",
        "138. CubanClave",
        "139. SonClave",
        "140. Maracas",
        "141. GuiroUp",
        "142. GuiroDown",
        "143. ShekereDown",
        "144. ShekereSlap",
        "145. ShekereUp",
        "146. TamboraRim",
        "147. TamboraOpn",
        "148. TamboraSlap",
        "149. TamboraHand",
        "150. AgogoMute",
        "151. AgogoLow",
        "152. AgogoHigh",
        "153. TamborimTch",
        "154. TamborimMid",
        "155. TamborimEdg",
        "156. SurdoTouch",
        "157. SurdoMute",
        "158. SurdoRim",
        "159. SurdoRimCl",
        "160. SurdoOpen",
        "161. PandeiroSlp",
        "162. PandeiroDmp",
        "163. PandeiroOpn",
        "164. PandeiroStr",
        "165. PandShake1",
        "166. PandShake2",
        "167. RepiqueDrag",
        "168. RepiqueTone",
        "169. RepiqueRimL",
        "170. RepiqueRimH",
        "171. RepiqueSlap",
        "172. Quica",
        "173. Caxixi",
        "174. SambaWhstle",
        "175. GanzaLong",
        "176. GanzaShort",
        "177. TriangleMte",
        "178. TriangleOpn",
        "179. Kick1",
        "180. Kick2",
        "181. Kick3",
        "182. Kick4",
        "183. Kick5",
        "184. Kick6",
        "185. Kick7",
        "186. Kick8",
        "187. Kick9",
        "188. Snare1",
        "189. Snare2",
        "190. Snare3",
        "191. Snare4",
        "192. Snare5",
        "193. Snare6",
        "194. Snare7",
        "195. Snare8",
        "196. SnareRimCl",
        "197. SnareRuff",
        "198. SnareRoll",
        "199. SnareBrush",
        "200. TomLow",
        "201. TomMed",
        "202. TomHigh",
        "203. TomSynth1",
        "204. TomSynth2",
        "205. HatClosed",
        "206. Hat1/3Op",
        "207. Hat2/3Op",
        "208. HatOpen",
        "209. HatStomp",
        "210. HatSynth",
        "211. RideBell",
        "212. RideCymbal",
        "213. Crash14\"",
        "214. Crash17\"",
        "215. CymbalSynth",
        "216. Tambourine1",
        "217. Tambourine2",
        "218. TambSynth",
        "219. Cowbell1",
        "220. Cowbell2",
        "221. Castanets",
        "222. Fingersnap",
        "223. Claps",
/// START OF BANK 2
        "224. BrassHit1",
        "225. BrassHit2",
        "226. BrassHit3",
        "227. BrassHit4",
        "228. BrassHit5",
        "229. BrassHit6",
        "230. BrassHit7",
        "231. TrpFX1",
        "232. TrpFX2",
        "233. TrpFX3",
        "234. Carnaval",
        "235. Cumbia",
        "236. AGozar",
        "237. Laughing",
        "238. QueRico",
        "239. ABailar",
        "240. Merengue",
        "241. ChaCuCha",
        "242. Eh",
        "243. Como",
        "244. Loca",
        "245. Roar",
        "246. RRRRR",
        "247. Shout",
        "248. Chant",
        "249. AhoraSi",
        "250. Salsa",
        "251. Vaya",
        "252. bt:Banda",
        "253. bt:Tejano",
        "254. bt:Cumbia",
        "255. bt:Salsa1",
        "256. bt:Salsa2",
        "257. bt:Salsa3",
        "258. bt:Salsa4",
        "259. bt:Salsa5",
        "260. bt:Salsa6",
        "261. bt:Salsa7",
        "262. bt:Mrngue1",
        "263. bt:Mrngue2",
        "264. bt:Songo",
        "265. bt:Brazil1",
        "266. bt:Brazil2",
        "267. bt:Techno1",
        "268. bt:Techno2",
        "269. bt:MacaTek",
        "270. bt:Weirdo1",
        "271. bt:Weirdo2",
        "272. bt:Klango1",
        "273. bt:Klango2",
        "274. bt:Klango3",
        "275. bt:Groove1",
        "276. bt:Groove2",
        "277. bt:Groove3",
        "278. bt:Groove4",
        "279. bt:Groove5",
        "280. bt:Groove6",
        "281. bt:Groove7",
        "282. bt:Groove8",
        "283. bt:Groove9",
        "284. bt:Folklore",
        "285. bt:Songo2",
        "286. kat:Kit1",
        "287. kat:Kit2",
        "288. kat:Kit3",
        "289. kat:Kit4",
        "290. kat:Kit5",
        "291. kat:Kit6",
        "292. kat:Kit7",
        "293. kat:Kit8",
        "294. kat:Kit9",
        "295. kat:Kit0",
        "296. kat:congas",
        "297. kat:timbale",
        };
        
    public static final String[] VINTAGE_KEYS_INSTRUMENTS =
        {
        "None",
        "001. B3DistLwSlw",
        "002. B3 Dist Fast",
        "003. B3 Hi Slow",
        "004. B3 Hi Fast",
        "005. B3 Full Slow",
        "006. B3 Full Fast",
        "007. B3 Perc 3rd",
        "008. Male Choir",
        "009. Femme Choir",
        "010. Males to Mix",
        "011. Femmes to Mix",
        "012. Violin Trio",
        "013. Flute",
        "014. Bari Sax",
        "015. Tenor Sax",
        "016. Alto Sax",
        "017. Trumpet Sft",
        "018. Trumpet Sft",
        "019. Trombone",
        "020. Trom/STpt",
        "021. Trom/HTpt",
        "022. Trom/Sax",
        "023. Farfisa",
        "024. CP-70",
        "025. CP-70 Mellow",
        "026. CP-70 Brite",
        "027. Piano Bass",
        "028. Wurlitzer1",
        "029. Wurlitzer2",
        "030. Dyno Rhodes1",
        "031. Dyno Rhodes2",
        "032. FendrRhodes",
        "033. Clavinet",
        "034. Brite Clavinet",
        "035. Mini Moog 1",
        "036. Mini Moog 2",
        "037. Mini Moog 3",
        "038. Mini Moog 4",
        "039. Mini Moog 5",
        "040. Mini Moog 6",
        "041. Micro Moog",
        "042. TaurusPedal",
        "043. JacoBass",
        "044. M12 Lead 1",
        "045. M12 Lead 2",
        "046. ARP 2600",
        "047. Rock DXman",
        "048. P5 Sync Lead",
        "049. Moog 55 Rez",
        "050. Memory Moog",
        "051. OBX Saws",
        "052. P5 Guitar Pad",
        "053. Matrix Pad",
        "054. P5 Piper",
        "055. P5 Strings",
        "056. M12 Strings",
        "057. ARP Strings",
        "058. AHHs",
        "059. Picked Bass",
        "060. Finger Bass",
        "061. Finger Tre",
        "062. Pickd 8 Top",
        "063. Finger 8 Top",
        "064. Finger 8 Tre",
        "065. The Guitar",
        "066. Twelve Top 1",
        "067. Twelve Top 2",
        "068. Twelve Top 3",
        "069. Drum Kit 1",
        "070. Drum Kit 2",
        "071. Drum Kit 3",
        "072. Drum Kit 4",
        "073. Drum Kit 5",
        "074. Drum Kit 6",
        "075. Drum Kit 7",
        "076. Large Hall",
        "077. Medium Room",
        "078. Tiled Room",
        "079. Gated Short",
        "080. Gated Long 1",
        "081. Gated Long 2",
        "082. Lush Reverb",
        "083. Blush Reverb",
        "084. Full On Reverb",
        "085. Dry Kick",
        "086. KickVerb",
        "087. Dry Snare",
        "088. Snare Verb",
        "089. Dry Tom",
        "090. Wet Tom",
        "091. High Hat 1",
        "092. High Hat 2",
        "093. High Hat 3",
        "094. Ride Ping",
        "095. Ride Bell",
        "096. Crash",
        "097. Cowbell",
        "098. Tambourine",
        "099. Side Stick",
        "100. Clave",
        "101. Square",
        "102. Sawtooth",
        "103. Triangle",
        "104. Moog Saw 1",
        "105. Moog Saw 2",
        "106. Moog Saw 3",
        "107. Moog Saw 4",
        "108. Moog Square 1",
        "109. Moog Square 2",
        "110. Moog Square 3",
        "111. Moog Square 4",
        "112. Moog Square 5",
        "113. Moog Square 6",
        "114. Moog Rectangle 1",
        "115. Moog Rectangle 2",
        "116. Moog Rectangle 3",
        "117. Moog Rectangle 4",
        "118. Moog Rectangle 5",
        "119. Moog Pulse 1",
        "120. Moog Pulse 2",
        "121. Moog Pulse 3",
        "122. Moog Pulse 4",
        "123. Moog Pulse 5",
        "124. OB Wave 1",
        "125. OB Wave 2",
        "126. OB Wave 3",
        "127. OB Wave 4",
        "128. OB Wave 5",
        "129. ARP 2600 1",
        "130. ARP 2600 2",
        "131. ARP 2600 3",
        "132. B3 Wave 1",
        "133. B3 Wave 2",
        "134. B3 Wave 3",
        "135. B3 Wave 4",
        "136. B3 Wave 5",
        "137. B3 Wave 6",
        "138. B3 Wave 7",
        "139. B3 Wave 8",
        "140. B3 Wave 9",
        "141. B3 Wave 10",
        "142. B3 Wave 11",
        "143. B3 Wave 12",
        "144. B3 Wave 13",
        "145. B3 Wave 14",
        "146. B3 Wave 15",
        "147. B3 Wave 16",
        "148. B3 Wave 17",
        "149. B3 Wave 18",
        "150. ARP Clarinet",
        "151. ARP Bassoon",
        "152. P5 No-Tone",
        "153. Noise Non-X",
        "154. Oct 1 (Sine)",
        "155. Oct 2 All",
        "156. Oct 3 All",
        "157. Oct 4 All",
        "158. Oct 5 All",
        "159. Oct 6 All",
        "160. Oct 7 All",
        "161. Oct 2 Odd",
        "162. Oct 3 Odd",
        "163. Oct 4 Odd",
        "164. Oct 5 Odd",
        "165. Oct 6 Odd",
        "166. Oct 7 Odd",
        "167. Oct 2 Even",
        "168. Oct 3 Even",
        "169. Oct 4 Even",
        "170. Oct 5 Even",
        "171. Oct 6 Even",
        "172. Oct 7 Even",
        "173. Low Odds",
        "174. Low Evens",
        "175. Four Octaves",
        "176. Synth Cycle 1",
        "177. Synth Cycle 2",
        "178. Synth Cycle 3",
        "179. Synth Cycle 4",
        "180. Fundamental Gone 1",
        "181. Fundamental Gone 2",
        "182. Bite Cycle",
        "183. Buzzy Cycle",
        "184. Metalphone",
        "185. Metalphone",
        "186. Metalphone",
        "187. Metalphone",
        "188. Duck Cycle 1",
        "189. Duck Cycle 2",
        "190. Duck Cycle 3",
        "191. Wind Cycle 1",
        "192. Wind Cycle 2",
        "193. Wind Cycle 3",
        "194. Wind Cycle 4",
        "195. Organ Cycle 1",
        "196. Organ Cycle 2",
        "197. Violin Essence",
        "198. Buzzoon",
        "199. Brassy Wave",
        "200. Reedy Buzz",
        "201. Growl Wave",
        "202. HarpsiWave",
        "203. Fuzzy Gruzz",
        "204. Power 5ths",
        "205. Filter Saw",
        "206. Ramp",
        "207. Evens Only",
        "208. Odds Gone",
        "209. Ice Bell",
        "210. Bronze Age",
        "211. Iron Plate",
        "212. Aluminum",
        "213. Lead beam",
        "214. Steel Xtract",
        "215. Winter Glass",
        "216. Town bell",
        "217. Orch Bells",
        "218. Tubular SE",
        "219. Soft Bell",
        "220. Swirly",
        "221. Tack Attack",
        "222. Shimmer Wave",
        "223. Mild Tone",
        "224. Ah Wave",
        "225. Vocal Wave",
        "226. Fuzzy Clav",
        "227. Electrhode",
        "228. Whine 1",
        "229. Filter Bass",
        "230. Harmonics",
        "231. ElecPiano",
        "232. Marimba Attack",
        "233. Vibe Attack",
        "234. Xposed Noise",
        "235. Marimba Loop",
        "236. Vibes Loop",
        "237. Stick Loop",
        "238. Cowbell Loop",
        "239. Clave Loop",
        "240. Hi Hat Loop",
        "241. Drum Pile",
        "242. CP-70 Pile",
        "243. Pick Bass Pile",
        "244. Guitar Pile",
        "245. Finger Bass Pile",
        "246. Moog Loop",
        "247. OB Loop",
        "248. Frost Loop",
        "249. Memory Loop"
        };



    public static final String[] VINTAGE_KEYS_PLUS_INSTRUMENTS =
        {
        "None",
        "001. B3DistLwSlw",
        "002. B3DistFast",
        "003. B3 Hi Slow",
        "004. B3 Hi Fast",
        "005. B3FullSlow",
        "006. B3FullFast",
        "007. B3 Perc 3rd",
        "008. Male Choir",
        "009. Femme Choir",
        "010. MalestoMix",
        "011. FemmestoMix",
        "012. Violin Trio",
        "013. Flute",
        "014. Bari Sax",
        "015. Tenor Sax",
        "016. Alto Sax",
        "017. Trumpet Sft",
        "018. Trumpet Hrd",
        "019. Trombone",
        "020. Trom / STpt",
        "021. Trom / HTpt",
        "022. Trom / Sax",
        "023. Farfisa",
        "024. CP-70",
        "025. CP-70Mello",
        "026. CP-70 Brite",
        "027. Piano Bass",
        "028. Wurlitzer1",
        "029. Wurlitzer2",
        "030. DynoRhodes1",
        "031. DynoRhodes2",
        "032. FendrRhodes",
        "033. Clavinet",
        "034. Brite Clav",
        "035. Mini Moog 1",
        "036. Mini Moog 2",
        "037. Mini Moog 3",
        "038. Mini Moog 4",
        "039. Mini Moog 5",
        "040. Mini Moog 6",
        "041. Micro Moog",
        "042. TaurusPedal",
        "043. Jaco Bass",
        "044. M12 Lead 1",
        "045. M12 Lead 2",
        "046. ARP 2600",
        "047. RockDXman",
        "048. P5 SyncLead",
        "049. Moog 55 Rez",
        "050. Memory Moog",
        "051. OBX Saws",
        "052. P5GuitarPad",
        "053. Matrix Pad",
        "054. P5 Piper",
        "055. P5 Strings",
        "056. M12 Strings",
        "057. ARP Strings",
        "058. AAAHs",
        "059. Picked Bass",
        "060. Finger Bass",
        "061. Finger Tre",
        "062. Pickd 8 Top",
        "063. Fingr 8 Top",
        "064. Fingr 8 Tre",
        "065. The Guitar",
        "066. Twelv Top 1",
        "067. Twelv Top 2",
        "068. Twelv Top 3",
        "069. Drum Kit 1",
        "070. Drum Kit 2",
        "071. Drum Kit 3",
        "072. Drum Kit 4",
        "073. Drum Kit 5",
        "074. Drum Kit 6",
        "075. Drum Kit 7",
        "076. Large Hall",
        "077. Medium Room",
        "078. Tiled Room",
        "079. Gated Short",
        "080. Gated Long1",
        "081. Gated Long2",
        "082. Lush Reverb",
        "083. BlushReverb",
        "084. FullOn Verb",
        "085. Dry Kick",
        "086. Kick Verb",
        "087. Dry Snare",
        "088. Snare Verb",
        "089. Dry Tom",
        "090. Wet Tom",
        "091. High Hat 1",
        "092. High Hat 2",
        "093. High Hat 3",
        "094. Ride Ping",
        "095. Ride Bell",
        "096. Crash",
        "097. Cowbell",
        "098. Tambourine",
        "099. Side Stick",
        "100. Clave",
        "101. Square",
        "102. Sawtooth",
        "103. Triangle",
        "104. Moog Saw 1",
        "105. Moog Saw 2",
        "106. Moog Saw 3",
        "107. Moog Saw 4",
        "108. Moog Sqr 1",
        "109. Moog Sqr 2",
        "110. Moog Sqr 3",
        "111. Moog Sqr 4",
        "112. Moog Sqr 5",
        "113. Moog Sqr 6",
        "114. Moog Rect 1",
        "115. Moog Rect 2",
        "116. Moog Rect 3",
        "117. Moog Rect 4",
        "118. Moog Rect 5",
        "119. Moog Pulse1",
        "120. Moog Pulse2",
        "121. Moog Pulse3",
        "122. Moog Pulse4",
        "123. Moog Pulse5",
        "124. OB Wave 1",
        "125. OB Wave 2",
        "126. OB Wave 3",
        "127. OB Wave 4",
        "128. OB Wave 5",
        "129. ARP 2600 1",
        "130. ARP 2600 2",
        "131. ARP 2600 3",
        "132. B3 Wave 1",
        "133. B3 Wave 2",
        "134. B3 Wave 3",
        "135. B3 Wave 4",
        "136. B3 Wave 5",
        "137. B3 Wave 6",
        "138. B3 Wave 7",
        "139. B3 Wave 8",
        "140. B3 Wave 9",
        "141. B3 Wave 10",
        "142. B3 Wave 11",
        "143. B3 Wave 12",
        "144. B3 Wave 13",
        "145. B3 Wave 14",
        "146. B3 Wave 15",
        "147. B3 Wave 16",
        "148. B3 Wave 17",
        "149. B3 Wave 18",
        "150. ARPClarinet",
        "151. ARP Bassoon",
        "152. P5 No-Tone",
        "153. Noise Non-X",
        "154. Oct 1 Sine",
        "155. Oct 2 All",
        "156. Oct 3 All",
        "157. Oct 4 All",
        "158. Oct 5 All",
        "159. Oct 6 All",
        "160. Oct 7 All",
        "161. Oct 2 Odd",
        "162. Oct 3 Odd",
        "163. Oct 4 Odd",
        "164. Oct 5 Odd",
        "165. Oct 6 Odd",
        "166. Oct 7 Odd",
        "167. Oct 2 Even",
        "168. Oct 3 Even",
        "169. Oct 4 Even",
        "170. Oct 5 Even",
        "171. Oct 6 Even",
        "172. Oct 7 Even",
        "173. Low Odds",
        "174. Low Evens",
        "175. FourOctaves",
        "176. Synth Cyc 1",
        "177. Synth Cyc 2",
        "178. Synth Cyc 3",
        "179. Synth Cyc 4",
        "180. Fund Gone 1",
        "181. Fund Gone 2",
        "182. Bite Cyc",
        "183. Buzzy Cyc",
        "184. Metlphone 1",
        "185. Metlphone 2",
        "186. Metlphone 3",
        "187. Metlphone 4",
        "188. Duck Cyc 1",
        "189. Duck Cyc 2",
        "190. Duck Cyc 3",
        "191. Wind Cyc 1",
        "192. Wind Cyc 2",
        "193. Wind Cyc 3",
        "194. Wind Cyc 4",
        "195. Organ Cyc 1",
        "196. Organ Cyc 2",
        "197. Vio Essence",
        "198. Buzzoon",
        "199. Brassy Wave",
        "200. Reedy Buzz",
        "201. Growl Wave",
        "202. HarpsiWave",
        "203. Fuzzy Gruzz",
        "204. Power 5ths",
        "205. Filter Saw",
        "206. Ramp",
        "207. Evens Only",
        "208. Odds Gone",
        "209. Ice Bell",
        "210. Bronze Age",
        "211. Iron Plate",
        "212. Aluminum",
        "213. Lead Beam",
        "214. SteelXtract",
        "215. WinterGlass",
        "216. Town Bell",
        "217. Orch Bells",
        "218. Tubular SE",
        "219. Soft Bell",
        "220. Swirly",
        "221. Tack Attack",
        "222. ShimmerWave",
        "223. Mild Tone",
        "224. Ah Wave",
        "225. Vocal Wave",
        "226. Fuzzy Clav",
        "227. Electrhode",
        "228. Whine 1",
        "229. Filter Bass",
        "230. Harmonics",
        "231. ElecPiano",
        "232. Marimba Atk",
        "233. Vibe Attack",
        "234. XposedNoise",
        "235. MarimbaLoop",
        "236. Vibes Loop",
        "237. Stick Loop",
        "238. CowbellLoop",
        "239. Clave Loop",
        "240. Hi Hat Loop",
        "241. Drum Pile",
        "242. CP-70 Pile",
        "243. PickBassPil",
        "244. Guitar Pile",
        "245. FingBassPile",
        "246. Moog Loop",
        "247. OB Loop",
        "248. Frost Loop",
        "249. Memory Loop",
        "250. Pulse 98",
        "251. Pulse 96",
        "252. Pulse 94",
        "253. Pulse 90",
        "254. Pulse 75",
        "255. Pulse 50",
        "256. Sync Wave 1",
        "257. Sync Wave 2",
        "258. Sync Wave 3",
        "259. Sync Wave 4",
        "260. Sync Wave 5",
        "261. Multi Pulse",
        "262. Multi Sync",
        "263. DrKick Wave",
        "264. Cabasa Wave",
        "265. Cow Wave",
        "266. Rumble Fish",
        "267. Tape Hiss 1",
        "268. Tape Hiss 2",
        "269. NoiseTrack1",
        "270. NoiseTrack2",
        "271. Drum Energy",
        "272. Madness 1",
        "273. Madness 2",
        "274. Compressor",
        "275. Tricorder",
        "276. Transporter",
        "277. Weird Noise",
        "278. Frenzy Loop",
        "279. Sci-Fi 1",
        "280. Sci-Fi 2",
        "281. Sci-Fi 3",
        "282. SciFi Loop",
        "283. SwordA Loop",
        "284. SwordB Loop",
        "285. SwordC Loop",
        "286. SwordFight1",
        "287. SwordFight2",
        "288. SwordFight3",
        "289. RezFX1 Loop",
        "290. RezFX2 Loop",
        "291. RezFX3 Loop",
        "292. RezFX4 Loop",
        "293. RezFX5 Loop",
        "294. ElDrum Loop",
        "295. Jones Loop",
        "296. BallLoop1/s",
        "297. BallLoop2/s",
        "298. BallLoop3/s",
        "299. BallLoop4/s",
        "300. BallLoop5/s",
        "301. LazerLoop 1",
        "302. LazerLoop 2",
        "303. Lazer Duo",
        "304. Tri Loop",
        "305. Dr Hat Loop",
        "306. Rim Loop 1",
        "307. Rim Loop 2",
        "308. DrSanreLoop",
        "309. DrKickLoop",
        "310. CrashLoop 1",
        "311. CrashLoop 2",
        "312. Ride Loop 1",
        "313. Ride Loop 2",
        "314. Cabasa Loop",
        "315. Cabasave lp",
        "316. Clave Loop1",
        "317. Clave Loop2",
        "318. Clap Loop 1",
        "319. Clap Loop 2",
        "320. Cowbeloop",
        "321. Stick Loop1",
        "322. Stick Loop2",
        "323. TR Hat Loop",
        "324. Cl Hat Loop",
        "325. Tom Loop",
        "326. TR Snare Lp",
        "327. TR Kick Lp",
        "328. Music Loop",
        "329. Fwink Loop",
        "330. Hit Loop",
        "331. Hit String",
        "332. Sword A",
        "333. Sword B",
        "334. Sword C",
        "335. Rez FX 1",
        "336. Rez FX 2",
        "337. Rez FX 3",
        "338. Rez FX 4",
        "339. Rez FX 5",
        "340. Verb Clap",
        "341. Dub Claps",
        "342. Space Claps",
        "343. ElectroDrum",
        "344. BBall Jones",
        "345. Lazer Snare",
        "346. Lazer Kick",
        "347. Dr Hat",
        "348. Dr Rim",
        "349. Dr Snare",
        "350. Dr Kick",
        "351. TR CrashCym",
        "352. TR RideCym",
        "353. TR Cabasa",
        "354. Cabasave",
        "355. TR Clave",
        "356. TR Claps",
        "357. TR Cowbell",
        "358. TR SideStik",
        "359. TR Op Hat",
        "360. TR Cl Hat",
        "361. TR Tom",
        "362. TR Snare",
        "363. TR Kick",
        "364. Musical Box",
        "365. Fwink",
        "366. Da Hits",
        "367. SP1200 Gtrs",
        "368. Special FX",
        "369. Drum Kit 1",
        "370. Drum Kit 2",
        "371. Drum Kit 3",
        "372. Drum Kit 4",
        "373. Guitar Wave",
        "374. Elec Sitar",
        "375. Slap P-Bass",
        "376. Pop P-Bass",
        "377. Stick/Clean",
        "378. Stick/DistB",
        "379. Stick/DistT",
        "380. Moog Lead 1",
        "381. Moog Lead 2",
        "382. Moog Lead 3",
        "383. P5SyncLead2",
        "384. FM BlueHarp",
        "385. FM Harpsi",
        "386. FM Pong",
        "387. JP8 12Strng",
        "388. PPG Aylius",
        "389. PPG Buzzy",
        "390. PPG Ringer",
        "391. PPG Zylo",
        "392. PPG Bang",
        "393. PPG Merge",
        "394. PPG Shapes",
        "395. PPG Heavy",
        "396. P5 Gong",
        "397. P5 Tablura",
        "398. D50 Pipes",
        "399. APR FiltRez",
        "400. ARP SyncSwp",
        "401. ARP SyncMod",
        "402. JP6 Filters",
        "403. J1 Poly Syn",
        "404. JP8 Pulse",
        "405. P5 PulseMod",
        "406. JX3P Chimes",
        "407. Taurus 2",
        "408. Taurus 3",
        "409. Taurus 4",
        "410. P5 Bass 1",
        "411. P5 Bass 2",
        "412. PPG Bass",
        "413. J1 Seq Bass",
        "414. JX3P Brass",
        "415. Memry Brass",
        "416. P5 Brass",
        "417. OBX Strings",
        "418. J1 Strings",
        "419. Memry Strng",
        "420. Chamberlin",
        "421. Vox Jaguar",
        "422. Tight Clav",
        "423. Slack Clav",
        "424. Clavinet 2",
        "425. FM Dyno",
        "426. FM Piano",
        "427. DynoRhodes3",
        "428. MkI Hard",
        "429. MkI Soft",
        "430. KeyBass Hrd",
        "431. KeyBass Sft",
        "432. Tight Piano",
        "433. Slack Piano",
        "434. Grand Piano"
        };

    public static HashMap PLANET_PHATT_OFFSETS_INV = null;
    public static final int[] PLANET_PHATT_OFFSETS = 
        {
        0,
        3329, 3330, 3331, 3332, 3333, 3334, 3335, 3336, 3337, 3338, 
        3339, 3340, 3341, 3342, 3343, 3344, 3345, 3346, 3347, 3348, 
        3349, 3350, 3351, 3352, 3353, 3354, 3355, 3356, 3357, 3358, 
        3359, 3360, 3361, 3362, 3363, 3364, 3365, 3366, 3367, 3368, 
        3369, 3370, 3371, 3372, 3373, 3374, 3375, 3376, 3377, 3378, 
        3379, 3380, 3381, 3382, 3383, 3384, 3385, 3386, 3387, 3388, 
        3389, 3390, 3391, 3392, 3393, 3394, 3395, 3396, 3397, 3398, 
        3399, 3400, 3401, 3402, 3403, 3404, 3405, 3406, 3407, 3408, 
        3409, 3410, 3411, 3412, 3413, 3414, 3415, 3416, 3417, 3418, 
        3419, 3420, 3421, 3422, 3423, 3424, 3694, 3425, 3426, 3427, 
        3428, 3429, 3430, 3431, 3432, 3433, 3434, 3435, 3436, 3437, 
        3438, 3439, 3440, 3441, 3442, 3443, 3444, 3445, 3446, 3447, 
        3448, 3449, 3450, 3451, 3452, 3453, 3454, 3455, 3456, 3798, 
        3695, 3457, 3458, 3459, 3460, 3461, 3462, 3463, 3464, 3465, 
        3466, 3467, 3468, 3469, 3470, 3496, 3497, 3498, 3499, 3500, 
        3501, 3502, 3503, 3504, 3505, 3506, 3507, 3508, 3509, 3510, 
        3511, 3512, 3513, 3514, 3515, 3516, 3517, 3518, 3519, 3520, 
        3521, 3522, 3523, 3524, 3525, 3526, 3527, 3528, 3529, 3530, 
        3531, 3532, 3533, 3534, 3535, 3536, 3537, 3538, 3539, 3540, 
        3541, 3542, 3543, 3544, 3545, 3546, 3547, 3471, 3472, 3473, 
        3474, 3475, 3476, 3477, 3478, 3479, 3480, 3481, 3482, 3483, 
        3484, 3485, 3486, 3487, 3488, 3489, 3490, 3491, 3492, 3493, 
        3494, 3495, 3548, 3549, 3550, 3551, 3552, 3553, 3554, 3555, 
        3556, 3557, 3558, 3559, 3560, 3561, 3562, 3563, 3564, 3565, 
        3566, 3567, 3568, 3569, 3570, 3585, 3586, 3587, 3588, 3589, 
        3590, 3591, 3592, 3593, 3594, 3595, 3596, 3597, 3598, 3599, 
        3600, 3601, 3602, 3603, 3604, 3605, 3606, 3607, 3608, 3609, 
        3610, 3611, 3612, 3613, 3614, 3615, 3616, 3617, 3618, 3619, 
        3620, 3621, 3622, 3623, 3624, 3625, 3626, 3627, 3628, 3629, 
        3630, 3631, 3632, 3633, 3634, 3635, 3636, 3637, 3638, 3639, 
        3640, 3641, 3642, 3643, 3644, 3645, 3646, 3647, 3761, 3648, 
        3649, 3650, 3651, 3652, 3653, 3654, 3655, 3656, 3657, 3658, 
        3659, 3660, 3661, 3662, 3663, 3664, 3665, 3666, 3667, 3668, 
        3669, 3670, 3671, 3672, 3673, 3674, 3675, 3700, 3701, 3702, 
        3703, 3704, 3705, 3706, 3707, 3708, 3709, 3710, 3711, 3712, 
        3713, 3714, 3715, 3716, 3717, 3718, 3719, 3720, 3721, 3722, 
        3723, 3724, 3725, 3726, 3727, 3728, 3729, 3730, 3731, 3732, 
        3733, 3734, 3735, 3736, 3737, 3738, 3739, 3740, 3741, 3742, 
        3743, 3744, 3745, 3746, 3747, 3748, 3749, 3750, 3751, 3752, 
        3753, 3754, 3755, 3756, 3676, 3757, 3758, 3677, 3759, 3760, 
        3692, 3762, 3763, 3764, 3765, 3766, 3767, 3768, 3769, 3770, 
        3771, 3772, 3773, 3774, 3775, 3776, 3777, 3778, 3678, 3679, 
        3779, 3780, 3781, 3782, 3680, 3681, 3786, 3783, 3784, 3785, 
        3682, 3787, 3788, 3789, 3790, 3791, 3792, 3793, 3794, 3795, 
        3796, 3797, 3799, 3800, 3801, 3802, 3803, 3804, 3805, 3806, 
        3807, 3808, 3809, 3810, 3811, 3812, 3813, 3814, 3815, 3816, 
        3817, 3818, 3683, 3684, 3819, 3820, 3685, 3821, 3822, 3823, 
        3686, 3687, 3688, 3689, 3690, 3691, 3693, 3696, 3697, 3699, 
        3698
        };

    public static HashMap ORBIT_V2_OFFSETS_INV = null;
    public static final int[] ORBIT_V2_OFFSETS = 
        {
        0,
        2817, 2818, 2819, 2820, 2821, 2822, 2823, 2824, 2825, 2826, 
        2827, 2828, 2829, 2830, 2831, 2832, 2833, 2834, 2835, 2836, 
        2837, 2838, 2839, 2840, 2841, 2842, 2843, 2844, 2845, 2846, 
        2847, 2848, 2849, 2850, 2851, 2852, 2853, 2854, 2855, 2856, 
        2857, 2858, 2859, 2860, 2861, 2862, 2863, 2864, 2865, 2866, 
        2867, 2868, 2869, 2870, 2871, 2872, 2873, 2874, 2875, 2876, 
        2877, 2878, 2879, 2880, 2881, 2882, 2883, 2884, 2885, 2886, 
        2887, 2888, 2889, 2890, 2891, 2892, 2893, 2894, 2895, 2896, 
        2897, 2898, 2899, 2900, 2901, 2902, 2903, 2904, 2905, 2906, 
        2907, 2908, 2909, 2910, 2911, 2912, 2913, 2914, 2915, 2916, 
        2917, 2918, 2919, 2920, 2921, 2922, 2923, 2924, 2925, 2926, 
        2927, 2928, 2929, 2930, 2931, 2932, 2933, 2934, 2935, 2936, 
        2937, 2938, 2939, 2940, 2941, 2942, 2943, 2944, 2945, 2946, 
        2947, 2948, 2949, 2950, 2951, 2952, 2953, 2954, 2955, 2956, 
        2957, 2958, 2959, 2960, 2961, 2962, 2963, 2964, 2965, 2966, 
        2967, 2968, 2969, 2970, 2971, 2972, 2973, 2974, 2975, 2976, 
        2977, 2978, 2979, 2980, 2981, 2982, 2983, 2984, 2985, 2986, 
        2987, 2988, 2989, 2990, 2991, 2992, 2993, 2994, 2995, 2996, 
        2997, 2998, 2999, 3000, 3001, 3002, 3003, 3004, 3005, 3006, 
        3007, 3008, 3009, 3010, 3011, 3012, 3013, 3014, 3015, 3016, 
        3017, 3018, 3019, 3020, 3021, 3022, 3023, 3024, 3025, 3026, 
        3027, 3028, 3029, 3030, 3031, 3032, 3033, 3034, 3035, 3036, 
        3037, 3038, 3039, 3040, 3041, 3042, 3043, 3044, 3045, 3046, 
        3047, 3048, 3049, 3050, 3051, 3052, 3073, 3074, 3075, 3076, 
        3077, 3078, 3079, 3080, 3081, 3082, 3083, 3084, 3085, 3086, 
        3087, 3088, 3089, 3090, 3091, 3092, 3093, 3094, 3095, 3096, 
        3097, 3098, 3099, 3100, 3101, 3102, 3103, 3104, 3105, 3106, 
        3107, 3108, 3109, 3110, 3111, 3112, 3113, 3114, 3115, 3116, 
        3117, 3118, 3119, 3120, 3121, 3122, 3123, 3124, 3125, 3126, 
        3127, 3128, 3129, 3130, 3131, 3132, 3133, 3134, 3135, 3136, 
        3137, 3138, 3139, 3140, 3141, 3142, 3143, 3144, 3145, 3146, 
        3147, 3148, 3149, 3150, 3151, 3152, 3153, 3154, 3155, 3156, 
        3157, 3158, 3159, 3160, 3161, 3162, 3163, 3164, 3165, 3166, 
        3167, 3168, 3169, 3170, 3171, 3172, 3173, 3174, 3175, 3176, 
        3177, 3178, 3179, 3180, 3181, 3182, 3183, 3184, 3185, 3186, 
        3187, 3188, 3189, 3190, 3191, 3192, 3193, 3194, 3195, 3196, 
        3197, 3198, 3199, 3200, 3201, 3202, 3203, 3204, 3205, 3206, 
        3207, 3208, 3209, 3210, 3211, 3212, 3213, 3214, 3215, 3216, 
        3217, 3218, 3219, 3220
        };

    public static HashMap CARNAVAL_OFFSETS_INV = null;
    public static final int[] CARNAVAL_OFFSETS = 
        {
        0,
        3841, 3842, 3843, 3844, 3845, 3846, 3847, 3848, 3849, 3850,
        3851, 3852, 3853, 3854, 3855, 3856, 3857, 3858, 3859, 3860, 
        3861, 3862, 3863, 3864, 3865, 3866, 3867, 3868, 3869, 3870, 
        3871, 3872, 3873, 3874, 3875, 3876, 3877, 3878, 3879, 3880, 
        3881, 3882, 3883, 3884, 3885, 3886, 3887, 3888, 3889, 3890, 
        3891, 3892, 3893, 3894, 3895, 3896, 3897, 3898, 3899, 3900, 
        3901, 3902, 3903, 3904, 3905, 3906, 3907, 3908, 3909, 3910, 
        3911, 3912, 3913, 3914, 3915, 3916, 3917, 3918, 3919, 3920, 
        3921, 3922, 3923, 3924, 3925, 3926, 3927, 3928, 3929, 3930, 
        3931, 3932, 3933, 3934, 3935, 3936, 3937, 3938, 3939, 3940, 
        3941, 3942, 3943, 3944, 3945, 3946, 3947, 3948, 3949, 3950, 
        3951, 3952, 3953, 3954, 3955, 3956, 3957, 3958, 3959, 3960, 
        3961, 3962, 3963, 3964, 3965, 3966, 3967, 3968, 3969, 3970, 
        3971, 3972, 3973, 3974, 3975, 3976, 3977, 3978, 3979, 3980, 
        3981, 3982, 3983, 3984, 3985, 3986, 3987, 3988, 3989, 3990, 
        3991, 3992, 3993, 3994, 3995, 3996, 3997, 3998, 3999, 4000, 
        4001, 4002, 4003, 4004, 4005, 4006, 4007, 4008, 4009, 4010, 
        4011, 4012, 4013, 4014, 4015, 4016, 4017, 4018, 4019, 4020, 
        4021, 4022, 4023, 4024, 4025, 4026, 4027, 4028, 4029, 4030, 
        4031, 4032, 4033, 4034, 4035, 4036, 4037, 4038, 4039, 4040, 
        4041, 4042, 4043, 4044, 4045, 4046, 4047, 4048, 4049, 4050, 
        4051, 4052, 4053, 4054, 4055, 4056, 4057, 4058, 4059, 4060, 
        4061, 4062, 4063, 4097, 4098, 4099, 4100, 4101, 4102, 4103, 
        4104, 4105, 4106, 4107, 4108, 4109, 4110, 4111, 4112, 4113, 
        4114, 4115, 4116, 4117, 4118, 4119, 4120, 4121, 4122, 4123, 
        4124, 4125, 4126, 4127, 4128, 4129, 4130, 4131, 4132, 4133, 
        4134, 4135, 4136, 4137, 4138, 4139, 4140, 4141, 4142, 4143, 
        4144, 4145, 4146, 4147, 4148, 4149, 4150, 4151, 4152, 4153, 
        4154, 4155, 4156, 4157, 4158, 4159, 4160, 4161, 4162, 4163, 
        4164, 4165, 4166, 4167, 4168, 4169, 4170
        };

    public static HashMap VINTAGE_KEYS_OFFSETS_INV = null;
    public static final int[] VINTAGE_KEYS_OFFSETS =
        {
        0,
        1537, 1538, 1539, 1540, 1541, 1542, 1543, 1544, 1545, 1546, 
        1547, 1548, 1549, 1550, 1551, 1552, 1553, 1554, 1555, 1556, 
        1557, 1558, 1559, 1560, 1561, 1562, 1563, 1564, 1565, 1566, 
        1567, 1568, 1569, 1570, 1571, 1572, 1573, 1574, 1575, 1576, 
        1577, 1578, 1579, 1580, 1581, 1582, 1583, 1584, 1585, 1586, 
        1587, 1588, 1589, 1590, 1591, 1592, 1593, 1594, 1595, 1596, 
        1597, 1598, 1599, 1600, 1601, 1602, 1603, 1604, 1605, 1606, 
        1607, 1608, 1609, 1610, 1611, 1612, 1613, 1614, 1615, 1616, 
        1617, 1618, 1619, 1620, 1621, 1622, 1623, 1624, 1625, 1626, 
        1627, 1628, 1629, 1630, 1631, 1632, 1633, 1634, 1635, 1636, 
        1637, 1638, 1639, 1640, 1641, 1642, 1643, 1644, 1645, 1646, 
        1647, 1648, 1649, 1650, 1651, 1652, 1653, 1654, 1655, 1656, 
        1657, 1658, 1659, 1660, 1661, 1662, 1663, 1664, 1665, 1666, 
        1667, 1668, 1669, 1670, 1671, 1672, 1673, 1674, 1675, 1676, 
        1677, 1678, 1679, 1680, 1681, 1682, 1683, 1684, 1685, 1686, 
        1687, 1688, 1689, 1690, 1691, 1692, 1693, 1694, 1695, 1696, 
        1697, 1698, 1699, 1700, 1701, 1702, 1703, 1704, 1705, 1706, 
        1707, 1708, 1709, 1710, 1711, 1712, 1713, 1714, 1715, 1716, 
        1717, 1718, 1719, 1720, 1721, 1722, 1723, 1724, 1725, 1726, 
        1727, 1728, 1729, 1730, 1731, 1732, 1733, 1734, 1735, 1736, 
        1737, 1738, 1739, 1740, 1741, 1742, 1743, 1744, 1745, 1746, 
        1747, 1748, 1749, 1750, 1751, 1752, 1753, 1754, 1755, 1756, 
        1757, 1758, 1759, 1760, 1761, 1762, 1763, 1764, 1765, 1766, 
        1767, 1768, 1769, 1770, 1771, 1772, 1773, 1774, 1775, 1776, 
        1777, 1778, 1779, 1780, 1781, 1782, 1783, 1784, 1785, 
        };

    public static HashMap VINTAGE_KEYS_PLUS_OFFSETS_INV = null;
    public static final int[] VINTAGE_KEYS_PLUS_OFFSETS = 
        {
        0,
        1537, 1538, 1539, 1540, 1541, 1542, 1543, 1544, 1545, 1546, 
        1547, 1548, 1549, 1550, 1551, 1552, 1553, 1554, 1555, 1556, 
        1557, 1558, 1559, 1560, 1561, 1562, 1563, 1564, 1565, 1566, 
        1567, 1568, 1569, 1570, 1571, 1572, 1573, 1574, 1575, 1576, 
        1577, 1578, 1579, 1580, 1581, 1582, 1583, 1584, 1585, 1586, 
        1587, 1588, 1589, 1590, 1591, 1592, 1593, 1594, 1595, 1596, 
        1597, 1598, 1599, 1600, 1601, 1602, 1603, 1604, 1605, 1606, 
        1607, 1608, 1609, 1610, 1611, 1612, 1613, 1614, 1615, 1616, 
        1617, 1618, 1619, 1620, 1621, 1622, 1623, 1624, 1625, 1626, 
        1627, 1628, 1629, 1630, 1631, 1632, 1633, 1634, 1635, 1636, 
        1637, 1638, 1639, 1640, 1641, 1642, 1643, 1644, 1645, 1646, 
        1647, 1648, 1649, 1650, 1651, 1652, 1653, 1654, 1655, 1656, 
        1657, 1658, 1659, 1660, 1661, 1662, 1663, 1664, 1665, 1666, 
        1667, 1668, 1669, 1670, 1671, 1672, 1673, 1674, 1675, 1676, 
        1677, 1678, 1679, 1680, 1681, 1682, 1683, 1684, 1685, 1686, 
        1687, 1688, 1689, 1690, 1691, 1692, 1693, 1694, 1695, 1696, 
        1697, 1698, 1699, 1700, 1701, 1702, 1703, 1704, 1705, 1706, 
        1707, 1708, 1709, 1710, 1711, 1712, 1713, 1714, 1715, 1716, 
        1717, 1718, 1719, 1720, 1721, 1722, 1723, 1724, 1725, 1726, 
        1727, 1728, 1729, 1730, 1731, 1732, 1733, 1734, 1735, 1736, 
        1737, 1738, 1739, 1740, 1741, 1742, 1743, 1744, 1745, 1746, 
        1747, 1748, 1749, 1750, 1751, 1752, 1753, 1754, 1755, 1756, 
        1757, 1758, 1759, 1760, 1761, 1762, 1763, 1764, 1765, 1766, 
        1767, 1768, 1769, 1770, 1771, 1772, 1773, 1774, 1775, 1776, 
        1777, 1778, 1779, 1780, 1781, 1782, 1783, 1784, 1785, 2049, 
        2050, 2051, 2052, 2053, 2054, 2055, 2056, 2057, 2058, 2059, 
        2060, 2061, 2062, 2063, 2064, 2065, 2066, 2067, 2068, 2069, 
        2070, 2071, 2072, 2073, 2074, 2075, 2076, 2077, 2078, 2079, 
        2080, 2081, 2082, 2083, 2084, 2085, 2086, 2087, 2088, 2089, 
        2090, 2091, 2092, 2093, 2094, 2095, 2096, 2097, 2098, 2099, 
        2100, 2101, 2102, 2103, 2104, 2105, 2106, 2107, 2108, 2109, 
        2110, 2111, 2112, 2113, 2114, 2115, 2116, 2117, 2118, 2119, 
        2120, 2121, 2122, 2123, 2124, 2125, 2126, 2127, 2128, 2129, 
        2130, 2131, 2132, 2133, 2134, 2135, 2136, 2137, 2138, 2139, 
        2140, 2141, 2142, 2143, 2144, 2145, 2146, 2147, 2148, 2149, 
        2150, 2151, 2152, 2153, 2154, 2155, 2156, 2157, 2158, 2159, 
        2160, 2161, 2162, 2163, 2164, 2165, 2166, 2167, 2168, 2169, 
        2170, 2171, 2172, 2173, 2174, 2175, 2176, 2177, 2178, 2179, 
        2180, 2181, 2182, 2183, 2184, 2185, 2186, 2187, 2188, 2189, 
        2190, 2191, 2192, 2193, 2194, 2195, 2196, 2197, 2198, 2199, 
        2200, 2201, 2202, 2203, 2204, 2205, 2206, 2207, 2208, 2209, 
        2210, 2211, 2212, 2213, 2214, 2215, 2216, 2217, 2218, 2219, 
        2220, 2221, 2222, 2223, 2224, 2225, 2226, 2227, 2228, 2229, 
        2230, 2231, 2232, 2233, 
        };


    static final String[][] ALL_INSTRUMENT_TYPES = { PLANET_PHATT_INSTRUMENTS, ORBIT_V1_INSTRUMENTS, ORBIT_V2_INSTRUMENTS, CARNAVAL_INSTRUMENTS, VINTAGE_KEYS_INSTRUMENTS, VINTAGE_KEYS_PLUS_INSTRUMENTS };
    public String[] getInstruments()
        {
        if (getSynthType() >= SYNTH_TYPE_PLANET_PHATT && getSynthType() <= SYNTH_TYPE_VINTAGE_KEYS_PLUS)
            return ALL_INSTRUMENT_TYPES[getSynthType()];
        else 
            return PLANET_PHATT_INSTRUMENTS;
        }

    static final String[][] ALL_FILTER_TYPES = { FILTER_TYPES, FILTER_TYPES, FILTER_TYPES, FILTER_TYPES, VINTAGE_KEYS_FILTER_TYPES, VINTAGE_KEYS_FILTER_TYPES };
    public String[] getFilters()
        {
        if (getSynthType() >= SYNTH_TYPE_PLANET_PHATT && getSynthType() <= SYNTH_TYPE_VINTAGE_KEYS_PLUS)
            {
            return ALL_FILTER_TYPES[getSynthType()];
            }
        else 
            {
            return FILTER_TYPES;
            }
        }

            
    public String[] getBankNames() 
        { 
        return (getSynthType() == SYNTH_TYPE_ORBIT_V1 || getSynthType() == SYNTH_TYPE_VINTAGE_KEYS || getSynthType() == SYNTH_TYPE_VINTAGE_KEYS_PLUS ? SHORT_BANKS : BANKS);
        }

    /** Return a list of all patch number names.  Default is { "Main" } */
    public String[] getPatchNumberNames()  { return buildIntegerNames(128, 0); }

    /** Return a list whether patches in banks are writeable.  Default is { false } */
    public boolean[] getWriteableBanks() 
        { 
        return (getSynthType() == SYNTH_TYPE_ORBIT_V1 || getSynthType() == SYNTH_TYPE_VINTAGE_KEYS || getSynthType() == SYNTH_TYPE_VINTAGE_KEYS_PLUS ? 
            new boolean[] { true, true, false, false } :
            new boolean[] { true, true, false, false, false });
        }

    /** Return a list whether individual patches can be written.  Default is FALSE. */
    public boolean getSupportsPatchWrites() { return true; }

    public int getPatchNameLength() { return 12; }

    public boolean getSendsAllParametersAsDump() { return false; }

    //public int getPauseAfterChangePatch() { return 1000; }

    //public int getPauseAfterReceivePatch() { return 1000; }
    public int getBatchDownloadWaitTime() { return 400; }

    public boolean getSendsParametersAfterNonMergeParse() { return true; }
    }
