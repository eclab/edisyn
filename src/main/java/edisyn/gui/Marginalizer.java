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


public class Marginalizer extends JComponent implements Gatherable
{
    JPanel panel = new JPanel();
        
    public Marginalizer()
    {
        setLayout(new BorderLayout());
        setBackground(Style.BACKGROUND_COLOR());
        panel.setBackground(Style.BACKGROUND_COLOR());
        add(panel, BorderLayout.CENTER);
    }
        
    public void addBottom(JComponent component)
    {
        add(component, BorderLayout.SOUTH);
    }
        
    public void addTop(JComponent component)
    {
        add(component, BorderLayout.NORTH);
    }
        
    public void addLeft(JComponent component)
    {
        add(component, BorderLayout.WEST);
    }
        
    public void addRight(JComponent component)
    {
        add(component, BorderLayout.EAST);
    }
        
    public void removeAll()
    {
        super.removeAll();
        add(panel, BorderLayout.CENTER);
    }               
                
    void gatherAllComponents(Container cont, java.util.ArrayList list)
    {
        Component[] c = cont.getComponents();
        for(int i = 0; i < c.length; i++)
            {
                list.add(c[i]);
                if (c[i] instanceof Gatherable)
                    ((Gatherable)c[i]).gatherAllComponents(list);
                else if (c[i] instanceof JPanel)
                    gatherAllComponents(((JPanel)c[i]), list);
                else if (c[i] instanceof Box)       // just in case
                    gatherAllComponents(((Box)c[i]), list);
            }
    }
        
    public void gatherAllComponents(java.util.ArrayList list)
    {
        gatherAllComponents(this, list);
    }
        
}
