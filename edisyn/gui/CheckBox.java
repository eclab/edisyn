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
   A wrapper for JCheckBox so that it updates itself in response to the model. 

   @author Sean Luke
*/

public class CheckBox extends NumericalComponent
    {
    JCheckBox check;
    boolean flipped;
    int width = 0;

    public void update(String key, Model model) 
        { 
        // we don't compare against min or max here because they
        // could be used by other widgets.  See for example Blofeld parameter 8
        if (flipped)
            check.setSelected(getState() == 0);
        else
            check.setSelected(getState() != 0); 
        }

    public CheckBox(String label, Synth synth, String key)
        {
        this(label, synth, key, false);
        }
                
    public void setWidth(int val)
    	{
    	width = val;
    	}
                
    public CheckBox(String label, Synth synth, String key, boolean flipped)
        {
        super(synth, key);

        this.flipped = flipped;
                
        check = new JCheckBox(label)
        	{
        	public Dimension getMinimumSize() 
        		{
        		if (width != 0)
        			{
        			Dimension d = super.getMinimumSize();
        			d.width += width;
        			return d;
        			} 
        		else return getPreferredSize(); 
        		}
        	};
        check.setFont(Style.SMALL_FONT);
        check.setBackground(Style.TRANSPARENT);
        check.setForeground(Style.TEXT_COLOR);

        setMax(1);
        setMin(0);
        setState(getState());
                
        setLayout(new BorderLayout());
        add(check, BorderLayout.CENTER);
                
        check.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                if (CheckBox.this.flipped)
                    setState(check.isSelected() ? getMin() : getMax());
                else
                    setState(check.isSelected() ? getMax() : getMin());                             
                }
            });
        }
    }
