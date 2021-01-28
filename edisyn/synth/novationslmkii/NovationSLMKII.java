/***
    Copyright 2021 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.novationslmkii;

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
   A patch editor for the Novation SL MK II.
        
   @author Sean Luke
*/

public class NovationSLMKII extends Synth
    {
    
    /// Interesting fact: the unit doesn't have "default", but the editor does.
    /// If Encoder is 0-16K then the maximum position is the size minus 1 to accommodate two DV
    /// Not clear why the template size and position maximum is 40 if there are only 32 templates
    
    
    /// IMPORTANT NOTE.  You will discover that the ordering of the strings in these arrays
    /// is unusual.  It's not the same ordering as appears in Novation's software, nor
    /// on Novation's SL units.  Nor is it necessarily the ordering which appears in the sysex.
    /// What's going on here?  The rearrangement is part of a desperate attempt to make it easy
    /// to cut and paste categories, much like they can be cut and pasted, or moved, in the
    /// Novation software.  Unfortunately, pots, encoders, buttons, drumpads, and pitch bend
    /// units are different enough from another to make cut and paste challenging unless certain
    /// things line up or are out of bounds so they can get cleaned up during revise().  
    /// This is why, for example, 0-16K is the *last* item in ENCODER_DISPLAYS.
    public static final String[] ENCODER_TYPES = { "Off", "CC", "NRPN", "RPN", "Sysex" };
    public static final String[] POT_TYPES = ENCODER_TYPES;
    public static final String[] PITCHBEND_TYPES = { "Off", "CC", "NRPN", "RPN", "Sysex", "Pitch Bend" };
    public static final String[] BUTTON_TYPES = { "Off", "CC", "NRPN", "RPN", "Sysex", "MMC", "Note", "Bank", "Program", "Template", "Real Time" };
    public static final String[] DRUMPAD_TYPES = { "Off", "CC", "NRPN", "RPN", "Sysex", "MMC", "Drum Note", "Bank", "Program", "Template", "Real Time" };
    public static final String[] MMCS = { "Stop", "Play", "Deferred Play", "Forward", "Rewind", "Record", "Record Exit", "Record Pause", "Pause", "Eject", "Chase", "Command Error Reset", "MMC Reset" };
    public static final String[] OFF_SYNC_VALUES = { "Timer", "32nd T", "32nd", "16th T", "16th", "8th T", "16th D", "8th", "4th T", "8th D", "4th", "2nd T", "4th D", "2nd", "1 Bar T", "2nd D", "1 Bar", "2 Bar T", "1 Bar d", "2 Bars", "4 Bar T", "3 Bars", "5 Bar T", "4 Bars", "3 Bar D", "7 Bar T", "5 Bars", "8 Bar T", "6 Bars", "7 Bars", "5 Bar D", "8 Bars", "9 Bars", "7 Bar D", "12 Bars" };
    public static final String[] BUTTON_STATES = { "Normal/Off", "Momentary", "Toggle", "Step" };
    public static final String[] REALTIME_BUTTON_STATES = { "Normal", "Momentary", "Toggle" };
    public static final String[] BANK_CHANGE_MODES = { "LSB", "MSB", "MSB-LSB" };
    public static final String[] PROGRAM_CHANGE_MODES = { "Off", "LSB", "MSB", "MSB-LSB" };
    public static final String[] DRUMPAD_STATES = { "Normal/Off", "Velocity", "Toggle", "Step" };
    public static final String[] REALTIME_DRUMPAD_STATES = { "Normal", "Momentary" };
    public static final String[] REAL_TIME_VALUES = { "Start Clock", "Continue Clock", "Stop Clock", "Active Sensing", "System Reset" };
    public static final String[] POT_PICKUPS = { "Off", "On", "Template", "Global" };
    public static final String[] ENCODER_DISPLAYS = { "0-127", "-64/+63", "REL 1", "REL 2", "APOT", "0-16K" };
    public static final String[] POT_DISPLAYS = { "0-127", "-64/+63" };
    public static final String[] BUTTON_DISPLAYS = { "0-127", "-64/+63", "ON/OFF", "LED" };
    public static final String[] PITCHBEND_DISPLAYS = { "0-127", "-64/+63", "REL 1", "REL 2", "APOT" };
    public static final String[] COMMON_PORTS = { "MIDI 1", "MIDI 2", "MIDI 1 2", "USB 1", "USB 1 MIDI 1", "USB 1 MIDI 2", "USB 1 MIDI 1 2", "USB 2", "USB 2 MIDI 1", "USB 2 MIDI 2", "USB 2 MIDI 1 2",  "USB 3", "USB 3 MIDI 1", "USB 3 MIDI 2", "USB 3 MIDI 1 2", "Off" };
    public static final String[] PROGRAM_PORTS = { "Use Common","MIDI 1", "MIDI 2", "MIDI 1 2", "USB 1", "USB 1 MIDI 1", "USB 1 MIDI 2", "USB 1 MIDI 1 2", "USB 2", "USB 2 MIDI 1", "USB 2 MIDI 2", "USB 2 MIDI 1 2", "Off" };
    public static final String[] CONTROL_PORTS = { "Use Common", "Use Program", "MIDI 1", "MIDI 2", "MIDI 1 2", "USB 1", "USB 1 MIDI 1", "USB 1 MIDI 2", "USB 1 MIDI 1 2", "USB 2", "USB 2 MIDI 1", "USB 2 MIDI 2", "USB 2 MIDI 1 2",  "USB 3", "USB 3 MIDI 1", "USB 3 MIDI 2", "USB 3 MIDI 1 2", "Off" };
    public static final String[] KEYBOARD_PORTS = { "MIDI 1", "MIDI 2", "MIDI 1 2", "USB 1", "USB 1 MIDI 1", "USB 1 MIDI 2", "USB 1 MIDI 1 2", "USB 2", "USB 2 MIDI 1", "USB 2 MIDI 2", "USB 2 MIDI 1 2", "Off" };
    public static final String[] SINGLE_DV_TYPES = { "None", "Single", "Roland" };
    public static final String[] DOUBLE_DV_TYPES = { "None", "LSB-MSB", "MSB-LSB", "Roland" };
    /// argh, these are *also* called "types"
	public static final String[] TOUCHPAD_X_TYPES = { "No Spring/Hold", "Spring Left", "Spring Center", "Spring Right" };
	public static final String[] TOUCHPAD_Y_TYPES = { "No Spring/Hold", "Spring Down", "Spring Center", "Spring Up" };

    public static final String[] NOTES = new String[] { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };

	public static final int ENCODERS_INDEX = 0;
	public static final int POTS_INDEX = 8;
	public static final int FADERS_INDEX = 16;
	public static final int BUTTONS_INDEX = 24;
	public static final int DRUMPADS_INDEX = 56;
	public static final int EXPRESSION_INDEX = 64;
	public static final int SUSTAIN_INDEX = 65;
	public static final int MODWHEEL_INDEX = 66;
	public static final int PITCHBEND_INDEX = 67;
	public static final int X1_INDEX = 68;
	public static final int Y1_INDEX = 69;
	public static final int X2_INDEX = 70;
	public static final int Y2_INDEX = 71;
	public static final int REWIND_INDEX = 72;
	public static final int FF_INDEX = 73;
	public static final int STOP_INDEX = 74;
	public static final int PLAY_INDEX = 75;
	public static final int RECORD_INDEX = 76;
	public static final int LOOP_INDEX = 77;
	public static final int CROSSFADER_INDEX = 85;

	// For our throwaway we'll use pot pickup 0, which has  4 values
	Chooser throwaway = new Chooser("", this, "control" + 0 + "potpickup", new String[] { "1", "2", "3", "4" });

    public JFrame sprout()
        {
        JFrame frame = super.sprout();
        receiveCurrent.setEnabled(false);
        receivePatch.setEnabled(false);
        getAll.setEnabled(false);
        merge.setEnabled(false);
        return frame;
        }         

	// We're going to define "Send" as the SL "UPLOAD" command.  It only happens if the user requests it explicitly.
	public boolean getSendsParametersOnlyOnSendCurrentPatch() { return true; }
	
    public NovationSLMKII()
        {
        /// SOUND PANEL
                
        JComponent soundPanel = new SynthPanel(this);
        VBox vbox = new VBox();
        vbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        
        HBox hbox = new HBox();
        hbox.add(addGeneral(Style.COLOR_A()));
        hbox.addLast(addMIDIPorts(Style.COLOR_A()));
        vbox.add(hbox);
        
        vbox.add(addZone(1, Style.COLOR_B()));
        vbox.add(addZone(2, Style.COLOR_A()));
        vbox.add(addZone(3, Style.COLOR_B()));
        vbox.add(addZone(4, Style.COLOR_A()));
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("General", soundPanel);
                
                
		int index = 0;
		
        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        hbox = new HBox();
        hbox.add(addEncoder(1, index++, Style.COLOR_A()));
        hbox.addLast(addEncoder(2, index++, Style.COLOR_A()));
        vbox.add(hbox);
        hbox = new HBox();
        hbox.add(addEncoder(3, index++, Style.COLOR_B()));
        hbox.addLast(addEncoder(4, index++, Style.COLOR_B()));
        vbox.add(hbox);
        hbox = new HBox();
        hbox.add(addEncoder(5, index++, Style.COLOR_A()));
        hbox.addLast(addEncoder(6, index++, Style.COLOR_A()));
        vbox.add(hbox);
        hbox = new HBox();
        hbox.add(addEncoder(7, index++, Style.COLOR_B()));
        hbox.addLast(addEncoder(8, index++, Style.COLOR_B()));
        vbox.add(hbox);
        soundPanel.add(vbox, BorderLayout.CENTER);
		((SynthPanel)soundPanel).makePasteable("control");
        addTab("Encoders", soundPanel);

        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        hbox = new HBox();
        hbox.add(addPot(1, index++, Style.COLOR_B()));
        hbox.addLast(addPot(2, index++, Style.COLOR_B()));
        vbox.add(hbox);
        hbox = new HBox();
        hbox.add(addPot(3, index++, Style.COLOR_A()));
        hbox.addLast(addPot(4, index++, Style.COLOR_A()));
        vbox.add(hbox);
        hbox = new HBox();
        hbox.add(addPot(5, index++, Style.COLOR_B()));
        hbox.addLast(addPot(6, index++, Style.COLOR_B()));
        vbox.add(hbox);
        hbox = new HBox();
        hbox.add(addPot(7, index++, Style.COLOR_A()));
        hbox.addLast(addPot(8, index++, Style.COLOR_A()));
        vbox.add(hbox);
        soundPanel.add(vbox, BorderLayout.CENTER);
		((SynthPanel)soundPanel).makePasteable("control");
        addTab("Pots", soundPanel);

        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        hbox = new HBox();
        hbox.add(addPot(1, index++, Style.COLOR_A()));
        hbox.addLast(addPot(2, index++, Style.COLOR_A()));
        vbox.add(hbox);
        hbox = new HBox();
        hbox.add(addPot(3, index++, Style.COLOR_B()));
        hbox.addLast(addPot(4, index++, Style.COLOR_B()));
        vbox.add(hbox);
        hbox = new HBox();
        hbox.add(addPot(5, index++, Style.COLOR_A()));
        hbox.addLast(addPot(6, index++, Style.COLOR_A()));
        vbox.add(hbox);
        hbox = new HBox();
        hbox.add(addPot(7, index++, Style.COLOR_B()));
        hbox.addLast(addPot(8, index++, Style.COLOR_B()));
        vbox.add(hbox);
        soundPanel.add(vbox, BorderLayout.CENTER);
		((SynthPanel)soundPanel).makePasteable("control");
        addTab("Faders", soundPanel);
        

        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        hbox = new HBox();
        hbox.add(addButton(1, index++, Style.COLOR_B()));
        hbox.addLast(addButton(2, index++, Style.COLOR_B()));
        vbox.add(hbox);
        hbox = new HBox();
        hbox.add(addButton(3, index++, Style.COLOR_A()));
        hbox.addLast(addButton(4, index++, Style.COLOR_A()));
        vbox.add(hbox);
        hbox = new HBox();
        hbox.add(addButton(5, index++, Style.COLOR_B()));
        hbox.addLast(addButton(6, index++, Style.COLOR_B()));
        vbox.add(hbox);
        hbox = new HBox();
        hbox.add(addButton(7, index++, Style.COLOR_A()));
        hbox.addLast(addButton(8, index++, Style.COLOR_A()));
        vbox.add(hbox);
        soundPanel.add(vbox, BorderLayout.CENTER);
		((SynthPanel)soundPanel).makePasteable("control");
        addTab("Buttons A", soundPanel);
        

        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        hbox = new HBox();
        hbox.add(addButton(1+8, index++, Style.COLOR_A()));
        hbox.addLast(addButton(2+8, index++, Style.COLOR_A()));
        vbox.add(hbox);
        hbox = new HBox();
        hbox.add(addButton(3+8, index++, Style.COLOR_B()));
        hbox.addLast(addButton(4+8, index++, Style.COLOR_B()));
        vbox.add(hbox);
        hbox = new HBox();
        hbox.add(addButton(5+8, index++, Style.COLOR_A()));
        hbox.addLast(addButton(6+8, index++, Style.COLOR_A()));
        vbox.add(hbox);
        hbox = new HBox();
        hbox.add(addButton(7+8, index++, Style.COLOR_B()));
        hbox.addLast(addButton(8+8, index++, Style.COLOR_B()));
        vbox.add(hbox);
        soundPanel.add(vbox, BorderLayout.CENTER);
		((SynthPanel)soundPanel).makePasteable("control");
        addTab("B", soundPanel);
        
        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        hbox = new HBox();
        hbox.add(addButton(1+16, index++, Style.COLOR_B()));
        hbox.addLast(addButton(2+16, index++, Style.COLOR_B()));
        vbox.add(hbox);
        hbox = new HBox();
        hbox.add(addButton(3+16, index++, Style.COLOR_A()));
        hbox.addLast(addButton(4+16, index++, Style.COLOR_A()));
        vbox.add(hbox);
        hbox = new HBox();
        hbox.add(addButton(5+16, index++, Style.COLOR_B()));
        hbox.addLast(addButton(6+16, index++, Style.COLOR_B()));
        vbox.add(hbox);
        hbox = new HBox();
        hbox.add(addButton(7+16, index++, Style.COLOR_A()));
        hbox.addLast(addButton(8+16, index++, Style.COLOR_A()));
        vbox.add(hbox);
        soundPanel.add(vbox, BorderLayout.CENTER);
		((SynthPanel)soundPanel).makePasteable("control");
        addTab("C", soundPanel);
        
        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        hbox = new HBox();
        hbox.add(addButton(1+24, index++, Style.COLOR_A()));
        hbox.addLast(addButton(2+24, index++, Style.COLOR_A()));
        vbox.add(hbox);
        hbox = new HBox();
        hbox.add(addButton(3+24, index++, Style.COLOR_B()));
        hbox.addLast(addButton(4+24, index++, Style.COLOR_B()));
        vbox.add(hbox);
        hbox = new HBox();
        hbox.add(addButton(5+24, index++, Style.COLOR_A()));
        hbox.addLast(addButton(6+24, index++, Style.COLOR_A()));
        vbox.add(hbox);
        hbox = new HBox();
        hbox.add(addButton(7+24, index++, Style.COLOR_B()));
        hbox.addLast(addButton(8+24, index++, Style.COLOR_B()));
        vbox.add(hbox);
        soundPanel.add(vbox, BorderLayout.CENTER);
		((SynthPanel)soundPanel).makePasteable("control");
        addTab("D", soundPanel);
        
        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        hbox = new HBox();
        hbox.add(addDrumPad(1, index++, Style.COLOR_B()));
        hbox.addLast(addDrumPad(2, index++, Style.COLOR_B()));
        vbox.add(hbox);
        hbox = new HBox();
        hbox.add(addDrumPad(3, index++, Style.COLOR_A()));
        hbox.addLast(addDrumPad(4, index++, Style.COLOR_A()));
        vbox.add(hbox);
        hbox = new HBox();
        hbox.add(addDrumPad(5, index++, Style.COLOR_B()));
        hbox.addLast(addDrumPad(6, index++, Style.COLOR_B()));
        vbox.add(hbox);
        hbox = new HBox();
        hbox.add(addDrumPad(7, index++, Style.COLOR_A()));
        hbox.addLast(addDrumPad(8, index++, Style.COLOR_A()));
        vbox.add(hbox);
        soundPanel.add(vbox, BorderLayout.CENTER);
		((SynthPanel)soundPanel).makePasteable("control");
        addTab("Pads", soundPanel);
        
        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        hbox = new HBox();
        hbox.add(addPot(1, index++, Style.COLOR_A()));					/// EXPRESSION
        hbox.addLast(addButton(2, index++, Style.COLOR_A()));			/// SUSTAIN
        vbox.add(hbox);
        hbox = new HBox();
        hbox.add(addPot(3, index++, Style.COLOR_B()));					/// MOD WHEEL
        hbox.addLast(addPitchBend(index++, Style.COLOR_B()));			/// PITCH BEND
        vbox.add(hbox);
        hbox = new HBox();
        hbox.add(addPitchBend(index++, Style.COLOR_A()));				/// X1
        hbox.addLast(addPitchBend(index++, Style.COLOR_A()));			/// Y1
        vbox.add(hbox);
        hbox = new HBox();
        hbox.add(addPitchBend(index++, Style.COLOR_B()));				/// X2
        hbox.addLast(addPitchBend(index++, Style.COLOR_B()));			/// Y2
        vbox.add(hbox);
        soundPanel.add(vbox, BorderLayout.CENTER);
		((SynthPanel)soundPanel).makePasteable("control");
        addTab("Control", soundPanel);

        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        hbox = new HBox();
        hbox.add(addButton(1, index++, Style.COLOR_B()));				/// REWIND
        hbox.addLast(addButton(2, index++, Style.COLOR_B()));			/// FF
        vbox.add(hbox);
        hbox = new HBox();
        hbox.add(addButton(3, index++, Style.COLOR_A()));				/// STOP
        hbox.addLast(addButton(4, index++, Style.COLOR_A()));			/// PLAY
        vbox.add(hbox);
        hbox = new HBox();
        
        // Loop and Record are out of order, notice index++ not used
        hbox.add(addButton(6, index, Style.COLOR_B()));					/// LOOP
        hbox.addLast(addButton(5, index + 1, Style.COLOR_B()));			/// RECORD
        index += 2;
        vbox.add(hbox);
        vbox.add(addPot(7, CROSSFADER_INDEX, Style.COLOR_A()));			/// CROSS-FADER
        soundPanel.add(vbox, BorderLayout.CENTER);
		((SynthPanel)soundPanel).makePasteable("control");
        addTab("Transport", soundPanel);
        
        loadDefaults();        
        }
                
    public String getDefaultResourceFileName() { return "NovationSLMKII.init"; }
    public String getHTMLResourceFileName() { return "NovationSLMKII.html"; }

  public boolean gatherPatchInfo(String title, Model change, boolean writing)
	  {
	  int num = model.get("number") + 1;
	  JTextField number = new JTextField("" + num, 2);

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
			  showSimpleError(title, "The Patch Number must be an integer 1...32");
			  continue;
			  }
		  if (n < 1 || n > 32)
			  {
			  showSimpleError(title, "The Patch Number must be an integer 1...32");
			  continue;
			  }
							
		  change.set("number", n - 1);
		  return true;
		  }
	  }
	  
    public static String getSynthName() { return "Novation SL MKII"; }

    public String revisePatchName(String name)
        {
        name = super.revisePatchName(name);
        if (name == null) name = "";
        name = name + "                                  ";		// 34 spaces
        char[] chars = name.toCharArray();
        for(int i = 0; i < chars.length; i++)
            {
            if (chars[i] < 32 || chars[i] > 127)
                chars[i] = ' ';
            }
        return new String(chars).substring(0, 34);
        }
                                        
    public String reviseManufacturer(String name)
        {
        if (name == null) name = "";
        name = name + "             ";		// 13 spaces
        char[] chars = name.toCharArray();
        for(int i = 0; i < chars.length; i++)
            {
            if (chars[i] < 32 || chars[i] > 127)
                chars[i] = ' ';
            }
        return new String(chars).substring(0, 13);
        }
                                        
    /** Add the global patch category (name, id, number, etc.) */
    public JComponent addNameGlobal(Color color)
        {
        Category globalCategory = new Category(this, getSynthName(), color);
        globalCategory.makeUnresettable();
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        comp = new PatchDisplay(this, 6);
        vbox.add(comp);
 		hbox.add(vbox);
 		
        comp = new StringComponent("Patch Name", this, "name", 34, "Name must be up to 34 ASCII characters.")
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
        model.set("name", "Untitled");
        vbox.add(comp);

        comp = new StringComponent("Manufacturer", this, "manufacturer", 13, "Name must be up to 13 ASCII characters.")
            {
            public String replace(String val)
                {
                return reviseManufacturer(val);
                }
                                
            public void update(String key, Model model)
                {
                super.update(key, model);
                updateTitle();
                }
            };
        model.set("manufacturer", "Unknown");
        HBox inner = new HBox();
        inner.add(comp);
        inner.addLast(Stretch.makeHorizontalStretch());
        vbox.add(inner);
        hbox.add(vbox);

		comp = new LabelledDial("Template Size", this, "templatesize", color, 1, 32);
		hbox.add(comp);

		comp = new LabelledDial("Template Position", this, "templateposition", color, 1, 32);
		hbox.add(comp);
                
        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }


	public JComponent addGeneral(Color color)
		{
        Category category = new Category(this, "General", color);
        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
		comp = new Chooser("Touchpad X Type", this, "touchpadxtype", TOUCHPAD_X_TYPES);
		vbox.add(comp);

		comp = new Chooser("Touchpad Y Type", this, "touchpadytype", TOUCHPAD_Y_TYPES);
		vbox.add(comp);
				
		hbox.add(vbox);
		vbox = new VBox();

        comp = new CheckBox("Pot Pickup", this, "potpickup");
		vbox.add(comp);
		
        comp = new CheckBox("Aftertouch", this, "aftertouch");
		vbox.add(comp);

        comp = new CheckBox("Enable Keyboard Zones", this, "enablekeyboardzones");
		vbox.add(comp);
		hbox.add(vbox);

		comp = new LabelledDial("Velocity Curve", this, "velocitycurve", color, 0, 126, -1);
		hbox.add(comp);

		comp = new LabelledDial("Octave", this, "octavesetting", color, 0, 9, 4)
			{
			public double getStartAngle() { return 210; }
			};
		hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

	public JComponent addMIDIPorts(Color color)
		{
        Category category = new Category(this, "MIDI Ports", color);
        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
		comp = new Chooser("Common Ports", this, "commonports", COMMON_PORTS);
		vbox.add(comp);
		comp = new Chooser("Program Ports", this, "progports", PROGRAM_PORTS);
		vbox.add(comp);
		hbox.add(vbox);

		comp = new LabelledDial("Common Channel", this, "commonchannel", color, 0, 15, -1);
		hbox.add(comp);

		comp = new LabelledDial("Program Channel", this, "progchannel", color, 0, 15, -1);
		hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


	public JComponent addZone(int zone, Color color)
		{
        Category category = new Category(this, "Keyboard Zone " + zone, color);
		category.makePasteable("zone" + zone);

        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();

        comp = new Chooser("Ports", this, "zone" + zone + "ports", KEYBOARD_PORTS);
		vbox.add(comp);
        comp = new CheckBox("Aftertouch", this, "zone" + zone + "aftertouch");
		vbox.add(comp);

		hbox.add(vbox);
		vbox = new VBox();
		
        comp = new CheckBox("Pitch Bend", this, "zone" + zone + "pitchbend");
		vbox.add(comp);
        comp = new CheckBox("Mod Wheel", this, "zone" + zone + "modwheel");
		vbox.add(comp);

		hbox.add(vbox);
		
		comp = new LabelledDial("Channel", this, "zone" + zone + "channel", color, 0, 15, -1);
		hbox.add(comp);

		comp = new LabelledDial("Vel Offset", this, "zone" + zone + "veloffset", color, 0, 126, -1);
		hbox.add(comp);

		comp = new LabelledDial("Min Note", this, "zone" + zone + "minnote", color, 0, 127)
            {
            public String map(int value)
                {
                return NOTES[value % 12] + (value / 12 - 2);			/// FIXME: is this -2?                    
                }
            };
		hbox.add(comp);

		comp = new LabelledDial("Max Note", this, "zone" + zone + "maxnote", color, 0, 127)
		    {
            public String map(int value)
                {
                return NOTES[value % 12] + (value / 12 - 2);			/// FIXME: is this -2?                    
                }
            };
		hbox.add(comp);

		comp = new LabelledDial("Transpose", this, "zone" + zone + "transpose", color, 0, 127, 64);
		hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
		}





	public JComponent addEncoder(int num, final int index, Color color)
		{
        Category category = new Category(this, "Encoder " + num, color);
		category.makePasteable("control" + index);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
		final VBox outer = new VBox();

		HBox valbox = new HBox();

		StringComponent name = new StringComponent("Name", this, "control" + index + "name", 8, "Name must be 8 characters long")
			{
			public String replace(String val)
				{
				char[] chars = (val + "        ").toCharArray();
				char[] newchars = new char[12];
				for(int i = 0; i < newchars.length; i++)
					{
					if (chars[i] >= 32 && chars[i] < 127)
						newchars[i] = chars[i];
					else newchars[i] = 32; 
					}
				return new String(newchars);
				}
			};
		
       	final LabelledDial lowval = new LabelledDial("Low", this, "control" + index + "lowval", color, 0, 127)
       		{ public String map(int value) { return "" + (model.get("control" + index + "display") != 1 ? value : value - 64); } };
       	model.register("control" + index + "display", lowval);
        final LabelledDial highval = new LabelledDial("High", this, "control" + index + "highval", color, 0, 127)
       		{ public String map(int value) { return "" + (model.get("control" + index + "display") != 1 ? value : value - 64); } };
       	model.register("control" + index + "display", highval);
       	//final LabelledDial defaultval = new LabelledDial("Default", this, "control" + index + "defaultval", color, 0, 127)
       	//	{ public String map(int value) { return "" + (model.get("control" + index + "display") != 1 ? value : value - 64); } };
       	//model.register("control" + index + "display", defaultval);
       	final LabelledDial lowvalbig = new LabelledDial("Low", this, "control" + index + "lowvalbig", color, 0, 16383);
        final LabelledDial highvalbig = new LabelledDial("High", this, "control" + index + "highvalbig", color, 0, 16383);
       	//final LabelledDial defaultvalbig = new LabelledDial("Default", this, "control" + index + "defaultvalbig", color, 0, 16383);
        final LabelledDial parammsb = new LabelledDial("Param MSB", this, "control" + index + "parammsb", color, 0, 127);
        final LabelledDial paramlsb = new LabelledDial("Param LSB", this, "control" + index + "paramlsb", color, 0, 127);
        //final Chooser mmctype = new Chooser("Command", this, "control" + index + "mmctype", MMCS);
        //final LabelledDial mmcDevice = new LabelledDial("Device ID", this, "control" + index + "mmcdevice", color, 0, 127);
        //final LabelledDial template = new LabelledDial("Template", this, "control" + index + "templatenumber", color, 1, 32);
        //final LabelledDial drumautooff = new LabelledDial("Auto-Off", this, "control" + index + "autooff", color, 0, 127);
        //final Chooser offsyncval = new Chooser("Off Sync Value", this, "control" + index + "drumoffsync", OFF_SYNC_VALUES);
        //final Chooser buttontype = new Chooser("Button Type", this, "control" + index + "buttontype", BUTTON_STATES);
        //final Chooser realtimenotebuttontype = new Chooser("Button Type", this, "control" + index + "realtimenotebuttontype", REALTIME_BUTTON_STATES);
        //final Chooser bankmode = new Chooser("Bank Mode", this, "control" + index + "bankmode", BANK_CHANGE_MODES);
        //final Chooser pcmode = new Chooser("Bank Mode", this, "control" + index + "pcbankmode", PROGRAM_CHANGE_MODES);		// yes it says bank mode
        //final Chooser realtime = new Chooser("Press", this, "control" + index + "realtime", REAL_TIME_VALUES);
        //final Chooser realtimerelease = new Chooser("Release", this, "control" + index + "realtime", REAL_TIME_VALUES);
        //final Chooser potpickup = new Chooser("Pot Pickup", this, "control" + index + "potpickup", POT_PICKUPS);
		final SysexBox sysexBox = new SysexBox(this, index, color);
        final Chooser display = new Chooser("Display Type", this, "control" + index + "display", ENCODER_DISPLAYS)
        	{
        	public void update(String key, Model model)
        		{
        		super.update(key, model);
        		valbox.removeAll();
        		if (model.get(key) == 5)	// 16 bit
        			{
					valbox.add(lowvalbig);
					valbox.add(highvalbig);
					//valbox.add(defaultvalbig);
        			sysexBox.setShows16K(true);
        			}
        		else
        			{
					valbox.add(lowval);
					valbox.add(highval);
					//valbox.add(defaultval);
        			sysexBox.setShows16K(false);
        			}
        		valbox.revalidate();
        		valbox.repaint();
        		}
        	};
        final Chooser ports = new Chooser("Ports", this, "control" + index + "ports", CONTROL_PORTS);
        //LabelledDial stepsize = new LabelledDial("Step Size", this, "control" + index + "stepsize", color, 0, 63)
        //	{
        //	public String map(int val)
        //		{
        //		return "" + (val + 1);
        //		}
        //	};
        final LabelledDial channel = new LabelledDial("Channel", this, "control" + index + "channel", color, 0, 17)
        	{
        	public String map(int val)
        		{
        		if (val == 0) return "Comm";
        		if (val == 1) return "Key";
        		return "" + (val - 1);
        		}
        	};
        	

/*
//// SYSEX
Min
Max
Default
Display Format
Sysex stuff

//// CC
MIDI Channel
Params LSB
Min
Max
Default
Display Format

//// NRPN or RPN
MIDI Channel
Params MSB
Params LSB
Min
Max
Default
Display Format
*/



        Chooser type = new Chooser("Type", this, "control" + index + "type", ENCODER_TYPES)
        	{
        	public void update(String key, Model model)
        		{
        		super.update(key, model);
        		outer.removeAll();
        		int val = model.get(key);
        		if (val == 0)				// OFF
        			{
        			// nothing
        			}
        		else if (val == 1 || val == 2 || val == 3)			// CC, RPN, NRPN
        			{
					HBox hbox2 = new HBox();
        			hbox2.add(display);
        			outer.add(hbox2);
        			hbox2 = new HBox();
        			hbox2.add(channel);
        			if (val == 1)	// CC
        				{
        				hbox2.add(paramlsb.setLabel("CC Number"));
        				}
        			else
        				{
        				hbox2.add(paramlsb.setLabel(val == 2 ? "NRPN LSB" : "RPN LSB"));
        				hbox2.add(parammsb.setLabel(val == 2 ? "NRPN MSB" : "RPN MSB"));
        				}
        			hbox2.add(valbox);
        			outer.add(hbox2);
        			}
				else 	// if (val == 4)			// Sysex
					{
					HBox hbox2 = new HBox();
        			hbox2.add(display);
        			outer.add(hbox2);
        			hbox2 = new HBox();
        			hbox2.add(valbox);
					hbox2.add(sysexBox);
					outer.add(hbox2);
					}
        		outer.revalidate();
        		outer.repaint();
        		} 
        	};

		valbox.add(lowval);
		valbox.add(highval);
		//valbox.add(defaultval);

		vbox.add(name);
		vbox.add(type);
		vbox.add(ports);
		hbox.add(vbox);

		hbox.add(outer);
		
        category.add(hbox, BorderLayout.CENTER);
        return category;
		}





	public JComponent addPot(int num, final int index, Color color)
		{
        Category category = new Category(this, 
        	(index == EXPRESSION_INDEX ? "Expression Pedal" : 
        		(index == MODWHEEL_INDEX ? "Modulation Wheel" : 
        			(index == CROSSFADER_INDEX ? "Cross-Fader" : 		// note not CROSSFADER_INDEX, because that value is misleading
        				(index < FADERS_INDEX ? "Pot " + num : "Fader " + num)))), color);
        
		category.makePasteable("control" + index);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
		final VBox outer = new VBox();


		StringComponent name = new StringComponent("Name", this, "control" + index + "name", 8, "Name must be 8 characters long")
			{
			public String replace(String val)
				{
				char[] chars = (val + "        ").toCharArray();
				char[] newchars = new char[12];
				for(int i = 0; i < newchars.length; i++)
					{
					if (chars[i] >= 32 && chars[i] < 127)
						newchars[i] = chars[i];
					else newchars[i] = 32; 
					}
				return new String(newchars);
				}
			};
		
       	final LabelledDial lowval = new LabelledDial("Low", this, "control" + index + "lowval", color, 0, 127)
       		{ public String map(int value) { return "" + (model.get("control" + index + "display") != 1 ? value : value - 64); } };
       	model.register("control" + index + "display", lowval);
       	model.register("control" + index + "type", lowval);
        final LabelledDial highval = new LabelledDial("High", this, "control" + index + "highval", color, 0, 127)
       		{ public String map(int value) { return "" + (model.get("control" + index + "display") != 1 ? value : value - 64); } };
       	model.register("control" + index + "display", highval);
       	//final LabelledDial defaultval = new LabelledDial("Default", this, "control" + index + "defaultval", color, 0, 127)
       	//	{ public String map(int value) { return "" + (model.get("control" + index + "display") != 1 ? value : value - 64); } };
       	//model.register("control" + index + "display", defaultval);
        final LabelledDial parammsb = new LabelledDial("Param MSB", this, "control" + index + "parammsb", color, 0, 127);
        final LabelledDial paramlsb = new LabelledDial("Param LSB", this, "control" + index + "paramlsb", color, 0, 127);
        //final Chooser mmctype = new Chooser("Command", this, "control" + index + "mmctype", MMCS);
        //final LabelledDial mmcDevice = new LabelledDial("Device ID", this, "control" + index + "mmcdevice", color, 0, 127);
        //final LabelledDial template = new LabelledDial("Template", this, "control" + index + "templatenumber", color, 1, 32);
        //final LabelledDial drumautooff = new LabelledDial("Auto-Off", this, "control" + index + "autooff", color, 0, 127);
        //final Chooser offsyncval = new Chooser("Off Sync Value", this, "control" + index + "drumoffsync", OFF_SYNC_VALUES);
        //final Chooser buttontype = new Chooser("Button Type", this, "control" + index + "buttontype", BUTTON_STATES);
        //final Chooser realtimenotebuttontype = new Chooser("Button Type", this, "control" + index + "realtimenotebuttontype", REALTIME_BUTTON_STATES);
        //final Chooser bankmode = new Chooser("Bank Mode", this, "control" + index + "bankmode", BANK_CHANGE_MODES);
        //final Chooser pcmode = new Chooser("Bank Mode", this, "control" + index + "pcbankmode", PROGRAM_CHANGE_MODES);		// yes it says bank mode
        //final Chooser realtime = new Chooser("Press", this, "control" + index + "realtime", REAL_TIME_VALUES);
        //final Chooser realtimerelease = new Chooser("Release", this, "control" + index + "realtime", REAL_TIME_VALUES);
        final Chooser potpickup = new Chooser("Pot Pickup", this, "control" + index + "potpickup", POT_PICKUPS);
        final Chooser display = new Chooser("Display Type", this, "control" + index + "display", POT_DISPLAYS);
        final Chooser ports = new Chooser("Ports", this, "control" + index + "ports", CONTROL_PORTS);
		final SysexBox sysexBox = new SysexBox(this, index, color);
        //LabelledDial stepsize = new LabelledDial("Step Size", this, "control" + index + "stepsize", color, 0, 63)
        //	{
        //	public String map(int val)
        //		{
        //		return "" + (val + 1);
        //		}
        //	};
        final LabelledDial channel = new LabelledDial("Channel", this, "control" + index + "channel", color, 0, 17)
        	{
        	public String map(int val)
        		{
        		if (val == 0) return "Comm";
        		if (val == 1) return "Key";
        		return "" + (val - 1);
        		}
        	};
        	

/*
//// SYSEX
Min
Max
Default
Display Format
Sysex stuff

//// CC
MIDI Channel
Params LSB
Min
Max
Default
Display Format

//// NRPN or RPN
MIDI Channel
Params MSB
Params LSB
Min
Max
Default
Display Format
*/



        Chooser type = new Chooser("Type", this, "control" + index + "type", POT_TYPES)
        	{
        	public void update(String key, Model model)
        		{
        		super.update(key, model);
        		outer.removeAll();
        		int val = model.get(key);
        		if (val == 0)				// OFF
        			{
        			// nothing
        			}
        		else if (val == 1 || val == 2 || val == 3)			// CC, RPN, NRPN
        			{
					HBox hbox2 = new HBox();
        			hbox2.add(display);
        			outer.add(hbox2);
					hbox2 = new HBox();
        			hbox2.add(channel);
        			if (val == 1)	// CC
        				{
        				hbox2.add(paramlsb.setLabel("CC Number"));
        				}
        			else
        				{
        				hbox2.add(paramlsb.setLabel(val == 2 ? "NRPN LSB" : "RPN LSB"));
        				hbox2.add(parammsb.setLabel(val == 2 ? "NRPN MSB" : "RPN MSB"));
        				}
        			hbox2.add(lowval);
        			hbox2.add(highval);
        			//hbox2.add(defaultval);
        			outer.add(hbox2);
        			}
				else 	// if (val == 4)			// Sysex
					{
					HBox hbox2 = new HBox();
        			hbox2.add(display);
        			outer.add(hbox2);
					hbox2 = new HBox();
        			hbox2.add(lowval);
        			hbox2.add(highval);
        			//hbox2.add(defaultval);
					hbox2.add(sysexBox);			// FIXME -- gotta specify 16 bit
					outer.add(hbox2);
					}
        		outer.revalidate();
        		outer.repaint();
        		} 
        	};


		vbox.add(name);
		HBox inner = new HBox();
		inner.add(type);
		inner.add(potpickup);
		vbox.add(inner);
		vbox.add(ports);
		hbox.add(vbox);

		hbox.add(outer);
		
        category.add(hbox, BorderLayout.CENTER);
        return category;
		}

		
	public JComponent addButton(int num, final int index, Color color)
		{
        Category category = new Category(this, 
        	(index < DRUMPADS_INDEX ? "Button " + num :
        		(index == REWIND_INDEX ? "Rewind" :
        			(index == SUSTAIN_INDEX ? "Sustain Pedal" :
	        			(index == FF_INDEX ? "Fast Foward" :
	        				(index == STOP_INDEX ? "Stop" :
	        					(index == PLAY_INDEX ? "Play" :
	        						(index == RECORD_INDEX ? "Record" : "Loop"))))))), color);
        
		category.makePasteable("control" + index);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
		final VBox outer = new VBox();


		StringComponent name = new StringComponent("Name", this, "control" + index + "name", 8, "Name must be 8 characters long")
			{
			public String replace(String val)
				{
				char[] chars = (val + "        ").toCharArray();
				char[] newchars = new char[12];
				for(int i = 0; i < newchars.length; i++)
					{
					if (chars[i] >= 32 && chars[i] < 127)
						newchars[i] = chars[i];
					else newchars[i] = 32; 
					}
				return new String(newchars);
				}
			};
		
       	final LabelledDial lowval = new LabelledDial("Low", this, "control" + index + "lowval", color, 0, 127)
       		{
       		public String map(int value)
       			{
       			if (model.get("control" + index + "type") == 6)			// Note
       				{
                	return NOTES[value % 12] + (value / 12 - 2);			/// FIXME: is this -2?                    
       				}
       			else
       				{
       				return "" + ((model.get("control" + index + "display") != 1 ||
       							model.get("control" + index + "type") > 4) ? value : value - 64); 
       				}
       			}
       		};
       	model.register("control" + index + "display", lowval);
       	model.register("control" + index + "type", lowval);
        final LabelledDial highval = new LabelledDial("High", this, "control" + index + "highval", color, 0, 127)
       		{ 
       		public String map(int value) 
       			{ 
       				return "" + ((model.get("control" + index + "display") != 1 ||
       							model.get("control" + index + "type") > 4) ? value : value - 64); 
       			} 
       		};
       	model.register("control" + index + "display", highval);
       	model.register("control" + index + "type", highval);
       	//final LabelledDial defaultval = new LabelledDial("Default", this, "control" + index + "defaultval", color, 0, 127)
       	//	{ 
       	//	public String map(int value) 
       	//		{ 
       	//			return "" + ((model.get("control" + index + "display") != 1 ||
       	//						model.get("control" + index + "type") > 4) ? value : value - 64); 
       	//		} 
       	//	};
       	//model.register("control" + index + "display", defaultval);
       	//model.register("control" + index + "type", defaultval);
        final LabelledDial parammsb = new LabelledDial("Param MSB", this, "control" + index + "parammsb", color, 0, 127);
        final LabelledDial paramlsb = new LabelledDial("Param LSB", this, "control" + index + "paramlsb", color, 0, 127);
        final Chooser mmctype = new Chooser("Command", this, "control" + index + "mmctype", MMCS);
        final LabelledDial mmcDevice = new LabelledDial("Device ID", this, "control" + index + "mmcdevice", color, 0, 127);
        final LabelledDial template = new LabelledDial("Template", this, "control" + index + "templatenumber", color, 1, 32);
        //final LabelledDial drumautooff = new LabelledDial("Auto-Off", this, "control" + index + "autooff", color, 0, 127);
        //final Chooser offsyncval = new Chooser("Off Sync Value", this, "control" + index + "drumoffsync", OFF_SYNC_VALUES);
        final Chooser buttontype = new Chooser("Button Type", this, "control" + index + "buttontype", BUTTON_STATES);
        final Chooser realtimenotebuttontype = new Chooser("Button Type", this, "control" + index + "realtimenotebuttontype", REALTIME_BUTTON_STATES);
        final Chooser bankmode = new Chooser("Bank Mode", this, "control" + index + "bankmode", BANK_CHANGE_MODES);
        final Chooser pcmode = new Chooser("Bank Mode", this, "control" + index + "pcbankmode", PROGRAM_CHANGE_MODES);		// yes it says bank mode
        final Chooser realtime = new Chooser("Press", this, "control" + index + "realtime", REAL_TIME_VALUES);
        final Chooser realtimerelease = new Chooser("Release", this, "control" + index + "realtime", REAL_TIME_VALUES);
        //final Chooser potpickup = new Chooser("Pot Pickup", this, "control" + index + "potpickup", POT_PICKUPS);
        final Chooser display = new Chooser("Display Type", this, "control" + index + "display", BUTTON_DISPLAYS);
        final Chooser ports = new Chooser("Ports", this, "control" + index + "ports", CONTROL_PORTS);
		final SysexBox sysexBox = new SysexBox(this, index, color);
        LabelledDial stepsize = new LabelledDial("Step Size", this, "control" + index + "stepsize", color, 0, 63)
        	{
        	public String map(int val)
        		{
        		return "" + (val + 1);
        		}
        	};
        final LabelledDial channel = new LabelledDial("Channel", this, "control" + index + "channel", color, 0, 17)
        	{
        	public String map(int val)
        		{
        		if (val == 0) return "Comm";
        		if (val == 1) return "Key";
        		return "" + (val - 1);
        		}
        	};
        	
/*
//// MMC
MMC Device
MMC Function

//// NOTE ON/OFF
MIDI Channel
Note Number
Note Vel
Button Type	[Not including Step]

//// Bank Select
MIDI Channel
Button Type	[special to Bank Select]
Bank MSB
Bank LSB

//// PC
MIDI Channel
Min
Button Type	[special to PC]
Step Size
Bank MSB
Bank LSB

//// SYSEX
Min
Max
Default
Button Type	[Including Step]
Display Format
Step Size
Sysex stuff

//// CC
MIDI Channel
Params LSB
Min
Max
Default
Button Type	[Including Step]
Display Format
Step Size

//// NRPN or RPN
MIDI Channel
Params MSB
Params LSB
Min
Max
Default
Button Type	[Including Step]
Display Format
Step Size
*/


        Chooser type = new Chooser("Type", this, "control" + index + "type", BUTTON_TYPES)
        	{
        	public void update(String key, Model model)
        		{
        		super.update(key, model);
        		outer.removeAll();
        		int val = model.get(key);
        		if (val == 0)				// OFF
        			{
        			// nothing
        			}
        		else if (val == 1 || val == 2 || val == 3)			// CC, RPN, NRPN
        			{
        			HBox hbox2 = new HBox();
        			hbox2.add(display);
        			hbox2.add(buttontype);
        			outer.add(hbox2);
        			hbox2 = new HBox();
        			hbox2.add(channel);
        			if (val == 1)	// CC
        				{
        				hbox2.add(paramlsb.setLabel("CC Number"));
        				}
        			else
        				{
        				hbox2.add(paramlsb.setLabel(val == 2 ? "NRPN LSB" : "RPN LSB"));
        				hbox2.add(parammsb.setLabel(val == 2 ? "NRPN MSB" : "RPN MSB"));
        				}
        			hbox2.add(lowval.setLabel("Press"));
        			hbox2.add(highval.setLabel("Release"));
        			//hbox2.add(defaultval.setLabel("Default"));
        			hbox2.add(stepsize);
        			outer.add(hbox2);
        			}
				else if (val == 4)	// Sysex
					{
        			HBox hbox2 = new HBox();
        			hbox2.add(display);
        			hbox2.add(buttontype);
        			outer.add(hbox2);
        			hbox2 = new HBox();
        			hbox2.add(lowval.setLabel("Press"));
        			hbox2.add(highval.setLabel("Release"));
        			//hbox2.add(defaultval.setLabel("Default"));
        			hbox2.add(stepsize);
					hbox2.add(sysexBox);			// FIXME -- gotta specify 16 bit
        			outer.add(hbox2);
					}
        		else if (val == 5)	// MMC
        			{
        			HBox hbox2 = new HBox();
        			hbox2.add(mmctype);
        			outer.add(hbox2);
        			hbox2 = new HBox();
        			hbox2.add(mmcDevice);
        			outer.add(hbox2);
        			}
        		else if (val == 6)			// Note On/Off
        			{
        			HBox hbox2 = new HBox();
        			hbox2.add(realtimenotebuttontype);
        			outer.add(hbox2);
        			hbox2 = new HBox();
        			hbox2.add(lowval.setLabel("Note"));
        			hbox2.add(highval.setLabel("Velocity"));
        			outer.add(hbox2);
        			}
        		else if (val == 7)			// Bank Select
        			{
        			HBox hbox2 = new HBox();
        			hbox2.add(bankmode);
        			outer.add(hbox2);
        			hbox2 = new HBox();
        			hbox2.add(paramlsb.setLabel("Bank LSB"));
        			hbox2.add(parammsb.setLabel("Bank MSB"));
        			outer.add(hbox2);
        			}
        		else if (val == 8)			// PC
        			{
        			HBox hbox2 = new HBox();
        			hbox2.add(pcmode);
        			hbox2.add(buttontype);
        			outer.add(hbox2);
        			hbox2 = new HBox();
        			hbox2.add(paramlsb.setLabel("Bank LSB"));
        			hbox2.add(parammsb.setLabel("Bank MSB"));
        			hbox2.add(lowval.setLabel("Press"));		// "PRESS 1" or "Press"
        			hbox2.add(highval.setLabel("Release"));		// "PRESS 2" or "Release"
         			outer.add(hbox2);
       			}
				else if (val == 9)						/// Template
					{
					outer.add(Strut.makeStrut(throwaway));
					outer.add(template);
					}
				else //	if (val == 10)					/// Real Time
					{
        			outer.add(realtimenotebuttontype);
        			outer.add(realtime);
        			outer.add(realtimerelease);
					}
        		outer.revalidate();
        		outer.repaint();
        		} 
        	};


		vbox.add(name);
		vbox.add(type);
		vbox.add(ports);
		hbox.add(vbox);

		hbox.add(outer);
		
        category.add(hbox, BorderLayout.CENTER);
        return category;
		}

		

		
	public JComponent addDrumPad(int num, final int index, Color color)
		{
        Category category = new Category(this, "Drum Pad " + num, color);
        
		category.makePasteable("control" + index);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
		final VBox outer = new VBox();


		StringComponent name = new StringComponent("Name", this, "control" + index + "name", 8, "Name must be 8 characters long")
			{
			public String replace(String val)
				{
				char[] chars = (val + "        ").toCharArray();
				char[] newchars = new char[12];
				for(int i = 0; i < newchars.length; i++)
					{
					if (chars[i] >= 32 && chars[i] < 127)
						newchars[i] = chars[i];
					else newchars[i] = 32; 
					}
				return new String(newchars);
				}
			};
		
       	final LabelledDial lowval = new LabelledDial("Low", this, "control" + index + "lowval", color, 0, 127)
       		{
       		public String map(int value)
       			{
       			if (model.get("control" + index + "type") == 6)	// Note or Drum Note
       				{
                	return NOTES[value % 12] + (value / 12 - 2);			/// FIXME: is this -2?                    
       				}
       			else
       				{
       				return "" + ((model.get("control" + index + "display") != 1 ||
       							model.get("control" + index + "type") > 4) ? value : value - 64); 
       				}
       			}
       		};
       	model.register("control" + index + "display", lowval);
       	model.register("control" + index + "type", lowval);
        final LabelledDial highval = new LabelledDial("High", this, "control" + index + "highval", color, 0, 127)
       		{ 
       		public String map(int value) 
       			{ 
       				return "" + ((model.get("control" + index + "display") != 1 ||
       							model.get("control" + index + "type") > 4) ? value : value - 64); 
       			} 
       		};
       	model.register("control" + index + "display", highval);
       	model.register("control" + index + "type", highval);
       	//final LabelledDial defaultval = new LabelledDial("Default", this, "control" + index + "defaultval", color, 0, 127)
       	//	{ 
       	//	public String map(int value) 
       	//		{ 
       	//			return "" + ((model.get("control" + index + "display") != 1 ||
       	//						model.get("control" + index + "type") > 4) ? value : value - 64); 
       	//		} 
       	//	};
       	//model.register("control" + index + "display", defaultval);
       	//model.register("control" + index + "type", defaultval);
        final LabelledDial parammsb = new LabelledDial("Param MSB", this, "control" + index + "parammsb", color, 0, 127);
        final LabelledDial paramlsb = new LabelledDial("Param LSB", this, "control" + index + "paramlsb", color, 0, 127);
        final Chooser mmctype = new Chooser("Command", this, "control" + index + "mmctype", MMCS);
        final LabelledDial mmcDevice = new LabelledDial("Device ID", this, "control" + index + "mmcdevice", color, 0, 127);
        final LabelledDial template = new LabelledDial("Template", this, "control" + index + "templatenumber", color, 1, 32);
        final LabelledDial drumautooff = new LabelledDial("Auto-Off", this, "control" + index + "autooff", color, 0, 127);
        final Chooser offsyncval = new Chooser("Off Sync Value", this, "control" + index + "drumoffsync", OFF_SYNC_VALUES);
        final Chooser buttontype = new Chooser("Button Type", this, "control" + index + "buttontype", DRUMPAD_STATES);
        final Chooser realtimenotebuttontype = new Chooser("Button Type", this, "control" + index + "realtimenotebuttontype", REALTIME_DRUMPAD_STATES);
        final Chooser bankmode = new Chooser("Bank Mode", this, "control" + index + "bankmode", BANK_CHANGE_MODES);
        final Chooser pcmode = new Chooser("Bank Mode", this, "control" + index + "pcbankmode", PROGRAM_CHANGE_MODES);		// yes it says bank mode
        final Chooser realtime = new Chooser("Press", this, "control" + index + "realtime", REAL_TIME_VALUES);
        final Chooser realtimerelease = new Chooser("Release", this, "control" + index + "realtime", REAL_TIME_VALUES);
        //final Chooser potpickup = new Chooser("Pot Pickup", this, "control" + index + "potpickup", POT_PICKUPS);
        final Chooser display = new Chooser("Display Type", this, "control" + index + "display", BUTTON_DISPLAYS);
        final Chooser ports = new Chooser("Ports", this, "control" + index + "ports", CONTROL_PORTS);
		final SysexBox sysexBox = new SysexBox(this, index, color);
        LabelledDial stepsize = new LabelledDial("Step Size", this, "control" + index + "stepsize", color, 0, 63)
        	{
        	public String map(int val)
        		{
        		return "" + (val + 1);
        		}
        	};
        final LabelledDial channel = new LabelledDial("Channel", this, "control" + index + "channel", color, 0, 17)
        	{
        	public String map(int val)
        		{
        		if (val == 0) return "Comm";
        		if (val == 1) return "Key";
        		return "" + (val - 1);
        		}
        	};
        	
/*
//// MMC
MMC Device
MMC Function

//// DRUM NOTE
MIDI Channel
Note Number
Note Vel
Auto Off
Off Sync

//// Bank Select
MIDI Channel
Button Type	[special to Bank Select]
Bank MSB
Bank LSB

//// PC
MIDI Channel
Min
Button Type	[special to PC]
Step Size
Bank MSB
Bank LSB

//// SYSEX
Min
Max
Default
Button Type	[Including Step]
Display Format
Step Size
Sysex stuff

//// CC
MIDI Channel
Params LSB
Min
Max
Default
Button Type	[Including Step]
Display Format
Step Size

//// NRPN or RPN
MIDI Channel
Params MSB
Params LSB
Min
Max
Default
Button Type	[Including Step]
Display Format
Step Size
*/


        Chooser type = new Chooser("Type", this, "control" + index + "type", DRUMPAD_TYPES)
        	{
        	public void update(String key, Model model)
        		{
        		super.update(key, model);
        		outer.removeAll();
        		int val = model.get(key);
        		if (val == 0)				// OFF
        			{
        			// nothing
        			}
        		else if (val == 1 || val == 2 || val == 3)			// CC, RPN, NRPN
        			{
        			HBox hbox2 = new HBox();
        			hbox2.add(display);
        			hbox2.add(buttontype);
        			outer.add(hbox2);
        			hbox2 = new HBox();
        			hbox2.add(channel);
        			if (val == 1)	// CC
        				{
        				hbox2.add(paramlsb.setLabel("CC Number"));
        				}
        			else
        				{
        				hbox2.add(paramlsb.setLabel(val == 2 ? "NRPN LSB" : "RPN LSB"));
        				hbox2.add(parammsb.setLabel(val == 2 ? "NRPN MSB" : "RPN MSB"));
        				}
        			hbox2.add(lowval.setLabel("Press"));
        			hbox2.add(highval.setLabel("Release"));
        			//hbox2.add(defaultval.setLabel("Default"));
        			hbox2.add(stepsize);
        			outer.add(hbox2);
        			}
				else if (val == 4)	// Sysex
					{
        			HBox hbox2 = new HBox();
        			hbox2.add(display);
        			hbox2.add(buttontype);
        			outer.add(hbox2);
        			hbox2 = new HBox();
        			hbox2.add(lowval.setLabel("Press"));
        			hbox2.add(highval.setLabel("Release"));
        			//hbox2.add(defaultval.setLabel("Default"));
        			hbox2.add(stepsize);
					hbox2.add(sysexBox);			// FIXME -- gotta specify 16 bit
        			outer.add(hbox2);
					}
        		else if (val == 5)	// MMC
        			{
        			HBox hbox2 = new HBox();
        			hbox2.add(mmctype);
        			outer.add(hbox2);
        			hbox2 = new HBox();
        			hbox2.add(mmcDevice);
        			outer.add(hbox2);
        			}
        		else if (val == 6)			// Drum Note
        			{
        			HBox hbox2 = new HBox();
        			hbox2.add(offsyncval);
        			outer.add(hbox2);
        			hbox2 = new HBox();
        			hbox2.add(lowval.setLabel("Note"));
        			hbox2.add(highval.setLabel("Velocity Curve"));
        			hbox2.add(drumautooff);
        			outer.add(hbox2);
        			}
        		else if (val == 7)			// Bank Select
        			{
        			HBox hbox2 = new HBox();
        			hbox2.add(bankmode);
        			outer.add(hbox2);
        			hbox2 = new HBox();
        			hbox2.add(paramlsb.setLabel("Bank LSB"));
        			hbox2.add(parammsb.setLabel("Bank MSB"));
        			outer.add(hbox2);
        			}
        		else if (val == 8)			// PC
        			{
        			HBox hbox2 = new HBox();
        			hbox2.add(pcmode);
        			hbox2.add(buttontype);
        			outer.add(hbox2);
					hbox2 = new HBox();
        			hbox2.add(paramlsb.setLabel("Bank LSB"));
        			hbox2.add(parammsb.setLabel("Bank MSB"));
        			hbox2.add(lowval.setLabel("Press"));			// "PRESS 1" or "Press"
        			hbox2.add(highval.setLabel("Release"));			// "PRESS 2" or "Release"
        			outer.add(hbox2);
        			}
				else if (val == 9)						/// Template
					{
					outer.add(Strut.makeStrut(throwaway));
					outer.add(template);
					}
				else //	if (val == 10)					/// Real Time
					{
        			outer.add(realtimenotebuttontype);
        			outer.add(realtime);
        			outer.add(realtimerelease);
					}
        		outer.revalidate();
        		outer.repaint();
        		} 
        	};


		vbox.add(name);
		vbox.add(type);
		vbox.add(ports);
		hbox.add(vbox);

		hbox.add(outer);
		
        category.add(hbox, BorderLayout.CENTER);
        return category;
		}

		


	public JComponent addPitchBend(final int index, Color color)
		{
        Category category = new Category(this, 
        	(index == EXPRESSION_INDEX ? "Expression" :
	        	(index == PITCHBEND_INDEX ? "Pitch Bend" :
	        		(index == X1_INDEX ? "Touch Pad X1" :
	        			(index == X2_INDEX ? "Touch Pad X2" :
	        				(index == Y1_INDEX ? "Touch Pad Y1" : "Touch Pad Y2"))))), color);
        
		category.makePasteable("control" + index);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
		final VBox outer = new VBox();


		StringComponent name = new StringComponent("Name", this, "control" + index + "name", 8, "Name must be 8 characters long")
			{
			public String replace(String val)
				{
				char[] chars = (val + "        ").toCharArray();
				char[] newchars = new char[12];
				for(int i = 0; i < newchars.length; i++)
					{
					if (chars[i] >= 32 && chars[i] < 127)
						newchars[i] = chars[i];
					else newchars[i] = 32; 
					}
				return new String(newchars);
				}
			};
		
       	final LabelledDial lowval = new LabelledDial("Low", this, "control" + index + "lowval", color, 0, 127)
       		{ public String map(int value) { return "" + (model.get("control" + index + "display") != 1 ? value : value - 64); } };
       	model.register("control" + index + "display", lowval);
       	model.register("control" + index + "type", lowval);
        final LabelledDial highval = new LabelledDial("High", this, "control" + index + "highval", color, 0, 127)
       		{ public String map(int value) { return "" + (model.get("control" + index + "display") != 1 ? value : value - 64); } };
       	model.register("control" + index + "display", highval);
       	//final LabelledDial defaultval = new LabelledDial("Default", this, "control" + index + "defaultval", color, 0, 127)
       	//	{ public String map(int value) { return "" + (model.get("control" + index + "display") != 1 ? value : value - 64); } };
       	//model.register("control" + index + "display", defaultval);
        final LabelledDial parammsb = new LabelledDial("Param MSB", this, "control" + index + "parammsb", color, 0, 127);
        final LabelledDial paramlsb = new LabelledDial("Param LSB", this, "control" + index + "paramlsb", color, 0, 127);
        //final Chooser mmctype = new Chooser("Command", this, "control" + index + "mmctype", MMCS);
        //final LabelledDial mmcDevice = new LabelledDial("Device ID", this, "control" + index + "mmcdevice", color, 0, 127);
        //final LabelledDial template = new LabelledDial("Template", this, "control" + index + "templatenumber", color, 1, 32);
        //final LabelledDial drumautooff = new LabelledDial("Auto-Off", this, "control" + index + "autooff", color, 0, 127);
        //final Chooser offsyncval = new Chooser("Off Sync Value", this, "control" + index + "drumoffsync", OFF_SYNC_VALUES);
        //final Chooser buttontype = new Chooser("Button Type", this, "control" + index + "buttontype", BUTTON_STATES);
        //final Chooser realtimenotebuttontype = new Chooser("Button Type", this, "control" + index + "realtimenotebuttontype", REALTIME_BUTTON_STATES);
        //final Chooser bankmode = new Chooser("Bank Mode", this, "control" + index + "bankmode", BANK_CHANGE_MODES);
        //final Chooser pcmode = new Chooser("Bank Mode", this, "control" + index + "pcbankmode", PROGRAM_CHANGE_MODES);		// yes it says bank mode
        //final Chooser realtime = new Chooser("Press", this, "control" + index + "realtime", REAL_TIME_VALUES);
        //final Chooser realtimerelease = new Chooser("Release", this, "control" + index + "realtime", REAL_TIME_VALUES);
        //final Chooser potpickup = new Chooser("Pot Pickup", this, "control" + index + "potpickup", POT_PICKUPS);
        final Chooser display = new Chooser("Display Type", this, "control" + index + "display", PITCHBEND_DISPLAYS);
        final Chooser ports = new Chooser("Ports", this, "control" + index + "ports", CONTROL_PORTS);
		final SysexBox sysexBox = new SysexBox(this, index, color);
        //LabelledDial stepsize = new LabelledDial("Step Size", this, "control" + index + "stepsize", color, 0, 63)
        //	{
        // 	public String map(int val)
        //		{
        //		return "" + (val + 1);
        //		}
        //	};
        final LabelledDial channel = new LabelledDial("Channel", this, "control" + index + "channel", color, 0, 17)
        	{
        	public String map(int val)
        		{
        		if (val == 0) return "Comm";
        		if (val == 1) return "Key";
        		return "" + (val - 1);
        		}
        	};
        	
/*
//// SYSEX
Min
Max
Default
Display Format
Sysex stuff

//// CC
MIDI Channel
Params LSB
Min
Max
Default
Display Format

//// NRPN or RPN
MIDI Channel
Params MSB
Params LSB
Min
Max
Default
Display Format

//// PITCHBEND
MIDI Channel
Min
Max
Display Format
*/


        Chooser type = new Chooser("Type", this, "control" + index + "type", PITCHBEND_TYPES)
        	{
        	public void update(String key, Model model)
        		{
        		super.update(key, model);
        		outer.removeAll();
        		int val = model.get(key);
        		if (val == 0)				// OFF
        			{
        			// nothing
        			}
        		else if (val == 1 || val == 2 || val == 3)			// CC, RPN, NRPN
        			{
        			HBox hbox2 = new HBox();
        			hbox2.add(display);
	       			outer.add(hbox2);
         			hbox2 = new HBox();
        			hbox2.add(channel);
        			if (val == 1)	// CC
        				{
        				hbox2.add(paramlsb.setLabel("CC Number"));
        				}
        			else
        				{
        				hbox2.add(paramlsb.setLabel(val == 2 ? "NRPN LSB" : "RPN LSB"));
        				hbox2.add(parammsb.setLabel(val == 2 ? "NRPN MSB" : "RPN MSB"));
        				}
        			hbox2.add(lowval.setLabel("Low"));
        			hbox2.add(highval.setLabel("High"));
        			//hbox2.add(defaultval.setLabel("Low"));
	       			outer.add(hbox2);
        			}
				else if (val == 4)			// Sysex
					{
        			HBox hbox2 = new HBox();
        			hbox2.add(display);
	       			outer.add(hbox2);
         			hbox2 = new HBox();
        			hbox2.add(lowval);
        			hbox2.add(highval);
        			//hbox2.add(defaultval);
					hbox2.add(sysexBox);			// FIXME -- gotta specify 16 bit
	       			outer.add(hbox2);
					}
				else 	// if (val == 4)			// Pitch Bend
					{
        			HBox hbox2 = new HBox();
        			hbox2.add(display);
	       			outer.add(hbox2);
         			hbox2 = new HBox();
        			hbox2.add(channel);
        			hbox2.add(lowval);
        			hbox2.add(highval);
	       			outer.add(hbox2);
					}
        		outer.revalidate();
        		outer.repaint();
        		} 
        	};

		vbox.add(name);
		vbox.add(type);
		vbox.add(ports);
		hbox.add(vbox);

		hbox.add(outer);
		
        category.add(hbox, BorderLayout.CENTER);
        return category;
		}





    public Model getNextPatchLocation(Model model)
        {
        int number = model.get("number");
        
        number++;
        if (number >= 32)
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
        
        int number = model.get("number") + 1;
        return "" + (number > 9 ? "" : "0") + number;
        }







	void emitName(int index, int pos, byte[] data)
		{
		String name = (model.get("control" + index + "name", "") + "        ").substring(0, 8);
		char[] chars = name.toCharArray();
		for(int i = 0; i < 8; i++)
			{
			data[pos + i] = (byte)chars[i];
			}
		}
	
	static final byte[] TO_ENCODER_DISPLAY = new byte[] { 0x00, 0x01, 0x06, 0x07, 0x011, 0x09 };  // because 0-16k is last
	static final byte[] TO_CONTROL_PORTS = new byte[] { 0x00, 0x20, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4a, 0x4b, 0x50, 0x51, 0x52, 0x53, 0x40 };  // because OFF is last
	static final byte[] TO_CONTROL_CHANNELS = new byte[] { 0x00, 0x20, 0x40, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4a, 0x4b, 0x4c, 0x4d, 0x4e, 0x4f };
	static final byte[] TO_SINGLE_DV_TYPES = new byte[] { 0, 1, 4 };
	static final byte[] TO_DOUBLE_DV_TYPES = new byte[] { 0, 2, 3, 4 };

	void emitEncoder(int index, byte[] data)
		{
		int pos = index * 41 + 0x1a3;
		int display = model.get("control" + index + "display");
		emitName(index, pos, data);
		data[pos + 8] = (byte)model.get("control" + index + "type");			// 0, 1, 2, 3, 4
		data[pos + 14] = 0x50;
		data[pos + 15] = TO_ENCODER_DISPLAY[display];
		data[pos + 18] = TO_CONTROL_PORTS[model.get("control" + index + "ports")];
		data[pos + 19] = TO_CONTROL_CHANNELS[model.get("control" + index + "channel")];
		
		if (display == 5) // 16K
			{
			int lowvalbig = (byte)model.get("control" + index + "lowvalbig");
			data[pos+9] = (byte)((lowvalbig >>> 7) & 127);
			data[pos+10] = (byte)(lowvalbig & 127);
			int highvalbig = (byte)model.get("control" + index + "highvalbig");
			data[pos+11] = (byte)((highvalbig >>> 7) & 127);
			data[pos+12] = (byte)(highvalbig & 127);
			int defaultvalbig = (byte)model.get("control" + index + "defaultvalbig");
			data[pos+20] = (byte)((defaultvalbig >>> 7) & 127);
			data[pos+21] = (byte)(defaultvalbig & 127);
			}
		else
			{
			int lowval = (byte)model.get("control" + index + "lowval");
			data[pos+10] = (byte)(lowval & 127);
			int highval = (byte)model.get("control" + index + "highval");
			data[pos+12] = (byte)(highval & 127);
			int defaultval = (byte)model.get("control" + index + "defaultval");
			data[pos+21] = (byte)(defaultval & 127);
			}
		data[pos + 16] = (byte)model.get("control" + index + "parammsb");
		data[pos + 17] = (byte)model.get("control" + index + "paramlsb");
		boolean isRoland = false;
		if (display == 5)
			{
			int dvtype = model.get("control" + index + "sysexdoubledvtype");
			data[pos + 26] = TO_DOUBLE_DV_TYPES[dvtype];
			isRoland = (dvtype == 3);
			}
		else
			{
			int dvtype = model.get("control" + index + "sysexsingledvtype");
			data[pos + 26] = TO_SINGLE_DV_TYPES[dvtype];
			isRoland = (dvtype == 2);
			}
			
		if (isRoland)
			{
			data[pos + 27] = (byte)model.get("control" + index + "rolandsysexlength");
			data[pos + 28] = (byte)(data[pos + 27] - 2);	// roland position
			}
		else
			{
			data[pos + 27] = (byte)model.get("control" + index + "sysexlength");
			data[pos + 28] = (byte)(model.get("control" + index + "sysexdvpos") + 1);
			}

		for(int i = 0; i < 12; i++)
			{
			data[pos + 29 + i] = (byte)(model.get("control" + index + "sysex" + i));
			}
		}

    static final byte[] TO_POT_PICKUPS = new byte[] { 0x00, 0x20, 0x40, 0x60 };
    
	void emitPot(int index, byte[] data)
		{
		int pos = index * 41 + 0x1a3;
		emitName(index, pos, data);
		data[pos + 8] = (byte)model.get("control" + index + "type");		// 0, 1, 2, 3, or 4
		if (index != MODWHEEL_INDEX && index != EXPRESSION_INDEX)
			data[pos + 14] = TO_POT_PICKUPS[model.get("control" + index + "potpickup")];
		else
			data[pos + 14] = 0x50;
		data[pos + 15] = (byte)model.get("control" + index + "display");				// 0 or 1
		data[pos + 18] = TO_CONTROL_PORTS[model.get("control" + index + "ports")];
		data[pos + 19] = TO_CONTROL_CHANNELS[model.get("control" + index + "channel")];
		
		int lowval = (byte)model.get("control" + index + "lowval");
		data[pos+10] = (byte)(lowval & 127);
		int highval = (byte)model.get("control" + index + "highval");
		data[pos+12] = (byte)(highval & 127);
		int defaultval = (byte)model.get("control" + index + "defaultval");
		data[pos+21] = (byte)(defaultval & 127);

		data[pos + 16] = (byte)model.get("control" + index + "parammsb");
		data[pos + 17] = (byte)model.get("control" + index + "paramlsb");
		
		int dvtype = model.get("control" + index + "sysexsingledvtype");
		data[pos + 26] = TO_SINGLE_DV_TYPES[dvtype];
		boolean isRoland = (dvtype == 2);
			
		if (isRoland)
			{
			data[pos + 27] = (byte)model.get("control" + index + "rolandsysexlength");
			data[pos + 28] = (byte)(data[pos + 27] - 2);	// roland position
			}
		else
			{
			data[pos + 27] = (byte)model.get("control" + index + "sysexlength");
			data[pos + 28] = (byte)(model.get("control" + index + "sysexdvpos") + 1);
			}

		for(int i = 0; i < 12; i++)
			{
			data[pos + 29 + i] = (byte)(model.get("control" + index + "sysex" + i));
			}
		}
		
	static final byte[] TO_PITCH_BEND_DISPLAY = new byte[] { 0x00, 0x01, 0x06, 0x07, 0x011 };
	static final byte[] TO_PITCH_BEND_TYPES = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x0A };

	void emitPitchBend(int index, byte[] data)
		{
		int pos = index * 41 + 0x1a3;
		emitName(index, pos, data);
		data[pos + 8] = TO_PITCH_BEND_TYPES[model.get("control" + index + "type")];
		data[pos + 14] = 0x50;
		data[pos + 15] = TO_PITCH_BEND_DISPLAY[(byte)model.get("control" + index + "display")];
		data[pos + 18] = TO_CONTROL_PORTS[model.get("control" + index + "ports")];
		data[pos + 19] = TO_CONTROL_CHANNELS[model.get("control" + index + "channel")];
		
		int lowval = (byte)model.get("control" + index + "lowval");
		data[pos+10] = (byte)(lowval & 127);
		int highval = (byte)model.get("control" + index + "highval");
		data[pos+12] = (byte)(highval & 127);
		int defaultval = (byte)model.get("control" + index + "defaultval");
		data[pos+21] = (byte)(defaultval & 127);

		data[pos + 16] = (byte)model.get("control" + index + "parammsb");
		data[pos + 17] = (byte)model.get("control" + index + "paramlsb");
		
		int dvtype = model.get("control" + index + "sysexsingledvtype");
		data[pos + 26] = TO_SINGLE_DV_TYPES[dvtype];
		boolean isRoland = (dvtype == 2);
			
		if (isRoland)
			{
			data[pos + 27] = (byte)model.get("control" + index + "rolandsysexlength");
			data[pos + 28] = (byte)(data[pos + 27] - 2);	// roland position
			}
		else
			{
			data[pos + 27] = (byte)model.get("control" + index + "sysexlength");
			data[pos + 28] = (byte)(model.get("control" + index + "sysexdvpos") + 1);
			}

		for(int i = 0; i < 12; i++)
			{
			data[pos + 29 + i] = (byte)(model.get("control" + index + "sysex" + i));
			}
		}		
		

	static final byte[] TO_BUTTON_DISPLAYS = new byte[] { 0x00, 0x01, 0x03, 0x10 };
	static final byte[] TO_BUTTON_TYPES = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x08, 0x09, 0x0C, 0x0D };
	static final byte[] TO_BUTTON_STANDARD_TYPES = new byte[] { 0x00, 0x04, 0x08, 0x10 };
	static final byte[] TO_BUTTON_BANK_CHANGE_MODES = new byte[] { 0x00, 0x01, 0x02 };
	static final byte[] TO_BUTTON_PROGRAM_CHANGE_MODES = new byte[] { 0x00, 0x01, 0x02, 0x03 };
	static final byte[] TO_BUTTON_REAL_TIME = new byte[] { 0x51, 0x52, 0x53, 0x54, 0x55 };

	void emitButton(int index, byte[] data)
		{
		int pos = index * 41 + 0x1a3;
		int type = model.get("control" + index + "type");
		emitName(index, pos, data);
		data[pos + 8] = TO_BUTTON_TYPES[type];
		data[pos + 14] = 0x50;		// by default
		data[pos + 15] = TO_BUTTON_DISPLAYS[(byte)model.get("control" + index + "display")];
		data[pos + 18] = TO_CONTROL_PORTS[model.get("control" + index + "ports")];
		
		if (type == 5) 		// MMC
			{
			data[pos + 19] = (byte)model.get("control" + index + "mmcdevice");
			}
		else
			{
			data[pos + 19] = TO_CONTROL_CHANNELS[model.get("control" + index + "channel")];
			}
		
		
		if (type == 5)		// MMC
			{
			int lowval = (byte)model.get("control" + index + "mmctype") + 1;
			data[pos+10] = (byte)(lowval & 127);
			}
		else if (type == 6)	// Note on/off, same as Realtime
			{
			int lowval = (byte)model.get("control" + index + "lowval");
			data[pos+10] = (byte)(lowval & 127);
			int highval = (byte)model.get("control" + index + "highval");
			data[pos+12] = (byte)(highval & 127);
			}
		else if (type == 7) 			// Bank Select
			{
			data[pos + 16] = (byte)model.get("control" + index + "bankmsb");
			data[pos + 17] = (byte)model.get("control" + index + "banklsb");
			}
		else if (type == 8) 			// PC
			{
			int lowval = (byte)model.get("control" + index + "lowval");
			data[pos+10] = (byte)(lowval & 127);
			int highval = (byte)model.get("control" + index + "highval");
			data[pos+12] = (byte)(highval & 127);
			data[pos + 16] = (byte)model.get("control" + index + "bankmsb");
			data[pos + 17] = (byte)model.get("control" + index + "banklsb");
			}
		else if (type == 9)	// Template
			{
			int lowval = (byte)model.get("control" + index + "template");
			data[pos+10] = (byte)(lowval & 127);
			}
		else if (type == 10)	// Realtime
			{
			data[pos+14] = TO_BUTTON_REAL_TIME[model.get("control" + index + "realtime")];
			}
		else					// 0, 1, 2, 3, 4
			{
			int lowval = (byte)model.get("control" + index + "lowval");
			data[pos+10] = (byte)(lowval & 127);
			int highval = (byte)model.get("control" + index + "highval");
			data[pos+12] = (byte)(highval & 127);
			int defaultval = (byte)model.get("control" + index + "defaultval");
			data[pos+21] = (byte)(defaultval & 127);

			data[pos + 16] = (byte)model.get("control" + index + "parammsb");
			data[pos + 17] = (byte)model.get("control" + index + "paramlsb");
			}
			
		if (type == 7)		// Bank Select
			{
			data[pos + 13] = TO_BUTTON_BANK_CHANGE_MODES[model.get("control" + index + "pcbankmode")];
			}
		else if (type == 8)		// PC
			{
			data[pos + 13] = TO_BUTTON_PROGRAM_CHANGE_MODES[model.get("control" + index + "bankmode")];
			}
		else
			{
			data[pos + 13] = TO_BUTTON_STANDARD_TYPES[model.get("control" + index + "buttontype")];
			data[pos + 22] = (byte)model.get("control" + index + "stepsize");
			}

		
		int dvtype = model.get("control" + index + "sysexsingledvtype");
		data[pos + 26] = TO_SINGLE_DV_TYPES[dvtype];
		boolean isRoland = (dvtype == 2);
			
		if (isRoland)
			{
			data[pos + 27] = (byte)model.get("control" + index + "rolandsysexlength");
			data[pos + 28] = (byte)(data[pos + 27] - 2);	// roland position
			}
		else
			{
			data[pos + 27] = (byte)model.get("control" + index + "sysexlength");
			data[pos + 28] = (byte)(model.get("control" + index + "sysexdvpos") + 1);
			}

		for(int i = 0; i < 12; i++)
			{
			data[pos + 29 + i] = (byte)(model.get("control" + index + "sysex" + i));
			}
		}		
		
	



	static final byte[] TO_DRUM_PAD_TYPES = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x0B, 0x08, 0x09, 0x0C, 0x0D };

	void emitDrumPad(int index, byte[] data)
		{
		int pos = index * 41 + 0x1a3;
		int type = model.get("control" + index + "type");
		emitName(index, pos, data);
		data[pos + 8] = TO_DRUM_PAD_TYPES[type];
		data[pos + 14] = 0x50;		// by default
		data[pos + 15] = TO_BUTTON_DISPLAYS[(byte)model.get("control" + index + "display")];
		data[pos + 18] = TO_CONTROL_PORTS[model.get("control" + index + "ports")];
		
		if (type == 5) 		// MMC
			{
			data[pos + 19] = (byte)model.get("control" + index + "mmcdevice");
			}
		else
			{
			data[pos + 19] = TO_CONTROL_CHANNELS[model.get("control" + index + "channel")];
			}
		
		
		if (type == 5)		// MMC
			{
			int lowval = (byte)model.get("control" + index + "mmctype") + 1;
			data[pos+10] = (byte)(lowval & 127);
			}
		else if (type == 6)		// Drum note
			{
			int lowval = (byte)model.get("control" + index + "lowval");
			data[pos+10] = (byte)(lowval & 127);
			int highval = (byte)model.get("control" + index + "highval");
			data[pos+12] = (byte)(highval & 127);
			}
		else if (type == 7) 			// Bank Select
			{
			data[pos + 16] = (byte)model.get("control" + index + "bankmsb");
			data[pos + 17] = (byte)model.get("control" + index + "banklsb");
			}
		else if (type == 8) 			// PC
			{
			int lowval = (byte)model.get("control" + index + "lowval");
			data[pos+10] = (byte)(lowval & 127);
			int highval = (byte)model.get("control" + index + "highval");
			data[pos+12] = (byte)(highval & 127);
			data[pos + 16] = (byte)model.get("control" + index + "bankmsb");
			data[pos + 17] = (byte)model.get("control" + index + "banklsb");
			}
		else if (type == 9)	// Template
			{
			int lowval = (byte)model.get("control" + index + "template");
			data[pos+10] = (byte)(lowval & 127);
			}
		else if (type == 10)	// Realtime
			{
			data[pos+14] = TO_BUTTON_REAL_TIME[model.get("control" + index + "realtime")];
			}
		else					// 0, 1, 2, 3, 4
			{
			int lowval = (byte)model.get("control" + index + "lowval");
			data[pos+10] = (byte)(lowval & 127);
			int highval = (byte)model.get("control" + index + "highval");
			data[pos+12] = (byte)(highval & 127);
			int defaultval = (byte)model.get("control" + index + "defaultval");
			data[pos+21] = (byte)(defaultval & 127);

			data[pos + 16] = (byte)model.get("control" + index + "parammsb");
			data[pos + 17] = (byte)model.get("control" + index + "paramlsb");
			}
			
		if (type == 7)		// Bank Select
			{
			data[pos + 13] = TO_BUTTON_BANK_CHANGE_MODES[model.get("control" + index + "pcbankmode")];
			}
		else if (type == 8)		// PC
			{
			data[pos + 13] = TO_BUTTON_PROGRAM_CHANGE_MODES[model.get("control" + index + "bankmode")];
			}
		else
			{
			data[pos + 13] = TO_BUTTON_STANDARD_TYPES[model.get("control" + index + "buttontype")];
			data[pos + 22] = (byte)model.get("control" + index + "stepsize");
			}

		
		int dvtype = model.get("control" + index + "sysexsingledvtype");
		data[pos + 26] = TO_SINGLE_DV_TYPES[dvtype];
		boolean isRoland = (dvtype == 2);
			
		if (isRoland)
			{
			data[pos + 27] = (byte)model.get("control" + index + "rolandsysexlength");
			data[pos + 28] = (byte)(data[pos + 27] - 2);	// roland position
			}
		else
			{
			data[pos + 27] = (byte)model.get("control" + index + "sysexlength");
			data[pos + 28] = (byte)(model.get("control" + index + "sysexdvpos") + 1);
			}

		for(int i = 0; i < 12; i++)
			{
			data[pos + 29 + i] = (byte)(model.get("control" + index + "sysex" + i));
			}
		}		

	static final byte[] TO_COMMON_PORTS = new byte[] { 0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4a, 0x4b, 0x50, 0x51, 0x52, 0x53, 0x00  };
	static final byte[] TO_PROGRAM_PORTS = new byte[] { 0x00, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4a, 0x4b, 0x40, };
	static final byte[] TO_KEYBOARD_PORTS = new byte[] { 0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4a, 0x4b, 0x40, };



	void emitHeader(byte[] data)
 		{
 		int pos = 13;		// 0x0D
		
		// Name and manufacturer
		String name = (model.get("name", "") + "                                  ").substring(0, 34);
		char[] chars = name.toCharArray();
		for(int i = 0; i < 34; i++)
			{
			data[pos + i] = (byte)chars[i];
			}
		pos += 34;
		
		data[pos++] = 0x20;
		
		String manufacturer = (model.get("manufacturer", "") + "             ").substring(0, 13);
		chars = manufacturer.toCharArray();
		for(int i = 0; i < 13; i++)
			{
			data[pos + i] = (byte)chars[i];
			}
		pos += 13;
		pos += 4;
		
		// remaining globals
		int templatesize = model.get("templatesize");
		int templatepos = model.get("templateposition");
		if (templatesize == 1) { templatesize = 0; templatepos = 0; }
		if (templatepos > templatesize) pos = templatesize;
		data[pos++] = (byte)templatesize;
		data[pos++] = (byte)templatepos;
		
		// Unknown constants
		data[pos++] = 0x00;
		data[pos++] = 0x5a;
		data[pos++] = 0x29;
		data[pos++] = 0x00;
		data[pos++] = 0x00;
		data[pos++] = 0x00;
		data[pos++] = 0x00;
		data[pos++] = 0x00;
		data[pos++] = 0x00;
		data[pos++] = 0x00;
		data[pos++] = 0x00;
		data[pos++] = 0x00;
		data[pos++] = 0x00;
		data[pos++] = 0x00;
		data[pos++] = 0x00;
		data[pos++] = 0x19;
		data[pos++] = 0x00;
		data[pos++] = 0x01;
		data[pos++] = 0x00;
		data[pos++] = 0x21;
		data[pos++] = 0x00;
		data[pos++] = 0x09;
		data[pos++] = 0x00;
		data[pos++] = 0x39;
		data[pos++] = 0x00;
		data[pos++] = 0x11;
		data[pos++] = 0x00;
		data[pos++] = 0x29;
		data[pos++] = 0x00;
		data[pos++] = 0x31;
		data[pos++] = 0x03;
		data[pos++] = 0x05;
		
		// MIDI port groups
		data[pos++] = (byte)model.get("progchannel");
		data[pos++] = TO_PROGRAM_PORTS[model.get("progports")];
		data[pos++] = (byte)model.get("commonchannel");
		data[pos++] = TO_COMMON_PORTS[model.get("commonports")];

		// Unknown constants
		data[pos++] = 0x00;
		data[pos++] = 0x00;

		// Other Stuff
		data[pos++] = (byte)model.get("velocitycurve");
		data[pos++] = (byte)model.get("octavesetting");
		data[pos++] = (byte)(model.get("potpickup") | ((1 - model.get("aftertouch") ) << 2));	// NOTE Aftertouch is inverted

		// Unknown constants
		data[pos++] = 0x00;
		data[pos++] = 0x07;

		// Keyboard Zones
		data[pos++] = (byte)model.get("enablekeyboardzones");
		for(int i = 1; i <= 4; i++)	// note <=
			{
			data[pos++] = (byte)model.get("zone" + i + "channel");
			data[pos++] = TO_KEYBOARD_PORTS[model.get("zone" + i + "ports")];
			data[pos++] = (byte)model.get("zone" + i + "veloffset");
			data[pos++] = (byte)model.get("zone" + i + "minnote");
			data[pos++] = (byte)model.get("zone" + i + "maxnote");
			data[pos++] = (byte)model.get("zone" + i + "transpose");
			data[pos++] = (byte)(model.get("zone" + i + "pitchbend") |
								 (model.get("zone" + i + "modwheel") << 1) |
								 (model.get("zone" + i + "aftertouch") << 2));
			data[pos++] = 0x00;
			data[pos++] = 0x00;
			data[pos++] = 0x00;
			}
		
		
		// Touchpad Types
		data[pos++] = (byte)model.get("touchpadxtype");
		data[pos++] = (byte)model.get("touchpadytype");
		
		// Unknown Constants		
		data[pos++] = 0x00;
		data[pos++] = 0x00;
		data[pos++] = 0x00;
		
		// Drum Note Auto-Off
		final int DRUMPAD_BASE_INDEX = 56;
		for(int i = 0; i < 8; i++)
			{
			data[pos++] = (byte)model.get("control" + (DRUMPAD_BASE_INDEX + i) + "autooff");
			}

		// Drum Note Off Sync Value
		for(int i = 0; i < 8; i++)
			{
			data[pos++] = (byte)model.get("control" + (DRUMPAD_BASE_INDEX + i) + "drumoffsync");
			}

		// Unknown Constants
		data[pos++] = 0x00;
		data[pos++] = 0x01;
		data[pos++] = 0x00;
		data[pos++] = 0x40;
		data[pos++] = 0x00;
		data[pos++] = 0x00;
		data[pos++] = 0x00;
		data[pos++] = 0x00;
		data[pos++] = 0x40;

		// Following this are 231 zeros
		}


    public byte[] emit(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        if (tempModel == null)
            tempModel = getModel();
		
        byte[] data = new byte[4112];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x00;
        data[2] = (byte)0x20;
        data[3] = (byte)0x29;
        data[4] = (byte)0x02;
        data[5] = (byte)0x03;
        data[6] = (byte)0x7F;
        
        if (toWorkingMemory)			// UPLOAD
        	{
	        data[7] = (byte)0x00;
	        data[8] = (byte)0x00;
	        data[9] = (byte)0x11;
	        data[10] = (byte)0x02;
	        data[11] = (byte)0x00;
	        data[12] = (byte)0x01;
        	}
        else							// WRITE
        	{
	        data[7] = (byte)0x01;
	        data[8] = (byte)0x00;
	        data[9] = (byte)0x09;
	        data[10] = (byte)0x06;
	        data[11] = (byte)0x00;
	        data[12] = (byte)tempModel.get("number");
	        }
        data[4109] = (byte)0x12;
        data[4110] = (byte)0x34;
        data[4111] = (byte)0xF7;
        
		emitHeader(data);
		int index = 0;

		// Encoders
		for(int i = 0; i < 8; i++)
			{
			emitEncoder(index++, data);
			}
		// Pots
		for(int i = 0; i < 8; i++)
			{
			emitPot(index++, data);
			}
		// Faders
		for(int i = 0; i < 8; i++)
			{
			emitPot(index++, data);
			}
		// Buttons
		for(int i = 0; i < 32; i++)
			{
			emitButton(index++, data);
			}
		// Drumpads
		for(int i = 0; i < 8; i++)
			{
			emitDrumPad(index++, data);
			}
		// Expression
		emitPot(index++, data);
		// Sustain
		emitButton(index++, data);
		// Modwheel
		emitPot(index++, data);
		// Pitchbend, X1, Y1, X2, Y2
		for(int i = 0; i < 5; i++)
			{
			emitPitchBend(index++, data);
			}
		// Transport Buttons
		for(int i = 0; i < 6; i++)
			{
			emitButton(index++, data);
			}
		// Skip 7 controls
		index += 7;
		// Cross-Fader
		emitPot(index++, data);
		// Skip 4 controls
		index += 4;
		// All Done
		
		return data; 
		}
		



	int at(int pos, byte[] stuff, byte val)
		{
		for(int i = 0; i < stuff.length; i++)
			{
			if (stuff[i] == val) return i;
			}
        System.err.println("NovationSLMKII.at() WARNING: Invalid data " + val + " at byte position " + pos + " (" + (pos - 0x1a3)/41 + ", " + (pos - 0x1a3)%41 + ")");
        new Throwable().printStackTrace();
		return 0;
		}

		
	void parseName(int index, int pos, byte[] data)
		{
		char[] chars = new char[8];
		for(int i = 0; i < 8; i++)
			{
			chars[i] = (char)data[pos + i];
			}
		model.set("control" + index + "name", new String(chars));
		}
		

	void parseEncoder(int index, byte[] data)
		{
		int pos = index * 41 + 0x1a3;
		parseName(index, pos, data);
		model.set("control" + index + "type", data[pos + 8]);
		int display = at(pos + 15, TO_ENCODER_DISPLAY, data[pos + 15]);
		model.set("control" + index + "display", display);
		model.set("control" + index + "ports", at(pos + 18, TO_CONTROL_PORTS, data[pos + 18]));
		model.set("control" + index + "channel", at(pos + 19, TO_CONTROL_CHANNELS, data[pos + 19]));
		
		if (display == 5) // 16K
			{
			model.set("control" + index + "lowvalbig", (data[pos+9] << 7) | data[pos+10]);
			model.set("control" + index + "highvalbig", (data[pos+11] << 7) | data[pos+12]);
			model.set("control" + index + "defaultvalbig", (data[pos+20] << 7) | data[pos+21]);
			}
		else
			{
			model.set("control" + index + "lowval", data[pos+10]);
			model.set("control" + index + "highval", data[pos+12]);
			model.set("control" + index + "defaultval", data[pos+21]);
			}
		model.set("control" + index + "parammsb", data[pos + 16]);
		model.set("control" + index + "paramlsb", data[pos + 17]);


// 	dvtype > 0 ? dvtype : (dvpos == 0 ? 0 : 1)

		boolean isRoland = false;
		if (display == 5)
			{
			int dvtype = at(pos + 26, TO_DOUBLE_DV_TYPES, data[pos + 26]);		// The software always sets the type to 0 even with single DV
			int dvpos = data[pos + 28];
			model.set("control" + index + "sysexdvpos", dvpos - 1);
			model.set("control" + index + "sysexdoubledvtype", dvtype);  // handle both software and unit
			isRoland = (dvtype == 3);
			}
		else
			{
			int dvtype = at(pos + 26, TO_SINGLE_DV_TYPES, data[pos + 26]);		// The software always sets the type to 0 even with single DV
			int dvpos = data[pos + 28];
			model.set("control" + index + "sysexdvpos", dvpos - 1);
			model.set("control" + index + "sysexsingledvtype", dvtype);  // handle both software and unit
			isRoland = (dvtype == 2);
			}
			
		if (isRoland)
			{
			model.set("control" + index + "rolandsysexlength", data[pos + 27]);
			}
		else
			{
			model.set("control" + index + "sysexlength", data[pos + 27]);
			}

		for(int i = 0; i < 12; i++)
			{
			model.set("control" + index + "sysex" + i, data[pos + 29 + i]);
			}
		}

    
	void parsePot(int index, byte[] data)
		{
		int pos = index * 41 + 0x1a3;
		parseName(index, pos, data);

		model.set("control" + index + "type", data[pos + 8]);
		if (index != MODWHEEL_INDEX && index != EXPRESSION_INDEX)
			model.set("control" + index + "potpickup", at(pos + 14, TO_POT_PICKUPS, (byte)((data[pos + 14] & (32 + 64)))));		// bits 5 and 6 only
		int display = at(pos + 15, TO_ENCODER_DISPLAY, data[pos + 15]);
		model.set("control" + index + "display", display);
		model.set("control" + index + "ports", at(pos + 18, TO_CONTROL_PORTS, data[pos + 18]));
		model.set("control" + index + "channel", at(pos + 19, TO_CONTROL_CHANNELS, data[pos + 19]));
		
		model.set("control" + index + "lowval", data[pos+10]);
		model.set("control" + index + "highval", data[pos+12]);
		model.set("control" + index + "defaultval", data[pos+21]);
		model.set("control" + index + "parammsb", data[pos + 16]);
		model.set("control" + index + "paramlsb", data[pos + 17]);

		int dvtype = at(pos + 26, TO_SINGLE_DV_TYPES, data[pos + 26]);		// The software always sets the type to 0 even with single DV
		int dvpos = data[pos + 28];
		model.set("control" + index + "sysexdvpos", dvpos - 1);
		model.set("control" + index + "sysexsingledvtype", dvtype);  // handle both software and unit
		boolean isRoland = (dvtype == 2);

		if (isRoland)
			{
			model.set("control" + index + "rolandsysexlength", data[pos + 27]);
			}
		else
			{
			model.set("control" + index + "sysexlength", data[pos + 27]);
			}

		for(int i = 0; i < 12; i++)
			{
			model.set("control" + index + "sysex" + i, data[pos + 29 + i]);
			}
		}
		
	void parseButton(int index, byte[] data)
		{
		int pos = index * 41 + 0x1a3;
		parseName(index, pos, data);

		int type = at(pos + 8, TO_BUTTON_TYPES, data[pos + 8]);
		model.set("control" + index + "type", type);
		int display = at(pos + 15, TO_BUTTON_DISPLAYS, data[pos + 15]);
		model.set("control" + index + "display", display);
		model.set("control" + index + "ports", at(pos + 18, TO_CONTROL_PORTS, data[pos + 18]));

		if (type == 5) 		// MMC
			{
			model.set("control" + index + "mmcdevice", data[pos + 19]);
			}
		else
			{
			model.set("control" + index + "channel", at(pos + 19, TO_CONTROL_CHANNELS, data[pos + 19]));
			}
		
		if (type == 5)		// MMC
			{
			model.set("control" + index + "mmctype", data[pos+10] - 1);
			}
		else if (type == 6)	// Note on/off, same as Realtime
			{
			model.set("control" + index + "lowval", data[pos+10]);
			model.set("control" + index + "highval", data[pos+12]);
			}
		else if (type == 7) 			// Bank Select
			{
			model.set("control" + index + "bankmsb", data[pos+16]);
			model.set("control" + index + "banklsb", data[pos+17]);
			}
		else if (type == 8) 			// PC
			{
			model.set("control" + index + "lowval", data[pos+10]);
			model.set("control" + index + "highval", data[pos+12]);
			model.set("control" + index + "bankmsb", data[pos+16]);
			model.set("control" + index + "banklsb", data[pos+17]);
			}
		else if (type == 9)	// Template
			{
			model.set("control" + index + "template", data[pos+10]);
			}
		else if (type == 10)	// Realtime
			{
			model.set("control" + index + "realtime", at(pos+14, TO_BUTTON_REAL_TIME, data[pos+14]));
			}
		else					// 0, 1, 2, 3, 4
			{
			model.set("control" + index + "lowval", data[pos+10]);
			model.set("control" + index + "highval", data[pos+12]);
			model.set("control" + index + "defaultval", data[pos+21]);
			model.set("control" + index + "parammsb", data[pos + 16]);
			model.set("control" + index + "paramlsb", data[pos + 17]);
			}
			
		if (type == 7)		// Bank Select
			{
			model.set("control" + index + "pcbankmode", at(pos+13, TO_BUTTON_BANK_CHANGE_MODES, data[pos+13]));
			}
		else if (type == 8)		// PC
			{
			model.set("control" + index + "bankmode", at(pos+13, TO_BUTTON_PROGRAM_CHANGE_MODES, data[pos+13]));
			}
		else
			{
			model.set("control" + index + "buttontype", at(pos+13, TO_BUTTON_STANDARD_TYPES, data[pos+13]));
			model.set("control" + index + "stepsize", data[pos + 22]);
			}

		int dvtype = at(pos + 26, TO_SINGLE_DV_TYPES, data[pos + 26]);		// The software always sets the type to 0 even with single DV
		int dvpos = data[pos + 28];
		model.set("control" + index + "sysexdvpos", dvpos - 1);
		model.set("control" + index + "sysexsingledvtype", dvtype);  // handle both software and unit
		boolean isRoland = (dvtype == 2);

		if (isRoland)
			{
			model.set("control" + index + "rolandsysexlength", data[pos + 27]);
			}
		else
			{
			model.set("control" + index + "sysexlength", data[pos + 27]);
			}

		for(int i = 0; i < 12; i++)
			{
			model.set("control" + index + "sysex" + i, data[pos + 29 + i]);
			}
		}		
						
	
	void parseDrumPad(int index, byte[] data)
		{
		int pos = index * 41 + 0x1a3;
		parseName(index, pos, data);

		int type = at(pos + 8, TO_DRUM_PAD_TYPES, data[pos + 8]);
		model.set("control" + index + "type", type);
		int display = at(pos + 15, TO_BUTTON_DISPLAYS, data[pos + 15]);
		model.set("control" + index + "display", display);
		model.set("control" + index + "ports", at(pos + 18, TO_CONTROL_PORTS, data[pos + 18]));

		if (type == 5) 		// MMC
			{
			model.set("control" + index + "mmcdevice", data[pos + 19]);
			}
		else
			{
			model.set("control" + index + "channel", at(pos + 19, TO_CONTROL_CHANNELS, data[pos + 19]));
			}
		
		if (type == 5)		// MMC
			{
			model.set("control" + index + "mmctype", data[pos+10] - 1);
			}
		else if (type == 6)	// Note on/off, same as Realtime
			{
			model.set("control" + index + "lowval", data[pos+10]);
			model.set("control" + index + "highval", data[pos+12]);
			}
		else if (type == 7) 			// Bank Select
			{
			model.set("control" + index + "bankmsb", data[pos+16]);
			model.set("control" + index + "banklsb", data[pos+17]);
			}
		else if (type == 8) 			// PC
			{
			model.set("control" + index + "lowval", data[pos+10]);
			model.set("control" + index + "highval", data[pos+12]);
			model.set("control" + index + "bankmsb", data[pos+16]);
			model.set("control" + index + "banklsb", data[pos+17]);
			}
		else if (type == 9)	// Template
			{
			model.set("control" + index + "template", data[pos+10]);
			}
		else if (type == 10)	// Realtime
			{
			model.set("control" + index + "realtime", at(pos+14, TO_BUTTON_REAL_TIME, data[pos+14]));
			}
		else					// 0, 1, 2, 3, 4
			{
			model.set("control" + index + "lowval", data[pos+10]);
			model.set("control" + index + "highval", data[pos+12]);
			model.set("control" + index + "defaultval", data[pos+21]);
			model.set("control" + index + "parammsb", data[pos + 16]);
			model.set("control" + index + "paramlsb", data[pos + 17]);
			}
			
		if (type == 7)		// Bank Select
			{
			model.set("control" + index + "pcbankmode", at(pos+13, TO_BUTTON_BANK_CHANGE_MODES, data[pos+13]));
			}
		else if (type == 8)		// PC
			{
			model.set("control" + index + "bankmode", at(pos+13, TO_BUTTON_PROGRAM_CHANGE_MODES, data[pos+13]));
			}
		else
			{
			model.set("control" + index + "buttontype", at(pos+13, TO_BUTTON_STANDARD_TYPES, data[pos+13]));
			model.set("control" + index + "stepsize", data[pos + 22]);
			}

		int dvtype = at(pos + 26, TO_SINGLE_DV_TYPES, data[pos + 26]);		// The software always sets the type to 0 even with single DV
		int dvpos = data[pos + 28];
		model.set("control" + index + "sysexdvpos", dvpos - 1);
		model.set("control" + index + "sysexsingledvtype", dvtype);  // handle both software and unit
		boolean isRoland = (dvtype == 2);

		if (isRoland)
			{
			model.set("control" + index + "rolandsysexlength", data[pos + 27]);
			}
		else
			{
			model.set("control" + index + "sysexlength", data[pos + 27]);
			}

		for(int i = 0; i < 12; i++)
			{
			model.set("control" + index + "sysex" + i, data[pos + 29 + i]);
			}
		}		
	
	
	
	void parsePitchBend(int index, byte[] data)
		{
		int pos = index * 41 + 0x1a3;
		parseName(index, pos, data);
		
		model.set("control" + index + "type", at(pos + 8, TO_PITCH_BEND_TYPES, data[pos + 8]));
		int display = at(pos + 15, TO_PITCH_BEND_DISPLAY, data[pos + 15]);
		model.set("control" + index + "display", display);
		model.set("control" + index + "ports", at(pos + 18, TO_CONTROL_PORTS, data[pos + 18]));
		model.set("control" + index + "channel", at(pos + 19, TO_CONTROL_CHANNELS, data[pos + 19]));
		
		model.set("control" + index + "lowval", data[pos+10]);
		model.set("control" + index + "highval", data[pos+12]);
		model.set("control" + index + "defaultval", data[pos+21]);
		model.set("control" + index + "parammsb", data[pos + 16]);
		model.set("control" + index + "paramlsb", data[pos + 17]);
		
		int dvtype = at(pos + 26, TO_SINGLE_DV_TYPES, data[pos + 26]);		// The software always sets the type to 0 even with single DV
		int dvpos = data[pos + 28];
		model.set("control" + index + "sysexdvpos", dvpos - 1);
		model.set("control" + index + "sysexsingledvtype", dvtype);  // handle both software and unit
		boolean isRoland = (dvtype == 2);

		if (isRoland)
			{
			model.set("control" + index + "rolandsysexlength", data[pos + 27]);
			}
		else
			{
			model.set("control" + index + "sysexlength", data[pos + 27]);
			}

		for(int i = 0; i < 12; i++)
			{
			model.set("control" + index + "sysex" + i, data[pos + 29 + i]);
			}
		}		
				
	void parseHeader(byte[] data)
 		{
 		int pos = 13;		// 0x0D

		char[] chars = new char[34];
		for(int i = 0; i < 34; i++)
			{
			chars[i] = (char)data[pos + i];
			}
		model.set("name", new String(chars));
		pos += 34;
		
		pos++;

		chars = new char[13];
		for(int i = 0; i < 13; i++)
			{
			chars[i] = (char)data[pos + i];
			}
		model.set("manufacturer", new String(chars));
		pos += 13;
		
		pos += 4;
		
		// remaining globals
		int templatesize = data[pos++];
		int templatepos = data[pos++];
		model.set("templatesize", templatesize == 0 ? 1 : templatesize);
		model.set("templateposition", templatepos);
		
		pos += 32;
		
		// MIDI port groups
		model.set("progchannel", data[pos++]);
		model.set("progports", at(pos, TO_PROGRAM_PORTS, data[pos++]));
		model.set("commonchannel", data[pos++]);
		model.set("commonports", at(pos, TO_COMMON_PORTS, data[pos++]));

		pos += 2;
		
		// Other Stuff
		model.set("velocitycurve", data[pos++]);
		model.set("octavesetting", data[pos++]);
		int val = data[pos++];
		model.set("potpickup", val & 0x01);
		model.set("aftertouch", 1 - ((val >>> 2) & 0x01));

		pos += 2;

		// Keyboard Zones
		model.set("enablekeyboardzones", data[pos++]);
		for(int i = 1; i <= 4; i++)	// note <=
			{
			model.set("zone" + i + "channel", data[pos++]);
			model.set("zone" + i + "ports", at(pos, TO_KEYBOARD_PORTS, data[pos++]));
			model.set("zone" + i + "veloffset", data[pos++]);
			model.set("zone" + i + "minnote", data[pos++]);
			model.set("zone" + i + "maxnote", data[pos++]);
			model.set("zone" + i + "transpose", data[pos++]);
			val = data[pos++];
			model.set("zone" + i + "pitchbend", val & 0x01);
			model.set("zone" + i + "modwheel", (val >>> 1) & 0x01);
			model.set("zone" + i + "aftertouch", (val >>> 2) & 0x01);
	
			pos += 3;		
			}
		
		// Touchpad Types
		model.set("touchpadxtype", data[pos++]);
		model.set("touchpadytype", data[pos++]);

		pos += 3;		
		
		// Drum Note Auto-Off
		final int DRUMPAD_BASE_INDEX = 56;
		for(int i = 0; i < 8; i++)
			{
			model.set("control" + (DRUMPAD_BASE_INDEX + i) + "autooff", data[pos++]);
			}

		// Drum Note Off Sync Value
		for(int i = 0; i < 8; i++)
			{
			model.set("control" + (DRUMPAD_BASE_INDEX + i) + "drumoffsync", data[pos++]);
			}

		pos += 9;

		// Following this are 231 zeros
		}




    public int parse(byte[] data, boolean fromFile)
        {
        // "Upload", no patch number
        if (data[7] == 0x00 && 
        	data[8] == 0x00 && 
        	data[9] == 0x11 && 
        	data[10] == 0x02 && 
        	data[11] == 0x00 && 
        	data[12] == 0x12) 
            {
            model.set("number", 0);
            }
        else
        	{
        	model.set("number", data[12]);
        	}
		parseHeader(data);

		int index = 0;

		// Encoders
		for(int i = 0; i < 8; i++)
			{
			parseEncoder(index++, data);
			}
		// Pots
		for(int i = 0; i < 8; i++)
			{
			parsePot(index++, data);
			}
		// Faders
		for(int i = 0; i < 8; i++)
			{
			parsePot(index++, data);
			}
		// Buttons
		for(int i = 0; i < 32; i++)
			{
			parseButton(index++, data);
			}
		// Drumpads
		for(int i = 0; i < 8; i++)
			{
			parseDrumPad(index++, data);
			}
		// Expression
		parsePot(index++, data);
		// Sustain
		parseButton(index++, data);
		// Modwheel
		parsePot(index++, data);
		// Pitchbend, X1, Y1, X2, Y2
		for(int i = 0; i < 5; i++)
			{
			parsePitchBend(index++, data);
			}
		// Transport Buttons
		for(int i = 0; i < 6; i++)
			{
			parseButton(index++, data);
			}
		// Skip 7 controls
		index += 7;
		// Cross-Fader
		parsePot(index++, data);
		// Skip 4 controls
		index += 4;
		// All Done
		
		//revise();
		
		return PARSE_SUCCEEDED; 
        }




	}





