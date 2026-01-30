/***
    Copyright 2026 by Sean Luke
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
   A container which splits widgets horizontally with even space.
        
   @author Sean Luke
*/

public class HSplitBox extends JComponent implements Gatherable
    {
    JPanel panel = new JPanel();
    JComponent lastComponent;
        
    public Insets getInsets() { return Style.HBOX_INSETS(); }

    public HSplitBox(JComponent leftComponent, JComponent rightComponent)
        {
        setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.gridx = 0;
		constraints.gridy = 0;
		add(leftComponent, constraints);
		constraints.gridx = 1;
		add(rightComponent, constraints);
        setBackground(Style.BACKGROUND_COLOR());
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
        Component[] c = getComponents();
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
    }
