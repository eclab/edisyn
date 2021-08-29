/***
    Copyright 2021 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.gui;

import javax.swing.*;
import java.awt.event.*;

/***
    This class modifies JTextField so that it assumes focus and selects its
    text whenever its JFrame comes front.
***/

public class SelectedTextField extends JTextField
    {
    public SelectedTextField()
        {
        super();
        setup();
        }
        
    public SelectedTextField(String text)
        {
        super(text);
        setup();
        }

    public SelectedTextField(String text, int columns)
        {
        super(text, columns);
        setup();
        }
        
    void setup()
        {
        // The following hack is inspired by https://tips4java.wordpress.com/2010/03/14/dialog-focus/
        // and results in the text field being selected (which is what should have happened in the first place) 
                    
        addAncestorListener(new javax.swing.event.AncestorListener()
            {
            public void ancestorAdded(javax.swing.event.AncestorEvent e)    
                { 
                JComponent component = e.getComponent();
                component.requestFocusInWindow();
                SelectedTextField.this.selectAll(); 
                }
            public void ancestorMoved(javax.swing.event.AncestorEvent e) { }
            public void ancestorRemoved(javax.swing.event.AncestorEvent e) { }
            });             
        }
    }
