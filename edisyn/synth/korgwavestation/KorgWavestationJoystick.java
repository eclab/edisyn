/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.korgwavestation;

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

public class KorgWavestationJoystick extends Joystick implements Updatable
    {
    public String[] keysAC;
    public String[] keysBD;
        
        
    public KorgWavestationJoystick(Synth synth, String[] ac, String[] bd)
        {
        super(synth);
        keysAC = ac;
        keysBD = bd;
        for(int i = 0; i < keysAC.length; i++)
            synth.getModel().register(keysAC[i], this);
        for(int i = 0; i < keysBD.length; i++)
            synth.getModel().register(keysBD[i], this);
        }
        
    // The mouseDown and mouseUp code here enables us to only do undo()
    // ONCE.
    public void mouseDown()
        {
        synth.getUndo().push(synth.getModel());
        synth.getUndo().setWillPush(false);
        }

    public void mouseUp()
        {
        synth.getUndo().setWillPush(true);
        }
                        
    public void prepaint(Graphics2D g)
        {
        super.prepaint(g);
        g.setColor(Style.DIAL_UNSET_COLOR());
        g.setStroke(new BasicStroke(Style.DIAL_STROKE_WIDTH() / 4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
        g.draw(new Line2D.Double(margin, getHeight() / 2.0, getWidth() / 2.0, margin));
        g.draw(new Line2D.Double(margin, getHeight() / 2.0, getWidth() / 2.0, getHeight() - margin));
        g.draw(new Line2D.Double(getWidth() - margin, getHeight() / 2.0, getWidth() / 2.0, margin));
        g.draw(new Line2D.Double(getWidth() - margin, getHeight() / 2.0, getWidth() / 2.0, getHeight() - margin));

        for(int i = 0; i < xPositions.length - 1; i++)
            {
            double centerX = (xPositions[i] + 1) / 2 * (getWidth() - margin * 2) + margin;
            double centerY = (yPositions[i] + 1) / 2 * (getHeight() - margin * 2) + margin;

            double centerX2 = (xPositions[i + 1] + 1) / 2 * (getWidth() - margin * 2) + margin;
            double centerY2 = (yPositions[i + 1] + 1) / 2 * (getHeight() - margin * 2) + margin;

            g.setColor(colors[i]);
            g.draw(new Line2D.Double(centerX, centerY, (centerX + centerX2)/2.0, (centerY + centerY2)/2.0));
            g.setColor(colors[i + 1]);
            g.draw(new Line2D.Double(centerX2, centerY2, (centerX + centerX2)/2.0, (centerY + centerY2)/2.0));
            }
        }

    public void revisePosition(MouseEvent e)
        {
        super.revisePosition(e);
        double[] bounds = boundJoystick(xPos, yPos);
        xPositions[position] = bounds[0];
        yPositions[position] = bounds[1];
        }

    boolean updating = false;
    public void updatePosition()
        {
        super.updatePosition();
        updating = true;
        int x = (int)Math.round((xPos + 1.0) * 127);
        int y = (int)Math.round((0.0 - yPos + 1.0) * 127);
        synth.getModel().set(keysAC[position], x);
        synth.getModel().set(keysBD[position], y);
        updating = false;
        }
        
    public void updateAll()
        {
        for(int i = 0; i < keysAC.length; i++)
            {
            update(keysAC[i], synth.getModel());  // no need to update BD, it's automatic
            }
        }
                
    public void update(String key, Model model)
        {
        if (!updating)
            {
            for(int i = 0; i < xPositions.length; i++)
                {
                if (keysAC[i].equals(key) || keysBD[i].equals(key))
                    {
                    xPositions[i] = model.get(keysAC[i]) / 127.0 - 1.0;
                    yPositions[i] = 0.0 - (model.get(keysBD[i]) / 127.0 - 1.0);
                    double[] d = boundJoystick(xPositions[i], yPositions[i]);
                    xPositions[i] = d[0];
                    yPositions[i] = d[1];

                    repaint();
                    return;
                    }
                }
            }
        }

// forces a square.  Stolen and modified from
// https://stackoverflow.com/questions/3489641/forcing-a-jcomponent-to-be-square-when-being-resized
    public void setBounds(int x, int y, int width, int height) {
        int currentWidth = getWidth();
        int currentHeight = getHeight();
        if (currentWidth!=width || currentHeight!=height) {
            // find out which one has changed
            if (currentWidth!=width && currentHeight!=height) {  
                // both changed, set size to max
                width = height = Math.min(width, height);
                }
            else if (currentWidth==width) {
                // height changed, make width the same
                width = height;
                }
            else // currentHeight==height
                height = width;
            }
        super.setBounds(x, y, width, height);
        }


// top right quadrant only
    double[] boundCanonical(double x, double y)
        {
        // first deal with infinite slope
        if (x == 0 && y >= 0)
            return new double[] { x, Math.min(y, 1) };
        if (x == 0 && y < 0) // never happens because we're top right quadrant but okay
            return new double[] { x, Math.max(y, -1) };
                        
        double m = y / x;
        double x2 = 1.0 / (m + 1.0);
        double y2 = m / (m + 1.0);
        
        if (x2 * x2 + y2 * y2 < x * x + y * y)  // we're bounded
            return new double[] { x2, y2 };
        else
            return new double[] { x, y };
        }

    public double[] boundJoystick(double x, double y)
        {
        double[] d = null;
        if (x >= 0)
            {
            if (y >= 0)
                {
                d = boundCanonical(x, y);
                }
            else 
                {
                d = boundCanonical(x, -y);
                d[1] = -d[1];
                }
            }
        else
            {
            if (y >= 0)
                {
                d = boundCanonical(-x, y);
                d[0] = -d[0];
                }
            else
                {
                d = boundCanonical(-x, -y);
                d[0] = -d[0];
                d[1] = -d[1];
                }
            }
        return d;
        }
    }
