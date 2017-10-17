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

public class PushButton extends JPanel
    {
    JButton button;
    
    public Insets getInsets() { return new Insets(0,0,0,0); }
    
    public JButton getButton() { return button; }
    
    public PushButton(final String text)
        {
        button = new JButton(text);
        button.putClientProperty("JComponent.sizeVariant", "small");
        button.setFont(Style.SMALL_FONT);
        button.setHorizontalAlignment(SwingConstants.CENTER);
                
        button.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                perform();
                }
            });
        setBackground(Style.BACKGROUND_COLOR);
        setLayout(new BorderLayout());
        add(button, BorderLayout.CENTER);
        }
    
    public void perform()
        {
        // do nothing by default
        }
    }
