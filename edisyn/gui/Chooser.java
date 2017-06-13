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
   A wrapper for JComboBox which edits and responds to changes to a numerical value
   in the model. The numerical value is assumed to of a min/max range 0...n-1,
   which corresponds to the n elements displayed in the JComboBox.  However you can
   change this an in fact map each element to its own special integer.
        
   For the Mac, the JComboBox is made small (JComponent.sizeVariant = small), but this
   probably won't do anything in Linux or Windows.
        
   @author Sean Luke
*/

public class Chooser extends NumericalComponent
    {
    JComboBox combo;

    // The integers corresponding to each element in the JComboBox.
    int[] vals;

    JLabel label = new JLabel("888", SwingConstants.LEFT)
        {
        public Insets getInsets() { return new Insets(0, 0, 0, 0); }
        };

    public void update(String key, Model model) 
        { 
        if (combo == null) return;  // we're not ready yet
                
        int state = getState();
                
        // it's possible that we're sharing a parameter
        // (see for example Blofeld Parameter 9), so here
        // we need to make sure we're within bounds
        if (state < 0)
            state = 0;
        if (state > vals.length)
            state = vals.length - 1;
                
        // look for it...
        for(int i = 0; i < vals.length; i++)
            if (vals[i] == state)
                {
                combo.setSelectedIndex(i);
                return;
                }
        }

    public Insets getInsets() 
    	{ 
    	if (Style.CHOOSER_INSETS == null)
    		return super.getInsets();
    	else return Style.CHOOSER_INSETS; 
    	}

    /** Creates a JComboBox with the given label, modifying the given key in the Style.
        The elements in the box are given by elements, and their corresponding numerical
        values in the model are given in vals. */
    //public Chooser(String _label, Synth synth, String key, String[] elements, int[] vals)
    //    {
    //    this(_label, synth, key, elements);
    //    System.arraycopy(vals, 0, this.vals, 0, vals.length);
    //    }
                
    /** Creates a JComboBox with the given label, modifying the given key in the Style.
        The elements in the box are given by elements, and their corresponding numerical
        values in the model 0...n. */
    public Chooser(String _label, Synth synth, String key, String[] elements)
        {
        super(synth, key);
                
        label.setFont(Style.SMALL_FONT);
        label.setBackground(Style.TRANSPARENT);
        label.setForeground(Style.TEXT_COLOR);
        //label.setMaximumSize(label.getPreferredSize());

        combo = new JComboBox(elements);
        combo.putClientProperty("JComponent.sizeVariant", "small");
        combo.setEditable(false);
        combo.setFont(Style.SMALL_FONT);
        combo.setMaximumRowCount(32);
        
        setElements(_label, elements);

        setState(getState());
                
        setLayout(new BorderLayout());
        add(combo, BorderLayout.CENTER);
        add(label, BorderLayout.NORTH);
                
        // we don't use an actionlistener here because of a Java bug.
        // Unlike other widgets (like JCheckBox), JComboBox calls
        // the actionlistener even when you programmatically change
        // its value.  OOPS.
        combo.addItemListener(new ItemListener()
            {
            public void itemStateChanged(ItemEvent e)
                {
                if (e.getStateChange() == ItemEvent.SELECTED)
                    {
                    setState(combo.getSelectedIndex());
                    }
                }
            });
        }
    
    public JComboBox getCombo()
    	{
    	return combo;
    	}
    	
    public String getElement(int position)
    	{
    	return (String)(combo.getItemAt(position));
    	}
    	
    public int getNumElements()
    	{
    	return combo.getItemCount();
    	}
    	
    public void setElements(String _label, String[] elements)
    	{
        label.setText("  " + _label);
        combo.removeAllItems();
        
        for(int i = 0; i < elements.length; i++)
        	combo.addItem(elements[i]);

        vals = new int[elements.length];
        for(int i = 0; i < vals.length; i++) 
            vals[i] = i;
                        
        setMin(0);
        setMax(elements.length - 1);
        
        combo.setSelectedIndex(0);
        setState(combo.getSelectedIndex());
    	}
        
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
