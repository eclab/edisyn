package edisyn.gui;
import java.awt.*;
import javax.swing.*;
 
public class WidgetList extends JPanel
    {
    /*
    public WidgetList(String[] labels, JComponent[] widgets)
        {
        int max = 0;
        JLabel[] jlabels = new JLabel[labels.length];
        for(int i = 0; i < labels.length; i++)
            {
            jlabels[i] = new JLabel(labels[i] + " ", SwingConstants.RIGHT);
            int width = (int)(jlabels[i].getPreferredSize().getWidth());
            if (width > max) max = width;   
            }

        Box vbox = new Box(BoxLayout.Y_AXIS);
        for(int i = 0; i < labels.length; i++)
            {
            jlabels[i].setPreferredSize(new Dimension(
                    max, (int)(jlabels[i].getPreferredSize().getHeight())));
            jlabels[i].setMinimumSize(jlabels[i].getPreferredSize());
            // for some reason this has to be set as well
            jlabels[i].setMaximumSize(jlabels[i].getPreferredSize());
            Box hbox = new Box(BoxLayout.X_AXIS);
            hbox.add(jlabels[i]);
            hbox.add(widgets[i]);
            vbox.add(hbox);
            }
        
        setLayout(new BorderLayout());
        add(vbox, BorderLayout.SOUTH);
        }
        */

    public WidgetList(String[] labels, JComponent[] widgets)
        {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        
        for(int i = 0; i < labels.length; i++)
        	{
        	c.gridx = 0;
        	c.gridy = i;
        	c.gridwidth = 1;
        	c.gridheight = 1;
        	c.fill = GridBagConstraints.HORIZONTAL;
        	c.anchor = GridBagConstraints.LINE_END;
        	c.weightx = 0;
        	c.weighty = 1;
        	panel.add(new JLabel(labels[i] + " ", SwingConstants.RIGHT), c);
        	
        	c.gridx = 1;
        	c.anchor = GridBagConstraints.LINE_START;
        	c.weightx = 1;
        	panel.add(widgets[i], c);
        	}
        
        setLayout(new BorderLayout());
        add(panel, BorderLayout.SOUTH);
        }

    }
        
