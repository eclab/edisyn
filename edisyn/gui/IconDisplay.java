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

    public IconDisplay(String label, ImageIcon[] icons, Synth synth, String key)
        {
		this.synth = synth;
		this.key = key;
        this.icons = icons;
        this.label = new JLabel(label);
        this.icon = new JLabel(icons[0]);

        this.label.setFont(Style.SMALL_FONT);
        this.label.setBackground(Style.TRANSPARENT);
        this.label.setForeground(Style.TEXT_COLOR);
        setBackground(Style.TRANSPARENT);
        this.icon.setBackground(Style.TRANSPARENT);

        setLayout(new BorderLayout());
        add(this.icon, BorderLayout.CENTER);
        if (label != null)
            add(this.label, BorderLayout.NORTH);

		synth.getModel().register(key, this);
		update(key, synth.getModel());
        }
    }
