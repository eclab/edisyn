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


public class Grid extends JComponent implements Gatherable
    {
    public Insets getInsets() { return Style.HBOX_INSETS(); }

    public Grid(int x, int y)
        {
        setLayout(new GridLayout(y, x));  // gridlayout is backwards!
        setBackground(Style.BACKGROUND_COLOR());
        }

    public void gatherAllComponents(java.util.ArrayList list)
        {
        Component[] c = getComponents();
        for(int i = 0; i < c.length; i++)
            {
            list.add(c[i]);
            if (c[i] instanceof Gatherable)
                ((Gatherable)c[i]).gatherAllComponents(list);
            }                       
        }
    }
