/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth;

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
   A patch editor for the Waldorf Microwave XT.  Does not deal with Multi mode, global parameters,
   modifying wavetables, or uploading samples.  Only Single mode patches.
        
   @author Sean Luke
*/

public class PreenFM2 extends Synth
    {
        
    public static final String VALID_CHARACTERS = "_ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789.,;:<>&*$";
    public static final String[] BANK_TYPES_IN = { "Bank", "DX7" };
    public static final String[] BANK_TYPES_OUT = { "Bank" };
    public static final String[] OPERATOR_SHAPES = { "Sin", "Saw", "Square", "Sin^2", "Sin Zero", "Sin Pos", "Random", "Off", "User 1", "User 2", "User 3", "User 4", "User 5", "User 6" };
    public static final String[] FILTER_TYPES = { "Off", "Mix", "Low Pass", "High Pass", "Bass Boost", "Band Pass", "Crusher" };
    public static final String[] ARPEGGIATOR_CLOCK = { "Off", "Internal", "External" };
    public static final String[] ARPEGGIATOR_DIRECTIONS = { "Up", "Down", "Up-Down", "Play", "Random", "Chord", "Rotate Up", "Rotate Down", "Rotate Up-Down", "Shift Up", "Shift Down", "Shift Up-Down" };
    public static final String[] ARPEGGIATOR_PATTERNS = { "o.o.o.o.o.o.o.o.   (1)", "o.o.ooooo.o.oooo   (2)", "o.o.oo.oo.o.oo.o   (3)", "o.o.o.ooo.o.o.oo   (4)", "o.o.o.o.oo.o.o.o   (5)", "o.o.o.o.o..oo.o.   (6)", "o.o.o..oo.o.o..o   (7)", "o..o....o..o....   (8)", "o..o..o..o..o..o   (9)", "o..o..o..o..o.o.   (10)", "o..o..o.o..o..o.   (11)", "o..oo...o.o.o.oo   (12)", "oo.o.oo.oo.o.oo.   (13)", "oo.oo.o.oo.oo.o.   (14)", "ooo.ooo.ooo.ooo.   (15)", "ooo.oo.oo.oo.oo.   (16)", "ooo.o.o.ooo.o.o.   (17)", "oooo.oo.oooo.oo.   (18)", "ooooo.oo.oo.ooo.   (19)", "o...o...o..o.o.o   (20)", "o.....oooooo.oo.   (21)", "o.......o...o.oo   (22)", "User 1", "User 2", "User 3", "User 4" };
    public static final String[] ARPEGGIATOR_DIVISIONS = { "2/1", "3/2", "1/1", "3/4", "2/3", "1/2", "3/8", "1/3", "1/4", "1/6", "1/8", "1/12", "1/16", "1/24", "1/32", "1/48", "1/96" };
    public static final String[] MODULATION_SOURCES = { "Off", "LFO 1", "LFO 2", "LFO 3", "Env 1", "Env 2", "Seq 1", "Seq 2", "Mod Wheel", "Pitch Bend", "Aftertouch", "Velocity", "Note 1", "Note 2", "Breath", "Perf 1", "Perf 2", "Perf 3", "Perf 4" };
	public static final String[] MODULATION_DESTINATIONS = { "Off", "Gate", "Modulation Index 1", "Modulation Index 2", "Modulation Index 3", "Modulation Index 4", "All Modulation Indices", "Mix1", "Pan1", "Mix2", "Pan2", "Mix3", "Pan3", "Mix3", "Pan3", "All Mixes", "All Pans", "Op 1 Frequency", "Op 2 Frequency", "Op 3 Frequency", "Op 4 Frequency", "Op 5 Frequency", "Op 6 Frequency", "All Op Frequencies", "Op 1 Attack", "Op 2 Attack", "Op 3 Attack", "Op 4 Attack", "Op 5 Attack", "Op 6 Attack", "All Op Attacks", "All Op Decays", "All Op Releases", "Matrix Multiplier 1", "Matrix Multiplier 2", "Matrix Multiplier 3", "Matrix Multiplier 4", "LFO 1 Frequency", "LFO 2 Frequency", "LFO 3 Frequency", "Envelope 1 Silence", "Step Sequencer 1 Gate", "Step Sequencer 2 Gate", "Filter Frequency" };
    public static final String[] LFO_SHAPES = { "Sine", "Saw Up", "Saw Down", "Square", "Random" };
	public static final String[] LFO_CLOCKS = { "Clk/16", "Clk/8", "Clk/4", "Clk/2", "Clock", "Clk*2", "Clk*3", "Clk*4", "Clk*8" };
	public static final String[] SEQUENCER_CLOCKS = { "Clk/4", "Clk/2", "Clock", "Clk*2", "Clk*4" };
	public static final String[] ENV_LOOPS = { "None", "Silence", "Attack" };
	public static final String[] NOTE_SCALING = { "Flat", "+Linear", "+Linear x 8", "+Exp", "-Linear", "-Linear x 8", "-Exp"  };
    public static final String[] KEYS = new String[] { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };

    public PreenFM2()
        {
        model.set("channel", 0);
        model.set("bank", 0);
        model.set("number", 0);
        
        setSendsAllParametersInBulk(false);
        
        /// SOUND PANEL
                
        JComponent frontPanel = new SynthPanel();
        VBox vbox = new VBox();
        
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL));
        hbox.add(addGeneral(Style.COLOR_A));
 		hbox.addLast(addFilter(Style.COLOR_A));
       	vbox.add(hbox);
 
        vbox.add(addModulationIndices(Style.COLOR_A));
        
        hbox = new HBox();
		hbox.add(addArpeggiator(Style.COLOR_B));
   		hbox.addLast(addPerformanceParameters(Style.COLOR_B));
       	vbox.add(hbox);
       	      
        vbox.add(addModulation(Style.COLOR_C));

        frontPanel.add(vbox, BorderLayout.CENTER);
        tabs.addTab("Front", frontPanel);
                
                
        // OSCILLATOR PANEL
                
        JComponent operatorPanel = new SynthPanel();
        vbox = new VBox();
        
        for(int i = 1; i < 7; i++)
        	{
        	vbox.add(addOperator(i, Style.COLOR_A));
        	}

        operatorPanel.add(vbox, BorderLayout.CENTER);
        tabs.addTab("Operators", operatorPanel);

        
        
        // LFOS PANEL
                
        JComponent lfoPanel = new SynthPanel();
        
        vbox = new VBox();
        hbox = new HBox();
        hbox.add(addLFO(1, Style.COLOR_A));
        hbox.add(addLFO(2, Style.COLOR_A));
        hbox.addLast(addLFO(3, Style.COLOR_A));
        vbox.add(hbox);
        
        hbox = new HBox();
        hbox.add(addEnvelope(1, Style.COLOR_B));
        hbox.addLast(addEnvelope(2, Style.COLOR_B));
        vbox.add(hbox);
        
        hbox = new HBox();
        hbox.add(addStepSequencer(1, Style.COLOR_C));
        hbox.addLast(addNoteScaling(1, Style.COLOR_C));
        vbox.add(hbox);

        hbox = new HBox();
        hbox.add(addStepSequencer(2, Style.COLOR_C));
        hbox.addLast(addNoteScaling(2, Style.COLOR_C));
        vbox.add(hbox);
                                
        lfoPanel.add(vbox, BorderLayout.CENTER);
        tabs.addTab("LFO and Envelopes", lfoPanel);


        
        tabs.addTab("About", new HTMLBrowser(this.getClass().getResourceAsStream("PreenFM2.html")));

        model.set("name", "Untitled Snd");  // has to be 12 long

        // make sure this never can be mutated
        model.set("soundformatversion", 1);  // always
        model.setImmutable("soundformatversion", true);
 
        buildParameterMap();
       
       
       
