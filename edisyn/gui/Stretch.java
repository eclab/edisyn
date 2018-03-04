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
   A utility class for making simple variable-height or variable-width blobs
        
   @author Sean Luke
*/

public class Stretch
    {
    public static JComponent makeHorizontalStretch()
        {
        return _makeStretch(Integer.MAX_VALUE, 0);
        } 
                
    public static JComponent makeVerticalStretch()
        {
        return _makeStretch(0, Integer.MAX_VALUE);
        }

    public static JComponent makeStretch()
        {
        return _makeStretch(Integer.MAX_VALUE, Integer.MAX_VALUE);
        } 
    
    static JComponent _makeStretch(int horizontal, int vertical)
        {
        JPanel panel = new JPanel()
            {
            public Dimension getMinimumSize() { return new Dimension(0, 0); }
            public Dimension getPreferredSize() { return new Dimension(0, 0); }
            public Dimension getMaximumSize() { return new Dimension(horizontal, vertical); }
            };
        panel.setBackground(Style.BACKGROUND_COLOR());
        return panel;
        } 
    }
