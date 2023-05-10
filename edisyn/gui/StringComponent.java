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
import java.util.*;


/**
   A simple widget maintains a string value in the model.  This widget appears
   as a BUTTON with the string value on it.  When the button is pressed, the user
   is prompted to change the text value.  We might change this to a text field
   in the future, but this at least makes it easy for us to guarantee constraints
   on the text.

   @author Sean Luke
*/

public class StringComponent extends JComponent implements Updatable, HasKey
    {
    JLabel label;
    JButton change;
    Random rand = new Random();
    int maxLength;
        
    String key;
    String instructions;
    Synth synth;
    
    
    static String[] wordList = null;
    
    public String getKey() { return key; }

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
    
    public JButton getButton() { return change; }
    public String getTitle() { return label.getText().trim(); }
    public String getCommand() { return "Enter " + getTitle(); }
    
    /** Override this method to convert the text field to a combo box and provide a drop-down list of items for it. */
    public String[] getList() { return new String[0]; }
    
    public StringComponent(final String _label, final Synth synth, final String key, int maxLength, String instructions)
        {
        super();
        this.key = key;
        this.synth = synth;
        this.maxLength = maxLength;
        this.instructions = instructions;
        synth.getModel().register(key, this);
                
        setBackground(Style.BACKGROUND_COLOR());
        setLayout(new BorderLayout());
                
        label = new JLabel("  " + _label);
        	
        if (_label != null)
            {
            label.setFont(Style.SMALL_FONT());
            label.setBackground(Style.BACKGROUND_COLOR());  // Style.TRANSPARENT);
            label.setForeground(Style.TEXT_COLOR());
            add(label, BorderLayout.NORTH);
            }
 
        String txt = "";
        for(int i = 0; i < maxLength; i++)
            txt = txt + "m";
        change = new JButton("<html>"+txt+"</html>");
        change.putClientProperty("JComponent.sizeVariant", "small");
        change.setFont(Style.SMALL_FONT());
        change.setPreferredSize(change.getPreferredSize());
        change.setHorizontalAlignment(SwingConstants.CENTER);
                
        final Color foreground = change.getForeground();
        if (Style.isMacOSMonterey() || Style.isMacOSVentura()) 
        	change.addMouseListener(buildUnderliningMouseAdapter(change));
        	
        change.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                perform(StringComponent.this);
                }
            });
                
        add(change, BorderLayout.SOUTH);
        synth.getModel().set(key, "");          // gotta set it to something
        }
        
    public void perform(Component parent)
        {
        String currentText = synth.getModel().get(key, "");
        while(true)
            {
            String val = synth.getModel().get(key, "").trim();
            
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            JPanel panel1 = new JPanel();
            panel1.setLayout(new BorderLayout());
            panel1.add(new JLabel(getCommand()), BorderLayout.WEST);
            panel.add(panel1, BorderLayout.NORTH);
            
            JComponent textComponent = null;
			if (getList().length == 0)
				{
				final JTextField text = new JTextField(maxLength);
				text.setText(currentText);        
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
				textComponent = text;
				}
			else
				{
				String[] list = getList();
				String[] listPlus = new String[list.length + 1];
				System.arraycopy(list, 0, listPlus, 1, list.length);
				listPlus[0] = currentText;
				final JComboBox text = new JComboBox(listPlus);
				text.setSelectedItem(currentText);
				text.addAncestorListener(new javax.swing.event.AncestorListener()
					{
					public void ancestorAdded(javax.swing.event.AncestorEvent e)    
						{ 
						JComponent component = e.getComponent();
						component.requestFocusInWindow();
						text.getEditor().selectAll(); 
						}
					public void ancestorMoved(javax.swing.event.AncestorEvent e) {}
					public void ancestorRemoved(javax.swing.event.AncestorEvent e) {}
					});
				text.setEditable(true);
				textComponent = text;
				}
				
            panel.add(textComponent, BorderLayout.CENTER);
                    
            synth.disableMenuBar();
            int opt = JOptionPane.showOptionDialog(parent, panel, getTitle(),
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[] { "Enter", "Cancel", "Rand", "Rules" }, "Enter");
            synth.enableMenuBar();
                                        
            if (opt == 3)       // this is "Rules"
                {
                synth.disableMenuBar();
                JOptionPane.showMessageDialog(parent, instructions, "Rules", JOptionPane.INFORMATION_MESSAGE);
                synth.enableMenuBar();
                }
            else if (opt == 2)       // this is "Rand"
                {
                if (wordList == null)
                    {
                    Scanner scan = new Scanner(StringComponent.class.getResourceAsStream("wordlist.txt"));
                    ArrayList<String> tokens = new ArrayList<>();
                    while (scan.hasNext()) tokens.add(scan.nextLine());
                    wordList = tokens.toArray(new String[0]);
                    }
                if (wordList != null && wordList.length > 0)
                    {
                    String word = wordList[rand.nextInt(wordList.length)];
                    currentText = word.substring(0, 1).toUpperCase() + word.substring(1);
                    }
                else
                    {
                    synth.showSimpleError("Random Word", "Error in loading random wordList, sorry.");
                    }
                }
            else if (opt == 1)  // this is "Cancel"
                { 
                return; 
                }
            else                                // This is "Enter"
                {
                String result = convert("" + (textComponent instanceof JTextField ? 
                								((JTextField)textComponent).getText() :
                								((JComboBox)textComponent).getSelectedItem()));
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

    public void paintComponent(Graphics g)
        {
        Graphics2D graphics = (Graphics2D) g;

        Rectangle rect = getBounds();
        rect.x = 0;
        rect.y = 0;
        graphics.setPaint(Style.BACKGROUND_COLOR());
        graphics.fill(rect);
        }



   /// The purpose of this method is to make a custom Mouse Adapter which underlines the text in the button
    /// when pressed as an additional cue that the button has been pressed due to the extremely muted button
    /// shade change in MacOS Monterey and Ventura.  I'd like to instead change the background color to something
    /// darker but this is very difficult to do in MacOS.
    
 	MouseAdapter buildUnderliningMouseAdapter(final JButton button)
    	{
	    final AWTEventListener[] releaseListener = { null };
	    
		return new MouseAdapter()
            {
            public void mouseExited(MouseEvent e)
            	{
        			button.setText("<html>"+getText()+"</html>");
                repaint();
            	}

            public void mouseEntered(MouseEvent e)
            	{
            	if (releaseListener[0] != null)
            		{
        			button.setText("<html><u>"+getText()+"</u></html>");
                	repaint();
                	}
            	}
            	
            public void mousePressed(MouseEvent e) 
                {
        			button.setText("<html><u>"+getText()+"</u></html>");
                
                // This gunk fixes a BAD MISFEATURE in Java: mouseReleased isn't sent to the
                // same component that received mouseClicked.  What the ... ? Asinine.
                // So we create a global event listener which checks for mouseReleased and
                // calls our own private function.  EVERYONE is going to do this.
                                                        
                Toolkit.getDefaultToolkit().addAWTEventListener( releaseListener[0] = new AWTEventListener()
                    {
                    public void eventDispatched(AWTEvent evt)
                        {
                        if (evt instanceof MouseEvent && evt.getID() == MouseEvent.MOUSE_RELEASED)
                            {
                            MouseEvent e = (MouseEvent) evt;
                            if (releaseListener[0] != null)
                                {
                                Toolkit.getDefaultToolkit().removeAWTEventListener( releaseListener[0] );
                                releaseListener[0] = null;
        			button.setText("<html>"+getText()+"</html>");
								repaint();
                                }
                            }
                        }
                    }, AWTEvent.MOUSE_EVENT_MASK);
                }
                
            public void mouseReleased(MouseEvent e) 
                {
                Toolkit.getDefaultToolkit().removeAWTEventListener( releaseListener[0] );
				releaseListener[0] = null;
        			button.setText("<html>"+getText()+"</html>");
                repaint();
                }
        	};
    	}
    }
