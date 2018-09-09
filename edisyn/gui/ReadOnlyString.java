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
   A simple widget maintains a string value in the model.  This widget appears
   as a LABEL with the string value on it.

   @author Sean Luke
*/

public class ReadOnlyString extends JPanel implements Updatable
    {
    JLabel text;
        
    String key;
    Synth synth;
    
    public String getKey() { return key; }

    static String buildInitialString(int columns)
        {
        String s = "";
        for(int i = 0; i < columns; i++)
            s = s + "M";
        return s;
        }

    public ReadOnlyString(final String label, final Synth synth, final String key, final int columns)
        {
        this.synth = synth;
        Model model = synth.getModel();
        setBackground(Style.BACKGROUND_COLOR());
        setLayout(new BorderLayout());
        setBorder(Style.PATCH_BORDER());        
                
        final Dimension[] dim = new Dimension[1];
        text = new JLabel(buildInitialString(columns))
            {
            public Dimension getMinimumSize() { return getPreferredSize(); }
            public Dimension getMaximumSize() { return getPreferredSize(); }
            public Dimension getPreferredSize() 
                { if (dim[0] == null) 
                    { return super.getPreferredSize(); }
                else
                    { return dim[0]; }
                }
            };
                
        text.setFont(Style.MEDIUM_FONT());
        text.setText(buildInitialString(columns));
        text.setBackground(Style.BACKGROUND_COLOR());
        text.setForeground(Style.TEXT_COLOR());
        // lock the preferred size to max of columns
        dim[0] = text.getPreferredSize();
    
        String name = synth.getPatchLocationName(synth.getModel());
        if (name == null) name = "";
        text.setText(name);
        text.repaint();
        add(text, BorderLayout.CENTER);
        
        JLabel lab = new JLabel(label, SwingConstants.LEFT);
        lab.setFont(Style.SMALL_FONT());
        lab.setBackground(Style.BACKGROUND_COLOR());
        lab.setForeground(Style.TEXT_COLOR());

        model.register(key, this);

        add(lab, BorderLayout.NORTH);
        }

    public void update(String key, Model model)
        {
        String name = model.get(key, "");
        if (name == null) name = "";
        text.setText(name);
        text.repaint(); 
        }

    }
