/*
  Copyright 2020 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package edisyn.gui;

import edisyn.*;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.border.*;
import javax.swing.*;
import javax.swing.event.*;


public class LabelledSlider extends JPanel
	{
	public static final int SLOP = 32;
	JLabel label;
	JSlider slider;
	public LabelledSlider(int min, int max, int start)
		{
		JLabel sacrificial = new JLabel("" + max);
		label = new JLabel("" + start);
		label.setPreferredSize(sacrificial.getPreferredSize());
		slider = new JSlider(min, max, start)
			{
			public Dimension getPreferredSize()
				{
				Dimension d = super.getPreferredSize();
				if (d.width < minSliderWidth) d.width = minSliderWidth;
				return d;
				}
			};
		setMinSliderWidth(max - min + 1 + SLOP);
			
		setLayout(new BorderLayout());
		add(slider, BorderLayout.CENTER);
		add(label, BorderLayout.EAST);
		slider.addChangeListener(new ChangeListener()
            {
            public void stateChanged(ChangeEvent e) 
                {
                label.setText("" + slider.getValue());
            	}
            });
		}

	int minSliderWidth = -1;	
	public void setMinSliderWidth(int val)
		{
		minSliderWidth = val;
		}
	
	public int getValue() { return slider.getValue(); }
	}