/***
    Copyright 2022 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.rolandalphajuno;

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
   A patch editor for the Roland Alpha Juno 1, Alpha Juno 2, and MKS-50
        
   @author Sean Luke
*/

public class RolandAlphaJuno extends Synth
    {
    public static final String[] BANKS = { 
        "Preset 1", "Preset 2", "Preset 3", "Preset 4", "Preset 5", "Preset 6", "Preset 7", "Preset 8", 
        "Memory 1", "Memory 2", "Memory 3", "Memory 4", "Memory 5", "Memory 6", "Memory 7", "Memory 8", 
        "Cartridge 1", "Cartridge 2", "Cartridge 3", "Cartridge 4", "Cartridge 5", "Cartridge 6", "Cartridge 7", "Cartridge 8" };
    public static final String[] DCO_ENV_MODES = { "Normal", "Inverted", "Normal with Dynamics", "Inverted with Dynamics" };
    public static final String[] VCF_ENV_MODES = { "Normal", "Inverted", "Normal with Dynamics" };
    public static final String[] VCA_ENV_MODES = { "Envelope", "Gate", "Envelope with Dynamics", "Gate with Dynamics" };
    public static final String[] DCO_RANGES = { "4'", "8'", "16'", "32'" };

    public static final ImageIcon[] SQUARE_ICONS = 
        {
        new ImageIcon(),
        new ImageIcon(RolandAlphaJuno.class.getResource("Square1.png")),
        new ImageIcon(RolandAlphaJuno.class.getResource("Square2.png")),
        new ImageIcon(RolandAlphaJuno.class.getResource("Square3.png")),
        };
        
    public static final String[] SQUARE_TEXT = { "Off  0", "1 ", "2 ", "3 " }; 

    public static final ImageIcon[] SAWTOOTH_ICONS = 
        {
        new ImageIcon(),
        new ImageIcon(RolandAlphaJuno.class.getResource("Saw1.png")),
        new ImageIcon(RolandAlphaJuno.class.getResource("Saw2.png")),
        new ImageIcon(RolandAlphaJuno.class.getResource("Saw3.png")),
        new ImageIcon(RolandAlphaJuno.class.getResource("Saw4.png")),
        new ImageIcon(RolandAlphaJuno.class.getResource("Saw5.png")),
        };
        
    public static final String[] SAWTOOTH_TEXT = { "Off  0", "1 ", "2 ", "3 ", "4 ", "5 " }; 

    public static final ImageIcon[] SUB_ICONS = 
        {
        new ImageIcon(RolandAlphaJuno.class.getResource("Square1.png")),
        new ImageIcon(RolandAlphaJuno.class.getResource("Square2.png")),
        new ImageIcon(RolandAlphaJuno.class.getResource("Sub2.png")),
        new ImageIcon(RolandAlphaJuno.class.getResource("Sub3.png")),
        new ImageIcon(RolandAlphaJuno.class.getResource("Sub4.png")),
        new ImageIcon(RolandAlphaJuno.class.getResource("Sub5.png")),
        };

    public static final String[] SUB_TEXT = { "0 ", "1 ", "2 ", "3 ", "4 ", "5 " };             // get it?  subtext?
        
    public RolandAlphaJuno()
        {
        if (allParametersToIndex == null)
            {
            allParametersToIndex = new HashMap();
            for(int i = 0; i < allParameters.length; i++)
                {
                allParametersToIndex.put(allParameters[i], Integer.valueOf(i));
                }
            }

        /// SOUND PANEL
        /*
          SynthPanel soundPanel = new SynthPanel(this);
          VBox vbox = new VBox();
          HBox hbox = new HBox();
          vbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
          vbox.add(addOscillators(Style.COLOR_A()));
          //vbox.add(hbox);
        
          vbox.add(addFilter(Style.COLOR_A()));
          hbox = new HBox();
          hbox.add(addAmplifier(Style.COLOR_A()));
          hbox.addLast(addLFO(Style.COLOR_C()));
          vbox.add(hbox);
          vbox.add(addEnvelope(Style.COLOR_C()));
                
          soundPanel.add(vbox, BorderLayout.CENTER);
          addTab("Everything", soundPanel);
        */

        SynthPanel soundPanel = new SynthPanel(this);
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        hbox.addLast(addAmplifier(Style.COLOR_A()));
        vbox.add(hbox);

        vbox.add(addOscillators(Style.COLOR_A()));

        hbox = new HBox();
        hbox.add(addFilter(Style.COLOR_A()));
        hbox.addLast(addLFO(Style.COLOR_C()));
        vbox.add(hbox);

        vbox.add(addEnvelope(Style.COLOR_C()));
                
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Everything", soundPanel);
                
        model.set("name", "UNTITLED");
        model.set("bank", 0);
        model.set("number", 0);
        
        loadDefaults();        
        }
                
    public String getDefaultResourceFileName() { return "RolandAlphaJuno.init"; }
    public String getHTMLResourceFileName() { return "RolandAlphaJuno.html"; }

    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        JComboBox bank = new JComboBox(BANKS);
        bank.setEditable(false);
        bank.setMaximumRowCount(32);
        bank.setSelectedIndex(model.get("bank"));

        JTextField number = new SelectedTextField("" + (model.get("number") < 10 ? "0" : "") + (model.get("number") + 1), 3);
        
        while(true)
            {
            boolean result = showMultiOption(this, new String[] { "Bank", "Patch Number"}, 
                new JComponent[] { bank, number }, title, "Enter the Bank and Patch Number");
                
            if (result == false) 
                return false;
                                
            int n;
            try { n = Integer.parseInt(number.getText()); }
            catch (NumberFormatException e)
                {
                showSimpleError(title, "The Patch Number must be an integer 1-8");
                continue;
                }
            if (n < 1 || n > 8)
                {
                showSimpleError(title, "The Patch Number must be an integer 1-8");
                continue;
                }
                                
            change.set("bank", bank.getSelectedIndex());
            change.set("number", n - 1);
                        
            return true;
            }
        }
        
    public JFrame sprout()
        {
        JFrame frame = super.sprout();
        //receiveCurrent.setEnabled(false);
        return frame;
        }         

    /** Add the global patch category (name, id, number, etc.) */
    public JComponent addNameGlobal(Color color)
        {
        Category globalCategory = new Category(this, getSynthName(), color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();

        VBox outer = new VBox();
        comp = new PatchDisplay(this, 6);
        hbox.add(comp);
        hbox.addLast(Strut.makeHorizontalStrut(200));
        outer.add(hbox);
        
        
        hbox = new HBox();
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
        hbox.add(comp);
        outer.add(hbox);

        globalCategory.add(outer, BorderLayout.WEST);
        return globalCategory;
        }

    public JComponent addOscillators(Color color)
        {
        Category category = new Category(this, "Oscillators", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        VBox outer = new VBox();
        HBox inner = new HBox();
                
        params = DCO_ENV_MODES;
        comp = new Chooser("Envelope Mode", this, "dcoenvmode", params);
        outer.add(comp);

        params = SAWTOOTH_TEXT;
        comp = new Chooser("Sawtooth Wave", this, "dcowaveformsawtooth", params, SAWTOOTH_ICONS);
        vbox.add(comp);

        params = SQUARE_TEXT;
        comp = new Chooser("Square Wave", this, "dcowaveformpulse", params, SQUARE_ICONS);
        vbox.add(comp);
        inner.add(vbox);
        
        vbox = new VBox();

        // FIXME: This hack stretches the chooser so it doesn't do "..." on some pictures...
        params = SUB_TEXT;
        comp = new Chooser("Sub Wave             ", this, "dcowaveformsub", params, SUB_ICONS);
        vbox.add(comp);

        params = DCO_RANGES;
        comp = new Chooser("Range", this, "dcorange", params);
        vbox.add(comp);

        inner.add(vbox);
        outer.addLast(inner);

        hbox.add(outer);

/*
  inner = new HBox();
  vbox = new VBox();
                
  comp = new LabelledDial("Sub", this, "dcosublevel", color, 0, 3);
  ((LabelledDial)comp).addAdditionalLabel("Level");
  inner.add(comp);

  comp = new LabelledDial("Noise", this, "dconoiselevel", color, 0, 3);
  ((LabelledDial)comp).addAdditionalLabel("Level");
  inner.add(comp);

  comp = new LabelledDial("PW/PWM", this, "dcopwpwmdepth", color, 0, 127);
  ((LabelledDial)comp).addAdditionalLabel("Depth");
  inner.add(comp);

  comp = new LabelledDial("PWM", this, "dcopwmrate", color, 0, 127)
  {
  public String map(int value)
  {
  if (value == 0) return "Fixed";
  else return "" + value;
  }
  };
  ((LabelledDial)comp).addAdditionalLabel("Rate");
  inner.add(comp);
        
  vbox.add(inner);
                
  inner = new HBox();
  comp = new LabelledDial("Pitch Bend", this, "benderrange", color, 0, 12);
  ((LabelledDial)comp).addAdditionalLabel("Range");
  inner.add(comp);

  comp = new LabelledDial("LFO", this, "dcolfomoddepth", color, 0, 127);
  ((LabelledDial)comp).addAdditionalLabel("-> DCO");
  inner.add(comp);

  comp = new LabelledDial("Env", this, "dcoenvmoddepth", color, 0, 127);
  ((LabelledDial)comp).addAdditionalLabel("-> DCO");
  inner.add(comp);

  comp = new LabelledDial("Pressure", this, "dcoafterdepth", color, 0, 127);
  ((LabelledDial)comp).addAdditionalLabel("-> DCO");
  inner.add(comp);
  vbox.add(inner);
  hbox.add(vbox);
*/

        comp = new LabelledDial("Sub", this, "dcosublevel", color, 0, 3);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);

        comp = new LabelledDial("Noise", this, "dconoiselevel", color, 0, 3);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);

        comp = new LabelledDial("PW/PWM", this, "dcopwpwmdepth", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Depth");
        hbox.add(comp);

        comp = new LabelledDial("PWM", this, "dcopwmrate", color, 0, 127)
            {
            public String map(int value)
                {
                if (value == 0) return "Fixed";
                else return "" + value;
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Rate");
        hbox.add(comp);
        
        comp = new LabelledDial("Pitch Bend", this, "benderrange", color, 0, 12);
        ((LabelledDial)comp).addAdditionalLabel("Range");
        hbox.add(comp);

        comp = new LabelledDial("LFO", this, "dcolfomoddepth", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("-> DCO");
        hbox.add(comp);

        comp = new LabelledDial("Env", this, "dcoenvmoddepth", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("-> DCO");
        hbox.add(comp);

        comp = new LabelledDial("Pressure", this, "dcoafterdepth", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("-> DCO");
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
                


    public JComponent addFilter(Color color)
        {
        Category category = new Category(this, "Filter", color);
                
        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        params = VCF_ENV_MODES;
        comp = new Chooser("Envelope Mode", this, "vcfenvmode", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("High Pass", this, "hpfcutofffreq", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Cutoff");
        hbox.add(comp);
                
        comp = new LabelledDial("Cutoff", this, "vcfcutofffreq", color, 0, 127);
        hbox.add(comp);
                
        comp = new LabelledDial("Resonance", this, "vcfresonance", color, 0, 127);
        hbox.add(comp);
                
        comp = new LabelledDial("LFO", this, "vcflfomoddepth", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("-> VCF");
        hbox.add(comp);

        comp = new LabelledDial("Envelope", this, "vcfenvmoddepth", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("-> VCF");
        hbox.add(comp);

        comp = new LabelledDial("Keyboard", this, "vcfkeyfollow", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("-> VCF");
        hbox.add(comp);

        comp = new LabelledDial("Pressure", this, "vcfafterdepth", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("-> VCF");
        hbox.add(comp);
        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }        
        
    public JComponent addAmplifier(Color color)
        {
        Category category = new Category(this, "Amplifier", color);
                
        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        params = VCA_ENV_MODES;
        comp = new Chooser("Envelope Mode", this, "vcaenvmode", params);
        vbox.add(comp);

        comp = new CheckBox("Chorus", this, "chorus");
        vbox.add(comp);

        hbox.add(vbox);

        comp = new LabelledDial("Level", this, "vcalevel", color, 0, 127);
        hbox.add(comp);
                
        comp = new LabelledDial("Pressure", this, "vcaafterdepth", color, 0, 31);
        ((LabelledDial)comp).addAdditionalLabel("-> VCA");
        hbox.add(comp);

        comp = new LabelledDial("Chorus", this, "chorusrate", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Rate");
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addEnvelope(Color color)
        {
        Category category = new Category(this, "Envelope", color);
                
        JComponent comp;
        String[] params;
        final HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        comp = new LabelledDial("Time 1", this, "envt1", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("(Attack)");
        hbox.add(comp);
                
        comp = new LabelledDial("Level 1", this, "envl1", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("(Attack)");
        hbox.add(comp);
                
        comp = new LabelledDial("Time 2", this, "envt2", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("(Decay)");
        hbox.add(comp);
                
        comp = new LabelledDial("Level 2", this, "envl2", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("(Decay)");
        hbox.add(comp);
                
        comp = new LabelledDial("Time 3", this, "envt3", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("(Decay 2)");
        hbox.add(comp);
                
        comp = new LabelledDial("Level 3", this, "envl3", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("(Sustain)");
        hbox.add(comp);
                
        comp = new LabelledDial("Time 4", this, "envt4", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("(Release)");
        hbox.add(comp);
                
        comp = new LabelledDial("Keyboard", this, "envkeyfollow", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("-> Env");
        hbox.add(comp);
                
        comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, "envt1", "envt2", "envt3", null, "envt4" },
            new String[] { null, "envl1", "envl2", "envl3", "envl3", null },
            new double[] { 0, 0.2 / 127, 0.2 / 127, 0.2 / 127, 0.2, 0.2 / 127},
            new double[] { 0, 1.0 / 127, 1.0 / 127, 1.0 / 127, 1.0 / 127, 0 });
        hbox.addLast(comp);
        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent addLFO(Color color)
        {
        Category category = new Category(this, "LFO", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();

        comp = new LabelledDial("Rate", this, "lforate", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Delay", this, "lfodelaytime", color, 0, 127);
        hbox.add(comp);
                
        category.add(hbox, BorderLayout.WEST);
        return category;
        }


    /** Map of parameter -> index in the allParameters array. */
    static HashMap allParametersToIndex = null;

                
    final static String[] allParameters = new String[]
    {
    "dcoenvmode", 
    "vcfenvmode", 
    "vcaenvmode", 
    "dcowaveformpulse", 
    "dcowaveformsawtooth", 
    "dcowaveformsub", 
    "dcorange", 
    "dcosublevel", 
    "dconoiselevel", 
    "hpfcutofffreq", 
    "chorus", 
    "dcolfomoddepth", 
    "dcoenvmoddepth", 
    "dcoafterdepth", 
    "dcopwpwmdepth", 
    "dcopwmrate", 
    "vcfcutofffreq", 
    "vcfresonance", 
    "vcflfomoddepth", 
    "vcfenvmoddepth", 
    "vcfkeyfollow", 
    "fcfafterdepth", 
    "vcalevel", 
    "vcaafterdepth", 
    "lforate",
    "lfodelaytime", 
    "envt1", 
    "envl1", 
    "envt2", 
    "envl2", 
    "envt3", 
    "envl3", 
    "envt4", 
    "envkeyfollow", 
    "chorusrate", 
    "benderrange", 
    };
    
    
    


/*
  public byte[] emit(String key)
  {
  if (key.equals("bank")) return new byte[0];  // this is not emittable
  if (key.equals("number")) return new byte[0];  // this is not emittable

  int index;
  int value;
        
  if (key.equals("name"))
  {
  return new byte[0];  // ignore
  }
  else if (key.equals("env1lfotriggermode") || key.equals("env2lfotriggermode") || key.equals("env3lfotriggermode"))
  {
  index = ((Integer)(internalParametersToIndex.get(key))).intValue();
  value = model.get(key);
  // convert
  if (value >= 1) value = value + 1;  // there is no value = 1, that's the same as value = 0
  }
  else if (key.equals("dco1bend") || key.equals("dco1vibrato"))
  {
  index = ((Integer)(internalParametersToIndex.get("dco1fixedmods1"))).intValue();
  value = model.get("dco1bend") |  (model.get("dco1vibrato") << 1);
  }
  else if (key.equals("dco1portamento"))
  {
  index = ((Integer)(internalParametersToIndex.get("dco1fixedmods2"))).intValue();
  value = model.get("dco1portamento");
  }
  else if (key.equals("dco2bend") || key.equals("dco2vibrato"))
  {
  index = ((Integer)(internalParametersToIndex.get("dco2fixedmods1"))).intValue();
  value = model.get("dco2bend") | (model.get("dco2vibrato") << 1);
  }
  else if (key.equals("dco2portamento") || key.equals("dco2keytracking"))
  {
  index = ((Integer)(internalParametersToIndex.get("dco2fixedmods2"))).intValue();
  value = model.get("dco2portamento") | (model.get("dco2keytracking") << 1);
  }
  else if (key.equals("dco1wave") || key.equals("dco1pulse"))
  {
  index = ((Integer)(internalParametersToIndex.get("dco1waveenable"))).intValue();
  value = model.get("dco1pulse") | (model.get("dco1wave") << 1);
  }
  else if (key.equals("dco2wave") || key.equals("dco2pulse") || key.equals("dco2noise"))
  {
  index = ((Integer)(internalParametersToIndex.get("dco2waveenable"))).intValue();
  value = model.get("dco2pulse") | (model.get("dco2wave") << 1) | (model.get("dco2noise") << 2);
  }
  else if (key.equals("vcfbend") || key.equals("vcfvibrato"))
  {
  index = ((Integer)(internalParametersToIndex.get("vcffixedmods1"))).intValue();
  value = model.get("vcfbend") | (model.get("vcfvibrato") << 1);
  }
  else if (key.equals("vcfportamento") || key.equals("vcfkeytracking"))
  {
  index = ((Integer)(internalParametersToIndex.get("vcffixedmods2"))).intValue();
  value = model.get("vcfportamento") | (model.get("vcfkeytracking") << 1);
  }
  else if (key.equals("dco2detune"))
  {
  index = ((Integer)(internalParametersToIndex.get(key))).intValue();
  value = model.get(key) & 127;  // sign-extend to 7th bit only
  }
  else if (key.startsWith("mod"))
  {
  int modnumber = (int)(key.charAt(3) - '0');
  if (key.charAt(4) == '0') // it's 10
  modnumber = 10;

  int modsource = model.get("mod" + modnumber  + "source");
  int moddestination = model.get("mod" + modnumber  + "destination");
  int modamount = model.get("mod" + modnumber  + "amount") & 127;

  // if one is "None", then the other must be as well            
  if (modsource == 0) moddestination = 0;
  else if (moddestination == 0) modsource = 0;
            
  modnumber--;

  return new byte[] { (byte)0xF0, 0x10, 0x06, 0x0B, (byte)modnumber, (byte)modsource, (byte) modamount, (byte)moddestination, (byte)0xF7 };
  }
  else if (key.equals("trackingsource"))
  {
  index = ((Integer)(internalParametersToIndex.get(key))).intValue();
  value = model.get(key) + 1;  // tracking source has no "none"
  }
  // don't need to customize portamentomode though we'll have to do it on parse
  else
  {
  index = ((Integer)(internalParametersToIndex.get(key))).intValue();
  value = model.get(key);
  }
        
  byte VV = (byte)(value & 127);
  byte PP = (byte)(index & 127);
  return new byte[] { (byte)0xF0, 0x10, 0x06, 0x06, PP, VV, (byte)0xF7 };
  }
    

  /// ERRORS IN MIDI SYSEX DESCRIPTION
  ///
  /// Though they're listed as "six bit (signed)" or "seven bit (signed)", all signed values
  /// are actually stored as signed 8-bit.  Six-bit signed values are just plain signed bytes
  /// which range from -32 to +31.  Similarly, 7-bit signed values are just plain signed bytes
  /// which range from -64 to +63.  When emitting or parsing a patch, the nybblization just breaks
  /// the byte into two nybbles and that's all.
  ///
  /// Note however that when sending INDIVIDUAL PARAMETERS, the sysex value is first masked to 
  /// 7 bits (& 127).  And in NRPN, all values, even unsigned ones, have 64 added to them to 
  /// push them to 0...127.
    
  public int parse(byte[] data, boolean fromFile)
  {
        
  byte[] name = new byte[8];
        
  // we don't know the bank, just the number.  :-(
  int number = data[4];
  model.set("number", number);
                        
  for(int i = 0; i < 134; i++)
  {
  String key = allParameters[i];

  // unpack from nybbles
  byte lonybble = data[i * 2 + 5];
  byte hinybble = data[i * 2 + 5 + 1];
  byte value = (byte)(((hinybble << 4) | (lonybble & 15)));

  if (i < 8)  // it's the name
  name[i] = value;
  else if (key.equals("env1lfotriggermode") || key.equals("env2lfotriggermode") || key.equals("env3lfotriggermode"))
  {
  // there is no value = 1, that's the same as value = 0
  if (value >= 1) value = (byte)(value - 1);
  model.set(key, value);
  }
  else if (key.equals("dco1fixedmods1"))
  {
  model.set("dco1bend", value & 1);
  model.set("dco1vibrato", (value >>> 1) & 1);
  }
  else if (key.equals("dco1fixedmods2"))
  {
  model.set("dco1portamento", value & 1);
  }
  else if (key.equals("dco2fixedmods1"))
  {
  model.set("dco2bend", value & 1);
  model.set("dco2vibrato", (value >>> 1) & 1);
  }
  else if (key.equals("dco2fixedmods2"))
  {
  model.set("dco2portamento", value & 1);
  model.set("dco2keytracking", (value >>> 1) & 1);
  }
  else if (key.equals("dco1waveenable"))
  {
  model.set("dco1pulse", value & 1);
  model.set("dco1wave", (value >>> 1) & 1);
  }
  else if (key.equals("dco2waveenable"))
  {
  model.set("dco2pulse", value & 1);
  model.set("dco2wave", (value >>> 1) & 1);
  model.set("dco2noise", (value >>> 2) & 1);
  }
  else if (key.equals("vcffixedmods1"))
  {
  model.set("vcfbend", value & 1);
  model.set("vcfvibrato", (value >>> 1) & 1);
  }
  else if (key.equals("vcffixedmods2"))
  {
  model.set("vcfportamento", value & 1);
  model.set("vcfkeytracking", (value >>> 1) & 1);
  }
  else if (key.equals("portamentomode"))
  {
  if (value == 4)
  value = (byte)3;  // get rid of extra exponential
  model.set(key, value);
  }
  else if (key.equals("trackingsource"))
  {
  if (value > 0)  // Some Matrix 1000 patches have the source set to 0 even though it's not supposed to be!
  model.set(key, (value - 1));  // tracking source has no "none"
  else
  System.err.println("Warning (RolandAlphaJuno): Tracking Source was incorrectly 0.  Setting to 1.");
  }
  else
  {
  model.set(key, value);
  }
  }
                
  // to get the bank, we'll try to extract it from the name.  It appears to be the fourth character
  int bank = 0;
        
  // The sysex docs say the name is stored using the "lower 6 bits".  This is technically true but not helpful,
  // since what's REALLY happening is that the range 0x40...0x5F (@ through _) is stored as 0x00...0x0F, and
  // the range 0x20...0x3F (space through ?) is kept as is.
        
  /// The Matrix 1000 does not store names using the lower 6 bits.  Instead it stores
  // the phrase "BNKx: yy", where x is the bank number and y is the patch number,
  // as direct 7-bit ASCII.  We have to differentiate between these.
        
  if (name[0] == 'B' && name[1] == 'N' && name[2] == 'K' && name[4] == ':' && name[5] == ' ')  // probably Matrix 1000, hence BNKx: yy
  {
  bank = name[3] - '0';
  if (bank < 0 || bank > 9)
  {
  bank = 0;
  char[] n = new char[8];
  for(int i = 0; i < 8; i++) 
  n[i] = (char)name[i];
  System.err.println("Warning (RolandAlphaJuno): \"BNK:\" found but invalid bank number discovered: " + new String(n));
  }
  model.set("bank", bank);
                                
  if (bank >= 2 || useClassicPatchNames)
  model.set("name", PATCH_NAMES[bank * 100 + number]);
  else
  model.set("name", "UNTITLED"); 
  }
  else                                                            // probably a Matrix 6/6R
  {
  boolean warning = false;
  for(int i = 0; i < 8; i++)
  if (name[i] < 0x20)     // need to push up 0x00...0x1F to 0x40...0x5F
  name[i] += 0x40;
  else if (name[i] >= 0x40)       // need to keep 0x20...0x3F where it is, and there shouldn't be anything above 0x40 originally
  warning = true;
  char[] n = new char[8];
  for(int i = 0; i < 8; i++) 
  n[i] = (char)name[i];
  if (warning)
  {
  System.err.println("Warning (RolandAlphaJuno): Invalid bytes in patch name discovered: " + new String(n));
  }
  model.set("bank", 0);           // default?
  model.set("name", new String(n));
  }
    
    
  revise();
  return PARSE_SUCCEEDED;
  }
    

  public boolean sendAllParametersInternal()
  {
  if (m1000)
  {
  return super.sendAllParametersInternal();
  }
  else            // PC and write to scratch patch 99
  {
  changePatch(0, 99);             // Patch 99 is our scratch patch for the Matrix 6/64
  simplePause(getPauseAfterChangePatch());
  tryToSendMIDI(emitAll(model, true, false));
  return true;
  }
  }


  public int getPauseAfterWritePatch() { return 300; }        // Less than 200 and I'll get failures to PC the second time: at 250 I got a failure to write the patch.  250 might be enough but let's go for 300, yeah, it's a lot
    
  public Object[] emitAll(Model tempModel, boolean toWorkingMemory, boolean toFile)
  {
  if (tempModel == null)
  tempModel = getModel();

  byte[] data = new byte[268];
  String nm = model.get("name", "UNTITLED") + "        ";
  byte[] name = null;
  try { name = nm.getBytes("US-ASCII"); } catch (Exception e ) { }
  int value;
  byte check = 0;
                
  for(int i = 0; i < 134; i++)
  {
  String key = allParameters[i];
                
  if (i < 8)  // it's the name
  {
  value = name[i];
  if (value >= 0x40)
  value -= 0x40;  // push 0x40...0x5F to 0x00...0x1F
  }
  else if (key.equals("env1lfotriggermode") || key.equals("env2lfotriggermode") || key.equals("env3lfotriggermode"))
  {
  value = model.get(key);
  // convert
  if (value >= 1) value = value + 1;  // there is no value = 1, that's the same as value = 0
  }
  else if (key.equals("dco1fixedmods1"))
  {
  value = (model.get("dco1vibrato") << 1) |
  (model.get("dco1bend"));
  }
  else if (key.equals("dco1fixedmods2"))
  {
  value = (model.get("dco1portamento"));
  }
  else if (key.equals("dco2fixedmods1"))
  {
  value = (model.get("dco2vibrato") << 1) |
  (model.get("dco2bend"));
  }
  else if (key.equals("dco2fixedmods2"))
  {
  value = (model.get("dco2keytracking") << 1) |
  (model.get("dco2portamento"));
  }
  else if (key.equals("dco1waveenable"))
  {
  value = (model.get("dco1wave") << 1) |
  (model.get("dco1pulse"));
  }
  else if (key.equals("dco2waveenable"))
  {
  value = (model.get("dco2noise") << 2) |
  (model.get("dco2wave") << 1) |
  (model.get("dco2pulse"));
  }
  else if (key.equals("vcffixedmods1"))
  {
  value = (model.get("vcfvibrato") << 1) |
  (model.get("vcfbend"));
  }
  else if (key.equals("vcffixedmods2"))
  {
  value = (model.get("vcfkeytracking") << 1) |
  (model.get("vcfportamento"));
  }
  // Note: no need to handle portamentomode specially, but we DO have to parse it specially
                        
  // Ugh, all this below is to deal with the source=destination=0 requirement.  Yuck.
                
  else if (key.equals("mod1source") || key.equals("mod1destination"))
  {
  value = model.get(key);
  if (model.get("mod1source") == 0 || model.get("mod1destination") == 0)
  value = 0;
  }
  else if (key.equals("mod2source") || key.equals("mod2destination"))
  {
  value = model.get(key);
  if (model.get("mod2source") == 0 || model.get("mod2destination") == 0)
  value = 0;
  }
  else if (key.equals("mod3source") || key.equals("mod3destination"))
  {
  value = model.get(key);
  if (model.get("mod3source") == 0 || model.get("mod3destination") == 0)
  value = 0;
  }
  else if (key.equals("mod4source") || key.equals("mod4destination"))
  {
  value = model.get(key);
  if (model.get("mod4source") == 0 || model.get("mod4destination") == 0)
  value = 0;
  }
  else if (key.equals("mod5source") || key.equals("mod5destination"))
  {
  value = model.get(key);
  if (model.get("mod5source") == 0 || model.get("mod5destination") == 0)
  value = 0;
  }
  else if (key.equals("mod6source") || key.equals("mod6destination"))
  {
  value = model.get(key);
  if (model.get("mod6source") == 0 || model.get("mod6destination") == 0)
  value = 0;
  }
  else if (key.equals("mod7source") || key.equals("mod7destination"))
  {
  value = model.get(key);
  if (model.get("mod7source") == 0 || model.get("mod7destination") == 0)
  value = 0;
  }
  else if (key.equals("mod8source") || key.equals("mod8destination"))
  {
  value = model.get(key);
  if (model.get("mod8source") == 0 || model.get("mod8destination") == 0)
  value = 0;
  }
  else if (key.equals("mod9source") || key.equals("mod9destination"))
  {
  value = model.get(key);
  if (model.get("mod9source") == 0 || model.get("mod9destination") == 0)
  value = 0;
  }
  else if (key.equals("mod10source") || key.equals("mod10destination"))
  {
  value = model.get(key);
  if (model.get("mod10source") == 0 || model.get("mod10destination") == 0)
  value = 0;
  }
  else if (key.equals("trackingsource"))
  {
  value = model.get(key) + 1;  // tracking source has no "none"
  }
  else
  {
  value = model.get(key);
  }
            
  // pack to nybbles
            
  if (value < 0) value += 256;  // so we're positive.
  byte lonybble = (byte)(value & 15);
  byte hinybble = (byte)((value >>> 4) & 15);
            
  // From here:  http://www.youngmonkey.ca/nose/audio_tech/synth/Oberheim-Matrix6R.html
  // it says this about the checksum:
  //
  // Checksum.
  // The original (not transmitted) data is summed in seven bits ignoring overflows
  //
  // I think this means to add into a byte, and then mask to 127.
            
  check += value;
                
  // write
  data[i * 2] = lonybble;
  data[i * 2 + 1] = hinybble;
  }
    
  byte checksum = (byte)(check & 127);
  byte[] d = new byte[275];
  d[0] = (byte)0xF0;
  d[1] = (byte)0x10;
  d[2] = (byte)0x06;

  if (toWorkingMemory && m1000)
  {
  // 0DH - SINGLE PATCH DATA TO EDIT BUFFER
  d[3] = (byte)0x0D;
  d[4] = (byte)0x00;
  }
  else if (toWorkingMemory)   // 6/6R only
  {
  // 01H-SINGLE PATCH DATA
  d[3] = (byte)0x01;
  d[4] = (byte)99;                    // our scratch patch
  }
  else
  {
  // 01H-SINGLE PATCH DATA
  d[3] = (byte)0x01;
  d[4] = (byte)tempModel.get("number");
  }

  System.arraycopy(data, 0, d, 5, 268);
  d[273] = checksum;
  d[274] = (byte)0xF7;
                
  if (toFile)
  {
  return new Object[] { d };
  }
  else if (toWorkingMemory || !m1000)
  {
  // See requestSendingParameters() for an explanation of the second message here
  return new Object[] { d , new byte[] { (byte)0xF0, 0x10, 0x06, 0x05, (byte)0xF7 }};
  }
  else
  {
  // You have to set the bank before you can write a patch because the 
  // Matrix1000 patch upload command doesn't include bank information;
  // it assumes the bank has already been set.
  // 
  // 0AH - SET BANK
  // we write this store-command as a sysex command 
  // so it gets stripped when we do a save to file
  // 
  // I think this should be compatible with the 6/6R because they don't respond to it at all
  byte[] changeBank = new byte[6];
  changeBank[0] = (byte)0xF0;
  changeBank[1] = (byte)0x10;
  changeBank[2] = (byte)0x06;  
  changeBank[3] = (byte)0x0A;
  changeBank[4] = (byte)tempModel.get("bank");
  changeBank[5] = (byte)0xF7;

  return new Object[] { changeBank, d };
  }
  }
        
        
        
  public void changePatch(Model tempModel)
  {
  changePatch(tempModel.get("bank"), tempModel.get("number"));
  }


  public void changePatch(int bank, int number)
  {
  // first change the bank
                
  // 0AH - SET BANK
  // we write this store-command as a sysex command 
  // so it gets stripped when we do a save to file
  // 
  // I think this should be compatible with the 6/6R because they don't respond to it at all
  byte[] data = new byte[6];
  data[0] = (byte)0xF0;
  data[1] = (byte)0x10;
  data[2] = (byte)0x06;  
  data[3] = (byte)0x0A;
  data[4] = (byte)(bank);
  data[5] = (byte)0xF7;

  tryToSendSysex(data);

  // Next do a program change.  We do a program change while the bank is locked because otherwise
  // it'd select in bank 0.
        
  byte NN = (byte)number;
  tryToSendMIDI(buildPC(getChannelOut(), NN));

  // Now for good measure let's unlock the bank

  // 0CH - UNLOCK BANK
  // we write this store-command as a sysex command 
  // so it gets stripped when we do a save to file
  // annoying that this gets re-locked by SET BANK
  // 
  // I think this should be compatible with the 6/6R because they don't respond to it at all
  data = new byte[5];
  data[0] = (byte)0xF0;
  data[1] = (byte)0x10;
  data[2] = (byte)0x06;  
  data[3] = (byte)0x0C;
  data[4] = (byte)0xF7;
  tryToSendSysex(data);
  }

  public void performRequestDump(Model tempModel, boolean changePatch)
  {
  super.performRequestDump(tempModel, changePatch);
  if (!m1000) requestSendingParameters();
  }

  public void performRequestCurrentDump()
  {
  super.performRequestCurrentDump();
  if (!m1000) requestSendingParameters();
  }

  public byte[] requestCurrentDump()
  {
  byte[] data = new byte[7];
  data[0] = (byte)0xF0;
  data[1] = (byte)0x10;
  data[2] = (byte)0x06;
  data[3] = (byte)0x04;
  data[4] = (byte)0x04;           // request edit buffer
  data[5] = (byte)0x00;
  data[6] = (byte)0xF7;
  return data;
  }

  public byte[] requestDump(Model tempModel)
  {               
  if (tempModel == null)
  tempModel = getModel();

  // Next do a dump request
  byte[] data = new byte[7];
  data[0] = (byte)0xF0;
  data[1] = (byte)0x10;
  data[2] = (byte)0x06;
  data[3] = (byte)0x04;
  data[4] = (byte)0x01;           // request single patch
  data[5] = (byte)(tempModel.get("number"));
  data[6] = (byte)0xF7;
  return data;
  }
*/
                    
    public static final int MAXIMUM_NAME_LENGTH = 8;
    public String revisePatchName(String name)
        {
        name = super.revisePatchName(name);  // trim first time
        if (name.length() > MAXIMUM_NAME_LENGTH)
            name = name.substring(0, MAXIMUM_NAME_LENGTH);
        
        StringBuffer nameb = new StringBuffer(name);                            
        for(int i = 0 ; i < nameb.length(); i++)
            {
            char c = nameb.charAt(i);
            if (c >= 0x60) c = (char)(c - 0x20);  // make uppercase etc.
            if (c < 0x20 || c >= 0x60) c = ' ';
            nameb.setCharAt(i, c);
            }
        name = nameb.toString();
        return super.revisePatchName(name);  // trim again
        }


    /** Verify that all the parameters are within valid values, and tweak them if not. */
    public void revise()
        {
        // check the easy stuff -- out of range parameters
        super.revise();
        
        String nm = model.get("name", "UNTITLED");
        String newnm = revisePatchName(nm);
        if (!nm.equals(newnm))
            model.set("name", newnm);
        }
        
//    public int getPauseAfterSendOneParameter() { return 75; }

    public static String getSynthName() { return "Roland Alpha Juno 1, 2, MKS-50"; }
    
    public String getPatchName(Model model) { return model.get("name", "UNTITLED"); }
    
//    public int getPauseAfterSendAllParameters() { return 200; }
    
    
    public Model getNextPatchLocation(Model model)
        {
        int bank = model.get("bank");
        int number = model.get("number");
        
        number++;
        if (number >= 100)
            {
            bank++;
            number = 0;
            if (bank >= 10)
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
        

        int number = model.get("number") + 1;
        return ((model.get("bank") == 0 ? "Preset " : (model.get("bank") == 1 ? "Internal " : "Cartridge ")) + 
            (number > 9 ? "" : "0") + number);
        }

    public boolean useClassicPatchNames = true;

    // This strange little message sets up the Matrix 6 and 6R to receive individual parameter
    // changes.  It is not, to my knowledge, required for the Matrix 1000.   I don't want to
    // send it before every individual parameter change, so instead I am sending it at certain
    // opportune times, like each patch emit, patch request, and when the user asks for it
    // via a menu option.
    void requestSendingParameters()
        {
        tryToSendSysex( new byte[] { (byte)0xF0, 0x10, 0x06, 0x05, (byte)0xF7 } );
        }

    public void addOberheimMenu()
        {
        JMenu menu = new JMenu("Matrix");
        menubar.add(menu);

        JMenuItem sendParameterMenu = new JMenuItem("Request Sending Parameters");
        sendParameterMenu.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt)
                {
                requestSendingParameters();
                }
            });
            
        menu.add(sendParameterMenu);

        // classic patch names
                
        final JCheckBoxMenuItem useClassicPatchNamesMenu = new JCheckBoxMenuItem("Use Classic Patch Names");
        menu.add(useClassicPatchNamesMenu);

        useClassicPatchNamesMenu.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt)
                {
                useClassicPatchNames = useClassicPatchNamesMenu.isSelected();
                setLastX("" + useClassicPatchNames, "UseClassicPatchNames", getSynthClassName(), true);
                }
            });
        
        String str = getLastX("UseClassicPatchNames", getSynthClassName(), true);
        if (str == null)
            useClassicPatchNames = true;
        else if (str.equalsIgnoreCase("true"))
            useClassicPatchNames = true;
        else useClassicPatchNames = false;
        
        useClassicPatchNamesMenu.setSelected(useClassicPatchNames);
        
        menu.addSeparator();

        // load patch
        for(int i = 0; i < 1000; i += 50)
            {
            JMenu patchgroup = new JMenu("" + "Request Patch " + (i < 100 ? (i < 10 ? "00" : "0") : "" ) + i + "..." + (i < 100 ? "0" : "") + (i + 49));
            menu.add(patchgroup);
            for(int j = i; j < i + 50; j++)
                {
                final int _j = j;
                JMenuItem patch = new JMenuItem("" + 
                    (j < 100 ? (j < 10 ? "00" + j : "0" + j) : "" + j) + ": " + 
                    PATCH_NAMES[j]);
                patch.addActionListener(new ActionListener()
                    {
                    public void actionPerformed(ActionEvent evt)
                        {
                        Model tempModel = buildModel();
                        tempModel.set("number", _j % 100);
                        tempModel.set("bank", _j / 100);
                        performRequestDump(tempModel, true);
                        }
                    });
                patchgroup.add(patch);
                }
            }
        }
 
    // These are drawn from the "Matrix 1000 Patchbook"
    public static final String[] PATCH_NAMES = 
        {
        "TOTOHORN",
        "1000STRG",
        "MOOOG_B",
        "EZYBRASS",
        "SYNTH",
        "MIBES",
        "CHUNK",
        "MINDSEAR",
        "CASTILLO",
        "DESTROY+",
        "BIG PIK",
        "M-CHOIR",
        "STRINGME",
        ")LIQUID(",
        "PNO-ELEC",
        "BED TRAK",
        "STELLAR",
        "SYNCAGE",
        "SHIVERS",
        "+ ZETA +",
        "STEELDR.",
        "TAURUS",
        "POWRSOLO",
        "INTERSTL",
        "REZTFUL",
        "WATRLNG",
        "BEELS",
        "LIKETHIS",
        "NTHENEWS",
        "SOFT MIX",
        "OBXA-A7",
        "BREATH",
        "MUTRONO",
        "SLOWATER",
        "HAUNTING",
        "FLANGED",
        "TENSION",
        "ECHOTRON",
        "PIRATES!",
        "EP SWEP",
        "DEJAVUE'",
        "DRAMA",
        "VIOLINCE",
        "BOUNCE",
        "SAGAN'Z",
        "OB LEAD",
        "FEEDGIT",
        "SAMPLE",
        "TINYPIAN",
        "GALACTIC",
        "DOU CIEL",
        "WA CLAV",
        "DREAMER",
        "XA STR",
        "CHURCH",
        "KIDDING?",
        "THUNDER",
        "ECHOWURL",
        "BLABINET",
        "STRUNGS",
        "AFRICAN",
        "B3+LSLIE",
        "CHIMES",
        "DIPIAN",
        "LAZ HARP",
        "SMTHSQ2",
        "TRUMPETS",
        "PAPANO 4",
        "WIPBASS",
        "LYLE-8VA",
        "SITAR",
        "VIOGITAR",
        "GOLIATH",
        "ANAXYLO",
        "FURY0",
        "SYNLUTH",
        "CHAMBER",
        "SPATBRS",
        "ETHEREE",
        "TBRAZZ",
        "NOBLE",
        "FLEXTONE",
        "GREEZY2",
        "ARPPEGT",
        "JUMP IES",
        "HARDVARK",
        "SWEETSKY",
        "SHIMRING",
        "TIMBOWS",
        "GALLOP",
        "PRELUDE1",
        "GROWLBRZ",
        "CLICKORG",
        "PRESLEZ1",
        "LYLE",
        "ARCANGEL",
        "BENSHIMR",
        "LUSHNESS",
        "NOISTRNG",
        "SOPIPES",
        "PAPANO 4",
        "WURLY 2",
        "TOTOHORN",
        "AGRESORN",
        "STRING S",
        "ODX 7",
        "JUMP IES",
        "PROFIT",
        "VOICES",
        "SAGAN'Z",
        "PA ANO 5",
        "MONSTER",
        "GENVIV",
        "CLAVINET",
        "STRINGER",
        "SIMONISK",
        "HOTBODOM",
        "XTASY",
        ")LIQUID(",
        "BED TRAK",
        "ROADS",
        "ECHOWURL",
        "POLYPHON",
        "DREAMER",
        "HISTRUNG",
        "DEJAVUE'",
        "WET BAZ",
        "PIPE",
        "OCTAVIA",
        "OB-STRGS",
        "KAWHY",
        "JOCKO",
        "FLUTES",
        "ATYPICAL",
        "BOW VIOL",
        "VERTABRA",
        "SLAP 1",
        "P.ORGAN5",
        "OCTAHORN",
        "LUSHNESS",
        "12\"GITAR",
        "STAND UP",
        "BERT'S B",
        "BRASS-11",
        "STRINGME",
        "LEED-2",
        "OW BASS",
        "ORGAN-1",
        "HORNISK",
        "STRING-1",
        "CARIPSO",
        "FAZ BASS",
        "ORGAN-2",
        "PHASEPAD",
        "STRING 2",
        "BATA",
        "BASS SYN",
        "SYNCAGE*",
        "DIGIHORN",
        "CELLO",
        "GLASSVOX",
        "FRET NOT",
        "ORGANISM",
        "LIMUIDZ",
        "STRANGER",
        "PAPANO 7",
        "WOW BASS",
        "P.ORGAN4",
        "MELOHORN",
        "LEED-1",
        "TINYPIAN",
        "SLAP 2",
        "FORESTS",
        "HONOCLAB",
        "STRING 7",
        "VELSYNC*",
        "RAINECHO",
        "ORGAN 9",
        "BRAZZ",
        "ACCORD",
        "DIGPIANO",
        "TENOR",
        "STORTGAN",
        "VIOGITAR",
        "STRUNGET",
        "BIRDLAND",
        "METAL-1",
        "METAL-8",
        "FUNK ART",
        "METAL-13",
        "VIBES",
        "STRING 6",
        "STRNGREZ",
        "SINGS",
        "TIMBOWS",
        "WHISTLE",
        "OBERHORN",
        "TOOTS ?",
        "FIREBALL",
        "SMPLTHIS",
        "OBXA-11",
        "OBXA-12",
        "OBXA-A2",
        "OBXA-A7",
        "OBXA-B7",
        "OBXA-B8",
        "OBXA-C2",
        "OBXA-C4",
        "OBXA-C6",
        "OBXA-C7",
        "OBXA-C8",
        "OBXA-D2",
        "OBXA-D3",
        "OBXA-D4",
        "OBXA-D5",
        "OBXA-D6",
        "OBXA-D7",
        "OBXA-D8",
        "OBXA-9\"",
        "OBCA-RE",
        "OBXJMP",
        "*'ANGEL",
        "+ ZETA +",
        "1984SWP6",
        "WAVES",
        "80MS DDL",
        "SYNTH",
        "AERIAL",
        "ALIENSWP",
        "AMBIANCE",
        "ANAFTST*",
        "ANAHARP",
        "ANALOG B",
        "ANAXYLO*",
        "ANGELS",
        "APOLLO",
        "ARCANGEL",
        "ARGEX-1",
        "ARGON7",
        "ATYPICAL",
        "AW WHY ?",
        "BENSHIMR",
        "BEOWCOMP",
        "BILLY",
        "BLASZZ",
        "BLOCKOUT",
        "BOEPTYN*",
        "BOTTLES",
        "BOUNCE*",
        "BRASSVOX",
        "BRILLANT",
        "BROADWAY",
        "BS ETAK*",
        "BURNHOUS",
        "CAMERA 1",
        "CHIME 1",
        "CHIME 2",
        "CHUNK",
        "CMI HIGH",
        "COEAUR 1",
        "COLONY 9",
        "CRYSLAKE",
        "CS-80",
        "DEACON",
        "DEJAVUE'",
        "DIDIER",
        "DISTANCE",
        "DMACHINE",
        "DREAMER",
        "DREEMER",
        "DUCKTIME",
        "DUNK IT",
        "E N O 1",
        "ECHOSYN",
        "ECHOTRON",
        "EGYPT",
        "EP SWEP*",
        "EPCH+BRZ",
        "EPDSTRT*",
        "ESQ-1",
        "ETHEREE",
        "FAKE DDL",
        "FIFTHS",
        "FLOATONG",
        "FLPFLOP*",
        "FLY TO",
        "FM BASS",
        "FUNDO",
        "*FUNK ART",
        "FUNKAY",
        "FURYO",
        "FWEEP",
        "S.1",
        "GALACTIC",
        "GALLOP *",
        "GENIVEEV",
        "GENVIV",
        "GENVIV*",
        "GIRLSWEP",
        "GOOD BED",
        "GOODTIME",
        "GROTTO",
        "HACKETT",
        "HALO",
        "HARMOVOX",
        "HARPOON",
        "HELI-IN",
        "HOMETOWN",
        "INTERSTL",
        "ITSONICE",
        "JAZZQUIT",
        "JM JARRE",
        "JOHN B'S",
        "KCEPMAX*",
        "KCEPSAW*",
        "KCHSYNC*",
        "KIRKLAND",
        "LDSUBHRM",
        "LIKETHIS",
        "LSTLAUGH",
        "LUN'AIR",
        "M-CHOIR",
        "MAGICAL",
        "MARIN",
        "MATMODUL",
        "MATRIX 1",
        "MATRIX 2",
        "METABOAD",
        "METABRD",
        "MINDSEAR",
        "MONSTER",
        "MR KYRIE",
        "MUSICBOX",
        "NAUTILUS",
        "NEW VOX",
        "NEWSOUND",
        "NIGHTPAD",
        "OB SWEEP",
        "OB VOX*",
        "OB-INTRO",
        "OBXA-B2",
        "OBXA-B4",
        "OCTAVIA",
        "OPEN AIR",
        "ORDINARY",
        "P CHORD",
        "P-PLUCK",
        "PAD",
        "PERC S",
        "PHASE 5",
        "PHASECHO",
        "PICKY",
        "PIPESTR",
        "PN/FMSWP",
        "POLCHOIR",
        "POWER",
        "PROPHET1",
        "PROPHET5",
        "PROPHETV",
        "PSYLITIS",
        "REZ*PULS",
        "REZTFUL",
        "SAMPLE",
        "SATURN",
        "SCIENCE",
        "SCRITTI+",
        "SECRETS",
        "SENSIT 2",
        "SENSITIV",
        "SEQUINOX",
        "SHANKAR",
        "SHIMMER",
        "SHIMRING",
        "SHIVERS",
        "SKRCHTN*",
        "SKTSOKY*",
        "SKY HIGH",
        "SKYVOICE",
        "SLAPBACH",
        "SLIDSTG",
        "SLOW CRY",
        "SLOWATER",
        "SMTHSQ2*",
        "SOFT MIX",
        "SOUNDPAD",
        "SOUNDTR[",
        "SPACE",
        "SPACE/CO",
        "SPARKLES",
        "SPLASH 1",
        "STELLAR*",
        "STUGROWL",
        "STYX",
        "SUBMARIN",
        "SUNDAY",
        "SUSSUDIO",
        "SWEPCORD",
        "SWRLEKO*",
        "SYN BOX*",
        "SYNCAGE*",
        "TOTOHORN",
        "OBXA-13",
        "OBXA-A1",
        "OBXA-B1",
        "OBXA-C1",
        "OBXA-D1",
        "6R BRASS",
        "AGRESORN",
        "ALASKA",
        "ANA HIT*",
        "ANASUTL*",
        "B'ARI/S2",
        "B/D-ANA*",
        "BAGPIPES",
        "BARISAX",
        "BASCLRNT",
        "BASSCLAR",
        "BASSOON",
        "BENDHORN",
        "BIGBRA$$",
        "BONES",
        "BRASRAMP",
        "BTRASSVOX",
        "BRASSY",
        "BRAZEN",
        "BRECHER",
        "BRTH FLT",
        "BRUTUS",
        "BRZIVIV*",
        "BUCHANN*",
        "BUZREED",
        "CHROMA-S",
        "CLARINET",
        "CRAZHORN",
        "CS-80",
        "CUIVRE((",
        "DBLREED*",
        "EASTREED",
        "EDGY",
        "ENSEMBL*",
        "EUROPE",
        "EWF HORN",
        "EZYBRASS",
        "FACTORY",
        "FIFTHS",
        "FLGLHORN",
        "FLOOT",
        "FLUGELHN",
        "FLUGLE",
        "FLUTE",
        "FLUTE TR",
        "FLUTE.",
        "FLUTES",
        "FLUTEY",
        "FLUX",
        "FM BRAZ",
        "FM DELAY",
        "FR.HORN",
        "FNRCHRN*",
        "FTHWEEL*",
        "FUE.JAPN",
        "FUSION",
        "FWEEP",
        "GABRIEL",
        "GO BED",
        "GOLIATH",
        "HORN'EM",
        "HORN-1",
        "HORNENS",
        "HORNFALL",
        "HORNSAS",
        "HORNY",
        "HRNSHAKE",
        "J HAMMER",
        "JTULLFLT",
        "JUBILEE",
        "KLARYNET",
        "KORGHORN",
        "LYRICON",
        "MATRONE",
        "MELFAZE*",
        "MELOHORN",
        "METHENY5",
        "MUTETRPT",
        "OB BRASS",
        "OB-8",
        "OBERHORN",
        "OBOE",
        "OCT.BRS",
        "OCTAFLUT",
        "OCTAHORN",
        "ORIENT",
        "PEDSWP*",
        "PEG-BRS",
        "PYRMFLT*",
        "RAHOOOL*",
        "RECORDER",
        "RELVELHO",
        "RICHCORD",
        "ROMAN",
        "SEXAFOAM",
        "SLO HRN",
        "SOPIPES",
        "SPATBRS*",
        "SQUARDOU",
        "STAB",
        "STAB-BRS",
        "STEPS 2.",
        "STUFLUTE",
        "SWRLYBRD",
        "SYN SAX*",
        "SYNBASS",
        "SYNBONE",
        "SYNBRSS*",
        "SYNHORN",
        "TBRAZZ",
        "TENOR",
        "TOTOAL",
        "TOUCH+GO",
        "TRILLFLT",
        "TRMBONE*",
        "TROMBONE",
        "TRUMPETS",
        "TRUPT-EU",
        "* 99 *",
        "TUBA 2",
        "OBXA-A8",
        "ELEAD*",
        "BDTH-2",
        "BIRDY",
        "BRECKERL",
        "CASTILLO",
        "CHICK",
        "DESTROY+",
        "DIGRUNGE",
        "DRAGON-3",
        "DXINDIAN",
        "FEEDBAK6",
        "FEEDBAK8",
        "FEEDGIT",
        "FIFTH I%",
        "FIFTHLIX",
        "GLASLEED",
        "GROWLBRS",
        "H-LEAD",
        "HILEED 6",
        "J HAMMER",
        "JAKOLEED",
        "JAN LEAD",
        "JAZZ",
        "JIMY'SRG",
        "KC LEAD*",
        "KIDDING?",
        "LEAD+PRT",
        "LEAD-1",
        "LEAD-3",
        "LEED-1",
        "LEED-2",
        "LYLE 2",
        "LYLE 3 M",
        "METHENEY",
        "METLSOLO",
        "MILESCOM",
        "MINIMOGG",
        "MINIMOOG",
        "MONOSTRG",
        "NASTY",
        "OB LEAD*",
        "OSC SYNC",
        "PANFLOET",
        "PINKLEAD",
        "POWRSOLO",
        "PRSSLIDE",
        "QUINCY",
        "RECORDER",
        "REZLEAD*",
        "SAWLEAD*",
        "SITAR",
        "SMOOTH",
        "SMUTHSQ*",
        "SOLO",
        "SOLODARM",
        "SOLOPROF",
        "SOLOSYNC",
        "SOLOW*",
        "SOPIPES",
        "SPITLEED",
        "SQARELED",
        "STUVIB",
        "SUSGUIT",
        "SNTHE 5",
        "UKSOLO",
        "UNIBASS",
        "UNIWAVE",
        "WAKEMANS",
        "WEIRDPRC",
        "WHISTLER",
        "WINAND 1",
        "XA'SOLO",
        "ZAW'QART",
        "OBXA-10",
        "OBXA-14",
        "OBXA-A3",
        "OBXA-B3",
        "OBXA-B6",
        "OBXA-C3",
        "OBXA",
        "OBXA-6",
        "(ARCO)01",
        "*'CANOPY",
        "1000STRG",
        "TOP",
        "2000STRG",
        "AGITATO*",
        "ALL LOVE",
        "ALT84TOP",
        "BED TRAK",
        "BLACSEAM",
        "BOW IT",
        "BOW VIOL",
        "CELLO",
        "CHAMBER",
        "CHILLO",
        "CLASSIKA",
        "CONCERT",
        "DEEPCAVE",
        "DEPTHS",
        "DLAYSTR*",
        "DONSTRIG",
        "DOU'CIEL",
        "DUNGEON",
        "DYNASTY",
        "E.VIOLIN",
        "FAMUS*OB",
        "FORESTS",
        "GRANULES",
        "GREAT\"OB",
        "HARMONIC",
        "ICY-CHRD",
        "INDNSTRG",
        "LOWSTRNG",
        "LOYAL",
        "LUSHNESS",
        "LYLE-8VA",
        "MELLO=14",
        "MKSINGS",
        "MONEY $$",
        "MUTEDSTR",
        "MZSTRING",
        "NOBLE",
        "NOISTGS",
        "OB A3PD*",
        "OB-STR1N",
        "OB-STRGS",
        "OBSTRING",
        "OBXA-A6",
        "OCARINA",
        "OCHESTRY",
        "OPENSTRG",
        "ORCH*",
        "ORIENT",
        "PITZ STR",
        "PIZZ^+P2",
        "PLANET P",
        "POLSTRG2",
        "PROHET-5",
        "PROPHET5",
        "RID ZEP",
        "ROYAL PH",
        "RP STRG5",
        "SECRETS'",
        "SHARPBOW",
        "SHIFT",
        "SHRTSTRG",
        "SINGS",
        "SLIDSTG",
        "SLOW BOW",
        "SLOW CRY",
        "SMASH",
        "SOLEMN",
        "SOLEMNIS",
        "SOLO",
        "SOUNDTR",
        "SOUNDTRK",
        "SOUNDTR[",
        "SRTRONGS",
        "STAND UP",
        "STR END*",
        "STR-8VA",
        "STRANGER",
        "STREENG",
        "STREENGS",
        "STRING 2",
        "STRING 6",
        "STRING 7",
        "STRING 8",
        "STRING S",
        "STRING\"8",
        "STRING-1",
        "STRINGER",
        "**A!A!**",
        "2600-2",
        "AGREBASS",
        "ANTEATER",
        "ARP-2",
        "ATYPBASS",
        "AXXE",
        "BARISAX",
        "BASS PAD",
        "BASS SYN",
        "BASS ZZT",
        "BASS-11",
        "BASSA",
        "BASSCLAR",
        "TUBULAR",
        "BASSE OA",
        "BASSGTAR",
        "BASSHIPO",
        "BASSHORN",
        "BASSVIOL",
        "BASSVOX",
        "BIG PIK",
        "BIRDLAND",
        "BOLUBASS",
        "BOTBASS",
        "BOWBASS",
        "BRAAS",
        "BS/STRG*",
        "UNI BASS",
        "CLAVBASS",
        "BOUBLEBS",
        "DUCK 2",
        "DUCKBASS",
        "EARTHESS",
        "ELC BASS",
        "ELEC BS*",
        "FANKNBAZ",
        "FAZ BASS",
        "FLOORIT",
        "FRET EKO",
        "FRET NOT",
        "FUNK BAZ",
        "HARMBAS5",
        "HISBASS",
        "HOTBODOM",
        "JAN BASS",
        "JOCKO",
        "JOCKO 2",
        "LEEDBASS",
        "LUMPBASS",
        "MINIBASS",
        "MONO BS*",
        "MOOGER",
        "MOOOG_B",
        "MUFFEL",
        "NOISBASS",
        "OCTABASS",
        "ORBASS",
        "OW BASS",
        "PABASS*",
        "PLUCK-BS",
        "POLBASS1",
        "POLYBASS",
        "PUKBASS",
        "R + B",
        "RAGABASS",
        "VELBASS",
        "RUBBER",
        "SEQUBASS",
        "VELGROWL",
        "SINCBASS",
        "WAPBASS",
        "SLAP 1",
        "SLAP 2",
        "SLIDER",
        "SLOWBASS",
        "SNTHBS1*",
        "SOFTBASS",
        "SPITBASS",
        "SQUISBAZ",
        "STAND UP",
        "STBASS",
        "STR.BASS",
        "STRANGTK",
        "STRIBASS",
        "WET BAZ",
        "STRINGBZ",
        "SUGITA\"",
        "SUPPORT",
        "SWELLBAZ",
        "SWP.BASS",
        "SYBASS 2",
        "SYN BS2*",
        "SYN BS3*",
        "SYN BS4*",
        "SYNCBASS",
        "TAURUS",
        "TENU'OB2",
        "WIPBASS",
        "TIKBASS",
        "AK-48",
        "APORT",
        "BALLGAME",
        "BANJO",
        "BASSDRUM",
        "BDTH-1",
        "BELL 1",
        "BELLIKE",
        "BELLS",
        "BELLS-GS",
        "BI-PLANE",
        "BOTTLES",
        "BTMEHRDR",
        "BURST 1",
        "CASCAD'4",
        "CHIMES",
        "CHIMES*",
        "CHOPPERZ",
        "COINOP 3",
        "COPOLIPS",
        "CRAZYMAN",
        "CRICKET",
        "CROZTALK",
        "DB BELL",
        "DIDIER",
        "DREAMING",
        "DRIFTER*",
        "DRUMPOP",
        "DUNDERZ",
        "DX-PLUCK",
        "TURBO",
        "FALLCHYM",
        "FIREBALL",
        "FLAME ON",
        "FLEXTONE",
        "FMPLUKS",
        "FURYO 2",
        "G.S.2",
        "G.S.3",
        "GLOCK",
        "WINDS",
        "HAUNTING",
        "HEART",
        "ZAP",
        "HORRORS",
        "HOWITZER",
        "HVN+HELL",
        "INDIAN",
        "TOP-GUN*",
        "INSIDES",
        "JETTZ 3",
        "JUNKANOO",
        "WETFEET",
        "KINGONG",
        "KONTAKTE",
        "LCTRCUTE",
        "WHIZZ",
        "LFO ART",
        "LFOMALET",
        "LIFTOFF",
        "LOOPBELL",
        "LYLE 3 P",
        "LYLES'",
        "THUNDRUS",
        "MACHINSM",
        "MANIAC*",
        "MARIMA",
        "MOFO",
        "MEMORIES",
        "MOUNTAIN",
        "MRIMBAH*",
        "NASTEEZ",
        "WARNINGS",
        "NOISE-DN",
        "NOISSWEP*",
        "NOIZGATE",
        "NTHENEWS",
        "NUKE EM'",
        "OCIEAN",
        "OCEANWAV",
        "OOZES 3",
        "PHASES*",
        "PINWHEEL",
        "PLUCK",
        "POLBELS2",
        "POLNOISE",
        "PORTAL",
        "PSYCHYM",
        "RAINECHO",
        ")RAPIST(",
        "WATER",
        "RUBRTOMS",
        "SATURDAY",
        "SCRATCH",
        "SEQEUNCE",
        "SGUSTING",
        "SHRNKRAY",
        "SIMONISK",
        "SMASH*",
        "SMPLTHIS",
        "PAPANO 4",
        "MIKPIANO",
        "HONOCLAB",
        "MR.ROGRS",
        "MTL PNO*",
        "MUSETTE",
        "MUTDCLV*",
        "MUTRONO",
        "NYLNPIK*",
        "NYLNPK2*",
        "NYLON 12",
        "OB8 JUMP",
        "OBNOXVOX",
        "OBXA-B5",
        "ODX 7",
        "OORGAN",
        "LAZ HARP",
        "ORGAN 9",
        "ORGAN-1",
        "ORGAN-1P",
        "ORGAN-2",
        "ORGANISM",
        "AKOUSTIK",
        "ORGNIZE*",
        "P.ORGAN",
        "P.ORGAN4",
        "P.ORGAN5",
        "PA ANO 5",
        "HARPO",
        "PAPANO 7",
        "LULLABOX",
        "PERCCLAV",
        "PERCPNO",
        "PIANITAR",
        "PIANO",
        "PIANO BO",
        "PIANOLA",
        "B-3.1",
        "PINPIANO",
        "PIPEORG.",
        "PIPEORG:",
        "PIPES",
        "PIPSTRNG",
        "PIRATES!",
        "PNO-ELEC",
        "POLPIANP",
        "PRELUDE1",
        "PRESLEZ1",
        "PROFIT",
        "PROPH W",
        "PROPHET",
        "RESPIANO",
        "RMIPIANO",
        "ROADS",
        "SAL00N 5",
        "SALOON 3",
        "B-3.2",
        "SALOON 7",
        "SAMPLORG",
        "SAMSGRND",
        "SITAR I",
        "SMTHSQ2*",
        "SPANIEL",
        "SPRPRTS*",
        "B-3.3",
        "B3+LSLIE",
        "STRGTR2*",
        "SYN CLAV",
        "BELLS",
        "SYNLUTH",
        "BLABINET",
        "SYNPIANO",
        "CELESTE",
        "CHIMES",
        "TINEOUT",
        "TONYPIAN",
        "TOYPIANO",
        "TWINSTRG",
        "CHURCH",
        "VIBECHOES",
        "VIBES",
        "CLAV B6",
        "CLAVI 2",
        "WA CLAB*",
        "CLAVINET",
        "WHAANO",
        "WHY FM",
        "CLICKORG",
        "WURLI8",
        "CLUBS",
        "WURLY 2",
        "WURLY 3",
        "X-GRAND",
        "XA'ORGAN",
        "YOUREYES",
        "ZITHER",
        "CORDINE1",
        "D\"AMMOND",
        "GREEZY1",
        "GRNDR 6*"
        };
        
    public boolean testVerify(Synth synth2, 
        String key,
        Object obj1, Object obj2) 
        {
        // destinations must be zero when sources are zero
        if (key.endsWith("source")) return true;
        if (key.endsWith("destination")) return true;
        return false;
        }

/*
// print out the original patches among the 200 first ones
public static void main(String[] args)
{
foo:
for(int i = 0; i < 200; i++)
{
for(int j = 200; j < 1000; j++)
{
if (PATCH_NAMES[i].equals(PATCH_NAMES[j]))
continue foo;
}
System.err.println("" + i + "  " + PATCH_NAMES[i]);
}
}
*/

    public String[] getBankNames() { return BANKS; }

    public String[] getPatchNumberNames() 
        { 
        return buildIntegerNames(100, 0); 
        }
                
    public boolean[] getWriteableBanks() { return new boolean[] { true, true }; }
                        
    public int getPatchNameLength() { return 8; }

    public boolean getSupportsPatchWrites() { return true; }

    public boolean librarianTested() { return true; }
    }
