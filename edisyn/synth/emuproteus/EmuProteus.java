/***
    Copyright 2018 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.emuproteus;

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
   A patch editor for the E-Mu Proteus Family (Proteus 1, Proteus 1 Orchestral, Proteus 2, Proteus 3)
        
   @author Sean Luke
*/

public class EmuProteus extends Synth
    {
    public static final String[] CROSSFADE_MODES = new String[] { "Off", "Crossfade", "Cross-switch" };
    public static final String[] CROSSFADE_DIRECTIONS = new String[] { "Pri->Sec", "Sec->Pri" };
    public static final String[] VELOCITY_CURVES = new String[] { "Off", "1", "2", "3", "4", "Global" };
    public static final String[] KEYBOARD_TUNINGS = new String[] { "Equal", "Just C", "Valotti", "19 Tone", "Gamelan", "User" };
    public static final String[] OUTPUTS = new String[] { "Main", "Sub 1", "Sub 2" };
    public static final String[] NOTES = new String[] { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };

/// FIXME:  On the Proteus 1 screen, the order is RAND TRI SINE SAW SQUARE.  In the docs, it's as below.  Dunno.
    public static final String[] LFO_SHAPES = new String[] { "Random", "Triangle", "Sine", "Sawtooth", "Square" };
        
    public static final int TYPE_1 = 0;
    public static final int TYPE_1_XR = 1;
    public static final int TYPE_1_ORCHESTRAL = 2;
    public static final int TYPE_2 = 3;
    public static final int TYPE_2_XR = 4;
    public static final int TYPE_3 = 5;
    public static final int TYPE_3_XR = 6;
    public static final String[] TYPES = { "Proteus 1", "Proteus 1 XR", "1+Orchestral", "Proteus 2", "Proteus 2 XR", "Proteus 3", "Proteus 3 XR" };
    public static final String TYPE_KEY = "type";
    int synthType = TYPE_1;
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

    Chooser[] instrumentChoosers = new Chooser[2];


    public void updateChoosers()
        {
        for(int j = 0; j < 2; j++)
            {
            JComboBox box = instrumentChoosers[j].getCombo();
            int sel = box.getSelectedIndex();
            box.removeAllItems();
            String[] elts = ((synthType == TYPE_1 || synthType == TYPE_1_XR) ? PROTEUS_1_INSTRUMENTS :
                    (synthType == TYPE_1_ORCHESTRAL ? PROTEUS_1_ORCHESTRAL_INSTRUMENTS :
                    ((synthType == TYPE_2 || synthType == TYPE_2_XR) ? PROTEUS_2_INSTRUMENTS : PROTEUS_3_INSTRUMENTS)));
            for(int i = 0; i < elts.length; i++)
                box.addItem(elts[i]);
            instrumentChoosers[j].setMax(elts.length - 1);
            if (sel <= elts.length)
                box.setSelectedIndex(sel);
            else
                box.setSelectedIndex(0);
            }
        }

        
    public EmuProteus()
        {
        if (parametersToIndex == null)
            {
            parametersToIndex = new HashMap();
            proteus2MappingToIndex = new HashMap();
                
            for(int i = 0; i < parameters.length; i++)
                {
                parametersToIndex.put(parameters[i], Integer.valueOf(i));
                }

            for(int i = 0; i < PROTEUS_2_MAPPING.length; i++)
                {
                proteus2MappingToIndex.put(Integer.valueOf(PROTEUS_2_MAPPING[i]), Integer.valueOf(i));
                }
            }
            
        String m = getLastX(TYPE_KEY, getSynthName());
        try
            {
            synthType = (m == null ? TYPE_1 : Integer.parseInt(m));
            if (synthType < TYPE_1 || synthType > TYPE_3_XR)
                {
                synthType = TYPE_1;
                }
            }
        catch (NumberFormatException ex)
            {
            synthType = TYPE_1;
            }
            
        /// SOUND PANEL
                
        SynthPanel soundPanel = new SynthPanel(this);
        
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        hbox.addLast(addEnvelope(3, Style.COLOR_C()));
        vbox.add(hbox);
        
        vbox.add(addInstrument(1, Style.COLOR_A())); 
        HBox hbox2 = new HBox();
        hbox2.add(addEnvelope(1, Style.COLOR_A()));
        hbox2.addLast(addKeyboard(Style.COLOR_C()));
        vbox.add(hbox2); 
                
        vbox.add(addInstrument(2, Style.COLOR_B()));
        hbox2 = new HBox();
        hbox2.add(addEnvelope(2, Style.COLOR_B()));
        hbox2.addLast(addCrossfade(Style.COLOR_C()));
        vbox.add(hbox2);
        vbox.add(addSplits(Style.COLOR_C()));
                
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Instruments", soundPanel);
                
              
        soundPanel = new SynthPanel(this);

        vbox = new VBox();
        hbox = new HBox();
        hbox.add(addLFO(1, Style.COLOR_B()));
        hbox.addLast(addLFO(2, Style.COLOR_B()));
        vbox.add(hbox);

        vbox.add(addKeyVel(Style.COLOR_A()));
        vbox.add(addRealtime(Style.COLOR_C()));
        
        vbox.add(addOther(Style.COLOR_A()));
        
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Modulation", soundPanel);
                        
        model.set("name", "Untitled");
        model.set("number", 0);
        
        updateChoosers();
        loadDefaults();
        }
                
    public String getDefaultResourceFileName() { return "EmuProteus.init"; }
    public String getHTMLResourceFileName() { return "EmuProteus.html"; }

    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        int num = model.get("number");
        JTextField number = new JTextField("" + (num < 10 ? "00" : (num < 100 ? "0" : "")) + num, 3);
        
        while(true)
            {
            String range = "(0...191)";
            if (synthType == TYPE_1_XR || synthType == TYPE_2_XR || synthType == TYPE_3_XR)
                {
                if (writing) range = "(0...255)";
                else range = "(0...383)";
                }
            else if (writing) range = "(64...127)";
            else if (getSynthType() == TYPE_1_ORCHESTRAL) range = "(0...487)";
            
            boolean result = showMultiOption(this, new String[] { "Patch Number"}, 
                new JComponent[] { number }, title, "Enter the Patch number " + range);
                
            if (result == false) 
                return false;
                                
            int n;
            try { n = Integer.parseInt(number.getText()); }
            catch (NumberFormatException e)
                {
                if (synthType == TYPE_1_XR || synthType == TYPE_2_XR || synthType == TYPE_3_XR)
                    {
                    if (writing)
                        showSimpleError(title, "The Patch Number must be an integer 0...255");
                    else
                        showSimpleError(title, "The Patch Number must be an integer 0...383");                                  
                    }
                else if (writing)
                    showSimpleError(title, "The Patch Number must be an integer 64...127");
                else if (getSynthType() == TYPE_1_ORCHESTRAL)
                    showSimpleError(title, "The Patch Number must be an integer 0...487");
                else
                    {
                    showSimpleError(title, "The Patch Number must be an integer 0...191");
                    }
                continue;
                }
            if (synthType == TYPE_1_XR || synthType == TYPE_2_XR || synthType == TYPE_3_XR)
                {
                if (n < 0 || n > 255)
                    {
                    showSimpleError(title, "The Patch Number must be an integer 0...255");
                    continue;
                    }
                else if (n < 0 || n > 383)
                    {
                    showSimpleError(title, "The Patch Number must be an integer 0...383");
                    continue;
                    }
                }
            if (writing)
                {
                if (n < 64 || n > 127)
                    {
                    showSimpleError(title, "The Patch Number must be an integer 64...127");
                    continue;
                    }
                }
            else if (getSynthType() == TYPE_1_ORCHESTRAL)
                {
                if (n < 0 || n > 487)
                    {
                    showSimpleError(title, "The Patch Number must be an integer 0...487");
                    continue;
                    }
                }
            else
                {
                if (n < 0 || n > 191)
                    {
                    showSimpleError(title, "The Patch Number must be an integer 0...191");
                    continue;
                    }
                }
                                                
            change.set("number", n);
                        
            return true;
            }
        }
        
    /** Add the global patch category (name, id, number, etc.) */
    public JComponent addNameGlobal(Color color)
        {
        Category globalCategory = new Category(this, "E-Mu Proteus", color);
        //globalCategory.makeUnresettable();
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        VBox vbox2 = new VBox();
        final PatchDisplay pd = new PatchDisplay(this, 3);
        comp = pd;
        vbox2.add(comp);
        hbox.add(vbox2);
        
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
                pd.update("number", model);  // doesn't matter what the key is, so I put in "number"
                }
            });
        synthTypeCombo.putClientProperty("JComponent.sizeVariant", "small");
        synthTypeCombo.setEditable(false);
        synthTypeCombo.setFont(Style.SMALL_FONT());
        VBox st = new VBox();
        st.add(label);
        st.addLast(synthTypeCombo);
        vbox.add(st);
        
        comp = new StringComponent("Patch Name", this, "name", 5, "Name must be up to 12 characters.")
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
        vbox.addBottom(Stretch.makeVerticalStretch()); 
        hbox.add(vbox);

        vbox = new VBox();
        params = OUTPUTS;
        comp = new Chooser("Output", this, "submix", params);
        vbox.add(comp);
        hbox.add(vbox);
                
        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }


    public JComponent addInstrument(int inst, Color color)
        {
        Category category = new Category(this, (inst == 1) ? "Primary Instrument" : "Secondary Instrument", color);
        category.makePasteable("i" + inst);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        vbox = new VBox();
        params = PROTEUS_1_INSTRUMENTS;
        comp = new Chooser("Instrument", this, "i" + inst + "instrument", params)
            {
            // normally if we're outside the min/max bounds, 
            // setState will bound to those values.  Here
            // we pick a more randomized mechanism 
            public void setState(int val)
                {
                val = val % getCombo().getItemCount();
                super.setState(val);
                }
            };
        instrumentChoosers[inst - 1] = (Chooser)comp;
        vbox.add(comp);

        comp = new CheckBox("Solo Mode", this, "i" + inst + "solomode");
        vbox.add(comp);

        comp = new CheckBox("Reverse Sound", this, "i" + inst + "reversesound");
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Sound Start", this, "i" + inst + "samplestartoffset", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Offset");
        hbox.add(comp);

        comp = new LabelledDial("Tuning", this, "i" + inst + "tuningcoarse", color, -36, 36);
        ((LabelledDial)comp).addAdditionalLabel("Coarse");
        hbox.add(comp);

        // no, really.  The range is 129 long!
        comp = new LabelledDial("Tuning", this, "i" + inst + "tuningfine", color, -64, 64);
        ((LabelledDial)comp).addAdditionalLabel("Fine");
        hbox.add(comp);

        comp = new LabelledDial("Volume", this, "i" + inst + "volume", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Pan", this, "i" + inst + "pan", color, -7, 7);
        hbox.add(comp);

        comp = new LabelledDial("Delay", this, "i" + inst + "delay", color, 0, 127);
        hbox.add(comp);
                
        comp = new LabelledDial("Low Key", this, "i" + inst + "lowkey", color, 0, 127)
            {
            public String map(int value)
                {
                return NOTES[value % 12] + (value / 12 - 2);
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("High Key", this, "i" + inst + "highkey", color, 0, 127)
            {
            public String map(int value)
                {
                return NOTES[value % 12] + (value / 12 - 2);
                }
            };
        hbox.add(comp);

        /// FIXME: Indicate that on the Proteus/1, it's just on/off (0/1)
        comp = new LabelledDial("Chorus", this, "i" + inst + "chorus", color, 0, 15)
            {
            public String map(int value)
                {
                if (value == 0) return "Off";
                else return ("" + value);
                }
            };
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
                

    public JComponent addEnvelope(int inst, Color color)
        {
        Category category = new Category(this, (inst == 1 ? "Primary Alternative Envelope" :
                (inst == 2 ? "Secondary Alternative Envelope" : "Auxiliary Envelope")), color);
        category.makePasteable("i" + inst);
              
        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();

        if (inst == 1 || inst == 2)
            {
            VBox vbox = new VBox();
            comp = new CheckBox("On", this, "i" + inst + "envelopeon");
            vbox.add(comp);
            hbox.add(vbox);
            }
        else
            {
            comp = new LabelledDial("Amount", this, "i" + inst + "amount", color, -128, 127);
            hbox.add(comp);
                        
            comp = new LabelledDial("Delay", this, "i" + inst + "delay", color, 0, 127);
            hbox.add(comp);
            }

        comp = new LabelledDial("Attack", this, "i" + inst + "attack", color, 0, 99);
        hbox.add(comp);
                
        comp = new LabelledDial("Hold", this, "i" + inst + "hold", color, 0, 99);
        hbox.add(comp);
                
        comp = new LabelledDial("Decay", this, "i" + inst + "decay", color, 0, 99);
        hbox.add(comp);
                
        comp = new LabelledDial("Sustain", this, "i" + inst + "sustain", color, 0, 99);
        hbox.add(comp);
                
        comp = new LabelledDial("Release", this, "i" + inst + "release", color, 0, 99);
        hbox.add(comp);
 
        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "i" + inst + "attack", "i" + inst + "hold", "i" + inst + "decay", null, "i" + inst + "release" },
            new String[] { null, null, null, "i" + inst + "sustain", "i" + inst + "sustain", null },
            new double[] { 0, 0.2/99, 0.2/99, 0.2/99, 0.2, 0.2/99 },
            new double[] { 0, 1.0, 1.0, 1.0/99.0, 1.0/99.0, 0 } 
            );
        hbox.addLast(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addKeyboard(Color color)
        {
        Category category = new Category(this, "Keyboard", color);
                
        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();

        VBox vbox = new VBox();
        params = VELOCITY_CURVES;
        comp = new Chooser("Velocity Curve", this, "velocitycurve", params);
        vbox.add(comp);
        params = KEYBOARD_TUNINGS;
        comp = new Chooser("Keyboard Tuning", this, "keyboardtuning", params);
        vbox.add(comp);
        hbox.add(vbox);
                
        comp = new LabelledDial("Pitch Bend", this, "pitchbendrange", color, 0, 13)
            {
            public String map(int value)
                {
                if (value == 13) return "Global";
                return "" + value;
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Range");
        hbox.add(comp);

        comp = new LabelledDial("Pressure", this, "pressureamount", color, -128, 127);
        hbox.add(comp);
                
        comp = new LabelledDial("Keyboard", this, "keyboardcenter", color, 0, 128)
            {
            public String map(int value)
                {
                return NOTES[value % 12] + (value / 12 - 2);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Center");
        hbox.add(comp);

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
        params = CROSSFADE_MODES;
        comp = new Chooser("Mode", this, "crossfademode", params);
        vbox.add(comp);

        params = CROSSFADE_DIRECTIONS;
        comp = new Chooser("Direction", this, "crossfadedirection", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Balance", this, "crossfadebalance", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Amount", this, "crossfadeamount", color, 0, 255);
        hbox.add(comp);

        comp = new LabelledDial("Switch Point", this, "switchpoint", color, 0, 128)
            {
            public String map(int value)
                {
                return "" + value + ":" + NOTES[value % 12] + (value / 12 - 2);
                }
            };
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
        
    public JComponent addKeyVel(Color color)
        {
        Category category = new Category(this, "Keyboard / Velocity Modulation Control", color);
        category.makeDistributable("keyvel");
                
        JComponent comp;
        String[] params;
        VBox vbox2 = new VBox();
        HBox hbox = new HBox();

        for(int i = 1; i <= 6; i++)
            {
            VBox vbox = new VBox();
            params = KEYVEL_SOURCES;
            comp = new Chooser("" + i + " Source", this, "keyvelsource" + i, params);
            vbox.add(comp);
        
            params = KEYVEL_DESTINATIONS;
            comp = new Chooser("" + i + " Destination", this, "keyveldest" + i, params);
            vbox.add(comp);
                        
                        
            comp = new LabelledDial("" + i + " Amount", this, "keyvelamount" + i, color, -128, 127);
            hbox.add(comp);

            hbox.add(vbox);
            if (i == 3 || i == 6)
                {
                vbox2.add(hbox);
                //if (i == 3) 
                vbox2.add(Strut.makeVerticalStrut(20));
                hbox = new HBox();
                }
            }

        category.add(vbox2, BorderLayout.CENTER);
        return category;
        }

    public JComponent addOther(Color color)
        {
        Category category = new Category(this, "General Control", color);
        category.makeDistributable("");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();


        VBox vbox = new VBox();
        params = FOOTSWITCH_DESTINATIONS;
        comp = new Chooser("Footswitch 1 Destination", this, "footswitchdest1", params);
        vbox.add(comp);
        comp = new Chooser("Footswitch 2 Destination", this, "footswitchdest2", params);
        vbox.add(comp);
        comp = new Chooser("Footswitch 3 Destination", this, "footswitchdest3", params);
        vbox.add(comp);
        hbox.add(vbox);
                
        comp = new LabelledDial("Controller A", this, "controlleramount1", color, -128, 127);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);
        comp = new LabelledDial("Controller B", this, "controlleramount2", color, -128, 127);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);
        comp = new LabelledDial("Controller C", this, "controlleramount3", color, -128, 127);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);
        comp = new LabelledDial("Controller D", this, "controlleramount4", color, -128, 127);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
        
        
    public JComponent addRealtime(Color color)
        {
        Category category = new Category(this, "Real-time Modulation Control", color);
        category.makeDistributable("");
                
        JComponent comp;
        String[] params;
        VBox vbox2 = new VBox();
        HBox hbox = new HBox();

        for(int i = 1; i <= 8; i++)
            {
            VBox vbox = new VBox();
            params = REALTIME_SOURCES;
            comp = new Chooser("" + i + " Source", this, "realtimesource" + i, params);
            vbox.add(comp);
        
            params = REALTIME_DESTINATIONS;
            comp = new Chooser("" + i + " Destination", this, "realtimedest" + i, params);
            vbox.add(comp);

            hbox.add(vbox);
            if (i == 4 || i == 8)
                {
                vbox2.add(hbox);
                if (i == 4) vbox2.add(Strut.makeVerticalStrut(20));
                hbox = new HBox();
                }
            }

        category.add(vbox2, BorderLayout.CENTER);
        return category;
        }
        
        
    public JComponent addLFO(int lfo, Color color)
        {
        Category category = new Category(this, "LFO " + lfo, color);
        category.makePasteable("lfo" + lfo);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();

        params = LFO_SHAPES;
        comp = new Chooser("Shape", this, "lfo" + lfo + "shape", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Rate", this, "lfo" + lfo + "frequency", color, 0, 127);
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


    public JComponent addSplits(Color color)
        {
        Category category = new Category(this, "Splits", color);
        category.makeDistributable("");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();

        for(int i = 0; i < 4; i++)
            {
            if (i >= 1)
                {
                hbox.add(Strut.makeHorizontalStrut(20));
                comp = new LabelledDial("Preset", this, "link" + i, color, -1, 511)
                    {
                    public String map(int val)
                        {
                        if (val == -1) return "Off";
                        if (val <= 191) return "" + val;
                        else return "" + val + "[+]";
                        }
                    };
                ((LabelledDial)comp).addAdditionalLabel("Link " + i);
                hbox.add(comp);
                }

            comp = new LabelledDial("Low Key", this, "lowkey" + i, color, 0, 127)
                {
                public String map(int value)
                    {
                    return NOTES[value % 12] + (value / 12 - 2);
                    }
                };
            ((LabelledDial)comp).addAdditionalLabel(i == 0 ? "Main" : ("Link " + i));
            hbox.add(comp);

            comp = new LabelledDial("High Key", this, "highkey" + i, color, 0, 127)
                {
                public String map(int value)
                    {
                    return NOTES[value % 12] + (value / 12 - 2);
                    }
                };
            ((LabelledDial)comp).addAdditionalLabel(i == 0 ? "Main" : ("Link " + i));
            hbox.add(comp);
            }

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


    public Object[] emitAll(String key)
        {
        if (key.equals("number")) return new Object[0];  // this is not emittable

        if (key.equals("name"))
            {
            String name = model.get(key, "") + "            ";
            Object[] ret = new Object[12];
            for(int i = 0; i < 12; i++)
                {
                byte[] data = new byte[] { (byte)0xF0, (byte)0x18, (byte)0x04, getID(), (byte)0x03, (byte)i, 0, (byte)(name.charAt(i)), 0, (byte)0xF7 };
                ret[i] = data;
                }
            return ret;
            }
        else 
            {
            int param = ((Integer)(parametersToIndex.get(key))).intValue();
            byte paramMSB = (byte)(param >>> 7);
            byte paramLSB = (byte)(param & 127);
            int val = model.get(key);
            if (key.endsWith("instrument"))
                {
                val = instrumentToSysex(val);
                }
            byte valMSB = (byte)((val >> 7) & 127);             // note >>, not >>>.  We're doing signed extension
            byte valLSB = (byte)(val & 127);
            
            byte[] data = new byte[] { (byte)0xF0, (byte)0x18, (byte)0x04, getID(), (byte)0x03, paramLSB, paramMSB, valLSB, valMSB, (byte)0xF7 };
            return new Object[] { data };
            }
        }


    public Object[] emitAll(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        if (tempModel == null)
            tempModel = getModel();

        int NN = tempModel.get("number", 0);
        if (toWorkingMemory)
            {
            NN = 127;               // we write to the top writable patch number in RAM
            // furthermore we have to change the patch because sendAllParameters
            // doesn't do it by default
            Model model = buildModel();
            model.set("number", NN);
            changePatch(model);
            }
        byte[] data = new byte[parameters.length * 2 + 9];
        
        data[0] = (byte)0xF0;
        data[1] = (byte)0x18;
        data[2] = (byte)0x04;
        data[3] = (byte)getID();
        data[4] = (byte)0x01;
        data[5] = (byte)(NN % 128);
        data[6] = (byte)(NN / 128);
        
        int offset = 7;
        
        //// The E-Mu proteus manuals do NOT say how to calculate
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
            if (parameters[i].endsWith("instrument"))
                {
                val = instrumentToSysex(val);
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
        
    public boolean getSendsParametersAfterNonMergeParse() { return true; }


    public int parse(byte[] data, boolean fromFile)
        {
        int NN = data[5] + data[6] * 128;
        model.set("number", NN);
        
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
            int val = (data[offset++] + data[offset++] * 128);

            if (parameters[i].endsWith("instrument"))
                {
                val = sysexToInstrument(val);
                }
            else
                {
                if (val >= 8192)
                    val = val - 16384;
                }
            model.set(parameters[i], val);
            }
        revise();
        return PARSE_SUCCEEDED;
        }
   
        
    public void changePatch(Model tempModel)
        {
        int number = tempModel.get("number", 0);
        
        // first we load the program number into map slot 0
        byte[] data = new byte[6 + 128 * 2];
        data[0] = (byte)0xF0;
        data[1] = 0x18;
        data[2] = 0x04;
        data[3] = (byte)getID();
        data[4] = 0x07;
        data[5] = (byte)(number % 128);
        data[6] = (byte)(number / 128);
        data[data.length - 1] = (byte)0xF7;
        tryToSendSysex(data);
        
        // now do a PC 0
        tryToSendMIDI(buildPC(getChannelOut(), 0));
        }

    public byte[] requestDump(Model tempModel)
        {
        if (tempModel == null)
            tempModel = getModel();

        int NN = tempModel.get("number", 0);
        byte[] data = new byte[8];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x18;
        data[2] = (byte)0x04;
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
        if (synthType == TYPE_1_ORCHESTRAL)
            return "E-Mu Proteus 1+Orchestral";
        else
            return "E-Mu " + TYPES[synthType];
        }
        
    public static String getSynthName() { return "E-Mu Proteus"; }
    public String getPatchName(Model model) { return model.get("name", "Untitled"); }
    
    public Model getNextPatchLocation(Model model)
        {
        int number = model.get("number");
        
        number++;
        if (number >= 128)
            {
            number = 0;
            }
                
        Model newModel = buildModel();
        newModel.set("number", number);
        return newModel;
        }

    public String getPatchLocationName(Model model)
        {
        // getPatchLocationName() is called from sprout() as a test to see if we should enable
        // batch downloading.  If we haven't yet created an .init file, then parameters won't exist
        // yet and this method will bomb badly.  So we return null in this case.
        if (!model.exists("number")) return null;
        
        int number = (model.get("number"));
        return ((number > 99 ? "" : (number > 9 ? "0" : "00")) + number);
        }
        
    /// The Proteus1 started out with rational numbers for its instruments (0...125).
    /// But then E-mu went down a path which, in retrospect, was really stupid.  They decided to give
    /// each sample in their sample sets a unique number.  To do this, they grouped samples into
    /// "sound sets", each with a unique integer, and the sample in the set was a unique integer.
    /// Then different synthesizers would have different collections of sound sets.  Specifically,
    /// the Proteus1 was "reassigned" to be sound set 0, the Proteus1+orchestral was 0 and 2, the
    /// Proteus2 was 1 and 2, and the Proteus 3 was 3, [and later models had other sound sets].
    /// When an instrument is emitted via sysex, its unique number is provided, *not* the number of
    /// the instrument in the synthesizer.  So we have to map one to another.  To make matters worse,
    /// the Proteus2 doesn't hae a linear mapping -- sound sets 1 and 2 are mixed together in the ordering
    /// so we have to have a disambiguation table.  The unique number for an instrument is a 14-bit
    /// integer (or course), whose top 5 bits are the sound set number and the bottom 8 bits are the
    /// sample number within the soundset.
    public int sysexToInstrument(int sysexValue)
        {
        int set = (sysexValue >>> 8);
        int num = (sysexValue & 255);
        if (synthType == TYPE_1 || synthType == TYPE_1_XR)
            {
            if (set == 0)
                {
                return num;
                }
            else
                {
//                System.err.println("Warning (EmuProteus sysexToInstrument): Synth is Proteus1 but set is " + set);
                return 0;
                }
            }
        else if (synthType == TYPE_1_ORCHESTRAL)
            {
            if (set == 0)
                {
                return num;
                }
            else if (set == 2)
                {
                return num + 126;
                }
            else
                {
//                System.err.println("Warning (EmuProteus sysexToInstrument): Synth is Proteus1+Orchestral but set is " + set);
                return 0;
                }
            }
        else if (synthType == TYPE_2 || synthType == TYPE_2_XR)
            {
            if (set == 1 || set == 2)
                {
                Integer val = (Integer)proteus2MappingToIndex.get(Integer.valueOf(sysexValue));
                if (val == null) 
                    {
//                    System.err.println("Warning (EmuProteus sysexToInstrument): Synth is Proteus2 but set/instrument was " + sysexValue);
                    return 0;
                    }
                else return val.intValue();
                }
            else
                {
//                System.err.println("Warning (EmuProteus sysexToInstrument): Synth is Proteus2 but set is " + set);
                return 0;
                }
            }
        else if (synthType == TYPE_3 || synthType == TYPE_3_XR)
            {
            if (set == 3)
                {
                return num;
                }
            else
                {
//                System.err.println("Warning (EmuProteus sysexToInstrument): Synth is Proteus3 but set is " + set);
                return 0;
                }
            }
        else
            {
            System.err.println("Warning (EmuProteus sysexToInstrument): Unknown synth type " + synthType);
            return 0;
            }
        }

    public int instrumentToSysex(int instrument)
        {
        if (synthType == TYPE_1 || synthType == TYPE_1_XR)
            {
            return instrument;
            }
        else if (synthType == TYPE_1_ORCHESTRAL)
            {
            if (instrument <= 125)
                {
                return instrument;
                }
            else
                {
                return 256 * 2 + (instrument - 126);
                }
            }
        else if (synthType == TYPE_2 || synthType == TYPE_2_XR)
            {
            return PROTEUS_2_MAPPING[instrument];
            }
        else if (synthType == TYPE_3 || synthType == TYPE_3_XR)
            {
            return 256 * 3 + instrument;
            }
        else
            {
            System.err.println("Warning (EmuProteus instrumentToSysex): Unknown synth type " + synthType);
            return 0;
            }
        }               


    static HashMap proteus2MappingToIndex = null;


    public static final int[] PROTEUS_2_MAPPING = new int[]
    {
    0x0000,
    0x0101,
    0x0102,
    0x0103,
    0x0104,
    0x0105,
    0x0106,
    0x0107,
    0x0108,
    0x0109,
    0x010A,
    0x010D,
    0x010B,
    0x010C,
    0x010E,
    0x0201,
    0x0202,
    0x0203,
    0x0205,
    0x0206,
    0x0207,
    0x0208,
    0x0204,
    0x0209,
    0x020A,
    0x020B,
    0x020C,
    0x020D,
    0x010F,
    0x013F,
    0x0141,
    0x0110,
    0x020E,
    0x020F,
    0x0210,
    0x0211,
    0x0212,
    0x0213,
    0x0214,
    0x021E,
    0x024F,
    0x0215,
    0x0111,
    0x0140,
    0x0112,
    0x0113,
    0x0216,
    0x0114,
    0x0115,
    0x0116,
    0x0117,
    0x0118,
    0x0119,
    0x011A,
    0x011B,
    0x011C,
    0x011D,
    0x011E,
    0x011F,
    0x0120,
    0x0121,
    0x0122,
    0x0123,
    0x0124,
    0x0125,
    0x0217,
    0x0218,
    0x0219,
    0x021A,
    0x021B,
    0x021C,
    0x021D,
    0x0126,
    0x0127,
    0x0128,
    0x0129,
    0x012A,
    0x012B,
    0x012C,
    0x012D,
    0x012E,
    0x012F,
    0x0130,
    0x0131,
    0x0132,
    0x0133,
    0x0134,
    0x0135,
    0x0136,
    0x0137,
    0x0138,
    0x0139,
    0x013A,
    0x013B,
    0x0220,
    0x0221,
    0x0222,
    0x0223,
    0x0224,
    0x0225,
    0x0226,
    0x0227,
    0x0228,
    0x0229,
    0x022A,
    0x022B,
    0x022C,
    0x022D,
    0x022E,
    0x022F,
    0x0230,
    0x0231,
    0x0232,
    0x0233,
    0x0234,
    0x0235,
    0x0236,
    0x0237,
    0x0238,
    0x0239,
    0x023A,
    0x023B,
    0x023C,
    0x023D,
    0x023E,
    0x023F,
    0x0240,
    0x0241,
    0x0242,
    0x0243,
    0x0244,
    0x0245,
    0x0246,
    0x0247,
    0x0248,
    0x0249,
    0x024A,
    0x024B,
    0x024C,
    0x024D,
    0x024E,
    0x021F,
    0x013C,
    0x013D,
    0x013E,
    };


    /** Map of parameter -> index in the allParameters array. */
    static HashMap parametersToIndex = null;


    /** List of all Emu proteus1orchestral parameters in order. */
                
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
    "link1",
    "link2",
    "link3",
    "lowkey0",
    "lowkey1",
    "lowkey2",
    "lowkey3",
    "highkey0",
    "highkey1",
    "highkey2",
    "highkey3",
    "i1instrument",
    "i1samplestartoffset",
    "i1tuningcoarse",
    "i1tuningfine",
    "i1volume",
    "i1pan",
    "i1delay",
    "i1lowkey",
    "i1highkey",
    "i1attack",
    "i1hold",
    "i1decay",
    "i1sustain",
    "i1release",
    "i1envelopeon",
    "i1solomode",
    "i1chorus",
    "i1reversesound",
    "i2instrument",
    "i2samplestartoffset",
    "i2tuningcoarse",
    "i2tuningfine",
    "i2volume",
    "i2pan",
    "i2delay",
    "i2lowkey",
    "i2highkey",
    "i2attack",
    "i2hold",
    "i2decay",
    "i2sustain",
    "i2release",
    "i2envelopeon",
    "i2solomode",
    "i2chorus",
    "i2reversesound",
    "crossfademode",
    "crossfadedirection",
    "crossfadebalance",
    "crossfadeamount",
    "switchpoint",
    "lfo1shape",
    "lfo1frequency",
    "lfo1delay",
    "lfo1variation",
    "lfo1amount",
    "lfo2shape",
    "lfo2frequency",
    "lfo2delay",
    "lfo2variation",
    "lfo2amount",
    "i3delay",
    "i3attack",
    "i3hold",
    "i3decay",
    "i3sustain",
    "i3release",
    "i3amount",
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
    };
    
    public static final String[] FOOTSWITCH_DESTINATIONS = new String[]
    {
    "Off",
    "Sustain",
    "Primary Sustain",
    "Secondary Sustain",
    "Alternate Volume Env",
    "Primary Alternate Volume Env",
    "Secondary Alternate Volume Env",
    "Alternate Volume Rel",
    "Primary Alternate Volume Rel",
    "Secondary Alternate Volume Rel",
    "Cross-Switch"
    };

    public static final String[] REALTIME_SOURCES = new String[]
    {
    "Pitch Wheel",
    "MIDI Control A",
    "MIDI Control B",
    "MIDI Control C",
    "MIDI Control D",
    "Mono Pressure",
    "Polyphonic Pressure",
    "LFO 1",
    "LFO 2",
    "Auxilliary Envelope"
    };
        
    public static final String[] REALTIME_DESTINATIONS = new String[]
    {
    "Off",
    "Pitch",
    "Primary Pitch",
    "Secondary Pitch",
    "Volume",
    "Primary Volume",
    "Secondary Volume",
    "Attack",
    "Primary Attack",
    "Secondary Attack",
    "Decay",
    "Primary Decay",
    "Secondary Decay",
    "Release",
    "Primary Release",
    "Secondary Release",
    "Crossfade",
    "LFO 1 Amount",
    "LFO 1 Rate",
    "LFO 2 Amount",
    "LFO 2 Rate",
    "Auxiliary Envelope Amount",
    "Auxiliary Envelope Attack",
    "Auxiliary Envelope Decay",
    "Auxiliary Envelope Release"
    };


    public static final String[] KEYVEL_SOURCES = new String[] { "Key", "Velocity" };

    public static final String[] KEYVEL_DESTINATIONS = new String[]
    {
    "Off",
    "Pitch",
    "Primary Pitch",
    "Secondary Pitch",
    "Volume",
    "Primary Volume",
    "Secondary Volume",
    "Attack",
    "Primary Attack",
    "Secondary Attack",
    "Decay",
    "Primary Decay",
    "Secondary Decay",
    "Release",
    "Primary Release",
    "Secondary Release",
    "Crossfade",
    "LFO 1 Amount",
    "LFO 1 Rate",
    "LFO 2 Amount",
    "LFO 2 Rate",
    "Auxiliary Envelope Amount",
    "Auxiliary Envelope Attack",
    "Auxiliary Envelope Decay",
    "Auxiliary Envelope Release",
    "Sample Start",
    "Primary Sample Start",
    "Secondary Sample Start",
    "Pan",
    "Primary Pan",
    "Secondary Pan",
    "Tone",
    "Primary Tone",
    "Secondary Tone"
    };

        

    public static final String[] PROTEUS_1_INSTRUMENTS = new String[]
    {
    "No Instrument",
    "Piano",
    "Piano Pad",
    "Loose Piano",
    "Tight Piano",
    "Strings",
    "Long Strings",
    "Slow Strings",
    "Dark Strings",
    "Voices",
    "Slow Voices",
    "Dark Choir",
    "Synth Flute",
    "Soft Flute",
    "Alto Sax",
    "Tenor Sax",
    "Baritone Sax",
    "Dark Sax",
    "Soft Trumpet",
    "Dark Soft Trumpet",
    "Hard Trumpet",
    "Dark Hard Trumpet",
    "Horn Falls",
    "Trombone 1",
    "Trombone 2",
    "French Horn",
    "Brass 1",
    "Brass 2",
    "Brass 3",
    "Trombone/Sax",
    "Guitar Mute",
    "Electric Guitar",
    "Acoustic Guitar",
    "Rock Bass",
    "Stone Bass",
    "Flint Bass",
    "Funk Slap",
    "Funk Pop",
    "Harmonics",
    "Rock/Harmonics",
    "Stone/Harmonics",
    "Nose Bass",
    "Bass Synth 1",
    "Bass Synth 2",
    "Synth Pad",
    "Medium Envelope Pad",
    "Long Envelope Pad",
    "Dark Synth",
    "Percussive Organ",
    "Marimba",
    "Vibraphone",
    "All Percussion (Balanced)",
    "All Percussion (Unbalanced)",
    "Standard Percussion Setup 1",
    "Standard Percussion Setup 2",
    "Standard Percussion Setup 3",
    "Kicks",
    "Snares",
    "Toms",
    "Cymbals",
    "Latin Drums",
    "Latin Percussion",
    "Agogo Bell",
    "Woodblock",
    "Conga",
    "Timbale",
    "Ride Cymbal",
    "Percussion FX1",
    "Percussion FX2",
    "Metal",
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
    "Synth Cycle 1",
    "Synth Cycle 2",
    "Synth Cycle 3",
    "Synth Cycle 4",
    "Fundamental Gone 1",
    "Fundamental Gone 2",
    "Bite Cycle",
    "Buzzy Cycle 1",
    "Metalphone 1",
    "Metalphone 2",
    "Metalphone 3",
    "Metalphone 4",
    "Duck Cycle 1",
    "Duck Cycle 2",
    "Duck Cycle 3",
    "Wind Cycle 1",
    "Wind Cycle 2",
    "Wind Cycle 3",
    "Wind Cycle 4",
    "Organ Cycle 1",
    "Organ Cycle 2",
    "Noise",
    "Stray Voice 1",
    "Stray Voice 2",
    "Stray Voice 3",
    "Stray Voice 4",
    "Synth String 1",
    "Synth String 2",
    "Animals",
    "Reed",
    "Pluck 1",
    "Pluck 2",
    "Mallet 1",
    "Mallet 2",
    };

    public static final String[] PROTEUS_1_ORCHESTRAL_INSTRUMENTS = new String[]
    {
    "No Instrument",
    "Piano",
    "Piano Pad",
    "Loose Piano",
    "Tight Piano",
    "Strings",
    "Long Strings",
    "Slow Strings",
    "Dark Strings",
    "Voices",
    "Slow Voices",
    "Dark Choir",
    "Synth Flute",
    "Soft Flute",
    "Alto Sax",
    "Tenor Sax",
    "Baritone Sax",
    "Dark Sax",
    "Soft Trumpet",
    "Dark Soft Trumpet",
    "Hard Trumpet",
    "Dark Hard Trumpet",
    "Horn Falls",
    "Trombone 1",
    "Trombone 2",
    "French Horn",
    "Brass 1",
    "Brass 2",
    "Brass 3",
    "Trombone/Sax",
    "Guitar Mute",
    "Electric Guitar",
    "Acoustic Guitar",
    "Rock Bass",
    "Stone Bass",
    "Flint Bass",
    "Funk Slap",
    "Funk Pop",
    "Harmonics",
    "Rock/Harmonics",
    "Stone/Harmonics",
    "Nose Bass",
    "Bass Synth 1",
    "Bass Synth 2",
    "Synth Pad",
    "Medium Envelope Pad",
    "Long Envelope Pad",
    "Dark Synth",
    "Percussive Organ",
    "Marimba",
    "Vibraphone",
    "All Percussion (Balanced)",
    "All Percussion (Unbalanced)",
    "Standard Percussion Setup 1",
    "Standard Percussion Setup 2",
    "Standard Percussion Setup 3",
    "Kicks",
    "Snares",
    "Toms",
    "Cymbals",
    "Latin Drums",
    "Latin Percussion",
    "Agogo Bell",
    "Woodblock",
    "Conga",
    "Timbale",
    "Ride Cymbal",
    "Percussion FX1",
    "Percussion FX2",
    "Metal",
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
    "Synth Cycle 1",
    "Synth Cycle 2",
    "Synth Cycle 3",
    "Synth Cycle 4",
    "Fundamental Gone 1",
    "Fundamental Gone 2",
    "Bite Cycle",
    "Buzzy Cycle 1",
    "Metalphone 1",
    "Metalphone 2",
    "Metalphone 3",
    "Metalphone 4",
    "Duck Cycle 1",
    "Duck Cycle 2",
    "Duck Cycle 3",
    "Wind Cycle 1",
    "Wind Cycle 2",
    "Wind Cycle 3",
    "Wind Cycle 4",
    "Organ Cycle 1",
    "Organ Cycle 2",
    "Noise",
    "Stray Voice 1",
    "Stray Voice 2",
    "Stray Voice 3",
    "Stray Voice 4",
    "Synth String 1",
    "Synth String 2",
    "Animals",
    "Reed",
    "Pluck 1",
    "Pluck 2",
    "Mallet 1",
    "Mallet 2",
    "Solo Cello",
    "Solo Viola",
    "Solo Violin",
    "Gambambo",
    "Quartet 1",
    "Quartet 2",
    "Quartet 3",
    "Quartet 4",
    "Pizz Basses",
    "Pizz Celli",
    "Pizz Violas",
    "Pizz Violin",
    "Pizzicombo",
    "Bass Clarinet",
    "Clarinet",
    "Bass Clarinet/Clarinet",
    "Contra Bassoon",
    "Bassoon",
    "English Horn",
    "Oboe",
    "Woodwinds",
    "Harmon Mute",
    "Tubular Bell",
    "Timpani",
    "Timpani/Tubular Bell",
    "Tamborine",
    "Tam Tam",
    "Percussion 3",
    "Special Effects",
    "Oboe noVib",
    "Upright Pizz",
    "Sine Wave",
    "Triangle Wave",
    "Square Wave",
    "Pulse 33%",
    "Pulse 25%",
    "Pulse 10%",
    "Sawtooth",
    "Sawtooth Odd Gone",
    "Ramp",
    "Ramp Even Only",
    "Violin Essence",
    "Buzzoon",
    "Brassy Wave",
    "Reedy Buzz",
    "Growl Wave",
    "HarpsiWave",
    "Fuzzy Gruzz",
    "Power 5ths",
    "Filtered Saw",
    "Ice Bell",
    "Bronze Age",
    "Iron Plate",
    "Aluminum",
    "Lead Beam",
    "Steel Extract",
    "Winter Glass",
    "Town Bell Wash",
    "Orchestral Bells",
    "Tubular SE",
    "Soft Bell Wave",
    "Swirly",
    "Tack Attack",
    "Shimmer Wave",
    "Moog Lead",
    "B3 SE",
    "Mild Tone",
    "Piper",
    "Ah Wave",
    "Vocal Wave",
    "Fuzzy Clav",
    "Electrhode",
    "Whine 1",
    "Whine 2",
    "Metal Drone",
    "Silver Race",
    "Metal Attack",
    "Filter Bass",
    };
        
    public static final String[] PROTEUS_2_INSTRUMENTS = new String[]
    {
    "No Instrument",
    "Arco Basses",
    "Arco Celli",
    "Arco Violas",
    "Arco Violin",
    "Dark Basses",
    "Dark Celli",
    "Dark Violas",
    "Dark Violin",
    "Low Tremolo",
    "High Tremolo",
    "Tremolande",
    "Strings 1",
    "Strings 2",
    "Strings 3",
    "Solo Cello",
    "Solo Viola",
    "Solo Violin",
    "Quartet 1",
    "Quartet 2",
    "Quartet 3",
    "Quartet 4",
    "Gambambo",
    "Pizz Basses",
    "Pizz Celli",
    "Pizz Violas",
    "Pizz Violin",
    "Pizzicombo",
    "Flute w/Vib",
    "Flute noVib",
    "Flute",
    "Piccolo",
    "Bass Clarinet",
    "Clarinet",
    "Bass Clarinet/Clarinet",
    "Contra Bassoon",
    "Bassoon",
    "English Horn",
    "Oboe w/Vib",
    "Oboe noVib",
    "Oboe",
    "Woodwinds",
    "Hi Trombone",
    "Lo Trombone",
    "MF Trumpet",
    "FF Trumpet",
    "Harmon Mute",
    "MF French Horn",
    "FF French Horn",
    "Tuba",
    "FF Brass",
    "MF Brass",
    "Harp",
    "Xylophone",
    "Celesta",
    "Triangle",
    "Bass Drum",
    "Snare Drum",
    "Piatti",
    "Temple Block",
    "Glockenspiel",
    "Percussion 1",
    "Percussion 2",
    "Low Percussion 2",
    "High Percussion 2",
    "Tubular Bell",
    "Timpani",
    "Timpani/Tubular Bell",
    "Tambourine",
    "Tam Tam",
    "Percussion 3",
    "Special Effects",
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
    "Sine Wave",
    "Triangle Wave",
    "Square Wave",
    "Pulse 33%",
    "Pulse 25%",
    "Pulse 10%",
    "Sawtooth",
    "Sawtooth Odd Gone",
    "Ramp",
    "Ramp Even Only",
    "Violin Essence",
    "Buzzoon",
    "Brassy Wave",
    "Reedy Buzz",
    "Growl Wave",
    "HarpsiWave",
    "Fuzzy Gruzz",
    "Power 5ths",
    "Filtered Saw",
    "Ice Bell",
    "Bronze Age",
    "Iron Plate",
    "Aluminum",
    "Lead Beam",
    "Steel Extract",
    "Winter Glass",
    "Town Bell Wash",
    "Orchestral Bells",
    "Tubular SE",
    "Soft Bell Wave",
    "Swirly",
    "Tack Attack",
    "Shimmer Wave",
    "Moog Lead",
    "B3 SE",
    "Mild Tone",
    "Piper",
    "Ah Wave",
    "Vocal Wave",
    "Fuzzy Clav",
    "Electrhode",
    "Whine 1",
    "Whine 2",
    "Metal Drone",
    "Silver Race",
    "Metal Attack",
    "Filter Bass",
    "Upright Pizz",
    "Nylon Pluck 1",
    "Nylon Pluck 2",
    "Plucked Bass",
    };

    public static final String[] PROTEUS_3_INSTRUMENTS = new String[]
    {
    "No Instrument",
    "Renaissance",
    "East Indian",
    "Folk America",
    "Down Under",
    "Troubadour",
    "Irish Harp",
    "Dulcimer",
    "Koto",
    "Banjo",
    "Hi Tar",
    "Guitar",
    "Sitar",
    "Tamburas",
    "Psaltry",
    "Waterphone 1",
    "Waterphone 2",
    "Pizz Bass",
    "Tam/Sitar",
    "Accordion",
    "Harmonica",
    "Harmonica",
    "Mizmars",
    "Shanai",
    "Penny Whistle",
    "Ocarina",
    "Shofar A",
    "Shofar B",
    "Shofars",
    "Siku",
    "Shakuhachi",
    "Ney Flute",
    "Bagpipe Drone",
    "Chanter A",
    "Chanter B",
    "Drone/ChanterA",
    "Drone/ChanterB",
    "Roarer/Catcher",
    "Bull Roarer",
    "Spirit Catcher",
    "Didjeridu",
    "Didjeridu A",
    "Didjeridu B",
    "Didjeridu C",
    "Jews Harp",
    "Jews Harp A",
    "Jews Harp B",
    "Jews Harp C",
    "Jews Harp D",
    "Trombone",
    "French Horn",
    "Trumpet",
    "Mid East Drum",
    "Udu Drum",
    "Bata Drums",
    "The Tabla",
    "Wood Drum",
    "Gamelan",
    "Bonang Kenong",
    "Kenong Bonang",
    "Seribu Pulau",
    "Surdo Drum",
    "Maracas",
    "Plexitones",
    "Traps",
    "All Percussion",
    "All Percussion 1P",
    "All Percussion",
    "Snare Drum",
    "Kck Drum",
    "Hi-Hat Closed",
    "Hi-Hat Open",
    "Wood Block",
    "Steel Drum",
    "Castanet",
    "Buzz/Likembe",
    "Likembe",
    "Likembe Buzz",
    "Surdo open",
    "Surdo Mute",
    "Deff Slap",
    "Deff Mute",
    "Bendir",
    "Req Open",
    "Req Slap",
    "Maraca A",
    "Maraca B",
    "Maraca C",
    "Maraca D",
    "Udu Tone",
    "Udu Release",
    "Udu Finger",
    "Udu Slap",
    "Bata Ipu Tone",
    "Bata Ipu Slap",
    "Bata Enu Tone",
    "Bata Hi Tone",
    "Bata Hi Mute",
    "Bata Hi Slap",
    "Crickets",
    "Baya Tone",
    "Baya Slap",
    "Baya Hit",
    "Tabla Tone",
    "Tabla Mute A",
    "Tabla Mute B",
    "Tabla Mute C",
    "Tabla Open",
    "China Gong",
    "Nepal Cymbal",
    "Tibetan Bowl",
    "Crotales",
    "Bonang",
    "Kenong",
    "Saron",
    "Suwuk Gong",
    "Clapper Stick",
    "Rosewood Bass",
    "Rosewood Tick",
    "Rosewood Harm.",
    "Rosewood Finger",
    "Tanzanian Shaker",
    "Hula Stick",
    "Log Drum",
    "Plexi-Tone",
    "Plexi-Slap A",
    "Plexi-Slap B",
    "Plexi-Slap C",
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
    "Synth Cycle 1",
    "Synth Cycle 2",
    "Synth Cycle 3",
    "Synth Cycle 4",
    "Fundamental Gone 1",
    "Fundamental Gone 2",
    "Bite Cycle",
    "Buzzy Cycle 1",
    "Metalphone 1",
    "Metalphone 2",
    "Metalphone 3",
    "Metalphone 4",
    "Duck Cycle 1",
    "Duck Cycle 2",
    "Duck Cycle 3",
    "Wind Cycle 1",
    "Wind Cycle 2",
    "Wind Cycle 3",
    "Wind Cycle 4",
    "Organ Cycle 1",
    "Organ Cycle 2",
    "Noise",
    "Dark Noise",
    "Triangle",
    "Square",
    "Sawtooth",
    "Sawtooth Odd Gone",
    "Ramp",
    "Ramp Even Only",
    "Violin Essence",
    "Buzzoon",
    "Brassy Wave",
    "Reedy Buzz",
    "Growl Wave",
    "HarpsiWave",
    "Fuzzy Gruzz",
    "Power 5ths",
    "Filtered Saw",
    "Ice Bell",
    "Bronze Age",
    "Iron Plate",
    "Aluminum",
    "Lead Beam",
    "Steel Extract",
    "Winter Glass",
    "Asian Gongs 1",
    "Asian Gongs 2",
    "Suwak Wave",
    "Savannah Land",
    "Swamp Thing",
    "Bugs 1",
    "Bugs 2",
    "Bugs 3",
    "Bugs 4",
    "Bugs 5",
    "Bugs 6",
    "Crickets",
    "Woodpecker 1",
    "Woodpecker 2",
    "Frogz",
    "Tribe 1",
    "Tribe 2",
    };
    
    }
