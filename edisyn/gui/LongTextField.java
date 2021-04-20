/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package edisyn.gui;

import edisyn.*;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.border.*;
import javax.swing.*;
import java.awt.event.*;


/** A simple class that lets you specify a label and validate a numerical value.  LongTextField assumes access
    to several image files for the widgets to the right of the text field: a left-arrow button, a right-arrow button, 
    and a "belly button".  The left-arrow button decreases the numerical value, the right-arrow button increases it, 
    and the belly button resets it to its initial default value.  You can also change the value in the text field proper.  
    Why use this class instead of a slider?  Because it is not ranged: the numbers can be any value.

    <p>LongTextField lets users increase values according to a provided formula of the form
    value = value * M + A, and similarly decrease values as value = (value - A) / M. You specify the
    values of M and A and the initial default value.  This gives you some control on how values should change:
    linearly or geometrically.

    <p>You can exercise further control by subclassing the class and overriding the newValue(val) method, which
    filters all newly user-set values and "corrects" them.  Programmatically set values (by calling setValue(...)) 
    are not filtered through newValue by default.  If you need to filter, you should do setValue(newValue(val));

    <p>LongTextFields can also be provided with an optional label.
*/

public abstract class LongTextField extends JComponent implements Updatable
    {
    String msb;
    String lsb;
    Synth synth;
    
    JTextField valField;
    JLabel label = new JLabel("888", SwingConstants.LEFT)
        {
        public Insets getInsets() { return new Insets(0, 0, 0, 0); }
        };

    Color defaultColor;
    Color editedColor;
        
    boolean edited = false;
    void setEdited(boolean edited)
        {
        if (this.edited != edited)
            {
            this.edited = edited;
            if (edited)
                {
                valField.setBackground(editedColor);
                }
            else
                {
                valField.setBackground(defaultColor);
                }
            }
        }
        
    public void submit()
        {
        if (edited)
            {
            long val;
            try
                {
                val = Long.parseLong(valField.getText());
                }
            catch (NumberFormatException e) { val = getValue(); }
            setValue(newValue(val));
            }
        }
    
    public void update(String key, Model model)
        {
        setValue(getValue());
        }
        
    KeyListener listener = new KeyListener()
        {
        public void keyReleased(KeyEvent keyEvent) { }
        public void keyTyped(KeyEvent keyEvent) { }
        public void keyPressed(KeyEvent keyEvent) {
            if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER)
                {
                submit();
                }
            else if (keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE)  // reset
                {
                update(null, synth.getModel());  // doesn't matter
                }
            else
                {
                setEdited(true);
                }
            }
        };
    
    FocusAdapter focusAdapter = new FocusAdapter()
        {
        public void focusLost ( FocusEvent e )
            {
            submit();
            }
        };

    /** Sets the value without filtering first. */
    public void setValue(long val)
        {
        valField.setText(""+val);
        setEdited(false);
        }

    
    /** Returns the most recently set value. */
    public long getValue()
        {
        return ((long)(synth.getModel().get(msb, 0)) << 32) | (long)synth.getModel().get(lsb, 0);
        }
        
    public JTextField getField() { return valField; }
            
    /** Creates a LongTextField which does not display the belly button or arrows. */
    public LongTextField(String _label, final Synth synth, int columns, final Color editedColor, final String msb, final String lsb)
        {
        this.msb = msb;
        this.lsb = lsb;
        this.synth = synth;
        
        setBackground(Style.BACKGROUND_COLOR());
        setLayout(new BorderLayout());

        label.setFont(Style.SMALL_FONT());
        label.setBackground(Style.BACKGROUND_COLOR());
        label.setForeground(Style.TEXT_COLOR());
        label.setText(_label);
        add(label, BorderLayout.NORTH);

        valField = new JTextField("", columns);
        valField.putClientProperty("JComponent.sizeVariant", "small");
        valField.addKeyListener(listener);
        valField.addFocusListener(focusAdapter);
        setValue(getValue());
        add(valField,BorderLayout.CENTER);

        this.editedColor = editedColor;
        this.defaultColor = valField.getBackground();
        
        }
        
    /** Override this to be informed when a new value has been set.
        The return value should be the value you want the display to show 
        instead. */
    public abstract long newValue(long newValue);
    
    /** Only call this to access the value field directly */
    public void setText(String val)
        {
        valField.setText(val);
        }
    
    public String getText()
        {
        return valField.getText();
        }
    }
