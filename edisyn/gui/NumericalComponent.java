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

public abstract class NumericalComponent extends JComponent implements Updatable
    {
    String key;
    Synth synth;

    /** Returns the min value for the key in the model. */
    public int getMin() { return (int) synth.getModel().getMin(key); }
        
    /** Returns the max value for the key in the model. */
    public int getMax() { return (int) synth.getModel().getMax(key); }
        
    /** Returns whether the max value exists for the key in the model. */
    public boolean maxExists() { return synth.getModel().maxExists(key); }

    /** Returns whether the min value exists for the key in the model. */
    public boolean minExists() { return synth.getModel().minExists(key); }
        
    /** Returns the current value for the key in the model. */
    public int getState() 
        {
        int _default = 0;
        if (minExists())
            _default = getMin();
        return (int) synth.getModel().get(key, _default); 
        }
                
    /** Sets the min value for the key in the model. */
    public void setMin(int val) 
        { 
        synth.getModel().setMin(key, val);  
        setState(getState()); 
        }
                
    /** Sets the max value for the key in the model. */
    public void setMax(int val) 
        { 
        synth.getModel().setMax(key, val);  
        setState(getState()); 
        }
                
    /** Sets the current value for the key in the model. */
    public void setState(int val) 
        { 
        if (maxExists() && minExists())  // we presume we're set up so we can do bounds checking
            {
            int min = getMin();
            int max = getMax();
            if (val < min) val = min;
            if (val > max) val = max;
            }
                
        if (!synth.getModel().exists(key) || getState() != val)
            {
            synth.getModel().set(key, val); 
            update(key, synth.getModel());
            repaint();
            }
        }
        
    /** Registers the NumericalComponent as a listener for changes to the key in the model. */
    // this is here so we can override it in LabelledDial
    // so it's not registered.  That way we can call update()
    // manually on LabelledDial, but update() won't get called
    // automatically on it.  See addLFO(...)
    public void register(String key)
        {
        synth.getModel().register(key, this);
        }
        
    public abstract void update(String key, Model model);
        
    public NumericalComponent(Synth synth, String key)
        {
        super();
        this.key = key;
        this.synth = synth;
        register(key);
        setBackground(Style.BACKGROUND_COLOR);
        }

    /** Mostly fills the background appropriately. */
    public void paintComponent(Graphics g)
        {
        Graphics2D graphics = (Graphics2D) g;
                
        Rectangle rect = getBounds();
        rect.x = 0;
        rect.y = 0;
        graphics.setPaint(Style.BACKGROUND_COLOR);
        graphics.fill(rect);
        }
    }
