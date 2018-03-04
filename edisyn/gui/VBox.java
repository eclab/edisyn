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

public class VBox extends JComponent implements Gatherable
    {
    Box box;
    JPanel panel = new JPanel();
    JComponent bottom;
        
    public Insets getInsets() { return Style.VBOX_INSETS(); }

    public VBox()
        {
        setLayout(new BorderLayout());
        setBackground(Style.BACKGROUND_COLOR());
        box = new Box(BoxLayout.Y_AXIS);
        add(box, BorderLayout.NORTH);
        panel.setLayout(new BorderLayout());
        panel.setBackground(Style.BACKGROUND_COLOR());
        add(panel, BorderLayout.CENTER);
        }
        
    public void addBottom(JComponent component)
        {
        addBottom(component, false);
        }
        
    public void addBottom(JComponent component, boolean stretch)
        {
        bottom = component;
        if (stretch)
            panel.add(bottom, BorderLayout.CENTER);
        else
            panel.add(bottom, BorderLayout.CENTER);
        }

    public void removeBottom()
        {
        if (bottom != null)
            panel.remove(bottom);
        bottom = null;
        }
        
    public void revalidate()
        {
        panel.revalidate();
        box.revalidate();
        super.revalidate();
        }
                
    public void removeAll()
        {
        box.removeAll();
        }               
                
    public int getCount()
        {
        return box.getComponentCount();
        }
                
    public void remove(int component)
        {
        box.remove(component);
        }        
        
    public void remove(Component component)
        {
        box.remove(component);
        }   
        
    public Component add(Component component)
        {
        return box.add(component);
        }
    
    public void gatherAllComponents(java.util.ArrayList list)
        {
        Component[] c = box.getComponents();
        for(int i = 0; i < c.length; i++)
            {
            list.add(c[i]);
            if (c[i] instanceof Gatherable)
                ((Gatherable)c[i]).gatherAllComponents(list);
            }  
                        
        if (bottom != null)
            {                       
            list.add(bottom);
            if (bottom instanceof Gatherable)
                ((Gatherable)bottom).gatherAllComponents(list);
            }               
        }
        
    }
