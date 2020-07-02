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
    ImageIcon[] originalIcons;
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
            {
            buildIcon(v);
            icon = icons[v];
            }
        else System.err.println("Warning (IconDisplay): invalid value for key " + key + ", was " + v);
        repaint();
        }

    public IconDisplay(String label, ImageIcon[] icons, Synth synth, String key, int width, int height)
        {
        h = height;
        w = width;
        retina = Style.isRetinaDisplay();
        this.icons = new ImageIcon[icons.length];
        originalIcons = (ImageIcon[])(icons.clone());
        buildIconDisplay(label, synth, key);
        }

	public void buildIcon(int i)
		{
		if (icons[i] == null)
			{
			icons[i] = new ImageIcon(originalIcons[i].getImage().getScaledInstance(retina ? w * 2 : w, retina ? h * 2 : h, java.awt.Image.SCALE_SMOOTH));
			}
		}

    public IconDisplay(String label, ImageIcon[] icons, Synth synth, String key)
        {
        this(label, icons, synth, key, icons[0].getImage().getWidth(null), icons[0].getImage().getHeight(null));
        }

    public void buildIconDisplay(String label, Synth synth, String key)
        {
        this.synth = synth;
        this.key = key;
        this.label = new JLabel(label);
        this.display = new JPanel()
        	{
        	public Dimension getMinimumSize()
        		{
        		return new Dimension(w, h);
        		}
        		
        	public Dimension getMaximumSize()
        		{
        		return new Dimension(w, h);
        		}
        		
        	public Dimension getPreferredSize()
        		{
        		return new Dimension(w, h);
        		}
        		
        	public void paintComponent(Graphics g)
        		{
        		Graphics2D g2 = (Graphics2D) g;
        		g2.setColor(Style.BACKGROUND_COLOR());
        		g2.fillRect(0, 0, getWidth(), getHeight());
        		if (retina)
        			{
        			g2.setColor(Color.BLACK);
        			AffineTransform a = g2.getTransform();
        			a.scale(0.5, 0.5);
        			g2.setTransform(a);
        			g2.fillRect(0, 0, getWidth() * 2, getHeight() * 2);
        			}
        		else
        			{
        			g2.setColor(Color.BLACK);
        			g2.fillRect(0, 0, getWidth(), getHeight());
        			}
        		g2.drawImage(icon.getImage(), 0, 0, null);
        		}
        	};

        this.label.setFont(Style.SMALL_FONT());
        this.label.setBackground(Style.BACKGROUND_COLOR()); // TRANSPARENT);
        this.label.setForeground(Style.TEXT_COLOR());
        setBackground(Style.BACKGROUND_COLOR()); // TRANSPARENT);

        setLayout(new BorderLayout());
        add(this.display, BorderLayout.NORTH);
        if (label != null)
            add(this.label, BorderLayout.CENTER);

        synth.getModel().register(key, this);
        update(key, synth.getModel());
        }
    }
