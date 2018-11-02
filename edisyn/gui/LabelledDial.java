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
   A labelled dial which the user can modify with the mouse.
   The dial updates the model and changes in response to it.
   For an unlabelled dial, see Dial.
        
   You can add a second label (or in fact, though it's not obvious,
   additional labels!)

   @author Sean Luke
*/

public class LabelledDial extends NumericalComponent
    {
    Dial dial;
    JLabel label;
    Box labelBox;
    Component glue;
    boolean updatesDynamically = true;
    boolean updatingDynamically = false;
        
    public void setEnabled(boolean val)
        {
        dial.setEnabled(val);
        label.setEnabled(val);
        }
        
    public Insets getInsets() { return Style.LABELLED_DIAL_INSETS(); }

    public void update(String key, Model model) 
        {
//        dial.field.setText(map(getState()));
        dial.repaint(); 
        }
        
    public void setLabel(String text)
        {
        label.setText(text);
        label.revalidate();
        label.repaint();
        }
        
    public JLabel getJLabel()
        {
        return label;
        }
    
    public Color getTextColor() { return dial.field.getForeground(); }
    public void setTextColor(Color color) { dial.field.setForeground(color); dial.repaint(); }
    
    public boolean getUpdatesDyamically() { return updatesDynamically; }
    public void setUpdatesDynamically(boolean val) { updatesDynamically = val; }
    public boolean isUpdatingDynamically() { return updatingDynamically; }
    
    public String map(int val) { return "" + (val - dial.subtractForDisplay); }

    public boolean isSymmetric() { return dial.getCanonicalSymmetric(); }
    
    public double getStartAngle() { return dial.getCanonicalStartAngle(); }

    /** Adds a second (or third or fourth or more!) label to the dial, to allow
        for multiline labels. */
    public JLabel addAdditionalLabel(String _label)
        {
        JLabel label2 = new JLabel(_label);
                
        label2.setFont(Style.SMALL_FONT());
        label2.setBackground(Style.BACKGROUND_COLOR()); // TRANSPARENT);
        label2.setForeground(Style.TEXT_COLOR());

        Box box = new Box(BoxLayout.X_AXIS);
        box.add(Box.createGlue());
        box.add(label2);
        box.add(Box.createGlue());
                
        labelBox.remove(glue);
        labelBox.add(box);
        labelBox.add(glue = Box.createGlue());

        revalidate();
        repaint();
        return label2;
        }
    
    public void setLabelFont(Font font)
        {
        dial.field.setFont(font);
        dial.revalidate();
        dial.repaint();
        }
        
    /** Makes a labelled dial for the given key parameter on the given synth, and with the given color and
        minimum and maximum.  Prior to display, subtractForDisplay is 
        SUBTRACTED from the parameter value.  You can use this to convert 0...127 in the model
        to -64...63 on-screen, for example.  */
    public LabelledDial(String _label, Synth synth, String key, Color staticColor, int min, int max, int subtractForDisplay)
        {
        this(_label, synth, key, staticColor, min, max);
        dial.subtractForDisplay = subtractForDisplay;
        update(key, synth.getModel());
        repaint();
        }

    /** Makes a labelled dial for the given key parameter on the given synth, and with the given color and
        minimum and maximum. */
    public LabelledDial(String _label, Synth synth, String key, Color staticColor, int min, int max)
        {
        this(_label, synth, key, staticColor);
        setMin(min);
        setMax(max);
        synth.getModel().setMetricMin(key, min);
        synth.getModel().setMetricMax(key, max);
        setState(getState());
        }

    /** Makes a labelled dial for the given key parameter on the given synth, and with the given color.  No minimum or maximum is set. */
    LabelledDial(String _label, Synth synth, String key, Color staticColor)
        {
        super(synth, key);
        setBackground(Style.BACKGROUND_COLOR());
        dial = new Dial(staticColor);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Style.BACKGROUND_COLOR());
        panel.add(dial, BorderLayout.CENTER);

        label = new JLabel(_label);
        if (_label != null)
            {
            label.setFont(Style.SMALL_FONT());
            label.setBackground(Style.BACKGROUND_COLOR());  // TRANSPARENT);
            label.setForeground(Style.TEXT_COLOR());

            labelBox = new Box(BoxLayout.Y_AXIS);
            Box box = new Box(BoxLayout.X_AXIS);
            box.add(Box.createGlue());
            box.add(label);
            box.add(Box.createGlue());
            labelBox.add(box);
            labelBox.add(glue = Box.createGlue());
            panel.add(labelBox, BorderLayout.SOUTH);
            }
        
        setLayout(new BorderLayout());
        add(panel, BorderLayout.NORTH);
        }

    public int reviseToAltValue(int val) { return val; }

    public int getDefaultValue()
        {
        if (isSymmetric())
            {
            return (int)Math.ceil((getMin() + getMax()) / 2.0);             // we do ceiling so we push to 64 on 0...127
            }
        else return getMin();
        }
                
    /** A useful utility function which returns the element in the sorted 
        (low to high) array A which is closest to VALUE.  You could use this
        to search arrays for alt values for rounding for example. */
                
    public static int findClosestValue(int value, int[] a) 
        {
        if (value < a[0]) { return a[0]; }
        if (value > a[a.length-1]) { return a[a.length-1]; }

        int lo = 0;
        int hi = a.length - 1;

        while (lo <= hi) 
            {
            // this could obviously overflow if hi and lo are really big
            int mid = (hi + lo) / 2;

            if (value < a[mid]) 
                hi = mid - 1;
            else if (value > a[mid]) 
                lo = mid + 1;
            else 
                return a[mid];
            }
                
        return (a[lo] - value) < (value - a[hi]) ? a[lo] : a[hi];
        }


    class Dial extends JPanel
        {
        // What's going on?  Is the user changing the dial?
        public static final int STATUS_STATIC = 0;
        public static final int STATUS_DIAL_DYNAMIC = 1;
        int status = STATUS_STATIC;
        Color staticColor;

        // The largest vertical range that a dial ought to go.
        public static final int MAX_EXTENT = 512;
        // The typical vertical range that the dial goes.  128 is reasonable
        public static final int MIN_EXTENT = 128;
        
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

        public Dimension getPreferredSize() { return new Dimension(Style.LABELLED_DIAL_WIDTH(), Style.LABELLED_DIAL_WIDTH()); }
        public Dimension getMinimumSize() { return new Dimension(Style.LABELLED_DIAL_WIDTH(), Style.LABELLED_DIAL_WIDTH()); }
        
        boolean enabled = true;
                
        public void setEnabled(boolean val)
            {
            enabled = val;
            field.setEnabled(val);
            repaint();
            }
        
        void mouseReleased(MouseEvent e)
            {                       
            if (!enabled) return;
            if (mouseDown)
                {
                status = STATUS_STATIC;
                repaint();
                mouseDown = false;
                if (releaseListener != null)
                    Toolkit.getDefaultToolkit().removeAWTEventListener(releaseListener);
                }
            }
 
        int getProposedState(MouseEvent e)
            {
            int y = -(e.getY() - startY);
            int range = (getMax() - getMin() + 1 );
            double multiplicand = 1;
                                        
            double extent = range;
            if (extent < MIN_EXTENT) extent = MIN_EXTENT;
            if (extent > MAX_EXTENT) extent = MAX_EXTENT;
                                        
            multiplicand = extent / (double) range;
                                        
            int proposedState = startState + (int)(y / multiplicand);

            if (((e.getModifiers() & InputEvent.CTRL_MASK) == InputEvent.CTRL_MASK) &&
                    (((e.getModifiers() & InputEvent.BUTTON2_MASK) == InputEvent.BUTTON2_MASK) || 
                    ((e.getModifiers() & InputEvent.ALT_MASK) == InputEvent.ALT_MASK)))
                {
                proposedState = startState + (int)(y / multiplicand / 64);
                }
            else if ((e.getModifiers() & InputEvent.CTRL_MASK) == InputEvent.CTRL_MASK)
                {
                proposedState = startState + (int)(y / multiplicand / 16);
                }
            else if (((e.getModifiers() & InputEvent.ALT_MASK) == InputEvent.ALT_MASK))
                {
                proposedState = startState + (int)(y / multiplicand / 4);
                }
            else if (((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) || 
                ((e.getModifiers() & InputEvent.SHIFT_MASK) == InputEvent.SHIFT_MASK))
                {
                proposedState = reviseToAltValue(proposedState);
                }
            return proposedState;
            }
                
                
 
        
        public Dial(Color staticColor)
            {
            this.staticColor = staticColor;

            field.setFont(Style.DIAL_FONT());
            field.setBackground(Style.BACKGROUND_COLOR()); // TRANSPARENT);
            field.setForeground(Style.TEXT_COLOR());
        
            addMouseWheelListener(new MouseWheelListener()
                {
                public void mouseWheelMoved(MouseWheelEvent e) 
                    {
                    if (!enabled) return;
                    int val = getState() - e.getWheelRotation() / 2;
                    if (val > getMax()) val = getMax();
                    if (val < getMin()) val = getMin();

                    setState(val);
                    }
                });
        
            addMouseListener(new MouseAdapter()
                {
                public void mousePressed(MouseEvent e)
                    {
                    if (!enabled) return;
                    mouseDown = true;
                    startX = e.getX();
                    startY = e.getY();
                    startState = getState();
                    status = STATUS_DIAL_DYNAMIC;
                    repaint();

                    if (releaseListener != null)
                        Toolkit.getDefaultToolkit().removeAWTEventListener(releaseListener);

                    // This gunk fixes a BAD MISFEATURE in Java: mouseReleased isn't sent to the
                    // same component that received mouseClicked.  What the ... ? Asinine.
                    // So we create a global event listener which checks for mouseReleased and
                    // calls our own private function.  EVERYONE is going to do this.
                                
                    Toolkit.getDefaultToolkit().addAWTEventListener( releaseListener = new AWTEventListener()
                        {
                        public void eventDispatched(AWTEvent e)
                            {
                            if (e instanceof MouseEvent && e.getID() == MouseEvent.MOUSE_RELEASED)
                                {
                                mouseReleased((MouseEvent)e);
                                }
                            }
                        }, AWTEvent.MOUSE_EVENT_MASK);
                    }
                        
                MouseEvent lastRelease;
                public void mouseReleased(MouseEvent e)
                    {
                    if (!enabled) return;
                    if (e == lastRelease) // we just had this event because we're in the AWT Event Listener.  So we ignore it
                        return;
                    
                    if (!updatesDynamically)
                        {
                        int proposedState = getProposedState(e);
                                                                                
                        // at present we're just going to use y.  It's confusing to use either y or x.
                        if (startState != proposedState)
                            {
                            setState(proposedState);
                            }
                        }
                        
                    status = STATUS_STATIC;
                    repaint();
                    if (releaseListener != null)
                        Toolkit.getDefaultToolkit().removeAWTEventListener(releaseListener);
                    lastRelease = e;
                    }
                
                public void mouseClicked(MouseEvent e)
                    {
                    if (synth.isShowingMutation())
                        {
                        synth.mutationMap.setFree(key, !synth.mutationMap.isFree(key));
                        LabelledDial.this.repaint();
                        }
                    else if (e.getClickCount() == 2)
                        {
                        setState(getDefaultValue());
                        }
                    }
                });
                        
            addMouseMotionListener(new MouseMotionAdapter()
                {
                public void mouseDragged(MouseEvent e)
                    {
                    if (!enabled) return;
                    int proposedState = getProposedState(e);
                                        
                    // at present we're just going to use y.  It's confusing to use either y or x.
                    if (getState() != proposedState)
                        {
                        if (!updatesDynamically)
                            synth.setSendMIDI(false);
                        updatingDynamically = true;
                        setState(proposedState);
                        updatingDynamically = false;
                        if (!updatesDynamically)
                            synth.setSendMIDI(true);
                        }
                    }
                });

            setLayout(new BorderLayout());
            add(field, BorderLayout.CENTER);
            repaint();
            }
        
        AWTEventListener releaseListener = null;
        
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
        
        public boolean getCanonicalSymmetric() 
            { 
            return subtractForDisplay == 64 || subtractForDisplay == 50 || getMax() == (0 - getMin()) || (getMax() == 127 && getMin() == -128);              
            }
        
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
                
        public void paintComponent(Graphics g)
            {
            // revise label if needed
            String val = map(getState());
            if (!(val.equals(dial.field.getText())))
                dial.field.setText(val);

            super.paintComponent(g);

            int min = getMin();
            int max = getMax();
                
            Style.prepareGraphics(g);
                
            Graphics2D graphics = (Graphics2D) g;
                
            Rectangle rect = getBounds();
            rect.x = 0;
            rect.y = 0;
            graphics.setPaint(Style.BACKGROUND_COLOR());
            graphics.fill(rect);
            rect = getDrawSquare();
            graphics.setPaint(Style.DIAL_UNSET_COLOR());
            graphics.setStroke(Style.DIAL_THIN_STROKE());
            Arc2D.Double arc = new Arc2D.Double();
        
            double startAngle = getStartAngle();
            double interval = -270;
                
            arc.setArc(rect.getX() + Style.DIAL_STROKE_WIDTH() / 2, rect.getY() + Style.DIAL_STROKE_WIDTH()/2, rect.getWidth() - Style.DIAL_STROKE_WIDTH(), rect.getHeight() - Style.DIAL_STROKE_WIDTH(), startAngle, interval, Arc2D.OPEN);

            graphics.draw(arc);

            if (!enabled) return;

            graphics.setStroke(Style.DIAL_THICK_STROKE());
            arc = new Arc2D.Double();
                
            int state = getState();
            interval = -((state - min) / (double)(max - min) * 265) - 5;

            if (status == STATUS_DIAL_DYNAMIC)
                {
                graphics.setPaint(Style.DIAL_DYNAMIC_COLOR());
                if (state == min)
                    {
                    interval = -5;
                    // If we're basically at zero, we still want to show a little bit while the user is scrolling so
                    // he gets some feedback. 
                    //arc.setArc(rect.getX() + Style.DIAL_STROKE_WIDTH() / 2, rect.getY() + Style.DIAL_STROKE_WIDTH()/2, rect.getWidth() - Style.DIAL_STROKE_WIDTH(), rect.getHeight() - Style.DIAL_STROKE_WIDTH(), 270,  -5, Arc2D.OPEN);
                    }
                else
                    {
                    //arc.setArc(rect.getX() + Style.DIAL_STROKE_WIDTH() / 2, rect.getY() + Style.DIAL_STROKE_WIDTH()/2, rect.getWidth() - Style.DIAL_STROKE_WIDTH(), rect.getHeight() - Style.DIAL_STROKE_WIDTH(), 270,  -((state - min) / (double)(max - min) * 265) - 5, Arc2D.OPEN);
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
                    //arc.setArc(rect.getX() + Style.DIAL_STROKE_WIDTH() / 2, rect.getY() + Style.DIAL_STROKE_WIDTH()/2, rect.getWidth() - Style.DIAL_STROKE_WIDTH(), rect.getHeight() - Style.DIAL_STROKE_WIDTH(), 270,  -((state - min) / (double)(max - min) * 265) - 5, Arc2D.OPEN);
                    }
                }

            arc.setArc(rect.getX() + Style.DIAL_STROKE_WIDTH() / 2, rect.getY() + Style.DIAL_STROKE_WIDTH()/2, rect.getWidth() - Style.DIAL_STROKE_WIDTH(), rect.getHeight() - Style.DIAL_STROKE_WIDTH(), startAngle, interval, Arc2D.OPEN);            
            graphics.draw(arc);
            }
        }
    }
