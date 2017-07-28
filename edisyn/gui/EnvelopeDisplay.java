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
	A tool to display envelopes.  You will provide one of two collections of items:
	
	<ul><li>
	An array of XKEYS, YKEYS, XCONSTANTS, and YCONSTANTS.  The xkeys and ykeys define
	the x and y coordinates of each point in the envelope, including the endpoints.  
	They are multiplied by the xconstants and yconstants so that the y values stay within
	the range [0...1] and the x values *summed together* will not exceed the range [0...1].
	Individual XKEYS and YKEYS can be null: if an xkey is null, its value is 0.  If a ykey
	is null, its value is 1.
	
	<p>This approach is useful for envelopes where the xkeys are related to the specific length of time
	that a synth will take to reach a next stage.  An example of a synth in this category: the
	Oberheim Matrix 1000.

	<ul><li>
	An array of XKEYS, YKEYS, XCONSTANTS, YCONSTANTS, and RATES.  The ykeys define
	the y coordinates of each point in the envelope, including the endpoints.  xkey #0
	defines the x coordinate of point 0 in the envelope; thereafter x coordinates are computed by taking
	the RATES (angles between 0 and PI/2) and figuring the x distance needed for a line of the
	given angle, starting at the previous y height, to reach the next y height.  Rates are always
	positive even if the resulting line has a negative slope.  You will probably have to scale
	the rates by a certain amount so that the largest possible envelopes sum to 1.0.  I have found
	that if you have three slopes, you may need to scale by 0.314, and if you have four slopes,
	you may need to scale by 0.25.  
	
	The xconstants now simply define the *maximum* x distance that a sloped line will cover: they
	should sum to 1.  The y constants again should be such that the y values, multiplied
	by the y constants, stay within the range [0...1].
	Individual XKEYS and YKEYS can be null: if an xkey is null, its value is 0.  If a ykey
	is null, its value is 1.
	
	<p>This approach is useful for envelopes where the xkeys are related directly to the angle of attack of the 
	sloped line.   An example of a synth in this category: the Waldorf Blofeld.
	<li>

   @author Sean Luke
*/

