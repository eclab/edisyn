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
   A container which lays widgets out vertically.  This is a wrapper for
   Box with built-in insets, and which automatically compresses (no glue),
   making it dead easy to use: just create one and add stuff to it and you're done.
   This is particularly useful because glue is broken for vertical boxes!
        
   @author Sean Luke
*/

public class VBox extends JComponent
    {
    Box box;
        
    public Insets getInsets() { return Style.VBOX_INSETS; }

    public VBox()
        {
        setLayout(new BorderLayout());
        setBackground(Style.BACKGROUND_COLOR);
        box = new Box(BoxLayout.Y_AXIS);
        add(box, BorderLayout.NORTH);
        }
        
    public void revalidate()
        {
        box.revalidate();
        }
                
    public void removeAll()
        {
        box.removeAll();
        }               
                
    public void add(JComponent component)
        {
        box.add(component);
        }
    }
