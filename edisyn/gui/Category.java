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
   A pretty container for widgets to categorize them

   @author Sean Luke
*/

public class Category extends JComponent
    {             
    Color color;
      
    public Category(String label, Color color)
        {
        setLayout(new BorderLayout());
        this.color = color;     
        setName(label);
        }
    
    public void setName(String label)
        {
        
        // here we're going to do a little hack.  TitledBorder doesn't put the title
        // on the FAR LEFT of the line, so when we draw the border we get a little square
        // dot to the left of the title which looks really annoying.  Rather than build a
        // totally new border, we're just going to change the insets.  Titled border uses
        // the insets of the underlying border as part of its calculation of where to put
        // the border, so if we subtract 5 from the insets of the underlying border this
        // counteracts the 5 pixels that titledBorder adds in to shift the title over to
        // the right annoyingly.  So during paintBorder, we turn off a flag, then when
        // super.paintBorder goes to grab the underlying border's insets, it gets a special
        // insets which are off by 5.  But other times the insets are requested (such as
        // in paintComponent) they return normal.
        
        final boolean[] paintingBorder = new boolean[1];
        
        final MatteBorder matteBorder = new MatteBorder(Style.CATEGORY_STROKE_WIDTH, 0, 0, 0, color)
            {
            public Insets getBorderInsets(Component c, Insets insets)
                {
                Insets ins = super.getBorderInsets(c, insets);
                if (paintingBorder[0]) 
                    ins.left = -5;
                return ins;
                }
            };
        
        TitledBorder titledBorder = new TitledBorder(
            matteBorder, //BorderFactory.createLineBorder(color, Style.CATEGORY_STROKE_WIDTH, true), 
            " " + label + " ",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            Style.CATEGORY_FONT,
            color)
            {
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) 
                {
                paintingBorder[0] = true;
                super.paintBorder(c, g, x, y, width, height);
                paintingBorder[0] = false;
                }
            };
                
        Border b = BorderFactory.createCompoundBorder(Style.CATEGORY_BORDER, titledBorder);
        setBorder(b);
        repaint();
        }
    
    public void paintComponent(Graphics g)
        {
        Graphics2D graphics = (Graphics2D) g;

        Style.prepareGraphics(g);

        Rectangle rect = getBounds();
        rect.x = 0;
        rect.y = 0;
        graphics.setPaint(Style.BACKGROUND_COLOR);
        graphics.fill(rect);
        }
    }
