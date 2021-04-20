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
   A labelled wrapper for JScrollBar which edits and responds to changes to a numerical value
   in the model.   Note that this class is presently unused and may be deleted.
        
   @author Sean Luke
*/

public class Scroller extends NumericalComponent
    {
    JScrollBar bar = new JScrollBar(JScrollBar.HORIZONTAL);
    JLabel label = new JLabel("888", SwingConstants.LEFT);
    JLabel field = new JLabel("888", SwingConstants.RIGHT);

    public void update(String key, Model model) 
        { 
        if (bar.getValue() != getState())
            bar.setValue(getState()); 
        field.setText("" + getState());
        }

    public void setMin(int val) 
        { 
        super.setMin(val); 
        bar.setMinimum(getMin());
        Dimension d = new Dimension((int)bar.getMinimumSize().getWidth() + (int)(1.5 * (getMax() - getMin() + 1)), (int)bar.getPreferredSize().getHeight());
        bar.setMinimumSize(d); 
        bar.setPreferredSize(d); 
        }
                
    public void setMax(int val) 
        { 
        super.setMax(val); 
        bar.setMaximum(getMax());
        Dimension d = new Dimension((int)bar.getMinimumSize().getWidth() + (int)(1.5 * (getMax() - getMin() + 1)), (int)bar.getPreferredSize().getHeight());
        bar.setMinimumSize(d); 
        bar.setPreferredSize(d); 
        }

    public Scroller(String _label, Synth synth, String key)
        {
        super(synth, key);
                
        bar.setVisibleAmount(0);  // this irritation causes no matter of problems
        setMin(getMin());
        setMax(getMax());
        synth.getModel().setMetricMin(key, getMin());
        synth.getModel().setMetricMax(key, getMax());

        label.setText(_label);
        label.setFont(Style.SMALL_FONT());
        label.setBackground(Style.BACKGROUND_COLOR()); // Style.TRANSPARENT);
        label.setForeground(Style.TEXT_COLOR());
        label.setBorder(BorderFactory.createLineBorder(Style.BACKGROUND_COLOR())); // Style.TRANSPARENT));

        field.setFont(Style.MEDIUM_FONT());
        field.setBackground(Style.BACKGROUND_COLOR()); // TRANSPARENT);
        field.setForeground(Style.TEXT_COLOR());
        field.setBorder(BorderFactory.createLineBorder(Style.BACKGROUND_COLOR())); // TRANSPARENT));
                
        // lock the size
        Dimension pref = field.getPreferredSize();
        field.setPreferredSize(pref);
        field.setMinimumSize(pref);
                
        setState(getState());
                
        setLayout(new BorderLayout());
        add(bar, BorderLayout.CENTER);
        add(field, BorderLayout.WEST);
        add(label, BorderLayout.NORTH);
                
        bar.addAdjustmentListener(new AdjustmentListener()
            {
            public void adjustmentValueChanged( AdjustmentEvent e)
                {
                setState(bar.getValue());
                }
            });
        }
    }
