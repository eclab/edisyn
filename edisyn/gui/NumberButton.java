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


public class NumberButton extends NumericalComponent
    {
    JLabel label;
    JButton change;
    
    public void update(String key, Model model)
        {
        change.setText("" + model.get(key, 0));
        }
        
    public int read(String val)
        {
        int num;
        try
            {
            num = Integer.parseInt(val);
            }
        catch (NumberFormatException ex)
            {
            num = synth.getModel().get(key, 0);
            }
        return num;
        }
          
    public JButton getButton() { return change; }
    public String getTitle() { return label.getText().trim(); }
    public String getCommand() { return "Enter " + getTitle(); }
    
    public NumberButton(final String _label, final Synth synth, final String key, final int minVal, final int maxVal, final String instructions)
        {
        super(synth, key);        
        setLayout(new BorderLayout());
                
        label = new JLabel("  " + _label);
        if (_label != null)
            {
            label.setFont(Style.SMALL_FONT());
            label.setBackground(Style.BACKGROUND_COLOR());
            label.setForeground(Style.TEXT_COLOR());
            add(label, BorderLayout.NORTH);
            }
 
        String txt = "" + maxVal;
        String mintxt = "" + minVal;
        if (mintxt.length() > txt.length())
            txt = mintxt;
        final int maxLength = txt.length();
        change = new JButton(txt);
        change.putClientProperty("JComponent.sizeVariant", "small");
        change.setFont(Style.SMALL_FONT());
        change.setPreferredSize(change.getPreferredSize());
        change.setHorizontalAlignment(SwingConstants.CENTER);
                
        change.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                while(true)
                    {
                    int val = synth.getModel().get(key, 0);
                    VBox vbox = new VBox();
                    vbox.add(new JLabel(getCommand()));
                    JTextField text = new JTextField(maxLength);
                    text.setText("" + val);
                    
                    // The following hack is inspired by https://tips4java.wordpress.com/2010/03/14/dialog-focus/
                    // and results in the text field being selected (which is what should have happened in the first place) 
                    
                    text.addAncestorListener(new javax.swing.event.AncestorListener()
                        {
                        public void ancestorAdded(javax.swing.event.AncestorEvent e)    
                            { 
                            JComponent component = e.getComponent();
                            component.requestFocusInWindow();
                            text.selectAll(); 
                            }
                        public void ancestorMoved(javax.swing.event.AncestorEvent e) {}
                        public void ancestorRemoved(javax.swing.event.AncestorEvent e) {}
                        });
                    vbox.add(text);
                    synth.disableMenuBar();
                    int opt = JOptionPane.showOptionDialog(NumberButton.this, vbox, getTitle(),
                        JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[] { "Enter",  "Cancel", "Rules"}, "Enter");
                    synth.enableMenuBar();
                                        
                    if (opt == JOptionPane.CANCEL_OPTION)       // this is "Rules"
                        {
                        synth.disableMenuBar();
                        JOptionPane.showMessageDialog(NumberButton.this, instructions, "Rules", JOptionPane.INFORMATION_MESSAGE);
                        synth.enableMenuBar();
                        }
                    else if (opt == JOptionPane.NO_OPTION)  // this is "Cancel"
                        { 
                        return; 
                        }
                    else
                        {
                        submitValue(read(text.getText()));
                        return;
                        }
                    }
                }
            });
                
        add(change, BorderLayout.SOUTH);

        synth.getModel().setMax(key, maxVal);
        synth.getModel().setMin(key, minVal);
        synth.getModel().set(key, minVal);
        }

    /** Submits a new number in response to a user request.  Override this as you see fit. */
    public void submitValue(int val)
        {
        setState(val);
        }

    public void paintComponent(Graphics g)
        {
        Graphics2D graphics = (Graphics2D) g;

        Rectangle rect = getBounds();
        rect.x = 0;
        rect.y = 0;
        graphics.setPaint(Style.BACKGROUND_COLOR());
        graphics.fill(rect);
        }
    }
