/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.gui;

import edisyn.*;
import java.awt.*;
import java.awt.geom.*;
/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

import javax.swing.border.*;
import javax.swing.*;
import java.awt.event.*;

/**
   A wrapper for JLabel which displays an image.
   @author Sean Luke
*/

public class IconDisplay extends JComponent implements Updatable
    {
    JLabel label;
    JLabel icon;
    ImageIcon[] icons;
    String key;
    Synth synth;

    public void update(String key, Model model) 
        { 
        icon.setIcon(icons[model.get(key, 0)]);
        icon.repaint();
        }

    public IconDisplay(String label, ImageIcon[] icons, Synth synth, String key, int width, int height)
        {
        for(int i = 0; i < icons.length; i++)
            {
            icons[i] = new ImageIcon(icons[i].getImage().getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH));
            }
        buildIconDisplay(label, icons, synth, key);
        }


    public IconDisplay(String label, ImageIcon[] icons, Synth synth, String key)
        {
        buildIconDisplay(label, icons, synth, key);
        }

    public void buildIconDisplay(String label, ImageIcon[] icons, Synth synth, String key)
        {
        this.synth = synth;
        this.key = key;
        this.icons = icons;
        this.label = new JLabel(label);
        this.icon = new JLabel(icons[0]);

        this.label.setFont(Style.SMALL_FONT());
        this.label.setBackground(Style.BACKGROUND_COLOR()); // TRANSPARENT);
        this.label.setForeground(Style.TEXT_COLOR());
        setBackground(Style.BACKGROUND_COLOR()); // TRANSPARENT);
        this.icon.setBackground(Style.BACKGROUND_COLOR()); // TRANSPARENT);

        setLayout(new BorderLayout());
        add(this.icon, BorderLayout.CENTER);
        if (label != null)
            add(this.label, BorderLayout.NORTH);

        synth.getModel().register(key, this);
        update(key, synth.getModel());
        }
    }
