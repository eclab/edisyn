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
   A simple button with some useful features.  
   
   1. If you construct the button as new PushButton(text), it's just a button 
   which calls perform() when pressed.  Override perform() as you see fit.
      
   2. If you construct the button as new PushButton(text, String[]), pressing
   the button will pop up a menu with the various String[] options, and call
   perform(int) if one of the options is selected.
      
   3. If you construct the button as new PushButton(text, JMenuItem[]), pressing
   the button will pop up a menu with those JMenuItem[] options. 

   @author Sean Luke
*/

public class PushButton extends JPanel
    {
    JButton button;
    JPopupMenu pop;
    
    public Insets getInsets() { return new Insets(0,0,0,0); }
    
    public JButton getButton() { return button; }
    
    public PushButton(final String text)
        {
        button = new JButton(text);
        button.putClientProperty("JComponent.sizeVariant", "small");
        button.setFont(Style.SMALL_FONT());
        button.setHorizontalAlignment(SwingConstants.CENTER);
                
        button.addActionListener(new ActionListener()
            {
		public void actionPerformed(ActionEvent e)
                {
                _perform();
                }
            });
        setBackground(Style.BACKGROUND_COLOR());
        setLayout(new BorderLayout());
	JPanel inner = new JPanel()
	    {
		public Insets getInsets() { return new Insets(0,0,0,0); }
	    };
	inner.setLayout(new BorderLayout());
	inner.setBackground(Style.BACKGROUND_COLOR());
	inner.add(button, BorderLayout.NORTH);
        add(inner, BorderLayout.WEST);

        if (Style.isNimbus())
        	{
        	//add(Strut.makeHorizontalStrut(3), BorderLayout.WEST);
        	add(Strut.makeVerticalStrut(3), BorderLayout.SOUTH);
			}
        }
    
    public PushButton(String text, String[] options)
        {
        this(text, options, null);
        }

    public void setName(String text)
        {
        button.setText(text);
        }

    public void setOptions(String[] options)
        {
        setOptions(options, null);
        }

    public void setOptions(String[] options, boolean[] enabled)
        {
        pop.removeAll();
        for(int i = 0; i < options.length; i++)
            {
            if (options[i] == null)
                {
                pop.addSeparator();
                }
            else
                {
                JMenuItem menu = new JMenuItem(options[i]);
                if (enabled != null)
                    {
                    menu.setEnabled(enabled[i]);
                    }
                final int _i = i;
                menu.addActionListener(new ActionListener()
                    {
                    public void actionPerformed(ActionEvent e)      
                        {
                        perform(_i);
                        }       
                    });     
                pop.add(menu);
                }
            }
        }

    public void setOptions(JMenuItem[] menuItems)
        {
        pop.removeAll();
        for(int i = 0; i < menuItems.length; i++)
            {
            if (menuItems[i] == null)
                pop.addSeparator();
            else
                pop.add(menuItems[i]);
            }
        }

    public PushButton(String text, String[] options, boolean[] enabled)
        {
        this(text);
        pop = new JPopupMenu();
        setOptions(options, enabled);
        }
    
    public PushButton(String text, JMenuItem[] menuItems)
        {
        this(text);
        pop = new JPopupMenu();
        setOptions(menuItems);
        }
    
    void _perform()
        {
        if (pop != null)
            {
            button.add(pop);
            if (Style.isMac())
                {
                // Mac buttons have strange insets, and only the top and bottom match the
                // actual border.
                Insets insets = button.getInsets();
                pop.show(button, 0/*button.getBounds().x*/ + insets.top, button.getBounds().y + button.getBounds().height - insets.bottom);
                }
            else
                {
                pop.show(button, 0/*button.getBounds().x*/, button.getBounds().y + button.getBounds().height);
                }
            button.remove(pop);
            }
        else
            {
            perform();
            }
        }
    
    public void perform()
        {
        }
        
    public void perform(int i)
        {
        }
    }
