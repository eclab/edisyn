/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.generic;

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

public class Generic extends Synth
    {

    public static final String[] CC_NAMES = {
        "Bank Select",
        "Modulation",
        "Breath",
        "",
        "Foot Controller",
        "Portamento Time",
        "Data Entry MSB",
        "Volume",

        "Balance",
        "",
        "Pan",
        "Expression",
        "Effect 1",
        "Effect 2",
        "",
        "",

        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",

        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",

        "Bank Selct LSB",
        "Modulation LSB",
        "Breath LSB",
        "",
        "Foot Cntrl LSB",
        "Porta Time LSB",
        "Data Entry LSB",
        "Volume LSB",

        "Balance LSB",
        "Pan LSB",
        "Expression LSB",
        "Effect 1 LSB",
        "Effect 2 LSB",
        "",
        "",
        "",

        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",

        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",

        "Damper/Sustain",
        "Portamento",
        "Sostenuto",
        "Soft Pedal",
        "Legato Footsw",
        "Hold 2",
        "Sound 1",
        "Sound 2",
        
        "Sound 3",
        "Sound 4",
        "Sound 5",
        "Sound 6",
        "Sound 7",
        "Sound 8",
        "Sound 9",
        "Sound 10",
        
        "",
        "",
        "",
        "",
        "Porta Ctrl",
        "",
        "",
        "",

        "HiRes Vel Pref",
        "",
        "",
        "Effect 1 Depth",
        "Effect 2 Depth",
        "Effect 3 Depth",
        "Effect 4 Depth",
        "Effect 5 Depth",
        
        "Data Increment",
        "Data Decrement",
        "NRPN LSB",
        "NRPN MSB",
        "RPN LSB",
        "RPN MSB",
        "",
        "",
        
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        
        "All Sound Off",
        "Reset Cntrlrs",
        "Local On/Off",
        "All Notes Off",
        "Omni Off",
        "Omni On",
        "Mono Mode",
        "Poly Mode"
        };


    ////// BELOW ARE DEFAULT IMPLEMENTATION OF COMMON METHODS THAT SYNTH EDITORS IMPLEMENT OR OVERRIDE.
    ////// If you do not need to implement or override a method, you should delete that method entirely
    ////// unless it is abstract, in which case, keep the default method described here.
        
        

    public Generic()
        {
        // Here you set up your interface.   You can look at other patch editors
        // to see how they were done.  At the very end you typically would say something like:
            
        JComponent soundPanel = new SynthPanel(this);
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        //hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        //vbox.add(hbox);
        vbox.add(addRawController(0, Style.COLOR_A()));
        soundPanel.add(vbox, BorderLayout.CENTER);
        tabs.addTab("CC 0-63", soundPanel);

        vbox = new VBox();
        vbox.add(addRawController(64, Style.COLOR_A()));
        soundPanel = new SynthPanel(this);
        soundPanel.add(vbox, BorderLayout.CENTER);
        tabs.addTab("CC 64-127", soundPanel);
        
        vbox = new VBox();
        vbox.add(addCustomController(Style.COLOR_B()));
        soundPanel = new SynthPanel(this);
        soundPanel.add(vbox, BorderLayout.CENTER);
        tabs.addTab("Custom CC", soundPanel);
        
        vbox = new VBox();
        vbox.add(addNRPN(Style.COLOR_B()));
        soundPanel = new SynthPanel(this);
        soundPanel.add(vbox, BorderLayout.CENTER);
        tabs.addTab("NRPN", soundPanel);
        
        vbox = new VBox();
        vbox.add(addRPN(Style.COLOR_B()));
        soundPanel = new SynthPanel(this);
        soundPanel.add(vbox, BorderLayout.CENTER);
        tabs.addTab("RPN", soundPanel);
        
        vbox = new VBox();
        vbox.add(addOther(Style.COLOR_B()));
        soundPanel = new SynthPanel(this);
        soundPanel.add(vbox, BorderLayout.CENTER);
        tabs.addTab("Other", soundPanel);
        
        /*
          vbox = new VBox();
          hbox.add(new Joystick(this));
          vbox.add(hbox);
          soundPanel = new SynthPanel(this);
          soundPanel.add(vbox, BorderLayout.CENTER);
          tabs.addTab("Joystick", soundPanel);
        */
        
        model.set("name", "MIDI Parameters");  // or whatever, to set the initial name of your patch (assuming you use "name" as the key for the patch name)
        //loadDefaults();                                 // this tells Edisyn to load the ".init" sysex file you created.  If you haven't set that up, it won't bother
        }

    /** Add the global patch category (name, id, number, etc.) */
/*
  public JComponent addNameGlobal(Color color)
  {
  Category globalCategory = new Category(this, "Generic", color);
                
  JComponent comp;
  String[] params;
  HBox hbox = new HBox();
  hbox.add(Strut.makeHorizontalStrut(100));
        
  globalCategory.add(hbox, BorderLayout.WEST);
  return globalCategory;
  }
*/

    public JComponent addCustomController(Color color)
        {
        Category category = new Category(this, "Custom CC", color);
        
        for(int i = 1; i <= 18; i++)
            {
            model.set("cust-cc-lsb-" + i, 0);
            model.setMin("cust-cc-lsb-" + i, 0);
            model.setMax("cust-cc-lsb-" + i, 1);
            }
        
        JComponent comp;
        String[] params;
        
        VBox main = new VBox();
        for(int j = 1; j < 18; j += 3)
            {
            HBox hbox = new HBox();
            for(int i = j; i < j + 3 ; i++)
                {
                final int _i = i;
                VBox vbox = new VBox();

                final LabelledDial msb = new LabelledDial("MSB", this, "cust-cc-value-" + i, color, 0, 127);
                        
                final LabelledDial msbalt = new LabelledDial("MSB", this, "cust-cc-value-alt-" + i, color, 0, 127);

                final CheckBox on = new CheckBox("On", this, "cust-cc-value-" + i)
                    {
                    public void update(String key, Model model) 
                        { 
                        if (model.get("cust-cc-lsb-" + _i) > 0 && model.get("cust-cc-param-" + _i) < 32)
                            {
                            getCheckBox().setSelected(getState() > 64 * 128); 
                            }
                        else
                            {
                            getCheckBox().setSelected(getState() > 64); 
                            }
                        }
                    };
                on.setMax(127);
                on.setState(on.getState());
 
                final CheckBox lsb = new CheckBox("+LSB", this, "cust-cc-lsb-" + i)
                    {
                    public void update(String key, Model model)
                        {
                        super.update(key, model);
                        if (model.get("cust-cc-lsb-" + _i) > 0 && model.get("cust-cc-param-" + _i) < 32)
                            {
                            msb.setLabel("MSB+LSB");
                            msbalt.setLabel("MSB+LSB");
                            msb.setMax(16383);
                            msbalt.setMax(16383);
                            }
                        else
                            {
                            msb.setLabel("MSB");
                            msbalt.setLabel("MSB");
                            msb.setMax(127);
                            msbalt.setMax(127);
                            }
                        }
                    };
                lsb.addToWidth(2);
               
                VBox vbox2 = new VBox();                                        
                comp = new LabelledDial("Parameter", this, "cust-cc-param-" + i, color, 0, 127)
                    {
                    public void update(String key, Model model)     
                        {
                        super.update(key, model);
                        lsb.setEnabled(model.get("cust-cc-param-" + _i) < 32);

                        if (model.get("cust-cc-lsb-" + _i) > 0 && model.get("cust-cc-param-" + _i) < 32)
                            {
                            msb.setLabel("MSB+LSB");
                            msbalt.setLabel("MSB+LSB");
                            msb.setMax(16383);
                            msbalt.setMax(16383);
                            }
                        else
                            {
                            msb.setLabel("MSB");
                            msbalt.setLabel("MSB");
                            msb.setMax(127);
                            msbalt.setMax(127);
                            }
                        }
                    };
                                        
                vbox2.add(comp);
                HBox hbox2 = new HBox();
                hbox2.add(vbox2);
                vbox2 = new VBox();
                                
                comp = lsb;
                ((CheckBox)comp).addToWidth(2);
                vbox2.add(comp);
                
                comp = on;
                vbox2.add(comp);
                hbox2.add(vbox2);
                vbox.add(hbox2);
                                
                comp = new StringComponent(null, this, "cust-cc-name-" + i, 6, "Name can be up to 12 long")
                    {
                    public String replace(String val)
                        {
                        return (val + "            ").substring(0,12).trim();
                        }

                    public String getCommand() { return "Enter CC Label up to 12 chars"; }
                    };
                model.set("cust-cc-name-" + i, "Name");
                vbox.add(comp);
                hbox.add(vbox);
                        
                vbox = new VBox();
                comp = msb;
                        
                vbox.add(comp);
                comp = new PushButton("Send") 
                    { 
                    public void perform() 
                        { 
                        getModel().set("cust-cc-value-" + _i, getModel().get("cust-cc-value-" + _i)); 
                        } 
                    };
                vbox.add(comp);
                hbox.add(vbox);

                vbox = new VBox();
                comp = msbalt;
                        
                vbox.add(comp);
                comp = new PushButton("Alt") 
                    { 
                    public void perform() 
                        { 
                        getModel().set("cust-cc-value-alt-" + _i, getModel().get("cust-cc-value-alt-" + _i)); 
                        } 
                    };
                vbox.add(comp);
                hbox.add(vbox);
                hbox.add(Strut.makeHorizontalStrut(40));
                }
            main.add(hbox);
            main.add(Strut.makeVerticalStrut(5));
            }
        
        category.add(main);
        return category;
        }


    public JComponent addNRPN(Color color)
        {
        Category category = new Category(this, "NRPN", color);
        
        for(int i = 1; i <= 18; i++)
            {
            model.set("cust-nrpn-lsb-" + i, 0);
            model.setMin("cust-nrpn-lsb-" + i, 0);
            model.setMax("cust-nrpn-lsb-" + i, 1);
            }
        
        
        JComponent comp;
        String[] params;
        
        VBox main = new VBox();
        for(int j = 1; j < 18; j += 3)
            {
            HBox hbox = new HBox();
            for(int i = j; i < j + 3 ; i++)
                {
                final int _i = i;
                VBox vbox = new VBox();

                final LabelledDial msb = new LabelledDial("MSB", this, "cust-nrpn-value-" + i, color, 0, 127);
                        
                final LabelledDial msbalt = new LabelledDial("MSB", this, "cust-nrpn-value-alt-" + i, color, 0, 127);

                final CheckBox on = new CheckBox("On", this, "cust-nrpn-value-" + i)
                    {
                    public void update(String key, Model model) 
                        { 
                        if (model.get("cust-nrpn-lsb-" + _i) > 0)
                            {
                            getCheckBox().setSelected(getState() > 64 * 128); 
                            }
                        else
                            {
                            getCheckBox().setSelected(getState() > 64); 
                            }
                        }
                    };
                on.setMax(127);
                on.setState(on.getState());
 
                final CheckBox lsb = new CheckBox("+LSB", this, "cust-nrpn-lsb-" + i)
                    {
                    public void update(String key, Model model)
                        {
                        super.update(key, model);
                        if (model.get("cust-nrpn-lsb-" + _i) > 0)
                            {
                            msb.setLabel("MSB+LSB");
                            msbalt.setLabel("MSB+LSB");
                            msb.setMax(16383);
                            msbalt.setMax(16383);
                            }
                        else
                            {
                            msb.setLabel("MSB");
                            msbalt.setLabel("MSB");
                            msb.setMax(127);
                            msbalt.setMax(127);
                            }
                        }
                    };
                lsb.addToWidth(2);
               
                VBox vbox2 = new VBox();                                        
                comp = new LabelledDial("Parameter", this, "cust-nrpn-param-" + i, color, 0, 16383)
                    {
                    public void update(String key, Model model)     
                        {
                        super.update(key, model);

                        if (model.get("cust-nrpn-lsb-" + _i) > 0)
                            {
                            msb.setLabel("MSB+LSB");
                            msbalt.setLabel("MSB+LSB");
                            msb.setMax(16383);
                            msbalt.setMax(16383);
                            }
                        else
                            {
                            msb.setLabel("MSB");
                            msbalt.setLabel("MSB");
                            msb.setMax(127);
                            msbalt.setMax(127);
                            }
                        }
                    };
                                        
                vbox2.add(comp);
                HBox hbox2 = new HBox();
                hbox2.add(vbox2);
                vbox2 = new VBox();
                                
                comp = lsb;
                ((CheckBox)comp).addToWidth(2);
                vbox2.add(comp);
                
                comp = on;
                vbox2.add(comp);
                hbox2.add(vbox2);
                vbox.add(hbox2);
                                
                comp = new StringComponent(null, this, "cust-nrpn-name-" + i, 6, "Name can be up to 12 long")
                    {
                    public String replace(String val)
                        {
                        return (val + "            ").substring(0,12).trim();
                        }

                    public String getCommand() { return "Enter NRPN Label up to 12 chars"; }
                    };
                model.set("cust-nrpn-name-" + i, "Name");
                vbox.add(comp);
                hbox.add(vbox);
                        
                vbox = new VBox();
                comp = msb;
                        
                vbox.add(comp);
                comp = new PushButton("Send") 
                    { 
                    public void perform() 
                        { 
                        getModel().set("cust-nrpn-value-" + _i, getModel().get("cust-nrpn-value-" + _i)); 
                        } 
                    };
                vbox.add(comp);
                hbox.add(vbox);

                vbox = new VBox();
                comp = msbalt;
                        
                vbox.add(comp);
                comp = new PushButton("Alt") 
                    { 
                    public void perform() 
                        { 
                        getModel().set("cust-nrpn-value-alt-" + _i, getModel().get("cust-nrpn-value-alt-" + _i)); 
                        } 
                    };
                vbox.add(comp);
                hbox.add(vbox);
                hbox.add(Strut.makeHorizontalStrut(40));
                }
            main.add(hbox);
            main.add(Strut.makeVerticalStrut(5));
            }
        
        category.add(main);
        return category;
        }


    public JComponent addRPN(Color color)
        {
        Category category = new Category(this, "RPN", color);
        
        for(int i = 1; i <= 18; i++)
            {
            model.set("cust-rpn-lsb-" + i, 0);
            model.setMin("cust-rpn-lsb-" + i, 0);
            model.setMax("cust-rpn-lsb-" + i, 1);
            }
        
        
        JComponent comp;
        String[] params;
        
        VBox main = new VBox();
        for(int j = 1; j < 18; j += 3)
            {
            HBox hbox = new HBox();
            for(int i = j; i < j + 3 ; i++)
                {
                final int _i = i;
                VBox vbox = new VBox();

                final LabelledDial msb = new LabelledDial("MSB", this, "cust-rpn-value-" + i, color, 0, 127);
                        
                final LabelledDial msbalt = new LabelledDial("MSB", this, "cust-rpn-value-alt-" + i, color, 0, 127);

                final CheckBox on = new CheckBox("On", this, "cust-rpn-value-" + i)
                    {
                    public void update(String key, Model model) 
                        { 
                        if (model.get("cust-rpn-lsb-" + _i) > 0)
                            {
                            getCheckBox().setSelected(getState() > 64 * 128); 
                            }
                        else
                            {
                            getCheckBox().setSelected(getState() > 64); 
                            }
                        }
                    };
                on.setMax(127);
                on.setState(on.getState());
 
                final CheckBox lsb = new CheckBox("+LSB", this, "cust-rpn-lsb-" + i)
                    {
                    public void update(String key, Model model)
                        {
                        super.update(key, model);
                        if (model.get("cust-rpn-lsb-" + _i) > 0)
                            {
                            msb.setLabel("MSB+LSB");
                            msbalt.setLabel("MSB+LSB");
                            msb.setMax(16383);
                            msbalt.setMax(16383);
                            }
                        else
                            {
                            msb.setLabel("MSB");
                            msbalt.setLabel("MSB");
                            msb.setMax(127);
                            msbalt.setMax(127);
                            }
                        }
                    };
                lsb.addToWidth(2);
               
                VBox vbox2 = new VBox();                                        
                comp = new LabelledDial("Parameter", this, "cust-rpn-param-" + i, color, 0, 16383)
                    {
                    public void update(String key, Model model)     
                        {
                        super.update(key, model);

                        if (model.get("cust-rpn-lsb-" + _i) > 0)
                            {
                            msb.setLabel("MSB+LSB");
                            msbalt.setLabel("MSB+LSB");
                            msb.setMax(16383);
                            msbalt.setMax(16383);
                            }
                        else
                            {
                            msb.setLabel("MSB");
                            msbalt.setLabel("MSB");
                            msb.setMax(127);
                            msbalt.setMax(127);
                            }
                        }
                    };
                                        
                vbox2.add(comp);
                HBox hbox2 = new HBox();
                hbox2.add(vbox2);
                vbox2 = new VBox();
                                
                comp = lsb;
                ((CheckBox)comp).addToWidth(2);
                vbox2.add(comp);
                
                comp = on;
                vbox2.add(comp);
                hbox2.add(vbox2);
                vbox.add(hbox2);
                                
                comp = new StringComponent(null, this, "cust-rpn-name-" + i, 6, "Name can be up to 12 long")
                    {
                    public String replace(String val)
                        {
                        return (val + "            ").substring(0,12).trim();
                        }

                    public String getCommand() { return "Enter RPN Label up to 12 chars"; }
                    };
                model.set("cust-rpn-name-" + i, "Name");
                vbox.add(comp);
                hbox.add(vbox);
                        
                vbox = new VBox();
                comp = msb;
                        
                vbox.add(comp);
                comp = new PushButton("Send") 
                    { 
                    public void perform() 
                        { 
                        getModel().set("cust-rpn-value-" + _i, getModel().get("cust-rpn-value-" + _i)); 
                        } 
                    };
                vbox.add(comp);
                hbox.add(vbox);

                vbox = new VBox();
                comp = msbalt;
                        
                vbox.add(comp);
                comp = new PushButton("Alt") 
                    { 
                    public void perform() 
                        { 
                        getModel().set("cust-rpn-value-alt-" + _i, getModel().get("cust-rpn-value-alt-" + _i)); 
                        } 
                    };
                vbox.add(comp);
                hbox.add(vbox);
                hbox.add(Strut.makeHorizontalStrut(40));
                }
            main.add(hbox);
            main.add(Strut.makeVerticalStrut(5));
            }
        
        category.add(main);
        return category;
        }


    public JComponent addRawController(final int offset, Color color)
        {
        Category category = new Category(this, (offset == 0 ? "CC 0-63" : "CC 64-127"), color);
                
        JComponent comp;
        String[] params;
        
        VBox main = new VBox();
        HBox hbox = new HBox();
        
        for(int j = 0; j < 64; j+=8)
            {
            hbox = new HBox();
            for(int i = j; i < j+8; i++)
                {
                final int _i = i;
                VBox vbox = new VBox();
                        
                VBox v = new VBox();
                HBox h = new HBox();
                comp = new CheckBox("On", this, "cc-" + (i + offset))
                    {
                    public void update(String key, Model model) { getCheckBox().setSelected(getState() > 64); }
                    };
                ((CheckBox)comp).setMax(127);
                ((CheckBox)comp).setState(((CheckBox)comp).getState());
                v.add(comp);
                comp = new PushButton("Send") 
                    { 
                    public void perform() 
                        { 
                        getModel().set("cc-" + _i, getModel().get("cc-" +  (_i + offset))); 
                        } 
                    };
                v.add(comp);
                h.add(v);
                vbox.add(h);
                comp = new LabelledDial(null, this, "cc-" + (i + offset), color, 0, 127);
                h.add(comp);


                comp = new StringComponent(null, this, "cc-name-" + (i + offset), 6, "Name can be up to 20 long")
                    {
                    public String replace(String val)
                        {
                        return (val + "                    ").substring(0,20).trim();
                        }
                    public String getTitle() { return "CC " +  (_i + offset); }
                    public String getCommand() { return "Enter CC Label up to 20 chars"; }
                    };
                vbox.add(comp);



                hbox.add(vbox);
                if (CC_NAMES[i + offset].equals(""))
                    model.set("cc-name-" + (i + offset), "CC " + (i + offset));
                else
                    model.set("cc-name-" + (i + offset), "CC " +  (i + offset) + " " + CC_NAMES[i + offset]);
                }
            main.add(hbox);
            }
        
        category.add(main);
        return category;
        }
        
    public JComponent addOther(Color color)
        {
        Category category = new Category(this, "Other", color);
                
        JComponent comp;
        String[] params;
        
        VBox main = new VBox();
        HBox hbox = new HBox();
        
        comp = new LabelledDial("Pitch Bend", this, "pitchbend", color, 0, 16383)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val)
                {
                return "" + (val - 8192);
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Pitch Bend", this, "pitchbendrange", color, 0, 127 * 100)
            {
            public String map(int val)
                {
                int semitones = (val / 100);
                int cents = (val % 100);
                return "" + semitones + "." + cents;
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Range");
        hbox.add(comp);


        comp = new LabelledDial("Coarse Tune", this, "coarsetune", color, 0, 127)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val)
                {
                return "" + (val - 64);
                }
            };
        hbox.add(comp);


        comp = new LabelledDial("Fine Tune", this, "finetune", color, 0, 16383)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val)
                {
                if (val == 0) return "-100";
                else if (val == 8192) return "--";
                else return String.format("%2.2f", (val - 8192) * 100.0 / 8192.0 );
                }
            };
        //Font font = ((LabelledDial)comp).getLabelFont();
        //float size = font.getSize2D();
        //((LabelledDial)comp).setLabelFont(font.deriveFont(size / 2));
        hbox.add(comp);
        
        category.add(hbox);
        return category;
        }
    
    public static final int HEADER = 12;
    public static boolean recognize(byte[] data)
        {
        return (data.length == HEADER + 1 + (18 * 19 * 3 + 128 * 21) + 7) &&
            data[0] == (byte)0xF0 &&
            data[1] == 0x7D &&
            data[2] =='E' &&
            data[3] =='D' &&
            data[4] =='I' &&
            data[5] =='S' &&
            data[6] =='Y' &&
            data[7] =='N' &&
            data[8] ==' ' &&
            data[9] =='C' &&
            data[10] =='C' &&
            data[11] == 0;                      // version number.  We recognize only 0
        }
        
    public int parse(byte[] data, boolean fromFile) 
        { 
        if (!fromFile) return PARSE_FAILED;
        
        int version = (int)data[HEADER - 1];
        if (version > 0)  // we don't know anything other than version 0
            return PARSE_FAILED;
                
        int pos = HEADER;
        
        for(int i = 1; i <= 18 ; i++)
            {
            model.set("cust-cc-param-" + i, ((data[pos++] << 7) | data[pos++]) );
            model.set("cust-cc-value-" + i, ((data[pos++] << 7) | data[pos++]) );
            model.set("cust-cc-value-alt-" + i, ((data[pos++] << 7) | data[pos++]) );
            model.set("cust-cc-lsb-" + i, (data[pos++]));
                        
            char[] name = new char[12];
            for(int j = 0; j < 12; j++)
                {
                name[j] = (char)(data[pos++]);
                }
            model.set("cust-cc-value-name-" + i, new String(name).trim());
            }

        for(int i = 1; i <= 18 ; i++)
            {
            model.set("cust-nrpn-param-" + i, ((data[pos++] << 7) | data[pos++]) );
            model.set("cust-nrpn-value-" + i, ((data[pos++] << 7) | data[pos++]) );
            model.set("cust-nrpn-value-alt-" + i, ((data[pos++] << 7) | data[pos++]) );
            model.set("cust-nrpn-lsb-" + i, (data[pos++]));
                        
            char[] name = new char[12];
            for(int j = 0; j < 12; j++)
                {
                name[j] = (char)(data[pos++]);
                }
            model.set("cust-nrpn-value-name-" + i, new String(name).trim());
            }
            
        for(int i = 1; i <= 18 ; i++)
            {
            model.set("cust-rpn-param-" + i, ((data[pos++] << 7) | data[pos++]) );
            model.set("cust-rpn-value-" + i, ((data[pos++] << 7) | data[pos++]) );
            model.set("cust-rpn-value-alt-" + i, ((data[pos++] << 7) | data[pos++]) );
            model.set("cust-rpn-lsb-" + i, (data[pos++]));
                        
            char[] name = new char[12];
            for(int j = 0; j < 12; j++)
                {
                name[j] = (char)(data[pos++]);
                }
            model.set("cust-rpn-value-name-" + i, new String(name).trim());
            }

        for(int i = 0; i < 128; i++)
            {
            model.set("cc-" + i, (data[pos++]));
            char[] name = new char[20];
            for(int j = 0; j < 20; j++)
                {
                name[j] = (char)(data[pos++]);
                }
            model.set("cc-name-" + i, new String(name).trim());
            }  
                
        model.set("pitchbend", (data[pos++] << 7) | data[pos++]);
        model.set("pitchbendrange", (data[pos++] << 7) | data[pos++]);
        model.set("coarsetune", (data[pos++]));
        model.set("finetune", (data[pos++] << 7) | data[pos++]);
               
        revise();
                
        return PARSE_SUCCEEDED;        
        }

    public byte[] emit(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        if (tempModel == null)
            tempModel = getModel();

        if (!toFile) return new byte[0];
        
        byte[] sysex = new byte[HEADER + 1 + (18 * 19 * 3 + 128 * 21) + 7];
        sysex[0] = (byte)0xF0;
        sysex[1] = (byte)0x7D;
        sysex[2] = (byte)'E';
        sysex[3] = (byte)'D';
        sysex[4] = (byte)'I';
        sysex[5] = (byte)'S';
        sysex[6] = (byte)'Y';
        sysex[7] = (byte)'N';
        sysex[8] = (byte)' ';
        sysex[9] = (byte)'C';
        sysex[10] = (byte)'C';
        sysex[11] = (byte)0;            // sysex version
        
        int pos = HEADER;
        
        for(int i = 1; i <= 18 ; i++)
            {
                {
                int val = model.get("cust-cc-param-" + i);
                byte msb = (byte)((val >>> 7) & 127);
                byte lsb = (byte)(val & 127);
                sysex[pos++] = msb;
                sysex[pos++] = lsb;
                }
                {
                int val = model.get("cust-cc-value-" + i);
                byte msb = (byte)((val >>> 7) & 127);
                byte lsb = (byte)(val & 127);
                sysex[pos++] = msb;
                sysex[pos++] = lsb;
                }
                {
                int val = model.get("cust-cc-value-alt-" + i);
                byte msb = (byte)((val >>> 7) & 127);
                byte lsb = (byte)(val & 127);
                sysex[pos++] = msb;
                sysex[pos++] = lsb;
                }
                        
                {
                int val = model.get("cust-cc-lsb-" + i);
                sysex[pos++] = (byte)val;
                }
                        
            String s = model.get("cust-cc-value-name-" + i,"") + "                ";
            for(int j = 0; j < 12; j++)
                {
                int val = (int)(s.charAt(j));
                sysex[pos++] = (byte) val;
                }
            }

        for(int i = 1; i <= 18 ; i++)
            {
                {
                int val = model.get("cust-nrpn-param-" + i);
                byte msb = (byte)((val >>> 7) & 127);
                byte lsb = (byte)(val & 127);
                sysex[pos++] = msb;
                sysex[pos++] = lsb;
                }
                {
                int val = model.get("cust-nrpn-value-" + i);
                byte msb = (byte)((val >>> 7) & 127);
                byte lsb = (byte)(val & 127);
                sysex[pos++] = msb;
                sysex[pos++] = lsb;
                }
                {
                int val = model.get("cust-nrpn-value-alt-" + i);
                byte msb = (byte)((val >>> 7) & 127);
                byte lsb = (byte)(val & 127);
                sysex[pos++] = msb;
                sysex[pos++] = lsb;
                }
                {
                int val = model.get("cust-nrpn-lsb-" + i);
                sysex[pos++] = (byte) val;
                }
            String s = model.get("cust-nrpn-value-name-" + i,"") + "                ";
            for(int j = 0; j < 12; j++)
                {
                int val = (int)(s.charAt(j));
                sysex[pos++] = (byte) val;
                }
            }
            
        for(int i = 1; i <= 18 ; i++)
            {
                {
                int val = model.get("cust-rpn-param-" + i);
                byte msb = (byte)((val >>> 7) & 127);
                byte lsb = (byte)(val & 127);
                sysex[pos++] = msb;
                sysex[pos++] = lsb;
                }
                {
                int val = model.get("cust-rpn-value-" + i);
                byte msb = (byte)((val >>> 7) & 127);
                byte lsb = (byte)(val & 127);
                sysex[pos++] = msb;
                sysex[pos++] = lsb;
                }
                {
                int val = model.get("cust-rpn-value-alt-" + i);
                byte msb = (byte)((val >>> 7) & 127);
                byte lsb = (byte)(val & 127);
                sysex[pos++] = msb;
                sysex[pos++] = lsb;
                }
                {
                int val = model.get("cust-rpn-lsb-" + i);
                sysex[pos++] = (byte) val;
                }
            String s = model.get("cust-rpn-value-name-" + i,"") + "                ";
            for(int j = 0; j < 12; j++)
                {
                int val = (int)(s.charAt(j));
                sysex[pos++] = (byte) val;
                }
            }            

        for(int i = 0; i < 128; i++)
            {
                {
                int val = model.get("cc-" + i);
                sysex[pos++] = (byte) val;
                }
                {
                String s = model.get("cc-name-" + i,"") + "                    ";
                for(int j = 0; j < 20; j++)
                    {
                    int val = (int)(s.charAt(j));
                    sysex[pos++] = (byte) val;
                    }
                }  
            }
        
        int val = model.get("pitchbend");
        sysex[pos++] = (byte) (val >>> 7);
        sysex[pos++] = (byte) (val & 127);
        val = model.get("pitchbendrange");
        sysex[pos++] = (byte) (val >>> 7);
        sysex[pos++] = (byte) (val & 127);
        val = model.get("coarsetune");
        sysex[pos++] = (byte) val;
        val = model.get("finetune");
        sysex[pos++] = (byte) (val >>> 7);
        sysex[pos++] = (byte) (val & 127);
               
        sysex[sysex.length - 1] = (byte)0xF7;           
        
        return sysex;
        }
        
    
    public Object[] buildRPN(int channel, int parameter, int value)
        {
        try
            {
            int p_msb = (parameter >>> 7);
            int p_lsb = (parameter & 127);
            int v_msb = (value >>> 7);
            int v_lsb = (value & 127);
            if (v_msb > 127) 
                {
                System.err.println("Problem with " + value);
                Synth.handleException(new Throwable());
                }
        
            return new Object[]
                {
                new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, 101, (byte)p_msb),
                new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, 100, (byte)p_lsb),
                new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, 6, (byte)v_msb),
                new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, 38, (byte)v_lsb),
                };
            }
        catch (InvalidMidiDataException e)
            {
            Synth.handleException(e);
            return new Object[0];
            }
        }

    public Object[] emitAll(String key)
        {
        if (key.equals("pitchbend"))
            {
            int val = model.get("pitchbend", 0);
            try
                {
                return new Object[] { new ShortMessage(ShortMessage.PITCH_BEND, getChannelOut(), (byte)(val >> 7) & 127 , (byte)val & 127) };
                }
            catch (InvalidMidiDataException ex)
                {
                Synth.handleException(ex); 
                return new Object[0];
                }
            }
        else if (key.equals("pitchbendrange"))
            {
            int val = model.get("pitchbendrange", 0);
            int cents = val % 100;
            int semitones = val / 100;
            return buildRPN(getChannelOut(), 0, cents + semitones * 128);
            }
        else if (key.equals("finetune"))
            {
            return buildRPN(getChannelOut(), 1, model.get("finetune", 0));
            }
        else if (key.equals("coarsetune"))
            {
            return buildRPN(getChannelOut(), 2, model.get("coarsetune", 0));
            }
        else if (key.startsWith("cc-") && !(key.startsWith("cc-name-")))
            {
            int cc = StringUtility.getInt(key);
            return buildCC(getChannelOut(), cc, model.get(key));
            }
        else if (key.startsWith("cust-cc-value-alt-"))
            {
            int param = StringUtility.getInt(key);
            int val = model.get(key);
            if (model.get("cust-cc-param-" + param) > 32 ||
                model.get("cust-cc-lsb-" + param) == 0)  // just 127
                {
                return buildCC(getChannelOut(), model.get("cust-cc-param-" + param), val);
                }
            else
                {
                return buildLongCC(getChannelOut(), model.get("cust-cc-param-" + param), val);
                }
            }
        else if (key.startsWith("cust-cc-value-"))
            {
            int param = StringUtility.getInt(key);
            int val = model.get(key);
            if (model.get("cust-cc-param-" + param) > 32 ||
                model.get("cust-cc-lsb-" + param) == 0)  // just 127
                {
                return buildCC(getChannelOut(), model.get("cust-cc-param-" + param), val);
                }
            else
                {
                return buildLongCC(getChannelOut(), model.get("cust-cc-param-" + param), val);
                }
            }
        else if (key.startsWith("cust-nrpn-value-alt-"))
            {
            int param = StringUtility.getInt(key);
            int val = model.get(key);
            if (model.get("cust-nrpn-lsb-" + param) == 0)  // just 127
                {
                return buildNRPN(getChannelOut(), model.get("cust-nrpn-param-" + param), val * 128);
                }
            else
                {
                return buildNRPN(getChannelOut(), model.get("cust-nrpn-param-" + param), val);
                }
            }
        else if (key.startsWith("cust-nrpn-value-"))
            {
            int param = StringUtility.getInt(key);
            int val = model.get(key);
            if (model.get("cust-nrpn-lsb-" + param) == 0)  // just 127
                {
                return buildNRPN(getChannelOut(), model.get("cust-nrpn-param-" + param), val * 128);
                }
            else
                {
                return buildNRPN(getChannelOut(), model.get("cust-nrpn-param-" + param), val);
                }
            }
        else if (key.startsWith("cust-rpn-value-alt-"))
            {
            int param = StringUtility.getInt(key);
            int val = model.get(key);
            if (model.get("cust-rpn-lsb-" + param) == 0)  // just 127
                {
                return buildNRPN(getChannelOut(), model.get("cust-rpn-param-" + param), val * 128);
                }
            else
                {
                return buildNRPN(getChannelOut(), model.get("cust-rpn-param-" + param), val);
                }
            }
        else if (key.startsWith("cust-rpn-value-"))
            {
            int param = StringUtility.getInt(key);
            int val = model.get(key);
            if (model.get("cust-rpn-lsb-" + param) == 0)  // just 127
                {
                return buildNRPN(getChannelOut(), model.get("cust-rpn-param-" + param), val * 128);
                }
            else
                {
                return buildNRPN(getChannelOut(), model.get("cust-rpn-param-" + param), val);
                }
            }
        else return new Object[0];
        }

    public boolean sendAllSoundsOffWhenWindowChanges()
        {
        return false;
        }


    public static String getSynthName() 
        { 
        return "MIDI Parameters"; 
        }

    public JFrame sprout()
        {
        JFrame frame = super.sprout();
        writeTo.setEnabled(false);
        transmitTo.setEnabled(false);
        transmitCurrent.setEnabled(false);
        receivePatch.setEnabled(false);
        receiveCurrent.setEnabled(false);
        merge.setEnabled(false);
        hillClimbMenu.setEnabled(false);
        return frame;
        }

    public String getHTMLResourceFileName() { return "Generic.html"; }
    }
