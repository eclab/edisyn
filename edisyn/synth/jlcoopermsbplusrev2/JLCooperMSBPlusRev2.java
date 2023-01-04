/***
    Copyright 2021 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.jlcoopermsbplusrev2;

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
   A patch editor for the JL Cooper MSB Plus Rev 2. 
        
   @author Sean Luke
*/

public class JLCooperMSBPlusRev2 extends Synth
    {
    boolean overwrite = true;   // for the moment
    public static final String OVERWRITE_KEY = "rev2";
    
    JCheckBox rev2Check;
    
    boolean rev2;
    public static final String REV2_KEY = "rev2";
    
    public boolean isRev2() { return rev2; }
    public void setRev2(boolean val, boolean save)
        {
        if (save)
            {
            setLastX("" + (val), REV2_KEY, getSynthClassName(), true);
            }
        rev2 = val;
        rev2Check.setSelected(val);
        updateTitle();
        }

    public boolean getOverwrite() { return overwrite; }
    public void setOverwrite(boolean val)
        {
        setLastX("" + (val), OVERWRITE_KEY, getSynthClassName(), true);
        overwrite = val;
        }
    
    public String getTitleBarSynthName()
        {
        return rev2 ? "JL Cooper MSB Plus Rev 2" : "JL Cooper MSB Plus"; 
        }
        

    public JLCooperMSBPlusRev2()
        {
        if (parametersToIndex == null)
            {
            parametersToIndex = new HashMap();
            for(int i = 0; i < parameters.length; i++)
                {
                parametersToIndex.put(parameters[i], Integer.valueOf(i));
                }
            }
                                
        String m = getLastX(REV2_KEY, getSynthClassName());
        rev2 = (m == null ? true : Boolean.parseBoolean(m));

        m = getLastX(OVERWRITE_KEY, getSynthClassName());
        overwrite = (m == null ? true : Boolean.parseBoolean(m));

        /// SOUND PANEL
                
        JComponent soundPanel = new SynthPanel(this);
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        hbox.addLast(addControl(Style.COLOR_C()));
        vbox.add(hbox);
        vbox.add(addProcessor(1, Style.COLOR_A()));
        vbox.add(addProcessor(2, Style.COLOR_B()));
        vbox.add(addOutput(Style.COLOR_C()));
        vbox.add(addProgramChange(Style.COLOR_A()));

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("MSB", soundPanel);
                
        boolean r = isRev2();
        loadDefaults();                 // may reset isRev2()     
        setRev2(r, false);
        }
                
    
    public String getDefaultResourceFileName() { return "JLCooperMSBPlusRev2.init"; }
    public String getHTMLResourceFileName() { return "JLCooperMSBPlusRev2.html"; }
                
                
    public JFrame sprout()
        {
        JFrame frame = super.sprout();
        transmitParameters.setEnabled(false);
        transmitParameters.setSelected(false);
        transmitCurrent.setEnabled(false);
        addMSBPlusRev2Menu();
        return frame;
        }         

    public void setupLibrarianTwoWay()
        {
        int num = model.get("number");
        for(int i = 0; i < 64; i++)
            {
            int first = (i / 8);
            int second = (i % 8);
            if (first == second)
                {
                for (int j = 0; j < 8; j++)
                    {
                    model.set("output" + (j + 1) + "assignment", first);
                    }
                model.set("output" + (first + 1) + "assignment", 11);           // off
                }
            else
                {
                for (int j = 0; j < 8; j++)
                    {
                    model.set("output" + (j + 1) + "assignment", 11);               // off
                    }
                model.set("output" + (first + 1) + "assignment", second);
                model.set("output" + (second + 1) + "assignment", first);
                }
            model.set("number", i);
            sendToLibrarian(model);
            }
        model.set("number", num);
        }

    public void setupLibrarianMerge()
        {
        int num = model.get("number");
        for(int i = 0; i < 64; i++)
            {
            int first = (i / 8);
            int second = (i % 8);
            if (first == second)
                {
                for (int j = 0; j < 8; j++)
                    {
                    model.set("output" + (j + 1) + "assignment", 8);                // PROC A
                    }
                model.set("output" + (first + 1) + "assignment", 11);           // off
                model.set("proc" + 1 + "inputport", first);                     // first -> PROC A
                }
            else
                {
                for (int j = 0; j < 8; j++)
                    {
                    model.set("output" + (j + 1) + "assignment", 10);               // MERGE
                    }
                model.set("output" + (first + 1) + "assignment", 11);           // off
                model.set("output" + (second + 1) + "assignment", 11);  // off
                model.set("proc" + 1 + "inputport", first);                     // first -> PROC A
                model.set("proc" + 2 + "inputport", second);            // first -> PROC B
                }
            model.set("number", i);
            sendToLibrarian(model);
            }
        model.set("number", num);
        }

    public void addMSBPlusRev2Menu()
        {
        JMenu menu = new JMenu("MSB Plus");
        menubar.add(menu);

        final JCheckBoxMenuItem overwriteMenu = new JCheckBoxMenuItem("Overwrite Ports on Patch Write");
        overwriteMenu.setSelected(getOverwrite());
        overwriteMenu.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                setOverwrite(overwriteMenu.isSelected());
                }
            });
        menu.add(overwriteMenu);
        final JMenuItem twoWay = new JMenuItem("Send Two-Way Setups to Librarian");
        twoWay.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                if (showSimpleConfirm("Send Two-Way Setups to Librarian", "Overwrite all patches in the librarian?"))
                    setupLibrarianTwoWay();
                }
            });
        menu.add(twoWay);
        final JMenuItem merge = new JMenuItem("Send Merge Setups to Librarian");
        merge.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                if (showSimpleConfirm("Send Merge Setups to Librarian", "Overwrite all patches in the librarian?"))
                    setupLibrarianMerge();
                }
            });
        menu.add(merge);
        menubar.add(menu);
        }
        
    public JComponent addNameGlobal(Color color)
        {
        Category globalCategory = new Category(this, "JL Cooper MSB Plus / Rev 2", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
                
        VBox vbox = new VBox();
        HBox inner = new HBox();
        comp = new PatchDisplay(this, 4, false);
        inner.add(comp);
        inner.addLast(Strut.makeHorizontalStrut(20));
        vbox.add(inner);
        
        // The HTML is a hack to get the box sized right.  Thanks MacOS for nothing.
        rev2Check = new JCheckBox("<html>Rev&nbsp;2</html>");
        rev2Check.setSelected(rev2);
        rev2Check.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                setRev2(rev2Check.isSelected(), true);
                }
            });
        rev2Check.setFont(Style.SMALL_FONT());
        rev2Check.setOpaque(false);
        rev2Check.setForeground(Style.TEXT_COLOR());
        inner = new HBox();
        inner.add(rev2Check);
        //inner.addLast(Stretch.makeHorizontalStretch());
        vbox.add(inner);
        
        hbox.add(vbox);
        
        hbox.add(Strut.makeHorizontalStrut(170));

        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }

    public JComponent addControl(Color color)
        {
        Category category = new Category(this, "Control", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();

        comp = new LabelledDial("Sysex / PC", this, "processorinputport", color, 0, 7)  
            {
            public String map(int val)
                {
                return "" + (val + 1);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Input");
        hbox.add(comp);
                
        comp = new LabelledDial("Sysex", this, "sysexoutputport", color, 0, 7)  
            {
            public String map(int val)
                {
                return "" + (val + 1);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Output");
        hbox.add(comp);
        
        comp = new LabelledDial("PC MIDI", this, "processorinputchannel", color, 0, 15)        
            {
            public String map(int val)
                {
                return "" + (val + 1);
                }
            };
        hbox.add(comp);
        ((LabelledDial)comp).addAdditionalLabel("Channel");
            
        category.add(hbox, BorderLayout.WEST);
        return category;
        }

        
    public JComponent addProcessor(int proc, Color color)
        {
        Category category = new Category(this, "Processor " + (proc == 1 ? "A" : "B"), color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();

        comp = new CheckBox("Filter Notes", this, "proc" + proc + "filternotes");
        vbox.add(comp); 

        comp = new CheckBox("Filter Bend", this, "proc" + proc + "filterbend");
        vbox.add(comp); 

        comp = new CheckBox("Filter CC", this, "proc" + proc + "filtercc");
        vbox.add(comp); 
        hbox.add(vbox);

        vbox = new VBox();
        comp = new CheckBox("Filter Aftertouch", this, "proc" + proc + "filteraftertouch");
        vbox.add(comp); 

        comp = new CheckBox("Filter Real-time", this, "proc" + proc + "filterrealtime");
        vbox.add(comp); 

        comp = new CheckBox("Filter Sysex", this, "proc" + proc + "filtersysex");
        vbox.add(comp); 
        hbox.add(vbox);

        vbox = new VBox();
        comp = new CheckBox("Filter PC", this, "proc" + proc + "filterpc");
        vbox.add(comp); 
        hbox.add(vbox);

        comp = new LabelledDial("Input", this, "proc" + proc + "inputport", color, 0, 7)
            {
            public String map(int val)
                {
                return "" + (val + 1);
                }
            };
        hbox.add(comp);
                
        /// FIXME: This will have to be modified since positive and negative are reversed
        comp = new LabelledDial("Channel Bump", this, "proc" + proc + "channelbump", color, -7, 8)
            {
            public int getDefaultValue() { return 0; }
            public double getStartAngle() { return 216; }
            public String map(int val)
                {
                if (val == 0) return "--";
                else return "" + val;
                }
            };
        hbox.add(comp);
                
        comp = new LabelledDial("Transpose", this, "proc" + proc + "transpose", color, -59, 59)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val)
                {
                if (val == 0) return "--";
                else if (val % 12 == 0) return ("" + (val / 12) + " Oct");
                else return "" + val;
                }
            };
        hbox.add(comp);
        
        category.add(hbox, BorderLayout.WEST);
        return category;
        }
        
    public JComponent addOutput(Color color)
        {
        Category category = new Category(this, "Sources for Outputs", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();

        for(int i = 1; i <= 8; i++)
            {
            comp = new LabelledDial("Output " + i, this, "output" + i + "assignment", color, 0, 11) 
                {
                public String map(int val)
                    {
                    if (val < 8) return "" + (val + 1);
                    else if (val == 8) return "A";
                    else if (val == 9) return "B";
                    else if (val == 10) return "Merge";
                    else return "Off";
                    }
                };
            hbox.add(comp);
            }
        category.add(hbox, BorderLayout.WEST);
        return category;
        }

    public JComponent addProgramChange(Color color)
        {
        Category category = new Category(this, "Auto Program Change [Rev 2]", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();

        for(int i = 1; i <= 8; i++)
            {
            /// FIXME: It appears that the unit can go from 0 to 129!  Dunno what's up with that,
            /// nor how that translates here.
            comp = new LabelledDial("PC " + i, this, "port" + i + "pc", color, -1, 127)     
                {
                public int getDefaultValue() { return -1; }
                public String map(int val)
                    {
                    if (val == -1) return "Off";
                    else return "" + val;
                    }
                };
            hbox.add(comp);
            }
        vbox.add(hbox);
          
        hbox = new HBox();
        for(int i = 1; i <= 8; i++)
            {
            comp = new LabelledDial("Channel " + i, this, "port" + i + "pcchannel", color, 1, 16);
            hbox.add(comp);
            }
        vbox.add(hbox);

        category.add(vbox, BorderLayout.WEST);
        return category;
        }
    
    
    /// NOTE: We don't actually use this parameter list, but out of tradition
    /// we'll load it here anyway.

    /** Map of parameter -> index in the parameters array. */
    static HashMap parametersToIndex = null;


    final static String[] parameters = new String[] 
    {
    "proc1filternotes",
    "proc1filterbend",
    "proc1filtercc",
    "proc1filteraftertouch",
    "proc1filterpc",
    "proc1filtersysex",
    "proc1filterrealtime",
    "proc1channelbump",
    "proc1inputport",
    "proc1transpose",           // needs to be both semitones and direction
    "proc2filternotes",
    "proc2filterbend",
    "proc2filtercc",
    "proc2filteraftertouch",
    "proc2filterpc",
    "proc2filtersysex",
    "proc2filterrealtime",
    "proc2channelbump",
    "proc2inputport",
    "proc2transpose",           // needs to be both semitones and direction
    "processorinputport",
    "processorinputchannel",
    "output1assignment",
    "output2assignment",
    "output3assignment",
    "output4assignment",
    "output5assignment",
    "output6assignment",
    "output7assignment",
    "output8assignment",
    "sysexoutputport",
    "port1program",
    "port1channel",
    "port2program",
    "port2channel",
    "port3program",
    "port3channel",
    "port4program",
    "port4channel",
    "port5program",
    "port5channel",
    "port6program",
    "port6channel",
    "port7program",
    "port7channel",
    "port8program",
    "port8channel",
    };





    public byte getID() 
        { 
        try 
            { 
            byte b = (byte)(Byte.parseByte(tuple.id));
            if (b >= 1 && b <= 8) return b;
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
            if (b >= 1 && b <= 8) return "" + b;
            } 
        catch (NumberFormatException e) { }             // expected
        return "" + getID();
        }



    // READING AND WRITING

    public Object[] emitAll(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        if (tempModel == null)
            tempModel = getModel();
        int num = tempModel.get("number");
        
        /// It appears that sending to working memory on the MSB hangs the machine.  :-(
        /// So we cannot do that.  But we also can't use a scratch slot, as they're too
        /// valuable; and there's no independent parameter setting available.  So we will 
        /// simply return an empty byte string.  Very sad.
        
        if (toWorkingMemory) return new Object[0];              // ARGH

        byte[] bytes = new byte[toWorkingMemory ? (isRev2() ? 45 : 21) : (isRev2() ? 46 : 22)];
        
        bytes[0] = (byte)0xF0;
        bytes[1] = (byte)0x15;
        bytes[2] = (byte)0x0b;
        bytes[3] = (byte)(toWorkingMemory ? (isRev2() ? 0x14 : 0x04) : (isRev2() ? 0x12 : 0x02));
        if (!toWorkingMemory) bytes[4] = (byte)num;
        int pos = (toWorkingMemory ? 4 : 5);
                
                
        int inputport = model.get("processorinputport");
        int outputport = model.get("sysexoutputport");
        int channel = model.get("processorinputchannel");
                
        // We're always changing these 
        if (toWorkingMemory || !overwrite)
            {
            inputport = getID() - 1;
            outputport = getID() - 1;
            channel = getChannelOut();
            }
                
        for(int proc = 1; proc <= 2; proc++)
            {
            // Filter bits
            bytes[pos++] = (byte)((model.get("proc" + proc + "filternotes") << 0) | 
                (model.get("proc" + proc + "filterbend") << 1) | 
                (model.get("proc" + proc + "filtercc") << 2) | 
                (model.get("proc" + proc + "filteraftertouch") << 3) | 
                (model.get("proc" + proc + "filterpc") << 4) | 
                (model.get("proc" + proc + "filtersysex") << 5) | 
                (model.get("proc" + proc + "filterrealtime") << 6));
                                                        
            // channel bump and input port
            int channelbump = model.get("proc" + proc + "channelbump");
            if (channelbump < 0) channelbump += 16;
            bytes[pos++] = (byte)((channelbump << 0) | 
                (model.get("proc" + proc + "inputport") << 4));
                                                          
            // transposition
            int transpose = model.get("proc" + proc + "transpose");
            int negative = (transpose < 0 ? 1 : 0);
            transpose = Math.abs(transpose);
            bytes[pos++] = (byte)((transpose << 0) | 
                (negative << 6));
            }
                
        // processor input port and channel
        bytes[pos++] = (byte)((channel << 0) | 
            (inputport << 4));
                                        
        // output ports
        for(int port = 1; port <= 8; port++)
            {
            bytes[pos++] = (byte)model.get("output" + port + "assignment");
            }
                                                
        // sysex output
        bytes[pos++] = (byte)outputport;

        if (isRev2())
            {
            // program numbers and channels
            for(int port = 1; port <= 8; port++)
                {
                int prog = model.get("port" + port + "pc");
                if (prog < 0) prog = 255;               // unset
                bytes[pos++] = (byte)((prog >> 0) & 15);
                bytes[pos++] = (byte)((prog >> 4) & 15);
                int chan = model.get("port" + port + "pcchannel");
                if (chan == 16) chan = 0;       // channel 16 is represented by 0
                bytes[pos++] = (byte)chan;
                }
            }
                
        bytes[pos++] = (byte)0xF7;
        return new Object[] { bytes };
        }

    //public boolean getSendsParametersAfterNonMergeParse() { return true; }
    //public boolean getSendsParametersAfterWrite() { return true; }
               
    public int parse(byte[] data, boolean fromFile)
        {
        int pos = 4;
        if (data[3] == 0x12 || data[3] == 0x02)  
            {
            model.set("number", data[4]);
            pos = 5;
            }
        
        for(int proc = 1; proc <= 2; proc++)
            {
            // Filter bits
            model.set("proc" + proc + "filternotes", (data[pos] >>> 0) & 1);
            model.set("proc" + proc + "filterbend", (data[pos] >>> 1) & 1);
            model.set("proc" + proc + "filtercc", (data[pos] >>> 2) & 1);
            model.set("proc" + proc + "filteraftertouch", (data[pos] >>> 3) & 1);
            model.set("proc" + proc + "filterpc", (data[pos] >>> 4) & 1);
            model.set("proc" + proc + "filtersysex", (data[pos] >>> 5) & 1);
            model.set("proc" + proc + "filterrealtime", (data[pos] >>> 6) & 1);
            pos++;
                        
            // channel bump and input port
            int channelbump = ((data[pos] >>> 0) & 15);
            if (channelbump > 8) channelbump -= 16;
            model.set("proc" + proc + "channelbump", channelbump);
            model.set("proc" + proc + "inputport", (data[pos] >>> 4) & 7);
            pos++;

            // transposition
            int transpose = (data[pos] & 63);
            int negative = ((data[pos] >>> 6) & 1);
            if (negative == 1) transpose = 0 - transpose;
            model.set("proc" + proc + "transpose", transpose);
            pos++;
            }
                        
        // processor input port and channel
        model.set("processorinputchannel", (data[pos] >>> 0) & 15);
        model.set("processorinputport", (data[pos] >>> 4) & 7);
        pos++;
                
        // output ports
        for(int port = 1; port <= 8; port++)
            {
            model.set("output" + port + "assignment", data[pos]);
            pos++;
            }

        // sysex output -- it appears there is junk on the higher bits
        model.set("sysexoutputport", data[pos] & 7);
        pos++;
                
        if (data.length == 45 || data.length == 46)
            {
            for(int port = 1; port <= 8; port++)
                {
                int pc = (data[pos] & 15) | ((data[pos + 1] & 15) << 4);
                if (pc > 127) pc = -1;
                model.set("port" + port + "pc", pc);
                pos += 2;
                // channel 16 is represented by 0
                model.set("port" + port + "pcchannel", data[pos] == 0 ? 16 : data[pos]);
                pos++;
                }
            setRev2(true, false);
            }
        else
            {
            for(int port = 1; port <= 8; port++)
                {
                model.set("port" + port + "pc", -1);
                model.set("port" + port + "pcchannel", 1);
                }
            setRev2(false, false);
            }
                        
        // revise the output and input ports
        //model.set("sysexoutputport", getID() - 1);
        //model.set("processorinputport", getID() - 1);

        revise();       
        return PARSE_SUCCEEDED;     
        }

    public void changePatch(Model tempModel)
        {
        /*
          int num = tempModel.get("number");
          try
          {
          tryToSendMIDI(new ShortMessage(ShortMessage.PROGRAM_CHANGE, getChannelOut(), num, 0));
          }
          catch (Exception e) 
          { 
          e.printStackTrace(); 
          }
        */
        }

    public byte[] requestDump(Model tempModel)
        {
        if (tempModel == null)
            tempModel = getModel();
        int num = tempModel.get("number");
        if (isRev2())
            {
            return new byte[] { (byte)0xF0, 0x15, 0x0B, 0x13, (byte)num, (byte)0xF7 };
            }
        else
            {
            return new byte[] { (byte)0xF0, 0x15, 0x0B, 0x03, (byte)num, (byte)0xF7 };
            }
        }
    
    public byte[] requestCurrentDump()
        {
        if (isRev2())
            {
            return new byte[] { (byte)0xF0, 0x15, 0x0B, 0x15, (byte)0xF7 };
            }
        else
            {
            return new byte[] { (byte)0xF0, 0x15, 0x0B, 0x05, (byte)0xF7 };
            }
        }
    
        
    
    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        int num = model.get("number");
        JTextField number = new SelectedTextField("" + (num / 8 + 1) + (num % 8 + 1), 3);
                
        while(true)
            {
            boolean result = showMultiOption(this, new String[] { "Patch Number"}, 
                new JComponent[] { number }, title, "Enter the Patch number.");
                
            if (result == false) 
                return false;
                                
            int n;
            try { n = Integer.parseInt(number.getText()); }
            catch (NumberFormatException e)
                {
                showSimpleError(title, "The Patch Number must be an integer 11 ... 88, no 0 or 9 digits.");
                continue;
                }
            if (n < 11 || n > 88 || (n % 10 == 0) || (n % 10 == 9))
                {
                showSimpleError(title, "The Patch Number must be an integer 11 ... 88, no 0 or 9 digits.");
                continue;
                }
                                
            change.set("number", (n / 10 - 1) * 8 + (n % 10 - 1));
                        
            return true;
            }
        }

    public static String getSynthName() { return "JL Cooper MSB Plus / Rev 2"; }
    
    public String getPatchName(Model model) 
        {
        return "Patch " + getPatchLocationName(model);
        }
    
    public Model getNextPatchLocation(Model model)
        {
        int number = model.get("number");
        
        number++;
        if (number > 63)
            {
            number = 0;
            }
                
        Model newModel = buildModel();
        newModel.set("number", number);
        return newModel;
        }

    public boolean getAllowsTransmitsParameters()
        {
        return false;
        }

    public String getPatchLocationName(Model model)
        {
        // getPatchLocationName() is called from sprout() as a test to see if we should enable
        // batch downloading.  If we haven't yet created an .init file, then parameters won't exist
        // yet and this method will bomb badly.  So we return null in this case.
        if (!model.exists("number")) return null;
        
        int num = model.get("number", 0);
        return "" + (num / 8 + 1) + (num % 8 + 1);
        }


    /** Return a list of all patch number names.  Default is { "Main" } */
    public String[] getPatchNumberNames()  
        { 
        String[] names = new String[64];
        for(int i = 0; i < 64; i++)
            {
            names[i] = "" + (i / 8 + 1) + (i % 8 + 1);
            }
        return names;
        }       
        
    /** Return a list whether individual patches can be written.  Default is FALSE. */
    public boolean getSupportsPatchWrites() { return true; }

    public int getPatchNameLength() { return 9; }               // FIXME: what should I put here?

    /*
      public byte[] requestAllDump() 
      {
      return new byte[] { (byte)0xF0, 0x15, 0x0B, 0x11, (byte)0xF7 };
      }
    */

    public int getPauseAfterReceivePatch() { return 200; }
    public int getPauseAfterWritePatch() { return 200; }

    public int getBatchDownloadWaitTime() { return 200; }
        
    public boolean librarianTested() { return true; }
    
    public boolean testVerify(Synth other, String key, Object obj1, Object obj2)
        {
        if (!isRev2() && key.startsWith("port")) return true;
        else return false;
        }
    }
    
