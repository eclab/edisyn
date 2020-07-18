/***
    Copyright 2020 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.yamahafs1r;

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
   A patch editor for Yamaha FS1R Fseqs.
   
   @author Sean Luke
*/

public class YamahaFS1RFseq extends Synth
    {
    public static final String[] BANKS = new String[] { "Internal", "Preset" };
    public static final String[] LOOP_MODES = new String[] { "One Way", "Round" };
    public static final String[] PITCH_MODES = new String[] { "Pitch", "Non-Pitch" };
    public static final String[] FORMATS = new String[] { "128 Frames", "256 Frames", "384 Frames", "512 Frames" };
    public static final String[] NOTES = {"A", "A#", "B", "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#" };
    public static final String[] OPERATORS = {"1", "2", "3", "4", "5", "6", "7", "8"};

	Box hi = null;
	Box lo = null;

    public YamahaFS1RFseq()
        {
        model.set("number", 0);
        model.set("bank", 0);
                
        JComponent soundPanel = new SynthPanel(this);
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        hbox.addLast(addGlobal(Style.COLOR_A()));
        vbox.add(hbox);

		vbox.add(Strut.makeVerticalStrut(10));
		vbox.add(new Category(this, null, Style.COLOR_A()));
		vbox.add(Strut.makeVerticalStrut(10));
        vbox.addLast(addFrames(Style.COLOR_B(), Style.COLOR_A(), Style.COLOR_B()));
        
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Common", soundPanel);

        soundPanel = new SynthPanel(this);
		soundPanel.add(addFrameControls(), BorderLayout.NORTH);
		 lo = new Box(BoxLayout.Y_AXIS);
		lo.add(addFrameDisplay(1, true, Style.COLOR_A()));
		lo.add(addFrameDisplay(1, false, Style.COLOR_B()));
        soundPanel.add(lo, BorderLayout.CENTER);
        addTab("1-256", soundPanel);

        soundPanel = new SynthPanel(this);
		soundPanel.add(addFrameControls(), BorderLayout.NORTH);
		 hi = new Box(BoxLayout.Y_AXIS);
		hi.add(addFrameDisplay(257, true, Style.COLOR_B()));
		hi.add(addFrameDisplay(257, false, Style.COLOR_B()));
        soundPanel.add(hi, BorderLayout.CENTER);
        addTab("257-512", soundPanel);

        loadDefaults();        
        }
                
    public String getDefaultResourceFileName() { return "YamahaFS1RFseq.init"; }
    public String getHTMLResourceFileName() { return "YamahaFS1RFseq.html"; }

    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        JComboBox bank = new JComboBox(BANKS);
        bank.setSelectedIndex(model.get("bank"));
        if (writing)
            {
            bank = new JComboBox(new String[] { "Internal" });
            bank.setEnabled(false);
        	bank.setSelectedIndex(0);
            }
        bank.setEditable(false);
        bank.setMaximumRowCount(32);
                
        JTextField number = new JTextField("" + (model.get("number") + 1), 3);
                
        while(true)
            {
            boolean result = showMultiOption(this, new String[] { "Bank", "Patch Number"}, 
                new JComponent[] { bank, number }, title, "Enter the Bank and Patch number.");
                
            if (result == false) 
                return false;
                          
            int b = bank.getSelectedIndex();      
            int n;
            try { n = Integer.parseInt(number.getText()); }
            catch (NumberFormatException e)
                {
                showSimpleError(title, (b == 0 ? "For the Internal bank, the Patch Number must be an integer 1 ... 6" :
                									"For the Preset bank, the Patch Number must be an integer 1 ... 90"));
                continue;
                }
            if (n < 1 || (b == 0 && n > 6) || (b == 1 && n > 90))
                {
                showSimpleError(title, (b == 0 ? "For the Internal bank, the Patch Number must be an integer 1 ... 6" :
                									"For the Preset bank, the Patch Number must be an integer 1 ... 90"));
                continue;
                }
                                
            change.set("bank", bank.getSelectedIndex());
            change.set("number", n - 1);
                        
            return true;
            }
        }
        
    /* 
    	// These were the original functions Thor had dug up.  They're not inverses unfortunately:
    
    public static final double C1 = 181378.0422;
    public static final double C2 = 1.001344206;
    public double intToFrequency(int val)
    	{
    	int hi = (val >>> 7) & 127;
    	int lo = val & 127;
    	
    	return Math.pow(2, hi/4.0) / C1 * Math.pow(C2, lo);
    	}
    	
    public static final double K = 738.5;
    public static final double P0 = 8983.3;
    public static final double FIX = 1.0637;
    
    public int frequencyToInt(double frequency)
    	{
    	return (int)(K * Math.log(frequency / FIX) + P0);
    	}
    */
    	
    /// However this function is a close match for the intToFrequency above and is much simpler */
    public double intToFrequency(int val)
    	{
    	return Math.exp(-12.1104 + 0.0013538 * val);
    	}
    
    /// This is the inverse according to Mathematica
    public int frequencyToInt(double frequency)
    	{
    	return (int)(738.662 * Math.log(181752 * frequency));
    	}
    	
    /*
    	// On a log graph, the original intToFrequency() is a nearly perfect line going from -12.1083 ... 10.0697,
    	// which suggests some other very simple arrangements.  Such as just going from -12 to +10, which yields:
    	
        public double intToFrequency(int val)
    	{
    	return Math.exp(val/16384.0 * 22 - 12);
    	}

	// or perhaps Yamaha might actually just be going from -10 to +10, so we'd have:
    public double intToFrequency(int val)
    	{
    	return Math.exp(val/16384.0 * 20 - 10);
    	}
    */
    
    public String intToFrequencyString(int val)
    	{
    	double d = intToFrequency(val);
    	if (val == 0) return "0";
    	else if (d >= 10000)
    		{
    		return "" + (int)d;
    		}
    	else if (d >= 1000)
    		{
    		return String.format("%5.1f", d);
    		}
    	else if (d >= 100)
    		{
    		return String.format("%5.2f", d);
    		}
    	else if (d >= 10)
    		{
    		return String.format("%5.3f", d);
    		}
    	else if (d >= 1)
    		{
    		return String.format("%5.4f", d);
    		}
    	else if (d >= 0.01)
    		{
    		return String.format("%5.5f", d).substring(1);
    		}
    	else		// here we have a problem, the resolution is too small even for scientific notation, so we have to change the font size
    		{
    		return "<html><font size=-2>" + String.format("%4.2e", d) + "</font></html>";
    		}
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
        HBox hbox2 = new HBox();
        comp = new PatchDisplay(this, 8);
        hbox2.add(comp);
        vbox.add(hbox2);
        
        comp = new StringComponent("Patch Name", this, "name", 8, "Name must be up to 8 ASCII characters.")
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
        vbox.add(comp);  // doesn't work right :-(

        hbox.add(vbox);

        // Not enough space to show the title
        hbox.addLast(Strut.makeHorizontalStrut(80));

        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }


    public JComponent addFrame(int frame, Color color)
        {
        Category category = new Category(this, "Frame " + frame, color);

        JComponent comp;
        String[] params;
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        
        LabelledDial pitch = new LabelledDial("Pitch", this, "frame" + frame + "pitch", color, 0, 16383)
        	{
        	public String map(int val)
        		{
				return intToFrequencyString(val);
        		}
        	};
        comp = pitch;
        hbox.add(comp);

		for(int i = 1; i <= 8; i++)
			{
       		comp = new LabelledDial("V Freq " + i, this, "frame" + frame + "voicedfrequency" + i, color, 0, 16383)
        	{
        	public String map(int val)
        		{
				return intToFrequencyString(val);
        		}
        	};
			hbox.add(comp);
       		comp = new LabelledDial("V Level " + i, this, "frame" + frame + "voicedlevel" + i, color, 0, 127);
			hbox.add(comp);
			}
			
		vbox.add(hbox);
		hbox = new HBox();
			
		hbox.add(Strut.makeStrut(pitch));
		
		for(int i = 1; i <= 8; i++)
			{
       		comp = new LabelledDial("U Freq " + i, this, "frame" + frame + "unvoicedfrequency" + i, color, 0, 16383)
        	{
        	public String map(int val)
        		{
				return intToFrequencyString(val);
        		}
        	};
			hbox.add(comp);
       		comp = new LabelledDial("U Level " + i, this, "frame" + frame + "unvoicedlevel" + i, color, 0, 127);
			hbox.add(comp);
			}
			
		vbox.add(hbox);
        category.add(vbox, BorderLayout.CENTER);
        return category;
        }


	public static final int VERTICAL_SIZE = 500;

	public JComponent addFrames(Color color, Color colorA, Color colorB)
        {
        JComponent comp;
        String[] params;
        
        
    	final JComponent typical = addFrame(0, color);		// throwaway
    	final int h = typical.getPreferredSize().height;
    	final int w = typical.getPreferredSize().width;
    	
    	ScrollableVBox frames = new ScrollableVBox()
    		{
			public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
				{
				if (orientation == SwingConstants.VERTICAL)
					return w;
				else
					return h;
				}

			public Dimension getPreferredScrollableViewportSize()
				{
				Dimension size = getPreferredSize();
				size.height = h * 3;
				return size;
				}
    		};
    		
    	for(int i = 1; i <= 512; i++)
    		{
    		JComponent frame = addFrame(i, (i % 2 == 0 ? colorA : colorB));
    		frames.add(frame);
    		}


        JScrollPane pane = new JScrollPane(frames, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		pane.setBorder(null);
		
        return pane;
        }


	public JComponent addFrameControls()
		{
		String[] params;
        HBox hbox = new HBox();
		JComponent comp;
		
        params = OPERATORS;
        comp = new Chooser("Manipulate", this, "manipulate", params)
        	{
        	public void update(String key, Model model)
        		{
        		super.update(key, model);
        		// this should do the job
        		rebuildFrameDisplays();
        		}
        	};
        hbox.add(comp);
		model.setStatus("manipulate", Model.STATUS_IMMUTABLE);

		for(int i = 1; i <= 8; i++)
			{
			comp = new CheckBox("Show " + i, this, "show" + i)
				{
				public void update(String key, Model model)
					{
					super.update(key, model);
					// this should do the job
		        	rebuildFrameDisplays();
					}
				};
	        hbox.add(comp);
	        model.set("show" + i, 1);
			model.setStatus("show" + i, Model.STATUS_IMMUTABLE);
	        }

        PushButton button = new PushButton("Show All")
            {
            public void perform()
                {
                for(int i = 1; i <= 8; i++)
                	{
                	model.set("show" + i, 1);
                	}
                }
            };
        hbox.add(button);
        
     	button = new PushButton("Hide All")
            {
            public void perform()
                {
                for(int i = 1; i <= 8; i++)
                	{
                	model.set("show" + i, 0);
                	}
                }
            };
        hbox.add(button);
        
	    return hbox;
		}
 

    public EnvelopeDisplay buildFrequencyDisplay(int pos, boolean voiced, Color color)
        {
        EnvelopeDisplay parent = null;
        JComponent comp;
                	ArrayList<EnvelopeDisplay> kids = new ArrayList<EnvelopeDisplay>();
	

               double[] widths = new double[256];
            for(int i = 1; i < widths.length; i++)
                widths[i] = 1.0 / (256 - 1);

            double[] heights = new double[256];
            for(int i = 0; i < heights.length; i++)
                heights[i] = 1.0 / 16383;
                

     for(int o = 1; o <= 8; o++)
        	{
        	if (model.get("show" + o, 0) == 0 &&
        		model.get("manipulate", 0) + 1 != o)
        			continue;
        		
	        final String[] mods = new String[256];
        for(int i = 0; i < mods.length; i++)
        	{
        	mods[i] = "frame" + (i + pos) + (voiced ? "" : "un") + "voicedfrequency" + o;
        	}
        	        	
        final int _o = o;
        
	comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), new String[256], mods, widths, heights)
                {
                // The mouseDown and mouseUp code here enables us to only do undo()
                // ONCE.
                public void mouseDown()
                    {
                    getUndo().push(getModel());
                    getUndo().setWillPush(false);
                    }
                
                public void mouseUp()
                    {
                    getUndo().setWillPush(true);
                    }
                        
                public void updateFromMouse(double x, double y, boolean continuation)
                    {
                    if (x < 0)
                        x = 0;
                    else if (x > 1)
                        x = 1.0;

                    if (y <= 0.0) y = 0.0;
                    if (y >= 1.0) y = 1.0;
                    int frame = (int)(x * (256.0 - 1) + 0.5);

                    double val = y * 16383;
                                                                        
                    YamahaFS1RFseq.this.model.set("frame" + (frame + pos) + (voiced ? "" : "un") + "voicedfrequency" + _o, (int)val);
                    }

                public int highlightIndex(double x, double y, boolean continuation)
                    {
                    if (x < 0) x = 0;
                    if (x > 1.0) x = 1.0;
                    return (int)(x * (256.0 - 1) + 0.5);
                    }

                public int verticalBorderThickness() { return 4; }
                };
            ((EnvelopeDisplay)comp).addVerticalDivider(0.5);
            ((EnvelopeDisplay)comp).setPreferredHeight(180);
			
			if (model.get("manipulate", 0) + 1 == o)
				{
				parent = (EnvelopeDisplay)comp;
				}
			else
				{
				kids.add((EnvelopeDisplay)comp);
				}

			((EnvelopeDisplay)comp).setFilled(false);
			}
		
		for(int i = 0; i < kids.size(); i++)
			{
			parent.link(kids.get(i));
			}
		
		return parent;
		}
		
		
    public EnvelopeDisplay buildLevelDisplay(int pos, boolean voiced, Color color)
        {
        EnvelopeDisplay parent = null;
        JComponent comp;
                	ArrayList<EnvelopeDisplay> kids = new ArrayList<EnvelopeDisplay>();

               double[] widths = new double[256];
            for(int i = 1; i < widths.length; i++)
                widths[i] = 1.0 / (256 - 1);

            double[] heights2 = new double[256];
            for(int i = 0; i < heights2.length; i++)
                heights2[i] = 1.0 / 127;

     for(int o = 1; o <= 8; o++)
        	{
        	if (model.get("show" + o, 0) == 0 &&
        		model.get("manipulate", 0) + 1 != o)
        			continue;
        		
        String[] mods2 = new String[256];
        for(int i = 0; i < mods2.length; i++)
        	{
        	mods2[i] = "frame" + (i + pos) + (voiced ? "" : "un") + "voicedlevel" + o;
        	}
        final int _o = o;

		comp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), new String[256], mods2, widths, heights2)
                {
                // The mouseDown and mouseUp code here enables us to only do undo()
                // ONCE.
                public void mouseDown()
                    {
                    getUndo().push(getModel());
                    getUndo().setWillPush(false);
                    }
                
                public void mouseUp()
                    {
                    getUndo().setWillPush(true);
                    }
                        
                public void updateFromMouse(double x, double y, boolean continuation)
                    {
                    if (x < 0)
                        x = 0;
                    else if (x > 1)
                        x = 1.0;

                    if (y <= 0.0) y = 0.0;
                    if (y >= 1.0) y = 1.0;
                    int frame = (int)(x * (256.0 - 1) + 0.5);

                    double val = y * 127;
                                                                        
                    YamahaFS1RFseq.this.model.set("frame" + (frame + pos) + (voiced ? "" : "un") + "voicedlevel" + _o, (int)val);
                    }

                public int highlightIndex(double x, double y, boolean continuation)
                    {
                    if (x < 0) x = 0;
                    if (x > 1.0) x = 1.0;
                    return (int)(x * (256.0 - 1) + 0.5);
                    }

                public int verticalBorderThickness() { return 4; }
                };
            ((EnvelopeDisplay)comp).addVerticalDivider(0.5);
            ((EnvelopeDisplay)comp).setPreferredHeight(60);

			if (model.get("manipulate", 0) + 1 == o)
				{
				parent = (EnvelopeDisplay)comp;
				}
			else
				{
				kids.add((EnvelopeDisplay)comp);
				}

			((EnvelopeDisplay)comp).setFilled(false);
			}

		for(int i = 0; i < kids.size(); i++)
			{
			parent.link(kids.get(i));
			}
		
		return parent;
		}


    public JComponent addFrameDisplay(int pos, boolean voiced, Color color)
        {
        Category category = new Category(this, (voiced ? "Voiced " : "Unvoiced ") + pos + " - " + (pos + 255), color);

        JComponent comp;
        String[] params;
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        
        JPanel panel = new JPanel();
        panel.setBackground(Style.BACKGROUND_COLOR());
        panel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.fill = c.BOTH;
        c.weightx = 1;
        c.weighty = 3;
        
		panel.add(buildFrequencyDisplay(pos, voiced, color), c);

			c.weighty = 1;
			c.gridy = 1;

		panel.add(buildLevelDisplay(pos, voiced, color), c);
            
        category.add(panel, BorderLayout.CENTER);
        return category;
        }
        
        
    public void rebuildFrameDisplays()
    	{
    	if (hi == null || lo == null)  // not ready yet
    		return;
    		
    	lo.removeAll();
    	lo.add(addFrameDisplay(1, true, Style.COLOR_A()));
    	lo.add(addFrameDisplay(1, false, Style.COLOR_B()));
    	lo.revalidate();
    	lo.repaint();
    	hi.removeAll();
    	hi.add(addFrameDisplay(257, true, Style.COLOR_A()));
    	hi.add(addFrameDisplay(257, false, Style.COLOR_B()));
    	hi.revalidate();
    	hi.repaint();
    	
    	// unregister the envelopes
    	for(int op = 1; op <= 8; op++)
    		{
    		ArrayList listeners = model.getListeners("frame1voicedfrequency" + op);
	    	for(int i = listeners.size() - 1; i >= 0; i--)
	    		{
	    		if (listeners.get(i) instanceof EnvelopeDisplay)
	    			listeners.remove(i);
	    		}
	    		
    		 listeners = model.getListeners("frame1voicedlevel" + op);
	    	for(int i = listeners.size() - 1; i >= 0; i--)
	    		{
	    		if (listeners.get(i) instanceof EnvelopeDisplay)
	    			listeners.remove(i);
	    		}
	    		
    		 listeners = model.getListeners("frame257voicedfrequency" + op);
	    	for(int i = listeners.size() - 1; i >= 0; i--)
	    		{
	    		if (listeners.get(i) instanceof EnvelopeDisplay)
	    			listeners.remove(i);
	    		}
	    		
    		 listeners = model.getListeners("frame257voicedlevel" + op);
	    	for(int i = listeners.size() - 1; i >= 0; i--)
	    		{
	    		if (listeners.get(i) instanceof EnvelopeDisplay)
	    			listeners.remove(i);
	    		}
	    	}
    	}



    public JComponent addGlobal( Color color)
        {
        Category category = new Category(this, "Global", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
                
        params = FORMATS;
        comp = new Chooser("Format", this, "format", params);
        vbox.add(comp);
        
        hbox.add(vbox);
        vbox = new VBox();

        params = LOOP_MODES;
        comp = new Chooser("Loop Mode", this, "loopmode", params);
        vbox.add(comp);

        params = PITCH_MODES;
        comp = new Chooser("Pitch Mode", this, "pitchmode", params);
        vbox.add(comp);

        hbox.add(vbox);

        comp = new LabelledDial("Length", this, "endstep", color, 0, 511, -1);
        hbox.add(comp);

        comp = new LabelledDial("Loop", this, "loopstart", color, 0, 511, -1);
        ((LabelledDial)comp).addAdditionalLabel("Start");
        hbox.add(comp);

        comp = new LabelledDial("Loop", this, "loopend", color, 0, 511, -1);
        ((LabelledDial)comp).addAdditionalLabel("End");
        hbox.add(comp);
        
        comp = new LabelledDial("Speed", this, "speedadjust", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Adjust");
        hbox.add(comp);

        comp = new LabelledDial("Velocity Sens.", this, "velocityensitivityfortempo", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("For Tempo");
        hbox.add(comp);

        comp = new LabelledDial("Note", this, "noteassign", color, 0, 127)
            {
            public String map(int val)
                {
                // not sure if this is right.  Needs to start at C-2
                return (NOTES[(val + 3) % 12] + (((val + 9) / 12) - 2));
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Pitch", this, "pitchtuning", color, 0, 126, 63)
        	{
        	public boolean isSymmetric() { return true; }
        	};
        ((LabelledDial)comp).addAdditionalLabel("Tuning");
        hbox.add(comp);

        comp = new LabelledDial("Sequence", this, "sequencedelay", color, 0, 63);
        ((LabelledDial)comp).addAdditionalLabel("Delay");
        hbox.add(comp);
       
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public byte[] emit(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        if (tempModel == null)
            tempModel = getModel();
            
        int format = model.get("format");
        int byteCount = 32 + 50 * (format == 0 ? 128 : (format == 1 ? 256 : (format == 2 ? 384 : 512)));
        int val = 0;
        
        byte[] data = new byte[byteCount + 11];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x43;
        data[2] = (byte)(getID() - 1);
        data[3] = (byte)0x5E;
        data[4] = (byte)((byteCount >>> 7) & 127);
        data[5] = (byte)(byteCount & 127);
        data[6] = (byte)(toWorkingMemory ? 0x60 : 0x61);
        data[7] = (byte)0x0;
        data[8] = (byte)(toWorkingMemory ? 0x00 : tempModel.get("number"));
        
        int pos = 9;
        
        String name = model.get("name", "INIT VOICE") + "          ";
        for(int i = 0; i < 8; i++)     
            {
            data[i + pos] = (byte)(name.charAt(i));
            }
        pos += 8;
        
        // "reserved"
    	pos += 8;
    	
    	val = model.get("loopstart");
    	data[pos] = (byte)((val >>> 7) & 127);
    	data[pos+1] = (byte)(val & 127);
    	pos += 2;
        
    	val = model.get("loopend");
    	data[pos] = (byte)((val >>> 7) & 127);
    	data[pos+1] = (byte)(val & 127);
    	pos += 2;
        
    	val = model.get("loopmode");
    	data[pos] = (byte)val;
    	pos += 1;

    	val = model.get("speedadjust");
    	data[pos] = (byte)val;
    	pos += 1;

    	val = model.get("velocityensitivityfortempo");
    	data[pos] = (byte)val;
    	pos += 1;

    	val = model.get("pitchmode");
    	data[pos] = (byte)val;
    	pos += 1;

    	val = model.get("noteassign");
    	data[pos] = (byte)val;
    	pos += 1;

    	val = model.get("pitchtuning");
    	data[pos] = (byte)val;
    	pos += 1;

    	val = model.get("sequencedelay");
    	data[pos] = (byte)val;
    	pos += 1;

    	val = model.get("format");
    	data[pos] = (byte)val;
    	pos += 1;
    	
    	// "reserved"
    	pos += 2;

    	val = model.get("endstep");
    	data[pos] = (byte)((val >>> 7) & 127);
    	data[pos+1] = (byte)(val & 127);
    	pos += 2;
    	
    	for(int i = 0; i < (format + 1) * 128; i++)
    		{
			val = model.get("frame" + (i + 1) + "pitch");
    	data[pos] = (byte)((val >>> 7) & 127);
    	data[pos+1] = (byte)(val & 127);
			pos += 2;
			
			for(int j = 1; j <= 8; j++)
				{
				val = model.get("frame" + (i + 1) + "voicedfrequency" + j);
    	data[pos] = (byte)((val >>> 7) & 127);
    	data[pos+1] = (byte)(val & 127);
				pos += 2;
				}
			
			for(int j = 1; j <= 8; j++)
				{
				// Thor says that this is inverted
				val = 127 - model.get("frame" + (i + 1) + "voicedlevel" + j);
    	data[pos] = (byte)val;
				pos += 1;
				}

			for(int j = 1; j <= 8; j++)
				{
				val = model.get("frame" + (i + 1) + "unvoicedfrequency" + j);
    	data[pos] = (byte)((val >>> 7) & 127);
    	data[pos+1] = (byte)(val & 127);
				pos += 2;
				}
			
			for(int j = 1; j <= 8; j++)
				{
				// Thor says that this is inverted
				val = 127 - model.get("frame" + (i + 1) + "unvoicedlevel" + j);
    	data[pos] = (byte)val;
				pos += 1;
				}
    		}
                
        data[data.length - 2] = produceChecksum(data, 4);
        data[data.length - 1] = (byte)0xF7;
        return data;
        }
    
    
    public int parse(byte[] data, boolean fromFile)
        {
        // dunno how to load the bank or number yet
		int pos = 9;
		                
        char[] name = new char[8];
        for(int i = 0; i < 8; i++)
            {
            name[i] = (char)data[i + pos];
            model.set("name", new String(name));
            }
        pos += 8;
        
        // "reserved"
    	pos += 8;
    	
    	model.set("loopstart", (data[pos] << 7) | data[pos+1]);
    	pos += 2;
        
    	model.set("loopend", (data[pos] << 7) | data[pos+1]);
    	pos += 2;
        
    	model.set("loopmode", data[pos]);
    	pos += 1;

    	model.set("speedadjust", data[pos]);
    	pos += 1;

    	model.set("velocityensitivityfortempo", data[pos]);
    	pos += 1;

    	model.set("pitchmode", data[pos]);
    	pos += 1;

    	model.set("noteassign", data[pos]);
    	pos += 1;

    	model.set("pitchtuning", data[pos]);
    	pos += 1;

    	model.set("sequencedelay", data[pos]);
    	pos += 1;

    	int format = data[pos];
    	model.set("format", format);
    	pos += 1;
    	
    	// "reserved"
    	pos += 2;

    	model.set("endstep", (data[pos] << 7) | data[pos+1]);
    	pos += 2;
    	
    	//// Clear frames first
    	for(int i = 0; i < 512; i++)
    		{
			model.set("frame" + (i + 1) + "pitch", 0);
			for(int j = 1; j <= 8; j++)
				{
				model.set("frame" + (i + 1) + "voicedfrequency" + j, 0);
				}
			
			for(int j = 1; j <= 8; j++)
				{
				model.set("frame" + (i + 1) + "voicedlevel" + j, 0);
				}

			for(int j = 1; j <= 8; j++)
				{
				model.set("frame" + (i + 1) + "unvoicedfrequency" + j, 0);
				}
			
			for(int j = 1; j <= 8; j++)
				{
				model.set("frame" + (i + 1) + "unvoicedlevel" + j, 0);
				}
    		}

		// Now load
    	for(int i = 0; i < (format + 1) * 128; i++)
    		{
			if (pos >= data.length - 2) // uh, 
				{
				System.err.println("Warning: truncated Fseq file");
				break;
				}
				
			model.set("frame" + (i + 1) + "pitch", (data[pos] << 7) | data[pos+1]);
			pos += 2;
			
			for(int j = 1; j <= 8; j++)
				{
				model.set("frame" + (i + 1) + "voicedfrequency" + j, (data[pos] << 7) | data[pos+1]);
				pos += 2;
				}
			
			for(int j = 1; j <= 8; j++)
				{
				// Thor says that this is inverted
				model.set("frame" + (i + 1) + "voicedlevel" + j, 127 - data[pos]);
				pos += 1;
				}

			for(int j = 1; j <= 8; j++)
				{
				model.set("frame" + (i + 1) + "unvoicedfrequency" + j, (data[pos] << 7) | data[pos+1]);
				pos += 2;
				}
			
			for(int j = 1; j <= 8; j++)
				{
				// Thor says that this is inverted
				model.set("frame" + (i + 1) + "unvoicedlevel" + j, 127 - data[pos]);
				pos += 1;
				}
    		}
    	
    	if (pos != data.length - 2)
    		{
    		System.err.println("Warning: overlong Fseq file");
    		}
        
        revise();
        return PARSE_SUCCEEDED_UNTITLED;
        }
        
        
    public byte getID() 
        { 
        try 
            { 
            byte b = (byte)(Byte.parseByte(tuple.id));
            if (b >= 1 && b < 16) return b;
            }
        catch (NullPointerException e) { } // expected.  Happens when tuple's not built yet
        catch (NumberFormatException e) { e.printStackTrace(); }
        return 1;
        }
        
    public String reviseID(String id)
        {
        try 
            { 
            byte b =(byte)(Byte.parseByte(id)); 
            if (b >= 1 && b < 16) return "" + b;
            } 
        catch (NumberFormatException e) { }             // expected
        return "" + getID();
        }


    byte produceChecksum(byte[] bytes, int start)
        {
        //      The TX816 owner's manual (p. 54) says the checksum is:
        //                              "the least significant 7 bits of the 2's complement sum of 155 data bytes.
        //                               0eeeeeee must be determined so that the least significant 7 bits of the
        //                              sum of the 155 data bytes and checksum equal zero."
        //
        //              The FS1R manual says "Check-sum is a value that makes "0" (zero) in lower 7 bits of an added value 
        //                                                              of Byte Count, Address, Data, and Check-sum itself"
                
        int checksum = 0;
        for(int i = start; i < bytes.length; i++)
            checksum = (checksum + bytes[i]) & 127;
        return (byte)((128 - checksum) & 127);
        }

    public void performRequestDump(Model tempModel, boolean changePatch)
        {
        if (tempModel == null)
            tempModel = getModel();

        // We ALWAYS change the patch no matter what.  We have to.  We have to force it for merging
        changePatch(tempModel);
        tryToSendSysex(requestDump(tempModel));
        }

    public byte[] requestDump(Model tempModel) 
        {
        // since performRequestDump ALWAYS changes the patch, we might
        // as well just call requestCurrentDump() here 
        return requestCurrentDump(); 
        }
    
    // Will request the current part
    public byte[] requestCurrentDump()
        {
        return new byte[]
            {
            (byte)0xF0,
            (byte)0x43,
            (byte)(32 + getID() - 1),
            (byte)0x5E,
            (byte)(0x60),
            0, 
            0, 
            (byte)0xF7
            };
        }

    public static boolean recognize(byte[] data)
        {
        return ((
        	(data.length == 11 + 32 + 50 * 128) ||
        	(data.length == 11 + 32 + 50 * 256) ||
        	(data.length == 11 + 32 + 50 * 384) ||
        	(data.length == 11 + 32 + 50 * 512)) &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x43 &&
            data[3] == (byte)0x5E);
        }
               
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
        
    public static String getSynthName() { return "Yamaha FS1R [Fseq]"; }

    public void changePatch(Model tempModel) 
        {
		// bank
        tryToSendSysex(new byte[] { (byte)0xf0, 0x43, (byte)(16 + (getID() - 1)), 0x5e, 0x10, 0x00, 0x16, 0x00, (byte)tempModel.get("bank"), (byte)0xf7});

		// number
        tryToSendSysex(new byte[] { (byte)0xf0, 0x43, (byte)(16 + (getID() - 1)), 0x5e, 0x10, 0x00, 0x17, 0x00, (byte)tempModel.get("number"), (byte)0xf7});
        
        // we assume that we successfully did it
        if (!isMerging())  // we're actually loading the patch, not merging with it
            {
            setSendMIDI(false);
            model.set("number", tempModel.get("number"));
            model.set("bank", tempModel.get("bank"));
            setSendMIDI(true);
            }
        }
    
    public String getPatchName(Model model) { return model.get("name", "Init"); }

    public Model getNextPatchLocation(Model model)
        {
        int bank = model.get("bank");
        int number = model.get("number");
        
        number++;
        if (number >= 128)
            {
            bank++;
            number = 0;
            if (bank >= 13)             // K = 12, Internal = 0
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
        int bank = model.get("bank");
        return (BANKS[model.get("bank")]) + 
            (number > 99 ? "" : (number > 9 ? "0" : "00")) + number;
        }
        

    public boolean testVerify(Synth synth2, 
        String key,
        Object obj1, Object obj2) 
        {
        return false;
        }

    // Writing takes a while to process.  However the FS1R has a huge buffer and can handle an entire
    // bank's worth of writes -- but then it's very slow to process through all of them, constantly displaying
    // "Bulk Received".  With about a 170ms delay or so, this message disappears right when Edisyn finishes,
    // so it's a good compromise from a UI standpoint.
    public int getPauseAfterWritePatch() { return 170; }            // don't know if we need any
    }


class ScrollableVBox extends VBox implements javax.swing.Scrollable
	{
	public Dimension getPreferredScrollableViewportSize()
		{
		return null;
		}
		
	// for now we're not doing a snap to the nearest category
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
		{
		if (orientation == SwingConstants.VERTICAL)
			return 1;
		else
			return 1;
		}

	public boolean getScrollableTracksViewportHeight()
		{
		return false;
		}

	public boolean getScrollableTracksViewportWidth()
		{
		return true;
		}
	
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction)
		{
		return 1;
		}
	}