//        loadDefaults();        
        }
                
    public String getDefaultResourceFileName() { return "PreenFM2.init"; }

    public boolean gatherInfo(String title, Model change, boolean writing)
        {
        JComboBox type = null;

        if (writing)
	        {
	     	type = new JComboBox(BANK_TYPES_OUT);
	        }
	    else
	    	{
	    	type = new JComboBox(BANK_TYPES_IN);
	    	}

		int _bank = model.get("bank", 0);
		if (_bank > 255 && writing)  // it's DX7
			{
			_bank = 0;
			}
		else if (_bank > 127 && _bank <= 255)  // It's combo, always disallow
			{
			_bank = 0;
			}
		type.setSelectedIndex( _bank < 256 ? 0 : 1);
		type.setEditable(false);
		type.setMaximumRowCount(32);
                
        JTextField bank = new JTextField("" + _bank, 3);
        JTextField number = new JTextField("" + (model.get("number", 0)), 3);
        JTextField channel = new JTextField("" + model.get("channel", 0), 3);
                
        while(true)
            {
            boolean result = doMultiOption(this, new String[] { "Bank Type", "Bank", "Patch Number", "MIDI Channel" }, 
                new JComponent[] { type, bank, number, channel }, title, "Enter the Bank Type, Bank, Patch number, and MIDI Channel.");
                
            if (result == false) 
                return false;
                
            int t = type.getSelectedIndex();
                                
            int b;
            try { b = Integer.parseInt(bank.getText()); }
            catch (NumberFormatException e)
                {
                JOptionPane.showMessageDialog(null, "The Bank Number must be an integer", title, JOptionPane.ERROR_MESSAGE);
                continue;
                }
            if ((t == 0) && (b < 0 || b > 63))
                {
                JOptionPane.showMessageDialog(null, "The Bank Number must be an integer  0...63 for this bank type", title, JOptionPane.ERROR_MESSAGE);
                continue;
                }
            else if ((t == 1) && (b < 0 || b > 255))
                {
                JOptionPane.showMessageDialog(null, "The Patch Number must be an integer  0...255 for this bank type", title, JOptionPane.ERROR_MESSAGE);
                continue;
                }

            int n;
            try { n = Integer.parseInt(number.getText()); }
            catch (NumberFormatException e)
                {
                JOptionPane.showMessageDialog(null, "The Patch Number must be an integer", title, JOptionPane.ERROR_MESSAGE);
                continue;
                }
            if ((t == 0) && (n < 0 || n > 127))
                {
                JOptionPane.showMessageDialog(null, "The Patch Number must be an integer  0...127 for this bank type", title, JOptionPane.ERROR_MESSAGE);
                continue;
                }
            else if ((t == 1) && (n < 0 || n > 31))
                {
                JOptionPane.showMessageDialog(null, "The Patch Number must be an integer  0...31 for this bank type", title, JOptionPane.ERROR_MESSAGE);
                continue;
                }
                                
            int c;
            try { c = Integer.parseInt(channel.getText()); }
            catch (NumberFormatException e)
                {
                JOptionPane.showMessageDialog(null, "The Channel must be an integer 1 ... 16", title, JOptionPane.ERROR_MESSAGE);
                continue;
                }
            if (c < 1 || c > 16)
                {
                JOptionPane.showMessageDialog(null, "The Channel must be an integer 0 ... 16", title, JOptionPane.ERROR_MESSAGE);
                continue;
                }
                        
            change.set("bank", t == 0 ? b : b + 256);
            change.set("number", n);
            change.set("channel", c - 1);
                        
            return true;
            }
        }


	
    /** Add the global patch category (name, id, number, etc.) */
    public JComponent addNameGlobal(Color color)
        {
        Category globalCategory = new Category("PreenFM2", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
                
        VBox vbox = new VBox();
        comp = new StringComponent("Patch Name", this, "name", 10, "Name must be up to 10 ASCII characters.")
            {
            public boolean isValid(String val)
                {
                if (val.length() > 10) return false;
                for(int i = 0 ; i < val.length(); i++)
                    {
                    char c = val.charAt(i);
                    if (VALID_CHARACTERS.indexOf(c) < 0) return false;
                    }
                return true;
                }
                                
            public void update(String key, Model model)
                {
                super.update(key, model);
                updateTitle();
                }
            };
        model.setImmutable("name", true);
        vbox.add(comp);
        hbox.add(vbox);
                        
        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }

        
    public JComponent addGeneral(Color color)
        {
        Category globalCategory = new Category("General", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
                
		comp = new LabelledDial("Algorithm", this, "algorithm", color, 0, 28);
		hbox.add(comp);

		comp = new LabelledDial("Velocity", this, "velocity", color, 0, 16);
		hbox.add(comp);

		comp = new LabelledDial("Voices", this, "voice", color, 0, 16);
		hbox.add(comp);

		// Maybe this should disappear if voices = 1
		comp = new LabelledDial("Glide", this, "glide", color, 0, 10);
		hbox.add(comp);

        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }


    public JComponent addModulationIndices(Color color)
        {
        Category globalCategory = new Category("Operator Modulation Indices", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();

        for(int i = 1; i < 6; i++)
	        {
	        comp = new LabelledDial("Index " + i, this, "im" + i, color, 0, 1600);
	        hbox.add(comp);

	        comp = new LabelledDial("Index " + i, this, "im" + i + "velocity", color, 0, 1600);
	        ((LabelledDial)comp).setSecondLabel("Velocity");
	        hbox.add(comp);
	        }
        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }

                        

    public JComponent addFilter(Color color)
        {
        Category globalCategory = new Category("Filter", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
             
        params = FILTER_TYPES;
        comp = new Chooser("Type", this, "filtertype", params);
        vbox.add(comp);
        hbox.add(vbox);

// these need to be customized
		comp = new LabelledDial("Parameter 1", this, "filterparam1", color, 0, 100);
		hbox.add(comp);

		comp = new LabelledDial("Parameter 2", this, "filterparam2", color, 0, 100);
		hbox.add(comp);

		comp = new LabelledDial("Gain", this, "filtergain", color, 0, 200);
		hbox.add(comp);

        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }

        
    public JComponent addArpeggiator(Color color)
        {
        Category globalCategory = new Category("Arpeggiator", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
                
        VBox vbox = new VBox();
        params = ARPEGGIATOR_CLOCK;
        comp = new Chooser("Clock", this, "arpeggiatorclock", params);
        vbox.add(comp);

        comp = new CheckBox("Latch", this, "arpeggiatorlatch");
        vbox.add(comp);
        hbox.add(vbox);
        
        vbox = new VBox();
        
        params = ARPEGGIATOR_DIRECTIONS;
        comp = new Chooser("Direction", this, "arpeggiatordirection", params);
        vbox.add(comp);

        params = new String[ARPEGGIATOR_PATTERNS.length];
        for(int i = 0; i < params.length; i++)
        	{
        	params[i] = ARPEGGIATOR_PATTERNS[i].replace('o', '\uFFED').replace('.', '\uFFEE');
        	}
        comp = new Chooser("Pattern", this, "arpeggiatorpattern", params);
        vbox.add(comp);
        hbox.add(vbox);


        vbox = new VBox();

        params = ARPEGGIATOR_DIVISIONS;
        comp = new Chooser("Division", this, "arpeggiatordivision", params);
        vbox.add(comp);

        params = ARPEGGIATOR_DIVISIONS;  // yes, divisions
        comp = new Chooser("Duration", this, "arpeggiatorduration", params);
        vbox.add(comp);
        hbox.add(vbox);

		comp = new LabelledDial("BPM", this, "arpeggiatorbpm", color, 0, 230);
		hbox.add(comp);

		comp = new LabelledDial("Octave", this, "arpeggiatoroctave", color, 0, 2); // or 1--3?
		hbox.add(comp);

        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }

        
    



    public JComponent addOperator(final int op, Color color)
        {
        Category category = new Category("Operator " + op, color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
                        
        params = OPERATOR_SHAPES;
        comp = new Chooser("Shape", this, "op" + op + "shape", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Mix", this, "op" + op + "mix", color, 0, 100);
        hbox.add(comp);

        comp = new LabelledDial("Pan", this, "op" + op + "pan", color, 0, 200)
        	{
            public String map(int val)
                {
                if (val == 100) 
                	return "--";
                else if (val > 100) 
                	return "R " + (val - 100);
                else // if (val < 100) 
                	return "L " + (100 - val);
                }
        	};
        hbox.add(comp);

        comp = new CheckBox("Fixed Freq", this, "op" + op + "freqtype");
        ((CheckBox)comp).addToWidth(1);
        vbox.add(comp);
        
        comp = new LabelledDial("Frequency", this, "op" + op + "frequency", color, 0, 192)
        	{
            public String map(int val)
                {
				int hi = (val / 3);
				int lo = (val % 64);
				if (lo == 0) lo = 0;  // duh
				else if (lo == 1) lo = 83;
				else if (lo == 2) lo = 166;

                int fixed = model.get("op" + op + "fixed", 0);
                if (fixed == 1)
                	{
                	return "" + (hi * 250 + lo);
                	}
                else
                	{
                	return "" + (hi * 250 + lo) / 1000.0;
                	}
                }
        	};
        model.register("op" + op + "fixed", (LabelledDial)comp);
        hbox.add(comp);

        comp = new LabelledDial("Fine Tune", this, "op" + op + "finetune", color, 0, 200)
        	{
            public String map(int val)
                {
                int fixed = model.get("op" + op + "fixed", 0);
                if (fixed == 1)
                	{
                	return "" + (val - 100);
                	}
                else
                	{
                	return "" + (val - 100) / 100.0;
                	}
                }
        	};
        model.register("op" + op + "fixed", (LabelledDial)comp);
        hbox.add(comp);

        comp = new LabelledDial("Attack", this, "op" + op + "envattack", color, 0, 100);
        hbox.add(comp);

        comp = new LabelledDial("Attack Lvl", this, "op" + op + "envattacklevel", color, 0, 100);
        hbox.add(comp);

        comp = new LabelledDial("Decay", this, "op" + op + "envdecay", color, 0, 100);
        hbox.add(comp);

        comp = new LabelledDial("Decay Lvl", this, "op" + op + "envdecaylevel", color, 0, 100);
        hbox.add(comp);

        comp = new LabelledDial("Sustain", this, "op" + op + "envsustain", color, 0, 100);
        hbox.add(comp);

        comp = new LabelledDial("Sustain Lvl", this, "op" + op + "envsustainlevel", color, 0, 100);
        hbox.add(comp);

        comp = new LabelledDial("Release", this, "op" + op + "envrelease", color, 0, 100);
        hbox.add(comp);

        comp = new LabelledDial("Release Lvl", this, "op" + op + "envreleaselevel", color, 0, 100);
        hbox.add(comp);

        comp = new EnvelopeDisplay(this, Color.red, 
            new String[] { null, "op" + op + "envattack", "op" + op + "envdecay", "op" + op + "envsustain", null, "op" + op + "envrelease" },
            new String[] { "op" + op + "envreleaselevel" , "op" + op + "envattacklevel", "op" + op + "envdecaylevel", "op" + op + "envsustainlevel",  "op" + op + "envsustainlevel", "op" + op + "envreleaselevel" },
            new double[] { 0, 0.2/100.0, 0.2/100.0,  0.2/100.0, 0.2, 0.2/100.0},
            new double[] { 1/100.0, 1/100.0, 1/100.0, 1/100.0, 1/100.0, 1/100.0 });
        hbox.addLast(comp);

        category.add(hbox, BorderLayout.WEST);
        return category;
        }


    /** Add the Modulation category */
    public JComponent addModulation(Color color)
        {
        Category category  = new Category("Modulation", color);
                        
        JComponent comp;
        String[] params;
        VBox main = new VBox();
        HBox hbox;
        VBox vbox;
        
        for(int row = 1; row < 13; row+= 4)
            {
            hbox = new HBox();
            boolean first = true;
            for(int i = row; i < row + 4; i++)
                {
                vbox = new VBox();

                // add some space
                if (!first)  // not the first one
                    {
                    hbox.add(Strut.makeHorizontalStrut(10));
                    }

                params = MODULATION_SOURCES;
                comp = new Chooser("" + i + " Source", this, "modulation" + i + "source", params);
                // model.setSpecial("mod" + i + "source", 0);
                vbox.add(comp);

                params = MODULATION_DESTINATIONS;
                comp = new Chooser("" + i + " Destination", this, "modulation" + i + "destination", params);
                vbox.add(comp);

                hbox.add(vbox);
                comp = new LabelledDial("" + i + " Level", this, "modulation" + i + "amount", color, 0, 2000)
                	{
					public String map(int val)
						{
						return "" + (val - 1000) / 100.0;
						}
                	};
                hbox.add(comp);

                first = false;
                }
                        
            // add some space
            if (row > 1)  // not the first one
                {
                main.add(Strut.makeVerticalStrut(10));
                }

            main.add(hbox);
            }
                                
        category.add(main, BorderLayout.WEST);
        return category;
        }



    public JComponent addPerformanceParameters(Color color)
        {
        Category category  = new Category("Performance Parameters", color);
                        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        for(int p = 1; p < 5; p++)
            {
			comp = new LabelledDial("Param " + p, this, "performanceparam" + p, color, 0, 200)
				{
				public String map(int val)
					{
					return "" + (val - 100) / 100.0;
					}
				};
			hbox.add(comp);
			}
                                
        category.add(hbox, BorderLayout.WEST);
        return category;
        }


    public JComponent addLFO(int lfo, Color color)
        {
        Category category  = new Category("LFO " + lfo, color);
                        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = LFO_SHAPES;
        comp = new Chooser("Shape", this, "lfo" + lfo + "shape", params);
        vbox.add(comp);
        
        comp = new LabelledDial("Frequency", this, "lfo" + lfo + "frequency", color, 0, 336)
            {
            public String map(int val)
                {
                // It goes 0.01...0.97 by 0.01	(97 of them)
                // It goes 1.0 ... 24 by 0.10 (231 of them)
                // Then there is M/16, MC/8, MC/4, MC/2, MClk, MC*2, MC*3, MC*4, MC*8
                // so it's 337 altogether
                
                if (val < 97)
                	{
                	return "" + (val + 1) / 100.0;
                	}
                else if (val < 328)
                	{
                	return "" + (val - 97) / 10.0 + 1;
                	}
                else
                	{
                	return LFO_CLOCKS[val - 328];
                	}
                }
            };
        hbox.add(comp);
        
        comp = new LabelledDial("Bias", this, "lfo" + lfo + "bias", color, 0, 200)
            {
            public String map(int val)
                {
                return "" + (val - 100) / 100.0;
                }
            };
        hbox.add(comp);
        
        comp = new LabelledDial("Key Sync", this, "lfo" + lfo + "keysync", color, 0, 0, 1601)
            {
            public String map(int val)
                {
                if (val == 0) return "Off";
                else return "" + (val - 1) / 100.0;
                }
            };
        hbox.add(comp);
        
        comp = new LabelledDial("Phase", this, "lfo" + lfo + "phase", color, 0, 100)
        	{
            public String map(int val)
                {
                return "" + (val - 1) / 100.0;
                }
        	};
        hbox.add(comp);

        category.add(hbox, BorderLayout.WEST);
        return category;
        }










        
    /** Add a "standard" envelope category */
    public JComponent addEnvelope(final int env, Color color)
        {
        Category category = new Category("Envelope " + env, color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        if (env == 2)
        	{
        	comp = new LabelledDial("Silence", this, "freeenv" + env + "silence", color, 0, 1600)
				{
				public String map(int val)
					{
					return "" + (val / 100.0);
					}
				};
	        hbox.add(comp);
        	}

        comp = new LabelledDial("Attack", this, "freeenv" + env + "attack", color, 0, 1600)
        	{
            public String map(int val)
                {
                return "" + (val / 100.0);
                }
        	};
        hbox.add(comp);
        
        comp = new LabelledDial("Decay", this, "freeenv" + env + "decay", color, 0, 1600)
        	{
            public String map(int val)
                {
                return "" + (val / 100.0);
                }
        	};
        hbox.add(comp);
        
        if (env == 1)
        	{
        	comp = new LabelledDial("Sustain", this, "freeenv" + env + "sustain", color, 0, 1600)
				{
				public String map(int val)
					{
					return "" + (val / 100.0);
					}
				};
	        hbox.add(comp);
        
	        comp = new LabelledDial("Release", this, "freeenv" + env + "release", color, 0, 1600)
				{
				public String map(int val)
					{
					return "" + (val / 100.0);
					}
				};
	        hbox.add(comp);

        comp = new EnvelopeDisplay(this, Color.red, 
            new String[] { null, "freeenv" + env + "attack", "freeenv" + env + "decay", null, "freeenv" + env + "release" },
            new String[] { null, null, "freeenv" + env + "sustain", "freeenv" + env + "sustain", null },
            new double[] { 0, 0.25/1600, 0.25/1600,  0.25, 0.25/1600},
            new double[] { 0, 1.0, 1.0/1600, 1.0/1600, 0 });
        ((EnvelopeDisplay)comp).setPreferredWidth(200);
        hbox.addLast(comp);
	    	}
	    else
	    	{
        	VBox vbox = new VBox();
        	params = ENV_LOOPS;
        	comp = new Chooser("Loop", this, "freeenv" + env + "loop", params);
        	vbox.add(comp);
			hbox.add(vbox);

        comp = new EnvelopeDisplay(this, Color.red, 
            new String[] { null, "freeenv" + env + "silence", "freeenv" + env + "attack", "freeenv" + env + "decay" },
            new String[] { null, null, null, null },
            new double[] { 0, 0.2/1600, 0.2/1600, 0.2/1600},
            new double[] { 0, 0.0, 1.0, 0 });
        ((EnvelopeDisplay)comp).setPreferredWidth(200);
        hbox.add(comp);
        hbox.addLast(new HBox());

	    	}
        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
        


    public JComponent addStepSequencer(int seq, Color color)
        {
        Category category  = new Category("Step Sequencer " + seq, color);
                                
        JComponent comp;
        String[] params;
        HBox main = new HBox();
        HBox hbox = new HBox();


        VBox vbox = new VBox();
		comp = new LabelledDial("BPM", this, "stepseq" + seq + "bpm", color, 0, 235)
			{
			// goes 10...240
			// then MC/4 MC/2 MC MCx2 MCx4
			public String map(int val)
				{
				if (val <= 230)
					{
					return "" + (val + 10);
					}
				else
					return SEQUENCER_CLOCKS[val - 231];
				}
			};
		hbox.add(comp);

		comp = new LabelledDial("Gate", this, "stepseq" + seq + "gate", color, 0, 100)
			{
			public String map(int val)
				{
				return "" + (val / 100.0);
				}
			};
		hbox.add(comp);
		vbox.add(hbox);
		main.add(vbox);
		
		vbox = new VBox();
		hbox = new HBox();
		
		for(int i = 1; i < 9; i++)
			{
			comp = new LabelledDial("Step " + i, this, "stepseq" + seq + "step" + i, color, 0, 15);
			hbox.add(comp);
			}
		vbox.add(hbox);
		
		vbox.add(Strut.makeVerticalStrut(10));
		
		hbox = new HBox();
		for(int i = 9; i < 17; i++)
			{
			comp = new LabelledDial("Step " + i, this, "stepseq" + seq + "step" + i, color, 0, 15);
			hbox.add(comp);
			}
		vbox.add(hbox);
		main.add(vbox);
		
        category.add(main, BorderLayout.WEST);
        return category;
        }


    public JComponent addNoteScaling(int note, Color color)
        {
        Category category  = new Category("Note " + note + " MIDI Scaling", color);
                                
        JComponent comp;
        String[] params;
        HBox main = new HBox();
        HBox hbox = new HBox();


        VBox vbox = new VBox();
        params = NOTE_SCALING;
        comp = new Chooser("Before", this, "notescaling" + note + "before", params);
        vbox.add(comp);

        params = NOTE_SCALING;
        comp = new Chooser("After", this, "notescaling" + note + "after", params);
        vbox.add(comp);
        hbox.add(vbox);

		comp = new LabelledDial("Break Note", this, "notescaling" + note + "breaknote", color, 0, 127)
			{
            public String map(int val)
                {
                return KEYS[val % 12] + (val / 12 + 1);  // note integer division
                }
			};
		hbox.add(comp);

        category.add(hbox, BorderLayout.WEST);
        return category;
        }

    public void changePatch(Model tempModel)
        {
        // Banks are:
        // 0..127			Bank
        // 128...255		Combo
        // 256...256+255	DX7
        int bank = tempModel.get("bank", 0);
        int msb = (bank >> 7);
        int lsb = (bank & 127);
        
        int program = tempModel.get("number", 0);
        int channel = tempModel.get("channel", 0);
        
        tryToSendSysex(buildLongCC(channel, 0, bank));
        tryToSendSysex(buildPC(channel, program));
        }

    public void performRequestCurrentDump()
    	{
    	int channel = getModel().get("channel", 0);
		
		// Send an NRPN with param = MSB127, LSB127, and a value of whatever (here, 0).
        tryToSendSysex(buildNRPN(channel, (127 << 7) | 127, 0));
    	}

    public Object[] emitAll(String key)
    	{
    	Model model = getModel();
    	int channel = model.get("channel", 0);
    	int param = ((Integer)(parameterToIndex.get(key))).intValue();
    	if (key.startsWith("name")) 
    		{
    		String value = model.get("name", "Untitled Snd");
    		int index = (int)(key.charAt(4) - '0');
    		return buildNRPN(channel, param, value.charAt(index - 1));  // we don't have a name0, we start at name1
       		}
       	else if (key.startsWith("lfo1frequency") || key.startsWith("lfo2frequency") || key.startsWith("lfo3frequency"))
       		{
	    	int value = model.get(key, 0);
	    	if (value < 97)
	    		value += 1;
	    	else if (value >= 97)
	    		value = (value - 97) * 10 + 100;
	    	else if (value >= (97 + 231))
	    		value = ((value - (97 + 231)) * 10 + 2410);
	    	return buildNRPN(channel, param, value);
       		}
    	else
    		{
	    	int value = model.get(key, 0);
	    	return buildNRPN(channel, param, value);
	    	}
    	}

    public static boolean recognize(byte[] data)
        {
        // we don't handle sysex patches yet, they're full memory dumps, not what we want
        return false;
        }
        
    /** Verify that all the parameters are within valid values, and tweak them if not. */
    public void revise()
        {
        // check the easy stuff -- out of range parameters
        super.revise();

        // handle "name" specially
        StringBuffer name = new StringBuffer(model.get("name", "Untitled Snd"));  // has to be 12 long
		for(int i = name.length(); i < 12; i++)
			name.append("_");  // or should it be " "?
			
		for(int i = 0 ; i < name.length(); i++)
			{
			char c = name.charAt(i);
			if (VALID_CHARACTERS.indexOf(c) < 0)
				{
				name.setCharAt(i, '_');
				System.err.println("Warning: Revised name from \"" + model.get("name", "Untitled Snd") + "\" to \"" + name.toString() + "\"");
				}
			}

        model.set("name", name.toString());
        }
        

	public void handleSynthCCOrNRPN(Midi.CCData data)
		{
		if (data.type == Midi.CCDATA_TYPE_NRPN)
			{
			String key = (String)(indexToParameter.get(Integer.valueOf(data.number)));
			System.err.println("" + data.number + "    " + key);
			if (key != null)
				{
				if (key.startsWith("name"))
					{
    				int index = (int)(key.charAt(4) - '0');  // name*1*, etc.
    				char[] name = model.get("name", "Untitled Snd").toCharArray();
    				name[index - 1] = (char)(data.value);  // I hope!
    				model.set("name", new String(name));
					}
				else if (key.startsWith("lfo1frequency") || key.startsWith("lfo2frequency") || key.startsWith("lfo3frequency"))
					{
					int value = data.value;
					if (value >= 2410)
						value = (value - 2410) / 10 - 1 + (97 + 231);
					else if (value >= 100)
						value = ((value - 100) / 10) + 97;
					else
						value = value - 1;
					System.err.println("was " + data.value + " is " + value);
					model.set(key, value);
					}
				else
					{
					model.set(key, data.value);
					}
				}
			}
		}
        
    public static String getSynthName() { return "PreenFM2"; }
    
    public String getPatchName() { return model.get("name", "Untitled Snd"); }
    
    public HashMap parameterToIndex = new HashMap();
    public HashMap indexToParameter = new HashMap();

	public void addParameter(String name, int msb, int lsb)
		{
		Integer val = Integer.valueOf((msb << 7) | lsb);
		parameterToIndex.put(name, Integer.valueOf(val));
		indexToParameter.put(val, name);
		}
    
    public void buildParameterMap()	
		{
		addParameter("algorithm", 0, 0);
		addParameter("velocity", 0, 1);
		addParameter("voice", 0, 2);
		addParameter("glide", 0, 3);
		for(int i = 1; i < 6; i++)
			addParameter("im" + i, 0, 4 + (i-1)*2);
		for(int i = 1; i < 6; i++)
			addParameter("im" + i + "velocity", 0, 5 + (i-1)*2);
		for(int i = 1; i < 7; i++)
			addParameter("op" + i + "mix", 0, 16 + (i-1)*2);
		for(int i = 1; i < 7; i++)
			addParameter("op" + i + "pan", 0, 17 + (i-1)*2);
		addParameter("arpeggiatorclock", 0, 28);
		addParameter("arpeggiatorbpm", 0, 29);
		addParameter("arpeggiatordirection", 0, 30);
		addParameter("arpeggiatoroctave", 0, 31);
		addParameter("arpeggiatorpattern", 0, 32);
		addParameter("arpeggiatordivision", 0, 33);
		addParameter("arpeggiatorduration", 0, 34);
		addParameter("arpeggiatorlatch", 0, 35);
		addParameter("filtertype", 0, 40);
		addParameter("filterparam1", 0, 41);
		addParameter("filterparam2", 0, 42);
		addParameter("filtergain", 0, 43);
		for(int i = 1; i < 7; i++)
			addParameter("op" + i + "shape", 0, 44 + (i-1)*4);
		for(int i = 1; i < 7; i++)
			addParameter("op" + i + "freqtype", 0, 45 + (i-1)*4);
		for(int i = 1; i < 7; i++)
			addParameter("op" + i + "frequency", 0, 46 + (i-1)*4);
		for(int i = 1; i < 7; i++)
			addParameter("op" + i + "finetune", 0, 47 + (i-1)*4);
		for(int i = 1; i < 7; i++)
			addParameter("op" + i + "envattack", 0, 68 + (i-1)*8);
		for(int i = 1; i < 7; i++)
			addParameter("op" + i + "envattacklevel", 0, 69 + (i-1)*8);
		for(int i = 1; i < 7; i++)
			addParameter("op" + i + "envdecay", 0, 70 + (i-1)*8);
		for(int i = 1; i < 7; i++)
			addParameter("op" + i + "envdecaylevel", 0, 71 + (i-1)*8);
		for(int i = 1; i < 7; i++)
			addParameter("op" + i + "envsustain", 0, 72 + (i-1)*8);
		for(int i = 1; i < 7; i++)
			addParameter("op" + i + "envsustainlevel", 0, 73 + (i-1)*8);
		for(int i = 1; i < 7; i++)
			addParameter("op" + i + "envrelease", 0, 74 + (i-1)*8);
		for(int i = 1; i < 7; i++)
			addParameter("op" + i + "envreleaselevel", 0, 75 + (i-1)*8);
		for(int i = 1; i < 4; i++)
			addParameter("modulation" + i + "source", 0, 116 + (i-1)*4);
		for(int i = 1; i < 4; i++)
			addParameter("modulation" + i + "amount", 0, 117 + (i-1)*4);
		for(int i = 1; i < 4; i++)
			addParameter("modulation" + i + "destination", 0, 118 + (i-1)*4);
		for(int i = 4; i < 13; i++)
			addParameter("modulation" + i + "source", 1, 0 + (i-4)*4);
		for(int i = 4; i < 13; i++)
			addParameter("modulation" + i + "amount", 1, 1 + (i-4)*4);
		for(int i = 4; i < 13; i++)
			addParameter("modulation" + i + "destination", 1, 2 + (i-4)*4);
		for(int i = 1; i < 5; i++)
			addParameter("performanceparam" + i, 1, 36 + (i-1));
		for(int i = 1; i < 4; i++)
			addParameter("lfo" + i + "shape", 1, 40 + (i-1)*4);
		for(int i = 1; i < 4; i++)
			addParameter("lfo" + i + "frequency", 1, 41 + (i-1)*4);
		for(int i = 1; i < 4; i++)
			addParameter("lfo" + i + "bias", 1, 42 + (i-1)*4);
		for(int i = 1; i < 4; i++)
			addParameter("lfo" + i + "keysync", 1, 43 + (i-1)*4);
		for(int i = 1; i < 4; i++)
			addParameter("lfo" + i + "phase", 1, 68 + (i-1));	// I wonder if this is right
		addParameter("freeenv1attack", 1, 52);
		addParameter("freeenv1decay", 1, 53);
		addParameter("freeenv1sustain", 1, 54);
		addParameter("freeenv1release", 1, 55);
		addParameter("freeenv2silence", 1, 56);
		addParameter("freeenv2attack", 1, 57);
		addParameter("freeenv2decay", 1, 58);
		addParameter("freeenv2loop", 1, 59);
		for(int i = 1; i < 3; i++)
			addParameter("stepseq" + i + "bpm", 1, 60 + (i-1)*4);
		for(int i = 1; i < 3; i++)
			addParameter("stepseq" + i + "gate", 1, 61 + (i-1)*4);
		for(int i = 1; i < 13; i++)
			addParameter("name" + i, 1, 100 + i - 1);	// I wonder if this is right
		for(int i = 1; i < 17; i++)
			addParameter("stepseq1step" + i, 2, i);	// I wonder if this is right
		for(int i = 1; i < 17; i++)
			addParameter("stepseq2step" + i, 3, i);	// I wonder if this is right
		
		// Let's do a little verificaton here
		
		String[] keys = (String[])(parameterToIndex.keySet().toArray(new String[0]));
		for(int i = 0; i < keys.length; i++)
			{
			if (!model.exists((String)(keys[i])))
				System.err.println(keys[i] + "  missing from model");
			}
			
		keys = model.getKeys();
		for(int i = 0; i < keys.length; i++)
			{
			if (!parameterToIndex.containsKey(keys[i]))
				System.err.println(keys[i] + "  missing from parameter list");
			}
			
		}
    
    
    }
