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
    JPanel display;
    ImageIcon[] icons;
    String key;
    Synth synth;
    ImageIcon icon = null;
    int h;
    int w;
    boolean retina;

    public void update(String key, Model model) 
        { 
        int v = model.get(key, 0);
        if (v < icons.length && v >= 0)
            icon = icons[v];
        else System.err.println("Warning (IconDisplay): invalid value for key " + key + ", was " + v);
        repaint();
        }

    public IconDisplay(String label, ImageIcon[] icons, Synth synth, String key, int width, int height)
        {
        h = height;
        w = width;
        retina = Style.isRetinaDisplay();
        for(int i = 0; i < icons.length; i++)
            {
            icons[i] = new ImageIcon(icons[i].getImage().getScaledInstance(retina ? width*2 : width, retina ? height*2 : height, java.awt.Image.SCALE_SMOOTH));
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
        this.display = new JPanel()
        	{
        	public Dimension getPreferredSize()
        		{
        		return new Dimension(w, h);
        		}
        		
        	public void paintComponent(Graphics g)
        		{
        			Graphics2D g2 = (Graphics2D) g;
        		if (retina)
        			{
        			AffineTransform a = g2.getTransform();
        			a.scale(0.5, 0.5);
        			g2.setTransform(a);
        			}
        		g2.setColor(Color.BLACK);
        		g2.fillRect(0, 0, getWidth(), getHeight());
        		g2.drawImage(icon.getImage(), 0, 0, null);
        		}
        	};

        this.label.setFont(Style.SMALL_FONT());
        this.label.setBackground(Style.BACKGROUND_COLOR()); // TRANSPARENT);
        this.label.setForeground(Style.TEXT_COLOR());
        setBackground(Style.BACKGROUND_COLOR()); // TRANSPARENT);
        //this.icon.setBackground(Style.BACKGROUND_COLOR()); // TRANSPARENT);

        setLayout(new BorderLayout());
        add(this.display, BorderLayout.CENTER);
        if (label != null)
            add(this.label, BorderLayout.NORTH);

        synth.getModel().register(key, this);
        update(key, synth.getModel());
        }
    }
