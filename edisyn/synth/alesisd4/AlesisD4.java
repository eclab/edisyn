/***
    Copyright 2020 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.alesisd4;

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
   A patch editor for the Alesis D4 and DM5.
        
   @author Sean Luke
*/

public class AlesisD4 extends Synth
    {
    public static final String[] PANS = { "<3", "<2", "<1", "--", "1>", "2", "3" }; 
    public static final String[] GROUPS = { "Multi", "Single", "Group 1", "Group 2" }; 
    public static final String[] KEYS =  { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };

    // These are NRPN parameter settings for various stuff we emit for individual parameters
    public static final int NRPN_ROOT = 0x10;
    public static final int NRPN_NOTE = 0x19;
    public static final int NRPN_BANK = 0x08;
    public static final int NRPN_NUMBER = 0x09;
    public static final int NRPN_COARSE = 0x0A;
    public static final int NRPN_FINE = 0x0B;
    public static final int NRPN_VOLUME = 0x0C;
    public static final int NRPN_PAN = 0x0D;
    public static final int NRPN_OUTPUT = 0x0E;
    public static final int NRPN_GROUP = 0x0F;
    
    public static final int PAUSE_NRPN = 50;
        
    // All the drum sounds are lumped together (see end of file).  These are the numbers
    // of each drum sound in each group -- I need this to break them out to send/receive sysex
    public static final int[] D4_BANKS = { 99, 99, 55, 92, 76, 80 };
    public static final int[] DM5_BANKS = { 95, 117, 71, 34, 36, 114, 65, 16 };
    
    public int testNote = 60;
     
    // choosers updated when changing D4 <--> D5M or changing the root note   
    Chooser[] drumChoosers = new Chooser[61];
    
    JCheckBox check;    
    boolean dm5;
    public static final String DM5_KEY = "DM5";
    
    public boolean isDM5() { return dm5; }
    boolean reenntrantBlock = false;
    public void setDM5(boolean val, boolean store)
        {
        if (reenntrantBlock) return;
        reenntrantBlock = true;
        if (store) setLastX("" + (!val), DM5_KEY, getSynthClassName(), false);
        dm5 = val;
        updateTitle();
        updateChoosers();
        if (check != null) check.setSelected(dm5);
        reenntrantBlock = false;
        }
    
    public static String getSynthName() { return "Alesis D4/DM5"; }

    public AlesisD4()
        {
        model.set("number", 0);
        
        String m = getLastX(DM5_KEY, getSynthClassName());
        dm5 = (m == null ? false : !Boolean.parseBoolean(m));
        
        VBox vbox = new VBox();
        HBox hbox = new HBox();

        JComponent sourcePanel = new SynthPanel(this);
        
        vbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        vbox.add(addTriggers(Style.COLOR_A()));
        vbox.add(addDrums(0, 7, Style.COLOR_B()));
        sourcePanel.add(vbox, BorderLayout.CENTER);
        addTab("General and Drums 0-6", sourcePanel);

        boolean primary = true;
        for(int i = 7; i <= 60; i+= 18)
            {
            sourcePanel = new SynthPanel(this);
            vbox = new VBox();
            vbox.add(addDrums(i, i+18, primary ? Style.COLOR_A() : Style.COLOR_B()));
            sourcePanel.add(vbox, BorderLayout.CENTER);
            addTab("Drums " + i + "-" + (i + 17), sourcePanel);
            primary = !primary;
            }

        model.set("name", "Untitled");
        
        loadDefaults();
        
        updateChoosers();
        }
                
    public String getDefaultResourceFileName() 
        { 
        if (isDM5()) return "AlesisDM5.init"; 
        else return "AlesisD4.init";
        }

    public String getHTMLResourceFileName() { return "AlesisD4.html"; }

    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        JTextField number = new SelectedTextField("" + model.get("number"), 3);

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
                showSimpleError(title, "The Patch Number must be an integer 0...20");
                continue;
                }
            if (n < 0 || n > 20)
                {
                showSimpleError(title, "The Patch Number must be an integer 0...20");
                continue;
                }
                
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
        comp = new PatchDisplay(this, 9, false);
        hbox2.add(comp);

        check = new JCheckBox("DM5");
        check.setSelected(dm5);
        check.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                setDM5(check.isSelected(), true);
                }
            });
        check.setFont(Style.SMALL_FONT());
        check.setOpaque(false);
        check.setForeground(Style.TEXT_COLOR());
        hbox2.addLast(check);
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
        vbox.add(comp);

        hbox.add(vbox);

        comp = new LabelledDial("Root Note", this, "drumsetnoteroot", color, 0, 67)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                updateDrumNoteLabels();         // gonna be expensive
                }

            public String map(int val)
                {
                return KEYS[val % 12] + (val / 12 - 2);
                }
            };
        hbox.add(comp);
        
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

    public String getDrumNoteLabel(int note, int root)
        {
        // The default root is 36
        int n = note + root;
        if (n == 60) // middle C
            return " Drum " + note + " ( " + n + ": Middle C )";
        else
            return " Drum " + note + " ( " + n + ": " + KEYS[n % 12] + (n / 12 - 2) + " )";
        }
                
    public void updateDrumNoteLabels()
        {
        int root = model.get("drumsetnoteroot", 36);
        for(int i = 0; i < 61; i++)
            if (drumChoosers[i] != null)
                drumChoosers[i].getLabel().setText(getDrumNoteLabel(i, root));
        repaint();
        }
                
    public void updateChoosers()
        {
        // disable listeners
        boolean li = model.getUpdateListeners();
        model.setUpdateListeners(false);
        boolean un = undo.getWillPush();
        undo.setWillPush(false);

        for(int i = 0; i < 61; i++)
            {
            if (drumChoosers[i] != null)
                {
                int j = drumChoosers[i].getIndex();
                if (!isDM5() && j >= D4_DRUMS.length)   // too high
                    {
                    j = 0;
                    }
                drumChoosers[i].setElements(drumChoosers[i].getLabelText(), isDM5() ? DM5_DRUMS : D4_DRUMS);
                drumChoosers[i].setIndex(j);
                }
            }
                        
        // reenable listeners
        model.setUpdateListeners(li);
        undo.setWillPush(un);    
                                       
        repaint();
        }

    public JComponent addDrums(int start, int end, Color color)
        {
        Category category = new Category(this, "Drums " + start + " - " + (end - 1), color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        
        int root = model.get("drumsetnoteroot", 36);
        for(int i = start; i < end; i+=2)
            {
            hbox = new HBox();
                
            VBox vbox2 = new VBox();
            params = D4_DRUMS;
            comp = new Chooser(getDrumNoteLabel(i, root),  this, "drum" + i + "voice", params);
            vbox2.add(comp);
            drumChoosers[i] = ((Chooser)comp);

            HBox hbox2 = new HBox();
            params = GROUPS;
            comp = new CheckBox("Aux Out", this, "drum" + i + "output");
            hbox2.add(comp);
            comp = new Chooser("Groups",  this, "drum" + i + "groups", params);
            hbox2.add(comp);
                        
            vbox2.add(hbox2);
            hbox.add(vbox2);

            comp = new LabelledDial("Volume", this, "drum" + i + "volume", color, 0, 99);
            hbox.add(comp);

            comp = new LabelledDial("Pan", this, "drum" + i + "pan", color, 0, 6)
                {
                public String map(int val)
                    {
                    return PANS[val];
                    }
                public boolean isSymmetric() { return true; }
                };
            hbox.add(comp);

            comp = new LabelledDial("Coarse", this, "drum" + i + "coarse", color, 0, 7, 4)
                {
                public int getDefaultValue() { return 4; }
                public double getStartAngle()
                    {
                    return 245;
                    }
                };
            hbox.add(comp);

            comp = new LabelledDial("Fine", this, "drum" + i + "fine", color, 0, 99);
            hbox.add(comp);
                        
            if (i < end - 1)
                {
                hbox.add(Strut.makeHorizontalStrut(30));
                        
                vbox2 = new VBox();
                params = D4_DRUMS;
                comp = new Chooser(getDrumNoteLabel((i + 1), root),  this, "drum" + (i + 1) + "voice", params);
                vbox2.add(comp);
                drumChoosers[i + 1] = ((Chooser)comp);

                hbox2 = new HBox();
                params = GROUPS;
                comp = new CheckBox("Aux Out", this, "drum" + (i + 1) + "output");
                hbox2.add(comp);
                comp = new Chooser("Groups",  this, "drum" + (i + 1) + "groups", params);
                hbox2.add(comp);
                        
                vbox2.add(hbox2);
                hbox.add(vbox2);

                comp = new LabelledDial("Volume", this, "drum" + (i + 1) + "volume", color, 0, 99);
                hbox.add(comp);

                comp = new LabelledDial("Pan", this, "drum" + (i + 1) + "pan", color, 0, 6)
                    {
                    public String map(int val)
                        {
                        return PANS[val];
                        }
                    public boolean isSymmetric() { return true; }
                    };
                hbox.add(comp);

                comp = new LabelledDial("Coarse", this, "drum" + (i + 1) + "coarse", color, 0, 7, 4)
                    {
                    public int getDefaultValue() { return 4; }
                    public double getStartAngle()
                        {
                        return 245;
                        }
                    };
                hbox.add(comp);

                comp = new LabelledDial("Fine", this, "drum" + (i + 1) + "fine", color, 0, 99);
                hbox.addLast(comp);
                }
                        
            vbox.add(hbox);
            }
        
        category.add(vbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addTriggers(Color color)
        {
        Category category = new Category(this, "Triggers", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        comp = new LabelledDial("Footswitch", this, "footswitchclosing", color, 0, 60)
            {
            public String map(int val)
                {
                val += model.get("drumsetnoteroot", 0);
                return KEYS[val % 12] + (val / 12 - 2);
                }
            };
        getModel().register("drumsetnoteroot", ((LabelledDial)comp));
        ((LabelledDial)comp).addAdditionalLabel("Closing");
        hbox.add(comp);

        comp = new LabelledDial("Footswitch", this, "footswitchheld", color, 0, 60)
            {
            public String map(int val)
                {
                val += model.get("drumsetnoteroot", 0);
                return KEYS[val % 12] + (val / 12 - 2);
                }
            };
        getModel().register("drumsetnoteroot", ((LabelledDial)comp));
        ((LabelledDial)comp).addAdditionalLabel("Held");
        hbox.add(comp);

        for(int i = 1; i <= 12; i++)
            {
            comp = new LabelledDial("Trigger " + i + " ", this, "trigger" + i, color, 0, 60)
                {
                public String map(int val)
                    {
                    val += model.get("drumsetnoteroot", 0);
                    return KEYS[val % 12] + (val / 12 - 2);
                    }
                };
            getModel().register("drumsetnoteroot", ((LabelledDial)comp));
            hbox.add(comp);
            }
        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public int getTestNotePitch() { return testNote; }


    public int parse(byte[] data, boolean fromFile)
        {
        boolean d4 = (data[4] == 0x06);
        int opcode = data[6];
        int drumset = 0;
        
        if (opcode == 0x01) // edit buffer
            {
            drumset = -1;           // we'll say that's the edit buffer
            }
        if (opcode < 64 && opcode >= 32) // Drumset.  Stuff >= 64 are requests.
            {
            drumset = (opcode - 32);
            }
                
        if (drumset <= 21)  // we got one
            {
            setDM5(!d4, false);
            if (drumset != -1)
                model.set("number", drumset);
                
            int pos = 7;            // start of data
                
            // get name
            char[] name = new char[14];
            for(int i = 0; i < 14; i++)
                name[i] = (char)(data[pos++]);
            model.set("name", new String(name));
                
            model.set("drumsetnoteroot", data[pos++]);
            model.set("footswitchclosing", data[pos++]);
            model.set("footswitchheld", data[pos++]);
            for(int i = 1; i <= 12; i++)
                {
                model.set("trigger" + i, data[pos++]);
                }
                
            // packets
            for(int i = 0; i < 61; i++)
                {
                model.set("drum" + i + "volume", data[pos++]);
                byte b = data[pos++];
                model.set("drum" + i + "pan", b >>> 4);
                model.set("drum" + i + "output", (b >>> 3) & 1);
                int bank = (b & 7);
                int number = data[pos++];
                
                int banksum = 0;
                for(int bb = 0; bb < bank; bb++)
                    banksum += (d4 ? D4_BANKS[bb > 5 ? 0 : bb] : DM5_BANKS[bb > 7 ? 0 : bb]);
                
                // assemble bank and number into voice
                model.set("drum" + i + "voice", banksum + number);
                model.set("drum" + i + "fine", data[pos++]);
                b = data[pos++];
                model.set("drum" + i + "groups", (b >>> 3) & 7);
                model.set("drum" + i + "coarse", b & 7);
                }
            return PARSE_SUCCEEDED;
            }
        else return PARSE_FAILED;
        }


    public int map(int i, int max)
        {
        int v = (int)((i * 127.0 + max) / max);  
        if (v > 127) v = 127;
        return v;
        }

    public Object[] emitAll(String key) 
        { 
        if (key.equals("drumsetnoteroot"))
            {
            ArrayList data = new ArrayList();
            final int total = 68;
            Object[] nrpn = buildNRPN(getChannelOut(), NRPN_ROOT, (128 * map(model.get(key), total-1))); 
            for(int i = 0; i < nrpn.length; i++) data.add(nrpn[i]);
            data.add(Integer.valueOf(PAUSE_NRPN));
            return (Object[])(data.toArray(new Object[0]));
            }
        else if (key.startsWith("drum") && !key.equals("drumsetnoteroot"))
            {
            // we can't emit a parameter, but at least we can set up
            // the preview button to play it
                
            int drum = StringUtility.getFirstInt(key);
                
            // compute bank and number
            int voice = model.get("drum" + drum + "voice");
            
            int bank = -1;
            int number = -1;
            for(int j = 0; j < (isDM5() ? DM5_BANKS.length : D4_BANKS.length); j++)
                {
                if (voice < (isDM5() ? DM5_BANKS[j] : D4_BANKS[j]))
                    {
                    bank = j;
                    number = voice;
                    break;
                    }
                else
                    voice -= (isDM5() ? DM5_BANKS[j] : D4_BANKS[j]);
                }
                        
            if (bank == -1) // error, should never happen
                {
                System.err.println("ERROR (AlesisD4.emit): bank and voice are bad for " + isDM5() + " " + model.get("drum" + drum + "voice"));
                return new Object[0];
                }
            
            
            //// IMPORTANT NOTE
            ////
            //// True to form, the NRPN documentation in Alesis's D4 service manual is completely wrong.
            //// The manual states that to compute the NRPN MSB value, you take the current value, multiply by 127,
            //// then divide by the maximum value.  They even give a (wrong) example: to compute the value for
            //// volume = 50 (volume goes 0...99), you do 50 * 127 / 99 = 64.  WRONG WRONG WRONG.
            ////
            //// The correct formula is unknown.  However for all parameters below except for COARSE TUNING,
            //// I have had success with the equation shown in map(i, max) above.  For COARSE TUNING below
            //// I have a custom equation which seems to work right.
            ////
            //// Absolutely nowhere on the internet does a correction appear for this.  Apparently nobody noticed.
            ////
            //// I can only test on the D4, so I am not certain if these equations will work properly for the drum
            //// voices for the DM5.  I need someone else to test for me.
            
            
            ArrayList data = new ArrayList();
            
            // I believe this sets the note that preview is playing, and also (?)
            // more importantly, the later changes after it will change that particular note.
            // Maybe?
            Object[] nrpn = null;
            
            // always
            testNote = drum + model.get("drumsetnoteroot");
            nrpn = buildNRPN(getChannelOut(), NRPN_NOTE, 128 * map(drum, 60));
            for(int i = 0; i < nrpn.length; i++) data.add(nrpn[i]);
            data.add(Integer.valueOf(PAUSE_NRPN));
                        

            if (key.endsWith("voice"))
                {
                int total = (isDM5() ? DM5_BANKS.length : D4_BANKS.length);
                nrpn = buildNRPN(getChannelOut(), NRPN_BANK, (128 * ((bank + 1) * 127 / total)));  
                for(int i = 0; i < nrpn.length; i++) data.add(nrpn[i]);
                data.add(Integer.valueOf(PAUSE_NRPN));
                        
                total = (isDM5() ? DM5_BANKS[bank]: D4_BANKS[bank]);
                //int math = ((number + 1) * 127) / total;
                //int math = ((number + 1 - 1) * 127) / (total - 1);
                nrpn = buildNRPN(getChannelOut(), NRPN_NUMBER, 128 * map(number, total-1)); 
                for(int i = 0; i < nrpn.length; i++) data.add(nrpn[i]);
                data.add(Integer.valueOf(PAUSE_NRPN));
                }
                                
            else if (key.endsWith("coarse"))
                {
                final int total = 8;
                // my mapping function doesn't work for this one
                //                nrpn = buildNRPN(getChannelOut(), NRPN_COARSE, (128 * map(model.get(key), total-1)));         
                nrpn = buildNRPN(getChannelOut(), NRPN_COARSE, (128 * ((model.get(key) + 1) * 127 / total)));           // this one works however
                for(int i = 0; i < nrpn.length; i++) data.add(nrpn[i]);
                data.add(Integer.valueOf(PAUSE_NRPN));
                }
                                
            else if (key.endsWith("fine"))
                {
                final int total = 100;
                nrpn = buildNRPN(getChannelOut(), NRPN_FINE, (128 * map(model.get(key), total-1)));             // (128 * ((model.get(key) + 1) * 127 / total)));  
                for(int i = 0; i < nrpn.length; i++) data.add(nrpn[i]);
                data.add(Integer.valueOf(PAUSE_NRPN));
                }

            else if (key.endsWith("volume"))
                {
                final int total = 100;
                nrpn = buildNRPN(getChannelOut(), NRPN_VOLUME, (128 * map(model.get(key), total-1)));           // (128 * ((model.get(key) + 1) * 127 / total)));  
                for(int i = 0; i < nrpn.length; i++) data.add(nrpn[i]);
                data.add(Integer.valueOf(PAUSE_NRPN));
                }

            else if (key.endsWith("pan"))
                {
                final int total = 7;
                nrpn = buildNRPN(getChannelOut(), NRPN_PAN, (128 * map(model.get(key), total-1)));              // (128 * ((model.get(key) + 1) * 127 / total)));  
                for(int i = 0; i < nrpn.length; i++) data.add(nrpn[i]);
                data.add(Integer.valueOf(PAUSE_NRPN));
                }

            else if (key.endsWith("output"))
                {
                final int total = 2;
                nrpn = buildNRPN(getChannelOut(), NRPN_OUTPUT, (128 * map(model.get(key), total-1)));           // (128 * ((model.get(key) + 1) * 127 / total)));  
                for(int i = 0; i < nrpn.length; i++) data.add(nrpn[i]);
                data.add(Integer.valueOf(PAUSE_NRPN));
                }

            else if (key.endsWith("groups"))
                {
                final int total = 4;
                nrpn = buildNRPN(getChannelOut(), NRPN_GROUP, (128 * map(model.get(key), total-1)));            // (128 * ((model.get(key) + 1) * 127 / total)));  
                for(int i = 0; i < nrpn.length; i++) data.add(nrpn[i]);
                data.add(Integer.valueOf(PAUSE_NRPN));
                }
                
            else
                {
                System.err.println("ERROR (AlesisD4.emit): unknown key " + key + ", should never happen.");
                return new Object[0];
                }

            return (Object[])(data.toArray(new Object[0]));
            }
        else
            {
            return new Object[0];
            }
        }
                
                
    public byte[] emit(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        if (tempModel == null)
            tempModel = getModel();

        boolean d4 = !isDM5();
        byte[] data = new byte[343];
        
        data[0] = (byte)0xF0;
        data[1] = (byte)0x00;
        data[2] = (byte)0x00;
        data[3] = (byte)0x0E;
        data[4] = (byte)(d4 ? 0x06 : 0x13);
        data[5] = (byte)getChannelOut();
        data[6] = (byte)(toWorkingMemory ? 0x01 : 32 + tempModel.get("number"));
        
        int pos = 7;            // start of data
                
        String name = model.get("name", "              ") + "              ";
        for(int i = 0; i < 14; i++)
            data[pos++] = (byte)(name.charAt(i));
        
        data[pos++] = (byte)model.get("drumsetnoteroot");
        data[pos++] = (byte)model.get("footswitchclosing");
        data[pos++] = (byte)model.get("footswitchheld");
        for(int i = 1; i <= 12; i++)
            {
            data[pos++] = (byte)model.get("trigger" + i);
            }
                
        // packets
        for(int i = 0; i < 61; i++)
            {
            // compute bank and number
            int voice = model.get("drum" + i + "voice");
            int bank = -1;
            int number = -1;
            for(int j = 0; j < (isDM5() ? DM5_BANKS.length : D4_BANKS.length); j++)
                {
                if (voice < (isDM5() ? DM5_BANKS[j] : D4_BANKS[j]))
                    {
                    bank = j;
                    number = voice;
                    break;
                    }
                else
                    voice -= (isDM5() ? DM5_BANKS[j] : D4_BANKS[j]);
                }
                                
            if (bank == -1) // error, should never happen
                {
                System.err.println("ERROR (AlesisD4.emit): bank and voice are bad for " + isDM5() + " " + model.get("drum" + i + "voice"));
                return new byte[0];
                }
            
            data[pos++] = (byte)model.get("drum" + i + "volume");
            data[pos++] = (byte)((model.get("drum" + i + "pan") << 4) | 
                (model.get("drum" + i + "output") << 3) |
                bank);
            data[pos++] = (byte)number;
            data[pos++] = (byte)model.get("drum" + i + "fine");
            data[pos++] = (byte)((model.get("drum" + i + "groups") << 3) | 
                model.get("drum" + i + "coarse"));
            }
            
        // compute checksum
        int checksum = 0;
        for(int i = 7; i < data.length - 2; i++)
            {
            checksum += data[i];
            }
        checksum = checksum & 127;
        data[data.length - 2] = (byte)checksum;
        data[data.length - 1] = (byte)0xF7;

        return data;
        }


    public byte[] requestDump(Model tempModel)
        {
        if (tempModel == null)
            tempModel = getModel();
            
        byte NN = (byte)(tempModel.get("number") + 32 + 64);
        byte TYPE = (byte)(isDM5() ? 0x13 : 0x06);
        return new byte[] { (byte)0xF0, 0x00, 0x00, 0x0E, TYPE, (byte)getChannelOut(), NN, (byte)0xF7 };
        }
    
    public byte[] requestCurrentDump()
        {
        byte TYPE = (byte)(isDM5() ? 0x13 : 0x06);
        return new byte[] { (byte)0xF0, 0x00, 0x00, 0x0E, TYPE, (byte)getChannelOut(), 65, (byte)0xF7 };
        }
    

    public String getPatchName(Model model) 
        {
        return model.get("name", "Untitled");
        }

    public String getPatchLocationName(Model model)
        {
        int num = model.get("number", 0);
        if (num < 10) return "0" + num;
        else return "" + num;
        }
    
    public Model getNextPatchLocation(Model model)
        {
        int number = model.get("number");
        
        number++;
        if (number >= 21)
            {
            number = 0;
            }
                
        Model newModel = buildModel();
        newModel.set("number", number);
        return newModel;
        }

    public int getPauseAfterChangePatch() { return 200; }
    public int getPauseAfterWritePatch() { return 2000; }
                
    public void changePatch(Model tempModel) 
        {
        tryToSendMIDI(buildPC(getChannelOut(), tempModel.get("number")));
        }

    public static final String[] D4_DRUMS = new String[]
    {
    // I note that D4 drums start with 1, whereas DM5 drums start with 0
    "K 1 Big \"O\"",
    "K 2 Stomp",
    "K 3 Industry",
    "K 4 Spiked",
    "K 5 Spike RM",
    "K 6 Spike HL",
    "K 7 Pillow",
    "K 8 Plated",
    "K 9 Hi Foot",
    "K 10 Hi Foot RM",
    "K 11 Foot",
    "K 12 Foot RM",
    "K 13 Foot HL",
    "K 14 Foot GT",
    "K 15 6O's R&B",
    "K 16 R&B Room",
    "K 17 Monster",
    "K 18 Mnstr RM",
    "K 19 Gtd Mnstr",
    "K 20 Dbl Head",
    "K 21 Dbl RM",
    "K 22 Dbl HL",
    "K 23 Thrash",
    "K 24 Stab",
    "K 25 Brt Stab",
    "K 26 Stab RM",
    "K 27 Stab HL",
    "K 28 Reggae",
    "K 29 Kinetic",
    "K 30 C&W #1",
    "K 31 22\" Power",
    "K 32 Amb 22 Pwr",
    "K 33 22\" Pwr RM",
    "K 34 Solid!!",
    "K 95 Solid RM",
    "K 36 Tiled",
    "K 37 Tiled RM",
    "K 38 Chunky",
    "K 39 Chunky RM",
    "K 40 Chunky HL",
    "K 41 Loose One",
    "K 42 Amb Loose",
    "K 43 Slammin'",
    "K 44 Slam Room",
    "K 45 22\" Deep",
    "K 46 Amb Deep",
    "K 47 Deep Room",
    "K 48 Movin' Air",
    "K 49 Mstr Mash",
    "K 50 Swift",
    "K 51 Abrupt",
    "K 52 Fusion",
    "K 53 Muffled",
    "K 54 Blaster",
    "K 55 24\" Power",
    "K 56 Amb 24\"",
    "K 57 24\" Room",
    "K 58 Deep 24\"",
    "K 59 Ballad",
    "K 60 Big Foot",
    "K 61 8 Foot RM",
    "K 62 The Thud",
    "K 63 Amb Thud",
    "K 64 Thud Hall",
    "K 65 C&W #2",
    "K 66 Tite",
    "K 67 Tite Room",
    "K 68 Tite Hall",
    "K 69 Chopped",
    "K 70 Flanged",
    "K 71 Deep Wood",
    "K 72 Wood Room",
    "K 73 Head Punch",
    "K 74 Amb Punch",
    "K 75 Garage",
    "K 76 Studio",
    "K 77 Hanger",
    "K 78 Lo Wood",
    "K 79 Lo Wood HL",
    "K 80 Rap",
    "K 81 Rumble",
    "K 82 DDL Slap",
    "K 83 Trendy",
    "K 84 Elec..",
    "K 85 ..tronic",
    "K 86 Ambnt Elc.",
    "K 87 Amb Tronic",
    "K 88 Faazed",
    "K 89 Faazed RM",
    "K 90 Tite Head",
    "K 91 Hi Wisp",
    "K 92 LoWisp",
    "K 93 Hi Wet Wisp",
    "K 94 Lo Wet Wisp",
    "K 95 Techno",
    "K 96 Techno RM",
    "K 97 Lo Techno",
    "K 98 Lo Tech RM",
    "K 99 Coliseum",
    "S 1 Raw Hide",
    "S 2 Fat City",
    "S 3 Ambient",
    "S 4 Torqued",
    "S 5 Classic",
    "S 6 Classic RM",
    "S 7 Hi Class",
    "S 8 Hi Class RM",
    "S 9 MIT Gate",
    "S 10 Dry Combo",
    "S 11 Combo RM",
    "S 12 Combo Plate",
    "S 13 Flange",
    "S 14 Media Hype",
    "S 15 Rim->Center",
    "S 16 Center->Rim",
    "S 17 Squwank",
    "S 18 Standard",
    "S 19 Brighter",
    "S 20 Darker",
    "S 21 Hi Gated",
    "S 22 Lo Gated",
    "S 23 Deep Dry",
    "S 24 Nasty",
    "S 25 Piccolo",
    "S 26 Wet Piccolo",
    "S 27 Hi Piccolo",
    "S 28 Amb Hi Picc",
    "S 29 Aerolux",
    "S 30 Wood",
    "S 31 Wood Room",
    "S 32 Wood Shed",
    "S 33 Rap",
    "S 34 Lo Rap",
    "S 35 Flanger",
    "S 36 Cracker Box",
    "S 37 Bigger Box",
    "S 38 Art O Fish",
    "S 39 Air Burst",
    "S 40 Edged In",
    "S 41 Edged Out",
    "S 42 ..Wet 40",
    "S 43 ..Wet 41",
    "S 44 Compressed",
    "S 45 W/Verb",
    "S 46 Wrap",
    "S 47 Attak Rap",
    "S 48 Brassy",
    "S 49 Brassy RM",
    "S 50 Brutal",
    "S 51 Lo Brutal",
    "S 52 Crisp Hit",
    "S 53 Crisp RM",
    "S 54 Pop 'n Pic",
    "S 55 Alloy",
    "S 56 Aggressive",
    "S 57 WetAggrssv",
    "S 58 Clik Clak",
    "S 59 Chrome",
    "S 60 Reverb",
    "S 61 Media",
    "S 62 Overtone",
    "S 63 Overtone RM",
    "S 64 Verbose",
    "S 65 Dance!",
    "S 66 Grrrated",
    "S 67 Dry Punch",
    "S 68 Amb Punch",
    "S 69 Spiked..",
    "S 70 Bad Punch",
    "S 71 Pop Shot",
    "S 72 Lo Pop Shot",
    "S 73 Flng Shot",
    "S 74 Studio A",
    "S 75 Studio B",
    "S 76 Hyper Pic",
    "S 77 Hi Elect",
    "S 78 Hi Wet Lct",
    "S 79 Electric",
    "S 80 Wet Electric",
    "S 81 Bitchin'",
    "S 82 Fringe",
    "S 83 Trbo Drive",
    "S 84 Play Room",
    "S 85 Slap It!",
    "S 86 Un Natural",
    "S 87 Arena",
    "S 88 Brush",
    "S 89 Brush Hit",
    "S 90 Tote Stik",
    "S 91 Stick",
    "S 92 Big Stik",
    "S 93 Gated",
    "S 94 Rim Shot",
    "S 95 R-Shot RM",
    "S 96 Gunner",
    "S 97 Sm Ballad",
    "S 98 W/Tmbrne",
    "S 99 Bg Ballad",
    "C 1 R&B Hat",
    "C 2 14\" Thin",
    "C 3 Dyno Edge",
    "C 4 14\" Med",
    "C 5 14\" Tite",
    "C 6 Power Hat",
    "C 7 Rock Tite",
    "C 8 Rock Edge",
    "C 9 Tension",
    "C 10 Jazzed!",
    "C 11 Hard Hat",
    "C 12 Rock Tip",
    "C 13 Sputt..",
    "C 14 Ambient",
    "C 15 Wet Hat",
    "C 16 Wetter..",
    "C 17 Half Open",
    "C 18 Wet Half",
    "C 19 Rock Half",
    "C 20 Clutched",
    "C 21 The Edge",
    "C 22 Trashy",
    "C 23 Open It Up",
    "C 24 Rattle",
    "C 25 Wet Rattle",
    "C 26 Close->Open",
    "C 27 Open->Close",
    "C 28 Hard Foot",
    "C 29 Soft Foot",
    "C 30 Wet Foot",
    "C 31 Edge->Bell",
    "C 32 Bell->Edge",
    "C 33 The Swing",
    "C 34 Flat Ride",
    "C 35 60's Flng",
    "C 36 Dark Ride",
    "C 37 Ping Ride",
    "C 38 Bell Ride",
    "C 39 Flng Jazz",
    "C 40 Flng Rock",
    "C 41 Hi Crash",
    "C 42 Lo Crash",
    "C 43 10\" Splash",
    "C 44 Medium",
    "C 45 Lo Medium",
    "C 46 20\" China",
    "C 47 18\" Crash",
    "C 48 Tiny",
    "C 49 20\" Bronze",
    "C 50 Flng Crsh",
    "C 51 Lo Pang",
    "C 52 Slo Crash",
    "C 53 Flng Pang",
    "C 54 Lft & Rght",
    "C 55 Big L&R",
    "T 1 Hi Power",
    "T 2 Med Power",
    "T 3 Low Power",
    "T 4 Pwr Floor",
    "T 5 Hi Thrash",
    "T 6 Md Thrash",
    "T 7 Low Thrash",
    "T 8 Hi Slam",
    "T 9 Low Slam",
    "T 10 Hi Slam RM",
    "T 11 Lo Slam RM",
    "T 12 Hi Studio",
    "T 13 Md Studio",
    "T 14 Low Studio",
    "T 15 Flr Studio",
    "T 16 Low Flr Std",
    "T 17 Ambnt Hi",
    "T 18 Ambnt Md",
    "T 19 Ambnt Low",
    "T 20 Ambnt Flr",
    "T 21 Hi Wet",
    "T 22 Mid Wet",
    "T 23 Low Wet",
    "T 24 Floor Wet",
    "T 25 Low Flr Wet",
    "T 26 Hi Blade",
    "T 27 Md Blade",
    "T 28 Low Blade",
    "T 29 Hi Stereo",
    "T 30 Md Stereo",
    "T 31 Low Stereo",
    "T 32 Hi Return",
    "T 33 Md Return",
    "T 34 Low Return",
    "T 35 10\" Stark",
    "T 36 12\" Stark",
    "T 37 14\" Stark",
    "T 38 16\" Stark",
    "T 39 Hi Cannon",
    "T 40 Md Cannon",
    "T 41 Low Cannon",
    "T 42 XLow Cannon",
    "T 43 Hi Cannon HL",
    "T 44 Md Cann HL",
    "T 45 Low Cann HL",
    "T 46 XLo Cann HL",
    "T 47 Hi Dbl",
    "T 48 Md Dbl",
    "T 49 Low Dbl",
    "T 50 Hi Dbl RM",
    "T 51 Md Dbl RM",
    "T 52 Low Dbl RM",
    "T 53 Hi Clear",
    "T 54 Md Clear",
    "T 55 Low Clear",
    "T 56 Amb Hi Clear",
    "T 57 Amb Md Clr",
    "T 58 Amb La Clr",
    "T 59 Hi Clr Wet",
    "T 60 Md Clr Wet",
    "T 61 Low Clr Wet",
    "T 62 Ol' Hex 1",
    "T 63 Ol' Hex 2",
    "T 64 Ol' Hex 3",
    "T 65 Wet Hex 1",
    "T 66 Wet Hex 2",
    "T 67 Wet Hex 3",
    "T 68 Hi Dynamic",
    "T 69 Md Dynamic",
    "T 70 Low Dynamic",
    "T 71 Hi D Amb",
    "T 72 Md D Amb",
    "T 73 Low D Amb",
    "T 74 Hi Plate",
    "T 75 Md Plate",
    "T 76 Low Plate",
    "T 77 Hi Media",
    "T 78 Md Media",
    "T 79 Low Media",
    "T 80 Hi Flange",
    "T 81 Mid Flange",
    "T 82 Low Flange",
    "T B3 Hi Aggrssv",
    "T 84 Md Aggrssv",
    "T 85 Low Aggrssv",
    "T 86 Hi Ring",
    "T 87 Low Ring",
    "T 88 Hi Ring RM",
    "T 89 Low Ring RM",
    "T 90 Hi Phase",
    "T 91 Md Phase",
    "T 92 Low Phase",
    "P 1 Talk Up",
    "P 2 Talk Down",
    "P 3 Squeezed",
    "P 4 Released",
    "P 5 Shaker",
    "P 6 Hi Timble",
    "P 7 Mid Timble",
    "P 8 Low Timble",
    "P 9 Hi W/Verb",
    "P 10 Mid W/Verb",
    "P 11 Low W/Verb",
    "P 12 Conga",
    "P 13 Low Conga",
    "P 14 Hi Open",
    "P 15 Low Open",
    "P 16 Conga Slap",
    "P 17 Low Slap",
    "P 18 Dynamic A",
    "P 19 Dynamic B",
    "P 20 Dynamic C",
    "P 21 Hi Vibra",
    "P 22 Low Vibra",
    "P 23 Hi Bongo",
    "P 24 Low Bongo",
    "P 25 Hi Cow",
    "P 26 Med Cow",
    "P 27 Low Cow",
    "P 28 Heifer",
    "P 29 Guernsey",
    "P 30 Holstein",
    "P 31 Torpedo",
    "P 32 Low Torpedo",
    "P 33 Hi Agogo",
    "P 34 Low Agogo",
    "P 35 Hi Muted",
    "P 36 Med Muted",
    "P 37 Low Muted",
    "P 38 Hi Wood",
    "P 39 Med Wood",
    "P 40 Lo Wood",
    "P 41 Hi Block",
    "P 42 Med Block",
    "P 43 Low Block",
    "P 44 Hi Folley",
    "P 45 Med Folley",
    "P 46 Low Folley",
    "P 47 Hi Synth",
    "P 48 Mid Synth",
    "P 49 Low Synth",
    "P 50 Flg Synth",
    "P 51 Cabasa",
    "P 52 Fast Cabasa",
    "P 53 Long Cabasa",
    "P 54 Marabasa",
    "P 55 Tambrine",
    "P 56 Dark Tambrn",
    "P 57 Hard Tambrn",
    "P 58 Hi Sticks",
    "P 59 Med Sticks",
    "P 60 Low Sticks",
    "P 61 Finger Snaps",
    "P 62 Power Snap",
    "P 63 Wide Snap",
    "P 64 Hand Clap",
    "P 65 Gated Claps",
    "P 66 Hi Clave",
    "P 67 Lo Clave",
    "P 68 Triangle",
    "P 69 Dinner Bell",
    "P 70 Maracas",
    "P 71 Low Maracas",
    "P 72 Fast Maracas",
    "P 73 Far East",
    "P 74 Far West",
    "P 75 Odd Shake",
    "P 76 Bead Bag",
    "E 1 Gut Wrench",
    "E 2 Xylimbal",
    "E 3 Xylimbal 2",
    "E 4 Xylimbal 3",
    "E 5 Layr Bell",
    "E 6 Hi Lip Pop",
    "E 7 Lip Pop",
    "E 8 Loose Lip",
    "E 9 Door Slam",
    "E 10 Puh!",
    "E 11 Puh-tooy",
    "E 12 Scrape It",
    "E 13 Broken",
    "E 14 Scratch",
    "E 15 Cat Scratch",
    "E 16 Slow Scratch",
    "E 17 Un Bottled",
    "E 18 Air Wrench",
    "E 19 Trq Wrench",
    "E 20 Pwer Wrnch",
    "E 21 Fat Frog",
    "E 22 Anvil",
    "E 23 Trash Lid",
    "E 24 Trash Can",
    "E 25 Dumpster",
    "E 26 Firecracker",
    "E 27 China Break",
    "E 28 Glass Break",
    "E 29 Window Brk",
    "E 30 Chopstix",
    "E 31 Bottle",
    "E 32 Low Bottle",
    "E 33 Jug",
    "E 34 STorpedo",
    "E 35 Hi Whip",
    "E 36 Low Whip",
    "E 37 Whippit",
    "E 38 Tomb Slam",
    "E 39 Hollow 1",
    "E 40 Hollow 2",
    "E 41 Hollow 3",
    "E 42 Hollow 4",
    "E 43 Hollow 5",
    "E 44 Hollow 6",
    "E 45 Hollow 7",
    "E 46 Hollow 8",
    "E 47 Blip",
    "E 48 Big Blip",
    "E 49 Sour Milk",
    "E 50 Hi Thang",
    "E 51 Thang",
    "E 52 Low Thang",
    "E 53 A Squib?",
    "E 54 A Squab?",
    "E 55 Hi Pipe",
    "E 56 Mid Pipe",
    "E 57 Low Pipe",
    "E 58 Hi Ethnic",
    "E 59 Med Ethnic",
    "E 60 Low Ethnic",
    "E 61 Bent Bongo",
    "E 62 Re-Bent",
    "E 63 Hi Filter",
    "E 64 Low Filter",
    "E 65 Ratl Boom",
    "E 66 Face Slap",
    "E 67 Heavy Metal",
    "E 68 Lite Metal",
    "E 69 Clatter",
    "E 70 Bamboo",
    "E 71 Bamb Cmbo",
    "E 72 Digital",
    "E 73 Tamboo",
    "E 74 Schizoid",
    "E 75 Thunder",
    "E 76 Analouge",
    "E 77 Re-Synth",
    "E 78 L To R",
    "E 79 Saucers?",
    "E 80 Silence"
    };


    public static final String[] DM5_DRUMS = new String[]
    {
    // I note that D4 drums start with 1, whereas DM5 drums start with 0
    "K 0 Arena",
    "K 1 Producer",
    "K 2 Pwr Rock",
    "K 3 Fat Head",
    "K 4 Dark Fat",
    "K 5 Passion",
    "K 6 Holo",
    "K 7 WarmKick",
    "K 8 SpeedMtl",
    "K 9 Plastine",
    "K 10 Back Mic",
    "K 11 FrontMic",
    "K 12 Lite",
    "K 13 RubbrBtr",
    "K 14 Simple",
    "K 15 Basic",
    "K 16 Slammin'",
    "K 17 Foot",
    "K 18 Bch Ball",
    "K 19 LowSolid",
    "K 20 Feels Gd",
    "K 21 Pillow",
    "K 22 Fusion",
    "K 23 Reggae",
    "K 24 Kinetica",
    "K 25 Brt Ambi",
    "K 26 Hi Gate",
    "K 27 Med Room",
    "K 28 Lrg Room",
    "K 29 Forum",
    "K 30 Punchy",
    "K 31 InTheKik",
    "K 32 Big One",
    "K 33 Bonk",
    "K 34 RockClub",
    "K 35 MyTribe",
    "K 36 RoundAmb",
    "K 37 RoundAtk",
    "K 38 HardAttk",
    "K 39 Blitz",
    "K 40 9oh9Kik1",
    "K 41 9oh9Kik2",
    "K 42 9oh9Kik3",
    "K 43 Native",
    "K 44 AnaKick",
    "K 45 Mangler",
    "K 46 SuprRave",
    "K 47 Spud",
    "K 48 Rap Wave",
    "K 49 Beat Box",
    "K 50 WeR Borg",
    "K 51 Indscpln",
    "K 52 SonarWav",
    "K 53 60Cycles",
    "K 54 Motor",
    "K 55 Stages",
    "K 56 Cybrwave",
    "K 57 Cybo",
    "K 58 BrainEtr",
    "K 59 Squish",
    "K 60 Crunch",
    "K 61 Thump",
    "K 62 CrnchHed",
    "K 63 CrnchFlp",
    "K 64 Pwr Down",
    "K 65 Hardware",
    "K 66 JunkDrwr",
    "K 67 Junk Man",
    "K 68 LooseLug",
    "K 69 Carpet",
    "K 70 Smoke",
    "K 71 Aggresor",
    "K 72 BadBreth",
    "K 73 King",
    "K 74 Xpando",
    "K 75 Deep IIx",
    "K 76 Dry IIx",
    "K 77 Hex Kick",
    "K 78 Fat Boy",
    "K 79 Techtik",
    "K 80 Skool",
    "K 81 KidStuff",
    "K 82 Scratchr",
    "K 83 Afro",
    "K 84 Cuban",
    "K 85 Tribal",
    "K 86 Steak",
    "K 87 Hazey",
    "K 88 Koosh",
    "K 89 Bowels",
    "K 90 Obergeil",
    "K 91 HiEnergy",
    "K 92 Undrwrld",
    "K 93 Cruiser",
    "K 94 Plumbing",
    "S 0 Get Real",
    "S 1 Big Rim",
    "S 2 Woodclif",
    "S 3 Hip Hop",
    "S 4 Heartlnd",
    "S 5 PwrBalld",
    "S 6 Session",
    "S 7 Funky",
    "S 8 Choked",
    "S 9 Crome",
    "S 10 ChromRng",
    "S 11 ChromeHi",
    "S 12 Beauty",
    "S 13 Piccolo",
    "S 14 Fat Picc",
    "S 15 Hi Ambi",
    "S 16 MicroPic",
    "S 17 PiccRoom",
    "S 18 Low Picc",
    "S 19 NicePicc",
    "S 20 Gun Picc",
    "S 21 Dyn Picc",
    "S 22 Velo>Rim",
    "S 23 Tiny E",
    "S 24 Crisp",
    "S 25 Clean",
    "S 26 Cadence",
    "S 27 DryShell",
    "S 28 TopBrass",
    "S 29 UltraThn",
    "S 30 Kamko",
    "S 31 Hawaii",
    "S 32 BluSprkl",
    "S 33 Bronze",
    "S 34 Hard Rim",
    "S 35 Vintage",
    "S 36 Weasel",
    "S 37 WetWeasl",
    "S 38 Has Edge",
    "S 39 WithClap",
    "S 40 Raunchy",
    "S 41 DeepRoom",
    "S 42 SlapRoom",
    "S 43 WarmRoom",
    "S 44 AnaKick",
    "S 45 LongTail",
    "S 46 ExtraLrg",
    "S 47 Big Hall",
    "S 48 BigPlate",
    "S 49 Compresd",
    "S 50 Solar",
    "S 51 Far Away",
    "S 52 Postmdrn",
    "S 53 Loose",
    "S 54 Grinder",
    "S 55 Freaky",
    "S 56 Woody",
    "S 57 ThinSkin",
    "S 58 Crank It",
    "S 59 Snareo",
    "S 60 TightLug",
    "S 61 Ibid",
    "S 62 Beefrank",
    "S 63 SlowFunk",
    "S 64 Low Ring",
    "S 65 FreakRim",
    "S 66 MetlHarm",
    "S 67 Groovy",
    "S 68 Splat",
    "S 69 RatlWood",
    "S 70 Trashier",
    "S 71 8oh8 Snr",
    "S 72 8oh8 Rim",
    "S 73 8oh8 Tin",
    "S 74 Krafty",
    "S 75 MetlPipe",
    "S 76 9oh9 Snr",
    "S 77 9oh9 Rim",
    "S 78 Release",
    "S 79 City",
    "S 80 U Bahn",
    "S 81 Gritty",
    "S 82 Fat Grit",
    "S 83 Rank",
    "S 84 BrikHaus",
    "S 85 Overtone",
    "S 86 DingoBoy",
    "S 87 Wonk",
    "S 88 HexSnare",
    "S 89 IIxSnare",
    "S 90 70'sFunk",
    "S 91 Ol Skool",
    "S 92 Stutter",
    "S 93 ThikGate",
    "S 94 MetalGat",
    "S 95 Face Beat",
    "S 96 Thrasher",
    "S 97 Shred",
    "S 98 Pipe Bomb",
    "S 99 Clanker",
    "S 100 Blast",
    "S 101 Assault",
    "S 102 Speck",
    "S 103 Spectral",
    "S 104 OrchRoom",
    "S 105 OrchHall",
    "S 106 OrchRoll",
    "S 107 BrushFat",
    "S 108 BrushThn",
    "S 109 BrushRim",
    "S 110 Jazz Hit",
    "S 111 Stik>Snr",
    "S 112 DryStick",
    "S 113 LiveStik",
    "S 114 DeepStik",
    "S 115 StikRoom",
    "S 116 AmbiStik",
    "T 0 Hero Hi",
    "T 1 Hero Mid",
    "T 2 Hero Low",
    "T 3 Hero Flr",
    "T 4 Open Hi",
    "T 5 Open Mid",
    "T 6 Open Low",
    "T 7 PinstrpH",
    "T 8 PinstrpM",
    "T 9 PinstrpL",
    "T 10 StudioHi",
    "T 11 StudioMd",
    "T 12 StudioLo",
    "T 13 Big O Hi",
    "T 14 Big O Lo",
    "T 15 Girth Hi",
    "T 16 Girth Lo",
    "T 17 InsideHi",
    "T 18 InsideMd",
    "T 19 InsideLo",
    "T 20 Jazz Hi",
    "T 21 Jazz Low",
    "T 22 Hall Hi",
    "T 23 Hall Mid",
    "T 24 Hall Low",
    "T 25 Hall Flr",
    "T 26 Psilo Hi",
    "T 27 PsiloMid",
    "T 28 PsiloLow",
    "T 29 PsiloFlr",
    "T 30 CannonHi",
    "T 31 CannonMd",
    "T 32 CannonLo",
    "T 33 CannonFl",
    "T 34 CanFlngH",
    "T 35 CanFlngM",
    "T 36 CanFlngL",
    "T 37 Ballo Hi",
    "T 38 BalloLow",
    "T 39 MakRakHi",
    "T 40 MakRakMd",
    "T 41 MakRakLo",
    "T 42 MakRakFl",
    "T 43 Omega Hi",
    "T 44 Omega Md",
    "T 45 Omega Lo",
    "T 46 Omega Fl",
    "T 47 Salvo Hi",
    "T 48 Salvo Md",
    "T 49 Salvo Lo",
    "T 50 Hex Hi",
    "T 51 Hex Mid",
    "T 52 Hex Low",
    "T 53 HexFloor",
    "T 54 ClascHex",
    "T 55 Noise Hi",
    "T 56 Noise Lo",
    "T 57 Exo Hi",
    "T 58 Exo Mid",
    "T 59 Exo Low",
    "T 60 OilCanHi",
    "T 61 OilCanLo",
    "T 62 8oh8 Hi",
    "T 63 8oh8 Mid",
    "T 64 8oh8 Low",
    "T 65 Bit TomH",
    "T 66 Bit TomL",
    "T 67 BombTomH",
    "T 68 BombTomM",
    "T 69 BombTomL",
    "T 70 Mad Roto",
    "H 0 BrtTite1",
    "H 1 BrtTite2",
    "H 2 Brt Clsd",
    "H 3 Brt Half",
    "H 4 BrtLoose",
    "H 5 BrtLoosr",
    "H 6 DynBrt 1",
    "H 7 DynBrt 2",
    "H 8 Brt Open",
    "H 9 Brt Foot",
    "H 10 SR Clsd",
    "H 11 SR Half",
    "H 12 SR Open",
    "H 13 LiteClsd",
    "H 14 Lite Dyn",
    "H 15 LiteHalf",
    "H 16 LiteOpen",
    "H 17 FlngClsd",
    "H 18 FlngHalf",
    "H 19 FlngOpen",
    "H 20 Rok Clsd",
    "H 21 RokLoose",
    "H 22 RokSlosh",
    "H 23 Rok Open",
    "H 24 Rok Foot",
    "H 25 8oh8Clsd",
    "H 26 8oh8Open",
    "H 27 Rap Clsd",
    "H 28 Rap Half",
    "H 29 Rap Open",
    "H 30 Zip Clsd",
    "H 31 Zip Open",
    "H 32 Zap Clsd",
    "H 33 Zap Open",
    "C 0 Ride Cym",
    "C 1 VeloRide",
    "C 2 PingRide",
    "C 3 Exotic",
    "C 4 RideBell",
    "C 5 TransBel",
    "C 6 El Bell",
    "C 7 Avantia",
    "C 8 CymParts",
    "C 9 BrtCrash",
    "C 10 Ster Brt",
    "C 11 DrkCrash",
    "C 12 SterDark",
    "C 13 LR Crsh1",
    "C 14 LR Crsh2",
    "C 15 IceCrash",
    "C 16 ZootMute",
    "C 17 DrtyMute",
    "C 18 Splash",
    "C 19 MicroCym",
    "C 20 8 Splash",
    "C 21 China",
    "C 22 SterChna",
    "C 23 Woo Han",
    "C 24 Doppler",
    "C 25 TipShank",
    "C 26 SterPhaz",
    "C 27 Hammered",
    "C 28 EastWest",
    "C 29 Orch Cym",
    "C 30 8oh8Crsh",
    "C 31 8CrashFl",
    "C 32 Syn Pang",
    "C 33 SynCrash",
    "C 34 BlastCym",
    "C 35 Noiz Cym",
    "P 0 Agogo Hi",
    "P 1 Agogo Lo",
    "P 2 AgoPitch",
    "P 3 Noggin",
    "P 4 Reco Hi",
    "P 5 Reco Lo",
    "P 6 Clay Pot",
    "P 7 Triangle",
    "P 8 Tri Mute",
    "P 9 TriPitch",
    "P 10 DrumStix",
    "P 11 Cowbell",
    "P 12 Tambrine",
    "P 13 TamPitch",
    "P 14 Sleighbl",
    "P 15 Snowjob",
    "P 16 Cabasa",
    "P 17 SharpShk",
    "P 18 TikTak",
    "P 19 Maracas",
    "P 20 ShakerHi",
    "P 21 ShakerLo",
    "P 22 Bead Pot",
    "P 23 BeadShk1",
    "P 24 BeadShk2",
    "P 25 BeadShk3",
    "P 26 SynShkr1",
    "P 27 SynShkr2",
    "P 28 SynShkrD",
    "P 29 Rattle",
    "P 30 CrashrHd",
    "P 31 CrashrSf",
    "P 32 Rainshak",
    "P 33 RainStik",
    "P 34 Gravel",
    "P 35 RatlBwap",
    "P 36 Bongo Hi",
    "P 37 BngHiSlp",
    "P 38 Bongo Lo",
    "P 39 BngLoSlp",
    "P 40 Conga Hi",
    "P 41 Conga Lo",
    "P 42 CongaSlp",
    "P 43 Slap Dyn",
    "P 44 Screech",
    "P 45 Cuica Hi",
    "P 46 Cuica Lo",
    "P 47 AmIndian",
    "P 48 Tatonka",
    "P 49 WarPaint",
    "P 50 BoLanGoo",
    "P 51 BoLanDyn",
    "P 52 BreketaH",
    "P 53 BreketaL",
    "P 54 BrktaDyn",
    "P 55 Elephant",
    "P 56 GhatamHi",
    "P 57 GhatamLo",
    "P 58 Udu",
    "P 59 Ethnika",
    "P 60 Amazon",
    "P 61 Nagara",
    "P 62 Oobla Hi",
    "P 63 Oobla Lo",
    "P 64 OoblaDyn",
    "P 65 Paah",
    "P 66 Ethno",
    "P 67 EasternV",
    "P 68 TalkngHi",
    "P 69 TalkngLo",
    "P 70 HandDrum",
    "P 71 Tavil Hi",
    "P 72 Tavil Lo",
    "P 73 Monastic",
    "P 74 Tavasa",
    "P 75 Tabla",
    "P 76 TblaDyn1",
    "P 77 TblaDyn2",
    "P 78 Ghatabla",
    "P 79 Tablchrd",
    "P 80 Haji",
    "P 81 TimbleHi",
    "P 82 TimbleLo",
    "P 83 8cwPitch",
    "P 84 8oh8 Cow",
    "P 85 8oh8 Rim",
    "P 86 CongaRap",
    "P 87 8oh8Clap",
    "P 88 9oh9Clap",
    "P 89 Big Clap",
    "P 90 LiteSnap",
    "P 91 ClscSnap",
    "P 92 Pwr Snap",
    "P 93 Clave",
    "P 94 ClveKord",
    "P 95 Castanet",
    "P 96 CastRoll",
    "P 97 CastDyn1",
    "P 98 CastDyn2",
    "P 99 Wood Hi",
    "P 100 Wood Lo",
    "P 101 Block Hi",
    "P 102 Block Lo",
    "P 103 TempleHi",
    "P 104 TempleLo",
    "P 105 Vibrslap",
    "P 106 Oil Can",
    "P 107 OilPitch",
    "P 108 MetalTik",
    "P 109 Plucky",
    "P 110 PopCheek",
    "P 111 Rappotab",
    "P 112 I'm Clay",
    "P 113 BigoBrek",
    "P 114 SpacePrc",
    "E 0 Anvil",
    "E 1 BallPeen",
    "E 2 BattyBel",
    "E 3 4 Star",
    "E 4 Blksmith",
    "E 5 Clank",
    "E 6 Tank Hit",
    "E 7 SunBurst",
    "E 8 Industry",
    "E 9 Big Shot",
    "E 10 Metal",
    "E 11 WhtNoiz1",
    "E 12 WhtNoiz2",
    "E 13 Spectre1",
    "E 14 Spectre2",
    "E 15 Tesla",
    "E 16 Machine",
    "E 17 PinkZap1",
    "E 18 PinkZap2",
    "E 19 PnkBlst1",
    "E 20 PnkBlst2",
    "E 21 Zap 1",
    "E 22 Zap 2",
    "E 23 Zap 3",
    "E 24 Wood Zap",
    "E 25 Dyn Zap",
    "E 26 Dual Zap",
    "E 27 Residue",
    "E 28 WhipCrak",
    "E 29 Kung Fu",
    "E 30 WhipNoiz",
    "E 31 Vinyl 1",
    "E 32 Vinyl 2",
    "E 33 DynVinyl",
    "E 34 PwrGtrHi",
    "E 35 PwrGtrLo",
    "E 36 Gtr Hit",
    "E 37 FlngGtrH",
    "E 38 FlngGtrL",
    "E 39 Guitrbot",
    "E 40 Slippery",
    "E 41 Danger!",
    "E 42 Screech",
    "E 43 FlScreeH",
    "E 44 FlScreeL",
    "E 45 Mercury",
    "E 46 Technoid",
    "E 47 Bucket",
    "E 48 Grab Bag",
    "E 49 Alloys 1",
    "E 50 Alloys 2",
    "E 51 Velopede",
    "E 52 Static",
    "E 53 Pole",
    "E 54 Froggy",
    "E 55 Sun City",
    "E 56 InduHit",
    "E 57 JetBeads",
    "E 58 Plonk",
    "E 59 Klonk",
    "E 60 Pop",
    "E 61 Knock",
    "E 62 Metronom",
    "E 63 Silence",
    "R 0 BrtHatC1",
    "R 1 BrtHatC2",
    "R 2 RokHatCl",
    "R 3 Real Snr",
    "R 4 LooseSnr",
    "R 5 TinSnare",
    "R 6 ValleySn",
    "R 7 FreakSnr",
    "R 8 Aliens",
    "R 9 Zapalog",
    "R 10 Blasters",
    "R 11 Metalize",
    "R 12 ShknBake",
    "R 13 Triblism",
    "R 14 CngoBngo",
    "R 15 RagaBabl"
    };


    public boolean testVerify(Synth synth2, 
        String key,
        Object obj1, Object obj2) 
        {
        // spaces at end of name are fine
        if (key.equals("name")) return true;
        return false;
        }
        
    /** Return a list of all patch number names.  Default is { "Main" } */
    public String[] getPatchNumberNames()  { return buildIntegerNames(21, 0); }

    /** Return a list whether patches in banks are writeable.  Default is { false } */
    public boolean[] getWriteableBanks() { return new boolean[] { true }; }

    /** Return a list whether individual patches can be written.  Default is FALSE. */
    public boolean getSupportsPatchWrites() { return true; }

    public int getPatchNameLength() { return 15; }

    public int getBatchDownloadWaitTime() { return 1500; }

    public boolean librarianTested() { return true; }
    }
