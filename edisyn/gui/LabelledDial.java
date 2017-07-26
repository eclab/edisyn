/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.gui;

import edisyn.*;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.border.*;
import javax.swing.*;
import java.awt.event.*;

/**
   A labelled dial which the user can modify with the mouse.
   The dial updates the model and changes in response to it.
   For an unlabelled dial, see Dial.
        
   You can add a second label (or in fact, though it's not obvious,
   additional labels!)

   @author Sean Luke
*/

public class LabelledDial extends NumericalComponent implements DialMap
    {
    Dial dial;
    JLabel label;
    Box labelBox;
    Component glue;
        
    public Insets getInsets() { return Style.LABELLED_DIAL_INSETS; }

    public int getMin() { return dial.getMin(); }
    public int getMax() { return dial.getMax(); }
    public int getState() { return dial.getState(); }
    public void setMin(int val) { dial.setMin(val); }
    public void setMax(int val) { dial.setMax(val); }
    public void setState(int val) { dial.setState(val); }
    public void update(String key, Model model) { dial.update(key, model); }
        
    public void setLabel(String text)
    	{
    	label.setText(text);
    	label.revalidate();
    	label.repaint();
    	}
    	
    // Turn off registration for LabelledDial.
    // That way we can call update()
    // manually on LabelledDial, but update() won't get called
    // automatically on it.  See addLFO(...)
    public void register(String key)
        {
        // do nothing
        }

    public String map(int val) { return "" + (val - dial.subtractForDisplay); }

    public boolean isSymmetric() { return dial.getCanonicalSymmetric(); }
    
    public double getStartAngle() { return dial.getCanonicalStartAngle(); }

    /** Adds a second (or third or fourth or more!) label to the dial, to allow
        for multiline labels. */
    public JLabel setSecondLabel(String _label)
        {
        JLabel label2 = new JLabel(_label);
                
        label2.setFont(Style.SMALL_FONT);
        label2.setBackground(Style.TRANSPARENT);
        label2.setForeground(Style.TEXT_COLOR);

        Box box = new Box(BoxLayout.X_AXIS);
        box.add(Box.createGlue());
        box.add(label2);
        box.add(Box.createGlue());
                
        labelBox.remove(glue);
        labelBox.add(box);
        labelBox.add(glue = Box.createGlue());

        revalidate();
        repaint();
        return label2;
        }
                
    /** Makes a labelled dial for the given key parameter on the given synth, and with the given color and
        minimum and maximum.  Prior to display, subtractForDisplay is 
        SUBTRACTED from the parameter value.  You can use this to convert 0...127 in the model
        to -64...63 on-screen, for example.  */
    public LabelledDial(String _label, Synth synth, String key, Color staticColor, int min, int max, int subtractForDisplay)
        {
        this(_label, synth, key, staticColor, min, max);
        dial.subtractForDisplay = subtractForDisplay;
        dial.update(key, synth.getModel());
        repaint();
        }

    /** Makes a labelled dial for the given key parameter on the given synth, and with the given color and
        minimum and maximum. */
    public LabelledDial(String _label, Synth synth, String key, Color staticColor, int min, int max)
        {
        this(_label, synth, key, staticColor);
        setMin(min);
        setMax(max);
        synth.getModel().setMetricMin(key, min);
        synth.getModel().setMetricMax(key, max);
        }

    /** Makes a labelled dial for the given key parameter on the given synth, and with the given color.  No minimum or maximum is set. */
    public LabelledDial(String _label, Synth synth, String key, Color staticColor)
        {
        super(synth, key);
        setBackground(Style.BACKGROUND_COLOR);
        dial = new Dial(synth, key, staticColor);
        dial.setDialMap(this);

        label = new JLabel(_label);
                
        label.setFont(Style.SMALL_FONT);
        label.setBackground(Style.TRANSPARENT);
        label.setForeground(Style.TEXT_COLOR);

        labelBox = new Box(BoxLayout.Y_AXIS);
        Box box = new Box(BoxLayout.X_AXIS);
        box.add(Box.createGlue());
        box.add(label);
        box.add(Box.createGlue());
        labelBox.add(box);
        labelBox.add(glue = Box.createGlue());

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Style.BACKGROUND_COLOR);
        panel.add(dial, BorderLayout.CENTER);
        panel.add(labelBox, BorderLayout.SOUTH);
        
        setLayout(new BorderLayout());
        add(panel, BorderLayout.NORTH);
        setState(getState());
        }

	public int getDefaultValue()
		{
		if (isSymmetric())
			{
			return (int)Math.ceil((getMin() + getMax()) / 2.0);		// we do ceiling so we push to 64 on 0...127
			}
		else return getMin();
		}

    }
