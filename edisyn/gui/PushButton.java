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
   A simple button which calls perform() when pressed.

   @author Sean Luke
*/

public class PushButton extends JButton
    {
    public PushButton(final String text)
        {
        super(text);
        putClientProperty("JComponent.sizeVariant", "small");
        setFont(Style.SMALL_FONT);
        setHorizontalAlignment(SwingConstants.CENTER);
                
        addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                perform();
                }
            });
        }
    
    public void perform()
    	{
    	// do nothing by default
    	}
    }
