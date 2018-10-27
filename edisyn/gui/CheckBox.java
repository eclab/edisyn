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
   A wrapper for JCheckBox so that it updates itself in response to the model. 

   @author Sean Luke
*/

public class CheckBox extends NumericalComponent
    {
    JCheckBox check;
    boolean flipped;
    int addToWidth = 0;

    public void update(String key, Model model) 
        { 
        // we don't compare against min or max here because they
        // could be used by other widgets.  See for example Blofeld parameter 8
        if (flipped)
            check.setSelected(getState() == 0);
        else
            check.setSelected(getState() != 0); 
        }

    public CheckBox(String label, Synth synth, String key)
        {
        this(label, synth, key, false);
        }
    
    public boolean isFlipped() { return flipped; }
     
    public void addToWidth(int val)
        {
        addToWidth = val;
        }
              
    public JCheckBox getCheckBox() { return check; }
      
    boolean enabled = true;
    public void setEnabled(boolean val)
        {
        enabled = val;
        updateBorder();
        }
        
    public void updateBorder()
        {
        super.updateBorder();
        if (synth.isShowingMutation())
            check.setEnabled(false);
        else
            check.setEnabled(true && enabled);
        }
        
    public CheckBox(String label, final Synth synth, final String key, boolean flipped)
        {
        super(synth, key);

        this.flipped = flipped;
                
        check = new JCheckBox(label)
            {
            public Dimension getMinimumSize() 
                {
                return getPreferredSize(); 
                }
            public Dimension getPreferredSize()
                {
                Dimension d = super.getPreferredSize();
                d.width += addToWidth;
                return d;
                }                       
            };
            
        check.setFont(Style.SMALL_FONT());
        check.setOpaque(false);
        //check.setContentAreaFilled(false);
        //check.setBorderPainted(false);
        //check.setBackground(Style.TRANSPARENT);               // creates bugs in Windows
        check.setForeground(Style.TEXT_COLOR());

        setMax(1);
        setMin(0);
        setState(getState());
                
        setLayout(new BorderLayout());
        add(check, BorderLayout.CENTER);
        
        check.addMouseListener(new MouseAdapter()
            {
            public void mouseClicked(MouseEvent e)
                {
                if (synth.isShowingMutation())
                    {
                    synth.mutationMap.setFree(key, !synth.mutationMap.isFree(key));
                    // wrap the repaint in an invokelater because the dial isn't responding right
                    SwingUtilities.invokeLater(new Runnable() { public void run() { repaint(); } });
                    }
                }
            });
        
        
        check.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                if (CheckBox.this.flipped)
                    setState(check.isSelected() ? getMin() : getMax());
                else
                    setState(check.isSelected() ? getMax() : getMin());                             
                }
            });
        }
    }
