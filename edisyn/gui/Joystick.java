/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.gui;
import edisyn.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.io.*;
import java.awt.geom.*;

public class Joystick extends JComponent
    {
    public double xPos = 0;
    public double yPos = 0;
    public double[] xPositions = new double[0];
    public double[] yPositions = new double[0];
    public int position = 0;
    boolean pressed = false;
    boolean snap = false;
    String[] parameter = new String[4];
    int[] lastVal = new int[4];
    public Synth synth;    
    public Color[] colors = new Color[0];
    boolean drawsUnpressedCursor = true;
    public int margin = (int)Style.JOYSTICK_WIDTH() / 2 + (int)Style.DIAL_STROKE_WIDTH() / 2;

    public void setNumPositions(Color[] colors) { xPositions = new double[colors.length]; yPositions = new double[colors.length]; position = 0; this.colors = colors; }
    public int getNumPositions() { return xPositions.length; }
    public void setPosition(int position) { this.position = position; }
    public int getPosition() { return position; }
    public void setSnap(boolean val) { snap = val; }
    public boolean getSnap() { return snap; }
    public void setParameter(String param, int index) { parameter[index] = param; lastVal[index] = -1;}
    public String getParameter(int index) { return parameter[index]; }
    public boolean getDrawsUnpressedCursor() { return drawsUnpressedCursor; }
    public void setDrawsUnpressedCursor(boolean val) { drawsUnpressedCursor = val; }
    
        
    public Dimension getMinimumSize()
        {
        return new Dimension((int)(128 + Style.JOYSTICK_WIDTH() + Style.DIAL_STROKE_WIDTH() + margin), (int)(128 + Style.JOYSTICK_WIDTH() + Style.DIAL_STROKE_WIDTH() + margin));
        }
        
    public Dimension getPreferredSize()
        {
        return getMinimumSize();
        }
    
    public void prepaint(Graphics2D g)
        {
        }
    
    public void paintComponent(Graphics g)
        {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Style.BACKGROUND_COLOR());
        g2d.fill(new Rectangle2D.Double(0, 0, getWidth(), getHeight()));
        prepaint(g2d);
        g2d.setColor(Style.DIAL_UNSET_COLOR());
        g2d.setStroke(Style.DIAL_THIN_STROKE());
        double strokeWidth = Style.DIAL_STROKE_WIDTH()  / 2;
        g2d.draw(new Rectangle2D.Double(strokeWidth / 2, strokeWidth / 2, getWidth() - strokeWidth , getHeight() - strokeWidth ));
        g2d.draw(new Line2D.Double(getWidth() / 2.0 , 0, getWidth() / 2.0, getHeight()));
        g2d.draw(new Line2D.Double(0, getHeight() / 2.0, getWidth(), getHeight() / 2.0));        

        g2d.setFont(Style.SMALL_FONT());
        int height = g2d.getFontMetrics().getHeight();
            
        for(int i = xPositions.length - 1; i >= 0 ; i--)
            {
            g2d.setColor(colors[i]);
            double centerX2 = (xPositions[i] + 1) / 2 * (getWidth() - margin * 2) + margin;
            double centerY2 = (yPositions[i] + 1) / 2 * (getHeight() - margin * 2) + margin;
            g2d.fill(new Ellipse2D.Double(centerX2 - Style.JOYSTICK_WIDTH() / 4.0, centerY2 - Style.JOYSTICK_WIDTH() / 4.0, Style.JOYSTICK_WIDTH() / 2.0, Style.JOYSTICK_WIDTH() / 2.0));
                
            if (xPositions.length > 1)
                {
                double hpos = centerY2 - Style.JOYSTICK_WIDTH() / 4.0 - height;
                if (hpos - height < 0)  // uh oh
                    hpos = centerY2 + Style.JOYSTICK_WIDTH() / 4.0 + height;
                g2d.drawString("" + i, 
                    (float)(centerX2 - g2d.getFontMetrics().stringWidth("" + i) / 2.0),
                    (float)hpos);
                }
            }
        
        if (pressed || drawsUnpressedCursor)
            {
            if (!pressed)
                g2d.setColor(Style.DIAL_UNSET_COLOR());
            else
                g2d.setColor(Style.DIAL_DYNAMIC_COLOR());
            g2d.setStroke(Style.DIAL_THIN_STROKE());
            double centerX = (xPos + 1) / 2 * (getWidth() - margin * 2) + margin;
            double centerY = (yPos + 1) / 2 * (getHeight() - margin * 2) + margin;
            g2d.draw(new Ellipse2D.Double(centerX - Style.JOYSTICK_WIDTH() / 2.0, centerY - Style.JOYSTICK_WIDTH() / 2.0, Style.JOYSTICK_WIDTH(), Style.JOYSTICK_WIDTH()));
            }
        }
    
    public void updatePosition()
        {
        repaint();
        }
        
    public void release()
        {
        if (snap)
            {
            xPos = 0;
            yPos = 0;
            updatePosition();
            }
        }
        
    public void revisePosition(MouseEvent e)
        {
        xPos = 2 * ((e.getX() - margin) / ((double)getWidth() - (margin * 2.0))) - 1;
        yPos = 2 * ((e.getY() - margin) / ((double)getHeight() - (margin * 2.0))) - 1;
        if (xPos < -1.0) xPos = -1.0;
        if (xPos > 1.0) xPos = 1.0;
        if (yPos < -1.0) yPos = -1.0;
        if (yPos > 1.0) yPos = 1.0;
        if (xPositions.length > 0)
            {
            xPositions[position] = xPos;
            yPositions[position] = yPos;
            }
        }
        
    public AWTEventListener releaseListener = null;

    public Joystick(Synth synth)
        {
        this.synth = synth;
        
        addMouseMotionListener(new MouseMotionAdapter()
            {
            public void mouseDragged(MouseEvent e)
                {
                revisePosition(e);
                updatePosition();
                }
            });
                        
        addMouseListener(new MouseAdapter()
            {
            public void mouseClicked(MouseEvent e)
                {
                if (e.getClickCount() > 1)
                    {
                    xPos = 0;
                    yPos = 0;
                    if (xPositions.length > 0)
                        {
                        xPositions[position] = 0;
                        yPositions[position] = 0;
                        }
                    updatePosition();
                    }
                }
                
            public void mousePressed(MouseEvent e)
                {
                mouseDown();
                
                if (releaseListener != null)
                    {
                    releaseListener = null;
                    }

                // This gunk fixes a BAD MISFEATURE in Java: mouseReleased isn't sent to the
                // same component that received mouseClicked.  What the ... ? Asinine.
                // So we create a global event listener which checks for mouseReleased and
                // calls our own private function.  EVERYONE is going to do this.
                                                        
                Toolkit.getDefaultToolkit().addAWTEventListener( releaseListener = new AWTEventListener()
                    {
                    public void eventDispatched(AWTEvent evt)
                        {
                        if (evt instanceof MouseEvent && evt.getID() == MouseEvent.MOUSE_RELEASED)
                            {
                            MouseEvent e = (MouseEvent) evt;
                            if (releaseListener != null)
                                {
                                mouseUp();
                                if (snap)
                                    {
                                    xPos = 0;
                                    yPos = 0;
                                    if (xPositions.length > 0)
                                        {
                                        xPositions[position] = 0;
                                        yPositions[position] = 0;
                                        }
                                    updatePosition();
                                    }
                                pressed = false;
                                Toolkit.getDefaultToolkit().removeAWTEventListener( releaseListener );
                                repaint();
                                }
                            }
                        }
                    }, AWTEvent.MOUSE_EVENT_MASK);


                boolean found = false;
                if (xPositions.length > 1)
                    {
                    for(int i = 0; i < xPositions.length; i++)
                        {
                        double centerX = (xPositions[i] + 1) / 2 * (getWidth() - margin * 2) + margin;
                        double centerY = (yPositions[i] + 1) / 2 * (getHeight() - margin * 2) + margin;
                        if (((centerX - e.getX()) * (centerX - e.getX()) +
                                (centerY - e.getY()) * (centerY - e.getY())) <= (Style.JOYSTICK_WIDTH() / 4.0) * (Style.JOYSTICK_WIDTH() / 4.0))
                            {
                            position = i;   // got a new position
                            found = true;
                            break;
                            }
                        }
                    }
                if (!found && xPositions.length > 1)
                    {
                    // maybe try a bit more slop
                    for(int i = 0; i < xPositions.length; i++)
                        {
                        double centerX = (xPositions[i] + 1) / 2 * (getWidth() - margin * 2) + margin;
                        double centerY = (yPositions[i] + 1) / 2 * (getHeight() - margin * 2) + margin;
                        if (((centerX - e.getX()) * (centerX - e.getX()) +
                                (centerY - e.getY()) * (centerY - e.getY())) <= (Style.JOYSTICK_WIDTH() / 2.0) * (Style.JOYSTICK_WIDTH() / 2.0))
                            {
                            position = i;   // got a new position
                            found = true;
                            break;
                            }
                        }
                    }
                        
                pressed = true;
                revisePosition(e);
                updatePosition();
                }
                                
            public void mouseReleased(MouseEvent e)
                {
                if (releaseListener == null)
                    {
                    mouseUp();
                    if (snap)
                        {
                        xPos = 0;
                        yPos = 0;
                        if (xPositions.length > 0)
                            {
                            xPositions[position] = 0;
                            yPositions[position] = 0;
                            }
                        updatePosition();
                        }
                    pressed = false;
                    repaint();
                    }
                }
            });
        repaint();
        }

    /** Empty hook, called when mouse is pressed */
    public void mouseDown() { }

    /** Empty hook, called when mouse is released */
    public void mouseUp() { }
    }
