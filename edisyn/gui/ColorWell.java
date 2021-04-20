/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package edisyn.gui;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.event.*;

public class ColorWell extends JPanel
    {
    Color color;
                
    public Insets getInsets() { return new Insets(4, 4, 4, 4); }

    public ColorWell() 
        {
        this(new Color(0,0,0,0));
        }
                        
    public ColorWell(Color c)
        {
        color = c;
        addMouseListener(new MouseAdapter()
            {
            public void mouseReleased(MouseEvent e)
                {
                Color col = JColorChooser.showDialog(null, "Choose Color", color);
                setColor(col);
                }
            });
        setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
        }
                
    // maybe in the future we'll add an opacity mechanism
    public void paintComponent(Graphics g)
        {
        g.setColor(color);
        g.fillRect(0,0,getWidth(),getHeight());
        }

    public void setColor(Color c)
        {
        if (c != null) 
            color = changeColor(c);
        repaint();
        }
                        
    public Color getColor()
        {
        return color;
        }
                        
    public Color changeColor(Color c) 
        {
        return c;
        }
    }