class SysexBox extends HBox
	{
	VBox typeBox = new VBox();
	HBox lengthBox = new HBox();
	Model model;
	Chooser sysexdoubledvtype;
	Chooser sysexsingledvtype;
 	LabelledDial sysexlength;
 	LabelledDial rolandsysexlength;
	int index;
	
	public SysexBox(Synth synth, int index, Color color)
		{
		model = synth.getModel();
		this.index = index;
		
		/// Build Sysex Byte Dials

        final HBox sysexBytes = new HBox();
        final LabelledDial[] sysex = new LabelledDial[12];
        for(int i = 0; i < sysex.length; i++)
        	{
        	final int _i = i;
        	sysex[i] = new LabelledDial("Byte " + (i + 1), synth, "control" + index + "sysex" + i, color, 0, 127)
        		{
				public String map(int val)
					{
					if (model.get("control" + index + "display") == 5)  // 16 K
						{
						int dv = model.get("control" + index + "sysexdoubledvtype");
						 if (dv != 0 && dv != 3)	// DV exists and is not roland
							{
							int pos = model.get("control" + index + "sysexdvpos");
							if (pos == _i || pos == _i - 1)	// I'm in the DV spot
								{
								return "DV";
								}
							}
						}
					else
						{
						int dv = model.get("control" + index + "sysexsingledvtype");
						 if (dv != 0 && dv != 2)	// DV exists and is not roland
							{
							if (model.get("control" + index + "sysexdvpos") == _i)	// I'm in the DV spot
								{
								return "DV";
								}
							}
						}
					
					// at this point I'm definitely not the DV
					return StringUtility.toHex((byte)val);
					}
        		};
        	sysexBytes.add(sysex[i]);
        	}
	

		// There are TWO sysex length dials, which alternate being shown depending on 16K
		
        sysexlength = new LabelledDial("Sysex Length", synth, "control" + index + "sysexlength", color, 0, 12)
        	{
        	public void update(String key, final Model model)
        		{
        		super.update(key, model);
        		sysexBytes.removeAll();
        		int val = model.get(key);
        		for(int i = 0; i < val; i++)
        			{
        			sysexBytes.add(sysex[i]);
        			}
        		sysexBytes.revalidate();
        		sysexBytes.repaint();
        		} 
        	};
        	
        rolandsysexlength = new LabelledDial("Sysex Length", synth, "control" + index + "rolandsysexlength", color, 9, 12)
        	{
        	public void update(String key, final Model model)
        		{
        		super.update(key, model);
        		sysexBytes.removeAll();
        		int val = model.get(key);
        		for(int i = 0; i < val; i++)
        			{
        			sysexBytes.add(sysex[i]);
        			}
        		sysexBytes.revalidate();
        		sysexBytes.repaint();
        		} 
        	};
        
        final HBox sysexdvposbox = new HBox();
        final LabelledDial sysexdvpos = new LabelledDial("Data Position", synth, "control" + index + "sysexdvpos", color, 0, 11, -1)
        	{
              public void update(String key, final Model model)
        		{
        		super.update(key, model);
        		sysexBytes.repaint();
        		}
		  	};
                
		// There are TWO sysex data type dials, which alternate being shown depending on 16K.  They have
		// an impact on the sysex length dials so we need to re-update them too
		
        sysexsingledvtype = new Chooser("Data Type", synth, "control" + index + "sysexsingledvtype", NovationSLMKII.SINGLE_DV_TYPES)	
        	{
        	public void update(String key, final Model model)
        		{
        		super.update(key, model);
        		lengthBox.remove(sysexlength);
        		lengthBox.remove(rolandsysexlength);
        		if (model.get(key) == 2)	// Roland [single]
        			{
        			lengthBox.add(rolandsysexlength);
					rolandsysexlength.update("control" + index + "rolandsysexlength", model);		// so it changes the sysex byte length
        			}
        		else
        			{
        			lengthBox.add(sysexlength);
					sysexlength.update("control" + index + "sysexlength", model);		// so it changes the sysex byte length
        			}
        			
        		sysexdvposbox.remove(sysexdvpos);
        		if (model.get(key) == 1)	// Single
        			{
        			sysexdvposbox.add(sysexdvpos);
        			}
        			
        		sysexdvposbox.revalidate();
        		sysexdvposbox.repaint();
        		lengthBox.revalidate();
        		lengthBox.repaint();
        		sysexBytes.repaint();
        		} 
        	};
        	
        sysexdoubledvtype = new Chooser("Data Type", synth, "control" + index + "sysexdoubledvtype", NovationSLMKII.DOUBLE_DV_TYPES)
        	{
        	public void update(String key, Model model)
        		{
        		super.update(key, model);
        		lengthBox.remove(sysexlength);
        		lengthBox.remove(rolandsysexlength);
        		if (model.get(key) == 3)	// Roland [double]
        			{
        			lengthBox.add(rolandsysexlength);
					rolandsysexlength.update("control" + index + "rolandsysexlength", model);		// so it changes the sysex byte length
        			}
        		else
        			{
        			lengthBox.add(sysexlength);
					sysexlength.update("control" + index + "sysexlength", model);		// so it changes the sysex byte length
        			}
        			
        		sysexdvposbox.remove(sysexdvpos);
        		if (model.get(key) == 1 || model.get(key) == 2)	// LSB-MSB or MSB-LSB
        			{
        			sysexdvposbox.add(sysexdvpos);
        			}
        			
        		sysexdvposbox.revalidate();
        		sysexdvposbox.repaint();
        		lengthBox.revalidate();
        		lengthBox.repaint();
        		sysexBytes.repaint();
        		} 
        	};
    
    	// Assemble
    	setShows16K(false);		// default
    	add(typeBox);
    	add(lengthBox);
    	add(sysexdvposbox);
    	add(sysexBytes);
    	}


	public void setShows16K(boolean val)
		{
		if (val)
			{
			typeBox.removeAll();
			typeBox.add(sysexdoubledvtype);
			typeBox.revalidate();
			typeBox.repaint();
			sysexdoubledvtype.update("control" + index + "sysexdoubledvtype", model);
			}
		else
			{
			typeBox.removeAll();
			typeBox.add(sysexsingledvtype);
			typeBox.revalidate();
			typeBox.repaint();
			sysexsingledvtype.update("control" + index + "sysexsingledvtype", model);
			}
		}
		
	}





    