public class EnvelopeDisplay extends JComponent implements Updatable
    {
    double xConstants[];
    double yConstants[];
	double rates[];
    String xKeys[];
    String yKeys[];
    Color color;
    Color semiTransparent;
    Synth synth;
    int width = 128;
    int behavior[] = null;
    
    public static final int NUM_INTERVALS = 2;
    String[] startKey = new String[NUM_INTERVALS];
    String[] endKey = new String[NUM_INTERVALS];
    
    String finalStageKey;
    String sustainStageKey;
    
    public void setFinalStageKey(String key)
    	{
    	finalStageKey = key;
		synth.getModel().register(key, this);
    	}

    public void setSustainStageKey(String key)
    	{
    	sustainStageKey = key;
		synth.getModel().register(key, this);
    	}
    	
    public void setLoopKeys(int interval, String startKey, String endKey)
    	{
    	this.startKey[interval] = startKey;
    	this.endKey[interval] = endKey;
		synth.getModel().register(startKey, this);
		synth.getModel().register(endKey, this);
    	}
    	
    public int postProcessLoopOrStageKey(String key, int val)
    	{
    	return val;
    	}
    	        
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
    
    public Dimension getPreferredSize() { return new Dimension(width, 84); }
    public Dimension getMinimiumSize() { return new Dimension(84, 84); }
    public Dimension getMaximumSize() { return new Dimension(100000, 100000); }
    
    public Color getColor() { return color; }
    
    public EnvelopeDisplay(Synth synth, Color color, String[] xKeys, String[] yKeys, double xConstants[], double yConstants[])
    	{
    	this(synth, color, xKeys, yKeys, xConstants, yConstants, null);
    	}

    public EnvelopeDisplay(Synth synth, Color color, String[] xKeys, String[] yKeys, double xConstants[], double yConstants[], double[] rates)
        {
        super();
        this.synth = synth;
        this.color = color;     
        semiTransparent = new Color(color.getRed(), color.getGreen(), 
            color.getBlue(), (int)(color.getAlpha() * Style.ENVELOPE_DISPLAY_FILL_TRANSPARENCY));
        int len = 0;
        this.rates = rates;
        
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
        
        // count loop intervals
        int numLoops = 0;
        for(int i = 0; i < startKey.length; i++)
        	if (startKey[i] != null)
        		numLoops++;
        
        double[] xs = new double[xKeys.length]; 
        double[] ys = new double[xKeys.length]; 
        
        System.arraycopy(xConstants, 0, xs, 0, xKeys.length);
        System.arraycopy(yConstants, 0, ys, 0, yKeys.length);
        
        for(int i = 0; i < xs.length; i++)
            {
            if (yKeys[i] != null)
                ys[i] *= synth.getModel().get(yKeys[i], 1);
            if (xKeys[i] != null)
                {
                if (rates != null && i > 0)			// we're doing angles
                	{
                	double yd = Math.abs(ys[i] - ys[i-1]);
                	double xd = Math.abs(yd / Math.tan(Math.PI/2.0 - rates[i] * synth.getModel().get(xKeys[i], 0)));
                	xs[i] *= xd;
                	}
	            else
	            	{
	            	xs[i] *= synth.getModel().get(xKeys[i], 1);
	            	}
                }
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
        rect.height -= Style.ENVELOPE_DISPLAY_BORDER_THICKNESS * 2;
        //rect.height -= spaceForLoopInterval * numLoops;

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
            
        double startHeight = rect.height;
        if (axis != 0)
            startHeight = rect.height - (rect.height * axis);

                
        Path2D.Double fillp = new Path2D.Double();
        Path2D.Double p = new Path2D.Double();
        Ellipse2D marker[] = new Ellipse2D[xs.length];

        fillp.moveTo(rect.x + xs[0], rect.y + startHeight); 
        
        p.moveTo(rect.x + xs[0], rect.y + rect.height - ys[0]);
        fillp.lineTo(rect.x + xs[0], rect.y + rect.height - ys[0]);
        marker[0] = new Ellipse2D.Double((rect.x + xs[0] - Style.ENVELOPE_DISPLAY_MARKER_WIDTH/2.0),
            (rect.y + rect.height - ys[0] - Style.ENVELOPE_DISPLAY_MARKER_WIDTH/2.0),
            Style.ENVELOPE_DISPLAY_MARKER_WIDTH, Style.ENVELOPE_DISPLAY_MARKER_WIDTH);
        
        for(int i = 1; i < xs.length; i++)
            {
            p.lineTo(rect.x + xs[i], rect.y + rect.height - ys[i]);
            fillp.lineTo(rect.x + xs[i], rect.y + rect.height - ys[i]);
            marker[i] = new Ellipse2D.Double((rect.x + xs[i] - Style.ENVELOPE_DISPLAY_MARKER_WIDTH/2.0),
                (rect.y + rect.height - ys[i] - Style.ENVELOPE_DISPLAY_MARKER_WIDTH/2.0),
                Style.ENVELOPE_DISPLAY_MARKER_WIDTH, Style.ENVELOPE_DISPLAY_MARKER_WIDTH);
            } 
        
        int end = xs.length - 1;
        
        fillp.lineTo(rect.x + xs[end], rect.y + startHeight);
        fillp.lineTo(rect.x + xs[0], rect.y + startHeight); 
        fillp.closePath();
        
        graphics.setColor(semiTransparent);
        graphics.fill(fillp);
        
        graphics.setColor(color);
        graphics.draw(p);
        
        // draw markers
        for(int i = 0; i < marker.length; i++)
            {
            graphics.fill(marker[i]);
            }
        
        // draw axis
        if (axis != 0)
            {
            graphics.setColor(color);
            line = new Line2D.Double(rect.x, rect.y + startHeight, rect.x + rect.width, rect.y + startHeight);
            graphics.setStroke(Style.ENVELOPE_AXIS_STROKE);
            graphics.draw(line);
            }
            
        // draw stage ends

        if (sustainStageKey != null)
        	{
        	int sustainStage = postProcessLoopOrStageKey(sustainStageKey, synth.getModel().get(sustainStageKey, 0));
        	line = new Line2D.Double(rect.x + xs[sustainStage], rect.y,
        							 rect.x + xs[sustainStage], rect.y + rect.height);
            graphics.setStroke(Style.ENVELOPE_AXIS_STROKE);
        	graphics.draw(line);
        	}
        

        if (finalStageKey != null)
        	{
        	int finalStage = postProcessLoopOrStageKey(finalStageKey, synth.getModel().get(finalStageKey, 0));
        	line = new Line2D.Double(rect.x + xs[finalStage], rect.y,
        							 rect.x + xs[finalStage], rect.y + rect.height);
       		graphics.setStroke(new BasicStroke(1.0f));
        	graphics.draw(line);
        	}
        
            
        graphics.setStroke(new BasicStroke(1.0f));
        // draw intervals
    	for(int i = 0; i < numLoops; i++)
    		{
    		double loopStart = rect.x + xs[postProcessLoopOrStageKey(startKey[i], synth.getModel().get(startKey[i], 0))];
    		double loopEnd = rect.x + xs[postProcessLoopOrStageKey(endKey[i], synth.getModel().get(endKey[i], 0))];
    		double loopHeight = rect.y + rect.height + 6 * (i + 1);
    		line = new Line2D.Double(loopStart, loopHeight, loopEnd, loopHeight);
    		graphics.draw(line);
    		Ellipse2D.Double loopEndMarker = new Ellipse2D.Double( loopEnd - Style.ENVELOPE_DISPLAY_MARKER_WIDTH/2.0,
    									  loopHeight - Style.ENVELOPE_DISPLAY_MARKER_WIDTH/2.0,
    									 Style.ENVELOPE_DISPLAY_MARKER_WIDTH, Style.ENVELOPE_DISPLAY_MARKER_WIDTH);
    		graphics.setColor(Style.BACKGROUND_COLOR);
    		graphics.fill(loopEndMarker);
    		graphics.setColor(color);
    		graphics.draw(loopEndMarker);
    		Ellipse2D.Double loopStartMarker = new Ellipse2D.Double( loopStart - Style.ENVELOPE_DISPLAY_MARKER_WIDTH/2.0,
    									  loopHeight - Style.ENVELOPE_DISPLAY_MARKER_WIDTH/2.0,
    									 Style.ENVELOPE_DISPLAY_MARKER_WIDTH, Style.ENVELOPE_DISPLAY_MARKER_WIDTH);
    		graphics.fill(loopStartMarker);
    		}
        }
        
    double axis = 0.0;
    public void setAxis(double val) { if (val >= 0.0 && val < 1.0) axis = val; }
    public double getAxis() { return axis; } 
    }


