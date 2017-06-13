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
   Abstract superclass of widgets which maintain numerical values in the model.
   Each such widget maintains a KEY which is the parameter name in the model. 
   Widgets can share the same KEY and thus must update to reflect changes by
   the other widget.  
        
   <p>You will notably have to implement the <tt>update(...)</tt> method to
   revise the widget in response to changes in the model.

   @author Sean Luke
*/

public class EnvelopeDisplay extends JComponent implements Updatable
    {
    double xConstants[];
    double yConstants[];
    String xKeys[];
    String yKeys[];
    Color color;
    Color semiTransparent;
    Synth synth;
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
        
    public void postProcess(double[] xVals, double[] yVals) { }
    
    public Dimension getPreferredSize() { return new Dimension(width, 64); }
    public Dimension getMinimiumSize() { return new Dimension(64, 64); }
    public Dimension getMaximumSize() { return new Dimension(100000, 100000); }
    
    public Color getColor() { return color; }
    
    public EnvelopeDisplay(Synth synth, Color color, String[] xKeys, String[] yKeys, double xConstants[], double yConstants[])
        {
        super();
        this.synth = synth;
        this.color = color;     
        semiTransparent = new Color(color.getRed(), color.getGreen(), 
            color.getBlue(), (int)(color.getAlpha() * Style.ENVELOPE_DISPLAY_FILL_TRANSPARENCY));
        int len = 0;
        
        if (xKeys != null)
            len = xKeys.length;
        else if (yKeys != null)
            len = yKeys.length;
        else if (xConstants != null)
            len = xConstants.length;
        else if (yConstants != null)
            len = yConstants.length;
        
        if (xKeys == null) 
            xKeys = new String[len];
        if (yKeys == null) 
            yKeys = new String[len];
                
        this.xKeys = xKeys;
        this.yKeys = yKeys;
        this.xConstants = xConstants;
        this.yConstants = yConstants;
        
        if (xKeys.length != yKeys.length ||
            xKeys.length != xConstants.length ||
            xKeys.length != yConstants.length)
            throw new IllegalArgumentException("Not all arrays have the same length.");
        
        if (xKeys.length < 2)
            throw new IllegalArgumentException("Length must be >= 2");
                
        for(int i = 0; i < xKeys.length; i++)
            if (xKeys[i] != null)
                synth.getModel().register(xKeys[i], this);
        for(int i = 0; i < yKeys.length; i++)
            if (yKeys[i] != null)
                synth.getModel().register(yKeys[i], this);
                        
        setBackground(Style.BACKGROUND_COLOR);
        }

    /** Mostly fills the background appropriately. */
    public void paintComponent(Graphics g)
        {
        Graphics2D graphics = (Graphics2D) g;
        
        double[] xs = new double[xKeys.length]; 
        double[] ys = new double[xKeys.length]; 
        
        System.arraycopy(xConstants, 0, xs, 0, xKeys.length);
        System.arraycopy(yConstants, 0, ys, 0, yKeys.length);
        
        for(int i = 0; i < xs.length; i++)
            {
            if (xKeys[i] != null)
                xs[i] *= synth.getModel().get(xKeys[i], 1);
            if (yKeys[i] != null)
                ys[i] *= synth.getModel().get(yKeys[i], 1);
            }
        
        postProcess(xs, ys);
        
        Rectangle rect = getBounds();
        rect.x = 0;
        rect.y = 0;

        graphics.setPaint(Style.BACKGROUND_COLOR);
        graphics.fill(rect);
        
        // revise
        
        graphics.setColor(color);

        rect.width -= Style.ENVELOPE_DISPLAY_BORDER_THICKNESS * 2;
        rect.height -= Style.ENVELOPE_DISPLAY_BORDER_THICKNESS + Style.ENVELOPE_DISPLAY_TOP_BORDER_THICKNESS;
        rect.x += Style.ENVELOPE_DISPLAY_BORDER_THICKNESS;
        rect.y += Style.ENVELOPE_DISPLAY_TOP_BORDER_THICKNESS;
        Line2D.Double line = new Line2D.Double(rect.x, rect.y, rect.x + rect.width, rect.y);
        graphics.draw(line);
        line = new Line2D.Double(rect.x, rect.y + rect.height, rect.x + rect.width, rect.y + rect.height);
        graphics.draw(line);
        
        double xcurrent = 0;
        for(int i = 0; i < xs.length; i++)
            {
            xs[i] *= rect.width;
            double f = xs[i];
            xs[i] += xcurrent;
            xcurrent = xcurrent + f;
            }
        for(int i = 0; i < ys.length; i++)
            {
            ys[i] *= rect.height;
            }
                
        Path2D.Double p = new Path2D.Double();

        p.moveTo(xs[0] + Style.ENVELOPE_DISPLAY_BORDER_THICKNESS, rect.height - ys[0] + Style.ENVELOPE_DISPLAY_TOP_BORDER_THICKNESS);
        for(int i = 1; i < xs.length; i++)
            {
            p.lineTo(xs[i] + Style.ENVELOPE_DISPLAY_BORDER_THICKNESS, rect.height - ys[i] + Style.ENVELOPE_DISPLAY_TOP_BORDER_THICKNESS);
            } 
        
        int end = xs.length - 1;
        
        p.moveTo(xs[end] + Style.ENVELOPE_DISPLAY_BORDER_THICKNESS, rect.height + 20 + Style.ENVELOPE_DISPLAY_TOP_BORDER_THICKNESS);
        p.moveTo(xs[0] + Style.ENVELOPE_DISPLAY_BORDER_THICKNESS, rect.height + 20 + Style.ENVELOPE_DISPLAY_TOP_BORDER_THICKNESS);
        p.moveTo(xs[0] + Style.ENVELOPE_DISPLAY_BORDER_THICKNESS, rect.height - ys[0] + Style.ENVELOPE_DISPLAY_TOP_BORDER_THICKNESS);   
        p.closePath();
        
        graphics.setColor(semiTransparent);
        graphics.fill(p);
        graphics.setColor(color);
        graphics.draw(p);
        
        if (axis != 0)
        	{
        	graphics.setColor(color);
        	double height = rect.height - (rect.height * axis);
        	line = new Line2D.Double(rect.x, rect.y + height, rect.x + rect.width, rect.y + height);
			graphics.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, new float[] { 4.0f }, 0.0f));
			graphics.draw(line);
        	}
        }
        
    double axis = 0.0;
    public void setAxis(double val) { if (val >= 0.0 && val < 1.0) axis = val; }
    public double getAxis() { return axis; } 
    }