/**** 
NOVATION SL MK II TEMPLATE SYSEX FORMAT

The SL has many sysex messages of dubious value, but it only has limited messages for communicating
to Novation's software editor.  Specifically, there is a single family of patch dump sysex messages.


-------------------------
SINGLE PATCH DUMP COMMAND
-------------------------

SUMMARY: I have seen the following messages, which I will call "WRITE", "UPLOAD", and "RECEIVED",
though these are somewhat misnomers.

WRITE:		F0 00 20 29 02 03 7F 01 00 09 06 00 PATCHNUM <DATA...> 12 34 F7
[or]		F0 00 20 29 02 03 7F 00 00 09 06 00 PATCHNUM <DATA...> 12 34 F7
	This message is sent to the SL from the software editor when a template has already
	been assigned a number.  The SL also dumps in this form when dumping all the patches
	in memory.  This command appears to write to permanent flash memory rather than
	send to temporary memory.
	
UPLOAD:		F0 00 20 29 02 03 7F 00 00 11 02 00 01 <DATA...> 12 34 F7
	This message is sent to the SL from the software editor for brand new patches'
	created from scratch.  It appears to send to temporary memory only.

RECEIVED:	F0 00 20 29 02 03 7F 00 00 0B 0E 00 PATCHNUM <DATA...> 12 34 F7
	This message is received by the software editor from the SL.  It's also sent to the SL.
	Though it has a patch numer, it appears that this command sends to temporary memory only,
	and the SL does not retain the patch number.  I do not know why.

PATCHNUM is located at position 0C and has a value 0x00 ... 0x1F.  When doing bulk uploads, 
it appears that there is a secret patch number 0x20 (32) which provides default names for things 
for automaps [I believe], but it looks like you shouldn't fool around with that.  Indeed you
can occasionally get the SL to indicate "Template-33" when it meant "Template-1": there are
still some bugs in the device.

<DATA...> is 4096 bytes, running from positions 0D to 100C inclusive.  

The last three bytes appear to always be 12, 34, and F7, located at positions 100D, 100E, and 100F.
There does not appear to be a checksum.




DATA
====
The 4096 bytes of DATA is as follows (starting at byte 0D):

BYTE	DESCRIPTION			
0D	Name				34 Bytes, padded with 0x20
2F	(0x20)
30	Manufacturer		13 Bytes, padded with 0x20
	*** NOTE: Name + Maufacturer essentially comprise one string 48 bytes long
3D 	(0x00)
3E	(0x00)
3F	(0x00)
40	(0x00)
41	Template Size		0: 1, 2-40: 2-40
	*** NOTE: The software permits values up to 40, but the Zero (and probably
	*** other units?) only permits values up to 32 because there are only 32
	*** templates.  This should be restricted to 32.
	*** NOTE: Size is never set to 1.  0 represents size 1.
42	Template Position	0, or 1-40
	*** Size must be a display value 1...40
	*** Position must be a display value from 1 ... Size
	*** If size is display 1, then the sysex values for size and position are both 0
	*** Otherwise the sysex values for size and position are their display values
	*** Yes, that's weird

UNKNOWN		*** I do not know the meaning of some of these constants
43	(0x00)
44	(0x5a)
45	(0x29)
46	(0x00)
47	(0x00)
48	(0x00)
49	(0x00)
4A	(0x00)
4B	(0x00)
4C	(0x00)
4D	(0x00)
4E	(0x00)
4F	(0x00)
50	(0x00)
51	(0x00)
52	(0x19)
53	(0x00)
54	(0x01)
55	(0x00)
56	(0x21)
57	(0x00)
58	(0x09)
59	(0x00)
5A	(0x39)
5B	(0x00)
5C	(0x11)
5D	(0x00)
5E	(0x29)
5F	(0x00)
60	(0x31)
61	(0x03)
62	(0x05)

MIDI PORT GROUPS KEYBOARD
63	Channel				0-16				[1-16, "As Common"]
64	Ports				[See Table 4]
	*** Note: I have seen this corrupted on the unit, where it returns 0x05 for "Common"

MIDI PORT GROUPS COMMON
65	Channel				0-15				[1-16]
66	Ports				[See Table 3]

UNKNOWN 		*** I do not know the meaning of these constants
67	(0x00)
68	(0x00)

69	Velocity Curve		0-126	[representing 1-127]
6A	Octave Setting		0-9		[representing -4 ... 5]
6B	Pot Pickup ON		0x01
+->	Aftertouch OFF		0x04

UNKNOWN 		*** I do not know the meaning of these constants
6C	(0x00)
6D	(0x07)

KEYBOARD ZONES
6E	Enable Keyboard Zones	0-1

6F	Zone 1 Channel		0-15	[1-16]
70	Zone 1 Ports		[See Table 8]
71	Zone 1 Vel Offset	0-126		[representing 1-127]
72	Zone 1 Min			0-127
73	Zone 1 Max			0-127
74	Zone 1 Transpose	0-127		[representing -64-63]
75	Zone 1 Aftertouch	0x04		[Note 0x04 is ON, which is not the same as 6B above]
+->	Zone 1 Pitch Bend	0x01
+->	Zone 1 Mod Wheel	0x02
76  (0x00)
77  (0x00)
78  (0x00)
79	Zone 2 Channel		0-15	[1-16]
7A	Zone 2 Ports		[See Table 8]
7B	Zone 2 Vel Offset	0-126		[representing 1-127]
7C	Zone 2 Min			0-127
7D	Zone 2 Max			0-127
7E	Zone 2 Transpose	0-127		[representing -64-63]
7F	Zone 2 Aftertouch	0x04		[Note 0x04 is ON, which is not the same as 6B above]
+->	Zone 2 Pitch Bend	0x01
+->	Zone 2 Mod Wheel	0x02
80  (0x00)
81  (0x00)
82  (0x00)
83	Zone 3 Channel		0-15	[1-16]
84	Zone 3 Ports		[See Table 8]
85	Zone 3 Vel Offset	0-126		[representing 1-127]
86	Zone 3 Min			0-127
87	Zone 3 Max			0-127
88	Zone 3 Transpose	0-127		[representing -64-63]
89	Zone 3 Aftertouch	0x04		[Note 0x04 is ON, which is not the same as 6B above]
+->	Zone 3 Pitch Bend	0x01
+->	Zone 3 Mod Wheel	0x02
8A  (0x00)
8B  (0x00)
8C  (0x00)
8D	Zone 4 Channel		0-15	[1-16]
8E	Zone 4 Ports		[See Table 8]
8F	Zone 4 Vel Offset	0-126		[representing 1-127]
90	Zone 4 Min			0-127
91	Zone 4 Max			0-127
92	Zone 4 Transpose	0-127		[representing -64-63]
93	Zone 4 Aftertouch	0x04		[Note 0x04 is ON, which is not the same as 6B above]
+->	Zone 4 Pitch Bend	0x01
+->	Zone 4 Mod Wheel	0x02
94	(0x00)
95	(0x00)
96	(0x00)


TOUCHPAD TYPES
97	Touchpad X Type		0-3		{ "No Spring/Hold" = 00, "Spring Right" = 03, "Spring Centre" = 02, "Spring Left" = 01 }
98	Touchpad Y Type		0-3		{ "No Spring/Hold" = 00, "Spring Up" = 03, "Spring Centre" = 02, "Spring Down" = 01 }

UNKNOWN 		*** I do not know the meaning of these constants
99	(0x00)
9A	(0x00)
9B	(0x00)

DRUM NOTES FOR THE EIGHT DRUM PADS -- I guess these couldn't fit in the standard slots
9C	Drum Note 0 Auto-Off	0-127
9E	Drum Note 1 Auto-Off	0-127
A0	Drum Note 2 Auto-Off	0-127
A2	Drum Note 3 Auto-Off	0-127
A4	Drum Note 4 Auto-Off	0-127
A6	Drum Note 5 Auto-Off	0-127
A8	Drum Note 6 Auto-Off	0-127
AA	Drum Note 7 Auto-Off	0-127
AB	Drum Note 0 Off-Sync-Value	[See Table 1]
AC	Drum Note 1 Off-Sync-Value	[See Table 1]
AD	Drum Note 2 Off-Sync-Value	[See Table 1]
AE	Drum Note 3 Off-Sync-Value	[See Table 1]
AF	Drum Note 4 Off-Sync-Value	[See Table 1]
B0	Drum Note 5 Off-Sync-Value	[See Table 1]
B1	Drum Note 6 Off-Sync-Value	[See Table 1]
B2	Drum Note 7 Off-Sync-Value	[See Table 1]

UNKNOWN 		*** I do not know the meaning of these constants 
B3 (0x00)
B4 (0x01)
B5 (0x00)
B6 (0x40)
B7 (0x00)
B8 (0x00)
B9 (0x00)
BA (0x00)
BB (0x40)

BC-1a2	(0x00)	[231 bytes]



*** NOTE: At this point, the remaining data are segments, each 41 bytes long, 
*** representing the parameters for the various encoders, buttons, pots, faders, 
*** and other controls that can be programmed on the SL.

ENCODERS
1a3	Encoder 0	[41 bytes]	[See ENCODER DATA]
1cc	Encoder 1	[41 bytes]	[See ENCODER DATA]
1f5	Encoder 2	[41 bytes]	[See ENCODER DATA]
21e	Encoder 3	[41 bytes]	[See ENCODER DATA]
247	Encoder 4	[41 bytes]	[See ENCODER DATA]
270	Encoder 5	[41 bytes]	[See ENCODER DATA]
299	Encoder 6	[41 bytes]	[See ENCODER DATA]
2c2	Encoder 7	[41 bytes]	[See ENCODER DATA]

POTS
2eb	Pot 0	[41 bytes]	[See POT DATA]
314	Pot 1	[41 bytes]	[See POT DATA]
33d	Pot 2	[41 bytes]	[See POT DATA]
366	Pot 3	[41 bytes]	[See POT DATA]
38f	Pot 4	[41 bytes]	[See POT DATA]
3b8	Pot 5	[41 bytes]	[See POT DATA]
3e1	Pot 6	[41 bytes]	[See POT DATA]
40a	Pot 7	[41 bytes]	[See POT DATA]

FADERS
433	Fader 0	[41 bytes]	[See POT DATA]
45c	Fader 1	[41 bytes]	[See POT DATA]
485	Fader 2	[41 bytes]	[See POT DATA]
4ae	Fader 3	[41 bytes]	[See POT DATA]
4d7	Fader 4	[41 bytes]	[See POT DATA]
500	Fader 5	[41 bytes]	[See POT DATA]
529	Fader 6	[41 bytes]	[See POT DATA]
552	Fader 7	[41 bytes]	[See POT DATA]

BUTTONS A (Top Left Row)
57b	Button A0	[41 bytes]	[See BUTTON DATA]
5a4	Button A1	[41 bytes]	[See BUTTON DATA]
5cd	Button A2	[41 bytes]	[See BUTTON DATA]
5f6	Button A3	[41 bytes]	[See BUTTON DATA]
61f	Button A4	[41 bytes]	[See BUTTON DATA]
648	Button A5	[41 bytes]	[See BUTTON DATA]
671	Button A6	[41 bytes]	[See BUTTON DATA]
69a	Button A7	[41 bytes]	[See BUTTON DATA]

BUTTONS B (Bottom Left Row)
6c3	Button B0	[41 bytes]	[See BUTTON DATA]
6ec	Button B1	[41 bytes]	[See BUTTON DATA]
715	Button B2	[41 bytes]	[See BUTTON DATA]
73e	Button B3	[41 bytes]	[See BUTTON DATA]
767	Button B4	[41 bytes]	[See BUTTON DATA]
790	Button B5	[41 bytes]	[See BUTTON DATA]
7b9	Button B6	[41 bytes]	[See BUTTON DATA]
7e2	Button B7	[41 bytes]	[See BUTTON DATA]

BUTTONS C (Top Right Row)
80b	Button C0	[41 bytes]	[See BUTTON DATA]
834	Button C1	[41 bytes]	[See BUTTON DATA]
85d	Button C2	[41 bytes]	[See BUTTON DATA]
886	Button C3	[41 bytes]	[See BUTTON DATA]
8af	Button C4	[41 bytes]	[See BUTTON DATA]
8d8	Button C5	[41 bytes]	[See BUTTON DATA]
901	Button C6	[41 bytes]	[See BUTTON DATA]
92a	Button C7	[41 bytes]	[See BUTTON DATA]

BUTTONS D (Bottom Right Row)
953	Button D0	[41 bytes]	[See BUTTON DATA]
97c	Button D1	[41 bytes]	[See BUTTON DATA]
9a5	Button D2	[41 bytes]	[See BUTTON DATA]
9ce	Button D3	[41 bytes]	[See BUTTON DATA]
9f7	Button D4	[41 bytes]	[See BUTTON DATA]
a20	Button D5	[41 bytes]	[See BUTTON DATA]
a49	Button D6	[41 bytes]	[See BUTTON DATA]
a72	Button D7	[41 bytes]	[See BUTTON DATA]

DRUMPADS
a9b	Drumpad 0	[41 bytes]	[See DRUMPAD DATA]
ac4	Drumpad 1	[41 bytes]	[See DRUMPAD DATA]
aed	Drumpad 2	[41 bytes]	[See DRUMPAD DATA]
b16	Drumpad 3	[41 bytes]	[See DRUMPAD DATA]
b3f	Drumpad 4	[41 bytes]	[See DRUMPAD DATA]
b68	Drumpad 5	[41 bytes]	[See DRUMPAD DATA]
b91	Drumpad 6	[41 bytes]	[See DRUMPAD DATA]
bba	Drumpad 7	[41 bytes]	[See DRUMPAD DATA]

VARIOUS
be3	EXPRESSION	[41 bytes]	[See POT DATA]
c0c	SUSTAIN		[41 bytes]	[See BUTTON DATA]
c35	MODWHEEL	[41 bytes]	[See POT DATA]
c5e	PITCHBEND	[41 bytes]	[See PITCHBEND DATA]
c87	X1			[41 bytes]	[See PITCHBEND DATA]
cb0	Y1			[41 bytes]	[See PITCHBEND DATA]
cd9	X2			[41 bytes]	[See PITCHBEND DATA]
d02	Y2			[41 bytes]	[See PITCHBEND DATA]

TRANSPORT
d2b	REWIND		[41 bytes]	[See BUTTON DATA]	
d54	FF			[41 bytes]	[See BUTTON DATA]
d7d	STOP		[41 bytes]	[See BUTTON DATA]
da6	PLAY		[41 bytes]	[See BUTTON DATA]
dcf	RECORD		[41 bytes]	[See BUTTON DATA]
df8	LOOP		[41 bytes]	[See BUTTON DATA]
	*** Note that Record and Loop are out of order
e21	"BUTTON25"	[41 bytes]	[See UNUSED DATA]
e4a	"BUTTON26"	[41 bytes]	[See UNUSED DATA]

EXTRA
e73	"BUTTON27"	[41 bytes]	[See UNUSED DATA]
e9c	"BUTTON28"	[41 bytes]	[See UNUSED DATA]
ec5	"BUTTON29"	[41 bytes]	[See UNUSED DATA]
eee	"BUTTON30"	[41 bytes]	[See UNUSED DATA]
f17	UNUSED		[41 bytes]	[See UNUSED DATA]
f40	CROSS-FADER	[41 bytes]	[See POT DATA]
f69	UNUSED		[41 bytes]	[See UNUSED DATA]
f92	UNUSED		[41 bytes]	[See UNUSED DATA]
fbb	UNUSED		[41 bytes]	[See UNUSED DATA]
fe4	UNUSED		[41 bytes]	[See UNUSED DATA]

---- END OF DATA ---




ENCODER DATA	[ 41 bytes ]

OFFSETS
DEC HEX
00  00	Name				[8 bytes, padded with 0x20]
08  08	Type				0=off, 1=CC, 2=NRPN, 3=RPN 4=Sysex
09  09	Low Value MSB
10  0a	Low	Value LSB 
11  0b	High Value MSB
12  0c	High Value LSB
13  0d	(0x00)				[Normally Button Type]
14  0e	(0x50)				[Normally Real Time or Pot Pickup]
	*** NOTE: When PORTS is set to "as below" on the Editor, this is set to 0x01 for some reason
15  0f	Display Format 		0x00=0...127, 0x01=-64...+63, 0x06=REL1, 0x07=REL2, 0x09=0-16K, 0x11=APOT
	*** NOTE: if you select 0-16K, this will turn on 16-bit MSB and LSB for stuff
16  10	RPN or NRPN MSB
17  11	CC, RPN, or NRPN LSB
18  12	Ports				[See Table 5]
	*** Note: I have seen this corrupted on the unit, where it returns 0x05 for "Common"
19  13	Channel				00: Common 	20: Keyboard 	40-4F:	[1-16]
20  14	Default Value MSB
21  15	Default Value LSB
22  16	(0x00)				[Normally Step Size]
23  17	(0x00)				
24  18	(0x00)				
25  19	(0x00)
26  1a	Sysex DV Type		0: no DV	1: Single	2: LSB-MSB 	3: MSB-LSB 4: Roland  [See Table 9]
	*** Software always outputs 0!
27  1b	Sysex Length		0...12	(9...12 for Roland Sysex)
28  1c	DV position			0 = no DV, 1 = position 0, 2 = position 1, etc.
	*** NOTE: If the Sysex DV Type is 0, then the DV position is undefined and can be anything
	*** NOTE: There are serious errors in Novation's editor, which et the Sysex DV Type and
	*** the DV position to unexpected values.
	*** NOTE: For Roland Sysex, the data position is fixed to Sysex Length - 2, so if sysex is 9, then this value is 7, representing position 8
29  1d	Sysex String		[12 bytes, padded with 0x00]
	*** NOTE: it appears that for the DV position, 0x7F normally serves as a placeholder




POT DATA	[ 41 bytes ]

OFFSETS
DEC HEX
00  00	Name				[8 bytes, padded with 0x20]
08  08	Type				0=off, 1=CC, 2=NRPN, 3=RPN 4=Sysex
09  09	(0x00)				[Normally Low Value MSB]
10  0a	Low	Value
11  0b	(0x00)				[Normally High Value MSB]
12  0c	High Value
13  0d	(0x00)				[Normally Button Type]
14  0e	Pot Pickup			[See Table 7]
	*** NOTE: The Expression pedal and Modwheel do not have pot pickups.  Set them to (0x50)
15  0f	Display Format 		0x00=0...127, 0x01=-64...+63
	*** NOTE: Novation's editor provides 0x06=REL1, 0x07=REL2, 0x11=APOT as well, but the unit
	*** does not.  I can't imagine how a pot would be able to provide these options.
	*** NOTE: Novation's editor does not give the Mod Wheel the options 0...127 or -64...+63.
	*** This may be an error, but I only have a Zero so I cannot confrm.
16  10	RPN or NRPN MSB
17  11	CC, RPN, or NRPN LSB
18  12	Ports				[See Table 5]
	*** Note: I have seen this corrupted on the unit, where it returns 0x05 for "Common"
19  13	Channel				00: Common 	20: Keyboard 	40-4F:	[1-16]
20  14	(0x00)				[Normally Default Value MSB]
21  15	Default Value
22  16	(0x00)				[Normally Step Size]
23  17	(0x00)				
24  18	(0x00)				
25  19	(0x00)
26  1a	Sysex DV Type		0: no DV	1: Single 4: Roland  [See Table 9]
27  1b	Sysex Length		0...12	(9...12 for Roland Sysex)
28  1c	DV position			0 = no DV, 1 = position 0, 2 = position 1, etc.
	*** NOTE: If the Sysex DV Type is 0, then the DV position is undefined and can be anything
	*** NOTE: There are serious errors in Novation's editor, which et the Sysex DV Type and
	*** the DV position to unexpected values.
	*** NOTE: For Roland Sysex, the data position is fixed to Sysex Length - 2, so if sysex is 9, then this value is 7, representing position 8
29  1d	Sysex String		[12 bytes, padded with 0x00]
	*** NOTE: it appears that for the DV position, 0x7F normally serves as a placeholder




BUTTON DATA	[ 41 bytes ]

OFFSETS
DEC HEX
00  00	Name				[8 bytes, padded with 0x20]
08  08	Type				0=off, 1=CC, 2=NRPN, 3=RPN 4=Sysex 5=MMC 6=NoteOn/Off 8=Bank Change 9=PC 0C=Template 0D=Real Time 
09  09	(0x00)				[Normally Low Value MSB]
10  0a	Low	Value
		OR MMC Type
		OR Template Number (1-32, not 0-31)
		OR PC 
		OR Note Number
11  0b	(0x00)				[Normally High Value MSB]
12  0c	High Value
		OR Note Velocity
		or PC Press 2
13  0d	Button Type			0x00 = Normal/Off, 0x04 = Momentary, 0x08 = Toggle, 0x10 = Step
							**** NOTE: it says +0x01 is SEND THE MS VALUE FIRST and +0x02 is SEND A 2BYTE VALUE
							**** But these are not options available in the editors or on the unit
							OR (Bank Change) 0x00 = LSB, 0x01 = MSB, 0x02 = MSB-LSB
							OR (Prog Change) 0x00 = OFF, 0x01 = LSB, 0x02 = MSB, 0x03 = MSB-LSB 
14  0e	Real Time			[See Table 6]
15  0f	Display Format 		0=0...127, 1=-64...63, 3=ON/OFF, LED = 0x10
16  10	RPN/NRPN/Bank MSB
17  11	CC/RPN/NRPN/Bank LSB
18  12	Ports				[See Table 5]
	*** Note: I have seen this corrupted on the unit, where it returns 0x05 for "Common"
19  13	Channel				00: Common 	20: Keyboard 	40-4F:	[1-16]
		OR MMC Device		0...127
20  14	(0x00)				[Normally Default Value MSB]
21  15	Default Value
22  16	Step Size			0...63	[representing 1...64]
23  17	(0x00)				
24  18	(0x00)				
25  19	(0x00)
26  1a	Sysex DV Type		0: no DV	1: Single 4: Roland  [See Table 9]
27  1b	Sysex Length		0...12	(9...12 for Roland Sysex)
28  1c	DV position			0 = no DV, 1 = position 0, 2 = position 1, etc.
	*** NOTE: If the Sysex DV Type is 0, then the DV position is undefined and can be anything
	*** NOTE: There are serious errors in Novation's editor, which et the Sysex DV Type and
	*** the DV position to unexpected values.
	*** NOTE: For Roland Sysex, the data position is fixed to Sysex Length - 2, so if sysex is 9, then this value is 7, representing position 8
29  1d	Sysex String		[12 bytes, padded with 0x00]
	*** NOTE: it appears that for the DV position, 0x7F normally serves as a placeholder



DRUMPAD DATA	[ 41 bytes ]
	*** NOTE: Drumpad data also includes auto-off and off-sync-value, located not here but at 9c through b2
	
OFFSETS
DEC HEX
00  00	Name				[8 bytes, padded with 0x20]
08  08	Type				0=off, 1=CC, 2=NRPN, 3=RPN 4=Sysex 5=MMC 0B=Drumnote 8=Bank Change 9=PC   0C=Template 0D=Real Time 
09  09	(0x00)				[Normally Low Value MSB]
10  0a	Low	Value
		OR MMC Type
		OR Template Number (1-32, not 0-31)
		OR PC 
		OR Drum Note Number
11  0b	(0x00)				[Normally High Value MSB]
12  0c	High Value
		OR Drum note Velocity
13  0d	Button Type			0x00 = Normal/Off, 0x04 = Velocity, 0x08 = Toggle, 0x10 = Step
							**** NOTE: it says +0x01 is SEND THE MS VALUE FIRST and +0x02 is SEND A 2BYTE VALUE
							**** But these are not options available in the editors or on the unit
							**** NOTE: Momentary is not available for Drumpads, instead it's "Velocity"
							OR (Bank Change) 0x00 = LSB, 0x01 = MSB, 0x02 = MSB-LSB
							OR (Prog Change) 0x00 = OFF, 0x01 = LSB, 0x02 = MSB, 0x03 = MSB-LSB 
14  0e	Real Time			[See Table 6]
15  0f	Display Format 		0=0...127, 1=-64...63, 3=ON/OFF, LED = 0x10
16  10	RPN/NRPN/Bank MSB
17  11	CC/RPN/NRPN/Bank LSB
18  12	Ports				[See Table 5]
	*** Note: I have seen this corrupted on the unit, where it returns 0x05 for "Common"
19  13	Channel				00: Common 	20: Keyboard 	40-4F:	[1-16]
		OR MMC Device		0...127
20  14	(0x00)				[Normally Default Value MSB]
21  15	Default Value
22  16	Step Size			0...63	[representing 1...64]
23  17	(0x00)				
24  18	(0x00)				
25  19	(0x00)
26  1a	Sysex DV Type		0: no DV	1: Single 4: Roland  [See Table 9]
27  1b	Sysex Length		0...12	(9...12 for Roland Sysex)
28  1c	DV position			0 = no DV, 1 = position 0, 2 = position 1, etc.
	*** NOTE: If the Sysex DV Type is 0, then the DV position is undefined and can be anything
	*** NOTE: There are serious errors in Novation's editor, which et the Sysex DV Type and
	*** the DV position to unexpected values.
	*** NOTE: For Roland Sysex, the data position is fixed to Sysex Length - 2, so if sysex is 9, then this value is 7, representing position 8
29  1d	Sysex String		[12 bytes, padded with 0x00]
	*** NOTE: it appears that for the DV position, 0x7F normally serves as a placeholder
29  1d	Sysex				[12 bytes, padded with 0x00]
	*** NOTE: it appears that for the DV position, 0x7F usually serves as a placeholder



PITCHBEND DATA	[ 41 bytes ]

OFFSETS
DEC HEX
00  00	Name				[8 bytes, padded with 0x20]
08  08	Type				0=off, 1=CC, 2=NRPN, 3=RPN 4=Sysex 0A=Pitch Bend  
09  09	(0x00)				[Normally Low Value MSB]
10  0a	Low	Value
11  0b	(0x00)				[Normally High Value MSB]
12  0c	High Value
13  0d	(0x00)				[Normally Button Type]
14  0e	(0x00)				[Normally Real Time or Pot Pickup]
15  0f	Display Format 		0x00=0...127, 0x01=-64...+63
	*** NOTE: Novation's editor provides 0x06=REL1, 0x07=REL2, 0x11=APOT as well.  I suspect that
	*** the unit does not but I only have a Zero so I cannot confirm.  I believe the pitchbend
	*** works basically like a pot, so I can't imagine how it would be able to provide these options.
	*** NOTE: Novation's editor does not give the Pitch Bend wheel the options 0...127 or -64...+63.
	*** This may be an error, but I only have a Zero so I cannot confrm.
16  10	RPN or NRPN MSB
17  11	CC, RPN, or NRPN LSB
18  12	Ports				[See Table 5]
	*** Note: I have seen this corrupted on the unit, where it returns 0x05 for "Common"
19  13	Channel				00: Common 	20: Keyboard 	40-4F:	[1-16]
21  15	Default Value
22  16	(0x00)				[Normally Step Size]
23  17	(0x00)				
24  18	(0x00)				
25  19	(0x00)
26  1a	Sysex DV Type		0: no DV	1: Single 4: Roland  [See Table 9]
27  1b	Sysex Length		0...12	(9...12 for Roland Sysex)
28  1c	DV position			0 = no DV, 1 = position 0, 2 = position 1, etc.
	*** NOTE: If the Sysex DV Type is 0, then the DV position is undefined and can be anything
	*** NOTE: There are serious errors in Novation's editor, which et the Sysex DV Type and
	*** the DV position to unexpected values.
	*** NOTE: For Roland Sysex, the data position is fixed to Sysex Length - 2, so if sysex is 9, then this value is 7, representing position 8
29  1d	Sysex String		[12 bytes, padded with 0x00]
	*** NOTE: it appears that for the DV position, 0x7F normally serves as a placeholder



IN GENERAL...	[ 41 bytes ]

OFFSETS
DEC HEX
00  00	Name				[8 bytes, padded with 0x20]
08  08	Type				0=off, 1=CC, 2=NRPN, 3=RPN 4=Sysex 5=MMC 6=NoteOn/Off 8=Bank Change 9=PC  0A=Pitch Bend  0B=Drumnote 0C=Template 0D=Real Time 
	*** Note: 0x7, 0xC, and 0xE are not used
09  09	Low Value MSB
10  0a	Low	Value LSB / MMC Type / Drum note Number/ Template Number (1-32, not 0-31) / PC 
11  0b	High Value MSB
12  0c	High Value LSB / Drum note Velocity
13  0d	Button Type			0x00 = Normal/Off, 0x04 = Momentary, 0x08 = Toggle, 0x10 = Step
							**** NOTE: it says +0x01 is SEND THE MS VALUE FIRST and +0x02 is SEND A 2BYTE VALUE
							OR (Bank Change) 0x00 = LSB, 0x01 = MSB, 0x02 = MSB-LSB
							OR (Prog Change) 0x00 = OFF, 0x01 = LSB, 0x02 = MSB, 0x03 = MSB-LSB 
14  0e	Real Time			[See Table 6]
		OR Pot Pickup		[See Table 7]
		OR (0x50) otherwise
	*** NOTE: The Expression pedal and Modwheel do not have pot pickups.  Set them to (0x50)
15  0f	Display Format 		(0=0...127, 1=-64...63, 3=ON/OFF, REL = 0x06, REL2 = 0x07, 16-bit Values = 0x09, LED = 0x10, APOT = 0x11)
	*** NOTE: Novation's editor does not give the Pitch Bend or Modulation wheels the options 0...127 or -64...+63.
	*** This may be an error, but I only have a Zero so I cannot confrm.
16  10	RPN/NRPN/Bank MSB
17  11	CC/RPN/NRPN/Bank LSB
18  12	Ports				[See Table 5]
	*** Note: I have seen this corrupted on the unit, where it returns 0x05 for "Common"
19  13	Channel			00: Common 	20: Keyboard 	40-4F:	[1-16]		// Note AFTER USB Port, which is different.  Also differnet from other "common" stuff
+-> [or] MMC Device			0...127
20  14	Default Value MSB
21  15	Default Value LSB
22  16	Step Size			0...63	[representing 1...64]
23  17	0x00				
24  18	0x00				--CNSPARE,3--
25  19	0x00				--CNXATTR1--
26  1a	Sysex DV Type		0: no DV	1: Single	2: LSB-MSB 	3: MSB-LSB 4: Roland  [See Table 9]
27  1b	Sysex length		0...12
28  1c	DV position			0 = no DV, 1 = position 0, 2 = position 1, etc.
	*** NOTE: If the Sysex DV Type is 0, then the DV position is undefined and can be anything
	*** NOTE: There are serious errors in Novation's editor, which et the Sysex DV Type and
	*** the DV position to unexpected values.
	*** NOTE: For Roland Sysex, the data position is fixed
29  1d	Sysex				[12 bytes, padded with 0x00]
	*** NOTE: it appears that for the DV position, 0x7F usually serves as a placeholder




TABLE 1: DRUM NOTES OFF SYNC VALUES
0	Timer
1	32nd T
2	32nd
3	16th T
4	16th
5	8th T
6	16th D
7	8th
8	4th T
9	8th D
10	4th
11	2nd T
12	4th D
13	2nd
14	1 Bar T
15	2nd D
16	1 Bar
17	2 Bar T
18	1 Bar d
19	2 Bars
20	4 Bar T
21	3 Bars
22	5 Bar T
23	4 Bars
24	3 Bar D
25	7 Bar T
26	5 Bars
27	8 Bar T
28	6 Bars
29	7 Bars
30	5 Bar D
31	8 Bars
32	9 Bars
33	7 Bar D
34	12 Bars




TABLE 2: MMC Types
01 Stop 
02 Play 
03 Deferred Play 
04 Forward 
05 Rewind 
06 Record
07 Record Exit 
08 Record Pause 
09 Pause 
0a Eject 
0b Chase
0c Command Error Reset 
0d MMC Reset 
	*** NOTE there are many more in the MIDI spec, but may not be available




TABLE 3: COMMON PORTS
0	Off
41	MIDI 1
42	MIDI 2
43	MIDI 1 2
44	USB 1
45	USB 1 MIDI 1
46	USB 1 MIDI 2
47	USB 1 MIDI 1 2
48	USB 2
49	USB 2 MIDI 1
4a	USB 2 MIDI 2
4b	USB 2 MIDI 1 2
	*** NOTE there are no values 4c-4f because only one of USB1, USB2, or USB3 can be chosen
51	USB 3
51	USB 3 MIDI 1
52	USB 3 MIDI 2
53	USB 3 MIDI 1 2




TABLE 4: PROGRAM PORTS
	*** The "program" port is the port that pedals and drum pads go to, and I think is also the
	*** port that the keyboard goes to if zones are not turned on.  Called "Keyboard" in the editor
	*** but called "PROG" in the unit.
0	Use Common Ports
40	Off
41	MIDI 1
42	MIDI 2
43	MIDI 1 2
44	USB 1
45	USB 1 MIDI 1
46	USB 1 MIDI 2
47	USB 1 MIDI 1 2
48	USB 2
49	USB 2 MIDI 1
4a	USB 2 MIDI 2
4b	USB 2 MIDI 1 2
	*** NOTE USB 3 is not available for Program/Keyboard.
	*** NOTE It's not clear if Program/Keyboard can use USB 3 if we have chosen "Use Common Ports"
	*** NOTE only one of USB1, or USB2 can be chosen



TABLE 5: CONTROL PORTS
0	Use Common Ports
20	Use Program Ports
40	Off
41	MIDI 1
42	MIDI 2
43	MIDI 1 2
44	USB 1
45	USB 1 MIDI 1
46	USB 1 MIDI 2
47	USB 1 MIDI 1 2
48	USB 2
49	USB 2 MIDI 1
4a	USB 2 MIDI 2
4b	USB 2 MIDI 1 2
	*** NOTE there are no values 4c-4f because only one of USB1, USB2, or USB3 can be chosen
50	USB 3
51	USB 3 MIDI 1
52	USB 3 MIDI 2
53	USB 3 MIDI 1 2




TABLE 6: REAL TIME VALUES
50 [Empty]
	*** NOTE: if we're not doing real-time at all, this slot is set to 50, else this is not an option
51 Start Clock
52 Continue Clock
53 Stop Clock
54 Active Sensing
55 System Reset 




TABLE 7: POT PICKUP VALUES
00	Off
20	On
40	Template
60	Global

*** NOTE: These reflect bits 5 and 6 ONLY.  You should mask out the
*** remaining bits.

*** NOTE: The original Novation editor calls "Off" "Jump", and it calls
*** "On" "Pickup", but they are not called this on the unit.

*** NOTE: Other documentation suggests this should be in the order off, on, global, template,
*** but that appears to be wrong.




TABLE 8: KEYBOARD PORTS
40	Off
41	MIDI 1
42	MIDI 2
43	MIDI 1 2
44	USB 1
45	USB 1 MIDI 1
46	USB 1 MIDI 2
47	USB 1 MIDI 1 2
48	USB 2
49	USB 2 MIDI 1
4a	USB 2 MIDI 2
4b	USB 2 MIDI 1 2
	*** NOTE USB 3 is not available for Keyboard.


TABLE 9: NOTES ON SYSEX TYPES

0	NONE
1	SINGLE		0-12 long, with DV at position 1...n
2	LSB-MSB		0-12 long, with DV DV from 1...n-1
	*** NOTE: Only available for encoders when in 0-16K
3	MSB-LSB		0-12 long, with DV DV from 1...n-1
	*** NOTE: Only available for encoders when in 0-16K
4	ROLAND		9-12 long, with DV CS or DV DV CS taking up end


****/

