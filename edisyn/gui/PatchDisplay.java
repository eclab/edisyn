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

public class PatchDisplay extends JPanel implements Updatable
    {
    String bankKey;
    String numberKey;
    JLabel text;
    Synth synth;
    boolean showsSync;
    
    public boolean getShowsSync() { return showsSync; }
    public void setShowsSync( boolean val ) { showsSync = val; }
    
    public String numberString(int number)
        {
        return "" + number;
        }
    
    public String bankString(int bank)
        {
        return "" + bank;
        }
        
    public String makeString(Model model)
        {
        String bank = null;
        String number = null;
        if (model.isString(bankKey) && model.get(bankKey, null) != null)
            bank = model.get(bankKey, null);
        else if (model.isInteger(bankKey) && model.get(bankKey, -1) != -1)
            bank = bankString(model.get(bankKey, 0));
        if (model.isString(numberKey) && model.get(numberKey, null) != null)
            number = model.get(numberKey, null);
        else if (model.isInteger(numberKey) && model.get(numberKey, -1) != -1)
            number = numberString(model.get(numberKey, 0));
        String s = "";
        if (bank != null) s = s + bank;
        if (number != null) s = s + number;
        return s;
        }
    
    static String buildInitialString(int columns)
        {
        String s = "";
        for(int i = 0; i < columns; i++)
            s = s + "M";
        return s;
        }
        
    
    public PatchDisplay(Synth synth, String label, String bankKey, String numberKey, int columns)
        {
        this.synth = synth;
        Model model = synth.getModel();
        setBackground(Style.BACKGROUND_COLOR);
        setLayout(new BorderLayout());
        setBorder(Style.PATCH_BORDER);        
                
        final Dimension[] dim = new Dimension[1];
        text = new JLabel(buildInitialString(columns))
            {
            public Dimension getMinimumSize() { return getPreferredSize(); }
            public Dimension getMaximumSize() { return getPreferredSize(); }
            public Dimension getPreferredSize() 
                { if (dim[0] == null) 
                    { return super.getPreferredSize(); }
                else
                    { return dim[0]; }
                }
            };
                
        text.setFont(Style.MEDIUM_FONT);
        text.setText(buildInitialString(columns));
        text.setBackground(Style.BACKGROUND_COLOR);
        text.setForeground(Style.PATCH_UNSYNCED_TEXT_COLOR);
        // lock the preferred size to max of columns
        dim[0] = text.getPreferredSize();
    
        if (bankKey != null)
            {
            model.register(this.bankKey = bankKey, this);
            model.setImmutable(bankKey, true);
            }
        
        if (numberKey != null)
            {
            model.register(this.numberKey = numberKey, this);
            model.setImmutable(numberKey, true);
            }

        text.setText(makeString(model));
        add(text, BorderLayout.CENTER);
        
        //JLabel lab = new JLabel(label);
        //lab.setFont(Style.MEDIUM_FONT);
        JLabel lab = new JLabel(label, SwingConstants.LEFT);
        lab.setFont(Style.SMALL_FONT);
        lab.setBackground(Style.BACKGROUND_COLOR);
        lab.setForeground(Style.TEXT_COLOR);
        //add(lab, BorderLayout.WEST);
        add(lab, BorderLayout.NORTH);
        }
    
    public void update(String key, Model model)
        {
        if (synth.isSynced() || showsSync )
            text.setForeground(Style.TEXT_COLOR);
        else
            text.setForeground(Style.PATCH_UNSYNCED_TEXT_COLOR);
        text.setText(makeString(model));
        text.repaint(); 
        }

    }
