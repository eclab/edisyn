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
   A container which lays widgets out horizontally.  This is a wrapper for
   Box with built-in insets, and which automatically compresses (no glue),
   making it dead easy to use: just create one and add stuff to it and you're done.
   
   <p>If you want the box to expand the rightmost component to fill all remaining space,
   add the component using the addLast() method.
        
   @author Sean Luke
*/

public class HBox extends JComponent implements Gatherable
    {
    Box box;
    JPanel panel = new JPanel();
    JComponent lastComponent;
        
    public Insets getInsets() { return Style.HBOX_INSETS(); }

    public HBox()
        {
        this(RIGHT_CONSUMES);
        }
    
    public static final int LEFT_CONSUMES = 0;
    public static final int RIGHT_CONSUMES = 1;
    public HBox(int alternative)
        {
        setLayout(new BorderLayout());
        setBackground(Style.BACKGROUND_COLOR());
        box = new Box(BoxLayout.X_AXIS);
        if (alternative == LEFT_CONSUMES)
            add(box, BorderLayout.EAST);
        else
            add(box, BorderLayout.WEST);            
        panel.setLayout(new BorderLayout());
        panel.setBackground(Style.BACKGROUND_COLOR());
        add(panel, BorderLayout.CENTER);
        }
    
    public void setBackground(Color color)
        {
        panel.setBackground(color);
        super.setBackground(color);
        }
        
    public void addLast(JComponent component)
        {
        lastComponent = component;
        panel.add(lastComponent, BorderLayout.CENTER);
        }

    public void removeLast()
        {
        if (lastComponent != null)
            panel.remove(lastComponent);
        lastComponent = null;
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
        if (lastComponent != null)
            {
            list.add(lastComponent);
            if (lastComponent instanceof Gatherable)
                ((Gatherable)lastComponent).gatherAllComponents(list);
            }                       
        }
    }
