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

public abstract class NumericalComponent extends JComponent implements Updatable, HasKey
    {
    String key;
    Synth synth;
    
    /** Sets the component's key.  Does not update. */
    public void setKey(String key) { this.key = key; }
    
    /** Sets the component's key.  Does not update. */
    public String getKey() { return key; }
    
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
        if (maxExists())  // we presume we're set up so we can do bounds checking
            {
            int max = getMax();
            if (val > max) val = max;
            }

        if (minExists())  // we presume we're set up so we can do bounds checking
            {
            int min = getMin();
            if (val < min) val = min;
            }
                
        // we don't check for duplicates any more so LabelledDial can do non-dynamic updates
        //if (!synth.getModel().exists(key) || getState() != val)
            {
            synth.getModel().set(key, val); 
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
        
    public void updateBorder()
        {
        // If we're supposed to show our mutation, and I'm free to mutate, but I'm not showing it, show it
        if (synth.isShowingMutation() && synth.mutationMap.isFree(key) && synth.getModel().getStatus(key) != Model.STATUS_IMMUTABLE)
            {
            borderColor = Style.DYNAMIC_COLOR();
            }
        // In all other situations, I should not be showing mutation.  If I am, stop it.
        else 
            {
            borderColor = Style.BACKGROUND_COLOR();
            }
        }
        
    public Color borderColor;
    
    public NumericalComponent(final Synth synth, final String key)
        {
        super();
        this.key = key;
        this.synth = synth;
        register(key);
        setBackground(Style.BACKGROUND_COLOR());
        Border border = new LineBorder(Style.BACKGROUND_COLOR(), 1)
            {
            public void paintBorder(final Component c, final Graphics g, final int x, final int y, final int width, final int height) 
                {
                super.lineColor = borderColor;
                super.paintBorder(c, g, x, y, width, height);
                }
            };
        setBorder(border);
        
        addMouseListener(new MouseAdapter()
            {
            public void mouseClicked(MouseEvent e)
                {
                if (synth.isShowingMutation() && synth.getModel().getStatus(key) != Model.STATUS_IMMUTABLE)
                    {
                    synth.mutationMap.setFree(key, !synth.mutationMap.isFree(key));
                    // wrap the repaint in an invokelater because the dial isn't responding right
                    SwingUtilities.invokeLater(new Runnable() { public void run() { repaint(); } });
                    }
                }
            });
        }

    /** Mostly fills the background appropriately. */
    public void paintComponent(Graphics g)
        {
        updateBorder();  // might require paintComponent to get called twice
        super.paintComponent(g);
        }
    }
