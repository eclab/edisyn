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
   A simple widget maintains a string value in the model.  This widget appears
   as a BUTTON with the string value on it.  When the button is pressed, the user
   is prompted to change the text value.  We might change this to a text field
   in the future, but this at least makes it easy for us to guarantee constraints
   on the text.

   @author Sean Luke
*/

public class StringComponent extends JComponent implements Updatable
    {
    JLabel label;
    JButton change;
        
    String key;
    Synth synth;

    /** Returns the current value for the key in the model. */
    public String getText() { return synth.getModel().get(key, ""); }
        
    /** Sets the current value for the key in the model. */
    public void setText(String val) 
        { 
        String state = getText(); 
        if (state == null || !val.equals(state)) 
            {
            synth.getModel().set(key, val); 
            update(key, synth.getModel());
            repaint();
            }
        }
        
    public void update(String key, Model model)
        {
        change.setText(getText().trim());
        }
        
    /** Override this method to verify whether the string is a valid one to store. */
    public boolean isValid(String val)
        {
        return true;
        }
      
    /** Override this method to replace the string with a valid one.  The default
    	returns null, which instructs StringComponent to instead call isValid() and
    	report to the user that the name is not valid. */
    public String replace(String val)
        {
        return null;
        }
      
    /** Override this method to convert val prior to determining if it is valid, perhaps
        to make it all-uppercase, for example. */  
    public String convert(String val)
        {
        return val;
        }
        
    public StringComponent(final String _label, final Synth synth, final String key, int maxLength, final String instructions)
        {
        super();
        this.key = key;
        this.synth = synth;
        synth.getModel().register(key, this);
                
        setBackground(Style.BACKGROUND_COLOR);
        setLayout(new BorderLayout());
                
        label = new JLabel("  " + _label);
        label.setFont(Style.SMALL_FONT);
        label.setBackground(Style.TRANSPARENT);
        label.setForeground(Style.TEXT_COLOR);
                
        String txt = "";
        for(int i = 0; i < maxLength; i++)
            txt = txt + "m";
        change = new JButton(txt);
        change.putClientProperty("JComponent.sizeVariant", "small");
        change.setFont(Style.SMALL_FONT);
        change.setPreferredSize(change.getPreferredSize());
        change.setHorizontalAlignment(SwingConstants.LEFT);
                
        change.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                while(true)
                    {
                    String val = synth.getModel().get(key, "").trim();
                    VBox vbox = new VBox();
                    vbox.add(new JLabel("Enter " + _label));
                    JTextField text = new JTextField(maxLength);
                    vbox.add(text);
                    int opt = JOptionPane.showOptionDialog(StringComponent.this, vbox, _label,
                    				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[] { "Enter",  "Cancel", "Rules"}, "Enter");

                    if (opt == JOptionPane.CANCEL_OPTION) 	// this is "Rules"
                    	{
                    	JOptionPane.showMessageDialog(StringComponent.this, instructions, "Rules", JOptionPane.INFORMATION_MESSAGE);
                    	}
                    else if (opt == JOptionPane.NO_OPTION)  // this is "Cancel"
                    	{ 
                    	return; 
                    	}
                    else
                    	{
						String result = convert(text.getText());
						if (result == null) return;
						String str = replace(result);
						if (str == null)
							{
							if (isValid(result))
								{ 
								setText(result); 
								return;
								}
							}
						else
							{
							setText(str); 
							return;
							}
						}
                    }
                }
            });
                
        add(label, BorderLayout.NORTH);
        add(change, BorderLayout.SOUTH);
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
