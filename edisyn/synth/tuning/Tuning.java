/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.tuning;

import edisyn.*;
import edisyn.gui.*;
import edisyn.util.*;
import edisyn.synth.tuning.*;
import edisyn.synth.tuning.tuningdefinitions.*;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.border.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javax.sound.midi.*;

public class Tuning extends Synth
{
	public static final String DEFAULT_NAME = "Tuning";
	public static final String[] NOTES = new String[] { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
	public static final int SCRATCH_SLOT = 0;
    
	public Tuning()
	{
		// Here you set up your interface.   You can look at other patch editors
		// to see how they were done.  At the very end you typically would say something like:
            
		VBox vbox = new VBox();
		vbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
		vbox.add(addTuning(1, Style.COLOR_A()));
		SynthPanel soundPanel = new SynthPanel(this);
		soundPanel.add(vbox, BorderLayout.CENTER);
		tabs.addTab("Global, 0-63", soundPanel);

		vbox = new VBox();
		vbox.add(addTuning(2, Style.COLOR_A()));
		soundPanel = new SynthPanel(this);
		soundPanel.add(vbox, BorderLayout.CENTER);
		tabs.addTab("64-127", soundPanel);
        
		model.set("name", DEFAULT_NAME);                // or whatever, to set the initial name of your patch (assuming you use "name" as the key for the patch name)
		model.set("number", 0);
		loadDefaults();                   // this tells Edisyn to load the ".init" sysex file you created.  If you haven't set that up, it won't bother
	}

	public JFrame sprout()     
	{
		JFrame frame = super.sprout();
		addTuningMenu();
        return frame;
    }

	/** Add the global patch category (name, id, number, etc.) */
	public JComponent addNameGlobal(Color color)
	{
		Category globalCategory = new Category(this, "MIDI Tuning Standard", color);
		globalCategory.makeUnresettable();
                
		JComponent comp;
		String[] params;
		HBox hbox = new HBox();
        
		VBox vbox = new VBox();
		comp = new PatchDisplay(this, 3);
		vbox.add(comp);
        
		comp = new StringComponent("Patch Name", this, "name", 16, "Name must be up to 16 ASCII characters.")
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
                                
		globalCategory.add(vbox, BorderLayout.WEST);
		return globalCategory;
	}
        
	public JComponent addTuning(int num, Color color)
	{
		Category category = new Category(this, "Tuning " + (num == 1 ? "0-63" : "64-127"), color);
                
		JComponent comp;
		String[] params;
        
		int pos = (num == 1 ? 0 : 64);
        
		VBox main = new VBox();
		for(int j = pos; j < pos + 64; j += 8)
			                   {
				                   HBox hbox = new HBox();
				                   for(int i = j; i < j + 8 ; i++)
					                   {
						                   if (i != j) hbox.add(Strut.makeHorizontalStrut(10));

						                   comp  = new LabelledDial("" + i + " Base", this, "base-" + i, color, 0, 127)
							                   {
								                   public String map(int val)
								                   {
									                   return (NOTES[val % 12] + " " + (val / 12)); 
								                   }
							                   };
						                   hbox.add(comp);

						                   comp  = new LabelledDial("" + i + " Detune", this, "detune-" + i, color, 0, 16383)
							                   {
								                   public String map(int val)
								                   {
									                   return String.format("%3.2f", (val / 16384.0 * 100.0));   // Note 16384, no 16383
								                   }
							                   };
						                   hbox.add(comp);
					                   }
				                   main.add(hbox);
			                   }
        
		category.add(main);
		return category;
	}


	public void setTunings(int[] base, int[] detune)
	{
		getUndo().push(model);
		getUndo().setWillPush(false);
		setSendMIDI(false);
		for(int i = 0; i < 128; i++)
			{
				model.set("base-" + i, base[i]);
				model.set("detune-" + i, detune[i]);
			}
		repaint();		// see discussion in Blofeld patch editor
		revise();
		setSendMIDI(true);
		getUndo().setWillPush(true);
		sendAllParameters();
	}

	static TuningDefinition[] tuningDefinitions = { new EDO() };
	    
	public void addTuningMenu()
	{
		JMenu menu = new JMenu("Tuning");
		menubar.add(menu);
		for(TuningDefinition definition : tuningDefinitions)
			{
			final TuningDefinition _def = definition;
			JMenuItem menuItem = new JMenuItem(definition.getMenuName());
			menuItem.addActionListener(new ActionListener() 
				{
					public void actionPerformed(ActionEvent e)
					{
						_def.popup(Tuning.this);
						if (_def.isConfigured())
							{
							setTunings(_def.getBases(), _def.getDetunes());
							}
					}
				});
			menu.add(menuItem);
			}
	}
        
        
	public byte getID() 
	{ 
		try 
			{ 
				byte b = (byte)(Byte.parseByte(tuple.id));
				if (b >= 0) return b;
			}
		catch (NullPointerException e) { } // expected.  Happens when tuple's not built yet
		catch (NumberFormatException e) { e.printStackTrace(); }
		return 0;
	}
        
	public String reviseID(String id)
	{
		try 
			{ 
				byte b =(byte)(Byte.parseByte(id)); 
				if (b >= 0) return "" + b;
			} 
		catch (NumberFormatException e) { }             // expected
		return "" + getID();
	}



	////// YOU MUST OVERRIDE ALL OF THE FOLLOWING

	public void changePatch(Model tempModel)
	{
		// we do nothing
	}

	public boolean gatherPatchInfo(String title, Model changeThis, boolean writing)     
	{
		JTextField number = new JTextField("" + (model.get("number") + 1), 3);

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
						showSimpleError(title, "The Patch Number must be an integer 1...128");
						continue;
					}
				if (n < 1 || n > 128)
					{
						showSimpleError(title, "The Patch Number must be an integer 1...128");
						continue;
					}
                
				n--;
                                
				changeThis.set("number", n);
                        
				return true;
			}
	}

	public static boolean recognize(byte[] data)
	{
		return (
		        data[0] == (byte)0xF0 &&
		        data[1] == 0x7E &&
		        data[3] == 0x08 &&
		        data[4] == 0x01 &&
		        data.length == 8 + 16 + 3 * 128);
	}

	public static String getSynthName() 
	{ 
		return "Tuning"; 
	}
    
	public String getDefaultResourceFileName() 
	{
		return "Tuning.init"; 
	}
        
	public String getHTMLResourceFileName() 
	{ 
		// Ultimately your synth will have an additional tab called "About", which displays an HTML
		// file.  This is a file ending in the extension ".html", such as "WaldorfBlofeld.html",
		// and is located right next to the class file (that is, "WaldorfBlofeld.class").
		// 
		// If you return null here, this tab will not be created.  But final
		// production code should not do that.
		return null; 
	}

	public String getPatchLocationName(Model model)
	{
		if (!model.exists("number")) return null;
		return ("" + (1 + model.get("number")));
	}
    
	public Model getNextPatchLocation(Model model)
	{
		int current = model.get("number", 0) + 1;
		if (current > 127) 
			current = 0;
                
		Model newModel = buildModel();
		newModel.set("number", current);
		return newModel;
	}

	public boolean patchLocationEquals(Model patch1, Model patch2)
	{
		return patch1.get("number", 0) == patch2.get("number", 0);
	}
 
	public String getPatchName(Model model) 
	{
		return model.get("name", DEFAULT_NAME);
	}

	public String revisePatchName(String name)
	{
		name = (name + "                ").substring(0, 16);
        
		char[] c = name.toCharArray();
		for(int i = 0; i < c.length; i++)
			if (c[i] > 127) c[i] = 32;

		return new String(c);       
	}
        
	public int parse(byte[] data, boolean fromFile)
	{
		char[] n = new char[16];
		for(int i = 0; i < 16; i++)
			{
				n[i] = (char)(data[6 + i]);
			}
		model.set("name", new String(n));
        
		for(int i = 0; i < 128; i++)
			{
				model.set("base-" + i, data[22 + (i * 3)]);
				model.set("detune-" + i, (data[22 + (i * 3) + 1] << 7) | data[22 + (i * 3) + 2]);
			}
        
		return PARSE_SUCCEEDED;
	}
        
	public byte[] emit(Model tempModel, boolean toWorkingMemory, boolean toFile) 
	{ 
        if (tempModel == null)
            tempModel = getModel();

		byte[] data = new byte[8 + 16 + 3 * 128];
		data[0] = (byte)0xF0;
		data[1] = 0x7E;
		data[2] = getID();
		data[3] = 0x08;
		data[4] = 0x01;
		data[5] = (byte)(toWorkingMemory ? SCRATCH_SLOT : tempModel.get("number"));
        
		String name = model.get("name", DEFAULT_NAME) + "                ";
		for(int i = 0; i < 16; i++)
			{
				data[6 + i] = (byte)(((int)name.charAt(i)) & 127);
			}
        	
		for(int i = 0; i < 128; i++)
			{
				int base = model.get("base-" + i);
				int detune = model.get("detune-" + i);
				int msb = ((detune >>> 7) & 127);
				int lsb = (detune & 127);
				data[22 + (i * 3)] = (byte)base;
				data[22 + (i * 3) + 1] = (byte)msb;
				data[22 + (i * 3) + 2] = (byte)lsb;
			}
        	
		int check = data[1];
		for(int i = 2; i < data.length - 2; i++)
			{
				check = check ^ data[i];
			}
        
		data[data.length - 2] = (byte)check;
		data[data.length - 1] = (byte)0xF7;
		return data;
	}
    
    
	public byte[] emit(String key) 
	{ 
		if (key.equals("number")) return new byte[0];
		if (key.equals("name")) return new byte[0];
        
		int k = StringUtility.getFirstInt(key);
		int base = model.get("base-" + k);
		int detune = model.get("detune-" + k);
		int msb = ((detune >>> 7) & 127);
		int lsb = (detune & 127);
	
		return new byte[] { (byte)0xF0, 0x7E, getID(), 0x08, 0x02, (byte)model.get("number"), 
		                    0x01, (byte)(k - 1), (byte)base, (byte)msb, (byte)lsb, (byte)0xF7 };
	}

	public byte[] requestCurrentDump() 
	{ 
		return new byte[] { (byte)0xF0, 0x7E, getID(), 0x08, 0x00, (byte)SCRATCH_SLOT, (byte)0xF7 };
	}

	public byte[] requestDump(Model tempModel) 
	{ 
        if (tempModel == null)
            tempModel = getModel();

		return new byte[] { (byte)0xF0, 0x7E, getID(), 0x08, 0x00, (byte)tempModel.get("number"), (byte)0xF7 };
	}



}
