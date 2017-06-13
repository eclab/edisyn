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
	A utility class for making simple fixed-height or fixed-width blobs
        
   @author Sean Luke
*/

public class Strut
    {
    public static JComponent makeHorizontalStrut(final int space)
    	{
    	JPanel panel = new JPanel()
			{
			public Dimension getMinimumSize() { return new Dimension(space,0); }
			public Dimension getPreferredSize() { return new Dimension(space,0); }
			public Dimension getMaximumSize() { return new Dimension(space,0); }
			};
		panel.setBackground(Style.BACKGROUND_COLOR);
		return panel;
		} 
		
    public static JComponent makeVerticalStrut(final int space)
    	{
    	JPanel panel = new JPanel()
			{
			public Dimension getMinimumSize() { return new Dimension(0,space); }
			public Dimension getPreferredSize() { return new Dimension(0,space); }
			public Dimension getMaximumSize() { return new Dimension(0,space); }
			};
		panel.setBackground(Style.BACKGROUND_COLOR);
		return panel;
		}               

    }
