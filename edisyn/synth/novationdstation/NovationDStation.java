/***
    Copyright 2020 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.novationdstation;

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
   A patch editor for the Novation Drumstation and D Station.
        
   @author Sean Luke
*/

public class NovationDStation extends Synth
    {
    public static final String[] PANS = { "L 4", "L 3", "L 2", "L 1", "--", "R 1", "R 2", "R 3", "R 4", "O 1", "O 2", "O 3", "O 4", "O 5", "O 6" };             // #15 is "O 4" again...
    public static final String[] DRUMS = { "909 Bass Drum", "909 Snare Drum", "909 Low Tom", "909 Mid Tom", "909 High Tom", "909 Rim Shot", "909 Hand Clap", "909 Closed High Hat", "909 Open High Hat", "909 Crash Cymbal", "909 Ride Cymbal", "808 Bass Drum", "808 Snare Drum", "808 Low Tom", "808 Mid Tom", "808 High Tom", "808 Rim Shot", "808 Hand Clap", "808 Cowbell", "808 Closed High Hat", "808 Open High Hat", "808 Crash Cymbal", "808 Low Conga", "808 Mid Conga", "808 High Conga", "808 Maracas", "808 Claves" };
    public static final String[] SETS = { "808", "909" };

    /*
      public static final String[] PROGRAMS = 
      {
      "Classic TR909 V1", "FX TR909/TR808", "Garage 909/808", "Classic TR909 V2",
      "Hardcore TR909", "Rave/Jungle TR808", "Classic TR808 V1", "Tune Me Up",
      "Cut TR909", "Dynamic TR909", "Cut TR808", "Grunge TR909",
      "Dynamic TR808", "Grunge TR808", "Classic TR909 V3", "Power TR909",
      "Power TR808", "A Bit Off TR909", "A Bit Off TR808", "Velo TR909",
      "VeloDis TR909", "VeloTom TR909", "VeloTom TR808", "Classic TR909 Ind.",
      "Classic TR808 Ind."
      };
    */


    public JFrame sprout()
        {
        JFrame frame = super.sprout();
        transmitTo.setEnabled(false);
        writeTo.setEnabled(false);
        receiveCurrent.setEnabled(false);
        receivePatch.setEnabled(false);
        receiveNextPatch.setEnabled(false);
        getAll.setEnabled(false);
        return frame;
        }         

    public NovationDStation()
        {
        // model.set("number", 0);
                
        for(int i = 0; i < ccParameters.length; i++)
            {
            ccParametersToIndex.put(ccParameters[i], Integer.valueOf(i + 20));          // CC values start at 20
            }

        /// SOUND PANEL
                
        JComponent soundPanel = new SynthPanel(this);
        VBox vbox = new VBox();
        
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        hbox.addLast(addBanks(Style.COLOR_A()));
        vbox.add(hbox);
        
        vbox.add(add808("BD", 0 + 11, Style.COLOR_B()));
        vbox.add(add808("SD", 1 + 11, Style.COLOR_A()));
        vbox.add(add808("LT", 2 + 11, Style.COLOR_B()));
        vbox.add(add808("MT", 3 + 11, Style.COLOR_A()));
        vbox.add(add808("HT", 4 + 11, Style.COLOR_B()));
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Global, 808 A", soundPanel);
                

        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        vbox.add(add808("RS", 5 + 11, Style.COLOR_A()));
        vbox.add(add808("CP", 6 + 11, Style.COLOR_B()));
        vbox.add(add808("CB", 7 + 11, Style.COLOR_A()));
        vbox.add(add808("CH", 8 + 11, Style.COLOR_B()));
        vbox.add(add808("OH", 9 + 11, Style.COLOR_A()));
        vbox.add(add808("CC", 10 + 11, Style.COLOR_B()));
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("808 B", soundPanel);
        
        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        vbox.add(add808("LC", 11 + 11, Style.COLOR_B()));
        vbox.add(add808("MC", 12 + 11, Style.COLOR_A()));
        vbox.add(add808("HC", 13 + 11, Style.COLOR_B()));
        vbox.add(add808("MA", 14 + 11, Style.COLOR_A()));
        vbox.add(add808("CL", 15 + 11, Style.COLOR_B()));
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("808 C", soundPanel);
                

        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        vbox.add(add909("BD", 0, Style.COLOR_A()));
        vbox.add(add909("SD", 1, Style.COLOR_B()));
        vbox.add(add909("LT", 2, Style.COLOR_A()));
        vbox.add(add909("MT", 3, Style.COLOR_B()));
        vbox.add(add909("HT", 4, Style.COLOR_A()));
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("909 A", soundPanel);

        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        vbox.add(add909("RS", 5, Style.COLOR_B()));
        vbox.add(add909("CP", 6, Style.COLOR_A()));
        vbox.add(add909("CH", 7, Style.COLOR_B()));
        vbox.add(add909("OH", 8, Style.COLOR_A()));
        vbox.add(add909("CC", 9, Style.COLOR_B()));
        vbox.add(add909("RC", 10, Style.COLOR_A()));
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("909 B", soundPanel);

        loadDefaults();        
        }
                
    public String getDefaultResourceFileName() { return "NovationDStation.init"; }
    public String getHTMLResourceFileName() { return "NovationDStation.html"; }

    /*
      public boolean gatherPatchInfo(String title, Model change, boolean writing)
      {
      int num = model.get("number");
      if (writing && num < 25) 
      num = 25;
      JTextField number = new SelectedTextField("" + num, 2);

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
      if (writing)
      showSimpleError(title, "The Patch Number must be an integer 25...39");
      else
      showSimpleError(title, "The Patch Number must be an integer 1...39");
      continue;
      }
      if (writing && (n < 25 || n > 39))
      {
      showSimpleError(title, "The Patch Number must be an integer 25...39");
      continue;
      }
      else if (!writing && (n < 0 || n > 39))
      {
      showSimpleError(title, "The Patch Number must be an integer 1...39");
      continue;
      }
                                
      change.set("number", n);
      return true;
      }
      }
    */
                                    
    /** Add the global patch category (name, id, number, etc.) */
    public JComponent addNameGlobal(Color color)
        {
        Category globalCategory = new Category(this, getSynthName(), color);
        //globalCategory.makeUnresettable();
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();

        /*                
                          VBox vbox = new VBox();
                          HBox hbox2 = new HBox();
                          comp = new PatchDisplay(this, "number", 4, false);
                          hbox2.add(comp);
                          vbox.add(hbox2);
                          hbox.add(vbox);
        */

        VBox vbox = new VBox();
        params = SETS;
        comp = new Chooser("General MIDI Set", this, "gmset", params);
        vbox.add(comp);
        hbox.add(vbox);

        // Not enough space to show the title
        hbox.addLast(Strut.makeHorizontalStrut(230));

        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }


    public JComponent addBanks( Color color)
        {
        Category category = new Category(this, "Banks", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = DRUMS;
        comp = new Chooser("Bank A", this, "banka", params);
        vbox.add(comp);

        params = DRUMS;
        comp = new Chooser("Bank B", this, "bankb", params);
        vbox.add(comp);
        hbox.add(vbox);

        vbox = new VBox();
        params = DRUMS;
        comp = new Chooser("Bank C", this, "bankc", params);
        vbox.add(comp);

        params = DRUMS;
        comp = new Chooser("Bank D", this, "bankd", params);
        vbox.add(comp);
        hbox.add(vbox);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent add808(String drum, int index, Color color)
        {
        Category category = new Category(this, DRUMS[index], color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();

        CheckBox levelvelocity = new CheckBox("Level Velocity", this, "808" + drum + "levelvelocity");

        if (drum.equals("CC"))
            {        
            vbox.add(Strut.makeStrut(levelvelocity));
            }
        else
            {
            comp = new CheckBox("Tune Velocity", this, "808" + drum + "tunevelocity");
            vbox.add(comp);
            }

        vbox.add(levelvelocity);
        hbox.add(vbox);
        
        vbox = new VBox();

        comp = new CheckBox("Note Off Recognition", this, "808" + drum + "noteoff");
        vbox.add(comp);
        hbox.add(vbox);
        
        if (drum.equals("BD") || drum.equals("LT") || drum.equals("MT") || drum.equals("HT") || drum.equals("CH") || drum.equals("OH") || drum.equals("CC"))
            {
            comp = new CheckBox("Decay Velocity", this, "808" + drum + "decayvelocity");
            vbox.add(comp);
            }
        else if (drum.equals("SD"))
            {
            comp = new CheckBox("Snappy Velocity", this, "808" + drum + "decayvelocity");
            vbox.add(comp);
            }
                
        if (drum.equals("BD") || drum.equals("SD") || drum.equals("CC"))
            {
            comp = new CheckBox("Tone Velocity", this, "808" + drum + "tonevelocity");
            vbox.add(comp);
            }
        hbox.add(vbox);
       
        LabelledDial level = new LabelledDial("Level", this, "808" + drum + "level", color, 0, 127);

        if (drum.equals("CC"))
            { 
            hbox.add(Strut.makeStrut(level));
            }
        else
            {
            comp = new LabelledDial("Tune", this, "808" + drum + "tune", color, 0, 127)
                {
                public boolean isSymmetric() { return true; }
                public String map(int val)
                    {
                    if (val < 64) return "< " + (64 - val);
                    else if (val > 64) return "" + (val - 64) + " >";
                    else return "--";
                    }
                };
            hbox.add(comp);
            }
        
        hbox.add(level);
        
        comp = new LabelledDial("Pan/Out", this, "808" + drum + "pan", color, 0, 14)
            {
            public String map(int val)
                {
                return PANS[val];
                }
            public double getStartAngle()
                {
                return 170;
                }
            public int getDefaultValue() { return 4; }                
            };
        hbox.add(comp);
        
        comp = new LabelledDial("Distortion", this, "808" + drum + "distortion", color, 0, 15);
        hbox.add(comp);
        
        comp = new LabelledDial("Front Cut", this, "808" + drum + "frontcut", color, 0, 99);
        hbox.add(comp);
        
        if (drum.equals("BD") || drum.equals("LT") || drum.equals("MT") || drum.equals("HT") || drum.equals("CH") || drum.equals("OH") || drum.equals("CC"))
            {
            comp = new LabelledDial("Decay", this, "808" + drum + "decay", color, 0, 127)
                {
                public boolean isSymmetric() { return true; }
                public String map(int val)
                    {
                    if (val < 64) return "< " + (64 - val);
                    else if (val > 64) return "" + (val - 64) + " >";
                    else return "--";
                    }
                };
            hbox.add(comp);
            }
        else if (drum.equals("SD"))
            {
            comp = new LabelledDial("Snappy", this, "808" + drum + "decay", color, 0, 127)
                {
                public boolean isSymmetric() { return true; }
                public String map(int val)
                    {
                    if (val < 64) return "< " + (64 - val);
                    else if (val > 64) return "" + (val - 64) + " >";
                    else return "--";
                    }
                };
            hbox.add(comp);
            }
                
        if (drum.equals("BD") || drum.equals("SD") || drum.equals("CC"))
            {
            comp = new LabelledDial("Tone", this, "808" + drum + "tone", color, 0, 127)
                {
                public boolean isSymmetric() { return true; }
                public String map(int val)
                    {
                    if (val < 64) return "< " + (64 - val);
                    else if (val > 64) return "" + (val - 64) + " >";
                    else return "--";
                    }
                };
            hbox.add(comp);
            }
        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
    
    
    
    public JComponent add909(String drum, int index, Color color)
        {
        Category category = new Category(this, DRUMS[index], color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        
        comp = new CheckBox("Tune Velocity", this, "909" + drum + "tunevelocity");
        vbox.add(comp);

        comp = new CheckBox("Level Velocity", this, "909" + drum + "levelvelocity");
        vbox.add(comp);
        hbox.add(vbox);
        
        vbox = new VBox();

        comp = new CheckBox("Note Off Recognition", this, "909" + drum + "noteoff");
        vbox.add(comp);
        hbox.add(vbox);

        if (drum.equals("BD") || drum.equals("LT") || drum.equals("MT") || drum.equals("HT") || drum.equals("CH") || drum.equals("OH") || drum.equals("CC") || drum.equals("RC"))
            {
            comp = new CheckBox("Decay Velocity", this, "909" + drum + "decayvelocity");
            vbox.add(comp);
            }
        else if (drum.equals("SD"))
            {
            comp = new CheckBox("Snappy Velocity", this, "909" + drum + "decayvelocity");
            vbox.add(comp);
            }
                
        if (drum.equals("BD"))
            {
            comp = new CheckBox("Attack Velocity", this, "909" + drum + "tonevelocity");
            vbox.add(comp);
            }
        else if (drum.equals("SD"))
            {
            comp = new CheckBox("Tone Velocity", this, "909" + drum + "tonevelocity");
            vbox.add(comp);
            }
                
        hbox.add(vbox);
        
        comp = new LabelledDial("Tune", this, "909" + drum + "tune", color, 0, 127)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val)
                {
                if (val < 64) return "< " + (64 - val);
                else if (val > 64) return "" + (val - 64) + " >";
                else return "--";
                }
            };
        hbox.add(comp);
        
        comp = new LabelledDial("Level", this, "909" + drum + "level", color, 0, 127);
        hbox.add(comp);
        
        comp = new LabelledDial("Pan/Out", this, "909" + drum + "pan", color, 0, 14)
            {
            public String map(int val)
                {
                return PANS[val];
                }
            public double getStartAngle()
                {
                return 170;
                }
            public int getDefaultValue() { return 4; }                
            };
        hbox.add(comp);
        
        comp = new LabelledDial("Distortion", this, "909" + drum + "distortion", color, 0, 15);
        hbox.add(comp);
        
        comp = new LabelledDial("Front Cut", this, "909" + drum + "frontcut", color, 0, 99);
        hbox.add(comp);
        
        if (drum.equals("BD") || drum.equals("LT") || drum.equals("MT") || drum.equals("HT") || drum.equals("CH") || drum.equals("OH") || drum.equals("CC") || drum.equals("RC"))
            {
            comp = new LabelledDial("Decay", this, "909" + drum + "decay", color, 0, 127)
                {
                public boolean isSymmetric() { return true; }
                public String map(int val)
                    {
                    if (val < 64) return "< " + (64 - val);
                    else if (val > 64) return "" + (val - 64) + " >";
                    else return "--";
                    }
                };
            hbox.add(comp);
            }
        else if (drum.equals("SD"))
            {
            comp = new LabelledDial("Snappy", this, "909" + drum + "decay", color, 0, 127)
                {
                public boolean isSymmetric() { return true; }
                public String map(int val)
                    {
                    if (val < 64) return "< " + (64 - val);
                    else if (val > 64) return "" + (val - 64) + " >";
                    else return "--";
                    }
                };
            hbox.add(comp);
            }
                
        if (drum.equals("BD"))
            {
            comp = new LabelledDial("Attack", this, "909" + drum + "tone", color, 0, 127)
                {
                public boolean isSymmetric() { return true; }
                public String map(int val)
                    {
                    if (val < 64) return "< " + (64 - val);
                    else if (val > 64) return "" + (val - 64) + " >";
                    else return "--";
                    }
                };
            hbox.add(comp);
            }
        else if (drum.equals("SD"))
            {
            comp = new LabelledDial("Tone", this, "909" + drum + "tone", color, 0, 127)
                {
                public boolean isSymmetric() { return true; }
                public String map(int val)
                    {
                    if (val < 64) return "< " + (64 - val);
                    else if (val > 64) return "" + (val - 64) + " >";
                    else return "--";
                    }
                };
            hbox.add(comp);
            }
        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public int emitDrum(byte[] data, int pos, String drum, int bytes)
        {
        // Is this first?
        if (!drum.equals("808CC"))
            data[pos++] = (byte)((model.get(drum + "tunevelocity") << 7) | (model.get(drum + "tune")));
        data[pos++] = (byte)((model.get(drum + "levelvelocity") << 7) | (model.get(drum + "level")));
        if (drum.equals("808CC"))
            data[pos++] = (byte)((model.get(drum + "tonevelocity") << 7) | (model.get(drum + "tone")));
        if (bytes == 6)
            data[pos++] = (byte)((model.get(drum + "tonevelocity") << 7) | (model.get(drum + "tone")));
        if (bytes == 6 || bytes == 5)
            data[pos++] = (byte)((model.get(drum + "decayvelocity") << 7) | (model.get(drum + "decay")));
        data[pos++] = (byte)((model.get(drum + "noteoff") << 7) | (model.get(drum + "frontcut")));
        data[pos++] = (byte)((model.get(drum + "distortion") << 4) | (model.get(drum + "pan")));
        return pos;
        }

    public int parseDrum(byte[] data, int pos, String drum, int bytes)
        {
        int val = 0;
                
        if (!drum.equals("808CC"))
            {
            val = (data[pos++] & 255);
            model.set(drum + "tunevelocity", val >>> 7);
            model.set(drum + "tune", val & 127);
            }
                
        val = (data[pos++] & 255);
        model.set(drum + "levelvelocity", val >>> 7);
        model.set(drum + "level", val & 127);

        if (drum.equals("808CC"))
            {
            val = (data[pos++] & 255);
            model.set(drum + "tonevelocity", val >>> 7);
            model.set(drum + "tone", val & 127);
            }

        if (bytes == 6)
            {
            val = (data[pos++] & 255);
            model.set(drum + "tonevelocity", val >>> 7);
            model.set(drum + "tone", val & 127);
            }
                
        if (bytes == 6 || bytes == 5)
            {
            val = (data[pos++] & 255);
            model.set(drum + "decayvelocity", val >>> 7);
            model.set(drum + "decay", val & 127);
            }

        val = (data[pos++] & 255);
        model.set(drum + "noteoff", val >>> 7);
        model.set(drum + "frontcut", val & 127);

        val = (data[pos++] & 255);
        model.set(drum + "distortion", val >>> 4);
        model.set(drum + "pan", (val & 15) < 15 ? (val & 15) : 13);             // 04 is repeated at slot 15 I believe
                
        return pos;
        }

    public void nybblize(byte[] data, byte[] nybbles, int pos)
        {
        for(int i = 0; i < data.length; i++)
            {
                        
            nybbles[pos + i * 2] = (byte)((data[i] & 255) >>> 4);
            nybbles[pos + i * 2 + 1] = (byte)((data[i] & 255) & 15);
            }
        }

    public byte[] denybblize(byte[] nybbles, int pos, int len)              // len is length of *data*
        {
        byte[] data = new byte[len];
        for(int i = 0; i < data.length; i++)
            {
            data[i] = (byte)((nybbles[pos + i * 2] << 4) | (nybbles[pos + i * 2 + 1]));
            }
        return data;
        }

    public int parse(byte[] data, boolean fromFile)
        {
        if (data[6] == 0x11) 
            {
            String[] names = new String[15];
            for(int i = 0; i < 15; i++)
                names[i] = "" + (i + 25);
                        
            int patchNum = showBankSysexOptions(data, names);
            if (patchNum < 0) return PARSE_CANCELLED;
            else return parseBulk(data, patchNum, fromFile);
            }
        else
            {               
            byte[] d = denybblize(data, 7, 140);
                
            int pos = 0;
            pos = parseDrum(d, pos, "909BD", 6);
            pos = parseDrum(d, pos, "909RS", 4);
            pos = parseDrum(d, pos, "909SD", 6);
            pos = parseDrum(d, pos, "909CP", 4);
            pos = parseDrum(d, pos, "909LT", 5);
            pos = parseDrum(d, pos, "909MT", 5);
            pos = parseDrum(d, pos, "909CH", 5);
            pos = parseDrum(d, pos, "909HT", 5);
            pos = parseDrum(d, pos, "909CC", 5);
            pos = parseDrum(d, pos, "909RC", 5);
            pos = parseDrum(d, pos, "909OH", 5);
            pos = parseDrum(d, pos, "808BD", 6);
            pos = parseDrum(d, pos, "808RS", 4);
            pos = parseDrum(d, pos, "808CP", 4);
            pos = parseDrum(d, pos, "808SD", 6);
            pos = parseDrum(d, pos, "808CH", 5);
            pos = parseDrum(d, pos, "808LT", 5);
            pos = parseDrum(d, pos, "808OH", 5);
            pos = parseDrum(d, pos, "808MT", 5);
            pos = parseDrum(d, pos, "808CC", 5);
            pos = parseDrum(d, pos, "808HT", 5);
            pos = parseDrum(d, pos, "808CB", 4);
            pos = parseDrum(d, pos, "808HC", 4);
            pos = parseDrum(d, pos, "808MC", 4);
            pos = parseDrum(d, pos, "808LC", 4);
            pos = parseDrum(d, pos, "808MA", 4);
            pos = parseDrum(d, pos, "808CL", 4);
            model.set("banka", d[pos++] & 255);
            model.set("bankb", d[pos++] & 255);
            model.set("bankc", d[pos++] & 255);
            model.set("bankd", d[pos++] & 255);
            pos+= 6;                // unknown
            model.set("gmset", d[pos++] & 255);
            return PARSE_SUCCEEDED;
            }
        }

    public JComponent getAdditionalBankSysexOptionsComponents(byte[] data, String[] names)
        {
        int rx = data[4100];    // LSB of RX channel
        int tx = data[4094];    // LSB of TX channel
        // note that these run 1...16, not 0..16.  I don't know why.
        VBox vbox = new VBox();
        vbox.add(Strut.makeVerticalStrut(10));
        vbox.add(new JLabel("Note: Drumstation bank patches also contain RX/TX channel data."));
        vbox.add(new JLabel("Writing the bank will set the synth's MIDI RX channel to " + rx));
        vbox.add(new JLabel("and its MIDI TX channel to " + tx));
        return vbox;
        }
                

    public int parseBulk(byte[] data, int elt, boolean fromFile)
        {
        byte[] d = denybblize(data, 7 + 136 * elt, 136);
                
        int pos = 0;
        pos = parseDrum(d, pos, "909BD", 6);
        pos = parseDrum(d, pos, "909RS", 4);
        pos = parseDrum(d, pos, "909SD", 6);
        pos = parseDrum(d, pos, "909CP", 4);
        pos = parseDrum(d, pos, "909LT", 5);
        pos = parseDrum(d, pos, "909MT", 5);
        pos = parseDrum(d, pos, "909CH", 5);
        pos = parseDrum(d, pos, "909HT", 5);
        pos = parseDrum(d, pos, "909CC", 5);
        pos = parseDrum(d, pos, "909RC", 5);
        pos = parseDrum(d, pos, "909OH", 5);
        pos = parseDrum(d, pos, "808BD", 6);
        pos = parseDrum(d, pos, "808RS", 4);
        pos = parseDrum(d, pos, "808CP", 4);
        pos = parseDrum(d, pos, "808SD", 6);
        pos = parseDrum(d, pos, "808CH", 5);
        pos = parseDrum(d, pos, "808LT", 5);
        pos = parseDrum(d, pos, "808OH", 5);
        pos = parseDrum(d, pos, "808MT", 5);
        pos = parseDrum(d, pos, "808CC", 5);
        pos = parseDrum(d, pos, "808HT", 5);
        pos = parseDrum(d, pos, "808CB", 4);
        pos = parseDrum(d, pos, "808HC", 4);
        pos = parseDrum(d, pos, "808MC", 4);
        pos = parseDrum(d, pos, "808LC", 4);
        pos = parseDrum(d, pos, "808MA", 4);
        pos = parseDrum(d, pos, "808CL", 4);
        model.set("banka", d[pos] & 31);                                                          // [Byte 0 bits 4, 3, 2, 1, 0]   // or byte 2?
        model.set("bankb", ((d[pos + 1] & 3) << 3) | ((d[pos] >>> 5) & 7));                       // [Byte 1 bits 1, 0] [Byte 0 bits 7, 6, 5]   // or byte 2 2, 3, 0?
        model.set("bankc", ((d[pos + 1] << 2) & 31));                                             // [Byte 1 bits 2, 3, 4, 5, 6]
        model.set("bankd", ((d[pos + 3] >>> 4) << 1) | ((d[pos + 1] >>> 7) & 1));                                 // [Byte 3 bits 7, 6, 5, 4] [Byte 1 bit 7]
        pos += 4;               // bank data
        pos += 2;               // unknown
        model.set("gmset", (d[pos++] == 0 ? 0 : 1));             // 808 = 0, 909 = 4
        return PARSE_SUCCEEDED;
        }

    public byte[] emit(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        if (tempModel == null)
            tempModel = getModel();

        byte[] data = new byte[288];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x00;
        data[2] = (byte)0x20;
        data[3] = (byte)0x29;
        data[4] = (byte)0x02;
        data[5] = (byte)0x01;
        data[6] = (byte)0x22;
        data[287] = (byte)0xF7;
        
        byte[] d = new byte[140];
        int pos = 0;
        pos = emitDrum(d, pos, "909BD", 6);
        pos = emitDrum(d, pos, "909RS", 4);
        pos = emitDrum(d, pos, "909SD", 6);
        pos = emitDrum(d, pos, "909CP", 4);
        pos = emitDrum(d, pos, "909LT", 5);
        pos = emitDrum(d, pos, "909MT", 5);
        pos = emitDrum(d, pos, "909CH", 5);
        pos = emitDrum(d, pos, "909HT", 5);
        pos = emitDrum(d, pos, "909CC", 5);
        pos = emitDrum(d, pos, "909RC", 5);
        pos = emitDrum(d, pos, "909OH", 5);
        pos = emitDrum(d, pos, "808BD", 6);
        pos = emitDrum(d, pos, "808RS", 4);
        pos = emitDrum(d, pos, "808CP", 4);
        pos = emitDrum(d, pos, "808SD", 6);
        pos = emitDrum(d, pos, "808CH", 5);
        pos = emitDrum(d, pos, "808LT", 5);
        pos = emitDrum(d, pos, "808OH", 5);
        pos = emitDrum(d, pos, "808MT", 5);
        pos = emitDrum(d, pos, "808CC", 5);
        pos = emitDrum(d, pos, "808HT", 5);
        pos = emitDrum(d, pos, "808CB", 4);
        pos = emitDrum(d, pos, "808HC", 4);
        pos = emitDrum(d, pos, "808MC", 4);
        pos = emitDrum(d, pos, "808LC", 4);
        pos = emitDrum(d, pos, "808MA", 4);
        pos = emitDrum(d, pos, "808CL", 4);
        d[pos++] = (byte)model.get("banka");
        d[pos++] = (byte)model.get("bankb");
        d[pos++] = (byte)model.get("bankc");
        d[pos++] = (byte)model.get("bankd");
        pos+= 6;                // unknown
        d[pos++] = (byte)model.get("gmset");
                
        nybblize(d, data, 7);
                
        return data;
        }

    HashMap ccParametersToIndex = new HashMap();

    public static final String[] ccParameters = new String[] 
    {
    "808BDfrontcut",
    "808BDpan",
    "808BDdistortion",
    "808BDtune",
    "808BDtone",
    "808BDdecay",
    "808SDfrontcut",
    "808SDpan",
    "808SDdistortion",
    "808SDtune",
    "808SDtone",
    "808SDdecay",
    "808LTfrontcut",
    "808LTpan",
    "808LTdistortion",
    "808LTtune",
    "808LTdecay",
    "808MTfrontcut",
    "808MTpan",
    "808MTdistortion",
    "808MTtune",
    "808MTdecay",
    "808HTfrontcut",
    "808HTpan",
    "808HTdistortion",
    "808HTtune",
    "808HTdecay",
    "808RSpan",
    "808RStune",
    "808CPpan",
    "808CPtune",
    "808CBpan",
    "808CBdistortion",
    "808CBtune",
    "808CHpan",
    "808CHtune",
    "808CHdecay",
    "808OHpan",
    "808OHtune",
    "808OHdecay",
    "808CCpan",
    "808CCtone",
    "808CCdecay",
    "808LCpan",
    "808LCdistortion",
    "808LCtune",
    "808MCpan",
    "808MCdistortion",
    "808MCtune",
    "808HCpan",
    "808HCdistortion",
    "808HCtune",
    "808MApan",
    "808MAtune",
    "808CLpan",
    "808CLtune",
    "909BDtune",
    "909BDtone",
    "909BDdecay",
    "909SDtune",
    "909SDtone",
    "909SDdecay",
    "909LTfrontcut",
    "909LTpan",
    "909LTdistortion",
    "909LTtune",
    "909LTdecay",
    "909MTfrontcut",
    "909MTpan",
    "909MTdistortion",
    "909MTtune",
    "909MTdecay",
    "909HTfrontcut",
    "909HTpan",
    "909HTdistortion",
    "909HTtune",
    "909HTdecay",
    "909RSpan",
    "909RStune",
    "909CPpan",
    "909CPtune",
    "909CHdistortion",
    "909CHtune",
    "909CHdecay",
    /// These items are out of order
    "909OHtune",            // 104
    "909BDfrontcut",        // 105
    "909BDpan",                     // 106
    "909BDdistortion",      // 107
    "909SDfrontcut",        // 108
    "909SDpan",                     // 109
    "909SDdistortion",      // 110
    "909CHpan",                     // 111
    "909OHpan",                     // 112
    "909OHdecay",           // 113
    /// End out of order
    "909CCpan",
    "909CCtune",
    "909CCdecay",
    "909RCpan",
    "909RCtune",
    "909RCdecay"
    };


    public Object[] emitAll(String key)
        {
        Integer param = (Integer)(ccParametersToIndex.get(key));
        if (param == null) return new Object[0];
        
        int p = param.intValue();
        
        return buildCC(getChannelOut(), p, model.get(key));
        }

    public static String getSynthName() { return "Novation Drumstation / D Station"; }
    }
    
    
/**** 
      NOVATION DRUMSTATION / D STATION MIDI FORMAT

      The Drumstation has only rudimentary sysex as far as I have ascertained.  It has only two commands, which
      it both transmits and receives:

      1. Dump a single patch
      2. Dump batch of 15 patches (numbers 25...39) plus some additional information

      There are no sysex commands for updating individual parameters, but many parameters can be updated in real 
      time via CC (as described in the manual).  There are also no sysex commands for requesting patches, nor any
      distinction between sending individual patches to current memory versus writing them to patch memory (this
      must be done manually on the machine per the manual -- see the WRITE switch).



      -------------------------
      SINGLE PATCH DUMP COMMAND
      -------------------------


      Sysex is as follows:

      0       F0
      1       00              Novation
      2       20              Novation
      3       29              Novation
      4       02
      5       01
      6       22
      7...11E         280 bytes of NYBBLIZED DATA
      11F     F7

      There does not appear to be a checksum.


      NYBBLIZED DATA
      ==============
      The Nybblized Data consists of 140 8-bit bytes of DATA broken into nybble pairs. The first nybble is the high 4 bits
      and the second nybble is the low 4 bits.  Thus altogether there are 280 bytes (7...11E inclusive). 


      DATA
      ====
      Once denybblized, the 140 bytes of DATA is in the following order.  See TABLES 1...4 for
      information on the 808 and 909 drum data.  See TABLE 6 for information on the Bank data.

      6 bytes 909 BD Data
      4 bytes 909 RS Data
      6 bytes 909 SD Data
      4 bytes 909 CP Data
      5 bytes 909 LT Data
      5 bytes 909 MT Data
      5 bytes 909 CH Data
      5 bytes 909 HT Data
      5 bytes 909 CC Data
      5 bytes 909 RC Data
      5 bytes 909 OH Data
      6 bytes 808 BD Data
      4 bytes 808 RS Data
      4 bytes 808 CP Data
      6 bytes 808 SD Data
      5 bytes 808 CH Data
      5 bytes 808 LT Data
      5 bytes 808 OH Data
      5 bytes 808 MT Data
      5 bytes 808 CC Data
      5 bytes 808 HT Data
      4 bytes 808 CB Data
      4 bytes 808 HC Data
      4 bytes 808 MC Data
      4 bytes 808 LC Data
      4 bytes 808 MA Data
      4 bytes 808 CL Data
      1 byte Bank A Data                      [Table 6]
      1 byte Bank B Data                      [Table 6]
      1 byte Bank C Data                      [Table 6]
      1 byte Bank D Data                      [Table 6]
      6 bytes UNUSED                          * use unknown if any
      1 byte GM Set Data                      0 for 808   or    1 for 909


      The 909 and 808 drum data take four forms: 6-byte, 5-byte [2 variations], and 4-byte. 

      [TABLE 1] 6-byte 808/909 data:

      Byte            Bits                                    Bits
      0               8: Tune Velocity                        1-7: Tune (0...127)
      1               8: Level Velocity                       1-7: Level (0...127)
      2               8: Tone Velocity                        1-7: Tone                               [or Attack] 
      3               8: Decay Velocity                       1-7: Decay                              [or Snappy]
      4               8: Note Off Recognition                 1-7: Front Cut (0...99)
      5               5-8: Distortion (0...15)                1-4: Pan/Output           (see TABLE 5)

      [TABLE 2] 5-byte 808/909 data [except 808 Crash Cymbal]:

      Byte            Bits                                    Bits
      0               8: Tune Velocity                        1-7: Tune (0...127)
      1               8: Level Velocity                       1-7: Level (0...127)
      2               8: Decay Velocity                       1-7: Decay
      3               8: Note Off Recognition                 1-7: Front Cut (0...99)
      4               5-8: Distortion (0...15)                1-4: Pan/Output           (see TABLE 5)

      [TABLE 3] 5-byte 808 data [808 Crash Cymbal Only]:

      Byte            Bits                                    Bits
      0               8: Level Velocity                       1-7: Level (0...127)                    *** MidiQuest appears to have this wrong, Crash Cymbal is treated like the other 5-byte drums
      1               8: Tone Velocity                        1-7: Tone (0...127)
      2               8: Decay Velocity                       1-7: Decay
      3               8: Note Off Recognition                 1-7: Front Cut (0...99)
      4               5-8: Distortion (0...15)                1-4: Pan/Output           (see TABLE 5)

      [TABLE 4] 4-byte 808/909 data:

      Byte            Bits                                    Bits
      0               8: Tune Velocity                        1-7: Tune (0...127)
      1               8: Level Velocity                       1-7: Level (0...127)
      2               8: Note Off Recognition                 1-7: Front Cut (0...99)
      3               5-8: Distortion (0...15)                1-4: Pan/Output           (see TABLE 5)

      NOTE: change "Level Velocity" ?  That's a MIDIQuest thing.  But "Velocity Velocity" sounds bad.  "Volume Velocity" maybe?

      [TABLE 5] 
      0  L4 
      1  L3 
      2  L2
      3  L1 
      4  -- 
      5  R1 
      6  R2 
      7  R3 
      8  R4 
      9  O1 
      10 O2 
      11 O3 
      12 O4 
      13 O5 
      14 O6 
      15 O4                     The O4 appears twice in case someone's foolish enough to set the pan/output value to 15




      [TABLE 6] Each Bank data is a single byte with the possible values

      0  909 BD
      1  909 SD
      2  909 LT
      3  909 MT
      4  909 HT
      5  909 RS
      6  909 CP
      7  909 CH
      8  909 OH
      9  909 CC
      10 909 RC
      11 808 BD
      12 808 SD
      13 808 LT
      14 808 MT
      15 808 HT
      16 808 RS
      17 808 CP
      18 808 CB
      19 808 CH               *** MidiQuest appears to have 808 CH and 808 OH switched erroneously
      20 808 OH
      21 808 CC
      22 808 LC
      23 808 MC
      24 808 HC
      25 808 MA
      26 808 CL









      ------------------
      BATCH DUMP COMMAND
      ------------------

      Sysex is as follows:

      0               F0
      1               00              Novation
      2               20              Novation
      3               29              Novation
      4               02
      5               01
      6               11
      7...1006                4096 bytes of NYBBLIZED DATA
      1007    F7


      NYBBLIZED DATA
      ==============
      The Nybblized Data consists of 15 patches (numbers 25 to 39), each of which is 136 8-bit 
      bytes, finally followed by four footer bytes, of DATA broken into nybble pairs, totaling 4096 bytes
      all told.  The first nybble is the high 4 bits and the second nybble is the low 4 bits.  


      DATA
      ====
      136 bytes       PATCH DATA 25
      136 bytes       PATCH DATA 26
      136 bytes       PATCH DATA 27
      136 bytes       PATCH DATA 28
      136 bytes       PATCH DATA 29
      136 bytes       PATCH DATA 30
      136 bytes       PATCH DATA 31
      136 bytes       PATCH DATA 32
      136 bytes       PATCH DATA 33
      136 bytes       PATCH DATA 34
      136 bytes       PATCH DATA 35
      136 bytes       PATCH DATA 36
      136 bytes       PATCH DATA 37
      136 bytes       PATCH DATA 38
      136 bytes       PATCH DATA 39
      8 bytes         FOOTER DATA


      PATCH DATA
      ==========
      Patch data is similar to the data in the single-patch dump message, except for the
      BANK DATA and GM SET DATA, which are different, and fewer UNUSED bytes.  See TABLES 1-4
      (in the previous single-patch dump sysex) for information about the 808 and 909 data.
      See TABLE 7 below for information about the BANK data.  
      
      6 bytes 909 BD Data
      4 bytes 909 RS Data
      6 bytes 909 SD Data
      4 bytes 909 CP Data
      5 bytes 909 LT Data
      5 bytes 909 MT Data
      5 bytes 909 CH Data
      5 bytes 909 HT Data
      5 bytes 909 CC Data
      5 bytes 909 RC Data
      5 bytes 909 OH Data
      6 bytes 808 BD Data
      4 bytes 808 RS Data
      4 bytes 808 CP Data
      6 bytes 808 SD Data
      5 bytes 808 CH Data
      5 bytes 808 LT Data
      5 bytes 808 OH Data
      5 bytes 808 MT Data
      5 bytes 808 CC Data
      5 bytes 808 HT Data
      4 bytes 808 CB Data
      4 bytes 808 HC Data
      4 bytes 808 MC Data
      4 bytes 808 LC Data
      4 bytes 808 MA Data
      4 bytes 808 CL Data
      4 bytes BANK DATA               * Note different organization than bank data in single patch sysex.  See TABLE 7
      2 bytes UNUSED                  * use unknown if any
      1 byte GM SET DATA              0 for 808   or   4 for 909    * Note different than the single patch sysex version

      The 808 and 909 data is the same as in the single-patch sysex.


      [TABLE 7] BANK DATA
      ===================
      Per TABLE 6, each bank is one of 27 possible values.  This requires
      five bits per bank.  These bits are packed into the four bytes,
      called bytes 0, 1, 2, and 3 (and stored in that order), in the 
      following way.  Note that bit 7 is the high bit, down to bit 0 
      being the low bit.

      Bank A: [Byte 0 bits 4, 3, 2, 1, 0]
      Bank B: [Byte 1 bits 1, 0] [Byte 0 bits 7, 6, 5]
      Bank C: [Byte 1 bits 6, 5, 4, 3, 2]
      Bank D: [Byte 3 bits 7, 6, 5, 4] [Byte 1 bit 7]

      The Bank data values are the same as in the single-patch sysex. 

      Other use of the bank data region is unknown.


      FOOTER DATA
      ===========
      The eight bytes are, in order, 0, 1, 2, 3, 4, 5, 6, 7
      Bit 7 is the high bit, bit 0 is the low bit

      MIDI RX Channel:        byte 6          [1...16:  NOT offset to 0...15; 0 does not appear to be valid]
      MIDI TX Channel:        byte 3          [1...16:  NOT offset to 0...15; 0 does not appear to be valid]

      Other use of the footer data is unknown.

****/


