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


public class TextLabel extends JLabel
    {
    public TextLabel(String text)
        {
        super(text, SwingConstants.LEFT);
        setFont(Style.SMALL_FONT());
        setBackground(Style.BACKGROUND_COLOR());  // Style.TRANSPARENT);
        setForeground(Style.TEXT_COLOR());
        }
    }
