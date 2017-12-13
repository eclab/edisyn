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
   A pretty panel for a synthesizer

   @author Sean Luke
*/

public class SynthPanel extends JPanel implements Gatherable
    {             
    public SynthPanel()
        {
        setLayout(new BorderLayout());
        setBackground(Style.BACKGROUND_COLOR);
        setBorder(BorderFactory.createMatteBorder(2, 2, 0, 4, Color.black));
        }

    public Insets getInsets() { return Style.SYNTH_PANEL_INSETS; }

// I thought maybe I'd make little dots which lit up when MIDI came in, but
// have changed my mind.
/*
	public void paint(Graphics graphics)
		{
		super.paint(graphics);
		
		// At this point all children have been painted.  So we're going to query
		// the synth to determine whether to draw some MIDI dots.  However we don't
		// have the synth proper.  We work our way up until we reach it.
		Component component = this;
		while((component != null) && !(component instanceof Synth))
			{
			component = component.getParent();
			}
		if (component == null) return;
		Synth synth = (Synth)component;

		Graphics2D g = (Graphics2D)graphics;
		if (true); // synth.incomingControllerMIDI())
			{
			g.setColor(Color.red);
			g.fillOval(getWidth() - 6 - 2, 2, 6, 6);
			}
		//if (synth.incomingSynthMIDI())
			{
			g.setColor(Color.white);
			g.fillOval(getWidth() - 4 * 2 - 2 * 2, 2, 4, 4);
			}		
		}
*/

    public void gatherAllComponents(java.util.ArrayList list)
        {
        Component[] c = getComponents();
        for(int i = 0; i < c.length; i++)
            {
            list.add(c[i]);
            if (c[i] instanceof Gatherable)
                ((Gatherable)c[i]).gatherAllComponents(list);
            }                       
        }
    }
