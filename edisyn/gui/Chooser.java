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
   change this and in fact map each element to its own special integer.
        
   For the Mac, the JComboBox is made small (JComponent.sizeVariant = small), but this
   probably won't do anything in Linux or Windows.
        
   @author Sean Luke
*/

public class Chooser extends NumericalComponent
    {
    JComboBox combo;
    int addToWidth = 0;

    // The integers corresponding to each element in the JComboBox.
    int[] vals;
    String[] labels;
    ImageIcon[] icons;

    JLabel label = new JLabel("888", SwingConstants.LEFT)
        {
        public Insets getInsets() { return new Insets(0, 0, 0, 0); }
        };

    boolean callActionListener = true;
    
    public String map(int val)
        {
        return "" + combo.getItemAt(val);
        }
    
    public void setCallActionListener(boolean val)
        {
        callActionListener = val;
        }
        
    public void updateBorder()
        {
        super.updateBorder();
        if (combo != null && 
            combo.isEnabled() == synth.isShowingMutation())  // this part prevents us from repeatedly calling setEnabled()... which creates a repaint loop
            {
            if (synth.isShowingMutation())
                combo.setEnabled(false);
            else
                combo.setEnabled(true);
            }
        }

    public void update(String key, Model model) 
        { 
        if (combo == null) return;  // we're not ready yet
                
        int state = getState();
                
        // it's possible that we're sharing a parameter
        // (see for example Blofeld Parameter 9), so here
        // we need to make sure we're within bounds
        if (minExists() && state < getMin())
            state = getMin();
        if (maxExists() && state > getMax())
            state = getMax();

        // look for it...
        for(int i = 0; i < vals.length; i++)
            {
            if (vals[i] == state)
                {
                // This is due to a Java bug.
                // Unlike other widgets (like JCheckBox), JComboBox calls
                // the actionlistener even when you programmatically change
                // its value.  OOPS.
                setCallActionListener(false);
                combo.setSelectedIndex(i);
                setCallActionListener(true);
                return;
                }
            }
//        System.err.println("Invalid value for " + key + " (" + state + ")");
        }

    public Insets getInsets() 
        { 
        if (Style.CHOOSER_INSETS() == null)
            return super.getInsets();
        else if (Style.isWindows())
            return Style.CHOOSER_WINDOWS_INSETS();
        else
            return Style.CHOOSER_INSETS(); 
        }

    static int[] buildDefaultValues(String[] elements)
        {
        int[] values = new int[elements.length];
        for(int i = 0; i < values.length; i++) values[i] = i;
        return values;
        }

    /** Creates a JComboBox with the given label, modifying the given key in the Style.
        The elements in the box are given by elements, and their corresponding numerical
        values in the model 0...n. */
    public Chooser(String _label, Synth synth, String key, String[] elements)
        {
        this(_label, synth, key, elements, buildDefaultValues(elements));
        }

    /** Creates a JComboBox with the given label, modifying the given key in the Style.
        The elements in the box are given by elements, and their corresponding numerical
        values in the model 0...n. */
    public Chooser(String _label, Synth synth, String key, String[] elements, int[] values)
        {
        this(_label, synth, key, elements, values, null);
        }

    /** Creates a JComboBox with the given label, modifying the given key in the Style.
        The elements in the box are given by elements, with images in icons, and their corresponding numerical
        values in the model 0...n.   Note that OS X won't behave properly with icons larger than about 34 high. */
    public Chooser(String _label, final Synth synth, final String key, String[] elements, ImageIcon[] icons)
        {
        this(_label, synth, key, elements, buildDefaultValues(elements), icons);
        }

    public Chooser(String _label, final Synth synth, final String key, String[] elements, int[] values, ImageIcon[] icons)
        {
        super(synth, key);
                
        label.setFont(Style.SMALL_FONT());
        label.setBackground(Style.BACKGROUND_COLOR()); // TRANSPARENT);
        label.setForeground(Style.TEXT_COLOR());
        //label.setMaximumSize(label.getPreferredSize());

        combo = new JComboBox(elements)
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

            protected void processMouseEvent(MouseEvent e)
                {
                super.processMouseEvent(e);
                if (e.getID() == MouseEvent.MOUSE_CLICKED)
                    {
                    if (synth.isShowingMutation())
                        {
                        synth.mutationMap.setFree(key, !synth.mutationMap.isFree(key));
                        // wrap the repaint in an invokelater because the dial isn't responding right
                        SwingUtilities.invokeLater(new Runnable() { public void run() { repaint(); } });
                        }
                    }
                }
            };

        combo.putClientProperty("JComponent.sizeVariant", "small");
        combo.setEditable(false);
        combo.setFont(Style.SMALL_FONT());
        combo.setMaximumRowCount(33);           // 33, not 32, to accommodate modulation destinations for Matrix 1000
        
        setElements(_label, elements, values);

        this.icons = icons;
        this.labels = elements;
        if (icons != null)
            {
            combo.setRenderer(new ComboBoxRenderer());
            //if (Style.isMac()) 
            combo.putClientProperty("JComponent.sizeVariant", "regular");
            }

        setState(getState());
                
        setLayout(new BorderLayout());
        add(combo, BorderLayout.CENTER);
        if (isLabelToLeft())
            add(label, BorderLayout.WEST);
        else
            add(label, BorderLayout.NORTH);
        
        
        /// Apparent OS X Java bug: sometimes after you programmatically change
        /// the value of a JComboBox, it no longer sends ActionListener events.  :-(   
        combo.addItemListener(new ItemListener()
            {
            public void itemStateChanged(ItemEvent e)
                {
                // This is due to a Java bug.
                // Unlike other widgets (like JCheckBox), JComboBox calls
                // the actionlistener even when you programmatically change
                // its value.  OOPS.
                if (callActionListener)
                    {
                    setState(vals[combo.getSelectedIndex()]);
                    }
                }
            });
        }
    
    public boolean isLabelToLeft() { return false; }
    
    public void addToWidth(int val)
        {
        addToWidth = val;
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
        
    public int getIndex()
        {
        return combo.getSelectedIndex();
        }
        
    public int getValue()
        {
        return vals[getIndex()];
        }

    public void setIndex(int index)
        {
        setCallActionListener(false);
        combo.setSelectedIndex(index);
        setCallActionListener(true);
        }
        
    public void setLabel(String _label)
        {
        label.setText("  " + _label);
        }
        
    public void setElements(String _label, String[] elements)
        {
        setElements(_label, elements, buildDefaultValues(elements));
        }

    public void setElements(String _label, String[] elements, int[] values)
        {
        setCallActionListener(false);
        label.setText("  " + _label);
        combo.removeAllItems();
        
        for(int i = 0; i < elements.length; i++)
            combo.addItem(elements[i]);

        vals = (int[])values.clone();

        int _min = Integer.MAX_VALUE;
        int _max = Integer.MIN_VALUE;
        for(int i = 0; i < values.length; i++)
            {
            if (_min > values[i]) _min = values[i];
            if (_max < values[i]) _max = values[i];
            }

        setMin(_min);
        setMax(_max);
        setCallActionListener(true);

        combo.setSelectedIndex(0);
        setState(vals[0]);
        revalidate();
        repaint();
        }

    class ComboBoxRenderer extends JLabel implements ListCellRenderer 
        {
        public ComboBoxRenderer() 
            {
            setOpaque(true);
            setHorizontalAlignment(CENTER);
            setVerticalAlignment(CENTER);
            }
 
        /*
         * This method finds the image and text corresponding
         * to the selected value and returns the label, set up
         * to display the text and image.
         */
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) 
            {
            // Get the selected index. (The index param isn't always valid, so just use the value.)
            //int selectedIndex = ((Integer)value).intValue();
                        
            if (index == -1) index = combo.getSelectedIndex();
            if (index == -1) return this;
                        
            if (isSelected) 
                {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
                } 
            else 
                {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
                }
 
            // Set the icon and text.  If icon was null, say so.
            ImageIcon icon = icons[index];
            String label = labels[index];
            setIcon(icon);
            setText(label);
            setFont(list.getFont());
            return this;
            }
        }


    }
