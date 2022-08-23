/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.yamaha4op;

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
   A patch editor for the Yamaha TX81Z and DX11 (Multi Mode).
        
   @author Sean Luke
*/

public class Yamaha4OpMulti extends Synth
    {
    /// Various collections of parameter names for pop-up menus
        
    public static final int TYPE_TX81Z = 0;
    public static final int TYPE_DX11 = 1;
    public static final String[] TYPES = { "TX81Z", "DX11" };
    public static final String[] KEYS = new String[] { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
    public static final String[] CHANNELS = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16" };
    public static final String[] BANKS = { "I", "A", "B", "C", "D" };
    public static final String[] EFFECTS = { "Off", "Delay", "Pan", "Chord Memory", "Delay 2 [DX11]", "Pan 2 [DX11]", "Chord Memory 2 [DX11]", "Delay 3 [DX11]", "Pan 3 [DX11]", "Chord Memory 3 [DX11]", "Delay 4 [DX11]", "Pan 4 [DX11]", "Chord Memory 4 [DX11]" };
    public static final String[] MICROTUNE_TABLES = { "Equal", "Pure (Major)", "Pure (Minor)", "Mean Tone", "Pythagorean", "Werckmeister", "Kirnberger", "Vallotti & Young", "1/4 Shift Equal", "1/4 Tone", "1/8 Tone", "User (Octave)", "User (Full)" };
    public static final String[] MICROTUNE_KEYS = { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
    public static final String[] LFO_SELECT = { "Off", "Instrument 1", "Instrument 2", "Vibrato" };
    public static final String[] OUT_ASSIGN = { "Off", "I", "II", "I and II" };

    public static final String TYPE_KEY = "type";
    int synthType = TYPE_TX81Z;
    JComboBox synthTypeCombo;
        
    public int getSynthType() { return synthType; }
    public void setSynthType(int val, boolean save)
        {
        if (save)
            {
            setLastX("" + val, TYPE_KEY, getSynthClassName(), true);
            }
        synthType = val;
        synthTypeCombo.setSelectedIndex(val);  // hopefully this isn't recursive
        updateTitle();
        }

    public Yamaha4OpMulti()
        {
        model.set("number", 0);

        for(int i = 0; i < allParameters.length; i++)
            {
            allParametersToIndex.put(allParameters[i], Integer.valueOf(i));
            }
                        
        /// SOUND PANEL
                
        JComponent soundPanel = new SynthPanel(this);
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        hbox.addLast(addGlobal(Style.COLOR_B()));
        vbox.add(hbox);
        
        vbox.add(addInstrument(1, Style.COLOR_A()));
        vbox.add(addInstrument(2, Style.COLOR_A()));

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Global, Instruments 1-2", soundPanel);
                

        SynthPanel sourcePanel = new SynthPanel(this);
        vbox = new VBox();
        
        vbox.add(addInstrument(3, Style.COLOR_A()));
        vbox.add(addInstrument(4, Style.COLOR_A()));
        vbox.add(addInstrument(5, Style.COLOR_A()));
            
        sourcePanel.add(vbox, BorderLayout.CENTER);
        addTab("Instruments 3-5", sourcePanel);

        sourcePanel = new SynthPanel(this);
        vbox = new VBox();
        
        vbox.add(addInstrument(6, Style.COLOR_A()));
        vbox.add(addInstrument(7, Style.COLOR_A()));
        vbox.add(addInstrument(8, Style.COLOR_A()));

        sourcePanel.add(vbox, BorderLayout.CENTER);
        addTab("Instruments 6-8", sourcePanel);
        
        model.set("name", "INIT SOUND");
        
        loadDefaults();        
        }
                
    public JFrame sprout()
        {
        JFrame frame = super.sprout();
        writeTo.setEnabled(false);
        return frame;
        }         


    public String getDefaultResourceFileName() { return "Yamaha4OpMulti.init"; }
    public String getHTMLResourceFileName() { return "Yamaha4OpMulti.html"; }

    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        JTextField number = new SelectedTextField("" + (model.get("number") + 1), 3);

        while(true)
            {
            boolean result = showMultiOption(this, new String[] { "Patch Number"}, 
                new JComponent[] { number }, title, "Enter the Patch number");
                
            if (result == false) 
                return false;
                                
            int n;
            try { n = Integer.parseInt(number.getText()); }
            catch (NumberFormatException e)
                {
                showSimpleError(title, "The Patch Number must be an integer 1...24");
                continue;
                }
            if (n < 1 || n > 24)
                {
                showSimpleError(title, "The Patch Number must be an integer 1...24");
                continue;
                }
                
            n--;
                                
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
        
        hbox2 = new HBox();
        
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
        hbox2.add(comp);

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
        hbox2.add(st);
        
        vbox.add(hbox2);
        hbox.add(vbox);

        // Not enough space to show the title
        hbox.addLast(Strut.makeHorizontalStrut(20));

        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }


    public JComponent addGlobal( Color color)
        {
        Category category = new Category(this, "Global", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = EFFECTS;
        comp = new Chooser("Effect", this, "effect", params);
        vbox.add(comp);
        
        params = MICROTUNE_TABLES;
        comp = new Chooser("Microtune Table", this, "microtunetable", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Microtune", this, "microtunekey", color, 0, 11)
            {
            public String map(int val)
                {
                return MICROTUNE_KEYS[val];
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Key");
        hbox.add(comp);

        vbox = new VBox();
        comp = new CheckBox("Alternate Assign Mode", this, "assignmode");
        vbox.add(comp);
        hbox.add(vbox);
        
        

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }



    public JComponent addInstrument(final int src, Color color)
        {
        final Category category = new Category(this, "Instrument " + src, color);
        //        category.makePasteable("instrument" + src);
        category.makePasteable("instrument");

        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = OUT_ASSIGN;
        comp = new Chooser("Output", this, "instrument" + src + "outassign", params);
        vbox.add(comp);

        params = LFO_SELECT;
        comp = new Chooser("LFO", this, "instrument" + src + "lfoselect", params);
        vbox.add(comp);
        hbox.add(vbox);

        vbox = new VBox();
        comp = new CheckBox("Microtune On", this, "instrument" + src + "microtuneanbled");
        vbox.add(comp);

        HBox hbox2 = new HBox();
        comp = new PushButton("Show")
            {
            public void perform()
                {
                final Yamaha4Op synth = new Yamaha4Op();
                if (tuple != null)
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
                                                
                    synth.setTitleBarAux("[Inst " + src + " of " + Yamaha4OpMulti.this.model.get("name", "") + "]");
                    synth.sprout();
                    JFrame frame = ((JFrame)(SwingUtilities.getRoot(synth)));
                    frame.setVisible(true);                                 

                    SwingUtilities.invokeLater(
                        new Runnable()
                            {
                            public void run() 
                                { 
                                int bank = Yamaha4OpMulti.this.model.get("instrument" + src + "voicebank");
                                int number = Yamaha4OpMulti.this.model.get("instrument" + src + "voicenumber");
                                                                
                                Model tempModel = buildModel();
                                tempModel.set("bank", bank);
                                tempModel.set("number", number);
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
        hbox2.addLast(comp);
        vbox.addBottom(hbox2);
        hbox.add(vbox);


        comp = new LabelledDial("Voice", this, "instrument" + src + "voicebank", color, 0, 4)
            {
            public String map(int val)
                {
                return BANKS[val % 4]; // we do this because we often get bad data here
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Bank");
        model.removeMetricMinMax("instrument" + src + "voicebank");
        hbox.add(comp);
        
        comp = new LabelledDial("Voice", this, "instrument" + src + "voicenumber", color, 0, 31, -1);
        ((LabelledDial)comp).addAdditionalLabel("Number");
        model.removeMetricMinMax("instrument" + src + "voicenumber");
        hbox.add(comp);
        
        comp = new LabelledDial("Receive", this, "instrument" + src + "channel", color, 0, 16)
            {
            public String map(int val)
                {
                if (val == 16) return "Omni";
                else return "" + (val + 1);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Channel");
        model.removeMetricMinMax("instrument" + src + "channel");
        hbox.add(comp);
        
        comp = new LabelledDial("Max Notes", this, "instrument" + src + "maxnotes", color, 0, 8);
        hbox.add(comp);
        
        comp = new LabelledDial("Lowest", this, "instrument" + src + "lowkey", color, 0, 127)
            {
            public String map(int val)
                {
                return KEYS[val % 12] + (val / 12 - 2);  // note integer division
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Key");
        hbox.add(comp);

        comp = new LabelledDial("Highest", this, "instrument" + src + "highkey", color, 0, 127)
            {
            public String map(int val)
                {
                return KEYS[val % 12] + (val / 12 - 2);  // note integer division
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Key");
        hbox.add(comp);
        

        comp = new LabelledDial("Detune", this, "instrument" + src + "detune", color, 0, 14, 7)
            {
            public boolean isSymmetric() { return true; }
            };
        hbox.add(comp);

        comp = new LabelledDial("Note Shift", this, "instrument" + src + "noteshift", color, 0, 48, 24)
            {
            public boolean isSymmetric() { return true; }
            };
        hbox.add(comp);
        
        comp = new LabelledDial("Volume", this, "instrument" + src + "volume", color, 0, 99);
        hbox.add(comp);
    
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
                



    /** Map of parameter -> index in the allParameters array. */
    HashMap allParametersToIndex = new HashMap();


    /** List of all Sysex parameters in order.  "-" is a reserved (unused and thus unnamed) parameter. */

    final static String[] allParameters = new String[] 
    {
    "instrument1maxnotes",
    "instrument1voicenumbermsb",
    "instrument1voicenumberlsb",
    "instrument1channel",
    "instrument1lowkey",
    "instrument1highkey",
    "instrument1detune",
    "instrument1noteshift",
    "instrument1volume",
    "instrument1outassign",
    "instrument1lfoselect",
    "instrument1microtuneanbled",

    "instrument2maxnotes",
    "instrument2voicenumbermsb",
    "instrument2voicenumberlsb",
    "instrument2channel",
    "instrument2lowkey",
    "instrument2highkey",
    "instrument2detune",
    "instrument2noteshift",
    "instrument2volume",
    "instrument2outassign",
    "instrument2lfoselect",
    "instrument2microtuneanbled",

    "instrument3maxnotes",
    "instrument3voicenumbermsb",
    "instrument3voicenumberlsb",
    "instrument3channel",
    "instrument3lowkey",
    "instrument3highkey",
    "instrument3detune",
    "instrument3noteshift",
    "instrument3volume",
    "instrument3outassign",
    "instrument3lfoselect",
    "instrument3microtuneanbled",

    "instrument4maxnotes",
    "instrument4voicenumbermsb",
    "instrument4voicenumberlsb",
    "instrument4channel",
    "instrument4lowkey",
    "instrument4highkey",
    "instrument4detune",
    "instrument4noteshift",
    "instrument4volume",
    "instrument4outassign",
    "instrument4lfoselect",
    "instrument4microtuneanbled",

    "instrument5maxnotes",
    "instrument5voicenumbermsb",
    "instrument5voicenumberlsb",
    "instrument5channel",
    "instrument5lowkey",
    "instrument5highkey",
    "instrument5detune",
    "instrument5noteshift",
    "instrument5volume",
    "instrument5outassign",
    "instrument5lfoselect",
    "instrument5microtuneanbled",

    "instrument6maxnotes",
    "instrument6voicenumbermsb",
    "instrument6voicenumberlsb",
    "instrument6channel",
    "instrument6lowkey",
    "instrument6highkey",
    "instrument6detune",
    "instrument6noteshift",
    "instrument6volume",
    "instrument6outassign",
    "instrument6lfoselect",
    "instrument6microtuneanbled",

    "instrument7maxnotes",
    "instrument7voicenumbermsb",
    "instrument7voicenumberlsb",
    "instrument7channel",
    "instrument7lowkey",
    "instrument7highkey",
    "instrument7detune",
    "instrument7noteshift",
    "instrument7volume",
    "instrument7outassign",
    "instrument7lfoselect",
    "instrument7microtuneanbled",

    "instrument8maxnotes",
    "instrument8voicenumbermsb",
    "instrument8voicenumberlsb",
    "instrument8channel",
    "instrument8lowkey",
    "instrument8highkey",
    "instrument8detune",
    "instrument8noteshift",
    "instrument8volume",
    "instrument8outassign",
    "instrument8lfoselect",
    "instrument8microtuneanbled",

    "microtunetable",
    "assignmode",
    "effect",
    "microtunekey",

    // name
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",

    };



    /*
      From mgregory22@gmail.com:

      I wrote an editor for the TX81Z awhile back.  Yeah, I think there are a couple bugs in the manual if I remember correctly. I see you wrote Gizmo in C++, and I worked out all the MIDI bugs in my editor, so I'm going to refer to that.

      Here is a file of constants with subgroup numbers on line 173:
      https://github.com/mgregory22/tx81z-programmer/blob/master/src/tx81z.h#L173

      So, the PCED has a subgroup of 0.

      There's also this file, which describes the different dump types in the TX81Z:
      https://github.com/mgregory22/tx81z-programmer/blob/master/src/tx81z_meta.c

      reqStr is a request string, and dumpHdr is the header of the received dump.

      Here's the code to interface with the TX81Z.  I implemented it as a window that sends messages, so I wouldn't have to write threading code, so it might be kind of confusing in places, but I think you'll be able to get it:
      https://github.com/mgregory22/tx81z-programmer/blob/master/src/tx81z.c

      TX81Z_SendParamChange() is probably what you're interested in, if you want to know how I did things:
      https://github.com/mgregory22/tx81z-programmer/blob/master/src/tx81z.c#L507

      I haven't worked on it in a long time, so I can't remember all that much about it, but if you have any questions, let me know and I'll try to answer them.

      Hope that helps,
      Matt

    */

    public static final int VCED_GROUP = 2 + 16; // 00010010
    public static final int ACED_GROUP = 3 + 16; // 00010011
    public static final int PCED_GROUP = 0 + 16; // 00010000        says 00010011 in the manual, wrong
    public static final int REMOTE_SWITCH_GROUP = 3 + 16; // 00010011        same as ACED_GROUP

    public Object[] emitAll(String key)
        {
        simplePause(50);
        if (key.equals("number")) return new Object[0];  // this is not emittable

        byte channel = (byte)(16 + getChannelOut());
             
        // maybe we don't want to do this
        if (key.equals("name"))  // ugh
            {
            String name = model.get("name", "INIT SOUND") + "          ";
            Object[] result = new Object[10];                       
            for(int i = 0; i < 10; i++)
                {
                result[i] = new byte[] { (byte)0xF0, 0x43, channel, PCED_GROUP, (byte)(100 + i), (byte)(name.charAt(i)), (byte)0xF7 };
                }
            return result;
            }
        for(int i = 1; i < 9; i++)
            {
            if (key.equals("instrument" + i + "voicenumber") || key.equals("instrument" + i + "voicebank"))
                {
                int val = model.get("instrument" + i + "voicebank") * 32 + model.get("instrument" + i + "voicenumber");
                byte lsb = (byte)(val & 127);
                byte msb = (byte)((val >>> 7) & 127);

                return new Object[]
                    {
                    new byte[] { (byte)0xF0, 0x43, channel, PCED_GROUP, (byte)((i - 1) * 12 + 1), msb, (byte)0xF7 },
                    new byte[] { (byte)0xF0, 0x43, channel, PCED_GROUP, (byte)((i - 1) * 12 + 2), lsb, (byte)0xF7 },
                    };
                }
            }        
        
        if (allParametersToIndex.containsKey(key))
            {
            int index = ((Integer)(allParametersToIndex.get(key))).intValue();
            int value = model.get(key);

            byte PP = (byte) index;
            byte VV = (byte) value;
            byte[] data = new byte[] { (byte)0xF0, 0x43, channel, PCED_GROUP, PP, VV, (byte)0xF7 };
            return new Object[] { data };
            }
        else 
            {
            System.err.println("Warning (Yamaha4OpMulti): Can't emit key " + key);
            return new Object[0];
            }
        }
        

    public int parse(byte[] data, boolean fromFile)
        {
        byte name[] = new byte[10];

        if (!Yamaha4OpMultiRec.recognizeBank(data))
            {
            // data starts at byte 16
                                
            for(int i = 0; i < allParameters.length; i++)
                {
                byte val = data[i + 16];
                                
                if (i >= 100) // name
                    {
                    name[i - 100] = val;
                    }
                else if (allParameters[i].equals("instrument1voicenumbermsb") ||
                    allParameters[i].equals("instrument2voicenumbermsb") ||
                    allParameters[i].equals("instrument3voicenumbermsb") ||
                    allParameters[i].equals("instrument4voicenumbermsb") ||
                    allParameters[i].equals("instrument5voicenumbermsb") ||
                    allParameters[i].equals("instrument6voicenumbermsb") ||
                    allParameters[i].equals("instrument7voicenumbermsb") ||
                    allParameters[i].equals("instrument8voicenumbermsb"))
                    {
                    // ignore, we handle in lsb
                    }
                else if (allParameters[i].equals("instrument1voicenumberlsb") ||
                    allParameters[i].equals("instrument2voicenumberlsb") ||
                    allParameters[i].equals("instrument3voicenumberlsb") ||
                    allParameters[i].equals("instrument4voicenumberlsb") ||
                    allParameters[i].equals("instrument5voicenumberlsb") ||
                    allParameters[i].equals("instrument6voicenumberlsb") ||
                    allParameters[i].equals("instrument7voicenumberlsb") ||
                    allParameters[i].equals("instrument8voicenumberlsb"))
                    {
                    int instrument = (i / 12);
                    int msb = data[16 + instrument * 12 + 1];
                    int lsb = data[16 + instrument * 12 + 2];
                                                                
                    // this data is often corrupted.  So we'll mask it just in case
                                
                    int combined = (((msb & 1) << 7) | (lsb & 127));
                                
                    // we'll also make sure it's in bounds
                                         
                    int oldcombined = combined;           
                    combined = combined % 160;

                    if (msb > 1 || lsb > 127 || oldcombined != combined)
                        System.err.println("Warning (Yamaha4OpMulti): Corrupt voice number or bank in received data.");


                    model.set("instrument" + (instrument + 1) + "voicenumber", combined % 32);
                    model.set("instrument" + (instrument + 1) + "voicebank", combined / 32);
                    }
                else
                    {
                    model.set(allParameters[i], val);
                    }
                }
            }
        else            // Bank
            {
            return parsePMEM(data, fromFile);
            }
                
        try { model.set("name", new String(name, "US-ASCII")); }
        catch (Exception e) { Synth.handleException(e); }
                
        revise();
        return PARSE_SUCCEEDED;
        }
  
  
  
  
  
    /** Parses a given patch from the provided bank sysex, and returns 
        PARSE_SUCCEEDED or PARSE_SUCCEEDED_UNTITLED if successful, else PARSE_FAILED (the default). */
    public int parseFromBank(byte[] data, int number)
        {
        // okay, we're loading and editing patch number.  Here we go.
        int patch = number * 76;
        int pos = 0;

        // extract name
        char[] name = new char[10];
        for (int j = 0; j < 10; j++)
            {
            name[j] = (char)(data[patch + 66 + j + 16] & 127);
            }
                        
        model.set("name", new String(name));
        model.set("number", number);
        //model.set("bank", 0);                   // we don't know what the bank is in reality
                
        for(int op = 0; op < 8; op++)
            {
            // max notes
            model.set(allParameters[pos++], (data[patch + op * 8 + 0 + 16] >>> 0) & 15);
            // voice number msb
            model.set(allParameters[pos++], (data[patch + op * 8 + 0 + 16] >>> 4) & 1);
            // voice number lsb
            model.set(allParameters[pos++], data[patch + op * 8 + 1 + 16] & 127);
            // channel
            model.set(allParameters[pos++], (data[patch + op * 8 + 2 + 16] >>> 0) & 31);
            // low key
            model.set(allParameters[pos++], data[patch + op * 8 + 3 + 16] & 127);
            // high key
            model.set(allParameters[pos++], data[patch + op * 8 + 4 + 16] & 127);
            // detune
            model.set(allParameters[pos++], data[patch + op * 8 + 5 + 16] & 15);
            // note shift
            model.set(allParameters[pos++], (data[patch + op * 8 + 6 + 16] >>> 0) & 63);
            // volume
            model.set(allParameters[pos++], data[patch + op * 8 + 7 + 16] & 127);
            // out assign
            model.set(allParameters[pos++], (data[patch + op * 8 + 0 + 16] >>> 5) & 3);
            // lfo select
            model.set(allParameters[pos++], (data[patch + op * 8 + 2 + 16] >>> 5) & 3);
            // micro tune enabled
            model.set(allParameters[pos++], (data[patch + op * 8 + 6 + 16] >>> 6) & 1);            
            }

        // microtunetable
        model.set(allParameters[pos++], data[patch + 64 + 16] & 15);
       
        // The documentation is poorly written here.  But I believe that
        // KEY (which needs 4 bits) is b3...b6, EFSEL (which needs 2 bits)
        // is b1...b2, and ASMODE (which needs 1 bit) is b0.
       
        // assignmode
        model.set(allParameters[pos++], (data[patch + 65 + 16] >>> 0) & 1);
        // effectselect
        model.set(allParameters[pos++], (data[patch + 65 + 16] >>> 1) & 3);
        // microtunekey
        model.set(allParameters[pos++], (data[patch + 65 + 16] >>> 3) & 15);
        
        revise();
        return PARSE_SUCCEEDED;  
        }

    public int parsePMEM(byte[] data, boolean fromFile)
        {
        // data starts at byte 16
                                
        // extract names
        char[][] names = new char[32][10];
        for(int i = 0; i < 32; i++)
            {
            for (int j = 0; j < 10; j++)
                {
                names[i][j] = (char)(data[i * 76 + 66 + j + 16] & 127);
                }
            }
                        
        String[] n = new String[32];
        for(int i = 0; i < 32; i++)
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

        return parseFromBank(data, patchNum);
        }
        
        
    public int getPauseAfterChangePatch()
        { 
        return 500;                                                             // Sucks, but we'll often fail if it's any less than this.  :-(
        }
    
    public int getPauseAfterSendAllParameters() 
        {
        return 200; 
        }


    // PMEM
    public Object[] emitBank(Model[] models, int bank, boolean toFile)
        {
        byte[] data = new byte[4104];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x43;
        data[2] = (byte)(getChannelOut());;
        data[3] = (byte)0x7E;
        data[4] = (byte)0x13;
        data[5] = (byte)0x0A;
        data[6] = (byte)'L';
        data[7] = (byte)'M';
        data[8] = (byte)' ';
        data[9] = (byte)' ';
        data[10] = (byte)'8';
        data[11] = (byte)'9';
        data[12] = (byte)'7';
        data[13] = (byte)'6';
        data[14] = (byte)'P';
        data[15] = (byte)'M';
        
        for(int number = 0; number < 24; number++)
            {
            // okay, we're loading and editing patch number.  Here we go.
            int patch = number * 76;
            int pos = 0;

            // emit name
            char[] name = (models[number].get("name", "INIT VOICE") + "          ").toCharArray();
            for (int j = 0; j < 10; j++)
                {
                data[patch + 66 + j + 16] = (byte)(name[j] & 127);
                }
                                                
            for(int op = 0; op < 8; op++)
                {
                // max notes
                data[patch + op * 8 + 0 + 16] |= (byte) ((models[number].get(allParameters[pos++]) & 15) << 0);
                // voice number msb
                data[patch + op * 8 + 0 + 16] |= (byte) ((models[number].get(allParameters[pos++]) & 1) << 4);
                // voice number lsb
                data[patch + op * 8 + 1 + 16] = (byte) (models[number].get(allParameters[pos++]) & 127);
                // channel
                data[patch + op * 8 + 2 + 16] |= (byte) ((models[number].get(allParameters[pos++]) & 31) << 0);
                // low key
                data[patch + op * 8 + 3 + 16] = (byte) (models[number].get(allParameters[pos++]) & 127);
                // high key
                data[patch + op * 8 + 4 + 16] = (byte) (models[number].get(allParameters[pos++]) & 127);
                // detune
                data[patch + op * 8 + 5 + 16] = (byte) (models[number].get(allParameters[pos++]) & 15);
                // note shift
                data[patch + op * 8 + 6 + 16] |= ((byte) (models[number].get(allParameters[pos++]) & 63) << 0);
                // volume
                data[patch + op * 8 + 7 + 16] = (byte) (models[number].get(allParameters[pos++]) & 127);
                // out assign
                data[patch + op * 8 + 0 + 16] |= ((byte) (models[number].get(allParameters[pos++]) & 3) << 5);
                // lfo select
                data[patch + op * 8 + 2 + 16] |= ((byte) (models[number].get(allParameters[pos++]) & 3) << 5);
                // micro tune enabled
                data[patch + op * 8 + 6 + 16] |= ((byte) (models[number].get(allParameters[pos++]) & 1) << 6);            
                }

            // microtunetable
            data[patch + 64 + 16] = (byte) (models[number].get(allParameters[pos++]) & 15);
           
            // The documentation is poorly written here.  But I believe that
            // KEY (which needs 4 bits) is b3...b6, EFSEL (which needs 2 bits)
            // is b1...b2, and ASMODE (which needs 1 bit) is b0.
           
            // assignmode
            data[patch + 65 + 16] |= (byte) ((models[number].get(allParameters[pos++]) & 1) << 0);
            // effectselect
            data[patch + 65 + 16] |= (byte) ((models[number].get(allParameters[pos++]) & 3) << 1);
            // microtunekey
            data[patch + 65 + 16] |= (byte) ((models[number].get(allParameters[pos++]) & 15) << 3);
            }
                
        data[data.length - 2] = produceChecksum(data, 16);
        data[data.length - 1] = (byte)0xF7;
        return new Object[] { data };
        }


    public byte[] emit(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        if (tempModel == null)
            tempModel = getModel();

        simplePause(50);

        byte data[] = new byte[120];
        data[0] = (byte)'L';
        data[1] = (byte)'M';
        data[2] = (byte)' ';
        data[3] = (byte)' ';
        data[4] = (byte)'8';
        data[5] = (byte)'9';
        data[6] = (byte)'7';
        data[7] = (byte)'6';
        data[8] = (byte)'P';
        data[9] = (byte)'E';
                
        String name = model.get("name", "INIT SOUND") + "          ";
        // Next the PCED
        for(int i = 0; i < allParameters.length; i++)  // no name, no operatorenabled
            {
            if (allParameters[i].equals("instrument1voicenumbermsb") ||
                allParameters[i].equals("instrument2voicenumbermsb") ||
                allParameters[i].equals("instrument3voicenumbermsb") ||
                allParameters[i].equals("instrument4voicenumbermsb") ||
                allParameters[i].equals("instrument5voicenumbermsb") ||
                allParameters[i].equals("instrument6voicenumbermsb") ||
                allParameters[i].equals("instrument7voicenumbermsb") ||
                allParameters[i].equals("instrument8voicenumbermsb"))
                {
                int instrument = (i / 12);
                int num = model.get("instrument" + (instrument + 1) + "voicebank") * 32 + model.get("instrument" + (instrument + 1) + "voicenumber");
                data[i + 10] = (byte)(num >>> 7);
                }
            else if (allParameters[i].equals("instrument1voicenumberlsb") ||
                allParameters[i].equals("instrument2voicenumberlsb") ||
                allParameters[i].equals("instrument3voicenumberlsb") ||
                allParameters[i].equals("instrument4voicenumberlsb") ||
                allParameters[i].equals("instrument5voicenumberlsb") ||
                allParameters[i].equals("instrument6voicenumberlsb") ||
                allParameters[i].equals("instrument7voicenumberlsb") ||
                allParameters[i].equals("instrument8voicenumberlsb"))
                {
                int instrument = (i / 12);
                int num = model.get("instrument" + (instrument + 1) + "voicebank") * 32 + model.get("instrument" + (instrument + 1) + "voicenumber");
                data[i + 10] = (byte)(num & 127);
                }
            else if (i >= 100)      // name
                {
                data[i + 10] = (byte)(name.charAt(i - 100));
                }
            else
                {
                data[i + 10] = (byte)(model.get(allParameters[i]));
                }
            }
        
        byte[] result = new byte[128];
        result[0] = (byte)0xF0;
        result[1] = 0x43;
        result[2] = (byte)getChannelOut();  //(byte)(32 + getChannelOut());
        result[3] = 0x7E;
        result[4] = 0x00;
        result[5] = 0x78;
        System.arraycopy(data, 0, result, 6, data.length);
        result[6 + data.length] = produceChecksum(data);
        result[6 + data.length + 1] = (byte)0xF7;
        
        return result;
        }

    /** Generate a TX81Z checksum of the data bytes */
    byte produceChecksum(byte[] bytes, int start)
        {
        //      The TX81Z manual says the checksum is the
        //              "Twos complement of the lower 7 bits of the sum of all databytes".
        //
        //              Apparently this is mistaken.  Based on the function "Snapshot_Checksum" here...
        //              https://raw.githubusercontent.com/mgregory22/tx81z-programmer/master/src/snapshot.c
        //
        //              It may be otherwise.  So here's my shot.

        int checksum = 0;
        for(int i = start; i < bytes.length; i++)
            checksum = (checksum + bytes[i]) & 255;
        return (byte)((256 - checksum) & 127);
        }

    /** Generate a TX81Z checksum of the data bytes */
    byte produceChecksum(byte[] bytes)
        {
        return produceChecksum(bytes, 0);
        }


    public void performRequestDump(Model tempModel, boolean changePatch)
        {
        if (tempModel == null)
            tempModel = getModel();

        // We ALWAYS change the patch no matter what.  We have to.
        performChangePatch(tempModel);
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
        
    public byte[] requestCurrentDump()
        {
        // PCED
        byte channel = (byte)(32 + getChannelOut());
        return new byte[] { (byte)0xF0, 0x43, channel, 0x7E, 
            (byte)'L', (byte)'M', (byte)' ', (byte)' ',
            (byte)'8', (byte)'9', (byte)'7', (byte)'6',
            (byte)'P', (byte)'E', (byte)0xF7 }; 
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
        
    public static String getSynthName() { return "Yamaha 4-Op FM [Multi]"; }
    
    public void changePatch(Model tempModel) 
        {
        int number = tempModel.get("number");
        
        /// NOTE: There is an error in the sysex document (page 68), where
        // it says that PF1-F24 are slots 161-184 in the Program Change Table.
        // Actually they are slots 160-183.  
        
        // Performance numbers PF1 ... PF24, corresponding to 161 .. 184
        byte lo = (byte)((number + 160) & 127);
        byte hi = (byte)((number + 160) >>> 7);

        // A program change in the TX81Z is a complicated affair.  We need to do three things:
        //
        // 1. Modify a slot in the program change table to the patch we want.  We'll modify slot 127.
        //
        // 2. At this point the TX81Z is in a strange "I got edited via MIDI" mode.  We need to get
        //    out of that and into standard program mode.  We do this by using sysex commands to virtually press
        //    the PLAY/PERFORM switch.
        //
        // 3. Now we're either in PLAY mode or we're in PERFORM mode.  At this point we send a PC 127, which
        //    causes the system to look up slot 127 in its table, discover it's a program change (not
        //    performance) value, and switch to that value, while also changing to PLAY mode.

        // Change program change table position 127 to what we want first
        byte[] table = new byte[9];
        table[0] = (byte)0xF0;
        table[1] = (byte)0x43;
        table[2] = (byte)(16 + getChannelOut());
        table[3] = (byte)0x10;
        table[4] = (byte)127;  // really!
        table[5] = (byte)127;  // we're changing table position 127
        table[6] = hi;
        table[7] = lo;
        table[8] = (byte)0xF7;
        tryToSendSysex(table);
        
        // Instruct the TX81Z to press its "PLAY/PERFORM" button.  Or "PERFORM" on the DX11
        byte PP = getSynthType() == TYPE_TX81Z ? (byte) 68 : (byte) 119;                // 119 is "PERFORM", 118 is "SINGLE"
        byte VV = (byte) 0;
        byte[] data = new byte[] { (byte)0xF0, (byte)0x43, (byte)(16 + getChannelOut()), REMOTE_SWITCH_GROUP, PP, (byte)0x7F, (byte)0xF7 };
        tryToSendSysex(data);

        // Do the program change to program 127
        tryToSendMIDI(buildPC(getChannelOut(), 127));

        if (!isMerging())  // we're actually loading the patch, not merging with it
            {
            // we assume that we successfully did it
            setSendMIDI(false);
            model.set("number", number);
            setSendMIDI(true);
            }
        }

    public String getPatchName(Model model) { return model.get("name", "INIT VOICE"); }

    public Model getNextPatchLocation(Model model)
        {
        int number = model.get("number");
        
        number++;
        if (number >= 24)
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
        if (!model.exists("bank")) return null;
        
        int number = model.get("number") + 1;
        return "PF" + (number > 9 ? "" : "0") + number;
        }
        

    public Object adjustBankSysexForEmit(byte[] data, Model model, int bank)
        { 
        data[2] = (byte) getChannelOut();
        return data; 
        }
  
    /** Return a list of all patch number names.  Default is { "Main" } */
    public String[] getPatchNumberNames() { return buildIntegerNames(24, 1); }
                
    /** Return a list whether patches in banks are writeable.  Default is { false } */
    public boolean[] getWriteableBanks() { return new boolean[] { true }; }

    public boolean getSupportsBankWrites() { return true; }

    public int getPatchNameLength() { return 10; }

    public int getRequestableBank() { return 0; }
    
    public byte[] requestBankDump(int bank) 
        { 
        return new byte[] 
            { 
            (byte)0xF0, 
            0x43, 
            (byte)(0x20 + getChannelOut()), 
            0x7E,
            (byte)'L',
            (byte)'M',
            (byte)' ',
            (byte)' ',
            (byte)'8',
            (byte)'9',
            (byte)'7',
            (byte)'6',
            (byte)'P',
            (byte)'M',
            (byte)0xF7 
            }; 
        }
    public boolean librarianTested() { return true; }
    }
