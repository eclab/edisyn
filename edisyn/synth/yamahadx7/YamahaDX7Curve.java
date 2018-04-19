/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.yamahadx7;

import edisyn.*;
import edisyn.gui.*;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.border.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javax.sound.midi.*;

public class YamahaDX7Curve extends JComponent implements Updatable
    {
    String breakpointkey;
    String leftkey;
    String rightkey;
    String leftDepthKey;
    String rightDepthKey;
    Synth synth;

    int width = 128;
    int minWidth = 84;
    int height = 84;

    public void setPreferredWidth(int width)
        {
        this.width = width;
        }
    
    public int getPreferredWidth()
        {
        return this.width;
        }
     
    public int getPreferredHeight() { return height; }
    public void setPreferredHeight(int height) { this.height = height; }
    
    public Dimension getPreferredSize() { return new Dimension(width, height); }
    public Dimension getMinimiumSize() { return new Dimension(minWidth, height); }
    public Dimension getMaximumSize() { return new Dimension(100000, 100000); }
    
    public YamahaDX7Curve(Synth synth, String breakpointkey, String leftkey, String rightkey, String leftDepthKey, String rightDepthKey)
        {
        this.breakpointkey = breakpointkey;
        this.leftkey = leftkey;
        this.rightkey = rightkey;
        this.leftDepthKey = leftDepthKey;
        this.rightDepthKey = rightDepthKey;
        this.synth = synth;
        
        synth.getModel().register(breakpointkey, this);
        synth.getModel().register(leftkey, this);
        synth.getModel().register(rightkey, this);
        synth.getModel().register(leftDepthKey, this);
        synth.getModel().register(rightDepthKey, this);
        }
    
    public void update(String key, Model model)
        {
        repaint();
        }
        
    public void paintComponent(Graphics g1)
        {
        Graphics2D g = (Graphics2D) g1;

        super.paintComponent(g);
        g.setColor(Style.BACKGROUND_COLOR());
        Rectangle rect = getBounds();
        rect.x = 0;
        rect.y = 0;
        g.fill(rect);
        g.setColor(Style.ENVELOPE_COLOR());
        rect.width -= 1;
        rect.height -= 1;
        rect.x += Style.ENVELOPE_DISPLAY_BORDER_THICKNESS();
        rect.width -= (2 * Style.ENVELOPE_DISPLAY_BORDER_THICKNESS());
        
        g.draw(new Rectangle2D.Double(rect.x, rect.y, rect.width, rect.height));
        g.draw(new Line2D.Double(rect.x, rect.y + rect.height/2.0, rect.x + rect.width, rect.y + rect.height / 2.0));
        g.draw(new Line2D.Double(rect.x + rect.width / 2.0, rect.y, rect.x + rect.width / 2.0, rect.y + rect.height)); 
        
        double x = synth.getModel().get(breakpointkey, 50) / 99.0 * rect.width;
        int left = synth.getModel().get(leftkey, 0);
        int right = synth.getModel().get(rightkey, 0); 
        double ldepth = synth.getModel().get(leftDepthKey, 0) / 99.0 * (rect.height / 2.0);
        double rdepth = synth.getModel().get(rightDepthKey, 0) / 99.0 * (rect.height / 2.0);
        
        Path2D.Double path = new Path2D.Double();
        double starty = (left == YamahaDX7.POSLINEAR || left == YamahaDX7.POSEXP) ? rect.y + rect.height / 2.0 - ldepth : rect.y + rect.height / 2.0 + ldepth;
        double endy = (right == YamahaDX7.POSLINEAR || right == YamahaDX7.POSEXP) ?  rect.y + rect.height / 2.0 - rdepth : rect.y + rect.height / 2.0 + rdepth;
        double startconty = (left == YamahaDX7.POSLINEAR ? rect.y + rect.height / 2.0 - ldepth : (left == YamahaDX7.NEGLINEAR ? rect.y + rect.height / 2.0 + ldepth : rect.y + rect.height / 2.0));
        double endconty = (right == YamahaDX7.POSLINEAR ?rect.y + rect.height / 2.0  - rdepth: (right == YamahaDX7.NEGLINEAR ? rect.y + rect.height / 2.0 + rdepth : rect.y + rect.height / 2.0));
                                                        
        path.moveTo( rect.x, rect.y + starty);
        path.quadTo( rect.x, rect.y + startconty, rect.x + x, rect.y + rect.height / 2.0 );
        path.quadTo( rect.x + rect.width, rect.y + endconty, rect.x + rect.width, rect.y + endy );
        
        g.setStroke(new BasicStroke(Style.DIAL_STROKE_WIDTH() / 4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
        g.draw(path);
                
        Ellipse2D.Double marker = new Ellipse2D.Double((rect.x + x - Style.ENVELOPE_DISPLAY_MARKER_WIDTH()/2.0),
            (rect.y + rect.height / 2.0 - Style.ENVELOPE_DISPLAY_MARKER_WIDTH()/2.0),
            Style.ENVELOPE_DISPLAY_MARKER_WIDTH(), Style.ENVELOPE_DISPLAY_MARKER_WIDTH());
                
        g.fill(marker);

        Ellipse2D.Double leftmarker = new Ellipse2D.Double((rect.x - Style.ENVELOPE_DISPLAY_MARKER_WIDTH()/2.0),
            (rect.y + starty - Style.ENVELOPE_DISPLAY_MARKER_WIDTH()/2.0),
            Style.ENVELOPE_DISPLAY_MARKER_WIDTH(), Style.ENVELOPE_DISPLAY_MARKER_WIDTH());
                
        g.fill(leftmarker);

        Ellipse2D.Double rightmarker = new Ellipse2D.Double((rect.x + rect.width - Style.ENVELOPE_DISPLAY_MARKER_WIDTH()/2.0),
            (rect.y + endy - Style.ENVELOPE_DISPLAY_MARKER_WIDTH()/2.0),
            Style.ENVELOPE_DISPLAY_MARKER_WIDTH(), Style.ENVELOPE_DISPLAY_MARKER_WIDTH());
                
        g.fill(rightmarker);
        }

    }
