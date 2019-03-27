/***
    Copyright 2018 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.emumorpheus;

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
   A patch editor for the E-Mu Morpheus and Ultra Proteus synthesizers
        
   @author Sean Luke
*/

public class EmuMorpheus extends Synth
    {
    Chooser[] instrumentChoosers = new Chooser[2];
    public HashMap ultraproteusInstrumentOffsetsToIndex = new HashMap();
    public HashMap ultraproteusFilterOffsetsToIndex = new HashMap();
    public HashMap morpheusInstrumentOffsetsToIndex = new HashMap();
    public HashMap morpheusFilterOffsetsToIndex = new HashMap();
        
    boolean morpheus;
    public static final String ULTRAPROTEUS_KEY = "UltraProteus";
    public static final int NUM_MORPHEUS_FILTERS = 198;
    
    public boolean isMorpheus() { return morpheus; }
    public void setMorpheus(boolean val)
        {
        setLastX("" + (!val), ULTRAPROTEUS_KEY, getSynthName(), true);
        morpheus = val;
        for(int i = 0; i < 2; i++)
            {
            instrumentChoosers[i].setElements("Type", getInstruments());
            }
        updateTitle();
        }
    
    public EmuMorpheus()
        {
        if (MORPHEUS_FILTERS == null)
            {
            // Load the morpheus filters dynamically, since they're a strict subset and may not be used.  Heck, why not?
            MORPHEUS_FILTERS = new String[NUM_MORPHEUS_FILTERS];
            MORPHEUS_FILTER_OFFSETS = new int[NUM_MORPHEUS_FILTERS];
            MORPHEUS_FILTER_TYPES = new int[NUM_MORPHEUS_FILTERS];
                
            System.arraycopy(ULTRAPROTEUS_FILTERS, 0, MORPHEUS_FILTERS, 0, NUM_MORPHEUS_FILTERS);
            System.arraycopy(ULTRAPROTEUS_FILTER_OFFSETS, 0, MORPHEUS_FILTER_OFFSETS, 0, NUM_MORPHEUS_FILTERS);
            System.arraycopy(ULTRAPROTEUS_FILTER_TYPES, 0, MORPHEUS_FILTER_TYPES, 0, NUM_MORPHEUS_FILTERS);
            }
        
        String m = getLastX(ULTRAPROTEUS_KEY, getSynthName());
        morpheus = (m == null ? false : !Boolean.parseBoolean(m));
        
        for(int i = 0; i < parameters.length; i++)
            {
            parametersToIndex.put(parameters[i], Integer.valueOf(i));
            }
            
        for(int i = 0; i < ULTRAPROTEUS_INSTRUMENT_OFFSETS.length; i++)
            {
            ultraproteusInstrumentOffsetsToIndex.put(Integer.valueOf(ULTRAPROTEUS_INSTRUMENT_OFFSETS[i]), Integer.valueOf(i));
            }

        for(int i = 0; i < ULTRAPROTEUS_FILTER_OFFSETS.length; i++)
            {
            ultraproteusFilterOffsetsToIndex.put(Integer.valueOf(ULTRAPROTEUS_FILTER_OFFSETS[i]), Integer.valueOf(i));
            }
                        
        for(int i = 0; i < MORPHEUS_INSTRUMENT_OFFSETS.length; i++)
            {
            morpheusInstrumentOffsetsToIndex.put(Integer.valueOf(MORPHEUS_INSTRUMENT_OFFSETS[i]), Integer.valueOf(i));
            }

        for(int i = 0; i < MORPHEUS_FILTER_OFFSETS.length; i++)
            {
            morpheusFilterOffsetsToIndex.put(Integer.valueOf(MORPHEUS_FILTER_OFFSETS[i]), Integer.valueOf(i));
            }
                        
                        
        /// SOUND PANEL
                
        SynthPanel soundPanel = new SynthPanel(this);
        
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        JComponent nameGlobal = addNameGlobal(Style.COLOR_GLOBAL());
        hbox.add(nameGlobal);
        hbox.addLast(addKey(Style.COLOR_A()));
        vbox.add(hbox);
        
        hbox = new HBox();
        hbox.add(addLFO(1, Style.COLOR_B()));
        hbox.addLast(addLFO(2, Style.COLOR_B()));
        vbox.add(hbox);

        hbox = new HBox();
        hbox.add(addControllers(Style.COLOR_A()));
        hbox.addLast(addAuxEnvelope(Style.COLOR_C()));
        vbox.add(hbox);
        
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Global", soundPanel);
                
              
        soundPanel = new SynthPanel(this);

        vbox = new VBox();
        hbox = new HBox();
        hbox.add(addInstrument(1, Style.COLOR_A()));
        hbox.addLast(addLoop(1, Style.COLOR_A()));
        vbox.add(hbox);

        hbox = new HBox();
        hbox.add(addFilter(1, Style.COLOR_B()));
        hbox.addLast(addInstrumentKey(1, Style.COLOR_A()));
        vbox.add(hbox);
        vbox.add(addAlternateEnvelope(1, Style.COLOR_C()));
  
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Instrument 1", soundPanel);

        soundPanel = new SynthPanel(this);

        vbox = new VBox();
        hbox = new HBox();
        hbox.add(addInstrument(2, Style.COLOR_A()));
        hbox.addLast(addLoop(2, Style.COLOR_A()));
        vbox.add(hbox);

        hbox = new HBox();
        hbox.add(addFilter(2, Style.COLOR_B()));
        hbox.addLast(addInstrumentKey(2, Style.COLOR_A()));
        vbox.add(hbox);
        vbox.add(addAlternateEnvelope(2, Style.COLOR_C()));
      
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Instrument 2", soundPanel);
                
        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        vbox.add(addFunctionGenerator(1, Style.COLOR_A()));
//        vbox.add(addFunctionGeneratorDisplay(1, Style.COLOR_B()));

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Generator 1", soundPanel);

        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        vbox.add(addFunctionGenerator(2, Style.COLOR_A()));
        //       vbox.add(addFunctionGeneratorDisplay(2, Style.COLOR_B()));

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Generator 2", soundPanel);

        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        vbox.add(addNoteOnMatrix(Style.COLOR_A()));
        vbox.add(addRealTimeMatrix(Style.COLOR_B()));
        vbox.add(addFootSwitch(Style.COLOR_C()));

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Patching", soundPanel);


                        
        model.set("name", "Untitled");
        model.set("bank", 0);
        model.set("number", 0);
                
        loadDefaults();        
        }
                
    public String getDefaultResourceFileName() { return "EmuMorpheus.init"; }
    public String getHTMLResourceFileName() { return "EmuMorpheus.html"; }

    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        JComboBox bank = new JComboBox(BANKS);
        int num = model.get("number");
        JTextField number = new JTextField("" + (num < 10 ? "00" : (num < 100 ? "0" : "")) + num, 3);
        
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
        Category globalCategory = new Category(this, "E-Mu Morpheus", color);           // Notice we've fixed the name
        globalCategory.makeUnresettable();
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
                                
        VBox vbox = new VBox();
        HBox hbox2 = new HBox();
        comp = new PatchDisplay(this, 7);
        hbox2.add(comp);
        
        JCheckBox check = new JCheckBox("Ultra Proteus");
        check.setSelected(!morpheus);
        check.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                setMorpheus(!check.isSelected());
                }
            });
        check.setFont(Style.SMALL_FONT());
        check.setOpaque(false);
        check.setForeground(Style.TEXT_COLOR());
        hbox2.add(check);
        hbox.addLast(Stretch.makeHorizontalStretch());
        vbox.add(hbox2);
        
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
        hbox.add(vbox);

        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }


    public JComponent addKey(Color color)
        {
        Category category = new Category(this, "Keyboard", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        vbox = new VBox();
        params = XFADE_MODES;
        comp = new Chooser("Crossfade Mode", this, "xfademode", params);
        vbox.add(comp);

        params = XFADE_DIRECTIONS;
        comp = new Chooser("Crossfade Direction", this, "xfadedirection", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Crossfade", this, "xfadebalance", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Balance");
        hbox.add(comp);

        comp = new LabelledDial("Crossfade", this, "xfadeamount", color, 0, 255);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        comp = new LabelledDial("Crossfade", this, "xswitchpoint", color, 0, 127)
            {
            public String map(int val)
                {
                return "" + NOTES[val % 12] + "" + (val / 12 + 1);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Switch Point");
        hbox.add(comp);

        vbox = new VBox();
        params = TUNE_TABLES;
        comp = new Chooser("Tune Table", this, "tunetable", params);
        vbox.add(comp);

        params = MIX_BUS;
        comp = new Chooser("Mix Bus", this, "mixbus", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Low Key", this, "presetlowkey", color, 0, 127)
            {
            public String map(int val)
                {
                return "" + NOTES[val % 12] + "" + (val / 12 + 1);
                }
            };
        hbox.add(comp);
        
        comp = new LabelledDial("Hi Key", this, "presethikey", color, 0, 127)
            {
            public String map(int val)
                {
                return "" + NOTES[val % 12] + "" + (val / 12 + 1);
                }
            };
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
        
        comp = new LabelledDial("Bend Range", this, "bendrange", color, 0, 13)
            {
            public String map(int val)
                {
                if (val == 13) return "Global";
                else return "" + val;
                }
            };
        getModel().setMetricMax("bendrange", 12);
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
        
        comp = new LabelledDial("Portamento", this, "portmode", color, 0, 5)
            {
            public String map(int val)
                {
                if (val == 0) return "Mono";
                else return "Poly " + val;
                }
            };
        getModel().setMetricMin("portmode", 1);
        ((LabelledDial)comp).addAdditionalLabel("Mode");
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
                
                
                
    public JComponent addNoteOnMatrix(Color color)
        {
        Category category = new Category(this, "Note On Matrix", color);

        JComponent comp;
        String[] params;
        VBox main = new VBox();
        HBox hbox = new HBox();

        for(int i = 1; i < 11; i++)
            {
            VBox vbox = new VBox();
            params = NOTE_ON_PATCHCORD_SOURCES;
            comp = new Chooser("" + i + " Source", this, "noteon" + i + "source", params);
            vbox.add(comp);

            params = PATCHCORD_DESTINATIONS;
            comp = new Chooser("" + i + " Destination", this, "noteon" + i + "dest", params);
            vbox.add(comp);
            hbox.add(vbox);

            comp = new LabelledDial("Amount", this, "noteon" + i + "amount", color, -128, 127);
            hbox.add(comp);
                        
            if (i == 5 || i == 10)
                {
                main.add(hbox);
                hbox = new HBox();
                }

            }

        category.add(main, BorderLayout.CENTER);
        return category;
        }

    public JComponent addRealTimeMatrix(Color color)
        {
        Category category = new Category(this, "Real Time Matrix", color);

        JComponent comp;
        String[] params;
        VBox main = new VBox();
        HBox hbox = new HBox();

        for(int i = 1; i < 11; i++)
            {
            VBox vbox = new VBox();
            params = REALTIME_PATCHCORD_SOURCES;
            comp = new Chooser("" + i + " Source", this, "realtime" + i + "source", params);
            vbox.add(comp);

            params = PATCHCORD_DESTINATIONS;
            comp = new Chooser("" + i + " Destination", this, "realtime" + i + "dest", params);
            vbox.add(comp);
            hbox.add(vbox);

            comp = new LabelledDial("Amount", this, "realtime" +i + "amount", color, -128, 127);
            hbox.add(comp);
                        
            if (i == 5 || i == 10)
                {
                main.add(hbox);
                hbox = new HBox();
                }
            }

        category.add(main, BorderLayout.CENTER);
        return category;
        }


    public JComponent addFootSwitch(Color color)
        {
        Category category = new Category(this, "Foot Switch", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();


        params = PATCHCORD_DESTINATIONS;
        comp = new Chooser("Destination 1", this, "foot1dest", params);
        hbox.add(comp);
        params = PATCHCORD_DESTINATIONS;
        comp = new Chooser("Destination 2", this, "foot2dest", params);
        hbox.add(comp);
        VBox vbox = new VBox();
        params = PATCHCORD_DESTINATIONS;
        comp = new Chooser("Destination 3", this, "foot3dest", params);
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addControllers(Color color)
        {
        Category category = new Category(this, "MIDI Control", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();

        comp = new LabelledDial("Control A", this, "ctrlaamount", color, -128, 127);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        comp = new LabelledDial("Control B", this, "ctrlbamount", color, -128, 127);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        comp = new LabelledDial("Control C", this, "ctrlcamount", color, -128, 127);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        comp = new LabelledDial("Control D", this, "ctrldamount", color, -128, 127);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        comp = new LabelledDial("Pressure", this, "pressureamount", color, -128, 127);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addLFO(int lfo, Color color)
        {
        Category category = new Category(this, "LFO " + lfo, color);

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

    public JComponent addFunctionGenerator(int fg, Color color)
        {
        Category category = new Category(this, "Function Generator", color);

        JComponent comp;
        String[] params;
        VBox main = new VBox();
        HBox hbox = new HBox();
        
        for(int i = 1; i < 9; i++)
            {
            VBox vbox = new VBox();

            params = FUNCTION_GENERATOR__TYPES;
            comp = new Chooser("" + i + " Type", this, "fg" + fg + "seg" + i + "type", params);
            vbox.add(comp);

            params = FUNCTION_GENERATORS;
            comp = new Chooser("" + i + " Shape", this, "fg" + fg + "seg" + i + "shape", params);
            vbox.add(comp);

            params = FUNCTION_GENERATOR_CONDITIONS;
            comp = new Chooser("" + i + " Jump Condition", this, "fg" + fg + "seg" + i + "condjump", params);
            vbox.add(comp);
            hbox.add(vbox);
            
            VBox vbox2 = new VBox();
            HBox hbox2 = new HBox();
            comp = new LabelledDial("" + i + " Level", this, "fg" + fg + "seg" + i + "level", color, -127, 127);
            hbox2.add(comp);
                        
            comp = new LabelledDial("" + i + " Time", this, "fg" + fg + "seg" + i + "time", color, 0, 4095);
            hbox2.add(comp);
            vbox2.add(hbox2);
            
            hbox2 = new HBox();
            comp = new LabelledDial("" + i + " Cond Val", this, "fg" + fg + "seg" + i + "condval", color, -127, 127);
            hbox2.add(comp);

            comp = new LabelledDial("" + i +  " Dest Seg", this, "fg" + fg + "seg" + i + "destseg", color, 0, 7);
            hbox2.add(comp);
            vbox2.add(hbox2);
            hbox.add(vbox2);


            if (i == 4 || i == 8)
                {
                main.add(hbox);
                hbox = new HBox();
                }
            }

        main.add(Strut.makeVerticalStrut(10));

        comp = new LabelledDial("Amount", this, "fg" + fg + "amount", color, -128, 127);
        hbox.add(comp);

        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "fg" + fg + "seg1time", "fg" + fg + "seg2time", "fg" + fg + "seg3time", "fg" + fg + "seg4time", "fg" + fg + "seg5time", "fg" + fg + "seg6time", "fg" + fg + "seg7time", "fg" + fg + "seg8time" },
            new String[] { null, "fg" + fg + "seg1level", "fg" + fg + "seg2level", "fg" + fg + "seg3level", "fg" + fg + "seg4level", "fg" + fg + "seg5level", "fg" + fg + "seg6level", "fg" + fg + "seg7level", "fg" + fg + "seg8level" },
            new double[] { 0, 0.125 / 4095, 0.125 / 4095, 0.125 / 4095, 0.125 / 4095, 0.125 / 4095, 0.125 / 4095, 0.125 / 4095, 0.125 / 4095 },
            new double[] { 0, 1.0 / 128, 1.0 / 128,1.0 / 128,1.0 / 128,1.0 / 128,1.0 / 128,1.0 / 128,1.0 / 128 }
            );
        ((EnvelopeDisplay)comp).setSigned(true);
        ((EnvelopeDisplay)comp).setAxis(0.5);        
        hbox.addLast(comp);
        main.add(hbox);

        category.add(main, BorderLayout.CENTER);
        return category;
        }

    public JComponent addAuxEnvelope(Color color)
        {
        Category category = new Category(this, "Auxillary Envelope", color);
                
        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();

        comp = new LabelledDial("Delay", this, "auxenvdelay", color, 0, 127);
        hbox.add(comp);
                
        comp = new LabelledDial("Attack", this, "auxenvattack", color, 0, 99);
        hbox.add(comp);
                
        comp = new LabelledDial("Hold", this, "auxenvhold", color, 0, 99);
        hbox.add(comp);
                
        comp = new LabelledDial("Decay", this, "auxenvdecay", color, 0, 99);
        hbox.add(comp);
                
        comp = new LabelledDial("Sustain", this, "auxenvsustain", color, 0, 99);
        hbox.add(comp);
                
        comp = new LabelledDial("Release", this, "auxenvrelease", color, 0, 99);
        hbox.add(comp);
 
        comp = new LabelledDial("Amount", this, "auxenvamount", color, -128, 127);
        hbox.add(comp);
                                                
        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "auxenvdelay", "auxenvattack", "auxenvhold", "auxenvdecay", null, "auxenvrelease" },
            new String[] { null, null, null, null, "auxenvsustain", "auxenvsustain", null },
            new double[] { 0, 0.2/127.0, 0.2, 0.2/99.0, 0.2, 0.2, 0.2 },
            new double[] { 0, 0, 1.0, 1.0, 1.0/99.0, 1.0/99.0, 0 },
            new double[] { 0, EnvelopeDisplay.TIME, (Math.PI/4/99.0), EnvelopeDisplay.TIME, (Math.PI/4/99.0), 0, (Math.PI/4/99.0) }
            );
        hbox.addLast(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public String[] getInstruments()
        {
        if (morpheus)
            return MORPHEUS_INSTRUMENTS;
        else
            return ULTRAPROTEUS_INSTRUMENTS;
        }
        
    public JComponent addInstrument(int inst, Color color)
        {
        Category category = new Category(this, "Type", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
 
        params = getInstruments();
        comp = new Chooser("Type", this, "layer" + inst + "instrument", params);
        instrumentChoosers[inst - 1] = (Chooser) comp;
        vbox.add(comp);

        comp = new CheckBox("Reverse", this, "layer" + inst + "soundreverse");
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Sound Start", this, "layer" + inst + "soundstart", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Sound Delay", this, "layer" + inst + "sounddelay", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Volume", this, "layer" + inst + "volume", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Pan", this, "layer" + inst + "pan", color, -7, 7);
        hbox.add(comp);

        comp = new LabelledDial("Detune", this, "layer" + inst + "doubledetune", color, 0, 15)
            {
            public String map(int val)
                {
                if (val == 0) return "Off";
                return "" + val;
                }
            };
        hbox.add(comp);

        category.add(hbox, BorderLayout.WEST);
        return category;
        }
                
    public JComponent addLoop(int inst, Color color)
        {
        Category category = new Category(this, "Loop", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
 
        comp = new CheckBox("Enable", this, "layer" + inst + "loopenable");
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Loop Start", this, "layer" + inst + "loopstartms", color, -999, 999);
        ((LabelledDial)comp).addAdditionalLabel("Coarse");
        hbox.add(comp);

        comp = new LabelledDial("Loop Start", this, "layer" + inst + "loopstartls", color, -999, 999);
        ((LabelledDial)comp).addAdditionalLabel("Fine");
        hbox.add(comp);

        comp = new LabelledDial("Loop Offset", this, "layer" + inst + "lpsizeoffms", color, -999, 999);
        ((LabelledDial)comp).addAdditionalLabel("Coarse");
        hbox.add(comp);

        comp = new LabelledDial("Loop Offset", this, "layer" + inst + "lpsizeoffls", color, -999, 999);
        ((LabelledDial)comp).addAdditionalLabel("Fine");
        hbox.add(comp);

        
        category.add(hbox, BorderLayout.WEST);
        return category;
        }

    public JComponent addInstrumentKey(int inst, Color color)
        {
        Category category = new Category(this, "Keys and Tuning", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
 
        params = SOLO_MODES;
        comp = new Chooser("Solo Mode", this, "layer" + inst + "solomode", params);
        vbox.add(comp);

        params = SOLO_PRIORITIES;
        comp = new Chooser("Solo Priority", this, "layer" + inst + "solopriority", params);
        vbox.add(comp);

        comp = new CheckBox("Nontranspose", this, "layer" + inst + "nontranspose");
        ((CheckBox)comp).addToWidth(2);
        vbox.add(comp);
        hbox.add(vbox);


        comp = new LabelledDial("Low Key", this, "layer" + inst + "lowkey", color, 0, 127)
            {
            public String map(int val)
                {
                return "" + NOTES[val % 12] + "" + (val / 12 + 1);
                }
            };
        hbox.add(comp);
                
        comp = new LabelledDial("Hi Key", this, "layer" + inst + "hikey", color, 0, 127)
            {
            public String map(int val)
                {
                return "" + NOTES[val % 12] + "" + (val / 12 + 1);
                }
            };
        hbox.add(comp);
                
        comp = new LabelledDial("Coarse Tune", this, "layer" + inst + "coarsetune", color, -36, 36);
        hbox.add(comp);

        comp = new LabelledDial("Transpose", this, "layer" + inst + "keyxpose", color, -36, 36);
        hbox.add(comp);

        comp = new LabelledDial("Fine Tune", this, "layer" + inst + "finetune", color, -64, 64);
        hbox.add(comp);

        comp = new LabelledDial("Portamento", this, "layer" + inst + "portamentorate", color, 0, 127)
            {
            public String map(int val)
                {
                if (val == 0) return "Off";
                else return "" + val;
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Rate");
        hbox.add(comp);

        comp = new LabelledDial("Portamento", this, "layer" + inst + "pmentoshape", color, 0, 8)
            {
            public String map(int val)
                {
                if (val == 0) return "Linear";
                else return "Exp+" + val;
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Shape");
        hbox.add(comp);

        category.add(hbox, BorderLayout.WEST);
        return category;
        }


    public JComponent addAlternateEnvelope(int inst, Color color)
        {
        Category category = new Category(this, "Alternate Volume Envelope", color);
                
        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();

        VBox vbox = new VBox();
 
        comp = new CheckBox("Enable", this, "layer" + inst + "altenvenable");
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Attack", this, "layer" + inst + "altenvattack", color, 0, 99);
        hbox.add(comp);
                
        comp = new LabelledDial("Hold", this, "layer" + inst + "altenvhold", color, 0, 99);
        hbox.add(comp);
                
        comp = new LabelledDial("Decay", this, "layer" + inst + "altenvdecay", color, 0, 99);
        hbox.add(comp);
                
        comp = new LabelledDial("Sustain", this, "layer" + inst + "altenvsustain", color, 0, 99);
        hbox.add(comp);
                
        comp = new LabelledDial("Release", this, "layer" + inst + "altenvrelease", color, 0, 99);
        hbox.add(comp);
 
        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "layer" + inst + "altenvattack", "layer" + inst + "altenvhold", "layer" + inst + "altenvdecay", null, "layer" + inst + "altenvrelease" },
            new String[] { null, null, null, "layer" + inst + "altenvsustain", "layer" + inst + "altenvsustain", null },
            new double[] { 0, 0.25, 0.25/99.0, 0.25, 0.25, 0.25 },
            new double[] { 0, 1.0, 1.0, 1.0/99.0, 1.0/99.0, 0 },
            new double[] { 0, (Math.PI/4/99.0), EnvelopeDisplay.TIME, (Math.PI/4/99.0), 0, (Math.PI/4/99.0) }
            );
        hbox.addLast(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addFilter(int inst, Color color)
        {
        Category category = new Category(this, "Filter", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
 
        params = ULTRAPROTEUS_FILTERS;
        comp = new Chooser("Type", this, "layer" + inst + "filttype", params);
        vbox.add(comp);

        comp = new CheckBox("Reverse", this, "layer" + inst + "filtreverse");
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Level", this, "layer" + inst + "filtlevel", color, 0, 255);
        hbox.add(comp);

        comp = new LabelledDial("Morph", this, "layer" + inst + "filtmorph", color, 0, 255);
        hbox.add(comp);

        comp = new LabelledDial("Frequency", this, "layer" + inst + "filtfrqtrk", color, 0, 255);
        ((LabelledDial)comp).addAdditionalLabel("Tracking");
        hbox.add(comp);

        comp = new LabelledDial("Transform 2", this, "layer" + inst + "filttrans2", color, 0, 255);
        hbox.add(comp);

        category.add(hbox, BorderLayout.WEST);
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
        catch (NumberFormatException e) { e.printStackTrace(); }
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
                byte[] data = new byte[] { (byte)0xF0, (byte)0x18, (byte)0x0C, getID(), (byte)0x03, 0, 0, 0, 0, (byte)0xF7 };
                final int PARAM_OFFSET = 8192;
                int param = i + PARAM_OFFSET;
                        
                data[5] = (byte)(param % 128);
                data[6] = (byte)(param / 128);
                        
                int val = (byte)(name.charAt(i));
                if (val < 0) val = val + 16384;
                data[7] = (byte)(val % 128);
                data[8] = (byte)(val / 128);
                ret[i] = data;
                }
            return ret;
            }
        else 
            {            
            byte[] data = new byte[] { (byte)0xF0, (byte)0x18, (byte)0x0C, getID(), (byte)0x03, 0, 0, 0, 0, (byte)0xF7 };
            final int PARAM_OFFSET = 8192;
            int param;
            if (key.startsWith("fg") &&
                (key.endsWith("level") || key.endsWith("type")))
                {
                String base = key.substring(0,7);
                param = ((Integer)(parametersToIndex.get(base + "typelevel"))).intValue();
                }
            else
                {
                param = ((Integer)(parametersToIndex.get(key))).intValue() + PARAM_OFFSET;
                }
                            
            data[5] = (byte)(param % 128);
            data[6] = (byte)(param / 128);
            
            int val = model.get(key, 0);

            if (key.startsWith("fg") &&
                (key.endsWith("level") || key.endsWith("type")))
                {
                String base = key.substring(0,7);
                int level = model.get(base + "level");
                if (level < 0) level += 256;
                                
                val = (model.get(base + "type", 0) << 8) | level;
                }
            else
                {
                if (key.equals("layer1instrument") ||
                    key.equals("layer2instrument"))
                    {
                    if (morpheus)
                        val = MORPHEUS_INSTRUMENT_OFFSETS[val];
                    else
                        val = ULTRAPROTEUS_INSTRUMENT_OFFSETS[val];
                    }
                else if (key.equals("layer1filttype") ||
                    key.equals("layer12filttype"))
                    {
                    if (morpheus)
                        val = MORPHEUS_FILTER_OFFSETS[val];
                    else
                        val = ULTRAPROTEUS_FILTER_OFFSETS[val];
                    }
                }

            if (val < 0) val = val + 16384;
            data[7] = (byte)(val % 128);
            data[8] = (byte)(val / 128);
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
            NN = 126;               // we write to the top patch number of bank 0 (RAM)
            // furthermore we have to change the patch because sendAllParameters
            // doesn't do it by default
            Model model = new Model();
            model.set("bank", 0);
            model.set("number", NN);
            changePatch(model);
            }
        byte[] data = new byte[parameters.length * 2 + 10];
        
        data[0] = (byte)0xF0;
        data[1] = (byte)0x18;
        data[2] = (byte)0x0C;
        data[3] = (byte)getID();
        data[4] = (byte)0x01;
        data[5] = (byte)0x01;
        data[6] = (byte)(NN % 128);
        data[7] = (byte)(NN / 128);
        
        int offset = 8;
        
        //// The E-Mu Morpheus and UltraProteus manuals do NOT say how to calculate
        //// the checksum.  But it appears to be simply the sum of the parameter bytes.
        
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

            if (parameters[i].endsWith("typelevel"))
                {
                String base = parameters[i].substring(0,7);
                int level = model.get(base + "level");
                if (level < 0) level += 256;
                                
                val = (model.get(base + "type", 0) << 8) | level;
                }
            else if (parameters[i].equals("layer1instrument") ||
                parameters[i].equals("layer2instrument"))
                {
                if (morpheus)
                    val = MORPHEUS_INSTRUMENT_OFFSETS[val];
                else
                    val = ULTRAPROTEUS_INSTRUMENT_OFFSETS[val];
                }
            else if (parameters[i].equals("layer1filttype") ||
                parameters[i].equals("layer2filttype"))
                {
                if (morpheus)
                    val = MORPHEUS_FILTER_OFFSETS[val];
                else
                    val = ULTRAPROTEUS_FILTER_OFFSETS[val];
                }
                        
            if (val < 0) val = val + 16384;
            data[offset++] = (byte)(val % 128);
            checksum += data[offset-1];
            data[offset++] = (byte)(val / 128);
            checksum += data[offset-1];
            }
              
        data[offset++] = (byte)(checksum & 127);
        data[offset++] = (byte)0xF7;

        //if (toWorkingMemory || toFile)
        //    {
        Object[] result = new Object[] { data };
        return result;
        //    }
        /*
          else // need to do a write
          {
          Object[] result = new Object[2];
          result[0] = data;
          result[1] = new byte[] { (byte)0xF0, (byte)0x18, (byte)0x0C, (byte)getID(), (byte)0x22, (byte)0, (byte)NN, (byte)0xF7 };
          return result;
          }
        */
        }
        
    public boolean getSendsParametersAfterNonMergeParse() { return true; }


    public int parse(byte[] data, boolean fromFile)
        {
        int NN = data[6] + data[7] * 128;
        model.set("bank", NN / 128);
        model.set("number", NN % 128);
        
        int offset = 8;
        
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

            if (parameters[i].endsWith("typelevel"))
                {
                int type = ((val >> 8) & 3);
                int level = (val & 255);
                if (level > 127)
                    level -= 256;
                String base = parameters[i].substring(0,7) ;
                model.set(base + "type", type);
                model.set(base + "level", level);
                }
            else
                {
                if (parameters[i].endsWith("instrument"))
                    {
                    int v = val;
                    try
                        {
                        if (morpheus)
                            val = ((Integer)(morpheusInstrumentOffsetsToIndex.get(Integer.valueOf(val)))).intValue();
                        else                                            
                            val = ((Integer)(ultraproteusInstrumentOffsetsToIndex.get(Integer.valueOf(val)))).intValue();
                        }
                    catch (Exception ex)
                        {
                        System.err.println("WARNING: Bad instrument offset " + v);
                        val = 0;
                        }
                    }
                else if (parameters[i].endsWith("filttype"))
                    {
                    int v = val;
                    try
                        {
                        if (morpheus)
                            val = ((Integer)(morpheusFilterOffsetsToIndex.get(Integer.valueOf(val)))).intValue();
                        else                                            
                            val = ((Integer)(ultraproteusFilterOffsetsToIndex.get(Integer.valueOf(val)))).intValue();
                        }
                    catch (Exception ex)
                        {
                        System.err.println("WARNING: Bad filter type " + v);
                        val = 0;
                        }
                    }
                        
                if (!parameters[i].equals("---"))
                    model.set(parameters[i], val);
                }
            }
                            
        revise();
        return PARSE_SUCCEEDED;
        }
    
        
    public void changePatch(Model tempModel)
        {
        int bank = tempModel.get("bank", 0);
        if (bank == 2)  // card
            bank++;  // because the card's actual bank is bank 3
        int number = tempModel.get("number", 0);
        // It appears that the Morpheus / Ultraproteus are among
        // the rare machines which use 14-bit CCs
        tryToSendMIDI(buildLongCC(getChannelOut(), 0x00, bank));
        tryToSendMIDI(buildPC(getChannelOut(), number));
        }

    public byte[] requestDump(Model tempModel)
        {
        int NN = tempModel.get("bank", 0) * 128 + tempModel.get("number", 0);
        byte[] data = new byte[8];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x18;
        data[2] = (byte)0x0C;
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
        transmitTo.setEnabled(false);  // can't send to a given patch
        return frame;
        }
                
    public static boolean recognize(byte[] data)
        {
        return  data.length == 10 + parameters.length * 2 &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte) 0x18 &&
            data[2] == (byte) 0x0C &&
            data[4] == (byte) 0x01 &&
            data[5] == (byte) 0x01;
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
        return morpheus ? "E-Mu Morpheus" : "E-Mu Ultra Proteus"; 
        }
        
    public static String getSynthName() { return "E-Mu Morpheus / Ultra Proteus"; }
    public String getPatchName(Model model) { return model.get("name", "Untitled"); }
    
    public Model getNextPatchLocation(Model model)
        {
        int bank = model.get("bank");
        int number = model.get("number");
        
        number++;
        if (number >= 128)
            {
            bank++;
            number = 0;
            if (bank >= 3)
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
    HashMap parametersToIndex = new HashMap();


    /** List of all Emu Morpheus parameters in order. */
                
    final static String[] parameters = new String[]
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
    "presetlowkey",
    "presethikey",
    "bendrange",
    "velocitycurve",
    "keyboardcenter",
    "tunetable",
    "mixbus",
    "portmode",
    "xfademode",
    "xfadedirection",
    "xfadebalance",
    "xfadeamount",
    "xswitchpoint",
    "noteon1source",
    "noteon1dest",
    "noteon1amount",
    "noteon2source",
    "noteon2dest",
    "noteon2amount",
    "noteon3source",
    "noteon3dest",
    "noteon3amount",
    "noteon4source",
    "noteon4dest",
    "noteon4amount",
    "noteon5source",
    "noteon5dest",
    "noteon5amount",
    "noteon6source",
    "noteon6dest",
    "noteon6amount",
    "noteon7source",
    "noteon7dest",
    "noteon7amount",
    "noteon8source",
    "noteon8dest",
    "noteon8amount",
    "noteon9source",
    "noteon9dest",
    "noteon9amount",
    "noteon10source",
    "noteon10dest",
    "noteon10amount",
    "realtime1source",
    "realtime1dest",
    "realtime1amount",
    "realtime2source",
    "realtime2dest",
    "realtime2amount",
    "realtime3source",
    "realtime3dest",
    "realtime3amount",
    "realtime4source",
    "realtime4dest",
    "realtime4amount",
    "realtime5source",
    "realtime5dest",
    "realtime5amount",
    "realtime6source",
    "realtime6dest",
    "realtime6amount",
    "realtime7source",
    "realtime7dest",
    "realtime7amount",
    "realtime8source",
    "realtime8dest",
    "realtime8amount",
    "realtime9source",
    "realtime9dest",
    "realtime9amount",
    "realtime10source",
    "realtime10dest",
    "realtime10amount",
    "foot1dest",
    "foot2dest",
    "foot3dest",
    "ctrlaamount",
    "ctrlbamount",
    "ctrlcamount",
    "ctrldamount",
    "pressureamount",
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
    "fg1amount",
    "fg1seg1typelevel",
    "fg1seg1time",
    "fg1seg1shape",
    "fg1seg1condjump",
    "fg1seg1condval",
    "fg1seg1destseg",
    "fg1seg2typelevel",
    "fg1seg2time",
    "fg1seg2shape",
    "fg1seg2condjump",
    "fg1seg2condval",
    "fg1seg2destseg",
    "fg1seg3typelevel",
    "fg1seg3time",
    "fg1seg3shape",
    "fg1seg3condjump",
    "fg1seg3condval",
    "fg1seg3destseg",
    "fg1seg4typelevel",
    "fg1seg4time",
    "fg1seg4shape",
    "fg1seg4condjump",
    "fg1seg4condval",
    "fg1seg4destseg",
    "fg1seg5typelevel",
    "fg1seg5time",
    "fg1seg5shape",
    "fg1seg5condjump",
    "fg1seg5condval",
    "fg1seg5destseg",
    "fg1seg6typelevel",
    "fg1seg6time",
    "fg1seg6shape",
    "fg1seg6condjump",
    "fg1seg6condval",
    "fg1seg6destseg",
    "fg1seg7typelevel",
    "fg1seg7time",
    "fg1seg7shape",
    "fg1seg7condjump",
    "fg1seg7condval",
    "fg1seg7destseg",
    "fg1seg8typelevel",
    "fg1seg8time",
    "fg1seg8shape",
    "fg1seg8condjump",
    "fg1seg8condval",
    "fg1seg8destseg",
    "fg2amount",
    "fg2seg1typelevel",
    "fg2seg1time",
    "fg2seg1shape",
    "fg2seg1condjump",
    "fg2seg1condval",
    "fg2seg1destseg",
    "fg2seg2typelevel",
    "fg2seg2time",
    "fg2seg2shape",
    "fg2seg2condjump",
    "fg2seg2condval",
    "fg2seg2destseg",
    "fg2seg3typelevel",
    "fg2seg3time",
    "fg2seg3shape",
    "fg2seg3condjump",
    "fg2seg3condval",
    "fg2seg3destseg",
    "fg2seg4typelevel",
    "fg2seg4time",
    "fg2seg4shape",
    "fg2seg4condjump",
    "fg2seg4condval",
    "fg2seg4destseg",
    "fg2seg5typelevel",
    "fg2seg5time",
    "fg2seg5shape",
    "fg2seg5condjump",
    "fg2seg5condval",
    "fg2seg5destseg",
    "fg2seg6typelevel",
    "fg2seg6time",
    "fg2seg6shape",
    "fg2seg6condjump",
    "fg2seg6condval",
    "fg2seg6destseg",
    "fg2seg7typelevel",
    "fg2seg7time",
    "fg2seg7shape",
    "fg2seg7condjump",
    "fg2seg7condval",
    "fg2seg7destseg",
    "fg2seg8typelevel",
    "fg2seg8time",
    "fg2seg8shape",
    "fg2seg8condjump",
    "fg2seg8condval",
    "fg2seg8destseg",
    "auxenvdelay",
    "auxenvattack",
    "auxenvhold",
    "auxenvdecay",
    "auxenvsustain",
    "auxenvrelease",
    "auxenvamount",
    "layer1instrument",
    "layer1lowkey",
    "layer1hikey",
    "layer1volume",
    "layer1pan",
    "layer1coarsetune",
    "layer1keyxpose",
    "layer1finetune",
    "layer1altenvenable",
    "layer1altenvattack",
    "layer1altenvhold",
    "layer1altenvdecay",
    "layer1altenvsustain",
    "layer1altenvrelease",
    "layer1loopenable",
    "layer1loopstartms",
    "layer1loopstartls",
    "layer1lpsizeoffms",
    "layer1lpsizeoffls",
    "layer1soundstart",
    "layer1sounddelay",
    "layer1solomode",
    "layer1solopriority",
    "layer1portamentorate",
    "layer1pmentoshape",
    "layer1doubledetune",
    "layer1soundreverse",
    "layer1nontranspose",
    "layer1filttype",
    "layer1filtreverse",
    "layer1filtlevel",
    "layer1filtmorph",
    "layer1filtfrqtrk",
    "layer1filttrans2",
    "---",
    "layer2instrument",
    "layer2lowkey",
    "layer2hikey",
    "layer2volume",
    "layer2pan",
    "layer2coarsetune",
    "layer2keyxpose",
    "layer2finetune",
    "layer2altenvenable",
    "layer2altenvattack",
    "layer2altenvhold",
    "layer2altenvdecay",
    "layer2altenvsustain",
    "layer2altenvrelease",
    "layer2loopenable",
    "layer2loopstartms",
    "layer2loopstartls",
    "layer2lpsizeoffms",
    "layer2lpsizeoffls",
    "layer2soundstart",
    "layer2sounddelay",
    "layer2solomode",
    "layer2solopriority",
    "layer2portamentorate",
    "layer2pmentoshape",
    "layer2doubledetune",
    "layer2soundreverse",
    "layer2nontranspose",
    "layer2filttype",
    "layer2filtreverse",
    "layer2filtlevel",
    "layer2filtmorph",
    "layer2filtfrqtrk",
    "layer2filttrans2",
    "---"
    };
    

    public static final String[] BANKS = new String[] { "RAM", "ROM", "Card" };
    public static final String[] NOTES = new String[] { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
    public static final String[] TUNE_TABLES = new String[] { "Equal", "Just C", "Vallotti", "19-Tone", "Gamelan", "User" };
    public static final String[] MIX_BUS = new String[] { "Main", "Sub1", "FXA", "FXB" };
    public static final String[] XFADE_MODES = new String[] { "Off", "XFade", "XSwitch" };
    public static final String[] XFADE_DIRECTIONS = new String[] { "Pri->Sec", "Sec->Pri" };
    public static final String[] LFO_SHAPES = new String[] { "Rand", "Tri", "Sine", "Saw", "Square" };
    public static final String[] SOLO_MODES = new String[] { "Off", "Wind", "Synth" };
    public static final String[] SOLO_PRIORITIES = new String[] { "Hi", "Low", "First", "Last", "Drum" };
    public static final String[] NOTE_ON_PATCHCORD_SOURCES = new String[]
    {
    "Key",
    "Velocity",
    "Pitch Bend",
    "Ctrl A",
    "Ctrl B",
    "Ctrl C",
    "Ctrl D",
    "Mono Press.",
    "Free FuncGen"
    };
        

    public static final String[] REALTIME_PATCHCORD_SOURCES = new String[]
    {
    "Pitch Bend",
    "Ctrl A",
    "Ctrl B",
    "Ctrl C",
    "Ctrl D",
    "Mono Press.",
    "Poly Press.",
    "LFO 1",
    "LFO 2",
    "Aux Env",
    "FuncGen 2",
    "FuncGen 1",
    "Free FuncGen"
    };
        

    public static final String[] PATCHCORD_DESTINATIONS = new String[]
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
    "Start [N]",
    "StartP [N]",
    "StartS [N]",
    "Pan",
    "PanP",
    "PanS",
    "Tone [N]",
    "ToneP [N]",
    "ToneS [N]",
    "Morph",
    "MorphP",
    "MorphS",
    "Trans [N]",
    "Trans2P [N]",
    "Trans2S [N]",
    "PortRt",
    "PortRtP",
    "PortRtS",
    "FG1Amt",
    "FG2Amt",
    "FltLev [N]",
    "FltLevP [N]",
    "FltLevS [N]",
    "FreqTrk [N]",
    "FreqTrkP [N]",
    "FreqTrkS [N]"
    };
        

    public static final String[] FUNCTION_GENERATORS = new String[]
    {
    "Linear",
    "Expo+1",
    "Expo+2",
    "Expo+3",
    "Expo+4",
    "Expo+5",
    "Expo+6",
    "Expo+7",
    "Circ1.4",
    "Circ1.6",
    "Circ1.8",
    "Cir1.16",
    "Squeeze",
    "FastLn1",
    "FastLn2",
    "FastLn3",
    "MedLn1",
    "MedLn2",
    "SlwRmp1",
    "SlwRmp2",
    "Bloom",
    "Bloom2",
    "Cr1.16R",
    "Cir1.8R24",
    "Cir1.6R",
    "Cir1.4R",
    "SlwCrv1",
    "SlwCrv2",
    "DelayDC",
    "DCdelay",
    "Curve2X",
    "Curv2XB",
    "Curv2XC",
    "ZizZag1",
    "ZizZag2",
    "ZizZag3",
    "Chaos03",
    "Chaos06",
    "Chaos12",
    "Chaos16",
    "Chaos25",
    "Chaos33",
    "Chaos37",
    "Chaos50",
    "Chaos66",
    "Chaos75",
    "Chaos95",
    "Chaos99",
    "LinShfl",
    "LnShfl2",
    "RandomA",
    "RandomB",
    "RandomC",
    "RandomD",
    "RandomE",
    "RandomF",
    "RandomG",
    "RandomH",
    "RandomI",
    "RandomJ",
    "RandomK",
    "RandomL",
    "RandomZ"
    };

    public static final String[] FUNCTION_GENERATOR_CONDITIONS = new String[]
    {
    "Never",
    "AlwaysEnd",
    "NoteOnEnd",
    "NoteOnImm",
    "NoteOffEnd",
    "NoteOffImm",
    "LFO1End",
    "LFO2End",
    "Foot1End",
    "Foot1Imm",
    "Foot2End",
    "Foot2Imm",
    "Foot3End",
    "Foot3Imm",
    "VelEnd",
    "KeyEnd"
    };
        
    public static final String[] FUNCTION_GENERATOR__TYPES = new String[] { "Absolute", "Delta", "Rnd Absolute", "Rnd Delta" };
        
    public static final String[] ULTRAPROTEUS_FILTERS = new String[]
    {
    "No Filter",
    "Low Pass Flange .4",
    "Low Pass Flange Bk .4",
    "Flange 2 .4",
    "CubeFlanger",
    "Flange3.4",
    "Flange 4.4",
    "Flange 5",
    "Flange 6.4",
    "Flange 6R.4",
    "Flanger 7.4",
    "BriteFlnge.4",
    "Flng>Flng1",
    "0>Flng2",
    "Flng>Flng3b",
    "Flng>Flng4",
    "Flng>Flng5",
    "Flng>Flng6",
    "Flng>FlngT",
    "Flng>FlngC",
    "CO>FlngT",
    "AEParLPVow",
    "AEParaVowel",
    "AOLpParaVow",
    "AOParaVowel",
    "AUParaVow.4",
    "UOParaVow.4",
    "SftEOVowel4",
    "SoftEOAE",
    "Vocal Cube",
    "C1-6 Harms4",
    "Voce.4",
    "ChoralComb4",
    "Bassutoi.4",
    "Be Ye.4",
    "Ee-Yi.4",
    "Ii-Yi.4",
    "Uhrrrah.4",
    "YeahYeah.4",
    "Vow>Vow1",
    "Vow>Vow2",
    "YahYahs.4",
    "YoYo.4",
    "VowelSpace",
    "BrickWaLP.4",
    "BrickWaLP2",
    "MdQ 2PoleLP",
    "HiQ 2PoleLP",
    "MdQ 4PoleLP",
    "HiQ 4PoleLP",
    "2poleLoQLP4",
    "4 PoleLoQ.4",
    "4PoleMidQ.4",
    "2p>4p 0",
    "LowPassPlus",
    "Low Past.4",
    "APass.4",
    "HPSweep.4",
    "HiSwept1.4",
    "HghsSwpt2.4",
    "HighAccent.4",
    "HiPassSweep.4",
    "Deep Combs",
    "One Peak",
    "More Peaks",
    "Rev Peaks",
    "Notcher 2.4",
    "Ntches2Oct4",
    "Odd>+",
    "VarSlope.4",
    "BassEQ 1.4",
    "B BOOST.4",
    "BssBOOST2.4",
    "BassDrumEQ",
    "Snare LPEQ2",
    "HiHatLPEQ",
    "HP LP PZ",
    "PZ Notch",
    "Band-aid",
    "LowQHiQ",
    "Wah4Vib.4",
    "WaWa",
    "BrassRez.4",
    "Clr>Oboe",
    "0>Muter",
    "PZ Syn Horn",
    "HP Brass",
    "BrassyBlast",
    "BrassSwell",
    "BrsSwell2.4",
    "LoVelTrum",
    "Chiffin.4",
    "ShakuFilter",
    "Cym",
    "VelMarim",
    "EZ Vibez.4",
    "Piano 01",
    "PianoFltr4",
    "EZ Rhodez4",
    "MoogVocodr4",
    "MoogVocSwp",
    "StrngThing4",
    "StrSweep.4",
    "BassXpress",
    "Qbase.4",
    "AcGtrRs.4",
    "Tube Sust.4",
    "GtrSkwk",
    "GuitXpress",
    "FG MajTrans4",
    "Tam",
    "HOTwell.4",
    "Bell.Waha",
    "Belwahb",
    "0>BellMorph:",
    "0>Bell+",
    "Bel>Crs>Bel",
    "Chrs>Flng1",
    "Chrs>Flng2",
    "Ev/OdNtch.4",
    "Odd/EvnNtch",
    "NotchPkSwp4",
    "1.5/3KNBPR4",
    "Swingshift",
    "500up.4",
    "C1Harmonic5oct.8R",
    "Odd>Even",
    "0>Shp1",
    "0>Shp2",
    "0>Shp3",
    "Shp>Shp1",
    "Lpeq Vel",
    "CleanSweep4",
    "PowerSweeps",
    "TSweep.4",
    "SweepHiQ1.4",
    "V>FcQuad.4",
    "Nexus.4",
    "Krators.4",
    "Harmonix.4",
    "GreenWorld4",
    "Comb/Swap.4",
    "Comb/HP.4",
    "Swirly",
    "Cavatate.4",
    "GentleRZ4",
    "Bendup/Swap",
    "Bendup",
    "SKWEEZIT",
    "Lo/High4",
    "SbtleMvmnt4",
    "Buzzy Pad.4",
    "Bw5kHz+6.4",
    "Bw65Hz/2k.4",
    "Bb80Hzbw1.4",
    "HighsTwist4",
    "Cubix",
    "Intervallc4",
    "EvenCuts4",
    "OddCuts.4",
    "PWMtrans.4",
    "HiEndQ.4",
    "BroadRes.4",
    "RubberHose4",
    "HeeghCube",
    "NoizCube",
    "VelctyTilt",
    "SynthWow4",
    "CntrySweep4",
    "Diffuser4",
    "MdlySweep4",
    "StrongShimr",
    "Acc.Vel-1",
    "Acc.Vel-2",
    "Vel2-Wind",
    "Harmo",
    "Start>EndA",
    "Start>EndB",
    "MovingPick1",
    "MovingPick2",
    "Mph+Trns1",
    "0>Odds",
    "Comb Voices",
    "Odd-Ev Hrm",
    "OddHrm+rez",
    "EvnHrm+rez",
    "MellowPeaks",
    "AHmBnd.4",
    "Vintage",
    "MildQPole",
    "Bonk>CO",
    "Speaker",
    "Expander",
    "Separator",
    "MildPolSwap",
    "PoleCross.4",
    "ApDistB6.4",
    "TubeJam.4",
    "[UP] Poles 1-7",
    "[UP] 2 Poles",
    "[UP] 4 Poles A",
    "[UP] 4 Poles B",
    "[UP] 4 Poles C",
    "[UP] 6 Poles",
    "[UP] Multipole",
    "[UP] Tracker",
    "[UP] Tracker 2",
    "[UP] Tracker 3",
    "[UP] Tracker5ths",
    "[UP] InFifths.4",
    "[UP] HarmTracker",
    "[UP] 1BndPrmtrcA",
    "[UP] 1BndPrmtrcB",
    "[UP] 1BndPrmtrcC",
    "[UP] 1BndPrmtrcD",
    "[UP] MultiMetricA",
    "[UP] MultiMetricB",
    "[UP] MultiMetricC",
    "[UP] Omni Metric",
    "[UP] HrmncPeaks",
    "[UP] HrmncPeaks2",
    "[UP] InHarMetric",
    "[UP] HarmShifter",
    "[UP] HarmShiftr2",
    "[UP] HarmShiftr3",
    "[UP] Phaser",
    "[UP] HrmncPhaser",
    "[UP] HrmncPhsr2",
    "[UP] Wide Bands",
    "[UP] New Boost",
    "[UP] Vowel Space2",
    "[UP] Vowel Space3",
    "[UP] Oh Shaper",
    "[UP] Ah Shaper",
    "[UP] Oo Shaper",
    "[UP] FrmntShaper",
    "[UP] Piano LP.4",
    "[UP] PianoSndBrd",
    "[UP] Strike Cube",
    "[UP] Clav Curves",
    "[UP] Symphony",
    "[UP] String Cube",
    "[UP] Swell Cube",
    "[UP] Quartet.4",
    "[UP] Mellotron.4",
    "[UP] Cello",
    "[UP] Key Squeak",
    "[UP] Pluck.4",
    "[UP] Pick It",
    "[UP] El Pick It",
    "[UP] ElGuit Cube",
    "[UP] ElGuit Pick",
    "[UP] Breather 1",
    "[UP] Breather 2",
    "[UP] WindNoise 1",
    "[UP] WindNoise 2",
    "[UP] Blow Cube",
    "[UP] Overblow",
    "[UP] Wind Filter",
    "[UP] FlutBreth.4",
    "[UP] VClarinet.4",
    "[UP] New Mute.4",
    "[UP] Trempeto.4",
    "[UP] SfBrzando.4",
    "[UP] UduFilter.4",
    "[UP] Hip Kick",
    "[UP] Cymbal Cube",
    "[UP] ChimeFlange",
    "[UP] CableRing.4",
    "[UP] TensionWire",
    "[UP] Auto Clang",
    "[UP] Dragon Claw",
    "[UP] Spectra",
    "[UP] RippleSheet",
    "[UP] Clear Water",
    "[UP] Invisible",
    "[UP] Wine Glass",
    "[UP] SnakeCros.4",
    "[UP] Analog.4",
    "[UP] Skrtch Cube",
    "[UP] KitchenSink",
    "[UP] Head Pan 1",
    "[UP] Head Pan 2",
    "[UP] Arpeggio",
    "[UP] HeavyFilt.4",
    "[UP] Noiz Cube 2",
    "[UP] Feedback",
    "[UP] AllPoleDist",
    "[UP] AllPoleDst2"
    };   

    
    public static final String[] ULTRAPROTEUS_INSTRUMENTS = new String[] 
    {
    "Stereo Grand",
    "Stereo Slack",
    "Stereo Tight",
    "Mono Grand",
    "Mono Slack",
    "Mono Tight",
    "Piano Pad",
    "Organ",
    "Tine Strike",
    "P1 AcGuitar",
    "P3 AcGuitar",
    "Rock Bass 1",
    "Rock Bass 2",
    "Rock Bass 3",
    "Alt Bass 1",
    "Alt Bass 2",
    "Alt Bass 3",
    "Alt Bass 4",
    "Alt Bass 5",
    "Alt Bass 6",
    "Alt Bass 7",
    "Bass Syn 1",
    "Bass Syn 2",
    "Funk Slap",
    "Funk Pop",
    "Harmonics",
    "Bass/Harms1",
    "Bass/Harms2",
    "Bass/Organ",
    "Guitar Mute",
    "Guitar",
    "Guit A",
    "Guit B",
    "Guit C",
    "Guit D",
    "Guit E",
    "Solo Cello",
    "Solo Viola",
    "Solo Violin",
    "Pizz Basses",
    "Pizz Celli",
    "Pizz Violas",
    "Pizz Violns",
    "Pizz Combos",
    "Single Pizz",
    "P1 Strings",
    "Long Strings",
    "Slow Strings",
    "P2 Strings 1",
    "P2 Strings 2",
    "P2 Strings 3",
    "Quartet 1",
    "Quartet 2",
    "Quartet 3",
    "Quartet 4",
    "Gambambo",
    "Arco Basses",
    "Arco Celli",
    "Arco Violas",
    "ArcoViolins",
    "Dark Basses",
    "Dark Celli",
    "Dark Violas",
    "Dark Violins",
    "Tremolande",
    "Trem Bass",
    "Trem Violin",
    "Troubador",
    "Troubador A",
    "Troubador B",
    "Troubador C",
    "Troubador D",
    "Irish Harp",
    "Irish Harp A",
    "Irish Harp B",
    "Irish Harp C",
    "Dulcimer",
    "Dulc A",
    "Dulc B",
    "Dulc C",
    "Koto",
    "Koto A",
    "Koto B",
    "Koto C",
    "Banjo",
    "Banjo A",
    "Banjo B",
    "Banjo C",
    "Hi Tar",
    "Sitar",
    "Tambura",
    "Tamb/Sitar",
    "Renaissance",
    "Psaltry",
    "Waterphone 1",
    "Waterphone 2",
    "Bari Sax",
    "Tenor Sax",
    "Alto Sax",
    "Dark Sax",
    "Tuba",
    "Trumpet 1 mf",
    "Trumpet 1 ff",
    "Trumpet 2 mf",
    "Trumpet 2 ff",
    "Trumpet 3",
    "MuteTrumpet",
    "Horn Falls",
    "Dark Trumpet mf",
    "Dark Trumpet ff",
    "Trombone 1",
    "Hi Trombone",
    "Lo Trombone",
    "Dark T-bone",
    "French Horn mf",
    "French Horn ff",
    "Bone/Tpt",
    "Bone/Sax",
    "Brass mf",
    "Brass ff",
    "Flute Attack",
    "Soft Flute",
    "Flute Vib",
    "Flute NoVib",
    "P2 AltFlute",
    "Piccolo",
    "DarkPiccolo",
    "Clarinet",
    "Clarinet",
    "B.Clar/Clar",
    "Cntrabssoon",
    "Bassoon",
    "EnglishHorn",
    "Oboe",
    "Long Oboe",
    "Alt. Oboe",
    "WoodWinds",
    "Accordion",
    "Harmonica",
    "Vb Harmonica",
    "Folk America",
    "Bagpipe Drone",
    "Chanter A",
    "Chanter B",
    "Dron/Chant A",
    "Dron/Chant B",
    "Mizmars",
    "Shenai",
    "PenWhistle",
    "Ocarina",
    "Siku",
    "Siku A",
    "Siku B",
    "Siku C",
    "Shakuhachi",
    "Ney Flute",
    "Shofars",
    "Shofar A",
    "Shofar B",
    "Roar/Catch",
    "Bull Roarer",
    "Spirit Catcher",
    "Didgeridoo",
    "Didgeridoo A",
    "Didgeridoo B",
    "Didgeridoo C",
    "Jews Harp",
    "Jews Harp A",
    "Jews Harp B",
    "Jews Harp C",
    "Jews Harp D",
    "Down Under",
    "Airy Voices",
    "Dark Voices",
    "Voice A",
    "Voice B",
    "Voice C",
    "Voice D",
    "Voice E",
    "Voice F",
    "Voice G",
    "Voice H",
    "Rock Perc 1",
    "Rock Perc 2",
    "Standard 1",
    "Standard 2",
    "Standard 3",
    "All 808",
    "Use Fists",
    "Rock Perc FX 1",
    "Rock Perc FX 2",
    "Metal Perc",
    "G. MIDI 1",
    "G. MIDI 2",
    "Kicks",
    "Kick A",
    "Kick B",
    "Kick C",
    "Snares",
    "Stereo Snare",
    "Snare B",
    "Snare C",
    "Snare D",
    "Click Snare",
    "Toms",
    "Tom A",
    "Tom B",
    "Cymbals",
    "Hat A",
    "Hat B",
    "Hat C",
    "Ride Cymbal",
    "Crash Cymbal",
    "BD/Tom 808",
    "Snare 808",
    "Cowbell 808",
    "Click 808",
    "Claps 808",
    "Cymbals 808",
    "Orch Perc 1",
    "Orch Perc 2",
    "Low Perc 2",
    "High Perc 2",
    "Orch Perc 3",
    "AllPercWKey",
    "Special FX 1",
    "Special FX 2",
    "Special FX 3",
    "Timpani",
    "Bass Drum",
    "Snare",
    "Piatti",
    "Tamborine",
    "Temple Block",
    "Tam Tam",
    "Castanet",
    "Marimba",
    "Vibraphone",
    "Xylophone",
    "Celesta",
    "Triangle",
    "Glocknspiel",
    "TubularBell",
    "Tmp/Tubular",
    "Latin Drums",
    "Latin Perc",
    "Maracas",
    "The Tabla",
    "Bata Drums",
    "Udu Drum",
    "Surdo Drum",
    "Wood Drum",
    "Log Drum",
    "Plexitones",
    "Steel Drum",
    "Gamelan",
    "Seribu Pulau",
    "World Perc 1",
    "World Perc 2",
    "World Perc LR",
    "M.EastCombi",
    "East Indian",
    "Agogo",
    "P1 Wood Block",
    "P3 Wood Block",
    "Conga",
    "Hi Tumba Tone",
    "HTmbaOpSlap",
    "Open Hand Tone",
    "Timbale",
    "Timbale Rim",
    "Fingers",
    "Cabasa",
    "Guiro Down",
    "Guiro Up",
    "Maraca A",
    "Maraca B",
    "Maraca C",
    "Maraca D",
    "Bata Ipu Tone",
    "Bata Ipu Slap",
    "Bata Enu Tone",
    "Bata Hi Tone",
    "Bata Hi Mute",
    "Bata HiSlap",
    "Tabla Tone",
    "Tabla Mute A",
    "Tabla Mute B",
    "Tabla Mute C",
    "Tabla Open",
    "Udu Tone",
    "Udu Release",
    "Udu Finger",
    "Udu Slap",
    "Surdo Open",
    "Surdo Mute",
    "Deff Slap",
    "Deff Mute",
    "Crickets",
    "Bayan Tone",
    "Bayan Slap",
    "Bayan Hit",
    "Clapper Stick",
    "Rosewood Bass",
    "Rosewood Tick",
    "Rosewood Harm",
    "Rosewood Finger",
    "Tanz Shaker",
    "Hula Stick",
    "Buzz Likembe",
    "Likembe",
    "Likembe Buzz",
    "Bendir",
    "Req Open",
    "Req Slap",
    "Plexi-Tone",
    "Plexi-Slap A",
    "Plexi-Slap B",
    "Plexi-Slap C",
    "Bonang",
    "Kenong",
    "Saron",
    "Rubbed Bonang",
    "Rubbed Kenong",
    "Rubbed Saron",
    "Bonang Kenong",
    "Kenong Bonang",
    "China Gong",
    "Rubbed Gong",
    "Nepal Cymbal",
    "Tibetan Bowl",
    "Rubbed Bowl",
    "Crotales",
    "Rubbed Crotales",
    "Suwuk Gong",
    "Rubbed Suwuk",
    "Synth Pad",
    "Med. Pad",
    "Long Pad",
    "Dark Pad",
    "Kaleidoscope",
    "Multi-Form",
    "Prophet Tone",
    "Noise Non-X",
    "Oct 1 Sine",
    "Oct 2 All",
    "Oct 3 All",
    "Oct 4 All",
    "Oct 5 All",
    "Oct 6 All",
    "Oct 7 All",
    "Oct 2 Odd",
    "Oct 3 Odd",
    "Oct 4 Odd",
    "Oct 5 Odd",
    "Oct 6 Odd",
    "Oct 7 Odd",
    "Oct 2 Even",
    "Oct 3 Even",
    "Oct 4 Even",
    "Oct 5 Even",
    "Oct 6 Even",
    "Oct 7 Even",
    "Low Odds",
    "Low Evens",
    "FourOctaves",
    "Square",
    "SquareChrs1",
    "SquareChrs2",
    "Sawtooth",
    "Filter Saw",
    "Sawstack",
    "Dark Stack",
    "Triangle",
    "Ramp",
    "Evens Only",
    "Odds Gone",
    "Fund Gone 1",
    "Fund Gone 2",
    "Moog Saw 1",
    "Moog Saw 2",
    "Moog Saw 3",
    "Moog Saw 4",
    "Moog Sqr 1",
    "Moog Sqr 2",
    "Moog Sqr 3",
    "Moog Sqr 4",
    "Moog Sqr 5",
    "Moog Sqr 6",
    "Moog Rect 1",
    "Moog Rect 2",
    "Moog Rect 3",
    "Moog Rect 4",
    "Moog Rect 5",
    "Moog Pulse1",
    "Moog Pulse2",
    "Moog Pulse3",
    "Moog Pulse4",
    "Moog Pulse5",
    "Ob Wave 1",
    "Ob Wave 2",
    "Ob Wave 3",
    "Ob Wave 4",
    "Ob Wave 5",
    "ARP 2600 1",
    "ARP 2600 2",
    "ARP 2600 3",
    "B3 Wave 1",
    "B3 Chrs 1",
    "B3 Wave 2",
    "B3 Chrs 2",
    "B3 Wave 3",
    "B3 Wave 4",
    "B3 Wave 5",
    "B3 Wave 6",
    "ARPClarinet",
    "ARP Bassoon",
    "Synth Cyc 1",
    "Synth Cyc 2",
    "Synth Cyc 3",
    "Synth Cyc 4",
    "Bite Cyc",
    "Buzzy Cyc",
    "Metlphone 1",
    "Metlphone 2",
    "Metlphone 3",
    "Metlphone 4",
    "Duck Cyc 1",
    "Duck Cyc 2",
    "Duck Cyc 3",
    "Wind Cyc 1",
    "Wind Cyc 2",
    "Wind Cyc 3",
    "Wind Cyc 4",
    "Organ Cyc 1",
    "Organ Cyc 2",
    "Vio Essence",
    "Buzzoon",
    "Brassy Wave",
    "Reedy Buzz",
    "Growl Wave",
    "HarpsiWave",
    "Fuzzy Gruzz",
    "Power 5ths",
    "Ice Bell",
    "IceBellChrs",
    "Bronze Age",
    "Iron Plate",
    "Aluminum",
    "Lead Beam",
    "SteelXtract",
    "WinterGlass",
    "Town Bell",
    "Orch Bells",
    "Tubular SE",
    "Soft Bell",
    "Swirly",
    "Tack Attack",
    "ShimmerWave",
    "Mild Tone",
    "Ah Wave",
    "Vocal Wave",
    "Fuzzy Clav",
    "Electrhode",
    "Whine",
    "Glass Chrs1",
    "Glass Chrs2",
    "Bell Chorus",
    "FM ToneChrs",
    "Oooohgan"
    };

    public static final String[] MORPHEUS_INSTRUMENTS = new String[]
    {
    "Dance Drums 1",
    "Dance Drums 2",
    "Dancer Synth",
    "Punchy Brass",
    "Spectrum Pad",
    "Vox Choir",
    "Jupiter String",
    "Orchestral String 1",
    "Orchestral String 2",
    "Acoustic Guitar",
    "Acoustic Guitar 2",
    "Acoustic Guitar 3",
    "Silver Flute",
    "Tenor Sax 1",
    "Tenor Sax 2",
    "Bari Sax",
    "Soprano Sax",
    "Alto Sax",
    "Metal String",
    "Gutt Bass",
    "Upright Bass",
    "Upright Bass 2",
    "Fretless",
    "Quick Bass",
    "Cool Vibe",
    "Bright Vibes",
    "Vibe Stik",
    "Crotales",
    "Peking",
    "Java Malet",
    "Kenong",
    "Icy Pad",
    "Bright Electric Piano",
    "Piano Bright",
    "Piano Medium 1",
    "Piano Medium 2",
    "Piano Dark",
    "Rock Organ",
    "Clean Strat",
    "Harmonics",
    "Bass Harmonic",
    "Electric Bass 1",
    "Electric Bass 2",
    "Electric Bass 3",
    "Electric Bass 4",
    "Electric Bass 5",
    "Electric Bass 6",
    "Electric Bass 7",
    "Sawtooth",
    "Square",
    "Pulse 50%",
    "Pulse 75%",
    "Pulse 90%",
    "Pulse 94%",
    "Pulse 96%",
    "Pulse 98%",
    "Pulse 99%",
    "Noise",
    "Oct 3rd White Noise",
    "Triangle",
    "Oct 1 (Sine)",
    "Oct 2 All",
    "Oct 3 All",
    "Oct 4 All",
    "Oct 5 All",
    "Oct 6 All",
    "Oct 7 All",
    "Oct 2 Odd",
    "Oct 3 Odd",
    "Oct 4 Odd",
    "Oct 5 Odd",
    "Oct 6 Odd",
    "Oct 7 Odd",
    "Oct 2 Even",
    "Oct 3 Even",
    "Oct 4 Even",
    "Oct 5 Even",
    "Oct 6 Even",
    "Oct 7 Even",
    "Low Odds",
    "Low Evens",
    "Four Octaves",
    "B3 Wave 1",
    "B3 Wave 2",
    "B3 Wave 4",
    "B3 Wave 5",
    "B3 Wave 6",
    "Organ Wave 1",
    "Organ Wave 2",
    "Barber Pole",
    "Synth Cycle 1",
    "Synth Cycle 2",
    "Synth Cycle 3",
    "Synth Cycle 4",
    "Fundamental Gone 1",
    "Fundamental Gone 2",
    "Bite Cycle",
    "Buzzy Cycle",
    "Metalphone 1",
    "Metalphone 2",
    "Metalphone 3",
    "Metalphone 4",
    "Tine Wave",
    "Duck Cycle 1",
    "Duck Cycle 2",
    "Duck Cycle 3",
    "Theremin Saw",
    "Wind Cycle 1",
    "Wind Cycle 2",
    "Wind Cycle 3",
    "Wind Cycle 4",
    "Organ Cycle 1",
    "Organ Cycle 2",
    "Violin Essence",
    "Buzzoon",
    "Brassy Wave",
    "Woofer Pulse",
    "Trumpet Waves 1",
    "Trumpet Waves 2",
    "Trumpet Waves 3",
    "Reedy Buzz",
    "Growl Wave",
    "HarpsiWave",
    "Fuzzy Gruzz",
    "Power 5ths",
    "Reson TG",
    "Hollow Whisp",
    "Metal Whisp",
    "Formant Axe",
    "Crunchy Lead",
    "Sync Lead",
    "Pulse Sweep",
    "Pulse Wave",
    "Emu Modular 1",
    "Emu Modular 2",
    "Mini MOOG",
    "Saint Moog",
    "Analog Bass",
    "Liquid Moog",
    "Quake Bass",
    "Q Bass",
    "Luxury Bass",
    "Reson Bass 2",
    "Deep Bass",
    "Eight 0 Eight",
    "MOOG Wave 1",
    "MOOG Wave 2",
    "MOOG Wave 3",
    "EMU RECTIFIER 1",
    "MOOG RECTANGLE 1",
    "MOOG RECTANGLE 2",
    "MOOG RECTANGLE 3",
    "MOOG RECTANGLE 4",
    "MOOG RECTANGLE 5",
    "Ring Mod Wave",
    "Ice Bell",
    "Bronze Age",
    "Iron Plate",
    "Aluminum",
    "Lead Beam",
    "Steel Xtract",
    "Winter Glass",
    "Ice Wash",
    "Hollow Wash",
    "Cluster",
    "Swirly",
    "Nyles Noise",
    "Mellow Pad",
    "Syn String 1",
    "Syn String 2",
    "Syn String 3",
    "Thick String Wave",
    "Silk Veil",
    "Mecha Vox",
    "Techno Vox",
    "Rack Bell",
    "Solder Sucker",
    "Rasp Shaker",
    "Iron Rattle",
    "Burst Hats",
    "Vise Grips",
    "Record Scratch 1",
    "Record Scratch 2",
    "Record Scratch 3",
    "Record Scratch 4",
    "Record Scratch 5",
    "Record Scratch 6",
    "Record Scratch 7",
    "Modular Scratch",
    "Distorto Kick",
    "Beef Kick Rap",
    "Bass 909 #2",
    "High Snare",
    "Snap Snare",
    "Snare 3",
    "Crushed Snare",
    "Noise Hat",
    "Bass Drum",
    "Kick 909",
    "Kick 909 #2",
    "Dry Bass Drum",
    "Liquid Drum",
    "Power Kick",
    "Cowbell",
    "Tamb707Noiz",
    "Shaker 808",
    "Shaker 2",
    "NoizTambHit",
    "Open Hat",
    "Foot Hat",
    "Tip Hat",
    "Hi Hat 808",
    "Open Hi Hat",
    "Closed Hat",
    "Hip Hat",
    "Hip Hat 2",
    "Rim Shot",
    "Snare Drum 1",
    "Snare Drum 2",
    "Snare 808",
    "Rim 808 Shot",
    "Electric Snare 4",
    "ROB 808 Claps",
    "Dry 909 Clap",
    "Open Hat 909",
    "Closed Hat 909",
    "Shaman Snare 1",
    "Shaman Snare 2",
    "Shaman Snare 3",
    "Ware Snare",
    "Snare 5",
    "Snare 909 1",
    "Snare 909 2",
    "Film Click",
    "All Kicks",
    "All Snares",
    "Hats & Noise",
    "Rap Scratch A",
    "Rap Scratch B",
    "Conga Hits",
    "Clave Log Drum",
    "Combined Hit"
    };
    
    public static int[] MORPHEUS_FILTER_TYPES = null;

    public static final int[] ULTRAPROTEUS_FILTER_TYPES = new int[]
    {
    0,
    1,
    1,
    1,
    3,
    1,
    1,
    3,
    1,
    1,
    1,
    1,
    3,
    3,
    3,
    3,
    3,
    3,
    3,
    3,
    3,
    3,
    3,
    3,
    3,
    1,
    1,
    1,
    3,
    3,
    1,
    1,
    1,
    1,
    1,
    1,
    1,
    1,
    1,
    3,
    3,
    1,
    1,
    3,
    1,
    3,
    3,
    3,
    3,
    3,
    1,
    1,
    1,
    3,
    3,
    1,
    1,
    1,
    1,
    1,
    1,
    1,
    1,
    3,
    3,
    3,
    1,
    1,
    3,
    1,
    1,
    1,
    1,
    3,
    3,
    3,
    3,
    3,
    3,
    3,
    1,
    1,
    1,
    3,
    3,
    3,
    3,
    3,
    3,
    1,
    3,
    1,
    3,
    3,
    3,
    1,
    3,
    1,
    1,
    1,
    3,
    1,
    1,
    3,
    1,
    1,
    1,
    3,
    3,
    1,
    3,
    1,
    3,
    3,
    3,
    3,
    3,
    3,
    3,
    1,
    3,
    1,
    1,
    3,
    1,
    3,
    3,
    3,
    3,
    3,
    3,
    3,
    1,
    3,
    1,
    1,
    1,
    1,
    1,
    1,
    1,
    1,
    1,
    3,
    1,
    1,
    3,
    3,
    3,
    1,
    1,
    1,
    1,
    1,
    1,
    1,
    3,
    1,
    1,
    1,
    1,
    1,
    1,
    1,
    3,
    3,
    3,
    1,
    1,
    1,
    1,
    3,
    3,
    3,
    3,
    3,
    3,
    3,
    3,
    3,
    3,
    3,
    3,
    3,
    3,
    3,
    3,
    1,
    3,
    3,
    3,
    3,
    3,
    3,
    3,
    1,
    5,
    5,
    3,
    3,
    3,
    3,
    3,
    3,
    3,
    3,
    3,
    3,
    3,
    1,
    3,
    3,
    3,
    3,
    3,
    3,
    3,
    3,
    3,
    3,
    3,
    3,
    3,
    3,
    3,
    3,
    3,
    3,
    3,
    3,
    3,
    3,
    3,
    3,
    3,
    3,
    1,
    3,
    3,
    3,
    3,
    3,
    3,
    1,
    1,
    3,
    3,
    1,
    3,
    3,
    3,
    3,
    3,
    3,
    3,
    3,
    3,
    3,
    3,
    1,
    1,
    1,
    1,
    1,
    1,
    3,
    3,
    3,
    1,
    3,
    3,
    3,
    3,
    3,
    3,
    3,
    3,
    1,
    1,
    3,
    3,
    3,
    3,
    3,
    1,
    3,
    3,
    3,
    3,
    };

    public static int[] MORPHEUS_FILTER_OFFSETS = null;  // will be set in the constructor
    public static String[] MORPHEUS_FILTERS = null;  // will be set in the constructor

    public static final int[] ULTRAPROTEUS_FILTER_OFFSETS = new int[]
    {
    0,
    162,
    163,
    164,
    165,
    166,
    168,
    167,
    169,
    170,
    179,
    381,
    171,
    172,
    174,
    175,
    176,
    173,
    177,
    178,
    185,
    186,
    187,
    188,
    189,
    190,
    191,
    192,
    205,
    193,
    194,
    196,
    197,
    198,
    318,
    199,
    200,
    201,
    202,
    203,
    204,
    207,
    208,
    184,
    210,
    234,
    211,
    212,
    213,
    214,
    331,
    340,
    221,
    215,
    218,
    219,
    233,
    222,
    223,
    224,
    225,
    226,
    227,
    228,
    209,
    229,
    242,
    243,
    231,
    232,
    246,
    247,
    248,
    244,
    240,
    239,
    236,
    237,
    238,
    241,
    249,
    259,
    261,
    367,
    369,
    262,
    253,
    263,
    264,
    266,
    265,
    267,
    268,
    292,
    293,
    294,
    270,
    272,
    273,
    274,
    275,
    327,
    305,
    377,
    277,
    276,
    278,
    360,
    376,
    251,
    396,
    291,
    368,
    370,
    281,
    282,
    283,
    284,
    356,
    285,
    288,
    397,
    286,
    297,
    298,
    299,
    300,
    301,
    302,
    357,
    345,
    303,
    304,
    307,
    308,
    309,
    310,
    311,
    312,
    313,
    314,
    315,
    316,
    317,
    319,
    320,
    321,
    322,
    323,
    324,
    325,
    326,
    328,
    329,
    330,
    332,
    333,
    334,
    335,
    336,
    337,
    338,
    341,
    344,
    346,
    347,
    348,
    349,
    351,
    352,
    353,
    355,
    358,
    183,
    359,
    361,
    362,
    363,
    364,
    371,
    365,
    366,
    372,
    373,
    374,
    375,
    378,
    380,
    385,
    383,
    180,
    181,
    182,
    386,
    382,
    354,
    403,
    407,
    425,
    424,
    421,
    422,
    423,
    426,
    427,
    420,
    450,
    443,
    444,
    534,
    525,
    428,
    429,
    430,
    431,
    432,
    433,
    434,
    522,
    441,
    451,
    516,
    442,
    445,
    452,
    437,
    438,
    439,
    517,
    440,
    530,
    531,
    447,
    448,
    449,
    523,
    496,
    535,
    484,
    453,
    489,
    486,
    488,
    465,
    536,
    458,
    463,
    471,
    435,
    436,
    490,
    532,
    480,
    482,
    481,
    483,
    485,
    524,
    470,
    477,
    495,
    521,
    468,
    469,
    473,
    479,
    459,
    476,
    533,
    467,
    520,
    478,
    454,
    472,
    474,
    461,
    462,
    464,
    475,
    487,
    519,
    527,
    528,
    457,
    466,
    518,
    446,
    455,
    456,
    };


    public static final int[] MORPHEUS_INSTRUMENT_OFFSETS = new int[]
    {
    1793,
    1794,
    1795,
    1796,
    1797,
    1798,
    1799,
    1800,
    1801,
    1802,
    1803,
    1804,
    1805,
    1806,
    1807,
    1808,
    1809,
    1810,
    1811,
    1812,
    1813,
    1814,
    1815,
    1816,
    1817,
    1818,
    1819,
    1820,
    1821,
    1822,
    1823,
    1824,
    1825,
    1826,
    1827,
    1828,
    1829,
    1830,
    1831,
    1832,
    1833,
    1834,
    1835,
    1836,
    1837,
    1838,
    1839,
    1840,
    1841,
    1842,
    1843,
    1844,
    1845,
    1846,
    1847,
    1848,
    1849,
    1850,
    1851,
    1852,
    1853,
    1854,
    1855,
    1856,
    1857,
    1858,
    1859,
    1860,
    1861,
    1862,
    1863,
    1864,
    1865,
    1866,
    1867,
    1868,
    1869,
    1870,
    1871,
    1872,
    1873,
    1874,
    1875,
    1876,
    1877,
    1878,
    1879,
    1880,
    1881,
    1882,
    1883,
    1884,
    1885,
    1886,
    1887,
    1888,
    1889,
    1890,
    1891,
    1892,
    1893,
    1894,
    1895,
    1896,
    1897,
    1898,
    1899,
    1900,
    1901,
    1902,
    1903,
    1904,
    1905,
    1906,
    1907,
    1908,
    1909,
    1910,
    1911,
    1912,
    1913,
    1914,
    1915,
    1916,
    1917,
    1918,
    1919,
    1920,
    1921,
    1922,
    1923,
    1924,
    1925,
    1926,
    1927,
    1928,
    1929,
    1930,
    1931,
    1932,
    1933,
    1934,
    1935,
    1936,
    1937,
    1938,
    1939,
    1940,
    1941,
    1942,
    1943,
    1944,
    1945,
    1946,
    1947,
    1948,
    1949,
    1950,
    1951,
    1952,
    1953,
    1954,
    1955,
    1956,
    1957,
    1958,
    1959,
    1960,
    1961,
    1962,
    1963,
    1964,
    1965,
    1966,
    1967,
    1968,
    1969,
    1970,
    1971,
    1972,
    1973,
    1974,
    1975,
    1976,
    1977,
    1978,
    1979,
    1980,
    1981,
    1982,
    1983,
    1984,
    1985,
    1986,
    1987,
    1988,
    1989,
    1990,
    1991,
    1992,
    1993,
    1994,
    1995,
    1996,
    1997,
    1998,
    1999,
    2000,
    2001,
    2002,
    2003,
    2004,
    2005,
    2006,
    2007,
    2008,
    2009,
    2010,
    2011,
    2012,
    2013,
    2014,
    2015,
    2016,
    2017,
    2018,
    2019,
    2020,
    2021,
    2022,
    2023,
    2024,
    2025,
    2026,
    2027,
    2028,
    2029,
    2030,
    2031,
    2032,
    2033,
    2034
    };
                                    
    public static final int[] ULTRAPROTEUS_INSTRUMENT_OFFSETS = new int[]
    {
    2305,   
    2306,   
    2307,   
    2308,   
    2309,   
    2310,   
    2559,   
    2383,   
    2441,   
    2361,   
    2648,   
    2362,   
    2363,   
    2364,   
    2365,   
    2366,   
    2367,   
    2368,   
    2369,   
    2370,   
    2371,   
    2377,   
    2378,   
    2372,   
    2373,   
    2374,   
    2375,   
    2376,   
    2384,   
    2354,   
    2355,   
    2356,   
    2357,   
    2358,   
    2359,   
    2360,   
    2314,   
    2315,   
    2316,   
    2322,   
    2323,   
    2324,   
    2325,   
    2326,   
    2327,   
    2311,   
    2312,   
    2313,   
    2572,   
    2573,   
    2574,   
    2317,   
    2318,   
    2319,   
    2320,   
    2321,   
    2561,   
    2562,   
    2563,   
    2564,   
    2565,   
    2566,   
    2567,   
    2568,   
    2571,   
    2569,   
    2570,   
    2626,   
    2627,   
    2628,   
    2629,   
    2630,   
    2631,   
    2632,   
    2633,   
    2634,   
    2635,   
    2636,   
    2637,   
    2638,   
    2639,   
    2640,   
    2641,   
    2642,   
    2643,   
    2644,   
    2645,   
    2646,   
    2647,   
    2649,   
    2650,   
    2654,   
    2622,   
    2651,   
    2652,   
    2653,   
    2342,   
    2341,   
    2340,   
    2343,   
    2597,   
    2344,   
    2346,   
    2592,   
    2593,   
    2352,   
    2594,   
    2353,   
    2345,   
    2347,   
    2348,   
    2590,   
    2591,   
    2349,   
    2595,   
    2596,   
    2350,   
    2351,   
    2599,   
    2598,   
    2338,   
    2339,   
    2575,   
    2576,   
    2577,   
    2578,   
    2579,   
    2580,   
    2581,   
    2582,   
    2583,   
    2584,   
    2585,   
    2586,   
    2587,   
    2588,   
    2589,   
    2655,   
    2656,   
    2657,   
    2624,   
    2671,   
    2672,   
    2673,   
    2674,   
    2675,   
    2658,   
    2659,   
    2660,   
    2661,   
    2665,   
    2666,   
    2667,   
    2668,   
    2669,   
    2670,   
    2664,   
    2662,   
    2663,   
    2676,   
    2677,   
    2678,   
    2679,   
    2680,   
    2681,   
    2682,   
    2683,   
    2684,   
    2685,   
    2686,   
    2687,   
    2625,   
    2328,   
    2329,   
    2330,   
    2331,   
    2332,   
    2333,   
    2334,   
    2335,   
    2336,   
    2337,   
    2387,   
    2388,   
    2389,   
    2390,   
    2391,   
    2766,   
    2392,   
    2427,   
    2428,   
    2429,   
    2393,   
    2773,   
    2394,   
    2395,   
    2396,   
    2397,   
    2398,   
    2399,   
    2400,   
    2401,   
    2402,   
    2403,   
    2404,   
    2405,   
    2406,   
    2407,   
    2408,   
    2409,   
    2410,   
    2411,   
    2412,   
    2767,   
    2768,   
    2769,   
    2770,   
    2771,   
    2772,   
    2608,   
    2609,   
    2610,   
    2611,   
    2617,   
    2621,   
    2618,   
    2619,   
    2620,   
    2613,   
    2603,   
    2604,   
    2605,   
    2615,   
    2606,   
    2616,   
    2707,   
    2385,   
    2386,   
    2600,   
    2601,   
    2602,   
    2607,   
    2612,   
    2614,   
    2413,   
    2414,   
    2700,   
    2691,   
    2690,   
    2689,   
    2699,   
    2692,   
    2761,   
    2701,   
    2706,   
    2693,   
    2698,   
    2702,   
    2703,   
    2704,   
    2688,   
    2623,   
    2415,   
    2416,   
    2705,   
    2417,   
    2419,   
    2420,   
    2426,   
    2418,   
    2421,   
    2422,   
    2423,   
    2424,   
    2425,   
    2718,   
    2719,   
    2720,   
    2721,   
    2726,   
    2727,   
    2728,   
    2729,   
    2730,   
    2731,   
    2736,   
    2737,   
    2738,   
    2739,   
    2740,   
    2722,   
    2723,   
    2724,   
    2725,   
    2711,   
    2712,   
    2713,   
    2714,   
    2732,   
    2733,   
    2734,   
    2735,   
    2754,   
    2755,   
    2756,   
    2757,   
    2758,   
    2759,   
    2760,   
    2708,   
    2709,   
    2710,   
    2715,   
    2716,   
    2717,   
    2762,   
    2763,   
    2764,   
    2765,   
    2748,   
    2749,   
    2750,   
    2697,   
    2695,   
    2751,   
    2694,   
    2696,   
    2741,   
    2742,   
    2743,   
    2744,   
    2745,   
    2746,   
    2747,   
    2752,   
    2753,   
    2379,   
    2380,   
    2381,   
    2382,   
    2774,   
    2775,   
    2481,   
    2482,   
    2483,   
    2484,   
    2485,   
    2486,   
    2487,   
    2488,   
    2489,   
    2490,   
    2491,   
    2492,   
    2493,   
    2494,   
    2495,   
    2496,   
    2497,   
    2498,   
    2499,   
    2500,   
    2501,   
    2502,   
    2503,   
    2504,   
    2442,   
    2432,   
    2433,   
    2443,   
    2534,   
    2430,   
    2431,   
    2444,   
    2535,   
    2536,   
    2537,   
    2509,   
    2510,   
    2445,   
    2446,   
    2447,   
    2448,   
    2449,   
    2450,   
    2451,   
    2452,   
    2453,   
    2454,   
    2455,   
    2456,   
    2457,   
    2458,   
    2459,   
    2460,   
    2461,   
    2462,   
    2463,   
    2464,   
    2465,   
    2466,   
    2467,   
    2468,   
    2469,   
    2470,   
    2471,   
    2472,   
    2473,   
    2434,   
    2474,   
    2435,   
    2475,   
    2476,   
    2477,   
    2478,   
    2479,   
    2480,   
    2505,   
    2506,   
    2507,   
    2508,   
    2511,   
    2512,   
    2513,   
    2514,   
    2515,   
    2516,   
    2517,   
    2518,   
    2519,   
    2520,   
    2521,   
    2522,   
    2523,   
    2524,   
    2525,   
    2526,   
    2527,   
    2528,   
    2529,   
    2530,   
    2531,   
    2532,   
    2533,   
    2538,   
    2436,   
    2539,   
    2540,   
    2541,   
    2542,   
    2543,   
    2544,   
    2545,   
    2546,   
    2547,   
    2548,   
    2549,   
    2550,   
    2551,   
    2552,   
    2553,   
    2554,   
    2555,   
    2556,   
    2557,   
    2437,   
    2438,   
    2439,   
    2440,   
    2558
    };

    }
