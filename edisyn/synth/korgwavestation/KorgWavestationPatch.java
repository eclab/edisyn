/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.korgwavestation;

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
   A patch editor for the Korg SG RACK [Multi].  Note NOT for the SG Pro X.
        
   @author Sean Luke
*/

public class KorgWavestationPatch extends KorgWavestationAbstract
    {
    /// Various collections of parameter names for pop-up menus
    
    public static final String[] WAVES = new String[] { "1:   A", "2:   A C", "4:   A B C D" };
    public static final String[] LOOPS = new String[] { "Off", "0 -> 3", "1 -> 3", "2 -> 3", "0 <-> 3", "1 <-> 3", "2 <-> 3" };
    public static final String[] PITCH_MACROS = new String[] { "Default", "Envelope 1 Bend", "Descending", "Ascending", "Aftertouch Bend", "MIDI Bend", "Aftertouch + MIDI Bend" };
    public static final String[] FILTER_MACROS = new String[] { "Bypass", "Low Pass", "Low Pass / LFO", "Aftertouch Sweep" };
    public static final String[] AMPLITUDE_ENVELOPE_MACROS = new String[] { "Default", "Piano", "Organ", "Organ Release", "Brass", "String", "Clav", "Drum", "Ramp", "On", "Off" };
    public static final String[] PAN_MACROS = new String[] { "Keyboard", "Velocity", "Keyboard + Velocity", "Off" };
    public static final String[] ENVELOPE_1_MACROS = AMPLITUDE_ENVELOPE_MACROS;
    public static final String[] LFO_SHAPES = new String[] { "Triangle", "Square", "Sawtooth", "Ramp", "Random" };
    public static final String[] OSCILLATORS = new String[] { "A", "B", "C", "D" };
    
    public SynthPanel[] panels = new SynthPanel[4];

    public HashMap subkeysToSubparameters = new HashMap();
    public HashMap keysToParameters = new HashMap();

    public KorgWavestationPatch()
        {
        for(int i = 0; i < keys.length; i++)
            {
            keysToParameters.put(keys[i], parameters[i]);
            }
                        
        for(int i = 0; i < subkeys.length; i++)
            {
            subkeysToSubparameters.put(subkeys[i], Integer.valueOf(subparameters[i]));
            }

        JComponent soundPanel = new SynthPanel(this);
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        hbox.addLast(addPatch(Style.COLOR_A()));
        vbox.add(hbox);
        vbox.addLast(add2DMixer(Style.COLOR_B()));
        
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Global", soundPanel);

        for(int i = 1; i <= 4; i++)
            {
            soundPanel = panels[i - 1] = new SynthPanel(this);
            panels[i - 1].makePasteable("osc" + i);
                        
            vbox = new VBox();
            hbox = new HBox();
            hbox.add(addOscillatorMain(i, Style.COLOR_A()));
            hbox.addLast(addFilter(i, Style.COLOR_B()));
            vbox.add(hbox);

            hbox = new HBox();
            hbox.add(addPitch(i, Style.COLOR_C()));
            hbox.addLast(addAmplifier(i, Style.COLOR_C()));
            vbox.add(hbox);


            hbox = new HBox();
            hbox.add(addLFO(i, 1, Style.COLOR_B()));
            hbox.addLast(addTuning(i, Style.COLOR_C()));
            vbox.add(hbox);

            hbox = new HBox();
            hbox.add(addLFO(i, 2, Style.COLOR_B()));
            hbox.addLast(addPan(i, Style.COLOR_C()));
            vbox.add(hbox);

            vbox.add(addEnvelope(i, 1, Style.COLOR_A()));
            vbox.add(addEnvelope(i, 2, Style.COLOR_A()));

            hbox = new HBox();
            //hbox.addLast(addMacros(i, Style.COLOR_C()));
            vbox.add(hbox);
                        
            soundPanel.add(vbox, BorderLayout.CENTER);
            addTab("Wave " + OSCILLATORS[i - 1], soundPanel);
            }
                
        model.set("name", "Init");
        
        model.set("number", 0);
        model.set("bank", 0);

        model.set("numoscillators", 0); // one oscillator

        loadDefaults();
        }
 
    public JFrame sprout()     
        {
        JFrame frame = super.sprout();
        addWavestationMenu();
        receiveCurrent.setEnabled(false);  // we can't request the "current" patch
        writeTo.setEnabled(false);  // can't write patches
        tabChanged();  // set up first time
        return frame;
        }

    public void addWavestationMenu(JMenu menu)
        {
        JMenuItem sendTestPerformanceMenu = new JMenuItem("Set up Test Performance in RAM 1 Slot 0");
        sendTestPerformanceMenu.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                sendTestPerformance();
                }
            });
        menu.add(sendTestPerformanceMenu);
        }
            
    public String getDefaultResourceFileName() { return "KorgWavestationPatch.init"; }
    public String getHTMLResourceFileName() { return "KorgWavestationPatch.html"; }
                
                
              
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
        comp = new PatchDisplay(this, 9);
        hbox2.add(comp);
        vbox.add(hbox2);
        
        comp = new StringComponent("Patch Name", this, "name", 15, "Name must be up to 15 ASCII characters.")
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
        
        hbox.add(Strut.makeHorizontalStrut(80));

        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }
        
    public String revisePatchName(String name)
        {
        name = super.revisePatchName(name);
        if (name == null) name = "";
        char[] chars = name.toCharArray();
        for(int i = 0; i < chars.length; i++)
            {
            if (chars[i] < 32 || chars[i] > 127)
                chars[i] = ' ';
            }
        return new String(chars);
        }

    public JComponent addPatch(Color color)
        {
        Category category  = new Category(this, "Patch", color);
                        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        params = WAVES;
        comp = new Chooser("Number of Waves", this, "numoscillators", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                                
                if (getNumTabs() == 0)  // haven't been set up yet
                    return;
                    
                String title = getSelectedTabTitle();
                                
                removeTab("Wave A");
                removeTab("Wave B");
                removeTab("Wave C");
                removeTab("Wave D");

                switch (model.get(key, 0))
                    {
                    case 0:
                        insertTab("Wave A", panels[0], 1);
                        break;
                    case 1:
                        insertTab("Wave A", panels[0], 1);
                        insertTab("Wave C", panels[2], 2);
                        break;
                    default:
                        insertTab("Wave A", panels[0], 1);
                        insertTab("Wave B", panels[1], 2);
                        insertTab("Wave C", panels[2], 3);
                        insertTab("Wave D", panels[3], 4);
                        break;
                    }
                
                int t = getIndexOfTabTitle(title);
                if (t == -1)
                    setSelectedTabIndex(0);
                else
                    setSelectedTabIndex(t);
                }
            };
        vbox.add(comp);
                
        comp = new CheckBox("Hard Sync", this, "hardsync", false);
        ((CheckBox)comp).addToWidth(4);
        vbox.add(comp);    
                
        category.add(vbox, BorderLayout.WEST);
        return category;
        }

    public JComponent add2DMixer(Color color)
        {
        Category category  = new Category(this, "2D Mixer", color);
        category.makeDistributable("mix");
                  
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        VBox main = new VBox();
        HBox mainH = new HBox();
        
        params = LOOPS;
        comp = new Chooser("Loop", this, "mixloop", params);
        vbox.add(comp);
        hbox.add(vbox);
                
        comp = new LabelledDial("Loop", this, "mixlooprepeats", color, 0, 127)
            {
            public String map(int val)
                {
                if (val == 0) return "Off";
                else if (val == 127) return "Inf";
                else return "" + val;
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Repeats");
        hbox.add(comp);
        main.add(hbox);

        hbox = new HBox();
        vbox = new VBox();

        Color[] colors = new Color[] { Style.COLOR_A(), Style.COLOR_B(), Style.COLOR_C(), Style.COLOR_GLOBAL(), Style.DYNAMIC_COLOR() };
        
        // it appears that although the mix values are -127 to +127, they're sent as 0...254 as parameters!
        comp = new LabelledDial("AC 0", this, "mixx0", color, 0, 254, 127)
            {
            public boolean isSymmetric() { return true; }
            };
        ((LabelledDial)comp).getJLabel().setForeground(colors[0]);
        Component c1 = comp;
        hbox.add(comp);

        comp = new LabelledDial("AC 1", this, "mixx1", color,  0, 254, 127)
            {
            public boolean isSymmetric() { return true; }
            };
        ((LabelledDial)comp).getJLabel().setForeground(colors[1]);
        hbox.add(comp);

        comp = new LabelledDial("AC 2", this, "mixx2", color,  0, 254, 127)
            {
            public boolean isSymmetric() { return true; }
            };
        ((LabelledDial)comp).getJLabel().setForeground(colors[2]);
        hbox.add(comp);

        comp = new LabelledDial("AC 3", this, "mixx3", color,  0, 254, 127)
            {
            public boolean isSymmetric() { return true; }
            };
        ((LabelledDial)comp).getJLabel().setForeground(colors[3]);
        ((LabelledDial)comp).addAdditionalLabel("(Sustain)");
        hbox.add(comp);

        comp = new LabelledDial("AC 4", this, "mixx4", color,  0, 254, 127)
            {
            public boolean isSymmetric() { return true; }
            };
        ((LabelledDial)comp).getJLabel().setForeground(colors[4]);
        ((LabelledDial)comp).addAdditionalLabel("(Release)");
        hbox.add(comp);

        params = SOURCES;
        comp = new Chooser("AC Mod 1 Source", this, "mixxmod1source", params);
        vbox.add(comp);
                
        params = SOURCES;
        comp = new Chooser("AC Mod 2 Source", this, "mixxmod2source", params);
        vbox.add(comp);
        hbox.add(vbox);
                
        comp = new LabelledDial("AC Mod 1", this, "mixxmod1amount", color, -127, 127);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        comp = new LabelledDial("AC Mod 2", this, "mixxmod2amount", color, -127, 127);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);
        
        main.add(hbox);        
        hbox = new HBox();

        // it appears that although the mix values are -127 to +127, they're sent as 0...254 as parameters!
        comp = new LabelledDial("BD 0", this, "mixy0", color, 0, 254, 127)
            {
            public boolean isSymmetric() { return true; }
            };
        ((LabelledDial)comp).getJLabel().setForeground(colors[0]);
        hbox.add(comp);

        comp = new LabelledDial("BD 1", this, "mixy1", color, 0, 254, 127)
            {
            public boolean isSymmetric() { return true; }
            };
        ((LabelledDial)comp).getJLabel().setForeground(colors[1]);
        hbox.add(comp);
        
        comp = new LabelledDial("BD 2", this, "mixy2", color, 0, 254, 127)
            {
            public boolean isSymmetric() { return true; }
            };
        ((LabelledDial)comp).getJLabel().setForeground(colors[2]);
        hbox.add(comp);
    
        comp = new LabelledDial("BD 3", this, "mixy3", color, 0, 254, 127)
            {
            public boolean isSymmetric() { return true; }
            };
        ((LabelledDial)comp).getJLabel().setForeground(colors[3]);
        ((LabelledDial)comp).addAdditionalLabel("(Sustain)");
        hbox.add(comp);
    
        comp = new LabelledDial("BD 4", this, "mixy4", color, 0, 254, 127)
            {
            public boolean isSymmetric() { return true; }
            };
        ((LabelledDial)comp).getJLabel().setForeground(colors[4]);
        ((LabelledDial)comp).addAdditionalLabel("(Release)");
        hbox.add(comp);

        vbox = new VBox();
        params = SOURCES;
        comp = new Chooser("BD Mod 1 Source", this, "mixymod1source", params);
        vbox.add(comp);
                
        params = SOURCES;
        comp = new Chooser("BD Mod 2 Source", this, "mixymod2source", params);
        vbox.add(comp);
        hbox.add(vbox);
                
        comp = new LabelledDial("BD Mod 1", this, "mixymod1amount", color, -127, 127);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        comp = new LabelledDial("BD Mod 2", this, "mixymod2amount", color, -127, 127);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        main.add(hbox);
        hbox = new HBox();

        hbox.add(Strut.makeStrut(c1));

        comp = new LabelledDial("Time to 1", this, "mixtime1", color, 0, 99);
        hbox.add(comp);
        
        comp = new LabelledDial("Time to 2", this, "mixtime2", color, 0, 99);
        hbox.add(comp);
    
        comp = new LabelledDial("Time to 3", this, "mixtime3", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("(Sustain)");
        hbox.add(comp);
    
        comp = new LabelledDial("Time to 4", this, "mixtime4", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("(Release)");
        hbox.add(comp);
        main.add(hbox);
        mainH.add(main);
        mainH.add(Strut.makeHorizontalStrut(40));
        mainH.add(Stretch.makeVerticalStretch());
                
        final KorgWavestationJoystick joy = new KorgWavestationJoystick(this, 
            new String[] { "mixx0", "mixx1", "mixx2", "mixx3", "mixx4" },
            new String[] { "mixy0", "mixy1", "mixy2", "mixy3", "mixy4" });
        joy.setNumPositions(colors);
        joy.updateAll();
        joy.setDrawsUnpressedCursor(false);
        mainH.addLast(joy);
        
        category.add(mainH, BorderLayout.CENTER);
        return category;
        }

    boolean updatingBankWave = false;
    JButton[] showButton = new JButton[4];

    public JComponent addOscillatorMain(final int osc, Color color)
        {
        Category category  = new Category(this, "Wave " + OSCILLATORS[osc - 1], color);
        category.makePasteable("osc" + osc);
                        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        

        params = buildWaves(model.get("osc" + osc + "bank", 0));
        final Chooser waves = new Chooser("Wave", this, "osc" + osc + "wave", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                
                int bank = model.get("osc" + osc + "wavebank", 0);
                model.set("osc" + osc + "wavebank", bank);
                
                // Do we display the "Show" button?
                if (showButton[osc - 1] != null)
                    {
                    showButton[osc - 1].setEnabled(model.get(key, 0) < 32);
                    }
                }
            };

        comp = new LabelledDial("Wave Bank", this, "osc" + osc + "wavebank", color, 0, BANK_CARD)
            {
            public String map(int val)
                {
                String[] vals = BANKS;
                return vals[val];
                }
                
            public void update(String key, Model model)
                {
                // We also need to update the wave at the same time
                if (updatingBankWave) return;  // break the cycle
                updatingBankWave = true; 
                super.update(key, model);
                
                int wave = model.get("osc" + osc + "wave", 0);
                // maybe should turn off MIDI transmission here so the chooser doesn't send a bunch of gunk
                boolean previousMIDI = getSendMIDI();
                setSendMIDI(false);
                waves.setElements("Wave", buildWaves(model.get(key, 0)));
                setSendMIDI(previousMIDI);
                model.set("osc" + osc + "wave", wave);
                updatingBankWave = false;
                }
            };
        ((LabelledDial)comp).addAdditionalLabel(" ");
        model.removeMetricMinMax("osc" + osc + "bank");
        hbox.add(comp);

        HBox hbox2 = new HBox();
        hbox2.add(waves);
        
        comp = new PushButton("Mute Others")
            {
            public void perform()
                {
                setSolo();
                }
            };
        Marginalizer marg = new Marginalizer();
        marg.addBottom(comp);
        hbox2.add(marg);

        comp = new PushButton("Unmute All")
            {
            public void perform()
                {
                resetSolo();
                }
            };
        marg = new Marginalizer();
        marg.addBottom(comp);
        hbox2.add(marg);
                
        vbox.add(hbox2);

        hbox2 = new HBox();
                
        // remember we also have the Wave_Num_Exp expansion
                
        comp = new PushButton("Show")
            {
            public void perform()
                {
                // nothing for the time being
                final KorgWavestationSequence synth = new KorgWavestationSequence();
                if (tuple != null)
                    synth.tuple = tuple.copy(synth.buildInReceiver(), synth.buildKeyReceiver());
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
                                Model tempModel = new Model();
                                tempModel.set("bank", KorgWavestationPatch.this.model.get("osc" + osc + "wavebank"));
                                tempModel.set("number", KorgWavestationPatch.this.model.get("osc" + osc + "wave"));
                                synth.requestingPatch = true; // otherwise the wave sequence editor will ask us for the number to load
                                synth.performRequestDump(tempModel, false);
                                }
                            });
                    }
                else
                    {
                    showSimpleError("Disconnected", "You can't show a patch when disconnected.");
                    }
                }
            };
        showButton[osc - 1] = ((PushButton)comp).getButton();
        hbox2.add(showButton[osc - 1]);

        // Do we display the "Show" button?
        showButton[osc - 1].setEnabled(model.get("osc" + osc + "wave", 0) < 32);

        comp = new CheckBox("Bus A", this, "osc" + osc + "busa", false);
        hbox2.add(comp);
        comp = new CheckBox("Bus B", this, "osc" + osc + "busb", false);
        hbox2.add(comp);
        comp = new CheckBox("Bus C", this, "osc" + osc + "busc", false);
        hbox2.add(comp);
        comp = new CheckBox("Bus D", this, "osc" + osc + "busd", false);
        ((CheckBox)comp).addToWidth(2);
        hbox2.add(comp);

        vbox.add(hbox2);
        hbox.add(vbox);
        
        category.add(hbox, BorderLayout.WEST);
        return category;
        }




    public JComponent addLFO(int osc, int lfo, Color color)
        {
        Category category  = new Category(this, "LFO " + lfo, color);
        category.makePasteable("osc" + osc + "lfo" + lfo);
                        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        params = SOURCES;
        comp = new Chooser("Depth Mod Source", this, "osc" + osc + "lfo" + lfo + "depthmodsource", params);
        vbox.add(comp);
                
        params = SOURCES;
        comp = new Chooser("Rate Mod Source", this, "osc" + osc + "lfo" + lfo + "ratemodsource", params);
        vbox.add(comp);
                
        hbox.add(vbox);

        vbox = new VBox();
        params = LFO_SHAPES;
        comp = new Chooser("Shape", this, "osc" + osc + "lfo" + lfo + "shape", params);
        vbox.add(comp);
                
        comp = new CheckBox("Sync", this, "osc" + osc + "lfo" + lfo + "sync", false);
        ((CheckBox)comp).addToWidth(1);
        vbox.add(comp);
        hbox.add(vbox);

        hbox.add(vbox);
                
        comp = new LabelledDial("Rate", this, "osc" + osc + "lfo" + lfo + "rate", color, 0, 99);
        hbox.add(comp);

        comp = new LabelledDial("Amount", this, "osc" + osc + "lfo" + lfo + "amount", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Delay", this, "osc" + osc + "lfo" + lfo + "delay", color, 0, 99);
        hbox.add(comp);

        comp = new LabelledDial("Fade In", this, "osc" + osc + "lfo" + lfo + "fadein", color, 0, 99);
        hbox.add(comp);

        comp = new LabelledDial("Depth Mod", this, "osc" + osc + "lfo" + lfo + "depthmod", color, -127, 127);
        hbox.add(comp);

        comp = new LabelledDial("Rate Mod", this, "osc" + osc + "lfo" + lfo + "ratemod", color, -127, 127);
        hbox.add(comp);

        category.add(hbox, BorderLayout.WEST);
        return category;
        }    





// Note we have stuff like osc1env4level0a rather than osc1env4level0 so that we can properly
// cut and paste (when the numbers get stripped, the "a" differentiates between level0 and, say, level1)


    public JComponent addEnvelope(int osc, int env, Color color)
        {
        Category category  = new Category(this, (env == 1 ? "Envelope 1" : "Amplitude Envelope"), color);
        category.makePasteable("osc" + osc + "env" + env);
                        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        comp = new LabelledDial("Level 0", this, "osc" + osc + "env" + env + "level0a", color, 0, 99);
        hbox.add(comp);

        comp = new LabelledDial("Time 1", this, "osc" + osc + "env" + env + "time1b", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("(Attack)");
        hbox.add(comp);

        comp = new LabelledDial("Level 1", this, "osc" + osc + "env" + env + "level1b", color, 0, 99);
        hbox.add(comp);

        comp = new LabelledDial("Time 2", this, "osc" + osc + "env" + env + "time2c", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("(Decay)");
        hbox.add(comp);

        comp = new LabelledDial("Level 2", this, "osc" + osc + "env" + env + "level2c", color, 0, 99);
        hbox.add(comp);

        comp = new LabelledDial("Time 3", this, "osc" + osc + "env" + env + "time3d", color, 0, 99);
        hbox.add(comp);

        LabelledDial levelthree = new LabelledDial("Level 3", this, "osc" + osc + "env" + env + "level3d", color, 0, 99);
        levelthree.addAdditionalLabel("(Sustain)");
        hbox.add(levelthree);

        comp = new LabelledDial("Time 4", this, "osc" + osc + "env" + env + "time4e", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("(Release)");
        hbox.add(comp);

        if (env == 1)
            {
            comp = new LabelledDial("Level 4", this, "osc" + osc + "env" + env + "level4e", color, 0, 99);
            hbox.add(comp);
            }
        else
            {
            hbox.add(Strut.makeStrut(levelthree));
            }

        comp = new LabelledDial("Attack", this, "osc" + osc + "env" + env + "attackvelocitymod", color, -127, 127);
        ((LabelledDial)comp).addAdditionalLabel("Velocity Mod");
        hbox.add(comp);

        comp = new LabelledDial("Env Amount", this, "osc" + osc + "env" + env + "amountvelocitymod", color, -127, 127);
        ((LabelledDial)comp).addAdditionalLabel("Velocity Mod");
        hbox.add(comp);

        comp = new LabelledDial("Decay/Release", this, "osc" + osc + "env" + env + "ratekeyboardmod", color, -127, 127);
        ((LabelledDial)comp).addAdditionalLabel("Keyboard Mod");
        hbox.add(comp);
                
        if (env == 1)
            {
            comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
                new String[] { null,  "osc" + osc + "env" + env + "time1b", "osc" + osc + "env" + env + "time2c", "osc" + osc + "env" + env + "time3d", null, "osc" + osc + "env" + env + "time4e" },
                new String[] { "osc" + osc + "env" + env + "level0a",  "osc" + osc + "env" + env + "level1b",  "osc" + osc + "env" + env + "level2c",  "osc" + osc + "env" + env + "level3d",  "osc" + osc + "env" + env + "level3d", "osc" + osc + "env" + env + "level4e" },
                new double[] { 0, 0.2 / 99, 0.2 / 99, 0.2 / 99, 0.2, 0.2 / 99 },
                new double[] { 1.0 / 99,  1.0 / 99, 1.0 / 99, 1.0 / 99, 1.0 / 99, 1.0 / 99 });
            hbox.addLast(comp);
            }
        else
            {
            comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
                new String[] { null,  "osc" + osc + "env" + env + "time1b", "osc" + osc + "env" + env + "time2c", "osc" + osc + "env" + env + "time3d", null, "osc" + osc + "env" + env + "time4e"},
                new String[] { "osc" + osc + "env" + env + "level0a",  "osc" + osc + "env" + env + "level1b",  "osc" + osc + "env" + env + "level2c",  "osc" + osc + "env" + env + "level3d",  "osc" + osc + "env" + env + "level3d", null },
                new double[] { 0, 0.2 / 99, 0.2 / 99, 0.2 / 99, 0.2, 0.2 / 99 },
                new double[] { 1.0 / 99,  1.0 / 99, 1.0 / 99, 1.0 / 99, 1.0 / 99, 0 });
            hbox.addLast(comp);
            }

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }    



/*
  public JComponent addMacros(int osc, Color color)
  {
  Category category  = new Category(this, "Macros", color);
                        
  JComponent comp;
  String[] params;
  HBox hbox = new HBox();
  VBox vbox = new VBox();
        
  params = PITCH_MACROS;
  comp = new Chooser("Pitch Bend Macro", this, "osc" + osc + "pitchbendmacro", params);
  vbox.add(comp);

  params = PAN_MACROS;
  comp = new Chooser("Pan Macro", this, "osc" + osc + "panmacro", params);
  vbox.add(comp);
  hbox.add(vbox);
                
  vbox = new VBox();
  params = AMPLITUDE_ENVELOPE_MACROS;
  comp = new Chooser("Amplitude Envelope Macro", this, "osc" + osc + "amplitudeenvelopemacro", params);
  vbox.add(comp);

  params = ENVELOPE_1_MACROS;
  comp = new Chooser("Envelope 1 Macro", this, "osc" + osc + "envelope1macro", params);
  vbox.add(comp);

  hbox.add(vbox);

  vbox = new VBox();
  params = FILTER_MACROS;
  comp = new Chooser("Filter Macro", this, "osc" + osc + "filtermacro", params);
  vbox.add(comp);

  hbox.add(vbox);

  category.add(hbox, BorderLayout.WEST);
  return category;
  }
*/

    public JComponent addFilter(int osc, Color color)
        {
        Category category  = new Category(this, "Filter", color);
        category.makePasteable("osc" + osc);
                        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        params = SOURCES;
        comp = new Chooser("Mod Source 1", this, "osc" + osc + "filtermod1source", params);
        vbox.add(comp);

        params = SOURCES;
        comp = new Chooser("Mod Source 2", this, "osc" + osc + "filtermod2source", params);
        vbox.add(comp);

        hbox.add(vbox);

        comp = new LabelledDial("Cutoff", this, "osc" + osc + "filtercutoff", color, 0, 99);
        hbox.add(comp);

        comp = new LabelledDial("Exciter", this, "osc" + osc + "filterexciter", color, 0, 99);
        hbox.add(comp);

        comp = new LabelledDial("Keyboard", this, "osc" + osc + "filterkeyboardtracking", color, -127, 127);
        ((LabelledDial)comp).addAdditionalLabel("Tracking");
        hbox.add(comp);

        comp = new LabelledDial("Mod 1", this, "osc" + osc + "filtermod1amount", color, -127, 127);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        comp = new LabelledDial("Mod 2", this, "osc" + osc + "filtermod2amount", color, -127, 127);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        category.add(hbox, BorderLayout.WEST);
        return category;
        }



    public JComponent addAmplifier(int osc, Color color)
        {
        Category category  = new Category(this, "Amplifier", color);
        category.makePasteable("osc" + osc);
                        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        params = SOURCES;
        comp = new Chooser("Mod 1 Source", this, "osc" + osc + "ampmod1source", params);
        vbox.add(comp);

        params = SOURCES;
        comp = new Chooser("Mod 2 Source", this, "osc" + osc + "ampmod2source", params);
        vbox.add(comp);
        hbox.add(vbox);
                
        comp = new LabelledDial("Mod 1", this, "osc" + osc + "ampmod1amount", color, -127, 127);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        comp = new LabelledDial("Mod 2", this, "osc" + osc + "ampmod2amount", color, -127, 127);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        comp = new LabelledDial("Level", this, "osc" + osc + "amplevel", color, 0, 99);
        hbox.add(comp);

        category.add(hbox, BorderLayout.WEST);
        return category;
        }




    public JComponent addPan(int osc, Color color)
        {
        Category category  = new Category(this, "Pan", color);
        category.makePasteable("osc" + osc);
                        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        comp = new LabelledDial("Velocity Mod", this, "osc" + osc + "panvelocitymod", color, -127, 127);
        hbox.add(comp);
        
        comp = new LabelledDial("Keyboard Mod", this, "osc" + osc + "pankeyboardmod", color, -127, 127);
        hbox.add(comp);

        category.add(hbox, BorderLayout.WEST);
        return category;
        }
        

    public JComponent addTuning(int osc, Color color)
        {
        Category category  = new Category(this, "Tuning", color);
        category.makePasteable("osc" + osc);
                        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        

        comp = new LabelledDial("Coarse", this, "osc" + osc + "tuningcoarse", color, -24, 24);
        hbox.add(comp);

        comp = new LabelledDial("Fine", this, "osc" + osc + "tuningfine", color, -99, 99);
        hbox.add(comp);


        // actual values are -200 ... 200 by 2
        comp = new LabelledDial("Slope", this, "osc" + osc + "tuningslope", color, -100, 100)
            {
            public int getDefaultValue()
                {
                return 50;      // 1.0
                }
            public String map(int val)
                {
                return "" + (val / 50.0);
                }
            };
        hbox.add(comp);

        category.add(hbox, BorderLayout.WEST);
        return category;
        }




    public JComponent addPitch(int osc, Color color)
        {
        Category category  = new Category(this, "Pitch", color);
        category.makePasteable("osc" + osc);
                        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
 
        params = SOURCES;
        comp = new Chooser("Mod Source 1", this, "osc" + osc + "pitchbendmod1source", params);
        vbox.add(comp);

        params = SOURCES;
        comp = new Chooser("Mod Source 2", this, "osc" + osc + "pitchbendmod2source", params);
        vbox.add(comp);

        hbox.add(vbox);
                
        comp = new LabelledDial("Mod 1", this, "osc" + osc + "pitchbendmod1amount", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        comp = new LabelledDial("Mod 2", this, "osc" + osc + "pitchbendmod2amount", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        comp = new LabelledDial("Bend", this, "osc" + osc + "pitchbendrange", color, -1, 12)
            {
            public String map(int val)
                {
                if (val == 0) return "Off";
                else if (val == -1) return "Global";
                else return "" + val;
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Range");
        hbox.add(comp);
        model.setMetricMin("osc" + osc + "pitchbendrange", 0);

       
        comp = new LabelledDial("Ramp", this, "osc" + osc + "pitchrampamount", color, -127, 127);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);


        comp = new LabelledDial("Ramp Time", this, "osc" + osc + "pitchramptime", color, 0, 100)
            {
            public String map(int val)
                {
                if (val == 100) return "On";
                else return "" + val;
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Ramp", this, "osc" + osc + "pitchrampvelocitymod", color, -127, 127);
        ((LabelledDial)comp).addAdditionalLabel("Velocity Mod");
        hbox.add(comp);

        
        category.add(hbox, BorderLayout.WEST);
        return category;
        }


/*
  params = PATCH_OUTPUTS;
  comp = new Chooser("Output", this, "osc" + osc + "output", params);
  vbox.add(comp);
  hbox.add(vbox);




  category.add(hbox, BorderLayout.WEST);
  return category;
  }    
*/



    // This is a list of keys for which you must first call CURRENT_WAVE and then
    // the appropriate parameter.   The full key is called
    // osc<N><SUBKEY> where N is 1...4.
    public static final String[] subkeys = new String[]
    {
    "pitchbendmod1source", 
    "pitchbendmod2source", 
    "pitchbendmod1amount", 
    "pitchbendmod2amount", 
    "pitchbendrange", 
    "pitchrampamount", 
    "pitchramptime", 
    "pitchrampvelocitymod", 
    "lfo1ratemodsource", 
    "lfo1depthmodsource", 
    "lfo1shape", 
    "lfo1sync", 
    "lfo1rate", 
    "lfo1amount", 
    "lfo1delay", 
    "lfo1fadein", 
    "lfo1ratemod", 
    "lfo1depthmod", 
    "ampmod1source", 
    "ampmod2source", 
    "ampmod1amount", 
    "ampmod2amount", 
    "lfo2ratemodsource", 
    "lfo2depthmodsource", 
    "lfo2shape", 
    "lfo2sync", 
    "lfo2rate", 
    "lfo2amount", 
    "lfo2delay", 
    "lfo2fadein", 
    "lfo2ratemod", 
    "lfo2depthmod", 
    "pankeyboardmod", 
    "panvelocitymod", 
    "env1level0a", 
    "env1time1b", 
    "env1level1b", 
    "env1time2c", 
    "env1level2c", 
    "env1time3d", 
    "env1level3d", 
    "env1time4e", 
    "env1level4e", 
    "env1amountvelocitymod", 
    "env1attackvelocitymod", 
    "env1ratekeyboardmod", 
    "env2level0a", 
    "env2time1b", 
    "env2level1b", 
    "env2time2c", 
    "env2level2c", 
    "env2time3d", 
    "env2level3d", 
    "env2time4e", 
    "env2amountvelocitymod", 
    "env2attackvelocitymod", 
    "env2ratekeyboardmod", 
    "filtermod1source", 
    "filtermod2source", 
    "filtercutoff", 
    "filterexciter", 
    "filterkeyboardtracking", 
    "filtermod1amount", 
    "filtermod2amount", 
    //"pitchbendmacro", 
    //"panmacro", 
    //"amplitudeenvelopemacro", 
    //"envelope1macro", 
    //"filtermacro"
    };
        
    // This is a list of parameter for which you must first call CURRENT_WAVE and then
    // the appropriate parameter.   The full key is called
    // osc<N><SUBKEY> where N is 1...4.
    public static final int[] subparameters = new int[]
    {
    PITCH_SOURCE_1, 
    PITCH_SOURCE_2, 
    PITCH_SOURCE_1_AMOUNT,
    PITCH_SOURCE_2_AMOUNT, 
    PATCH_PITCH_BEND_RANGE, 
    PATCH_PITCH_RAMP_AMT, 
    PATCH_PITCH_RAMP_RATE,
    PATCH_PITCH_VEL_AMT, 
    LFO1_RATE_MOD_SOURCE,
    LFO1_DEPTH_MOD_SOURCE, 
    LFO1_SHAPE,
    LFO1_SYNC,
    LFO1_RATE,
    LFO1_INITIAL_AMOUNT,
    LFO1_DELAY,
    LFO1_FADE_IN,
    LFO1_RATE_MOD_SRC_AMT,
    LFO1_DEPTH_MOD_SRC_AMT,
    AMP_MOD_SOURCE_1, 
    AMP_MOD_SOURCE_2, 
    AMP_MOD_SOURCE_1_AMOUNT, 
    AMP_MOD_SOURCE_2_AMOUNT, 
    LFO2_RATE_MOD_SOURCE, 
    LFO2_DEPTH_MOD_SOURCE, 
    LFO2_SHAPE, 
    LFO2_SYNC, 
    LFO2_RATE, 
    LFO2_INITIAL_AMOUNT, 
    LFO2_DELAY, 
    LFO2_FADE_IN, 
    LFO2_RATE_MOD_SRC_AMT, 
    LFO2_DEPTH_MOD_SRC_AMT, 
    PAN_KEYBOARD_AMOUNT, 
    PAN_VELOCITY_AMOUNT, 
    GP_ENV_LEVEL_0, 
    GP_ENV_RATE_1, 
    GP_ENV_LEVEL_1, 
    GP_ENV_RATE_2, 
    GP_ENV_LEVEL_2, 
    GP_ENV_RATE_3, 
    GP_ENV_LEVEL_3, 
    GP_ENV_RATE_4, 
    GP_ENV_LEVEL_4, 
    GP_VEL_ENV_AMT, 
    ENV1_MOD_VEL_RATE, 
    ENV1_MOD_KBD_RATE,
    AMP_ENV_LEVEL_0, 
    AMP_ENV_RATE_1, 
    AMP_ENV_LEVEL_1, 
    AMP_ENV_RATE_2, 
    AMP_ENV_LEVEL_2, 
    AMP_ENV_RATE_3, 
    AMP_ENV_LEVEL_3, 
    AMP_ENV_RATE_4, 
    AMP_MOD_VEL_ENV_AMOUNT, 
    AMP_MOD_VEL_ATTACK_RATE, 
    AMP_MOD_KBD_DECAY_RATE,
    FILTER_MOD_SOURCE1, 
    FILTER_MOD_SOURCE2, 
    FILTER_MOD_CUTOFF, 
    FILTER_EXCITER_AMOUNT, 
    FILTER_MOD_TRACKING, 
    FILTER_MOD_SOURCE1_AMT, 
    FILTER_MOD_SOURCE2_AMT, 
    //PATCH_PITCH_MACRO, 
    //PATCH_PAN_MACRO, 
    //PATCH_AMP_MACRO, 
    //PATCH_ENV_MACRO, 
    //PATCH_FILTER_MACRO      
    };

    // This is a list of keys which have unique parameters (as opposed to subkeys and subparameters as
    // mentioned earlier) and which don't need special handling.
    public static final String[] keys = new String[]
    {
    "osc1wavebank",
    "osc1wave",
    "osc1amplevel",
    "osc1tuningcoarse", 
    "osc1tuningfine", 
    "osc1tuningslope", 
    "osc1busa",
    "osc1busb",
    "osc1busc",
    "osc1busd",
    "osc2wavebank",
    "osc2wave",
    "osc2amplevel",
    "osc2tuningcoarse", 
    "osc2tuningfine", 
    "osc2tuningslope", 
    "osc2busa",
    "osc2busb",
    "osc2busc",
    "osc2busd",
    "osc3wavebank",
    "osc3wave",
    "osc3amplevel",
    "osc3tuningcoarse", 
    "osc3tuningfine", 
    "osc3tuningslope", 
    "osc3busa",
    "osc3busb",
    "osc3busc",
    "osc3busd",
    "osc4wavebank",
    "osc4wave",
    "osc4amplevel",
    "osc4tuningcoarse", 
    "osc4tuningfine", 
    "osc4tuningslope", 
    "osc4busa",
    "osc4busb",
    "osc4busc",
    "osc4busd",
    "hardsync",
    "mixloop",
    "mixlooprepeats",
    "mixxmod1source",
    "mixxmod2source",
    "mixymod1source",
    "mixymod2source",
    "mixxmod1amount",
    "mixxmod2amount",
    "mixymod1amount",
    "mixymod2amount",
    "numoscillators",
    };
                
    // This is a list of unique parameters (as opposed to subkeys and subparameters as
    // mentioned earlier) which don't need special handling.
    public static final int[] parameters = new int[]
    {
    WAVEA_BANK,
    WAVEA_NUM,
    WAVEA_LEVEL,
    WAVEA_TUNE_COARSE, 
    WAVEA_TUNE_FINE, 
    WAVEA_TUNE_SLOPE, 
    WAVEA_BUS_A,
    WAVEA_BUS_B,
    WAVEA_BUS_C,
    WAVEA_BUS_D,
    WAVEB_BANK,
    WAVEB_NUM,
    WAVEB_LEVEL,
    WAVEB_TUNE_COARSE, 
    WAVEB_TUNE_FINE, 
    WAVEB_TUNE_SLOPE, 
    WAVEB_BUS_A,
    WAVEB_BUS_B,
    WAVEB_BUS_C,
    WAVEB_BUS_D,
    WAVEC_BANK,
    WAVEC_NUM,
    WAVEC_LEVEL,
    WAVEC_TUNE_COARSE, 
    WAVEC_TUNE_FINE, 
    WAVEC_TUNE_SLOPE, 
    WAVEC_BUS_A,
    WAVEC_BUS_B,
    WAVEC_BUS_C,
    WAVEC_BUS_D,
    WAVED_BANK,
    WAVED_NUM,
    WAVED_LEVEL,
    WAVED_TUNE_COARSE, 
    WAVED_TUNE_FINE, 
    WAVED_TUNE_SLOPE, 
    WAVED_BUS_A,
    WAVED_BUS_B,
    WAVED_BUS_C,
    WAVED_BUS_D,
    PATCH_HARD_SYNC,
    MIX_ENV_LOOP,
    MIX_ENV_REPEATS,
    MIX_MOD_X_SOURCE1,
    MIX_MOD_X_SOURCE2,
    MIX_MOD_Y_SOURCE1,
    MIX_MOD_Y_SOURCE2,
    MIX_MOD_X_SRC1_AMT,
    MIX_MOD_X_SRC2_AMT,
    MIX_MOD_Y_SRC1_AMT,
    MIX_MOD_Y_SRC2_AMT,
    PATCH_STRUCTURE,
    };

    public int getPauseAfterSendOneParameter() 
        { 
        // without this, we seem to drop some packets as we overfill the buffer, unfortunately
        return 40; 
        }

    public Object[] emitAll(String key)
        {
        if (key.equals("bank")) return new Object[0];  // this is not emittable
        if (key.equals("number")) return new Object[0];  // this is not emittable
                
        /*
          byte[] bank_mesg = paramBytes(CURRENT_BANK, edisynToWSBank[model.get("bank", 0)]);
          byte[] num_mesg = paramBytes(CURRENT_PROG, model.get("number", 0));
          byte[] part_mesg = paramBytes(CURRENT_PART, 1);
        */

        // there aren't enough unique parameters to bother with a hashtable, so we'll just hard-code it here.

        if (key.equals("name"))
            {
            byte[] mesg = paramBytes(PART_PATCH_NAME, model.get(key, ""));
            return new byte[][] { /*bank_mesg, num_mesg, part_mesg,*/ mesg };
            }
        else 
            {
            int index = 0;
            int val = model.get(key, 0);
                        
            if (keysToParameters.containsKey(key))  // this has to be before "osc" because there are a few that start with "osc" in here
                {
                if (key.endsWith("bank"))
                    { 
                    val = edisynToWSBank[model.get(key, 0)];        // ugh, banks are mixed up
                    }
                else if (key.endsWith("tuningslope"))
                    { 
                    val = val * 2;
                    }

                index = ((Integer)(keysToParameters.get(key))).intValue();
                byte[] mesg = paramBytes(index, val);
                return new byte[][] { /*bank_mesg, num_mesg, part_mesg,*/ mesg };
                }
            else if (key.startsWith("osc"))
                {
                int osc = extractNumbers(key)[0];
                String subkey = key.substring(4);
                index = ((Integer)(subkeysToSubparameters.get(subkey))).intValue();
                byte[] wave_mesg = paramBytes(CURRENT_WAVE, osc - 1);
                byte[] mesg = paramBytes(index, val);
                return new byte[][] { /*bank_mesg, num_mesg, part_mesg,*/ wave_mesg, mesg };
                }
            else if (key.startsWith("mix"))
                {
                for(int i = 0; i <= 4; i++)
                    {
                    if (key.equals("mixx" + i))
                        {
                        byte[] mix_mesg = paramBytes(MIX_ENV_POINT, i);
                        byte[] mesg = paramBytes(MIX_ENV_X, val);
                        return new byte[][] { /*bank_mesg, num_mesg, part_mesg,*/ mix_mesg, mesg };
                        }
                    else if (key.equals("mixy" + i))
                        {
                        byte[] mix_mesg = paramBytes(MIX_ENV_POINT, i);
                        byte[] mesg = paramBytes(MIX_ENV_Y, val);
                        return new byte[][] { /*bank_mesg, num_mesg, part_mesg,*/ mix_mesg, mesg };
                        }
                    else if (key.equals("mixtime" + i))
                        {
                        byte[] mix_mesg = paramBytes(MIX_ENV_POINT, i);
                        byte[] mesg = paramBytes(MIX_ENV_RATE, val);
                        return new byte[][] { /*bank_mesg, num_mesg, part_mesg,*/ mix_mesg, mesg };
                        }
                    }
                }
            }

        // uh oh
        System.err.println("Warning (KorgWavestationPatch): Unknown Key " + key);
        return new Object[0];
        }


    class Indiv
        {
        int waveCoarse;
        int waveFine;
        int waveBank;
        int waveNum;
        int waveScale;
        int lfo1Rate;
        int lfo1Amt;
        int lfo1Delay;
        int lfo1Fade;
        int lfo1Shape;
        int s1Lfo1R;
        int s1Lfo1RAmt;
        int s1Lfo1A;
        int s1Lfo1AAmt;
        int lfo2Rate;
        int lfo2Amt;
        int lfo2Delay;
        int lfo2Fade;
        int lfo2Shape;
        int s1Lfo2R;
        int s1Lfo2RAmt;
        int s1Lfo2A;
        int s1Lfo2AAmt;
        int egRate1;
        int egRate2;
        int egRate3;
        int egRate4;
        int egLevel0;
        int egLevel1;
        int egLevel2;
        int egLevel3;
        int egLevel4;
        int egLevel;
        int velEgA;
        int aegRate1;
        int aegRate2;
        int aegRate3;
        int aegRate4;
        int aegLevel0;
        int aegLevel1;
        int aegLevel2;
        int aegLevel3;
        int pitchMac;
        int filMac;
        int ampMac;
        int panMac;
        int envMac;
        int pwRange;
        int s1Pitch;
        int s1PitchAmt;
        int s2Pitch;
        int s2PitchAmt;
        int keyFilter;
        int s1Filter;
        int s1FilterAmt;
        int s2Filter;
        int s2FilterAmt;
        int velAegA;
        int velAegR;
        int keyAegR;
        int s1Amp;
        int s1AmpAmt;
        int s2Amp;
        int s2AmpAmt;
        int keyPanAmt;
        int velPanAmt;
        int cutoff;
        int filterExciter;
        int velEgR;
        int keyEgR;
        int pegAmt;
        int pegRate;
        int velPegA;
        int indivLevel;
        long lfo1Inc;
        long lfo2Inc;
        int patchOutput;
        int waveNumExp;
        
        int read(byte[] data, int pos)
            {
            waveCoarse = readByte(data, pos);
            pos += 1;
            waveFine = readByte(data, pos);
            pos += 1;
            waveBank = readUByte(data, pos);
            pos += 1;
            waveNum = readUWord(data, pos);
            pos += 2;
            waveScale = readByte(data, pos);
            pos += 1;
            lfo1Rate = readUByte(data, pos);
            pos += 1;
            lfo1Amt = readUByte(data, pos);
            pos += 1;
            lfo1Delay = readUByte(data, pos);
            pos += 1;
            lfo1Fade = readUByte(data, pos);
            pos += 1;
            lfo1Shape = readUByte(data, pos);
            pos += 1;
            s1Lfo1R = readByte(data, pos);
            pos += 1;
            s1Lfo1RAmt = readByte(data, pos);
            pos += 1;
            s1Lfo1A = readByte(data, pos);
            pos += 1;
            s1Lfo1AAmt = readByte(data, pos);
            pos += 1;
            lfo2Rate = readUByte(data, pos);
            pos += 1;
            lfo2Amt = readUByte(data, pos);
            pos += 1;
            lfo2Delay = readUByte(data, pos);
            pos += 1;
            lfo2Fade = readUByte(data, pos);
            pos += 1;
            lfo2Shape = readUByte(data, pos);
            pos += 1;
            s1Lfo2R = readByte(data, pos);
            pos += 1;
            s1Lfo2RAmt = readByte(data, pos);
            pos += 1;
            s1Lfo2A = readByte(data, pos);
            pos += 1;
            s1Lfo2AAmt = readByte(data, pos);
            pos += 1;
            egRate1 = readUByte(data, pos);
            pos += 1;
            egRate2 = readUByte(data, pos);
            pos += 1;
            egRate3 = readUByte(data, pos);
            pos += 1;
            egRate4 = readUByte(data, pos);
            pos += 1;
            egLevel0 = readUByte(data, pos);
            pos += 1;
            egLevel1 = readUByte(data, pos);
            pos += 1;
            egLevel2 = readUByte(data, pos);
            pos += 1;
            egLevel3 = readUByte(data, pos);
            pos += 1;
            egLevel4 = readUByte(data, pos);
            pos += 1;
            velEgA = readByte(data, pos);
            pos += 1;
            aegRate1 = readUByte(data, pos);
            pos += 1;
            aegRate2 = readUByte(data, pos);
            pos += 1;
            aegRate3 = readUByte(data, pos);
            pos += 1;
            aegRate4 = readUByte(data, pos);
            pos += 1;
            aegLevel0 = readUByte(data, pos);
            pos += 1;
            aegLevel1 = readUByte(data, pos);
            pos += 1;
            aegLevel2 = readUByte(data, pos);
            pos += 1;
            aegLevel3 = readUByte(data, pos);
            pos += 1;
            pitchMac = readByte(data, pos);
            pos += 1;
            filMac = readByte(data, pos);
            pos += 1;
            ampMac = readByte(data, pos);
            pos += 1;
            panMac = readByte(data, pos);
            pos += 1;
            envMac = readByte(data, pos);
            pos += 1;
            pwRange = readByte(data, pos);
            pos += 1;
            s1Pitch = readByte(data, pos);
            pos += 1;
            s1PitchAmt = readByte(data, pos);
            pos += 1;
            s2Pitch = readByte(data, pos);
            pos += 1;
            s2PitchAmt = readByte(data, pos);
            pos += 1;
            keyFilter = readByte(data, pos);
            pos += 1;
            s1Filter = readByte(data, pos);
            pos += 1;
            s1FilterAmt = readByte(data, pos);
            pos += 1;
            s2Filter = readByte(data, pos);
            pos += 1;
            s2FilterAmt = readByte(data, pos);
            pos += 1;
            velAegA = readByte(data, pos);
            pos += 1;
            velAegR = readByte(data, pos);
            pos += 1;
            keyAegR = readByte(data, pos);
            pos += 1;
            s1Amp = readByte(data, pos);
            pos += 1;
            s1AmpAmt = readByte(data, pos);
            pos += 1;
            s2Amp = readByte(data, pos);
            pos += 1;
            s2AmpAmt = readByte(data, pos);
            pos += 1;
            keyPanAmt = readByte(data, pos);
            pos += 1;
            velPanAmt = readByte(data, pos);
            pos += 1;
            cutoff = readUByte(data, pos);
            pos += 1;
            filterExciter = readUByte(data, pos);
            pos += 1;
            velEgR = readByte(data, pos);
            pos += 1;
            keyEgR = readByte(data, pos);
            pos += 1;
            pegAmt = readByte(data, pos);
            pos += 1;
            pegRate = readUByte(data, pos);
            pos += 1;
            velPegA = readByte(data, pos);
            pos += 1;
            indivLevel = readByte(data, pos);
            pos += 1;
            lfo1Inc = readULong(data, pos);
            pos += 4;
            lfo2Inc = readULong(data, pos);
            pos += 4;
            patchOutput = readByte(data, pos);
            pos += 1;
            waveNumExp = readByte(data, pos);
            pos += 1;
            return pos;
            }
                
        int write(byte[] data, int pos)
            {
            writeByte(waveCoarse, data, pos);
            pos += 1;
            writeByte(waveFine, data, pos);
            pos += 1;
            writeUByte(waveBank, data, pos);
            pos += 1;
            writeUWord(waveNum, data, pos);
            pos += 2;
            writeByte(waveScale, data, pos);
            pos += 1;
            writeUByte(lfo1Rate, data, pos);
            pos += 1;
            writeUByte(lfo1Amt, data, pos);
            pos += 1;
            writeUByte(lfo1Delay, data, pos);
            pos += 1;
            writeUByte(lfo1Fade, data, pos);
            pos += 1;
            writeUByte(lfo1Shape, data, pos);
            pos += 1;
            writeByte(s1Lfo1R, data, pos);
            pos += 1;
            writeByte(s1Lfo1RAmt, data, pos);
            pos += 1;
            writeByte(s1Lfo1A, data, pos);
            pos += 1;
            writeByte(s1Lfo1AAmt, data, pos);
            pos += 1;
            writeUByte(lfo2Rate, data, pos);
            pos += 1;
            writeUByte(lfo2Amt, data, pos);
            pos += 1;
            writeUByte(lfo2Delay, data, pos);
            pos += 1;
            writeUByte(lfo2Fade, data, pos);
            pos += 1;
            writeUByte(lfo2Shape, data, pos);
            pos += 1;
            writeByte(s1Lfo2R, data, pos);
            pos += 1;
            writeByte(s1Lfo2RAmt, data, pos);
            pos += 1;
            writeByte(s1Lfo2A, data, pos);
            pos += 1;
            writeByte(s1Lfo2AAmt, data, pos);
            pos += 1;
            writeUByte(egRate1, data, pos);
            pos += 1;
            writeUByte(egRate2, data, pos);
            pos += 1;
            writeUByte(egRate3, data, pos);
            pos += 1;
            writeUByte(egRate4, data, pos);
            pos += 1;
            writeUByte(egLevel0, data, pos);
            pos += 1;
            writeUByte(egLevel1, data, pos);
            pos += 1;
            writeUByte(egLevel2, data, pos);
            pos += 1;
            writeUByte(egLevel3, data, pos);
            pos += 1;
            writeUByte(egLevel4, data, pos);
            pos += 1;
            writeByte(velEgA, data, pos);
            pos += 1;
            writeUByte(aegRate1, data, pos);
            pos += 1;
            writeUByte(aegRate2, data, pos);
            pos += 1;
            writeUByte(aegRate3, data, pos);
            pos += 1;
            writeUByte(aegRate4, data, pos);
            pos += 1;
            writeUByte(aegLevel0, data, pos);
            pos += 1;
            writeUByte(aegLevel1, data, pos);
            pos += 1;
            writeUByte(aegLevel2, data, pos);
            pos += 1;
            writeUByte(aegLevel3, data, pos);
            pos += 1;
            writeByte(pitchMac, data, pos);
            pos += 1;
            writeByte(filMac, data, pos);
            pos += 1;
            writeByte(ampMac, data, pos);
            pos += 1;
            writeByte(panMac, data, pos);
            pos += 1;
            writeByte(envMac, data, pos);
            pos += 1;
            writeByte(pwRange, data, pos);
            pos += 1;
            writeByte(s1Pitch, data, pos);
            pos += 1;
            writeByte(s1PitchAmt, data, pos);
            pos += 1;
            writeByte(s2Pitch, data, pos);
            pos += 1;
            writeByte(s2PitchAmt, data, pos);
            pos += 1;
            writeByte(keyFilter, data, pos);
            pos += 1;
            writeByte(s1Filter, data, pos);
            pos += 1;
            writeByte(s1FilterAmt, data, pos);
            pos += 1;
            writeByte(s2Filter, data, pos);
            pos += 1;
            writeByte(s2FilterAmt, data, pos);
            pos += 1;
            writeByte(velAegA, data, pos);
            pos += 1;
            writeByte(velAegR, data, pos);
            pos += 1;
            writeByte(keyAegR, data, pos);
            pos += 1;
            writeByte(s1Amp, data, pos);
            pos += 1;
            writeByte(s1AmpAmt, data, pos);
            pos += 1;
            writeByte(s2Amp, data, pos);
            pos += 1;
            writeByte(s2AmpAmt, data, pos);
            pos += 1;
            writeByte(keyPanAmt, data, pos);
            pos += 1;
            writeByte(velPanAmt, data, pos);
            pos += 1;
            writeUByte(cutoff, data, pos);
            pos += 1;
            writeUByte(filterExciter, data, pos);
            pos += 1;
            writeByte(velEgR, data, pos);
            pos += 1;
            writeByte(keyEgR, data, pos);
            pos += 1;
            writeByte(pegAmt, data, pos);
            pos += 1;
            writeUByte(pegRate, data, pos);
            pos += 1;
            writeByte(velPegA, data, pos);
            pos += 1;
            writeByte(indivLevel, data, pos);
            pos += 1;
            writeULong(lfo1Inc, data, pos);
            pos += 4;
            writeULong(lfo2Inc, data, pos);
            pos += 4;
            writeByte(patchOutput, data, pos);
            pos += 1;
            writeByte(waveNumExp, data, pos);
            pos += 1;
            return pos;
            }
        }
    
    class Patch
        {
        char[] name = new char[16];

        public void setName(String val)
            {
            val = val + "                ";
            System.arraycopy(val.toCharArray(), 0, name, 0, name.length);
            }
                
        int mixRate1;
        int mixRate2;
        int mixRate3;
        int mixRate4;
        int mixCount1;
        int mixCount2;
        int mixCount3;
        int mixCount3B;
        int mixCount2B;
        int mixCount1B;
        int mixCount4;
        long mixXSlope1;
        long mixXSlope2;
        long mixXSlope3;
        long mixXSlope4;
        long mixYSlope1;
        long mixYSlope2;
        long mixYSlope3;
        long mixYSlope4;
        int mixX0;
        int mixX1;
        int mixX2;
        int mixX3;
        int mixX4;
        int mixY0;
        int mixY1;
        int mixY2;
        int mixY3;
        int mixY4;
        int mixRepeats;
        int mixEnvLoop;
        int s1MixAc;
        int s1MixAcAmt;
        int s2MixAc;
        int s2MixAcAmt;
        int s1MixBd;
        int s1MixBdAmt;
        int s2MixBd;
        int s2MixBdAmt;
        int numberOfWaves;
        int hardSync;
        int bankExp;
        int dummy141;
        Indiv[] waves = new Indiv[4];

        public Patch()
            {
            for(int i = 0; i < waves.length; i++)
                waves[i] = new Indiv();
            }

        int read(byte[] data, int pos)
            {
            for(int i = 0; i < 16; i++)
                name[i] = (char)data[i + pos];
            pos += 16;
            mixRate1 = readUByte(data, pos);
            pos += 1;
            mixRate2 = readUByte(data, pos);
            pos += 1;
            mixRate3 = readUByte(data, pos);
            pos += 1;
            mixRate4 = readUByte(data, pos);
            pos += 1;
            mixCount1 = readUWord(data, pos);
            pos += 2;
            mixCount2 = readUWord(data, pos);
            pos += 2;
            mixCount3 = readUWord(data, pos);
            pos += 2;
            mixCount3B = readUWord(data, pos);
            pos += 2;
            mixCount2B = readUWord(data, pos);
            pos += 2;
            mixCount1B = readUWord(data, pos);
            pos += 2;
            mixCount4 = readUWord(data, pos);
            pos += 2;
            mixXSlope1 = readULong(data, pos);
            pos += 4;
            mixXSlope2 = readULong(data, pos);
            pos += 4;
            mixXSlope3 = readULong(data, pos);
            pos += 4;
            mixXSlope4 = readULong(data, pos);
            pos += 4;
            mixYSlope1 = readULong(data, pos);
            pos += 4;
            mixYSlope2 = readULong(data, pos);
            pos += 4;
            mixYSlope3 = readULong(data, pos);
            pos += 4;
            mixYSlope4 = readULong(data, pos);
            pos += 4;
            mixX0 = readUByte(data, pos);
            pos += 1;
            mixX1 = readUByte(data, pos);
            pos += 1;
            mixX2 = readUByte(data, pos);
            pos += 1;
            mixX3 = readUByte(data, pos);
            pos += 1;
            mixX4 = readUByte(data, pos);
            pos += 1;
            mixY0 = readUByte(data, pos);
            pos += 1;
            mixY1 = readUByte(data, pos);
            pos += 1;
            mixY2 = readUByte(data, pos);
            pos += 1;
            mixY3 = readUByte(data, pos);
            pos += 1;
            mixY4 = readUByte(data, pos);
            pos += 1;
            mixRepeats = readUByte(data, pos);
            pos += 1;
            mixEnvLoop = readUByte(data, pos);
            pos += 1;
            s1MixAc = readUByte(data, pos);
            pos += 1;
            s1MixAcAmt = readByte(data, pos);
            pos += 1;
            s2MixAc = readUByte(data, pos);
            pos += 1;
            s2MixAcAmt = readByte(data, pos);
            pos += 1;
            s1MixBd = readUByte(data, pos);
            pos += 1;
            s1MixBdAmt = readByte(data, pos);
            pos += 1;
            s2MixBd = readUByte(data, pos);
            pos += 1;
            s2MixBdAmt = readByte(data, pos);
            pos += 1;
            numberOfWaves = readByte(data, pos);
            pos += 1;
            hardSync = readUByte(data, pos);
            pos += 1;
            bankExp = readByte(data, pos);
            pos += 1;
            dummy141 = readByte(data, pos);
            pos += 1;
                
            for(int i = 0; i < 4; i++)
                pos = waves[i].read(data, pos);
                                
            return pos;
            }


        int write(byte[] data, int pos)
            {
            for(int i = 0; i < 16; i++)
                data[i + pos] = (byte)name[i];
            pos += 16;
            writeUByte(mixRate1, data, pos);
            pos += 1;
            writeUByte(mixRate2, data, pos);
            pos += 1;
            writeUByte(mixRate3, data, pos);
            pos += 1;
            writeUByte(mixRate4, data, pos);
            pos += 1;
            writeUWord(mixCount1, data, pos);
            pos += 2;
            writeUWord(mixCount2, data, pos);
            pos += 2;
            writeUWord(mixCount3, data, pos);
            pos += 2;
            writeUWord(mixCount3B, data, pos);
            pos += 2;
            writeUWord(mixCount2B, data, pos);
            pos += 2;
            writeUWord(mixCount1B, data, pos);
            pos += 2;
            writeUWord(mixCount4, data, pos);
            pos += 2;
            writeULong(mixXSlope1, data, pos);
            pos += 4;
            writeULong(mixXSlope2, data, pos);
            pos += 4;
            writeULong(mixXSlope3, data, pos);
            pos += 4;
            writeULong(mixXSlope4, data, pos);
            pos += 4;
            writeULong(mixYSlope1, data, pos);
            pos += 4;
            writeULong(mixYSlope2, data, pos);
            pos += 4;
            writeULong(mixYSlope3, data, pos);
            pos += 4;
            writeULong(mixYSlope4, data, pos);
            pos += 4;
            writeUByte(mixX0, data, pos);
            pos += 1;
            writeUByte(mixX1, data, pos);
            pos += 1;
            writeUByte(mixX2, data, pos);
            pos += 1;
            writeUByte(mixX3, data, pos);
            pos += 1;
            writeUByte(mixX4, data, pos);
            pos += 1;
            writeUByte(mixY0, data, pos);
            pos += 1;
            writeUByte(mixY1, data, pos);
            pos += 1;
            writeUByte(mixY2, data, pos);
            pos += 1;
            writeUByte(mixY3, data, pos);
            pos += 1;
            writeUByte(mixY4, data, pos);
            pos += 1;
            writeUByte(mixRepeats, data, pos);
            pos += 1;
            writeUByte(mixEnvLoop, data, pos);
            pos += 1;
            writeUByte(s1MixAc, data, pos);
            pos += 1;
            writeByte(s1MixAcAmt, data, pos);
            pos += 1;
            writeUByte(s2MixAc, data, pos);
            pos += 1;
            writeByte(s2MixAcAmt, data, pos);
            pos += 1;
            writeUByte(s1MixBd, data, pos);
            pos += 1;
            writeByte(s1MixBdAmt, data, pos);
            pos += 1;
            writeUByte(s2MixBd, data, pos);
            pos += 1;
            writeByte(s2MixBdAmt, data, pos);
            pos += 1;
            writeByte(numberOfWaves, data, pos);
            pos += 1;
            writeUByte(hardSync, data, pos);
            pos += 1;
            writeByte(bankExp, data, pos);
            pos += 1;
            writeByte(dummy141, data, pos);
            pos += 1;

            for(int i = 0; i < 4; i++)
                pos = waves[i].write(data, pos);
                                
            return pos;
            }


        }
    
  
  
        
    public int parse(byte[] data, boolean fromFile)
        {
        if (data[4] == (byte)0x40)
            {
            model.set("bank", wsToEdisynBank[data[5]]);
            model.set("number", data[6]);        
            return subparse(data, 7);
            }
        else
            {
            // extract names
            String[] n = new String[35];
            for(int i = 0; i < 35; i++)
                {
                // yuck, denybblize and extract the patch just to get the name...
                byte[] d = denybblize(data, i * 852 + 6, 852);
        
                Patch patch = new Patch();
                patch.read(d, 0);
        
                n[i] = new String(patch.name);
                } 
                
            // Now that we have an array of names, one per patch, we present the user with options;
            // 0. Cancel [handled automatically]
            // 1. Save the bank data [handled automatically]
            // 2. Upload the bank data [handled automatically] 
            // 3. Load and edit a certain patch number
            int patchNum = showBankSysexOptions(data, n);
            if (patchNum < 0) return PARSE_CANCELLED;
                
            model.set("bank", wsToEdisynBank[data[5]]);
            model.set("number", patchNum);

            // okay, we're loading and editing patch number patchNum.  Here we go.
            return subparse(data, patchNum * 852 + 6);      
            }
        }
                
                
    public int subparse(byte[] data, int pos)
        {
        data = denybblize(data, pos);
        
        Patch patch = new Patch();
        patch.read(data, 0);
        
        model.set("name", new String(patch.name));
        model.set("mixx0", patch.mixX0);
        model.set("mixx1", patch.mixX1);
        model.set("mixx2", patch.mixX2);
        model.set("mixx3", patch.mixX3);
        model.set("mixx4", patch.mixX4);
        model.set("mixy0", patch.mixY0);
        model.set("mixy1", patch.mixY1);
        model.set("mixy2", patch.mixY2);
        model.set("mixy3", patch.mixY3);
        model.set("mixy4", patch.mixY4);
        model.set("mixtime1", patch.mixRate1);
        model.set("mixtime2", patch.mixRate2);
        model.set("mixtime3", patch.mixRate3);
        model.set("mixtime4", patch.mixRate4);
        model.set("hardsync", patch.hardSync);
        model.set("mixloop", patch.mixEnvLoop);
        model.set("mixlooprepeats", patch.mixRepeats);
        model.set("mixxmod1source", patch.s1MixAc);
        model.set("mixxmod2source", patch.s2MixAc);
        model.set("mixymod1source", patch.s1MixBd);
        model.set("mixymod2source", patch.s2MixBd);
        model.set("mixxmod1amount", patch.s1MixAcAmt);
        model.set("mixxmod2amount", patch.s2MixAcAmt);
        model.set("mixymod1amount", patch.s1MixBdAmt);
        model.set("mixymod2amount", patch.s2MixBdAmt);
        model.set("numoscillators", patch.numberOfWaves);


        for(int i = 0; i < 4; i++)
            {
            String osc = "osc" + (i + 1);
            model.set(osc + "pitchbendmod1source", patch.waves[i].s1Pitch); 
            model.set(osc + "pitchbendmod1amount", patch.waves[i].s1PitchAmt); 
            model.set(osc + "pitchbendmod2source", patch.waves[i].s2Pitch); 
            model.set(osc + "pitchbendmod2amount", patch.waves[i].s2PitchAmt); 
            model.set(osc + "pitchbendrange", patch.waves[i].pwRange); 
            model.set(osc + "pitchrampamount", patch.waves[i].pegAmt); 
            model.set(osc + "pitchramptime", patch.waves[i].pegRate); 
            model.set(osc + "pitchrampvelocitymod", patch.waves[i].velPegA); 

            model.set(osc + "lfo1ratemodsource", patch.waves[i].s1Lfo1R); 
            model.set(osc + "lfo1depthmodsource", patch.waves[i].s1Lfo1A); 
            model.set(osc + "lfo1shape", patch.waves[i].lfo1Shape & 127); 
            model.set(osc + "lfo1sync", (patch.waves[i].lfo1Shape >>> 7) & 1); 
            model.set(osc + "lfo1rate", patch.waves[i].lfo1Rate); 
            model.set(osc + "lfo1amount", patch.waves[i].lfo1Amt); 
            model.set(osc + "lfo1delay", patch.waves[i].lfo1Delay); 
            model.set(osc + "lfo1fadein", patch.waves[i].lfo1Fade); 
            model.set(osc + "lfo1ratemod", patch.waves[i].s1Lfo1RAmt); 
            model.set(osc + "lfo1depthmod", patch.waves[i].s1Lfo1AAmt); 

            model.set(osc + "ampmod1source", patch.waves[i].s1Amp); 
            model.set(osc + "ampmod1amount", patch.waves[i].s1AmpAmt); 
            model.set(osc + "ampmod2source", patch.waves[i].s2Amp); 
            model.set(osc + "ampmod2amount", patch.waves[i].s2AmpAmt); 

            model.set(osc + "lfo2ratemodsource", patch.waves[i].s1Lfo2R); 
            model.set(osc + "lfo2depthmodsource", patch.waves[i].s1Lfo2A); 
            model.set(osc + "lfo2shape", patch.waves[i].lfo2Shape & 127); 
            model.set(osc + "lfo2sync", (patch.waves[i].lfo2Shape >>> 7) & 1); 
            model.set(osc + "lfo2rate", patch.waves[i].lfo2Rate); 
            model.set(osc + "lfo2amount", patch.waves[i].lfo2Amt); 
            model.set(osc + "lfo2delay", patch.waves[i].lfo2Delay); 
            model.set(osc + "lfo2fadein", patch.waves[i].lfo2Fade); 
            model.set(osc + "lfo2ratemod", patch.waves[i].s1Lfo2RAmt); 
            model.set(osc + "lfo2depthmod", patch.waves[i].s1Lfo2AAmt); 

            model.set(osc + "pankeyboardmod", patch.waves[i].keyPanAmt); 
            model.set(osc + "panvelocitymod", patch.waves[i].velPanAmt); 
            model.set(osc + "env1level0a", patch.waves[i].egLevel0); 
            model.set(osc + "env1time1b", patch.waves[i].egRate1); 
            model.set(osc + "env1level1b", patch.waves[i].egLevel1); 
            model.set(osc + "env1time2c", patch.waves[i].egRate2); 
            model.set(osc + "env1level2c", patch.waves[i].egLevel2); 
            model.set(osc + "env1time3d", patch.waves[i].egRate3); 
            model.set(osc + "env1level3d", patch.waves[i].egLevel3); 
            model.set(osc + "env1time4e", patch.waves[i].egRate4); 
            model.set(osc + "env1level4e", patch.waves[i].egLevel4); 
            model.set(osc + "env1amountvelocitymod", patch.waves[i].velEgA); 
            model.set(osc + "env1attackvelocitymod", patch.waves[i].velEgR); 
            model.set(osc + "env1ratekeyboardmod", patch.waves[i].keyEgR); 
            model.set(osc + "env2level0a", patch.waves[i].aegLevel0); 
            model.set(osc + "env2time1b", patch.waves[i].aegRate1); 
            model.set(osc + "env2level1b", patch.waves[i].aegLevel1); 
            model.set(osc + "env2time2c", patch.waves[i].aegRate2); 
            model.set(osc + "env2level2c", patch.waves[i].aegLevel2); 
            model.set(osc + "env2time3d", patch.waves[i].aegRate3); 
            model.set(osc + "env2level3d", patch.waves[i].aegLevel3); 
            model.set(osc + "env2time4e", patch.waves[i].aegRate4); 
            model.set(osc + "env2amountvelocitymod", patch.waves[i].velAegA); 
            model.set(osc + "env2attackvelocitymod", patch.waves[i].velAegR); 
            model.set(osc + "env2ratekeyboardmod", patch.waves[i].keyAegR); 

            model.set(osc + "filtermod1source", patch.waves[i].s1Filter); 
            model.set(osc + "filtermod2source", patch.waves[i].s2Filter); 
            model.set(osc + "filtercutoff", patch.waves[i].cutoff); 
            model.set(osc + "filterexciter", patch.waves[i].filterExciter); 
            model.set(osc + "filterkeyboardtracking", patch.waves[i].keyFilter); 
            model.set(osc + "filtermod1amount", patch.waves[i].s1FilterAmt); 
            model.set(osc + "filtermod2amount", patch.waves[i].s2FilterAmt); 

            //model.set(osc + "pitchbendmacro", patch.waves[i].pitchMac); 
            //model.set(osc + "panmacro", patch.waves[i].panMac); 
            //model.set(osc + "amplitudeenvelopemacro", patch.waves[i].ampMac); 
            //model.set(osc + "envelope1macro", patch.waves[i].envMac); 
            //model.set(osc + "filtermacro", patch.waves[i].filMac);

            model.set(osc + "wave", patch.waves[i].waveNum + patch.waves[i].waveNumExp);
            model.set(osc + "amplevel", patch.waves[i].indivLevel);
            model.set(osc + "tuningcoarse",  patch.waves[i].waveCoarse);
            model.set(osc + "tuningfine",  patch.waves[i].waveFine);
            model.set(osc + "tuningslope",  patch.waves[i].waveScale);

// This is computed according to the Developer FAQ
            model.set(osc + "busa", patch.waves[i].patchOutput & 1);
            model.set(osc + "busb", (patch.waves[i].patchOutput >>> 1) & 1);
            model.set(osc + "busc", (patch.waves[i].patchOutput >>> 2) & 1);
            model.set(osc + "busd", (patch.waves[i].patchOutput >>> 3) & 1);

/// These are computed as determined in the Developer FAQ.  We don't need them in parsing
//              long    Lfo1_Inc;               /* Lfo fade in amount increment*/
//              long    Lfo2_Inc;               /* Lfo fade in amount increment*/


// This one is complex.  There are twelve banks, in three groups of four.
// Bank_Exp specifies which group it is for each of the waves.  I believe
// it works like this.  For each Wave there are two bits.  If both bits are
// unset, it's group 1 (0, 1, 2, 3).  If the first bit is set, it's group 
// 2 (4, 5, 6, 7).  If the second bit is set, it's group 3 (8, 9, 10, 10).
// It's not clear what happens if both bits are set.

            int bita = ((patch.bankExp >>> i) & 1);   // we assume Wave A = 0, Wave B = 1, etc.
            int bitb = ((patch.bankExp >>> (i + 4)) & 1);

            if (bitb == 1)
                {
                model.set(osc + "wavebank", wsBankExpToEdisynBank[patch.waves[i].waveBank + 8]);
                }
            else if (bita == 1)
                {
                model.set(osc + "wavebank", wsBankExpToEdisynBank[patch.waves[i].waveBank + 4]);
                }
            else
                {
                model.set(osc + "wavebank", wsBankExpToEdisynBank[patch.waves[i].waveBank]);
                }

            }

        revise();
                
        return PARSE_SUCCEEDED;     
        }
    
    public static final int NYBBLIZED_LENGTH = 426;

    public static final int[] RATE_TAB = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 83, 85, 90, 95, 100, 110, 120, 130, 140, 150, 180, 210, 240, 270, 300, 400, 500, 600, 700 };

    public Object[] emitAll(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        if (tempModel == null)
            tempModel = getModel();
            
        byte[] d = new byte[EXPECTED_SYSEX_LENGTH];
        d[0] = (byte)0xF0;
        d[1] = (byte)0x42;
        d[2] = (byte)(48 + getChannelOut());
        d[3] = (byte)0x28;
        d[4] = (byte)0x40;
        d[5] = (byte)edisynToWSBank[model.get("bank")];
        d[6] = (byte)model.get("number");
       
        Patch patch = new Patch();

        patch.setName(model.get("name", "Untitled"));  
                     
        patch.mixX0 = model.get("mixx0");
        patch.mixX0 = model.get("mixx0");
        patch.mixX1 = model.get("mixx1");
        patch.mixX2 = model.get("mixx2");
        patch.mixX3 = model.get("mixx3");
        patch.mixX4 = model.get("mixx4");
        patch.mixY0 = model.get("mixy0");
        patch.mixY1 = model.get("mixy1");
        patch.mixY2 = model.get("mixy2");
        patch.mixY3 = model.get("mixy3");
        patch.mixY4 = model.get("mixy4");
        patch.mixRate1 = model.get("mixtime1");
        patch.mixRate2 = model.get("mixtime2");
        patch.mixRate3 = model.get("mixtime3");
        patch.mixRate4 = model.get("mixtime4");
        patch.hardSync = model.get("hardsync");
        patch.mixEnvLoop = model.get("mixloop");
        patch.mixRepeats = model.get("mixlooprepeats");
        patch.s1MixAc = model.get("mixxmod1source");
        patch.s2MixAc = model.get("mixxmod2source");
        patch.s1MixBd = model.get("mixymod1source");
        patch.s2MixBd = model.get("mixymod2source");
        patch.s1MixAcAmt = model.get("mixxmod1amount");
        patch.s2MixAcAmt = model.get("mixxmod2amount");
        patch.s1MixBdAmt = model.get("mixymod1amount");
        patch.s2MixBdAmt = model.get("mixymod2amount");
        patch.numberOfWaves = model.get("numoscillators");

//// Need to compute these.  The following code is taken from the Developer FAQ.
//// The Developer FAQ code has a weird error in it, which I believe I have corrected.
//// Also it looks like the if/else statements compute the same thing but I believe this
//// is not correct, and is an arrangement to permit unsigned ints to use their full space.
//// We're using long to store the unsigned ints, so we could probably have done this more simply.

//// The code uses ulong.   But the results are stored in signed longs in the structs.  Error in the sysex document?
//// UPDATE: Yes, error. by "long" they mean "unsigned (32-bit) long".  I'll have to use longs to store them.

        patch.mixCount1 = RATE_TAB[patch.mixRate1];
        patch.mixCount1B = RATE_TAB[patch.mixRate1];
        if (patch.mixX1 >= patch.mixX0)
            patch.mixXSlope1 = 0x1000000L * ((long)patch.mixX1-(long)patch.mixX0)/RATE_TAB[patch.mixRate1];
        else
            patch.mixXSlope1 = -(0x1000000L * ((long)patch.mixX0-(long)patch.mixX1)/RATE_TAB[patch.mixRate1]);
        if (patch.mixY1 >= patch.mixY0)
            patch.mixYSlope1 = 0x1000000L * ((long)patch.mixY1-(long)patch.mixY0)/RATE_TAB[patch.mixRate1];
        else
            patch.mixYSlope1 = -(0x1000000L * ((long)patch.mixY0-(long)patch.mixY1)/RATE_TAB[patch.mixRate1]);

        patch.mixCount2 = RATE_TAB[patch.mixRate2];
        patch.mixCount2B = RATE_TAB[patch.mixRate2];
        if (patch.mixX2 >= patch.mixX1)
            patch.mixXSlope2 = 0x1000000L  * ((long)patch.mixX2-(long)patch.mixX1)/RATE_TAB[patch.mixRate2];
        else
            patch.mixXSlope2 = -(0x1000000L * ((long)patch.mixX1-(long)patch.mixX2)/RATE_TAB[patch.mixRate2]);
        if (patch.mixY2 >= patch.mixY1)
            patch.mixYSlope2 = 0x1000000L * ((long)patch.mixY2-(long)patch.mixY1)/RATE_TAB[patch.mixRate2];
        else
            patch.mixYSlope2 = -(0x1000000L * ((long)patch.mixY1-(long)patch.mixY2)/RATE_TAB[patch.mixRate2]);

        patch.mixCount3 = RATE_TAB[patch.mixRate3];
        patch.mixCount3B = RATE_TAB[patch.mixRate3];
        if (patch.mixX3 >= patch.mixX2)
            patch.mixXSlope3 = 0x1000000L * ((long)patch.mixX3-(long)patch.mixX2)/RATE_TAB[patch.mixRate3];
        else
            patch.mixXSlope3 = -(0x1000000L * ((long)patch.mixX2-(long)patch.mixX3)/RATE_TAB[patch.mixRate3]);
        if (patch.mixY3 >= patch.mixY2)
            patch.mixYSlope3 = 0x1000000L * ((long)patch.mixY3-(long)patch.mixY2)/RATE_TAB[patch.mixRate3];
        else
            patch.mixYSlope3 = -(0x1000000L * ((long)patch.mixY2-(long)patch.mixY3)/RATE_TAB[patch.mixRate3]);

        patch.mixCount4 = RATE_TAB[patch.mixRate4];
        //patch.mixCount4B = RATE_TAB[patch.mixRate4];               // This appears to be an error in the developer FAQ.  There is no such variable.
        if (patch.mixX4 >= patch.mixX3)
            patch.mixXSlope4 = 0x1000000L * ((long)patch.mixX4-(long)patch.mixX3)/RATE_TAB[patch.mixRate4];
        else
            patch.mixXSlope4 = -(0x1000000L * ((long)patch.mixX3-(long)patch.mixX4)/RATE_TAB[patch.mixRate4]);
        if (patch.mixY4 >= patch.mixY3)
            patch.mixYSlope4 = 0x1000000L * ((long)patch.mixY4-(long)patch.mixY3)/RATE_TAB[patch.mixRate4];
        else
            patch.mixYSlope4 = -(0x1000000L * ((long)patch.mixY3-(long)patch.mixY4)/RATE_TAB[patch.mixRate4]);



        for(int i = 0; i < 4; i++)
            {
            String osc = "osc" + (i + 1);
            patch.waves[i].s1Pitch = model.get(osc + "pitchbendmod1source"); 
            patch.waves[i].s1PitchAmt = model.get(osc + "pitchbendmod1amount"); 
            patch.waves[i].s2Pitch = model.get(osc + "pitchbendmod2source"); 
            patch.waves[i].s2PitchAmt = model.get(osc + "pitchbendmod2amount"); 
            patch.waves[i].pwRange = model.get(osc + "pitchbendrange"); 
            patch.waves[i].pegAmt = model.get(osc + "pitchrampamount"); 
            patch.waves[i].pegRate = model.get(osc + "pitchramptime"); 
            patch.waves[i].velPegA = model.get(osc + "pitchrampvelocitymod"); 

            patch.waves[i].s1Lfo1R = model.get(osc + "lfo1ratemodsource"); 
            patch.waves[i].s1Lfo1A = model.get(osc + "lfo1depthmodsource"); 
            patch.waves[i].lfo1Shape = (model.get(osc + "lfo1sync") << 7) |  model.get(osc + "lfo1shape");
            patch.waves[i].lfo1Rate = model.get(osc + "lfo1rate"); 
            patch.waves[i].lfo1Amt = model.get(osc + "lfo1amount"); 
            patch.waves[i].lfo1Delay = model.get(osc + "lfo1delay"); 
            patch.waves[i].lfo1Fade = model.get(osc + "lfo1fadein"); 
            patch.waves[i].s1Lfo1RAmt = model.get(osc + "lfo1ratemod"); 
            patch.waves[i].s1Lfo1AAmt = model.get(osc + "lfo1depthmod"); 

            patch.waves[i].s1Amp = model.get(osc + "ampmod1source"); 
            patch.waves[i].s1AmpAmt = model.get(osc + "ampmod1amount"); 
            patch.waves[i].s2Amp = model.get(osc + "ampmod2source"); 
            patch.waves[i].s2AmpAmt = model.get(osc + "ampmod2amount"); 

            patch.waves[i].s1Lfo2R = model.get(osc + "lfo2ratemodsource"); 
            patch.waves[i].s1Lfo2A = model.get(osc + "lfo2depthmodsource"); 
            patch.waves[i].lfo1Shape = (model.get(osc + "lfo2sync") << 7) |  model.get(osc + "lfo2shape");
            patch.waves[i].lfo2Rate = model.get(osc + "lfo2rate"); 
            patch.waves[i].lfo2Amt = model.get(osc + "lfo2amount"); 
            patch.waves[i].lfo2Delay = model.get(osc + "lfo2delay"); 
            patch.waves[i].lfo2Fade = model.get(osc + "lfo2fadein"); 
            patch.waves[i].s1Lfo2RAmt = model.get(osc + "lfo2ratemod"); 
            patch.waves[i].s1Lfo2AAmt = model.get(osc + "lfo2depthmod"); 

            patch.waves[i].keyPanAmt = model.get(osc + "pankeyboardmod"); 
            patch.waves[i].velPanAmt = model.get(osc + "panvelocitymod"); 
            patch.waves[i].egLevel0 = model.get(osc + "env1level0a"); 
            patch.waves[i].egRate1 = model.get(osc + "env1time1b"); 
            patch.waves[i].egLevel1 = model.get(osc + "env1level1b"); 
            patch.waves[i].egRate2 = model.get(osc + "env1time2c"); 
            patch.waves[i].egLevel2 = model.get(osc + "env1level2c"); 
            patch.waves[i].egRate3 = model.get(osc + "env1time3d"); 
            patch.waves[i].egLevel3 = model.get(osc + "env1level3d"); 
            patch.waves[i].egRate4 = model.get(osc + "env1time4e"); 
            patch.waves[i].egLevel4 = model.get(osc + "env1level4e"); 
            patch.waves[i].velEgA = model.get(osc + "env1amountvelocitymod"); 
            patch.waves[i].velEgR = model.get(osc + "env1attackvelocitymod"); 
            patch.waves[i].keyEgR = model.get(osc + "env1ratekeyboardmod"); 
            patch.waves[i].aegLevel0 = model.get(osc + "env2level0a"); 
            patch.waves[i].aegRate1 = model.get(osc + "env2time1b"); 
            patch.waves[i].aegLevel1 = model.get(osc + "env2level1b"); 
            patch.waves[i].aegRate2 = model.get(osc + "env2time2c"); 
            patch.waves[i].aegLevel2 = model.get(osc + "env2level2c"); 
            patch.waves[i].aegRate3 = model.get(osc + "env2time3d"); 
            patch.waves[i].aegLevel3 = model.get(osc + "env2level3d"); 
            patch.waves[i].aegRate4 = model.get(osc + "env2time4e"); 
            patch.waves[i].velAegA = model.get(osc + "env2amountvelocitymod"); 
            patch.waves[i].velAegR = model.get(osc + "env2attackvelocitymod"); 
            patch.waves[i].keyAegR = model.get(osc + "env2ratekeyboardmod"); 

            patch.waves[i].s1Filter = model.get(osc + "filtermod1source"); 
            patch.waves[i].s2Filter = model.get(osc + "filtermod2source"); 
            patch.waves[i].cutoff = model.get(osc + "filtercutoff"); 
            patch.waves[i].filterExciter = model.get(osc + "filterexciter"); 
            patch.waves[i].keyFilter = model.get(osc + "filterkeyboardtracking"); 
            patch.waves[i].s1FilterAmt = model.get(osc + "filtermod1amount"); 
            patch.waves[i].s2FilterAmt = model.get(osc + "filtermod2amount"); 

            // hard-set the macros -- we assume that all the data in the editor is correct
                    
            final int MACRO_USER = -1;
            patch.waves[i].pitchMac = MACRO_USER;  // model.get(osc + "pitchbendmacro"); 
            patch.waves[i].panMac = MACRO_USER; // model.get(osc + "panmacro"); 
            patch.waves[i].ampMac = MACRO_USER; // model.get(osc + "amplitudeenvelopemacro"); 
            patch.waves[i].envMac = MACRO_USER; // model.get(osc + "envelope1macro"); 
            patch.waves[i].filMac = MACRO_USER; // model.get(osc + "filtermacro");

//// Not sure how to split up the wave number.  This is my guess.
            if (model.get(osc + "wave") < 397)
                {
                patch.waves[i].waveNum = model.get(osc + "wave");
                patch.waves[i].waveNumExp = 0;
                }
            else
                {
                patch.waves[i].waveNum = 396;
                patch.waves[i].waveNumExp = model.get(osc + "wave") - patch.waves[i].waveNum;
                }

            patch.waves[i].indivLevel = model.get(osc + "amplevel");
            patch.waves[i].waveCoarse = model.get(osc + "tuningcoarse");
            patch.waves[i].waveFine = model.get(osc + "tuningfine");
            patch.waves[i].waveScale = model.get(osc + "tuningslope");

// This is computed according to the Developer FAQ
            patch.waves[i].patchOutput = ((model.get(osc + "busd") << 3) |
                (model.get(osc + "busc") << 2) |
                (model.get(osc + "busb") << 2) |
                (model.get(osc + "busa")));
                                                        
// I need to compute these I believe.  The following code is from the Developer FAQ.

            if (patch.waves[i].lfo1Fade == 0)
                patch.waves[i].lfo1Inc = (0x7FFFFFL * (long)patch.waves[i].lfo1Amt)/127;
            else
                patch.waves[i].lfo1Inc = (0x7FFFFFL * (long)patch.waves[i].lfo1Amt) / (RATE_TAB[patch.waves[i].lfo1Fade] * 127);

            if (patch.waves[i].lfo2Fade == 0)
                patch.waves[i].lfo2Inc = (0x7FFFFFL * (long)patch.waves[i].lfo2Amt)/127;
            else
                patch.waves[i].lfo2Inc = (0x7FFFFFL * (long)patch.waves[i].lfo2Amt) / (RATE_TAB[patch.waves[i].lfo2Fade] * 127);

// Now I need to compute patch.bankExp and patch.waves[i].waveBank
            patch.waves[i].waveBank = edisynToWSBank[model.get(osc + "wavebank")] % 4;
            }

// Now compute patch.bankExp
// Not sure if I need to set BOTH bits.  Doesn't appear so.
        int b1 = edisynToWSBank[model.get("osc1wavebank")];
        int b2 = edisynToWSBank[model.get("osc2wavebank")];
        int b3 = edisynToWSBank[model.get("osc3wavebank")];
        int b4 = edisynToWSBank[model.get("osc4wavebank")];
        patch.bankExp =
            (b1 >= 8 ? 16 : b1 >= 4 ? 1 : 0) + 
            (b2 >= 8 ? 32 : b2 >= 4 ? 2 : 0) + 
            (b3 >= 8 ? 64 : b3 >= 4 ? 4 : 0) + 
            (b4 >= 8 ? 128 : b4 >= 4 ? 8 : 0); 


        byte[] data = new byte[NYBBLIZED_LENGTH];
        patch.write(data, 0);
        data = nybblize(data);
        System.arraycopy(data, 0, d, 7, data.length);
        int checksum = 0;
        for(int i = 0; i < data.length; i++)
            checksum += data[i];
        checksum = (checksum & 127);
        d[d.length - 2] = (byte)checksum;
        d[d.length - 1] = (byte)0xF7;
           
        if (toFile || toWorkingMemory)
            return new Object[] { d };
        else
            {
            // we'll attempt a write but fail  -- this doesn't work
            return new Object[] { d, new byte[] { (byte)0xF0, (byte)0x42, (byte)(48 + getChannelOut()), (byte)0x28, (byte)0x11, d[4], d[5], (byte)0xF7 }};
            }
        }

    public int getPauseAfterChangePatch() { return 300; }  // looks like 300 is about the minimum for a standard PC (see Performance.java); may be too much here.

    /// WAVE_MUTE seems to be based on bits:
    /// Bits DCBA (that is, values 0...15)
    /// ... are set to 1 to represent which waves are muted

    public static final int[] MUTES = new int[]{ 2 + 4 + 8, 1 + 4 + 8, 1 + 2 + 8, 1 + 2 + 4 };
    public void setSolo()
        {
        byte[] midi_mesg = paramBytes(WAVE_MUTE, MUTES[getSelectedTabIndex() - 1]);
        tryToSendSysex(midi_mesg);
        }
                
    public void resetSolo()
        {
        byte[] midi_mesg = paramBytes(WAVE_MUTE, 0);
        tryToSendSysex(midi_mesg);
        }
        
    public void changePatch(Model tempModel)
        {
        byte[] midi_mesg = paramBytes(MIDI_MODE, MULTISET_MIDI_MODE);
        tryToSendSysex(midi_mesg);
        
        byte[] midi_mesg_2 = paramBytes(MIDI_MODE, PERFORMANCE_MIDI_MODE);
        tryToSendSysex(midi_mesg_2);
        
        
        //// FIXME -- I modified this, is it still okay?
        
/*
  byte[] part_mesg = paramBytes(CURRENT_PART, DEFAULT_PART);
  tryToSendSysex(part_mesg);
*/        
        byte[] patch_bank_mesg = paramBytes(PART_PATCH_BANK, edisynToWSBank[tempModel.get("bank", 0)]);
        tryToSendSysex(patch_bank_mesg);

        byte[] patch_num_mesg = paramBytes(PART_PATCH_NUM, tempModel.get("number", 0));
        tryToSendSysex(patch_num_mesg);

/*
  byte[] solo_mesg = paramBytes(EXECUTE_SOLO_PART, 1);
  tryToSendSysex(solo_mesg);
*/

        // for some reason changing the patch changes the mode to OMNI.  We need to turn that off.
        byte[] patch_poly_mesg = paramBytes(MIDI_MODE, 1);
        tryToSendSysex(patch_poly_mesg);
        patch_poly_mesg = paramBytes(PERF_MIDI_MODE, 1);
        tryToSendSysex(patch_poly_mesg);
        }

    public byte[] requestDump(Model tempModel)
        {
        byte BB = (byte)edisynToWSBank[tempModel.get("bank")];
        byte NN = (byte)tempModel.get("number");
        return new byte[] { (byte)0xF0, (byte)0x42, (byte)(48 + getChannelOut()), 0x28, 0x10, BB, NN, (byte)0xF7 };
        }
                                
    
    public static final int EXPECTED_SYSEX_LENGTH = 861;
    public static boolean recognize(byte[] data)
        {
        return ((data.length == EXPECTED_SYSEX_LENGTH &&
                data[0] == (byte)0xF0 &&
                data[1] == (byte)0x42 &&
                data[3] == (byte)0x28 &&
                data[4] == (byte)0x40)
            
            || recognizeBulk(data));                 
        }

    public static boolean recognizeBulk(byte[] data)
        {
        return (data.length == 29828 && // I think it's 29828?  A patch is 852...?
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x42 &&
            data[3] == (byte)0x28 &&
            data[4] == (byte)0x4C);                 
        }
    
    
    
    
    /////// OTHER ABSTRACT METHODS
    
    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        JComboBox bank = new JComboBox(BANKS);
        bank.setEditable(false);
        bank.setMaximumRowCount(32);
        bank.setSelectedIndex(model.get("bank"));
                
        JTextField number = new JTextField("" + model.get("number"), 3);
                
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
                showSimpleError(title, "The Patch Number must be an integer 0 ... 34");
                continue;
                }
            if (n < 0 || n > 34)
                {
                showSimpleError(title, "The Patch Number must be an integer 0... 34");
                continue;
                }
                                
            change.set("bank", bank.getSelectedIndex());
            change.set("number", n);
                        
            return true;
            }
        }
        

    public void revise()
        {
        // check the easy stuff -- out of range parameters
        super.revise();

        String nm = model.get("name", "Init");
        String newnm = revisePatchName(nm);
        if (!nm.equals(newnm))
            model.set("name", newnm);
        }
        
    public static String getSynthName() { return "Korg Wavestation SR [Patch]"; }
    
    public String getPatchName(Model model) { return model.get("name", "Init"); }
    

    public Model getNextPatchLocation(Model model)
        {
        int bank = model.get("bank");
        int number = model.get("number");
        
        number++;
        if (number >= 34)
            {
            bank++;
            number = 0;
            if (bank >= 4)
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
        
        int number = model.get("number");
        return BANKS[model.get("bank")] + " " + (number > 9 ? "" : "0") + number;
        }

    public void sendTestPerformance()
        {
        if (tuple == null)
            if (!setupMIDI(tuple))
                return;

        if (tuple != null)
            {
            final KorgWavestationPerformance synth = new KorgWavestationPerformance();
            synth.tuple = tuple.copy(synth.buildInReceiver(), synth.buildKeyReceiver());
            if (synth.tuple != null)
                {
                synth.loadDefaults();
                synth.getModel().set("part1bank", model.get("bank"));
                synth.getModel().set("part1number", model.get("number"));
                synth.performChangePatch(synth.getModel());
                synth.tryToSendMIDI(synth.emitAll(synth.getModel(), true, false));

                // load me into the patch location
                performChangePatch(getModel());
                tryToSendMIDI(emitAll(getModel(), true, false));       
                }
            }
        }

    public int getPauseBetweenHillClimbPlays()
        {
        return 1100;
        }

    }
    
    
/**
   From
   https://web.archive.org/web/20121116105637/http://www.ex5tech.com/ex5ubb_cgi/ultimatebb.cgi?ubb=get_topic&f=23&t=000004
        
   These are the PUBLISHED VS waveform names

   01 Sine
   02 Sawtooth
   03 Square
   14 Pulse var1
   15 Pulse var2
   25 3rd and 5th harmonics *fundamental absent
   27 Heavy 7th harmonics
   29 BASSBELL 14th and 28th harmonics
   44 VOCAL1 *Detune oscillators using either waveform to bring out the vocal quality
   45 VOCAL2 *Detune oscillators using either waveform to bring out the vocal quality
   82 Bell Partials1
   84 Sawtooth 3rd and 5th
   85 Sine 5ths *2 sines an octave and a 5th apart
   86 Sine 2 Octave *2 sines, 2 octaves apart
   87 Sine 4 Octave *2 sines, 4 Ocataves apart
   88 Sawtooth 5ths *2 saws, an octave and a 5th apart
   89 Sawtooth 2-Octaves *2 saws, 2 octaves apart
   90 Square 5ths *2 squares, a 5th apart
   91 Square Octave+5th *2 squares, an octave and a 5th apart
   92 Square 2-Octaves *2 squares, 2 octaves apart
   94 Bell Partials2
   95 Blank Wave


   These are the *supposed* unpublished VS waveform names, though the original designer says he really didn't have names for any of them

   1 Sine
   2 Saw
   3 Sqr
   4 WmBell
   5 RdBell
   6 R2Bell
   7 W2Bell
   8 FmtBell
   9 FzReed
   10 FmtAOh
   11 FmtAhh
   12 TriPlus
   13 DisBel
   14 Pulse1
   15 Pulse2
   16 SqrReed
   17 Oohh
   18 49. Eehh
   19 FeedBack
   20 Piano1
   21 E.Pno
   22 M.Harm
   23 HiTop
   24 WmReed
   25 3rd and 5th harmonics *fundamental absent
   26 Hollow
   27 Hvy7
   28 BelOrg
   29 BASSBELL *14th and 28th harmonics
   30 Tine1
   31 PhSQR
   32 Orient
   33 HiPipe
   34 Mass
   35 ReedOrg
   36 OrgAhh
   37 MelOrg
   38 FmtOrg
   39 Clar
   40 AhhFem
   41 AhhHom
   42 AhhBass
   43 RegVox
   44 VOCAL1 *Detune oscs using either wave to bring out the vocal
   45 VOCAL2 *Detune oscs using either wave to bring out the vocal 
   46 HiAhh
   47 Bass
   48 Guitar
   49 Nice
   50 WWind
   51 Oboe
   52 Harp
   53 Pipe
   54 Hack1
   55 Hack2
   56 Hack3
   57 Pinch
   58 BellHrm
   59 BellVox
   60 Hi Harm
   61 Hi Reed
   62 BellReed
   63 WmWhstl
   64 Wood
   65 Pure
   66 Med Pure
   67 HiHarm
   68 FullBell
   69 Bell
   70 Pinch
   71 Clustr
   72 M.Pinch
   73 VoxPnch
   74 OrgPnch
   75 AhhPnch
   76 PnoOrg
   77 BrReed
   78 NoFund
   79 ReedHrm
   80 LiteFund
   81 MelOrg
   82 Bell
   83 Bell
   84 Sawtooth 3rd and 5th
   85 Sine 5ths *2 sines an octave and a 5th apart
   86 Sine 2 Octave *2 sines, 2 octaves apart
   87 Sine 4 Octave *2 sines, 4 Ocataves apart
   88 Sawtooth 5ths *2 saws, an octave and a 5th apart
   89 Sawtooth 2-Octaves *2 saws, 2 octaves apart
   90 Square 5ths *2 squares, a 5th apart
   91 Square Octave+5th *2 squares, an octave and a 5th apart
   92 Square 2-Octaves *2 squares, 2 octaves apart
   94 Bell Partials2
   95 Null Wave
   96+ User Waves (Editor someone?!)
**/
