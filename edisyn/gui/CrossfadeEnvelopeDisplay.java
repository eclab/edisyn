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


public class CrossfadeEnvelopeDisplay extends JComponent implements Updatable
    {
    Synth synth;
    Color color;
    Color indexColor;
    
    double lengthConstants[];
    double heightConstants[];
    double fadeConstants[];
    String lengthKeys[];
    String heightKeys[];
    String fadeKeys[];
    String seqLenKey;
    String seqIndexKey;
    String magnifyKey;
    
    int width = 128;

    public void setPreferredWidth(int width)
        {
        this.width = width;
        }
                
    public int getPreferredWidth()
        {
        return this.width;
        }
     
    public void update(String key, Model model)
        {
        repaint();
        }
        
    public Dimension getPreferredSize() { return new Dimension(width, 84); }
    public Dimension getMinimiumSize() { return new Dimension(84, 84); }
    public Dimension getMaximumSize() { return new Dimension(100000, 100000); }
    
    public Color getColor() { return color; }
    
    public CrossfadeEnvelopeDisplay(Synth synth, Color color, Color indexColor, String seqIndexKey, String seqLenKey, 
        double[] lengthConstants, double[] heightConstants, double[] fadeConstants, 
        String[] lengthKeys, String[] heightKeys, String[] fadeKeys, String magnifyKey)
        {
        super();
        this.synth = synth;
        this.color = color;  
        this.indexColor = indexColor;
        this.seqIndexKey = seqIndexKey;
        this.seqLenKey = seqLenKey;
        this.magnifyKey = magnifyKey;
        
        this.lengthConstants = lengthConstants;
        this.heightConstants = heightConstants;
        this.fadeConstants = fadeConstants;
        this.lengthKeys = lengthKeys;
        this.heightKeys = heightKeys;
        this.fadeKeys = fadeKeys;
        
        int len = lengthConstants.length;
        if (len < 1)
            throw new IllegalArgumentException("Length must be >= 1");
        if (heightConstants.length != lengthConstants.length)
            throw new IllegalArgumentException("heightConstants is not normal size");
        if (fadeConstants.length != lengthConstants.length)
            throw new IllegalArgumentException("fadeConstants is not normal size");
        if (lengthKeys.length != lengthConstants.length)
            throw new IllegalArgumentException("lengthKeys is not normal size");
        if (heightKeys.length != lengthConstants.length)
            throw new IllegalArgumentException("heightKeys is not normal size");
        if (fadeKeys.length != lengthConstants.length)
            throw new IllegalArgumentException("fadeKeys is not normal size");
                
        Model model = synth.getModel();

        for(int i = 0; i < lengthKeys.length; i++)
            if (lengthKeys[i] != null)
                model.register(lengthKeys[i], this);

        for(int i = 0; i < heightKeys.length; i++)
            if (heightKeys[i] != null)
                model.register(heightKeys[i], this);

        for(int i = 0; i < fadeKeys.length; i++)
            if (fadeKeys[i] != null)
                model.register(fadeKeys[i], this);
                
        model.register(seqIndexKey, this);
        model.register(seqLenKey, this);
        if (magnifyKey != null)
            model.register(magnifyKey, this);
                        
        setBackground(Style.BACKGROUND_COLOR());
        }

    public void paintComponent(Graphics g)
        {
        Graphics2D graphics = (Graphics2D) g;
        
        Rectangle rect = getBounds();
        rect.x = 0;
        rect.y = 0;

        graphics.setPaint(Style.BACKGROUND_COLOR());
        graphics.fill(rect);
        
        Model model = synth.getModel();

        int magnify = 1;
        if (magnifyKey != null)
            magnify = model.get(magnifyKey, 1);
        
        rect.width -= Style.ENVELOPE_DISPLAY_BORDER_THICKNESS() * 2;
        rect.height -= Style.ENVELOPE_DISPLAY_BORDER_THICKNESS() * 2;

        rect.x += Style.ENVELOPE_DISPLAY_BORDER_THICKNESS();
        rect.y += Style.ENVELOPE_DISPLAY_BORDER_THICKNESS();

        int seqLen = model.get(seqLenKey, 0);
        int seqIndex = model.get(seqIndexKey, 0) - model.getMin(seqIndexKey);
        int start = seqIndex;
        /* if (start > 0)
           start --; */
        if (start < 0)
            start = 0;
        else if (start > 0)
            start --;
                        
        double x = rect.x;
        double y = rect.y + rect.height;
        for(int i = start; i < seqLen && x <= rect.width - (rect.width / 5); i++)
            {               
            double fadein = 0.0;
            if (i != 0)
                fadein = model.get(fadeKeys[i - 1], 0) * fadeConstants[i - 1] * magnify;
            double fadeout = model.get(fadeKeys[i], 0) * fadeConstants[i] * magnify;
            double height = model.get(heightKeys[i], 0) * heightConstants[i];
            double length = model.get(lengthKeys[i], 0) * lengthConstants[i] * magnify;
                
            if (i == seqIndex)
                graphics.setColor(indexColor);
            else
                graphics.setColor(color);
                        
            graphics.fill(new Ellipse2D.Double(x - Style.ENVELOPE_DISPLAY_MARKER_WIDTH()/2.0,
                    y - Style.ENVELOPE_DISPLAY_MARKER_WIDTH()/2.0,
                    Style.ENVELOPE_DISPLAY_MARKER_WIDTH(), Style.ENVELOPE_DISPLAY_MARKER_WIDTH()));

            graphics.draw(new Line2D.Double(x, y, 
                    x + rect.width * fadein, y - (rect.height * height)));

            graphics.fill(new Ellipse2D.Double(x + rect.width * fadein - Style.ENVELOPE_DISPLAY_MARKER_WIDTH()/2.0,
                    y - (rect.height * height) - Style.ENVELOPE_DISPLAY_MARKER_WIDTH()/2.0,
                    Style.ENVELOPE_DISPLAY_MARKER_WIDTH(), Style.ENVELOPE_DISPLAY_MARKER_WIDTH()));

            graphics.draw(new Line2D.Double(x + rect.width * fadein, y - (rect.height * height), 
                    x + rect.width * (fadein + length), y - (rect.height * height)));

            graphics.fill(new Ellipse2D.Double(x + rect.width * (fadein + length) - Style.ENVELOPE_DISPLAY_MARKER_WIDTH()/2.0,
                    y - (rect.height * height) - Style.ENVELOPE_DISPLAY_MARKER_WIDTH()/2.0,
                    Style.ENVELOPE_DISPLAY_MARKER_WIDTH(), Style.ENVELOPE_DISPLAY_MARKER_WIDTH()));

            graphics.draw(new Line2D.Double(x + rect.width * (fadein + length), y - (rect.height * height), 
                    x + rect.width * (fadein + length + fadeout), y));

            graphics.fill(new Ellipse2D.Double(x + rect.width * (fadein + length + fadeout) - Style.ENVELOPE_DISPLAY_MARKER_WIDTH()/2.0,
                    y - Style.ENVELOPE_DISPLAY_MARKER_WIDTH()/2.0,
                    Style.ENVELOPE_DISPLAY_MARKER_WIDTH(), Style.ENVELOPE_DISPLAY_MARKER_WIDTH()));

            x += rect.width * (fadein + length);
            }
        }
    }


