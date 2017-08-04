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
    JComponent bottom;
        
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
        super.revalidate();
        }
                
    public void removeAll()
        {
        box.removeAll();
        if (bottom != null)
            remove(bottom);
        }               
                
    public void add(JComponent component)
        {
        box.add(component);
        }
    
    public void addBottom(JComponent component)
        {
        bottom = component;
        add(bottom, BorderLayout.SOUTH);
        }

    public int getCount()
        {
        return box.getComponentCount();
        }
                
    public void remove(int component)
        {
        box.remove(component);
        }        
        
    public void remove(JComponent component)
        {
        box.remove(component);
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
