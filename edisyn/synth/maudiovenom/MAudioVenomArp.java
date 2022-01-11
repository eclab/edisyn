/***
    Copyright 2018 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.maudiovenom;

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
   A patch editor for the M-Audio Venom (Multimode)
        
   @author Sean Luke
*/

public class MAudioVenomArp extends Synth
    {
    public static final byte DEFAULT_ID = (byte)0x7F;           // this seems to be what the venom returns
    
    public static final String[] BANKS = new String[] { "A", "B" };
    public static final String[] WRITEABLE_BANKS = new String[] { "B" };

    public static final String[] ARP_PATTERN_TYPES = new String[] { "End", "Note", "CC", "Pitch Bend" };    
    public static final String[] ARP_MODES = new String[] { "Standard", "Phrase", "Drum" };
    public static final String[] ARP_SOURCES = new String[] { "Pattern", "Single" };
    public static final String[] ARP_BANKS = new String[] { "A", "B" };
    public static final String[] ARP_NOTE_ORDERS = new String[]  { "Up", "Down", "Up/Down Excl.",  "Up/Down Incl.", "Down/Up Excl.",  "Down/Up Incl.", "Chord" };
    public static final String[] PUSH_BUTTON_OPTIONS = new String[] 
    { "Delete", "Insert Before", "Swap With Next", null, "1 (Bar 1)", "2", "3", "4", "5", "6", "7", "8", "9",
      "10", "11", "12", "13", "14", "15", "16", "17 (Bar 2)", "18", "19",
      "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32" };
    public static final String[] NOTES = new String[] { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };

    public static final int QUANTIZE_UP = 0;
    public static final int QUANTIZE_DOWN = 1;
    public static final int QUANTIZE_NEAREST = 2;
        
                

 
    public MAudioVenomArp()
        {
        if (parametersToIndex == null)
            parametersToIndex = new HashMap();
        for(int i = 0; i < parameters.length; i++)
            {
            parametersToIndex.put(parameters[i], Integer.valueOf(i));
            }

        /// SOUND PANEL
                
        SynthPanel soundPanel = new SynthPanel(this);
        
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        hbox.addLast(addArpeggiator(Style.COLOR_A()));
        vbox.add(hbox);
        vbox.add(addPattern(1, Style.COLOR_B()));
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("General, 0", soundPanel);

        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        vbox.add(addPattern(2, Style.COLOR_A()));
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("16", soundPanel);

        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        vbox.add(addPattern(3, Style.COLOR_B()));
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("36", soundPanel);

        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        vbox.add(addPattern(4, Style.COLOR_A()));
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("56", soundPanel);

        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        vbox.add(addPattern(5, Style.COLOR_B()));
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("76", soundPanel);

        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        vbox.add(addPattern(6, Style.COLOR_A()));
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("96", soundPanel);

        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        vbox.add(addPattern(7, Style.COLOR_B()));
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("116", soundPanel);

        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        vbox.add(addPattern(8, Style.COLOR_A()));
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("136", soundPanel);

        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        vbox.add(addPattern(9, Style.COLOR_B()));
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("156", soundPanel);

        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        vbox.add(addPattern(10, Style.COLOR_A()));
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("176", soundPanel);

        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        vbox.add(addPattern(11, Style.COLOR_B()));
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("196", soundPanel);

        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        vbox.add(addPattern(12, Style.COLOR_B()));
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("216", soundPanel);

        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        vbox.add(addPattern(13, Style.COLOR_B()));
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("236", soundPanel);

        model.set("name", "Untitled");
        model.set("number", 0);
        model.set("bank", 0);
                
        loadDefaults();
        }
                
    public String getDefaultResourceFileName() { return "MAudioVenomArp.init"; }
    public String getHTMLResourceFileName() { return "MAudioVenomArp.html"; }

    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        JComboBox bank = new JComboBox(writing? WRITEABLE_BANKS : BANKS);
        bank.setEditable(false);
        bank.setMaximumRowCount(32);
        int b = model.get("bank");
        if (writing)
            {
            b -= 1;
            if (b < 0) b = 0;
            }
        bank.setSelectedIndex(b);
                
        JTextField number = new SelectedTextField("" + (model.get("number")), 3);
                
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
                showSimpleError(title, "The Patch Number must be an integer 0 ... 127");
                continue;
                }
            if (n < 0 || n > 127)
                {
                showSimpleError(title, "The Patch Number must be an integer 0 ... 127");
                continue;
                }
                                
            change.set("bank", bank.getSelectedIndex() + (writing ? 1 : 0));
            change.set("number", n);
                        
            return true;
            }
        }
                                                                      
        
    /** Add the global patch category (name, id, number, etc.) */
    public JComponent addNameGlobal(Color color)
        {
        Category globalCategory = new Category(this, "M-Audio Venom [Arp]", color);
        //globalCategory.makeUnresettable();
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        final PatchDisplay pd = new PatchDisplay(this, 3);
        comp = pd;
        vbox.add(comp);
                
        comp = new StringComponent("Patch Name", this, "name", 10, "Name must be up to 10 characters.")
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
        hbox.add(Strut.makeHorizontalStrut(100));
                
        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }
        
        
    // arpsource is 0=pattern, *127*=single

    // arpbipolar is 0 or 127

    // arplatchkeys is 0 or 127

    // docs say octave is -4...4, but it is 60...68

    public JComponent addArpeggiator(Color color)
        {
        Category category = new Category(this, "General", color);
              
        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();

        final LabelledDial octaverange = new LabelledDial("Octave", this,  "arpoctaverange", color, 60, 68, 64);
        ((LabelledDial)octaverange).addAdditionalLabel("Range");

        final LabelledDial rootnote = new LabelledDial("Root", this,  "arprootnote", color, 0, 127)
            {
            public String map(int value)
                {
                return NOTES[value % 12] + (value / 12 - 2);                    
                }
            };
        ((LabelledDial)rootnote).addAdditionalLabel("Note");

        final VBox noteorder_bipolar = new VBox();
        params = ARP_NOTE_ORDERS;
        comp = new Chooser("Note Order", this,  "arpnoteorder", params);
        noteorder_bipolar.add(comp);

        comp = new CheckBox("Bipolar", this,  "arpbipolar");
        noteorder_bipolar.add(comp);

        VBox vbox = new VBox();

        /*              
                        params = ARP_BANKS;
                        comp = new Chooser("Bank", this,  "arpbank", params)
                        {
                        public void update(String key, Model model)
                        {
                        super.update(key, model);
                        int bank = model.get( "arpbank");
                        int pattern = model.get( "arppattern");
                        if (bank >= 0 && pattern >= 0)
                        {
                        category.setName("Arpeggiator " + part + " (" + DEFAULT_ARP_PATTERN_NAMES[bank][pattern] + ")");
                        }
                        }
                        };
                        vbox.add(comp);
        */

        /*
          vbox = new VBox();
          params = ARP_SOURCES;
          comp = new Chooser("Source", this,  "arpsource", params);
          vbox.add(comp);
        */
        hbox.add(vbox);

        vbox = new VBox();        
        params = ARP_MODES;
        comp = new Chooser("Mode", this,  "arpmode", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);

                hbox.remove(rootnote);
                hbox.remove(octaverange);
                hbox.remove(noteorder_bipolar);

                int val = model.get( "arpmode", 0);
                if (val == 0)           // standard
                    {
                    hbox.add(octaverange);
                    hbox.add(noteorder_bipolar);
                    }
                else if (val == 1)      // phrase
                    {
                    hbox.add(octaverange);
                    hbox.add(rootnote);
                    }
                else                            // drum 
                    {
                    // addnothing
                    }
                        
                hbox.revalidate();
                hbox.repaint();
                }
            };
        vbox.add(comp);

        comp = new CheckBox("Enable", this,  "arpenable");
        vbox.add(comp);
        hbox.add(vbox);

        comp = new CheckBox("Latch", this,  "arplatchkeys");
        vbox.add(comp);
        hbox.add(vbox);

        /*
          comp = new LabelledDial("Pattern", this,  "arppattern", color, 0, 127)
          {
          public void update(String key, Model model)
          {
          super.update(key, model);

          int bank = model.get( "arpbank");
          int pattern = model.get( "arppattern");
          if (bank >= 0 && pattern >= 0)
          category.setName("Arpeggiator " + part + " (" + DEFAULT_ARP_PATTERN_NAMES[bank][pattern] + ")");
          }
          };
          hbox.add(comp);
        */

        hbox.add(octaverange);
        hbox.add(noteorder_bipolar);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }



    public static final int MODE_END = 0;
    public static final int MODE_NOTE = 1;
    public static final int MODE_CONTROLLER = 2;
    public static final int MODE_BEND = 3;

    public JComponent addPattern(final int stage, Color color)
        {
        int start = 16 + (stage - 2) * 20;
        int end = start + 20;
                
        if (stage == 1)
            {
            start = 0;
            end = 16;
            }
                        

        Category category = new Category(this, "Pattern Steps " + start + "-" + (end - 1), color);
              
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();

        for(int i = start; i < end; i++)
            {
            final LabelledDial[] dials = new LabelledDial[3];

            final int _i = i;
            final LabelledDial a = new LabelledDial("Note", this,  "pattern" + i + "a" , color, 0, 127)
                {
                public void update(String key, Model model)
                    {
                    super.update(key, model);
                    SwingUtilities.invokeLater(new Runnable()
                        {
                        public void run()
                            {
                            for(int j = 0; j < 3; j++)
                                if (dials[j] != null) 
                                    dials[j].repaintDial();
                            }
                        });
                    }
        
                public String map(int value)
                    {
                    int mode = model.get("pattern" + _i + "mode");
                    if (mode == MODE_END)
                        {
                        return "";
                        }
                    else if (mode == MODE_NOTE)
                        {
                        return NOTES[value % 12] + (value / 12 - 2);                    
                        }
                    else if (mode == MODE_CONTROLLER)
                        {
                        return "" + value;
                        }
                    else            // MODE_BEND
                        {
                        return "" + ((value - 64) * 128);
                        }
                    }
                };

            final LabelledDial b = new LabelledDial("Velocity", this,  "pattern" + i + "b" , color, 0, 127)
                {
                public void update(String key, Model model)
                    {
                    super.update(key, model);
                    SwingUtilities.invokeLater(new Runnable()
                        {
                        public void run()
                            {
                            for(int j = 0; j < 3; j++)
                                if (dials[j] != null) 
                                    dials[j].repaintDial();
                            }
                        });
                    }
        
        
                public String map(int value)
                    {
                    int mode = model.get("pattern" + _i + "mode");
                    if (mode == MODE_END)
                        {
                        return "";
                        }
                    else if (mode == MODE_NOTE)
                        {
                        if (value == 0) return ("Off");
                        else return "" + value;
                        }
                    else if (mode == MODE_CONTROLLER)
                        {
                        return "" + value;
                        }
                    else            // MODE_BEND
                        {
                        int a = model.get("pattern" + _i + "a");
                        return "" + ((a * 128 + value) - 8192);
                        }
                    }
                };

            // Timestamps go to 65535, but realistically they can only go to 767 due to the two-bar limit.

            final LabelledDial when = new LabelledDial("When", this,  "pattern" + i + "time" , color, 0, 767)               // 65535)
                {
                public void update(String key, Model model)
                    {
                    super.update(key, model);
                    SwingUtilities.invokeLater(new Runnable()
                        {
                        public void run()
                            {
                            for(int j = 0; j < 3; j++)
                                if (dials[j] != null) 
                                    dials[j].repaintDial();
                            }
                        });
                    }
        
        
                public String map(int value)
                    {
                    if (model.get("pattern" + _i + "mode") == MODE_END)
                        {
                        return "End";
                        }
                    else if (value % (96 / 4)  == 0)
                        {
                        return "-" + (value / (96 / 4) + 1) + "-";              // display in 16th notes
                        }
                    else
                        {
                        return "" + (value + 1);
                        }
                    }
                };

            dials[0] = a;
            dials[1] = b;
            dials[2] = when;

            params = ARP_PATTERN_TYPES;
            comp = new Chooser("Step " + _i , this, "pattern" + i + "mode", params)
                {
                public void update(String key, Model model)
                    {
                    super.update(key, model);
                    int mode = model.get(key);
                    if (mode == MODE_END)
                        {
                        when.setLabel(" ");
                        a.setLabel(" ");
                        b.setLabel(" ");
                        a.repaintDial();
                        b.repaintDial();
                        when.repaintDial();
                        }
                    else if (mode == MODE_NOTE)
                        {
                        when.setLabel("When");
                        a.setLabel("Note");
                        b.setLabel("Velocity");
                        a.repaintDial();
                        b.repaintDial();
                        when.repaintDial();
                        }
                    else if (mode == MODE_CONTROLLER)
                        {
                        when.setLabel("When");
                        a.setLabel("Parameter");
                        b.setLabel("Value");
                        a.repaintDial();
                        b.repaintDial();
                        when.repaintDial();
                        }
                    else            // MODE_BEND
                        {
                        when.setLabel("When");
                        a.setLabel("Coarse");
                        b.setLabel("Fine");
                        a.repaintDial();
                        b.repaintDial();
                        when.repaintDial();
                        }
                    }
                };

            PushButton options = new PushButton("Options", PUSH_BUTTON_OPTIONS)
                {
                public void perform(int val)
                    {
                    if (val < 3)
                        {
                        Model backup = (Model)(model.clone());
                        setSendMIDI(false);
                        undo.setWillPush(false);

                        if (val == 0)   // delete
                            {
                            for(int j = _i + 1; j < 256; j++)
                                {
                                model.set("pattern" + (j-1) + "a", model.get("pattern" + j + "a"));
                                model.set("pattern" + (j-1) + "b", model.get("pattern" + j + "b"));
                                model.set("pattern" + (j-1) + "time", model.get("pattern" + j + "time"));
                                model.set("pattern" + (j-1) + "mode", model.get("pattern" + j + "mode"));
                                }
                            model.set("pattern" + 255 + "a", 0);
                            model.set("pattern" + 255 + "b", 0);
                            model.set("pattern" + 255 + "time", 0);
                            model.set("pattern" + 255 + "mode", MODE_END);
                            }
                        else if (val == 1)      // insert before
                            {
                            for(int j = 255; j > _i; j--)
                                {
                                model.set("pattern" + j + "a", model.get("pattern" + (j-1) + "a"));
                                model.set("pattern" + j + "b", model.get("pattern" + (j-1) + "b"));
                                model.set("pattern" + j + "time", model.get("pattern" + (j-1) + "time"));
                                model.set("pattern" + j + "mode", model.get("pattern" + (j-1) + "mode"));
                                }
                            model.set("pattern" + _i + "a", 0);
                            model.set("pattern" + _i + "b", 0);
                            model.set("pattern" + _i + "time", 0);
                            model.set("pattern" + _i + "mode", MODE_END);
                            }
                        else if (val == 2)  // swap with next
                            {
                            if (_i < 255)
                                {
                                int a = model.get("pattern" + _i + "a");
                                int b = model.get("pattern" + _i + "b");
                                int time = model.get("pattern" + _i + "time");
                                int mode = model.get("pattern" + _i + "mode");
                                model.set("pattern" + _i + "a", model.get("pattern" + (_i + 1) + "a"));
                                model.set("pattern" + _i + "b", model.get("pattern" + (_i + 1) + "b"));
                                model.set("pattern" + _i + "time", model.get("pattern" + (_i + 1) + "time"));
                                model.set("pattern" + _i + "mode", model.get("pattern" + (_i + 1) + "mode"));
                                model.set("pattern" + (_i + 1) + "a", a);
                                model.set("pattern" + (_i + 1) + "b", b);
                                model.set("pattern" + (_i + 1) + "time", time);
                                model.set("pattern" + (_i + 1) + "mode", mode);
                                }
                            }
                        setSendMIDI(true);
                        undo.setWillPush(true);
                        if (!backup.keyEquals(getModel()))  // it's changed, do an undo push
                            undo.push(backup);
                        // sendAllParameters();                 // dunno about this
                        }
                    else
                        {
                        // include null (horizontal bar)
                        model.set("pattern" + _i + "time", (val - 4) * 24);
                        }
                    }
                };

            HBox hbox0 = new HBox();
            hbox0.add(comp);
            VBox vbox0 = new VBox(VBox.TOP_CONSUMES);
            vbox0.addLast(Stretch.makeVerticalStretch());
            vbox0.add(options);
            hbox0.addLast(vbox0);
            HBox hbox1 = new HBox();
            VBox vbox1 = new VBox();
            hbox1.add(when);
            hbox1.add(a);
            hbox1.add(b);
            vbox1.add(hbox0);
            vbox1.add(hbox1);
            hbox.add(vbox1);
                        
            if (i - start == 3 || i - start == 7 || i - start == 11 || i - start == 15 || i - start == 19)
                {
                vbox.add(hbox);
                if ((stage == 1 && (i - start != 15)) || 
                    (stage > 1 && (i - start != 19)))
                    {                                       
                    vbox.add(Strut.makeVerticalStrut(16));
                    hbox = new HBox();
                    }
                }
            }
        
        category.add(vbox, BorderLayout.CENTER);
        return category;
        }




    /*
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
    */

    
    //// These are a lot like the Prophet '08 7<-->8 bit conversions.
    ////
    //// The Venom manual describes conversion from 8 bit to 7 bit as
    //// taking 7 bytes at a time, removing the top bits and putting them
    //// in the first byte, producing 8 7-bit bytes as a result.  That's all
    //// fine and good, except that the data doesn't come in 7 byte chunks.
    //// It's 198 bytes long, which isn't a multiple of 7.  It appears that
    //// the answer is to take the remainder (2 bytes), strip off the high bits
    //// and make 3 more 7-bit bytes.


    // converts up to but not including 'to'
    byte[] convertTo8Bit(byte[] data, int from, int to)
        {
        // How big?
        int main = ((to - from) / 8) * 8;
        int remainder = (to - from) - main;
        int size = ((to - from) / 8) * 7 + (remainder - 1);
        
        byte[] newd = new byte[size];
        
        int j = 0;
        for(int i = from; i < to; i += 8)
            {            
            for(int x = 0; x < 7; x++)
                {
                if (j + x < newd.length)
                    newd[j + x] = (byte)(data[i + x + 1] | (byte)(((data[i] >>> x) & 0x1) << 7));
                }
            j += 7;
            }
        return newd;
        }
        
        
    // converts all bytes
    byte[] convertTo7Bit(byte[] data)
        {
        // How big?
        int main = ((data.length) / 7) * 7;
        int remainder = data.length - main;
        int size = (data.length) / 7 * 8 + (remainder + 1);
        byte[] newd = new byte[size];   
             
        int j = 0;
        for(int i = 0; i < data.length; i+=7)
            {
            // First load the top bits
            for(int x = 0; x < 7; x++)
                {
                if (i+x < data.length)
                    newd[j] = (byte)(newd[j] | (((data[i + x] >>> 7) & 1) << x));
                }
            j++;
            // Next load the data
            for(int x = 0; x < 7; x++)
                {
                if (i+x < data.length)
                    newd[j+x] = (byte)(data[i+x] & 127);
                }
            j+=7;
            }
        return newd;
        }



    // this is only changed for testing
    public static boolean truncateAndSortOnEmit = true;
        
    public Object[] emitAll(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        if (tempModel == null)
            tempModel = getModel();
            
        int BB = tempModel.get("bank", 0);
        int NN = tempModel.get("number", 0);

        byte[] data = new byte[35];
        
        data[0] = (byte)0xF0;
        data[1] = (byte)0x00;           // M-audio
        data[2] = (byte)0x01;
        data[3] = (byte)0x05;
        data[4] = (byte)0x21;           // Venom
        data[5] = (byte)DEFAULT_ID;             //(byte)getID();
        data[6] = (byte)0x02;           // Write Data Dump
        data[7] = (byte)(toWorkingMemory ? 0x00 : 0x03);        // Arpeggio Data [Header?] Dump
        data[8] = (byte)(toWorkingMemory ? 0x08 : BB + 1);              // it's literally "1..2"   --- also notice Arpeggio Header Single Edit Dump
        data[9] = (byte)NN;             // ignored when toWorkingMemory
        data[data.length - 1] = (byte)0xF7;     
                
        byte[] d = new byte[parameters.length];
        
        // enable -- this is 0 or 127
        d[0] = (byte)(model.get("arpenable") == 0 ? 0 : 127);

        // source -- this is hard coded to "pattern" (0) [vs. "single" (127)]
        d[1] = (byte)0;
        
        // bank -- this is hard coded to my bank.  It's 0-1
        d[2] = (byte)model.get("bank");
        
        // pattern -- this is hard coded to my number.  It's 0-127
        d[3] = (byte)model.get("number");
        
        // arp mode
        d[4] = (byte)model.get("arpmode");
        
        // node order
        d[5] = (byte)model.get("arpnoteorder");

        /// A bug in the Venom sends 0 back instead of 64 when downloading a patch other than current.
        /// I try to replicate that here.
            
        // octave range
        d[6] = (byte)model.get("arpoctaverange");
        if (d[6] == 64 && !toWorkingMemory)
            d[6] = 0;
        
        // bipolar -- this is 0 or 127
        d[7] = (byte)(model.get("arpbipolar") == 0 ? 0 : 127);
        
        // latch keys -- this is 0 or 127
        d[8] = (byte)(model.get("arplatchkeys") == 0 ? 0 : 127);

        // root note
        d[9] = (byte)model.get("arprootnote");
        
        // Load name
        String name = model.get("name", "") + "          ";
        for(int i = 0; i < 10; i++)
            {
            d[10 + i] = (byte)(name.charAt(i));
            }

        // Nybblize, or whatever you'd call it, into data
        d = convertTo7Bit(d);
        System.arraycopy(d, 0, data, 10, d.length);
                
        // Compute checksum
        data[data.length - 2] = checksum(data, 6, data.length - 2);             // starting at command



        // sort and truncate the data we're sending out
        Model emit = model.copy();
        Model current = model;
        model = emit;
        if (truncateAndSortOnEmit)
            {
            truncate();
            sort();
            }
        model = current;

        //// ARP PATTERN
                
        // How long is our pattern?
        int patternlen = length(emit);                  // must truncate and sort first
        byte[] d2 = new byte[patternlen * 4];
        for(int i = 0; i < patternlen; i++)
            {
            int mode = emit.get("pattern" + i + "mode");
            int val = 0;
            if (mode != MODE_END)
                {
                // Error in documentation: d2 and d1 are mixed up
                
                int a = emit.get("pattern" + i + "a", 0);
                int b = emit.get("pattern" + i + "b", 0);
                
                val = (emit.get("pattern" + i + "time", 0) << 16) |
                    (((mode >>> 1) & 0x01) << 15) |
                    (a << 8) |
                    ((mode & 0x01) << 7) |
                    (b << 0);
                }
                        
            // in reverse order than the manual says
            d2[i * 4 + 0] = (byte)((val >>> 0) & 0xFF);
            d2[i * 4 + 1] = (byte)((val >>> 8) & 0xFF);
            d2[i * 4 + 2] = (byte)((val >>> 16) & 0xFF);
            d2[i * 4 + 3] = (byte)((val >>> 24) & 0xFF);
            }

        // Nybblize, or whatever you'd call it, into data2
        d2 = convertTo7Bit(d2);

        byte[] data2 = new byte[d2.length + 2 + 10];
        System.arraycopy(d2, 0, data2, 10, d2.length);
                
        // Fill in rest of data2
        data2[0] = (byte)0xF0;
        data2[1] = (byte)0x00;          // M-audio
        data2[2] = (byte)0x01;
        data2[3] = (byte)0x05;
        data2[4] = (byte)0x21;          // Venom
        data2[5] = (byte)DEFAULT_ID;            //(byte)getID();
        data2[6] = (byte)0x02;          // Write Data Dump
        data2[7] = (byte)(toWorkingMemory ? 0x00 : 0x04);       // Arpeggio Pattern Dump
        data2[8] = (byte)(toWorkingMemory ? 0x0D : BB + 1);             // it's literally "1..2"   --- also notice Arpeggio Pattern Single Edit Dump
        data2[9] = (byte)NN;            // ignored when toWorkingMemory
        data2[data2.length - 1] = (byte)0xF7;   
        
        // Compute checksum
        data2[data2.length - 2] = checksum(data2, 6, data2.length - 2);         // starting at command

        Object[] result = new Object[] { data, POST_HEADER_PAUSE, data2 };
        return result;
        }
        
    public static final int POST_HEADER_PAUSE = 300;

    // manual says: 
    // The checksum is calculated as the sum of all bytes taken from the <cmd> byte 
    // and stores 0-Total with the top bit set to 0. When a SysEx is received, it 
    // totals up all values from the <cmd> byte including the checksum and the 
    // result in the bot- tom 7 bits should be 0.

    // FIXME: is this right?
    public byte checksum(byte[] data, int start, int end)
        {
        int sum = 0;
        for(int i = start; i < end; i++)
            {
            sum += data[i];
            }
        return (byte)((0-sum) & 0x7F);
        }
        
    public byte[][] split(byte[] data)
        {
        // How many syx messages do we have?
        int count = 0;
        for(int i = 0; i < data.length; i++)
            if (data[i] == (byte)0xF7) 
                count++;
                
        byte[][] split = new byte[count][];
        int num = 0;
        int start = 0;
        for(int i = start; i < data.length; i++)
            {
            if (data[i] == (byte)0xF7)
                {
                int len = i - start + 1;
                split[num] = new byte[len];
                System.arraycopy(data, start, split[num], 0, len);
                num++;
                start = i + 1;
                }
            }
        return split;
        }
        
    public int parse(byte[] data, boolean fromFile)
        {
        boolean pattern = false;
        // Split the data
        byte[][] split = split(data);
        for(int i = 0; i < split.length; i++)
            {
            boolean result = subparse(split[i], fromFile);
            pattern = pattern || result;
            }
        
        revise();
        return (pattern ? PARSE_SUCCEEDED : PARSE_INCOMPLETE);      // we're only complete if we found a pattern and a header
        }
    
    public void markEnd(int val)
        {
        model.set("pattern" + val + "a", 0);
        model.set("pattern" + val + "b", 0);
        model.set("pattern" + val + "time", 0);
        model.set("pattern" + val + "mode", MODE_END);
        }
        
    public boolean subparse(byte[] data, boolean fromFile)
        {
        /// Is this a pattern?
        boolean pattern = (data[7] == 0x04 || 
            (data[7] == 0x00 && (data[8] == 0x0D || data[8] == 0x0E || data[8] == 0x0F || data[8] == 0x10 || data[8] == 0x11)));
                                                                  
        if (!pattern)           // We're doing a header
            {
            if (data[7] == 0x03)     // it's going to a specific patch (0x03 == Arpeggiator Data Dump as opposed to 0x00 == Edit Buffer Dump)
                {
                int bank = data[8] - 1;
                if (bank < 0 || bank > 1) bank = 0;
                model.set("bank", bank);
                int number = data[9];
                model.set("number", number);
                }

            // First denybblize
            byte[] d = convertTo8Bit(data, 10, data.length - 2);
                        
            model.set("arpenable", d[0] < 64 ? 0 : 1);
            // skip arpsource
            // skip arpbank
            // skip arppattern
            model.set("arpmode", d[4]);
            model.set("arpnoteorder", d[5]);
            
            /// A bug in the Venom sends 0 back instead of 64 when downloading a patch other than current
            
            model.set("arpoctaverange", (d[6] == 0 ? 64 : d[6]));
            model.set("arpbipolar", d[7] < 64 ? 0 : 1);
            model.set("arplatchkeys", d[8] < 64 ? 0 : 1);
            model.set("arprootnote", d[9]);

            // Load name
            char[] name = new char[10];
            for(int i = 0; i < 10; i++)
                {
                name[i] = (char)(d[10 + i] & 127);
                }
            model.set("name", new String(name));
                        
            // Acknowledge, then request pattern
            if (!fromFile)
                {
                boolean sendMIDI = getSendMIDI();
                setSendMIDI(true);
                                
                // CANCEL
                // We send a CANCEL instead of an ACK because the Venom seems to respond to ACKs by often resending
                // the data.
                tryToSendSysex(new byte[] { (byte)0xF0, 0x00, 0x01, 0x05, 0x21, 
                    (byte)DEFAULT_ID, //getID(), 
                    0x7D,                       // ACK is 0x7F
                    (byte)0xF7 });
                                
                // Request pattern also
                tryToSendSysex(new byte[] { (byte)0xF0, 0x00, 0x01, 0x05, 0x021,
                    (byte)DEFAULT_ID, //getID()
                    0x01,
                    (byte)(loadingFromWorkingMemory ? 0x00 : 0x04),             // arpeggiator pattern dump
                    (byte)(loadingFromWorkingMemory ? 0x0D : (byte)(model.get("bank") + 1)),        // it's literally "1..2"
                    (byte)(model.get("number")),
                    (byte)0xF7 });
                    
                setSendMIDI(sendMIDI);
                }
            }
        else
            {
            // Unlike the header, we won't load the patch address from this data, so the
            // header can take precedence over the pattern.
                        
            // First denybblize

            byte[] d = convertTo8Bit(data, 10, data.length - 2);
                                
            // Initialize with "End"
            for(int i = 0; i < 256; i++)
                {
                markEnd(i);
                }
                        
 
            //// NOTE
            ////
            //// Gagh, the documentation on p.92-93 is very wrong when it comes to the arp pattern data.
            ////
            //// (1) The pattern byte data is transmitted in reverse order than shown: the far right byte
            ////     comes first, then the byte to its left, then the next byte to its left, and
            ////     finally the far left byte.
            ////
            //// (2) d1 and d2 are mixed up.  The correct table is:
            ////
            ////     EVENT TYPE  D1          D2
            ////     Note        Velocity    Note Num
            ////     Controller  Value       CC Num
            ////     Pitch Bend  PB MSB      PB LSB
            ////
            //// (3) NOTE and CC events are not properly described.  The correct table is:
            ////     (F1 = bit 7 of the first byte, F2 = bit 7 of the second byte)
            ////
            ////     F2 F1 EVENT TYPE
            ////     0  0  --END-- [assuming all other bits are 0]
            ////     0  1  NOTE
            ////     1  0  CONTROLLER
            ////     1  1  PITCH BEND

                    
            // Load data
            if (((d.length / 4) * 4) != d.length)
                {
                System.err.println("MAudioVenomArp Parse WARNING: pattern data length isn't a multiple of 4.  It's possible some data may have been corrupted.");
                }
                
            for(int i = 0; i < d.length; i+=4)      
                {
                if (i + 4 > d.length)  // bad
                    break;
                int val = ((d[i + 0] & 0xFF) << 0) | ((d[i + 1] & 0xFF) << 8) | ((d[i + 2] & 0xFF) << 16) | ((d[i + 3] & 0xFF) << 24);
                                
                // Error in documentation: d2 and d1 are mixed up
                
                model.set("pattern" + (i / 4) + "b", val & 127);
                model.set("pattern" + (i / 4) + "a", (val >>> 8) & 127);
                model.set("pattern" + (i / 4) + "time", (val >>> 16) & 65535);
                int mode = (((val >>> 15) & 0x01) << 1) | (((val >>> 7) & 0x01) << 0);
                                
                // I BELIEVE that the modes are probably END, NOTE, CC, BEND

                model.set("pattern" + (i / 4) + "mode", mode);
                }
                
            //// A bug in the Venom sends a message at timestep 769 (768) with a parameter of 0 and often a value of 102
            //// at the end of a number of arpeggios. We change all 769 values to END.
            for(int i = 0; i < 256; i++)
                {
                if ((model.get("pattern" + i + "time") == 768))
                    {
                    System.err.println("MAudioVenomArp Parse WARNING: buggy marker #" + i + " with timestep 768 changed to End");
                    markEnd(i);
                    }
                }
                

            if (!fromFile)
                {
                boolean sendMIDI = getSendMIDI();
                setSendMIDI(true);
                                
                // CANCEL
                // We send a CANCEL instead of an ACK because the Venom seems to respond to ACKs by often resending
                // the data.
                tryToSendSysex(new byte[] { (byte)0xF0, 0x00, 0x01, 0x05, 0x21, 
                    (byte)DEFAULT_ID, //getID(), 
                    0x7D,                       // ACK is 0x7F
                    (byte)0xF7 });
                                

                setSendMIDI(sendMIDI);
                }
            }
        
        return pattern;
        }
        
    boolean loadingFromWorkingMemory = false;
    
    String toBits(byte b)
        {
        String s = "";
        for(int i = 7; i >= 0; i--)
            s = s + ((b >>> i) & 0x01);
        return s;
        }

    String toIBits(int b)
        {
        String s = "";
        for(int i = 31; i >= 0; i--)
            s = s + ((b >>> i) & 0x01);
        return s;
        }

    byte[] secret()
        {
        // Vyzex sends the following undocumented command after saving an arp patch, but I'm not sure what it does.
                
        byte[] data = new byte[13];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x00;
        data[2] = (byte)0x01;
        data[3] = (byte)0x05;
        data[4] = (byte)0x21;
        data[5] = (byte)DEFAULT_ID; //(byte)getID();
        data[6] = (byte)0x02;           // "Write Dump" What???  Really???
        data[7] = (byte)0x15;           // UNDOCUMENTED LOCATION
        data[8] = (byte)0x00;           // UNDOCUMENTED LOCATION
        data[9] = (byte)0x09;           // UNDOCUMENTED LOCATION
        data[10] = (byte)0x00;  
        data[11] = (byte)0x00;          // also it emits 0x01
        data[12] = (byte)0xF7;
        return data;
        }

    public byte[] requestCurrentDump()
        {
        loadingFromWorkingMemory = true;

        byte[] data = new byte[11];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x00;
        data[2] = (byte)0x01;
        data[3] = (byte)0x05;
        data[4] = (byte)0x21;
        data[5] = (byte)DEFAULT_ID; //(byte)getID();
        data[6] = (byte)0x01;           // data dump request
        data[7] = (byte)0x00;           // current buffer
        data[8] = (byte)0x08;           // arp Header Single Dump
        data[9] = (byte)0x00;           // doesn't matter
        data[10] = (byte)0xF7;
        return data;
        }

    // This is how you'd request a patch, but we're not using it because we have
    // overridden performRequestDump above.
    public byte[] requestDump(Model tempModel)
        {
        if (tempModel == null)
            tempModel = getModel();
        loadingFromWorkingMemory = false;

        int NN = tempModel.get("number", 0);                    // The numbers are 0...127 but...
        int BB = tempModel.get("bank", 0) + 1;                  // Believe it or not, the banks are literally 1...2

        // we set the patch number here because it won't get set when loaded
        model.set("number", tempModel.get("number", 0));
        model.set("bank", tempModel.get("bank", 0) );
        
        byte[] data = new byte[11];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x00;
        data[2] = (byte)0x01;
        data[3] = (byte)0x05;
        data[4] = (byte)0x21;
        data[5] = (byte)DEFAULT_ID; //(byte)getID();
        data[6] = (byte)0x01;           // data dump request
        data[7] = (byte)0x03;           // arp data dump
        data[8] = (byte)BB;     
        data[9] = (byte)NN;
        data[10] = (byte)0xF7;
        return data;
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
        
        String nm = model.get("name", "Untitled");
        String newnm = revisePatchName(nm);
        if (!nm.equals(newnm))
            model.set("name", newnm);
        }
    
    public static String getSynthName() { return "M-Audio Venom [Arp]"; }
    public String getPatchName(Model model) { return model.get("name", "Untitled"); }
    
    public Model getNextPatchLocation(Model model)
        {
        int number = model.get("number");
        int bank = model.get("bank");
        
        number++;
        if (number >= 128)
            {
            number = 0;
            bank++;
            if (bank >= 2)
                bank = 0;
            }
                
        Model newModel = buildModel();
        newModel.set("number", number);
        newModel.set("bank", bank);
        return newModel;
        }

    public String getPatchLocationName(Model model)
        {
        // getPatchLocationName() is called from sprout() as a test to see if we should enable
        // batch downloading.  If we haven't yet created an .init file, then parameters won't exist
        // yet and this method will bomb badly.  So we return null in this case.
        if (!model.exists("number")) return null;
        
        int number = (model.get("number"));
        int bank = (model.get("bank"));
        return BANKS[bank] + " " + ((number > 99 ? "" : (number > 9 ? "0" : "00")) + number);
        }
    
    class Node implements Comparable 
        { 
        public int index; 
        public int time;
        public int a;
        public int b;
        public int mode;
        public Node(int i, int a, int b, int t, int m) 
            { 
            index = i; 
            this.a = a; 
            this.b = b; 
            time = t; 
            mode = m;
            }
        public int compareTo(Object obj) 
            { 
            Node n = (Node) obj;
            if (n.mode == MODE_END && n.mode == MODE_END) return 0;
            if (mode == MODE_END) return 1;
            if (n.mode == MODE_END) return -1;
            if (time < n.time) return -1;
            if (time > n.time) return 1;
            return 0;
            }
        }
    
        
    void truncate()
        {
        Model backup = (Model)(model.clone());
        setSendMIDI(false);
        undo.setWillPush(false);

        boolean truncated = false;
        for(int i = 0; i < 256; i++)
            {
            if (model.get("pattern" + i + "mode") == MODE_END)                              // found end marker
                {
                truncated = true;
                }
            else if (truncated)
                {
                markEnd(i);
                }
            else
                {
                // do nothing
                }
            }

        setSendMIDI(true);
        undo.setWillPush(true);
        if (!backup.keyEquals(getModel()))  // it's changed, do an undo push
            undo.push(backup);
        // sendAllParameters();                 // dunno about this
        }
        

    // must truncate and sort first
    int length(Model model)
        {
        for(int i = 0; i < 256; i++)
            {
            if (
                model.get("pattern" + i + "a") == 0 &&
                model.get("pattern" + i + "b") == 0 &&
                model.get("pattern" + i + "time") == 0 &&
                model.get("pattern" + i + "mode") == 0)
                {
                return i + 1;
                }
            }
        return 256;
        }
        
        
    void sort()
        {
        Model backup = (Model)(model.clone());
        setSendMIDI(false);
        undo.setWillPush(false);

        Node[] nodes = new Node[256];
        for(int i = 0; i < nodes.length; i++)
            {
            nodes[i] = new Node(i, model.get("pattern" + i + "a"),
                model.get("pattern" + i + "b"),
                model.get("pattern" + i + "time"),
                model.get("pattern" + i + "mode"));
            }
        Arrays.sort(nodes);
        for(int i = 0; i < nodes.length; i++)
            {
            model.set("pattern" + i + "a", nodes[i].a);
            model.set("pattern" + i + "b", nodes[i].b);
            model.set("pattern" + i + "time", nodes[i].time);
            model.set("pattern" + i + "mode", nodes[i].mode);
            }

        setSendMIDI(true);
        undo.setWillPush(true);
        if (!backup.keyEquals(getModel()))  // it's changed, do an undo push
            undo.push(backup);
        // sendAllParameters();                 // dunno about this
        }
        
    void quantize(int type)
        {
        Model backup = (Model)(model.clone());
        setSendMIDI(false);
        undo.setWillPush(false);

        for(int i = 0; i < 256; i++)
            {
            int t = model.get("pattern" + i + "time");
            int v = (t / 24) * 24;
            if (type == QUANTIZE_UP)
                {
                if (v != t) t = v + 24;
                }
            else if (type == QUANTIZE_DOWN)
                {
                if (v != t) t = v;
                }
            else if (type == QUANTIZE_NEAREST)
                {
                if (v != t)
                    {
                    if (t - v < (v + 24 - t))
                        t = v;
                    else
                        t = v + 24;
                    }
                }
            model.set("pattern" + i + "time", t);
            }

        setSendMIDI(true);
        undo.setWillPush(true);
        if (!backup.keyEquals(getModel()))  // it's changed, do an undo push
            undo.push(backup);
        // sendAllParameters();                 // dunno about this
        }

    public JFrame sprout()
        {
        JFrame frame = super.sprout();  
        addMAudioVenomArpMenu();
        return frame;
        }         

    public void addMAudioVenomArpMenu()
        {
        JMenu menu = new JMenu("M-Audio Venom");
        menubar.add(menu);

        JMenuItem load = new JMenuItem("Load MIDI File...");
        load.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                loadMIDI();
                }
            });
        menu.add(load);

        JMenuItem sort = new JMenuItem("Sort");
        sort.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                sort();
                }
            });
        menu.add(sort);

        JMenuItem truncate = new JMenuItem("Truncate");
        truncate.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                truncate();
                }
            });
        menu.add(truncate);

        JMenuItem up = new JMenuItem("Quantize Up");
        up.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                quantize(QUANTIZE_UP);
                }
            });
        menu.add(up);

        JMenuItem down = new JMenuItem("Quantize Down");
        down.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                quantize(QUANTIZE_DOWN);
                }
            });
        menu.add(down);

        JMenuItem nearest = new JMenuItem("Quantize to Nearest");
        nearest.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                quantize(QUANTIZE_NEAREST);
                }
            });
        menu.add(nearest);
        }

    boolean validMessage(MidiMessage m)
        {
        if (!(m instanceof ShortMessage)) return false;
        int status = ((ShortMessage)m).getStatus();
        return (status == ShortMessage.NOTE_ON || status == ShortMessage.NOTE_OFF || status == ShortMessage.PITCH_BEND || status == ShortMessage.CONTROL_CHANGE);
        }
        
        
    public void loadMIDI()
        {
        FileDialog fd = new FileDialog((Frame)(SwingUtilities.getRoot(this)), "Load MIDI File...", FileDialog.LOAD);
        fd.setFilenameFilter(new FilenameFilter()
            {
            public boolean accept(File dir, String name)
                {
                return StringUtility.ensureFileEndsWith(name, ".mid").equals(name) || StringUtility.ensureFileEndsWith(name, ".MID").equals(name) || StringUtility.ensureFileEndsWith(name, ".midi").equals(name) || StringUtility.ensureFileEndsWith(name, ".MIDI").equals(name);
                }
            });

        String path = getLastDirectory();
        if (path != null)
            fd.setDirectory(path);

        disableMenuBar();
        fd.setVisible(true);
        enableMenuBar();
        if (fd.getFile() != null)
            {
            try
                {
                Sequence seq = MidiSystem.getSequence(new File(fd.getDirectory(), fd.getFile()));
                int resolution = getResolution(seq);
                if (resolution == -1)
                    {
                    showSimpleError("File Error", "This file contains MIDI data using an invalid time measure (such as SMTPE).");
                    }
                else
                    {
                    if (resolution != 96)
                        {
                        showSimpleConfirm("Nonstandard Time Measure", "This file contains MIDI at " + resolution + "pulses per quarter note.\n" +
                            "The Venom expects 96 pulses per quarter note.  Edisyn will try to\n" +
                            "convert as best it can.");
                        }

                    Model backup = (Model)(model.clone());
                    setSendMIDI(false);
                    undo.setWillPush(false);

                    // clear out
                    for(int i = 0; i < 256; i++)
                        {
                        markEnd(i);
                        }

                    int count = 0;
                    boolean tooLong = false;
                    ArrayList<MidiEvent> data = extractMidi(seq);
                    for(MidiEvent evt : data)
                        {
                        if (count >= 256)
                            {
                            showSimpleConfirm("Too Much Data", "An arpeggio pattern can only have 256 events.\n" +
                                "Some MIDI data was not imported.");
                            break;
                            }
                        long tick = evt.getTick();
                        tick = (tick * 96) / resolution;
                        if (tick >= (96 * 4 * 2))       // uh oh
                            {
                            tooLong = true;
                            }
                        else
                            {
                            ShortMessage mesg = (ShortMessage)(evt.getMessage());
                            int status = mesg.getStatus();
                            model.set("pattern" + count+ "time", (int)tick);
                            if (status == ShortMessage.NOTE_ON)
                                {
                                model.set("pattern" + count + "a", mesg.getData1());
                                model.set("pattern" + count + "b", mesg.getData2());
                                model.set("pattern" + count + "mode", MODE_NOTE);
                                }
                            else if (status == ShortMessage.NOTE_OFF)
                                {
                                model.set("pattern" + count + "a", mesg.getData1());
                                model.set("pattern" + count + "b", 0);
                                model.set("pattern" + count + "mode", MODE_NOTE);
                                }
                            else if (status == ShortMessage.PITCH_BEND)
                                {
                                model.set("pattern" + count + "a", mesg.getData1());
                                model.set("pattern" + count + "b", mesg.getData2());
                                model.set("pattern" + count + "mode", MODE_BEND);
                                }
                            else if (status == ShortMessage.CONTROL_CHANGE)
                                {
                                model.set("pattern" + count + "a", mesg.getData1());
                                model.set("pattern" + count + "b", mesg.getData2());
                                model.set("pattern" + count + "mode", MODE_CONTROLLER);
                                }
                            count++;
                            }
                        }

                    setSendMIDI(true);
                    undo.setWillPush(true);
                    if (!backup.keyEquals(getModel()))  // it's changed, do an undo push
                        undo.push(backup);
                    // sendAllParameters();                 // dunno about this

                    if (tooLong)
                        {
                        showSimpleConfirm("Too Long", "An arpeggio pattern can only be 2 measures long.\n" +
                            "Some MIDI data was not imported.");
                        }
                    }
                }
            catch (Exception ex)
                {
                Synth.handleException(ex); 
                showSimpleError("File Error", "Could not get MIDI data from this file.");
                } 
            }
        }

    public Sequence getSequence(File f)
        {
        try
            {
            return MidiSystem.getSequence(f);
            }
        catch (Exception ex)
            {
            Synth.handleException(ex); 
            return null;
            }
        }
        
    /** Returns -1 if invalid resolution */
    public int getResolution(Sequence seq)
        {
        if (seq.getDivisionType() != Sequence.PPQ)
            return -1;
        else return seq.getResolution();
        }
                
    public ArrayList<MidiEvent> extractMidi(Sequence seq)
        {
        int count = 0;
                        
        Track[] t  = seq.getTracks();
        for(int i = 0; i < t.length; i++)
            for(int j = 0; j < t[i].size(); j++)
                {
                if (validMessage(t[i].get(j).getMessage()))
                    count++;
                }
                                                                                        
        ArrayList<MidiEvent> events = new ArrayList<MidiEvent>();
                                        
        count = 0;
        for(int i = 0; i < t.length; i++)
            for(int j = 0; j < t[i].size(); j++)
                if (validMessage(t[i].get(j).getMessage()))
                    {
                    events.add(t[i].get(j));
                    count++;
                    }

        return events;
        }


    /** Map of parameter -> index in the allParameters array. */
    static HashMap parametersToIndex = null;


    /** List of all M-audio Venom arp parameters in order. */
                
    final static String[] parameters = new String[]
    {
    "arpenable",
    "arpsource",
    "arpbank",
    "arppattern",
    "arpmode",
    "arpnoteorder",
    "arpoctaverange",
    "arpbipolar",
    "arplatchkeys",
    "arprootnote",
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

    public boolean testVerify(Synth other, String key, Object obj1, Object Obj2)
        {
        if (key.startsWith("pattern"))
            {
            int step = StringUtility.getFirstInt(key);
            return (model.get("pattern" + step + "mode") == MODE_END);
            }
        else return false;
        }

    public int getPauseAfterSendAllParameters() { return 750; }	

    public String[] getBankNames() { return BANKS; }

	/** Return a list of all patch number names.  Default is { "Main" } */
	public String[] getPatchNumberNames()  { return buildIntegerNames(128, 0); }

	/** Return a list whether patches in banks are writeable.  Default is { false } */
	public boolean[] getWriteableBanks() { return new boolean[] { false, true }; }

	/** Return a list whether individual patches can be written.  Default is FALSE. */
	public boolean getSupportsPatchWrites() { return true; }

	public int getPatchNameLength() { return 10; }

    public boolean librarianTested() { return true; }
    }
