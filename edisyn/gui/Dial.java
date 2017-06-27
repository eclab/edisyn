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
   A dial which the user can modify with the mouse.
   The dial updates the model and changes in response to it.
   The dial has no label: for a labelled dial, see LabelledDial.

   @author Sean Luke
*/



public class Dial extends NumericalComponent
    {
    // What's going on?  Is the user changing the dial?
    public static final int STATUS_STATIC = 0;
    public static final int STATUS_DIAL_DYNAMIC = 1;
    int status = STATUS_STATIC;
    Color staticColor;

    // The largest range that the dial can represent.  127 is reasonable
    // for most synths but some synths (DSI ahem) will require more.
    public static final int MAX_EXTENT = 127;
        
    // Used to convert the number into text shown in the dial
    Map map;
        
    // The state when the mouse was pressed 
    int startState;
    // The mouse position when the mouse was pressed 
    int startX;
    int startY;
        
    // Is the mouse pressed?  This is part of a mechanism for dealing with
    // a stupidity in Java: if you PRESS in a widget, it'll be told. But if
    // you then drag elsewhere and RELEASE, the widget is never told.
    boolean mouseDown;
        
    // how much should be subtracted from the value in the model before
    // it is displayed onscreen?
    int subtractForDisplay = 0;
                
    // Field in the center of the dial
    JLabel field = new JLabel("88888", SwingConstants.CENTER);



    public void update(String key, Model model) { field.setText(map(getState())); repaint(); }

    /** Maps the stored number to a text string.
        Override this as you see fit. By default it calls its Map object (typically provided by the LabelledDial).
        Otherwise it subtracts
        the requested amount from the value and converts to a string directly. */
    public String map(int val) { if (map != null) return map.map(val); else return "" +  (val - subtractForDisplay); }

    public Dimension getPreferredSize() { return new Dimension(55, 55); }
    public Dimension getMinimumSize() { return new Dimension(55, 55); }
        
    /** Returns the current Map object, which maps numbers to strings. */
    public Map getMap() { return map; }
    /** Sets the current Map object, which maps numbers to strings. */
    public void setMap(Map v) 
        {
        map = v; 
        if (map != null) 
            {
            field.setText(map(getState()));
            }
        }

    void mouseReleased(MouseEvent e)
        {                       
        if (mouseDown)
            {
            status = STATUS_STATIC;
            repaint();
            mouseDown = false;
            }
        }
        
    /** Makes a dial for the given key parameter on the given synth, and with the given color and
        minimum and maximum.  If there is no map, then prior to display, subtractForDisplay is 
        SUBTRACTED from the parameter value.  You can use this to convert 0...127 in the model
        to -64...63 on-screen, for example.  */
    public Dial(Synth synth, String key, Color staticColor, int min, int max, int subtractForDisplay)
        {
        this(synth, key, staticColor);
        setMin(min);
        setMax(max);
        }
                
    public Dial(Synth synth, String key, Color staticColor)
        {
        super(synth, key);
                
        this.staticColor = staticColor;

        field.setFont(Style.DIAL_FONT);
        field.setBackground(Style.TRANSPARENT);
        field.setForeground(Style.TEXT_COLOR);
        
        addMouseWheelListener(new MouseWheelListener()
            {
            public void mouseWheelMoved(MouseWheelEvent e) 
                {
                int val = getState() - e.getWheelRotation();
                if (val > getMax()) val = getMax();
                if (val < getMin()) val = getMin();
                setState(val);
                }
            });
        
        addMouseListener(new MouseAdapter()
            {
            public void mousePressed(MouseEvent e)
                {
                mouseDown = true;
                startX = e.getX();
                startY = e.getY();
                startState = getState();
                status = STATUS_DIAL_DYNAMIC;
                repaint();
                }
                        
            public void mouseReleased(MouseEvent e)
                {
                status = STATUS_STATIC;
                repaint();
                }
            });
                        
        addMouseMotionListener(new MouseMotionAdapter()
            {
            public void mouseDragged(MouseEvent e)
                {
                //int x = e.getX() - startX;
                int y = -(e.getY() - startY);
                int range = (getMax() - getMin() + 1 );
                int multiplicand = 1;
                if (range < MAX_EXTENT)
                    multiplicand = MAX_EXTENT / range;
                                        
                // at present we're just going to use y.  It's confusing to use either y or x.
                setState(startState + y / multiplicand);
                field.setText(map(getState()));
                repaint();
                }
            });

        // This gunk fixes a BAD MISFEATURE in Java: mouseReleased isn't sent to the
        // same component that received mouseClicked.  What the ... ? Asinine.
        // So we create a global event listener which checks for mouseReleased and
        // calls our own private function.  EVERYONE is going to do this.
        long eventMask = AWTEvent.MOUSE_EVENT_MASK;
                
        Toolkit.getDefaultToolkit().addAWTEventListener( new AWTEventListener()
            {
            public void eventDispatched(AWTEvent e)
                {
                if (e instanceof MouseEvent && e.getID() == MouseEvent.MOUSE_RELEASED)
                    {
                    mouseReleased((MouseEvent)e);
                    }
                }
            }, eventMask);

        setState(getState());
        setLayout(new BorderLayout());
        add(field, BorderLayout.CENTER);
        field.setText(map(getState()));
        repaint();
        }
        
    /** Returns the actual square within which the Dial's circle
        is drawn. */
    public Rectangle getDrawSquare()
        {
        Insets insets = getInsets();
        Dimension size = getSize();
        int width = size.width - insets.left - insets.right;
        int height = size.height - insets.top - insets.bottom;
                
        // How big do we draw our circle?
        if (width > height)
            {
            // base it on height
            int h = height;
            int w = h;
            int y = insets.top;
            int x = insets.left + (width - w) / 2;
            return new Rectangle(x, y, w, h);
            }
        else
            {
            // base it on width
            int w = width;
            int h = w;
            int x = insets.left;
            int y = insets.top + (height - h) / 2;
            return new Rectangle(x, y, w, h);
            }
        }

    public boolean isSymmetric() { if (map != null) return map.isSymmetric(); else return getCanonicalSymmetric(); } 
        
    public boolean getCanonicalSymmetric() { return subtractForDisplay == 64; }
        
    public double getCanonicalStartAngle()
        {
        if (isSymmetric())
            {
            return 90 + (270 / 2);
            }
        else
            {
            return 270;
            }
        }
                
    public double getStartAngle()
        {
        if (map != null)
            return map.getStartAngle();
        else return getCanonicalStartAngle();
        }
                
        
    public void paintComponent(Graphics g)
        {
        int min = getMin();
        int max = getMax();
                
        Synth.prepareGraphics(g);
                
        Graphics2D graphics = (Graphics2D) g;
                
        Rectangle rect = getBounds();
        rect.x = 0;
        rect.y = 0;
        graphics.setPaint(Style.BACKGROUND_COLOR);
        graphics.fill(rect);
        rect = getDrawSquare();
        graphics.setPaint(Style.DIAL_UNSET_COLOR);
        graphics.setStroke(Style.DIAL_THIN_STROKE);
        Arc2D.Double arc = new Arc2D.Double();
        
        double startAngle = getStartAngle();
        double interval = -270;
                
        arc.setArc(rect.getX() + Style.DIAL_STROKE_WIDTH / 2, rect.getY() + Style.DIAL_STROKE_WIDTH/2, rect.getWidth() - Style.DIAL_STROKE_WIDTH, rect.getHeight() - Style.DIAL_STROKE_WIDTH, startAngle, interval, Arc2D.OPEN);

        graphics.draw(arc);
        graphics.setStroke(Style.DIAL_THICK_STROKE);
        arc = new Arc2D.Double();
                
        int state = getState();
        interval = -((state - min) / (double)(max - min) * 265) - 5;

        if (status == STATUS_DIAL_DYNAMIC)
            {
            graphics.setPaint(Style.DIAL_DYNAMIC_COLOR);
            if (state == min)
                {
                interval = -5;
                // If we're basically at zero, we still want to show a little bit while the user is scrolling so
                // he gets some feedback. 
                //arc.setArc(rect.getX() + Style.DIAL_STROKE_WIDTH / 2, rect.getY() + Style.DIAL_STROKE_WIDTH/2, rect.getWidth() - Style.DIAL_STROKE_WIDTH, rect.getHeight() - Style.DIAL_STROKE_WIDTH, 270,  -5, Arc2D.OPEN);
                }
            else
                {
                //arc.setArc(rect.getX() + Style.DIAL_STROKE_WIDTH / 2, rect.getY() + Style.DIAL_STROKE_WIDTH/2, rect.getWidth() - Style.DIAL_STROKE_WIDTH, rect.getHeight() - Style.DIAL_STROKE_WIDTH, 270,  -((state - min) / (double)(max - min) * 265) - 5, Arc2D.OPEN);
                }
            }
        else
            {
            graphics.setPaint(staticColor);
            if (state == min)
                {
                interval = 0;
                // do nothing.  Here we'll literally draw a zero
                }
            else
                {
                //arc.setArc(rect.getX() + Style.DIAL_STROKE_WIDTH / 2, rect.getY() + Style.DIAL_STROKE_WIDTH/2, rect.getWidth() - Style.DIAL_STROKE_WIDTH, rect.getHeight() - Style.DIAL_STROKE_WIDTH, 270,  -((state - min) / (double)(max - min) * 265) - 5, Arc2D.OPEN);
                }
            }

        arc.setArc(rect.getX() + Style.DIAL_STROKE_WIDTH / 2, rect.getY() + Style.DIAL_STROKE_WIDTH/2, rect.getWidth() - Style.DIAL_STROKE_WIDTH, rect.getHeight() - Style.DIAL_STROKE_WIDTH, startAngle, interval, Arc2D.OPEN);            
        graphics.draw(arc);
        }

    /** Interface which converts integers into appropriate string values to display in the center of the dial. */
    public interface Map
        {
        /** Maps an integer to an appropriate String value. */ 
        public String map(int val); 
        public boolean isSymmetric(); 
        public double getStartAngle();
        }
    }




