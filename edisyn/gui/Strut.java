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
        return makeStrut(space, 0);
        } 
                
    public static JComponent makeVerticalStrut(final int space)
        {
        return makeStrut(0, space);
        }

    public static JComponent makeStrut(final int width, final int height)
        {
        JPanel panel = new JPanel()
            {
            public Dimension getMinimumSize() { return new Dimension(width, height); }
            public Dimension getPreferredSize() { return new Dimension(width, height); }
            public Dimension getMaximumSize() { return new Dimension(width, height); }
            };
        panel.setBackground(Style.BACKGROUND_COLOR());
        return panel;
        } 
    
    public static JComponent makeStrut(Component[] components)
        {
        int maxWidth = 0;
        int maxHeight = 0;
        for(int i = 0; i < components.length; i++)
            {
            components[i].validate();
            Dimension size = components[i].getPreferredSize();
            if (maxWidth < size.width)
                maxWidth = size.width;
            if (maxHeight < size.height)
                maxHeight = size.height;
            }
        return makeStrut(maxWidth, maxHeight);
        }
        
    public static JComponent makeStrut(Component component)
        {
        return makeStrut(new Component[] { component });
        }

    }
